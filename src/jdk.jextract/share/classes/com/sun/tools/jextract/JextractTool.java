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

import com.sun.tools.jextract.parser.Parser;
import com.sun.tools.jextract.tree.Tree;

import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JextractTool {
    private final HeaderResolver headerResolver;
    private final SymbolFilter symbolFilter;
    private final Parser parser;
    private final Function<HeaderFile, AsmCodeFactory> codeFactory;
    private final Collection<String> clangArgs;
    private final Collection<Path> sources;

    public JextractTool(Context ctx) {
        this.headerResolver = new HeaderResolver(ctx);
        this.parser = new Parser(ctx, Main.INCLUDE_MACROS);
        this.symbolFilter = new SymbolFilter(ctx);
        this.codeFactory = ctx.genStaticForwarder ?
                hf -> new AsmCodeFactoryExt(ctx, hf) :
                hf -> new AsmCodeFactory(ctx, hf);
        this.clangArgs = ctx.clangArgs;
        this.sources = ctx.sources;
    }

    /** This is the main jextract entry point */
    public Writer processHeaders() {
        Map<HeaderFile, List<Tree>> headerMap = parser.parse(sources, clangArgs).stream()
                .map(symbolFilter)
                .map(new TypedefHandler())
                .map(new EmptyNameHandler())
                .flatMap(h -> h.declarations().stream())
                .distinct()
                .collect(Collectors.groupingBy(this::headerFromDecl));

        //generate classes
        Map<String, byte[]> results = new LinkedHashMap<>();
        headerMap.forEach((hf, decls) -> generateHeader(hf, decls, results));
        return new Writer(results);
    }

    private void generateHeader(HeaderFile hf, List<Tree> decls, Map<String, byte[]> results) {
        TypeEnter enter = new TypeEnter(hf.dictionary());
        decls.forEach(t -> t.accept(enter, null));
        AsmCodeFactory cf = codeFactory.apply(hf);
        results.putAll(cf.generateNativeHeader(decls));
    }

    private HeaderFile headerFromDecl(Tree tree) {
        Path path = tree.cursor().getSourceLocation().getFileLocation().path();
        return headerResolver.headerFor(path);
    }
}
