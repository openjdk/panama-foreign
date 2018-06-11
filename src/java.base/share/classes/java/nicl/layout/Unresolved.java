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

import jdk.internal.nicl.LayoutResolver;

import java.util.Map;

/**
 * An unresolved layout acts as a placeholder for another layout. Unresolved layouts can be resolved, which yields
 * a new layout whose size is known. The resolution process is typically driven by the annotations attached to the
 * unresolved layout.
 */
public class Unresolved extends AbstractLayout<Unresolved> implements Layout {
    protected Unresolved(Map<String, String> annotations) {
        super(annotations);
    }

    /**
     * Create a new selector layout from given path expression.
     * @return the new selector layout.
     */
    public static Unresolved of() {
        return new Unresolved(NO_ANNOS);
    }

    @Override
    public long bitsSize() {
        throw new UnsupportedOperationException("bitsSize on Unresolved");
    }

    @Override
    public boolean isPartial() {
        return true;
    }

    @Override
    public String toString() {
        return wrapWithAnnotations("$");
    }

    @Override
    Unresolved dup(Map<String, String> annotations) {
        return new Unresolved(annotations);
    }
}
