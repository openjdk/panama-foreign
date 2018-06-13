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

/**
 * A Java Type descriptor
 */
public interface JType {

    /**
     * The descriptor of this type
     *
     * @return The type descriptor as defined in JVMS 4.3
     */
    public String getDescriptor();

    default public String getSignature() { return getDescriptor(); }

    public final static JType Void = () -> "V";
    public final static JType Byte = () -> "B";
    public final static JType Bool = () -> "Z";
    public final static JType Char = () -> "C";
    public final static JType Short = () -> "S";
    public final static JType Int = () -> "I";
    public final static JType Long = () -> "J";
    public final static JType Float = () -> "F";
    public final static JType Double = () -> "D";
    public final static JType Object = new ObjectRef(java.lang.Object.class);

    public static JType of(final Class<?> cls) {
        if (cls.isArray()) {
            return ofArray(JType.of(cls.getComponentType()));
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
        return new ObjectRef(cls);
    }

    public static String boxing(JType t) {
        if (t instanceof JType2) {
            t = ((JType2) t).getDelegate();
        }

        if (t == JType.Bool) {
            return "Ljava/lang/Boolean;";
        } else if (t == JType.Char) {
            return "Ljava/lang/Character;";
        } else if (t == JType.Int) {
            return "Ljava/lang/Integer;";
        } else if (t == JType.Byte) {
            return "Ljava/lang/Byte;";
        } else if (t == JType.Double) {
            return "Ljava/lang/Double;";
        } else if (t == JType.Float) {
            return "Ljava/lang/Float;";
        } else if (t == JType.Long) {
            return "Ljava/lang/Long;";
        } else if (t == JType.Short) {
            return "Ljava/lang/Short;";
        } else if (t == JType.Void) {
            return "Ljava/lang/Void;";
        } else {
            return t.getSignature();
        }
    }

    public static JType of(String clsName) {
        return new ObjectRef(clsName);
    }
    public static JType ofArray(JType elementType) { return new ArrayType(elementType); }

    /**
     * Return Java type signature for JType. If JType is a Pointer&lt;Void&gt;, return as
     * Pointer&lt;?&gt;
     * @param jt The JType to get signature for
     * @return The Java type signature
     */
    static String getPointerVoidAsWildcard(JType jt) {
        // Return Pointer<?> instead of Pointer<Void>
        if (jt instanceof JType2) {
            jt = ((JType2) jt).getDelegate();
        }
        if (jt instanceof TypeAlias) {
            jt = ((TypeAlias) jt).canonicalType();
        }
        if (jt instanceof PointerType) {
            return ((PointerType) jt).getSignature(true);
        } else {
            return jt.getSignature();
        }
    }

    static class ObjectRef implements JType {
        final String clsName;

        ObjectRef(Class<?> cls) {
            this.clsName = cls.getName().replace('.', '/');
        }

        ObjectRef(String clsName) {
            this.clsName = clsName;
        }

        @Override
        public String getDescriptor() {
            return "L" + clsName + ";";
        }
    }

    final static class InnerType implements JType {
        final String outerName;
        final String name;

        InnerType(Class<?> outer, String name) {
            outerName = outer.getName().replace('.', '/');
            this.name = name;
        }

        InnerType(String outerName, String name) {
            this.outerName = outerName;
            this.name = name;
        }

        public String getOuterClassName() {
            return outerName;
        }

        public String getName() {
            return name;
        }

        @Override
        public String getDescriptor() {
            return "L" + outerName + "$" + name + ";";
        }
    }

    final class ArrayType implements JType {
        final JType elementType;

        ArrayType(JType type) {
            elementType = type;
        }

        @Override
        public String getDescriptor() {
            return JType.of(java.nicl.types.Array.class).getDescriptor();
        }

        @Override
        public String getSignature() {
            StringBuilder sb = new StringBuilder();
            sb.append("L");
            sb.append(java.nicl.types.Array.class.getName().replace('.', '/'));
            sb.append("<");
            JType pt = elementType;
            if (pt instanceof JType2) {
                pt = ((JType2) pt).getDelegate();
            }
            if (pt instanceof TypeAlias) {
                pt = ((TypeAlias) pt).canonicalType();
            }
            sb.append(JType.boxing(pt));
            sb.append(">;");
            return sb.toString();
        }

        public JType getElementType() {
            return elementType;
        }
    }

    final static class Function implements JType {
        final JType returnType;
        final JType[] args;
        final boolean isVarArgs;
        final java.nicl.layout.Function layout;

        Function(java.nicl.layout.Function layout, boolean isVarArgs, JType returnType, JType... args) {
            this.layout = layout;
            this.returnType = returnType;
            this.args = args;
            this.isVarArgs = isVarArgs;
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
        public String getSignature() {
            StringBuilder sb = new StringBuilder();
            sb.append('(');
            // ensure sequence
            for (int i = 0; i < args.length; i++) {
                sb.append(getPointerVoidAsWildcard(args[i]));
            }
            if (isVarArgs) {
                sb.append("[Ljava/lang/Object;");
            }
            sb.append(')');
            sb.append(returnType.getSignature());
            return sb.toString();
        }

        public String getNativeDescriptor() {
            return layout.toString();
        }
    }

    final static class FnIf implements JType {
        final JType type;
        final Function fn;

        FnIf(JType type, Function fn) {
            this.type = type;
            this.fn = fn;
        }

        @Override
        public String getDescriptor() {
            return type.getDescriptor();
        }

        @Override
        public String getSignature() {
            return type.getSignature();
        }

        Function getFunction() {
            return fn;
        }

        @Override
        public String toString() {
            return "FunctionalInterface: " + getDescriptor() + " for " + fn.getSignature();
        }
    }
}
