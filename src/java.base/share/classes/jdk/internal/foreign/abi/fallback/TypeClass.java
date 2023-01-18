/*
 * Copyright (c) 2023, Oracle and/or its affiliates. All rights reserved.
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
package jdk.internal.foreign.abi.fallback;

import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.ValueLayout;

enum TypeClass {
    BOOLEAN,
    BYTE,
    CHAR,
    SHORT,
    INT,
    LONG,
    FLOAT,
    DOUBLE,
    ADDRESS,
    SEGMENT,
    VOID;

    static TypeClass classify(MemoryLayout layout) {
        if (layout instanceof ValueLayout.OfBoolean) {
            return BOOLEAN;
        } else if (layout instanceof ValueLayout.OfByte) {
            return BYTE;
        } else if (layout instanceof ValueLayout.OfShort) {
            return SHORT;
        } else if (layout instanceof ValueLayout.OfChar) {
            return CHAR;
        } else if (layout instanceof ValueLayout.OfInt) {
            return INT;
        } else if (layout instanceof ValueLayout.OfLong) {
            return LONG;
        } else if (layout instanceof ValueLayout.OfFloat) {
            return FLOAT;
        } else if (layout instanceof ValueLayout.OfDouble) {
            return DOUBLE;
        } else if (layout instanceof ValueLayout.OfAddress) {
            return ADDRESS;
        } else if (layout instanceof GroupLayout) {
            return SEGMENT;
        }
        throw new IllegalArgumentException("Can not classify layout: " + layout);
    }
}
