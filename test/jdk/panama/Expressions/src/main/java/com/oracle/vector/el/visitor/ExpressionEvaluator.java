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
package com.oracle.vector.el.visitor;

import com.oracle.vector.el.Shape;
import com.oracle.vector.el.Val;
import com.oracle.vector.el.expression.*;
import com.oracle.vector.el.expression.bexp.VBinBExp;
import com.oracle.vector.el.expression.IndexedVal;

public interface ExpressionEvaluator<R> {

    <E,S extends Shape> R  visit(VAdd<E,S> v);
    <E,S extends Shape> R  visit(VDiv<E,S> v);
    <E,S extends Shape> R  visit(VMul<E,S> v);
    <E>                 R  visit(VProd<E> v);
    <E,S extends Shape> R  visit(VSub<E,S> v);
    <E>                 R  visit(VSum<E> v);
    <E,S extends Shape> R  visit(Val<E,S> v);
    <E,S extends Shape> R  visit(ITE<E,S> v);
    <E,S extends Shape> R  visit(VConst<E,S> v);
                        R  visit(FloatScalarOp v);
                        R  visit(DoubleScalarOp v);
                        R  visit(IntScalarOp v);
                        R  visit(LongScalarOp v);
                        R  visit(FloatScalarBinOp v);
                        R  visit(DoubleScalarBinOp v);
                        R  visit(IntScalarBinOp v);
                        R  visit(LongScalarBinOp v);
    <E,S extends Shape> R  visit(VBroadcast<E,S> v);
    <E,S extends Shape> R  visit(VMask<E,S> v);
    <E,S extends Shape> R  visit(VBinBExp<E,S> v);
    <E,S extends Shape> R  visit(IndexedVal<E,S> v);
    //<E,S extends Shape> R  visit(IndexableVal<E,S> v);
}
