/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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

package jdk.internal.foreign.memory;

import jdk.internal.foreign.*;

import java.foreign.layout.Sequence;
import java.foreign.layout.Value;
import java.foreign.layout.Value.Kind;
import java.foreign.memory.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Helper class for references. Defines several reference subclasses, specialized in well-known Java carrier
 * types (both primitive and reference types). It also provides factories for common reference types.
 */
public final class References {

    private References() {}

    static class AbstractReference implements Reference {
        protected MethodHandle getter, setter;

        AbstractReference(MethodHandle getter, MethodHandle setter) {
            this.getter = getter;
            this.setter = setter;
        }

        public final MethodHandle getter() {
            return getter;
        }

        public final MethodHandle setter() {
            return setter;
        }
    }


    /**
     * Reference for primitive carriers. It exposes specialized accessors for getting/setting
     * the contents of a reference.
     */
    static class OfPrimitive extends AbstractReference {

        static final MethodHandle MH_GET_LONG_BITS;
        static final MethodHandle MH_SET_LONG_BITS;

        static {
            try {
                MH_GET_LONG_BITS = MethodHandles.lookup().findStatic(OfPrimitive.class, "getLongBits", MethodType.methodType(long.class, Pointer.class));
                MH_SET_LONG_BITS = MethodHandles.lookup().findStatic(OfPrimitive.class, "setLongBits", MethodType.methodType(void.class, Pointer.class, long.class));
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }

        static long getLongBits(Pointer<?> p2) {
            BoundedPointer<?> ptr = (BoundedPointer<?>)p2;
            boolean signed = ptr.type.layout() instanceof Value &&
                    ((Value)ptr.type.layout()).kind() != Kind.INTEGRAL_UNSIGNED;
            return ptr.region.getBits(ptr.offset, ptr.type.layout().bitsSize() / 8, signed);
        }

        static void setLongBits(Pointer<?> p2, long value) {
            BoundedPointer<?> ptr = (BoundedPointer<?>)p2;
            ptr.region.putBits(ptr.offset, ptr.type.layout().bitsSize() / 8, value);
        }

        OfPrimitive(Class<?> carrier) {
            super(MethodHandles.explicitCastArguments(MH_GET_LONG_BITS, MH_GET_LONG_BITS.type().changeReturnType(carrier)),
                    MethodHandles.explicitCastArguments(MH_SET_LONG_BITS, MH_SET_LONG_BITS.type().changeParameterType(1, carrier)));
        }
    }

    /**
     * A reference for the Java primitive type {@code boolean}.
     */
    public static class OfBoolean extends AbstractReference {

        static final MethodHandle MH_TO_BOOLEAN;
        static final MethodHandle MH_FROM_BOOLEAN;

        static {
            try {
                MH_TO_BOOLEAN = MethodHandles.lookup().findStatic(OfBoolean.class, "toBoolean", MethodType.methodType(boolean.class, long.class));
                MH_FROM_BOOLEAN = MethodHandles.lookup().findStatic(OfBoolean.class, "fromBoolean", MethodType.methodType(long.class, boolean.class));
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }

        static boolean toBoolean(long value) {
            return value != 0;
        }

        static long fromBoolean(boolean value) {
            return value ? 1 : 0;
        }


        OfBoolean() {
            super(MethodHandles.filterReturnValue(OfPrimitive.MH_GET_LONG_BITS, MH_TO_BOOLEAN),
                    MethodHandles.filterArguments(OfPrimitive.MH_SET_LONG_BITS, 1, MH_FROM_BOOLEAN));
        }
    }

    /**
     * A reference for the Java primitive type {@code float}.
     */
    public static class OfFloat extends AbstractReference {

        static final MethodHandle MH_TO_FLOAT;
        static final MethodHandle MH_FROM_FLOAT;

        static {
            try {
                MH_TO_FLOAT = MethodHandles.explicitCastArguments(MethodHandles.lookup().findStatic(Float.class, "intBitsToFloat", MethodType.methodType(float.class, int.class)), MethodType.methodType(float.class, long.class));
                MH_FROM_FLOAT = MethodHandles.explicitCastArguments(MethodHandles.lookup().findStatic(Float.class, "floatToRawIntBits", MethodType.methodType(int.class, float.class)), MethodType.methodType(long.class, float.class));
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }

        OfFloat() {
            super(MethodHandles.filterReturnValue(OfPrimitive.MH_GET_LONG_BITS, MH_TO_FLOAT),
                    MethodHandles.filterArguments(OfPrimitive.MH_SET_LONG_BITS, 1, MH_FROM_FLOAT));
        }
    }

    /**
     * A reference for the Java primitive type {@code double}.
     */
    public static class OfDouble extends AbstractReference {

        static final MethodHandle MH_TO_DOUBLE;
        static final MethodHandle MH_FROM_DOUBLE;

        static {
            try {
                MH_TO_DOUBLE = MethodHandles.lookup().findStatic(Double.class, "longBitsToDouble", MethodType.methodType(double.class, long.class));
                MH_FROM_DOUBLE = MethodHandles.lookup().findStatic(Double.class, "doubleToRawLongBits", MethodType.methodType(long.class, double.class));
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }

        OfDouble() {
            super(MethodHandles.filterReturnValue(OfPrimitive.MH_GET_LONG_BITS, MH_TO_DOUBLE),
                    MethodHandles.filterArguments(OfPrimitive.MH_SET_LONG_BITS, 1, MH_FROM_DOUBLE));
        }

        /**
         * Read the contents of this reference as a double value.
         * @return the double value.
         */
        public double get(Pointer<?> pointer) {
            try {
                return (double)getter().invokeExact(pointer);
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }

        /**
         * Store a given double value in this reference.
         * @param value the double value.
         */
        public void set(Pointer<?> pointer, double value) {
            try {
                setter().invokeExact(pointer, value);
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    /**
     * Reference for pointers.
     */
    public static class OfPointer extends AbstractReference {

        static final MethodHandle MH_PTR_GET;
        static final MethodHandle MH_PTR_SET;

        static {
            try {
                MH_PTR_GET = MethodHandles.lookup().findStatic(OfPointer.class, "get", MethodType.methodType(Pointer.class, Pointer.class));
                MH_PTR_SET = MethodHandles.lookup().findStatic(OfPointer.class, "set", MethodType.methodType(void.class, Pointer.class, Pointer.class));
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }

        public OfPointer() {
            super(MH_PTR_GET, MH_PTR_SET);
        }

        static Pointer<?> get(Pointer<?> pointer) {
            long addr = OfPrimitive.getLongBits(pointer);
            LayoutType<?> pointeeType = ((LayoutTypeImpl<?>)pointer.type()).pointeeType();
            BoundedPointer<?> rp = addr == 0 ?
                    Pointer.nullPointer() :
                    new BoundedPointer<>(pointeeType, new BoundedMemoryRegion(null, addr, Long.MAX_VALUE), 0);
            return rp;
        }

        static void set(Pointer<?> pointer, Pointer<?> pointerValue) {
            try {
                OfPrimitive.setLongBits(pointer, pointerValue.addr());
            } catch (IllegalAccessException iae) {
                throw new RuntimeException("Access denied", iae);
            }
        }
    }

    /**
     * Reference for arrays.
     */
    public static class OfArray extends AbstractReference {

        static final MethodHandle MH_ARR_GET;
        static final MethodHandle MH_ARR_SET;

        static {
            try {
                MH_ARR_GET = MethodHandles.lookup().findStatic(OfArray.class, "get", MethodType.methodType(Array.class, Pointer.class));
                MH_ARR_SET = MethodHandles.lookup().findStatic(OfArray.class, "set", MethodType.methodType(void.class, Pointer.class, Array.class));
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }


        @SuppressWarnings({"unchecked", "rawtypes"})
        public OfArray() {
            super(MH_ARR_GET, MH_ARR_SET);
        }

        static Array<?> get(Pointer<?> pointer) {
            ((BoundedPointer<?>)pointer).checkAlive();
            Sequence seq = (Sequence)pointer.type().layout();
            LayoutType<?> elementType = ((LayoutTypeImpl<?>)pointer.type()).elementType();
            return new BoundedArray<>((BoundedPointer<?>)Util.unsafeCast(pointer, elementType), seq.elementsSize());
        }

        static void set(Pointer<?> pointer, Array<?> arrayValue) {
            try {
                Util.copy(arrayValue.elementPointer(), pointer,
                        arrayValue.elementPointer().bytesSize());
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    /**
     * A reference for native structs.
     */
    @SuppressWarnings("unchecked")
    public static class OfStruct extends AbstractReference {

        static final MethodHandle MH_STR_GET;
        static final MethodHandle MH_STR_SET;

        static {
            try {
                MH_STR_GET = MethodHandles.lookup().findStatic(OfStruct.class, "get", MethodType.methodType(Struct.class, Pointer.class));
                MH_STR_SET = MethodHandles.lookup().findStatic(OfStruct.class, "set", MethodType.methodType(void.class, Pointer.class, Struct.class));
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }

        OfStruct() {
            super(MH_STR_GET, MH_STR_SET);
        }

        @SuppressWarnings("unchecked")
        static Struct<?> get(Pointer<?> pointer) {
            ((BoundedPointer<?>)pointer).checkAlive();
            Class<?> carrier = ((LayoutTypeImpl<?>)pointer.type()).carrier();
            Class<?> structClass = LibrariesHelper.getStructImplClass(carrier);
            try {
                return (Struct<?>)structClass.getConstructor(Pointer.class).newInstance(pointer);
            } catch (ReflectiveOperationException ex) {
                throw new IllegalStateException(ex);
            }
        }

        static void set(Pointer<?> pointer, Struct<?> t) {
            try {
                Util.copy(t.ptr(), pointer, pointer.type().layout().bitsSize() / 8);
            } catch (IllegalAccessException iae) {
                throw new RuntimeException("Access denied", iae);
            }
        }
    }

    /**
     * Reference for function pointers.
     */
    public static class OfFunction extends AbstractReference {

        static final MethodHandle MH_FUNC_GET;
        static final MethodHandle MH_FUNC_SET;

        static {
            try {
                MH_FUNC_GET = MethodHandles.lookup().findStatic(OfFunction.class, "get", MethodType.methodType(Callback.class, Pointer.class));
                MH_FUNC_SET = MethodHandles.lookup().findStatic(OfFunction.class, "set", MethodType.methodType(void.class, Pointer.class, Callback.class));
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }

        OfFunction() {
            super(MH_FUNC_GET, MH_FUNC_SET);
        }

        static Callback<?> get(Pointer<?> pointer) {
            long addr = OfPrimitive.getLongBits(pointer);
            Class<?> carrier = ((LayoutTypeImpl<?>)pointer.type()).getFuncIntf();
            BoundedPointer<?> rp = addr == 0 ?
                    Pointer.nullPointer() :
                    BoundedPointer.createNativeVoidPointer(pointer.scope(), addr);
            return new CallbackImpl<>(rp, carrier);
        }

        static void set(Pointer<?> pointer, Callback<?> func) {
            try {
                OfPrimitive.setLongBits(pointer, func.entryPoint().addr());
            } catch (IllegalAccessException iae) {
                throw new RuntimeException("Access denied", iae);
            }
        }
    }

    /**
     * Reference factory for the {@code char} primitive type.
     */
    public static OfPrimitive ofChar = new OfPrimitive(char.class);

    /**
     * Reference factory for the {@code boolean} primitive type.
     */
    public static OfBoolean ofBoolean = new OfBoolean();

    /**
     * Reference factory for the {@code byte} primitive type.
     */
    public static OfPrimitive ofByte = new OfPrimitive(byte.class);

    /**
     * Reference factory for the {@code short} primitive type.
     */
    public static OfPrimitive ofShort = new OfPrimitive(short.class);

    /**
     * Reference factory for the {@code int} primitive type.
     */
    public static OfPrimitive ofInt = new OfPrimitive(int.class);

    /**
     * Reference factory for the {@code float} primitive type.
     */
    public static OfFloat ofFloat = new OfFloat();

    /**
     * Reference factory for the {@code long} primitive type.
     */
    public static OfPrimitive ofLong = new OfPrimitive(long.class);

    /**
     * Reference for the {@code double} primitive type.
     */
    public static OfDouble ofDouble = new OfDouble();

    public static OfPointer ofPointer = new OfPointer();

    public static OfArray ofArray = new OfArray();

    public static OfStruct ofStruct = new OfStruct();

    public static Reference ofVoid = new Reference() {
        @Override
        public MethodHandle getter() {
            throw new UnsupportedOperationException();
        }

        @Override
        public MethodHandle setter() {
            throw new UnsupportedOperationException();
        }
    };

    public static OfFunction ofFunction = new OfFunction();
}
