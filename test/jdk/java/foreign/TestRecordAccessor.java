/*
 *  Copyright (c) 2023, Oracle and/or its affiliates. All rights reserved.
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
 * @enablePreview
 * @run junit/othervm --enable-native-access=ALL-UNNAMED TestRecordAccessor
 */

import org.junit.jupiter.api.*;

import java.lang.foreign.Arena;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.util.List;
import java.util.function.Function;

import static java.lang.foreign.ValueLayout.*;
import static org.junit.jupiter.api.Assertions.*;

public class TestRecordAccessor {

    private static final GroupLayout POINT_LAYOUT = MemoryLayout.structLayout(
            JAVA_INT.withName("x"),
            JAVA_INT.withName("y"));

    private static final GroupLayout LINE_LAYOUT = MemoryLayout.structLayout(
            POINT_LAYOUT.withName("begin"),
            POINT_LAYOUT.withName("end"));

    private static final MemorySegment POINT_SEGMENT = MemorySegment.ofArray(new int[]{
            3, 4,
            6, 0,
            0, 0});

    // Records

    public record Point(int x, int y) {
    }

    public record FlippedPoint(int y, int x) {
    }

    public record PointUnion(Point normal, FlippedPoint flipped) {
        static final GroupLayout LAYOUT = MemoryLayout.unionLayout(
                POINT_LAYOUT.withName("normal"),
                POINT_LAYOUT.withName("flipped")
        );
    }

    public record PointUnionUnion(PointUnion left, PointUnion right) {
        static final GroupLayout LAYOUT = MemoryLayout.unionLayout(
                PointUnion.LAYOUT.withName("left"),
                PointUnion.LAYOUT.withName("right")
        );
    }


    public record LongPoint(long x, long y) {
    }

    public record Line(Point begin, Point end) {
    }

    // Manually declared function

    static class PointMapper implements Function<MemorySegment, Point> {

        @Override
        public Point apply(MemorySegment segment) {
            return new Point(segment.get(JAVA_INT, 0), segment.get(JAVA_INT, 4));
        }

    }

    @Test
    public void testCustomPoint() {
        test(POINT_SEGMENT, new PointMapper(), new Point(3, 4));
    }

    @Test
    public void testPointMapper() {
        test(POINT_SEGMENT, POINT_LAYOUT.recordMapper(Point.class), new Point(3, 4));
    }

    @Test
    public void testLongPointMapper() {
        // This should fail as the types `int` and `long` differ
        assertThrows(IllegalArgumentException.class, () ->
                POINT_LAYOUT.recordMapper(LongPoint.class)
        );
    }

    @Test
    public void testFlippedPointMapper() {
        test(POINT_SEGMENT, POINT_LAYOUT.recordMapper(FlippedPoint.class), new FlippedPoint(4, 3));
    }

    // Line

    @Test
    public void testLineMapper() {
        test(POINT_SEGMENT, LINE_LAYOUT.recordMapper(Line.class), new Line(new Point(3, 4), new Point(6, 0)));
    }

    // Union
    @Test
    public void testUnion() {
        test(POINT_SEGMENT, PointUnion.LAYOUT.recordMapper(PointUnion.class),
                new PointUnion(
                        new Point(3, 4),
                        new FlippedPoint(4, 3))
        );
    }

    // Union of Union
    @Test
    public void testUnionUnion() {
        test(POINT_SEGMENT, PointUnionUnion.LAYOUT.recordMapper(PointUnionUnion.class),
                new PointUnionUnion(
                        new PointUnion(
                                new Point(3, 4),
                                new FlippedPoint(4, 3)),
                        new PointUnion(
                                new Point(3, 4),
                                new FlippedPoint(4, 3))
                ));
    }

    // Test Padding
    @Test
    public void testPadding() {
        GroupLayout paddedPointLayout = MemoryLayout.structLayout(
                MemoryLayout.paddingLayout(Integer.SIZE * 2).withName("padding"),
                JAVA_INT.withName("x"),
                JAVA_INT.withName("y"));
        test(POINT_SEGMENT, paddedPointLayout.recordMapper(Point.class), new Point(6, 0));
    }

    @Test
    public void testStream() {

        List<Point> points = POINT_SEGMENT.elements(POINT_LAYOUT)
                .map(POINT_LAYOUT.recordMapper(Point.class))
                .toList();

        assertEquals(List.of(new Point(3, 4), new Point(6, 0), new Point(0, 0)), points);
    }


    // A lot of types

    public record Types(byte by, boolean bo, short sh, char ch, int in, long lo, float fl, double dl) {
    }

    @Test
    public void testTypes() {

        // Test wrappers Integer etc.

        var layout = MemoryLayout.structLayout(
                JAVA_BYTE.withName("by"),
                JAVA_BOOLEAN.withName("bo"),
                JAVA_SHORT.withName("sh"),
                JAVA_CHAR.withName("ch"),
                JAVA_INT_UNALIGNED.withName("in"),
                JAVA_LONG_UNALIGNED.withName("lo"),
                JAVA_FLOAT_UNALIGNED.withName("fl"),
                JAVA_DOUBLE_UNALIGNED.withName("dl")
        );

        try (var arena = Arena.ofConfined()) {
            var segment = arena.allocate(layout);

            layout.varHandle(PathElement.groupElement("by")).set(segment, (byte) 1);
            layout.varHandle(PathElement.groupElement("bo")).set(segment, true);
            layout.varHandle(PathElement.groupElement("sh")).set(segment, (short) 1);
            layout.varHandle(PathElement.groupElement("ch")).set(segment, 'a');
            layout.varHandle(PathElement.groupElement("in")).set(segment, 1);
            layout.varHandle(PathElement.groupElement("lo")).set(segment, 1L);
            layout.varHandle(PathElement.groupElement("fl")).set(segment, 1f);
            layout.varHandle(PathElement.groupElement("dl")).set(segment, 1d);

            var mapper = layout.recordMapper(Types.class);
            Types types = mapper.apply(segment);
            assertEquals(new Types(
                    (byte) 1,
                    true,
                    (short) 1,
                    'a',
                    1,
                    1L,
                    1.0f,
                    1.0d
            ), types);
        }
    }

    // Float80, From https://github.com/graalvm/sulong/blob/db830610d6ffbdab9678eef359a9f915e6ad2ee8/projects/com.oracle.truffle.llvm.types/src/com/oracle/truffle/llvm/types/floating/LLVM80BitFloat.java

    public record Float80(short exponent, long fraction){}

    @Test
    public void testFloat80() {

        short exponent = (short) 3;
        long fraction = 23423423L;

        var layout = MemoryLayout.structLayout(
                JAVA_SHORT.withName("exponent"),
                JAVA_LONG_UNALIGNED.withName("fraction")
        );

        try (var arena = Arena.ofConfined()) {
            var segment = arena.allocate(layout);

            layout.varHandle(PathElement.groupElement("exponent")).set(segment, exponent);
            layout.varHandle(PathElement.groupElement("fraction")).set(segment, fraction);

            var mapper = layout.recordMapper(Float80.class);
            Float80 float80 = mapper.apply(segment);
            assertEquals(new Float80(exponent, fraction), float80);
        }
    }

    @Test
    public void testToString() {
        var toString = POINT_LAYOUT.recordMapper(Point.class).toString();
        assertTrue(toString.contains("type=" + Point.class.getName()));
        assertTrue(toString.contains("layout=" + POINT_LAYOUT));
    }

    public <T> void test(MemorySegment segment,
                         Function<MemorySegment, T> mapper,
                         T expected) {

        T actual = mapper.apply(segment);
        assertEquals(expected, actual);
    }

}
