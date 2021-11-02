/*
 * Copyright (c) 2020, 2021, Oracle and/or its affiliates. All rights reserved.
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

package jdk.internal.invoke;

import java.lang.invoke.MethodType;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * This class describes a 'native invoker', which is used as an appendix argument to linkToNative calls.
 */
public class NativeEntryPoint {
    static {
        registerNatives();
    }

    private final MethodType methodType;
    private final long invoker;

    private static final WeakReferenceCache<CacheKey, NativeEntryPoint> CACHE = new WeakReferenceCache<>();
    private record CacheKey(MethodType mt, ABIDescriptorProxy abi,
                            List<VMStorageProxy> argMoves, List<VMStorageProxy> retMoves,
                            boolean isImr) {}

    private NativeEntryPoint(MethodType methodType, long invoker) {
        this.methodType = methodType;
        this.invoker = invoker;
    }

    public static NativeEntryPoint make(ABIDescriptorProxy abi,
                                        VMStorageProxy[] argMoves, VMStorageProxy[] returnMoves,
                                        MethodType methodType, boolean needsReturnBuffer) {
        if (returnMoves.length > 1 != needsReturnBuffer) {
            throw new IllegalArgumentException("Multiple register return, but needsReturnBuffer was false");
        }

        assert (methodType.parameterType(0) == long.class) : "Address expected";
        assert (!needsReturnBuffer || methodType.parameterType(1) == long.class) : "IMR address expected";

        CacheKey key = new CacheKey(methodType, abi, Arrays.asList(argMoves), Arrays.asList(returnMoves), isImr);
        return CACHE.get(key, k -> {
            long invoker = makeInvoker(k.mt(), k.abi(), argMoves, returnMoves, k.isImr());
            return new NativeEntryPoint(k.mt(), invoker);
        });
    }

    private static native long makeInvoker(MethodType methodType, ABIDescriptorProxy abi,
                                           VMStorageProxy[] encArgMoves, VMStorageProxy[] encRetMoves,
                                           boolean needsReturnBuffer);

    public MethodType type() {
        return methodType;
    }

    private static native void registerNatives();
}

class WeakReferenceCache<K, V> {
    private final Map<K, Node> cache = new ConcurrentHashMap<>();

    public V get(K key, Function<K, V> valueFactory) {
        return cache
            .computeIfAbsent(key, k -> new Node()) // cheap lock (has to be according to ConcurrentHashMap)
            .get(key, valueFactory); // expensive lock
    }

    private class Node {
        private WeakReference<V> ref;

        public Node() {}

        public V get(K key, Function<K, V> valueFactory) {
            V result;
            if (ref == null || (result = ref.get()) == null) {
                synchronized (this) { // don't let threads race on the valueFactory::apply call
                    if (ref == null || (result = ref.get()) == null) {
                        result = valueFactory.apply(key); // keep alive
                        ref = new WeakReference<>(result);
                    }
                }
            }
            return result;
        }
    }
}
