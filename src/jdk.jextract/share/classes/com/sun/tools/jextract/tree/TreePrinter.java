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

import java.util.List;

public class TreePrinter extends SimpleTreeVisitor<Void, Void> {
    @Override
    public Void defaultAction(Tree t, Void v) {
        System.out.println(t.getClass().getSimpleName());
        String name = t.name();
        if (!name.isEmpty()) {
            System.out.println("name: " + t.name());
        }
        System.out.println(t);
        return null;
    }

    @Override
    public Void visitEnum(EnumTree e, Void v) {
        defaultAction(e, v);
        List<? extends FieldTree> fields = e.constants();
        if (! fields.isEmpty()) {
            System.out.println("fields");
            for (FieldTree f : fields) {
                System.out.println(f.name() + " = " + f.enumConstant().get());
                f.accept(this, v);
            }
        }
        if (!e.isDefinition() && e.definition().isPresent()) {
            System.out.println("--> definition");
            defaultAction(e.definition().get(), v);
        }
        return null;
    }

    @Override
    public Void visitFunction(FunctionTree f, Void v) {
        defaultAction(f, v);
        System.out.printf("%s layout = %s\n\n", f.name(), f.function());
        return null;
    }

    @Override
    public Void visitMacro(MacroTree m, Void v) {
        defaultAction(m, v);
        if (m.isConstant()) {
            System.out.printf("Macro %s = %s\n\n", m.name(), m.value().get());
        }
        return null;
    }

    @Override
    public Void visitHeader(HeaderTree t, Void v) {
        System.out.println("HeaderTree @ " + t.path());
        String name = t.name();
        if (!name.isEmpty()) {
            System.out.println("name: " + t.name());
        }
        int i = 0;
        for (Tree decl : t.declarations()) {
            System.out.println("--> header declaration: " + i++);
            decl.accept(this, v);
        }
        return null;
    }

    @Override
    public Void visitStruct(StructTree s, Void v) {
        defaultAction(s, v);
        System.out.printf("name = '%s', isAnonumous? = %b, layout = %s\n\n",
            s.name(), s.isAnonymous(), s.layout());
        List<? extends FieldTree> fields = s.fields();
        if (! fields.isEmpty()) {
            System.out.println("--> fields");
            for (FieldTree f : fields) {
                f.accept(this, v);
            }
        }
        List<? extends Tree> nested = s.nestedTypes();
        if (! nested.isEmpty()) {
            System.out.println("--> nested types");
            for (Tree nt : nested) {
                nt.accept(this, v);
            }
        }
        if (!s.isDefinition() && s.definition().isPresent()) {
            System.out.println("--> definition");
            defaultAction(s.definition().get(), v);
        }
        return null;
    }

    @Override
    public Void visitVar(VarTree t, Void v) {
        defaultAction(t, v);
        System.out.printf("%s layout = %s\n\n", t.name(), t.layout());
        return null;
    }

    @Override
    public Void visitTypedef(TypedefTree t, Void v) {
        defaultAction(t, v);
        if (t.typeDefinition().isPresent()) {
            System.out.println("--> type definition");
            defaultAction(t.typeDefinition().get(), v);
        }
        return null;
    }
}
