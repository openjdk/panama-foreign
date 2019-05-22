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
 * @run testng TestScopes
 */

import java.foreign.GroupLayout;
import java.foreign.Layout;
import java.foreign.MemoryScope;
import java.foreign.PaddingLayout;
import java.foreign.ValueLayout;
import java.util.function.LongFunction;

import org.testng.annotations.*;
import static org.testng.Assert.*;

public class TestScopes {
    @Test(dataProvider = "badScopeSizeAndAlignments", expectedExceptions = IllegalArgumentException.class)
    public void testBadAllocateAlign(long size, long align) {
        MemoryScope.globalScope().allocate(size, align);
    }

    @Test(dataProvider = "badScopeLayouts", expectedExceptions = IllegalArgumentException.class)
    public void testBadAllocateLayout(Layout layout) {
        MemoryScope.globalScope().allocate(layout);
    }

    @Test(expectedExceptions = OutOfMemoryError.class)
    public void testAllocateTooBig() {
        MemoryScope.globalScope().allocate(Long.MAX_VALUE);
    }

    @DataProvider(name = "badScopeSizeAndAlignments")
    public Object[][] sizesAndAlignments() {
        return new Object[][] {
                { -1, 8 },
                { 1, 15 },
                { 1, -15 }
        };
    }

    @DataProvider(name = "badScopeLayouts")
    public Object[][] layouts() {
        SizedLayoutFactory[] layoutFactories = SizedLayoutFactory.values();
        Object[][] values = new Object[layoutFactories.length * 2][2];
        for (int i = 0; i < layoutFactories.length ; i++) {
            values[i * 2] = new Object[] {GroupLayout.struct(layoutFactories[i].make(7), PaddingLayout.of(9)) }; // good size, bad align
            values[(i * 2) + 1] = new Object[] { layoutFactories[i].make(15).alignTo(16) }; // bad size, good align
        }
        return values;
    }

    enum SizedLayoutFactory {
        U_VALUE(ValueLayout::ofUnsignedInt),
        S_VALUE(ValueLayout::ofSignedInt),
        FP_VALUE(ValueLayout::ofFloatingPoint),
        PADDING(PaddingLayout::of);

        private final LongFunction<Layout> factory;

        SizedLayoutFactory(LongFunction<Layout> factory) {
            this.factory = factory;
        }

        Layout make(long size) {
            return factory.apply(size);
        }
    }
}
