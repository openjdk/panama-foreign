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

import jdk.internal.access.JavaNioAccess;
import jdk.internal.access.SharedSecrets;

import jdk.incubator.foreign.MemorySegment;

import java.nio.ByteBuffer;

public class BufferScope extends MemorySegmentImpl.Scope {

    private static final JavaNioAccess javaNioAccess = SharedSecrets.getJavaNioAccess();

    // Keep a reference to the buffer so it is kept alive while the segment is alive
    private ByteBuffer bb;

    private BufferScope(ByteBuffer bb) {
        this.bb = bb;
    }

    @Override
    public Object base() {
        return javaNioAccess.getBufferBase(bb);
    }

    @Override
    public void close() {
        super.close();
        bb = null;
    }

    public static MemorySegment of(ByteBuffer bb) {
        long bbAddress = javaNioAccess.getBufferAddress(bb);

        int pos = bb.position();
        int limit = bb.limit();

        BufferScope bufferScope = new BufferScope(bb);
        return new MemorySegmentImpl(bbAddress + pos, limit - pos, 0, bufferScope);
    }
}
