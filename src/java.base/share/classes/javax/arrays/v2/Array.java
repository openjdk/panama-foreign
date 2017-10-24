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

import java.util.Spliterator;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterator.SIZED;
import static java.util.Spliterator.SUBSIZED;
import java.util.concurrent.RecursiveAction;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.LongFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.arrays.v2.nodes.Simplifier;

/**
 * An interface for array-like things.
 * These are intended to be friendly to recursive subdivision,
 * allow specialized implementations, and extension.
 *
 * This is a prototype, demonstrating a particular approach to this problem
 * that is not very "functional", and is much more full of side-effects and
 * aliases.
 *
 * @param <T>
 */

public interface Array<T> extends A1Expr<T> {

    /**
     * Returns the element at index i.
     *
     * @param i
     * @return the value at index i.
     */
    T get(long i);

    /**
     * Replaces the element at index i with x.
     *
     * @param i
     * @param x
     */
    void set(long i, T x);

    boolean cas(long i, T expected, T replacement);

    default void set(A1Expr<T> source) {
        source.setInto(this);
    }

    default Array<T> set(LongFunction<T> filler) {
        fill(filler);
        return this;
    }

    @Override
    default A1Expr<T> simplify(Array<T> target) {
        return this;
    }

    @Override
    default A1Expr<T> simplify(Array<T> target, Simplifier simplifier) {
        return simplifier.simplifyArray(target, this);
    }

    /**
     *
     * @param target
     */
    @Override
    default void evalInto(Array<T> target) {
        target.set( (i) -> get(i));
    }


    @Override
    default Array<T> getValue() {
        return this;
    }

    /**
     * Returns the exact size of the array.
     *
     * @return the size of the array.
     */
    long length();

   /**
     * Returns a performance-sensitive midpoint of beginInclusive and endExclusive.
     * As a general rule this should lie between 1/4 and 3/4 of the distance
     * between beginInclusive and endExclusive, and if there is no performance
     * benefit, then the exact midpoint is preferred.
     *
     * @param beginInclusive
     * @param endExclusive
     * @return a performance-sensitive midpoint of beginInclusive and endExclusive
     */
    default long middle(long beginInclusive, long endExclusive) {
            return (beginInclusive + endExclusive) >>> 1;
    }

    default long middle() {
        return middle(0, length());
    }

    /**
     * Returns true if subArray of the respective indices would yield
     * an array with simpler (computationally less expensive) indexing
     * operations.
     *
     * @param beginInclusive
     * @param endExclusive
     * @return true if a subArray of these indices will have simpler indexing.
     */
    default boolean canSimplify(long beginInclusive, long endExclusive) {
        return false;
    }

     /**
     * Calls filler with argument i+offset to obtain the value to store at
     * index i for indices beginInclusive up to endExclusive.  The calls and
     * assignments are performed in some order.  This is a leaf helper method
     * for fill; it is not expected to use fork/join.
     *
     * This may/should be overridden for performance.
     *
     * @param beginInclusive
     * @param endExclusive
     * @param offset
     * @param filler
     */
    default void indexedAssignmentToRange(long beginInclusive, long endExclusive,
                                    long offset, LongFunction<T> filler) {
        for (long i = beginInclusive; i < endExclusive; i++) {
            set(i, filler.apply(i+offset));
        }
    }

    /**
     * Calls action on a range of this array's elements.  The calls are
     * performed in some order.
     *
     * This is a leaf helper method for Spliterators.
     * This may/should be overridden for performance.
     *
     * @param beginInclusive
     * @param endExclusive
     * @param action
     */
    default void applyToRange(long beginInclusive,
            long endExclusive,
            Consumer<? super T> action) {
        // hoist accesses and checks from loop
        if (endExclusive <= length() && beginInclusive >= 0 && beginInclusive < endExclusive) {
            do {
                action.accept(get(beginInclusive));
            } while (++beginInclusive < endExclusive);
        }
    }

    /**
     * Calls action on a range of the zip of this array and another array's elements.
     * The calls are performed in serial order.
     *
     * This is a leaf helper method for ZipSpliterators.
     * This may/should be overridden for performance.
     *
     * @param <U>
     * @param beginInclusive
     * @param endExclusive
     * @param otherOffset
     * @param other
     * @param opr
     * @param action
     */
    default <U> void zipAndApplyToRange(long beginInclusive, long endExclusive, long otherOffset,
            Array<T> other, BiFunction<T,T,U> opr, Consumer<? super U> action) {

        // hoist accesses and checks from loop
            if (endExclusive <= length()
                    && beginInclusive >= 0 && beginInclusive < endExclusive) {
                do {
                    action.accept(opr.apply(get(beginInclusive), other.get(beginInclusive + otherOffset)));
                } while (++beginInclusive < endExclusive);
            }
    }

    /**
     * Returns a subarray, aliased with a portion of this array, spanning the
     * elements of this array from beginInclusive to endExclusive.
     * Requires { @code 0 &lt;= beginInclusive &lt;= endExclusive &lt;= size() },
     * else IllegalArgumentException is thrown.
     *
     * This may/should be overridden for performance; the intent is that if an
     * indexing range is small enough to permit a simpler indexing computation
     * (for example, within a single Java array), then it should. For example,
     * SubArrayImpl itself overrides this method to rebase a nested subarray
     * within its parent.
     *
     * @param beginInclusive
     * @param endExclusive
     * @return the subarray aliased to the specified elements.
     */
    default Array<T> subArray(final long beginInclusive, final long endExclusive) {
        checkLegalRange(beginInclusive, endExclusive);
        if (beginInclusive == 0 && endExclusive == length()) {
            return this;
        }
        return new SubArrayImpl<>(this, beginInclusive, endExclusive);
    }

    /**
     * Returns a larger array comprising (and aliased with) this array followed
     * by other.  If both arrays are suitably aligned subarrays of the same larger
     * array, a subarray of the larger array might be returned, with a goal of
     * minimizing the complexity of indexing operations.
     *
     * @param other
     * @return the aliased concatenation of the two arrays.
     */
    default Array<T> append(final Array<T> other) {
        return new AppendArray<>(this, other);
    }

    /**
     * Returns a stream from this array, with a preference to stop binary
     * subdivision at blocksize, if other considerations do not intrude.
     *
     * @param blocksize
     * @return
     */
    default Stream<T> asStream(final int blocksize) {
        return StreamSupport.stream(asSpliterator(0, length(), blocksize, null), true);
    }

    default Stream<T> asStream(final int blocksize, ReductionKernel<T> rk) {
        return StreamSupport.stream(asSpliterator(0, length(), blocksize, rk), true);
    }

    /**
     * Returns a spliterator from {@code from} to {@code to},
     * with a preference to stop splitting when the range is
     * smaller than blocksize.  {@code rk} is an optional (may be null)
     * reduction kernel that may be hand-coded for peak performance
     * (this is a benchmarking tool among other things).
     *
     * @param from
     * @param to
     * @param blocksize
     * @param rk
     * @return
     */
    default Spliterator<T> asSpliterator(final long from, final long to,
                                  final int blocksize, ReductionKernel<T> rk) {
        // TODO Report parameter values in exceptions.
        if ((from | to | (to - from) | (length() - to)) < 0) {
            throw new IllegalArgumentException();
        }

        return new DefaultSpliterator<>(this, from, to, blocksize, rk);
    }

    /**
     * Returns a stream by combining two arrays with binary function opr,
     * with a preference to stop recursive splitting when the range is smaller
     * than blocksize.
     *
     * @param <U>
     * @param other
     * @param opr
     * @param blocksize
     * @return
     */
    default <U> Stream<U> asZippedStream(final Array<T> other, BiFunction<T,T,U> opr,
                                         final int blocksize) {
        return StreamSupport.stream(zipWithSpliterator(other, opr, 0, length(), blocksize, null), true);
    }

    /**
     * Like asZippedStream, but with a ZipReductionKernel parameter for
     * benchmarking and performance.
     *
     * @param <U>
     * @param other
     * @param opr
     * @param blocksize
     * @param zrk
     * @return
     */
    default <U> Stream<U> asZippedStream(final Array<T> other, BiFunction<T,T,U> opr,
                                         final int blocksize, ZipReductionKernel<T,U> zrk) {
        return StreamSupport.stream(zipWithSpliterator(other, opr, 0, length(), blocksize, zrk), true);
    }

    default <U> Spliterator<U> zipWithSpliterator(final Array<T> other, BiFunction<T,T,U> opr,
            final long from, final long to, final int blocksize, ZipReductionKernel<T,U> zrk) {
        return new DefaultZipSpliterator<>(this, other, opr, from, to, blocksize, zrk);
    }

    default void checkLegalRange(final long beginInclusive, final long endExclusive) throws IllegalArgumentException {
        // Check for legal indexing w.r.t. this array before delegating.
        if (beginInclusive < 0
                || endExclusive < beginInclusive
                || length() < endExclusive) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Calls filler with argument i to obtain the value to store at index i.
     *
     * @param filler
     */
    default void fill(final LongFunction<T> filler) {
        fill(filler, 0L, length());
    }

    /**
     * Calls filler with argument i to obtain the value to store at index i
     * for indices beginInclusive up to endExclusive.  Uses fork-join parallelism
     * to try to make this faster.
     *
     * @param filler
     * @param beginInclusive
     * @param endExclusive
     */
    default void fill(final LongFunction<T> filler,
            final long beginInclusive, final long endExclusive) {
        new IndexedAction<>(this, filler, beginInclusive, endExclusive).compute();
    }

    /**
     * Returns an Arrays 2.0 array aliased with an existing Java array. (i.e.
 may be regarded as using that array for storage and/or initialization).
 The values of the Java array are not changed; this is one way of creating
 a new initialized Array.
     *
     * @param <T>
     * @param array
     * @return
     */
    static <T> Array<T> fromArray(final T[] array) {
        return new JavaArray<>(array);

    }


    static public <T> Array<T> allocateNew(final Class<T> elt_class,
                                 final long size) {
        return allocateNew(elt_class, size, A2Expr.arrayBlockSize());
    }
    /**
     * Allocate a new 2.0 Array potentially using multiple old-style arrays for
     * its storage. Each storage array except perhaps the last will contain
     * {@code 1 << log_block_size} elements. In addition,
     * {@code (size + (1 << log_block_size) - 1) >>> log_block_size}
     * must be smaller than 2-to-the-31; that is the number of blocks
     * necessary to implement the array must be representable as a Java int.
     *
     * TODO a version that takes an initializer and fires off threads might be
     * worth adding.
     *
     * @param <T>
     * @param elt_class
     * @param size
     * @param log_block_size
     * @return
     */
    @SuppressWarnings("unchecked")
    static <T> Array<T> allocateNew(final Class<T> elt_class,
                                 final long size,
                                 final int log_block_size) {
        if (size < 0 || log_block_size < 0 || log_block_size > 30) {
            throw new IllegalArgumentException();
        }
        final int mask = (1 << log_block_size) - 1;
        int last_size = (mask & ((int) size - 1)) + 1;

        @SuppressWarnings("unchecked")
        T[] last_block_array = (T[]) java.lang.reflect.Array.newInstance(elt_class, last_size);
        Array<T> last_block = fromArray(last_block_array);
        if (last_size == size) {
            return last_block;
        }

        long l_n_blocks = (size + mask) >>> log_block_size;
        final int n_blocks = (int) l_n_blocks;
        if (l_n_blocks != n_blocks) {
            throw new IllegalArgumentException("'l+mask >>> log_block_size' must be no larger than Integer.MAX_VALUE");
        }

        @SuppressWarnings("unchecked")
        Array<T>[] blocks = (Array<T>[]) java.lang.reflect.Array.newInstance(Array.class, n_blocks);
        for (int i = 0; i < n_blocks - 1; i++) {
            blocks[i] = fromArray((T[]) java.lang.reflect.Array.newInstance(elt_class, mask + 1));
        }
        blocks[n_blocks - 1] = last_block;

        return new BlockedArray<>(elt_class, blocks, log_block_size) ;
    }


    /**
     * A default Spliterator for arrays, with specified blocking.
     *
     * @param <T>
     */
    static class DefaultSpliterator<T> implements Spliterator<T> {

        protected long origin;
        protected final long fence;
        // blocksize is one or larger
        protected final int blocksize;
        protected final Array<T> array;
        protected final ReductionKernel<T> kernel;

        DefaultSpliterator(Array<T> _array, long _from, long _to, int _blocksize, ReductionKernel<T> rk) {
            origin = _from;
            fence = _to;
            blocksize = _blocksize > 0 ? _blocksize : 16;
            array = _array;
            kernel = rk;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            if (origin >= fence) {
                return false;
            }
            action.accept(array.get(origin++));
            return true;
        }

        @Override
        public Spliterator<T> trySplit() {
            long _size = estimateSize();
            if (_size <= blocksize) {
                return null;
            }
            // size is two or larger
            long mid = array.middle(origin, fence);
            Spliterator<T> result = array.canSimplify(origin, mid) ?
               array.subArray(origin, mid).asSpliterator(0, mid - origin, blocksize, kernel) :
               array.asSpliterator(origin, mid, blocksize, kernel);
            origin = mid;
            return result;
        }

        @Override
        public void forEachRemaining(Consumer<? super T> action) {
            if (action == null) {
                throw new NullPointerException();
            }

            Array<T> a = array;
            long i = origin;
            long hi = fence;

            if (a.canSimplify(i, hi)) {
                a = array.subArray(origin, fence);
                hi = fence - origin;
                try {
                    a.asSpliterator(0, hi, blocksize, kernel).forEachRemaining(action);
                } finally {
                    origin = hi;
                }
            } else {
                origin = hi; // consume it all, even if an exception is thrown
                if (kernel != null && kernel.isApplicable(array)) {
                    action.accept(kernel.apply(array, i, hi));
                } else {
                    a.applyToRange(i, hi, action);
                }
            }
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

    /**
     * A default Spliterator for zipped arrays, with specified blocking.
     *
     * @param <T>
     * @param <U>
     */
    static class DefaultZipSpliterator<T,U> implements Spliterator<U> {

        protected long origin;
        protected final long fence;
        // blocksize is one or larger
        protected final int blocksize;
        protected final Array<T> left;
        protected final Array<T> right;
        protected final BiFunction<T,T,U> opr;
        protected final ZipReductionKernel<T,U> zrk;

        DefaultZipSpliterator(Array<T> left, Array<T> right, BiFunction<T,T,U> opr, long from, long to, int blocksize,  ZipReductionKernel<T,U> zrk) {
            this.origin = from;
            this.fence = to;
            this.blocksize = blocksize > 0 ? blocksize : 16;
            this.left = left;
            this.right = right;
            this.opr = opr;
            this.zrk = zrk;
        }

        @Override
        public boolean tryAdvance(Consumer<? super U> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            if (origin >= fence) {
                return false;
            }
            T l = left.get(origin);
            T r = right.get(origin++);
            action.accept(opr.apply(l,r));
            return true;
        }

        @Override
        public Spliterator<U> trySplit() {
            long _size = estimateSize();
            if (_size <= blocksize) {
                return null;
            }
            // size is two or larger
            long mid = (origin + fence) >>> 1;
            long other_mid = right.middle(origin, fence);
            if (other_mid != mid) {
                mid = other_mid;
            } else {
                other_mid = left.middle(origin, fence);
                if (other_mid != mid)
                    mid = other_mid;
            }
            // Extra allocation, could conditionall not take the left_prefix
            Array<T> right_prefix = right.subArray(origin, mid);
            Array<T> left_prefix = left.subArray(origin, mid);
            Spliterator<U> result =  left_prefix.zipWithSpliterator(right_prefix, opr, 0, mid-origin, blocksize, zrk);
            origin = mid;
            return result;
        }

        // TODO need plan to specialize for arraytype=arraytype case.
        @Override
        public void forEachRemaining(Consumer<? super U> action) {
            if (action == null) {
                throw new NullPointerException();
            }

            Array<T> a = left;
            Array<T> b = right;
            long lo = origin;
            long hi = fence;
            long offset = 0;

            origin = hi; // consume it all, even if an exception is thrown

            if (a instanceof SubArray) {
                SubArray<T> sub_l = (SubArray<T>) a;
                if (lo < 0 || hi > sub_l.length())
                   throw new IllegalArgumentException();
                lo += sub_l.begin();
                hi += sub_l.begin();
                offset -= sub_l.begin();
                a = sub_l.parent();
            }
            if (b instanceof SubArray) {
                SubArray<T> sub_r = (SubArray<T>) b;
                if (lo + offset < 0 || hi + offset > sub_r.length()) {
                    throw new IllegalArgumentException();
                }
                offset += sub_r.begin();
                b = sub_r.parent();
            }
            if (zrk != null && zrk.isApplicable(a, b)) {
                // Does this specialization help at all?  Appears not to.
                if (offset == 0)
                     action.accept(zrk.apply(a, b, lo, hi));
                else action.accept(zrk.apply(a, b, lo, hi, offset));
            } else {
                a.zipAndApplyToRange(lo, hi, offset, b, opr, action);
            }
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

    /**
     * A wrapper for applying a function at each index of an array to obtain
     * values assigned to those elements.
     * @param <T>
     */
    static public class IndexedAction<T> extends RecursiveAction {
        private static final long serialVersionUID = 0x2ff28c4610bea8e7L;
        protected final Array<T> array;
        protected final LongFunction<T> filler;
        protected final long beginInclusive;
        protected final long endExclusive;

        protected long serialBelow() {
            return 65537;
        }

        public IndexedAction(final Array<T> array, final LongFunction<T> filler,
            final long beginInclusive, final long endExclusive) {
            this.array = array;
            this.filler = filler;
            this.beginInclusive = beginInclusive;
            this.endExclusive = endExclusive;
        }

        @Override
        protected void compute() {
            long lo = beginInclusive;
            long hi = endExclusive;
            Array<T> a = array;
            if (hi - lo < serialBelow()) {
                if (a.canSimplify(lo, hi)) {
                    a = a.subArray(lo, hi);
                    hi = hi - lo;
                    a.indexedAssignmentToRange(0, hi, lo, filler);
                } else {
                    a.indexedAssignmentToRange(lo, hi, 0, filler);
                    // Does keeping the loops separate allow for better inlining?
                }
            } else {
                long mid = a.middle(lo, hi);
                IndexedAction<T> right = new IndexedAction<>(array, filler, mid, hi);
                right.fork();
                IndexedAction<T> left = new IndexedAction<>(array, filler, lo, mid);
                left.compute();
                right.join();
            }
        }
    }
}
