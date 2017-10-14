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
package java.nicl.types;

public interface MemoryRegion {
    int MODE_R  = (1 << 0);
    int MODE_W  = (1 << 1);
    int MODE_RW = MODE_R | MODE_W;

    // Various checking methods which throw exceptions on errors
    void checkAccess(int mode);
    void checkAlive();
    void checkBounds(long offset) throws IndexOutOfBoundsException;

    long addr() throws UnsupportedOperationException;

    // Getters & setters
    long getBits(long offset, long size, boolean isSigned);
    void putBits(long offset, long size, long value);

    byte getByte(long offset);
    void putByte(long offset, byte value);
    short getShort(long offset);
    void putShort(long offset, short value);
    int getInt(long offset);
    void putInt(long offset, int value);
    long getLong(long offset);
    void putLong(long offset, long value);

    long getAddress(long offset);
    void putAddress(long offset, long value);
}
