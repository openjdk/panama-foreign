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

import jdk.internal.foreign.Utils;

import java.util.Optional;

/**
 * A layout can be used to describe the contents of a memory segment in a <em>language neutral</em> fashion.
 * There are two leaves in the layout hierarchy, <em>value layouts</em>, which are used to represent values of given size and kind (see
 * {@link ValueLayout}) and <em>padding layouts</em> which are used, as the name suggests, to represent a portion of a memory
 * segment whose contents should be ignored, and which are primarily present for alignment reasons (see {@link PaddingLayout}).
 * <p>
 * More complex layouts can be derived from simpler ones: a <em>sequence layout</em> denotes a repetiton of one or more
 * layout element (see {@link SequenceLayout}); a <em>group layout</em> denotes an aggregation of (typically) heterogeneous
 * layout elements (see {@link GroupLayout}).
 *
 * <h2>Size, alignment and byte order</h2>
 *
 * All layouts have a size; layout size for value and padding layouts is always explicitly denoted; this means that a layout description
 * has always the same size in bits, regardless of the platform in which it is used. For derived layouts, the size is computed
 * as follows:
 * <ul>
 *     <li>for a <em>finite</em> sequence layout <em>S</em> whose layout element is <em>E</em> and size is L,
 *     the size of <em>S</em> is that of <em>E, multiplied by L</em></li>
 *     <li>the size of an <em>unbounded</em> sequence layout is <em>unknown</em></li>
 *     <li>for a group layout <em>G</em> with sub-elements <em>E1</em>, <em>E2</em>, ... <em>En</em> whose sizes are
 *     <em>S1</em>, <em>S2</em>, ... <em>Sn</em>, respectively, the size of <em>G</em> is either <em>S1 + S2 + ... + Sn</em> or
 *     <em>max(S1, S2, ... Sn)</em> depending on whether the group is a <em>struct</em> or an <em>union</em>, respectively</li>
 * </ul>
 * <p>
 * Furthermore, all layouts feature a <em>natural alignment</em> which can be inferred as follows:
 * <ul>
 *     <li>for value and padding layout <em>L</em> whose size is <em>N</em>, the natural alignment of <em>L</em> is <em>N</em></li>
 *     <li>for a sequence layout <em>S</em> whose layout element is <em>E</em>, the natural alignment of <em>S</em> is that of <em>E</em></li>
 *     <li>for a group layout <em>G</em> with sub-elements <em>E1</em>, <em>E2</em>, ... <em>En</em> whose alignments are
 *     <em>A1</em>, <em>A2</em>, ... <em>An</em>, respectively, the natural alignment of <em>G</em> is <em>max(A1, A2 ... An)</em></li>
 * </ul>
 * A layout's natural alignment can be overridden if needed (see {@link Layout#alignTo(long)}), which can be useful to describe
 * hyper-aligned layouts.
 * <p>
 * Where it's not explicitly provided, the byte order of value layouts is assumed to be compatible with the
 * platform byte order (see {@link java.nio.ByteOrder#nativeOrder()}).
 */
public interface Layout {

    /**
     * Computes the layout size, in bits.
     * @return the layout size, in bits.
     * @throws UnsupportedOperationException if the layout has unbounded size (see {@link SequenceLayout}).
     */
    long bitsSize() throws UnsupportedOperationException;

    /**
     * Computes the layout size, in bytes.
     * @return the layout size, in bytes.
     * @throws UnsupportedOperationException if the layout has unbounded size (see {@link SequenceLayout}),
     * or if the bits size (see {@link Layout#bitsSize()} is not a multiple of 8.
     */
    default long bytesSize() throws UnsupportedOperationException {
        return Utils.bitsToBytesOrThrow(bitsSize(),
                () -> new UnsupportedOperationException("Cannot compute byte size; bit size is not a multiple of 8"));
    }

    /**
     * Return the <em>name</em> (if any) associated with this layout.
     * @return the layout <em>name</em> (if any).
     * @see Layout#withName(String)
     */
    Optional<String> name();

    /**
     * Attach a <em>name</em> to this layout.
     * @param name the layout name.
     * @return a new layout which is the same as this layout, except for the <em>name</em> associated to it.
     * @see Layout#name()
     */
    Layout withName(String name);

    /**
     * Returns the alignment constraints associated with this layout, expressed in bits. Layout alignment defines a power
     * of two A which is the bitwise alignment of the layout. If A&gt;=8 then A/8 is the number of bytes that must be aligned
     * for any pointer that correctly points to this layout. Thus:
     *
     * <ul>
     * <li>A=8 means unaligned (in the usual sense), which is common in packets.</li>
     * <li>A=64 means word aligned (on LP64), A=32 int aligned, A=16 short aligned, etc.</li>
     * <li>A=512 is the most strict alignment required by the x86/SV ABI (for AVX-512 data).</li>
     * </ul>
     *
     * @return the layout alignment constraint, in bits.
     */
    long bitsAlignment();

    /**
     * Returns the alignment constraints associated with this layout, expressed in bytes. Layout alignment defines a power
     * of two A which is the bytewise alignment of the layout, where A is the number of bytes that must be aligned
     * for any pointer that correctly points to this layout. Thus:
     *
     * <ul>
     * <li>A=1 means unaligned (in the usual sense), which is common in packets.</li>
     * <li>A=8 means word aligned (on LP64), A=4 int aligned, A=2 short aligned, etc.</li>
     * <li>A=64 is the most strict alignment required by the x86/SV ABI (for AVX-512 data).</li>
     * </ul>
     *
     * @return the layout alignment constraint, in bytes.
     */
    default long bytesAlignment() {
        return Utils.bitsToBytesOrThrow(bitsAlignment(),
                () -> new UnsupportedOperationException("Cannot compute byte alignment; bit alignment is not a multiple of 8"));
    }

    /**
     * Creates a new layout which features the desired alignment.
     *
     * @param alignmentBits the layout alignment, expressed in bits.
     * @return a new layout which is the same as this layout, except for the alignment associated to it.
     * @throws IllegalArgumentException if the supplied alignment is not a power of two, or if it's lower than 8.
     */
    Layout alignTo(long alignmentBits) throws IllegalArgumentException;

    /**
     * Compares the specified object with this layout for equality. Returns {@code true} if and only if the specified
     * object is also a layout, and it is equal to this layout.
     *
     * @param that the object to be compared for equality with this layout.
     * @return {@code true} if the specified object is equal to this layout.
     */
    boolean equals(Object that);

    /**
     * Returns the hash code value for this layout.
     * @return the hash code value for this layout.
     */
    int hashCode();

    /**
     * Returns a string representation of this layout.
     * @return a string representation of this layout.
     */
    @Override
    String toString();
}
