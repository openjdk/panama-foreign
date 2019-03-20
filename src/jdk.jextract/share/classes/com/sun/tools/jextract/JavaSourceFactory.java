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

import java.foreign.layout.Layout;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.tools.jextract.parser.MacroParser;
import jdk.internal.clang.SourceLocation;
import jdk.internal.clang.Type;
import com.sun.tools.jextract.tree.EnumTree;
import com.sun.tools.jextract.tree.FieldTree;
import com.sun.tools.jextract.tree.FunctionTree;
import com.sun.tools.jextract.tree.MacroTree;
import com.sun.tools.jextract.tree.SimpleTreeVisitor;
import com.sun.tools.jextract.tree.StructTree;
import com.sun.tools.jextract.tree.Tree;
import com.sun.tools.jextract.tree.TypedefTree;
import com.sun.tools.jextract.tree.VarTree;

/*
 * Scan a header file and generate Java source items for entities defined in that header
 * file. Tree visitor visit methods return true/false depending on whether a
 * particular Tree is processed or skipped.
 */
class JavaSourceFactory extends SimpleTreeVisitor<Boolean, JType> {
    // simple names of java.foreign annotations
    private static final String NATIVE_CALLBACK = "NativeCallback";
    private static final String NATIVE_HEADER = "NativeHeader";
    private static final String NATIVE_LOCATION = "NativeLocation";
    private static final String NATIVE_STRUCT = "NativeStruct";
    private static final String NATIVE_FUNCTION = "NativeFunction";
    private static final String NATIVE_GETTER = "NativeGetter";
    private static final String NATIVE_SETTER = "NativeSetter";
    private static final String NATIVE_ADDRESSOF = "NativeAddressof";
    private static final String NATIVE_NUM_CONST = "NativeNumericConstant";
    private static final String NATIVE_STR_CONST = "NativeStringConstant";

    protected final String headerClassName;
    protected final HeaderFile headerFile;
    private final Map<String, JavaSourceBuilder> types;
    private final List<String> libraryNames;
    private final List<String> libraryPaths;
    private final boolean noNativeLocations;
    private final JavaSourceBuilder global_jsb;
    protected final Path srcDir;
    protected final Log log;

    JavaSourceFactory(Context ctx, HeaderFile header) {
        this.log = ctx.log;
        log.print(Level.INFO, () -> "Instantiate JavaSourceFactory for " + header.path);
        this.headerFile = header;
        this.headerClassName = headerFile.pkgName + "." + headerFile.clsName;
        this.types = new HashMap<>();
        this.libraryNames = ctx.options.libraryNames;
        this.libraryPaths = ctx.options.recordLibraryPath? ctx.options.libraryPaths : null;
        this.noNativeLocations = ctx.options.noNativeLocations;
        this.global_jsb = new JavaSourceBuilder();
        this.srcDir = Paths.get(ctx.options.srcDumpDir)
            .resolve(headerFile.pkgName.replace('.', File.separatorChar));
    }

    // main entry point that generates & saves .java files for the header file
    public void generate(List<Tree> decls) {
        global_jsb.addPackagePrefix(headerFile.pkgName);

        Map<String, Object> header = new HashMap<>();
        header.put("path", headerFile.path.toAbsolutePath().toString());
        if (!libraryNames.isEmpty()) {
            header.put("libraries", libraryNames.toArray(new String[0]));
            if (libraryPaths != null && !libraryPaths.isEmpty()) {
                header.put("libraryPaths", libraryPaths.toArray(new String[0]));
            }
        }

        JType.ClassType[] classes = headerFile.dictionary().resolutionRoots()
              .toArray(JType.ClassType[]::new);
        if (classes.length != 0) {
            header.put("resolutionContext", classes);
        }

        Set<Layout> global_layouts = new LinkedHashSet<>();
        for (Tree tr : decls) {
            if (tr instanceof VarTree) {
                VarTree varTree = (VarTree)tr;
                global_layouts.add(varTree.layout().withAnnotation(Layout.NAME, varTree.name()));
            }
        }
        if (!global_layouts.isEmpty()) {
            String[] globals = global_layouts.stream().map(Object::toString).toArray(String[]::new);
            header.put("globals", globals);
        }

        global_jsb.addAnnotation(false, NATIVE_HEADER, header);
        String clsName = headerFile.clsName;
        global_jsb.interfaceBegin(clsName, false);

        //generate all decls
        decls.forEach(this::generateDecl);
        //generate functional interfaces
        headerFile.dictionary().functionalInterfaces()
                .forEach(fi -> createFunctionalInterface((JType.FunctionalInterfaceType)fi));

        for (JavaSourceBuilder jsb : types.values()) {
            global_jsb.addNestedType(jsb);
        }

        global_jsb.interfaceEnd();
        String src = global_jsb.build();
        try {
            Files.createDirectories(srcDir);
            Path srcPath = srcDir.resolve(clsName + ".java");
            Files.write(srcPath, List.of(src));
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    protected void handleException(Exception ex) {
        log.printError("cannot.write.class.file", headerFile.pkgName + "." + headerFile.clsName, ex);
        log.printStackTrace(ex);
    }

    private void addNativeLocation(JavaSourceBuilder jsb, Tree tree) {
        addNativeLocation(jsb, tree.location());
    }

    private void addNativeLocation(JavaSourceBuilder jsb, SourceLocation src) {
        addNativeLocation(true, jsb, src);
    }

    private void addNativeLocation(boolean align, JavaSourceBuilder jsb, SourceLocation src) {
        if (! noNativeLocations) {
            SourceLocation.Location loc = src.getFileLocation();
            Path p = loc.path();
            Map<String, Object> fields = new HashMap<>();
            fields.put("file", p == null ? "builtin" :  p.toAbsolutePath().toString());
            fields.put("line", loc.line());
            fields.put("column", loc.column());
            jsb.addAnnotation(align, NATIVE_LOCATION, fields);
        }
    }

    private void addClassIfNeeded(String clsName, JavaSourceBuilder jsb) {
        if (null != types.put(clsName, jsb)) {
            log.printWarning("warn.class.overwritten", clsName);
        }
    }

    private static boolean isBitField(Tree tree) {
        return tree instanceof FieldTree && ((FieldTree)tree).isBitField();
    }

    /**
     *
     * @param jsb JavaSourceBuilder for the struct
     * @param tree The Tree
     * @param parentType The struct type
     */
    private boolean addField(JavaSourceBuilder jsb, Tree tree, Type parentType) {
        String fieldName = tree.name();
        assert !fieldName.isEmpty();
        Type type = tree.type();
        JType jt = headerFile.dictionary().lookup(type);
        assert (jt != null);

        addNativeLocation(jsb, tree);
        jsb.addGetter(fieldName + "$get", jt);
        jsb.addSetter(fieldName + "$set", jt);

        if (tree instanceof VarTree || !isBitField(tree)) {
            JType ptrType = JType.GenericType.ofPointer(jt);
            jsb.addAnnotation(NATIVE_ADDRESSOF, Map.of("value", fieldName));
            jsb.addGetter(fieldName + "$ptr", ptrType);
        }

        return true;
    }

    @Override
    public Boolean visitVar(VarTree varTree, JType jt) {
        return addField(global_jsb, varTree, null);
    }

    private void addConstant(JavaSourceBuilder jsb, SourceLocation src, String name, JType type, Object value) {
        addNativeLocation(jsb, src);

        if (value instanceof String) {
            jsb.addAnnotation(NATIVE_STR_CONST, Map.of("value", value));
        } else {
            //numeric (int, long or double)
            final long longValue;
            if (value instanceof Integer) {
                longValue = (Integer)value;
            } else if (value instanceof Long) {
                longValue = (Long)value;
            } else if (value instanceof Double) {
                longValue = Double.doubleToRawLongBits((Double)value);
            } else {
                throw new IllegalStateException("Unexpected constant: " + value);
            }
            jsb.addAnnotation(NATIVE_NUM_CONST, Map.of("value", longValue));
        }

        jsb.addGetter(name, type);
    }

    @Override
    public Boolean visitStruct(StructTree structTree, JType jt) {
        //generate nested structs recursively
        structTree.nestedTypes().forEach(this::generateDecl);

        if (structTree.isAnonymous()) {
            //skip anonymous
            return false;
        }
        String nativeName = structTree.name();
        Type type = structTree.type();
        log.print(Level.FINE, () -> "Create struct: " + nativeName);

        String intf = Utils.toClassName(nativeName);
        String name = headerClassName + "." + intf;

        log.print(Level.FINE, () -> "Define class " + name + " for native type " + nativeName);

        JavaSourceBuilder jsb = new JavaSourceBuilder(global_jsb.align() + 1);
        jsb.addAnnotation(false, NATIVE_STRUCT, Map.of("value", structTree.layout().toString()));
        jsb.interfaceBegin(intf, true);
        // fields
        structTree.fields().forEach(fieldTree -> addField(jsb, fieldTree, type));
        jsb.interfaceEnd();
        addClassIfNeeded(headerClassName + "." + intf, jsb);

        return true;
    }

    @Override
    public Boolean visitEnum(EnumTree enumTree, JType jt) {
        // define enum constants in global_cw
        enumTree.constants().forEach(constant -> addConstant(global_jsb,
                constant.location(),
                constant.name(),
                headerFile.dictionary().lookup(constant.type()),
                constant.enumConstant().get()));

        if (enumTree.name().isEmpty()) {
            // We are done with anonymous enum
            return true;
        }

        // generate annotation class for named enum
        createAnnotationCls(enumTree);
        return true;
    }

    private void createAnnotationCls(Tree tree) {
        String nativeName = tree.name();
        log.print(Level.FINE, () -> "Create annotation for: " + nativeName);

        String intf = Utils.toClassName(nativeName);
        String name = headerClassName + "." + intf;
        JavaSourceBuilder jsb = new JavaSourceBuilder(global_jsb.align() + 1);
        log.print(Level.FINE, () -> "Define class " + name + " for native type " + nativeName);

        addNativeLocation(false, jsb, tree.location());
        jsb.addAnnotation(false, "Target", Map.of("value", ElementType.TYPE_USE));
        jsb.addAnnotation(false, "Retention", Map.of("value", RetentionPolicy.RUNTIME));
        jsb.interfaceBegin(intf, true, true);
        jsb.interfaceEnd();

        addClassIfNeeded(headerClassName + "." + intf, jsb);
    }

    private void createFunctionalInterface(JType.FunctionalInterfaceType fnif) {
        JType.Function fn = fnif.getFunction();
        String intf;
        String nativeName;
        String nDesc = fnif.getFunction().getNativeDescriptor();
        intf = fnif.getSimpleName();
        nativeName = "anonymous function";
        log.print(Level.FINE, () -> "Create FunctionalInterface " + intf);

        final String name = headerClassName + "." + intf;

        JavaSourceBuilder jsb = new JavaSourceBuilder(global_jsb.align() + 1);
        log.print(Level.FINE, () -> "Define class " + name + " for native type " + nativeName + nDesc);

        jsb.addAnnotation(false, "FunctionalInterface", Map.of());
        jsb.addAnnotation(false, NATIVE_CALLBACK, Map.of("value", nDesc));
        jsb.interfaceBegin(intf, true);
        jsb.addMethod("fn", fn);
        jsb.interfaceEnd();

        // add the method
        addClassIfNeeded(headerClassName + "." + intf, jsb);
    }

    @Override
    public Boolean visitTypedef(TypedefTree typedefTree, JType jt) {
        createAnnotationCls(typedefTree);
        return true;
    }

    @Override
    public Boolean visitTree(Tree tree, JType jt) {
        log.print(Level.WARNING, () -> "Unsupported declaration tree:");
        log.print(Level.WARNING, () -> tree.toString());
        return true;
    }

    @Override
    public Boolean visitFunction(FunctionTree funcTree, JType jt) {
        assert (jt instanceof JType.Function);
        JType.Function fn = (JType.Function)jt;
        log.print(Level.FINE, () -> "Add method: " + fn.getSignature(false));

        addNativeLocation(global_jsb, funcTree);
        Type type = funcTree.type();
        final String descStr = Utils.getFunction(type).toString();
        global_jsb.addAnnotation(NATIVE_FUNCTION, Map.of("value", descStr));
        global_jsb.addMethod(funcTree, fn);

        return true;
    }

    private void generateDecl(Tree tree) {
        try {
            log.print(Level.FINE, () -> "Process tree " + tree.name());
            tree.accept(this, tree.isPreprocessing() ? null : headerFile.dictionary().lookup(tree.type()));
        } catch (Exception ex) {
            handleException(ex);
            log.print(Level.WARNING, () -> "Tree causing above exception is: " + tree.name());
            log.print(Level.WARNING, () -> tree.toString());
        }
    }

    @Override
    public Boolean visitMacro(MacroTree macroTree, JType jt) {
        if (!macroTree.isConstant()) {
            log.print(Level.FINE, () -> "Skipping unrecognized object-like macro " + macroTree.name());
            return false;
        }
        String name = macroTree.name();
        MacroParser.Macro macro = macroTree.macro().get();
        log.print(Level.FINE, () -> "Adding macro " + name);

        addConstant(global_jsb, macroTree.location(), name, macro.type(), macro.value());

        return true;
    }
}
