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
 * MatrixTemporary AST nodes.  These are inserted into ASTs during
 * simplification to ensure that subtree evaluations have a destination
 * allocated.

* @param <T> element type of expression and result
 */
public class MatrixTemporary<T> extends MatrixNode<T,T> {
    // Perhaps this should be lazily allocated...
    public final Matrix<T> temporary;

    public MatrixTemporary(Matrix<T> temporary, A2Expr<T> x) {
        super(x);
        this.temporary = temporary;
    }

    @Override
    public String toString() {
        return "(TEMP " + x + ")";
    }

    @Override
    public Class<T> elementType() {
        return temporary.elementType();
    }

    @Override
    public int preferredMajorVote() {
        return temporary.preferredMajorVote();
    }

    @Override
    public long length(int d) {
        return x.length(d);
    }

    @Override
    public void setInto(Matrix<T> target) {
        throw new Error("Did not expect to call Temporary.setInto");
    }

    @Override
    public void evalInto(Matrix<T> target) {
        throw new Error("Did not expect to call Temporary.evalInto");
    }

    @Override
    public A2Expr<T> simplify(Matrix<T> target, Simplifier simplifier) {
        return this; // for now.
    }

    @Override
    public A2Expr<T> tempifyAsNecessary(Simplifier simplifier, int desiredMajorOrder) {
        return this;
    }

    @Override
    public Matrix<T> getValue() {
        x.evalInto(temporary);
        return temporary;
    }

}
