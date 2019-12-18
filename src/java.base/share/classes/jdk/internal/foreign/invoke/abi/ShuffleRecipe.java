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

import java.foreign.memory.Pointer;
import java.util.Map;

import static sun.security.action.GetBooleanAction.privilegedGetProperty;

public class ShuffleRecipe {
    private static final boolean DEBUG = privilegedGetProperty("jdk.internal.foreign.abi.ShuffleRecipe.DEBUG");

    private final long[] recipe;

    private final int nArgumentPulls;
    private final int nReturnPulls;

    private final Map<ArgumentBinding, Long> offsets;

    ShuffleRecipe(long[] recipe, int nArgumentPulls, int nReturnPulls, Map<ArgumentBinding, Long> offsets) {
        this.recipe = recipe;
        this.nArgumentPulls = nArgumentPulls;
        this.nReturnPulls = nReturnPulls;
        this.offsets = offsets;
    }

    public static ShuffleRecipe make(CallingSequence callingSequence) {
        ShuffleRecipeBuilder builder = new ShuffleRecipeBuilder();

        for (int i = 0 ; i < 2; i++) {
            boolean args = i == 0;
            long offset = 0L;
            for (jdk.internal.foreign.invoke.abi.ShuffleRecipeClass recipeClass : jdk.internal.foreign.invoke.abi.ShuffleRecipeClass.values()) {
                StorageClass storageClass = recipeClass.storageClass(args);
                if (storageClass != null) {
                    int indexInClass = 0;
                    for (ArgumentBinding binding : callingSequence.bindings(storageClass)) {
                        while (indexInClass < binding.storage().getStorageIndex()) {
                            builder.addSkip();
                            indexInClass++;
                        }
                        long size = binding.storage().getSize() / 8;
                        builder.addPulls(!args, size);
                        if (binding.storage().getSize() < binding.storage().getMaxSize()) {
                            builder.addSkip();
                        }
                        builder.addOffset(binding, offset);
                        offset += size;
                        indexInClass++;
                    }
                }
                builder.addStop();
            }
        }

        if(DEBUG) {
            System.out.println("Translating CallingSequence:");
            System.out.println(callingSequence.asString().indent(2));
            System.out.println("into:");
            System.out.println(builder.asString().indent(2));
        }

        return builder.build();
    }

    public long[] getRecipe() {
        return recipe;
    }

    public int getNoofArgumentPulls() {
        return nArgumentPulls;
    }

    public int getNoofReturnPulls() {
        return nReturnPulls;
    }

    public Pointer<?> offset(Pointer<?> ptr, ArgumentBinding binding) {
        return ptr.offset(offsets.get(binding));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("ShuffleRecipe { nArgumentPulls: ")
                .append(nArgumentPulls)
                .append(" nReturnPulls: ")
                .append(nReturnPulls)
                .append("}\n");

        return sb.toString();
    }
}
