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

import jdk.incubator.jextract.Declaration;
import jdk.incubator.jextract.JextractTask;
import jdk.incubator.jextract.Position;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class MacroParserImpl implements JextractTask.ConstantParser {

    private Reparser reparser;
    TreeMaker treeMaker = new TreeMaker();

    public MacroParserImpl(TreeMaker treeMaker, TranslationUnit tu, Collection<String> args) {
        try {
            this.reparser = new ClangReparser(tu, args);
            this.treeMaker = treeMaker;
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
                return Optional.of(Macro.longMacro(macroName, Type.primitive(Type.Primitive.Kind.Int), num));
            }
        }
        return Optional.empty();
    }

    final Map<String, UnparsedMacro> macrosToReparse = new LinkedHashMap<>();

    @Override
    public Optional<Declaration.Constant> parseConstant(Position pos, String name, String[] tokens) {
        if (!(pos instanceof TreeMaker.CursorPosition)) {
            return Optional.empty();
        } else {
            Cursor cursor = ((TreeMaker.CursorPosition)pos).cursor();
            if (cursor.isMacroFunctionLike()) {
                return Optional.empty();
            } else {
                Optional<MacroParserImpl.Macro> macro = eval(name, tokens);
                if (macro.isEmpty()) {
                    macrosToReparse.put(name, new UnparsedMacro(name, tokens, cursor));
                }
                return macro.map(m -> treeMaker.createMacro(cursor, m));
            }
        }
    }

    class UnparsedMacro {
        final String name;
        final String[] tokens;
        final Cursor cursor;

        public UnparsedMacro(String name, String[] tokens, Cursor cursor) {
            this.name = name;
            this.tokens = tokens;
            this.cursor = cursor;
        }

        String constantDecl(boolean forcePtr) {
            //we use __auto_type, so that clang will also do type inference for us
            return (forcePtr) ?
                    "__auto_type jextract$macro$ptr$" + name + " = (uintptr_t)" + name + ";" :
                    "__auto_type jextract$macro$" + name + " = " + name + ";";
        }
    }

    String macroDecl(Collection<UnparsedMacro> macros, boolean forcePtr) {
        StringBuilder buf = new StringBuilder();
        if (forcePtr) {
            buf.append("#include <stdint.h>\n");
        }
        for (UnparsedMacro m : macros) {
            buf.append(m.constantDecl(forcePtr));
            buf.append("\n");
        }
        return buf.toString();
    }

    public List<Declaration.Constant> reparseConstants() {
        //slow path
        List<Declaration.Constant> macros = new ArrayList<>();
        //step one, parse constant as is
        int last = -1;
        while (macrosToReparse.size() > 0 && macrosToReparse.size() != last) {
            Map<String, Type> pendingTypes = new LinkedHashMap<>();
            Map<String, UnparsedMacro> pendingMacros = new LinkedHashMap<>();
            last = macrosToReparse.size();
            List<MacroParserImpl.MacroResult> results = reparse(macroDecl(macrosToReparse.values(), false));
            for (MacroParserImpl.MacroResult result : results) {
                UnparsedMacro unparsedMacro = macrosToReparse.get(normalizeName(result.name));
                if (result.success()) {
                    macros.add(treeMaker.createMacro(unparsedMacro.cursor, (MacroParserImpl.Macro) result));
                    macrosToReparse.remove(normalizeName(result.name));
                } else if (result.type != null) {
                    pendingMacros.put(normalizeName(result.name), unparsedMacro);
                    pendingTypes.put(normalizeName(result.name), result.type);
                } else {
                    // this is not a recoverable failure
                    macrosToReparse.remove(normalizeName(result.name));
                }
            }
            if (!pendingMacros.isEmpty()) {
                //step two, attempt parsing pointer constant, by forcing it to uintptr_t
                results = reparse(macroDecl(pendingMacros.values(), true));
                for (MacroParserImpl.MacroResult result : results) {
                    UnparsedMacro unparsedMacro = pendingMacros.get(normalizeName(result.name));
                    if (result.success()) {
                        result = result.asType(pendingTypes.get(normalizeName(result.name)));
                        macros.add(treeMaker.createMacro(unparsedMacro.cursor, (MacroParserImpl.Macro) result));
                    }
                    // never look at this again
                    macrosToReparse.remove(normalizeName(result.name));
                }
            }
        }
        return macros;
    }

    List<MacroResult> reparse(String snippet) {
        try {
            return reparser.reparse(snippet)
                    .filter(c -> c.kind() == CursorKind.VarDecl &&
                            c.spelling().contains("jextract$"))
                    .map(c -> compute(c))
                    .collect(Collectors.toList());
        } finally {
            treeMaker.typeMaker.resolveTypeReferences();
        }
    }

    private Integer toNumber(String str) {
        try {
            // Integer.decode supports '#' hex literals which is not valid in C.
            return str.length() > 0 && str.charAt(0) != '#'? Integer.decode(str) : null;
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    MacroResult compute(Cursor decl) {
        try (EvalResult result = decl.eval()) {
            switch (result.getKind()) {
                case Integral: {
                    long value = result.getAsInt();
                    return Macro.longMacro(decl.spelling(), treeMaker.typeMaker.makeType(decl.type()), value);
                }
                case FloatingPoint: {
                    double value = result.getAsFloat();
                    return Macro.doubleMacro(decl.spelling(), treeMaker.typeMaker.makeType(decl.type()), value);
                }
                case StrLiteral: {
                    String value = result.getAsString();
                    return Macro.stringMacro(decl.spelling(), treeMaker.typeMaker.makeType(decl.type()), value);
                }
                default:
                    return new Failure(decl.spelling(), decl.type().equals(decl.type().canonicalType()) ?
                            null : treeMaker.typeMaker.makeType(decl.type()));
            }
        }
    }

    static abstract class MacroResult {
        final Type type;
        final String name;

        MacroResult(String name, Type type) {
            this.type = type;
            this.name = name;
        }

        public Type type() {
            return type;
        }

        abstract boolean success();

        abstract MacroResult asType(Type type);
    }

    static class Failure extends MacroResult {

        public Failure(String name, Type type) {
            super(name, type);
        }

        @Override
        boolean success() {
            return false;
        }

        @Override
        MacroResult asType(Type type) {
            return new Failure(name, type);
        }
    }

    public static class Macro extends MacroResult {
        Object value;

        private Macro(String name, Type type, Object value) {
            super(name, type);
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
            return new Macro(name, type, value);
        }

        static Macro longMacro(String name, Type type, long value) {
            return new Macro(name, type, value);
        }

        static Macro doubleMacro(String name, Type type, double value) {
            return new Macro(name, type, value);
        }

        static Macro stringMacro(String name, Type type, String value) {
            return new Macro(name, type, value);
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
                    "-ferror-limit=0",
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
            //System.out.println(diag.spelling());
        }
    }

    static String normalizeName(String name) {
        return name.substring(name.lastIndexOf('$') + 1);
    }
}
