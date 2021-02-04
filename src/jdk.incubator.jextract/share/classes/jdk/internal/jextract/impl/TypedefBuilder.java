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

public class TypedefBuilder extends NestedClassBuilder {

    private final Type type;
    private final String superClass;

    public TypedefBuilder(JavaSourceBuilder prev, String className, String superClass, Type type) {
        super(prev, Kind.CLASS, className);
        this.type = type;
        this.superClass = superClass;
    }

    @Override
    Type type() {
        return type;
    }

    @Override
    String superClass() {
        return superClass;
    }

    @Override
    JavaSourceBuilder classEnd() {
        if (superClass == null) {
            emitAllocatePointerMethods();
        }
        return super.classEnd();
    }

    void emitAllocatePointerMethods() {
        // allocatePointer
        builder.incrAlign();
        builder.indent();
        builder.append(PUB_MODS);
        builder.append(" MemorySegment allocatePointer() {\n");
        builder.incrAlign();
        builder.indent();
        builder.append("return MemorySegment.allocateNative(C_POINTER);\n");
        builder.decrAlign();
        builder.indent();
        builder.append("}\n");
        builder.decrAlign();

        // allocatePointer (scope version)
        builder.incrAlign();
        builder.indent();
        builder.append(PUB_MODS);
        builder.append(" MemorySegment allocatePointer(NativeScope scope) {\n");
        builder.incrAlign();
        builder.indent();
        builder.append("return scope.allocate(C_POINTER);\n");
        builder.decrAlign();
        builder.indent();
        builder.append("}\n");
        builder.decrAlign();
    }
}
