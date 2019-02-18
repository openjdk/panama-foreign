/*
 *  Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.
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

/*
 * @test
 * @run testng PartialStructsTest
 */

import java.foreign.Scope;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class PartialStructsTest {

    @Test
    public void testPartialStructNested() {
        try (Scope sc = Scope.globalScope().fork()) {
            Nested.A1 a1 = sc.allocateStruct(Nested.A1.class);
            Nested.A2 a2 = sc.allocateStruct(Nested.A2.class);
            Nested.A3 a3 = sc.allocateStruct(Nested.A3.class);
            Nested.A4 a4 = sc.allocateStruct(Nested.A4.class);
            a4.i$set(42);
            a3.next$set(a4);
            a2.next$set(a3);
            a1.next$set(a2);
            assertEquals(a1.next$get().next$get().next$get().i$get(), a4.i$get());
        }
    }
    
    @Test
    public void testPartialStructSplitPos() {
        try (Scope sc = Scope.globalScope().fork()) {
            splitPos.A1 a1 = sc.allocateStruct(splitPos.A1.class);
            splitPos.A2 a2 = sc.allocateStruct(splitPos.A2.class);
            splitPos.A3 a3 = sc.allocateStruct(splitPos.A3.class);
            splitPos.A4 a4 = sc.allocateStruct(splitPos.A4.class);
            a4.i$set(42);
            a3.next$set(a4);
            a2.next$set(a3);
            a1.next$set(a2);
            assertEquals(a1.next$get().next$get().next$get().i$get(), a4.i$get());
        }
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testPartialStructSplitNeg() {
        try (Scope sc = Scope.globalScope().fork()) {
            splitNeg.A1 a1 = sc.allocateStruct(splitNeg.A1.class);
            splitNeg.A2 a2 = sc.allocateStruct(splitNeg.A2.class);
            splitNeg.A3 a3 = sc.allocateStruct(splitNeg.A3.class);
            splitNeg.A4 a4 = sc.allocateStruct(splitNeg.A4.class);
        }
    }
}
