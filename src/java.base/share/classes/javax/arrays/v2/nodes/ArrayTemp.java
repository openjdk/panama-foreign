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

import javax.arrays.v2.A1Expr;
import javax.arrays.v2.Array;

/**
 * Temporary AST nodes with array values.  These are inserted into an expression
 * tree during simplification to provide target storage for subtree evaluation.
 * Their geometry (their locality) is chosen based on the needs of their parent
 * and child nodes.
 *
 * @param <T> Element type of temporary operand and result.
 */
public class ArrayTemp<T> extends ArrayNode<T,T> {
    // Perhaps this should be lazily allocated...
    public final Array<T> temporary;

    public ArrayTemp(Array<T> temporary, A1Expr<T> x) {
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
    public long length() {
        return x.length();
    }

    @Override
    public void setInto(Array<T> target) {
        throw new Error("Did not expect to call Temporary.setInto");
    }

    @Override
    public void evalInto(Array<T> target) {
        throw new Error("Did not expect to call Temporary.evalInto");
    }

    @Override
    public A1Expr<T> simplify(Array<T> target, Simplifier simplifier) {
        return this; // for now.
    }

    @Override
    public A1Expr<T> tempifyAsNecessary(Simplifier simplifier) {
        return this;
    }

    @Override
    public Array<T> getValue() {
        x.evalInto(temporary);
        return temporary;
    }

}
