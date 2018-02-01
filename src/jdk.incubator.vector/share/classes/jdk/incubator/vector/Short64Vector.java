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
final class Short64Vector extends ShortVector<Shapes.S64Bit> {
    static final Short64Species SPECIES = new Short64Species();

    static final Short64Vector ZERO = new Short64Vector();

    short[] vec;

    Short64Vector() {
        vec = new short[SPECIES.length()];
    }

    Short64Vector(short[] v) {
        vec = v;
    }


    // Unary operator

    @Override
    Short64Vector uOp(FUnOp f) {
        short[] res = new short[length()];
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i]);
        }
        return new Short64Vector(res);
    }

    @Override
    Short64Vector uOp(Mask<Short, Shapes.S64Bit> o, FUnOp f) {
        short[] res = new short[length()];
        Short64Mask m = (Short64Mask) o;
        for (int i = 0; i < length(); i++) {
            res[i] = m.bits[i] ? f.apply(i, vec[i]) : vec[i];
        }
        return new Short64Vector(res);
    }

    // Binary operator

    @Override
    Short64Vector bOp(Vector<Short, Shapes.S64Bit> o, FBinOp f) {
        short[] res = new short[length()];
        Short64Vector v = (Short64Vector) o;
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i], v.vec[i]);
        }
        return new Short64Vector(res);
    }

    @Override
    Short64Vector bOp(Vector<Short, Shapes.S64Bit> o1, Mask<Short, Shapes.S64Bit> o2, FBinOp f) {
        short[] res = new short[length()];
        Short64Vector v = (Short64Vector) o1;
        Short64Mask m = (Short64Mask) o2;
        for (int i = 0; i < length(); i++) {
            res[i] = m.bits[i] ? f.apply(i, vec[i], v.vec[i]) : vec[i];
        }
        return new Short64Vector(res);
    }

    // Trinary operator

    @Override
    Short64Vector tOp(Vector<Short, Shapes.S64Bit> o1, Vector<Short, Shapes.S64Bit> o2, FTriOp f) {
        short[] res = new short[length()];
        Short64Vector v1 = (Short64Vector) o1;
        Short64Vector v2 = (Short64Vector) o2;
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i], v1.vec[i], v2.vec[i]);
        }
        return new Short64Vector(res);
    }

    @Override
    Short64Vector tOp(Vector<Short, Shapes.S64Bit> o1, Vector<Short, Shapes.S64Bit> o2, Mask<Short, Shapes.S64Bit> o3, FTriOp f) {
        short[] res = new short[length()];
        Short64Vector v1 = (Short64Vector) o1;
        Short64Vector v2 = (Short64Vector) o2;
        Short64Mask m = (Short64Mask) o3;
        for (int i = 0; i < length(); i++) {
            res[i] = m.bits[i] ? f.apply(i, vec[i], v1.vec[i], v2.vec[i]) : vec[i];
        }
        return new Short64Vector(res);
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

        Short64Vector that = (Short64Vector) o;
        return Arrays.equals(vec, that.vec);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    Short64Mask bTest(Vector<Short, Shapes.S64Bit> o, FBinTest f) {
        Short64Vector v = (Short64Vector) o;
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(i, vec[i], v.vec[i]);
        }
        return new Short64Mask(bits);
    }

    // Foreach

    @Override
    void forEach(FUnCon f) {
        for (int i = 0; i < length(); i++) {
            f.apply(i, vec[i]);
        }
    }

    @Override
    void forEach(Mask<Short, Shapes.S64Bit> o, FUnCon f) {
        Short64Mask m = (Short64Mask) o;
        forEach((i, a) -> {
            if (m.bits[i]) { f.apply(i, a); }
        });
    }



    @Override
    public Short64Vector rotateEL(int j) {
        short[] res = new short[length()];
        for (int i = 0; i < length(); i++){
            res[j + i % length()] = vec[i];
        }
        return new Short64Vector(res);
    }

    @Override
    public Short64Vector rotateER(int j) {
        short[] res = new short[length()];
        for (int i = 0; i < length(); i++){
            int z = i - j;
            if(j < 0) {
                res[length() + z] = vec[i];
            } else {
                res[z] = vec[i];
            }
        }
        return new Short64Vector(res);
    }

    @Override
    public Short64Vector shiftEL(int j) {
        short[] res = new short[length()];
        for (int i = 0; i < length() - j; i++) {
            res[i] = vec[i + j];
        }
        return new Short64Vector(res);
    }

    @Override
    public Short64Vector shiftER(int j) {
        short[] res = new short[length()];
        for (int i = 0; i < length() - j; i++){
            res[i + j] = vec[i];
        }
        return new Short64Vector(res);
    }

    @Override
    public Short64Vector shuffle(Vector<Short, Shapes.S64Bit> o, Shuffle<Short, Shapes.S64Bit> s) {
        Short64Vector v = (Short64Vector) o;
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
    public Short64Vector swizzle(Shuffle<Short, Shapes.S64Bit> s) {
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
    public Short64Vector with(int i, short e) {
        short[] res = vec.clone();
        res[i] = e;
        return new Short64Vector(res);
    }

    // Mask

    static final class Short64Mask extends AbstractMask<Short, Shapes.S64Bit> {
        static final Short64Mask TRUE_MASK = new Short64Mask(true);
        static final Short64Mask FALSE_MASK = new Short64Mask(false);

        public Short64Mask(boolean[] bits) {
            super(bits);
        }

        public Short64Mask(boolean val) {
            super(val);
        }

        @Override
        Short64Mask uOp(MUnOp f) {
            boolean[] res = new boolean[species().length()];
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Short64Mask(res);
        }

        @Override
        Short64Mask bOp(Mask<Short, Shapes.S64Bit> o, MBinOp f) {
            boolean[] res = new boolean[species().length()];
            Short64Mask m = (Short64Mask) o;
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i], m.bits[i]);
            }
            return new Short64Mask(res);
        }

        @Override
        public Short64Species species() {
            return SPECIES;
        }

        @Override
        public Short64Vector toVector() {
            short[] res = new short[species().length()];
            for (int i = 0; i < species().length(); i++) {
                res[i] = (short) (bits[i] ? -1 : 0);
            }
            return new Short64Vector(res);
        }
    }

    // Species

    @Override
    public Short64Species species() {
        return SPECIES;
    }

    static final class Short64Species extends ShortSpecies<Shapes.S64Bit> {
        static final int BIT_SIZE = Shapes.S_64_BIT.bitSize();

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
        public Shapes.S64Bit shape() {
            return Shapes.S_64_BIT;
        }

        @Override
        Short64Vector op(FOp f) {
            short[] res = new short[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return new Short64Vector(res);
        }

        @Override
        Short64Vector op(Mask<Short, Shapes.S64Bit> o, FOp f) {
            short[] res = new short[length()];
            Short64Mask m = (Short64Mask) o;
            for (int i = 0; i < length(); i++) {
                if (m.bits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return new Short64Vector(res);
        }

        // Factories

        @Override
        public Short64Vector zero() {
            return ZERO;
        }

        @Override
        public Short64Mask constantMask(boolean... bits) {
            return new Short64Mask(bits);
        }

        @Override
        public Short64Mask trueMask() {
            return Short64Mask.TRUE_MASK;
        }

        @Override
        public Short64Mask falseMask() {
            return Short64Mask.FALSE_MASK;
        }
    }
}
