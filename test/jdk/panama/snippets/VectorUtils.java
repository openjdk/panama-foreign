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

package panama.snippets;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import jdk.internal.misc.Unsafe;
import jdk.vm.ci.code.Register;
import jdk.vm.ci.panama.MachineCodeSnippet;

import static jdk.vm.ci.amd64.AMD64.xmmRegistersSSE;
import static jdk.vm.ci.panama.MachineCodeSnippet.requires;
import static jdk.vm.ci.amd64.AMD64.CPUFeature.*;
import static jdk.vm.ci.panama.MachineCodeSnippet.Effect.*;

public class VectorUtils {

    /* ========================================================================================*/

    static final MethodType MT_L2_BINARY = MethodType.methodType(Long2.class, Long2.class, Long2.class);
    static final MethodType MT_L4_BINARY = MethodType.methodType(Long4.class, Long4.class, Long4.class);

    static final MethodHandle MHm128_shuffle_epi8 = MachineCodeSnippet.make(
            "m128_shuffle_epi8", MT_L2_BINARY, requires(SSE3),
            0x66, 0x0F, 0x38, 0x00, 0xC1); // pshufb xmm0,xmm1

    public static Long2 shuffle_epi8(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2)MHm128_shuffle_epi8.invokeExact(a, b);
//            assert res.equals(shuffle_epi8_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    // FIXME: not correct
//    private static Long2 shuffle_epi8_naive(Long2 a, Long2 b) {
//        Long2 r = Long2.ZERO;
//        for (int i = 0; i < 16; i++) {
//            if ((getByte(b, i) & 0x80) != 0) {
//                r = putByte(r, i, (byte) 0);
//            } else {
//                int idx = getByte(b, i) & 0x0F;
//                byte val = getByte(a, idx);
//                r = putByte(r, i, val);
//            }
//        }
//        return r;
//    }

    /* ========================================================================================*/

    static final MethodHandle MHm256_shuffle_epi8 = MachineCodeSnippet.make(
            "m256_shuffle_epi8", MT_L4_BINARY, requires(AVX2),
            0xC4, 0xE2, 0x75, 0x00, 0xC0); // vpshufb ymm0,ymm1,ymm0

    public static Long4 shuffle_epi8(Long4 a, Long4 b) {
        try {
            Long4 res = (Long4)MHm256_shuffle_epi8.invokeExact(b, a);
//            assert res.equals(shuffle_epi8_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    // FIXME: incorrect
//    private static Long4 shuffle_epi8_naive(Long4 a, Long4 b) {
//        Long2 r = Long2.ZERO;
//        for (int i = 0; i < 32; i++) {
//            if ((getByte(b, i) & 0x80) != 0) {
//                r = putByte(r, i, (byte) 0);
//            } else {
//                int idx = getByte(b, i) & 0x0F;
//                byte val = getByte(a, idx);
//                r = putByte(r, i, val);
//            }
//        }
//        return r;
//    }

    /* ========================================================================================*/

    private static final MethodHandle MHm128_add_epi32 = MachineCodeSnippet.make(
            "m128_add_epi32", MT_L2_BINARY, requires(SSE2),
            0x66, 0x0F, 0xFE, 0xC1); // paddd xmm0,xmm1

    /* ========================================================================================*/

    private static final MethodHandle MHm128_vpadd_epi32 = MachineCodeSnippet.make(
            "m128_add_epi32", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                // VEX.NDS.128.66.0F.WIG FE /r VPADDD xmm1, xmm2, xmm3/m128
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];

                int[] vex = vex2(1, in2.encoding(), 0, 1);
                return new int[]{
                        vex[0], vex[1],
                        0xFE,
                        modRM(out, in1)};
            });


    public static Long2 add_epi32(Long2 a, Long2 b) {
        try {
//            Long2 res = (Long2) MHm128_add_epi32.invokeExact(a, b);
            Long2 res = (Long2) MHm128_vpadd_epi32.invokeExact(a, b);
            assert assertEquals(res, add_epi32_naive(a,b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    private static Long2 add_epi32_naive(Long2 a, Long2 b) {
        long l1 = pack(
                getInt(a, 0) + getInt(b, 0),
                getInt(a, 1) + getInt(b, 1));
        long l2 = pack(
                getInt(a, 2) + getInt(b, 2),
                getInt(a, 3) + getInt(b, 3));
        return Long2.make(l1, l2);
    }

    /* ========================================================================================*/

    // VEX.NDS.256.66.0F.WIG FE /r
    // VPADDD ymm1, ymm2, ymm3/m256
    static final MethodHandle MHm256_add_epi32 = MachineCodeSnippet.make(
            "m256_add_epi32", MT_L4_BINARY, requires(AVX2),
            0xC5, 0xFD, 0xFE, 0xC1); // vpaddd ymm0,ymm0,ymm1

    public static Long4 add_epi32(Long4 s1, Long4 s2) {
        try {
            Long4 res = (Long4) MHm256_add_epi32.invokeExact(s1, s2);
            assert assertEquals(res, add_epi32_naive(s1, s2));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    static Long4 add_epi32_naive(Long4 s1, Long4 s2) {
        Long2 s1lo = lo(s1), s1hi = hi(s1),
                s2lo = lo(s2), s2hi = hi(s2);
        Long2 lo = add_epi32(s1lo, s2lo),
                hi = add_epi32(s1hi, s2hi);
        return pack(lo, hi);
    }

    /* ========================================================================================*/

    static final MethodHandle MHm128_mullo_epi32 = MachineCodeSnippet.make(
            "m128_mullo_epi32", MT_L2_BINARY, requires(SSE4_1),
            0x66, 0x0F, 0x38, 0x40, 0xC1); // pmulld xmm0,xmm1

    public static Long2 mullo_epi32(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm128_mullo_epi32.invokeExact(a, b);
            assert assertEquals(res, mullo_epi32_naive(a,b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    static Long2 mullo_epi32_naive(Long2 a, Long2 b) {
        long l1 = pack(
                getInt(a, 0) * getInt(b, 0),
                getInt(a, 1) * getInt(b, 1));
        long l2 = pack(
                getInt(a, 2) * getInt(b, 2),
                getInt(a, 3) * getInt(b, 3));
        return Long2.make(l1, l2);
    }

    /* ========================================================================================*/

    // TODO: intrinsify

    public static long hadd_epi32(Long2 a) {
        int i1 = getInt(a, 0) + getInt(a, 1);
        int i2 = getInt(a, 2) + getInt(a, 3);
        return pack(i1, i2);
    }

    public static Long2 hadd_epi32(Long4 v) {
        long l1 = hadd_epi32(lo(v));
        long l2 = hadd_epi32(hi(v));
        return Long2.make(l1, l2);
    }

    public static Long2 hadd_epi32(Long2 a, Long2 b) {
        return hadd_epi32(pack(a, b));
    }

    /* ========================================================================================*/

    // VPHADDD: VEX.NDS.256.66.0F38.WIG 02 /r
    static final MethodHandle MHm256_hadd_epi32 = MachineCodeSnippet.make(
            "m256_hadd_epi32", MT_L4_BINARY, requires(AVX2),
            0xC4, 0xE2, 0x75, 0x02, 0xC0); // vphaddd ymm0,ymm1,ymm0

    public static Long4 hadd_epi32(Long4 a, Long4 b) {
        try {
            Long4 res = (Long4) MHm256_hadd_epi32.invokeExact(a, b);
            assert assertEquals(res, hadd_epi32_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error("" + a + b, e);
        }
    }

    private static Long4 hadd_epi32_naive(Long4 a, Long4 b) {
        Long2 l1 = hadd_epi32(a);
        Long2 l2 = hadd_epi32(b);
        return Long4.make(l2.extract(0), l1.extract(0),
                l2.extract(1), l1.extract(1));
    }

    /* ========================================================================================*/

    // VEX.NDS.256.66.0F38.WIG 40 /r
    // VPMULLD ymm1, ymm2, ymm3/m256
    // Multiply the packed dword signed integers in ymm2 and ymm3/m256 and store the low 32 bits of each product in ymm1.
    static final MethodHandle MHm256_mullo_epi32 = MachineCodeSnippet.make(
            "m256_mullo_epi32", MT_L4_BINARY, requires(AVX2),
            0xC4, 0xE2, 0x7D, 0x40, 0xC1); // vpmulld ymm0,ymm0,ymm1

    public static Long4 mullo_epi32(Long4 s1, Long4 s2) {
        try {
            Long4 res = (Long4) MHm256_mullo_epi32.invokeExact(s1, s2);
            assert assertEquals(res, mullo_epi32_naive(s1, s2));
            return res;
        } catch (Throwable e) {
            throw new Error("" + s1 + s2, e);
        }
    }

    static Long4 mullo_epi32_naive(Long4 s1, Long4 s2) {
        Long2 s1lo = lo(s1), s1hi = hi(s1),
                s2lo = lo(s2), s2hi = hi(s2);
        Long2 lo = mullo_epi32(s1lo, s2lo),
                hi = mullo_epi32(s1hi, s2hi);
        return pack(lo, hi);
    }

    /* ========================================================================================*/

    // m128i _mm_insert_epi32(m128i s1, int s2, const int ndx)
    // Insert integer doubleword into packed integer array element selected by index.
    // PINSRD xmm0, r/m32, imm8
    static MethodHandle make_m128_insert_epi32(int loc) {
        if (loc < 0 || loc >= 4)  throw new IllegalArgumentException(""+loc);

        return MachineCodeSnippet.make(
                "mm128_insert_epi32_@"+loc,
                MethodType.methodType(Long2.class, Long2.class, long.class),
                requires(SSE4_1),
                0x66, 0x0F, 0x3A, 0x22, 0xC6, loc);  // pinsrd xmm0,esi,imm8(loc)
    }

//    static @Stable final MethodHandle[] MHm128_insert_epi32 = new MethodHandle[] {
//            make_m128_insert_epi32(0),
//            make_m128_insert_epi32(1),
//            make_m128_insert_epi32(2),
//            make_m128_insert_epi32(3)};

    static final MethodHandle MHm128_insert_epi32_0 = make_m128_insert_epi32(0);
    static final MethodHandle MHm128_insert_epi32_1 = make_m128_insert_epi32(1);
    static final MethodHandle MHm128_insert_epi32_2 = make_m128_insert_epi32(2);
    static final MethodHandle MHm128_insert_epi32_3 = make_m128_insert_epi32(3);

    public static Long2 insert_epi32(Long2 v, int x, int off) {
        try {
            // FIXME: constant fold array elem loads
//                return (Long2) MHm128_insert_epi32[off].invokeExact(v, x);
            switch(off) {
                case 0: return (Long2) MHm128_insert_epi32_0.invokeExact(v, x);
                case 1: return (Long2) MHm128_insert_epi32_1.invokeExact(v, x);
                case 2: return (Long2) MHm128_insert_epi32_2.invokeExact(v, x);
                case 3: return (Long2) MHm128_insert_epi32_3.invokeExact(v, x);
                default: throw new IllegalArgumentException(""+off);
            }
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/

    // m128i _m_insert_epi32(m128i s1, int s2, const int ndx)
    // Insert integer doubleword into packed integer array element selected by index.
    // PINSRQ xmm0, r/m32, imm8
    // 66 REX.W 0F 3A 22 /r ib
    static MethodHandle make_m128_insert_epi64(int loc) {
        if (loc < 0 || loc >= 2)  throw new IllegalArgumentException(""+loc);

        // MT (Object,Object,long)L2
        return MachineCodeSnippet.make("mm128_insert_epi64_@"+loc,
                MethodType.methodType(Long2.class, Long2.class, long.class),
                requires(SSE4_1),
                0x66, 0x48, 0x0F, 0x3A, 0x22, 0xC2, loc); // pinsrq xmm0,rdx,imm8(loc)
    }

//    static @Stable final MethodHandle[] MHm128_insert_epi64 = new MethodHandle[] {
//            make_m128_insert_epi64(0),
//            make_m128_insert_epi64(1)};

    static final MethodHandle MHm128_insert_epi64_0 = make_m128_insert_epi64(0);
    static final MethodHandle MHm128_insert_epi64_1 = make_m128_insert_epi64(1);

    public static Long2 insert_epi64(Long2 v, long x, int off) {
        try {
            // FIXME: constant fold loads from an MH array (e.g. use frozen array or @Stable)
//            return (Long2)MHm128_insert_epi64[off].invokeExact(v, x);
            switch (off) {
                case 0: return (Long2)MHm128_insert_epi64_0.invokeExact(v, x);
                case 1: return (Long2)MHm128_insert_epi64_1.invokeExact(v, x);
                default: throw new IllegalArgumentException(""+off);
            }
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/

    // VEX.NDS.256.66.0F3A.W0 38 /r ib
    // VINSERTI128 ymm1, ymm2, xmm3/m128, imm8
    // Insert 128-bits of integer data from xmm3/mem and the remaining values from ymm2 into ymm1.
    static MethodHandle make_m256_insert_mm128(int loc) {
        if (loc < 0 || loc >= 2)  throw new IllegalArgumentException(""+loc);

        // MT (Object,Object,long)L2
        return MachineCodeSnippet.make("mm256_insert_mm128_@"+loc,
                MethodType.methodType(Long4.class, Long4.class, Long2.class),
                requires(AVX2),
                0xC4, 0xE3, 0x7D, 0x38, 0xC1, loc); // vinserti128 ymm0,ymm0,xmm1,imm8(loc)
    }

    static final MethodHandle MHm256_insert_mm128_0 = make_m256_insert_mm128(0);
    static final MethodHandle MHm256_insert_mm128_1 = make_m256_insert_mm128(1);

    public static Long4 insert_m128(Long4 v, Long2 x, int off) {
        try {
            // FIXME: constant fold loads from an MH array (e.g. use frozen array or @Stable)
//            return (Long2)MHm128_insert_epi64[off].invokeExact(v, x);
            switch (off) {
                case 0: return (Long4)MHm256_insert_mm128_0.invokeExact(v, x);
                case 1: return (Long4)MHm256_insert_mm128_1.invokeExact(v, x);
                default: throw new IllegalArgumentException(""+off);
            }
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/

    // Bitwise XOR of xmm2/m128 and xmm1.
    static MethodHandle make_m128_pxor() {
        // MT (Object,L2,L2)L2
        return MachineCodeSnippet.make("mm128_pxor",
                MT_L2_BINARY, requires(SSE2),
                0x66, 0x0F, 0xEF, 0xC1); // pxor xmm0,xmm1
    }
    static final MethodHandle MHm128_pxor = make_m128_pxor();

    // VPXOR xmm1, xmm2, xmm3/m128
    static final MethodHandle MHm128_vpxor = MachineCodeSnippet.make(
            "mm128_vpxor", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE /*out*/, xmmRegistersSSE /*in1*/, xmmRegistersSSE /*in2*/},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];

                // VEX.NDS.128.66.0F.WIG EF /r
                // VPXOR xmm1, xmm2, xmm3/m128
                int[] vex = vex2(1, in2.encoding(), 0, 1);
                return new int[]{
                        vex[0], vex[1],
                        0xEF,
                        modRM(out, in1)};

            });

    public static Long2 xor(Long2 v1, Long2 v2) {
        try {
//            return (Long2) MHm128_pxor.invokeExact(v1, v2);
            return (Long2) MHm128_vpxor.invokeExact(v1, v2);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }


    /* ========================================================================================*/

    // Bitwise XOR of ymm3/m256 and ymm2.
    // VEX.NDS.256.66.0F.WIG EF /r
    // PXOR ymm1, ymm2, ymm3/m256
    static MethodHandle make_m256_pxor() {
        return MachineCodeSnippet.make("mm256_pxor",
                MT_L4_BINARY, requires(AVX2),
                0xC5, 0xFD, 0xEF, 0xC1); // vpxor ymmm0,ymm0,ymm1
    }

    static final MethodHandle MHm256_pxor = make_m256_pxor();

    public static Long4 xor(Long4 v1, Long4 v2) {
        try {
            return (Long4)MHm256_pxor.invokeExact(v1, v2);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/

    static MethodHandle make_m128_ptest() {
        // MT (L2)boolean
        return MachineCodeSnippet.make("mm128_ptest",
                MethodType.methodType(boolean.class, Long2.class),
                requires(SSE4_1),
                0x48, 0x31, 0xC0,              // xor rax,rax
                0x48, 0x31, 0xC9,              // xor rcx,rcx
                0xB1, 0x01,                    // mov cl,$1
                0x66, 0x0F, 0x38, 0x17, 0xC0,  // ptest xmm0,xmm0
                0x48, 0x0F, 0x45, 0xC1);       // cmovne rax,rcx
    }

    static final MethodHandle MHm128_ptest = make_m128_ptest();

    public static boolean ptest(Long2 v) {
        try {
            return (boolean)MHm128_ptest.invokeExact(v);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/

    // VEX.256.66.0F38.WIG 17 /r
    static MethodHandle make_m256_ptest() {
        // MT (L2)boolean
        return MachineCodeSnippet.make("mm256_ptest",
                MethodType.methodType(boolean.class, Long4.class),
                requires(AVX),
                0x48, 0x31, 0xC0,              // xor rax,rax
                0x48, 0x31, 0xC9,              // xor rcx,rcx
                0xB1, 0x01,                    // mov cl,$1
                0xC4, 0xE2, 0x7D, 0x17, 0xC0,  // vptest ymm0,ymm0
                0x48, 0x0F, 0x45, 0xC1);       // cmovne rax,rcx
    }

    static final MethodHandle MHm256_ptest = make_m256_ptest();

    public static boolean ptest(Long4 v) {
        try {
            return (boolean)MHm256_ptest.invokeExact(v);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/

    static MethodHandle make_m256_vadd() {
        return MachineCodeSnippet.make("mm256_vadd",
                MT_L4_BINARY, requires(AVX2),
                0xC5, 0xF5, 0xFE, 0xC0); // vpaddd %ymm0, %ymm1, %ymm0
    }

    static final MethodHandle MHm256_vadd = make_m256_vadd();

    public static Long4 vadd(Long4 v1, Long4 v2) {
        try {
            return (Long4)MHm256_vadd.invokeExact(v1, v2);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/

    static int modRM (Register r1, Register r2) {
        assert (r1.encoding() & 0x7) == r1.encoding();
        assert (r2.encoding() & 0x7) == r2.encoding();

        return 0xC0 | (r1.encoding() << 3) | r2.encoding();
    }

    static int[] vex3(int rex_r, int rex_x, int rex_b, int m,
                      int rex_w, int r_enc, int vlen, int pp) {
        assert (r_enc & 0x7) == r_enc;

        return new int[] { 0xC4,
                           (rex_r << 7) | (rex_x << 6) | (rex_b << 5) | m,
                           (rex_w << 7) | ((~r_enc & 0xF) << 3) | (vlen << 2) | pp};
    }

    static int[] vex2 (int rex_r, int r_enc, int l, int pp) {
        assert (r_enc & 0x7) == r_enc;
        assert (r_enc & 0x7) == r_enc;

        return new int[] { 0xC5,
                           (rex_r << 7) | ((~r_enc & 0xF) << 3) | (l << 2) | pp};
    }

    /* ========================================================================================*/

    public static Long4 pack(Long2 lo, Long2 hi) {
        Long4 v = Long4.ZERO;
        v = insert_m128(v, lo, 0);
        v = insert_m128(v, hi, 1);
        assert lo.extract(0) == v.extract(0) && lo.extract(1) == v.extract(1) &&
                hi.extract(0) == v.extract(2) && hi.extract(1) == v.extract(3) : "" + v + lo + hi;
        return v;
    }

    public static Long2 lo(Long4 v) {
        return Long2.make(v.extract(0), v.extract(1));
    }

    public static Long2 hi(Long4 v) {
        return Long2.make(v.extract(2), v.extract(3));
    }

    public static Long2 valueOf(int i1, int i2, int i3, int i4) {
        return Long2.make(pack(i1, i2), pack(i3, i4));
    }

    public static Long4 valueOf(int i1, int i2, int i3, int i4,
                                int i5, int i6, int i7, int i8) {
        return Long4.make(pack(i1, i2), pack(i3, i4), pack(i5, i6), pack(i7, i8));
    }

    public static long putByte(long l, int i, byte b) {
        if (i < 0 || i >=8 )  throw new IllegalArgumentException(""+i);
        int bits = 8 * (i % 8);
        return (l & ~((long)0xFF << bits)) | (((long)b & 0xFF) << bits);
    }

    public static Long2 putByte(Long2 l, int i, byte b) {
        if (i < 0 || i >=16 )  throw new IllegalArgumentException(""+i);

        long l1 = l.extract(0), l2 = l.extract(1);

        long lo = (i <  8 ? putByte(l1, i % 8, b) : l1);
        long hi = (i >= 8 ? putByte(l2, i % 8, b) : l2);

        return Long2.make(lo, hi);
    }

    public static byte getByte(Long2 l, int i) {
        if (i < 0 || i >=16 )  throw new IllegalArgumentException(""+i);
        long r = l.extract(i / 8);
        int bits = 8 * (i % 8);
        int val = (int)((r >> bits) & 0xFF);
        return (byte)val;
    }


    public static int getInt(Long2 l, int i) {
        if (i < 0 || i >=4 )  throw new IllegalArgumentException("" + i);
        long r = l.extract(i / 2);
        int bits = 32 * (i % 2);
        int val = (int)(r >> bits);
        return val;
    }

    public static long getLong(Long2 l, int i) {
        if (i < 0 || i >=2 )  throw new IllegalArgumentException(""+i);
        return l.extract(i);
    }

    public static byte getByte(Long4 l, int i) {
        if (i < 0 || i >=32 )  throw new IllegalArgumentException(""+i);
        long r = l.extract(i / 8);
        int bits = 8 * (i % 8);
        int val = (int)((r >> bits) & 0xFF);
        return (byte)val;
    }

    public static int getInt(Long4 l, int i) {
        if (i < 0 || i >=8 )  throw new IllegalArgumentException("" + i);
        long r = l.extract(i / 2);
        int bits = 4 * 8 * (i % 8);
        int val = (int)(r >> bits);
        return val;
    }

    public static long pack(int lo, int hi) {
        long hiPacked = ((long)hi) << 32;
        long loPacked = lo & 0xFFFFFFFFL;
        return hiPacked | loPacked;
    }

    static private final Unsafe U = Unsafe.getUnsafe();

    public static Long4 loadL4(Object base, long offset) {
        return U.getLong4(base, offset);
    }

    public static Long2 loadL2(Object base, long offset) {
        return U.getLong2(base, offset);
    }

    private static boolean assertEquals(Object o1, Object o2) {
        if (o1 == null && o2 == null)    return true;
        if (o1 != null && o1.equals(o2)) return true;
        throw new AssertionError(o1 + " vs " + o2);
    }
}
