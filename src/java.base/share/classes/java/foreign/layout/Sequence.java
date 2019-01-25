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
package java.foreign.layout;

import java.util.Collections;
import java.util.Map;

/**
 * A sequence layout. A sequence layout is a special case of a group layout, made up of an element layout and a repetition count.
 * The repetition count can be zero if the sequence contains no elements. A sequence layout of the kind e.g. {@code [ 4i32 ]},
 * always induces a group layout e.g. {@code [i32 i32 i32 i32]} - that is, the group associated with a sequence is
 * a 'struct' group (see {@link Group.Kind#STRUCT}), where a given layout element is repeated a number of times that
 * is equal to the sequence size.
 */
public final class Sequence extends Group {

    private final long size;
    private final Layout elementLayout;

    private Sequence(long size, Layout elementLayout, Map<String, String> annotations) {
        super(Kind.STRUCT, Collections.nCopies((int)size, elementLayout), annotations);
        this.size = size;
        this.elementLayout = elementLayout;
    }

    /**
     * The element layout associated with this sequence layout.
     * @return element layout.
     */
    public Layout element() {
        return elementLayout;
    }

    /**
     * Create a new sequence layout with given element layout and size.
     * @param elementLayout the element layout.
     * @param size the array repetition count.
     * @return the new sequence layout.
     */
    public static Sequence of(long size, Layout elementLayout) {
        return new Sequence(size, elementLayout, NO_ANNOS);
    }

    /**
     * Returns the repetition count associated with this sequence layout.
     * @return the repetition count (can be zero if array size is unspecified).
     */
    public long elementsSize() {
        return size;
    }

    @Override
    public String toString() {
        return wrapWithAnnotations(String.format("[%d%s]",
                size, elementLayout));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!super.equals(other)) {
            return false;
        }
        if (!(other instanceof Sequence)) {
            return false;
        }
        Sequence s = (Sequence)other;
        return size == s.size && elementLayout.equals(s.elementLayout);
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ Long.hashCode(size) ^ elementLayout.hashCode();
    }

    @Override
    Sequence withAnnotations(Map<String, String> annotations) {
        return new Sequence(elementsSize(), elementLayout, annotations);
    }

    @Override
    public Sequence stripAnnotations() {
        return (Sequence)super.stripAnnotations();
    }

    @Override
    public Sequence withAnnotation(String name, String value) {
        return (Sequence) super.withAnnotation(name, value);
    }

    @Override
    public Sequence withEndianness(Value.Endianness newEndian) {
        return new Sequence(elementsSize(),
                elementLayout.withEndianness(Value.Endianness.hostEndian()),
                annotations());
    }
}
