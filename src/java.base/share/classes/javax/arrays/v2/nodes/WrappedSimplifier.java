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
 * Proof-of-concept for a replacement simplifier.
 * See also AnyOp.D_WRAPPED_SIMPLIFIER
 */
public class WrappedSimplifier extends Simplifier {

    @Override
    public <T> A2Expr<T> simplifyMatrix(Matrix<T> target, Matrix<T> source) {
        System.out.println("simplifyMatrix");
        return super.simplifyMatrix(target, source);
    }

    @Override
    public <T> A2Expr<T> simplifyEltWise4(Matrix<T> target, MatrixAssociative4ary<T> source) throws Error {
        System.out.println("simplifyEltWise4");
        return super.simplifyEltWise4(target, source);
    }

    @Override
    public <T> A2Expr<T> simplifyEltWise3(Matrix<T> target, MatrixAssociative3ary<T> source) throws Error {
        System.out.println("simplifyEltWise3");
        return super.simplifyEltWise3(target, source);
    }

    @Override
    public <T> A2Expr<T> simplifyEltWise2(Matrix<T> target, MatrixAssociativeBinary<T> source) throws Error {
        System.out.println("simplifyEltWise2");
        return super.simplifyEltWise2(target, source);
    }

    @Override
    public <T, U, V> A2Expr<T> simplifyScalarRight(Matrix<T> target, MatScalarRight<T, U, V> source) throws Error {
        System.out.println("simplifyScalarRight");
        return super.simplifyScalarRight(target, source);
    }

    @Override
    public <T, U, V> A2Expr<T> simplifyScalarLeft(Matrix<T> target, MatScalarLeft<T, U, V> source) throws Error {
        System.out.println("simplifyScalarLeft");
        return super.simplifyScalarLeft(target, source);
    }

    @Override
    public <T, U, V> A2Expr<T> simplifyEltWise2NA(Matrix<T> target, MatEltBinary<T, U, V> source) throws Error {
        System.out.println("simplifyEltWise2NA");
        return super.simplifyEltWise2NA(target, source);
    }

    @Override
    public <T, U> A2Expr<T> simplifyTranspose(Matrix<T> target, MatTranspose<T> source) {
        System.out.println("simplifyTranspose");
        return super.simplifyTranspose(target, source);
    }

    @Override
    public <T, U> A2Expr<T> simplifyEltWise1(Matrix<T> target, MatEltUnary<T, U> source) {
        System.out.println("simplifyEltWise1");
        return super.simplifyEltWise1(target, source);
    }

}
