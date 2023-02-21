/*
 * Copyright (c) 2023, Oracle and/or its affiliates. All rights reserved.
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

union U1{ char f0; };
struct S1{ float f0[3]; };
struct S2{ char f0[3][3]; };
struct S3{ struct S2 f0; long long f1; union U1 f2; short f3[2]; };
struct S4{ short f0[3]; };
struct S5{ int f0; float f1; short f2; short f3; };
struct S6{ double f0; int f1; float f2; long long f3; };
union U2{ int f0; void* f1[4]; void* f2; };

EXPORT struct S5 F85(struct S5 (*cb)(long long, void*, struct S3, short, void*, char, struct S1, void*,
                                         struct S4, char, long long, char, long long, char, short, struct S5,
                                         long long, struct S6, float, float, union U2, double, long long, double, float),
        long long a0, void* a1, struct S3 a2, short a3, void* a4, char a5, struct S1 a6, void* a7, struct S4 a8,
        char a9, long long a10, char a11, long long a12, char a13, short a14, struct S5 a15, long long a16,
        struct S6 a17, float a18, float a19, union U2 a20, double a21, long long a22, double a23, float a24){
    return cb(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19, a20, a21, a22, a23, a24);
}
