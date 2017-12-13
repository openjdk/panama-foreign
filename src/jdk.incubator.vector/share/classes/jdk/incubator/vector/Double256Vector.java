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
final class Double256Vector extends DoubleVector<Shapes.S256Bit> {
    static final Double256Species SPECIES = new Double256Species();

    static final Double256Vector ZERO = new Double256Vector();

    double[] vec;

    Double256Vector() {
        vec = new double[SPECIES.length()];
    }

    Double256Vector(double[] v) {
        vec = v;
    }


    // Unary operator

    @Override
    Double256Vector uOp(FUnOp f) {
        double[] res = new double[length()];
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i]);
        }
        return new Double256Vector(res);
    }

    @Override
    Double256Vector uOp(Mask<Double, Shapes.S256Bit> m, FUnOp f) {
        double[] res = new double[length()];
        for (int i = 0; i < length(); i++) {
            res[i] = m.getElement(i) ? f.apply(i, vec[i]) : vec[i];
        }
        return new Double256Vector(res);
    }

    // Binary operator

    @Override
    Double256Vector bOp(Vector<Double, Shapes.S256Bit> o, FBinOp f) {
        double[] res = new double[length()];
        Double256Vector v = (Double256Vector) o;
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i], v.vec[i]);
        }
        return new Double256Vector(res);
    }

    @Override
    Double256Vector bOp(Vector<Double, Shapes.S256Bit> o, Mask<Double, Shapes.S256Bit> m, FBinOp f) {
        double[] res = new double[length()];
        Double256Vector v = (Double256Vector) o;
        for (int i = 0; i < length(); i++) {
            res[i] = m.getElement(i) ? f.apply(i, vec[i], v.vec[i]) : vec[i];
        }
        return new Double256Vector(res);
    }

    // Trinary operator

    @Override
    Double256Vector tOp(Vector<Double, Shapes.S256Bit> o1, Vector<Double, Shapes.S256Bit> o2, FTriOp f) {
        double[] res = new double[length()];
        Double256Vector v1 = (Double256Vector) o1;
        Double256Vector v2 = (Double256Vector) o2;
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i], v1.vec[i], v2.vec[i]);
        }
        return new Double256Vector(res);
    }

    @Override
    Double256Vector tOp(Vector<Double, Shapes.S256Bit> o1, Vector<Double, Shapes.S256Bit> o2, Mask<Double, Shapes.S256Bit> m, FTriOp f) {
        double[] res = new double[length()];
        Double256Vector v1 = (Double256Vector) o1;
        Double256Vector v2 = (Double256Vector) o2;
        for (int i = 0; i < length(); i++) {
            res[i] = m.getElement(i) ? f.apply(i, vec[i], v1.vec[i], v2.vec[i]) : vec[i];
        }
        return new Double256Vector(res);
    }

    @Override
    double rOp(double v, FBinOp f) {
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

        Double256Vector that = (Double256Vector) o;
        return Arrays.equals(vec, that.vec);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    Mask<Double, Shapes.S256Bit> bTest(Vector<Double, Shapes.S256Bit> o, FBinTest f) {
        Double256Vector v = (Double256Vector) o;
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(i, vec[i], v.vec[i]);
        }
        return new GenericMask<>(this.species(), bits);
    }

    // Foreach

    @Override
    void forEach(FUnCon f) {
        for (int i = 0; i < length(); i++) {
            f.apply(i, vec[i]);
        }
    }

    @Override
    void forEach(Mask<Double, Shapes.S256Bit> m, FUnCon f) {
        forEach((i, a) -> {
            if (m.getElement(i)) { f.apply(i, a); }
        });
    }

    Long256Vector toBits() {
        long[] res = new long[this.species().length()];
        for(int i = 0; i < this.species().length(); i++){
            res[i] = Double.doubleToLongBits(vec[i]);
        }
        return new Long256Vector(res);
    }


    @Override
    public Double256Vector rotateEL(int j) {
        double[] res = new double[length()];
        for (int i = 0; i < length(); i++){
            res[j + i % length()] = vec[i];
        }
        return new Double256Vector(res);
    }

    @Override
    public Double256Vector rotateER(int j) {
        double[] res = new double[length()];
        for (int i = 0; i < length(); i++){
            int z = i - j;
            if(j < 0) {
                res[length() + z] = vec[i];
            } else {
                res[z] = vec[i];
            }
        }
        return new Double256Vector(res);
    }

    @Override
    public Double256Vector shiftEL(int j) {
        double[] res = new double[length()];
        for (int i = 0; i < length() - j; i++) {
            res[i] = vec[i + j];
        }
        return new Double256Vector(res);
    }

    @Override
    public Double256Vector shiftER(int j) {
        double[] res = new double[length()];
        for (int i = 0; i < length() - j; i++){
            res[i + j] = vec[i];
        }
        return new Double256Vector(res);
    }

    @Override
    public Double256Vector shuffle(Vector<Double, Shapes.S256Bit> o, Shuffle<Double, Shapes.S256Bit> s) {
        Double256Vector v = (Double256Vector) o;
        return uOp((i, a) -> {
            int e = s.getElement(i);
            if(e > 0 && e < length()) {
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
    public Double256Vector swizzle(Shuffle<Double, Shapes.S256Bit> s) {
        return uOp((i, a) -> {
            int e = s.getElement(i);
            if(e > 0 && e < length()) {
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
    public double get(int i) {
        return vec[i];
    }

    @Override
    public Double256Vector with(int i, double e) {
        double[] res = vec.clone();
        res[i] = e;
        return new Double256Vector(res);
    }

    // Species

    @Override
    public Double256Species species() {
        return SPECIES;
    }

    static final class Double256Species extends DoubleSpecies<Shapes.S256Bit> {
        static final int BIT_SIZE = Shapes.S_256_BIT.bitSize();

        static final int LENGTH = BIT_SIZE / Double.SIZE;

        @Override
        public String toString() {
           StringBuilder sb = new StringBuilder("Shape[");
           sb.append(bitSize()).append(" bits, ");
           sb.append(length()).append(" ").append(double.class.getSimpleName()).append("s x ");
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
        public Class<Double> elementType() {
            return Double.class;
        }

        @Override
        public int elementSize() {
            return Double.SIZE;
        }

        @Override
        public Shapes.S256Bit shape() {
            return Shapes.S_256_BIT;
        }

        @Override
        Double256Vector op(FOp f) {
            double[] res = new double[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return new Double256Vector(res);
        }

        @Override
        Double256Vector op(Mask<Double, Shapes.S256Bit> m, FOp f) {
            double[] res = new double[length()];
            for (int i = 0; i < length(); i++) {
                if (m.getElement(i)) {
                    res[i] = f.apply(i);
                }
            }
            return new Double256Vector(res);
        }

        // Factories

        @Override
        public Double256Vector zero() {
            return ZERO;
        }
    }
}
