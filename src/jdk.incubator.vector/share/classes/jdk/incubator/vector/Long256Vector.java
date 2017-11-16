/*
 * Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
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
import java.util.Arrays;

@SuppressWarnings("cast")
final class Long256Vector extends LongVector<Shapes.S256Bit> {
    static final Long256Species SPECIES = new Long256Species();

    static final Long256Vector ZERO = new Long256Vector();

    long[] vec;

    Long256Vector() {
        vec = new long[SPECIES.length()];
    }

    Long256Vector(long[] v) {
        vec = v;
    }


    // Unary operator

    @Override
    Long256Vector uOp(FUnOp f) {
        long[] res = new long[length()];
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i]);
        }
        return new Long256Vector(res);
    }

    @Override
    Long256Vector uOp(Mask<Long, Shapes.S256Bit> o, FUnOp f) {
        long[] res = new long[length()];
        Long256Mask m = (Long256Mask) o;
        for (int i = 0; i < length(); i++) {
            res[i] = m.bits[i] ? f.apply(i, vec[i]) : vec[i];
        }
        return new Long256Vector(res);
    }

    // Binary operator

    @Override
    Long256Vector bOp(Vector<Long, Shapes.S256Bit> o, FBinOp f) {
        long[] res = new long[length()];
        Long256Vector v = (Long256Vector) o;
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i], v.vec[i]);
        }
        return new Long256Vector(res);
    }

    @Override
    Long256Vector bOp(Vector<Long, Shapes.S256Bit> o1, Mask<Long, Shapes.S256Bit> o2, FBinOp f) {
        long[] res = new long[length()];
        Long256Vector v = (Long256Vector) o1;
        Long256Mask m = (Long256Mask) o2;
        for (int i = 0; i < length(); i++) {
            res[i] = m.bits[i] ? f.apply(i, vec[i], v.vec[i]) : vec[i];
        }
        return new Long256Vector(res);
    }

    // Trinary operator

    @Override
    Long256Vector tOp(Vector<Long, Shapes.S256Bit> o1, Vector<Long, Shapes.S256Bit> o2, FTriOp f) {
        long[] res = new long[length()];
        Long256Vector v1 = (Long256Vector) o1;
        Long256Vector v2 = (Long256Vector) o2;
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i], v1.vec[i], v2.vec[i]);
        }
        return new Long256Vector(res);
    }

    @Override
    Long256Vector tOp(Vector<Long, Shapes.S256Bit> o1, Vector<Long, Shapes.S256Bit> o2, Mask<Long, Shapes.S256Bit> o3, FTriOp f) {
        long[] res = new long[length()];
        Long256Vector v1 = (Long256Vector) o1;
        Long256Vector v2 = (Long256Vector) o2;
        Long256Mask m = (Long256Mask) o3;
        for (int i = 0; i < length(); i++) {
            res[i] = m.bits[i] ? f.apply(i, vec[i], v1.vec[i], v2.vec[i]) : vec[i];
        }
        return new Long256Vector(res);
    }

    @Override
    long rOp(long v, FBinOp f) {
        for (int i = 0; i < length(); i++) {
            v = f.apply(i, v, vec[i]);
        }
        return v;
    }

    //

    @Override
    public String toString() {
        return Arrays.toString(vec);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        Long256Vector that = (Long256Vector) o;
        return Arrays.equals(vec, that.vec);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    Long256Mask bTest(Vector<Long, Shapes.S256Bit> o, FBinTest f) {
        Long256Vector v = (Long256Vector) o;
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(i, vec[i], v.vec[i]);
        }
        return new Long256Mask(bits);
    }

    // Foreach

    @Override
    void forEach(FUnCon f) {
        for (int i = 0; i < length(); i++) {
            f.apply(i, vec[i]);
        }
    }

    @Override
    void forEach(Mask<Long, Shapes.S256Bit> o, FUnCon f) {
        Long256Mask m = (Long256Mask) o;
        forEach((i, a) -> {
            if (m.bits[i]) { f.apply(i, a); }
        });
    }


    Double256Vector toFP() {
        double[] res = new double[this.species().length()];
        for(int i = 0; i < this.species().length(); i++){
            res[i] = Double.longBitsToDouble(vec[i]);
        }
        return new Double256Vector(res);
    }

    @Override
    public Long256Vector rotateEL(int j) {
        long[] res = new long[length()];
        for (int i = 0; i < length(); i++){
            res[j + i % length()] = vec[i];
        }
        return new Long256Vector(res);
    }

    @Override
    public Long256Vector rotateER(int j) {
        long[] res = new long[length()];
        for (int i = 0; i < length(); i++){
            int z = i - j;
            if(j < 0) {
                res[length() + z] = vec[i];
            } else {
                res[z] = vec[i];
            }
        }
        return new Long256Vector(res);
    }

    @Override
    public Long256Vector shiftEL(int j) {
        long[] res = new long[length()];
        for (int i = 0; i < length() - j; i++) {
            res[i] = vec[i + j];
        }
        return new Long256Vector(res);
    }

    @Override
    public Long256Vector shiftER(int j) {
        long[] res = new long[length()];
        for (int i = 0; i < length() - j; i++){
            res[i + j] = vec[i];
        }
        return new Long256Vector(res);
    }

    @Override
    public Long256Vector shuffle(Vector<Long, Shapes.S256Bit> o, Shuffle<Long, Shapes.S256Bit> s) {
        Long256Vector v = (Long256Vector) o;
        return uOp((i, a) -> {
            int e = s.getElement(i);
            if(e >= 0 && e < length()) {
                //from this
                return vec[e];
            } else if(e < length() * 2) {
                //from o
                return v.vec[e - length()];
            } else {
                throw new ArrayIndexOutOfBoundsException("Bad reordering for shuffle");
            }
        });
    }

    @Override
    public Long256Vector swizzle(Shuffle<Long, Shapes.S256Bit> s) {
        return uOp((i, a) -> {
            int e = s.getElement(i);
            if(e >= 0 && e < length()) {
                return vec[e];
            } else {
                throw new ArrayIndexOutOfBoundsException("Bad reordering for shuffle");
            }
        });
    }

    @Override
    public <F, Z extends Shape<Vector<?, ?>>> Vector<F, Z> cast(Class<F> type, Z shape) {
        Vector.Species<F,Z> species = Vector.speciesInstance(type, shape);

        // Whichever is larger
        int blen = Math.max(species.bitSize(), bitSize()) / Byte.SIZE;
        ByteBuffer bb = ByteBuffer.allocate(blen);

        int limit = Math.min(species.length(), length());

        if (type == Byte.class) {
            for (int i = 0; i < limit; i++){
                bb.put(i, (byte) vec[i]);
            }
        } else if (type == Short.class) {
            for (int i = 0; i < limit; i++){
                bb.asShortBuffer().put(i, (short) vec[i]);
            }
        } else if (type == Integer.class) {
            for (int i = 0; i < limit; i++){
                bb.asIntBuffer().put(i, (int) vec[i]);
            }
        } else if (type == Long.class){
            for (int i = 0; i < limit; i++){
                bb.asLongBuffer().put(i, (long) vec[i]);
            }
        } else if (type == Float.class){
            for (int i = 0; i < limit; i++){
                bb.asFloatBuffer().put(i, (float) vec[i]);
            }
        } else if (type == Double.class){
            for (int i = 0; i < limit; i++){
                bb.asDoubleBuffer().put(i, (double) vec[i]);
            }
        } else {
            throw new UnsupportedOperationException("Bad lane type for casting.");
        }

        return species.fromByteBuffer(bb);
    }

    // Accessors

    @Override
    public long get(int i) {
        return vec[i];
    }

    @Override
    public Long256Vector with(int i, long e) {
        long[] res = vec.clone();
        res[i] = e;
        return new Long256Vector(res);
    }

    // Mask

    static final class Long256Mask extends AbstractMask<Long, Shapes.S256Bit> {
        static final Long256Mask TRUE_MASK = new Long256Mask(true);
        static final Long256Mask FALSE_MASK = new Long256Mask(false);

        public Long256Mask(boolean[] bits) {
            super(bits);
        }

        public Long256Mask(boolean val) {
            super(val);
        }

        @Override
        Long256Mask uOp(MUnOp f) {
            boolean[] res = new boolean[species().length()];
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Long256Mask(res);
        }

        @Override
        Long256Mask bOp(Mask<Long, Shapes.S256Bit> o, MBinOp f) {
            boolean[] res = new boolean[species().length()];
            Long256Mask m = (Long256Mask) o;
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i], m.bits[i]);
            }
            return new Long256Mask(res);
        }

        @Override
        public Long256Species species() {
            return SPECIES;
        }

        @Override
        public Long256Vector toVector() {
            long[] res = new long[species().length()];
            for (int i = 0; i < species().length(); i++) {
                res[i] = (long) (bits[i] ? -1 : 0);
            }
            return new Long256Vector(res);
        }
    }

    // Species

    @Override
    public Long256Species species() {
        return SPECIES;
    }

    static final class Long256Species extends LongSpecies<Shapes.S256Bit> {
        static final int BIT_SIZE = Shapes.S_256_BIT.bitSize();

        static final int LENGTH = BIT_SIZE / Long.SIZE;

        @Override
        public String toString() {
           StringBuilder sb = new StringBuilder("Shape[");
           sb.append(bitSize()).append(" bits, ");
           sb.append(length()).append(" ").append(long.class.getSimpleName()).append("s x ");
           sb.append(elementSize()).append(" bits");
           sb.append("]");
           return sb.toString();
        }

        @Override
        public int bitSize() {
            return BIT_SIZE;
        }

        @Override
        public int length() {
            return LENGTH;
        }

        @Override
        public Class<Long> elementType() {
            return Long.class;
        }

        @Override
        public int elementSize() {
            return Long.SIZE;
        }

        @Override
        public Shapes.S256Bit shape() {
            return Shapes.S_256_BIT;
        }

        @Override
        Long256Vector op(FOp f) {
            long[] res = new long[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return new Long256Vector(res);
        }

        @Override
        Long256Vector op(Mask<Long, Shapes.S256Bit> o, FOp f) {
            long[] res = new long[length()];
            Long256Mask m = (Long256Mask) o;
            for (int i = 0; i < length(); i++) {
                if (m.bits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return new Long256Vector(res);
        }

        // Factories

        @Override
        public Long256Vector zero() {
            return ZERO;
        }

        @Override
        public Long256Mask constantMask(boolean... bits) {
            return new Long256Mask(bits);
        }

        @Override
        public Long256Mask trueMask() {
            return Long256Mask.TRUE_MASK;
        }

        @Override
        public Long256Mask falseMask() {
            return Long256Mask.FALSE_MASK;
        }
    }
}
