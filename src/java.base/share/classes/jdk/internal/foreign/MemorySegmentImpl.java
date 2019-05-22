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
package jdk.internal.foreign;

import jdk.internal.misc.Unsafe;

import java.foreign.MemoryAddress;
import java.foreign.MemorySegment;
import java.foreign.MemoryScope;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public class MemorySegmentImpl implements MemorySegment {

    public static final MemorySegmentImpl ofNothing(MemoryScope scope) {
        return ofNative(scope, 0, 0);
    }

    public static final Unsafe UNSAFE = Unsafe.getUnsafe();

    public static final long BYTE_BUFFER_BASE;
    public static final long BUFFER_ADDRESS;

    static {
        try {
            BYTE_BUFFER_BASE = UNSAFE.objectFieldOffset(ByteBuffer.class.getDeclaredField("hb"));
            BUFFER_ADDRESS = UNSAFE.objectFieldOffset(Buffer.class.getDeclaredField("address"));
        }
        catch (Exception e) {
            throw new InternalError(e);
        }
    }

    public final Object base;
    public final long min;
    final long length;
    final MemoryScope scope;
    final MemoryAddress baseAddr;

    @Override
    public MemoryAddress baseAddress() {
        return baseAddr;
    }

    @Override
    public MemoryScope scope() {
        return scope;
    }

    @Override
    public long bytesSize() {
        return length;
    }

    private MemorySegmentImpl(MemoryScope scope, Object base, long min, long length) {
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
        this.scope = scope;
        this.baseAddr = new MemoryAddressImpl(this);
    }

    public static MemorySegmentImpl ofNative(MemoryScope scope, long min, long length) {
        return new MemorySegmentImpl(scope, null, min, length);
    }

    public static MemorySegmentImpl ofHeap(MemoryScope scope, Object base, long min, long length) {
        checkOverflow(min, length);
        return new MemorySegmentImpl(scope, base, min, length);
    }

    private static void checkOverflow(long min, long length) {
        // we never access at `length`
        addUnsignedExact(min, length == 0 ? 0 : length - 1);
    }

    public static MemorySegmentImpl ofByteBuffer(MemoryScope scope, ByteBuffer bb) {
        // For a direct ByteBuffer base == null and address is absolute
        Object base = getBufferBase(bb);
        long address = getBufferAddress(bb);

        int pos = bb.position();
        int limit = bb.limit();
        return new MemorySegmentImpl(scope, base, address + pos, limit - pos) {
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
            public MemorySegmentImpl resize(long offset, long newLength) {
                throw new UnsupportedOperationException(); // bb ref would be lost otherwise
            }
        };
    }

    void checkRange(long offset, long length) {
        if (outOfBounds(offset, length)) {
            throw new IllegalStateException();
        }
    }

    private boolean outOfBounds(long offset, long length) {
        return (length < 0 ||
                offset < 0 ||
                offset > this.length - length); // careful of overflow
    }

    @Override
    public MemorySegmentImpl resize(long offset, long newLength) {
        if (outOfBounds(offset, newLength)) {
            throw new IllegalArgumentException();
        }
        return new MemorySegmentImpl(scope, base, min + offset, newLength);
    }

    void checkAlive() {
        ((AbstractMemoryScopeImpl)scope()).checkAlive();
    }

     static long addUnsignedExact(long a, long b) {
        long result = a + b;
        if(Long.compareUnsigned(result, a) < 0) {
            throw new ArithmeticException(
                "Unsigned overflow: "
                    + Long.toUnsignedString(a) + " + "
                    + Long.toUnsignedString(b));
        }

        return result;
    }

    @Override
    public String toString() {
        return base != null ?
                "HeapRegion{base=" + base + ", length=" + length + "}" :
                "NativeRegion{min=" + min + ", length=" + length + "}";
    }

    static Object getBufferBase(ByteBuffer bb) {
        return UNSAFE.getReference(bb, BYTE_BUFFER_BASE);
    }

    static long getBufferAddress(ByteBuffer bb) {
        return UNSAFE.getLong(bb, BUFFER_ADDRESS);
    }
}
