/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.tools.jextract;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import jdk.internal.clang.Cursor;
import jdk.internal.clang.CursorKind;
import jdk.internal.clang.Type;
import jdk.internal.clang.TypeKind;

/**
 * This class represent a native code header file
 */
public final class HeaderFile {
    private final Context ctx;
    final Path path;
    final String pkgName;
    final String clsName;
    private final TypeDictionary dict;
    // The top header file cause this file to be parsed
    private HeaderFile main;
    private AsmCodeFactory cf;
    List<String> libraries; // immutable
    List<String> libraryPaths; // immutable

    private final AtomicInteger serialNo;

    private final Logger logger = Logger.getLogger(getClass().getPackage().getName());

    HeaderFile(Context ctx, Path path, String pkgName, String clsName, HeaderFile main) {
        this.ctx = ctx;
        this.path = path;
        this.pkgName = pkgName;
        this.clsName = clsName;
        dict = ctx.typeDictionaryFor(pkgName);
        serialNo = new AtomicInteger();
        this.main = main == null ? this : main;
    }

    void useLibraries(List<String> libraries, List<String> libraryPaths) {
        this.libraries = Collections.unmodifiableList(libraries);
        this.libraryPaths = Collections.unmodifiableList(libraryPaths);
    }

    AsmCodeFactory getCodeFactory() {
        return cf;
    }

    /**
     * Call this function to enable code generation for this HeaderFile.
     * This function should only be called once to turn on code generation and before process any cursor.
     * @param cf The CodeFactory used to generate code
     */
    void useCodeFactory(AsmCodeFactory cf) {
        if (null != this.cf) {
            logger.config(() -> "CodeFactory had been initialized for " + path);
            // Diagnosis code
            if (Main.DEBUG) {
                new Throwable().printStackTrace(ctx.err);
            }
        } else {
            this.cf = cf;
        }
    }

    @Override
    public String toString() {
        return "HeaderFile(path=" + path + ")";
    }

    private int serialNo() {
        return serialNo.incrementAndGet();
    }

    void processCursor(Cursor c, HeaderFile main, boolean isBuiltIn) {
        if (c.isDeclaration()) {
            logger.finest(() -> "Looking at cursor " + c.spelling() + " of kind " + c.kind());
            if (c.kind() == CursorKind.UnexposedDecl ||
                    c.kind() == CursorKind.Namespace) {
                c.children()
                        .filter(c1 -> c1.isDeclaration())
                        .peek(c1 -> logger.finest(
                                () -> "Cursor: " + c1.spelling() + "@" + c1.USR() + "?" + c1.isDeclaration()))
                        .forEach(c1 -> processCursor(c1, main, isBuiltIn));
                return;
            }
            Type t = c.type();
            if (t.kind() == TypeKind.FunctionProto ||
                t.kind() == TypeKind.FunctionNoProto) {
                String name = c.spelling();

                if (ctx.isSymbolExcluded(name)) {
                    return;
                }

                if (!ctx.isSymbolFound(name)) {
                    ctx.err.println(Main.format("warn.symbol.not.found", name));
                }
            }

            // C structs and unions can have nested structs, unions and enums.
            // And nested types are hoisted to the containing scope - i.e., nested
            // structs/unions/enums are not scoped types. We recursively visit all
            // nested types.
            if (c.kind() == CursorKind.StructDecl ||
                c.kind() == CursorKind.UnionDecl) {
                c.children()
                    .filter(c1 -> c1.isDeclaration() &&
                         (c1.kind() == CursorKind.StructDecl ||
                         c1.kind() == CursorKind.UnionDecl ||
                         c1.kind() == CursorKind.EnumDecl))
                    .peek(c1 -> logger.finest(
                         () -> "Cursor: " + c1.spelling() + "@" + c1.USR() + "?" + c1.isDeclaration()))
                    .forEach(c1 -> processCursor(c1, main, isBuiltIn));
            }

            // generate nothing for anonymous structs/unions. Fields are generated in the
            // containing struct or union
            if (c.isAnonymousStruct()) {
                return;
            }

            JType jt = dict.computeIfAbsent(t, type -> {
                logger.fine(() -> "PH: Compute type for " + type.spelling());
                return define(type);
            });
            assert (jt instanceof JType2);

            if (t.kind() == TypeKind.Typedef &&
                    t.canonicalType().kind() == TypeKind.Enum &&
                    t.spelling().equals(t.canonicalType().getDeclarationCursor().spelling()))
            {
                logger.fine("Skip redundant typedef " + t.spelling());
                return;
            }

            // Only main file can define interface
            if (cf != null && this.main == main) {
                cf.addType(jt, c);
            }
        } else if (c.isPreprocessing()) {
            if (c.kind() == CursorKind.MacroDefinition && !isBuiltIn) {
                ctx.defineMacro(c);
            }
        }
    }

    JType globalLookup(Type type) {
        JType jt;
        try {
            jt = dict.lookup(type);
            if (null == jt) {
                jt = dict.computeIfAbsent(type, this::define);
            }
        } catch (TypeDictionary.NotDeclaredException ex) {
            // The type has no declaration, consider it local defined
            jt = dict.computeIfAbsent(type, this::define);
        }
        return jt;
    }

    /**
     * Local lookup, the type is assumed to be locally defined. Use
     * TypeDictionary.lookup(Type) for a global lookup or Context.getJType(Cursor)
     *
     * @param type
     * @return
     * @see TypeDictionary#lookup(Type)
     */
    JType localLookup(Type type) {
        return dict.computeIfAbsent(type, this::define);
    }

    private JType doRecord(Type t) {
        assert(t.kind() == TypeKind.Record);
        String name = Utils.toClassName(Utils.getIdentifier(t));
        Cursor dcl = t.getDeclarationCursor();
        // Define record locally but not declared in this file, likely a built-in type.
        // __builtin_va_list is such a type.
        boolean gen_code = (cf != null) && (dcl.getSourceLocation().getFileLocation().path() == null);
        JType2 jt;
        // case of #typedef struct Foo Bar, struct Foo is a Record type
        // as no definition found, we consider it an annotation
        Cursor defC = dcl.getDefinition();
        jt = JType2.bind(
                new JType.InnerType(Utils.toInternalName(pkgName, clsName), name),
                t, defC.isInvalid() ? dcl : defC);
        if (gen_code) {
            cf.addType(jt, defC);
        }
        return jt;
    }

    // Use of dict.lookup() and lookup() is tricky, if a type should have being
    // declared earlier, use dict.lookup(); otherwise use lookup() for potentially
    // local declaration of a type.
    JType define(Type t) {
        JType jt;
        JType2 jt2;
        logger.fine("Define " + t.kind() + ":" + t.spelling() + " for TD " + pkgName);
        switch (t.kind()) {
            case Unexposed:
            case Elaborated:
                jt = define(t.canonicalType());
                break;
            case ConstantArray:
                jt = JType.ofArray(globalLookup(t.getElementType()));
                break;
            case IncompleteArray:
                jt = JType.ofArray(globalLookup(t.getElementType()));
                break;
            case FunctionProto:
            case FunctionNoProto:
                JType[] args = new JType[t.numberOfArgs()];
                for (int i = 0; i < args.length; i++) {
                    // argument could be function pointer declared locally
                    args[i] = globalLookup(t.argType(i));
                }
                jt = new JType.Function(Utils.getFunction(t), t.isVariadic(), globalLookup(t.resultType()), args);
                break;
            case Enum:
                String name = Utils.toInternalName(pkgName, clsName,
                        Utils.toClassName(Utils.getIdentifier(t)));
                jt = TypeAlias.of(name, JType.Int);
                break;
            case Invalid:
                throw new IllegalArgumentException("Invalid type");
            case Record:
                jt = doRecord(t);
                break;
            case Pointer:
                Type pointee = t.getPointeeType().canonicalType();
                jt2 = (JType2) globalLookup(pointee);
                jt = jt2.getDelegate();
                if (jt instanceof JType.Function) {
                    jt = new JType.FnIf(new JType.InnerType(
                                Utils.toInternalName(pkgName, clsName),
                                "FI" + serialNo()),
                            (JType.Function) jt);
                    if (cf != null) {
                        cf.addType(JType2.bind(jt, t, null), null);
                    }
                } else {
                    jt = new PointerType(jt);
                }
                break;
            case Typedef:
                Type truetype = t.canonicalType();
                logger.fine(() -> "Typedef " + t.spelling() + " as " + truetype.spelling());
                name = Utils.toInternalName(pkgName, clsName,
                        Utils.toClassName(t.spelling()));
                jt = TypeAlias.of(name, globalLookup(truetype));
                break;
            case BlockPointer:
                // FIXME: what is BlockPointer? A FunctionalPointer as this is closure
                pointee = t.getPointeeType();
                jt2 = (JType2) globalLookup(pointee);
                jt = jt2.getDelegate();
                jt = new JType.FnIf(new JType.InnerType(
                            Utils.toInternalName(pkgName, clsName),
                            "FI" + serialNo()),
                        (JType.Function) jt);
                if (cf != null) {
                    cf.addType(JType2.bind(jt, t, null), null);
                }
                break;
            default:
                throw new UnsupportedOperationException("Type kind not supported: " + t.kind());
        }

        final JType finalJt = jt;
        logger.config(() -> "Type " + t.spelling() + " defined as " + finalJt.getSignature());
        return (jt instanceof JType2) ? jt : JType2.bind(jt, t, t.getDeclarationCursor());
    }
}
