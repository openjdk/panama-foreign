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

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.function.Supplier;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import static java.lang.System.out;
import static org.testng.Assert.*;

public class TestSegmentsOverlap {

    static Path tempPath;

    static {
        try {
            File file = File.createTempFile("buffer", "txt");
            file.deleteOnExit();
            tempPath = file.toPath();
            Files.write(file.toPath(), new byte[256], StandardOpenOption.WRITE);

        } catch (IOException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    // native

    @DataProvider(name = "nativeSegmentFactories")
    public Object[][] nativeSegmentFactories() {
        List<Supplier<MemorySegment>> l = List.of(
                () -> MemorySegment.allocateNative(128, ResourceScope.newConfinedScope()),
                () -> {
                    try {
                        return MemorySegment.mapFile(tempPath, 0L, 16, FileChannel.MapMode.READ_WRITE, ResourceScope.newConfinedScope());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        return l.stream().map(s -> new Object[] { s }).toArray(Object[][]::new);
    }

    @Test(dataProvider="nativeSegmentFactories")
    public void testNativeBasic(Supplier<MemorySegment> segmentSupplier) {
        var s1 = segmentSupplier.get();
        var s2 = segmentSupplier.get();
        var sHeap = MemorySegment.ofArray(new byte[]{});
        out.format("testNativeBasic s1:%s, s2:%s, sHeap:%s\n", s1, s2, sHeap);
        assertFalse(s1.isOverlapping(s2));
        assertFalse(s2.isOverlapping(s1));
        assertFalse(s1.isOverlapping(sHeap));
    }

    @Test(dataProvider="nativeSegmentFactories")
    public void testNativeIdentical(Supplier<MemorySegment> segmentSupplier) {
        var s1 = segmentSupplier.get();
        var s2 = s1.asReadOnly();
        out.format("testNativeIdentical s1:%s, s2:%s\n", s1, s2);
        assertTrue(s1.isOverlapping(s2));
        assertTrue(s2.isOverlapping(s1));
    }

    @Test(dataProvider="nativeSegmentFactories")
    public void testNativeSlices(Supplier<MemorySegment> segmentSupplier) {
        MemorySegment s1 = segmentSupplier.get();
        MemorySegment s2 = segmentSupplier.get();
        for (int offset = 0 ; offset < 10 ; offset++) {
            MemorySegment slice = s1.asSlice(offset);
            out.format("testNativeSlices s1:%s, s2:%s, slice:%s, offset:%d\n", s1, s2, slice, offset);
            assertTrue(s1.isOverlapping(slice));
            assertTrue(slice.isOverlapping(s1));
            assertFalse(s2.isOverlapping(slice));
        }
    }

    // heap

    @DataProvider(name = "heapSegmentFactories")
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

    @Test(dataProvider="heapSegmentFactories")
    public void testHeapBasic(Supplier<MemorySegment> segmentSupplier) {
        var s1 = segmentSupplier.get();
        var s2 = segmentSupplier.get();
        var sNative = MemorySegment.allocateNative(128, ResourceScope.newConfinedScope());
        out.format("testHeapBasic s1:%s, s2:%s, sNative:%s\n", s1, s2, sNative);
        assertFalse(s1.isOverlapping(s2));
        assertFalse(s2.isOverlapping(s1));
        assertFalse(s1.isOverlapping(sNative));
    }

    @Test(dataProvider="heapSegmentFactories")
    public void testHeapIdentical(Supplier<MemorySegment> segmentSupplier) {
        var s1 = segmentSupplier.get();
        var s2 = s1.asReadOnly();
        out.format("testHeapIdentical s1:%s, s2:%s\n", s1, s2);
        assertTrue(s1.isOverlapping(s2));
        assertTrue(s2.isOverlapping(s1));
    }

    @Test(dataProvider="heapSegmentFactories")
    public void testHeapSlices(Supplier<MemorySegment> segmentSupplier) {
        MemorySegment s1 = segmentSupplier.get();
        MemorySegment s2 = segmentSupplier.get();
        for (int offset = 0 ; offset < 4 ; offset++) {
            MemorySegment slice = s1.asSlice(offset);
            out.format("testHeapSlices s1:%s, s2:%s, slice:%s\n", s1, s2, slice);
            assertTrue(s1.isOverlapping(slice));
            assertTrue(slice.isOverlapping(s1));
            assertFalse(s2.isOverlapping(slice));
        }
    }
}
