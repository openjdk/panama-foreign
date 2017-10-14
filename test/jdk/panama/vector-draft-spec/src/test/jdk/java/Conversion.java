/*
 * Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
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
 * or visit www.oracle.com if you need additional information or have
 * questions.
 */

import com.oracle.vector.ByteVector;
import com.oracle.vector.FloatVector;
import com.oracle.vector.IntVector;
import com.oracle.vector.LongVector;
import com.oracle.vector.Shapes;
import com.oracle.vector.Vector;

public class Conversion {


    public static void main(String args[]) {
        FloatVector.FloatSpecies<Shapes.S256Bit> species = (FloatVector.FloatSpecies<Shapes.S256Bit>) Vector.speciesInstance(Float.class, Shapes.S_256_BIT);

        float[] src = {0f, 1f, 2f, 3f, 4f, 5f, 6f, 7f};
        FloatVector<Shapes.S256Bit> v1 = species.fromArray(src, 0);

        IntVector<Shapes.S256Bit> v1i = (IntVector<Shapes.S256Bit>) v1.cast(Integer.class);
        IntVector<Shapes.S128Bit> v1ishort = (IntVector<Shapes.S128Bit>) v1.cast(Integer.class, Shapes.S_128_BIT);
        IntVector<Shapes.S512Bit> v1ilong = (IntVector<Shapes.S512Bit>) v1.cast(Integer.class, Shapes.S_512_BIT);

        System.out.println("Int Conversions from Float (1f)");
        for (int i = 0; i < v1i.length(); i++) {
            System.out.print(v1i.get(i) + " ");
        }
        System.out.println();

        for (int i = 0; i < v1ishort.length(); i++) {
            System.out.print(v1ishort.get(i) + " ");
        }
        System.out.println();

        for (int i = 0; i < v1ilong.length(); i++) {
            System.out.print(v1ilong.get(i) + " ");
        }
        System.out.println();
        System.out.println();

        System.out.println("Byte Conversions from Float (1f)");
        ByteVector<Shapes.S256Bit> vb = (ByteVector<Shapes.S256Bit>) v1.cast(Byte.class);
        for (int i = 0; i < vb.length(); i++) {
            System.out.print(vb.get(i) + " ");
        }
        System.out.println();
        System.out.println();

        ByteVector<Shapes.S512Bit> vb2 = (ByteVector<Shapes.S512Bit>) v1.cast(Byte.class, Shapes.S_512_BIT);
        for (int i = 0; i < vb2.length(); i++) {
            System.out.print(vb2.get(i) + " ");
        }
        System.out.println();
        System.out.println();

        System.out.println("Long Conversions from Float (1f)");
        LongVector<Shapes.S256Bit> vl = (LongVector<Shapes.S256Bit>) v1.cast(Long.class);
        for (int i = 0; i < vl.length(); i++) {
            System.out.print(vl.get(i) + " ");
        }
    }

}
