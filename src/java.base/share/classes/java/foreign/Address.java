/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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
package java.foreign;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The layout corresponding to a memory address. An address layout can (optionally) be associated with a descriptor
 * describing the contents of the target memory region pointed to by the address (see {@link Layout}).
 */
public final class Address extends Value {

    /**
     * The addressee kind. Denotes the contents of the memory region pointed at by an address.
     */
    private enum PointeeKind {
        /**
         * The address points to some data with given layout.
         */
        LAYOUT,
        /**
         * The address does not have any associated pointee info.
         */
        VOID
    }

    private final PointeeKind pointeeKind;
    private final Layout layout;

    private Address(PointeeKind pointeeKind, Layout layout, long size, Kind kind, Endianness endianness, Optional<Compound> contents, Map<String, String> annotations) {
        super(kind, endianness, size, contents, annotations);
        this.pointeeKind = pointeeKind;
        this.layout = layout;
    }

    @Override
    public Address withContents(Compound contents) {
        return new Address(pointeeKind, layout, bitsSize(), kind(), endianness(), Optional.of(contents), annotations());
    }

    /**
     * Obtain the {@link Layout} object associated with the memory region pointed to by this address (if any).
     * @return an optional {@link Layout} object.
     */
    public Optional<Layout> layout() {
        return pointeeKind == PointeeKind.LAYOUT ?
                Optional.of(layout) : Optional.empty();
    }

    /**
     * Create a new address of given size.
     * @param size address size.
     * @return the new address layout.
     */
    public static Address ofVoid(long size) {
        return ofVoid(size, Kind.INTEGRAL_UNSIGNED);
    }

    /**
     * Create a new address of given size and kind.
     * @param size address size.
     * @param kind address kind.
     * @return the new address layout.
     */
    public static Address ofVoid(long size, Kind kind) {
        return ofVoid(size, kind, Endianness.hostEndian());
    }

    /**
     * Create a new address of given size, kind and endianness.
     * @param size address size.
     * @param kind address kind.
     * @param endianness address endianness.
     * @return the new address layout.
     */
    public static Address ofVoid(long size, Kind kind, Endianness endianness) {
        return new Address(PointeeKind.VOID, null, size, kind, endianness, Optional.empty(), NO_ANNOS);
    }

    /**
     * Create a new address of given size and addressee layout.
     * @param size address size.
     * @param layout addressee layout.
     * @return the new address layout.
     */
    public static Address ofLayout(long size, Layout layout) {
        return ofLayout(size, layout, Kind.INTEGRAL_UNSIGNED);
    }

    /**
     * Create a new address of given size, kind and addressee layout.
     * @param size address size.
     * @param kind address kind.
     * @param layout addressee layout.
     * @return the new address layout.
     */
    public static Address ofLayout(long size, Layout layout, Kind kind) {
        return ofLayout(size, layout, kind, Endianness.hostEndian());
    }

    /**
     * Create a new address of given size, kind, endianness and addressee layout.
     * @param size address size.
     * @param kind address sign.
     * @param endianness address endianness.
     * @param layout addressee layout.
     * @return the new address layout.
     */
    public static Address ofLayout(long size, Layout layout, Kind kind, Endianness endianness) {
        return new Address(PointeeKind.LAYOUT, layout, size, kind, endianness, Optional.empty(), NO_ANNOS);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Address)) {
            return false;
        }
        Address addr = (Address)other;
        return super.equals(other) &&
                Objects.equals(layout, addr.layout);
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ Objects.hashCode(layout);
    }

    @Override
    public String toString() {
        return super.toString() + ":" + (layout == null ? "v" : layout.toString());
    }

    @Override
    Address withAnnotations(Map<String, String> annotations) {
        return new Address(pointeeKind, layout, bitsSize(), kind(), endianness(), contents(), annotations);
    }

    @Override
    public Address stripAnnotations() {
        return (Address)super.stripAnnotations();
    }

    @Override
    public Address withAnnotation(String name, String value) {
        return (Address)super.withAnnotation(name, value);
    }
}
