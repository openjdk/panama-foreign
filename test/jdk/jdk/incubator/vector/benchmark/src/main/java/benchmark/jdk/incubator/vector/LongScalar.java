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
public class LongScalar extends AbstractVectorBenchmark {
    static final int INVOC_COUNT = 1; // To align with vector benchmarks.

    @Param("1024")
    int size;

    long[] fill(IntFunction<Long> f) {
        long[] array = new long[size];
        for (int i = 0; i < array.length; i++) {
            array[i] = f.apply(i);
        }
        return array;
    }

    long[] as, bs, cs, rs;
    boolean[] ms, rms;
    int[] ss;

    @Setup
    public void init() {
        as = fill(i -> (long)(2*i));
        bs = fill(i -> (long)(i+1));
        cs = fill(i -> (long)(i+5));
        rs = fill(i -> (long)0);
        ms = fillMask(size, i -> (i % 2) == 0);
        rms = fillMask(size, i -> false);

        ss = fillInt(size, i -> RANDOM.nextInt(Math.max(i,1)));
    }

    final IntFunction<long[]> fa = vl -> as;
    final IntFunction<long[]> fb = vl -> bs;
    final IntFunction<long[]> fc = vl -> cs;
    final IntFunction<long[]> fr = vl -> rs;
    final IntFunction<boolean[]> fm = vl -> ms;
    final IntFunction<boolean[]> fmr = vl -> rms;
    final IntFunction<int[]> fs = vl -> ss;


    @Benchmark
    public void add(Blackhole bh) {
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);
        long[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                long b = bs[i];
                rs[i] = (long)(a + b);
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void addMasked(Blackhole bh) {
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);
        long[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                long b = bs[i];
                if (ms[i % ms.length]) {
                    rs[i] = (long)(a + b);
                } else {
                    rs[i] = a;
                }
            }
        }
        bh.consume(rs);
    }

    @Benchmark
    public void sub(Blackhole bh) {
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);
        long[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                long b = bs[i];
                rs[i] = (long)(a - b);
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void subMasked(Blackhole bh) {
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);
        long[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                long b = bs[i];
                if (ms[i % ms.length]) {
                    rs[i] = (long)(a - b);
                } else {
                    rs[i] = a;
                }
            }
        }
        bh.consume(rs);
    }



    @Benchmark
    public void mul(Blackhole bh) {
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);
        long[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                long b = bs[i];
                rs[i] = (long)(a * b);
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void mulMasked(Blackhole bh) {
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);
        long[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                long b = bs[i];
                if (ms[i % ms.length]) {
                    rs[i] = (long)(a * b);
                } else {
                    rs[i] = a;
                }
            }
        }
        bh.consume(rs);
    }


    @Benchmark
    public void and(Blackhole bh) {
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);
        long[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                long b = bs[i];
                rs[i] = (long)(a & b);
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void andMasked(Blackhole bh) {
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);
        long[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                long b = bs[i];
                if (ms[i % ms.length]) {
                    rs[i] = (long)(a & b);
                } else {
                    rs[i] = a;
                }
            }
        }
        bh.consume(rs);
    }



    @Benchmark
    public void or(Blackhole bh) {
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);
        long[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                long b = bs[i];
                rs[i] = (long)(a | b);
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void orMasked(Blackhole bh) {
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);
        long[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                long b = bs[i];
                if (ms[i % ms.length]) {
                    rs[i] = (long)(a | b);
                } else {
                    rs[i] = a;
                }
            }
        }
        bh.consume(rs);
    }



    @Benchmark
    public void xor(Blackhole bh) {
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);
        long[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                long b = bs[i];
                rs[i] = (long)(a ^ b);
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void xorMasked(Blackhole bh) {
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);
        long[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                long b = bs[i];
                if (ms[i % ms.length]) {
                    rs[i] = (long)(a ^ b);
                } else {
                    rs[i] = a;
                }
            }
        }
        bh.consume(rs);
    }



    @Benchmark
    public void shiftLeft(Blackhole bh) {
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);
        long[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                long b = bs[i];
                rs[i] = (long)((a << b));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void shiftLeftMasked(Blackhole bh) {
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);
        long[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                long b = bs[i];
                if (ms[i % ms.length]) {
                    rs[i] = (long)((a << b));
                } else {
                    rs[i] = a;
                }
            }
        }
        bh.consume(rs);
    }







    @Benchmark
    public void shiftRight(Blackhole bh) {
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);
        long[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                long b = bs[i];
                rs[i] = (long)((a >>> b));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void shiftRightMasked(Blackhole bh) {
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);
        long[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                long b = bs[i];
                if (ms[i % ms.length]) {
                    rs[i] = (long)((a >>> b));
                } else {
                    rs[i] = a;
                }
            }
        }
        bh.consume(rs);
    }







    @Benchmark
    public void shiftArithmeticRight(Blackhole bh) {
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);
        long[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                long b = bs[i];
                rs[i] = (long)((a >> b));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void shiftArithmeticRightMasked(Blackhole bh) {
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);
        long[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                long b = bs[i];
                if (ms[i % ms.length]) {
                    rs[i] = (long)((a >> b));
                } else {
                    rs[i] = a;
                }
            }
        }
        bh.consume(rs);
    }







    @Benchmark
    public void shiftLeftShift(Blackhole bh) {
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);
        long[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                long b = bs[i];
                rs[i] = (long)((a << b));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void shiftLeftMaskedShift(Blackhole bh) {
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);
        long[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                long b = bs[i];
                boolean m = ms[i % ms.length];
                rs[i] = (m ? (long)((a << b)) : a);
            }
        }

        bh.consume(rs);
    }







    @Benchmark
    public void shiftRightShift(Blackhole bh) {
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);
        long[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                long b = bs[i];
                rs[i] = (long)((a >>> b));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void shiftRightMaskedShift(Blackhole bh) {
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);
        long[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                long b = bs[i];
                boolean m = ms[i % ms.length];
                rs[i] = (m ? (long)((a >>> b)) : a);
            }
        }

        bh.consume(rs);
    }







    @Benchmark
    public void shiftArithmeticRightShift(Blackhole bh) {
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);
        long[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                long b = bs[i];
                rs[i] = (long)((a >> b));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void shiftArithmeticRightMaskedShift(Blackhole bh) {
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);
        long[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                long b = bs[i];
                boolean m = ms[i % ms.length];
                rs[i] = (m ? (long)((a >> b)) : a);
            }
        }

        bh.consume(rs);
    }






    @Benchmark
    public void max(Blackhole bh) {
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);
        long[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                long b = bs[i];
                rs[i] = (long)(Math.max(a, b));
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void maxMasked(Blackhole bh) {
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);
        long[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                long b = bs[i];
                if (ms[i % ms.length]) {
                    rs[i] = (long)(Math.max(a, b));
                } else {
                    rs[i] = a;
                }
            }
        }
        bh.consume(rs);
    }

    @Benchmark
    public void min(Blackhole bh) {
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);
        long[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                long b = bs[i];
                rs[i] = (long)(Math.min(a, b));
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void minMasked(Blackhole bh) {
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);
        long[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                long b = bs[i];
                if (ms[i % ms.length]) {
                    rs[i] = (long)(Math.min(a, b));
                } else {
                    rs[i] = a;
                }
            }
        }
        bh.consume(rs);
    }


    @Benchmark
    public void andLanes(Blackhole bh) {
        long[] as = fa.apply(size);
        long r = -1;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = -1;
            for (int i = 0; i < as.length; i++) {
                r &= as[i];
            }
        }
        bh.consume(r);
    }



    @Benchmark
    public void andLanesMasked(Blackhole bh) {
        long[] as = fa.apply(size);
        boolean[] ms = fm.apply(size);
        long r = -1;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = -1;
            for (int i = 0; i < as.length; i++) {
                if (ms[i % ms.length])
                    r &= as[i];
            }
        }
        bh.consume(r);
    }



    @Benchmark
    public void orLanes(Blackhole bh) {
        long[] as = fa.apply(size);
        long r = 0;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = 0;
            for (int i = 0; i < as.length; i++) {
                r |= as[i];
            }
        }
        bh.consume(r);
    }



    @Benchmark
    public void orLanesMasked(Blackhole bh) {
        long[] as = fa.apply(size);
        boolean[] ms = fm.apply(size);
        long r = 0;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = 0;
            for (int i = 0; i < as.length; i++) {
                if (ms[i % ms.length])
                    r |= as[i];
            }
        }
        bh.consume(r);
    }



    @Benchmark
    public void xorLanes(Blackhole bh) {
        long[] as = fa.apply(size);
        long r = 0;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = 0;
            for (int i = 0; i < as.length; i++) {
                r ^= as[i];
            }
        }
        bh.consume(r);
    }



    @Benchmark
    public void xorLanesMasked(Blackhole bh) {
        long[] as = fa.apply(size);
        boolean[] ms = fm.apply(size);
        long r = 0;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = 0;
            for (int i = 0; i < as.length; i++) {
                if (ms[i % ms.length])
                    r ^= as[i];
            }
        }
        bh.consume(r);
    }


    @Benchmark
    public void addLanes(Blackhole bh) {
        long[] as = fa.apply(size);
        long r = 0;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = 0;
            for (int i = 0; i < as.length; i++) {
                r += as[i];
            }
        }
        bh.consume(r);
    }

    @Benchmark
    public void addLanesMasked(Blackhole bh) {
        long[] as = fa.apply(size);
        boolean[] ms = fm.apply(size);
        long r = 0;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = 0;
            for (int i = 0; i < as.length; i++) {
                if (ms[i % ms.length])
                    r += as[i];
            }
        }
        bh.consume(r);
    }

    @Benchmark
    public void mulLanes(Blackhole bh) {
        long[] as = fa.apply(size);
        long r = 1;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = 1;
            for (int i = 0; i < as.length; i++) {
                r *= as[i];
            }
        }
        bh.consume(r);
    }

    @Benchmark
    public void mulLanesMasked(Blackhole bh) {
        long[] as = fa.apply(size);
        boolean[] ms = fm.apply(size);
        long r = 1;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = 1;
            for (int i = 0; i < as.length; i++) {
                if (ms[i % ms.length])
                    r *= as[i];
            }
        }
        bh.consume(r);
    }

    @Benchmark
    public void minLanes(Blackhole bh) {
        long[] as = fa.apply(size);
        long r = Long.MAX_VALUE;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = Long.MAX_VALUE;
            for (int i = 0; i < as.length; i++) {
                r = (long)Math.min(r, as[i]);
            }
        }
        bh.consume(r);
    }

    @Benchmark
    public void minLanesMasked(Blackhole bh) {
        long[] as = fa.apply(size);
        boolean[] ms = fm.apply(size);
        long r = Long.MAX_VALUE;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = Long.MAX_VALUE;
            for (int i = 0; i < as.length; i++) {
                if (ms[i % ms.length])
                    r = (long)Math.min(r, as[i]);
            }
        }
        bh.consume(r);
    }

    @Benchmark
    public void maxLanes(Blackhole bh) {
        long[] as = fa.apply(size);
        long r = Long.MIN_VALUE;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = Long.MIN_VALUE;
            for (int i = 0; i < as.length; i++) {
                r = (long)Math.max(r, as[i]);
            }
        }
        bh.consume(r);
    }

    @Benchmark
    public void maxLanesMasked(Blackhole bh) {
        long[] as = fa.apply(size);
        boolean[] ms = fm.apply(size);
        long r = Long.MIN_VALUE;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = Long.MIN_VALUE;
            for (int i = 0; i < as.length; i++) {
                if (ms[i % ms.length])
                    r = (long)Math.max(r, as[i]);
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
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);

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
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);

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
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);

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
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);

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
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);

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
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);

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
        long[] as = fa.apply(size);
        long[] bs = fb.apply(size);
        long[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                long b = bs[i];
                boolean m = ms[i % ms.length];
                rs[i] = (m ? b : a);
            }
        }

        bh.consume(rs);
    }
    void rearrangeShared(int window, Blackhole bh) {
        long[] as = fa.apply(size);
        int[] order = fs.apply(size);
        long[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i += window) {
                for (int j = 0; j < window; j++) {
                    long a = as[i+j];
                    int pos = order[j];
                    rs[i + pos] = a;
                }
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void rearrange064(Blackhole bh) {
        int window = 64 / Long.SIZE;
        rearrangeShared(window, bh);
    }

    @Benchmark
    public void rearrange128(Blackhole bh) {
        int window = 128 / Long.SIZE;
        rearrangeShared(window, bh);
    }

    @Benchmark
    public void rearrange256(Blackhole bh) {
        int window = 256 / Long.SIZE;
        rearrangeShared(window, bh);
    }

    @Benchmark
    public void rearrange512(Blackhole bh) {
        int window = 512 / Long.SIZE;
        rearrangeShared(window, bh);
    }





















    @Benchmark
    public void neg(Blackhole bh) {
        long[] as = fa.apply(size);
        long[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                rs[i] = (long)(-((long)a));
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void negMasked(Blackhole bh) {
        long[] as = fa.apply(size);
        long[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                boolean m = ms[i % ms.length];
                rs[i] = (m ? (long)(-((long)a)) : a);
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void abs(Blackhole bh) {
        long[] as = fa.apply(size);
        long[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                rs[i] = (long)(Math.abs((long)a));
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void absMasked(Blackhole bh) {
        long[] as = fa.apply(size);
        long[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                boolean m = ms[i % ms.length];
                rs[i] = (m ? (long)(Math.abs((long)a)) : a);
            }
        }

        bh.consume(rs);
    }


    @Benchmark
    public void not(Blackhole bh) {
        long[] as = fa.apply(size);
        long[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                rs[i] = (long)(~((long)a));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void notMasked(Blackhole bh) {
        long[] as = fa.apply(size);
        long[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                long a = as[i];
                boolean m = ms[i % ms.length];
                rs[i] = (m ? (long)(~((long)a)) : a);
            }
        }

        bh.consume(rs);
    }




    @Benchmark
    public void gatherBase0(Blackhole bh) {
        long[] as = fa.apply(size);
        int[] is    = fs.apply(size);
        long[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                int ix = 0 + is[i];
                rs[i] = as[ix];
            }
        }

        bh.consume(rs);
    }


    void gather(int window, Blackhole bh) {
        long[] as = fa.apply(size);
        int[] is    = fs.apply(size);
        long[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i += window) {
                for (int j = 0; j < window; j++) {
                    int ix = i + is[i + j];
                    rs[i + j] = as[ix];
                }
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void gather064(Blackhole bh) {
        int window = 64 / Long.SIZE;
        gather(window, bh);
    }

    @Benchmark
    public void gather128(Blackhole bh) {
        int window = 128 / Long.SIZE;
        gather(window, bh);
    }

    @Benchmark
    public void gather256(Blackhole bh) {
        int window = 256 / Long.SIZE;
        gather(window, bh);
    }

    @Benchmark
    public void gather512(Blackhole bh) {
        int window = 512 / Long.SIZE;
        gather(window, bh);
    }



    @Benchmark
    public void scatterBase0(Blackhole bh) {
        long[] as = fa.apply(size);
        int[] is    = fs.apply(size);
        long[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                int ix = 0 + is[i];
                rs[ix] = as[i];
            }
        }

        bh.consume(rs);
    }

    void scatter(int window, Blackhole bh) {
        long[] as = fa.apply(size);
        int[] is    = fs.apply(size);
        long[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i += window) {
                for (int j = 0; j < window; j++) {
                    int ix = i + is[i + j];
                    rs[ix] = as[i + j];
                }
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void scatter064(Blackhole bh) {
        int window = 64 / Long.SIZE;
        scatter(window, bh);
    }

    @Benchmark
    public void scatter128(Blackhole bh) {
        int window = 128 / Long.SIZE;
        scatter(window, bh);
    }

    @Benchmark
    public void scatter256(Blackhole bh) {
        int window = 256 / Long.SIZE;
        scatter(window, bh);
    }

    @Benchmark
    public void scatter512(Blackhole bh) {
        int window = 512 / Long.SIZE;
        scatter(window, bh);
    }

}

