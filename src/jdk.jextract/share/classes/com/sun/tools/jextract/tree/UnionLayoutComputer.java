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
import java.util.List;
import jdk.internal.clang.Cursor;
import jdk.internal.clang.Type;

/**
 * Layout computer for C unions.
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
    Layout fieldLayout(Cursor c) {
        if (c.isBitField()) {
            Layout layout = LayoutUtils.getLayout(c.type());
            return bitfield((Value)layout, List.of(super.fieldLayout(c)));
        } else {
            return super.fieldLayout(c);
        }
    }

    @Override
    long fieldSize(Cursor c) {
        return c.type().size() * 8;
    }

    @Override
    Layout finishLayout() {
        // pad at the end, if any
        long expectedSize = type.size() * 8;
        if (actualSize < expectedSize) {
            addFieldLayout(Padding.of(expectedSize - actualSize));
        }

        Layout[] fields = fieldLayouts.toArray(new Layout[0]);
        Group g = Group.union(fields);
        return g.withAnnotation(Layout.NAME, LayoutUtils.getName(cursor));
    }
}
