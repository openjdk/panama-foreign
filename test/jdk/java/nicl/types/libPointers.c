/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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

#include <stddef.h>
#include <stdio.h>

struct MyStruct {
  int ia[3];
  const char* str;
};

static const char* strings[] = {
  "String A",
  "String B",
  "String C"
};

static const struct MyStruct structs[] = {
  { { 1, 2, 3 }, "String A" },
  { { 4, 5, 6 }, "String B" },
  { { 7, 8, 9 }, "String C" }
};

static const struct MyStruct* struct_pointers[] = {
  &structs[0],
  &structs[1],
  &structs[2]
};

const char** get_strings2(int* pcount) {
  if (NULL == pcount) {
    return NULL;
  }
  *pcount = sizeof(strings) / sizeof(const char*);
  return strings;
}

void get_strings(const char*** p, int* pcount) {
  *p = get_strings2(pcount);
}

const struct MyStruct** get_structs2(int* pcount) {
  *pcount = sizeof(struct_pointers) / sizeof(struct MyStruct*);
  return struct_pointers;
}

void get_structs(const struct MyStruct*** p, int* pcount) {
  *p = get_structs2(pcount);
}

void* get_stringsAsVoidPtr(int* pcount) {
    return get_strings2(pcount);
}

struct opaque* get_stringsAsOpaquePtr(int *pcount) {
    return (struct opaque*) get_strings2(pcount);
}