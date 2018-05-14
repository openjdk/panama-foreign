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

import java.nicl.layout.Function;
import java.nicl.layout.Layout;
import java.nicl.layout.Value;

import jdk.internal.nicl.abi.AbstractABI;
import jdk.internal.nicl.abi.CallingSequence;
import jdk.internal.nicl.abi.CallingSequenceBuilder;
import jdk.internal.nicl.types.Types;

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
    public long definedSize(Value s) {
        return s.bitsSize() / 8;
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
}

