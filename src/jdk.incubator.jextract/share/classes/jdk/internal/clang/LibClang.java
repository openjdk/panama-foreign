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
 *   Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 *
 */
package jdk.internal.clang;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.LibraryLookup;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;
import jdk.internal.clang.libclang.Index_h;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

public class LibClang {
    private static final boolean DEBUG = Boolean.getBoolean("libclang.debug");
    private static final boolean CRASH_RECOVERY = Boolean.getBoolean("libclang.crash_recovery");
    private static final boolean IS_WINDOWS = System.getProperty("os.name").startsWith("Windows");

    private final static MemorySegment disableCrashRecovery =
            CLinker.toCString("LIBCLANG_DISABLE_CRASH_RECOVERY=" + CRASH_RECOVERY, ResourceScope.newImplicitScope());

    static {
        if (!CRASH_RECOVERY) {
            //this is an hack - needed because clang_toggleCrashRecovery only takes effect _after_ the
            //first call to createIndex.
            try {
                CLinker linker = CLinker.getInstance();
                String putenv = IS_WINDOWS ? "_putenv" : "putenv";
                MethodHandle PUT_ENV = linker.downcallHandle(LibraryLookup.ofDefault().lookup(putenv).get(),
                                MethodType.methodType(int.class, MemoryAddress.class),
                                FunctionDescriptor.of(CLinker.C_INT, CLinker.C_POINTER));
                int res = (int) PUT_ENV.invokeExact(disableCrashRecovery.address());
            } catch (Throwable ex) {
                throw new ExceptionInInitializerError(ex);
            }
        }
    }

    public static Index createIndex(boolean local) {
        Index index = new Index(Index_h.clang_createIndex(local ? 1 : 0, 0));
        if (DEBUG) {
            System.err.println("LibClang crash recovery " + (CRASH_RECOVERY ? "enabled" : "disabled"));
        }
        return index;
    }

    public static String CXStrToString(MemorySegment cxstr) {
        MemoryAddress buf = Index_h.clang_getCString(cxstr);
        String str = CLinker.toJavaString(buf);
        Index_h.clang_disposeString(cxstr);
        return str;
    }

    public static String version() {
        try (ResourceScope scope = ResourceScope.newConfinedScope()) {
            return CXStrToString(Index_h.clang_getClangVersion(scope));
        }
    }
}
