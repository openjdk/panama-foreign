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

import java.foreign.NativeTypes;
import java.foreign.Scope;
import java.foreign.memory.Pointer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import clang.Index_h.CXTranslationUnitImpl;

public class Index {
    // Pointer to CXIndex
    private final Pointer<Void> ptr;
    // Set of TranslationUnit
    public final List<TranslationUnit> translationUnits;

    Index(Pointer<Void> ptr) {
        this.ptr = ptr;
        translationUnits = new ArrayList<>();
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

    public static class ParsingFailedException extends RuntimeException {
        private static final long serialVersionUID = -1L;
        private final Path srcFile;

        public ParsingFailedException(Path srcFile) {
            super("Failed to parse " + srcFile.toAbsolutePath().toString());
            this.srcFile = srcFile;
        }
    }

    public TranslationUnit parseTU(String file, int options, String... args)
    throws ParsingFailedException {
        final clang.Index_h lclang = LibClang.lib;

        try (Scope scope = Scope.globalScope().fork()) {
            Pointer<Byte> src = scope.allocateCString(file);
            Pointer<Pointer<Byte>> cargs = toCStrArray(scope, args);
            Pointer<CXTranslationUnitImpl> tu = lclang.clang_parseTranslationUnit(
                    ptr, src, cargs, args.length, Pointer.ofNull(), 0, options);

            if (tu == null || tu.isNull()) {
                throw new ParsingFailedException(Path.of(file).toAbsolutePath());
            }

            TranslationUnit rv = new TranslationUnit(tu);
            translationUnits.add(rv);
            return rv;
        }
    }

    private int defaultOptions(boolean detailedPreprocessorRecord) {
        int rv = LibClang.lib.CXTranslationUnit_ForSerialization();
        if (detailedPreprocessorRecord) {
            rv |= LibClang.lib.CXTranslationUnit_DetailedPreprocessingRecord();
        }
        return rv;
    }

    public TranslationUnit parse(String file, Consumer<Diagnostic> dh, boolean detailedPreprocessorRecord, String... args)
    throws ParsingFailedException {
        TranslationUnit tu = parse(file, detailedPreprocessorRecord, args);
        tu.processDiagnostics(dh);
        return tu;
    }

    public TranslationUnit parse(String file, boolean detailedPreprocessorRecord, String... args)
    throws ParsingFailedException {
        final clang.Index_h lclang = LibClang.lib;

        return parseTU(file, defaultOptions(detailedPreprocessorRecord), args);
    }

    public void dispose() {
        for (TranslationUnit tu: translationUnits) {
            tu.dispose();
        }
        LibClang.lib.clang_disposeIndex(ptr);
    }

    private static Pointer<Pointer<Byte>> toCStrArray(Scope sc, String[] ar) {
        if (ar.length == 0) {
            return Pointer.ofNull();
        }

        Pointer<Pointer<Byte>> ptr = sc.allocate(NativeTypes.UINT8.pointer(), ar.length);
        for (int i = 0; i < ar.length; i++) {
            Pointer<Byte> s = sc.allocateCString(ar[i]);
            ptr.offset(i).set(s);
        }

        return ptr;
    }
}
