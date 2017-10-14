/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
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
package javax.arrays.v2;

import javax.arrays.tbd.Complex;
import javax.arrays.tbd.DComplex;
import static javax.arrays.v2.RowMatrix.CXinL.CXtoLong;

/**
 * A matrix in which adjacent elements in the same column are also likely to be
 * adjacent in memory.
 *
 * (The current implementation limits the number of rows and columns to be less
 * than or equal to 2-to-the-31 minus 1; that is, Java arrays are used
 * internally.)
 *
 * @param <T>
 */
public class ColumnMatrix<T> extends AbstractMatrix.ColumnMajor<T> {

    // Only public to experiment with kernels.
    public final T[][] blocks;

    public static <T> ColumnMatrix<T> make(Class<T> elt_class, long m, long n) {
        if (m < Integer.MAX_VALUE && n < Integer.MAX_VALUE) {
            return new ColumnMatrix<>(elt_class, (int) m, (int) n);
        } else {
            throw new Error("long-indexed matrices not yet");
        }
    }

    @SuppressWarnings("unchecked")
    public ColumnMatrix(Class<T> elt_class, int n_rows, int n_columns) {
        super(elt_class, n_rows, n_columns);
        T[] first = (T[]) java.lang.reflect.Array.newInstance(elt_class, n_rows);
        this.blocks = (T[][]) java.lang.reflect.Array.newInstance(first.getClass(), n_columns);
        if (n_columns > 0) {
            blocks[0] = first;
            for (int i = 1; i < n_columns; i++) {
                blocks[i] = (T[]) java.lang.reflect.Array.newInstance(elt_class, n_rows);
            }
        }
    }

    @Override
    public T get(long i, long j) {
        return blocks[(int) j][(int) i];
    }

    @Override
    public void set(long i, long j, T x) {
        blocks[(int) j][(int) i] = x;
    }

    @Override
    public boolean cas(long i, long j, T expected, T replacement) {
        T[] a = blocks[(int) j];
        checkValidIndex(i, a.length);
        long l = i * ImplPrivate.ARRAY_OBJECT_INDEX_SCALE +
                       ImplPrivate.ARRAY_OBJECT_BASE_OFFSET;

        return ImplPrivate.u.compareAndSetObject(a, l,
                expected, replacement);
    }

    /**
     * Column major implementation of Double matrix stored flat within arrays of
     * doubles.
     */
    public static class D extends AbstractMatrix.ColumnMajor<Double> {

        public final double[][] blocks;

        public static ColumnMajor<Double> make(long m, long n) {
            if (m < Integer.MAX_VALUE && n < Integer.MAX_VALUE) {
                return new ColumnMatrix.D((int) m, (int) n);
            } else {
                throw new Error("long-indexed matrices not yet");
            }
        }

        public D(Matrix<Double> other) {
            this((int) other.length(ROW_DIM), (int) other.length(COL_DIM));
            this.set(other);
        }

        public D(int n_rows, int n_columns) {
            super(Double.class, n_rows, n_columns);
            this.blocks = new double[n_columns][n_rows];
        }

        public D(double[][] blocks) {
            super(Double.class, blocks.length > 0 ? blocks[0].length : 0, blocks.length);
            this.blocks = blocks;
            for (int i = 1; i < nCols; i++) {
                if (blocks[i].length != nRows) {
                    throw new IllegalArgumentException("Java matrix must be uniform");
                }
            }
        }

        @Override
        public Double get(long i, long j) {
            return blocks[(int) j][(int) i];
        }

        public double getValue(long i, long j) {
            return blocks[(int) j][(int) i];
        }

        @Override
        public void set(long i, long j, Double x) {
            blocks[(int) j][(int) i] = x;
        }

        @Override
        public boolean cas(long i, long j, Double expected, Double replacement) {
        double[] a = blocks[(int) j];
            checkValidIndex(i, a.length);
        long l = i * ImplPrivate.ARRAY_LONG_INDEX_SCALE +
                       ImplPrivate.ARRAY_LONG_BASE_OFFSET;

        return ImplPrivate.u.compareAndSetLong(a, l,
                Double.doubleToRawLongBits(expected), Double.doubleToRawLongBits(replacement));
        }

        public void putValue(long i, long j, double x) {
            blocks[(int) j][(int) i] = x;
        }
    }

    /**
     * Column major implementation of Long matrix stored flat within arrays of
     * doubles.
     */
    public static class L extends AbstractMatrix.ColumnMajor<Long> {

        private final long[][] blocks;

        public static ColumnMajor<Long> make(long m, long n) {
            if (m < Integer.MAX_VALUE && n < Integer.MAX_VALUE) {
                return new ColumnMatrix.L((int) m, (int) n);
            } else {
                throw new Error("long-indexed matrices not yet");
            }
        }

        public L(Matrix<Long> other) {
            this((int) other.length(ROW_DIM), (int) other.length(COL_DIM));
            this.set(other);
        }

        public L(int n_rows, int n_columns) {
            super(Long.class, n_rows, n_columns);
            this.blocks = new long[n_columns][n_rows];
        }

        @Override
        public Long get(long i, long j) {
            return blocks[(int) j][(int) i];
        }

        public double getValue(long i, long j) {
            return blocks[(int) j][(int) i];
        }

        @Override
        public void set(long i, long j, Long x) {
            blocks[(int) j][(int) i] = x;
        }

        @Override
        public boolean cas(long i, long j, Long expected, Long replacement) {
            long[] a = blocks[(int) j];
            checkValidIndex(i, a.length);
            long l = i * ImplPrivate.ARRAY_LONG_INDEX_SCALE
                    + ImplPrivate.ARRAY_LONG_BASE_OFFSET;

            return ImplPrivate.u.compareAndSetLong(a, l, expected, replacement);
        }

        public void putValue(long i, long j, long x) {
            blocks[(int) j][(int) i] = x;
        }
    }

    /**
     * Column major implementation of Complex matrix stored flat within arrays
     * of paired floats.
     */
    public static class CXinFF extends AbstractMatrix.ColumnMajor<Complex> {

        private final float[][] blocks;

        public static ColumnMajor<Complex> make(long m, long n) {
            if (m < Integer.MAX_VALUE/2 && n < Integer.MAX_VALUE) {
                return new ColumnMatrix.CXinFF((int) m, (int) n);
            } else {
                throw new Error("long-indexed matrices not yet");
            }
        }

        public CXinFF(Matrix<Complex> other) {
            this((int) other.length(ROW_DIM), (int) other.length(COL_DIM));
            this.set(other);
        }

        public CXinFF(int n_rows, int n_columns) {
            super(Complex.class, n_rows, n_columns);
            this.blocks = new float[n_columns][2 * n_rows];
        }

        public CXinFF(float[][] blocks) {
            super(Complex.class, blocks.length > 0 ? blocks[0].length / 2 : 0, blocks.length);
            this.blocks = blocks;
            for (int i = 0; i < nCols; i++) {
                if (blocks[i].length != 2 * nRows) {
                    throw new IllegalArgumentException("Java matrix must be uniform");
                }
            }
        }

        @Override
        public Complex get(long i, long j) {
            float[] f = blocks[(int) j];
            float re = f[2 * (int) i];
            float im = f[2 * (int) i + 1];
            return Complex.valueOf(re, im);
        }

        @Override
        public void set(long i, long j, Complex x) {
            float[] f = blocks[(int) j];
            f[2 * (int) i] = x.re;
            f[2 * (int) i + 1] = x.im;
        }

        @Override
        public boolean cas(long i, long j, Complex expected, Complex replacement) {
            throw new UnsupportedOperationException("Compare and swap of pair of floats");
        }
    }

    /**
     * Column major implementation of Complex matrix stored flat within arrays
     * of longs, as bits-for-pairs-of-floats.
     */
    public static class CXinL extends AbstractMatrix.ColumnMajor<Complex> {

        private final long[][] blocks;

        public static ColumnMajor<Complex> make(long m, long n) {
            if (m < Integer.MAX_VALUE && n < Integer.MAX_VALUE) {
                return new ColumnMatrix.CXinL((int) m, (int) n);
            } else {
                throw new Error("long-indexed matrices not yet");
            }
        }

        public CXinL(Matrix<Complex> other) {
            this((int) other.length(ROW_DIM), (int) other.length(COL_DIM));
            this.set(other);
        }

        public CXinL(int n_rows, int n_columns) {
            super(Complex.class, n_rows, n_columns);
            this.blocks = new long[n_columns][n_rows];
        }

        public CXinL(long[][] blocks) {
            super(Complex.class, blocks.length > 0 ? blocks[0].length : 0, blocks.length);
            this.blocks = blocks;

            for (int i = 0; i < nCols; i++) {
                if (blocks[i].length != nRows) {
                    throw new IllegalArgumentException("Java matrix must be uniform");
                }
            }
        }

        @Override
        public Complex get(long i, long j) {
            long[] f = blocks[(int) j];
            long l = f[(int) i];
            float re = Float.intBitsToFloat((int) (l >>> 32));
            float im = Float.intBitsToFloat((int) l);
            return Complex.valueOf(re, im);
        }

        @Override
        public void set(long i, long j, Complex x) {
            long[] f = blocks[(int) j];
            long l = CXtoLong(x);
            f[(int) i] = l;
        }

         static long CXtoLong(Complex x) {
            return ((long) Float.floatToRawIntBits(x.re) << 32)
                    + (0xffffffffL & (long) Float.floatToRawIntBits(x.im));
        }

        @Override
        public boolean cas(long i, long j, Complex expected, Complex replacement) {
            long[] a = blocks[(int) j];
            checkValidIndex(i, a.length);
            long l = i * ImplPrivate.ARRAY_LONG_INDEX_SCALE
                    + ImplPrivate.ARRAY_LONG_BASE_OFFSET;

            long expected_long = CXtoLong(expected);
            long replacement_long = CXtoLong(replacement);

            return ImplPrivate.u.compareAndSetLong(a, l, expected_long, replacement_long);
        }
    }

    /**
     * Column major implementation of DComplex matrix stored flat within arrays
     * of doubles.
     */
    public static class DX extends AbstractMatrix.ColumnMajor<DComplex> {

        private final double[][] blocks;

         public static ColumnMajor<DComplex> make(long m, long n) {
            if (m < Integer.MAX_VALUE/2 && n < Integer.MAX_VALUE) {
                return new ColumnMatrix.DX((int) m, (int) n);
            } else {
                throw new Error("long-indexed matrices not yet");
            }
        }

       public DX(Matrix<DComplex> other) {
            this((int) other.length(ROW_DIM), (int) other.length(COL_DIM));
            this.set(other);
        }

        public DX(int n_rows, int n_columns) {
            super(DComplex.class, n_rows, n_columns);
            this.blocks = new double[n_columns][2 * n_rows];
        }

        public DX(double[][] blocks) {
            super(DComplex.class, blocks.length, blocks.length > 0 ? blocks[0].length / 2 : 0);
            this.blocks = blocks;
            for (int i = 0; i < nCols; i++) {
                if (blocks[i].length != 2 * nRows) {
                    throw new IllegalArgumentException("Java matrix must be uniform");
                }
            }
        }

        @Override
        public DComplex get(long i, long j) {
            double[] f = blocks[(int) j];
            double re = f[2 * (int) i];
            double im = f[2 * (int) i + 1];
            return DComplex.valueOf(re, im);
        }

        @Override
        public void set(long i, long j, DComplex x) {
            double[] f = blocks[(int) j];
            f[2 * (int) i] = x.re;
            f[2 * (int) i + 1] = x.im;
        }

        @Override
        public boolean cas(long i, long j, DComplex expected, DComplex replacement) {
            throw new UnsupportedOperationException("Compare and swap of pair of doubles");
        }
    }

    public static class F extends AbstractMatrix.ColumnMajor<Float> {

        private final float[][] blocks;

        public static ColumnMajor<Float> make(long m, long n) {
            if (m < Integer.MAX_VALUE && n < Integer.MAX_VALUE) {
                return new ColumnMatrix.F((int) m, (int) n);
            } else {
                throw new Error("long-indexed matrices not yet");
            }
        }

        public F(Matrix<Float> other) {
            this((int) other.length(ROW_DIM), (int) other.length(COL_DIM));
            this.set(other);
        }

        public F(int n_rows, int n_columns) {
            super(Float.class, n_rows, n_columns);
            this.blocks = new float[n_columns][n_rows];
        }

        public F(float[][] blocks) {
            super(Float.class, blocks.length > 0 ? blocks[0].length : 0, blocks.length);
            this.blocks = blocks;
            for (int i = 1; i < nCols; i++) {
                if (blocks[i].length != nRows) {
                    throw new IllegalArgumentException("Java matrix must be uniform");
                }
            }
        }

        @Override
        public Float get(long i, long j) {
            return blocks[(int) j][(int) i];
        }

        public float getValue(long i, long j) {
            return blocks[(int) j][(int) i];
        }

        @Override
        public void set(long i, long j, Float x) {
            blocks[(int) j][(int) i] = x;
        }

        public void putValue(long i, long j, float x) {
            blocks[(int) j][(int) i] = x;
        }

       @Override
        public boolean cas(long i, long j, Float expected, Float replacement) {
            float[] a = blocks[(int) j];
            checkValidIndex(i, a.length);
            long l = i * ImplPrivate.ARRAY_FLOAT_INDEX_SCALE
                    + ImplPrivate.ARRAY_FLOAT_BASE_OFFSET;

            int expected_int = Float.floatToRawIntBits(expected);
            int replacement_int = Float.floatToRawIntBits(replacement);

            return ImplPrivate.u.compareAndSetInt(a, l, expected_int, replacement_int);
        }
    }

    public static class I extends AbstractMatrix.ColumnMajor<Integer> {

        private final int[][] blocks;

        public static ColumnMajor<Integer> make(long m, long n) {
            if (m < Integer.MAX_VALUE && n < Integer.MAX_VALUE) {
                return new ColumnMatrix.I((int) m, (int) n);
            } else {
                throw new Error("long-indexed matrices not yet");
            }
        }

        public I(Matrix<Integer> other) {
            this((int) other.length(ROW_DIM), (int) other.length(COL_DIM));
            this.set(other);
        }

        public I(int n_rows, int n_columns) {
            super(Integer.class, n_rows, n_columns);
            this.blocks = new int[n_columns][n_rows];
        }

        public I(int[][] blocks) {
            super(Integer.class, blocks.length > 0 ? blocks[0].length : 0, blocks.length);
            this.blocks = blocks;
            for (int i = 1; i < nCols; i++) {
                if (blocks[i].length != nRows) {
                    throw new IllegalArgumentException("Java matrix must be uniform");
                }
            }
        }

        @Override
        public Integer get(long i, long j) {
            return blocks[(int) j][(int) i];
        }

        public int getValue(long i, long j) {
            return blocks[(int) j][(int) i];
        }

        @Override
        public void set(long i, long j, Integer x) {
            blocks[(int) j][(int) i] = x;
        }

        public void putValue(long i, long j, int x) {
            blocks[(int) j][(int) i] = x;
        }

      @Override
        public boolean cas(long i, long j, Integer expected, Integer replacement) {
            int[] a = blocks[(int) j];
            checkValidIndex(i, a.length);
            long l =  i * ImplPrivate.ARRAY_INT_INDEX_SCALE
                    + ImplPrivate.ARRAY_INT_BASE_OFFSET;

            return ImplPrivate.u.compareAndSetInt(a, l, expected, replacement);
        }
    }

    public static class S extends AbstractMatrix.ColumnMajor<Short> {

        private final short[][] blocks;

        public static ColumnMajor<Short> make(long m, long n) {
            if (m < Integer.MAX_VALUE && n < Integer.MAX_VALUE) {
                return new ColumnMatrix.S((int) m, (int) n);
            } else {
                throw new Error("long-indexed matrices not yet");
            }
        }

        public S(Matrix<Short> other) {
            this((int) other.length(ROW_DIM), (int) other.length(COL_DIM));
            this.set(other);
        }

        public S(int n_rows, int n_columns) {
            super(Short.class, n_rows, n_columns);
            this.blocks = new short[n_columns][n_rows];
        }

        public S(short[][] blocks) {
            super(Short.class, blocks.length > 0 ? blocks[0].length : 0, blocks.length);
            this.blocks = blocks;
            for (int i = 1; i < nCols; i++) {
                if (blocks[i].length != nRows) {
                    throw new IllegalArgumentException("Java matrix must be uniform");
                }
            }
        }

        @Override
        public Short get(long i, long j) {
            return blocks[(int) j][(int) i];
        }

        public short getValue(long i, long j) {
            return blocks[(int) j][(int) i];
        }

        @Override
        public void set(long i, long j, Short x) {
            blocks[(int) j][(int) i] = x;
        }

        public void putValue(long i, long j, short x) {
            blocks[(int) j][(int) i] = x;
        }
        @Override
        public boolean cas(long i, long j, Short expected, Short replacement) {
            short[] a = blocks[(int) j];
            checkValidIndex(i, a.length);
            long l =  i * ImplPrivate.ARRAY_SHORT_INDEX_SCALE
                    + ImplPrivate.ARRAY_SHORT_BASE_OFFSET;

            int atByte = (int) l & 3;
            l = l ^ atByte;

            int existing_int = ImplPrivate.u.getInt(a, l);
            int expected_int = ImplPrivate.inserter.insertShort(atByte, existing_int, expected);
            int replacement_int = ImplPrivate.inserter.insertShort(atByte, existing_int, replacement);

            return ImplPrivate.u.compareAndSetInt(a, l, expected_int, replacement_int);
        }
    }

    public static class B extends AbstractMatrix.ColumnMajor<Byte> {

        private final byte[][] blocks;

        public static ColumnMajor<Byte> make(long m, long n) {
            if (m < Integer.MAX_VALUE && n < Integer.MAX_VALUE) {
                return new ColumnMatrix.B((int) m, (int) n);
            } else {
                throw new Error("long-indexed matrices not yet");
            }
        }

        public B(Matrix<Byte> other) {
            this((int) other.length(ROW_DIM), (int) other.length(COL_DIM));
            this.set(other);
        }

        public B(int n_rows, int n_columns) {
            super(Byte.class, n_rows, n_columns);
            this.blocks = new byte[n_columns][n_rows];
        }

        public B(byte[][] blocks) {
            super(Byte.class, blocks.length > 0 ? blocks[0].length : 0, blocks.length);
            this.blocks = blocks;
            for (int i = 1; i < nCols; i++) {
                if (blocks[i].length != nRows) {
                    throw new IllegalArgumentException("Java matrix must be uniform");
                }
            }
        }

        @Override
        public Byte get(long i, long j) {
            return blocks[(int) j][(int) i];
        }

        public byte getValue(long i, long j) {
            return blocks[(int) j][(int) i];
        }

        @Override
        public void set(long i, long j, Byte x) {
            blocks[(int) j][(int) i] = x;
        }

        public void putValue(long i, long j, byte x) {
            blocks[(int) j][(int) i] = x;
        }
        @Override
        public boolean cas(long i, long j, Byte expected, Byte replacement) {
            byte[] a = blocks[(int) j];
            checkValidIndex(i, a.length);
            long l = i * ImplPrivate.ARRAY_BYTE_INDEX_SCALE
                    + ImplPrivate.ARRAY_BYTE_BASE_OFFSET;

            int atByte = (int) l & 3;
            l = l ^ atByte;

            int existing_int = ImplPrivate.u.getInt(a, l);
            int expected_int = ImplPrivate.inserter.insertByte(atByte, existing_int, expected);
            int replacement_int = ImplPrivate.inserter.insertByte(atByte, existing_int, replacement);

            return ImplPrivate.u.compareAndSetInt(a, l, expected_int, replacement_int);
        }
    }
}
