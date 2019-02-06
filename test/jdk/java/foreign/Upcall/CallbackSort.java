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

/*
 * @test
 * @run main/othervm CallbackSort
 */

import java.foreign.annotations.NativeFunction;
import java.lang.invoke.MethodHandles;
import java.foreign.Libraries;
import java.foreign.NativeTypes;
import java.foreign.Scope;
import java.foreign.annotations.NativeCallback;
import java.foreign.annotations.NativeHeader;
import java.foreign.annotations.NativeLocation;
import java.foreign.memory.Callback;
import java.foreign.memory.Pointer;
import java.util.Iterator;

public class CallbackSort {
    private static final boolean DEBUG = Boolean.getBoolean("CallbackSort.DEBUG");
    private boolean upcallCalled = false;

    @NativeHeader
    public static interface stdlib {
        @NativeCallback("(u64:vu64:v)i32")
        @FunctionalInterface
        static interface compar {
            @NativeLocation(file="dummy", line=47, column=11)
            public int fn(Pointer<Void> e1, Pointer<Void> e2);
        }

        @NativeLocation(file="dummy", line=47, column=11)
        @NativeFunction("(u64:vu64u64u64:(u64:vu64:v)i32)v")
        public abstract void slowsort(Pointer<?> base, long nmemb, long size, Callback<compar> compar);
    }

    public class comparator implements stdlib.compar {
        @Override
        public int fn(Pointer<Void> e1, Pointer<Void> e2) {
            upcallCalled = true;

            Pointer<Integer> p1 = e1.cast(NativeTypes.INT32);
            Pointer<Integer> p2 = e2.cast(NativeTypes.INT32);

            int i1 = p1.get();
            int i2 = p2.get();

            if (DEBUG) {
                System.out.println("fn(i1=" + i1 + ", i2=" + i2 + ")");
            }

            return i1 - i2;
        }
    }

    private void doSort(NativeIntArray elems) {
        stdlib i = Libraries.bind(stdlib.class, Libraries.loadLibrary(MethodHandles.lookup(), "Upcall"));
        Pointer<?> p = elems.getBasePointer().cast(NativeTypes.VOID);
        try (Scope sc = Scope.newNativeScope()) {
            i.slowsort(p, elems.size(), elems.getElemSize(), sc.allocateCallback(new comparator()));
        }
    }

    private void printElems(NativeIntArray arr) {
        for (int i : arr) {
            System.out.print(i + " ");
        }
        System.out.println();
    }

    public void testSort() {
        int nelems = 10;
        NativeIntArray arr = new NativeIntArray(nelems);

        for (int i = 0; i < nelems; i++) {
            arr.setAt(i, nelems - 1 - i);
        }

        if (DEBUG) {
            printElems(arr);
        }

        doSort(arr);

        assertTrue(upcallCalled);

        if (DEBUG) {
            printElems(arr);
        }

        for (int i = 0; i < arr.size(); i++) {
            if (i > 0) {
                assertTrue(arr.getAt(i - 1) < arr.getAt(i));
            }
        }
    }

    public static void main(String[] args) {
        new CallbackSort().testSort();
    }

    static void assertTrue(boolean actual) {
        if (!actual) {
            throw new RuntimeException("assertion failed, actual: " + actual);
        }
    }

    static class NativeIntArray implements Iterable<Integer> {
        private static final int ELEM_SIZE = 4;

        private final Scope scope = Scope.newNativeScope();

        private final int nelems;
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
}
