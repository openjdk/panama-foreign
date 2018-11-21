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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Index {
    // Pointer to CXIndex
    final long ptr;
    // Set of TranslationUnit
    public final List<Long> translationUnits;

    Index(long ptr) {
        this.ptr = ptr;
        translationUnits = new ArrayList<>();
    }

    native void disposeIndex(long ptr);
    // parseTranslationUnit, return pointer(CXTranslationUnit)
    native long parseFile(long ptr, String file, boolean detailedPreprocessorRecord, String... args);
    static native void disposeTranslationUnit(long tu);
    static native Cursor getTranslationUnitCursor(long tu);
    static native Diagnostic[] getTranslationUnitDiagnostics(long tu);
    static native String[] tokenize(long translationUnit, SourceRange range);

    public Cursor parse(String file, Consumer<Diagnostic> eh, boolean detailedPreprocessorRecord, String... args) {
        long tu = parseFile(ptr, file, detailedPreprocessorRecord, args);
        if (tu != 0) {
            translationUnits.add(tu);
        }

        Diagnostic[] diags = getTranslationUnitDiagnostics(tu);
        if (null != diags) {
            for (Diagnostic diag: diags) {
                eh.accept(diag);
            }
        }

        return getTranslationUnitCursor(tu);
    }

    public void dispose() {
        for (long tu: translationUnits) {
            disposeTranslationUnit(tu);
        }
        disposeIndex(ptr);
    }
}
