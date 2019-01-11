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


import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.foreign.memory.Pointer;
import java.util.Arrays;

public class Util {
    public enum Function {
        PRINTF, FPRINTF, FDOPEN, FFLUSH;
    }

    public static MethodHandle lookup(Function function) throws NoSuchMethodException, IllegalAccessException {
        switch (function) {
        case PRINTF: return MethodHandles.publicLookup().findVirtual(stdio.class, "printf", MethodType.methodType(int.class, Pointer.class, Object[].class));
        case FPRINTF: return MethodHandles.publicLookup().findVirtual(stdio.class, "fprintf", MethodType.methodType(int.class, Pointer.class, Pointer.class, Object[].class));
        case FFLUSH: return MethodHandles.publicLookup().findVirtual(stdio.class, "fflush", MethodType.methodType(int.class, Pointer.class));
        default: throw new IllegalArgumentException("Unhandled function: " + function);
        }
    }

    public static MethodHandle lookup(Object o, String fname) throws Exception {
        return MethodHandles.publicLookup().unreflect(Arrays.stream(o.getClass().getDeclaredMethods()).filter(m -> m.getName().equals(fname)).findFirst().get());
    }
}
