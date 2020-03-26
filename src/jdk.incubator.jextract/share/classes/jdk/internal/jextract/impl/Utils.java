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

import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.MemoryLayout;
import jdk.internal.clang.Cursor;
import jdk.internal.clang.CursorKind;
import jdk.internal.clang.SourceLocation;
import jdk.internal.clang.Type;
import jdk.internal.clang.TypeKind;

import javax.lang.model.SourceVersion;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * General utility functions
 */
class Utils {
    public static void validSimpleIdentifier(String name) {
        int length = name.length();
        if (length == 0) {
            throw new IllegalArgumentException();
        }

        int ch = name.codePointAt(0);
        if (length == 1 && ch == '_') {
            throw new IllegalArgumentException("'_' is no longer valid identifier.");
        }

        if (!Character.isJavaIdentifierStart(ch)) {
            throw new IllegalArgumentException("Invalid start character for an identifier: " + ch);
        }

        for (int i = 1; i < length; i++) {
            ch = name.codePointAt(i);
            if (!Character.isJavaIdentifierPart(ch)) {
                throw new IllegalArgumentException("Invalid character for an identifier: " + ch);
            }
        }
    }

    public static void validPackageName(String name) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException();
        }
        int idx = name.lastIndexOf('.');
        if (idx == -1) {
           validSimpleIdentifier(name);
        } else {
            validSimpleIdentifier(name.substring(idx + 1));
            validPackageName(name.substring(0, idx));
        }
    }

    public static String toJavaIdentifier(String str) {
        final int size = str.length();
        StringBuilder sb = new StringBuilder(size);
        if (! Character.isJavaIdentifierStart(str.charAt(0))) {
            sb.append('_');
        }
        for (int i = 0; i < size; i++) {
            char ch = str.charAt(i);
            if (Character.isJavaIdentifierPart(ch)) {
                sb.append(ch);
            } else {
                sb.append('_');
            }
        }
        return sb.toString();
    }

    private static String toSafeName(String name) {
        StringBuilder sb = new StringBuilder(name.length());
        name = toJavaIdentifier(name);
        sb.append(name);
        if (SourceVersion.isKeyword(name)) {
            sb.append("$");
        }
        return sb.toString();
    }

    public static String toClassName(String cname) {
        return toSafeName(cname);
    }

    public static String toMacroName(String mname) {
        return toSafeName(mname);
    }

    public static String toInternalName(String pkg, String name, String... nested) {
        if ((pkg == null || pkg.isEmpty()) && nested == null) {
            return name;
        }

        StringBuilder sb = new StringBuilder();
        if (pkg != null && ! pkg.isEmpty()) {
            sb.append(pkg.replace('.', '/'));
            if (sb.charAt(sb.length() - 1) != '/') {
                sb.append('/');
            }
        }
        sb.append(name);
        for (String n: nested) {
            sb.append('$');
            sb.append(n);
        }
        return sb.toString();
    }

    public static String getName(Type type) {
        return LayoutUtils.getName(type);
    }

    public static Stream<Cursor> flattenableChildren(Cursor c) {
        return c.children()
                .filter(cx -> cx.isAnonymousStruct() || cx.kind() == CursorKind.FieldDecl);
    }

    public static Optional<Cursor> lastChild(Cursor c) {
        List<Cursor> children = flattenableChildren(c)
                .collect(Collectors.toList());
        return children.isEmpty() ? Optional.empty() : Optional.of(children.get(children.size() - 1));
    }

    public static boolean hasIncompleteArray(Cursor c) {
        switch (c.kind()) {
            case FieldDecl:
                return c.type().kind() == TypeKind.IncompleteArray;
            case UnionDecl:
                return flattenableChildren(c)
                        .anyMatch(Utils::hasIncompleteArray);
            case StructDecl:
                return lastChild(c).map(Utils::hasIncompleteArray).orElse(false);
            default:
                throw new IllegalStateException("Unhandled cursor kind: " + c.kind());
        }
    }

    // return builtin Record types accessible from the given Type
    public static Stream<Cursor> getBuiltinRecordTypes(Type type) {
        List<Cursor> recordTypes = new ArrayList<>();
        fillBuiltinRecordTypes(type, recordTypes);
        return recordTypes.stream().distinct();
    }

    private static void fillBuiltinRecordTypes(Type type, List<Cursor> recordTypes) {
        Type canonicalType = type.canonicalType();
        switch (canonicalType.kind()) {
            case ConstantArray:
            case IncompleteArray:
                fillBuiltinRecordTypes(canonicalType.getElementType(), recordTypes);
                break;

            case FunctionProto:
            case FunctionNoProto: {
                final int numArgs = canonicalType.numberOfArgs();
                for (int i = 0; i < numArgs; i++) {
                    fillBuiltinRecordTypes(canonicalType.argType(i), recordTypes);
                }
                fillBuiltinRecordTypes(canonicalType.resultType(), recordTypes);
            }
            break;

            case Record: {
                Cursor c = canonicalType.getDeclarationCursor();
                if (c.isDefinition()) {
                    SourceLocation sloc = c.getSourceLocation();
                    if (sloc != null && sloc.getFileLocation().path() == null) {
                        recordTypes.add(c);
                    }
                }
            }
            break;

            case BlockPointer:
            case Pointer:
                fillBuiltinRecordTypes(canonicalType.getPointeeType(), recordTypes);
                break;

            case Unexposed:
                if (! canonicalType.equalType(type)) {
                    fillBuiltinRecordTypes(canonicalType, recordTypes);
                }
                break;

            case Elaborated:
            case Typedef:
                fillBuiltinRecordTypes(canonicalType, recordTypes);
                break;

            default: // nothing to do
        }
    }

    // return the absolute path of the library of given name by searching
    // in the given array of paths.
    public static Optional<Path> findLibraryPath(Path[] paths, String libName) {
        return Arrays.stream(paths).
                map(p -> p.resolve(System.mapLibraryName(libName))).
                filter(Files::isRegularFile).map(Path::toAbsolutePath).findFirst();
    }

    /*
     * FIXME: when we add jdk.compiler dependency from jdk.jextract module, revisit
     * the following. The following methods 'quote', 'quote' and 'isPrintableAscii'
     * are from javac source. See also com.sun.tools.javac.util.Convert.java.
     */

    /**
     * Escapes each character in a string that has an escape sequence or
     * is non-printable ASCII.  Leaves non-ASCII characters alone.
     */
    public static String quote(String s) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            buf.append(quote(s.charAt(i)));
        }
        return buf.toString();
    }

    /**
     * Escapes a character if it has an escape sequence or is
     * non-printable ASCII.  Leaves non-ASCII characters alone.
     */
    public static String quote(char ch) {
        switch (ch) {
        case '\b':  return "\\b";
        case '\f':  return "\\f";
        case '\n':  return "\\n";
        case '\r':  return "\\r";
        case '\t':  return "\\t";
        case '\'':  return "\\'";
        case '\"':  return "\\\"";
        case '\\':  return "\\\\";
        default:
            return (isPrintableAscii(ch))
                ? String.valueOf(ch)
                : String.format("\\u%04x", (int) ch);
        }
    }

    /**
     * Is a character printable ASCII?
     */
    private static boolean isPrintableAscii(char ch) {
        return ch >= ' ' && ch <= '~';
    }

    public static Optional<GroupLayout> getContents(MemoryLayout layout) {
        return layout.attribute("contents").map(GroupLayout.class::cast);
    }

    @SuppressWarnings("unchecked")
    public static <Z extends MemoryLayout> Z addContents(Z layout, GroupLayout contents) {
        return (Z) layout.withAttribute("contents", contents);
    }
}
