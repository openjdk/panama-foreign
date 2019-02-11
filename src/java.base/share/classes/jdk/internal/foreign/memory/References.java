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

import java.foreign.layout.Sequence;
import java.foreign.layout.Value;
import java.foreign.layout.Value.Kind;
import java.foreign.memory.Array;
import java.foreign.memory.Callback;
import java.foreign.memory.LayoutType;
import java.foreign.memory.Pointer;
import java.foreign.memory.Struct;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Supplier;
import jdk.internal.foreign.LibrariesHelper;
import jdk.internal.foreign.Util;

/**
 * Helper class for references. Defines several reference subclasses, specialized in well-known Java carrier
 * types (both primitive and reference types). It also provides factories for common reference types.
 */
public final class References {

    private References() {}

    /**
     * A reference is a view over a memory layout associated with a {@link java.foreign.memory.Pointer} instance. As such,
     * it provides capabilities for retrieving (resp. storing) values from (resp. to) memory.
     */
    public interface Reference {


        /**
         * A {@link MethodHandle} which can be used to retrieve the contents of memory layout associated
         * with the pointer passed as argument.
         * <p>
         * A getter method handle is of the form:
         * {@code (Pointer) -> T}
         * Where {@code T} is the Java type to which the layout will be converted.
         * </p>
         * @return a 'getter' method handle.
         */
        MethodHandle getter();

        /**
         * A {@link MethodHandle} which can be used to store a value into the memory layout associated with
         * with the pointer passed as argument.
         * <p>
         * A setter method handle is of the form:
         * {@code (Pointer, T) -> V}
         * Where {@code T} is the Java type to which the layout will be converted.
         * </p>
         * the pointer passed as argument.
         * @return a 'getter' method handle.
         */
        MethodHandle setter();
    }

    /**
     * A reference for the Java primitive type {@code boolean}.
     */
    static class OfBoolean implements Reference {
        static final MethodHandle GETTER_MH;
        static final MethodHandle SETTER_MH;

        static {
            try {
                GETTER_MH = MethodHandles.lookup().findStatic(OfBoolean.class, "getBoolean", MethodType.methodType(boolean.class, Pointer.class));
                SETTER_MH = MethodHandles.lookup().findStatic(OfBoolean.class, "setBoolean", MethodType.methodType(void.class, Pointer.class, boolean.class));
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public MethodHandle getter() {
            return GETTER_MH;
        }

        @Override
        public MethodHandle setter() {
            return SETTER_MH;
        }

        static boolean getBoolean(Pointer<?> pointer) {
            return ((BoundedPointer<?>)pointer).getBits() != 0;
        }

        static void setBoolean(Pointer<?> pointer, boolean value) {
            ((BoundedPointer<?>)pointer).putBits(value ? 1 : 0);
        }
    }

    /**
     * A reference for the Java primitive type {@code char}.
     */
    static class OfChar implements Reference {
        static final MethodHandle GETTER_MH;
        static final MethodHandle SETTER_MH;

        static {
            try {
                GETTER_MH = MethodHandles.lookup().findStatic(References.OfChar.class, "getChar", MethodType.methodType(char.class, Pointer.class));
                SETTER_MH = MethodHandles.lookup().findStatic(References.OfChar.class, "setChar", MethodType.methodType(void.class, Pointer.class, char.class));
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public MethodHandle getter() {
            return GETTER_MH;
        }

        @Override
        public MethodHandle setter() {
            return SETTER_MH;
        }

        static char getChar(Pointer<?> pointer) {
            return (char)((BoundedPointer<?>)pointer).getBits();
        }

        static void setChar(Pointer<?> pointer, char value) {
            ((BoundedPointer<?>)pointer).putBits(value);
        }
    }

    /**
     * A reference for the Java primitive type {@code byte}.
     */
    static class OfByte implements Reference {
        static final MethodHandle GETTER_MH;
        static final MethodHandle SETTER_MH;

        static {
            try {
                GETTER_MH = MethodHandles.lookup().findStatic(OfByte.class, "getByte", MethodType.methodType(byte.class, Pointer.class));
                SETTER_MH = MethodHandles.lookup().findStatic(OfByte.class, "setByte", MethodType.methodType(void.class, Pointer.class, byte.class));
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public MethodHandle getter() {
            return GETTER_MH;
        }

        @Override
        public MethodHandle setter() {
            return SETTER_MH;
        }

        static byte getByte(Pointer<?> pointer) {
            return (byte)((BoundedPointer<?>)pointer).getBits();
        }

        static void setByte(Pointer<?> pointer, byte value) {
            ((BoundedPointer<?>)pointer).putBits(value);
        }
    }

    /**
     * A reference for the Java primitive type {@code short}.
     */
    static class OfShort implements Reference {
        static final MethodHandle GETTER_MH;
        static final MethodHandle SETTER_MH;

        static {
            try {
                GETTER_MH = MethodHandles.lookup().findStatic(OfShort.class, "getShort", MethodType.methodType(short.class, Pointer.class));
                SETTER_MH = MethodHandles.lookup().findStatic(OfShort.class, "setShort", MethodType.methodType(void.class, Pointer.class, short.class));
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public MethodHandle getter() {
            return GETTER_MH;
        }

        @Override
        public MethodHandle setter() {
            return SETTER_MH;
        }

        static short getShort(Pointer<?> pointer) {
            return (short)((BoundedPointer<?>)pointer).getBits();
        }

        static void setShort(Pointer<?> pointer, short value) {
            ((BoundedPointer<?>)pointer).putBits(value);
        }
    }

    /**
     * A reference for the Java primitive type {@code int}.
     */
    static class OfInt implements Reference {
        static final MethodHandle GETTER_MH;
        static final MethodHandle SETTER_MH;

        static {
            try {
                GETTER_MH = MethodHandles.lookup().findStatic(OfInt.class, "getInt", MethodType.methodType(int.class, Pointer.class));
                SETTER_MH = MethodHandles.lookup().findStatic(OfInt.class, "setInt", MethodType.methodType(void.class, Pointer.class, int.class));
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public MethodHandle getter() {
            return GETTER_MH;
        }

        @Override
        public MethodHandle setter() {
            return SETTER_MH;
        }

        static int getInt(Pointer<?> pointer) {
            return (int)((BoundedPointer<?>)pointer).getBits();
        }

        static void setInt(Pointer<?> pointer, int value) {
            ((BoundedPointer<?>)pointer).putBits(value);
        }
    }

    /**
     * A reference for the Java primitive type {@code float}.
     */
    static class OfFloat implements Reference {
        static final MethodHandle GETTER_MH;
        static final MethodHandle SETTER_MH;

        static {
            try {
                GETTER_MH = MethodHandles.lookup().findStatic(OfFloat.class, "getFloat", MethodType.methodType(float.class, Pointer.class));
                SETTER_MH = MethodHandles.lookup().findStatic(OfFloat.class, "setFloat", MethodType.methodType(void.class, Pointer.class, float.class));
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public MethodHandle getter() {
            return GETTER_MH;
        }

        @Override
        public MethodHandle setter() {
            return SETTER_MH;
        }

        static float getFloat(Pointer<?> pointer) {
            return Float.intBitsToFloat((int)((BoundedPointer<?>)pointer).getBits());
        }

        static void setFloat(Pointer<?> pointer, float value) {
            ((BoundedPointer<?>)pointer).putBits(Float.floatToIntBits(value));
        }
    }

    /**
     * A reference for the Java primitive type {@code long}.
     */
    static class OfLong implements Reference {
        static final MethodHandle GETTER_MH;
        static final MethodHandle SETTER_MH;

        static {
            try {
                GETTER_MH = MethodHandles.lookup().findStatic(OfLong.class, "getLong", MethodType.methodType(long.class, Pointer.class));
                SETTER_MH = MethodHandles.lookup().findStatic(OfLong.class, "setLong", MethodType.methodType(void.class, Pointer.class, long.class));
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public MethodHandle getter() {
            return GETTER_MH;
        }

        @Override
        public MethodHandle setter() {
            return SETTER_MH;
        }

        static long getLong(Pointer<?> pointer) {
            return ((BoundedPointer<?>)pointer).getBits();
        }

        static void setLong(Pointer<?> pointer, long value) {
            ((BoundedPointer<?>)pointer).putBits(value);
        }
    }

    /**
     * A reference for the Java primitive type {@code double}.
     */
    static class OfDouble implements Reference {
        static final MethodHandle GETTER_MH;
        static final MethodHandle SETTER_MH;

        static {
            try {
                GETTER_MH = MethodHandles.lookup().findStatic(OfDouble.class, "getDouble", MethodType.methodType(double.class, Pointer.class));
                SETTER_MH = MethodHandles.lookup().findStatic(OfDouble.class, "setDouble", MethodType.methodType(void.class, Pointer.class, double.class));
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public MethodHandle getter() {
            return GETTER_MH;
        }

        @Override
        public MethodHandle setter() {
            return SETTER_MH;
        }

        static double getDouble(Pointer<?> pointer) {
            return Double.longBitsToDouble(((BoundedPointer<?>)pointer).getBits());
        }

        static void setDouble(Pointer<?> pointer, double value) {
            ((BoundedPointer<?>)pointer).putBits(Double.doubleToLongBits(value));
        }
    }

    /**
     * A reference for the Java reference type {@code BigDecimal}.
     */
    public static class OfLongDouble implements Reference {

        static final MethodHandle GETTER_MH;
        static final MethodHandle SETTER_MH;

        static {
            try {
                GETTER_MH = MethodHandles.lookup().findStatic(OfLongDouble.class, "get", MethodType.methodType(double.class, Pointer.class));
                SETTER_MH = MethodHandles.lookup().findStatic(OfLongDouble.class, "set", MethodType.methodType(void.class, Pointer.class, double.class));
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public MethodHandle getter() {
            return GETTER_MH;
        }

        @Override
        public MethodHandle setter() {
            return SETTER_MH;
        }

        /**
         * Read the contents of this {@code long double} as a double value.
         * @return the double value.
         */
        static double get(Pointer<?> pointer) {
            return Util.withOffHeapAddress(pointer, References::longDoubleToDouble);
        }

        /**
         * Store a given {@code long double} value in this reference.
         * @param value the double value.
         */
        static void set(Pointer<?> pointer, double value) {
            Util.withOffHeapAddress(pointer, addr -> { doubleToLongDouble(addr, value); return null; });
        }
    }

    /**
     * Reference for pointers.
     */
    public static class OfPointer implements Reference {

        static final MethodHandle GETTER_MH;
        static final MethodHandle SETTER_MH;

        static {
            try {
                GETTER_MH = MethodHandles.lookup().findStatic(OfPointer.class, "get", MethodType.methodType(Pointer.class, Pointer.class));
                SETTER_MH = MethodHandles.lookup().findStatic(OfPointer.class, "set", MethodType.methodType(void.class, Pointer.class, Pointer.class));
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public MethodHandle getter() {
            return GETTER_MH;
        }

        @Override
        public MethodHandle setter() {
            return SETTER_MH;
        }

        static Pointer<?> get(Pointer<?> pointer) {
            long addr = ((BoundedPointer<?>)pointer).getBits();
            LayoutType<?> pointeeType = ((LayoutTypeImpl<?>)pointer.type()).pointeeType();
            Pointer<?> rp = addr == 0 ?
                    Pointer.nullPointer() :
                    BoundedPointer.createNativePointer(pointeeType, addr);
            return rp;
        }

        static void set(Pointer<?> pointer, Pointer<?> pointerValue) {
            try {
                ((BoundedPointer<?>)pointer).putBits(pointerValue.addr());
            } catch (IllegalAccessException iae) {
                throw new RuntimeException("Access denied", iae);
            }
        }
    }

    /**
     * Reference for arrays.
     */
    public static class OfArray implements Reference {

        static final MethodHandle GETTER_MH;
        static final MethodHandle SETTER_MH;

        static {
            try {
                GETTER_MH = MethodHandles.lookup().findStatic(OfArray.class, "get", MethodType.methodType(Array.class, Pointer.class));
                SETTER_MH = MethodHandles.lookup().findStatic(OfArray.class, "set", MethodType.methodType(void.class, Pointer.class, Array.class));
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public MethodHandle getter() {
            return GETTER_MH;
        }

        @Override
        public MethodHandle setter() {
            return SETTER_MH;
        }

        static Array<?> get(Pointer<?> pointer) {
            ((BoundedPointer<?>)pointer).checkAlive();
            Sequence seq = (Sequence)pointer.type().layout();
            LayoutType<?> elementType = ((LayoutTypeImpl<?>)pointer.type()).elementType();
            return new BoundedArray<>((BoundedPointer<?>)Util.unsafeCast(pointer, elementType), seq.elementsSize());
        }

        static void set(Pointer<?> pointer, Array<?> arrayValue) {
            try {
                Pointer.copy(arrayValue.elementPointer(), pointer,
                        arrayValue.bytesSize());
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    /**
     * A reference for native structs.
     */
    @SuppressWarnings("unchecked")
    public static class OfStruct implements Reference {

        static final MethodHandle GETTER_MH;
        static final MethodHandle SETTER_MH;

        static {
            try {
                GETTER_MH = MethodHandles.lookup().findStatic(OfStruct.class, "get", MethodType.methodType(Struct.class, Pointer.class));
                SETTER_MH = MethodHandles.lookup().findStatic(OfStruct.class, "set", MethodType.methodType(void.class, Pointer.class, Struct.class));
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public MethodHandle getter() {
            return GETTER_MH;
        }

        @Override
        public MethodHandle setter() {
            return SETTER_MH;
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
                Pointer.copy(t.ptr(), pointer, pointer.type().bytesSize());
            } catch (IllegalAccessException iae) {
                throw new RuntimeException("Access denied", iae);
            }
        }
    }

    /**
     * Reference for function pointers.
     */
    public static class OfFunction implements Reference {

        static final MethodHandle GETTER_MH;
        static final MethodHandle SETTER_MH;

        static {
            try {
                GETTER_MH = MethodHandles.lookup().findStatic(OfFunction.class, "get", MethodType.methodType(Callback.class, Pointer.class));
                SETTER_MH = MethodHandles.lookup().findStatic(OfFunction.class, "set", MethodType.methodType(void.class, Pointer.class, Callback.class));
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public MethodHandle getter() {
            return GETTER_MH;
        }

        @Override
        public MethodHandle setter() {
            return SETTER_MH;
        }

        static Callback<?> get(Pointer<?> pointer) {
            long addr = ((BoundedPointer<?>)pointer).getBits();
            Class<?> carrier = ((LayoutTypeImpl<?>)pointer.type()).getFuncIntf();
            Pointer<?> rp = addr == 0 ?
                    Pointer.nullPointer() :
                    BoundedPointer.createNativeVoidPointer(pointer.scope(), addr);
            return new CallbackImpl<>(rp, carrier);
        }

        static void set(Pointer<?> pointer, Callback<?> func) {
            try {
                ((BoundedPointer<?>)pointer).putBits(func.entryPoint().addr());
            } catch (IllegalAccessException iae) {
                throw new RuntimeException("Access denied", iae);
            }
        }
    }

    /**
     * Reference that always throws (useful to model null/void pointers).
     */
    public static class OfGrumpy implements Reference {

        Supplier<RuntimeException> exceptionFactory;

        OfGrumpy(Supplier<RuntimeException> exceptionFactory) {
            this.exceptionFactory = exceptionFactory;
        }

        @Override
        public MethodHandle getter() {
            throw exceptionFactory.get();
        }

        @Override
        public MethodHandle setter() {
            throw exceptionFactory.get();
        }
    }

    /**
     * Reference for the {@code char} primitive type.
     */
    public static OfChar ofChar = new OfChar();

    /**
     * Reference for the {@code boolean} primitive type.
     */
    public static OfBoolean ofBoolean = new OfBoolean();

    /**
     * Reference for the {@code byte} primitive type.
     */
    public static OfByte ofByte = new OfByte();

    /**
     * Reference for the {@code short} primitive type.
     */
    public static OfShort ofShort = new OfShort();

    /**
     * Reference for the {@code int} primitive type.
     */
    public static OfInt ofInt = new OfInt();

    /**
     * Reference for the {@code float} primitive type.
     */
    public static OfFloat ofFloat = new OfFloat();

    /**
     * Reference for the {@code long} primitive type.
     */
    public static OfLong ofLong = new OfLong();

    /**
     * Reference for the {@code double} primitive type.
     */
    public static OfDouble ofDouble = new OfDouble();

    /**
     * Reference for the {@code long double} primitive type.
     */
    public static OfLongDouble ofLongDouble = new OfLongDouble();

    /**
     * Reference for pointer types.
     */
    public static OfPointer ofPointer = new OfPointer();

    /**
     * Reference for array types.
     */
    public static OfArray ofArray = new OfArray();

    /**
     * Reference for struct types.
     */
    public static OfStruct ofStruct = new OfStruct();

    /**
     * Reference for function pointer types.
     */
    public static OfFunction ofFunction = new OfFunction();

    /**
     * Reference for type {@code void}. Always throws.
     */
    public static Reference ofVoid = new OfGrumpy(() -> new UnsupportedOperationException("Cannot dereference void"));

    /**
     * Reference for null pointers. Always throws.
     */
    public static Reference ofNull = new OfGrumpy(() -> new NullPointerException("Cannot dereference null"));

    public native static double longDoubleToDouble(long ptr);
    public native static void doubleToLongDouble(long ptr, double val);

    private static native void registerNatives();
    static {
        registerNatives();
    }
}
