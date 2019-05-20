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

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * A group layout is used to combine together multiple layouts. There are two ways in which layouts can be combined,
 * e.g. a 'struct' (see {@link Kind#STRUCT}), where contained elements are laid out one after the other, and a 'union'
 * (see {@link Kind#UNION}, where contained elements are laid out 'on top' of each other.
 */
public class GroupLayout extends AbstractLayout implements CompoundLayout, Iterable<Layout> {

    /**
     * The group kind.
     */
    public enum Kind {
        /**
         * A 'struct' kind.
         */
        STRUCT(LongStream::sum, ""),
        /**
         * A 'union' kind.
         */
        UNION(ls -> ls.max().getAsLong(), "|");

        final ToLongFunction<LongStream> sizeFunc;
        final String delimTag;

        Kind(ToLongFunction<LongStream> sizeFunc, String delimTag) {
            this.sizeFunc = sizeFunc;
            this.delimTag = delimTag;
        }
    }

    private final Kind kind;
    private final List<Layout> elements;
    private long size = -1L;
    private long alignment = -1L;

    GroupLayout(Kind kind, List<Layout> elements, OptionalLong alignment, Optional<String> name) {
        super(alignment, name);
        this.kind = kind;
        this.elements = elements;
    }

    /**
     * Returns the kind associated to this group.
     * @return the group kind.
     */
    public Kind kind() {
        return kind;
    }

    /**
     * Returns the sub-elements associated with this group.
     * @return the element layouts.
     */
    public Stream<Layout> elements() {
        return elements.stream();
    }

    @Override
    public String toString() {
        return decorateLayoutString(elements.stream()
                .map(Object::toString)
                .collect(Collectors.joining(kind.delimTag, "[", "]")));
    }

    /**
     * Returns an iterator over the elements in this layout in proper sequence.
     * @return an iterator over the elements in this list in proper sequence.
     */
    @Override
    public Iterator<Layout> iterator() {
        return elements().iterator();
    }

    /**
     * The element layout at given position in this group layout.
     * @param index the position of the group sub-element.
     * @return element layout.
     */
    public Layout elementLayout(long index) {
        return elements.get((int)index);
    }

    /**
     * Returns the number of elements in this compound layout.
     * @return the number of elements in this compound layout.
     */
    public long elementsSize() {
        return elements.size();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!super.equals(other)) {
            return false;
        }
        if (!(other instanceof GroupLayout)) {
            return false;
        }
        GroupLayout g = (GroupLayout)other;
        return kind.equals(g.kind) && elements.equals(g.elements);
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ kind.hashCode() ^ elements.hashCode();
    }

    @Override
    public long bitsSize() {
        if (size == -1L) {
            size = kind.sizeFunc.applyAsLong(elements.stream().mapToLong(Layout::bitsSize));
        }
        return size;
    }

    @Override
    long naturalAlignmentBits() {
        if (alignment == -1L) {
            alignment = Kind.UNION.sizeFunc.applyAsLong(elements.stream().mapToLong(Layout::alignmentBits));
        }
        return alignment;
    }

    /**
     * Create a new product group layout with given elements.
     * @param elements The components of the product layout.
     * @return the new product group layout.
     */
    public static GroupLayout struct(Layout... elements) {
        return new GroupLayout(Kind.STRUCT, List.of(elements), OptionalLong.empty(), Optional.empty());
    }

    /**
     * Create a new sum group layout with given elements.
     * @param elements The components of the sum layout.
     * @return the new sum group layout.
     */
    public static GroupLayout union(Layout... elements) {
        return new GroupLayout(Kind.UNION, List.of(elements), OptionalLong.empty(), Optional.empty());
    }

    @Override
    GroupLayout dup(OptionalLong alignment, Optional<String> name) {
        return new GroupLayout(kind, elements, alignment, name);
    }

    //hack: the declarations below are to make javadoc happy; we could have used generics in AbstractLayout
    //but that causes issues with javadoc, see JDK-8224052

    /**
     * {@inheritDoc}
     */
    @Override
    public GroupLayout withName(String name) {
        return (GroupLayout)super.withName(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroupLayout alignTo(long alignmentBits) throws IllegalArgumentException {
        return (GroupLayout)super.alignTo(alignmentBits);
    }
}
