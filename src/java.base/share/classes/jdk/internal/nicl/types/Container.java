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

import java.util.Arrays;
import java.util.stream.Stream;
import jdk.internal.nicl.Platform;

public class Container implements Type {
    final Type[] members;
    final boolean isUnion;

    public Container(boolean isUnion, Type... members) {
        this.members = members;
        this.isUnion = isUnion;
    }

    @Override
    public long getSize() {
        return Platform.getInstance().getABI().sizeof(this);
    }

    public boolean isUnion() {
        return isUnion;
    }

    public Stream<Type> getMembers() {
        return Stream.of(members);
    }

    public int memberCount() {
        return members.length;
    }

    public Type getMember(int index) {
        return members[index];
    }

    @Override
    public int hashCode() {
        return (isUnion ? 0x40000000 : 0x60000000) | Arrays.hashCode(members);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Container)) {
            return false;
        }

        Container other = (Container) o;
        if (other.isUnion != isUnion) {
            return false;
        }
        if (other.members.length != members.length) {
            return false;
        }
        for (int i = 0; i < members.length; i++) {
            if (!members[i].equals(other.members[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        for (Type t : members) {
            sb.append(t);
            if (isUnion) {
                sb.append('|');
            }
        }
        if (isUnion) {
            sb.setCharAt(sb.length() - 1, ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }
}
