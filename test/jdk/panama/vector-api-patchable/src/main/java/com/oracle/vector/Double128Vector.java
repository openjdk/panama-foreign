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


import javax.sound.midi.Patch;

import static com.oracle.vector.PatchableVecUtils.*;

import java.util.BitSet;
import java.util.function.*;
import java.util.stream.IntStream;

import java.lang.reflect.Field;
import java.lang.*;

public class Double128Vector extends DoubleVector<Shapes.S128Bit> {

    private final Long2 vec;

    private static final Double128Vector STUB = new Double128Vector(Long2.ZERO);
    private static final Double128VectorSpecies SPECIES = new Double128VectorSpecies();

    //Constants for zero comparisons
    private static final double[] pzArray = {0f, 0f};
    private static final double[] nzArray = {-0f, -0f};
    private static final Double128Vector POSZERO = (Double128Vector) STUB.fromArray(pzArray, 0);
    private static final Double128Vector NEGZERO = (Double128Vector) STUB.fromArray(nzArray, 0);

    Double128Vector(Long2 v) {
        this.vec = v;
    }

    private Double128Vector(double[] ary) {
        this.vec = PatchableVecUtils.long2FromDoubleArray(ary, 0);
    }

    @Override
    public Vector<Double, Shapes.S128Bit> fromArray(double[] ary, int offset) {
        Long2 v = PatchableVecUtils.long2FromDoubleArray(ary, 0);
        return new Double128Vector(v);
    }

    @Override
    public void intoArray(double[] ary, int offset) {
        PatchableVecUtils.long2ToDoubleArray(ary, offset, this.vec);
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
    public Species<Double, Shapes.S128Bit> species() {
        return SPECIES;
    }

    @Override
    public Double getElement(int index) {
        if (index >= this.length()) throw new IllegalArgumentException("getElement index must be 0-1 inclusive");
        double dVal = Double.longBitsToDouble(PatchableVecUtils.vpextrq(index, this.vec));
        return (Double) dVal;
    }

    @Override
    public Vector<Double, Shapes.S128Bit> putElement(int index, Double x) {
        if (index >= this.length()) throw new IllegalArgumentException("putElement index must be 0-1 inclusive");
        return new Double128Vector(PatchableVecUtils.vpinsrq(this.vec, Double.doubleToLongBits(x), index));
    }

//    @Override
    public void intoArray(byte[] a, int o) {
        PatchableVecUtils.long2ToByteArray(a,o,this.vec);
    }

    @Override
    public Vector<Integer, Shapes.S128Bit> compareEqual(Vector<Double, Shapes.S128Bit> v) {
        Double128Vector v2 = (Double128Vector) v;
        return new Int128Vector(PatchableVecUtils.vcmpeqpd(this.vec, v.toLong2()));
    }

    @Override
    public Vector<Integer, Shapes.S128Bit> lessThan(Vector<Double, Shapes.S128Bit> v) {
        Double128Vector v2 = (Double128Vector) v;
        return new Int128Vector(PatchableVecUtils.vcmpltpd(this.vec, v.toLong2()));
    }

    @Override
    public Vector<Integer, Shapes.S128Bit> greaterThan(Vector<Double, Shapes.S128Bit> v) {
        Double128Vector v2 = (Double128Vector) v;
        return new Int128Vector(PatchableVecUtils.vcmpgtpd(this.vec, v.toLong2()));
    }

    @Override
    public Mask<Shapes.S128Bit> test(Predicate<Double> op) {
        boolean[] ary = new boolean[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = op.test(this.getElement(i));
        }
        return new Double128Vector.Mask64Bit(ary);
    }

    @Override
    public Vector<Double, Shapes.S128Bit> map(UnaryOperator<Double> op) {
        double[] ary = new double[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = op.apply(this.getElement(i));
        }
        return new Double128Vector(ary);
    }

    @Override
    public Vector<Double, Shapes.S128Bit> mapWhere(Mask<Shapes.S128Bit> mask, UnaryOperator<Double> op) {
        double[] ary = new double[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = mask.getElement(i) ? op.apply(this.getElement(i)) : this.getElement(i);
        }
        return new Double128Vector(ary);
    }

    @Override
    public Vector<Double, Shapes.S128Bit> map(BinaryOperator<Double> op, Vector<Double, Shapes.S128Bit> this2) {
        Double128Vector this2c = (Double128Vector) this2;
        double[] ary = new double[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = op.apply(this.getElement(i), this2c.getElement(i));
        }
        return new Double128Vector(ary);
    }

    @Override
    public Vector<Double, Shapes.S128Bit> mapWhere(Mask<Shapes.S128Bit> mask, BinaryOperator<Double> op, Vector<Double, Shapes.S128Bit> this2) {
        Double128Vector this2c = (Double128Vector) this2;
        double[] ary = new double[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = mask.getElement(i) ? op.apply(this.getElement(i), this2c.getElement(i)) : this.getElement(i);
        }
        return new Double128Vector(ary);
    }

    @Override
    public boolean isZeros() {
        return vec.equals(POSZERO.toLong2()) || this.vec.equals(NEGZERO.toLong2());
    }

    @Override
    public Vector<Double, Shapes.S128Bit> neg() {
        return new Double128Vector(PatchableVecUtils.vxorpd(this.vec, NEGZERO.toLong2()));
    }

//    @Override
    public Vector<Double, Shapes.S128Bit> and(Vector<Double, Shapes.S128Bit> o) {
        Double128Vector oc = (Double128Vector) o;
        return new Double128Vector(PatchableVecUtils.vandpd(this.vec,oc.toLong2()));
    }

//    @Override
    public Vector<Double, Shapes.S128Bit> sqrt() {
        return new Double128Vector(PatchableVecUtils.vsqrtpd(this.vec));
    }

    @Override
    public Vector<Double, Shapes.S128Bit> xor(Vector<Double, Shapes.S128Bit> o) {
        Double128Vector oc = (Double128Vector) o;
        return new Double128Vector(PatchableVecUtils.vxorpd(this.vec, oc.toLong2()));
    }

    @Override
    public Vector<Double, Shapes.S128Bit> add(Vector<Double, Shapes.S128Bit> o) {
        Double128Vector oc = (Double128Vector) o;
        return new Double128Vector(PatchableVecUtils.vaddpd(vec, oc.toLong2()));
    }

//    @Override
    public Vector<Double, Shapes.S128Bit> sub(Vector<Double, Shapes.S128Bit> o) {
        Double128Vector oc = (Double128Vector) o;
        return new Double128Vector(PatchableVecUtils.vsubpd(this.vec,oc.toLong2()));
    }

    @Override
    public Vector<Double, Shapes.S128Bit> mul(Vector<Double, Shapes.S128Bit> o) {
        Double128Vector oc = (Double128Vector) o;
        return new Double128Vector(PatchableVecUtils.vmulpd(this.vec, oc.toLong2()));
    }

    @Override
    public <T> T reduce(Function<Double, T> mapper, BinaryOperator<T> op) {
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
    public Double sumAll() {
        double res = 0f;
        for (int i = 0; i < this.length(); i++) {
            res += this.getElement(i);
        }
        return res;
    }

    @Override
    public Vector<Double, Shapes.S128Bit> blend(Vector<Double, Shapes.S128Bit> b, Vector<Double, Shapes.S128Bit> mask) {
        Double128Vector bc = (Double128Vector) b;
        double[] ary = new double[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = mask.getElement(i) == 0 ? this.getElement(i) : bc.getElement(i);
        }
        return new Double128Vector(ary);
    }

    @Override
    public Vector<Double, Shapes.S128Bit> shuffle(Shuffle<Shapes.S128Bit> perm) {
        int[] p = perm.toArray();
        double[] buf = new double[this.length()];
        for (int i = 0; i < this.length(); i++) {
            buf[p[i]] = this.getElement(i);
        }
        return new Double128Vector(buf);
    }

    @Override
    public Shuffle<Shapes.S128Bit> toShuffle() {
        return new Shuffle128(this.vec);
    }

//    @Override
    public Vector<Float, Shapes.S128Bit> toFloatVector() {
        throw new UnsupportedOperationException("Double Conversions Unsupported.");
    }

//    @Override
    public Vector<Integer, Shapes.S128Bit> toIntVector() {
        throw new UnsupportedOperationException("Double Conversions Unsupported.");
    }

//    @Override
    public Vector<Double, Shapes.S128Bit> toDoubleVector() {
        return this;
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
        return this;
    }

    private static class Double128VectorSpecies implements Species<Double, Shapes.S128Bit> {

        @Override
        public Shapes.S128Bit shape() {
            return Shapes.SHAPE_128_BIT;
        }

        @Override
        public Class<Double> elementType() {
            return Double.class;
        }

        @Override
        public int elementSize() {
            return 64;
        }

        @Override
        public Function<Double, Vector<Double, Shapes.S128Bit>> fromElementFactory() {
            return (Double d) -> new Double128Vector(broadcastDoubleL2(d));
        }

        @Override
        public Vector<Double, Shapes.S128Bit> generate(IntFunction<? extends Double> generator) {
            throw new UnsupportedOperationException(); //IntFunction doesn't map to float lanes.
        }

        @Override
        public BiFunction<Double[], Integer, Vector<Double, Shapes.S128Bit>> fromArrayFactory() {
            return (Double[] ary, Integer offset) -> {
                double[] tmp = new double[this.length()];
                for (int i = 0; i < this.length(); i++) {
                    tmp[i] = ary[i];
                }
                return STUB.fromArray(tmp, 0);
            };
        }

//        @Override
        public BiFunction<int[], Integer, Vector<Double, Shapes.S128Bit>> fromIntArrayFactory() {
           return (int[] ints, Integer offset) -> new Double128Vector(PatchableVecUtils.long2FromIntArray(ints,offset));
        }

//        @Override
        public BiFunction<float[], Integer, Vector<Double, Shapes.S128Bit>> fromFloatArrayFactory() {
            return (float[] floats, Integer offset) -> new Double128Vector(PatchableVecUtils.long2FromFloatArray(floats,offset));
        }

//        @Override
        public BiFunction<double[], Integer, Vector<Double, Shapes.S128Bit>> fromDoubleArrayFactory() {
            return (double[] doubles, Integer offset) -> new Double128Vector(PatchableVecUtils.long2FromDoubleArray(doubles,offset));
        }

//        @Override
        public BiFunction<long[], Integer, Vector<Double, Shapes.S128Bit>> fromLongArrayFactory() {
            return (long[] longs, Integer offset) -> new Double128Vector(PatchableVecUtils.long2FromLongArray(longs,offset));
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
            return 2;
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
        public <E> Vector<E, Shapes.S128Bit> toVector(Class<E> type) {
            if (type == Double.class) {
                Vector<Double, Shapes.S128Bit> v = new Double128Vector(Long2.ZERO);
                for (int i = 0; i < this.length(); i++) {
                    if (bits[i]) {
                        v = v.putElement(i, 1.0);
                    }
                }
                return (Vector<E, Shapes.S128Bit>) v;

            }
            throw new UnsupportedOperationException();
        }
    }

    private static class Shuffle128 implements Shuffle<Shapes.S128Bit> {

        int[] shuf;

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
        public <E> Vector<E, Shapes.S128Bit> toVector(Class<E> type) {
            if (type == Double.class) {
                double[] buffer = new double[this.length()];
                for (int i = 0; i < shuf.length; i++) {
                    buffer[i] = (double) shuf[i];
                }
                return (Vector<E, Shapes.S128Bit>) new Double128Vector(buffer);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }
}
