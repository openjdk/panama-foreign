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
package jdk.incubator.jextract.tool;

import jdk.incubator.foreign.*;
import jdk.incubator.jextract.Declaration;
import jdk.incubator.jextract.Type;
import jdk.incubator.jextract.Type.Primitive;
import jdk.internal.foreign.abi.SharedUtils;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
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
    private final Set<String> constants = new HashSet<>();
    // To detect duplicate Variable and Function declarations.
    private final Set<Declaration.Variable> variables = new HashSet<>();
    private final Set<Declaration.Function> functions = new HashSet<>();

    protected final HeaderBuilder builder;
    protected final ConstantHelper constantHelper;
    protected final TypeTranslator typeTranslator = new TypeTranslator();
    private final String clsName;
    private final String pkgName;
    private StructBuilder structBuilder;
    private Map<Declaration, String> structClassNames = new HashMap<>();
    private List<String> structSources = new ArrayList<>();
    private Set<String> nestedClassNames = new HashSet<>();
    private int nestedClassNameCount = 0;
    /*
     * We may have case-insensitive name collision! A C program may have
     * defined structs/unions/typedefs with the names FooS, fooS, FoOs, fOOs.
     * Because we map structs/unions/typedefs to nested classes of header classes,
     * such a case-insensitive name collision is problematic. This is because in
     * a case-insensitive file system javac will overwrite classes for
     * Header$CFooS, Header$CfooS, Header$CFoOs and so on! We solve this by
     * generating unique case-insensitive names for nested classes.
     */
    private String uniqueNestedClassName(String name) {
        return nestedClassNames.add(name.toLowerCase())? name : (name + "$" + nestedClassNameCount++);
    }

    private String structClassName(Declaration decl) {
        return structClassNames.computeIfAbsent(decl, d -> uniqueNestedClassName("C" + d.name()));
    }

    // have we seen this Variable earlier?
    protected boolean variableSeen(Declaration.Variable tree) {
        return !variables.add(tree);
    }

    // have we seen this Function earlier?
    protected boolean functionSeen(Declaration.Function tree) {
        return !functions.add(tree);
    }

    static JavaFileObject[] generateWrapped(Declaration.Scoped decl, String clsName, String pkgName, List<String> libraryNames) {
        String qualName = pkgName.isEmpty() ? clsName : pkgName + "." + clsName;
        ConstantHelper constantHelper = new ConstantHelper(qualName,
                ClassDesc.of(pkgName, "RuntimeHelper"), ClassDesc.of(pkgName, "Cstring"),
                libraryNames.toArray(String[]::new));
        return new OutputFactory(clsName, pkgName,
                new HeaderBuilder(clsName, pkgName, constantHelper), constantHelper).generate(decl);
    }

    public OutputFactory(String clsName, String pkgName, HeaderBuilder builder, ConstantHelper constantHelper) {
        this.clsName = clsName;
        this.pkgName = pkgName;
        this.builder = builder;
        this.constantHelper = constantHelper;
    }

    private static String getCLangConstantsHolder() {
        String prefix = "jdk.incubator.foreign.SystemABI.";
        String abi = SharedUtils.getSystemABI().name();
        switch (abi) {
            case SystemABI.ABI_SYSV:
                return prefix + "SysV";
            case SystemABI.ABI_WINDOWS:
                return prefix + "Win64";
            case SystemABI.ABI_AARCH64:
                return prefix + "AArch64";
            default:
                throw new UnsupportedOperationException("Unsupported ABI: " + abi);
        }
    }

    static final String C_LANG_CONSTANTS_HOLDER = getCLangConstantsHolder();

    public JavaFileObject[] generate(Declaration.Scoped decl) {
        builder.classBegin();
        //generate all decls
        decl.members().forEach(this::generateDecl);
        for (String src : structSources) {
            builder.addContent(src);
        }
        builder.classEnd();
        try {
            List<JavaFileObject> files = new ArrayList<>();
            files.add(builder.build());
            files.addAll(constantHelper.getClasses());
            files.add(fileFromString(pkgName,"RuntimeHelper", getRuntimeHelperSource()));
            files.add(getCstringFile(pkgName));
            files.addAll(getPrimitiveTypeFiles(pkgName));
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

    private JavaFileObject getCstringFile(String pkgName) throws IOException, URISyntaxException {
        var cstringFile = OutputFactory.class.getResource("resources/Cstring.java.template");
        var lines = Files.readAllLines(Paths.get(cstringFile.toURI()));
        String pkgPrefix = pkgName.isEmpty()? "" : "package " + pkgName + ";\n";
        String contents =  pkgPrefix +
                lines.stream().collect(Collectors.joining("\n"));
        return fileFromString(pkgName,"Cstring", contents);
    }

    private List<JavaFileObject> getPrimitiveTypeFiles(String pkgName) throws IOException, URISyntaxException {
        var abi = SharedUtils.getSystemABI();
        var cXJavaFile = OutputFactory.class.getResource("resources/C-X.java.template");
        var lines = Files.readAllLines(Paths.get(cXJavaFile.toURI()));

        List<JavaFileObject> files = new ArrayList<>();
        String pkgPrefix = pkgName.isEmpty()? "" : "package " + pkgName + ";\n";
        for (Primitive.Kind type : Primitive.Kind.values()) {
            if (type.layout().isEmpty()) continue;
            String typeName = type.typeName().toLowerCase().replace(' ', '_');
            MemoryLayout layout = type.layout().get();
            String contents =  pkgPrefix +
                    lines.stream().collect(Collectors.joining("\n")).
                            replace("-X", typeName).
                            replace("${C_LANG}", C_LANG_CONSTANTS_HOLDER).
                            replace("${LAYOUT}", TypeTranslator.typeToLayoutName(type)).
                            replace("${CARRIER}", classForType(type, layout).getName());
            files.add(fileFromString(pkgName,"C" + typeName, contents));
        }
        return files;
    }

    private static Class<?> classForType(Primitive.Kind type, MemoryLayout layout) {
        boolean isFloat = switch(type) {
            case Float, Double, LongDouble -> true;
            default-> false;
        };
        return TypeTranslator.layoutToClass(isFloat, layout);
    }

    private JavaFileObject fileFromString(String pkgName, String clsName, String contents) {
        String pkgPrefix = pkgName.isEmpty() ? "" : pkgName.replaceAll("\\.", "/") + "/";
        return new SimpleJavaFileObject(URI.create(pkgPrefix + clsName + ".java"), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
                return contents;
            }
        };
    }

    @Override
    public Void visitConstant(Declaration.Constant constant, Declaration parent) {
        if (!constants.add(constant.name())) {
            //skip
            return null;
        }

        builder.addConstantGetter(Utils.javaSafeIdentifier(constant.name()), typeTranslator.getJavaType(constant.type()), constant.value());
        return null;
    }

    @Override
    public Void visitScoped(Declaration.Scoped d, Declaration parent) {
        if (d.layout().isEmpty()) {
            //skip decl-only
            return null;
        }
        boolean structClass = false;
        StructBuilder oldStructBuilder = this.structBuilder;
        if (!d.name().isEmpty() || !isRecord(parent)) {
            //only add explicit struct layout if the struct is not to be flattened inside another struct
            switch (d.kind()) {
                case STRUCT:
                case UNION: {
                    structClass = true;
                    String className = structClassName(d.name().isEmpty() ? parent : d);
                    this.structBuilder = new StructBuilder(className, pkgName, constantHelper);
                    structBuilder.incrAlign();
                    structBuilder.classBegin();
                    structBuilder.addLayoutGetter(className, d.layout().get());
                    break;
                }
            }
        }
        d.members().forEach(fieldTree -> fieldTree.accept(this, d.name().isEmpty() ? parent : d));
        if (structClass) {
            this.structBuilder.classEnd();
            structSources.add(structBuilder.getSource());
            this.structBuilder = oldStructBuilder;
        }
        return null;
    }

    @Override
    public Void visitFunction(Declaration.Function funcTree, Declaration parent) {
        if (functionSeen(funcTree)) {
            return null;
        }

        MethodType mtype = typeTranslator.getMethodType(funcTree.type());
        FunctionDescriptor descriptor = Type.descriptorFor(funcTree.type()).orElse(null);
        if (descriptor == null) {
            //abort
            return null;
        }
        String mhName = Utils.javaSafeIdentifier(funcTree.name());
        builder.addMethodHandleGetter(mhName, funcTree.name(), mtype, descriptor, funcTree.type().varargs());
        //generate static wrapper for function
        List<String> paramNames = funcTree.parameters()
                                          .stream()
                                          .map(Declaration.Variable::name)
                                          .map(p -> !p.isEmpty() ? Utils.javaSafeIdentifier(p) : p)
                                          .collect(Collectors.toList());
        builder.addStaticFunctionWrapper(Utils.javaSafeIdentifier(funcTree.name()), funcTree.name(), mtype,
                Type.descriptorFor(funcTree.type()).orElseThrow(), funcTree.type().varargs(), paramNames);
        int i = 0;
        for (Declaration.Variable param : funcTree.parameters()) {
            Type.Function f = getAsFunctionPointer(param.type());
            if (f != null) {
                String name = funcTree.name() + "$" + (param.name().isEmpty() ? "x" + i : param.name());
                name = Utils.javaSafeIdentifier(name);
                //generate functional interface
                MethodType fitype = typeTranslator.getMethodType(f);
                builder.addFunctionalInterface(name, fitype, Type.descriptorFor(f).orElseThrow());
                i++;
            }
        }
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
                            builder.emitTypedef(uniqueNestedClassName("C" + tree.name()), structClassName(s));
                        }
                    }
                    break;
                    default:
                        visitScoped(s, tree);
                }
            }
        } else if (type instanceof Type.Primitive) {
             builder.emitPrimitiveTypedef((Type.Primitive)type, uniqueNestedClassName("C" + tree.name()));
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
        MemoryLayout layout = tree.layout().orElse(Type.layoutFor(type).orElse(null));
        if (layout == null) {
            //no layout - abort
            return null;
        }
        Class<?> clazz = typeTranslator.getJavaType(type);
        if (tree.kind() == Declaration.Variable.Kind.BITFIELD || clazz == MemoryAddress.class ||
                (layout instanceof ValueLayout && layout.byteSize() > 8)) {
            //skip
            return null;
        }

        boolean isSegment = clazz == MemorySegment.class;
        MemoryLayout treeLayout = tree.layout().orElseThrow();
        if (parent != null) { //struct field
            MemoryLayout parentLayout = parentLayout(parent);
            if (isSegment) {
                structBuilder.addAddressOf(fieldName, tree.name(), treeLayout, clazz, parentLayout);
            } else {
                structBuilder.addVarHandleGetter(fieldName, tree.name(), treeLayout, clazz, parentLayout);
                structBuilder.addGetter(fieldName, tree.name(), treeLayout, clazz, parentLayout);
                structBuilder.addSetter(fieldName, tree.name(), treeLayout, clazz, parentLayout);
            }
        } else {
            if (isSegment) {
                builder.addAddressOf(fieldName, tree.name(), treeLayout, clazz, null);
            } else {
                builder.addLayoutGetter(fieldName, layout);
                builder.addVarHandleGetter(fieldName, tree.name(), treeLayout, clazz,null);
                builder.addAddressGetter(fieldName, tree.name(), treeLayout);
                builder.addGetter(fieldName, tree.name(), treeLayout, clazz, null);
                builder.addSetter(fieldName, tree.name(), treeLayout, clazz, null);
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
}
