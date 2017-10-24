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

import java.util.concurrent.RecursiveAction;
import javax.arrays.v2.A0Expr;
import javax.arrays.v2.A1Expr;
import javax.arrays.v2.A2Expr;
import javax.arrays.v2.Array;
import javax.arrays.v2.Scalar;
import javax.arrays.v2.SubArray;
import javax.arrays.v2.ops.ProductOp;

/**
 * AST node for the product of a matrix with a vector (an array).
 *
 * @param <T> element type of the result matrix
 * @param <U> element type of the left (first) operand array
 * @param <V> element type of the right (second) operand array
 */
public class ArrayVectorProduct<T,U,V> extends Scalar<T> {
    public final ProductOp<T,U,V> op;
    public final A1Expr<U> x;
    public final A1Expr<V> y;

    public ArrayVectorProduct(ProductOp<T,U,V> op, A1Expr<U> x, A1Expr<V> y) {
        this.x = x;
        this.op = op;
        this.y = y;
        if (x.length() != y.length()) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Class<T> elementType() {
        return op().resultType();
    }

    /**
     * @return
     */
    @Override
    public T getValue() {
        evalInto(this);
        return get();
    }

    @Override
    public void setInto(Scalar<T> target) {
        A0Expr<T> source = simplify(target);
        source.evalInto(target);
    }

    @Override
    public A0Expr<T> simplify(Scalar<T> target, Simplifier simplifier) {
        return simplifier.simplifyArrayVectorProduct(target, this);
    }

    public ProductOp<T,U,V>  op() {
        return op;
    }

    @Override
    public void evalInto(Scalar<T> target) {
        // Recursive subdivision of x,y and target,
        // eventually performing target = op(x,y).

        long n_rows1 = x.length();
        long n_rows2 = y.length();

        if (n_rows1 != n_rows2 ) {
            throw new IllegalArgumentException("Arrays must have conforming sizes");
        }
        set(setRecursive(x.getValue(), y.getValue(), A2Expr.productBlockSize()));
        target.set(get());
    }


    /**
     * Performs this gets operand1 X operand2 with the row-column inner product
     * defined by times and plus, perhaps with recursive subdivision, perhaps
     * with fork/join parallelism. Recursive subevaluations are applied to
     * results of "subArray" to create an opportunity for specialization
     * (simplification) of array substructure.
     *
     * It may be profitable to override this method to obtain customized
     * behavior for special (e.g., distributed) array representations.
     *
     * @param operand1
     * @param operand2
     * @param blocksize
     */
    public T setRecursive(Array<U> operand1, Array<V> operand2, int blocksize) {
        long n_rows = operand1.length();
        if (n_rows <= blocksize) {
                // kernel iteration.
                return setSubArray(operand1, operand2, 0, n_rows);
        } else {
            long m0 = n_rows >> 1;
            long m1 = operand1.middle();
            long m2 = operand2.middle();
            m0 = A2Expr.firstNotEqual(m0, m1, m2);

            ElementWiseSetProductAction<T,U,V> right = new ElementWiseSetProductAction<>(
                    this,
                    operand1.subArray(m0, n_rows),
                    operand2.subArray(m0, n_rows), blocksize);
            right.fork();
            ElementWiseSetProductAction<T,U,V> left = new ElementWiseSetProductAction<>(
                    this,
                    operand1.subArray(0, m0),
                    operand2.subArray(0, m0), blocksize);
            left.compute();
            right.join();
            return op.plus(left.value(), right.value());
        }
    }

    /**
     * Performs 'this gets opr1 X opr2' for equally-offset this, opr1, opr2
     * submatrices, multiplication defined by element operations times and plus.
     *
     * Does this by removing intervening submatrices, deriving additional
     * offsets, and delegating to a not-equally-offset overloading of getsLeaf.
     *
     * @param operand1
     * @param operand2
     * @param i_lo
     * @param i_hi
     * @return
     */
    public T setSubArray(Array<U> operand1, Array<V> operand2,
            long i_lo, long i_hi) {
        long o2_off = 0;
        if (operand1.length() != operand2.length()) {
            throw new IllegalArgumentException("Operand dimensions incompatible for array-vector product, "
                    + operand1.length() + " and " + operand2.length());
        }

        if (operand1 instanceof SubArray) {
            SubArray<U> sub_1 = (SubArray<U>) operand1;
            long delta = sub_1.begin();
            i_lo += delta;
            i_hi += delta;
            o2_off -= delta;
            operand1 = sub_1.parent();
        }
        if (operand2 instanceof SubArray) {
            SubArray<V> sub_2 = (SubArray<V>) operand2;
            o2_off += sub_2.begin();
            operand2 = sub_2.parent();
        }
        return op.setOffsetSubArray(operand1, operand2, i_lo, i_hi, o2_off);
    }



    static public class ElementWiseSetProductAction<T,U,V> extends RecursiveAction {
        private static final long serialVersionUID = 0x20bcf0121e519bbcL;

        protected T target;
        protected final Array<U> operand1;
        protected final Array<V> operand2;
        protected final ArrayVectorProduct<T,U,V> node;
        protected final int blocksize;

        public ElementWiseSetProductAction(ArrayVectorProduct<T,U,V> node, Array<U> operand1, Array<V> operand2, int blocksize) {
            this.operand1 = operand1;
            this.operand2 = operand2;
            this.blocksize = blocksize;
            this.node = node;
        }

        @Override
        protected void compute() {
            target = node.setRecursive(operand1, operand2, blocksize);
        }

        T value() {
            return target;
        }
    }
}
