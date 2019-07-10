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
 * @run testng TestLayoutConstants
 */

import jdk.incubator.foreign.MemoryLayout;

import java.lang.invoke.MethodHandles;
import java.nio.ByteOrder;

import org.testng.annotations.*;
import static org.testng.Assert.*;

public class TestLayoutConstants {

    @Test(dataProvider = "layouts")
    public void testDescribeResolve(MemoryLayout expected) {
        try {
            MemoryLayout actual = expected.describeConstable().get()
                    .resolveConstantDesc(MethodHandles.lookup());
            assertEquals(actual, expected);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }

    @DataProvider(name = "layouts")
    public Object[][] createLayouts() {
        return new Object[][] {
                //padding
                { MemoryLayout.ofPadding(32) },
                { MemoryLayout.ofSequence(MemoryLayout.ofPadding(32)) },
                { MemoryLayout.ofSequence(5, MemoryLayout.ofPadding(32)) },
                { MemoryLayout.ofStruct(MemoryLayout.ofPadding(32), MemoryLayout.ofPadding(32)) },
                { MemoryLayout.ofUnion(MemoryLayout.ofPadding(32), MemoryLayout.ofPadding(32)) },
                //values, floating point
                { MemoryLayout.ofFloatingPoint(32) },
                { MemoryLayout.ofFloatingPoint(ByteOrder.LITTLE_ENDIAN, 32) },
                { MemoryLayout.ofFloatingPoint(ByteOrder.BIG_ENDIAN, 32) },
                { MemoryLayout.ofSequence(MemoryLayout.ofFloatingPoint(32)) },
                { MemoryLayout.ofSequence(5, MemoryLayout.ofFloatingPoint(32)) },
                { MemoryLayout.ofStruct(MemoryLayout.ofFloatingPoint(32), MemoryLayout.ofFloatingPoint(32)) },
                { MemoryLayout.ofUnion(MemoryLayout.ofFloatingPoint(32), MemoryLayout.ofFloatingPoint(32)) },
                //values, signed int
                { MemoryLayout.ofSignedInt(32) },
                { MemoryLayout.ofSignedInt(ByteOrder.LITTLE_ENDIAN, 32) },
                { MemoryLayout.ofSignedInt(ByteOrder.BIG_ENDIAN, 32) },
                { MemoryLayout.ofSequence(MemoryLayout.ofSignedInt(32)) },
                { MemoryLayout.ofSequence(5, MemoryLayout.ofSignedInt(32)) },
                { MemoryLayout.ofStruct(MemoryLayout.ofSignedInt(32), MemoryLayout.ofSignedInt(32)) },
                { MemoryLayout.ofUnion(MemoryLayout.ofSignedInt(32), MemoryLayout.ofSignedInt(32)) },
                //values, unsigned int
                { MemoryLayout.ofUnsignedInt(32) },
                { MemoryLayout.ofUnsignedInt(ByteOrder.LITTLE_ENDIAN, 32) },
                { MemoryLayout.ofUnsignedInt(ByteOrder.BIG_ENDIAN, 32) },
                { MemoryLayout.ofSequence(MemoryLayout.ofUnsignedInt(32)) },
                { MemoryLayout.ofSequence(5, MemoryLayout.ofUnsignedInt(32)) },
                { MemoryLayout.ofStruct(MemoryLayout.ofUnsignedInt(32), MemoryLayout.ofUnsignedInt(32)) },
                { MemoryLayout.ofUnion(MemoryLayout.ofUnsignedInt(32), MemoryLayout.ofUnsignedInt(32)) },
                //deeply nested
                { MemoryLayout.ofStruct(MemoryLayout.ofPadding(16), MemoryLayout.ofStruct(MemoryLayout.ofPadding(8), MemoryLayout.ofSignedInt(32))) },
                { MemoryLayout.ofUnion(MemoryLayout.ofPadding(16), MemoryLayout.ofStruct(MemoryLayout.ofPadding(8), MemoryLayout.ofSignedInt(32))) },
                { MemoryLayout.ofSequence(MemoryLayout.ofStruct(MemoryLayout.ofPadding(8), MemoryLayout.ofSignedInt(32))) },
                { MemoryLayout.ofSequence(5, MemoryLayout.ofStruct(MemoryLayout.ofPadding(8), MemoryLayout.ofSignedInt(32))) },
        };
    }
}
