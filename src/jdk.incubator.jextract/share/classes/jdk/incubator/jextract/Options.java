/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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
package jdk.incubator.jextract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class Options {
    // The args for parsing C
    final List<String> clangArgs;
    // The list of library names
    final List<String> libraryNames;
    final List<String> filters;
    // target package
    final String targetPackage;
    // output directory
    final String outputDir;
    final boolean source;

    private Options(List<String> clangArgs, List<String> libraryNames,
            List<String> filters, String targetPackage,
            String outputDir, boolean source) {
        this.clangArgs = clangArgs;
        this.libraryNames = libraryNames;
        this.filters = filters;
        this.targetPackage = targetPackage;
        this.outputDir = outputDir;
        this.source = source;
    }

    static Builder builder() {
        return new Builder();
    }

    static Options createDefault() {
        return builder().build();
    }

    static class Builder {
        private final List<String> clangArgs;
        private final List<String> libraryNames;
        private final List<String> filters;
        private String targetPackage;
        private String outputDir;
        private boolean source;

        Builder() {
            this.clangArgs = new ArrayList<>();
            this.libraryNames = new ArrayList<>();
            this.filters = new ArrayList<>();
            this.targetPackage = "";
            this.outputDir = ".";
            this.source = false;
        }

        Options build() {
            return new Options(
                    Collections.unmodifiableList(clangArgs),
                    Collections.unmodifiableList(libraryNames),
                    Collections.unmodifiableList(filters),
                    targetPackage, outputDir, source
            );
        }

        void addClangArg(String arg) {
            clangArgs.add(arg);
        }

        void addLibraryName(String name) {
            libraryNames.add(name);
        }

        void setOutputDir(String outputDir) {
            this.outputDir = outputDir;
        }

        void setTargetPackage(String pkg) {
            this.targetPackage = pkg;
        }

        void addFilter(String filter) {
            filters.add(filter);
        }

        void setGenerateSource() {
            source = true;
        }
    }
}
