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

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.internal.clang.libclang.Index_h;

public class LibClang {
    private static final boolean DEBUG = Boolean.getBoolean("libclang.debug");
    private static final boolean CRASH_RECOVERY = Boolean.getBoolean("libclang.crash_recovery");

    public static Index createIndex(boolean local) {
        Index index = new Index(Index_h.clang_createIndex(local ? 1 : 0, 0));
        Index_h.clang_toggleCrashRecovery(CRASH_RECOVERY ? 1 : 0);
        if (DEBUG && !CRASH_RECOVERY) {
            System.err.println("LibClang crash recovery disabled");
        }
        return index;
    }

    public static String CXStrToString(MemorySegment cxstr) {
        MemoryAddress buf = Index_h.clang_getCString(cxstr);
        String str = Utils.toJavaString(buf);
        Index_h.clang_disposeString(cxstr);
        return str;
    }

    public static String version() {
        return CXStrToString(Index_h.clang_getClangVersion());
    }
}
