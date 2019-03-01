/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.nio.file.Path;

public class TranslationUnit {
    final long ptr;

    TranslationUnit(long ptr) {
        this.ptr = ptr;
    }

    public final Cursor getCursor() {
        return Index.getTranslationUnitCursor(ptr);
    }

    public final Diagnostic[] getDiagnostics() {
        return Index.getTranslationUnitDiagnostics(ptr);
    }

    public final String[] tokens(SourceRange range) {
        return Index.tokenize(ptr, range);
    }

    public final void save(Path path) throws TranslationUnitSaveException {
        int res = Index.saveTranslationUnit(ptr, path.toAbsolutePath().toString());
        if (res != 0) {
            throw new TranslationUnitSaveException(path);
        }
    }

    public final void dispose() {
        Index.disposeTranslationUnit(ptr);
    }

    public static class TranslationUnitSaveException extends IOException {

        static final long serialVersionUID = 1L;

        TranslationUnitSaveException(Path path) {
            super("Cannot save translation unit to: " + path.toAbsolutePath());
        }
    }

    @Override
    public int hashCode() {
      return (int)ptr;
    }

    public boolean equals(Object o) {
      return (o instanceof TranslationUnit) && ((TranslationUnit)o).ptr == ptr;
    }
}
