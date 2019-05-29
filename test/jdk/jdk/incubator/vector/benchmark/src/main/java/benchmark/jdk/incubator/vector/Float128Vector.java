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

import jdk.incubator.vector.Vector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorShape;
import jdk.incubator.vector.VectorSpecies;
import jdk.incubator.vector.VectorShuffle;
import jdk.incubator.vector.FloatVector;

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
public class Float128Vector extends AbstractVectorBenchmark {
    static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_128;

    static final int INVOC_COUNT = 1; // get rid of outer loop

    @Param("1024")
    int size;

    float[] fill(IntFunction<Float> f) {
        float[] array = new float[size];
        for (int i = 0; i < array.length; i++) {
            array[i] = f.apply(i);
        }
        return array;
    }

    float[] a, b, c, r;
    boolean[] m, rm;
    int[] s;

    @Setup
    public void init() {
        size += size % SPECIES.length(); // FIXME: add post-loops

        a = fill(i -> (float)(2*i));
        b = fill(i -> (float)(i+1));
        c = fill(i -> (float)(i+5));
        r = fill(i -> (float)0);

        m = fillMask(size, i -> (i % 2) == 0);
        rm = fillMask(size, i -> false);

        s = fillInt(size, i -> RANDOM.nextInt(SPECIES.length()));
    }

    final IntFunction<float[]> fa = vl -> a;
    final IntFunction<float[]> fb = vl -> b;
    final IntFunction<float[]> fc = vl -> c;
    final IntFunction<float[]> fr = vl -> r;
    final IntFunction<boolean[]> fm = vl -> m;
    final IntFunction<boolean[]> fmr = vl -> rm;
    final BiFunction<Integer,Integer,int[]> fs = (i,j) -> s;


    @Benchmark
    public void add(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] b = fb.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                FloatVector bv = FloatVector.fromArray(SPECIES, b, i);
                av.add(bv).intoArray(r, i);
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void addMasked(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] b = fb.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Float> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                FloatVector bv = FloatVector.fromArray(SPECIES, b, i);
                av.add(bv, vmask).intoArray(r, i);
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void sub(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] b = fb.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                FloatVector bv = FloatVector.fromArray(SPECIES, b, i);
                av.sub(bv).intoArray(r, i);
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void subMasked(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] b = fb.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Float> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                FloatVector bv = FloatVector.fromArray(SPECIES, b, i);
                av.sub(bv, vmask).intoArray(r, i);
            }
        }

        bh.consume(r);
    }


    @Benchmark
    public void div(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] b = fb.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                FloatVector bv = FloatVector.fromArray(SPECIES, b, i);
                av.div(bv).intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void divMasked(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] b = fb.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Float> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                FloatVector bv = FloatVector.fromArray(SPECIES, b, i);
                av.div(bv, vmask).intoArray(r, i);
            }
        }

        bh.consume(r);
    }


    @Benchmark
    public void mul(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] b = fb.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                FloatVector bv = FloatVector.fromArray(SPECIES, b, i);
                av.mul(bv).intoArray(r, i);
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void mulMasked(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] b = fb.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Float> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                FloatVector bv = FloatVector.fromArray(SPECIES, b, i);
                av.mul(bv, vmask).intoArray(r, i);
            }
        }

        bh.consume(r);
    }











































    @Benchmark
    public void max(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] b = fb.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                FloatVector bv = FloatVector.fromArray(SPECIES, b, i);
                av.max(bv).intoArray(r, i);
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void maxMasked(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] b = fb.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Float> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                FloatVector bv = FloatVector.fromArray(SPECIES, b, i);
                av.max(bv, vmask).intoArray(r, i);
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void min(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] b = fb.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                FloatVector bv = FloatVector.fromArray(SPECIES, b, i);
                av.min(bv).intoArray(r, i);
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void minMasked(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] b = fb.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Float> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                FloatVector bv = FloatVector.fromArray(SPECIES, b, i);
                av.min(bv, vmask).intoArray(r, i);
            }
        }

        bh.consume(r);
    }







    @Benchmark
    public void addLanes(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                ra += av.addLanes();
            }
        }
        bh.consume(ra);
    }

    @Benchmark
    public void addLanesMasked(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Float> vmask = VectorMask.fromValues(SPECIES, mask);
        float ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                ra += av.addLanes(vmask);
            }
        }
        bh.consume(ra);
    }

    @Benchmark
    public void mulLanes(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float ra = 1;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 1;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                ra *= av.mulLanes();
            }
        }
        bh.consume(ra);
    }

    @Benchmark
    public void mulLanesMasked(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Float> vmask = VectorMask.fromValues(SPECIES, mask);
        float ra = 1;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 1;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                ra *= av.mulLanes(vmask);
            }
        }
        bh.consume(ra);
    }

    @Benchmark
    public void minLanes(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float ra = Float.POSITIVE_INFINITY;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Float.POSITIVE_INFINITY;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                ra = (float)Math.min(ra, av.minLanes());
            }
        }
        bh.consume(ra);
    }

    @Benchmark
    public void minLanesMasked(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Float> vmask = VectorMask.fromValues(SPECIES, mask);
        float ra = Float.POSITIVE_INFINITY;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Float.POSITIVE_INFINITY;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                ra = (float)Math.min(ra, av.minLanes(vmask));
            }
        }
        bh.consume(ra);
    }

    @Benchmark
    public void maxLanes(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float ra = Float.NEGATIVE_INFINITY;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Float.NEGATIVE_INFINITY;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                ra = (float)Math.max(ra, av.maxLanes());
            }
        }
        bh.consume(ra);
    }

    @Benchmark
    public void maxLanesMasked(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Float> vmask = VectorMask.fromValues(SPECIES, mask);
        float ra = Float.NEGATIVE_INFINITY;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Float.NEGATIVE_INFINITY;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                ra = (float)Math.max(ra, av.maxLanes(vmask));
            }
        }
        bh.consume(ra);
    }



    @Benchmark
    public void with(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                av.with(0, (float)4).intoArray(r, i);
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public Object lessThan() {
        float[] a = fa.apply(size);
        float[] b = fb.apply(size);
        boolean[] ms = fm.apply(size);
        VectorMask<Float> m = VectorMask.fromArray(SPECIES, ms, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                FloatVector bv = FloatVector.fromArray(SPECIES, b, i);
                VectorMask<Float> mv = av.lessThan(bv);

                m = m.and(mv); // accumulate results, so JIT can't eliminate relevant computations
            }
        }
        return m;
    }


    @Benchmark
    public Object greaterThan() {
        float[] a = fa.apply(size);
        float[] b = fb.apply(size);
        boolean[] ms = fm.apply(size);
        VectorMask<Float> m = VectorMask.fromArray(SPECIES, ms, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                FloatVector bv = FloatVector.fromArray(SPECIES, b, i);
                VectorMask<Float> mv = av.greaterThan(bv);

                m = m.and(mv); // accumulate results, so JIT can't eliminate relevant computations
            }
        }
        return m;
    }


    @Benchmark
    public Object equal() {
        float[] a = fa.apply(size);
        float[] b = fb.apply(size);
        boolean[] ms = fm.apply(size);
        VectorMask<Float> m = VectorMask.fromArray(SPECIES, ms, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                FloatVector bv = FloatVector.fromArray(SPECIES, b, i);
                VectorMask<Float> mv = av.equal(bv);

                m = m.and(mv); // accumulate results, so JIT can't eliminate relevant computations
            }
        }
        return m;
    }


    @Benchmark
    public Object notEqual() {
        float[] a = fa.apply(size);
        float[] b = fb.apply(size);
        boolean[] ms = fm.apply(size);
        VectorMask<Float> m = VectorMask.fromArray(SPECIES, ms, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                FloatVector bv = FloatVector.fromArray(SPECIES, b, i);
                VectorMask<Float> mv = av.notEqual(bv);

                m = m.and(mv); // accumulate results, so JIT can't eliminate relevant computations
            }
        }
        return m;
    }


    @Benchmark
    public Object lessThanEq() {
        float[] a = fa.apply(size);
        float[] b = fb.apply(size);
        boolean[] ms = fm.apply(size);
        VectorMask<Float> m = VectorMask.fromArray(SPECIES, ms, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                FloatVector bv = FloatVector.fromArray(SPECIES, b, i);
                VectorMask<Float> mv = av.lessThanEq(bv);

                m = m.and(mv); // accumulate results, so JIT can't eliminate relevant computations
            }
        }
        return m;
    }


    @Benchmark
    public Object greaterThanEq() {
        float[] a = fa.apply(size);
        float[] b = fb.apply(size);
        boolean[] ms = fm.apply(size);
        VectorMask<Float> m = VectorMask.fromArray(SPECIES, ms, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                FloatVector bv = FloatVector.fromArray(SPECIES, b, i);
                VectorMask<Float> mv = av.greaterThanEq(bv);

                m = m.and(mv); // accumulate results, so JIT can't eliminate relevant computations
            }
        }
        return m;
    }


    @Benchmark
    public void blend(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] b = fb.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Float> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                FloatVector bv = FloatVector.fromArray(SPECIES, b, i);
                av.blend(bv, vmask).intoArray(r, i);
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void rearrange(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        int[] order = fs.apply(a.length, SPECIES.length());
        float[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                av.rearrange(VectorShuffle.fromArray(SPECIES, order, i)).intoArray(r, i);
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void extract(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
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
        float[] a = fa.apply(SPECIES.length());
        float[] r = new float[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.single(SPECIES, a[i]);
                av.intoArray(r, i);
            }
        }

        bh.consume(r);
    }


    @Benchmark
    public void sin(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                av.sin().intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void exp(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                av.exp().intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void log1p(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                av.log1p().intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void log(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                av.log().intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void log10(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                av.log10().intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void expm1(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                av.expm1().intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void cos(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                av.cos().intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void tan(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                av.tan().intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void sinh(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                av.sinh().intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void cosh(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                av.cosh().intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void tanh(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                av.tanh().intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void asin(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                av.asin().intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void acos(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                av.acos().intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void atan(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                av.atan().intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void cbrt(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                av.cbrt().intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void hypot(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] b = fb.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                FloatVector bv = FloatVector.fromArray(SPECIES, b, i);
                av.hypot(bv).intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void pow(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] b = fb.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                FloatVector bv = FloatVector.fromArray(SPECIES, b, i);
                av.pow(bv).intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void atan2(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] b = fb.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                FloatVector bv = FloatVector.fromArray(SPECIES, b, i);
                av.atan2(bv).intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void fma(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] b = fb.apply(SPECIES.length());
        float[] c = fc.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                FloatVector bv = FloatVector.fromArray(SPECIES, b, i);
                FloatVector cv = FloatVector.fromArray(SPECIES, c, i);
                av.fma(bv, cv).intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void fmaMasked(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] b = fb.apply(SPECIES.length());
        float[] c = fc.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Float> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                FloatVector bv = FloatVector.fromArray(SPECIES, b, i);
                FloatVector cv = FloatVector.fromArray(SPECIES, c, i);
                av.fma(bv, cv, vmask).intoArray(r, i);
            }
        }

        bh.consume(r);
    }


    @Benchmark
    public void neg(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                av.neg().intoArray(r, i);
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void negMasked(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Float> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                av.neg(vmask).intoArray(r, i);
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void abs(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                av.abs().intoArray(r, i);
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void absMasked(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Float> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                av.abs(vmask).intoArray(r, i);
            }
        }

        bh.consume(r);
    }




    @Benchmark
    public void sqrt(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                av.sqrt().intoArray(r, i);
            }
        }

        bh.consume(r);
    }



    @Benchmark
    public void sqrtMasked(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        float[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Float> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                av.sqrt(vmask).intoArray(r, i);
            }
        }

        bh.consume(r);
    }


    @Benchmark
    public void gather(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        int[] b    = fs.apply(a.length, SPECIES.length());
        float[] r = new float[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i, b, i);
                av.intoArray(r, i);
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void gatherMasked(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        int[] b    = fs.apply(a.length, SPECIES.length());
        float[] r = new float[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Float> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i, vmask, b, i);
                av.intoArray(r, i);
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void scatter(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        int[] b = fs.apply(a.length, SPECIES.length());
        float[] r = new float[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                av.intoArray(r, i, b, i);
            }
        }

        bh.consume(r);
    }

    @Benchmark
    public void scatterMasked(Blackhole bh) {
        float[] a = fa.apply(SPECIES.length());
        int[] b = fs.apply(a.length, SPECIES.length());
        float[] r = fb.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Float> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector av = FloatVector.fromArray(SPECIES, a, i);
                av.intoArray(r, i, vmask, b, i);
            }
        }

        bh.consume(r);
    }
}

