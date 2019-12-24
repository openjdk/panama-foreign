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
import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntUnaryOperator;

import jdk.internal.vm.annotation.ForceInline;

import static jdk.incubator.vector.VectorIntrinsics.*;
import static jdk.incubator.vector.VectorOperators.*;

// -- This file was mechanically generated: Do not edit! -- //

@SuppressWarnings("cast")  // warning: redundant cast
final class Double512Vector extends DoubleVector {
    static final DoubleSpecies VSPECIES =
        (DoubleSpecies) DoubleVector.SPECIES_512;

    static final VectorShape VSHAPE =
        VSPECIES.vectorShape();

    static final Class<Double512Vector> VCLASS = Double512Vector.class;

    static final int VSIZE = VSPECIES.vectorBitSize();

    static final int VLENGTH = VSPECIES.laneCount();

    static final Class<Double> ETYPE = double.class;

    // The JVM expects to find the state here.
    private final double[] vec; // Don't access directly, use vec() instead.

    Double512Vector(double[] v) {
        vec = v;
    }

    // For compatibility as Double512Vector::new,
    // stored into species.vectorFactory.
    Double512Vector(Object v) {
        this((double[]) v);
    }

    static final Double512Vector ZERO = new Double512Vector(new double[VLENGTH]);
    static final Double512Vector IOTA = new Double512Vector(VSPECIES.iotaArray());

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

    @ForceInline
    @Override
    public final Class<Double> elementType() { return double.class; }

    @ForceInline
    @Override
    public final int elementSize() { return Double.SIZE; }

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
    double[] vec() {
        return VectorIntrinsics.maybeRebox(this).vec;
    }

    // Virtualized constructors

    @Override
    @ForceInline
    public final Double512Vector broadcast(double e) {
        return (Double512Vector) super.broadcastTemplate(e);  // specialize
    }

    @Override
    @ForceInline
    public final Double512Vector broadcast(long e) {
        return (Double512Vector) super.broadcastTemplate(e);  // specialize
    }

    @Override
    @ForceInline
    Double512Mask maskFromArray(boolean[] bits) {
        return new Double512Mask(bits);
    }

    @Override
    @ForceInline
    Double512Shuffle iotaShuffle() { return Double512Shuffle.IOTA; }

    @ForceInline
    Double512Shuffle iotaShuffle(int start, int step, boolean wrap) {
      if (wrap) {
        return (Double512Shuffle)VectorIntrinsics.shuffleIota(ETYPE, Double512Shuffle.class, VSPECIES, VLENGTH, start, step, 1, (l, lstart, lstep) -> new Double512Shuffle(i -> (VectorIntrinsics.wrapToRange(i*lstep + lstart, l))));
      } else {
        return (Double512Shuffle)VectorIntrinsics.shuffleIota(ETYPE, Double512Shuffle.class, VSPECIES, VLENGTH, start, step, 0, (l, lstart, lstep) -> new Double512Shuffle(i -> (i*lstep + lstart)));
      }
    }

    @Override
    @ForceInline
    Double512Shuffle shuffleFromBytes(byte[] reorder) { return new Double512Shuffle(reorder); }

    @Override
    @ForceInline
    Double512Shuffle shuffleFromArray(int[] indexes, int i) { return new Double512Shuffle(indexes, i); }

    @Override
    @ForceInline
    Double512Shuffle shuffleFromOp(IntUnaryOperator fn) { return new Double512Shuffle(fn); }

    // Make a vector of the same species but the given elements:
    @ForceInline
    final @Override
    Double512Vector vectorFactory(double[] vec) {
        return new Double512Vector(vec);
    }

    @ForceInline
    final @Override
    Byte512Vector asByteVectorRaw() {
        return (Byte512Vector) super.asByteVectorRawTemplate();  // specialize
    }

    @ForceInline
    final @Override
    AbstractVector<?> asVectorRaw(LaneType laneType) {
        return super.asVectorRawTemplate(laneType);  // specialize
    }

    // Unary operator

    final @Override
    Double512Vector uOp(FUnOp f) {
        return (Double512Vector) super.uOpTemplate(f);  // specialize
    }

    @ForceInline
    final @Override
    Double512Vector uOp(VectorMask<Double> m, FUnOp f) {
        return (Double512Vector)
            super.uOpTemplate((Double512Mask)m, f);  // specialize
    }

    // Binary operator

    @ForceInline
    final @Override
    Double512Vector bOp(Vector<Double> v, FBinOp f) {
        return (Double512Vector) super.bOpTemplate((Double512Vector)v, f);  // specialize
    }

    @ForceInline
    final @Override
    Double512Vector bOp(Vector<Double> v,
                     VectorMask<Double> m, FBinOp f) {
        return (Double512Vector)
            super.bOpTemplate((Double512Vector)v, (Double512Mask)m,
                              f);  // specialize
    }

    // Ternary operator

    @ForceInline
    final @Override
    Double512Vector tOp(Vector<Double> v1, Vector<Double> v2, FTriOp f) {
        return (Double512Vector)
            super.tOpTemplate((Double512Vector)v1, (Double512Vector)v2,
                              f);  // specialize
    }

    @ForceInline
    final @Override
    Double512Vector tOp(Vector<Double> v1, Vector<Double> v2,
                     VectorMask<Double> m, FTriOp f) {
        return (Double512Vector)
            super.tOpTemplate((Double512Vector)v1, (Double512Vector)v2,
                              (Double512Mask)m, f);  // specialize
    }

    @ForceInline
    final @Override
    double rOp(double v, FBinOp f) {
        return super.rOpTemplate(v, f);  // specialize
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
    public Double512Vector lanewise(Unary op) {
        return (Double512Vector) super.lanewiseTemplate(op);  // specialize
    }

    @Override
    @ForceInline
    public Double512Vector lanewise(Binary op, Vector<Double> v) {
        return (Double512Vector) super.lanewiseTemplate(op, v);  // specialize
    }


    /*package-private*/
    @Override
    @ForceInline
    public final
    Double512Vector
    lanewise(VectorOperators.Ternary op, Vector<Double> v1, Vector<Double> v2) {
        return (Double512Vector) super.lanewiseTemplate(op, v1, v2);  // specialize
    }

    @Override
    @ForceInline
    public final
    Double512Vector addIndex(int scale) {
        return (Double512Vector) super.addIndexTemplate(scale);  // specialize
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

    // Specialized unary testing

    @Override
    @ForceInline
    public final Double512Mask test(Test op) {
        return super.testTemplate(Double512Mask.class, op);  // specialize
    }

    // Specialized comparisons

    @Override
    @ForceInline
    public final Double512Mask compare(Comparison op, Vector<Double> v) {
        return super.compareTemplate(Double512Mask.class, op, v);  // specialize
    }

    @Override
    @ForceInline
    public final Double512Mask compare(Comparison op, double s) {
        return super.compareTemplate(Double512Mask.class, op, s);  // specialize
    }

    @Override
    @ForceInline
    public final Double512Mask compare(Comparison op, long s) {
        return super.compareTemplate(Double512Mask.class, op, s);  // specialize
    }

    @Override
    @ForceInline
    public Double512Vector blend(Vector<Double> v, VectorMask<Double> m) {
        return (Double512Vector)
            super.blendTemplate(Double512Mask.class,
                                (Double512Vector) v,
                                (Double512Mask) m);  // specialize
    }

    @Override
    @ForceInline
    public Double512Vector slice(int origin, Vector<Double> v) {
        return (Double512Vector) super.sliceTemplate(origin, v);  // specialize
    }

    @Override
    @ForceInline
    public Double512Vector slice(int origin) {
       if ((origin < 0) || (origin >= VLENGTH)) {
         throw new ArrayIndexOutOfBoundsException("Index " + origin + " out of bounds for vector length " + VLENGTH);
       } else {
         Double512Shuffle Iota = (Double512Shuffle)VectorShuffle.iota(VSPECIES, 0, 1, true);
         VectorMask<Double> BlendMask = Iota.toVector().compare(VectorOperators.LT, (broadcast((double)(VLENGTH-origin))));
         Iota = (Double512Shuffle)VectorShuffle.iota(VSPECIES, origin, 1, true);
         return ZERO.blend(this.rearrange(Iota), BlendMask);
       }
    }

    @Override
    @ForceInline
    public Double512Vector unslice(int origin, Vector<Double> w, int part) {
        return (Double512Vector) super.unsliceTemplate(origin, w, part);  // specialize
    }

    @Override
    @ForceInline
    public Double512Vector unslice(int origin, Vector<Double> w, int part, VectorMask<Double> m) {
        return (Double512Vector)
            super.unsliceTemplate(Double512Mask.class,
                                  origin, w, part,
                                  (Double512Mask) m);  // specialize
    }

    @Override
    @ForceInline
    public Double512Vector unslice(int origin) {
       if ((origin < 0) || (origin >= VLENGTH)) {
         throw new ArrayIndexOutOfBoundsException("Index " + origin + " out of bounds for vector length " + VLENGTH);
       } else {
         Double512Shuffle Iota = (Double512Shuffle)VectorShuffle.iota(VSPECIES, 0, 1, true);
         VectorMask<Double> BlendMask = Iota.toVector().compare(VectorOperators.GE, (broadcast((double)(origin))));
         Iota = (Double512Shuffle)VectorShuffle.iota(VSPECIES, -origin, 1, true);
         return ZERO.blend(this.rearrange(Iota), BlendMask);
       }
    }

    @Override
    @ForceInline
    public Double512Vector rearrange(VectorShuffle<Double> s) {
        return (Double512Vector)
            super.rearrangeTemplate(Double512Shuffle.class,
                                    (Double512Shuffle) s);  // specialize
    }

    @Override
    @ForceInline
    public Double512Vector rearrange(VectorShuffle<Double> shuffle,
                                  VectorMask<Double> m) {
        return (Double512Vector)
            super.rearrangeTemplate(Double512Shuffle.class,
                                    (Double512Shuffle) shuffle,
                                    (Double512Mask) m);  // specialize
    }

    @Override
    @ForceInline
    public Double512Vector rearrange(VectorShuffle<Double> s,
                                  Vector<Double> v) {
        return (Double512Vector)
            super.rearrangeTemplate(Double512Shuffle.class,
                                    (Double512Shuffle) s,
                                    (Double512Vector) v);  // specialize
    }

    @Override
    @ForceInline
    public Double512Vector selectFrom(Vector<Double> v) {
        return (Double512Vector)
            super.selectFromTemplate((Double512Vector) v);  // specialize
    }

    @Override
    @ForceInline
    public Double512Vector selectFrom(Vector<Double> v,
                                   VectorMask<Double> m) {
        return (Double512Vector)
            super.selectFromTemplate((Double512Vector) v,
                                     (Double512Mask) m);  // specialize
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
                                    double[] vecarr = vec.vec();
                                    return (long)Double.doubleToLongBits(vecarr[ix]);
                                });
        return Double.longBitsToDouble(bits);
    }

    @Override
    public Double512Vector withLane(int i, double e) {
        if (i < 0 || i >= VLENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + VLENGTH);
        }
        return VectorIntrinsics.insert(
                                VCLASS, ETYPE, VLENGTH,
                                this, i, (long)Double.doubleToLongBits(e),
                                (v, ix, bits) -> {
                                    double[] res = v.vec().clone();
                                    res[ix] = Double.longBitsToDouble((long)bits);
                                    return v.vectorFactory(res);
                                });
    }

    // Mask

    static final class Double512Mask extends AbstractMask<Double> {

        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public Double512Mask(boolean[] bits) {
            this(bits, 0);
        }

        public Double512Mask(boolean[] bits, int offset) {
            boolean[] a = new boolean[vspecies().laneCount()];
            for (int i = 0; i < a.length; i++) {
                a[i] = bits[offset + i];
            }
            this.bits = a;
        }

        public Double512Mask(boolean val) {
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
        Double512Mask uOp(MUnOp f) {
            boolean[] res = new boolean[vspecies().laneCount()];
            boolean[] bits = getBits();
            for (int i = 0; i < res.length; i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Double512Mask(res);
        }

        @Override
        Double512Mask bOp(VectorMask<Double> m, MBinOp f) {
            boolean[] res = new boolean[vspecies().laneCount()];
            boolean[] bits = getBits();
            boolean[] mbits = ((Double512Mask)m).getBits();
            for (int i = 0; i < res.length; i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new Double512Mask(res);
        }

        @ForceInline
        @Override
        public final
        Double512Vector toVector() {
            return (Double512Vector) super.toVectorTemplate();  // specialize
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
                return new Byte512Vector.Byte512Mask(maskArray).check(species);
            case LaneType.SK_SHORT:
                return new Short512Vector.Short512Mask(maskArray).check(species);
            case LaneType.SK_INT:
                return new Int512Vector.Int512Mask(maskArray).check(species);
            case LaneType.SK_LONG:
                return new Long512Vector.Long512Mask(maskArray).check(species);
            case LaneType.SK_FLOAT:
                return new Float512Vector.Float512Mask(maskArray).check(species);
            case LaneType.SK_DOUBLE:
                return new Double512Vector.Double512Mask(maskArray).check(species);
            }

            // Should not reach here.
            throw new AssertionError(species);
        }

        // Unary operations

        @Override
        @ForceInline
        public Double512Mask not() {
            return (Double512Mask) VectorIntrinsics.unaryOp(
                                             VECTOR_OP_NOT, Double512Mask.class, long.class, VLENGTH,
                                             this,
                                             (m1) -> m1.uOp((i, a) -> !a));
        }

        // Binary operations

        @Override
        @ForceInline
        public Double512Mask and(VectorMask<Double> mask) {
            Objects.requireNonNull(mask);
            Double512Mask m = (Double512Mask)mask;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Double512Mask.class, long.class, VLENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Double512Mask or(VectorMask<Double> mask) {
            Objects.requireNonNull(mask);
            Double512Mask m = (Double512Mask)mask;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, Double512Mask.class, long.class, VLENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(BT_ne, Double512Mask.class, long.class, VLENGTH,
                                         this, this,
                                         (m, __) -> anyTrueHelper(((Double512Mask)m).getBits()));
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(BT_overflow, Double512Mask.class, long.class, VLENGTH,
                                         this, vspecies().maskAll(true),
                                         (m, __) -> allTrueHelper(((Double512Mask)m).getBits()));
        }

        /*package-private*/
        static Double512Mask maskAll(boolean bit) {
            return bit ? TRUE_MASK : FALSE_MASK;
        }
        static final Double512Mask TRUE_MASK = new Double512Mask(true);
        static final Double512Mask FALSE_MASK = new Double512Mask(false);
    }

    // Shuffle

    static final class Double512Shuffle extends AbstractShuffle<Double> {
        Double512Shuffle(byte[] reorder) {
            super(reorder);
        }

        public Double512Shuffle(int[] reorder) {
            super(reorder);
        }

        public Double512Shuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        public Double512Shuffle(IntUnaryOperator fn) {
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
        static final Double512Shuffle IOTA = new Double512Shuffle(IDENTITY);

        @Override
        @ForceInline
        public Double512Vector toVector() {
            return VectorIntrinsics.shuffleToVector(VCLASS, ETYPE, Double512Shuffle.class, this, VLENGTH,
                                                    (s) -> ((Double512Vector)(((AbstractShuffle<Double>)(s)).toVectorTemplate())));
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
                return new Byte512Vector.Byte512Shuffle(shuffleArray).check(species);
            case LaneType.SK_SHORT:
                return new Short512Vector.Short512Shuffle(shuffleArray).check(species);
            case LaneType.SK_INT:
                return new Int512Vector.Int512Shuffle(shuffleArray).check(species);
            case LaneType.SK_LONG:
                return new Long512Vector.Long512Shuffle(shuffleArray).check(species);
            case LaneType.SK_FLOAT:
                return new Float512Vector.Float512Shuffle(shuffleArray).check(species);
            case LaneType.SK_DOUBLE:
                return new Double512Vector.Double512Shuffle(shuffleArray).check(species);
            }

            // Should not reach here.
            throw new AssertionError(species);
        }

        @Override
        public Double512Shuffle rearrange(VectorShuffle<Double> shuffle) {
            Double512Shuffle s = (Double512Shuffle) shuffle;
            byte[] r = new byte[reorder.length];
            for (int i = 0; i < reorder.length; i++) {
                int ssi = s.reorder[i];
                r[i] = this.reorder[ssi];  // throws on exceptional index
            }
            return new Double512Shuffle(r);
        }
    }

    // ================================================

    // Specialized low-level memory operations.

    @ForceInline
    @Override
    final
    DoubleVector fromArray0(double[] a, int offset) {
        return super.fromArray0Template(a, offset);  // specialize
    }

    @ForceInline
    @Override
    final
    DoubleVector fromByteArray0(byte[] a, int offset) {
        return super.fromByteArray0Template(a, offset);  // specialize
    }

    @ForceInline
    @Override
    final
    DoubleVector fromByteBuffer0(ByteBuffer bb, int offset) {
        return super.fromByteBuffer0Template(bb, offset);  // specialize
    }

    @ForceInline
    @Override
    final
    void intoArray0(double[] a, int offset) {
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
