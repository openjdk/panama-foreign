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
import java.util.Arrays;
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
import com.sun.tools.jextract.tree.TreePrinter;
import jdk.internal.clang.Cursor;

/**
 * This tree visitor handles the tree empty names encountered in the tree
 * so that subsequent passes need not check tree.name() for empty string.
 *
 * 1. Names are generated & set for anonymous structs & unions.
 * 2. Anonymous (bit) FieldTree instances are removed.
 */
final class EmptyNameHandler extends SimpleTreeVisitor<Tree, Void> {
    private final TreeMaker treeMaker = new TreeMaker();

    HeaderTree transform(HeaderTree ht) {
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
    public Tree visitStruct(StructTree s, Void v) {
        // Common simple case. No nested names and no anonymous field names.
        // We just need to check struct name itself is empty or not.
        if (s.nestedTypes().isEmpty() && !hasAnonymousFields(s)) {
            /*
             * Examples:
             *
             *   struct { int i } x; // global variable of anon. struct type
             *   void func(struct { int x; } p); // param of anon. struct type
             */
            if (s.name().isEmpty()) {
                return s.withName(generateName(s));
            } else {
                // all fine with this struct
                return s;
            }
        } else {
            // handle all nested types
            return renameRecursively(s);
        }
    }

    // does the given struct has any anonymous (bit) field?
    private boolean hasAnonymousFields(StructTree s) {
        return s.fields().stream().map(f -> f.name().isEmpty()).findFirst().isPresent();
    }

    private StructTree renameRecursively(StructTree s) {
        List<Tree> newDecls = s.declarations().stream().map(decl -> {
            if (decl instanceof StructTree) {
                return renameRecursively((StructTree)decl);
            } else if (decl instanceof FieldTree && decl.name().isEmpty()) {
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
                return decl;
            }
        }).filter(d -> d != null).collect(Collectors.toList());

        return s.withNameAndDecls(generateName(s), newDecls);
    }

    // test main to manually check this visitor
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
        EmptyNameHandler handler = new EmptyNameHandler();
        for (HeaderTree ht : headers) {
            handler.transform(ht).accept(printer, null);
        }
    }
}
