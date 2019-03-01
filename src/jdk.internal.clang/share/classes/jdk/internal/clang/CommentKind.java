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

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public enum CommentKind {
    Null(0),
    Text(1),
    InlineCommand(2),
    HTMLStartTag(3),
    HTMLEndTag(4),
    Paragraph(5),
    BlockCommand(6),
    ParamCommand(7),
    TParamCommand(8),
    VerbatimBlockCommand(9),
    VerbatimBlockLine(10),
    VerbatimLine(11),
    FullComment(12);

    private final int value;

    CommentKind(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    private final static Map<Integer, CommentKind> lookup;

    static {
        lookup = new HashMap<>();
        for (CommentKind e: CommentKind.values()) {
            lookup.put(e.value(), e);
        }
    }

    public final static CommentKind valueOf(int value) {
        CommentKind x = lookup.get(value);
        if (null == x) {
            throw new NoSuchElementException("kind = " + value);
        }
        return x;
    }
}
