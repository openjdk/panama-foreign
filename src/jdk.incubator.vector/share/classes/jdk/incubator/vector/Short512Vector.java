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
final class Short512Vector extends ShortVector<Shapes.S512Bit> {
    static final Short512Species SPECIES = new Short512Species();

    static final Short512Vector ZERO = new Short512Vector();

    short[] vec;

    Short512Vector() {
        vec = new short[SPECIES.length()];
    }

    Short512Vector(short[] v) {
        vec = v;
    }


    // Unary operator

    @Override
    Short512Vector uOp(FUnOp f) {
        short[] res = new short[length()];
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i]);
        }
        return new Short512Vector(res);
    }

    @Override
    Short512Vector uOp(Mask<Short, Shapes.S512Bit> o, FUnOp f) {
        short[] res = new short[length()];
        Short512Mask m = (Short512Mask) o;
        for (int i = 0; i < length(); i++) {
            res[i] = m.bits[i] ? f.apply(i, vec[i]) : vec[i];
        }
        return new Short512Vector(res);
    }

    // Binary operator

    @Override
    Short512Vector bOp(Vector<Short, Shapes.S512Bit> o, FBinOp f) {
        short[] res = new short[length()];
        Short512Vector v = (Short512Vector) o;
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i], v.vec[i]);
        }
        return new Short512Vector(res);
    }

    @Override
    Short512Vector bOp(Vector<Short, Shapes.S512Bit> o1, Mask<Short, Shapes.S512Bit> o2, FBinOp f) {
        short[] res = new short[length()];
        Short512Vector v = (Short512Vector) o1;
        Short512Mask m = (Short512Mask) o2;
        for (int i = 0; i < length(); i++) {
            res[i] = m.bits[i] ? f.apply(i, vec[i], v.vec[i]) : vec[i];
        }
        return new Short512Vector(res);
    }

    // Trinary operator

    @Override
    Short512Vector tOp(Vector<Short, Shapes.S512Bit> o1, Vector<Short, Shapes.S512Bit> o2, FTriOp f) {
        short[] res = new short[length()];
        Short512Vector v1 = (Short512Vector) o1;
        Short512Vector v2 = (Short512Vector) o2;
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i], v1.vec[i], v2.vec[i]);
        }
        return new Short512Vector(res);
    }

    @Override
    Short512Vector tOp(Vector<Short, Shapes.S512Bit> o1, Vector<Short, Shapes.S512Bit> o2, Mask<Short, Shapes.S512Bit> o3, FTriOp f) {
        short[] res = new short[length()];
        Short512Vector v1 = (Short512Vector) o1;
        Short512Vector v2 = (Short512Vector) o2;
        Short512Mask m = (Short512Mask) o3;
        for (int i = 0; i < length(); i++) {
            res[i] = m.bits[i] ? f.apply(i, vec[i], v1.vec[i], v2.vec[i]) : vec[i];
        }
        return new Short512Vector(res);
    }

    @Override
    short rOp(short v, FBinOp f) {
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

        Short512Vector that = (Short512Vector) o;
        return Arrays.equals(vec, that.vec);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    Short512Mask bTest(Vector<Short, Shapes.S512Bit> o, FBinTest f) {
        Short512Vector v = (Short512Vector) o;
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(i, vec[i], v.vec[i]);
        }
        return new Short512Mask(bits);
    }

    // Foreach

    @Override
    void forEach(FUnCon f) {
        for (int i = 0; i < length(); i++) {
            f.apply(i, vec[i]);
        }
    }

    @Override
    void forEach(Mask<Short, Shapes.S512Bit> o, FUnCon f) {
        Short512Mask m = (Short512Mask) o;
        forEach((i, a) -> {
            if (m.bits[i]) { f.apply(i, a); }
        });
    }



    @Override
    public Short512Vector rotateEL(int j) {
        short[] res = new short[length()];
        for (int i = 0; i < length(); i++){
            res[j + i % length()] = vec[i];
        }
        return new Short512Vector(res);
    }

    @Override
    public Short512Vector rotateER(int j) {
        short[] res = new short[length()];
        for (int i = 0; i < length(); i++){
            int z = i - j;
            if(j < 0) {
                res[length() + z] = vec[i];
            } else {
                res[z] = vec[i];
            }
        }
        return new Short512Vector(res);
    }

    @Override
    public Short512Vector shiftEL(int j) {
        short[] res = new short[length()];
        for (int i = 0; i < length() - j; i++) {
            res[i] = vec[i + j];
        }
        return new Short512Vector(res);
    }

    @Override
    public Short512Vector shiftER(int j) {
        short[] res = new short[length()];
        for (int i = 0; i < length() - j; i++){
            res[i + j] = vec[i];
        }
        return new Short512Vector(res);
    }

    @Override
    public Short512Vector shuffle(Vector<Short, Shapes.S512Bit> o, Shuffle<Short, Shapes.S512Bit> s) {
        Short512Vector v = (Short512Vector) o;
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
    public Short512Vector swizzle(Shuffle<Short, Shapes.S512Bit> s) {
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
    public <F, Z extends Shape> Vector<F, Z> cast(Class<F> type, Z shape) {
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
    public short get(int i) {
        return vec[i];
    }

    @Override
    public Short512Vector with(int i, short e) {
        short[] res = vec.clone();
        res[i] = e;
        return new Short512Vector(res);
    }

    // Mask

    static final class Short512Mask extends AbstractMask<Short, Shapes.S512Bit> {
        static final Short512Mask TRUE_MASK = new Short512Mask(true);
        static final Short512Mask FALSE_MASK = new Short512Mask(false);

        public Short512Mask(boolean[] bits) {
            super(bits);
        }

        public Short512Mask(boolean val) {
            super(val);
        }

        @Override
        Short512Mask uOp(MUnOp f) {
            boolean[] res = new boolean[species().length()];
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Short512Mask(res);
        }

        @Override
        Short512Mask bOp(Mask<Short, Shapes.S512Bit> o, MBinOp f) {
            boolean[] res = new boolean[species().length()];
            Short512Mask m = (Short512Mask) o;
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i], m.bits[i]);
            }
            return new Short512Mask(res);
        }

        @Override
        public Short512Species species() {
            return SPECIES;
        }

        @Override
        public Short512Vector toVector() {
            short[] res = new short[species().length()];
            for (int i = 0; i < species().length(); i++) {
                res[i] = (short) (bits[i] ? -1 : 0);
            }
            return new Short512Vector(res);
        }
    }

    // Species

    @Override
    public Short512Species species() {
        return SPECIES;
    }

    static final class Short512Species extends ShortSpecies<Shapes.S512Bit> {
        static final int BIT_SIZE = Shapes.S_512_BIT.bitSize();

        static final int LENGTH = BIT_SIZE / Short.SIZE;

        @Override
        public String toString() {
           StringBuilder sb = new StringBuilder("Shape[");
           sb.append(bitSize()).append(" bits, ");
           sb.append(length()).append(" ").append(short.class.getSimpleName()).append("s x ");
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
        public Class<Short> elementType() {
            return Short.class;
        }

        @Override
        public int elementSize() {
            return Short.SIZE;
        }

        @Override
        public Shapes.S512Bit shape() {
            return Shapes.S_512_BIT;
        }

        @Override
        Short512Vector op(FOp f) {
            short[] res = new short[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return new Short512Vector(res);
        }

        @Override
        Short512Vector op(Mask<Short, Shapes.S512Bit> o, FOp f) {
            short[] res = new short[length()];
            Short512Mask m = (Short512Mask) o;
            for (int i = 0; i < length(); i++) {
                if (m.bits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return new Short512Vector(res);
        }

        // Factories

        @Override
        public Short512Vector zero() {
            return ZERO;
        }

        @Override
        public Short512Mask constantMask(boolean... bits) {
            return new Short512Mask(bits);
        }

        @Override
        public Short512Mask trueMask() {
            return Short512Mask.TRUE_MASK;
        }

        @Override
        public Short512Mask falseMask() {
            return Short512Mask.FALSE_MASK;
        }
    }
}
