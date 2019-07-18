/*
 * Copyright (c) 2017, 2019, Oracle and/or its affiliates. All rights reserved.
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
 * or visit www.oracle.com if you need additional information or have
 * questions.
 */
package jdk.incubator.vector;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.nio.ReadOnlyBufferException;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntUnaryOperator;

import jdk.internal.misc.Unsafe;
import jdk.internal.vm.annotation.ForceInline;
import jdk.internal.vm.annotation.Stable;

import static jdk.incubator.vector.VectorIntrinsics.*;
import static jdk.incubator.vector.VectorOperators.*;

// -- This file was mechanically generated: Do not edit! -- //

@SuppressWarnings("cast")  // warning: redundant cast
final class Long128Vector extends LongVector {
    static final LongSpecies VSPECIES =
        (LongSpecies) LongVector.SPECIES_128;

    static final VectorShape VSHAPE =
        VSPECIES.vectorShape();

    static final Class<Long128Vector> VCLASS = Long128Vector.class;

    static final int VSIZE = VSPECIES.vectorBitSize();

    static final int VLENGTH = VSPECIES.laneCount();

    static final Class<Long> ETYPE = long.class;

    // The JVM expects to find the state here.
    private final long[] vec; // Don't access directly, use getElements() instead.

    Long128Vector(long[] v) {
        vec = v;
    }

    // For compatibility as Long128Vector::new,
    // stored into species.vectorFactory.
    Long128Vector(Object v) {
        this((long[]) v);
    }

    static final Long128Vector ZERO = new Long128Vector(new long[VLENGTH]);
    static final Long128Vector IOTA = new Long128Vector(VSPECIES.iotaArray());

    static {
        // Warm up a few species caches.
        // If we do this too much we will
        // get NPEs from bootstrap circularity.
        VSPECIES.dummyVector();
        VSPECIES.withLanes(LaneType.BYTE);
    }

    // Specialized extractors

    @ForceInline
    final @Override
    public LongSpecies vspecies() {
        // ISSUE:  This should probably be a @Stable
        // field inside AbstractVector, rather than
        // a megamorphic method.
        return VSPECIES;
    }

    @ForceInline
    @Override
    public final Class<Long> elementType() { return Long.class; }

    @ForceInline
    @Override
    public final int elementSize() { return Long.SIZE; }

    @ForceInline
    @Override
    public final VectorShape shape() { return VSHAPE; }

    @ForceInline
    @Override
    public final int length() { return VLENGTH; }

    @ForceInline
    @Override
    public final int bitSize() { return VSIZE; }

    @ForceInline
    @Override
    public final int byteSize() { return VSIZE / Byte.SIZE; }

    /*package-private*/
    @ForceInline
    final @Override
    long[] getElements() {
        return VectorIntrinsics.maybeRebox(this).vec;
    }

    // Virtualized constructors

    @Override
    @ForceInline
    public final Long128Vector broadcast(long e) {
        return (Long128Vector) super.broadcastTemplate(e);  // specialize
    }


    @Override
    @ForceInline
    Long128Mask maskFromArray(boolean[] bits) {
        return new Long128Mask(bits);
    }

    @Override
    @ForceInline
    Long128Shuffle iotaShuffle() { return Long128Shuffle.IOTA; }

    @ForceInline
    Long128Shuffle iotaShuffle(int start) { 
        return (Long128Shuffle)VectorIntrinsics.shuffleIota(ETYPE, Long128Shuffle.class, VSPECIES, VLENGTH, start, (val, l) -> new Long128Shuffle(i -> (Long128Shuffle.partiallyWrapIndex(i + val, l))));
    }

    @Override
    @ForceInline
    Long128Shuffle shuffleFromBytes(byte[] reorder) { return new Long128Shuffle(reorder); }

    @Override
    @ForceInline
    Long128Shuffle shuffleFromArray(int[] indexes, int i) { return new Long128Shuffle(indexes, i); }

    @Override
    @ForceInline
    Long128Shuffle shuffleFromOp(IntUnaryOperator fn) { return new Long128Shuffle(fn); }

    // Make a vector of the same species but the given elements:
    @ForceInline
    final @Override
    Long128Vector vectorFactory(long[] vec) {
        return new Long128Vector(vec);
    }

    @ForceInline
    final @Override
    Byte128Vector asByteVectorRaw() {
        return (Byte128Vector) super.asByteVectorRawTemplate();  // specialize
    }

    @ForceInline
    final @Override
    AbstractVector<?> asVectorRaw(LaneType laneType) {
        return super.asVectorRawTemplate(laneType);  // specialize
    }

    // Unary operator

    final @Override
    Long128Vector uOp(FUnOp f) {
        return (Long128Vector) super.uOpTemplate(f);  // specialize
    }

    @ForceInline
    final @Override
    Long128Vector uOp(VectorMask<Long> m, FUnOp f) {
        return (Long128Vector)
            super.uOpTemplate((Long128Mask)m, f);  // specialize
    }

    // Binary operator

    @ForceInline
    final @Override
    Long128Vector bOp(Vector<Long> v, FBinOp f) {
        return (Long128Vector) super.bOpTemplate((Long128Vector)v, f);  // specialize
    }

    @ForceInline
    final @Override
    Long128Vector bOp(Vector<Long> v,
                     VectorMask<Long> m, FBinOp f) {
        return (Long128Vector)
            super.bOpTemplate((Long128Vector)v, (Long128Mask)m,
                              f);  // specialize
    }

    // Ternary operator

    @ForceInline
    final @Override
    Long128Vector tOp(Vector<Long> v1, Vector<Long> v2, FTriOp f) {
        return (Long128Vector)
            super.tOpTemplate((Long128Vector)v1, (Long128Vector)v2,
                              f);  // specialize
    }

    @ForceInline
    final @Override
    Long128Vector tOp(Vector<Long> v1, Vector<Long> v2,
                     VectorMask<Long> m, FTriOp f) {
        return (Long128Vector)
            super.tOpTemplate((Long128Vector)v1, (Long128Vector)v2,
                              (Long128Mask)m, f);  // specialize
    }

    @ForceInline
    final @Override
    long rOp(long v, FBinOp f) {
        return super.rOpTemplate(v, f);  // specialize
    }

    @Override
    @ForceInline
    public final <F>
    Vector<F> convertShape(VectorOperators.Conversion<Long,F> conv,
                           VectorSpecies<F> rsp, int part) {
        return super.convertShapeTemplate(conv, rsp, part);  // specialize
    }

    @Override
    @ForceInline
    public final <F>
    Vector<F> reinterpretShape(VectorSpecies<F> toSpecies, int part) {
        return super.reinterpretShapeTemplate(toSpecies, part);  // specialize
    }

    // Specialized algebraic operations:

    // The following definition forces a specialized version of this
    // crucial method into the v-table of this class.  A call to add()
    // will inline to a call to lanewise(ADD,), at which point the JIT
    // intrinsic will have the opcode of ADD, plus all the metadata
    // for this particular class, enabling it to generate precise
    // code.
    //
    // There is probably no benefit to the JIT to specialize the
    // masked or broadcast versions of the lanewise method.

    @Override
    @ForceInline
    public Long128Vector lanewise(Unary op) {
        return (Long128Vector) super.lanewiseTemplate(op);  // specialize
    }

    @Override
    @ForceInline
    public Long128Vector lanewise(Binary op, Vector<Long> v) {
        return (Long128Vector) super.lanewiseTemplate(op, v);  // specialize
    }

    /*package-private*/
    @Override
    @ForceInline Long128Vector
    lanewiseShift(VectorOperators.Binary op, int e) {
        return (Long128Vector) super.lanewiseShiftTemplate(op, e);  // specialize
    }

    /*package-private*/
    @Override
    @ForceInline
    public final
    Long128Vector
    lanewise(VectorOperators.Ternary op, Vector<Long> v1, Vector<Long> v2) {
        return (Long128Vector) super.lanewiseTemplate(op, v1, v2);  // specialize
    }

    @Override
    @ForceInline
    public final
    Long128Vector addIndex(int scale) {
        return (Long128Vector) super.addIndexTemplate(scale);  // specialize
    }

    // Type specific horizontal reductions

    @Override
    @ForceInline
    public final long reduceLanes(VectorOperators.Associative op) {
        return super.reduceLanesTemplate(op);  // specialized
    }

    @Override
    @ForceInline
    public final long reduceLanes(VectorOperators.Associative op,
                                    VectorMask<Long> m) {
        return super.reduceLanesTemplate(op, m);  // specialized
    }

    @Override
    @ForceInline
    public final long reduceLanesToLong(VectorOperators.Associative op) {
        return (long) super.reduceLanesTemplate(op);  // specialized
    }

    @Override
    @ForceInline
    public final long reduceLanesToLong(VectorOperators.Associative op,
                                        VectorMask<Long> m) {
        return (long) super.reduceLanesTemplate(op, m);  // specialized
    }

    @Override
    @ForceInline
    public VectorShuffle<Long> toShuffle() {
        long[] a = toArray();
        int[] sa = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            sa[i] = (int) a[i];
        }
        return VectorShuffle.fromArray(VSPECIES, sa, 0);
    }

    // Specialized unary testing

    @Override
    @ForceInline
    public final Long128Mask test(Test op) {
        return super.testTemplate(Long128Mask.class, op);  // specialize
    }

    // Specialized comparisons

    @Override
    @ForceInline
    public final Long128Mask compare(Comparison op, Vector<Long> v) {
        return super.compareTemplate(Long128Mask.class, op, v);  // specialize
    }

    @Override
    @ForceInline
    public final Long128Mask compare(Comparison op, long s) {
        return super.compareTemplate(Long128Mask.class, op, s);  // specialize
    }


    @Override
    @ForceInline
    public Long128Vector blend(Vector<Long> v, VectorMask<Long> m) {
        return (Long128Vector)
            super.blendTemplate(Long128Mask.class,
                                (Long128Vector) v,
                                (Long128Mask) m);  // specialize
    }

    @Override
    @ForceInline
    public Long128Vector slice(int origin, Vector<Long> v) {
        return (Long128Vector) super.sliceTemplate(origin, v);  // specialize
    }

    @Override
    @ForceInline
    public Long128Vector slice(int origin) {
       if ((origin < 0) || (origin >= VLENGTH)) {
         throw new ArrayIndexOutOfBoundsException("Index " + origin + " out of bounds for vector length " + VLENGTH);
       } else {
         Long128Shuffle Iota = iotaShuffle(origin);
         VectorMask<Long> BlendMask = Iota.toVector().compare(VectorOperators.GE, (broadcast((long)(origin))));
         Iota = (Long128Shuffle)iotaShuffle(origin).wrapIndexes();
         return ZERO.blend(this.rearrange(Iota), BlendMask);
       }
    }

    @Override
    @ForceInline
    public Long128Vector unslice(int origin, Vector<Long> w, int part) {
        return (Long128Vector) super.unsliceTemplate(origin, w, part);  // specialize
    }

    @Override
    @ForceInline
    public Long128Vector unslice(int origin, Vector<Long> w, int part, VectorMask<Long> m) {
        return (Long128Vector)
            super.unsliceTemplate(Long128Mask.class,
                                  origin, w, part,
                                  (Long128Mask) m);  // specialize
    }

    @Override
    @ForceInline
    public Long128Vector unslice(int origin) {
       if ((origin < 0) || (origin >= VLENGTH)) {
         throw new ArrayIndexOutOfBoundsException("Index " + origin + " out of bounds for vector length " + VLENGTH);
       } else {
         Long128Shuffle Iota = iotaShuffle(-origin);
         VectorMask<Long> BlendMask = Iota.toVector().compare(VectorOperators.GE, (broadcast((long)(0))));
         Iota = (Long128Shuffle)iotaShuffle(-origin).wrapIndexes();
         return ZERO.blend(this.rearrange(Iota), BlendMask);
       }
    }

    @Override
    @ForceInline
    public Long128Vector rearrange(VectorShuffle<Long> s) {
        return (Long128Vector)
            super.rearrangeTemplate(Long128Shuffle.class,
                                    (Long128Shuffle) s);  // specialize
    }

    @Override
    @ForceInline
    public Long128Vector rearrange(VectorShuffle<Long> shuffle,
                                  VectorMask<Long> m) {
        return (Long128Vector)
            super.rearrangeTemplate(Long128Shuffle.class,
                                    (Long128Shuffle) shuffle,
                                    (Long128Mask) m);  // specialize
    }

    @Override
    @ForceInline
    public Long128Vector rearrange(VectorShuffle<Long> s,
                                  Vector<Long> v) {
        return (Long128Vector)
            super.rearrangeTemplate(Long128Shuffle.class,
                                    (Long128Shuffle) s,
                                    (Long128Vector) v);  // specialize
    }

    @Override
    @ForceInline
    public Long128Vector selectFrom(Vector<Long> v) {
        return (Long128Vector)
            super.selectFromTemplate((Long128Vector) v);  // specialize
    }

    @Override
    @ForceInline
    public Long128Vector selectFrom(Vector<Long> v,
                                   VectorMask<Long> m) {
        return (Long128Vector)
            super.selectFromTemplate((Long128Vector) v,
                                     (Long128Mask) m);  // specialize
    }


    @Override
    public long lane(int i) {
        if (i < 0 || i >= VLENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + VLENGTH);
        }
        return (long) VectorIntrinsics.extract(
                                VCLASS, ETYPE, VLENGTH,
                                this, i,
                                (vec, ix) -> {
                                    long[] vecarr = vec.getElements();
                                    return (long)vecarr[ix];
                                });
    }

    @Override
    public Long128Vector withLane(int i, long e) {
        if (i < 0 || i >= VLENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + VLENGTH);
        }
        return VectorIntrinsics.insert(
                                VCLASS, ETYPE, VLENGTH,
                                this, i, (long)e,
                                (v, ix, bits) -> {
                                    long[] res = v.getElements().clone();
                                    res[ix] = (long)bits;
                                    return v.vectorFactory(res);
                                });
    }

    // Mask

    static final class Long128Mask extends AbstractMask<Long> {

        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public Long128Mask(boolean[] bits) {
            this(bits, 0);
        }

        public Long128Mask(boolean[] bits, int offset) {
            boolean[] a = new boolean[vspecies().laneCount()];
            for (int i = 0; i < a.length; i++) {
                a[i] = bits[offset + i];
            }
            this.bits = a;
        }

        public Long128Mask(boolean val) {
            boolean[] bits = new boolean[vspecies().laneCount()];
            Arrays.fill(bits, val);
            this.bits = bits;
        }

        @ForceInline
        final @Override
        public LongSpecies vspecies() {
            // ISSUE:  This should probably be a @Stable
            // field inside AbstractMask, rather than
            // a megamorphic method.
            return VSPECIES;
        }

        boolean[] getBits() {
            return VectorIntrinsics.maybeRebox(this).bits;
        }

        @Override
        Long128Mask uOp(MUnOp f) {
            boolean[] res = new boolean[vspecies().laneCount()];
            boolean[] bits = getBits();
            for (int i = 0; i < res.length; i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Long128Mask(res);
        }

        @Override
        Long128Mask bOp(VectorMask<Long> m, MBinOp f) {
            boolean[] res = new boolean[vspecies().laneCount()];
            boolean[] bits = getBits();
            boolean[] mbits = ((Long128Mask)m).getBits();
            for (int i = 0; i < res.length; i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new Long128Mask(res);
        }

        @ForceInline
        @Override
        public final
        Long128Vector toVector() {
            return (Long128Vector) super.toVectorTemplate();  // specialize
        }

        @Override
        @ForceInline
        public <E> VectorMask<E> cast(VectorSpecies<E> s) {
            AbstractSpecies<E> species = (AbstractSpecies<E>) s;
            if (length() != species.laneCount())
                throw new IllegalArgumentException("VectorMask length and species length differ");
            boolean[] maskArray = toArray();
            // enum-switches don't optimize properly JDK-8161245
            switch (species.laneType.switchKey) {
            case LaneType.SK_BYTE:
                return new Byte128Vector.Byte128Mask(maskArray).check(species);
            case LaneType.SK_SHORT:
                return new Short128Vector.Short128Mask(maskArray).check(species);
            case LaneType.SK_INT:
                return new Int128Vector.Int128Mask(maskArray).check(species);
            case LaneType.SK_LONG:
                return new Long128Vector.Long128Mask(maskArray).check(species);
            case LaneType.SK_FLOAT:
                return new Float128Vector.Float128Mask(maskArray).check(species);
            case LaneType.SK_DOUBLE:
                return new Double128Vector.Double128Mask(maskArray).check(species);
            }

            // Should not reach here.
            throw new AssertionError(species);
        }

        // Unary operations

        @Override
        @ForceInline
        public Long128Mask not() {
            return (Long128Mask) VectorIntrinsics.unaryOp(
                                             VECTOR_OP_NOT, Long128Mask.class, long.class, VLENGTH,
                                             this,
                                             (m1) -> m1.uOp((i, a) -> !a));
        }

        // Binary operations

        @Override
        @ForceInline
        public Long128Mask and(VectorMask<Long> mask) {
            Objects.requireNonNull(mask);
            Long128Mask m = (Long128Mask)mask;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Long128Mask.class, long.class, VLENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Long128Mask or(VectorMask<Long> mask) {
            Objects.requireNonNull(mask);
            Long128Mask m = (Long128Mask)mask;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, Long128Mask.class, long.class, VLENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(BT_ne, Long128Mask.class, long.class, VLENGTH,
                                         this, this,
                                         (m, __) -> anyTrueHelper(((Long128Mask)m).getBits()));
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(BT_overflow, Long128Mask.class, long.class, VLENGTH,
                                         this, vspecies().maskAll(true),
                                         (m, __) -> allTrueHelper(((Long128Mask)m).getBits()));
        }

        /*package-private*/
        static Long128Mask maskAll(boolean bit) {
            return bit ? TRUE_MASK : FALSE_MASK;
        }
        static final Long128Mask TRUE_MASK = new Long128Mask(true);
        static final Long128Mask FALSE_MASK = new Long128Mask(false);
    }

    // Shuffle

    static final class Long128Shuffle extends AbstractShuffle<Long> {
        Long128Shuffle(byte[] reorder) {
            super(reorder);
        }

        public Long128Shuffle(int[] reorder) {
            super(reorder);
        }

        public Long128Shuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        public Long128Shuffle(IntUnaryOperator fn) {
            super(fn);
        }

        @Override
        public LongSpecies vspecies() {
            return VSPECIES;
        }

        static {
            // There must be enough bits in the shuffle lanes to encode
            // VLENGTH valid indexes and VLENGTH exceptional ones.
            assert(VLENGTH < Byte.MAX_VALUE);
            assert(Byte.MIN_VALUE <= -VLENGTH);
        }
        static final Long128Shuffle IOTA = new Long128Shuffle(IDENTITY);

        @Override
        @ForceInline
        public Long128Vector toVector() {
            return VectorIntrinsics.shuffleToVector(VCLASS, ETYPE, Long128Shuffle.class, this, VLENGTH,
                                                    (s) -> ((Long128Vector)(((AbstractShuffle<Long>)(s)).toVectorTemplate())));
        }

        @Override
        @ForceInline
        public <F> VectorShuffle<F> cast(VectorSpecies<F> s) {
            AbstractSpecies<F> species = (AbstractSpecies<F>) s;
            if (length() != species.laneCount())
                throw new IllegalArgumentException("VectorShuffle length and species length differ");
            int[] shuffleArray = toArray();
            // enum-switches don't optimize properly JDK-8161245
            switch (species.laneType.switchKey) {
            case LaneType.SK_BYTE:
                return new Byte128Vector.Byte128Shuffle(shuffleArray).check(species);
            case LaneType.SK_SHORT:
                return new Short128Vector.Short128Shuffle(shuffleArray).check(species);
            case LaneType.SK_INT:
                return new Int128Vector.Int128Shuffle(shuffleArray).check(species);
            case LaneType.SK_LONG:
                return new Long128Vector.Long128Shuffle(shuffleArray).check(species);
            case LaneType.SK_FLOAT:
                return new Float128Vector.Float128Shuffle(shuffleArray).check(species);
            case LaneType.SK_DOUBLE:
                return new Double128Vector.Double128Shuffle(shuffleArray).check(species);
            }

            // Should not reach here.
            throw new AssertionError(species);
        }

        @Override
        public Long128Shuffle rearrange(VectorShuffle<Long> shuffle) {
            Long128Shuffle s = (Long128Shuffle) shuffle;
            byte[] r = new byte[reorder.length];
            for (int i = 0; i < reorder.length; i++) {
                int ssi = s.reorder[i];
                r[i] = this.reorder[ssi];  // throws on exceptional index
            }
            return new Long128Shuffle(r);
        }
    }

    // ================================================

    // Specialized low-level memory operations.

    @ForceInline
    @Override
    final
    LongVector fromArray0(long[] a, int offset) {
        return super.fromArray0Template(a, offset);  // specialize
    }

    @ForceInline
    @Override
    final
    LongVector fromByteArray0(byte[] a, int offset) {
        return super.fromByteArray0Template(a, offset);  // specialize
    }

    @ForceInline
    @Override
    final
    LongVector fromByteBuffer0(ByteBuffer bb, int offset) {
        return super.fromByteBuffer0Template(bb, offset);  // specialize
    }

    @ForceInline
    @Override
    final
    void intoArray0(long[] a, int offset) {
        super.intoArray0Template(a, offset);  // specialize
    }

    @ForceInline
    @Override
    final
    void intoByteArray0(byte[] a, int offset) {
        super.intoByteArray0Template(a, offset);  // specialize
    }

    // End of specialized low-level memory operations.

    // ================================================

}
