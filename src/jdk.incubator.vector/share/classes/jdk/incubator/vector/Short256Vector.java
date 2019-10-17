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
final class Short256Vector extends ShortVector {
    static final ShortSpecies VSPECIES =
        (ShortSpecies) ShortVector.SPECIES_256;

    static final VectorShape VSHAPE =
        VSPECIES.vectorShape();

    static final Class<Short256Vector> VCLASS = Short256Vector.class;

    static final int VSIZE = VSPECIES.vectorBitSize();

    static final int VLENGTH = VSPECIES.laneCount();

    static final Class<Short> ETYPE = short.class;

    // The JVM expects to find the state here.
    private final short[] vec; // Don't access directly, use getElements() instead.

    Short256Vector(short[] v) {
        vec = v;
    }

    // For compatibility as Short256Vector::new,
    // stored into species.vectorFactory.
    Short256Vector(Object v) {
        this((short[]) v);
    }

    static final Short256Vector ZERO = new Short256Vector(new short[VLENGTH]);
    static final Short256Vector IOTA = new Short256Vector(VSPECIES.iotaArray());

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
    public final Short256Vector broadcast(short e) {
        return (Short256Vector) super.broadcastTemplate(e);  // specialize
    }

    @Override
    @ForceInline
    public final Short256Vector broadcast(long e) {
        return (Short256Vector) super.broadcastTemplate(e);  // specialize
    }

    @Override
    @ForceInline
    Short256Mask maskFromArray(boolean[] bits) {
        return new Short256Mask(bits);
    }

    @Override
    @ForceInline
    Short256Shuffle iotaShuffle() { return Short256Shuffle.IOTA; }

    @ForceInline
    Short256Shuffle iotaShuffle(int start) { 
        return (Short256Shuffle)VectorIntrinsics.shuffleIota(ETYPE, Short256Shuffle.class, VSPECIES, VLENGTH, start, (val, l) -> new Short256Shuffle(i -> (VectorIntrinsics.wrapToRange(i + val, l))));
    }

    @Override
    @ForceInline
    Short256Shuffle shuffleFromBytes(byte[] reorder) { return new Short256Shuffle(reorder); }

    @Override
    @ForceInline
    Short256Shuffle shuffleFromArray(int[] indexes, int i) { return new Short256Shuffle(indexes, i); }

    @Override
    @ForceInline
    Short256Shuffle shuffleFromOp(IntUnaryOperator fn) { return new Short256Shuffle(fn); }

    // Make a vector of the same species but the given elements:
    @ForceInline
    final @Override
    Short256Vector vectorFactory(short[] vec) {
        return new Short256Vector(vec);
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
    Short256Vector uOp(FUnOp f) {
        return (Short256Vector) super.uOpTemplate(f);  // specialize
    }

    @ForceInline
    final @Override
    Short256Vector uOp(VectorMask<Short> m, FUnOp f) {
        return (Short256Vector)
            super.uOpTemplate((Short256Mask)m, f);  // specialize
    }

    // Binary operator

    @ForceInline
    final @Override
    Short256Vector bOp(Vector<Short> v, FBinOp f) {
        return (Short256Vector) super.bOpTemplate((Short256Vector)v, f);  // specialize
    }

    @ForceInline
    final @Override
    Short256Vector bOp(Vector<Short> v,
                     VectorMask<Short> m, FBinOp f) {
        return (Short256Vector)
            super.bOpTemplate((Short256Vector)v, (Short256Mask)m,
                              f);  // specialize
    }

    // Ternary operator

    @ForceInline
    final @Override
    Short256Vector tOp(Vector<Short> v1, Vector<Short> v2, FTriOp f) {
        return (Short256Vector)
            super.tOpTemplate((Short256Vector)v1, (Short256Vector)v2,
                              f);  // specialize
    }

    @ForceInline
    final @Override
    Short256Vector tOp(Vector<Short> v1, Vector<Short> v2,
                     VectorMask<Short> m, FTriOp f) {
        return (Short256Vector)
            super.tOpTemplate((Short256Vector)v1, (Short256Vector)v2,
                              (Short256Mask)m, f);  // specialize
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
    public Short256Vector lanewise(Unary op) {
        return (Short256Vector) super.lanewiseTemplate(op);  // specialize
    }

    @Override
    @ForceInline
    public Short256Vector lanewise(Binary op, Vector<Short> v) {
        return (Short256Vector) super.lanewiseTemplate(op, v);  // specialize
    }

    /*package-private*/
    @Override
    @ForceInline Short256Vector
    lanewiseShift(VectorOperators.Binary op, int e) {
        return (Short256Vector) super.lanewiseShiftTemplate(op, e);  // specialize
    }

    /*package-private*/
    @Override
    @ForceInline
    public final
    Short256Vector
    lanewise(VectorOperators.Ternary op, Vector<Short> v1, Vector<Short> v2) {
        return (Short256Vector) super.lanewiseTemplate(op, v1, v2);  // specialize
    }

    @Override
    @ForceInline
    public final
    Short256Vector addIndex(int scale) {
        return (Short256Vector) super.addIndexTemplate(scale);  // specialize
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
    public final Short256Mask test(Test op) {
        return super.testTemplate(Short256Mask.class, op);  // specialize
    }

    // Specialized comparisons

    @Override
    @ForceInline
    public final Short256Mask compare(Comparison op, Vector<Short> v) {
        return super.compareTemplate(Short256Mask.class, op, v);  // specialize
    }

    @Override
    @ForceInline
    public final Short256Mask compare(Comparison op, short s) {
        return super.compareTemplate(Short256Mask.class, op, s);  // specialize
    }

    @Override
    @ForceInline
    public final Short256Mask compare(Comparison op, long s) {
        return super.compareTemplate(Short256Mask.class, op, s);  // specialize
    }

    @Override
    @ForceInline
    public Short256Vector blend(Vector<Short> v, VectorMask<Short> m) {
        return (Short256Vector)
            super.blendTemplate(Short256Mask.class,
                                (Short256Vector) v,
                                (Short256Mask) m);  // specialize
    }

    @Override
    @ForceInline
    public Short256Vector slice(int origin, Vector<Short> v) {
        return (Short256Vector) super.sliceTemplate(origin, v);  // specialize
    }

    @Override
    @ForceInline
    public Short256Vector slice(int origin) {
       if ((origin < 0) || (origin >= VLENGTH)) {
         throw new ArrayIndexOutOfBoundsException("Index " + origin + " out of bounds for vector length " + VLENGTH);
       } else {
         Short256Shuffle Iota = (Short256Shuffle)VectorShuffle.iota(VSPECIES, 0, 1, true);
         VectorMask<Short> BlendMask = Iota.toVector().compare(VectorOperators.LT, (broadcast((short)(VLENGTH-origin))));
         Iota = (Short256Shuffle)VectorShuffle.iota(VSPECIES, origin, 1, true);
         return ZERO.blend(this.rearrange(Iota), BlendMask);
       }
    }

    @Override
    @ForceInline
    public Short256Vector unslice(int origin, Vector<Short> w, int part) {
        return (Short256Vector) super.unsliceTemplate(origin, w, part);  // specialize
    }

    @Override
    @ForceInline
    public Short256Vector unslice(int origin, Vector<Short> w, int part, VectorMask<Short> m) {
        return (Short256Vector)
            super.unsliceTemplate(Short256Mask.class,
                                  origin, w, part,
                                  (Short256Mask) m);  // specialize
    }

    @Override
    @ForceInline
    public Short256Vector unslice(int origin) {
       if ((origin < 0) || (origin >= VLENGTH)) {
         throw new ArrayIndexOutOfBoundsException("Index " + origin + " out of bounds for vector length " + VLENGTH);
       } else {
         Short256Shuffle Iota = (Short256Shuffle)VectorShuffle.iota(VSPECIES, 0, 1, true);
         VectorMask<Short> BlendMask = Iota.toVector().compare(VectorOperators.GE, (broadcast((short)(origin))));
         Iota = (Short256Shuffle)VectorShuffle.iota(VSPECIES, -origin, 1, true);
         return ZERO.blend(this.rearrange(Iota), BlendMask);
       }
    }

    @Override
    @ForceInline
    public Short256Vector rearrange(VectorShuffle<Short> s) {
        return (Short256Vector)
            super.rearrangeTemplate(Short256Shuffle.class,
                                    (Short256Shuffle) s);  // specialize
    }

    @Override
    @ForceInline
    public Short256Vector rearrange(VectorShuffle<Short> shuffle,
                                  VectorMask<Short> m) {
        return (Short256Vector)
            super.rearrangeTemplate(Short256Shuffle.class,
                                    (Short256Shuffle) shuffle,
                                    (Short256Mask) m);  // specialize
    }

    @Override
    @ForceInline
    public Short256Vector rearrange(VectorShuffle<Short> s,
                                  Vector<Short> v) {
        return (Short256Vector)
            super.rearrangeTemplate(Short256Shuffle.class,
                                    (Short256Shuffle) s,
                                    (Short256Vector) v);  // specialize
    }

    @Override
    @ForceInline
    public Short256Vector selectFrom(Vector<Short> v) {
        return (Short256Vector)
            super.selectFromTemplate((Short256Vector) v);  // specialize
    }

    @Override
    @ForceInline
    public Short256Vector selectFrom(Vector<Short> v,
                                   VectorMask<Short> m) {
        return (Short256Vector)
            super.selectFromTemplate((Short256Vector) v,
                                     (Short256Mask) m);  // specialize
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
    public Short256Vector withLane(int i, short e) {
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

    static final class Short256Mask extends AbstractMask<Short> {

        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public Short256Mask(boolean[] bits) {
            this(bits, 0);
        }

        public Short256Mask(boolean[] bits, int offset) {
            boolean[] a = new boolean[vspecies().laneCount()];
            for (int i = 0; i < a.length; i++) {
                a[i] = bits[offset + i];
            }
            this.bits = a;
        }

        public Short256Mask(boolean val) {
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
        Short256Mask uOp(MUnOp f) {
            boolean[] res = new boolean[vspecies().laneCount()];
            boolean[] bits = getBits();
            for (int i = 0; i < res.length; i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Short256Mask(res);
        }

        @Override
        Short256Mask bOp(VectorMask<Short> m, MBinOp f) {
            boolean[] res = new boolean[vspecies().laneCount()];
            boolean[] bits = getBits();
            boolean[] mbits = ((Short256Mask)m).getBits();
            for (int i = 0; i < res.length; i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new Short256Mask(res);
        }

        @ForceInline
        @Override
        public final
        Short256Vector toVector() {
            return (Short256Vector) super.toVectorTemplate();  // specialize
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
        public Short256Mask not() {
            return (Short256Mask) VectorIntrinsics.unaryOp(
                                             VECTOR_OP_NOT, Short256Mask.class, short.class, VLENGTH,
                                             this,
                                             (m1) -> m1.uOp((i, a) -> !a));
        }

        // Binary operations

        @Override
        @ForceInline
        public Short256Mask and(VectorMask<Short> mask) {
            Objects.requireNonNull(mask);
            Short256Mask m = (Short256Mask)mask;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Short256Mask.class, short.class, VLENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Short256Mask or(VectorMask<Short> mask) {
            Objects.requireNonNull(mask);
            Short256Mask m = (Short256Mask)mask;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, Short256Mask.class, short.class, VLENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(BT_ne, Short256Mask.class, short.class, VLENGTH,
                                         this, this,
                                         (m, __) -> anyTrueHelper(((Short256Mask)m).getBits()));
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(BT_overflow, Short256Mask.class, short.class, VLENGTH,
                                         this, vspecies().maskAll(true),
                                         (m, __) -> allTrueHelper(((Short256Mask)m).getBits()));
        }

        /*package-private*/
        static Short256Mask maskAll(boolean bit) {
            return bit ? TRUE_MASK : FALSE_MASK;
        }
        static final Short256Mask TRUE_MASK = new Short256Mask(true);
        static final Short256Mask FALSE_MASK = new Short256Mask(false);
    }

    // Shuffle

    static final class Short256Shuffle extends AbstractShuffle<Short> {
        Short256Shuffle(byte[] reorder) {
            super(reorder);
        }

        public Short256Shuffle(int[] reorder) {
            super(reorder);
        }

        public Short256Shuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        public Short256Shuffle(IntUnaryOperator fn) {
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
        static final Short256Shuffle IOTA = new Short256Shuffle(IDENTITY);

        @Override
        @ForceInline
        public Short256Vector toVector() {
            return VectorIntrinsics.shuffleToVector(VCLASS, ETYPE, Short256Shuffle.class, this, VLENGTH,
                                                    (s) -> ((Short256Vector)(((AbstractShuffle<Short>)(s)).toVectorTemplate())));
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
        public Short256Shuffle rearrange(VectorShuffle<Short> shuffle) {
            Short256Shuffle s = (Short256Shuffle) shuffle;
            byte[] r = new byte[reorder.length];
            for (int i = 0; i < reorder.length; i++) {
                int ssi = s.reorder[i];
                r[i] = this.reorder[ssi];  // throws on exceptional index
            }
            return new Short256Shuffle(r);
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
