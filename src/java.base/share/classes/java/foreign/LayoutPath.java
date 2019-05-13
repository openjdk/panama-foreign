/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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

package java.foreign;

import java.lang.invoke.VarHandle;

/**
 * A layout path.
 */
public interface LayoutPath {

    /**
     * The layout associated with this path.
     * @return the layout associated with this path.
     */
    Layout layout();

    /**
     * The enclosing path of this layout path.
     * @return The enclosing path of this layout path.
     */
    LayoutPath enclosing();

    /**
     * The offset of this layout path (relative to the root element).
     * @return The offset of this layout path (relative to the root element).
     */
    long offset();

    /**
     * A var handle that can be used to dereference memory at this path.
     * @param carrier the var handle carrier type.
     * @return a var handle which can be used to dereference memory at this path.
     */
    VarHandle dereferenceHandle(Class<?> carrier);

    /**
     * Lookup a layout path for a sub-elements with given name.
     * @param name the name of the matching layout sub-element.
     * @return the layout path for the sub-element
     * @throws IllegalArgumentException if no sub-element with given name can be found in current path.
     * @throws UnsupportedOperationException if the current path does not point to a {@link Group} layout, or
     * a {@link Value} layout whose sub-contents (see {@link Value#contents()}) are set.
     *
     * @implSpec in case multiple sub-elements with matching name exists, the first is returned; that is,
     * the sub-element with lowest offset from current path is returned.
     */
    LayoutPath groupElement(String name) throws IllegalArgumentException, UnsupportedOperationException;

    /**
     * Lookup a layout path for a sub-elements at given index.
     * @param index the index of the matching layout sub-element.
     * @return the layout path for the sub-element
     * @throws IllegalArgumentException if the index is &lt; 0, or if it is bigger than number of elements in the group.
     * @throws UnsupportedOperationException if the current path does not point to a {@link Group} layout, or
     * a {@link Value} layout whose sub-contents (see {@link Value#contents()}) are set.
     */
    LayoutPath groupElement(long index) throws IllegalArgumentException, UnsupportedOperationException;

    /**
     * Return a layout path to an array element.
     * @return a layout path to an array element.
     * @throws UnsupportedOperationException if the current path does not point to a {@link Sequence} layout.
     */
    LayoutPath sequenceElement() throws UnsupportedOperationException;
}
