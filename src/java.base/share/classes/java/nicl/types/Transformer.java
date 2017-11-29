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
package java.nicl.types;

import jdk.internal.nicl.Util;
import jdk.internal.nicl.types.BindingRegistry;
import jdk.internal.nicl.types.Descriptor;

import java.io.ByteArrayOutputStream;
import java.nicl.NativeLibrary;
import java.nicl.Scope;
import java.util.function.Function;

/**
 * Perform data transformation between members
 */
public class Transformer {
    @FunctionalInterface
    public interface N2J<T> {
        T convert(MemoryRegion r);

        default <S> N2J<S> combine(Function<T, S> fn) {
            return r -> fn.apply(convert(r));
        }
    }

    @FunctionalInterface
    public interface J2N<T> {
        void convert(T o, MemoryRegion r);

        default <S> J2N<S> combine(Function<S, T> fn) {
            return (o, r) -> convert(fn.apply(o), r);
        }
    }

    public static final <T> boolean register(String nativeDescriptor, Class<T> carrierClass,
            N2J<T> native2java, J2N<T> java2native) {
        jdk.internal.nicl.types.Type nt = (new Descriptor(nativeDescriptor)).types().findFirst().get();
        BindingRegistry registry = BindingRegistry.getInstance();
        return registry.register(nt, carrierClass, native2java, java2native);
    }

    private static final LayoutType<Byte> BYTE_TYPE = NativeLibrary.createLayout(byte.class);
    private static final LayoutType<Pointer<Byte>> BYTE_PTR_TYPE = BYTE_TYPE.ptrType();

    public static String toString(Pointer<Byte> cstr) {
        if (cstr == null || cstr.isNull()) {
            return null;
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        byte b;
        for (int i = 0; (b = cstr.offset(i).lvalue().get()) != 0; i++) {
            os.write(b);
        }
        return os.toString();
    }

    private static Pointer<Byte> toCString(byte[] ar, Scope scope) {
        try {
            Pointer<Byte> buf = scope.allocateArray(BYTE_TYPE, ar.length + 1);
            Pointer<Byte> src = Util.createArrayElementsPointer(ar);
            Util.copy(src, buf, ar.length);
            buf.offset(ar.length).lvalue().set((byte)0);
            return buf;
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Pointer<Byte> toCString(String str, Scope scope) {
        return toCString(str.getBytes(), scope);
    }

    public static Pointer<Pointer<Byte>> toCStrArray(String[] ar, Scope scope) {
        if (ar.length == 0) {
            return null;
        }

        Pointer<Pointer<Byte>> ptr = scope.allocateArray(BYTE_PTR_TYPE, ar.length);
        for (int i = 0; i < ar.length; i++) {
            Pointer<Byte> s = toCString(ar[i], scope);
            ptr.offset(i).lvalue().set(s);
        }

        return ptr;
    }
}
