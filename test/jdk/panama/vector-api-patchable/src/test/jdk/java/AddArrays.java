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
 * or visit www.oracle.com if you need additional information or have
 * questions.
 */
import com.oracle.vector.FloatVector;
import com.oracle.vector.Shapes;

public class AddArrays {

    private static final int SIZE = 2097152;//131072;

    private static float[] proc(float[] left, float[] right, float[] res){
        if(left.length != right.length){
            throw new UnsupportedOperationException("Arrays need to be equal in length");
        }
        for(int i = 0; i < left.length; i+=8){
            addArrays(left,right,res,i);
        }
        return res;
    }

    private static void addArrays(float[] left, float[] right, float[] res, int i){
        FloatVector<Shapes.S256Bit> l  = (FloatVector<Shapes.S256Bit>) FloatVector.float256FromArray(left,i);
        FloatVector<Shapes.S256Bit> r  = (FloatVector<Shapes.S256Bit>) FloatVector.float256FromArray(right,i);
        FloatVector<Shapes.S256Bit> lr = (FloatVector<Shapes.S256Bit>) l.add(r);
        lr.intoArray(res,i);

    }

    private static void init(float[] l, float[] r){
        for(int i = 0; i < SIZE; i++){
            l[i] = 1f;
            r[i] = 2f;
        }
    }

    public static void main(String args[]){
        float[] l = new float[SIZE],
                r = new float[SIZE];
        float[] res = null;

        init(l,r);

        float[] resArr = new float[SIZE];
        for(int i = 0; i < 50000; i++){
            res = proc(l,r,resArr);
        }
    }
}
