/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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

#ifdef _WIN64
#define EXPORT __declspec(dllexport)
#else
#define EXPORT
#endif

// fits in single 64 bit register
struct small_tuple {
    int one;
    int two;
};

struct tuple {
   int one;
   int two;
   int three;
   int four;
};

struct big_tuple {
   int one;
   int two;
   int three;
   int four;
   int five;
};

struct small_tuple SMALL_T = { 1, 2 };
struct tuple T = { 1, 2, 3, 4 };
struct big_tuple BIG_T = { 1, 2, 3, 4, 5 };
 
EXPORT struct small_tuple small_make() {
    return SMALL_T;
}

EXPORT struct small_tuple small_id(struct small_tuple t) {
    return t;
}

EXPORT struct small_tuple small_zero(struct small_tuple t) {
    t.one = 0;
    t.two = 0;
    return t;
}


EXPORT struct tuple make() {
    return T;
}
 
EXPORT struct tuple id(struct tuple t) {
    return t;
}

EXPORT struct tuple zero(struct tuple t) {
    t.one = 0;
    t.two = 0;
    t.three = 0;
    t.four = 0;
    return t;
}
 
EXPORT struct big_tuple big_make() {
    return BIG_T;
}
 
EXPORT struct big_tuple big_id(struct big_tuple t) {
    return t;
}

EXPORT struct big_tuple big_zero(struct big_tuple t) {
    t.one = 0;
    t.two = 0;
    t.three = 0;
    t.four = 0;
    t.five = 0;
    return t;
}