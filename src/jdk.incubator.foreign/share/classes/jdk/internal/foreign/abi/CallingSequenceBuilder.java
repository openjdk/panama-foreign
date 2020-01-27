/*
 *  Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.
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
 *  Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 */
package jdk.internal.foreign.abi;

import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryLayout;

import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

public class CallingSequenceBuilder {
    private final List<List<Binding>> inputBindings = new ArrayList<>();
    private List<Binding> ouputBindings = List.of();

    private MethodType mt = MethodType.methodType(void.class);
    private FunctionDescriptor desc = FunctionDescriptor.ofVoid(false);

    public final CallingSequenceBuilder addArgumentBindings(Class<?> carrier, MemoryLayout layout,
                                                            List<Binding> bindings) {
        inputBindings.add(bindings);
        mt = mt.appendParameterTypes(carrier);
        descAddArgument(layout);
        return this;
    }

    private void descAddArgument(MemoryLayout layout) {
        boolean isVoid = desc.returnLayout().isEmpty();
        var args = new ArrayList<>(desc.argumentLayouts());
        args.add(layout);
        var argsArray = args.toArray(MemoryLayout[]::new);
        if (isVoid) {
            desc = FunctionDescriptor.ofVoid(false, argsArray);
        } else {
            desc = FunctionDescriptor.of(desc.returnLayout().get(), false, argsArray);
        }
    }

    public CallingSequenceBuilder setReturnBindings(Class<?> carrier, MemoryLayout layout,
                                                    List<Binding> bindings) {
        this.ouputBindings = bindings;
        mt = mt.changeReturnType(carrier);
        desc = FunctionDescriptor.of(layout, false, desc.argumentLayouts().toArray(MemoryLayout[]::new));
        return this;
    }

    public CallingSequence build() {
        return new CallingSequence(mt, desc, inputBindings, ouputBindings);
    }

    public int currentIndex() {
        return mt.parameterCount();
    }
}
