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
 * @run testng TestLayouts
 */

import jdk.incubator.foreign.MemoryLayouts;
import jdk.incubator.foreign.MemoryLayout;

import java.nio.ByteOrder;
import java.util.function.LongFunction;

import org.testng.annotations.*;
import static org.testng.Assert.*;

public class TestLayouts {

    @Test(dataProvider = "badLayoutSizes", expectedExceptions = IllegalArgumentException.class)
    public void testBadLayoutSize(SizedLayoutFactory factory, long size) {
        factory.make(size);
    }

    @Test(dataProvider = "badAlignments", expectedExceptions = IllegalArgumentException.class)
    public void testBadLayoutAlignment(MemoryLayout layout, long alignment) {
        layout.withBitAlignment(alignment);
    }

    @DataProvider(name = "badLayoutSizes")
    public Object[][] factoriesAndSizes() {
        return new Object[][] {
                { SizedLayoutFactory.VALUE_BE, 0 },
                { SizedLayoutFactory.VALUE_BE, -1 },
                { SizedLayoutFactory.VALUE_LE, 0 },
                { SizedLayoutFactory.VALUE_LE, -1 },
                { SizedLayoutFactory.PADDING, 0 },
                { SizedLayoutFactory.PADDING, -1 },
                { SizedLayoutFactory.SEQUENCE, -1 }
        };
    }

    @DataProvider(name = "badAlignments")
    public Object[][] layoutsAndAlignments() {
        LayoutKind[] layoutKinds = LayoutKind.values();
        Object[][] values = new Object[layoutKinds.length * 2][2];
        for (int i = 0; i < layoutKinds.length ; i++) {
            values[i * 2] = new Object[] { layoutKinds[i].layout, 3 }; // smaller than 8
            values[(i * 2) + 1] = new Object[] { layoutKinds[i].layout, 18 }; // not a power of 2
        }
        return values;
    }

    enum SizedLayoutFactory {
        VALUE_LE(size -> MemoryLayout.ofValueBits(size, ByteOrder.LITTLE_ENDIAN)),
        VALUE_BE(size -> MemoryLayout.ofValueBits(size, ByteOrder.BIG_ENDIAN)),
        PADDING(MemoryLayout::ofPaddingBits),
        SEQUENCE(size -> MemoryLayout.ofSequence(size, MemoryLayouts.PAD_8));

        private final LongFunction<MemoryLayout> factory;

        SizedLayoutFactory(LongFunction<MemoryLayout> factory) {
            this.factory = factory;
        }

        MemoryLayout make(long size) {
            return factory.apply(size);
        }
    }

    enum LayoutKind {
        VALUE_LE(MemoryLayouts.BITS_8_LE),
        VALUE_BE(MemoryLayouts.BITS_8_BE),
        PADDING(MemoryLayouts.PAD_8),
        SEQUENCE(MemoryLayout.ofSequence(1, MemoryLayouts.PAD_8)),
        STRUCT(MemoryLayout.ofStruct(MemoryLayouts.PAD_8, MemoryLayouts.PAD_8)),
        UNION(MemoryLayout.ofUnion(MemoryLayouts.PAD_8, MemoryLayouts.PAD_8));

        final MemoryLayout layout;

        LayoutKind(MemoryLayout layout) {
            this.layout = layout;
        }
    }
}
