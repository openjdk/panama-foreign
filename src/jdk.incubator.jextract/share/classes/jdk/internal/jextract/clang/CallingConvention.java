/*
 *  Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.  Oracle designates this
 *  particular file as subject to the "Classpath" exception as provided
 *  by Oracle in the LICENSE file that accompanied this code.
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
 *   Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 *
 */
package jdk.internal.jextract.clang;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public enum CallingConvention {

    Default(0),
    C(1),
    X86StdCall(2),
    X86FastCall(3),
    X86ThisCall(4),
    X86Pascal(5),
    AAPCS(6),
    AAPCS_VFP(7),
    PnaclCall(8),
    IntelOclBicc(9),
    X86_64Win64(10),
    X86_64SysV(11),
    Invalid(100),
    Unexposed(200);

    private final int value;

    CallingConvention(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    private final static Map<Integer, CallingConvention> lookup;

    static {
        lookup = new HashMap<>();
        for (CallingConvention e: CallingConvention.values()) {
            lookup.put(e.value(), e);
        }
    }

    public final static CallingConvention valueOf(int value) {
        CallingConvention x = lookup.get(value);
        if (null == x) {
            throw new NoSuchElementException();
        }
        return x;
    }
}
