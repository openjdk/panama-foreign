/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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

package jdk.internal.foreign;

import jdk.internal.foreign.LayoutPaths.LayoutPath;
import jdk.internal.foreign.memory.LayoutTypeImpl;
import jdk.internal.foreign.memory.References;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.foreign.NativeTypes;
import java.foreign.layout.Value;
import java.foreign.memory.LayoutType;
import java.foreign.memory.Pointer;

final class RuntimeSupport {
    private RuntimeSupport() {}

    private static final MethodHandle BITFIELD_GETTER_IMPL;
    private static final MethodHandle BITFIELD_SETTER_IMPL;
    private static final MethodHandle CASTER_IMPL;

    static {
        try {
            BITFIELD_GETTER_IMPL = MethodHandles.lookup().findStatic(RuntimeSupport.class, "bitfieldGetImpl",
                MethodType.methodType(Object.class, long.class, LayoutPath.class, LayoutType.class));
            BITFIELD_SETTER_IMPL = MethodHandles.lookup().findStatic(RuntimeSupport.class, "bitfieldSetImpl",
                MethodType.methodType(long.class, long.class, Object.class, LayoutPath.class, LayoutType.class));
            CASTER_IMPL = MethodHandles.lookup().findStatic(RuntimeSupport.class, "casterImpl",
                    MethodType.methodType(Pointer.class, Pointer.class, LayoutPath.class, LayoutType.class));
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
    }

    // These static utility methods are invoked from generated code

    static MethodHandle getterHandle(LayoutType<?> type, LayoutPath layoutPath) {
        if (layoutPath.enclosing.layout instanceof Value) {
            LayoutType<?> enclosingType = LayoutTypeImpl.of(long.class, layoutPath.enclosing.layout, References.ofLong);
            MethodHandle bfGetter = bitfieldGetterHandle(layoutPath, type);
            MethodHandle containerGetter = getterHandle(enclosingType, layoutPath.enclosing);
            MethodHandle getter = MethodHandles.filterArguments(bfGetter, 0, containerGetter);
            Class<?> expectedReturnType = ((LayoutTypeImpl<?>)type).carrier();
            return getter.asType(getter.type().changeReturnType(expectedReturnType));
        } else {
            MethodHandle caster = casterHandle(layoutPath, type);
            MethodHandle refGetter = type.getter();
            MethodHandle setter = MethodHandles.filterArguments(refGetter, 0, caster);
            return setter;
        }
    }

    public static MethodHandle setterHandle(LayoutType<?> type, LayoutPath layoutPath) {
        if (layoutPath.enclosing.layout instanceof Value) {
            LayoutType<?> enclosingType = LayoutTypeImpl.of(long.class, layoutPath.enclosing.layout, References.ofLong);
            MethodHandle bfSetter = bitfieldSetterHandle(layoutPath, type);
            MethodHandle containerGetter = getterHandle(enclosingType, layoutPath.enclosing);
            MethodHandle updater = MethodHandles.filterArguments(bfSetter, 0, containerGetter);
            MethodHandle containerSetter = setterHandle(enclosingType, layoutPath.enclosing);
            MethodHandle setter = MethodHandles.collectArguments(containerSetter, 1, updater);
            setter = MethodHandles.permuteArguments(setter, setter.type().dropParameterTypes(0, 1), new int[] {0, 0, 1});
            Class<?> expectedArgType = ((LayoutTypeImpl<?>)type).carrier();
            return setter.asType(setter.type().changeParameterType(1, expectedArgType));
        } else {
            MethodHandle caster = casterHandle(layoutPath, type);
            MethodHandle refSetter = type.setter();
            MethodHandle setter = MethodHandles.filterArguments(refSetter, 0, caster);
            return setter;
        }
    }

    public static MethodHandle casterHandle(LayoutPath path, LayoutType<?> type) {
        return MethodHandles.insertArguments(CASTER_IMPL, 1, path, type);
    }

    public static MethodHandle bitfieldGetterHandle(LayoutPath layoutPath, LayoutType<?> type) {
        return MethodHandles.insertArguments(BITFIELD_GETTER_IMPL, 1, layoutPath, type);
    }

    public static MethodHandle bitfieldSetterHandle(LayoutPath layoutPath, LayoutType<?> type) {
        return MethodHandles.insertArguments(BITFIELD_SETTER_IMPL, 2, layoutPath, type);
    }

    private static Pointer<?> casterImpl(Pointer<?> ptr, LayoutPath path, LayoutType<?> type) {
        return Util.unsafeCast(
            Util.unsafeCast(ptr, NativeTypes.UINT8).offset(path.offset() / 8),
            type);
    }

    private static Object bitfieldGetImpl(long bits, LayoutPath layoutPath, LayoutType<?> type) {
        long offsetInBitfield = layoutPath.offset() - layoutPath.enclosing.offset();
        bits = bits >> offsetInBitfield;
        bits = bits & mask(layoutPath.layout.bitsSize());
        Class<?> carrier = ((LayoutTypeImpl<?>)type).carrier();
        if (carrier == boolean.class) {
            return Boolean.valueOf((byte)bits != 0);
        } else if (carrier == byte.class) {
            return Byte.valueOf((byte)bits);
        } else if (carrier == char.class) {
            return Character.valueOf((char)bits);
        } else if (carrier == short.class) {
            return Short.valueOf((short)bits);
        } else if (carrier == int.class) {
            return Integer.valueOf((int)bits);
        } else if (carrier == long.class) {
            return Long.valueOf(bits);
        } else {
            throw new IllegalStateException();
        }
    }

    private static long bitfieldSetImpl(long bits, Object value, LayoutPath layoutPath, LayoutType<?> type) {
        long offsetInBitfield = layoutPath.offset() - layoutPath.enclosing.offset();
        long mask = mask(layoutPath.layout.bitsSize()) << offsetInBitfield;
        bits &= ~mask; //all bits in the slice are now zeroed
        long newBits;
        Class<?> carrier = ((LayoutTypeImpl<?>)type).carrier();
        if (carrier == boolean.class) {
            newBits = (boolean) value ? 1 : 0;
        } else if (carrier == byte.class) {
            newBits = (byte) value;
        } else if (carrier == char.class) {
            newBits = (char) value;
        } else if (carrier == short.class) {
            newBits = (short) value;
        } else if (carrier == int.class) {
            newBits = (int) value;
        } else if (carrier == long.class) {
            newBits = (long) value;
        } else {
            throw new IllegalStateException();
        }
        bits |= ((newBits << offsetInBitfield) & mask);
        return bits;
    }

    private static long mask(long size) {
        return size == 64 ? -1L : (1L << size) - 1;
    }
}
