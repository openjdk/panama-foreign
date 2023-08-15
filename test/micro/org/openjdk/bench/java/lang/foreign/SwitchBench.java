/*
 * Copyright (c) 2020, 2022, Oracle and/or its affiliates. All rights reserved.
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
package org.openjdk.bench.java.lang.foreign;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@State(org.openjdk.jmh.annotations.Scope.Thread)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(3)
public class SwitchBench  {

    /*
    Benchmark                       Mode  Cnt  Score   Error  Units
SwitchBench.ifOnInstancesUtf32  avgt   30  0.654 ? 0.038  ns/op
SwitchBench.ifOnInstancesUtf8   avgt   30  0.576 ? 0.018  ns/op
SwitchBench.ifOnStringsUtf32    avgt   30  3.923 ? 0.015  ns/op
SwitchBench.ifOnStringsUtf8     avgt   30  0.597 ? 0.018  ns/op
SwitchBench.switchOnNamesUtf32  avgt   30  1.059 ? 0.005  ns/op
SwitchBench.switchOnNamesUtf8   avgt   30  1.058 ? 0.004  ns/op
     */

/*    @Benchmark
    public int switchOnNamesUtf8() {
        return CharsetKindSwitch.of(StandardCharsets.UTF_8).terminatorCharSize();
    }

    @Benchmark
    public int switchOnNamesUtf32() {
        return CharsetKindSwitch.of(StandardCharsets.UTF_32).terminatorCharSize();
    }

    @Benchmark
    public int ifOnInstancesUtf8() {
        return CharsetKindIfInstance.of(StandardCharsets.UTF_8).terminatorCharSize();
    }

    @Benchmark
    public int ifOnInstancesUtf32() {
        return CharsetKindIfInstance.of(StandardCharsets.UTF_32).terminatorCharSize();
    }

    @Benchmark
    public int ifOnStringsUtf8() {
        return CharsetKindIfString.of(StandardCharsets.UTF_8).terminatorCharSize();
    }

    @Benchmark
    public int ifOnStringsUtf32() {
        return CharsetKindIfString.of(StandardCharsets.UTF_32).terminatorCharSize();
    }*/

    @Benchmark
    public int optimizedUtf8() {
        return CharsetKindOptimized.of(StandardCharsets.UTF_8).terminatorCharSize();
    }

    @Benchmark
    public int optimizedUtf32() {
        return CharsetKindOptimized.of(CharSets.UTF_32).terminatorCharSize();
    }

    public enum CharsetKindSwitch {
        SINGLE_BYTE(1),
        DOUBLE_BYTE(2),
        QUAD_BYTE(4);

        final int terminatorCharSize;

        CharsetKindSwitch(int terminatorCharSize) {
            this.terminatorCharSize = terminatorCharSize;
        }

        public int terminatorCharSize() {
            return terminatorCharSize;
        }

        public static CharsetKindSwitch of(Charset charset) {
            // Switching on the charset names rather than specific instances of
            // `Charset` avoids loading the class `StandardCharsets`
            return switch (charset.name()) {
                case "UTF-8", "ISO8859_1", "US-ASCII" -> SINGLE_BYTE;
                case "UTF-16LE", "UTF-16BE", "UTF-16" -> DOUBLE_BYTE;
                case "UTF-32LE", "UTF-32BE", "UTF-32" -> QUAD_BYTE;
                default -> throw new UnsupportedOperationException("Unsupported charset: " + charset);
            };
        }
    }

    public enum CharsetKindIfInstance {
        SINGLE_BYTE(1),
        DOUBLE_BYTE(2),
        QUAD_BYTE(4);

        final int terminatorCharSize;

        CharsetKindIfInstance(int terminatorCharSize) {
            this.terminatorCharSize = terminatorCharSize;
        }

        public int terminatorCharSize() {
            return terminatorCharSize;
        }

        public static CharsetKindIfInstance of(Charset charset) {
            if (charset == StandardCharsets.UTF_8 || charset == StandardCharsets.ISO_8859_1 || charset == StandardCharsets.US_ASCII) {
                return CharsetKindIfInstance.SINGLE_BYTE;
            } else if (charset == StandardCharsets.UTF_16LE || charset == StandardCharsets.UTF_16BE || charset == StandardCharsets.UTF_16) {
                return CharsetKindIfInstance.DOUBLE_BYTE;
            } else if (charset == StandardCharsets.UTF_32LE || charset == StandardCharsets.UTF_32BE || charset == StandardCharsets.UTF_32) {
                return CharsetKindIfInstance.QUAD_BYTE;
            } else {
                throw new UnsupportedOperationException("Unsupported charset: " + charset);
            }
        }
    }

    public enum CharsetKindIfString {
        SINGLE_BYTE(1),
        DOUBLE_BYTE(2),
        QUAD_BYTE(4);

        final int terminatorCharSize;

        CharsetKindIfString(int terminatorCharSize) {
            this.terminatorCharSize = terminatorCharSize;
        }

        public int terminatorCharSize() {
            return terminatorCharSize;
        }

        public static CharsetKindIfInstance of(Charset charset) {
            String name = charset.name();
            if ("UTF-8".equals(name) || "ISO8859_1".equals(name) || "US-ASCII".equals(name)) {
                return CharsetKindIfInstance.SINGLE_BYTE;
            } else if ("UTF-16LE".equals(name) || "UTF-16BE".equals(name) || "UTF-16".equals(name)) {
                return CharsetKindIfInstance.DOUBLE_BYTE;
            } else if ("UTF-32LE".equals(name) || "UTF-32BE".equals(name) || "UTF-32".equals(name)) {
                return CharsetKindIfInstance.QUAD_BYTE;
            } else {
                throw new UnsupportedOperationException("Unsupported charset: " + charset);
            }
        }
    }


    public static final class CharSets {

        private static final Charset UTF_16LE = new UTF_16LE();
        private static final Charset UTF_16BE = new UTF_16BE();
        private static final Charset UTF_16 = new UTF_16();
        private static final Charset UTF_32LE = new UTF_32LE();
        private static final Charset UTF_32BE = new UTF_32BE();
        private static final Charset UTF_32 = new UTF_32();

        public static final class UTF_16LE extends Charset {

            public UTF_16LE() {
                super("UTF_16LE", null);
            }

            @Override
            public boolean contains(Charset cs) {
                return false;
            }

            @Override
            public CharsetDecoder newDecoder() {
                return null;
            }

            @Override
            public CharsetEncoder newEncoder() {
                return null;
            }
        }

        public static final class UTF_16BE extends Charset {

            public UTF_16BE() {
                super("UTF_16BE", null);
            }

            @Override
            public boolean contains(Charset cs) {
                return false;
            }

            @Override
            public CharsetDecoder newDecoder() {
                return null;
            }

            @Override
            public CharsetEncoder newEncoder() {
                return null;
            }
        }

        public static final class UTF_16 extends Charset {

            public UTF_16() {
                super("UTF_16", null);
            }

            @Override
            public boolean contains(Charset cs) {
                return false;
            }

            @Override
            public CharsetDecoder newDecoder() {
                return null;
            }

            @Override
            public CharsetEncoder newEncoder() {
                return null;
            }
        }

        public static final class UTF_32LE extends Charset {

            public UTF_32LE() {
                super("UTF_32LE", null);
            }

            @Override
            public boolean contains(Charset cs) {
                return false;
            }

            @Override
            public CharsetDecoder newDecoder() {
                return null;
            }

            @Override
            public CharsetEncoder newEncoder() {
                return null;
            }
        }

        public static final class UTF_32BE extends Charset {

            public UTF_32BE() {
                super("UTF_32BE", null);
            }

            @Override
            public boolean contains(Charset cs) {
                return false;
            }

            @Override
            public CharsetDecoder newDecoder() {
                return null;
            }

            @Override
            public CharsetEncoder newEncoder() {
                return null;
            }
        }

        public static final class UTF_32 extends Charset {

            public UTF_32() {
                super("UTF_32", null);
            }

            @Override
            public boolean contains(Charset cs) {
                return false;
            }

            @Override
            public CharsetDecoder newDecoder() {
                return null;
            }

            @Override
            public CharsetEncoder newEncoder() {
                return null;
            }
        }

    }

    public enum CharsetKindOptimized {
        SINGLE_BYTE(1),
        DOUBLE_BYTE(2),
        QUAD_BYTE(4);

        final int terminatorCharSize;

        CharsetKindOptimized(int terminatorCharSize) {
            this.terminatorCharSize = terminatorCharSize;
        }

        public int terminatorCharSize() {
            return terminatorCharSize;
        }

        public static CharsetKindIfInstance of(Charset charset) {
            if (charset == StandardCharsets.UTF_8) {
                // Fast path for the most likely charset
                return CharsetKindIfInstance.SINGLE_BYTE;
            }
            if (charset == StandardCharsets.ISO_8859_1 || charset == StandardCharsets.US_ASCII) {
                return CharsetKindIfInstance.SINGLE_BYTE;
            } else if (charset instanceof CharSets.UTF_16LE || charset instanceof CharSets.UTF_16BE || charset instanceof CharSets.UTF_16) {
                return CharsetKindIfInstance.DOUBLE_BYTE;
            } else if (charset instanceof CharSets.UTF_32LE || charset instanceof CharSets.UTF_32BE || charset instanceof CharSets.UTF_32) {
                return CharsetKindIfInstance.QUAD_BYTE;
            } else {
                throw new UnsupportedOperationException("Unsupported charset: " + charset);
            }
        }
    }

}
