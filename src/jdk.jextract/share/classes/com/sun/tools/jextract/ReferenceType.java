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

import java.nicl.types.Reference;

public final class ReferenceType implements JType {
    final JType referenced;

    ReferenceType(JType type) {
        referenced = type;
    }

    @Override
    public String getDescriptor() {
        return JType.of(Reference.class).getDescriptor();
    }

    @Override
    public String getSignature() {
        JType ref = (referenced instanceof JType2) ? ((JType2) referenced).getDelegate() : referenced;
        StringBuilder sb = new StringBuilder();
        sb.append("L");
        sb.append(Reference.class.getName().replace('.', '/'));
        sb.append("<");
        sb.append(JType.boxing(ref));
        sb.append(">;");
        return sb.toString();
    }
}
