/*
 *  Copyright (c) 2023, Oracle and/or its affiliates. All rights reserved.
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

package jdk.internal.foreign;

import jdk.internal.util.ArraysSupport;
import jdk.internal.vm.annotation.Stable;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.PaddingLayout;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class LayoutRecordMapper<T extends Record>
        implements Function<MemorySegment, T> {

    // Related on constructing : https://github.com/openjdk/jdk/pull/13853/files

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private final Class<T> type;
    private final GroupLayout layout;
    @Stable
    private final LayoutRecordMapper.MethodHandleAndOffset[] handles;
    private final Constructor<T> canonicalConstructor;

    public LayoutRecordMapper(Class<T> type,
                              GroupLayout layout) {
        this(type, layout, 0);
    }

    @SuppressWarnings("unchecked")
    public LayoutRecordMapper(Class<T> type,
                              GroupLayout layout,
                              long offset) {
        this.type = type;
        this.layout = layout;

        // Todo: Compose a single MethodHandle(MemorySegment, long) that returns T, to use as a getter

        Map<String, RecordComponent> components = Stream.of(type.getRecordComponents())
                .collect(toLinkedHashMap(RecordComponent::getName, Function.identity()));

        if (components.isEmpty()) {
            throw new IllegalArgumentException("The provided Record type did not contain any components.");
        }

        Map<String, MemoryLayout> layouts = layout.memberLayouts().stream()
                .collect(toLinkedHashMap(l -> l.name().orElseThrow(), Function.identity()));

        var missingComponents = components.keySet().stream()
                .filter(l -> !layouts.containsKey(l))
                .toList();

        if (!missingComponents.isEmpty()) {
            throw new IllegalArgumentException("There is no mapping for " +
                    missingComponents + " in " + type.getName() +
                    "(" + String.join(", ", components.keySet()) + ")" +
                    " provided by the layout " + layout);
        }

        Map<String, ComponentAndLayout> componentLayoutMap = components.entrySet().stream()
                .map(e -> new ComponentAndLayout(e.getValue(), layouts.get(e.getKey())))
                .collect(toLinkedHashMap(cl -> cl.component().getName(), Function.identity()));

        Class<?>[] ctorParameterTypes = components.values().stream()
                .map(RecordComponent::getType)
                .toArray(Class<?>[]::new);

        try {
            canonicalConstructor = type.getDeclaredConstructor(ctorParameterTypes);

            this.handles = componentLayoutMap.values().stream()
                    .map(cl -> {
                        var name = cl.layout().name().orElseThrow();
                        var pathElement = MemoryLayout.PathElement.groupElement(name);
                        long byteOffset = layout.byteOffset(pathElement) + offset;

                        return switch (cl.layout()) {
                            case ValueLayout vl -> {
                                try {
                                    assertExactMatch(cl, type, vl, layout);
                                    MethodHandle mh = LOOKUP.unreflect(
                                            MemorySegment.class.getMethod("get", valueLayoutType(vl), long.class));
                                    // (MemorySegment, OfX, long ) -> (MemorySegment, long)
                                    yield new MethodHandleAndOffset(MethodHandles.insertArguments(mh, 1, vl), byteOffset);
                                } catch (NoSuchMethodException | IllegalAccessException e) {
                                    throw new InternalError(e);
                                }
                            }
                            case GroupLayout gl -> {
                                var componentType = (Class<T>) cl.component().getType();
                                var componentMapper = recordMapper(componentType, gl, byteOffset);
                                try {
                                    var mh = LOOKUP.unreflect(
                                            LayoutRecordMapper.class.getDeclaredMethod("get", MemorySegment.class, long.class));

                                    // (LayoutRecordAccessor, MemorySegment, long) -> (MemorySegment, long)
                                    yield new MethodHandleAndOffset(MethodHandles.insertArguments(mh, 0, componentMapper), byteOffset);
                                } catch (NoSuchMethodException | IllegalAccessException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            case SequenceLayout sl -> {
                                try {
                                    long count = sl.elementCount();
                                    if (count > ArraysSupport.SOFT_MAX_ARRAY_LENGTH) {
                                        throw new IllegalArgumentException("Unable to accommodate " + sl + " in an array.");
                                    }
                                    switch (sl.elementLayout()) {
                                        case ValueLayout vl -> {
                                            assertExactMatch(cl, type, vl, layout);

                                            Method method = LayoutRecordMapper.class.getDeclaredMethod("toArray", MemorySegment.class, valueLayoutType(vl), long.class, long.class);
                                            // Declared as package private
                                            method.setAccessible(true);
                                            MethodHandle mh = LOOKUP.unreflect(method);
                                            // (MemorySegment, OfX, long offset, long count) -> (MemorySegment, OfX, long offset)
                                            MethodHandle mh2 = MethodHandles.insertArguments(mh, 3, count);
                                            // (MemorySegment, OfX, long offset) -> (MemorySegment, long offset)
                                            yield new MethodHandleAndOffset(MethodHandles.insertArguments(mh2, 1, vl), byteOffset);
                                        }
                                        case GroupLayout gl -> {
                                            var arrayComponentType = (Class<T>) cl.component().getType().componentType();
                                            // The "local" byteOffset for the record component mapper is zero
                                            var componentMapper = recordMapper(arrayComponentType, gl, 0);
                                            try {
                                                var mh = LOOKUP.unreflect(
                                                        LayoutRecordMapper.class.getDeclaredMethod("toArray", MemorySegment.class, GroupLayout.class, long.class, long.class, LayoutRecordMapper.class));
                                                // (MemorySegment, GroupLayout, long offset, long count, Function) ->
                                                // (MemorySegment, GroupLayout, long offset, long count)
                                                var mh2 = MethodHandles.insertArguments(mh, 4, componentMapper);
                                                // (MemorySegment, GroupLayout, long offset, long count) ->
                                                // (MemorySegment, GroupLayout, long offset)
                                                var mh3 = MethodHandles.insertArguments(mh2, 3, count);
                                                // (MemorySegment, GroupLayout, long offset) ->
                                                // (MemorySegment, long offset)
                                                var mh4 = MethodHandles.insertArguments(mh3, 1, gl);
                                                yield new MethodHandleAndOffset(mh4, byteOffset);
                                            } catch (NoSuchMethodException | IllegalAccessException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                        case SequenceLayout __ -> {
                                            throw new UnsupportedOperationException("Sequence layout of sequence layout is not supported: " + sl);
                                        }
                                        case PaddingLayout __ -> {
                                            yield null;
                                        }
                                    }
                                } catch (NoSuchMethodException | IllegalAccessException e) {
                                    throw new InternalError(e);
                                }
                            }
                            case PaddingLayout __ -> null; // Just ignore
                        };
                    })
                    .filter(Objects::nonNull) // Remove ignored items
                    .toArray(MethodHandleAndOffset[]::new);

        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("There is no constructor in " + type.getName() +
                    " for " + Arrays.toString(ctorParameterTypes), e);
        }

    }

    @Override
    public T apply(MemorySegment segment) {
        return get(segment, 0);
    }

    // Reflectively used
    public T get(MemorySegment segment, long offset) {
        Object[] parameters;
        try {
            parameters = new Object[handles.length];
            for (int i = 0; i < handles.length; i++) {
                try {
                    MethodHandleAndOffset mho = handles[i];
                    parameters[i] = mho.handle().invoke(segment, mho.offset());
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to extract values for the canonical constructor", e);
        }
        try {
            // Todo: Use a MethodHandle instead
            return canonicalConstructor.newInstance(parameters);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to invoke the canonical constructor for "
                    + type.getName() + " (" + canonicalConstructor + ") using " +
                    Arrays.toString(parameters), e);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "type=" + type.getName() + ", " +
                "layout=" + layout + "}";
    }

    private static <T, K, U>
    Collector<T, ?, Map<K, U>> toLinkedHashMap(Function<? super T, ? extends K> keyMapper,
                                               Function<? super T, ? extends U> valueMapper) {
        return Collectors.toMap(keyMapper, valueMapper, (a, b) -> {
            throw new InternalError("Should not reach here");
        }, LinkedHashMap::new);
    }

    static Class<? extends ValueLayout> valueLayoutType(ValueLayout vl) {
        return switch (vl) {
            case ValueLayout.OfBoolean __ -> ValueLayout.OfBoolean.class;
            case ValueLayout.OfByte __ -> ValueLayout.OfByte.class;
            case ValueLayout.OfShort __ -> ValueLayout.OfShort.class;
            case ValueLayout.OfChar __ -> ValueLayout.OfChar.class;
            case ValueLayout.OfInt __ -> ValueLayout.OfInt.class;
            case ValueLayout.OfLong __ -> ValueLayout.OfLong.class;
            case ValueLayout.OfFloat __ -> ValueLayout.OfFloat.class;
            case ValueLayout.OfDouble __ -> ValueLayout.OfDouble.class;
            case AddressLayout __ -> throw new IllegalStateException("No type for: " + vl.toString());
        };
    }

    static void assertExactMatch(ComponentAndLayout cl,
                                 Class<? extends Record> type,
                                 ValueLayout vl,
                                 MemoryLayout originalLayout) {

        Class<?> recordComponentType = cl.component().getType();
        if (recordComponentType.isArray() && cl.layout() instanceof SequenceLayout) {
            recordComponentType = recordComponentType.componentType();
        }

        if (recordComponentType != vl.carrier()) {
            throw new IllegalArgumentException("The return type of '" + cl.component().getName() + "()' (in " +
                    type.getName() + ") is '" + cl.component().getType() +
                    "' but the layout type is '" + vl.carrier() + "' (in " + originalLayout + ")");
        }
    }

    private <R extends Record> LayoutRecordMapper<R> recordMapper(Class<R> componentType,
                                                                  GroupLayout gl,
                                                                  long byteOffset) {

        if (!componentType.isRecord()) {
            throw new IllegalArgumentException(componentType + " is not a Record");
        }
        return new LayoutRecordMapper<>(componentType, gl, byteOffset);
    }

    @FunctionalInterface
    interface ObjLongFunction<T, R> {
        R apply(T t, long l);
    }

    record ComponentAndLayout(RecordComponent component,
                              MemoryLayout layout) {
    }

    /*
     * The MethodHandle shall have the coordinates of (MemorySegment, long)
     */
    record MethodHandleAndOffset(MethodHandle handle,
                                 long offset) {
    }

    // Wrapper to create an array of Records

    @SuppressWarnings("unchecked")
    static <R extends Record> R[] toArray(MemorySegment segment,
                                          GroupLayout elementLayout,
                                          long offset,
                                          long count,
                                          LayoutRecordMapper<R> mapper) {

        return slice(segment, elementLayout, offset, count)
                .elements(elementLayout)
                .map(mapper)
                .toArray(l -> (R[]) Array.newInstance(mapper.type, l));
    }

    // Below are `MemorySegment::toArray` wrapper methods that is also taking an offset
    // Begin: Reflectively used methods

    static byte[] toArray(MemorySegment segment,
                          ValueLayout.OfByte elementLayout,
                          long offset,
                          long count) {

        return slice(segment, elementLayout, offset, count).toArray(elementLayout);
    }

    static short[] toArray(MemorySegment segment,
                           ValueLayout.OfShort elementLayout,
                           long offset,
                           long count) {

        return slice(segment, elementLayout, offset, count).toArray(elementLayout);
    }

    static char[] toArray(MemorySegment segment,
                          ValueLayout.OfChar elementLayout,
                          long offset,
                          long count) {

        return slice(segment, elementLayout, offset, count).toArray(elementLayout);
    }

    static int[] toArray(MemorySegment segment,
                         ValueLayout.OfInt elementLayout,
                         long offset,
                         long count) {

        return slice(segment, elementLayout, offset, count).toArray(elementLayout);
    }

    static long[] toArray(MemorySegment segment,
                          ValueLayout.OfLong elementLayout,
                          long offset,
                          long count) {

        return slice(segment, elementLayout, offset, count).toArray(elementLayout);
    }

    static float[] toArray(MemorySegment segment,
                           ValueLayout.OfFloat elementLayout,
                           long offset,
                           long count) {

        return slice(segment, elementLayout, offset, count).toArray(elementLayout);
    }

    static double[] toArray(MemorySegment segment,
                            ValueLayout.OfDouble elementLayout,
                            long offset,
                            long count) {

        return slice(segment, elementLayout, offset, count).toArray(elementLayout);
    }

    // End: Reflectively used methods

    private static MemorySegment slice(MemorySegment segment,
                                       MemoryLayout elementLayout,
                                       long offset,
                                       long count) {

        return segment.asSlice(offset, elementLayout.byteSize() * count);
    }

}
