/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
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

import java.util.ArrayList;

public class CallingSequence {
    private final ArrayList<ArgumentBinding>[] bindings;
    private final boolean returnsInMemory;

    public CallingSequence(ArrayList<ArgumentBinding>[] bindings, boolean returnsInMemory) {
        this.bindings = bindings;
        this.returnsInMemory = returnsInMemory;
    }

    public ArrayList<ArgumentBinding> getBindings(StorageClass storageClass) {
        return bindings[storageClass.ordinal()];
    }

    public boolean returnsInMemory() {
        return returnsInMemory;
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
