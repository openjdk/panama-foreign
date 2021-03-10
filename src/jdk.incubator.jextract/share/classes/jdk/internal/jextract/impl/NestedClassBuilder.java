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

import javax.tools.JavaFileObject;
import java.lang.constant.ClassDesc;
import java.util.List;
import java.util.function.Consumer;

public class NestedClassBuilder extends JavaSourceBuilder {

    protected final JavaSourceBuilder enclosing;

    public NestedClassBuilder(JavaSourceBuilder enclosing, Kind kind, String className) {
        super(enclosing.align(), kind, ClassDesc.of(enclosing.packageName(), enclosing.uniqueNestedClassName(className)));
        this.enclosing = enclosing;
    }

    @Override
    protected String mods() {
        return kind == Kind.INTERFACE ?
                "public " : "public static ";
    }

    @Override
    boolean isEnclosedBySameName(String name) {
        return super.isEnclosedBySameName(name) || enclosing.isEnclosedBySameName(name);
    }

    @Override
    String fullName() {
        return enclosing.className() + "." + className();
    }

    @Override
    void classBegin() {
        incrAlign();
        super.classBegin();
    }

    @Override
    JavaSourceBuilder classEnd() {
        super.classEnd();
        decrAlign();
        enclosing.append(build());
        return enclosing;
    }

    @Override
    protected void emitPackagePrefix() {
        // nested class. containing class has necessary package declaration
    }

    @Override
    protected void emitImportSection() {
        // nested class. containing class has necessary imports
    }

    @Override
    public List<JavaFileObject> toFiles() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void emitWithConstantClass(String javaName, Consumer<ConstantBuilder> constantConsumer) {
        if (this instanceof ConstantBuilder cb) {
            // use this class to emit constants
            constantConsumer.accept(cb);
        } else {
            enclosing.emitWithConstantClass(javaName, constantConsumer);
        }
    }
}
