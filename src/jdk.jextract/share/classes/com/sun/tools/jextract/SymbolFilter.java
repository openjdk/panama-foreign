/*
 *  Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.
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
 *  Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 */

package com.sun.tools.jextract;

import com.sun.tools.jextract.tree.FunctionTree;
import com.sun.tools.jextract.tree.Tree;

import java.foreign.Libraries;
import java.foreign.Library;
import java.io.PrintWriter;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class SymbolFilter extends TreeFilter {
    private Predicate<String> symChecker;
    private Predicate<String> includeSymFilter;
    private Predicate<String> excludeSymFilter;
    private final PrintWriter err;
    private final List<String> libraryNames;
    private final MissingSymbolAction missingSymbolAction;

    public SymbolFilter(Context ctx) {
        this.err = ctx.err;
        this.libraryNames = ctx.libraryNames;
        this.missingSymbolAction = ctx.missingSymbolAction;
        initSymFilters(ctx.includeSymbols, ctx.excludeSymbols);
        initSymChecker(ctx.libraryPaths);
    }

    /*
     * Load the specified shared libraries from the specified paths.
     *
     * @param lookup Lookup object of the caller.
     * @param pathStrs array of paths to load the shared libraries from.
     * @param names array of shared library names.
     */
    // used by jextract tool to load libraries for symbol checks.
    public static Library[] loadLibraries(MethodHandles.Lookup lookup, String[] pathStrs, String[] names) {
        if (pathStrs == null || pathStrs.length == 0) {
            return Arrays.stream(names).map(
                    name -> Libraries.loadLibrary(lookup, name)).toArray(Library[]::new);
        } else {
            Path[] paths = Arrays.stream(pathStrs).map(Paths::get).toArray(Path[]::new);
            return Arrays.stream(names).map(libName -> {
                Optional<Path> absPath = Context.findLibraryPath(paths, libName);
                return absPath.isPresent() ?
                        Libraries.load(lookup, absPath.get().toString()) :
                        Libraries.loadLibrary(lookup, libName);
            }).toArray(Library[]::new);
        }
    }

    private void initSymChecker(List<String> linkCheckPaths) {
        if (!libraryNames.isEmpty() && !linkCheckPaths.isEmpty()) {
            try {
                Library[] libs = loadLibraries(MethodHandles.lookup(),
                        linkCheckPaths.toArray(new String[0]),
                        libraryNames.toArray(new String[0]));
                // check if the given symbol is found in any of the libraries or not.
                // If not found, warn the user for the missing symbol.
                symChecker = name -> {
                    if (Main.DEBUG) {
                        err.println("Searching symbol: " + name);
                    }
                    return (Arrays.stream(libs).anyMatch(lib -> {
                        try {
                            lib.lookup(name);
                            if (Main.DEBUG) {
                                err.println("Found symbol: " + name);
                            }
                            return true;
                        } catch (NoSuchMethodException nsme) {
                            return false;
                        }
                    }));
                };
            } catch (UnsatisfiedLinkError ex) {
                err.println(Main.format("warn.lib.not.found"));
                symChecker = null;
            }
        } else {
            symChecker = null;
        }
    }

    private boolean isSymbolFound(String name) {
        return symChecker == null || symChecker.test(name);
    }

    private void initSymFilters(List<Pattern> includeSymbols, List<Pattern> excludeSymbols) {
        if (!includeSymbols.isEmpty()) {
            Pattern[] pats = includeSymbols.toArray(new Pattern[0]);
            includeSymFilter = name ->
                Arrays.stream(pats).anyMatch(pat -> pat.matcher(name).matches());
        } else {
            includeSymFilter = null;
        }

        if (!excludeSymbols.isEmpty()) {
            Pattern[] pats = excludeSymbols.toArray(new Pattern[0]);
            excludeSymFilter = name ->
                Arrays.stream(pats).anyMatch(pat -> pat.matcher(name).matches());
        } else {
            excludeSymFilter = null;
        }
    }

    private boolean isSymbolIncluded(String name) {
        return includeSymFilter == null || includeSymFilter.test(name);
    }

    private boolean isSymbolExcluded(String name) {
        return excludeSymFilter != null && excludeSymFilter.test(name);
    }


    @Override
    boolean filter(Tree tree) {
        String name = tree.name();
        return isSymbolIncluded(name) && !isSymbolExcluded(name);
    }

    @Override
    public Tree visitFunction(FunctionTree ft, Void v) {
        String name = ft.name();
        if (missingSymbolAction != MissingSymbolAction.IGNORE) {
            // check for function symbols in libraries & apply action for missing symbols
            if (!isSymbolFound(name) && missingSymbolAction.handle(err, name)) {
                return null;
            }
        }

        return super.visitFunction(ft, null);
    }
}
