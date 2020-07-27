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

import jdk.incubator.foreign.CSupport;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;

public class Utils {

    static int getInt(MemorySegment addr) {
        return MemoryAccess.getInt(addr);
    }

    static void setLong(MemorySegment addr, long i) {
        MemoryAccess.setLong(addr, i);
    }

    static MemoryAddress getPointer(MemorySegment addr) {
        return MemoryAccess.getAddress(addr);
    }

    static void setPointer(MemorySegment addr, MemoryAddress ptr) {
        MemoryAccess.setAddress(addr, ptr);
    }

    static MemorySegment toNativeString(String value) {
        return toNativeString(value, value.length() + 1);
    }

    static MemorySegment toNativeString(String value, int length) {
        MemoryLayout strLayout = MemoryLayout.ofSequence(length, CSupport.C_CHAR);
        MemorySegment segment = MemorySegment.allocateNative(strLayout);
        for (int i = 0 ; i < value.length() ; i++) {
            MemoryAccess.setByteAtOffset(segment, i, (byte)value.charAt(i));
        }
        MemoryAccess.setByteAtOffset(segment, value.length(), (byte)0);
        return segment;
    }

    static String toJavaString(MemoryAddress address) {
        return CSupport.toJavaStringRestricted(address);
    }

    static MemorySegment toNativeStringArray(String[] ar) {
        if (ar.length == 0) {
            return null;
        }

        MemorySegment segment = MemorySegment.allocateNative(MemoryLayout.ofSequence(ar.length, CSupport.C_POINTER));
        for (int i = 0; i < ar.length; i++) {
            MemoryAccess.setAddressAtIndex(segment, i, toNativeString(ar[i]).address());
        }

        return segment;
    }

}
