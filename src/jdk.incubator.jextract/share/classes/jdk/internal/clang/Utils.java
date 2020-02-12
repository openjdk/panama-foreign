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

import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.LibraryLookup;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryHandles;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.SystemABI;
import jdk.internal.jextract.impl.LayoutUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;

public class Utils {
    public static final VarHandle BYTE_VH = LayoutUtils.C_CHAR.varHandle(byte.class);
    public static final VarHandle BYTE_ARR_VH = MemoryHandles.withStride(BYTE_VH, 1);
    public static final VarHandle INT_VH = LayoutUtils.C_INT.varHandle(int.class);
    public static final VarHandle LONG_VH = LayoutUtils.C_LONGLONG.varHandle(long.class);
    public static final VarHandle POINTER_VH = LayoutUtils.C_POINTER.varHandle(MemoryAddress.class);
    public static final VarHandle POINTER_ARR_VH = MemoryHandles.withStride(POINTER_VH, 8);

    private static final MethodHandle STRLEN;
    private static final MethodHandle STRCPY;

    static {
        try {
            STRLEN = SystemABI.getInstance().downcallHandle(
                    LibraryLookup.ofDefault().lookup("strlen"),
                    MethodType.methodType(int.class, MemoryAddress.class),
                    FunctionDescriptor.of(LayoutUtils.C_INT, LayoutUtils.C_POINTER));

            STRCPY = SystemABI.getInstance().downcallHandle(
                    LibraryLookup.ofDefault().lookup("strcpy"),
                    MethodType.methodType(MemoryAddress.class, MemoryAddress.class, MemoryAddress.class),
                    FunctionDescriptor.of(LayoutUtils.C_POINTER, LayoutUtils.C_POINTER, LayoutUtils.C_POINTER));
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    static int getInt(MemoryAddress addr) {
        return (int)INT_VH.get(addr);
    }

    static void setInt(MemoryAddress addr, int i) {
        INT_VH.set(addr, i);
    }

    static int getLong(MemoryAddress addr) {
        return (int)LONG_VH.get(addr);
    }

    static void setLong(MemoryAddress addr, long i) {
        LONG_VH.set(addr, i);
    }

    static byte getByte(MemoryAddress addr) {
        return (byte)BYTE_VH.get(addr);
    }

    static MemoryAddress getPointer(MemoryAddress addr) {
        return (MemoryAddress)POINTER_VH.get(addr);
    }

    static void setPointer(MemoryAddress addr, MemoryAddress ptr) {
        POINTER_VH.set(addr, ptr);
    }

    static MemorySegment toNativeString(String value) {
        return toNativeString(value, value.length() + 1);
    }

    static MemorySegment toNativeString(String value, int length) {
        MemoryLayout strLayout = MemoryLayout.ofSequence(length, LayoutUtils.C_CHAR);
        MemorySegment segment = MemorySegment.allocateNative(strLayout);
        MemoryAddress addr = segment.baseAddress();
        for (int i = 0 ; i < value.length() ; i++) {
            BYTE_ARR_VH.set(addr, i, (byte)value.charAt(i));
        }
        BYTE_ARR_VH.set(addr, (long)value.length(), (byte)0);
        return segment;
    }

    static int strlen(MemoryAddress str) {
        try {
            return (int)STRLEN.invokeExact(str);
        } catch (Throwable ex) {
            throw new AssertionError(ex);
        }
    }

    static MemoryAddress strcpy(MemoryAddress dest, MemoryAddress src) {
        try {
            return (MemoryAddress)STRCPY.invokeExact(dest, src);
        } catch (Throwable ex) {
            throw new AssertionError(ex);
        }
    }

    static String toJavaString(MemoryAddress address) {
        try (MemorySegment str = MemorySegment.allocateNative(strlen(address) + 1)) {
            strcpy(str.baseAddress(), address);
            StringBuilder buf = new StringBuilder();
            byte curr = (byte)BYTE_ARR_VH.get(str.baseAddress(), 0);
            long offset = 0;
            while (curr != 0) {
                buf.append((char)curr);
                curr = (byte)BYTE_ARR_VH.get(str.baseAddress(), ++offset);
            }
            return buf.toString();
        }
    }

    static MemorySegment toNativeStringArray(String[] ar) {
        if (ar.length == 0) {
            return null;
        }

        MemorySegment segment = MemorySegment.allocateNative(MemoryLayout.ofSequence(ar.length, LayoutUtils.C_POINTER));
        for (int i = 0; i < ar.length; i++) {
            POINTER_ARR_VH.set(segment.baseAddress(), i, toNativeString(ar[i]).baseAddress());
        }

        return segment;
    }

}
