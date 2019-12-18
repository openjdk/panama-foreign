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

import jdk.internal.foreign.invoke.Util;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ShuffleRecipeBuilder {

    List<ShuffleRecipeOperation> ops = new ArrayList<>();
    Map<ArgumentBinding, Long> offsets = new HashMap<>();
    int nArgPulls;
    int nRetPulls;

    void addSkip() {
        ops.add(ShuffleRecipeOperation.SKIP);
    }

    void addStop() {
        ops.add(ShuffleRecipeOperation.STOP);
    }

    void addPulls(boolean isReturn, long n) {
        for (long i = 0; i < n; i++) {
            ops.add(ShuffleRecipeOperation.PULL);
            if (isReturn) {
                nRetPulls++;
            } else {
                nArgPulls++;
            }
        }
    }

    void addOffset(ArgumentBinding binding, long offset) {
        offsets.put(binding, offset);
    }

    private long[] allocArray() {
        long nOpBits = ops.size() * ShuffleRecipeOperation.BITS_PER_OP;
        long nOpWords = nOpBits / 64;

        long nBits = nOpBits + nOpWords; // MSB in each word is reserved

        nBits = Util.alignUp(nBits, 64);

        long nWords = nBits / 64;

        return new long[(int)nWords];
    }

    private void encodeOp(long[] arr, int index, ShuffleRecipeOperation op) {
        int n = index * ShuffleRecipeOperation.BITS_PER_OP;

        int word = n / 64;
        n += word; // MSB bits are reserved

        int start_bit = n % 64;
        if (start_bit == 63) {
            word++;
            start_bit++;
        }

        arr[word] |= (long)op.ordinal() << start_bit;
    }

    private long[] recipeToLongArray() {
        long[] arr = allocArray();

        for (int i = 0; i < arr.length; i++) {
            arr[i] = (1L << 63);
        }

        int i = 0;

        for (ShuffleRecipeOperation op : ops) {
            encodeOp(arr, i++, op);
        }

        return arr;
    }

    ShuffleRecipe build() {
        return new ShuffleRecipe(recipeToLongArray(), nArgPulls, nRetPulls, offsets);
    }

    public String asString() {
        StringBuilder sb = new StringBuilder();

        sb.append("ShuffleRecipe: {\n");
        sb.append("  Operations: {\n");
        sb.append(ops);
        sb.append("  }\n");
        sb.append("}\n");

        return sb.toString();
    }
}
