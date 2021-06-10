/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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
 * @run testng TestCopyFrom
 */

import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemoryLayouts;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;
import jdk.incubator.foreign.ValueLayout;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

import static org.testng.Assert.*;

public class TestCopyFrom {

    @Test(dataProvider = "slices")
    public void testByteCopy(SegmentSlice s1, SegmentSlice s2) {
        int size = Math.min(s1.byteSize(), s2.byteSize());
        //prepare source and target segments
        for (int i = 0 ; i < size ; i++) {
            Type.BYTE.set(s2, i, 0);
        }
        for (int i = 0 ; i < size ; i++) {
            Type.BYTE.set(s1, i, i);
        }
        //perform copy
        s2.segment.copyFrom(s1.segment.asSlice(0, size));
        //check that copy actually worked
        for (int i = 0 ; i < size ; i++) {
            Type.BYTE.check(s2, i, i);
        }
    }

    @Test(dataProvider = "slices")
    public void testElementCopy(SegmentSlice s1, SegmentSlice s2) {
        if (s1.type.carrier != s2.type.carrier) return;
        int size = Math.min(s1.elementSize(), s2.elementSize());
        //prepare source and target segments
        for (int i = 0 ; i < size ; i++) {
            s2.set(i, 0);
        }
        for (int i = 0 ; i < size ; i++) {
            s1.set(i, i);
        }
        //perform copy
        s2.segment.copyFrom(s1.segment.asSlice(0, size * s1.type.size()), s1.type.layout, s2.type.layout);
        //check that copy actually worked
        for (int i = 0; i < size; i++) {
            s2.check(i, i);
        }
    }

    interface Getter<X> {
        X get(MemorySegment segment, long index, ByteOrder order);
    }

    interface Setter<X> {
        void set(MemorySegment segment, long index, ByteOrder order, X val);
    }

    enum Type {
        // Byte
        BYTE(byte.class, MemoryLayouts.JAVA_BYTE, (s, i, o) -> MemoryAccess.getByteAtOffset(s, i), (s, i, o, v) -> MemoryAccess.setByteAtOffset(s, i, v), i -> (byte)i),
        //LE
        SHORT_LE(short.class, MemoryLayouts.BITS_16_LE, MemoryAccess::getShortAtOffset, MemoryAccess::setShortAtOffset, i -> (short)i),
        CHAR_LE(char.class, MemoryLayouts.BITS_16_LE, MemoryAccess::getCharAtOffset, MemoryAccess::setCharAtOffset, i -> (char)i),
        INT_LE(int.class, MemoryLayouts.BITS_32_LE, MemoryAccess::getIntAtOffset, MemoryAccess::setIntAtOffset, i -> i),
        FLOAT_LE(float.class, MemoryLayouts.BITS_32_LE, MemoryAccess::getFloatAtOffset, MemoryAccess::setFloatAtOffset, i -> (float)i),
        LONG_LE(long.class, MemoryLayouts.BITS_64_LE, MemoryAccess::getLongAtOffset, MemoryAccess::setLongAtOffset, i -> (long)i),
        DOUBLE_LE(double.class, MemoryLayouts.BITS_64_LE, MemoryAccess::getDoubleAtOffset, MemoryAccess::setDoubleAtOffset, i -> (double)i),
        //BE
        SHORT_BE(short.class, MemoryLayouts.BITS_16_BE, MemoryAccess::getShortAtOffset, MemoryAccess::setShortAtOffset, i -> (short)i),
        CHAR_BE(char.class, MemoryLayouts.BITS_16_BE, MemoryAccess::getCharAtOffset, MemoryAccess::setCharAtOffset, i -> (char)i),
        INT_BE(int.class, MemoryLayouts.BITS_32_BE, MemoryAccess::getIntAtOffset, MemoryAccess::setIntAtOffset, i -> i),
        FLOAT_BE(float.class, MemoryLayouts.BITS_32_BE, MemoryAccess::getFloatAtOffset, MemoryAccess::setFloatAtOffset, i -> (float)i),
        LONG_BE(long.class, MemoryLayouts.BITS_64_BE, MemoryAccess::getLongAtOffset, MemoryAccess::setLongAtOffset, i -> (long)i),
        DOUBLE_BE(double.class, MemoryLayouts.BITS_64_BE, MemoryAccess::getDoubleAtOffset, MemoryAccess::setDoubleAtOffset, i -> (double)i);

        final ValueLayout layout;
        final Getter<Object> getter;
        final Setter<Object> setter;
        final IntFunction<Object> valueConverter;
        final Class<?> carrier;

        @SuppressWarnings("unchecked")
        <Z> Type(Class<Z> carrier, ValueLayout layout, Getter<Z> getter, Setter<Z> setter, IntFunction<Z> valueConverter) {
            this.carrier = carrier;
            this.layout = layout;
            this.getter = (Getter<Object>)getter;
            this.setter = (Setter<Object>)setter;
            this.valueConverter = (IntFunction<Object>)valueConverter;
        }

        int size() {
            return (int)layout.byteSize();
        }

        void set(SegmentSlice slice, int index, int val) {
            setter.set(slice.segment, index * size(), layout.order(), valueConverter.apply(val));
        }

        void check(SegmentSlice slice, int index, int val) {
            assertEquals(getter.get(slice.segment, index * size(), layout.order()), valueConverter.apply(val));
        }
    }

    static class SegmentSlice {

        enum Kind {
            NATIVE(i -> MemorySegment.allocateNative(i, ResourceScope.newImplicitScope())),
            ARRAY(i -> MemorySegment.ofArray(new byte[i]));

            final IntFunction<MemorySegment> segmentFactory;

            Kind(IntFunction<MemorySegment> segmentFactory) {
                this.segmentFactory = segmentFactory;
            }

            MemorySegment makeSegment(int elems) {
                return segmentFactory.apply(elems);
            }
        }

        final Kind kind;
        final Type type;
        final int first;
        final int last;
        final MemorySegment segment;

        public SegmentSlice(Kind kind, Type type, int first, int last, MemorySegment segment) {
            this.kind = kind;
            this.type = type;
            this.first = first;
            this.last = last;
            this.segment = segment;
        }

        void set(int index, int val) {
            type.set(this, index, val);
        }

        void check(int index, int val) {
            type.check(this, index, val);
        }

        int byteSize() {
            return last - first + 1;
        }

        int elementSize() {
            return byteSize() / type.size();
        }

        @Override
        public String toString() {
            return String.format("SegmentSlice{%s, %d, %d}", type, first, last);
        }
    }

    @DataProvider(name = "slices")
    static Object[][] elementSlices() {
        List<SegmentSlice> slices = new ArrayList<>();
        for (SegmentSlice.Kind kind : SegmentSlice.Kind.values()) {
            MemorySegment segment = kind.makeSegment(16);
            //compute all slices
            for (Type type : Type.values()) {
                for (int index = 0; index < 16; index += type.size()) {
                    MemorySegment first = segment.asSlice(0, index);
                    slices.add(new SegmentSlice(kind, type, 0, index - 1, first));
                    MemorySegment second = segment.asSlice(index);
                    slices.add(new SegmentSlice(kind, type, index, 15, second));
                }
            }
        }
        Object[][] sliceArray = new Object[slices.size() * slices.size()][];
        for (int i = 0 ; i < slices.size() ; i++) {
            for (int j = 0 ; j < slices.size() ; j++) {
                sliceArray[i * slices.size() + j] = new Object[] { slices.get(i), slices.get(j) };
            }
        }
        return sliceArray;
    }
}
