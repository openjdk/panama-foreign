/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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
package java.nicl.layout;

import java.util.Map;
import java.util.Optional;

/**
 * The layout corresponding to a memory address. An address layout can (optionally) be associated with additional
 * information describing the contents of the target memory region pointed to by the address (see {@link Info}).
 */
public final class Address extends Value {

    /**
     * The info associated with the memory region pointed to by an address. The target memory region can be associated
     * with either a layout, or a function.
     */
    public static class Info {
        /**
         * The addressee kind. Denotes the contents of the memory region pointed at by an address.
         */
        public enum Kind {
            /**
             * The address points to some data with given layout.
             */
            LAYOUT,
            /**
             * The address points to the entry point of some native function.
             */
            FUNCTION
        }

        private final Kind kind;
        private final Object info;

        private Info(Kind kind, Object info) {
            this.kind = kind;
            this.info = info;
        }

        /**
         * Test as to whether this addressee info has given kind (see {@link Kind}).
         * @param kind the kind this address is compared with.
         * @return true, if this address has given kind.
         */
        public boolean hasKind(Kind kind) {
            return this.kind == kind;
        }

        /**
         * The memory region layout associated with this addressee info.
         * @return the addressee layout.
         * @throws UnsupportedOperationException if the info kind is not {@link Kind#LAYOUT}.
         */
        public Layout layout() throws UnsupportedOperationException {
            if (!hasKind(Kind.LAYOUT)) {
                throw new UnsupportedOperationException("Cannot retrieve layout info");
            } else {
                return (Layout)info;
            }
        }

        /**
         * The function description associated with this addressee info.
         * @return the addressee function.
         * @throws UnsupportedOperationException if the info kind is not {@link Kind#FUNCTION}.
         */
        public Function function() {
            if (!hasKind(Kind.FUNCTION)) {
                throw new UnsupportedOperationException("Cannot retrieve function info");
            } else {
                return (Function)info;
            }
        }

        static Info ofLayout(Layout layout) {
            return new Info(Kind.LAYOUT, layout);
        }

        static Info ofFunction(Function function) {
            return new Info(Kind.FUNCTION, function);
        }
    }

    private final Optional<Info> info;

    private Address(Optional<Info> info, long size, Kind kind, Endianness endianness, Optional<Group> contents, Map<String, String> annotations) {
        super(kind, endianness, size, contents, annotations);
        this.info = info;
    }

    @Override
    public Address withContents(Group contents) {
        return new Address(info, bitsSize(), kind(), endianness(), Optional.of(contents), annotations());
    }

    /**
     * Obtain the addressee info associated with this address (if any).
     * @return the addressee info.
     */
    public Optional<Info> addresseeInfo() {
        return info;
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
        return ofVoid(size, kind, Endianness.LITTLE_ENDIAN);
    }

    /**
     * Create a new address of given size, kind and endianness.
     * @param size address size.
     * @param kind address kind.
     * @param endianness address endianness.
     * @return the new address layout.
     */
    public static Address ofVoid(long size, Kind kind, Endianness endianness) {
        return new Address(Optional.empty(), size, kind, endianness, Optional.empty(), NO_ANNOS);
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
        return ofLayout(size, layout, kind, Endianness.LITTLE_ENDIAN);
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
        return new Address(Optional.of(Info.ofLayout(layout)), size, kind, endianness, Optional.empty(), NO_ANNOS);
    }

    /**
     * Create a new address of given size and addressee function.
     * @param size address size.
     * @param function addressee function.
     * @return the new address layout.
     */
    public static Address ofFunction(long size, Function function) {
        return ofFunction(size, function, Kind.INTEGRAL_UNSIGNED);
    }

    /**
     * Create a new address of given size, kind and addressee function.
     * @param size address size.
     * @param kind address kind.
     * @param function addressee function.
     * @return the new address layout.
     */
    public static Address ofFunction(long size, Function function, Kind kind) {
        return ofFunction(size, function, kind, Endianness.LITTLE_ENDIAN);
    }

    /**
     * Create a new address of given size, kind, endianness and addressee function.
     * @param size address size.
     * @param kind address kind.
     * @param endianness address endianness.
     * @param function addressee function.
     * @return the new address layout.
     */
    public static Address ofFunction(long size, Function function, Kind kind, Endianness endianness) {
        return new Address(Optional.of(Info.ofFunction(function)), size, kind, endianness, Optional.empty(), NO_ANNOS);
    }

    @Override
    public String toString() {
        return super.toString() + ":" + info.map(i -> i.info).orElse("v");
    }

    @Override
    Address dup(Map<String, String> annotations) {
        return new Address(info, bitsSize(), kind(), endianness(), contents(), annotations);
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
