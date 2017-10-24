/*
 *  Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.  Oracle designates this
 *  particular file as subject to the "Classpath" exception as provided
 *  by Oracle in the LICENSE file that accompanied this code.
 *
 *  This code is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  version 2 for more details (a copy is included in the LICENSE file that
 *  accompanied this code).
 *
 *  You should have received a copy of the GNU General Public License version
 *  2 along with this work; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *  Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 */
package javax.arrays.v2.nodes;

import javax.arrays.v2.Matrix;
import javax.arrays.v2.A2Expr;

/**
 * The superclass of matrix aggregate operations, for example, transposed view.
 * (Ought this be the superclass of temporaries?)
 *
 * @param <T> Element type of operand and result.
 */
abstract public class MatrixAggregate<T> extends MatrixNode<T,T> {

    public MatrixAggregate(A2Expr<T> x) {
        super(x);
    }

    @Override
    public int preferredMajorVote() {
        return x.preferredMajorVote();
    }

    @Override
    public long length(int d) {
        return x.length(d);
    }

    @Override
    public Class<T> elementType() {
        return x.elementType();
    }

    @Override
    public void evalInto(Matrix<T> target) {
        x.evalInto(target);
    }

    @Override
    public Matrix<T> getValue() {
        Matrix<T> y = x.getValue();
        return y;
    }

    @Override
    public A2Expr<T> tempifyAsNecessary(Simplifier simplifier, int desiredMajorOrder) {
        A2Expr<T> y = x.tempifyAsNecessary(simplifier, desiredMajorOrder);
        return y;
    }

}
