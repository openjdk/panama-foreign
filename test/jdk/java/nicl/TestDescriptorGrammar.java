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
 * @bug 8192974
 * @summary overhaul descriptor string parsing
 * @modules java.base/jdk.internal.nicl.types
 * @run main/othervm -Xmx1g TestDescriptorGrammar
 */

import jdk.internal.nicl.types.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
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

    enum ArraySize implements Template {
        ANY() {
            @Override
            public Stream<String> generate(int depth) {
                return Collections.singletonList("*").stream();
            }
        },
        NUMBER() {
            @Override
            public Stream<String> generate(int depth) {
                return digits(2);
            }
        }
    }

    //Note: separators are not tested here
    enum ElementType implements Template {
        SCALAR_TYPE() {
            @Override
            public Stream<String> generate(int depth) {
                return generateAll(depth, ScalarType.class);
            }
        },
        CONTAINER_TYPE() {
            @Override
            public Stream<String> generate(int depth) {
                return generateAll(depth, ContainerType.class);
            }
        },
        ARRAY_TYPE() {
            @Override
            public Stream<String> generate(int depth) {
                return depth == 0 ?
                        IntegerTypeInternal.INT.generate(depth) :
                        generateAll(depth, ArraySize.class)
                            .flatMap(as -> generateAll(depth - 1, ElementType.class).map(e -> as + e));
            }
        }
    }

    enum ReturnType implements Template {
        ELEMENT_TYPE() {
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
        PLAIN() {
            @Override
            public Stream<String> generate(int depth) {
                return generateAll(depth, ContainerTypeInternal.class).map(e -> "[" + e + "]");
            }
        }
        //Todo: container endianness (not supported in the API)
    }

    enum ContainerTypeInternal implements Template {
        ELEMENT1() {
            @Override
            public Stream<String> generate(int depth) {
                return depth == 0 ?
                        IntegerTypeInternal.INT.generate(depth) :
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

    enum Endianness implements Template {
        BIG('>'),
        SMALL('<'),
        NATIVE('@');

        char tag;

        Endianness(char tag) {
            this.tag = tag;
        }

        @Override
        public Stream<String> generate(int depth) {
            return Collections.singletonList(tag + "").stream();
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

    enum ScalarType implements Template {
        INTEGER(IntegerType.class),
        REAL(RealType.class),
        POINTER(PointerType.class),
        SIZED(SizedType.class),
        MISC(MiscType.class),
        BITFIELD(BitFieldType.class);

        Class<?> clazz;

        ScalarType(Class<?> clazz) {
            this.clazz = clazz;
        }

        @Override
        public Stream<String> generate(int depth) {
            return generateAll(depth, cast(clazz));
        }
    }

    enum MiscType implements Template {
        BOOLEAN('B'),
        PADDING('x'),
        CHAR('c');

        char tag;

        MiscType(char tag) {
            this.tag = tag;
        }

        @Override
        public Stream<String> generate(int depth) {
            return Collections.singletonList(tag + "").stream();
        }
    }

    enum IntegerType implements Template {
        PLAIN() {
            @Override
            public Stream<String> generate(int depth) {
                return generateAll(depth, IntegerTypeInternal.class);
            }
        },
        ENDIANNESS() {
            @Override
            public Stream<String> generate(int depth) {
                return PLAIN.generate(depth)
                        .flatMap(i -> generateAll(depth, Endianness.class).map(e -> e + i));
            }
        }
    }

    enum IntegerTypeInternal implements Template {
        STANDARD_SIZE() {
            @Override
            public Stream<String> generate(int depth) {
                return generateAll(depth, StandardSizeType.class);
            }
        },
        INT() {
            @Override
            public Stream<String> generate(int depth) {
                return Collections.singletonList("i").stream();
            }
        }
    }

    enum RealType implements Template {
        PLAIN() {
            @Override
            public Stream<String> generate(int depth) {
                return generateAll(depth, RealTypeInternal.class);
            }
        },
        ENDIANNESS() {
            @Override
            public Stream<String> generate(int depth) {
                return PLAIN.generate(depth)
                        .flatMap(i -> generateAll(depth, Endianness.class).map(e -> e + i));
            }
        }
    }

    enum RealTypeInternal implements Template {
        FLOAT('f'),
        DOUBLE('d'),
        LONG_DOUBLE('e');

        char tag;

        RealTypeInternal(char tag) {
            this.tag = tag;
        }

        @Override
        public Stream<String> generate(int depth) {
            return Collections.singletonList(tag + "").stream();
        }
    }

    enum StandardSizeType implements Template {
        OCTET('o'),
        SHORT('s'),
        LONG('l'),
        LONG_LONG('q'),
        UNSIGNED_OCTET('O'),
        UNSIGNED_SHORT('S'),
        UNSIGNED_LONG('L'),
        UNSIGNED_LONG_LONG('Q');

        char tag;

        StandardSizeType(char tag) {
            this.tag = tag;
        }

        @Override
        public Stream<String> generate(int depth) {
            return Collections.singletonList(tag + "").stream();
        }
    }

    enum PointerType implements Template {
        PLAIN() {
            @Override
            public Stream<String> generate(int depth) {
                return Collections.singletonList("p").stream();
            }
        },
        BREAKDOWN() {
            @Override
            public Stream<String> generate(int depth) {
                return depth == 0 ?
                        PLAIN.generate(depth) :
                        sample(generateAll(depth - 1, PointeeType.class)).map(e -> "p:" + e);
            }
        }
    }

    enum PointeeType implements Template {
        DESCRIPTOR() {
            @Override
            public Stream<String> generate(int depth) {
                return generateAll(depth, Descriptor.class);
            }
        },

        VOID() {
            @Override
            public Stream<String> generate(int depth) {
                return Stream.of("V");
            }
        }
    }

    enum SizedType implements Template {
        PLAIN() {
            @Override
            public Stream<String> generate(int depth) {
                return generateAll(depth, SizedTypeInternal.class);
            }
        },
        ENDIANNES() {
            @Override
            public Stream<String> generate(int depth) {
                return PLAIN.generate(depth)
                        .flatMap(i -> generateAll(depth, Endianness.class).map(e -> e + i));
            }
        }
    }

    enum SizedTypeInternal implements Template {
        STANDARD() {
            @Override
            public Stream<String> generate(int depth) {
                return generateAll(depth, StandardSizeType.class)
                        .map(s -> "=" + s);
            }
        },
        EXPLICIT() {
            @Override
            public Stream<String> generate(int depth) {
                return digits(1).flatMap(n ->
                        generateAll(depth, IntegerTypeInternal.INT, RealTypeInternal.FLOAT)
                                .map(tag -> "=" + n + tag));

            }
        }
    }

    enum BitFieldType implements Template {
        ONE() {
            @Override
            public Stream<String> generate(int depth) {
                return generateAll(depth, IntegerTypeInternal.class).flatMap(t ->
                        generateAll(depth, BitFieldTypeInternal.class).flatMap(f -> Stream.of(t + ":" + f)));
            }
        },
        TWO() {
            @Override
            public Stream<String> generate(int depth) {
                return ONE.generate(depth).flatMap(prev ->
                        generateAll(depth, BitFieldTypeInternal.class).flatMap(f -> Stream.of(prev + f)));
            }
        },
        THREE() {
            @Override
            public Stream<String> generate(int depth) {
                return TWO.generate(depth).flatMap(prev ->
                        generateAll(depth, BitFieldTypeInternal.class).flatMap(f -> Stream.of(prev + f)));
            }
        }
    }

    enum BitFieldTypeInternal implements Template {
        SIZE() {
            @Override
            public Stream<String> generate(int depth) {
                return digits(1).map(d -> d + "b");
            }
        },
        NO_SIZE() {
            @Override
            public Stream<String> generate(int depth) {
                return Stream.of("b");
            }
        }
    }
    
    static int SAMPLE_SIZE = 40;
    static int DIGITS_SIZE = 4;

    static <Z extends Template> Stream<String> generateAll(int depth, Z... zs) {
        return Stream.of(zs).flatMap(t -> t.generate(depth));
    }

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

    static Stream<String> sample(Stream<String> ss) {
        return sampleInternal(ss, SAMPLE_SIZE);
    }

    static Stream<String> sampleInternal(Stream<String> ss, int size) {
        return ss.filter(s -> new Random().nextInt(10) >= 5)
                .limit(size);
    }

    static long checks = 0;

    public static void main(String[] args) {
        generateAll(2, Descriptor.class).forEach(TestDescriptorGrammar::testDescriptor);
        System.err.println("Checks executed: " + checks);
    }

    static void testDescriptor(String layout) {
        try {
            new DescriptorParser(layout).parseDescriptorOrLayouts();
            checks++;
        } catch (Throwable t) {
            throw new AssertionError("Cannot parse: " + layout, t);
        }
    }
}
