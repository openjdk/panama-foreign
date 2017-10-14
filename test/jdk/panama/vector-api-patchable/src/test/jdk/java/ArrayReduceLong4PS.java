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
import com.oracle.vector.PatchableVecUtils;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;


public class ArrayReduceLong4PS {



    private static final int SIZE = 2097152;//131072;


    public static final MethodHandle sop;

    static {
        try {
            sop = MethodHandles.collectArguments(PatchableVecUtils.sumprod_float_L4_sunk,1,PatchableVecUtils.MHm256_vmovdqu_load_floatarray);

        } catch (Throwable e){
            throw new Error(e);
        }
    }

    public static void main(String[] args){
        float[] l = new float[SIZE];
        float[] r = new float[SIZE];

        init(l,r);
        float result = 0f;
        for(int i = 0; i < 25000; i++){
            result = sumOfProd(l,0,r,0,SIZE);
        }
        System.out.println("RESULT : " + result);
    }

    public static void init(float[] left, float[] right){
        for(int i = 0; i < left.length; i++){
            left[i]  = 1f;
            right[i] = 1f;
        }
    }

    public static float sumOfProd(float[] left, int leftOff, float[] right, int rightOff, int len){
       float sum = 0f;

       int lmax = leftOff + len;
       for(int i = leftOff, j = rightOff; i < lmax; i+=8, j+=8){
           sum = sop(left,i,right,j,sum);
       }
       return sum;
    }

    private static float sop(float[] left, int li, float[] right, int ri, float sum){
        try {
            return (float) sop.invokeExact(sum,left,li,right,ri);
        } catch (Throwable e){
            throw new Error(e);
        }
    }

}
