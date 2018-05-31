/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.jextract;

import jdk.internal.clang.Cursor;
import jdk.internal.clang.Type;

/**
 * Enhances JType with native type descriptor
 */
public class JType2 implements JType {
    final JType delegate;
    final Type cType;
    final Cursor cursor;

    private JType2(Type ct, Cursor c, JType type) {
        cType = ct;
        cursor = c;
        delegate = type;
    }

    public String getNativeDescriptor() {
        return Utils.getLayout(cType).toString();
    }

    public int getCallingConvention() {
        return cType.getCallingConvention().ordinal();
    }

    public JType getDelegate() {
        return delegate;
    }

    public Cursor getCursor() { return cursor; }

    public static JType2 bind(JType type, Type cType, Cursor c) {
        return new JType2(cType, c, type);
    }

    @Override
    public String getDescriptor() {
        return delegate.getDescriptor();
    }

    @Override
    public String getSignature() {
        return delegate.getSignature();
    }
}
