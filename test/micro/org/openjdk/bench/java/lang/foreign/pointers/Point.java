package org.openjdk.bench.java.lang.foreign.pointers;

import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class Point extends Struct<Point> {

    Point(Pointer<Point> ptr) {
        super(ptr);
    }

    int x() {
        return ptr.segment.getAtIndex(NativeType.C_INT.layout(),  0);
    }
    int y() {
        return ptr.segment.getAtIndex(NativeType.C_INT.layout(),  4);
    }

    static Point wrap(MemorySegment segment) {
        return new Point(Pointer.wrap(TYPE, segment));
    }

    static final NativeType.OfStruct<Point> TYPE = new NativeType.OfStruct<Point>() {
        static final GroupLayout LAYOUT = MemoryLayout.structLayout(
                ValueLayout.JAVA_INT.withName("x"),
                ValueLayout.JAVA_INT.withName("y"));

        @Override
        public GroupLayout layout() {
            return LAYOUT;
        }

        @Override
        public Point make(Pointer<Point> pointer) {
            return new Point(pointer);
        }
    };
}
