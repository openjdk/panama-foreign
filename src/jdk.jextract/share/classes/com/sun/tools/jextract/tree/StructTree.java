/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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

import java.foreign.layout.Layout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import jdk.internal.clang.Cursor;
import jdk.internal.clang.CursorKind;

public class StructTree extends Tree {
    private final List<Tree> declarations;

    public StructTree(Cursor c, List<Tree> declarations) {
        super(c);
        this.declarations = Collections.unmodifiableList(declarations);
    }

    public List<Tree> declarations() {
        return declarations;
    }

    public List<FieldTree> fields() {
        // Note that we have to include fields from nested annoymous unions and structs
        // in the containing struct.
        List<FieldTree> fields = new ArrayList<>();
        for (Tree decl : declarations) {
            if (decl instanceof FieldTree) {
                fields.add((FieldTree)decl);
            } else if (decl instanceof StructTree) {
                StructTree s = (StructTree)decl;
                if (s.isAnonymous()) {
                    fields.addAll(s.fields());
                }
            }
        }
        return Collections.unmodifiableList(fields);
    }

    public List<Tree> nestedTypes() {
        // C structs and unions can have nested structs, unions and enums.
        // And (even deeply) nested types are hoisted to the containing scope.
        // i.e., nested structs/unions/enums are not scoped.
        List<Tree> nested = new ArrayList<>();
        for (Tree decl : declarations) {
            if (decl instanceof EnumTree) {
                nested.add(decl);
            } else if (decl instanceof StructTree) {
                StructTree s = (StructTree)decl;
                if (!s.isAnonymous()) {
                    nested.add(s);
                }
                nested.addAll(s.nestedTypes());
            }
        }
        return Collections.unmodifiableList(nested);
    }

    @Override
    public <R,D> R accept(TreeVisitor<R,D> visitor, D data) {
        return visitor.visitStruct(this, data);
    }

    public boolean isUnion() {
        return cursor().kind() == CursorKind.UnionDecl;
    }

    /**
     * Is this struct/union declared as anonymous member of another struct/union?
     *
     * Example:
     *
     *    struct X {
     *        struct { int i; int j; }; // <-- anonymous struct
     *        long l;
     *    };
     *
     * Note: this is specific use of the word 'anonymous'. A struct with name()
     * being empty is not necessarily anonymous in this usage.
     *
     * The structs in the following declarations are *not* anonymous eventhough
     * the names of these structs are empty.
     *
     *    struct { int i; int j; } thePoint;
     *    void func(struct { char* name; int len; } p1);
     *    typedef struct { int i; int j; } Point;
     */
    public boolean isAnonymous() {
        return cursor().isAnonymousStruct();
    }

    public Layout layout(BiFunction<FieldTree, Layout, Layout> fieldMapper) {
        TreeMaker m = new TreeMaker();
        return LayoutUtils.getRecordLayout(cursor().type(), (cursor, layout) -> {
            return fieldMapper.apply(m.createField(cursor), layout);
        });
    }
}
