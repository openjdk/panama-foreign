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

import jdk.incubator.foreign.CSupport;
import jdk.incubator.foreign.ForeignLinker;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.ValueLayout;

public class NativeTestHelper {

    public static final ForeignLinker ABI = CSupport.getSystemLinker();

    public static boolean isIntegral(MemoryLayout layout) {
        return switch (ABI.name()) {
            case CSupport.SysV.NAME -> layout.attribute(CSupport.SysV.CLASS_ATTRIBUTE_NAME).get() == CSupport.SysV.ArgumentClass.INTEGER;
            case CSupport.Win64.NAME -> layout.attribute(CSupport.Win64.CLASS_ATTRIBUTE_NAME).get() == CSupport.Win64.ArgumentClass.INTEGER;
            case CSupport.AArch64.NAME -> layout.attribute(CSupport.AArch64.CLASS_ATTRIBUTE_NAME).get() == CSupport.AArch64.ArgumentClass.INTEGER;
            default -> throw new AssertionError("unexpected ABI: " + ABI.name());
        };
    }

    public static boolean isPointer(MemoryLayout layout) {
        return switch (ABI.name()) {
            case CSupport.SysV.NAME -> layout.attribute(CSupport.SysV.CLASS_ATTRIBUTE_NAME).get() == CSupport.SysV.ArgumentClass.POINTER;
            case CSupport.Win64.NAME -> layout.attribute(CSupport.Win64.CLASS_ATTRIBUTE_NAME).get() == CSupport.Win64.ArgumentClass.POINTER;
            case CSupport.AArch64.NAME -> layout.attribute(CSupport.AArch64.CLASS_ATTRIBUTE_NAME).get() == CSupport.AArch64.ArgumentClass.POINTER;
            default -> throw new AssertionError("unexpected ABI: " + ABI.name());
        };
    }

    public static ValueLayout asVarArg(ValueLayout layout) {
        return ABI.name().equals(CSupport.Win64.NAME) ? CSupport.Win64.asVarArg(layout) : layout;
    }
}
