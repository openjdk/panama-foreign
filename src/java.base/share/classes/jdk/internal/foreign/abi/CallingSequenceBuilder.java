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

import java.foreign.layout.Address;
import java.foreign.layout.Layout;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class CallingSequenceBuilder {

    private final EnumMap<StorageClass, List<ArgumentBinding>> bindings = new EnumMap<>(StorageClass.class);
    private final boolean returnsInMemory;

    private int argIndex = 0;

    private final BindingsComputer returnBindgingsComputer;
    private final BindingsComputer argumentBindgingsComputer;
    private final BindingsComputer varargsBindgingsComputer;

    protected CallingSequenceBuilder(Layout ret,
                                     BindingsComputer returnBindgingsComputer,
                                     BindingsComputer argumentBindgingsComputer,
                                     BindingsComputer varargsBindgingsComputer) {
        this.returnBindgingsComputer = returnBindgingsComputer;
        this.argumentBindgingsComputer = argumentBindgingsComputer;
        this.varargsBindgingsComputer = varargsBindgingsComputer;
        if (ret != null) {
            Argument retInfo = makeArgument(ret, -1, "__retval");
            this.returnsInMemory = retInfo.inMemory();
            if (returnsInMemory) {
                retInfo = makeArgument(Address.ofLayout(64, ret), -1, "__retval");
                addArgumentBindings(retInfo, false);
            }
            addReturnBindings(retInfo);
        } else {
            this.returnsInMemory = false;
        }
    }

    public final CallingSequenceBuilder addArgument(Layout l) {
        return addArgument(l, false);
    }

    public final CallingSequenceBuilder addArgument(Layout l, boolean isVarargs) {
        addArgumentBindings(makeArgument(l, argIndex, "arg" + argIndex), isVarargs);
        argIndex++;
        return this;
    }

    public final CallingSequence build() {
        return new CallingSequence(returnsInMemory, bindings);
    }

    protected abstract Argument makeArgument(Layout layout, int pos, String name);

    private void addReturnBindings(Argument a) {
        returnBindgingsComputer.computeBindings(a, this::addBinding);
    }

    private void addArgumentBindings(Argument a, boolean isVarargs) {
        if (isVarargs) {
            varargsBindgingsComputer.computeBindings(a, this::addBinding);
        } else {
            argumentBindgingsComputer.computeBindings(a, this::addBinding);
        }
    }

    private void addBinding(StorageClass storageClass, ArgumentBinding binding) {
        bindings.computeIfAbsent(storageClass, _unused -> new ArrayList<>()).add(binding);
    }

    public interface BindingsComputer {
        void computeBindings(Argument argument, BiConsumer<StorageClass, ArgumentBinding> bindingConsumer);
    }
}
