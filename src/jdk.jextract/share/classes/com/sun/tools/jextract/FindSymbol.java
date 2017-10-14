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

package com.sun.tools.jextract;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import jdk.internal.clang.Cursor;
import jdk.internal.clang.Index;
import jdk.internal.clang.LibClang;

public class FindSymbol {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("libclang version: " + LibClang.version());
            return;
        }

        Index index = LibClang.createIndex();
        Cursor tuCursor = index.parse(args[0], diag -> { System.err.println(diag.toString()); }, false);
        final Printer p = new Printer();

        if (args.length == 1) {
            p.printTree(tuCursor, Integer.MAX_VALUE);
            return;
        }

        final Collection<String> set = Arrays.asList(Arrays.copyOfRange(args, 1, args.length));
        Predicate<Cursor> matchName = c -> c.isDeclaration() && set.contains(c.spelling());
        tuCursor.stream()
                .filter(matchName)
                .forEach(c -> p.printTree(c, Integer.MAX_VALUE));
    }
}
