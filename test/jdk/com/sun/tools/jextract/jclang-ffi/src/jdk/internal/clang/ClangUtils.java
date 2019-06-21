/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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

package jdk.internal.clang;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper functions to supplement libclang
 */
class ClangUtils {
    private static final TranslationUnit typeChecker;
    private static final Path jextractH;
    private static final Type INVALID_TYPE;

    // TU to types table
    private static final Map<TranslationUnit, Map<String, Type>> contexts = new HashMap<>();

    static {
        try {
            jextractH = Files.createTempFile("jextract", ".h");
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
        jextractH.toFile().deleteOnExit();
        Index idx = LibClang.createIndex(true);
        typeChecker = idx.parse(jextractH.toAbsolutePath().toString(), d -> {}, false);
        INVALID_TYPE = typeChecker.getCursor().type();
    }

    static Type checkBuiltinType(String spelling) {
        typeChecker.reparse(d -> {
            if (d.severity() >= Diagnostic.CXDiagnostic_Warning) {
                throw new RuntimeException("Cannot parse type " + spelling);
            }
        }, Index.UnsavedFile.of(jextractH, spelling + " arg;"));
        Type found = typeChecker.getCursor().children()
                .filter(c -> c.kind() == CursorKind.VarDecl)
                .filter(c -> c.spelling().equals("arg"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No matching cursor"))
                .type();
        return found;
    }

    static boolean isAtomicType(Type type) {
        if (type.spelling().startsWith("_Atomic(")) {
            assert type.spelling().endsWith(")");
            return true;
        }
        return false;
    }

    static Type getValueType(Type type) {
        if (! isAtomicType(type)) {
            return INVALID_TYPE;
        }
        String spelling = type.spelling();
        spelling = spelling.substring("_Atomic(".length(), spelling.length() - 1);
        try {
            return checkBuiltinType(spelling);
        } catch (RuntimeException re) {
            for (Map<String, Type> dict : contexts.values()) {
                Type rt = dict.get(spelling);
                if (rt != null) {
                    return rt;
                }
            }
        }

        throw new IllegalStateException("Cannot find value type " + spelling);
    }

    static Type observe(Cursor c) {
        if (! c.isDeclaration()) {
            return c.type();
        }
        Type type = c.type();
        TranslationUnit tu = c.getTranslationUnit();
        Map<String, Type> context = contexts.computeIfAbsent(tu, k -> new HashMap<>());
        return context.putIfAbsent(type.spelling(), type);
    }

    static void removeTU(TranslationUnit tu) {
        contexts.remove(tu);
    }
}
