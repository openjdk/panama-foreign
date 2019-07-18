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
final class Byte128Vector extends ByteVector {
    static final ByteSpecies VSPECIES =
        (ByteSpecies) ByteVector.SPECIES_128;

    static final VectorShape VSHAPE =
        VSPECIES.vectorShape();

    static final Class<Byte128Vector> VCLASS = Byte128Vector.class;

    static final int VSIZE = VSPECIES.vectorBitSize();

    static final int VLENGTH = VSPECIES.laneCount();

    static final Class<Byte> ETYPE = byte.class;

    // The JVM expects to find the state here.
    private final byte[] vec; // Don't access directly, use getElements() instead.

    Byte128Vector(byte[] v) {
        vec = v;
    }

    // For compatibility as Byte128Vector::new,
    // stored into species.vectorFactory.
    Byte128Vector(Object v) {
        this((byte[]) v);
    }

    static final Byte128Vector ZERO = new Byte128Vector(new byte[VLENGTH]);
    static final Byte128Vector IOTA = new Byte128Vector(VSPECIES.iotaArray());

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
    public final Class<Byte> elementType() { return Byte.class; }

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
    public final Byte128Vector broadcast(byte e) {
        return (Byte128Vector) super.broadcastTemplate(e);  // specialize
    }

    @Override
    @ForceInline
    public final Byte128Vector broadcast(long e) {
        return (Byte128Vector) super.broadcastTemplate(e);  // specialize
    }

    @Override
    @ForceInline
    Byte128Mask maskFromArray(boolean[] bits) {
        return new Byte128Mask(bits);
    }

    @Override
    @ForceInline
    Byte128Shuffle iotaShuffle() { return Byte128Shuffle.IOTA; }

    @ForceInline
    Byte128Shuffle iotaShuffle(int start) { 
        return (Byte128Shuffle)VectorIntrinsics.shuffleIota(ETYPE, Byte128Shuffle.class, VSPECIES, VLENGTH, start, (val, l) -> new Byte128Shuffle(i -> (Byte128Shuffle.partiallyWrapIndex(i + val, l))));
    }

    @Override
    @ForceInline
    Byte128Shuffle shuffleFromBytes(byte[] reorder) { return new Byte128Shuffle(reorder); }

    @Override
    @ForceInline
    Byte128Shuffle shuffleFromArray(int[] indexes, int i) { return new Byte128Shuffle(indexes, i); }

    @Override
    @ForceInline
    Byte128Shuffle shuffleFromOp(IntUnaryOperator fn) { return new Byte128Shuffle(fn); }

    // Make a vector of the same species but the given elements:
    @ForceInline
    final @Override
    Byte128Vector vectorFactory(byte[] vec) {
        return new Byte128Vector(vec);
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
    Byte128Vector uOp(FUnOp f) {
        return (Byte128Vector) super.uOpTemplate(f);  // specialize
    }

    @ForceInline
    final @Override
    Byte128Vector uOp(VectorMask<Byte> m, FUnOp f) {
        return (Byte128Vector)
            super.uOpTemplate((Byte128Mask)m, f);  // specialize
    }

    // Binary operator

    @ForceInline
    final @Override
    Byte128Vector bOp(Vector<Byte> v, FBinOp f) {
        return (Byte128Vector) super.bOpTemplate((Byte128Vector)v, f);  // specialize
    }

    @ForceInline
    final @Override
    Byte128Vector bOp(Vector<Byte> v,
                     VectorMask<Byte> m, FBinOp f) {
        return (Byte128Vector)
            super.bOpTemplate((Byte128Vector)v, (Byte128Mask)m,
                              f);  // specialize
    }

    // Ternary operator

    @ForceInline
    final @Override
    Byte128Vector tOp(Vector<Byte> v1, Vector<Byte> v2, FTriOp f) {
        return (Byte128Vector)
            super.tOpTemplate((Byte128Vector)v1, (Byte128Vector)v2,
                              f);  // specialize
    }

    @ForceInline
    final @Override
    Byte128Vector tOp(Vector<Byte> v1, Vector<Byte> v2,
                     VectorMask<Byte> m, FTriOp f) {
        return (Byte128Vector)
            super.tOpTemplate((Byte128Vector)v1, (Byte128Vector)v2,
                              (Byte128Mask)m, f);  // specialize
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
    public Byte128Vector lanewise(Unary op) {
        return (Byte128Vector) super.lanewiseTemplate(op);  // specialize
    }

    @Override
    @ForceInline
    public Byte128Vector lanewise(Binary op, Vector<Byte> v) {
        return (Byte128Vector) super.lanewiseTemplate(op, v);  // specialize
    }

    /*package-private*/
    @Override
    @ForceInline Byte128Vector
    lanewiseShift(VectorOperators.Binary op, int e) {
        return (Byte128Vector) super.lanewiseShiftTemplate(op, e);  // specialize
    }

    /*package-private*/
    @Override
    @ForceInline
    public final
    Byte128Vector
    lanewise(VectorOperators.Ternary op, Vector<Byte> v1, Vector<Byte> v2) {
        return (Byte128Vector) super.lanewiseTemplate(op, v1, v2);  // specialize
    }

    @Override
    @ForceInline
    public final
    Byte128Vector addIndex(int scale) {
        return (Byte128Vector) super.addIndexTemplate(scale);  // specialize
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
    public final Byte128Mask test(Test op) {
        return super.testTemplate(Byte128Mask.class, op);  // specialize
    }

    // Specialized comparisons

    @Override
    @ForceInline
    public final Byte128Mask compare(Comparison op, Vector<Byte> v) {
        return super.compareTemplate(Byte128Mask.class, op, v);  // specialize
    }

    @Override
    @ForceInline
    public final Byte128Mask compare(Comparison op, byte s) {
        return super.compareTemplate(Byte128Mask.class, op, s);  // specialize
    }

    @Override
    @ForceInline
    public final Byte128Mask compare(Comparison op, long s) {
        return super.compareTemplate(Byte128Mask.class, op, s);  // specialize
    }

    @Override
    @ForceInline
    public Byte128Vector blend(Vector<Byte> v, VectorMask<Byte> m) {
        return (Byte128Vector)
            super.blendTemplate(Byte128Mask.class,
                                (Byte128Vector) v,
                                (Byte128Mask) m);  // specialize
    }

    @Override
    @ForceInline
    public Byte128Vector slice(int origin, Vector<Byte> v) {
        return (Byte128Vector) super.sliceTemplate(origin, v);  // specialize
    }

    @Override
    @ForceInline
    public Byte128Vector slice(int origin) {
       if ((origin < 0) || (origin >= VLENGTH)) {
         throw new ArrayIndexOutOfBoundsException("Index " + origin + " out of bounds for vector length " + VLENGTH);
       } else {
         Byte128Shuffle Iota = iotaShuffle(origin);
         VectorMask<Byte> BlendMask = Iota.toVector().compare(VectorOperators.GE, (broadcast((byte)(origin))));
         Iota = (Byte128Shuffle)iotaShuffle(origin).wrapIndexes();
         return ZERO.blend(this.rearrange(Iota), BlendMask);
       }
    }

    @Override
    @ForceInline
    public Byte128Vector unslice(int origin, Vector<Byte> w, int part) {
        return (Byte128Vector) super.unsliceTemplate(origin, w, part);  // specialize
    }

    @Override
    @ForceInline
    public Byte128Vector unslice(int origin, Vector<Byte> w, int part, VectorMask<Byte> m) {
        return (Byte128Vector)
            super.unsliceTemplate(Byte128Mask.class,
                                  origin, w, part,
                                  (Byte128Mask) m);  // specialize
    }

    @Override
    @ForceInline
    public Byte128Vector unslice(int origin) {
       if ((origin < 0) || (origin >= VLENGTH)) {
         throw new ArrayIndexOutOfBoundsException("Index " + origin + " out of bounds for vector length " + VLENGTH);
       } else {
         Byte128Shuffle Iota = iotaShuffle(-origin);
         VectorMask<Byte> BlendMask = Iota.toVector().compare(VectorOperators.GE, (broadcast((byte)(0))));
         Iota = (Byte128Shuffle)iotaShuffle(-origin).wrapIndexes();
         return ZERO.blend(this.rearrange(Iota), BlendMask);
       }
    }

    @Override
    @ForceInline
    public Byte128Vector rearrange(VectorShuffle<Byte> s) {
        return (Byte128Vector)
            super.rearrangeTemplate(Byte128Shuffle.class,
                                    (Byte128Shuffle) s);  // specialize
    }

    @Override
    @ForceInline
    public Byte128Vector rearrange(VectorShuffle<Byte> shuffle,
                                  VectorMask<Byte> m) {
        return (Byte128Vector)
            super.rearrangeTemplate(Byte128Shuffle.class,
                                    (Byte128Shuffle) shuffle,
                                    (Byte128Mask) m);  // specialize
    }

    @Override
    @ForceInline
    public Byte128Vector rearrange(VectorShuffle<Byte> s,
                                  Vector<Byte> v) {
        return (Byte128Vector)
            super.rearrangeTemplate(Byte128Shuffle.class,
                                    (Byte128Shuffle) s,
                                    (Byte128Vector) v);  // specialize
    }

    @Override
    @ForceInline
    public Byte128Vector selectFrom(Vector<Byte> v) {
        return (Byte128Vector)
            super.selectFromTemplate((Byte128Vector) v);  // specialize
    }

    @Override
    @ForceInline
    public Byte128Vector selectFrom(Vector<Byte> v,
                                   VectorMask<Byte> m) {
        return (Byte128Vector)
            super.selectFromTemplate((Byte128Vector) v,
                                     (Byte128Mask) m);  // specialize
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
    public Byte128Vector withLane(int i, byte e) {
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

    static final class Byte128Mask extends AbstractMask<Byte> {

        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public Byte128Mask(boolean[] bits) {
            this(bits, 0);
        }

        public Byte128Mask(boolean[] bits, int offset) {
            boolean[] a = new boolean[vspecies().laneCount()];
            for (int i = 0; i < a.length; i++) {
                a[i] = bits[offset + i];
            }
            this.bits = a;
        }

        public Byte128Mask(boolean val) {
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
        Byte128Mask uOp(MUnOp f) {
            boolean[] res = new boolean[vspecies().laneCount()];
            boolean[] bits = getBits();
            for (int i = 0; i < res.length; i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Byte128Mask(res);
        }

        @Override
        Byte128Mask bOp(VectorMask<Byte> m, MBinOp f) {
            boolean[] res = new boolean[vspecies().laneCount()];
            boolean[] bits = getBits();
            boolean[] mbits = ((Byte128Mask)m).getBits();
            for (int i = 0; i < res.length; i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new Byte128Mask(res);
        }

        @ForceInline
        @Override
        public final
        Byte128Vector toVector() {
            return (Byte128Vector) super.toVectorTemplate();  // specialize
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
        public Byte128Mask not() {
            return (Byte128Mask) VectorIntrinsics.unaryOp(
                                             VECTOR_OP_NOT, Byte128Mask.class, byte.class, VLENGTH,
                                             this,
                                             (m1) -> m1.uOp((i, a) -> !a));
        }

        // Binary operations

        @Override
        @ForceInline
        public Byte128Mask and(VectorMask<Byte> mask) {
            Objects.requireNonNull(mask);
            Byte128Mask m = (Byte128Mask)mask;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Byte128Mask.class, byte.class, VLENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Byte128Mask or(VectorMask<Byte> mask) {
            Objects.requireNonNull(mask);
            Byte128Mask m = (Byte128Mask)mask;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, Byte128Mask.class, byte.class, VLENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(BT_ne, Byte128Mask.class, byte.class, VLENGTH,
                                         this, this,
                                         (m, __) -> anyTrueHelper(((Byte128Mask)m).getBits()));
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(BT_overflow, Byte128Mask.class, byte.class, VLENGTH,
                                         this, vspecies().maskAll(true),
                                         (m, __) -> allTrueHelper(((Byte128Mask)m).getBits()));
        }

        /*package-private*/
        static Byte128Mask maskAll(boolean bit) {
            return bit ? TRUE_MASK : FALSE_MASK;
        }
        static final Byte128Mask TRUE_MASK = new Byte128Mask(true);
        static final Byte128Mask FALSE_MASK = new Byte128Mask(false);
    }

    // Shuffle

    static final class Byte128Shuffle extends AbstractShuffle<Byte> {
        Byte128Shuffle(byte[] reorder) {
            super(reorder);
        }

        public Byte128Shuffle(int[] reorder) {
            super(reorder);
        }

        public Byte128Shuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        public Byte128Shuffle(IntUnaryOperator fn) {
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
        static final Byte128Shuffle IOTA = new Byte128Shuffle(IDENTITY);

        @Override
        @ForceInline
        public Byte128Vector toVector() {
            return VectorIntrinsics.shuffleToVector(VCLASS, ETYPE, Byte128Shuffle.class, this, VLENGTH,
                                                    (s) -> ((Byte128Vector)(((AbstractShuffle<Byte>)(s)).toVectorTemplate())));
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
        public Byte128Shuffle rearrange(VectorShuffle<Byte> shuffle) {
            Byte128Shuffle s = (Byte128Shuffle) shuffle;
            byte[] r = new byte[reorder.length];
            for (int i = 0; i < reorder.length; i++) {
                int ssi = s.reorder[i];
                r[i] = this.reorder[ssi];  // throws on exceptional index
            }
            return new Byte128Shuffle(r);
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
