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
import jdk.incubator.foreign.MemoryLayouts.WinABI;
import jdk.incubator.foreign.SystemABI;
import jdk.incubator.foreign.SystemABI.Type;
import jdk.incubator.foreign.ValueLayout;
import jdk.internal.foreign.Utils;

import static jdk.incubator.foreign.SystemABI.ABI_WINDOWS;

public class NativeTestHelper {

    public static final SystemABI ABI = SystemABI.getInstance();

    public static boolean isIntegral(MemoryLayout layout) {
        var optAbiType = layout.abiType();
        if (!optAbiType.isPresent()) {
            return false;
        }
        return switch(optAbiType.get()) {
            case BOOL, UNSIGNED_CHAR, SIGNED_CHAR, CHAR, SHORT, UNSIGNED_SHORT,
                INT, UNSIGNED_INT, LONG, UNSIGNED_LONG, LONG_LONG, UNSIGNED_LONG_LONG -> true;
            default -> false;
        };
    }

    public static boolean isPointer(MemoryLayout layout) {
        var optAbiType = layout.abiType();
        return optAbiType.isPresent()? optAbiType.get() == Type.POINTER : false;
    }

    public static ValueLayout asVarArg(ValueLayout layout) {
        return ABI.name().equals(ABI_WINDOWS) ? WinABI.asVarArg(layout) : layout;
    }
}
