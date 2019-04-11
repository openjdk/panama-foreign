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

#include "sub/dupname.h"

#ifdef __cplusplus
extern "C" {
#endif // __cplusplus

#ifdef _WIN64
#define EXPORT __declspec(dllexport)
#else
#define EXPORT
#endif

// make sure that 'true', 'false' macros do not
// generate non-compilable source!

#include <stdbool.h>

// The following macro is inspired from a macro from
// /usr/include/net/route.h. Make sure that jextract
// generates proper String literal

#define PROBLEM_STRING \
    "\020\1UP\2GATEWAY\3HOST\4REJECT\5DYNAMIC\6MODIFIED\7DONE" \
    "\10DELCLONE\11CLONING\12XRESOLVE\13LLINFO\14STATIC\15BLACKHOLE"

enum {
    R, G, B
};

enum Color {
    RED, GREEN, BLUE
};

struct Point {
    int x, y;
};

EXPORT int num;

EXPORT int x_coord(struct Point* p);
EXPORT int y_coord(struct Point* p);
EXPORT int sum(int i, ...);

#define INFINITY 1.0/0.0
#define NAN 0.0/0.0

EXPORT int (*funcPtr)(int);

#ifdef __cplusplus
}
#endif // __cplusplus
