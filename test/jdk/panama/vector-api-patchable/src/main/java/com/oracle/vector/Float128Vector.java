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

class Float128Vector extends FloatVector<Shapes.S128Bit> {

    private @Stable final Long2 vec;

    private static final Float128Vector STUB = new Float128Vector(Long2.ZERO);
    private static final Float128VectorSpecies SPECIES = new Float128VectorSpecies();

    //Constants for zero comparisons
    private static final float[] pzArray = {0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};
    private static final float[] nzArray = {-0f, -0f, -0f, -0f, -0f, -0f, -0f, -0f};
    private static final Float128Vector POSZERO = (Float128Vector) STUB.fromArray(pzArray, 0);
    private static final Float128Vector NEGZERO = (Float128Vector) STUB.fromArray(nzArray, 0);

    Float128Vector(Long2 v) {
        this.vec = v;
    }

    private Float128Vector(float[] ary) {
        this.vec = PatchableVecUtils.long2FromFloatArray(ary, 0);
    }

    @Override
    public Vector<Float, Shapes.S128Bit> fromArray(float[] ary, int offset) {
        return new Float128Vector(PatchableVecUtils.long2FromFloatArray(ary, offset));
    }

    @Override
    public void intoArray(float[] ary, int offset) {
        PatchableVecUtils.long2ToFloatArray(ary, offset, this.vec);
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
    public Species<Float, Shapes.S128Bit> species() {
        return SPECIES;
    }

    @Override
    public Float getElement(int index) {
        if (index >= this.length()) throw new IllegalArgumentException("getElement index must be 0-3 inclusive");
        return PatchableVecUtils.vextractps(index, this.vec);
    }

    @Override
    public Vector<Float, Shapes.S128Bit> putElement(int index, Float x) {
        if (index >= this.length()) throw new IllegalArgumentException("putElement index must be 0-3 inclusive");
        return new Float128Vector(PatchableVecUtils.vpinsrd(this.vec, Float.floatToIntBits(x), index));
    }

//    @Override
    public void intoArray(byte[] a, int o) {
        PatchableVecUtils.long2ToByteArray(a,o,this.vec);
    }

    @Override
    public Vector<Integer, Shapes.S128Bit> compareEqual(Vector<Float, Shapes.S128Bit> v) {
        Float128Vector vc = (Float128Vector) v;
        return new Int128Vector(PatchableVecUtils.vcmpeqps(this.vec, vc.toLong2()));
    }

    @Override
    public Vector<Integer, Shapes.S128Bit> lessThan(Vector<Float, Shapes.S128Bit> v) {
        Float128Vector vc = (Float128Vector) v;
        return new Int128Vector(PatchableVecUtils.vcmpltps(this.vec,vc.toLong2()));
    }

    @Override
    public Vector<Integer, Shapes.S128Bit> greaterThan(Vector<Float, Shapes.S128Bit> v) {
        Float128Vector vc = (Float128Vector) v;
        return new Int128Vector(PatchableVecUtils.vcmpgtps(this.vec,vc.toLong2()));
    }

    @Override
    public Mask<Shapes.S128Bit> test(Predicate<Float> op) {
        boolean[] ary = new boolean[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = op.test(this.getElement(i));
        }
        return new Mask64Bit(ary);
    }

    @Override
    public Vector<Float, Shapes.S128Bit> map(UnaryOperator<Float> op) {
        float[] ary = new float[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = op.apply(this.getElement(i));
        }
        return new Float128Vector(ary);
    }

    @Override
    public Vector<Float, Shapes.S128Bit> mapWhere(Mask<Shapes.S128Bit> mask, UnaryOperator<Float> op) {
        float[] ary = new float[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = mask.getElement(i) ? op.apply(this.getElement(i)) : this.getElement(i);
        }
        return new Float128Vector(ary);
    }

    @Override
    public Vector<Float, Shapes.S128Bit> map(BinaryOperator<Float> op, Vector<Float, Shapes.S128Bit> this2) {
        Float128Vector this2c = (Float128Vector) this2;
        float[] ary = new float[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = op.apply(this.getElement(i), this2c.getElement(i));
        }
        return new Float128Vector(ary);
    }

    @Override
    public Vector<Float, Shapes.S128Bit> mapWhere(Mask<Shapes.S128Bit> mask, BinaryOperator<Float> op, Vector<Float, Shapes.S128Bit> this2) {
        Float128Vector this2c = (Float128Vector) this2;
        float[] ary = new float[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = mask.getElement(i) ? op.apply(this.getElement(i), this2c.getElement(i)) : this.getElement(i);
        }
        return new Float128Vector(ary);
    }

    @Override
    public boolean isZeros() {
        return this.vec.equals(POSZERO.toLong2()) || this.vec.equals(NEGZERO.toLong2());
    }

    @Override
    @ForceInline
    public Vector<Float, Shapes.S128Bit> neg() {
        return new Float128Vector(PatchableVecUtils.vxorps(this.vec, NEGZERO.toLong2()));
    }

//    @Override
    public Vector<Float, Shapes.S128Bit> and(Vector<Float, Shapes.S128Bit> o) {
        Float128Vector oc = (Float128Vector) o;
        return new Float128Vector(PatchableVecUtils.vandps(this.vec,oc.toLong2()));
    }

    @Override
    @ForceInline
    public Vector<Float, Shapes.S128Bit> xor(Vector<Float, Shapes.S128Bit> o) {
        Float128Vector oc = (Float128Vector) o;
        return new Float128Vector(PatchableVecUtils.vxorps(this.vec, oc.toLong2()));
    }

//    @Override
    public Vector<Float, Shapes.S128Bit> sqrt() {
        return new Float128Vector(PatchableVecUtils.vsqrtps(this.vec));
    }

    @Override
    @ForceInline
    public Vector<Float, Shapes.S128Bit> add(Vector<Float, Shapes.S128Bit> o) {
        Float128Vector oc = (Float128Vector) o;
        return new Float128Vector(PatchableVecUtils.vaddps(this.vec, oc.toLong2()));
    }

//    @Override
    public Vector<Float, Shapes.S128Bit> sub(Vector<Float, Shapes.S128Bit> o) {
        Float128Vector oc = (Float128Vector) o;
        return new Float128Vector(PatchableVecUtils.vsubps(this.vec,oc.toLong2()));
    }

    @Override
    @ForceInline
    public Vector<Float, Shapes.S128Bit> mul(Vector<Float, Shapes.S128Bit> o) {
        Float128Vector oc = (Float128Vector) o;
        return new Float128Vector(PatchableVecUtils.vmulps(this.vec, oc.toLong2()));
    }

    @Override
    @ForceInline
    public <T> T reduce(Function<Float, T> mapper, BinaryOperator<T> op) {
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
    public Float sumAll() {
        float res = 0f;
        for (int i = 0; i < this.length(); i++) {
            res += this.getElement(i);
        }
        return res;
    }


    @Override
    public Vector<Float, Shapes.S128Bit> blend(Vector<Float, Shapes.S128Bit> b, Vector<Float, Shapes.S128Bit> mask) {
        Float128Vector bc = (Float128Vector) b;
        float[] ary = new float[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = mask.getElement(i) == 0 ? this.getElement(i) : bc.getElement(i);
        }
        return new Float128Vector(ary);
    }

    @Override
    public Vector<Float, Shapes.S128Bit> shuffle(Shuffle<Shapes.S128Bit> perm) {
        int[] p = perm.toArray();
        float[] buf = new float[this.length()];
        for (int i = 0; i < this.length(); i++) {
            buf[p[i]] = this.getElement(i);
        }
        return new Float128Vector(buf);
    }

    @Override
    public Shuffle<Shapes.S128Bit> toShuffle() {
        return new Shuffle128(this.vec);
    }

//    @Override
    public Vector<Float, Shapes.S128Bit> toFloatVector() {
        return this;
    }

//    @Override
    public Vector<Integer, Shapes.S128Bit> toIntVector() {
        return new Int128Vector(PatchableVecUtils.vcvtps2dq(this.vec));
    }

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

    private static class Float128VectorSpecies implements Species<Float, Shapes.S128Bit> {

        @Override
        public Shapes.S128Bit shape() {
            return Shapes.SHAPE_128_BIT;
        }

        @Override
        public Class<Float> elementType() {
            return Float.class;
        }

        @Override
        public int elementSize() {
            return 32;
        }

        @Override
        public Function<Float, Vector<Float, Shapes.S128Bit>> fromElementFactory() {
            return (Float f) -> new Float128Vector(PatchableVecUtils.broadcastFloatL2(f));
        }

        @Override
        public Vector<Float, Shapes.S128Bit> generate(IntFunction<? extends Float> generator) {
            throw new UnsupportedOperationException(); //IntFunction doesn't map to float lanes.
        }

        @Override
        public BiFunction<Float[], Integer, Vector<Float, Shapes.S128Bit>> fromArrayFactory() {
            return (Float[] ary, Integer offset) -> {
                float[] tmp = new float[this.length()];
                for (int i = 0; i < this.length(); i++) {
                    tmp[i] = ary[i];
                }
                return STUB.fromArray(tmp, 0);
            };
        }

//        @Override
        public BiFunction<int[], Integer, Vector<Float, Shapes.S128Bit>> fromIntArrayFactory() {
            return (int[] ints, Integer offset) -> new Float128Vector(PatchableVecUtils.long2FromIntArray(ints,offset));
        }

//        @Override
        public BiFunction<float[], Integer, Vector<Float, Shapes.S128Bit>> fromFloatArrayFactory() {
            return (float[] floats, Integer offset) -> new Float128Vector(PatchableVecUtils.long2FromFloatArray(floats,offset));
        }

//        @Override
        public BiFunction<double[], Integer, Vector<Float, Shapes.S128Bit>> fromDoubleArrayFactory() {
            return (double[] doubles, Integer offset) -> new Float128Vector(PatchableVecUtils.long2FromDoubleArray(doubles,offset));
        }

//        @Override
        public BiFunction<long[], Integer, Vector<Float, Shapes.S128Bit>> fromLongArrayFactory() {
            return (long[] longs, Integer offset) -> new Float128Vector(PatchableVecUtils.long2FromLongArray(longs,offset));
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
            if (type == Float.class) {
                Vector<Float, Shapes.S128Bit> v = new Float128Vector(Long2.ZERO);
                for (int i = 0; i < this.length(); i++) {
                    if (bits[i]) {
                        v = v.putElement(i, 1f);  //NOTE: What does it mean for a float to be masked here?
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
            if (type == Float.class) {
                float[] buffer = new float[this.length()];
                for (int i = 0; i < shuf.length; i++) {
                    buffer[i] = (float) shuf[i];
                }
                return (Vector<E, Shapes.S128Bit>) new Float128Vector(buffer);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }
}
