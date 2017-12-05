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
import jdk.internal.HotSpotIntrinsicCandidate;

@SuppressWarnings("cast")
final class Float512Vector extends FloatVector<Shapes.S512Bit> {
    static final Float512Species SPECIES = new Float512Species();

    static final Float512Vector ZERO = new Float512Vector();

    float[] vec;

    Float512Vector() {
        vec = new float[SPECIES.length()];
    }

    Float512Vector(float[] v) {
        vec = v;
    }


    // Unary operator

    @Override
    Float512Vector uOp(FUnOp f) {
        float[] res = new float[length()];
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i]);
        }
        return new Float512Vector(res);
    }

    @Override
    Float512Vector uOp(Mask<Float, Shapes.S512Bit> o, FUnOp f) {
        float[] res = new float[length()];
        Float512Mask m = (Float512Mask) o;
        for (int i = 0; i < length(); i++) {
            res[i] = m.bits[i] ? f.apply(i, vec[i]) : vec[i];
        }
        return new Float512Vector(res);
    }

    // Binary operator

    @Override
    Float512Vector bOp(Vector<Float, Shapes.S512Bit> o, FBinOp f) {
        float[] res = new float[length()];
        Float512Vector v = (Float512Vector) o;
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i], v.vec[i]);
        }
        return new Float512Vector(res);
    }

    @Override
    Float512Vector bOp(Vector<Float, Shapes.S512Bit> o1, Mask<Float, Shapes.S512Bit> o2, FBinOp f) {
        float[] res = new float[length()];
        Float512Vector v = (Float512Vector) o1;
        Float512Mask m = (Float512Mask) o2;
        for (int i = 0; i < length(); i++) {
            res[i] = m.bits[i] ? f.apply(i, vec[i], v.vec[i]) : vec[i];
        }
        return new Float512Vector(res);
    }

    // Trinary operator

    @Override
    Float512Vector tOp(Vector<Float, Shapes.S512Bit> o1, Vector<Float, Shapes.S512Bit> o2, FTriOp f) {
        float[] res = new float[length()];
        Float512Vector v1 = (Float512Vector) o1;
        Float512Vector v2 = (Float512Vector) o2;
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i], v1.vec[i], v2.vec[i]);
        }
        return new Float512Vector(res);
    }

    @Override
    Float512Vector tOp(Vector<Float, Shapes.S512Bit> o1, Vector<Float, Shapes.S512Bit> o2, Mask<Float, Shapes.S512Bit> o3, FTriOp f) {
        float[] res = new float[length()];
        Float512Vector v1 = (Float512Vector) o1;
        Float512Vector v2 = (Float512Vector) o2;
        Float512Mask m = (Float512Mask) o3;
        for (int i = 0; i < length(); i++) {
            res[i] = m.bits[i] ? f.apply(i, vec[i], v1.vec[i], v2.vec[i]) : vec[i];
        }
        return new Float512Vector(res);
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

        Float512Vector that = (Float512Vector) o;
        return Arrays.equals(vec, that.vec);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    Float512Mask bTest(Vector<Float, Shapes.S512Bit> o, FBinTest f) {
        Float512Vector v = (Float512Vector) o;
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(i, vec[i], v.vec[i]);
        }
        return new Float512Mask(bits);
    }

    // Foreach

    @Override
    void forEach(FUnCon f) {
        for (int i = 0; i < length(); i++) {
            f.apply(i, vec[i]);
        }
    }

    @Override
    void forEach(Mask<Float, Shapes.S512Bit> o, FUnCon f) {
        Float512Mask m = (Float512Mask) o;
        forEach((i, a) -> {
            if (m.bits[i]) { f.apply(i, a); }
        });
    }

    Int512Vector toBits() {
        int[] res = new int[this.species().length()];
        for(int i = 0; i < this.species().length(); i++){
            res[i] = Float.floatToIntBits(vec[i]);
        }
        return new Int512Vector(res);
    }


    @Override
    public Float512Vector rotateEL(int j) {
        float[] res = new float[length()];
        for (int i = 0; i < length(); i++){
            res[j + i % length()] = vec[i];
        }
        return new Float512Vector(res);
    }

    @Override
    public Float512Vector rotateER(int j) {
        float[] res = new float[length()];
        for (int i = 0; i < length(); i++){
            int z = i - j;
            if(j < 0) {
                res[length() + z] = vec[i];
            } else {
                res[z] = vec[i];
            }
        }
        return new Float512Vector(res);
    }

    @Override
    public Float512Vector shiftEL(int j) {
        float[] res = new float[length()];
        for (int i = 0; i < length() - j; i++) {
            res[i] = vec[i + j];
        }
        return new Float512Vector(res);
    }

    @Override
    public Float512Vector shiftER(int j) {
        float[] res = new float[length()];
        for (int i = 0; i < length() - j; i++){
            res[i + j] = vec[i];
        }
        return new Float512Vector(res);
    }

    @Override
    public Float512Vector shuffle(Vector<Float, Shapes.S512Bit> o, Shuffle<Float, Shapes.S512Bit> s) {
        Float512Vector v = (Float512Vector) o;
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
    public Float512Vector swizzle(Shuffle<Float, Shapes.S512Bit> s) {
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
    public Float512Vector with(int i, float e) {
        float[] res = vec.clone();
        res[i] = e;
        return new Float512Vector(res);
    }

    // Mask

    static final class Float512Mask extends AbstractMask<Float, Shapes.S512Bit> {
        static final Float512Mask TRUE_MASK = new Float512Mask(true);
        static final Float512Mask FALSE_MASK = new Float512Mask(false);

        public Float512Mask(boolean[] bits) {
            super(bits);
        }

        public Float512Mask(boolean val) {
            super(val);
        }

        @Override
        Float512Mask uOp(MUnOp f) {
            boolean[] res = new boolean[species().length()];
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Float512Mask(res);
        }

        @Override
        Float512Mask bOp(Mask<Float, Shapes.S512Bit> o, MBinOp f) {
            boolean[] res = new boolean[species().length()];
            Float512Mask m = (Float512Mask) o;
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i], m.bits[i]);
            }
            return new Float512Mask(res);
        }

        @Override
        public Float512Species species() {
            return SPECIES;
        }

        @Override
        public Float512Vector toVector() {
            float[] res = new float[species().length()];
            for (int i = 0; i < species().length(); i++) {
                res[i] = (float) (bits[i] ? -1 : 0);
            }
            return new Float512Vector(res);
        }
    }

    // Species

    @Override
    public Float512Species species() {
        return SPECIES;
    }

    static final class Float512Species extends FloatSpecies<Shapes.S512Bit> {
        static final int BIT_SIZE = Shapes.S_512_BIT.bitSize();

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
        public Shapes.S512Bit shape() {
            return Shapes.S_512_BIT;
        }

        @Override
        Float512Vector op(FOp f) {
            float[] res = new float[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return new Float512Vector(res);
        }

        @Override
        Float512Vector op(Mask<Float, Shapes.S512Bit> o, FOp f) {
            float[] res = new float[length()];
            Float512Mask m = (Float512Mask) o;
            for (int i = 0; i < length(); i++) {
                if (m.bits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return new Float512Vector(res);
        }

        // Factories

        @Override
        public Float512Vector zero() {
            return ZERO;
        }

        @Override
        public Float512Mask constantMask(boolean... bits) {
            return new Float512Mask(bits);
        }

        @HotSpotIntrinsicCandidate
        @Override
        public Float512Mask trueMask() {
            return Float512Mask.TRUE_MASK;
        }

        @HotSpotIntrinsicCandidate
        @Override
        public Float512Mask falseMask() {
            return Float512Mask.FALSE_MASK;
        }
    }
}
