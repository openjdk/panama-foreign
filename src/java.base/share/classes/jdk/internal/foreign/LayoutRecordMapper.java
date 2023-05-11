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

import java.lang.foreign.AddressLayout;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.PaddingLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
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

    @FunctionalInterface
    interface ObjLongFunction<T, R> {
        R apply(T t, long l);
    }

    private final ObjLongFunction<MemorySegment, T> getter;

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

        record ComponentLayout(RecordComponent component, MemoryLayout layout) {
        }

        record MethodHandleOffset(MethodHandle handle, long offset) {
        }

        // Todo: Compose a single MethodHandle(MemorySegment, long) that returns T, to use as a getter

        Map<String, RecordComponent> components = Stream.of(type.getRecordComponents())
                .collect(toLinkedHashMap(RecordComponent::getName, Function.identity()));

        if (components.isEmpty()) {
            throw new IllegalArgumentException("The provided Record type did not contain any components.");
        }

        // System.out.println("components = " + components);

        Map<String, MemoryLayout> layouts = layout.memberLayouts().stream()
                .collect(toLinkedHashMap(l -> l.name().orElseThrow(), Function.identity()));

        // System.out.println("layouts = " + layouts);

        var missingComponents = components.keySet().stream()
                .filter(l -> !layouts.containsKey(l))
                .toList();

        if (!missingComponents.isEmpty()) {
            throw new IllegalArgumentException("There is no mapping for " +
                    missingComponents + " in " + type.getName() +
                    "(" + String.join(", ", components.keySet()) + ")" +
                    " provided by the layout " + layout);
        }

        Map<String, ComponentLayout> componentLayoutMap = components.entrySet().stream()
                .map(e -> new ComponentLayout(e.getValue(), layouts.get(e.getKey())))
                .collect(toLinkedHashMap(cl -> cl.component().getName(), Function.identity()));

        Class<?>[] ctorParameterTypes = components.values().stream()
                .map(RecordComponent::getType)
                .toArray(Class<?>[]::new);

        System.out.println("Creating mapper for " + type + " via " + layout);
        System.out.println("ctorParameterTypes = " + Arrays.toString(ctorParameterTypes));

        try {
            Constructor<T> canonicalConstructor = type.getDeclaredConstructor(ctorParameterTypes);

            System.out.println("canonicalConstructor = " + canonicalConstructor);

            var handles = componentLayoutMap.values().stream()
                    .map(cl -> {
                        var name = cl.layout().name().get();
                        var pathElement = MemoryLayout.PathElement.groupElement(name);
                        long byteOffset = layout.byteOffset(pathElement) + offset;

                        System.out.println("Offset for " + name + " is " + byteOffset);

                        return switch (cl.layout()) {
                            case ValueLayout vl -> {
                                try {
                                    Class<?> valueLayoutType = switch (vl) {
                                        case ValueLayout.OfBoolean __ -> ValueLayout.OfBoolean.class;
                                        case ValueLayout.OfByte __ -> ValueLayout.OfByte.class;
                                        case ValueLayout.OfShort __ -> ValueLayout.OfShort.class;
                                        case ValueLayout.OfChar __ -> ValueLayout.OfChar.class;
                                        case ValueLayout.OfInt __ -> ValueLayout.OfInt.class;
                                        case ValueLayout.OfLong __ -> ValueLayout.OfLong.class;
                                        case ValueLayout.OfFloat __ -> ValueLayout.OfFloat.class;
                                        case ValueLayout.OfDouble __ -> ValueLayout.OfDouble.class;
                                        case AddressLayout __ -> throw new IllegalStateException(layout.toString());
                                    };
                                    Method method = MemorySegment.class.getMethod("get", valueLayoutType, long.class);
                                    method.setAccessible(true);
                                    MethodHandle mh = LOOKUP.unreflect(method);

                                    System.out.println("Return type: " + method.getReturnType() + ", carrier: " + vl.carrier());

                                    if (cl.component().getType() != vl.carrier()) {
                                        throw new IllegalArgumentException("The return type of '" + cl.component().getName() + "()' (in " +
                                                type.getName() + ") is '" + cl.component().getType() +
                                                "' but the layout type is '" + vl.carrier() + "' (in " + layout + ")");
                                    }

                                    // (MemorySegment, OfX, long ) -> (MemorySegment, long)
                                    yield new MethodHandleOffset(MethodHandles.insertArguments(mh, 1, vl), byteOffset);
                                } catch (NoSuchMethodException | IllegalAccessException e) {
                                    throw new InternalError(e);
                                }
                            }
                            case GroupLayout gl -> {

                                System.out.println("StructLayout: " + gl);

                                Class<?> componentType = cl.component().getType();
                                if (!componentType.isRecord()) {
                                    throw new IllegalArgumentException(componentType + " is not a Record");
                                }
                                Class<Record> componentRecordType = (Class<Record>) componentType;

                                LayoutRecordMapper<?> componentRecord =
                                        new LayoutRecordMapper<>(componentRecordType, gl, byteOffset);

                                try {
                                    Method method = LayoutRecordMapper.class.getDeclaredMethod("get", MemorySegment.class, long.class);
                                    method.setAccessible(true);
                                    var mh = LOOKUP.unreflect(method);
                                    // (LayoutRecordAccessor, MemorySegment, long) -> (MemorySegment, long)
                                    yield new MethodHandleOffset(MethodHandles.insertArguments(mh, 0, componentRecord), byteOffset);
                                } catch (NoSuchMethodException | IllegalAccessException e) {
                                    throw new RuntimeException(e);
                                }

/*                                //MethodType methodType = MethodType.methodType(componentRecordType, MemorySegment.class, long.class);
                                MethodType methodType = MethodType.methodType(Object.class, MemorySegment.class, long.class);
                                try {
                                    // LayoutRecordAccessor is in jdk.internal so, we need to use lookup() rather than publicLookup()
                                    MethodHandle mh = MethodHandles.lookup()
                                            .findVirtual(LayoutRecordAccessor.class, "get", methodType);
                                    yield MethodHandles.insertArguments(mh, 0, componentRecord);
                                } catch (NoSuchMethodException | IllegalArgumentException | IllegalAccessException e) {
                                    throw new IllegalArgumentException(e);
                                }*/
                            }
                            case PaddingLayout __ -> null;
                            default -> throw new UnsupportedOperationException();
                        };
                    })
                    .filter(Objects::nonNull)
                    .toArray(MethodHandleOffset[]::new);

            // Todo: Use LazyArray here
            Function<MemorySegment, Object[]> extractor = ms -> {

                Object[] parameters = new Object[handles.length];
                for (int i = 0; i < handles.length; i++) {
                    try {
                        MethodHandleOffset mho = handles[i];
                        parameters[i] = mho.handle().invoke(ms, mho.offset());
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
                System.out.println("Parameters = " + Arrays.toString(parameters));
                return parameters;
            };

            getter = (ms, o) -> {
                try {
                    // Todo: Use Method reference instead
                    return canonicalConstructor.newInstance(extractor.apply(ms));
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("Unable to extract " + type.getName(), e);
                }
            };


        } catch (NoSuchMethodException nsme) {
            throw new IllegalArgumentException("There is no constructor in " + type.getName() + " for " + ctorParameterTypes);
        }

    }

    @Override
    public T apply(MemorySegment segment) {
        return getter.apply(segment, 0);
    }


    public T get(MemorySegment segment, long offset) {
        return getter.apply(segment, offset);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{type=" + type.getName() + ", layout=" + layout + "}";
    }

    private static <T, K, U>
    Collector<T, ?, Map<K, U>> toLinkedHashMap(Function<? super T, ? extends K> keyMapper,
                                               Function<? super T, ? extends U> valueMapper) {
        return Collectors.toMap(keyMapper, valueMapper, (a, b) -> {
            throw new InternalError("Should not reach here");
        }, LinkedHashMap::new);
    }

}
