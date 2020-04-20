/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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

import java.lang.invoke.VarHandle;
import java.nio.charset.Charset;
import jdk.incubator.foreign.NativeAllocationScope;
import jdk.incubator.foreign.Foreign;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import static jdk.incubator.foreign.MemoryLayouts.C_CHAR;

public final class Cstring {
    // don't create!
    private Cstring() {
    }

    private static final Foreign foreign = Foreign.getInstance();

    private static VarHandle arrayHandle(MemoryLayout elemLayout, Class<?> elemCarrier) {
        return MemoryLayout.ofSequence(elemLayout)
                .varHandle(elemCarrier, MemoryLayout.PathElement.sequenceElement());
    }
    private final static VarHandle byteArrHandle = arrayHandle(C_CHAR, byte.class);

    private static void copy(MemoryAddress addr, byte[] bytes) {
        var heapSegment = MemorySegment.ofArray(bytes);
        MemoryAddress.copy(heapSegment.baseAddress(), addr, bytes.length);
        byteArrHandle.set(addr, (long)bytes.length, (byte)0);
    }

    private static MemorySegment toCString(byte[] bytes) {
        MemoryLayout strLayout = MemoryLayout.ofSequence(bytes.length + 1, C_CHAR);
        MemorySegment segment = MemorySegment.allocateNative(strLayout);
        MemoryAddress addr = segment.baseAddress();
        copy(addr, bytes);
        return segment;
    }

    private static MemoryAddress toCString(byte[] bytes, NativeAllocationScope scope) {
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

    public static MemoryAddress toCString(String str, NativeAllocationScope scope) {
        return toCString(str.getBytes(), scope);
    }

    public static MemoryAddress toCString(String str, Charset charset, NativeAllocationScope scope) {
        return toCString(str.getBytes(charset), scope);
    }

    public static String toJavaString(MemoryAddress addr) {
        StringBuilder buf = new StringBuilder();
        MemoryAddress sizedAddr = addr.segment() == null ?
                foreign.withSize(addr, Long.MAX_VALUE) :
                addr;
        byte curr = (byte) byteArrHandle.get(sizedAddr, 0);
        long offset = 0;
        while (curr != 0) {
            buf.append((char) curr);
            curr = (byte) byteArrHandle.get(sizedAddr, ++offset);
        }
        return buf.toString();
    }
}
