/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.vector;

import jdk.internal.vm.annotation.ForceInline;

import static com.oracle.vector.PatchableVecUtils.*;

import java.util.BitSet;
import java.util.function.*;

class Int128Vector extends IntVector<Shapes.S128Bit> {
    private static final SpeciesInt128 SPECIES = new SpeciesInt128();

    private final Long2 vec;

    private static final Long2 neg_ones = Long2.make(pack(-1, -1), pack(-1, -1));

    private Int128Vector(int[] ary) {
        this.vec = PatchableVecUtils.long2FromIntArray(ary, 0);
    }

    public Int128Vector(Long2 v) {
        this.vec = v;
    }

    @Override
    public Species<Integer, Shapes.S128Bit> species() {
        return SPECIES;
    }

    @Override
    public Long2 toLong2() {
        return this.vec;
    }

    @Override
    public Long4 toLong4() {
        return Long4.make(this.vec.extract(0), this.vec.extract(1), 0, 0);
    }

    @Override
    public Long8 toLong8() {
        return Long8.make(this.vec.extract(0), this.vec.extract(1), 0, 0, 0, 0, 0, 0);
    }

    @Override
    public Integer getElement(int index) {
        return PatchableVecUtils.vpextrd(index, this.vec);
    }

    @Override
    public Vector<Integer, Shapes.S128Bit> putElement(int index, Integer x) {
        return new Int128Vector(PatchableVecUtils.vpinsrd(this.vec, x, index));
    }

//    @Override
    public void intoArray(byte[] a, int o) {
        PatchableVecUtils.long2ToByteArray(a,o,this.vec);
    }

    @Override
    public Vector<Integer, Shapes.S128Bit> compareEqual(Vector<Integer, Shapes.S128Bit> v) {
        Int128Vector v2 = (Int128Vector) v;
        return new Int128Vector(vpcmpeqd(this.vec, v2.toLong2()));
    }

    @Override
    public Vector<Integer, Shapes.S128Bit> lessThan(Vector<Integer, Shapes.S128Bit> v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Vector<Integer, Shapes.S128Bit> greaterThan(Vector<Integer, Shapes.S128Bit> v) {
        Int128Vector v2 = (Int128Vector) v;
        return new Int128Vector(PatchableVecUtils.vpcmpgtd(this.vec, v2.toLong2()));
    }

    @Override
    public Vector<Integer, Shapes.S128Bit> map(UnaryOperator<Integer> op) {
        int[] res = new int[this.length()];
        for (int i = 0; i < this.length(); i++) {
            res[i] = op.apply(this.getElement(i));
        }
        return new Int128Vector(res);
    }

    @Override
    public Mask<Shapes.S128Bit> test(Predicate<Integer> op) {
        boolean[] msk = new boolean[this.length()];
        for (int i = 0; i < this.length(); i++) {
            msk[i] = op.test(this.getElement(i));
        }
        return new Mask64Bit(msk);
    }

    @Override
    public Vector<Integer, Shapes.S128Bit> mapWhere(Mask<Shapes.S128Bit> mask, UnaryOperator<Integer> op) {
        int[] ary = new int[this.length()];
        for (int i = 0; i < ary.length; i++) {
            ary[i] = mask.getElement(i) ? op.apply(ary[i]) : this.getElement(i);
        }
        return new Int128Vector(ary);
    }

    @Override
    public Vector<Integer, Shapes.S128Bit> map(BinaryOperator<Integer> op, Vector<Integer, Shapes.S128Bit> this2) {
        Int128Vector this2c = (Int128Vector) this2;
        int[] ary = new int[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = op.apply(this.getElement(i), this2c.getElement(i));
        }
        return new Int128Vector(ary);
    }

    @Override
    public Vector<Integer, Shapes.S128Bit> mapWhere(Mask<Shapes.S128Bit> mask, BinaryOperator<Integer> op, Vector<Integer, Shapes.S128Bit> this2) {
        Int128Vector this2c = (Int128Vector) this2;
        int[] ary = new int[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = mask.getElement(i) ? op.apply(this.getElement(i), this2c.getElement(i)) : this.getElement(i);
        }
        return new Int128Vector(ary);
    }

    @Override
    public boolean isZeros() {
        return this.vec.equals(Long2.ZERO);
    }

    @Override
    @ForceInline
    public Vector<Integer, Shapes.S128Bit> neg() {
        return new Int128Vector(PatchableVecUtils.vpsignd(this.vec, neg_ones));
    }

    @Override
    @ForceInline
    public Vector<Integer, Shapes.S128Bit> add(Vector<Integer, Shapes.S128Bit> this2) {
        Int128Vector this2c = (Int128Vector) this2;
        return new Int128Vector(PatchableVecUtils.vpaddd(this.vec, this2c.toLong2()));
    }

    @Override
    public <T> T reduce(Function<Integer, T> mapper, BinaryOperator<T> op) {
        Integer[] ary = new Integer[this.length()];
        this.intoArray(ary, 0);
        T redval = mapper.apply(ary[0]);
        for (int i = 1; i < ary.length; i++) {
            redval = op.apply(redval, mapper.apply(ary[i]));
        }
        return redval;
    }

    @Override
    public Mask<Shapes.S128Bit> toMask() {
        boolean[] mask = new boolean[this.length()];
        for (int i = 0; i < this.length(); i++) {
            if ((this.getElement(i) & 0x80000000) == 0) {
                mask[i] = true;
            }
        }
        return new Mask64Bit(mask);
    }

    @Override
    public Integer sumAll() {
        Integer sum = 0;
        for (int i = 0; i < this.length(); i++) {
            sum += this.getElement(i);
        }
        return sum;
    }

    @Override
    public Vector<Integer, Shapes.S128Bit> blend(Vector<Integer, Shapes.S128Bit> b, Vector<Integer, Shapes.S128Bit> mask) {
        Int128Vector bc = (Int128Vector) b;
        int[] ary = new int[this.length()];
        for (int i = 0; i < ary.length; i++) {
            ary[i] = mask.getElement(i) == 0 ? this.getElement(i) : bc.getElement(i);
        }
        return new Int128Vector(ary);
    }

    //   @Override
    public Vector<Integer, Shapes.S128Bit> sub(Vector<Integer, Shapes.S128Bit> this2) {
        Long2 res = vpsubd(this.vec, this2.toLong2());
        return new Int128Vector(res);
    }

    @Override
    public Vector<Integer, Shapes.S128Bit> mul(Vector<Integer, Shapes.S128Bit> this2) {
        Long2 res = vpmulld(this.vec, this2.toLong2());
        return new Int128Vector(res);
    }

//    @Override
    public Vector<Integer, Shapes.S128Bit> and(Vector<Integer, Shapes.S128Bit> this2) {
        Long2 res = PatchableVecUtils.vpand(this.vec, this2.toLong2());
        return new Int128Vector(res);
    }

    //  @Override
    public Vector<Integer, Shapes.S128Bit> or(Vector<Integer, Shapes.S128Bit> this2) {
        Long2 res = PatchableVecUtils.vpor(this.vec, this2.toLong2());
        return new Int128Vector(res);
    }

    @Override
    public Vector<Integer, Shapes.S128Bit> xor(Vector<Integer, Shapes.S128Bit> this2) {
        Long2 res = PatchableVecUtils.vpxor(this.vec, this2.toLong2());
        return new Int128Vector(res);
    }

//    @Override
    public Vector<Integer, Shapes.S128Bit> sqrt() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Vector<Integer, Shapes.S128Bit> shuffle(Shuffle<Shapes.S128Bit> perm) {
        //TODO: This is incorrect.  pshufd for 32-bit words is directed by imm8, not x/y/zmm register
        return new Int128Vector(PatchableVecUtils.vpshufd(this.vec, perm.toArray()[0]));

    }

    @Override
    public Shuffle<Shapes.S128Bit> toShuffle() {
        return new ShuffleIntLong2(this.vec);
    }

//    @Override
    public Vector<Float, Shapes.S128Bit> toFloatVector() {
        return new Float128Vector(PatchableVecUtils.vcvtdq2ps(this.vec));
    }

//    @Override
    public Vector<Integer, Shapes.S128Bit> toIntVector() {
        return this;
    }

//    @Override
    public Vector<Double, Shapes.S128Bit> toDoubleVector() {
        throw new UnsupportedOperationException("Conversions to Double Unsupported");
    }

//    @Override
    public Vector<Float, Shapes.S128Bit> toFloatVectorBits() {
        return new Float128Vector(this.vec);
    }

//    @Override
    public Vector<Integer, Shapes.S128Bit> toIntVectorBits() {
        return this;
    }                                                                                                            

//    @Override
    public Vector<Double, Shapes.S128Bit> toDoubleVectorBits() {
        return new Double128Vector(this.vec);
    }

    @Override
    public Vector<Integer, Shapes.S128Bit> fromArray(int[] ary, int offset) {
        Long2 v = PatchableVecUtils.long2FromIntArray(ary, offset);
        return new Int128Vector(v);
    }

    @Override
    public void intoArray(int[] ary, int offset) {
        PatchableVecUtils.long2ToIntArray(ary, offset, this.vec);
    }

    private static final class SpeciesInt128 implements Species<Integer, Shapes.S128Bit> {
        static final Shapes.S128Bit shape = Shapes.SHAPE_128_BIT;

        @Override
        public Class<Integer> elementType() {
            return Integer.class;
        }

        @Override
        public int elementSize() {
            return Integer.SIZE;
        }

        @Override
        public Shapes.S128Bit shape() {
            return shape;
        }

        @Override
        public Function<Integer, Vector<Integer, Shapes.S128Bit>> fromElementFactory() {
            return (Integer i) -> new Int128Vector(PatchableVecUtils.broadcastIntL2(i));
        }

        @Override
        public Vector<Integer, Shapes.S128Bit> generate(IntFunction<? extends Integer> generator) {
            int[] arr = new int[this.length()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = generator.apply(i);
            }
            return new Int128Vector(arr);
        }

        @Override
        public BiFunction<Integer[], Integer, Vector<Integer, Shapes.S128Bit>> fromArrayFactory() {
            return (Integer[] ary, Integer off) -> {
                if (off >= ary.length) throw new ArrayIndexOutOfBoundsException();
                int[] v = new int[this.length()];
                int l = Math.min(v.length, ary.length - off);
                for (int i = 0; i < l; i++) {
                    v[i] = ary[off + i];
                }
                return new Int128Vector(v);

            };
        }

//        @Override
        public BiFunction<int[], Integer, Vector<Integer, Shapes.S128Bit>> fromIntArrayFactory() {
            return (int[] ints, Integer offset) -> new Int128Vector(PatchableVecUtils.long2FromIntArray(ints,offset));
        }

//        @Override
        public BiFunction<float[], Integer, Vector<Integer, Shapes.S128Bit>> fromFloatArrayFactory() {
            return (float[] floats, Integer offset) -> new Int128Vector(PatchableVecUtils.long2FromFloatArray(floats,offset));
        }

//        @Override
        public BiFunction<double[], Integer, Vector<Integer, Shapes.S128Bit>> fromDoubleArrayFactory() {
            return (double[] doubles, Integer offset) -> new Int128Vector(PatchableVecUtils.long2FromDoubleArray(doubles,offset));
        }

//        @Override
        public BiFunction<long[], Integer, Vector<Integer, Shapes.S128Bit>> fromLongArrayFactory() {
            return (long[] longs, Integer offset) -> new Int128Vector(PatchableVecUtils.long2FromLongArray(longs,offset));
        }

        @Override
        public Shuffle<Shapes.S128Bit> iota() {
            int[] ary = new int[this.length()];
            for (int i = 0; i < length(); i++) {
                ary[i] = i;
            }
            return new ShuffleIntLong2(ary);
        }

        @Override
        public Shuffle<Shapes.S128Bit> iota(int base, int step, int modulus) {
            int[] ary = new int[this.length()];
            for (int i = 0; i < this.length(); i++) {
                ary[i] = (base + i * step) % modulus;
            }
            return new ShuffleIntLong2(ary);
        }
    }

    private static class Mask64Bit implements Mask<Shapes.S128Bit> {
        private boolean[] bits = new boolean[this.length()];


        Mask64Bit(boolean[] bits) {
            this.bits = bits;
        }

        @Override
        public int length() {
            return 4;
        }

        @Override
        public long toLong() {
            long res = 0;
            for (int i = 0; i < this.length(); i++) {
                res = (res << 1);
                if (bits[i]) {
                    res |= 1;
                }
            }
            return res;
        }

        @Override
        public BitSet toBitSet() {
            BitSet bs = new BitSet(this.length());
            for (int i = 0; i < this.length(); i++) {
                bs.set(i, bits[i]);
            }

            return bs;
        }

        @Override
        public boolean[] toArray() {
            return bits;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <E> Vector<E, Shapes.S128Bit> toVector(Class<E> type) {
            if (type == Integer.class) {
                Vector<Integer, Shapes.S128Bit> v = new Int128Vector(Long2.ZERO);
                for (int i = 0; i < this.length(); i++) {
                    if (bits[i]) {
                        v = v.putElement(i, 0xFFFFFFFF);
                    }
                }
                return (Vector<E, Shapes.S128Bit>) v;

            }
            throw new UnsupportedOperationException();
        }
    }

    private static class ShuffleIntLong2 implements Shuffle<Shapes.S128Bit> {

        final Long2 v;

        ShuffleIntLong2(int[] ary) {
            v = Long2.make(pack(ary[0], ary[1]), pack(ary[2], ary[3]));
        }

        ShuffleIntLong2(Long2 v) {
            this.v = v;
        }

        @Override
        public Long2 toLong2() {
            return v;
        }

        @Override
        public Long4 toLong4() {
            return Long4.make(v.extract(0), v.extract(1), 0, 0);
        }

        @Override
        public Long8 toLong8() {
            return Long8.make(v.extract(0), v.extract(1), 0, 0, 0, 0, 0, 0);
        }

        @Override
        public int length() {
            return 4;
        }

        @Override
        public int[] toArray() {
            int[] ary = new int[4];
            for (int i = 0; i < ary.length; i++) {
                ary[i] = getInt(v, i);
            }
            return ary;
        }

        @Override
        public Vector<Integer, Shapes.S128Bit> toVector() {
            throw new UnsupportedOperationException();
        }

        @Override
        public <E> Vector<E, Shapes.S128Bit> toVector(Class<E> type) {
            throw new UnsupportedOperationException();
        }
    }
}
