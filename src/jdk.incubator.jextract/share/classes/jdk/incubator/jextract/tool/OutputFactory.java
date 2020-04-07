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

import jdk.incubator.jextract.Declaration;
import jdk.incubator.jextract.Type;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.SystemABI;
import jdk.internal.foreign.InternalForeign;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.io.IOException;
import java.io.UncheckedIOException;
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

    private final Set<String> structsAndVars = new HashSet<>();
    private final Map<String, String> mangledNames = new HashMap<>();

    protected final JavaSourceBuilder builder;
    protected final TypeTranslator typeTranslator = new TypeTranslator();
    private final String clsName;
    private final String pkgName;

    // have we seen this Variable earlier?
    protected boolean variableSeen(Declaration.Variable tree) {
        return !variables.add(tree);
    }

    // have we seen this Function earlier?
    protected boolean functionSeen(Declaration.Function tree) {
        return !functions.add(tree);
    }

    // have we visited a struct/union or a global variable of given name?
    protected boolean structOrVariableSeen(String name) {
        return !structsAndVars.add(name);
    }

    private void setMangledName(String name, String prefix) {
        if (!name.isEmpty() && structOrVariableSeen(name)) {
            mangledNames.put(name, prefix + name);
        }
    }

    protected void setMangledName(Declaration.Scoped d) {
        switch (d.kind()) {
            case STRUCT:
                setMangledName(d.name(), "struct$");
                break;
            case UNION:
                setMangledName(d.name(), "union$");
                break;
        }
    }

    protected void setMangledName(Declaration.Variable v) {
        setMangledName(v.name(), "var$");
    }

    protected String getMangledName(Declaration d) {
        String name = d.name();
        return name.isEmpty()? name : mangledNames.getOrDefault(name, name);
    }

    static JavaFileObject[] generateWrapped(Declaration.Scoped decl, String clsName, String pkgName, List<String> libraryNames) {
        return new OutputFactory(clsName, pkgName, libraryNames,
                new JavaSourceBuilder(pkgName, libraryNames.toArray(String[]::new))).generate(decl);
    }

    public OutputFactory(String clsName, String pkgName, List<String> libraryNames, JavaSourceBuilder builder) {
        this.clsName = clsName;
        this.pkgName = pkgName;
        this.builder = builder;
    }

    private static String getCLangConstantsHolder() {
        String prefix = "jdk.incubator.foreign.MemoryLayouts.";
        String abi = InternalForeign.getInstancePrivileged().getSystemABI().name();
        switch (abi) {
            case SystemABI.ABI_SYSV:
                return prefix + "SysV";
            case SystemABI.ABI_WINDOWS:
                return prefix + "WinABI";
            case SystemABI.ABI_AARCH64:
                return prefix + "AArch64ABI";
            default:
                throw new UnsupportedOperationException("Unsupported ABI: " + abi);
        }
    }

    static final String C_LANG_CONSTANTS_HOLDER = getCLangConstantsHolder();

    public JavaFileObject[] generate(Declaration.Scoped decl) {
        builder.classBegin(clsName);
        //generate all decls
        decl.members().forEach(this::generateDecl);

        builder.classEnd();
        List<JavaFileObject> outputs = builder.build();
        try {
            List<JavaFileObject> files = new ArrayList<>(outputs);
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
        var abi = InternalForeign.getInstancePrivileged().getSystemABI();
        var cXJavaFile = OutputFactory.class.getResource("resources/C-X.java.template");
        var lines = Files.readAllLines(Paths.get(cXJavaFile.toURI()));

        List<JavaFileObject> files = new ArrayList<>();
        String pkgPrefix = pkgName.isEmpty()? "" : "package " + pkgName + ";\n";
        for (SystemABI.Type type : SystemABI.Type.values()) {
            // FIXME: ignore pointer and complex type
            if (type == SystemABI.Type.POINTER || type == SystemABI.Type.COMPLEX_LONG_DOUBLE) {
                continue;
            }

            String typeName = type.name().toLowerCase();
            MemoryLayout layout = abi.layoutFor(type).get();
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

    private static Class<?> classForType(SystemABI.Type type, MemoryLayout layout) {
        boolean isFloat = switch(type) {
            case FLOAT, DOUBLE, LONG_DOUBLE -> true;
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
        if (d.kind() == Declaration.Scoped.Kind.TYPEDEF) {
            return d.members().get(0).accept(this, d);
        }
        if (d.layout().isEmpty()) {
            //skip decl-only
            return null;
        }
        String name;
        // FIXME: we need tree transformer. The mangling should be a separate tree transform phase
        if (d.name().isEmpty() && parent != null) {
            name = getMangledName(parent);
        } else {
            setMangledName(d);
            name = getMangledName(d);
        }

        if (!d.name().isEmpty() || !isRecord(parent)) {
            //only add explicit struct layout if the struct is not to be flattened inside another struct
            switch (d.kind()) {
                case STRUCT:
                case UNION: {
                    builder.addLayoutGetter(Utils.javaSafeIdentifier(name), d.layout().get());
                    break;
                }
            }
        }
        d.members().forEach(fieldTree -> fieldTree.accept(this, d.name().isEmpty() ? parent : d));
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
                builder.addFunctionalInterface(name, fitype);
                //generate helper
                builder.addFunctionalFactory(name, fitype, Type.descriptorFor(f).orElseThrow());
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
    public Void visitVariable(Declaration.Variable tree, Declaration parent) {
        if (parent == null && variableSeen(tree)) {
            return null;
        }

        String fieldName = tree.name();
        String symbol = tree.name();
        assert !symbol.isEmpty();
        assert !fieldName.isEmpty();

        // FIXME: we need tree transformer. The mangling should be a separate tree transform phase
        if (parent == null) {
            setMangledName(tree);
            fieldName = getMangledName(tree);
        }
        fieldName = Utils.javaSafeIdentifier(fieldName);

        Type type = tree.type();
        MemoryLayout layout = tree.layout().orElse(Type.layoutFor(type).orElse(null));
        if (layout == null) {
            //no layout - abort
            return null;
        }
        Class<?> clazz = typeTranslator.getJavaType(type);
        if (tree.kind() == Declaration.Variable.Kind.BITFIELD || clazz == MemoryAddress.class ||
                clazz == MemorySegment.class || layout.byteSize() > 8) {
            //skip
            return null;
        }

        MemoryLayout treeLayout = tree.layout().orElseThrow();
        if (parent != null) { //struct field
            Declaration.Scoped parentC = (Declaration.Scoped) parent;
            String parentName = Utils.javaSafeIdentifier(getMangledName(parentC));
            fieldName = parentName + "$" + fieldName;
            MemoryLayout parentLayout = parentLayout(parentC);
            builder.addVarHandleGetter(fieldName, tree.name(), treeLayout, clazz, parentLayout);
            builder.addGetter(fieldName, tree.name(), treeLayout, clazz, parentLayout);
            builder.addSetter(fieldName, tree.name(), treeLayout, clazz, parentLayout);
        } else {
            builder.addLayoutGetter(fieldName, layout);
            builder.addVarHandleGetter(fieldName, tree.name(), treeLayout, clazz, null);
            builder.addAddressGetter(fieldName, tree.name());
            builder.addGetter(fieldName, tree.name(), treeLayout, clazz, null);
            builder.addSetter(fieldName, tree.name(), treeLayout, clazz, null);
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

    protected static MemoryLayout parentLayout(Declaration.Scoped parent) {
        // case like `typedef struct { ... } Foo`
        return (parent.kind() == Declaration.Scoped.Kind.TYPEDEF
            ? (Declaration.Scoped) parent.members().get(0)
            : parent).layout().orElseThrow();
    }
}
