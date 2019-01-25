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
import java.foreign.layout.Unresolved;
import java.foreign.layout.Value;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jdk.internal.clang.Cursor;
import jdk.internal.clang.CursorKind;
import jdk.internal.clang.Type;
import jdk.internal.clang.TypeKind;

/**
 * Base class for C struct, union layout computer helper classes.
 */
abstract class RecordLayoutComputer {
    // enclosing struct type (or this struct type for top level structs)
    final Type parent;
    // this struct type
    final Type type;
    // cursor of this struct
    final Cursor cursor;
    final List<Layout> fieldLayouts;

    RecordLayoutComputer(Type parent, Type type) {
        this.parent = parent;
        this.type = type;
        this.cursor = type.getDeclarationCursor().getDefinition();
        this.fieldLayouts = new ArrayList<>();
    }

    static Layout compute(long offsetInParent, Type parent, Type type) {
        Cursor cursor = type.getDeclarationCursor().getDefinition();
        if (cursor.isInvalid()) {
            return LayoutUtils.getRecordReferenceLayout(type);
        }

        final boolean isUnion = cursor.kind() == CursorKind.UnionDecl;
        return isUnion? new UnionLayoutComputer(offsetInParent, parent, type).compute() :
                new StructLayoutComputer(offsetInParent, parent, type).compute();
    }

    private static boolean isFlattenable(Cursor c) {
        return c.isAnonymousStruct() || c.kind() == CursorKind.FieldDecl;
    }

    private static Optional<Cursor> lastChild(Cursor c) {
        List<Cursor> children = c.children()
                .filter(RecordLayoutComputer::isFlattenable)
                .collect(Collectors.toList());
        return children.isEmpty() ? Optional.empty() : Optional.of(children.get(children.size() - 1));
    }

    private static boolean hasIncompleteArray(Cursor c) {
        switch (c.kind()) {
            case FieldDecl:
                return c.type().kind() == TypeKind.IncompleteArray;
            case UnionDecl:
                return c.children()
                        .filter(RecordLayoutComputer::isFlattenable)
                        .anyMatch(RecordLayoutComputer::hasIncompleteArray);
            case StructDecl:
                return lastChild(c).map(RecordLayoutComputer::hasIncompleteArray).orElse(false);
            default:
                throw new IllegalStateException("Unhandled cursor kind: " + c.kind());
        }
    }

    final Layout compute() {
        if (hasIncompleteArray(cursor)) {
            throw new UnsupportedOperationException("Flexible array members not supported.");
        }
        Stream<Cursor> fieldCursors = cursor.children()
                .filter(RecordLayoutComputer::isFlattenable);
        for (Cursor fc : fieldCursors.collect(Collectors.toList())) {
            /*
             * Ignore bitfields of zero width.
             *
             * struct Foo {
             *     int i:0;
             * }
             */
            if (fc.isBitField() && fc.getBitFieldWidth() == 0) {
                continue;
            }

            processField(fc);
        }

        return finishLayout();
    }

    abstract void processField(Cursor c);
    abstract Layout finishLayout();

    void addFieldLayout(Layout layout) {
        fieldLayouts.add(layout);
    }

    void addFieldLayout(long offset, Type parent, Cursor c) {
        Layout layout = c.isAnonymousStruct()?
            compute(offset, parent, c.type()) :
            fieldLayout(c);
        addFieldLayout(layout);
    }

    Layout fieldLayout(Cursor c) {
        Layout layout = LayoutUtils.getLayout(c.type());
        String name = LayoutUtils.getName(c);
        if (c.isBitField()) {
            boolean isSigned = ((Value)layout).kind() == Value.Kind.INTEGRAL_SIGNED;
            Layout sublayout = isSigned ?
                Value.ofSignedInt(c.getBitFieldWidth()) :
                Value.ofUnsignedInt(c.getBitFieldWidth());
            return sublayout.withAnnotation(Layout.NAME, name);
        } else {
            return layout.withAnnotation(Layout.NAME, name);
        }
    }

    long fieldSize(Cursor c) {
        return c.isBitField()? c.getBitFieldWidth() : c.type().size() * 8;
    }

    Value bitfield(Value v, List<Layout> sublayouts) {
        return v.withContents(Group.struct(sublayouts.toArray(new Layout[0])));
    }

    long offsetOf(Type parent, Cursor c) {
        if (c.kind() == CursorKind.FieldDecl) {
            return parent.getOffsetOf(c.spelling());
        } else {
            return c.children()
                    .mapToLong(child -> offsetOf(parent, child))
                    .findFirst().orElseThrow(IllegalStateException::new);
        }
    }
}
