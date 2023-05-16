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

import java.lang.foreign.AddressLayout;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.PaddingLayout;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toUnmodifiableSet;

/**
 * A record mapper that is matching a GroupLayout to match the components of a record.
 *
 * @param <T> the Record type
 */
public final class LayoutRecordMapper<T extends Record>
        implements Function<MemorySegment, T> {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final MethodHandles.Lookup PUBLIC_LOOKUP = MethodHandles.publicLookup();

    private final Class<T> type;
    private final GroupLayout layout;
    private final MethodHandle ctor;

    public LayoutRecordMapper(Class<T> type,
                              GroupLayout layout) {
        this(type, layout, 0);
    }

    private LayoutRecordMapper(Class<T> type,
                               GroupLayout layout,
                               long offset) {
        this.type = type;
        this.layout = layout;

        var recordComponents = Arrays.asList(type.getRecordComponents());

        var nameToLayoutMap = layout.memberLayouts().stream()
                .filter(l -> l.name().isPresent())
                .collect(toMap(l -> l.name().orElseThrow(), Function.identity()));

        var missingComponents = recordComponents.stream()
                .map(RecordComponent::getName)
                .filter(l -> !nameToLayoutMap.containsKey(l))
                .toList();

        if (!missingComponents.isEmpty()) {
            throw new IllegalArgumentException("There is no mapping for " +
                    missingComponents + " in " + type.getName() +
                    "(" + String.join(", ", recordComponents.stream().map(RecordComponent::getName).collect(Collectors.joining(", "))) + ")" +
                    " provided by the layout " + layout);
        }

        var componentAndLayoutList = recordComponents.stream()
                .map(c -> new ComponentAndLayout(c, nameToLayoutMap.get(c.getName())))
                .toList();

        Class<?>[] ctorParameterTypes = recordComponents.stream()
                .map(RecordComponent::getType)
                .toArray(Class<?>[]::new);

        // An array of the record component MethodHandle extractors, each of type (MemorySegment)X
        // where X is the component type.
        MethodHandle[] handles = componentAndLayoutList.stream()
                .map(cl -> {
                    var name = cl.layout().name().orElseThrow();
                    var pathElement = MemoryLayout.PathElement.groupElement(name);
                    long byteOffset = layout.byteOffset(pathElement) + offset;

                    return switch (cl.layout()) {
                        case ValueLayout vl -> {
                            try {
                                assertTypesMatch(cl, type, vl, layout);
                                var mt = MethodType.methodType(vl.carrier(), valueLayoutType(vl), long.class);
                                var mh = PUBLIC_LOOKUP.findVirtual(MemorySegment.class, "get", mt);
                                // (MemorySegment, OfX, long ) -> (MemorySegment, long)
                                mh = MethodHandles.insertArguments(mh, 1, vl);
                                // (MemorySegment, long ) -> (MemorySegment)
                                yield castReturnType(MethodHandles.insertArguments(mh, 1, byteOffset), cl.component().getType());
                            } catch (NoSuchMethodException | IllegalAccessException e) {
                                throw new InternalError(e);
                            }
                        }
                        case GroupLayout gl -> {
                            @SuppressWarnings("unchecked")
                            var componentType = (Class<? extends Record>) cl.component().getType();
                            var componentMapper = recordMapper(componentType, gl, byteOffset);
                            try {
                                var mt = MethodType.methodType(Record.class, MemorySegment.class);
                                var mh = LOOKUP.findVirtual(LayoutRecordMapper.class, "apply", mt);
                                // (LayoutRecordAccessor, MemorySegment)Record -> (MemorySegment)Record
                                mh = MethodHandles.insertArguments(mh, 0, componentMapper);
                                // (MemorySegment)Record -> (MemorySegment)componentType
                                yield MethodHandles.explicitCastArguments(mh, MethodType.methodType(componentType, MemorySegment.class));
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
                                        assertTypesMatch(cl, type, vl, layout);
                                        var mh = findStaticToArray(vl.carrier().arrayType(), valueLayoutType(vl), null);
                                        // (MemorySegment, OfX, long offset, long count) -> (MemorySegment, OfX, long offset)
                                        mh = MethodHandles.insertArguments(mh, 3, count);
                                        // (MemorySegment, OfX, long offset) -> (MemorySegment, long offset)
                                        mh = MethodHandles.insertArguments(mh, 1, vl);
                                        // (MemorySegment, long offset) -> (MemorySegment)
                                        yield castReturnType(MethodHandles.insertArguments(mh, 1, byteOffset), cl.component().getType());
                                    }
                                    case GroupLayout gl -> {
                                        @SuppressWarnings("unchecked")
                                        var arrayComponentType = (Class<? extends Record>) Objects.requireNonNull(cl.component()
                                                .getType()
                                                .componentType());
                                        // The "local" byteOffset for the record component mapper is zero
                                        var componentMapper = recordMapper(arrayComponentType, gl, 0);
                                        try {
                                            var mh = findStaticToArray(Record.class.arrayType(), GroupLayout.class, LayoutRecordMapper.class);
                                            // (MemorySegment, GroupLayout, long offset, long count, Function) ->
                                            // (MemorySegment, GroupLayout, long offset, long count)
                                            mh = MethodHandles.insertArguments(mh, 4, componentMapper);
                                            // (MemorySegment, GroupLayout, long offset, long count) ->
                                            // (MemorySegment, GroupLayout, long offset)
                                            mh = MethodHandles.insertArguments(mh, 3, count);
                                            // (MemorySegment, GroupLayout, long offset) ->
                                            // (MemorySegment, long offset)
                                            mh = MethodHandles.insertArguments(mh, 1, gl);
                                            // (MemorySegment, long offset) -> (MemorySegment)Record[]
                                            mh = MethodHandles.insertArguments(mh, 1, byteOffset);
                                            // (MemorySegment, long offset)Record[] -> (MemorySegment)componentType
                                            yield MethodHandles.explicitCastArguments(mh, MethodType.methodType(cl.component().getType(), MemorySegment.class));
                                        } catch (NoSuchMethodException | IllegalAccessException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                    case SequenceLayout __ -> {
                                        throw new UnsupportedOperationException("Sequence layout of sequence layout is not supported: " + sl);
                                    }
                                    case PaddingLayout __ -> {
                                        yield null; // Ignore
                                    }
                                }
                            } catch (NoSuchMethodException | IllegalAccessException e) {
                                throw new InternalError(e);
                            }
                        }
                        case PaddingLayout __ -> null; // Ignore
                    };
                })
                .filter(Objects::nonNull) // Remove ignored items
                .toArray(MethodHandle[]::new);

        try {
            var ctor = PUBLIC_LOOKUP.findConstructor(type, MethodType.methodType(void.class, ctorParameterTypes));
            for (int i = 0; i < handles.length; i++) {
                // Insert the respective handler for the constructor
                ctor = MethodHandles.filterArguments(ctor, i, handles[i]);
            }

            var mt = MethodType.methodType(type, MemorySegment.class);
            // Fold the many identical MemorySegment arguments into a single argument
            ctor = MethodHandles.permuteArguments(ctor, mt, IntStream.range(0, handles.length)
                    .map(i -> 0)
                    .toArray());

            // The constructor MethodHandle is now of type (MemorySegment)T
            this.ctor = ctor;
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new IllegalArgumentException("There is no public constructor in " + type.getName() +
                    " for " + Arrays.toString(ctorParameterTypes), e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T apply(MemorySegment segment) {
        try {
            return (T) (ctor.invoke(segment));
        } catch (Throwable e) {
            throw new IllegalStateException(
                    "Unable to invoke the canonical constructor for " + type.getName(), e);
        }
    }

    @Override
    public String toString() {
        return "LayoutRecordMapper{" +
                "type=" + type.getName() + ", " +
                "layout=" + layout + "}";
    }

    // The parameter is of type ValueLayouts.OfByteImpl and the likes.
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
            case AddressLayout __ -> AddressLayout.class;
        };
    }

    static MethodHandle findStaticToArray(Class<?> rType,
                                          Class<?> layoutType,
                                          Class<?> extra) throws NoSuchMethodException, IllegalAccessException {

        var pTypes = Stream.of(MemorySegment.class, layoutType, long.class, long.class, extra)
                .filter(Objects::nonNull)
                .toArray(Class<?>[]::new);

        var mt = MethodType.methodType(rType, pTypes);
        return LOOKUP.findStatic(LayoutRecordMapper.class, "toArray", mt);
    }

    static void assertTypesMatch(ComponentAndLayout cl,
                                 Class<? extends Record> type,
                                 ValueLayout vl,
                                 MemoryLayout originalLayout) {

        Class<?> recordComponentType = cl.component().getType();
        if (recordComponentType.isArray() && cl.layout() instanceof SequenceLayout) {
            recordComponentType = Objects.requireNonNull(recordComponentType.componentType());
        }

        // Accept boxing: e.g. Integer.isInstance(int.class) -> true
        if (recordComponentType.isInstance(vl.carrier())) {
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

    record ComponentAndLayout(RecordComponent component,
                              MemoryLayout layout) {
    }

    // Provide widening and boxing magic
    static MethodHandle castReturnType(MethodHandle mh,
                                       Class<?> to) {
        var from = mh.type().returnType();
        if (from == to) {
            // We are done as it is
            return mh;
        }

        if (!to.isPrimitive() && !isWrapperClass(to)) {
            throw new IllegalArgumentException("Cannot convert '" + from + "' to '" + to.getName() +
                    "' because '" + to.getName() + "' is not a wrapper class: [" + WRAPPER_CLASSES.stream()
                    .map(Class::getSimpleName)
                    .collect(Collectors.joining(", "))+"]");
        }

        return MethodHandles.explicitCastArguments(mh, MethodType.methodType(to, MemorySegment.class));
    }

    private static final Set<Class<?>> WRAPPER_CLASSES = Stream.of(
            Byte.class, Boolean.class, Short.class, Character.class,
            Integer.class, Long.class, Float.class, Double.class
    ).collect(Collectors.collectingAndThen(
            Collectors.toCollection(LinkedHashSet::new),
            Collections::unmodifiableSet));

    static boolean isWrapperClass(Class<?> type) {
        return WRAPPER_CLASSES.contains(type);
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
