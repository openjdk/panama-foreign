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

import java.util.function.LongFunction;

/**
 * A permutation matrix is a square (N by N) matrix with the property
 * containing ones (trues) and zeroes (falses) with the property
 * that it contains N ones (trues), and each row and each column
 * contains exactly one one (true).
 *
 * The implementation uses arrays of indices, but for linear algebra
 * purposes it is sometimes useful to have a permutation appear as a
 * matrix for multiplication purposes.
 */
abstract public class PermutationMatrix implements Matrix<Boolean> {

    private final long size;
    private final long sparseSize;
    /**
     * Optional inverse of this permutation matrix.
     * Maintained on the fly if it is ever requested.
     */
    private PermutationMatrix inverse;
    private boolean readOnly;

    static public final LongFunction<Long> longId = z -> z;

    /**
     * Creates an uninitialized permutation array;
     * it has not yet become a permutation.  The array
     * that is allocated to represent the permutation
     * will be size-optimized to the size of the permutation;
     * that is, a 256 element permutation can be stored in
     * bytes (which will be unsigned-extended on fetch).
     *
     * @param n
     * @return
     */
    static private PermutationMatrix make_empty(long n) {
        PermutationMatrix p;
        if (n >= Integer.MAX_VALUE) {
            // Need Array<long>
            p = new LongPermutation(n);
        } else if (n > 0xffff) {
            // int[]
            p = new IntPermutation((int) n);
        } else if (n >= 0xff) {
            // short[], treated as unsigned
            p = new ShortPermutation((int) n);
        } else {
            // byte[], treated as unsigned
            p = new BytePermutation((int) n);
        }
        return p;
    }

    /**
     * Permutation array factory.
     *
     * @param n
     * @return
     */
    static public PermutationMatrix make(long n) {
        PermutationMatrix p = make_empty(n);
        if (! (p instanceof LongPermutation))
            // NB LongPermutation is self-initializing
            // because it uses Array<Long> for permutation
            // and can use "fill" which can uses fork/join.
            for (int i = 0; i < n; i++) {
                p.put_for_init(i, i);
            }
        return p;
    }

    private static PermutationMatrix makeInverse(PermutationMatrix other) {
        long n = other.sparseSize();
        PermutationMatrix p = make_empty(n);
        for (int i = 0; i < n; i++) {
            p.put_for_init(other.indexOfNonZeroColumnInRow(i), i);
        }
        return p;
    }

    /**
     * Swap exchanges two rows of a permutation array and also
     * updates the inverse if it exists.
     *
     * @param i
     * @param j
     */
    public final void swapRows(long i, long j) {
        if (readOnly) {
            throw new IllegalStateException("Cannot modify a readonly permutation");
        }
        if (inverse == null) {
            swap_only(i, j);
        } else {
            long i_old = indexOfNonZeroColumnInRow(i);
            long j_old = indexOfNonZeroColumnInRow(j);
            swap_only(i, j);
            inverse.swap_only(i_old, j_old);
        }
    }

    /**
     * Permutation arrays can only be modified with row or column swapping,
     * but for initialization (either after construction, or for lazy creation
     * of an inverse matrix) elementwise puts are required.
     *
     * @param i row of inserted 1
     * @param j column of inserted 1
     */
    abstract protected void put_for_init(long i, long j);

    /**
     * Underlying swapRows operation that does not maintain optional inverse matrix.
     *
     * @param i
     * @param j
     */
    abstract protected void swap_only(long i, long j);

    /**
     * For row inRow, what is the index of the column that contains the one (true)?
     *
     * @param inRow
     * @return
     */
    abstract public long indexOfNonZeroColumnInRow(long inRow);

    /**
     * Returns the inverse of this matrix, constructing it if necessary.
     *
     * @return
     */
    final public PermutationMatrix inverse() {
        if (inverse == null) {
            PermutationMatrix inv = PermutationMatrix.makeInverse(this);
            if (readOnly)
                inv.setReadOnly();
            inverse = inv;
        }
        return inverse;
    }

    @Override
    final public void setReadOnly() {
        readOnly = true;
    }

    final public boolean isReadOnly() {
        return readOnly;
    }

    protected PermutationMatrix(long n) {
        this.sparseSize = n;
        this.size = n * n;
    }

    @Override
    public final long size() {
        return size;
    }

    @Override
    public final long sparseSize() {
        return sparseSize;
    }

    @Override
    public final long length(int x) {
        return sparseSize;
    }

    @Override
    public void set(long i, long j, Boolean x) {
        throw new UnsupportedOperationException("Cannot change permutation array with bare assignment");
    }

    @Override
    public boolean cas(long i, long j, Boolean expected, Boolean replacement) {
        throw new UnsupportedOperationException("Cannot assign into a permutation matrix");
    }

    @Override
    public int preferredMajorVote() {
        return NO_DIM;
    }

    @Override
    public Class<Boolean> elementType() {
        return Boolean.class;
    }

    final static class LongPermutation extends PermutationMatrix {

        private final Array<Long> perm;

        public LongPermutation(long n) {
            super(n);
            perm = LongArray.make(n);
            // NB note that initialization is performed
            // in constructor unlike other arrays which
            // use plain Java arrays.
            perm.fill(longId);
        }

        @Override
        public Boolean get(long i, long j) {
            long k = perm.get(i);
            return k == j ? Boolean.TRUE : Boolean.FALSE;
        }

        @Override
        protected synchronized  void swap_only(long i, long j) {
            long i_old = perm.get(i);
            long j_old = perm.get(j);
            perm.set(j, i_old);
            perm.set(i, j_old);
        }

        @Override
        public long indexOfNonZeroColumnInRow(long inRow) {
            return perm.get(inRow);
        }

        @Override
        protected void put_for_init(long i, long j) {
            perm.set(i, j);
        }
    }

    final static class IntPermutation extends PermutationMatrix {

        private final int[] perm;

        public IntPermutation(long n) {
            super(n);
            int ni = (int) n;
            perm = new int[ni];
        }

        @Override
        public Boolean get(long i, long j) {
            int k = perm[(int) i];
            return k == j ? Boolean.TRUE : Boolean.FALSE;
        }

        @Override
        protected synchronized void swap_only(long i, long j) {
            int ii = (int) i;
            int jj = (int) j;
            int i_old = perm[ii];
            int j_old = perm[jj];
            perm[jj] = i_old;
            perm[ii] = j_old;
        }

        @Override
        public long indexOfNonZeroColumnInRow(long inRow) {
            return perm[(int) inRow];
        }

        @Override
        protected void put_for_init(long i, long j) {
            perm[(int) i] = (int) j;
        }
    }

    final static class ShortPermutation extends PermutationMatrix {

        private final short[] perm;

        public ShortPermutation(long n) {
            super(n);
            int ni = (int) n;
            perm = new short[ni];
        }

        @Override
        public Boolean get(long i, long j) {
            int k = perm[(int) i] & 0xffff;
            return k == j ? Boolean.TRUE : Boolean.FALSE;
        }

        @Override
        protected synchronized void swap_only(long i, long j) {
            int ii = (int) i;
            int jj = (int) j;
            int i_old = perm[ii] & 0xffff;
            int j_old = perm[jj] & 0xffff;
            perm[jj] = (short) i_old;
            perm[ii] = (short) j_old;
        }

        @Override
        public long indexOfNonZeroColumnInRow(long inRow) {
            return perm[(int) inRow] & 0xffff;
        }

        @Override
        protected void put_for_init(long i, long j) {
            perm[(int) i] = (short) j;
        }
    }

    final static class BytePermutation extends PermutationMatrix {

        private final byte[] perm;

        public BytePermutation(long n) {
            super(n);
            int ni = (int) n;
            perm = new byte[ni];
        }

        @Override
        public Boolean get(long i, long j) {
            int k = perm[(int) i] & 0xff;
            return k == j ? Boolean.TRUE : Boolean.FALSE;
        }

        @Override
        protected synchronized  void swap_only(long i, long j) {
            int ii = (int) i;
            int jj = (int) j;
            int i_old = perm[ii] & 0xff;
            int j_old = perm[jj] & 0xff;
            perm[jj] = (byte) i_old;
            perm[ii] = (byte) j_old;
        }

        @Override
        public long indexOfNonZeroColumnInRow(long inRow) {
            return perm[(int) inRow] & 0xff;
        }

        @Override
        protected void put_for_init(long i, long j) {
            perm[(int) i] = (byte) j;
        }
    }
}
