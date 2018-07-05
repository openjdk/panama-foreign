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

#include "struct.h"
#include <stdio.h>
#include <stdlib.h>

char* LastCalledMethod;

struct UndefinedStruct {
    int x;
    int y;
};

struct UndefinedStructForPointer {
    UndefinedStruct position;
    UndefinedStructPointer parent;
    UndefinedStructPointer firstChild;
    struct UndefinedStructForPointer *left;
    UndefinedStructPointer right;
    struct Opaque *data;
};

struct Opaque {
    int size;
    char *data;
};

UndefinedStruct* allocateUndefinedStruct() {
    UndefinedStruct *p = malloc(sizeof(UndefinedStruct));
    p->x = 0x1234;
    p->y = 0x3412;
    return p;
}

struct Plain fromUndefinedStruct(UndefinedStruct *p) {
    return *((struct Plain*) p);
}

// intentionally mismatch prototype with same type
UndefinedStructPointer getParent(struct UndefinedStructForPointer * node) {
    return node->parent;
}

// intentionally mismatch prototype with same type
UndefinedStructPointer getSibling(UndefinedStructPointer node) {
    return node->right;
}

UndefinedStructPointer getFirstChild(struct UndefinedStructForPointer *node) {
    return node->firstChild;
}

struct Opaque* allocate_opaque_struct() {
    return (struct Opaque*) malloc(sizeof(struct Opaque));
}

TypedefAnonymous getAnonymous(TypedefNamedDifferent_t fns, int x, int y) {
    TypedefAnonymous s;
    s.x = x;
    s.y = y;
    s.l = fns.fn(x, y);

    return s;
}

void emptyArguments() {
    LastCalledMethod = "emptyArguments";
    printf("%s\n", LastCalledMethod);
}

void voidArguments(void) {
    LastCalledMethod = "voidArguments";
    printf("%s\n", LastCalledMethod);
}

void* FunctionWithVoidPointer(void *data, void **array_data) {
    return NULL;
}

void** GetArrayData(struct IncompleteArray *par) {
    return NULL;
}

void* GetData(struct IncompleteArray ar) {
    printf("Sizeof(IncompleteArray) = %lu\n", sizeof(ar));
    return NULL;
}
