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

package javax.arrays.v2;

/**
 * A Stencil is a matrix combined with a target-offset point.
 * For example, in Conway's game of life the stencil is a 3x3
 * square with the target offset the center point (1,1).
 *
 * Stencils are typically used in convolution operations.
 *
 * @param <T>
 */
public class Stencil<T> {
    protected final int xOffset;
    protected final int yOffset;
    protected final Matrix<T> stencil;

    private Stencil(Matrix<T> stencil, int xOffset, int yOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.stencil = stencil;
    }

    public final int xOffset() {
       return this.xOffset;
    }

    public final int yOffset() {
       return this.yOffset;
    }

    public final T get(int i, int j) {
        return stencil.get(i, j);
    }

    public static <T> Stencil<T> getStencil(Matrix<T> stencil, int xOffset, int yOffset) {
        // Need to write special cases for {1,2,3}x{1,2,3} stencils.
        return new Stencil<>(stencil, xOffset, yOffset);
    }
}
