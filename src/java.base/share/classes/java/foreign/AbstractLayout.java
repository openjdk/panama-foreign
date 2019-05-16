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

import jdk.internal.foreign.LayoutPathImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.stream.Collectors;

abstract class AbstractLayout<L extends AbstractLayout<L>> implements Layout {
    private final Map<String, String> attributes;
    private final OptionalLong alignment;

    public AbstractLayout(OptionalLong alignment, Map<String, String> attributes) {
        this.attributes = Collections.unmodifiableMap(attributes);
        this.alignment = alignment;
    }

    @Override
    public Optional<String> attribute(String name) {
        return Optional.ofNullable(attributes.get(name));
    }

    protected Map<String, String> attributes() {
        return attributes;
    }

    protected OptionalLong optAlignment() {
        return alignment;
    }

    @Override
    public L stripAnnotations() {
        return dup(alignment, NO_ANNOS);
    }

    @Override
    public L withAttribute(String name, String value) {
        Map<String, String> newAnnotations = new LinkedHashMap<>(attributes);
        newAnnotations.put(name, value);
        return dup(alignment, newAnnotations);
    }

    abstract L dup(OptionalLong alignment, Map<String, String> attributes);

    abstract long naturalAlignmentBits();

    @Override
    public final L alignTo(long alignmentBits) throws IllegalArgumentException {
        checkAlignment(alignmentBits);
        return dup(OptionalLong.of(alignmentBits), attributes);
    }

    private void checkAlignment(long alignmentBitCount) {
        if (alignmentBitCount <= 0 || //alignment must be positive
                ((alignmentBitCount & (alignmentBitCount - 1)) != 0L) || //alignment must be a power of two
                (alignmentBitCount < 8)) { //alignment must be greater than 8
            throw new IllegalArgumentException("Invalid alignment: " + alignmentBitCount);
        }
    }

    @Override
    public final long alignmentBits() {
        return alignment.orElse(naturalAlignmentBits());
    }

    String wrapWithAlignmentAndAttributes(String s) {
        if (!attributes.isEmpty()) {
            s = String.format("%s%s",
                    s, attributes.entrySet().stream()
                            .map(e -> !e.getKey().equals(NAME_ATTRIBUTE) ?
                                    String.format("(%s=%s)", e.getKey(), e.getValue()) :
                                    String.format("(%s)", e.getValue()))
                            .collect(Collectors.joining()));
        }
        if (alignment.isPresent()) {
            s = alignment.getAsLong() + "%" + s;
        }
        return s;
    }

    @Override
    public LayoutPath toPath() {
        if (isPartial()) {
            throw new UnsupportedOperationException("Cannot compute layout path of partial layout: " + this);
        }
        return LayoutPathImpl.of(this);
    }

    @Override
    public int hashCode() {
        return attributes.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof AbstractLayout)) {
            return false;
        }

        return attributes.equals(((AbstractLayout)other).attributes);
    }

    static final Map<String, String> NO_ANNOS = Collections.unmodifiableMap(new HashMap<>());
}
