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
final class Double256Vector extends DoubleVector {
    static final DoubleSpecies VSPECIES =
        (DoubleSpecies) DoubleVector.SPECIES_256;

    static final VectorShape VSHAPE =
        VSPECIES.vectorShape();

    static final Class<Double256Vector> VCLASS = Double256Vector.class;

    static final int VSIZE = VSPECIES.vectorBitSize();

    static final int VLENGTH = VSPECIES.laneCount();

    static final Class<Double> ETYPE = double.class;

    // The JVM expects to find the state here.
    private final double[] vec; // Don't access directly, use getElements() instead.

    Double256Vector(double[] v) {
        vec = v;
    }

    // For compatibility as Double256Vector::new,
    // stored into species.vectorFactory.
    Double256Vector(Object v) {
        this((double[]) v);
    }

    static final Double256Vector ZERO = new Double256Vector(new double[VLENGTH]);
    static final Double256Vector IOTA = new Double256Vector(VSPECIES.iotaArray());

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
    public final Double256Vector broadcast(double e) {
        return (Double256Vector) super.broadcastTemplate(e);  // specialize
    }

    @Override
    @ForceInline
    public final Double256Vector broadcast(long e) {
        return (Double256Vector) super.broadcastTemplate(e);  // specialize
    }

    @Override
    @ForceInline
    Double256Mask maskFromArray(boolean[] bits) {
        return new Double256Mask(bits);
    }

    @Override
    @ForceInline
    Double256Shuffle iotaShuffle() { return Double256Shuffle.IOTA; }

    @Override
    @ForceInline
    Double256Shuffle shuffleFromBytes(byte[] reorder) { return new Double256Shuffle(reorder); }

    @Override
    @ForceInline
    Double256Shuffle shuffleFromArray(int[] indexes, int i) { return new Double256Shuffle(indexes, i); }

    @Override
    @ForceInline
    Double256Shuffle shuffleFromOp(IntUnaryOperator fn) { return new Double256Shuffle(fn); }

    // Make a vector of the same species but the given elements:
    @ForceInline
    final @Override
    Double256Vector vectorFactory(double[] vec) {
        return new Double256Vector(vec);
    }

    @ForceInline
    final @Override
    Byte256Vector asByteVectorRaw() {
        return (Byte256Vector) super.asByteVectorRawTemplate();  // specialize
    }

    @ForceInline
    final @Override
    AbstractVector<?> asVectorRaw(LaneType laneType) {
        return super.asVectorRawTemplate(laneType);  // specialize
    }

    // Unary operator

    final @Override
    Double256Vector uOp(FUnOp f) {
        return (Double256Vector) super.uOp(f);  // specialize
    }

    @ForceInline
    final @Override
    Double256Vector uOp(VectorMask<Double> m, FUnOp f) {
        return (Double256Vector) super.uOp((Double256Mask)m, f);  // specialize
    }

    // Binary operator

    @ForceInline
    final @Override
    Double256Vector bOp(Vector<Double> o, FBinOp f) {
        return (Double256Vector) super.bOp((Double256Vector)o, f);  // specialize
    }

    @ForceInline
    final @Override
    Double256Vector bOp(Vector<Double> o,
                     VectorMask<Double> m, FBinOp f) {
        return (Double256Vector) super.bOp((Double256Vector)o, (Double256Mask)m,
                                        f);  // specialize
    }

    // Ternary operator

    @ForceInline
    final @Override
    Double256Vector tOp(Vector<Double> o1, Vector<Double> o2, FTriOp f) {
        return (Double256Vector) super.tOp((Double256Vector)o1, (Double256Vector)o2,
                                        f);  // specialize
    }

    @ForceInline
    final @Override
    Double256Vector tOp(Vector<Double> o1, Vector<Double> o2,
                     VectorMask<Double> m, FTriOp f) {
        return (Double256Vector) super.tOp((Double256Vector)o1, (Double256Vector)o2,
                                        (Double256Mask)m, f);  // specialize
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
    public Double256Vector lanewise(Unary op) {
        return (Double256Vector) super.lanewiseTemplate(op);  // specialize
    }

    @Override
    @ForceInline
    public Double256Vector lanewise(Binary op, Vector<Double> v) {
        return (Double256Vector) super.lanewiseTemplate(op, v);  // specialize
    }


    /*package-private*/
    @Override
    @ForceInline
    public final
    Double256Vector
    lanewise(VectorOperators.Ternary op, Vector<Double> v1, Vector<Double> v2) {
        return (Double256Vector) super.lanewiseTemplate(op, v1, v2);  // specialize
    }

    @Override
    @ForceInline
    public final
    Double256Vector addIndex(int scale) {
        return (Double256Vector) super.addIndexTemplate(scale);  // specialize
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
    public final Double256Mask compare(Comparison op, Vector<Double> v) {
        return super.compareTemplate(Double256Mask.class, op, v);  // specialize
    }

    @Override
    @ForceInline
    public final Double256Mask compare(Comparison op, double s) {
        return super.compareTemplate(Double256Mask.class, op, s);  // specialize
    }

    @Override
    @ForceInline
    public final Double256Mask compare(Comparison op, long s) {
        return super.compareTemplate(Double256Mask.class, op, s);  // specialize
    }

    @Override
    @ForceInline
    public Double256Vector blend(Vector<Double> v, VectorMask<Double> m) {
        return (Double256Vector)
            super.blendTemplate(Double256Mask.class,
                                (Double256Vector) v,
                                (Double256Mask) m);  // specialize
    }

    @Override
    @ForceInline
    public Double256Vector slice(int origin, Vector<Double> v) {
        return (Double256Vector) super.sliceTemplate(origin, v);  // specialize
    }

    @Override
    @ForceInline
    public Double256Vector unslice(int origin, Vector<Double> w, int part) {
        return (Double256Vector) super.unsliceTemplate(origin, w, part);  // specialize
    }

    @Override
    @ForceInline
    public Double256Vector unslice(int origin, Vector<Double> w, int part, VectorMask<Double> m) {
        return (Double256Vector)
            super.unsliceTemplate(Double256Mask.class,
                                  origin, w, part,
                                  (Double256Mask) m);  // specialize
    }

    @Override
    @ForceInline
    public Double256Vector rearrange(VectorShuffle<Double> s) {
        return (Double256Vector)
            super.rearrangeTemplate(Double256Shuffle.class,
                                    (Double256Shuffle) s);  // specialize
    }

    @Override
    @ForceInline
    public Double256Vector rearrange(VectorShuffle<Double> shuffle,
                                  VectorMask<Double> m) {
        return (Double256Vector)
            super.rearrangeTemplate(Double256Shuffle.class,
                                    (Double256Shuffle) shuffle,
                                    (Double256Mask) m);  // specialize
    }

    @Override
    @ForceInline
    public Double256Vector rearrange(VectorShuffle<Double> s,
                                  Vector<Double> v) {
        return (Double256Vector)
            super.rearrangeTemplate(Double256Shuffle.class,
                                    (Double256Shuffle) s,
                                    (Double256Vector) v);  // specialize
    }

    @Override
    @ForceInline
    public Double256Vector selectFrom(Vector<Double> v) {
        return (Double256Vector)
            super.selectFromTemplate((Double256Vector) v);  // specialize
    }

    @Override
    @ForceInline
    public Double256Vector selectFrom(Vector<Double> v,
                                   VectorMask<Double> m) {
        return (Double256Vector)
            super.selectFromTemplate((Double256Vector) v,
                                     (Double256Mask) m);  // specialize
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
    public Double256Vector withLane(int i, double e) {
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

    static final class Double256Mask extends AbstractMask<Double> {

        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public Double256Mask(boolean[] bits) {
            this(bits, 0);
        }

        public Double256Mask(boolean[] bits, int offset) {
            boolean[] a = new boolean[vspecies().laneCount()];
            for (int i = 0; i < a.length; i++) {
                a[i] = bits[offset + i];
            }
            this.bits = a;
        }

        public Double256Mask(boolean val) {
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
        Double256Mask uOp(MUnOp f) {
            boolean[] res = new boolean[vspecies().laneCount()];
            boolean[] bits = getBits();
            for (int i = 0; i < res.length; i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Double256Mask(res);
        }

        @Override
        Double256Mask bOp(VectorMask<Double> o, MBinOp f) {
            boolean[] res = new boolean[vspecies().laneCount()];
            boolean[] bits = getBits();
            boolean[] mbits = ((Double256Mask)o).getBits();
            for (int i = 0; i < res.length; i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new Double256Mask(res);
        }

        @ForceInline
        @Override
        public final
        Double256Vector toVector() {
            return (Double256Vector) super.toVectorTemplate();  // specialize
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
                return new Byte256Vector.Byte256Mask(maskArray).check(species);
            case LaneType.SK_SHORT:
                return new Short256Vector.Short256Mask(maskArray).check(species);
            case LaneType.SK_INT:
                return new Int256Vector.Int256Mask(maskArray).check(species);
            case LaneType.SK_LONG:
                return new Long256Vector.Long256Mask(maskArray).check(species);
            case LaneType.SK_FLOAT:
                return new Float256Vector.Float256Mask(maskArray).check(species);
            case LaneType.SK_DOUBLE:
                return new Double256Vector.Double256Mask(maskArray).check(species);
            }

            // Should not reach here.
            throw new AssertionError(species);
        }

        // Unary operations

        @Override
        @ForceInline
        public Double256Mask not() {
            return (Double256Mask) VectorIntrinsics.unaryOp(
                                             VECTOR_OP_NOT, Double256Mask.class, long.class, VLENGTH,
                                             this,
                                             (m1) -> m1.uOp((i, a) -> !a));
        }

        // Binary operations

        @Override
        @ForceInline
        public Double256Mask and(VectorMask<Double> o) {
            Objects.requireNonNull(o);
            Double256Mask m = (Double256Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Double256Mask.class, long.class, VLENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Double256Mask or(VectorMask<Double> o) {
            Objects.requireNonNull(o);
            Double256Mask m = (Double256Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, Double256Mask.class, long.class, VLENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(BT_ne, Double256Mask.class, long.class, VLENGTH,
                                         this, this,
                                         (m, __) -> anyTrueHelper(((Double256Mask)m).getBits()));
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(BT_overflow, Double256Mask.class, long.class, VLENGTH,
                                         this, vspecies().maskAll(true),
                                         (m, __) -> allTrueHelper(((Double256Mask)m).getBits()));
        }

        /*package-private*/
        static Double256Mask maskAll(boolean bit) {
            return bit ? TRUE_MASK : FALSE_MASK;
        }
        static final Double256Mask TRUE_MASK = new Double256Mask(true);
        static final Double256Mask FALSE_MASK = new Double256Mask(false);
    }

    // Shuffle

    static final class Double256Shuffle extends AbstractShuffle<Double> {
        Double256Shuffle(byte[] reorder) {
            super(reorder);
        }

        public Double256Shuffle(int[] reorder) {
            super(reorder);
        }

        public Double256Shuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        public Double256Shuffle(IntUnaryOperator fn) {
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
        static final Double256Shuffle IOTA = new Double256Shuffle(IDENTITY);

        @Override
        public Double256Vector toVector() {
            return (Double256Vector) super.toVectorTemplate();  // specialize
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
                return new Byte256Vector.Byte256Shuffle(shuffleArray).check(species);
            case LaneType.SK_SHORT:
                return new Short256Vector.Short256Shuffle(shuffleArray).check(species);
            case LaneType.SK_INT:
                return new Int256Vector.Int256Shuffle(shuffleArray).check(species);
            case LaneType.SK_LONG:
                return new Long256Vector.Long256Shuffle(shuffleArray).check(species);
            case LaneType.SK_FLOAT:
                return new Float256Vector.Float256Shuffle(shuffleArray).check(species);
            case LaneType.SK_DOUBLE:
                return new Double256Vector.Double256Shuffle(shuffleArray).check(species);
            }

            // Should not reach here.
            throw new AssertionError(species);
        }

        @Override
        public Double256Shuffle rearrange(VectorShuffle<Double> o) {
            Double256Shuffle s = (Double256Shuffle) o;
            byte[] r = new byte[reorder.length];
            for (int i = 0; i < reorder.length; i++) {
                int ssi = s.reorder[i];
                r[i] = this.reorder[ssi];  // throws on exceptional index
            }
            return new Double256Shuffle(r);
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
