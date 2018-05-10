/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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
package jdk.internal.nicl.types;

import jdk.internal.nicl.Util;

import java.lang.reflect.ParameterizedType;
import java.nicl.layout.Address;
import java.nicl.layout.Layout;
import java.nicl.types.LayoutType;
import java.nicl.types.Pointer;
import java.util.Arrays;
import java.util.Objects;
import static sun.security.action.GetPropertyAction.privilegedGetProperty;

public class LayoutTypeImpl<T> implements LayoutType<T> {
    private static final boolean QUIET = Boolean.parseBoolean(
        privilegedGetProperty("LayoutTypeImpl.QUIET"));

    private final java.lang.reflect.Type carrierType;
    private final Layout type;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <S> LayoutTypeImpl<S> create(Class<S> c) {
        if (c.isPrimitive()) {
            switch (jdk.internal.org.objectweb.asm.Type.getDescriptor(c)) {
            case "V":
                return (LayoutTypeImpl<S>) new LayoutTypeImpl<Void>(void.class, Types.VOID);

            case "Z":
                return (LayoutTypeImpl<S>) new LayoutTypeImpl<Boolean>(boolean.class, Types.BOOLEAN);

            case "B":
                return (LayoutTypeImpl<S>) new LayoutTypeImpl<Byte>(byte.class, Types.CHAR);

            case "S":
                return (LayoutTypeImpl<S>) new LayoutTypeImpl<Short>(short.class, Types.SHORT);

            case "C":
                return (LayoutTypeImpl<S>) new LayoutTypeImpl<Character>(char.class, Types.UNSIGNED.SHORT);

            case "I":
                return (LayoutTypeImpl<S>) new LayoutTypeImpl<Integer>(int.class, Types.INT32);

            case "J":
                return (LayoutTypeImpl<S>) new LayoutTypeImpl<Long>(long.class, Types.INT64);

            case "F":
                return (LayoutTypeImpl<S>) new LayoutTypeImpl<Float>(float.class, Types.FLOAT);

            case "D":
                return (LayoutTypeImpl<S>) new LayoutTypeImpl<Double>(double.class, Types.DOUBLE);

            default:
                throw new UnsupportedOperationException("NYI: " + c.getName());
            }
        } else if (Pointer.class.isAssignableFrom(c)) {
            if (!QUIET) {
                new IllegalArgumentException("WARNING: Making raw pointer type - please use LayoutType.ptrType() instead").printStackTrace();
            }
            return (LayoutTypeImpl<S>) create(void.class).ptrType();
        } else if (Util.isCStruct(c)) {
            return new LayoutTypeImpl<>(c, Util.typeof(c));
        } else if (Util.isFunctionalInterface(c)) {
            return new LayoutTypeImpl<>(c, Address.ofFunction(64, Util.functionof(c)));
        } else {
            switch (c.getName()) {
            case "java.lang.Object": // FIXME: what does this really mean?
            case "java.lang.Void":
                return (LayoutTypeImpl<S>) new LayoutTypeImpl<Void>(void.class, Types.VOID);

            case "java.lang.Boolean":
                return (LayoutTypeImpl<S>) new LayoutTypeImpl<Boolean>(boolean.class, Types.BOOLEAN);

            case "java.lang.Byte":
                return (LayoutTypeImpl<S>) new LayoutTypeImpl<Byte>(byte.class, Types.CHAR);

            case "java.lang.Short":
                return (LayoutTypeImpl<S>) new LayoutTypeImpl<Short>(short.class, Types.SHORT);

            case "java.lang.Character":
                return (LayoutTypeImpl<S>) new LayoutTypeImpl<Character>(char.class, Types.UNSIGNED.SHORT);

            case "java.lang.Integer":
                return (LayoutTypeImpl<S>) new LayoutTypeImpl<Integer>(int.class, Types.INT32);

            case "java.lang.Long":
                return (LayoutTypeImpl<S>) new LayoutTypeImpl<Long>(long.class, Types.INT64);

            case "java.lang.Float":
                return (LayoutTypeImpl<S>) new LayoutTypeImpl<Float>(float.class, Types.FLOAT);

            case "java.lang.Double":
                return (LayoutTypeImpl<S>) new LayoutTypeImpl<Double>(double.class, Types.DOUBLE);

            default:
                throw new Error("Unhandled type: " + c);
            }
        }
    }

    public LayoutTypeImpl(java.lang.reflect.Type carrierType, Layout type) {
        this.carrierType = carrierType;
        this.type = type;
    }

    public LayoutType<?> getInnerType() {
        if (!(carrierType instanceof ParameterizedType)) {
            return null;
        }

        ParameterizedType pt = ((ParameterizedType) carrierType);

        if (pt.getRawType() != Pointer.class) {
            throw new IllegalArgumentException("Unexpected type: " + pt.getRawType());
        }

        @SuppressWarnings("rawtypes")
        LayoutTypeImpl<?> lt = new LayoutTypeImpl(pt.getActualTypeArguments()[0], ((Address)type).addresseeInfo().get().layout());

        return lt;
    }

    @SuppressWarnings("unchecked")
    public Class<T> getCarrierType() {
        if (carrierType instanceof ParameterizedType) {
            return (Class<T>)((ParameterizedType)carrierType).getRawType();
        }

        return (Class<T>)carrierType;
    }

    @Override
    public String getTypeDescriptor() {
        return type.toString();
    }

    @Override
    public long getNativeTypeSize() {
        return type.bitsSize() / 8;
    }

    public Layout getType() {
        return type;
    }

    @Override
    public Layout getLayout() {
        return getType();
    }

    @SuppressWarnings("unchecked")
    public LayoutType<Pointer<T>> ptrType() {
        return new LayoutTypeImpl<>(new PointerType(carrierType), Address.ofLayout(64, type));
    }

    public static class PointerType implements ParameterizedType {
        private final java.lang.reflect.Type pointeeType;

        public PointerType(java.lang.reflect.Type pointeeType) {
            this.pointeeType = pointeeType;
        }

        @Override
        public java.lang.reflect.Type[] getActualTypeArguments() {
            return new java.lang.reflect.Type[] { pointeeType };
        }

        @Override
        public java.lang.reflect.Type getOwnerType() {
            return null;
        }

        @Override
        public java.lang.reflect.Type getRawType() {
            return Pointer.class;
        }

        @Override
        public int hashCode() {
            return
                Arrays.hashCode(getActualTypeArguments()) ^
                Objects.hashCode(getOwnerType()) ^
                Objects.hashCode(getRawType());
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof ParameterizedType) {
                ParameterizedType that = (ParameterizedType) o;

                if (this == that)
                    return true;

                java.lang.reflect.Type thatOwner   = that.getOwnerType();
                java.lang.reflect.Type thatRawType = that.getRawType();

                return
                    Objects.equals(getOwnerType(), thatOwner) &&
                    Objects.equals(getRawType(), thatRawType) &&
                    Arrays.equals(getActualTypeArguments(), // avoid clone
                                  that.getActualTypeArguments());
            } else
                return false;
        }

        @Override
        public String toString() {
            return "{ PointerType pointeeType=" + pointeeType + " }";
        }
    }

    @Override
    public String toString() {
        return "{ LayoutTypeImpl carrierType=" + carrierType + " type=" + type + " }";
    }
}
