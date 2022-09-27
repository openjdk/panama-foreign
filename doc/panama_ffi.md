## State of foreign function support

**May 2022**

**Maurizio Cimadamore**

Panama supports foreign functions through the Foreign Linker API, which has been available as an [incubating](https://openjdk.java.net/jeps/11) API since Java [16](https://openjdk.java.net/jeps/389). The central abstraction in the Foreign Linker API is the *foreign linker*, which allows clients to construct *downcall* method handles — that is, method handles whose invocation targets a native function defined in some native library. In other words, Panama foreign function support is completely expressed in terms of Java code and no intermediate native code is required.

### Native addresses

Before we dive into the specifics of the foreign function support, it would be useful to briefly recap some of the main concepts we have learned when exploring the [foreign memory access support](panama_memaccess.md). The Foreign Memory Access API allows client to create and manipulate *memory segments*. A memory segment is a view over a memory source (either on- or off-heap) which is spatially bounded, temporally bounded and thread-confined. The guarantees ensure that dereferencing a segment that has been created by Java code is always *safe*, and can never result in a VM crash, or, worse, in silent memory corruption.

Now, in the case of memory segments, the above properties (spatial bounds, temporal bounds and confinement) can be known *in full* when the segment is created. But when we interact with native libraries we often receive *raw* pointers; such pointers have no spatial bounds (does a `char*` in C refer to one `char`, or a `char` array of a given size?), no notion of temporal bounds, nor thread-confinement. Raw addresses in our interop support are modeled using the `MemoryAddress` abstraction.

If clients want to dereference `MemoryAddress`, they can do so *unsafely* in two ways. First, they can use one of the *unsafe* dereference methods provided by `MemoryAddress` (these methods closely mirror those offered by `MemorySegment`); these methods are *restricted* and will generate runtime warnings if called without specifying the `--enable-native-access` command-line flag:

```java
...
MemoryAddress addr = ... //obtain address from native code
int x = addr.get(JAVA_INT, 0);
```

Alternatively, the client can create a memory segment from an address *unsafely*, using the `MemorySegment::ofAddress` factory (which is also a *restricted* method); this can also be useful to inject extra knowledge about spatial bounds which might be available in the native library the client is interacting with:

```java
MemoryAddress addr = ... //obtain address from native code
try (MemorySession session = MemorySession openConfined()) {
	MemorySegment segment = MemorySegment.ofAddress(100, session);
	int x = segment.get(JAVA_INT, 0);
}
```

Both `MemoryAddress` and `MemorySegment` implement the `Addressable` interface, which is an interface modelling entities that can be passed *by reference* — that is, which can be projected to a `MemoryAddress` instance. In the case of `MemoryAddress` such a projection is the identity function; in the case of a memory segment, the projection returns the `MemoryAddress` instance for the segment's base address. This abstraction allows to pass either memory address or memory segments where an address is expected (this is especially useful when generating native bindings).

### Segment allocators

Idiomatic C code implicitly relies on stack allocation to allow for concise variable declarations; consider this example:

```c
int arr[] = { 0, 1, 2, 3, 4 };
```

A variable initializer such as the one above can be implemented as follows, using the Foreign Memory Access API:

```java
try (MemorySession session = MemorySession openConfined()) {
    MemorySegment arr = MemorySegment.allocateNative(MemoryLayout.sequenceLayout(5, JAVA_INT), session);
    for (int i = 0 ; i < 5 ; i++) {
        arr.setAtIndex(JAVA_INT, i, i);
    }
}
```

There are a number of issues with the above code snippet:

* compared to the C code, it is more verbose — the native array has to be initialized *element by element*
* allocation is very slow compared to C; allocating the `arr` variable now takes a full `malloc`, while in C the variable was simply stack-allocated
* when having multiple declarations like the one above, it might become increasingly harder to manage the lifecycle of the various segments

To address these problems, Panama provides a `SegmentAllocator` abstraction, a functional interface which provides methods to allocate commonly used values. Since `MemorySession` implements the `SegmentAllocator` interface, the above code can be rewritten conveniently as follows:

```java
try (MemorySession session = MemorySession openConfined()) {    
    MemorySegment arr = session.allocateArray(JAVA_INT, 0, 1, 2, 3, 4);
} // 'arr' is released here
```

In the above code, the memory session acts as a *native* allocator (that is, an allocator built on top of `MemorySegment::allocateNative`). The session is then used to create a native array, initialized to the values `0, 1, 2, 3, 4`.  The array initialization is more efficient, compared to the previous snippet, as the Java array is copied *in bulk* into the memory region associated with the newly allocated memory segment. The returned segment is associated with the session which performed the allocation, meaning that the segment will no longer be accessible after the try-with-resource construct.

Custom segment allocators are also critical to achieve optimal allocation performance; for this reason, a number of predefined allocators are available via factories in the `SegmentAllocator` interface. For instance, it is possible to create an arena-based allocator, as follows:

```java
try (MemorySession session = MemorySession openConfined()) {
    SegmentAllocator allocator = SegmentAllocator.newNativeArena(session);
    for (int i = 0 ; i < 100 ; i++) {
        allocator.allocateArray(JAVA_INT, 0, 1, 2, 3, 4);
    }
    ...
} // all memory allocated is released here
```

The above code creates a confined session; inside the *try-with-resources*, a new unbounded arena allocation is created, associated with the existing session. The allocator will pre-allocate a native segment, of a specific size, and respond to allocation requests by returning different slices of the pre-allocated segment. If the pre-allocated segment does not have sufficient space to accommodate a new allocation request, a new segment will be allocated. If the session associated with the arena allocator is closed, all memory segments created by the allocator (see the body of the `for` loop) will be deallocated at once. This idiom combines the advantages of deterministic deallocation (provided by the Memory Access API) with a more flexible and scalable allocation scheme, and can be very useful when writing large applications.

For these reasons, all the methods in the Foreign Linker API which *produce* memory segments (see `VaList::nextVarg`), allow an optional allocator to be provided by user code — this is key in ensuring that an application using the Foreign Linker API achieves optimal allocation performances, especially in non-trivial use cases.

### Symbol lookups

The first ingredient of any foreign function support is a mechanism to lookup symbols in native libraries. In traditional Java/JNI, this is done via the `System::loadLibrary` and `System::load` methods. Unfortunately, these methods do not provide a way for clients to obtain the *address* associated with a given library symbol. For this reason, the Foreign Linker API introduces a new abstraction, namely `SymbolLookup` (similar in spirit to a method handle lookup), which provides capabilities to lookup named symbols; we can obtain a symbol lookup in 3 different ways:

* `SymbolLookup::libraryLookup(String, MemorySession)` — creates a symbol lookup which can be used to search symbol in a library with the given name. The provided memory session parameter controls the library lifecycle: that is, when the memory session is closed, the library referred to by the lookup will also be closed;
* `SymbolLookup::loaderLookup` — creates a symbol lookup which can be used to search symbols in all the libraries loaded by the caller's classloader (e.g. using `System::loadLibrary` or `System::load`)
* `Linker::defaultLookup` — returns the default symbol lookup associated with a `Linker` instance. For instance, the default lookup of the native linker (see `Linker::nativeLinker`) can be used to look up platform-specific symbols in the standard C library (such as `strlen`, or `getpid`).

Once a lookup has been obtained, a client can use it to retrieve handles to library symbols (either global variables or functions) using the `lookup(String)` method, which returns an `Optional<MemorySegment>`.  The memory segments returned by the `lookup` are zero-length segments, whose base address is the address of the function or variable in the library.

For instance, the following code can be used to look up the `clang_getClangVersion` function provided by the `clang` library; it does so by creating a *library lookup* whose lifecycle is associated to that of a confined memory session.

```java
try (MemorySession session = MemorySession.openConfined()) {
    SymbolLookup libclang = SymbolLookup.libraryLookup("libclang.so");
    MemorySegment clangVersion = libclang.lookup("clang_getClangVersion").get();
}
```

### Linker

At the core of Panama foreign function support we find the `Linker` abstraction. This abstraction plays a dual role: first, for downcalls, it allows modelling foreign function calls as plain `MethodHandle` calls (see `Linker::downcallHandle`); second, for upcalls, it allows to convert an existing `MethodHandle` (which might point to some Java method) into a `MemorySegment` which could then be passed to foreign functions as a function pointer (see `Linker::upcallStub`):

```java
interface Linker {
    MethodHandle downcallHandle(Addressable symbol, FunctionDescriptor function);
    MemorySegment upcallStub(MethodHandle target, FunctionDescriptor function, MemorySession session);    
    ... // some overloads omitted here

    static Linker nativeLinker() { ... }
}
```

Both functions take a `FunctionDescriptor` instance — essentially an aggregate of memory layouts which is used to describe the argument and return types of a foreign function in full. Supported layouts are *value layouts* (for scalars and pointers) and *group layouts* (for structs/unions). Each layout in a function descriptor is associated with a carrier Java type (see table below); together, all the carrier types associated with layouts in a function descriptor will determine a unique Java `MethodType`  — that is, the Java signature that clients will be using when interacting with said downcall handles, or upcall stubs.

The `Linker::nativeLinker` factory is used to obtain a `Linker` implementation for the ABI associated with the OS and processor where the Java runtime is currently executing. As such, the native linker can be used to call C functions. The following table shows the mapping between C types, layouts and Java carriers under the Linux/macOS native linker implementation; note that the mappings can be platform dependent: on Windows/x64, the C type `long` is 32-bit, so the `JAVA_INT` layout (and the Java carrier `int.class`) would have to be used instead:

| C type                                                       | Layout                                                       | Java carrier                       |
| ------------------------------------------------------------ | ------------------------------------------------------------ | ---------------------------------- |
| `bool`                                                       | `JAVA_BOOLEAN`                                               | `byte`                             |
| `char`                                                       | `JAVA_BYTE`                                                  | `byte`                             |
| `short`                                                      | `JAVA_SHORT`                                                 | `short`, `char`                    |
| `int`                                                        | `JAVA_INT`                                                   | `int`                              |
| `long`                                                       | `JAVA_LONG`                                                  | `long`                             |
| `long long`                                                  | `JAVA_LONG`                                                  | `long`                             |
| `float`                                                      | `JAVA_FLOAT`                                                 | `float`                            |
| `double`                                                     | `JAVA_DOUBLE`                                                | `double`                           |
| `char*`<br />`int**`<br /> ...                               | `ADDRESS`                                                    | `Addressable`<br />`MemoryAddress` |
| `struct Point { int x; int y; };`<br />`union Choice { float a; int b; };`<br />... | `MemoryLayout.structLayout(...)`<br />`MemoryLayout.unionLayout(...)`<br /> | `MemorySegment`                    |

Note that all C pointer types are modelled using the `ADDRESS` layout constant; the Java carrier type associated with this layout is either `Addressable` or `MemoryAddress` depending on where the layout occurs in the function descriptor. For downcall method handles, for instance, the `Addressable` carrier is used when the `ADDRESS` layout occurs in a parameter position of the corresponding function descriptor. This maximizes applicability of a downcall method handles, ensuring that any implementation of `Addressable` (e.g. memory segments, memory address, upcall stubs, va lists) can be passed where a pointer is expected.

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
		linker.defaultLookup().lookup("strlen").get(),
        FunctionDescriptor.of(JAVA_LONG, ADDRESS)
);
```

Note that, since the function `strlen` is part of the standard C library, which is loaded with the VM, we can just use the default lookup of the native linker to look it up. The rest is pretty straightforward — the only tricky detail is how to model `size_t`: typically this type has the size of a pointer, so we can use `JAVA_LONG` both Linux and Windows. On the Java side, we model the `size_t` using a `long` and the pointer is modelled using an `Addressable` parameter.

Once we have obtained the downcall method handle, we can just use it as any other method handle<a href="#2"><sup>1</sup></a>:

```java
try (MemorySession session = MemorySession openConfined()) {
    long len = strlen.invoke(session.allocateUtf8String("Hello")); // 5
}
```

Here we are using a memory session to convert a Java string into an off-heap memory segment which contains a `NULL` terminated C string. We then pass that segment to the method handle and retrieve our result in a Java `long`. Note how all this is possible *without* any piece of intervening native code — all the interop code can be expressed in (low level) Java. Note also how we use an explicit memory session to control the lifecycle of the allocated C string, which ensures timely deallocation of the memory segment holding the native string.

The `Linker` interface also supports linking of native functions without an address known at link time; when that happens, an address (of type `Addressable`) must be provided when the method handle returned by the linker is invoked — this is very useful to support *virtual calls*. For instance, the above code can be rewritten as follows:

```java
MethodHandle strlen_virtual = linker.downcallHandle( // address parameter missing!
		FunctionDescriptor.of(JAVA_LONG, ADDRESS)
);

try (MemorySession session = MemorySession openConfined()) {
    long len = strlen_virtual.invoke(
        linker.defaultLookup().lookup("strlen").get() // address provided here!
        session.allocateUtf8String("Hello")
    ); // 5
}
```

It is important to note that, albeit the interop code is written in Java, the above code can *not* be considered 100% safe. There are many arbitrary decisions to be made when setting up downcall method handles such as the one above, some of which might be obvious to us (e.g. how many parameters does the function take), but which cannot ultimately be verified by the Panama runtime. After all, a symbol in a dynamic library is nothing but a numeric offset and, unless we are using a shared library with debugging information, no type information is attached to a given library symbol. This means that the Panama runtime has to *trust* the function descriptor passed in<a href="#3"><sup>2</sup></a>; for this reason, the `Linker::nativeLinker` factory is also a restricted method.

If a native function returns a raw pointer (of type `MemoryAddress`), it is then up to the client to make sure that the address is being accessed and disposed of correctly, compatibly with the requirements of the underlying native library. If a native function returns a struct by value, a *fresh*, memory segment is allocated off-heap and returned to the caller. In such cases, the downcall method handle will feature an additional prefix `SegmentAllocator` (see above) parameter which will be used by the downcall method handle to allocate the returned segment. The allocation will likely associate the segment with a memory session that is known to the caller and which can then be used to release the memory associated with that segment. 

When working with deterministic deallocation and shared memory session, it is always possible for the session associated with a memory segment passed *by reference* to a native function to be closed (by another thread) *while* the native function is executing. When this happens, the native code is at risk of dereferencing already-freed memory, which might trigger a JVM crash, or even result in silent memory corruption. For this reason, the `Linker` API provides some basic temporal safety guarantees: any `Addressable` instance passed to a downcall method handle will be *kept alive* for the entire duration of the call. In other words, it's as if the call to the downcall method handle occurred inside an invisible call to `MemorySession::whileAlive`.

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
try (MemorySession session = MemorySession openConfined()) {
    MemorySegment comparFunc = linker.upcallStub(comparHandle, comparDesc, session);
    MemorySegment array = session.allocateArray(0, 9, 3, 4, 6, 5, 1, 8, 2, 7);
    qsort.invoke(array, 10L, 4L, comparFunc);
    int[] sorted = array.toArray(JAVA_INT); // [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 ]
}
```

The above code creates an upcall stub — `comparFunc` — a function pointer that can be used to invoke our Java comparator function, of type `MemorySegment`. The upcall stub is associated with the provided memory session instance; this means that the stub will be uninstalled when the session is closed.

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

To do this using the foreign function support provided by Panama we would have to build a *specialized* downcall handle for that call shape, using the `FunctionDescriptor::asVariadic` to inject additional variadic layouts, as follows:

```java
Linker linker = Linker.nativeLinker();
MethodHandle printf = linker.downcallHandle(
		linker.defaultLookup().lookup("printf").get(),
        FunctionDescriptor.of(JAVA_INT, ADDRESS).asVariadic(JAVA_INT, JAVA_INT, JAVA_INT)
);
```

Then we can call the specialized downcall handle as usual:

```java
try (MemorySession session = MemorySession openConfined()) {    
    printf.invoke(session.allocateUtf8String("%d plus %d equals %d"), 2, 2, 4); //prints "2 plus 2 equals 4"
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
try (MemorySession session = MemorySession openConfined()) {
    vprintf.invoke(
            session.allocateUtf8String("%d plus %d equals %d"),
            VaList.make(builder ->
                            builder.addVarg(JAVA_INT, 2)
                                   .addVarg(JAVA_INT, 2)
                                   .addVarg(JAVA_INT, 4), session)
); //prints "2 plus 2 equals 4"
```

While the callee has to do more work to call the `vprintf` handle, note that that now we're back in a place where the downcall handle  `vprintf` can be shared across multiple callees. Note that both the format string and the `VaList` are associated with the given memory session — this means that both will remain valid throughout the native function call.

Using `VaList` also scales to upcall stubs — it is therefore possible for clients to create upcalls stubs which take a `VaList` and then, from the Java upcall, read the arguments packed inside the `VaList` one by one using the methods provided by the `VaList` API (e.g. `VaList::nextVarg(ValueLayout.OfInt)`), which mimics the behavior of the C `va_arg` macro.

### Appendix: full source code

The full source code containing most of the code shown throughout this document can be seen below:

```java
import java.lang.foreign.Linker;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.MemoryAddress;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.MemorySession;
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
                STDLIB.lookup("strlen").get(),
                FunctionDescriptor.of(JAVA_LONG, ADDRESS)
        );

        try (MemorySession session = MemorySession.openConfined()) {
            MemorySegment hello = session.allocateUtf8String("Hello");
            long len = (long) strlen.invoke(hello); // 5
            System.out.println(len);
        }
    }

    public static void strlen_virtual() throws Throwable {
        MethodHandle strlen_virtual = LINKER.downcallHandle(
                FunctionDescriptor.of(JAVA_LONG, ADDRESS)
        );

        try (MemorySession session = MemorySession.openConfined()) {
            MemorySegment hello = session.allocateUtf8String("Hello");
            long len = (long) strlen_virtual.invoke(
                STDLIB.lookup("strlen").get(),
                hello); // 5
            System.out.println(len);
        }
    }

    static class Qsort {
        static int qsortCompare(MemoryAddress addr1, MemoryAddress addr2) {
            return addr1.get(JAVA_INT, 0) - addr2.get(JAVA_INT, 0);
        }
    }

    public static void qsort() throws Throwable {
        MethodHandle qsort = LINKER.downcallHandle(
                STDLIB.lookup("qsort").get(),
                FunctionDescriptor.ofVoid(ADDRESS, JAVA_LONG, JAVA_LONG, ADDRESS)
        );
        FunctionDescriptor comparDesc = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS);
        MethodHandle comparHandle = MethodHandles.lookup()
                                         .findStatic(Qsort.class, "qsortCompare",
                                                     Linker.upcallType(comparDesc));

        try (MemorySession session = MemorySession.openConfined()) {
            MemorySegment comparFunc = LINKER.upcallStub(
                comparHandle, comparDesc, session);

            MemorySegment array = session.allocateArray(JAVA_INT, 0, 9, 3, 4, 6, 5, 1, 8, 2, 7);
            qsort.invoke(array, 10L, 4L, comparFunc);
            int[] sorted = array.toArray(JAVA_INT); // [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 ]
            System.out.println(Arrays.toString(sorted));
        }
    }

    public static void printf() throws Throwable {
        MethodHandle printf = LINKER.downcallHandle(
                STDLIB.lookup("printf").get(),
                FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT, JAVA_INT),
                Linker.Option.firstVariadicArg(1) // first int is variadic
        );
        try (MemorySession session = MemorySession.openConfined()) {
            MemorySegment s = session.allocateUtf8String("%d plus %d equals %d\n");
            printf.invoke(s, 2, 2, 4);
        }
    }

    public static void vprintf() throws Throwable {

        MethodHandle vprintf = LINKER.downcallHandle(
                STDLIB.lookup("vprintf").get(),
                FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS));

        try (MemorySession session = MemorySession.openConfined()) {
            MemorySegment s = session.allocateUtf8String("%d plus %d equals %d\n");
            VaList vlist = VaList.make(builder ->
                     builder.addVarg(JAVA_INT, 2)
                            .addVarg(JAVA_INT, 2)
                            .addVarg(JAVA_INT, 4), session);
            vprintf.invoke(s, vlist);
        }
    }
}
```



* <a id="1"/>(<sup>1</sup>):<small> For simplicity, the examples shown in this document use `MethodHandle::invoke` rather than `MethodHandle::invokeExact`; by doing so we avoid having to cast by-reference arguments back to `Addressable`. With `invokeExact` the method handle invocation should be rewritten as `strlen.invokeExact((Addressable)session.allocateUtf8String("Hello"));`</small>
* <a id="2"/>(<sup>2</sup>):<small> In reality this is not entirely new; even in JNI, when you call a `native` method the VM trusts that the corresponding implementing function in C will feature compatible parameter types and return values; if not a crash might occur.</small>
