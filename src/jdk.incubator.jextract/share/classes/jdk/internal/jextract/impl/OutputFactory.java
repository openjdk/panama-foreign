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
import jdk.incubator.jextract.Type;
import jdk.incubator.jextract.Type.Primitive;

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
    protected final AnnotationWriter annotationWriter;
    private final String pkgName;
    private final Map<Declaration, String> structClassNames = new HashMap<>();
    private final Set<Declaration.Typedef> unresolvedStructTypedefs = new HashSet<>();

    private String addStructDefinition(Declaration decl, String name) {
        return structClassNames.put(decl, name);
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

    public static JavaFileObject[] generateWrapped(Declaration.Scoped decl, String headerName, boolean source,
                String pkgName, List<String> libraryNames) {
        String clsName = Utils.javaSafeIdentifier(headerName.replace(".h", "_h"), true);
        ConstantHelper constantHelper = ConstantHelper.make(source, pkgName, clsName,
                ClassDesc.of(pkgName, "RuntimeHelper"), ClassDesc.of("jdk.incubator.foreign", "CLinker"),
                libraryNames.toArray(String[]::new));
        AnnotationWriter annotationWriter = new AnnotationWriter();
        ToplevelBuilder toplevelBuilder = new ToplevelBuilder(clsName, pkgName, constantHelper, annotationWriter);
        return new OutputFactory(pkgName, toplevelBuilder, annotationWriter).generate(decl);
    }

    private OutputFactory(String pkgName, ToplevelBuilder toplevelBuilder,
                          AnnotationWriter annotationWriter) {
        this.pkgName = pkgName;
        this.toplevelBuilder = toplevelBuilder;
        this.currentBuilder = toplevelBuilder;
        this.annotationWriter = annotationWriter;
    }

    static final String C_LANG_CONSTANTS_HOLDER = "jdk.incubator.foreign.CLinker";

    JavaFileObject[] generate(Declaration.Scoped decl) {
        toplevelBuilder.classBegin();
        //generate all decls
        decl.members().forEach(this::generateDecl);
        // check if unresolved typedefs can be resolved now!
        for (Declaration.Typedef td : unresolvedStructTypedefs) {
            Declaration.Scoped structDef = ((Type.Declared) td.type()).tree();
            toplevelBuilder.addTypeDef(td.name(),
                    structDefinitionSeen(structDef) ? structDefinitionName(structDef) : null, td.type());
        }
        toplevelBuilder.classEnd();
        try {
            List<JavaFileObject> files = new ArrayList<>();
            files.addAll(toplevelBuilder.build());
            files.add(jfoFromString(pkgName,"RuntimeHelper", getRuntimeHelperSource()));
            files.add(jfoFromString(pkgName,"C", getCAnnotationSource()));
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

    private String getCAnnotationSource() throws URISyntaxException, IOException {
        URL cAnnotation = OutputFactory.class.getResource("resources/C.java.template");
        return (pkgName.isEmpty()? "" : "package " + pkgName + ";\n") +
                String.join("\n", Files.readAllLines(Paths.get(cAnnotation.toURI())));
    }

    private void generateDecl(Declaration tree) {
        try {
            tree.accept(this, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static Class<?> classForType(Primitive.Kind type, MemoryLayout layout) {
        boolean isFloat = switch(type) {
            case Float, Double -> true;
            default-> false;
        };
        return TypeTranslator.layoutToClass(isFloat, layout);
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

        String anno = annotationWriter.getCAnnotation(constant.type());
        header().addConstantGetter(Utils.javaSafeIdentifier(constant.name()),
                constant.value() instanceof String ? MemorySegment.class :
                typeTranslator.getJavaType(constant.type()), constant.value(), anno);
        return null;
    }

    @Override
    public Void visitScoped(Declaration.Scoped d, Declaration parent) {
        if (d.layout().isEmpty()) {
            //skip decl-only
            return null;
        }
        boolean structClass = false;
        if (!d.name().isEmpty() || !isRecord(parent)) {
            //only add explicit struct layout if the struct is not to be flattened inside another struct
            switch (d.kind()) {
                case STRUCT:
                case UNION: {
                    structClass = true;
                    String className = d.name().isEmpty() ? parent.name() : d.name();
                    GroupLayout parentLayout = (GroupLayout)parentLayout(d);
                    currentBuilder = currentBuilder.newStructBuilder(className, parentLayout, Type.declared(d));
                    addStructDefinition(d, currentBuilder.className);
                    currentBuilder.classBegin();
                    currentBuilder.addLayoutGetter(((StructBuilder)currentBuilder).layoutField(), d.layout().get());
                    break;
                }
            }
        }
        d.members().forEach(fieldTree -> fieldTree.accept(this, d));
        if (structClass) {
            currentBuilder = currentBuilder.classEnd();
        }
        return null;
    }

    private static final boolean isVaList(FunctionDescriptor desc) {
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

    private static boolean isUnsupported(MemoryLayout layout) {
        if (layout instanceof ValueLayout) {
            return UnsupportedLayouts.isUnsupported((ValueLayout)layout);
        } else if (layout instanceof GroupLayout) {
            GroupLayout gl = (GroupLayout)layout;
            for (MemoryLayout ml : gl.memberLayouts()) {
                if (isUnsupported(ml)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static MemoryLayout isUnsupported(FunctionDescriptor desc) {
        MemoryLayout resultLayout = desc.returnLayout().orElse(null);
        if (resultLayout != null && isUnsupported(resultLayout)) {
            return resultLayout;
        }

        for (MemoryLayout argLayout : desc.argumentLayouts()) {
            if (isUnsupported(argLayout)) {
                return argLayout;
            }
        }

        return null;
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
            warn("skipping " + funcTree.name() + " because of unsupported type usage: " + unsupportedLayout.name().get());
            return null;
        }

        MethodType mtype = typeTranslator.getMethodType(funcTree.type());

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
        header().addMethodHandleGetter(mhName, funcTree.name(), mtype, descriptor, funcTree.type().varargs());
        //generate static wrapper for function
        List<String> paramNames = funcTree.parameters()
                                          .stream()
                                          .map(Declaration.Variable::name)
                                          .map(p -> !p.isEmpty() ? Utils.javaSafeIdentifier(p) : p)
                                          .collect(Collectors.toList());
        List<String> annos = funcTree.parameters()
                .stream()
                .map(Declaration.Variable::type)
                .map(annotationWriter::getCAnnotation)
                .collect(Collectors.toList());
        String returnAnno = annotationWriter.getCAnnotation(funcTree.type().returnType());

        int i = 0;
        for (Declaration.Variable param : funcTree.parameters()) {
            Type.Function f = getAsFunctionPointer(param.type());
            if (f != null) {
                String name = funcTree.name() + "$" + (param.name().isEmpty() ? "x" + i : param.name());
                name = Utils.javaSafeIdentifier(name);
                //generate functional interface
                if (f.varargs()) {
                    warn("varargs in callbacks is not supported");
                }
                MethodType fitype = typeTranslator.getMethodType(f, false);
                FunctionDescriptor fpDesc = Type.descriptorFor(f).orElseThrow();
                unsupportedLayout = isUnsupported(fpDesc);
                if (unsupportedLayout != null) {
                    warn("skipping " + funcTree.name() + " because of unsupported type usage: " +
                            unsupportedLayout.name().get() + " in " + param.name());
                    return null;
                }

                toplevelBuilder.addFunctionalInterface(name, fitype, fpDesc, param.type());
                i++;
            }
        }

        header().addStaticFunctionWrapper(Utils.javaSafeIdentifier(funcTree.name()), funcTree.name(), mtype,
                Type.descriptorFor(funcTree.type()).orElseThrow(), funcTree.type().varargs(), paramNames, annos, returnAnno);

        return null;
    }

    Type.Function getAsFunctionPointer(Type type) {
        if (type instanceof Type.Delegated) {
            switch (((Type.Delegated) type).kind()) {
                case POINTER: {
                    Type pointee = ((Type.Delegated) type).type();
                    return (pointee instanceof Type.Function) ?
                        (Type.Function)pointee : null;
                }
                default:
                    return getAsFunctionPointer(((Type.Delegated) type).type());
            }
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
                    case STRUCT:
                    case UNION: {
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
                                toplevelBuilder.addTypeDef(tree.name(), structDefinitionName(s), tree.type());
                            } else {
                                /*
                                 * Definition of typedef'ed struct/union not seen yet. May be the definition comes later.
                                 * Save it to visit at the end of all declarations.
                                 */
                                unresolvedStructTypedefs.add(tree);
                            }
                        }
                    }
                    break;
                    default:
                        visitScoped(s, tree);
                }
            }
        } else if (type instanceof Type.Primitive) {
             String anno = annotationWriter.getCAnnotation(type);
             header().emitPrimitiveTypedef((Type.Primitive)type, tree.name(), anno);
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

        if (isUnsupported(layout)) {
            String name = parent != null? parent.name() + "." : "";
            name += fieldName;
            warn("skipping " + name + " because of unsupported type usage: " + layout.name().get());
        }

        Class<?> clazz = typeTranslator.getJavaType(type);
        String anno = annotationWriter.getCAnnotation(type);
        if (tree.kind() == Declaration.Variable.Kind.BITFIELD ||
                (layout instanceof ValueLayout && layout.byteSize() > 8)) {
            //skip
            return null;
        }

        boolean isSegment = clazz == MemorySegment.class;
        boolean sizeAvailable;
        try {
            layout.byteSize();
            sizeAvailable = true;
        } catch (Exception ignored) {
            sizeAvailable = false;
        }
        MemoryLayout treeLayout = tree.layout().orElseThrow();
        if (parent != null) { //struct field
            if (isSegment) {
                if (sizeAvailable) {
                    currentBuilder.addSegmentGetter(fieldName, tree.name(), treeLayout);
                } else {
                    warn("Layout size not available for " + fieldName);
                }
            } else {
                currentBuilder.addVarHandleGetter(fieldName, tree.name(), treeLayout, clazz);
                currentBuilder.addGetter(fieldName, tree.name(), treeLayout, clazz, anno);
                currentBuilder.addSetter(fieldName, tree.name(), treeLayout, clazz, anno);
            }
        } else {
            if (sizeAvailable) {
                if (isSegment) {
                    header().addSegmentGetter(fieldName, tree.name(), treeLayout);
                } else {
                    header().addLayoutGetter(fieldName, layout);
                    header().addVarHandleGetter(fieldName, tree.name(), treeLayout, clazz);
                    header().addSegmentGetter(fieldName, tree.name(), treeLayout);
                    header().addGetter(fieldName, tree.name(), treeLayout, clazz, anno);
                    header().addSetter(fieldName, tree.name(), treeLayout, clazz, anno);
                }
            } else {
                warn("Layout size not available for " + fieldName);
            }
        }

        return null;
    }

    private boolean isRecord(Declaration declaration) {
        if (declaration == null) {
            return false;
        } else if (!(declaration instanceof Declaration.Scoped)) {
            return false;
        } else {
            Declaration.Scoped scope = (Declaration.Scoped)declaration;
            return scope.kind() == Declaration.Scoped.Kind.CLASS ||
                    scope.kind() == Declaration.Scoped.Kind.STRUCT ||
                    scope.kind() == Declaration.Scoped.Kind.UNION;
        }
    }

    protected static MemoryLayout parentLayout(Declaration parent) {
        if (parent instanceof Declaration.Typedef) {
            Declaration.Typedef alias = (Declaration.Typedef) parent;
            return Type.layoutFor(alias.type()).orElseThrow();
        } else if (parent instanceof Declaration.Scoped) {
            return ((Declaration.Scoped) parent).layout().orElseThrow();
        } else {
            throw new IllegalArgumentException("Unexpected parent declaration");
        }
        // case like `typedef struct { ... } Foo`
    }

    private void warn(String msg) {
        System.err.println("WARNING: " + msg);
    }

    HeaderFileBuilder header() {
        return toplevelBuilder.nextHeader();
    }
}
