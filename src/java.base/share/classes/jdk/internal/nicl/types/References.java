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

package jdk.internal.nicl.types;

import jdk.internal.nicl.LibrariesHelper;
import jdk.internal.nicl.Util;

import java.nicl.metadata.NativeStruct;
import java.nicl.layout.Sequence;
import java.nicl.layout.Value;
import java.nicl.layout.Value.Kind;
import java.nicl.types.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Helper class for references. Defines several reference subclasses, specialized in well-known Java carrier
 * types (both primitive and reference types). It also provides factories for common reference types.
 */
public final class References {

    private References() {}

    /**
     * Reference for primitive carriers. It exposes specialized accessors for getting/setting
     * the contents of a reference.
     */
    static class OfPrimitive implements Reference {

        static final MethodHandle MH_GET_LONG_BITS;
        static final MethodHandle MH_SET_LONG_BITS;
        
        Class<?> carrier;

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

        static boolean toBoolean(long value) {
            return value != 0;
        }

        static long fromBoolean(boolean value) {
            return value ? 1 : 0;
        }

        public OfPrimitive(Class<?> carrier) {
            super();
            this.carrier = carrier;
        }

        @Override
        public MethodHandle getter() {
            return MethodHandles.explicitCastArguments(MH_GET_LONG_BITS, MH_GET_LONG_BITS.type().changeReturnType(carrier));
        }

        @Override
        public MethodHandle setter() {
            return MethodHandles.explicitCastArguments(MH_SET_LONG_BITS, MH_SET_LONG_BITS.type().changeParameterType(1, carrier));
        }
    }

    /**
     * A reference for the Java primitive type {@code boolean}.
     */
    public static class OfBoolean extends OfPrimitive {

        static final MethodHandle MH_TO_BOOLEAN;
        static final MethodHandle MH_FROM_BOOLEAN;

        static {
            try {
                MH_TO_BOOLEAN = MethodHandles.lookup().findStatic(OfPrimitive.class, "toBoolean", MethodType.methodType(boolean.class, long.class));
                MH_FROM_BOOLEAN = MethodHandles.lookup().findStatic(OfPrimitive.class, "fromBoolean", MethodType.methodType(long.class, boolean.class));
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }


        OfBoolean() {
            super(boolean.class);
        }

        @Override
        public MethodHandle getter() {
            return MethodHandles.filterReturnValue(MH_GET_LONG_BITS, MH_TO_BOOLEAN);
        }

        @Override
        public MethodHandle setter() {
            return MethodHandles.filterArguments(MH_SET_LONG_BITS, 1, MH_FROM_BOOLEAN);
        }

        /**
         * Read the contents of this reference as a boolean value.
         * @return the boolean value.
         */
        public boolean get(Pointer<?> pointer) {
            try {
                return (boolean)getter().invokeExact(pointer);
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }

        /**
         * Store a given boolean value in this reference.
         * @param value the boolean value.
         */
        public void set(Pointer<?> pointer, boolean value) {
            try {
                setter().invokeExact(pointer, value);
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    /**
     * A reference for the Java primitive type {@code char}.
     */
    public static class OfChar extends OfPrimitive {

        OfChar() {
            super(char.class);
        }

        /**
         * Read the contents of this reference as a char value.
         * @return the char value.
         */
        public char get(Pointer<?> pointer) {
            try {
                return (char)getter().invokeExact(pointer);
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }

        /**
         * Store a given char value in this reference.
         * @param value the char value.
         */
        public void set(Pointer<?> pointer, char value) {
            try {
                setter().invokeExact(pointer, value);
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    /**
     * A reference for the Java primitive type {@code byte}.
     */
    public static final class OfByte extends OfPrimitive {

        OfByte() {
            super(byte.class);
        }

        /**
         * Read the contents of this reference as a byte value.
         * @return the byte value.
         */
        public byte get(Pointer<?> pointer) {
            try {
                return (byte)getter().invokeExact(pointer);
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }

        /**
         * Store a given byte value in this reference.
         * @param value the byte value.
         */
        public void set(Pointer<?> pointer, byte value) {
            try {
                setter().invokeExact(pointer, value);
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    /**
     * A reference for the Java primitive type {@code short}.
     */
    public static class OfShort extends OfPrimitive {

        OfShort() {
            super(short.class);
        }

        /**
         * Read the contents of this reference as a short value.
         * @return the short value.
         */
        public short get(Pointer<?> pointer) {
            try {
                return (short)getter().invokeExact(pointer);
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }

        /**
         * Store a given short value in this reference.
         * @param value the short value.
         */
        public void set(Pointer<?> pointer, short value) {
            try {
                setter().invokeExact(pointer, value);
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    /**
     * A reference for the Java primitive type {@code int}.
     */
    public static class OfInt extends OfPrimitive {
        OfInt() {
            super(int.class);
        }

        /**
         * Read the contents of this reference as an int value.
         * @return the int value.
         */
        public int get(Pointer<?> pointer) {
            try {
                return (int)getter().invokeExact(pointer);
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }

        /**
         * Store a given int value in this reference.
         * @param value the int value.
         */
        public void set(Pointer<?> pointer, int value) {
            try {
                setter().invokeExact(pointer, value);
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    /**
     * A reference for the Java primitive type {@code float}.
     */
    public static class OfFloat extends OfPrimitive {

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
            super(float.class);
        }

        @Override
        public MethodHandle getter() {
            return MethodHandles.filterReturnValue(MH_GET_LONG_BITS, MH_TO_FLOAT);
        }

        @Override
        public MethodHandle setter() {
            return MethodHandles.filterArguments(MH_SET_LONG_BITS, 1, MH_FROM_FLOAT);
        }

        /**
         * Read the contents of this reference as a float value.
         * @return the float value.
         */
        public float get(Pointer<?> pointer) {
            try {
                return (float)getter().invokeExact(pointer);
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }

        /**
         * Store a given float value in this reference.
         * @param value the float value.
         */
        public void set(Pointer<?> pointer, float value) {
            try {
                setter().invokeExact(pointer, value);
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    /**
     * A reference for the Java primitive type {@code long}.
     */
    public static class OfLong extends OfPrimitive {

        OfLong() {
            super(long.class);
        }

        /**
         * Read the contents of this reference as a long value.
         * @return the long value.
         */
        public long get(Pointer<?> pointer) {
            try {
                return (long)getter().invokeExact(pointer);
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }

        /**
         * Store a given long value in this reference.
         * @param value the long value.
         */
        public void set(Pointer<?> pointer, long value) {
            try {
                setter().invokeExact(pointer, value);
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    /**
     * A reference for the Java primitive type {@code double}.
     */
    public static class OfDouble extends OfPrimitive {

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
            super(double.class);
        }

        @Override
        public MethodHandle getter() {
            return MethodHandles.filterReturnValue(MH_GET_LONG_BITS, MH_TO_DOUBLE);
        }

        @Override
        public MethodHandle setter() {
            return MethodHandles.filterArguments(MH_SET_LONG_BITS, 1, MH_FROM_DOUBLE);
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
     * This class models a reference to a Java class type (non-primitive). It provides strongly typed operations
     * to get/set the contents of the reference.
     * @param <T> the Java type.
     */
    public static abstract class OfClass<T> implements Reference {

        static final MethodHandle MH_GET;
        static final MethodHandle MH_SET;

        static {
            try {
                MH_GET = MethodHandles.lookup().findVirtual(OfClass.class, "get", MethodType.methodType(Object.class, Pointer.class));
                MH_SET = MethodHandles.lookup().findVirtual(OfClass.class, "set", MethodType.methodType(void.class, Pointer.class, Object.class));
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }

        protected Class<T> carrier;

        public OfClass(Class<T> carrier) {
            super();
            this.carrier = carrier;
        }

        @Override
        public final MethodHandle getter() {
            return MH_GET.asType(MH_GET.type().changeReturnType(carrier)).bindTo(this);
        }

        @Override
        public final MethodHandle setter() {
            return MH_SET.asType(MH_SET.type().changeParameterType(2, carrier)).bindTo(this);
        }

        /**
         * Read the contents of the memory associated with this reference.
         * @return a value of type {@code T}.
         */
        public abstract T get(Pointer<?> pointer);

        /**
         * Store a value of type {@code T} in this reference.
         * @param value a value of type {@code T}.
         */
        public abstract void set(Pointer<?> pointer, T value);
    }

    /**
     * Reference for pointers.
     */
    public static class OfPointer<X> extends OfClass<Pointer<X>> {

        private final LayoutType<X> pointeeType;

        @SuppressWarnings({"unchecked", "rawtypes"})
        public OfPointer(LayoutType<X> pointeeType) {
            super((Class) Pointer.class);
            this.pointeeType = pointeeType;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Pointer<X> get(Pointer<?> pointer) {
            long addr = ofLong.get(pointer);
            BoundedPointer<X> rp = addr == 0 ?
                    Pointer.nullPointer() :
                    new BoundedPointer<>(pointeeType, new BoundedMemoryRegion(null, addr, Long.MAX_VALUE), 0);
            return rp;
        }

        @Override
        public void set(Pointer<?> pointer, Pointer<X> pointerValue) {
            try {
                ofLong.set(pointer, pointerValue.addr());
            } catch (IllegalAccessException iae) {
                throw new RuntimeException("Access denied", iae);
            }
        }
    }

    /**
     * Reference for arrays.
     */
    public static class OfArray<X> extends OfClass<Array<X>> {

        private final LayoutType<X> elementType;

        @SuppressWarnings({"unchecked", "rawtypes"})
        public OfArray(LayoutType<X> elementType) {
            super((Class) Array.class);
            this.elementType = elementType;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Array<X> get(Pointer<?> pointer) {
            Sequence seq = (Sequence)pointer.type().layout();
            return new BoundedArray<>((BoundedPointer<X>)pointer.cast(elementType), seq.elementsSize());
        }

        @Override
        public void set(Pointer<?> pointer, Array<X> arrayValue) {
            try {
                MethodHandle elemGetter = elementType.getter();
                MethodHandle elemSetter = elementType.setter();
                Pointer<?> toElemPointer = ((Array<?>)pointer.get()).elementPointer();
                Pointer<X> fromElemPointer = arrayValue.elementPointer();
                // @@@ Perform a bulk copy from the array into the pointers
                // memory region
                Sequence seq = ((Sequence)pointer.type().layout());
                int size = seq.elementsSize();
                for (int i = 0 ; i < size ; i++) {
                    Object newVal = elemGetter.invoke(fromElemPointer.offset(i));
                    elemSetter.invoke(toElemPointer.offset(i), newVal);
                }
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    /**
     * A reference for native structs.
     * @param <T> The Java type associated with the native struct.
     */
    public static class OfStruct<T extends Struct<T>> extends OfClass<T> {
        OfStruct(Class<T> carrier) {
            super(carrier);
        }

        @Override
        @SuppressWarnings("unchecked")
        public T get(Pointer<?> pointer) {
            Class<?> structClass = LibrariesHelper.getStructImplClass(carrier);
            try {
                return (T)structClass.getConstructor(Pointer.class).newInstance(pointer);
            } catch (ReflectiveOperationException ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public void set(Pointer<?> pointer, T t) {
            try {
                Util.copy(t.ptr(), pointer, pointer.type().layout().bitsSize() / 8);
            } catch (IllegalAccessException iae) {
                throw new RuntimeException("Access denied", iae);
            }
        }
    }

    /**
     * Reference factory for the {@code char} primitive type.
     */
    public static OfChar ofChar = new OfChar(); 

    /**
     * Reference factory for the {@code boolean} primitive type.
     */
    public static OfBoolean ofBoolean = new OfBoolean();

    /**
     * Reference factory for the {@code byte} primitive type.
     */
    public static OfByte ofByte = new OfByte();

    /**
     * Reference factory for the {@code short} primitive type.
     */
    public static OfShort ofShort = new OfShort();

    /**
     * Reference factory for the {@code int} primitive type.
     */
    public static OfInt ofInt = new OfInt();

    /**
     * Reference factory for the {@code float} primitive type.
     */
    public static OfFloat ofFloat = new OfFloat();

    /**
     * Reference factory for the {@code long} primitive type.
     */
    public static OfLong ofLong = new OfLong();

    /**
     * Reference for the {@code double} primitive type.
     */
    public static OfDouble ofDouble = new OfDouble();

    public static <X> OfPointer<X> ofPointer(LayoutType<X> pointeeType) {
        return new OfPointer<>(pointeeType);
    }

    /**
     * Create an array reference factory from a given carrier class.
     * @param elementType the array carrier.
     * @param <T> the array type.
     * @return a reference factory for array references.
     * @throws IllegalArgumentException if the carrier is not an array type.
     */
    public static <T> OfArray<T> ofArray(LayoutType<T> elementType) { return new OfArray<>(elementType); }

    /**
     * Create a struct reference factory from a given carrier class.
     * @param clazz the native struct carrier.
     * @param <T> the native struct type.
     * @return a reference factory for native struct references.
     * @throws IllegalArgumentException if the carrier is not annotated with the {@link NativeStruct} annotation.
     */
    public static <T extends Struct<T>> OfStruct<T> ofStruct(Class<T> clazz) throws IllegalArgumentException {
        if (!clazz.isAnnotationPresent(NativeStruct.class)) {
            throw new IllegalArgumentException("Not a native struct!");
        }
        return new OfStruct<>(clazz);
    }
}
