/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
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
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.oracle.vector;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

final class VectorBytesLong2 {

    private VectorBytesLong2() { }

    static final SpeciesByteLong2 SPECIES = new SpeciesByteLong2();

    static final class SpeciesByteLong2 implements Vector.Species<Byte, Shapes.S128Bit> {
        static final Shapes.S128Bit shape = Shapes.SHAPE_128_BIT;

        @Override
        public Class<Byte> elementType() {
            return Byte.class;
        }

        @Override
        public int elementSize() {
            return Byte.SIZE;
        }

        @Override
        public Shapes.S128Bit shape() {
            return shape;
        }

        @Override
        public Byte zeroElement() {
            return 0;
        }

        private byte[] zeros() {
            return new byte[length()];
        }

        @Override
        public Vector<Byte, Shapes.S128Bit> zeroVector() {
            return new VectorByteLong2();
        }

        @Override
        public Vector<Byte, Shapes.S128Bit> generate(IntFunction<? extends Byte> generator) {
            byte[] v = zeros();
            for (int i = 0; i < v.length; i++)
                v[i] = generator.apply(i);
            return new VectorByteLong2(v);
        }

        @Override
        public Function<Byte, Vector<Byte, Shapes.S128Bit>> fromElementFactory() {
            return e -> {
                byte[] v = zeros();
                Arrays.fill(v, e);
                return new VectorByteLong2(v);
            };
        }

        @Override
        public BiFunction<Byte[], Integer, Vector<Byte, Shapes.S128Bit>> fromArrayFactory() {
            return (a, o) -> {
                if (o >= a.length) throw new IndexOutOfBoundsException();

                byte[] v = zeros();
                int l = Math.min(v.length, a.length - o);
                for (int i = 0; i < l; i++) {
                    v[i] = a[o + i];
                }
                return new VectorByteLong2(v);
            };
        }

        @Override
        public Vector.Shuffle<Shapes.S128Bit> iota() {
            byte[] indexes = new byte[Long2.BYTES];
            for (int i = 0; i < indexes.length; i++) {
                indexes[i] = (byte) i;
            }
            return new ShuffleByteLong2(indexes);
        }

        @Override
        public Vector.Shuffle<Shapes.S128Bit> iota(int base, int step, int modulus) {
            byte[] indexes = new byte[Long2.BYTES];
            for (int i = 0; i < length(); i++) {
                indexes[i] = (byte) ((base + i * step) % modulus);
            }
            return new ShuffleByteLong2(indexes);
        }
    }

    static class VectorByteLong2 implements Vector<Byte, Shapes.S128Bit> {
        final Long2 v;

        VectorByteLong2() {
            this.v = Long2.ZERO;
        }

        VectorByteLong2(Long2 v) {
            this.v = v;
        }

        VectorByteLong2(byte[] v) {
            this.v = Long2.make(Shapes.pack(v, 0), Shapes.pack(v, 8));
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
        public Species<Byte, Shapes.S128Bit> species() {
            return SPECIES;
        }

        @Override
        public Byte getElement(int index) {
            if (index < 0 || index > Long2.BYTES)
                throw new IndexOutOfBoundsException();

            Byte r = (byte) LowLevelVectorOps.pextrb(v, index);
            assert _getElement(index).equals(r) : r + " " + _getElement(index);
            return r;
        }
        private Byte _getElement(int index) {
            if (index < 0 || index > Long2.BYTES)
                throw new IndexOutOfBoundsException();

            long v = this.v.extract(index >> 3);
            int shift = (index % 8) << 3;
            return (byte) ((v >>> shift) & 0xFF);
        }

        @Override
        public Vector<Byte, Shapes.S128Bit> putElement(int index, Byte x) {
            if (index < 0 || index > Long2.BYTES)
                throw new IndexOutOfBoundsException();

            VectorByteLong2 r = new VectorByteLong2(LowLevelVectorOps.pinsrb(v, index, x));
            assert _putElement(index, x).equals(r) : r + " " + _putElement(index, x);
            return r;
        }
        private Vector<Byte, Shapes.S128Bit> _putElement(int index, Byte x) {
            if (index < 0 || index > Long2.BYTES)
                throw new IndexOutOfBoundsException();

            long v = this.v.extract(index >> 3);
            int shift = (index % 8) << 3;
            v &= ~(0xFFL << shift);
            v |= (x & 0xFFL) << shift;

            return new VectorByteLong2((index < 8)
                                  ? Long2.make(v, this.v.extract(1))
                                  : Long2.make(this.v.extract(0), v));
        }

        @Override
        public Vector<Byte, Shapes.S128Bit> compareEqual(Vector<Byte, Shapes.S128Bit> o) {
            VectorByteLong2 r = new VectorByteLong2(LowLevelVectorOps.cmpeqb(v, o.toLong2()));
            assert _compareEqual(o).equals(r) : r + " " + _compareEqual(o);
            return r;
        }
        private Vector<Byte, Shapes.S128Bit> _compareEqual(Vector<Byte, Shapes.S128Bit> o) {
            byte[] r = new byte[Long2.BYTES];
            for (int i = 0; i < Long2.BYTES; i++) {
                int a = this.getElement(i) & 0xFF;
                int b = o.getElement(i) & 0xFF;
                if (a == b)
                    r[i] = (byte) 0xFF;
            }
            return new VectorByteLong2(r);
        }

        @Override
        public Mask<Shapes.S128Bit> test(Predicate<Byte> op) {
            long mask = 0;
            for (int i = 0; i < Long2.BYTES; i++) {
                if (op.test(this.getElement(i))) {
                    mask |= 1L << i;
                }
            }

            return new VectorBytes.Mask64bit<>(SPECIES, mask);
        }

        @Override
        public Vector<Byte, Shapes.S128Bit> map(UnaryOperator<Byte> op) {
            byte[] r = new byte[Long2.BYTES];
            for (int i = 0; i < Long2.BYTES; i++) {
                r[i] = op.apply(this.getElement(i));
            }
            return new VectorByteLong2(r);
        }

        @Override
        public Vector<Byte, Shapes.S128Bit> mapWhere(Mask<Shapes.S128Bit> mask, UnaryOperator<Byte> op) {
            byte[] r = new byte[Long2.BYTES];
            long m = mask.toLong();
            for (int i = 0; i < Long2.BYTES; i++) {
                if ((m & (1L << i)) != 0)
                    r[i] = op.apply(this.getElement(i));
            }
            return new VectorByteLong2(r);
        }

        @Override
        public Vector<Byte, Shapes.S128Bit> map(BinaryOperator<Byte> op, Vector<Byte, Shapes.S128Bit> o) {
            byte[] r = new byte[Long2.BYTES];
            for (int i = 0; i < Long2.BYTES; i++) {
                r[i] = op.apply(this.getElement(i), o.getElement(i));
            }
            return new VectorByteLong2(r);
        }

        @Override
        public Vector<Byte, Shapes.S128Bit> mapWhere(Mask<Shapes.S128Bit> mask, BinaryOperator<Byte> op, Vector<Byte, Shapes.S128Bit> o) {
            byte[] r = new byte[Long2.BYTES];
            long m = mask.toLong();
            for (int i = 0; i < Long2.BYTES; i++) {
                if ((m & (1L << i)) != 0)
                    r[i] = op.apply(this.getElement(i), o.getElement(i));
            }
            return new VectorByteLong2(r);
        }

        @Override
        public Vector<Byte, Shapes.S128Bit> neg() {
            byte[] r = new byte[Long2.BYTES];
            for (int i = 0; i < length(); i++) {
                r[i] = (byte) -this.getElement(i);
            }
            return new VectorByteLong2(r);
        }

        @Override
        public boolean isZeros() {
            return v.extract(0) == 0 && v.extract(1) == 0;
        }

        @Override
        public Vector<Byte, Shapes.S128Bit> xor(Vector<Byte, Shapes.S128Bit> o) {
            VectorByteLong2 r = new VectorByteLong2(LowLevelVectorOps.xor(v, o.toLong2()));
            assert _xor(o).equals(r) : r + " " + _xor(o);
            return r;
        }
        private VectorByteLong2 _xor(Vector<Byte, Shapes.S128Bit> o) {
            byte[] r = new byte[Long2.BYTES];
            for (int i = 0; i < length(); i++) {
                int a = this.getElement(i) & 0xFF;
                int b = o.getElement(i) & 0xFF;
                r[i] = (byte) (a ^ b);
            }
            return new VectorByteLong2(r);
        }

        @Override
        public Vector<Byte, Shapes.S128Bit> add(Vector<Byte, Shapes.S128Bit> o) {
            VectorByteLong2 r = new VectorByteLong2(LowLevelVectorOps.paddb(v, o.toLong2()));
            assert _add(o).equals(r) : r + " " + _add(o);
            return r;
        }
        private Vector<Byte, Shapes.S128Bit> _add(Vector<Byte, Shapes.S128Bit> o) {
            byte[] r = new byte[Long2.BYTES];
            for (int i = 0; i < length(); i++) {
                int a = this.getElement(i) & 0xFF;
                int b = o.getElement(i) & 0xFF;
                r[i] = (byte) (a + b);
            }
            return new VectorByteLong2(r);
        }

        @Override
        public <T> T reduce(Function<Byte, T> mapper, BinaryOperator<T> op) {
            T v = mapper.apply(this.getElement(0));
            for (int i = 1; i < length(); i++) {
                v = op.apply(v, mapper.apply(this.getElement(i)));
            }
            return v;
        }

        @Override
        public Mask<Shapes.S128Bit> toMask() {
            VectorBytes.Mask64bit<Shapes.S128Bit> r = new VectorBytes.Mask64bit<>(
                    SPECIES, LowLevelVectorOps.pmovmskb(v));
            assert r.equals(_toMask()) : r + " " + _toMask();
            return r;
        }
        private Mask<Shapes.S128Bit> _toMask() {
            long mask = 0;
            for (int i = 0; i < Long2.BYTES; i++) {
                if ((this.getElement(i) & 0x80) != 0) {
                    mask |= 1L << i;
                }
            }

            return new VectorBytes.Mask64bit<>(SPECIES, mask);
        }

        @Override
        public Vector<Byte, Shapes.S128Bit> blend(Vector<Byte, Shapes.S128Bit> b, Vector<Byte, Shapes.S128Bit> mask) {
            VectorByteLong2 r = new VectorByteLong2(LowLevelVectorOps.pblendvb(mask.toLong2(), v, b.toLong2()));
            assert _blend(b, mask).equals(r) : r + " " + _blend(b, mask);
            return r;
        }
        private Vector<Byte, Shapes.S128Bit> _blend(Vector<Byte, Shapes.S128Bit> b, Vector<Byte, Shapes.S128Bit> mask) {
            Vector<Byte, Shapes.S128Bit> r = SPECIES.zeroVector();
            for (int i = 0; i < length(); i++) {
                boolean selectB = (mask.getElement(i) & 0x80) != 0;
                r = r.putElement(i, selectB ? b.getElement(i) : this.getElement(i));
            }
            return r;
        }

        @Override
        public Vector<Byte, Shapes.S128Bit> shuffle(Shuffle<Shapes.S128Bit> perm) {
            VectorByteLong2 r = new VectorByteLong2(LowLevelVectorOps.pshufb(v, perm.toLong2()));
            assert _shuffle(perm).equals(r) : r + " " + _shuffle(perm);
            return r;
        }
        private Vector<Byte, Shapes.S128Bit> _shuffle(Shuffle<Shapes.S128Bit> perm) {
            byte[] r = new byte[Long2.BYTES];
            int[] indexes = perm.toArray();
            for (int i = 0; i < length(); i++) {
                r[i] = this.getElement(indexes[i]);
            }
            return new VectorByteLong2(r);
        }

        @Override
        public Shuffle<Shapes.S128Bit> toShuffle() {
            return new ShuffleByteLong2(v);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof VectorByteLong2)) return false;

            VectorByteLong2 long2 = (VectorByteLong2) o;

            return v.equals(long2.v);

        }

        @Override
        public int hashCode() {
            return v.hashCode();
        }

        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            b.append(length()).append(":").append(bitSize());
            b.append(":<<");
            for (int i = 0; i < Long2.BYTES; i++) {
                if (i > 0)
                    b.append(", ");
                b.append(String.format("0x%02x", getElement(i) & 0xFF));
            }

            b.append(">>");
            return b.toString();
        }
    }

    static class ShuffleByteLong2 implements Vector.Shuffle<Shapes.S128Bit> {
        final VectorByteLong2 v;

        ShuffleByteLong2(byte[] v) {
            this.v = new VectorByteLong2(v);
        }

        public ShuffleByteLong2(Long2 v) {
            this.v = new VectorByteLong2(v);
        }

        @Override
        public Long2 toLong2() {
            return v.v;
        }

        @Override
        public Long4 toLong4() {
            return v.toLong4();
        }

        @Override
        public Long8 toLong8() {
            return v.toLong8();
        }

        @Override
        public int length() {
            return SPECIES.length();
        }

        @Override
        public int[] toArray() {
            int[] indexes = new int[Long2.BYTES];
            for (int i = 0; i < length(); i++) {
                indexes[i] = v.getElement(i);
            }
            return indexes;
        }

        @Override
        public Vector<Integer, Shapes.S128Bit> toVector() {
            // @@@ Is this needed?
            throw new UnsupportedOperationException();
        }

        @Override
        public <E> Vector<E, Shapes.S128Bit> toVector(Class<E> type) {
            /*
            @@@ Consider moving this method to a factory method on species
             */
            throw new UnsupportedOperationException();
        }

        @Override
        public int getElement(int i) {
            return v.getElement(i);
        }

        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            b.append(length()).append(":").append(SPECIES.bitSize());
            b.append(":[[");
            for (int i = 0; i < Long2.BYTES; i++) {
                if (i > 0)
                    b.append(", ");
                b.append(String.format("0x%02x", v.getElement(i) & 0xFF));
            }

            b.append("]]");
            return b.toString();
        }
    }

}
