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
package com.sun.tools.jextract.tree;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import jdk.internal.clang.Cursor;
import jdk.internal.clang.CursorKind;
import jdk.internal.clang.Type;

public class TreeMaker {
    private final Map<Cursor, Tree> treeCache = new HashMap<>();

    public TreeMaker() {}

    private <T extends Tree> T checkCache(Cursor c, Class<T> clazz, Supplier<Tree> factory) {
        return clazz.cast(treeCache.computeIfAbsent(c, cx->factory.get()));
    }

    public Tree createTree(Cursor c) {
        switch (Objects.requireNonNull(c).kind()) {
            case EnumDecl:
                return createEnum(c);
            case EnumConstantDecl:
            case FieldDecl:
                return createField(c);
            case FunctionDecl:
                return createFunction(c);
            case TypedefDecl:
                return createTypedef(c);
            case StructDecl:
            case UnionDecl:
                return createStruct(c);
            case VarDecl:
                return createVar(c);
            default:
                return checkCache(c, Tree.class, ()->new Tree(c));
        }
    }

    private static Stream<Cursor> enumConstants(Cursor c) {
        return c.children().filter(cx -> cx.kind() == CursorKind.EnumConstantDecl);
    }

    public EnumTree createEnum(Cursor c) {
        checkCursor(c, CursorKind.EnumDecl);
        List<FieldTree> consts = new ArrayList<>();
        enumConstants(c).forEachOrdered(cx -> consts.add((FieldTree)createTree(cx)));
        return createEnumCommon(c, consts);
    }

    public EnumTree createEnum(Cursor c, List<FieldTree> fields) {
        checkCursor(c, CursorKind.EnumDecl);
        return createEnumCommon(c, fields);
    }

    private EnumTree createEnumCommon(Cursor c, List<FieldTree> fields) {
        // If the current Cursor is not a definition, get the definition
        // and wrap it only if that is a valid definition.
        Optional<Tree> def = Optional.ofNullable(
            (c.isDefinition() || c.getDefinition().isInvalid())?
            null : createTree(c.getDefinition())
        );
        return checkCache(c, EnumTree.class, ()->new EnumTree(c, def, fields));
    }

    public FieldTree createField(Cursor c) {
        checkCursorAny(c, CursorKind.EnumConstantDecl, CursorKind.FieldDecl);
        return checkCache(c, FieldTree.class, ()->new FieldTree(c));
    }

    public FunctionTree createFunction(Cursor c) {
        checkCursorAny(c, CursorKind.FunctionDecl);
        return checkCache(c, FunctionTree.class, ()->new FunctionTree(c));
    }

    public MacroTree createMacro(Cursor c, Optional<Object> value) {
        checkCursorAny(c, CursorKind.MacroDefinition);
        return checkCache(c, MacroTree.class, ()->new MacroTree(c, value));
    }

    public HeaderTree createHeader(Cursor c, Path path, List<Tree> decls) {
        return checkCache(c, HeaderTree.class, ()->new HeaderTree(c, path, decls));
    }

    public StructTree createStruct(Cursor c) {
        checkCursorAny(c, CursorKind.StructDecl, CursorKind.UnionDecl);
        List<Tree> decls = c.children().map(this::createTree).collect(Collectors.toList());
        return createStructCommon(c, decls);
    }

    public StructTree createStruct(Cursor c, List<Tree> declarations) {
        checkCursorAny(c, CursorKind.StructDecl, CursorKind.UnionDecl);
        return createStructCommon(c, declarations);
    }

    private StructTree createStructCommon(Cursor c, List<Tree> declarations) {
        // If the current Cursor is not a definition, get the definition
        // and wrap it only if that is a valid definition.
        Optional<Tree> def = Optional.ofNullable(
            (c.isDefinition() || c.getDefinition().isInvalid())?
            null : createTree(c.getDefinition())
        );
        return checkCache(c, StructTree.class, ()->new StructTree(c, def, declarations));
    }

    public TypedefTree createTypedef(Cursor c) {
        checkCursor(c, CursorKind.TypedefDecl);
        Cursor dcl = c.type().canonicalType().getDeclarationCursor();
        Optional<Tree> def = Optional.ofNullable((dcl.isDefinition() || dcl.isDeclaration()) ? createTree(dcl) : null);
        return checkCache(c, TypedefTree.class, ()->{
            return new TypedefTree(c, def);
        });
    }

    private VarTree createVar(Cursor c) {
        checkCursor(c, CursorKind.VarDecl);
        return checkCache(c, VarTree.class, ()->new VarTree(c));
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
