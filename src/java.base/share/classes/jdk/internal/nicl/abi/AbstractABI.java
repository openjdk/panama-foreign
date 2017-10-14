/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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

package jdk.internal.nicl.abi;

import jdk.internal.nicl.types.*;

/**
 * Base class implements default rules for ABI. Mostly base on System V AMD64
 * ABI convention.
 * A ABI following similar convention will only need to override definedSize() and
 * arrangeCall() methods.
 */
public abstract class AbstractABI implements SystemABI {
    @Override
    public long definedAlignment(char typeCode) {
        // default to same as size, which happen to be the case for AMD64
        return definedSize(typeCode);
    }

    private long alignUp(long addr, long alignment) {
        return ((addr - 1) | (alignment - 1)) + 1;
    }

    private long alignDown(long addr, long alignment) {
        return addr & ~(alignment - 1);
    }

    protected long alignmentOfScalar(Scalar st) {
        switch (st.typeCode()) {
            case 'v':
                return st.getSize();
            case 'i':
                return st.getSize();
            default:
                return definedAlignment(st.typeCode());
        }
    }

    protected long alignmentOfArray(Array ar, boolean isVar) {
        if (ar.getOccurrence() < 0) {
            // VLA or incomplete
            return 16;
        } else if (ar.getSize() >= 16 && isVar) {
            return 16;
        } else {
            // align as element type
            Type elementType = ar.getElementType();
            return alignment(elementType, false);
        }
    }

    protected long alignmentOfContainer(Container ct) {
        // Most strict member
        return ct.getMembers().mapToLong(t -> alignment(t, false)).max().orElse(1);
    }

    @Override
    public long alignment(Type t, boolean isVar) {
        if (t instanceof Scalar) {
            return alignmentOfScalar((Scalar) t);
        } else if (t instanceof Array) {
            // when array is used alone
            return alignmentOfArray((Array) t, isVar);
        } else if (t instanceof Container) {
            return alignmentOfContainer((Container) t);
        } else if (t instanceof BitFields) {
            return alignmentOfScalar(((BitFields) t).getStorage());
        } else if (t instanceof Pointer) {
            return definedAlignment('p');
        } else {
            throw new IllegalArgumentException("Invalid type: " + t);
        }
    }

    public long sizeOfArray(Array ar) {
        long occurrence = ar.getOccurrence();
        if (occurrence < 0) {
            // VLA or incomplete, unknown size with negative value
            return occurrence;
        } else {
            Type elementType = ar.getElementType();
            return occurrence * alignment(elementType, false);
        }
    }

    class ContainerSizeCalculator implements ContainerSizeInfo {
        private final boolean isUnion;
        private final long pack;
        private long[] offsets;
        private long size = 0;
        private long alignment_of_container = 0;

        ContainerSizeCalculator(Container ct, long pack) {
            this.isUnion = ct.isUnion();
            this.pack = pack;
            offsets = new long[ct.memberCount()];
            for (int i = 0; i < offsets.length; i++) {
                offsets[i] = calculate(ct.getMember(i));
            }
        }

        private long calculate(Type t) {
            long offset = 0;
            long alignment = (pack <= 0) ? AbstractABI.this.alignment(t, false) : pack;
            if (isUnion) {
                if (t.getSize() > size) {
                    size = t.getSize();
                }
            } else {
                offset = AbstractABI.this.alignUp(size, alignment);
                size = offset + t.getSize();
            }

            if (alignment > alignment_of_container) {
                alignment_of_container = alignment;
            }

            return offset;
        }

        @Override
        public long alignment() {
            return alignment_of_container;
        }

        @Override
        public long size() {
            // need to be multiple of alignment requirement
            return AbstractABI.this.alignUp(size, alignment_of_container);
        }

        @Override
        public long[] offsets() {
            return offsets;
        }

        @Override
        public long offset(int index) {
            return offsets[index];
        }
    }

    @Override
    public ContainerSizeInfo layout(Container ct, long pack) {
        return new ContainerSizeCalculator(ct, pack);
    }

    @Override
    public long sizeof(Type t) {
        if (t instanceof Scalar) {
            return t.getSize();
        } else if (t instanceof Array) {
            return sizeOfArray((Array) t);
        } else if (t instanceof Container) {
            return layout((Container) t, -1).size();
        } else if (t instanceof BitFields) {
            return definedSize(((BitFields) t).getStorage().typeCode());
        } else if (t instanceof Pointer) {
            return definedSize('p');
        } else {
            throw new IllegalArgumentException("Invalid type: " + t);
        }
    }

    @Override
    public long align(Type t, boolean isVar, long addr) {
        return alignUp(addr, alignment(t, isVar));
    }
}
