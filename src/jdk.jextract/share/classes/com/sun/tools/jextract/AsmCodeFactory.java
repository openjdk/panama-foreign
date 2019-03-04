/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
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
import java.nio.file.Path;
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
import jdk.internal.org.objectweb.asm.AnnotationVisitor;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import com.sun.tools.jextract.tree.EnumTree;
import com.sun.tools.jextract.tree.FieldTree;
import com.sun.tools.jextract.tree.FunctionTree;
import com.sun.tools.jextract.tree.MacroTree;
import com.sun.tools.jextract.tree.SimpleTreeVisitor;
import com.sun.tools.jextract.tree.StructTree;
import com.sun.tools.jextract.tree.Tree;
import com.sun.tools.jextract.tree.TypedefTree;
import com.sun.tools.jextract.tree.VarTree;

import static jdk.internal.org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static jdk.internal.org.objectweb.asm.Opcodes.ACC_ANNOTATION;
import static jdk.internal.org.objectweb.asm.Opcodes.ACC_INTERFACE;
import static jdk.internal.org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static jdk.internal.org.objectweb.asm.Opcodes.ACC_STATIC;
import static jdk.internal.org.objectweb.asm.Opcodes.ACC_VARARGS;
import static jdk.internal.org.objectweb.asm.Opcodes.ARETURN;
import static jdk.internal.org.objectweb.asm.Opcodes.DRETURN;
import static jdk.internal.org.objectweb.asm.Opcodes.FRETURN;
import static jdk.internal.org.objectweb.asm.Opcodes.I2B;
import static jdk.internal.org.objectweb.asm.Opcodes.I2C;
import static jdk.internal.org.objectweb.asm.Opcodes.I2S;
import static jdk.internal.org.objectweb.asm.Opcodes.IRETURN;
import static jdk.internal.org.objectweb.asm.Opcodes.LRETURN;
import static jdk.internal.org.objectweb.asm.Opcodes.V1_8;

/**
 * Scan a header file and generate classes for entities defined in that header
 * file. Tree visitor visit methods return true/false depending on whether a
 * particular Tree is processed or skipped.
 */
class AsmCodeFactory extends SimpleTreeVisitor<Boolean, JType> {
    private static final String ANNOTATION_PKG_PREFIX = "Ljava/foreign/annotations/";
    private static final String NATIVE_CALLBACK = ANNOTATION_PKG_PREFIX + "NativeCallback;";
    private static final String NATIVE_HEADER = ANNOTATION_PKG_PREFIX + "NativeHeader;";
    private static final String NATIVE_LOCATION = ANNOTATION_PKG_PREFIX + "NativeLocation;";
    private static final String NATIVE_STRUCT = ANNOTATION_PKG_PREFIX + "NativeStruct;";
    private static final String NATIVE_FUNCTION = ANNOTATION_PKG_PREFIX + "NativeFunction;";
    private static final String NATIVE_GETTER = ANNOTATION_PKG_PREFIX + "NativeGetter;";
    private static final String NATIVE_SETTER = ANNOTATION_PKG_PREFIX + "NativeSetter;";
    private static final String NATIVE_ADDRESSOF = ANNOTATION_PKG_PREFIX + "NativeAddressof;";
    private static final String NATIVE_NUM_CONST = ANNOTATION_PKG_PREFIX + "NativeNumericConstant;";
    private static final String NATIVE_STR_CONT = ANNOTATION_PKG_PREFIX + "NativeStringConstant;";

    private final ClassWriter global_cw;
    private final Set<Layout> global_layouts = new LinkedHashSet<>();
    protected final String headerClassName;
    protected final HeaderFile headerFile;
    protected final Map<String, byte[]> types;
    protected final List<String> libraryNames;
    protected final List<String> libraryPaths;
    protected final boolean noNativeLocations;

    protected final Log log;

    AsmCodeFactory(Context ctx, HeaderFile header) {
        this.log = ctx.log;
        log.print(Level.INFO, () -> "Instantiate AsmCodeFactory for " + header.path);
        this.headerFile = header;
        this.headerClassName = Utils.toInternalName(headerFile.pkgName, headerFile.clsName);
        this.global_cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        this.types = new HashMap<>();
        this.libraryNames = ctx.options.libraryNames;
        this.libraryPaths = ctx.options.recordLibraryPath? ctx.options.libraryPaths : null;
        this.noNativeLocations = ctx.options.noNativeLocations;
        global_cw.visit(V1_8, ACC_PUBLIC | ACC_ABSTRACT | ACC_INTERFACE,
                headerClassName,
                null, "java/lang/Object", null);
    }

    public Map<String, byte[]> generateNativeHeader(List<Tree> decls) {
        //generate all decls
        decls.forEach(this::generateDecl);
        //generate functional interfaces
        headerFile.dictionary().functionalInterfaces()
                .forEach(fi -> createFunctionalInterface((JType.FunctionalInterfaceType)fi));

        //generate header intf
        AnnotationVisitor av = global_cw.visitAnnotation(NATIVE_HEADER, true);
        av.visit("path", headerFile.path.toAbsolutePath().toString());
        if (!libraryNames.isEmpty()) {
            AnnotationVisitor libNames = av.visitArray("libraries");
            for (String name : libraryNames) {
                libNames.visit(null, name);
            }
            libNames.visitEnd();
            if (libraryPaths != null && !libraryPaths.isEmpty()) {
                AnnotationVisitor libPaths = av.visitArray("libraryPaths");
                for (String path : libraryPaths) {
                    libPaths.visit(null, path);
                }
                libPaths.visitEnd();
            }
        }

        AnnotationVisitor resolutionContext = av.visitArray("resolutionContext");
        headerFile.dictionary().resolutionRoots()
                .forEach(jt -> resolutionContext.visit(null,
                        jdk.internal.org.objectweb.asm.Type.getObjectType(jt.clsName)));
        resolutionContext.visitEnd();
        AnnotationVisitor globals = av.visitArray("globals");
        global_layouts.stream().map(Layout::toString).forEach(s -> globals.visit(null, s));
        globals.visitEnd();
        av.visitEnd();
        global_cw.visitEnd();
        addClassIfNeeded(headerClassName, global_cw.toByteArray());
        return Collections.unmodifiableMap(types);
    }

    private void handleException(Exception ex) {
        log.printError("cannot.write.class.file", headerFile.pkgName + "." + headerFile.clsName, ex);
        log.printStackTrace(ex);
    }

    private void annotateNativeLocation(ClassVisitor cw, Tree tree) {
        if (! noNativeLocations) {
            AnnotationVisitor av = cw.visitAnnotation(NATIVE_LOCATION, true);
            SourceLocation src = tree.location();
            SourceLocation.Location loc = src.getFileLocation();
            Path p = loc.path();
            av.visit("file", p == null ? "builtin" : p.toAbsolutePath().toString());
            av.visit("line", loc.line());
            av.visit("column", loc.column());
            av.visitEnd();
        }
    }

    private void addClassIfNeeded(String clsName, byte[] bytes) {
        if (null != types.put(clsName, bytes)) {
            log.printWarning("warn.class.overwritten", clsName);
        }
    }

    private static boolean isBitField(Tree tree) {
        return tree instanceof FieldTree && ((FieldTree)tree).isBitField();
    }

    /**
     *
     * @param cw ClassWriter for the struct
     * @param tree The Tree
     * @param parentType The struct type
     */
    private boolean addField(ClassVisitor cw, Tree tree, Type parentType) {
        String fieldName = tree.name();
        assert !fieldName.isEmpty();
        Type type = tree.type();
        JType jt = headerFile.dictionary().lookup(type);
        assert (jt != null);
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_ABSTRACT, fieldName + "$get",
                "()" + jt.getDescriptor(), "()" + jt.getSignature(false), null);
        jt.visitInner(cw);

        if (! noNativeLocations) {
            AnnotationVisitor av = mv.visitAnnotation(NATIVE_LOCATION, true);
            SourceLocation src = tree.location();
            SourceLocation.Location loc = src.getFileLocation();
            Path p = loc.path();
            av.visit("file", p == null ? "builtin" : p.toAbsolutePath().toString());
            av.visit("line", loc.line());
            av.visit("column", loc.column());
            av.visitEnd();
        }

        AnnotationVisitor av = mv.visitAnnotation(NATIVE_GETTER, true);
        av.visit("value", fieldName);
        av.visitEnd();

        mv.visitEnd();
        mv = cw.visitMethod(ACC_PUBLIC | ACC_ABSTRACT, fieldName + "$set",
                "(" + jt.getDescriptor() + ")V",
                "(" + jt.getSignature(true) + ")V", null);
        jt.visitInner(cw);
        av = mv.visitAnnotation(NATIVE_SETTER, true);
        av.visit("value", fieldName);
        av.visitEnd();
        mv.visitEnd();

        if (tree instanceof VarTree || !isBitField(tree)) {
            JType ptrType = JType.GenericType.ofPointer(jt);
            mv = cw.visitMethod(ACC_PUBLIC | ACC_ABSTRACT, fieldName + "$ptr",
                    "()" + ptrType.getDescriptor(), "()" + ptrType.getSignature(false), null);
            ptrType.visitInner(cw);
            av = mv.visitAnnotation(NATIVE_ADDRESSOF, true);
            av.visit("value", fieldName);
            av.visitEnd();
            mv.visitEnd();
        }

        return true;
    }

    @Override
    public Boolean visitVar(VarTree varTree, JType jt) {
        global_layouts.add(varTree.layout().withAnnotation(Layout.NAME, varTree.name()));
        return addField(global_cw, varTree, null);
    }

    private void addConstant(ClassWriter cw, SourceLocation src, String name, JType type, Object value) {
        String desc = "()" + type.getDescriptor();
        String sig = "()" + type.getSignature(false);
        MethodVisitor mv = global_cw.visitMethod(ACC_ABSTRACT | ACC_PUBLIC, name, desc, sig, null);
        type.visitInner(cw);

        if (! noNativeLocations) {
            AnnotationVisitor av = mv.visitAnnotation(NATIVE_LOCATION, true);
            SourceLocation.Location loc = src.getFileLocation();
            Path p = loc.path();
            av.visit("file", p == null ? "builtin" : p.toAbsolutePath().toString());
            av.visit("line", loc.line());
            av.visit("column", loc.column());
            av.visitEnd();
        }

        if (value instanceof String) {
            AnnotationVisitor av = mv.visitAnnotation(NATIVE_STR_CONT, true);
            av.visit("value", value);
            av.visitEnd();
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
            AnnotationVisitor av = mv.visitAnnotation(NATIVE_NUM_CONST, true);
            av.visit("value", longValue);
            av.visitEnd();
        }
        mv.visitEnd();
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
        String name = headerClassName + "$" + intf;

        log.print(Level.FINE, () -> "Define class " + name + " for native type " + nativeName);
        /* FIXME: Member interface is implicit static, also ASM.CheckClassAdapter is not
         * taking static as a valid flag, so comment this out during development.
         */
        global_cw.visitInnerClass(name, headerClassName, intf, ACC_PUBLIC | ACC_STATIC | ACC_ABSTRACT | ACC_INTERFACE);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC /*| ACC_STATIC*/ | ACC_INTERFACE | ACC_ABSTRACT,
                name, "Ljava/lang/Object;Ljava/foreign/memory/Struct<L" + name + ";>;",
                "java/lang/Object", new String[] {"java/foreign/memory/Struct"});
        annotateNativeLocation(cw, structTree);

        AnnotationVisitor av = cw.visitAnnotation(NATIVE_STRUCT, true);
        Layout structLayout = structTree.layout();
        av.visit("value", structLayout.toString());
        av.visitEnd();
        cw.visitInnerClass(name, headerClassName, intf, ACC_PUBLIC | ACC_STATIC | ACC_ABSTRACT | ACC_INTERFACE);

        // fields
        structTree.fields().forEach(fieldTree -> addField(cw, fieldTree, type));
        // Write class
        cw.visitEnd();
        addClassIfNeeded(headerClassName + "$" + intf, cw.toByteArray());
        return true;
    }

    @Override
    public Boolean visitEnum(EnumTree enumTree, JType jt) {
        // define enum constants in global_cw
        enumTree.constants().forEach(constant -> addConstant(global_cw,
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
        String name = headerClassName + "$" + intf;

        log.print(Level.FINE, () -> "Define class " + name + " for native type " + nativeName);
        global_cw.visitInnerClass(name, headerClassName, intf,
                ACC_PUBLIC | ACC_STATIC | ACC_ABSTRACT | ACC_INTERFACE | ACC_ANNOTATION);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        String[] superAnno = { "java/lang/annotation/Annotation" };
        cw.visit(V1_8, ACC_PUBLIC | ACC_ABSTRACT | ACC_INTERFACE | ACC_ANNOTATION,
                name, null, "java/lang/Object", superAnno);
        annotateNativeLocation(cw, tree);
        AnnotationVisitor av = cw.visitAnnotation("Ljava/lang/annotation/Target;", true);
        av.visitEnum("value", "Ljava/lang/annotation/ElementType;", "TYPE_USE");
        av.visitEnd();
        av = cw.visitAnnotation("Ljava/lang/annotation/Retention;", true);
        av.visitEnum("value", "Ljava/lang/annotation/RetentionPolicy;", "RUNTIME");
        av.visitEnd();
        cw.visitInnerClass(name, headerClassName, intf,
                ACC_PUBLIC | ACC_STATIC | ACC_ABSTRACT | ACC_INTERFACE | ACC_ANNOTATION);
        // Write class
        cw.visitEnd();
        addClassIfNeeded(headerClassName + "$" + intf, cw.toByteArray());
    }

    private void createFunctionalInterface(JType.FunctionalInterfaceType fnif) {
        JType.Function fn = fnif.getFunction();
        String intf;
        String nativeName;
        String nDesc = fnif.getFunction().getNativeDescriptor();
        intf = fnif.getSimpleName();
        nativeName = "anonymous function";
        log.print(Level.FINE, () -> "Create FunctionalInterface " + intf);

        final String name = headerClassName + "$" + intf;

        log.print(Level.FINE, () -> "Define class " + name + " for native type " + nativeName + nDesc);
        global_cw.visitInnerClass(name, headerClassName, intf, ACC_PUBLIC | ACC_STATIC | ACC_ABSTRACT | ACC_INTERFACE);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC | ACC_ABSTRACT | ACC_INTERFACE,
                name, "Ljava/lang/Object;",
                "java/lang/Object", new String[0]);
        AnnotationVisitor av = cw.visitAnnotation(
                "Ljava/lang/FunctionalInterface;", true);
        av.visitEnd();
        av = cw.visitAnnotation(NATIVE_CALLBACK, true);
        av.visit("value", nDesc);
        av.visitEnd();
        cw.visitInnerClass(name, headerClassName, intf, ACC_PUBLIC | ACC_STATIC | ACC_ABSTRACT | ACC_INTERFACE);

        // add the method

        int flags = ACC_PUBLIC | ACC_ABSTRACT;
        if (fn.isVarArgs) {
            flags |= ACC_VARARGS;
        }
        MethodVisitor mv = cw.visitMethod(flags, "fn",
                fn.getDescriptor(), fn.getSignature(false), null);
        fn.visitInner(cw);
        mv.visitEnd();
        // Write class
        cw.visitEnd();
        addClassIfNeeded(headerClassName + "$" + intf, cw.toByteArray());
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
        int flags = ACC_PUBLIC | ACC_ABSTRACT;
        if (fn.isVarArgs) {
            flags |= ACC_VARARGS;
        }
        MethodVisitor mv = global_cw.visitMethod(flags,
                funcTree.name(), fn.getDescriptor(), fn.getSignature(false), null);
        jt.visitInner(global_cw);
        final int arg_cnt = funcTree.numParams();
        for (int i = 0; i < arg_cnt; i++) {
            String name = funcTree.paramName(i);
            final int tmp = i;
            log.print(Level.FINER, () -> "  arg " + tmp + ": " + name);
            mv.visitParameter(name, 0);
        }

        if (! noNativeLocations) {
            AnnotationVisitor av = mv.visitAnnotation(NATIVE_LOCATION, true);
            SourceLocation src = funcTree.location();
            SourceLocation.Location loc = src.getFileLocation();
            Path p = loc.path();
            av.visit("file", p == null ? "builtin" : p.toAbsolutePath().toString());
            av.visit("line", loc.line());
            av.visit("column", loc.column());
            av.visitEnd();
        }

        Type type = funcTree.type();
        final String descStr = Utils.getFunction(type).toString();

        AnnotationVisitor av = mv.visitAnnotation(NATIVE_FUNCTION, true);
        av.visit("value", descStr);
        av.visitEnd();

        mv.visitEnd();
        return true;
    }

    private AsmCodeFactory generateDecl(Tree tree) {
        try {
            log.print(Level.FINE, () -> "Process tree " + tree.name());
            tree.accept(this, tree.isPreprocessing() ? null : headerFile.dictionary().lookup(tree.type()));
        } catch (Exception ex) {
            handleException(ex);
            log.print(Level.WARNING, () -> "Tree causing above exception is: " + tree.name());
            log.print(Level.WARNING, () -> tree.toString());
        }
        return this;
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

        addConstant(global_cw, macroTree.location(), name, macro.type(), macro.value());

        return true;
    }
}
