/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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

package jdk.internal.jextract.impl;

public class StringSourceBuilder {

    // code buffer
    private StringBuilder sb = new StringBuilder();
    // current line alignment (number of 4-spaces)
    private int align;

    void append(String s) {
        sb.append(s);
    }

    void append(char c) {
        sb.append(c);
    }

    void append(boolean b) {
        sb.append(b);
    }

    void append(long l) {
        sb.append(l);
    }

    void indent() {
        for (int i = 0; i < align; i++) {
            append("    ");
        }
    }

    void incrAlign() {
        align++;
    }

    void decrAlign() {
        align--;
    }

    String build() {
        String s = sb.toString();
        sb = null;
        return s;
    }
}
