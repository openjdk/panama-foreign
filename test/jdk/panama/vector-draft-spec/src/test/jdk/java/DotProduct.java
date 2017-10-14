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

import com.oracle.vector.FloatVector;
import com.oracle.vector.Shapes;
import com.oracle.vector.Vector;

public class DotProduct<S extends Vector.Shape<Vector<?, ?>>> {

    private final FloatVector.FloatSpecies<S> spec;

    DotProduct(FloatVector.FloatSpecies<S> v) {
        spec = v;
    }


    float dot(float[] a, float[] b, int offa, int offb, int len) {
        int i = 0;
        float sum = 0.0f;

        if (spec.length() < len) {
            for (; (i + 8) < len; i += spec.length()) {
                FloatVector<S> l, r;
                l = spec.fromArray(a, offa + i);
                r = spec.fromArray(b, offb + i);
                sum += l.mul(r).sumAll();
            }
        }

        /* Naive fixup routine */
        if (len - i > 0) {
            boolean[] ms = new boolean[spec.length()];
            for (int j = 0; j < (len - i); j++) {
                ms[j] = true;
            }

            Vector.Mask<Float, S> m = spec.constantMask(ms);
            FloatVector<S> fres = spec.fromArray(a, offa + i, m).mul(spec.fromArray(b, offb + i, m));
            sum += fres.sumAll();
        }

        return sum;
    }


    public static void main(String args[]) {
        float[] a = {1, 1, 1, 1, 1, 1, 1, 1, 1};
        float[] b = {9, 9, 9, 9, 9, 9, 9, 9, 9};

        FloatVector.FloatSpecies<Shapes.S256Bit> species = (FloatVector.FloatSpecies<Shapes.S256Bit>) Vector.speciesInstance(Float.class, Shapes.S_256_BIT);

        DotProduct<Shapes.S256Bit> dp = new DotProduct<>(species);

        float res = dp.dot(a, b, 0, 0, a.length);

        System.out.println(res);
    }
}
