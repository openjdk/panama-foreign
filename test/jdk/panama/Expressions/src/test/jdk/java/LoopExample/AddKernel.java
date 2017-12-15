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
package LoopExample;

import com.oracle.vector.el.Ops;
import com.oracle.vector.ops.OpProvider;
import com.oracle.vector.ops.OpProviders;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class AddKernel {

    private static final MethodHandle addKernel = kernel();


    //Adding two arrays of equal length and sinking the result.
    public void add(float[] a, float[] b, float[] c){
        for(int i = 0; i < (a.length & b.length); i++){
            c[i] = a[i] + b[i];
        }
    }

    public void addVectorized(float[] a, float[] b, float[] c) {
        try {
            for(int i = 0; i < a.length; i+=Float256.elements()){
                addKernel.invokeExact(i,a,b,c);
            }
        } catch (Throwable e) {
           throw new Error(e);
        }
    }


    /*
    (int index,float[] a, float[] b, float[] c) ->
    LoadFloat256(a,index) ->-----|
                                 |
                                 |-----------|
                                        Add(av,bv)-------->StoreFloat256(c,index);
                                 |--------------|
                                 |
    LoadFloat256(b,index) ->-----|
    */


    private static MethodHandle kernel(){

        //Look up Op providers
        OpProvider ops = OpProviders.provider(Float256.class);
        //LoadFloat256
        //(float[],int)Float256
        MethodHandle load = ops.getOp(Float.class,Ops.ARY_LOAD,8);

        //(Float256,float[],int)void
        MethodHandle store = ops.getOp(Float.class,Ops.ARY_STORE,8);

        //(Float256,Float256)Float256
        MethodHandle add = ops.getOp(Float.class,Ops.ADD,8);

        //(Float256,Float256)Float256
        MethodHandle kernel = add;

        //Step one.  Adapt loads to adds.

        //(float[],int,Float256)Float256
        kernel = MethodHandles.collectArguments(kernel,0,load);

        //Float256(float[],int,float[],int)
        kernel = MethodHandles.collectArguments(kernel,2,load);

        //Step two.  Collect intermediate kernel result to store MethodHandle

        //void(float[],int,float[],int,float[],int)
        kernel = MethodHandles.collectArguments(store,0,kernel);

        //Step three.  Eliminate redundant int args with a fanout procedure.
        //Note: The MethodType construction defines our resulting type.
        //      The int args specify the reordering of arguments from the final type
        //      adapted to our intermediate construction.
        //void(int,float[],float[],float[])
        kernel = MethodHandles.permuteArguments(kernel,
                                                MethodType.methodType(void.class,int.class,float[].class,float[].class,float[].class),
                                                1,0,2,0,3,0);

        //kernel is now the desired shape, arity, and function to the spec above.
        return kernel;
    }


    /*
    private static MethodHandle kernelEL(){
        OpProvider ops = OpProviders.provider(Float.class,8);
        MethodHandle k = Builder.builder().bind(Float.class,Shapes.L8,Float.class,Shapes.L8, (Val<Float,Shapes.LENGTH8> left, Val<Float,Shapes.LENGTH8> right, Builder b) -> b.return_(left.add(right))).build(ops);
    }
    */
}
