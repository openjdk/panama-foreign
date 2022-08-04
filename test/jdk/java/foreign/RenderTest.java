/*
 * Copyright (c) 2020, 2022, Oracle and/or its affiliates. All rights reserved.
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
 * @enablePreview
 * @modules java.base/jdk.internal.foreign
 * @run testng/othervm --enable-native-access=ALL-UNNAMED RenderTest
 */

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

import jdk.internal.foreign.MemorySegmentRenderUtil;
import org.testng.annotations.*;

import static java.util.stream.Collectors.joining;
import static org.testng.Assert.*;
import static jdk.internal.foreign.MemorySegmentRenderUtil.*;
import static java.util.Objects.requireNonNull;

@Test
public class RenderTest {

    private static final int HEX_SEGMENT_SIZE = 64 + 4;
    private static final String THE_QUICK = "The quick brown fox jumped over the lazy dog\nSecond line\t:here";

    private static final byte[] THE_QUICK_ARRAY = THE_QUICK.getBytes(StandardCharsets.UTF_8);
    private static final String EXPECTED_HEX = platformLineSeparated("""
                0000000000000000  54 68 65 20 71 75 69 63  6B 20 62 72 6F 77 6E 20  |The quick brown |
                0000000000000010  66 6F 78 20 6A 75 6D 70  65 64 20 6F 76 65 72 20  |fox jumped over |
                0000000000000020  74 68 65 20 6C 61 7A 79  20 64 6F 67 0A 53 65 63  |the lazy dog.Sec|
                0000000000000030  6F 6E 64 20 6C 69 6E 65  09 3A 68 65 72 65 00 00  |ond line.:here..|
                0000000000000040  00 00 00 00                                       |....|""");

    @Test
    public void testHexStream() {

        var actual = testWithFreshMemorySegment(HEX_SEGMENT_SIZE, segment -> {
            segment.setUtf8String(0, THE_QUICK);
            return hexDump(segment)
                    .collect(joining(System.lineSeparator()));
        });
        assertEquals(EXPECTED_HEX, actual);
    }

    @Test
    public void testHexStreamByteArray() {

        var array = new byte[HEX_SEGMENT_SIZE];
        System.arraycopy(THE_QUICK_ARRAY, 0, array, 0, THE_QUICK.length());
        var actual = hexDump(MemorySegment.ofArray(array))
                .collect(joining(System.lineSeparator()));

        assertEquals(EXPECTED_HEX, actual);
    }

    @Test
    public void testHexStreamByteBuffer() {

        var array = new byte[HEX_SEGMENT_SIZE];
        System.arraycopy(THE_QUICK_ARRAY, 0, array, 0, THE_QUICK.length());
        var actual = hexDump(MemorySegment.ofBuffer(ByteBuffer.wrap(array)))
                .collect(joining(System.lineSeparator()));

        assertEquals(EXPECTED_HEX, actual);
    }


    @Test
    public void valueLayouts() {

        var expectAddress = "0x" + "00".repeat((int) ValueLayout.ADDRESS.byteSize());

        record TestInput(ValueLayout layout, String stringValue){};
        List.of(
                new TestInput(ValueLayout.JAVA_BYTE, "0"),
                new TestInput(ValueLayout.JAVA_SHORT, "0"),
                new TestInput(ValueLayout.JAVA_INT, "0"),
                new TestInput(ValueLayout.JAVA_LONG, "0"),
                new TestInput(ValueLayout.JAVA_FLOAT, "0.0"),
                new TestInput(ValueLayout.JAVA_DOUBLE, "0.0"),
                new TestInput(ValueLayout.JAVA_CHAR, ""+(char)0),
                new TestInput(ValueLayout.JAVA_BOOLEAN, "false"),
                new TestInput(ValueLayout.ADDRESS, expectAddress)
        ).forEach(ti -> {
            var expect = ti.layout() + "=" + ti.stringValue();
            var actual = testWithFreshMemorySegment(ti.layout().byteSize(), s -> MemorySegmentRenderUtil.toString(s, ti.layout()));
            assertEquals(expect, actual);
        });
    }

    @Test
    public void point() {

        final class Point {

            private static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
                    ValueLayout.JAVA_INT.withName("x"),
                    ValueLayout.JAVA_INT.withName("y")
            ).withName("Point");

            private static final VarHandle xVH = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("x"));
            private static final VarHandle yVH = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("y"));

            private final MemorySegment memorySegment;

            public Point(MemorySegment memorySegment) {
                this.memorySegment = requireNonNull(memorySegment);
            }

            int x() {
                return (int) xVH.get(memorySegment);
            }

            int y() {
                return (int) yVH.get(memorySegment);
            }

            void x(int x) {
                xVH.set(memorySegment, x);
            }

            void y(int y) {
                yVH.set(memorySegment, y);
            }

            @Override
            public String toString() {
                return "Point {x=" + x() + ", y=" + y() + "}";
            }
        }

        var expect = platformLineSeparated("""
                Point {
                    x=1,
                    y=2
                }""");

        var actual = testWithFreshMemorySegment(Integer.BYTES * 2, segment -> {
            final Point point = new Point(segment);
            point.x(1);
            point.y(2);
            return MemorySegmentRenderUtil.toString(segment, Point.LAYOUT);
        });

        assertEquals(expect, actual);
    }

    @Test
    public void sequence() {
        final int arraySize = 4;
        var sequenceLayout = MemoryLayout.sequenceLayout(arraySize,
                MemoryLayout.structLayout(
                        ValueLayout.JAVA_INT.withName("x"),
                        ValueLayout.JAVA_INT.withName("y")
                ).withName("Point")
        ).withName("PointArrayOfElements");

        var expect = platformLineSeparated("""
                PointArrayOfElements [
                    Point {
                        x=0,
                        y=0
                    },
                    Point {
                        x=0,
                        y=0
                    },
                    Point {
                        x=0,
                        y=0
                    },
                    Point {
                        x=0,
                        y=0
                    }
                ]""");
        var actual = testWithFreshMemorySegment(Integer.BYTES * 2 * arraySize, segment ->
                MemorySegmentRenderUtil.toString(segment, sequenceLayout));

        assertEquals(expect, actual);
    }


    @Test
    public void union() {
        var u0 = MemoryLayout.structLayout(
                ValueLayout.JAVA_INT.withName("x"),
                ValueLayout.JAVA_INT.withName("y"),
                MemoryLayout.paddingLayout(Integer.SIZE)
        ).withName("Point");

        var u1 = MemoryLayout.structLayout(
                ValueLayout.JAVA_INT.withName("x"),
                ValueLayout.JAVA_INT.withName("y"),
                ValueLayout.JAVA_INT.withName("z")
        ).withName("3D-Point");

        var union = MemoryLayout.unionLayout(u0, u1).withName("Union");

        var expect = platformLineSeparated("""
                Union {
                    Point {
                        x=0,
                        y=0,
                        32 padding bits
                    }|
                    3D-Point {
                        x=0,
                        y=0,
                        z=0
                    }
                }""");
        var actual = testWithFreshMemorySegment(Integer.BYTES * 3, segment ->
                MemorySegmentRenderUtil.toString(segment, union));

        assertEquals(expect, actual);
    }

    private static String platformLineSeparated(String s) {
        return s.lines()
                .collect(joining(System.lineSeparator()));
    }

    private static <T> T testWithFreshMemorySegment(long size,
                                                    Function<MemorySegment, T> mapper) {
        try (final MemorySession session = MemorySession.openConfined()) {
            var segment = session.allocate(size);
            return mapper.apply(segment);
        }
    }

}
