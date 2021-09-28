/*
 *  Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
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
 * @run testng/othervm TestSegmentsOverlap
 */

import java.util.List;
import java.util.function.Supplier;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import static org.testng.Assert.*;

public class TestSegmentsOverlap {

    // native

    @Test
    public void testNativeBasic() {
        var s1 = MemorySegment.allocateNative(128, ResourceScope.newConfinedScope());
        var s2 = MemorySegment.allocateNative(128, ResourceScope.newConfinedScope());
        var s3 = MemorySegment.ofArray(new byte[]{});
        assertFalse(s1.isOverlapping(s2));
        assertFalse(s1.isOverlapping(s3));
    }

    @Test
    public void testNativeIdentical() {
        var s1 = MemorySegment.allocateNative(128, ResourceScope.newConfinedScope());
        var s2 = s1.asReadOnly();
        assertTrue(s1.isOverlapping(s2));
    }

    @Test
    public void testNativeSlices() {
        MemorySegment segment = MemorySegment.allocateNative(10, 1, ResourceScope.newConfinedScope());
        MemorySegment other = MemorySegment.allocateNative(10, 1, ResourceScope.newConfinedScope());
        for (int offset = 0 ; offset < 10 ; offset++) {
            MemorySegment slice = segment.asSlice(offset);
            assertTrue(segment.isOverlapping(slice));
            assertTrue(slice.isOverlapping(segment));
            assertFalse(other.isOverlapping(slice));
        }
    }

    // heap

    @DataProvider(name = "segmentFactories")
    public Object[][] segmentFactories() {
        List<Supplier<MemorySegment>> l = List.of(
                () -> MemorySegment.ofArray(new byte[] { 0x00, 0x01, 0x02, 0x03 }),
                () -> MemorySegment.ofArray(new char[] {'a', 'b', 'c', 'd' }),
                () -> MemorySegment.ofArray(new double[] { 1d, 2d, 3d, 4d} ),
                () -> MemorySegment.ofArray(new float[] { 1.0f, 2.0f, 3.0f, 4.0f }),
                () -> MemorySegment.ofArray(new int[] { 1, 2, 3, 4 }),
                () -> MemorySegment.ofArray(new long[] { 1L, 2L, 3L, 4L } ),
                () -> MemorySegment.ofArray(new short[] { 1, 2, 3, 4 } )
        );
        return l.stream().map(s -> new Object[] { s }).toArray(Object[][]::new);
    }

    @Test(dataProvider="segmentFactories")
    public void testHeapBasic(Supplier<MemorySegment> segmentSupplier) {
        var s1 = segmentSupplier.get();
        var s2 = segmentSupplier.get();
        var s3 = MemorySegment.allocateNative(128, ResourceScope.newConfinedScope());
        assertFalse(s1.isOverlapping(s2));
        assertFalse(s1.isOverlapping(s3));
    }

    @Test(dataProvider="segmentFactories")
    public void testHeapIdentical(Supplier<MemorySegment> segmentSupplier) {
        var s1 = segmentSupplier.get();
        var s2 = s1.asReadOnly();
        assertTrue(s1.isOverlapping(s2));
    }

    @Test(dataProvider="segmentFactories")
    public void testHeapSlices(Supplier<MemorySegment> segmentSupplier) {
        MemorySegment segment = segmentSupplier.get();
        MemorySegment other = segmentSupplier.get();
        for (int offset = 0 ; offset < 4 ; offset++) {
            MemorySegment slice = segment.asSlice(offset);
            assertTrue(segment.isOverlapping(slice));
            assertTrue(slice.isOverlapping(segment));
            assertFalse(other.isOverlapping(slice));
        }
    }
}
