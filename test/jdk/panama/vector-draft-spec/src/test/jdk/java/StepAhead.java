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

import java.util.Random;

public class StepAhead<S extends Vector.Shape<Vector<?, ?>>> {

    private final FloatVector.FloatSpecies<S> species;

    public StepAhead(FloatVector.FloatSpecies<S> zed) {
        species = zed;
    }


    public void sarray(float[] array, float scaleA, float scaleB) {

        int bound = array.length - (species.length() - 1);

        Vector<Float, S> scaleAv = species.broadcast(scaleA);
        Vector<Float, S> scaleBv = species.broadcast(scaleB);
        Vector<Float, S> ii = species.fromArray(array, 0);
        for (int i = 1; i < bound; i++) {
            FloatVector<S> ip1 = species.fromArray(array, i);
            ip1.mul(scaleAv).add(scaleBv.mul(ii)).intoArray(array, i);
            ii = ip1;
        }
    }


    public static void main(String args[]) {
        final int ARRAY_SIZE = 2048;

        FloatVector.FloatSpecies<?> species = (FloatVector.FloatSpecies<?>) Vector.speciesInstance(Float.class, Shapes.S_256_BIT);
        StepAhead<?> sa = new StepAhead<>(species);

        float[] sarray = new float[ARRAY_SIZE];
        Random r = new Random();
        for (int i = 0; i < ARRAY_SIZE; i++) {
            sarray[i] = r.nextFloat();
        }

        //Kernel
        sa.sarray(sarray, 1.0f, .5f);
    }
}
