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
 * @run testng TestAnnotations
 */

import jdk.internal.foreign.memory.DescriptorParser;
import jdk.internal.foreign.memory.DescriptorParser.InvalidDescriptorException;

import org.testng.annotations.*;
import static org.testng.Assert.*;

public class TestAnnotations {

    @Test(dataProvider = "annos")
    public void testAnnoNameWithNumbers(String annoPrefix, String annoSuffix, String valueSuffix) {
        parse(annoPrefix, "a123", valueSuffix, annoSuffix);
    }

    @Test(dataProvider = "annos")
    public void testAnnoNameWithTrailingSpace(String annoPrefix, String annoSuffix, String valueSuffix) {
        parse(annoPrefix, "a123 ", valueSuffix, annoSuffix);
    }

    @Test(dataProvider = "annos", expectedExceptions = InvalidDescriptorException.class)
    public void testAnnoNoName(String annoPrefix, String annoSuffix, String valueSuffix) {
        parse(annoPrefix, "", valueSuffix, annoSuffix);
    }

    @Test(dataProvider = "annos", expectedExceptions = InvalidDescriptorException.class)
    public void testAnnoNameWithMiddleSpace(String annoPrefix, String annoSuffix, String valueSuffix) {
        parse(annoPrefix, "a 123", valueSuffix, annoSuffix);
    }

    @Test(dataProvider = "annos", expectedExceptions = InvalidDescriptorException.class)
    public void testAnnoNameWithBadNumbers(String annoPrefix, String annoSuffix, String valueSuffix) {
        parse(annoPrefix, "123a", valueSuffix, annoSuffix);
    }

    @Test(dataProvider = "annos", expectedExceptions = InvalidDescriptorException.class)
    public void testAnnoWithBadName(String annoPrefix, String annoSuffix, String valueSuffix) {
        parse(annoPrefix, "na!me", valueSuffix, annoSuffix);
    }

    static void parse(String annoPrefix, String name, String valueSuffix, String annoSuffix) {
        DescriptorParser.parseLayout("i32" + annoPrefix + "(" + name + valueSuffix + ")" + annoSuffix);
    }

    enum Anno {
        NONE(""),
        NAME("(foo)"),
        NAME_VALUE("(foo=bar)");

        String annoStr;

        Anno(String annoStr) {
            this.annoStr = annoStr;
        }
    }

    enum ValueSuffix {
        NONE(""),
        VALUE("=bar");

        String suffixStr;

        ValueSuffix(String suffixStr) {
            this.suffixStr = suffixStr;
        }
    }

    @DataProvider(name = "annos")
    public static Object[][] annos() {
        int numDecls = Anno.values().length;
        int numValueSuffixes = ValueSuffix.values().length;
        Object[][] decls = new Object[numDecls * numDecls * numValueSuffixes][];
        for (Anno pre : Anno.values()) {
            for (Anno post : Anno.values()) {
                for (ValueSuffix suffix : ValueSuffix.values()) {
                    decls[(pre.ordinal() * numDecls + post.ordinal()) * numValueSuffixes + suffix.ordinal()] =
                            new Object[] { pre.annoStr, post.annoStr, suffix.suffixStr };
                }
            }
        }
        return decls;
    }
}
