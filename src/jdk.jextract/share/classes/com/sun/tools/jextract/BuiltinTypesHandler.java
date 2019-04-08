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
import java.util.List;
import java.util.logging.Logger;
import com.sun.tools.jextract.parser.Parser;
import com.sun.tools.jextract.tree.HeaderTree;
import com.sun.tools.jextract.tree.SimpleTreeVisitor;
import com.sun.tools.jextract.tree.Tree;
import com.sun.tools.jextract.tree.TreeMaker;
import com.sun.tools.jextract.tree.TreePhase;
import com.sun.tools.jextract.tree.TreePrinter;
import jdk.internal.clang.Cursor;

/**
 * Handles builtin struct/union types by creating fake Trees in a
 * builtin header tree.
 */
final class BuiltinTypesHandler extends SimpleTreeVisitor<Void, Void>
        implements TreePhase {
    private final TreeMaker treeMaker = new TreeMaker();

    // Potential Tree instances that will go into transformed HeaderTree
    // are collected in this list.
    private List<Tree> decls = new ArrayList<>();
    private final Log log;

    public BuiltinTypesHandler(Context ctx) {
        this.log = ctx.log;
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
        /*
         * There are built-in types like struct __va_list_tag. These are not exposed
         * as declaration cursor for any headers, but are available from Type objects.
         * We've to walk and check null file path to detect these Cursors and create Trees.
         */
        Utils.getBuiltinRecordTypes(tree.type()).forEach(c -> {
            decls.add(treeMaker.createTree(c));
        });

        decls.add(tree);
        return null;
    }

    @Override
    public Void visitHeader(HeaderTree ht, Void v) {
        ht.declarations().forEach(decl -> decl.accept(this, null));
        return null;
    }

    // test main to manually check this visitor
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Expected a header file");
            return;
        }

        Context context = Context.createDefault();
        Parser p = new Parser(context, true);
        Path builtinInc = Paths.get(System.getProperty("java.home"), "conf", "jextract");
        List<String> clangArgs = List.of("-I" + builtinInc);
        HeaderTree header = p.parse(Paths.get(args[0]), clangArgs);
        TreePrinter printer = new TreePrinter();
        BuiltinTypesHandler handler = new BuiltinTypesHandler(context);
        handler.transform(header).accept(printer, null);
    }
}
