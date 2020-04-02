/*
 *  Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.  Oracle designates this
 *  particular file as subject to the "Classpath" exception as provided
 *  by Oracle in the LICENSE file that accompanied this code.
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
package jdk.internal.foreign;

import jdk.incubator.foreign.Foreign;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.SystemABI;
import jdk.internal.foreign.abi.aarch64.AArch64ABI;
import jdk.internal.foreign.abi.x64.sysv.SysVx64ABI;
import jdk.internal.foreign.abi.x64.windows.Windowsx64ABI;
import sun.security.action.GetPropertyAction;

public class InternalForeign implements Foreign {

    private static final String foreignAccess
            = GetPropertyAction.privilegedGetProperty("jdk.incubator.foreign.Foreign", "deny");
    private static final InternalForeign INSTANCE = new InternalForeign();

    private InternalForeign() {}

    public static InternalForeign getInstance() {
        checkRestrictedAccess();
        return getInstancePrivileged();
    }

    public static InternalForeign getInstancePrivileged() {
        return INSTANCE;
    }

    @Override
    public MemoryAddress withSize(MemoryAddress base, long byteSize) throws IllegalAccessError {
        return Utils.makeNativeSegmentUnchecked(base.toRawLongValue(), byteSize, null, false)
                .baseAddress();
    }

    @Override
    public MemorySegment asMallocSegment(MemoryAddress base, long byteSize) throws IllegalAccessError {
        return Utils.makeNativeSegmentUnchecked(base.toRawLongValue(), byteSize, Thread.currentThread(), true);
    }

    @Override
    public MemorySegment asUnconfined(MemorySegment segment) {
        return ((MemorySegmentImpl)segment).asUnconfined();
    }

    @Override
    public SystemABI getSystemABI() {
        String arch = System.getProperty("os.arch");
        String os = System.getProperty("os.name");
        if (arch.equals("amd64") || arch.equals("x86_64")) {
            if (os.startsWith("Windows")) {
                return Windowsx64ABI.getInstance();
            } else {
                return SysVx64ABI.getInstance();
            }
        } else if (arch.equals("aarch64")) {
            return AArch64ABI.getInstance();
        }
        throw new UnsupportedOperationException("Unsupported os or arch: " + os + ", " + arch);
    }

    private static void checkRestrictedAccess() {
        switch (foreignAccess) {
            case "deny" -> throwIllegalAccessError(foreignAccess);
            case "warn" -> System.err.println("WARNING: Accessing jdk.incubator.foreign.Foreign.");
            case "debug" -> {
                StringBuilder sb = new StringBuilder("DEBUG: Accessing jdk.incubator.foreign.Foreign.");
                StackWalker.getInstance().walk(s -> {
                     s
                     .forEach(f -> sb.append(System.lineSeparator()).append("\tat ").append(f));
                    return null;
                });
                System.out.println(sb.toString());
            }
            case "permit" -> {}
            default -> throwIllegalAccessError(foreignAccess);
        }
    }

    private static void throwIllegalAccessError(String value) {
        throw new IllegalAccessError("Can not access jdk.incubator.foreign.Foreign." +
                " System property 'jdk.incubator.foreign.Foreign' is set to '" + value + "'");
    }
}
