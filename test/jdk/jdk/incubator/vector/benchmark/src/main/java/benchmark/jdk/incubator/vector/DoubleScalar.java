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

// -- This file was mechanically generated: Do not edit! -- //

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
public class DoubleScalar extends AbstractVectorBenchmark {
    static final int INVOC_COUNT = 1; // To align with vector benchmarks.

    @Param("1024")
    int size;

    double[] fill(IntFunction<Double> f) {
        double[] array = new double[size];
        for (int i = 0; i < array.length; i++) {
            array[i] = f.apply(i);
        }
        return array;
    }

    double[] as, bs, cs, rs;
    boolean[] ms, rms;
    int[] ss;

    @Setup
    public void init() {
        as = fill(i -> (double)(2*i));
        bs = fill(i -> (double)(i+1));
        cs = fill(i -> (double)(i+5));
        rs = fill(i -> (double)0);
        ms = fillMask(size, i -> (i % 2) == 0);
        rms = fillMask(size, i -> false);

        ss = fillInt(size, i -> RANDOM.nextInt(Math.max(i,1)));
    }

    final IntFunction<double[]> fa = vl -> as;
    final IntFunction<double[]> fb = vl -> bs;
    final IntFunction<double[]> fc = vl -> cs;
    final IntFunction<double[]> fr = vl -> rs;
    final IntFunction<boolean[]> fm = vl -> ms;
    final IntFunction<boolean[]> fmr = vl -> rms;
    final IntFunction<int[]> fs = vl -> ss;


    @Benchmark
    public void add(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] bs = fb.apply(size);
        double[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                double b = bs[i];
                rs[i] = (double)(a + b);
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void addMasked(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] bs = fb.apply(size);
        double[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                double b = bs[i];
                if (ms[i % ms.length]) {
                    rs[i] = (double)(a + b);
                } else {
                    rs[i] = a;
                }
            }
        }
        bh.consume(rs);
    }

    @Benchmark
    public void sub(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] bs = fb.apply(size);
        double[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                double b = bs[i];
                rs[i] = (double)(a - b);
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void subMasked(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] bs = fb.apply(size);
        double[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                double b = bs[i];
                if (ms[i % ms.length]) {
                    rs[i] = (double)(a - b);
                } else {
                    rs[i] = a;
                }
            }
        }
        bh.consume(rs);
    }


    @Benchmark
    public void div(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] bs = fb.apply(size);
        double[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                double b = bs[i];
                rs[i] = (double)(a / b);
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void divMasked(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] bs = fb.apply(size);
        double[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                double b = bs[i];
                if (ms[i % ms.length]) {
                    rs[i] = (double)(a / b);
                } else {
                    rs[i] = a;
                }
            }
        }
        bh.consume(rs);
    }


    @Benchmark
    public void mul(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] bs = fb.apply(size);
        double[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                double b = bs[i];
                rs[i] = (double)(a * b);
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void mulMasked(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] bs = fb.apply(size);
        double[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                double b = bs[i];
                if (ms[i % ms.length]) {
                    rs[i] = (double)(a * b);
                } else {
                    rs[i] = a;
                }
            }
        }
        bh.consume(rs);
    }









    @Benchmark
    public void lanewise_FIRST_NONZERO(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] bs = fb.apply(size);
        double[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                double b = bs[i];
                rs[i] = (double)(Double.doubleToLongBits(a)!=0?a:b);
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void lanewise_FIRST_NONZEROMasked(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] bs = fb.apply(size);
        double[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                double b = bs[i];
                if (ms[i % ms.length]) {
                    rs[i] = (double)(Double.doubleToLongBits(a)!=0?a:b);
                } else {
                    rs[i] = a;
                }
            }
        }
        bh.consume(rs);
    }





































    @Benchmark
    public void max(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] bs = fb.apply(size);
        double[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                double b = bs[i];
                rs[i] = (double)(Math.max(a, b));
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void min(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] bs = fb.apply(size);
        double[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                double b = bs[i];
                rs[i] = (double)(Math.min(a, b));
            }
        }

        bh.consume(rs);
    }




    @Benchmark
    public void addLanes(Blackhole bh) {
        double[] as = fa.apply(size);
        double r = 0;
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
        double[] as = fa.apply(size);
        double r = 1;
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
        double[] as = fa.apply(size);
        double r = Double.POSITIVE_INFINITY;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = Double.POSITIVE_INFINITY;
            for (int i = 0; i < as.length; i++) {
                r = (double)Math.min(r, as[i]);
            }
        }
        bh.consume(r);
    }

    @Benchmark
    public void maxLanes(Blackhole bh) {
        double[] as = fa.apply(size);
        double r = Double.NEGATIVE_INFINITY;
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            r = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < as.length; i++) {
                r = (double)Math.max(r, as[i]);
            }
        }
        bh.consume(r);
    }



    @Benchmark
    public void lessThan(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] bs = fb.apply(size);

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
        double[] as = fa.apply(size);
        double[] bs = fb.apply(size);

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
        double[] as = fa.apply(size);
        double[] bs = fb.apply(size);

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
        double[] as = fa.apply(size);
        double[] bs = fb.apply(size);

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
        double[] as = fa.apply(size);
        double[] bs = fb.apply(size);

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
        double[] as = fa.apply(size);
        double[] bs = fb.apply(size);

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
        double[] as = fa.apply(size);
        double[] bs = fb.apply(size);
        double[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                double b = bs[i];
                boolean m = ms[i % ms.length];
                rs[i] = (m ? b : a);
            }
        }

        bh.consume(rs);
    }
    void rearrangeShared(int window, Blackhole bh) {
        double[] as = fa.apply(size);
        int[] order = fs.apply(size);
        double[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i += window) {
                for (int j = 0; j < window; j++) {
                    double a = as[i+j];
                    int pos = order[j];
                    rs[i + pos] = a;
                }
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void rearrange064(Blackhole bh) {
        int window = 64 / Double.SIZE;
        rearrangeShared(window, bh);
    }

    @Benchmark
    public void rearrange128(Blackhole bh) {
        int window = 128 / Double.SIZE;
        rearrangeShared(window, bh);
    }

    @Benchmark
    public void rearrange256(Blackhole bh) {
        int window = 256 / Double.SIZE;
        rearrangeShared(window, bh);
    }

    @Benchmark
    public void rearrange512(Blackhole bh) {
        int window = 512 / Double.SIZE;
        rearrangeShared(window, bh);
    }


    @Benchmark
    public void sin(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                rs[i] = (double)(Math.sin((double)a));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void exp(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                rs[i] = (double)(Math.exp((double)a));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void log1p(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                rs[i] = (double)(Math.log1p((double)a));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void log(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                rs[i] = (double)(Math.log((double)a));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void log10(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                rs[i] = (double)(Math.log10((double)a));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void expm1(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                rs[i] = (double)(Math.expm1((double)a));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void cos(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                rs[i] = (double)(Math.cos((double)a));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void tan(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                rs[i] = (double)(Math.tan((double)a));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void sinh(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                rs[i] = (double)(Math.sinh((double)a));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void cosh(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                rs[i] = (double)(Math.cosh((double)a));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void tanh(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                rs[i] = (double)(Math.tanh((double)a));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void asin(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                rs[i] = (double)(Math.asin((double)a));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void acos(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                rs[i] = (double)(Math.acos((double)a));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void atan(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                rs[i] = (double)(Math.atan((double)a));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void cbrt(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                rs[i] = (double)(Math.cbrt((double)a));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void hypot(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] bs = fb.apply(size);
        double[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                double b = bs[i];
                rs[i] = (double)(Math.hypot((double)a, (double)b));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void pow(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] bs = fb.apply(size);
        double[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                double b = bs[i];
                rs[i] = (double)(Math.pow((double)a, (double)b));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void atan2(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] bs = fb.apply(size);
        double[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                double b = bs[i];
                rs[i] = (double)(Math.atan2((double)a, (double)b));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void lanewise_FMA(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] bs = fb.apply(size);
        double[] cs = fc.apply(size);
        double[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                double b = bs[i];
                double c = cs[i];
                rs[i] = (double)(Math.fma(a, b, c));
            }
        }

        bh.consume(rs);
    }




    @Benchmark
    public void lanewise_FMAMasked(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] bs = fb.apply(size);
        double[] cs = fc.apply(size);
        double[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                double b = bs[i];
                double c = cs[i];
                if (ms[i % ms.length]) {
                    rs[i] = (double)(Math.fma(a, b, c));
                } else {
                    rs[i] = a;
                }
            }
        }
        bh.consume(rs);
    }




    @Benchmark
    public void neg(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                rs[i] = (double)(-((double)a));
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void negMasked(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                boolean m = ms[i % ms.length];
                rs[i] = (m ? (double)(-((double)a)) : a);
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void abs(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                rs[i] = (double)(Math.abs((double)a));
            }
        }

        bh.consume(rs);
    }

    @Benchmark
    public void absMasked(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                boolean m = ms[i % ms.length];
                rs[i] = (m ? (double)(Math.abs((double)a)) : a);
            }
        }

        bh.consume(rs);
    }






    @Benchmark
    public void sqrt(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                rs[i] = (double)(Math.sqrt((double)a));
            }
        }

        bh.consume(rs);
    }



    @Benchmark
    public void sqrtMasked(Blackhole bh) {
        double[] as = fa.apply(size);
        double[] rs = fr.apply(size);
        boolean[] ms = fm.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                double a = as[i];
                boolean m = ms[i % ms.length];
                rs[i] = (m ? (double)(Math.sqrt((double)a)) : a);
            }
        }

        bh.consume(rs);
    }


    @Benchmark
    public void gatherBase0(Blackhole bh) {
        double[] as = fa.apply(size);
        int[] is    = fs.apply(size);
        double[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                int ix = 0 + is[i];
                rs[i] = as[ix];
            }
        }

        bh.consume(rs);
    }


    void gather(int window, Blackhole bh) {
        double[] as = fa.apply(size);
        int[] is    = fs.apply(size);
        double[] rs = fr.apply(size);

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
        int window = 64 / Double.SIZE;
        gather(window, bh);
    }

    @Benchmark
    public void gather128(Blackhole bh) {
        int window = 128 / Double.SIZE;
        gather(window, bh);
    }

    @Benchmark
    public void gather256(Blackhole bh) {
        int window = 256 / Double.SIZE;
        gather(window, bh);
    }

    @Benchmark
    public void gather512(Blackhole bh) {
        int window = 512 / Double.SIZE;
        gather(window, bh);
    }



    @Benchmark
    public void scatterBase0(Blackhole bh) {
        double[] as = fa.apply(size);
        int[] is    = fs.apply(size);
        double[] rs = fr.apply(size);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < as.length; i++) {
                int ix = 0 + is[i];
                rs[ix] = as[i];
            }
        }

        bh.consume(rs);
    }

    void scatter(int window, Blackhole bh) {
        double[] as = fa.apply(size);
        int[] is    = fs.apply(size);
        double[] rs = fr.apply(size);

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
        int window = 64 / Double.SIZE;
        scatter(window, bh);
    }

    @Benchmark
    public void scatter128(Blackhole bh) {
        int window = 128 / Double.SIZE;
        scatter(window, bh);
    }

    @Benchmark
    public void scatter256(Blackhole bh) {
        int window = 256 / Double.SIZE;
        scatter(window, bh);
    }

    @Benchmark
    public void scatter512(Blackhole bh) {
        int window = 512 / Double.SIZE;
        scatter(window, bh);
    }

}

