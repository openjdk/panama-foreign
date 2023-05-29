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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

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
    private final long offset;
    private final MethodHandle ctor;

    public LayoutRecordMapper(Class<T> type,
                              GroupLayout layout) {
        this(type, layout, 0);
    }

    private LayoutRecordMapper(Class<T> type,
                               GroupLayout layout,
                               long offset) {

        if (Record.class.equals(type)) {
            throw new IllegalArgumentException("The common base class java.lang.Record is not a record in itself");
        }

        this.type = type;
        this.layout = layout;
        this.offset = offset;

        assertMappingsCorrect();

        // For each component, find an f(a) = MethodHandle(MemorySegment) that returns the component type
        var handles = Arrays.stream(type.getRecordComponents())
                .map(this::methodHandle)
                .toList();

        Class<?>[] ctorParameterTypes = Arrays.stream(type.getRecordComponents())
                .map(RecordComponent::getType)
                .toArray(Class<?>[]::new);

        try {
            var ctor = PUBLIC_LOOKUP.findConstructor(type, MethodType.methodType(void.class, ctorParameterTypes));
            for (int i = 0; i < handles.size(); i++) {
                // Insert the respective handler for the constructor
                ctor = MethodHandles.filterArguments(ctor, i, handles.get(i));
            }

            var mt = MethodType.methodType(type, MemorySegment.class);
            // Fold the many identical MemorySegment arguments into a single argument
            ctor = MethodHandles.permuteArguments(ctor, mt, new int[handles.size()]);

            // The constructor MethodHandle is now of type (MemorySegment)T
            this.ctor = ctor;
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new IllegalArgumentException("There is no public constructor in '" + type.getName() +
                    "' for " + Arrays.toString(ctorParameterTypes), e);
        }
    }

    private MethodHandle methodHandle(RecordComponent component) {

        var pathElement = MemoryLayout.PathElement.groupElement(component.getName());
        var componentLayout = layout.select(pathElement);
        var byteOffset = layout.byteOffset(pathElement) + offset;
        try {
            return switch (componentLayout) {
                case ValueLayout vl -> methodHandle(vl, component, byteOffset);
                case GroupLayout gl -> methodHandle(gl, component, byteOffset);
                case SequenceLayout sl -> methodHandle(sl, component, byteOffset);
                case PaddingLayout __ -> throw fail(component, componentLayout);
            };
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new InternalError(e);
        }
    }

    private MethodHandle methodHandle(ValueLayout vl,
                                      RecordComponent component,
                                      long byteOffset) throws NoSuchMethodException, IllegalAccessException {

        assertTypesMatch(component, null, vl);
        var mt = MethodType.methodType(vl.carrier(), valueLayoutType(vl), long.class);
        var mh = PUBLIC_LOOKUP.findVirtual(MemorySegment.class, "get", mt);
        // (MemorySegment, OfX, long) -> (MemorySegment, long)
        mh = MethodHandles.insertArguments(mh, 1, vl);
        // (MemorySegment, long) -> (MemorySegment)
        return castReturnType(MethodHandles.insertArguments(mh, 1, byteOffset), component.getType());
    }

    private MethodHandle methodHandle(GroupLayout gl,
                                      RecordComponent component,
                                      long byteOffset) throws NoSuchMethodException, IllegalAccessException {

        var componentType = component.getType().asSubclass(Record.class);
        var componentMapper = recordMapper(componentType, gl, byteOffset);
        var mt = MethodType.methodType(Record.class, MemorySegment.class);
        var mh = LOOKUP.findVirtual(LayoutRecordMapper.class, "apply", mt);
        // (LayoutRecordAccessor, MemorySegment)Record -> (MemorySegment)Record
        mh = MethodHandles.insertArguments(mh, 0, componentMapper);
        // (MemorySegment)Record -> (MemorySegment)componentType
        return MethodHandles.explicitCastArguments(mh, MethodType.methodType(componentType, MemorySegment.class));
    }

    private MethodHandle methodHandle(SequenceLayout sl,
                                      RecordComponent component,
                                      long byteOffset) throws NoSuchMethodException, IllegalAccessException {

        String name = component.getName();
        var componentType = component.getType();
        if (!componentType.isArray()) {
            throw new IllegalArgumentException("Unable to map '" + sl +
                    "' because the component '" + componentType.getName() + " " + name + "' is not an array");
        }

        MultidimensionalSequenceLayoutInfo info = MultidimensionalSequenceLayoutInfo.of(sl);

        if (info.elementLayout() instanceof ValueLayout.OfBoolean) {
            throw new IllegalArgumentException("Arrays of booleans (" + info.elementLayout() + ") are not supported");
        }

        if (dimensionOf(componentType) != info.sequences().size()) {
            throw new IllegalArgumentException("Unable to map '" + sl + "'" +
                    " of dimension " + info.sequences().size() +
                    " because the component '" + componentType.getName() + " " + name + "'" +
                    " has a dimension of " + dimensionOf(componentType));
        }

        // Handle multi-dimensional arrays
        if (info.sequences().size() > 1) {
            var mh = LOOKUP.findStatic(LayoutRecordMapper.class, "toMultiArrayFunction",
                    MethodType.methodType(Object.class, MemorySegment.class, MultidimensionalSequenceLayoutInfo.class, long.class, Class.class, Function.class));
            // (MemorySegment, MultidimensionalSequenceLayoutInfo, long offset, Class leafType, Function mapper) ->
            // (MemorySegment, long offset, Class leafType, Function mapper)
            mh = MethodHandles.insertArguments(mh, 1, info);
            // (MemorySegment, long offset, Class leafType, Function mapper) ->
            // (MemorySegment, Class leafType, Function mapper)
            mh = MethodHandles.insertArguments(mh, 1, byteOffset);

            switch (info.elementLayout()) {
                case ValueLayout vl -> {
                    // (MemorySegment, Class leafType, Function mapper) ->
                    // (MemorySegment, Function mapper)
                    mh = MethodHandles.insertArguments(mh, 1, vl.carrier());
                    Function<MemorySegment, Object> leafArrayMapper =
                            switch (vl) {
                                case ValueLayout.OfByte ofByte -> ms -> ms.toArray(ofByte);
                                case ValueLayout.OfBoolean ofBoolean ->
                                        throw new UnsupportedOperationException("boolean arrays not supported: " + ofBoolean);
                                case ValueLayout.OfShort ofShort -> ms -> ms.toArray(ofShort);
                                case ValueLayout.OfChar ofChar -> ms -> ms.toArray(ofChar);
                                case ValueLayout.OfInt ofInt -> ms -> ms.toArray(ofInt);
                                case ValueLayout.OfLong ofLong -> ms -> ms.toArray(ofLong);
                                case ValueLayout.OfFloat ofFloat -> ms -> ms.toArray(ofFloat);
                                case ValueLayout.OfDouble ofDouble -> ms -> ms.toArray(ofDouble);
                                case AddressLayout addressLayout -> ms -> ms.elements(addressLayout)
                                        .map(s -> s.get(addressLayout, 0))
                                        .toArray(MemorySegment[]::new);
                            };
                    // (MemorySegment, Function mapper) ->
                    // (MemorySegment)
                    mh = MethodHandles.insertArguments(mh, 1, leafArrayMapper);
                    return castReturnType(mh, component.getType());
                }
                case GroupLayout gl -> {
                    var arrayComponentType = deepArrayComponentType(component.getType()).asSubclass(Record.class);
                    // The "local" byteOffset for the record component mapper is zero
                    var componentMapper = recordMapper(arrayComponentType, gl, 0);
                    Function<MemorySegment, Object> leafArrayMapper = ms -> {
                        Object leafArray = Array.newInstance(arrayComponentType, info.lastDimension());

                        int[] i = new int[]{0};
                        ms.elements(info.elementLayout())
                                .map(componentMapper)
                                .forEachOrdered(r -> Array.set(leafArray, i[0]++, r));
                        return leafArray;
                    };

                    // (MemorySegment, Class leafType, Function mapper) ->
                    // (MemorySegment, Function mapper)
                    mh = MethodHandles.insertArguments(mh, 1, arrayComponentType);
                    // (MemorySegment, Function mapper) ->
                    // (MemorySegment)
                    mh = MethodHandles.insertArguments(mh, 1, leafArrayMapper);
                    return castReturnType(mh, component.getType());
                }
                case SequenceLayout __ -> {
                    throw new InternalError("Should not reach here");
                }
                case PaddingLayout __ -> throw fail(component, sl);
            }
        }

        // Faster single-dimensional arrays
        switch (info.elementLayout()) {
            case ValueLayout vl -> {
                assertTypesMatch(component, sl, vl);
                var mh = findStaticToArray(vl.carrier().arrayType(), valueLayoutType(vl), null);
                // (MemorySegment, OfX, long offset, long count) -> (MemorySegment, OfX, long offset)
                mh = MethodHandles.insertArguments(mh, 3, info.sequences().getFirst().elementCount());
                // (MemorySegment, OfX, long offset) -> (MemorySegment, long offset)
                mh = MethodHandles.insertArguments(mh, 1, vl);
                // (MemorySegment, long offset) -> (MemorySegment)
                return castReturnType(MethodHandles.insertArguments(mh, 1, byteOffset), component.getType());
            }
            case GroupLayout gl -> {
                var arrayComponentType = deepArrayComponentType(component.getType()).asSubclass(Record.class);
                // The "local" byteOffset for the record component mapper is zero
                var componentMapper = recordMapper(arrayComponentType, gl, 0);
                try {
                    var mh = findStaticToArray(Record.class.arrayType(), GroupLayout.class, LayoutRecordMapper.class);
                    // (MemorySegment, GroupLayout, long offset, long count, Function) ->
                    // (MemorySegment, GroupLayout, long offset, long count)
                    mh = MethodHandles.insertArguments(mh, 4, componentMapper);
                    // (MemorySegment, GroupLayout, long offset, long count) ->
                    // (MemorySegment, GroupLayout, long offset)
                    mh = MethodHandles.insertArguments(mh, 3, info.sequences().getFirst().elementCount());
                    // (MemorySegment, GroupLayout, long offset) ->
                    // (MemorySegment, long offset)
                    mh = MethodHandles.insertArguments(mh, 1, gl);
                    // (MemorySegment, long offset) -> (MemorySegment)Record[]
                    mh = MethodHandles.insertArguments(mh, 1, byteOffset);
                    // (MemorySegment, long offset)Record[] -> (MemorySegment)componentType
                    return MethodHandles.explicitCastArguments(mh, MethodType.methodType(component.getType(), MemorySegment.class));
                } catch (NoSuchMethodException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            case SequenceLayout __ ->  throw new InternalError("Should not reach here");
            case PaddingLayout __ -> throw fail(component, sl);
        }
    }

    private IllegalArgumentException fail(RecordComponent component,
                                          MemoryLayout layout) {
        throw new IllegalArgumentException(
                "Unable to map " + layout + " to " + type.getName() + "." + component.getName());
    }

    @SuppressWarnings("unchecked")
    @Override
    public T apply(MemorySegment segment) {
        try {
            return (T) (ctor.invoke(segment));
        } catch (Throwable e) {
            throw new IllegalArgumentException(
                    "Unable to invoke the canonical constructor for " + type.getName() +
                            " using " + segment, e);
        }
    }

    @Override
    public String toString() {
        return "LayoutRecordMapper{" +
                "type=" + type.getName() + ", " +
                "layout=" + layout + ", " +
                "offset=" + offset + "}";
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

    void assertTypesMatch(RecordComponent component,
                          MemoryLayout sequenceLayout,
                          ValueLayout vl) {

        Class<?> recordComponentType = component.getType();
        if (recordComponentType.isArray() && sequenceLayout instanceof SequenceLayout) {
            recordComponentType = deepArrayComponentType(recordComponentType);
        }

        if (!(recordComponentType == vl.carrier())) {
            throw new IllegalArgumentException("Unable to match types because the component '" +
                    component.getName() + "' (in " + type.getName() + ") has the type of '" + component.getType() +
                    "' but the layout carrier is '" + vl.carrier() + "' (in " + layout + ")");
        }
    }

    static Class<?> deepArrayComponentType(Class<?> arrayType) {
        Class<?> recordComponentType = arrayType;
        while (recordComponentType.isArray()) {
            recordComponentType = Objects.requireNonNull(recordComponentType.componentType());
        }
        return recordComponentType;
    }

    void assertMappingsCorrect() {
        var nameMappingCounts = layout.memberLayouts().stream()
                .map(MemoryLayout::name)
                .flatMap(Optional::stream)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // Make sure we have all components distinctly mapped
        for (RecordComponent component : type.getRecordComponents()) {
            String name = component.getName();
            switch (nameMappingCounts.get(name).intValue()) {
                case 0 -> throw new IllegalArgumentException("No mapping for " +
                        type.getName() + "." + component.getName() +
                        " in layout " + layout);
                case 1 -> { /* Happy path */ }
                default -> throw new IllegalArgumentException("Duplicate mappings for " +
                        type.getName() + "." + component.getName() +
                        " in layout " + layout);
            }
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

    record MultidimensionalSequenceLayoutInfo(List<SequenceLayout> sequences,
                                              MemoryLayout elementLayout){

        int[] dimensions() {
            return sequences().stream()
                    .mapToLong(SequenceLayout::elementCount)
                    .mapToInt(Math::toIntExact)
                    .toArray();
        }

        int firstDimension() {
           return (int) sequences().getFirst().elementCount();
        }

        int lastDimension() {
            return (int) sequences().getLast().elementCount();
        }

        long layoutByteSize() {
            return sequences()
                    .getFirst()
                    .byteSize();
        }

        MultidimensionalSequenceLayoutInfo removeFirst() {
            var removed = new ArrayList<>(sequences);
            removed.removeFirst();
            return new MultidimensionalSequenceLayoutInfo(removed, elementLayout);
        }

        static MultidimensionalSequenceLayoutInfo of(SequenceLayout sequenceLayout) {
            MemoryLayout current = sequenceLayout;
            List<SequenceLayout> sequences = new ArrayList<>();
            while(true) {
                if (current instanceof SequenceLayout element) {
                    long count = element.elementCount();
                    if (count > ArraysSupport.SOFT_MAX_ARRAY_LENGTH) {
                        throw new IllegalArgumentException("Unable to accommodate '" + element + "' in an array.");
                    }
                    current = element.elementLayout();
                    sequences.add(element);
                } else {
                    return new MultidimensionalSequenceLayoutInfo(List.copyOf(sequences), current);
                }
            }
        }

    }

    // Provide widening and boxing magic
    static MethodHandle castReturnType(MethodHandle mh,
                                       Class<?> to) {
        var from = mh.type().returnType();
        if (from == to) {
            // We are done as it is
            return mh;
        }

        if (!to.isPrimitive() && !isWrapperClass(to) && !to.isArray()) {
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

    static int dimensionOf(Class<?> arrayClass) {
        return (int) Stream.<Class<?>>iterate(arrayClass, Class::isArray, Class::componentType)
                .count();
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

    static MemorySegment[] toArray(MemorySegment segment,
                                   AddressLayout elementLayout,
                                   long offset,
                                   long count) {

        return slice(segment, elementLayout, offset, count)
                .elements(elementLayout)
                .map(s -> s.get(elementLayout, 0))
                .toArray(MemorySegment[]::new);
    }

    // End: Reflectively used methods

    private static MemorySegment slice(MemorySegment segment,
                                       MemoryLayout elementLayout,
                                       long offset,
                                       long count) {

        return segment.asSlice(offset, elementLayout.byteSize() * count);
    }

    static Object toMultiArrayFunction(MemorySegment segment,
                                       MultidimensionalSequenceLayoutInfo info,
                                       long offset,
                                       Class<?> leafType,
                                       Function<MemorySegment, Object> leafArrayConstructor) {

        int[] dimensions = info.dimensions();
        // Create the array to return
        Object result = Array.newInstance(leafType, dimensions);

        int firstDimension = info.firstDimension();

        var infoFirstRemoved = info.removeFirst();
        int secondDimension = infoFirstRemoved.firstDimension();
        long chunkByteSize = infoFirstRemoved.layoutByteSize();

        for (int i = 0; i < firstDimension; i++) {
            Object part;
            if (dimensions.length == 2) {
                // Trivial case: Just extract the array from the memory segment
                var slice = slice(segment, info.elementLayout(), offset + i * chunkByteSize, secondDimension);
                part = leafArrayConstructor.apply(slice);
            } else {
                // Recursively convert to arrays of (dimension - 1)
                var slice = segment.asSlice(i * chunkByteSize);
                part = toMultiArrayFunction(slice, infoFirstRemoved, offset, leafType, leafArrayConstructor);
            }
            Array.set(result, i, part);
        }
        return result;
    }

}
