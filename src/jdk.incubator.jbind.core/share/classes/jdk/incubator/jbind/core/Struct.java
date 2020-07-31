/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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

package jdk.incubator.jbind.core;

import java.lang.invoke.VarHandle;
import java.util.Arrays;
import jdk.incubator.foreign.Addressable;
import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryHandles;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemoryLayout.PathElement;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.SequenceLayout;

public abstract class Struct<T extends Struct<T>> implements Addressable {
    private final MemorySegment ms;

    protected Struct(MemorySegment segment) {
        this.ms = segment;
    }

    public abstract GroupLayout getLayout();

    @Override
    public MemoryAddress address() {
        return ms.address();
    };

    protected final MemorySegment getFieldAddr(String name) {
        return ms.asSlice(getLayout().byteOffset(MemoryLayout.PathElement.groupElement(name)));
    }

    /**
     * Return the VarHandle for the field. The returned handle features no coordinate
     * unless the field is of SequenceLayout, then one long coordinate for each dimension
     * is added.
     */
    protected final VarHandle getFieldHandle(String fieldName, Class<?> carrier) {
        MemoryLayout.PathElement field = MemoryLayout.PathElement.groupElement(fieldName);
        MemoryLayout fieldLayout = getLayout().select(field);
        long offset = getLayout().byteOffset(field);
        return MemoryHandles.insertCoordinates(RuntimeHelper.varHandle(carrier, fieldLayout),
            0, ms.asSlice(offset));
    }

    public MemorySegment segment() {
        return ms;
    }

    public MemorySegment asSegment() {
        return ms.asSlice(0, getLayout().byteSize());
    }
}
