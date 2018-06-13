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

import com.sun.tools.jextract.JType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.nicl.metadata.NativeLocation;
import org.testng.annotations.Test;

import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/*
 * @test
 * @summary Unit test for JType
 * @modules jdk.jextract/com.sun.tools.jextract
 * @run testng JTypeTest
 */
public class JTypeTest {

    static class Inner {};
    static interface InnerIf {};

    @Target(TYPE_USE)
    @Retention(RUNTIME)
    @NativeLocation(file="", line=1, column=1, USR="")
    static @interface Alias {};

    public JTypeTest() {
    }

    @Test
    /**
     * Primitive types defined in JVMS 2.2
     */
    public void testOfPrimitives() {
        assertEquals(JType.of(byte.class), JType.Byte);
        assertEquals(JType.of(short.class), JType.Short);
        assertEquals(JType.of(int.class), JType.Int);
        assertEquals(JType.of(long.class), JType.Long);
        assertEquals(JType.of(char.class), JType.Char);
        assertEquals(JType.of(float.class), JType.Float);
        assertEquals(JType.of(double.class), JType.Double);
        assertEquals(JType.of(boolean.class), JType.Bool);
        assertEquals(JType.of(void.class), JType.Void);
    }

    @Test
    public void testOfReference() {
        assertEquals(JType.of(Object.class), JType.Object);
        int[] ar = new int[0];
        assertTrue(JType.of(ar.getClass()) instanceof JType.ArrayType);
        assertTrue(JType.of(getClass()) instanceof JType.ObjectRef);
        // FIXME: the follwing cases fail.
        // assertTrue(JType.of(Alias.class) instanceof TypeAlias);
        // assertTrue(JType.of(Predicate.class) instanceof JType.FnIf);
        // assertTrue(JType.of(Inner.class) instanceof JType.InnerType);
        // assertTrue(JType.of(InnerIf.class) instanceof JType.InnerType);

    }
}
