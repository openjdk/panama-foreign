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

import javax.arrays.tbd.LongLongFunction;
import java.io.PrintStream;
import java.util.Spliterator;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterator.SIZED;
import static java.util.Spliterator.SUBSIZED;
import java.util.concurrent.RecursiveAction;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import static javax.arrays.v2.A2Expr.COL_DIM;
import static javax.arrays.v2.A2Expr.ROW_DIM;
import javax.arrays.v2.nodes.Simplifier;

public interface Matrix<T> extends A2Expr<T> {

    /**
     * Returns the element at index i.
     *
     * @param i
     * @param j
     * @return the value at index (i,j).
     */
    T get(long i, long j);

    /**
     * Replaces the element at index (i,j) with x.
     *
     * @param i
     * @param j
     * @param x
     */
    void set(long i, long j, T x);

    boolean cas(long i, long j, T expected, T replacement);

    /**
     * This is a placeholder; it's not clear we can reconcile dynamic choice
     * of readonly with efficient access, but array modification is less common
     * than array examination, so perhaps this will be acceptable.
     */
    default void setReadOnly() {
    }

    /**
     * Return true if the specified rectangle has a simpler (cheaper, more
     * easily analyzed) indexing structure than this matrix.
     *
     * @param i_lo
     * @param i_hi
     * @param j_lo
     * @param j_hi
     * @return
     */
    default boolean canSimplifyAccesses(long i_lo, long i_hi, long j_lo, long j_hi) {
        return false;
    }

    /**
     * Returns row i as a 1-dimensional array
     * @param i
     * @return
     */
    default Array<T> getRow(long i) {
        return new RowSection<>(this, i);
    }

    /**
     * Returns column j as a 1-dimensional array
     * @param j
     * @return
     */
    default Array<T> getColumn(long j) {
        return new ColumnSection<>(this, j);
    }

    /**
     * Returns row/column i as a one dimensional array, with row or column
     * specified by parameter d (ROW_DIM or COL_DIM).
     * @param d
     * @param i
     * @return
     */
    default Array<T> getSection(int d, long i) {
        if (d == ROW_DIM) {
            return getRow(i);
        } else if (d == COL_DIM) {
            return getColumn(i);
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Returns a new matrix whose rows are those of this matrix followed
     * by those of the other matrix.  This and other must have the same
     * number of columns.
     *
     * @param other
     * @return
     */
    default Matrix<T> appendRows(Matrix<T> other) {
        return new AppendRowsMatrix<>(this, other);
    }

    /**
     * Returns a new matrix whose columns are those of this matrix followed
     * by those of the other matrix.  This and other must ahve the same number
     * of rows.
     *
     * @param other
     * @return
     */
    default Matrix<T> appendColumns(Matrix<T> other) {
        return new AppendColumnsMatrix<>(this, other);
    }

    /**
     * Returns the submatrix bounded by the inclusive-exclusive corners (i_lo,
     * j_lo) and (i_hi, j_hi). The returned submatrix may be simpler in
     * structure than this matrix, might not refer to it as its parent, and
     * might not be an instance of SubMatrix.
     *
     * @param i_lo
     * @param i_hi
     * @param j_lo
     * @param j_hi
     * @return
     */
    default Matrix<T> subMatrix(long i_lo, long i_hi, long j_lo, long j_hi) {
        if (!(0 <= i_lo && i_lo <= i_hi & i_hi <= length(ROW_DIM)
                && 0 <= j_lo && j_lo <= j_hi & j_hi <= length(COL_DIM))) {
            throw new IndexOutOfBoundsException();
        }

        if (0 == i_lo && i_hi == length(ROW_DIM) && 0 == j_lo && j_hi == length(COL_DIM)) {
            return this;
        }
        return allocateSubMatrix(i_lo, i_hi, j_lo, j_hi);
    }

    /**
     * Sparse matrices might have interesting submatrices -- for example,
     * constant "zeroes", and this method allows that option to be implemented
     * in efficient ways.
     *
     * @param i_lo
     * @param i_hi
     * @param j_lo
     * @param j_hi
     * @return
     */
    default Matrix<T> allocateSubMatrix(long i_lo, long i_hi, long j_lo, long j_hi) {
                return new SubMatrix<>(this, i_lo, i_hi, j_lo, j_hi);
    }

    /**
     * Extracts the submatrix consisting of rows i_lo (inclusive) up to i_hi
     * (exclusive).
     *
     * @param i_lo
     * @param i_hi
     * @return the submatrix containing the specified rows
     */
    default Matrix<T> getRows(long i_lo, long i_hi) {
        return subMatrix(i_lo, i_hi, 0, length(COL_DIM));
    }

    /**
     * Extracts the submatrix consisting of columns j_lo (inclusive) up to j_hi
     * (exclusive)
     *
     * @param j_lo
     * @param j_hi
     * @return the submatrix containing the specified columns
     */
    default Matrix<T> getColumns(long j_lo, long j_hi) {
        return subMatrix(0, length(ROW_DIM), j_lo, j_hi);
    }

    /**
     * Returns this matrix regarded as a stream of rows.
     *
     * @param blocksize
     * @return
     */
    default Stream<Array<T>> asStreamOfRows(final int blocksize) {
        return StreamSupport.stream(asSpliteratorOfRows(0, length(ROW_DIM), blocksize), true);
    }

    default Spliterator<Array<T>> asSpliteratorOfRows(final long from, final long to, final int blocksize) {
        // TODO Report parameter values in exceptions.
        if ((from | to | (to - from) | (size() - to)) < 0) {
            throw new IllegalArgumentException();
        }

        return new SpliteratorOfRows<>(this, from, to, blocksize);
    }

   /**
     * Returns this matrix regarded as a stream of columns.
     *
     * @param blocksize
     * @return
     */
    default Stream<Array<T>> asStreamOfColumns(final int blocksize) {
        return StreamSupport.stream(asSpliteratorOfColumns(0, length(ROW_DIM), blocksize), true);
    }

    default Spliterator<Array<T>> asSpliteratorOfColumns(final long from, final long to, final int blocksize) {
        // TODO Report parameter values in exceptions.
        if ((from | to | (to - from) | (size() - to)) < 0) {
            throw new IllegalArgumentException();
        }

        return new SpliteratorOfColumns<>(this, from, to, blocksize);
    }

    /**
     * Serial application of actions to some rows of this matrix.
     * Leaf case invoked by default implementations of asStreamOfRows/asSpliteratorOfRows.
     * May be overridden for performance with special-case matrices.
     *
     * @param beginInclusive
     * @param endExclusive
     * @param action
     */
    default void applyToRows(long beginInclusive,
            long endExclusive,
            Consumer<? super Array<T>> action) {
        // hoist accesses and checks from loop
        if (endExclusive <= length(ROW_DIM) && beginInclusive >= 0 && beginInclusive < endExclusive) {
            do {
                action.accept(getRow(beginInclusive));
            } while (++beginInclusive < endExclusive);
        }
    }

   /**
     * Serial application of actions to some columns of this matrix.
     * Leaf case invoked by default implementations of asStreamOfColumns/asSpliteratorOfColumns.
     * May be overridden for performance with special-case matrices.
     *
     * @param beginInclusive
     * @param endExclusive
     * @param action
     */
   default void applyToColumns(long beginInclusive,
            long endExclusive,
            Consumer<? super Array<T>> action) {
        // hoist accesses and checks from loop
        if (endExclusive <= length(COL_DIM) && beginInclusive >= 0 && beginInclusive < endExclusive) {
            do {
                action.accept(getColumn(beginInclusive));
            } while (++beginInclusive < endExclusive);
        }
    }

   /**
    * Print the contents of the matrix in row major order, with specified separator.
    * @param out
    * @param separator
    */
    default void tabularOut(final PrintStream out, final String separator) {
        asStreamOfRows(1).forEachOrdered(
                (Array<T> t) -> {
                    t.asStream(1).forEachOrdered(
                            new Consumer<T>() {
                                private boolean not_first;
                                @Override
                                public void accept(T u) {
                                    String s = String.valueOf(u);
                                    if (not_first) {
                                        out.print(separator);
                                        out.print(s);
                                    } else {
                                        out.print(s);
                                        not_first = true;
                                    }
                                }
                            }
                    );
                    out.println();
        });
    }

  /**
    * Print the contents of the matrix in row major order with elements
    * separated by commas.
    * @param out
    */
    default void csvOut(final PrintStream out) {
        tabularOut(out, ", ");
    }

    static class SpliteratorOfRows<T> implements Spliterator<Array<T>> {
        protected long origin;
        protected final long fence;
        // blocksize is one or larger
        protected final int blocksize;
        protected final Matrix<T> matrix;

        SpliteratorOfRows(Matrix<T> _matrix, long _from, long _to, int _blocksize) {
            origin = _from;
            fence = _to;
            blocksize = _blocksize > 0 ? _blocksize : 16;
            matrix = _matrix;
        }

        @Override
        public boolean tryAdvance(Consumer<? super Array<T>> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            if (origin >= fence) {
                return false;
            }
            action.accept(matrix.getRow(origin++));
            return true;
        }

        @Override
        public Spliterator<Array<T>> trySplit() {
            long _size = estimateSize();
            if (_size <= blocksize) {
                return null;
            }
            // size is two or larger
            long mid = matrix.middle(ROW_DIM, origin, fence);
            // TODO later, make canSimplifyAccesses sensitive?
            Spliterator<Array<T>> result = // array.canSimplifyAccesses(origin, mid) ?
                    matrix.getRows(origin, mid).asSpliteratorOfRows(0, mid - origin, blocksize);
            origin = mid;
            return result;
        }

        @Override
        public void forEachRemaining(Consumer<? super Array<T>> action) {
            if (action == null) {
                throw new NullPointerException();
            }

            Matrix<T> a = matrix;
            long i = origin;
            long hi = fence;

            origin = hi; // consume it all, even if an exception is thrown
            a.applyToRows(i, hi, action);

        }

        @Override
        public long estimateSize() {
            return fence - origin;
        }

        @Override
        public int characteristics() {
            // TODO What's the plan for CONCURRENT or IMMUTABLE?
            return IMMUTABLE | ORDERED | SIZED | SUBSIZED;
        }
    }

        static class SpliteratorOfColumns<T> implements Spliterator<Array<T>> {
        protected long origin;
        protected final long fence;
        // blocksize is one or larger
        protected final int blocksize;
        protected final Matrix<T> matrix;

        SpliteratorOfColumns(Matrix<T> _matrix, long _from, long _to, int _blocksize) {
            origin = _from;
            fence = _to;
            blocksize = _blocksize > 0 ? _blocksize : 16;
            matrix = _matrix;
        }

        @Override
        public boolean tryAdvance(Consumer<? super Array<T>> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            if (origin >= fence) {
                return false;
            }
            action.accept(matrix.getColumn(origin++));
            return true;
        }

        @Override
        public Spliterator<Array<T>> trySplit() {
            long _size = estimateSize();
            if (_size <= blocksize) {
                return null;
            }
            // size is two or larger
            long mid = matrix.middle(ROW_DIM, origin, fence);
            // TODO later, make canSimplifyAccesses sensitive?
            Spliterator<Array<T>> result = // array.canSimplifyAccesses(origin, mid) ?
                    matrix.getColumns(origin, mid).asSpliteratorOfColumns(0, mid - origin, blocksize);
            origin = mid;
            return result;
        }

        @Override
        public void forEachRemaining(Consumer<? super Array<T>> action) {
            if (action == null) {
                throw new NullPointerException();
            }

            Matrix<T> a = matrix;
            long i = origin;
            long hi = fence;

            origin = hi; // consume it all, even if an exception is thrown
            a.applyToColumns(i, hi, action);

        }

        @Override
        public long estimateSize() {
            return fence - origin;
        }

        @Override
        public int characteristics() {
            // TODO What's the plan for CONCURRENT or IMMUTABLE?
            return IMMUTABLE | ORDERED | SIZED | SUBSIZED;
        }
    }

    /*
     * How assignment works:

        matrix.set(expr) =>
        expr.setInto(matrix) =>
        expr' = expr.simplify(target) then expr'.evalInto(target)

        if expr is matrix, then just copy bits (default evalInto below)
        else figure out best way to evaluate expr in light of structure of
        matrix ("target") and then do so.

        Note that
        matrix = expr
        will fail with a type error, so there is a minor speedbump in front of
        doing "the wrong thing".
     *
     */

    default void set(A2Expr<T> source) {
        source.setInto(this);
    }

    @Override
    default A2Expr<T> simplify(Matrix<T> target) {
        return this;
    }

    @Override
    default A2Expr<T> simplify(Matrix<T> target, Simplifier simplifier) {
        return simplifier.simplifyMatrix(target, this);
    }

    /**
     *
     * @param target
     */
    @Override
    default void evalInto(Matrix<T> target) {
        target.set( (i,j) -> get(i,j));
    }

    @Override
    default Matrix<T> getValue() {
        return this;
    }

    @Override
    default A2Expr<T> tempifyAsNecessary(Simplifier simplifier, int desiredMajorOrder) {
        return this;
    }

    /*
     ************** Elementwise operations on one matrix *************
     */

    default Matrix<T> set(LongLongFunction<T> filler) {
        setRecursive(filler, 0, 0, Parameters.ELEMENTWISE_BLOCKSIZE);
        return this;
    }

    default Matrix<T> set(final Matrix<T> other) {
        // Need to special case this, perhaps.
        // Better just to do an identity op, will simplify addressing into "other".
        setRecursive((i,j) -> other.get(i,j), 0, 0, Parameters.ELEMENTWISE_BLOCKSIZE);
        return this;
    }


    default void setRecursive(LongLongFunction<T> filler, long delta_i, long delta_j, int blocksize) {
        Matrix<T> target = this;
        long n_rows = target.length(ROW_DIM);
        long n_cols = target.length(COL_DIM);
        if (n_rows <= blocksize) {
            if (n_cols <= blocksize) {
                // kernel iteration.
                target.setSubMatrix(filler, 0, n_rows, 0, n_cols, delta_i, delta_j);
            } else {
                // subdivide columns
                long m0 = target.middle(COL_DIM);

                ElementWiseSetAction<T> right = new ElementWiseSetAction<>(
                        target.getColumns(m0, n_cols),
                        filler, delta_i, delta_j + m0,
                        blocksize);
                right.fork();
                ElementWiseSetAction<T> left = new ElementWiseSetAction<>(
                        target.getColumns(0, m0),
                        filler, delta_i, delta_j,
                        blocksize);
                left.compute();
                right.join();
            }
        } else {
            // subdivide rows
            long m0 = target.middle(ROW_DIM);

            ElementWiseSetAction<T> right = new ElementWiseSetAction<>(
                    target.getRows(m0, n_rows),
                    filler, delta_i + m0, delta_j,
                    blocksize);
            right.fork();
            ElementWiseSetAction<T> left = new ElementWiseSetAction<>(
                    target.getRows(0, m0),
                    filler, delta_i, delta_j,
                    blocksize);
            left.compute();
            right.join();
        }
    }

    default void setSubMatrix(LongLongFunction<T> filler,
            long i_lo, long i_hi,
            long j_lo, long j_hi, long delta_i, long delta_j) {

        if (preferredMajorDim() != COL_DIM) {
            for (long i = i_lo; i < i_hi; i++) {
                for (long j = j_lo; j < j_hi; j++) {
                    set(i, j, filler.apply(i + delta_i, j + delta_j));
                }
            }
        } else { // Column major.
            for (long j = j_lo; j < j_hi; j++) {
                for (long i = i_lo; i < i_hi; i++) {
                    set(i, j, filler.apply(i + delta_i, j + delta_j));
                }
            }

        }
    }

     static public class ElementWiseSetAction<T> extends RecursiveAction {
        private static final long serialVersionUID = 0x7c6a6114c6ca4c08L;

        protected final Matrix<T> target;
        protected final LongLongFunction<T> filler;
        protected final int blocksize;
        protected final long delta_i;
        protected final long delta_j;

        public ElementWiseSetAction(Matrix<T> target,
                LongLongFunction<T> filler, long delta_i, long delta_j, int blocksize) {
            this.target = target;
            this.filler = filler;
            this.blocksize = blocksize;
            this.delta_i = delta_i;
            this.delta_j = delta_j;
        }

        @Override
        protected void compute() {
            target.setRecursive(filler, delta_i, delta_j, blocksize);
        }
    }

    static class RowSection<T> implements Array<T> {

        final Matrix<T> within;
        final long row;

        RowSection(Matrix<T> x, long i) {
            if (x.canSimplifyAccesses(i, i+1, 0, x.length(COL_DIM))) {
                x = x.subMatrix(i, i+1, 0, x.length(COL_DIM));
                i = 0;
            }

            this.within = x;
            this.row = i;
        }

        @Override
        public T get(long i) {
            return within.get(row, i);
        }

        @Override
        public void set(long i, T x) {
            within.set(row, i, x);
        }

        @Override
        public boolean cas(long i, T expected, T replacement) {
            return within.cas(row, i, expected, replacement);
        }

        @Override
        public long length() {
            return within.length(COL_DIM);
        }

        @Override
        public Class<T> elementType() {
            return within.elementType();
        }

        @Override
        public boolean canSimplify(long beginInclusive, long endExclusive) {
            return within.canSimplifyAccesses(row, row+1, beginInclusive, endExclusive);
        }

        @Override
        public  Array<T> subArray(final long beginInclusive, final long endExclusive) {
            checkLegalRange(beginInclusive, endExclusive);
            if (beginInclusive == 0 && endExclusive == length()) {
                return this;
            }
            return new RowSection<>(within.subMatrix(row, row+1, beginInclusive, endExclusive), 0);
        }
    }

    static class ColumnSection<T> implements Array<T> {

        public final Matrix<T> within;
        public final long column;

        ColumnSection(Matrix<T> x, long i) {
            if (x.canSimplifyAccesses(0, x.length(ROW_DIM), i, i+1)) {
                x = x.subMatrix(0, x.length(ROW_DIM), i, i+1);
                i = 0;
            }
            this.within = x;
            this.column = i;
        }

        @Override
        public T get(long i) {
            return within.get(i, column);
        }

        @Override
        public void set(long i, T x) {
            within.set(i, column, x);
        }

        @Override
        public boolean cas(long i, T expected, T replacement) {
            return within.cas(i, column, expected, replacement);
        }

        @Override
        public long length() {
            return within.length(ROW_DIM);
        }

        @Override
        public Class<T> elementType() {
            return within.elementType();
        }

        @Override
        public boolean canSimplify(long beginInclusive, long endExclusive) {
            return within.canSimplifyAccesses(beginInclusive, endExclusive, column, column+1);
        }

        @Override
        public  Array<T> subArray(final long beginInclusive, final long endExclusive) {
            checkLegalRange(beginInclusive, endExclusive);
            if (beginInclusive == 0 && endExclusive == length()) {
                return this;
            }
            return new ColumnSection<>(within.subMatrix(beginInclusive, endExclusive, column, column+1), 0);
        }

    }
}
