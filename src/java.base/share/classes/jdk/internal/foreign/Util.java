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
package jdk.internal.foreign;

import jdk.internal.foreign.memory.*;
import jdk.internal.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.foreign.NativeTypes;
import java.foreign.layout.Address;
import java.foreign.layout.Function;
import java.foreign.layout.Layout;
import java.foreign.layout.Sequence;
import java.foreign.annotations.NativeCallback;
import java.foreign.annotations.NativeStruct;
import java.foreign.memory.*;
import java.foreign.memory.Array;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public final class Util {

    public static final LayoutType<Byte> BYTE_TYPE = NativeTypes.INT8;
    public static final LayoutType<Pointer<Byte>> BYTE_PTR_TYPE = BYTE_TYPE.pointer();

    private static final Unsafe UNSAFE = Unsafe.getUnsafe();

    public static final long BYTE_BUFFER_BASE;
    public static final long BUFFER_ADDRESS;
    public static final long LONG_ARRAY_BASE;
    public static final long LONG_ARRAY_SCALE;

    static {
        try {
            BYTE_BUFFER_BASE = UNSAFE.objectFieldOffset(ByteBuffer.class.getDeclaredField("hb"));
            BUFFER_ADDRESS = UNSAFE.objectFieldOffset(Buffer.class.getDeclaredField("address"));

            LONG_ARRAY_BASE = UNSAFE.arrayBaseOffset(long[].class);
            LONG_ARRAY_SCALE = UNSAFE.arrayIndexScale(long[].class);
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
        return Struct.class.isAssignableFrom(clz) &&
                clz.isAnnotationPresent(NativeStruct.class);
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

    static boolean isCompatible(Method method, Function function) {
        // same return kind (void or non-void)
        boolean isNonVoidMethod = method.getReturnType() != void.class;
        if (isNonVoidMethod != function.returnLayout().isPresent())  {
            return false;
        }

        //same vararg-ness
        if(method.isVarArgs() != function.isVariadic()) {
            return false;
        }

        //same arity (take Java varargs array into account)
        int expectedArity = function.argumentLayouts().size() + (function.isVariadic() ? 1 : 0);
        return method.getParameterCount() == expectedArity;
    }

    public static void checkCompatible(Method method, Function function) {
        if (!isCompatible(method, function)) {
            throw new IllegalArgumentException(
                "Java method signature and native layout not compatible: " + method + " : " + function);
        }
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

    public static Class<?> findUniqueCallback(Class<?> cls) {
        Set<Class<?>> candidates = new HashSet<>();
        findUniqueCallbackInternal(cls, candidates);
        return (candidates.size() == 1) ?
                candidates.iterator().next() :
                null;
    }

    private static void findUniqueCallbackInternal(Class<?> cls, Set<Class<?>> candidates) {
        if (isCallback(cls)) {
            candidates.add(cls);
        }
        Class<?> sup = cls.getSuperclass();
        if (sup != null) {
            findUniqueCallbackInternal(sup, candidates);
        }
        for (Class<?> i : cls.getInterfaces()) {
            findUniqueCallbackInternal(i, candidates);
        }
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
                if (arg instanceof WildcardType || arg == Void.class) {
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
        } else if (isCStruct(erasure(carrier))) {
            return LayoutType.ofStruct((Class) carrier);
        } else if (Callback.class.isAssignableFrom(erasure(carrier))) {
            if (carrier instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) carrier;
                Type arg = pt.getActualTypeArguments()[0];
                Class<?> cb = erasure(arg);
                if (isCallback(cb)) {
                    return LayoutType.ofFunction((Address) layout, (Class) cb);
                }
            }
            //Error: missing type info!
            throw new IllegalStateException("Invalid callback carrier: " + carrier.getTypeName());
        } else {
            throw new IllegalStateException("Unknown carrier: " + carrier.getTypeName());
        }
    }

    public static Class<?> erasure(Type type) {
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

    public static void copy(Pointer<?> src, Pointer<?> dst, long bytes) throws IllegalAccessException {
        BoundedPointer<?> bsrc = (BoundedPointer<?>)Objects.requireNonNull(src);
        BoundedPointer<?> bdst = (BoundedPointer<?>)Objects.requireNonNull(dst);

        bsrc.copyTo(bdst, bytes);
    }

    public static <Z> Pointer<Z> unsafeCast(Pointer<?> ptr, LayoutType<Z> layoutType) {
        return ptr.cast(NativeTypes.VOID).cast(layoutType);
    }

    public static MethodType checkNoArrays(MethodHandles.Lookup lookup, Class<?> fi) {
        try {
            return checkNoArrays(lookup.unreflect(findFunctionalInterfaceMethod(fi)).type());
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }
    public static MethodType checkNoArrays(MethodType mt) {
        if (Stream.concat(Stream.of(mt.returnType()), mt.parameterList().stream())
                .anyMatch(c -> Array.class.isAssignableFrom(c))) {
            //arrays in functions not supported!
            throw new UnsupportedOperationException("Array carriers not supported in functions");
        }
        return mt;
    }

    public static Pointer<?> getSyntheticCallbackAddress(Object o) {
        return (Pointer<?>)UNSAFE.getObject(o, 0L);
    }
}
