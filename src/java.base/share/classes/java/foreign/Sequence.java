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

import java.util.Map;
import java.util.OptionalLong;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * A sequence layout. A sequence layout is a special case of a group layout, made up of an element layout and a repetition count.
 * The repetition count can be zero if the sequence contains no elements. A 'bound' sequence layout of the kind e.g. {@code [ 4:i32 ]},
 * can be thought of as a group layout e.g. {@code [i32 i32 i32 i32]} where a given layout element is repeated a number of times that
 * is equal to the sequence size. Unbound sequence layouts can be thought of an infinite sequence of a layout element,
 * as in {@code [i32 i32 i32 i32, ...]}. In such cases the sequence layout will not have an associated size.
 */
public class Sequence extends AbstractLayout<Sequence> implements Compound {

    private final OptionalLong size;
    private final Layout elementLayout;

    private Sequence(OptionalLong size, Layout elementLayout, OptionalLong alignment, Map<String, String> attributes) {
        super(alignment, attributes);
        this.size = size;
        this.elementLayout = elementLayout;
    }

    @Override
    public long bitsSize() throws UnsupportedOperationException {
        if (size.isPresent()) {
            return elementLayout.bitsSize() * size.getAsLong();
        } else {
            throw new UnsupportedOperationException("Cannot compute size of unbound layout");
        }
    }

    @Override
    long naturalAlignmentBits() {
        return elementLayout().bitsSize();
    }

    @Override
    public boolean isPartial() {
        return elementLayout.isPartial();
    }

    /**
     * The element layout associated with this sequence layout.
     * @return element layout.
     */
    public Layout elementLayout() {
        return elementLayout;
    }

    @Override
    public Stream<Layout> elements() {
        Stream<Layout> elems = Stream.iterate(elementLayout, UnaryOperator.identity());
        return size.isPresent() ?
                elems.limit(size.getAsLong()) :
                elems;
    }

    /**
     * Create a new sequence layout with given element layout and size.
     * @param elementLayout the element layout.
     * @param size the array repetition count.
     * @return the new sequence layout.
     */
    public static Sequence of(long size, Layout elementLayout) {
        return new Sequence(OptionalLong.of(size), elementLayout, OptionalLong.empty(), NO_ANNOS);
    }

    /**
     * Create a new unbound sequence layout with given element layout.
     * @param elementLayout the element layout.
     * @return the new sequence layout.
     */
    public static Sequence of(Layout elementLayout) {
        return new Sequence(OptionalLong.empty(), elementLayout, OptionalLong.empty(), NO_ANNOS);
    }

    /**
     * Returns the repetition count associated with this sequence layout.
     * @return the repetition count (can be zero if array size is unspecified).
     */
    public OptionalLong elementsSize() {
        return size;
    }

    @Override
    public String toString() {
        return wrapWithAlignmentAndAttributes(String.format("[%s:%s]",
                size.isPresent() ? size.getAsLong() : "", elementLayout));
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
        return super.hashCode() ^ size.hashCode() ^ elementLayout.hashCode();
    }

    @Override
    Sequence dup(OptionalLong alignment, Map<String, String> attributes) {
        return new Sequence(elementsSize(), elementLayout, alignment, attributes);
    }
}
