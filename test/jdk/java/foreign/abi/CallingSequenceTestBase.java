/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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

import jdk.internal.foreign.abi.CallingSequence;
import jdk.internal.foreign.abi.CallingSequenceBuilder;
import jdk.internal.foreign.abi.StorageClass;

import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemoryLayouts;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.testng.Assert.assertEquals;
import static jdk.internal.foreign.abi.StorageClass.*;

public class CallingSequenceTestBase {

    public static class Binding {
        public final StorageClass cls;
        public final long offset;

        public Binding(StorageClass cls, long offset) {
            this.cls = cls;
            this.offset = offset;
        }
    }

    public static class Verifier {
        private final CallingSequenceBuilder csb;
        private final List<Consumer<CallingSequence>> verifiers = new ArrayList<>();
        private final EnumMap<StorageClass, Integer> classCounts = new EnumMap<>(StorageClass.class);

        public Verifier(CallingSequenceBuilder csb) {
            this.csb = csb;
        }

        public Verifier vararg(MemoryLayout arg, Binding...bindings) {
            return argInternal(arg, true, bindings);
        }

        public Verifier args(int repeats, MemoryLayout arg, Binding...bindings) {
            for(int i = 0; i < repeats; i++) {
                arg(arg, bindings);
            }
            return this;
        }

        public Verifier arg(MemoryLayout arg, Binding...bindings) {
            return argInternal(arg, false, bindings);
        }

        private Verifier argInternal(MemoryLayout arg, boolean varargs, Binding...bindings) {
            csb.addArgument(arg, varargs);
            for(Binding binding : bindings) {
                StorageClass cls = binding.cls;
                long offset = binding.offset;
                int indexInClass = classCounts.getOrDefault(cls, 0);
                verifiers.add(recipe -> {
                    assertEquals(recipe.bindings(cls).get(indexInClass).argument().layout(), arg,
                            "Unexpected argument layout");
                    assertEquals(recipe.bindings(cls).get(indexInClass).offset(), offset,
                            "Unexpected binding offset");
                });
                classCounts.put(cls, indexInClass + 1);
            }
            return this;
        }

        public void check(boolean returnsInMemory) {
            CallingSequence recipe = csb.build();

            // System.out.println(recipe.asString());

            assertEquals(returnsInMemory, recipe.returnsInMemory());
            classCounts.forEach((scls, count) -> assertEquals(recipe.bindings(scls).size(), (int) count,
                    String.format("Unexpected argument class count for class %s", scls)));

            for(var verifier : verifiers) {
                verifier.accept(recipe);
            }
        }
    }

    public static Binding binding(StorageClass cls, int offset) {
        return new Binding(cls, offset);
    }

    public void testInteger(Function<MemoryLayout, CallingSequenceBuilder> factory,
                            int maxIntArgs,
                            MemoryLayout intLayout) {
        new Verifier(factory.apply(null))
                .args(maxIntArgs, intLayout,
                        binding(INTEGER_ARGUMENT_REGISTER, 0))
                .args(2, intLayout,
                        binding(STACK_ARGUMENT_SLOT, 0))
                .check(false);
    }

    public void testVector(Function<MemoryLayout, CallingSequenceBuilder> factory,
                           int maxVectorArgs,
                           MemoryLayout vectorLayout) {
        new Verifier(factory.apply(null))
                .args(maxVectorArgs, vectorLayout,
                        binding(VECTOR_ARGUMENT_REGISTER, 0))
                .args(2, vectorLayout,
                        binding(STACK_ARGUMENT_SLOT, 0))
                .check(false);
    }

}
