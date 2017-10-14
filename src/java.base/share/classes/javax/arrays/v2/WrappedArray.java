/*
 *  Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.  Oracle designates this
 *  particular file as subject to the "Classpath" exception as provided
 *  by Oracle in the LICENSE file that accompanied this code.
 *
 *  This code is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  version 2 for more details (a copy is included in the LICENSE file that
 *  accompanied this code).
 *
 *  You should have received a copy of the GNU General Public License version
 *  2 along with this work; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *  Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 */

package javax.arrays.v2;

/**
 * A base class for Array "wrapper" class that by default delegates calls
 * to the wrapped array.  Example use is for a "ConstantArray" wrapper
 * that forbids calls to the set method.
 *
 * (This may be a dumb idea; the cost of the forwarding operation is
 * not trivial, and there may not be many use cases beyond ConstantArrays).
 *
 * @param <T>
 */
public class WrappedArray<T> implements Array<T> {

    protected final Array<T> wrappee;

    public WrappedArray(Array<T> wrappee) {
        this.wrappee = wrappee;
    }

    @Override
    public Class<T> elementType() {
        return wrappee.elementType();
    }

    /**
     * Wrap another array just like this one.
     * It is expected that subclasses will
     * supply their own implementations of this method.
     *
     * @param x
     * @return
     */
    public Array<T> wrapLikeThis(Array<T> x) {
        return new WrappedArray<>(x);
    }

    @Override
    public void checkLegalRange(long beginInclusive, long endExclusive) throws IllegalArgumentException {
        wrappee.checkLegalRange(beginInclusive, endExclusive); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Array<T> subArray(long beginInclusive, long endExclusive) {
        return wrapLikeThis(wrappee.subArray(beginInclusive, endExclusive));
    }

    @Override
    public boolean canSimplify(long beginInclusive, long endExclusive) {
        return wrappee.canSimplify(beginInclusive, endExclusive); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long middle(long beginInclusive, long endExclusive) {
        return wrappee.middle(beginInclusive, endExclusive); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public T get(long i) {
        return wrappee.get(i);
    }

    @Override
    public void set(long i, T x) {
        wrappee.set(i, x);
    }

    @Override
    public boolean cas(long i, T expected, T replacement) {
        return wrappee.cas(i, expected, replacement);
    }

    @Override
    public long length() {
        return wrappee.length();
    }

    static public  <T>  ConstantArray<T> constantArray (Array<T> x) {
        if (x instanceof SubArray) {
            return new ConstantSubArray<>((SubArray<T>)x);
        } else {
            return new ConstantArray<>(x);
        }
    }

    static public class ConstantArray<T> extends WrappedArray<T> {
        public ConstantArray(Array<T> x) {
            super(x);
        }

        @Override
        public Array<T> wrapLikeThis(Array<T> x) {
            return constantArray(x);
        }

        @Override
        final public void set(long i, T x) {
            throw new IllegalAccessError("Modification of ConstantArray not permitted.");
        }
    }

    static public class ConstantSubArray<T> extends ConstantArray<T> implements SubArray <T> {
        public ConstantSubArray(SubArray<T> x) {
            super(x);
        }

        @Override
        public Array<T> parent() {
            return wrapLikeThis(((SubArray<T>) wrappee).parent());
        }

        @Override
        public long end() {
            return ((SubArray) wrappee).end();
        }

        @Override
        public long begin() {
            return ((SubArray) wrappee).begin();
        }
    }

}
