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

class Long256Vector extends LongVector<Shapes.S256Bit> {

    private static final int LENGTH = 4;

    //Singleton Stub for static invocations of instance methods.
    private static final Long256Vector STUB = new Long256Vector(Long4.ZERO);
    private static final Species<Long, Shapes.S256Bit> SPECIES = new Long256Vector.Long256VectorSpecies();

    //Constants for zero comparisons
    private static final long[] pzArray = {0, 0, 0, 0};
    private static final Long256Vector POSZERO = (Long256Vector) STUB.fromArray(pzArray, 0);

    @Stable
    private final Long4 vec;

    Long256Vector(Long4 vec) {
        this.vec = vec;
    }

    private Long256Vector(long[] ary) {
        this.vec = PatchableVecUtils.long4FromLongArray(ary, 0);
    }

    @Override
    public Vector<Long, Shapes.S256Bit> fromArray(long[] ary, int offset) {
        return new Long256Vector(PatchableVecUtils.long4FromLongArray(ary, offset));
    }

    @Override
    @ForceInline
    public void intoArray(long[] ary, int offset) {
        PatchableVecUtils.long4ToLongArray(ary, offset, this.vec);
    }

    // TODO : Returns only the first 128 bits. getLowLong2, getHighLong2
    @Override
    public Long2 toLong2() {
        return PatchableVecUtils.vextractf128(0, this.vec);
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
    public Species<Long, Shapes.S256Bit> species() {
        return SPECIES;
    }

    @Override
    public Long getElement(int index) {
        if (index >= this.length()) throw new IllegalArgumentException("getElement index must be 0-4 inclusive");
        Long2 l2 = (index < 2) ? vextractf128(0, this.vec) : vextractf128(1, this.vec);
        return PatchableVecUtils.vpextrq(index & 01, l2);
    }

    @Override
    public Vector<Long, Shapes.S256Bit> putElement(int index, Long x) {
        if (index >= this.length()) throw new IllegalArgumentException("putElement index must be 0-4 inclusive");
        long[] arr = this.toArray();
        arr[index] = x;
        return this.fromArray(arr, 0);
    }

//    @Override
    public void intoArray(byte[] a, int o) {
        PatchableVecUtils.long4ToByteArray(a,0,this.vec);
    }

    @Override
    public Vector<Integer, Shapes.S256Bit> compareEqual(Vector<Long, Shapes.S256Bit> v) {
        Long256Vector v2 = (Long256Vector) v;
        return new Int256Vector(PatchableVecUtils.vpcmpeqq(this.vec, v2.toLong4()));
    }

    @Override
    public Vector<Integer, Shapes.S256Bit> lessThan(Vector<Long, Shapes.S256Bit> v) {
        Long256Vector v2 = (Long256Vector) v;
        return new Int256Vector(PatchableVecUtils.vpcmpgtq(v2.toLong4(), this.vec));
    }

    @Override
    public Vector<Integer, Shapes.S256Bit> greaterThan(Vector<Long, Shapes.S256Bit> v) {
        Long256Vector v2 = (Long256Vector) v;
        return new Int256Vector(PatchableVecUtils.vpcmpgtq(this.vec, v2.toLong4()));
    }

    @Override
    public Mask<Shapes.S256Bit> test(Predicate<Long> op) {
        boolean[] ary = new boolean[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = op.test(this.getElement(i));
        }
        return new Mask64Bit(ary);
    }

    @Override
    public Vector<Long, Shapes.S256Bit> map(UnaryOperator<Long> op) {
        long[] ary = new long[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = op.apply(this.getElement(i));
        }
        return new Long256Vector(ary);
    }

    @Override
    public Vector<Long, Shapes.S256Bit> mapWhere(Mask<Shapes.S256Bit> mask, UnaryOperator<Long> op) {
        long[] ary = new long[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = mask.getElement(i) ? op.apply(this.getElement(i)) : this.getElement(i);
        }
        return new Long256Vector(ary);
    }

    @Override
    public Vector<Long, Shapes.S256Bit> map(BinaryOperator<Long> op, Vector<Long, Shapes.S256Bit> this2) {
        Long256Vector this2c = (Long256Vector) this2;
        long[] ary = new long[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = op.apply(this.getElement(i), this2c.getElement(i));
        }
        return new Long256Vector(ary);
    }

    @Override
    public Vector<Long, Shapes.S256Bit> mapWhere(Mask<Shapes.S256Bit> mask, BinaryOperator<Long> op, Vector<Long, Shapes.S256Bit> this2) {
        Long256Vector this2c = (Long256Vector) this2;
        long[] ary = new long[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = mask.getElement(i) ? op.apply(this.getElement(i), this2c.getElement(i)) : this.getElement(i);
        }
        return new Long256Vector(ary);
    }

    @Override
    public boolean isZeros() {
        return this.vec.equals(POSZERO.toLong4());
    }

    // TODO
    @Override
    public Vector<Long, Shapes.S256Bit> neg() {
        throw new UnsupportedOperationException();
    }

//    @Override
    public Vector<Long, Shapes.S256Bit> and(Vector<Long, Shapes.S256Bit> o) {
        Long256Vector oc = (Long256Vector) o;
        return new Long256Vector(PatchableVecUtils.vpand(this.vec,oc.toLong4()));
    }

    @Override
    public Vector<Long, Shapes.S256Bit> xor(Vector<Long, Shapes.S256Bit> o) {
        Long256Vector oc = (Long256Vector) o;
        return new Long256Vector(PatchableVecUtils.vpxor(this.vec, oc.toLong4()));
    }

    // TODO
//    @Override
    public Vector<Long, Shapes.S256Bit> sqrt() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Vector<Long, Shapes.S256Bit> add(Vector<Long, Shapes.S256Bit> o) {
        Long256Vector oc = (Long256Vector) o;
        return new Long256Vector(PatchableVecUtils.vpaddq(this.vec, oc.toLong4()));
    }

//    @Override
    public Vector<Long, Shapes.S256Bit> sub(Vector<Long, Shapes.S256Bit> o) {
        Long256Vector oc = (Long256Vector) o;
        return new Long256Vector(PatchableVecUtils.vpsubq(this.vec,oc.toLong4()));
    }

    // TODO
    @Override
    public Vector<Long, Shapes.S256Bit> mul(Vector<Long, Shapes.S256Bit> o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T reduce(Function<Long, T> mapper, BinaryOperator<T> op) {
        T accumulator = mapper.apply(this.getElement(0));
        for (int i = 1; i < this.length(); i++) {
            accumulator = op.apply(accumulator, mapper.apply(this.getElement(i)));
        }
        return accumulator;
    }

    @Override
    public Mask<Shapes.S256Bit> toMask() {
        boolean[] ary = new boolean[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = this.getElement(i) != 0f;
        }
        return new Mask64Bit(ary);
    }

    @Override
    public Long sumAll() {
        long[] res = this.toArray();
        long sum = 0;
        for (int i = 0; i < LENGTH; i++) {
            sum += res[i];
        }
        return sum;
    }

    @Override
    public Vector<Long, Shapes.S256Bit> blend(Vector<Long, Shapes.S256Bit> b, Vector<Long, Shapes.S256Bit> mask) {
        Long256Vector bc = (Long256Vector) b;
        long[] ary = new long[this.length()];
        for (int i = 0; i < this.length(); i++) {
            ary[i] = mask.getElement(i) == 0 ? this.getElement(i) : bc.getElement(i);
        }
        return new Long256Vector(ary);
    }

    @Override
    public Vector<Long, Shapes.S256Bit> shuffle(Shuffle<Shapes.S256Bit> perm) {
        int[] p = perm.toArray();
        long[] buf = new long[this.length()];
        for (int i = 0; i < this.length(); i++) {
            buf[p[i]] = this.getElement(i);
        }
        return new Long256Vector(buf);
    }

    @Override
    public Shuffle<Shapes.S256Bit> toShuffle() {
        return new Shuffle256(this.vec);
    }

    // TODO
//    @Override
    public Vector<Float, Shapes.S256Bit> toFloatVector() {
        throw new UnsupportedOperationException("Double Conversions Not Supported");
    }

    // TODO
//    @Override
    public Vector<Integer, Shapes.S256Bit> toIntVector() {
        throw new UnsupportedOperationException("Double Conversions Not Supported");
    }

    // TODO
//    @Override
    public Vector<Double, Shapes.S256Bit> toDoubleVector() {
        throw new UnsupportedOperationException("Double Conversions Not Supported");
    }

//    @Override
    public Vector<Float, Shapes.S256Bit> toFloatVectorBits() {
        return new Float256Vector(this.vec);
    }

//    @Override
    public Vector<Integer, Shapes.S256Bit> toIntVectorBits() {
        return new Int256Vector(this.vec);
    }

//    @Override
    public Vector<Double, Shapes.S256Bit> toDoubleVectorBits() {
        return new Double256Vector(this.vec);
    }

    private static class Long256VectorSpecies implements Species<Long, Shapes.S256Bit> {

        @Override
        public Shapes.S256Bit shape() {
            return Shapes.SHAPE_256_BIT;
        }

        @Override
        public Class<Long> elementType() {
            return Long.class;
        }

        @Override
        public int elementSize() {
            return 4;
        }

        @Override
        public Function<Long, Vector<Long, Shapes.S256Bit>> fromElementFactory() {
            return (Long l) -> new Long256Vector(PatchableVecUtils.broadcastLongL4(l));
        }

        @Override
        public Vector<Long, Shapes.S256Bit> generate(IntFunction<? extends Long> generator) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BiFunction<Long[], Integer, Vector<Long, Shapes.S256Bit>> fromArrayFactory() {
            return (Long[] ary, Integer offset) -> {
                long[] tmp = new long[this.length()];
                for (int i = 0; i < this.length(); i++) {
                    tmp[i] = ary[i];
                }
                return STUB.fromArray(tmp, 0);
            };
        }

//        @Override
        public BiFunction<int[], Integer, Vector<Long, Shapes.S256Bit>> fromIntArrayFactory() {
            return (int[] ints, Integer offset) -> new Long256Vector(PatchableVecUtils.long4FromIntArray(ints,offset));
        }

//        @Override
        public BiFunction<float[], Integer, Vector<Long, Shapes.S256Bit>> fromFloatArrayFactory() {
            return (float[] floats, Integer offset) -> new Long256Vector(PatchableVecUtils.long4FromFloatArray(floats,offset));
        }

//        @Override
        public BiFunction<double[], Integer, Vector<Long, Shapes.S256Bit>> fromDoubleArrayFactory() {
            return (double[] doubles, Integer offset) -> new Long256Vector(PatchableVecUtils.long4FromDoubleArray(doubles,offset));
        }

//        @Override
        public BiFunction<long[], Integer, Vector<Long, Shapes.S256Bit>> fromLongArrayFactory() {
            return (long[] longs, Integer offset) -> new Long256Vector(PatchableVecUtils.long4FromLongArray(longs,offset));
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
            return new Shuffle256(arr);
        }

    }

    private static class Mask64Bit implements Mask<Shapes.S256Bit> {
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
        public <E> Vector<E, Shapes.S256Bit> toVector(Class<E> type) {
            if (type == Long.class) {
                Vector<Long, Shapes.S256Bit> v = new Long256Vector(Long4.ZERO);
                for (int i = 0; i < this.length(); i++) {
                    if (bits[i]) {
                        v = v.putElement(i, (long)1);  //NOTE: What does it mean for a float to be masked here?
                    }
                }
                return (Vector<E, Shapes.S256Bit>) v;

            }
            throw new UnsupportedOperationException();
        }
    }

    private static class Shuffle256 implements Shuffle<Shapes.S256Bit> {

        final int[] shuf;

        private Long4 vec() {
            return PatchableVecUtils.long4FromIntArray(shuf, 0);
        }

        public Shuffle256() {
            int[] nshuf = new int[this.length()];
            for (int i = 0; i < nshuf.length; i++) {
                nshuf[i] = i;
            }
            shuf = nshuf;
        }

        Shuffle256(int[] s) {
            shuf = s;
        }

        public Shuffle256(Long4 vec) {
            shuf = new int[this.length()];
            PatchableVecUtils.long4ToIntArray(shuf, 0, vec);
        }

        @Override
        public Long2 toLong2() {
            Long4 vec = vec();
            return PatchableVecUtils.vextractf128(0, vec);
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
        public Vector<Integer, Shapes.S256Bit> toVector() {
            return new Int256Vector(vec());
        }

        @Override
        @SuppressWarnings("unchecked")
        public <E> Vector<E, Shapes.S256Bit> toVector(Class<E> type) {
            if (type == Long.class) {
                long[] buffer = new long[this.length()];
                for (int i = 0; i < shuf.length; i++) {
                    buffer[i] = (long) shuf[i];
                }
                return (Vector<E, Shapes.S256Bit>) new Long256Vector(buffer);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }
}
