/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
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

/*
 * @test
 * @compile VectorUtils.java VectorizedHashCode.java
 * @modules java.base/jdk.internal.misc
 * @run main/othervm/timeout=300 panama.snippets.VectorizedHashCode
 */

package panama.snippets;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Random;
import jdk.internal.misc.Unsafe;

import static panama.snippets.VectorUtils.*;

import jdk.vm.ci.panama.amd64.CPUID;
import static jdk.vm.ci.amd64.AMD64.CPUFeature.AVX2;


public class VectorizedHashCode {
    static final int B1 = 31;
    static final int B2 = B1*B1;
    static final int B4 = B2*B2;
    static final int B8 = B4*B4;
    static final Long4 pow8 = valueOf(B4 * B2 * B1, B4 * B2, B4 * B1, B4, B2 * B1, B2, B1, 1);

    static final Long4 pow88 = valueOf(B8, B8, B8, B8, B8, B8, B8, B8);

    // Naive implementation
    static int simple_hash(byte[] buf, int off, int n) {
        int h = 0;
        for (int i = off; i < n; i++) {
            h = h * 31 + Byte.toUnsignedInt(buf[i]);
        }

        return h;
    }

    static int vectorized_hash(byte[] buf, int off, int len) {
        Long4 acc = Long4.ZERO;
        for (; len >= 8; off += 8, len -= 8) {
            acc = vhash8(acc, U.getLong(buf, Unsafe.ARRAY_BYTE_BASE_OFFSET + off));
        }
        int hash = hadd_v8si(acc);
        if (len >= 4) {
            len -= 4;
            byte x0 = buf[off++];
            byte x1 = buf[off++];
            byte x2 = buf[off++];
            byte x3 = buf[off++];
            hash = H4R(hash, x0, x1, x2, x3);
        }
        for (; len > 0; len--) {
            hash = H1(hash, buf[off++]);
        }
        return hash;
    }

    static Long4 vhash8(Long4 acc, long ch8) {
        acc = mullo_epi32(acc, pow88);
        Long4 cv8 = load_v8qi_to_v8si(ch8);
        cv8 = mullo_epi32(cv8, pow8);
        acc = add_epi32(acc, cv8);
        return acc;
    }

    static final Long2 bytesa = valueOf(0xFFFFFF00, 0xFFFFFF01, 0xFFFFFF02, 0xFFFFFF03);
    static final Long2 bytesb = valueOf(0xFFFFFF04, 0xFFFFFF05, 0xFFFFFF06, 0xFFFFFF07);

    static Long4 load_v8qi_to_v8si(long ch8) {
        Long2 buf128a = insert_epi64(Long2.ZERO, ch8, 0);
        Long2 buf128b = buf128a;
        buf128a = shuffle_epi8(buf128a, bytesa);
        buf128b = shuffle_epi8(buf128b, bytesb);
        Long4 buf256 = pack(buf128a, buf128b);

        // FIXME: doesn't work
//        Long2 buf128 = insert_epi64(Long2.ZERO, ch8, 0);
//        Long4 buf256 = insert_m128(Long4.ZERO, buf128, 0);
//        buf256 = shuffle_epi8(buf256, shuffle);

        return buf256;
    }

    static int hadd_v8si(Long4 acc) {
        Long4 tmp = acc;                   // 8 int
        tmp = hadd_epi32(tmp, Long4.ZERO); // 4 int
        tmp = hadd_epi32(tmp, Long4.ZERO); // 2 int
        int res = getInt(tmp, 7) + getInt(tmp, 3);
        return res;
    }

    static int TIMES_31(int x) {
        return (x << 5) - x;
    }

    static int H1(int acc, byte x) {
        return TIMES_31(acc) + Byte.toUnsignedInt(x);
    }

    static int H2(int acc, byte x0, byte x1) {
        return H1(H1(acc, x0), x1);
    }

    static int H3(int acc, byte x0, byte x1, byte x2) {
        return H1(H2(acc, x0, x1), x2);
    }

    static int H4(int acc, byte x0, byte x1, byte x2, byte x3) {
        return H1(H3(acc, x0, x1, x2), x3);
    }

    static int H4R(int acc, byte x0, byte x1, byte x2, byte x3) {
        byte b0 = (byte)0;
        return H4(acc, b0, b0, b0, b0) +
                H4(  0, x0, b0, b0, b0) +
                H4(  0, b0, x1, b0, b0) +
                H4(  0, b0, b0, x2, b0) +
                H4(  0, b0, b0, b0, x3);
    }

    static int testId = 0;
    static void test(byte[] buf) {
        ++testId;

        int hqi = simple_hash(buf, 0, buf.length);
        int vhash = vectorized_hash(buf, 0, buf.length);

        if (hqi != vhash) {
            StringBuilder sb = new StringBuilder(String.format("%5d: ", testId));
            if (buf.length < 32) {
                sb.append(Arrays.toString(buf));
            } else {
                sb.append(String.format("[ ... too long (%d) ... ] ", buf.length));
            }
            sb.append(String.format(" %s: expected=%d; vectorized=%d\n", (hqi == vhash ? "SUCCESS" : "ERROR"), hqi, vhash));

            throw new Error(sb.toString());
        }
    }

    static byte[] arr(int b, int len) {
        byte[] res = new byte[len];
        for (int i = 0; i < res.length; i++)  res[i] = (byte)b;
        return res;
    }

    private static final Random r = new Random(0);

    static byte[] randomByteArray(int len) {
        byte[] bytes = new byte[len];
        r.nextBytes(bytes);
        return bytes;
    }

    public static void main(String[] args) {
        if (!CPUID.has(AVX2))  return; // Not supported

        test(arr(0, 0));

        test(arr(0, 1));
        test(arr(0, 3));
        test(arr(0, 4));
        test(arr(0, 8));
        test(arr(0, 10));
        test(arr(0, 12));
        test(arr(0, 13));
        test(arr(0, 50));

        test(arr(1, 1));
        test(arr(1, 3));
        test(arr(1, 4));
        test(arr(1, 8));
        test(arr(1, 10));
        test(arr(1, 12));
        test(arr(1, 13));
        test(arr(1, 50));

        test(new byte[] { 1, 2, 3 });
        test(new byte[] { 1, 2, 3, 4 });
        test(new byte[] { 1, 2, 3, 4, 5 });
        test(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 });
        test(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });
        test(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 });
        test(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 });
        test(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 });

        test(new byte[] {-42, -8, -48, -60, 79, 41, 18, -117, -87, 9});

        for (int i = 0; i < 20_000; i++) {
            test(randomByteArray(35));
        }

        for (int i = 0; i < 20_000; i++) {
            test(randomByteArray(i));
        }
    }

    static private final Unsafe U;
    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            U = (Unsafe)f.get(null);
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }
}
