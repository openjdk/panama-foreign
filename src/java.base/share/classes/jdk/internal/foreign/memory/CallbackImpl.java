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

import java.foreign.memory.Callback;
import java.foreign.memory.Pointer;
import jdk.internal.foreign.LibrariesHelper;

public class CallbackImpl<X> implements Callback<X> {

    private final Pointer<?> addr;
    private final Class<?> funcIntfClass;

    public CallbackImpl(Pointer<?> addr, Class<?> funcIntfClass) {
        this.addr = addr;
        this.funcIntfClass = funcIntfClass;
    }

    private static final CallbackImpl<Object> theNullCallback =
        new CallbackImpl<Object>(Pointer.ofNull(), null) {
            @Override
            public Object asFunction() {
                throw new NullPointerException();
            }
        };

    @SuppressWarnings("unchecked")
    public static <X> Callback<X> ofNull() {
        return (Callback<X>) theNullCallback;
    }

    @Override
    public Pointer<?> entryPoint() {
        return addr;
    }

    @Override
    @SuppressWarnings("unchecked")
    public X asFunction() {
        try {
            ((BoundedPointer<?>) entryPoint()).checkAlive();
            //create a wrapper around a true native function
            Class<?> callbackClass = LibrariesHelper.getCallbackImplClass(funcIntfClass);
            return (X) callbackClass.getConstructor(Pointer.class).newInstance(entryPoint());
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
    }
}
