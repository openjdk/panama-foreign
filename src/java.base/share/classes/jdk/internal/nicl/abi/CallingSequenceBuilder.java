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
package jdk.internal.nicl.abi;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import jdk.internal.nicl.Argument;
import java.nicl.layout.Layout;
import java.util.ArrayList;
import java.util.List;

public class CallingSequenceBuilder {
    private static final String DUMMY_RETURN_VARIABLE_NAME = "__retval";

    private Argument returned = null;
    private final ArrayList<Argument> arguments = new ArrayList<>();
    private final Class<? extends AbstractCallingSequenceBuilderImpl> implClass;

    private int curArgIndex = 0;

    public CallingSequenceBuilder(Class<? extends AbstractCallingSequenceBuilderImpl> implClass) {
        this.implClass = implClass;
    }

    /**
     * Add an anonymous argument
     */
    public CallingSequenceBuilder addArgument(Layout type) {
        return addArgument(type, null);
    }

    /**
     * Add a named argument
     */
    public CallingSequenceBuilder addArgument(Layout type, String name) {
        arguments.add(new Argument(curArgIndex++, type, name));
        return this;
    }

    public List<Argument> getArguments() {
        return arguments;
    }

    public CallingSequenceBuilder setReturnType(Layout type) {
        returned = new Argument(-1, type, DUMMY_RETURN_VARIABLE_NAME);
        return this;
    }

    public Argument getReturn() {
        return returned;
    }

    public CallingSequence build() {
        try {
            MethodHandle mh = MethodHandles.lookup().findConstructor(implClass, MethodType.methodType(void.class, Argument.class, ArrayList.class));
            return ((AbstractCallingSequenceBuilderImpl)mh.invoke(returned, arguments)).build();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
