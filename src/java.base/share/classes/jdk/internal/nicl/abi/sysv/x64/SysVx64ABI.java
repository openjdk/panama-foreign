/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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
package jdk.internal.nicl.abi.sysv.x64;

import jdk.internal.nicl.types.Type;
import jdk.internal.nicl.types.Types;
import jdk.internal.nicl.types.Function;
import jdk.internal.nicl.abi.AbstractABI;
import jdk.internal.nicl.abi.CallingSequence;
import jdk.internal.nicl.abi.CallingSequenceBuilder;

/**
 * ABI implementation based on System V ABI AMD64 supplement v.0.99.6
 */
public class SysVx64ABI extends AbstractABI {
    @Override
    public long definedSize(char typeCode) {
        switch (typeCode) {
            case 'c':
            case 'o':
            case 'O':
            case 'x':
            case 'B':
                return 1;
            case 's':
            case 'S':
                return 2;
            case 'i':
            case 'I':
                return 4;
            case 'l':
            case 'L':
            case 'q':
            case 'Q':
                return 8;
            case 'f':
            case 'F':
                return 4;
            case 'd':
            case 'D':
                return 8;
            case 'e':
            case 'E':
                return 16;
            case 'p':
                return 8;
            case 'V':
                return 0;
            default:
                throw new IllegalArgumentException("Invalid type descriptor " + typeCode);
        }
    }

    @Override
    public CallingSequence arrangeCall(Function f) {
        CallingSequenceBuilder builder = new CallingSequenceBuilder(CallingSequenceBuilderImpl.class);
        for (int i = 0; i < f.argumentCount(); i++) {
            Type type = f.getArgumentType(i);
            builder.addArgument(type, "arg" + i);
        }
        if (f.isVarArg()) {
            builder.addArgument(Types.POINTER, null);
        }

        if (! f.getReturnType().equals(Types.VOID)) {
            builder.setReturnType(f.getReturnType());
        }

        return builder.build();
    }
}

