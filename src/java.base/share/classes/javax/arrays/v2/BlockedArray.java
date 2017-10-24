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
import java.util.function.Consumer;

/**
 * Provides a new blocked array based on the existing array of Java arrays
 * passed in to it.  These storage arrays must all have a length that is the
 specified power of two, except for the last, which may be smaller (but not
 zero-length).  The contents of the Java arrays are not changed by this
 allocation; this can be used to provide an initial value, or the Array.fill
 method can be used to initialize the array with an index-dependent value.

 TODO - exposing the class like this creates the possibility of a degenerate
 blocked array; we might hope to ban that.
 *
 * @param <T>
 */
public class BlockedArray<T> extends AbstractArray<T>  {

    protected final int logBlockSize;
    protected final int mask;
    protected final Array<T>[] blocks;
    protected final long size;

    /**
     * Given a factory for an array of one-dimensional arrays
     * and for the arrays themselves, allocate a blocked array.
     * The factory parameter allows customized storage layouts
     * for the elements themselves; for example, primitive types
     * might be stored directly, or bits might be packed.
     *
     * @param <T>
     * @param n the number of elements in the array to be allocated.
     * @param f the factor for smaller arrays of elements and the array of arrays
     *          of elements.
     * @return an Array of the specified size and implementation.
     */
    public static <T> Array<T> make(long n, ArrayFactory<T> f) {
            int b = Long.numberOfLeadingZeros(n);
            int s = 63 - b; // the (log of) the highest set bit
            int h = s >> 1; // half that logarithm.
            int m = (1 << h) - 1; // the mask to extract the number of elements in the last subarray.
            int last = (int) n & m;
            int number_of_arrays = (int) (n >> h) + (last == 0 ? 0 : 1);
            Array<T>[] arrays = f.arrayOfArrayOf(number_of_arrays);
            for (int i = 0; i < number_of_arrays-1; i++) {
                arrays[i] = f.arrayOf(1 << h);
            }
            arrays[number_of_arrays-1] = f.arrayOf(last == 0 ? 1 << h : last);
            return new BlockedArray<>(f.elementType(), arrays, h);
    }

    // Package protection only, please.
    BlockedArray(Class<T> element_type, Array<T>[] blocks, int log_block_size) {
        super(element_type);
        this.logBlockSize = log_block_size;
        this.mask = (1 << log_block_size) - 1;
        this.blocks = blocks;
        long sum = 0;
        for (int i = 0; i < blocks.length; i++) {
            long s = blocks[i].length();
            if (s != mask+1)
                if (i == 0 || i != blocks.length-1)
                    throw new IllegalArgumentException();
            sum += s;
        }
        this.size = sum;
    }

    @Override
    public T get(long l) {
        int b = (int) (l >>> logBlockSize);
        int i = (int) l & mask;
        return blocks[b].get(i);
    }

    @Override
    public void set(long l, T x) {
        int b = (int) (l >>> logBlockSize);
        int i = (int) l & mask;
        blocks[b].set(i, x);
    }

    @Override
    public boolean cas(long l, T expected, T replacement) {
        int b = (int) (l >>> logBlockSize);
        int i = (int) l & mask;
        return blocks[b].cas(i, expected, replacement);
    }

    @Override
    public long length() {
        return size;
    }

    @Override
    public long middle(long origin, long fence) {
        long mid = (origin + fence) >>> 1;
        if ((mid & ~mask) == (origin & ~mask)) {
            long mid0 = (mid | mask) + 1;
            if (mid0 < fence) {
                mid = mid0;
            }
        } else {
            mid = mid & ~mask;
        }
        return mid;
    }

    @Override
    public boolean canSimplify(long beginInclusive, long endExclusive) {
        return beginInclusive >> logBlockSize
                == (endExclusive - 1) >> logBlockSize;
    }

    @Override
    public Array<T> subArray(final long beginInclusive, final long endExclusive) {
        if (beginInclusive < 0
                || endExclusive < beginInclusive
                || length() < endExclusive) {
            throw new IllegalArgumentException();
        }
        if (beginInclusive == 0 && endExclusive == length()) {
            return this;
        }
        int b_begin = (int) (beginInclusive >> logBlockSize);
        int b_endm1 = (int) ((endExclusive - 1) >> logBlockSize);
        if (b_begin == b_endm1) {
            return blocks[b_begin].
                    subArray(beginInclusive & mask, ((endExclusive - 1) & mask) + 1);
        }
        return new SubArrayImpl<>(this, beginInclusive, endExclusive);
    }

    @Override
    public Spliterator<T> asSpliterator(final long _from, final long _to, final int _blocksize, ReductionKernel<T> rk) {
        // TODO Report parameter values in exceptions.
        // TODO is this too cute?  Would it be better to explicitly compare
        // each one against zero?
        if ((_from | _to | (_to - _from) | (length() - _to)) < 0) {
            throw new IllegalArgumentException();
        }

        if (_from >> logBlockSize == (_to - 1) >> logBlockSize) {
            // TODO Can iterate in a single array.
            // But whoops, we lose the specified blocking.

            // return Arrays.spliterator(blocks[(int) (_to >> logBlockSize)],
            //         (int) _from & mask, ((int) (_to - 1) & mask) + 1);
            return blocks[(int) (_to >> logBlockSize)].
                    asSpliterator(_from & mask, ((_to - 1) & mask) + 1,
                            _blocksize, rk);
        } else if (_from == _to) {
            // Empty case, not sure, just do the array thing for now.
            return blocks[(int) (_to >> logBlockSize)].
                    asSpliterator(
                    (int) _from & mask, ((int) (_to) & mask), _blocksize, rk);
        } else {
            // TODO need to enhance ReductionKernel
            return new Array.DefaultSpliterator<T>(this, _from, _to, _blocksize, rk) {
                @Override
                public void forEachRemaining(Consumer<? super T> action) {
                    if (action == null) {
                        throw new NullPointerException();
                    }

                    Array<T> a = array;
                    int blo = (int) (origin >> logBlockSize);
                    int bhi = (int) (fence >> logBlockSize);

                    int i = (int) (origin & mask);

                    // Handle blocks running to the end of the block.
                    for (int bi = blo; bi < bhi; bi++) {
                        try {
                            Array<T> b = blocks[bi];
                            b.asSpliterator(i,b.length(), _blocksize, rk).forEachRemaining(action);
                        } finally {
                            // i = first unprocessed element, where
                            // a thrown exception implies processed.
                            origin = (bi << logBlockSize) + i;
                        }
                        i = 0; // reset to zero for next iteration.
                    }

                    // Handle the last block, stopping before the end of the block.
                    // May also be the first block, in which case i is positioned
                    // at the first element to accept, instead of zero.
                    // Note that last block need not be a power-of-two in
                    // length.
                    int hi = (int) (fence & mask);
                    if (hi > 0) {
                        // iterate from i to (fence & mask) in block bhi
                        try {
                            Array<T> b = blocks[bhi];
                            b.asSpliterator(i,hi, _blocksize, rk).forEachRemaining(action);
                        } finally {
                            // i = first unprocessed element, where
                            // a thrown exception implies processed.
                            origin = (bhi << logBlockSize) + i;
                        }
                    }
                }
            };
        }
    }
}
