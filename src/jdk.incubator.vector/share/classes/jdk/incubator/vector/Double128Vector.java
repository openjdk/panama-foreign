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
import java.util.Objects;
import jdk.internal.HotSpotIntrinsicCandidate;
import jdk.internal.vm.annotation.ForceInline;
import static jdk.incubator.vector.VectorIntrinsics.*;

@SuppressWarnings("cast")
final class Double128Vector extends DoubleVector<Shapes.S128Bit> {
    static final Double128Species SPECIES = new Double128Species();

    static final Double128Vector ZERO = new Double128Vector();

    static final int LENGTH = SPECIES.length();

    double[] vec;

    Double128Vector() {
        vec = new double[SPECIES.length()];
    }

    Double128Vector(double[] v) {
        vec = v;
    }

    @Override
    public int length() { return LENGTH; }

    // Unary operator

    @Override
    Double128Vector uOp(FUnOp f) {
        double[] res = new double[length()];
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i]);
        }
        return new Double128Vector(res);
    }

    @Override
    Double128Vector uOp(Mask<Double, Shapes.S128Bit> o, FUnOp f) {
        double[] res = new double[length()];
        Double128Mask m = (Double128Mask) o;
        for (int i = 0; i < length(); i++) {
            res[i] = m.bits[i] ? f.apply(i, vec[i]) : vec[i];
        }
        return new Double128Vector(res);
    }

    // Binary operator

    @Override
    Double128Vector bOp(Vector<Double, Shapes.S128Bit> o, FBinOp f) {
        double[] res = new double[length()];
        Double128Vector v = (Double128Vector) o;
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i], v.vec[i]);
        }
        return new Double128Vector(res);
    }

    @Override
    Double128Vector bOp(Vector<Double, Shapes.S128Bit> o1, Mask<Double, Shapes.S128Bit> o2, FBinOp f) {
        double[] res = new double[length()];
        Double128Vector v = (Double128Vector) o1;
        Double128Mask m = (Double128Mask) o2;
        for (int i = 0; i < length(); i++) {
            res[i] = m.bits[i] ? f.apply(i, vec[i], v.vec[i]) : vec[i];
        }
        return new Double128Vector(res);
    }

    // Trinary operator

    @Override
    Double128Vector tOp(Vector<Double, Shapes.S128Bit> o1, Vector<Double, Shapes.S128Bit> o2, FTriOp f) {
        double[] res = new double[length()];
        Double128Vector v1 = (Double128Vector) o1;
        Double128Vector v2 = (Double128Vector) o2;
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i], v1.vec[i], v2.vec[i]);
        }
        return new Double128Vector(res);
    }

    @Override
    Double128Vector tOp(Vector<Double, Shapes.S128Bit> o1, Vector<Double, Shapes.S128Bit> o2, Mask<Double, Shapes.S128Bit> o3, FTriOp f) {
        double[] res = new double[length()];
        Double128Vector v1 = (Double128Vector) o1;
        Double128Vector v2 = (Double128Vector) o2;
        Double128Mask m = (Double128Mask) o3;
        for (int i = 0; i < length(); i++) {
            res[i] = m.bits[i] ? f.apply(i, vec[i], v1.vec[i], v2.vec[i]) : vec[i];
        }
        return new Double128Vector(res);
    }

    @Override
    double rOp(double v, FBinOp f) {
        for (int i = 0; i < length(); i++) {
            v = f.apply(i, v, vec[i]);
        }
        return v;
    }

    // Binary operations

    @Override
    @ForceInline
    public Double128Vector add(Vector<Double,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Double128Vector v = (Double128Vector)o;
        return (Double128Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_ADD, Double128Vector.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> ((Double128Vector)v1).bOp(v2, (i, a, b) -> (double)(a + b)));
    }

    @Override
    @ForceInline
    public Double128Vector sub(Vector<Double,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Double128Vector v = (Double128Vector)o;
        return (Double128Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_SUB, Double128Vector.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> ((Double128Vector)v1).bOp(v2, (i, a, b) -> (double)(a - b)));
    }

    @Override
    @ForceInline
    public Double128Vector mul(Vector<Double,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Double128Vector v = (Double128Vector)o;
        return (Double128Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MUL, Double128Vector.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> ((Double128Vector)v1).bOp(v2, (i, a, b) -> (double)(a * b)));
    }


    @Override
    @ForceInline
    public Double128Vector div(Vector<Double,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Double128Vector v = (Double128Vector)o;
        return (Double128Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_DIV, Double128Vector.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> ((Double128Vector)v1).bOp(v2, (i, a, b) -> (double)(a / b)));
    }


    // Type specific horizontal reductions

    @Override
    @ForceInline
    public double addAll() {
        long bits = (long) VectorIntrinsics.reductionCoerced(
                                VECTOR_OP_ADD, Double128Vector.class, double.class, LENGTH,
                                this,
                                v -> {
                                    double r = v.rOp((double) 0, (i, a, b) -> (double) (a + b));
                                    return (long)Double.doubleToLongBits(r);
                                });
        return Double.longBitsToDouble(bits);
    }

    @Override
    @ForceInline
    public double mulAll() {
        long bits = (long) VectorIntrinsics.reductionCoerced(
                                VECTOR_OP_MUL, Double128Vector.class, double.class, LENGTH,
                                this,
                                v -> {
                                    double r = v.rOp((double) 0, (i, a, b) -> (double) (a * b));
                                    return (long)Double.doubleToLongBits(r);
                                });
        return Double.longBitsToDouble(bits);
    }

    // Memory operations

    @Override
    @ForceInline
    public void intoArray(double[] a, int ix) {
        Objects.requireNonNull(a);
        if (VectorIntrinsics.VECTOR_ACCESS_OOB_CHECK) {
            Objects.checkFromIndexSize(ix, LENGTH, a.length);
        }
        VectorIntrinsics.store(Double128Vector.class, double.class, LENGTH,
                               a, ix, this,
                               (arr, idx, v) -> v.forEach((i, a_) -> ((double[])arr)[idx + i] = a_));
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

        Double128Vector that = (Double128Vector) o;
        return Arrays.equals(vec, that.vec);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    Double128Mask bTest(Vector<Double, Shapes.S128Bit> o, FBinTest f) {
        Double128Vector v = (Double128Vector) o;
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(i, vec[i], v.vec[i]);
        }
        return new Double128Mask(bits);
    }

    // Foreach

    @Override
    void forEach(FUnCon f) {
        for (int i = 0; i < length(); i++) {
            f.apply(i, vec[i]);
        }
    }

    @Override
    void forEach(Mask<Double, Shapes.S128Bit> o, FUnCon f) {
        Double128Mask m = (Double128Mask) o;
        forEach((i, a) -> {
            if (m.bits[i]) { f.apply(i, a); }
        });
    }

    Long128Vector toBits() {
        long[] res = new long[this.species().length()];
        for(int i = 0; i < this.species().length(); i++){
            res[i] = Double.doubleToLongBits(vec[i]);
        }
        return new Long128Vector(res);
    }


    @Override
    public Double128Vector rotateEL(int j) {
        double[] res = new double[length()];
        for (int i = 0; i < length(); i++){
            res[j + i % length()] = vec[i];
        }
        return new Double128Vector(res);
    }

    @Override
    public Double128Vector rotateER(int j) {
        double[] res = new double[length()];
        for (int i = 0; i < length(); i++){
            int z = i - j;
            if(j < 0) {
                res[length() + z] = vec[i];
            } else {
                res[z] = vec[i];
            }
        }
        return new Double128Vector(res);
    }

    @Override
    public Double128Vector shiftEL(int j) {
        double[] res = new double[length()];
        for (int i = 0; i < length() - j; i++) {
            res[i] = vec[i + j];
        }
        return new Double128Vector(res);
    }

    @Override
    public Double128Vector shiftER(int j) {
        double[] res = new double[length()];
        for (int i = 0; i < length() - j; i++){
            res[i + j] = vec[i];
        }
        return new Double128Vector(res);
    }

    @Override
    public Double128Vector shuffle(Vector<Double, Shapes.S128Bit> o, Shuffle<Double, Shapes.S128Bit> s) {
        Double128Vector v = (Double128Vector) o;
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
    public Double128Vector swizzle(Shuffle<Double, Shapes.S128Bit> s) {
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
    public double get(int i) {
        return vec[i];
    }

    @Override
    public Double128Vector with(int i, double e) {
        double[] res = vec.clone();
        res[i] = e;
        return new Double128Vector(res);
    }

    // Mask

    static final class Double128Mask extends AbstractMask<Double, Shapes.S128Bit> {
        static final Double128Mask TRUE_MASK = new Double128Mask(true);
        static final Double128Mask FALSE_MASK = new Double128Mask(false);

        public Double128Mask(boolean[] bits) {
            super(bits);
        }

        public Double128Mask(boolean val) {
            super(val);
        }

        @Override
        Double128Mask uOp(MUnOp f) {
            boolean[] res = new boolean[species().length()];
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Double128Mask(res);
        }

        @Override
        Double128Mask bOp(Mask<Double, Shapes.S128Bit> o, MBinOp f) {
            boolean[] res = new boolean[species().length()];
            Double128Mask m = (Double128Mask) o;
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i], m.bits[i]);
            }
            return new Double128Mask(res);
        }

        @Override
        public Double128Species species() {
            return SPECIES;
        }

        @Override
        public Double128Vector toVector() {
            double[] res = new double[species().length()];
            for (int i = 0; i < species().length(); i++) {
                res[i] = (double) (bits[i] ? -1 : 0);
            }
            return new Double128Vector(res);
        }

        // Unary operations

        //Mask<E, S> not();

        // Binary operations

        @Override
        @ForceInline
        public Double128Mask and(Mask<Double,Shapes.S128Bit> o) {
            Objects.requireNonNull(o);
            Double128Mask m = (Double128Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Double128Mask.class, long.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a && b));
        }

        @Override
        @ForceInline
        public Double128Mask or(Mask<Double,Shapes.S128Bit> o) {
            Objects.requireNonNull(o);
            Double128Mask m = (Double128Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, Double128Mask.class, long.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a && b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(COND_notZero, Double128Mask.class, long.class, LENGTH,
                                         this, this,
                                         (m1, m2) -> super.anyTrue());
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(COND_carrySet, Double128Mask.class, long.class, LENGTH,
                                         this, trueMask(),
                                         (m1, m2) -> super.allTrue());
        }

        // Helpers

        @ForceInline
        static Double128Mask trueMask() {
            return Double128Mask.trueMask();
        }

        @ForceInline
        static Double128Mask falseMask() {
            return Double128Mask.falseMask();
        }
    }

    // Species

    @Override
    public Double128Species species() {
        return SPECIES;
    }

    static final class Double128Species extends DoubleSpecies<Shapes.S128Bit> {
        static final int BIT_SIZE = Shapes.S_128_BIT.bitSize();

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
        public Shapes.S128Bit shape() {
            return Shapes.S_128_BIT;
        }

        @Override
        Double128Vector op(FOp f) {
            double[] res = new double[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return new Double128Vector(res);
        }

        @Override
        Double128Vector op(Mask<Double, Shapes.S128Bit> o, FOp f) {
            double[] res = new double[length()];
            Double128Mask m = (Double128Mask) o;
            for (int i = 0; i < length(); i++) {
                if (m.bits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return new Double128Vector(res);
        }

        // Factories

        @Override
        public Double128Mask constantMask(boolean... bits) {
            return new Double128Mask(bits);
        }


        @Override
        @ForceInline
        public Double128Vector zero() {
            return VectorIntrinsics.broadcastCoerced(Double128Vector.class, double.class, LENGTH,
                                                     Double.doubleToLongBits(0.0f),
                                                     (z -> ZERO));
        }

        @Override
        @ForceInline
        public Double128Vector broadcast(double e) {
            return VectorIntrinsics.broadcastCoerced(
                Double128Vector.class, double.class, LENGTH,
                Double.doubleToLongBits(e),
                ((long bits) -> SPECIES.op(i -> Double.longBitsToDouble((long)bits))));
        }

        @HotSpotIntrinsicCandidate
        @Override
        @ForceInline
        public Double128Mask trueMask() {
            return VectorIntrinsics.broadcastCoerced(Double128Mask.class, long.class, LENGTH,
                                                     (long)-1,
                                                     (z -> Double128Mask.TRUE_MASK));
        }

        @HotSpotIntrinsicCandidate
        @Override
        @ForceInline
        public Double128Mask falseMask() {
            return VectorIntrinsics.broadcastCoerced(Double128Mask.class, long.class, LENGTH,
                                                     0,
                                                     (z -> Double128Mask.FALSE_MASK));
        }

        @Override
        @ForceInline
        public Double128Vector fromArray(double[] a, int ix) {
            Objects.requireNonNull(a);
            if (VectorIntrinsics.VECTOR_ACCESS_OOB_CHECK) {
                Objects.checkFromIndexSize(ix, LENGTH, a.length);
            }
            return (Double128Vector) VectorIntrinsics.load(Double128Vector.class, double.class, LENGTH,
                                                        a, ix,
                                                        (arr, idx) -> super.fromArray((double[]) arr, idx));
        }
    }
}
