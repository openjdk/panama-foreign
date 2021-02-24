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

package jdk.internal.jextract.clang;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public enum RefQualifierKind {
    /** No ref-qualifier was provided. */
    CXRefQualifier_None(0),
    /** An lvalue ref-qualifier was provided (\c &). */
    CXRefQualifier_LValue(1),
    /** An rvalue ref-qualifier was provided (\c &&). */
    CXRefQualifier_RValue(2);

    private final int value;

    RefQualifierKind(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    private final static Map<Integer, RefQualifierKind> lookup;

    static {
        lookup = new HashMap<>();
        for (RefQualifierKind e: RefQualifierKind.values()) {
            lookup.put(e.value(), e);
        }
    }

    public final static RefQualifierKind valueOf(int value) {
        RefQualifierKind x = lookup.get(value);
        if (null == x) {
            throw new NoSuchElementException("Invalid RefQualifierKind kind value: " + value);
        }
        return x;
    }
}
