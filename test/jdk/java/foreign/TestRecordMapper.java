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
 * @run junit/othervm --enable-native-access=ALL-UNNAMED TestRecordMapper
 */

import org.junit.jupiter.api.*;

import java.lang.foreign.Arena;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static java.lang.foreign.ValueLayout.*;
import static org.junit.jupiter.api.Assertions.*;

public class TestRecordMapper {

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

    public record BytePoint(byte x, byte y) {}
    @Test
    public void testByte() {
        testPointType(new BytePoint((byte)3, (byte)4), new byte[]{3, 4}, JAVA_BYTE);
    }

    public record BooleanPoint(boolean x, boolean y) {}
    @Test
    public void testBoolean() {
        testPointType(new BooleanPoint(false, true), new byte[]{0, 1}, JAVA_BOOLEAN);
    }

    public record ShortPoint(short x, short y) {}
    @Test
    public void testShort() {
        testPointType(new ShortPoint((short)3, (short)4), new short[]{3, 4}, JAVA_SHORT);
    }

    public record CharPoint(char x, char y) {}
    @Test
    public void testChar() {
        testPointType(new CharPoint('d', 'e'), new char[]{'d', 'e'}, JAVA_CHAR);
    }

    public record IntPoint(int x, int y) {}
    @Test
    public void testInt() {
        testPointType(new IntPoint(3, 4), new int[]{3, 4}, JAVA_INT);
    }

    @Test
    public void testLong() {
        testPointType(new LongPoint(3L, 4L), new long[]{3L, 4L}, JAVA_LONG);
    }

    public record FloatPoint(float x, float y) {}
    @Test
    public void testFloat() {
        testPointType(new FloatPoint(3.0f, 4.0f), new float[]{3.0f, 4.0f}, JAVA_FLOAT);
    }

    public record DoublePoint(double x, double y){}
    @Test
    public void testDouble() {
        testPointType(new DoublePoint(3.0d, 4.0d), new double[]{3.0d, 4.0d}, JAVA_DOUBLE);
    }


    public record SequenceBox(int before, int[] ints, int after) {

        @Override
        public boolean equals(Object obj) {
            return obj instanceof SequenceBox other &&
                    before == other.before &&
                    Arrays.equals(ints, other.ints) &&
                    after == other.after;
        }

        @Override
        public String toString() {
            return "SequenceBox{before=" + before + ", ints=" + Arrays.toString(ints) + ", after=" + after;
        }
    }

    @Test
    public void testSequenceBox() {

        var segment = MemorySegment.ofArray(new int[]{0, 2, 3, 4});

        var layout = MemoryLayout.structLayout(
                JAVA_INT.withName("before"),
                MemoryLayout.sequenceLayout(2, JAVA_INT).withName("ints"),
                JAVA_INT.withName("after")
        );

        var mapper = layout.recordMapper(SequenceBox.class);

        SequenceBox sequenceBox = mapper.apply(segment);

        assertEquals(new SequenceBox(0, new int[]{2, 3}, 4), sequenceBox);
    }

    public record SequenceOfPoints(int before, Point[] points, int after) {

        @Override
        public boolean equals(Object obj) {
            return obj instanceof SequenceOfPoints other &&
                    before == other.before &&
                    Arrays.equals(points, other.points) &&
                    after == other.after;
        }

        @Override
        public String toString() {
            return "SequenceOfPoints{before=" + before + ", ints=" + Arrays.toString(points) + ", after=" + after;
        }

    }

    @Test
    public void reproduce() {
        System.out.println("### Reproduce");
        var segment = MemorySegment.ofArray(new int[]{-1, 2, 3, 4, 5, -2});
        var s2 = segment.asSlice(4, 16);
        var mapper = POINT_LAYOUT.recordMapper(Point.class);
        System.out.println("mapper = " + mapper);
        s2.elements(POINT_LAYOUT)
                .forEach(System.out::println);

        var list = s2.elements(POINT_LAYOUT)
                .map(mapper)
                .toList();

        System.out.println("list = " + list);
    }

    @Test
    public void testPointSequence() {

        var segment = MemorySegment.ofArray(new int[]{-1, 2, 3, 4, 5, -2});

        var layout = MemoryLayout.structLayout(
                JAVA_INT.withName("before"),
                MemoryLayout.sequenceLayout(2, POINT_LAYOUT).withName("points"),
                JAVA_INT.withName("after")
        );

        var mapper = layout.recordMapper(SequenceOfPoints.class);

        SequenceOfPoints sequenceOfPoints = mapper.apply(segment);

        System.out.println("pointSequence = " + sequenceOfPoints);
        assertEquals(new SequenceOfPoints(-1, new Point[]{new Point(2, 3), new Point(4, 5)}, -2), sequenceOfPoints);
    }

    static public <R extends Record> void testPointType(R expected,
                                                 Object array,
                                                 ValueLayout valueLayout) {
        testType(expected, array, valueLayout, "x", "y");
    }

        @SuppressWarnings("unchecked")
    static public <R extends Record> void testType(R expected,
                                            Object array,
                                            ValueLayout valueLayout,
                                            String... names) {

        MemorySegment segment = switch (array) {
            case byte[] a -> MemorySegment.ofArray(a);
            case short[] a -> MemorySegment.ofArray(a);
            case char[] a -> MemorySegment.ofArray(a);
            case int[] a -> MemorySegment.ofArray(a);
            case long[] a -> MemorySegment.ofArray(a);
            case float[] a -> MemorySegment.ofArray(a);
            case double[] a -> MemorySegment.ofArray(a);
            default -> throw new IllegalArgumentException("Unknown array type: " + array);
        };

        StructLayout layout = MemoryLayout.structLayout(Arrays.stream(names)
                .map(valueLayout::withName)
                .toArray(MemoryLayout[]::new));

        Class<R> type = (Class<R>) expected.getClass();
        Function<MemorySegment, R> mapper = layout.recordMapper(type);
        R actual = mapper.apply(segment);
        assertEquals(expected, actual);
    }


    public <T> void test(MemorySegment segment,
                         Function<MemorySegment, T> mapper,
                         T expected) {

        T actual = mapper.apply(segment);
        assertEquals(expected, actual);
    }

}
