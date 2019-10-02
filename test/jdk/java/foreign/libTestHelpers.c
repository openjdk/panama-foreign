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

#ifdef _WIN64
#define EXPORT __declspec(dllexport)
#else
#define EXPORT
#endif

#include <stdlib.h>

typedef struct point {
    int x;
    int y;
    void *data;
} point_t;

EXPORT void setCoordination(point_t *dot, int x, int y) {
    dot->x = x;
    dot->y = y;
}

EXPORT int getDotX(point_t *dot) {
    return dot->x;
}

EXPORT int getDotY(point_t *dot) {
    return dot->y;
}

EXPORT point_t* allocateDot() {
    return malloc(sizeof(point_t));
}

EXPORT void freeDot(point_t *dot) {
    free(dot);
}

EXPORT point_t* allocateDotArray(int number) {
    return calloc(number, sizeof(point_t));
}

EXPORT void freeDotArray(point_t* dots) {
    free(dots);
}

EXPORT void allocateDots(int number, point_t** dots) {
    for (int i = 0; i < number; i++) {
        dots[i] = malloc(sizeof(point_t));
        setCoordination(dots[i], i, i);
    }
}

EXPORT void freeDots(int number, point_t** dots) {
    for (int i = 0; i < number; i++) {
        free(dots[i]);
    }
}

EXPORT point_t getDot(int x, int y, void* data) {
    point_t rv;

    rv.x = x;
    rv.y = y;
    rv.data = data;
    return rv;
}

