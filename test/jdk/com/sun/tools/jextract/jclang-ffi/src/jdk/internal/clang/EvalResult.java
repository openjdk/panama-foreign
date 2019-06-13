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

package jdk.internal.clang;

import java.foreign.memory.Pointer;

public class EvalResult implements AutoCloseable {
    private final Pointer<?> ptr;

    public EvalResult(Pointer<?> ptr) {
        this.ptr = ptr;
    }

    public enum Kind {
        Integral,
        FloatingPoint,
        StrLiteral,
        Erroneous,
        Unknown
    }

    private int getKind0() {
        return LibClang.lib.clang_EvalResult_getKind(ptr);
    }

    public Kind getKind() {
        int code = getKind0();
        switch (code) {
            case 1: return Kind.Integral;
            case 2: return Kind.FloatingPoint;
            case 3: case 4: case 5:
                return Kind.StrLiteral;
            default:
                return Kind.Unknown;
        }
    }

    private long getAsInt0() {
        return LibClang.lib.clang_EvalResult_getAsLongLong(ptr);
    }

    public long getAsInt() {
        Kind kind = getKind();
        switch (kind) {
            case Integral:
                return getAsInt0();
            default:
                throw new IllegalStateException("Unexpected kind: " + kind);
        }
    }

    private double getAsFloat0() {
        return LibClang.lib.clang_EvalResult_getAsDouble(ptr);
    }

    public double getAsFloat() {
        Kind kind = getKind();
        switch (kind) {
            case FloatingPoint:
                return getAsFloat0();
            default:
                throw new IllegalStateException("Unexpected kind: " + kind);
        }
    }

    private String getAsString0() {
        Pointer<Byte> value = LibClang.lib.clang_EvalResult_getAsStr(ptr);
        return Pointer.toString(value);
    }

    public String getAsString() {
        Kind kind = getKind();
        switch (kind) {
            case StrLiteral:
                return getAsString0();
            default:
                throw new IllegalStateException("Unexpected kind: " + kind);
        }
    }

    @Override
    public void close() {
        if (!ptr.isNull()) {
            LibClang.lib.clang_EvalResult_dispose(ptr);
        }
    }

    final static EvalResult erroneous = new EvalResult(Pointer.ofNull()) {
        @Override
        public Kind getKind() {
            return Kind.Erroneous;
        }

        @Override
        public void close() {
            //do nothing
        }
    };
}
