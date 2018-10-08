/*
 * Copyright (c) 2015, 2018, Oracle and/or its affiliates. All rights reserved.
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
package jdk.internal.foreign.abi;

import jdk.internal.foreign.abi.sysv.x64.Constants;

import java.util.ArrayList;
import java.util.List;

public class CallingSequence {
    private final List<ArgumentBinding>[] bindings;
    private final boolean returnsInMemory;
    private final List<ArgumentBinding>[] argBindings;
    private final List<ArgumentBinding> retBindings = new ArrayList<>();
    private final int[] argsOffsets = new int[Constants.ARGUMENT_STORAGE_CLASSES.length];
    private final int[] retOffsets = new int[StorageClass.values().length];

    @SuppressWarnings({"unchecked", "rawtypes"})
    public CallingSequence(int argSize, ArrayList<ArgumentBinding>[] bindings, boolean returnsInMemory) {
        this.bindings = bindings;
        this.returnsInMemory = returnsInMemory;
        argBindings = new List[argSize];
        classifyBindings();
    }

    public List<ArgumentBinding> getBindings(StorageClass storageClass) {
        return bindings[storageClass.ordinal()];
    }

    public boolean returnsInMemory() {
        return returnsInMemory;
    }

    private void classifyBindings() {
        for (StorageClass storageClass : StorageClass.values()) {
            for (ArgumentBinding binding : getBindings(storageClass)) {
                if (storageClass.isArgumentClass()) {
                    //update offsets
                    for (int i = storageClass.ordinal() + 1 ; i < argsOffsets.length ; i++) {
                        argsOffsets[i]++;
                    }
                    //classify arguments
                    if (storageClass == StorageClass.INTEGER_ARGUMENT_REGISTER &&
                            returnsInMemory() && binding.getStorage().getStorageIndex() == 0) {
                        retBindings.add(binding);
                    } else {
                        int index = binding.getMember().getArgumentIndex();
                        List<ArgumentBinding> args = argBindings[index];
                        if (args == null) {
                            argBindings[index] = args = new ArrayList<>();
                        }
                        args.add(binding);
                    }
                } else {
                    if (!returnsInMemory()) {
                        //update offsets
                        for (int i = storageClass.ordinal() + 1 ; i < retOffsets.length ; i++) {
                            retOffsets[i]++;
                        }
                        //classify returns
                        retBindings.add(binding);
                    }
                }
            }
        }
    }

    public List<ArgumentBinding> getArgumentBindings(int i) {
        return argBindings[i] == null ? List.of() : argBindings[i];
    }

    public List<ArgumentBinding> getReturnBindings() {
        return retBindings == null ? List.of() : retBindings;
    }

    public long argumentStorageOffset(ArgumentBinding b) {
        return argsOffsets[b.getStorage().getStorageClass().ordinal()] +
                 b.getStorage().getStorageIndex();
    }

    public long returnStorageOffset(ArgumentBinding b) {
        return retOffsets[b.getStorage().getStorageClass().ordinal()] +
                 b.getStorage().getStorageIndex();
    }

    public String asString() {
        StringBuilder sb = new StringBuilder();

        sb.append("CallingSequence: {\n");
        sb.append("  Flags:\n");
        if (returnsInMemory) {
            sb.append("    returnsInMemory\n");
        }
        for (StorageClass c : StorageClass.values()) {
            sb.append("  ").append(c).append("\n");
            for (ArgumentBinding binding : getBindings(c)) {
                if (binding != null) {
                    sb.append("    ").append(binding.toString()).append("\n");
                }
            }
        }
        sb.append("}\n");

        return sb.toString();
    }
}
