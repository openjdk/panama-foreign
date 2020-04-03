/*
 *  Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.  Oracle designates this
 *  particular file as subject to the "Classpath" exception as provided
 *  by Oracle in the LICENSE file that accompanied this code.
 *
 *  This code is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  version 2 for more details (a copy is included in the LICENSE file that
 *  accompanied this code).
 *
 *  You should have received a copy of the GNU General Public License version
 *  2 along with this work; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *   Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 *
 */

package jdk.internal.clang;

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryHandles;
import jdk.incubator.foreign.MemorySegment;
import jdk.internal.clang.libclang.Index_h;

import java.lang.invoke.VarHandle;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static jdk.internal.jextract.impl.LayoutUtils.C_POINTER;

public class Index implements AutoCloseable {
    // Pointer to CXIndex
    private MemoryAddress ptr;
    // Set of TranslationUnit
    public final List<TranslationUnit> translationUnits;

    Index(MemoryAddress ptr) {
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
        private final ErrorCode code;

        public ParsingFailedException(Path srcFile, ErrorCode code) {
            super("Failed to parse " + srcFile.toAbsolutePath().toString() + ": " + code);
            this.srcFile = srcFile;
            this.code = code;
        }
    }

    private static final VarHandle VH_MemoryAddress =
            MemoryHandles.asAddressVarHandle(C_POINTER.varHandle(long.class));

    public TranslationUnit parseTU(String file, Consumer<Diagnostic> dh, int options, String... args)
    throws ParsingFailedException {
        try (MemorySegment src = Utils.toNativeString(file) ;
             MemorySegment cargs = Utils.toNativeStringArray(args);
             MemorySegment outAddress = MemorySegment.allocateNative(C_POINTER)) {
            ErrorCode code = ErrorCode.valueOf(Index_h.clang_parseTranslationUnit2(
                    ptr,
                    src.baseAddress(),
                    cargs == null ? MemoryAddress.NULL : cargs.baseAddress(),
                    args.length, MemoryAddress.NULL,
                    0,
                    options,
                    outAddress.baseAddress()));

            MemoryAddress tu = (MemoryAddress) VH_MemoryAddress.get(outAddress.baseAddress());
            TranslationUnit rv = new TranslationUnit(tu);
            // even if we failed to parse, we might still have diagnostics
            rv.processDiagnostics(dh);

            if (code != ErrorCode.Success) {
                throw new ParsingFailedException(Path.of(file).toAbsolutePath(), code);
            }

            translationUnits.add(rv);
            return rv;
        }
    }

    private int defaultOptions(boolean detailedPreprocessorRecord) {
        int rv = Index_h.CXTranslationUnit_ForSerialization;
        rv |= Index_h.CXTranslationUnit_SkipFunctionBodies;
        if (detailedPreprocessorRecord) {
            rv |= Index_h.CXTranslationUnit_DetailedPreprocessingRecord;
        }
        return rv;
    }

    public TranslationUnit parse(String file, Consumer<Diagnostic> dh, boolean detailedPreprocessorRecord, String... args)
    throws ParsingFailedException {
        return parseTU(file, dh, defaultOptions(detailedPreprocessorRecord), args);
    }

    public TranslationUnit parse(String file, boolean detailedPreprocessorRecord, String... args)
    throws ParsingFailedException {
        return parse(file, dh -> {}, detailedPreprocessorRecord, args);
    }

    @Override
    public void close() {
        dispose();
    }

    public void dispose() {
        for (TranslationUnit tu: translationUnits) {
            tu.dispose();
        }
        if (ptr != MemoryAddress.NULL) {
            Index_h.clang_disposeIndex(ptr);
        }
        ptr = MemoryAddress.NULL;
    }

}
