/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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

import jdk.internal.foreign.memory.Array;
import jdk.internal.foreign.memory.*;
import jdk.internal.foreign.memory.Container;
import jdk.internal.foreign.memory.Function;
import jdk.internal.foreign.memory.Scalar;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.testng.Assert.*;

/*
 * @test
 * @ignore this test relies too much on the toString representation of layouts
 * @summary Unit test for Type Descriptor
 * @modules java.base/jdk.internal.foreign.types
 * @run testng DescriptorTest
 */
public class DescriptorTest {

    private void validate(String descriptor, List<Type> types) {
        DescriptorParser d = new DescriptorParser(descriptor);
        List<Type> actual = d.parseLayout().collect(Collectors.toList());
        assertEquals(actual, types, descriptor);
    }

    @Test
    public void testValidCharacters() {
        final String validSingleType = "osilqOSILQfdeFDEpxVcB";
        for (char ch = Character.MIN_VALUE; ch < Character.MAX_VALUE; ch++) {
            try {
                Stream<Type> ts = new DescriptorParser("" + ch).parseLayout();
                Optional<Type> t = ts.findFirst();
                if (t.isPresent()) {
                    assertFalse(validSingleType.indexOf(ch) == -1);
                } else {
                    fail("separator-only layouts are not supported by the grammar: " + ch);
                }
            } catch (DescriptorParser.InvalidDescriptorException ex) {
                assertTrue(validSingleType.indexOf(ch) == -1);
            }
        }
    }

    @Test
    public void testSignedTypes() {
        final String signedTypes = "osilqfde";
        for (char ch: signedTypes.toCharArray()) {
            Type type = new Scalar(ch, Type.Endianness.NATIVE);
            List expected = Collections.singletonList(type);
            validate("" + ch, expected);

            ch = Character.toUpperCase(ch);
            type = new Scalar(ch);
            expected = Collections.singletonList(type);
            validate("" + ch, expected);
        }
    }

    @Test
    public void testEndianness() {
        final String validTypes = "osilqOSILQfdeFDEc";
        for (char ch: validTypes.toCharArray()) {
            Type type = new Scalar(ch);
            List<Type> expected = Collections.singletonList(type);
            validate("" + ch, expected);

            type = new Scalar(ch, Type.Endianness.NATIVE);
            expected = Collections.singletonList(type);
            validate("" + ch, expected);

            type = new Scalar(ch, Type.Endianness.BIG);
            expected = Collections.singletonList(type);
            validate(">" + ch, expected);

            type = new Scalar(ch, Type.Endianness.LITTLE);
            expected = Collections.singletonList(type);
            validate("<" + ch, expected);

            type = new Scalar(ch, Type.Endianness.NATIVE);
            expected = Collections.singletonList(type);
            validate("@" + ch, expected);

            type = new Scalar(ch);
            expected = Collections.singletonList(type);
            validate("@" + ch, expected);
        }
    }

    @DataProvider(name = "badCases")
    public Object[][] badCases() { return badCases; }

    Object[][] badCases = {
            { "i\t= O\n", "cannot have space after =" },
            { "l:2b\n3b\r2 b", "cannot have space after count" },
            { "Ip\uBADCb", "bad character > 0x7E" },
            { "f\bi", "Invalid character < ' '"},
            { "=0S", "count cannot be 0" },
            { "p:", "type needed after ':'"},
            { "F:3b4b", "float cannot be storage type" },
            { "(i)", "function need return type" },
            { "S:2bb0b", "0 bit is not allowed" },
            { "s:", "not finished bitfields" },
            { "[i[]l]", "empty container"}
    };

    @Test(expectedExceptions = {DescriptorParser.InvalidDescriptorException.class},
          dataProvider = "badCases")
    public void testBadDescriptors(String descriptor, String reason) {
        DescriptorParser d = new DescriptorParser(descriptor);
        d.parseLayout().count();
        fail("Should not reach here! " + reason);
    }

    @Test
    public void testDescriptors() {
        String descriptor = "i=qLo:2b3b>[sqFp:(ii)V4c]p:c";
        List<Type> expected = List.of(Types.INT,
                Types.INT64,
                Types.UNSIGNED.LONG,
                new BitFields((Scalar) Types.BYTE, new int[] { 2, 3 }),
                new Container(false,
                        Types.BE.SHORT,
                        Types.BE.LONG_LONG,
                        Types.BE.UNSIGNED.FLOAT,
                        new Pointer(new Function(new Type[] { Types.INT, Types.INT },
                                Types.VOID, false)),
                        new Array(Types.CHAR, 4)),
                new Pointer(Types.CHAR));

        validate(descriptor, expected);

        descriptor = "[ll[i|<i|q:b1b16b4b8b|s:bbb]p:(i*)i]*c";
        expected = List.of(new Container(false,
                Types.LONG,
                Types.LONG,
                new Container(true,
                        Types.INT,
                        Types.LE.INT,
                        new BitFields((Scalar) Types.LONG_LONG, new int[] {1,1,16,4,8}),
                        new BitFields((Scalar) Types.SHORT, new int[] {1,1,1})),
                new Pointer(new Function(new Type[] { Types.INT }, Types.INT, true))),
                new Array(Types.CHAR, -1));
        validate(descriptor, expected);

        descriptor = "(l)s:2b2b4b";
        expected = List.of(new Function(new Type[] { Types.LONG },
                new BitFields((Scalar) Types.SHORT, new int[] {2,2,4}), false));
        validate(descriptor, expected);


        descriptor = "(ls:2b2b4b)V";
        expected = List.of(new Function(new Type[] { Types.LONG,
                new BitFields((Scalar) Types.SHORT, new int[] {2,2,4}) },
                Types.VOID, false));
        validate(descriptor, expected);

        descriptor = "[s:2b2b2bl:4b4b8b][[if]|[f|l]]";
        expected = List.of(new Container(false,
                new BitFields((Scalar) Types.SHORT, new int[] {2,2,2}),
                new BitFields((Scalar) Types.LONG, new int[] {4,4,8})),
                new Container(true,
                        new Container(false, Types.INT, Types.FLOAT),
                        new Container(true, Types.FLOAT, Types.LONG)));
        validate(descriptor, expected);

        descriptor = "  il#int and long\n\r\tfD  #float #and\tdouble\r\np:c  #done";
        expected = List.of(Types.INT, Types.LONG, Types.FLOAT, Types.UNSIGNED.DOUBLE,
                new Pointer(Types.CHAR));
        validate(descriptor, expected);

        descriptor = "#begin  il#int and long\n\r\tf\nD  #\tdouble\r\np:c";
        expected = List.of(Types.FLOAT, Types.UNSIGNED.DOUBLE, new Pointer(Types.CHAR));
        validate(descriptor, expected);

        descriptor = "l:2b\n3b\r2b";
        expected = List.of(new BitFields((Scalar) Types.LONG, new int[] {2,3,2}));
        validate(descriptor, expected);

        descriptor = "=o=s=l=q=O=S=L=Q=256v=16i>=32f=128I=64F";
        expected = List.of(Types.INT8, Types.INT16, Types.INT32, Types.INT64,
                Types.UNSIGNED.INT8, Types.UNSIGNED.INT16, Types.UNSIGNED.INT32, Types.UNSIGNED.INT64,
                new Scalar('v', Type.Endianness.NATIVE, 256), Types.INT16,
                new Scalar('f', Type.Endianness.BIG, 32), new Scalar('I', Type.Endianness.NATIVE, 128),
                new Scalar('F', Type.Endianness.NATIVE, 64));
        validate(descriptor, expected);

        descriptor = "i:2bb3b20i";
        expected = List.of(new BitFields((Scalar) Types.INT, new int[] {2,1,3}),
                new Array(Types.INT, 20));
        validate(descriptor, expected);
    }

    @Test
    public void testFunctions() {
        String descriptor = "()V";
        List<Type> expected = List.of(new Function(new Type[0], Types.VOID, false));
        validate(descriptor, expected);
    }
}
