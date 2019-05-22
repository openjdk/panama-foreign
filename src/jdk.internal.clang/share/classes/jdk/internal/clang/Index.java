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
    public final List<TranslationUnit> translationUnits;

    Index(long ptr) {
        this.ptr = ptr;
        translationUnits = new ArrayList<>();
    }

    private native void disposeIndex(long ptr);
    // parseTranslationUnit, return pointer(CXTranslationUnit)
    private native long parseFile(long ptr, String file, int options, String... args);

    private final static int CXTranslationUnit_DetailedPreprocessingRecord = 0x01;
    private final static int CXTranslationUnit_ForSerialization = 0x10;

    private final static int defaultOption(boolean detailedPreprocessorRecord) {
        int rv = CXTranslationUnit_ForSerialization;
        if (detailedPreprocessorRecord) {
            rv |= CXTranslationUnit_DetailedPreprocessingRecord;
        }
        return rv;
    }

    public static class ParsingFailedException extends RuntimeException {
        private static final long serialVersionUID = -1L;
        private final Path srcFile;

        public ParsingFailedException(Path srcFile) {
            super("Failed to parse " + srcFile.toAbsolutePath().toString());
            this.srcFile = srcFile;
        }
    }

    public TranslationUnit parse(String file, Consumer<Diagnostic> dh, boolean detailedPreprocessorRecord, String... args)
            throws ParsingFailedException {
        TranslationUnit tu = parse(file, detailedPreprocessorRecord, args);
        tu.processDiagnostics(dh);
        return tu;
    }

    public TranslationUnit parse(String file, boolean detailedPreprocessorRecord, String... args)
            throws ParsingFailedException {
        long tuPtr = parseFile(ptr, file, defaultOption(detailedPreprocessorRecord), args);
        if (tuPtr == 0) {
            throw new ParsingFailedException(Path.of(file).toAbsolutePath());
        }

        TranslationUnit tu = new TranslationUnit(tuPtr);
        translationUnits.add(tu);
        return tu;
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

    public void dispose() {
        for (TranslationUnit tu: translationUnits) {
            tu.dispose();
        }
        disposeIndex(ptr);
    }
}
