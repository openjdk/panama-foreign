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
final class Double128Vector extends DoubleVector {
    static final DoubleSpecies VSPECIES =
        (DoubleSpecies) DoubleVector.SPECIES_128;

    static final VectorShape VSHAPE =
        VSPECIES.vectorShape();

    static final Class<Double128Vector> VCLASS = Double128Vector.class;

    static final int VSIZE = VSPECIES.vectorBitSize();

    static final int VLENGTH = VSPECIES.laneCount();

    static final Class<Double> ETYPE = double.class;

    // The JVM expects to find the state here.
    private final double[] vec; // Don't access directly, use vec() instead.

    Double128Vector(double[] v) {
        vec = v;
    }

    // For compatibility as Double128Vector::new,
    // stored into species.vectorFactory.
    Double128Vector(Object v) {
        this((double[]) v);
    }

    static final Double128Vector ZERO = new Double128Vector(new double[VLENGTH]);
    static final Double128Vector IOTA = new Double128Vector(VSPECIES.iotaArray());

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
    public final Double128Vector broadcast(double e) {
        return (Double128Vector) super.broadcastTemplate(e);  // specialize
    }

    @Override
    @ForceInline
    public final Double128Vector broadcast(long e) {
        return (Double128Vector) super.broadcastTemplate(e);  // specialize
    }

    @Override
    @ForceInline
    Double128Mask maskFromArray(boolean[] bits) {
        return new Double128Mask(bits);
    }

    @Override
    @ForceInline
    Double128Shuffle iotaShuffle() { return Double128Shuffle.IOTA; }

    @ForceInline
    Double128Shuffle iotaShuffle(int start, int step, boolean wrap) {
      if (wrap) {
        return (Double128Shuffle)VectorIntrinsics.shuffleIota(ETYPE, Double128Shuffle.class, VSPECIES, VLENGTH, start, step, 1, (l, lstart, lstep) -> new Double128Shuffle(i -> (VectorIntrinsics.wrapToRange(i*lstep + lstart, l))));
      } else {
        return (Double128Shuffle)VectorIntrinsics.shuffleIota(ETYPE, Double128Shuffle.class, VSPECIES, VLENGTH, start, step, 0, (l, lstart, lstep) -> new Double128Shuffle(i -> (i*lstep + lstart)));
      }
    }

    @Override
    @ForceInline
    Double128Shuffle shuffleFromBytes(byte[] reorder) { return new Double128Shuffle(reorder); }

    @Override
    @ForceInline
    Double128Shuffle shuffleFromArray(int[] indexes, int i) { return new Double128Shuffle(indexes, i); }

    @Override
    @ForceInline
    Double128Shuffle shuffleFromOp(IntUnaryOperator fn) { return new Double128Shuffle(fn); }

    // Make a vector of the same species but the given elements:
    @ForceInline
    final @Override
    Double128Vector vectorFactory(double[] vec) {
        return new Double128Vector(vec);
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
    Double128Vector uOp(FUnOp f) {
        return (Double128Vector) super.uOpTemplate(f);  // specialize
    }

    @ForceInline
    final @Override
    Double128Vector uOp(VectorMask<Double> m, FUnOp f) {
        return (Double128Vector)
            super.uOpTemplate((Double128Mask)m, f);  // specialize
    }

    // Binary operator

    @ForceInline
    final @Override
    Double128Vector bOp(Vector<Double> v, FBinOp f) {
        return (Double128Vector) super.bOpTemplate((Double128Vector)v, f);  // specialize
    }

    @ForceInline
    final @Override
    Double128Vector bOp(Vector<Double> v,
                     VectorMask<Double> m, FBinOp f) {
        return (Double128Vector)
            super.bOpTemplate((Double128Vector)v, (Double128Mask)m,
                              f);  // specialize
    }

    // Ternary operator

    @ForceInline
    final @Override
    Double128Vector tOp(Vector<Double> v1, Vector<Double> v2, FTriOp f) {
        return (Double128Vector)
            super.tOpTemplate((Double128Vector)v1, (Double128Vector)v2,
                              f);  // specialize
    }

    @ForceInline
    final @Override
    Double128Vector tOp(Vector<Double> v1, Vector<Double> v2,
                     VectorMask<Double> m, FTriOp f) {
        return (Double128Vector)
            super.tOpTemplate((Double128Vector)v1, (Double128Vector)v2,
                              (Double128Mask)m, f);  // specialize
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
    public Double128Vector lanewise(Unary op) {
        return (Double128Vector) super.lanewiseTemplate(op);  // specialize
    }

    @Override
    @ForceInline
    public Double128Vector lanewise(Binary op, Vector<Double> v) {
        return (Double128Vector) super.lanewiseTemplate(op, v);  // specialize
    }


    /*package-private*/
    @Override
    @ForceInline
    public final
    Double128Vector
    lanewise(VectorOperators.Ternary op, Vector<Double> v1, Vector<Double> v2) {
        return (Double128Vector) super.lanewiseTemplate(op, v1, v2);  // specialize
    }

    @Override
    @ForceInline
    public final
    Double128Vector addIndex(int scale) {
        return (Double128Vector) super.addIndexTemplate(scale);  // specialize
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
    public final Double128Mask test(Test op) {
        return super.testTemplate(Double128Mask.class, op);  // specialize
    }

    // Specialized comparisons

    @Override
    @ForceInline
    public final Double128Mask compare(Comparison op, Vector<Double> v) {
        return super.compareTemplate(Double128Mask.class, op, v);  // specialize
    }

    @Override
    @ForceInline
    public final Double128Mask compare(Comparison op, double s) {
        return super.compareTemplate(Double128Mask.class, op, s);  // specialize
    }

    @Override
    @ForceInline
    public final Double128Mask compare(Comparison op, long s) {
        return super.compareTemplate(Double128Mask.class, op, s);  // specialize
    }

    @Override
    @ForceInline
    public Double128Vector blend(Vector<Double> v, VectorMask<Double> m) {
        return (Double128Vector)
            super.blendTemplate(Double128Mask.class,
                                (Double128Vector) v,
                                (Double128Mask) m);  // specialize
    }

    @Override
    @ForceInline
    public Double128Vector slice(int origin, Vector<Double> v) {
        return (Double128Vector) super.sliceTemplate(origin, v);  // specialize
    }

    @Override
    @ForceInline
    public Double128Vector slice(int origin) {
       if ((origin < 0) || (origin >= VLENGTH)) {
         throw new ArrayIndexOutOfBoundsException("Index " + origin + " out of bounds for vector length " + VLENGTH);
       } else {
         Double128Shuffle Iota = (Double128Shuffle)VectorShuffle.iota(VSPECIES, 0, 1, true);
         VectorMask<Double> BlendMask = Iota.toVector().compare(VectorOperators.LT, (broadcast((double)(VLENGTH-origin))));
         Iota = (Double128Shuffle)VectorShuffle.iota(VSPECIES, origin, 1, true);
         return ZERO.blend(this.rearrange(Iota), BlendMask);
       }
    }

    @Override
    @ForceInline
    public Double128Vector unslice(int origin, Vector<Double> w, int part) {
        return (Double128Vector) super.unsliceTemplate(origin, w, part);  // specialize
    }

    @Override
    @ForceInline
    public Double128Vector unslice(int origin, Vector<Double> w, int part, VectorMask<Double> m) {
        return (Double128Vector)
            super.unsliceTemplate(Double128Mask.class,
                                  origin, w, part,
                                  (Double128Mask) m);  // specialize
    }

    @Override
    @ForceInline
    public Double128Vector unslice(int origin) {
       if ((origin < 0) || (origin >= VLENGTH)) {
         throw new ArrayIndexOutOfBoundsException("Index " + origin + " out of bounds for vector length " + VLENGTH);
       } else {
         Double128Shuffle Iota = (Double128Shuffle)VectorShuffle.iota(VSPECIES, 0, 1, true);
         VectorMask<Double> BlendMask = Iota.toVector().compare(VectorOperators.GE, (broadcast((double)(origin))));
         Iota = (Double128Shuffle)VectorShuffle.iota(VSPECIES, -origin, 1, true);
         return ZERO.blend(this.rearrange(Iota), BlendMask);
       }
    }

    @Override
    @ForceInline
    public Double128Vector rearrange(VectorShuffle<Double> s) {
        return (Double128Vector)
            super.rearrangeTemplate(Double128Shuffle.class,
                                    (Double128Shuffle) s);  // specialize
    }

    @Override
    @ForceInline
    public Double128Vector rearrange(VectorShuffle<Double> shuffle,
                                  VectorMask<Double> m) {
        return (Double128Vector)
            super.rearrangeTemplate(Double128Shuffle.class,
                                    (Double128Shuffle) shuffle,
                                    (Double128Mask) m);  // specialize
    }

    @Override
    @ForceInline
    public Double128Vector rearrange(VectorShuffle<Double> s,
                                  Vector<Double> v) {
        return (Double128Vector)
            super.rearrangeTemplate(Double128Shuffle.class,
                                    (Double128Shuffle) s,
                                    (Double128Vector) v);  // specialize
    }

    @Override
    @ForceInline
    public Double128Vector selectFrom(Vector<Double> v) {
        return (Double128Vector)
            super.selectFromTemplate((Double128Vector) v);  // specialize
    }

    @Override
    @ForceInline
    public Double128Vector selectFrom(Vector<Double> v,
                                   VectorMask<Double> m) {
        return (Double128Vector)
            super.selectFromTemplate((Double128Vector) v,
                                     (Double128Mask) m);  // specialize
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
    public Double128Vector withLane(int i, double e) {
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

    static final class Double128Mask extends AbstractMask<Double> {

        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public Double128Mask(boolean[] bits) {
            this(bits, 0);
        }

        public Double128Mask(boolean[] bits, int offset) {
            boolean[] a = new boolean[vspecies().laneCount()];
            for (int i = 0; i < a.length; i++) {
                a[i] = bits[offset + i];
            }
            this.bits = a;
        }

        public Double128Mask(boolean val) {
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
        Double128Mask uOp(MUnOp f) {
            boolean[] res = new boolean[vspecies().laneCount()];
            boolean[] bits = getBits();
            for (int i = 0; i < res.length; i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Double128Mask(res);
        }

        @Override
        Double128Mask bOp(VectorMask<Double> m, MBinOp f) {
            boolean[] res = new boolean[vspecies().laneCount()];
            boolean[] bits = getBits();
            boolean[] mbits = ((Double128Mask)m).getBits();
            for (int i = 0; i < res.length; i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new Double128Mask(res);
        }

        @ForceInline
        @Override
        public final
        Double128Vector toVector() {
            return (Double128Vector) super.toVectorTemplate();  // specialize
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
        public Double128Mask not() {
            return (Double128Mask) VectorIntrinsics.unaryOp(
                                             VECTOR_OP_NOT, Double128Mask.class, long.class, VLENGTH,
                                             this,
                                             (m1) -> m1.uOp((i, a) -> !a));
        }

        // Binary operations

        @Override
        @ForceInline
        public Double128Mask and(VectorMask<Double> mask) {
            Objects.requireNonNull(mask);
            Double128Mask m = (Double128Mask)mask;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Double128Mask.class, long.class, VLENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Double128Mask or(VectorMask<Double> mask) {
            Objects.requireNonNull(mask);
            Double128Mask m = (Double128Mask)mask;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, Double128Mask.class, long.class, VLENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(BT_ne, Double128Mask.class, long.class, VLENGTH,
                                         this, this,
                                         (m, __) -> anyTrueHelper(((Double128Mask)m).getBits()));
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(BT_overflow, Double128Mask.class, long.class, VLENGTH,
                                         this, vspecies().maskAll(true),
                                         (m, __) -> allTrueHelper(((Double128Mask)m).getBits()));
        }

        /*package-private*/
        static Double128Mask maskAll(boolean bit) {
            return bit ? TRUE_MASK : FALSE_MASK;
        }
        static final Double128Mask TRUE_MASK = new Double128Mask(true);
        static final Double128Mask FALSE_MASK = new Double128Mask(false);
    }

    // Shuffle

    static final class Double128Shuffle extends AbstractShuffle<Double> {
        Double128Shuffle(byte[] reorder) {
            super(reorder);
        }

        public Double128Shuffle(int[] reorder) {
            super(reorder);
        }

        public Double128Shuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        public Double128Shuffle(IntUnaryOperator fn) {
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
        static final Double128Shuffle IOTA = new Double128Shuffle(IDENTITY);

        @Override
        @ForceInline
        public Double128Vector toVector() {
            return VectorIntrinsics.shuffleToVector(VCLASS, ETYPE, Double128Shuffle.class, this, VLENGTH,
                                                    (s) -> ((Double128Vector)(((AbstractShuffle<Double>)(s)).toVectorTemplate())));
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
        public Double128Shuffle rearrange(VectorShuffle<Double> shuffle) {
            Double128Shuffle s = (Double128Shuffle) shuffle;
            byte[] r = new byte[reorder.length];
            for (int i = 0; i < reorder.length; i++) {
                int ssi = s.reorder[i];
                r[i] = this.reorder[ssi];  // throws on exceptional index
            }
            return new Double128Shuffle(r);
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
