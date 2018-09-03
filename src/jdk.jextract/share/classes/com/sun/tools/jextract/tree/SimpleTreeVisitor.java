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
package com.sun.tools.jextract.tree;

public class SimpleTreeVisitor<R,P> implements TreeVisitor<R, P> {
    /**
     * The default value, returned by the {@link #defaultAction default action}.
     */
    protected final R DEFAULT_VALUE;

    public SimpleTreeVisitor() {
        DEFAULT_VALUE = null;
    }

    public SimpleTreeVisitor(R defaultValue) {
        DEFAULT_VALUE = defaultValue;
    }

    protected R defaultAction(Tree node, P p) {
        return DEFAULT_VALUE;
    }

    @Override
    public R visitEnum(EnumTree t, P p) {
        return defaultAction(t, p);
    }

    @Override
    public R visitField(FieldTree t, P p) {
        return defaultAction(t, p);
    }

    @Override
    public R visitFunction(FunctionTree t, P p) {
        return defaultAction(t, p);
    }

    @Override
    public R visitHeader(HeaderTree t, P p) {
        return defaultAction(t, p);
    }

    @Override
    public R visitMacro(MacroTree t, P p) {
        return defaultAction(t, p);
    }

    @Override
    public R visitStruct(StructTree t, P p) {
        return defaultAction(t, p);
    }

    @Override
    public R visitTree(Tree t, P p) {
        return defaultAction(t, p);
    }

    @Override
    public R visitTypedef(TypedefTree t, P p) {
        return defaultAction(t, p);
    }

    @Override
    public R visitVar(VarTree t, P p) {
        return defaultAction(t, p);
    }
}
