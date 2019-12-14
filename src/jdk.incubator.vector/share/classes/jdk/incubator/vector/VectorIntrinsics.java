package jdk.incubator.vector;

import jdk.internal.HotSpotIntrinsicCandidate;
import jdk.internal.misc.Unsafe;
import jdk.internal.vm.annotation.ForceInline;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.*;

/*non-public*/ class VectorIntrinsics {
    static final Unsafe U = Unsafe.getUnsafe();

    static final long BUFFER_ADDRESS
            = U.objectFieldOffset(Buffer.class, "address");

    // Buffer.limit
    static final long BUFFER_LIMIT
            = U.objectFieldOffset(Buffer.class, "limit");

    // ByteBuffer.hb
    static final long BYTE_BUFFER_HB
            = U.objectFieldOffset(ByteBuffer.class, "hb");

    // ByteBuffer.isReadOnly
    static final long BYTE_BUFFER_IS_READ_ONLY
            = U.objectFieldOffset(ByteBuffer.class, "isReadOnly");

    // Kinds of operations
    static final int VECTOR_OPK_UNARY   = 0x001; //(N)N
    static final int VECTOR_OPK_BINARY  = 0x002; //(N,N)N
    static final int VECTOR_OPK_TERNARY = 0x003; //(N,N,N)N
    static final int VECTOR_OPK_INT_A2  = 0x004; //(N,int)N
    static final int VECTOR_OPK_BOOL_R  = 0x008; //(N,N)boolean
    static final int VECTOR_OPK_NO_FP   = 0x010; //N=int,long,byte,short
    static final int VECTOR_OPK_NO_INT  = 0x020; //N=float,double

    // Unary
    static final int VECTOR_OP_ABS  = 0;
    static final int VECTOR_OP_NEG  = 1;
    static final int VECTOR_OP_SQRT = 2;
    static final int VECTOR_OP_NOT  = 3;
    static final int VECTOR_OP_ZOMO = -3;  //FIXME: Implement?

    // Binary
    static final int VECTOR_OP_ADD  = 4;
    static final int VECTOR_OP_SUB  = 5;
    static final int VECTOR_OP_MUL  = 6;
    static final int VECTOR_OP_DIV  = 7;
    static final int VECTOR_OP_MIN  = 8;
    static final int VECTOR_OP_MAX  = 9;
    static final int VECTOR_OP_FIRST_NONZERO = -9;  //a!=0?a:b  FIXME: Implement?

    static final int VECTOR_OP_AND  = 10;
    static final int VECTOR_OP_AND_NOT = -10;  //&~ FIXME: Implement?
    static final int VECTOR_OP_OR   = 11;
    static final int VECTOR_OP_XOR  = 12;
    static final int VECTOR_OP_EQV = -12;  //^~ FIXME: Implement?

    // Ternary
    static final int VECTOR_OP_FMA  = 13;
    static final int VECTOR_OP_BITWISE_BLEND = -13;  //a^((a^b)&c) FIXME: Implement?

    // Broadcast int
    static final int VECTOR_OP_LSHIFT  = 14;
    static final int VECTOR_OP_RSHIFT  = 15;
    static final int VECTOR_OP_URSHIFT = 16;
    static final int VECTOR_OP_LROTATE = 17;
    static final int VECTOR_OP_RROTATE = 18;

    // Math routines
    static final int VECTOR_OP_TAN = 101;
    static final int VECTOR_OP_TANH = 102;
    static final int VECTOR_OP_SIN = 103;
    static final int VECTOR_OP_SINH = 104;
    static final int VECTOR_OP_COS = 105;
    static final int VECTOR_OP_COSH = 106;
    static final int VECTOR_OP_ASIN = 107;
    static final int VECTOR_OP_ACOS = 108;
    static final int VECTOR_OP_ATAN = 109;
    static final int VECTOR_OP_ATAN2 = 110;
    static final int VECTOR_OP_CBRT = 111;
    static final int VECTOR_OP_LOG = 112;
    static final int VECTOR_OP_LOG10 = 113;
    static final int VECTOR_OP_LOG1P = 114;
    static final int VECTOR_OP_POW = 115;
    static final int VECTOR_OP_EXP = 116;
    static final int VECTOR_OP_EXPM1 = 117;
    static final int VECTOR_OP_HYPOT = 118;

    // enum BoolTest
    static final int BT_eq = 0;
    static final int BT_ne = 4;
    static final int BT_le = 5;
    static final int BT_ge = 7;
    static final int BT_lt = 3;
    static final int BT_gt = 1;
    static final int BT_overflow = 2;
    static final int BT_no_overflow = 6;

    // BasicType codes, for primitives only:
    /*package-private*/
    static final int
        T_FLOAT   = 6,
        T_DOUBLE  = 7,
        T_BYTE    = 8,
        T_SHORT   = 9,
        T_INT     = 10,
        T_LONG    = 11;

    /* ============================================================================ */
    interface BroadcastOperation<VM, E, S extends VectorSpecies<E>> {
        VM broadcast(long l, S s);
    }

    @HotSpotIntrinsicCandidate
    static
    <VM, E, S extends VectorSpecies<E>>
    VM broadcastCoerced(Class<? extends VM> vmClass, Class<E> E, int length,
                                  long bits, S s,
                                  BroadcastOperation<VM, E, S> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        return defaultImpl.broadcast(bits, s);
    }

    /* ============================================================================ */
    interface ShuffleIotaOperation<E> {
        VectorShuffle<E> apply(int step, int length);
    }

    @HotSpotIntrinsicCandidate
    static
    <E>
    VectorShuffle<E> shuffleIota(Class<?> E, Class<?> ShuffleClass, VectorSpecies<E> s, int length,
                     int step, ShuffleIotaOperation<E> defaultImpl) {
       assert isNonCapturingLambda(defaultImpl) : defaultImpl;
       return defaultImpl.apply(step, length);
    }

    interface ShuffleToVectorOperation<VM, Sh, E> {
       VM apply(Sh s);
    }

    @HotSpotIntrinsicCandidate
    static
    <VM ,Sh extends VectorShuffle<E>, E>
    VM shuffleToVector(Class<?> VM, Class<?>E , Class<?> ShuffleClass, Sh s, int length,
                       ShuffleToVectorOperation<VM,Sh,E> defaultImpl) {
      assert isNonCapturingLambda(defaultImpl) : defaultImpl;
      return defaultImpl.apply(s);
    }

    /* ============================================================================ */
    interface IndexOperation<V extends Vector<E>, E, S extends VectorSpecies<E>> {
        V index(V v, int step, S s);
    }

    //FIXME @HotSpotIntrinsicCandidate
    static
    <V extends Vector<E>, E, S extends VectorSpecies<E>>
    V indexVector(Class<? extends V> vClass, Class<E> E, int length,
                  V v, int step, S s,
                  IndexOperation<V, E, S> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        return defaultImpl.index(v, step, s);
    }

    /* ============================================================================ */

    @HotSpotIntrinsicCandidate
    static
    <V extends Vector<?>>
    long reductionCoerced(int oprId, Class<?> vectorClass, Class<?> elementType, int length,
                          V v,
                          Function<V,Long> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        return defaultImpl.apply(v);
    }

    /* ============================================================================ */

    interface VecExtractOp<V> {
        long apply(V v1, int idx);
    }

    @HotSpotIntrinsicCandidate
    static
    <V extends Vector<?>>
    long extract(Class<?> vectorClass, Class<?> elementType, int vlen,
                 V vec, int ix,
                 VecExtractOp<V> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        return defaultImpl.apply(vec, ix);
    }

    /* ============================================================================ */

    interface VecInsertOp<V> {
        V apply(V v1, int idx, long val);
    }

    @HotSpotIntrinsicCandidate
    static <V extends Vector<?>>
    V insert(Class<? extends V> vectorClass, Class<?> elementType, int vlen,
                        V vec, int ix, long val,
                        VecInsertOp<V> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        return defaultImpl.apply(vec, ix, val);
    }

    /* ============================================================================ */

    @HotSpotIntrinsicCandidate
    static
    <VM>
    VM unaryOp(int oprId, Class<? extends VM> vmClass, Class<?> elementType, int length,
               VM vm,
               Function<VM, VM> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        return defaultImpl.apply(vm);
    }

    /* ============================================================================ */

    @HotSpotIntrinsicCandidate
    static
    <VM>
    VM binaryOp(int oprId, Class<? extends VM> vmClass, Class<?> elementType, int length,
                VM vm1, VM vm2,
                BiFunction<VM, VM, VM> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        return defaultImpl.apply(vm1, vm2);
    }

    /* ============================================================================ */

    interface TernaryOperation<V> {
        V apply(V v1, V v2, V v3);
    }

    @HotSpotIntrinsicCandidate
    static
    <VM>
    VM ternaryOp(int oprId, Class<? extends VM> vmClass, Class<?> elementType, int length,
                 VM vm1, VM vm2, VM vm3,
                 TernaryOperation<VM> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        return defaultImpl.apply(vm1, vm2, vm3);
    }

    /* ============================================================================ */

    // Memory operations

    interface LoadOperation<C, V, E, S extends VectorSpecies<E>> {
        V load(C container, int index, S s);
    }

    @HotSpotIntrinsicCandidate
    @ForceInline
    static
    <C, VM, E, S extends VectorSpecies<E>>
    VM load(Class<? extends VM> vmClass, Class<E> E, int length,
           Object base, long offset,    // Unsafe addressing
           C container, int index, S s,     // Arguments for default implementation
           LoadOperation<C, VM, E, S> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        return defaultImpl.load(container, index, s);
    }

    /* ============================================================================ */

    interface LoadVectorOperationWithMap<C, V extends Vector<?>, E, S extends VectorSpecies<E>> {
        V loadWithMap(C container, int index, int[] indexMap, int indexM, S s);
    }

    @HotSpotIntrinsicCandidate
    static
    <C, V extends Vector<?>, W extends IntVector, E, S extends VectorSpecies<E>>
    V loadWithMap(Class<?> vectorClass, Class<E> E, int length, Class<?> vectorIndexClass,
                  Object base, long offset, // Unsafe addressing
                  W index_vector,
                  C container, int index, int[] indexMap, int indexM, S s, // Arguments for default implementation
                  LoadVectorOperationWithMap<C, V, E, S> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        return defaultImpl.loadWithMap(container, index, indexMap, indexM, s);
    }

    /* ============================================================================ */

    interface StoreVectorOperation<C, V extends Vector<?>> {
        void store(C container, int index, V v);
    }

    @HotSpotIntrinsicCandidate
    static
    <C, V extends Vector<?>>
    void store(Class<?> vectorClass, Class<?> elementType, int length,
               Object base, long offset,    // Unsafe addressing
               V v,
               C container, int index,      // Arguments for default implementation
               StoreVectorOperation<C, V> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        defaultImpl.store(container, index, v);
    }

    /* ============================================================================ */

    interface StoreVectorOperationWithMap<C, V extends Vector<?>> {
        void storeWithMap(C container, int index, V v, int[] indexMap, int indexM);
    }

    @HotSpotIntrinsicCandidate
    static
    <C, V extends Vector<?>, W extends IntVector>
    void storeWithMap(Class<?> vectorClass, Class<?> elementType, int length, Class<?> vectorIndexClass,
                      Object base, long offset,    // Unsafe addressing
                      W index_vector, V v,
                      C container, int index, int[] indexMap, int indexM, // Arguments for default implementation
                      StoreVectorOperationWithMap<C, V> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        defaultImpl.storeWithMap(container, index, v, indexMap, indexM);
    }

    /* ============================================================================ */

    @HotSpotIntrinsicCandidate
    static
    <VM>
    boolean test(int cond, Class<?> vmClass, Class<?> elementType, int length,
                 VM vm1, VM vm2,
                 BiFunction<VM, VM, Boolean> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        return defaultImpl.apply(vm1, vm2);
    }

    /* ============================================================================ */

    interface VectorCompareOp<V,M> {
        M apply(int cond, V v1, V v2);
    }

    @HotSpotIntrinsicCandidate
    static <V extends Vector<E>,
            M extends VectorMask<E>,
            E>
    M compare(int cond, Class<? extends V> vectorClass, Class<M> maskClass, Class<?> elementType, int length,
              V v1, V v2,
              VectorCompareOp<V,M> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        return defaultImpl.apply(cond, v1, v2);
    }

    /* ============================================================================ */

    interface VectorRearrangeOp<V extends Vector<E>,
            Sh extends VectorShuffle<E>,
            E> {
        V apply(V v1, Sh shuffle);
    }

    @HotSpotIntrinsicCandidate
    static
    <V extends Vector<E>,
            Sh extends VectorShuffle<E>,
            E>
    V rearrangeOp(Class<? extends V> vectorClass, Class<Sh> shuffleClass, Class<?> elementType, int vlen,
            V v1, Sh sh,
            VectorRearrangeOp<V,Sh, E> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        return defaultImpl.apply(v1, sh);
    }

    /* ============================================================================ */

    interface VectorBlendOp<V extends Vector<E>,
            M extends VectorMask<E>,
            E> {
        V apply(V v1, V v2, M mask);
    }

    @HotSpotIntrinsicCandidate
    static
    <V extends Vector<E>,
     M extends VectorMask<E>,
     E>
    V blend(Class<? extends V> vectorClass, Class<M> maskClass, Class<?> elementType, int length,
            V v1, V v2, M m,
            VectorBlendOp<V,M, E> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        return defaultImpl.apply(v1, v2, m);
    }

    /* ============================================================================ */

    interface VectorBroadcastIntOp<V extends Vector<?>> {
        V apply(V v, int n);
    }

    @HotSpotIntrinsicCandidate
    static
    <V extends Vector<?>>
    V broadcastInt(int opr, Class<? extends V> vectorClass, Class<?> elementType, int length,
                   V v, int n,
                   VectorBroadcastIntOp<V> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        return defaultImpl.apply(v, n);
    }

    /* ============================================================================ */

    interface VectorReinterpretOp<VIN, S, V> {
        V apply(VIN v, S species);
    }

    // Users of this intrinsic assume that it respects
    // REGISTER_ENDIAN, which is currently ByteOrder.LITTLE_ENDIAN.
    // See javadoc for REGISTER_ENDIAN.

    @HotSpotIntrinsicCandidate
    static
    <VIN, S, V>
    V reinterpret(Class<?> fromVectorClass,
                  Class<?> fromElementType, int fromVLen,
                  Class<?> toVectorClass,
                  Class<?> toElementType, int toVLen,
                  VIN v, S s,
                  VectorReinterpretOp<VIN, S, V> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        return defaultImpl.apply(v, s);
    }

    /* ============================================================================ */

    // Both reinterpret and cast have the same signature,
    // and they are also used in similar ways.
    // There is something to refactor here!
    // FIXME: Consolidate these intrinsics, and add an
    // opcode parameter to select the various kinds of
    // lanewise casting.  The rebracketing done by
    // reinterpret is not always lanewise (sometimes
    // it is) but it is similar enough to send through
    // a combined intrinsic.
    // https://bugs.openjdk.java.net/browse/JDK-8225740

    interface VectorCastOp<VIN, S, V> {
        V apply(VIN v, S species);
    }

    @HotSpotIntrinsicCandidate
    static
    <VIN, S, V>
    V cast(Class<?> fromVectorClass,
           Class<?> fromElementType, int fromVLen,
           Class<?> toVectorClass,
           Class<?> toElementType, int toVLen,
           VIN v, S s,
           VectorCastOp<VIN, S, V> defaultImpl) {
        assert isNonCapturingLambda(defaultImpl) : defaultImpl;
        return defaultImpl.apply(v, s);
    }

    /* ============================================================================ */

    @HotSpotIntrinsicCandidate
    static <V> V maybeRebox(V v) {
        // The fence is added here to avoid memory aliasing problems in C2 between scalar & vector accesses.
        // TODO: move the fence generation into C2. Generate only when reboxing is taking place.
        U.loadFence();
        return v;
    }

    /* ============================================================================ */

    static final int VECTOR_ACCESS_OOB_CHECK = Integer.getInteger("jdk.incubator.vector.VECTOR_ACCESS_OOB_CHECK", 2);

    @ForceInline
    static void requireLength(int haveLength, int length) {
        if (haveLength != length) {
            throw requireLengthFailed(haveLength, length);
        }
    }
    static IllegalArgumentException requireLengthFailed(int haveLength, int length) {
        String msg = String.format("Length check failed: "+
                                   "length %d should have been %s",
                                   haveLength, length);
        return new IllegalArgumentException(msg);
    }

    @ForceInline
    static int checkFromIndexSize(int ix, int vlen, int length) {
        switch (VectorIntrinsics.VECTOR_ACCESS_OOB_CHECK) {
            case 0: return ix; // no range check
            case 1: return Objects.checkFromIndexSize(ix, vlen, length);
            case 2: return Objects.checkIndex(ix, length - (vlen - 1));
            default: throw new InternalError();
        }
    }

    @ForceInline
    static IntVector checkIndex(IntVector vix, int length) {
        switch (VectorIntrinsics.VECTOR_ACCESS_OOB_CHECK) {
            case 0: return vix; // no range check
            case 1: // fall-through
            case 2:
                if (vix.compare(VectorOperators.LT, 0)
                    .or(vix.compare(VectorOperators.GE, length))
                    .anyTrue()) {
                    throw checkIndexFailed(vix, length);
                }
                return vix;
            default: throw new InternalError();
        }
    }

    private static
    IndexOutOfBoundsException checkIndexFailed(IntVector vix, int length) {
        String msg = String.format("Range check failed: vector %s out of bounds for length %d", vix, length);
        return new IndexOutOfBoundsException(msg);
    }

    static boolean isNonCapturingLambda(Object o) {
        return o.getClass().getDeclaredFields().length == 0;
    }

    // If the index is not already a multiple of size,
    // round it down to the next smaller multiple of size.
    // It is an error if size is less than zero.
    @ForceInline
    static int roundDown(int index, int size) {
        if ((size & (size - 1)) == 0) {
            // Size is zero or a power of two, so we got this.
            return index & ~(size - 1);
        } else {
            return roundDownNPOT(index, size);
        }
    }
    private static int roundDownNPOT(int index, int size) {
        if (index >= 0) {
            return index - (index % size);
        } else {
            return index - Math.floorMod(index, Math.abs(size));
        }
    }
    @ForceInline
    static int wrapToRange(int index, int size) {
        if ((size & (size - 1)) == 0) {
            // Size is zero or a power of two, so we got this.
            return index & (size - 1);
        } else {
            return wrapToRangeNPOT(index, size);
        }
    }
    private static int wrapToRangeNPOT(int index, int size) {
        if (index >= 0) {
            return (index % size);
        } else {
            return Math.floorMod(index, Math.abs(size));
        }
    }

    /* ============================================================================ */

    // query the JVM's supported vector sizes and types

    static int getMaxLaneCount(Class<?> etype) {
        // Note: Unsafe.getMaxVectorSize returns a lane count,
        // not a bit or byte size.
        return U.getMaxVectorSize(etype);
    }


    /*package-private*/
    @ForceInline
    static Object bufferBase(ByteBuffer bb) {
        return U.getReference(bb, BYTE_BUFFER_HB);
    }

    /*package-private*/
    @ForceInline
    static long bufferAddress(ByteBuffer bb, long offset) {
        return U.getLong(bb, BUFFER_ADDRESS) + offset;
    }
}
