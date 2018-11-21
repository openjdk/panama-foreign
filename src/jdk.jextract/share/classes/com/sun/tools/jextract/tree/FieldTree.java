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

import java.util.Optional;
import jdk.internal.clang.Cursor;
import jdk.internal.clang.CursorKind;

public class FieldTree extends Tree {
    FieldTree(Cursor c) {
        this(c, c.spelling());
    }

    private FieldTree(Cursor c, String name) {
        super(c, name);
    }

    @Override
    public FieldTree withName(String newName) {
        return name().equals(newName)? this : new FieldTree(cursor(), newName);
    }

    @Override
    public <R,D> R accept(TreeVisitor<R,D> visitor, D data) {
        return visitor.visitField(this, data);
    }

    public boolean isBitField() {
        return cursor().isBitField();
    }

    public Optional<Integer> bitFieldWidth() {
        return isBitField()? Optional.of(cursor().getBitFieldWidth()) : Optional.empty();
    }

    public boolean isEnumConstant() {
        return cursor().kind() == CursorKind.EnumConstantDecl;
    }

    public Optional<Long> enumConstant() {
        return isEnumConstant()? Optional.of(cursor().getEnumConstantValue()) : Optional.empty();
    }
}
