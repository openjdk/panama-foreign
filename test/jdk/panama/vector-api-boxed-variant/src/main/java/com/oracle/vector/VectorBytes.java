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
import java.util.BitSet;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;


final class VectorBytes {

    private VectorBytes() { }

    /**
     * Returns a byte-based species for a given compatible shape.
     */
    static <S extends Vector.Shape<Vector<?, S>>> Vector.Species<Byte, S> species(S shape) {
        if (shape.bitSize() == 128) {
            Vector.Species s = new VectorBytesLong2.SpeciesByteLong2();
            return (Vector.Species<Byte, S>) s;
        }
        return new SpeciesByte<>(shape);
    }

    /**
     * Generic byte-based species implementation for a given compatible shape.
     */
    static class SpeciesByte<S extends Vector.Shape<Vector<?, S>>> implements Vector.Species<Byte, S> {
        final S shape;

        SpeciesByte(S shape) {
            this.shape = shape;
        }

        @Override
        public Class<Byte> elementType() {
            return Byte.class;
        }

        @Override
        public int elementSize() {
            return Byte.SIZE;
        }

        @Override
        public S shape() {
            return shape;
        }

        @Override
        public Byte zeroElement() {
            return 0;
        }

        private Byte[] zeros() {
            Byte[] zeros = new Byte[length()];
            Arrays.fill(zeros, zeroElement());
            return zeros;
        }

        @Override
        public Vector<Byte, S> generate(IntFunction<? extends Byte> generator) {
            Byte[] v = zeros();
            Arrays.setAll(v, generator);
            return new VectorByte<>(this, v);
        }

        @Override
        public Function<Byte, Vector<Byte, S>> fromElementFactory() {
            return e -> {
                Byte[] v = zeros();
                Arrays.fill(v, e);
                return new VectorByte<>(this, v);
            };
        }

        @Override
        public BiFunction<Byte[], Integer, Vector<Byte, S>> fromArrayFactory() {
            return (a, o) -> {
                if (o >= a.length) throw new IndexOutOfBoundsException();

                Byte[] v = zeros();
                int l = Math.min(v.length, a.length - o);
                for (int i = 0; i < l; i++) {
                    v[i] = a[o + i];
                }
                return new VectorByte<>(this, v);
            };
        }

        @Override
        public Vector.Shuffle<S> iota() {
            int[] indexes = new int[length()];
            Arrays.setAll(indexes, i -> i);
            return new ShuffleImpl<>(this, indexes);
        }

        @Override
        public Vector.Shuffle<S> iota(int base, int step, int modulus) {
            int[] indexes = new int[length()];
            for (int i = 0; i < length(); i++) {
                indexes[i] = (base + i * step) % modulus;
            }
            return new ShuffleImpl<>(this, indexes);
        }
    }

    static class Mask64bit<S extends Vector.Shape<Vector<?, S>>> implements Vector.Mask<S> {
        final Vector.Species<Byte, S> species;

        final long mask;

        public Mask64bit(Vector.Species<Byte, S> species, long mask) {
            assert species.length() <= 64;

            this.species = species;
            this.mask = mask;
        }

        @Override
        public int length() {
            return species.length();
        }

        @Override
        public long toLong() {
            return mask;
        }

        @Override
        public boolean[] toArray() {
            boolean[] ba = new boolean[length()];
            for (int i = 0; i < length(); i++) {
                if ((mask & (1L << i)) != 0)
                    ba[i] = true;
            }
            return ba;
        }

        @Override
        public BitSet toBitSet() {
            return BitSet.valueOf(new long[]{mask});
        }

        @Override
        public <E> Vector<E, S> toVector(Class<E> type) {
            /*
            @@@ Consider moving this method to a factory method on species
             */
            if (type == Byte.class) {
                Vector<Byte, S> br = species.zeroVector();
                for (int i = 0; i < length(); i++) {
                    if ((mask & (1L << i)) != 0)
                        br = br.putElement(i, (byte) 0xFF);
                }
                @SuppressWarnings("unchecked")
                Vector<E, S> r = (Vector<E, S>) br;
                return r;
            }
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Mask64bit)) return false;

            Mask64bit<?> bit = (Mask64bit<?>) o;

            if (mask != bit.mask) return false;
            return species != null ? species.equals(bit.species) : bit.species == null;
        }

        @Override
        public int hashCode() {
            int result = species != null ? species.hashCode() : 0;
            result = 31 * result + (int) (mask ^ (mask >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return String.format("Mask {%d: 0x%08x | 0x%08x :0}",
                                 length(), mask >>> 32, mask & 0xFFFFFFFFL);
        }
    }

    static class ShuffleImpl<S extends Vector.Shape<Vector<?, S>>> implements Vector.Shuffle<S> {
        final Vector.Species<Byte, S> species;

        final int[] indexes;

        public ShuffleImpl(Vector.Species<Byte, S> species, int[] indexes) {
            this.species = species;
            this.indexes = indexes;
        }

        @Override
        public Long2 toLong2() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Long4 toLong4() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Long8 toLong8() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int length() {
            return species.length();
        }

        @Override
        public int[] toArray() {
            return indexes.clone();
        }

        @Override
        public Vector<Integer, S> toVector() {
            // @@@ Is this needed?
            throw new UnsupportedOperationException();
        }

        @Override
        public <E> Vector<E, S> toVector(Class<E> type) {
            /*
            @@@ Consider moving this method to a factory method on species
             */
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Generic byte-based vector implementation for any byte-based species.
     *
     * Specific shapes could be optimized with specific implementations, for
     * example
     * 1) acting as a view over a source Byte[] array, thus avoiding copying,
     * assuming the source is not modified during vector operation
     * 2) an alternative layout more suited to value types, such as using
     * multiple long fields or Long{2, 3, 8} etc.
     */
    static class VectorByte<S extends Vector.Shape<Vector<?, S>>> implements Vector<Byte, S> {
        final Species<Byte, S> species;

        final Byte[] v;

        VectorByte(Species<Byte, S> species, Byte[] v) {
            this.species = species;
            this.v = v;
        }

        @Override
        public Long2 toLong2() {
            return Long2.make(Shapes.pack(v, 0), Shapes.pack(v, 8));
        }

        @Override
        public Long4 toLong4() {
            Long2 v = toLong2();
            return Long4.make(v.extract(0), v.extract(0), 0, 0);
        }

        @Override
        public Long8 toLong8() {
            Long2 v = toLong2();
            return Long8.make(v.extract(0), v.extract(0), 0, 0, 0, 0, 0, 0);
        }

        @Override
        public Species<Byte, S> species() {
            return species;
        }

        @Override
        public Byte getElement(int index) {
            if (index < 0 || index > v.length)
                throw new IndexOutOfBoundsException();

            return v[index];
        }

        @Override
        public Vector<Byte, S> putElement(int index, Byte x) {
            if (index < 0 || index > v.length)
                throw new IndexOutOfBoundsException();

            v[index] = x;
            return this;
        }

        @Override
        public Vector<Byte, S> compareEqual(Vector<Byte, S> o) {
            Vector<Byte, S> r = species.zeroVector();
            for (int i = 0; i < v.length; i++) {
                int a = this.getElement(i) & 0xFF;
                int b = o.getElement(i) & 0xFF;
                if (a == b)
                    r.putElement(i, (byte) 0xFF);
            }
            return r;
        }

        @Override
        public Mask<S> test(Predicate<Byte> op) {
            long mask = 0;
            for (int i = 0; i < v.length; i++) {
                if (op.test(this.getElement(i))) {
                    mask |= 1L << i;
                }
            }

            return new Mask64bit<>(species, mask);
        }

        @Override
        public Vector<Byte, S> map(UnaryOperator<Byte> op) {
            Vector<Byte, S> r = species.zeroVector();
            for (int i = 0; i < v.length; i++) {
                r.putElement(i, op.apply(this.getElement(i)));
            }
            return r;
        }

        @Override
        public Vector<Byte, S> mapWhere(Mask<S> mask, UnaryOperator<Byte> op) {
            Vector<Byte, S> r = species.zeroVector();
            long m = mask.toLong();
            for (int i = 0; i < v.length; i++) {
                if ((m & (1L << i)) != 0)
                    r.putElement(i, op.apply(this.getElement(i)));
            }
            return r;
        }

        @Override
        public Vector<Byte, S> map(BinaryOperator<Byte> op, Vector<Byte, S> o) {
            Vector<Byte, S> r = species.zeroVector();
            for (int i = 0; i < v.length; i++) {
                r.putElement(i, op.apply(this.getElement(i), o.getElement(i)));
            }
            return r;
        }

        @Override
        public Vector<Byte, S> mapWhere(Mask<S> mask, BinaryOperator<Byte> op, Vector<Byte, S> o) {
            Vector<Byte, S> r = species.zeroVector();
            long m = mask.toLong();
            for (int i = 0; i < v.length; i++) {
                if ((m & (1L << i)) != 0)
                    r.putElement(i, op.apply(this.getElement(i), o.getElement(i)));
            }
            return r;
        }

        @Override
        public Vector<Byte, S> neg() {
            Vector<Byte, S> r = species.zeroVector();
            for (int i = 0; i < length(); i++) {
                r.putElement(i, (byte) -this.getElement(i));
            }
            return r;
        }

        @Override
        public boolean isZeros() {
            for (int i = 0; i < length(); i++) {
                int a = this.getElement(i) & 0xFF;
                if (a != 0) return false;
            }
            return true;
        }

        @Override
        public Vector<Byte, S> xor(Vector<Byte, S> o) {
            Vector<Byte, S> r = species.zeroVector();
            for (int i = 0; i < length(); i++) {
                int a = this.getElement(i) & 0xFF;
                int b = o.getElement(i) & 0xFF;
                r.putElement(i, (byte) (a ^ b));
            }
            return r;
        }

        @Override
        public Vector<Byte, S> add(Vector<Byte, S> o) {
            Vector<Byte, S> r = species.zeroVector();
            for (int i = 0; i < length(); i++) {
                int a = this.getElement(i) & 0xFF;
                int b = o.getElement(i) & 0xFF;
                r.putElement(i, (byte) (a + b));
            }
            return r;
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
        public Mask<S> toMask() {
            long mask = 0;
            for (int i = 0; i < v.length; i++) {
                if ((this.getElement(i) & 0x80) != 0) {
                    mask |= 1L << i;
                }
            }

            return new Mask64bit<>(species, mask);
        }

        @Override
        public Vector<Byte, S> blend(Vector<Byte, S> b, Vector<Byte, S> mask) {
            Vector<Byte, S> r = species.zeroVector();
            for (int i = 0; i < v.length; i++) {
                boolean selectB = (mask.getElement(i) & 0x80) != 0;
                r.putElement(i, selectB ? b.getElement(i) : this.getElement(i));
            }
            return r;
        }

        @Override
        public Vector<Byte, S> shuffle(Shuffle<S> perm) {
            Vector<Byte, S> r = species.zeroVector();
            int[] indexes = perm.toArray();
            for (int i = 0; i < length(); i++) {
                r.putElement(i, this.getElement(indexes[i]));
            }
            return r;
        }

        @Override
        public Shuffle<S> toShuffle() {
            int[] indexes = new int[length()];
            for (int i = 0; i < length(); i++) {
                indexes[i] = this.getElement(i);
            }
            return new ShuffleImpl<>(species, indexes);
        }

        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            b.append(length()).append(":").append(bitSize());
            b.append(":<<");
            for (int i = 0; i < v.length; i++) {
                if (i > 0)
                    b.append(", ");
                b.append(v[i] & 0xFF);
            }

            b.append(">>");
            return b.toString();
        }
    }
}
