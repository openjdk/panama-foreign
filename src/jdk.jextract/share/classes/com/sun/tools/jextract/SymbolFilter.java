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

import com.sun.tools.jextract.tree.FunctionTree;
import com.sun.tools.jextract.tree.MacroTree;
import com.sun.tools.jextract.tree.Tree;
import com.sun.tools.jextract.tree.TypedefTree;
import com.sun.tools.jextract.tree.VarTree;

import java.foreign.Libraries;
import java.foreign.Library;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Filters functions, global variable and macros based on whether
 * 1. they appear in a root header,
 * 2. they pass the symbol filter patterns.
 */
public class SymbolFilter extends TreeFilter {
    private final Filters filters;

    public SymbolFilter(Context ctx) {
        this.filters = ctx.filters;
    }

    private boolean filter(Tree tree) {
        return filters.isInRootHeader(tree) && filters.filterSymbol(tree);
    }

    @Override
    public Boolean visitVar(VarTree vt, Void v) {
        return filter(vt);
    }

    @Override
    public Boolean visitFunction(FunctionTree ft, Void v) {
        return filter(ft);
    }

    @Override
    public Boolean visitMacro(MacroTree mt, Void v) {
        return filter(mt);
    }

    @Override
    public Boolean visitTypedef(TypedefTree tt, Void v) {
        return filters.filterSymbol(tt);
    }
}
