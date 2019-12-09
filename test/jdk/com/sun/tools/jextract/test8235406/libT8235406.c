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

#include "t8235406.h"

struct Brush00 buffer[3];

EXPORT int setBrush(int x, enum Color c00, enum Color c01) {
    if (x < 0 || x > 2) {
        return -1;
    }

    buffer[x].foreground03 = c00;
    buffer[x].background03 = c01;
    return x;
}

EXPORT int getBrush(int x, struct Brush00 *pBrush) {
    if (x < 0 || x > 2) {
        return -1;
    }

    pBrush->foreground03 = buffer[x].foreground03;
    pBrush->background03 = buffer[x].background03;
    return x;
}

EXPORT int getBrush00(struct Brush00 *brush00) {
    return getBrush(0, brush00);
}

EXPORT int getBrush01(struct Brush00 *brush01) {
    return getBrush(1, brush01);
}

EXPORT int getBrush02(struct Brush00 *brush02) {
    return getBrush(2, brush02);
}
