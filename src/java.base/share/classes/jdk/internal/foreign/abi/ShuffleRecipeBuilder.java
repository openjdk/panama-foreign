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

import jdk.internal.foreign.Util;
import java.util.ArrayList;

class ShuffleRecipeBuilder {
    private final ShuffleRecipeOperationCollector arguments = new ShuffleRecipeOperationCollector();
    private final ShuffleRecipeOperationCollector returns = new ShuffleRecipeOperationCollector();

    ShuffleRecipeBuilder() {
    }

    ShuffleRecipeOperationCollector getArgumentsCollector() {
        return arguments;
    }

    ShuffleRecipeOperationCollector getReturnsCollector() {
        return returns;
    }

    private int getTotalNumberOfOps() {
        return arguments.getTotalNumberOfOps() + returns.getTotalNumberOfOps();
    }

    private long[] allocArray() {
        long nOpBits = getTotalNumberOfOps() * ShuffleRecipeOperation.BITS_PER_OP;
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
        for (ArrayList<ShuffleRecipeOperation> oparr : arguments.getOps()) {
            for (ShuffleRecipeOperation op : oparr) {
                encodeOp(arr, i++, op);
            }
            encodeOp(arr, i++, ShuffleRecipeOperation.STOP);
        }
        for (ArrayList<ShuffleRecipeOperation> oparr : returns.getOps()) {
            for (ShuffleRecipeOperation op : oparr) {
                encodeOp(arr, i++, op);
            }
            encodeOp(arr, i++, ShuffleRecipeOperation.STOP);
        }

        return arr;
    }

    ShuffleRecipe build() {
        return new ShuffleRecipe(recipeToLongArray(), getArgumentsCollector().getNoofPulls(), getReturnsCollector().getNoofPulls());
    }

    public String asString() {
        StringBuilder sb = new StringBuilder();

        sb.append("ShuffleRecipe: {\n");
        sb.append("  Arguments: {\n");
        sb.append(arguments.asString().indent(4));
        sb.append("  }\n");
        sb.append("  Returns: {\n");
        sb.append(returns.asString().indent(4));
        sb.append("  }\n");
        sb.append("}\n");

        return sb.toString();
    }
}
