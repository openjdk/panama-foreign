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
public class HandleSourceFactory implements Declaration.Visitor<Void, Declaration> {
    private final Set<String> constants = new HashSet<>();
    // To detect duplicate Variable and Function declarations.
    private final Set<Declaration.Variable> variables = new HashSet<>();
    private final Set<Declaration.Function> functions = new HashSet<>();

    private final Set<String> structsAndVars = new HashSet<>();
    private final Map<String, String> mangledNames = new HashMap<>();

    protected final JavaSourceBuilder builder = new JavaSourceBuilder();
    protected final TypeTranslator typeTranslator = new TypeTranslator();
    private final List<String> libraryNames;
    private final String clsName;
    private final String pkgName;

    // have we visited this Variable earlier?
    protected boolean visitedVariable(Declaration.Variable tree) {
        return !variables.add(tree);
    }

    // have we visited this Function earlier?
    protected boolean visitedFunction(Declaration.Function tree) {
        return !functions.add(tree);
    }

    // have we visited a struct/union or a global variable of given name?
    protected boolean visitedStructOrVariable(String name) {
        return !structsAndVars.add(name);
    }

    private void setMangledName(String name, String prefix) {
        if (!name.isEmpty() && visitedStructOrVariable(name)) {
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

    static JavaFileObject[] generateRaw(Declaration.Scoped decl, String clsName, String pkgName, List<String> libraryNames) {
        return new HandleSourceFactory(clsName, pkgName, libraryNames).generate(decl);
    }

    static JavaFileObject[] generateWrapped(Declaration.Scoped decl, String clsName, String pkgName, List<String> libraryNames) {
        return new StaticWrapperSourceFactory(clsName, pkgName, libraryNames).generate(decl);
    }

    public HandleSourceFactory(String clsName, String pkgName, List<String> libraryNames) {
        this.libraryNames = libraryNames;
        this.clsName = clsName;
        this.pkgName = pkgName;
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
        builder.addPackagePrefix(pkgName);
        builder.classBegin(clsName);
        builder.addLibraries(libraryNames.toArray(new String[0]));
        //generate all decls
        decl.members().forEach(this::generateDecl);

        //generate functional interfaces
        generateFunctionalInterfaces(decl);

        builder.classEnd();
        String src = builder.build();

        URL runtimeHelper = HandleSourceFactory.class.getResource("resources/RuntimeHelper.java.template");

        try {
            List<JavaFileObject> files = new ArrayList<>();
            files.add(fileFromString(pkgName, clsName, src));
            files.add(fileFromString(pkgName,"RuntimeHelper", (pkgName.isEmpty()? "" : "package " + pkgName + ";\n") +
                            Files.readAllLines(Paths.get(runtimeHelper.toURI()))
                            .stream().collect(Collectors.joining("\n")).replace("${C_LANG}", C_LANG_CONSTANTS_HOLDER)));
            files.add(getCstringFile(pkgName));
            files.addAll(getPrimitiveTypeFiles(pkgName));
            return files.toArray(new JavaFileObject[0]);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (URISyntaxException ex2) {
            throw new RuntimeException(ex2);
        }
    }

    protected void generateFunctionalInterfaces(Declaration.Scoped decl) {
        //generate functional interfaces
        Set<FunctionDescriptor> functionalInterfaces = new HashSet<>();
        new FunctionalInterfaceScanner(functionalInterfaces).scan(decl);
        functionalInterfaces.forEach(builder::addUpcallFactory);
    }

    private void generateDecl(Declaration tree) {
        try {
            tree.accept(this, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private JavaFileObject getCstringFile(String pkgName) throws IOException, URISyntaxException {
        var cstringFile = HandleSourceFactory.class.getResource("resources/Cstring.java.template");
        var lines = Files.readAllLines(Paths.get(cstringFile.toURI()));
        String pkgPrefix = pkgName.isEmpty()? "" : "package " + pkgName + ";\n";
        String contents =  pkgPrefix +
                lines.stream().collect(Collectors.joining("\n"));
        return fileFromString(pkgName,"Cstring", contents);
    }

    private List<JavaFileObject> getPrimitiveTypeFiles(String pkgName) throws IOException, URISyntaxException {
        var abi = InternalForeign.getInstancePrivileged().getSystemABI();
        var cXJavaFile = HandleSourceFactory.class.getResource("resources/C-X.java.template");
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
    public Void visitVariable(Declaration.Variable tree, Declaration parent) {
        if (parent == null && visitedVariable(tree)) {
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

        if (parent != null) {
            //struct field
            builder.addVarHandle(fieldName, clazz, getMangledName(parent));
        } else {
            builder.addLayout(fieldName, layout);
            builder.addVarHandle(fieldName, clazz, null);
            builder.addAddress(fieldName);
        }

        return null;
    }

    @Override
    public Void visitFunction(Declaration.Function funcTree, Declaration parent) {
        if (visitedFunction(funcTree)) {
            return null;
        }

        FunctionDescriptor descriptor = Type.descriptorFor(funcTree.type()).orElse(null);
        if (descriptor == null) {
            //abort
        }
        MethodType mtype = typeTranslator.getMethodType(funcTree.type());
        builder.addMethodHandle(funcTree, mtype, descriptor);
        return null;
    }

    @Override
    public Void visitConstant(Declaration.Constant constant, Declaration parent) {
        if (!constants.add(constant.name())) {
            //skip
            return null;
        }

        builder.addConstant(constant.name(), typeTranslator.getJavaType(constant.type()), constant.value());
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
        String name = d.name();
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
                    builder.addLayout(name, d.layout().get());
                    break;
                }
            }
        }
        d.members().forEach(fieldTree -> fieldTree.accept(this, d.name().isEmpty() ? parent : d));
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
}
