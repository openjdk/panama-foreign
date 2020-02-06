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
package jdk.incubator.jextract.tool;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Options {
    // The args for parsing C
    public final List<String> clangArgs;
    // The list of library names
    public final List<String> libraryNames;
    public final List<String> filters;
    // target package
    public final String targetPackage;
    // output directory
    public final String outputDir;
    public final boolean source;

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

    public static Builder builder() {
        return new Builder();
    }

    public static Options createDefault() {
        return builder().build();
    }

    public static class Builder {
        private final List<String> clangArgs;
        private final List<String> libraryNames;
        private final List<String> filters;
        private String targetPackage;
        private String outputDir;
        private boolean source;

        public Builder() {
            this.clangArgs = new ArrayList<>();
            this.libraryNames = new ArrayList<>();
            this.filters = new ArrayList<>();
            this.targetPackage = "";
            this.outputDir = ".";
            this.source = source;
        }

        public Options build() {
            return new Options(
                    Collections.unmodifiableList(clangArgs),
                    Collections.unmodifiableList(libraryNames),
                    Collections.unmodifiableList(filters),
                    targetPackage, outputDir, source
            );
        }

        public void addClangArg(String arg) {
            clangArgs.add(arg);
        }

        public void addLibraryName(String name) {
            libraryNames.add(name);
        }

        public void setOutputDir(String outputDir) {
            this.outputDir = outputDir;
        }

        public void setTargetPackage(String pkg) {
            this.targetPackage = pkg;
        }

        public void addFilter(String filter) {
            filters.add(filter);
        }

        public void setGenerateSource() {
            source = true;
        }
    }
}
