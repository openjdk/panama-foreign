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
import jdk.incubator.jextract.Declaration;
import jdk.incubator.jextract.Type;

import javax.tools.JavaFileObject;
import java.lang.constant.ClassDesc;
import java.lang.invoke.MethodType;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A helper class to generate header interface class in source form.
 * After aggregating various constituents of a .java source, build
 * method is called to get overall generated source string.
 */
class ToplevelBuilder extends JavaSourceBuilder {

    private int declCount;
    private final String[] libraryNames;
    private final List<SplitHeader> headers = new ArrayList<>();

    static final int DECLS_PER_HEADER_CLASS = Integer.getInteger("jextract.decls.per.header", 1000);

    ToplevelBuilder(ClassDesc desc, String[] libraryNames) {
        super(Kind.CLASS, desc);
        this.libraryNames = libraryNames;
        SplitHeader first = new FirstHeader(ClassDesc.of(packageName(), className()));
        first.classBegin();
        headers.add(first);
    }

    @Override
    void classBegin() {
        throw new UnsupportedOperationException();
    }

    @Override
    JavaSourceBuilder classEnd() {
        throw new UnsupportedOperationException();
    }

    public List<JavaFileObject> toFiles() {
        headers.stream().skip(1).findFirst()
                .orElse(lastHeader()).emitLibraries(libraryNames);
        List<JavaFileObject> files = new ArrayList<>();
        files.addAll(headers.stream()
                .flatMap(hf -> hf.toFiles().stream())
                .collect(Collectors.toList()));
        return files;
    }

    @Override
    public void addVar(String javaName, String nativeName, MemoryLayout layout, Class<?> type) {
        nextHeader().addVar(javaName, nativeName, layout, type);
    }

    @Override
    public void addFunction(String javaName, String nativeName, MethodType mtype, FunctionDescriptor desc, boolean varargs, List<String> paramNames) {
        nextHeader().addFunction(javaName, nativeName, mtype, desc, varargs, paramNames);
    }

    @Override
    public void addConstant(String javaName, Class<?> type, Object value) {
        nextHeader().addConstant(javaName, type, value);
    }

    @Override
    public void addTypedef(String name, String superClass, Type type) {
        nextHeader().addTypedef(name, superClass, type);
    }

    @Override
    public StructBuilder addStruct(String name, Declaration parent, GroupLayout layout, Type type) {
        return nextHeader().addStruct(name, parent, layout, type);
    }

    @Override
    public void addFunctionalInterface(String name, MethodType mtype, FunctionDescriptor desc) {
        nextHeader().addFunctionalInterface(name, mtype, desc);
    }

    private SplitHeader lastHeader() {
        return headers.get(headers.size() - 1);
    }

    private SplitHeader nextHeader() {
        if (declCount == DECLS_PER_HEADER_CLASS) {
            boolean hasSuper = !(lastHeader() instanceof FirstHeader);
            SplitHeader headerFileBuilder = new SplitHeader(ClassDesc.of(packageName(), className() + "_" + headers.size()),
                    hasSuper ? lastHeader().className() : null);
            headerFileBuilder.classBegin();
            headers.add(headerFileBuilder);
            declCount = 1;
            return headerFileBuilder;
        } else {
            declCount++;
            return lastHeader();
        }
    }

    static class SplitHeader extends HeaderFileBuilder {
        SplitHeader(ClassDesc desc, String superclass) {
            super(desc, superclass);
        }

        @Override
        String mods() {
            return " ";
        }

        private void emitLibraries(String[] libraryNames) {
            incrAlign();
            indent();
            append("static final ");
            append("LibraryLookup[] LIBRARIES = RuntimeHelper.libraries(new String[] {\n");
            incrAlign();
            for (String lib : libraryNames) {
                indent();
                append('\"');
                append(quoteLibraryName(lib));
                append("\",\n");
            }
            decrAlign();
            indent();
            append("});\n\n");
            decrAlign();
        }

        private static String quoteLibraryName(String lib) {
            return lib.replace("\\", "\\\\"); // double up slashes
        }
    }

    class FirstHeader extends SplitHeader {

        FirstHeader(ClassDesc desc) {
            super(desc, "#{SUPER}");
        }

        @Override
        String mods() {
            return "public ";
        }

        @Override
        void classBegin() {
            super.classBegin();
            emitConstructor();
        }

        void emitConstructor() {
            incrAlign();
            indent();
            append("/* package-private */ ");
            append(className());
            append("() {}");
            append('\n');
            decrAlign();
        }

        @Override
        String build() {
            HeaderFileBuilder last = lastHeader();
            return super.build().replace("extends #{SUPER}",
                    last != this ? "extends " + last.className() : "");
        }
    }
}
