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
import javax.arrays.v2.Array;
import javax.arrays.v2.A1Expr;
import javax.arrays.v2.A2Expr;
import javax.arrays.v2.SubArray;
import javax.arrays.v2.ops.AssociativeOp;

/**
 * A binary AST node with an elementwise associative operator.
 * Result and operand element types are all the same.
 *
 * @param <T> Element type of operands and result.
 */
public class ArrayAssociativeBinary<T> extends ArrayAssociative<T> {
    public final A1Expr<T> y;

    public ArrayAssociativeBinary(AssociativeOp<T> op, A1Expr<T> x, A1Expr<T> y) {
        super(op, x);
        this.y = y;
        if (x.length() != y.length() ) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String toString() {
        return "(" + op + " " + x + " " + y + ")";
    }

    @Override
    public A1Expr<T> simplify(Array<T> target, Simplifier simplifier) {
        return simplifier.simplifyArrNode2(target, this);
    }

    @Override
    public void evalInto(Array<T> target) {
        long n = target.length();
        if (x.length() != n || y.length() != n) {
            throw new IllegalArgumentException("Matrices must have conforming sizes");
        }
        setRecursive(target, x.getValue(), y.getValue(), A2Expr.elementWiseBlockSize());
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
     void setRecursive(Array<T> target, Array<T> operand1, Array<T> operand2, int blocksize) {
        long n = target.length();
            if (n <= blocksize) {
                // kernel iteration.
                setSubArray(target, operand1, operand2, 0, n);
            } else {
                // subdivide columns
                long m0 = n >> 1;
                long m1 = target.middle();
                long m2 = operand1.middle();
                long m3 = operand2.middle();
                m0 = A2Expr.firstNotEqual(m0, m1, m2, m3);

                ArrNode2Action<T> right = new ArrNode2Action<>(
                        this, target.subArray(m0, n),
                        operand1.subArray(m0, n),
                        operand2.subArray(m0, n), blocksize);
                ArrNode2Action<T> left = new ArrNode2Action<>(
                        this, target.subArray(0, m0),
                        operand1.subArray(0, m0),
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
     void setSubArray(Array<T> target, Array<T> operand1, Array<T> operand2,
            long i_lo, long i_hi) {
        long o1_i_off = 0;
        long o2_i_off = 0;

        if (target instanceof SubArray) {
            SubArray<T> sub_this = (SubArray<T>) target;
            long i_off = sub_this.begin();
            long j_off = sub_this.begin();
            i_lo += i_off;
            i_hi += i_off;
            o1_i_off -= i_off;
            o2_i_off -= i_off;
            target = sub_this.parent();
        }
        if (operand1 instanceof SubArray) {
            SubArray<T> sub_1 = (SubArray<T>) operand1;
            o1_i_off += sub_1.begin();
            operand1 = sub_1.parent();
        }
        if (operand2 instanceof SubArray) {
            SubArray<T> sub_2 = (SubArray<T>) operand2;
            o2_i_off += sub_2.begin();
            operand2 = sub_2.parent();
        }
        op.setOffsetSubArray(target, operand1, operand2,
                i_lo, i_hi, o1_i_off, o2_i_off);
    }


    static public class ArrNode2Action<T> extends RecursiveAction {
        private static final long serialVersionUID = 0x776a3f5b320f1c5eL;

        protected final Array<T> target;
        protected final Array<T> operand1;
        protected final Array<T> operand2;
        protected final ArrayAssociativeBinary<T> node;
        protected final int blocksize;

        public ArrNode2Action(ArrayAssociativeBinary<T> node, Array<T> target,
                Array<T> operand1, Array<T> operand2, int blocksize) {
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
