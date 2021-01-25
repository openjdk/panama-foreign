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

import java.lang.constant.Constable;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.jextract.Declaration;
import jdk.incubator.jextract.Position;
import jdk.incubator.jextract.Type;
import jdk.internal.clang.Cursor;
import jdk.internal.clang.CursorKind;
import jdk.internal.clang.SourceLocation;

class TreeMaker {
    public TreeMaker() {}

    TypeMaker typeMaker = new TypeMaker(this);

    public void freeze() {
        typeMaker.resolveTypeReferences();
    }

    interface ScopedFactoryLayout {
        Declaration.Scoped make(Position pos, String name, MemoryLayout layout, Declaration... decls);
    }

    interface ScopedFactoryNoLayout {
        Declaration.Scoped make(Position pos, String name, Declaration... decls);
    }

    interface VarFactoryNoLayout {
        Declaration.Variable make(Position pos, String name, Type type);
    }

    Map<String, List<Constable>> collectAttributes(Cursor c) {
        return c.children().filter(Cursor::isAttribute)
                .collect(Collectors.groupingBy(
                        attr -> attr.kind().name(),
                        Collectors.mapping(Cursor::spelling, Collectors.toList())
                ));
    }

    public Declaration createTree(Cursor c) {
        Objects.requireNonNull(c);
        var rv = (DeclarationImpl) createTreeInternal(c);
        return (rv == null) ? null : rv.withAttributes(collectAttributes(c));
    }

    private Declaration createTreeInternal(Cursor c) {
        switch (c.kind()) {
            case EnumDecl:
                return createScoped(c, Declaration.Scoped.Kind.ENUM, Declaration::enum_, Declaration::enum_);
            case EnumConstantDecl:
                return createEnumConstant(c);
            case FieldDecl:
                return createVar(c.isBitField() ?
                        Declaration.Variable.Kind.BITFIELD : Declaration.Variable.Kind.FIELD, c, Declaration::field);
            case ParmDecl:
                return createVar(Declaration.Variable.Kind.PARAMETER, c, Declaration::parameter);
            case FunctionDecl:
                return createFunction(c);
            case StructDecl:
                return createScoped(c, Declaration.Scoped.Kind.STRUCT, Declaration::struct, Declaration::struct);
            case UnionDecl:
                return createScoped(c, Declaration.Scoped.Kind.UNION, Declaration::union, Declaration::union);
            case TypedefDecl: {
                return createTypedef(c);
            }
            case VarDecl:
                return createVar(Declaration.Variable.Kind.GLOBAL, c, Declaration::globalVariable);
            default:
                return null;
        }
    }

    Position toPos(Cursor cursor) {
        SourceLocation loc = cursor.getSourceLocation();
        if (loc == null) {
            return Position.NO_POSITION;
        }
        SourceLocation.Location sloc = loc.getFileLocation();
        if (sloc == null) {
            return Position.NO_POSITION;
        }
        return new CursorPosition(cursor);
    }

    static class CursorPosition implements Position {
        private final Cursor cursor;
        private final Path path;
        private final int line;
        private final int column;

        CursorPosition(Cursor cursor) {
            this.cursor = cursor;
            SourceLocation.Location loc = cursor.getSourceLocation().getFileLocation();
            this.path = loc.path();
            this.line = loc.line();
            this.column = loc.column();
        }

        @Override
        public Path path() {
            return path;
        }

        @Override
        public int line() {
            return line;
        }

        @Override
        public int col() {
            return column;
        }

        public Cursor cursor() {
            return cursor;
        }

        @Override
        public String toString() {
            return PrettyPrinter.position(this);
        }
    }

    public Declaration.Function createFunction(Cursor c) {
        checkCursor(c, CursorKind.FunctionDecl);
        List<Declaration.Variable> params = new ArrayList<>();
        for (int i = 0 ; i < c.numberOfArgs() ; i++) {
            params.add((Declaration.Variable)createTree(c.getArgument(i)));
        }
        Type type = toType(c);
        Type funcType = type instanceof Type.Delegated? ((Type.Delegated)type).type() : type;
        return Declaration.function(toPos(c), c.spelling(), (Type.Function)funcType,
                params.toArray(new Declaration.Variable[0]));
    }

    public Declaration.Constant createMacro(Cursor c, String name, Type type, Object value) {
        checkCursorAny(c, CursorKind.MacroDefinition);
        return Declaration.constant(toPos(c), name, value, type);
    }

    public Declaration.Constant createEnumConstant(Cursor c) {
        return Declaration.constant(toPos(c), c.spelling(), c.getEnumConstantValue(), typeMaker.makeType(c.type()));
    }

    public Declaration.Scoped createHeader(Cursor c, List<Declaration> decls) {
        return Declaration.toplevel(toPos(c), filterNestedDeclarations(decls).toArray(new Declaration[0]));
    }

    public Declaration.Scoped createScoped(Cursor c, Declaration.Scoped.Kind scopeKind, ScopedFactoryLayout factoryLayout, ScopedFactoryNoLayout factoryNoLayout) {
        List<Declaration> decls = filterNestedDeclarations(c.children()
                .map(this::createTree).collect(Collectors.toList()));
        if (c.isDefinition()) {
            //just a declaration AND definition, we have a layout
            MemoryLayout layout = null;
            try {
                layout = LayoutUtils.getLayout(c.type());
            } catch (TypeMaker.TypeException ex) {
                System.err.println(ex);
                System.err.println("WARNING: generating empty struct: " + c.spelling());
                return factoryNoLayout.make(toPos(c), c.spelling(), decls.toArray(new Declaration[0]));
            }
            List<Declaration> adaptedDecls = layout instanceof GroupLayout ?
                    collectBitfields(layout, decls) :
                    decls;
            return factoryLayout.make(toPos(c), c.spelling(), layout, adaptedDecls.toArray(new Declaration[0]));
        } else {
            //just a declaration
            if (scopeKind == Declaration.Scoped.Kind.STRUCT ||
                    scopeKind == Declaration.Scoped.Kind.UNION ||
                    scopeKind == Declaration.Scoped.Kind.ENUM ||
                    scopeKind == Declaration.Scoped.Kind.CLASS) {
                //if there's a real definition somewhere else, skip this redundant declaration
                if (!c.getDefinition().isInvalid()) {
                    return null;
                }
            }
            return factoryNoLayout.make(toPos(c), c.spelling(), decls.toArray(new Declaration[0]));
        }
    }

    private static boolean isEnum(Declaration d) {
        return d instanceof Declaration.Scoped && ((Declaration.Scoped)d).kind() == Declaration.Scoped.Kind.ENUM;
    }

    private static boolean isAnonymousStruct(Declaration declaration) {
        return ((CursorPosition)declaration.pos()).cursor.isAnonymousStruct();
    }

    private List<Declaration> filterNestedDeclarations(List<Declaration> declarations) {
        return declarations.stream()
                .filter(Objects::nonNull)
                .filter(d -> isEnum(d) || !d.name().isEmpty() || isAnonymousStruct(d))
                .collect(Collectors.toList());
    }

    private Declaration.Typedef createTypedef(Cursor c) {
        Type cursorType = toType(c);
        Type canonicalType = cursorType instanceof Type.Function
            ? cursorType
            : ((Type.Delegated) cursorType).type(); // normal typedef
        if (canonicalType instanceof Type.Declared) {
            Declaration.Scoped s = ((Type.Declared) canonicalType).tree();
            if (s.name().equals(c.spelling())) {
                // typedef record with the same name, no need to present twice
                return null;
            }
        }
        return Declaration.typedef(toPos(c), c.spelling(), canonicalType);
    }

    private Declaration.Variable createVar(Declaration.Variable.Kind kind, Cursor c, VarFactoryNoLayout varFactory) {
        checkCursorAny(c, CursorKind.VarDecl, CursorKind.FieldDecl, CursorKind.ParmDecl);
        if (c.isBitField()) {
            return Declaration.bitfield(toPos(c), c.spelling(), toType(c),
                    MemoryLayout.ofValueBits(c.getBitFieldWidth(), ByteOrder.nativeOrder()));
        } else {
            Type type = null;
            try {
                type = toType(c);
            } catch (TypeMaker.TypeException ex) {
                System.err.println(ex);
                System.err.println("WARNING: ignoring variable: " + c.spelling());
                return null;
            }
            return varFactory.make(toPos(c), c.spelling(), type);
        }
    }

    private static void collectNestedBitFields(Set<Declaration> out, Declaration.Scoped anonymousStruct) {
        for  (Declaration field : anonymousStruct.members()) {
            if (isAnonymousStruct(field)) {
                collectNestedBitFields(out, (Declaration.Scoped) field);
            } else if (field instanceof Declaration.Scoped
                       && ((Declaration.Scoped) field).kind() == Declaration.Scoped.Kind.BITFIELDS) {
                out.addAll(((Declaration.Scoped) field).members());
            }
        }
    }

    private static Set<Declaration> nestedBitFields(List<Declaration> members) {
        Set<Declaration> res = new HashSet<>();
        for (Declaration member : members) {
            if (isAnonymousStruct(member)) {
                collectNestedBitFields(res, (Declaration.Scoped) member);
            }
        }
        return res;
    }

    private List<Declaration> collectBitfields(MemoryLayout layout, List<Declaration> declarations) {
        Set<String> nestedBitfieldNames = nestedBitFields(declarations).stream()
                                                                       .map(Declaration::name)
                                                                       .collect(Collectors.toSet());
        List<Declaration> newDecls = new ArrayList<>();
        for (MemoryLayout e : ((GroupLayout)layout).memberLayouts()) {
            Optional<GroupLayout> contents = Utils.getContents(e);
            if (contents.isPresent()) {
                List<Declaration.Variable> bfDecls = new ArrayList<>();
                outer: for (MemoryLayout bitfield : contents.get().memberLayouts()) {
                    if (bitfield.name().isPresent() && !nestedBitfieldNames.contains(bitfield.name().get())) {
                        Iterator<Declaration> declIt = declarations.iterator();
                        while (declIt.hasNext()) {
                            Declaration d = declIt.next();
                            if (d.name().equals(bitfield.name().get())) {
                                bfDecls.add((Declaration.Variable)d);
                                declIt.remove();
                                continue outer;
                            }
                        }
                        throw new IllegalStateException("No matching declaration found for bitfield: " + bitfield);
                    }
                }
                if (!bfDecls.isEmpty()) {
                    newDecls.add(Declaration.bitfields(bfDecls.get(0).pos(), "", contents.get(), bfDecls.toArray(new Declaration.Variable[0])));
                }
            }
        }
        newDecls.addAll(declarations);
        return newDecls;
    }

    private Type toType(Cursor c) {
        return typeMaker.makeType(c.type());
    }

    private void checkCursor(Cursor c, CursorKind k) {
        if (c.kind() != k) {
            throw new IllegalArgumentException("Invalid cursor kind");
        }
    }

    private void checkCursorAny(Cursor c, CursorKind... kinds) {
        CursorKind expected = Objects.requireNonNull(c.kind());
        for (CursorKind k : kinds) {
            if (k == expected) {
                return;
            }
        }
        throw new IllegalArgumentException("Invalid cursor kind");
    }
}
