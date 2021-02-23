/*
 * Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
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

import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.NativeScope;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.*;
import static test.jextract.funcpointers.func_h.*;

/*
 * @test id=classes
 * @library ..
 * @modules jdk.incubator.jextract
 * @run driver JtregJextract -l Func -t test.jextract.funcpointers -- func.h
 * @run testng/othervm -Dforeign.restricted=permit TestFuncPointerInvokers
 */
/*
 * @test id=sources
 * @library ..
 * @modules jdk.incubator.jextract
 * @run driver JtregJextractSources -l Func -t test.jextract.funcpointers -- func.h
 * @run testng/othervm -Dforeign.restricted=permit TestFuncPointerInvokers
 */
public class TestFuncPointerInvokers {
    @Test
    public void testStructField() {
        try (NativeScope scope = NativeScope.unboundedScope()) {
            AtomicInteger val = new AtomicInteger(-1);
            MemorySegment bar = Bar.allocate(scope);
            Bar.foo$set(bar, Foo.allocate((i) -> val.set(i), scope).address());
            Bar.foo(bar, 42);
            assertEquals(val.get(), 42);
        }
    }

    @Test
    public void testGlobal() {
        try (NativeScope scope = NativeScope.unboundedScope()) {
            AtomicInteger val = new AtomicInteger(-1);
            f$set(Foo.allocate((i) -> val.set(i), scope).address());
            f(42);
            assertEquals(val.get(), 42);
        }
    }
}
