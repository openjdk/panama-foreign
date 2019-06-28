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

import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * A sequence layout. A sequence layout is a special case of a compound layout, made up of a layout element and a repetition count.
 * The repetition count can be zero if the sequence contains no elements. A finite sequence layout can be thought of as a group layout
 * where a given layout element is repeated a number of times that is equal to the sequence size. In other words this
 * layout:
 *
 * <pre>{@code
SequenceLayout.of(3, ValueLayout.ofSignedInt(32));
 * }</pre>
 *
 * is equivalent to the following layout:
 *
 * <pre>{@code
GroupLayout.struct(
    ValueLayout.ofSignedInt(32),
    ValueLayout.ofSignedInt(32),
    ValueLayout.ofSignedInt(32));
 * }</pre>
 * <p>
 * Unbounded sequence layouts can be thought of as a infinite repetitions of a layout element. In such cases the sequence
 * layout will <em>not</em> have an associated size.
 * <p>
 * This is a <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>
 * class; use of identity-sensitive operations (including reference equality
 * ({@code ==}), identity hash code, or synchronization) on instances of
 * {@code SequenceLayout} may have unpredictable results and should be avoided.
 * The {@code equals} method should be used for comparisons.
 *
 * @implSpec
 * This class is immutable and thread-safe.
 */
public class SequenceLayout extends AbstractLayout implements CompoundLayout {

    private final OptionalLong size;
    private final Layout elementLayout;

    private SequenceLayout(OptionalLong size, Layout elementLayout, OptionalLong alignment, Optional<String> name) {
        super(alignment, name);
        this.size = size;
        this.elementLayout = elementLayout;
    }

    /**
     * Computes the layout size, in bits. Since not all sequences have a finite size, this method can throw an exception.
     * @return the layout size (where defined).
     * @throws UnsupportedOperationException if the sequence is unbounded in size (see {@link SequenceLayout#elementsSize()}).
     */
    @Override
    public long bitsSize() throws UnsupportedOperationException {
        if (size.isPresent()) {
            return elementLayout.bitsSize() * size.getAsLong();
        } else {
            throw new UnsupportedOperationException("Cannot compute size of unbounded sequence");
        }
    }

    @Override
    long naturalAlignmentBits() {
        return element().bitsAlignment();
    }

    /**
     * The layout element associated with this sequence layout.
     * @return layout element.
     */
    public Layout element() {
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
     * Create a new sequence layout with given layout element and size.
     * @param elementLayout the layout element.
     * @param size the array repetition count.
     * @return the new sequence layout.
     * @throws IllegalArgumentException if size &lt; 0.
     */
    public static SequenceLayout of(long size, Layout elementLayout) throws IllegalArgumentException {
        checkSize(size, true);
        return new SequenceLayout(OptionalLong.of(size), elementLayout, OptionalLong.empty(), Optional.empty());
    }

    /**
     * Create a new sequence layout, with unbounded size and given layout element.
     * @param elementLayout the layout element.
     * @return the new sequence layout.
     */
    public static SequenceLayout of(Layout elementLayout) {
        return new SequenceLayout(OptionalLong.empty(), elementLayout, OptionalLong.empty(), Optional.empty());
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
        return decorateLayoutString(String.format("[%s:%s]",
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
        if (!(other instanceof SequenceLayout)) {
            return false;
        }
        SequenceLayout s = (SequenceLayout)other;
        return size == s.size && elementLayout.equals(s.elementLayout);
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ size.hashCode() ^ elementLayout.hashCode();
    }

    @Override
    SequenceLayout dup(OptionalLong alignment, Optional<String> name) {
        return new SequenceLayout(elementsSize(), elementLayout, alignment, name);
    }

    //hack: the declarations below are to make javadoc happy; we could have used generics in AbstractLayout
    //but that causes issues with javadoc, see JDK-8224052

    /**
     * {@inheritDoc}
     */
    @Override
    public SequenceLayout withName(String name) {
        return (SequenceLayout)super.withName(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SequenceLayout alignTo(long alignmentBits) throws IllegalArgumentException {
        return (SequenceLayout)super.alignTo(alignmentBits);
    }
}
