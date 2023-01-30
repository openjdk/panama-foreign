/*
 * Copyright (c) 2019, 2022, Oracle and/or its affiliates. All rights reserved.
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
 *   Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 *
 */

/*
 * @test
 * @enablePreview
 * @run testng TestDereferencePath
 */

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.MemorySession;

import java.lang.foreign.ValueLayout;
import org.testng.annotations.*;

import java.lang.invoke.VarHandle;
import static org.testng.Assert.*;

public class TestDereferencePath {

    static final MemoryLayout A = MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withName("b")
    );

    static final MemoryLayout B = MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withName("c")
    );

    static final MemoryLayout C = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("x")
    );

    static final VarHandle abcx = A.varHandle(
            PathElement.groupElement("b"), PathElement.derefElement(B),
            PathElement.groupElement("c"), PathElement.derefElement(C),
            PathElement.groupElement("x"));

    @Test
    public void testSingle() {
        try (MemorySession session = MemorySession.openConfined()) {
            // init structs
            MemorySegment a = MemorySegment.allocateNative(A, session);
            MemorySegment b = MemorySegment.allocateNative(B, session);
            MemorySegment c = MemorySegment.allocateNative(C, session);
            // init struct fields
            a.set(ValueLayout.ADDRESS, 0, b);
            b.set(ValueLayout.ADDRESS, 0, c);
            c.set(ValueLayout.JAVA_INT, 0, 42);
            // dereference
            int val = (int) abcx.get(a);
            assertEquals(val, 42);
        }
    }

    static final VarHandle abcx_multi = A.varHandle(
            PathElement.groupElement("b"), PathElement.derefElement(MemoryLayout.sequenceLayout(2, B)), PathElement.sequenceElement(),
            PathElement.groupElement("c"), PathElement.derefElement(MemoryLayout.sequenceLayout(2, C)), PathElement.sequenceElement(),
            PathElement.groupElement("x"));

    @Test
    public void testMulti() {
        try (MemorySession session = MemorySession.openConfined()) {
            // init structs
            MemorySegment a = session.allocate(A);
            MemorySegment b = session.allocateArray(B, 2);
            MemorySegment c = session.allocateArray(C, 4);
            // init struct fields
            a.set(ValueLayout.ADDRESS, 0, b);
            b.set(ValueLayout.ADDRESS, 0, c);
            b.setAtIndex(ValueLayout.ADDRESS, 1, c.asSlice(C.byteSize() * 2));
            c.setAtIndex(ValueLayout.JAVA_INT, 0, 1);
            c.setAtIndex(ValueLayout.JAVA_INT, 1, 2);
            c.setAtIndex(ValueLayout.JAVA_INT, 2, 3);
            c.setAtIndex(ValueLayout.JAVA_INT, 3, 4);
            // dereference
            int val00 = (int) abcx_multi.get(a, 0, 0); // a->b[0]->c[0] = 1
            assertEquals(val00, 1);
            int val10 = (int) abcx_multi.get(a, 1, 0); // a->b[1]->c[0] = 3
            assertEquals(val10, 3);
            int val01 = (int) abcx_multi.get(a, 0, 1); // a->b[0]->c[1] = 2
            assertEquals(val01, 2);
            int val11 = (int) abcx_multi.get(a, 1, 1); // a->b[1]->c[1] = 4
            assertEquals(val11, 4);
        }
    }
}
