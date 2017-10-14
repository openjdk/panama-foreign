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
package com.oracle.vector.el.comp;

import com.oracle.vector.el.Shape;
import com.oracle.vector.el.Val;
import com.oracle.vector.el.expression.*;
import com.oracle.vector.el.expression.bexp.VBinBExp;
import com.oracle.vector.el.expression.IndexedVal;
import com.oracle.vector.el.visitor.ExpressionEvaluator;

import java.util.ArrayList;
import java.util.List;

public class ExpVarOrder implements ExpressionEvaluator<List<Val>> {

    public static final ExpVarOrder instance = new ExpVarOrder();

    @Override
    public <E, T extends Shape> List<Val> visit(VAdd<E, T> v) {
        return append(v.getLeft().accept(this),v.getRight().accept(this));
    }

    @Override
    public <E, T extends Shape> List<Val> visit(VDiv<E, T> v) {
        return append(v.getLeft().accept(this),v.getRight().accept(this));
    }

    @Override
    public <E, T extends Shape> List<Val> visit(VMul<E, T> v) {
        return append(v.getLeft().accept(this),v.getRight().accept(this));
    }

    @Override
    public <E> List<Val> visit(VProd<E> v) {
        return v.getFactors().accept(this);
    }

    @Override
    public <E, T extends Shape> List<Val> visit(VSub<E, T> v) {
        return append(v.getLeft().accept(this),v.getRight().accept(this));
    }

    @Override
    public <E> List<Val> visit(VSum<E> v) {
        return v.getAddends().accept(this);
    }

    @Override
    public <E, T extends Shape> List<Val> visit(Val<E, T> v) {
        List<Val> l = new ArrayList<>();
        l.add(v);
        return l;
    }

    @Override
    public <E, T extends Shape> List<Val> visit(VConst<E,T> v) {
        return List.of(); //empty list.
    }

    @Override
    public List<Val> visit(FloatScalarOp v) {
        return v.getChild().accept(this);
    }

    @Override
    public List<Val> visit(DoubleScalarOp v) {
        return v.getChild().accept(this);
    }

    @Override
    public List<Val> visit(IntScalarOp v) {
        return v.getChild().accept(this);
    }

    @Override
    public List<Val> visit(LongScalarOp v) {
        return v.getChild().accept(this);
    }

    @Override
    public List<Val> visit(FloatScalarBinOp v) {
        return append(v.getLeft().accept(this),v.getRight().accept(this));
    }

    @Override
    public List<Val> visit(DoubleScalarBinOp v) {
        return append(v.getLeft().accept(this),v.getRight().accept(this));
    }

    @Override
    public List<Val> visit(IntScalarBinOp v) {
        return append(v.getLeft().accept(this),v.getRight().accept(this));
    }

    @Override
    public List<Val> visit(LongScalarBinOp v) {
        return append(v.getLeft().accept(this),v.getRight().accept(this));
    }


        @Override
    public <E, T extends Shape> List<Val> visit(VBroadcast<E, T> v) {
        return v.getChild().accept(this);
    }

    @Override
    public <E, T extends Shape> List<Val> visit(VMask<E, T> v) {
        return append(append(v.getMask().accept(this),v.getThn().accept(this)),v.getEls().accept(this));
    }

    @Override
    public <E, S extends Shape> List<Val> visit(VBinBExp<E, S> v) {
        return append(v.getLeft().accept(this),v.getRight().accept(this));
    }

    @Override
    public <E, S extends Shape> List<Val> visit(IndexedVal<E, S> v) {
        return append(v.getIxExp().accept(this),v.getExp().accept(this));
    }

    @Override
    public <E, T extends Shape> List<Val> visit(ITE<E, T> v) {
        List<Val> i,thn,els;
        i = v.getTest().accept(this);
        thn = v.getThen().accept(this);
        els = v.getElse().accept(this);

        return append(i,append(thn,els));
    }


    private <R> List<R> append(List<R> left, List<R> right){
        List<R> l = new ArrayList<>();
        l.addAll(left);
        l.addAll(right);
        return l;
    }
}
