/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
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
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.function.Predicate;
import com.sun.tools.jextract.tree.Tree;
import com.sun.tools.jextract.tree.HeaderTree;
import com.sun.tools.jextract.tree.Printer;

public class FindSymbol {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Expected a header file");
            return;
        }

        final List<Path> paths = List.of(Paths.get(args[0]));
        final Path builtinInc = Paths.get(System.getProperty("java.home"), "conf", "jextract");
        final List<String> clangArgs = List.of("-I" + builtinInc);

        final Parser parser = new Parser(true);
        final List<HeaderTree> headers = parser.parse(paths, clangArgs);
        final Printer p = new Printer();
        final HeaderTree tu = headers.get(0);

        if (args.length == 1) {
            p.printRecursive(tu, Integer.MAX_VALUE);
            return;
        }

        final Collection<String> set = Arrays.asList(Arrays.copyOfRange(args, 1, args.length));
        final Predicate<Tree> matchName = tree -> tree.isDeclaration() && set.contains(tree.name());
        tu.declarations().stream()
                .filter(matchName)
                .forEach(declTree -> p.printRecursive(declTree, Integer.MAX_VALUE));
    }
}
