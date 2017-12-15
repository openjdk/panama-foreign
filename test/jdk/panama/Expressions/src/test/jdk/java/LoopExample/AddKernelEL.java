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

import com.oracle.vector.el.Shapes;
import com.oracle.vector.el.stmt.Builder;
import com.oracle.vector.ops.TestOps;

import java.lang.invoke.MethodHandle;

public class AddKernelEL {

    public static final MethodHandle addKernelLoop = Builder.zip(Shapes.L8,
            (a, b, builder) -> builder.return_(a.add(b)));

    public static final MethodHandle addKernel = Builder.builder(Float.class,Shapes.L8)
            .bind(Float.class,Shapes.L8,Float.class,Shapes.L8, (a, b, builder) ->builder.return_(a.add(b))) //(FloatxL8,FloatxL8)FloatxL8
            .build(TestOps.instance); //Instantiate with a methodhandle map



    public static void main(String args[]){
        System.out.println(addKernel);
        System.out.println(addKernelLoop);
    }
}
