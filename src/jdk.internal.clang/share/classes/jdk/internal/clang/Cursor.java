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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Cursor extends StructType {
    public static enum VisitResult {
        Break,
        Continue,
        Recurse;
    }

    @FunctionalInterface
    public static interface Visitor {
        /**
         * Callback function for visitChildren
         * @param parent
         * @param current
         * @param data
         * @return 0 - break, 1 - continue, 2 - recurse
         */
        VisitResult visit(Cursor current, Cursor parent, Object data);
    }

    private static int visit(Visitor v, ByteBuffer c, ByteBuffer p, Object data) {
        return v.visit(new Cursor(c), new Cursor(p), data).ordinal();
    }

    Cursor(ByteBuffer buf) {
        super(buf);
    }

    public native boolean isDeclaration();
    public native boolean isPreprocessing();
    public native boolean isInvalid();
    public native boolean isDefinition();

    // Determine whether the given cursor represents an anonymous record declaration.
    // "Anonymous" here is not just about name (spelling) being empty. This is an
    // anonymous struct or union embedded in another struct or union.
    public native boolean isAnonymousStruct();
    public native boolean isMacroFunctionLike();

    public native String spelling();
    public native String USR();

    public native int kind1();

    public native int visitChildren(Visitor visitor, Object data);

    public native Type type();
    public native Type getEnumDeclIntegerType();

    public native Cursor getDefinition();

    public native SourceLocation getSourceLocation();
    public native SourceRange getExtent();

    public native int numberOfArgs();
    public native Cursor getArgument(int idx);

    // C long long, 64-bit
    public native long getEnumConstantValue();
    // C unsigned long long, 64-bit
    public native long getEnumConstantUnsignedValue();

    public native boolean isBitField();
    public native int getBitFieldWidth();

    native long getTranslationUnit0();
    public final TranslationUnit getTranslationUnit() {
        return new TranslationUnit(getTranslationUnit0());
    }

    public native String getMangling();

    public CursorKind kind() {
        int v = kind1();
        // FIXME: assert(v == getData().getInt(0));
        return CursorKind.valueOf(v);
    }

    public Stream<Cursor> children() {
        ArrayList<Cursor> ar = new ArrayList<>();
        visitChildren((c, p, d) -> {
            @SuppressWarnings("unchecked")
            List<Cursor> a = (List<Cursor>) d;
            a.add(c);
            return VisitResult.Continue;
        }, ar);
        return ar.stream();
    }

    public Stream<Cursor> allChildren() {
        return children().flatMap(c -> Stream.concat(Stream.of(c), c.children()));
    }

    public native boolean equalCursor(Cursor other);

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Cursor)) {
            return false;
        }
        return equalCursor((Cursor)other);
    }

    @Override
    public int hashCode() {
        return spelling().hashCode();
    }
}
