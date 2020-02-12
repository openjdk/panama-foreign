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

package jdk.incubator.jextract.tool;

import java.lang.invoke.MethodType;
import java.util.List;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.jextract.Declaration;
import jdk.incubator.jextract.Type;

public class StaticWrapperSourceFactory extends HandleSourceFactory {
    public StaticWrapperSourceFactory(String clsName, String pkgName, List<String> libraryNames) {
        super(clsName, pkgName, libraryNames);
    }

    @Override
    protected void generateFunctionalInterfaces(Declaration.Scoped decl) {
        //do nothing
    }

    @Override
    public Void visitFunction(Declaration.Function funcTree, Declaration parent) {
        MethodType mtype = typeTranslator.getMethodType(funcTree.type());
        FunctionDescriptor descriptor = Type.descriptorFor(funcTree.type()).orElse(null);
        if (descriptor == null) {
            //abort
            return null;
        }
        builder.addMethodHandle(funcTree, mtype, descriptor);
        //generate static wrapper for function
        builder.addStaticFunctionWrapper(funcTree, mtype);
        int i = 0;
        for (Declaration.Variable param : funcTree.parameters()) {
            Type.Function f = getAsFunctionPointer(param.type());
            if (f != null) {
                String name = funcTree.name() + "$" + (param.name().isEmpty() ? "x" + i : param.name());
                //add descriptor constant
                builder.addDescriptor(name, Type.descriptorFor(f).get());
                //generate functional interface
                MethodType fitype = typeTranslator.getMethodType(f);
                builder.addFunctionalInterface(name, fitype);
                //generate helper
                builder.addFunctionalFactory(name, fitype);
                i++;
            }
        }
        return null;
    }

    Type.Function getAsFunctionPointer(Type type) {
        if (type instanceof Type.Delegated) {
            switch (((Type.Delegated) type).kind()) {
                case POINTER: {
                    Type pointee = ((Type.Delegated) type).type();
                    return (pointee instanceof Type.Function) ?
                        (Type.Function)pointee : null;
                }
                default:
                    return getAsFunctionPointer(((Type.Delegated) type).type());
            }
        } else {
            return null;
        }
    }

    @Override
    public Void visitVariable(Declaration.Variable tree, Declaration parent) {
        String fieldName = tree.name();
        String symbol = tree.name();
        assert !symbol.isEmpty();
        assert !fieldName.isEmpty();
        Type type = tree.type();
        MemoryLayout layout = tree.layout().orElse(Type.layoutFor(type).orElse(null));
        if (layout == null) {
            //no layout - abort
            return null;
        }
        Class<?> clazz = typeTranslator.getJavaType(type);
        if (clazz == MemoryAddress.class || clazz == MemorySegment.class || layout.byteSize() > 8) {
            //skip
            return null;
        }

        if (parent != null) {
            //struct field
            builder.addVarHandle(fieldName, clazz, parent.name());
        } else {
            builder.addLayout(fieldName, layout);
            builder.addVarHandle(fieldName, clazz, null);
            builder.addAddress(fieldName);
        }
        //add getter and setters
        builder.addGetter(fieldName, clazz, parent);
        builder.addSetter(fieldName, clazz, parent);

        return null;
    }
}
