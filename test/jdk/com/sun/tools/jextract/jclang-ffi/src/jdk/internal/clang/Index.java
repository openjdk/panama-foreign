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

import java.foreign.Scope;
import java.foreign.memory.Pointer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import clang.Index.CXTranslationUnitImpl;
import clang.Index.CXDiagnostic;

public class Index {
    // Pointer to CXIndex
    private final Pointer<Void> ptr;
    // Set of TranslationUnit
    public final List<Pointer<CXTranslationUnitImpl>> translationUnits;

    Index(Pointer<Void> ptr) {
        this.ptr = ptr;
        translationUnits = new ArrayList<>();
    }

    public TranslationUnit parseTU(String file, String... args) {
        final clang.Index lclang = LibClang.lib;

        try (Scope scope = Scope.newNativeScope()) {
            Pointer<Byte> src = scope.toCString(file);
            Pointer<Pointer<Byte>> cargs = scope.toCStrArray(args);
            Pointer<CXTranslationUnitImpl> tu = lclang.clang_parseTranslationUnit(
                    ptr, src, cargs, args.length, null, 0,
                    LibClang.lib.CXTranslationUnit_DetailedPreprocessingRecord());
            return new TranslationUnit(tu);
        }
    }

    public Cursor parse(String file, Consumer<Diagnostic> eh, boolean detailedPreprocessorRecord, String... args) {
        final clang.Index lclang = LibClang.lib;

        try (Scope scope = Scope.newNativeScope()) {
            Pointer<Byte> src = scope.toCString(file);
            Pointer<Pointer<Byte>> cargs = scope.toCStrArray(args);
            Pointer<CXTranslationUnitImpl> tu = lclang.clang_parseTranslationUnit(
                    ptr, src, cargs, args.length, Pointer.nullPointer(), 0,
                    detailedPreprocessorRecord ?
                            LibClang.lib.CXTranslationUnit_DetailedPreprocessingRecord() :
                            LibClang.lib.CXTranslationUnit_None());

            if (tu != null && !tu.isNull()) {
                translationUnits.add(tu);
            }

            int cntDiags = lclang.clang_getNumDiagnostics(tu);
            for (int i = 0; i < cntDiags; i++) {
                @CXDiagnostic Pointer<Void> diag = lclang.clang_getDiagnostic(tu, i);
                eh.accept(new Diagnostic(diag));
            }

            return new Cursor(lclang.clang_getTranslationUnitCursor(tu));
        }
    }

    public void dispose() {
        for (Pointer<CXTranslationUnitImpl> tu: translationUnits) {
            LibClang.lib.clang_disposeTranslationUnit(tu);
        }
        LibClang.lib.clang_disposeIndex(ptr);
    }
}
