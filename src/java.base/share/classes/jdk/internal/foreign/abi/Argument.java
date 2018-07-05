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

import java.foreign.layout.Layout;

public class Argument {
    private final int argumentIndex; // index of argument (in argument list)
    private final Layout type;

    // for testing/debugging, also serves as indicator that argument is named (as opposed to elipsis/varargs arg)
    private final String name;

    public Argument(int argumentIndex, Layout type, String name) {
        this.argumentIndex = argumentIndex;
        this.type = type;
        this.name = name;
    }

    public Argument(int index, Layout type) {
        this(index, type, null);
    }

    public int getArgumentIndex() {
        return argumentIndex;
    }

    public Layout getType() {
        return type;
    }

    public String getName() {
        return name != null ? name : "<anonymous>";
    }

    public boolean isNamed() {
        return name != null;
    }

    @Override
    public String toString() {
        return "[" + type.toString() + " " + getName() + "]";
    }
}
