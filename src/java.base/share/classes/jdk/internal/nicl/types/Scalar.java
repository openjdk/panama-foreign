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

import jdk.internal.nicl.Platform;

public class Scalar implements Type {
    final char type;
    final Endianness endianness;
    final long size;

    private final static String noEndianness = "BVcxv";

    public Scalar(char type) {
        this(type, Endianness.NATIVE);
    }

    public Scalar(char type, Endianness endianness) {
        this.type = type;
        this.endianness = (noEndianness.indexOf(type) == -1) ?
                Endianness.NATIVE : endianness;
        this.size = Platform.getInstance().getABI().definedSize(type);
    }

    public Scalar(char type, Endianness endianness, int bits) {
        this.type = type;
        this.size = ((bits & 7) != 0) ? (bits >> 3) + 1 : bits >> 3;
        this.endianness = (noEndianness.indexOf(type) != -1) ?
                Endianness.NATIVE : endianness;
    }

    public Endianness getEndianness() {
        return endianness;
    }

    public char typeCode() {
        return type;
    }

    public boolean isSigned() {
        return Character.isLowerCase(type);
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public int hashCode() {
        return (type & 0xFF) | ((int) size << 8) | (endianness.ordinal() << 16);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof jdk.internal.nicl.types.Scalar)) {
            return false;
        }
        jdk.internal.nicl.types.Scalar other = (jdk.internal.nicl.types.Scalar) o;
        if (type != other.type) {
            return false;
        }
        if (endianness != other.endianness) {
            return false;
        }
        if (size != other.size) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (Endianness.NATIVE != endianness) {
            sb.append(endianness.modifier);
        }
        if (type == 'i' || type == 'f' || type == 'v' || type == 'I' || type == 'F') {
            sb.append('=');
            sb.append(size << 3);
        }
        sb.append(type);
        return sb.toString();
    }
}
