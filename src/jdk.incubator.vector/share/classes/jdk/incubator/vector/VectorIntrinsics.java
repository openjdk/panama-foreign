package jdk.incubator.vector;

import jdk.internal.HotSpotIntrinsicCandidate;
import jdk.internal.vm.annotation.ForceInline;

import java.util.Objects;
import java.util.function.*;

/*non-public*/ class VectorIntrinsics {
    static final int VECTOR_OP_ADD = 0;
    static final int VECTOR_OP_SUB = 1;
    static final int VECTOR_OP_MUL = 2;
    static final int VECTOR_OP_DIV = 3;

    static final int VECTOR_OP_AND = 4;
    static final int VECTOR_OP_OR  = 5;
    static final int VECTOR_OP_XOR = 6;

    // Copied from open/src/hotspot/cpu/x86/assembler_x86.hpp
    // enum Condition { // The x86 condition codes used for conditional jumps/moves.
    static final int COND_zero          = 0x4;
    static final int COND_notZero       = 0x5;
    static final int COND_equal         = 0x4;
    static final int COND_notEqual      = 0x5;
    static final int COND_less          = 0xc;
    static final int COND_lessEqual     = 0xe;
    static final int COND_greater       = 0xf;
    static final int COND_greaterEqual  = 0xd;
    static final int COND_below         = 0x2;
    static final int COND_belowEqual    = 0x6;
    static final int COND_above         = 0x7;
    static final int COND_aboveEqual    = 0x3;
    static final int COND_overflow      = 0x0;
    static final int COND_noOverflow    = 0x1;
    static final int COND_carrySet      = 0x2;
    static final int COND_carryClear    = 0x3;
    static final int COND_negative      = 0x8;
    static final int COND_positive      = 0x9;
    static final int COND_parity        = 0xa;
    static final int COND_noParity      = 0xb;

    /* ============================================================================ */

    @HotSpotIntrinsicCandidate
    static <V> V broadcastCoerced(Class<V> vectorClass, Class<?> elementType, int vlen,
                                  long bits,
                                  LongFunction<V> defaultImpl) {
        return defaultImpl.apply(bits);
    }

    @HotSpotIntrinsicCandidate
    static
    <V extends Vector<?,?>>
    long reductionCoerced(int oprId, Class<?> vectorClass, Class<?> elementType, int vlen,
                          V v,
                          Function<V,Long> defaultImpl) {
        return defaultImpl.apply(v);
    }

    /* ============================================================================ */

    @HotSpotIntrinsicCandidate
    static <V> V binaryOp(int oprId, Class<V> vectorClass, Class<?> elementType, int vlen,
                          V v1, V v2, /*Vector.Mask<E,S> m,*/
                          BiFunction<V,V,V> defaultImpl) {
        return defaultImpl.apply(v1, v2);
    }

    /* ============================================================================ */

    // Memory operations

    // FIXME: arrays are erased to Object

    @HotSpotIntrinsicCandidate
    static
    <V extends Vector<?,?>>
    V load(Class<?> vectorClass, Class<?> elementType, int vlen,
           Object array, int index, /* Vector.Mask<E,S> m*/
           BiFunction<Object, Integer, V> defaultImpl) {
        return defaultImpl.apply(array, index);
    }

    interface StoreVectorOperation<V extends Vector<?,?>> {
        void store(Object array, int index, V v);
    }

    @HotSpotIntrinsicCandidate
    static
    <V extends Vector<?,?>>
    void store(Class<?> vectorClass, Class<?> elementType, int vlen,
               Object array, int index, V v, /*Vector.Mask<E,S> m*/
               StoreVectorOperation<V> defaultImpl) {
        defaultImpl.store(array, index, v);
    }

    /* ============================================================================ */

    @HotSpotIntrinsicCandidate
    static <V> boolean test(int cond, Class<?> vectorClass, Class<?> elementType, int vlen,
                            V v1, V v2,
                            BiFunction<V, V, Boolean> defaultImpl) {
        return defaultImpl.apply(v1, v2);
    }

    /* ============================================================================ */

    @HotSpotIntrinsicCandidate
    static <V> V maybeRebox(V v) {
        return v;
    }

    /* ============================================================================ */

    static final int VECTOR_ACCESS_OOB_CHECK = Integer.getInteger("jdk.incubator.vector.VECTOR_ACCESS_OOB_CHECK", 2);

    @ForceInline
    static int checkIndex(int ix, int length, int vlen) {
        switch (VectorIntrinsics.VECTOR_ACCESS_OOB_CHECK) {
            case 0: return ix; // no range check
            case 1: return Objects.checkFromIndexSize(ix, vlen, length);
            case 2: return Objects.checkIndex(ix, length - (vlen - 1));
            default: throw new InternalError();
        }
    }
}
