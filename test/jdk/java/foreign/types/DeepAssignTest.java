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

/*
 * @test
 * @run testng DeepAssignTest
 */

import java.foreign.NativeTypes;
import java.foreign.Scope;
import java.foreign.annotations.NativeStruct;
import java.foreign.memory.Array;
import java.foreign.memory.Struct;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class DeepAssignTest {

    @NativeStruct("[" +
            "i32(get=x$get)(set=x$set)" +
            "i32(get=y$get)(set=y$set)" +
            "]")
    interface Pair extends Struct<Pair> {
        int x$get();
        void x$set(int x);
        int y$get();
        void y$set(int y);
    }

    @NativeStruct("[" +
            "i32(get=x$get)(set=x$set)" +
            "i32(get=y$get)(set=y$set)" +
            "i32(get=z$get)(set=z$set)" +
            "]")
    interface Triple extends Struct<Triple> {
        int x$get();
        void x$set(int x);
        int y$get();
        void y$set(int y);
        int z$get();
        void z$set(int z);
    }

    @Test
    public void testAssignPairPair() {
        try (Scope sc = Scope.newNativeScope()) {
            Pair p1 = sc.allocateStruct(Pair.class);
            p1.x$set(1);
            p1.y$set(2);
            Pair p2 = sc.allocateStruct(Pair.class);
            Struct.assign(p1, p2);
            assertEquals(p1.x$get(), p2.x$get());
            assertEquals(p1.y$get(), p2.y$get());
        }
    }

    @Test
    public void testAssignTripleTriple() {
        try (Scope sc = Scope.newNativeScope()) {
            Triple t1 = sc.allocateStruct(Triple.class);
            t1.x$set(1);
            t1.y$set(2);
            t1.z$set(3);
            Triple t2 = sc.allocateStruct(Triple.class);
            Struct.assign(t1, t2);
            assertEquals(t1.x$get(), t2.x$get());
            assertEquals(t1.y$get(), t2.y$get());
            assertEquals(t1.z$get(), t2.z$get());
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAssignTriplePair() {
        try (Scope sc = Scope.newNativeScope()) {
            Triple t1 = sc.allocateStruct(Triple.class);
            t1.x$set(1);
            t1.y$set(2);
            t1.z$set(3);
            Pair p2 = sc.allocateStruct(Pair.class);
            //fool the type system
            Struct.assign((Struct)t1, p2);
        }
    }

    @Test
    public void testAssignArraySameSize() {
        try (Scope sc = Scope.newNativeScope()) {
            Array<Integer> a1 = sc.allocateArray(NativeTypes.INT32, new int[] { 1, 2, 3});
            Array<Integer> a2 = sc.allocateArray(NativeTypes.INT32, 3);
            Array.assign(a1, a2);
            assertEquals(a1.toArray(int[]::new), a2.toArray(int[]::new));
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAssignArrayDifferentSize() {
        try (Scope sc = Scope.newNativeScope()) {
            Array<Integer> a1 = sc.allocateArray(NativeTypes.INT32, new int[] { 1, 2, 3});
            Array<Integer> a2 = sc.allocateArray(NativeTypes.INT32, 5);
            Array.assign(a1, a2);
        }
    }
}
