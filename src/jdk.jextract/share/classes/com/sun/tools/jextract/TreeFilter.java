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
import java.util.function.Predicate;
import java.util.stream.Collectors;
import com.sun.tools.jextract.parser.Parser;
import com.sun.tools.jextract.tree.FunctionTree;
import com.sun.tools.jextract.tree.HeaderTree;
import com.sun.tools.jextract.tree.MacroTree;
import com.sun.tools.jextract.tree.SimpleTreeVisitor;
import com.sun.tools.jextract.tree.Tree;
import com.sun.tools.jextract.tree.TreePhase;
import com.sun.tools.jextract.tree.VarTree;
import com.sun.tools.jextract.tree.TreeMaker;
import com.sun.tools.jextract.tree.TreePrinter;

/**
 * This visitor filters variable, function, macro trees
 * based on a Tree Predicate initialized.
 */
abstract class TreeFilter extends SimpleTreeVisitor<Tree, Void>
        implements TreePhase {
    private final TreeMaker treeMaker = new TreeMaker();

    private Tree filterTree(Tree tree) {
        return filter(tree)? tree : null;
    }

    @Override
    public HeaderTree transform(HeaderTree ht) {
        return (HeaderTree)ht.accept(this, null);
    }

    @Override
    public Tree defaultAction(Tree tree, Void v) {
        return tree;
    }

    @Override
    public Tree visitFunction(FunctionTree ft, Void v) {
        return filterTree(ft);
    }

    @Override
    public Tree visitMacro(MacroTree mt, Void v) {
        return filterTree(mt);
    }

    @Override
    public Tree visitHeader(HeaderTree ht, Void v) {
        List<Tree> decls =  ht.declarations().stream().
            map(decl -> decl.accept(this, null)).
            filter(decl -> decl != null).
            collect(Collectors.toList());
        return treeMaker.createHeader(ht.cursor(), ht.path(), decls);
    }

    @Override
    public Tree visitVar(VarTree vt, Void v) {
        return filterTree(vt);
    }

    abstract boolean filter(Tree tree);

    // test main to manually check this visitor
    // Usage: <header-file> [<regex-for-symbols-to-include>]
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
        Predicate<Tree> nameFilter =  args.length > 1? t->t.name().matches(args[1]) : t->true;
        TreeFilter filter = new TreeFilter() {
            @Override
            boolean filter(Tree tree) {
                return nameFilter.test(tree);
            }
        };
        filter.transform(header).accept(printer, null);
    }
}
