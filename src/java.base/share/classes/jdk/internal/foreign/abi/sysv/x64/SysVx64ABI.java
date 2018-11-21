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
package jdk.internal.foreign.abi.sysv.x64;

import java.foreign.layout.Function;
import java.foreign.layout.Layout;
import java.foreign.layout.Value;

import jdk.internal.foreign.abi.AbstractABI;
import jdk.internal.foreign.abi.CallingSequence;
import jdk.internal.foreign.abi.CallingSequenceBuilder;
import jdk.internal.foreign.memory.Types;

/**
 * ABI implementation based on System V ABI AMD64 supplement v.0.99.6
 */
public class SysVx64ABI extends AbstractABI {

    private static SysVx64ABI instance;

    public static SysVx64ABI getInstance() {
        if (instance == null) {
            instance = new SysVx64ABI();
        }
        return instance;
    }

    @Override
    public CallingSequence arrangeCall(Function f) {
        CallingSequenceBuilder builder = new CallingSequenceBuilder(CallingSequenceBuilderImpl.class);
        for (int i = 0; i < f.argumentLayouts().size(); i++) {
            Layout type = f.argumentLayouts().get(i);
            builder.addArgument(type, "arg" + i);
        }
        if (f.isVariadic()) {
            builder.addArgument(Types.POINTER, null);
        }

        if (f.returnLayout().isPresent()) {
            builder.setReturnType(f.returnLayout().get());
        }

        return builder.build();
    }

    @Override
    public Layout layoutFor(CType type) {
        switch (type) {
            case Char:
            case SignedChar:
                return Value.ofSignedInt(8);
            case Bool:
            case UnsignedChar:
                return Value.ofUnsignedInt(8);
            case Short:
                return Value.ofSignedInt(16);
            case UnsignedShort:
                return Value.ofUnsignedInt(16);
            case Int:
                return Value.ofSignedInt(32);
            case UnsignedInt:
                return Value.ofUnsignedInt(32);
            case Long:
            case LongLong:
                return Value.ofSignedInt(64);
            case UnsignedLong:
            case UnsignedLongLong:
                return Value.ofUnsignedInt(64);
            case Float:
                return Value.ofFloatingPoint(32);
            case Double:
                return Value.ofFloatingPoint(64);
            case LongDouble:
                return Value.ofFloatingPoint(128);
            case Pointer:
                return Value.ofUnsignedInt(64);
            default:
                throw new IllegalArgumentException("Unknown layout " + type);

        }
    }
}

