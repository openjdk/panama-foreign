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

import com.oracle.vector.el.Shapes;
import com.oracle.vector.el.stmt.Builder;

import java.lang.invoke.MethodHandle;

public class ZipTest {


    public static final int SIZE = 500000;
    public static final int LOOPS= 30000;
    public static final MethodHandle mh = Builder.zip(Shapes.L8,(left, right, builder) -> builder.return_(left.add(right)));

    public static final float[] a1 = new float[SIZE];
    public static final float[] a2 = new float[SIZE];
    public static final float[] a3 = new float[SIZE];


    public static void main(String[] args){

        System.out.println("Initialized successfully.");

        loopKern();

    }

    public static void runKern() throws Throwable {
        mh.invokeExact(a1,a2,a3);
    }


    public static void loopKern(){
        try {
            for(int i = 0; i < LOOPS; i++){
                runKern();
            }
        } catch (Throwable e){
            throw new Error(e);
        }
    }




}
