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
package jdk.internal.jextract.impl;

import jdk.incubator.foreign.*;
import jdk.incubator.jextract.Declaration;
import jdk.incubator.jextract.JextractTool;
import jdk.incubator.jextract.Type;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.constant.ClassDesc;
import java.lang.invoke.MethodType;
import java.net.URI;
import java.net.URL;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * Scan a header file and generate Java source items for entities defined in that header
 * file. Tree visitor visit methods return true/false depending on whether a
 * particular Tree is processed or skipped.
 */
public class OutputFactory implements Declaration.Visitor<Void, Declaration> {
    // internal symbol used by clang for "va_list"
    private static final String VA_LIST_TAG = "__va_list_tag";
    private final Set<String> constants = new HashSet<>();
    // To detect duplicate Variable and Function declarations.
    private final Set<String> variables = new HashSet<>();
    private final Set<Declaration.Function> functions = new HashSet<>();

    protected final ToplevelBuilder toplevelBuilder;
    protected JavaSourceBuilder currentBuilder;
    protected final TypeTranslator typeTranslator = new TypeTranslator();
    private final String pkgName;
    private final Map<Declaration, String> structClassNames = new HashMap<>();
    private final Set<Declaration.Typedef> unresolvedStructTypedefs = new HashSet<>();

    private void addStructDefinition(Declaration decl, String name) {
        structClassNames.put(decl, name);
    }

    private boolean structDefinitionSeen(Declaration decl) {
        return structClassNames.containsKey(decl);
    }

    private String structDefinitionName(Declaration decl) {
        return structClassNames.get(decl);
    }

    // have we seen this Variable earlier?
    protected boolean variableSeen(Declaration.Variable tree) {
        return !variables.add(tree.name());
    }

    // have we seen this Function earlier?
    protected boolean functionSeen(Declaration.Function tree) {
        return !functions.add(tree);
    }

    public static JavaFileObject[] generateWrapped(Declaration.Scoped decl, String headerName,
                String pkgName, List<String> libraryNames) {
        String clsName = Utils.javaSafeIdentifier(headerName.replace(".h", "_h"), true);
        ToplevelBuilder toplevelBuilder = new ToplevelBuilder(ClassDesc.of(pkgName, clsName), libraryNames.toArray(new String[0]));
        return new OutputFactory(pkgName, toplevelBuilder).generate(decl);
    }

    private OutputFactory(String pkgName, ToplevelBuilder toplevelBuilder) {
        this.pkgName = pkgName;
        this.toplevelBuilder = toplevelBuilder;
        this.currentBuilder = toplevelBuilder;
    }

    static final String C_LANG_CONSTANTS_HOLDER = "jdk.incubator.foreign.CLinker";

    JavaFileObject[] generate(Declaration.Scoped decl) {
        //generate all decls
        decl.members().forEach(this::generateDecl);
        // check if unresolved typedefs can be resolved now!
        for (Declaration.Typedef td : unresolvedStructTypedefs) {
            Declaration.Scoped structDef = ((Type.Declared) td.type()).tree();
            toplevelBuilder.addTypedef(td.name(),
                    structDefinitionSeen(structDef) ? structDefinitionName(structDef) : null, td.type());
        }
        try {
            List<JavaFileObject> files = new ArrayList<>(toplevelBuilder.toFiles());
            files.add(jfoFromString(pkgName,"RuntimeHelper", getRuntimeHelperSource()));
            return files.toArray(new JavaFileObject[0]);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (URISyntaxException ex2) {
            throw new RuntimeException(ex2);
        }
    }

    private String getRuntimeHelperSource() throws URISyntaxException, IOException {
        URL runtimeHelper = OutputFactory.class.getResource("resources/RuntimeHelper.java.template");
        return (pkgName.isEmpty()? "" : "package " + pkgName + ";\n") +
                        String.join("\n", Files.readAllLines(Paths.get(runtimeHelper.toURI())))
                                .replace("${C_LANG}", C_LANG_CONSTANTS_HOLDER);
    }

    private void generateDecl(Declaration tree) {
        try {
            tree.accept(this, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private JavaFileObject jfoFromString(String pkgName, String clsName, String contents) {
        String pkgPrefix = pkgName.isEmpty() ? "" : pkgName.replaceAll("\\.", "/") + "/";
        return InMemoryJavaCompiler.jfoFromString(URI.create(pkgPrefix + clsName + ".java"), contents);
    }

    @Override
    public Void visitConstant(Declaration.Constant constant, Declaration parent) {
        if (!constants.add(constant.name())) {
            //skip
            return null;
        }

        Class<?> clazz = getJavaType(constant.type());
        if (clazz == null) {
            warn("skipping " + constant.name() + " because of unsupported type usage");
            return null;
        }
        toplevelBuilder.addConstant(Utils.javaSafeIdentifier(constant.name()),
                constant.value() instanceof String ? MemorySegment.class :
                clazz, constant.value());
        return null;
    }

    @Override
    public Void visitScoped(Declaration.Scoped d, Declaration parent) {
        if (d.layout().isEmpty()) {
            //skip decl-only
            return null;
        }
        boolean isStructKind = switch (d.kind()) {
            case STRUCT, UNION -> true;
            default -> false;
        };
        if (isStructKind) {
            String className = d.name();
            GroupLayout layout = (GroupLayout) layoutFor(d);
            currentBuilder = currentBuilder.addStruct(className, parent, layout, Type.declared(d));
            currentBuilder.classBegin();
            if (!className.isEmpty()) {
                addStructDefinition(d, currentBuilder.fullName());
            }
        }
        try {
            d.members().forEach(fieldTree -> fieldTree.accept(this, d));
        } finally {
            if (isStructKind) {
                currentBuilder = currentBuilder.classEnd();
            }
        }
        return null;
    }

    private static boolean isVaList(FunctionDescriptor desc) {
        List<MemoryLayout> argLayouts = desc.argumentLayouts();
        int size = argLayouts.size();
        if (size > 0) {
            MemoryLayout lastLayout = argLayouts.get(size - 1);
            if (lastLayout instanceof SequenceLayout) {
                SequenceLayout seq = (SequenceLayout)lastLayout;
                MemoryLayout elem = seq.elementLayout();
                // FIXME: hack for now to use internal symbol used by clang for va_list
                return elem.name().orElse("").equals(VA_LIST_TAG);
            }
        }

        return false;
    }

    private static MemoryLayout isUnsupported(MemoryLayout layout) {
        if (layout instanceof ValueLayout) {
            if (UnsupportedLayouts.isUnsupported(layout)) {
                return layout;
            }
        } else if (layout instanceof GroupLayout) {
            GroupLayout gl = (GroupLayout)layout;
            for (MemoryLayout ml : gl.memberLayouts()) {
                MemoryLayout ul = isUnsupported(ml);
                if (ul != null) {
                    return ul;
                }
            }
        }

        return null;
    }

    private static MemoryLayout isUnsupported(FunctionDescriptor desc) {
        MemoryLayout resultLayout = desc.returnLayout().orElse(null);
        if (resultLayout != null) {
            MemoryLayout ul = isUnsupported(resultLayout);
            if (ul != null) {
                return ul;
            }
        }

        for (MemoryLayout argLayout : desc.argumentLayouts()) {
            MemoryLayout ul = isUnsupported(argLayout);
            if (ul != null) {
                return ul;
            }
        }

        return null;
    }

    private boolean generateFunctionalInterface(Type.Function func, String name) {
        name = Utils.javaSafeIdentifier(name);
        //generate functional interface
        if (func.varargs()) {
            warn("varargs in callbacks is not supported: " + name);
        }
        MethodType fitype = getMethodType(func, false);
        if (fitype == null) {
            warn("skipping " + name + " because of unsupported type usage");
            return false;
        }

        FunctionDescriptor fpDesc = Type.descriptorFor(func).orElseThrow();
        MemoryLayout unsupportedLayout = isUnsupported(fpDesc);
        if (unsupportedLayout != null) {
            warn("skipping " + name + " because of unsupported type usage: " +
                    UnsupportedLayouts.getUnsupportedTypeName(unsupportedLayout));
            return false;
        }

        currentBuilder.addFunctionalInterface(name, fitype, fpDesc);
        return true;
    }

    @Override
    public Void visitFunction(Declaration.Function funcTree, Declaration parent) {
        if (functionSeen(funcTree)) {
            return null;
        }

        FunctionDescriptor descriptor = Type.descriptorFor(funcTree.type()).orElse(null);
        if (descriptor == null) {
            //abort
            return null;
        }

        MemoryLayout unsupportedLayout = isUnsupported(descriptor);
        if (unsupportedLayout != null) {
            warn("skipping " + funcTree.name() + " because of unsupported type usage: " +
                    UnsupportedLayouts.getUnsupportedTypeName(unsupportedLayout));
            return null;
        }

        MethodType mtype = getMethodType(funcTree.type());
        if (mtype == null) {
            warn("skipping " + funcTree.name() + " because of unsupported type usage");
            return null;
        }

        if (isVaList(descriptor)) {
            MemoryLayout[] argLayouts = descriptor.argumentLayouts().toArray(new MemoryLayout[0]);
            argLayouts[argLayouts.length - 1] = CLinker.C_VA_LIST;
            descriptor = descriptor.returnLayout().isEmpty()?
                    FunctionDescriptor.ofVoid(argLayouts) :
                    FunctionDescriptor.of(descriptor.returnLayout().get(), argLayouts);
            Class<?>[] argTypes = mtype.parameterArray();
            argTypes[argLayouts.length - 1] = MemoryAddress.class;
            mtype = MethodType.methodType(mtype.returnType(), argTypes);
        }

        String mhName = Utils.javaSafeIdentifier(funcTree.name());
        //generate static wrapper for function
        List<String> paramNames = funcTree.parameters()
                                          .stream()
                                          .map(Declaration.Variable::name)
                                          .map(p -> !p.isEmpty() ? Utils.javaSafeIdentifier(p) : p)
                                          .collect(Collectors.toList());
        int i = 0;
        for (Declaration.Variable param : funcTree.parameters()) {
            Type.Function f = getAsFunctionPointer(param.type());
            if (f != null) {
                String name = funcTree.name() + "$" + (param.name().isEmpty() ? "x" + i : param.name());
                if (! generateFunctionalInterface(f, name)) {
                    return null;
                }
                i++;
            }
        }

        toplevelBuilder.addFunction(mhName, funcTree.name(), mtype,
                descriptor, funcTree.type().varargs(), paramNames);

        return null;
    }

    Type.Function getAsFunctionPointer(Type type) {
        if (type instanceof Type.Delegated) {
            Type.Delegated delegated = (Type.Delegated)type;
            return delegated.kind() == Type.Delegated.Kind.POINTER ?
                getAsFunctionPointer(delegated.type()) : null;
        } else if (type instanceof Type.Function) {
            /*
             * // pointer to function declared as function like this
             *
             * typedef void CB(int);
             * void func(CB cb);
             */
            return (Type.Function)type;
        } else {
            return null;
        }
    }

    @Override
    public Void visitTypedef(Declaration.Typedef tree, Declaration parent) {
        Type type = tree.type();
        if (type instanceof Type.Declared) {
            Declaration.Scoped s = ((Type.Declared) type).tree();
            if (!s.name().equals(tree.name())) {
                switch (s.kind()) {
                    case STRUCT, UNION -> {
                        if (s.name().isEmpty()) {
                            visitScoped(s, tree);
                        } else {
                            /*
                             * If typedef is seen after the struct/union definition, we can generate subclass
                             * right away. If not, we've to save it and revisit after all the declarations are
                             * seen. This is to support forward declaration of typedefs.
                             *
                             * typedef struct Foo Bar;
                             *
                             * struct Foo {
                             *     int x, y;
                             * };
                             */
                            if (structDefinitionSeen(s)) {
                                toplevelBuilder.addTypedef(tree.name(), structDefinitionName(s), tree.type());
                            } else {
                                /*
                                 * Definition of typedef'ed struct/union not seen yet. May be the definition comes later.
                                 * Save it to visit at the end of all declarations.
                                 */
                                unresolvedStructTypedefs.add(tree);
                            }
                        }
                    }
                    default -> visitScoped(s, tree);
                }
            }
        } else if (type instanceof Type.Primitive) {
             toplevelBuilder.addTypedef(tree.name(), null, type);
        } else {
            Type.Function func = getAsFunctionPointer(type);
            if (func != null) {
                generateFunctionalInterface(func, tree.name());
            }
        }
        return null;
    }

    @Override
    public Void visitVariable(Declaration.Variable tree, Declaration parent) {
        if (parent == null && variableSeen(tree)) {
            return null;
        }

        String fieldName = tree.name();
        String symbol = tree.name();
        assert !symbol.isEmpty();
        assert !fieldName.isEmpty();
        fieldName = Utils.javaSafeIdentifier(fieldName);

        Type type = tree.type();

        if (type instanceof Type.Declared && ((Type.Declared) type).tree().name().isEmpty()) {
            // anon type - let's generate something
            ((Type.Declared) type).tree().accept(this, tree);
        }
        MemoryLayout layout = tree.layout().orElse(Type.layoutFor(type).orElse(null));
        if (layout == null) {
            //no layout - abort
            return null;
        }

        MemoryLayout ul = isUnsupported(layout);
        if (ul != null) {
            String name = parent != null? parent.name() + "." : "";
            name += fieldName;
            warn("skipping " + name + " because of unsupported type usage: " +
                    UnsupportedLayouts.getUnsupportedTypeName(ul));
            return null;
        }

        Type.Function func = getAsFunctionPointer(type);
        if (func != null) {
            generateFunctionalInterface(func, fieldName);
        }

        if (tree.kind() == Declaration.Variable.Kind.BITFIELD ||
                (layout instanceof ValueLayout && layout.byteSize() > 8)) {
            //skip
            return null;
        }

        boolean sizeAvailable;
        try {
            layout.byteSize();
            sizeAvailable = true;
        } catch (Exception ignored) {
            sizeAvailable = false;
        }
        MemoryLayout treeLayout = tree.layout().orElseThrow();
        if (sizeAvailable) {
            Class<?> clazz = getJavaType(type);
            if (clazz == null) {
                String name = parent != null? parent.name() + "." : "";
                name += fieldName;
                warn("skipping " + name + " because of unsupported type usage");
                return null;
            }
            currentBuilder.addVar(fieldName, tree.name(), treeLayout, clazz);
        } else {
            warn("Layout size not available for " + fieldName);
        }

        return null;
    }

    protected static MemoryLayout layoutFor(Declaration decl) {
        if (decl instanceof Declaration.Typedef) {
            Declaration.Typedef alias = (Declaration.Typedef) decl;
            return Type.layoutFor(alias.type()).orElseThrow();
        } else if (decl instanceof Declaration.Scoped) {
            return ((Declaration.Scoped) decl).layout().orElseThrow();
        } else {
            throw new IllegalArgumentException("Unexpected parent declaration");
        }
        // case like `typedef struct { ... } Foo`
    }

    private void warn(String msg) {
        System.err.println("WARNING: " + msg);
    }

    private Class<?> getJavaType(Type type) {
        try {
            return typeTranslator.getJavaType(type);
        } catch (UnsupportedOperationException uoe) {
            warn(uoe.toString());
            if (JextractTool.DEBUG) {
                uoe.printStackTrace();
            }
            return null;
        }
    }

    private MethodType getMethodType(Type.Function type) {
        try {
            return typeTranslator.getMethodType(type);
        } catch (UnsupportedOperationException uoe) {
            warn(uoe.toString());
            if (JextractTool.DEBUG) {
                uoe.printStackTrace();
            }
            return null;
        }
    }

    private MethodType getMethodType(Type.Function type, boolean varargsCheck) {
        try {
            return typeTranslator.getMethodType(type, varargsCheck);
        } catch (UnsupportedOperationException uoe) {
            warn(uoe.toString());
            if (JextractTool.DEBUG) {
                uoe.printStackTrace();
            }
            return null;
        }
    }
}
