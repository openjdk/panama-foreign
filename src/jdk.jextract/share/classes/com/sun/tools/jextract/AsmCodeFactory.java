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
import jdk.internal.clang.Cursor;
import jdk.internal.clang.CursorKind;
import jdk.internal.clang.SourceLocation;
import jdk.internal.clang.Type;
import jdk.internal.foreign.Util;
import jdk.internal.org.objectweb.asm.AnnotationVisitor;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.TypeReference;

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
final class AsmCodeFactory {
    private static final String ANNOTATION_PKG_PREFIX = "Ljava/foreign/annotations/";
    private static final String NATIVE_CALLBACK = ANNOTATION_PKG_PREFIX + "NativeCallback;";
    private static final String NATIVE_HEADER = ANNOTATION_PKG_PREFIX + "NativeHeader;";
    private static final String NATIVE_LOCATION = ANNOTATION_PKG_PREFIX + "NativeLocation;";
    private static final String NATIVE_STRUCT = ANNOTATION_PKG_PREFIX + "NativeStruct;";

    private final Context ctx;
    private final ClassWriter global_cw;
    // to avoid duplicate generation of methods, field accessors
    private final Set<String> global_methods = new HashSet<>();
    private final Set<String> global_fields = new HashSet<>();
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
        generateMacros();
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

    private void annotateNativeLocation(ClassVisitor cw, Cursor dcl) {
        AnnotationVisitor av = cw.visitAnnotation(NATIVE_LOCATION, true);
        SourceLocation src = dcl.getSourceLocation();
        SourceLocation.Location loc = src.getFileLocation();
        Path p = loc.path();
        av.visit("file", p == null ? "builtin" : p.toAbsolutePath().toString());
        av.visit("line", loc.line());
        av.visit("column", loc.column());
        av.visit("USR", dcl.USR());
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

    /**
     *
     * @param cw ClassWriter for the struct
     * @param c The FieldDecl cursor
     * @param parentType The struct type
     */
    private void addField(ClassVisitor cw, Cursor c, Type parentType) {
        String fieldName = c.spelling();
        if (fieldName.isEmpty()) {
            //skip anon fields
            return;
        }
        Type t = c.type();
        JType jt = owner.globalLookup(t);
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
        SourceLocation src = c.getSourceLocation();
        SourceLocation.Location loc = src.getFileLocation();
        Path p = loc.path();
        av.visit("file", p == null ? "builtin" : p.toAbsolutePath().toString());
        av.visit("line", loc.line());
        av.visit("column", loc.column());
        av.visit("USR", c.USR());
        av.visitEnd();

        mv.visitEnd();
        cw.visitMethod(ACC_PUBLIC | ACC_ABSTRACT, fieldName + "$set",
                "(" + jt.getDescriptor() + ")V",
                "(" + JType.getPointerVoidAsWildcard(jt) + ")V", null);
        if (!c.isBitField()) {
            JType ptrType = new PointerType(jt);
            cw.visitMethod(ACC_PUBLIC | ACC_ABSTRACT, fieldName + "$ptr",
                    "()" + ptrType.getDescriptor(), "()" + ptrType.getSignature(), null);
        }
    }

    private void addVar(ClassVisitor cw, Cursor c, Type parentType) {
        addField(cw, c, parentType);
        Layout layout = Utils.getLayout(c.type());
        String descStr = decorateAsAccessor(c, layout).toString();
        addHeaderDecl(c.spelling(), descStr);
    }

    private void addHeaderDecl(String symbol, String desc) {
        headerDeclarations.add(String.format("%s=%s", symbol, desc));
    }

    private void addConstant(ClassVisitor cw, Cursor c) {
        assert (c.kind() == CursorKind.EnumConstantDecl);
        String name = c.spelling();
        String desc = owner.globalLookup(c.type()).getDescriptor();
        Object value = null;
        switch (desc) {
            case "J":
                value = c.getEnumConstantValue();
                break;
            case "I":
                value = (int) c.getEnumConstantValue();
                break;
        }
        cw.visitField(ACC_PUBLIC | ACC_FINAL | ACC_STATIC, name, desc, null, value);
    }

    private void createStruct(Cursor cursor) {
        String nativeName = Utils.getIdentifier(cursor);
        Type t = cursor.type();
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
        annotateNativeLocation(cw, cursor);

        AnnotationVisitor av = cw.visitAnnotation(NATIVE_STRUCT, true);
        Layout structLayout = Utils.getRecordLayout(t, this::decorateAsAccessor);
        av.visit("value", structLayout.toString());
        av.visitEnd();
        cw.visitInnerClass(name, internal_name, intf, ACC_PUBLIC | ACC_STATIC | ACC_ABSTRACT | ACC_INTERFACE);

        // fields
        structFields(cursor).forEach(cx -> addField(cw, cx, cursor.type()));
        // Write class
        try {
            writeClassFile(cw, owner.clsName + "$" + intf);
        } catch (IOException ex) {
            handleException(ex);
        }
    }

    Layout decorateAsAccessor(Cursor accessorCursor, Layout layout) {
        String accessorName = accessorCursor.spelling();
        layout = layout
                    .withAnnotation("get", accessorName + "$get")
                    .withAnnotation("set", accessorName + "$set");
        if (!accessorCursor.isBitField()) {
            //no pointer accessors for bitfield!
            layout = layout.withAnnotation("ptr", accessorName + "$ptr");
        }
        return layout;
    }

    // A stream of fields of a struct (or union). Note that we have to include
    // fields from nested annoymous unions and structs in the containing struct.
    private Stream<Cursor> structFields(Cursor cursor) {
        return cursor.children()
            .flatMap(c -> c.isAnonymousStruct()? structFields(c) : Stream.of(c))
            .filter(c -> c.kind() == CursorKind.FieldDecl);
    }

    private void createEnum(Cursor cursor) {
        // define enum constants in global_cw
        cursor.stream()
                .filter(cx -> cx.kind() == CursorKind.EnumConstantDecl)
                .forEachOrdered(cx -> addConstant(global_cw, cx));

        if (cursor.isAnonymousEnum()) {
            // We are done with anonymous enum
            return;
        }

        // generate annotation class for named enum
        createAnnotationCls(cursor);
    }

    private void createAnnotationCls(Cursor dcl) {
        String nativeName = Utils.getIdentifier(dcl);
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
        annotateNativeLocation(cw, dcl);
        Type t = dcl.type().canonicalType();
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

    private void createFunctionalInterface(Cursor dcl, JType.FnIf fnif) {
        JType.Function fn = fnif.getFunction();
        String intf;
        String nativeName;
        String nDesc = fnif.getFunction().getNativeDescriptor();
        if (dcl == null) {
            intf = ((JType.InnerType) fnif.type).getName();
            nativeName = "anonymous function";
        } else {
            nativeName = Utils.getIdentifier(dcl);
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
        if (dcl != null) {
            annotateNativeLocation(cw, dcl);
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

    private void defineType(Cursor dcl, TypeAlias alias) {
        if (alias.getAnnotationDescriptor() != null) {
            createAnnotationCls(dcl);
        } else {
            JType real = alias.canonicalType();
            if (real instanceof JType.FnIf) {
                createFunctionalInterface(dcl, (JType.FnIf) real);
            }
            // Otherwise, type alias is a same named stuct
        }
    }

    private void addMethod(Cursor dcl, JType.Function fn) {
        String uniqueName = dcl.spelling() + "." + fn.getDescriptor();
        if (! global_methods.add(uniqueName)) {
            return; // added already
        }
        logger.fine(() -> "Add method: " + fn.getSignature());
        int flags = ACC_PUBLIC | ACC_ABSTRACT;
        if (fn.isVarArgs) {
            flags |= ACC_VARARGS;
        }
        MethodVisitor mv = global_cw.visitMethod(flags,
                dcl.spelling(), fn.getDescriptor(), fn.getSignature(), null);
        final int arg_cnt = dcl.numberOfArgs();
        for (int i = 0; i < arg_cnt; i++) {
            String name = dcl.getArgument(i).spelling();
            final int tmp = i;
            logger.finer(() -> "  arg " + tmp + ": " + name);
            mv.visitParameter(name, 0);
        }
        AnnotationVisitor av = mv.visitAnnotation(NATIVE_LOCATION, true);
        SourceLocation src = dcl.getSourceLocation();
        SourceLocation.Location loc = src.getFileLocation();
        Path p = loc.path();
        av.visit("file", p == null ? "builtin" : p.toAbsolutePath().toString());
        av.visit("line", loc.line());
        av.visit("column", loc.column());
        av.visit("USR", dcl.USR());
        av.visitEnd();
        Type t = dcl.type();
        final String descStr = Utils.getFunction(t).toString();
        addHeaderDecl(dcl.spelling(), descStr);

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
    }

    protected AsmCodeFactory addType(JType jt, Cursor cursor) {
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
        if (cursor == null) {
            assert (jt2 != null);
            if (jt instanceof JType.FnIf) {
                createFunctionalInterface(null, (JType.FnIf) jt);
            }
            return this;
        }

        try {
            logger.fine(() -> "Process cursor " + cursor.spelling());
            switch (cursor.kind()) {
                case StructDecl:
                case UnionDecl:
                    if (!cursor.getDefinition().isInvalid()) {
                        createStruct(cursor);
                    } else {
                        logger.fine(() -> "Skipping undeclared struct or union:");
                        logger.fine(() -> Printer.Stringifier(p -> p.dumpCursor(cursor, true)));
                    }
                    break;
                case FunctionDecl:
                    assert (jt instanceof JType.Function);
                    addMethod(cursor, (JType.Function) jt);
                    break;
                case EnumDecl:
                    createEnum(cursor);
                    break;
                case TypedefDecl:
                    // anonymous typedef struct {} xxx will not get TypeAlias
                    if (jt instanceof TypeAlias) {
                        defineType(cursor, (TypeAlias) jt);
                    }
                    break;
                case VarDecl:
                    addVar(global_cw, cursor, null);
                    break;
                default:
                    logger.warning(() -> "Unsupported declaration Cursor:");
                    logger.warning(() -> Printer.Stringifier(p -> p.dumpCursor(cursor, true)));
                    break;
            }
        } catch (Exception ex) {
            handleException(ex);
            logger.warning("Cursor causing above exception is: " + cursor.spelling());
            logger.warning(() -> Printer.Stringifier(p -> p.dumpCursor(cursor, true)));
        }
        return this;
    }

    AsmCodeFactory generateMacros() {
        for (MacroParser.Macro macro : ctx.macros(owner)) {
            if (macro.isConstantMacro()) {
                logger.fine(() -> "Adding macro " + macro.name());
                Object value = macro.value();
                Class<?> macroType = (Class<?>) Util.unboxIfNeeded(value.getClass());

                String sig = jdk.internal.org.objectweb.asm.Type.getMethodDescriptor(jdk.internal.org.objectweb.asm.Type.getType(macroType));
                MethodVisitor mv = global_cw.visitMethod(ACC_PUBLIC, macro.name(), sig, sig, null);

                Cursor cursor = macro.cursor();
                AnnotationVisitor av = mv.visitAnnotation(NATIVE_LOCATION, true);
                SourceLocation src = cursor.getSourceLocation();
                SourceLocation.Location loc = src.getFileLocation();
                Path p = loc.path();
                av.visit("file", p == null ? "builtin" : p.toAbsolutePath().toString());
                av.visit("line", loc.line());
                av.visit("column", loc.column());
                av.visit("USR", cursor.USR());
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
            } else {
                logger.fine(() -> "Skipping unrecognized object-like macro " + macro.name());
            }
        }

        return this;
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

    public static void main(String[] args) throws IOException {
        final Path file = Paths.get(args[1]);
        final String pkg = args[0];
        Context ctx = new Context();
        ctx.usePackageForFolder(file, pkg);
        ctx.usePackageForFolder(Paths.get("/usr/include"), "system");
        ctx.addSource(file);
        ctx.parse();
        ctx.collectJarFile(Paths.get(args[2]), args, pkg);
    }
}
