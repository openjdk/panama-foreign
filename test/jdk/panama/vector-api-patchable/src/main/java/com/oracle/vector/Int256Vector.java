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
import jdk.internal.vm.annotation.Stable;

import java.util.function.*;
import java.util.stream.IntStream;
import java.util.BitSet;


import static com.oracle.vector.PatchableVecUtils.*;

class Int256Vector extends IntVector<Shapes.S256Bit> {

    private static final int LENGTH = 8;

    //Singleton Stub for static invocations of instance methods.
    private static final Int256Vector STUB = new Int256Vector(Long4.ZERO);
    private static final Species<Integer, Shapes.S256Bit> SPECIES = new Int256Vector.Int256VectorSpecies();

    @Stable private final Long4 vec;

    Int256Vector(Long4 vec) {
        this.vec = vec;
    }

    private Int256Vector(int[] ary){
        this.vec = PatchableVecUtils.long4FromIntArray(ary,0);
    }

    @Override
    @ForceInline public Vector<Integer, Shapes.S256Bit> fromArray(int[] ary, int index) {
        return new Int256Vector(PatchableVecUtils.long4FromIntArray(ary,index));
    }

    @Override
    @ForceInline public void intoArray(int[] ary, int index) {
        PatchableVecUtils.long4ToIntArray(ary,index,this.vec);
    }

    // TODO : Returns only the first 128 bits. getLowLong2, getHighLong2
    @Override
    public Long2 toLong2() {
        return PatchableVecUtils.vextractf128(0,this.vec);
    }

    @Override
    public Long4 toLong4() {
        return this.vec;
    }

    @Override
    public Long8 toLong8() {
        return Long8.make(this.vec.extract(0), this.vec.extract(1),
                this.vec.extract(2), this.vec.extract(3), 0L, 0L, 0L, 0L);
    }

    @Override
    public Species<Integer, Shapes.S256Bit> species() {
        return SPECIES;
    }

    @Override
    public Integer getElement(int index) {
        Long2 l2 = (index < 4) ? vextractf128(0,this.vec) : vextractf128(1,this.vec);
        return vpextrd(index, l2);
    }

    @Override
    public Vector<Integer, Shapes.S256Bit> putElement(int index, Integer x) {
        if (index >= LENGTH) throw new IllegalArgumentException("putElement index must be 0-7 inclusive");
        int[] arr = this.toArray();
        arr[index] = x;
        return this.fromArray(arr,0);
    }

//    @Override
    public void intoArray(byte[] a, int o) {
        PatchableVecUtils.long4ToByteArray(a,o,this.vec);
    }

    @Override
    public Vector<Integer, Shapes.S256Bit> compareEqual(Vector<Integer, Shapes.S256Bit> v) {
        Int256Vector v2 = (Int256Vector) v;
        return new Int256Vector(vpcmpeqd(this.vec, v2.toLong4()));
    }

    @Override
    public Vector<Integer, Shapes.S256Bit> lessThan(Vector<Integer, Shapes.S256Bit> v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Vector<Integer, Shapes.S256Bit> greaterThan(Vector<Integer, Shapes.S256Bit> v) {
        Int256Vector v2 = (Int256Vector) v;
        return new Int256Vector(PatchableVecUtils.vpcmpgtd(this.vec, v2.toLong4()));
    }

    @Override
    public Mask<Shapes.S256Bit> test(Predicate<Integer> op) {
        boolean[] ary = new boolean[this.length()];
        for(int i = 0; i < this.length(); i++){
            ary[i] = op.test(this.getElement(i));
        }
        return new Int256Vector.Mask64Bit(ary);
    }

    @Override
    public Vector<Integer, Shapes.S256Bit> map(UnaryOperator<Integer> op) {
        int[] ary = new int[this.length()];
        for(int i = 0; i < this.length(); i++){
            ary[i] = op.apply(this.getElement(i));
        }
        return new Int256Vector(ary);
    }

    @Override
    public Vector<Integer, Shapes.S256Bit> mapWhere(Mask<Shapes.S256Bit> mask, UnaryOperator<Integer> op) {
        int[] ary = new int[this.length()];
        for(int i = 0; i < this.length(); i++){
            ary[i] = mask.getElement(i) ? op.apply(this.getElement(i)) : this.getElement(i);
        }
        return new Int256Vector(ary);
    }

    @Override
    public Vector<Integer, Shapes.S256Bit> map(BinaryOperator<Integer> op, Vector<Integer, Shapes.S256Bit> this2) {
        Int256Vector this2c = (Int256Vector) this2;
        int[] ary = new int[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = op.apply(this.getElement(i), this2c.getElement(i));
        }
        return new Int256Vector(ary);
    }

        @Override
    public Vector<Integer, Shapes.S256Bit> mapWhere(Mask<Shapes.S256Bit> mask, BinaryOperator<Integer> op, Vector<Integer, Shapes.S256Bit> this2) {
        Int256Vector this2c = (Int256Vector) this2;
        int[] ary = new int[this.length()];
        for(int i = 0; i < this.length(); i++){
            ary[i] = mask.getElement(i) ? op.apply(this.getElement(i),this2c.getElement(i)) : this.getElement(i);
        }
        return new Int256Vector(ary);
    }

    @Override
    public boolean isZeros() {
        return this.vec.equals(Long4.ZERO);
    }

    @Override
    public Vector<Integer, Shapes.S256Bit> neg() {
        Long4 neg_ones = Long4.make(pack(-1, -1), pack(-1, -1), pack(-1, -1), pack(-1, -1));
        Long4 res = vpsignd(this.vec, neg_ones);
        return new Int256Vector(res);
    }

//    @Override
    public Vector<Integer, Shapes.S256Bit> and(Vector<Integer, Shapes.S256Bit> o) {
        Int256Vector o2 = (Int256Vector) o;
        return new Int256Vector(PatchableVecUtils.vpand(this.vec,o2.toLong4()));
    }

    @Override
    public Vector<Integer, Shapes.S256Bit> xor(Vector<Integer, Shapes.S256Bit> o) {
        Int256Vector o2 = (Int256Vector) o;
        return new Int256Vector(PatchableVecUtils.vpxor(this.vec, o2.toLong4()));
    }

    // TODO
//    @Override
    public Vector<Integer, Shapes.S256Bit> sqrt() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Vector<Integer, Shapes.S256Bit> add(Vector<Integer, Shapes.S256Bit> o) {
        Int256Vector o2 = (Int256Vector) o;
        return new Int256Vector(PatchableVecUtils.vpaddd(this.vec, o2.toLong4()));
    }

//    @Override
    public Vector<Integer, Shapes.S256Bit> sub(Vector<Integer, Shapes.S256Bit> o) {
        Int256Vector o2 = (Int256Vector) o;
        return new Int256Vector(PatchableVecUtils.vpsubd(this.vec,o2.toLong4()));
    }

    @Override
    public Vector<Integer, Shapes.S256Bit> mul(Vector<Integer, Shapes.S256Bit> o) {
        Int256Vector o2 = (Int256Vector) o;
        return new Int256Vector(PatchableVecUtils.vpmulld(this.vec, o2.toLong4()));
    }

    @Override
    public <T> T reduce(Function<Integer, T> mapper, BinaryOperator<T> op) {
        T accumulator = mapper.apply(this.getElement(0));
        for(int i = 1; i < this.length(); i++){
            accumulator = op.apply(accumulator,mapper.apply(this.getElement(i)));
        }
        return accumulator;
    }

    @Override
    public Mask<Shapes.S256Bit> toMask() {
        boolean[] ary = new boolean[this.length()];
        for(int i = 0; i < this.length(); i++){
            ary[i] = this.getElement(i) != 0;
        }
        return new Mask64Bit(ary);
    }

    @Override
    @ForceInline public Integer sumAll() {
        int[] res = this.toArray();
        int sum = 0;
        for(int i = 0; i < LENGTH; i++){
            sum += res[i];
        }
        return sum;
    }

    @Override
    public Vector<Integer, Shapes.S256Bit> blend(Vector<Integer, Shapes.S256Bit> b, Vector<Integer, Shapes.S256Bit> mask) {
        Int256Vector this2c = (Int256Vector) b;
        int[] ary = new int[this.length()];
        for(int i = 0; i < this.length(); i++){
            ary[i] = mask.getElement(i) == 0 ? this.getElement(i) : this2c.getElement(i);
        }
        return new Int256Vector(ary);
    }

    @Override
    public Vector<Integer, Shapes.S256Bit> shuffle(Shuffle<Shapes.S256Bit> perm) {
        int[] p = perm.toArray();
        int[] buf = new int[this.length()];
        for(int i = 0; i < this.length(); i++){
            buf[p[i]] = this.getElement(i);
        }
        return new Int256Vector(buf);
    }

    @Override
    public Shuffle<Shapes.S256Bit> toShuffle() {
        return new Shuffle256(this.vec);
    }

//    @Override
    public Vector<Float, Shapes.S256Bit> toFloatVector() {
        return new Float256Vector(PatchableVecUtils.vcvtdq2ps(this.vec));
    }

//    @Override
    public Vector<Integer, Shapes.S256Bit> toIntVector() {
        return this;
    }

//    @Override
    public Vector<Double, Shapes.S256Bit> toDoubleVector() {
        throw new UnsupportedOperationException("Conversions to Double not supported.");
    }

//    @Override
    public Vector<Float, Shapes.S256Bit> toFloatVectorBits() {
        return new Float256Vector(this.vec);
    }

//    @Override
    public Vector<Integer, Shapes.S256Bit> toIntVectorBits() {
        return this;
    }

//    @Override
    public Vector<Double, Shapes.S256Bit> toDoubleVectorBits() {
        return new Double256Vector(this.vec);
    }

    private static class Int256VectorSpecies implements Species<Integer, Shapes.S256Bit> {

        @Override
        public Shapes.S256Bit shape() {
            return Shapes.SHAPE_256_BIT;
        }

        @Override
        public Class<Integer> elementType() {
            return Integer.class;
        }

        @Override
        public int elementSize() {
            return 32;
        }

        @Override
        public Function<Integer, Vector<Integer, Shapes.S256Bit>> fromElementFactory() {
            return (Integer i) -> new Int256Vector(PatchableVecUtils.broadcastIntL4(i));
        }

        @Override
        public Vector<Integer, Shapes.S256Bit> generate(IntFunction<? extends Integer> generator) {
            throw new UnsupportedOperationException(); //IntFunction doesn't map to int lanes.
        }

        @Override
        public BiFunction<Integer[], Integer, Vector<Integer, Shapes.S256Bit>> fromArrayFactory() {
            return (Integer[] ary, Integer offset) -> {
                int[] tmp = new int[8];
                for(int i = 0; i < 8; i ++){
                    tmp[i] = ary[i];
                }
                return STUB.fromArray(tmp,0);
            };
        }

//        @Override
        public BiFunction<int[], Integer, Vector<Integer, Shapes.S256Bit>> fromIntArrayFactory() {
            return (int[] ints, Integer offset) -> new Int256Vector(PatchableVecUtils.long4FromIntArray(ints,offset));
        }

//        @Override
        public BiFunction<float[], Integer, Vector<Integer, Shapes.S256Bit>> fromFloatArrayFactory() {
            return (float[] floats, Integer offset) -> new Int256Vector(PatchableVecUtils.long4FromFloatArray(floats,offset));
        }

//        @Override
        public BiFunction<double[], Integer, Vector<Integer, Shapes.S256Bit>> fromDoubleArrayFactory() {
            return (double[] doubles, Integer offset) -> new Int256Vector(PatchableVecUtils.long4FromDoubleArray(doubles,offset));
        }

//        @Override
        public BiFunction<long[], Integer, Vector<Integer, Shapes.S256Bit>> fromLongArrayFactory() {
            return (long[] longs, Integer offset) -> new Int256Vector(PatchableVecUtils.long4FromLongArray(longs,offset));
        }

        @Override
        public Shuffle<Shapes.S256Bit> iota() {
            return iota(0, 1, Integer.MAX_VALUE);
        }

        @Override
        // arithmetic sequence ((B+i*S)%M)
        public Shuffle<Shapes.S256Bit> iota(int base, int step, int modulus) {
            int arr[] = IntStream.range(0, LENGTH)
                    .map((int i) -> (base + i * step) % modulus)
                    .toArray();
            return new Int256Vector.Shuffle256(arr);
        }
    }

    private static class Mask64Bit implements Mask<Shapes.S256Bit> {
        private final int LENGTH = 8;
        private boolean[] bits = new boolean[LENGTH];



        Mask64Bit(boolean[] bits) {
            this.bits = bits;
            for(int i =0 ; i< LENGTH;i++) {
                System.out.println("Array Element: " + bits[i]);
            }
        }

        @Override
        public int length() { return LENGTH; }

        @Override
        public long toLong() {
            long res = 0;
            for (int i = 0; i < LENGTH; i++) {
                res = (res << 1);
                if (bits[i]) {
                    res |= 1;
                }
            }
            return res;
        }

        @Override
        public BitSet toBitSet() {
            BitSet bs = new BitSet(8);
            for (int i = 0; i < LENGTH; i++) {
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
        public <E> Vector<E, Shapes.S256Bit> toVector(Class<E> type) {
            if (type == Integer.class) {
                Vector<Integer, Shapes.S256Bit> v = new Int256Vector(Long4.ZERO);
                for (int i = 0; i < LENGTH; i++) {
                    if (bits[i]) {
                        v = v.putElement(i, 1);
                    }
                }
                return (Vector<E, Shapes.S256Bit>) v;
            }
            throw new UnsupportedOperationException();
        }
    }

    private static class Shuffle256 implements Shuffle<Shapes.S256Bit> {

        final int[] shuf;

        private Long4 vec(){
            return PatchableVecUtils.long4FromIntArray(shuf,0);
        }

        public Shuffle256(){
            int[] nshuf = new int[8];
            for(int i = 0; i < nshuf.length; i++){
                nshuf[i] = i;
            }
            shuf = nshuf;
        }

        Shuffle256(int[] s){
            shuf = s;
        }

        public Shuffle256(Long4 vec){
            shuf = new int[8];
            PatchableVecUtils.long4ToIntArray(shuf,0,vec);
        }

        @Override
        public Long2 toLong2() {
            Long4 vec = vec();
            return PatchableVecUtils.vextractf128(0,vec);
        }

        @Override
        public Long4 toLong4() {
            return vec();
        }

        @Override
        public Long8 toLong8() {
            Long4 vec = vec();
            return Long8.make(vec.extract(0), vec.extract(1),
                    vec.extract(2), vec.extract(3), 0L, 0L, 0L, 0L);
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
        //TODO
        public Vector<Integer, Shapes.S256Bit> toVector() {
            return new Int256Vector(vec());
        }

        @Override
        @SuppressWarnings("unchecked")
        public <E> Vector<E, Shapes.S256Bit> toVector(Class<E> type) {
            if(type == Integer.class){
                int[] buffer = new int[8];
                for(int i = 0; i < shuf.length; i++){
                    buffer[i] = shuf[i];
                }
                return (Vector<E,Shapes.S256Bit>) new Int256Vector(buffer);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }
}
