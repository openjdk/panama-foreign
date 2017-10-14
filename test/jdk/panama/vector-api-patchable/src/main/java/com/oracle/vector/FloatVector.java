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
package com.oracle.vector;

public abstract class FloatVector<S extends Vector.Shape<Vector<?,S>>> implements Vector<Float,S> {

    private static final Float128Vector FLOAT128 = new Float128Vector(Long2.ZERO);
    private static final Float256Vector FLOAT256 = new Float256Vector(Long4.ZERO);

    public static Vector<Float,Shapes.S256Bit> float256Broadcast(float val){
        float[] ary = {val,val,val,val,val,val,val,val};
        return FLOAT256.fromArray(ary);
    }

    public static Vector<Float,Shapes.S256Bit> float256FromArray(float[] ary, int offset){
        return FLOAT256.fromArray(ary,offset);
    }

    public static Vector<Float,Shapes.S256Bit> float256FromArray(float[] ary){
        return FLOAT256.fromArray(ary,0);
    }

    public static Vector<Float,Shapes.S256Bit> zero256(){
        return FLOAT256;
    }

    public abstract Vector<Float,S> fromArray(float[] ary, int offset);

    public Vector<Float,S> fromArray(float[] ary){
        return fromArray(ary,0);
    }

    public abstract void intoArray(float[] ary, int offset);

    public void intoArray(float[] ary){
        intoArray(ary,0);
    }

    public float[] toArray(){
        float[] res = new float[this.length()];
        intoArray(res);
        return res;
    }
}
