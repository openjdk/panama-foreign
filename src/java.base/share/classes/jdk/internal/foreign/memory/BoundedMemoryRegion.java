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

import java.foreign.memory.Pointer.AccessMode;
import java.foreign.Scope;
import java.util.Objects;
import java.security.AccessControlException;
import java.nio.ByteBuffer;
import jdk.internal.foreign.Util;

public class BoundedMemoryRegion {
    
    public static final BoundedMemoryRegion EVERYTHING = BoundedMemoryRegion.ofEverything(AccessMode.READ_WRITE, null);
    public static final BoundedMemoryRegion NOTHING = BoundedMemoryRegion.of(0, 0);

    private static final Unsafe U = Unsafe.getUnsafe();

    private final Scope scope;
    private final Object base;
    private final long min;
    private final long length;

    private final AccessMode mode;

    private BoundedMemoryRegion(Object base, long min, long length, AccessMode mode, Scope scope) {
        this.base = base;
        this.min = min;
        this.length = length;
        this.mode = mode;
        this.scope = scope;
    }

    // # Length = everything
    public static BoundedMemoryRegion ofEverything(Scope scope) {
        return ofEverything(AccessMode.READ_WRITE, scope);
    }

    public static BoundedMemoryRegion ofEverything(AccessMode mode, Scope scope) {
        return new Everything(mode, scope);
    }

    // # Length unknown:
    public static BoundedMemoryRegion of(long min) {
        return BoundedMemoryRegion.of(min, AccessMode.READ_WRITE, null);
    }

    public static BoundedMemoryRegion of(long min, AccessMode mode, Scope scope) {
        // min + length may not overflow to positive, and length must be positive
        long maxLength = min < 0 && min != Long.MIN_VALUE ? -min : Long.MAX_VALUE; 
        return BoundedMemoryRegion.ofInternal(null, min, maxLength, mode, scope);
    }

    // # Length known:
    public static BoundedMemoryRegion of(long min, long length) {
        return BoundedMemoryRegion.of(min, length, null);
    }

    public static BoundedMemoryRegion of(long min, long length, Scope scope) {
        return BoundedMemoryRegion.of(null, min, length, AccessMode.READ_WRITE, scope);
    }

    public static BoundedMemoryRegion of(Object base, long min, long length) {
        return BoundedMemoryRegion.of(base, min, length, AccessMode.READ_WRITE, null);
    }

    public static BoundedMemoryRegion ofByteBuffer(ByteBuffer bb) {
        // For a direct ByteBuffer base == null and address is absolute
        Object base = Util.getBufferBase(bb);
        long address = Util.getBufferAddress(bb);
        int pos = bb.position();
        int limit = bb.limit();
        return new BoundedMemoryRegion(base, address + pos, limit - pos, bb.isReadOnly() ? AccessMode.READ : AccessMode.READ_WRITE, null) {
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
            BoundedMemoryRegion limit(long newLength) {
                throw new UnsupportedOperationException(); // bb ref would be lost otherwise
            }
        };
    }    

    public static BoundedMemoryRegion of(Object base, long min, long length, AccessMode mode, Scope scope) {
        checkOverflow(min, length);
        return ofInternal(base, min, length, mode, scope);
    }

    private static BoundedMemoryRegion ofInternal(Object base, long min, long length, AccessMode mode, Scope scope) {
        if(length < 0) {
            throw new IllegalArgumentException("length must be positive");
        }
        if(base != null && min < 0) {
            throw new IllegalArgumentException("min must be positive if base is used");
        }
        return new BoundedMemoryRegion(base, min, length, mode, scope);
    }

    private static void checkOverflow(long min, long length) {
        // we never access at `length`
        Util.addUnsignedExact(min, length == 0 ? 0 : length - 1);
    }

    public boolean isAccessibleFor(AccessMode mode) {
        return this.mode.isAvailable(mode);
    }

    BoundedMemoryRegion withAccess(AccessMode mode) {
        return BoundedMemoryRegion.ofInternal(base, min, length, mode, scope);
    }

    BoundedMemoryRegion asReadOnly() throws AccessControlException {
        if(!isAccessibleFor(AccessMode.READ))
            throw new AccessControlException("This memory region is not read-accessible");
        return withAccess(AccessMode.READ);
    }

    BoundedMemoryRegion asWriteOnly() throws AccessControlException {
        if(!isAccessibleFor(AccessMode.WRITE))
            throw new AccessControlException("This memory region is not write-accessible");
        return withAccess(AccessMode.WRITE);
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

    public void checkAccess(AccessMode mode) {
        if (!isAccessibleFor(mode)) {
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

    private void checkRead(long offset, long length) {
        checkAlive();
        checkAccess(AccessMode.READ);
        checkRange(offset, length);
    }

    private void checkWrite(long offset, long length) {
        checkAlive();
        checkAccess(AccessMode.WRITE);
        checkRange(offset, length);
    }

    BoundedMemoryRegion limit(long newLength) {
        if (newLength > length || newLength < 0) {
            throw new IllegalArgumentException();
        }
        return new BoundedMemoryRegion(base, min, newLength, mode, scope);
    }

    public void copyTo(long srcOffset, BoundedMemoryRegion dst, long dstOffset, long length) {
        if(length == 0) return; // nothing to do
        Objects.requireNonNull(dst);

        checkRead(srcOffset, length);
        dst.checkWrite(dstOffset, length);

        U.copyMemory(this.base, this.min + srcOffset, dst.base, dst.min + dstOffset, length);
    }

    public long getBits(long offset, long size, boolean isSigned) {
        checkRead(offset, size);      

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
        checkWrite(offset, size);       

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
        checkRead(offset, U.addressSize());      
        return U.getAddress(this.min + offset);
    }

    public void putAddress(long offset, long value) {
        if (base != null) {
            throw new UnsupportedOperationException();
        }
        checkWrite(offset, U.addressSize());           
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

    private static class Everything extends BoundedMemoryRegion {

        public Everything(AccessMode mode, Scope scope) {
            super(null, 0, Long.MAX_VALUE, mode, scope);
        }

        @Override
        BoundedMemoryRegion withAccess(AccessMode mode) {
            return BoundedMemoryRegion.ofEverything(mode, super.scope);
        }

        @Override
        public void checkBounds(long offset) {} // any offset is in bounds
    
        @Override
        void checkRange(long offset, long length) {
            checkOverflow(offset, length);
        }

    }
}
