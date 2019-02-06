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
package com.sun.tools.jextract;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.sun.tools.jextract.parser.Parser;
import com.sun.tools.jextract.tree.EnumTree;
import com.sun.tools.jextract.tree.FieldTree;
import com.sun.tools.jextract.tree.LayoutUtils;
import com.sun.tools.jextract.tree.HeaderTree;
import com.sun.tools.jextract.tree.SimpleTreeVisitor;
import com.sun.tools.jextract.tree.StructTree;
import com.sun.tools.jextract.tree.Tree;
import com.sun.tools.jextract.tree.TreeMaker;
import com.sun.tools.jextract.tree.TreePhase;
import com.sun.tools.jextract.tree.TreePrinter;
import jdk.internal.clang.Cursor;

/**
 * This tree visitor handles the tree empty names encountered in the tree
 * so that subsequent passes need not check tree.name() for empty string.
 *
 * 1. Names are generated & set for anonymous structs & unions.
 * 2. Anonymous (bit) FieldTree instances are removed.
 */
final class EmptyNameHandler extends SimpleTreeVisitor<Tree, Void>
        implements TreePhase {
    private final TreeMaker treeMaker = new TreeMaker();

    @Override
    public HeaderTree transform(HeaderTree ht) {
        return (HeaderTree)ht.accept(this, null);
    }

    // generate unique name for an empty name
    private String generateName(Tree tree) {
        return LayoutUtils.getName(tree);
    }

    @Override
    public Tree defaultAction(Tree tree, Void v) {
        return tree;
    }

    @Override
    public Tree visitHeader(HeaderTree ht, Void v) {
        List<Tree> decls =  ht.declarations().stream().
            map(decl -> decl.accept(this, null)).
            collect(Collectors.toList());
        return treeMaker.createHeader(ht.cursor(), ht.path(), decls);
    }

    @Override
    public Tree visitField(FieldTree t, Void aVoid) {
        if (t.name().isEmpty()) {
            /*
             * Skip anonymous fields. This happens in the following case:
             *
             * struct {
             *    int  :23; // anonymous bit field
             *    int x:9;
             * }
             */

            return null;
        } else {
            return t;
        }
    }

    @Override
    public Tree visitStruct(StructTree s, Void v) {
        List<Tree> newDecls = s.declarations().stream()
                .map(decl -> decl.accept(this, null))
                .filter(d -> d != null)
                .collect(Collectors.toList());

        return s.withNameAndDecls(generateName(s), newDecls);
    }

    // test main to manually check this visitor
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Expected a header file");
            return;
        }

        Context context = new Context();
        Parser p = new Parser(context,true);
        Path builtinInc = Paths.get(System.getProperty("java.home"), "conf", "jextract");
        List<String> clangArgs = List.of("-I" + builtinInc);
        HeaderTree header = p.parse(Paths.get(args[0]), clangArgs);
        TreePrinter printer = new TreePrinter();
        EmptyNameHandler handler = new EmptyNameHandler();
        handler.transform(header).accept(printer, null);
    }
}
