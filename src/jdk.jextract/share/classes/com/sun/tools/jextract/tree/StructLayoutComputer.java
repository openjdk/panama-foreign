/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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

package com.sun.tools.jextract.tree;

import java.foreign.layout.Group;
import java.foreign.layout.Layout;
import java.foreign.layout.Padding;
import java.foreign.layout.Value;
import java.util.ArrayList;
import java.util.List;
import jdk.internal.clang.Cursor;
import jdk.internal.clang.Type;

/**
 * Layout computer for C structs.
 */
final class StructLayoutComputer extends RecordLayoutComputer {
    private long offset;
    private long actualSize = 0L;
    // List to collect bitfield fields to process later, may be null
    private List<Layout> bitfieldLayouts;

    StructLayoutComputer(long offsetInParent, Type parent, Type type) {
        super(parent, type);
        this.offset = offsetInParent;
    }

    @Override
    void addFieldLayout(Layout layout) {
        if (bitfieldLayouts != null) {
            bitfieldLayouts.add(layout);
        } else {
            fieldLayouts.add(layout);
        }
    }

    @Override
    void processField(Cursor c) {
        boolean isBitfield = c.isBitField();
        long expectedOffset = offsetOf(parent, c);
        if (expectedOffset > offset) {
            addFieldLayout(Padding.of(expectedOffset - offset));
            actualSize += (expectedOffset - offset);
            offset = expectedOffset;
        }

        if (isBitfield) {
            /*
             * In a struct, a bitfield field is seen after a non-bitfield.
             * Initialize bitfieldLayouts list to collect this and subsequent
             * bitfield layouts.
             */
             if (bitfieldLayouts == null) {
                 bitfieldLayouts = new ArrayList<>();
             }
        } else { // !isBitfield
            /*
             * We may be crossing from bit fields to non-bitfield field.
             *
             * struct Foo {
             *     int i:12;
             *     int j:20;
             *     int k; // <-- processing this
             *     int m;
             * }
             */
             handleBitfields();
        }

        addFieldLayout(offset, parent, c);
        long size = fieldSize(c);
        offset += size;
        actualSize += size;
    }

    @Override
    Layout finishLayout() {
        // pad at the end, if any
        long expectedSize = type.size() * 8;
        if (actualSize < expectedSize) {
            addFieldLayout(Padding.of(expectedSize - actualSize));
        }

        /*
         * Handle bitfields at the end, if any.
         *
         * struct Foo {
         *     int i,j, k;
         *     int f:10;
         *     int pad:12;
         * }
         */
        handleBitfields();

        Layout[] fields = fieldLayouts.toArray(new Layout[0]);
        Group g = Group.struct(fields);
        return g.withAnnotation(Layout.NAME, LayoutUtils.getName(cursor));
    }

    // process bitfields if any and clear bitfield layouts
    private void handleBitfields() {
        if (bitfieldLayouts != null) {
            fieldLayouts.addAll(convertBitfields(bitfieldLayouts));
            bitfieldLayouts = null;
        }
    }

    private List<Layout> convertBitfields(List<Layout> layouts) {
        long storageSize = storageSize(layouts);
        long offset = 0L;
        List<Layout> newFields = new ArrayList<>();
        List<Layout> pendingFields = new ArrayList<>();
        for (Layout l : layouts) {
            offset += l.bitsSize();
            pendingFields.add(l);
            if (!pendingFields.isEmpty() && offset == storageSize) {
                //emit new
                newFields.add(bitfield(Value.ofUnsignedInt(storageSize), pendingFields));
                pendingFields.clear();
                offset = 0L;
            } else if (offset > storageSize) {
                throw new IllegalStateException("Crossing storage unit boundaries");
            }
        }
        if (!pendingFields.isEmpty()) {
            throw new IllegalStateException("Partially used storage unit");
        }
        return newFields;
    }

    private long storageSize(List<Layout> layouts) {
        long size = layouts.stream().mapToLong(Layout::bitsSize).sum();
        int[] sizes = { 64, 32, 16, 8 };
        for (int s : sizes) {
            if (size % s == 0) {
                return s;
            }
        }
        throw new IllegalStateException("Cannot infer storage size");
    }
}
