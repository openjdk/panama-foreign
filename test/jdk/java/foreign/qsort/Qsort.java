/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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


import java.foreign.Scope;
import java.lang.invoke.MethodHandles;
import java.foreign.Libraries;
import java.foreign.NativeTypes;
import java.foreign.memory.Pointer;

/**
 * @test
 * @run main Qsort
 */
public class Qsort {
    /**
     * qsort sorting (callback) implementation
     */
    public class comparator implements stdlib.compar {
        /**
         * Sort function
         *
         * @param e1 pointer to the first object to be compared
         * @param e2 pointer to the second object to be compared
         *
         * @return an integer less than, equal to, or greater than zero if the first argument is considered to be respectively less than, equal to, or greater than the second
         */
        @Override
        public int fn(Pointer<Void> e1, Pointer<Void> e2) {
            // Extract the actual integers to be compared
            Pointer<Integer> p1 = e1.cast(NativeTypes.INT32);
            Pointer<Integer> p2 = e2.cast(NativeTypes.INT32);

            int i1 = p1.get();
            int i2 = p2.get();

            System.out.println("sort_function(" + i1 + ", " + i2 + ")");

            return i1 - i2;
        }
    }

    private void printElements(NativeIntArray arr) {
        for (int i : arr) {
            System.out.print(i + " ");
        }
        System.out.println();
    }

    public void testQsort() {
        stdlib stdlib = Libraries.bind(MethodHandles.lookup(), stdlib.class);

        int nelems = 10;
        NativeIntArray arr = new NativeIntArray(nelems);

        for (int i = 0; i < nelems; i++) {
            arr.setAt(i, nelems - 1 - i);
        }

        printElements(arr);

        try (Scope sc = Scope.newNativeScope()) {
            stdlib.qsort(arr.getBasePointer().cast(NativeTypes.VOID), arr.size(), arr.getElemSize(),
                    sc.allocateCallback(new comparator()));
        }

        printElements(arr);
    }

    public static void main(String[] args) {
        Qsort t = new Qsort();
        t.testQsort();
    }
}
