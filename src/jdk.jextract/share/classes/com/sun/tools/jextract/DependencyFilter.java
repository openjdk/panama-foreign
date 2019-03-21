/*
 *  Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.tools.jextract.tree.EnumTree;
import com.sun.tools.jextract.tree.FieldTree;
import com.sun.tools.jextract.tree.FunctionTree;
import com.sun.tools.jextract.tree.HeaderTree;
import com.sun.tools.jextract.tree.MacroTree;
import com.sun.tools.jextract.tree.SimpleTreeVisitor;
import com.sun.tools.jextract.tree.StructTree;
import com.sun.tools.jextract.tree.Tree;
import com.sun.tools.jextract.tree.TreeMaker;
import com.sun.tools.jextract.tree.TreePhase;
import com.sun.tools.jextract.tree.TypedefTree;
import com.sun.tools.jextract.tree.VarTree;
import jdk.internal.clang.Cursor;
import jdk.internal.clang.Type;
import jdk.internal.clang.TypeKind;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

/**
 * A phase that does 2 passes;
 *
 * 1. For all declarations in a root header header,
 *    find all dependent structs, enums and typedefs and add them to a Set.
 *
 * 2. Filters out structs, typedefs and enums that do
 *    not appear in the set of required elements.
 */
public class DependencyFilter implements TreePhase {
    private final Context ctx;

    public DependencyFilter(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public HeaderTree transform(HeaderTree header) {
        Set<Tree> required = new HashSet<>();
        header = new DependencyFinder(ctx, required).transform(header);
        header = new ElementFilter(ctx, required).transform(header);
        return header;
    }

    /**
     * For all declarations in a root header header,
     * find all dependent structs, enums and typedefs and add them to a Set.
     */
    private static class DependencyFinder extends SimpleTreeVisitor<Void, Void> implements TreePhase {

        private final TreeMaker treeMaker = new TreeMaker();
        private final Log log;
        private final Filters filters;
        private final Set<Tree> required;

        public DependencyFinder(Context ctx, Set<Tree> required) {
            this.required = required;
            this.log = ctx.log;
            this.filters = ctx.filters;
        }

        private void visitType(Tree tree) {
            visitType(tree.type());
        }

        private void visitCursor(Cursor cursor) {
            accept(treeMaker.createTree(cursor));
        }

        private boolean add(Tree tree) {
            log.print(Level.FINE, () -> "DepedencyFinder adding tree: " + tree.name());
            return required.add(tree);
        }

        private void visitType(Type type) {
            visitCursor(type.getDeclarationCursor());
            Type elementType = type.getElementType();
            if(elementType.kind() != TypeKind.Invalid) {
                visitCursor(elementType.getDeclarationCursor());
            }
            Type pointeeType = type.getPointeeType();
            if(pointeeType.kind() != TypeKind.Invalid) {
                visitCursor(pointeeType.getDeclarationCursor());
            }
        }

        private void accept(Tree tree) {
            tree.accept(this, null);
        }

        @Override
        protected Void defaultAction(Tree t, Void p) {
            add(t);
            log.print(Level.FINE, () -> "DepedencyFinder adding unknown tree type: " + t.name());
            return null;
        }

        @Override
        public HeaderTree transform(HeaderTree header) {
            accept(header);
            return header;
        }

        @Override
        public Void visitHeader(HeaderTree t, Void p) {
            t.declarations().stream().filter(filters::isInRootHeader).forEach(this::accept);
            return null;
        }

        @Override
        public Void visitFunction(FunctionTree t, Void p) {
            t.paramTypes().forEach(this::visitType);
            visitType(t.returnType());
            return null;
        }

        @Override
        public Void visitVar(VarTree t, Void p) {
            visitType(t);
            return null;
        }

        @Override
        public Void visitMacro(MacroTree t, Void p) {
            t.cursor().children().map(treeMaker::createTree).forEach(tree -> tree.accept(this, null));
            return null;
        }

        @Override
        public Void visitEnum(EnumTree t, Void p) {
            if(add(t)) {
                visitType(t.cursor().getEnumDeclIntegerType());
            }
            return null;
        }

        @Override
        public Void visitField(FieldTree t, Void p) {
            visitType(t);
            return null;
        }

        @Override
        public Void visitStruct(StructTree t, Void p) {
            if(add(t)) {
                t.cursor().children().forEach(this::visitCursor);
            }
            return null;
        }

        @Override
        public Void visitTypedef(TypedefTree t, Void p) {
            if(add(t)) {
                visitType(t.type().canonicalType());
            }
            return null;
        }
    }

    /**
     * TreeFilter used to filter out structs, typedefs and enums that do
     * not appear in the set of required elements.
     */
    private static class ElementFilter extends TreeFilter {
        private final Log log;
        private final Set<Tree> required;

        public ElementFilter(Context ctx, Set<Tree> required) {
            this.log = ctx.log;
            this.required = required;
        }

        private boolean filter(Tree tree) {
            if(required.contains(tree)) {
                log.print(Level.FINE, () -> "Including required tree: " + tree.name());
                return true;
            }
            log.print(Level.FINE, () -> "Excluding: " + tree.name());
            return false;
        }

        @Override
        public Boolean visitStruct(StructTree t, Void aVoid) {
            return filter(t);
        }

        @Override
        public Boolean visitTypedef(TypedefTree t, Void aVoid) {
            return filter(t);
        }

        @Override
        public Boolean visitEnum(EnumTree t, Void aVoid) {
            return filter(t);
        }

    }
}
