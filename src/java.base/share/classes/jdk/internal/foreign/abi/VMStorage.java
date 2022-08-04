/*
 * Copyright (c) 2020, 2022, Oracle and/or its affiliates. All rights reserved.
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
package jdk.internal.foreign.abi;

import java.util.Objects;

public class VMStorage {
    private static final byte STACK_TYPE = Architecture.current().stackType();

    private final byte type;
    private final short segmentMaskOrSize;
    private final int index;

    private final String debugName;

    private VMStorage(byte type, short segmentMaskOrSize, int index, String debugName) {
        this.type = type;
        this.segmentMaskOrSize = segmentMaskOrSize;
        this.index = index;
        this.debugName = debugName;
    }

    public static VMStorage stackStorage(short size, int byteOffset) {
        return new VMStorage(STACK_TYPE, size, byteOffset, "Stack@" + byteOffset);
    }

    public static VMStorage regStorage(byte type, short segmentMask, int index, String debugName) {
        if (type == STACK_TYPE)
            throw new IllegalArgumentException("Should not be stack type: " + type);
        return new VMStorage(type, segmentMask, index, debugName);
    }

    public byte type() {
        return type;
    }

    public short segmentMask() {
        if (!isReg())
            throw new IllegalArgumentException("Should be reg type: " + type);
        return segmentMaskOrSize;
    }

    public short size() {
        if (!isStack())
            throw new IllegalArgumentException("Should be stack type: " + type);
        return segmentMaskOrSize;
    }

    public int index() {
        return index;
    }

    public String name() {
        return debugName;
    }

    public boolean isStack() {
        return type == STACK_TYPE;
    }

    public boolean isReg() {
        return !isStack();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return (o instanceof VMStorage vmStorage)
            && type == vmStorage.type
            && segmentMaskOrSize == vmStorage.segmentMaskOrSize
            && index == vmStorage.index
            && Objects.equals(debugName, vmStorage.debugName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, segmentMaskOrSize, index, debugName);
    }

    @Override
    public String toString() {
        return "VMStorage{" +
                "type=" + type +
                ", segmentMaskOrSize=" + segmentMaskOrSize +
                ", index=" + index +
                ", debugName='" + debugName + '\'' +
                '}';
    }
}
