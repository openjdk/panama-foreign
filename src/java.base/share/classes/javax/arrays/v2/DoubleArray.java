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

public class DoubleArray extends AbstractPrimitiveArray<Double> {

    protected final double[] array;

    static public final ArrayFactory<Double> doubleArrayFactory = new ArrayFactory<Double>() {

          @Override
          final public Array<Double>[] arrayOfArrayOf(int n) {
              return new DoubleArray[n];
          }

          @Override
          final public Array<Double> arrayOf(int n) {
              return new DoubleArray(new double[n]);
          }

          @Override
          final public Class<Double> elementType() {
              return Double.class;
          }

    };

    static public Array<Double> make(long n) {
        if (n < Integer.MAX_VALUE)
            return new DoubleArray(new double[(int) n]);
        else {
            return BlockedArray.make(n, doubleArrayFactory);
        }
    }

    /**
     * Create a DoubleArray aliased with the input Java array.
     *
     * @param array
     */
    public DoubleArray(double[] array) {
        this.array = array;
    }


    @Override
    final public Class<Double> elementType() {
        return Double.class;
    }

    @Override
    public Double get(long l) {
        int i = (int) l;
        if (i != l) {
            throw new IndexOutOfBoundsException();
        }
        return array[i];
    }

    @Override
    public void set(long l, Double x) {
        int i = (int) l;
        if (i != l) {
            throw new IndexOutOfBoundsException();
        }
        array[i] = x;
    }

    @Override
    public boolean cas(long l, Double expected, Double replacement) {
        int i = checkAndCast(l);
        // Check is mandatory for Unsafe.cas
        if (i < 0 || i >= length())
            throw new ArrayIndexOutOfBoundsException();
        l = (long) i * ImplPrivate.ARRAY_DOUBLE_INDEX_SCALE +
                       ImplPrivate.ARRAY_DOUBLE_BASE_OFFSET;
        // Not happy about this, but there is no casDouble
        return ImplPrivate.u.compareAndSetLong(array, l,
                Double.doubleToRawLongBits(expected), Double.doubleToRawLongBits(replacement));
    }


    public double getValue(long l) {
        int i = (int) l;
        if (i != l) {
            throw new IndexOutOfBoundsException();
        }
        return array[i];
    }

    public void putValue(long l, double x) {
        int i = (int) l;
        if (i != l) {
            throw new IndexOutOfBoundsException();
        }
        array[i] = x;
    }

    @Override
    public void indexedAssignmentToRange(long lo, long hi, long offset, LongFunction<Double> filler) {
        double[] a = array;
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
            Consumer<? super Double> action) {
        // hoist accesses and checks from loop
        double[] a = array;
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
            Array<Double> other, BiFunction<Double, Double, U> opr, Consumer<? super U> action) {

        // TODO specialize for array case; need RHS-biased helper method
        // to simplify RHS to an array if possible
        // hoist accesses and checks from loop
        double[] a = array;

        int ihi = (int) endExclusive;
        int ilo = (int) beginInclusive;

        if (other instanceof DoubleArray) {
            double[] b = ((DoubleArray) other).array;
            int i_offset = (int) otherOffset;
            if (ihi == endExclusive && ilo == beginInclusive && i_offset == otherOffset &&
                ihi <= a.length && ilo >= 0 && ilo + i_offset >= 0 && ilo < ihi &&
                ihi + i_offset <= b.length) {
                do {
                    action.accept(opr.apply(a[ilo], b[ilo]));
                } while (++ilo < ihi);
            }

        } else {
            if (ihi == endExclusive && ilo == beginInclusive
                    && ihi <= a.length && ilo >= 0 && ilo < ihi) {
            do {
                action.accept(opr.apply(a[ilo], other.get(ilo + otherOffset)));
            } while (++ilo < ihi);
        }
        }
    }

    @Override
    public long length() {
        return array.length;
    }

    // For purposes of testing "kernel" acceleration.
    public double[] array() {
        return array;
    }
}
