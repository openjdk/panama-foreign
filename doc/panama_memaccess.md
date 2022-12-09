## State of foreign memory support

**December 2022**

**Maurizio Cimadamore**

A crucial part of any native interop story lies in the ability of accessing off-heap memory efficiently and safely. Java achieves this goal through the Foreign Function & Memory API (FFM API in short), parts of which have been available as an [incubating](https://openjdk.java.net/jeps/11) API since Java [14](https://openjdk.java.net/jeps/370). The FFM API introduces abstractions to allocate and access flat memory regions (whether on- or off-heap), to manage the lifecycle of memory resources and to model native memory addresses.

### Memory segments and lifetimes

Memory segments are abstractions which can be used to model contiguous memory regions, located either on-heap (i.e. *heap segments*) or off- the Java heap (i.e. *native segments*). Memory segments provide *strong* spatial, temporal and thread-confinement guarantees which make memory dereference operation *safe* (more on that later), although in most simple cases some properties of memory segments can safely be ignored.

For instance, the following snippet allocates 100 bytes off-heap:

```java
MemorySegment segment = MemorySegment.allocateNative(100, SegmentScope.global());
```

The above code allocates a 100-bytes long memory segment. The lifecycle of a memory segment is controlled by an abstraction called `SegmentScope`. In this example, the segment is associated with the simplest scope, called the *global* scope. Memory segments associated with this scope are always *alive* and their backing region of memory are never deallocated. In other words, we say that the above segment has an *unbounded* lifetime.

Most programs, though, require off-heap memory to be deallocated  while the program is running, and thus need memory segments with *bounded* lifetimes. The simplest way to obtain a segment with bounded lifetime is to use an *automatic scope*:

```java
MemorySegment segment = MemorySegment.allocateNative(100, SegmentScope.auto());
```

Segments associated with an automatic scope are alive as long as they are determined to be reachable by the garbage collector. In other words, the above snippet creates a native segment whose behavior closely matches that of a `ByteBuffer` allocated with the `allocateDirect` factory.

There are cases, however, where automatic deallocation is not enough: consider the case where a large memory segment is mapped from a file (this is possible using `MemorySegment::map`); in this case, an application would probably prefer to release (e.g. `unmap`) the memory associated with this segment in a *deterministic* fashion, to ensure that memory doesn't remain available for longer than in needs to.

An `Arena` provides a scope - the arena scope - which features a bounded and deterministic lifetime. The arena scope is alive from the time when the arena is opened, until the time when the arena is closed. Multiple segments allocated with the same arena scope  enjoy the same bounded lifetime and can safely contain mutual references. For example, this code opens an arena and uses the arena's scope to specify the lifetime of two segments:

```
try (Arena arena = Arena.openConfined()) {
    MemorySegment segment1 = MemorySegment.allocateNative(100, arena.scope());
    MemorySegment segment2 = MemorySegment.allocateNative(100, arena.scope());
    ...
    MemorySegment segmentN = MemorySegment.allocateNative(100, arena.scope());
} // all segments are deallocated here
```

When the arena is closed (above, this is done with the *try-with-resources* construct) the arena scope is no longer alive, all the segments associated with it are invalidated,  and the regions of memory backing the segments are deallocated  atomically.

### Thread-confinement

Arenas provide a strong temporal safety guarantee: memory segments associated with an arena scope cannot be accessed after the arena is closed, since the arena scope is no longer alive. If an arena is opened and closed by the same thread, and all memory segments allocated with the arena's scope are accessed only  by that thread, then ensuring correctness is straightforward. However, if memory segments allocated with the arena scope are accessed by  multiple threads then ensuring correctness is complex. For example, a segment allocated with arena scope might be accessed by one thread while another thread attempts to close the arena. To guarantee temporal safety without making single-threaded clients pay undue cost, there are  two kinds of arenas: *confined* and *shared*.

- A confined arena (`Arena::openConfined`) supports strong thread-confinement guarantees. A confined arena has an *owner thread*, typically the thread which opened it. The memory segments allocated in a confined arena (i.e., with the confined arena's scope) can be accessed  only by the owner thread. Any attempt to close the confined arena from a thread other than the owner thread will fail with an exception.
- A shared arena (`Arena::openShared`) has no owner thread.  The memory segments allocated in a shared arena can be accessed by  multiple threads. Moreover, a shared arena can be closed by any thread,  and the closure is guaranteed to be safe and atomic even under races<a href="#1"><sup>1</sup></a>.

In summary, a segment scope controls which threads can access a  memory segment, and when. A memory segment with global scope or  automatic scope can be accessed by any thread. Conversely, arena scopes  restrict access to specific threads in order to provide both strong  temporal safety and a predictable performance model.

### Slicing segments

Memory segments support *slicing* — that is, given a segment, it is possible to create a new segment whose spatial bounds are stricter than that of the original segment:

```java
MemorySegment segment = MemorySement.allocateNative(10, SegmentScope.auto());
MemorySegment slice = segment.asSlice(4, 4);
```

The above code creates a slice that starts at offset 4 and has a length of 4 bytes. Slices have the *same* temporal bounds (i.e. segment scope) as the parent segment. In the above example, the memory associated with the parent segment will not be released as long as there is at least one *reachable* slice derived from that segment.

To process the contents of a memory segment in bulk, a memory segment can be turned into a stream of slices, using the `MemorySegment::stream` method:

```java
SequenceLayout seq = MemoryLayout.sequenceLayout(1_000_000, JAVA_INT);
SequenceLayout bulk_element = MemoryLayout.sequenceLayout(100, JAVA_INT);

try (Arena arena = Arena.openShared()) {
    MemorySegment segment = MemorySegment.allocateNative(seq, arena.scope());
    int sum = segment.elements(bulk_element).parallel()
                       .mapToInt(slice -> {
                           int res = 0;
                           for (int i = 0; i < 100 ; i++) {
                               res += slice.getAtIndex(JAVA_INT, i);
                           }
                           return res;
                       }).sum();
}
```

The `MemorySegment::elements` method takes an element layout and returns a new stream. The stream is built on top of a spliterator instance (see `MemorySegment::spliterator`) which splits the segment into chunks whose size match that of the provided layout. Here, we want to sum elements in an array which contains a million of elements; now, doing a parallel sum where each computation processes *exactly* one element would be inefficient, so instead we use a *bulk* element layout. The bulk element layout is a sequence layout containing a group of 100 elements — which should make it more amenable to parallel processing. Since we are using `Stream::parallel` to work on disjoint slices in parallel, here we use a *shared* arena, to ensure that the resulting segment can be accessed by multiple threads.

### Accessing segments

Memory segments can be dereferenced easily, by using *value layouts* (layouts are covered in greater details in the next section). A value layout captures information such as:

- The number of bytes to be dereferenced;
- The alignment constraints of the address at which dereference occurs;
- The endianness with which bytes are stored in said memory region;
- The Java type to be used in the dereference operation (e.g. `int` vs `float`).

For instance, the layout constant `ValueLayout.JAVA_INT` is four bytes wide, has no alignment constraints, uses the native platform endianness (e.g. little-endian on Linux/x64) and is associated with the Java type `int`. The following example reads pairs of 32-bit values (as Java ints) and uses them to construct an array of points:

```java
record Point(int x, int y);
MemorySegment segment = MemorySement.allocateNative(10 * 4 * 2, SegmentScope.auto());
Point[] values = new Point[10];
for (int i = 0 ; i < values.length ; i++) {
    int x = segment.getAtIndex(JAVA_INT, i * 2);
    int y = segment.getAtIndex(JAVA_INT, (i * 2) + 1);
    values[i] = new Point(x, y);
}
```

The above snippet allocates a flat array of 80 bytes using `MemorySegment::allocateNative`. Then, inside the loop, elements in the array are accessed using the `MemorySegment::getAtIndex` method, which accesses `int` elements in a segment at a certain *logical* index (under the hood, the segment offset being accessed is obtained by multiplying the logical index by 4, which is the stride of a Java `int` array). Thus, all coordinates `x` and `y` are collected into instances of a `Point` record.

### Structured access

Expressing byte offsets (as in the example above) can lead to code that is hard to read, and very fragile — as memory layout invariants are captured, implicitly, in the constants used to scale offsets. To address this issue, clients can use a `MemoryLayout` to to describe the contents of a memory segment *programmatically*. For instance, the layout of the array used in the above example can be expressed using the following code <a href="#2"><sup>2</sup></a>:

```java
MemoryLayout points = MemoryLayout.sequenceLayout(10,
    MemoryLayout.structLayout(
        JAVA_INT.withName("x"),
        JAVA_INT.withName("y")
    )
);            
```

That is, our layout is a repetition of 10 *struct* elements, each struct element containing two 32-bit values each. Once defined, a memory layout can be queried — for instance we can compute the offset of the `y` coordinate in the 4th element of the `points` array:

```java
long y3 = points.byteOffset(PathElement.sequenceElement(3), PathElement.groupElement("y")); // 28
```

To specify which nested layout element should be used for the offset calculation we use a *layout path*, a selection expression that navigates the layout, from the *root* layout, down to the leaf layout we wish to select; in this case we need to select the 4th layout element in the sequence, and then select the layout named `y` inside the selected group layout.

One of the things that can be derived from a layout is a *memory access var handle*. A memory access var handle is a special kind of var handle which takes a memory segment access coordinate, together with a byte offset — the offset, relative to the segment's base address at which the dereference operation should occur. With memory access var handles we can rewrite our example above as follows:

```java
MemorySegment segment = MemorySegment.allocateNative(points, SegmentScope.auto());
VarHandle xHandle = points.varHandle(PathElement.sequenceElement(), PathElement.groupElement("x"));
VarHandle yHandle = points.varHandle(PathElement.sequenceElement(), PathElement.groupElement("y"));
Point[] values = new Point[10];
for (int i = 0 ; i < values.length ; i++) {
    int x = (int)xHandle.get(segment, (long)i);
    int y = (int)yHandle.get(segment, (long)i);
}
```

In the above, `xHandle` and `yHandle` are two var handle instances whose type is `int` and which takes two access coordinates:

1. a `MemorySegment` instance; the segment whose memory should be dereferenced
2. a *logical* index, which is used to select the element of the sequence we want to access (as the layout path used to construct these var handles contains one free dimension)

Note that memory access var handles (as any other var handle) are *strongly* typed; and to get maximum efficiency, it is generally necessary to introduce casts to make sure that the access coordinates match the expected types — in this case we have to cast `i` into a `long`; similarly, since the signature polymorphic method `VarHandle::get` notionally returns `Object` a cast is necessary to force the right return type the var handle operation <a href="#3"><sup>3</sup></a>.

In other words, manual offset computation is no longer needed — offsets and strides can in fact be derived from the layout object; note how `yHandle` is able to compute the required offset of the `y` coordinate in the flat array without the need of any error-prone arithmetic computation.

### Combining memory access handles

We have seen in the previous sections how memory access var handles dramatically simplify user code when structured access is involved. While deriving memory access var handles from layout is the most convenient option, the FFM API also allows to create such memory access var handles in a standalone fashion, as demonstrated in the following code:

```java
VarHandle intHandle = MethodHandles.memorySegmentViewVarHandle(JAVA_INT); // (MS, J) -> I
```

The above code creates a memory access var handle which reads/writes `int` values at a certain byte offset in a segment. To create this var handle we have to specify a carrier type — the type we want to use e.g. to extract values from memory, as well as whether any byte swapping should be applied when contents are read from or stored to memory. Additionally, the user might want to impose additional constraints on how memory dereferences should occur; for instance, a client might want to prevent access to misaligned 32 bit values. Of course, all this information can be succinctly derived from the provided value layout (`JAVA_INT` in the above example).

The attentive reader might have noted how rich the var handles obtains from memory layouts are, compared to the simple memory access var handle we have constructed here. How do we go from a simple access var handle that takes a byte offset to a var handle that can dereference a complex layout path? The answer is, by using var handle *combinators*. Developers familiar with the method handles know how simpler method handles can be combined into more complex ones using the various combinator methods in the `MethodHandles` class. These methods allow, for instance, to insert (or bind) arguments into a target method handle, filter return values, permute arguments and much more.

The FFM API adds a rich set of var handle combinators in the `MethodHandles` class; with these tools, developers can express var handle transformations such as:

* mapping a var handle carrier type into a different one, using an embedding/projection method handle pairs
* filter one or more var handle access coordinates using unary filters
* permute var handle access coordinates
* bind concrete access coordinates to an existing var handle

Without diving too deep, let's consider how we might want to take a basic memory access handle and turn it into a var handle which dereference a segment at a specific offset (again using the `points` layout defined previously):

```java
VarHandle intHandle = MemoryHandles.memorySegmentViewVarHandle(JAVA_INT); // (MS, J) -> I
long offsetOfY = points.byteOffset(PathElement.sequenceElement(3), PathElement.groupElement("y"));
VarHandle valueHandle = MethodHandles.insertCoordinates(intHandle, 1, offsetOfValue); // (MS) -> I
```

We have been able to derive, from a basic memory access var handle, a new var handle that dereferences a segment at a given fixed offset. It is easy to see how other, richer, var handles obtained using a memory layout can also be constructed manually using the var handle combinators provided by the FFM API.

### Unsafe segments

The FFM API provides basic safety guarantees for all memory segments created using the API. More specifically, a memory dereference operation should either succeed, or result in a runtime exception — but, crucially, should never result in a VM crash, or, more subtly, in memory corruption occurring *outside* the region of memory associated with a memory segment. This is indeed the case, as all memory segments feature immutable *spatial bounds*, and, as we have seen, are associated with a segment scope which make sure that segments cannot be dereferenced after their backing region of memory have been deallocated.

That said, it is sometimes necessary to create a segment out of an existing memory source, which might be managed by native code. This is the case, for instance, if we want to create a segment out of a memory region managed by a *custom allocator*.

The `ByteBuffer` API allows such a move, through a JNI [method](https://docs.oracle.com/javase/8/docs/technotes/guides/jni/spec/functions.html#NewDirectByteBuffer), namely `NewDirectByteBuffer`. This native method can be used to wrap a long address in a fresh direct byte buffer instance which is then returned to unsuspecting Java code.

Memory segments provide a similar capability — that is, given an address (which might have been obtained through some native calls), it is possible to wrap a segment around it, with given spatial bounds and segment scope, as follows:

```java
try (Arena arena = Arena.openShared()) {
    long addr = ...
    var unsafeSegment = MemorySegment.ofAddress(addr, 10, arena.scope());
    ...
}
```

The above code creates a shared arena and then, inside the *try-with-resources* it creates a *new* unsafe segment from a given address; the size of the segment is 10 bytes, and the unsafe segment is associated with the current shared arena. This means that the unsafe segment cannot be dereferenced after the shared arena has been closed.

Of course, segments created this way are completely *unsafe*. There is no way for the runtime to verify that the provided address indeed points to a valid memory location, or that the size of the memory region pointed to by `addr` is indeed 10 bytes. Similarly, there are no guarantees that the underlying memory region associated with `addr` will not be deallocated *prior* to the call to `Arena::close`.

For these reasons, `MemorySegment::ofAddress` is a *restricted method* in the FFM API. The first time a restricted method is invoked, a runtime warning is generated. Developers can get rid of warnings by specifying the set of modules that are allowed to call restricted methods. This is done by specifying the option `--enable-native-access=M`, where `M` is a module name. Multiple module names can be specified in a comma-separated list, where the special name `ALL-UNNAMED` is used to enable restricted access for all code on the class path. If the `--enable-native-access` option is specified, any attempt to call restricted operations from a module not listed in the option will fail with a runtime exception.

* <a id="1"/>(<sup>1</sup>):<small> Shared arenas rely on VM thread-local handshakes (JEP [312](https://openjdk.java.net/jeps/312)) to implement lock-free, safe, shared memory access; that is, when it comes to memory access, there should no difference in performance between a shared segment and a confined segment. On the other hand, `Arena::close` might be slower on shared arenas than on confined ones.</small>
* <a id="2"/>(<sup>2</sup>):<small> In general, deriving a complete layout from a C `struct` declaration is no trivial matter, and it's one of those areas where tooling can help greatly.</small>
* <a id="3"/>(<sup>3</sup>):<small> Clients can enforce stricter type checking when interacting with `VarHandle` instances, by obtaining an *exact* var handle, using the `VarHandle::withInvokeExactBehavior` method.</small>

