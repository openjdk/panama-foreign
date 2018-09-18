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

import java.foreign.layout.Function;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import jdk.internal.clang.Cursor;
import jdk.internal.clang.Type;

public class FunctionTree extends Tree {
    FunctionTree(Cursor c) {
        this(c, c.spelling());
    }

    private FunctionTree(Cursor c, String name) {
        super(c, name);
    }

    @Override
    public FunctionTree withName(String newName) {
        return name().equals(newName)? this : new FunctionTree(cursor(), newName);
    }

    @Override
    public <R,D> R accept(TreeVisitor<R,D> visitor, D data) {
        return visitor.visitFunction(this, data);
    }

    public boolean isVariadic() {
        return type().isVariadic();
    }

    public List<Type> paramTypes() {
        Type t = type();
        int numParams = t.numberOfArgs();
        List<Type> res = new ArrayList<>(numParams);
        for (int i = 0; i < numParams; i++) {
            res.add(t.argType(i));
        }
        return Collections.unmodifiableList(res);
    }

    public int numParams() {
        return type().numberOfArgs();
    }

    public Type paramType(int idx) {
        return type().argType(idx);
    }

    public String paramName(int idx) {
        return cursor().getArgument(idx).spelling();
    }

    public Type returnType() {
        return type().resultType();
    }

    public Function function() {
        return LayoutUtils.getFunction(type());
    }
}
