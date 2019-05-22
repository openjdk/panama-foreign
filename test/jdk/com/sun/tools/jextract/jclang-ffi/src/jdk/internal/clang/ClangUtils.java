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
}
