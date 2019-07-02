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

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

abstract class AbstractLayout implements Layout {
    private final OptionalLong alignment;
    private final Optional<String> name;

    public AbstractLayout(OptionalLong alignment, Optional<String> name) {
        this.alignment = alignment;
        this.name = name;
    }

    OptionalLong optAlignment() {
        return alignment;
    }

    Optional<String> optName() {
        return name;
    }

    @Override
    public AbstractLayout withName(String name) {
        return dup(alignment, Optional.of(name));
    }

    @Override
    public final Optional<String> name() {
        return name;
    }

    abstract AbstractLayout dup(OptionalLong alignment, Optional<String> name);

    abstract long naturalAlignmentBits();

    @Override
    public AbstractLayout alignTo(long alignmentBits) throws IllegalArgumentException {
        checkAlignment(alignmentBits);
        return dup(OptionalLong.of(alignmentBits), name);
    }

    void checkAlignment(long alignmentBitCount) {
        if (alignmentBitCount <= 0 || //alignment must be positive
                ((alignmentBitCount & (alignmentBitCount - 1)) != 0L) || //alignment must be a power of two
                (alignmentBitCount < 8)) { //alignment must be greater than 8
            throw new IllegalArgumentException("Invalid alignment: " + alignmentBitCount);
        }
    }

    static void checkSize(long size) {
        checkSize(size, false);
    }

    static void checkSize(long size, boolean includeZero) {
        if (size < 0 || (!includeZero && size == 0)) {
            throw new IllegalArgumentException("Invalid size for layout: " + size);
        }
    }

    @Override
    public final long bitsAlignment() {
        return alignment.orElse(naturalAlignmentBits());
    }

    String decorateLayoutString(String s) {
        if (name.isPresent()) {
            s = String.format("%s(%s)", s, name.get());
        }
        if (alignment.isPresent()) {
            s = alignment.getAsLong() + "%" + s;
        }
        return s;
    }

    @Override
    public int hashCode() {
        return name.hashCode() << alignment.orElse(0L);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof AbstractLayout)) {
            return false;
        }

        return Objects.equals(name, ((AbstractLayout)other).name) &&
                Objects.equals(alignment, ((AbstractLayout)other).alignment);
    }
}
