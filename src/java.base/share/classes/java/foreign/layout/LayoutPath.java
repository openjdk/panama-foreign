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

package java.foreign.layout;

import jdk.internal.foreign.LayoutPathsImpl;

import java.lang.invoke.VarHandle;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
     * Lookup a layout path for elements with given name.
     * @param name the name of the matching layout subelements.
     * @return a stream of matching layout subelements.
     */
    default Stream<LayoutPath> lookup(String name) {
        return lookup(l -> l.name().map(n -> n.equals(name)).orElse(false));
    }

    /**
     * Lookup a layout path with given matching predicate.
     * @param condition the predicate describing matching layout subelements.
     * @return a stream of matching layout subelements.
     */
    Stream<LayoutPath> lookup(Predicate<? super Layout> condition);

    /**
     * Create a new layout path rooted in the given layout.
     * @param layout the root of the layout path to be created.
     * @return a new layout path.
     */
    static LayoutPath of(Layout layout) {
        return LayoutPathsImpl.of(layout);
    }
}
