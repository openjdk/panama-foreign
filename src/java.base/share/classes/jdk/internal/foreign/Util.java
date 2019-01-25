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

import java.foreign.NativeMethodType;
import java.foreign.NativeTypes;
import java.foreign.Scope;
import java.foreign.annotations.NativeCallback;
import java.foreign.annotations.NativeGetter;
import java.foreign.annotations.NativeHeader;
import java.foreign.annotations.NativeStruct;
import java.foreign.layout.Address;
import java.foreign.layout.Function;
import java.foreign.layout.Layout;
import java.foreign.layout.Sequence;
import java.foreign.layout.Value;
import java.foreign.memory.Array;
import java.foreign.memory.Callback;
import java.foreign.memory.LayoutType;
import java.foreign.memory.Pointer;
import java.foreign.memory.Struct;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.LongFunction;
import java.util.stream.Stream;
import jdk.internal.foreign.memory.DescriptorParser;
import jdk.internal.foreign.memory.Types;
import jdk.internal.misc.Unsafe;

public final class Util {

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

    public static long addUnsignedExact(long a, long b) {
        long result = a + b;
        if(Long.compareUnsigned(result, a) < 0) {
            throw new ArithmeticException(
                "Unsigned overflow: "
                    + Long.toUnsignedString(a) + " + "
                    + Long.toUnsignedString(b));
        }

        return result;
    }

    public static Object getBufferBase(ByteBuffer bb) {
        return UNSAFE.getReference(bb, BYTE_BUFFER_BASE);
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

    public static boolean isCLibrary(Class<?> clz) {
        return clz.isAnnotationPresent(NativeHeader.class);
    }

    public static Class<?>[] resolutionContextFor(Class<?> clz) {
        if (isCallback(clz)) {
            return clz.getAnnotation(NativeCallback.class).resolutionContext();
        } else if (isCStruct(clz)) {
            return clz.getAnnotation(NativeStruct.class).resolutionContext();
        } else if (isCLibrary(clz)) {
            return clz.getAnnotation(NativeHeader.class).resolutionContext();
        } else {
            return null;
        }
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
        } else {
            throw new IllegalArgumentException("Class is not a @NativeCallback: " + c.getName());
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
        } else if (carrier == Pointer.class) {
            return NativeTypes.VOID.pointer();
        } else if (Pointer.class.isAssignableFrom(erasure(carrier))) {
            Type targ = extractTypeArgument(carrier);
            Address addr = (Address)layout;
            if (targ == null || addr.layout().isEmpty()) {
                return NativeTypes.VOID.pointer();
            } else {
                return makeType(targ, addr.layout().get()).pointer();
            }
        } else if (Array.class.isAssignableFrom(erasure(carrier))) {
            Type targ = extractTypeArgument(carrier);
            Sequence seq = (Sequence)layout;
            if (targ == null) {
                return NativeTypes.VOID.array();
            } else {
                return makeType(targ, seq.element()).array(seq.elementsSize());
            }
        } else if (isCStruct(erasure(carrier))) {
            return LayoutType.ofStruct((Class) carrier);
        } else if (Callback.class.isAssignableFrom(erasure(carrier))) {
            Type targ = extractTypeArgument(carrier);
            if (targ == null) {
                throw new IllegalStateException("Invalid callback carrier: " + carrier.getTypeName());
            }
            return LayoutType.ofFunction((Address) layout, erasure(targ));
        } else {
            throw new IllegalStateException("Unknown carrier: " + carrier.getTypeName());
        }
    }

    static Type extractTypeArgument(Type t) {
        if (t instanceof ParameterizedType) {
            Type arg = ((ParameterizedType)t).getActualTypeArguments()[0];
            if (arg == Void.class) {
                return null;
            } else if (arg instanceof WildcardType) {
                Type[] lo =  ((WildcardType) arg).getLowerBounds();
                Type[] hi =  ((WildcardType) arg).getUpperBounds();
                if (lo.length == 1) {
                    // Lower bound is zero-elem array if this is '?' or '? extends' wildcards.
                    // Otherwise it's one element array.
                    return lo[0];
                } else if (hi.length == 2 && hi[0] == Object.class) {
                    // Upper bound is always guaranteed to have at least one element, but
                    // the first bound can be j.l.Object if an interface bound is used. In that case,
                    // skip Object, and return the interface bound.
                    return hi[1];
                } else if (hi.length == 1) {
                    // Return either the non-Object class bound, or null.
                    return hi[0] == Object.class ?
                            null : hi[0];
                } else {
                    //unsupported combination of upper/lower bounds.
                    throw new IllegalStateException("Unsupported wildcard type-argument: " + arg.getTypeName());
                }
            } else {
                return arg;
            }
        } else {
            return null;
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
        // First field
        return (Pointer<?>) UNSAFE.getReference(o, 0L);
    }

    public static <Z> Z withOffHeapAddress(Pointer<?> p, LongFunction<Z> longFunction) {
        try {
            try {
                //address
                return longFunction.apply(p.addr());
            } catch (UnsupportedOperationException ex) {
                //heap pointer
                try (Scope sc = Scope.globalScope().fork()) {
                    Pointer<?> offheapPtr = sc.allocate(p.type());
                    Pointer.copy(p, offheapPtr, p.type().bytesSize());
                    Z z = longFunction.apply(offheapPtr.addr());
                    Pointer.copy(offheapPtr, p, p.type().bytesSize());
                    return z;
                }
            }
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static MethodHandle getCallbackMH(Method m) {
        try {
            MethodHandle mh = MethodHandles.publicLookup().unreflect(m);
            Util.checkNoArrays(mh.type());
            return mh;
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static Function getResolvedFunction(Class<?> nativeCallback, Method m) {
        LayoutResolver resolver = LayoutResolver.get(nativeCallback);
        return resolver.resolve(Util.functionof(nativeCallback));
    }

    public static NativeMethodType nativeMethodType(Function function, Method method) {
        checkCompatible(method, function);

        LayoutType<?> ret = function.returnLayout()
                .<LayoutType<?>>map(l -> makeType(method.getGenericReturnType(), l))
                .orElse(NativeTypes.VOID);

        // Use function argument size and ignore last argument from method for vararg function
        LayoutType<?>[] args = new LayoutType<?>[function.argumentLayouts().size()];
        for (int i = 0; i < args.length; i++) {
            args[i] = makeType(method.getGenericParameterTypes()[i], function.argumentLayouts().get(i));
        }

        return NativeMethodType.of(function.isVariadic(), ret, args);
    }

    @SuppressWarnings("unchecked")
    public static Class<?> findStructInterface(Struct<?> struct) {
        for (Class<?> intf : struct.getClass().getInterfaces()) {
            if (intf.isAnnotationPresent(NativeStruct.class)) {
                return intf;
            }
        }
        throw new IllegalStateException("Can not find struct interface");
    }


    public static Method getterByName(Class<?> cls, String name) {
        for (Method m : cls.getDeclaredMethods()) {
            NativeGetter ng = m.getAnnotation(NativeGetter.class);
            if (ng != null && ng.value().equals(name)) {
                return m;
            }
        }
        return null;
    }

    public static Layout requireNoEndianLayout(Layout layout) {
        if (layout instanceof Value) {
            if (((Value) layout).endianness().isPresent()) {
                throw new IllegalArgumentException("Method argument is not allowed to have endianness");
            }
        }
        return layout;
    }
}
