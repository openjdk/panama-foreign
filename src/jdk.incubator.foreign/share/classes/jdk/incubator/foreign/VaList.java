package jdk.incubator.foreign;

import jdk.internal.foreign.abi.SharedUtils;
import jdk.internal.foreign.abi.aarch64.linux.LinuxAArch64VaList;
import jdk.internal.foreign.abi.aarch64.macos.MacOsAArch64VaList;
import jdk.internal.foreign.abi.x64.sysv.SysVVaList;
import jdk.internal.foreign.abi.x64.windows.WinVaList;
import jdk.internal.reflect.CallerSensitive;
import jdk.internal.reflect.Reflection;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * An interface that models a C {@code va_list}.
 * <p>
 * A va list is a stateful cursor used to iterate over a set of variadic arguments. A va list can be passed
 * by reference e.g. to a downcall method handle.
 * <p>
 * Per the C specification (see C standard 6.5.2.2 Function calls - item 6),
 * arguments to variadic calls are erased by way of 'default argument promotions',
 * which erases integral types by way of integer promotion (see C standard 6.3.1.1 - item 2),
 * and which erases all {@code float} arguments to {@code double}.
 * <p>
 * As such, this interface only supports reading {@code int}, {@code double},
 * and any other type that fits into a {@code long}.
 *
 * <p> Unless otherwise specified, passing a {@code null} argument, or an array argument containing one or more {@code null}
 * elements to a method in this class causes a {@link NullPointerException NullPointerException} to be thrown. </p>
 */
sealed public interface VaList extends Addressable permits WinVaList, SysVVaList, LinuxAArch64VaList, MacOsAArch64VaList, SharedUtils.EmptyVaList {

    /**
     * Reads the next value as an {@code int} and advances this va list's position.
     *
     * @param layout the layout of the value
     * @return the value read as an {@code int}
     * @throws IllegalStateException if the resource scope associated with this instance has been closed
     * (see {@link #scope()}).
     */
    int nextVarg(ValueLayout.OfInt layout);

    /**
     * Reads the next value as a {@code long} and advances this va list's position.
     *
     * @param layout the layout of the value
     * @return the value read as an {@code long}
     * @throws IllegalStateException if the resource scope associated with this instance has been closed
     * (see {@link #scope()}).
     */
    long nextVarg(ValueLayout.OfLong layout);

    /**
     * Reads the next value as a {@code double} and advances this va list's position.
     *
     * @param layout the layout of the value
     * @return the value read as an {@code double}
     * @throws IllegalStateException if the resource scope associated with this instance has been closed
     * (see {@link #scope()}).
     */
    double nextVarg(ValueLayout.OfDouble layout);

    /**
     * Reads the next value as a {@code MemoryAddress} and advances this va list's position.
     *
     * @param layout the layout of the value
     * @return the value read as an {@code MemoryAddress}
     * @throws IllegalStateException if the resource scope associated with this instance has been closed
     * (see {@link #scope()}).
     */
    MemoryAddress nextVarg(ValueLayout.OfAddress layout);

    /**
     * Reads the next value as a {@code MemorySegment}, and advances this va list's position.
     * <p>
     * The memory segment returned by this method will be allocated using the given {@link SegmentAllocator}.
     *
     * @param layout the layout of the value
     * @param allocator the allocator to be used for the native segment allocation
     * @return the value read as an {@code MemorySegment}
     * @throws IllegalStateException if the resource scope associated with this instance has been closed
     * (see {@link #scope()}).
     */
    MemorySegment nextVarg(GroupLayout layout, SegmentAllocator allocator);

    /**
     * Skips a number of elements with the given memory layouts, and advances this va list's position.
     *
     * @param layouts the layout of the value
     * @throws IllegalStateException if the resource scope associated with this instance has been closed
     * (see {@link #scope()}).
     */
    void skip(MemoryLayout... layouts);

    /**
     * Returns the resource scope associated with this instance.
     * @return the resource scope associated with this instance.
     */
    ResourceScope scope();

    /**
     * Copies this C {@code va_list} at its current position. Copying is useful to traverse the va list's elements
     * starting from the current position, without affecting the state of the original va list, essentially
     * allowing the elements to be traversed multiple times.
     * <p>
     * Any native resource required by the execution of this method will be allocated in the resource scope
     * associated with this instance (see {@link #scope()}).
     * <p>
     * This method only copies the va list cursor itself and not the memory that may be attached to the
     * va list which holds its elements. That means that if this va list was created with the
     * {@link #make(Consumer, ResourceScope)} method, closing this va list will also release the native memory that holds its
     * elements, making the copy unusable.
     *
     * @return a copy of this C {@code va_list}.
     * @throws IllegalStateException if the resource scope associated with this instance has been closed
     * (see {@link #scope()}).
     */
    VaList copy();

    /**
     * Constructs a new {@code VaList} instance out of a memory address pointing to an existing C {@code va_list}.
     * <p>
     * This method is <a href="package-summary.html#restricted"><em>restricted</em></a>.
     * Restricted methods are unsafe, and, if used incorrectly, their use might crash
     * the JVM or, worse, silently result in memory corruption. Thus, clients should refrain from depending on
     * restricted methods, and use safe and supported functionalities, where possible.
     *
     * @param address a memory address pointing to an existing C {@code va_list}.
     * @return a new {@code VaList} instance backed by the C {@code va_list} at {@code address}.
     * @throws IllegalCallerException if access to this method occurs from a module {@code M} and the command line option
     * {@code --enable-native-access} is either absent, or does not mention the module name {@code M}, or
     * {@code ALL-UNNAMED} in case {@code M} is an unnamed module.
     */
    @CallerSensitive
    static VaList ofAddress(MemoryAddress address) {
        Reflection.ensureNativeAccess(Reflection.getCallerClass());
        return SharedUtils.newVaListOfAddress(address, ResourceScope.globalScope());
    }

    /**
     * Constructs a new {@code VaList} instance out of a memory address pointing to an existing C {@code va_list},
     * with given resource scope.
     * <p>
     * This method is <a href="package-summary.html#restricted"><em>restricted</em></a>.
     * Restricted methods are unsafe, and, if used incorrectly, their use might crash
     * the JVM or, worse, silently result in memory corruption. Thus, clients should refrain from depending on
     * restricted methods, and use safe and supported functionalities, where possible.
     *
     * @param address a memory address pointing to an existing C {@code va_list}.
     * @param scope the resource scope to be associated with the returned {@code VaList} instance.
     * @return a new {@code VaList} instance backed by the C {@code va_list} at {@code address}.
     * @throws IllegalStateException if {@code scope} has been already closed, or if access occurs from a thread other
     * than the thread owning {@code scope}.
     * @throws IllegalCallerException if access to this method occurs from a module {@code M} and the command line option
     * {@code --enable-native-access} is either absent, or does not mention the module name {@code M}, or
     * {@code ALL-UNNAMED} in case {@code M} is an unnamed module.
     */
    @CallerSensitive
    static VaList ofAddress(MemoryAddress address, ResourceScope scope) {
        Reflection.ensureNativeAccess(Reflection.getCallerClass());
        Objects.requireNonNull(address);
        Objects.requireNonNull(scope);
        return SharedUtils.newVaListOfAddress(address, scope);
    }

    /**
     * Constructs a new {@code VaList} using a builder (see {@link Builder}), associated with a given
     * {@linkplain ResourceScope resource scope}.
     * <p>
     * If this method needs to allocate native memory, such memory will be managed by the given
     * {@linkplain ResourceScope resource scope}, and will be released when the resource scope is {@linkplain ResourceScope#close closed}.
     * <p>
     * Note that when there are no elements added to the created va list,
     * this method will return the same as {@link #empty()}.
     *
     * @param actions a consumer for a builder (see {@link Builder}) which can be used to specify the elements
     *                of the underlying C {@code va_list}.
     * @param scope the scope to be used for the valist allocation.
     * @return a new {@code VaList} instance backed by a fresh C {@code va_list}.
     * @throws IllegalStateException if {@code scope} has been already closed, or if access occurs from a thread other
     * than the thread owning {@code scope}.
     */
    static VaList make(Consumer<Builder> actions, ResourceScope scope) {
        Objects.requireNonNull(actions);
        Objects.requireNonNull(scope);
        return SharedUtils.newVaList(actions, scope);
    }

    /**
     * Returns an empty C {@code va_list} constant.
     * <p>
     * The returned {@code VaList} can not be closed.
     *
     * @return a {@code VaList} modelling an empty C {@code va_list}.
     */
    static VaList empty() {
        return SharedUtils.emptyVaList();
    }

    /**
     * A builder interface used to construct a C {@code va_list}.
     *
     * <p> Unless otherwise specified, passing a {@code null} argument, or an array argument containing one or more {@code null}
     * elements to a method in this class causes a {@link NullPointerException NullPointerException} to be thrown. </p>
     */
    sealed interface Builder permits WinVaList.Builder, SysVVaList.Builder, LinuxAArch64VaList.Builder, MacOsAArch64VaList.Builder {

        /**
         * Adds a native value represented as an {@code int} to the C {@code va_list} being constructed.
         *
         * @param layout the native layout of the value.
         * @param value the value, represented as an {@code int}.
         * @return this builder.
         */
        Builder addVarg(ValueLayout.OfInt layout, int value);

        /**
         * Adds a native value represented as a {@code long} to the C {@code va_list} being constructed.
         *
         * @param layout the native layout of the value.
         * @param value the value, represented as a {@code long}.
         * @return this builder.
         */
        Builder addVarg(ValueLayout.OfLong layout, long value);

        /**
         * Adds a native value represented as a {@code double} to the C {@code va_list} being constructed.
         *
         * @param layout the native layout of the value.
         * @param value the value, represented as a {@code double}.
         * @return this builder.
         */
        Builder addVarg(ValueLayout.OfDouble layout, double value);

        /**
         * Adds a native value represented as a {@code MemoryAddress} to the C {@code va_list} being constructed.
         *
         * @param layout the native layout of the value.
         * @param value the value, represented as a {@code Addressable}.
         * @return this builder.
         */
        Builder addVarg(ValueLayout.OfAddress layout, Addressable value);

        /**
         * Adds a native value represented as a {@code MemorySegment} to the C {@code va_list} being constructed.
         *
         * @param layout the native layout of the value.
         * @param value the value, represented as a {@code MemorySegment}.
         * @return this builder.
         */
        Builder addVarg(GroupLayout layout, MemorySegment value);
    }
}
