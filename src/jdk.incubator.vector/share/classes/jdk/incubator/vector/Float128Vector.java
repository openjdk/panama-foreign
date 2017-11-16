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
final class Float128Vector extends FloatVector<Shapes.S128Bit> {
    static final Float128Species SPECIES = new Float128Species();

    static final Float128Vector ZERO = new Float128Vector();

    float[] vec;

    Float128Vector() {
        vec = new float[SPECIES.length()];
    }

    Float128Vector(float[] v) {
        vec = v;
    }


    // Unary operator

    @Override
    Float128Vector uOp(FUnOp f) {
        float[] res = new float[length()];
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i]);
        }
        return new Float128Vector(res);
    }

    @Override
    Float128Vector uOp(Mask<Float, Shapes.S128Bit> o, FUnOp f) {
        float[] res = new float[length()];
        Float128Mask m = (Float128Mask) o;
        for (int i = 0; i < length(); i++) {
            res[i] = m.bits[i] ? f.apply(i, vec[i]) : vec[i];
        }
        return new Float128Vector(res);
    }

    // Binary operator

    @Override
    Float128Vector bOp(Vector<Float, Shapes.S128Bit> o, FBinOp f) {
        float[] res = new float[length()];
        Float128Vector v = (Float128Vector) o;
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i], v.vec[i]);
        }
        return new Float128Vector(res);
    }

    @Override
    Float128Vector bOp(Vector<Float, Shapes.S128Bit> o1, Mask<Float, Shapes.S128Bit> o2, FBinOp f) {
        float[] res = new float[length()];
        Float128Vector v = (Float128Vector) o1;
        Float128Mask m = (Float128Mask) o2;
        for (int i = 0; i < length(); i++) {
            res[i] = m.bits[i] ? f.apply(i, vec[i], v.vec[i]) : vec[i];
        }
        return new Float128Vector(res);
    }

    // Trinary operator

    @Override
    Float128Vector tOp(Vector<Float, Shapes.S128Bit> o1, Vector<Float, Shapes.S128Bit> o2, FTriOp f) {
        float[] res = new float[length()];
        Float128Vector v1 = (Float128Vector) o1;
        Float128Vector v2 = (Float128Vector) o2;
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i], v1.vec[i], v2.vec[i]);
        }
        return new Float128Vector(res);
    }

    @Override
    Float128Vector tOp(Vector<Float, Shapes.S128Bit> o1, Vector<Float, Shapes.S128Bit> o2, Mask<Float, Shapes.S128Bit> o3, FTriOp f) {
        float[] res = new float[length()];
        Float128Vector v1 = (Float128Vector) o1;
        Float128Vector v2 = (Float128Vector) o2;
        Float128Mask m = (Float128Mask) o3;
        for (int i = 0; i < length(); i++) {
            res[i] = m.bits[i] ? f.apply(i, vec[i], v1.vec[i], v2.vec[i]) : vec[i];
        }
        return new Float128Vector(res);
    }

    @Override
    float rOp(float v, FBinOp f) {
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

        Float128Vector that = (Float128Vector) o;
        return Arrays.equals(vec, that.vec);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    Float128Mask bTest(Vector<Float, Shapes.S128Bit> o, FBinTest f) {
        Float128Vector v = (Float128Vector) o;
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(i, vec[i], v.vec[i]);
        }
        return new Float128Mask(bits);
    }

    // Foreach

    @Override
    void forEach(FUnCon f) {
        for (int i = 0; i < length(); i++) {
            f.apply(i, vec[i]);
        }
    }

    @Override
    void forEach(Mask<Float, Shapes.S128Bit> o, FUnCon f) {
        Float128Mask m = (Float128Mask) o;
        forEach((i, a) -> {
            if (m.bits[i]) { f.apply(i, a); }
        });
    }

    Int128Vector toBits() {
        int[] res = new int[this.species().length()];
        for(int i = 0; i < this.species().length(); i++){
            res[i] = Float.floatToIntBits(vec[i]);
        }
        return new Int128Vector(res);
    }


    @Override
    public Float128Vector rotateEL(int j) {
        float[] res = new float[length()];
        for (int i = 0; i < length(); i++){
            res[j + i % length()] = vec[i];
        }
        return new Float128Vector(res);
    }

    @Override
    public Float128Vector rotateER(int j) {
        float[] res = new float[length()];
        for (int i = 0; i < length(); i++){
            int z = i - j;
            if(j < 0) {
                res[length() + z] = vec[i];
            } else {
                res[z] = vec[i];
            }
        }
        return new Float128Vector(res);
    }

    @Override
    public Float128Vector shiftEL(int j) {
        float[] res = new float[length()];
        for (int i = 0; i < length() - j; i++) {
            res[i] = vec[i + j];
        }
        return new Float128Vector(res);
    }

    @Override
    public Float128Vector shiftER(int j) {
        float[] res = new float[length()];
        for (int i = 0; i < length() - j; i++){
            res[i + j] = vec[i];
        }
        return new Float128Vector(res);
    }

    @Override
    public Float128Vector shuffle(Vector<Float, Shapes.S128Bit> o, Shuffle<Float, Shapes.S128Bit> s) {
        Float128Vector v = (Float128Vector) o;
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
    public Float128Vector swizzle(Shuffle<Float, Shapes.S128Bit> s) {
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
    public float get(int i) {
        return vec[i];
    }

    @Override
    public Float128Vector with(int i, float e) {
        float[] res = vec.clone();
        res[i] = e;
        return new Float128Vector(res);
    }

    // Mask

    static final class Float128Mask extends AbstractMask<Float, Shapes.S128Bit> {
        static final Float128Mask TRUE_MASK = new Float128Mask(true);
        static final Float128Mask FALSE_MASK = new Float128Mask(false);

        public Float128Mask(boolean[] bits) {
            super(bits);
        }

        public Float128Mask(boolean val) {
            super(val);
        }

        @Override
        Float128Mask uOp(MUnOp f) {
            boolean[] res = new boolean[species().length()];
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Float128Mask(res);
        }

        @Override
        Float128Mask bOp(Mask<Float, Shapes.S128Bit> o, MBinOp f) {
            boolean[] res = new boolean[species().length()];
            Float128Mask m = (Float128Mask) o;
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i], m.bits[i]);
            }
            return new Float128Mask(res);
        }

        @Override
        public Float128Species species() {
            return SPECIES;
        }

        @Override
        public Float128Vector toVector() {
            float[] res = new float[species().length()];
            for (int i = 0; i < species().length(); i++) {
                res[i] = (float) (bits[i] ? -1 : 0);
            }
            return new Float128Vector(res);
        }
    }

    // Species

    @Override
    public Float128Species species() {
        return SPECIES;
    }

    static final class Float128Species extends FloatSpecies<Shapes.S128Bit> {
        static final int BIT_SIZE = Shapes.S_128_BIT.bitSize();

        static final int LENGTH = BIT_SIZE / Float.SIZE;

        @Override
        public String toString() {
           StringBuilder sb = new StringBuilder("Shape[");
           sb.append(bitSize()).append(" bits, ");
           sb.append(length()).append(" ").append(float.class.getSimpleName()).append("s x ");
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
        public Class<Float> elementType() {
            return Float.class;
        }

        @Override
        public int elementSize() {
            return Float.SIZE;
        }

        @Override
        public Shapes.S128Bit shape() {
            return Shapes.S_128_BIT;
        }

        @Override
        Float128Vector op(FOp f) {
            float[] res = new float[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return new Float128Vector(res);
        }

        @Override
        Float128Vector op(Mask<Float, Shapes.S128Bit> o, FOp f) {
            float[] res = new float[length()];
            Float128Mask m = (Float128Mask) o;
            for (int i = 0; i < length(); i++) {
                if (m.bits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return new Float128Vector(res);
        }

        // Factories

        @Override
        public Float128Vector zero() {
            return ZERO;
        }

        @Override
        public Float128Mask constantMask(boolean... bits) {
            return new Float128Mask(bits);
        }

        @Override
        public Float128Mask trueMask() {
            return Float128Mask.TRUE_MASK;
        }

        @Override
        public Float128Mask falseMask() {
            return Float128Mask.FALSE_MASK;
        }
    }
}
