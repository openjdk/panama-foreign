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

import java.util.function.Consumer;

/**
 * Provides a subarray alias of an existing array.
 *
 * @param <T>
 */
public class SubArrayImpl<T> implements SubArray<T> {

        final private Array<T> parent;
        final private long beginInclusive;
        final private long endExclusive;

        public SubArrayImpl(Array<T> parent, long beginInclusive, long endExclusive) {
            this.parent = parent;
            this.beginInclusive = beginInclusive;
            this.endExclusive = endExclusive;
        }


        @Override
        public Class<T> elementType() {
            return parent.elementType();
        }

       @Override
        public T get(long i) {
            i = checkAndAdjust(i);
            return parent.get(i);
        }

        @Override
        public void set(long i, T x) {
            i = checkAndAdjust(i);
            parent.set(i, x);
        }

    long checkAndAdjust(long i) throws ArrayIndexOutOfBoundsException {
        if (i < 0) {
            throw new ArrayIndexOutOfBoundsException();
        }
        i += beginInclusive;
        if (i >= endExclusive) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return i;
    }

        @Override
        public boolean cas(long i, T expected, T replacement) {
            i = checkAndAdjust(i);
            return parent.cas(i, expected, replacement);
        }

        @Override
        public long length() {
            return endExclusive - beginInclusive;
        }

        @Override
        public boolean canSimplify(long start, long end) {
            return parent.canSimplify(beginInclusive + start, beginInclusive + end);
        }

        @Override
        public void applyToRange(long beginInclusive,
                    long endExclusive,
                    Consumer<? super T> action) {
            checkLegalRange(beginInclusive, endExclusive);
            parent.applyToRange(beginInclusive + this.beginInclusive,
                                 endExclusive + this.beginInclusive, action);
        }

        /**
         * Returns a subarray of this Subarray by delegating to the parent to
         * avoid long chains of sub-sub-sub-arrays.
         * Also provides the opportunity for a simplified subarray when it is
         * small enough.
         *
         * @param beginInclusive
         * @param endExclusive
         * @return
         */
        @Override
        public Array<T> subArray(final long beginInclusive, final long endExclusive) {
            if (beginInclusive == 0 && endExclusive == length()) {
                return this;
            }
            checkLegalRange(beginInclusive, endExclusive);
            return parent.subArray(beginInclusive + this.beginInclusive,
                                   endExclusive + this.beginInclusive);
        }

        @Override
        public Array<T> append(final Array<T> other) {
            if (other instanceof SubArrayImpl) {
                SubArrayImpl<T> sub_other = (SubArrayImpl<T>) other;
                if (this.parent() == sub_other.parent()
                        && this.end() == sub_other.begin())
                // Avoid deep chains of split-join, parent factory will
                // short-circuit to parent itself if possible.
                {
                    return parent.subArray(this.begin(), sub_other.end());
                }
            }
            return new AppendArray<>(this, other);
        }

        @Override
        public Array<T> parent() {
            return parent;
        }

        @Override
        public long end() {
            return endExclusive;
        }

        @Override
        public long begin() {
            return beginInclusive;
        }
    }
