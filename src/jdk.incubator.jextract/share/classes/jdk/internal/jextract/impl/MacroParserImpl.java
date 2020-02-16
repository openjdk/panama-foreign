/*
 *  Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.  Oracle designates this
 *  particular file as subject to the "Classpath" exception as provided
 *  by Oracle in the LICENSE file that accompanied this code.
 *
 *  This code is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  version 2 for more details (a copy is included in the LICENSE file that
 *  accompanied this code).
 *
 *  You should have received a copy of the GNU General Public License version
 *  2 along with this work; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *   Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 *
 */

package jdk.internal.jextract.impl;

import jdk.incubator.jextract.Type;
import jdk.internal.clang.Cursor;
import jdk.internal.clang.CursorKind;
import jdk.internal.clang.Diagnostic;
import jdk.internal.clang.EvalResult;
import jdk.internal.clang.Index;
import jdk.internal.clang.LibClang;
import jdk.internal.clang.TranslationUnit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

class MacroParserImpl {

    private Reparser reparser;
    TypeMaker typeMaker = new TypeMaker(null);

    public MacroParserImpl(TranslationUnit tu, Collection<String> args) {
        try {
            this.reparser = new ClangReparser(tu, args);
        } catch (IOException | Index.ParsingFailedException ex) {
            this.reparser = Reparser.DUMMY;
        }
    }

    /**
     * This method attempts to evaluate the macro. Evaluation occurs in two steps: first, an attempt is made
     * to see if the macro corresponds to a simple numeric constant. If so, the constant is parsed in Java directly.
     * If that is not possible (e.g. because the macro refers to other macro, or has a more complex grammar), fall
     * back to use clang evaluation support.
     */
    Optional<Macro> eval(String macroName, String... tokens) {
        if (tokens.length == 2) {
            //check for fast path
            Integer num = toNumber(tokens[1]);
            if (num != null) {
                return Optional.of(Macro.longMacro(Type.primitive(Type.Primitive.Kind.Int, LayoutUtils.C_INT), num));
            }
        }
        //slow path
        try {
            //step one, parse constant as is
            MacroResult result = reparse(constantDecl(macroName, false));
            if (!result.success()) {
                //step two, attempt parsing pointer constant, by forcing it to uintptr_t
                result = reparse(constantDecl(macroName, true)).asType(result.type);
            }
            return result.success() ?
                    Optional.of((Macro)result) :
                    Optional.empty();
        } catch (Throwable ex) {
            // This ate the NPE and cause skip of macros
            // Why are we expecting exception here? Simply be defensive?
            if (JextractTaskImpl.VERBOSE) {
                System.err.println("Failed to handle macro " + macroName);
                ex.printStackTrace(System.err);
            }
            return Optional.empty();
        }
    }

    MacroResult reparse(String snippet) {
        MacroResult rv = reparser.reparse(snippet)
                .filter(c -> c.kind() == CursorKind.VarDecl &&
                        c.spelling().contains("jextract$"))
                .map(c -> compute(c))
                .findAny().get();
        typeMaker.resolveTypeReferences();
        return rv;
    }

    private Integer toNumber(String str) {
        try {
            // Integer.decode supports '#' hex literals which is not valid in C.
            return str.length() > 0 && str.charAt(0) != '#'? Integer.decode(str) : null;
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    String constantDecl(String macroName, boolean forcePtr) {
        //we use __auto_type, so that clang will also do type inference for us
        return (forcePtr) ?
                "#include <stdint.h> \n __auto_type jextract$macro$ptr$" + macroName + " = (uintptr_t)" + macroName + ";" :
                "__auto_type jextract$macro$" + macroName + " = " + macroName + ";";
    }

    MacroResult compute(Cursor decl) {
        try (EvalResult result = decl.eval()) {
            switch (result.getKind()) {
                case Integral: {
                    long value = result.getAsInt();
                    return Macro.longMacro(typeMaker.makeType(decl.type()), value);
                }
                case FloatingPoint: {
                    double value = result.getAsFloat();
                    return Macro.doubleMacro(typeMaker.makeType(decl.type()), value);
                }
                case StrLiteral: {
                    String value = result.getAsString();
                    return Macro.stringMacro(typeMaker.makeType(decl.type()), value);
                }
                default:
                    return new Failure(typeMaker.makeType(decl.type()));
            }
        }
    }

    static abstract class MacroResult {
        private final Type type;

        MacroResult(Type type) {
            this.type = type;
        }

        public Type type() {
            return type;
        }

        abstract boolean success();

        abstract MacroResult asType(Type type);
    }

    static class Failure extends MacroResult {

        public Failure(Type type) {
            super(type);
        }

        @Override
        boolean success() {
            return false;
        }

        @Override
        MacroResult asType(Type type) {
            return new Failure(type);
        }
    }

    public static class Macro extends MacroResult {
        Object value;

        private Macro(Type type, Object value) {
            super(type);
            this.value = value;
        }

        @Override
        boolean success() {
            return true;
        }

        public Object value() {
            return value;
        }

        @Override
        MacroResult asType(Type type) {
            return new Macro(type, value);
        }

        static Macro longMacro(Type type, long value) {
            return new Macro(type, value);
        }

        static Macro doubleMacro(Type type, double value) {
            return new Macro(type, value);
        }

        static Macro stringMacro(Type type, String value) {
            return new Macro(type, value);
        }
    }

    interface Reparser {
        Stream<Cursor> reparse(String snippet);

        Reparser DUMMY = s -> Stream.empty();
    }

    /**
     * This class allows client to reparse a snippet of code against a given set of include files.
     * For performance reasons, the set of includes (which comes from the jextract parser) is compiled
     * into a precompiled header, so as to speed to incremental recompilation of the generated snippets.
     */
    class ClangReparser implements Reparser {
        final Path macro;
        final Index macroIndex = LibClang.createIndex(true);
        final TranslationUnit macroUnit;

        public ClangReparser(TranslationUnit tu, Collection<String> args) throws IOException, Index.ParsingFailedException {
            Path precompiled = Files.createTempFile("jextract$", ".pch");
            precompiled.toFile().deleteOnExit();
            tu.save(precompiled);
            this.macro = Files.createTempFile("jextract$", ".h");
            this.macro.toFile().deleteOnExit();
            String[] patchedArgs = Stream.concat(
                Stream.of(
                    // Avoid system search path, use bundled instead
                    "-nostdinc",
                    // precompiled header
                    "-include-pch", precompiled.toAbsolutePath().toString()),
                args.stream()).toArray(String[]::new);
            this.macroUnit = macroIndex.parse(macro.toAbsolutePath().toString(),
                    d -> processDiagnostics(null, d),
                    false, //add serialization support (needed for macros)
                    patchedArgs);
        }

        @Override
        public Stream<Cursor> reparse(String snippet) {
            macroUnit.reparse(d -> processDiagnostics(snippet, d),
                    Index.UnsavedFile.of(macro, snippet));
            return macroUnit.getCursor().children();
        }

        void processDiagnostics(String snippet, Diagnostic diag) {
            if (diag.severity() > Diagnostic.CXDiagnostic_Warning) {
                throw new BadMacroException(diag);
            }
        }
    }

    private static class BadMacroException extends RuntimeException {
        static final long serialVersionUID = 1L;

        public BadMacroException(Diagnostic diag) {
            super(diag.toString());
        }
    }
}
