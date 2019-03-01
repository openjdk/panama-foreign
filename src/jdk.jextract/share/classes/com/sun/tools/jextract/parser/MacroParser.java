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

import com.sun.tools.jextract.JType;
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
import java.util.stream.Stream;

class MacroParser {

    private final TypeDictionary dictionary = new TypeDictionary(null, null);
    private Reparser reparser;

    public MacroParser(TranslationUnit tu) {
        try {
            this.reparser = new ClangReparser(tu);
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
    Optional<Object> eval(Cursor macro, String... tokens) {
        if (tokens.length == 2) {
            //check for fast path
            Number num = toNumber(tokens[1]);
            if (num != null) {
                return Optional.of(num);
            }
        }
        //slow path
        try {
            return reparser.reparse(constantDecl(macro))
                    .filter(c -> c.kind() == CursorKind.VarDecl)
                    .findAny().map(this::computeValue);
        } catch (Throwable ex) {
            return Optional.empty();
        }
    }

    private Number toNumber(String str) {
        try {
            // Integer.decode supports '#' hex literals which is not valid in C.
            return str.length() > 0 && str.charAt(0) != '#'? Integer.decode(str) : null;
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    String constantDecl(Cursor macro) {
        String macroName = macro.spelling();
        //we use __auto_type, so that clang will also do type inference for us
        return "__auto_type jextract$macro$" + macroName + " = " + macroName + ";";
    }

    Object computeValue(Cursor decl) {
        try (EvalResult result = decl.eval()) {
            JType jtype = dictionary.enterIfAbsent(decl.type().canonicalType());
            switch (result.getKind()) {
                case Integral: {
                    long value = result.getAsInt();
                    switch (jtype.getDescriptor()) {
                        case "Z":
                            return value == 1;
                        case "B":
                            return (byte) value;
                        case "C":
                            return (char) value;
                        case "S":
                            return (short) value;
                        case "I":
                            return (int) value;
                        case "J":
                            return value;
                        default:
                            throw new IllegalStateException("Unexpected type: " + jtype.getDescriptor());
                    }
                }
                case FloatingPoint: {
                    double value = result.getAsFloat();
                    switch (jtype.getDescriptor()) {
                        case "F":
                            return (float) value;
                        case "D":
                            return value;
                        default:
                            throw new IllegalStateException("Unexpected type: " + jtype.getDescriptor());
                    }
                }
                case StrLiteral:
                    return result.getAsString();
                default:
                    return null;
            }
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
    static class ClangReparser implements Reparser {
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
                    BadMacroException::new,
                    false, //add serialization support (needed for macros)
                    "-include-pch", precompiled.toAbsolutePath().toString()).getTranslationUnit();
        }

        @Override
        public Stream<Cursor> reparse(String snippet) {
            macroIndex.reparse(BadMacroException::new, macroUnit,
                    Index.UnsavedFile.of(macro, snippet));
            return macroUnit.getCursor().children();
        }
    }

    private static class BadMacroException extends RuntimeException {
        static final long serialVersionUID = 1L;

        public BadMacroException(Diagnostic diag) {
            super(diag.toString());
        }
    }
}
