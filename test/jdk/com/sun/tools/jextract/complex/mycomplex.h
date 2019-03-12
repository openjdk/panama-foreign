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

extern float _Complex cexpf(float _Complex);
extern double _Complex cexp(double _Complex);
extern long double _Complex cexpl(long double _Complex);

extern float cabsf(float _Complex);
extern double cabs(double _Complex);
extern long double cabsl(long double _Complex);

extern float cargf(float _Complex);
extern double carg(double _Complex);
extern long double cargl(long double _Complex);

extern float cimagf(float _Complex);
extern double cimag(double _Complex);
extern long double cimagl(long double _Complex);

extern float _Complex conjf(float _Complex);
extern double _Complex conj(double _Complex);
extern long double _Complex conjl(long double _Complex);

extern float crealf(float _Complex);
extern double creal(double _Complex);
extern long double creall(long double _Complex);

#endif
