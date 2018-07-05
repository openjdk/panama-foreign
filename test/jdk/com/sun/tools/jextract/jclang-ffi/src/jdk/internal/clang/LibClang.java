/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
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
package jdk.internal.clang;

import clang.CXString.CXString;

import java.lang.invoke.MethodHandles;
import java.foreign.Libraries;
import java.foreign.Library;
import java.foreign.memory.Pointer;

public class LibClang {
    private static final boolean DEBUG = Boolean.getBoolean("libclang.debug");

    static final clang.Index lib;
    private static final clang.CXString lcxstr;

    static {
        if (DEBUG) {
            System.err.println("Loading LibClang FFI");
        }
        Library libclang = Libraries.loadLibrary(MethodHandles.lookup(), "clang");
        lib = Libraries.bind(clang.Index.class, libclang);
        lcxstr = Libraries.bind(clang.CXString.class, libclang);
    }

    public static Index createIndex() {
        Index index = new Index(lib.clang_createIndex(0, 0));
        lib.clang_toggleCrashRecovery(0);
        if (DEBUG) {
            System.err.println("LibClang crash recovery disabled");
        }
        return index;
    }

    public static String CXStrToString(CXString cxstr) {
        Pointer<Byte> buf = lcxstr.clang_getCString(cxstr);
        String str = Pointer.toString(buf);
        lcxstr.clang_disposeString(cxstr);
        return str;
    }

    public static String version() {
        return CXStrToString(lib.clang_getClangVersion());
    }
}
