## State of foreign memory support

**September 2021**

**Maurizio Cimadamore**

A crucial part of any native interop story lies in the ability of accessing off-heap memory efficiently and safely. Panama achieves this goal through the Foreign Memory Access API, which has been available as an [incubating](https://openjdk.java.net/jeps/11) API since Java [14](https://openjdk.java.net/jeps/370). The Foreign Memory Access API introduces abstractions to allocate and access flat memory regions (whether on- or off-heap), to manage the lifecycle of memory resources and to model native memory addresses.

### Segments

Memory segments are abstractions which can be used to model contiguous memory regions, located either on- or off- the Java heap. Segments can be allocated from native memory (e.g. like a `malloc`), or can be wrapped around existing memory sources (e.g. a Java array or a `ByteBuffer`). Memory segments provide *strong* spatial, temporal and thread-confinement guarantees which make memory dereference operation *safe* (more on that later), although in most simple cases some properties of memory segments can safely be ignored.

For instance, the following snippet allocates 100 bytes off-heap:

```java
MemorySegment segment = MemorySegment.allocateNative(100, ResourceScope.newConfinedScope());
```

The above code allocates a 100-bytes long memory segment. The lifecycle of a memory segment is controlled by an abstraction called `ResourceScope`, which can be used to deallocate the memory associated with the memory segment (we will cover that in a later section of this document). Resource scopes feature (by default) an *implicit deallocation* mechanism, which allow memory segments such as the one above to be used pretty much in the same way as a `ByteBuffer` allocated with the `allocateDirect` factory. That is, the memory associated with the segment is deallocated when the resource scope, and hence, the segment, becomes unreacheable.

Memory segments support *slicing* — that is, given a segment, it is possible to create a new segment whose spatial bounds are stricter than that of the original segment:

```java
MemorySegment segment = MemorySement.allocateNative(10, ResourceScope.newConfinedScope());
MemorySegment slice = segment.asSlice(4, 4);
```

The above code creates a slice that starts at offset 4 and has a length of 4 bytes. Generally speaking, slices have the *same* temporal bounds as the parent segment (we will refine this concept later in this document). In this example, the memory associated with the parent segment will not be released as long as there is at least one *reachable* slice derived from that segment.

Memory segments can be dereferenced easily, by using *value layouts* (layouts are covered in greater details in the next section). A value layout captures information such as:

- The number of bytes to be dereferenced;
- The alignment constraints of the address at which dereference occurs;
- The endianness with which bytes are stored in said memory region;
- The Java type to be used in the dereference operation (e.g. `int` vs `float`).

For instance, the layout constant `ValueLayout.JAVA_INT` is four bytes wide, has no alignment constraints, uses the native platform endianness (e.g. little-endian on Linux/x64) and is associated with the Java type `int`. The following example reads pairs of 32-bit values (as Java ints) and uses them to construct an array of points:

```java
record Point(int x, int y);
MemorySegment segment = MemorySement.allocateNative(10 * 4 * 2, ResourceScope.newConfinedScope());
Point[] values = new Point[10];
for (int i = 0 ; i < values.length ; i++) {
    int x = segment.getAtIndex(JAVA_INT, i * 2);
    int y = segment.getAtIndex(JAVA_INT, (i * 2) + 1);
    values[i] = new Point(x, y);
}
```

The above snippet allocates a flat array of 80 bytes using `MemorySegment::allocateNative`. Then, inside the loop, elements in the array are accessed using the `MemorySegment::getAtIndex` method, which accesses `int` elements in a segment at a certain *logical* index (in other words, the segment offset being accessed is obtained by multiplying the index by 4, which is the stride of a Java `int` array). Thus, all coordinates `x` and `y` are collected into instances of a `Point` record.

Memory segments are pretty flexible when it comes to interacting with existing memory sources and APIs. For instance, it is possible to create a `ByteBuffer` *view* out of an existing memory segment, as follows:

```java
IntBuffer intBuffer = segment.asByteBuffer().asIntBuffer();
Point[] values = new Point[10];
for (int i = 0 ; i < values.length ; i++) {
    int x = intBuffer.get(i * 2);
    int y = intBuffer.get((i * 2) + 1);
    values[i] = new Point(x, y);
}
```

Creating buffer views out of existing segment is a crucial tool enabling interoperability with existing API (especially those dealing with I/O) which might be expressed in terms of the ByteBuffer API.

### Layouts and structured access

Expressing byte offsets (as in the example above) can lead to code that is hard to read, and very fragile — as memory layout invariants are captured, implicitly, in the constants used to scale offsets. To address this issue, we add a *memory layout* API which allows clients to define memory layouts *programmatically*. For instance, the layout of the array used in the above example can be expressed using the following code <a href="#1"><sup>1</sup></a>:

```java
MemoryLayout points = MemoryLayout.sequenceLayout(10,
    MemoryLayout.structLayout(
        MemoryLayouts.JAVA_INT.withName("x"),
        MemoryLayouts.JAVA_INT.withName("y")
    )
);            
```

That is, our layout is a repetition of 10 *struct* elements, each struct element containing two 32-bit values each. The advantage of defining a memory layout upfront, using an API, is that we can then query the layout — for instance we can compute the offset of the `y` coordinate in the 4th element of the points array:

```java
long y3 = points.byteOffset(PathElement.sequenceElement(3), PathElement.groupElement("y")); // 28
```

To specify which nested layout element should be used for the offset calculation we use a *layout path*, a selection expression that navigates the layout, from the *root* layout, down to the leaf layout we wish to select; in this case we need to select the 4th layout element in the sequence, and then select the layout named `y` inside the selected group layout.

One of the things that can be derived from a layout is a *memory access var handle*. A memory access var handle is a special kind of var handle which takes a memory segment access coordinate, together with a byte offset — the offset, relative to the segment's base address at which the dereference operation should occur. With memory access var handles we can rewrite our example above as follows:

```java
MemorySegment segment = MemorySegment.allocateNative(points, ResourceScope.newConfinedScope());
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

Note that memory access var handles (as any other var handle) are *strongly* typed; and to get maximum efficiency, it is generally necessary to introduce casts to make sure that the access coordinates match the expected types — in this case we have to cast `i` into a `long`; similarly, since the signature polymorphic method `VarHandle::get` notionally returns `Object` a cast is necessary to force the right return type the var handle operation <a href="#2"><sup>2</sup></a>.

In other words, manual offset computation is no longer needed — offsets and strides can in fact be derived from the layout object; note how `yHandle` is able to compute the required offset of the `y` coordinate in the flat array without the need of any error-prone arithmetic computation.

### Deterministic deallocation

In addition to spatial bounds, memory segments also feature temporal bounds as well as thread-confinement. In the examples shown so far, we have always used the API in its simpler form, leaving the runtime to handle details such as whether it was safe or not to reclaim memory associated with a given memory segment. But there are cases where this behavior is not desirable: consider the case where a large memory segment is mapped from a file (this is possible using `MemorySegment::map`); in this case, an application would probably prefer to deterministically release (e.g. unmap) the memory associated with this segment, to ensure that memory doesn't remain available for longer than in needs to (and therefore potentially impacting the performance of the application).

Memory segments support deterministic deallocation, through an abstraction called `ResourceScope`. A resource scope models the lifecycle associated with one or more resources (in this document, by resources we mean mostly memory segments); a resource scope has a state: it starts off in the *alive* state, which means that all the resources it manages can be safely accessed — and, at the user request, it can be *closed*. After a resource scope is closed, access to resources managed by that scope is no longer allowed. Resource scope support the `AutoCloseable` interface, which means that user can use resource scopes with the *try-with-resources* construct, as demonstrated in the following code:

```java
try (ResourceScope scope = ResourceScope.newConfinedScope()) {
    MemorySegment mapped = MemorySegment.map(Path.of("someFile"), 0, 100000, MapMode.READ_WRITE, scope);    
} // segment is unmapped here
```

Here, we create a new *confined* resource scope, which is then used when creating a mapped segment; this means that the lifecycle of the `mapped` segment will be tied to that of the resource scope, and that accessing the segment (e.g. dereference) *after* `scope` has been closed will not be possible.

As this example alludes to, resource scopes can come in two flavors: they can be *confined* (where access is restricted to the thread which created the scope) or *shared* <a href="#3"><sup>3</sup></a> (where access can occur in any thread). By default, all resources scopes are associated with an internal `Cleaner` object, which would take care of performing implicit deallocation (in case `close` is never called). Optionally, clients can provide a custom `Cleaner` object, or decide not to use a `Cleaner` all together. While this latter option provides slightly better scope creation performance, it must be used with caution: any scope that becomes unreachable before its `close` method has been called will end up leaking memory resources.

Resource scopes are very handy when managing the lifecycle of multiple resources:

```java
try (ResourceScope scope = ResourceScope.newConfinedScope()) {
    MemorySegment segment1 = MemorySegment.allocateNative(100, scope);
    MemorySegment segment2 = MemorySegment.allocateNative(100, scope);
    ...
    MemorySegment segmentN = MemorySegment.allocateNative(100, scope);
} // all segments are deallocated here
```

Here we create another confined scope, and then, inside the *try-with-resources* we use the scope to create many segments; all such segments share the *same* resource scope — meaning that when such scope is closed, the memory associated with all these segments will be reclaimed at once.

Dealing with shared access *and* deterministic deallocation at the same time is tricky, and poses new problems for the user code; consider the case where a method receives a segment and has to write two values in that segment (e.g. two point coordinates):

```java
void writePoint(MemorySegment segment, int x, int y) {
    segment.setAtIndex(JAVA_INT, 0, x);
    segment.setAtIndex(JAVA_INT, 1, y);
}
```

If the segment is associated with a confined scope, no problem arises: the thread that created the segment is the same thread that performs the dereference operation — as such, when `writePoint` is called, the segment's scope is either alive (and will remain so for the duration of the call), or already closed (in which case some exception will be thrown, and no value will be written).

But, if the segment is associated with a shared scope, there is a new problem we are faced with: the segment might be closed (concurrently) in between the two accesses! This means that, the method ends up writing only one value instead of two; in other words, the behavior of the method is no longer atomic.

To avoid this problem, clients can temporarily prevent a scope from being closed, by creating a temporal dependency between that scope and another scope under their control. Let's illustrate how that works in practice:

```java
void writePointSafe(MemorySegment segment, int x, int y) {
    try (ResourceScope scope = ResourceScope.newConfinedScope()) {
		scope.keepAlive(segment.scope());
        MemoryAccess.setIntAtIndex(segment, 0, x);
        MemoryAccess.setIntAtIndex(segment, 1, y);
    }
}
```

Here, the client creates a *fresh* confined scope, and then sets up a dependency between this new scope and the segment's scope, using `ResourceScope::keepAlive`. This means that the segment cannot be released until the local scope is closed. The attentive user might have noticed that this idiom acts as a more restricted version <a href="#4"><sup>4</sup></a> of an *atomic reference count*; each time a target scope is kept alive by a new local scope, its *acquired count* goes up; conversely the count goes down each time a local scope associated with the target scope is released. A target scope can only be closed if its acquired count is exactly zero. In our example above, the semantics of resource scope handles guarantees that the method will be able to either set up the temporal dependency successfully, and write both values, or fail, and write no value.

### Parallel processing

The contents of a memory segment can be processed in *parallel* (e.g. using a framework such as Fork/Join) — by obtaining a `Spliterator` instance out of a memory segment. For instance to sum all the 32 bit values of a memory segment in parallel, we can use the following code:

```java
SequenceLayout seq = MemoryLayout.sequenceLayout(1_000_000, MemoryLayouts.JAVA_INT);
SequenceLayout bulk_element = MemoryLayout.sequenceLayout(100, MemoryLayouts.JAVA_INT);

try (ResourceScope scope = ResourceScope.newSharedScope()) {
    MemorySegment segment = MemorySegment.allocateNative(seq, scope);
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

The `MemorySegment::elements` method takes an element layout and returns a new stream. The stream is built on top of a spliterator instance (see `MemorySegment::spliterator`) which splits the segment into chunks which corresponds to the elements in the provided layout. Here, we want to sum elements in an array which contains a million of elements; now, doing a parallel sum where each computation processes *exactly* one element would be inefficient, so instead we use a *bulk* element layout. The bulk element layout is a sequence layout containing a group of 100 elements — which should make it more amenable to parallel processing.

Since the segment operated upon by the spliterator is associated with a shared scope, the segment can be accessed from multiple threads concurrently; the spliterator API ensures that the access occurs in a disjoint fashion: a slice is created from the original segment, and given to a thread to perform some computation — thus ensuring that no two threads can ever operate concurrently on the same memory region.

### Combining memory access handles

We have seen in the previous sections how memory access var handles dramatically simplify user code when structured access is involved. While deriving memory access var handles from layout is the most convenient option, the Foreign Memory Access API also allows to create such memory access var handles in a standalone fashion, as demonstrated in the following code:

```java
VarHandle intHandle = MemoryHandles.varHandle(JAVA_INT); // (MS, J) -> I
```

The above code creates a memory access var handle which reads/writes `int` values at a certain byte offset in a segment. To create this var handle we have to specify a carrier type — the type we want to use e.g. to extract values from memory, as well as whether any byte swapping should be applied when contents are read from or stored to memory. Additionally, the user might want to impose additional constraints on how memory dereferences should occur; for instance, a client might want to prevent access to misaligned 32 bit values. Of course, all this information can be succinctly derived from the provided value layout (`JAVA_INT` in the above example).

The attentive reader might have noted how rich the var handles returned by the layout API are, compared to the simple memory access var handle we have constructed above. How do we go from a simple access var handle that takes a byte offset to a var handle that can dereference a complex layout path? The answer is, by using var handle *combinators*. Developers familiar with the method handle API know how simpler method handles can be combined into more complex ones using the various combinator methods in the `MethodHandles` API. These methods allow, for instance, to insert (or bind) arguments into a target method handle, filter return values, permute arguments and much more.

Sadly, none of these features are available when working with var handles. The Foreign Memory Access API rectifies this, by adding a rich set of var handle combinators in the `MemoryHandles` class; with these tools, developers can express var handle transformations such as:

* mapping a var handle carrier type into a different one, using an embedding/projection method handle pairs
* filter one or more var handle access coordinates using unary filters
* permute var handle access coordinates
* bind concrete access coordinates to an existing var handle

Without diving too deep, let's consider how we might want to take a basic memory access handle and turn it into a var handle which dereference a segment at a specific offset (again using the `points` layout defined previously):

```java
VarHandle intHandle = MemoryHandles.varHandle(JAVA_INT); // (MS, J) -> I
long offsetOfY = points.byteOffset(PathElement.sequenceElement(3), PathElement.groupElement("y"));
VarHandle valueHandle = MemoryHandles.insertCoordinates(intHandle, 1, offsetOfValue); // (MS) -> I
```

We have been able to derive, from a basic memory access var handle, a new var handle that dereferences a segment at a given fixed offset. It is easy to see how other, richer, var handles obtained using the layout API can be constructed manually using the var handle combinator API.

### Unsafe segments

The memory access API provides basic safety guarantees for all memory segments created using the API. More specifically, dereferencing memory should either succeed, or result in a runtime exception — but, crucially, should never result in a VM crash, or, more subtly, in memory corruption occurring *outside* the region of memory associated with a memory segment. This is possible, since all segments have immutable *spatial bounds*, and, as we have seen, are associated with a resource scope which make sure that the segment cannot be dereferenced after the scope has been closed, or, in case of a confined scope, that the segment is dereferenced from the very same thread which created the scope.

That said, it is sometimes necessary to create a segment out of an existing memory source, which might be managed by native code. This is the case, for instance, if we want to create a segment out of memory managed by a custom allocator.

The ByteBuffer API allows such a move, through a JNI [method](https://docs.oracle.com/javase/8/docs/technotes/guides/jni/spec/functions.html#NewDirectByteBuffer), namely `NewDirectByteBuffer`. This native method can be used to wrap a long address in a fresh byte buffer instance which is then returned to unsuspecting Java code.

Memory segments provide a similar capability — that is, given an address (which might have been obtained through some native calls), it is possible to wrap a segment around it, with given spatial bounds and resource scope; a cleanup action to be executed when the segment is closed might also be specified.

For instance, assuming we have an address pointing at some externally managed memory block, we can construct an *unsafe* segment, as follows:

```java
try (ResourceScope scope = ResourceScope.newSharedScope()) {
    MemoryAddress addr = MemoryAddress.ofLong(someLongAddr);
    var unsafeSegment = MemorySegment.ofAddressNative(addr, 10, scope);
    ...
}
```

The above code creates a shared scope and then, inside the *try-with-resources* it creates a *new* unsafe segment from a given address; the size of the segment is 10 bytes, and the unsafe segment is associated with the current shared scope. This means that the unsafe segment cannot be dereferenced after the shared scope has been closed.

Of course, segments created this way are completely *unsafe*. There is no way for the runtime to verify that the provided address indeed points to a valid memory location, or that the size of the memory region pointed to by `addr` is indeed 10 bytes. Similarly, there are no guarantees that the underlying memory region associated with `addr` will not be deallocated *prior* to the call to `ResourceScope::close`.

For these reasons, creating unsafe segments is a *restricted* operation in the Foreign Memory Access API. Restricted operations can only be performed from selected modules. To grant a given module `M` the permission to execute restricted methods, the option `--enable-native-access=M` must be specified on the command line. Multiple module names can be specified in a comma-separated list, where the special name `ALL-UNNAMED` is used to enable restricted access for all code on the class path. Any attempt to call restricted operations from a module not listed in the above flag will fail with a runtime exception.

* <a id="1"/>(<sup>1</sup>):<small> In general, deriving a complete layout from a C `struct` declaration is no trivial matter, and it's one of those areas where tooling can help greatly.</small>
* <a id="2"/>(<sup>2</sup>):<small> Clients can enforce stricter type checking when interacting with `VarHandle` instances, by obtaining an *exact* var handle, using the `VarHandle::withInvokeExactBehavior` method.</small>
* <a id="3"/>(<sup>3</sup>):<small> Shared segments rely on VM thread-local handshakes (JEP [312](https://openjdk.java.net/jeps/312)) to implement lock-free, safe, shared memory access; that is, when it comes to memory access, there should no difference in performance between a shared segment and a confined segment. On the other hand, `MemorySegment::close` might be slower on shared segments than on confined ones.</small>
* <a id="4"/>(<sup>4</sup>):<small> The main difference between reference counting and the mechanism proposed here is that reference counting is *symmetric* — meaning that any client is able to both increment and decrement the reference count at will. The resource scope handle mechanism is *asymmetric*, since only the client acquiring a handle has the capability to release that handle. This avoids situation where a client might be tempted to e.g. decrement the reference count multiple times in order to perform some task which would otherwise be forbidden. </small>

