/*
 * Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved.
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
package jdk.internal.nicl;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Type;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.nicl.Libraries;
import java.nicl.metadata.*;
import java.nicl.types.LayoutType;
import java.nicl.types.Pointer;
import java.nicl.types.Reference;

import static sun.security.action.GetPropertyAction.privilegedGetProperty;
import static jdk.internal.org.objectweb.asm.Opcodes.*;

class HeaderImplGenerator extends ClassGenerator {
    private static final boolean FAIL_EAGERLY = Boolean.valueOf(
        privilegedGetProperty("jdk.internal.nicl.HeaderImplGenerator.FAIL_EAGERLY", "false"));

    // name of pointer field, only used for record types
    private static final String POINTER_FIELD_NAME = "ptr";

    // names of helper methods defined in the RuntimeSupport class
    private static final String BUILD_REF_NAME = "buildRef";
    private static final String PTR_COPY_TO_ARRAY_OBJECT = "ptrCopyToArrayObject";
    private static final String PTR_COPY_TO_ARRAY_INT = "ptrCopyToArrayInt";
    private static final String PTR_COPY_FROM_ARRAY_OBJECT = "ptrCopyFromArrayObject";
    private static final String PTR_COPY_FROM_ARRAY_INT = "ptrCopyFromArrayInt";

    // the interface to implement
    private final Class<?> c;

    // lookup helper to use for looking up symbols
    private final SymbolLookup lookup;

    // true if the interface is representing a struct/class
    private final boolean isRecordType;

    HeaderImplGenerator(Class<?> hostClass, String implClassName, Class<?> c, SymbolLookup lookup, boolean isRecordType) {
        super(hostClass, implClassName, new Class<?>[] { c });

        this.c = c;
        this.lookup = lookup;
        this.isRecordType = isRecordType;
    }

    enum AccessorMethodType {
        get, set, ref;

        MethodType getMethodType(Class<?> c) {
            switch (this) {
                case get: return MethodType.methodType(c);
                case set: return MethodType.methodType(void.class, c);
                case ref: return MethodType.methodType(Reference.class);
            }

            throw new IllegalArgumentException("Unhandled type: " + this);
        }
    }

    private void generateGlobalVariableMethods(ClassGeneratorContext ctxt, Method method, String symbolName) {
        Class<?> c = method.getReturnType();
        java.lang.reflect.Type type = method.getGenericReturnType();
        LayoutType<?> lt = Util.createLayoutType(type);

        int dollarIndex = method.getName().indexOf("$");
        String methodBaseName = method.getName().substring(0, dollarIndex);
        Pointer<?> p;

        try {
            p = lookup.lookup(symbolName).getAddress().cast(lt);
        } catch (NoSuchMethodException e) {
            if (FAIL_EAGERLY) {
                throw new RuntimeException(e);
            }

            UnsupportedOperationException uoe = new UnsupportedOperationException("Trying to call method " + method.getName() + " for which native lookup failed", e);
            generateExceptionThrowingMethod(ctxt, method.getName(), Util.methodTypeFor(method), method.isVarArgs(), uoe);
            return;
        }

        for (AccessorMethodType t : AccessorMethodType.values()) {
            String methodName = methodBaseName + "$" + t.name();
            MethodHandle target;
            try {
                switch (t) {
                    case get:
                        target = MethodHandles.publicLookup().findVirtual(Reference.class, "get", MethodType.methodType(Object.class));
                        target = target.bindTo(p.lvalue()).asType(MethodType.methodType(c));
                        break;

                    case set:
                        target = MethodHandles.publicLookup().findVirtual(Reference.class, "set", MethodType.methodType(void.class, Object.class));
                        target = target.bindTo(p.lvalue()).asType(MethodType.methodType(void.class, c));
                        break;

                    case ref:
                        target = MethodHandles.constant(Reference.class, p.lvalue());
                        break;

                    default:
                        throw new InternalError("Unexpected access method type: " + t);
                }
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            MethodGenerator generator = new GenericMethodImplGenerator(ctxt, methodName, t.getMethodType(c), false, target);
            generator.generate();
        }
    }

    private String getGetterBaseName(Method method) {
        String name = method.getName();

        if (!name.endsWith("$get")) {
            throw new IllegalArgumentException("Unexpected method name " + method.getName());
        }

        return name.substring(0, name.lastIndexOf("$"));
    }

    private String getSymbolName(Method method, String defaultName) {
        String name = method.getAnnotation(NativeType.class).name();
        if (NativeType.NO_NAME.equals(name)) {
            // FIXME: Make this an error (require name to be set)?
            return defaultName;
        } else {
            return name;
        }
    }

    private String getSymbolName(Method method) {
        return getSymbolName(method, method.getName());
    }

    private void generateArrayFieldAccessors(ClassGeneratorContext ctxt, Method method, Class<?> javaType, long offset) {
        Array ar = method.getAnnotation(Array.class);
        if (null == ar) {
            throw new IllegalStateException("Array return type should have Array annotation");
        }
        final long length = ar.length();
        if (length > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Array size is too large");
        }

        generateArrayGetter(ctxt, method, javaType, offset, (int) length);
        generateArraySetter(ctxt, method, javaType, offset, (int) length);
    }

    private void allocArray(MethodVisitor mv, Class<?> componentType, int length) {
        mv.visitLdcInsn(length);

        if (componentType.isPrimitive()) {
            switch (PrimitiveClassType.typeof(componentType)) {
                case INT:
                    mv.visitIntInsn(NEWARRAY, T_INT);
                    break;

                // FIXME: Add other primitives here
                default:
                    throw new IllegalArgumentException("Unhandled type: " + componentType);
            }
        } else {
            mv.visitTypeInsn(ANEWARRAY, Type.getInternalName(componentType));
        }
    }

    private void generateArrayGetter(ClassGeneratorContext ctxt, Method method, Class<?> javaType, long offset, int length) {
        ClassWriter cw = ctxt.getClassWriter();
        Class<?> componentType = javaType.getComponentType();

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, method.getName(), Type.getMethodDescriptor(Type.getType(method.getReturnType())), null, null);
        mv.visitCode();

        allocArray(mv, componentType, length);
        mv.visitVarInsn(ASTORE, 1);

        //load receiver MH
        mv.visitFieldInsn(GETSTATIC, implClassName, componentType.isPrimitive() ?
                PTR_COPY_TO_ARRAY_INT : PTR_COPY_TO_ARRAY_OBJECT,
                Type.getDescriptor(MethodHandle.class));

        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassName, POINTER_FIELD_NAME, Type.getDescriptor(Pointer.class));
        mv.visitLdcInsn(offset);

        mv.visitVarInsn(ALOAD, 1);

        mv.visitLdcInsn(length);

        if (componentType.isPrimitive()) {
            switch (PrimitiveClassType.typeof(componentType)) {
                case INT:
                    mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(MethodHandle.class), "invokeExact", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Pointer.class), Type.LONG_TYPE, Type.getType(int[].class), Type.INT_TYPE), false);
                    break;

                // FIXME: Add other primitives here
                default:
                    throw new UnsupportedOperationException("Unhandled component type: " + componentType);
            }
        } else {
            mv.visitLdcInsn(Type.getType(componentType));
            mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(MethodHandle.class), "invokeExact", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Pointer.class), Type.LONG_TYPE, Type.getType(Object[].class), Type.INT_TYPE, Type.getType(Class.class)), false);
        }

        mv.visitVarInsn(ALOAD, 1);

        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateArraySetter(ClassGeneratorContext ctxt, Method method, Class<?> javaType, long offset, int length) {
        ClassWriter cw = ctxt.getClassWriter();
        Class<?> componentType = javaType.getComponentType();

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, method.getName().replace("$get", "$set"), Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(method.getReturnType())), null, null);
        mv.visitCode();

        //load receiver MH
        mv.visitFieldInsn(GETSTATIC, implClassName, componentType.isPrimitive() ?
                PTR_COPY_FROM_ARRAY_INT : PTR_COPY_FROM_ARRAY_OBJECT,
                Type.getDescriptor(MethodHandle.class));

        mv.visitVarInsn(ALOAD, 1);

        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassName, POINTER_FIELD_NAME, Type.getDescriptor(Pointer.class));
        mv.visitLdcInsn(offset);

        mv.visitLdcInsn(length);

        if (componentType.isPrimitive()) {
            switch (PrimitiveClassType.typeof(componentType)) {
                case INT:
                    mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(MethodHandle.class), "invokeExact", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(int[].class), Type.getType(Pointer.class), Type.LONG_TYPE, Type.INT_TYPE), false);
                    break;

                // FIXME: Add other primitives here
                default:
                    throw new UnsupportedOperationException("Unhandled component type: " + componentType);
            }
        } else {
            mv.visitLdcInsn(Type.getType(componentType));
            mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(MethodHandle.class), "invokeExact", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object[].class), Type.getType(Pointer.class), Type.LONG_TYPE, Type.INT_TYPE, Type.getType(Class.class)), false);
        }

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void box(MethodVisitor mv, Class<?> c) {
        if (c.isPrimitive()) {
            switch (PrimitiveClassType.typeof(c)) {
                case BOOLEAN:
                    mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Boolean.class), "valueOf", Type.getMethodDescriptor(Type.getType(Boolean.class), Type.BOOLEAN_TYPE), false);
                    break;
                case BYTE:
                    mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Byte.class), "valueOf", Type.getMethodDescriptor(Type.getType(Byte.class), Type.BYTE_TYPE), false);
                    break;
                case SHORT:
                    mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Short.class), "valueOf", Type.getMethodDescriptor(Type.getType(Short.class), Type.SHORT_TYPE), false);
                    break;
                case CHAR:
                    mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Character.class), "valueOf", Type.getMethodDescriptor(Type.getType(Character.class), Type.CHAR_TYPE), false);
                    break;
                case INT:
                    mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Integer.class), "valueOf", Type.getMethodDescriptor(Type.getType(Integer.class), Type.INT_TYPE), false);
                    break;
                case FLOAT:
                    mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Float.class), "valueOf", Type.getMethodDescriptor(Type.getType(Float.class), Type.FLOAT_TYPE), false);
                    break;
                case LONG:
                    mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Long.class), "valueOf", Type.getMethodDescriptor(Type.getType(Long.class), Type.LONG_TYPE), false);
                    break;
                case DOUBLE:
                    mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Double.class), "valueOf", Type.getMethodDescriptor(Type.getType(Double.class), Type.DOUBLE_TYPE), false);
                    break;
                case VOID:
                    throw new IllegalArgumentException();
            }
        } else {
            throw new UnsupportedOperationException("Unhandled type: " + c);
        }
    }


    private void unbox(MethodVisitor mv, Class<?> c) {
        if (c.isPrimitive()) {
            switch (PrimitiveClassType.typeof(c)) {
                case BOOLEAN:
                    mv.visitTypeInsn(CHECKCAST, Type.getInternalName(Boolean.class));
                    mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Boolean.class), "booleanValue", Type.getMethodDescriptor(Type.BOOLEAN_TYPE), false);
                    break;
                case BYTE:
                    mv.visitTypeInsn(CHECKCAST, Type.getInternalName(Byte.class));
                    mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Byte.class), "byteValue", Type.getMethodDescriptor(Type.BYTE_TYPE), false);
                    break;
                case CHAR:
                    mv.visitTypeInsn(CHECKCAST, Type.getInternalName(Character.class));
                    mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Character.class), "charValue", Type.getMethodDescriptor(Type.CHAR_TYPE), false);
                    break;
                case SHORT:
                    mv.visitTypeInsn(CHECKCAST, Type.getInternalName(Short.class));
                    mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Short.class), "shortValue", Type.getMethodDescriptor(Type.SHORT_TYPE), false);
                    break;
                case INT:
                    mv.visitTypeInsn(CHECKCAST, Type.getInternalName(Integer.class));
                    mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Integer.class), "intValue", Type.getMethodDescriptor(Type.INT_TYPE), false);
                    break;
                case FLOAT:
                    mv.visitTypeInsn(CHECKCAST, Type.getInternalName(Float.class));
                    mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Float.class), "floatValue", Type.getMethodDescriptor(Type.FLOAT_TYPE), false);
                    break;
                case LONG:
                    mv.visitTypeInsn(CHECKCAST, Type.getInternalName(Long.class));
                    mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Long.class), "longValue", Type.getMethodDescriptor(Type.LONG_TYPE), false);
                    break;
                case DOUBLE:
                    mv.visitTypeInsn(CHECKCAST, Type.getInternalName(Double.class));
                    mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Double.class), "doubleValue", Type.getMethodDescriptor(Type.DOUBLE_TYPE), false);
                    break;
                case VOID:
                    throw new IllegalArgumentException();
            }
        }
    }

    private void pushRef(MethodVisitor mv, long off, String layoutTypeFieldName) {
        /*
         * ref(<offset>, this.<layoutTypeField>)
         */
        mv.visitVarInsn(ALOAD, 0);

        mv.visitLdcInsn(off);
        mv.visitFieldInsn(GETSTATIC, implClassName, layoutTypeFieldName, Type.getDescriptor(LayoutType.class));

        mv.visitMethodInsn(INVOKEVIRTUAL, implClassName, "ref", Type.getMethodDescriptor(Type.getType(Reference.class), Type.LONG_TYPE, Type.getType(LayoutType.class)), false);
    }

    private void generateGetter(MethodVisitor mv, long off, Class<?> javaType, String layoutTypeFieldName) {
        /*
         * return this.ref(<offset>, this.<layoutTypeField>).get();
         */

        pushRef(mv, off, layoutTypeFieldName);

        mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(Reference.class), "get", Type.getMethodDescriptor(Type.getType(Object.class)), true);

        if (javaType.isPrimitive()) {
            unbox(mv, javaType);
        } else {
            mv.visitTypeInsn(CHECKCAST, Type.getInternalName(javaType));
        }

        mv.visitInsn(Util.returnInsn(javaType));
    }

    private void generateSetter(MethodVisitor mv, long off, Class<?> javaType, String layoutTypeFieldName) {
        /*
         * this.ref(<offset>, this.<layoutTypeField>).set(<value>);
         */

        pushRef(mv, off, layoutTypeFieldName);

        mv.visitVarInsn(Util.loadInsn(javaType), 1);

        if (javaType.isPrimitive()) {
            box(mv, javaType);
        }

        mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(Reference.class), "set", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class)), true);

        mv.visitInsn(RETURN);
    }

    private void generateConstructor(ClassGeneratorContext ctxt) {
        /*
         * <init>(Pointer p) {
         *     super();
         *     this.p = p;
         * }
         */
        ClassWriter cw = ctxt.getClassWriter();
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Pointer.class)), null, null);
        mv.visitCode();

        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false);

        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitFieldInsn(PUTFIELD, implClassName, POINTER_FIELD_NAME, Type.getDescriptor(Pointer.class));

        mv.visitInsn(RETURN);

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateNormalFieldAccessors(ClassGeneratorContext ctxt, Method method, Class<?> javaType, long offset) {
        ClassWriter cw = ctxt.getClassWriter();

        LayoutType<?> lt = Util.createLayoutType(method.getGenericReturnType());

        FieldGenerator layoutField = new FieldGenerator(method.getName() + "$layout", true, lt) {
            @Override
            public void generate(ClassWriter cw) {
                cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, getName(), Type.getDescriptor(LayoutType.class), null, null);
            }
        };

        ctxt.getFieldsBuilder().add(layoutField);


        // Getter
        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, method.getName(), Type.getMethodDescriptor(Type.getType(javaType)), null, null);
            mv.visitCode();

            generateGetter(mv, offset, javaType, layoutField.getName());

            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        // Setter
        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, method.getName().replace("$get", "$set"),
                    Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(javaType)), null, null);
            mv.visitCode();

            generateSetter(mv, offset, javaType, layoutField.getName());

            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        // Reference
        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, method.getName().replace("$get", "$ref"),
                    Type.getMethodDescriptor(Type.getType(Reference.class)), null, null);
            mv.visitCode();

            pushRef(mv, offset, layoutField.getName());

            mv.visitInsn(ARETURN);

            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }

    private void generateFieldAccessors(ClassGeneratorContext ctxt, Method method, long offset) {
        Class<?> javaType = method.getReturnType();

        try {
            if (javaType.isArray()) {
                generateArrayFieldAccessors(ctxt, method, javaType, offset);
            } else {
                generateNormalFieldAccessors(ctxt, method, javaType, offset);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create accessors for " + method, e);
        }
    }

    private void generateReferenceImpl(ClassGeneratorContext ctxt) {
        ClassWriter cw = ctxt.getClassWriter();

        // Reference<T>.get()
        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "get", Type.getMethodDescriptor(Type.getType(Object.class)), null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        // Reference<T>.set()
        // FIXME: Copy here?
        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "set", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class)), null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, implClassName);
            mv.visitFieldInsn(GETFIELD, implClassName, POINTER_FIELD_NAME, Type.getDescriptor(Pointer.class));
            mv.visitFieldInsn(PUTFIELD, implClassName, POINTER_FIELD_NAME, Type.getDescriptor(Pointer.class));
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }

    private void generatePointerGetter(ClassGeneratorContext ctxt) {
        ClassWriter cw = ctxt.getClassWriter();
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "ptr", Type.getMethodDescriptor(Type.getType(Pointer.class)), null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassName, POINTER_FIELD_NAME, Type.getDescriptor(Pointer.class));
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateRefHelper(ClassGeneratorContext ctxt) {
        /*
         * MethodHandle buildRef = <buildRef>
         * private <T> Reference<T> ref(long offset, LayoutType<T> t) {
         *     return buildRef.invokeExact(p, offset, t);
         * }
         */
        ClassWriter cw = ctxt.getClassWriter();
        MethodVisitor mv = cw.visitMethod(ACC_PRIVATE, "ref", Type.getMethodDescriptor(Type.getType(Reference.class), Type.LONG_TYPE, Type.getType(LayoutType.class)), null, null);
        mv.visitCode();
        mv.visitFieldInsn(GETSTATIC, implClassName, BUILD_REF_NAME, Type.getDescriptor(MethodHandle.class));
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassName, POINTER_FIELD_NAME, Type.getDescriptor(Pointer.class));
        mv.visitVarInsn(LLOAD, 1);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(MethodHandle.class), "invokeExact", Type.getMethodDescriptor(Type.getType(Reference.class), Type.getType(Pointer.class), Type.LONG_TYPE, Type.getType(LayoutType.class)), false);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateHelperHandles(ClassGeneratorContext ctxt) {
        try {
            ctxt.getFieldsBuilder().add(new FieldGenerator(BUILD_REF_NAME, true,
                    MethodHandles.lookup().findStatic(RuntimeSupport.class, "buildRef", MethodType.methodType(Reference.class, Pointer.class, long.class, LayoutType.class))) {
                @Override
                public void generate(ClassWriter cw) {
                    cw.visitField(ACC_PRIVATE | ACC_FINAL | ACC_STATIC, getName(), Type.getDescriptor(MethodHandle.class), null, null);
                }
            });

            ctxt.getFieldsBuilder().add(new FieldGenerator(PTR_COPY_TO_ARRAY_INT, true,
                    MethodHandles.lookup().findStatic(RuntimeSupport.class, "copyToArray", MethodType.methodType(void.class, Pointer.class, long.class, int[].class, int.class))) {
                @Override
                public void generate(ClassWriter cw) {
                    cw.visitField(ACC_PRIVATE | ACC_FINAL | ACC_STATIC, getName(), Type.getDescriptor(MethodHandle.class), null, null);
                }
            });

            ctxt.getFieldsBuilder().add(new FieldGenerator(PTR_COPY_TO_ARRAY_OBJECT, true,
                    MethodHandles.lookup().findStatic(RuntimeSupport.class, "copyToArray", MethodType.methodType(void.class, Pointer.class, long.class, Object[].class, int.class, Class.class))) {
                @Override
                public void generate(ClassWriter cw) {
                    cw.visitField(ACC_PRIVATE | ACC_FINAL | ACC_STATIC, getName(), Type.getDescriptor(MethodHandle.class), null, null);
                }
            });

            ctxt.getFieldsBuilder().add(new FieldGenerator(PTR_COPY_FROM_ARRAY_INT, true,
                    MethodHandles.lookup().findStatic(RuntimeSupport.class, "copyFromArray", MethodType.methodType(void.class, int[].class, Pointer.class, long.class, int.class))) {
                @Override
                public void generate(ClassWriter cw) {
                    cw.visitField(ACC_PRIVATE | ACC_FINAL | ACC_STATIC, getName(), Type.getDescriptor(MethodHandle.class), null, null);
                }
            });

            ctxt.getFieldsBuilder().add(new FieldGenerator(PTR_COPY_FROM_ARRAY_OBJECT, true,
                    MethodHandles.lookup().findStatic(RuntimeSupport.class, "copyFromArray", MethodType.methodType(void.class, Object[].class, Pointer.class, long.class, int.class, Class.class))) {
                @Override
                public void generate(ClassWriter cw) {
                    cw.visitField(ACC_PRIVATE | ACC_FINAL | ACC_STATIC, getName(), Type.getDescriptor(MethodHandle.class), null, null);
                }
            });
        } catch (Throwable ex) {
            throw new IllegalStateException();
        }
    }

    private void generatePointerField(ClassGeneratorContext ctxt) {
        ctxt.getFieldsBuilder().add(new FieldGenerator(POINTER_FIELD_NAME, false) {
            @Override
            public void generate(ClassWriter cw) {
                cw.visitField(ACC_PRIVATE | ACC_FINAL, getName(), Type.getDescriptor(Pointer.class), null, null);
            }
        });
    }

    private void generateExceptionThrowingMethod(ClassGeneratorContext ctxt, String methodName, MethodType methodType, boolean isVarArgs, Exception e) {
        // Drop all arguments and delegate to a bound, throwing method handle
        MethodHandle target = MethodHandles.dropArguments(MethodHandles.throwException(methodType.returnType(), e.getClass()).bindTo(e), 0, methodType.parameterArray()).asType(methodType);
        MethodGenerator generator = new GenericMethodImplGenerator(ctxt, methodName, methodType, isVarArgs, target, false);
        generator.generate();
    }

    private void generateMethod(ClassGeneratorContext ctxt, Method method) {
        if (method.isAnnotationPresent(Offset.class)) {
            if (!method.isAnnotationPresent(C.class) || !method.isAnnotationPresent(NativeType.class)) {
                throw new IllegalArgumentException("Unexpectedly found an @Offset annotated method without a @NativeType annotation");
            }

            if (!isRecordType) {
                throw new IllegalArgumentException("Unexpectedly found an @Offset annotated method in a non-record type");
            }

            long off = method.getAnnotation(Offset.class).offset();
            if (off < 0 || off % 8 != 0) {
                throw new Error("NYI: Sub-byte offsets (" + off + ") in struct type: " + c);
            }
            off = off / 8;

            generateFieldAccessors(ctxt, method, off);
        } else if (method.isAnnotationPresent(NativeType.class) && !Util.isFunction(method)) {
            /*
             * Native type is not a function, so this must be a global
             * variable (typically an extern variable of some sort).
             */
            generateGlobalVariableMethods(ctxt, method, getSymbolName(method, getGetterBaseName(method)));
        } else if (method.isDefault()) {
            // default methods don't need implementations
        } else if (method.isAnnotationPresent(C.class)) {
            if (method.isAnnotationPresent(CallingConvention.class)) {
                MethodType methodType = Util.methodTypeFor(method);
                NativeInvoker invoker;

                try {
                    invoker = new NativeInvoker(methodType, method.isVarArgs(), lookup, getSymbolName(method), method.toString(), method.getGenericReturnType());
                } catch (NoSuchMethodException | IllegalAccessException e) {
                    if (FAIL_EAGERLY) {
                        throw new RuntimeException(e);
                    }

                    UnsupportedOperationException uoe = new UnsupportedOperationException("Trying to call method " + method.getName() + " for which native lookup failed", e);
                    generateExceptionThrowingMethod(ctxt, method.getName(), methodType, method.isVarArgs(), uoe);
                    return;
                }
                MethodGenerator generator = new GenericMethodImplGenerator(ctxt, method.getName(), methodType, method.isVarArgs(), invoker.getBoundMethodHandle());
                generator.generate();

            } else {
                if (FAIL_EAGERLY) {
                    throw new IllegalArgumentException("Unhandled method: " + method);
                }

                // FIXME: Add support for getter method here
                System.err.println("WARNING: Ignoring method " + method + " which does not have an @CallingConvention annotation");
            }
        } else if (method.getName().endsWith("$ref") || method.getName().endsWith("$set")) {
            // ignore - the corresponding methods are generated as part of processing the $get method
        } else if (isRecordType && method.getDeclaringClass() == Reference.class) {
            // ignore - the corresponding methods are generated as part of setting up the record type
        } else {
            if (FAIL_EAGERLY) {
                throw new IllegalArgumentException("Unhandled method: " + method);
            }

            // FIXME: Convert this to a RuntimeError or something - there should be no unknown methods, right?
            UnsupportedOperationException uoe = new UnsupportedOperationException("Trying to call method " + method.getName() + " which does not have a @C annotation");
            generateExceptionThrowingMethod(ctxt, method.getName(), Util.methodTypeFor(method), method.isVarArgs(), uoe);
        }
    }

    private void generateMethods(ClassGeneratorContext ctxt) {
        if (isRecordType) {
            generateConstructor(ctxt);
            generateRefHelper(ctxt);
            generateHelperHandles(ctxt);
            generatePointerGetter(ctxt);

            generateReferenceImpl(ctxt);
        }

        for (Method m : c.getMethods()) {
            try {
                generateMethod(ctxt, m);
            } catch (Exception | Error e) {
                throw new RuntimeException("Failed to generate method " + m, e);
            }
        }
    }

    private void generateFields(ClassGeneratorContext ctxt) {
        if (isRecordType) {
            generatePointerField(ctxt);
        }
    }

    @Override
    void generate(ClassGeneratorContext ctxt) {
        /*
        if (!isRecordType && !c.isAnnotationPresent(Header.class)) {
            throw new IllegalArgumentException("Not a @Header");
        }
        */

        generateFields(ctxt);
        generateMethods(ctxt);
    }
}
