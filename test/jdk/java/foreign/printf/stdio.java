/*
 * Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved.
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


import java.foreign.annotations.NativeHeader;
import java.foreign.annotations.NativeLocation;
import java.foreign.memory.Pointer;

@NativeHeader(path="/usr/include/stdio.h", declarations =
        "getpid=()i32" +
        "printf=(u64:u8*)i32" +
        "fprintf=(u64:vu64:u8*)i32" +
        "fflush=(u64:v)i32" +
        "fdopen=(i32u64:u8)u64:v"
)
public interface stdio {
    @NativeLocation(file="/usr/include/stdio.h", line=47, column=11)
    int getpid();

    @NativeLocation(file="/usr/include/stdio.h", line=47, column=11)
    int printf(Pointer<Byte> fmt, Object... args);

    @NativeLocation(file="/usr/include/stdio.h", line=47, column=11)
    int fprintf(Pointer<Void> strm, Pointer<Byte> fmt, Object... args);

    @NativeLocation(file="/usr/include/stdio.h", line=47, column=11)
    int fflush(Pointer<Void> stream);

    @NativeLocation(file="/usr/include/stdio.h", line=47, column=11)
    Pointer<Void> fdopen(int fd, Pointer<Byte> mode);
}
