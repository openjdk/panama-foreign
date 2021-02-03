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
import jdk.internal.clang.TypeKind;

import java.util.List;

/**
 * MemoryLayout computer for C unions.
 */
final class UnionLayoutComputer extends RecordLayoutComputer {
    private final long offset;
    private long actualSize = 0L;

    UnionLayoutComputer(long offsetInParent, Type parent, Type type) {
        super(parent, type);
        this.offset = offsetInParent;
    }

    @Override
    void processField(Cursor c) {
        long expectedOffset = offsetOf(parent, c);
        if (expectedOffset > offset) {
            throw new IllegalStateException("No padding in union elements!");
        }

        addFieldLayout(offset, parent, c);
        actualSize = Math.max(actualSize, fieldSize(c));
    }

    @Override
    void startBitfield() {
        // do nothing
    }

    @Override
    MemoryLayout fieldLayout(Cursor c) {
        if (c.isBitField()) {
            MemoryLayout layout = LayoutUtils.getLayout(c.type());
            return bitfield((ValueLayout) layout, List.of(super.fieldLayout(c)));
        } else {
            return super.fieldLayout(c);
        }
    }

    @Override
    long fieldSize(Cursor c) {
        if (c.type().kind() == TypeKind.IncompleteArray) {
            return 0;
        }
        return c.type().size() * 8;
    }

    @Override
    MemoryLayout finishLayout() {
        // size mismatch indicates anonymous bitfield used for padding
        long expectedSize = type.size() * 8;
        if (actualSize < expectedSize) {
            addFieldLayout(MemoryLayout.ofPaddingBits(expectedSize));
        }

        MemoryLayout[] fields = fieldLayouts.toArray(new MemoryLayout[0]);
        GroupLayout g = MemoryLayout.ofUnion(fields);
        String name = LayoutUtils.getName(cursor);
        return name.isEmpty() ?
                g : g.withName(name);
    }
}
