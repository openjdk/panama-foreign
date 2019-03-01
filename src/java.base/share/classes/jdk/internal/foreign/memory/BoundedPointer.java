/*
 * Copyright (c) 2016, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.foreign.NativeTypes;
import java.foreign.Scope;
import java.foreign.layout.Layout;
import java.foreign.layout.Unresolved;
import java.foreign.layout.Value;
import java.foreign.memory.Array;
import java.foreign.memory.LayoutType;
import java.foreign.memory.Pointer;
import java.nio.ByteBuffer;
import java.security.AccessControlException;
import java.util.Objects;
import jdk.internal.access.SharedSecrets;
import jdk.internal.foreign.ScopeImpl;
import jdk.internal.foreign.Util;
import jdk.internal.misc.Unsafe;

public class BoundedPointer<X> implements Pointer<X> {

    private static final Unsafe UNSAFE = Unsafe.getUnsafe();

    private static final BoundedPointer<?> theNullPointer = new BoundedPointer<>(
        LayoutTypeImpl.nullType, null, AccessMode.READ, MemoryBoundInfo.NOTHING);

    public static boolean isNull(long addr) {
        // FIMXE: Include the 64k?
        return addr == 0;
    }

    @SuppressWarnings("unchecked")
    public static <Z> BoundedPointer<Z> ofNull() {
        return (BoundedPointer<Z>) theNullPointer;
    }

    private final MemoryBoundInfo boundInfo;
    private final long offset;
    private final LayoutType<X> type;
    private final Scope scope;
    private final AccessMode mode;

    public BoundedPointer(LayoutType<X> type, Scope scope, AccessMode accessMode, MemoryBoundInfo boundInfo) {
        this(type, scope, accessMode, boundInfo, 0);
    }

    public BoundedPointer(LayoutType<X> type, Scope scope, AccessMode accessMode, MemoryBoundInfo boundInfo, long offset) {
        this.boundInfo = Objects.requireNonNull(boundInfo);
        this.offset = offset;
        this.type = Objects.requireNonNull(type);
        this.scope = scope;
        this.mode = accessMode;
    }

    @Override
    public LayoutType<X> type() {
        return type;
    }

    private long effectiveAddress() {
        if (boundInfo.base != null) {
            throw new UnsupportedOperationException();
        } else {
            return toUnsafeOffset();
        }
    }

    @Override
    public boolean isAccessibleFor(AccessMode mode) {
        return this.mode.isAvailable(mode);
    }

    @Override
    public Pointer<X> asReadOnly() throws AccessControlException {
        checkAccessibleFor(AccessMode.READ);
        return new BoundedPointer<>(type, scope, AccessMode.READ, boundInfo, offset);
    }

    @Override
    public Pointer<X> asWriteOnly() throws AccessControlException {
        checkAccessibleFor(AccessMode.WRITE);
        return new BoundedPointer<>(type, scope, AccessMode.WRITE, boundInfo, offset);
    }

    @Override
    public long addr() throws UnsupportedOperationException, IllegalAccessException {
        checkAlive();
        checkInBounds();
        return effectiveAddress();
    }

    @Override
    public Array<X> withSize(long size) {
        return new BoundedArray<>(this, size);
    }

    public BoundedPointer<X> limit(long nelems) {
        return new BoundedPointer<>(type, scope, mode, boundInfo.limit(offset + ((type.bytesSize()) * nelems)), offset);
    }

    @Override
    public boolean isNull() {
        return isNull(effectiveAddress());
    }

    @Override
    public BoundedPointer<X> offset(long nElements) throws IllegalArgumentException, IndexOutOfBoundsException {
        long elemSize = type.bytesSize();
        if (elemSize == 0) {
            throw new IllegalArgumentException();
        }

        long newOffset = this.offset + nElements * elemSize;

        // Note: the pointer may point outside of the memory region bounds.
        // This is allowed, as long as the pointer/data is not dereferenced
        return new BoundedPointer<>(type, scope, mode, boundInfo, newOffset);
    }

    @Override
    public ByteBuffer asDirectByteBuffer(int bytes) throws IllegalAccessException {
        boundInfo.checkRange(offset, bytes);
        return SharedSecrets.getJavaNioAccess()
                .newDirectByteBuffer(addr(), bytes, null);
    }

    public void copyTo(BoundedPointer<?> dst, long bytes) {
        if(bytes == 0) return; // nothing to do
        Objects.requireNonNull(dst);

        checkRead(bytes);
        dst.checkWrite(bytes);

        unsafeCopyTo(dst, bytes);
    }

    public long getBits() {
        checkRead(type.bytesSize());
        return unsafeGetBits();
    }

    public void putBits(long value) {
        checkWrite(type.bytesSize());
        unsafePutBits(value);
    }

    public String dump(int nbytes) {
        StringBuilder buf = new StringBuilder();
        Pointer<Byte> base = Util.unsafeCast(this, NativeTypes.UINT8);
        base.iterate(base.offset(nbytes)).forEach(sp -> {
            byte b = sp.get();
            String hex = Long.toHexString(b & 0xFF);
            buf.append(hex.length() == 1 ? "0" + hex : hex);
            buf.append(" ");
        });
        return buf.toString();
    }

    public <Z> BoundedPointer<Z> cast(LayoutType<Z> layoutType) {
        if (isCompatible(type.layout(), layoutType.layout())) {
            return new BoundedPointer<>(layoutType, scope, mode, boundInfo, offset);
        } else {
            throw new ClassCastException("Pointer to " + type.layout() +
                " cannot be cast to pointer to " + layoutType.layout());
        }
    }
    // where
    private static boolean isCompatible(Layout src, Layout dest) {
        /*
         * 1. Any pointer can be converted to void* and vice-versa.
         * 2. Any pointer can be converted to a pointer with same layout
         */
        return src.equals(NativeTypes.VOID.layout()) ||
            dest.equals(NativeTypes.VOID.layout()) ||
            src.equals(dest);
    }

    public void checkAlive() {
        if (scope != null) {
            ((ScopeImpl)scope).checkAlive();
        }
    }

    private void checkAccessibleFor(AccessMode mode) {
        if (!isAccessibleFor(mode)) {
            throw new AccessControlException("Access denied");
        }
    }

    private void checkInBounds() {
        if (! (type.layout() instanceof Unresolved)) {
            boundInfo.checkRange(offset, type.bytesSize());
        }
    }

    private void checkRead(long length) {
        checkAccess(AccessMode.READ, length);
    }

    private void checkWrite(long length) {
        checkAccess(AccessMode.WRITE, length);
    }

    private void checkAccess(AccessMode mode, long length) {
        checkAlive();
        checkAccessibleFor(mode);
        boundInfo.checkRange(offset, length);
    }

    public static BoundedPointer<?> createNativeVoidPointer(long offset) {
        return createNativeVoidPointer(ScopeImpl.UNCHECKED, offset);
    }

    public static BoundedPointer<?> createNativeVoidPointer(Scope scope, long offset) {
        return new BoundedPointer<>(NativeTypes.VOID, scope, AccessMode.READ_WRITE, MemoryBoundInfo.EVERYTHING, offset);
    }

    public static <Z> BoundedPointer<Z> createRegisterPointer(LayoutType<Z> type, long offset, boolean isReturn) {
        return new BoundedPointer<>(type, ScopeImpl.UNCHECKED,
                isReturn ? AccessMode.WRITE : AccessMode.READ,
                MemoryBoundInfo.EVERYTHING, offset);
    }

    public static <Z> BoundedPointer<Z> fromLongArray(LayoutType<Z> type, long[] values) {
        return new BoundedPointer<>(type, ScopeImpl.UNCHECKED, AccessMode.READ_WRITE,
                        MemoryBoundInfo.ofHeap(values, Util.LONG_ARRAY_BASE, values.length * Util.LONG_ARRAY_SCALE));
    }

    public Scope scope() {
        return scope;
    }

    @Override
    public String toString() {
        return "{ BoundedPointer type: " + type + " region: " + boundInfo + " offset=0x" + Long.toHexString(offset) + " }";
    }

    @Override
    public int hashCode() {
        return Long.valueOf(effectiveAddress()).hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof BoundedPointer) {
            return ((BoundedPointer) o).effectiveAddress() == effectiveAddress();
        } else if (o instanceof Pointer) {
            try {
                return ((Pointer) o).addr() == addr();
            } catch (IllegalAccessException iae) {
                throw new IllegalStateException();
            }
        } else {
            return false;
        }
    }



    //memory access helper functions

    private Object toUnsafeBase() {
        return boundInfo.base;
    }

    private long toUnsafeOffset() {
        return boundInfo.min + offset;
    }

    private void unsafeCopyTo(BoundedPointer<?> to, long size) {
        UNSAFE.copyMemory(toUnsafeBase(), toUnsafeOffset(),
                to.toUnsafeBase(), to.toUnsafeOffset(), size);
    }

    private long unsafeGetBits() {
        boolean isSigned = false;
        boolean reverseByteOrder = false;
        if (type().layout() instanceof Value) {
            Value v = (Value) type().layout();
            isSigned = v.kind() != Value.Kind.INTEGRAL_UNSIGNED;
            reverseByteOrder = !v.isNativeByteOrder();
        }
        long size = type().bytesSize();
        long bits;
        switch ((int)size) {
            case 1:
                bits = UNSAFE.getByte(toUnsafeBase(), toUnsafeOffset());
                return isSigned ? bits : Byte.toUnsignedLong((byte)bits);

            case 2:
                short s = UNSAFE.getShort(toUnsafeBase(), toUnsafeOffset());
                bits = reverseByteOrder ? Short.reverseBytes(s) : s;
                return isSigned ? bits : Short.toUnsignedLong((short)bits);

            case 4:
                int i = UNSAFE.getInt(toUnsafeBase(), toUnsafeOffset());
                bits = reverseByteOrder ? Integer.reverseBytes(i) : i;
                return isSigned ? bits : Integer.toUnsignedLong((int)bits);

            case 8:
                bits = UNSAFE.getLong(toUnsafeBase(), toUnsafeOffset());
                return reverseByteOrder ? Long.reverseBytes(bits) : bits;

            default:
                throw new IllegalArgumentException("Invalid size: " + size);
        }
    }

    private void unsafePutBits(long value) {
        long size = type().bytesSize();
        boolean reverseByteOrder = type.layout() instanceof Value &&
                      ! ((Value) (type.layout())).isNativeByteOrder();
        switch ((int)size) {
            case 1:
                UNSAFE.putByte(toUnsafeBase(), toUnsafeOffset(), (byte)value);
                break;

            case 2:
                short s = (short) value;
                UNSAFE.putShort(toUnsafeBase(), toUnsafeOffset(),
                        reverseByteOrder ? Short.reverseBytes(s) : s);
                break;

            case 4:
                int i = (int) value;
                UNSAFE.putInt(toUnsafeBase(), toUnsafeOffset(),
                        reverseByteOrder ? Integer.reverseBytes(i) : i);
                break;

            case 8:
                UNSAFE.putLong(toUnsafeBase(), toUnsafeOffset(),
                        reverseByteOrder ? Long.reverseBytes(value) : value);
                break;

            default:
                throw new IllegalArgumentException("Invalid size: " + size);
        }
    }
}
