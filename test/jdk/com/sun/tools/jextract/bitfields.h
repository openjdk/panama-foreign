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

struct bitfields1 {
   long long x : 2;
   char  : 0;
   long long y : 15;
   int z : 20;
   int w : 13;
};

struct bitfields2 {
    char c : 3,
         c2 : 3,
         c3: 7;
    int i: 4;
    long long l: 21;
    long long ll : 42;
};

struct bitfields3 {
   char c1 : 4;
   int i : 20;
   char c2 : 8;
   int l1 : 32;
   int l2 : 32;
};

struct bitfields4 {
   long long l;
   char c : 4;
};

struct bitfields5 {
   char c:7;
   long long l:63;
};

union bitfields6 {
   char c1 : 4;
   int i : 20;
};
