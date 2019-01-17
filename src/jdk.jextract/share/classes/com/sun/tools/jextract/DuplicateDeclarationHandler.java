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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import com.sun.tools.jextract.parser.Parser;
import com.sun.tools.jextract.tree.EnumTree;
import com.sun.tools.jextract.tree.FunctionTree;
import com.sun.tools.jextract.tree.HeaderTree;
import com.sun.tools.jextract.tree.MacroTree;
import com.sun.tools.jextract.tree.SimpleTreeVisitor;
import com.sun.tools.jextract.tree.StructTree;
import com.sun.tools.jextract.tree.Tree;
import com.sun.tools.jextract.tree.TreeMaker;
import com.sun.tools.jextract.tree.TreePhase;
import com.sun.tools.jextract.tree.TreePrinter;
import com.sun.tools.jextract.tree.VarTree;
import jdk.internal.clang.SourceLocation;

/**
 * 1. Remove repeated declarations.
 * 2. Remove redundant struct/union/enum forward/backward declarations
 */
final class DuplicateDeclarationHandler extends SimpleTreeVisitor<Void, Void>
        implements TreePhase {
    private final TreeMaker treeMaker = new TreeMaker();
    private Path headerPath;

    // Potential Tree instances that will go into transformed HeaderTree
    // are collected in this list.
    private List<Tree> decls = new ArrayList<>();

    private boolean isFromSameHeader(Tree def, Tree decl) {
        SourceLocation locDef = def.location();
        SourceLocation locDecl = decl.location();
        return locDef != null && locDecl != null &&
                Objects.equals(locDecl.getFileLocation().path(), locDef.getFileLocation().path());
    }

    // type declarations seen
    private final Map<String, Tree> typeDeclsSeen = new HashMap<>();
    private void addTypeDeclaration(Tree tree) {
        assert tree instanceof EnumTree || tree instanceof StructTree;
        Tree existing = typeDeclsSeen.get(tree.name());
        if (existing == null || !isFromSameHeader(tree, existing)) {
            decls.add(tree);
        }
        typeDeclsSeen.put(tree.name(), tree);
    }

    // global symbols (variables, functions) seen
    private final Map<String, Tree> globalsSeen = new HashMap<>();
    private void addGlobal(Tree tree) {
        assert tree instanceof FunctionTree || tree instanceof VarTree;
        Tree existing = globalsSeen.get(tree.name());
        if (existing == null || !isFromSameHeader(tree, existing)) {
            decls.add(tree);
        }
        globalsSeen.put(tree.name(), tree);
    }

    // macros seen
    private final Map<String, Tree> macrosSeen = new HashMap<>();
    private void addMacro(MacroTree tree) {
        Tree existing = macrosSeen.get(tree.name());
        if (existing == null || !isFromSameHeader(tree, existing)) {
            decls.add(tree);
        }
        macrosSeen.put(tree.name(), tree);
    }

    @Override
    public HeaderTree transform(HeaderTree ht) {
        // Process all header declarations are collect potential
        // declarations that will go into transformed HeaderTree
        // into the this.decls field.
        ht.accept(this, null);

        return treeMaker.createHeader(ht.cursor(), ht.path(), decls);
    }

    @Override
    public Void defaultAction(Tree tree, Void v) {
        decls.add(tree);
        return null;
    }

    @Override
    public Void visitEnum(EnumTree e, Void v) {
        /*
         * If we're seeing a forward/backward declaration of an
         * enum which is definied elsewhere in this compilation
         * unit, ignore it. If no definition is found, we want to
         * leave the declaration so that dummy definition will be
         * generated.
         *
         * Example:
         *
         *  enum Color ; // <-- forward declaration
         *  enum Color { r, g, b };
         *  enum Number { Int, Float, Complex };
         *  enum Number; // <-- backward declaration
         */

        // include this only if this is a definition or a declaration
        // for which no definition is found elsewhere in this header.
        if (e.isDefinition()) {
            decls.add(e);
        } else {
            Optional<Tree> def = e.definition();
            if (!def.isPresent() || !isFromSameHeader(def.get(), e)) {
                addTypeDeclaration(e);
            }
        }
        return null;
    }

    @Override
    public Void visitFunction(FunctionTree t, Void v) {
        addGlobal(t);
        return null;
    }

    @Override
    public Void visitHeader(HeaderTree ht, Void v) {
        this.headerPath = ht.path();
        ht.declarations().forEach(decl -> decl.accept(this, null));
        return null;
    }

    @Override
    public Void visitMacro(MacroTree t, Void v) {
        addMacro(t);
        return null;
    }

    @Override
    public Void visitStruct(StructTree s, Void v) {
        List<Tree> oldDecls = decls;
        List<Tree> structDecls = new ArrayList<>();
        try {
            decls = structDecls;
            s.declarations().forEach(t -> t.accept(this, null));
        } finally {
            decls = oldDecls;
        }

        /*
         * If we're seeing a forward/backward declaration of
         * a struct which is definied elsewhere in this compilation
         * unit, ignore it. If no definition is found, we want to
         * leave the declaration so that dummy definition will be
         * generated.
         *
         * Example:
         *
         *  struct Point; // <-- forward declaration
         *  struct Point { int i; int j; };
         *  struct Point3D { int i; int j; int k; };
         *  struct Point3D; // <-- backward declaration
         */

        // include this only if this is a definition or a declaration
        // for which no definition is found elsewhere in this header.
        if (s.isDefinition()) {
            decls.add(s.withNameAndDecls(s.name(), structDecls));
        } else {
            Optional<Tree> def = s.definition();
            if (!def.isPresent() || !isFromSameHeader(def.get(), s)) {
                StructTree newStruct = s.withNameAndDecls(s.name(), structDecls);
                addTypeDeclaration(newStruct);
            }
        }
        return null;
    }

    @Override
    public Void visitVar(VarTree t, Void v) {
        addGlobal(t);
        return null;
    }

    // test main to manually check this visitor
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Expected a header file");
            return;
        }

        Context context = new Context();
        Parser p = new Parser(context, true);
        Path builtinInc = Paths.get(System.getProperty("java.home"), "conf", "jextract");
        List<String> clangArgs = List.of("-I" + builtinInc);
        HeaderTree header = p.parse(Paths.get(args[0]), clangArgs);
        TreePrinter printer = new TreePrinter();
        DuplicateDeclarationHandler handler = new DuplicateDeclarationHandler();
        handler.transform(header).accept(printer, null);
    }
}
