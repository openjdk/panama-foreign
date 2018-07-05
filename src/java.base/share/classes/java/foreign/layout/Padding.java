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
package java.foreign.layout;

import java.util.Map;

/**
 * A padding layout specifies the size of extra space used to align struct fields around word boundaries.
 */
public class Padding extends AbstractLayout<Padding> implements Layout {
    private final long size;

    protected Padding(long size, Map<String, String> annotations) {
        super(annotations);
        this.size = size;
    }

    /**
     * Create a new selector layout from given path expression.
     * @return the new selector layout.
     */
    public static Padding of(long size) {
        return new Padding(size, NO_ANNOS);
    }

    @Override
    public long bitsSize() {
        return size;
    }

    @Override
    public boolean isPartial() {
        return false;
    }

    @Override
    public String toString() {
        return wrapWithAnnotations("x" + size);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Padding)) {
            return false;
        }
        Padding p = (Padding)other;
        return size == p.size;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(size);
    }

    @Override
    Padding dup(Map<String, String> annotations) {
        return new Padding(size, annotations);
    }
}
