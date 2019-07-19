/*
 *  Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.
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
 *  Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 */
package jdk.incubator.foreign;

import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * The foreign.layout corresponding to a memory address. An address foreign.layout can (optionally) be associated with a descriptor
 * describing the contents of the target memory region pointed to by the address - either a function or a layout.
 */
public final class AddressLayout extends ValueLayout {

    /**
     * The addressee kind. Denotes the contents of the memory region pointed at by an address.
     */
    enum PointeeKind {
        /**
         * The address points to some data with given
         */
        LAYOUT(MH_LAYOUT_ADDRESS),
        /**
         * The address points to the entry point of some native function.
         */
        FUNCTION(MH_FUNCTION_ADDRESS),
        /**
         * The address does not have any associated pointee info.
         */
        VOID(MH_VOID_ADDRESS);

        final MethodHandleDesc mhDesc;

        PointeeKind(MethodHandleDesc mhDesc) {
            this.mhDesc = mhDesc;
        }
    }

    private final PointeeKind pointeeKind;
    private final Constable descriptor;

    AddressLayout(PointeeKind pointeeKind, Constable descriptor, Kind kind, ByteOrder endianness, long size, OptionalLong alignment, Optional<String> name) {
        super(kind, endianness, size, alignment, name);
        this.pointeeKind = pointeeKind;
        this.descriptor = descriptor;
    }

    /**
     * Obtain the {@link FunctionDescriptor} object associated with the memory region pointed to by this address (if any).
     * @return an optional {@link FunctionDescriptor} object.
     */
    public Optional<FunctionDescriptor> function() {
        return pointeeKind == PointeeKind.FUNCTION ?
                Optional.of((FunctionDescriptor) descriptor) : Optional.empty();
    }

    /**
     * Obtain the {@link MemoryLayout} object associated with the memory region pointed to by this address (if any).
     * @return an optional {@link MemoryLayout} object.
     */
    public Optional<MemoryLayout> layout() {
        return pointeeKind == PointeeKind.LAYOUT ?
                Optional.of((MemoryLayout)descriptor) : Optional.empty();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AddressLayout)) {
            return false;
        }
        AddressLayout addr = (AddressLayout)other;
        return super.equals(other) &&
                Objects.equals(descriptor, addr.descriptor);
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ Objects.hashCode(descriptor);
    }

    @Override
    public String toString() {
        return super.toString() + ":" + (descriptor == null ? "v" : descriptor.toString());
    }

    @Override
    AddressLayout dup(OptionalLong alignment, Optional<String> name) {
        return new AddressLayout(pointeeKind, descriptor, kind, order(), bitSize(), alignment, name);
    }

    @Override
    public Optional<DynamicConstantDesc<ValueLayout>> describeConstable() {
        ConstantDesc[] constants = pointeeKind != PointeeKind.VOID ?
                new ConstantDesc[] { pointeeKind.mhDesc, bitSize(), descriptor.describeConstable().get() } :
                new ConstantDesc[] { pointeeKind.mhDesc, bitSize() };
        return Optional.of(DynamicConstantDesc.ofNamed(
                    ConstantDescs.BSM_INVOKE, "address", CD_ADDRESS_LAYOUT, constants));
    }
}