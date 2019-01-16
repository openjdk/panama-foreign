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

enum Color {
   R = 0xFF0000,
   G = 0x00FF00,
   B = 0x0000FF
};

EXPORT int red_green();
EXPORT int green_blue();
EXPORT int red_green_blue();

enum IntEnum {
   I_VALUE1 = 42,
   I_VALUE2 = -5345345
};

#ifndef _WIN64
enum LongEnum {
   L_VALUE1 = -4564565645L,
   L_VALUE2 = 45645645645L
};
#else
enum LongEnum {
   L_VALUE1 = -5345345L,
   L_VALUE2 = -5345345
};
#endif

EXPORT int i_value1_func();
EXPORT int i_value2_func();
EXPORT long l_value1_func();
EXPORT long l_value2_func();
