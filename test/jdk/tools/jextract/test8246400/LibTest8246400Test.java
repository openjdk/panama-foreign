/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.NativeScope;
import org.testng.annotations.Test;
import test.jextract.test8246400.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static test.jextract.test8246400.test8246400_h.*;
import static test.jextract.test8246400.RuntimeHelper.*;

/*
 * @test
 * @library ..
 * @modules jdk.incubator.jextract
 * @bug 8246400
 * @summary jextract should generate a utility to manage mutliple MemorySegments
 * @run driver JtregJextract -l Test8246400 -t test.jextract.test8246400 -- test8246400.h
 * @run testng/othervm -Dforeign.restricted=permit LibTest8246400Test
 */
public class LibTest8246400Test {
    @Test
    public void testSegmentRegister() {
        MemorySegment sum = null;
        MemoryAddress callback = null;
        try (var scope = NativeScope.unboundedScope()) {
            var v1 = Vector.allocate(scope);
            Vector.x$set(v1, 1.0);
            Vector.y$set(v1, 0.0);

            var v2 = Vector.allocate(scope);
            Vector.x$set(v2, 0.0);
            Vector.y$set(v2, 1.0);

            sum = add(v1.segment(), v2.segment());
            sum = scope.register(sum);

            assertEquals(Vector.x$get(sum.address()), 1.0, 0.1);
            assertEquals(Vector.y$get(sum.address()), 1.0, 0.1);

            callback = cosine_similarity$dot.allocate((a, b) -> {
                return (Vector.x$get(a.address()) * Vector.x$get(b.address())) +
                    (Vector.y$get(a.address()) * Vector.y$get(b.address()));
            }, scope);

            var value = cosine_similarity(v1.segment(), v2.segment(), callback);
            assertEquals(value, 0.0, 0.1);

            value = cosine_similarity(v1.segment(), v1.segment(), callback);
            assertEquals(value, 1.0, 0.1);
        }
        assertTrue(!sum.isAlive());
        assertTrue(!callback.segment().isAlive());
    }
}
