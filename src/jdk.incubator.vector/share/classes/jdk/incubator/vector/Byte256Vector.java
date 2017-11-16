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
final class Byte256Vector extends ByteVector<Shapes.S256Bit> {
    static final Byte256Species SPECIES = new Byte256Species();

    static final Byte256Vector ZERO = new Byte256Vector();

    byte[] vec;

    Byte256Vector() {
        vec = new byte[SPECIES.length()];
    }

    Byte256Vector(byte[] v) {
        vec = v;
    }


    // Unary operator

    @Override
    Byte256Vector uOp(FUnOp f) {
        byte[] res = new byte[length()];
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i]);
        }
        return new Byte256Vector(res);
    }

    @Override
    Byte256Vector uOp(Mask<Byte, Shapes.S256Bit> o, FUnOp f) {
        byte[] res = new byte[length()];
        Byte256Mask m = (Byte256Mask) o;
        for (int i = 0; i < length(); i++) {
            res[i] = m.bits[i] ? f.apply(i, vec[i]) : vec[i];
        }
        return new Byte256Vector(res);
    }

    // Binary operator

    @Override
    Byte256Vector bOp(Vector<Byte, Shapes.S256Bit> o, FBinOp f) {
        byte[] res = new byte[length()];
        Byte256Vector v = (Byte256Vector) o;
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i], v.vec[i]);
        }
        return new Byte256Vector(res);
    }

    @Override
    Byte256Vector bOp(Vector<Byte, Shapes.S256Bit> o1, Mask<Byte, Shapes.S256Bit> o2, FBinOp f) {
        byte[] res = new byte[length()];
        Byte256Vector v = (Byte256Vector) o1;
        Byte256Mask m = (Byte256Mask) o2;
        for (int i = 0; i < length(); i++) {
            res[i] = m.bits[i] ? f.apply(i, vec[i], v.vec[i]) : vec[i];
        }
        return new Byte256Vector(res);
    }

    // Trinary operator

    @Override
    Byte256Vector tOp(Vector<Byte, Shapes.S256Bit> o1, Vector<Byte, Shapes.S256Bit> o2, FTriOp f) {
        byte[] res = new byte[length()];
        Byte256Vector v1 = (Byte256Vector) o1;
        Byte256Vector v2 = (Byte256Vector) o2;
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i], v1.vec[i], v2.vec[i]);
        }
        return new Byte256Vector(res);
    }

    @Override
    Byte256Vector tOp(Vector<Byte, Shapes.S256Bit> o1, Vector<Byte, Shapes.S256Bit> o2, Mask<Byte, Shapes.S256Bit> o3, FTriOp f) {
        byte[] res = new byte[length()];
        Byte256Vector v1 = (Byte256Vector) o1;
        Byte256Vector v2 = (Byte256Vector) o2;
        Byte256Mask m = (Byte256Mask) o3;
        for (int i = 0; i < length(); i++) {
            res[i] = m.bits[i] ? f.apply(i, vec[i], v1.vec[i], v2.vec[i]) : vec[i];
        }
        return new Byte256Vector(res);
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

        Byte256Vector that = (Byte256Vector) o;
        return Arrays.equals(vec, that.vec);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    Byte256Mask bTest(Vector<Byte, Shapes.S256Bit> o, FBinTest f) {
        Byte256Vector v = (Byte256Vector) o;
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(i, vec[i], v.vec[i]);
        }
        return new Byte256Mask(bits);
    }

    // Foreach

    @Override
    void forEach(FUnCon f) {
        for (int i = 0; i < length(); i++) {
            f.apply(i, vec[i]);
        }
    }

    @Override
    void forEach(Mask<Byte, Shapes.S256Bit> o, FUnCon f) {
        Byte256Mask m = (Byte256Mask) o;
        forEach((i, a) -> {
            if (m.bits[i]) { f.apply(i, a); }
        });
    }



    @Override
    public Byte256Vector rotateEL(int j) {
        byte[] res = new byte[length()];
        for (int i = 0; i < length(); i++){
            res[j + i % length()] = vec[i];
        }
        return new Byte256Vector(res);
    }

    @Override
    public Byte256Vector rotateER(int j) {
        byte[] res = new byte[length()];
        for (int i = 0; i < length(); i++){
            int z = i - j;
            if(j < 0) {
                res[length() + z] = vec[i];
            } else {
                res[z] = vec[i];
            }
        }
        return new Byte256Vector(res);
    }

    @Override
    public Byte256Vector shiftEL(int j) {
        byte[] res = new byte[length()];
        for (int i = 0; i < length() - j; i++) {
            res[i] = vec[i + j];
        }
        return new Byte256Vector(res);
    }

    @Override
    public Byte256Vector shiftER(int j) {
        byte[] res = new byte[length()];
        for (int i = 0; i < length() - j; i++){
            res[i + j] = vec[i];
        }
        return new Byte256Vector(res);
    }

    @Override
    public Byte256Vector shuffle(Vector<Byte, Shapes.S256Bit> o, Shuffle<Byte, Shapes.S256Bit> s) {
        Byte256Vector v = (Byte256Vector) o;
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
    public Byte256Vector swizzle(Shuffle<Byte, Shapes.S256Bit> s) {
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
    public byte get(int i) {
        return vec[i];
    }

    @Override
    public Byte256Vector with(int i, byte e) {
        byte[] res = vec.clone();
        res[i] = e;
        return new Byte256Vector(res);
    }

    // Mask

    static final class Byte256Mask extends AbstractMask<Byte, Shapes.S256Bit> {
        static final Byte256Mask TRUE_MASK = new Byte256Mask(true);
        static final Byte256Mask FALSE_MASK = new Byte256Mask(false);

        public Byte256Mask(boolean[] bits) {
            super(bits);
        }

        public Byte256Mask(boolean val) {
            super(val);
        }

        @Override
        Byte256Mask uOp(MUnOp f) {
            boolean[] res = new boolean[species().length()];
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Byte256Mask(res);
        }

        @Override
        Byte256Mask bOp(Mask<Byte, Shapes.S256Bit> o, MBinOp f) {
            boolean[] res = new boolean[species().length()];
            Byte256Mask m = (Byte256Mask) o;
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i], m.bits[i]);
            }
            return new Byte256Mask(res);
        }

        @Override
        public Byte256Species species() {
            return SPECIES;
        }

        @Override
        public Byte256Vector toVector() {
            byte[] res = new byte[species().length()];
            for (int i = 0; i < species().length(); i++) {
                res[i] = (byte) (bits[i] ? -1 : 0);
            }
            return new Byte256Vector(res);
        }
    }

    // Species

    @Override
    public Byte256Species species() {
        return SPECIES;
    }

    static final class Byte256Species extends ByteSpecies<Shapes.S256Bit> {
        static final int BIT_SIZE = Shapes.S_256_BIT.bitSize();

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
        public Shapes.S256Bit shape() {
            return Shapes.S_256_BIT;
        }

        @Override
        Byte256Vector op(FOp f) {
            byte[] res = new byte[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return new Byte256Vector(res);
        }

        @Override
        Byte256Vector op(Mask<Byte, Shapes.S256Bit> o, FOp f) {
            byte[] res = new byte[length()];
            Byte256Mask m = (Byte256Mask) o;
            for (int i = 0; i < length(); i++) {
                if (m.bits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return new Byte256Vector(res);
        }

        // Factories

        @Override
        public Byte256Vector zero() {
            return ZERO;
        }

        @Override
        public Byte256Mask constantMask(boolean... bits) {
            return new Byte256Mask(bits);
        }

        @Override
        public Byte256Mask trueMask() {
            return Byte256Mask.TRUE_MASK;
        }

        @Override
        public Byte256Mask falseMask() {
            return Byte256Mask.FALSE_MASK;
        }
    }
}
