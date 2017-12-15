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
import com.oracle.vector.PatchableVecUtils;
import com.oracle.vector.el.Shapes;
import com.oracle.vector.el.expression.Expression;
import com.oracle.vector.el.expression.Expressions;
import com.oracle.vector.el.stmt.Builder;
import com.oracle.vector.ops.TestOps;

import java.lang.invoke.MethodHandle;

public class CrossLane {


    void computeCall(int bound , double[]arr , double  scale1, double  scale2)
    {
        for(int i = bound; i > 0; i--)
            for(int j = 0; j <= i - 1; j++)
                arr[j] = scale1 * arr[j + 1] + scale2 * arr[j];
    }


    static final MethodHandle computeKernelEL;

    static {
        try {
            computeKernelEL = Builder.builder(Double.class,Shapes.L4).bind(Double.class,Shapes.L4,Double.class,Shapes.L4,Integer.class,Shapes.L1,(scale1, scale2, j, b) ->
                b.bindDoubleIndexable(Shapes.L4,(arr,b2) -> {
                    Expression<Double,Shapes.LENGTH4> j1,jp1;
                    j1 = arr.get(j);
                    jp1 = arr.get(j.add(Expressions.constant(1)));
                    return b.return_(scale1.mul(jp1).add(scale2.mul(j1)));
                })
            ).build(TestOps.instance);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static void main(String[] args) throws Throwable{
        System.out.println(computeKernelEL);
        double[] src1 = {1d,1d,1d,1d,16d};
        double[] src2 = {2d,2d,2d,2d};
        double[] src3 = {3d,3d,3d,3d};

        Long4 scale1 = PatchableVecUtils.long4FromDoubleArray(src2,0);
        Long4 scale2 = PatchableVecUtils.long4FromDoubleArray(src3,0);

        //scale1 * arr[j + 1] + scale2 * arr[j];
        // {2d..} * {1,1,1,16} + {3d,..} * {1d,...}
        // {2,2,2,32} + {3,3,3,3}
        //    {5d,5d,5d,35d}
        Long4 res = (Long4) computeKernelEL.invokeExact(scale1,scale2,0,src1);

        double[] result = new double[4];
        PatchableVecUtils.long4ToDoubleArray(result,0,res);


        for(int i = 0 ; i < result.length; i++){
            System.out.print(result[i] + " ");
        }
        System.out.println();

    }

    private static int plusOne(int i){
        return i+1;
    }
}
