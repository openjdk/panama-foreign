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

#ifdef __cplusplus
extern "C" {
#endif // __cplusplus

#ifdef _WIN64
#define EXPORT __declspec(dllexport)
#else
#define EXPORT
#endif

typedef struct UndefinedStruct UndefinedStruct;
EXPORT UndefinedStruct* allocateUndefinedStruct();

typedef struct UndefinedStructForPointer *UndefinedStructPointer;
EXPORT UndefinedStructPointer getParent(UndefinedStructPointer node);
EXPORT struct UndefinedStructForPointer* getSibling(UndefinedStructPointer node);
EXPORT UndefinedStructPointer getFirstChild(struct UndefinedStructForPointer* node);

EXPORT struct Opaque* allocate_opaque_struct();

typedef struct TypedefNamedAsIs {
    int i;
    long l;
} TypedefNamedAsIs;

typedef struct TypedefNamedDifferent {
    long (*fn)(int i, int j);
} TypedefNamedDifferent_t;

typedef struct {
    union {
        long l;
        struct {
            int x1;
            int y1;
        };
        struct {
            int x2;
            int y2;
        } p2;
    };
    int x;
    int y;
} TypedefAnonymous;

struct Plain {
    int x;
    int y;
};

EXPORT struct Plain fromUndefinedStruct(UndefinedStruct *p);

EXPORT TypedefAnonymous getAnonymous(TypedefNamedDifferent_t fn, int x, int y);

EXPORT void emptyArguments();
EXPORT void voidArguments(void);

typedef void* (*FunctionPointer)(void *data, void **array_data);

EXPORT void* FunctionWithVoidPointer(void *data, void **array_data);

struct IncompleteArray {
    long list_length;
    void *ptr;
    void **junk;
    FunctionPointer fn;
    void *list_of_data[];
};

EXPORT void** GetArrayData(struct IncompleteArray *par);

// This works with C, but incomplete array is omitted as not exist
EXPORT void* GetData(struct IncompleteArray ar);

EXPORT extern char* LastCalledMethod;

#ifdef __cplusplus
}
#endif // __cplusplus
