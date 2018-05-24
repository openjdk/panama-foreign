/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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
package java.nicl.layout;

import jdk.internal.nicl.types.DescriptorParser;

import java.util.Map;
import java.util.Optional;

/**
 * This interface models the layout of a group of bits in a memory region.
 * Layouts can be annotated in order to embed domain specific knowledge, and they can be referenced by name
 * (see {@link Unresolved}). A layout is always associated with a size (in bits).
 */
public interface Layout {
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
     * The key of the predefined 'name' annotation.
     */
    String NAME = "name";

    /**
     * Return the key-value annotations map associated with this object.
     * @return the key-value annotations map.
     */
    Map<String, String> annotations();

    /**
     * Return the value of the 'name' annotation (if any) associated with this object.
     * @return the layout name (if any).
     */
    default Optional<String> name() {
        return Optional.ofNullable(annotations().get(NAME));
    }

    /**
     * Add annotation to layout.
     * @param name the annotation name.
     * @param value the annotation value.
     * @return the annotated layout.
     */
    Layout withAnnotation(String name, String value);

    /**
     * Strip all annotations from this (possibly annotated) layout.
     * @return the unannotated layout.
     */
    Layout stripAnnotations();

    @Override
    String toString();

    static Layout of(String s) {
        return new DescriptorParser(s).parseLayout();
    }
}
