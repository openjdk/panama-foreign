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
package com.oracle.vector.el;

import com.oracle.vector.el.expression.Expression;
import com.oracle.vector.el.expression.Expressions;
import com.oracle.vector.el.expression.VConst;
import com.oracle.vector.el.visitor.ExpressionEvaluator;

import java.util.Optional;

public class Val<E,S extends Shape> implements Expression<E,S>{
    final S shape;
    final Class<E> etype;

    public Val(S shape, Class<E> etype){
        this.shape = shape;
        this.etype = etype;
    }

    @Override
    public <R> R accept(ExpressionEvaluator<R> v) {
        return v.visit(this);
    }

    @Override
    public Class<E> elementType(){
        return etype;
    }

    @Override
    public Optional<VConst<E, S>> toVConst() {
        return Optional.empty();
    }

    @Override
    public S shape() {
       return shape;
    }

    public Class<?> getValueLevelElementType(){
        if(etype.equals(Byte.class)){
            return byte.class;
        } else if(etype.equals(Short.class)){
            return short.class;
        } else if(etype.equals(Float.class)){
            return float.class;
        } else if(etype.equals(Integer.class)){
            return int.class;
        } else if(etype.equals(Long.class)){
            return long.class;
        } else if(etype.equals(Double.class)){
            return double.class;
        } else if(etype.equals(Character.class)){
            return char.class;
        }

        throw new UnsupportedOperationException("Invalid lane element type.");
    }

    public Class<?> getPackedType() {
        return Expressions.packedType(this.elementType(),this.shape());
    }
}
