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
import java.nio.ShortBuffer;
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
final class Short512Vector extends ShortVector {
    static final ShortSpecies VSPECIES =
        (ShortSpecies) ShortVector.SPECIES_512;

    static final VectorShape VSHAPE =
        VSPECIES.vectorShape();

    static final Class<Short512Vector> VCLASS = Short512Vector.class;

    static final int VSIZE = VSPECIES.vectorBitSize();

    static final int VLENGTH = VSPECIES.laneCount();

    static final Class<Short> ETYPE = short.class;

    // The JVM expects to find the state here.
    private final short[] vec; // Don't access directly, use getElements() instead.

    Short512Vector(short[] v) {
        vec = v;
    }

    // For compatibility as Short512Vector::new,
    // stored into species.vectorFactory.
    Short512Vector(Object v) {
        this((short[]) v);
    }

    static final Short512Vector ZERO = new Short512Vector(new short[VLENGTH]);
    static final Short512Vector IOTA = new Short512Vector(VSPECIES.iotaArray());

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
    public ShortSpecies vspecies() {
        // ISSUE:  This should probably be a @Stable
        // field inside AbstractVector, rather than
        // a megamorphic method.
        return VSPECIES;
    }

    @ForceInline
    @Override
    public final Class<Short> elementType() { return short.class; }

    @ForceInline
    @Override
    public final int elementSize() { return Short.SIZE; }

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
    short[] getElements() {
        return VectorIntrinsics.maybeRebox(this).vec;
    }

    // Virtualized constructors

    @Override
    @ForceInline
    public final Short512Vector broadcast(short e) {
        return (Short512Vector) super.broadcastTemplate(e);  // specialize
    }

    @Override
    @ForceInline
    public final Short512Vector broadcast(long e) {
        return (Short512Vector) super.broadcastTemplate(e);  // specialize
    }

    @Override
    @ForceInline
    Short512Mask maskFromArray(boolean[] bits) {
        return new Short512Mask(bits);
    }

    @Override
    @ForceInline
    Short512Shuffle iotaShuffle() { return Short512Shuffle.IOTA; }

    @ForceInline
    Short512Shuffle iotaShuffle(int start) { 
        return (Short512Shuffle)VectorIntrinsics.shuffleIota(ETYPE, Short512Shuffle.class, VSPECIES, VLENGTH, start, (val, l) -> new Short512Shuffle(i -> (Short512Shuffle.partiallyWrapIndex(i + val, l))));
    }

    @Override
    @ForceInline
    Short512Shuffle shuffleFromBytes(byte[] reorder) { return new Short512Shuffle(reorder); }

    @Override
    @ForceInline
    Short512Shuffle shuffleFromArray(int[] indexes, int i) { return new Short512Shuffle(indexes, i); }

    @Override
    @ForceInline
    Short512Shuffle shuffleFromOp(IntUnaryOperator fn) { return new Short512Shuffle(fn); }

    // Make a vector of the same species but the given elements:
    @ForceInline
    final @Override
    Short512Vector vectorFactory(short[] vec) {
        return new Short512Vector(vec);
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
    Short512Vector uOp(FUnOp f) {
        return (Short512Vector) super.uOpTemplate(f);  // specialize
    }

    @ForceInline
    final @Override
    Short512Vector uOp(VectorMask<Short> m, FUnOp f) {
        return (Short512Vector)
            super.uOpTemplate((Short512Mask)m, f);  // specialize
    }

    // Binary operator

    @ForceInline
    final @Override
    Short512Vector bOp(Vector<Short> v, FBinOp f) {
        return (Short512Vector) super.bOpTemplate((Short512Vector)v, f);  // specialize
    }

    @ForceInline
    final @Override
    Short512Vector bOp(Vector<Short> v,
                     VectorMask<Short> m, FBinOp f) {
        return (Short512Vector)
            super.bOpTemplate((Short512Vector)v, (Short512Mask)m,
                              f);  // specialize
    }

    // Ternary operator

    @ForceInline
    final @Override
    Short512Vector tOp(Vector<Short> v1, Vector<Short> v2, FTriOp f) {
        return (Short512Vector)
            super.tOpTemplate((Short512Vector)v1, (Short512Vector)v2,
                              f);  // specialize
    }

    @ForceInline
    final @Override
    Short512Vector tOp(Vector<Short> v1, Vector<Short> v2,
                     VectorMask<Short> m, FTriOp f) {
        return (Short512Vector)
            super.tOpTemplate((Short512Vector)v1, (Short512Vector)v2,
                              (Short512Mask)m, f);  // specialize
    }

    @ForceInline
    final @Override
    short rOp(short v, FBinOp f) {
        return super.rOpTemplate(v, f);  // specialize
    }

    @Override
    @ForceInline
    public final <F>
    Vector<F> convertShape(VectorOperators.Conversion<Short,F> conv,
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
    public Short512Vector lanewise(Unary op) {
        return (Short512Vector) super.lanewiseTemplate(op);  // specialize
    }

    @Override
    @ForceInline
    public Short512Vector lanewise(Binary op, Vector<Short> v) {
        return (Short512Vector) super.lanewiseTemplate(op, v);  // specialize
    }

    /*package-private*/
    @Override
    @ForceInline Short512Vector
    lanewiseShift(VectorOperators.Binary op, int e) {
        return (Short512Vector) super.lanewiseShiftTemplate(op, e);  // specialize
    }

    /*package-private*/
    @Override
    @ForceInline
    public final
    Short512Vector
    lanewise(VectorOperators.Ternary op, Vector<Short> v1, Vector<Short> v2) {
        return (Short512Vector) super.lanewiseTemplate(op, v1, v2);  // specialize
    }

    @Override
    @ForceInline
    public final
    Short512Vector addIndex(int scale) {
        return (Short512Vector) super.addIndexTemplate(scale);  // specialize
    }

    // Type specific horizontal reductions

    @Override
    @ForceInline
    public final short reduceLanes(VectorOperators.Associative op) {
        return super.reduceLanesTemplate(op);  // specialized
    }

    @Override
    @ForceInline
    public final short reduceLanes(VectorOperators.Associative op,
                                    VectorMask<Short> m) {
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
                                        VectorMask<Short> m) {
        return (long) super.reduceLanesTemplate(op, m);  // specialized
    }

    @Override
    @ForceInline
    public VectorShuffle<Short> toShuffle() {
        short[] a = toArray();
        int[] sa = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            sa[i] = (int) a[i];
        }
        return VectorShuffle.fromArray(VSPECIES, sa, 0);
    }

    // Specialized unary testing

    @Override
    @ForceInline
    public final Short512Mask test(Test op) {
        return super.testTemplate(Short512Mask.class, op);  // specialize
    }

    // Specialized comparisons

    @Override
    @ForceInline
    public final Short512Mask compare(Comparison op, Vector<Short> v) {
        return super.compareTemplate(Short512Mask.class, op, v);  // specialize
    }

    @Override
    @ForceInline
    public final Short512Mask compare(Comparison op, short s) {
        return super.compareTemplate(Short512Mask.class, op, s);  // specialize
    }

    @Override
    @ForceInline
    public final Short512Mask compare(Comparison op, long s) {
        return super.compareTemplate(Short512Mask.class, op, s);  // specialize
    }

    @Override
    @ForceInline
    public Short512Vector blend(Vector<Short> v, VectorMask<Short> m) {
        return (Short512Vector)
            super.blendTemplate(Short512Mask.class,
                                (Short512Vector) v,
                                (Short512Mask) m);  // specialize
    }

    @Override
    @ForceInline
    public Short512Vector slice(int origin, Vector<Short> v) {
        return (Short512Vector) super.sliceTemplate(origin, v);  // specialize
    }

    @Override
    @ForceInline
    public Short512Vector slice(int origin) {
       if ((origin < 0) || (origin >= VLENGTH)) {
         throw new ArrayIndexOutOfBoundsException("Index " + origin + " out of bounds for vector length " + VLENGTH);
       } else {
         Short512Shuffle Iota = iotaShuffle(origin);
         VectorMask<Short> BlendMask = Iota.toVector().compare(VectorOperators.GE, (broadcast((short)(origin))));
         Iota = (Short512Shuffle)iotaShuffle(origin).wrapIndexes();
         return ZERO.blend(this.rearrange(Iota), BlendMask);
       }
    }

    @Override
    @ForceInline
    public Short512Vector unslice(int origin, Vector<Short> w, int part) {
        return (Short512Vector) super.unsliceTemplate(origin, w, part);  // specialize
    }

    @Override
    @ForceInline
    public Short512Vector unslice(int origin, Vector<Short> w, int part, VectorMask<Short> m) {
        return (Short512Vector)
            super.unsliceTemplate(Short512Mask.class,
                                  origin, w, part,
                                  (Short512Mask) m);  // specialize
    }

    @Override
    @ForceInline
    public Short512Vector unslice(int origin) {
       if ((origin < 0) || (origin >= VLENGTH)) {
         throw new ArrayIndexOutOfBoundsException("Index " + origin + " out of bounds for vector length " + VLENGTH);
       } else {
         Short512Shuffle Iota = iotaShuffle(-origin);
         VectorMask<Short> BlendMask = Iota.toVector().compare(VectorOperators.GE, (broadcast((short)(0))));
         Iota = (Short512Shuffle)iotaShuffle(-origin).wrapIndexes();
         return ZERO.blend(this.rearrange(Iota), BlendMask);
       }
    }

    @Override
    @ForceInline
    public Short512Vector rearrange(VectorShuffle<Short> s) {
        return (Short512Vector)
            super.rearrangeTemplate(Short512Shuffle.class,
                                    (Short512Shuffle) s);  // specialize
    }

    @Override
    @ForceInline
    public Short512Vector rearrange(VectorShuffle<Short> shuffle,
                                  VectorMask<Short> m) {
        return (Short512Vector)
            super.rearrangeTemplate(Short512Shuffle.class,
                                    (Short512Shuffle) shuffle,
                                    (Short512Mask) m);  // specialize
    }

    @Override
    @ForceInline
    public Short512Vector rearrange(VectorShuffle<Short> s,
                                  Vector<Short> v) {
        return (Short512Vector)
            super.rearrangeTemplate(Short512Shuffle.class,
                                    (Short512Shuffle) s,
                                    (Short512Vector) v);  // specialize
    }

    @Override
    @ForceInline
    public Short512Vector selectFrom(Vector<Short> v) {
        return (Short512Vector)
            super.selectFromTemplate((Short512Vector) v);  // specialize
    }

    @Override
    @ForceInline
    public Short512Vector selectFrom(Vector<Short> v,
                                   VectorMask<Short> m) {
        return (Short512Vector)
            super.selectFromTemplate((Short512Vector) v,
                                     (Short512Mask) m);  // specialize
    }


    @Override
    public short lane(int i) {
        if (i < 0 || i >= VLENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + VLENGTH);
        }
        return (short) VectorIntrinsics.extract(
                                VCLASS, ETYPE, VLENGTH,
                                this, i,
                                (vec, ix) -> {
                                    short[] vecarr = vec.getElements();
                                    return (long)vecarr[ix];
                                });
    }

    @Override
    public Short512Vector withLane(int i, short e) {
        if (i < 0 || i >= VLENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + VLENGTH);
        }
        return VectorIntrinsics.insert(
                                VCLASS, ETYPE, VLENGTH,
                                this, i, (long)e,
                                (v, ix, bits) -> {
                                    short[] res = v.getElements().clone();
                                    res[ix] = (short)bits;
                                    return v.vectorFactory(res);
                                });
    }

    // Mask

    static final class Short512Mask extends AbstractMask<Short> {

        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public Short512Mask(boolean[] bits) {
            this(bits, 0);
        }

        public Short512Mask(boolean[] bits, int offset) {
            boolean[] a = new boolean[vspecies().laneCount()];
            for (int i = 0; i < a.length; i++) {
                a[i] = bits[offset + i];
            }
            this.bits = a;
        }

        public Short512Mask(boolean val) {
            boolean[] bits = new boolean[vspecies().laneCount()];
            Arrays.fill(bits, val);
            this.bits = bits;
        }

        @ForceInline
        final @Override
        public ShortSpecies vspecies() {
            // ISSUE:  This should probably be a @Stable
            // field inside AbstractMask, rather than
            // a megamorphic method.
            return VSPECIES;
        }

        boolean[] getBits() {
            return VectorIntrinsics.maybeRebox(this).bits;
        }

        @Override
        Short512Mask uOp(MUnOp f) {
            boolean[] res = new boolean[vspecies().laneCount()];
            boolean[] bits = getBits();
            for (int i = 0; i < res.length; i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Short512Mask(res);
        }

        @Override
        Short512Mask bOp(VectorMask<Short> m, MBinOp f) {
            boolean[] res = new boolean[vspecies().laneCount()];
            boolean[] bits = getBits();
            boolean[] mbits = ((Short512Mask)m).getBits();
            for (int i = 0; i < res.length; i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new Short512Mask(res);
        }

        @ForceInline
        @Override
        public final
        Short512Vector toVector() {
            return (Short512Vector) super.toVectorTemplate();  // specialize
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
        public Short512Mask not() {
            return (Short512Mask) VectorIntrinsics.unaryOp(
                                             VECTOR_OP_NOT, Short512Mask.class, short.class, VLENGTH,
                                             this,
                                             (m1) -> m1.uOp((i, a) -> !a));
        }

        // Binary operations

        @Override
        @ForceInline
        public Short512Mask and(VectorMask<Short> mask) {
            Objects.requireNonNull(mask);
            Short512Mask m = (Short512Mask)mask;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Short512Mask.class, short.class, VLENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Short512Mask or(VectorMask<Short> mask) {
            Objects.requireNonNull(mask);
            Short512Mask m = (Short512Mask)mask;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, Short512Mask.class, short.class, VLENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(BT_ne, Short512Mask.class, short.class, VLENGTH,
                                         this, this,
                                         (m, __) -> anyTrueHelper(((Short512Mask)m).getBits()));
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(BT_overflow, Short512Mask.class, short.class, VLENGTH,
                                         this, vspecies().maskAll(true),
                                         (m, __) -> allTrueHelper(((Short512Mask)m).getBits()));
        }

        /*package-private*/
        static Short512Mask maskAll(boolean bit) {
            return bit ? TRUE_MASK : FALSE_MASK;
        }
        static final Short512Mask TRUE_MASK = new Short512Mask(true);
        static final Short512Mask FALSE_MASK = new Short512Mask(false);
    }

    // Shuffle

    static final class Short512Shuffle extends AbstractShuffle<Short> {
        Short512Shuffle(byte[] reorder) {
            super(reorder);
        }

        public Short512Shuffle(int[] reorder) {
            super(reorder);
        }

        public Short512Shuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        public Short512Shuffle(IntUnaryOperator fn) {
            super(fn);
        }

        @Override
        public ShortSpecies vspecies() {
            return VSPECIES;
        }

        static {
            // There must be enough bits in the shuffle lanes to encode
            // VLENGTH valid indexes and VLENGTH exceptional ones.
            assert(VLENGTH < Byte.MAX_VALUE);
            assert(Byte.MIN_VALUE <= -VLENGTH);
        }
        static final Short512Shuffle IOTA = new Short512Shuffle(IDENTITY);

        @Override
        @ForceInline
        public Short512Vector toVector() {
            return VectorIntrinsics.shuffleToVector(VCLASS, ETYPE, Short512Shuffle.class, this, VLENGTH,
                                                    (s) -> ((Short512Vector)(((AbstractShuffle<Short>)(s)).toVectorTemplate())));
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
        public Short512Shuffle rearrange(VectorShuffle<Short> shuffle) {
            Short512Shuffle s = (Short512Shuffle) shuffle;
            byte[] r = new byte[reorder.length];
            for (int i = 0; i < reorder.length; i++) {
                int ssi = s.reorder[i];
                r[i] = this.reorder[ssi];  // throws on exceptional index
            }
            return new Short512Shuffle(r);
        }
    }

    // ================================================

    // Specialized low-level memory operations.

    @ForceInline
    @Override
    final
    ShortVector fromArray0(short[] a, int offset) {
        return super.fromArray0Template(a, offset);  // specialize
    }

    @ForceInline
    @Override
    final
    ShortVector fromByteArray0(byte[] a, int offset) {
        return super.fromByteArray0Template(a, offset);  // specialize
    }

    @ForceInline
    @Override
    final
    ShortVector fromByteBuffer0(ByteBuffer bb, int offset) {
        return super.fromByteBuffer0Template(bb, offset);  // specialize
    }

    @ForceInline
    @Override
    final
    void intoArray0(short[] a, int offset) {
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
