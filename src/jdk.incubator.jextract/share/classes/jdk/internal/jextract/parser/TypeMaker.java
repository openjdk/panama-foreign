/*
 * Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
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

package jdk.internal.jextract.parser;


import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import jdk.incubator.jextract.Declaration;
import jdk.incubator.jextract.Type;
import jdk.incubator.jextract.Type.Delegated;
import jdk.incubator.jextract.Type.Primitive;

public class TypeMaker {

    TreeMaker treeMaker;
    private final Map<jdk.internal.jextract.clang.Type, Type> typeCache = new HashMap<>();
    private List<ClangTypeReference> unresolved = new ArrayList<>();

    private class ClangTypeReference implements Supplier<Type> {
        jdk.internal.jextract.clang.Type origin;
        Type derived;

        private ClangTypeReference(jdk.internal.jextract.clang.Type origin) {
            this.origin = origin;
            derived = typeCache.get(origin);
        }

        public boolean isUnresolved() {
            return null == derived;
        }

        public void resolve() {
            derived = makeType(origin);
            Objects.requireNonNull(derived, "Clang type cannot be resolved: " + origin.spelling());
        }

        public Type get() {
            Objects.requireNonNull(derived, "Type is not yet resolved.");
            return derived;
        }
    }

    private ClangTypeReference reference(jdk.internal.jextract.clang.Type type) {
        ClangTypeReference ref = new ClangTypeReference(type);
        if (ref.isUnresolved()) {
            unresolved.add(ref);
        }
        return ref;
    }

    public TypeMaker(TreeMaker treeMaker) {
        this.treeMaker = treeMaker;
    }

    /**
     * Resolve all type references. This method should be called before discard clang cursors/types
     */
    void resolveTypeReferences() {
        List<ClangTypeReference> resolving = unresolved;
        unresolved = new ArrayList<>();
        while (! resolving.isEmpty()) {
            resolving.forEach(ClangTypeReference::resolve);
            resolving = unresolved;
            unresolved = new ArrayList<>();
        }
    }

    Type makeType(jdk.internal.jextract.clang.Type t) {
        Type rv = typeCache.get(t);
        if (rv != null) {
            return rv;
        }
        rv = makeTypeInternal(t);
        if (null != rv && typeCache.put(t, rv) != null) {
            throw new ConcurrentModificationException();
        }
        return rv;
    }

    // TODO: this is used by Layouts
    public static class TypeException extends RuntimeException {
        static final long serialVersionUID = 1L;

        public TypeException(String msg) {
            super(msg);
        }
    }

    Type makeTypeInternal(jdk.internal.jextract.clang.Type t) {
        switch(t.kind()) {
            case Auto:
                return makeType(t.canonicalType());
            case Void:
                return Type.void_();
            case Char_S:
            case Char_U:
                return Type.primitive(Primitive.Kind.Char);
            case Short:
                return Type.primitive(Primitive.Kind.Short);
            case Int:
                return Type.primitive(Primitive.Kind.Int);
            case Long:
                return Type.primitive(Primitive.Kind.Long);
            case LongLong:
                return Type.primitive(Primitive.Kind.LongLong);
            case SChar: {
                Type chType = Type.primitive(Primitive.Kind.Char);
                return Type.qualified(Delegated.Kind.SIGNED, chType);
            }
            case UShort: {
                Type chType = Type.primitive(Primitive.Kind.Short);
                return Type.qualified(Delegated.Kind.UNSIGNED, chType);
            }
            case UInt: {
                Type chType = Type.primitive(Primitive.Kind.Int);
                return Type.qualified(Delegated.Kind.UNSIGNED, chType);
            }
            case ULong: {
                Type chType = Type.primitive(Primitive.Kind.Long);
                return Type.qualified(Delegated.Kind.UNSIGNED, chType);
            }
            case ULongLong: {
                Type chType = Type.primitive(Primitive.Kind.LongLong);
                return Type.qualified(Delegated.Kind.UNSIGNED, chType);
            }
            case UChar: {
                Type chType = Type.primitive(Primitive.Kind.Char);
                return Type.qualified(Delegated.Kind.UNSIGNED, chType);
            }

            case Bool:
                return Type.primitive(Primitive.Kind.Bool);
            case Double:
                return Type.primitive(Primitive.Kind.Double);
            case Float:
                return Type.primitive(Primitive.Kind.Float);
            case Unexposed:
            case Elaborated:
                jdk.internal.jextract.clang.Type canonical = t.canonicalType();
                if (canonical.equalType(t)) {
                    throw new TypeException("Unknown type with same canonical type: " + t.spelling());
                }
                return makeType(canonical);
            case ConstantArray: {
                Type elem = makeType(t.getElementType());
                return Type.array(t.getNumberOfElements(), elem);
            }
            case IncompleteArray: {
                Type elem = makeType(t.getElementType());
                return Type.array(elem);
            }
            case FunctionProto:
            case FunctionNoProto: {
                List<Type> args = new ArrayList<>();
                for (int i = 0; i < t.numberOfArgs(); i++) {
                    // argument could be function pointer declared locally
                    args.add(lowerFunctionType(t.argType(i)));
                }
                return Type.function(t.isVariadic(), lowerFunctionType(t.resultType()), args.toArray(new Type[0]));
            }
            case Enum:
            case Record: {
                if (treeMaker == null) {
                    // Macro evaluation, type is meaningless as this can only be pointer and we only care value
                    return Type.void_();
                }
                return Type.declared((Declaration.Scoped) treeMaker.createTree(t.getDeclarationCursor()));
            }
            case BlockPointer:
            case Pointer: {
                // TODO: We can always erase type for macro evaluation, should we?
                return new TypeImpl.PointerImpl(reference(t.getPointeeType()));
            }
            case Typedef: {
                Type __type = makeType(t.canonicalType());
                return Type.typedef(t.spelling(), __type);
            }
            case Complex: {
                Type __type = makeType(t.getElementType());
                return Type.qualified(Delegated.Kind.COMPLEX, __type);
            }
            case Vector: {
                Type __type = makeType(t.getElementType());
                return Type.vector(t.getNumberOfElements(), __type);
            }
            case WChar: //unsupported
                return Type.primitive(Primitive.Kind.WChar);
            case Char16: //unsupported
                return Type.primitive(Primitive.Kind.Char16);
            case Half: //unsupported
                return Type.primitive(Primitive.Kind.HalfFloat);
            case Int128: //unsupported
                return Type.primitive(Primitive.Kind.Int128);
            case LongDouble: //unsupported
                return Type.primitive(Primitive.Kind.LongDouble);
            case UInt128: { //unsupported
                Type iType = Type.primitive(Primitive.Kind.Int128);
                return Type.qualified(Delegated.Kind.UNSIGNED, iType);
            }
            default:
                return TypeImpl.ERROR;
        }
    }

    private Type lowerFunctionType(jdk.internal.jextract.clang.Type t) {
        Type t2 = makeType(t);
        return t2.accept(lowerFunctionType, null);
    }

    private Type.Visitor<Type, Void> lowerFunctionType = new Type.Visitor<>() {
        @Override
        public Type visitArray(Type.Array t, Void aVoid) {
            return Type.pointer(t.elementType());
        }

        @Override
        public Type visitDelegated(Type.Delegated t, Void aVoid) {
            if (t.kind() == Delegated.Kind.TYPEDEF && t.type() instanceof Type.Array) {
                return visitArray((Type.Array)t.type(), aVoid);
            }
            return visitType(t, aVoid);
        }

        @Override
        public Type visitType(Type t, Void aVoid) {
            return t;
        }
    };
}
