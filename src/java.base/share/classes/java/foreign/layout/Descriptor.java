/*
 *  Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.
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

package java.foreign.layout;

import java.util.Map;
import java.util.Optional;

/**
 * This interface models a native descriptor. There are two kinds of descriptors: (i) layouts and functions.
 * Layouts are used to model layout of some memory region (see {@link Layout}, while functions are used to model
 * native function signatures. Descriptors can be annotated, as a way to embed custom information. A special 'name'
 * annotation is reserved (see {@link Descriptor#NAME}); this annotation is useful when referencing descriptors by name
 * (see {@link Unresolved}).
 *
 * @see Layout
 * @see Function
 */
public interface Descriptor {
    /**
     * The key of the predefined 'name' annotation.
     */
    String NAME = "name";

    /**
     * Does this descriptor contain unresolved layouts?
     * @return true if this layout contains (possibly nested) unresolved layouts.
     */
    boolean isPartial();

    /**
     * Return the key-value annotations map associated with this object.
     * @return the key-value annotations map.
     */
    Map<String, String> annotations();

    /**
     * Return the value of the 'name' annotation (if any) associated with this object.
     * @return the descriptor name (if any).
     */
    default Optional<String> name() {
        return Optional.ofNullable(annotations().get(NAME));
    }

    /**
     * Add annotation to descriptor.
     * @param name the annotation name.
     * @param value the annotation value.
     * @return the annotated layout.
     */
    Descriptor withAnnotation(String name, String value);

    /**
     * Strip all annotations from this (possibly annotated) descriptor.
     * @return the unannotated descriptor.
     */
    Descriptor stripAnnotations();

    @Override
    String toString();
}
