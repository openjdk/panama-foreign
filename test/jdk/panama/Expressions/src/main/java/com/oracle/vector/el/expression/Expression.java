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
package com.oracle.vector.el.expression;

import com.oracle.vector.el.Ops;
import com.oracle.vector.el.Shape;
import com.oracle.vector.el.Shapes;
import com.oracle.vector.el.expression.bexp.VBinBExp;
import com.oracle.vector.el.visitor.ExpressionEvaluator;

import java.util.Optional;
import java.util.function.Supplier;

public interface Expression<E,S extends Shape> {
    default Expression<E,S> add(Expression<E,S> v) { return new VAdd<E,S>(this,v);}
    default Expression<E,S> sub(Expression<E,S> v) { return new VSub<E,S>(this,v);}
    default Expression<E,S> mul(Expression<E,S> v) { return new VMul<E,S>(this,v);}
    default Expression<E,S> div(Expression<E,S> v) { return new VDiv<E,S>(this,v);}
    default Expression<E,S> ite(Expression<Boolean,Shapes.LENGTH1> t, Expression<E,S> thn, Expression<E,S> els){ return new ITE<>(t,thn,els);}


    default Expression<Integer,Shapes.LENGTH1> pack(int val){
        return new VConst<>(Shapes.L1,val);
    }

    default Expression<Integer,Shapes.LENGTH4> broadcast4(int val){
        return new VConst<>(Shapes.L4,val);
    }


    //Horizontal Reductions
    default Expression<E,Shapes.LENGTH1> sum() { return new VSum<E>(this);}
    default Expression<E,Shapes.LENGTH1> prod() { return new VProd<E>(this);}

    <R> R accept(ExpressionEvaluator<R> v);

    default int length() {
        return this.shape().length();
    }
    S shape();
    Class<E> elementType();

    Optional<VConst<E,S>> toVConst();
}
