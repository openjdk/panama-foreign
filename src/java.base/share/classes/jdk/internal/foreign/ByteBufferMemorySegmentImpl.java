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

package jdk.internal.foreign;

import java.foreign.MemoryScope;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public class ByteBufferMemorySegmentImpl extends MemorySegmentImpl {

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

    // Keep a reference to the buffer so it is kept alive while the segment is alive
    final ByteBuffer ref;

    private ByteBufferMemorySegmentImpl(ByteBuffer bb, MemoryScope scope, Object base, long min, long length) {
        super(scope, base, min, length);
        this.ref = bb;
    }

    @Override
    public MemorySegmentImpl resize(long offset, long newLength) {
        if (outOfBounds(offset, newLength)) {
            throw new IllegalArgumentException();
        }
        return new ByteBufferMemorySegmentImpl(ref, scope, base, min + offset, newLength);
    }

    static Object getBufferBase(ByteBuffer bb) {
        return UNSAFE.getReference(bb, BYTE_BUFFER_BASE);
    }

    static long getBufferAddress(ByteBuffer bb) {
        return UNSAFE.getLong(bb, BUFFER_ADDRESS);
    }

    static ByteBufferMemorySegmentImpl of(MemoryScope scope, ByteBuffer bb) {
        Object bbBase = getBufferBase(bb);
        long bbAddress = getBufferAddress(bb);

        int pos = bb.position();
        int limit = bb.limit();

        return new ByteBufferMemorySegmentImpl(bb, scope, bbBase,
                bbAddress + pos, limit - pos);
    }
}
