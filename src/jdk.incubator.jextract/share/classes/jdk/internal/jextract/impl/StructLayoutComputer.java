/*
 *  Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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

package jdk.internal.jextract.impl;

import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.ValueLayout;
import jdk.internal.clang.Cursor;
import jdk.internal.clang.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * MemoryLayout computer for C structs.
 */
final class StructLayoutComputer extends RecordLayoutComputer {
    private long offset;
    private long actualSize = 0L;
    // List to collect bitfield fields to process later, may be null
    private List<MemoryLayout> bitfieldLayouts;

    StructLayoutComputer(long offsetInParent, Type parent, Type type) {
        super(parent, type);
        this.offset = offsetInParent;
    }

    @Override
    void addFieldLayout(MemoryLayout MemoryLayout) {
        if (bitfieldLayouts != null) {
            bitfieldLayouts.add(MemoryLayout);
        } else {
            fieldLayouts.add(MemoryLayout);
        }
    }

    @Override
    void startBitfield() {
        /*
         * In a struct, a bitfield field is seen after a non-bitfield.
         * Initialize bitfieldLayouts list to collect this and subsequent
         * bitfield layouts.
         */
        if (bitfieldLayouts == null) {
            bitfieldLayouts = new ArrayList<>();
        }
    }

    @Override
    void processField(Cursor c) {
        boolean isBitfield = c.isBitField();
        long expectedOffset = offsetOf(parent, c);
        if (expectedOffset > offset) {
            addFieldLayout(MemoryLayout.ofPaddingBits(expectedOffset - offset));
            actualSize += (expectedOffset - offset);
            offset = expectedOffset;
        }

        if (isBitfield) {
            startBitfield();
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
    MemoryLayout finishLayout() {
        // pad at the end, if any
        long expectedSize = type.size() * 8;
        if (actualSize < expectedSize) {
            addFieldLayout(MemoryLayout.ofPaddingBits(expectedSize - actualSize));
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

        MemoryLayout[] fields = fieldLayouts.toArray(new MemoryLayout[0]);
        GroupLayout g = MemoryLayout.ofStruct(fields);
        String name = LayoutUtils.getName(cursor);
        return name.isEmpty() ?
                g : g.withName(name);
    }

    // process bitfields if any and clear bitfield layouts
    private void handleBitfields() {
        if (bitfieldLayouts != null) {
            fieldLayouts.addAll(convertBitfields(bitfieldLayouts));
            bitfieldLayouts = null;
        }
    }

    private List<MemoryLayout> convertBitfields(List<MemoryLayout> layouts) {
        long offset = 0L;
        List<MemoryLayout> newFields = new ArrayList<>();
        List<MemoryLayout> pendingFields = new ArrayList<>();
        for (MemoryLayout l : layouts) {
            offset += l.bitSize();
            if (offset > MAX_STORAGE_SIZE) {
                throw new IllegalStateException("Crossing storage unit boundaries");
            }
            pendingFields.add(l);
            long storageSize = storageSize(offset);
            if (!pendingFields.isEmpty() && storageSize != -1) {
                //emit new
                newFields.add(bitfield(storageSize, pendingFields));
                pendingFields.clear();
                offset = 0L;
            }
        }
        if (!pendingFields.isEmpty()) {
            long storageSize = nextStorageSize(offset);
            //emit new
            newFields.add(bitfield(storageSize, pendingFields));
            pendingFields.clear();
        }
        return newFields;
    }

    static int[] STORAGE_SIZES = { 64, 32, 16, 8 };
    static int[] ALIGN_SIZES = { 8, 16, 32, 64 };
    static int MAX_STORAGE_SIZE = 64;

    private long storageSize(long size) {
        // offset should be < MAX_STORAGE_SIZE
        for (int s : STORAGE_SIZES) {
            if (size == s) {
                return s;
            }
        }
        return -1;
    }

    private long nextStorageSize(long size) {
        // offset should be < MAX_STORAGE_SIZE
        for (int s : ALIGN_SIZES) {
            long alignedSize = alignUp(size, s);
            long storageSize = storageSize(alignedSize);
            if (storageSize != -1) {
                return storageSize;
            }
        }
        return -1;
    }

    private static long alignUp(long n, long alignment) {
        return (n + alignment - 1) & -alignment;
    }
}
