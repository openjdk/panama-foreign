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
 * AST nodes represent future computation.  {@code MatrixNode}s are the
 * superclass of all expression nodes whose evaluation results in a matrix
 * value.  ASTs may be "optimized" to reduce temporary creation or obtain better
 * locality (along cache lines).  AST nodes are responsible for subdividing
 * their work for evaluation.
 *
 * @param <T> Element type of the result.
 * @param <U> Element type of first matrix-typed operand.
 */
public abstract class MatrixNode<T,U> implements A2Expr<T> {
    public final A2Expr<U> x;
    public MatrixNode(A2Expr<U> x) {
        this.x = x;
    }
    /**
     * GetValue is only intended for implementation by expressions with
     * O(1) computation cost; temporaries, transposes, and leaf values.
     *
     * @return
     */
    @Override
    public Matrix<T> getValue() {
        // Default behavior for all complex expressions except Temporary and Transpose
        throw new Error("Malformed tree, getValue applied to bare expression");
    }
}
