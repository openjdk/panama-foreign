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
final class ShortMaxVector extends ShortVector {
    static final ShortSpecies VSPECIES =
        (ShortSpecies) ShortVector.SPECIES_MAX;

    static final VectorShape VSHAPE =
        VSPECIES.vectorShape();

    static final Class<ShortMaxVector> VCLASS = ShortMaxVector.class;

    static final int VSIZE = VSPECIES.vectorBitSize();

    static final int VLENGTH = VSPECIES.laneCount();

    static final Class<Short> ETYPE = short.class;

    // The JVM expects to find the state here.
    private final short[] vec; // Don't access directly, use getElements() instead.

    ShortMaxVector(short[] v) {
        vec = v;
    }

    // For compatibility as ShortMaxVector::new,
    // stored into species.vectorFactory.
    ShortMaxVector(Object v) {
        this((short[]) v);
    }

    static final ShortMaxVector ZERO = new ShortMaxVector(new short[VLENGTH]);
    static final ShortMaxVector IOTA = new ShortMaxVector(VSPECIES.iotaArray());

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
    public final ShortMaxVector broadcast(short e) {
        return (ShortMaxVector) super.broadcastTemplate(e);  // specialize
    }

    @Override
    @ForceInline
    public final ShortMaxVector broadcast(long e) {
        return (ShortMaxVector) super.broadcastTemplate(e);  // specialize
    }

    @Override
    @ForceInline
    ShortMaxMask maskFromArray(boolean[] bits) {
        return new ShortMaxMask(bits);
    }

    @Override
    @ForceInline
    ShortMaxShuffle iotaShuffle() { return ShortMaxShuffle.IOTA; }

    @ForceInline
    ShortMaxShuffle iotaShuffle(int start) { 
        return (ShortMaxShuffle)VectorIntrinsics.shuffleIota(ETYPE, ShortMaxShuffle.class, VSPECIES, VLENGTH, start, (val, l) -> new ShortMaxShuffle(i -> (VectorIntrinsics.wrapToRange(i + val, l))));
    }

    @Override
    @ForceInline
    ShortMaxShuffle shuffleFromBytes(byte[] reorder) { return new ShortMaxShuffle(reorder); }

    @Override
    @ForceInline
    ShortMaxShuffle shuffleFromArray(int[] indexes, int i) { return new ShortMaxShuffle(indexes, i); }

    @Override
    @ForceInline
    ShortMaxShuffle shuffleFromOp(IntUnaryOperator fn) { return new ShortMaxShuffle(fn); }

    // Make a vector of the same species but the given elements:
    @ForceInline
    final @Override
    ShortMaxVector vectorFactory(short[] vec) {
        return new ShortMaxVector(vec);
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
    ShortMaxVector uOp(FUnOp f) {
        return (ShortMaxVector) super.uOpTemplate(f);  // specialize
    }

    @ForceInline
    final @Override
    ShortMaxVector uOp(VectorMask<Short> m, FUnOp f) {
        return (ShortMaxVector)
            super.uOpTemplate((ShortMaxMask)m, f);  // specialize
    }

    // Binary operator

    @ForceInline
    final @Override
    ShortMaxVector bOp(Vector<Short> v, FBinOp f) {
        return (ShortMaxVector) super.bOpTemplate((ShortMaxVector)v, f);  // specialize
    }

    @ForceInline
    final @Override
    ShortMaxVector bOp(Vector<Short> v,
                     VectorMask<Short> m, FBinOp f) {
        return (ShortMaxVector)
            super.bOpTemplate((ShortMaxVector)v, (ShortMaxMask)m,
                              f);  // specialize
    }

    // Ternary operator

    @ForceInline
    final @Override
    ShortMaxVector tOp(Vector<Short> v1, Vector<Short> v2, FTriOp f) {
        return (ShortMaxVector)
            super.tOpTemplate((ShortMaxVector)v1, (ShortMaxVector)v2,
                              f);  // specialize
    }

    @ForceInline
    final @Override
    ShortMaxVector tOp(Vector<Short> v1, Vector<Short> v2,
                     VectorMask<Short> m, FTriOp f) {
        return (ShortMaxVector)
            super.tOpTemplate((ShortMaxVector)v1, (ShortMaxVector)v2,
                              (ShortMaxMask)m, f);  // specialize
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
    public ShortMaxVector lanewise(Unary op) {
        return (ShortMaxVector) super.lanewiseTemplate(op);  // specialize
    }

    @Override
    @ForceInline
    public ShortMaxVector lanewise(Binary op, Vector<Short> v) {
        return (ShortMaxVector) super.lanewiseTemplate(op, v);  // specialize
    }

    /*package-private*/
    @Override
    @ForceInline ShortMaxVector
    lanewiseShift(VectorOperators.Binary op, int e) {
        return (ShortMaxVector) super.lanewiseShiftTemplate(op, e);  // specialize
    }

    /*package-private*/
    @Override
    @ForceInline
    public final
    ShortMaxVector
    lanewise(VectorOperators.Ternary op, Vector<Short> v1, Vector<Short> v2) {
        return (ShortMaxVector) super.lanewiseTemplate(op, v1, v2);  // specialize
    }

    @Override
    @ForceInline
    public final
    ShortMaxVector addIndex(int scale) {
        return (ShortMaxVector) super.addIndexTemplate(scale);  // specialize
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
    public final ShortMaxMask test(Test op) {
        return super.testTemplate(ShortMaxMask.class, op);  // specialize
    }

    // Specialized comparisons

    @Override
    @ForceInline
    public final ShortMaxMask compare(Comparison op, Vector<Short> v) {
        return super.compareTemplate(ShortMaxMask.class, op, v);  // specialize
    }

    @Override
    @ForceInline
    public final ShortMaxMask compare(Comparison op, short s) {
        return super.compareTemplate(ShortMaxMask.class, op, s);  // specialize
    }

    @Override
    @ForceInline
    public final ShortMaxMask compare(Comparison op, long s) {
        return super.compareTemplate(ShortMaxMask.class, op, s);  // specialize
    }

    @Override
    @ForceInline
    public ShortMaxVector blend(Vector<Short> v, VectorMask<Short> m) {
        return (ShortMaxVector)
            super.blendTemplate(ShortMaxMask.class,
                                (ShortMaxVector) v,
                                (ShortMaxMask) m);  // specialize
    }

    @Override
    @ForceInline
    public ShortMaxVector slice(int origin, Vector<Short> v) {
        return (ShortMaxVector) super.sliceTemplate(origin, v);  // specialize
    }

    @Override
    @ForceInline
    public ShortMaxVector slice(int origin) {
       if ((origin < 0) || (origin >= VLENGTH)) {
         throw new ArrayIndexOutOfBoundsException("Index " + origin + " out of bounds for vector length " + VLENGTH);
       } else {
         ShortMaxShuffle Iota = (ShortMaxShuffle)VectorShuffle.iota(VSPECIES, 0, 1, true);
         VectorMask<Short> BlendMask = Iota.toVector().compare(VectorOperators.LT, (broadcast((short)(VLENGTH-origin))));
         Iota = (ShortMaxShuffle)VectorShuffle.iota(VSPECIES, origin, 1, true);
         return ZERO.blend(this.rearrange(Iota), BlendMask);
       }
    }

    @Override
    @ForceInline
    public ShortMaxVector unslice(int origin, Vector<Short> w, int part) {
        return (ShortMaxVector) super.unsliceTemplate(origin, w, part);  // specialize
    }

    @Override
    @ForceInline
    public ShortMaxVector unslice(int origin, Vector<Short> w, int part, VectorMask<Short> m) {
        return (ShortMaxVector)
            super.unsliceTemplate(ShortMaxMask.class,
                                  origin, w, part,
                                  (ShortMaxMask) m);  // specialize
    }

    @Override
    @ForceInline
    public ShortMaxVector unslice(int origin) {
       if ((origin < 0) || (origin >= VLENGTH)) {
         throw new ArrayIndexOutOfBoundsException("Index " + origin + " out of bounds for vector length " + VLENGTH);
       } else {
         ShortMaxShuffle Iota = (ShortMaxShuffle)VectorShuffle.iota(VSPECIES, 0, 1, true);
         VectorMask<Short> BlendMask = Iota.toVector().compare(VectorOperators.GE, (broadcast((short)(origin))));
         Iota = (ShortMaxShuffle)VectorShuffle.iota(VSPECIES, -origin, 1, true);
         return ZERO.blend(this.rearrange(Iota), BlendMask);
       }
    }

    @Override
    @ForceInline
    public ShortMaxVector rearrange(VectorShuffle<Short> s) {
        return (ShortMaxVector)
            super.rearrangeTemplate(ShortMaxShuffle.class,
                                    (ShortMaxShuffle) s);  // specialize
    }

    @Override
    @ForceInline
    public ShortMaxVector rearrange(VectorShuffle<Short> shuffle,
                                  VectorMask<Short> m) {
        return (ShortMaxVector)
            super.rearrangeTemplate(ShortMaxShuffle.class,
                                    (ShortMaxShuffle) shuffle,
                                    (ShortMaxMask) m);  // specialize
    }

    @Override
    @ForceInline
    public ShortMaxVector rearrange(VectorShuffle<Short> s,
                                  Vector<Short> v) {
        return (ShortMaxVector)
            super.rearrangeTemplate(ShortMaxShuffle.class,
                                    (ShortMaxShuffle) s,
                                    (ShortMaxVector) v);  // specialize
    }

    @Override
    @ForceInline
    public ShortMaxVector selectFrom(Vector<Short> v) {
        return (ShortMaxVector)
            super.selectFromTemplate((ShortMaxVector) v);  // specialize
    }

    @Override
    @ForceInline
    public ShortMaxVector selectFrom(Vector<Short> v,
                                   VectorMask<Short> m) {
        return (ShortMaxVector)
            super.selectFromTemplate((ShortMaxVector) v,
                                     (ShortMaxMask) m);  // specialize
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
    public ShortMaxVector withLane(int i, short e) {
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

    static final class ShortMaxMask extends AbstractMask<Short> {

        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public ShortMaxMask(boolean[] bits) {
            this(bits, 0);
        }

        public ShortMaxMask(boolean[] bits, int offset) {
            boolean[] a = new boolean[vspecies().laneCount()];
            for (int i = 0; i < a.length; i++) {
                a[i] = bits[offset + i];
            }
            this.bits = a;
        }

        public ShortMaxMask(boolean val) {
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
        ShortMaxMask uOp(MUnOp f) {
            boolean[] res = new boolean[vspecies().laneCount()];
            boolean[] bits = getBits();
            for (int i = 0; i < res.length; i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new ShortMaxMask(res);
        }

        @Override
        ShortMaxMask bOp(VectorMask<Short> m, MBinOp f) {
            boolean[] res = new boolean[vspecies().laneCount()];
            boolean[] bits = getBits();
            boolean[] mbits = ((ShortMaxMask)m).getBits();
            for (int i = 0; i < res.length; i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new ShortMaxMask(res);
        }

        @ForceInline
        @Override
        public final
        ShortMaxVector toVector() {
            return (ShortMaxVector) super.toVectorTemplate();  // specialize
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
        public ShortMaxMask not() {
            return (ShortMaxMask) VectorIntrinsics.unaryOp(
                                             VECTOR_OP_NOT, ShortMaxMask.class, short.class, VLENGTH,
                                             this,
                                             (m1) -> m1.uOp((i, a) -> !a));
        }

        // Binary operations

        @Override
        @ForceInline
        public ShortMaxMask and(VectorMask<Short> mask) {
            Objects.requireNonNull(mask);
            ShortMaxMask m = (ShortMaxMask)mask;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, ShortMaxMask.class, short.class, VLENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public ShortMaxMask or(VectorMask<Short> mask) {
            Objects.requireNonNull(mask);
            ShortMaxMask m = (ShortMaxMask)mask;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, ShortMaxMask.class, short.class, VLENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(BT_ne, ShortMaxMask.class, short.class, VLENGTH,
                                         this, this,
                                         (m, __) -> anyTrueHelper(((ShortMaxMask)m).getBits()));
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(BT_overflow, ShortMaxMask.class, short.class, VLENGTH,
                                         this, vspecies().maskAll(true),
                                         (m, __) -> allTrueHelper(((ShortMaxMask)m).getBits()));
        }

        /*package-private*/
        static ShortMaxMask maskAll(boolean bit) {
            return bit ? TRUE_MASK : FALSE_MASK;
        }
        static final ShortMaxMask TRUE_MASK = new ShortMaxMask(true);
        static final ShortMaxMask FALSE_MASK = new ShortMaxMask(false);
    }

    // Shuffle

    static final class ShortMaxShuffle extends AbstractShuffle<Short> {
        ShortMaxShuffle(byte[] reorder) {
            super(reorder);
        }

        public ShortMaxShuffle(int[] reorder) {
            super(reorder);
        }

        public ShortMaxShuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        public ShortMaxShuffle(IntUnaryOperator fn) {
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
        static final ShortMaxShuffle IOTA = new ShortMaxShuffle(IDENTITY);

        @Override
        @ForceInline
        public ShortMaxVector toVector() {
            return VectorIntrinsics.shuffleToVector(VCLASS, ETYPE, ShortMaxShuffle.class, this, VLENGTH,
                                                    (s) -> ((ShortMaxVector)(((AbstractShuffle<Short>)(s)).toVectorTemplate())));
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
        public ShortMaxShuffle rearrange(VectorShuffle<Short> shuffle) {
            ShortMaxShuffle s = (ShortMaxShuffle) shuffle;
            byte[] r = new byte[reorder.length];
            for (int i = 0; i < reorder.length; i++) {
                int ssi = s.reorder[i];
                r[i] = this.reorder[ssi];  // throws on exceptional index
            }
            return new ShortMaxShuffle(r);
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
