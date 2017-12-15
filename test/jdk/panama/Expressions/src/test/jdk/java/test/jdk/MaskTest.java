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
package test;

import com.oracle.vector.PatchableVecUtils;
import com.oracle.vector.el.Shapes;
import com.oracle.vector.el.expression.Expression;
import com.oracle.vector.el.expression.Expressions;
import com.oracle.vector.el.stmt.Builder;
import com.oracle.vector.ops.TestOps;

import java.lang.invoke.MethodHandle;

public class MaskTest {


    public static final MethodHandle mask1 = Builder.builder(Float.class,Shapes.L8).bind(Float.class,Shapes.L8,Float.class,Shapes.L8,(left,right,b) -> {
        Expression<Float,Shapes.LENGTH8> masked = Expressions.mask(Expressions.eq(left,right),left.add(right),left.mul(right));
        return b.return_(masked);
    }).build(TestOps.instance);



    public static Long4 invoker() throws Throwable{
        float[] f = {5,5,1,5,5,6,5,5};
        float[] s = {6,6,6,6,6,6,6,6};
        Long4 fives, sixes, masked;
        masked = Long4.ZERO;

        fives = PatchableVecUtils.long4FromFloatArray(f,0);
        sixes = PatchableVecUtils.long4FromFloatArray(s,0);
        for(int i = 0; i < 10000000; i++){
            masked = (Long4) mask1.invokeExact(sixes,fives);
        }

        return masked;

    }

    public static void main(String args[]) throws Throwable{


        Long4 res = invoker();

        float[] output = new float[8];
        /*
        for(int i = 0; i < output.length; i++){
            System.out.println(i + ": " + output[i]);
        }
        */

        PatchableVecUtils.long4ToFloatArray(output,0,res);

        for(int i = 0; i < output.length; i++){
            System.out.println(i + ": " + output[i]);
        }
    }
}
