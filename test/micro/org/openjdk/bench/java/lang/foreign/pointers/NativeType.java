package org.openjdk.bench.java.lang.foreign.pointers;

import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.ValueLayout;

public sealed abstract class NativeType<X> {

    public abstract MemoryLayout layout();

    public non-sealed static abstract class OfInt<X> extends NativeType<X> {
        public abstract ValueLayout.OfInt layout();
    }
    public non-sealed static abstract class OfDouble<X> extends NativeType<X> {
        public abstract ValueLayout.OfDouble layout();
    }

    private static final ValueLayout.OfAddress UNSAFE_ADDRESS = ValueLayout.ADDRESS.asUnbounded();

    public final static class OfPointer<X> extends NativeType<X> {
        public ValueLayout.OfAddress layout() {
            return UNSAFE_ADDRESS;
        }
    }

    public non-sealed static abstract class OfStruct<X> extends NativeType<X> {
        public abstract GroupLayout layout();
        public abstract X make(Pointer<X> ptr);
    }

    public static final OfInt<Integer> C_INT = new OfInt<>() {
        @Override
        public ValueLayout.OfInt layout() {
            return ValueLayout.JAVA_INT;
        }
    };

    public static final OfDouble<Double> C_DOUBLE = new OfDouble<>() {
        @Override
        public ValueLayout.OfDouble layout() {
            return ValueLayout.JAVA_DOUBLE;
        }
    };

    @SuppressWarnings("unchecked")
    final static OfPointer C_VOID_PTR = new OfPointer();

    @SuppressWarnings("unchecked")
    public static final OfPointer<Pointer<Integer>> C_INT_PTR = NativeType.C_VOID_PTR;
    @SuppressWarnings("unchecked")
    public static final OfPointer<Pointer<Double>> C_DOUBLE_PTR = NativeType.C_VOID_PTR;



    @SuppressWarnings("unchecked")
    public static <Z> OfPointer<Pointer<Z>> ptr(NativeType<Z> type) {
        return NativeType.C_VOID_PTR;
    }
}
