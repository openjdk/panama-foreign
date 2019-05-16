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
 * A layout path describes a sequence of layout elements that need to be traversed to get from a root layout
 * to a nested layout element. A root layout path can be obtained by calling the {@link Layout#toPath()} method,
 * after which nested layout access can be achieved by using the methods {@link LayoutPath#elementPath(long)},
 * {@link LayoutPath#elementPath(String)} and {@link LayoutPath#elementPath()}.
 *
 * Layout paths can feature one or more 'free' dimensions (see {@link LayoutPath#dimensions()}. For instance,
 * a layout path pointing into an unspecified sequence element (see {@link LayoutPath#elementPath()} features
 * a free dimension, which will have to be bound at runtime; that is, the {@link VarHandle} obtained via
 * {@link LayoutPath#dereferenceHandle(Class)} will feature an extra {@code long} access coordinate.
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
     *
     * @apiNote if this layout path has one (or more) free dimensions (see {@link LayoutPath#dimensions()},
     * the offset is computed as if all the indices corresponding to such dimensions were set to {@code 0}.
     */
    long offset() throws UnsupportedOperationException;

    /**
     * The number of free dimensions in this layout path. Each dimension will correspond to an extra
     * {@code long} access coordinate in the {@link VarHandle} obtained with {@link LayoutPath#dereferenceHandle(Class)}.
     * @return the number of free dimensions in this path.
     */
    int dimensions();

    /**
     * A var handle that can be used to dereference memory at this path.
     * @param carrier the var handle carrier type.
     * @return a var handle which can be used to dereference memory at this path.
     * @throws UnsupportedOperationException if the layout targeted by this path is not a {@link ValueLayout} layout.
     * @throws IllegalArgumentException if the carrier does not represent a primitive type, if the carrier is {@code void},
     * {@code boolean}, or if the size of the carrier type does not match that of the layout targeted by this layout path.
     *
     * @apiNote the result var handle will feature an additional {@code long} access coordinate for every
     * unspecified sequence access component contained in this layout path.
     */
    VarHandle dereferenceHandle(Class<?> carrier) throws UnsupportedOperationException, IllegalArgumentException;

    /**
     * Returns a layout path for a sub-element with given name. The number of dimensions of the resulting path is
     * the same as that of the currrent path.
     * @param name the name of the matching layout sub-element.
     * @return the layout path for the sub-element
     * @throws IllegalArgumentException if no sub-element with given name can be found in current path.
     * @throws UnsupportedOperationException if the current path does not point to a {@link GroupLayout} layout.
     *
     * @implSpec in case multiple sub-elements with matching name exists, the first is returned; that is,
     * the sub-element with lowest offset from current path is returned.
     */
    LayoutPath elementPath(String name) throws IllegalArgumentException, UnsupportedOperationException;

    /**
     * Returns a layout path for a sub-element at given index. The number of dimensions of the resulting path is
     * the same as that of the currrent path.
     * @param index the index of the matching layout sub-element.
     * @return the layout path for the sub-element
     * @throws IllegalArgumentException if the index is &lt; 0, or if it is bigger than number of elements in the group.
     * @throws UnsupportedOperationException if the current path does not point to a {@link CompoundLayout} layout.
     */
    LayoutPath elementPath(long index) throws IllegalArgumentException, UnsupportedOperationException;

    /**
     * Returns a layout path to an unspecified sequence element. The number of dimensions of the resulting path
     * will be {@code 1 + n}, where {@code n} is the number of dimensions in the current path.
     * @return a layout path to an unspecified sequence element.
     * @throws UnsupportedOperationException if the current path does not point to a {@link SequenceLayout} layout.
     */
    LayoutPath elementPath() throws UnsupportedOperationException;
}
