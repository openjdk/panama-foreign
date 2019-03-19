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

import com.sun.tools.jextract.tree.Tree;

import java.nio.file.Path;

public class Filters {
    private final PatternFilter headers;
    private final PatternFilter symbols;

    public Filters(PatternFilter headers, PatternFilter symbols) {
        this.headers = headers;
        this.symbols = symbols;
    }

    public static Filters createDefault() {
        return new Filters(PatternFilter.empty(), PatternFilter.empty());
    }

    public boolean filterSymbol(Tree tree) {
        return symbols.filter(tree.name());
    }

    public boolean isInRootHeader(Tree tree) {
        Path path = tree.cursor().getSourceLocation().getFileLocation().path();
        return headers.filter(path.toString());
    }
}
