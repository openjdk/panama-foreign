/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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
package jdk.internal.foreign;

import jdk.incubator.foreign.MemoryHandles;
import sun.invoke.util.Wrapper;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.util.Objects;

public class UnsignedAdapters {

    private static final MethodHandle INT_TO_BYTE;
    private static final MethodHandle BYTE_TO_UNSIGNED_INT;
    private static final MethodHandle INT_TO_SHORT;
    private static final MethodHandle SHORT_TO_UNSIGNED_INT;
    private static final MethodHandle LONG_TO_BYTE;
    private static final MethodHandle BYTE_TO_UNSIGNED_LONG;
    private static final MethodHandle LONG_TO_SHORT;
    private static final MethodHandle SHORT_TO_UNSIGNED_LONG;
    private static final MethodHandle LONG_TO_INT;
    private static final MethodHandle INT_TO_UNSIGNED_LONG;

    static {
        try {
            INT_TO_BYTE = MethodHandles.explicitCastArguments(MethodHandles.identity(byte.class),
                    MethodType.methodType(byte.class, int.class));
            BYTE_TO_UNSIGNED_INT = MethodHandles.lookup().findStatic(Byte.class, "toUnsignedInt",
                    MethodType.methodType(int.class, byte.class));
            INT_TO_SHORT = MethodHandles.explicitCastArguments(MethodHandles.identity(short.class),
                    MethodType.methodType(short.class, int.class));
            SHORT_TO_UNSIGNED_INT = MethodHandles.lookup().findStatic(Short.class, "toUnsignedInt",
                    MethodType.methodType(int.class, short.class));
            LONG_TO_BYTE = MethodHandles.explicitCastArguments(MethodHandles.identity(byte.class),
                    MethodType.methodType(byte.class, long.class));
            BYTE_TO_UNSIGNED_LONG = MethodHandles.lookup().findStatic(Byte.class, "toUnsignedLong",
                    MethodType.methodType(long.class, byte.class));
            LONG_TO_SHORT = MethodHandles.explicitCastArguments(MethodHandles.identity(short.class),
                    MethodType.methodType(short.class, long.class));
            SHORT_TO_UNSIGNED_LONG = MethodHandles.lookup().findStatic(Short.class, "toUnsignedLong",
                    MethodType.methodType(long.class, short.class));
            LONG_TO_INT = MethodHandles.explicitCastArguments(MethodHandles.identity(int.class),
                    MethodType.methodType(int.class, long.class));
            INT_TO_UNSIGNED_LONG = MethodHandles.lookup().findStatic(Integer.class, "toUnsignedLong",
                    MethodType.methodType(long.class, int.class));
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static VarHandle asUnsigned(VarHandle target, final Class<?> adaptedType) {
        Objects.requireNonNull(target);
        Objects.requireNonNull(adaptedType);
        final Class<?> carrier = target.varType();
        checkWidenable(carrier);
        checkNarrowable(adaptedType);
        checkTargetWiderThanCarrier(carrier, adaptedType);

        if (adaptedType == int.class && carrier == byte.class) {
            return intToUnsignedByte(target);
        } else if (adaptedType == int.class && carrier == short.class) {
            return intToUnsignedShort(target);
        } else if (adaptedType == long.class && carrier == byte.class) {
            return longToUnsignedByte(target);
        } else if (adaptedType == long.class && carrier == short.class) {
            return longToUnsignedShort(target);
        } else if (adaptedType == long.class && carrier == int.class) {
            return longToUnsignedInt(target);
        } else {
            throw new InternalError("should not reach here");
        }
    }

    // int to byte
    private static VarHandle intToUnsignedByte(VarHandle target) {
        if (target.varType() != byte.class)
            throw new InternalError("expected byte carrier, but got: " + target.varType());
        return MemoryHandles.filterValue(target, INT_TO_BYTE, BYTE_TO_UNSIGNED_INT);
    }

    // int to short
    private static VarHandle intToUnsignedShort(VarHandle target) {
        if (target.varType() != short.class)
            throw new InternalError("expected byte carrier, but got: " + target.varType());
        return MemoryHandles.filterValue(target, INT_TO_SHORT, SHORT_TO_UNSIGNED_INT);
    }

    // long to byte
    private static VarHandle longToUnsignedByte(VarHandle target) {
        if (target.varType() != byte.class)
            throw new InternalError("expected byte carrier, but got: " + target.varType());
        return MemoryHandles.filterValue(target, LONG_TO_BYTE, BYTE_TO_UNSIGNED_LONG);
    }

    // long to short
    private static VarHandle longToUnsignedShort(VarHandle target) {
        if (target.varType() != short.class)
            throw new InternalError("expected byte carrier, but got: " + target.varType());
        return MemoryHandles.filterValue(target, LONG_TO_SHORT, SHORT_TO_UNSIGNED_LONG);
    }

    //long to int
    private static VarHandle longToUnsignedInt(VarHandle target) {
        if (target.varType() != int.class)
            throw new InternalError("expected byte carrier, but got: " + target.varType());
        return MemoryHandles.filterValue(target, LONG_TO_INT, INT_TO_UNSIGNED_LONG);
    }

    private static void checkWidenable(Class<?> carrier) {
        if (!(carrier == byte.class || carrier == short.class || carrier == int.class)) {
            throw newIllegalArgumentException("illegal carrier", carrier.getSimpleName());
        }
    }

    private static void checkNarrowable(Class<?> type) {
        if (!(type == int.class || type == long.class)) {
            throw newIllegalArgumentException("illegal adapter type", type.getSimpleName());
        }
    }

    private static void checkTargetWiderThanCarrier(Class<?> carrier, Class<?> target) {
        if (Wrapper.forPrimitiveType(target).bitWidth() <= Wrapper.forPrimitiveType(carrier).bitWidth()) {
            throw newIllegalArgumentException(
                    target.getSimpleName() + " is not wider than: ", carrier.getSimpleName());
        }
    }

    static RuntimeException newIllegalArgumentException(String message, Object obj) {
        return new IllegalArgumentException(message(message, obj));
    }
    private static String message(String message, Object obj) {
        if (obj != null)  message = message + ": " + obj;
        return message;
    }
}
