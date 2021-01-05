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

package jdk.internal.clang;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Describes the kind of a template argument.
 */
public enum TemplateArgumentKind {
    Null(0),
    Type(1),
    Declaration(2),
    NullPtr(3),
    Integral(4),
    Template(5),
    TemplateExpansion(6),
    Expression(7),
    Pack(8),
    /* Indicates an error case, preventing the kind from being deduced. */
    Invalid(9);

    private final int value;

    TemplateArgumentKind(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    private final static Map<Integer, TemplateArgumentKind> lookup;

    static {
        lookup = new HashMap<>();
        for (TemplateArgumentKind e: TemplateArgumentKind.values()) {
            lookup.put(e.value(), e);
        }
    }

    public final static TemplateArgumentKind valueOf(int value) {
        TemplateArgumentKind x = lookup.get(value);
        if (null == x) {
            throw new ClangException("Invalid TemplateArgumentKind kind value: " + value);
        }
        return x;
    }
}
