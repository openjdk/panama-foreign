/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.tools.jextract;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import com.sun.tools.jextract.parser.MacroParser;
import com.sun.tools.jextract.tree.Tree;
import com.sun.tools.jextract.tree.EnumTree;
import com.sun.tools.jextract.tree.FieldTree;
import com.sun.tools.jextract.tree.FunctionTree;
import com.sun.tools.jextract.tree.MacroTree;
import com.sun.tools.jextract.tree.VarTree;

/**
 * This extended factory generates a class with only static methods and fields. A native
 * library interface instance (of the given header file) is kept as a static private field.
 * One static method is generated for every library interface method. Enum and macro constants
 * are mapped to static final fields. By importing the "static forwarder" class, the user code
 * looks more or less like C code. Libraries.bind and header interface usage is hidden.
 */
final class JavaSourceFactoryExt extends JavaSourceFactory {
    // field name for the header interface instance.
    private static final String STATICS_LIBRARY_FIELD_NAME = "_theLibrary";
    private final JavaSourceBuilderExt header_jsb;
    private final List<JavaSourceBuilderExt> enums;

    JavaSourceFactoryExt(Context ctx, HeaderFile header) {
        super(ctx, header);
        log.print(Level.INFO, () -> "Instantiate StaticForwarderGenerator for " + header.path);
        header_jsb = new JavaSourceBuilderExt();
        enums = new ArrayList<>();
    }

    @Override
    public Map<String, String> generate(List<Tree> decls) {
        header_jsb.addPackagePrefix(headerFile.pkgName);
        String ifaceClsName = headerFile.headerClsName;
        String forwarderName = headerFile.staticForwarderClsName;
        header_jsb.classBegin(forwarderName, false);

        header_jsb.addLibraryField(ifaceClsName, STATICS_LIBRARY_FIELD_NAME);
        header_jsb.emitScopeAccessor(STATICS_LIBRARY_FIELD_NAME);

        Map<String, String> srcMap = super.generate(decls);
        enums.forEach(header_jsb::addNestedType);

        header_jsb.classEnd();
        String src = header_jsb.build();
        if (srcDir != null) {
            try {
                Path srcPath = srcDir.resolve(forwarderName + ".java");
                Files.write(srcPath, List.of(src));
            } catch (Exception ex) {
                handleException(ex);
            }
        }
        srcMap.put(headerFile.pkgName + "." + forwarderName, src);
        return srcMap;
    }

    @Override
    public Boolean visitVar(VarTree varTree, JType jt) {
        if (super.visitVar(varTree, jt)) {
            String fieldName = varTree.name();
            assert !fieldName.isEmpty();
            header_jsb.emitStaticForwarder(varTree, jt, STATICS_LIBRARY_FIELD_NAME);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Boolean visitEnum(EnumTree enumTree, JType jt) {
        if (super.visitEnum(enumTree, jt)) {
            if (enumTree.name().isEmpty()) {
                enumTree.constants().forEach(constant -> addConstant(header_jsb, constant.name(),
                    headerFile.dictionary().lookup(constant.type()),
                    constant.enumConstant().get()));
            } else {
                JavaSourceBuilderExt enumBuilder = new JavaSourceBuilderExt(header_jsb.align() + 1);
                enumBuilder.interfaceBegin(enumTree.name(), true);
                enumTree.constants().forEach(constant -> addConstant(enumBuilder, constant.name(),
                    headerFile.dictionary().lookup(constant.type()),
                    constant.enumConstant().get()));
                enumBuilder.interfaceEnd();
                enums.add(enumBuilder);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Boolean visitFunction(FunctionTree funcTree, JType jt) {
        if (super.visitFunction(funcTree, jt)) {
            header_jsb.emitStaticForwarder(funcTree, (JType.Function)jt, STATICS_LIBRARY_FIELD_NAME);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Boolean visitMacro(MacroTree macroTree, JType jt) {
        if (super.visitMacro(macroTree, jt)) {
            String name = macroTree.name();
            MacroParser.Macro macro = macroTree.macro().get();
            log.print(Level.FINE, () -> "Adding macro " + name);
            addConstant(header_jsb, Utils.toMacroName(name), macro.type(), macro.value());
            return true;
        } else {
            return false;
        }
    }

    private void addConstant(JavaSourceBuilderExt jsb, String name, JType type, Object value) {
        Object constantValue = makeConstantValue(type, value);
        jsb.addField(name, type, constantValue, STATICS_LIBRARY_FIELD_NAME);
    }

    private Object makeConstantValue(JType type, Object value) {
        switch (type.getDescriptor()) {
            case "Z":
                return ((long)value) != 0;
            case "C":
                return (char)(long)value;
            case "B":
                return (byte)(long)value;
            case "S":
                return (short)(long)value;
            case "I":
                return (int)(long)value;
            case "F":
                return (float)(double)value;
            case "J": case "D":
                return value;
            default:
                return null;
        }
    }
}
