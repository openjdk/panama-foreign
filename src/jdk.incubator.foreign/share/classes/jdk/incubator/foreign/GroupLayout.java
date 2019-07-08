/*
 *  Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.  Oracle designates this
 *  particular file as subject to the "Classpath" exception as provided
 *  by Oracle in the LICENSE file that accompanied this code.
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
 *   Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 *
 */
package jdk.incubator.foreign;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * A group layout is used to combine together multiple <em>member layouts</em>. There are two ways in which member layouts
 * can be combined: if member layouts are laid out one after the other, the resulting group layout is said to be a <em>struct</em>
 * (see {@link Layout#ofStruct(Layout...)}); conversely, if all member layouts are laid out at the same starting offset,
 * the resulting group layout is said to be a <em>union</em> (see {@link Layout#ofUnion(Layout...)}).
 * <p>
 * This is a <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>
 * class; use of identity-sensitive operations (including reference equality
 * ({@code ==}), identity hash code, or synchronization) on instances of
 * {@code GroupLayout} may have unpredictable results and should be avoided.
 * The {@code equals} method should be used for comparisons.
 *
 * @implSpec
 * This class is immutable and thread-safe.
 */
public class GroupLayout extends AbstractLayout {

    /**
     * The group kind.
     */
    enum Kind {
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
     * Returns the member layouts associated with this group.
     * @return the member layouts associated with this group.
     *
     * @apiNote the order in which member layouts are returned is the same order in which member layouts have
     * been passed to one of the group layout factory methods (see {@link Layout#ofStruct(Layout...)},
     * {@link Layout#ofUnion(Layout...)}).
     */
    public List<Layout> memberLayouts() {
        return Collections.unmodifiableList(elements);
    }

    @Override
    public String toString() {
        return decorateLayoutString(elements.stream()
                .map(Object::toString)
                .collect(Collectors.joining(kind.delimTag, "[", "]")));
    }

    /**
     * Is this group layout a <em>struct</em>?
     * @return true, if this group layout is a <em>struct</em>.
     */
    public boolean isStruct() {
        return kind == Kind.STRUCT;
    }

    /**
     * Is this group layout a <em>union</em>?
     * @return true, if this group layout is a <em>union</em>.
     */
    public boolean isUnion() {
        return kind == Kind.UNION;
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
            alignment = Kind.UNION.sizeFunc.applyAsLong(elements.stream().mapToLong(Layout::bitsAlignment));
        }
        return alignment;
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
