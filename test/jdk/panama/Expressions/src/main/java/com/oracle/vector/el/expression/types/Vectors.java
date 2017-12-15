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
package com.oracle.vector.el.expression.types;

import com.oracle.vector.el.Shapes;

public class Vectors {


    public static final VectorClass<Float,Shapes.LENGTH8> float256 = new Float256();

    @SuppressWarnings("unchecked")
    public static <E> VectorClass<E,?> vectorClass(Class<E> lane, int width){
        VectorClass<?,?> res = null;
        if(lane == Float.class){
            switch(width){
                //case 32:
                //case 64:
                //case 128:
                case 256:
                    res = float256;
                default:
                    throw new UnsupportedOperationException("Type not supported");
            }
        } else if(lane == Integer.class) {

        } else if(lane == Short.class) {

        } else if(lane == Double.class) {

        } else if(lane == Long.class) {

        } else if(lane == Byte.class) {

        }

        return (VectorClass<E,?>) res;
    }
}
