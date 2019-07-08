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

import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.Layout;
import jdk.incubator.foreign.Layout.PathElement;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.SequenceLayout;
import jdk.incubator.foreign.ValueLayout;
import java.lang.invoke.VarHandle;
import java.util.stream.LongStream;

import static org.testng.Assert.*;

public class TestMemoryAlignment {

    @Test(dataProvider = "alignments")
    public void testAlignedAccess(long align) {
        ValueLayout layout = Layout.ofSignedInt(32);
        assertEquals(layout.bitsAlignment(), 32);
        ValueLayout aligned = layout.alignTo(align);
        assertEquals(aligned.bitsAlignment(), align); //unreasonable alignment here, to make sure access throws
        VarHandle vh = aligned.dereferenceHandle(int.class);
        try (MemorySegment segment = MemorySegment.ofNative(aligned)) {
            MemoryAddress addr = segment.baseAddress();
            vh.set(addr, -42);
            int val = (int)vh.get(addr);
            assertEquals(val, -42);
        }
    }

    @Test(dataProvider = "alignments")
    public void testUnalignedAccess(long align) {
        ValueLayout layout = Layout.ofSignedInt(32);
        assertEquals(layout.bitsAlignment(), 32);
        ValueLayout aligned = layout.alignTo(align);
        Layout alignedGroup = Layout.ofStruct(Layout.ofPadding(8), aligned);
        assertEquals(alignedGroup.bitsAlignment(), align);
        VarHandle vh = aligned.dereferenceHandle(int.class);
        try (MemorySegment segment = MemorySegment.ofNative(alignedGroup)) {
            MemoryAddress addr = segment.baseAddress();
            vh.set(addr.offset(1L), -42);
            assertEquals(align, 8); //this is the only case where access is aligned
        } catch (IllegalStateException ex) {
            assertNotEquals(align, 8); //if align != 8, access is always unaligned
        }
    }

    @Test(dataProvider = "alignments")
    public void testUnalignedPath(long align) {
        Layout layout = Layout.ofSignedInt(32);
        Layout aligned = layout.alignTo(align).withName("value");
        GroupLayout alignedGroup = Layout.ofStruct(Layout.ofPadding(8), aligned);
        try {
            alignedGroup.dereferenceHandle(int.class, PathElement.groupElement("value"));
            assertEquals(align, 8); //this is the only case where path is aligned
        } catch (UnsupportedOperationException ex) {
            assertNotEquals(align, 8); //if align != 8, path is always unaligned
        }
    }

    @Test(dataProvider = "alignments")
    public void testUnalignedSequence(long align) {
        SequenceLayout layout = Layout.ofSequence(5, Layout.ofSignedInt(32).alignTo(align));
        VarHandle vh = layout.dereferenceHandle(int.class, PathElement.sequenceElement());
        try (MemorySegment segment = MemorySegment.ofNative(layout)) {
            MemoryAddress addr = segment.baseAddress();
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
        ValueLayout vChar = Layout.ofSignedInt(8);
        ValueLayout vShort = Layout.ofSignedInt(16);
        ValueLayout vInt = Layout.ofSignedInt(32);
        //mimic pragma pack(1)
        GroupLayout g = Layout.ofStruct(vChar.alignTo(8).withName("a"),
                               vShort.alignTo(8).withName("b"),
                               vInt.alignTo(8).withName("c"));
        assertEquals(g.bitsAlignment(), 8);
        VarHandle vh_c = g.dereferenceHandle(byte.class, PathElement.groupElement("a"));
        VarHandle vh_s = g.dereferenceHandle(short.class, PathElement.groupElement("b"));
        VarHandle vh_i = g.dereferenceHandle(int.class, PathElement.groupElement("c"));
        try (MemorySegment segment = MemorySegment.ofNative(g)) {
            MemoryAddress addr = segment.baseAddress();
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
