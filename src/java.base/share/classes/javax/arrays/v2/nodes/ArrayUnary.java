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
import javax.arrays.v2.Array;
import javax.arrays.v2.A1Expr;
import javax.arrays.v2.A2Expr;
import static javax.arrays.v2.A2Expr.firstNotEqual;
import javax.arrays.v2.SubArray;
import javax.arrays.v2.ops.UnaryOp;

/**
 * Elementwise unary-operation array AST nodes.
 *
 * @param <T> Result element type
 * @param <U> Operand element type
 */
public class ArrayUnary<T, U> extends ArrayWithOp<T, U> {
    final UnaryOp<T, U> op;

    public ArrayUnary(UnaryOp<T, U> op, A1Expr<U> x) {
        super(x);
        this.op = op;
    }

    @Override
    public UnaryOp<T, U> op() {
        return op;
    }

    @Override
    public String toString() {
        return "(" + op + " " + x + ")";
    }

    @Override
    public A1Expr<T> simplify(Array<T> target, Simplifier simplifier) {
        return simplifier.simplifyArrNode1(target, this);
    }

    @Override
    public void evalInto(Array<T> target) {
        setRecursive(target, x.getValue(), A2Expr.elementWiseBlockSize());
    }


       /**
     * Assigns (op operand1) into target, perhaps with recursive subdivision,
     * perhaps with fork/join parallelism. Recursive subevaluations
     * are applied to results of "subArray" to create an opportunity for
     * specialization (simplification) of array substructure.
     *
     * It may be profitable to override this method to obtain customized
     * behavior for special (e.g., distributed) array representations.
     *
     * @param operand1
     * @param op
     * @param blocksize
     */
    void setRecursive(Array<T> target, Array<U> operand1, int blocksize) {
        long n = target.length();
            if (n <= blocksize) {
                // kernel iteration.
                setSubArray(target, operand1, 0, n);
            } else {
                // subdivide columns
                long m0 = n >> 1;
                long m1 = target.middle();
                long m2 = operand1.middle();
                m0 = firstNotEqual(m0, m1, m2);

                ArrNode1Action<T,U> right = new ArrNode1Action<>(
                        target.subArray(m0, n),
                        operand1.subArray(m0, n), this,
                        blocksize);
                right.fork();
                ArrNode1Action<T,U> left = new ArrNode1Action<>(
                        target.subArray(0, m0),
                        operand1.subArray(0, m0), this,
                        blocksize);
                left.compute();
                right.join();
            }
    }

    /**
     * Performs 'this gets op opr1' for equally-offset this, opr1
     * submatrices. Does this by removing intervening submatrices, deriving
     * additional offsets, and delegating to a not-equally-offset overloading of
     * getsLeaf.
     *
     * @param operand1
     * @param op
     * @param i_lo
     * @param i_hi
     * @param j_lo
     * @param j_hi
     */
    void setSubArray(Array<T> target, Array<U> operand1,
            long i_lo, long i_hi) {
        long o1_i_off = 0;
        if (target instanceof SubArray) {
            SubArray<T> sub_this = (SubArray<T>) target;
            long i_off = sub_this.begin();
            i_lo += i_off;
            i_hi += i_off;
            o1_i_off -= i_off;
            target = sub_this.parent();
        }
        if (operand1 instanceof SubArray) {
            SubArray<U> sub_1 = (SubArray<U>) operand1;
            o1_i_off += sub_1.begin();
            operand1 = sub_1.parent();
        }
        op.setOffsetSubArray(target, operand1,
                i_lo, i_hi, o1_i_off);
    }

    static public class ArrNode1Action<T,U> extends RecursiveAction {
        private static final long serialVersionUID = 0x5cd1ff3fa3a97aacL;

        protected final Array<T> target;
        protected final Array<U> operand1;
        protected final ArrayUnary<T,U> node;
        protected final int blocksize;

        public ArrNode1Action(Array<T> target,
                Array<U> operand1, ArrayUnary<T,U> node, int blocksize) {
            this.target = target;
            this.operand1 = operand1;
            this.node = node;
            this.blocksize = blocksize;
        }

        @Override
        protected void compute() {
            node.setRecursive(target, operand1, blocksize);
        }
    }
}
