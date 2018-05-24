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

package jdk.internal.nicl.types;

public class Array implements Type {
    final Type elementType;
    final int occurrence;

    /**
     * Array type.
     *
     * @param elementType The element type
     * @param occurrence  Number of elements. Negative number indicates incomplete array.
     */
    public Array(Type elementType, int occurrence) {
        this.elementType = elementType;
        this.occurrence = occurrence;
    }

    public int getOccurrence() {
        return occurrence;
    }

    public Type getElementType() {
        return elementType;
    }

    @Override
    public long getSize() {
        if (occurrence < 0) {
            return occurrence;
        }

        return occurrence * elementType.getSize();
    }

    public int hashCode() {
        return occurrence * 31 + elementType.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof jdk.internal.nicl.types.Array)) {
            return false;
        }
        jdk.internal.nicl.types.Array other = (jdk.internal.nicl.types.Array) o;
        return (occurrence == other.occurrence &&
                elementType.equals(other.elementType));
    }

    @Override
    public String toString() {
        return ((occurrence < 0) ? "*" : Integer.toString(occurrence)) +
                elementType.toString();
    }
}
