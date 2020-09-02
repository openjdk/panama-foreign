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

import jdk.incubator.jextract.Type;
import java.util.stream.Collectors;

public class AnnotationWriter implements Type.Visitor<String, Void> {
    @Override
    public String visitPrimitive(Type.Primitive t, Void aVoid) {
        if (t.kind().layout().isEmpty()) {
            return "void"; //skip for now
        } else {
            return t.kind().typeName();
        }
    }

    @Override
    public String visitDelegated(Type.Delegated t, Void aVoid) {
        if (t.kind() == Type.Delegated.Kind.TYPEDEF) {
            return t.name().get();
        } else if (t.kind() == Type.Delegated.Kind.POINTER) {
            String typeStr = t.type().accept(this, null);
            // FIXME Revisit this logic for pointer to function types
            if (t.type() instanceof Type.Function) {
                return typeStr.replace("(", "(*)(");
            } else {
                return typeStr + "*";
            }
        } else {
            String prefix = switch (t.kind()) {
                case ATOMIC -> "_Atomic";
                case COMPLEX -> "complex";
                case SIGNED -> "signed";
                case UNSIGNED -> "unsigned";
                case VOLATILE -> "volatile";
                default -> throw new IllegalStateException("Invalid input" + t);
            };
            return prefix + " " + t.type().accept(this, null);
        }
    }

    @Override
    public String visitFunction(Type.Function t, Void aVoid) {
        String ret = t.returnType().accept(this, null);
        String args = t.argumentTypes().stream().map(p -> p.accept(this, null))
                .collect(Collectors.joining(",", "(", ")"));
        return ret + args;
    }

    @Override
    public String visitDeclared(Type.Declared t, Void aVoid) {
        String name = t.tree().name();
        return switch (t.tree().kind()) {
            case STRUCT -> "struct " + name;
            case UNION -> "union " + name;
            case ENUM -> "enum " + name;
            default -> name;
        };
    }

    @Override
    public String visitArray(Type.Array t, Void aVoid) {
        if (t.kind() == Type.Array.Kind.VECTOR) {
            return ""; //skip for now
        } else {
            return t.elementType().accept(this, null) + "[]";
        }
    }

    @Override
    public String visitType(Type t, Void aVoid) {
        throw new UnsupportedOperationException();
    }

    String getCAnnotation(Type t) {
        return "@C(\"" + t.accept(this, null) + "\")";
    }
}
