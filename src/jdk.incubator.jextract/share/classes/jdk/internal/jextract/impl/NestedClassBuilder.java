/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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

package jdk.internal.jextract.impl;

public abstract class NestedClassBuilder extends JavaSourceBuilder {

    JavaSourceBuilder prev;

    public NestedClassBuilder(JavaSourceBuilder prev, Kind kind, String className) {
        super(kind, prev.uniqueNestedClassName(className), prev.pkgName, prev.constantHelper, prev.annotationWriter);
        this.prev = prev;
    }

    JavaSourceBuilder prev() {
        return prev;
    }

    @Override
    void append(String s) {
        prev.append(s);
    }

    @Override
    void append(char c) {
        prev.append(c);
    }

    @Override
    void append(long l) {
        prev.append(l);
    }

    @Override
    void indent() {
        prev.indent();
    }

    @Override
    void incrAlign() {
        prev.incrAlign();
    }

    @Override
    void decrAlign() {
        prev.decrAlign();
    }

    @Override
    protected String getClassModifiers() {
        return PUB_MODS;
    }

    @Override
    protected void addPackagePrefix() {
        // nested class. containing class has necessary package declaration
    }

    @Override
    protected void addImportSection() {
        // nested class. containing class has necessary imports
    }
}
