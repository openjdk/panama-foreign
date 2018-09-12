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

import java.io.IOException;
import java.foreign.layout.Layout;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;
import jdk.internal.clang.SourceLocation;
import jdk.internal.clang.Type;
import jdk.internal.clang.TypeKind;
import jdk.internal.foreign.Util;
import jdk.internal.org.objectweb.asm.AnnotationVisitor;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.TypeReference;
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
import static jdk.internal.org.objectweb.asm.Opcodes.ACC_FINAL;
import static jdk.internal.org.objectweb.asm.Opcodes.ACC_INTERFACE;
import static jdk.internal.org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static jdk.internal.org.objectweb.asm.Opcodes.ACC_STATIC;
import static jdk.internal.org.objectweb.asm.Opcodes.ACC_VARARGS;
import static jdk.internal.org.objectweb.asm.Opcodes.ARETURN;
import static jdk.internal.org.objectweb.asm.Opcodes.DRETURN;
import static jdk.internal.org.objectweb.asm.Opcodes.FRETURN;
import static jdk.internal.org.objectweb.asm.Opcodes.I2C;
import static jdk.internal.org.objectweb.asm.Opcodes.IRETURN;
import static jdk.internal.org.objectweb.asm.Opcodes.LRETURN;
import static jdk.internal.org.objectweb.asm.Opcodes.V1_8;

/**
 * Scan a header file and generate classes for entities defined in that header
 * file.
 */
final class AsmCodeFactory extends SimpleTreeVisitor<Void, JType> {
    private static final String ANNOTATION_PKG_PREFIX = "Ljava/foreign/annotations/";
    private static final String NATIVE_CALLBACK = ANNOTATION_PKG_PREFIX + "NativeCallback;";
    private static final String NATIVE_HEADER = ANNOTATION_PKG_PREFIX + "NativeHeader;";
    private static final String NATIVE_LOCATION = ANNOTATION_PKG_PREFIX + "NativeLocation;";
    private static final String NATIVE_STRUCT = ANNOTATION_PKG_PREFIX + "NativeStruct;";

    private final Context ctx;
    private final ClassWriter global_cw;
    // to avoid duplicate generation of methods, field accessors, macros
    private final Set<String> global_methods = new HashSet<>();
    private final Set<String> global_fields = new HashSet<>();
    private final Set<String> global_macros = new HashSet<>();
    private final String internal_name;
    private final HeaderFile owner;
    private final Map<String, byte[]> types;
    private final Logger logger = Logger.getLogger(getClass().getPackage().getName());
    private final List<String> headerDeclarations = new ArrayList<>();
    private transient boolean built = false;

    AsmCodeFactory(Context ctx, HeaderFile header) {
        this.ctx = ctx;
        logger.info(() -> "Instantiate AsmCodeFactory for " + header.path);
        this.owner = header;
        this.internal_name = Utils.toInternalName(owner.pkgName, owner.clsName);
        this.global_cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        this.types = new HashMap<>();
        global_cw.visit(V1_8, ACC_PUBLIC | ACC_ABSTRACT | ACC_INTERFACE,
                internal_name,
                null, "java/lang/Object", null);
    }

    private void generateNativeHeader() {
        AnnotationVisitor av = global_cw.visitAnnotation(NATIVE_HEADER, true);
        av.visit("path", owner.path.toAbsolutePath().toString());
        if (owner.libraries != null && !owner.libraries.isEmpty()) {
            AnnotationVisitor libNames = av.visitArray("libraries");
            for (String name : owner.libraries) {
                libNames.visit(null, name);
            }
            libNames.visitEnd();
            if (owner.libraryPaths != null && !owner.libraryPaths.isEmpty()) {
                AnnotationVisitor libPaths = av.visitArray("libraryPaths");
                for (String path : owner.libraryPaths) {
                    libPaths.visit(null, path);
                }
                libPaths.visitEnd();
            }
        }
        av.visit("declarations", String.join(" ", headerDeclarations));
        av.visitEnd();
    }

    private void handleException(Exception ex) {
        ctx.err.println(Main.format("cannot.write.class.file", owner.pkgName + "." + owner.clsName, ex));
        if (Main.DEBUG) {
            ex.printStackTrace(ctx.err);
        }
    }

    private void annotateNativeLocation(ClassVisitor cw, Tree tree) {
        AnnotationVisitor av = cw.visitAnnotation(NATIVE_LOCATION, true);
        SourceLocation src = tree.location();
        SourceLocation.Location loc = src.getFileLocation();
        Path p = loc.path();
        av.visit("file", p == null ? "builtin" : p.toAbsolutePath().toString());
        av.visit("line", loc.line());
        av.visit("column", loc.column());
        av.visit("USR", tree.USR());
        av.visitEnd();
    }

    private void writeClassFile(final ClassWriter cw, String clsName)
            throws IOException {
        cw.visitEnd();
        byte[] bytecodes = cw.toByteArray();
        if (null != types.put(clsName, bytecodes)) {
            logger.warning("Class " + clsName + " definition is overwritten");
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
    private void addField(ClassVisitor cw, Tree tree, Type parentType) {
        String fieldName = tree.name();
        if (fieldName.isEmpty()) {
            //skip anon fields
            return;
        }
        Type type = tree.type();
        JType jt = owner.globalLookup(type);
        assert (jt != null);
        if (cw == global_cw) {
            String uniqueName = fieldName + "." + jt.getDescriptor();
            if (! global_fields.add(uniqueName)) {
                return; // added already
            }
        }
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_ABSTRACT, fieldName + "$get",
                "()" + jt.getDescriptor(), "()" + jt.getSignature(), null);

        AnnotationVisitor av = mv.visitAnnotation(NATIVE_LOCATION, true);
        SourceLocation src = tree.location();
        SourceLocation.Location loc = src.getFileLocation();
        Path p = loc.path();
        av.visit("file", p == null ? "builtin" : p.toAbsolutePath().toString());
        av.visit("line", loc.line());
        av.visit("column", loc.column());
        av.visit("USR", tree.USR());
        av.visitEnd();

        mv.visitEnd();
        cw.visitMethod(ACC_PUBLIC | ACC_ABSTRACT, fieldName + "$set",
                "(" + jt.getDescriptor() + ")V",
                "(" + JType.getPointerVoidAsWildcard(jt) + ")V", null);
        if (tree instanceof VarTree || !isBitField(tree)) {
            JType ptrType = new PointerType(jt);
            cw.visitMethod(ACC_PUBLIC | ACC_ABSTRACT, fieldName + "$ptr",
                    "()" + ptrType.getDescriptor(), "()" + ptrType.getSignature(), null);
        }
    }

    @Override
    public Void visitVar(VarTree varTree, JType jt) {
        addField(global_cw, varTree, null);
        Layout layout = varTree.layout();
        String descStr = decorateAsAccessor(varTree, layout).toString();
        addHeaderDecl(varTree.name(), descStr);
        return null;
    }

    private void addHeaderDecl(String symbol, String desc) {
        headerDeclarations.add(String.format("%s=%s", symbol, desc));
    }

    private void addConstant(ClassVisitor cw, FieldTree fieldTree) {
        assert (fieldTree.isEnumConstant());
        String name = fieldTree.name();
        String desc = owner.globalLookup(fieldTree.type()).getDescriptor();
        Object value = null;
        switch (desc) {
            case "J":
                value = fieldTree.enumConstant().get();
                break;
            case "I":
                value = fieldTree.enumConstant().get().intValue();
                break;
        }
        cw.visitField(ACC_PUBLIC | ACC_FINAL | ACC_STATIC, name, desc, null, value);
    }

    @Override
    public Void visitStruct(StructTree structTree, JType jt) {
        String nativeName = structTree.identifier();
        Type type = structTree.type();
        logger.fine(() -> "Create struct: " + nativeName);

        String intf = Utils.toClassName(nativeName);
        String name = internal_name + "$" + intf;

        logger.fine(() -> "Define class " + name + " for native type " + nativeName);
        /* FIXME: Member interface is implicit static, also ASM.CheckClassAdapter is not
         * taking static as a valid flag, so comment this out during development.
         */
        global_cw.visitInnerClass(name, internal_name, intf, ACC_PUBLIC | ACC_STATIC | ACC_ABSTRACT | ACC_INTERFACE);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC /*| ACC_STATIC*/ | ACC_INTERFACE | ACC_ABSTRACT,
                name, "Ljava/lang/Object;Ljava/foreign/memory/Struct<L" + name + ";>;",
                "java/lang/Object", new String[] {"java/foreign/memory/Struct"});
        annotateNativeLocation(cw, structTree);

        AnnotationVisitor av = cw.visitAnnotation(NATIVE_STRUCT, true);
        Layout structLayout = structTree.layout(this::decorateAsAccessor);
        av.visit("value", structLayout.toString());
        av.visitEnd();
        cw.visitInnerClass(name, internal_name, intf, ACC_PUBLIC | ACC_STATIC | ACC_ABSTRACT | ACC_INTERFACE);

        // fields
        structTree.fields().forEach(fieldTree -> addField(cw, fieldTree, type));
        // Write class
        try {
            writeClassFile(cw, owner.clsName + "$" + intf);
        } catch (IOException ex) {
            handleException(ex);
        }
        return null;
    }

    Layout addGetterSetterName(Layout layout, String accessorName) {
        return layout
            .withAnnotation("get", accessorName + "$get")
            .withAnnotation("set", accessorName + "$set");
    }

    Layout decorateAsAccessor(VarTree varTree, Layout layout) {
        return addGetterSetterName(layout, varTree.name()).
            withAnnotation("ptr", varTree.name() + "$ptr");
    }

    Layout decorateAsAccessor(FieldTree fieldTree, Layout layout) {
        layout = addGetterSetterName(layout, fieldTree.name());
        if (!fieldTree.isBitField()) {
            //no pointer accessors for bitfield!
            layout = layout.withAnnotation("ptr", fieldTree.name() + "$ptr");
        }
        return layout;
    }

    @Override
    public Void visitEnum(EnumTree enumTree, JType jt) {
        // define enum constants in global_cw
        enumTree.constants().forEach(constant -> addConstant(global_cw, constant));

        if (enumTree.name().isEmpty()) {
            // We are done with anonymous enum
            return null;
        }

        // generate annotation class for named enum
        createAnnotationCls(enumTree);
        return null;
    }

    private void createAnnotationCls(Tree tree) {
        String nativeName = tree.identifier();
        logger.fine(() -> "Create annotation for: " + nativeName);

        String intf = Utils.toClassName(nativeName);
        String name = internal_name + "$" + intf;

        logger.fine(() -> "Define class " + name + " for native type " + nativeName);
        global_cw.visitInnerClass(name, internal_name, intf,
                ACC_PUBLIC | ACC_STATIC | ACC_ABSTRACT | ACC_INTERFACE | ACC_ANNOTATION);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        String[] superAnno = { "java/lang/annotation/Annotation" };
        cw.visit(V1_8, ACC_PUBLIC | ACC_ABSTRACT | ACC_INTERFACE | ACC_ANNOTATION,
                name, null, "java/lang/Object", superAnno);
        annotateNativeLocation(cw, tree);
        Type type = tree.type().canonicalType();
        AnnotationVisitor av = cw.visitAnnotation("Ljava/lang/annotation/Target;", true);
        av.visitEnum("value", "Ljava/lang/annotation/ElementType;", "TYPE_USE");
        av.visitEnd();
        av = cw.visitAnnotation("Ljava/lang/annotation/Retention;", true);
        av.visitEnum("value", "Ljava/lang/annotation/RetentionPolicy;", "RUNTIME");
        av.visitEnd();
        cw.visitInnerClass(name, internal_name, intf,
                ACC_PUBLIC | ACC_STATIC | ACC_ABSTRACT | ACC_INTERFACE | ACC_ANNOTATION);
        // Write class
        try {
            writeClassFile(cw, owner.clsName + "$" + intf);
        } catch (IOException ex) {
            handleException(ex);
        }
    }

    private void createFunctionalInterface(Tree tree, JType.FnIf fnif) {
        JType.Function fn = fnif.getFunction();
        String intf;
        String nativeName;
        String nDesc = fnif.getFunction().getNativeDescriptor();
        if (tree == null) {
            intf = ((JType.InnerType) fnif.type).getName();
            nativeName = "anonymous function";
        } else {
            nativeName = tree.identifier();
            intf = Utils.toClassName(nativeName);
        }
        logger.fine(() -> "Create FunctionalInterface " + intf);

        final String name = internal_name + "$" + intf;

        logger.fine(() -> "Define class " + name + " for native type " + nativeName + nDesc);
        global_cw.visitInnerClass(name, internal_name, intf, ACC_PUBLIC | ACC_STATIC | ACC_ABSTRACT | ACC_INTERFACE);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC | ACC_ABSTRACT | ACC_INTERFACE,
                name, "Ljava/lang/Object;Ljava/foreign/memory/Callback<L" + name + ";>;",
                "java/lang/Object", new String[] {"java/foreign/memory/Callback"});
        if (tree != null) {
            annotateNativeLocation(cw, tree);
        }
        AnnotationVisitor av = cw.visitAnnotation(
                "Ljava/lang/FunctionalInterface;", true);
        av.visitEnd();
        av = cw.visitAnnotation(NATIVE_CALLBACK, true);
        av.visit("value", nDesc);
        av.visitEnd();
        cw.visitInnerClass(name, internal_name, intf, ACC_PUBLIC | ACC_STATIC | ACC_ABSTRACT | ACC_INTERFACE);

        // add the method

        int flags = ACC_PUBLIC | ACC_ABSTRACT;
        if (fn.isVarArgs) {
            flags |= ACC_VARARGS;
        }
        MethodVisitor mv = cw.visitMethod(flags, "fn",
                fn.getDescriptor(), fn.getSignature(), null);
        mv.visitEnd();
        // Write class
        try {
            writeClassFile(cw, owner.clsName + "$" + intf);
        } catch (IOException ex) {
            handleException(ex);
        }
    }

    @Override
    public Void visitTypedef(TypedefTree typedefTree, JType jt) {
        Type t = typedefTree.type();
        if (t.canonicalType().kind() == TypeKind.Enum &&
            t.spelling().equals(t.canonicalType().getDeclarationCursor().spelling())) {
            logger.fine("Skip redundant typedef " + t.spelling());
            return null;
        }

        // anonymous typedef struct {} xxx will not get TypeAlias
        if (jt instanceof TypeAlias) {
            TypeAlias alias = (TypeAlias) jt;
            if (alias.getAnnotationDescriptor() != null) {
                createAnnotationCls(typedefTree);
            } else {
                JType real = alias.canonicalType();
                if (real instanceof JType.FnIf) {
                    createFunctionalInterface(typedefTree, (JType.FnIf) real);
                }
                // Otherwise, type alias is a same named stuct
            }
        }
        return null;
    }

    @Override
    public Void visitTree(Tree tree, JType jt) {
        logger.warning(() -> "Unsupported declaration tree:");
        logger.warning(() -> tree.toString());
        return null;
    }

    @Override
    public Void visitFunction(FunctionTree funcTree, JType jt) {
        assert (jt instanceof JType.Function);
        JType.Function fn = (JType.Function)jt;
        String uniqueName = funcTree.name() + "." + fn.getDescriptor();
        if (! global_methods.add(uniqueName)) {
            return null; // added already
        }
        logger.fine(() -> "Add method: " + fn.getSignature());
        int flags = ACC_PUBLIC | ACC_ABSTRACT;
        if (fn.isVarArgs) {
            flags |= ACC_VARARGS;
        }
        MethodVisitor mv = global_cw.visitMethod(flags,
                funcTree.name(), fn.getDescriptor(), fn.getSignature(), null);
        final int arg_cnt = funcTree.numParams();
        for (int i = 0; i < arg_cnt; i++) {
            String name = funcTree.paramName(i);
            final int tmp = i;
            logger.finer(() -> "  arg " + tmp + ": " + name);
            mv.visitParameter(name, 0);
        }
        AnnotationVisitor av = mv.visitAnnotation(NATIVE_LOCATION, true);
        SourceLocation src = funcTree.location();
        SourceLocation.Location loc = src.getFileLocation();
        Path p = loc.path();
        av.visit("file", p == null ? "builtin" : p.toAbsolutePath().toString());
        av.visit("line", loc.line());
        av.visit("column", loc.column());
        av.visit("USR", funcTree.USR());
        av.visitEnd();
        Type type = funcTree.type();
        final String descStr = Utils.getFunction(type).toString();
        addHeaderDecl(funcTree.name(), descStr);

        int idx = 0;
        for (JType arg: fn.args) {
            if (arg instanceof TypeAlias) {
                TypeAlias alias = (TypeAlias) arg;
                final int tmp = idx;
                logger.finest(() -> "  arg " + tmp + " is an alias " + alias);
                if (alias.getAnnotationDescriptor() != null) {
                    mv.visitTypeAnnotation(
                            TypeReference.newFormalParameterReference(idx).getValue(),
                            null, alias.getAnnotationDescriptor(), true)
                      .visitEnd();
                }
            }
            idx++;
        }

        if (fn.returnType instanceof TypeAlias) {
            TypeAlias alias = (TypeAlias) fn.returnType;
            logger.finest(() -> "  return type is an alias " + alias);
            if (alias.getAnnotationDescriptor() != null) {
                mv.visitTypeAnnotation(
                        TypeReference.newTypeReference(TypeReference.METHOD_RETURN).getValue(),
                        null, alias.getAnnotationDescriptor(), true)
                  .visitEnd();
            }
        }
        mv.visitEnd();
        return null;
    }

    protected AsmCodeFactory addType(JType jt, Tree tree) {
        JType2 jt2 = null;
        if (jt instanceof JType2) {
            jt2 = (JType2) jt;
            jt = jt2.getDelegate();
        } else {
            logger.warning(() -> "Should have JType2 in addType");
            if (Main.DEBUG) {
                new Throwable().printStackTrace(ctx.err);
            }
        }
        if (tree == null) {
            assert (jt2 != null);
            if (jt instanceof JType.FnIf) {
                createFunctionalInterface(null, (JType.FnIf) jt);
            }
            return this;
        }
        /*
        // FIXME: what is this?
        boolean noDef = cursor.isInvalid();
        if (noDef) {
            cursor = jt2.getCursor();
        }
        */

        try {
            logger.fine(() -> "Process tree " + tree.name());
            tree.accept(this, jt);
        } catch (Exception ex) {
            handleException(ex);
            logger.warning("Tree causing above exception is: " + tree.name());
            logger.warning(() -> tree.toString());
        }
        return this;
    }

    @Override
    public Void visitMacro(MacroTree macroTree, JType jt) {
        if (!macroTree.isConstant()) {
            logger.fine(() -> "Skipping unrecognized object-like macro " + macroTree.name());
            return null;
        }
        String name = macroTree.name();
        Object value = macroTree.value().get();
        if (! global_macros.add(name)) {
            return null; // added already
        }
        logger.fine(() -> "Adding macro " + name);
        Class<?> macroType = (Class<?>) Util.unboxIfNeeded(value.getClass());

        String sig = jdk.internal.org.objectweb.asm.Type.getMethodDescriptor(jdk.internal.org.objectweb.asm.Type.getType(macroType));
        MethodVisitor mv = global_cw.visitMethod(ACC_PUBLIC, name, sig, sig, null);

        AnnotationVisitor av = mv.visitAnnotation(NATIVE_LOCATION, true);
        SourceLocation src = macroTree.location();
        SourceLocation.Location loc = src.getFileLocation();
        Path p = loc.path();
        av.visit("file", p == null ? "builtin" : p.toAbsolutePath().toString());
        av.visit("line", loc.line());
        av.visit("column", loc.column());
        av.visit("USR", macroTree.USR());
        av.visitEnd();

        mv.visitCode();

        mv.visitLdcInsn(value);
        if (macroType.equals(char.class)) {
            mv.visitInsn(I2C);
            mv.visitInsn(IRETURN);
        } else if (macroType.equals(int.class)) {
            mv.visitInsn(IRETURN);
        } else if (macroType.equals(float.class)) {
            mv.visitInsn(FRETURN);
        } else if (macroType.equals(long.class)) {
            mv.visitInsn(LRETURN);
        } else if (macroType.equals(double.class)) {
            mv.visitInsn(DRETURN);
        } else if (macroType.equals(String.class)) {
            mv.visitInsn(ARETURN);
        }
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        return null;
    }

    protected synchronized void produce() {
        if (built) {
            throw new IllegalStateException("Produce is called multiple times");
        }
        built = true;
        generateNativeHeader();
        try {
            writeClassFile(global_cw, owner.clsName);
        } catch (IOException ex) {
            handleException(ex);
        }
    }

    protected Map<String, byte[]> collect() {
        // Ensure classes are produced
        if (!built) produce();
        HashMap<String, byte[]> rv = new HashMap<>();
        // Not copying byte[] for efficiency, perhaps not a safe idea though
        if (owner.pkgName.isEmpty()) {
            types.forEach((clsName, bytecodes) -> {
                rv.put(clsName, bytecodes);
            });
        } else {
            types.forEach((clsName, bytecodes) -> {
                rv.put(owner.pkgName + "." + clsName, bytecodes);
            });
        }
        return Collections.unmodifiableMap(rv);
    }
}
