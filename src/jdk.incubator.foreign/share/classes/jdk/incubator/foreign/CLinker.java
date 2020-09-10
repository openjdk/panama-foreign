/*
 *  Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.  Oracle designates this
 *  particular file as subject to the "Classpath" exception as provided
 *  by Oracle in the LICENSE file that accompanied this code.
 *
 *  This code is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  version 2 for more details (a copy is included in the LICENSE file that
 *  accompanied this code).
 *
 *  You should have received a copy of the GNU General Public License version
 *  2 along with this work; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *   Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 *
 */
package jdk.incubator.foreign;

import jdk.internal.foreign.NativeMemorySegmentImpl;
import jdk.internal.foreign.PlatformLayouts;
import jdk.internal.foreign.Utils;
import jdk.internal.foreign.abi.SharedUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.function.Consumer;

import static jdk.internal.foreign.PlatformLayouts.*;

/**
 * This interface is a used as a super class for all ForeinLinker implementations that
 * implement the system's C ABI and calling convention. As such, instances of this interface
 * can be used to link foreign functions in native libraries that follow the JVM's target
 * platform C application binary interface (ABI).
 *
 * <p>There are two components that go into linking a foreign function: a method type, and
 * a function descriptor. The method type, consisting of a set of 'carrier' types constitutes
 * the Java side of a call to a foreign function, while the function descriptor constitutes
 * the native side of the call. Memory layout attributes are used in the function descriptor
 * to attach ABI classification meta-data to memory layouts, which are required for linking.
 * Clients of this API should use the prepared memory layout constants found in this interface
 * to create their function descriptor, based on the built-in types provided by the C language.</p>
 * 
 * <p>Implementations of this interface support the following primitive carrier types:
 * {@code byte}, {@code short}, {@code char}, {@code int}, {@code long}, {@code float},
 * and {@code double}, as well as {@link MemoryAddress} for passing pointers, and
 * {@link MemorySegment} for passing structs and unions. Finally, the {@link VaList}
 * carrier type can be used to match the native {@code va_list} type.</p>
 *
 * <p>The function descriptor used in linking contains a memory layout to match each carrier type.
 * There are some restrictions on the carrier type and memory layout combinations that are allowed:</p>
 * <ul>
 *   <li>If a primitve type is used as a carrier type, the corresponding
 *   memory layout must be a {@code ValueLayout}, and the bit size of the layout must match that of the carrier type
 *   (see {@link Integer#SIZE} and similar fields in other primitive wrapper classes).</li>
 *
 *   <li>If the carrier type is {@code MemoryAddress} or {@code VaList}, then the corresponding memory layout must be a
 *   {@code ValueLayout}, and its bit size must match the platform's address size (see {@link MemoryLayouts#ADDRESS}).
 *   For this purpose, {@link CLinker#C_POINTER} can  be used as a memory layout for {@code MemoryAddress} carriers,
 *   and {@link CLinker#C_VA_LIST} can be used for {@code VaList} carriers.</li>
 *
 *   <li>If the carrier type is {@code MemorySegment}, then the corresponding memory layout must be a
 *   {@code GroupLayout}</li>
 * </ul>
 *
 * <p>Variadic functions, declared in C either with a trailing ellipses ({@code ...}) at the end of the formal parameter
 * list or with an empty formal parameter list, are not supported directly. It is not possible to create a method handle
 * that takes a variable number of arguments, and neither is it possible to create an upcall stub wrapping a method
 * handle that accepts a variable number of arguments. However, for down calls only, it is possible to link a native
 * variadic function by using a 'specialized' method type and function descriptor: for each argument that is to be
 * passed as a variadic argument, an explicit carrier type and memory layout must be present in the method type and
 * function descriptor when linking the function. Furthermore, the memory layouts of variadic arguments must
 * have a special vararg attribute. Such memory layouts can be created from an existing layout by calling
 * {@link #asVarArg(MemoryLayout)}</p>
 */
public interface CLinker extends ForeignLinker {

    /**
     * Obtain a linker that uses the de facto C ABI of the current system to do it's linking.
     * <p>
     * This method is <em>restricted</em>. Restricted method are unsafe, and, if used incorrectly, their use might crash
     * the JVM or, worse, silently result in memory corruption. Thus, clients should refrain from depending on
     * restricted methods, and use safe and supported functionalities, where possible.
     * @return a linker for this system.
     * @throws IllegalAccessError if the runtime property {@code foreign.restricted} is not set to either
     * {@code permit}, {@code warn} or {@code debug} (the default value is set to {@code deny}).
     */
    static CLinker getSystemLinker() {
        Utils.checkRestrictedAccess("SystemCLinker.getSystemLinker");
        return SharedUtils.getSystemLinker();
    }

    /**
     * Obtain a method handle which can be used to call a given native function.
     *
     * @param symbol   downcall symbol.
     * @param type     the method type.
     * @param function the function descriptor.
     * @return the downcall method handle.
     * @throws IllegalArgumentException in the case of a carrier type and memory layout mismatch.
     */
    @Override
    MethodHandle downcallHandle(Addressable symbol, MethodType type, FunctionDescriptor function);

    /**
     * Allocates a native stub segment which contains executable code to upcall into a given method handle.
     *
     * <p>The base address of the returned stub segment can be passed to other foreign functions
     * (as a function pointer). The returned segment is <em>not</em> thread-confined, and it only features
     * the {@link MemorySegment#CLOSE} access mode. When the returned segment is closed,
     * the corresponding native stub will be deallocated.</p>
     *
     * <p>The method type of the target method handle is used for linking</p>
     *
     * @param target   the target method handle.
     * @param function the function descriptor.
     * @return the native stub segment.
     * @throws IllegalArgumentException in the case of a carrier type and memory layout mismatch.
     */
    @Override
    MemorySegment upcallStub(MethodHandle target, FunctionDescriptor function);

    /**
     * The {@code _Bool} native type.
     */
    ValueLayout C_BOOL = pick(SysV.C_BOOL, Win64.C_BOOL, AArch64.C_BOOL);
    /**
     * The {@code char} native type.
     */
    ValueLayout C_CHAR = pick(SysV.C_CHAR, Win64.C_CHAR, AArch64.C_CHAR);
    /**
     * The {@code short} native type.
     */
    ValueLayout C_SHORT = pick(SysV.C_SHORT, Win64.C_SHORT, AArch64.C_SHORT);
    /**
     * The {@code int} native type.
     */
    ValueLayout C_INT = pick(SysV.C_INT, Win64.C_INT, AArch64.C_INT);
    /**
     * The {@code long} native type.
     */
    ValueLayout C_LONG = pick(SysV.C_LONG, Win64.C_LONG, AArch64.C_LONG);
    /**
     * The {@code long long} native type.
     */
    ValueLayout C_LONGLONG = pick(SysV.C_LONGLONG, Win64.C_LONGLONG, AArch64.C_LONGLONG);
    /**
     * The {@code float} native type.
     */
    ValueLayout C_FLOAT = pick(SysV.C_FLOAT, Win64.C_FLOAT, AArch64.C_FLOAT);
    /**
     * The {@code double} native type.
     */
    ValueLayout C_DOUBLE = pick(SysV.C_DOUBLE, Win64.C_DOUBLE, AArch64.C_DOUBLE);
    /**
     * The {@code long double} native type.
     */
    ValueLayout C_LONGDOUBLE = pick(SysV.C_LONGDOUBLE, Win64.C_LONGDOUBLE, AArch64.C_LONGDOUBLE);
    /**
     * The {@code T*} native type.
     */
    ValueLayout C_POINTER = pick(SysV.C_POINTER, Win64.C_POINTER, AArch64.C_POINTER);
    /**
     * The {@code va_list} native type.
     */
    MemoryLayout C_VA_LIST = pick(SysV.C_VA_LIST, Win64.C_VA_LIST, AArch64.C_VA_LIST);

    /**
     * Returns a memory layout that is suitable to use the layout for variadic arguments.
     *
     * @param ml the layout the adapt
     * @return a potentially newly created layout with the right attributes
     */
    @SuppressWarnings("unchecked")
    static <T extends MemoryLayout> T asVarArg(T ml) {
        return (T) PlatformLayouts.asVarArg(ml);
    }

        /**
     * Convert a Java string into a null-terminated C string, using the
     * platform's default charset, storing the result into a new native memory segment.
     * <p>
     * This method always replaces malformed-input and unmappable-character
     * sequences with this charset's default replacement byte array.  The
     * {@link java.nio.charset.CharsetEncoder} class should be used when more
     * control over the encoding process is required.
     *
     * @param str the Java string to be converted into a C string.
     * @return a new native memory segment containing the converted C string.
     * @throws NullPointerException if either {@code str == null}.
     */
    public static MemorySegment toCString(String str) {
        Objects.requireNonNull(str);
        return toCString(str.getBytes());
    }

    /**
     * Convert a Java string into a null-terminated C string, using the given {@linkplain java.nio.charset.Charset charset},
     * storing the result into a new native memory segment.
     * <p>
     * This method always replaces malformed-input and unmappable-character
     * sequences with this charset's default replacement byte array.  The
     * {@link java.nio.charset.CharsetEncoder} class should be used when more
     * control over the encoding process is required.
     *
     * @param str the Java string to be converted into a C string.
     * @param charset The {@linkplain java.nio.charset.Charset} to be used to compute the contents of the C string.
     * @return a new native memory segment containing the converted C string.
     * @throws NullPointerException if either {@code str == null} or {@code charset == null}.
     */
    public static MemorySegment toCString(String str, Charset charset) {
        Objects.requireNonNull(str);
        Objects.requireNonNull(charset);
        return toCString(str.getBytes(charset));
    }

    /**
     * Convert a Java string into a null-terminated C string, using the platform's default charset,
     * storing the result into a native memory segment allocated using the provided scope.
     * <p>
     * This method always replaces malformed-input and unmappable-character
     * sequences with this charset's default replacement byte array.  The
     * {@link java.nio.charset.CharsetEncoder} class should be used when more
     * control over the encoding process is required.
     *
     * @param str the Java string to be converted into a C string.
     * @param scope the scope to be used for the native segment allocation.
     * @return a new native memory segment containing the converted C string.
     * @throws NullPointerException if either {@code str == null} or {@code scope == null}.
     */
    public static MemorySegment toCString(String str, NativeScope scope) {
        Objects.requireNonNull(str);
        Objects.requireNonNull(scope);
        return toCString(str.getBytes(), scope);
    }

    /**
     * Convert a Java string into a null-terminated C string, using the given {@linkplain java.nio.charset.Charset charset},
     * storing the result into a new native memory segment native memory segment allocated using the provided scope.
     * <p>
     * This method always replaces malformed-input and unmappable-character
     * sequences with this charset's default replacement byte array.  The
     * {@link java.nio.charset.CharsetEncoder} class should be used when more
     * control over the encoding process is required.
     *
     * @param str the Java string to be converted into a C string.
     * @param charset The {@linkplain java.nio.charset.Charset} to be used to compute the contents of the C string.
     * @param scope the scope to be used for the native segment allocation.
     * @return a new native memory segment containing the converted C string.
     * @throws NullPointerException if either {@code str == null}, {@code charset == null} or {@code scope == null}.
     */
    public static MemorySegment toCString(String str, Charset charset, NativeScope scope) {
        Objects.requireNonNull(str);
        Objects.requireNonNull(charset);
        Objects.requireNonNull(scope);
        return toCString(str.getBytes(charset), scope);
    }

    /**
     * Convert a null-terminated C string stored at given address into a Java string, using the platform's default charset.
     * <p>
     * This method always replaces malformed-input and unmappable-character
     * sequences with this charset's default replacement string.  The {@link
     * java.nio.charset.CharsetDecoder} class should be used when more control
     * over the decoding process is required.
     * <p>
     * This method is <em>restricted</em>. Restricted method are unsafe, and, if used incorrectly, their use might crash
     * the JVM or, worse, silently result in memory corruption. Thus, clients should refrain from depending on
     * restricted methods, and use safe and supported functionalities, where possible.
     * @param addr the address at which the string is stored.
     * @return a Java string with the contents of the null-terminated C string at given address.
     * @throws NullPointerException if {@code addr == null}
     * @throws IllegalArgumentException if the size of the native string is greater than {@code Integer.MAX_VALUE}.
     */
    public static String toJavaStringRestricted(MemoryAddress addr) {
        Utils.checkRestrictedAccess("CLinker.toJavaStringRestricted");
        return SharedUtils.toJavaStringInternal(NativeMemorySegmentImpl.EVERYTHING, addr.toRawLongValue(), Charset.defaultCharset());
    }

    /**
     * Convert a null-terminated C string stored at given address into a Java string, using the given {@linkplain java.nio.charset.Charset charset}.
     * <p>
     * This method always replaces malformed-input and unmappable-character
     * sequences with this charset's default replacement string.  The {@link
     * java.nio.charset.CharsetDecoder} class should be used when more control
     * over the decoding process is required.
     * <p>
     * This method is <em>restricted</em>. Restricted method are unsafe, and, if used incorrectly, their use might crash
     * the JVM or, worse, silently result in memory corruption. Thus, clients should refrain from depending on
     * restricted methods, and use safe and supported functionalities, where possible.
     * @param addr the address at which the string is stored.
     * @param charset The {@linkplain java.nio.charset.Charset} to be used to compute the contents of the Java string.
     * @return a Java string with the contents of the null-terminated C string at given address.
     * @throws NullPointerException if {@code addr == null}
     * @throws IllegalArgumentException if the size of the native string is greater than {@code Integer.MAX_VALUE}.
     */
    public static String toJavaStringRestricted(MemoryAddress addr, Charset charset) {
        Utils.checkRestrictedAccess("CLinker.toJavaStringRestricted");
        return SharedUtils.toJavaStringInternal(NativeMemorySegmentImpl.EVERYTHING, addr.toRawLongValue(), charset);
    }

    /**
     * Convert a null-terminated C string stored at given address into a Java string, using the platform's default charset.
     * <p>
     * This method always replaces malformed-input and unmappable-character
     * sequences with this charset's default replacement string.  The {@link
     * java.nio.charset.CharsetDecoder} class should be used when more control
     * over the decoding process is required.
     * @param addr the address at which the string is stored.
     * @return a Java string with the contents of the null-terminated C string at given address.
     * @throws NullPointerException if {@code addr == null}
     * @throws IllegalArgumentException if the size of the native string is greater than {@code Integer.MAX_VALUE}.
     * @throws IllegalStateException if the size of the native string is greater than the size of the segment
     * associated with {@code addr}, or if {@code addr} is associated with a segment that is <em>not alive</em>.
     */
    public static String toJavaString(MemorySegment addr) {
        return SharedUtils.toJavaStringInternal(addr, 0L, Charset.defaultCharset());
    }

    /**
     * Convert a null-terminated C string stored at given address into a Java string, using the given {@linkplain java.nio.charset.Charset charset}.
     * <p>
     * This method always replaces malformed-input and unmappable-character
     * sequences with this charset's default replacement string.  The {@link
     * java.nio.charset.CharsetDecoder} class should be used when more control
     * over the decoding process is required.
     * @param addr the address at which the string is stored.
     * @param charset The {@linkplain java.nio.charset.Charset} to be used to compute the contents of the Java string.
     * @return a Java string with the contents of the null-terminated C string at given address.
     * @throws NullPointerException if {@code addr == null}
     * @throws IllegalArgumentException if the size of the native string is greater than {@code Integer.MAX_VALUE}.
     * @throws IllegalStateException if the size of the native string is greater than the size of the segment
     * associated with {@code addr}, or if {@code addr} is associated with a segment that is <em>not alive</em>.
     */
    public static String toJavaString(MemorySegment addr, Charset charset) {
        return SharedUtils.toJavaStringInternal(addr, 0L, charset);
    }

    private static void copy(MemorySegment addr, byte[] bytes) {
        var heapSegment = MemorySegment.ofArray(bytes);
        addr.copyFrom(heapSegment);
        MemoryAccess.setByteAtOffset(addr, bytes.length, (byte)0);
    }

    private static MemorySegment toCString(byte[] bytes) {
        MemorySegment segment = MemorySegment.allocateNative(bytes.length + 1, 1L);
        copy(segment, bytes);
        return segment;
    }

    private static MemorySegment toCString(byte[] bytes, NativeScope scope) {
        MemorySegment addr = scope.allocate(bytes.length + 1, 1L);
        copy(addr, bytes);
        return addr;
    }

    /**
     * Allocate memory of given size using malloc.
     * <p>
     * This method is <em>restricted</em>. Restricted method are unsafe, and, if used incorrectly, their use might crash
     * the JVM or, worse, silently result in memory corruption. Thus, clients should refrain from depending on
     * restricted methods, and use safe and supported functionalities, where possible.
     *
     * @param size memory size to be allocated
     * @return addr memory address of the allocated memory
     */
    public static MemoryAddress allocateMemoryRestricted(long size) {
        Utils.checkRestrictedAccess("CLinker.allocateMemoryRestricted");
        return SharedUtils.allocateMemoryInternal(size);
    }

    /**
     * Free the memory pointed by the given memory address.
     * <p>
     * This method is <em>restricted</em>. Restricted method are unsafe, and, if used incorrectly, their use might crash
     * the JVM or, worse, silently result in memory corruption. Thus, clients should refrain from depending on
     * restricted methods, and use safe and supported functionalities, where possible.
     *
     * @param addr memory address of the native memory to be freed
     */
    public static void freeMemoryRestricted(MemoryAddress addr) {
        Utils.checkRestrictedAccess("CLinker.freeMemoryRestricted");
        SharedUtils.freeMemoryInternal(addr);
    }

    /**
     * An interface that models a C {@code va_list}.
     * <p>
     * A va list is a stateful cursor used to iterate over a set of variadic arguments.
     * <p>
     * Per the C specification (see C standard 6.5.2.2 Function calls - item 6),
     * arguments to variadic calls are erased by way of 'default argument promotions',
     * which erases integral types by way of integer promotion (see C standard 6.3.1.1 - item 2),
     * and which erases all {@code float} arguments to {@code double}.
     * <p>
     * As such, this interface only supports reading {@code int}, {@code double},
     * and any other type that fits into a {@code long}.
     */
    interface VaList extends Addressable, AutoCloseable {

        /**
         * Reads the next value as an {@code int} and advances this va list's position.
         *
         * @param layout the layout of the value
         * @return the value read as an {@code int}
         * @throws IllegalStateException if the C {@code va_list} associated with this instance is no longer valid
         * (see {@link #close()}).
         * @throws IllegalArgumentException if the given memory layout is not compatible with {@code int}
         */
        int vargAsInt(MemoryLayout layout);

        /**
         * Reads the next value as a {@code long} and advances this va list's position.
         *
         * @param layout the layout of the value
         * @return the value read as an {@code long}
         * @throws IllegalStateException if the C {@code va_list} associated with this instance is no longer valid
         * (see {@link #close()}).
         * @throws IllegalArgumentException if the given memory layout is not compatible with {@code long}
         */
        long vargAsLong(MemoryLayout layout);

        /**
         * Reads the next value as a {@code double} and advances this va list's position.
         *
         * @param layout the layout of the value
         * @return the value read as an {@code double}
         * @throws IllegalStateException if the C {@code va_list} associated with this instance is no longer valid
         * (see {@link #close()}).
         * @throws IllegalArgumentException if the given memory layout is not compatible with {@code double}
         */
        double vargAsDouble(MemoryLayout layout);

        /**
         * Reads the next value as a {@code MemoryAddress} and advances this va list's position.
         *
         * @param layout the layout of the value
         * @return the value read as an {@code MemoryAddress}
         * @throws IllegalStateException if the C {@code va_list} associated with this instance is no longer valid
         * (see {@link #close()}).
         * @throws IllegalArgumentException if the given memory layout is not compatible with {@code MemoryAddress}
         */
        MemoryAddress vargAsAddress(MemoryLayout layout);

        /**
         * Reads the next value as a {@code MemorySegment}, and advances this va list's position.
         * <p>
         * The memory segment returned by this method will be allocated using
         * {@link MemorySegment#allocateNative(long, long)}, and will have to be closed separately.
         *
         * @param layout the layout of the value
         * @return the value read as an {@code MemorySegment}
         * @throws IllegalStateException if the C {@code va_list} associated with this instance is no longer valid
         * (see {@link #close()}).
         * @throws IllegalArgumentException if the given memory layout is not compatible with {@code MemorySegment}
         */
        MemorySegment vargAsSegment(MemoryLayout layout);

        /**
         * Reads the next value as a {@code MemorySegment}, and advances this va list's position.
         * <p>
         * The memory segment returned by this method will be allocated using the given {@code NativeScope}.
         *
         * @param layout the layout of the value
         * @param scope the scope to allocate the segment in
         * @return the value read as an {@code MemorySegment}
         * @throws IllegalStateException if the C {@code va_list} associated with this instance is no longer valid
         * (see {@link #close()}).
         * @throws IllegalArgumentException if the given memory layout is not compatible with {@code MemorySegment}
         */
        MemorySegment vargAsSegment(MemoryLayout layout, NativeScope scope);

        /**
         * Skips a number of elements with the given memory layouts, and advances this va list's position.
         *
         * @param layouts the layout of the value
         * @throws IllegalStateException if the C {@code va_list} associated with this instance is no longer valid
         * (see {@link #close()}).
         */
        void skip(MemoryLayout... layouts);

        /**
         * A predicate used to check if the memory associated with the C {@code va_list} modelled
         * by this instance is still valid to use.
         *
         * @return true, if the memory associated with the C {@code va_list} modelled by this instance is still valid
         * @see #close()
         */
        boolean isAlive();

        /**
         * Releases the underlying C {@code va_list} modelled by this instance, and any native memory that is attached
         * to this va list that holds its elements (see {@link VaList#make(Consumer)}).
         * <p>
         * After calling this method, {@link #isAlive()} will return {@code false} and further attempts to read values
         * from this va list will result in an exception.
         *
         * @see #isAlive()
         */
        void close();

        /**
         * Copies this C {@code va_list} at its current position. Copying is useful to traverse the va list's elements
         * starting from the current position, without affecting the state of the original va list, essentially
         * allowing the elements to be traversed multiple times.
         * <p>
         * If this method needs to allocate native memory for the copy, it will use
         * {@link MemorySegment#allocateNative(long, long)} to do so. {@link #close()} will have to be called on the
         * returned va list instance to release the allocated memory.
         * <p>
         * This method only copies the va list cursor itself and not the memory that may be attached to the
         * va list which holds its elements. That means that if this va list was created with the
         * {@link #make(Consumer)} method, closing this va list will also release the native memory that holds its
         * elements, making the copy unusable.
         *
         * @return a copy of this C {@code va_list}.
         * @throws IllegalStateException if the C {@code va_list} associated with this instance is no longer valid
         * (see {@link #close()}).
         */
        VaList copy();

        /**
         * Copies this C {@code va_list} at its current position. Copying is useful to traverse the va list's elements
         * starting from the current position, without affecting the state of the original va list, essentially
         * allowing the elements to be traversed multiple times.
         * <p>
         * If this method needs to allocate native memory for the copy, it will use
         * the given {@code NativeScope} to do so.
         * <p>
         * This method only copies the va list cursor itself and not the memory that may be attached to the
         * va list which holds its elements. That means that if this va list was created with the
         * {@link #make(Consumer)} method, closing this va list will also release the native memory that holds its
         * elements, making the copy unusable.
         *
         * @param scope the scope to allocate the copy in
         * @return a copy of this C {@code va_list}.
         * @throws IllegalStateException if the C {@code va_list} associated with this instance is no longer valid
         * (see {@link #close()}).
         */
        VaList copy(NativeScope scope);

        /**
         * Returns the memory address of the C {@code va_list} associated with this instance.
         *
         * @return the memory address of the C {@code va_list} associated with this instance.
         */
        @Override
        MemoryAddress address();

        /**
         * Constructs a new {@code VaList} instance out of a memory address pointing to an existing C {@code va_list}.
         * <p>
         * This method is <em>restricted</em>. Restricted method are unsafe, and, if used incorrectly, their use might crash
         * the JVM or, worse, silently result in memory corruption. Thus, clients should refrain from depending on
         * restricted methods, and use safe and supported functionalities, where possible.
         *
         * @param address a memory address pointing to an existing C {@code va_list}.
         * @return a new {@code VaList} instance backed by the C {@code va_list} at {@code address}.
         */
        static VaList ofAddressRestricted(MemoryAddress address) {
            Utils.checkRestrictedAccess("VaList.ofAddressRestricted");
            return SharedUtils.newVaListOfAddress(address);
        }

        /**
         * Constructs a new {@code VaList} using a builder (see {@link Builder}).
         * <p>
         * If this method needs to allocate native memory for the va list, it will use
         * {@link MemorySegment#allocateNative(long, long)} to do so.
         * <p>
         * This method will allocate native memory to hold the elements in the va list. This memory
         * will be 'attached' to the returned va list instance, and will be released when {@link VaList#close()}
         * is called.
         * <p>
         * Note that when there are no elements added to the created va list,
         * this method will return the same as {@linkplain #empty()}.
         *
         * @param actions a consumer for a builder (see {@link Builder}) which can be used to specify the elements
         *                of the underlying C {@code va_list}.
         * @return a new {@code VaList} instance backed by a fresh C {@code va_list}.
         */
        static VaList make(Consumer<Builder> actions) {
            return SharedUtils.newVaList(actions, MemorySegment::allocateNative);
        }

        /**
         * Constructs a new {@code VaList} using a builder (see {@link Builder}).
         * <p>
         * If this method needs to allocate native memory for the va list, it will use
         * the given {@code NativeScope} to do so.
         * <p>
         * This method will allocate native memory to hold the elements in the va list. This memory
         * will be managed by the given {@code NativeScope}, and will be released when the scope is closed.
         * <p>
         * Note that when there are no elements added to the created va list,
         * this method will return the same as {@linkplain #empty()}.
         *
         * @param actions a consumer for a builder (see {@link Builder}) which can be used to specify the elements
         *                of the underlying C {@code va_list}.
         * @param scope the scope to be used for the valist allocation.
         * @return a new {@code VaList} instance backed by a fresh C {@code va_list}.
         */
        static VaList make(Consumer<Builder> actions, NativeScope scope) {
            return SharedUtils.newVaList(actions, SharedUtils.Allocator.ofScope(scope));
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
         */
        interface Builder {

            /**
             * Adds a native value represented as an {@code int} to the C {@code va_list} being constructed.
             *
             * @param layout the native layout of the value.
             * @param value the value, represented as an {@code int}.
             * @return this builder.
             * @throws IllegalArgumentException if the given memory layout is not compatible with {@code int}
             */
            Builder vargFromInt(ValueLayout layout, int value);

            /**
             * Adds a native value represented as a {@code long} to the C {@code va_list} being constructed.
             *
             * @param layout the native layout of the value.
             * @param value the value, represented as a {@code long}.
             * @return this builder.
             * @throws IllegalArgumentException if the given memory layout is not compatible with {@code long}
             */
            Builder vargFromLong(ValueLayout layout, long value);

            /**
             * Adds a native value represented as a {@code double} to the C {@code va_list} being constructed.
             *
             * @param layout the native layout of the value.
             * @param value the value, represented as a {@code double}.
             * @return this builder.
             * @throws IllegalArgumentException if the given memory layout is not compatible with {@code double}
             */
            Builder vargFromDouble(ValueLayout layout, double value);

            /**
             * Adds a native value represented as a {@code MemoryAddress} to the C {@code va_list} being constructed.
             *
             * @param layout the native layout of the value.
             * @param value the value, represented as a {@code Addressable}.
             * @return this builder.
             * @throws IllegalArgumentException if the given memory layout is not compatible with {@code MemoryAddress}
             */
            Builder vargFromAddress(ValueLayout layout, Addressable value);

            /**
             * Adds a native value represented as a {@code MemorySegment} to the C {@code va_list} being constructed.
             *
             * @param layout the native layout of the value.
             * @param value the value, represented as a {@code MemorySegment}.
             * @return this builder.
             * @throws IllegalArgumentException if the given memory layout is not compatible with {@code MemorySegment}
             */
            Builder vargFromSegment(GroupLayout layout, MemorySegment value);
        }
    }

}
