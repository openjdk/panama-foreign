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

package jdk.incubator.jbind.core;

import java.lang.invoke.VarHandle;
import java.nio.charset.Charset;
import jdk.incubator.foreign.NativeScope;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import static jdk.incubator.foreign.CSupport.C_CHAR;

public final class CString {
    private CString() {
        super();
    };

    private static VarHandle arrayHandle(MemoryLayout elemLayout, Class<?> elemCarrier) {
        return MemoryLayout.ofSequence(elemLayout)
                .varHandle(elemCarrier, MemoryLayout.PathElement.sequenceElement());
    }
    private final static VarHandle byteArrHandle = arrayHandle(C_CHAR, byte.class);

    private static void copy(MemoryAddress addr, byte[] bytes) {
        var heapSegment = MemorySegment.ofArray(bytes);
        addr.segment()
            .asSlice(addr.segmentOffset(), bytes.length)
            .copyFrom(heapSegment);
        byteArrHandle.set(addr, (long)bytes.length, (byte)0);
    }

    private static MemorySegment toCString(byte[] bytes) {
        MemoryLayout strLayout = MemoryLayout.ofSequence(bytes.length + 1, C_CHAR);
        MemorySegment segment = MemorySegment.allocateNative(strLayout);
        MemoryAddress addr = segment.baseAddress();
        copy(addr, bytes);
        return segment;
    }

    private static MemoryAddress toCString(byte[] bytes, NativeScope scope) {
        MemoryLayout strLayout = MemoryLayout.ofSequence(bytes.length + 1, C_CHAR);
        MemoryAddress addr = scope.allocate(strLayout);
        copy(addr, bytes);
        return addr;
    }

    public static void copy(MemoryAddress addr, String str) {
        copy(addr, str.getBytes());
    }

    public static void copy(MemoryAddress addr, String str, Charset charset) {
        copy(addr, str.getBytes(charset));
    }

    public static MemorySegment toCString(String str) {
         return toCString(str.getBytes());
    }

    public static MemorySegment toCString(String str, Charset charset) {
         return toCString(str.getBytes(charset));
    }

    public static MemoryAddress toCString(String str, NativeScope scope) {
        return toCString(str.getBytes(), scope);
    }

    public static MemoryAddress toCString(String str, Charset charset, NativeScope scope) {
        return toCString(str.getBytes(charset), scope);
    }

    public static String toJavaString(MemoryAddress addr) {
        StringBuilder buf = new StringBuilder();
        MemoryAddress baseAddr = addr.segment() != null ?
                addr :
                MemorySegment.ofNativeRestricted(addr, Long.MAX_VALUE, Thread.currentThread(),
                        null, null).baseAddress();
        byte curr = (byte) byteArrHandle.get(baseAddr, 0);
        long offset = 0;
        while (curr != 0) {
            buf.append((char) curr);
            curr = (byte) byteArrHandle.get(baseAddr, ++offset);
        }
        return buf.toString();
    }
}
