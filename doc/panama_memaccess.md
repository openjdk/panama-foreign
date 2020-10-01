## State of foreign memory support

**October 2020**

* Reflect latest API changes in handoff/implicit deallocation
* Updated section on restricted segments reflecting new, simpler API
* Revamped section on confinement to add description of shared segments
* Added section on implicit deallocation

**Maurizio Cimadamore**

A crucial part of any native interop story lies in the ability of accessing off-heap memory in an efficient fashion. Panama achieves this goal through the so called Foreign Memory Access API. This API has been made available as an incubating API in Java [14](https://openjdk.java.net/jeps/370) ad [15](https://openjdk.java.net/jeps/383), and is, to date, the most mature part of the Panama interop story.

### Segments

Memory segments are abstractions which can be used to model memory regions, located either on- or off- the Java heap. Segments can be allocated from native memory (e.g. like a `malloc`), or can be wrapped around existing memory sources (e.g. a Java array or a `ByteBuffer`). Memory segments provide *strong* guarantees which make memory dereference operation *safe*. More specifically, each segment provides:

* *spatial bounds* - that is, a segment has a base address and a size, and accessing a segment outside those boundaries is forbidden
* *temporal bounds* - that is, a segment has a _state_ - meaning that it can be used and then _closed_ when the memory backing the segment is no longer needed (note, this might trigger deallocation of said memory)
* *thread-confinement*  - that is, a segment is a view over a memory region that is *owned* by the thread which created it. Attempting to dereference  or close a segment outside the confinement thread is forbidden (this is crucial to avoid access vs. close races in multi-threaded scenario)

For instance, the following snippet allocates 100 bytes off-heap:

```java
try (MemorySegment segment = MemorySegment.allocateNative(100)) {
    ...
} // frees memory
```

Since segments are `AutoCloseable` they can be used inside a *try-with-resources* statement, which helps ensure that memory will be released when the segment is no longer needed.

Memory segments support *slicing* — that is, given a segment, it is possible to create a new segment whose spatial bounds are stricter than that of the original segment:

```java
MemorySegment segment = MemorySement.allocateNative(10);
MemorySegment slice = segment.asSlice(4, 4);
```

The above code creates a slice that starts at offset 4 and has a length of 4 bytes. Slices have the *same* temporal bounds as the parent segment - this means that when the parent segment is closed, all the slices derived from it are also closed. The opposite is also true, closing a slice closes the parent segment (and all the other slices derived from it). If a library wants to share a slice, but prevent a client from closing it (could be useful e.g. when implementing a slab allocator), the library could prevent a client from closing the slice by creating a *non-closeable* view:

```java
MemorySegment sharedSlice = slice.withAccessModes(ALL_ACCESS & ~CLOSE);
```

Any attempt to call `close` on `sharedSlice` will be met with an exception. Memory segments support various access modes (including those for read/write access) which can be used to constrain the set of operations available to clients.

### Memory access handles

Dereferencing memory associated with segments is made possible by using *memory access var handles*. A memory access var handle is a special kind of var handle which takes a memory segment access coordinate, together with a byte offset — the offset, relative to the segment's base address at which the dereference operation should occur. A memory access var handle can be obtained as follows:

```java
VarHandle intHandle = MemoryHandles.varHandle(int.class, ByteOrder.nativeOrder())    
```

To create a dereference handle we have to specify a carrier type — the type we want to use e.g. to extract values from memory, as well as to whether any byte swapping should be applied when contents are read from or stored to memory. Additionally, the user can supply an extra alignment parameter (not shown here) — this can be useful to impose additional constraints on how memory dereferences should occur; for instance, a client might want to prevent access to misaligned 32 bit values.

For instance, to read 10 int values from a segment, we can write the following code:

```java
MemorySegment segment = ...
int[] values = new int[10];
for (int i = 0 ; i < values.length ; i++) {
    values[i] = (int)intHandle.get(segment, (long)i * 4);
}
```

Memory access var handles (as any other var handle) are *strongly* typed; and to get maximum efficiency, it is generally necessary to introduce casts to make sure that the access coordinates match the expected types — in this case we have to cast `i * 4` into a long; similarly, since the signature polymorphic method `VarHandle::get` notionally returns `Object` a cast is necessary to force the right return type the var handle operation.

Note that, since the offset of the dereference operation is expressed in bytes, we have to manually compute the starting offset, by multiplying the logical index `i` by `4` — the size (in bytes) of a Java int value; this is not dissimilar to what happens with the `ByteBuffer` absolute get/put methods. We shall see later how memory layouts help us achieving higher level, structured access.

### Safety

The memory access API provides basic safety guarantees for memory dereference operations. More specifically, dereferencing memory should either succeed, or result in a runtime exception - but, crucially, should never result in a VM crash, or, more subtly, in memory corruption occurring *outside* the region of memory associated with a memory segment.

To enforce this, strong spatial and temporal checks are enforced upon every access. Consider the following code:

```java
MemorySegment segment = MemorySegment.allocateNative(10);
intHandle.get(segment, 8); //throws ISE
```

The above code leads to a runtime exception, as we trying to access memory outside the segment's bounds — the access operation starts at offset 8 (which is within bounds), but ends at offset 11 (which is outside bounds).

Similarly, attempting to access an already closed segment leads to a failure:

```java
segment.close();
intHandle.get(segment, 0); //throws ISE
```

This time, the access occurs within the spatial bounds implied by the segment, but when access occurs, the segment has already been *closed*, so the access operation fails. This is crucial to guarantee safety: since memory segments ensure *deterministic deallocation*, the above code might end up attempting to dereference already freed memory.

On top of basic spatial and temporal bound guarantees, memory segments also enforce thread-confinement guarantees, which will be discussed in a later [section](#Confinement). Note that, while these checks might seem expensive when considered in isolation, the Foreign Memory Access API is designed and implemented such that the JIT compiler can hoist most, if not all, such checks outside hot loops. Hence, memory access efficiency is not negatively impacted by the safety requirements of the API.

### Layouts

Expressing byte offsets (as in the [above](#memory-access-handles) example) can lead to code that is hard to read, and very fragile — as memory layout invariants are captured, implicitly, in the constants used to scale offsets. To address this issue, we add a *memory layout* API which allows clients to define memory layouts *programmatically*. For instance, the layout of the array used in the above example can be expressed using the following code:

```java
MemoryLayout intArray = MemoryLayout.ofSequence(10, MemoryLayout.ofValueBits(32));
```

That is, our layout is a repetition of 10 elements whose size is 32 bit each. The advantage of defining a memory layout upfront, using an API, is that we can then query on the layout — for instance we can compute the offset of the 3rd element of the array:

```java
long element3 = intArray.byteOffset(PathElement.sequenceElement(3)); // 12
```

To specify which nested layout element should be used for the offset calculation we use a so called *layout path* - that is, a selection expression that navigates the layout, from the *root* layout, down to the leaf layout we wish to select (in this case the 3rd layout element in the sequence).

Layouts can also be used to obtain memory access var handles; so we can rewrite the above example as follows:

```java
MemorySegment segment = ...
int[] values = new int[10];
VarHandle elemHandle = intArray.varHandle(int.class, PathElement.sequenceElement());
for (int i = 0 ; i < values.length ; i++) {
    values[i] = (int)elemHandle.get(segment, (long)i);
}
```

In the above, `elemHandle` is a var handle whose type is `int` , which takes two access coordinates:

1. a `MemorySegment` instance; the segment whose memory should be dereferenced
2. a *logical* index, which is used to select the element of the sequence we want to access

In other words, manual offset computation is no longer needed — offsets and strides can in fact be derived from the layout object.

Memory layouts shine when structured access is needed — consider the following C declaration:

```c
typedef struct {
	char kind;
    int value;
} TaggedValues[5];
```

The above C declaration can be modeled using the layout below:

```javascript
SequenceLayout taggedValues = MemoryLayout.ofSequence(5,
    MemoryLayout.ofStruct(
        MemoryLayout.ofValueBits(8, ByteOrder.nativeOrder()).withName("kind"),
        MemoryLayout.ofPaddingBits(24),
        MemoryLayout.ofValueBits(32, ByteOrder.nativeOrder()).withName("value")
    )
).withName("TaggedValues");
```

Here we assume that we need to insert some padding after the `kind` field to honor the alignment requirements of the `value` field <a href="#1"><sup>1</sup></a>. Now, if we had to access the `value` field of all the elements of the array using manual offset computation, the code would quickly become pretty hard to read — on each iteration we would have to remember that the stride of the array is 8 bytes and that the offset of the `value` field relative to the `TaggedValue` struct is 4 bytes. This gives us an access expression like `(i * 8) + 4`, where `i` is the index of the element whose `value` field needs to be accessed.

With memory layouts, we can simply compute, once and for all, the memory access var handle to access the `value` field inside the sequence, as follows:

```java
VarHandle valuesHandle = taggedValues.varHandle(int.class,
                                               PathElement.sequenceElement(),
                                               PathElement.groupElement("value"));
```

When using this var handle, no manual offset computation will be required: the resulting `valuesHandle` will feature an additional `long` coordinate which can be used to select  the desired `value` field from the sequence.

### Var handle combinators

The attentive reader might have noted how rich the var handles returned by the layout API are, compared to the simple memory access var handle we have seen at play [here](#memory-access-handles). How do we go from a simple access var handle that takes a byte offset to a var handle that can dereference a complex layout path? The answer is, by using var handle *combinators*. Developers familiar with the method handle API know how simpler method handles can be combined into more complex ones using the various combinator methods in the `MethodHandles` API. These methods allow, for instance, to insert (or bind) arguments into a target method handle, filter return values, permute arguments and much more.

Sadly, none of these features are available when working with var handles. The Foreign Memory Access API rectifies this, by adding a rich set of var handle combinators in the `MemoryHandles` class; with these tools, developers can express var handle transformations such as:

* mapping a var handle carrier type into a different one, using an embedding/projection method handle pairs
* filter one or more var handle access coordinates using unary filters
* permute var handle access coordinates
* bind concrete access coordinates to an existing var handle

Without diving too deep, let's consider how we might want to take a basic memory access handle and turn it into a var handle which dereference a segment at a specific offset (again using the `taggedValues` layout defined previously):

```java
VarHandle intHandle = MemoryHandles.varHandle(int.class, ByteOrder.nativeOrder()); // (MS, J) -> I
long offsetOfValue = taggedValues.byteOffset(PathElement.sequenceElement(0),
                                             PathElement.groupElement("value"));
VarHandle valueHandle = MemoryHandles.insertCoordinates(intHandle, 0, offsetOfValue); // (MS) -> I
```

We have been able to derive, from a basic memory access var handle, a new var handle that dereferences a segment at a given fixed offset. It is easy to see how other, richer, var handles obtained using the layout API can be constructed manually using the var handle combinator API.

### Segment accessors

Building complex memory access var handles using layout paths and the combinator API is useful, especially for structured access. But in simple cases, creating a `VarHandle` just to be able to read an int value at a given segment offset can be perceived as overkill. For this reason, the foreign memory access API provides ready-made static accessors in the `MemoryAccess` class, which allows to dereference a segment in various ways. For instance, if a client wants to read an int value from a segment, one of the following methods can be used:

* `MemoryAccess::getInt(MemorySegment)` — reads an int value (4 bytes) starting at the segment's base address
* `MemoryAccess::getIntAtOffset(MemorySegment, long)` — reads an int value (4 bytes) starting at the address `A = B + O` where `B` is the segment's base address, and `O` is an offset (in bytes) supplied by the client
* `MemoryAccess::getIntAtIndex(MemorySegment, long)` — reads an int value (4 bytes) starting at the address `A = B + (4 * I)` where `B` is the segment's base address, and `I` is a logical index supplied by the client; this accessor is useful for mimicking array access.

In other words, at least in simple cases, memory dereference operations can be achieved without the need of going through the `VarHandle` API; of course in more complex cases (structured and/or multidimensional access, fenced access) the full power of the `VarHandle` API might still come in handy.

### Interoperability

Memory segments are pretty flexible when it comes to interacting with existing memory sources. For instance it is possible to:

* create segment from a Java array
* convert a segment into a Java array
* create a segment from a byte buffer
* convert a segment into a byte buffer

For instance, thanks to bi-directional integration with the byte buffer API, it is possible for users to create a memory segment, and then de-reference it using the byte buffer API, as follows:

```java
MemorySegment segment = ...
int[] values = new int[10];
ByteBuffer bb = segment.asByteBuffer();
for (int i = 0 ; i < values.length ; i++) {
    values[i] = bb.getInt();
}
```

The only thing to remember is that, when a byte buffer view is created out of a memory segment, the buffer has the same temporal bound and thread-confinement guarantees as those of the segment it originated from. This means that if the segment is closed, any subsequent attempt to dereference its memory via a (previously obtained) byte buffer view will fail with an exception.

### Unsafe segments

It is sometimes necessary to create a segment out of an existing memory source, which might be managed by native code. This is the case, for instance, if we want to create a segment out of memory managed by a custom allocator.

The ByteBuffer API allows such a move, through a JNI [method](https://docs.oracle.com/javase/8/docs/technotes/guides/jni/spec/functions.html#NewDirectByteBuffer), namely `NewDirectByteBuffer`. This native method can be used to wrap a long address in a fresh byte buffer instance which is then returned to unsuspecting Java code.

Memory segments provide a similar capability - that is, given an address (which might have been obtained through some native calls), it is possible to wrap a segment around it, with given spatial, temporal and confinement bounds; a cleanup action to be executed when the segment is closed might also be specified.

For instance, assuming we have an address pointing at some externally managed memory block, we can construct an *unsafe* segment, as follows:

```java
MemoryAddress addr = MemoryAddress.ofLong(someLongAddr);
var unsafeSegment = addr.asSegmentRestricted(10);
```

The above code creates a new confined unsafe segment from a given address; the size of the segment is 10 bytes; the confinement thread is the current thread, and there's no cleanup action associated with the segment (that can be changed as needed by calling `MemorySegment::withCleanupAction`).

Of course, segments created this way are completely *unsafe*. There is no way for the runtime to verify that the provided address indeed points to a valid memory location, or that the size of the memory region pointed to by `addr` is indeed 10 bytes. Similarly, there are no guarantees that the underlying memory region associated with `addr` will not be deallocated *prior* to the call to `MemorySegment::close`.

For these reasons, creating unsafe segments is a *restricted* operation in the Foreign Memory Access API. Restricted operations can only be performed if the running application has set a read-only runtime property — `foreign.restricted=permit`. Any attempt to call restricted operations without said runtime property will fail with a runtime exception.

We plan, in the future, to make access to restricted operations more integrated with the module system; that is, certain modules might *require* restricted native access; when an application which depends on said modules is executed, the user might need to provide *permissions* to said modules to perform restricted native operations, or the runtime will refuse to build the application's module graph.

### Confinement

In addition to spatial and temporal bounds, segments also feature thread-confinement. That is, a segment is *owned* by the thread which created it, and no other thread can access the contents on the segment, or perform certain operations (such as `close`) on it. Thread confinement, while restrictive, is crucial to guarantee optimal memory access performance even in a multi-threaded environment.

The Foreign Memory Access API provides several ways to relax the thread confinement barriers. First, threads can cooperatively share segments by performing explicit *handoff* operations, where a thread releases its ownership on a given segment and transfers it onto another thread. Consider the following code:

```java
MemorySegment segmentA = MemorySegment.allocateNative(10); // confined by thread A
...
var segmentB = segmentA.handoff(threadB); // confined by thread B
```

This pattern of access is also known as *serial confinement* and might be useful in producer/consumer use cases where only one thread at a time needs to access a segment. Note that, to make the handoff operation safe, the API *kills* the original segment (as if `close` was called, but without releasing the underlying memory) and returns a *new* segment with the correct owner. That is, handoff operations are *terminal operations* (like `MemorySegment::close`). The implementation also makes sure that all writes by the first thread are flushed into memory by the time the second thread accesses the segment.

When serial confinement is not enough, clients can optionally remove thread ownership, that is, turn a confined segment into a *shared* one which can be accessed — and closed — concurrently, by multiple threads<a href="#2"><sup>2</sup></a>. As before, sharing a segment kills the original segment and returns a new segment with no owner thread:

```java
MemorySegment segmentA = MemorySegment.allocateNative(10); // confined by thread A
...
var sharedSegment = segmentA.share() // shared segment
```

A shared segments is especially useful when multiple threads need to operate on the segment's contents in *parallel* (e.g. using a framework such as Fork/Join) — by obtaining a `Spliterator` instance out of a memory segment. For instance to sum all the 32 bit values of a memory segment in parallel, we can use the following code:

```java
SequenceLayout seq = MemoryLayout.ofSequence(1_000_000, MemoryLayouts.JAVA_INT);
SequenceLayout seq_bulk = seq.reshape(-1, 100);
VarHandle intHandle = seq.varHandle(int.class, sequenceElement());    

int sum = StreamSupport.stream(MemorySegment.spliterator(segment.share(), seq_bulk), true)
                .mapToInt(slice -> {
					int res = 0;
        			for (int i = 0; i < 100 ; i++) {
            			res += MemoryAccess.getIntAtIndex(slice, i);
        			}
        			return res;
                }).sum();
```

The `MemorySegment::spliterator` takes a segment, a *sequence* layout and returns a spliterator instance which splits the segment into chunks which corresponds to the elements in the provided sequence layout. Here, we want to sum elements in an array which contains a million of elements; now, doing a parallel sum where each computation processes *exactly* one element would be inefficient, so instead we use the layout API to derive a *bulk* sequence layout. The bulk layout is a sequence layout which has the same size of the original layouts, but where the elements are arranged into groups of 100 elements — which should make it more amenable to parallel processing.

Once we have the spliterator, we can use it to construct a parallel stream and sum the contents of the segment in parallel. Since the segment operated upon by the spliterator is shared, the segment can be accessed from multiple threads concurrently; the spliterator API ensures that the access occurs in a regular fashion: a slice is created from the original segment, and given to a thread to perform some computation — thus ensuring that no two threads can ever operate concurrently on the same memory region.

Shared segment can also be useful to perform serial confinement in cases where the thread handing off the segment does not know which other thread will continue the work on the segment, for instance:

```java
// thread A
MemorySegment segment = MemorySegment.allocateNative(10); // confined by thread A
//do some work
segment = segment.share();

// thread B
segment.handoff(Thread.currentThread()); // now confined by thread B
// do some more work
```

That is, multiple threads can *race* to acquire a given shared segment — the API ensures that only one of them will succeed in acquiring ownership of the shared segment.

### Implicit deallocation

While memory segment feature *deterministic deallocation* they can also be registered against a `Cleaner`, to make sure that the memory resources associated with a segment are released when the GC determines that the segment is no longer *reachable*:

```java
MemorySegment segment = MemorySegment.allocateNative(100);
Cleaner cleaner = Cleaner.create();
segment = segment.registerCleaner(cleaner);
// do some work
segment = null; // Cleaner might reclaim the segment memory now
```

As for handoff, registering a segment with a cleaner *kills* the current segment and returns a new one which features implicit deallocation; this is also a *terminal operation*. Note that registering a segment with a cleaner doesn't prevent clients from calling `MemorySegment::close` explicitly on the returned segment; the API will guarantee that the segment's cleanup action will be called at most once — either explicitly, or implicitly (by a cleaner). Moreover, since an unreachable segment cannot (by definition) be accessed by any thread, the cleaner can always release any memory resources associated with an unreachable segment, regardless of whether it is a confined, or a shared segment.

* <a id="1"/>(<sup>1</sup>):<small> In general, deriving a complete layout from a C `struct` declaration is no trivial matter, and it's one of those areas where tooling can help greatly.</small>
* <a id="2"/>(<sup>2</sup>):<small> Shared segments rely on VM thread-local handshakes (JEP [312](https://openjdk.java.net/jeps/312)) to implement lock-free, safe, shared memory access; that is, when it comes to memory access, there should no difference in performance between a shared segment and a confined segment. On the other hand, `MemorySegment::close` might be slower on shared segments than on confined ones.</small>

