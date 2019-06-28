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

import jdk.incubator.vector.Vector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorShape;
import jdk.incubator.vector.VectorSpecies;
import jdk.incubator.vector.VectorShuffle;
import jdk.incubator.vector.DoubleVector;

import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.IntFunction;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(value = 1, jvmArgsPrepend = {"--add-modules=jdk.incubator.vector"})
public class Double256Vector extends AbstractVectorBenchmark {
    static final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_256;

    static final int INVOC_COUNT = 1; // get rid of outer loop

    @Param("1024")
    int size;

    double[] fill(IntFunction<Double> f) {
        double[] array = new double[size];
        for (int i = 0; i < array.length; i++) {
            array[i] = f.apply(i);
        }
        return array;
    }

    double[] a, b, c, r;
    boolean[] m, rm;
    int[] s;

    @Setup
    public void init() {
        size += size % SPECIES.length(); // FIXME: add post-loops

        a = fill(i -> (double)(2*i));
        b = fill(i -> (double)(i+1));
        c = fill(i -> (double)(i+5));
        r = fill(i -> (double)0);

        m = fillMask(size, i -> (i % 2) == 0);
        rm = fillMask(size, i -> false);

        s = fillInt(size, i -> RANDOM.nextInt(SPECIES.length()));
    }

    final IntFunction<double[]> fa = vl -> a;
    final IntFunction<double[]> fb = vl -> b;
    final IntFunction<double[]> fc = vl -> c;
    final IntFunction<double[]> fr = vl -> r;
    final IntFunction<boolean[]> fm = vl -> m;
    final IntFunction<boolean[]> fmr = vl -> rm;
    final BiFunction<Integer,Integer,int[]> fs = (i,j) -> s;


    @Benchmark
    public void ADD(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                DoubleVector bv = DoubleVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.ADD, bv).intoArray(r, i);
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void ADDMasked(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Double> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                DoubleVector bv = DoubleVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.ADD, bv, vmask).intoArray(r, i);
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void SUB(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                DoubleVector bv = DoubleVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.SUB, bv).intoArray(r, i);
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void SUBMasked(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Double> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                DoubleVector bv = DoubleVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.SUB, bv, vmask).intoArray(r, i);
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void MUL(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                DoubleVector bv = DoubleVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.MUL, bv).intoArray(r, i);
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void MULMasked(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Double> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                DoubleVector bv = DoubleVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.MUL, bv, vmask).intoArray(r, i);
            }
        }

        bh.consume(r);
    }


    @Benchmark
    public void DIV(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                DoubleVector bv = DoubleVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.DIV, bv).intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void DIVMasked(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Double> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                DoubleVector bv = DoubleVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.DIV, bv, vmask).intoArray(r, i);
            }
        }

        bh.consume(r);
    }


    @Benchmark
    public void FIRST_NONZERO(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                DoubleVector bv = DoubleVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.FIRST_NONZERO, bv).intoArray(r, i);
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void FIRST_NONZEROMasked(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Double> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                DoubleVector bv = DoubleVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.FIRST_NONZERO, bv, vmask).intoArray(r, i);
            }
        }

        bh.consume(r);
    }

































    @Benchmark
    public void MIN(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                DoubleVector bv = DoubleVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.MIN, bv).intoArray(r, i);
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void MAX(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                DoubleVector bv = DoubleVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.MAX, bv).intoArray(r, i);
            }
        }

        bh.consume(r);
    }







    @Benchmark
    public void ADDLanes(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                ra += av.reduceLanes(VectorOperators.ADD);
            }
        }
        bh.consume(ra);
    }

    @Benchmark
    public void ADDMaskedLanes(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Double> vmask = VectorMask.fromValues(SPECIES, mask);
        double ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                ra += av.reduceLanes(VectorOperators.ADD, vmask);
            }
        }
        bh.consume(ra);
    }

    @Benchmark
    public void MULLanes(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double ra = 1;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 1;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                ra *= av.reduceLanes(VectorOperators.MUL);
            }
        }
        bh.consume(ra);
    }

    @Benchmark
    public void MULMaskedLanes(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Double> vmask = VectorMask.fromValues(SPECIES, mask);
        double ra = 1;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 1;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                ra *= av.reduceLanes(VectorOperators.MUL, vmask);
            }
        }
        bh.consume(ra);
    }

    @Benchmark
    public void MINLanes(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double ra = Double.POSITIVE_INFINITY;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Double.POSITIVE_INFINITY;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                ra = (double)Math.min(ra, av.reduceLanes(VectorOperators.MIN));
            }
        }
        bh.consume(ra);
    }

    @Benchmark
    public void MINMaskedLanes(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Double> vmask = VectorMask.fromValues(SPECIES, mask);
        double ra = Double.POSITIVE_INFINITY;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Double.POSITIVE_INFINITY;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                ra = (double)Math.min(ra, av.reduceLanes(VectorOperators.MIN, vmask));
            }
        }
        bh.consume(ra);
    }

    @Benchmark
    public void MAXLanes(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double ra = Double.NEGATIVE_INFINITY;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                ra = (double)Math.max(ra, av.reduceLanes(VectorOperators.MAX));
            }
        }
        bh.consume(ra);
    }

    @Benchmark
    public void MAXMaskedLanes(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Double> vmask = VectorMask.fromValues(SPECIES, mask);
        double ra = Double.NEGATIVE_INFINITY;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                ra = (double)Math.max(ra, av.reduceLanes(VectorOperators.MAX, vmask));
            }
        }
        bh.consume(ra);
    }



    @Benchmark
    public void withLane(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                av.withLane(0, (double)4).intoArray(r, i);
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public Object LT() {
        double[] a = fa.apply(size);
        double[] b = fb.apply(size);
        boolean[] ms = fm.apply(size);
        VectorMask<Double> m = VectorMask.fromArray(SPECIES, ms, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                DoubleVector bv = DoubleVector.fromArray(SPECIES, b, i);
                VectorMask<Double> mv = av.compare(VectorOperators.LT, bv);

                m = m.and(mv); // accumulate results, so JIT can't eliminate relevant computations
            }
        }
        return m;
    }


    @Benchmark
    public Object GT() {
        double[] a = fa.apply(size);
        double[] b = fb.apply(size);
        boolean[] ms = fm.apply(size);
        VectorMask<Double> m = VectorMask.fromArray(SPECIES, ms, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                DoubleVector bv = DoubleVector.fromArray(SPECIES, b, i);
                VectorMask<Double> mv = av.compare(VectorOperators.GT, bv);

                m = m.and(mv); // accumulate results, so JIT can't eliminate relevant computations
            }
        }
        return m;
    }


    @Benchmark
    public Object EQ() {
        double[] a = fa.apply(size);
        double[] b = fb.apply(size);
        boolean[] ms = fm.apply(size);
        VectorMask<Double> m = VectorMask.fromArray(SPECIES, ms, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                DoubleVector bv = DoubleVector.fromArray(SPECIES, b, i);
                VectorMask<Double> mv = av.compare(VectorOperators.EQ, bv);

                m = m.and(mv); // accumulate results, so JIT can't eliminate relevant computations
            }
        }
        return m;
    }


    @Benchmark
    public Object NE() {
        double[] a = fa.apply(size);
        double[] b = fb.apply(size);
        boolean[] ms = fm.apply(size);
        VectorMask<Double> m = VectorMask.fromArray(SPECIES, ms, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                DoubleVector bv = DoubleVector.fromArray(SPECIES, b, i);
                VectorMask<Double> mv = av.compare(VectorOperators.NE, bv);

                m = m.and(mv); // accumulate results, so JIT can't eliminate relevant computations
            }
        }
        return m;
    }


    @Benchmark
    public Object LE() {
        double[] a = fa.apply(size);
        double[] b = fb.apply(size);
        boolean[] ms = fm.apply(size);
        VectorMask<Double> m = VectorMask.fromArray(SPECIES, ms, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                DoubleVector bv = DoubleVector.fromArray(SPECIES, b, i);
                VectorMask<Double> mv = av.compare(VectorOperators.LE, bv);

                m = m.and(mv); // accumulate results, so JIT can't eliminate relevant computations
            }
        }
        return m;
    }


    @Benchmark
    public Object GE() {
        double[] a = fa.apply(size);
        double[] b = fb.apply(size);
        boolean[] ms = fm.apply(size);
        VectorMask<Double> m = VectorMask.fromArray(SPECIES, ms, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                DoubleVector bv = DoubleVector.fromArray(SPECIES, b, i);
                VectorMask<Double> mv = av.compare(VectorOperators.GE, bv);

                m = m.and(mv); // accumulate results, so JIT can't eliminate relevant computations
            }
        }
        return m;
    }


    @Benchmark
    public void blend(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Double> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                DoubleVector bv = DoubleVector.fromArray(SPECIES, b, i);
                av.blend(bv, vmask).intoArray(r, i);
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void rearrange(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        int[] order = fs.apply(a.length, SPECIES.length());
        double[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                av.rearrange(VectorShuffle.fromArray(SPECIES, order, i)).intoArray(r, i);
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void extract(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                int num_lanes = SPECIES.length();
                // Manually unroll because full unroll happens after intrinsification.
                // Unroll is needed because get intrinsic requires for index to be a known constant.
                if (num_lanes == 1) {
                    r[i]=av.lane(0);
                } else if (num_lanes == 2) {
                    r[i]=av.lane(0);
                    r[i+1]=av.lane(1);
                } else if (num_lanes == 4) {
                    r[i]=av.lane(0);
                    r[i+1]=av.lane(1);
                    r[i+2]=av.lane(2);
                    r[i+3]=av.lane(3);
                } else if (num_lanes == 8) {
                    r[i]=av.lane(0);
                    r[i+1]=av.lane(1);
                    r[i+2]=av.lane(2);
                    r[i+3]=av.lane(3);
                    r[i+4]=av.lane(4);
                    r[i+5]=av.lane(5);
                    r[i+6]=av.lane(6);
                    r[i+7]=av.lane(7);
                } else if (num_lanes == 16) {
                    r[i]=av.lane(0);
                    r[i+1]=av.lane(1);
                    r[i+2]=av.lane(2);
                    r[i+3]=av.lane(3);
                    r[i+4]=av.lane(4);
                    r[i+5]=av.lane(5);
                    r[i+6]=av.lane(6);
                    r[i+7]=av.lane(7);
                    r[i+8]=av.lane(8);
                    r[i+9]=av.lane(9);
                    r[i+10]=av.lane(10);
                    r[i+11]=av.lane(11);
                    r[i+12]=av.lane(12);
                    r[i+13]=av.lane(13);
                    r[i+14]=av.lane(14);
                    r[i+15]=av.lane(15);
                } else if (num_lanes == 32) {
                    r[i]=av.lane(0);
                    r[i+1]=av.lane(1);
                    r[i+2]=av.lane(2);
                    r[i+3]=av.lane(3);
                    r[i+4]=av.lane(4);
                    r[i+5]=av.lane(5);
                    r[i+6]=av.lane(6);
                    r[i+7]=av.lane(7);
                    r[i+8]=av.lane(8);
                    r[i+9]=av.lane(9);
                    r[i+10]=av.lane(10);
                    r[i+11]=av.lane(11);
                    r[i+12]=av.lane(12);
                    r[i+13]=av.lane(13);
                    r[i+14]=av.lane(14);
                    r[i+15]=av.lane(15);
                    r[i+16]=av.lane(16);
                    r[i+17]=av.lane(17);
                    r[i+18]=av.lane(18);
                    r[i+19]=av.lane(19);
                    r[i+20]=av.lane(20);
                    r[i+21]=av.lane(21);
                    r[i+22]=av.lane(22);
                    r[i+23]=av.lane(23);
                    r[i+24]=av.lane(24);
                    r[i+25]=av.lane(25);
                    r[i+26]=av.lane(26);
                    r[i+27]=av.lane(27);
                    r[i+28]=av.lane(28);
                    r[i+29]=av.lane(29);
                    r[i+30]=av.lane(30);
                    r[i+31]=av.lane(31);
                } else if (num_lanes == 64) {
                    r[i]=av.lane(0);
                    r[i+1]=av.lane(1);
                    r[i+2]=av.lane(2);
                    r[i+3]=av.lane(3);
                    r[i+4]=av.lane(4);
                    r[i+5]=av.lane(5);
                    r[i+6]=av.lane(6);
                    r[i+7]=av.lane(7);
                    r[i+8]=av.lane(8);
                    r[i+9]=av.lane(9);
                    r[i+10]=av.lane(10);
                    r[i+11]=av.lane(11);
                    r[i+12]=av.lane(12);
                    r[i+13]=av.lane(13);
                    r[i+14]=av.lane(14);
                    r[i+15]=av.lane(15);
                    r[i+16]=av.lane(16);
                    r[i+17]=av.lane(17);
                    r[i+18]=av.lane(18);
                    r[i+19]=av.lane(19);
                    r[i+20]=av.lane(20);
                    r[i+21]=av.lane(21);
                    r[i+22]=av.lane(22);
                    r[i+23]=av.lane(23);
                    r[i+24]=av.lane(24);
                    r[i+25]=av.lane(25);
                    r[i+26]=av.lane(26);
                    r[i+27]=av.lane(27);
                    r[i+28]=av.lane(28);
                    r[i+29]=av.lane(29);
                    r[i+30]=av.lane(30);
                    r[i+31]=av.lane(31);
                    r[i+32]=av.lane(32);
                    r[i+33]=av.lane(33);
                    r[i+34]=av.lane(34);
                    r[i+35]=av.lane(35);
                    r[i+36]=av.lane(36);
                    r[i+37]=av.lane(37);
                    r[i+38]=av.lane(38);
                    r[i+39]=av.lane(39);
                    r[i+40]=av.lane(40);
                    r[i+41]=av.lane(41);
                    r[i+42]=av.lane(42);
                    r[i+43]=av.lane(43);
                    r[i+44]=av.lane(44);
                    r[i+45]=av.lane(45);
                    r[i+46]=av.lane(46);
                    r[i+47]=av.lane(47);
                    r[i+48]=av.lane(48);
                    r[i+49]=av.lane(49);
                    r[i+50]=av.lane(50);
                    r[i+51]=av.lane(51);
                    r[i+52]=av.lane(52);
                    r[i+53]=av.lane(53);
                    r[i+54]=av.lane(54);
                    r[i+55]=av.lane(55);
                    r[i+56]=av.lane(56);
                    r[i+57]=av.lane(57);
                    r[i+58]=av.lane(58);
                    r[i+59]=av.lane(59);
                    r[i+60]=av.lane(60);
                    r[i+61]=av.lane(61);
                    r[i+62]=av.lane(62);
                    r[i+63]=av.lane(63);
                } else {
                    for (int j = 0; j < SPECIES.length(); j++) {
                        r[i+j]=av.lane(j);
                    }
                }
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void single(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] r = new double[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.single(SPECIES, a[i]);
                av.intoArray(r, i);
            }
        }

        bh.consume(r);
    }


    @Benchmark
    public void SIN(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.SIN).intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void EXP(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.EXP).intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void LOG1P(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.LOG1P).intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void LOG(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.LOG).intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void LOG10(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.LOG10).intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void EXPM1(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.EXPM1).intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void COS(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.COS).intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void TAN(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.TAN).intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void SINH(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.SINH).intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void COSH(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.COSH).intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void TANH(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.TANH).intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void ASIN(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ASIN).intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void ACOS(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ACOS).intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void ATAN(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ATAN).intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void CBRT(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.CBRT).intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void HYPOT(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                DoubleVector bv = DoubleVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.HYPOT, bv).intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void POW(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                DoubleVector bv = DoubleVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.POW, bv).intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void ATAN2(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                DoubleVector bv = DoubleVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.ATAN2, bv).intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void FMA(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());
        double[] c = fc.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                DoubleVector bv = DoubleVector.fromArray(SPECIES, b, i);
                DoubleVector cv = DoubleVector.fromArray(SPECIES, c, i);
                av.lanewise(VectorOperators.FMA, bv, cv).intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void FMAMasked(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());
        double[] c = fc.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Double> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                DoubleVector bv = DoubleVector.fromArray(SPECIES, b, i);
                DoubleVector cv = DoubleVector.fromArray(SPECIES, c, i);
                av.lanewise(VectorOperators.FMA, bv, cv, vmask).intoArray(r, i);
            }
        }

        bh.consume(r);
    }




    @Benchmark
    public void NEG(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.NEG).intoArray(r, i);
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void NEGMasked(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Double> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.NEG, vmask).intoArray(r, i);
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void ABS(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ABS).intoArray(r, i);
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void ABSMasked(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Double> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ABS, vmask).intoArray(r, i);
            }
        }

        bh.consume(r);
    }






    @Benchmark
    public void SQRT(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.SQRT).intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void SQRTMasked(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        double[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Double> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.SQRT, vmask).intoArray(r, i);
            }
        }

        bh.consume(r);
    }


    @Benchmark
    public void gather(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        int[] b    = fs.apply(a.length, SPECIES.length());
        double[] r = new double[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i, b, i);
                av.intoArray(r, i);
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void scatter(Blackhole bh) {
        double[] a = fa.apply(SPECIES.length());
        int[] b = fs.apply(a.length, SPECIES.length());
        double[] r = new double[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
                av.intoArray(r, i, b, i);
            }
        }

        bh.consume(r);
    }
}

