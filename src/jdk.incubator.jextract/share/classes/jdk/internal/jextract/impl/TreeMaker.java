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

import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
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
    private final Map<Cursor, Declaration> treeCache = new HashMap<>();

    public TreeMaker() {}

    TypeMaker typeMaker = new TypeMaker(this);

    private <T extends Declaration> T checkCache(Cursor c, Class<T> clazz, Supplier<Declaration> factory) {
        // The supplier function may lead to adding some other type, which will cause CME using computeIfAbsent.
        // This implementation relax the constraint a bit only check for same key
        Declaration rv;
        if (treeCache.containsKey(c)) {
            rv = treeCache.get(c);
        } else {
            rv = factory.get();
            if (null != rv && treeCache.put(c, rv) != null) {
                throw new ConcurrentModificationException();
            }
        }
        return clazz.cast(rv);
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

    public Declaration createTree(Cursor c) {
        switch (Objects.requireNonNull(c).kind()) {
            case EnumDecl:
                return createScoped(c, Declaration.Scoped.Kind.ENUM, Declaration::enum_, Declaration::enum_);
            case EnumConstantDecl:
                return createEnumConstant(c);
            case FieldDecl:
                return createVar(c.isBitField() ?
                        Declaration.Variable.Kind.BITFIELD : Declaration.Variable.Kind.FIELD, c, Declaration::field);
            case ParamDecl:
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

        CursorPosition(Cursor cursor) {
            this.cursor = cursor;
        }

        @Override
        public Path path() {
            return cursor.getSourceLocation().getFileLocation().path();
        }

        @Override
        public int line() {
            return cursor.getSourceLocation().getFileLocation().line();
        }

        @Override
        public int col() {
            return cursor.getSourceLocation().getFileLocation().column();
        }

        public Cursor cursor() {
            return cursor;
        }
    }

    public Declaration.Function createFunction(Cursor c) {
        checkCursor(c, CursorKind.FunctionDecl);
        List<Declaration.Variable> params = new ArrayList<>();
        for (int i = 0 ; i < c.numberOfArgs() ; i++) {
            params.add((Declaration.Variable)createTree(c.getArgument(i)));
        }
        return checkCache(c, Declaration.Function.class,
                ()->Declaration.function(toPos(c), c.spelling(), (Type.Function)toType(c), params.toArray(new Declaration.Variable[0])));
    }

    public Declaration.Constant createMacro(Cursor c, Optional<MacroParserImpl.Macro> macro) {
        checkCursorAny(c, CursorKind.MacroDefinition);
        if (macro.isEmpty()) {
            return null;
        } else {
            MacroParserImpl.Macro parsedMacro = macro.get();
            return checkCache(c, Declaration.Constant.class,
                    ()->Declaration.constant(toPos(c), c.spelling(), parsedMacro.value, parsedMacro.type()));
        }
    }

    public Declaration.Constant createEnumConstant(Cursor c) {
        return checkCache(c, Declaration.Constant.class,
                ()->Declaration.constant(toPos(c), c.spelling(), c.getEnumConstantValue(), typeMaker.makeType(c.type())));
    }

    public Declaration.Scoped createHeader(Cursor c, List<Declaration> decls) {
        return checkCache(c, Declaration.Scoped.class,
                ()->Declaration.toplevel(toPos(c), filterNestedDeclarations(decls).toArray(new Declaration[0])));
    }

    public Declaration.Scoped createScoped(Cursor c, Declaration.Scoped.Kind scopeKind, ScopedFactoryLayout factoryLayout, ScopedFactoryNoLayout factoryNoLayout) {
        List<Declaration> decls = filterNestedDeclarations(c.children()
                .map(this::createTree).collect(Collectors.toList()));
        return checkCache(c, Declaration.Scoped.class, () -> {
            if (c.isDefinition()) {
                //just a declaration AND definition, we have a layout
                MemoryLayout layout = LayoutUtils.getLayout(c.type());
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
        });
    }

    private List<Declaration> filterNestedDeclarations(List<Declaration> declarations) {
        return declarations.stream()
                .filter(Objects::nonNull)
                .filter(d -> !d.name().isEmpty() || ((CursorPosition)d.pos()).cursor.isAnonymousStruct())
                .collect(Collectors.toList());
    }

    private Declaration.Scoped createTypedef(Cursor c) {
        Optional<Cursor> decl = c.children().findFirst();
        if (decl.isPresent() && decl.get().isDefinition() && decl.get().spelling().isEmpty()) {
            Declaration def = createTree(decl.get());
            if (def instanceof Declaration.Scoped) {
                return checkCache(c, Declaration.Scoped.class,
                        () -> Declaration.typedef(toPos(c), c.spelling(), def));
            }
        }
        return null;
    }

    private Declaration.Variable createVar(Declaration.Variable.Kind kind, Cursor c, VarFactoryNoLayout varFactory) {
        checkCursorAny(c, CursorKind.VarDecl, CursorKind.FieldDecl, CursorKind.ParamDecl);
        if (c.isBitField()) {
            return checkCache(c, Declaration.Variable.class,
                    () -> Declaration.bitfield(toPos(c), c.spelling(), toType(c),
                    MemoryLayout.ofValueBits(c.getBitFieldWidth(), ByteOrder.nativeOrder())));
        } else {
            return checkCache(c, Declaration.Variable.class,
                    ()->varFactory.make(toPos(c), c.spelling(), toType(c)));
        }
    }

    private List<Declaration> collectBitfields(MemoryLayout layout, List<Declaration> declarations) {
        List<Declaration> newDecls = new ArrayList<>();
        for (MemoryLayout e : ((GroupLayout)layout).memberLayouts()) {
            Optional<GroupLayout> contents = Utils.getContents(e);
            if (contents.isPresent()) {
                List<Declaration.Variable> bfDecls = new ArrayList<>();
                outer: for (MemoryLayout bitfield : contents.get().memberLayouts()) {
                    if (bitfield.name().isPresent()) {
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
                newDecls.add(Declaration.bitfields(bfDecls.get(0).pos(), "", contents.get(), bfDecls.toArray(new Declaration.Variable[0])));
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
