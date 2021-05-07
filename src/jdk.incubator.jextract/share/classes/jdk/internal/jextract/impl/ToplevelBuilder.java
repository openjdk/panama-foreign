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

import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.jextract.Declaration;
import jdk.incubator.jextract.Type;

import javax.tools.JavaFileObject;
import java.io.File;
import java.lang.constant.ClassDesc;
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
    private final List<JavaSourceBuilder> builders = new ArrayList<>();
    private SplitHeader lastHeader;
    private int headersCount;
    private final ClassDesc headerDesc;
    private final String[] libraryNames;

    static final int DECLS_PER_HEADER_CLASS = Integer.getInteger("jextract.decls.per.header", 1000);

    ToplevelBuilder(String packageName, String headerClassName, String[] libraryNames) {
        this.libraryNames = libraryNames;
        this.headerDesc = ClassDesc.of(packageName, headerClassName);
        SplitHeader first = lastHeader = new FirstHeader(headerClassName);
        first.classBegin();
        builders.add(first);
    }

    public List<JavaFileObject> toFiles() {
        if (constantBuilder != null) {
            constantBuilder.classEnd();
        }
        lastHeader.classEnd();
        builders.addAll(constantBuilders);
        List<JavaFileObject> files = new ArrayList<>();
        files.addAll(builders.stream()
                .flatMap(b -> b.toFiles().stream())
                .collect(Collectors.toList()));
        return files;
    }

    public String headerClassName() {
        return headerDesc.displayName();
    }

    @Override
    boolean isEnclosedBySameName(String name) {
        return false;
    }

    @Override
    public String packageName() {
        return headerDesc.packageName();
    }

    @Override
    public void addVar(String javaName, String nativeName, VarInfo varInfo) {
        nextHeader().addVar(javaName, nativeName, varInfo);
    }

    @Override
    public void addFunction(String javaName, String nativeName, FunctionInfo functionInfo) {
        nextHeader().addFunction(javaName, nativeName, functionInfo);
    }

    @Override
    public void addConstant(String javaName, Class<?> type, Object value) {
        nextHeader().addConstant(javaName, type, value);
    }

    @Override
    public void addTypedef(String name, String superClass, Type type) {
        if (type instanceof Type.Primitive) {
            // primitive
            nextHeader().emitPrimitiveTypedef((Type.Primitive)type, name);
        } else {
            TypedefBuilder builder = new TypedefBuilder(this, name, superClass);
            builders.add(builder);
            builder.classBegin();
            builder.classEnd();
        }
    }

    @Override
    public StructBuilder addStruct(String name, Declaration parent, GroupLayout layout, Type type) {
        String structName = name.isEmpty() ? parent.name() : name;
        StructBuilder structBuilder = new StructBuilder(this, structName, layout, type);
        builders.add(structBuilder);
        return structBuilder;
    }

    @Override
    public String addFunctionalInterface(String name, FunctionInfo functionInfo) {
        FunctionalInterfaceBuilder builder = new FunctionalInterfaceBuilder(this,
                name, functionInfo.methodType(), functionInfo.descriptor());
        builders.add(builder);
        builder.classBegin();
        builder.classEnd();
        return builder.className();
    }

    private SplitHeader nextHeader() {
        if (declCount == DECLS_PER_HEADER_CLASS) {
            boolean hasSuper = !(lastHeader instanceof FirstHeader);
            SplitHeader headerFileBuilder = new SplitHeader(headerDesc.displayName() + "_" + ++headersCount,
                    hasSuper ? lastHeader.className() : null);
            lastHeader.classEnd();
            headerFileBuilder.classBegin();
            builders.add(headerFileBuilder);
            lastHeader = headerFileBuilder;
            declCount = 1;
            return headerFileBuilder;
        } else {
            declCount++;
            return lastHeader;
        }
    }

    class SplitHeader extends HeaderFileBuilder {
        SplitHeader(String name, String superclass) {
            super(ToplevelBuilder.this, name, superclass);
        }

        @Override
        String mods() {
            return " ";
        }
    }

    class FirstHeader extends SplitHeader {

        FirstHeader(String name) {
            super(name, "#{SUPER}");
        }

        @Override
        String mods() {
            return "public ";
        }

        @Override
        void classBegin() {
            super.classBegin();
            emitLibraries(libraryNames);
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
            HeaderFileBuilder last = lastHeader;
            return super.build().replace("extends #{SUPER}",
                    last != this ? "extends " + last.className() : "");
        }

        private void emitLibraries(String[] libraryNames) {
            incrAlign();
            indent();
            append("static {\n");
            incrAlign();
            for (String lib : libraryNames) {
                String quotedLibName = quoteLibraryName(lib);
                indent();
                if (quotedLibName.indexOf(File.separatorChar) != -1) {
                    append("System.load(\"" + quotedLibName + "\");");
                } else {
                    append("System.loadLibrary(\"" + quotedLibName + "\");");
                }
                append("\n");
            }
            decrAlign();
            indent();
            append("}\n\n");
            indent();
            append("static final ");
            append("SymbolLookup LIBRARIES = SymbolLookup.loaderLookup(" + className() + ".class.getClassLoader());");
            decrAlign();
        }

        private String quoteLibraryName(String lib) {
            return lib.replace("\\", "\\\\"); // double up slashes
        }
    }

    // constant support

    int constant_counter = 0;
    int constant_class_index = 0;
    List<ConstantBuilder> constantBuilders = new ArrayList<>();

    static final int CONSTANTS_PER_CLASS = Integer.getInteger("jextract.constants.per.class", 5);
    ConstantBuilder constantBuilder;

    protected void emitWithConstantClass(Consumer<ConstantBuilder> constantConsumer) {
        if (constant_counter > CONSTANTS_PER_CLASS || constantBuilder == null) {
            if (constantBuilder != null) {
                constantBuilder.classEnd();
            }
            constant_counter = 0;
            constantBuilder = new ConstantBuilder(this, "constants$" + constant_class_index++) {
                @Override
                String mods() {
                    return ""; // constants package-private!
                }
            };
            constantBuilders.add(constantBuilder);
            constantBuilder.classBegin();
        }
        constantConsumer.accept(constantBuilder);
        constant_counter++;
    }
}
