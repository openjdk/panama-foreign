/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.tools.jextract;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Options {
    // FIXME: Remove this if/when the macros support is deemed stable
    public static boolean INCLUDE_MACROS = Boolean.parseBoolean(System.getProperty("jextract.INCLUDE_MACROS", "true"));

    // The args for parsing C
    public final List<String> clangArgs;
    // The list of library names
    public final List<String> libraryNames;
    // The list of library paths
    public final List<String> libraryPaths;
    // whether library paths are recorded in .class files or not?
    public final boolean recordLibraryPath;
    // Symbol patterns to be included
    public final List<Pattern> includeSymbols;
    // Symbol patterns to be excluded
    public final List<Pattern> excludeSymbols;
    // Action to take when encountering missing symbol
    public final MissingSymbolAction missingSymbolAction;
    // no NativeLocation info
    public final boolean noNativeLocations;
    // generate static forwarder class or not?
    public final boolean genStaticForwarder;
    // target package
    public final String targetPackage;
    // package mappings
    public final Map<Path, String> pkgMappings;

    private Options(List<String> clangArgs, List<String> libraryNames, List<String> libraryPaths,
                   boolean recordLibraryPath, List<Pattern> includeSymbols, List<Pattern> excludeSymbols,
                   MissingSymbolAction missingSymbolAction, boolean noNativeLocations, boolean genStaticForwarder,
                   String targetPackage, Map<Path, String> pkgMappings) {
        this.clangArgs = clangArgs;
        this.libraryNames = libraryNames;
        this.libraryPaths = libraryPaths;
        this.recordLibraryPath = recordLibraryPath;
        this.includeSymbols = includeSymbols;
        this.excludeSymbols = excludeSymbols;
        this.missingSymbolAction = missingSymbolAction;
        this.noNativeLocations = noNativeLocations;
        this.genStaticForwarder = genStaticForwarder;
        this.targetPackage = targetPackage;
        this.pkgMappings = pkgMappings;
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
        private final List<String> libraryPaths;
        private boolean recordLibraryPath;
        private final List<Pattern> includeSymbols;
        private final List<Pattern> excludeSymbols;
        private MissingSymbolAction missingSymbolAction;
        private boolean noNativeLocations;
        private boolean genStaticForwarder;
        private String targetPackage;
        private final Map<Path, String> pkgMappings;

        public Builder() {
            this.clangArgs = new ArrayList<>();
            this.libraryNames = new ArrayList<>();
            this.libraryPaths = new ArrayList<>();
            this.recordLibraryPath = false;
            this.includeSymbols = new ArrayList<>();
            this.excludeSymbols = new ArrayList<>();
            this.missingSymbolAction = MissingSymbolAction.EXCLUDE;
            this.noNativeLocations = false;
            this.genStaticForwarder = false;
            this.targetPackage = null;
            this.pkgMappings = new LinkedHashMap<>();
        }

        public Options build() {
            return new Options(
                    Collections.unmodifiableList(clangArgs),
                    Collections.unmodifiableList(libraryNames),
                    Collections.unmodifiableList(libraryPaths),
                    recordLibraryPath,
                    Collections.unmodifiableList(includeSymbols),
                    Collections.unmodifiableList(excludeSymbols),
                    missingSymbolAction,
                    noNativeLocations,
                    genStaticForwarder,
                    targetPackage,
                    Collections.unmodifiableMap(pkgMappings)
            );
        }

        public void addClangArg(String arg) {
            clangArgs.add(arg);
        }

        public void addLibraryName(String name) {
            libraryNames.add(name);
        }

        public void addLibraryPath(String path) {
            libraryPaths.add(path);
        }

        public void setNoNativeLocations() {
            noNativeLocations = true;
        }

        public void setMissingSymbolAction(MissingSymbolAction msa) {
            missingSymbolAction = msa;
        }

        public void setRecordLibraryPath() {
            recordLibraryPath = true;
        }

        public void addIncludeSymbols(String pattern) {
            includeSymbols.add(Pattern.compile(pattern));
        }

        public void addExcludeSymbols(String pattern) {
            excludeSymbols.add(Pattern.compile(pattern));
        }

        public void setGenStaticForwarder(boolean flag) {
            this.genStaticForwarder = flag;
        }

        public void setTargetPackage(String pkg) {
            this.targetPackage = pkg;
        }

        public void addPackageMapping(Path path, String pkg) {
            pkgMappings.put(path, pkg);
        }

    }
}
