/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
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
package jdk.internal.foreign.abi;

import jdk.internal.foreign.abi.aarch64.linux.LinuxAArch64Linker;
import jdk.internal.foreign.abi.aarch64.macos.MacOsAArch64Linker;
import jdk.internal.foreign.abi.x64.sysv.SysVx64Linker;
import jdk.internal.foreign.abi.x64.windows.Windowsx64Linker;

import java.lang.foreign.CLinker;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.NativeSymbol;
import java.lang.foreign.ResourceScope;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public abstract sealed class AbstractLinker implements CLinker permits LinuxAArch64Linker, MacOsAArch64Linker,
                                                                       SysVx64Linker, Windowsx64Linker {

    private final SoftReferenceCache<FunctionDescriptor, MethodHandle> DOWNCALL_CACHE = new SoftReferenceCache<>();

    @Override
    public MethodHandle downcallHandle(FunctionDescriptor function) {
        Objects.requireNonNull(function);

        return DOWNCALL_CACHE.get(function, fd -> {
            MethodType type = SharedUtils.inferMethodType(fd, false);
            MethodHandle handle = arrangeDowncall(type, fd);
            handle = SharedUtils.maybeInsertAllocator(handle);
            return handle;
        });
    }
    protected abstract MethodHandle arrangeDowncall(MethodType inferredMethodType, FunctionDescriptor function);

    @Override
    public NativeSymbol upcallStub(MethodHandle target, FunctionDescriptor function, ResourceScope scope) {
        Objects.requireNonNull(scope);
        Objects.requireNonNull(target);
        Objects.requireNonNull(function);
        SharedUtils.checkExceptions(target);

        MethodType type = SharedUtils.inferMethodType(function, true);
        if (!type.equals(target.type())) {
            throw new IllegalArgumentException("Wrong method handle type: " + target.type());
        }
        NativeSymbol symb =  arrangeUpcall(target, target.type(), function, scope);
        return symb;
    }

    protected abstract NativeSymbol arrangeUpcall(MethodHandle target, MethodType targetType,
                                                  FunctionDescriptor function, ResourceScope scope);

    private static class SoftReferenceCache<K, V> {
        private final Map<K, Node> cache = new ConcurrentHashMap<>();

        public V get(K key, Function<K, V> valueFactory) {
            return cache
                    .computeIfAbsent(key, k -> new Node()) // short lock (has to be according to ConcurrentHashMap)
                    .get(key, valueFactory); // long lock, but just for the particular key
        }

        private class Node {
            private SoftReference<V> ref;

            public Node() {
            }

            public V get(K key, Function<K, V> valueFactory) {
                V result;
                if (ref == null || (result = ref.get()) == null) {
                    synchronized (this) { // don't let threads race on the valueFactory::apply call
                        if (ref == null || (result = ref.get()) == null) {
                            result = valueFactory.apply(key); // keep alive
                            ref = new SoftReference<>(result);
                        }
                    }
                }
                return result;
            }
        }
    }
}
