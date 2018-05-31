/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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

package java.nicl.types;

import jdk.internal.nicl.types.DescriptorParser;
import jdk.internal.nicl.types.Reference;
import jdk.internal.nicl.types.References;

import java.nicl.layout.Address;
import java.nicl.layout.Group;
import java.nicl.layout.Layout;
import java.nicl.layout.Sequence;

import java.lang.invoke.MethodHandle;
import java.nicl.metadata.NativeStruct;

/**
 * This class describes the relationship between a memory layout (usually described in bits) and a Java carrier
 * (e.g. {@code int}, {@code long}, or any Java reference type. A {@code LayoutType} defines operation for getting/setting
 * the layout contents using a given Java carrier (see {@link LayoutType#getter()} and {@link LayoutType#setter()}).
 * Moreover, a {@code LayoutType} defines operation for creating array and pointer derived {@code LayoutType} instances
 * (see {@link LayoutType#array()}, {@link LayoutType#array(int)} and {@link LayoutType#pointer()}).
 */
public class LayoutType<X> {

    private final Reference reference;
    private final Layout layout;

    /* package */ LayoutType(Layout layout, Reference reference) {
        this.reference = reference;
        this.layout = layout;
    }

    public long bytesSize() {
        return layout().bitsSize() / 8;
    }

    /**
     * Retrieves the memory layout associated with this {@code LayoutType}.
     * @return the layout.
     */
    public Layout layout() {
        return layout;
    }

    /**
     * A {@link MethodHandle} which can be used to retrieve the contents of memory layout associated
     * with this {@code LayoutType}. Note: the pointer passed as argument must be compatible with said layout.
     * <p>
     * A getter method handle is of the form:
     * {@code (Pointer) -> T}
     * Where {@code T} is the Java type to which the layout will be converted.
     * </p>
     * @return a 'getter' method handle.
     */
    public MethodHandle getter() {
        return reference.getter();
    }

    /**
     * A {@link MethodHandle} which can be used to store a value into the memory layout associated
     * with this {@code LayoutType}. Note: the pointer passed as argument must be compatible with said layout.
     * <p>
     * A setter method handle is of the form:
     * {@code (Pointer, T) -> V}
     * Where {@code T} is the Java type to which the layout will be converted.
     * </p>
     * the pointer passed as argument.
     * @return a 'setter' method handle.
     */
    public MethodHandle setter() {
        return reference.setter();
    }

    /**
     * Create an array {@code LayoutType} from this instance.
     * @return the array {@code LayoutType}.
     */
    @SuppressWarnings("unchecked")
    public LayoutType<Array<X>> array() {
        return array(0);
    }

    /**
     * Create an array {@code LayoutType} from this instance with given size.
     * @param size the array size.
     * @return the array {@code LayoutType}.
     */
    @SuppressWarnings("unchecked")
    public LayoutType<Array<X>> array(long size) {
        return new LayoutType<>(Sequence.of(size, layout), References.ofArray(this));
    }

    /**
     * Create a pointer {@code LayoutType} from this instance.
     * @return the pointer {@code LayoutType}.
     */
    @SuppressWarnings("unchecked")
    public LayoutType<Pointer<X>> pointer() {
        return new LayoutType<>(Address.ofLayout(64, layout), References.ofPointer(this));
    }

    /**
     * Create a {@code LayoutType} from the {@code boolean} Java primitive carrier and given layout.
     * @param layout the layout.
     * @return the {@code LayoutType}.
     */
    public static LayoutType<Boolean> ofBoolean(Layout layout) {
        return new LayoutType<>(layout, References.ofBoolean);
    }

    /**
     * Create a {@code LayoutType} from the {@code char} Java primitive carrier and given layout.
     * @param layout the layout.
     * @return the {@code LayoutType}.
     */
    public static LayoutType<Character> ofChar(Layout layout) {
        return new LayoutType<>(layout, References.ofChar);
    }

    /**
     * Create a {@code LayoutType} from the {@code byte} Java primitive carrier and given layout.
     * @param layout the layout.
     * @return the {@code LayoutType}.
     */
    public static LayoutType<Byte> ofByte(Layout layout) {
        return new LayoutType<>(layout, References.ofByte);
    }

    /**
     * Create a {@code LayoutType} from the {@code short} Java primitive carrier and given layout.
     * @param layout the layout.
     * @return the {@code LayoutType}.
     */
    public static LayoutType<Short> ofShort(Layout layout) {
        return new LayoutType<>(layout, References.ofShort);
    }

    /**
     * Create a {@code LayoutType} from the {@code int} Java primitive carrier and given layout.
     * @param layout the layout.
     * @return the {@code LayoutType}.
     */
    public static LayoutType<Integer> ofInt(Layout layout) {
        return new LayoutType<>(layout, References.ofInt);
    }

    /**
     * Create a {@code LayoutType} from the {@code float} Java primitive carrier and given layout.
     * @param layout the layout.
     * @return the {@code LayoutType}.
     */
    public static LayoutType<Float> ofFloat(Layout layout) {
        return new LayoutType<>(layout, References.ofFloat);
    }

    /**
     * Create a {@code LayoutType} from the {@code long} Java primitive carrier and given layout.
     * @param layout the layout.
     * @return the {@code LayoutType}.
     */
    public static LayoutType<Long> ofLong(Layout layout) {
        return new LayoutType<>(layout, References.ofLong);
    }

    /**
     * Create a {@code LayoutType} from the {@code double} Java primitive carrier and given layout.
     * @param layout the layout.
     * @return the {@code LayoutType}.
     */
    public static LayoutType<Double> ofDouble(Layout layout) {
        return new LayoutType<>(layout, References.ofDouble);
    }

    /**
     * Create a carrier-less {@code LayoutType} from given layout.
     * @param layout the layout.
     * @return the {@code LayoutType}.
     */
    public static LayoutType<?> ofVoid(Layout layout) {
        return new LayoutType<>(layout, null) {
            @Override
            public MethodHandle getter() throws UnsupportedOperationException {
                throw new UnsupportedOperationException();
            }

            @Override
            public MethodHandle setter() throws UnsupportedOperationException {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Create a {@code LayoutType} from a {@link Struct} interface carrier.
     * @param <T> the struct type.
     * @param carrier the struct carrier.
     * @return the {@code LayoutType}.
     * @throws IllegalArgumentException if the given carrier is not annotated with the {@link java.nicl.metadata.NativeStruct} annotation.
     */
    public static <T extends Struct<T>> LayoutType<T> ofStruct(Class<T> carrier) throws IllegalArgumentException {
        NativeStruct nativeStruct = carrier.getAnnotation(NativeStruct.class);
        if (nativeStruct == null) {
            throw new IllegalArgumentException("Not a struct type!");
        }
        Group type = (Group) new DescriptorParser(nativeStruct.value()).parseLayout();
        return new LayoutType<>(type, References.ofStruct(carrier));
    }
}
