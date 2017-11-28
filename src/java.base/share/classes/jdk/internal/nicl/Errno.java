/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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
package jdk.internal.nicl;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nicl.NativeLibrary;
import java.nicl.RuntimeSupport;
import java.nicl.types.Pointer;
import java.nicl.types.Transformer;

public class Errno {
    private static String getErrnoLocationFunctionName() {
        String osName = System.getProperty("os.name");
        switch (osName) {
        case "Linux": return "__errno_location";
            //case "Mac OS X": return "__error"; // FIXME: this is cheating
        case "Windows": return null; // no errno here
        default:
            return null;
            // FIXME: Make this a hard error at some point
            //throw new Error("Unknown os: " + osName);
        }
    }

    public static boolean platformHasErrno() {
        return getErrnoLocationFunctionName() != null;
    }

    private final MethodHandle errnoMH;
    private final MethodHandle strerrorMH;

    public Errno() {
        try {
            errnoMH = Util.findNative(getErrnoLocationFunctionName(), MethodType.methodType(long.class));
            strerrorMH = Util.findNative("strerror", MethodType.methodType(long.class, int.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public int errno() {
        try {
            Pointer<Integer> p = RuntimeSupport.createPtr((long)errnoMH.invoke(), NativeLibrary.createLayout(int.class));
            return p.lvalue().get();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public String strerror(int errno) {
        try {
            Pointer<Byte> p = RuntimeSupport.createPtr((long)strerrorMH.invoke(errno), NativeLibrary.createLayout(byte.class));
            return Transformer.toString(p);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
