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
package jdk.internal.foreign;

import jdk.internal.access.SharedSecrets;
import jdk.internal.misc.Unsafe;
import jdk.internal.vm.annotation.ForceInline;

import java.foreign.MemoryAddress;
import java.foreign.MemoryScope;
import java.nio.ByteBuffer;
import java.util.Objects;

public class MemoryAddressImpl implements MemoryAddress {

    static Unsafe UNSAFE;

    public static MemoryAddress NULL_ADDR = new MemoryAddressImpl(MemoryScopeImpl.UNCHECKED, MemoryBoundInfo.NOTHING, 0);

    static {
        if (MemoryAddressImpl.class.getClassLoader() != null) {
            throw new IllegalStateException();
        }
        UNSAFE = Unsafe.getUnsafe();
    }

    private final MemoryBoundInfo boundInfo;
    private final long offset;
    private final MemoryScope scope;

    public MemoryAddressImpl(MemoryScope scope, MemoryBoundInfo boundInfo) {
        this(scope, boundInfo, 0);
    }

    public MemoryAddressImpl(MemoryScope scope, MemoryBoundInfo boundInfo, long offset) {
        this.boundInfo = Objects.requireNonNull(boundInfo);
        this.offset = offset;
        this.scope = scope;
    }

    public static MemoryAddress ofNull() {
        return NULL_ADDR;
    }

    public static MemoryAddress ofNative(long addr) {
        return ofNative(MemoryScopeImpl.UNCHECKED, addr);
    }

    public static MemoryAddress ofNative(MemoryScope scope, long addr) {
        return new MemoryAddressImpl(scope, MemoryBoundInfo.EVERYTHING, addr);
    }

    public static MemoryAddress ofArray(Object array) {
        int size = java.lang.reflect.Array.getLength(array);
        long base = UNSAFE.arrayBaseOffset(array.getClass());
        long scale = UNSAFE.arrayIndexScale(array.getClass());
        return new MemoryAddressImpl(MemoryScopeImpl.UNCHECKED, MemoryBoundInfo.ofHeap(array, base, size * scale));
    }

    public static void copy(MemoryAddressImpl src, MemoryAddressImpl dst, long size) {
        ((MemoryScopeImpl)src.scope()).checkAlive();
        ((MemoryScopeImpl)dst.scope()).checkAlive();
        src.checkAccess(0, size);
        dst.checkAccess(0, size);
        UNSAFE.copyMemory(
                src.unsafeGetBase(), src.unsafeGetOffset(),
                dst.unsafeGetBase(), dst.unsafeGetOffset(),
                size);
    }

    public long size() {
        return boundInfo.length;
    }

    public long offset() {
        return offset;
    }

    @Override
    public MemoryScope scope() {
        return scope;
    }

    @Override
    @ForceInline
    public MemoryAddress narrow(long newSize) {
        return new MemoryAddressImpl(scope, boundInfo.limit(offset, offset + newSize), 0);
    }

    @Override
    public MemoryAddress offset(long bytes) {
        return new MemoryAddressImpl(scope, boundInfo, offset + bytes);
    }

    public void checkAccess(long offset, long length) {
        if (scope != null) {
            ((MemoryScopeImpl)scope).checkAlive();
        }
        boundInfo.checkRange(this.offset + offset, length);
    }

    public long unsafeGetOffset() {
        return boundInfo.min + offset;
    }

    public Object unsafeGetBase() {
        return boundInfo.base;
    }

    @Override
    public ByteBuffer asDirectByteBuffer(int bytes) throws IllegalAccessException {
        boundInfo.checkRange(offset, bytes);
        return SharedSecrets.getJavaNioAccess()
                .newDirectByteBuffer(unsafeGetOffset(), bytes, null);
    }

    public static long addressof(MemoryAddress address) {
        if (address.equals(NULL_ADDR)) {
            return 0L;
        } else {
            MemoryAddressImpl addressImpl = (MemoryAddressImpl)address;
            addressImpl.checkAccess(0L, 1);
            if (addressImpl.unsafeGetBase() != null) {
                throw new IllegalStateException("Heap address!");
            }
            return addressImpl.unsafeGetOffset();
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(unsafeGetBase(), unsafeGetOffset());
    }

    @Override
    public boolean equals(Object that) {
        if (that instanceof MemoryAddressImpl) {
            MemoryAddressImpl addr = (MemoryAddressImpl)that;
            return Objects.equals(unsafeGetBase(), ((MemoryAddressImpl) that).unsafeGetBase()) &&
                    unsafeGetOffset() == addr.unsafeGetOffset();
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "MemoryAddress{ region: " + boundInfo + " offset=0x" + Long.toHexString(offset) + " }";
    }
}
