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
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

/**
 * A record mapper that is matching a GroupLayout to match the components of a record.
 *
 * @param <T> the Record type
 */
public final class LayoutRecordMapper<T extends Record>
        implements Function<MemorySegment, T> {

    enum Allow {EXACT, BOXING, BOXING_NARROWING_AND_WIDENING}

    public static final Allow ALLOW = Allow.EXACT;

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
                                var componentType = cl.component().getType();
                                if (!componentType.isArray()) {
                                    throw new IllegalArgumentException("Unable to map '" + sl +
                                            "' because the component '" + componentType.getName() + " " + name + "' is not an array");
                                }

                                MultidimensionalSequenceLayoutInfo info = MultidimensionalSequenceLayoutInfo.of(sl);

                                if (dimensionOf(componentType) != info.sequences().size()) {
                                    throw new IllegalArgumentException("Unable to map '" + sl + "'" +
                                            " of dimension " + info.sequences().size() +
                                            " because the component '" + componentType.getName() + " " + name + "'" +
                                            " has a dimension of " + dimensionOf(componentType));
                                }

                                if (info.sequences().size() > 1) {
                                    var mh = LOOKUP.findStatic(LayoutRecordMapper.class, "toMultiArrayFunction",
                                            MethodType.methodType(Object.class, MemorySegment.class, MultidimensionalSequenceLayoutInfo.class, long.class));
                                    // (MemorySegment, MultidimensionalSequenceLayoutInfo, long offset) ->
                                    // (MemorySegment, long offset)
                                    mh = MethodHandles.insertArguments(mh, 1, info);
                                    // (MemorySegment, long offset) -> (MemorySegment)
                                    mh = MethodHandles.insertArguments(mh, 1, byteOffset);
                                    yield castReturnType(mh, cl.component().getType());
                                }

                                switch (info.elementLayout()) {
                                    case ValueLayout vl -> {
                                        assertTypesMatch(cl, type, vl, layout);
                                        var mh = findStaticToArray(vl.carrier().arrayType(), valueLayoutType(vl), null);
                                        // (MemorySegment, OfX, long offset, long count) -> (MemorySegment, OfX, long offset)
                                        mh = MethodHandles.insertArguments(mh, 3, info.sequences().getFirst().elementCount());
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
                                            mh = MethodHandles.insertArguments(mh, 3, info.sequences().getFirst().elementCount());
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
                                        throw new InternalError("Should not reach here");
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
            throw new IllegalArgumentException("There is no public constructor in '" + type.getName() +
                    "' for " + Arrays.toString(ctorParameterTypes), e);
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
            while (recordComponentType.isArray()) {
                recordComponentType = Objects.requireNonNull(recordComponentType.componentType());
            }
        }

        boolean match = switch (ALLOW) {
            // Require types to be identical only: e.g. Integer.class != int.class
            case EXACT -> recordComponentType == vl.carrier();
            // Accept boxing: e.g. Integer.class.isInstance(int.class) -> true
            case BOXING -> recordComponentType.isInstance(vl.carrier());
            // Accept anything for now and signal errors later when composing VHs
            case BOXING_NARROWING_AND_WIDENING -> true;
        };

        if (!match) {
            throw new IllegalArgumentException("Unable to match types because the component '" +
                    cl.component().getName() + "' (in " + type.getName() + ") has the type of '" + cl.component().getType() +
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

    // End: Reflectively used methods

    private static MemorySegment slice(MemorySegment segment,
                                       MemoryLayout elementLayout,
                                       long offset,
                                       long count) {

        return segment.asSlice(offset, elementLayout.byteSize() * count);
    }

/*    // todo: There must be a better way...
    @SuppressWarnings("unckecked")
    static <T> T functionWrapper(MemorySegment segment,
                                 MethodHandle mh *//* Function<MemorySegment, T> mapper*//* ) {
        try {
            return (T) mh.invokeExact(segment);
        } catch (Throwable e) {
            throw new IllegalStateException("Unable to wrap method handle", e);
        }
    }*/

    static Object toMultiArrayFunction(MemorySegment segment,
                                       MultidimensionalSequenceLayoutInfo info,
                                       long offset) {
        return switch (info.elementLayout()) {
            case ValueLayout.OfByte ofByte ->
                    toMultiIntArrayFunctionNew(segment, info, offset, byte.class, s -> s.toArray(ofByte));
            case ValueLayout.OfBoolean ofBoolean ->
                    throw new UnsupportedOperationException(ofBoolean + " arrays not supported");
            case ValueLayout.OfShort ofShort ->
                    toMultiIntArrayFunctionNew(segment, info, offset, short.class, s -> s.toArray(ofShort));
            case ValueLayout.OfChar ofChar ->
                    toMultiIntArrayFunctionNew(segment, info, offset, char.class, s -> s.toArray(ofChar));
            case ValueLayout.OfInt ofInt ->
                    toMultiIntArrayFunctionNew(segment, info, offset, int.class, s -> s.toArray(ofInt));
            case ValueLayout.OfLong ofLong ->
                    toMultiIntArrayFunctionNew(segment, info, offset, long.class, s -> s.toArray(ofLong));
            case ValueLayout.OfFloat ofFloat ->
                    toMultiIntArrayFunctionNew(segment, info, offset, float.class, s -> s.toArray(ofFloat));
            case ValueLayout.OfDouble ofDouble ->
                    toMultiIntArrayFunctionNew(segment, info, offset, double.class, s -> s.toArray(ofDouble));
            case AddressLayout addressLayout ->
                    throw new UnsupportedOperationException(addressLayout + " arrays not supported");
            case GroupLayout groupLayout ->
                    // Todo: Fix this
                    throw new UnsupportedOperationException(groupLayout + " not supported");
            default -> throw new UnsupportedOperationException(info.elementLayout + " arrays not supported");
        };
    }

    // Todo: Recursively apply this method for smaller and smaller Lists

    static Object toMultiIntArrayFunction(MemorySegment segment,
                                          MultidimensionalSequenceLayoutInfo info,
                                          long offset) {
        ValueLayout.OfInt layout = (ValueLayout.OfInt) info.elementLayout();
        int size0 = (int) info.sequences().getFirst().elementCount();
        int[][] result = new int[size0][];
        int size1 = (int) info.sequences().getLast().elementCount();
        for (int i = 0; i < result.length; i++) {
            int[] part = slice(segment, layout, offset + i * layout.byteSize() * size0, size1).toArray(layout);
            result[i] = part;
        }
        return result;
    }

    static Object toMultiIntArrayFunctionNew(MemorySegment segment,
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
                part = toMultiIntArrayFunctionNew(slice, infoFirstRemoved, offset, leafType, leafArrayConstructor);
            }
            Array.set(result, i, part);
        }
        return result;
    }

}
