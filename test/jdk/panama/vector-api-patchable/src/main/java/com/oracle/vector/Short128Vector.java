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

import java.util.BitSet;
import java.util.function.*;
import java.util.stream.IntStream;

import jdk.internal.vm.annotation.ForceInline;
import jdk.internal.vm.annotation.Stable;

class Short128Vector extends ShortVector<Shapes.S128Bit> {

    private @Stable final Long2 vec;

    private static final Short128Vector STUB = new Short128Vector(Long2.ZERO);
    private static final Short128VectorSpecies SPECIES = new Short128VectorSpecies();

    //Constants for zero comparisons
    private static final short[] pzArray = {0, 0, 0, 0, 0, 0, 0, 0};
    private static final Short128Vector POSZERO = (Short128Vector) STUB.fromArray(pzArray, 0);

    Short128Vector(Long2 v) {
        this.vec = v;
    }

    private Short128Vector(short[] ary) {
        this.vec = PatchableVecUtils.long2FromShortArray(ary, 0);
    }

    @Override
    public Vector<Short, Shapes.S128Bit> fromArray(short[] ary, int offset) {
        return new Short128Vector(PatchableVecUtils.long2FromShortArray(ary, offset));
    }

    @Override
    public void intoArray(short[] ary, int offset) {
        PatchableVecUtils.long2ToShortArray(ary, offset, this.vec);
    }

    @Override
    public Long2 toLong2() {
        return this.vec;
    }

    @Override
    public Long4 toLong4() {
        return Long4.make(this.vec.extract(0), this.vec.extract(1),
                0L, 0L);
    }

    @Override
    public Long8 toLong8() {
        return Long8.make(this.vec.extract(0), this.vec.extract(1),
                0L, 0L, 0L, 0L, 0L, 0L);
    }

    @Override
    public Species<Short, Shapes.S128Bit> species() {
        return SPECIES;
    }

    @Override
    public Short getElement(int index) {
        if (index >= this.length()) throw new IllegalArgumentException("getElement index must be 0-7 inclusive");
        return (Short)PatchableVecUtils.vpextrw(index, this.vec);
    }

    @Override
    public Vector<Short, Shapes.S128Bit> putElement(int index, Short x) {
        if (index >= this.length()) throw new IllegalArgumentException("putElement index must be 0-7 inclusive");
        return new Short128Vector(PatchableVecUtils.vpinsrw(this.vec, x, index));
    }

//    @Override
    public void intoArray(byte[] a, int o) {
        PatchableVecUtils.long2ToByteArray(a,o,this.vec);
    }

    @Override
    public Vector<Integer, Shapes.S128Bit> compareEqual(Vector<Short, Shapes.S128Bit> v) {
        Short128Vector vc = (Short128Vector) v;
        return new Int128Vector(PatchableVecUtils.vpcmpeqw(this.vec, vc.toLong2()));
    }

    @Override
    public Vector<Integer, Shapes.S128Bit> lessThan(Vector<Short, Shapes.S128Bit> v) {
        Short128Vector vc = (Short128Vector) v;
        return new Int128Vector(PatchableVecUtils.vpcmpgtw(vc.toLong2(), this.vec));
    }

    @Override
    public Vector<Integer, Shapes.S128Bit> greaterThan(Vector<Short, Shapes.S128Bit> v) {
        Short128Vector vc = (Short128Vector) v;
        return new Int128Vector(PatchableVecUtils.vpcmpgtw(this.vec,vc.toLong2()));
    }

    @Override
    public Mask<Shapes.S128Bit> test(Predicate<Short> op) {
        boolean[] ary = new boolean[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = op.test(this.getElement(i));
        }
        return new Mask64Bit(ary);
    }

    @Override
    public Vector<Short, Shapes.S128Bit> map(UnaryOperator<Short> op) {
        short[] ary = new short[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = op.apply(this.getElement(i));
        }
        return new Short128Vector(ary);
    }

    @Override
    public Vector<Short, Shapes.S128Bit> mapWhere(Mask<Shapes.S128Bit> mask, UnaryOperator<Short> op) {
        short[] ary = new short[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = mask.getElement(i) ? op.apply(this.getElement(i)) : this.getElement(i);
        }
        return new Short128Vector(ary);
    }

    @Override
    public Vector<Short, Shapes.S128Bit> map(BinaryOperator<Short> op, Vector<Short, Shapes.S128Bit> this2) {
        Short128Vector this2c = (Short128Vector) this2;
        short[] ary = new short[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = op.apply(this.getElement(i), this2c.getElement(i));
        }
        return new Short128Vector(ary);
    }

    @Override
    public Vector<Short, Shapes.S128Bit> mapWhere(Mask<Shapes.S128Bit> mask, BinaryOperator<Short> op, Vector<Short, Shapes.S128Bit> this2) {
        Short128Vector this2c = (Short128Vector) this2;
        short[] ary = new short[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = mask.getElement(i) ? op.apply(this.getElement(i), this2c.getElement(i)) : this.getElement(i);
        }
        return new Short128Vector(ary);
    }

    @Override
    public boolean isZeros() {
        return this.vec.equals(POSZERO.toLong2());
    }

    // TODO
    @Override
    @ForceInline
    public Vector<Short, Shapes.S128Bit> neg() {
        throw new UnsupportedOperationException();
    }

//    @Override
    public Vector<Short, Shapes.S128Bit> and(Vector<Short, Shapes.S128Bit> o) {
        Short128Vector oc = (Short128Vector) o;
        return new Short128Vector(PatchableVecUtils.vpand(this.vec,oc.toLong2()));
    }

    @Override
    @ForceInline
    public Vector<Short, Shapes.S128Bit> xor(Vector<Short, Shapes.S128Bit> o) {
        Short128Vector oc = (Short128Vector) o;
        return new Short128Vector(PatchableVecUtils.vpxor(this.vec, oc.toLong2()));
    }

    // TODO
//    @Override
    public Vector<Short, Shapes.S128Bit> sqrt() {
        throw new UnsupportedOperationException();
    }

    @Override
    @ForceInline
    public Vector<Short, Shapes.S128Bit> add(Vector<Short, Shapes.S128Bit> o) {
        Short128Vector oc = (Short128Vector) o;
        return new Short128Vector(PatchableVecUtils.vpaddw(this.vec, oc.toLong2()));
    }

//    @Override
    public Vector<Short, Shapes.S128Bit> sub(Vector<Short, Shapes.S128Bit> o) {
        Short128Vector oc = (Short128Vector) o;
        return new Short128Vector(PatchableVecUtils.vpsubw(this.vec,oc.toLong2()));
    }

    // TODO
    @Override
    @ForceInline
    public Vector<Short, Shapes.S128Bit> mul(Vector<Short, Shapes.S128Bit> o) {
        throw new UnsupportedOperationException();
    }

    @Override
    @ForceInline
    public <T> T reduce(Function<Short, T> mapper, BinaryOperator<T> op) {
        T accumulator = mapper.apply(this.getElement(0));
        for (int i = 1; i < this.length(); i++) {
            accumulator = op.apply(accumulator, mapper.apply(this.getElement(i)));
        }
        return accumulator;
    }

    @Override
    public Mask<Shapes.S128Bit> toMask() {
        boolean[] ary = new boolean[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = this.getElement(i) != 0f;
        }
        return new Mask64Bit(ary);
    }

    @Override
    public Short sumAll() {
        short res = 0;
        for (int i = 0; i < this.length(); i++) {
            res += this.getElement(i);
        }
        return res;
    }


    @Override
    public Vector<Short, Shapes.S128Bit> blend(Vector<Short, Shapes.S128Bit> b, Vector<Short, Shapes.S128Bit> mask) {
        Short128Vector bc = (Short128Vector) b;
        short[] ary = new short[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = mask.getElement(i) == 0 ? this.getElement(i) : bc.getElement(i);
        }
        return new Short128Vector(ary);
    }

    @Override
    public Vector<Short, Shapes.S128Bit> shuffle(Shuffle<Shapes.S128Bit> perm) {
        int[] p = perm.toArray();
        short[] buf = new short[this.length()];
        for (int i = 0; i < this.length(); i++) {
            buf[p[i]] = this.getElement(i);
        }
        return new Short128Vector(buf);
    }

    @Override
    public Shuffle<Shapes.S128Bit> toShuffle() {
        return new Shuffle128(this.vec);
    }

    // TODO
//    @Override
    public Vector<Float, Shapes.S128Bit> toFloatVector() {
        throw new UnsupportedOperationException("Conversions to Double not supported.");
    }

    // TODO
//    @Override
    public Vector<Integer, Shapes.S128Bit> toIntVector() {
        throw new UnsupportedOperationException("Conversions to Double not supported.");
    }

    // TODO
//    @Override
    public Vector<Double, Shapes.S128Bit> toDoubleVector() {
        throw new UnsupportedOperationException("Conversions to Double not supported.");
    }

//    @Override
    public Vector<Float, Shapes.S128Bit> toFloatVectorBits() {
        return new Float128Vector(this.vec);
    }

//    @Override
    public Vector<Integer, Shapes.S128Bit> toIntVectorBits() {
        return new Int128Vector(this.vec);
    }

//    @Override
    public Vector<Double, Shapes.S128Bit> toDoubleVectorBits() {
        return new Double128Vector(this.vec);
    }

    private static class Short128VectorSpecies implements Species<Short, Shapes.S128Bit> {

        @Override
        public Shapes.S128Bit shape() {
            return Shapes.SHAPE_128_BIT;
        }

        @Override
        public Class<Short> elementType() {
            return Short.class;
        }

        @Override
        public int elementSize() {
            return 8;
        }

        @Override
        public Function<Short, Vector<Short, Shapes.S128Bit>> fromElementFactory() {
            return (Short s) -> new Short128Vector(PatchableVecUtils.broadcastShortL2(s));
        }

        @Override
        public Vector<Short, Shapes.S128Bit> generate(IntFunction<? extends Short> generator) {
            throw new UnsupportedOperationException(); //IntFunction doesn't map to float lanes.
        }

        @Override
        public BiFunction<Short[], Integer, Vector<Short, Shapes.S128Bit>> fromArrayFactory() {
            return (Short[] ary, Integer offset) -> {
                short[] tmp = new short[this.length()];
                for (int i = 0; i < this.length(); i++) {
                    tmp[i] = ary[i];
                }
                return STUB.fromArray(tmp, 0);
            };
        }

//        @Override
        public BiFunction<int[], Integer, Vector<Short, Shapes.S128Bit>> fromIntArrayFactory() {
            return (int[] ints, Integer offset) -> new Short128Vector(PatchableVecUtils.long2FromIntArray(ints,offset));
        }

//        @Override
        public BiFunction<float[], Integer, Vector<Short, Shapes.S128Bit>> fromFloatArrayFactory() {
            return (float[] floats, Integer offset) -> new Short128Vector(PatchableVecUtils.long2FromFloatArray(floats,offset));
        }

//        @Override
        public BiFunction<double[], Integer, Vector<Short, Shapes.S128Bit>> fromDoubleArrayFactory() {
            return (double[] doubles, Integer offset) -> new Short128Vector(PatchableVecUtils.long2FromDoubleArray(doubles,offset));
        }

//        @Override
        public BiFunction<long[], Integer, Vector<Short, Shapes.S128Bit>> fromLongArrayFactory() {
            return (long[] longs, Integer offset) -> new Short128Vector(PatchableVecUtils.long2FromLongArray(longs,offset));
        }

        @Override
        public Shuffle<Shapes.S128Bit> iota() {
            return iota(0, 1, Integer.MAX_VALUE);
        }

        @Override
        // arithmetic sequence ((B+i*S)%M)
        public Shuffle<Shapes.S128Bit> iota(int base, int step, int modulus) {
            int arr[] = IntStream.range(0, this.length())
                    .map((int i) -> (base + i * step) % modulus)
                    .toArray();
            return new Shuffle128(arr);
        }

    }

    private static class Mask64Bit implements Mask<Shapes.S128Bit> {
        private boolean[] bits = new boolean[this.length()];


        Mask64Bit(boolean[] bits) {
            this.bits = bits;
        }

        @Override
        public int length() {
            return 8;
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
            if (type == Short.class) {
                Vector<Short, Shapes.S128Bit> v = new Short128Vector(Long2.ZERO);
                for (int i = 0; i < this.length(); i++) {
                    if (bits[i]) {
                        v = v.putElement(i, (short)1);  //NOTE: What does it mean for a short to be masked here?
                    }
                }
                return (Vector<E, Shapes.S128Bit>) v;

            }
            throw new UnsupportedOperationException();
        }
    }

    private static class Shuffle128 implements Shuffle<Shapes.S128Bit> {

        final int[] shuf;

        private Long2 vec() {
            return PatchableVecUtils.long2FromIntArray(shuf, 0);
        }

        public Shuffle128() {
            int[] nshuf = new int[this.length()];
            for (int i = 0; i < nshuf.length; i++) {
                nshuf[i] = i;
            }
            shuf = nshuf;
        }

        Shuffle128(int[] s) {
            shuf = s;
        }

        public Shuffle128(Long2 vec) {
            shuf = new int[this.length()];
            PatchableVecUtils.long2ToIntArray(shuf, 0, vec);
        }

        @Override
        public Long2 toLong2() {
            return vec();
        }

        @Override
        public Long4 toLong4() {
            Long2 vec = vec();
            return Long4.make(vec.extract(0), vec.extract(1), 0L, 0L);
        }

        @Override
        public Long8 toLong8() {
            Long2 vec = vec();
            return Long8.make(vec.extract(0), vec.extract(1),
                    0L, 0L, 0L, 0L, 0L, 0L);
        }

        @Override
        public int length() {
            return 8;
        }

        @Override
        public int[] toArray() {
            return shuf;
        }

        @Override
        public Vector<Integer, Shapes.S128Bit> toVector() {
            return new Int128Vector(vec());
        }

        @Override
        @SuppressWarnings("unchecked")
        public <E> Vector<E, Shapes.S128Bit> toVector(Class<E> type) {
            if (type == Short.class) {
                short[] buffer = new short[this.length()];
                for (int i = 0; i < shuf.length; i++) {
                    buffer[i] = (byte) shuf[i];
                }
                return (Vector<E, Shapes.S128Bit>) new Short128Vector(buffer);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }
}
