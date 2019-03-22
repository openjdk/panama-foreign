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

#include "filterDep1.h"
#include "filterDep2.h"
#include "filterDep3.h"
#include "filterDep4.h"
#include "filterDep5.h"
#include "filterDep6.h"
#include "filterDep7.h"
#include "filterDep8.h"
#include "filterDep9.h"

#ifdef _WIN64
#define EXPORT __declspec(dllexport)
#else
#define EXPORT
#endif

// normal dep on Widget1
EXPORT void foo1(struct Widget1 widget);

// dep through array
EXPORT void foo2(struct Widget2 arr[2]);

// dep through pointer
EXPORT void foo3(struct Widget3* ptr);

// dep through typedef
EXPORT void foo4(Widget4 widget);

// dep through structs
struct Widget5 {
    struct Widget6* w;
};

// filtered by pattern
EXPORT void foo5(struct Junk6 junk);

// filtered by library lookup
EXPORT void foo6(struct Junk7 junk);
