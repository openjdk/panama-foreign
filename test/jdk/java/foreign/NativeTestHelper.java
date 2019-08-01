/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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
 *   Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 *
 */

import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemoryLayouts;
import jdk.incubator.foreign.ValueLayout;
import jdk.internal.foreign.Utils;
import jdk.internal.foreign.abi.ArgumentClass;
import sun.security.action.GetPropertyAction;

public class NativeTestHelper {

    private static final boolean isWindows;
    private static final boolean isAArch64;
    private static final boolean isX86;


    static {
        String arch = GetPropertyAction.privilegedGetProperty("os.arch");
        isX86 = arch.equals("amd64") || arch.equals("x86_64");
        isAArch64 = arch.equals("aarch64");
        String os = GetPropertyAction.privilegedGetProperty("os.name");
        isWindows = os.startsWith("Windows");
    }

    public static final ValueLayout C_CHAR = pick(MemoryLayouts.SysV.C_CHAR, MemoryLayouts.WinABI.C_CHAR, MemoryLayouts.AArch64ABI.C_CHAR);
    public static final ValueLayout C_INT = pick(MemoryLayouts.SysV.C_INT, MemoryLayouts.WinABI.C_INT, MemoryLayouts.AArch64ABI.C_INT);
    public static final ValueLayout C_FLOAT = pick(MemoryLayouts.SysV.C_FLOAT, MemoryLayouts.WinABI.C_FLOAT, MemoryLayouts.AArch64ABI.C_FLOAT);
    public static final ValueLayout C_ULONG = pick(MemoryLayouts.SysV.C_ULONG, MemoryLayouts.WinABI.C_ULONG, MemoryLayouts.AArch64ABI.C_ULONG);
    public static final ValueLayout C_DOUBLE = pick(MemoryLayouts.SysV.C_DOUBLE, MemoryLayouts.WinABI.C_DOUBLE, MemoryLayouts.AArch64ABI.C_DOUBLE);
    public static final ValueLayout C_POINTER = pick(MemoryLayouts.SysV.C_POINTER, MemoryLayouts.WinABI.C_POINTER, MemoryLayouts.AArch64ABI.C_POINTER);

    private static ValueLayout pick(ValueLayout sysv, ValueLayout win, ValueLayout aarch) {
        if (isX86) {
            return isWindows ? win : sysv;
        } else if (isAArch64) {
            return aarch;
        } else {
            throw new UnsupportedOperationException("Unsupported platform");
        }
    }

    public static boolean isIntegral(MemoryLayout layout) {
        return ((ArgumentClass)Utils.getAnnotation(layout, ArgumentClass.ABI_CLASS)).isIntegral();
    }

    public static boolean isPointer(MemoryLayout layout) {
        return ((ArgumentClass)Utils.getAnnotation(layout, ArgumentClass.ABI_CLASS)).isPointer();
    }

    public static ValueLayout asVarArg(ValueLayout layout) {
        return isWindows ? MemoryLayouts.WinABI.asVarArg(layout) : layout;
    }
}
