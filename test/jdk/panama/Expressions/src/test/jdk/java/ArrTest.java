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
import com.oracle.vector.el.Shapes;
import com.oracle.vector.el.expression.Expression;
import com.oracle.vector.el.stmt.Builder;
import com.oracle.vector.ops.TestOps;

import java.lang.invoke.MethodHandle;

public class ArrTest {


    public static final MethodHandle kern = Builder.builder(Float.class,Shapes.L8).bind(Integer.class, Shapes.L1, Float.class,Shapes.L8,Float.class,Shapes.L8,(i,left,right,b) ->
        b.bindFloatIndexable(Shapes.L8,(arr,bIn) -> {
          Expression<Float,Shapes.LENGTH8> r = arr.get(i);
          Expression<Float,Shapes.LENGTH8> res = left.add(right.mul(r));
          return b.return_(res);
        })
    ).build(TestOps.instance);



    public static void main(String[] args) {
        System.out.println(kern);
    }
}
