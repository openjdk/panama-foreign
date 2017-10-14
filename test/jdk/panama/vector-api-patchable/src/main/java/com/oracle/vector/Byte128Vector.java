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

class Byte128Vector extends ByteVector<Shapes.S128Bit> {

    private @Stable final Long2 vec;

    private static final Byte128Vector STUB = new Byte128Vector(Long2.ZERO);
    private static final Byte128VectorSpecies SPECIES = new Byte128VectorSpecies();

    //Constants for zero comparisons
    private static final byte[] pzArray = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private static final Byte128Vector POSZERO = (Byte128Vector) STUB.fromArray(pzArray, 0);

    Byte128Vector(Long2 v) {
        this.vec = v;
    }

    private Byte128Vector(byte[] ary) {
        this.vec = PatchableVecUtils.long2FromByteArray(ary, 0);
    }

    @Override
    public Vector<Byte, Shapes.S128Bit> fromArray(byte[] ary, int offset) {
        return new Byte128Vector(PatchableVecUtils.long2FromByteArray(ary, offset));
    }

    @Override
    public void intoArray(byte[] ary, int offset) {
        PatchableVecUtils.long2ToByteArray(ary, offset, vec);
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
    public Species<Byte, Shapes.S128Bit> species() {
        return SPECIES;
    }

    @Override
    public Byte getElement(int index) {
        if (index >= this.length()) throw new IllegalArgumentException("getElement index must be 0-15 inclusive");
        return (Byte)PatchableVecUtils.vpextrb(index, this.vec);
    }

    @Override
    public Vector<Byte, Shapes.S128Bit> putElement(int index, Byte x) {
        if (index >= this.length()) throw new IllegalArgumentException("putElement index must be 0-15 inclusive");
        return new Byte128Vector(PatchableVecUtils.vpinsrb(this.vec, x, index));
    }

    @Override
    public Vector<Integer, Shapes.S128Bit> compareEqual(Vector<Byte, Shapes.S128Bit> v) {
        Byte128Vector vc = (Byte128Vector) v;
        return new Int128Vector(PatchableVecUtils.vpcmpeqb(this.vec, vc.toLong2()));
    }

    @Override
    public Vector<Integer, Shapes.S128Bit> lessThan(Vector<Byte, Shapes.S128Bit> v) {
        Byte128Vector vc = (Byte128Vector) v;
        return new Int128Vector(PatchableVecUtils.vpcmpgtb(vc.toLong2(), this.vec));
    }

    @Override
    public Vector<Integer, Shapes.S128Bit> greaterThan(Vector<Byte, Shapes.S128Bit> v) {
        Byte128Vector vc = (Byte128Vector) v;
        return new Int128Vector(PatchableVecUtils.vpcmpgtb(this.vec,vc.toLong2()));
    }

    @Override
    public Mask<Shapes.S128Bit> test(Predicate<Byte> op) {
        boolean[] ary = new boolean[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = op.test(this.getElement(i));
        }
        return new Mask64Bit(ary);
    }

    @Override
    public Vector<Byte, Shapes.S128Bit> map(UnaryOperator<Byte> op) {
        byte[] ary = new byte[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = op.apply(this.getElement(i));
        }
        return new Byte128Vector(ary);
    }

    @Override
    public Vector<Byte, Shapes.S128Bit> mapWhere(Mask<Shapes.S128Bit> mask, UnaryOperator<Byte> op) {
        byte[] ary = new byte[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = mask.getElement(i) ? op.apply(this.getElement(i)) : this.getElement(i);
        }
        return new Byte128Vector(ary);
    }

    @Override
    public Vector<Byte, Shapes.S128Bit> map(BinaryOperator<Byte> op, Vector<Byte, Shapes.S128Bit> this2) {
        Byte128Vector this2c = (Byte128Vector) this2;
        byte[] ary = new byte[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = op.apply(this.getElement(i), this2c.getElement(i));
        }
        return new Byte128Vector(ary);
    }

    @Override
    public Vector<Byte, Shapes.S128Bit> mapWhere(Mask<Shapes.S128Bit> mask, BinaryOperator<Byte> op, Vector<Byte, Shapes.S128Bit> this2) {
        Byte128Vector this2c = (Byte128Vector) this2;
        byte[] ary = new byte[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = mask.getElement(i) ? op.apply(this.getElement(i), this2c.getElement(i)) : this.getElement(i);
        }
        return new Byte128Vector(ary);
    }

    @Override
    public boolean isZeros() {
        return this.vec.equals(POSZERO.toLong2());
    }

    // TODO
    @Override
    @ForceInline
    public Vector<Byte, Shapes.S128Bit> neg() {
        throw new UnsupportedOperationException();
    }

//    @Override
    public Vector<Byte, Shapes.S128Bit> and(Vector<Byte, Shapes.S128Bit> o) {
        Byte128Vector oc = (Byte128Vector) o;
        return new Byte128Vector(PatchableVecUtils.vpand(this.vec,oc.toLong2()));
    }

    @Override
    @ForceInline
    public Vector<Byte, Shapes.S128Bit> xor(Vector<Byte, Shapes.S128Bit> o) {
        Byte128Vector oc = (Byte128Vector) o;
        return new Byte128Vector(PatchableVecUtils.vpxor(this.vec, oc.toLong2()));
    }

    // TODO
//    @Override
    public Vector<Byte, Shapes.S128Bit> sqrt() {
        throw new UnsupportedOperationException();
    }

    @Override
    @ForceInline
    public Vector<Byte, Shapes.S128Bit> add(Vector<Byte, Shapes.S128Bit> o) {
        Byte128Vector oc = (Byte128Vector) o;
        return new Byte128Vector(PatchableVecUtils.vpaddb(this.vec, oc.toLong2()));
    }

    // TODO
//    @Override
    public Vector<Byte, Shapes.S128Bit> sub(Vector<Byte, Shapes.S128Bit> o) {
        Byte128Vector oc = (Byte128Vector) o;
        return new Byte128Vector(PatchableVecUtils.vpsubb(this.vec,oc.toLong2()));
    }

    // TODO
    @Override
    @ForceInline
    public Vector<Byte, Shapes.S128Bit> mul(Vector<Byte, Shapes.S128Bit> o) {
        throw new UnsupportedOperationException();
    }

    @Override
    @ForceInline
    public <T> T reduce(Function<Byte, T> mapper, BinaryOperator<T> op) {
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
    public Byte sumAll() {
        byte res = 0;
        for (int i = 0; i < this.length(); i++) {
            res += this.getElement(i);
        }
        return res;
    }


    @Override
    public Vector<Byte, Shapes.S128Bit> blend(Vector<Byte, Shapes.S128Bit> b, Vector<Byte, Shapes.S128Bit> mask) {
        Byte128Vector bc = (Byte128Vector) b;
        byte[] ary = new byte[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = mask.getElement(i) == 0 ? this.getElement(i) : bc.getElement(i);
        }
        return new Byte128Vector(ary);
    }

    @Override
    public Vector<Byte, Shapes.S128Bit> shuffle(Shuffle<Shapes.S128Bit> perm) {
        int[] p = perm.toArray();
        byte[] buf = new byte[this.length()];
        for (int i = 0; i < this.length(); i++) {
            buf[p[i]] = this.getElement(i);
        }
        return new Byte128Vector(buf);
    }

    @Override
    public Shuffle<Shapes.S128Bit> toShuffle() {
        return new Shuffle128(this.vec);
    }

    // From here: Implement it properly
//    @Override
    public Vector<Float, Shapes.S128Bit> toFloatVector() {
        throw new UnsupportedOperationException("Conversions to Double not supported.");
    }

//    @Override
    public Vector<Integer, Shapes.S128Bit> toIntVector() {
        throw new UnsupportedOperationException("Conversions to Double not supported.");
    }

//    @Override
    public Vector<Double, Shapes.S128Bit> toDoubleVector() {
        throw new UnsupportedOperationException("Conversions to Double not supported.");
    }
    // Till here: Implement it properly

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

    private static class Byte128VectorSpecies implements Species<Byte, Shapes.S128Bit> {

        @Override
        public Shapes.S128Bit shape() {
            return Shapes.SHAPE_128_BIT;
        }

        @Override
        public Class<Byte> elementType() {
            return Byte.class;
        }

        @Override
        public int elementSize() {
            return 8;
        }

        @Override
        public Function<Byte, Vector<Byte, Shapes.S128Bit>> fromElementFactory() {
            return (Byte b) -> new Byte128Vector(PatchableVecUtils.broadcastByteL2(b));
        }

        @Override
        public Vector<Byte, Shapes.S128Bit> generate(IntFunction<? extends Byte> generator) {
            throw new UnsupportedOperationException(); //IntFunction doesn't map to float lanes.
        }

        @Override
        public BiFunction<Byte[], Integer, Vector<Byte, Shapes.S128Bit>> fromArrayFactory() {
            return (Byte[] ary, Integer offset) -> {
                byte[] tmp = new byte[this.length()];
                for (int i = 0; i < this.length(); i++) {
                    tmp[i] = ary[i];
                }
                return STUB.fromArray(tmp, 0);
            };
        }

//        @Override
        public BiFunction<int[], Integer, Vector<Byte, Shapes.S128Bit>> fromIntArrayFactory() {
            return (int[] ints, Integer offset) -> new Byte128Vector(PatchableVecUtils.long2FromIntArray(ints,offset));
        }

//        @Override
        public BiFunction<float[], Integer, Vector<Byte, Shapes.S128Bit>> fromFloatArrayFactory() {
            return (float[] floats, Integer offset) -> new Byte128Vector(PatchableVecUtils.long2FromFloatArray(floats,offset));
        }

//        @Override
        public BiFunction<double[], Integer, Vector<Byte, Shapes.S128Bit>> fromDoubleArrayFactory() {
            return (double[] doubles, Integer offset) -> new Byte128Vector(PatchableVecUtils.long2FromDoubleArray(doubles,offset));
        }

//        @Override
        public BiFunction<long[], Integer, Vector<Byte, Shapes.S128Bit>> fromLongArrayFactory() {
            return (long[] longs, Integer offset) -> new Byte128Vector(PatchableVecUtils.long2FromLongArray(longs,offset));
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
            return 16;
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
            if (type == Byte.class) {
                Vector<Byte, Shapes.S128Bit> v = new Byte128Vector(Long2.ZERO);
                for (int i = 0; i < this.length(); i++) {
                    if (bits[i]) {
                        v = v.putElement(i, (byte)1);  //NOTE: What does it mean for a byte to be masked here?
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
            if (type == Byte.class) {
                byte[] buffer = new byte[this.length()];
                for (int i = 0; i < shuf.length; i++) {
                    buffer[i] = (byte) shuf[i];
                }
                return (Vector<E, Shapes.S128Bit>) new Byte128Vector(buffer);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }
}
