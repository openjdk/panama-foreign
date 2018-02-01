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
final class Byte64Vector extends ByteVector<Shapes.S64Bit> {
    static final Byte64Species SPECIES = new Byte64Species();

    static final Byte64Vector ZERO = new Byte64Vector();

    byte[] vec;

    Byte64Vector() {
        vec = new byte[SPECIES.length()];
    }

    Byte64Vector(byte[] v) {
        vec = v;
    }


    // Unary operator

    @Override
    Byte64Vector uOp(FUnOp f) {
        byte[] res = new byte[length()];
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i]);
        }
        return new Byte64Vector(res);
    }

    @Override
    Byte64Vector uOp(Mask<Byte, Shapes.S64Bit> o, FUnOp f) {
        byte[] res = new byte[length()];
        Byte64Mask m = (Byte64Mask) o;
        for (int i = 0; i < length(); i++) {
            res[i] = m.bits[i] ? f.apply(i, vec[i]) : vec[i];
        }
        return new Byte64Vector(res);
    }

    // Binary operator

    @Override
    Byte64Vector bOp(Vector<Byte, Shapes.S64Bit> o, FBinOp f) {
        byte[] res = new byte[length()];
        Byte64Vector v = (Byte64Vector) o;
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i], v.vec[i]);
        }
        return new Byte64Vector(res);
    }

    @Override
    Byte64Vector bOp(Vector<Byte, Shapes.S64Bit> o1, Mask<Byte, Shapes.S64Bit> o2, FBinOp f) {
        byte[] res = new byte[length()];
        Byte64Vector v = (Byte64Vector) o1;
        Byte64Mask m = (Byte64Mask) o2;
        for (int i = 0; i < length(); i++) {
            res[i] = m.bits[i] ? f.apply(i, vec[i], v.vec[i]) : vec[i];
        }
        return new Byte64Vector(res);
    }

    // Trinary operator

    @Override
    Byte64Vector tOp(Vector<Byte, Shapes.S64Bit> o1, Vector<Byte, Shapes.S64Bit> o2, FTriOp f) {
        byte[] res = new byte[length()];
        Byte64Vector v1 = (Byte64Vector) o1;
        Byte64Vector v2 = (Byte64Vector) o2;
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i], v1.vec[i], v2.vec[i]);
        }
        return new Byte64Vector(res);
    }

    @Override
    Byte64Vector tOp(Vector<Byte, Shapes.S64Bit> o1, Vector<Byte, Shapes.S64Bit> o2, Mask<Byte, Shapes.S64Bit> o3, FTriOp f) {
        byte[] res = new byte[length()];
        Byte64Vector v1 = (Byte64Vector) o1;
        Byte64Vector v2 = (Byte64Vector) o2;
        Byte64Mask m = (Byte64Mask) o3;
        for (int i = 0; i < length(); i++) {
            res[i] = m.bits[i] ? f.apply(i, vec[i], v1.vec[i], v2.vec[i]) : vec[i];
        }
        return new Byte64Vector(res);
    }

    @Override
    byte rOp(byte v, FBinOp f) {
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

        Byte64Vector that = (Byte64Vector) o;
        return Arrays.equals(vec, that.vec);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    Byte64Mask bTest(Vector<Byte, Shapes.S64Bit> o, FBinTest f) {
        Byte64Vector v = (Byte64Vector) o;
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(i, vec[i], v.vec[i]);
        }
        return new Byte64Mask(bits);
    }

    // Foreach

    @Override
    void forEach(FUnCon f) {
        for (int i = 0; i < length(); i++) {
            f.apply(i, vec[i]);
        }
    }

    @Override
    void forEach(Mask<Byte, Shapes.S64Bit> o, FUnCon f) {
        Byte64Mask m = (Byte64Mask) o;
        forEach((i, a) -> {
            if (m.bits[i]) { f.apply(i, a); }
        });
    }



    @Override
    public Byte64Vector rotateEL(int j) {
        byte[] res = new byte[length()];
        for (int i = 0; i < length(); i++){
            res[j + i % length()] = vec[i];
        }
        return new Byte64Vector(res);
    }

    @Override
    public Byte64Vector rotateER(int j) {
        byte[] res = new byte[length()];
        for (int i = 0; i < length(); i++){
            int z = i - j;
            if(j < 0) {
                res[length() + z] = vec[i];
            } else {
                res[z] = vec[i];
            }
        }
        return new Byte64Vector(res);
    }

    @Override
    public Byte64Vector shiftEL(int j) {
        byte[] res = new byte[length()];
        for (int i = 0; i < length() - j; i++) {
            res[i] = vec[i + j];
        }
        return new Byte64Vector(res);
    }

    @Override
    public Byte64Vector shiftER(int j) {
        byte[] res = new byte[length()];
        for (int i = 0; i < length() - j; i++){
            res[i + j] = vec[i];
        }
        return new Byte64Vector(res);
    }

    @Override
    public Byte64Vector shuffle(Vector<Byte, Shapes.S64Bit> o, Shuffle<Byte, Shapes.S64Bit> s) {
        Byte64Vector v = (Byte64Vector) o;
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
    public Byte64Vector swizzle(Shuffle<Byte, Shapes.S64Bit> s) {
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
    public byte get(int i) {
        return vec[i];
    }

    @Override
    public Byte64Vector with(int i, byte e) {
        byte[] res = vec.clone();
        res[i] = e;
        return new Byte64Vector(res);
    }

    // Mask

    static final class Byte64Mask extends AbstractMask<Byte, Shapes.S64Bit> {
        static final Byte64Mask TRUE_MASK = new Byte64Mask(true);
        static final Byte64Mask FALSE_MASK = new Byte64Mask(false);

        public Byte64Mask(boolean[] bits) {
            super(bits);
        }

        public Byte64Mask(boolean val) {
            super(val);
        }

        @Override
        Byte64Mask uOp(MUnOp f) {
            boolean[] res = new boolean[species().length()];
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Byte64Mask(res);
        }

        @Override
        Byte64Mask bOp(Mask<Byte, Shapes.S64Bit> o, MBinOp f) {
            boolean[] res = new boolean[species().length()];
            Byte64Mask m = (Byte64Mask) o;
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i], m.bits[i]);
            }
            return new Byte64Mask(res);
        }

        @Override
        public Byte64Species species() {
            return SPECIES;
        }

        @Override
        public Byte64Vector toVector() {
            byte[] res = new byte[species().length()];
            for (int i = 0; i < species().length(); i++) {
                res[i] = (byte) (bits[i] ? -1 : 0);
            }
            return new Byte64Vector(res);
        }
    }

    // Species

    @Override
    public Byte64Species species() {
        return SPECIES;
    }

    static final class Byte64Species extends ByteSpecies<Shapes.S64Bit> {
        static final int BIT_SIZE = Shapes.S_64_BIT.bitSize();

        static final int LENGTH = BIT_SIZE / Byte.SIZE;

        @Override
        public String toString() {
           StringBuilder sb = new StringBuilder("Shape[");
           sb.append(bitSize()).append(" bits, ");
           sb.append(length()).append(" ").append(byte.class.getSimpleName()).append("s x ");
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
        public Class<Byte> elementType() {
            return Byte.class;
        }

        @Override
        public int elementSize() {
            return Byte.SIZE;
        }

        @Override
        public Shapes.S64Bit shape() {
            return Shapes.S_64_BIT;
        }

        @Override
        Byte64Vector op(FOp f) {
            byte[] res = new byte[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return new Byte64Vector(res);
        }

        @Override
        Byte64Vector op(Mask<Byte, Shapes.S64Bit> o, FOp f) {
            byte[] res = new byte[length()];
            Byte64Mask m = (Byte64Mask) o;
            for (int i = 0; i < length(); i++) {
                if (m.bits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return new Byte64Vector(res);
        }

        // Factories

        @Override
        public Byte64Vector zero() {
            return ZERO;
        }

        @Override
        public Byte64Mask constantMask(boolean... bits) {
            return new Byte64Mask(bits);
        }

        @Override
        public Byte64Mask trueMask() {
            return Byte64Mask.TRUE_MASK;
        }

        @Override
        public Byte64Mask falseMask() {
            return Byte64Mask.FALSE_MASK;
        }
    }
}
