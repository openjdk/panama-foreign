/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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
 * or visit www.oracle.com if you need additional information or have
 * questions.
 */

import static com.oracle.vector.PatchableVecUtils.*;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Random;

//Note: You can run this from the gradle file with $> gradle runSnippetTest

public class SnippetTest {

    public SnippetTest() {
    }

    static private final jdk.internal.misc.Unsafe U;

    static {
        try {
            Field f = jdk.internal.misc.Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            U = (jdk.internal.misc.Unsafe) f.get(null);
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    //From Vladimir's
    public static Long4 loadL4(Object base, long offset) {
        return U.getLong4(base, offset);
    }

    public static Long2 loadL2(Object base, long offset) {
        return U.getLong2(base, offset);
    }

    //Quick and easy forcing of our assertions with random numbers.
    public static void main(String[] args) {
        Random rand = new Random();
        Iterator<Integer> ints = rand
                .ints()
                .iterator();

        for (int i = 0; i < 1000; i++) {
            Long2 a = long2FromInts(ints.next(), ints.next(), ints.next(), ints.next());
            Long2 b = long2FromInts(ints.next(), ints.next(), ints.next(), ints.next());

            Long2 fa = long2FromFloats(rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
            Long2 fb = long2FromFloats(rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat());

            Long4 faa = randomLong4(rand);
            Long4 fba = randomLong4(rand);
            int random_imm8 = rand.nextInt(8);

            vpaddd(a, b);
            vaddss(fa, fb);
            vaddps(faa, fba);
            vpsubd(a, b);
            vpmulld(a, b);
            vpxor(a, b);
            vpand(a, b);
            vpor(a, b);
            vmulps(faa, fba);
            vpshufd(a, random_imm8);
            vpsignd(a, b);
            vpblendd(a, b, random_imm8);
            vpcmpeqd(a, b);
            vxorps(fa, fb);
            vmulps(fba, fba);
            vaddps(faa, fba);
            vcmpeqps(Long2.ZERO,Long2.make(0L,1L));

        }
    }

    public static Long2 someLong2() {
        Random rand = new Random();
        Iterator<Integer> ints = rand
                .ints()
                .iterator();
        return long2FromInts(ints.next(), ints.next(), ints.next(), ints.next());

    }


    public static Long2 long2FromInts(int a, int b, int c, int d) {
        return Long2.make(
                pack(a, b),
                pack(c, d)
        );
    }

    public static Long2 long2FromFloats(float a, float b, float c, float d) {
        return Long2.make(
                pack(a, b),
                pack(c, d)
        );
    }

    public static Long4 randomLong4(Random r) {
        return Long4.make(pack(r.nextFloat(), r.nextFloat()),
                pack(r.nextFloat(), r.nextFloat()),
                pack(r.nextFloat(), r.nextFloat()),
                pack(r.nextFloat(), r.nextFloat()));
    }

}
