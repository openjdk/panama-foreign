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

import static javax.arrays.v2.Array.fromArray;
import java.util.Arrays;
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

@Deprecated
public class JavaBlockedArray<T> extends AbstractArray<T> {

    protected final int logBlockSize;
    protected final int mask;
    protected final T[][] blocks;
    protected final long size;

    @SuppressWarnings("unchecked")
    public JavaBlockedArray(T[][] blocks, int log_block_size) {
        // Not a problem element, element types must match.
        // Note this is further checked below.
        super((Class<T>)blocks.getClass().getComponentType().getComponentType());
        this.logBlockSize = log_block_size;
        this.mask = (1 << log_block_size) - 1;
        this.blocks = blocks;
        long sum = 0;
        for (int i = 0; i < blocks.length; i++) {
            long s = blocks[i].length;
            if (s != mask+1)
                if (i == 0 || i != blocks.length-1)
                    throw new IllegalArgumentException("Block length must be " + (mask+1) + ", not " + s);
            if (blocks[i].getClass().getComponentType() != elementType)
                throw new IllegalArgumentException("Element type of block must be " +
                        elementType.getName() + ", not " + blocks[i].getClass().getComponentType().getName());
            sum += s;
        }
        this.size = sum;
    }

    @Override
    public T get(long l) {
        int b = (int) (l >>> logBlockSize);
        int i = (int) l & mask;
        return blocks[b][i];
    }

    @Override
    public void set(long l, T x) {
        int b = (int) (l >>> logBlockSize);
        int i = (int) l & mask;
        blocks[b][i] = x;
    }

    @Override
    public boolean cas(long l, T expected, T replacement) {
        // Check is mandatory for Unsafe.cas
        if (l < 0 || l >= length())
            throw new ArrayIndexOutOfBoundsException();
        int b = (int) (l >>> logBlockSize);
        int i = (int) l & mask;

        T[] a = blocks[b];
        if (i >= a.length) // math ought to protect us, but belt and suspenders.
            throw new ArrayIndexOutOfBoundsException();

        l = (long) i * ImplPrivate.ARRAY_OBJECT_INDEX_SCALE +
                       ImplPrivate.ARRAY_OBJECT_BASE_OFFSET;

        return ImplPrivate.u.compareAndSetObject(a, l,
                expected, replacement);

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
            return fromArray(blocks[b_begin]).
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
            return fromArray(blocks[(int) (_to >> logBlockSize)]).
                    asSpliterator(_from & mask, ((_to - 1) & mask) + 1,
                            _blocksize, rk);
        } else if (_from == _to) {
            // Empty case, not sure, just do the array thing for now.
            return Arrays.spliterator(blocks[(int) (_to >> logBlockSize)],
                    (int) _from & mask, ((int) (_to) & mask));
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
                            T[] b = blocks[bi];
                            int hi = b.length;
                            if (i < hi) {
                                do {
                                    action.accept(b[i++]);
                                } while (i < hi);
                            }
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
                            T[] b = blocks[bhi];
                            if (hi <= b.length && i < hi) {
                                do {
                                    action.accept(b[i++]);
                                } while (i < hi);
                            }
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
