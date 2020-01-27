/*
 * Copyright (c) 2019, 2020, Oracle and/or its affiliates. All rights reserved.
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
package jdk.internal.foreign.abi;

import jdk.incubator.foreign.MemoryLayout;

import java.util.Objects;

public abstract class Binding {
    static final int MOVE_TAG = 0;
    static final int DEREFERENCE_TAG = 1;
    static final int COPY_BUFFER_TAG = 2;
    static final int ALLOC_BUFFER_TAG = 3;
    static final int BOX_ADDRESS_TAG = 4;
    static final int BASE_ADDRESS_TAG = 5;
    static final int DUP_TAG = 6;

    private final int tag;

    private Binding(int tag) {
        this.tag = tag;
    }

    public int tag() {
        return tag;
    }

    /**
     * Moves from a primitve to a VMStorage
     */
    public static class Move extends Binding {
        private final VMStorage storage;
        private final Class<?> type;

        public Move(VMStorage storage, Class<?> type) {
            super(MOVE_TAG);
            this.storage = storage;
            this.type = type;
        }

        public VMStorage storage() {
            return storage;
        }

        public Class<?> type() {
            return type;
        }

        @Override
        public String toString() {
            return "Move{" +
                    "tag=" + tag() +
                    ", storage=" + storage +
                    ", type=" + type +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Move move = (Move) o;
            return storage.equals(move.storage) &&
                    type.equals(move.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tag(), storage, type);
        }
    }

    /**
     * Loads or stores a Java primitive to a MemorySegment at a certain offset
     */
    public static class Dereference extends Binding {
        private final long offset;
        private final Class<?> type;

        public Dereference(long offset, Class<?> type) {
            super(DEREFERENCE_TAG);
            this.offset = offset;
            this.type = type;
        }

        public long offset() {
            return offset;
        }

        public Class<?> type() {
            return type;
        }

        @Override
        public String toString() {
            return "Dereference{" +
                    "tag=" + tag() +
                    ", offset=" + offset +
                    ", type=" + type +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Dereference that = (Dereference) o;
            return offset == that.offset &&
                    type.equals(that.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tag(), offset, type);
        }
    }

    /**
     * Copies from a MemoryAddress into a newly allocated MemorySegment
     */
    public static class Copy extends Binding {
        private final long size;
        private final long alignment;

        public Copy(long size, long alignment) {
            super(COPY_BUFFER_TAG);
            this.size = size;
            this.alignment = alignment;
        }

        public long size() {
            return size;
        }

        public long alignment() {
            return alignment;
        }

        @Override
        public String toString() {
            return "Copy{" +
                    "tag=" + tag() +
                    ", size=" + size +
                    ", alignment=" + alignment +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Copy copy = (Copy) o;
            return size == copy.size &&
                    alignment == copy.alignment;
        }

        @Override
        public int hashCode() {
            return Objects.hash(tag(), size, alignment);
        }
    }

    /**
     * Allocates a MemorySegment
     */
    public static class AllocateBuffer extends Binding {
        private final long size;
        private final long alignment;

        public AllocateBuffer(MemoryLayout layout) {
            super(ALLOC_BUFFER_TAG);
            this.size = layout.byteSize();
            this.alignment = layout.byteAlignment();
        }

        public long size() {
            return size;
        }

        public long alignment() {
            return alignment;
        }

        @Override
        public String toString() {
            return "AllocateBuffer{" +
                    "tag=" + tag() +
                    "size=" + size +
                    ", alignment=" + alignment +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AllocateBuffer that = (AllocateBuffer) o;
            return size == that.size &&
                    alignment == that.alignment;
        }

        @Override
        public int hashCode() {
            return Objects.hash(tag(), size, alignment);
        }
    }

    /**
     * Boxes or unboxes a MemoryAddress to a long and vice versa (depending on box/unbox interpreter)
     */
    public static class BoxAddress extends Binding {
        public BoxAddress() {
            super(BOX_ADDRESS_TAG);
        }

        @Override
        public String toString() {
            return "BoxAddress{" +
                    "tag=" + tag() +
                    "}";
        }

        @Override
        public int hashCode() {
            return tag();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            return o != null && getClass() == o.getClass();
        }
    }

    /**
     * Takes the base address of a MemorySegment
     */
    public static class BaseAddress extends Binding {
        public BaseAddress() {
            super(BASE_ADDRESS_TAG);
        }

        @Override
        public String toString() {
            return "BaseAddress{" +
                    "tag=" + tag() +
                    "}";
        }

        @Override
        public int hashCode() {
            return tag();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            return o != null && getClass() == o.getClass();
        }
    }

    /**
     * Duplicates a value on top of the interpreter stack
     */
    public static class Dup extends Binding {
        public Dup() {
            super(DUP_TAG);
        }

        @Override
        public String toString() {
            return "Dup{" +
                    "tag=" + tag() +
                    "}";
        }

        @Override
        public int hashCode() {
            return tag();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            return o != null && getClass() == o.getClass();
        }
    }
}
