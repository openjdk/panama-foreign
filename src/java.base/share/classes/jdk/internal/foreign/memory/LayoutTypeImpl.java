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

package jdk.internal.foreign.memory;

import jdk.internal.foreign.abi.SystemABI;

import java.foreign.memory.Callback;
import java.lang.invoke.MethodHandle;
import java.foreign.layout.Address;
import java.foreign.layout.Layout;
import java.foreign.layout.Sequence;
import java.foreign.memory.Array;
import java.foreign.memory.LayoutType;
import java.foreign.memory.Pointer;

public class LayoutTypeImpl<X> implements LayoutType<X> {

    private final Class<X> carrier;
    private final Reference reference;
    private final Layout layout;

    private static long PTR_SIZE = SystemABI.getInstance().layoutFor(SystemABI.CType.Pointer).bitsSize();

    LayoutTypeImpl(Class<X> carrier, Layout layout, Reference reference) {
        this.carrier = carrier;
        this.reference = reference;
        this.layout = layout;
    }

    @Override
    public Layout layout() {
        return layout;
    }

    @Override
    public MethodHandle getter() {
        return reference.getter();
    }

    @Override
    public MethodHandle setter() {
        return reference.setter();
    }

    public LayoutType<?> elementType() {
        throw new IllegalStateException();
    }

    public LayoutType<?> pointeeType() {
        throw new IllegalStateException();
    }

    public Class<?> carrier() {
        return carrier;
    }

    public Class<?> getFuncIntf() {
        throw new IllegalStateException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public LayoutType<Array<X>> array(long size) {
        return new LayoutTypeImpl<>((Class)Array.class, Sequence.of(size, layout), References.ofArray) {
            @Override
            public LayoutType<?> elementType() {
                return LayoutTypeImpl.this;
            }
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public LayoutType<Pointer<X>> pointer() {
        return new LayoutTypeImpl<>((Class)Pointer.class, Address.ofLayout(PTR_SIZE, layout), References.ofPointer) {
            @Override
            public LayoutType<?> pointeeType() {
                return LayoutTypeImpl.this;
            }
        };
    }

    public static <X> LayoutType<X> of(Class<X> carrier, Layout layout, Reference reference) {
        return new LayoutTypeImpl<>(carrier, layout, reference);
    }

    @SuppressWarnings("unchecked")
    public static <X extends Callback<X>> LayoutType<Callback<X>> ofCallback(Address layout, Reference reference, Class<X> funcIntf) {
        return new LayoutTypeImpl<>((Class)Callback.class, layout, reference) {
            @Override
            public Class<?> getFuncIntf() {
                return funcIntf;
            }
        };
    }
}
