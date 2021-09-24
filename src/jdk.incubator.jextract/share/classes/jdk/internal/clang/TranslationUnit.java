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

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;
import jdk.incubator.foreign.SegmentAllocator;
import jdk.internal.clang.libclang.CXToken;
import jdk.internal.clang.libclang.Index_h;
import jdk.internal.clang.libclang.CXUnsavedFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;

import static jdk.internal.clang.libclang.Index_h.C_INT;
import static jdk.internal.clang.libclang.Index_h.C_POINTER;

public class TranslationUnit implements AutoCloseable {

    private MemoryAddress tu;

    TranslationUnit(MemoryAddress tu) {
        this.tu = tu;
    }

    public Cursor getCursor() {
        return new Cursor(Index_h.clang_getTranslationUnitCursor(ResourceScope.newConfinedScope(), tu));
    }

    public Diagnostic[] getDiagnostics() {
        int cntDiags = Index_h.clang_getNumDiagnostics(tu);
        Diagnostic[] rv = new Diagnostic[cntDiags];
        for (int i = 0; i < cntDiags; i++) {
            MemoryAddress diag = Index_h.clang_getDiagnostic(tu, i);
            rv[i] = new Diagnostic(diag);
        }

        return rv;
    }

    public final void save(Path path) throws TranslationUnitSaveException {
        try (ResourceScope scope = ResourceScope.newConfinedScope()) {
            MemorySegment pathStr = scope.allocateUtf8String(path.toAbsolutePath().toString());
            SaveError res = SaveError.valueOf(Index_h.clang_saveTranslationUnit(tu, pathStr, 0));
            if (res != SaveError.None) {
                throw new TranslationUnitSaveException(path, res);
            }
        }
    }

    void processDiagnostics(Consumer<Diagnostic> dh) {
        Objects.requireNonNull(dh);
        for (Diagnostic diag : getDiagnostics()) {
            dh.accept(diag);
        }
    }

    static long FILENAME_OFFSET = CXUnsavedFile.$LAYOUT().byteOffset(MemoryLayout.PathElement.groupElement("Filename"));
    static long CONTENTS_OFFSET = CXUnsavedFile.$LAYOUT().byteOffset(MemoryLayout.PathElement.groupElement("Contents"));
    static long LENGTH_OFFSET = CXUnsavedFile.$LAYOUT().byteOffset(MemoryLayout.PathElement.groupElement("Length"));

    public void reparse(Index.UnsavedFile... inMemoryFiles) {
        try (ResourceScope scope = ResourceScope.newConfinedScope()) {
            MemorySegment files = inMemoryFiles.length == 0 ?
                    null :
                    scope.allocateArray(CXUnsavedFile.$LAYOUT(), inMemoryFiles.length);
            for (int i = 0; i < inMemoryFiles.length; i++) {
                MemorySegment start = files.asSlice(i * CXUnsavedFile.$LAYOUT().byteSize());
                start.set(C_POINTER, FILENAME_OFFSET, scope.allocateUtf8String(inMemoryFiles[i].file));
                start.set(C_POINTER, CONTENTS_OFFSET, scope.allocateUtf8String(inMemoryFiles[i].contents));
                start.set(C_INT, LENGTH_OFFSET, inMemoryFiles[i].contents.length());
            }
            ErrorCode code = ErrorCode.valueOf(Index_h.clang_reparseTranslationUnit(
                        tu,
                        inMemoryFiles.length,
                        files == null ? MemoryAddress.NULL : files,
                        Index_h.clang_defaultReparseOptions(tu)));

            if (code != ErrorCode.Success) {
                throw new IllegalStateException("Re-parsing failed: " + code);
            }
        }
    }

    public void reparse(Consumer<Diagnostic> dh, Index.UnsavedFile... inMemoryFiles) {
        reparse(inMemoryFiles);
        processDiagnostics(dh);
    }

    public String[] tokens(SourceRange range) {
        Tokens tokens = tokenize(range);
        String rv[] = new String[tokens.size()];
        for (int i = 0; i < rv.length; i++) {
            rv[i] = tokens.getToken(i).spelling();
        }
        return rv;
    }

    public Tokens tokenize(SourceRange range) {
        try (ResourceScope scope = ResourceScope.newConfinedScope()) {
            MemorySegment p = MemorySegment.allocateNative(C_POINTER, scope);
            MemorySegment pCnt = MemorySegment.allocateNative(C_INT, scope);
            Index_h.clang_tokenize(tu, range.range, p, pCnt);
            Tokens rv = new Tokens(p.get(C_POINTER, 0), pCnt.get(C_INT, 0));
            return rv;
        }
    }

    @Override
    public void close() {
        dispose();
    }

    public void dispose() {
        if (tu != MemoryAddress.NULL) {
            Index_h.clang_disposeTranslationUnit(tu);
            tu = MemoryAddress.NULL;
        }
    }

    public class Tokens {
        private final MemoryAddress ar;
        private final int size;

        Tokens(MemoryAddress ar, int size) {
            this.ar = ar;
            this.size = size;
        }

        public void dispose() {
            Index_h.clang_disposeTokens(tu, ar, size);
        }

        public int size() {
            return size;
        }

        public MemorySegment getTokenSegment(int idx) {
            MemoryAddress p = ar.addOffset(idx * CXToken.$LAYOUT().byteSize());
            return MemorySegment.ofAddressNative(p, CXToken.$LAYOUT().byteSize(), ResourceScope.newConfinedScope());
        }

        public Token getToken(int index) {
            return new Token(getTokenSegment(index));
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < size; i++) {
                try (ResourceScope scope = ResourceScope.newConfinedScope()) {
                    MemorySegment s = Index_h.clang_getTokenSpelling(scope, tu, getTokenSegment(i));
                    sb.append("Token[");
                    sb.append(i);
                    sb.append("]=");
                    sb.append(LibClang.CXStrToString(s));
                    sb.append("\n");
                }
            }
            return sb.toString();
        }
    }

    public class Token {
        final MemorySegment token;

        Token(MemorySegment token) {
            this.token = token;
        }

        public int kind() {
            return Index_h.clang_getTokenKind(token);
        }

        public String spelling() {
            try (ResourceScope scope = ResourceScope.newConfinedScope()) {
                MemorySegment s = Index_h.clang_getTokenSpelling(
                    scope, tu, token);
                return LibClang.CXStrToString(s);
            }
        }

        public SourceLocation getLocation() {
            return new SourceLocation(Index_h.clang_getTokenLocation(
                ResourceScope.newConfinedScope(), tu, token));
        }

        public SourceRange getExtent() {
            return new SourceRange(Index_h.clang_getTokenExtent(ResourceScope.newConfinedScope(),
                    tu, token));
        }
    }

    public static class TranslationUnitSaveException extends IOException {

        static final long serialVersionUID = 1L;

        private final SaveError error;

        TranslationUnitSaveException(Path path, SaveError error) {
            super("Cannot save translation unit to: " + path.toAbsolutePath() + ". Error: " + error);
            this.error = error;
        }
    }
}
