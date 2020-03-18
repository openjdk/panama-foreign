/*
 *  Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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

package jdk.incubator.foreign.unsafe;

import java.lang.invoke.VarHandle;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.internal.foreign.MemoryAddressImpl;
import jdk.internal.foreign.Utils;
import static jdk.incubator.foreign.MemoryLayouts.C_CHAR;

/**
 * Unsafe methods to allow interop between sun.misc.unsafe and memory access API.
 */
public final class ForeignUnsafe {

    private ForeignUnsafe() {
        //just the one, please
    }

    // The following methods can be used in conjunction with the java.foreign API.

    /**
     * Obtain the base object (if any) associated with this address. This can be used in conjunction with
     * {@link #getUnsafeOffset(MemoryAddress)} in order to obtain a base/offset addressing coordinate pair
     * to be used with methods like {@link sun.misc.Unsafe#getInt(Object, long)} and the likes.
     *
     * @param address the address whose base object is to be obtained.
     * @return the base object associated with the address, or {@code null}.
     */
    public static Object getUnsafeBase(MemoryAddress address) {
        return ((MemoryAddressImpl)address).unsafeGetBase();
    }

    /**
     * Obtain the offset associated with this address. If {@link #getUnsafeBase(MemoryAddress)} returns {@code null} on the passed
     * address, then the offset is to be interpreted as the (absolute) numerical value associated said address.
     * Alternatively, the offset represents the displacement of a field or an array element within the containing
     * base object. This can be used in conjunction with {@link #getUnsafeBase(MemoryAddress)} in order to obtain a base/offset
     * addressing coordinate pair to be used with methods like {@link sun.misc.Unsafe#getInt(Object, long)} and the likes.
     *
     * @param address the address whose offset is to be obtained.
     * @return the offset associated with the address.
     */
    public static long getUnsafeOffset(MemoryAddress address) {
        return ((MemoryAddressImpl)address).unsafeGetOffset();
    }

    /**
     * Returns a new native memory segment with given base address and size. The returned segment has its own temporal
     * bounds, and can therefore be closed; closing such a segment does <em>not</em> result in any resource being
     * deallocated.
     * @param base the desired base address
     * @param byteSize the desired size.
     * @return a new native memory segment with given base address and size.
     * @throws IllegalArgumentException if {@code base} does not encapsulate a native memory address.
     */
    public static MemorySegment ofNativeUnchecked(MemoryAddress base, long byteSize) {
        return Utils.makeNativeSegmentUnchecked(base, byteSize);
    }

    private static VarHandle arrayHandle(MemoryLayout elemLayout, Class<?> elemCarrier) {
        return MemoryLayout.ofSequence(1, elemLayout)
                .varHandle(elemCarrier, MemoryLayout.PathElement.sequenceElement());
    }
    private final static VarHandle byteArrHandle = arrayHandle(C_CHAR, byte.class);

    /**
     * Returns a new native memory segment holding contents of the given Java String
     * @param str the Java String
     * @return a new native memory segment
     */
    public static MemorySegment makeNativeString(String str) {
        return makeNativeString(str, str.length() + 1);
    }

    private static MemorySegment makeNativeString(String str, int length) {
        MemoryLayout strLayout = MemoryLayout.ofSequence(length, C_CHAR);
        MemorySegment segment = MemorySegment.allocateNative(strLayout);
        MemoryAddress addr = segment.baseAddress();
        for (int i = 0 ; i < str.length() ; i++) {
            byteArrHandle.set(addr, i, (byte)str.charAt(i));
        }
        byteArrHandle.set(addr, (long)str.length(), (byte)0);
        return segment;
    }

    /**
     * Returns a Java String from the contents of the given C '\0' terminated string
     * @param addr the address of the C string
     * @return a Java String
     */
    public static String toJavaString(MemoryAddress addr) {
        StringBuilder buf = new StringBuilder();
        try (MemorySegment seg = ofNativeUnchecked(addr, Long.MAX_VALUE)) {
            MemoryAddress baseAddr = seg.baseAddress();
            byte curr = (byte) byteArrHandle.get(baseAddr, 0);
            long offset = 0;
            while (curr != 0) {
                buf.append((char) curr);
                curr = (byte) byteArrHandle.get(baseAddr, ++offset);
            }
        }
        return buf.toString();
    }
}
