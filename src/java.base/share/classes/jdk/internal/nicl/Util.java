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
import jdk.internal.nicl.types.BoundedPointer;
import jdk.internal.nicl.types.DescriptorParser;
import jdk.internal.nicl.types.Types;

import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.nicl.NativeTypes;
import java.nicl.layout.Address;
import java.nicl.layout.Function;
import java.nicl.layout.Layout;
import java.nicl.layout.Sequence;
import java.nicl.metadata.NativeCallback;
import java.nicl.metadata.NativeStruct;
import java.nicl.types.*;
import java.nicl.types.Array;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

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
        return clz.isAnnotationPresent(NativeStruct.class);
    }

    public static Layout variadicLayout(Class<?> c) {
        c = (Class<?>)unboxIfNeeded(c);
        if (c == char.class || c == byte.class || c == short.class || c == int.class || c == long.class) {
            //it is ok to approximate with a machine word here; numerics arguments in a prototype-less
            //function call are always rounded up to a register size anyway.
            return Types.INT64;
        } else if (c == float.class || c == double.class) {
            return Types.DOUBLE;
        } else if (Pointer.class.isAssignableFrom(c)) {
            return Types.POINTER;
        } else if (isCallback(c)) {
            return Types.POINTER;
        } else if (isCStruct(c)) {
            return layoutof(c);
        } else {
            throw new IllegalArgumentException("Unhandled variadic argument class: " + c);
        }
    }

    public static Layout layoutof(Class<?> c) {
        String layout;
        if (c.isAnnotationPresent(NativeStruct.class)) {
            layout = c.getAnnotation(NativeStruct.class).value();
        } else {
            throw new IllegalArgumentException("@NativeStruct or @NativeType expected: " + c);
        }
        return new DescriptorParser(layout).parseLayout();
    }

    public static Function functionof(Class<?> c) {
        if (! c.isAnnotationPresent(NativeCallback.class)) {
            throw new IllegalArgumentException("@NativeCallback expected: " + c);
        }
        NativeCallback nc = c.getAnnotation(NativeCallback.class);
        return new DescriptorParser(nc.value()).parseFunction();
    }

    static MethodType methodTypeFor(Method method) {
        return MethodType.methodType(method.getReturnType(), method.getParameterTypes());
    }

    public static boolean isCallback(Class<?> c) {
        return c.isAnnotationPresent(NativeCallback.class);
    }

    public static Method findFunctionalInterfaceMethod(Class<?> c) {
        Optional<Method> methodOpt = Optional.empty();
        if (c.isAnnotationPresent(NativeCallback.class)) {
            methodOpt = Stream.of(c.getDeclaredMethods())
                .filter(m -> (m.getModifiers() & (Modifier.ABSTRACT | Modifier.PUBLIC)) != 0)
                .findFirst();
        }
        return methodOpt.orElseThrow(IllegalStateException::new);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static LayoutType<?> makeType(Type carrier, Layout layout) {
        carrier = unboxIfNeeded(carrier);
        if (carrier == byte.class) {
            return LayoutType.ofByte(layout);
        } else if (carrier == void.class) {
            return LayoutType.ofVoid(layout);
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
                Type arg = pt.getActualTypeArguments()[0];
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
                Type arg = pt.getActualTypeArguments()[0];
                if (arg instanceof WildcardType) {
                    return NativeTypes.VOID.array();
                }
                return makeType(arg, ((Sequence)layout).element()).array(((Sequence)layout).elementsSize());
            } else {
                return NativeTypes.VOID.array();
            }
        } else if (Struct.class.isAssignableFrom(erasure(carrier))) {
            return LayoutType.ofStruct((Class) carrier);
        } else {
            throw new IllegalStateException("Unknown carrier: " + carrier.getTypeName());
        }
    }

    static Class<?> erasure(Type type) {
        if (type instanceof ParameterizedType) {
            return (Class<?>)((ParameterizedType)type).getRawType();
        } else if (type instanceof GenericArrayType) {
            return java.lang.reflect.Array.newInstance(erasure(((GenericArrayType)type).getGenericComponentType()), 0).getClass();
        } else if (type instanceof TypeVariable<?>) {
            return erasure(((TypeVariable<?>)type).getBounds()[0]);
        } else {
            return (Class<?>)type;
        }
    }

    public static Type unboxIfNeeded(Type clazz) {
        if (clazz == Boolean.class) {
            return boolean.class;
        } else if (clazz == Void.class) {
            return void.class;
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

    public static void copy(Pointer<?> src, Pointer<?> dst, long bytes) throws IllegalAccessException {
        BoundedPointer<?> bsrc = (BoundedPointer<?>)Objects.requireNonNull(src);
        BoundedPointer<?> bdst = (BoundedPointer<?>)Objects.requireNonNull(dst);

        bsrc.copyTo(bdst, bytes);
    }
}
