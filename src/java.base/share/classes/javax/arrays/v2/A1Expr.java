/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package javax.arrays.v2;

import javax.arrays.v2.nodes.Simplifier;
import javax.arrays.v2.nodes.ArrayTemp;

/**
 *
 * @param <T>
 */
public interface A1Expr<T> {

    Class<T> elementType();

    Array<T> getValue();

    long length();

    default A1Expr<T> simplify(Array<T> target) {
        return simplify(target, A2Expr.simplifier);
    }

    A1Expr<T> simplify(Array<T> target, Simplifier simplifier);

    default void setInto(Array<T> target) {
        A1Expr<T> source = simplify(target);
        source.evalInto(target);
    }

    void evalInto(Array<T> target);

    // Override in Temporary, Matrix, Transpose
    default A1Expr<T> tempifyAsNecessary(Simplifier simplifier) {
        // Here we must hold our nose and do the best possible job of allocating
        // the temporary.  We desperately DO NOT want to box primitives or near
        // primitives.
        Array<T> temp = simplifier.makeArray(this);
        A1Expr<T> x = simplify(temp, simplifier);
        ArrayTemp<T> t = new ArrayTemp<>(temp, x);
        return t;
    }

}
