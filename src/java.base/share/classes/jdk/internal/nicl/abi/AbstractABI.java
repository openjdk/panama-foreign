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

import java.nicl.layout.Address;
import java.nicl.layout.Sequence;
import java.nicl.layout.Group;
import java.nicl.layout.Group.Kind;
import java.nicl.layout.Layout;
import java.nicl.layout.Unresolved;
import java.nicl.layout.Value;

/**
 * Base class implements default rules for ABI. Mostly base on System V AMD64
 * ABI convention.
 * A ABI following similar convention will only need to override definedSize() and
 * arrangeCall() methods.
 */
public abstract class AbstractABI implements SystemABI {

    private long alignUp(long addr, long alignment) {
        return ((addr - 1) | (alignment - 1)) + 1;
    }

    private long alignDown(long addr, long alignment) {
        return addr & ~(alignment - 1);
    }

    protected long alignmentOfScalar(Value st) {
        return st.bitsSize() / 8;
    }

    protected long alignmentOfArray(Sequence ar, boolean isVar) {
        if (ar.elementsSize() == 0) {
            // VLA or incomplete
            return 16;
        } else if ((ar.bitsSize() / 8) >= 16 && isVar) {
            return 16;
        } else {
            // align as element type
            Layout elementType = ar.element();
            return alignment(elementType, false);
        }
    }

    protected long alignmentOfContainer(Group ct) {
        // Most strict member
        return ct.elements().stream().mapToLong(t -> alignment(t, false)).max().orElse(1);
    }

    @Override
    public long alignment(Layout t, boolean isVar) {
        if (t instanceof Value) {
            return alignmentOfScalar((Value) t);
        } else if (t instanceof Sequence) {
            // when array is used alone
            return alignmentOfArray((Sequence) t, isVar);
        } else if (t instanceof Group) {
            return alignmentOfContainer((Group) t);
        } else if (t instanceof Address) {
            return 8;
        } else {
            throw new IllegalArgumentException("Invalid type: " + t);
        }
    }

    public long sizeOfArray(Sequence ar) {
        long occurrence = ar.elementsSize();
        if (occurrence == 0) {
            // VLA or incomplete, unknown size with negative value
            return occurrence;
        } else {
            Layout elementType = ar.element();
            return occurrence * alignment(elementType, false);
        }
    }

    class ContainerSizeInfo {
        private final boolean isUnion;
        private final long pack;
        private long[] offsets;
        private long size = 0;
        private long alignment_of_container = 0;

        ContainerSizeInfo(Group ct, long pack) {
            this.isUnion = ct.kind() == Kind.UNION;
            this.pack = pack;
            offsets = new long[ct.elements().size()];
            for (int i = 0; i < offsets.length; i++) {
                offsets[i] = calculate(ct.elements().get(i));
            }
        }

        private long calculate(Layout t) {
            long offset = 0;
            long alignment = (pack <= 0) ? AbstractABI.this.alignment(t, false) : pack;
            if (isUnion) {
                if ((t.bitsSize() / 8) > size) {
                    size = t.bitsSize() / 8;
                }
            } else {
                offset = AbstractABI.this.alignUp(size, alignment);
                size = offset + (t.bitsSize() / 8);
            }

            if (alignment > alignment_of_container) {
                alignment_of_container = alignment;
            }

            return offset;
        }

        public long alignment() {
            return alignment_of_container;
        }

        public long size() {
            // need to be multiple of alignment requirement
            return AbstractABI.this.alignUp(size, alignment_of_container);
        }

        public long[] offsets() {
            return offsets;
        }

        public long offset(int index) {
            return offsets[index];
        }
    }

    @Override
    public long sizeof(Layout t) {
        if (t instanceof Value) {
            return t.bitsSize() / 8;
        } else if (t instanceof Sequence) {
            return sizeOfArray((Sequence) t);
        } else if (t instanceof Group) {
            return new ContainerSizeInfo((Group) t, -1).size();
        } else if (t instanceof Address) {
            return t.bitsSize() / 8;
        } else {
            throw new IllegalArgumentException("Invalid type: " + t);
        }
    }

    @Override
    public long align(Layout t, boolean isVar, long addr) {
        return alignUp(addr, alignment(t, isVar));
    }
}
