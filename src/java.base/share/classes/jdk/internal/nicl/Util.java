/*
 * Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package jdk.internal.nicl;

import jdk.internal.misc.Unsafe;
import jdk.internal.nicl.types.BoundedMemoryRegion;
import jdk.internal.nicl.types.BoundedPointer;
import jdk.internal.nicl.types.DescriptorParser;
import jdk.internal.nicl.types.Types;
import jdk.internal.org.objectweb.asm.Type;

import java.lang.annotation.Native;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.nicl.NativeTypes;
import java.nicl.Scope;
import java.nicl.layout.Address;
import java.nicl.layout.Function;
import java.nicl.layout.Layout;
import java.nicl.layout.Sequence;
import java.nicl.metadata.C;
import java.nicl.metadata.CallingConvention;
import java.nicl.metadata.NativeType;
import java.nicl.types.*;
import java.nicl.types.Array;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Objects;

public final class Util {

    public static final long BYTE_BUFFER_BASE;
    public static final long BUFFER_ADDRESS;

    static {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Unsafe UNSAFE = (Unsafe) unsafeField.get(null);

            BYTE_BUFFER_BASE = UNSAFE.objectFieldOffset(ByteBuffer.class.getDeclaredField("hb"));
            BUFFER_ADDRESS = UNSAFE.objectFieldOffset(Buffer.class.getDeclaredField("address"));
        }
        catch (Exception e) {
            throw new InternalError(e);
        }
    }

    private Util() {
    }

    public static Object getBufferBase(ByteBuffer bb) {
        return UNSAFE.getObject(bb, BYTE_BUFFER_BASE);
    }

    public static long getBufferAddress(ByteBuffer bb) {
        return UNSAFE.getLong(bb, BUFFER_ADDRESS);
    }

    public static long alignUp(long n, long alignment) {
        return (n + alignment - 1) & ~(alignment - 1);
    }

    public static boolean isCStruct(Class<?> clz) {
        if (!clz.isAnnotationPresent(C.class) ||
               !clz.isAnnotationPresent(NativeType.class)) {
            return false;
        }
        NativeType nt = clz.getAnnotation(NativeType.class);
        return nt.isRecordType();
    }

    public static boolean isFunction(Class<?> clz) {
        if (!isCStruct(clz)) {
            return false;
        }

        return clz.isAnnotationPresent(CallingConvention.class);
    }

    public static Layout variadicLayout(Class<?> c) {
        c = (Class<?>)unboxIfNeeded(c);
        if (c.isPrimitive()) {
            //it is ok to approximate with a machine word here; numerics arguments in a prototype-less
            //function call are always rounded up to a register size anyway.
            return Types.INT64;
        } else if (Pointer.class.isAssignableFrom(c)) {
            return Types.POINTER;
        } else if (isFunctionalInterface(c)) {
            return Types.POINTER;
        } else if (isCStruct(c)) {
            return layoutof(c);
        } else {
            throw new IllegalArgumentException("Unhandled variadic argument class: " + c);
        }
    }

    public static Layout layoutof(Class<?> c) {
        NativeType nt = c.getAnnotation(NativeType.class);
        return new DescriptorParser(nt.layout()).parseLayout().findFirst().get();
    }

    public static Function functionof(Method m) {
        NativeType nt = m.getAnnotation(NativeType.class);
        return (Function)new DescriptorParser(nt.layout()).parseDescriptorOrLayouts().findFirst().get();
    }

    public static boolean isFunction(Method m) {
        try {
            functionof(m);
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    static MethodType methodTypeFor(Method method) {
        return MethodType.methodType(method.getReturnType(), method.getParameterTypes());
    }

    public static boolean isFunctionalInterface(Class<?> c) {
        return c.isAnnotationPresent(FunctionalInterface.class);
    }

    public static Method findFunctionalInterfaceMethod(Class<?> c) {
        for (Method m : c.getMethods()) {
            if (m.getName().equals("fn")) {
                return m;
            }
        }

        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static LayoutType<?> makeType(java.lang.reflect.Type carrier, Layout layout) {
        carrier = unboxIfNeeded(carrier);
        if (carrier == byte.class) {
            return LayoutType.ofByte(layout);
        } else if (carrier == boolean.class) {
            return LayoutType.ofBoolean(layout);
        } else if (carrier == short.class) {
            return LayoutType.ofShort(layout);
        } else if (carrier == int.class) {
            return LayoutType.ofInt(layout);
        } else if (carrier == char.class) {
            return LayoutType.ofChar(layout);
        } else if (carrier == long.class) {
            return LayoutType.ofLong(layout);
        } else if (carrier == float.class) {
            return LayoutType.ofFloat(layout);
        } else if (carrier == double.class) {
            return LayoutType.ofDouble(layout);
        } else if (carrier == Pointer.class) { //pointers
            return NativeTypes.VOID.pointer();
        } else if (Pointer.class.isAssignableFrom(erasure(carrier))) {
            if (carrier instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType)carrier;
                java.lang.reflect.Type arg = pt.getActualTypeArguments()[0];
                if (arg instanceof WildcardType) {
                    return NativeTypes.VOID.pointer();
                }
                Address addr = (Address)layout;
                return addr.addresseeInfo().isPresent() ?
                        makeType(arg, ((Address)layout).addresseeInfo().get().layout()).pointer() :
                        NativeTypes.VOID.pointer();
            } else {
                return NativeTypes.VOID.pointer();
            }
        } else if (Array.class.isAssignableFrom(erasure(carrier))) {
            if (carrier instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType)carrier;
                java.lang.reflect.Type arg = pt.getActualTypeArguments()[0];
                if (arg instanceof WildcardType) {
                    return NativeTypes.VOID.array();
                }
                return makeType(arg, ((Sequence)layout).element()).array(((Sequence)layout).elementsSize());
            } else {
                return NativeTypes.VOID.array();
            }
        } else if (Struct.class.isAssignableFrom(erasure(carrier))) {
            return LayoutType.ofStruct((Class) carrier);
        } else if (erasure(carrier).isArray()) {
            //Todo: this provisional, Java arrays are not meant to be supported in this way
            java.lang.reflect.Type element = (carrier instanceof GenericArrayType) ?
                    ((GenericArrayType)carrier).getGenericComponentType() :
                    erasure(carrier).getComponentType();
            return makeType(element, ((Sequence)layout).element()).array(((Sequence)layout).elementsSize());
        } else {
            throw new IllegalStateException("Unknown carrier: " + carrier.getTypeName());
        }
    }

    static Class<?> erasure(java.lang.reflect.Type type) {
        return (type instanceof ParameterizedType) ?
                (Class<?>)((ParameterizedType)type).getRawType() :
                (Class<?>)type;
    }

    public static java.lang.reflect.Type unboxIfNeeded(java.lang.reflect.Type clazz) {
        if (clazz == Boolean.class) {
            return boolean.class;
        } else if (clazz == Byte.class) {
            return byte.class;
        } else if (clazz == Character.class) {
            return char.class;
        } else if (clazz == Short.class) {
            return short.class;
        } else if (clazz == Integer.class) {
            return int.class;
        } else if (clazz == Long.class) {
            return long.class;
        } else if (clazz == Float.class) {
            return float.class;
        } else if (clazz == Double.class) {
            return double.class;
        } else {
            return clazz;
        }
    }

    public static final LayoutType<Byte> BYTE_TYPE = NativeTypes.INT8;
    public static final LayoutType<Pointer<Byte>> BYTE_PTR_TYPE = BYTE_TYPE.pointer();

    private static final Unsafe UNSAFE = Unsafe.getUnsafe();

    public static long unpack(Pointer<?> ptr) throws IllegalAccessException {
        return ptr == null? 0 : ptr.addr();
    }

    public static long strlen(long addr) {
        long i = 0;

        while (UNSAFE.getByte(addr + i) != 0) {
            i++;
        }

        return i;
    }

    public static <T> Pointer<T> createPtr(long addr, LayoutType<T> type) {
        return createPtr(null, addr, type);
    }

    public static <T> Pointer<T> createPtr(Object base, long addr, LayoutType<T> type) {
        // FIXME: Long.MAX_VALUE is not correct
        return new BoundedPointer<>(type, new BoundedMemoryRegion(base, addr, Long.MAX_VALUE), 0);
    }

    // Helper methods useful for playing with pointers into the Java heap and data copying

    public static BoundedMemoryRegion createRegionForArrayElements(long[] arr) {
        return new BoundedMemoryRegion(arr, UNSAFE.arrayBaseOffset(long[].class), arr.length * 8, BoundedMemoryRegion.MODE_RW);
    }

    public static BoundedMemoryRegion createRegionForArrayElements(byte[] arr) {
        return new BoundedMemoryRegion(arr, UNSAFE.arrayBaseOffset(byte[].class), arr.length, BoundedMemoryRegion.MODE_RW);
    }

    public static BoundedMemoryRegion createRegionForArrayElements(long[] arr, Scope scope) {
        return new BoundedMemoryRegion(arr, UNSAFE.arrayBaseOffset(long[].class), arr.length * 8, BoundedMemoryRegion.MODE_RW, scope);
    }

    public static Pointer<Long> createArrayElementsPointer(long[] arr) {
        return new BoundedPointer<>(NativeTypes.INT64, createRegionForArrayElements(arr), 0);
    }

    public static Pointer<Byte> createArrayElementsPointer(byte[] arr) {
        return new BoundedPointer<>(NativeTypes.INT8, createRegionForArrayElements(arr), 0);
    }

    public static Pointer<Long> createArrayElementsPointer(long[] arr, Scope scope) {
        return new BoundedPointer<>(NativeTypes.INT64, createRegionForArrayElements(arr, scope), 0);
    }

    public static void copy(Pointer<?> src, Pointer<?> dst, long bytes) throws IllegalAccessException {
        BoundedPointer<?> bsrc = (BoundedPointer<?>)Objects.requireNonNull(src);
        BoundedPointer<?> bdst = (BoundedPointer<?>)Objects.requireNonNull(dst);

        bsrc.copyTo(bdst, bytes);
    }
}
