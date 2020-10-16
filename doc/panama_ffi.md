## State of foreign function support

**October 2020**

* Adjust reference to NativeScope handoff
* Tweak references to restricted segments to use new API
* Tweak signature of LibraryLookup::lookup
* Replaced usages of ForeignLinker with CLinker, as per new API

**Maurizio Cimadamore**

In this document we explore the main concepts behind Panama's foreign function support; as we shall see, the central abstraction in the foreign function support is the so called *foreign linker*, an abstraction that allows clients to construct *native* method handles — that is, method handles whose invocation targets a native function defined in some native library. As we shall see, Panama foreign function support is completely expressed in terms of Java code and no intermediate native code is required.

### Native addresses

Before we dive into the specifics of the foreign function support, it would be useful to briefly recap some of the main concepts we have learned when exploring the [foreign memory access support](http://cr.openjdk.java.net/~mcimadamore/panama/foreign-memaccess.html). The Foreign Memory Access API allows client to create and manipulate *memory segments*. A memory segment is a view over a memory source (either on- or off-heap) which is spatially bounded, temporally bounded and thread-confined. The guarantees ensure that dereferencing a segment that has been created by Java code is always *safe*, and can never result in a VM crash, or, worse, in silent memory corruption.

Now, in the case of memory segments, the above properties (spatial bounds, temporal bounds and confinement) can be known *in full* when the segment is created. But when we interact with native libraries we will often be receiving *raw* pointers; such pointers have no spatial bounds (does a `char*` in C refer to one `char`, or a `char` array of a given size?), no notion of temporal bounds, nor thread-confinement. Raw addresses in our interop support are modelled using the `MemoryAddress` abstraction.

A memory address is just what the name implies: it encapsulates a memory address (either on- or off-heap). Since, in order to dereference memory using a memory access var handle, we need a segment, it follows that it is *not* possible to directly dereference a memory address — to do that we need a segment first. So clients can proceed in three different ways here.

First, if the memory address is known to belong to a segment the client *already* owns, a *rebase* operation can be performed; in other words, the client can ask the address what is its offset relative to a given segment, and then proceed to dereference the original segment accordingly:

```java
MemorySegment segment = MemorySegment.allocateNative(100);
...
MemoryAddress addr = ... //obtain address from native code
int x = MemoryAccess.getIntAtOffset(segment, addr.segmentOffset(segment));    
```

Secondly, if the client does *not* have a segment which contains a given memory address, it can create one *unsafely*, using the `MemoryAddress::asSegmentRestricted`; this can also be useful to inject extra knowledge about spatial bounds which might be available in the native library the client is interacting with:

```java
MemoryAddress addr = ... //obtain address from native code
MemorySegment segment = addr.asSegmentRestricted(100);
int x = MemoryAccess.getInt(segment);
```

Alternatively, the client can fall back to use the so called *everything* segment - that is, a primordial segment which covers the entire native heap. Since this segment is available as a constant, dereference can happen without the need of creating any additional segment instances:

```java
MemoryAddress addr = ... //obtain address from native code
int x = MemoryAccess.getIntAtOffset(MemorySegment.ofNativeRestricted(), addr.toRawLongValue());
```

Of course, since accessing the entire native heap is inherently *unsafe*, accessing the *everything* segment is considered a *restricted* operation which is only allowed after explicit opt in by setting the `foreign.restricted=permit` runtime flag.

`MemoryAddress`, like `MemorySegment` , implements the `Addressable` interface, which is a functional interface whose method projects an entity down to a `MemoryAddress` instance. In the case of `MemoryAddress` such a projection is the identity function; in the case of a memory segment, the projection returns the `MemoryAddres` instance for the segment's base address. This abstraction allows to pass either memory address or memory segments where an address is expected (this is especially useful when generating native bindings).

### Symbol lookups

The first ingredient of any foreign function support is a mechanism to lookup symbols in native libraries. In traditional Java/JNI, this is done via the `System::loadLibrary` and `System::load` methods, which internally map into calls to `dlopen`. In Panama, library lookups are modeled more directly, using a  class called`LibraryLookup`  (similar to a method handle lookup),  which provides capabilities to lookup named symbols in a given native library; we can obtain a library lookup in 3 different ways:

* `LibraryLookup::ofDefault`  — returns the library lookup which can *see* all the symbols that have been loaded with the VM
* `LibraryLookup::ofPath` — creates a library lookup associated with the library found at the given absolute path
* `LibraryLookup::ofLibrary` — creates a library lookup associated with the library with given name (this might require setting the `java.library.path` variable accordingly)

Once a lookup has been obtained, a client can use it to retrieve handles to library symbols (either global variables or functions) using the `lookup(String)` method, which returns an  `Optional<LibraryLookup.Symbol>`. A lookup symbol is just a proxy for a memory address (in fact, it implements `Addressable`) and a name.

For instance, the following code can be used to lookup the `clang_getClangVersion` function provided by the `clang` library:

```java
LibraryLookup libclang = LibraryLookup.ofLibrary("clang");
LibraryLookup.Symbol clangVersion = libclang.lookup("clang_getClangVersion").get();
```

One crucial distinction between the library loading support of the  Foreign Linker API and of JNI is that JNI libraries are loaded into a  class loader. Furthermore, to preserve [classloader integrity](https://docs.oracle.com/javase/7/docs/technotes/guides/jni/jni-12.html#libmanage) integrity, the same JNI library cannot be loaded into more than one  classloader.  The foreign function support described here is more  primitive — the Foreign Linker API allows clients to target native  libraries directly, without any intervening JNI code. Crucially, Java  objects are never passed to and from native code by the Foreign Linker API. Because of this, libraries loaded through the `LibraryLookup` hook are not tied to any class loader and can be (re)loaded as many times as needed.

### C Linker

At the core of Panama foreign function support we find the `CLinker` abstraction. This abstraction plays a dual role: first, for downcalls, it allows to model native function calls as plain `MethodHandle` calls (see `ForeignLinker::downcallHandle`); second, for upcalls, it allows to convert an existing `MethodHandle` (which might point to some Java method) into a `MemorySegment` which could then be passed to native functions as a function pointer (see `ForeignLinker::upcallStub`):

```java
interface CLinker {
    MethodHandle downcallHandle(Addressable func, MethodType type, FunctionDescriptor function);
    MemorySegment upcallStub(MethodHandle target, FunctionDescriptor function);
    ...
    static CLinker getInstance() { ... }
}
```

In the following sections we will dive deeper into how downcall handles and upcall stubs are created; here we want to focus on the similarities between these two routines. First, both take a `FunctionDescriptor` instance — essentially an aggregate of memory layouts which is used to describe the signature of a foreign function in full. Speaking of C, the `CLinker` class defines many layout constants (one for each main C primitive type) — these layouts can be combined using a `FunctionDescriptor` to describe the signature of a C function. For instance, assuming we have a C function taking a `char*` and returning a `long` we can model such a function with the following descriptor:

```java
FunctionDescriptor func = FunctionDescriptor.of(CLinker.C_LONG, CLinker.C_POINTER);
```

The layouts used above will be mapped to the right layout according to the platform we are executing on. This also means that these layouts will be platform dependent and that e.g. `C_LONG` will be a 32 bit value layout on Windows, while being a 64-bit value on Linux.

Layouts defined in the `CLinker` class are not only handy, as they already model the C types we want to work on; they also contain hidden pieces of information which the foreign linker support uses in order to compute the calling sequence associated with a given function descriptor. For instance, the two C types `int` and `float` might share a similar memory layout (they both are expressed as 32 bit values), but are typically passed using different machine registers. The layout attributes attached to the C-specific layouts in the `CLinker` class ensures that arguments and return values are interpreted in the correct way.

Another similarity between `downcallHandle` and `upcallStub` is that they both accept (either directly, or indirectly) a `MethodType` instance. The method type describes the Java signatures that clients will be using when interacting with said downcall handles, or upcall stubs. The C linker implementation adds constraints on which layouts can be used with which Java carrier — for instance by enforcing that the size of the Java carrier is equal to that of the corresponding layout, or by making sure that certain layouts are associated with specific carriers. The following table shows the Java carrier vs. layout mappings enforced by the Linux/macOS foreign linker implementation:

| C layout      | Java carrier    |
| ------------- | --------------- |
| `C_BOOL`      | `byte`          |
| `C_CHAR`      | `byte`          |
| `C_SHORT`     | `short`         |
| `C_INT`       | `int`           |
| `C_LONG`      | `long`          |
| `C_LONGLONG`  | `long`          |
| `C_FLOAT`     | `float`         |
| `C_DOUBLE`    | `double`        |
| `C_POINTER`   | `MemoryAddress` |
| `GroupLayout` | `MemorySegment` |

Aside from the mapping between primitive layout and primitive Java carriers (which might vary across platforms), it is important to note how all pointer layouts must correspond to a `MemoryAddress` carrier, whereas structs (whose layout is defined by a `GroupLayout`) must be associated with a `MemorySegment` carrier.

### Downcalls

We will now look at how foreign functions can be called from Java using the foreign linker abstraction. Assume we wanted to call the following function from the standard C library:

```c
size_t strlen(const char *s);
```

In order to do that, we have to:

* lookup the `strlen` symbol
* describe the signature of the C function using the layouts in the `CLinker` class

* select a Java signature we want to *overlay* on the native function — this will be the signature that clients of the native method handles will interact with
* create a *downcall* native method handle with the above information, using the standard C foreign linker

Here's an example of how we might want to do that (a full listing of all the examples in this and subsequent sections will be provided in the [appendix](#appendix: full-source-code)):

```java
MethodHandle strlen = CLinker.getInstance().downcallHandle(
		LibraryLookup.ofDefault().lookup("strlen").get(),
        MethodType.methodType(long.class, MemoryAddress.class),
        FunctionDescriptor.of(C_LONG, C_POINTER)
);
```

Note that, since the function `strlen` is part of the standard C library, which is loaded with the VM, we can just use the default lookup to look it up. The rest is pretty straightforward — the only tricky detail is how to model `size_t`: typically this type has the size of a pointer, so we can use `C_LONG` on Linux, but we'd have to use `C_LONGLONG` on Windows. On the Java side, we model the `size_t` using a `long` and the pointer is modeled using a `MemoryAddress` parameter.

One we have obtained the downcall native method handle, we can just use it as any other method handle:

```java
long len = strlen.invokeExact(CLinker.toCString("Hello").address()) // 5
```

Here we are using one of the helper methods in `CLinker` to convert a Java string into an off-heap memory segment which contains a `NULL` terminated C string. We then pass that segment to the method handle and retrieve our result in a Java `long`. Note how all this has been possible *without* any piece of intervening native code — all the interop code can be expressed in (low level) Java.

Now that we have seen the basics of how foreign function calls are supported in Panama, let's add some additional considerations. First, it is important to note that, albeit the interop code is written in Java, the above code can *not* be considered 100% safe. There are many arbitrary decisions to be made when setting up downcall method handles such as the one above, some of which might be obvious to us (e.g. how many parameters does the function take), but which cannot ultimately be verified by the Panama runtime. After all, a symbol in a dynamic library is, mostly a numeric offset and, unless we are using a shared library with debugging information, no type information is attached to a given library symbol. This means that, in this case, the Panama runtime has to *trust* our description of the `strlen` function. For this reason, access to the foreign linker is a restricted operation, which can only be performed if the runtime flag `foreign.restricted=permit` is passed on the command line of the Java launcher <a href="#1"><sup>1</sup></a>.

Finally let's talk about the life-cycle of some of the entities involved here; first, as a downcall native handle wraps a lookup symbol, the library from which the symbol has been loaded will stay loaded until there are reachable downcall handles referring to one of its symbols; in the above example, this consideration is less important, given the use of the default lookup object, which can be assumed to stay alive for the entire duration of the application.

Certain functions might return pointers, or structs; it is important to realize that if a function returns a pointer (or a `MemoryAddress`), no life-cycle whatsoever is attached to that pointer. It is then up to the client to e.g. free the memory associated with that pointer, or do nothing (in case the library is responsible for the life-cycle of that pointer). If a library returns a struct by value, things are different, as a *fresh*, confined memory segment is allocated off-heap and returned to the callee. It is the responsibility of the callee to cleanup that struct's segment (using `MemorySegment::close`) <a href="#2"><sup>2</sup></a>.

Performance-wise, the reader might ask how efficient calling a foreign function using a native method handle is; the answer is *very*. The JVM comes with some special support for native method handles, so that, if a give method handle is invoked many times (e.g, inside an *hot* loop), the JIT compiler might decide to just generate a snippet of assembly code required to call the native function, and execute that directly. In most cases, invoking native function this way is as efficient as doing so through JNI <a href="#3a"><sup>3a</sup></a><a href="#3b"><sup>3b</sup></a>.

### Upcalls

Sometimes, it is useful to pass Java code as a function pointer to some native function; we can achieve that by using foreign linker support for upcalls. To demonstrate this, let's consider the following function from the C standard library:

```c
void qsort(void *base, size_t nmemb, size_t size,
           int (*compar)(const void *, const void *));
```

This is a function that can be used to sort the contents of an array, using a custom comparator function — `compar` — which is passed as a function pointer. To be able to call the `qsort` function from Java we have first to create a downcall native method handle for it:

```java
MethodHandle qsort = CLinker.getInstance().downcallHandle(
		LibraryLookup.ofDefault().lookup("qsort").get(),
        MethodType.methodType(void.class, MemoryAddress.class, long.class, long.class, MemoryAddress.class),
        FunctionDescriptor.ofVoid(C_POINTER, C_LONG, C_LONG, C_POINTER)
);
```

As before, we use `C_LONG` and `long.class` to map the C `size_t` type, and we use `MemoryAddess.class` both for the first pointer parameter (the array pointer) and the last parameter (the function pointer).

This time, in order to invoke the `qsort` downcall handle, we need a *function pointer* to be passed as the last parameter; this is where the upcall support in foreign linker comes in handy, as it allows us to create a function pointer out of an existing method handle. First, let's write a function that can compare two int elements (passed as pointers):

```java
class Qsort {
	static int qsortCompare(MemoryAddress addr1, MemoryAddress addr2) {
		return MemoryAccess.getIntAtOffset(MemorySegment.ofNativeRestricted(), addr1.toRawLongValue()) - 
	    	   MemoryAccess.getIntAtOffset(MemorySegment.ofNativeRestricted(), addr2.toRawLongValue());
	}
}
```

Here we can see that the function is performing some *unsafe* dereference of the pointer contents, by using the *everything* segment. Now let's create a method handle pointing to the comparator function above:

```java
MethodHandle comparHandle = MethodHandles.lookup()
                                         .findStatic(Qsort.class, "qsortCompare",
                                                     MethodType.methodType(int.class, MemoryAddress.class, MemoryAddress.class));
```

Now that we have a method handle for our Java comparator function, we can create a function pointer, using the foreign linker upcall support  — as for downcalls,  we have to describe the signature of the foreign function pointer using the layouts in the `CLinker` class:

```java
MemorySegment comparFunc = CLinker.getInstance().upcallStub(
    comparHandle,
    FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER)
);
```

So, we finally have a memory segment — `comparFunc` — containing a stub that can be used to invoke our Java comparator function; this means we now have all we need to call the `qsort` downcall handle:

```java
MemorySegment array = MemorySegment.allocateNative(4 * 10);
array.copyFrom(MemorySegment.ofArray(new int[] { 0, 9, 3, 4, 6, 5, 1, 8, 2, 7 }));
qsort.invokeExact(array.address(), 10L, 4L, comparFunc.address());
int[] sorted = array.toIntArray(); // [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 ]
```

The above code creates an off-heap array, then copies the contents of a Java array on it (we shall see in the next [section](#native-scope) ways to do that more succinctly), and then pass the array to the `qsort` handle, along with the comparator function we obtained from the foreign linker.  As a side-effect, after the call, the contents of the off-heap array will be sorted (as instructed by our comparator function, written in Java). We can than extract a new Java array from the segment, which contains the sorted elements. This is a more advanced example, but one that shows how powerful the native interop support provided by the foreign linker abstraction is, allowing full bidirectional interop support between Java and native.

As before, we conclude with a quick note on life-cycle. First, the life-cycle of the upcall stub is tied to that of the segment returned by the foreign linker. When the segment is closed, the upcall is uninstalled from the VM and will no longer be a valid function pointer. Second, the life-cycle of structs (if any) passed by value to the Java upcall function is independent from that of the upcall <a href="#4"><sup>4</sup></a>, so again the user will have to pay attention not to leak memory and to call `MemorySegment::close` on any segments obtained through an upcall.

### Native scope

Idiomatic C code implicitly relies on stack allocation to allow for concise variable declarations; consider this example:

```c
void foo(int i, int *size);

int size = 5;
foo(42, &size);
```

Here the function `foo` takes an output parameter, a pointer to an `int` variable. Unfortunately (and we have seen part of that pain in the `qsort` example above), implementing this idiom in Panama is not straightforward:

```java
MethodHandle foo = ...
MemorySegment size = MemorySegment.allocateNative(C_INT);
MemoryAccess.setInt(size, 5);
foo.invokeExact(42, size);
```

There are a number of issues with the above code snippet:

* compared to the C code, it is very verbose
* allocation is very slow compared to C; allocating the `size` variable now takes a full `malloc`, while in C the variable was simply stack-allocated
* we end up with a standalone segment with its own temporal bounds, which will have to be *closed* later, to avoid leaks

To address these problems, Panama provides a `NativeScope` abstraction, which can be used to group allocation so as to achieve superior allocation performance, but also so that groups of logically-related segments can share the same temporal bounds, and can therefore be closed at once (e.g. by using a single *try-with-resources* statement). With the help of native scopes, the above code can be rewritten as follows:

```java
try (NativeScope scope = NativeScope.unboundedScope()) {
	MemorySegment size = scope.allocate(C_INT, 5);
	foo.invokeExact(42, size);
}
```

It's easy to see how this code improves over the former in many ways; first, native scopes have primitives to allocate *and* initialize the contents of a segment; secondly, native scope use more efficient allocation underneath, so that not every allocation request is turned into a `malloc` — in fact, if the size of memory to be used is known before hand, clients can also use the bounded variant of native scope, using the `NativeScope::boundedScope(long size)` factory <a href="#5"><sup>5</sup></a>. Third, a native scope can be used as a single temporal bound for all the segments allocated within it: that is, if the code needs to instantiate other variables, it can keep doing so using the same scope — when the *try-with-resource* statement completes, all resources associated with the scope will be freed.

There are at least two cases where allocation of native resources occurs *outside* a native scope:

* structs segments returned by native calls (or passed to upcalls Java methods)
* upcall stubs segments returned by the foreign linker

In these cases, the API gives us a segment which feature *its own* temporal bounds — this is handy, as we can use the segment to control the lifecycle of the resource allocated by the foreign linker support; but it is also a bit unfortunate: if the surrounding code happens to already have a native scope, these new segments won't be able to interoperate with it, and a separate *try-with-resource* segment should be used.

To alleviate this problem, and allow *all* segments to be managed using the *same* native scope, the native scope API not only supports the ability to allocate *new* segments, but it also allows to take ownership of *existing* ones. To see an example of this, let's go back to our `qsort` example, and see how we can make it better by using native scopes:

```java
try (NativeScope scope = NativeScope.unboundedScope()) {
    comparFunc = comparFunc.handoff(scope);
    MemorySegment array = scope.allocateArray(C_INT, new int[] { 0, 9, 3, 4, 6, 5, 1, 8, 2, 7 });
    qsort.invokeExact(array, 10L, 4L, comparFunc);
    int[] sorted = array.toIntArray(); // [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 ]
}
```

Not only native scope helps in making the allocation of the native array simpler (we no longer need to create an heap segment, and dump its contents onto the off-heap array); but we can also use the native scope as an *handoff target* for existing upcall stub segments. When we do that, we obtain a *new* segment, whose temporal bounds are the same as that of the native scope; the old segment will be killed and will no longer be usable. As with all segments returned by native scope, the new segment we get back will be non-closeable — the only way to close it is to close the native scope it belongs to.

In short, native scopes are a good way to manage the lifecycle of multiple, logically-related segments, and, despite their simplicity, they provide a fairly good usability and performance boost.

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

To do this using the foreign function support provided by Panama we would have to build a *specialized* downcall handle for that call shape <a href="#6"><sup>6</sup></a>:

```java
MethodHandle printf = CLinker.getInstance().downcallHandle(
		LibraryLookup.ofDefault().lookup("printf").get(),
        MethodType.methodType(int.class, MemoryAddress.class, int.class, int.class, int.class),
        FunctionDescriptor.of(C_INT, C_POINTER, C_INT, C_INT, C_INT)
);
```

Then we can call the specialized downcall handle as usual:

```java
printf.invoke(CLinker.toCString("%d plus %d equals %d").address(), 2, 2, 4); //prints "2 plus 2 equals 4"
```

While this works, it is easy to see how such an approach is not very desirable:

* If the variadic function needs to be called with many different shapes, we have to create many different downcall handles
* while this approach works for downcalls (since the Java code is in charge of determining which and how many arguments should be passed) it fails to scale to upcalls; in that case, the call comes from native code, so we have no way to guarantee that the shape of the upcall stub we have created will match that required by the native function.

To mitigate these issues, the standard C foreign linker comes equipped with support for C variable argument lists — or `va_list`.  When a variadic function is called, C code has to unpack the variadic arguments by creating a `va_list` structure, and then accessing the variadic arguments through the `va_list` one by one (using the `va_arg` macro). To facilitate interop between standard variadic functions and `va_list` many C library functions in fact define *two* flavors of the same function, one using standard variadic signature, one using an extra `va_list` parameter. For instance, in the case of `printf` we can find that a `va_list`-accepting function performing the same task is also defined:

```c
int vprintf(const char *format, va_list ap);
```

The behavior of this function is the same as before — the only difference is that the ellipsis notation `...` has been replaced with a single `va_list` parameter; in other words, the function is no longer variadic.

It is indeed fairly easy to create a downcall for `vprintf`:

```java
MethodHandle vprintf = CLinker.getInstance().downcallHandle(
		LibraryLookup.ofDefault().lookup("vprintf").get(),
		MethodType.methodType(int.class, MemoryAddress.class, VaList.class),
        FunctionDescriptor.of(C_INT, C_POINTER, C_VA_LIST));
```

Here, the notable thing is that `CLinker` comes equipped with the special `C_VA_LIST` layout (the layout of a `va_list` parameter) as well as a `VaList` carrier, which can be used to construct and represent variable argument lists from Java code.

To call the `vprintf` handle we need to create an instance of `VaList` which contains the arguments we want to pass to the `vprintf` function — we can do so, as follows:

```java
vprintf.invoke(
		CLinker.toCString("%d plus %d equals %d").address(),
        VaList.make(builder ->
                   		builder.vargFromInt(C_INT, 2)
                               .vargFromInt(C_INT, 2)
                               .vargFromInt(C_INT, 4))
); //prints "2 plus 2 equals 4"
```

While the callee has to do more work to call the `vprintf` handle, note that that now we're back in a place where the downcall handle  `vprintf` can be shared across multiple callees. A `VaList` object created this way has its own lifetime (`VaList` also supports a `close` operation), so it is up to the callee to make sure that the `VaList` instance is closed (or attached to some existing native scope) so as to avoid leaks.

Another advantage of using `VaList` is that this approach also scales to upcall stubs — it is therefore possible for clients to create upcalls stubs which take a `VaList` and then, from the Java upcall, read the arguments packed inside the `VaList` one by one using the methods provided by the `VaList` API (e.g. `VaList::vargAsInt(MemoryLayout)`), which mimic the behavior of the C `va_arg` macro.

### Appendix: full source code

The full source code containing most of the code shown throughout this document can be seen below:

```java
import jdk.incubator.foreign.Addressable;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.LibraryLookup;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.NativeScope;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;

import static jdk.incubator.foreign.CLinker.*;

public class Examples {

    public static void main(String[] args) throws Throwable {
        strlen();
        qsort();
        printf();
        vprintf();
    }

    public static void strlen() throws Throwable {
        MethodHandle strlen = CLinker.getInstance().downcallHandle(
                LibraryLookup.ofDefault().lookup("strlen").get(),
                MethodType.methodType(long.class, MemoryAddress.class),
                FunctionDescriptor.of(C_LONG, C_POINTER)
        );

        try (MemorySegment hello = CLinker.toCString("Hello")) {
            long len = (long) strlen.invokeExact(hello.address()); // 5
            System.out.println(len);
        }
    }

    static class Qsort {
        static int qsortCompare(MemoryAddress addr1, MemoryAddress addr2) {
            int v1 = MemoryAccess.getIntAtOffset(MemorySegment.ofNativeRestricted(), addr1.toRawLongValue());
            int v2 = MemoryAccess.getIntAtOffset(MemorySegment.ofNativeRestricted(), addr2.toRawLongValue());
            return v1 - v2;
        }
    }

    public static void qsort() throws Throwable {
        MethodHandle qsort = CLinker.getInstance().downcallHandle(
                LibraryLookup.ofDefault().lookup("qsort").get(),
                MethodType.methodType(void.class, MemoryAddress.class, long.class, long.class, MemoryAddress.class),
                FunctionDescriptor.ofVoid(C_POINTER, C_LONG, C_LONG, C_POINTER)
        );

        MethodHandle comparHandle = MethodHandles.lookup()
                .findStatic(Qsort.class, "qsortCompare",
                        MethodType.methodType(int.class, MemoryAddress.class, MemoryAddress.class));

        MemorySegment comparFunc = CLinker.getInstance().upcallStub(
                comparHandle,
                FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER)
        );

        try (NativeScope scope = NativeScope.unboundedScope()) {
            comparFunc = comparFunc.handoff(scope);
            MemorySegment array = scope.allocateArray(C_INT, new int[] { 0, 9, 3, 4, 6, 5, 1, 8, 2, 7 });
            qsort.invokeExact(array.address(), 10L, 4L, comparFunc.address());
            int[] sorted = array.toIntArray(); // [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 ]
            System.out.println(Arrays.toString(sorted));
        }
    }

    public static void printf() throws Throwable {
        MethodHandle printf = CLinker.getInstance().downcallHandle(
                LibraryLookup.ofDefault().lookup("printf").get(),
                MethodType.methodType(int.class, MemoryAddress.class, int.class, int.class, int.class),
                FunctionDescriptor.of(C_INT, C_POINTER, C_INT, C_INT, C_INT)
        );
        try (MemorySegment s = CLinker.toCString("%d plus %d equals %d\n")) {
            printf.invoke(s.address(), 2, 2, 4);
        }
    }

    public static void vprintf() throws Throwable {

        MethodHandle vprintf = CLinker.getInstance().downcallHandle(
                LibraryLookup.ofDefault().lookup("vprintf").get(),
                MethodType.methodType(int.class, MemoryAddress.class, CLinker.VaList.class),
                FunctionDescriptor.of(C_INT, C_POINTER, C_VA_LIST));

        try (NativeScope scope = NativeScope.unboundedScope()) {
            MemorySegment s = CLinker.toCString("%d plus %d equals %d\n", scope);
            CLinker.VaList vlist = CLinker.VaList.make(builder ->
                     builder.vargFromInt(C_INT, 2)
                            .vargFromInt(C_INT, 2)
                            .vargFromInt(C_INT, 4), scope);
            vprintf.invoke(s.address(), vlist);
        }
    }
}
```







* <a id="1"/>(<sup>1</sup>):<small> In reality this is not entirely new; even in JNI, when you call a `native` method the VM trusts that the corresponding implementing function in C will feature compatible parameter types and return values; if not a crash might occur.</small>
* <a id="2"/>(<sup>2</sup>):<small> In the fututre we might consider knobs to allow structs returned by value to be allocated on-heap rather than off-heap. If these structs are always passed back and forth in an opaque manner, there could be a significant performance advantage in avoiding an off-heap allocation.</small>
* <a id="3a"/>(<sup>3a</sup>):<small> At the time of writing, support for native method intrinsics has been disabled by default due to some spurious VM crash being detected when running `jextract` with the intrinsics support enabled. While we work to rectify this situation, the intrinsics support can still be enabled using the ` -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=true` flag.</small>
* <a id="3b"/>(<sup>3b</sup>):<small> As an advanced option, Panama allows the user to opt-in to remove Java to native thread transitions; while, in the general case it is unsafe doing so (removing thread transitions could have a negative impact on GC for long running native functions, and could crash the VM if the downcall needs to pop back out in Java, e.g. via an upcall), greater efficiency can be achieved; performance sensitive users should consider this option at least for the functions that are called more frquently, assuming that these functions are *leaf* functions (e.g. do not go back to Java via an upcall) and are relatively short-lived.</small>
* <a id="4"/>(<sup>4</sup>):<small> This might change in the future, as we might want to tie the lifecycle of structs created for an upcall to the lifecycle of the upcall itself so that e.g. any segment that are created ahead of calling a Java upcall, are released immediately after the upcall returns</small>
* <a id="5"/>(<sup>5</sup>):<small> We are currently investigating alternate allocation strategies to make allocation inside native scopes even faster</small>
* <a id="6"/>(<sup>6</sup>):<small> On Windows, layouts for variadic arguments have to be adjusted using the `CLinker.Win64.asVarArg(ValueLayout)`; this is necessay because the Windows ABI passes variadic arguments using different rules than the ones used for ordinary arguments.</small>


