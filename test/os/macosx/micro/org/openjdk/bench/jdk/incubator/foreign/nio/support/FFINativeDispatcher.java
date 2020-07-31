/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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

package org.openjdk.bench.jdk.incubator.foreign.nio.support;

import jdk.incubator.foreign.CSupport;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.NativeScope;

public class FFINativeDispatcher extends NativeDispatcher {
    public MemoryAddress opendir(String path) { return opendirFFI(path); }
    public String readdir(MemoryAddress dir) { return readdirFFI(dir); }
    public void closedir(MemoryAddress dir) { closedirFFI(dir); }
    public UnixFileAttributes readAttributes(String path) { return statFFI(path); }

    public static UnixFileAttributes statFFI(String path) {
        try (NativeScope scope = NativeScope.unboundedScope()) {
            var file = CSupport.toCString(path, scope);
            LibC.stat64 buffer = LibC.stat64.allocate(scope::allocate);
            LibC.stat64(file, buffer);
            return new UnixFileAttributes(buffer);
        }
    }

    public static MemoryAddress opendirFFI(String path) {
        try (NativeScope scope = NativeScope.unboundedScope()) {
            MemoryAddress dir = LibC.opendir(CSupport.toCString(path, scope));
            if (dir.equals(MemoryAddress.NULL)) {
                throw new RuntimeException();
            }
            return dir;
        }
    }

    /**
     * closedir(DIR* dirp)
     */
    public static void closedirFFI(MemoryAddress dir) {
        LibC.closedir(dir);
    }

    /**
     * struct dirent* readdir(DIR *dirp)
     *
     * @return  dirent->d_name
     */
    public static String readdirFFI(MemoryAddress dir) {
        MemoryAddress pdir = LibC.readdir(dir);
        if (pdir.equals(MemoryAddress.NULL)) {
            return null;
        }

        MemorySegment segment = MemorySegment.ofNativeRestricted()
                .asSlice(pdir.toRawLongValue(), LibC.dirent.sizeof());
        return CSupport.toJavaString(LibC.dirent.at(segment).d_name$ptr());
    }
};
