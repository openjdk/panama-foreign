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

import jdk.internal.misc.Unsafe;

import java.foreign.Scope;
import java.util.Objects;
import java.security.AccessControlException;

public class BoundedMemoryRegion {

    public static final int MODE_R  = (1 << 0);
    public static final int MODE_W  = (1 << 1);
    public static final int MODE_RW = MODE_R | MODE_W;

    public static final BoundedMemoryRegion EVERYTHING = new BoundedMemoryRegion(0, Long.MAX_VALUE); // FIXME: Not actually MAX_VALUE
    public static final BoundedMemoryRegion NOTHING = new BoundedMemoryRegion(0, 0);

    private static final Unsafe U = Unsafe.getUnsafe();

    private final Scope scope;
    private final Object base;
    private final long min;
    final long length;

    private final int mode;

    public BoundedMemoryRegion(long min, long length) {
        this(min, length, MODE_RW);
    }

    public BoundedMemoryRegion(long min, long length, int mode) {
        this(min, length, mode, null);
    }

    public BoundedMemoryRegion(long min, long length, Scope scope) {
        this(null, min, length, MODE_RW, scope);
    }

    public BoundedMemoryRegion(long min, long length, int mode, Scope scope) {
        this(null, min, length, mode, scope);
    }

    public BoundedMemoryRegion(Object base, long min, long length) {
        this(base, min, length, MODE_RW, null);
    }

    public BoundedMemoryRegion(Object base, long min, long length, int mode) {
        this(base, min, length, mode, null);
    }

    public BoundedMemoryRegion(Object base, long min, long length, int mode, Scope scope) {
        if (min < 0 || length < 0) {
            throw new IllegalArgumentException();
        }
        this.base = base;
        this.min = min;
        this.length = length;
        this.mode = mode;
        this.scope = scope;
    }

    public boolean isAccessibleFor(int mode) {
        return (this.mode & mode) == mode;
    }

    public long addr() throws UnsupportedOperationException {
        if (base != null) {
            throw new UnsupportedOperationException();
        }

        return min;
    }

    public Scope scope() {
        return scope;
    }

    public void checkAccess(int mode) {
        if ((this.mode & mode) == 0) {
            throw new AccessControlException("Access denied");
        }
    }

    public void checkAlive() {
        if (scope != null) {
            scope.checkAlive();
        }
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

    BoundedMemoryRegion limit(long newLength) {
        if (newLength > length || newLength < 0) {
            throw new IllegalArgumentException();
        }
        return new BoundedMemoryRegion(base, min, newLength, mode, scope);
    }

    public void copyTo(long srcOffset, BoundedMemoryRegion dst, long dstOffset, long length) {
        checkRange(srcOffset, length);
        checkAccess(MODE_R);
        checkAlive();
        Objects.requireNonNull(dst);
        dst.checkRange(dstOffset, length);
        dst.checkAccess(MODE_W);
        dst.checkAlive();

        U.copyMemory(this.base, this.min + srcOffset, dst.base, dst.min + dstOffset, length);
    }

    public long getBits(long offset, long size, boolean isSigned) {
        checkAccess(MODE_R);
        checkRange(offset, size);
        checkAlive();

        if (size < 0 || size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Invalid size: " + size);
        }

        long bits;

        switch ((int)size) {
        case 1:
            bits = U.getByte(base, this.min + offset);
            return isSigned ? bits : Byte.toUnsignedLong((byte)bits);

        case 2:
            bits = U.getShort(base, this.min + offset);
            return isSigned ? bits : Short.toUnsignedLong((short)bits);

        case 4:
            bits = U.getInt(base, this.min + offset);
            return isSigned ? bits : Integer.toUnsignedLong((int)bits);

        case 8:
            return U.getLong(base, this.min + offset);

        default:
            throw new IllegalArgumentException("Invalid size: " + size);
        }
    }

    public long getBits(long offset, long size) {
        return getBits(offset, size, true);
    }

    public void putBits(long offset, long size, long value) {
        checkAccess(MODE_R);
        checkRange(offset, size);
        checkAlive();

        if (size < 0 || size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Invalid size: " + size);
        }

        switch ((int)size) {
        case 1:
            U.putByte(base, this.min + offset, (byte)value);
            break;

        case 2:
            U.putShort(base, this.min + offset, (short)value);
            break;

        case 4:
            U.putInt(base, this.min + offset, (int)value);
            break;

        case 8:
            U.putLong(base, this.min + offset, value);
            break;

        default:
            throw new IllegalArgumentException("Invalid size: " + size);
        }
    }

    public byte getByte(long offset) {
        return (byte)getBits(offset, 1);
    }

    public void putByte(long offset, byte value) {
        putBits(offset, 1, value);
    }

    public short getShort(long offset) {
        return (short)getBits(offset, 2);
    }

    public void putShort(long offset, short value) {
        putBits(offset, 2, value);
    }

    public int getInt(long offset) {
        return (int)getBits(offset, 4);
    }

    public void putInt(long offset, int value) {
        putBits(offset, 4, value);
    }

    public long getLong(long offset) {
        return getBits(offset, 8);
    }

    public void putLong(long offset, long value) {
        putBits(offset, 8, value);
    }

    public long getAddress(long offset) {
        if (base != null) {
            throw new UnsupportedOperationException();
        }
        checkRange(offset, U.addressSize());
        checkAlive();
        return U.getAddress(this.min + offset);
    }

    public void putAddress(long offset, long value) {
        if (base != null) {
            throw new UnsupportedOperationException();
        }
        checkRange(offset, U.addressSize());
        checkAlive();
        U.putAddress(this.min + offset, value);
    }

    @Override
    public String toString() {
        return "{ BoundedMemoryRegion base=" + base + ", min=0x" + Long.toHexString(min) + " length=0x" + Long.toHexString(length) + " mode=" + mode + " }";
    }

    public String dump(long start, int nbytes) {
        StringBuilder buf = new StringBuilder();
        for (long offset = 0 ; offset < nbytes ; offset++) {
            byte b = (byte)getBits(start + offset, 1);
            String hex = Long.toHexString(b & 0xFF);
            buf.append(hex.length() == 1 ? "0" + hex : hex);
            buf.append(" ");
        }
        return buf.toString();
    }
}
