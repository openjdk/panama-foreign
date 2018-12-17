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

class ShuffleRecipeOperationCollector {
    private static final int MAX_VECTOR_WIDTH_BITS = 256; // Look it up
    private static final int MAX_VECTOR_WIDTH_LONGS = MAX_VECTOR_WIDTH_BITS / 64;

    private final ArrayList<ShuffleRecipeOperation>[] ops;
    private int nPulls;

    @SuppressWarnings("unchecked")
    ShuffleRecipeOperationCollector() {
        ops = (ArrayList<ShuffleRecipeOperation>[])new ArrayList<?>[ShuffleRecipeClass.values().length];
        for (ShuffleRecipeClass c : ShuffleRecipeClass.values()) {
            ops[c.ordinal()] = new ArrayList<>();
        }
    }

    ArrayList<ShuffleRecipeOperation>[] getOps() {
        return ops;
    }

    int getNoofPulls() {
        return nPulls;
    }

    int getTotalNumberOfOps() {
        int n = 0;
        for (ArrayList<ShuffleRecipeOperation> oparr : ops) {
            n += oparr.size();
            n++; // account for implicit STOP at the end of each class
        }
        return n;
    }

    void add(ShuffleRecipeClass c, ShuffleRecipeOperation op) {
        if (op == ShuffleRecipeOperation.STOP) {
            throw new IllegalArgumentException("STOP is implicit and must not be added explicitly");
        }
        ops[c.ordinal()].add(op);
        if (op == ShuffleRecipeOperation.PULL) {
            nPulls++;
        }
    }

    void add(ShuffleRecipeClass c, ShuffleRecipeOperation op, long n) {
        for (long i = 0; i < n; i++) {
            add(c, op);
        }
    }

    void addPull(ShuffleRecipeClass c) {
        addPulls(c, 1);
    }

    void addPulls(ShuffleRecipeClass c, long n) {
        add(c, ShuffleRecipeOperation.PULL, n);
        if (c == ShuffleRecipeClass.VECTOR && n < MAX_VECTOR_WIDTH_LONGS) {
            add(c, ShuffleRecipeOperation.SKIP);
        }
    }

    public String asString() {
        StringBuilder sb = new StringBuilder();
        for(ShuffleRecipeClass cls : ShuffleRecipeClass.values()) {
            sb.append(cls.name()).append(": ").append(ops[cls.ordinal()]).append("\n");
        }
        return sb.toString();
    }
}
