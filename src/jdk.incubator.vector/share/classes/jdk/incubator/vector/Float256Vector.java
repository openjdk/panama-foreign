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
import java.nio.FloatBuffer;
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
final class Float256Vector extends FloatVector {
    static final FloatSpecies VSPECIES =
        (FloatSpecies) FloatVector.SPECIES_256;

    static final VectorShape VSHAPE =
        VSPECIES.vectorShape();

    static final Class<Float256Vector> VCLASS = Float256Vector.class;

    static final int VSIZE = VSPECIES.vectorBitSize();

    static final int VLENGTH = VSPECIES.laneCount();

    static final Class<Float> ETYPE = float.class;

    // The JVM expects to find the state here.
    private final float[] vec; // Don't access directly, use getElements() instead.

    Float256Vector(float[] v) {
        vec = v;
    }

    // For compatibility as Float256Vector::new,
    // stored into species.vectorFactory.
    Float256Vector(Object v) {
        this((float[]) v);
    }

    static final Float256Vector ZERO = new Float256Vector(new float[VLENGTH]);
    static final Float256Vector IOTA = new Float256Vector(VSPECIES.iotaArray());

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
    public FloatSpecies vspecies() {
        // ISSUE:  This should probably be a @Stable
        // field inside AbstractVector, rather than
        // a megamorphic method.
        return VSPECIES;
    }


    /*package-private*/
    @ForceInline
    final @Override
    float[] getElements() {
        return VectorIntrinsics.maybeRebox(this).vec;
    }

    // Virtualized constructors

    @Override
    @ForceInline
    public final Float256Vector broadcast(float e) {
        return (Float256Vector) super.broadcastTemplate(e);  // specialize
    }

    @Override
    @ForceInline
    public final Float256Vector broadcast(long e) {
        return (Float256Vector) super.broadcastTemplate(e);  // specialize
    }

    @Override
    @ForceInline
    Float256Mask maskFromArray(boolean[] bits) {
        return new Float256Mask(bits);
    }

    @Override
    @ForceInline
    Float256Shuffle iotaShuffle() { return Float256Shuffle.IOTA; }

    @ForceInline
    Float256Shuffle iotaShuffle(int start) { 
        return (Float256Shuffle)VectorIntrinsics.shuffleIota(ETYPE, Float256Shuffle.class, VSPECIES, VLENGTH, start, (val, l) -> new Float256Shuffle(i -> (Float256Shuffle.partiallyWrapIndex(i + val, l))));
    }

    @Override
    @ForceInline
    Float256Shuffle shuffleFromBytes(byte[] reorder) { return new Float256Shuffle(reorder); }

    @Override
    @ForceInline
    Float256Shuffle shuffleFromArray(int[] indexes, int i) { return new Float256Shuffle(indexes, i); }

    @Override
    @ForceInline
    Float256Shuffle shuffleFromOp(IntUnaryOperator fn) { return new Float256Shuffle(fn); }

    // Make a vector of the same species but the given elements:
    @ForceInline
    final @Override
    Float256Vector vectorFactory(float[] vec) {
        return new Float256Vector(vec);
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
    Float256Vector uOp(FUnOp f) {
        return (Float256Vector) super.uOp(f);  // specialize
    }

    @ForceInline
    final @Override
    Float256Vector uOp(VectorMask<Float> m, FUnOp f) {
        return (Float256Vector) super.uOp((Float256Mask)m, f);  // specialize
    }

    // Binary operator

    @ForceInline
    final @Override
    Float256Vector bOp(Vector<Float> v, FBinOp f) {
        return (Float256Vector) super.bOp((Float256Vector)v, f);  // specialize
    }

    @ForceInline
    final @Override
    Float256Vector bOp(Vector<Float> v,
                     VectorMask<Float> m, FBinOp f) {
        return (Float256Vector) super.bOp((Float256Vector)v, (Float256Mask)m,
                                        f);  // specialize
    }

    // Ternary operator

    @ForceInline
    final @Override
    Float256Vector tOp(Vector<Float> v1, Vector<Float> v2, FTriOp f) {
        return (Float256Vector) super.tOp((Float256Vector)v1, (Float256Vector)v2,
                                        f);  // specialize
    }

    @ForceInline
    final @Override
    Float256Vector tOp(Vector<Float> v1, Vector<Float> v2,
                     VectorMask<Float> m, FTriOp f) {
        return (Float256Vector) super.tOp((Float256Vector)v1, (Float256Vector)v2,
                                        (Float256Mask)m, f);  // specialize
    }

    @ForceInline
    final @Override
    float rOp(float v, FBinOp f) {
        return super.rOp(v, f);  // specialize
    }

    @Override
    @ForceInline
    public final <F>
    Vector<F> convertShape(VectorOperators.Conversion<Float,F> conv,
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
    public Float256Vector lanewise(Unary op) {
        return (Float256Vector) super.lanewiseTemplate(op);  // specialize
    }

    @Override
    @ForceInline
    public Float256Vector lanewise(Binary op, Vector<Float> v) {
        return (Float256Vector) super.lanewiseTemplate(op, v);  // specialize
    }


    /*package-private*/
    @Override
    @ForceInline
    public final
    Float256Vector
    lanewise(VectorOperators.Ternary op, Vector<Float> v1, Vector<Float> v2) {
        return (Float256Vector) super.lanewiseTemplate(op, v1, v2);  // specialize
    }

    @Override
    @ForceInline
    public final
    Float256Vector addIndex(int scale) {
        return (Float256Vector) super.addIndexTemplate(scale);  // specialize
    }

    // Type specific horizontal reductions

    @Override
    @ForceInline
    public final float reduceLanes(VectorOperators.Associative op) {
        return super.reduceLanesTemplate(op);  // specialized
    }

    @Override
    @ForceInline
    public final float reduceLanes(VectorOperators.Associative op,
                                    VectorMask<Float> m) {
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
                                        VectorMask<Float> m) {
        return (long) super.reduceLanesTemplate(op, m);  // specialized
    }

    @Override
    @ForceInline
    public VectorShuffle<Float> toShuffle() {
        float[] a = toArray();
        int[] sa = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            sa[i] = (int) a[i];
        }
        return VectorShuffle.fromArray(VSPECIES, sa, 0);
    }

    // Specialized comparisons

    @Override
    @ForceInline
    public final Float256Mask compare(Comparison op, Vector<Float> v) {
        return super.compareTemplate(Float256Mask.class, op, v);  // specialize
    }

    @Override
    @ForceInline
    public final Float256Mask compare(Comparison op, float s) {
        return super.compareTemplate(Float256Mask.class, op, s);  // specialize
    }

    @Override
    @ForceInline
    public final Float256Mask compare(Comparison op, long s) {
        return super.compareTemplate(Float256Mask.class, op, s);  // specialize
    }

    @Override
    @ForceInline
    public Float256Vector blend(Vector<Float> v, VectorMask<Float> m) {
        return (Float256Vector)
            super.blendTemplate(Float256Mask.class,
                                (Float256Vector) v,
                                (Float256Mask) m);  // specialize
    }

    @Override
    @ForceInline
    public Float256Vector slice(int origin, Vector<Float> v) {
        return (Float256Vector) super.sliceTemplate(origin, v);  // specialize
    }

    @Override
    @ForceInline
    public Float256Vector slice(int origin) {
       if ((origin < 0) || (origin >= VLENGTH)) {
         throw new ArrayIndexOutOfBoundsException("Index " + origin + " out of bounds for vector length " + VLENGTH);
       } else {
         Float256Shuffle Iota = iotaShuffle(origin);
         VectorMask<Float> BlendMask = Iota.toVector().compare(VectorOperators.GE, (broadcast((float)(origin))));
         Iota = iotaShuffle(origin);
         return ZERO.blend(this.rearrange(Iota), BlendMask);
       }
    }

    @Override
    @ForceInline
    public Float256Vector unslice(int origin, Vector<Float> w, int part) {
        return (Float256Vector) super.unsliceTemplate(origin, w, part);  // specialize
    }

    @Override
    @ForceInline
    public Float256Vector unslice(int origin, Vector<Float> w, int part, VectorMask<Float> m) {
        return (Float256Vector)
            super.unsliceTemplate(Float256Mask.class,
                                  origin, w, part,
                                  (Float256Mask) m);  // specialize
    }

    @Override
    @ForceInline
    public Float256Vector unslice(int origin) {
       if ((origin < 0) || (origin >= VLENGTH)) {
         throw new ArrayIndexOutOfBoundsException("Index " + origin + " out of bounds for vector length " + VLENGTH);
       } else {
         Float256Shuffle Iota = iotaShuffle(-origin);
         VectorMask<Float> BlendMask = Iota.toVector().compare(VectorOperators.GE, (broadcast((float)(0))));
         Iota = iotaShuffle(-origin);
         return ZERO.blend(this.rearrange(Iota), BlendMask);
       }
    }

    @Override
    @ForceInline
    public Float256Vector rearrange(VectorShuffle<Float> s) {
        return (Float256Vector)
            super.rearrangeTemplate(Float256Shuffle.class,
                                    (Float256Shuffle) s);  // specialize
    }

    @Override
    @ForceInline
    public Float256Vector rearrange(VectorShuffle<Float> shuffle,
                                  VectorMask<Float> m) {
        return (Float256Vector)
            super.rearrangeTemplate(Float256Shuffle.class,
                                    (Float256Shuffle) shuffle,
                                    (Float256Mask) m);  // specialize
    }

    @Override
    @ForceInline
    public Float256Vector rearrange(VectorShuffle<Float> s,
                                  Vector<Float> v) {
        return (Float256Vector)
            super.rearrangeTemplate(Float256Shuffle.class,
                                    (Float256Shuffle) s,
                                    (Float256Vector) v);  // specialize
    }

    @Override
    @ForceInline
    public Float256Vector selectFrom(Vector<Float> v) {
        return (Float256Vector)
            super.selectFromTemplate((Float256Vector) v);  // specialize
    }

    @Override
    @ForceInline
    public Float256Vector selectFrom(Vector<Float> v,
                                   VectorMask<Float> m) {
        return (Float256Vector)
            super.selectFromTemplate((Float256Vector) v,
                                     (Float256Mask) m);  // specialize
    }


    @Override
    public float lane(int i) {
        if (i < 0 || i >= VLENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + VLENGTH);
        }
        int bits = (int) VectorIntrinsics.extract(
                                VCLASS, ETYPE, VLENGTH,
                                this, i,
                                (vec, ix) -> {
                                    float[] vecarr = vec.getElements();
                                    return (long)Float.floatToIntBits(vecarr[ix]);
                                });
        return Float.intBitsToFloat(bits);
    }

    @Override
    public Float256Vector withLane(int i, float e) {
        if (i < 0 || i >= VLENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + VLENGTH);
        }
        return VectorIntrinsics.insert(
                                VCLASS, ETYPE, VLENGTH,
                                this, i, (long)Float.floatToIntBits(e),
                                (v, ix, bits) -> {
                                    float[] res = v.getElements().clone();
                                    res[ix] = Float.intBitsToFloat((int)bits);
                                    return v.vectorFactory(res);
                                });
    }

    // Mask

    static final class Float256Mask extends AbstractMask<Float> {

        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public Float256Mask(boolean[] bits) {
            this(bits, 0);
        }

        public Float256Mask(boolean[] bits, int offset) {
            boolean[] a = new boolean[vspecies().laneCount()];
            for (int i = 0; i < a.length; i++) {
                a[i] = bits[offset + i];
            }
            this.bits = a;
        }

        public Float256Mask(boolean val) {
            boolean[] bits = new boolean[vspecies().laneCount()];
            Arrays.fill(bits, val);
            this.bits = bits;
        }

        @ForceInline
        final @Override
        public FloatSpecies vspecies() {
            // ISSUE:  This should probably be a @Stable
            // field inside AbstractMask, rather than
            // a megamorphic method.
            return VSPECIES;
        }

        boolean[] getBits() {
            return VectorIntrinsics.maybeRebox(this).bits;
        }

        @Override
        Float256Mask uOp(MUnOp f) {
            boolean[] res = new boolean[vspecies().laneCount()];
            boolean[] bits = getBits();
            for (int i = 0; i < res.length; i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Float256Mask(res);
        }

        @Override
        Float256Mask bOp(VectorMask<Float> m, MBinOp f) {
            boolean[] res = new boolean[vspecies().laneCount()];
            boolean[] bits = getBits();
            boolean[] mbits = ((Float256Mask)m).getBits();
            for (int i = 0; i < res.length; i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new Float256Mask(res);
        }

        @ForceInline
        @Override
        public final
        Float256Vector toVector() {
            return (Float256Vector) super.toVectorTemplate();  // specialize
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
        public Float256Mask not() {
            return (Float256Mask) VectorIntrinsics.unaryOp(
                                             VECTOR_OP_NOT, Float256Mask.class, int.class, VLENGTH,
                                             this,
                                             (m1) -> m1.uOp((i, a) -> !a));
        }

        // Binary operations

        @Override
        @ForceInline
        public Float256Mask and(VectorMask<Float> mask) {
            Objects.requireNonNull(mask);
            Float256Mask m = (Float256Mask)mask;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Float256Mask.class, int.class, VLENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Float256Mask or(VectorMask<Float> mask) {
            Objects.requireNonNull(mask);
            Float256Mask m = (Float256Mask)mask;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, Float256Mask.class, int.class, VLENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(BT_ne, Float256Mask.class, int.class, VLENGTH,
                                         this, this,
                                         (m, __) -> anyTrueHelper(((Float256Mask)m).getBits()));
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(BT_overflow, Float256Mask.class, int.class, VLENGTH,
                                         this, vspecies().maskAll(true),
                                         (m, __) -> allTrueHelper(((Float256Mask)m).getBits()));
        }

        /*package-private*/
        static Float256Mask maskAll(boolean bit) {
            return bit ? TRUE_MASK : FALSE_MASK;
        }
        static final Float256Mask TRUE_MASK = new Float256Mask(true);
        static final Float256Mask FALSE_MASK = new Float256Mask(false);
    }

    // Shuffle

    static final class Float256Shuffle extends AbstractShuffle<Float> {
        Float256Shuffle(byte[] reorder) {
            super(reorder);
        }

        public Float256Shuffle(int[] reorder) {
            super(reorder);
        }

        public Float256Shuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        public Float256Shuffle(IntUnaryOperator fn) {
            super(fn);
        }

        @Override
        public FloatSpecies vspecies() {
            return VSPECIES;
        }

        static {
            // There must be enough bits in the shuffle lanes to encode
            // VLENGTH valid indexes and VLENGTH exceptional ones.
            assert(VLENGTH < Byte.MAX_VALUE);
            assert(Byte.MIN_VALUE <= -VLENGTH);
        }
        static final Float256Shuffle IOTA = new Float256Shuffle(IDENTITY);

        @Override
        @ForceInline
        public Float256Vector toVector() {
            return VectorIntrinsics.shuffleToVector(VCLASS, ETYPE, Float256Shuffle.class, this, VLENGTH,
                                                    (s) -> ((Float256Vector)(((AbstractShuffle<Float>)(s)).toVectorTemplate())));
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
        public Float256Shuffle rearrange(VectorShuffle<Float> shuffle) {
            Float256Shuffle s = (Float256Shuffle) shuffle;
            byte[] r = new byte[reorder.length];
            for (int i = 0; i < reorder.length; i++) {
                int ssi = s.reorder[i];
                r[i] = this.reorder[ssi];  // throws on exceptional index
            }
            return new Float256Shuffle(r);
        }
    }

    // ================================================

    // Specialized low-level memory operations.

    @ForceInline
    @Override
    final
    FloatVector fromArray0(float[] a, int offset) {
        return super.fromArray0(a, offset);  // specialize
    }

    @ForceInline
    @Override
    final
    FloatVector fromByteArray0(byte[] a, int offset) {
        return super.fromByteArray0(a, offset);  // specialize
    }

    @ForceInline
    @Override
    final
    FloatVector fromByteBuffer0(ByteBuffer bb, int offset) {
        return super.fromByteBuffer0(bb, offset);  // specialize
    }

    @ForceInline
    @Override
    final
    void intoArray0(float[] a, int offset) {
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
