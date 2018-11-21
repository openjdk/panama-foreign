/*
 * Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.tools.jextract;

import java.foreign.memory.Pointer;

public final class PointerType implements JType {
    final JType referenced;

    PointerType(JType type) {
        referenced = type;
    }

    @Override
    public String getDescriptor() {
        return JType.of(Pointer.class).getDescriptor();
    }

    public String getSignature(boolean useWildcard) {
        StringBuilder sb = new StringBuilder();
        sb.append("L");
        sb.append(Pointer.class.getName().replace('.', '/'));
        sb.append("<");
        JType pt = referenced;
        if (pt instanceof JType2) {
            pt = ((JType2) pt).getDelegate();
        }
        if (pt instanceof TypeAlias) {
            pt = ((TypeAlias) pt).canonicalType();
        }
        if (pt == JType.Void && useWildcard) {
            sb.append("*");
        } else {
            sb.append(JType.boxing(pt));
        }
        sb.append(">;");
        return sb.toString();
    }

    @Override
    public String getSignature() {
        return getSignature(false);
    }

    public JType getPointeeType() {
        return referenced;
    }
}
