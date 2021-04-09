/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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
package jdk.internal.jextract.impl;

import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.ValueLayout;
import java.nio.ByteOrder;

/*
 * Layouts for the primitive types not supported by ABI implementations.
 */
public final class UnsupportedLayouts {
    private UnsupportedLayouts() {}

    private static final String ATTR_LAYOUT_KIND = "jextract.abi.unsupported.layout.kind";

    public static final ValueLayout __INT128 = MemoryLayout.valueLayout(128, ByteOrder.nativeOrder()).
            withAttribute(ATTR_LAYOUT_KIND, "__int128");

    public static final ValueLayout LONG_DOUBLE = MemoryLayout.valueLayout(128, ByteOrder.nativeOrder()).
            withAttribute(ATTR_LAYOUT_KIND, "long double");

    public static final ValueLayout _FLOAT128 = MemoryLayout.valueLayout(128, ByteOrder.nativeOrder()).
            withAttribute(ATTR_LAYOUT_KIND, "_float128");

    public static final ValueLayout __FP16 = MemoryLayout.valueLayout(16, ByteOrder.nativeOrder()).
            withAttribute(ATTR_LAYOUT_KIND, "__fp16");

    public static final ValueLayout CHAR16 = MemoryLayout.valueLayout(16, ByteOrder.nativeOrder()).
            withAttribute(ATTR_LAYOUT_KIND, "char16");

    public static final ValueLayout WCHAR_T = MemoryLayout.valueLayout(16, ByteOrder.nativeOrder()).
            withAttribute(ATTR_LAYOUT_KIND, "wchar_t");

    static boolean isUnsupported(MemoryLayout vl) {
        return vl.attribute(ATTR_LAYOUT_KIND).isPresent();
    }

    static String getUnsupportedTypeName(MemoryLayout vl) {
        return (String)
                vl.attribute(ATTR_LAYOUT_KIND).orElseThrow(IllegalArgumentException::new);
    }
}
