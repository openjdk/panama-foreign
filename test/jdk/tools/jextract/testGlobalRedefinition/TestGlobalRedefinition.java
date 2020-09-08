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

import org.testng.annotations.Test;
import test.jextract.redef.*;

import java.lang.reflect.Method;

import static org.testng.Assert.assertEquals;
import static test.jextract.redef.redef_h.*;

/*
 * @test id=classes
 * @library ..
 * @modules jdk.incubator.jextract
 * @run driver JtregJextract -t test.jextract.redef -- redef.h
 * @run testng/othervm -Dforeign.restricted=permit TestGlobalRedefinition
 */
/*
 * @test id=sources
 * @library ..
 * @modules jdk.incubator.jextract
 * @run driver JtregJextractSources -t test.jextract.redef -- redef.h
 * @run testng/othervm -Dforeign.restricted=permit TestGlobalRedefinition
 */
public class TestGlobalRedefinition {
    @Test
    public void test() throws Throwable {
        Method mGet = redef_h.class.getMethod("x$get");
        C c1 = mGet.getAnnotatedReturnType().getAnnotation(C.class);
        assertEquals(c1.value(), "int");

        Method mSet = redef_h.class.getMethod("x$set", int.class);
        C c2 = mSet.getAnnotatedParameterTypes()[0].getAnnotation(C.class);
        assertEquals(c2.value(), "int");
    }
}
