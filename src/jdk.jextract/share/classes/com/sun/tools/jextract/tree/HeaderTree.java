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

import java.nio.file.Path;
import java.util.List;
import java.util.Collections;
import jdk.internal.clang.Cursor;

public class HeaderTree extends Tree {
    private final Path path;
    private final List<Tree> declarations;

    public HeaderTree(Cursor c, Path path, List<Tree> declarations) {
        super(c);
        this.path = path;
        this.declarations = Collections.unmodifiableList(declarations);
    }

    public Path path() {
        return path;
    }

    public List<Tree> declarations() {
        return declarations;
    }

    @Override
    public <R,D> R accept(TreeVisitor<R,D> visitor, D data) {
        return visitor.visitHeader(this, data);
    }
}
