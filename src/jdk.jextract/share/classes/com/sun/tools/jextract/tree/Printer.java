/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
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

import jdk.internal.clang.Cursor;
import jdk.internal.clang.SourceLocation;
import jdk.internal.clang.Type;
import jdk.internal.clang.TypeKind;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

public final class Printer {
    private int level = 0;
    private final PrintStream ps;

    public Printer() {
        this(System.out);
    }

    public Printer(PrintStream ps) {
        this.ps = ps;
    }

    private void println(String str) {
        println(ps, str);
    }

    void println(PrintStream ps, String str) {
        int l = level;
        while (l > 0) {
            ps.append("  ");
            l--;
        }
        ps.println(str);
    }

    void showCursorLocation(Cursor c) {
        if (!c.isInvalid()) {
            SourceLocation src = c.getSourceLocation();
            if (null != src) {
                SourceLocation.Location loc = src.getFileLocation();
                println("Location: " + loc.path() + "@" + loc.line() + ":" + loc.column());
                println("Is system header? " + src.isInSystemHeader());
                println("Is from main file? " + src.isFromMainFile());
            }
        }
    }

    public void dumpType(Type t) {
        println("Type: " + t.spelling());
        try {
            println("Type Kind: " + t.kind().name());
        } catch (NoSuchElementException ex) {
            println("Type kind unknown: " + t.kind1());
        }
        Cursor c = t.getDeclarationCursor();
        println("Declared by cursor: " + c.spelling() + " of kind " + c.kind());
        showCursorLocation(c);
        if (!c.isInvalid()) {
            println("Declaring cursor is definition? " + c.isDefinition());
            if (!c.isDefinition()) {
                Cursor dc = c.getDefinition();
                println("Definition cursor is " + dc.spelling() + " of kind " + dc.kind());
                showCursorLocation(dc);
            }
        }
        println("Size: " + t.size() + " bytes");
        if (t.kind() == TypeKind.Typedef || t.kind() == TypeKind.Unexposed) {
            Type canonicalType = t.canonicalType();
            println("Canonical Type: " + canonicalType.spelling());
            println("CType Kind: " + canonicalType.kind().name());
            println("== Canonical Type ==");
            dumpType(canonicalType, true);
        } else if (t.kind() == TypeKind.ConstantArray || t.kind() == TypeKind.IncompleteArray) {
            Type etype = t.getElementType();
            println("Element Type: " + etype.spelling());
            println("Element Type Kind: " + etype.kind().name());
            println("== Element Type ==");
            dumpType(etype, true);

        }
    }

    public void dumpType(Type t, final boolean indent) {
        try {
            if (indent) {
                level++;
            }
            dumpType(t);
        } finally {
            if (indent) {
                level--;
            }
        }
    }

    void dumpCursor(Cursor c) {
        println("------------------------");
        println("Cursor: " + c.spelling());
        try {
            println("Kind: " + c.kind().name());
        } catch (NoSuchElementException ex) {
            println("Cursor kind unknown: " + c.kind1());
        }
        if (! c.isInvalid()) {
            SourceLocation src = c.getSourceLocation();
            if (null != src) {
                SourceLocation.Location loc = src.getFileLocation();
                println("Location: " + loc.path() + "@" + loc.line() + ":" + loc.column());
                println("Is system header? " + src.isInSystemHeader());
                println("Is from main file? " + src.isFromMainFile());
            }
            println("== Type ==");
            Type t = c.type();
            dumpType(t);
            println("Is this cursor definition? " + c.isDefinition());
            if (! c.isDefinition()) {
                Cursor dc = c.getDefinition();
                println("== Definition Cursor ==");
                dumpCursor(dc, true);
            }
        }
    }

    void dumpCursor(Cursor c, final boolean indent) {
        try {
            if (indent) {
                level++;
            }
            dumpCursor(c);
        } finally {
            if (indent) {
                level--;
            }
        }
    }

    public void printRecursive(Tree tree, int depth) {
        printRecursive(tree.cursor(), depth);
    }

    void printRecursive(Cursor c, int depth) {
        if (depth == 0) {
            return;
        }

        try {
            dumpCursor(c);
            println("+--->");
            level++;
            c.children()
             .forEachOrdered(cx -> printRecursive(cx, depth - 1));
        } finally {
            level--;
        }
    }

    public static String Stringifier(Consumer<Printer> print) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bos);
        Printer p = new Printer(ps);
        print.accept(p);
        ps.close();
        return bos.toString();
    }
}
