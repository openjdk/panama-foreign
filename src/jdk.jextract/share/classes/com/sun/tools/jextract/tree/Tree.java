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

import java.util.Objects;
import jdk.internal.clang.Cursor;
import jdk.internal.clang.Type;
import jdk.internal.clang.SourceLocation;
import jdk.internal.clang.SourceRange;

public class Tree {
    private final Cursor c;
    private final String name;

    Tree(Cursor c) {
        this(c, c.spelling());
    }

    Tree(Cursor c, String name) {
        this.c = Objects.requireNonNull(c);
        this.name = Objects.requireNonNull(name);
    }

    public final Cursor cursor() {
        return c;
    }

    public final Type type() {
        return c.type();
    }

    public Tree withName(String newName) {
        return name.equals(newName)? this : new Tree(c, newName);
    }

    public final String name() {
        return name;
    }

    public final SourceLocation location() {
        return c.getSourceLocation();
    }

    public final String USR() {
        return c.USR();
    }

    public final boolean isDeclaration() {
        return c.isDeclaration();
    }

    public final boolean isInvalid() {
        return c.isInvalid();
    }

    public final boolean isPreprocessing() {
        return c.isPreprocessing();
    }

    public final boolean isDefinition() {
        return c.isDefinition();
    }

    public final boolean isFromMain() {
        return location().isFromMainFile();
    }

    public final boolean isFromSystem() {
        return location().isInSystemHeader();
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Tree)) {
            return false;
        }

        Tree t = (Tree)obj;
        return name.equals(t.name()) && location().equals(t.location());
    }

    @Override
    public final int hashCode() {
        return name.hashCode() ^ location().hashCode();
    }

    @Override
    public final String toString() {
        return Printer.Stringifier(p -> p.dumpCursor(c, true));
    }

    public <R,D> R accept(TreeVisitor<R,D> visitor, D data) {
        return visitor.visitTree(this, data);
    }
}
