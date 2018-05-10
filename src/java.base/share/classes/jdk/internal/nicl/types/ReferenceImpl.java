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

import jdk.internal.nicl.UpcallHandler;
import jdk.internal.nicl.Util;

import java.nicl.layout.Layout;
import java.nicl.layout.Value;
import java.nicl.types.*;
import java.nicl.types.Pointer;

class ReferenceImpl<T> implements Reference<T> {
    private final BoundedMemoryRegion region;
    private final long offset;

    private final LayoutType<T> type;
    private final int mode;

    ReferenceImpl(LayoutType<T> type, BoundedMemoryRegion region, long offset) {
        this(type, region, offset, BoundedMemoryRegion.MODE_RW);
    }

    ReferenceImpl(LayoutType<T> type, BoundedMemoryRegion region, long offset, int mode) {
        this.region = region;
        this.offset = offset;
        this.type = type;
        this.mode = mode;
    }

    public Pointer<T> ptr() {
        return new BoundedPointer<>(type, region, offset, mode);
    }

    private long getLongBits() throws IllegalAccessException {
        Layout t = type.getLayout();

        boolean isSigned = true;
        if (t instanceof Value) {
            isSigned = ((Value)t).kind() != Value.Kind.INTEGRAL_UNSIGNED;
        }
        return region.getBits(offset, type.getNativeTypeSize(), isSigned);
    }

    private void putLongBits(long value) throws IllegalAccessException {
        region.putBits(offset, type.getNativeTypeSize(), value);
    }

    @Override
    public void checkAccess(int mode) {
        if ((this.mode & mode) == 0) {
            throw new RuntimeException("Access denied");
        }
    }

    public T get() {
        try {
            return getInner();
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private T getInner() throws IllegalAccessException {
        checkAccess(BoundedMemoryRegion.MODE_R);

        Class<?> c = type.getCarrierType();
        if (c.isPrimitive()) {
            switch (jdk.internal.org.objectweb.asm.Type.getDescriptor(c)) {
            case "Z":
                return (T) Boolean.valueOf((byte)getLongBits() != 0);
            case "B":
                return (T) Byte.valueOf((byte)getLongBits());
            case "C":
                return (T) Character.valueOf((char)getLongBits());
            case "S":
                return (T) Short.valueOf((short)getLongBits());
            case "I":
                return (T) Integer.valueOf((int)getLongBits());
            case "J":
                return (T) Long.valueOf(getLongBits());
            case "F":
                return (T) Float.valueOf(Float.intBitsToFloat((int)getLongBits()));
            case "D":
                return (T) Double.valueOf(Double.longBitsToDouble(getLongBits()));
            case "V":
                throw new RuntimeException("void type");
            default:
                throw new UnsupportedOperationException("Unhandled type: " + c.getName());
            }
        } else if (Pointer.class.isAssignableFrom(c)) {
            if (type.getInnerType() == null) {
                throw new IllegalArgumentException("Inner type unexpectedly null: " + this);
            }
            return (T) Util.createPtr(getLongBits(), (LayoutType)type.getInnerType());
        } else if (Util.isCStruct(c)) {
            return ptr().deref();
        } else {
            throw new UnsupportedOperationException("Unhandled type: " + c);
        }
    }

    public void set(T value) {
        try {
            setInner(value);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void setInner(T value) throws IllegalAccessException {
        checkAccess(BoundedMemoryRegion.MODE_W);

        Class<?> c = type.getCarrierType();
        if (c.isPrimitive()) {
            switch (jdk.internal.org.objectweb.asm.Type.getDescriptor(c)) {
            case "Z":
                putLongBits((boolean)value ? 1 : 0);
                break;
            case "B":
                putLongBits((byte)value);
                break;
            case "C":
                putLongBits((char)value);
                break;
            case "S":
                putLongBits((short)value);
                break;
            case "I":
                putLongBits((int)value);
                break;
            case "J":
                putLongBits((long)value);
                break;
            case "F":
                putLongBits(Float.floatToRawIntBits((float)value));
                break;
            case "D":
                putLongBits(Double.doubleToRawLongBits((double)value));
                break;
            case "V":
                throw new RuntimeException("void type");
            default:
                throw new UnsupportedOperationException("Unhandled type: " + c.getName());
            }
        } else if (Pointer.class.isAssignableFrom(c)) {
            Pointer<?> ptr = (Pointer<?>) value;
            try {
                region.putAddress(offset, Util.unpack(ptr));
            } catch (IllegalAccessException iae) {
                throw new RuntimeException("Access denied", iae);
            }
        } else if (Util.isCStruct(c)) {
            // FIXME: This may not be the right thing to do...
            Reference<?> r = (Reference)value;
            Util.copy(r.ptr(), ptr(), Util.sizeof(c));
        } else if (Util.isFunctionalInterface(c)) {
            long codePtr;
            try {
                if (value == null) {
                    codePtr = 0;
                } else {
                    codePtr = UpcallHandler.make(c, value).getNativeEntryPoint().addr();
                }
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
            region.putAddress(offset, codePtr);
        } else {
            throw new UnsupportedOperationException("Unhandled type: " + type.getCarrierType());
        }
    }

    public String toString() {
        return "{ ReferenceImpl type=" + type + " }";
    }
}
