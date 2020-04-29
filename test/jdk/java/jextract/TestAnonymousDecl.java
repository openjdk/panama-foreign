/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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

/*
 * @test
 * @build JextractApiTestBase
 * @modules jdk.incubator.jbind/jdk.incubator.jbind jdk.incubator.jextract/jdk.internal.jextract.impl
 * @run testng/othervm -ea TestAnonymousDecl
 */

import java.util.Set;
import java.util.Optional;
import java.util.function.Consumer;
import jdk.incubator.jbind.AnonymousRegistry;
import jdk.incubator.jbind.SymbolDependencyCollector;
import jdk.incubator.jextract.Declaration;
import jdk.incubator.jextract.Type;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class TestAnonymousDecl extends JextractApiTestBase {
    Declaration.Scoped root;

    @BeforeClass
    public void parse() {
        root = parse("anonymousDecl.h");
        System.out.println(root);
    }

    private String brief(AnonymousRegistry registry, Declaration decl) {
        StringBuilder sb = new StringBuilder();
        sb.append(registry.getName(decl).get());
        sb.append(" for ");
        if (decl instanceof Declaration.Scoped) {
            sb.append(((Declaration.Scoped)decl).kind());
        } else if (decl instanceof Declaration.Variable) {
            sb.append(((Declaration.Variable)decl).kind());
        } else {
            sb.append(decl.getClass().getSimpleName());
        }
        sb.append(" ")
          .append(decl.name())
          .append("@")
          .append(decl.pos().toString());
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    public static <D extends Declaration> D findDecl(Set<Declaration> set, String name, Class<D> declType) {
        Optional<Declaration> d = set.stream().filter(byNameAndType(name, declType)).findAny();
        if (d.isEmpty()) {
            fail("No declaration with name " + name + " found");
            return null;
        }
        return (D) d.get();
    }

    public static void checkStructType(Type actual, String name, String... fields) {
        Declaration.Scoped s = ((Type.Declared) actual).tree();
        checkRecord(s, name, Declaration.Scoped.Kind.STRUCT, fields);
    }

    private Declaration.Scoped assertDeclaredTypedef(Declaration.Variable decl) {
        assertEquals(decl.kind(), Declaration.Variable.Kind.TYPE);
        Type type = decl.type();
        assertTrue(type instanceof Type.Declared);
        return ((Type.Declared) type).tree();
    }

    private void validatePointT(Set<Declaration> deps) {
        Declaration.Scoped point = checkStruct(root, "point", "x", "y");
        assertEquals(findDecl(deps, "point", Declaration.Scoped.class), point);
        Declaration.Variable point_t = findDecl(deps, "point_t", Declaration.Variable.class);
        assertEquals(point_t.kind(), Declaration.Variable.Kind.TYPE);
        assertTypeEquals(point_t.type(), Type.declared(point));
    }

    private void validateCircleT(Set<Declaration> deps) {
        Declaration.Variable circle_t = findDecl(deps, "circle_t", Declaration.Variable.class);
        Declaration.Scoped s = assertDeclaredTypedef(circle_t);
        checkRecord(s, "", Declaration.Scoped.Kind.STRUCT, "center", "radius");
        Declaration.Variable center = findDecl(s, "center", Declaration.Variable.class);
        assertEquals(center.kind(), Declaration.Variable.Kind.FIELD);
        checkStructType(center.type(), "", "x", "y");
    }

    private void validateShapeT(Set<Declaration> deps) {
        Declaration.Variable shape_t = findDecl(deps, "shape_t", Declaration.Variable.class);
        Declaration.Scoped struct = assertDeclaredTypedef(shape_t);
        checkRecord(struct, "", Declaration.Scoped.Kind.STRUCT, "kind", "line", "circle", "polygon");
        // TODO: More specific
        validatePointT(deps);
        validateCircleT(deps);
    }

    @Test
    public void testDependencyVar() {
        AnonymousRegistry registry = new AnonymousRegistry();
        Declaration.Variable canvas = findDecl(root, "canvas_size", Declaration.Variable.class);
        Set<Declaration> deps = SymbolDependencyCollector.collect(canvas, registry);
        System.out.println("Dependency of canvas_size:");
        deps.forEach(d -> System.out.println(brief(registry, d)));
        assertEquals(deps.size(), 1);
        Declaration.Variable v = findDecl(deps, "canvas_size", Declaration.Variable.class);
        assertEquals(v.kind(), Declaration.Variable.Kind.TYPE);
        assertEquals(v.pos(), canvas.pos());
        checkStructType(v.type(), "", "width", "height");
    }

    @Test
    public void testDependencyFn() {
        AnonymousRegistry registry = new AnonymousRegistry();
        Declaration.Function makeCircle = findDecl(root, "makeCircle", Declaration.Function.class);
        Set<Declaration> deps = SymbolDependencyCollector.collect(makeCircle, registry);
        System.out.println("Dependency of makeCircle:");
        deps.forEach(d -> System.out.println(brief(registry, d)));
        validateShapeT(deps);
        validatePointT(deps);
    }

    @Test
    public void testDependencyTypedef() {
        Declaration.Variable shape_t = findDecl(root, "shape_t", Declaration.Variable.class);
        Declaration.Scoped shape = assertDeclaredTypedef(shape_t);
        assertEquals(shape.kind(), Declaration.Scoped.Kind.STRUCT);
        final AnonymousRegistry regShape = new AnonymousRegistry();
        Set<Declaration> deps = SymbolDependencyCollector.collect(shape_t, regShape);
        System.out.println("Dependency of shape:");
        deps.forEach(d -> System.out.println(brief(regShape, d)));
        validateShapeT(deps);
    }

    @Test
    public void testRegistry() {
        AnonymousRegistry registry = new AnonymousRegistry();
        SymbolDependencyCollector.collect(root, registry);
        root.accept(WALKER, d -> System.out.println(registry.getName(d).orElseThrow() + ": " + d));
    }

    private static Declaration.Visitor<Void, Consumer<Declaration>> WALKER = new Declaration.Visitor<>() {
        @Override
        public Void visitScoped(Declaration.Scoped scope, Consumer<Declaration> act) {
            act.accept(scope);
            scope.members().forEach(m -> m.accept(this, act));
            return null;
        }

        @Override
        public Void visitFunction(Declaration.Function func, Consumer<Declaration> act) {
            SymbolDependencyCollector.collect(func, new AnonymousRegistry()).forEach(act);
            return null;
        }

        @Override
        public Void visitDeclaration(Declaration d, Consumer<Declaration> act) {
            act.accept(d);
            return null;
        }
    };
}
