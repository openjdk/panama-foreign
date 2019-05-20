package jdk.internal.clang;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Helper functions to supplement libclang
 */
class ClangUtils {
    // C11 types as in n1570::6.2.5
    private static final String[] builtinTypes = new String[] {
            "_Bool", "char", "signed char", "unsigned char",
            "short", "int", "long", "long long",
            "unsigned short", "unsigned int", "unsigned long", "unsigned long long",
            "float", "double", "long double",
            "float _Complex", "double _Complex", "long double _Complex"
    };
    private static final Map<String, Type> builtins = new HashMap<>();
    private static final Type INVALID_TYPE;

    // TU to types table
    private static final Map<TranslationUnit, Map<String, Type>> contexts = new HashMap<>();

    private static Type initBuiltinTypes() {
        try {
            Path tmpFile = Files.createTempFile("jextract", ".h");
            tmpFile.toFile().deleteOnExit();
            Files.write(tmpFile, IntStream.range(0, builtinTypes.length)
                    .mapToObj(i -> builtinTypes[i] + " arg" + i + ";")
                    .collect(Collectors.toList())
            );
            Index idx = LibClang.createIndex(true);
            Cursor tu = idx.parse(tmpFile.toAbsolutePath().toString(),
                    d -> {
                        if (d.severity() > Diagnostic.CXDiagnostic_Warning) {
                            throw new RuntimeException(d.toString());
                        }

                    }, false);
            tu.children().map(Cursor::type)
                    .forEach(ct -> builtins.put(ct.spelling(), ct));
            Type t = tu.type();
            // assert Invalid type
            if (t.kind() != TypeKind.Invalid || ! t.spelling().isEmpty()) {
                throw new IllegalStateException("Expected Invalid Type");
            }
            return t;
        } catch (IOException ioExp) {
            throw new UncheckedIOException(ioExp);
        }
    }

    static {
        INVALID_TYPE = initBuiltinTypes();
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
        Type rt = builtins.get(spelling);
        if (rt != null) {
            return rt;
        }

        for (Map<String, Type> dict: contexts.values()) {
            rt = dict.get(spelling);
            if (rt != null) {
                return rt;
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
