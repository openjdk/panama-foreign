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
 * @run testng TestMemoryAlignment
 */

import org.testng.annotations.*;

import java.foreign.GroupLayout;
import java.foreign.Layout;
import java.foreign.MemoryAddress;
import java.foreign.MemoryScope;
import java.foreign.PaddingLayout;
import java.foreign.SequenceLayout;
import java.foreign.ValueLayout;
import java.lang.invoke.VarHandle;
import java.util.stream.LongStream;

import static org.testng.Assert.*;

public class TestMemoryAlignment {

    @Test(dataProvider = "alignments")
    public void testAlignedAccess(long align) {
        Layout layout = ValueLayout.ofSignedInt(32);
        assertEquals(layout.alignmentBits(), 32);
        Layout aligned = layout.alignTo(align);
        assertEquals(aligned.alignmentBits(), align); //unreasonable alignment here, to make sure access throws
        VarHandle vh = aligned.toPath().dereferenceHandle(int.class);
        try (MemoryScope scope = MemoryScope.globalScope().fork()) {
            MemoryAddress addr = scope.allocate(aligned);
            vh.set(addr, -42);
            int val = (int)vh.get(addr);
            assertEquals(val, -42);
        }
    }

    @Test(dataProvider = "alignments")
    public void testUnalignedAccess(long align) {
        Layout layout = ValueLayout.ofSignedInt(32);
        assertEquals(layout.alignmentBits(), 32);
        Layout aligned = layout.alignTo(align);
        Layout alignedGroup = GroupLayout.struct(PaddingLayout.of(8), aligned);
        assertEquals(alignedGroup.alignmentBits(), align);
        VarHandle vh = aligned.toPath().dereferenceHandle(int.class);
        try (MemoryScope scope = MemoryScope.globalScope().fork()) {
            MemoryAddress addr = scope.allocate(alignedGroup);
            vh.set(addr.offset(1L), -42);
            assertEquals(align, 8); //this is the only case where access is aligned
        } catch (IllegalStateException ex) {
            assertNotEquals(align, 8); //if align != 8, access is always unaligned
        }
    }

    @Test(dataProvider = "alignments")
    public void testUnalignedPath(long align) {
        Layout layout = ValueLayout.ofSignedInt(32);
        Layout aligned = layout.alignTo(align);
        Layout alignedGroup = GroupLayout.struct(PaddingLayout.of(8), aligned);
        try {
            alignedGroup.toPath().elementPath(1).dereferenceHandle(int.class);
            assertEquals(align, 8); //this is the only case where path is aligned
        } catch (UnsupportedOperationException ex) {
            assertNotEquals(align, 8); //if align != 8, path is always unaligned
        }
    }

    @Test(dataProvider = "alignments")
    public void testUnalignedSequence(long align) {
        Layout layout = SequenceLayout.of(5, ValueLayout.ofSignedInt(32).alignTo(align));
        VarHandle vh = layout.toPath().elementPath().dereferenceHandle(int.class);
        try (MemoryScope scope = MemoryScope.globalScope().fork()) {
            MemoryAddress addr = scope.allocate(layout);
            for (long i = 0 ; i < 5 ; i++) {
                vh.set(addr, i, -42);
            }
            assertTrue(align <= 32); //if align <= 32, access is always aligned
        } catch (IllegalStateException ex) {
            assertTrue(align > 32); //if align > 32, access is always unaligned (for some elements)
        }
    }

    @Test
    public void testPackedAccess() {
        ValueLayout vChar = ValueLayout.ofSignedInt(8);
        ValueLayout vShort = ValueLayout.ofSignedInt(16);
        ValueLayout vInt = ValueLayout.ofSignedInt(32);
        //mimic pragma pack(1)
        GroupLayout g = GroupLayout.struct(vChar.alignTo(8),
                               vShort.alignTo(8),
                               vInt.alignTo(8));
        assertEquals(g.alignmentBits(), 8);
        VarHandle vh_c = g.toPath().elementPath(0).dereferenceHandle(byte.class);
        VarHandle vh_s = g.toPath().elementPath(1).dereferenceHandle(short.class);
        VarHandle vh_i = g.toPath().elementPath(2).dereferenceHandle(int.class);
        try (MemoryScope scope = MemoryScope.globalScope().fork()) {
            MemoryAddress addr = scope.allocate(g);
            vh_c.set(addr, Byte.MIN_VALUE);
            assertEquals(vh_c.get(addr), Byte.MIN_VALUE);
            vh_s.set(addr, Short.MIN_VALUE);
            assertEquals(vh_s.get(addr), Short.MIN_VALUE);
            vh_i.set(addr, Integer.MIN_VALUE);
            assertEquals(vh_i.get(addr), Integer.MIN_VALUE);
        }
    }

    @DataProvider(name = "alignments")
    public Object[][] createAlignments() {
        return LongStream.range(3, 32)
                .mapToObj(v -> new Object[] { 1L << v })
                .toArray(Object[][]::new);
    }
}
