/*
 *  Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.  Oracle designates this
 *  particular file as subject to the "Classpath" exception as provided
 *  by Oracle in the LICENSE file that accompanied this code.
 *
 *  This code is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  version 2 for more details (a copy is included in the LICENSE file that
 *  accompanied this code).
 *
 *  You should have received a copy of the GNU General Public License version
 *  2 along with this work; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *   Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 *
 */
package jdk.internal.foreign.invoke.abi;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CallingSequence {
    private final EnumMap<StorageClass, List<ArgumentBinding>> bindings;
    private final boolean returnsInMemory;
    private final Map<Integer, List<ArgumentBinding>> bindingsByIndex;

    public CallingSequence(boolean returnsInMemory, EnumMap<StorageClass, List<ArgumentBinding>> bindings) {
        this.bindings = new EnumMap<>(bindings);
        this.returnsInMemory = returnsInMemory;
        this.bindingsByIndex = Stream.of(StorageClass.values())
                .flatMap(sc -> bindings(sc).stream())
                .collect(Collectors.groupingBy(b -> b.argument().argumentIndex()));
    }

    public List<ArgumentBinding> bindings(StorageClass storageClass) {
        return bindings.getOrDefault(storageClass, List.of());
    }

    public boolean returnsInMemory() {
        return returnsInMemory;
    }

    public List<ArgumentBinding> argumentBindings(int i) {
        return bindingsByIndex.getOrDefault(i, List.of());
    }

    public List<ArgumentBinding> returnBindings() {
        if (returnsInMemory) {
            throw new IllegalStateException("Attempting to obtain return bindings for in-memory return!");
        }
        return bindingsByIndex.getOrDefault(-1, List.of());
    }

    private static boolean isReturnInMemoryStorageClass(StorageClass storageClass) {
        return storageClass == StorageClass.INTEGER_ARGUMENT_REGISTER
            || storageClass == StorageClass.INDIRECT_RESULT_REGISTER;
    }

    public ArgumentBinding returnInMemoryBinding() {
        if (!returnsInMemory) {
            throw new IllegalStateException("Attempting to obtain in-memory binding for regular return");
        }
        //if returns in memory, we have two bindings with position -1, the argument and the return.
        //The code below filters out the return binding.
        return bindingsByIndex.getOrDefault(-1, List.of()).stream()
                .filter(b -> isReturnInMemoryStorageClass(b.storage().getStorageClass()))
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }

    public String asString() {
        StringBuilder sb = new StringBuilder();

        sb.append("CallingSequence: {\n");
        sb.append("  returnsInMemory: " + returnsInMemory + "\n");
        sb.append("  Classes:\n");
        for (StorageClass c : StorageClass.values()) {
            sb.append("    ").append(c).append("\n");
            for (ArgumentBinding binding : bindings(c)) {
                if (binding != null) {
                    sb.append("      ").append(binding.toString()).append("\n");
                }
            }
        }
        sb.append("}\n");

        return sb.toString();
    }
}
