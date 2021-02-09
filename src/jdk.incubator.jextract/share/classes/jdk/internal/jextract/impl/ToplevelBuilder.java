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
import jdk.incubator.jextract.Type;

import javax.tools.JavaFileObject;
import java.lang.invoke.MethodType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A helper class to generate header interface class in source form.
 * After aggregating various constituents of a .java source, build
 * method is called to get overall generated source string.
 */
class ToplevelBuilder extends HeaderFileBuilder {

    private int declCount;
    private String[] libraryNames;

    static final int DECLS_PER_HEADER_CLASS = Integer.getInteger("jextract.decls.per.header", 1000);

    ToplevelBuilder(String headerFileName, String pkgName, String[] libraryNames) {
        super(pkgName, headerFileName, null, true);
        this.libraryNames = libraryNames;
        classBegin();
    }

    @Override
    void classBegin() {
        super.classBegin();
        emitConstructor();
    }

    @Override
    String superClass() {
        return "#{SUPER}";
    }

    @Override
    protected String getClassModifiers() {
        return PUB_CLS_MODS;
    }

    public List<JavaFileObject> build() {
        JavaSourceBuilder librariesBuilder = headers.stream().findFirst().orElse(this);
        emitLibraries(librariesBuilder.builder, libraryNames);
        classEnd();
        String res = builder.build().replace("extends #{SUPER}",
                lastHeader().map(h -> "extends " + h.className).orElse(""));
        List<JavaFileObject> files = new ArrayList<>();
        files.add(Utils.fileFromString(pkgName, className, res));
        files.addAll(headers.stream()
                .flatMap(hf -> hf.build().stream())
                .collect(Collectors.toList()));
        return files;
    }

    void emitConstructor() {
        builder.incrAlign();
        builder.indent();
        builder.append("/* package-private */ ");
        builder.append(className);
        builder.append("() {}");
        builder.append('\n');
        builder.decrAlign();
    }

    private List<HeaderFileBuilder> headers = new ArrayList<>();

    Optional<HeaderFileBuilder> lastHeader() {
        return headers.size() == 0 ?
                Optional.empty() :
                Optional.of(headers.get(headers.size() - 1));
    }

    HeaderFileBuilder nextHeader() {
        if (declCount > DECLS_PER_HEADER_CLASS) {
            HeaderFileBuilder headerFileBuilder = new HeaderFileBuilder(pkgName, className + "_" + headers.size(),
                    lastHeader().map(h -> h.className).orElse(null), false);
            headerFileBuilder.classBegin();
            headers.add(headerFileBuilder);
            declCount = 1;
            return headerFileBuilder;
        } else {
            declCount++;
            return lastHeader().orElse(this);
        }
    }

    private void emitLibraries(StringSourceBuilder builder, String[] libraryNames) {
        builder.incrAlign();
        builder.indent();
        builder.append("static final ");
        builder.append("LibraryLookup[] LIBRARIES = RuntimeHelper.libraries(new String[] {\n");
        builder.incrAlign();
        for (String lib : libraryNames) {
            builder.indent();
            builder.append('\"');
            builder.append(quoteLibraryName(lib));
            builder.append("\",\n");
        }
        builder.decrAlign();
        builder.indent();
        builder.append("});\n\n");
        builder.decrAlign();
    }

    private static String quoteLibraryName(String lib) {
        return lib.replace("\\", "\\\\"); // double up slashes
    }
}
