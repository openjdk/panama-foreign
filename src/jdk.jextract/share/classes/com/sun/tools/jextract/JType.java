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
package com.sun.tools.jextract;

import java.foreign.memory.Callback;
import java.foreign.memory.Pointer;

/**
 * A Java Type descriptor
 */
public abstract class JType {

    /**
     * The descriptor of this type
     *
     * @return The type descriptor as defined in JVMS 4.3
     */
    public abstract String getDescriptor();

    public String getSignature(boolean isArgument) { return getDescriptor(); }

    public final static JType Void = new PrimitiveType("V", of(Void.class));
    public final static JType Byte = new PrimitiveType("B", of(Byte.class));
    public final static JType Bool = new PrimitiveType("Z", of(Boolean.class));
    public final static JType Char = new PrimitiveType("C", of(Character.class));
    public final static JType Short = new PrimitiveType("S", of(Short.class));
    public final static JType Int = new PrimitiveType("I", of(Integer.class));
    public final static JType Long = new PrimitiveType("J", of(Long.class));
    public final static JType Float = new PrimitiveType("F", of(Float.class));
    public final static JType Double = new PrimitiveType("D", of(Double.class));
    public final static JType Object = of(java.lang.Object.class);

    public static JType of(final Class<?> cls) {
        if (cls.isArray()) {
            return new ArrayType(JType.of(cls.getComponentType()));
        }
        if (cls.isPrimitive()) {
            switch (cls.getTypeName()) {
                case "int":
                    return JType.Int;
                case "long":
                    return JType.Long;
                case "byte":
                    return JType.Byte;
                case "char":
                    return JType.Char;
                case "float":
                    return JType.Float;
                case "double":
                    return JType.Double;
                case "short":
                    return JType.Short;
                case "boolean":
                    return JType.Bool;
                case "void":
                    return JType.Void;
            }
        }
        if (cls == Object.class) {
            return JType.Object;
        }
        // assuming reference
        return new ClassType(binaryName(cls));
    }

    private static String binaryName(Class<?> cls) {
        return cls.getName().replace('.', '/');
    }

    public JType box() {
        return this;
    }

    public static class PrimitiveType extends JType {
        String desc;
        JType boxed;

        PrimitiveType(String desc, JType boxed) {
            this.desc = desc;
            this.boxed = boxed;
        }

        @Override
        public JType box() {
            return boxed;
        }

        @Override
        public String getDescriptor() {
            return desc;
        }
    }

    public static class ClassType extends JType {
        final String clsName;

        ClassType(String clsName) {
            this.clsName = clsName;
        }

        @Override
        public String getDescriptor() {
            return "L" + clsName + ";";
        }

        public String getSimpleName() {
            int innerEnd = clsName.lastIndexOf('$');
            int packageEnd = clsName.lastIndexOf('.');
            if (innerEnd != -1) {
                return clsName.substring(innerEnd + 1);
            } else if (packageEnd != -1) {
                return clsName.substring(packageEnd + 1);
            } else {
                return clsName;
            }
        }
    }

    public final static class ArrayType extends JType {
        final JType elementType;

        ArrayType(JType type) {
            elementType = type;
        }

        @Override
        public String getDescriptor() {
            return JType.of(java.foreign.memory.Array.class).getDescriptor();
        }

        @Override
        public String getSignature(boolean isArgument) {
            StringBuilder sb = new StringBuilder();
            sb.append("L");
            sb.append(java.foreign.memory.Array.class.getName().replace('.', '/'));
            sb.append("<");
            JType pt = elementType;
            sb.append(pt.box().getSignature(isArgument));
            sb.append(">;");
            return sb.toString();
        }

        public JType getElementType() {
            return elementType;
        }
    }

    public final static class Function extends JType {
        final JType returnType;
        final JType[] args;
        final boolean isVarArgs;
        final java.foreign.layout.Function layout;

        Function(java.foreign.layout.Function layout, boolean isVarArgs, JType returnType, JType... args) {
            this.layout = layout;
            this.returnType = returnType;
            this.args = args;
            for (int i = 0; i < args.length; i++) {
                args[i] = arrayAsPointer(args[i]);
            }
            this.isVarArgs = isVarArgs;
        }

        private static JType arrayAsPointer(JType t) {
            return t instanceof ArrayType ?
                    GenericType.ofPointer(((ArrayType)t).elementType) :
                    t;
        }

        @Override
        public String getDescriptor() {
            StringBuilder sb = new StringBuilder();
            sb.append('(');
            // ensure sequence
            for (int i = 0; i < args.length; i++) {
                sb.append(args[i].getDescriptor());
            }
            if (isVarArgs) {
                sb.append("[Ljava/lang/Object;");
            }
            sb.append(')');
            sb.append(returnType.getDescriptor());
            return sb.toString();
        }

        @Override
        public String getSignature(boolean isArgument) {
            StringBuilder sb = new StringBuilder();
            sb.append('(');
            // ensure sequence
            for (int i = 0; i < args.length; i++) {
                sb.append(args[i].getSignature(true));
            }
            if (isVarArgs) {
                sb.append("[Ljava/lang/Object;");
            }
            sb.append(')');
            sb.append(returnType.getSignature(false));
            return sb.toString();
        }

        public String getNativeDescriptor() {
            return layout.toString();
        }
    }

    final static class FunctionalInterfaceType extends ClassType {
        final Function fn;

        FunctionalInterfaceType(String name, Function fn) {
            super(name);
            this.fn = fn;
        }

        Function getFunction() {
            return fn;
        }
    }

    public static class GenericType extends ClassType {
        JType targ;

        GenericType(String base, JType targ) {
            super(base);
            this.targ = targ;
        }

        public JType getTypeArgument() {
            return targ;
        }

        @Override
        public String getSignature(boolean isArgument) {
            StringBuilder sb = new StringBuilder();
            sb.append("L");
            sb.append(clsName);
            sb.append("<");
            if (targ == JType.Void && isArgument) {
                sb.append("*");
            } else {
                if (targ instanceof GenericType && isArgument) {
                    sb.append("+");
                }
                sb.append(targ.box().getSignature(isArgument));
            }
            sb.append(">;");
            return sb.toString();
        }

        public static GenericType ofPointer(JType targ) {
            return new GenericType(JType.binaryName(Pointer.class), targ);
        }

        public static GenericType ofCallback(JType targ) {
            return new GenericType(JType.binaryName(Callback.class), targ);
        }
    }
}
