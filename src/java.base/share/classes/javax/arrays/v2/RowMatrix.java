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

import javax.arrays.tbd.DComplex;
import javax.arrays.tbd.Complex;
import static javax.arrays.v2.Matrix.ROW_DIM;

/**
 * A matrix in which adjacent elements in the same row are also likely to be
 * adjacent in memory.
 *
 * (The current implementation limits the number of rows and columns to be less
 * than or equal to 2-to-the-31 minus 1; that is, Java arrays are used
 * internally.)
 *
 * @param <T>
 */
public class RowMatrix<T> extends AbstractMatrix.RowMajor<T> {

    private final T[][] blocks;

    public static <T> RowMatrix<T> make(Class<T> elt_class, long m, long n) {
        if (m < Integer.MAX_VALUE && n < Integer.MAX_VALUE) {
            return new RowMatrix<>(elt_class, (int) m, (int) n);
        } else {
            throw new Error("long-indexed matrices not yet");
        }
    }

    @SuppressWarnings("unchecked")
    public RowMatrix(Class<T> elt_class, int n_rows, int n_columns) {
        super(elt_class, n_rows, n_columns);
        T[] first = (T[]) java.lang.reflect.Array.newInstance(elt_class, n_columns);
        this.blocks = (T[][]) java.lang.reflect.Array.newInstance(first.getClass(), n_rows);
        if (n_rows > 0) {
            blocks[0] = first;
            for (int i = 1; i < n_rows; i++) {
                blocks[i] = (T[]) java.lang.reflect.Array.newInstance(elt_class, n_columns);
            }
        }
    }

    @Override
    public T get(long i, long j) {
        return blocks[(int) i][(int) j];
    }

    @Override
    public void set(long i, long j, T x) {
        blocks[(int) i][(int) j] = x;
    }

    @Override
    public boolean cas(long i, long j, T expected, T replacement) {
        T[] a = blocks[(int) i];
        checkValidIndex(j, a.length);
        long l = j * ImplPrivate.ARRAY_OBJECT_INDEX_SCALE +
                       ImplPrivate.ARRAY_OBJECT_BASE_OFFSET;

        return ImplPrivate.u.compareAndSetObject(a, l,
                expected, replacement);
    }

    /**
     * Row major implementation of Double matrix stored flat within arrays of
     * doubles.
     */
    public static class D extends AbstractMatrix.RowMajor<Double> {

        // Only public to experiment with kernels.
        public final double[][] blocks;

        public static RowMajor<Double> make(long m, long n) {
            if (m < Integer.MAX_VALUE && n < Integer.MAX_VALUE) {
                return new RowMatrix.D((int) m, (int) n);
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
            this.blocks = new double[n_rows][n_columns];
        }

        public D(double[][] blocks) {
            super(Double.class, blocks.length, blocks.length > 0 ? blocks[0].length : 0);
            this.blocks = blocks;
            for (int i = 1; i < nRows; i++) {
                if (blocks[i].length != nCols) {
                    throw new IllegalArgumentException("Java matrix must be uniform");
                }
            }
        }

        @Override
        public Double get(long i, long j) {
            return blocks[(int) i][(int) j];
        }

        public double getValue(long i, long j) {
            return blocks[(int) i][(int) j];
        }

        @Override
        public void set(long i, long j, Double x) {
            blocks[(int) i][(int) j] = x;
        }

        @Override
        public boolean cas(long i, long j, Double expected, Double replacement) {
        double[] a = blocks[(int) i];
        checkValidIndex(j, a.length);
        long l = j * ImplPrivate.ARRAY_LONG_INDEX_SCALE +
                       ImplPrivate.ARRAY_LONG_BASE_OFFSET;

        return ImplPrivate.u.compareAndSetLong(a, l,
                Double.doubleToRawLongBits(expected), Double.doubleToRawLongBits(replacement));
        }

        public void putValue(long i, long j, double x) {
            blocks[(int) i][(int) j] = x;
        }
    }

    /**
     * Row major implementation of Long matrix stored flat within arrays of
     * doubles.
     */
    public static class L extends AbstractMatrix.RowMajor<Long> {

        private final long[][] blocks;

        public static RowMajor<Long> make(long m, long n) {
            if (m < Integer.MAX_VALUE && n < Integer.MAX_VALUE) {
                return new RowMatrix.L((int) m, (int) n);
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
            this.blocks = new long[n_rows][n_columns];
        }

        @Override
        public Long get(long i, long j) {
            return blocks[(int) i][(int) j];
        }

        public double getValue(long i, long j) {
            return blocks[(int) i][(int) j];
        }

        @Override
        public void set(long i, long j, Long x) {
            blocks[(int) i][(int) j] = x;
        }

        @Override
        public boolean cas(long i, long j, Long expected, Long replacement) {
            long[] a = blocks[(int) i];
            checkValidIndex(j, a.length);
            long l = j * ImplPrivate.ARRAY_LONG_INDEX_SCALE
                    + ImplPrivate.ARRAY_LONG_BASE_OFFSET;

            return ImplPrivate.u.compareAndSetLong(a, l, expected, replacement);
        }

        public void putValue(long i, long j, long x) {
            blocks[(int) i][(int) j] = x;
        }
    }

    /**
     * Row major implementation of Complex matrix stored flat within arrays of
     * paired floats.
     */
    public static class CXinFF extends AbstractMatrix.RowMajor<Complex> {

        private final float[][] blocks;

        public static RowMajor<Complex> make(long m, long n) {
            if (m < Integer.MAX_VALUE && n < Integer.MAX_VALUE / 2) {
                return new RowMatrix.CXinFF((int) m, (int) n);
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
            this.blocks = new float[n_rows][2 * n_columns];

        }

        public CXinFF(float[][] blocks) {
            super(Complex.class, blocks.length, blocks.length > 0 ? blocks[0].length / 2 : 0);
            this.blocks = blocks;
            for (int i = 0; i < nRows; i++) {
                if (blocks[i].length != 2 * nCols) {
                    throw new IllegalArgumentException("Java matrix must be uniform");
                }
            }
        }

        @Override
        public Complex get(long i, long j) {
            float[] f = blocks[(int) i];
            float re = f[2 * (int) j];
            float im = f[2 * (int) j + 1];
            return Complex.valueOf(re, im);
        }

        @Override
        public void set(long i, long j, Complex x) {
            float[] f = blocks[(int) i];
            f[2 * (int) j] = x.re;
            f[2 * (int) j + 1] = x.im;
        }

        @Override
        public boolean cas(long i, long j, Complex expected, Complex replacement) {
            throw new UnsupportedOperationException("Compare and swap of pair of floats");
        }

    }

    /**
     * Row major implementation of Complex matrix stored flat within arrays of
     * longs, as bits-for-pairs-of-floats.
     */
    public static class CXinL extends AbstractMatrix.RowMajor<Complex> {

        private final long[][] blocks;

        public static RowMajor<Complex> make(long m, long n) {
            if (m < Integer.MAX_VALUE && n < Integer.MAX_VALUE) {
                return new RowMatrix.CXinL((int) m, (int) n);
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
            this.blocks = new long[n_rows][n_columns];
        }

        public CXinL(long[][] blocks) {
            super(Complex.class, blocks.length, blocks.length > 0 ? blocks[0].length : 0);
            this.blocks = blocks;

            for (int i = 0; i < nRows; i++) {
                if (blocks[i].length != nCols) {
                    throw new IllegalArgumentException("Java matrix must be uniform");
                }
            }
        }

        @Override
        public Complex get(long i, long j) {
            long[] f = blocks[(int) i];
            long l = f[(int) j];
            float re = Float.intBitsToFloat((int) (l >>> 32));
            float im = Float.intBitsToFloat((int) l);
            return Complex.valueOf(re, im);
        }

        @Override
        public void set(long i, long j, Complex x) {
            long[] f = blocks[(int) i];
            long l = CXtoLong(x);
            f[(int) j] = l;
        }

        static long CXtoLong(Complex x) {
            return ((long) Float.floatToRawIntBits(x.re) << 32)
                    + (0xffffffffL & (long) Float.floatToRawIntBits(x.im));
        }

        @Override
        public boolean cas(long i, long j, Complex expected, Complex replacement) {
            long[] a = blocks[(int) i];
            checkValidIndex(j, a.length);
            long l = j * ImplPrivate.ARRAY_LONG_INDEX_SCALE
                    + ImplPrivate.ARRAY_LONG_BASE_OFFSET;

            long expected_long = CXtoLong(expected);
            long replacement_long = CXtoLong(replacement);

            return ImplPrivate.u.compareAndSetLong(a, l, expected_long, replacement_long);
        }

    }

    /**
     * Row major implementation of DComplex matrix stored flat within arrays of
     * doubles.
     */
    public static class DX extends AbstractMatrix.RowMajor<DComplex> {

        private final double[][] blocks;

        public static RowMajor<DComplex> make(long m, long n) {
            if (m < Integer.MAX_VALUE && n < Integer.MAX_VALUE / 2) {
                return new RowMatrix.DX((int) m, (int) n);
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
            this.blocks = new double[n_rows][2 * n_columns];
        }

        public DX(double[][] blocks) {
            super(DComplex.class, blocks.length, blocks.length > 0 ? blocks[0].length / 2 : 0);
            this.blocks = blocks;
            for (int i = 0; i < nRows; i++) {
                if (blocks[i].length != 2 * nCols) {
                    throw new IllegalArgumentException("Java matrix must be uniform");
                }
            }
        }

        @Override
        public DComplex get(long i, long j) {
            double[] f = blocks[(int) i];
            int jj = 2 * (int) j;
            int jj1 = jj + 1;
            double re = f[jj];
            double im = f[jj1];
            return DComplex.valueOf(re, im);
        }

        @Override
        public void set(long i, long j, DComplex x) {
            double[] f = blocks[(int) i];
            f[2 * (int) j] = x.re;
            f[2 * (int) j + 1] = x.im;
        }

        @Override
        public boolean cas(long i, long j, DComplex expected, DComplex replacement) {
            throw new UnsupportedOperationException("Compare and swap of pair of doubles");
        }
    }

    public static class F extends AbstractMatrix.RowMajor<Float> {

        private final float[][] blocks;

        public static RowMajor<Float> make(long m, long n) {
            if (m < Integer.MAX_VALUE && n < Integer.MAX_VALUE) {
                return new RowMatrix.F((int) m, (int) n);
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
            this.blocks = new float[n_rows][n_columns];
        }

        public F(float[][] blocks) {
            super(Float.class, blocks.length, blocks.length > 0 ? blocks[0].length : 0);
            this.blocks = blocks;
            for (int i = 1; i < nRows; i++) {
                if (blocks[i].length != nCols) {
                    throw new IllegalArgumentException("Java matrix must be uniform");
                }
            }
        }

        @Override
        public Float get(long i, long j) {
            return blocks[(int) i][(int) j];
        }

        public float getValue(long i, long j) {
            return blocks[(int) i][(int) j];
        }

        @Override
        public void set(long i, long j, Float x) {
            blocks[(int) i][(int) j] = x;
        }

        public void putValue(long i, long j, float x) {
            blocks[(int) i][(int) j] = x;
        }

        @Override
        public boolean cas(long i, long j, Float expected, Float replacement) {
            float[] a = blocks[(int) i];
            checkValidIndex(j, a.length);
            long l = j * ImplPrivate.ARRAY_FLOAT_INDEX_SCALE
                    + ImplPrivate.ARRAY_FLOAT_BASE_OFFSET;

            int expected_int = Float.floatToRawIntBits(expected);
            int replacement_int = Float.floatToRawIntBits(replacement);

            return ImplPrivate.u.compareAndSetInt(a, l, expected_int, replacement_int);
        }
    }

    public static class I extends AbstractMatrix.RowMajor<Integer> {

        private final int[][] blocks;

        public static RowMajor<Integer> make(long m, long n) {
            if (m < Integer.MAX_VALUE && n < Integer.MAX_VALUE) {
                return new RowMatrix.I((int) m, (int) n);
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
            this.blocks = new int[n_rows][n_columns];
        }

        public I(int[][] blocks) {
            super(Integer.class, blocks.length, blocks.length > 0 ? blocks[0].length : 0);
            this.blocks = blocks;
            for (int i = 1; i < nRows; i++) {
                if (blocks[i].length != nCols) {
                    throw new IllegalArgumentException("Java matrix must be uniform");
                }
            }
        }

        @Override
        public Integer get(long i, long j) {
            return blocks[(int) i][(int) j];
        }

        public int getValue(long i, long j) {
            return blocks[(int) i][(int) j];
        }

        @Override
        public void set(long i, long j, Integer x) {
            blocks[(int) i][(int) j] = x;
        }

        public void putValue(long i, long j, int x) {
            blocks[(int) i][(int) j] = x;
        }

       @Override
        public boolean cas(long i, long j, Integer expected, Integer replacement) {
            int[] a = blocks[(int) i];
            checkValidIndex(j, a.length);
            long l = j * ImplPrivate.ARRAY_INT_INDEX_SCALE
                    + ImplPrivate.ARRAY_INT_BASE_OFFSET;

            return ImplPrivate.u.compareAndSetInt(a, l, expected, replacement);
        }
    }

    public static class S extends AbstractMatrix.RowMajor<Short> {

        private final short[][] blocks;

        public static RowMajor<Short> make(long m, long n) {
            if (m < Integer.MAX_VALUE && n < Integer.MAX_VALUE) {
                return new RowMatrix.S((int) m, (int) n);
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
            this.blocks = new short[n_rows][n_columns];
        }

        public S(short[][] blocks) {
            super(Short.class, blocks.length, blocks.length > 0 ? blocks[0].length : 0);
            this.blocks = blocks;
            for (int i = 1; i < nRows; i++) {
                if (blocks[i].length != nCols) {
                    throw new IllegalArgumentException("Java matrix must be uniform");
                }
            }
        }

        @Override
        public Short get(long i, long j) {
            return blocks[(int) i][(int) j];
        }

        public short getValue(long i, long j) {
            return blocks[(int) i][(int) j];
        }

        @Override
        public void set(long i, long j, Short x) {
            blocks[(int) i][(int) j] = x;
        }

        public void putValue(long i, long j, short x) {
            blocks[(int) i][(int) j] = x;
        }

        @Override
        public boolean cas(long i, long j, Short expected, Short replacement) {
            short[] a = blocks[(int) i];
            checkValidIndex(j, a.length);
            long l = j * ImplPrivate.ARRAY_SHORT_INDEX_SCALE
                    + ImplPrivate.ARRAY_SHORT_BASE_OFFSET;

            int atByte = (int) l & 3;
            l = l ^ atByte;

            int existing_int = ImplPrivate.u.getInt(a, l);
            int expected_int = ImplPrivate.inserter.insertShort(atByte, existing_int, expected);
            int replacement_int = ImplPrivate.inserter.insertShort(atByte, existing_int, replacement);

            return ImplPrivate.u.compareAndSetInt(a, l, expected_int, replacement_int);
        }
    }

    public static class B extends AbstractMatrix.RowMajor<Byte> {

        private final byte[][] blocks;

        public static RowMajor<Byte> make(long m, long n) {
            if (m < Integer.MAX_VALUE && n < Integer.MAX_VALUE) {
                return new RowMatrix.B((int) m, (int) n);
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
            this.blocks = new byte[n_rows][n_columns];
        }

        public B(byte[][] blocks) {
            super(Byte.class, blocks.length, blocks.length > 0 ? blocks[0].length : 0);
            this.blocks = blocks;
            for (int i = 1; i < nRows; i++) {
                if (blocks[i].length != nCols) {
                    throw new IllegalArgumentException("Java matrix must be uniform");
                }
            }
        }

        @Override
        public Byte get(long i, long j) {
            return blocks[(int) i][(int) j];
        }

        public byte getValue(long i, long j) {
            return blocks[(int) i][(int) j];
        }

        @Override
        public void set(long i, long j, Byte x) {
            blocks[(int) i][(int) j] = x;
        }

        public void putValue(long i, long j, byte x) {
            blocks[(int) i][(int) j] = x;
        }

        @Override
        public boolean cas(long i, long j, Byte expected, Byte replacement) {
            byte[] a = blocks[(int) i];
            checkValidIndex(j, a.length);
            long l = j * ImplPrivate.ARRAY_BYTE_INDEX_SCALE
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
