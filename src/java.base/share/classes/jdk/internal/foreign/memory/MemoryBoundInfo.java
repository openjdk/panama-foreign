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
package jdk.internal.foreign.memory;

import java.nio.ByteBuffer;
import jdk.internal.foreign.Util;

public class MemoryBoundInfo {
    
    public static final MemoryBoundInfo EVERYTHING = new MemoryBoundInfo(null, 0, Long.MAX_VALUE) {
        @Override
        public void checkBounds(long offset) {
            // any offset is in bounds
        }

        @Override
        void checkRange(long offset, long length) {
            checkOverflow(offset, length);
        }
    };
    public static final MemoryBoundInfo NOTHING = ofNative(0, 0);

    public final Object base;
    public final long min;
    private final long length;

    private MemoryBoundInfo(Object base, long min, long length) {
        if(length < 0) {
            throw new IllegalArgumentException("length must be positive");
        }
        if(base != null && min < 0) {
            throw new IllegalArgumentException("min must be positive if base is used");
        }
        checkOverflow(min, length);
        this.base = base;
        this.min = min;
        this.length = length;
    }

    public static MemoryBoundInfo ofNative(long min, long length) {
        return new MemoryBoundInfo(null, min, length);
    }

    public static MemoryBoundInfo ofHeap(Object base, long min, long length) {
        checkOverflow(min, length);
        return new MemoryBoundInfo(base, min, length);
    }

    public static MemoryBoundInfo ofByteBuffer(ByteBuffer bb) {
        // For a direct ByteBuffer base == null and address is absolute
        Object base = Util.getBufferBase(bb);
        long address = Util.getBufferAddress(bb);
        int pos = bb.position();
        int limit = bb.limit();
        return new MemoryBoundInfo(base, address + pos, limit - pos) {
            // Keep a reference to the buffer so it is kept alive while the
            // region is alive
            final Object ref = bb;

            // @@@ For heap ByteBuffer the addr() will throw an exception
            //     need to adapt a pointer and memory region be more cognizant
            //     of the double addressing mode
            //     the direct address for a heap buffer needs to behave
            //     differently see JNI GetPrimitiveArrayCritical for clues on
            //     behaviour.

            // @@@ Same trick can be performed to create a pointer to a
            //     primitive array

            @Override
            MemoryBoundInfo limit(long newLength) {
                throw new UnsupportedOperationException(); // bb ref would be lost otherwise
            }
        };
    }

    private static void checkOverflow(long min, long length) {
        // we never access at `length`
        Util.addUnsignedExact(min, length == 0 ? 0 : length - 1);
    }

    public void checkBounds(long offset) {
        if (length == 0 && offset == 0) {
            return;
        }
        if (offset < 0 || offset >= length) {
            // FIXME: Objects.checkIndex(long, long) ?
            throw new IndexOutOfBoundsException("offset=0x" + Long.toHexString(offset) + " length=0x" + Long.toHexString(length));
        }
    }

    void checkRange(long offset, long length) {
        checkBounds(offset);
        if (length != 0) {
            checkBounds(offset + length - 1);
        }
    }

    MemoryBoundInfo limit(long newLength) {
        if (newLength > length || newLength < 0) {
            throw new IllegalArgumentException();
        }
        return new MemoryBoundInfo(base, min, newLength);
    }
}
