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
final class ByteMaxVector extends ByteVector {
    static final ByteSpecies VSPECIES =
        (ByteSpecies) ByteVector.SPECIES_MAX;

    static final VectorShape VSHAPE =
        VSPECIES.vectorShape();

    static final Class<ByteMaxVector> VCLASS = ByteMaxVector.class;

    static final int VSIZE = VSPECIES.vectorBitSize();

    static final int VLENGTH = VSPECIES.laneCount();

    static final Class<Byte> ETYPE = byte.class;

    // The JVM expects to find the state here.
    private final byte[] vec; // Don't access directly, use getElements() instead.

    ByteMaxVector(byte[] v) {
        vec = v;
    }

    // For compatibility as ByteMaxVector::new,
    // stored into species.vectorFactory.
    ByteMaxVector(Object v) {
        this((byte[]) v);
    }

    static final ByteMaxVector ZERO = new ByteMaxVector(new byte[VLENGTH]);
    static final ByteMaxVector IOTA = new ByteMaxVector(VSPECIES.iotaArray());

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
    public final ByteMaxVector broadcast(byte e) {
        return (ByteMaxVector) super.broadcastTemplate(e);  // specialize
    }

    @Override
    @ForceInline
    public final ByteMaxVector broadcast(long e) {
        return (ByteMaxVector) super.broadcastTemplate(e);  // specialize
    }

    @Override
    @ForceInline
    ByteMaxMask maskFromArray(boolean[] bits) {
        return new ByteMaxMask(bits);
    }

    @Override
    @ForceInline
    ByteMaxShuffle iotaShuffle() { return ByteMaxShuffle.IOTA; }

    @ForceInline
    ByteMaxShuffle iotaShuffle(int start) {
        return (ByteMaxShuffle)VectorIntrinsics.shuffleIota(ETYPE, ByteMaxShuffle.class, VSPECIES, VLENGTH, start, (val, l) -> new ByteMaxShuffle(i -> (VectorIntrinsics.wrapToRange(i + val, l))));
    }

    @Override
    @ForceInline
    ByteMaxShuffle shuffleFromBytes(byte[] reorder) { return new ByteMaxShuffle(reorder); }

    @Override
    @ForceInline
    ByteMaxShuffle shuffleFromArray(int[] indexes, int i) { return new ByteMaxShuffle(indexes, i); }

    @Override
    @ForceInline
    ByteMaxShuffle shuffleFromOp(IntUnaryOperator fn) { return new ByteMaxShuffle(fn); }

    // Make a vector of the same species but the given elements:
    @ForceInline
    final @Override
    ByteMaxVector vectorFactory(byte[] vec) {
        return new ByteMaxVector(vec);
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
    ByteMaxVector uOp(FUnOp f) {
        return (ByteMaxVector) super.uOpTemplate(f);  // specialize
    }

    @ForceInline
    final @Override
    ByteMaxVector uOp(VectorMask<Byte> m, FUnOp f) {
        return (ByteMaxVector)
            super.uOpTemplate((ByteMaxMask)m, f);  // specialize
    }

    // Binary operator

    @ForceInline
    final @Override
    ByteMaxVector bOp(Vector<Byte> v, FBinOp f) {
        return (ByteMaxVector) super.bOpTemplate((ByteMaxVector)v, f);  // specialize
    }

    @ForceInline
    final @Override
    ByteMaxVector bOp(Vector<Byte> v,
                     VectorMask<Byte> m, FBinOp f) {
        return (ByteMaxVector)
            super.bOpTemplate((ByteMaxVector)v, (ByteMaxMask)m,
                              f);  // specialize
    }

    // Ternary operator

    @ForceInline
    final @Override
    ByteMaxVector tOp(Vector<Byte> v1, Vector<Byte> v2, FTriOp f) {
        return (ByteMaxVector)
            super.tOpTemplate((ByteMaxVector)v1, (ByteMaxVector)v2,
                              f);  // specialize
    }

    @ForceInline
    final @Override
    ByteMaxVector tOp(Vector<Byte> v1, Vector<Byte> v2,
                     VectorMask<Byte> m, FTriOp f) {
        return (ByteMaxVector)
            super.tOpTemplate((ByteMaxVector)v1, (ByteMaxVector)v2,
                              (ByteMaxMask)m, f);  // specialize
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
    public ByteMaxVector lanewise(Unary op) {
        return (ByteMaxVector) super.lanewiseTemplate(op);  // specialize
    }

    @Override
    @ForceInline
    public ByteMaxVector lanewise(Binary op, Vector<Byte> v) {
        return (ByteMaxVector) super.lanewiseTemplate(op, v);  // specialize
    }

    /*package-private*/
    @Override
    @ForceInline ByteMaxVector
    lanewiseShift(VectorOperators.Binary op, int e) {
        return (ByteMaxVector) super.lanewiseShiftTemplate(op, e);  // specialize
    }

    /*package-private*/
    @Override
    @ForceInline
    public final
    ByteMaxVector
    lanewise(VectorOperators.Ternary op, Vector<Byte> v1, Vector<Byte> v2) {
        return (ByteMaxVector) super.lanewiseTemplate(op, v1, v2);  // specialize
    }

    @Override
    @ForceInline
    public final
    ByteMaxVector addIndex(int scale) {
        return (ByteMaxVector) super.addIndexTemplate(scale);  // specialize
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
    public final ByteMaxMask test(Test op) {
        return super.testTemplate(ByteMaxMask.class, op);  // specialize
    }

    // Specialized comparisons

    @Override
    @ForceInline
    public final ByteMaxMask compare(Comparison op, Vector<Byte> v) {
        return super.compareTemplate(ByteMaxMask.class, op, v);  // specialize
    }

    @Override
    @ForceInline
    public final ByteMaxMask compare(Comparison op, byte s) {
        return super.compareTemplate(ByteMaxMask.class, op, s);  // specialize
    }

    @Override
    @ForceInline
    public final ByteMaxMask compare(Comparison op, long s) {
        return super.compareTemplate(ByteMaxMask.class, op, s);  // specialize
    }

    @Override
    @ForceInline
    public ByteMaxVector blend(Vector<Byte> v, VectorMask<Byte> m) {
        return (ByteMaxVector)
            super.blendTemplate(ByteMaxMask.class,
                                (ByteMaxVector) v,
                                (ByteMaxMask) m);  // specialize
    }

    @Override
    @ForceInline
    public ByteMaxVector slice(int origin, Vector<Byte> v) {
        return (ByteMaxVector) super.sliceTemplate(origin, v);  // specialize
    }

    @Override
    @ForceInline
    public ByteMaxVector slice(int origin) {
       if ((origin < 0) || (origin >= VLENGTH)) {
         throw new ArrayIndexOutOfBoundsException("Index " + origin + " out of bounds for vector length " + VLENGTH);
       } else {
         ByteMaxShuffle Iota = (ByteMaxShuffle)VectorShuffle.iota(VSPECIES, 0, 1, true);
         VectorMask<Byte> BlendMask = Iota.toVector().compare(VectorOperators.LT, (broadcast((byte)(VLENGTH-origin))));
         Iota = (ByteMaxShuffle)VectorShuffle.iota(VSPECIES, origin, 1, true);
         return ZERO.blend(this.rearrange(Iota), BlendMask);
       }
    }

    @Override
    @ForceInline
    public ByteMaxVector unslice(int origin, Vector<Byte> w, int part) {
        return (ByteMaxVector) super.unsliceTemplate(origin, w, part);  // specialize
    }

    @Override
    @ForceInline
    public ByteMaxVector unslice(int origin, Vector<Byte> w, int part, VectorMask<Byte> m) {
        return (ByteMaxVector)
            super.unsliceTemplate(ByteMaxMask.class,
                                  origin, w, part,
                                  (ByteMaxMask) m);  // specialize
    }

    @Override
    @ForceInline
    public ByteMaxVector unslice(int origin) {
       if ((origin < 0) || (origin >= VLENGTH)) {
         throw new ArrayIndexOutOfBoundsException("Index " + origin + " out of bounds for vector length " + VLENGTH);
       } else {
         ByteMaxShuffle Iota = (ByteMaxShuffle)VectorShuffle.iota(VSPECIES, 0, 1, true);
         VectorMask<Byte> BlendMask = Iota.toVector().compare(VectorOperators.GE, (broadcast((byte)(origin))));
         Iota = (ByteMaxShuffle)VectorShuffle.iota(VSPECIES, -origin, 1, true);
         return ZERO.blend(this.rearrange(Iota), BlendMask);
       }
    }

    @Override
    @ForceInline
    public ByteMaxVector rearrange(VectorShuffle<Byte> s) {
        return (ByteMaxVector)
            super.rearrangeTemplate(ByteMaxShuffle.class,
                                    (ByteMaxShuffle) s);  // specialize
    }

    @Override
    @ForceInline
    public ByteMaxVector rearrange(VectorShuffle<Byte> shuffle,
                                  VectorMask<Byte> m) {
        return (ByteMaxVector)
            super.rearrangeTemplate(ByteMaxShuffle.class,
                                    (ByteMaxShuffle) shuffle,
                                    (ByteMaxMask) m);  // specialize
    }

    @Override
    @ForceInline
    public ByteMaxVector rearrange(VectorShuffle<Byte> s,
                                  Vector<Byte> v) {
        return (ByteMaxVector)
            super.rearrangeTemplate(ByteMaxShuffle.class,
                                    (ByteMaxShuffle) s,
                                    (ByteMaxVector) v);  // specialize
    }

    @Override
    @ForceInline
    public ByteMaxVector selectFrom(Vector<Byte> v) {
        return (ByteMaxVector)
            super.selectFromTemplate((ByteMaxVector) v);  // specialize
    }

    @Override
    @ForceInline
    public ByteMaxVector selectFrom(Vector<Byte> v,
                                   VectorMask<Byte> m) {
        return (ByteMaxVector)
            super.selectFromTemplate((ByteMaxVector) v,
                                     (ByteMaxMask) m);  // specialize
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
    public ByteMaxVector withLane(int i, byte e) {
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

    static final class ByteMaxMask extends AbstractMask<Byte> {

        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public ByteMaxMask(boolean[] bits) {
            this(bits, 0);
        }

        public ByteMaxMask(boolean[] bits, int offset) {
            boolean[] a = new boolean[vspecies().laneCount()];
            for (int i = 0; i < a.length; i++) {
                a[i] = bits[offset + i];
            }
            this.bits = a;
        }

        public ByteMaxMask(boolean val) {
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
        ByteMaxMask uOp(MUnOp f) {
            boolean[] res = new boolean[vspecies().laneCount()];
            boolean[] bits = getBits();
            for (int i = 0; i < res.length; i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new ByteMaxMask(res);
        }

        @Override
        ByteMaxMask bOp(VectorMask<Byte> m, MBinOp f) {
            boolean[] res = new boolean[vspecies().laneCount()];
            boolean[] bits = getBits();
            boolean[] mbits = ((ByteMaxMask)m).getBits();
            for (int i = 0; i < res.length; i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new ByteMaxMask(res);
        }

        @ForceInline
        @Override
        public final
        ByteMaxVector toVector() {
            return (ByteMaxVector) super.toVectorTemplate();  // specialize
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
        public ByteMaxMask not() {
            return (ByteMaxMask) VectorIntrinsics.unaryOp(
                                             VECTOR_OP_NOT, ByteMaxMask.class, byte.class, VLENGTH,
                                             this,
                                             (m1) -> m1.uOp((i, a) -> !a));
        }

        // Binary operations

        @Override
        @ForceInline
        public ByteMaxMask and(VectorMask<Byte> mask) {
            Objects.requireNonNull(mask);
            ByteMaxMask m = (ByteMaxMask)mask;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, ByteMaxMask.class, byte.class, VLENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public ByteMaxMask or(VectorMask<Byte> mask) {
            Objects.requireNonNull(mask);
            ByteMaxMask m = (ByteMaxMask)mask;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, ByteMaxMask.class, byte.class, VLENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(BT_ne, ByteMaxMask.class, byte.class, VLENGTH,
                                         this, this,
                                         (m, __) -> anyTrueHelper(((ByteMaxMask)m).getBits()));
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(BT_overflow, ByteMaxMask.class, byte.class, VLENGTH,
                                         this, vspecies().maskAll(true),
                                         (m, __) -> allTrueHelper(((ByteMaxMask)m).getBits()));
        }

        /*package-private*/
        static ByteMaxMask maskAll(boolean bit) {
            return bit ? TRUE_MASK : FALSE_MASK;
        }
        static final ByteMaxMask TRUE_MASK = new ByteMaxMask(true);
        static final ByteMaxMask FALSE_MASK = new ByteMaxMask(false);
    }

    // Shuffle

    static final class ByteMaxShuffle extends AbstractShuffle<Byte> {
        ByteMaxShuffle(byte[] reorder) {
            super(reorder);
        }

        public ByteMaxShuffle(int[] reorder) {
            super(reorder);
        }

        public ByteMaxShuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        public ByteMaxShuffle(IntUnaryOperator fn) {
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
        static final ByteMaxShuffle IOTA = new ByteMaxShuffle(IDENTITY);

        @Override
        @ForceInline
        public ByteMaxVector toVector() {
            return VectorIntrinsics.shuffleToVector(VCLASS, ETYPE, ByteMaxShuffle.class, this, VLENGTH,
                                                    (s) -> ((ByteMaxVector)(((AbstractShuffle<Byte>)(s)).toVectorTemplate())));
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
        public ByteMaxShuffle rearrange(VectorShuffle<Byte> shuffle) {
            ByteMaxShuffle s = (ByteMaxShuffle) shuffle;
            byte[] r = new byte[reorder.length];
            for (int i = 0; i < reorder.length; i++) {
                int ssi = s.reorder[i];
                r[i] = this.reorder[ssi];  // throws on exceptional index
            }
            return new ByteMaxShuffle(r);
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
