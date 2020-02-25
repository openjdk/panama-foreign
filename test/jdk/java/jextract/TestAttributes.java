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
 * @bug 8239808
 * @build JextractApiTestBase
 * @run testng TestAttributes
 */

import java.lang.constant.ConstantDesc;
import java.util.List;
import java.util.stream.Collectors;
import jdk.incubator.foreign.MemoryLayouts;
import jdk.incubator.jextract.Declaration;
import jdk.incubator.jextract.Type;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class TestAttributes extends JextractApiTestBase {
    private final static Type C_INT = Type.primitive(Type.Primitive.Kind.Int, MemoryLayouts.C_INT);
    private final static String ASMLABEL = "AsmLabelAttr";

    private void validateAsmLabel(Declaration d, boolean isAdd) {
        List<ConstantDesc> attrs = d.getAttribute(ASMLABEL).get();
        String value = isMacOSX ? "_" : "";
        value += d.name();
        value += isAdd ? "A" : "B";
        assertEquals(attrs.get(0), value);
    }

    private void validateHeader(Declaration.Scoped top, boolean isAdd) {
        if (isWindows) {
            // TODO: add Windows validation
            // Simply dump declaration for now
            System.out.println(top);
            return;
        }
        var list = top.members().stream()
                .filter(byNameAndType("foo", Declaration.Variable.class))
                .map(Declaration.Variable.class::cast)
                .collect(Collectors.toList());
        assertEquals(list.size(), 3);
        int hasAttrs = 0;
        for (Declaration.Variable foo: list) {
            assertEquals(Declaration.Variable.Kind.GLOBAL, foo.kind());
            assertTypeEquals(C_INT, foo.type());
            if (foo.getAttribute(ASMLABEL).isPresent()) {
                hasAttrs++;
                validateAsmLabel(foo, isAdd);
            }
        }
        assertEquals(hasAttrs, 2);
        var listFunc = top.members().stream()
                .filter(byNameAndType("func", Declaration.Function.class))
                .map(Declaration.Function.class::cast)
                .collect(Collectors.toList());
        assertEquals(listFunc.size(), 3);
        hasAttrs = 0;
        for (Declaration.Function func: listFunc) {
            checkFunction(func, C_INT, C_INT, C_INT);
            if (func.getAttribute(ASMLABEL).isPresent()) {
                hasAttrs++;
                validateAsmLabel(func, isAdd);
            }
        }
        assertEquals(hasAttrs, 2);
    }

    @Test
    public void testA() {
        Declaration.Scoped d = parse("libAsmSymbol.h", "-DADD");
        validateHeader(d, true);
    }

    @Test
    public void testB() {
        Declaration.Scoped d = parse("libAsmSymbol.h");
        validateHeader(d, false);
    }
}
