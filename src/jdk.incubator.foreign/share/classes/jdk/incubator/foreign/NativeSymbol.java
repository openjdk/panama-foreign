package jdk.incubator.foreign;

import jdk.internal.foreign.NativeSymbolImpl;
import jdk.internal.reflect.CallerSensitive;
import jdk.internal.reflect.Reflection;

import java.lang.invoke.MethodHandle;

/**
 * A native symbol models a reference to a location (typically the entry point of a function) in a native library.
 * A native symbol has a name, and is associated with a scope, which governs the native symbol's lifecycle.
 * This is useful, since the library a native symbol refers to can be <em>unloaded</em>, thus invalidating the native symbol.
 * While native symbols are typically obtained using a {@link SymbolLookup#lookup(String) symbol lookup}, it is also possible to obtain an
 * <em>anonymous</em> native symbol, in the form of an {@linkplain CLinker#upcallStub(MethodHandle, FunctionDescriptor, ResourceScope) upcall stub},
 * that is, a reference to a dynamically-generated native symbol which can be used to call back into Java code.
 */
sealed public interface NativeSymbol extends Addressable permits NativeSymbolImpl {

    /**
     * Returns the name of this symbol.
     * @return the name of this symbol.
     */
    String name();

    /**
     * Returns the resource scope associated with this symbol.
     * @return the resource scope associated with this symbol.
     */
    ResourceScope scope();

    /**
     * Returns the memory address associated with this symbol.
     * @throws IllegalStateException if the scope associated with this symbol has been closed, or if access occurs from
     * a thread other than the thread owning that scope.
     * @return The memory address associated with this symbol.
     */
    @Override
    MemoryAddress address();

    /**
     * Creates a new symbol from given name, address and scope.
     * <p>
     * This method is <a href="package-summary.html#restricted"><em>restricted</em></a>.
     * Restricted methods are unsafe, and, if used incorrectly, their use might crash
     * the JVM or, worse, silently result in memory corruption. Thus, clients should refrain from depending on
     * restricted methods, and use safe and supported functionalities, where possible.
     * @param name the symbol name.
     * @param address the symbol address.
     * @param scope the symbol scope.
     * @return A new symbol from given name, address and scope.
     * @throws IllegalCallerException if access to this method occurs from a module {@code M} and the command line option
     * {@code --enable-native-access} is either absent, or does not mention the module name {@code M}, or
     * {@code ALL-UNNAMED} in case {@code M} is an unnamed module.
     */
    @CallerSensitive
    static NativeSymbol ofAddress(String name, MemoryAddress address, ResourceScope scope) {
        Reflection.ensureNativeAccess(Reflection.getCallerClass());
        return new NativeSymbolImpl(name, address, scope);
    }
}
