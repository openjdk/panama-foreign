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

import jdk.internal.foreign.LayoutPathImpl;

import java.lang.invoke.VarHandle;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * A compound layout. A compound layout is made of one or more layout sub-elements. Heterogeneous compound layouts
 * are modelled by the {@link GroupLayout} class, and are always finite in size. Homogeneous compound layouts are modelled
 * by the {@link SequenceLayout} class, and can be either finite or infinite in size.
 */
public interface CompoundLayout extends Layout {
    /**
     * Returns a stream of the layout sub-elements which forms this compound layout.
     * @return a stream of layout sub-elements.
     *
     * @apiNote in case the compound layout is a {@link SequenceLayout} whose size is unbounded, the resulting stream is infinite
     * and methods such as {@link Stream#limit} should be used.
     */
    Stream<Layout> elements();

    /**
     * The offset of the layout element selected by a given layout path, where the path is considered rooted in this
     * layout.
     * @param pathOperator an operator that can be used to generate the desired layout path.
     * @return The offset of layout element selected by a given layout path.
     *
     * @apiNote if the layout path has one (or more) free dimensions,
     * the offset is computed as if all the indices corresponding to such dimensions were set to {@code 0}.
     */
    default long offset(UnaryOperator<Path> pathOperator) throws UnsupportedOperationException {
        return ((LayoutPathImpl)pathOperator.apply(LayoutPathImpl.rootPath(this))).offset();
    }

    /**
     * A var handle that can be used to dereference memory at the layout element selected by a given layout path,
     * where the path is considered rooted in this layout.
     * @param carrier the var handle carrier type.
     * @param pathOperator an operator that can be used to generate the desired layout path.
     * @return a var handle which can be used to dereference memory at the layout element denoted by given layout path.
     * @throws UnsupportedOperationException if the layout targeted by this path is not a {@link ValueLayout} layout.
     * @throws IllegalArgumentException if the layout path produced by {@code pathOperator} is not valid, if the carrier
     * does not represent a primitive type, if the carrier is {@code void}, {@code boolean}, or if the size of the carrier
     * type does not match that of the layout selected by the path produced by {@code pathOperator}.
     *
     * @apiNote the result var handle will feature an additional {@code long} access coordinate for every
     * unspecified sequence access component contained in this layout path.
     */
    default VarHandle dereferenceHandle(Class<?> carrier, UnaryOperator<Path> pathOperator) throws UnsupportedOperationException, IllegalArgumentException {
        return ((LayoutPathImpl)pathOperator.apply(LayoutPathImpl.rootPath(this))).dereferenceHandle(carrier);
    }

    /**
     * Instances of this class are used to model <em>layout paths</em>. A <em>layout path</em> originates from a <em>root</em>
     * layout element (typically a compound layout) and terminates at a layout element nested within the root layout - this is
     * the layout element <em>selected</em> by the layout path.
     * Layout paths are useful in order to e.g. to obtain offset of leaf elements inside arbitrarily nested compound layouts
     * (see {@link CompoundLayout#offset(UnaryOperator)}), or to quickly obtain a memory access handle corresponding to the selected
     * layout element (see {@link CompoundLayout#dereferenceHandle(Class, UnaryOperator)}).
     * <p>
     * Such <em>layout paths</em> can be constructed programmatically using the instance methods in this class.
     * For instance, given a layout constructed as follows:
     * <blockquote><pre>{@code
SequenceLayout seq = SequenceLayout.of(5,
    GroupLayout.struct(
        PaddingLayout.of(32),
        ValueLayout.ofSignedInt(32).withName("value")
    ));
     * }</pre></blockquote>
     *
     * We can obtain the offset of the layout element named <code>value</code> from <code>seq</code>, as follows:
     * <blockquote><pre>{@code
 long valueOffset = seq.offset(path -> path.sequenceElement().groupElement("value"));
     * }</pre></blockquote>
     *
     * Layout paths can feature one or more <em>free dimensions</em>. For instance, a layout path traversing
     * an unspecified sequence element (that is, as obtained with {@link CompoundLayout.Path#sequenceElement()}) features an additional
     * free dimension, which will have to be bound at runtime; that is, the memory access var handle associated with such a layout path
     * expression will feature an extra {@code long} access coordinate. The layout path constructed in the above example
     * features exactly one free dimension.
     *
     * @see MemoryAccessVarHandles
     */
    interface Path {

        /**
         * Returns a <em>nested</em> path which selects the layout element with given name from the group layout selected
         * by this path. The number of free dimensions of the resulting path is the same as the number of free
         * dimensions in this path.
         * @param name the index of the group element to be selected.
         * @return a path to the group element with given name.
         * @throws IllegalArgumentException if no sub-element with given name can be found in current path.
         * @throws IllegalStateException if this path does not select a {@link GroupLayout} layout.
         *
         * @implSpec in case multiple group elements with matching name exist, the first is returned; that is,
         * the group element with lowest offset from current path is returned.
         */
        Path groupElement(String name) throws IllegalArgumentException, IllegalStateException;

        /**
         * Returns a <em>nested</em> path which selects the layout element at the specified position from the sequence layout
         * selected by this path. The number of free dimensions of the resulting path is the same as the number of free
         * dimensions in this path.
         * @param index the index of the sequence element to be selected.
         * @return a layout path to the sequence element with given index.
         * @throws IllegalArgumentException if the index is &lt; 0, or if it is bigger than number of elements in the group.
         * @throws IllegalStateException if this path does not select a {@link SequenceLayout} layout.
         */
        Path sequenceElement(long index) throws IllegalArgumentException, IllegalStateException;

        /**
         * Returns a <em>nested</em> path which selects an unspecified layout element from the sequence layout
         * selected by this path. The number of free dimensions of the resulting path will be {@code 1 + n}, where {@code n}
         * is the number of free dimensions in the this path.
         * @return a layout path to an unspecified sequence element.
         * @throws IllegalStateException if this path does not select a {@link SequenceLayout} layout.
         */
        Path sequenceElement() throws IllegalStateException;
    }
}
