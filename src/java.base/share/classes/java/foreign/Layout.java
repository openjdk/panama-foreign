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

import java.lang.annotation.Native;
import java.util.Optional;

/**
 * This interface models the layout of a group of bits in a memory region.
 * Layouts can be associated with one or more attributes in order to embed domain specific knowledge,
 * and they can be referenced by name (see {@link Unresolved}). A layout is always associated with a size (in bits).
 */
public interface Layout {
    /**
     * The key of the predefined 'name' attribute.
     */
    String NAME_ATTRIBUTE = "name";

    /**
     * Computes the layout size, in bits
     * @return the layout size.
     */
    long bitsSize();

    /**
     * Does this layout contain unresolved layouts?
     * @return true if this layout contains (possibly nested) unresolved layouts.
     */
    boolean isPartial();

    /**
     * Return the value of the layout attribute with given name (if any).
     * @param name the name of the attribute whose value is to be retrieved.
     * @return the value of the layout attribute with given name.
     */
    Optional<String> attribute(String name);

    /**
     * Add an attribute to the current layout.
     * @param name the attribute name.
     * @param value the attribute value.
     * @return a new layout with the desired layout attribute.
     */
    Layout withAttribute(String name, String value);

    /**
     * Return the value of the 'name' attribute (if any) associated with this layout.
     * @return the layout 'name' attribute (if any).
     */
    default Optional<String> name() {
        return attribute(NAME_ATTRIBUTE);
    }

    /**
     * Attach name annotation to the current layout.
     * @param name name annotation.
     * @return a new layout with desired name annotation.
     */
    default Layout withName(String name) {
        return withAttribute(NAME_ATTRIBUTE, name);
    }

    /**
     * Strip all annotations from the current (possibly annotated) layout.
     * @return the unannotated layout.
     */
    Layout stripAnnotations();

    @Override
    String toString();

    /**
     * Obtain layout path rooted at this layout.
     * @return a new layout path rooted at this layout.
     */
    LayoutPath toPath();
}
