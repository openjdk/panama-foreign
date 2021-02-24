/*
 * Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
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

package jdk.internal.jextract.gen;

import javax.lang.model.SourceVersion;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.io.IOException;
import java.net.URI;

/**
 * General utility functions
 */
class Utils {
    private static URI fileName(String pkgName, String clsName, String extension) {
        String pkgPrefix = pkgName.isEmpty() ? "" : pkgName.replaceAll("\\.", "/") + "/";
        return URI.create(pkgPrefix + clsName + extension);
    }

    static JavaFileObject fileFromString(String pkgName, String clsName, String contents) {
        return new SimpleJavaFileObject(fileName(pkgName, clsName, ".java"), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
                return contents;
            }
        };
    }

    static String javaSafeIdentifier(String name) {
        return javaSafeIdentifier(name, false);
    }

    static String javaSafeIdentifier(String name, boolean checkAllChars) {
        if (checkAllChars) {
            StringBuilder buf = new StringBuilder();
            char[] chars = name.toCharArray();
            if (Character.isJavaIdentifierStart(chars[0])) {
                buf.append(chars[0]);
            } else {
                buf.append('_');
            }
            if (chars.length > 1) {
                for (int i = 1; i < chars.length; i++) {
                    char ch = chars[i];
                    if (Character.isJavaIdentifierPart(ch)) {
                        buf.append(ch);
                    } else {
                        buf.append('_');
                    }
                }
            }
            return buf.toString();
        } else {
            // We never get the problem of Java non-identifiers (like 123, ab-xy) as
            // C identifiers. But we may have a java keyword used as a C identifier.
            assert SourceVersion.isIdentifier(name);

            return SourceVersion.isKeyword(name) || isRestrictedTypeName(name) ? (name + "_") : name;
        }
    }

    private static boolean isRestrictedTypeName(String name) {
        return switch (name) {
            case "var", "yield", "record",
                "sealed", "permits" -> true;
            default -> false;
        };
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
    static String quote(String s) {
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
    static String quote(char ch) {
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
}
