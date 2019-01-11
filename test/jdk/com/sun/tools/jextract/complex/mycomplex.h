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

/*
 * Subset of declarations found in /usr/include/complex.h
 *
 * Two reasons why we don't just jextract /usr/include/complex.h
 *
 *  1. binder does not yet support long double and so we can't support
 *     long double _Complex type yet.
 *  2. linux /usr/include/complex.h includes & expands macros from
 *     <bits/cmathcalls.h> and so jextracting that files brings no
 *     declaration!
 */

#ifndef _WIN64
#include <complex.h>

extern float complex cexpf(float complex);
extern double complex cexp(double complex);
extern long double complex cexpl(long double complex);

extern float cabsf(float complex);
extern double cabs(double complex);
extern long double cabsl(long double complex);

extern float cargf(float complex);
extern double carg(double complex);
extern long double cargl(long double complex);

extern float cimagf(float complex);
extern double cimag(double complex);
extern long double cimagl(long double complex);

extern float complex conjf(float complex);
extern double complex conj(double complex);
extern long double complex conjl(long double complex);

extern float crealf(float complex);
extern double creal(double complex);
extern long double creall(long double complex);

#endif