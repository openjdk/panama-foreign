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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class SourceLocation extends StructType {

    SourceLocation(ByteBuffer buf) {
        super(buf);
    }

    protected SourceLocation(ByteBuffer buf, boolean copy) {
        super(buf, copy);
    }

    public native Location getFileLocation();
    public native Location getExpansionLocation();
    public native Location getSpellingLocation();
    public native boolean isInSystemHeader();
    public native boolean isFromMainFile();

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SourceLocation)) {
            return false;
        }
        SourceLocation sloc = (SourceLocation)other;
        return Objects.equals(getFileLocation(), sloc.getFileLocation());
    }

    @Override
    public int hashCode() {
        return getFileLocation().hashCode();
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

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Location)) {
                return false;
            }
            Location loc = (Location)other;
            return Objects.equals(path, loc.path) &&
                line == loc.line && column == loc.column &&
                offset == loc.offset;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(path) ^ line ^ column ^ offset;
        }
    }
}
