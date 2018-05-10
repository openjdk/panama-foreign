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

import jdk.internal.nicl.Platform;

import java.nicl.layout.Function;
import java.nicl.layout.Layout;
import java.nicl.layout.Value;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.stream.Stream;
import java.lang.invoke.MethodType;

public class BindingRegistry {
    @FunctionalInterface
    public interface N2J<T> {
        T convert(BoundedMemoryRegion r);

        default <S> N2J<S> combine(java.util.function.Function<T, S> fn) {
            return r -> fn.apply(convert(r));
        }
    }

    @FunctionalInterface
    public interface J2N<T> {
        void convert(T o, BoundedMemoryRegion r);

        default <S> J2N<S> combine(java.util.function.Function<S, T> fn) {
            return (o, r) -> convert(fn.apply(o), r);
        }
    }

    public static final <T> boolean register(String nativeDescriptor, Class<T> carrierClass,
            N2J<T> native2java, J2N<T> java2native) {
        Layout nt = new DescriptorParser(nativeDescriptor).parseLayout().findFirst().get();
        BindingRegistry registry = BindingRegistry.getInstance();
        return registry.register(nt, carrierClass, native2java, java2native);
    }

    private final class NativeBinding<T> {
        final Layout nativeType;
        final Class<T> carrierClass;
        final N2J<T> native2java;
        final J2N<T> java2native;

        NativeBinding(Layout nativeType,
                Class<T> carrierClass, N2J<T> n2j, J2N<T> j2n) {
            this.nativeType = nativeType;
            this.carrierClass = carrierClass;
            this.native2java = n2j;
            this.java2native = j2n;
        }
    }

    private final Map<Layout, LinkedHashMap<java.lang.reflect.Type, NativeBinding<?>>> registry;

    private BindingRegistry() {
        registry = new HashMap<>();
    }

    private void initLE() {
        register(Types.LE.SHORT, short.class, mr -> mr.getShort(0), (v, mr) -> mr.putShort(0, v));
        register(Types.LE.INT, int.class, mr -> mr.getInt(0), (v, mr) -> mr.putInt(0, v));
        register(Types.LE.LONG, long.class, mr -> mr.getLong(0), (v, mr) -> mr.putLong(0, v));
        register(Types.BE.SHORT, short.class,
                mr -> Short.reverseBytes(mr.getShort(0)),
                (v, mr) -> mr.putShort(0, Short.reverseBytes(v)));
        register(Types.BE.INT, int.class,
                mr -> Integer.reverseBytes(mr.getInt(0)),
                (v, mr) -> mr.putInt(0, Integer.reverseBytes(v)));
        register(Types.BE.LONG, long.class,
                mr -> Long.reverseBytes(mr.getLong(0)),
                (v, mr) -> mr.putLong(0, Long.reverseBytes(v)));
        register(Types.LE.INT16, short.class, mr -> mr.getShort(0), (v, mr) -> mr.putShort(0, v));
        register(Types.LE.INT32, int.class, mr -> mr.getInt(0), (v, mr) -> mr.putInt(0, v));
        register(Types.LE.INT64, long.class, mr -> mr.getLong(0), (v, mr) -> mr.putLong(0, v));
        register(Types.BE.INT16, short.class,
                mr -> Short.reverseBytes(mr.getShort(0)),
                (v, mr) -> mr.putShort(0, Short.reverseBytes(v)));
        register(Types.BE.INT32, int.class,
                mr -> Integer.reverseBytes(mr.getInt(0)),
                (v, mr) -> mr.putInt(0, Integer.reverseBytes(v)));
        register(Types.BE.INT64, long.class,
                mr -> Long.reverseBytes(mr.getLong(0)),
                (v, mr) -> mr.putLong(0, Long.reverseBytes(v)));
    }

    private void initBE() {
        register(Types.BE.SHORT, short.class, mr -> mr.getShort(0), (v, mr) -> mr.putShort(0, v));
        register(Types.BE.INT, int.class, mr -> mr.getInt(0), (v, mr) -> mr.putInt(0, v));
        register(Types.BE.LONG, long.class, mr -> mr.getLong(0), (v, mr) -> mr.putLong(0, v));
        register(Types.LE.SHORT, short.class,
                mr -> Short.reverseBytes(mr.getShort(0)),
                (v, mr) -> mr.putShort(0, Short.reverseBytes(v)));
        register(Types.LE.INT, int.class,
                mr -> Integer.reverseBytes(mr.getInt(0)),
                (v, mr) -> mr.putInt(0, Integer.reverseBytes(v)));
        register(Types.LE.LONG, long.class,
                mr -> Long.reverseBytes(mr.getLong(0)),
                (v, mr) -> mr.putLong(0, Long.reverseBytes(v)));
        register(Types.BE.INT16, short.class, mr -> mr.getShort(0), (v, mr) -> mr.putShort(0, v));
        register(Types.BE.INT32, int.class, mr -> mr.getInt(0), (v, mr) -> mr.putInt(0, v));
        register(Types.BE.INT64, long.class, mr -> mr.getLong(0), (v, mr) -> mr.putLong(0, v));
        register(Types.LE.INT16, short.class,
                mr -> Short.reverseBytes(mr.getShort(0)),
                (v, mr) -> mr.putShort(0, Short.reverseBytes(v)));
        register(Types.LE.INT32, int.class,
                mr -> Integer.reverseBytes(mr.getInt(0)),
                (v, mr) -> mr.putInt(0, Integer.reverseBytes(v)));
        register(Types.LE.INT64, long.class,
                mr -> Long.reverseBytes(mr.getLong(0)),
                (v, mr) -> mr.putLong(0, Long.reverseBytes(v)));
    }

    private void init() {
        register(Types.BYTE, byte.class, mr -> mr.getByte(0), (v, mr) -> mr.putByte(0, v));
        register(Types.SHORT, short.class, mr -> mr.getShort(0), (v, mr) -> mr.putShort(0, v));
        register(Types.INT, int.class, mr -> mr.getInt(0), (v, mr) -> mr.putInt(0, v));
        register(Types.LONG, long.class, mr -> mr.getLong(0), (v, mr) -> mr.putLong(0, v));
        register(Types.INT8, byte.class, mr -> mr.getByte(0), (v, mr) -> mr.putByte(0, v));
        register(Types.INT16, short.class, mr -> mr.getShort(0), (v, mr) -> mr.putShort(0, v));
        register(Types.INT32, int.class, mr -> mr.getInt(0), (v, mr) -> mr.putInt(0, v));
        register(Types.INT64, long.class, mr -> mr.getLong(0), (v, mr) -> mr.putLong(0, v));

        if (Platform.getInstance().defaultEndianness() == Value.Endianness.LITTLE_ENDIAN) {
            initLE();
        } else {
            assert (Platform.getInstance().defaultEndianness() == Value.Endianness.BIG_ENDIAN);
            initBE();
        }
    }

    public static final BindingRegistry theOne;

    static {
        theOne = new BindingRegistry();
        theOne.init();
    }

    public static BindingRegistry getInstance() {
        return theOne;
    }

    public final <T> boolean register(final Layout nativeType,
            final Class<T> carrierClass, final N2J<T> native2java, final J2N<T> java2native) {
        LinkedHashMap<java.lang.reflect.Type, NativeBinding<?>> carriers =
            registry.computeIfAbsent(nativeType, nt -> new LinkedHashMap<>());
        return null == carriers.computeIfAbsent(carrierClass, ct ->
            new NativeBinding<T>(nativeType, carrierClass, native2java, java2native));
    }

    public Stream<java.lang.reflect.Type> getCarrierTypes(Layout nativeType) {
        Map<java.lang.reflect.Type, NativeBinding<?>> carriers = registry.get(nativeType);
        if (carriers == null) {
            return Stream.empty();
        }
        return carriers.keySet().stream();
    }

    public java.lang.reflect.Type getCarrierType(Layout nativeType) {
        return getCarrierTypes(nativeType).findFirst().get();
    }

    public MethodType defaultMethodType(Function fn) {
        // TODO: implement
        return null;
    }
}
