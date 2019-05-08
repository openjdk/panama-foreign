/*
 *  Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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
import com.sun.tools.jextract.tree.VarTree;

import java.foreign.Libraries;
import java.foreign.Library;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Filters functions and global variable based on whether
 * they appear in any shared library.
 */
public class LibraryLookupFilter extends TreeFilter {
    private Predicate<String> symChecker;
    private final Log log;
    private final List<String> libraryNames;
    private final MissingSymbolAction missingSymbolAction;

    private static final Predicate<String> DEFAULT_CHECKER = sym -> {
        try {
            Libraries.getDefaultLibrary().lookup(sym);
            return true;
        } catch (NoSuchMethodException nsme) {
            return false;
        }
    };

    public LibraryLookupFilter(Context ctx) {
        this.log = ctx.log;
        this.libraryNames = ctx.options.libraryNames;
        this.missingSymbolAction = ctx.options.missingSymbolAction;
        initSymChecker(ctx.options.libraryPaths);
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
                Optional<Path> absPath = Utils.findLibraryPath(paths, libName);
                return absPath.isPresent() ?
                        Libraries.load(lookup, absPath.get().toString()) :
                        Libraries.loadLibrary(lookup, libName);
            }).toArray(Library[]::new);
        }
    }

    private void initSymChecker(List<String> linkCheckPaths) {
        if (!libraryNames.isEmpty()) {
            assert !linkCheckPaths.isEmpty();
            try {
                Library[] libs = loadLibraries(MethodHandles.lookup(),
                        linkCheckPaths.toArray(new String[0]),
                        libraryNames.toArray(new String[0]));
                // check if the given symbol is found in any of the libraries or not.
                // If not found, warn the user for the missing symbol.
                symChecker = name -> {
                    log.printNote("note.searching.symbol", name);
                    return (Arrays.stream(libs).anyMatch(lib -> {
                        try {
                            lib.lookup(name);
                            log.printNote("note.symbol.found", name);
                            return true;
                        } catch (NoSuchMethodException nsme) {
                            return false;
                        }
                    }));
                };
            } catch (UnsatisfiedLinkError ex) {
                log.printWarning("warn.lib.not.found");
                symChecker = null;
            }
        } else {
            symChecker = DEFAULT_CHECKER;
        }
    }

    private boolean isSymbolFound(String name) {
        return symChecker == null || symChecker.test(name);
    }

    private Boolean filterLibrarySymbol(Tree tree) {
        if (missingSymbolAction != MissingSymbolAction.IGNORE) {
            String name = tree.name();
            if (tree instanceof FunctionTree) {
                name = ((FunctionTree) tree).function().name().orElse(name);
            } else if (tree instanceof VarTree) {
                name = ((VarTree) tree).layout().name().orElse(name);
            }
            // check for function symbols in libraries & apply action for missing symbols
            if (!isSymbolFound(name) && missingSymbolAction.handle(log, name)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Boolean visitVar(VarTree vt, Void v) {
        return filterLibrarySymbol(vt);
    }

    @Override
    public Boolean visitFunction(FunctionTree ft, Void v) {
        return filterLibrarySymbol(ft);
    }

}
