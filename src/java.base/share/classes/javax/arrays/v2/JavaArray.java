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

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.LongFunction;

public class JavaArray<T> extends AbstractArray<T> {

    protected final T[] array;

    @SuppressWarnings("unchecked")
    public JavaArray(T[] array) {
        // Should not be an actual problem, the element type is correct.
        super((Class<T>)array.getClass().getComponentType());
        this.array = array;
    }

    @Override
    public T get(long l) {
        int i = checkAndCast(l);
        return array[i];
    }

    @Override
    public void set(long l, T x) {
        int i = checkAndCast(l);
        array[i] = x;
    }

    @Override
    public boolean cas(long l, T expected, T replacement) {
        int i = checkAndCast(l);
        // Check is mandatory for Unsafe.cas
        if (i < 0 || i >= length())
            throw new ArrayIndexOutOfBoundsException();

        l = (long) i * ImplPrivate.ARRAY_OBJECT_INDEX_SCALE +
                       ImplPrivate.ARRAY_OBJECT_BASE_OFFSET;

        return ImplPrivate.u.compareAndSetObject(array, l,
                expected, replacement);

    }

    @Override
    public void indexedAssignmentToRange(long lo, long hi, long offset, LongFunction<T> filler) {
        T[] a = array;
        int ihi = (int) hi;
        int ilo = (int) lo;
        if (lo < 0 || lo != ilo || hi != ihi || ihi > a.length) {
            throw new IndexOutOfBoundsException();
        }

        for (int i = ilo; i < ihi; i++) {
            a[i] = filler.apply(i + offset);
        }
    }

    @Override
    public void applyToRange(long beginInclusive,
            long endExclusive,
            Consumer<? super T> action) {
        // hoist accesses and checks from loop
        T[] a = array;
        int ilo = (int) beginInclusive;
        int ihi = (int) endExclusive;
        if (ihi <= a.length && ilo >= 0 && ilo < ihi
                && ilo == beginInclusive && ihi == endExclusive) {
            do {
                action.accept(a[ilo]);
            } while (++ilo < ihi);
        }
    }

    @Override
    public <U> void zipAndApplyToRange(long beginInclusive, long endExclusive, long otherOffset,
            Array<T> other, BiFunction<T, T, U> opr, Consumer<? super U> action) {

        // TODO specialize for array case; need RHS-biased helper method
        // to simplify RHS to an array if possible
        // hoist accesses and checks from loop
        T[] a = array;
        int i_end = (int) endExclusive;
        int i_begin = (int) beginInclusive;
        if (i_end == endExclusive && i_begin == beginInclusive &&
                i_end <= a.length && i_end + otherOffset <= other.length() &&
                i_begin >= 0 && i_begin + otherOffset >= 0 && i_begin < i_end) {
            do {
                action.accept(opr.apply(a[i_begin], other.get(i_begin + otherOffset)));
            } while (++i_begin < i_end);
        }
    }

    @Override
    public long length() {
        return array.length;
    }
}
