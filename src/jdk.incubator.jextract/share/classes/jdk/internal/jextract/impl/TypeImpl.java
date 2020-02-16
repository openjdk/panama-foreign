/*
 *  Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.  Oracle designates this
 *  particular file as subject to the "Classpath" exception as provided
 *  by Oracle in the LICENSE file that accompanied this code.
 *
 *  This code is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  version 2 for more details (a copy is included in the LICENSE file that
 *  accompanied this code).
 *
 *  You should have received a copy of the GNU General Public License version
 *  2 along with this work; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *   Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 *
 */

package jdk.internal.jextract.impl;

import java.util.Objects;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.jextract.Declaration;
import jdk.incubator.jextract.Type;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Supplier;

public abstract class TypeImpl implements Type {

    @Override
    public boolean isErroneous() {
        return false;
    }

    public static final TypeImpl ERROR = new TypeImpl() {
        @Override
        public <R, D> R accept(Visitor<R, D> visitor, D data) {
            return visitor.visitType(this, data);
        }

        @Override
        public boolean isErroneous() {
            return true;
        }
    };

    public static class PrimitiveImpl extends TypeImpl implements Type.Primitive {

        private final Primitive.Kind kind;
        private final Optional<MemoryLayout> layoutOpt;

        public PrimitiveImpl(Kind kind, MemoryLayout layout) {
            this(kind, Optional.of(layout));
        }

        public PrimitiveImpl(Kind kind) {
            this(kind, Optional.empty());
        }

        private PrimitiveImpl(Kind kind, Optional<MemoryLayout> layoutOpt) {
            super();
            this.kind = kind;
            this.layoutOpt = layoutOpt;
        }

        @Override
        public <R, D> R accept(Visitor<R, D> visitor, D data) {
            return visitor.visitPrimitive(this, data);
        }

        @Override
        public Kind kind() {
            return kind;
        }

        @Override
        public Optional<MemoryLayout> layout() {
            return layoutOpt;
        }
    }

    static abstract class DelegatedBase extends TypeImpl implements Type.Delegated {
        Delegated.Kind kind;
        Optional<String> name;

        DelegatedBase(Kind kind, Optional<String> name) {
            this.kind = kind;
            this.name = name;
        }

        @Override
        public <R, D> R accept(Visitor<R, D> visitor, D data) {
            return visitor.visitDelegated(this, data);
        }

        @Override
        public Delegated.Kind kind() {
            return kind;
        }

        @Override
        public Optional<String> name() {
            return name;
        }
    }

    public static class QualifiedImpl extends DelegatedBase {
        private final Type type;

        public QualifiedImpl(Kind kind, Type type) {
            this(kind, Optional.empty(), type);
        }

        public QualifiedImpl(Kind kind, String name, Type type) {
            this(kind, Optional.of(name), type);
        }

        private QualifiedImpl(Kind kind, Optional<String> name, Type type) {
            super(kind, name);
            this.type = type;
        }

        @Override
        public Type type() {
            return type;
        }
    }

    public static class PointerImpl extends DelegatedBase {
        private final Supplier<Type> pointeeFactory;
        private Type pointee;

        public PointerImpl(Supplier<Type> pointeeFactory) {
            super(Kind.POINTER, Optional.empty());
            this.pointeeFactory = pointeeFactory;
            this.pointee = null;
        }

        public PointerImpl(Type pointee) {
            super(Kind.POINTER, Optional.empty());
            pointeeFactory = null;
            this.pointee = pointee;
        }

        @Override
        public Type type() {
            if (pointee == null && pointeeFactory != null) {
                pointee =pointeeFactory.get();
                Objects.requireNonNull(pointee);
            }
            return pointee;
        }
    }

    public static class DeclaredImpl extends TypeImpl implements Type.Declared {

        private final Declaration.Scoped declaration;

        public DeclaredImpl(Declaration.Scoped declaration) {
            super();
            this.declaration = declaration;
        }

        @Override
        public <R, D> R accept(Visitor<R, D> visitor, D data) {
            return visitor.visitDeclared(this, data);
        }

        @Override
        public Declaration.Scoped tree() {
            return declaration;
        }
    }

    public static class FunctionImpl extends TypeImpl implements Type.Function {

        private final boolean varargs;
        private final List<Type> argtypes;
        private final Type restype;

        public FunctionImpl(boolean varargs, List<Type> argtypes, Type restype) {
            super();
            this.varargs = varargs;
            this.argtypes = argtypes;
            this.restype = restype;
        }

        @Override
        public <R, D> R accept(Visitor<R, D> visitor, D data) {
            return visitor.visitFunction(this, data);
        }

        @Override
        public boolean varargs() {
            return varargs;
        }

        @Override
        public List<Type> argumentTypes() {
            return argtypes;
        }

        @Override
        public Type returnType() {
            return restype;
        }
    }

    public static class ArrayImpl extends TypeImpl implements Type.Array {

        private final Kind kind;
        private final OptionalLong elemCount;
        private final Type elemType;

        public ArrayImpl(Kind kind, long count, Type elemType) {
            this(kind, elemType, OptionalLong.of(count));
        }

        public ArrayImpl(Kind kind, Type elemType) {
            this(kind, elemType, OptionalLong.empty());
        }

        private ArrayImpl(Kind kind, Type elemType, OptionalLong elemCount) {
            super();
            this.kind = kind;
            this.elemCount = elemCount;
            this.elemType = elemType;
        }

        @Override
        public <R, D> R accept(Visitor<R, D> visitor, D data) {
            return visitor.visitArray(this, data);
        }

        @Override
        public OptionalLong elementCount() {
            return elemCount;
        }

        @Override
        public Type elementType() {
            return elemType;
        }

        @Override
        public Kind kind() {
            return kind;
        }
    }

    @Override
    public String toString() {
        return PrettyPrinter.type(this);
    }
}
