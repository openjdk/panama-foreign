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

import java.nio.file.DirectoryStream;

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.NativeScope;
import jdk.incubator.foreign.CSupport;

/**
 * Unix system and library calls.
 */

public abstract class NativeDispatcher {
    static final int PATH_MAX = 1024;

    public abstract MemoryAddress opendir(String path);
    public abstract String readdir(MemoryAddress dir);
    public abstract void closedir(MemoryAddress dir);
    public abstract UnixFileAttributes readAttributes(String path);

    public DirectoryStream<String> newDirectoryStream(String path) {
        return new DirectoryIterator(this, path);
    }

    public static final NativeDispatcher FFI  = new FFINativeDispatcher();

    public static final NativeDispatcher JNI = new NativeDispatcher() {
        public MemoryAddress opendir(String path) { return MemoryAddress.ofLong(opendirJNI(path)); }
        public String readdir(MemoryAddress dir) { return readdirJNI(dir.toRawLongValue()); }
        public void closedir(MemoryAddress dir) { closedirJNI(dir.toRawLongValue()); }
        public UnixFileAttributes readAttributes(String path) { return statJNI(path); }
    };

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
        System.loadLibrary("NativeDispatcher");
        initJNI();
    }
}
