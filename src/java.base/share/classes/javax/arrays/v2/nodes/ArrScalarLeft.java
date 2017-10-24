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

import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import javax.arrays.v2.A0Expr;
import javax.arrays.v2.Array;
import javax.arrays.v2.A1Expr;
import javax.arrays.v2.A2Expr;
import javax.arrays.v2.SubArray;
import javax.arrays.v2.ops.BinaryOp;

/**
 * An array-valued AST node for a scalar left operation,
 * where target := scalar elementwise-OP result .
 *
 * @param <T> Element type of array (linear algebra, vector) result.
 * @param <U> Type of scalar left operand.
 * @param <V> Element type of array (linear algebra, vector) right operand.
 */
public class ArrScalarLeft<T, U, V> extends ArrayWithOp<T, V> {
    public final A0Expr<U> a;
    public final BinaryOp<T, U, V> op;

    public ArrScalarLeft(BinaryOp<T, U, V> op, A0Expr<U> a, A1Expr<V> x) {
        super(x);
        this.a = a;
        this.op = op;
    }

    @Override
    public BinaryOp<T, U, V> op() {
        return op;
    }

    @Override
    public String toString() {
        return "(" + op + " " + a + " " + x + ")";
    }

    @Override
    public A1Expr<T> simplify(Array<T> target, Simplifier simplifier) {
        return simplifier.simplifyArrScalarLeft(target, this);
    }

    @Override
    public void evalInto(Array<T> target) {
        long n = target.length();
        if (x.length() != n ) {
            throw new IllegalArgumentException("Matrices must have conforming sizes");
        }
        setRecursive(target, a.getValue(), x.getValue(), A2Expr.elementWiseBlockSize());
    }

      /**
     * Performs this gets operand1 op operand2, perhaps with recursive
     * subdivision, perhaps with fork/join parallelism. Recursive subevaluations
     * are applied to results of "subArray" to create an opportunity for
     * specialization (simplification) of array substructure.
     *
     * It may be profitable to override this method to obtain customized
     * behavior for special (e.g., distributed) array representations.
     *
     * @param operand1
     * @param op
     * @param operand2
     * @param blocksize
     */
     void setRecursive(Array<T> target, U operand1, Array<V> operand2, int blocksize) {
        long n = target.length();
            if (n <= blocksize) {
                // kernel iteration.
                setSubArray(target, operand1, operand2, 0, n);
            } else {
                // subdivide columns
                long m0 = n >> 1;
                long m1 = target.middle();
                long m2 = operand2.middle();
                m0 = A2Expr.firstNotEqual(m0, m1, m2);

                ArrScalarLeftAction<T,U,V> right = new ArrScalarLeftAction<>(
                        this, target.subArray(m0, n),
                        operand1,
                        operand2.subArray(m0, n), blocksize);
                ArrScalarLeftAction<T,U,V> left = new ArrScalarLeftAction<>(
                        this, target.subArray(0, m0),
                        operand1,
                        operand2.subArray(0, m0), blocksize);
                ForkJoinTask.invokeAll(left, right);
            }
    }

    /**
     * Performs 'this gets opr1 op opr2' for equally-offset this, opr1, opr2
     * submatrices. Does this by removing intervening submatrices, deriving
     * additional offsets, and delegating to a not-equally-offset overloading of
     * getsLeaf.
     *
     * @param operand1
     * @param op
     * @param operand2
     * @param i_lo
     * @param i_hi
     * @param j_lo
     * @param j_hi
     */
     void setSubArray(Array<T> target, U operand1, Array<V> operand2,
            long i_lo, long i_hi) {
        long o2_i_off = 0;
        long o2_j_off = 0;
        if (target instanceof SubArray) {
            SubArray<T> sub_this = (SubArray<T>) target;
            long i_off = sub_this.begin();
            i_lo += i_off;
            i_hi += i_off;
            o2_i_off -= i_off;
            target = sub_this.parent();
        }
        if (operand2 instanceof SubArray) {
            SubArray<V> sub_2 = (SubArray<V>) operand2;
            o2_i_off += sub_2.begin();
            operand2 = sub_2.parent();
        }
        op.setOffsetSubArray(target, operand1, operand2,
                i_lo, i_hi, o2_i_off);
    }


    static public class ArrScalarLeftAction<T,U,V> extends RecursiveAction {
        private static final long serialVersionUID = 0x71a1a3b67914231cL;

        protected final Array<T> target;
        protected final U operand1;
        protected final Array<V> operand2;
        protected final ArrScalarLeft<T,U,V> node;
        protected final int blocksize;

        public ArrScalarLeftAction(ArrScalarLeft<T,U,V> node, Array<T> target,
                U operand1, Array<V> operand2, int blocksize) {
            this.target = target;
            this.operand1 = operand1;
            this.operand2 = operand2;
            this.node = node;
            this.blocksize = blocksize;
        }

        @Override
        protected void compute() {
            node.setRecursive(target, operand1, operand2, blocksize);
        }
    }
}
