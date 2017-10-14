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

import javax.arrays.tbd.Complex;
import javax.arrays.tbd.DComplex;
import javax.arrays.v2.A0Expr;
import javax.arrays.v2.A1Expr;
import javax.arrays.v2.Matrix;
import javax.arrays.v2.A2Expr;
import static javax.arrays.v2.A2Expr.COL_DIM;
import static javax.arrays.v2.A2Expr.NO_DIM;
import static javax.arrays.v2.A2Expr.ROW_DIM;
import static javax.arrays.v2.A2Expr.transposeDim;
import javax.arrays.v2.Array;
import javax.arrays.v2.ColumnMatrix;
import javax.arrays.v2.DoubleArray;
import javax.arrays.v2.LongArray;
import javax.arrays.v2.RowMatrix;
import javax.arrays.v2.Scalar;
import javax.arrays.v2.ops.AssociativeOp;
import javax.arrays.v2.ops.BinaryOp;
import javax.arrays.v2.ops.UnaryOp;
import javax.arrays.v2.ops.ProductOp;

/**
 * This is a proof-of-concept, not the acme of AST simplifiers.
 * Someone who adds new operators with interesting properties might want
 * to extend this class.
 */
public class Simplifier {

    public static <T> int combinedMajorVote(Matrix<T> target, A2Expr<T> source) {
        return target.preferredMajorVote() + source.preferredMajorVote();
    }

    // This is probably an unnecessary level of indirection, but it for now
    // it is here for historical purposes because editing it out would be
    // a pain.
    public <T> A2Expr<T> tempifyAsNecessary(A2Expr<T> x, int desiredMajorOrder) {
        return x.tempifyAsNecessary(this, desiredMajorOrder);
    }

    // And this is here to be consistent with the other dubious decision.
    public <T> A1Expr<T> tempifyAsNecessary(A1Expr<T> x) {
        return x.tempifyAsNecessary(this);
    }

    @SuppressWarnings("unchecked")
    public <T> Matrix<T> makeMatrix(A2Expr<T> anyNode, int desiredMajorOrder) {
        Class<T> eltClass = anyNode.elementType();
        // need to fix the int lengths here.
        int rows = (int) anyNode.length(ROW_DIM);
        int columns = (int) anyNode.length(COL_DIM);
        if (desiredMajorOrder == NO_DIM || desiredMajorOrder == ROW_DIM) {
            if (eltClass == Double.class) {
                return (Matrix<T>) new RowMatrix.D(rows, columns);
            } else if (eltClass == Float.class) {
                return (Matrix<T>) new RowMatrix.F(rows, columns);
            } else if (eltClass == DComplex.class) {
                return (Matrix<T>) new RowMatrix.DX(rows, columns);
            } else if (eltClass == Complex.class) {
                return (Matrix<T>) new RowMatrix.CXinFF(rows, columns);
            } else if (eltClass == Long.class) {
                return (Matrix<T>) new RowMatrix.L(rows, columns);
            } else if (eltClass == Integer.class) {
                return (Matrix<T>) new RowMatrix.I(rows, columns);
            } else if (eltClass == Boolean.class) {

            } else if (eltClass == Byte.class) {
                return (Matrix<T>) new RowMatrix.B(rows, columns);
            } else if (eltClass == Short.class) {
                return (Matrix<T>) new RowMatrix.S(rows, columns);
            } else if (eltClass == Character.class) {

            }
            // Need to generalize to larger...
            return new RowMatrix<>(eltClass, rows, columns);

        } else {
            if (eltClass == Double.class) {
                return (Matrix<T>) new ColumnMatrix.D(rows, columns);
            } else if (eltClass == Float.class) {
                return (Matrix<T>) new ColumnMatrix.F(rows, columns);
            } else if (eltClass == DComplex.class) {
                return (Matrix<T>) new ColumnMatrix.DX(rows, columns);
            } else if (eltClass == Complex.class) {
                return (Matrix<T>) new ColumnMatrix.CXinFF(rows, columns);
            } else if (eltClass == Long.class) {
                return (Matrix<T>) new ColumnMatrix.L(rows, columns);
            } else if (eltClass == Integer.class) {
                return (Matrix<T>) new ColumnMatrix.I(rows, columns);
            } else if (eltClass == Boolean.class) {

            } else if (eltClass == Byte.class) {
                return (Matrix<T>) new ColumnMatrix.B(rows, columns);
            } else if (eltClass == Short.class) {
                return (Matrix<T>) new ColumnMatrix.S(rows, columns);
            } else if (eltClass == Character.class) {

            }
            // Need to generalize to larger...
            return new ColumnMatrix<>(eltClass, rows, columns);
        }
    }

   @SuppressWarnings("unchecked")
   public <T> Array<T> makeArray(A1Expr<T> anyNode) {
        Class<T> eltClass = anyNode.elementType();
        // need to fix the int lengths here.
        long n = anyNode.length();
            if (eltClass == Double.class) {
                return (Array<T>) DoubleArray.make(n);
            } else if (eltClass == Float.class) {
            } else if (eltClass == DComplex.class) {
            } else if (eltClass == Complex.class) {
            } else if (eltClass == Long.class) {
                return (Array<T>) LongArray.make(n);
            } else if (eltClass == Integer.class) {
            } else if (eltClass == Boolean.class) {

            } else if (eltClass == Byte.class) {
            } else if (eltClass == Short.class) {
            } else if (eltClass == Character.class) {

            }
            // Need to generalize to larger...
            return Array.allocateNew(eltClass, n);

    }


    /**
     * This exists only to allow hooking and possible reorganization of inputs.
     *
     * @param <T>
     * @param target
     * @param source
     * @return
     */
    public <T> A2Expr<T> simplifyMatrix(Matrix<T> target, Matrix<T> source) {
        return source;
    }

    public <T> A1Expr<T> simplifyArray(Array<T> target, Array<T> source) {
        return source;
    }

    public <T> A0Expr<T> simplifyScalar(Scalar<T> target, Scalar<T> source) {
        return source;
    }

    public <T,U> A2Expr<T> simplifyEltWise1(Matrix<T> target, MatEltUnary<T,U> source) {
        int desiredMajorOrder = combinedMajorVote(target, source);
        A2Expr<U> _x = source.x;
        A2Expr<U> x = _x;
        UnaryOp<T,U> op = source.op;
        x = tempifyAsNecessary(x, desiredMajorOrder);
        return (x == _x) ? source : new MatEltUnary<>(op, x);
    }

    public <T,U> A1Expr<T> simplifyArrNode1(Array<T> target, ArrayUnary<T,U> source) {
        A1Expr<U> _x = source.x;
        A1Expr<U> x = _x;
        UnaryOp<T,U> op = source.op;
        x = tempifyAsNecessary(x);
        return (x == _x) ? source : new ArrayUnary<>(op, x);
    }

    public <T> A1Expr<T> simplifyArrNode2(Array<T> target, ArrayAssociativeBinary<T> source) {
        A1Expr<T> _x = source.x;
        A1Expr<T> x = _x;
        A1Expr<T> _y = source.y;
        A1Expr<T> y = _y;
        AssociativeOp<T> op = source.op;
        x = tempifyAsNecessary(x);
        y = tempifyAsNecessary(y);
        return (x == _x && y == _y) ? source : new ArrayAssociativeBinary<>(op, x, y);
    }

    public <T, U, V> A1Expr<T> simplifyArrNode2NA(Array<T> target, ArrayBinary<T, U, V> source) {
        A1Expr<U> _x = source.x;
        A1Expr<U> x = _x;
        A1Expr<V> _y = source.y;
        A1Expr<V> y = _y;
        BinaryOp<T,U,V> op = source.op;
        x = tempifyAsNecessary(x);
        y = tempifyAsNecessary(y);
        return (x == _x && y == _y) ? source : new ArrayBinary<>(op, x, y);
    }

    public <T,U,V> A1Expr<T> simplifyArrScalarLeft(Array<T> target, ArrScalarLeft<T, U, V> source) {
        A1Expr<V> _x = source.x;
        A1Expr<V> x = _x;
        A0Expr<U> a = source.a;
        BinaryOp<T,U,V> op = source.op;
        x = tempifyAsNecessary(x);
        // a = tempifyAsNecessary(x); // No storage allocation required.
        return (x == _x) ? source : new ArrScalarLeft<>(op, a, x);
    }

    public <T,U> A2Expr<T> simplifyTranspose(Matrix<T> target, MatTranspose<T> source) {
        int desiredMajorOrder = transposeDim(target.preferredMajorVote()) + source.preferredMajorVote();
        A2Expr<T> _x = source.x;
        A2Expr<T> x = _x;
        x = tempifyAsNecessary(x, desiredMajorOrder);
        return (x == _x) ? source : new MatTranspose<>(x);
    }

    public <T,U,V> A2Expr<T> simplifyRowColNode(Matrix<T> target, MatrixProduct<T,U,V> source) throws Error {
        int desiredMajorOrder = combinedMajorVote(target, source);
        A2Expr<U> _x = source.x;
        A2Expr<V> _y = source.y;
        A2Expr<U> x = _x;
        A2Expr<V> y = _y;
        ProductOp<T,U,V> op = source.op();

        boolean x_is_value = x instanceof Matrix;
        boolean y_is_value = y instanceof Matrix;
        if (x_is_value && y_is_value) {
            return source;
        }
        // Otherwise insert temporaries as necessary
        x = tempifyAsNecessary(x, 64*ROW_DIM);
        y = tempifyAsNecessary(y, 64*COL_DIM);

        // Want Simplifier to be idempotent by default.
        return (x == _x && y == _y) ? source : new MatrixProduct<>(op, x, y);
    }


     public <T,U,V> A1Expr<T> simplifyMatrixVectorProduct(Array<T> target, MatrixVectorProduct<T,U,V> source) throws Error {
        A2Expr<U> _x = source.x;
        A1Expr<V> _y = source.y;
        A2Expr<U> x = _x;
        A1Expr<V> y = _y;
        ProductOp<T,U,V> op = source.op();

        boolean x_is_value = x instanceof Matrix;
        boolean y_is_value = y instanceof Array;
        if (x_is_value && y_is_value) {
            return source;
        }
        // Otherwise insert temporaries as necessary
        x = tempifyAsNecessary(x, 64*ROW_DIM);
        y = tempifyAsNecessary(y);

        // Want Simplifier to be idempotent by default.
        return (x == _x && y == _y) ? source : new MatrixVectorProduct<>(op, x, y);
    }

     public <T,U,V> A0Expr<T> simplifyArrayVectorProduct(Scalar<T> target, ArrayVectorProduct<T,U,V> source) throws Error {
        A1Expr<U> _x = source.x;
        A1Expr<V> _y = source.y;
        A1Expr<U> x = _x;
        A1Expr<V> y = _y;
        ProductOp<T,U,V> op = source.op();

        boolean x_is_value = x instanceof Array;
        boolean y_is_value = y instanceof Array;
        if (x_is_value && y_is_value) {
            return source;
        }
        // Otherwise insert temporaries as necessary
        x = tempifyAsNecessary(x);
        y = tempifyAsNecessary(y);

        // Want Simplifier to be idempotent by default.
        return (x == _x && y == _y) ? source : new ArrayVectorProduct<>(op, x, y);
    }

    public <T,U,V> A2Expr<T> simplifyEltWise2NA(Matrix<T> target, MatEltBinary<T,U,V> source) throws Error {
        int desiredMajorOrder = combinedMajorVote(target, source);
        A2Expr<U> _x = source.x;
        A2Expr<V> _y = source.y;
        A2Expr<U> x = _x;
        A2Expr<V> y = _y;
        BinaryOp<T,U,V> op = source.op();

        boolean x_is_value = x instanceof Matrix;
        boolean y_is_value = y instanceof Matrix;
        if (x_is_value && y_is_value) {
            return source;
        }
        // Otherwise insert temporaries as necessary
        x = tempifyAsNecessary(x, desiredMajorOrder);
        y = tempifyAsNecessary(y, desiredMajorOrder);

        // Want Simplifier to be idempotent by default.
        return (x == _x && y == _y) ? source : new MatEltBinary<>(op, x, y);
    }

    public <T,U,V> A2Expr<T> simplifyScalarLeft(Matrix<T> target, MatScalarLeft<T,U,V> source) throws Error {
        int desiredMajorOrder = combinedMajorVote(target, source);
        A2Expr<V> _x = source.x;
        U a = source.a;
        A2Expr<V> x = _x;
        BinaryOp<T,U,V> op = source.op();

        boolean x_is_value = x instanceof Matrix;
        if (x_is_value) {
            return source;
        }
        // Otherwise insert temporaries as necessary
        x = tempifyAsNecessary(x, desiredMajorOrder);

        // Want Simplifier to be idempotent by default.
        return (x == _x) ? source : new MatScalarLeft<>(op, a, x);
    }

    public <T,U,V> A2Expr<T> simplifyScalarRight(Matrix<T> target, MatScalarRight<T,U,V> source) throws Error {
        int desiredMajorOrder = combinedMajorVote(target, source);
        A2Expr<U> _x = source.x;
        V a = source.a;
        A2Expr<U> x = _x;
        BinaryOp<T,U,V> op = source.op();

        boolean x_is_value = x instanceof Matrix;
        if (x_is_value) {
            return source;
        }
        // Otherwise insert temporaries as necessary
        x = tempifyAsNecessary(x, desiredMajorOrder);

        // Want Simplifier to be idempotent by default.
        return (x == _x) ? source : new MatScalarRight<>(op, x, a);
    }

    public <T> A2Expr<T> simplifyEltWise2(Matrix<T> target, MatrixAssociativeBinary<T> source) throws Error {
        int desiredMajorOrder = combinedMajorVote(target, source);
        A2Expr<T> _x = source.x;
        A2Expr<T> _y = source.y;
        A2Expr<T> x = _x;
        A2Expr<T> y = _y;
        AssociativeOp<T> op = source.op();

        boolean x_is_value = x instanceof Matrix;
        boolean y_is_value = y instanceof Matrix;
        if (x_is_value && y_is_value) {
            return source;
        }

        // Attempt to flatten tree to avoid temporaries.
        // This is the killer app for pattern matchiung, too bad we lack it.
        MatrixAssociative<T> x_ewn = x instanceof MatrixAssociative ? (MatrixAssociative<T>) x : null;
        MatrixAssociative<T> y_ewn = y instanceof MatrixAssociative ? (MatrixAssociative<T>) y : null;
        AssociativeOp<T> x_op = x_ewn != null ? x_ewn.op() : null;
        AssociativeOp<T> y_op = y_ewn != null ? y_ewn.op() : null;

        if (x_op == op && !(x_ewn instanceof MatrixAssociative4ary)) {
            // NB op is not null, hence x_op is not null, hence x_ewn is not null
            // Similar logic applies when y_op = op.
            // (Netbeans complains about the possibility of a null deref when
            // none exists.

            // (+ (+ a b ...) y)
            // can group x2 with root or y2, can group x3 with root
            if (y_op == op && x_ewn instanceof MatrixAssociativeBinary) {
                MatrixAssociativeBinary<T> x2 = (MatrixAssociativeBinary<T>) x_ewn;
                if (y instanceof MatrixAssociativeBinary) {
                    // (+ (+ a b) (+ c d)) -> (+ a b c d)
                    MatrixAssociativeBinary<T> y2 = (MatrixAssociativeBinary<T>) y_ewn;
                    return new MatrixAssociative4ary<>(
                            op,
                            tempifyAsNecessary(x2.x, desiredMajorOrder),
                            tempifyAsNecessary(x2.y, desiredMajorOrder),
                            tempifyAsNecessary(y2.x, desiredMajorOrder),
                            tempifyAsNecessary(y2.y, desiredMajorOrder)
                    );
                } else {
                    // (+ (+ a b) y) -> (+ a b y)
                    return new MatrixAssociative3ary<>(
                            op,
                            tempifyAsNecessary(x2.x, desiredMajorOrder),
                            tempifyAsNecessary(x2.y, desiredMajorOrder),
                            tempifyAsNecessary(y, desiredMajorOrder)
                    );
                }
            } else {
                if (x_ewn instanceof MatrixAssociativeBinary) {
                    // (+ (+ a b) y) -> (+ a b y)
                    MatrixAssociativeBinary<T> x2 = (MatrixAssociativeBinary<T>) x_ewn;
                    return new MatrixAssociative3ary<>(
                            op,
                            tempifyAsNecessary(x2.x, desiredMajorOrder),
                            tempifyAsNecessary(x2.y, desiredMajorOrder),
                            tempifyAsNecessary(y, desiredMajorOrder)
                    );
                } else if (x_ewn instanceof MatrixAssociative3ary) {
                    // (+ (+ a b c) y) -> (+ a b c y)
                    MatrixAssociative3ary<T> x3 = (MatrixAssociative3ary<T>) x_ewn;
                    return new MatrixAssociative4ary<>(
                            op,
                            tempifyAsNecessary(x3.x, desiredMajorOrder),
                            tempifyAsNecessary(x3.y, desiredMajorOrder),
                            tempifyAsNecessary(x3.z, desiredMajorOrder),
                            tempifyAsNecessary(y, desiredMajorOrder)
                    );
                } else {
                    throw new Error("Should not reach here, x = " + x);
                }
            }
        } else if (y_op == op && !(y_ewn instanceof MatrixAssociative4ary)) {
            // can group root with y2 or y3
            if (y_ewn instanceof MatrixAssociativeBinary) {
                // (+ x (+ a b )) -> (+ x a b)
                MatrixAssociativeBinary<T> y2 = (MatrixAssociativeBinary<T>) y_ewn;
                return new MatrixAssociative3ary<>(
                        op,
                        tempifyAsNecessary(x, desiredMajorOrder),
                        tempifyAsNecessary(y2.x, desiredMajorOrder),
                        tempifyAsNecessary(y2.y, desiredMajorOrder)
                );
            } else if (y instanceof MatrixAssociative3ary) {
                // (+ x (+ a b c)) -> (+ x a b c)
                MatrixAssociative3ary<T> y3 = (MatrixAssociative3ary<T>) y_ewn;
                return new MatrixAssociative4ary<>(
                        op,
                        tempifyAsNecessary(x, desiredMajorOrder),
                        tempifyAsNecessary(y3.x, desiredMajorOrder),
                        tempifyAsNecessary(y3.y, desiredMajorOrder),
                        tempifyAsNecessary(y3.z, desiredMajorOrder)
                );
            } else {
                throw new Error("Should not reach here, y = " + y);
            }
        }
        // Otherwise insert temporaries as necessary
        x = tempifyAsNecessary(x, desiredMajorOrder);
        y = tempifyAsNecessary(y, desiredMajorOrder);
        // Want Simplifier to be idempotent by default.
        return (x == _x && y == _y) ? source : new MatrixAssociativeBinary<>(op, x, y);
    }

    public <T> A2Expr<T> simplifyEltWise3(Matrix<T> target, MatrixAssociative3ary<T> source) throws Error {
        // TODO Could fold any subtree with same op into ternary.
        int desiredMajorOrder = combinedMajorVote(target, source);
        A2Expr<T> _x = source.x;
        A2Expr<T> _y = source.y;
        A2Expr<T> _z = source.z;
        A2Expr<T> x = _x;
        A2Expr<T> y = _y;
        A2Expr<T> z = _z;
        AssociativeOp<T> op = source.op();

        boolean x_is_value = x instanceof Matrix;
        boolean y_is_value = y instanceof Matrix;
        boolean z_is_value = z instanceof Matrix;
        if (x_is_value && y_is_value && z_is_value) {
            return source;
        }
        // Otherwise insert temporaries as necessary
        x = tempifyAsNecessary(x, desiredMajorOrder);
        y = tempifyAsNecessary(y, desiredMajorOrder);
        z = tempifyAsNecessary(z, desiredMajorOrder);

        // Want Simplifier to be idempotent by default.
        return (x == _x && y == _y && z == _z) ? source : new MatrixAssociative3ary<>(op, x, y, z);
    }

    public <T> A2Expr<T> simplifyEltWise4(Matrix<T> target, MatrixAssociative4ary<T> source) throws Error {
        int desiredMajorOrder = combinedMajorVote(target, source);
        A2Expr<T> _w = source.w;
        A2Expr<T> _x = source.x;
        A2Expr<T> _y = source.y;
        A2Expr<T> _z = source.z;
        A2Expr<T> w = _w;
        A2Expr<T> x = _x;
        A2Expr<T> y = _y;
        A2Expr<T> z = _z;
        AssociativeOp<T> op = source.op();

        boolean w_is_value = w instanceof Matrix;
        boolean x_is_value = x instanceof Matrix;
        boolean y_is_value = y instanceof Matrix;
        boolean z_is_value = z instanceof Matrix;
        if (w_is_value && x_is_value && y_is_value && z_is_value) {
            return source;
        }
        // Otherwise insert temporaries as necessary
        w = tempifyAsNecessary(w, desiredMajorOrder);
        x = tempifyAsNecessary(x, desiredMajorOrder);
        y = tempifyAsNecessary(y, desiredMajorOrder);
        z = tempifyAsNecessary(z, desiredMajorOrder);

        // Want Simplifier to be idempotent by default.
        return (w == _w && x == _x && y == _y && z == _z) ? source : new MatrixAssociative4ary<>(op, w, x, y, z);
    }
}
