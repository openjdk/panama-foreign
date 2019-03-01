/*
 * Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
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

/*
 * @test
 * @bug 8192974 8218153
 * @summary overhaul descriptor string parsing
 * @modules java.base/jdk.internal.foreign.memory
 * @run main/othervm -Xmx1g TestDescriptorGrammar
 */

import jdk.internal.foreign.memory.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * This test generates all possible combinations of sentences in the type descriptor grammar and checks that the
 * descriptor parser can indeed parse such sentences. To ensure the test runs in an acceptable amount of time,
 * certain productions only generate a sequence of samples of all possible combinations (the sample size can
 * be altered by tweaking the associated static field accordingly).
 */
public class TestDescriptorGrammar {

    interface Template {
        Stream<String> generate(int depth);
    }

    enum Descriptor implements Template {
        ELEMENT_TYPE(ElementType.class),
        FUNCTION_TYPE(FunctionType.class);

        Class<?> e;

        Descriptor(Class<?> e) {
            this.e = e;
        }

        @Override
        public Stream<String> generate(int depth) {
            return generateAll(depth, cast(e));
        }
    }

    //Note: separators are not tested here
    enum ElementType implements Template {
        VALUE() {
            @Override
            public Stream<String> generate(int depth) {
                return generateAll(depth, ValueType.class);
            }
        },
        CONTAINER_TYPE() {
            @Override
            public Stream<String> generate(int depth) {
                return generateAll(depth, ContainerType.class);
            }
        },
        BYTE_SWAPPED_CONTAINER_TYPE() {
            @Override
            public Stream<String> generate(int depth) {
                return Stream.of(Endianness.BYTE_SWAP_BE, Endianness.BYTE_SWAP_LE)
                        .flatMap(endian -> Stream.concat(
                                sample(ContainerType.STRUCT.generate(depth)),
                                sample(depth == 0 ? Stream.empty() : ContainerType.ARRAY.generate(depth)))
                        .map(e -> endian.apply(e)));
            }
        }
    }

    enum ReturnType implements Template {
        ELEMENT() {
            @Override
            public Stream<String> generate(int depth) {
                return generateAll(depth, ElementType.class);
            }
        },

        VOID() {
            @Override
            public Stream<String> generate(int depth) {
                return Stream.of("V");
            }
        }
    }

    enum ContainerType implements Template {
        STRUCT() {
            @Override
            public Stream<String> generate(int depth) {
                return generateAll(depth, StructType.class).map(e -> "[" + e + "]");
            }
        },
        ARRAY() {
            @Override
            public Stream<String> generate(int depth) {
                return depth == 0 ?
                        ValueTypePrefix.PLAIN.generate(depth) :
                        digits(0).flatMap(s -> generateAll(depth - 1, ElementType.class).map(e -> "[" + s + e + "]"));
            }
        }
    }

    enum StructType implements Template {
        ELEMENT1() {
            @Override
            public Stream<String> generate(int depth) {
                return depth == 0 ?
                        ValueTypePrefix.PLAIN.generate(depth) :
                        generateAll(depth - 1, ElementType.class);
            }
        },
        ELEMENT2() {
            @Override
            public Stream<String> generate(int depth) {
                return sample(ELEMENT1.generate(depth)).flatMap(
                    e1 -> sample(ELEMENT1.generate(depth)).map(e2 -> e1 + e2)
                );
            }
        },
        UNION2() {
            @Override
            public Stream<String> generate(int depth) {
                return sample(ELEMENT1.generate(depth)).flatMap(
                    e1 -> sample(ELEMENT1.generate(depth)).map(e2 -> e1 + "|" + e2)
                );
            }
        }
    }

    enum FunctionType implements Template {
        PLAIN() {
            @Override
            public Stream<String> generate(int depth) {
                return sample(generateAll(depth, ReturnType.class))
                        .flatMap(r -> generateAll(depth, ArgListInternal.class).map(al -> "(" + al + ")" + r));
            }
        },
        VARARGS() {
            @Override
            public Stream<String> generate(int depth) {
                return sample(generateAll(depth, ReturnType.class))
                        .flatMap(r -> generateAll(depth, ArgListInternal.class).map(al -> "(" + al + "*)" + r));
            }
        }
    }

    enum ArgListInternal implements Template {
        ZERO() {
            @Override
            public Stream<String> generate(int depth) {
                return Stream.empty();
            }
        },
        ONE() {
            @Override
            public Stream<String> generate(int depth) {
                return generateAll(depth, ElementType.class);
            }
        },
        TWO() {
            @Override
            public Stream<String> generate(int depth) {
                return sample(generateAll(depth, ElementType.class))
                        .flatMap(e1 -> sample(generateAll(depth, ElementType.class)).map(e2 -> e1 + e2));
            }
        }
    }

    enum ValueType implements Template {
        PLAIN(ValueTypePrefix.class),
        POINTER(PointerType.class);

        Class<?> clazz;

        ValueType(Class<?> clazz) {
            this.clazz = clazz;
        }

        @Override
        public Stream<String> generate(int depth) {
            return generateAll(depth, cast(clazz));
        }
    }

    enum ValueTypePrefix implements Template {
        PLAIN() {
            @Override
            public Stream<String> generate(int depth) {
                return generateAll(depth, ScalarType.class);
            }
        },
        SUBGROUP() {
            @Override
            public Stream<String> generate(int depth) {
                return depth == 0 ?
                        PLAIN.generate(depth) :
                        generateAll(depth, ScalarType.class).flatMap(s ->
                                sample(generateAll(depth - 1, ContainerType.class)).map(c -> s + "=" + c));
            }
        }
    }

    enum ScalarType implements Template {
        INTEGRAL_UNSIGNED("u"),
        INTEGRAL_SIGNED("i"),
        FLOAT("f");

        String tag;

        ScalarType(String tag) {
            this.tag = tag;
        }

        @Override
        public Stream<String> generate(int depth) {
            return Stream.of(TestDescriptorGrammar.Endianness.values())
                    .flatMap(e -> bytes().map(n -> e.apply(tag) + n));
        }
    }

    enum PointerType implements Template {
        PLAIN() {
            @Override
            public Stream<String> generate(int depth) {
                return generateAll(depth - 1, ScalarType.class).map(s -> s + ":v");
            }
        },
        BREAKDOWN() {
            @Override
            public Stream<String> generate(int depth) {
                return depth == 0 ?
                        PLAIN.generate(depth) :
                        generateAll(depth - 1, ValueTypePrefix.class).flatMap(
                                s -> sample(generateAll(depth - 1, Descriptor.class)).map(e -> s + ":" + e));
            }
        }
    }

    enum Endianness implements Function<String, String> {
        BYTE_SWAP_LE,
        BYTE_SWAP_BE,
        NO_ENDIAN;

        @Override
        public String apply(String layout) {
            switch (this) {
                case BYTE_SWAP_BE:
                    return ">" + layout;
                case BYTE_SWAP_LE:
                    return "<" + layout;
                case NO_ENDIAN:
                    return layout;
            }
            throw new IllegalStateException("Should never reach here");
        }
    }
    
    static int SAMPLE_SIZE = 30;
    static int DIGITS_SIZE = 4;

    static <Z extends Enum<?> & Template> Stream<String> generateAll(int depth, Class<Z> enumClazz) {
        Entry e = new Entry(enumClazz, depth);
        List<String> res = templateCache.get(e);
        if (res == null) {
            res = Stream.of(enumClazz.getEnumConstants()).flatMap(t -> t.generate(depth)).collect(Collectors.toList());
            templateCache.put(e, res);
        }
        return res.stream();
    }

    //cache previously executed combo to save time
    static Map<Entry, List<String>> templateCache = new HashMap<>();

    static class Entry {
        Class<?> enumClass;
        Integer depth;

        Entry(Class<?> enumClass, Integer depth) {
            this.enumClass = enumClass;
            this.depth = depth;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry entry = (Entry) o;
            return Objects.equals(enumClass, entry.enumClass) &&
                    Objects.equals(depth, entry.depth);
        }

        @Override
        public int hashCode() {
            return Objects.hash(enumClass, depth);
        }
    }

    @SuppressWarnings("unchecked")
    static <Z extends Enum<?> & Template> Class<Z> cast(Class<?> cl) {
        return (Class<Z>)cl;
    }

    static Stream<String> digits(int lo) {
        return sampleInternal(IntStream.range(lo, Integer.MAX_VALUE)
                .mapToObj(String::valueOf), DIGITS_SIZE);
    }

    static Stream<String> bytes() {
        return digits(1).map(d -> "" + (Integer.valueOf(d) * 8));
    }

    static Stream<String> sample(Stream<String> ss) {
        return sampleInternal(ss, SAMPLE_SIZE);
    }

    static Stream<String> sampleInternal(Stream<String> ss, int size) {
        return ss.filter(s -> new Random().nextInt(10) >= 5)
                .limit(size);
    }

    static long checks = 0;

    public static void main(String[] args) {
        testDescriptor(generateAll(2, ElementType.class), DescriptorParser::parseLayout);
        testDescriptor(generateAll(2, FunctionType.class), DescriptorParser::parseFunction);
        System.err.println("Checks executed: " + checks);
    }

    static void testDescriptor(Stream<String> descriptors, Consumer<DescriptorParser> parserFunc) {
        descriptors.forEach(d -> {
            try {
                parserFunc.accept(new DescriptorParser(d));
                checks++;
            } catch (Throwable t) {
                throw new AssertionError("Cannot parse: " + d, t);
            }
        });
    }
}
