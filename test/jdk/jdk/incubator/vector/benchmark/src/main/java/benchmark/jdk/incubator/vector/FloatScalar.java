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
public class FloatScalar extends AbstractVectorBenchmark {
    static final int INVOC_COUNT = 1; // To align with vector benchmarks.

    @Param("1024")
    int size;

    float[] fill(IntFunction<Float> f) {
        float[] array = new float[size];
        for (int i = 0; i < array.length; i++) {
            array[i] = f.apply(i);
        }
        return array;
    }

    float[] as, bs, cs, rs;
    boolean[] ms, rms;
    int[] ss;

    @Setup
    public void init() {
        as = fill(i -> (float)(2*i));
        bs = fill(i -> (float)(i+1));
        cs = fill(i -> (float)(i+5));
        rs = fill(i -> (float)0);
        ms = fillMask(size, i -> (i % 2) == 0);
        rms = fillMask(size, i -> false);

        ss = fillInt(size, i -> RANDOM.nextInt(Math.max(i,1)));
    }

    final IntFunction<float[]> fa = vl -> as;
    final IntFunction<float[]> fb = vl -> bs;
    final IntFunction<float[]> fc = vl -> cs;
    final IntFunction<float[]> fr = vl -> rs;
    final IntFunction<boolean[]> fm = vl -> ms;
    final IntFunction<boolean[]> fmr = vl -> rms;
    final IntFunction<int[]> fs = vl -> ss;


    @Benchmark
    public void add(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] bs = fb.apply(size);
        float[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                float b = bs[i];
                rs[i] = (float)(a + b);
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void addMasked(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] bs = fb.apply(size);
        float[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                float b = bs[i];
                if (ms[i % ms.length]) {
                    rs[i] = (float)(a + b);
                } else {
                    rs[i] = a;
                }
            }
        }
        bh.consume(rs);
    }

    @Benchmark
    public void sub(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] bs = fb.apply(size);
        float[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                float b = bs[i];
                rs[i] = (float)(a - b);
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void subMasked(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] bs = fb.apply(size);
        float[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                float b = bs[i];
                if (ms[i % ms.length]) {
                    rs[i] = (float)(a - b);
                } else {
                    rs[i] = a;
                }
            }
        }
        bh.consume(rs);
    }


    @Benchmark
    public void div(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] bs = fb.apply(size);
        float[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                float b = bs[i];
                rs[i] = (float)(a / b);
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void divMasked(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] bs = fb.apply(size);
        float[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                float b = bs[i];
                if (ms[i % ms.length]) {
                    rs[i] = (float)(a / b);
                } else {
                    rs[i] = a;
                }
            }
        }
        bh.consume(rs);
    }


    @Benchmark
    public void mul(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] bs = fb.apply(size);
        float[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                float b = bs[i];
                rs[i] = (float)(a * b);
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void mulMasked(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] bs = fb.apply(size);
        float[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                float b = bs[i];
                if (ms[i % ms.length]) {
                    rs[i] = (float)(a * b);
                } else {
                    rs[i] = a;
                }
            }
        }
        bh.consume(rs);
    }











































    @Benchmark
    public void max(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] bs = fb.apply(size);
        float[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                float b = bs[i];
                rs[i] = (float)(Math.max(a, b));
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void min(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] bs = fb.apply(size);
        float[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                float b = bs[i];
                rs[i] = (float)(Math.min(a, b));
            }
        }

        bh.consume(rs);
    }




    @Benchmark
    public void addLanes(Blackhole bh) {
        float[] as = fa.apply(size);
        float r = 0;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = 0;
            for (int i = 0; i < as.length; i++) {
                r += as[i];
            }
        }
        bh.consume(r);
    }

    @Benchmark
    public void mulLanes(Blackhole bh) {
        float[] as = fa.apply(size);
        float r = 1;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = 1;
            for (int i = 0; i < as.length; i++) {
                r *= as[i];
            }
        }
        bh.consume(r);
    }

    @Benchmark
    public void minLanes(Blackhole bh) {
        float[] as = fa.apply(size);
        float r = Float.POSITIVE_INFINITY;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = Float.POSITIVE_INFINITY;
            for (int i = 0; i < as.length; i++) {
                r = (float)Math.min(r, as[i]);
            }
        }
        bh.consume(r);
    }

    @Benchmark
    public void maxLanes(Blackhole bh) {
        float[] as = fa.apply(size);
        float r = Float.NEGATIVE_INFINITY;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = Float.NEGATIVE_INFINITY;
            for (int i = 0; i < as.length; i++) {
                r = (float)Math.max(r, as[i]);
            }
        }
        bh.consume(r);
    }



    @Benchmark
    public void lessThan(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] bs = fb.apply(size);

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
        float[] as = fa.apply(size);
        float[] bs = fb.apply(size);

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
        float[] as = fa.apply(size);
        float[] bs = fb.apply(size);

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
        float[] as = fa.apply(size);
        float[] bs = fb.apply(size);

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
        float[] as = fa.apply(size);
        float[] bs = fb.apply(size);

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
        float[] as = fa.apply(size);
        float[] bs = fb.apply(size);

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
        float[] as = fa.apply(size);
        float[] bs = fb.apply(size);
        float[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                float b = bs[i];
                boolean m = ms[i % ms.length];
                rs[i] = (m ? b : a);
            }
        }

        bh.consume(rs);
    }
    void rearrangeShared(int window, Blackhole bh) {
        float[] as = fa.apply(size);
        int[] order = fs.apply(size);
        float[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i += window) {
                for (int j = 0; j < window; j++) {
                    float a = as[i+j];
                    int pos = order[j];
                    rs[i + pos] = a;
                }
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void rearrange064(Blackhole bh) {
        int window = 64 / Float.SIZE;
        rearrangeShared(window, bh);
    }

    @Benchmark
    public void rearrange128(Blackhole bh) {
        int window = 128 / Float.SIZE;
        rearrangeShared(window, bh);
    }

    @Benchmark
    public void rearrange256(Blackhole bh) {
        int window = 256 / Float.SIZE;
        rearrangeShared(window, bh);
    }

    @Benchmark
    public void rearrange512(Blackhole bh) {
        int window = 512 / Float.SIZE;
        rearrangeShared(window, bh);
    }


    @Benchmark
    public void sin(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                rs[i] = (float)(Math.sin((double)a));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void exp(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                rs[i] = (float)(Math.exp((double)a));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void log1p(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                rs[i] = (float)(Math.log1p((double)a));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void log(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                rs[i] = (float)(Math.log((double)a));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void log10(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                rs[i] = (float)(Math.log10((double)a));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void expm1(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                rs[i] = (float)(Math.expm1((double)a));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void cos(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                rs[i] = (float)(Math.cos((double)a));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void tan(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                rs[i] = (float)(Math.tan((double)a));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void sinh(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                rs[i] = (float)(Math.sinh((double)a));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void cosh(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                rs[i] = (float)(Math.cosh((double)a));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void tanh(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                rs[i] = (float)(Math.tanh((double)a));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void asin(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                rs[i] = (float)(Math.asin((double)a));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void acos(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                rs[i] = (float)(Math.acos((double)a));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void atan(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                rs[i] = (float)(Math.atan((double)a));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void cbrt(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                rs[i] = (float)(Math.cbrt((double)a));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void hypot(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] bs = fb.apply(size);
        float[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                float b = bs[i];
                rs[i] = (float)(Math.hypot((double)a, (double)b));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void pow(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] bs = fb.apply(size);
        float[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                float b = bs[i];
                rs[i] = (float)(Math.pow((double)a, (double)b));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void atan2(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] bs = fb.apply(size);
        float[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                float b = bs[i];
                rs[i] = (float)(Math.atan2((double)a, (double)b));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void fma(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] bs = fb.apply(size);
        float[] cs = fc.apply(size);
        float[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                float b = bs[i];
                float c = cs[i];
                rs[i] = (float)(Math.fma(a, b, c));
            }
        }

        bh.consume(rs);
    }




    @Benchmark
    public void fmaMasked(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] bs = fb.apply(size);
        float[] cs = fc.apply(size);
        float[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                float b = bs[i];
                float c = cs[i];
                if (ms[i % ms.length]) {
                    rs[i] = (float)(Math.fma(a, b, c));
                } else {
                    rs[i] = a;
                }
            }
        }
        bh.consume(rs);
    }


    @Benchmark
    public void neg(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                rs[i] = (float)(-((float)a));
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void negMasked(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                boolean m = ms[i % ms.length];
                rs[i] = (m ? (float)(-((float)a)) : a);
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void abs(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                rs[i] = (float)(Math.abs((float)a));
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void absMasked(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                boolean m = ms[i % ms.length];
                rs[i] = (m ? (float)(Math.abs((float)a)) : a);
            }
        }

        bh.consume(rs);
    }




    @Benchmark
    public void sqrt(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                rs[i] = (float)(Math.sqrt((double)a));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void sqrtMasked(Blackhole bh) {
        float[] as = fa.apply(size);
        float[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                float a = as[i];
                boolean m = ms[i % ms.length];
                rs[i] = (m ? (float)(Math.sqrt((double)a)) : a);
            }
        }

        bh.consume(rs);
    }


    @Benchmark
    public void gatherBase0(Blackhole bh) {
        float[] as = fa.apply(size);
        int[] is    = fs.apply(size);
        float[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                int ix = 0 + is[i];
                rs[i] = as[ix];
            }
        }

        bh.consume(rs);
    }


    void gather(int window, Blackhole bh) {
        float[] as = fa.apply(size);
        int[] is    = fs.apply(size);
        float[] rs = fr.apply(size);

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
        int window = 64 / Float.SIZE;
        gather(window, bh);
    }

    @Benchmark
    public void gather128(Blackhole bh) {
        int window = 128 / Float.SIZE;
        gather(window, bh);
    }

    @Benchmark
    public void gather256(Blackhole bh) {
        int window = 256 / Float.SIZE;
        gather(window, bh);
    }

    @Benchmark
    public void gather512(Blackhole bh) {
        int window = 512 / Float.SIZE;
        gather(window, bh);
    }



    @Benchmark
    public void scatterBase0(Blackhole bh) {
        float[] as = fa.apply(size);
        int[] is    = fs.apply(size);
        float[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                int ix = 0 + is[i];
                rs[ix] = as[i];
            }
        }

        bh.consume(rs);
    }

    void scatter(int window, Blackhole bh) {
        float[] as = fa.apply(size);
        int[] is    = fs.apply(size);
        float[] rs = fr.apply(size);

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
        int window = 64 / Float.SIZE;
        scatter(window, bh);
    }

    @Benchmark
    public void scatter128(Blackhole bh) {
        int window = 128 / Float.SIZE;
        scatter(window, bh);
    }

    @Benchmark
    public void scatter256(Blackhole bh) {
        int window = 256 / Float.SIZE;
        scatter(window, bh);
    }

    @Benchmark
    public void scatter512(Blackhole bh) {
        int window = 512 / Float.SIZE;
        scatter(window, bh);
    }

}

