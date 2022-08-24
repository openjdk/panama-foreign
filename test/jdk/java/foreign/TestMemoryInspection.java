/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
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
 * @run testng/othervm --enable-native-access=ALL-UNNAMED TestMemoryInspection
 */

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import jdk.internal.foreign.MemoryInspectionUtil;
import org.testng.annotations.*;

import static java.lang.foreign.ValueLayout.*;
import static java.util.stream.Collectors.joining;
import static org.testng.Assert.*;
import static jdk.internal.foreign.MemoryInspectionUtil.*;
import static java.util.Objects.requireNonNull;

@Test
public class TestMemoryInspection {

    private static final int HEX_SEGMENT_SIZE = 64 + 4;
    private static final String THE_QUICK = "The quick brown fox jumped over the lazy dog\nSecond line\t:here";

    private static final byte[] THE_QUICK_ARRAY = THE_QUICK.getBytes(StandardCharsets.UTF_8);
    private static final String EXPECTED_HEX = platformLineSeparated("""
            0000000000000000  54 68 65 20 71 75 69 63  6B 20 62 72 6F 77 6E 20  |The quick brown |
            0000000000000010  66 6F 78 20 6A 75 6D 70  65 64 20 6F 76 65 72 20  |fox jumped over |
            0000000000000020  74 68 65 20 6C 61 7A 79  20 64 6F 67 0A 53 65 63  |the lazy dog.Sec|
            0000000000000030  6F 6E 64 20 6C 69 6E 65  09 3A 68 65 72 65 00 00  |ond line.:here..|
            0000000000000040  00 00 00 00                                       |....|""");

    private static final String EXPECT_ADDRESS = "0x" + "00".repeat((int) ValueLayout.ADDRESS.byteSize());

    @Test
    public void testHexStream() {

        var actual = testWithFreshMemorySegment(HEX_SEGMENT_SIZE, segment -> {
            segment.setUtf8String(0, THE_QUICK);
            return hexDump(segment, MemoryInspection.Adapter.ofMemorySegment())
                    .collect(joining(System.lineSeparator()));
        });
        assertEquals(actual, EXPECTED_HEX);
    }

    @Test
    public void testHexStreamByteArray() {

        final byte[] array = new byte[HEX_SEGMENT_SIZE];
        System.arraycopy(THE_QUICK_ARRAY, 0, array, 0, THE_QUICK.length());
        var actual = hexDump(array, MemoryInspection.Adapter.ofByteArray())
                .collect(joining(System.lineSeparator()));

        assertEquals(actual, EXPECTED_HEX);
    }

    public void testHexStreamIntArray() {

        final byte[] byteArray = new byte[HEX_SEGMENT_SIZE];
        System.arraycopy(THE_QUICK_ARRAY, 0, byteArray, 0, THE_QUICK.length());
        final var intByteBuffer = ByteBuffer.wrap(byteArray).asIntBuffer();
        final int[] array = new int[HEX_SEGMENT_SIZE / Integer.BYTES];
        for (int i = 0; i < array.length; i++) {
            array[i] = intByteBuffer.get(i);
        }

        var actual = hexDump(array, MemoryInspection.Adapter.ofIntArray())
                .collect(joining(System.lineSeparator()));

        assertEquals(actual, EXPECTED_HEX);
    }

    @Test
    public void testHexStreamByteBuffer() {

        var array = new byte[HEX_SEGMENT_SIZE];
        System.arraycopy(THE_QUICK_ARRAY, 0, array, 0, THE_QUICK.length());
        var bb = ByteBuffer.wrap(array);
        var actual = hexDump(bb, MemoryInspection.Adapter.ofByteBuffer())
                .collect(joining(System.lineSeparator()));

        assertEquals(actual, EXPECTED_HEX);
    }


    @Test
    public void valueLayouts() {

        record TestInput(ValueLayout layout, String stringValue) {
        }

        List.of(
                new TestInput(ValueLayout.JAVA_BYTE, "0"),
                new TestInput(ValueLayout.JAVA_SHORT, "0"),
                new TestInput(ValueLayout.JAVA_INT, "0"),
                new TestInput(ValueLayout.JAVA_LONG, "0"),
                new TestInput(ValueLayout.JAVA_FLOAT, "0.0"),
                new TestInput(ValueLayout.JAVA_DOUBLE, "0.0"),
                new TestInput(ValueLayout.JAVA_CHAR, "" + (char) 0),
                new TestInput(JAVA_BOOLEAN, "false"),
                new TestInput(ValueLayout.ADDRESS, EXPECT_ADDRESS)
        ).forEach(ti -> {
            var expect = ti.layout() + "=" + ti.stringValue();
            var actual = testWithFreshMemorySegment(ti.layout().byteSize(), s -> MemoryInspectionUtil.toString(s, ti.layout(), MemoryInspection.ValueLayoutRenderer.standard()));
            assertEquals(actual, expect);
        });
    }

    @Test
    public void test256HexDump() {
        var expect = platformLineSeparated("""
                0000000000000000  00 01 02 03 04 05 06 07  08 09 0A 0B 0C 0D 0E 0F  |................|
                0000000000000010  10 11 12 13 14 15 16 17  18 19 1A 1B 1C 1D 1E 1F  |................|
                0000000000000020  20 21 22 23 24 25 26 27  28 29 2A 2B 2C 2D 2E 2F  | !"#$%&'()*+,-./|
                0000000000000030  30 31 32 33 34 35 36 37  38 39 3A 3B 3C 3D 3E 3F  |0123456789:;<=>?|
                0000000000000040  40 41 42 43 44 45 46 47  48 49 4A 4B 4C 4D 4E 4F  |@ABCDEFGHIJKLMNO|
                0000000000000050  50 51 52 53 54 55 56 57  58 59 5A 5B 5C 5D 5E 5F  |PQRSTUVWXYZ[\\]^_|
                0000000000000060  60 61 62 63 64 65 66 67  68 69 6A 6B 6C 6D 6E 6F  |`abcdefghijklmno|
                0000000000000070  70 71 72 73 74 75 76 77  78 79 7A 7B 7C 7D 7E 7F  |pqrstuvwxyz{|}~.|
                0000000000000080  80 81 82 83 84 85 86 87  88 89 8A 8B 8C 8D 8E 8F  |................|
                0000000000000090  90 91 92 93 94 95 96 97  98 99 9A 9B 9C 9D 9E 9F  |................|
                00000000000000A0  A0 A1 A2 A3 A4 A5 A6 A7  A8 A9 AA AB AC AD AE AF  |................|
                00000000000000B0  B0 B1 B2 B3 B4 B5 B6 B7  B8 B9 BA BB BC BD BE BF  |................|
                00000000000000C0  C0 C1 C2 C3 C4 C5 C6 C7  C8 C9 CA CB CC CD CE CF  |................|
                00000000000000D0  D0 D1 D2 D3 D4 D5 D6 D7  D8 D9 DA DB DC DD DE DF  |................|
                00000000000000E0  E0 E1 E2 E3 E4 E5 E6 E7  E8 E9 EA EB EC ED EE EF  |................|
                00000000000000F0  F0 F1 F2 F3 F4 F5 F6 F7  F8 F9 FA FB FC FD FE FF  |................|""");

        try (var session = MemorySession.openConfined()) {
            var segment = session.allocate(256);
            for (int i = 0; i < segment.byteSize(); i++) {
                segment.set(ValueLayout.JAVA_BYTE, i, (byte) i);
            }
            var actual = MemoryInspectionUtil.hexDump(segment, MemoryInspection.Adapter.ofMemorySegment())
                    .collect(joining(System.lineSeparator()));
            assertEquals(actual, expect);
        }
    }

    @Test
    public void test4kHexDump() {
        try (var session = MemorySession.openConfined()) {
            var segment = session.allocate(2048);
            for (int i = 0; i < segment.byteSize(); i++) {
                segment.set(ValueLayout.JAVA_BYTE, i, (byte) i);
            }
            MemoryInspectionUtil.hexDump(segment, MemoryInspection.Adapter.ofMemorySegment())
                    .forEach(l -> assertEquals(l.length(), "0000000000000000  00 01 02 03 04 05 06 07  08 09 0A 0B 0C 0D 0E 0F  |................|".length()));
        }
    }

    @Test
    public void point() {

        var expect = platformLineSeparated("""
                Point {
                    x=1,
                    y=2
                }""");

        var actual = testWithFreshMemorySegment(Integer.BYTES * 2, segment -> {
            final Point point = new Point(segment);
            point.x(1);
            point.y(2);
            return MemoryInspectionUtil.toString(segment, Point.LAYOUT, MemoryInspection.ValueLayoutRenderer.standard());
        });

        assertEquals(actual, expect);
    }


    @Test
    public void pointByteBuffer() {

        var expect = platformLineSeparated("""
                Point {
                    x=1,
                    y=2
                }""");

        var actual = testWithFreshMemorySegment(Integer.BYTES * 2, segment -> {
            final Point point = new Point(segment);
            point.x(1);
            point.y(2);

            ByteBuffer byteBuffer = segment.asByteBuffer();

            return MemoryInspection.toString(
                    byteBuffer,
                    MemoryInspection.Adapter.ofByteBuffer(),
                    Point.LAYOUT,
                    MemoryInspection.ValueLayoutRenderer.standard());
        });

        assertEquals(actual, expect);
    }

    @Test
    public void pointCustomRenderer() {

        var expect = platformLineSeparated("""
                Point {
                    x=0x0001,
                    y=0x0002
                }""");

        var actual = testWithFreshMemorySegment(Integer.BYTES * 2, segment -> {
            final Point point = new Point(segment);
            point.x(1);
            point.y(2);
            return MemoryInspectionUtil.toString(segment, Point.LAYOUT, new MemoryInspection.ValueLayoutRenderer() {
                @Override
                public String render(ValueLayout.OfInt layout, int value) {
                    return String.format("0x%04x", value);
                }
            });
        });

        assertEquals(actual, expect);
    }

    @Test
    public void standardCustomRenderer() {

        MemoryLayout layout = MemoryLayout.structLayout(
                // These are in bit alignment order (descending) for all platforms
                // in order for each element to be aligned to its type's bit alignment.
                Stream.of(
                                JAVA_LONG,
                                JAVA_DOUBLE,
                                ADDRESS,
                                JAVA_INT,
                                JAVA_FLOAT,
                                JAVA_SHORT,
                                JAVA_CHAR,
                                JAVA_BOOLEAN,
                                JAVA_BYTE
                        )
                        .map(vl -> vl.withName(vl.carrier().getSimpleName()))
                        .toArray(MemoryLayout[]::new)
        ).withName("struct");

        System.out.println("layout = " + layout);
        var expect = platformLineSeparated("""
                struct {
                    long=0,
                    double=0.0,
                    MemorySegment=$1,
                    int=0,
                    float=0.0,
                    short=0,
                    char=\u0000,
                    boolean=false,
                    byte=0
                }""").replace("$1", EXPECT_ADDRESS);


        var actual = testWithFreshMemorySegment(layout.byteSize(), segment ->
                MemoryInspectionUtil.toString(segment, layout, MemoryInspection.ValueLayoutRenderer.standard()));

        assertEquals(actual, expect);
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
                MemoryInspectionUtil.toString(segment, sequenceLayout, MemoryInspection.ValueLayoutRenderer.standard()));

        assertEquals(actual, expect);
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
                MemoryInspectionUtil.toString(segment, union, MemoryInspection.ValueLayoutRenderer.standard()));

        assertEquals(actual, expect);
    }

    static final class Point {

        static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
                ValueLayout.JAVA_INT.withName("x"),
                ValueLayout.JAVA_INT.withName("y")
        ).withName("Point");

        static final VarHandle xVH = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("x"));
        static final VarHandle yVH = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("y"));

        private final MemorySegment memorySegment;

        Point(MemorySegment memorySegment) {
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
