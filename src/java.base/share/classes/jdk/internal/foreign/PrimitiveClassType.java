/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
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
package jdk.internal.foreign;

enum PrimitiveClassType {
    VOID, BOOLEAN, BYTE, CHAR, SHORT, INT, LONG, FLOAT, DOUBLE;

    static PrimitiveClassType typeof(Class<?> c) {
        if (!c.isPrimitive()) {
            throw new IllegalArgumentException(c + " is not primitive");
        }

        if (c == Integer.TYPE) {
            return INT;
        } else if (c == Void.TYPE) {
            return VOID;
        } else if (c == Boolean.TYPE) {
            return BOOLEAN;
        } else if (c == Byte.TYPE) {
            return BYTE;
        } else if (c == Character.TYPE) {
            return CHAR;
        } else if (c == Short.TYPE) {
            return SHORT;
        } else if (c == Double.TYPE) {
            return DOUBLE;
        } else if (c == Float.TYPE) {
            return FLOAT;
        } else if (c == Long.TYPE) {
            return LONG;
        } else {
            throw new IllegalArgumentException("Unhandled class: " + c);
        }
    }
}

