/*
 *  Copyright (c) 2019, 2020, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.
 *
 *  This code is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  version 2 for more details (a copy is included in the LICENSE file that
 *  accompanied this code).
 *
 *  You should have received a copy of the GNU General Public License version
 *  2 along with this work; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *  Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 *
 */

import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemoryLayouts.AArch64ABI;
import jdk.incubator.foreign.MemoryLayouts.SysV;
import jdk.incubator.foreign.MemoryLayouts.WinABI;
import jdk.incubator.foreign.SystemABI;
import jdk.incubator.foreign.ValueLayout;
import jdk.internal.foreign.Utils;
import jdk.internal.foreign.abi.ArgumentClass;

import static jdk.incubator.foreign.SystemABI.ABI_AARCH64;
import static jdk.incubator.foreign.SystemABI.ABI_SYSV;
import static jdk.incubator.foreign.SystemABI.ABI_WINDOWS;

public class NativeTestHelper {

    public static final SystemABI ABI = SystemABI.getInstance();

    public static final ValueLayout C_CHAR = pick(SysV.C_CHAR, WinABI.C_CHAR, AArch64ABI.C_CHAR);
    public static final ValueLayout C_INT = pick(SysV.C_INT, WinABI.C_INT, AArch64ABI.C_INT);
    public static final ValueLayout C_FLOAT = pick(SysV.C_FLOAT, WinABI.C_FLOAT, AArch64ABI.C_FLOAT);
    public static final ValueLayout C_ULONG = pick(SysV.C_ULONG, WinABI.C_ULONG, AArch64ABI.C_ULONG);
    public static final ValueLayout C_DOUBLE = pick(SysV.C_DOUBLE, WinABI.C_DOUBLE, AArch64ABI.C_DOUBLE);
    public static final ValueLayout C_POINTER = pick(SysV.C_POINTER, WinABI.C_POINTER, AArch64ABI.C_POINTER);

    private static ValueLayout pick(ValueLayout sysv, ValueLayout win, ValueLayout aarch) {
        return switch(ABI.name()) {
            case ABI_SYSV -> sysv;
            case ABI_WINDOWS -> win;
            case ABI_AARCH64 -> aarch;
            default -> throw new UnsupportedOperationException("Unsupported ABI: " + ABI.name());
        };
    }

    public static boolean isIntegral(MemoryLayout layout) {
        return ((ArgumentClass)Utils.getAnnotation(layout, ArgumentClass.ABI_CLASS)).isIntegral();
    }

    public static boolean isPointer(MemoryLayout layout) {
        return ((ArgumentClass)Utils.getAnnotation(layout, ArgumentClass.ABI_CLASS)).isPointer();
    }

    public static ValueLayout asVarArg(ValueLayout layout) {
        return ABI.name().equals(ABI_WINDOWS) ? WinABI.asVarArg(layout) : layout;
    }
}
