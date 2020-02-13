/*
 *  Copyright (c) 2019, 2020, Oracle and/or its affiliates. All rights reserved.
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

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemoryLayouts;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.SystemABI;
import jdk.incubator.foreign.ValueLayout;
import jdk.internal.access.JavaLangInvokeAccess;
import jdk.internal.access.JavaNioAccess;
import jdk.internal.access.SharedSecrets;
import jdk.internal.access.foreign.MemoryAddressProxy;
import jdk.internal.access.foreign.UnmapperProxy;
import jdk.internal.foreign.abi.aarch64.AArch64ABI;
import jdk.internal.foreign.abi.x64.sysv.SysVx64ABI;
import jdk.internal.foreign.abi.x64.windows.Windowsx64ABI;
import jdk.internal.misc.Unsafe;
import sun.invoke.util.Wrapper;
import sun.nio.ch.FileChannelImpl;
import sun.security.action.GetBooleanAction;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.constant.Constable;
import java.lang.reflect.Field;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.function.Supplier;

/**
 * This class contains misc helper functions to support creation of memory segments.
 */
public final class Utils {

    private static Unsafe unsafe = Unsafe.getUnsafe();

    private static final MethodHandle ADDRESS_FILTER;
    private static final MethodHandle LONG_TO_ADDRESS;
    private static final MethodHandle ADDRESS_TO_LONG;

    static {
        try {
            ADDRESS_FILTER = MethodHandles.lookup().findStatic(Utils.class, "filterAddress",
                    MethodType.methodType(MemoryAddressProxy.class, MemoryAddress.class));
            LONG_TO_ADDRESS = MethodHandles.lookup().findStatic(Utils.class, "longToAddress",
                    MethodType.methodType(MemoryAddressProxy.class, long.class));
            ADDRESS_TO_LONG = MethodHandles.lookup().findStatic(Utils.class, "addressToLong",
                    MethodType.methodType(long.class, MemoryAddressProxy.class));
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    // The maximum alignment supported by malloc - typically 16 on
    // 64-bit platforms and 8 on 32-bit platforms.
    private final static long MAX_ALIGN = Unsafe.ADDRESS_SIZE == 4 ? 8 : 16;

    // the memory address var handle assumes that addresses have same size as a Java long
    private final static long POINTER_SIZE = 8;

    private static final JavaNioAccess javaNioAccess = SharedSecrets.getJavaNioAccess();
    private static final JavaLangInvokeAccess javaLangInvokeAccess = SharedSecrets.getJavaLangInvokeAccess();

    private static final boolean skipZeroMemory = GetBooleanAction.privilegedGetProperty("jdk.internal.foreign.skipZeroMemory");

    public static long alignUp(long n, long alignment) {
        return (n + alignment - 1) & -alignment;
    }

    public static long bitsToBytesOrThrow(long bits, Supplier<RuntimeException> exFactory) {
        if (bits % 8 == 0) {
            return bits / 8;
        } else {
            throw exFactory.get();
        }
    }

    static final Class<?> PADDING_CLASS;

    static {
        try {
            PADDING_CLASS = Class.forName("jdk.incubator.foreign.PaddingLayout");
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static boolean isPadding(MemoryLayout layout) {
        return layout.getClass() == PADDING_CLASS;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Constable> getAnnotations(MemoryLayout layout) {
        try {
            Field f = ValueLayout.class.getSuperclass().getDeclaredField("annotations");
            f.setAccessible(true);
            return (Map<String, Constable>) f.get(layout);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static Constable getAnnotation(MemoryLayout layout, String name) {
        return getAnnotations(layout).get(name);
    }

    public static MemoryAddress resizeNativeAddress(MemoryAddress base, long byteSize) {
        return new MemoryAddressImpl((MemorySegmentImpl)Utils.makeNativeSegmentUnchecked(base, byteSize), 0);
    }

    public static void checkCarrier(Class<?> carrier) {
        if (carrier == void.class || carrier == boolean.class ||
                (!carrier.isPrimitive() && !isAddress(carrier))) {
            throw new IllegalArgumentException("Illegal carrier: " + carrier.getSimpleName());
        }
    }

    public static long carrierSize(Class<?> carrier) {
        if (isAddress(carrier)) {
            return POINTER_SIZE;
        }
        long bitsAlignment = Math.max(8, Wrapper.forPrimitiveType(carrier).bitWidth());
        return Utils.bitsToBytesOrThrow(bitsAlignment, IllegalStateException::new);
    }

    public static boolean isAddress(Class<?> carrier) {
        return MemoryAddress.class == carrier ||
                MemoryAddressProxy.class == carrier;
    }

    public static Class<?> adjustCarrier(Class<?> carrier) {
        return carrier == MemoryAddress.class ?
                MemoryAddressProxy.class : carrier;
    }

    // segment factories

    public static MemorySegment makeNativeSegment(long bytesSize, long alignmentBytes) {
        long alignedSize = bytesSize;

        if (alignmentBytes > MAX_ALIGN) {
            alignedSize = bytesSize + (alignmentBytes - 1);
        }

        long buf = unsafe.allocateMemory(alignedSize);
        if (!skipZeroMemory) {
            unsafe.setMemory(buf, alignedSize, (byte)0);
        }
        long alignedBuf = Utils.alignUp(buf, alignmentBytes);
        MemoryScope scope = new MemoryScope(null, () -> unsafe.freeMemory(buf));
        MemorySegment segment = new MemorySegmentImpl(buf, null, alignedSize, 0, Thread.currentThread(), scope);
        if (alignedBuf != buf) {
            long delta = alignedBuf - buf;
            segment = segment.asSlice(delta, bytesSize);
        }
        return segment;
    }

    public static MemorySegment makeNativeSegmentUnchecked(MemoryAddress base, long bytesSize) {
        if (((MemorySegmentImpl)base.segment()).base != null) {
            throw new IllegalArgumentException("Not a native address: " + base);
        }
        return makeNativeSegmentUnchecked(((MemoryAddressImpl)base).unsafeGetOffset(), bytesSize);
    }

    public static MemorySegment makeNativeSegmentUnchecked(long min, long bytesSize) {
        MemoryScope scope = new MemoryScope(null, null);
        return new MemorySegmentImpl(min, null, bytesSize, 0, Thread.currentThread(), scope);
    }

    public static MemorySegment makeArraySegment(byte[] arr) {
        return makeArraySegment(arr, arr.length, Unsafe.ARRAY_BYTE_BASE_OFFSET, Unsafe.ARRAY_BYTE_INDEX_SCALE);
    }

    public static MemorySegment makeArraySegment(char[] arr) {
        return makeArraySegment(arr, arr.length, Unsafe.ARRAY_CHAR_BASE_OFFSET, Unsafe.ARRAY_CHAR_INDEX_SCALE);
    }

    public static MemorySegment makeArraySegment(short[] arr) {
        return makeArraySegment(arr, arr.length, Unsafe.ARRAY_SHORT_BASE_OFFSET, Unsafe.ARRAY_SHORT_INDEX_SCALE);
    }

    public static MemorySegment makeArraySegment(int[] arr) {
        return makeArraySegment(arr, arr.length, Unsafe.ARRAY_INT_BASE_OFFSET, Unsafe.ARRAY_INT_INDEX_SCALE);
    }

    public static MemorySegment makeArraySegment(float[] arr) {
        return makeArraySegment(arr, arr.length, Unsafe.ARRAY_FLOAT_BASE_OFFSET, Unsafe.ARRAY_FLOAT_INDEX_SCALE);
    }

    public static MemorySegment makeArraySegment(long[] arr) {
        return makeArraySegment(arr, arr.length, Unsafe.ARRAY_LONG_BASE_OFFSET, Unsafe.ARRAY_LONG_INDEX_SCALE);
    }

    public static MemorySegment makeArraySegment(double[] arr) {
        return makeArraySegment(arr, arr.length, Unsafe.ARRAY_DOUBLE_BASE_OFFSET, Unsafe.ARRAY_DOUBLE_INDEX_SCALE);
    }

    private static MemorySegment makeArraySegment(Object arr, int size, int base, int scale) {
        MemoryScope scope = new MemoryScope(null, null);
        return new MemorySegmentImpl(base, arr, size * scale, 0, Thread.currentThread(), scope);
    }

    public static MemorySegment makeBufferSegment(ByteBuffer bb) {
        long bbAddress = javaNioAccess.getBufferAddress(bb);
        Object base = javaNioAccess.getBufferBase(bb);

        int pos = bb.position();
        int limit = bb.limit();

        MemoryScope bufferScope = new MemoryScope(bb, null);
        return new MemorySegmentImpl(bbAddress + pos, base, limit - pos, 0, Thread.currentThread(), bufferScope);
    }

    // create and map a file into a fresh segment
    public static MemorySegment makeMappedSegment(Path path, long bytesSize, FileChannel.MapMode mapMode) throws IOException {
        if (bytesSize <= 0) throw new IllegalArgumentException("Requested bytes size must be > 0.");
        try (FileChannelImpl channelImpl = (FileChannelImpl)FileChannel.open(path, openOptions(mapMode))) {
            UnmapperProxy unmapperProxy = channelImpl.mapInternal(mapMode, 0L, bytesSize);
            MemoryScope scope = new MemoryScope(null, unmapperProxy::unmap);
            return new MemorySegmentImpl(unmapperProxy.address(), null, bytesSize, 0, Thread.currentThread(), scope);
        }
    }

    private static OpenOption[] openOptions(FileChannel.MapMode mapMode) {
        if (mapMode == FileChannel.MapMode.READ_ONLY) {
            return new OpenOption[] { StandardOpenOption.READ };
        } else if (mapMode == FileChannel.MapMode.READ_WRITE || mapMode == FileChannel.MapMode.PRIVATE) {
            return new OpenOption[] { StandardOpenOption.READ, StandardOpenOption.WRITE };
        } else {
            throw new UnsupportedOperationException("Unsupported map mode: " + mapMode);
        }
    }

    public static VarHandle fixUpVarHandle(VarHandle handle) {
        // This adaptation is required, otherwise the memory access var handle will have type MemoryAddressProxy,
        // and not MemoryAddress (which the user expects), which causes performance issues with asType() adaptations.
        handle = MethodHandles.filterCoordinates(handle, 0, ADDRESS_FILTER);
        return (javaLangInvokeAccess.memoryAddressCarrier(handle) == MemoryAddressProxy.class) ?
                MethodHandles.filterValue(handle, ADDRESS_TO_LONG, LONG_TO_ADDRESS) :
                handle;
    }

    private static MemoryAddressProxy filterAddress(MemoryAddress addr) {
        return (MemoryAddressImpl)addr;
    }

    private static MemoryAddressProxy longToAddress(long value) {
        return value == 0L ?
                (MemoryAddressImpl)MemoryAddress.NULL :
                (MemoryAddressImpl)MemoryAddress.ofLong(value);
    }

    private static long addressToLong(MemoryAddressProxy value) {
        return value.unsafeGetOffset();
    }
}
