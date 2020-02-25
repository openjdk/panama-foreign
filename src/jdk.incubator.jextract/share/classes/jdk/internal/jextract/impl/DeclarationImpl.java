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

import java.lang.constant.ConstantDesc;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.jextract.Declaration;
import jdk.incubator.jextract.Position;
import jdk.incubator.jextract.Type;

public abstract class DeclarationImpl implements Declaration {

    private final String name;
    private final Position pos;
    private final Map<String, List<ConstantDesc>> attributes;

    public DeclarationImpl(String name, Position pos, Map<String, List<ConstantDesc>> attrs) {
        this.name = name;
        this.pos = pos;
        this.attributes = attrs;
    }

    public String toString() {
        return new PrettyPrinter().print(this);
    }

    public String name() {
        return name;
    }

    @Override
    public Position pos() {
        return pos;
    }

    @Override
    public Optional<List<ConstantDesc>> getAttribute(String name) {
        return Optional.ofNullable(attributes.get(name));
    }

    @Override
    public Set<String> availableAttributes() { return Collections.unmodifiableSet(attributes.keySet()); }

    public static class VariableImpl extends DeclarationImpl implements Declaration.Variable {

        final Variable.Kind kind;
        final Type type;
        final Optional<MemoryLayout> layout;

        public VariableImpl(Type type, Variable.Kind kind, String name, Position pos, Map<String, List<ConstantDesc>> attrs) {
            this(type, LayoutUtils.getLayout(type), kind, name, pos, attrs);
        }

        public VariableImpl(Type type, MemoryLayout layout, Variable.Kind kind, String name, Position pos, Map<String, List<ConstantDesc>> attrs) {
            this(type, Optional.of(layout), kind, name, pos, attrs);
        }

        private VariableImpl(Type type, Optional<MemoryLayout> layout, Variable.Kind kind, String name, Position pos, Map<String, List<ConstantDesc>> attrs) {
            super(name, pos, attrs);
            this.kind = kind;
            this.type = type;
            this.layout = layout;
        }

        @Override
        public Kind kind() {
            return kind;
        }

        @Override
        public <R, D> R accept(Visitor<R, D> visitor, D data) {
            return visitor.visitVariable(this, data);
        }

        @Override
        public Type type() {
            return type;
        }

        @Override
        public Optional<MemoryLayout> layout() {
            return layout;
        }
    }

    public static class FunctionImpl extends DeclarationImpl implements Declaration.Function {

        final List<Variable> params;
        final Type.Function type;

        public FunctionImpl(Type.Function type, List<Variable> params, String name, Position pos, Map<String, List<ConstantDesc>> attrs) {
            super(name, pos, attrs);
            this.params = params;
            this.type = type;
        }

        @Override
        public <R, D> R accept(Visitor<R, D> visitor, D data) {
            return visitor.visitFunction(this, data);
        }

        @Override
        public List<Variable> parameters() {
            return params;
        }

        @Override
        public Type.Function type() {
            return type;
        }
    }

    public static class ScopedImpl extends DeclarationImpl implements Declaration.Scoped {

        private final Scoped.Kind kind;
        private final List<Declaration> declarations;
        private final Optional<MemoryLayout> optLayout;

        public ScopedImpl(Kind kind, MemoryLayout layout, List<Declaration> declarations, String name, Position pos) {
            this(kind, Optional.of(layout), declarations, name, pos);
        }

        public ScopedImpl(Kind kind, List<Declaration> declarations, String name, Position pos) {
            this(kind, Optional.empty(), declarations, name, pos);
        }

        ScopedImpl(Kind kind, Optional<MemoryLayout> optLayout, List<Declaration> declarations, String name, Position pos) {
            super(name, pos, Collections.emptyMap());
            this.kind = kind;
            this.declarations = declarations;
            this.optLayout = optLayout;
        }

        @Override
        public <R, D> R accept(Visitor<R, D> visitor, D data) {
            return visitor.visitScoped(this, data);
        }

        @Override
        public List<Declaration> members() {
            return declarations;
        }

        @Override
        public Optional<MemoryLayout> layout() {
            return optLayout;
        }

        @Override
        public Kind kind() {
            return kind;
        }
    }

    public static class ConstantImpl extends DeclarationImpl implements Declaration.Constant {

        final Object value;
        final Type type;

        public ConstantImpl(Type type, Object value, String name, Position pos) {
            this(type, value, name, pos, Collections.emptyMap());
        }

        public ConstantImpl(Type type, Object value, String name, Position pos, Map<String, List<ConstantDesc>> attrs) {
            super(name, pos, attrs);
            this.value = value;
            this.type = type;
        }

        @Override
        public <R, D> R accept(Visitor<R, D> visitor, D data) {
            return visitor.visitConstant(this, data);
        }

        @Override
        public Object value() {
            return value;
        }

        @Override
        public Type type() {
            return type;
        }
    }
}
