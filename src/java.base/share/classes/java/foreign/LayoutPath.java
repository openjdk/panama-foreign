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
 * Layout paths can be bound or unbound. A layout path is said to be bound, if all its components are fully known
 * statically, e.g. if one can determine the offset from the root layout down to the innermost nested layout element
 * described by this layout. Layout paths for which this property is not true are said to be unbound layout paths.
 *
 * An example of an unbound layout path is a layout path which contains an access to an unspecified sequence layout element,
 * that is, an access whose full coordinate will not be known in full until execution.
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
     * @throws UnsupportedOperationException if the path is unbound (see {@link LayoutPath#isBound()}.
     */
    long offset() throws UnsupportedOperationException;

    /**
     * Is this layout path fully specified? That is, are all components of this layout paths known statically?
     * @return true, if the layout path is fully specified.
     */
    boolean isBound();

    /**
     * A var handle that can be used to dereference memory at this path.
     * @param carrier the var handle carrier type.
     * @return a var handle which can be used to dereference memory at this path.
     * @throws UnsupportedOperationException if the layout targeted by this path is not a {@link Value} layout.
     * @throws IllegalArgumentException if the carrier does not represent a primitive type, if the carrier is {@code void},
     * {@code boolean}, or if the size of the carrier type does not match that of the layout targeted by this layout path.
     *
     * @apiNote the result var handle will feature an additional {@code long} access coordinate for every
     * unspecified sequence access component contained in this layout path.
     */
    VarHandle dereferenceHandle(Class<?> carrier) throws UnsupportedOperationException, IllegalArgumentException;

    /**
     * Returns a bound layout path for a sub-elements with given name.
     * @param name the name of the matching layout sub-element.
     * @return the layout path for the sub-element
     * @throws IllegalArgumentException if no sub-element with given name can be found in current path.
     * @throws UnsupportedOperationException if the current path does not point to a {@link Group} layout.
     *
     * @implSpec in case multiple sub-elements with matching name exists, the first is returned; that is,
     * the sub-element with lowest offset from current path is returned.
     */
    LayoutPath elementPath(String name) throws IllegalArgumentException, UnsupportedOperationException;

    /**
     * Returns a bound layout path for a sub-elements at given index.
     * @param index the index of the matching layout sub-element.
     * @return the layout path for the sub-element
     * @throws IllegalArgumentException if the index is &lt; 0, or if it is bigger than number of elements in the group.
     * @throws UnsupportedOperationException if the current path does not point to a {@link Compound} layout.
     */
    LayoutPath elementPath(long index) throws IllegalArgumentException, UnsupportedOperationException;

    /**
     * Returns an unbound layout path to an unspecified sequence element. The index of the access element will need to be provided
     * at runtime, on the var handle obtained through {@link LayoutPath#dereferenceHandle(Class)}.
     * @return a layout path to an unspecified sequence element.
     * @throws UnsupportedOperationException if the current path does not point to a {@link Sequence} layout.
     */
    LayoutPath elementPath() throws UnsupportedOperationException;
}
