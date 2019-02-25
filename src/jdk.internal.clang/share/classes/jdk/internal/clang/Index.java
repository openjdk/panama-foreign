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

import java.nio.file.Path;
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
    static native int saveTranslationUnit(long tu, String file);
    static native Cursor getTranslationUnitCursor(long tu);
    static native Diagnostic[] getTranslationUnitDiagnostics(long tu);
    static native String[] tokenize(long translationUnit, SourceRange range);
    static native int reparse0(long translationUnit, UnsavedFile[] files);

    public Cursor parse(String file, Consumer<Diagnostic> eh, boolean detailedPreprocessorRecord, String... args) {
        long tu = parseFile(ptr, file, detailedPreprocessorRecord, args);
        if (tu != 0) {
            translationUnits.add(tu);
        }

        processDiagnostics(eh, tu);

        return getTranslationUnitCursor(tu);
    }

    public static class UnsavedFile {
        final String file;
        final String contents;

        private UnsavedFile(Path path, String contents) {
            this.file = path.toAbsolutePath().toString();
            this.contents = contents;
        }

        public static UnsavedFile of(Path path, String contents) {
            return new UnsavedFile(path, contents);
        }
    }

    public void reparse(Consumer<Diagnostic> eh, TranslationUnit translationUnit, UnsavedFile... inMemoryFiles) {
        if (!translationUnits.contains(translationUnit.ptr)) {
            throw new IllegalStateException("Cannot reparse unknown translation unit!");
        }
        reparse0(translationUnit.ptr, inMemoryFiles);

        processDiagnostics(eh, translationUnit.ptr);
    }

    private void processDiagnostics(Consumer<Diagnostic> eh, long translationUnitPtr) {
        Diagnostic[] diags = getTranslationUnitDiagnostics(translationUnitPtr);
        if (null != diags) {
            for (Diagnostic diag: diags) {
                eh.accept(diag);
            }
        }
    }

    public void dispose() {
        for (long tu: translationUnits) {
            disposeTranslationUnit(tu);
        }
        disposeIndex(ptr);
    }
}
