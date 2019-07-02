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

import jdk.internal.misc.Unsafe;

import jdk.incubator.foreign.MemorySegment;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public class BufferScope extends MemorySegmentImpl.Scope {

    private static Unsafe unsafe = Unsafe.getUnsafe();

    private static final long BYTE_BUFFER_BASE;
    private static final long BUFFER_ADDRESS;

    static {
        try {
            BYTE_BUFFER_BASE = unsafe.objectFieldOffset(ByteBuffer.class.getDeclaredField("hb"));
            BUFFER_ADDRESS = unsafe.objectFieldOffset(Buffer.class.getDeclaredField("address"));
        }
        catch (Exception e) {
            throw new InternalError(e);
        }
    }

    // Keep a reference to the buffer so it is kept alive while the segment is alive
    private ByteBuffer bb;

    private BufferScope(ByteBuffer bb) {
        this.bb = bb;
    }

    @Override
    public Object base() {
        return getBufferBase(bb);
    }

    @Override
    public void close() {
        super.close();
        bb = null;
    }

    static Object getBufferBase(ByteBuffer bb) {
        return unsafe.getReference(bb, BYTE_BUFFER_BASE);
    }

    static long getBufferAddress(ByteBuffer bb) {
        return unsafe.getLong(bb, BUFFER_ADDRESS);
    }

    public static MemorySegment of(ByteBuffer bb) {
        long bbAddress = getBufferAddress(bb);

        int pos = bb.position();
        int limit = bb.limit();

        return new MemorySegmentImpl(bbAddress + pos, limit - pos, 0, new BufferScope(bb));
    }
}
