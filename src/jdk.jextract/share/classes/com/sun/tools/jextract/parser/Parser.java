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
package com.sun.tools.jextract.parser;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jdk.internal.clang.Cursor;
import jdk.internal.clang.CursorKind;
import jdk.internal.clang.Diagnostic;
import jdk.internal.clang.Index;
import jdk.internal.clang.LibClang;
import jdk.internal.clang.SourceLocation;
import jdk.internal.clang.SourceRange;
import jdk.internal.clang.TranslationUnit;
import com.sun.tools.jextract.tree.HeaderTree;
import com.sun.tools.jextract.tree.MacroTree;
import com.sun.tools.jextract.tree.SimpleTreeVisitor;
import com.sun.tools.jextract.tree.Tree;
import com.sun.tools.jextract.tree.TreeMaker;
import com.sun.tools.jextract.tree.TreePrinter;

public class Parser {
    private final PrintWriter out;
    private final PrintWriter err;
    private final TreeMaker treeMaker;
    private final boolean supportMacros;
    private final Logger logger = Logger.getLogger(getClass().getPackage().getName());

    public Parser(PrintWriter out, PrintWriter err, boolean supportMacros) {
        this.out = out;
        this.err = err;
        this.treeMaker = new TreeMaker();
        this.supportMacros = supportMacros;
    }

    public Parser(boolean supportMacros) {
        this(new PrintWriter(System.out, true),
            new PrintWriter(System.err, true), supportMacros);
    }

    public List<HeaderTree> parse(Collection<Path> paths, Collection<String> args) {
        final List<HeaderTree> headers = new ArrayList<>();
        final Index index = LibClang.createIndex();
        for (Path path : paths) {
            logger.info(() -> {
                StringBuilder sb = new StringBuilder(
                        "Parsing header file " + path + " with following args:\n");
                int i = 0;
                for (String arg : args) {
                    sb.append("arg[");
                    sb.append(i++);
                    sb.append("] = ");
                    sb.append(arg);
                    sb.append("\n");
                }
                return sb.toString();
            });

            Cursor tuCursor = index.parse(path.toString(),
                d -> {
                    err.println(d);
                    if (d.severity() >  Diagnostic.CXDiagnostic_Warning) {
                        throw new RuntimeException(d.toString());
                    }
                },
                supportMacros, args.toArray(new String[0]));

            MacroParser macroParser = new MacroParser();
            List<Tree> decls = new ArrayList<>();
            tuCursor.children().
                peek(c -> logger.finest(
                    () -> "Cursor: " + c.spelling() + "@" + c.USR() + "?" + c.isDeclaration())).
                forEach(c -> {
                    SourceLocation loc = c.getSourceLocation();
                    if (loc == null) {
                        logger.info(() -> "Ignore Cursor " + c.spelling() + "@" + c.USR() + " has no SourceLocation");
                        return;
                    }

                    SourceLocation.Location src = loc.getFileLocation();
                    if (src == null) {
                        logger.info(() -> "Cursor " + c.spelling() + "@" + c.USR() + " has no FileLocation");
                        return;
                    }

                    logger.fine(() -> "Do cursor: " + c.spelling() + "@" + c.USR());

                    if (c.isDeclaration()) {
                        if (c.kind() == CursorKind.UnexposedDecl ||
                            c.kind() == CursorKind.Namespace) {
                            c.children().forEach(cu -> decls.add(treeMaker.createTree(cu)));
                        } else {
                            decls.add(treeMaker.createTree(c));
                        }
                    } else if (supportMacros && isMacro(c) && src.path() != null) {
                        handleMacro(macroParser, c);
                    }
                });

            decls.addAll(macros(macroParser));
            headers.add(treeMaker.createHeader(tuCursor, path, decls));
        }

        return Collections.unmodifiableList(headers);
    }

    private List<MacroTree> macros(MacroParser macroParser) {
        return macroParser.macros().stream()
            .map(m -> treeMaker.createMacro(m.cursor(), macroValue(m)))
            .collect(Collectors.toList());
    }

    private Optional<Object> macroValue(MacroParser.Macro m) {
        try {
            return Optional.ofNullable(m.value());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private void handleMacro(MacroParser macroParser, Cursor cursor) {
        String macroName = cursor.spelling();
        if (cursor.isMacroFunctionLike()) {
            logger.fine(() -> "Skipping function-like macro " + macroName);
            return;
        }

        if (macroParser.isDefined(macroName)) {
            logger.fine(() -> "Macro " + macroName + " already handled");
            return;
        }

        logger.fine(() -> "Defining macro " + macroName);

        TranslationUnit tu = cursor.getTranslationUnit();
        SourceRange range = cursor.getExtent();
        String[] tokens = tu.tokens(range);

        macroParser.parse(cursor, tokens);
    }

    private boolean isMacro(Cursor c) {
        return c.isPreprocessing() && c.kind() == CursorKind.MacroDefinition;
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Expected a header file");
            return;
        }

        Parser p = new Parser(true);
        List<Path> paths = Arrays.stream(args).map(Paths::get).collect(Collectors.toList());
        Path builtinInc = Paths.get(System.getProperty("java.home"), "conf", "jextract");
        List<String> clangArgs = List.of("-I" + builtinInc);
        List<HeaderTree> headers = p.parse(paths, clangArgs);
        TreePrinter printer = new TreePrinter();
        for (HeaderTree ht : headers) {
            ht.accept(printer, null);
        }
    }
}
