/*
 *  Copyright (c) 2019, 2022, Oracle and/or its affiliates. All rights reserved.
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
package jdk.internal.foreign.layout;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.SequenceLayout;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static jdk.internal.foreign.layout.MemoryLayoutUtil.checkGetIndex;

public final class SequenceLayoutImpl extends AbstractLayout<SequenceLayoutImpl> implements SequenceLayout {

    private final long elemCount;
    private final MemoryLayout elementLayout;

    private SequenceLayoutImpl(long elemCount, MemoryLayout elementLayout) {
        this(elemCount, elementLayout, elementLayout.bitAlignment(), Optional.empty());
    }

    private SequenceLayoutImpl(long elemCount, MemoryLayout elementLayout, long bitAlignment, Optional<String> name) {
        super(Math.multiplyExact(elemCount, elementLayout.bitSize()), bitAlignment, name);
        this.elemCount = elemCount;
        this.elementLayout = elementLayout;
    }

    public MemoryLayout elementLayout() {
        return elementLayout;
    }

    public long elementCount() {
        return elemCount;
    }

    @Override
    public MemoryLayout elementAt(long index) {
        checkGetIndex(elemCount, index);
        return elementLayout;
    }

    @Override
    public Iterator<MemoryLayout> iterator() {
        return stream().iterator();
    }

    @Override
    public Stream<MemoryLayout> stream() {
        return LongStream.range(0, elemCount)
                .mapToObj(i -> elementLayout);
    }

    public SequenceLayout withElementCount(long elementCount) {
        MemoryLayoutUtil.checkSize(elementCount, true);
        return new SequenceLayoutImpl(elementCount, elementLayout, bitAlignment(), name());
    }

    public SequenceLayout reshape(long... elementCounts) {
        Objects.requireNonNull(elementCounts);
        if (elementCounts.length == 0) {
            throw new IllegalArgumentException();
        }
        SequenceLayout flat = flatten();
        long expectedCount = flat.elementCount();

        long actualCount = 1;
        int inferPosition = -1;
        for (int i = 0; i < elementCounts.length; i++) {
            if (elementCounts[i] == -1) {
                if (inferPosition == -1) {
                    inferPosition = i;
                } else {
                    throw new IllegalArgumentException("Too many unspecified element counts");
                }
            } else if (elementCounts[i] <= 0) {
                throw new IllegalArgumentException("Invalid element count: " + elementCounts[i]);
            } else {
                actualCount = elementCounts[i] * actualCount;
            }
        }

        // infer an unspecified element count (if any)
        if (inferPosition != -1) {
            long inferredCount = expectedCount / actualCount;
            elementCounts[inferPosition] = inferredCount;
            actualCount = actualCount * inferredCount;
        }

        if (actualCount != expectedCount) {
            throw new IllegalArgumentException("Element counts do not match expected size: " + expectedCount);
        }

        MemoryLayout res = flat.elementLayout();
        for (int i = elementCounts.length - 1; i >= 0; i--) {
            res = MemoryLayout.sequenceLayout(elementCounts[i], res);
        }
        return (SequenceLayoutImpl) res;
    }

    public SequenceLayout flatten() {
        long count = elementCount();
        MemoryLayout elemLayout = elementLayout();
        while (elemLayout instanceof SequenceLayoutImpl elemSeq) {
            count = count * elemSeq.elementCount();
            elemLayout = elemSeq.elementLayout();
        }
        return MemoryLayout.sequenceLayout(count, elemLayout);
    }

    @Override
    public String toString() {
        return decorateLayoutString(String.format("[%s:%s]",
                elemCount, elementLayout));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!super.equals(other)) {
            return false;
        }
        return other instanceof SequenceLayoutImpl otherSeq &&
                elemCount == otherSeq.elemCount &&
                elementLayout.equals(otherSeq.elementLayout);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), elemCount, elementLayout);
    }

    @Override
    SequenceLayoutImpl dup(long bitAlignment, Optional<String> name) {
        return new SequenceLayoutImpl(elementCount(), elementLayout, bitAlignment, name);
    }

    @Override
    public boolean hasNaturalAlignment() {
        return bitAlignment() == elementLayout.bitAlignment();
    }

    public static SequenceLayout of(long elementCount, MemoryLayout elementLayout) {
        return new SequenceLayoutImpl(elementCount, elementLayout);
    }

}
