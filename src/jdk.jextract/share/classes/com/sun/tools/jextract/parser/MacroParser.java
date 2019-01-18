/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.jextract.parser;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.SimpleTreeVisitor;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacTaskPool;
import jdk.internal.clang.Cursor;
import jdk.internal.clang.SourceLocation.Location;

import javax.lang.model.element.VariableElement;
import javax.tools.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class MacroParser {

    private final Map<String, Macro> macros = new HashMap<>();
    private final JavacTaskPool pool = new JavacTaskPool(1);

    private Set<String> expansionStack = new HashSet<>();

    static JavaFileManager fm = ToolProvider.getSystemJavaCompiler().getStandardFileManager(null, null, null);

    private static Number toNumber(String str) {
        try {
            // Integer.decode supports '#' hex literals which is not valid in C.
            return str.length() > 0 && str.charAt(0) != '#'? Integer.decode(str) : null;
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    void parse(Cursor cursor, String... tokens) {
        Macro macro = null;
        if (tokens.length == 2) {
            Number num = toNumber(tokens[1]);
            if (num != null) {
                macro = new NumMacro(num, cursor, tokens);
            }
        }
        if (macro == null) {
            macro = new ExprMacro(cursor, tokens);
        }
        macros.put(cursor.spelling(), macro);
    }

    boolean isDefined(String macroName) {
        return macros.containsKey(macroName);
    }

    List<Macro> macros() {
        return new ArrayList<>(macros.values());
    }

    abstract class Macro {
        private final Cursor cursor;
        private String[] tokens;

        Macro(Cursor cursor, String... tokens) {
            this.cursor = cursor;
            this.tokens = tokens;
        }

        final boolean isConstantMacro() {
            try {
                return value() != null;
            } catch (UnresolvableMacroException ex) {
                return false;
            }
        }

        final Location getFileLocation() {
            return cursor.getSourceLocation().getFileLocation();
        }

        final String expand() {
            if (expansionStack.add(name())) {
                try {
                    return Stream.of(tokens)
                            .skip(1) //first token is the macro name!
                            .map(this::expandToken)
                            .collect(Collectors.joining(""));
                } finally {
                    expansionStack.remove(name());
                }
            } else {
                throw new UnresolvableMacroException("Cyclic macro definition");
            }
        }

        private String expandToken(String s) {
            if (Character.isDigit(s.charAt(0))) {
                //this is a number, drop 'U'
                return s.replaceAll("U", "");
            } else {
                Macro macro = macros.get(s);
                return macro != null ?
                        macro.expand() : s;
            }
        }

        @Override
        final public String toString() {
            if (isConstantMacro()) {
                return String.format("Macro[name=%s, value=%s, type=%s]", name(), value(), type());
            } else {
                return String.format("Macro[name=%s, source=%s]", name(), String.join(" ", tokens));
            }
        }

        final String name() {
            return cursor.spelling();
        }

        final Cursor cursor() {
            return cursor;
        }

        abstract Object value() throws UnresolvableMacroException;
        abstract String type() throws UnresolvableMacroException;
    }

    // Macro implementation for simple int valued macros
    final class NumMacro extends Macro {
        private final Number value;

        NumMacro(Number value, Cursor cursor, String... tokens) {
            super(cursor, tokens);
            this.value = value;
        }

        Object value() throws UnresolvableMacroException {
            return value;
        }

        String type() throws UnresolvableMacroException {
            return value.getClass().getName();
        }
    }

    // Macro implementation for macros with expressions
    final class ExprMacro extends Macro {
        private VariableElement ve;

        ExprMacro(Cursor cursor, String... tokens) {
            super(cursor, tokens);
        }

        private void eval() throws UnresolvableMacroException {
            if (ve == null) {
                ve = pool.getTask(null, fm, diag -> {},
                        List.of(), List.of(), List.of(new MacroTemplate(this)), this::doWork);
            }
        }

        private VariableElement doWork(JavacTask task) {
            Trees trees = Trees.instance(task);
            try {
                Iterator<? extends CompilationUnitTree> cus = task.parse().iterator();
                if (cus.hasNext()) {
                    CompilationUnitTree cu = cus.next();
                    VariableTree var = getMacroTree(cu);
                    var.getInitializer().accept(macroValidator, null);
                    TreePath path = trees.getPath(cu, var);
                    task.analyze();
                    return (VariableElement)trees.getElement(path);
                }
            } catch (Throwable t) {
                //ignore
            }
            throw new UnresolvableMacroException("Cannot analyze macro initializer");
        }

        private VariableTree getMacroTree(CompilationUnitTree cu) {
            ClassTree classTree = (ClassTree)cu.getTypeDecls().get(0);
            MethodTree methodTree = (MethodTree)classTree.getMembers().get(0);
            return (VariableTree)methodTree.getBody().getStatements().get(0);
        }

        Object value() throws UnresolvableMacroException {
            eval();
            return ve.getConstantValue();
        }

        String type() throws UnresolvableMacroException {
            eval();
            return ve.asType().toString();
        }
    }

    static TreeVisitor<Void, Void> macroValidator = new SimpleTreeVisitor<>() {

        @Override
        public Void visitLiteral(LiteralTree node, Void _unused) {
            return null;
        }

        @Override
        public Void visitUnary(UnaryTree node, Void _unused) {
            return node.accept(this, null);
        }

        @Override
        public Void visitBinary(BinaryTree node, Void _unused) {
            node.getLeftOperand().accept(this, null);
            return node.getRightOperand().accept(this, null);
        }

        @Override
        public Void visitParenthesized(ParenthesizedTree node, Void _unused) {
            return node.getExpression().accept(this, null);
        }

        @Override
        protected Void defaultAction(Tree node, Void aVoid) {
            throw new UnresolvableMacroException("Unsupported AST node");
        }
    };

    static class MacroTemplate extends SimpleJavaFileObject {
        static String template =
                "class #NAME {" +
                        "   void m() {" +
                        "       final var x = #EXPR;" +
                        "   }" +
                        "}";

        private final String contents;

        public MacroTemplate(Macro macro) {
            super(URI.create("file://" + macro.name() + ".java"), Kind.SOURCE);
            this.contents = template.replaceAll("#NAME", macro.name())
                    .replaceAll("#EXPR", macro.expand());
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return contents;
        }
    }

    static class UnresolvableMacroException extends RuntimeException {
        static final long serialVersionUID = 1L;

        UnresolvableMacroException(String msg) {
            super(msg);
        }
    }
}
