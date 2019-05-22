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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.LongFunction;

import org.testng.annotations.*;

import static org.testng.Assert.*;

public class TestScopes {

    static MemoryScope GL_SCOPE = MemoryScope.globalScope();
    static MemoryScope F_SCOPE = GL_SCOPE.fork();

    @Test(dataProvider = "badScopeSizeAndAlignments", expectedExceptions = IllegalArgumentException.class)
    public void testBadAllocateAlign(MemoryScope scope, long size, long align) {
        scope.allocate(size, align);
    }

    @Test(dataProvider = "badScopeLayouts", expectedExceptions = IllegalArgumentException.class)
    public void testBadAllocateLayout(MemoryScope scope, Layout layout) {
        scope.allocate(layout);
    }

    @Test(dataProvider = "scopes", expectedExceptions = OutOfMemoryError.class)
    public void testAllocateTooBig(MemoryScope scope) {
        scope.allocate(Long.MAX_VALUE);
    }

    @Test(dataProvider = "charateristics", expectedExceptions = IllegalArgumentException.class)
    public void testBadCharateristics(MemoryScope scope, long charateristics) {
        scope.fork(charateristics);
    }

    @Test(dataProvider = "scopeOperations")
    public void testOpOutsideConfinement(ScopeOperation scopeOp) throws Throwable {
        AtomicBoolean failed = new AtomicBoolean(false);
        Thread t = new Thread(() -> scopeOp.run(F_SCOPE));
        t.setUncaughtExceptionHandler((thread, ex) -> failed.set(true));
        t.start();
        t.join();
        assertTrue(failed.get());
    }

    @DataProvider(name = "badScopeSizeAndAlignments")
    public Object[][] sizesAndAlignments() {
        return new Object[][] {
                { GL_SCOPE, -1, 8 },
                { GL_SCOPE, 1, 15 },
                { GL_SCOPE, 1, -15 },
                { F_SCOPE, -1, 8 },
                { F_SCOPE, 1, 15 },
                { F_SCOPE, 1, -15 }
        };
    }

    @DataProvider(name = "badScopeLayouts")
    public Object[][] layouts() {
        SizedLayoutFactory[] layoutFactories = SizedLayoutFactory.values();
        Object[][] values = new Object[layoutFactories.length * 2 * 2][2];
        for (int i = 0; i < layoutFactories.length ; i++) {
            values[i * 4] = new Object[] { GL_SCOPE, GroupLayout.struct(layoutFactories[i].make(7), PaddingLayout.of(9)) }; // good size, bad align
            values[(i * 4) + 1] = new Object[] { GL_SCOPE, layoutFactories[i].make(15).alignTo(16) }; // bad size, good align
            values[(i * 4) + 2] = new Object[] { F_SCOPE, GroupLayout.struct(layoutFactories[i].make(7), PaddingLayout.of(9)) }; // good size, bad align
            values[(i * 4) + 3] = new Object[] { F_SCOPE, layoutFactories[i].make(15).alignTo(16) }; // bad size, good align
        }
        return values;
    }

    @DataProvider(name = "scopes")
    public Object[][] scopes() {
        return new Object[][] {
            { GL_SCOPE },
            { F_SCOPE }
        };
    }

    @DataProvider(name = "charateristics")
    public Object[][] charateristics() {
        return new Object[][] {
            { GL_SCOPE, 42 },
            { GL_SCOPE, -42 },
            { GL_SCOPE, MemoryScope.PINNED },
            { F_SCOPE, 42 },
            { F_SCOPE, -42 },
            { F_SCOPE, MemoryScope.PINNED },
        };
    }

    @DataProvider(name = "scopeOperations")
    public Object[][] scopeOperations() {
        return new Object[][] {
                { ScopeOperation.FORK },
                { ScopeOperation.ALLOCATE },
                { ScopeOperation.CLOSE },
                { ScopeOperation.MERGE }
        };
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

    enum ScopeOperation {
        FORK(MemoryScope::fork),
        ALLOCATE(s -> s.allocate(8, 8)),
        CLOSE(MemoryScope::close),
        MERGE(MemoryScope::merge);

        private final Consumer<MemoryScope> scopeOp;

        ScopeOperation(Consumer<MemoryScope> op) {
            this.scopeOp = op;
        }

        void run(MemoryScope s) {
            scopeOp.accept(s);
        }
    }
}
