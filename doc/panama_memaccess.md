## State of foreign memory support

**May 2022**

**Maurizio Cimadamore**

A crucial part of any native interop story lies in the ability of accessing off-heap memory efficiently and safely. Panama achieves this goal through the Foreign Memory Access API, which has been available as an [incubating](https://openjdk.java.net/jeps/11) API since Java [14](https://openjdk.java.net/jeps/370). The Foreign Memory Access API introduces abstractions to allocate and access flat memory regions (whether on- or off-heap), to manage the lifecycle of memory resources and to model native memory addresses.

### Segments

Memory segments are abstractions which can be used to model contiguous memory regions, located either on- or off- the Java heap. Segments can be allocated from native memory (e.g. like a `malloc`), or can be wrapped around existing memory sources (e.g. a Java array or a `ByteBuffer`). Memory segments provide *strong* spatial, temporal and thread-confinement guarantees which make memory dereference operation *safe* (more on that later), although in most simple cases some properties of memory segments can safely be ignored.

For instance, the following snippet allocates 100 bytes off-heap:

```java
MemorySegment segment = MemorySegment.allocateNative(100, MemorySession.openImplicit());
```

The above code allocates a 100-bytes long memory segment. The lifecycle of a memory segment is controlled by an abstraction called `MemorySession`. In this example, the segment memory will not be *freed* as long as the segment instance is deemed *reachable*, as specified by the `openImplicit()` parameter. In other words, the above factory creates a segment whose behavior closely matches that of a `ByteBuffer` allocated with the `allocateDirect` factory. Of course, the memory access API also supports deterministic memory release; we will cover that in a later section of this document.

Memory segments support *slicing* — that is, given a segment, it is possible to create a new segment whose spatial bounds are stricter than that of the original segment:

```java
MemorySegment segment = MemorySement.allocateNative(10, MemorySession.openImplicit());
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
MemorySegment segment = MemorySement.allocateNative(10 * 4 * 2, MemorySession.openImplicit());
Point[] values = new Point[10];
for (int i = 0 ; i < values.length ; i++) {
    int x = segment.getAtIndex(JAVA_INT, i * 2);
    int y = segment.getAtIndex(JAVA_INT, (i * 2) + 1);
    values[i] = new Point(x, y);
}
```

The above snippet allocates a flat array of 80 bytes using `MemorySegment::allocateNative`. Then, inside the loop, elements in the array are accessed using the `MemorySegment::getAtIndex` method, which accesses `int` elements in a segment at a certain *logical* index (under the hood, the segment offset being accessed is obtained by multiplying the logical index by 4, which is the stride of a Java `int` array). Thus, all coordinates `x` and `y` are collected into instances of a `Point` record.

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
        JAVA_INT.withName("x"),
        JAVA_INT.withName("y")
    )
);            
```

That is, our layout is a repetition of 10 *struct* elements, each struct element containing two 32-bit values each. The advantage of defining a memory layout upfront, using an API, is that we can then query the layout — for instance we can compute the offset of the `y` coordinate in the 4th element of the `points` array:

```java
long y3 = points.byteOffset(PathElement.sequenceElement(3), PathElement.groupElement("y")); // 28
```

To specify which nested layout element should be used for the offset calculation we use a *layout path*, a selection expression that navigates the layout, from the *root* layout, down to the leaf layout we wish to select; in this case we need to select the 4th layout element in the sequence, and then select the layout named `y` inside the selected group layout.

One of the things that can be derived from a layout is a *memory access var handle*. A memory access var handle is a special kind of var handle which takes a memory segment access coordinate, together with a byte offset — the offset, relative to the segment's base address at which the dereference operation should occur. With memory access var handles we can rewrite our example above as follows:

```java
MemorySegment segment = MemorySegment.allocateNative(points, MemorySession.openImplicit());
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

Memory segments support deterministic deallocation, through an abstraction called `MemorySession`. A memory session models the lifecycle associated with one or more memory resources (in this document, by memory resources we mean mostly memory segments); a memory session has a state: it starts off in the *alive* state, which means that all the resources it manages can be safely accessed — and, at the user's request, it can be *closed*. After a memory session is closed, access to resources managed by that session is no longer allowed. Memory sessions implement the `AutoCloseable` interface, and can therefore be used with the *try-with-resources* construct, as demonstrated in the following code:

```java
try (MemorySession session = MemorySession.openConfined()) {
    MemorySegment mapped = MemorySegment.map(Path.of("someFile"), 0, 100000, MapMode.READ_WRITE, session);    
} // segment is unmapped here
```

Here, we create a new *confined* memory session, which is then used when creating a mapped segment; this means that the lifecycle of the `mapped` segment is tied to that of the memory session, and that accessing the segment (e.g. dereference) *after* `session` has been closed will not be possible.

As this example alludes to, memory sessions can come in many flavors: they can be *confined* (where access is restricted to the thread which created the session), *shared* <a href="#3"><sup>3</sup></a> (where access can occur in any thread) and can be optionally associated with a `Cleaner` object (as in the case of `openImplicit`), which performs *implicit* deallocation when the memory session becomes *unreachable* (if the `close` method has not been called by the user). Memory sessions are very handy when managing the lifecycle of multiple resources:

```java
try (MemorySession session = MemorySession.openConfined()) {
    MemorySegment segment1 = MemorySegment.allocateNative(100, session);
    MemorySegment segment2 = MemorySegment.allocateNative(100, session);
    ...
    MemorySegment segmentN = MemorySegment.allocateNative(100, session);
} // all segments are deallocated here
```

Here we create another confined session, and then, inside the *try-with-resources* we use the session to create many segments; all such segments share the *same* memory session — meaning that when such session is closed, the memory associated with all these segments will be reclaimed at once.

#### Challenges

Working with deterministic deallocation is great in terms of achieving better control as to *when* memory resources are released. But deterministic deallocation present some unique challenges which we discuss here; some of these issues might look surprising, especially coming from a world where deallocation happens implicitly (as in the ByteBuffer API). Consider the following method:

```java
void m() {
    MemorySegment segment = MemorySegment.allocateNative(MemorySession.openConfined());
    segment.set(JAVA_INT, 0, 42);
}
```

This method creates a segment backed by a fresh confined memory session. But the session is not closed before the method returns. This means the off-heap memory associated with the native segment will never be released. In other words, we have created a *memory leak*. With power comes responsibility: clients must not forget to call the close method (unless they are working with a session backed by a `Cleaner` object, in which case the call will happen implicitly, of course).

Another issue with deterministic deallocation is that it can sometimes be tricky to determine whether a certain access operation might fail or not. Consider the following method:

```java
void accept(MemorySegment segment) {
   segment.setAtIndex(JAVA_INT, 0, 1);
   segment.setAtIndex(JAVA_INT, 1, 2);
}
```

The first call to `setAtIndex` might fail, if the session associated to the segment has already been closed. But, if the segment is associated with a shared session, it might also be possible for the *second* call to fail (if some other thread has closed the session concurrently). To help clients running a sequence of operation against one or more segments in a more atomic fashion, the `MemorySession::whileAlive` method can be used:

```java
void accept(MemorySegment segment) {
   segment.session().whileAlive(() -> {
       segment.setAtIndex(JAVA_INT, 0, 1);
       segment.setAtIndex(JAVA_INT, 1, 2);
   });
}
```

Finally, when writing APIs returning memory segments, API authors might want to take extra caution so that the API private memory session is not leaked outside the API, through the memory segments generated by the API. Consider the following code:

```java
class Allocator {
    private final MemorySession privateSession = MemorySession.openConfined();
    
    MemorySegment allocate(long byteSize) {
        return MemorySegment.allocateNative(byteSize, privateSession);
    }
}
```

And now, consider the following client code:

```java
Allocator allocator = new Allocator();
MemorySegment segment = allocator.allocate(100);
...
segment.session().close();
```

The problem here is that the API is exposing its own memory session via the segment it returns; by doing so, clients can then access the session of the segments obtained from the API, and even *close* the session, thus releasing *all* the memory that has been allocated by the `Allocator` instance, even the memory associated with segments that the client knew nothing about. To help writing more robust APIs, the `MemorySession::asNonCloseable` method can be used, which obtain a *non-closeable* view of a given memory session:

```java
class Allocator {
    private final MemorySession privateSession = MemorySession.openConfined();
    
    MemorySegment allocate(long byteSize) {
        return MemorySegment.allocateNative(byteSize, privateSession.asNonCloseable());
    }
}
```

In the above example, we have tweaked the `Allocator::allocate` method so that it returns a segment associated with a non-closeable view of the private memory session. This means that clients of this method will no longer be able to call `MemorySession::close` on the returned segment.

As we have seen in this session, deterministic deallocation, as most things in computer science, is a trade-off. More specifically, we are trading between predictability of deallocation and simplicity of API and user code. It is ultimately up to developers to pick the solution that works best for their use case. As a general (and rough!) rule of thumb, long-lived, shared memory resources might be better modelled using implicit memory sessions, whereas short-lived, thread-confined memory resources are often modelled using closeable memory sessions.

### Streaming slices

To process the contents of a memory segment in bulk, a memory segment can be turned into a stream of slices, using the `MemorySegment::stream` method:

```java
SequenceLayout seq = MemoryLayout.sequenceLayout(1_000_000, JAVA_INT);
SequenceLayout bulk_element = MemoryLayout.sequenceLayout(100, JAVA_INT);

try (MemorySession session = MemorySession.openShared()) {
    MemorySegment segment = MemorySegment.allocateNative(seq, session);
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

The `MemorySegment::elements` method takes an element layout and returns a new stream. The stream is built on top of a spliterator instance (see `MemorySegment::spliterator`) which splits the segment into chunks whose size match that of the provided layout. Here, we want to sum elements in an array which contains a million of elements; now, doing a parallel sum where each computation processes *exactly* one element would be inefficient, so instead we use a *bulk* element layout. The bulk element layout is a sequence layout containing a group of 100 elements — which should make it more amenable to parallel processing. Since we are using `Stream::parallel` to work on disjoint slices in parallel, here we use a *shared* memory session, to ensure that the resulting segment can be accessed by multiple threads.

### Combining memory access handles

We have seen in the previous sections how memory access var handles dramatically simplify user code when structured access is involved. While deriving memory access var handles from layout is the most convenient option, the Foreign Memory Access API also allows to create such memory access var handles in a standalone fashion, as demonstrated in the following code:

```java
VarHandle intHandle = MethodHandles.memorySegmentViewVarHandle(JAVA_INT); // (MS, J) -> I
```

The above code creates a memory access var handle which reads/writes `int` values at a certain byte offset in a segment. To create this var handle we have to specify a carrier type — the type we want to use e.g. to extract values from memory, as well as whether any byte swapping should be applied when contents are read from or stored to memory. Additionally, the user might want to impose additional constraints on how memory dereferences should occur; for instance, a client might want to prevent access to misaligned 32 bit values. Of course, all this information can be succinctly derived from the provided value layout (`JAVA_INT` in the above example).

The attentive reader might have noted how rich the var handles returned by the layout API are, compared to the simple memory access var handle we have constructed here. How do we go from a simple access var handle that takes a byte offset to a var handle that can dereference a complex layout path? The answer is, by using var handle *combinators*. Developers familiar with the method handle API know how simpler method handles can be combined into more complex ones using the various combinator methods in the `MethodHandles` API. These methods allow, for instance, to insert (or bind) arguments into a target method handle, filter return values, permute arguments and much more.

The Foreign Memory Access API adds a rich set of var handle combinators in the `MethodHandles` class; with these tools, developers can express var handle transformations such as:

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

We have been able to derive, from a basic memory access var handle, a new var handle that dereferences a segment at a given fixed offset. It is easy to see how other, richer, var handles obtained using the layout API can be constructed manually using the var handle combinator API.

### Unsafe segments

The memory access API provides basic safety guarantees for all memory segments created using the API. More specifically, a memory dereference operation should either succeed, or result in a runtime exception — but, crucially, should never result in a VM crash, or, more subtly, in memory corruption occurring *outside* the region of memory associated with a memory segment. This is indeed the case, as all memory segments feature immutable *spatial bounds*, and, as we have seen, are associated with a memory session which make sure that segments cannot be dereferenced after their session has been closed, or, in case of a confined session, that segments cannot be dereferenced from a thread other than the one which created the session.

That said, it is sometimes necessary to create a segment out of an existing memory source, which might be managed by native code. This is the case, for instance, if we want to create a segment out of a memory region managed by a *custom allocator*.

The ByteBuffer API allows such a move, through a JNI [method](https://docs.oracle.com/javase/8/docs/technotes/guides/jni/spec/functions.html#NewDirectByteBuffer), namely `NewDirectByteBuffer`. This native method can be used to wrap a long address in a fresh direct byte buffer instance which is then returned to unsuspecting Java code.

Memory segments provide a similar capability — that is, given an address (which might have been obtained through some native calls), it is possible to wrap a segment around it, with given spatial bounds and memory session, as follows:

```java
try (MemorySession session = MemorySession.openShared()) {
    MemoryAddress addr = MemoryAddress.ofLong(someLongAddr);
    var unsafeSegment = MemorySegment.ofAddress(addr, 10, session);
    ...
}
```

The above code creates a shared session and then, inside the *try-with-resources* it creates a *new* unsafe segment from a given address; the size of the segment is 10 bytes, and the unsafe segment is associated with the current shared session. This means that the unsafe segment cannot be dereferenced after the shared session has been closed.

Of course, segments created this way are completely *unsafe*. There is no way for the runtime to verify that the provided address indeed points to a valid memory location, or that the size of the memory region pointed to by `addr` is indeed 10 bytes. Similarly, there are no guarantees that the underlying memory region associated with `addr` will not be deallocated *prior* to the call to `MemorySession::close`.

For these reasons, `MemorySegment::ofAddress` is a *restricted method* in the Foreign Memory Access API. The first time a restricted method is invoked, a runtime warning is generated. Developers can get rid of warnings by specifying the set of modules that are allowed to call restricted methods. This is done by specifying the option `--enable-native-access=M`, where `M` is a module name. Multiple module names can be specified in a comma-separated list, where the special name `ALL-UNNAMED` is used to enable restricted access for all code on the class path. If the `--enable-native-access` option is specified, any attempt to call restricted operations from a module not listed in the option will fail with a runtime exception.

* <a id="1"/>(<sup>1</sup>):<small> In general, deriving a complete layout from a C `struct` declaration is no trivial matter, and it's one of those areas where tooling can help greatly.</small>
* <a id="2"/>(<sup>2</sup>):<small> Clients can enforce stricter type checking when interacting with `VarHandle` instances, by obtaining an *exact* var handle, using the `VarHandle::withInvokeExactBehavior` method.</small>
* <a id="3"/>(<sup>3</sup>):<small> Shared sessions rely on VM thread-local handshakes (JEP [312](https://openjdk.java.net/jeps/312)) to implement lock-free, safe, shared memory access; that is, when it comes to memory access, there should no difference in performance between a shared segment and a confined segment. On the other hand, `MemorySession::close` might be slower on shared sessions than on confined ones.</small>

