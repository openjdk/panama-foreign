/*
 * Copyright (c) 2018, 2019, Oracle and/or its affiliates. All rights reserved.
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
 * or visit www.oracle.com if you need additional information or have
 * questions.
 */

package benchmark.jdk.incubator.vector;

import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(value = 1, jvmArgsPrepend = {"--add-modules=jdk.incubator.vector"})
public class ShortScalar extends AbstractVectorBenchmark {
    static final int INVOC_COUNT = 1; // To align with vector benchmarks.

    @Param("1024")
    int size;

    short[] fill(IntFunction<Short> f) {
        short[] array = new short[size];
        for (int i = 0; i < array.length; i++) {
            array[i] = f.apply(i);
        }
        return array;
    }

    short[] as, bs, cs, rs;
    boolean[] ms, rms;
    int[] ss;

    @Setup
    public void init() {
        as = fill(i -> (short)(2*i));
        bs = fill(i -> (short)(i+1));
        cs = fill(i -> (short)(i+5));
        rs = fill(i -> (short)0);
        ms = fillMask(size, i -> (i % 2) == 0);
        rms = fillMask(size, i -> false);

        ss = fillInt(size, i -> RANDOM.nextInt(Math.max(i,1)));
    }

    final IntFunction<short[]> fa = vl -> as;
    final IntFunction<short[]> fb = vl -> bs;
    final IntFunction<short[]> fc = vl -> cs;
    final IntFunction<short[]> fr = vl -> rs;
    final IntFunction<boolean[]> fm = vl -> ms;
    final IntFunction<boolean[]> fmr = vl -> rms;
    final IntFunction<int[]> fs = vl -> ss;


    @Benchmark
    public void add(Blackhole bh) {
        short[] as = fa.apply(size);
        short[] bs = fb.apply(size);
        short[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                short a = as[i];
                short b = bs[i];
                rs[i] = (short)(a + b);
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void addMasked(Blackhole bh) {
        short[] as = fa.apply(size);
        short[] bs = fb.apply(size);
        short[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                short a = as[i];
                short b = bs[i];
                if (ms[i % ms.length]) {
                    rs[i] = (short)(a + b);
                } else {
                    rs[i] = a;
                }
            }
        }
        bh.consume(rs);
    }

    @Benchmark
    public void sub(Blackhole bh) {
        short[] as = fa.apply(size);
        short[] bs = fb.apply(size);
        short[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                short a = as[i];
                short b = bs[i];
                rs[i] = (short)(a - b);
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void subMasked(Blackhole bh) {
        short[] as = fa.apply(size);
        short[] bs = fb.apply(size);
        short[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                short a = as[i];
                short b = bs[i];
                if (ms[i % ms.length]) {
                    rs[i] = (short)(a - b);
                } else {
                    rs[i] = a;
                }
            }
        }
        bh.consume(rs);
    }



    @Benchmark
    public void mul(Blackhole bh) {
        short[] as = fa.apply(size);
        short[] bs = fb.apply(size);
        short[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                short a = as[i];
                short b = bs[i];
                rs[i] = (short)(a * b);
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void mulMasked(Blackhole bh) {
        short[] as = fa.apply(size);
        short[] bs = fb.apply(size);
        short[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                short a = as[i];
                short b = bs[i];
                if (ms[i % ms.length]) {
                    rs[i] = (short)(a * b);
                } else {
                    rs[i] = a;
                }
            }
        }
        bh.consume(rs);
    }


    @Benchmark
    public void and(Blackhole bh) {
        short[] as = fa.apply(size);
        short[] bs = fb.apply(size);
        short[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                short a = as[i];
                short b = bs[i];
                rs[i] = (short)(a & b);
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void andMasked(Blackhole bh) {
        short[] as = fa.apply(size);
        short[] bs = fb.apply(size);
        short[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                short a = as[i];
                short b = bs[i];
                if (ms[i % ms.length]) {
                    rs[i] = (short)(a & b);
                } else {
                    rs[i] = a;
                }
            }
        }
        bh.consume(rs);
    }



    @Benchmark
    public void or(Blackhole bh) {
        short[] as = fa.apply(size);
        short[] bs = fb.apply(size);
        short[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                short a = as[i];
                short b = bs[i];
                rs[i] = (short)(a | b);
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void orMasked(Blackhole bh) {
        short[] as = fa.apply(size);
        short[] bs = fb.apply(size);
        short[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                short a = as[i];
                short b = bs[i];
                if (ms[i % ms.length]) {
                    rs[i] = (short)(a | b);
                } else {
                    rs[i] = a;
                }
            }
        }
        bh.consume(rs);
    }



    @Benchmark
    public void xor(Blackhole bh) {
        short[] as = fa.apply(size);
        short[] bs = fb.apply(size);
        short[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                short a = as[i];
                short b = bs[i];
                rs[i] = (short)(a ^ b);
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void xorMasked(Blackhole bh) {
        short[] as = fa.apply(size);
        short[] bs = fb.apply(size);
        short[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                short a = as[i];
                short b = bs[i];
                if (ms[i % ms.length]) {
                    rs[i] = (short)(a ^ b);
                } else {
                    rs[i] = a;
                }
            }
        }
        bh.consume(rs);
    }





















    @Benchmark
    public void aShiftRShift(Blackhole bh) {
        short[] as = fa.apply(size);
        short[] bs = fb.apply(size);
        short[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                short a = as[i];
                short b = bs[i];
                rs[i] = (short)((a >> (b & 15)));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void aShiftRMaskedShift(Blackhole bh) {
        short[] as = fa.apply(size);
        short[] bs = fb.apply(size);
        short[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                short a = as[i];
                short b = bs[i];
                boolean m = ms[i % ms.length];
                rs[i] = (m ? (short)((a >> (b & 15))) : a);
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void shiftLShift(Blackhole bh) {
        short[] as = fa.apply(size);
        short[] bs = fb.apply(size);
        short[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                short a = as[i];
                short b = bs[i];
                rs[i] = (short)((a << (b & 15)));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void shiftLMaskedShift(Blackhole bh) {
        short[] as = fa.apply(size);
        short[] bs = fb.apply(size);
        short[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                short a = as[i];
                short b = bs[i];
                boolean m = ms[i % ms.length];
                rs[i] = (m ? (short)((a << (b & 15))) : a);
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void shiftRShift(Blackhole bh) {
        short[] as = fa.apply(size);
        short[] bs = fb.apply(size);
        short[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                short a = as[i];
                short b = bs[i];
                rs[i] = (short)(((a & 0xFFFF) >>> (b & 15)));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void shiftRMaskedShift(Blackhole bh) {
        short[] as = fa.apply(size);
        short[] bs = fb.apply(size);
        short[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                short a = as[i];
                short b = bs[i];
                boolean m = ms[i % ms.length];
                rs[i] = (m ? (short)(((a & 0xFFFF) >>> (b & 15))) : a);
            }
        }

        bh.consume(rs);
    }


    @Benchmark
    public void max(Blackhole bh) {
        short[] as = fa.apply(size);
        short[] bs = fb.apply(size);
        short[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                short a = as[i];
                short b = bs[i];
                rs[i] = (short)(Math.max(a, b));
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void min(Blackhole bh) {
        short[] as = fa.apply(size);
        short[] bs = fb.apply(size);
        short[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                short a = as[i];
                short b = bs[i];
                rs[i] = (short)(Math.min(a, b));
            }
        }

        bh.consume(rs);
    }


    @Benchmark
    public void andAll(Blackhole bh) {
        short[] as = fa.apply(size);
        short r = -1;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = -1;
            for (int i = 0; i < as.length; i++) {
                r &= as[i];
            }
        }
        bh.consume(r);
    }



    @Benchmark
    public void orAll(Blackhole bh) {
        short[] as = fa.apply(size);
        short r = 0;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = 0;
            for (int i = 0; i < as.length; i++) {
                r |= as[i];
            }
        }
        bh.consume(r);
    }



    @Benchmark
    public void xorAll(Blackhole bh) {
        short[] as = fa.apply(size);
        short r = 0;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = 0;
            for (int i = 0; i < as.length; i++) {
                r ^= as[i];
            }
        }
        bh.consume(r);
    }


    @Benchmark
    public void addAll(Blackhole bh) {
        short[] as = fa.apply(size);
        short r = 0;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = 0;
            for (int i = 0; i < as.length; i++) {
                r += as[i];
            }
        }
        bh.consume(r);
    }

    @Benchmark
    public void mulAll(Blackhole bh) {
        short[] as = fa.apply(size);
        short r = 1;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = 1;
            for (int i = 0; i < as.length; i++) {
                r *= as[i];
            }
        }
        bh.consume(r);
    }

    @Benchmark
    public void minAll(Blackhole bh) {
        short[] as = fa.apply(size);
        short r = Short.MAX_VALUE;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = Short.MAX_VALUE;
            for (int i = 0; i < as.length; i++) {
                r = (short)Math.min(r, as[i]);
            }
        }
        bh.consume(r);
    }

    @Benchmark
    public void maxAll(Blackhole bh) {
        short[] as = fa.apply(size);
        short r = Short.MIN_VALUE;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = Short.MIN_VALUE;
            for (int i = 0; i < as.length; i++) {
                r = (short)Math.max(r, as[i]);
            }
        }
        bh.consume(r);
    }


    @Benchmark
    public void anyTrue(Blackhole bh) {
        boolean[] ms = fm.apply(size);
        boolean r = false;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = false;
            for (int i = 0; i < ms.length; i++) {
                r |= ms[i];
            }
        }
        bh.consume(r);
    }



    @Benchmark
    public void allTrue(Blackhole bh) {
        boolean[] ms = fm.apply(size);
        boolean r = true;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = true;
            for (int i = 0; i < ms.length; i++) {
                r &= ms[i];
            }
        }
        bh.consume(r);
    }


    @Benchmark
    public void lessThan(Blackhole bh) {
        short[] as = fa.apply(size);
        short[] bs = fb.apply(size);

        boolean r = false;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = false;
            for (int i = 0; i < as.length; i++) {
                boolean m = (as[i] < bs[i]);
                r |= m; // accumulate so JIT can't eliminate the computation
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void greaterThan(Blackhole bh) {
        short[] as = fa.apply(size);
        short[] bs = fb.apply(size);

        boolean r = false;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = false;
            for (int i = 0; i < as.length; i++) {
                boolean m = (as[i] > bs[i]);
                r |= m; // accumulate so JIT can't eliminate the computation
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void equal(Blackhole bh) {
        short[] as = fa.apply(size);
        short[] bs = fb.apply(size);

        boolean r = false;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = false;
            for (int i = 0; i < as.length; i++) {
                boolean m = (as[i] == bs[i]);
                r |= m; // accumulate so JIT can't eliminate the computation
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void notEqual(Blackhole bh) {
        short[] as = fa.apply(size);
        short[] bs = fb.apply(size);

        boolean r = false;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = false;
            for (int i = 0; i < as.length; i++) {
                boolean m = (as[i] != bs[i]);
                r |= m; // accumulate so JIT can't eliminate the computation
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void lessThanEq(Blackhole bh) {
        short[] as = fa.apply(size);
        short[] bs = fb.apply(size);

        boolean r = false;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = false;
            for (int i = 0; i < as.length; i++) {
                boolean m = (as[i] <= bs[i]);
                r |= m; // accumulate so JIT can't eliminate the computation
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void greaterThanEq(Blackhole bh) {
        short[] as = fa.apply(size);
        short[] bs = fb.apply(size);

        boolean r = false;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = false;
            for (int i = 0; i < as.length; i++) {
                boolean m = (as[i] >= bs[i]);
                r |= m; // accumulate so JIT can't eliminate the computation
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void blend(Blackhole bh) {
        short[] as = fa.apply(size);
        short[] bs = fb.apply(size);
        short[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                short a = as[i];
                short b = bs[i];
                boolean m = ms[i % ms.length];
                rs[i] = (m ? b : a);
            }
        }

        bh.consume(rs);
    }
    void rearrangeShared(int window, Blackhole bh) {
        short[] as = fa.apply(size);
        int[] order = fs.apply(size);
        short[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i += window) {
                for (int j = 0; j < window; j++) {
                    short a = as[i+j];
                    int pos = order[j];
                    rs[i + pos] = a;
                }
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void rearrange064(Blackhole bh) {
        int window = 64 / Short.SIZE;
        rearrangeShared(window, bh);
    }

    @Benchmark
    public void rearrange128(Blackhole bh) {
        int window = 128 / Short.SIZE;
        rearrangeShared(window, bh);
    }

    @Benchmark
    public void rearrange256(Blackhole bh) {
        int window = 256 / Short.SIZE;
        rearrangeShared(window, bh);
    }

    @Benchmark
    public void rearrange512(Blackhole bh) {
        int window = 512 / Short.SIZE;
        rearrangeShared(window, bh);
    }





















    @Benchmark
    public void neg(Blackhole bh) {
        short[] as = fa.apply(size);
        short[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                short a = as[i];
                rs[i] = (short)(-((short)a));
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void negMasked(Blackhole bh) {
        short[] as = fa.apply(size);
        short[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                short a = as[i];
                boolean m = ms[i % ms.length];
                rs[i] = (m ? (short)(-((short)a)) : a);
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void abs(Blackhole bh) {
        short[] as = fa.apply(size);
        short[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                short a = as[i];
                rs[i] = (short)(Math.abs((short)a));
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void absMasked(Blackhole bh) {
        short[] as = fa.apply(size);
        short[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                short a = as[i];
                boolean m = ms[i % ms.length];
                rs[i] = (m ? (short)(Math.abs((short)a)) : a);
            }
        }

        bh.consume(rs);
    }


    @Benchmark
    public void not(Blackhole bh) {
        short[] as = fa.apply(size);
        short[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                short a = as[i];
                rs[i] = (short)(~((short)a));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void notMasked(Blackhole bh) {
        short[] as = fa.apply(size);
        short[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                short a = as[i];
                boolean m = ms[i % ms.length];
                rs[i] = (m ? (short)(~((short)a)) : a);
            }
        }

        bh.consume(rs);
    }





}

