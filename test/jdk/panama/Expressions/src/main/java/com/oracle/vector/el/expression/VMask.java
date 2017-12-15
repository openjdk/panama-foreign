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

import com.oracle.vector.el.Shape;
import com.oracle.vector.el.visitor.ExpressionEvaluator;

import java.util.Optional;

public class VMask<E,S extends Shape> implements Expression<E,S>{

    Expression<Boolean,S> mask;
    Expression<E,S> thn, els;

    VMask(Expression<Boolean,S> b, Expression<E,S> t, Expression<E,S> e){
        this.mask = b;
        this.thn  = t;
        this.els  = e;

    }

    @Override
    public <R> R accept(ExpressionEvaluator<R> v) {
        return v.visit(this);
    }

    @Override
    public S shape() {
        return thn.shape();
    }

    @Override
    public Class<E> elementType() {
        return thn.elementType();
    }

    @Override
    public Optional<VConst<E, S>> toVConst() {
        return Optional.empty();
    }

    public Expression<Boolean, S> getMask() {
        return mask;
    }

    public Expression<E, S> getThn() {
        return thn;
    }

    public Expression<E, S> getEls() {
        return els;
    }
}
