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
final class Byte512Vector extends ByteVector {
    static final ByteSpecies VSPECIES =
        (ByteSpecies) ByteVector.SPECIES_512;

    static final VectorShape VSHAPE =
        VSPECIES.vectorShape();

    static final Class<Byte512Vector> VCLASS = Byte512Vector.class;

    static final int VSIZE = VSPECIES.vectorBitSize();

    static final int VLENGTH = VSPECIES.laneCount();

    static final Class<Byte> ETYPE = byte.class;

    // The JVM expects to find the state here.
    private final byte[] vec; // Don't access directly, use getElements() instead.

    Byte512Vector(byte[] v) {
        vec = v;
    }

    // For compatibility as Byte512Vector::new,
    // stored into species.vectorFactory.
    Byte512Vector(Object v) {
        this((byte[]) v);
    }

    static final Byte512Vector ZERO = new Byte512Vector(new byte[VLENGTH]);
    static final Byte512Vector IOTA = new Byte512Vector(VSPECIES.iotaArray());

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
    public ByteSpecies vspecies() {
        // ISSUE:  This should probably be a @Stable
        // field inside AbstractVector, rather than
        // a megamorphic method.
        return VSPECIES;
    }

    @ForceInline
    @Override
    public final Class<Byte> elementType() { return byte.class; }

    @ForceInline
    @Override
    public final int elementSize() { return Byte.SIZE; }

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
    byte[] getElements() {
        return VectorIntrinsics.maybeRebox(this).vec;
    }

    // Virtualized constructors

    @Override
    @ForceInline
    public final Byte512Vector broadcast(byte e) {
        return (Byte512Vector) super.broadcastTemplate(e);  // specialize
    }

    @Override
    @ForceInline
    public final Byte512Vector broadcast(long e) {
        return (Byte512Vector) super.broadcastTemplate(e);  // specialize
    }

    @Override
    @ForceInline
    Byte512Mask maskFromArray(boolean[] bits) {
        return new Byte512Mask(bits);
    }

    @Override
    @ForceInline
    Byte512Shuffle iotaShuffle() { return Byte512Shuffle.IOTA; }

    @ForceInline
    Byte512Shuffle iotaShuffle(int start) {
        return (Byte512Shuffle)VectorIntrinsics.shuffleIota(ETYPE, Byte512Shuffle.class, VSPECIES, VLENGTH, start, (val, l) -> new Byte512Shuffle(i -> (VectorIntrinsics.wrapToRange(i + val, l))));
    }

    @Override
    @ForceInline
    Byte512Shuffle shuffleFromBytes(byte[] reorder) { return new Byte512Shuffle(reorder); }

    @Override
    @ForceInline
    Byte512Shuffle shuffleFromArray(int[] indexes, int i) { return new Byte512Shuffle(indexes, i); }

    @Override
    @ForceInline
    Byte512Shuffle shuffleFromOp(IntUnaryOperator fn) { return new Byte512Shuffle(fn); }

    // Make a vector of the same species but the given elements:
    @ForceInline
    final @Override
    Byte512Vector vectorFactory(byte[] vec) {
        return new Byte512Vector(vec);
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
    Byte512Vector uOp(FUnOp f) {
        return (Byte512Vector) super.uOpTemplate(f);  // specialize
    }

    @ForceInline
    final @Override
    Byte512Vector uOp(VectorMask<Byte> m, FUnOp f) {
        return (Byte512Vector)
            super.uOpTemplate((Byte512Mask)m, f);  // specialize
    }

    // Binary operator

    @ForceInline
    final @Override
    Byte512Vector bOp(Vector<Byte> v, FBinOp f) {
        return (Byte512Vector) super.bOpTemplate((Byte512Vector)v, f);  // specialize
    }

    @ForceInline
    final @Override
    Byte512Vector bOp(Vector<Byte> v,
                     VectorMask<Byte> m, FBinOp f) {
        return (Byte512Vector)
            super.bOpTemplate((Byte512Vector)v, (Byte512Mask)m,
                              f);  // specialize
    }

    // Ternary operator

    @ForceInline
    final @Override
    Byte512Vector tOp(Vector<Byte> v1, Vector<Byte> v2, FTriOp f) {
        return (Byte512Vector)
            super.tOpTemplate((Byte512Vector)v1, (Byte512Vector)v2,
                              f);  // specialize
    }

    @ForceInline
    final @Override
    Byte512Vector tOp(Vector<Byte> v1, Vector<Byte> v2,
                     VectorMask<Byte> m, FTriOp f) {
        return (Byte512Vector)
            super.tOpTemplate((Byte512Vector)v1, (Byte512Vector)v2,
                              (Byte512Mask)m, f);  // specialize
    }

    @ForceInline
    final @Override
    byte rOp(byte v, FBinOp f) {
        return super.rOpTemplate(v, f);  // specialize
    }

    @Override
    @ForceInline
    public final <F>
    Vector<F> convertShape(VectorOperators.Conversion<Byte,F> conv,
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
    public Byte512Vector lanewise(Unary op) {
        return (Byte512Vector) super.lanewiseTemplate(op);  // specialize
    }

    @Override
    @ForceInline
    public Byte512Vector lanewise(Binary op, Vector<Byte> v) {
        return (Byte512Vector) super.lanewiseTemplate(op, v);  // specialize
    }

    /*package-private*/
    @Override
    @ForceInline Byte512Vector
    lanewiseShift(VectorOperators.Binary op, int e) {
        return (Byte512Vector) super.lanewiseShiftTemplate(op, e);  // specialize
    }

    /*package-private*/
    @Override
    @ForceInline
    public final
    Byte512Vector
    lanewise(VectorOperators.Ternary op, Vector<Byte> v1, Vector<Byte> v2) {
        return (Byte512Vector) super.lanewiseTemplate(op, v1, v2);  // specialize
    }

    @Override
    @ForceInline
    public final
    Byte512Vector addIndex(int scale) {
        return (Byte512Vector) super.addIndexTemplate(scale);  // specialize
    }

    // Type specific horizontal reductions

    @Override
    @ForceInline
    public final byte reduceLanes(VectorOperators.Associative op) {
        return super.reduceLanesTemplate(op);  // specialized
    }

    @Override
    @ForceInline
    public final byte reduceLanes(VectorOperators.Associative op,
                                    VectorMask<Byte> m) {
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
                                        VectorMask<Byte> m) {
        return (long) super.reduceLanesTemplate(op, m);  // specialized
    }

    @Override
    @ForceInline
    public VectorShuffle<Byte> toShuffle() {
        byte[] a = toArray();
        int[] sa = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            sa[i] = (int) a[i];
        }
        return VectorShuffle.fromArray(VSPECIES, sa, 0);
    }

    // Specialized unary testing

    @Override
    @ForceInline
    public final Byte512Mask test(Test op) {
        return super.testTemplate(Byte512Mask.class, op);  // specialize
    }

    // Specialized comparisons

    @Override
    @ForceInline
    public final Byte512Mask compare(Comparison op, Vector<Byte> v) {
        return super.compareTemplate(Byte512Mask.class, op, v);  // specialize
    }

    @Override
    @ForceInline
    public final Byte512Mask compare(Comparison op, byte s) {
        return super.compareTemplate(Byte512Mask.class, op, s);  // specialize
    }

    @Override
    @ForceInline
    public final Byte512Mask compare(Comparison op, long s) {
        return super.compareTemplate(Byte512Mask.class, op, s);  // specialize
    }

    @Override
    @ForceInline
    public Byte512Vector blend(Vector<Byte> v, VectorMask<Byte> m) {
        return (Byte512Vector)
            super.blendTemplate(Byte512Mask.class,
                                (Byte512Vector) v,
                                (Byte512Mask) m);  // specialize
    }

    @Override
    @ForceInline
    public Byte512Vector slice(int origin, Vector<Byte> v) {
        return (Byte512Vector) super.sliceTemplate(origin, v);  // specialize
    }

    @Override
    @ForceInline
    public Byte512Vector slice(int origin) {
       if ((origin < 0) || (origin >= VLENGTH)) {
         throw new ArrayIndexOutOfBoundsException("Index " + origin + " out of bounds for vector length " + VLENGTH);
       } else {
         Byte512Shuffle Iota = (Byte512Shuffle)VectorShuffle.iota(VSPECIES, 0, 1, true);
         VectorMask<Byte> BlendMask = Iota.toVector().compare(VectorOperators.LT, (broadcast((byte)(VLENGTH-origin))));
         Iota = (Byte512Shuffle)VectorShuffle.iota(VSPECIES, origin, 1, true);
         return ZERO.blend(this.rearrange(Iota), BlendMask);
       }
    }

    @Override
    @ForceInline
    public Byte512Vector unslice(int origin, Vector<Byte> w, int part) {
        return (Byte512Vector) super.unsliceTemplate(origin, w, part);  // specialize
    }

    @Override
    @ForceInline
    public Byte512Vector unslice(int origin, Vector<Byte> w, int part, VectorMask<Byte> m) {
        return (Byte512Vector)
            super.unsliceTemplate(Byte512Mask.class,
                                  origin, w, part,
                                  (Byte512Mask) m);  // specialize
    }

    @Override
    @ForceInline
    public Byte512Vector unslice(int origin) {
       if ((origin < 0) || (origin >= VLENGTH)) {
         throw new ArrayIndexOutOfBoundsException("Index " + origin + " out of bounds for vector length " + VLENGTH);
       } else {
         Byte512Shuffle Iota = (Byte512Shuffle)VectorShuffle.iota(VSPECIES, 0, 1, true);
         VectorMask<Byte> BlendMask = Iota.toVector().compare(VectorOperators.GE, (broadcast((byte)(origin))));
         Iota = (Byte512Shuffle)VectorShuffle.iota(VSPECIES, -origin, 1, true);
         return ZERO.blend(this.rearrange(Iota), BlendMask);
       }
    }

    @Override
    @ForceInline
    public Byte512Vector rearrange(VectorShuffle<Byte> s) {
        return (Byte512Vector)
            super.rearrangeTemplate(Byte512Shuffle.class,
                                    (Byte512Shuffle) s);  // specialize
    }

    @Override
    @ForceInline
    public Byte512Vector rearrange(VectorShuffle<Byte> shuffle,
                                  VectorMask<Byte> m) {
        return (Byte512Vector)
            super.rearrangeTemplate(Byte512Shuffle.class,
                                    (Byte512Shuffle) shuffle,
                                    (Byte512Mask) m);  // specialize
    }

    @Override
    @ForceInline
    public Byte512Vector rearrange(VectorShuffle<Byte> s,
                                  Vector<Byte> v) {
        return (Byte512Vector)
            super.rearrangeTemplate(Byte512Shuffle.class,
                                    (Byte512Shuffle) s,
                                    (Byte512Vector) v);  // specialize
    }

    @Override
    @ForceInline
    public Byte512Vector selectFrom(Vector<Byte> v) {
        return (Byte512Vector)
            super.selectFromTemplate((Byte512Vector) v);  // specialize
    }

    @Override
    @ForceInline
    public Byte512Vector selectFrom(Vector<Byte> v,
                                   VectorMask<Byte> m) {
        return (Byte512Vector)
            super.selectFromTemplate((Byte512Vector) v,
                                     (Byte512Mask) m);  // specialize
    }


    @Override
    public byte lane(int i) {
        if (i < 0 || i >= VLENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + VLENGTH);
        }
        return (byte) VectorIntrinsics.extract(
                                VCLASS, ETYPE, VLENGTH,
                                this, i,
                                (vec, ix) -> {
                                    byte[] vecarr = vec.getElements();
                                    return (long)vecarr[ix];
                                });
    }

    @Override
    public Byte512Vector withLane(int i, byte e) {
        if (i < 0 || i >= VLENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + VLENGTH);
        }
        return VectorIntrinsics.insert(
                                VCLASS, ETYPE, VLENGTH,
                                this, i, (long)e,
                                (v, ix, bits) -> {
                                    byte[] res = v.getElements().clone();
                                    res[ix] = (byte)bits;
                                    return v.vectorFactory(res);
                                });
    }

    // Mask

    static final class Byte512Mask extends AbstractMask<Byte> {

        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public Byte512Mask(boolean[] bits) {
            this(bits, 0);
        }

        public Byte512Mask(boolean[] bits, int offset) {
            boolean[] a = new boolean[vspecies().laneCount()];
            for (int i = 0; i < a.length; i++) {
                a[i] = bits[offset + i];
            }
            this.bits = a;
        }

        public Byte512Mask(boolean val) {
            boolean[] bits = new boolean[vspecies().laneCount()];
            Arrays.fill(bits, val);
            this.bits = bits;
        }

        @ForceInline
        final @Override
        public ByteSpecies vspecies() {
            // ISSUE:  This should probably be a @Stable
            // field inside AbstractMask, rather than
            // a megamorphic method.
            return VSPECIES;
        }

        boolean[] getBits() {
            return VectorIntrinsics.maybeRebox(this).bits;
        }

        @Override
        Byte512Mask uOp(MUnOp f) {
            boolean[] res = new boolean[vspecies().laneCount()];
            boolean[] bits = getBits();
            for (int i = 0; i < res.length; i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Byte512Mask(res);
        }

        @Override
        Byte512Mask bOp(VectorMask<Byte> m, MBinOp f) {
            boolean[] res = new boolean[vspecies().laneCount()];
            boolean[] bits = getBits();
            boolean[] mbits = ((Byte512Mask)m).getBits();
            for (int i = 0; i < res.length; i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new Byte512Mask(res);
        }

        @ForceInline
        @Override
        public final
        Byte512Vector toVector() {
            return (Byte512Vector) super.toVectorTemplate();  // specialize
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
        public Byte512Mask not() {
            return (Byte512Mask) VectorIntrinsics.unaryOp(
                                             VECTOR_OP_NOT, Byte512Mask.class, byte.class, VLENGTH,
                                             this,
                                             (m1) -> m1.uOp((i, a) -> !a));
        }

        // Binary operations

        @Override
        @ForceInline
        public Byte512Mask and(VectorMask<Byte> mask) {
            Objects.requireNonNull(mask);
            Byte512Mask m = (Byte512Mask)mask;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Byte512Mask.class, byte.class, VLENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Byte512Mask or(VectorMask<Byte> mask) {
            Objects.requireNonNull(mask);
            Byte512Mask m = (Byte512Mask)mask;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, Byte512Mask.class, byte.class, VLENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(BT_ne, Byte512Mask.class, byte.class, VLENGTH,
                                         this, this,
                                         (m, __) -> anyTrueHelper(((Byte512Mask)m).getBits()));
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(BT_overflow, Byte512Mask.class, byte.class, VLENGTH,
                                         this, vspecies().maskAll(true),
                                         (m, __) -> allTrueHelper(((Byte512Mask)m).getBits()));
        }

        /*package-private*/
        static Byte512Mask maskAll(boolean bit) {
            return bit ? TRUE_MASK : FALSE_MASK;
        }
        static final Byte512Mask TRUE_MASK = new Byte512Mask(true);
        static final Byte512Mask FALSE_MASK = new Byte512Mask(false);
    }

    // Shuffle

    static final class Byte512Shuffle extends AbstractShuffle<Byte> {
        Byte512Shuffle(byte[] reorder) {
            super(reorder);
        }

        public Byte512Shuffle(int[] reorder) {
            super(reorder);
        }

        public Byte512Shuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        public Byte512Shuffle(IntUnaryOperator fn) {
            super(fn);
        }

        @Override
        public ByteSpecies vspecies() {
            return VSPECIES;
        }

        static {
            // There must be enough bits in the shuffle lanes to encode
            // VLENGTH valid indexes and VLENGTH exceptional ones.
            assert(VLENGTH < Byte.MAX_VALUE);
            assert(Byte.MIN_VALUE <= -VLENGTH);
        }
        static final Byte512Shuffle IOTA = new Byte512Shuffle(IDENTITY);

        @Override
        @ForceInline
        public Byte512Vector toVector() {
            return VectorIntrinsics.shuffleToVector(VCLASS, ETYPE, Byte512Shuffle.class, this, VLENGTH,
                                                    (s) -> ((Byte512Vector)(((AbstractShuffle<Byte>)(s)).toVectorTemplate())));
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
        public Byte512Shuffle rearrange(VectorShuffle<Byte> shuffle) {
            Byte512Shuffle s = (Byte512Shuffle) shuffle;
            byte[] r = new byte[reorder.length];
            for (int i = 0; i < reorder.length; i++) {
                int ssi = s.reorder[i];
                r[i] = this.reorder[ssi];  // throws on exceptional index
            }
            return new Byte512Shuffle(r);
        }
    }

    // ================================================

    // Specialized low-level memory operations.

    @ForceInline
    @Override
    final
    ByteVector fromArray0(byte[] a, int offset) {
        return super.fromArray0Template(a, offset);  // specialize
    }

    @ForceInline
    @Override
    final
    ByteVector fromByteArray0(byte[] a, int offset) {
        return super.fromByteArray0Template(a, offset);  // specialize
    }

    @ForceInline
    @Override
    final
    ByteVector fromByteBuffer0(ByteBuffer bb, int offset) {
        return super.fromByteBuffer0Template(bb, offset);  // specialize
    }

    @ForceInline
    @Override
    final
    void intoArray0(byte[] a, int offset) {
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
