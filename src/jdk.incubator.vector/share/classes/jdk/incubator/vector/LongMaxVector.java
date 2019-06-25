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
final class LongMaxVector extends LongVector {
    static final LongSpecies VSPECIES =
        (LongSpecies) LongVector.SPECIES_MAX;

    static final VectorShape VSHAPE =
        VSPECIES.vectorShape();

    static final Class<LongMaxVector> VCLASS = LongMaxVector.class;

    static final int VSIZE = VSPECIES.vectorBitSize();

    static final int VLENGTH = VSPECIES.laneCount();

    static final Class<Long> ETYPE = long.class;

    // The JVM expects to find the state here.
    private final long[] vec; // Don't access directly, use getElements() instead.

    LongMaxVector(long[] v) {
        vec = v;
    }

    // For compatibility as LongMaxVector::new,
    // stored into species.vectorFactory.
    LongMaxVector(Object v) {
        this((long[]) v);
    }

    static final LongMaxVector ZERO = new LongMaxVector(new long[VLENGTH]);
    static final LongMaxVector IOTA = new LongMaxVector(VSPECIES.iotaArray());

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


    /*package-private*/
    @ForceInline
    final @Override
    long[] getElements() {
        return VectorIntrinsics.maybeRebox(this).vec;
    }

    // Virtualized constructors

    @Override
    @ForceInline
    public final LongMaxVector broadcast(long e) {
        return (LongMaxVector) super.broadcastTemplate(e);  // specialize
    }


    @Override
    @ForceInline
    LongMaxMask maskFromArray(boolean[] bits) {
        return new LongMaxMask(bits);
    }

    @Override
    @ForceInline
    LongMaxShuffle iotaShuffle() { return LongMaxShuffle.IOTA; }

    @ForceInline
    LongMaxShuffle iotaShuffle(int start) { 
        return (LongMaxShuffle)VectorIntrinsics.shuffleIota(ETYPE, LongMaxShuffle.class, VSPECIES, VLENGTH, start, (val, l) -> new LongMaxShuffle(i -> ((i + val) & (l-1))));
    }

    @Override
    @ForceInline
    LongMaxShuffle shuffleFromBytes(byte[] reorder) { return new LongMaxShuffle(reorder); }

    @Override
    @ForceInline
    LongMaxShuffle shuffleFromArray(int[] indexes, int i) { return new LongMaxShuffle(indexes, i); }

    @Override
    @ForceInline
    LongMaxShuffle shuffleFromOp(IntUnaryOperator fn) { return new LongMaxShuffle(fn); }

    // Make a vector of the same species but the given elements:
    @ForceInline
    final @Override
    LongMaxVector vectorFactory(long[] vec) {
        return new LongMaxVector(vec);
    }

    @ForceInline
    final @Override
    ByteMaxVector asByteVectorRaw() {
        return (ByteMaxVector) super.asByteVectorRawTemplate();  // specialize
    }

    @ForceInline
    final @Override
    AbstractVector<?> asVectorRaw(LaneType laneType) {
        return super.asVectorRawTemplate(laneType);  // specialize
    }

    // Unary operator

    final @Override
    LongMaxVector uOp(FUnOp f) {
        return (LongMaxVector) super.uOp(f);  // specialize
    }

    @ForceInline
    final @Override
    LongMaxVector uOp(VectorMask<Long> m, FUnOp f) {
        return (LongMaxVector) super.uOp((LongMaxMask)m, f);  // specialize
    }

    // Binary operator

    @ForceInline
    final @Override
    LongMaxVector bOp(Vector<Long> v, FBinOp f) {
        return (LongMaxVector) super.bOp((LongMaxVector)v, f);  // specialize
    }

    @ForceInline
    final @Override
    LongMaxVector bOp(Vector<Long> v,
                     VectorMask<Long> m, FBinOp f) {
        return (LongMaxVector) super.bOp((LongMaxVector)v, (LongMaxMask)m,
                                        f);  // specialize
    }

    // Ternary operator

    @ForceInline
    final @Override
    LongMaxVector tOp(Vector<Long> v1, Vector<Long> v2, FTriOp f) {
        return (LongMaxVector) super.tOp((LongMaxVector)v1, (LongMaxVector)v2,
                                        f);  // specialize
    }

    @ForceInline
    final @Override
    LongMaxVector tOp(Vector<Long> v1, Vector<Long> v2,
                     VectorMask<Long> m, FTriOp f) {
        return (LongMaxVector) super.tOp((LongMaxVector)v1, (LongMaxVector)v2,
                                        (LongMaxMask)m, f);  // specialize
    }

    @ForceInline
    final @Override
    long rOp(long v, FBinOp f) {
        return super.rOp(v, f);  // specialize
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
    public LongMaxVector lanewise(Unary op) {
        return (LongMaxVector) super.lanewiseTemplate(op);  // specialize
    }

    @Override
    @ForceInline
    public LongMaxVector lanewise(Binary op, Vector<Long> v) {
        return (LongMaxVector) super.lanewiseTemplate(op, v);  // specialize
    }

    /*package-private*/
    @Override
    @ForceInline LongMaxVector
    lanewiseShift(VectorOperators.Binary op, int e) {
        return (LongMaxVector) super.lanewiseShiftTemplate(op, e);  // specialize
    }

    /*package-private*/
    @Override
    @ForceInline
    public final
    LongMaxVector
    lanewise(VectorOperators.Ternary op, Vector<Long> v1, Vector<Long> v2) {
        return (LongMaxVector) super.lanewiseTemplate(op, v1, v2);  // specialize
    }

    @Override
    @ForceInline
    public final
    LongMaxVector addIndex(int scale) {
        return (LongMaxVector) super.addIndexTemplate(scale);  // specialize
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

    // Specialized comparisons

    @Override
    @ForceInline
    public final LongMaxMask compare(Comparison op, Vector<Long> v) {
        return super.compareTemplate(LongMaxMask.class, op, v);  // specialize
    }

    @Override
    @ForceInline
    public final LongMaxMask compare(Comparison op, long s) {
        return super.compareTemplate(LongMaxMask.class, op, s);  // specialize
    }


    @Override
    @ForceInline
    public LongMaxVector blend(Vector<Long> v, VectorMask<Long> m) {
        return (LongMaxVector)
            super.blendTemplate(LongMaxMask.class,
                                (LongMaxVector) v,
                                (LongMaxMask) m);  // specialize
    }

    @Override
    @ForceInline
    public LongMaxVector slice(int origin, Vector<Long> v) {
        return (LongMaxVector) super.sliceTemplate(origin, v);  // specialize
    }

    @Override
    @ForceInline
    public LongMaxVector unslice(int origin, Vector<Long> w, int part) {
        return (LongMaxVector) super.unsliceTemplate(origin, w, part);  // specialize
    }

    @Override
    @ForceInline
    public LongMaxVector unslice(int origin, Vector<Long> w, int part, VectorMask<Long> m) {
        return (LongMaxVector)
            super.unsliceTemplate(LongMaxMask.class,
                                  origin, w, part,
                                  (LongMaxMask) m);  // specialize
    }

    @Override
    @ForceInline
    public LongMaxVector rearrange(VectorShuffle<Long> s) {
        return (LongMaxVector)
            super.rearrangeTemplate(LongMaxShuffle.class,
                                    (LongMaxShuffle) s);  // specialize
    }

    @Override
    @ForceInline
    public LongMaxVector rearrange(VectorShuffle<Long> shuffle,
                                  VectorMask<Long> m) {
        return (LongMaxVector)
            super.rearrangeTemplate(LongMaxShuffle.class,
                                    (LongMaxShuffle) shuffle,
                                    (LongMaxMask) m);  // specialize
    }

    @Override
    @ForceInline
    public LongMaxVector rearrange(VectorShuffle<Long> s,
                                  Vector<Long> v) {
        return (LongMaxVector)
            super.rearrangeTemplate(LongMaxShuffle.class,
                                    (LongMaxShuffle) s,
                                    (LongMaxVector) v);  // specialize
    }

    @Override
    @ForceInline
    public LongMaxVector selectFrom(Vector<Long> v) {
        return (LongMaxVector)
            super.selectFromTemplate((LongMaxVector) v);  // specialize
    }

    @Override
    @ForceInline
    public LongMaxVector selectFrom(Vector<Long> v,
                                   VectorMask<Long> m) {
        return (LongMaxVector)
            super.selectFromTemplate((LongMaxVector) v,
                                     (LongMaxMask) m);  // specialize
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
    public LongMaxVector withLane(int i, long e) {
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

    static final class LongMaxMask extends AbstractMask<Long> {

        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public LongMaxMask(boolean[] bits) {
            this(bits, 0);
        }

        public LongMaxMask(boolean[] bits, int offset) {
            boolean[] a = new boolean[vspecies().laneCount()];
            for (int i = 0; i < a.length; i++) {
                a[i] = bits[offset + i];
            }
            this.bits = a;
        }

        public LongMaxMask(boolean val) {
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
        LongMaxMask uOp(MUnOp f) {
            boolean[] res = new boolean[vspecies().laneCount()];
            boolean[] bits = getBits();
            for (int i = 0; i < res.length; i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new LongMaxMask(res);
        }

        @Override
        LongMaxMask bOp(VectorMask<Long> m, MBinOp f) {
            boolean[] res = new boolean[vspecies().laneCount()];
            boolean[] bits = getBits();
            boolean[] mbits = ((LongMaxMask)m).getBits();
            for (int i = 0; i < res.length; i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new LongMaxMask(res);
        }

        @ForceInline
        @Override
        public final
        LongMaxVector toVector() {
            return (LongMaxVector) super.toVectorTemplate();  // specialize
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
                return new ByteMaxVector.ByteMaxMask(maskArray).check(species);
            case LaneType.SK_SHORT:
                return new ShortMaxVector.ShortMaxMask(maskArray).check(species);
            case LaneType.SK_INT:
                return new IntMaxVector.IntMaxMask(maskArray).check(species);
            case LaneType.SK_LONG:
                return new LongMaxVector.LongMaxMask(maskArray).check(species);
            case LaneType.SK_FLOAT:
                return new FloatMaxVector.FloatMaxMask(maskArray).check(species);
            case LaneType.SK_DOUBLE:
                return new DoubleMaxVector.DoubleMaxMask(maskArray).check(species);
            }

            // Should not reach here.
            throw new AssertionError(species);
        }

        // Unary operations

        @Override
        @ForceInline
        public LongMaxMask not() {
            return (LongMaxMask) VectorIntrinsics.unaryOp(
                                             VECTOR_OP_NOT, LongMaxMask.class, long.class, VLENGTH,
                                             this,
                                             (m1) -> m1.uOp((i, a) -> !a));
        }

        // Binary operations

        @Override
        @ForceInline
        public LongMaxMask and(VectorMask<Long> mask) {
            Objects.requireNonNull(mask);
            LongMaxMask m = (LongMaxMask)mask;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, LongMaxMask.class, long.class, VLENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public LongMaxMask or(VectorMask<Long> mask) {
            Objects.requireNonNull(mask);
            LongMaxMask m = (LongMaxMask)mask;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, LongMaxMask.class, long.class, VLENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(BT_ne, LongMaxMask.class, long.class, VLENGTH,
                                         this, this,
                                         (m, __) -> anyTrueHelper(((LongMaxMask)m).getBits()));
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(BT_overflow, LongMaxMask.class, long.class, VLENGTH,
                                         this, vspecies().maskAll(true),
                                         (m, __) -> allTrueHelper(((LongMaxMask)m).getBits()));
        }

        /*package-private*/
        static LongMaxMask maskAll(boolean bit) {
            return bit ? TRUE_MASK : FALSE_MASK;
        }
        static final LongMaxMask TRUE_MASK = new LongMaxMask(true);
        static final LongMaxMask FALSE_MASK = new LongMaxMask(false);
    }

    // Shuffle

    static final class LongMaxShuffle extends AbstractShuffle<Long> {
        LongMaxShuffle(byte[] reorder) {
            super(reorder);
        }

        public LongMaxShuffle(int[] reorder) {
            super(reorder);
        }

        public LongMaxShuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        public LongMaxShuffle(IntUnaryOperator fn) {
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
        static final LongMaxShuffle IOTA = new LongMaxShuffle(IDENTITY);

        private LongMaxVector toVector_helper() {
            return (LongMaxVector) super.toVectorTemplate();  // specialize
        }

        @Override
        @ForceInline
        public LongMaxVector toVector() {
            return VectorIntrinsics.shuffleToVector(VCLASS, ETYPE, LongMaxShuffle.class, this, VLENGTH,
                                                    (s) -> (s.toVector_helper()));
        }

        @Override
        @ForceInline
        public <F> VectorShuffle<F> cast(VectorSpecies<F> s) {
            AbstractSpecies<F> species = (AbstractSpecies<F>) s;
            if (length() != species.laneCount())
                throw new AssertionError("NYI: Shuffle length and species length differ");
            int[] shuffleArray = toArray();
            // enum-switches don't optimize properly JDK-8161245
            switch (species.laneType.switchKey) {
            case LaneType.SK_BYTE:
                return new ByteMaxVector.ByteMaxShuffle(shuffleArray).check(species);
            case LaneType.SK_SHORT:
                return new ShortMaxVector.ShortMaxShuffle(shuffleArray).check(species);
            case LaneType.SK_INT:
                return new IntMaxVector.IntMaxShuffle(shuffleArray).check(species);
            case LaneType.SK_LONG:
                return new LongMaxVector.LongMaxShuffle(shuffleArray).check(species);
            case LaneType.SK_FLOAT:
                return new FloatMaxVector.FloatMaxShuffle(shuffleArray).check(species);
            case LaneType.SK_DOUBLE:
                return new DoubleMaxVector.DoubleMaxShuffle(shuffleArray).check(species);
            }

            // Should not reach here.
            throw new AssertionError(species);
        }

        @Override
        public LongMaxShuffle rearrange(VectorShuffle<Long> shuffle) {
            LongMaxShuffle s = (LongMaxShuffle) shuffle;
            byte[] r = new byte[reorder.length];
            for (int i = 0; i < reorder.length; i++) {
                int ssi = s.reorder[i];
                r[i] = this.reorder[ssi];  // throws on exceptional index
            }
            return new LongMaxShuffle(r);
        }
    }

    // ================================================

    // Specialized low-level memory operations.

    @ForceInline
    @Override
    final
    LongVector fromArray0(long[] a, int offset) {
        return super.fromArray0(a, offset);  // specialize
    }

    @ForceInline
    @Override
    final
    LongVector fromByteArray0(byte[] a, int offset) {
        return super.fromByteArray0(a, offset);  // specialize
    }

    @ForceInline
    @Override
    final
    LongVector fromByteBuffer0(ByteBuffer bb, int offset) {
        return super.fromByteBuffer0(bb, offset);  // specialize
    }

    @ForceInline
    @Override
    final
    void intoArray0(long[] a, int offset) {
        super.intoArray0(a, offset);  // specialize
    }

    @ForceInline
    @Override
    final
    void intoByteArray0(byte[] a, int offset) {
        super.intoByteArray0(a, offset);  // specialize
    }

    // End of specialized low-level memory operations.

    // ================================================

}
