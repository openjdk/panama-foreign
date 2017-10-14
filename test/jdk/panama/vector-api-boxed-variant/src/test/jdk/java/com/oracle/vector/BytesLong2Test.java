/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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
 */
package com.oracle.vector;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class BytesLong2Test {
    static final Vector.Species<Byte, Shapes.S128Bit> BS =
            Vector.byteSpecies(Shapes.SHAPE_128_BIT);

    @Test
    public void testGetElement() {
        Vector<Byte, Shapes.S128Bit> b = BS.generate(i -> (byte) i);

        for (int i = 0; i < BS.length(); i++) {
            Byte bi = (byte) i;
            Assert.assertEquals(bi, b.getElement(i));
        }
    }

    @Test
    public void testPutElement() {
        Vector<Byte, Shapes.S128Bit> b = BS.generate(i -> (byte) i);

        Byte bi = (byte) 0xFF;
        for (int i = 0; i < BS.length(); i++) {
            b = b.putElement(i, (byte) 0xFF);
            Assert.assertEquals(bi, b.getElement(i));
        }
    }

    @Test
    public void testXor() {
        Vector<Byte, Shapes.S128Bit> a = BS.fromElement((byte) 0b01010101);

        Vector<Byte, Shapes.S128Bit> b = BS.fromElement((byte) 0b10101010);

        Vector<Byte, Shapes.S128Bit> xor = a.xor(b);

        Assert.assertEquals(BS.fromElement((byte) 0b11111111), xor);
    }

    @Test
    public void testCompareEqual() {
        Vector<Byte, Shapes.S128Bit> a = BS.zeroVector();

        Vector<Byte, Shapes.S128Bit> b = BS.generate(i -> (byte) (i % 2 == 0 ? 1 : 0));

        Vector<Byte, Shapes.S128Bit> ceq = a.compareEqual(b);

        Assert.assertEquals(BS.generate(i -> (byte) (i % 2 == 0 ? 0 : 0xFF)), ceq);
    }

    @Test
    public void testToMask() {
        Vector<Byte, Shapes.S128Bit> a = BS.generate(i -> (byte) (i % 2 == 0 ? 0 : 0x80));

        Vector.Mask<Shapes.S128Bit> mask = a.toMask();

        Assert.assertEquals(0b1010101010101010, mask.toLong());

        Vector<Byte, Shapes.S128Bit> fromMask = mask.toVector(Byte.class);

        Assert.assertEquals(BS.generate(i -> (byte) (i % 2 == 0 ? 0 : 0xFF)), fromMask);
    }

    @Test
    public void testShuffle() {
        Vector<Byte, Shapes.S128Bit> a = BS.generate(i -> (byte) (i + 1));

        Vector.Shuffle<Shapes.S128Bit> reverse = BS.iota(a.length() - 1, -1, a.length());

        Vector<Byte, Shapes.S128Bit> shuffle = a.shuffle(reverse);

        Assert.assertEquals(BS.generate(i -> (byte) (15 - i + 1)), shuffle);
    }

    @Test
    public void testAdd() {
        Vector<Byte, Shapes.S128Bit> a = BS.generate(i -> (byte) i);

        Vector<Byte, Shapes.S128Bit> b = BS.generate(i -> (byte) (i + 16));

        Vector<Byte, Shapes.S128Bit> add = a.add(b);

        Assert.assertEquals(BS.generate(i -> (byte) (16 + i + i)), add);
    }

    @Test
    public void testBlend() {
        Vector<Byte, Shapes.S128Bit> a = BS.generate(i -> (byte) i);

        Vector<Byte, Shapes.S128Bit> b = BS.generate(i -> (byte) (i + 16));

        Vector<Byte, Shapes.S128Bit> mask = BS.generate(i -> (byte) ((i % 2) == 0 ? 0 : 0x80));

        Vector<Byte, Shapes.S128Bit> blend = a.blend(b, mask);

        Assert.assertEquals(BS.generate(i -> (byte) ((i % 2) == 0 ? i : i + 16)), blend);
    }

    @Test
    public void testAddArrays() {
        Byte[] a = new Byte[65];
        Byte[] b = new Byte[65];
        Byte[] r = new Byte[65];

        Arrays.fill(a, (byte) 1);
        Arrays.fill(b, (byte) 2);

        int l = a.length;
        for (int i = 0; i < l; i += BS.length()) {
            Vector<Byte, Shapes.S128Bit> va = BS.fromArray(a, i);
            Vector<Byte, Shapes.S128Bit> vb = BS.fromArray(b, i);
            Vector<Byte, Shapes.S128Bit> vr = va.add(vb);
            vr.intoArray(r, i);
        }

        Byte[] er = new Byte[65];
        Arrays.fill(er, (byte) 3);
        Assert.assertArrayEquals(er, r);
    }
}