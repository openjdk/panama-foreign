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
import java.nio.DoubleBuffer;
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
final class DoubleMaxVector extends DoubleVector {
    static final DoubleSpecies VSPECIES =
        (DoubleSpecies) DoubleVector.SPECIES_MAX;

    static final VectorShape VSHAPE =
        VSPECIES.vectorShape();

    static final Class<DoubleMaxVector> VCLASS = DoubleMaxVector.class;

    static final int VSIZE = VSPECIES.vectorBitSize();

    static final int VLENGTH = VSPECIES.laneCount();

    static final Class<Double> ETYPE = double.class;

    // The JVM expects to find the state here.
    private final double[] vec; // Don't access directly, use getElements() instead.

    DoubleMaxVector(double[] v) {
        vec = v;
    }

    // For compatibility as DoubleMaxVector::new,
    // stored into species.vectorFactory.
    DoubleMaxVector(Object v) {
        this((double[]) v);
    }

    static final DoubleMaxVector ZERO = new DoubleMaxVector(new double[VLENGTH]);
    static final DoubleMaxVector IOTA = new DoubleMaxVector(VSPECIES.iotaArray());

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
    public DoubleSpecies vspecies() {
        // ISSUE:  This should probably be a @Stable
        // field inside AbstractVector, rather than
        // a megamorphic method.
        return VSPECIES;
    }


    /*package-private*/
    @ForceInline
    final @Override
    double[] getElements() {
        return VectorIntrinsics.maybeRebox(this).vec;
    }

    // Virtualized constructors

    @Override
    @ForceInline
    public final DoubleMaxVector broadcast(double e) {
        return (DoubleMaxVector) super.broadcastTemplate(e);  // specialize
    }

    @Override
    @ForceInline
    public final DoubleMaxVector broadcast(long e) {
        return (DoubleMaxVector) super.broadcastTemplate(e);  // specialize
    }

    @Override
    @ForceInline
    DoubleMaxMask maskFromArray(boolean[] bits) {
        return new DoubleMaxMask(bits);
    }

    @Override
    @ForceInline
    DoubleMaxShuffle iotaShuffle() { return DoubleMaxShuffle.IOTA; }

    @Override
    @ForceInline
    DoubleMaxShuffle shuffleFromBytes(byte[] reorder) { return new DoubleMaxShuffle(reorder); }

    @Override
    @ForceInline
    DoubleMaxShuffle shuffleFromArray(int[] indexes, int i) { return new DoubleMaxShuffle(indexes, i); }

    @Override
    @ForceInline
    DoubleMaxShuffle shuffleFromOp(IntUnaryOperator fn) { return new DoubleMaxShuffle(fn); }

    // Make a vector of the same species but the given elements:
    @ForceInline
    final @Override
    DoubleMaxVector vectorFactory(double[] vec) {
        return new DoubleMaxVector(vec);
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
    DoubleMaxVector uOp(FUnOp f) {
        return (DoubleMaxVector) super.uOp(f);  // specialize
    }

    @ForceInline
    final @Override
    DoubleMaxVector uOp(VectorMask<Double> m, FUnOp f) {
        return (DoubleMaxVector) super.uOp((DoubleMaxMask)m, f);  // specialize
    }

    // Binary operator

    @ForceInline
    final @Override
    DoubleMaxVector bOp(Vector<Double> o, FBinOp f) {
        return (DoubleMaxVector) super.bOp((DoubleMaxVector)o, f);  // specialize
    }

    @ForceInline
    final @Override
    DoubleMaxVector bOp(Vector<Double> o,
                     VectorMask<Double> m, FBinOp f) {
        return (DoubleMaxVector) super.bOp((DoubleMaxVector)o, (DoubleMaxMask)m,
                                        f);  // specialize
    }

    // Ternary operator

    @ForceInline
    final @Override
    DoubleMaxVector tOp(Vector<Double> o1, Vector<Double> o2, FTriOp f) {
        return (DoubleMaxVector) super.tOp((DoubleMaxVector)o1, (DoubleMaxVector)o2,
                                        f);  // specialize
    }

    @ForceInline
    final @Override
    DoubleMaxVector tOp(Vector<Double> o1, Vector<Double> o2,
                     VectorMask<Double> m, FTriOp f) {
        return (DoubleMaxVector) super.tOp((DoubleMaxVector)o1, (DoubleMaxVector)o2,
                                        (DoubleMaxMask)m, f);  // specialize
    }

    @ForceInline
    final @Override
    double rOp(double v, FBinOp f) {
        return super.rOp(v, f);  // specialize
    }

    @Override
    @ForceInline
    public final <F>
    Vector<F> convertShape(VectorOperators.Conversion<Double,F> conv,
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
    public DoubleMaxVector lanewise(Unary op) {
        return (DoubleMaxVector) super.lanewiseTemplate(op);  // specialize
    }

    @Override
    @ForceInline
    public DoubleMaxVector lanewise(Binary op, Vector<Double> v) {
        return (DoubleMaxVector) super.lanewiseTemplate(op, v);  // specialize
    }


    /*package-private*/
    @Override
    @ForceInline
    public final
    DoubleMaxVector
    lanewise(VectorOperators.Ternary op, Vector<Double> v1, Vector<Double> v2) {
        return (DoubleMaxVector) super.lanewiseTemplate(op, v1, v2);  // specialize
    }

    @Override
    @ForceInline
    public final
    DoubleMaxVector addIndex(int scale) {
        return (DoubleMaxVector) super.addIndexTemplate(scale);  // specialize
    }

    // Type specific horizontal reductions

    @Override
    @ForceInline
    public final double reduceLanes(VectorOperators.Associative op) {
        return super.reduceLanesTemplate(op);  // specialized
    }

    @Override
    @ForceInline
    public final double reduceLanes(VectorOperators.Associative op,
                                    VectorMask<Double> m) {
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
                                        VectorMask<Double> m) {
        return (long) super.reduceLanesTemplate(op, m);  // specialized
    }

    @Override
    @ForceInline
    public VectorShuffle<Double> toShuffle() {
        double[] a = toArray();
        int[] sa = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            sa[i] = (int) a[i];
        }
        return VectorShuffle.fromArray(VSPECIES, sa, 0);
    }

    // Specialized comparisons

    @Override
    @ForceInline
    public final DoubleMaxMask compare(Comparison op, Vector<Double> v) {
        return super.compareTemplate(DoubleMaxMask.class, op, v);  // specialize
    }

    @Override
    @ForceInline
    public final DoubleMaxMask compare(Comparison op, double s) {
        return super.compareTemplate(DoubleMaxMask.class, op, s);  // specialize
    }

    @Override
    @ForceInline
    public final DoubleMaxMask compare(Comparison op, long s) {
        return super.compareTemplate(DoubleMaxMask.class, op, s);  // specialize
    }

    @Override
    @ForceInline
    public DoubleMaxVector blend(Vector<Double> v, VectorMask<Double> m) {
        return (DoubleMaxVector)
            super.blendTemplate(DoubleMaxMask.class,
                                (DoubleMaxVector) v,
                                (DoubleMaxMask) m);  // specialize
    }

    @Override
    @ForceInline
    public DoubleMaxVector slice(int origin, Vector<Double> v) {
        return (DoubleMaxVector) super.sliceTemplate(origin, v);  // specialize
    }

    @Override
    @ForceInline
    public DoubleMaxVector unslice(int origin, Vector<Double> w, int part) {
        return (DoubleMaxVector) super.unsliceTemplate(origin, w, part);  // specialize
    }

    @Override
    @ForceInline
    public DoubleMaxVector unslice(int origin, Vector<Double> w, int part, VectorMask<Double> m) {
        return (DoubleMaxVector)
            super.unsliceTemplate(DoubleMaxMask.class,
                                  origin, w, part,
                                  (DoubleMaxMask) m);  // specialize
    }

    @Override
    @ForceInline
    public DoubleMaxVector rearrange(VectorShuffle<Double> s) {
        return (DoubleMaxVector)
            super.rearrangeTemplate(DoubleMaxShuffle.class,
                                    (DoubleMaxShuffle) s);  // specialize
    }

    @Override
    @ForceInline
    public DoubleMaxVector rearrange(VectorShuffle<Double> shuffle,
                                  VectorMask<Double> m) {
        return (DoubleMaxVector)
            super.rearrangeTemplate(DoubleMaxShuffle.class,
                                    (DoubleMaxShuffle) shuffle,
                                    (DoubleMaxMask) m);  // specialize
    }

    @Override
    @ForceInline
    public DoubleMaxVector rearrange(VectorShuffle<Double> s,
                                  Vector<Double> v) {
        return (DoubleMaxVector)
            super.rearrangeTemplate(DoubleMaxShuffle.class,
                                    (DoubleMaxShuffle) s,
                                    (DoubleMaxVector) v);  // specialize
    }

    @Override
    @ForceInline
    public DoubleMaxVector selectFrom(Vector<Double> v) {
        return (DoubleMaxVector)
            super.selectFromTemplate((DoubleMaxVector) v);  // specialize
    }

    @Override
    @ForceInline
    public DoubleMaxVector selectFrom(Vector<Double> v,
                                   VectorMask<Double> m) {
        return (DoubleMaxVector)
            super.selectFromTemplate((DoubleMaxVector) v,
                                     (DoubleMaxMask) m);  // specialize
    }


    @Override
    public double lane(int i) {
        if (i < 0 || i >= VLENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + VLENGTH);
        }
        long bits = (long) VectorIntrinsics.extract(
                                VCLASS, ETYPE, VLENGTH,
                                this, i,
                                (vec, ix) -> {
                                    double[] vecarr = vec.getElements();
                                    return (long)Double.doubleToLongBits(vecarr[ix]);
                                });
        return Double.longBitsToDouble(bits);
    }

    @Override
    public DoubleMaxVector withLane(int i, double e) {
        if (i < 0 || i >= VLENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + VLENGTH);
        }
        return VectorIntrinsics.insert(
                                VCLASS, ETYPE, VLENGTH,
                                this, i, (long)Double.doubleToLongBits(e),
                                (v, ix, bits) -> {
                                    double[] res = v.getElements().clone();
                                    res[ix] = Double.longBitsToDouble((long)bits);
                                    return v.vectorFactory(res);
                                });
    }

    // Mask

    static final class DoubleMaxMask extends AbstractMask<Double> {

        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public DoubleMaxMask(boolean[] bits) {
            this(bits, 0);
        }

        public DoubleMaxMask(boolean[] bits, int offset) {
            boolean[] a = new boolean[vspecies().laneCount()];
            for (int i = 0; i < a.length; i++) {
                a[i] = bits[offset + i];
            }
            this.bits = a;
        }

        public DoubleMaxMask(boolean val) {
            boolean[] bits = new boolean[vspecies().laneCount()];
            Arrays.fill(bits, val);
            this.bits = bits;
        }

        @ForceInline
        final @Override
        public DoubleSpecies vspecies() {
            // ISSUE:  This should probably be a @Stable
            // field inside AbstractMask, rather than
            // a megamorphic method.
            return VSPECIES;
        }

        boolean[] getBits() {
            return VectorIntrinsics.maybeRebox(this).bits;
        }

        @Override
        DoubleMaxMask uOp(MUnOp f) {
            boolean[] res = new boolean[vspecies().laneCount()];
            boolean[] bits = getBits();
            for (int i = 0; i < res.length; i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new DoubleMaxMask(res);
        }

        @Override
        DoubleMaxMask bOp(VectorMask<Double> o, MBinOp f) {
            boolean[] res = new boolean[vspecies().laneCount()];
            boolean[] bits = getBits();
            boolean[] mbits = ((DoubleMaxMask)o).getBits();
            for (int i = 0; i < res.length; i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new DoubleMaxMask(res);
        }

        @ForceInline
        @Override
        public final
        DoubleMaxVector toVector() {
            return (DoubleMaxVector) super.toVectorTemplate();  // specialize
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
        public DoubleMaxMask not() {
            return (DoubleMaxMask) VectorIntrinsics.unaryOp(
                                             VECTOR_OP_NOT, DoubleMaxMask.class, long.class, VLENGTH,
                                             this,
                                             (m1) -> m1.uOp((i, a) -> !a));
        }

        // Binary operations

        @Override
        @ForceInline
        public DoubleMaxMask and(VectorMask<Double> o) {
            Objects.requireNonNull(o);
            DoubleMaxMask m = (DoubleMaxMask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, DoubleMaxMask.class, long.class, VLENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public DoubleMaxMask or(VectorMask<Double> o) {
            Objects.requireNonNull(o);
            DoubleMaxMask m = (DoubleMaxMask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, DoubleMaxMask.class, long.class, VLENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(BT_ne, DoubleMaxMask.class, long.class, VLENGTH,
                                         this, this,
                                         (m, __) -> anyTrueHelper(((DoubleMaxMask)m).getBits()));
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(BT_overflow, DoubleMaxMask.class, long.class, VLENGTH,
                                         this, vspecies().maskAll(true),
                                         (m, __) -> allTrueHelper(((DoubleMaxMask)m).getBits()));
        }

        /*package-private*/
        static DoubleMaxMask maskAll(boolean bit) {
            return bit ? TRUE_MASK : FALSE_MASK;
        }
        static final DoubleMaxMask TRUE_MASK = new DoubleMaxMask(true);
        static final DoubleMaxMask FALSE_MASK = new DoubleMaxMask(false);
    }

    // Shuffle

    static final class DoubleMaxShuffle extends AbstractShuffle<Double> {
        DoubleMaxShuffle(byte[] reorder) {
            super(reorder);
        }

        public DoubleMaxShuffle(int[] reorder) {
            super(reorder);
        }

        public DoubleMaxShuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        public DoubleMaxShuffle(IntUnaryOperator fn) {
            super(fn);
        }

        @Override
        public DoubleSpecies vspecies() {
            return VSPECIES;
        }

        static {
            // There must be enough bits in the shuffle lanes to encode
            // VLENGTH valid indexes and VLENGTH exceptional ones.
            assert(VLENGTH < Byte.MAX_VALUE);
            assert(Byte.MIN_VALUE <= -VLENGTH);
        }
        static final DoubleMaxShuffle IOTA = new DoubleMaxShuffle(IDENTITY);

        @Override
        public DoubleMaxVector toVector() {
            return (DoubleMaxVector) super.toVectorTemplate();  // specialize
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
        public DoubleMaxShuffle rearrange(VectorShuffle<Double> o) {
            DoubleMaxShuffle s = (DoubleMaxShuffle) o;
            byte[] r = new byte[reorder.length];
            for (int i = 0; i < reorder.length; i++) {
                int ssi = s.reorder[i];
                r[i] = this.reorder[ssi];  // throws on exceptional index
            }
            return new DoubleMaxShuffle(r);
        }
    }

    // ================================================

    // Specialized low-level memory operations.

    @ForceInline
    @Override
    final
    DoubleVector fromArray0(double[] a, int offset) {
        return super.fromArray0(a, offset);  // specialize
    }

    @ForceInline
    @Override
    final
    DoubleVector fromByteArray0(byte[] a, int offset) {
        return super.fromByteArray0(a, offset);  // specialize
    }

    @ForceInline
    @Override
    final
    DoubleVector fromByteBuffer0(ByteBuffer bb, int offset) {
        return super.fromByteBuffer0(bb, offset);  // specialize
    }

    @ForceInline
    @Override
    final
    void intoArray0(double[] a, int offset) {
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
