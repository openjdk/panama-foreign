/*
 *  Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.
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
 *  Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 */

package com.sun.tools.jextract.parser;

import com.sun.tools.jextract.Context;
import com.sun.tools.jextract.JType;
import com.sun.tools.jextract.Log;
import com.sun.tools.jextract.TypeDictionary;
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
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Stream;

public class MacroParser {

    private final TypeDictionary dictionary = new TypeDictionary(null, null);
    private Reparser reparser;
    private Log log;

    public MacroParser(TranslationUnit tu, Log log) {
        try {
            this.reparser = new ClangReparser(tu);
            this.log = log;
        } catch (IOException ex) {
            this.reparser = Reparser.DUMMY;
        }
    }

    /**
     * This method attempts to evaluate the macro. Evaluation occurs in two steps: first, an attempt is made
     * to see if the macro corresponds to a simple numeric constant. If so, the constant is parsed in Java directly.
     * If that is not possible (e.g. because the macro refers to other macro, or has a more complex grammar), fall
     * back to use clang evaluation support.
     */
    Optional<Macro> eval(Cursor macro, String... tokens) {
        if (tokens.length == 2) {
            //check for fast path
            Integer num = toNumber(tokens[1]);
            if (num != null) {
                return Optional.of(Macro.longMacro(JType.Int, num));
            }
        }
        //slow path
        try {
            //step one, parse constant as is
            MacroResult result = reparse(constantDecl(macro.spelling(), false));
            if (!result.success() &&
                    result.type().getDescriptor().equals("Ljava/foreign/memory/Pointer;")) {
                //step two, attempt parsing pointer constant, by forcing it to uintptr_t
                result = reparse(constantDecl(macro.spelling(), true))
                        .withType(result.type());
            }
            return result.success() ?
                    Optional.of((Macro)result) :
                    Optional.empty();
        } catch (Throwable ex) {
            log.print(Level.FINE, () ->
                    String.format("Unknown error when processing macro: %s",
                            macro.spelling()));
            return Optional.empty();
        }
    }

    MacroResult reparse(String snippet) {
        return reparser.reparse(snippet)
                .filter(c -> c.kind() == CursorKind.VarDecl &&
                        c.spelling().contains("jextract$"))
                .map(c -> compute(c))
                .findAny().get();
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
            JType jtype = dictionary.enterIfAbsent(decl.type().canonicalType());
            switch (result.getKind()) {
                case Integral: {
                    long value = result.getAsInt();
                    return Macro.longMacro(jtype, value);
                }
                case FloatingPoint: {
                    double value = result.getAsFloat();
                    return Macro.doubleMacro(jtype, value);
                }
                case StrLiteral: {
                    String value = result.getAsString();
                    return Macro.stringMacro(jtype, value);
                }
                default:
                    log.print(Level.FINE, () ->
                            String.format("Error when processing macro snippet:\n%s\nUnexpected type: %s",
                                    decl.spelling(),
                                    decl.type().canonicalType()));
                    return new Failure(jtype);
            }
        }
    }

    static abstract class MacroResult {
        JType type;

        MacroResult(JType type) {
            this.type = type;
        }

        public JType type() {
            return type;
        }

        abstract boolean success();

        abstract MacroResult withType(JType type);
    }

    static class Failure extends MacroResult {
        Failure(JType type) {
            super(type);
        }

        @Override
        boolean success() {
            return false;
        }

        @Override
        MacroResult withType(JType type) {
            return new Failure(type);
        }
    }

    public static class Macro extends MacroResult {
        Object value;

        private Macro(JType type, Object value) {
            super(type);
            this.value = value;
        }

        @Override
        boolean success() {
            return true;
        }

        @Override
        MacroResult withType(JType type) {
            return new Macro(type, value);
        }

        @Override
        public JType type() {
            return type;
        }

        public Object value() {
            return value;
        }

        static Macro longMacro(JType type, long value) {
            return new Macro(type, value);
        }

        static Macro doubleMacro(JType type, double value) {
            return new Macro(type, value);
        }

        static Macro stringMacro(JType type, String value) {
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

        public ClangReparser(TranslationUnit tu) throws IOException {
            Path precompiled = Files.createTempFile("jextract$", ".pch");
            precompiled.toFile().deleteOnExit();
            tu.save(precompiled);
            this.macro = Files.createTempFile("jextract$", ".h");
            this.macro.toFile().deleteOnExit();
            this.macroUnit = macroIndex.parse(macro.toAbsolutePath().toString(),
                    d -> processDiagnostics(null, d),
                    false, //add serialization support (needed for macros)
                    "-I", Context.getBuiltinHeadersDir().toString(),
                    "-include-pch", precompiled.toAbsolutePath().toString()).getTranslationUnit();
        }

        @Override
        public Stream<Cursor> reparse(String snippet) {
            macroIndex.reparse(d -> processDiagnostics(snippet, d), macroUnit,
                    Index.UnsavedFile.of(macro, snippet));
            return macroUnit.getCursor().children();
        }

        void processDiagnostics(String snippet, Diagnostic diag) {
            if (diag.severity() > Diagnostic.CXDiagnostic_Warning) {
                log.print(Level.FINE, () ->
                        String.format("Error when processing macro snippet:\n%s\nCause: %s",
                                snippet,
                                diag.spelling()));
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
