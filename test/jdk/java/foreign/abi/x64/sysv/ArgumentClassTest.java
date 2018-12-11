/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
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
 * @modules java.base/jdk.internal.foreign.abi.x64.sysv
 */

import jdk.internal.foreign.abi.x64.sysv.ArgumentClass;

public class ArgumentClassTest {
    private final BitMatrix tested = new BitMatrix(ArgumentClass.values().length, ArgumentClass.values().length);

    private void runTests() {
        // Same classes -> trivial merge
        for (ArgumentClass c : ArgumentClass.values()) {
            maybeRunTestCase(c, c, c);
        }

        // V, NOCLASS -> V
        for (ArgumentClass c : ArgumentClass.values()) {
            maybeRunTestCase(c, c, ArgumentClass.NO_CLASS);
            maybeRunTestCase(c, ArgumentClass.NO_CLASS, c);
        }

        // V, MEMORY -> MEMORY
        for (ArgumentClass c : ArgumentClass.values()) {
            maybeRunTestCase(ArgumentClass.MEMORY, c, ArgumentClass.MEMORY);
            maybeRunTestCase(ArgumentClass.MEMORY, ArgumentClass.MEMORY, c);
        }

        // V, POINTER -> POINTER
        for (ArgumentClass c : ArgumentClass.values()) {
            maybeRunTestCase(ArgumentClass.POINTER, c, ArgumentClass.POINTER);
            maybeRunTestCase(ArgumentClass.POINTER, ArgumentClass.POINTER, c);
        }

        // V, INTEGER -> INTEGER
        for (ArgumentClass c : ArgumentClass.values()) {
            maybeRunTestCase(ArgumentClass.INTEGER, c, ArgumentClass.INTEGER);
            maybeRunTestCase(ArgumentClass.INTEGER, ArgumentClass.INTEGER, c);
        }

        // V, (X87 | X87UP | COMPLEX_X87) -> MEMORY
        for (ArgumentClass c : ArgumentClass.values()) {
            maybeRunTestCase(ArgumentClass.MEMORY, c, ArgumentClass.X87);
            maybeRunTestCase(ArgumentClass.MEMORY, ArgumentClass.X87, c);

            maybeRunTestCase(ArgumentClass.MEMORY, c, ArgumentClass.X87UP);
            maybeRunTestCase(ArgumentClass.MEMORY, ArgumentClass.X87UP, c);

            maybeRunTestCase(ArgumentClass.MEMORY, c, ArgumentClass.COMPLEX_X87);
            maybeRunTestCase(ArgumentClass.MEMORY, ArgumentClass.COMPLEX_X87, c);
        }

        // <else> SSE
        for (ArgumentClass c1 : ArgumentClass.values()) {
            for (ArgumentClass c2 : ArgumentClass.values()) {
                if (!tested.get(c1.ordinal(), c2.ordinal())) {
                    maybeRunTestCase(ArgumentClass.SSE, c1, c2);
                }
            }
        }
    }

    private void maybeRunTestCase(ArgumentClass expected, ArgumentClass c1, ArgumentClass c2) {
        if (tested.get(c1.ordinal(), c2.ordinal())) {
            return;
        }

        tested.set(c1.ordinal(), c2.ordinal());
        test(expected, c1, c2);
    }

    public void test(ArgumentClass expected, ArgumentClass c1, ArgumentClass c2) {
        System.err.println("test expect: " + expected + " c1: " + c1 + " c2: " + c2);
        assertEquals(expected, c1.merge(c2));
    }

    static void assertEquals(Object expected, Object actual) {
        if (expected != actual) {
            throw new RuntimeException("expected: " + expected + " does not match actual: " + actual);
        }
    }

    public static void main(String[] args) {
        ArgumentClassTest t = new ArgumentClassTest();

        t.runTests();
    }
}
