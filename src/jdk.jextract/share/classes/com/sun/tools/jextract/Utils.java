/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
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

import java.foreign.layout.Function;
import java.foreign.layout.Layout;
import java.util.function.BiFunction;
import javax.lang.model.SourceVersion;
import jdk.internal.clang.Type;
import com.sun.tools.jextract.tree.LayoutUtils;

/**
 * General utility functions
 */
public class Utils {
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

    public static String toClassName(String cname) {
        StringBuilder sb = new StringBuilder(cname.length());
        cname = toJavaIdentifier(cname);
        sb.append(cname);
        if (SourceVersion.isKeyword(cname)) {
            sb.append("$");
        }
        return sb.toString();
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

    public static Layout getLayout(Type type) {
        return LayoutUtils.getLayout(type);
    }

    public static Function getFunction(Type type) {
        return LayoutUtils.getFunction(type);
    }

    public static Class<?> unboxIfNeeded(Class<?> clazz) {
        if (clazz == Boolean.class) {
            return boolean.class;
        } else if (clazz == Void.class) {
            return void.class;
        } else if (clazz == Byte.class) {
            return byte.class;
        } else if (clazz == Character.class) {
            return char.class;
        } else if (clazz == Short.class) {
            return short.class;
        } else if (clazz == Integer.class) {
            return int.class;
        } else if (clazz == Long.class) {
            return long.class;
        } else if (clazz == Float.class) {
            return float.class;
        } else if (clazz == Double.class) {
            return double.class;
        } else {
            return clazz;
        }
    }
}
