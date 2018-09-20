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

package jdk.internal.clang;

import clang.*;
import clang.Index;

import static clang.Index.CXCursor;
import static clang.Index.CXSourceLocation;
import static clang.Index.CXSourceRange;

import java.foreign.memory.Pointer;
import java.foreign.Scope;
import java.util.ArrayList;
import java.util.stream.Stream;

public class Cursor {

    private final CXCursor cursor;
    private final int kind;

    Cursor(CXCursor cursor) {
        this.cursor = cursor;
        kind = LibClang.lib.clang_getCursorKind(cursor);
    }
    public boolean isDeclaration() {
        return LibClang.lib.clang_isDeclaration(kind) != 0;
    }

    public boolean isPreprocessing() { return LibClang.lib.clang_isPreprocessing(kind) != 0; }

    public boolean isInvalid() {
        return LibClang.lib.clang_isInvalid(kind) != 0;
    }

    public boolean isDefinition() {
        return LibClang.lib.clang_isCursorDefinition(cursor) != 0;
    }

    public boolean isAnonymousStruct() { return LibClang.lib.clang_Cursor_isAnonymous(cursor) != 0; }

    public boolean isMacroFunctionLike() {
        return LibClang.lib.clang_Cursor_isMacroFunctionLike(cursor) != 0;
    }

    public String spelling() {
        return LibClang.CXStrToString(
                LibClang.lib.clang_getCursorSpelling(cursor));
    }

    public String USR() {
        return LibClang.CXStrToString(
                LibClang.lib.clang_getCursorUSR(cursor));
    }

    public boolean equalCursor(Cursor other) {
        return LibClang.lib.clang_equalCursors(cursor, other.cursor) != 0;
    }

    public Type type() {
        return new Type(LibClang.lib.clang_getCursorType(cursor));
    }

    public Type getEnumDeclIntegerType() {
        return new Type(LibClang.lib.clang_getEnumDeclIntegerType(cursor));
    }

    public Cursor getDefinition() {
        return new Cursor(LibClang.lib.clang_getCursorDefinition(cursor));
    }

    public SourceLocation getSourceLocation() {
        CXSourceLocation loc = LibClang.lib.clang_getCursorLocation(cursor);
        if (LibClang.lib.clang_equalLocations(loc, LibClang.lib.clang_getNullLocation()) != 0) {
            return null;
        }
        return new SourceLocation(loc);
    }

    public SourceRange getExtent() {
        CXSourceRange range = LibClang.lib.clang_getCursorExtent(cursor);
        if (LibClang.lib.clang_Range_isNull(range) != 0) {
            return null;
        }
        return new SourceRange(range);
    }

    public int numberOfArgs() {
        return LibClang.lib.clang_Cursor_getNumArguments(cursor);
    }

    public Cursor getArgument(int idx) {
        return new Cursor(LibClang.lib.clang_Cursor_getArgument(cursor, idx));
    }

    // C long long, 64-bit
    public long getEnumConstantValue() {
        return LibClang.lib.clang_getEnumConstantDeclValue(cursor);
    }

    // C unsigned long long, 64-bit
    public long getEnumConstantUnsignedValue() {
        return LibClang.lib.clang_getEnumConstantDeclUnsignedValue(cursor);
    }

    public boolean isBitField() {
        return LibClang.lib.clang_Cursor_isBitField(cursor) != 0;
    }

    public int getBitFieldWidth() {
        return LibClang.lib.clang_getFieldDeclBitWidth(cursor);
    }

    public CursorKind kind() {
        return CursorKind.valueOf(kind);
    }

    public Stream<Cursor> children() {
        final ArrayList<Cursor> ar = new ArrayList<>();
        // FIXME: need a way to pass ar down as user data d
        try (Scope sc = Scope.newNativeScope()) {
            LibClang.lib.clang_visitChildren(cursor, sc.allocateCallback((c, p, d) -> {
                ar.add(new Cursor(c));
                return LibClang.lib.CXChildVisit_Continue();
            }), Pointer.nullPointer());
            return ar.stream();
        }
    }

    public Stream<Cursor> allChildren() {
        return children().flatMap(c -> Stream.concat(Stream.of(c), c.children()));
    }

    public String getMangling() {
        return LibClang.CXStrToString(
                LibClang.lib.clang_Cursor_getMangling(cursor));
    }

    public TranslationUnit getTranslationUnit() {
        return new TranslationUnit(LibClang.lib.clang_Cursor_getTranslationUnit(cursor));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Cursor)) {
            return false;
        }
        return (LibClang.lib.clang_equalCursors(cursor, ((Cursor)other).cursor) != 0);
    }

    @Override
    public int hashCode() {
        return spelling().hashCode();
    }
}
