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
import jdk.internal.clang.CursorKind;
import jdk.internal.clang.Type;
import jdk.internal.clang.TypeKind;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static jdk.internal.jextract.impl.LayoutUtils.JEXTRACT_ANONYMOUS;

/**
 * Base class for C struct, union MemoryLayout computer helper classes.
 */
abstract class RecordLayoutComputer {
    // enclosing struct type (or this struct type for top level structs)
    final Type parent;
    // this struct type
    final Type type;
    // cursor of this struct
    final Cursor cursor;
    final List<MemoryLayout> fieldLayouts;

    private int anonCount = 0;

    RecordLayoutComputer(Type parent, Type type) {
        this.parent = parent;
        this.type = type;
        this.cursor = type.getDeclarationCursor().getDefinition();
        this.fieldLayouts = new ArrayList<>();
    }

    static MemoryLayout compute(long offsetInParent, Type parent, Type type) {
        Cursor cursor = type.getDeclarationCursor().getDefinition();
        if (cursor.isInvalid()) {
            return MemoryLayout.ofPaddingBits(64);
        }

        final boolean isUnion = cursor.kind() == CursorKind.UnionDecl;
        return isUnion? new UnionLayoutComputer(offsetInParent, parent, type).compute() :
                new StructLayoutComputer(offsetInParent, parent, type).compute();
    }

    final MemoryLayout compute() {
        Stream<Cursor> fieldCursors = Utils.flattenableChildren(cursor);
        for (Cursor fc : fieldCursors.collect(Collectors.toList())) {
            /*
             * Ignore bitfields of zero width.
             *
             * struct Foo {
             *     int i:0;
             * }
             *
             * And bitfields without a name.
             * (padding is computed automatically)
             */
            if (fc.isBitField() && (fc.getBitFieldWidth() == 0 || fc.spelling().isEmpty())) {
                startBitfield();
                continue;
            }

            processField(fc);
        }

        return finishLayout();
    }

    abstract void startBitfield();
    abstract void processField(Cursor c);
    abstract MemoryLayout finishLayout();

    void addFieldLayout(MemoryLayout MemoryLayout) {
        fieldLayouts.add(MemoryLayout);
    }

    void addFieldLayout(long offset, Type parent, Cursor c) {
        MemoryLayout memoryLayout = c.isAnonymousStruct()
            ? compute(offset, parent, c.type())
                .withName(nextAnonymousName())
                .withAttribute(JEXTRACT_ANONYMOUS, true)
            : fieldLayout(c);
        addFieldLayout(memoryLayout);
    }

    private String nextAnonymousName() {
        return "$anon$" + anonCount++;
    }

    MemoryLayout fieldLayout(Cursor c) {
        MemoryLayout l = LayoutUtils.getLayout(c.type());
        String name = LayoutUtils.getName(c);
        if (c.isBitField()) {
            MemoryLayout sublayout = MemoryLayout.ofValueBits(c.getBitFieldWidth(), ByteOrder.nativeOrder());
            return sublayout.withName(name);
        } else {
            return l.withName(name);
        }
    }

    long fieldSize(Cursor c) {
        if (c.type().kind() == TypeKind.IncompleteArray) {
            return 0;
        }
        return c.isBitField() ? c.getBitFieldWidth() : c.type().size() * 8;
    }

    MemoryLayout bitfield(List<MemoryLayout> sublayouts) {
        return MemoryLayout.ofStruct(sublayouts.toArray(new MemoryLayout[0])).withAttribute("BITFIELDS", true);
    }

    long offsetOf(Type parent, Cursor c) {
        if (c.kind() == CursorKind.FieldDecl) {
            return parent.getOffsetOf(c.spelling());
        } else {
            return Utils.flattenableChildren(c)
                    .mapToLong(child -> offsetOf(parent, child))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Can not find offset of: " + c + ", in: " + parent));
        }
    }
}
