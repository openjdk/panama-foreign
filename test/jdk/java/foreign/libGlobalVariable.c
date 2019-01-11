/**
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

#include <stdint.h>
#include <stdbool.h>
#include <stdio.h>
#if defined(__AVX__)
#include <immintrin.h>
#endif

#ifdef _WIN64
#define EXPORT __declspec(dllexport)
#else
#define EXPORT
#endif

struct MyStruct {
  int i;
};

EXPORT bool     global_boolean = true;
EXPORT int8_t   global_i8  = 42;
EXPORT int16_t  global_i16 = 42;
EXPORT uint16_t global_u16 = 42;
EXPORT int32_t  global_i32 = 42;
EXPORT int64_t  global_i64 = 42;
EXPORT float    global_f32 = 42;
EXPORT double   global_d64 = 42;

EXPORT struct MyStruct global_struct = {
  .i = 42
};

#if defined(__AVX__)
EXPORT __m256i global_v256;
#else
EXPORT char global_v256;
#endif

EXPORT void init() {
#if defined(__AVX__)
  global_v256 = _mm256_set_epi64x(0, 0, 0, 42);
#endif
}
