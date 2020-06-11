/*
 * Copyright (c) 2008, 2019, Oracle and/or its affiliates. All rights reserved.
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

import java.nio.file.DirectoryStream;

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.NativeAllocationScope;
import jdk.incubator.jbind.core.CString;

/**
 * Unix system and library calls.
 */

public abstract class UnixNativeDispatcher {
    static final int PATH_MAX = 1024;

    public abstract long opendir(String path);
    public abstract String readdir(long dir);
    public abstract void closedir(long dir);
    public abstract UnixFileAttributes readAttributes(String path);

    public DirectoryStream<String> newDirectoryStream(String path) {
        return new DirectoryIterator(this, path);
    }

    public static final UnixNativeDispatcher FFI  = new UnixNativeDispatcher() {
        public long opendir(String path) { return opendirFFI(path); }
        public String readdir(long dir) { return readdirFFI(dir); }
        public void closedir(long dir) { closedirFFI(dir); }
        public UnixFileAttributes readAttributes(String path) { return statFFI(path); }
    };

    public static final UnixNativeDispatcher JNI = new UnixNativeDispatcher() {
        public long opendir(String path) { return opendirJNI(path); }
        public String readdir(long dir) { return readdirJNI(dir); }
        public void closedir(long dir) { closedirJNI(dir); }
        public UnixFileAttributes readAttributes(String path) { return statJNI(path); }
    };

    public static UnixFileAttributes statFFI(String path) {
        try (NativeAllocationScope scope = NativeAllocationScope.unboundedScope()) {
            MemoryAddress file = CString.toCString(path, scope);
            LibC.stat64 buffer = LibC.stat64.allocate(scope::allocate);
            LibC.stat64(file, buffer.ptr());
            return new UnixFileAttributes(buffer);
        }
    }

    public static long opendirFFI(String path) {
        try (NativeAllocationScope scope = NativeAllocationScope.unboundedScope()) {
            MemoryAddress dir = LibC.opendir(CString.toCString(path, scope));
            if (dir.equals(MemoryAddress.NULL)) {
                throw new RuntimeException();
            }
            return dir.toRawLongValue();
        }
    }

    /**
     * closedir(DIR* dirp)
     */
    public static void closedirFFI(long dir) {
        MemoryAddress dirp = MemoryAddress.ofLong(dir);
        LibC.closedir(dirp);
    }

    /**
     * struct dirent* readdir(DIR *dirp)
     *
     * @return  dirent->d_name
     */
    public static String readdirFFI(long dir) {
        MemoryAddress dirp = MemoryAddress.ofLong(dir);
        MemoryAddress pdir = resizePointer(LibC.readdir(dirp), LibC.dirent.sizeof());
        if (pdir.equals(MemoryAddress.NULL)) {
            return null;
        }

        return CString.toJavaString(LibC.dirent.at(pdir).d_name$ptr());
    }

    public static MemoryAddress resizePointer(MemoryAddress addr, long size) {
        if (addr.segment() == null) {
            return MemorySegment.ofNativeRestricted(addr, size, null, null, null).baseAddress();
        } else {
            return addr;
        }
    }

    public static native long opendirJNI(String path);
    public static native String readdirJNI(long dir);
    public static native void closedirJNI(long dir);
    public static UnixFileAttributes statJNI(String path) {
        UnixFileAttributes attrs = new UnixFileAttributes();
        statJNI(path, attrs);
        return attrs;
    }

    public static native void statJNI(String path, UnixFileAttributes attrs);
    static native int initJNI();

    static {
        System.loadLibrary("UnixNativeDispatcher");
        initJNI();
    }
}
