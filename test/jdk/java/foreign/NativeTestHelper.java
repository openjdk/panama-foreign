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
import jdk.internal.foreign.CABI;

import static jdk.internal.foreign.PlatformLayouts.*;

public class NativeTestHelper {

    public static final CABI ABI = CABI.current();

    public static boolean isIntegral(MemoryLayout layout) {
        return switch (ABI) {
            case SysV -> layout.attribute(SysV.CLASS_ATTRIBUTE_NAME).get() == SysV.ArgumentClass.INTEGER;
            case Win64 -> layout.attribute(Win64.CLASS_ATTRIBUTE_NAME).get() == Win64.ArgumentClass.INTEGER;
            case AArch64 -> layout.attribute(AArch64.CLASS_ATTRIBUTE_NAME).get() == AArch64.ArgumentClass.INTEGER;
        };
    }

    public static boolean isPointer(MemoryLayout layout) {
        return switch (ABI) {
            case SysV -> layout.attribute(SysV.CLASS_ATTRIBUTE_NAME).get() == SysV.ArgumentClass.POINTER;
            case Win64 -> layout.attribute(Win64.CLASS_ATTRIBUTE_NAME).get() == Win64.ArgumentClass.POINTER;
            case AArch64 -> layout.attribute(AArch64.CLASS_ATTRIBUTE_NAME).get() == AArch64.ArgumentClass.POINTER;
        };
    }
}
