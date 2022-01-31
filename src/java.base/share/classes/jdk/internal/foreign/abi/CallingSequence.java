/*
 * Copyright (c) 2020, 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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

import java.lang.foreign.FunctionDescriptor;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.stream.Stream;

public class CallingSequence {
    private final MethodType mt;
    private final FunctionDescriptor desc;
    private final boolean needsReturnBuffer;
    private final long returnBufferSize;
    private final long allocationSize;

    private final List<Binding> returnBindings;
    private final List<List<Binding>> argumentBindings;

    public CallingSequence(MethodType mt, FunctionDescriptor desc,
                           boolean needsReturnBuffer, long returnBufferSize, long allocationSize,
                           List<List<Binding>> argumentBindings, List<Binding> returnBindings) {
        this.mt = mt;
        this.desc = desc;
        this.needsReturnBuffer = needsReturnBuffer;
        this.returnBufferSize = returnBufferSize;
        this.allocationSize = allocationSize;
        this.returnBindings = returnBindings;
        this.argumentBindings = argumentBindings;
    }

    public int argumentCount() {
        return argumentBindings.size();
    }

    public List<Binding> argumentBindings(int i) {
        return argumentBindings.get(i);
    }

    public Stream<Binding> argumentBindings() {
        return argumentBindings.stream().flatMap(List::stream);
    }

    public List<Binding> returnBindings() {
        return returnBindings;
    }

    public String asString() {
        StringBuilder sb = new StringBuilder();

        sb.append("CallingSequence: {\n");
        sb.append("  MethodType: ").append(mt);
        sb.append("  FunctionDescriptor: ").append(desc);
        sb.append("  Argument Bindings:\n");
        for (int i = 0; i < mt.parameterCount(); i++) {
            sb.append("    ").append(i).append(": ").append(argumentBindings.get(i)).append("\n");
        }
        if (mt.returnType() != void.class) {
            sb.append("    ").append("Return: ").append(returnBindings).append("\n");
        }
        sb.append("}\n");

        return sb.toString();
    }

    public MethodType methodType() {
        return mt;
    }

    public FunctionDescriptor functionDesc() {
        return desc;
    }

    public boolean needsReturnBuffer() {
        return needsReturnBuffer;
    }

    public long returnBufferSize() {
        return returnBufferSize;
    }

    public long allocationSize() {
        return allocationSize;
    }
}
