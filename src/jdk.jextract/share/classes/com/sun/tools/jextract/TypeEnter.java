/*
 *  Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.
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
 *  Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 */
package com.sun.tools.jextract;

import com.sun.tools.jextract.tree.*;

import java.util.*;

/**
 * This visitor enters clang types into given type dictionary. This step is required in order to generate stable
 * names for functional interface types.
 */
final class TypeEnter extends SimpleTreeVisitor<Void, Void>
        implements TreePhase {

    TypeDictionary typeDictionary;

    public TypeEnter(TypeDictionary typeDictionary) {
        this.typeDictionary = typeDictionary;
    }

    @Override
    public HeaderTree transform(HeaderTree ht) {
        // Process all header declarations are collect potential
        // declarations that will go into transformed HeaderTree
        // into the this.decls field.
        ht.accept(this, null);

        return ht;
    }

    @Override
    public Void defaultAction(Tree tree, Void v) {
        return null;
    }

    @Override
    public Void visitHeader(HeaderTree ht, Void v) {
        ht.declarations().forEach(decl -> decl.accept(this, null));
        return null;
    }

    @Override
    public Void visitEnum(EnumTree t, Void aVoid) {
        typeDictionary.enterIfAbsent(t.type());
        t.constants().forEach(c -> c.accept(this, null));
        return null;
    }

    @Override
    public Void visitStruct(StructTree s, Void v) {
        typeDictionary.enterIfAbsent(s.type());
        //add type
        s.declarations().forEach(t -> t.accept(this, null));
        return null;
    }

    @Override
    public Void visitField(FieldTree t, Void aVoid) {
        typeDictionary.enterIfAbsent(t.type());
        return null;
    }

    @Override
    public Void visitVar(VarTree t, Void aVoid) {
        typeDictionary.enterIfAbsent(t.type());
        return null;
    }

    @Override
    public Void visitFunction(FunctionTree t, Void aVoid) {
        typeDictionary.enterIfAbsent(t.type());
        return null;
    }

    @Override
    public Void visitTypedef(TypedefTree tt, Void v) {
        typeDictionary.enterIfAbsent(tt.type());
        Optional<Tree> def = tt.typeDefinition();
        if (def.isPresent()) {
            def.get().accept(this, null);
        }

        return null;
    }
}
