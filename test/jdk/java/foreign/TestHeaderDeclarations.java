/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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
 * @modules java.base/jdk.internal.foreign.memory
 * @run testng TestHeaderDeclarations
 */

import jdk.internal.foreign.memory.DescriptorParser;
import jdk.internal.foreign.memory.DescriptorParser.InvalidDescriptorException;

import org.testng.annotations.*;
import static org.testng.Assert.*;

public class TestHeaderDeclarations {

    @Test(dataProvider = "decls")
    public void testFuncNameWithNumbers(String prefix, String suffix) {
        parse(prefix, "a123=(i32)v", suffix);
    }

    @Test(dataProvider = "decls")
    public void testVarNameWithNumbers(String prefix, String suffix) {
        parse(prefix, "a123=i32", suffix);
    }

    @Test(dataProvider = "decls")
    public void testFuncNameWithSpaceBeforeEqual(String prefix, String suffix) {
        parse(prefix, "a123   =(i32)v", suffix);
    }

    @Test(dataProvider = "decls")
    public void testVarNameWithSpaceBeforeEqual(String prefix, String suffix) {
        parse(prefix, "a123   =i32", suffix);
    }

    @Test(dataProvider = "decls", expectedExceptions = InvalidDescriptorException.class)
    public void testFuncNoName(String prefix, String suffix) {
        parse(prefix, "=(i32)v", suffix);
    }

    @Test(dataProvider = "decls", expectedExceptions = InvalidDescriptorException.class)
    public void testVarNoName(String prefix, String suffix) {
        parse(prefix, "=i32", suffix);
    }

    @Test(dataProvider = "decls", expectedExceptions = InvalidDescriptorException.class)
    public void testFuncNameWithSpace(String prefix, String suffix) {
        parse(prefix, "a 123=(i32)v", suffix);
    }

    @Test(dataProvider = "decls", expectedExceptions = InvalidDescriptorException.class)
    public void testVarNameWithSpace(String prefix, String suffix) {
        parse(prefix, "a 123=i32", suffix);
    }

    @Test(dataProvider = "decls", expectedExceptions = InvalidDescriptorException.class)
    public void testFuncNameWithBadNumbers(String prefix, String suffix) {
        parse(prefix, "123a=(i32)v", suffix);
    }

    @Test(dataProvider = "decls", expectedExceptions = InvalidDescriptorException.class)
    public void testVarNameWithBadNumbers(String prefix, String suffix) {
        parse(prefix, "123a=i32", suffix);
    }

    @Test(dataProvider = "decls", expectedExceptions = InvalidDescriptorException.class)
    public void testFuncEqualNoName(String prefix, String suffix) {
        parse(prefix, "=(i32)v", suffix);
    }

    @Test(dataProvider = "decls", expectedExceptions = InvalidDescriptorException.class)
    public void testVarDeclEqualNoName(String prefix, String suffix) {
        parse(prefix, "=i32", suffix);
    }

    @Test(dataProvider = "decls", expectedExceptions = InvalidDescriptorException.class)
    public void testFuncDeclNoEqual(String prefix, String suffix) {
        parse(prefix, "name(i32)v", suffix);
    }

    @Test(dataProvider = "declsNoSuffix", expectedExceptions = InvalidDescriptorException.class)
    public void testVarDeclNoEqual(String prefix, String suffix) {
        parse(prefix, "namei32", suffix);
    }

    @Test(dataProvider = "declsNoSuffix", expectedExceptions = InvalidDescriptorException.class)
    public void testLoneIdent(String prefix, String suffix) {
        parse(prefix, "name", suffix);
    }

    @Test(dataProvider = "declsNoPrefix", expectedExceptions = InvalidDescriptorException.class)
    public void testLoneFunc(String prefix, String suffix) {
        parse(prefix, "(i32)v", suffix);
    }

    @Test(dataProvider = "declsNoSuffix", expectedExceptions = InvalidDescriptorException.class)
    public void testLoneLayout(String prefix, String suffix) {
        parse(prefix, "i32", suffix);
    }

    @Test(dataProvider = "decls", expectedExceptions = InvalidDescriptorException.class)
    public void testFuncDeclBadName(String prefix, String suffix) {
        parse(prefix, "n!me=(i32)v", suffix);
    }

    @Test(dataProvider = "decls", expectedExceptions = InvalidDescriptorException.class)
    public void testVarDeclBadName(String prefix, String suffix) {
        parse(prefix, "n!me=i32", suffix);
    }

    @Test(dataProvider = "decls", expectedExceptions = InvalidDescriptorException.class)
    public void testFuncDeclTooManyEquals(String prefix, String suffix) {
        parse(prefix, "name==(i32)v", suffix);
    }

    @Test(dataProvider = "decls", expectedExceptions = InvalidDescriptorException.class)
    public void testVarDeclTooManyEquals(String prefix, String suffix) {
        parse(prefix, "name==i32", suffix);
    }

    static void parse(String prefix, String declStr, String suffix) {
        DescriptorParser.parseHeaderDeclarations(prefix + declStr + suffix);
    }

    enum Decl {
        EMPTY(""),
        SPACE(" "),
        VAR_DECL("var=i32"),
        FUNC_DECL("func=(i32)v");

        String declStr;

        Decl(String declStr) {
            this.declStr = declStr;
        }

        String asPrefix() {
            switch (this) {
                case EMPTY: case SPACE:
                    return declStr;
                case VAR_DECL: case FUNC_DECL:
                    return declStr + " ";
                default:
                    throw new IllegalStateException("Cannot get here!");
            }
        }

        String asSuffix() {
            switch (this) {
                case EMPTY: case SPACE:
                    return declStr;
                case VAR_DECL: case FUNC_DECL:
                    return " " + declStr;
                default:
                    throw new IllegalStateException("Cannot get here!");
            }
        }
    }

    @DataProvider(name = "decls")
    public static Object[][] decls() {
        int numDecls = Decl.values().length;
        Object[][] decls = new Object[numDecls * numDecls][];
        for (Decl pre : Decl.values()) {
            for (Decl post : Decl.values()) {
                decls[pre.ordinal() * numDecls + post.ordinal()] = new Object[] { pre.asPrefix(), post.asSuffix() };
            }
        }
        return decls;
    }

    @DataProvider(name = "declsNoPrefix")
    public static Object[][] declsNoPrefix() {
        int numDecls = Decl.values().length;
        Object[][] decls = new Object[numDecls * 2][];
        for (Decl pre : new Decl[] { Decl.EMPTY, Decl.SPACE }) {
            for (Decl post : Decl.values()) {
                decls[pre.ordinal() * numDecls + post.ordinal()] = new Object[] { pre.asPrefix(), post.asSuffix() };
            }
        }
        return decls;
    }

    @DataProvider(name = "declsNoSuffix")
    public static Object[][] declsNoSuffix() {
        int numDecls = Decl.values().length;
        Object[][] decls = new Object[numDecls * 2][];
        for (Decl pre : Decl.values()) {
            for (Decl post : new Decl[] { Decl.EMPTY, Decl.SPACE }) {
                decls[pre.ordinal() * 2 + post.ordinal()] = new Object[] { pre.asPrefix(), post.asSuffix() };
            }
        }
        return decls;
    }
}
