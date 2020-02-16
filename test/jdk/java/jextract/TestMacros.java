/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
 *
 */

/*
 * @test
 * @build JextractApiTestBase
 * @run testng TestMacros
 */

import java.nio.file.Path;
import java.nio.file.Paths;
import jdk.incubator.foreign.MemoryLayouts;
import jdk.incubator.jextract.Declaration;
import jdk.incubator.jextract.Type;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

public class TestMacros extends JextractApiTestBase {
    @Test
    public void testBadMacros() {
        // Somehow without this line, C_INT will be null after parse. Still looking for affirmative explanation.
        assertNotNull(MemoryLayouts.C_INT);
        // We need stdint.h for pointer macro, otherwise evaluation failed and Constant declaration is not created
        Path builtinInc = Paths.get(System.getProperty("java.home"), "conf", "jextract");
        Declaration.Scoped d = parse("badMacros.h", "-I", builtinInc.toString());
        assertNotNull(MemoryLayouts.C_INT);
        checkConstant(d, "INVALID_INT_CONSUMER",
            Type.pointer(Type.function(false, Type.void_(), Type.primitive(Type.Primitive.Kind.Int, MemoryLayouts.C_INT))),
            0L);
        Declaration.Scoped structFoo = checkStruct(d, "foo", "ptrFoo", "ptrBar");
        // Record type in macro definition are erased to void
        checkConstant(d, "NO_FOO", Type.pointer(Type.void_()), 0L);
    }
}
