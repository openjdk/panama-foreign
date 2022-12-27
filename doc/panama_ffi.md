## State of foreign function support

**December 2022**

**Maurizio Cimadamore**

The Foreign Function & Memory API (FFM API in short) provides access to foreign functions through the `Linker` interface, which has been available as an [incubating](https://openjdk.java.net/jeps/11) API since Java [16](https://openjdk.java.net/jeps/389). A linker allows clients to construct *downcall* method handles — that is, method handles whose invocation targets a native function defined in some native library. In other words, FFM API's foreign function support is completely expressed in terms of Java code and no intermediate native code is required.

### Zero-length memory segments

Before we dive into the specifics of the foreign function support, it would be useful to briefly recap some of the main concepts we have learned when exploring the [foreign memory access support](panama_memaccess.md). The Foreign Memory Access API allows client to create and manipulate *memory segments*. A memory segment is a view over a memory source (either on- or off-heap) which is spatially bounded, temporally bounded and thread-confined. The guarantees ensure that dereferencing a segment that has been created by Java code is always *safe*, and can never result in a VM crash, or, worse, in silent memory corruption.

Now, in the case of memory segments, the above properties (spatial bounds, temporal bounds and confinement) can be known *in full* when the segment is created. But when we interact with native libraries we often receive *raw* pointers; such pointers have no spatial bounds (does a `char*` in C refer to one `char`, or a `char` array of a given size?), no notion of temporal bounds, nor thread-confinement. Raw addresses in the FFM API are modelled using *zero-length memory segments*.

If clients want to dereference a zero-length memory segment, they can do so *unsafely* in two ways. First, the client can create a new memory segment from the zero-length memory segment *unsafely*, using the `MemorySegment::ofAddress` factory. This method is *restricted* and will generate runtime warnings if called without specifying the `--enable-native-access` command-line flag. By calling `MemorySegment::ofAddress` a client inject extra knowledge about spatial bounds which might be available in the native library the client is interacting with:

```java
MemorySegment raw = ... //obtain address from native code
try (Arena arena = Arena.openConfined()) {
    MemorySegment segment = MemorySegment.ofAddress(raw.address(), 100, arena.scope());
    int x = segment.get(JAVA_INT, 0);
}
```

Alternatively, clients can obtain an *unbounded* address value layout. This is done using the `ValueLayout.OfAddress::asUnbounded` method (which is also a restricted method). When an access operation uses an unbounded address value layouts, the runtime will wrap any corresponding raw addresses with native segments with <em>maximal</em> size (i.e. `Long.MAX_VALUE`). As such, these segments can be accessed directly, as follows:

```java
MemorySegment foreign = someSegment.get(ValueLayout.ADDRESS.asUnbounded(), 0); // wrap address into segment (size = Long.MAX_VALUE)
int x = foreign.get(ValueLayout.JAVA_INT, 0); //ok
```

Which approach is taken largely depends on the information that a client has available when obtaining a memory segment wrapping a native pointer. For instance, if such pointer points to a C struct, the client might prefer to resize the segment unsafely, to match the size of the struct (so that out-of-bounds access will be detected by the API). In other instances, however, there will be no, or little information as to what spatial and/or temporal bounds should be associated with a given native pointer. In these cases using an unbounded address layout might be preferable.
### Segment allocators

Idiomatic C code implicitly relies on stack allocation to allow for concise variable declarations; consider this example:

```c
int arr[] = { 0, 1, 2, 3, 4 };
```

A variable initializer such as the one above can be implemented as follows, using the Foreign Memory Access API:

```java
try (Arena arena = Arena openConfined()) {
    MemorySegment arr = MemorySegment.allocateNative(MemoryLayout.sequenceLayout(5, JAVA_INT), arena.scope());
    for (int i = 0 ; i < 5 ; i++) {
        arr.setAtIndex(JAVA_INT, i, i);
    }
}
```

There are a number of issues with the above code snippet:

* compared to the C code, it is more verbose — the native array has to be initialized *element by element*
* allocation is very slow compared to C; allocating the `arr` variable now takes a full `malloc`, while in C the variable was simply stack-allocated
* when having multiple declarations like the one above, it might become increasingly harder to manage the lifecycle of the various segments

To address these problems, the FFM API provides a `SegmentAllocator` abstraction, a functional interface which provides methods to allocate commonly used values. Since `Arena` implements the `SegmentAllocator` interface, the above code can be rewritten conveniently as follows:

```java
try (Arena arena = Arena.openConfined()) {
    MemorySegment arr = arena.allocateArray(JAVA_INT, 0, 1, 2, 3, 4);
} // 'arr' is released here
```

In the above code, the arena acts as a *native* allocator (that is, an allocator built on top of `MemorySegment::allocateNative`). The arena is then used to create a native array, initialized to the values `0, 1, 2, 3, 4`.  The array initialization is more efficient, compared to the previous snippet, as the Java array is copied *in bulk* into the memory region associated with the newly allocated memory segment. The returned segment is associated with the scope of the arena which performed the allocation, meaning that the segment will no longer be accessible after the try-with-resource construct.

Custom segment allocators are also critical to achieve optimal allocation performance; for this reason, a number of predefined allocators are available via factories in the `SegmentAllocator` interface. For example, the following code creates a *slicing* allocator and uses it to allocate a segment whose content is initialized from a Java `int` array:

```java
try (Arena arena = Arena.openConfined()) {
    SegmentAllocator allocator = SegmentAllocator.slicingAllocator(arena.allocate(1024));
    for (int i = 0 ; i < 10 ; i++) {
        MemorySegment s = allocator.allocateArray(JAVA_INT,  new int[] { 1, 2, 3, 4, 5 });
        ...
    }
    ...
 } // all memory allocated is released here
```

This code creates a native segment whose size is 1024 bytes. The segment is then used to create a slicing allocator, which responds to  allocation requests by returning slices of that pre-allocated segment.  If the current segment does not have sufficient space to accommodate an  allocation request, an exception is thrown. All of the memory associated with the segments created by the allocator (i.e., in the body of the  for loop) is deallocated atomically when the arena is closed. This  technique combines the advantages of deterministic deallocation,  provided by the `Arena` abstraction, with a more flexible and scalable allocation scheme. It can be very useful when writing code  which manages a large number of off-heap segments.

All the methods in the FFM API which *produce* memory segments (see `VaList::nextVarg` and downcall method handles), allow for an allocator parameter to be provided — this is key in ensuring that an application using the FFM API achieves optimal allocation performances, especially in non-trivial use cases.

### Symbol lookups

The first ingredient of any foreign function support is a mechanism to lookup symbols in native libraries. In traditional Java/JNI, this is done via the `System::loadLibrary` and `System::load` methods. Unfortunately, these methods do not provide a way for clients to obtain the *address* associated with a given library symbol. For this reason, the Foreign Linker API introduces a new abstraction, namely `SymbolLookup` (similar in spirit to a method handle lookup), which provides capabilities to lookup named symbols; we can obtain a symbol lookup in 3 different ways:

* `SymbolLookup::libraryLookup(String, SegmentScope)` — creates a symbol lookup which can be used to search symbol in a library with the given name. The provided segment scope parameter controls the library lifecycle: that is, when the scope is not longer alive, the library referred to by the lookup will also be closed;
* `SymbolLookup::loaderLookup` — creates a symbol lookup which can be used to search symbols in all the libraries loaded by the caller's classloader (e.g. using `System::loadLibrary` or `System::load`)
* `Linker::defaultLookup` — returns the default symbol lookup associated with a `Linker` instance. For instance, the default lookup of the native linker (see `Linker::nativeLinker`) can be used to look up platform-specific symbols in the standard C library (such as `strlen`, or `getpid`).

Once a lookup has been obtained, a client can use it to retrieve handles to library symbols (either global variables or functions) using the `find(String)` method, which returns an `Optional<MemorySegment>`.  The memory segments returned by the `lookup` are zero-length segments, whose base address is the address of the function or variable in the library.

For instance, the following code can be used to look up the `clang_getClangVersion` function provided by the `clang` library; it does so by creating a *library lookup* whose lifecycle is associated to that of a confined arena.

```java
try (Arena arena = Arena.openConfined()) {
    SymbolLookup libclang = SymbolLookup.libraryLookup("libclang.so", arena.scope());
    MemorySegment clangVersion = libclang.find("clang_getClangVersion").get();
}
```

### Linker

At the core of the FFM API's foreign function support we find the `Linker` abstraction. This abstraction plays a dual role: first, for downcalls, it allows modelling foreign function calls as plain `MethodHandle` calls (see `Linker::downcallHandle`); second, for upcalls, it allows to convert an existing `MethodHandle` (which might point to some Java method) into a `MemorySegment` which could then be passed to foreign functions as a function pointer (see `Linker::upcallStub`):

```java
interface Linker {
    MethodHandle downcallHandle(Addressable symbol, FunctionDescriptor function);
    MemorySegment upcallStub(MethodHandle target, FunctionDescriptor function, SegmentScope scope);
    ... // some overloads omitted here

    static Linker nativeLinker() { ... }
}
```

Both functions take a `FunctionDescriptor` instance — essentially an aggregate of memory layouts which is used to describe the argument and return types of a foreign function in full. Supported layouts are *value layouts* (for scalars and pointers) and *group layouts* (for structs/unions). Each layout in a function descriptor is associated with a carrier Java type (see table below); together, all the carrier types associated with layouts in a function descriptor will determine a unique Java `MethodType`  — that is, the Java signature that clients will be using when interacting with said downcall handles, or upcall stubs.

The `Linker::nativeLinker` factory is used to obtain a `Linker` implementation for the ABI associated with the OS and processor where the Java runtime is currently executing. As such, the native linker can be used to call C functions. The following table shows the mapping between C types, layouts and Java carriers under the Linux/macOS native linker implementation; note that the mappings can be platform dependent: on Windows/x64, the C type `long` is 32-bit, so the `JAVA_INT` layout (and the Java carrier `int.class`) would have to be used instead:

| C type                                                       | Layout                                                       | Java carrier    |
| ------------------------------------------------------------ | ------------------------------------------------------------ | --------------- |
| `bool`                                                       | `JAVA_BOOLEAN`                                               | `byte`          |
| `char`                                                       | `JAVA_BYTE`                                                  | `byte`          |
| `short`                                                      | `JAVA_SHORT`                                                 | `short`, `char` |
| `int`                                                        | `JAVA_INT`                                                   | `int`           |
| `long`                                                       | `JAVA_LONG`                                                  | `long`          |
| `long long`                                                  | `JAVA_LONG`                                                  | `long`          |
| `float`                                                      | `JAVA_FLOAT`                                                 | `float`         |
| `double`                                                     | `JAVA_DOUBLE`                                                | `double`        |
| `char*`<br />`int**`<br /> ...                               | `ADDRESS`                                                    | `MemorySegment` |
| `struct Point { int x; int y; };`<br />`union Choice { float a; int b; };`<br />... | `MemoryLayout.structLayout(...)`<br />`MemoryLayout.unionLayout(...)`<br /> | `MemorySegment` |

Both C structs/unions and pointers are modelled using the `MemorySegment` carrier type. However, C structs/unions are modelled in function descriptors with memory layouts of type `GroupLayout`, whereas pointers are modelled using the `ADDRESS` value layout constant (whose size is platform-specific). Moreover, the behavior of a downcall method handle returning a struct/union type is radically different from that of a downcall method handle returning a C pointer:

* downcall method handles returning C pointers will wrap the pointer address into a fresh zero-length memory segment (unless an unbounded address layout is specified);
* downcall method handles returning a C struct/union type will return a *new* segment, of given size (the size of the struct/union). The segment is allocated using a user-provided `SegmentAllocator`, which is provided using an additional prefix parameter inserted in the downcall method handle signature.

A tool, such as `jextract`, will generate all the required C layouts (for scalars and structs/unions) *automatically*, so that clients do not have to worry about platform-dependent details such as sizes, alignment constraints and padding.

### Downcalls

We will now look at how foreign functions can be called from Java using the native linker. Assume we wanted to call the following function from the standard C library:

```c
size_t strlen(const char *s);
```

In order to do that, we have to:

* lookup the `strlen` symbol
* describe the signature of the C function using a function descriptor

* create a *downcall* native method handle with the above information, using the native linker

Here's an example of how we might want to do that (a full listing of all the examples in this and subsequent sections will be provided in the [appendix](#appendix-full-source-code)):

```java
Linker linker = Linker.nativeLinker();
MethodHandle strlen = linker.downcallHandle(
        linker.defaultLookup().find("strlen").get(),
        FunctionDescriptor.of(JAVA_LONG, ADDRESS)
);
```

Note that, since the function `strlen` is part of the standard C library, which is loaded with the VM, we can just use the default lookup of the native linker to look it up. The rest is pretty straightforward — the only tricky detail is how to model `size_t`: typically this type has the size of a pointer, so we can use `JAVA_LONG` both Linux and Windows. On the Java side, we model the `size_t` using a `long` and the pointer is modelled using an `Addressable` parameter.

Once we have obtained the downcall method handle, we can just use it as any other method handle<a href="#2"><sup>1</sup></a>:

```java
try (Arena arena = Arena.openConfined()) {
    long len = strlen.invoke(arena.allocateUtf8String("Hello")); // 5
}
```

Here we are using a confined arena to convert a Java string into an off-heap memory segment which contains a `NULL` terminated C string. We then pass that segment to the method handle and retrieve our result in a Java `long`. Note how all this is possible *without* any piece of intervening native code — all the interop code can be expressed in (low level) Java. Note also how we use an arena to control the lifecycle of the allocated C string, which ensures timely deallocation of the memory segment holding the native string.

The `Linker` interface also supports linking of native functions without an address known at link time; when that happens, an address (of type `MemorySegment`) must be provided when the method handle returned by the linker is invoked — this is very useful to support *virtual calls*. For instance, the above code can be rewritten as follows:

```java
MethodHandle strlen_virtual = linker.downcallHandle( // address parameter missing!
		FunctionDescriptor.of(JAVA_LONG, ADDRESS)
);

try (Arena arena = Arena openConfined()) {
    long len = strlen_virtual.invoke(
        linker.defaultLookup().find("strlen").get() // address provided here!
        arena.allocateUtf8String("Hello")
    ); // 5
}
```

It is important to note that, albeit the interop code is written in Java, the above code can *not* be considered 100% safe. There are many arbitrary decisions to be made when setting up downcall method handles such as the one above, some of which might be obvious to us (e.g. how many parameters does the function take), but which cannot ultimately be verified by the Java runtime. After all, a symbol in a dynamic library is nothing but a numeric offset and, unless we are using a shared library with debugging information, no type information is attached to a given library symbol. This means that the Java runtime has to *trust* the function descriptor passed in<a href="#3"><sup>2</sup></a>; for this reason, the `Linker::nativeLinker` factory is also a restricted method.

When working with shared arenas, it is always possible for the arena associated with a memory segment passed *by reference* to a native function to be closed (by another thread) *while* the native function is executing. When this happens, the native code is at risk of dereferencing already-freed memory, which might trigger a JVM crash, or even result in silent memory corruption. For this reason, the `Linker` API provides some basic temporal safety guarantees: any `MemorySegment` instance passed by reference to a downcall method handle will be *kept alive* for the entire duration of the call. In other words, it's as if the call to the downcall method handle occurred inside an invisible call to `SegmentScope::whileAlive`.

Performance-wise, the reader might ask how efficient calling a foreign function using a native method handle is; the answer is *very*. The JVM comes with some special support for native method handles, so that, if a give method handle is invoked many times (e.g, inside a *hot* loop), the JIT compiler might decide to generate a snippet of assembly code required to call the native function, and execute that directly. In most cases, invoking native function this way is as efficient as doing so through JNI.

### Upcalls

Sometimes, it is useful to pass Java code as a function pointer to some native function; we can achieve that by using foreign linker support for upcalls. To demonstrate this, let's consider the following function from the C standard library:

```c
void qsort(void *base, size_t nmemb, size_t size,
           int (*compar)(const void *, const void *));
```

The `qsort` function can be used to sort the contents of an array, using a custom comparator function — `compar` — which is passed as a function pointer. To be able to call the `qsort` function from Java we have first to create a downcall method handle for it:

```java
Linker linker = Linker.nativeLinker();
MethodHandle qsort = linker.downcallHandle(
		linker.defaultLookup().lookup("qsort").get(),
        FunctionDescriptor.ofVoid(ADDRESS, JAVA_LONG, JAVA_LONG, ADDRESS)
);
```

As before, we use `JAVA_LONG` and `long.class` to map the C `size_t` type, and `ADDRESS` for both the first pointer parameter (the array pointer) and the last parameter (the function pointer).

This time, in order to invoke the `qsort` downcall handle, we need a *function pointer* to be passed as the last parameter; this is where the upcall support in foreign linker comes in handy, as it allows us to create a function pointer out of an existing method handle. First, let's write a function that can compare two int elements (passed as pointers):

```java
class Qsort {
	static int qsortCompare(MemoryAddress addr1, MemoryAddress addr2) {
		return addr1.get(JAVA_INT, 0) - addr2.get(JAVA_INT, 0);
	}
}
```

Here we can see that the function is performing some *unsafe* dereference of the pointer contents.

Now let's create a method handle pointing to the comparator function above:

```java
FunctionDescriptor comparDesc = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS);
MethodHandle comparHandle = MethodHandles.lookup()
                                         .findStatic(Qsort.class, "qsortCompare",
                                                     CLinker.upcallType(comparDesc));
```

To do that, we first create a function descriptor for the function pointer type, and then we use the `CLinker::upcallType` to turn that function descriptor into a suitable `MethodType` instance to be used in a method handle lookup. Now that we have a method handle for our Java comparator function, we finally have all the ingredients to create an upcall stub, and pass it to the `qsort` downcall handle:

```java
try (Arena arena = Arena.openConfined()) {
    MemorySegment comparFunc = linker.upcallStub(comparHandle, comparDesc, session);
    MemorySegment array = session.allocateArray(0, 9, 3, 4, 6, 5, 1, 8, 2, 7);
    qsort.invoke(array, 10L, 4L, comparFunc);
    int[] sorted = array.toArray(JAVA_INT); // [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 ]
}
```

The above code creates an upcall stub — `comparFunc` — a function pointer that can be used to invoke our Java comparator function, of type `MemorySegment`. The upcall stub is associated with the provided segment scope instance; this means that the stub will be uninstalled when the arena is closed.

The snippet then creates an off-heap array from a Java array, which is then passed to the `qsort` handle, along with the comparator function we obtained from the foreign linker.  As a side effect, after the call, the contents of the off-heap array will be sorted (as instructed by our comparator function, written in Java). We can than extract a new Java array from the segment, which contains the sorted elements. This is a more advanced example, but one that shows how powerful the native interop support provided by the foreign linker abstraction is, allowing full bidirectional interop support between Java and native.

### Varargs

Some C functions are *variadic* and can take an arbitrary number of arguments. Perhaps the most common example of this is the `printf` function, defined in the C standard library:

```c
int printf(const char *format, ...);
```

This function takes a format string, which features zero or more *holes*, and then can take a number of additional arguments that is identical to the number of holes in the format string.

The foreign function support can support variadic calls, but with a caveat: the client must provide a specialized Java signature, and a specialized description of the C signature. For instance, let's say we wanted to model the following C call:

```C
printf("%d plus %d equals %d", 2, 2, 4);
```

To do this using the foreign function support provided by the FFM API we would have to build a *specialized* downcall handle for that call shape, using the `FunctionDescriptor::asVariadic` to inject additional variadic layouts, as follows:

```java
Linker linker = Linker.nativeLinker();
MethodHandle printf = linker.downcallHandle(
		linker.defaultLookup().lookup("printf").get(),
        FunctionDescriptor.of(JAVA_INT, ADDRESS).asVariadic(JAVA_INT, JAVA_INT, JAVA_INT)
);
```

Then we can call the specialized downcall handle as usual:

```java
try (Arena arena = Arena.openConfined()) {
    printf.invoke(arena.allocateUtf8String("%d plus %d equals %d"), 2, 2, 4); //prints "2 plus 2 equals 4"
}
```

While this works, and provides optimal performance, there are some drawbacks:

* If the variadic function needs to be called with many shapes, we have to create many downcall handles
* while this approach works for downcalls (since the Java code is in charge of determining which and how many arguments should be passed) it fails to scale to upcalls; in that case, the call comes from native code, so we have no way to guarantee that the shape of the upcall stub we have created will match that required by the native function.

To add flexibility, the standard C foreign linker comes equipped with support for C variable argument lists — or `va_list`.  When a variadic function is called, C code has to unpack the variadic arguments by creating a `va_list` structure, and then accessing the variadic arguments through the `va_list` one by one (using the `va_arg` macro). To facilitate interop between standard variadic functions and `va_list` many C library functions in fact define *two* flavors of the same function, one using standard variadic signature, one using an extra `va_list` parameter. For instance, in the case of `printf` we can find that a `va_list`-accepting function performing the same task is also defined:

```c
int vprintf(const char *format, va_list ap);
```

The behavior of this function is the same as before — the only difference is that the ellipsis notation `...` has been replaced with a single `va_list` parameter; in other words, the function is no longer variadic.

It is indeed fairly easy to create a downcall for `vprintf`:

```java
Linker linker = Linker.nativeLinker();
MethodHandle vprintf = linker.downcallHandle(
		linker.defaultLookup().lookup("vprintf").get(),
		FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS));
```

Here, the layout of a `va_list` parameter is simply `ADDRESS` (as va lists are passed by reference). To call the `vprintf` handle we need to create an instance of `VaList` which contains the arguments we want to pass to the `vprintf` function — we can do so, as follows:

```java
try (Arena arena = Arena.openConfined()) {
    vprintf.invoke(
            arena.allocateUtf8String("%d plus %d equals %d"),
            VaList.make(builder ->
                            builder.addVarg(JAVA_INT, 2)
                                   .addVarg(JAVA_INT, 2)
                                   .addVarg(JAVA_INT, 4), arena.scope()).segment()
); //prints "2 plus 2 equals 4"
```

While the callee has to do more work to call the `vprintf` handle, note that that now we're back in a place where the downcall handle  `vprintf` can be shared across multiple callees. Note that both the format string and the `VaList` are associated with the given segment scope — this means that both will remain valid throughout the native function call.

Using `VaList` also scales to upcall stubs — it is therefore possible for clients to create upcalls stubs which take a `VaList` and then, from the Java upcall, read the arguments packed inside the `VaList` one by one using the methods provided by the `VaList` API (e.g. `VaList::nextVarg(ValueLayout.OfInt)`), which mimics the behavior of the C `va_arg` macro.

### Appendix: full source code

The full source code containing most of the code shown throughout this document can be seen below:

```java
import java.lang.foreign.Arena;
import java.lang.foreign.Linker;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.VaList;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;

import static java.lang.foreign.ValueLayout.*;

public class Examples {

    static Linker LINKER = Linker.nativeLinker();
    static SymbolLookup STDLIB = LINKER.defaultLookup();

    public static void main(String[] args) throws Throwable {
        strlen();
        strlen_virtual();
        qsort();
        printf();
        vprintf();
    }

    public static void strlen() throws Throwable {
        MethodHandle strlen = LINKER.downcallHandle(
                STDLIB.find("strlen").get(),
                FunctionDescriptor.of(JAVA_LONG, ADDRESS)
        );

        try (Arena arena = Arena.openConfined()) {
            MemorySegment hello = arena.allocateUtf8String("Hello");
            long len = (long) strlen.invoke(hello); // 5
            System.out.println(len);
        }
    }

    public static void strlen_virtual() throws Throwable {
        MethodHandle strlen_virtual = LINKER.downcallHandle(
                FunctionDescriptor.of(JAVA_LONG, ADDRESS)
        );

        try (Arena arena = Arena.openConfined()) {
            MemorySegment hello = arena.allocateUtf8String("Hello");
            long len = (long) strlen_virtual.invoke(
                STDLIB.find("strlen").get(),
                hello); // 5
            System.out.println(len);
        }
    }

    static class Qsort {
        static int qsortCompare(MemorySegment addr1, MemorySegment addr2) {
            return addr1.get(JAVA_INT, 0) - addr2.get(JAVA_INT, 0);
        }
    }

    public static void qsort() throws Throwable {
        MethodHandle qsort = LINKER.downcallHandle(
                STDLIB.find("qsort").get(),
                FunctionDescriptor.ofVoid(ADDRESS, JAVA_LONG, JAVA_LONG, ADDRESS)
        );
        FunctionDescriptor comparDesc = FunctionDescriptor.of(JAVA_INT, ADDRESS.asUnbounded(), ADDRESS.asUnbounded());
        MethodHandle comparHandle = MethodHandles.lookup()
                                         .findStatic(Qsort.class, "qsortCompare",
                                                     comparDesc.toMethodType());

        try (Arena arena = Arena.openConfined()) {
            MemorySegment comparFunc = LINKER.upcallStub(
                comparHandle, comparDesc, arena.scope());

            MemorySegment array = arena.allocateArray(JAVA_INT, 0, 9, 3, 4, 6, 5, 1, 8, 2, 7);
            qsort.invoke(array, 10L, 4L, comparFunc);
            int[] sorted = array.toArray(JAVA_INT); // [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 ]
            System.out.println(Arrays.toString(sorted));
        }
    }

    public static void printf() throws Throwable {
        MethodHandle printf = LINKER.downcallHandle(
                STDLIB.find("printf").get(),
                FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT, JAVA_INT),
                Linker.Option.firstVariadicArg(1) // first int is variadic
        );
        try (Arena arena = Arena.openConfined()) {
            MemorySegment s = arena.allocateUtf8String("%d plus %d equals %d\n");
            printf.invoke(s, 2, 2, 4);
        }
    }

    public static void vprintf() throws Throwable {

        MethodHandle vprintf = LINKER.downcallHandle(
                STDLIB.find("vprintf").get(),
                FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS));

        try (Arena arena = Arena.openConfined()) {
            MemorySegment s = arena.allocateUtf8String("%d plus %d equals %d\n");
            VaList vlist = VaList.make(builder ->
                     builder.addVarg(JAVA_INT, 2)
                            .addVarg(JAVA_INT, 2)
                            .addVarg(JAVA_INT, 4), arena.scope());
            vprintf.invoke(s, vlist.segment());
        }
    }
}
```



* <a id="1"/>(<sup>1</sup>):<small> For simplicity, the examples shown in this document use `MethodHandle::invoke` rather than `MethodHandle::invokeExact`; by doing so we avoid having to cast by-reference arguments back to `Addressable`. With `invokeExact` the method handle invocation should be rewritten as `strlen.invokeExact((Addressable)session.allocateUtf8String("Hello"));`</small>
* <a id="2"/>(<sup>2</sup>):<small> In reality this is not entirely new; even in JNI, when you call a `native` method the VM trusts that the corresponding implementing function in C will feature compatible parameter types and return values; if not a crash might occur.</small>
