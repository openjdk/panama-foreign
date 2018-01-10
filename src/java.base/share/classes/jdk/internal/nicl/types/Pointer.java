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

public class Pointer implements Type {
    Type pointee;

    public Pointer(Type pointee) {
        this.pointee = pointee;
    }

    public Type getPointeeType() {
        return pointee;
    }

    @Override
    public long getSize() {
        return Platform.getInstance().getABI().definedSize('p');
    }

    @Override
    public int hashCode() {
        return 0x80000000 | pointee.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof jdk.internal.nicl.types.Pointer)) {
            return false;
        }
        jdk.internal.nicl.types.Pointer other = (jdk.internal.nicl.types.Pointer) o;

        if (pointee == null) {
            return other.pointee == null;
        }

        return pointee.equals(other.pointee);
    }

    @Override
    public String toString() {
        return (pointee == null) ? "p" : "p:" + pointee.toString();
    }
}
