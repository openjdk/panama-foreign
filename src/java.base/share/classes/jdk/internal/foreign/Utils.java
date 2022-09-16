/*
 *  Copyright (c) 2019, 2022, Oracle and/or its affiliates. All rights reserved.
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

package jdk.internal.foreign;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Array;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import jdk.internal.access.SharedSecrets;
import jdk.internal.misc.ScopedMemoryAccess;
import jdk.internal.misc.Unsafe;
import jdk.internal.vm.annotation.ForceInline;
import static java.lang.foreign.ValueLayout.JAVA_BYTE;

/**
 * This class contains misc helper functions to support creation of memory segments.
 */
public final class Utils {
    public static final MethodHandle MH_BITS_TO_BYTES_OR_THROW_FOR_OFFSET;
    public static final Supplier<RuntimeException> BITS_TO_BYTES_THROW_OFFSET
            = () -> new UnsupportedOperationException("Cannot compute byte offset; bit offset is not a multiple of 8");
    private static final MethodHandle BYTE_TO_BOOL;
    private static final MethodHandle BOOL_TO_BYTE;
    private static final MethodHandle ADDRESS_TO_LONG;
    private static final MethodHandle LONG_TO_ADDRESS_SAFE;
    private static final MethodHandle LONG_TO_ADDRESS_UNSAFE;

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            BYTE_TO_BOOL = lookup.findStatic(Utils.class, "byteToBoolean",
                    MethodType.methodType(boolean.class, byte.class));
            BOOL_TO_BYTE = lookup.findStatic(Utils.class, "booleanToByte",
                    MethodType.methodType(byte.class, boolean.class));
            ADDRESS_TO_LONG = lookup.findVirtual(MemorySegment.class, "address",
                    MethodType.methodType(long.class));
            LONG_TO_ADDRESS_SAFE = lookup.findStatic(Utils.class, "longToAddressSafe",
                    MethodType.methodType(MemorySegment.class, long.class));
            LONG_TO_ADDRESS_UNSAFE = lookup.findStatic(Utils.class, "longToAddressUnsafe",
                    MethodType.methodType(MemorySegment.class, long.class));
            MH_BITS_TO_BYTES_OR_THROW_FOR_OFFSET = MethodHandles.insertArguments(
                    lookup.findStatic(Utils.class, "bitsToBytesOrThrow",
                            MethodType.methodType(long.class, long.class, Supplier.class)),
                    1,
                    BITS_TO_BYTES_THROW_OFFSET);
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private Utils() {
    }

    public static MemorySegment alignUp(MemorySegment ms, long alignment) {
        long offset = ms.address();
        return ms.asSlice(alignUp(offset, alignment) - offset);
    }

    public static long alignUp(long n, long alignment) {
        return (n + alignment - 1) & -alignment;
    }

    public static long bitsToBytesOrThrow(long bits, Supplier<RuntimeException> exFactory) {
        if (Utils.isAligned(bits, 8)) {
            return bits / 8;
        } else {
            throw exFactory.get();
        }
    }

    @ForceInline
    public static boolean isAligned(long offset, long align) {
        return (offset & (align - 1)) == 0;
    }

    public static VarHandle makeSegmentViewVarHandle(ValueLayout layout) {
        class VarHandleCache {
            private static final Map<ValueLayout, VarHandle> handleMap = new ConcurrentHashMap<>();

            static VarHandle put(ValueLayout layout, VarHandle handle) {
                VarHandle prev = handleMap.putIfAbsent(layout, handle);
                return prev != null ? prev : handle;
            }
        }
        Class<?> baseCarrier = layout.carrier();
        if (layout.carrier() == MemorySegment.class) {
            baseCarrier = switch ((int) ValueLayout.ADDRESS.byteSize()) {
                case 8 -> long.class;
                case 4 -> int.class;
                default -> throw new UnsupportedOperationException("Unsupported address layout");
            };
        } else if (layout.carrier() == boolean.class) {
            baseCarrier = byte.class;
        }

        VarHandle handle = SharedSecrets.getJavaLangInvokeAccess().memorySegmentViewHandle(baseCarrier,
                layout.byteAlignment() - 1, layout.order());

        if (layout.carrier() == boolean.class) {
            handle = MethodHandles.filterValue(handle, BOOL_TO_BYTE, BYTE_TO_BOOL);
        } else if (layout instanceof ValueLayout.OfAddress addressLayout) {
            handle = MethodHandles.filterValue(handle,
                    MethodHandles.explicitCastArguments(ADDRESS_TO_LONG, MethodType.methodType(baseCarrier, MemorySegment.class)),
                    MethodHandles.explicitCastArguments(addressLayout.isUnbounded() ?
                            LONG_TO_ADDRESS_UNSAFE : LONG_TO_ADDRESS_SAFE, MethodType.methodType(MemorySegment.class, baseCarrier)));
        }
        return VarHandleCache.put(layout, handle);
    }

    public static MemorySegment toCString(byte[] bytes, SegmentAllocator allocator) {
        MemorySegment addr = allocator.allocate(bytes.length + 1);
        copy(addr, bytes);
        return addr;
    }

    public static void copy(MemorySegment addr, byte[] bytes) {
        var heapSegment = MemorySegment.ofArray(bytes);
        addr.copyFrom(heapSegment);
        addr.set(JAVA_BYTE, bytes.length, (byte)0);
    }

    @ForceInline
    public static void checkElementAlignment(MemoryLayout layout, String msg) {
        if (layout.bitAlignment() > layout.bitSize()) {
            throw new IllegalArgumentException(msg);
        }
    }

    public static long pointeeSize(MemoryLayout layout) {
        if (layout instanceof ValueLayout.OfAddress addressLayout) {
            return addressLayout.isUnbounded() ? Long.MAX_VALUE : 0L;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public static void checkAllocationSizeAndAlign(long byteSize, long byteAlignment) {
        // size should be >= 0
        if (byteSize < 0) {
            throw new IllegalArgumentException("Invalid allocation size : " + byteSize);
        }

        // alignment should be > 0, and power of two
        if (byteAlignment <= 0 ||
                ((byteAlignment & (byteAlignment - 1)) != 0L)) {
            throw new IllegalArgumentException("Invalid alignment constraint : " + byteAlignment);
        }
    }

    public static boolean byteToBoolean(byte b) {

        return b != 0;
    }

    private static byte booleanToByte(boolean b) {
        return b ? (byte)1 : (byte)0;
    }

    @ForceInline
    private static MemorySegment longToAddressSafe(long addr) {
        return NativeMemorySegmentImpl.makeNativeSegmentUnchecked(addr, 0L);
    }

    @ForceInline
    private static MemorySegment longToAddressUnsafe(long addr) {
        return NativeMemorySegmentImpl.makeNativeSegmentUnchecked(addr, Long.MAX_VALUE);
    }

    public static <Z> Z computePathOp(LayoutPath path,
                                      Function<LayoutPath, Z> finalizer,
                                      Set<LayoutPath.PathElementImpl.PathKind> badKinds,
                                      MemoryLayout.PathElement... elements) {
        Objects.requireNonNull(elements);
        for (MemoryLayout.PathElement e : elements) {
            LayoutPath.PathElementImpl pathElem = (LayoutPath.PathElementImpl)Objects.requireNonNull(e);
            if (badKinds.contains(pathElem.kind())) {
                throw new IllegalArgumentException(String.format("Invalid %s selection in layout path", pathElem.kind().description()));
            }
            path = pathElem.apply(path);
        }
        return finalizer.apply(path);
    }

    @ForceInline
    public static void copy(MemorySegment srcSegment,
                            ValueLayout srcElementLayout,
                            long srcOffset,
                            MemorySegment dstSegment,
                            ValueLayout dstElementLayout,
                            long dstOffset,
                            long elementCount) {

        AbstractMemorySegmentImpl srcImpl = (AbstractMemorySegmentImpl)srcSegment;
        AbstractMemorySegmentImpl dstImpl = (AbstractMemorySegmentImpl)dstSegment;
        if (srcElementLayout.byteSize() != dstElementLayout.byteSize()) {
            throw new IllegalArgumentException("Source and destination layouts must have same size");
        }
        Utils.checkElementAlignment(srcElementLayout, "Source layout alignment greater than its size");
        Utils.checkElementAlignment(dstElementLayout, "Destination layout alignment greater than its size");
        if (!srcImpl.isAlignedForElement(srcOffset, srcElementLayout)) {
            throw new IllegalArgumentException("Source segment incompatible with alignment constraints");
        }
        if (!dstImpl.isAlignedForElement(dstOffset, dstElementLayout)) {
            throw new IllegalArgumentException("Destination segment incompatible with alignment constraints");
        }
        long size = elementCount * srcElementLayout.byteSize();
        srcImpl.checkAccess(srcOffset, size, true);
        dstImpl.checkAccess(dstOffset, size, false);
        if (srcElementLayout.byteSize() == 1 || srcElementLayout.order() == dstElementLayout.order()) {
            ScopedMemoryAccess.getScopedMemoryAccess().copyMemory(srcImpl.sessionImpl(), dstImpl.sessionImpl(),
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffset,
                    dstImpl.unsafeGetBase(), dstImpl.unsafeGetOffset() + dstOffset, size);
        } else {
            ScopedMemoryAccess.getScopedMemoryAccess().copySwapMemory(srcImpl.sessionImpl(), dstImpl.sessionImpl(),
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffset,
                    dstImpl.unsafeGetBase(), dstImpl.unsafeGetOffset() + dstOffset, size, srcElementLayout.byteSize());
        }
    }

    @ForceInline
    public static void copy(MemorySegment srcSegment,
                            ValueLayout srcLayout,
                            long srcOffset,
                            Object dstArray,
                            int dstIndex,
                            int elementCount) {

        long baseAndScale = getBaseAndScale(dstArray.getClass());
        if (dstArray.getClass().componentType() != srcLayout.carrier()) {
            throw new IllegalArgumentException("Incompatible value layout: " + srcLayout);
        }
        int dstBase = (int)baseAndScale;
        long dstWidth = (int)(baseAndScale >> 32); // Use long arithmetics below
        AbstractMemorySegmentImpl srcImpl = (AbstractMemorySegmentImpl)srcSegment;
        Utils.checkElementAlignment(srcLayout, "Source layout alignment greater than its size");
        if (!srcImpl.isAlignedForElement(srcOffset, srcLayout)) {
            throw new IllegalArgumentException("Source segment incompatible with alignment constraints");
        }
        srcImpl.checkAccess(srcOffset, elementCount * dstWidth, true);
        Objects.checkFromIndexSize(dstIndex, elementCount, Array.getLength(dstArray));
        if (dstWidth == 1 || srcLayout.order() == ByteOrder.nativeOrder()) {
            ScopedMemoryAccess.getScopedMemoryAccess().copyMemory(srcImpl.sessionImpl(), null,
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffset,
                    dstArray, dstBase + (dstIndex * dstWidth), elementCount * dstWidth);
        } else {
            ScopedMemoryAccess.getScopedMemoryAccess().copySwapMemory(srcImpl.sessionImpl(), null,
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffset,
                    dstArray, dstBase + (dstIndex * dstWidth), elementCount * dstWidth, dstWidth);
        }
    }

    @ForceInline
    public static void copy(Object srcArray,
                            int srcIndex,
                            MemorySegment dstSegment,
                            ValueLayout dstLayout,
                            long dstOffset,
                            int elementCount) {

        long baseAndScale = getBaseAndScale(srcArray.getClass());
        if (srcArray.getClass().componentType() != dstLayout.carrier()) {
            throw new IllegalArgumentException("Incompatible value layout: " + dstLayout);
        }
        int srcBase = (int)baseAndScale;
        long srcWidth = (int)(baseAndScale >> 32); // Use long arithmetics below
        Objects.checkFromIndexSize(srcIndex, elementCount, Array.getLength(srcArray));
        AbstractMemorySegmentImpl destImpl = (AbstractMemorySegmentImpl)dstSegment;
        Utils.checkElementAlignment(dstLayout, "Destination layout alignment greater than its size");
        if (!destImpl.isAlignedForElement(dstOffset, dstLayout)) {
            throw new IllegalArgumentException("Destination segment incompatible with alignment constraints");
        }
        destImpl.checkAccess(dstOffset, elementCount * srcWidth, false);
        if (srcWidth == 1 || dstLayout.order() == ByteOrder.nativeOrder()) {
            ScopedMemoryAccess.getScopedMemoryAccess().copyMemory(null, destImpl.sessionImpl(),
                    srcArray, srcBase + (srcIndex * srcWidth),
                    destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffset, elementCount * srcWidth);
        } else {
            ScopedMemoryAccess.getScopedMemoryAccess().copySwapMemory(null, destImpl.sessionImpl(),
                    srcArray, srcBase + (srcIndex * srcWidth),
                    destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffset, elementCount * srcWidth, srcWidth);
        }
    }

    private static long getBaseAndScale(Class<?> arrayType) {
        if (arrayType.equals(byte[].class)) {
            return (long) Unsafe.ARRAY_BYTE_BASE_OFFSET | ((long)Unsafe.ARRAY_BYTE_INDEX_SCALE << 32);
        } else if (arrayType.equals(char[].class)) {
            return (long)Unsafe.ARRAY_CHAR_BASE_OFFSET | ((long)Unsafe.ARRAY_CHAR_INDEX_SCALE << 32);
        } else if (arrayType.equals(short[].class)) {
            return (long)Unsafe.ARRAY_SHORT_BASE_OFFSET | ((long)Unsafe.ARRAY_SHORT_INDEX_SCALE << 32);
        } else if (arrayType.equals(int[].class)) {
            return (long)Unsafe.ARRAY_INT_BASE_OFFSET | ((long) Unsafe.ARRAY_INT_INDEX_SCALE << 32);
        } else if (arrayType.equals(float[].class)) {
            return (long)Unsafe.ARRAY_FLOAT_BASE_OFFSET | ((long)Unsafe.ARRAY_FLOAT_INDEX_SCALE << 32);
        } else if (arrayType.equals(long[].class)) {
            return (long)Unsafe.ARRAY_LONG_BASE_OFFSET | ((long)Unsafe.ARRAY_LONG_INDEX_SCALE << 32);
        } else if (arrayType.equals(double[].class)) {
            return (long)Unsafe.ARRAY_DOUBLE_BASE_OFFSET | ((long)Unsafe.ARRAY_DOUBLE_INDEX_SCALE << 32);
        } else {
            throw new IllegalArgumentException("Not a supported array class: " + arrayType.getSimpleName());
        }
    }

    public static long mismatch(MemorySegment srcSegment,
                                long srcFromOffset,
                                long srcToOffset,
                                MemorySegment dstSegment,
                                long dstFromOffset,
                                long dstToOffset) {

        AbstractMemorySegmentImpl srcImpl = (AbstractMemorySegmentImpl)Objects.requireNonNull(srcSegment);
        AbstractMemorySegmentImpl dstImpl = (AbstractMemorySegmentImpl)Objects.requireNonNull(dstSegment);
        long srcBytes = srcToOffset - srcFromOffset;
        long dstBytes = dstToOffset - dstFromOffset;
        srcImpl.checkAccess(srcFromOffset, srcBytes, true);
        dstImpl.checkAccess(dstFromOffset, dstBytes, true);
        if (dstImpl == srcImpl) {
            srcImpl.checkValidState();
            return -1;
        }

        long bytes = Math.min(srcBytes, dstBytes);
        long i = 0;
        if (bytes > 7) {
            if (srcImpl.get(JAVA_BYTE, srcFromOffset) != dstImpl.get(JAVA_BYTE, dstFromOffset)) {
                return 0;
            }
            i = AbstractMemorySegmentImpl.vectorizedMismatchLargeForBytes(srcImpl.sessionImpl(), dstImpl.sessionImpl(),
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcFromOffset,
                    dstImpl.unsafeGetBase(), dstImpl.unsafeGetOffset() + dstFromOffset,
                    bytes);
            if (i >= 0) {
                return i;
            }
            long remaining = ~i;
            assert remaining < 8 : "remaining greater than 7: " + remaining;
            i = bytes - remaining;
        }
        for (; i < bytes; i++) {
            if (srcImpl.get(JAVA_BYTE, srcFromOffset + i) != dstImpl.get(JAVA_BYTE, dstFromOffset + i)) {
                return i;
            }
        }
        return srcBytes != dstBytes ? bytes : -1;
    }

}
