/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
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

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * The setup for the tool execution
 */
public final class Context {
    // The args for parsing C
    public final List<String> clangArgs;
    // The set of source header files
    public final Set<Path> sources;
    // The list of library names
    public final List<String> libraryNames;
    // The list of library paths
    public final List<String> libraryPaths;
    // The list of library paths for link checks
    public final List<String> linkCheckPaths;
    // Symbol patterns to be included
    public final List<Pattern> includeSymbols;
    // Symbol patterns to be excluded
    public final List<Pattern> excludeSymbols;
    // no NativeLocation info
    public boolean noNativeLocations;
    // generate static forwarder class or not?
    public boolean genStaticForwarder;
    // target package
    public String targetPackage;
    // package mappings
    public final Map<Path, String> pkgMappings = new LinkedHashMap<>();

    public final PrintWriter out;
    public final PrintWriter err;

    final Logger logger = Logger.getLogger(getClass().getPackage().getName());

    public Context(PrintWriter out, PrintWriter err) {
        this.clangArgs = new ArrayList<>();
        this.sources = new LinkedHashSet<>();
        this.libraryNames = new ArrayList<>();
        this.libraryPaths = new ArrayList<>();
        this.linkCheckPaths = new ArrayList<>();
        this.includeSymbols = new ArrayList<>();
        this.excludeSymbols = new ArrayList<>();
        this.out = out;
        this.err = err;
    }

    public Context() {
        this(new PrintWriter(System.out, true), new PrintWriter(System.err, true));
    }

    public void addClangArg(String arg) {
        clangArgs.add(arg);
    }

    public void addSource(Path path) {
        sources.add(path);
    }

    public void addLibraryName(String name) {
        libraryNames.add(name);
    }

    public void addLibraryPath(String path) {
        libraryPaths.add(path);
    }

    public void addLinkCheckPath(String path) {
        linkCheckPaths.add(path);
    }

    public void setNoNativeLocations() {
        noNativeLocations = true;
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

    // return the absolute path of the library of given name by searching
    // in the given array of paths.
    public static Optional<Path> findLibraryPath(Path[] paths, String libName) {
        return Arrays.stream(paths).
                map(p -> p.resolve(System.mapLibraryName(libName))).
                filter(Files::isRegularFile).map(Path::toAbsolutePath).findFirst();
    }
}
