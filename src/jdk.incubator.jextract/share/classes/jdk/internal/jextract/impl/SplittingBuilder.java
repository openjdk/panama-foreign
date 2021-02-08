/*
 * Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
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

import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.jextract.Type;

import javax.tools.JavaFileObject;
import java.lang.invoke.MethodType;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * A helper class to generate header interface class in source form.
 * After aggregating various constituents of a .java source, build
 * method is called to get overall generated source string.
 */
class SplittingBuilder extends JavaSourceBuilder {

    private int declCount;

    static final int DECLS_PER_HEADER_CLASS = Integer.getInteger("jextract.decls.per.header", 1000);

    interface BuilderFactory {
        JavaSourceBuilder make(String pkgName, String className, String superclass, ConstantHelper helper, boolean isPublic);
    }

    final BuilderFactory builderFactory;

    SplittingBuilder(Kind kind, String className, String pkgName, ConstantHelper constantHelper, BuilderFactory builderFactory) {
        super(null, kind, className, pkgName, constantHelper);
        this.builderFactory = builderFactory;
        addBuilder(builderFactory.make(pkgName, className, "#{SUPER}", constantHelper, true));
    }

    @Override
    public List<JavaFileObject> build() {
        List<JavaFileObject> files = new ArrayList<>(builders.size());
        boolean first = true;
        for (JavaSourceBuilder builder : builders) {
            if (first) {
                files.addAll(builder.build(s -> s.replace("extends #{SUPER}",
                        builders.size() == 1 ? "" : ("extends " + current().className))));
            } else {
                files.addAll(builder.build());
            }
        }
        return files;
    }

    private List<JavaSourceBuilder> builders = new ArrayList<>();

    JavaSourceBuilder current() {
        return builders.get(builders.size() - 1);
    }

    JavaSourceBuilder nextBuilder() {
        if (declCount > DECLS_PER_HEADER_CLASS) {
            JavaSourceBuilder newBuilder = builderFactory.make(pkgName, className + "_" + builders.size(),
                    builders.size() == 1 ? null : current().className, constantHelper, false);
            addBuilder(newBuilder);
            return newBuilder;
        } else {
            declCount++;
            return current();
        }
    }

    void addBuilder(JavaSourceBuilder builder) {
        builder.classBegin();
        builders.add(builder);
        declCount = 1;
    }

    @Override
    public void addVar(String javaName, String nativeName, MemoryLayout layout, Class<?> type) {
        current().addVar(javaName, nativeName, layout, type);
    }

    @Override
    public void addFunction(String javaName, String nativeName, MethodType mtype, FunctionDescriptor desc, boolean varargs, List<String> paramNames) {
        current().addFunction(javaName, nativeName, mtype, desc, varargs, paramNames);
    }

    @Override
    public void addConstant(String javaName, Class<?> type, Object value) {
        current().addConstant(javaName, type, value);
    }

    @Override
    public void addTypedef(String name, String superClass, Type type) {
        current().addTypedef(name, superClass, type);
    }

    @Override
    public StructBuilder addStruct(String name, GroupLayout parentLayout, Type type) {
        return current().addStruct(name, parentLayout, type);
    }

    @Override
    public void addFunctionalInterface(String name, MethodType mtype, FunctionDescriptor desc, Type type) {
        current().addFunctionalInterface(name, mtype, desc, type);
    }
}
