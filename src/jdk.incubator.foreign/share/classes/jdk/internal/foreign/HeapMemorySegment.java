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

import jdk.incubator.foreign.MemorySegment;
import jdk.internal.access.JavaNioAccess;
import jdk.internal.access.SharedSecrets;
import jdk.internal.misc.Unsafe;
import jdk.internal.vm.annotation.ForceInline;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * This class provides an immutable implementation for the {@code MemorySegment} interface. This class contains information
 * about the segment's spatial and temporal bounds, as well as the addressing coordinates (base + offset) which allows
 * unsafe access; each memory segment implementation is associated with an owner thread which is set at creation time.
 * Access to certain sensitive operations on the memory segment will fail with {@code IllegalStateException} if the
 * segment is either in an invalid state (e.g. it has already been closed) or if access occurs from a thread other
 * than the owner thread. See {@link MemoryScope} for more details on management of temporal bounds.
 */
public abstract class HeapMemorySegment<H> extends AbstractMemorySegment {

    private static final Unsafe UNSAFE = Unsafe.getUnsafe();
    private static final int BYTE_ARR_BASE = UNSAFE.arrayBaseOffset(byte[].class);

    final long offset;
    final H base;

    @ForceInline
    HeapMemorySegment(long offset, H base, long length, int mask, Thread owner, MemoryScope scope) {
        super(length, mask, owner, scope);
        this.offset = offset;
        this.base = base;
    }

    @Override
    ByteBuffer makeByteBuffer() {
        if (!(base() instanceof byte[])) {
            throw new UnsupportedOperationException("Not an address to an heap-allocated byte array");
        }
        JavaNioAccess nioAccess = SharedSecrets.getJavaNioAccess();
        return nioAccess.newHeapByteBuffer((byte[]) base(), (int)min() - BYTE_ARR_BASE, (int) byteSize(), this);
    }

    @Override
    long min() {
        return offset;
    }

    public static class OfByte extends HeapMemorySegment<byte[]> {
        OfByte(long offset, byte[] base, long length, int mask, Thread owner, MemoryScope scope) {
            super(offset, base, length, mask, owner, scope);
        }

        @Override
        AbstractMemorySegment dup(long offset, long size, int mask, Thread owner, MemoryScope scope) {
            return new OfByte(this.offset + offset, base, size, mask, owner, scope);
        }

        @Override
        Object base() {
            return Objects.requireNonNull(base);
        }

        public static MemorySegment makeArraySegment(byte[] arr) {
            int base = Unsafe.ARRAY_BYTE_BASE_OFFSET;
            int scale = Unsafe.ARRAY_BYTE_INDEX_SCALE;
            MemoryScope scope = new MemoryScope(null, null);
            return new OfByte(base, arr, arr.length * scale,
                    DEFAULT_MASK | SMALL, Thread.currentThread(), scope);
        }
    }

    public static class OfChar extends HeapMemorySegment<char[]> {
        OfChar(long offset, char[] base, long length, int mask, Thread owner, MemoryScope scope) {
            super(offset, base, length, mask, owner, scope);
        }

        @Override
        AbstractMemorySegment dup(long offset, long size, int mask, Thread owner, MemoryScope scope) {
            return new OfChar(this.offset + offset, base, size, mask, owner, scope);
        }

        @Override
        Object base() {
            return Objects.requireNonNull(base);
        }

        public static MemorySegment makeArraySegment(char[] arr) {
            int base = Unsafe.ARRAY_CHAR_BASE_OFFSET;
            int scale = Unsafe.ARRAY_CHAR_INDEX_SCALE;
            MemoryScope scope = new MemoryScope(null, null);
            return new OfChar(base, arr, arr.length * scale,
                    DEFAULT_MASK | SMALL, Thread.currentThread(), scope);
        }
    }

    public static class OfShort extends HeapMemorySegment<short[]> {
        OfShort(long offset, short[] base, long length, int mask, Thread owner, MemoryScope scope) {
            super(offset, base, length, mask, owner, scope);
        }

        @Override
        AbstractMemorySegment dup(long offset, long size, int mask, Thread owner, MemoryScope scope) {
            return new OfShort(this.offset + offset, base, size, mask, owner, scope);
        }

        @Override
        Object base() {
            return Objects.requireNonNull(base);
        }

        public static MemorySegment makeArraySegment(short[] arr) {
            int base = Unsafe.ARRAY_SHORT_BASE_OFFSET;
            int scale = Unsafe.ARRAY_SHORT_INDEX_SCALE;
            MemoryScope scope = new MemoryScope(null, null);
            return new OfShort(base, arr, arr.length * scale,
                    DEFAULT_MASK | SMALL, Thread.currentThread(), scope);
        }
    }

    public static class OfInt extends HeapMemorySegment<int[]> {
        OfInt(long offset, int[] base, long length, int mask, Thread owner, MemoryScope scope) {
            super(offset, base, length, mask, owner, scope);
        }

        @Override
        AbstractMemorySegment dup(long offset, long size, int mask, Thread owner, MemoryScope scope) {
            return new OfInt(this.offset + offset, base, size, mask, owner, scope);
        }

        @Override
        Object base() {
            return Objects.requireNonNull(base);
        }

        public static MemorySegment makeArraySegment(int[] arr) {
            int base = Unsafe.ARRAY_INT_BASE_OFFSET;
            int scale = Unsafe.ARRAY_INT_INDEX_SCALE;
            MemoryScope scope = new MemoryScope(null, null);
            return new OfInt(base, arr, arr.length * scale,
                    DEFAULT_MASK | SMALL, Thread.currentThread(), scope);
        }
    }

    public static class OfLong extends HeapMemorySegment<long[]> {
        OfLong(long offset, long[] base, long length, int mask, Thread owner, MemoryScope scope) {
            super(offset, base, length, mask, owner, scope);
        }

        @Override
        AbstractMemorySegment dup(long offset, long size, int mask, Thread owner, MemoryScope scope) {
            return new OfLong(this.offset + offset, base, size, mask, owner, scope);
        }

        @Override
        Object base() {
            return Objects.requireNonNull(base);
        }

        public static MemorySegment makeArraySegment(long[] arr) {
            int base = Unsafe.ARRAY_LONG_BASE_OFFSET;
            int scale = Unsafe.ARRAY_LONG_INDEX_SCALE;
            MemoryScope scope = new MemoryScope(null, null);
            return new OfLong(base, arr, arr.length * scale,
                    DEFAULT_MASK | SMALL, Thread.currentThread(), scope);
        }
    }

    public static class OfFloat extends HeapMemorySegment<float[]> {
        OfFloat(long offset, float[] base, long length, int mask, Thread owner, MemoryScope scope) {
            super(offset, base, length, mask, owner, scope);
        }

        @Override
        AbstractMemorySegment dup(long offset, long size, int mask, Thread owner, MemoryScope scope) {
            return new OfFloat(this.offset + offset, base, size, mask, owner, scope);
        }

        @Override
        Object base() {
            return Objects.requireNonNull(base);
        }

        public static MemorySegment makeArraySegment(float[] arr) {
            int base = Unsafe.ARRAY_FLOAT_BASE_OFFSET;
            int scale = Unsafe.ARRAY_FLOAT_INDEX_SCALE;
            MemoryScope scope = new MemoryScope(null, null);
            return new OfFloat(base, arr, arr.length * scale,
                    DEFAULT_MASK | SMALL, Thread.currentThread(), scope);
        }
    }

    public static class OfDouble extends HeapMemorySegment<double[]> {
        OfDouble(long offset, double[] base, long length, int mask, Thread owner, MemoryScope scope) {
            super(offset, base, length, mask, owner, scope);
        }

        @Override
        AbstractMemorySegment dup(long offset, long size, int mask, Thread owner, MemoryScope scope) {
            return new OfDouble(this.offset + offset, base, size, mask, owner, scope);
        }

        @Override
        Object base() {
            return Objects.requireNonNull(base);
        }

        public static MemorySegment makeArraySegment(double[] arr) {
            int base = Unsafe.ARRAY_DOUBLE_BASE_OFFSET;
            int scale = Unsafe.ARRAY_DOUBLE_INDEX_SCALE;
            MemoryScope scope = new MemoryScope(null, null);
            return new OfDouble(base, arr, arr.length * scale,
                    DEFAULT_MASK | SMALL, Thread.currentThread(), scope);
        }
    }
}
