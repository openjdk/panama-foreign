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

package jdk.internal.jextract.impl;

import javax.tools.JavaFileObject;
import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MultiConstantHelper implements ConstantHelper {
    int constant_counter = 0;
    int constant_class_index = 0;

    static final int CONSTANTS_PER_CLASS = Integer.getInteger("jextract.constants.per.class", 5);
    ConstantBuilder constantBuilder;
    final String packageName;

    final List<ConstantBuilder> constantBuilders = new ArrayList<>();

    public MultiConstantHelper(String packageName, String[] libraryNames) {
        this.constantBuilder = new FirstConstant(this, ClassDesc.of(packageName, nextConstantClassName()), libraryNames);
        this.packageName = packageName;
        constantBuilder.classBegin();
        constantBuilders.add(constantBuilder);
    }

    @Override
    public String librariesClass() {
        return constantBuilders.get(0).className();
    }

    private String nextConstantClassName() {
        return "constants$" + constant_class_index++;
    }

    @Override
    public void emitWithConstantClass(Consumer<ConstantBuilder> constantConsumer) {
        if (constant_counter > CONSTANTS_PER_CLASS || constantBuilder == null) {
            if (constantBuilder != null) {
                constantBuilder.classEnd();
            }
            constant_counter = 0;
            constantBuilder = new ConstantBuilder(this, JavaSourceBuilder.Kind.CLASS, ClassDesc.of(packageName, nextConstantClassName()));
            constantBuilders.add(constantBuilder);
            constantBuilder.classBegin();
        }
        constantConsumer.accept(constantBuilder);
        constant_counter++;
    }

    public List<JavaFileObject> toFiles() {
        if (constantBuilder != null) {
            constantBuilder.classEnd();
        }
        List<JavaFileObject> files = new ArrayList<>();
        files.addAll(constantBuilders.stream()
                .flatMap(b -> b.toFiles().stream())
                .collect(Collectors.toList()));
        return files;
    }

    static class FirstConstant extends ConstantBuilder {

        final String[] libraryNames;

        public FirstConstant(MultiConstantHelper constantHelper, ClassDesc desc, String[] libraryNames) {
            super(constantHelper, Kind.CLASS, desc);
            this.libraryNames = libraryNames;
        }

        @Override
        void classBegin() {
            super.classBegin();
            emitLibraries(libraryNames);
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

        private String quoteLibraryName(String lib) {
            return lib.replace("\\", "\\\\"); // double up slashes
        }

    }
}
