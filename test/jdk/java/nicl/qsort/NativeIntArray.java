/*
 * Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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


import java.nicl.NativeTypes;
import java.nicl.Scope;
import java.nicl.types.Pointer;
import java.util.Iterator;

public class NativeIntArray implements Iterable<Integer> {
    private static final int ELEM_SIZE = 4;

    private final int nelems;
    private final Scope scope = Scope.newNativeScope();
    private final Pointer<Integer> base;

    public NativeIntArray(int nelems) {
        this.nelems = nelems;
        this.base = scope.allocate(NativeTypes.INT32, nelems * ELEM_SIZE);
    }

    public Pointer<Integer> getBasePointer() {
        return base;
    }

    public int size() {
        return nelems;
    }

    public int getElemSize() {
        return ELEM_SIZE;
    }

    private Pointer<Integer> at(int index) {
        if (index < 0 || index >= nelems) {
            throw new IndexOutOfBoundsException();
        }

        return base.offset(index);
    }

    public int getAt(int index) {
        return at(index).get();
    }

    public void setAt(int index, int value) {
        at(index).set(value);
    }

    @Override
    public Iterator<Integer> iterator() {
        return new Iter();
    }

    class Iter implements Iterator<Integer> {
        private int curIndex;

        @Override
        public boolean hasNext() {
            return curIndex < nelems;
        }

        @Override
        public Integer next() {
            return getAt(curIndex++);
        }
    }
}
