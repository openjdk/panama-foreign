/*
 * Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
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

#include "libStdLibTest.h"

EXPORT char *libc_strcat(char *str1, const char *str2) {
    return strcat(str1, str2);
}

EXPORT int libc_strcmp(const char *str1, const char *str2) {
    return strcmp(str1, str2);
}

EXPORT size_t libc_strlen(const char *str) {
    return strlen(str);
}

EXPORT int libc_puts(const char *str) {
    return puts(str);
}

EXPORT struct tm *libc_gmtime(const time_t* timer) {
    return gmtime(timer);
}

EXPORT void libc_qsort(void *base, size_t nitems, size_t size, int (*compar)(const void *, const void*)) {
    qsort(base, nitems, size, compar);
}

EXPORT int libc_rand(void) {
    return rand();
}

EXPORT int libc_vprintf(const char *format, va_list arg) {
    return vprintf(format, arg);
}

EXPORT int libc_printf(const char *format, ...) {
   va_list arg;
   int done;

   va_start(arg, format);
   done = vprintf(format, arg);
   va_end(arg);

   return done;
}
