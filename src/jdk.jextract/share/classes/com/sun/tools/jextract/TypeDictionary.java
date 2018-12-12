/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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

import java.foreign.memory.DoubleComplex;
import java.foreign.memory.FloatComplex;
import java.foreign.memory.LongDoubleComplex;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

import jdk.internal.clang.Type;
import jdk.internal.clang.TypeKind;

/**
 * A dictionary that find Java type for a given native type.
 * Each instance of TypeDictionary presents types for a given java package.
 */
final class TypeDictionary {
    private final Logger logger = Logger.getLogger(getClass().getPackage().getName());
    private Context ctx;
    private final HeaderFile headerFile;
    private final Map<String, JType> functionalTypes;
    private int serialNo;

    TypeDictionary(Context ctx, HeaderFile headerFile) {
        this.ctx = ctx;
        this.headerFile = headerFile;
        functionalTypes = new HashMap<>();
    }
    
    private int serialNo() {
        return ++serialNo;
    }

    private String recordOwnerClass(Type t) {
        try {
            //try resolve globally
            Path p = t.getDeclarationCursor().getSourceLocation().getFileLocation().path();
            HeaderFile hf = ctx.headerFor(p);
            return Utils.toInternalName(hf.pkgName, hf.clsName);
        } catch (Throwable ex) {
            //fallback: resolve locally. This can happen for two reasons: (i) the symbol to be resolved is a builtin
            //symbol (e.g. no header file has its definition), or (ii) when the declaration cursor points to an header file
            //not previously seen by Context.
	        return headerClass();
        }
    }
    
    private String headerClass() {
        return Utils.toInternalName(headerFile.pkgName, headerFile.clsName);
    }

    public Stream<JType> functionalInterfaces() {
        return functionalTypes.entrySet().stream()
                .map(Map.Entry::getValue);
    }

    /**
     * @param t
     * @return
     */
    private JType getInternal(Type t, Function<JType.Function, JType> funcResolver) {
        switch(t.kind()) {
            case Void:
                return JType.Void;
            // signed integer
            case Char_S:
            case SChar:
            case Short:
            case Int:
            case Long:
            case LongLong:
            case Int128:
            // unsigned integer, size-compatible at groveller level
            // accomodate value-range needed at civilizer level
            case UShort:
            case UInt:
            case UInt128:
            case ULong:
            case ULongLong:
            case Char_U:
            case UChar:
                long size = t.size();
                if (size <= 1) {
                    return JType.Byte;
                } else if (size <= 2) {
                    return JType.Short;
                } else if (size <= 4) {
                    return JType.Int;
                } else if (size <= 8) {
                    return JType.Long;
                } else {
                    return JType.of(BigInteger.class);
                }
            case WChar:
            case Char16:
                return JType.Char;
            case Bool:
                return JType.Bool;
            case Double:
            case LongDouble:
                return JType.Double;
            case Float:
                return JType.Float;
            case Unexposed:
            case Elaborated:
                return getInternal(t.canonicalType(), funcResolver);
            case ConstantArray:
            case IncompleteArray:
                return new JType.ArrayType(getInternal(t.getElementType(), funcResolver));
            case FunctionProto:
            case FunctionNoProto:
                JType[] args = new JType[t.numberOfArgs()];
                for (int i = 0; i < args.length; i++) {
                    // argument could be function pointer declared locally
                    args[i] = getInternal(t.argType(i), funcResolver);
                }
                return new JType.Function(Utils.getFunction(t), t.isVariadic(), getInternal(t.resultType(), funcResolver), args);
            case Enum: {
                return JType.Int;
            }
            case Invalid:
                throw new IllegalArgumentException("Invalid type");
            case Record: {
                String name = Utils.toClassName(Utils.getName(t));
                return new JType.ClassType(recordOwnerClass(t) + "$" + name);
            }
            case Pointer: {
                JType jt = getInternal(t.getPointeeType().canonicalType(), funcResolver);
                if (jt instanceof JType.Function) {
                    jt = funcResolver.apply((JType.Function) jt);
                    return JType.GenericType.ofCallback(jt);
                } else {
                    return JType.GenericType.ofPointer(jt);
                }
            }
            case Typedef: {
                Type truetype = t.canonicalType();
                logger.fine(() -> "Typedef " + t.spelling() + " as " + truetype.spelling());
                return getInternal(truetype, funcResolver);
            }
            case BlockPointer:
                // FIXME: what is BlockPointer? A FunctionalPointer as this is closure
                JType jt = getInternal(t.getPointeeType(), funcResolver);
                jt = funcResolver.apply((JType.Function)jt);
                return JType.GenericType.ofCallback(jt);
            case Complex:
                TypeKind ek = t.getElementType().kind();
                if (ek == TypeKind.Float) {
                    return JType.of(FloatComplex.class);
                } else if (ek == TypeKind.Double) {
                    return JType.of(DoubleComplex.class);
                } else if (ek == TypeKind.LongDouble) {
                    return JType.of(LongDoubleComplex.class);
                } else {
                    throw new UnsupportedOperationException("_Complex kind " + ek + " not supported");
                }
            case Vector:
                switch ((int) t.size()) {
                    case 8:
                        return JType.Long;
                    case 16:
                    case 32:
                    case 64:
                    default:
                        throw new UnsupportedOperationException("Support for vector size: " + t.size());
                }
            default:
                throw new IllegalStateException("Unexpected type:" + t.kind());
        }
    }

    public JType enterIfAbsent(Type t) {
        return getInternal(t, this::enterFunctionIfNeeded);
    }

    //where
    private JType enterFunctionIfNeeded(JType.Function f) {
        return functionalTypes.computeIfAbsent(f.getNativeDescriptor(), _unused ->
            new JType.FunctionalInterfaceType(headerClass() +
                    "$FI" + serialNo(), f));
    }

    public JType lookup(Type t) {
        return getInternal(t, this::lookupFunction);
    }

    //where
    private JType lookupFunction(JType.Function f) {
        return Optional.ofNullable(functionalTypes.get(f.getNativeDescriptor()))
                .orElseThrow(IllegalStateException::new);
    }
}
