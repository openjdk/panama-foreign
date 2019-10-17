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
import java.nio.IntBuffer;
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
final class Int128Vector extends IntVector {
    static final IntSpecies VSPECIES =
        (IntSpecies) IntVector.SPECIES_128;

    static final VectorShape VSHAPE =
        VSPECIES.vectorShape();

    static final Class<Int128Vector> VCLASS = Int128Vector.class;

    static final int VSIZE = VSPECIES.vectorBitSize();

    static final int VLENGTH = VSPECIES.laneCount();

    static final Class<Integer> ETYPE = int.class;

    // The JVM expects to find the state here.
    private final int[] vec; // Don't access directly, use getElements() instead.

    Int128Vector(int[] v) {
        vec = v;
    }

    // For compatibility as Int128Vector::new,
    // stored into species.vectorFactory.
    Int128Vector(Object v) {
        this((int[]) v);
    }

    static final Int128Vector ZERO = new Int128Vector(new int[VLENGTH]);
    static final Int128Vector IOTA = new Int128Vector(VSPECIES.iotaArray());

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
    public IntSpecies vspecies() {
        // ISSUE:  This should probably be a @Stable
        // field inside AbstractVector, rather than
        // a megamorphic method.
        return VSPECIES;
    }

    @ForceInline
    @Override
    public final Class<Integer> elementType() { return int.class; }

    @ForceInline
    @Override
    public final int elementSize() { return Integer.SIZE; }

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
    int[] getElements() {
        return VectorIntrinsics.maybeRebox(this).vec;
    }

    // Virtualized constructors

    @Override
    @ForceInline
    public final Int128Vector broadcast(int e) {
        return (Int128Vector) super.broadcastTemplate(e);  // specialize
    }

    @Override
    @ForceInline
    public final Int128Vector broadcast(long e) {
        return (Int128Vector) super.broadcastTemplate(e);  // specialize
    }

    @Override
    @ForceInline
    Int128Mask maskFromArray(boolean[] bits) {
        return new Int128Mask(bits);
    }

    @Override
    @ForceInline
    Int128Shuffle iotaShuffle() { return Int128Shuffle.IOTA; }

    @ForceInline
    Int128Shuffle iotaShuffle(int start) { 
        return (Int128Shuffle)VectorIntrinsics.shuffleIota(ETYPE, Int128Shuffle.class, VSPECIES, VLENGTH, start, (val, l) -> new Int128Shuffle(i -> (VectorIntrinsics.wrapToRange(i + val, l))));
    }

    @Override
    @ForceInline
    Int128Shuffle shuffleFromBytes(byte[] reorder) { return new Int128Shuffle(reorder); }

    @Override
    @ForceInline
    Int128Shuffle shuffleFromArray(int[] indexes, int i) { return new Int128Shuffle(indexes, i); }

    @Override
    @ForceInline
    Int128Shuffle shuffleFromOp(IntUnaryOperator fn) { return new Int128Shuffle(fn); }

    // Make a vector of the same species but the given elements:
    @ForceInline
    final @Override
    Int128Vector vectorFactory(int[] vec) {
        return new Int128Vector(vec);
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
    Int128Vector uOp(FUnOp f) {
        return (Int128Vector) super.uOpTemplate(f);  // specialize
    }

    @ForceInline
    final @Override
    Int128Vector uOp(VectorMask<Integer> m, FUnOp f) {
        return (Int128Vector)
            super.uOpTemplate((Int128Mask)m, f);  // specialize
    }

    // Binary operator

    @ForceInline
    final @Override
    Int128Vector bOp(Vector<Integer> v, FBinOp f) {
        return (Int128Vector) super.bOpTemplate((Int128Vector)v, f);  // specialize
    }

    @ForceInline
    final @Override
    Int128Vector bOp(Vector<Integer> v,
                     VectorMask<Integer> m, FBinOp f) {
        return (Int128Vector)
            super.bOpTemplate((Int128Vector)v, (Int128Mask)m,
                              f);  // specialize
    }

    // Ternary operator

    @ForceInline
    final @Override
    Int128Vector tOp(Vector<Integer> v1, Vector<Integer> v2, FTriOp f) {
        return (Int128Vector)
            super.tOpTemplate((Int128Vector)v1, (Int128Vector)v2,
                              f);  // specialize
    }

    @ForceInline
    final @Override
    Int128Vector tOp(Vector<Integer> v1, Vector<Integer> v2,
                     VectorMask<Integer> m, FTriOp f) {
        return (Int128Vector)
            super.tOpTemplate((Int128Vector)v1, (Int128Vector)v2,
                              (Int128Mask)m, f);  // specialize
    }

    @ForceInline
    final @Override
    int rOp(int v, FBinOp f) {
        return super.rOpTemplate(v, f);  // specialize
    }

    @Override
    @ForceInline
    public final <F>
    Vector<F> convertShape(VectorOperators.Conversion<Integer,F> conv,
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
    public Int128Vector lanewise(Unary op) {
        return (Int128Vector) super.lanewiseTemplate(op);  // specialize
    }

    @Override
    @ForceInline
    public Int128Vector lanewise(Binary op, Vector<Integer> v) {
        return (Int128Vector) super.lanewiseTemplate(op, v);  // specialize
    }

    /*package-private*/
    @Override
    @ForceInline Int128Vector
    lanewiseShift(VectorOperators.Binary op, int e) {
        return (Int128Vector) super.lanewiseShiftTemplate(op, e);  // specialize
    }

    /*package-private*/
    @Override
    @ForceInline
    public final
    Int128Vector
    lanewise(VectorOperators.Ternary op, Vector<Integer> v1, Vector<Integer> v2) {
        return (Int128Vector) super.lanewiseTemplate(op, v1, v2);  // specialize
    }

    @Override
    @ForceInline
    public final
    Int128Vector addIndex(int scale) {
        return (Int128Vector) super.addIndexTemplate(scale);  // specialize
    }

    // Type specific horizontal reductions

    @Override
    @ForceInline
    public final int reduceLanes(VectorOperators.Associative op) {
        return super.reduceLanesTemplate(op);  // specialized
    }

    @Override
    @ForceInline
    public final int reduceLanes(VectorOperators.Associative op,
                                    VectorMask<Integer> m) {
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
                                        VectorMask<Integer> m) {
        return (long) super.reduceLanesTemplate(op, m);  // specialized
    }

    @Override
    @ForceInline
    public VectorShuffle<Integer> toShuffle() {
        int[] a = toArray();
        int[] sa = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            sa[i] = (int) a[i];
        }
        return VectorShuffle.fromArray(VSPECIES, sa, 0);
    }

    // Specialized unary testing

    @Override
    @ForceInline
    public final Int128Mask test(Test op) {
        return super.testTemplate(Int128Mask.class, op);  // specialize
    }

    // Specialized comparisons

    @Override
    @ForceInline
    public final Int128Mask compare(Comparison op, Vector<Integer> v) {
        return super.compareTemplate(Int128Mask.class, op, v);  // specialize
    }

    @Override
    @ForceInline
    public final Int128Mask compare(Comparison op, int s) {
        return super.compareTemplate(Int128Mask.class, op, s);  // specialize
    }

    @Override
    @ForceInline
    public final Int128Mask compare(Comparison op, long s) {
        return super.compareTemplate(Int128Mask.class, op, s);  // specialize
    }

    @Override
    @ForceInline
    public Int128Vector blend(Vector<Integer> v, VectorMask<Integer> m) {
        return (Int128Vector)
            super.blendTemplate(Int128Mask.class,
                                (Int128Vector) v,
                                (Int128Mask) m);  // specialize
    }

    @Override
    @ForceInline
    public Int128Vector slice(int origin, Vector<Integer> v) {
        return (Int128Vector) super.sliceTemplate(origin, v);  // specialize
    }

    @Override
    @ForceInline
    public Int128Vector slice(int origin) {
       if ((origin < 0) || (origin >= VLENGTH)) {
         throw new ArrayIndexOutOfBoundsException("Index " + origin + " out of bounds for vector length " + VLENGTH);
       } else {
         Int128Shuffle Iota = (Int128Shuffle)VectorShuffle.iota(VSPECIES, 0, 1, true);
         VectorMask<Integer> BlendMask = Iota.toVector().compare(VectorOperators.LT, (broadcast((int)(VLENGTH-origin))));
         Iota = (Int128Shuffle)VectorShuffle.iota(VSPECIES, origin, 1, true);
         return ZERO.blend(this.rearrange(Iota), BlendMask);
       }
    }

    @Override
    @ForceInline
    public Int128Vector unslice(int origin, Vector<Integer> w, int part) {
        return (Int128Vector) super.unsliceTemplate(origin, w, part);  // specialize
    }

    @Override
    @ForceInline
    public Int128Vector unslice(int origin, Vector<Integer> w, int part, VectorMask<Integer> m) {
        return (Int128Vector)
            super.unsliceTemplate(Int128Mask.class,
                                  origin, w, part,
                                  (Int128Mask) m);  // specialize
    }

    @Override
    @ForceInline
    public Int128Vector unslice(int origin) {
       if ((origin < 0) || (origin >= VLENGTH)) {
         throw new ArrayIndexOutOfBoundsException("Index " + origin + " out of bounds for vector length " + VLENGTH);
       } else {
         Int128Shuffle Iota = (Int128Shuffle)VectorShuffle.iota(VSPECIES, 0, 1, true);
         VectorMask<Integer> BlendMask = Iota.toVector().compare(VectorOperators.GE, (broadcast((int)(origin))));
         Iota = (Int128Shuffle)VectorShuffle.iota(VSPECIES, -origin, 1, true);
         return ZERO.blend(this.rearrange(Iota), BlendMask);
       }
    }

    @Override
    @ForceInline
    public Int128Vector rearrange(VectorShuffle<Integer> s) {
        return (Int128Vector)
            super.rearrangeTemplate(Int128Shuffle.class,
                                    (Int128Shuffle) s);  // specialize
    }

    @Override
    @ForceInline
    public Int128Vector rearrange(VectorShuffle<Integer> shuffle,
                                  VectorMask<Integer> m) {
        return (Int128Vector)
            super.rearrangeTemplate(Int128Shuffle.class,
                                    (Int128Shuffle) shuffle,
                                    (Int128Mask) m);  // specialize
    }

    @Override
    @ForceInline
    public Int128Vector rearrange(VectorShuffle<Integer> s,
                                  Vector<Integer> v) {
        return (Int128Vector)
            super.rearrangeTemplate(Int128Shuffle.class,
                                    (Int128Shuffle) s,
                                    (Int128Vector) v);  // specialize
    }

    @Override
    @ForceInline
    public Int128Vector selectFrom(Vector<Integer> v) {
        return (Int128Vector)
            super.selectFromTemplate((Int128Vector) v);  // specialize
    }

    @Override
    @ForceInline
    public Int128Vector selectFrom(Vector<Integer> v,
                                   VectorMask<Integer> m) {
        return (Int128Vector)
            super.selectFromTemplate((Int128Vector) v,
                                     (Int128Mask) m);  // specialize
    }


    @Override
    public int lane(int i) {
        if (i < 0 || i >= VLENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + VLENGTH);
        }
        return (int) VectorIntrinsics.extract(
                                VCLASS, ETYPE, VLENGTH,
                                this, i,
                                (vec, ix) -> {
                                    int[] vecarr = vec.getElements();
                                    return (long)vecarr[ix];
                                });
    }

    @Override
    public Int128Vector withLane(int i, int e) {
        if (i < 0 || i >= VLENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + VLENGTH);
        }
        return VectorIntrinsics.insert(
                                VCLASS, ETYPE, VLENGTH,
                                this, i, (long)e,
                                (v, ix, bits) -> {
                                    int[] res = v.getElements().clone();
                                    res[ix] = (int)bits;
                                    return v.vectorFactory(res);
                                });
    }

    // Mask

    static final class Int128Mask extends AbstractMask<Integer> {

        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public Int128Mask(boolean[] bits) {
            this(bits, 0);
        }

        public Int128Mask(boolean[] bits, int offset) {
            boolean[] a = new boolean[vspecies().laneCount()];
            for (int i = 0; i < a.length; i++) {
                a[i] = bits[offset + i];
            }
            this.bits = a;
        }

        public Int128Mask(boolean val) {
            boolean[] bits = new boolean[vspecies().laneCount()];
            Arrays.fill(bits, val);
            this.bits = bits;
        }

        @ForceInline
        final @Override
        public IntSpecies vspecies() {
            // ISSUE:  This should probably be a @Stable
            // field inside AbstractMask, rather than
            // a megamorphic method.
            return VSPECIES;
        }

        boolean[] getBits() {
            return VectorIntrinsics.maybeRebox(this).bits;
        }

        @Override
        Int128Mask uOp(MUnOp f) {
            boolean[] res = new boolean[vspecies().laneCount()];
            boolean[] bits = getBits();
            for (int i = 0; i < res.length; i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Int128Mask(res);
        }

        @Override
        Int128Mask bOp(VectorMask<Integer> m, MBinOp f) {
            boolean[] res = new boolean[vspecies().laneCount()];
            boolean[] bits = getBits();
            boolean[] mbits = ((Int128Mask)m).getBits();
            for (int i = 0; i < res.length; i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new Int128Mask(res);
        }

        @ForceInline
        @Override
        public final
        Int128Vector toVector() {
            return (Int128Vector) super.toVectorTemplate();  // specialize
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
        public Int128Mask not() {
            return (Int128Mask) VectorIntrinsics.unaryOp(
                                             VECTOR_OP_NOT, Int128Mask.class, int.class, VLENGTH,
                                             this,
                                             (m1) -> m1.uOp((i, a) -> !a));
        }

        // Binary operations

        @Override
        @ForceInline
        public Int128Mask and(VectorMask<Integer> mask) {
            Objects.requireNonNull(mask);
            Int128Mask m = (Int128Mask)mask;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Int128Mask.class, int.class, VLENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Int128Mask or(VectorMask<Integer> mask) {
            Objects.requireNonNull(mask);
            Int128Mask m = (Int128Mask)mask;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, Int128Mask.class, int.class, VLENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(BT_ne, Int128Mask.class, int.class, VLENGTH,
                                         this, this,
                                         (m, __) -> anyTrueHelper(((Int128Mask)m).getBits()));
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(BT_overflow, Int128Mask.class, int.class, VLENGTH,
                                         this, vspecies().maskAll(true),
                                         (m, __) -> allTrueHelper(((Int128Mask)m).getBits()));
        }

        /*package-private*/
        static Int128Mask maskAll(boolean bit) {
            return bit ? TRUE_MASK : FALSE_MASK;
        }
        static final Int128Mask TRUE_MASK = new Int128Mask(true);
        static final Int128Mask FALSE_MASK = new Int128Mask(false);
    }

    // Shuffle

    static final class Int128Shuffle extends AbstractShuffle<Integer> {
        Int128Shuffle(byte[] reorder) {
            super(reorder);
        }

        public Int128Shuffle(int[] reorder) {
            super(reorder);
        }

        public Int128Shuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        public Int128Shuffle(IntUnaryOperator fn) {
            super(fn);
        }

        @Override
        public IntSpecies vspecies() {
            return VSPECIES;
        }

        static {
            // There must be enough bits in the shuffle lanes to encode
            // VLENGTH valid indexes and VLENGTH exceptional ones.
            assert(VLENGTH < Byte.MAX_VALUE);
            assert(Byte.MIN_VALUE <= -VLENGTH);
        }
        static final Int128Shuffle IOTA = new Int128Shuffle(IDENTITY);

        @Override
        @ForceInline
        public Int128Vector toVector() {
            return VectorIntrinsics.shuffleToVector(VCLASS, ETYPE, Int128Shuffle.class, this, VLENGTH,
                                                    (s) -> ((Int128Vector)(((AbstractShuffle<Integer>)(s)).toVectorTemplate())));
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
        public Int128Shuffle rearrange(VectorShuffle<Integer> shuffle) {
            Int128Shuffle s = (Int128Shuffle) shuffle;
            byte[] r = new byte[reorder.length];
            for (int i = 0; i < reorder.length; i++) {
                int ssi = s.reorder[i];
                r[i] = this.reorder[ssi];  // throws on exceptional index
            }
            return new Int128Shuffle(r);
        }
    }

    // ================================================

    // Specialized low-level memory operations.

    @ForceInline
    @Override
    final
    IntVector fromArray0(int[] a, int offset) {
        return super.fromArray0Template(a, offset);  // specialize
    }

    @ForceInline
    @Override
    final
    IntVector fromByteArray0(byte[] a, int offset) {
        return super.fromByteArray0Template(a, offset);  // specialize
    }

    @ForceInline
    @Override
    final
    IntVector fromByteBuffer0(ByteBuffer bb, int offset) {
        return super.fromByteBuffer0Template(bb, offset);  // specialize
    }

    @ForceInline
    @Override
    final
    void intoArray0(int[] a, int offset) {
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
