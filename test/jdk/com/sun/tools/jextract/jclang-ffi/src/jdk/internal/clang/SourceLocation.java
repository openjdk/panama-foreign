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

import java.nicl.NativeTypes;
import java.nicl.Scope;
import java.nicl.types.LayoutType;
import java.nicl.types.Pointer;
import java.nio.file.Path;
import java.nio.file.Paths;

import static clang.CXString.CXString;
import static clang.Index.CXSourceLocation;

public class SourceLocation {

    private final CXSourceLocation loc;

    SourceLocation(CXSourceLocation loc) {
        this.loc = loc;
    }

    @FunctionalInterface
    private interface LocationFactory {
        void get(CXSourceLocation loc, Pointer<Pointer<Void>> file,
                 Pointer<Integer> line, Pointer<Integer> column, Pointer<Integer> offset);
    }

    @SuppressWarnings("unchecked")
    private Location getLocation(LocationFactory fn) {
        try (Scope s = Scope.newNativeScope()) {
            Pointer<Pointer<Void>> file = s.allocate(NativeTypes.VOID.pointer());
            Pointer<Integer> line = s.allocate(NativeTypes.INT);
            Pointer<Integer> col = s.allocate(NativeTypes.INT);
            Pointer<Integer> offset = s.allocate(NativeTypes.INT);

            fn.get(loc, file, line, col, offset);
            CXString fname = LibClang.lib.clang_getFileName(file.get());
            return new Location(LibClang.CXStrToString(fname), line.get(),
                col.get(), offset.get());
        }
    }

    public Location getFileLocation() { return getLocation(LibClang.lib::clang_getFileLocation); }
    public Location getExpansionLocation() { return getLocation(LibClang.lib::clang_getExpansionLocation); }
    public Location getSpellingLocation() { return getLocation(LibClang.lib::clang_getSpellingLocation); }
    public boolean isInSystemHeader() {
        return LibClang.lib.clang_Location_isInSystemHeader(loc) != 0;
    }

    public boolean isFromMainFile() {
        return LibClang.lib.clang_Location_isFromMainFile(loc) != 0;
    }

    public final static class Location {
        private final Path path;
        private final int line;
        private final int column;
        private final int offset;

        private Location(String filename, int line, int column, int offset) {
            if (filename == null || filename.isEmpty()) {
                this.path = null;
            } else {
                this.path = Paths.get(filename);
            }

            this.line = line;
            this.column = column;
            this.offset = offset;
        }

        public Path path() {
            return path;
        }

        public int line() {
            return line;
        }

        public int column() {
            return column;
        }

        public int offset() {
            return offset;
        }
    }
}
