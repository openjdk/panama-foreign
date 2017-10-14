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


/**
 * An array aliased with the concatenation of two other arrays "left" and "right".
 *
 * @param <T>
 */
public class AppendArray<T> extends AbstractArray<T> {

    private final Array<T> left;
    private final Array<T> right;
    private final long split;

    public AppendArray(Array<T> left, Array<T> right) {
        super(left.elementType());
        this.left = left;
        this.right = right;
        this.split = left.length();
    }

    @Override
    public T get(long i) {
        return i < split ? left.get(i) : right.get(i - split);
    }

    @Override
    public void set(long i, T x) {
        if (i < split) {
            left.set(i, x);
        } else {
            right.set(i - split, x);
        }
    }

    @Override
    public boolean cas(long i, T expected, T replacement) {
        return i < split ? left.cas(i, expected, replacement) :
                           right.cas(i - split, expected, replacement);

    }

    @Override
    public long length() {
        return split + right.length();
    }

    @Override
    public long middle(long from, long to) {
        long trial_middle = (from + to) >>> 1;
        long mid_split_diff = trial_middle - split;
        if (mid_split_diff > 0) {
            // from BIG split SMALL mid
            if (split - from > mid_split_diff) {
                trial_middle = split;
            }
        } else if (mid_split_diff < 0) {
            // mid SMALL split LARGE to
            if (split - to < mid_split_diff) {
                trial_middle = split;
            }
        }
        return trial_middle;
    }

    @Override
    public boolean canSimplify(long beginInclusive, long endExclusive) {
        // Don't bother rejecting illegal subarrays, that will sort out later.
        return beginInclusive >= split || endExclusive <= split;
    }

    @Override
    public Array<T> subArray(final long beginInclusive, final long endExclusive) {
        if (beginInclusive == 0 && endExclusive == length()) {
            return this;
        }
        if (beginInclusive < 0
                || endExclusive < beginInclusive
                || length() < endExclusive) {
            throw new IllegalArgumentException();
        }

        // Prefer simpler indexing -- catch the left, and right cases.
        if (endExclusive <= split) {
            return left.subArray(beginInclusive, endExclusive);
        }
        if (beginInclusive >= split) {
            return right.subArray(beginInclusive - split, endExclusive - split);
        }
        return new SubArrayImpl<>(this, beginInclusive, endExclusive);
    }
}
