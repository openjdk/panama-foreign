/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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

package build.tools.panama;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskEvent.Kind;
import com.sun.source.util.TaskListener;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import static java.nio.file.StandardOpenOption.*;

public class RestrictedMethodsFinder implements Plugin {
    private static final boolean DEBUG = Boolean.getBoolean("panama.javac.plugin.debug");
    private static final String RESTRICTED_NATIVE = "jdk.internal.vm.annotation.RestrictedNative";

    private final List<String> declarations = new ArrayList<>();

    @Override
    public void init(JavacTask task, String... args) {
        Trees trees = Trees.instance(task);
        Types types = task.getTypes();
        task.addTaskListener(new TaskListener() {
            @Override
            public void finished(TaskEvent e) {
                if (e.getKind() == Kind.ANALYZE) {
                    CompilationUnitTree cut = e.getCompilationUnit();
                    new TreePathScanner<Void, Void>() {
                        @Override
                        public Void visitMethod(MethodTree node, Void p) {
                            TreePath curPath = getCurrentPath();
                            Element el = trees.getElement(curPath);
                            if (el instanceof ExecutableElement) {
                                ExecutableElement execElem = (ExecutableElement)el;
                                if (isRestrictedNative(execElem)) {
                                    if (DEBUG) {
                                        trees.printMessage(Diagnostic.Kind.NOTE,
                                            "Found a method marked with @RestrictedNative", node, cut);
                                    }
                                    StringBuilder buf = new StringBuilder();
                                    Element parent = trees.getElement(curPath.getParentPath());
                                    if (parent instanceof TypeElement) {
                                        buf.append(getInternalName((TypeElement)parent));
                                        buf.append(' ');
                                        buf.append(node.getName());
                                        buf.append(' ');
                                        buf.append(getInternalSignature(types, execElem));
                                        declarations.add(buf.toString());
                                    }
                                }

                            }
                            return super.visitMethod(node, p);
                        }
                    }.scan(cut, null);
                } else if (e.getKind() == Kind.COMPILATION) {
                    File declsFile = new File(args[0]);
                    declsFile.getParentFile().mkdirs();
                    try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(declsFile.toPath(), CREATE, APPEND))) {
                        for (String decl : declarations) {
                            out.println(decl);
                        }
                    } catch (IOException ioExp) {
                        throw new UncheckedIOException(ioExp);
                    }
                }
            }
        });
    }

    private String getInternalName(TypeElement typeElem) {
        return typeElem.getQualifiedName().toString().replace(".", "/");
    }

    private String getInternalSignature(Types types, ExecutableElement execElem) {
        ExecutableType et = (ExecutableType)types.erasure(execElem.asType());
        StringBuilder buf = new StringBuilder();
        buf.append('(');
        for (TypeMirror pt : et.getParameterTypes()) {
            buf.append(getInternalSignature(pt));
        }
        buf.append(')');
        buf.append(getInternalSignature(et.getReturnType()));
        return buf.toString();
    }

    private String getInternalSignature(TypeMirror type) {
        TypeKind kind = type.getKind();
        switch (kind) {
            case ARRAY:
                return "[" + getInternalSignature(((ArrayType)type).getComponentType());
            case BOOLEAN:
                return "Z";
            case BYTE:
                return "B";
            case CHAR:
                return "C";
            case DECLARED:
                return "L" + getInternalName((TypeElement)((DeclaredType)type).asElement()) + ";";
            case DOUBLE:
                return "D";
            case FLOAT:
                return "F";
            case INT:
                return "I";
            case LONG:
                return "J";
            case SHORT:
                return "S";
            case VOID:
                return "V";
            default:
                throw new AssertionError("unexpected type kind: " + kind);
        }
    }

    private boolean isRestrictedNative(ExecutableElement execElem) {
        return checkAnnotation(execElem, RESTRICTED_NATIVE);
    }

    private boolean checkAnnotation(Element execElem, String name) {
        return execElem.getAnnotationMirrors().stream().anyMatch(
            am -> ((TypeElement) am.getAnnotationType().asElement()).getQualifiedName().contentEquals(name));
    }

    @Override
    public String getName() {
        return "RestrictedMethodsFinder";
    }

    public boolean autoStart() {
        return false;
    }
}
