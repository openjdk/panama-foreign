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
package com.oracle.vector;

import jdk.internal.misc.Unsafe;
import jdk.internal.vm.annotation.ForceInline;
import jdk.vm.ci.code.Register;
import jdk.vm.ci.panama.MachineCodeSnippet;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import jdk.internal.vm.annotation.Stable;
import java.util.Arrays;
import java.util.stream.IntStream;

import static com.oracle.vector.CPUID.*;
import static com.oracle.vector.CPUID.Capabilities.*;

import static com.oracle.vector.PatchableVecUtils.VEXLength.*;
import static com.oracle.vector.PatchableVecUtils.VEXWBit.*;
import static com.oracle.vector.PatchableVecUtils.VEXRBit.*;
import static com.oracle.vector.PatchableVecUtils.VEXBBit.*;
import static com.oracle.vector.PatchableVecUtils.VEXXBit.*;
import static com.oracle.vector.PatchableVecUtils.SIMDPrefix.*;
import static com.oracle.vector.PatchableVecUtils.VEXOpcode.*;
import static jdk.vm.ci.amd64.AMD64.*;

import java.lang.reflect.Field;

public class PatchableVecUtils {
    private static final MethodType MT_L2_UNARY = MethodType.methodType(Long2.class, Long2.class);
    private static final MethodType MT_L2_BINARY = MethodType.methodType(Long2.class, Long2.class, Long2.class);
    private static final MethodType MT_L4_UNARY = MethodType.methodType(Long4.class, Long4.class);
    private static final MethodType MT_L4_BINARY = MethodType.methodType(Long4.class, Long4.class, Long4.class);
    private static final MethodType MT_INT_BINARY = MethodType.methodType(int.class, int.class, int.class);
    private static final MethodType MT_LONG_BINARY = MethodType.methodType(long.class, long.class, long.class);
    private static final MethodType MT_L2_L2_INT = MethodType.methodType(Long2.class, Long2.class, int.class);
    private static final MethodType MT_L4_L4_INT = MethodType.methodType(Long4.class, Long4.class, int.class);
    private static final MethodType MT_L2_L2_LONG = MethodType.methodType(Long2.class, Long2.class, Long.class);
    private static final MethodType MT_L4_L4_LONG = MethodType.methodType(Long4.class, Long4.class, Long.class);
    private static final MethodType MT_L2_INT = MethodType.methodType(Long2.class,int.class);
    private static final MethodType MT_L4_INT = MethodType.methodType(Long4.class,int.class);
    private static final MethodType MT_L2_BYTE = MethodType.methodType(Long2.class,byte.class);
    private static final MethodType MT_L4_BYTE = MethodType.methodType(Long4.class,byte.class);
    private static final MethodType MT_L2_SHORT = MethodType.methodType(Long2.class,short.class);
    private static final MethodType MT_L4_SHORT = MethodType.methodType(Long4.class,short.class);
    private static final MethodType MT_L2_LONG = MethodType.methodType(Long2.class,long.class);
    private static final MethodType MT_L4_LONG = MethodType.methodType(Long4.class, long.class);
    private static final MethodType MT_L2_LONG_BINARY = MethodType.methodType(Long2.class, Long2.class, long.class);
    private static final MethodType MT_L4_L2 = MethodType.methodType(Long4.class, Long4.class, Long2.class);
    private static final MethodType MT_L4_L2_ = MethodType.methodType(Long2.class, Long4.class);
    private static final MethodType MT_INT_L2 = MethodType.methodType(int.class, Long2.class);
    private static final MethodType MT_FLOAT_FLOAT_L4 = MethodType.methodType(float.class,float.class,Long4.class);
    private static final MethodType MT_FLOAT_FLOAT_L4_L4 = MethodType.methodType(float.class,float.class,Long4.class,Long4.class);


    private static final MethodType MT_VOID_OBJ_LONG_L4 = MethodType.methodType(void.class, Object.class, long.class, Long4.class);
    private static final MethodType MT_VOID_OBJ_LONG_L2 = MethodType.methodType(void.class, Object.class, long.class, Long2.class);
    private static final MethodType MT_L4_OBJ_LONG = MethodType.methodType(Long4.class, Object.class, long.class);
    private static final MethodType MT_L2_OBJ_LONG = MethodType.methodType(Long2.class, Object.class, long.class);
    private static final MethodType MT_LONG_L2 = MethodType.methodType(long.class, Long2.class);
    private static final MethodType MT_L4_L4_FLOATARY_INT = MethodType.methodType(Long4.class, Long4.class, float[].class, int.class);
    private static final MethodType MT_L2_FLOAT = MethodType.methodType(Long2.class, float.class);
    private static final MethodType MT_L4_FLOAT = MethodType.methodType(Long4.class, float.class);
    private static final MethodType MT_L2_DOUBLE = MethodType.methodType(Long2.class, double.class);
    private static final MethodType MT_L4_DOUBLE = MethodType.methodType(Long4.class, double.class);
    private static final MethodType MT_FLOAT_FLOAT_L4_FLOATARY_INT = MethodType.methodType(float.class,float.class,Long4.class,float[].class,int.class);
    private static final MethodType MT_BYTE_L2 = MethodType.methodType(byte.class,Long2.class);
    private static final MethodType MT_L2_L2_BYTE = MethodType.methodType(Long2.class, Long2.class, byte.class);
    private static final MethodType MT_SHORT_L2 = MethodType.methodType(short.class,Long2.class);
    private static final MethodType MT_L2_L2_SHORT = MethodType.methodType(Long2.class, Long2.class, short.class);

    static private final Register[] ub = {r8, r9, r10, r11, r12, r13, r14, r15, xmm8, xmm9, xmm10, xmm11, xmm12, xmm13, xmm14, xmm15};

    static private boolean isUB64(Register r) {
        for (Register anUb : ub) {
            if (r == anUb) {
                return true;
            }
        }
        return false;
    }

    //NOTE: REX bits are 1's-complemented in VEX Bit form.
    static private VEXXBit xBit(Register r) {
        return isUB64(r) ? X_HIGH : X_LOW;
    }

    static private VEXRBit rBit(Register r) {
        return isUB64(r) ? R_HIGH : R_LOW;
    }

    static private VEXBBit bBit(Register r) {
        return isUB64(r) ? B_HIGH : B_LOW;
    }

    static boolean requires(CPUID.Capabilities cap) {
        return isX64() && CPUID.has(cap);
    }

    static int sibByte(Register index, Register base, int scale) {
        return ((scale & 0x3) << 6) | ((index.encoding() & 0x7) << 3) | (base.encoding() & 0x7);
    }

    static int vsibByte(Register index, Register base, int scale) {
        if (!(cpuRegisters[base.encoding()] == base))
            throw new IllegalArgumentException("VSIB Base register must be a GPR");
        if (!(xmmRegistersSSE[index.encoding()] == index))
            throw new IllegalArgumentException("VSIB Index register must be an XMM register");
        return ((scale & 0x3) << 6) | ((index.encoding() & 0x7) << 3) | (base.encoding() & 0x7);
    }

    static int modRM_SIB_NODISP(Register reg) {
        return 0b00000100 | ((reg.encoding() & 0x7) << 3);
    }

    static int modRM_SIB_DISP8(Register reg) {
        return 0b01000100 | ((reg.encoding() & 0x7) << 3);
    }

    static int modRM_SIB_DISP32(Register reg) {
        return 0b10000100 | ((reg.encoding() & 0x7) << 3);
    }

    //Register indirect addressing
    /* REG, R/M , resp.*/
    static int modRM_regInd(Register reg, Register r_m) {
        assert (reg.encoding() & 0x7) == reg.encoding();
        assert (r_m.encoding() & 0x7) == r_m.encoding();
        return /*0x00 |*/ ((reg.encoding() & 0x7) << 3) | (r_m.encoding() & 0x7);
    }

    //Register base+index+disp8 addressing
    static int modRM_reg_bid8(Register reg, Register r_m) {
        assert (reg.encoding() & 0x7) == reg.encoding();
        assert (r_m.encoding() & 0x7) == r_m.encoding();
        return 0x40 | ((reg.encoding() & 0x7) << 3) | (r_m.encoding() & 0x7);
    }

    static int modRM(Register reg, Register r_m) {
        assert (reg.encoding() & 0x7) == reg.encoding();
        assert (r_m.encoding() & 0x7) == r_m.encoding();
        return 0xC0 | ((reg.encoding() & 0x7) << 3) | (r_m.encoding() & 0x7);
    }

    // Vex encoding for instructions with nds registers
    static int[] vex_prefix(VEXRBit r, VEXXBit x, VEXBBit b, VEXOpcode m,
                            VEXWBit w, Register nds, VEXLength len, SIMDPrefix pp) {
        assert ((nds.encoding() & 0x7) == nds.encoding());
        if (b.isHigh() || x.isHigh() || w.isHigh() ||
                (m == M_0F38) || (m == M_0F3A)) {
            int rxb = (~((r.encoding() << 7) | (x.encoding() << 6) | (b.encoding() << 5))) & 0b11100000;
            return new int[]{
                    0xC4,
                    rxb | m.encoding() & 0x3,
                    (w.encoding() << 7) | ((~nds.encoding() & 0xF) << 3) | (len.encoding() << 2) | pp.encoding()};
        } else {
            return new int[]{
                    0xC5,
                    ((~r.encoding() << 7) & 0b10000000) | ((~nds.encoding() & 0xF) << 3) | (len.encoding() << 2) | pp.encoding()};
        }
    }

    // Vex encoding for instructions in 2 address form
    static int[] vex_prefix_nonds(VEXRBit r, VEXXBit x, VEXBBit b, VEXOpcode m,
                                  VEXWBit w, int noreg, VEXLength len, SIMDPrefix pp) {
        if (b.isHigh() || x.isHigh() || w.isHigh() ||
                (m == M_0F38) || (m == M_0F3A)) {
            int rxb = (~((r.encoding() << 7) | (x.encoding() << 6) | (b.encoding() << 5))) & 0b11100000;
            return new int[]{
                    0xC4,
                    rxb | m.encoding() & 0x3,
                    (w.encoding() << 7) | (noreg << 3) | (len.encoding() << 2) | pp.encoding()};
        } else {
            return new int[]{
                    0xC5,
                    ((~r.encoding() << 7) & 0b10000000) | (noreg << 3) | (len.encoding() << 2) | pp.encoding()};
        }
    }

    static int[] vex_emit(int vex[], int... bytes) {
        int[] result = new int[vex.length + bytes.length];
        System.arraycopy(vex, 0, result, 0, vex.length);
        System.arraycopy(bytes, 0, result, vex.length, result.length - vex.length);
        return result;
    }

    static int[] join(int[]... arys) {
        int len = 0;
        int offset = 0;
        for (int[] ary : arys) {
            len += ary.length;
        }
        int[] res = new int[len];
        for (int[] ary : arys) {
            int l = ary.length;
            System.arraycopy(ary, 0, res, offset, l);
            offset += l;
        }
        return res;
    }

    /* ========================================================================================*/
    // Vex emit mem of encoded values
    static int[] vex_emit_mem(int vex[], int opcode, int modRM, int sib, int disp) {
        if (vex.length == 2) {
            return new int[]{
                    vex[0], vex[1],
                    opcode,
                    modRM,
                    sib,
                    disp
            };
        } else if (vex.length == 3) {
            return new int[]{
                    vex[0], vex[1], vex[2],
                    opcode,
                    modRM,
                    sib,
                    disp
            };
        }
        throw new UnsupportedOperationException("vex_emit_mem only supports arrays of length 2 or 3.");
    }
    /* ========================================================================================*/
    static final MethodHandle HBOX_L2_MH = MachineCodeSnippet.make("hboxL2", MethodType.methodType(Long2.class, Long2.class), true);
    static final MethodHandle HBOX_L4_MH = MachineCodeSnippet.make("hboxL4", MethodType.methodType(Long4.class, Long4.class), true);

    public static Long2 hbox(Long2 v) {
        try {
            return (Long2)HBOX_L2_MH.invokeExact(v);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static Long4 hbox(Long4 v) {
        try {
            return (Long4)HBOX_L4_MH.invokeExact(v);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/

    /* ========================================================================================*/
    // VPMULLD xmm1, xmm2, xmm3/m128
    public static final MethodHandle MHm128_vpmulld = MachineCodeSnippet.make(
            "mm128_vpmulld", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE /*out*/, xmmRegistersSSE /*in1*/, xmmRegistersSSE /*in2*/},
            (Register[] regs) -> {
                //VEX.NDS.128.66.0F38.WIG 40 /r
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F38, W_LOW, in1, L_128, PP_66);
                return vex_emit(vex, 0x40, modRM(out, in2));
            });

    private static Long2 vpmulld_naive(Long2 a, Long2 b) {
        long l1 = pack(
                getInt(a, 0) * getInt(b, 0),
                getInt(a, 1) * getInt(b, 1));
        long l2 = pack(
                getInt(a, 2) * getInt(b, 2),
                getInt(a, 3) * getInt(b, 3));
        return Long2.make(l1, l2);
    }

    public static Long2 vpmulld(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm128_vpmulld.invokeExact(a, b);
            assert assertEquals(res, vpmulld_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VPMULLD ymm1, ymm2, ymm3/m256
    public static final MethodHandle MHm256_vpmulld = MachineCodeSnippet.make(
            "mm256_vpmulld", MT_L4_BINARY, requires(AVX2),
            new Register[][]{xmmRegistersSSE /*out*/, xmmRegistersSSE /*in1*/, xmmRegistersSSE /*in2*/},
            (Register[] regs) -> {
                //VEX.NDS.256.66.0F38.WIG 40 /r
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                int[] vex = vex_prefix(R_LOW, X_LOW, B_LOW, M_0F38, W_LOW, in1, L_256, PP_66);
                return vex_emit(vex, 0x40, modRM(out, in2));
            });

    private static Long4 vpmulld_naive(Long4 a, Long4 b) {
        int[] res = new int[8];
        for (int i = 0; i < 8; i++) {
            res[i] = getInt(a, i) * getInt(b, i);
        }
        return Long4.make(
                pack(res[0], res[1]),
                pack(res[2], res[3]),
                pack(res[4], res[5]),
                pack(res[6], res[7]));
    }

    public static Long4 vpmulld(Long4 a, Long4 b) {
        try {
            Long4 res = (Long4) MHm256_vpmulld.invokeExact(a, b);
            assert assertEquals(res, vpmulld_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VPXOR xmm1, xmm2, xmm3/m128
    public static final MethodHandle MHm128_vpxor = MachineCodeSnippet.make(
            "mm128_vpxor", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE /*out*/, xmmRegistersSSE /*in1*/, xmmRegistersSSE /*in2*/},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                // VEX.NDS.128.66.0F.WIG EF /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_128, PP_66);
                return vex_emit(vex, 0xEF, modRM(out, in2));
            });

    private static Long2 vpxor_naive(Long2 a, Long2 b) {
        long la0, la1, lb0, lb1;
        la0 = a.extract(0);
        la1 = a.extract(1);
        lb0 = b.extract(0);
        lb1 = b.extract(1);
        return Long2.make(la0 ^ lb0, la1 ^ lb1);
    }

    public static Long2 vpxor(Long2 v1, Long2 v2) {
        try {
            Long2 res = (Long2) MHm128_vpxor.invokeExact(v1, v2);
            assert assertEquals(res, vpxor_naive(v1, v2));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VPXOR ymm1, ymm2, ymm3/m256
    public static final MethodHandle MHm256_vpxor = MachineCodeSnippet.make(
            "mm256_vpxor", MT_L4_BINARY, requires(AVX2),
            new Register[][]{xmmRegistersSSE /*out*/, xmmRegistersSSE /*in1*/, xmmRegistersSSE /*in2*/},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                // VEX.NDS.256.66.0F.WIG EF /r
                int[] vex = vex_prefix(R_LOW, X_LOW, B_LOW, M_0F, W_LOW, in1, L_256, PP_66);
                return vex_emit(vex, 0xEF, modRM(out, in2));
            });

    private static Long4 vpxor_naive(Long4 a, Long4 b) {
        int[] res = new int[8];
        for (int i = 0; i < 8; i++) {
            int a_i = getInt(a, i);
            int b_i = getInt(b, i);
            res[i] = a_i ^ b_i;
        }
        return Long4.make(
                pack(res[0], res[1]),
                pack(res[2], res[3]),
                pack(res[4], res[5]),
                pack(res[6], res[7])
        );
    }

    public static Long4 vpxor(Long4 v1, Long4 v2) {
        try {
            Long4 res = (Long4) MHm256_vpxor.invokeExact(v1, v2);
            assert assertEquals(res, vpxor_naive(v1, v2));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VPAND xmm1, xmm2, xmm3/m128
    public static final MethodHandle MHm128_vpand = MachineCodeSnippet.make(
            "mm128_vpand", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE /*out*/, xmmRegistersSSE /*in1*/, xmmRegistersSSE /*in2*/},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                // VEX.NDS.128.66.0F.WIG DB /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_128, PP_66);
                return vex_emit(vex, 0xDB, modRM(out, in2));
            });

    private static Long2 vpand_naive(Long2 a, Long2 b) {
        long la0, la1, lb0, lb1;
        la0 = a.extract(0);
        la1 = a.extract(1);
        lb0 = b.extract(0);
        lb1 = b.extract(1);
        return Long2.make(la0 & lb0, la1 & lb1);
    }

    public static Long2 vpand(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm128_vpand.invokeExact(a, b);
            assert assertEquals(res, vpand_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VPAND ymm1, ymm2, ymm3/m256
    public static final MethodHandle MHm256_vpand = MachineCodeSnippet.make(
            "mm256_vpand", MT_L4_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE /*out*/, xmmRegistersSSE /*in1*/, xmmRegistersSSE /*in2*/},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                // VEX.NDS.256.66.0F.WIG DB /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_256, PP_66);
                return vex_emit(vex, 0xDB, modRM(out, in2));
            });

    private static Long4 vpand_naive(Long4 a, Long4 b) {
        //TODO: Write this test
        return null;
    }

    public static Long4 vpand(Long4 a, Long4 b) {
        try {
            Long4 res = (Long4) MHm256_vpand.invokeExact(a, b);
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VPOR xmm1, xmm2, xmm3/m128
    public static final MethodHandle MHm128_vpor = MachineCodeSnippet.make(
            "mm128_vpor", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE /*out*/, xmmRegistersSSE /*in1*/, xmmRegistersSSE /*in2*/},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                // VEX.NDS.128.66.0F.WIG EB /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_128, PP_66);
                return vex_emit(vex, 0xEB, modRM(out, in2));
            });

    private static Long2 vpor_naive(Long2 a, Long2 b) {
        long la0, la1, lb0, lb1;
        la0 = a.extract(0);
        la1 = a.extract(1);
        lb0 = b.extract(0);
        lb1 = b.extract(1);
        return Long2.make(la0 | lb0, la1 | lb1);
    }

    public static Long2 vpor(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm128_vpor.invokeExact(a, b);
            assert assertEquals(res, vpor_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VPSUBD xmm1, xmm2, xmm3/m128
    public static final MethodHandle MHm128_vpsubd = MachineCodeSnippet.make(
            "mm128_vpsubd", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                // VEX.NDS.128.66.0F.WIG FA /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_128, PP_66);
                return vex_emit(vex, 0xFA, modRM(out, in2));
            });

    private static Long2 vpsubd_naive(Long2 a, Long2 b) {
        long l1 = pack(
                getInt(a, 0) - getInt(b, 0),
                getInt(a, 1) - getInt(b, 1));
        long l2 = pack(
                getInt(a, 2) - getInt(b, 2),
                getInt(a, 3) - getInt(b, 3));
        return Long2.make(l1, l2);
    }

    public static Long2 vpsubd(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm128_vpsubd.invokeExact(a, b);
            assert assertEquals(res, vpsubd_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VPSUBD ymm1, ymm2, ymm3/m256
    public static final MethodHandle MHm256_vpsubd = MachineCodeSnippet.make(
            "mm256_vpsubd", MT_L4_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                // VEX.NDS.256.66.0F.WIG FA /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_256, PP_66);
                return vex_emit(vex, 0xFA, modRM(out, in2));
            });

    private static Long4 vpsubd_naive(Long4 a, Long4 b) {
        //TODO: Write this test
        return null;
    }

    public static Long4 vpsubd(Long4 a, Long4 b) {
        try {
            Long4 res = (Long4) MHm256_vpsubd.invokeExact(a, b);
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VPSIGND xmm1, xmm2, xmm3/m128
    public static final MethodHandle MHm128_vpsign = MachineCodeSnippet.make(
            "mm128_vpsignd", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.128.66.0F38.WIG 0A /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F38, W_LOW, in1, L_128, PP_66);
                return vex_emit(vex, 0x0A, modRM(out, in2));
            });

    private static Long2 vpsignd_naive(Long2 a, Long2 b) {
        int val;
        int[] res = new int[4];
        for (int i = 0; i < 4; i++) {
            val = getInt(b, i);
            if (val < 0) {
                res[i] = -1 * (getInt(a, i));
            } else if (val == 0) {
                res[i] = 0;
            } else {
                res[i] = getInt(a, i);
            }

        }
        return Long2.make(pack(res[0], res[1]), pack(res[2], res[3]));
    }

    public static Long2 vpsignd(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm128_vpsign.invokeExact(a, b);
            assert assertEquals(res, vpsignd_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VPSIGND ymm1, ymm2, ymm3/m256
    public static final MethodHandle MHm256_vpsign = MachineCodeSnippet.make(
            "mm256_vpsignd", MT_L4_BINARY, requires(AVX2),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.256.66.0F38.WIG 0A /r
                int[] vex = vex_prefix(R_LOW, X_LOW, B_LOW, M_0F38, W_LOW, in1, L_256, PP_66);
                return vex_emit(vex, 0x0A, modRM(out, in2));
            });

    private static Long4 vpsignd_naive(Long4 a, Long4 b) {
        int val;
        int[] res = new int[8];
        for (int i = 0; i < 8; i++) {
            val = getInt(b, i);
            if (val < 0) {
                res[i] = -1 * (getInt(a, i));
            } else if (val == 0) {
                res[i] = 0;
            } else {
                res[i] = getInt(a, i);
            }

        }
        return Long4.make(pack(res[0], res[1]), pack(res[2], res[3]), pack(res[4], res[5]), pack(res[6], res[7]));
    }

    public static Long4 vpsignd(Long4 a, Long4 b) {
        try {
            Long4 res = (Long4) MHm256_vpsign.invokeExact(a, b);
            assert assertEquals(res, vpsignd_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VPCMPEQD xmm1, xmm2, xmm3/m128
    public static final MethodHandle MHm128_vpcmpeqd = MachineCodeSnippet.make(
            "mm128_vpcmpeqd", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.128.66.0F.WIG 76 /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_128, PP_66);
                return vex_emit(vex, 0x76, modRM(out, in2));
            });

    static Long2 vpcmpeqd_naive(Long2 a, Long2 b) {
        int[] res = new int[4];
        for (int i = 0; i < 4; i++) {
            int a_i = getInt(a, i);
            int b_i = getInt(b, i);
            if (a_i == b_i) {
                res[i] = 0xFFFFFFFF;
            }
        }
        return Long2.make(
                pack(res[0], res[1]),
                pack(res[2], res[3])
        );
    }

    public static Long2 vpcmpeqd(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm128_vpcmpeqd.invokeExact(a, b);
            assert assertEquals(res, vpcmpeqd_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VPCMPEQD ymm1, ymm2, ymm3/m256
    public static final MethodHandle MHm256_vpcmpeqd = MachineCodeSnippet.make(
            "mm256_vpcmpeqd", MT_L4_BINARY, requires(AVX2),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.256.66.0F.WIG 76 /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_256, PP_66);
                return vex_emit(vex, 0x76, modRM(out, in2));
            });

    static Long4 vpcmpeqd_naive(Long4 a, Long4 b) {
       /* double[] res = new double[4];
        for (int i = 0; i < 4; i++) {
            int a_i = getDouble(a, i);
            int b_i = getDouble(b, i);
            if (a_i == b_i) {
                res[i] = 0xFFFFFFFF;
            }
        }
        return Long2.make(
                pack(res[0], res[1]),
                pack(res[2], res[3])
        );*/
        return null;
    }

    public static Long4 vpcmpeqd(Long4 a, Long4 b) {
        try {
            Long4 res = (Long4) MHm256_vpcmpeqd.invokeExact(a, b);
            //assert assertEquals(res, vpcmpeqd_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VPCMPGTD xmm1, xmm2, xmm3/m128
    public static final MethodHandle MHm128_vpcmpgtd = MachineCodeSnippet.make(
            "mm128_vpcmpgtd", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.128.66.0F.WIG 66 /r
                int[] vex = vex_prefix(rBit(out),X_LOW,bBit(in2),M_0F,W_LOW,in1,L_128,PP_66);
                return vex_emit(vex, 0x66, modRM(out, in2));
            });

    static Long2 vpcmpgtd_naive(Long2 a, Long2 b) {
        /*int[] res = new int[4];
        for (int i = 0; i < 4; i++) {
            int a_i = getInt(a, i);
            int b_i = getInt(b, i);
            if (a_i == b_i) {
                res[i] = 0xFFFFFFFF;
            }
        }
        return Long2.make(
                pack(res[0], res[1]),
                pack(res[2], res[3])
        );*/
        return null;
    }

    public static Long2 vpcmpgtd(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm128_vpcmpgtd.invokeExact(a, b);
            //assert assertEquals(res, vpcmpgtd_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }
    /* ========================================================================================*/
    // VPCMPGTD ymm1, ymm2, ymm3/m256
    public static final MethodHandle MHm256_vpcmpgtd = MachineCodeSnippet.make(
            "mm256_vpcmpgtd", MT_L4_BINARY, requires(AVX2),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.256.66.0F.WIG 66 /r
                int[] vex = vex_prefix(rBit(out),X_LOW,bBit(in2),M_0F,W_LOW,in1,L_256,PP_66);
                return vex_emit(vex, 0x66, modRM(out, in2));
            });

    static Long4 vpcmpgtd_naive(Long4 a, Long4 b) {
       /* double[] res = new double[4];
        for (int i = 0; i < 4; i++) {
            int a_i = getDouble(a, i);
            int b_i = getDouble(b, i);
            if (a_i == b_i) {
                res[i] = 0xFFFFFFFF;
            }
        }
        return Long2.make(
                pack(res[0], res[1]),
                pack(res[2], res[3])
        );*/
        return null;
    }

    public static Long4 vpcmpgtd(Long4 a, Long4 b) {
        try {
            Long4 res = (Long4) MHm256_vpcmpgtd.invokeExact(a, b);
            //assert assertEquals(res, vpcmpgtd_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    //VANDPD xmm1, xmm2, xmm3/m128
    public static final MethodHandle MHm128_vandpd = MachineCodeSnippet.make(
            "mm128_vandpd", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.128.66.0F.WIG 54 /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_128, PP_66);
                return vex_emit(vex, 0x54, modRM(out, in2));
            });

    private static Long2 vandpd_naive(Long2 a, Long2 b) {
        //TODO: Write this test.
        return null;
    }

    public static Long2 vandpd(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm128_vandpd.invokeExact(a, b);
            //assert assertEquals(res, VANDPS_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }
    /* ========================================================================================*/
    //VANDPD ymm1, ymm2, ymm3/m256
    public static final MethodHandle MHm256_vandpd = MachineCodeSnippet.make(
            "mm256_vandpd", MT_L4_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.256.66.0F.WIG 54 /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_256, PP_66);
                return vex_emit(vex, 0x54, modRM(out, in2));
            });

    private static Long4 vandpd_naive(Long4 a, Long4 b) {
        //TODO: Write this test.
        return null;
    }

    public static Long4 vandpd(Long4 a, Long4 b) {
        try {
            Long4 res = (Long4) MHm256_vandpd.invokeExact(a, b);
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VXORPS xmm1, xmm2, xmm3/m128
    public static final MethodHandle MHm128_vxorps = MachineCodeSnippet.make(
            "mm128_vxorps", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.128.0F.WIG 57 /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_128, PP_NONE);
                return vex_emit(vex, 0x57, modRM(out, in2));
            });

    static Long2 vxorps_naive(Long2 a, Long2 b) {
        int[] res = new int[4];
        for (int i = 0; i < 4; i++) {
            int a_i = getInt(a, i);
            int b_i = getInt(b, i);
            res[i] = a_i ^ b_i;

        }
        return Long2.make(
                pack(res[0], res[1]),
                pack(res[2], res[3])
        );
    }

    public static Long2 vxorps(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm128_vxorps.invokeExact(a, b);
            assert assertEquals(res, vxorps_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VXORPS ymm1, ymm2, ymm3/m256
    public static final MethodHandle MHm256_vxorps = MachineCodeSnippet.make(
            "mm256_vxorps", MT_L4_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.256.0F.WIG 57 /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_256, PP_NONE);
                return vex_emit(vex, 0x57, modRM(out, in2));
            });

    static Long4 vxorps_naive(Long4 a, Long4 b) {
        int[] res = new int[8];
        for (int i = 0; i < 8; i++) {
            int a_i = getInt(a, i);
            int b_i = getInt(b, i);
            res[i] = a_i ^ b_i;

        }
        return Long4.make(
                pack(res[0], res[1]),
                pack(res[2], res[3]),
                pack(res[4], res[5]),
                pack(res[6], res[7])
        );
    }

    public static Long4 vxorps(Long4 a, Long4 b) {
        try {
            Long4 res = (Long4) MHm256_vxorps.invokeExact(a, b);
            assert assertEquals(res, vxorps_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VXORPD xmm1, xmm2, xmm3/m128
    public static final MethodHandle MHm128_vxorpd = MachineCodeSnippet.make(
            "mm128_vxorpd", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.128.66.0F.WIG 57 /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_128, PP_66);
                return vex_emit(vex, 0x57, modRM(out, in2));
            });

    static Long2 vxorpd_naive(Long2 a, Long2 b) {
        long[] res = new long[4];
        for (int i = 0; i < 2; i++) {
            long a_i = getLong(a, i);
            long b_i = getLong(b, i);
            res[i] = a_i ^ b_i;

        }
        return Long2.make(
                res[0], res[1]
        );
    }

    public static Long2 vxorpd(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm128_vxorpd.invokeExact(a, b);
            assert assertEquals(res, vxorpd_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VXORPD ymm1, ymm2, ymm3/m256
    public static final MethodHandle MHm256_vxorpd = MachineCodeSnippet.make(
            "mm256_vxorpd", MT_L4_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.256.66.0F.WIG 57 /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_256, PP_66);
                return vex_emit(vex, 0x57, modRM(out, in2));
            });

    static Long4 vxorpd_naive(Long4 a, Long4 b) {
        /*long[] res = new long[4];
        for (int i = 0; i < 4; i++) {
            long a_i = getLong(a, i);
            long b_i = getLong(b, i);
            res[i] = a_i ^ b_i;

        }
        return Long2.make(
                res[0], res[1]
        );*/
        return null;
    }

    public static Long4 vxorpd(Long4 a, Long4 b) {
        try {
            Long4 res = (Long4) MHm256_vxorpd.invokeExact(a, b);
            //assert assertEquals(res, vxorpd_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // RADD8F xmm1, ymm2, xmm3
    public static Long2 radd4f(Long2 sum, Long4 b) {
        try {
            sum = (Long2) MHm32_vaddss.invokeExact(sum, b);
            Long2 tmp = (Long2) MHm128_vpshufd_handles[1].invokeExact(b);
            sum = (Long2) MHm32_vaddss.invokeExact(sum, tmp);
            tmp = (Long2) MHm128_vpshufd_handles[2].invokeExact(b);
            sum = (Long2) MHm32_vaddss.invokeExact(sum, tmp);
            tmp = (Long2) MHm128_vpshufd_handles[3].invokeExact(b);
            sum = (Long2) MHm32_vaddss.invokeExact(sum, tmp);
            return sum;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // createGatherIndex r32, r32, res
    public static Long2 createGatherIndex(int vindex_stride, int induction_var, Long2 res) {
        try {
            Long2 xmm1 = (Long2) MHm32_vmovd_gpr2xmm.invokeExact(vindex_stride);
            Long2 xmm3 = (Long2) MHm32_vmovd_gpr2xmm.invokeExact(induction_var);
            res = (Long2) MHm128_vpxor.invokeExact(res, res);
            res = (Long2) MHm128_vblendps_handles[1].invokeExact(res, xmm1);
            xmm1 = (Long2) MHm32_vaddss.invokeExact(xmm1, xmm3);
            res = (Long2) MHm128_vblendps_handles[2].invokeExact(res, xmm1);
            xmm1 = (Long2) MHm32_vaddss.invokeExact(xmm1, xmm3);
            res = (Long2) MHm128_vblendps_handles[3].invokeExact(res, xmm1);//, 0x4); //TODO: 4?
            xmm1 = (Long2) MHm32_vaddss.invokeExact(xmm1, xmm3);
            res = (Long2) MHm128_vblendps_handles[8].invokeExact(res, xmm1);//, 0x8);
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    //RADD8F ymmX, ymmY, ymmZ
    //Also requires 64-bit mode
    public static final MethodHandle radd8f = MachineCodeSnippet.make(
            "radd8f", MT_FLOAT_FLOAT_L4,
            new MachineCodeSnippet.Effect[]{},
            requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            //new Register[]{}, //This macro instruction kills these registers
            //new int[]{},
            (Register[] regs) -> {
                return join(xmmMov(xmm8, regs[1], L_256), //Load our input registers onto ymm8 and ymm9
                            xmmMov(xmm9, regs[2], L_256),
                                      /*
                                          ;three register reduction
                                          ;All three registers killed in this example.
                                          ;ymm8 - sum
                                          ;ymm9 - input
                                          ;ymm10 - tmp
                                          vaddss  xmm8,  xmm8, xmm9
                                          vpshufd xmm10, xmm9, 0x1
                                          vaddss  xmm8,  xmm8, xmm10
                                          vpshufd xmm10, xmm9, 0x2
                                          vaddss  xmm8,  xmm8, xmm10
                                          vpshufd xmm10, xmm9, 0x3
                                          vaddss  xmm8,  xmm8, xmm10
                                          vextractf128   xmm9, ymm9, 0x1
                                          vaddss  xmm8,  xmm8, xmm9
                                          vpshufd xmm10, xmm9, 0x1
                                          vaddss  xmm8,  xmm8, xmm10
                                          vpshufd xmm10, xmm9, 0x2
                                          vaddss  xmm8,  xmm8, xmm10
                                          vpshufd xmm10, xmm9, 0x3
                                          vaddss  xmm8,  xmm8, xmm10
                                       */
                        new int[]{0xc4, 0x41, 0x3a, 0x58, 0xc1, 0xc4, 0x41, 0x79, 0x70, 0xd1, 0x01, 0xc4, 0x41, 0x3a, 0x58, 0xc2, 0xc4, 0x41, 0x79, 0x70, 0xd1, 0x02, 0xc4, 0x41, 0x3a, 0x58, 0xc2, 0xc4, 0x41, 0x79, 0x70, 0xd1, 0x03, 0xc4, 0x41, 0x3a, 0x58, 0xc2, 0xc4, 0x43, 0x7d, 0x19, 0xc9, 0x01, 0xc4, 0x41, 0x3a, 0x58, 0xc1, 0xc4, 0x41, 0x79, 0x70, 0xd1, 0x01, 0xc4, 0x41, 0x3a, 0x58, 0xc2, 0xc4, 0x41, 0x79, 0x70, 0xd1, 0x02, 0xc4, 0x41, 0x3a, 0x58, 0xc2, 0xc4, 0x41, 0x79, 0x70, 0xd1, 0x03, 0xc4, 0x41, 0x3a, 0x58, 0xc2},
                        xmmMov(regs[0], xmm8, L_256)); //Spill the result to the output register
            }
    );

    public static Long4 radd8f(Long4 acc, Long4 addend){
        try {
            Long4 res = (Long4) radd8f.invokeExact(acc,addend);
            return res;
        } catch (Throwable e){
            throw new Error(e);
        }
    }

    //RADD8F_UNSTABLE ymmX, ymmY, ymmZ
    //Numerically less-stable horizontal reduction (vhaddps)
    //See the assembly to determine associativity
    public static final MethodHandle radd8f_unstable = MachineCodeSnippet.make(
            "radd8f_unstable", MT_L4_BINARY,
            new MachineCodeSnippet.Effect[]{},
            requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            new Register[]{xmm8, xmm9, xmm10}, //This macro instruction kills these registers
            new int[]{256, 256, 256},
            (Register[] regs) -> join(xmmMov(xmm8, regs[1], L_256), //Load our input registers onto ymm8 and ymm9
                    xmmMov(xmm9, regs[2], L_256),
                                      /*
                                         ;ymm8  input1
                                         ;ymm9  input2
                                         ;ymm10 scratch
                                         vaddps  ymm10, ymm8,  ymm9  ;combine both operands
                                         vpxor   ymm8,  ymm8,  ymm8  ;zero ymm8 for additive identity
                                         vhaddps ymm10, ymm10, ymm8  ;ymm10 [0,1,2,3] with zeroed upper slots
                                         vhaddps ymm10, ymm10, ymm8  ;ymm10 [0,1] with zeroed upper slots
                                         vhaddps ymm10, ymm10, ymm8  ;ymm10 [0] with zeroed upper slots
                                         vextractf128 xmm8, ymm10, 0x1
                                         vaddss xmm10, xmm8, xmm10
                                       */
                    new int[]{0xc4, 0x41, 0x3c, 0x58, 0xd1, 0xc4, 0x41, 0x3d, 0xef, 0xc0, 0xc4, 0x41, 0x2f, 0x7c, 0xd0, 0xc4, 0x41, 0x2f, 0x7c, 0xd0, 0xc4, 0x41, 0x2f, 0x7c, 0xd0, 0xc4, 0x43, 0x7d, 0x19, 0xd0, 0x01, 0xc4, 0x41, 0x3a, 0x58, 0xd2},
                    xmmMov(regs[0], xmm10, L_256)) //Spill the result to the output register
    );

    //Reg-to-reg vmovdqu
    private static int[] xmmMov(Register dst, Register src, VEXLength l) {
        if (Arrays.binarySearch(xmmRegistersSSE, dst) < 0 || Arrays.binarySearch(xmmRegistersSSE, src) < 0)
            throw new UnsupportedOperationException("xmmMov needs xmm registers");
        int[] vex = vex_prefix_nonds(rBit(dst), X_LOW, bBit(src), M_0F, W_LOW, 0b1111, l, PP_F3);
        return vex_emit(vex, 0x6F, modRM(dst, src));
    }



    public static float dot_prod(float partial, Long4 left, Long4 right){
        try {
           return (float) sumprod_float_L4.invokeExact(partial,left,right);
        } catch (Throwable e){
            throw new Error(e);
        }
    }


    //(float,Long4,Long4)float
    public static final MethodHandle sumprod_float_L4 = MachineCodeSnippet.make(
        "sumprod_float_L4", MT_FLOAT_FLOAT_L4_L4,
            new MachineCodeSnippet.Effect[]{},
            requires(AVX),
            new Register[][]{xmmRegistersSSE,xmmRegistersSSE,xmmRegistersSSE,xmmRegistersSSE},
            new Register[]{},
            new int[]{},
            (Register[] regs) -> {
               Register out  = regs[0],
                        psum = regs[1],
                        inp1 = regs[2],
                        inp2 = regs[3];
                return join(
                              vmulps(inp1,inp1,inp2,L_256)  //get products
                        ,     vaddss(psum,inp1)             //add partial sum
                        ,     vextractf128(psum,inp1,0x1)   //upper 128-bits of inp1
                        ,     vaddps(psum,inp1,psum,L_128)  //fold inp1 in half into psum
                        ,     vmovshdup(inp1,psum,L_128)    //expand (inp1's) upper qwords into inp1
                        ,     vaddps(psum,psum,inp1,L_128)  //and add to psum
                        ,     vmovhlps(inp1,inp1,psum)      //expand the lower qwords
                        ,     vaddss(out, psum, inp1)       //and add the lowest position to get result
                );
            }
    );

    //(float,Long4,Long4)float
    public static final MethodHandle sumprod_float_L4_sunk = MachineCodeSnippet.make(
            "sumprod_float_L4", MT_FLOAT_FLOAT_L4_FLOATARY_INT,
            new MachineCodeSnippet.Effect[]{},
            requires(AVX),
            new Register[][]{xmmRegistersSSE,xmmRegistersSSE,xmmRegistersSSE,cpuRegisters,cpuRegisters},
            new Register[]{},
            new int[]{},
            (Register[] regs) -> {
                Register out  = regs[0],
                        psum  = regs[1],
                        inp1  = regs[2],
                        base  = regs[3],
                        index = regs[4];
                return join(
                         vaddss(psum,inp1)
                   ,     vmulps_sunk(inp1,inp1,base,index,L_256)
                   ,     vextractf128(psum,inp1,0x1)
                   ,     vaddps(psum,inp1,psum,L_128)
                   ,     vmovshdup(inp1,psum,L_128)
                   ,     vaddps(psum,psum,inp1,L_128)
                   ,     vmovhlps(inp1,inp1,psum)
                   ,     vaddss(out, psum, inp1)
                );
            }
    );

    //VEX 256.F3.0F.WIG 16 /r
    public static int[] vmovshdup(Register out, Register in, VEXLength l){
       int[] vex = vex_prefix_nonds(rBit(out), X_LOW, B_LOW, M_0F, W_LOW, 0b1111, l, PP_F3);
       return vex_emit(vex, 0x16, modRM(out, in));
    }

    //VEX.NDS.128.0F.WIG 12 /r
    public static int[] vmovhlps(Register out, Register left, Register right){
        int[] vex = vex_prefix(rBit(out), X_LOW, bBit(right), M_0F, W_LOW, left, L_128, PP_NONE);
        return vex_emit(vex, 0x12, modRM(out, right));
    }

    private static int[] vaddps(Register out, Register left, Register right, VEXLength l){
        int[] vex = vex_prefix(rBit(out), X_LOW, bBit(right), M_0F, W_LOW, left, l, PP_NONE);
        return vex_emit(vex, 0x58, modRM(out, right));
    }

    private static int[] vaddss(Register out, Register left, Register right){
        int[] vex = vex_prefix(rBit(out), X_LOW, bBit(right), M_0F, W_LOW, left, L_128, PP_F3);
        return vex_emit(vex, 0x58, modRM(out, right));
    }

    private static int[] vaddss(Register left, Register right){
        return new int[]{0xF3, 0x0F, 0x58, modRM(right, left)};
    }

    private static int[] vmulps(Register out, Register left, Register right, VEXLength l){
        int[] vex = vex_prefix(rBit(out), X_LOW, bBit(right), M_0F, W_LOW, left, l, PP_NONE);
        return vex_emit(vex, 0x59, modRM(out, right));
    }

    private static int[] vmulps_sunk(Register out, Register inp, Register base, Register index, VEXLength l){
        int[] vex = vex_prefix(rBit(out), xBit(index), bBit(base), M_0F, W_LOW, inp, l, PP_NONE);
        return vex_emit(vex, 0x59, modRM_SIB_DISP8(out), sibByte(index, base, sibScale(Unsafe.ARRAY_FLOAT_INDEX_SCALE)), Unsafe.ARRAY_FLOAT_BASE_OFFSET);
    }

    private static int[] vextractf128(Register out, Register in, int imm){
        int[] vex = vex_prefix_nonds(rBit(in), X_LOW, bBit(out), M_0F3A, W_LOW, 0b1111, L_256, PP_66);
        return vex_emit(vex, 0x19, modRM(in, out), imm & 0x1);
    }



    /* ========================================================================================*/
    // broadcastDwords xmm1, r32
    public static Long2 broadcastDwords(int a) {
        try {
            Long2 r1 = (Long2) MHm32_vmovd_gpr2xmm.invokeExact(a);
            Long2 res = (Long2) MHm256_vpbroadcastd.invokeExact(r1);
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VMOVSS xmm1, xmm2, xmm3/m32
    public static final MethodHandle MHm32_vmovss = MachineCodeSnippet.make(
            "mm32_vmovss", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.LIG.F3.0F.WIG 10 /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_128, PP_F3);
                return vex_emit(vex, 0x10, modRM(out, in2));
            });

    static Long2 vmovss_naive(Long2 a, Long2 b) {
        float[] res = new float[4];
        for (int i = 0; i < 4; i++) {
            float a_i = getFloat(a, i);
            if (i == 0) {
                res[i] = getFloat(b, i);
            } else {
                res[i] = a_i;
            }
        }
        return Long2.make(
                pack(res[0], res[1]),
                pack(res[2], res[3])
        );
    }

    public static Long2 vmovss(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm32_vmovss.invokeExact(a, b);
            assert assertEquals(res, vmovss_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    //FIXME: TBD On how to encode these instructions and when.
    // VGATRHERDPS ymm1, vm32x, ymm2
//    static final MethodHandle MHm256_vgatherdps = MachineCodeSnippet.make(
//            "mm256_vgatherdps", MT_L2_BINARY, requires(AVX),
//            new Register[][]{xmmRegistersSSE, cpuRegisters, xmmRegistersSSE, xmmRegistersSSE},
//            (Register[] regs) -> {
//                Register out = regs[0];
//                Register base = regs[1];
//                Register vindex = regs[2];
//                Register mask = regs[3];
//                //VEX.NDS.LIG.66.0F38.WIG 92 /r
//                //because we are ps, we know the data data is float and ergo the scale is 4(0x2).
//                int[] vex = vex_prefix(R_HIGH,X_HIGH,B_HIGH,M_0F38,W_LOW,memreg,L_256,PP_66);
//                // fixme: need real disp not just dummied up disp8
//                // fixme: importing this code, not sure about sib byte ordering?
//                return vex_emit_mem(vex, 0x92, modRM_reg_bid8(out,mask), sibByte(base,vindex,0x2), 0);
//            });
//
//    private static Long4 vgatherdps_naive(float[] ary, int offset, Long4 vindex, Long4 mask) {
//        float[] res = new float[8];
//        for(int i = 0; i < 8; i++){
//           res[i] = ary[offset+i]; // TODO: how do we use vindex and mask here?
//        }
//        return Long4.make(pack(res[0],res[1]),pack(res[2],res[3]),pack(res[4],res[5]),pack(res[6],res[7]));
//    }
//
//    public static Long4 vgatherdps(long addr, int offset, Long4 vindex, Long4 mask) {
//        try {
//            Long4 res =(Long4) MHm256_vgatherdps.invokeExact(addr,vindex,mask);
//            //assert assertEquals(res, vgatherdps_naive(addr, offset, vindex, mask));
//            return res;
//        } catch (Throwable e){
//            throw new Error(e);
//        }
//    }

    /* ========================================================================================*/
    // VADDSS xmm1, xmm2, xmm3/m32
    public static final MethodHandle MHm32_vaddss = MachineCodeSnippet.make(
            "mm32_vaddss", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.LIG.F3.0F.WIG 58 /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_128, PP_F3);
                return vex_emit(vex, 0x58, modRM(out, in2));
            });

    static Long2 vaddss_naive(Long2 a, Long2 b) {
        float[] res = new float[4];
        for (int i = 0; i < 4; i++) {
            float a_i = getFloat(a, i);
            if (i == 0) {
                float b_i = getFloat(b, i);
                res[i] = a_i + b_i;
            } else {
                res[i] = a_i;
            }
        }
        return Long2.make(
                pack(res[0], res[1]),
                pack(res[2], res[3])
        );
    }

    public static Long2 vaddss(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm32_vaddss.invokeExact(a, b);
            assert assertEquals(res, vaddss_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VMULSS xmm1, xmm2, xmm3/m32
    public static final MethodHandle MHm32_vmulss = MachineCodeSnippet.make(
            "mm32_vmulss", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.LIG.F3.0F.WIG 59 /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_128, PP_F3);
                return vex_emit(vex, 0x59, modRM(out, in2));
            });

    static Long2 vmulss_naive(Long2 a, Long2 b) {
        float[] res = new float[4];
        for (int i = 0; i < 4; i++) {
            float a_i = getFloat(a, i);
            if (i == 0) {
                float b_i = getFloat(b, i);
                res[i] = a_i * b_i;
            } else {
                res[i] = a_i;
            }
        }
        return Long2.make(
                pack(res[0], res[1]),
                pack(res[2], res[3])
        );
    }

    public static Long2 vmulss(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm32_vmulss.invokeExact(a, b);
            assert assertEquals(res, vmulss_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VPADDD xmm1, xmm2, xmm3/m128
    public static final MethodHandle MHm128_vpaddd = MachineCodeSnippet.make(
            "m128_vpaddd", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE /*out*/, xmmRegistersSSE /*in1*/, xmmRegistersSSE /*in2*/},
            (Register[] regs) -> {
                // VEX.NDS.128.66.0F.WIG FE /r
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_128, PP_66);
                return vex_emit(vex, 0xFE, modRM(out, in2));
            });

    private static Long2 vpaddd_naive(Long2 a, Long2 b) {
        long l1 = pack(
                getInt(a, 0) + getInt(b, 0),
                getInt(a, 1) + getInt(b, 1));
        long l2 = pack(
                getInt(a, 2) + getInt(b, 2),
                getInt(a, 3) + getInt(b, 3));
        return Long2.make(l1, l2);
    }

    public static Long2 vpaddd(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm128_vpaddd.invokeExact(a, b);
            assert assertEquals(res, vpaddd_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VPADDD ymm1, ymm2, ymm3/m256
    public static final MethodHandle MHm256_vpaddd = MachineCodeSnippet.make(
            "m256_vpaddd", MT_L4_BINARY, requires(AVX2),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                // VEX.NDS.256.66.0F.WIG FE /r
                int[] vex = vex_prefix(R_LOW, X_LOW, B_LOW, M_0F, W_LOW, in1, L_256, PP_66);
                return vex_emit(vex, 0xFE, modRM(out, in2));
            });

    private static Long4 vpaddd_naive(Long4 left, Long4 right) {
        int[] res = new int[8];
        for (int i = 0; i < 8; i++) {
            res[i] = getInt(left, i) + getInt(right, i);
        }
        return Long4.make(pack(res[0], res[1]),
                pack(res[2], res[3]),
                pack(res[4], res[5]),
                pack(res[6], res[7]));
    }

    public static Long4 vpaddd(Long4 a, Long4 b) {
        try {
            Long4 res = (Long4) MHm256_vpaddd.invokeExact(a, b);
            assert assertEquals(res, vpaddd_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VBLENDPS ymm1, ymm2, ymm3/m256, imm8
    private static MethodHandle MHm256_vblendps_gen(int imm) {
        return MachineCodeSnippet.make(
                "mm256_vblendps", MT_L4_BINARY, requires(AVX),
                new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
                (Register[] regs) -> {
                    Register out = regs[0];
                    Register in1 = regs[1];
                    Register in2 = regs[2];
                    //VEX.NDS.256.0F.WIG 0C /r
                    int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_256, PP_NONE);
                    return vex_emit(vex, 0x0C, modRM(out, in2), imm);
                });
    }

    private static Long4 vblendps_naive(Long4 a, Long4 b, int imm) {
        float[] res = new float[8];
        for (int i = 0; i < 8; i++) {
            if (((1 << i) & imm) != 0) {
                res[i] = getFloat(b, i);
            } else {
                res[i] = getFloat(a, i);
            }
        }
        return Long4.make(pack(res[0], res[1]),
                pack(res[2], res[3]),
                pack(res[4], res[5]),
                pack(res[6], res[7]));
    }

    @Stable public static final MethodHandle[] MHm256_vblendps_handles = IntStream
            .range(0, 8)
            .mapToObj(PatchableVecUtils::MHm256_vblendps_gen)
            .toArray((int dontcare) -> new MethodHandle[8]);

    public static Long4 vblendps(Long4 a, Long4 b, int imm) {
        try {
            Long4 res = (Long4) MHm256_vblendps_handles[imm].invokeExact(a, b);
            assert assertEquals(res, vblendps_naive(a, b, imm));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VBLENDPS xmm1, xmm2, xmm3/m128, imm8
    private static MethodHandle MHm128_vblendps_gen(int imm) {
        return MachineCodeSnippet.make(
                "mm256_vblendps", MT_L2_BINARY, requires(AVX),
                new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
                (Register[] regs) -> {
                    Register out = regs[0];
                    Register in1 = regs[1];
                    Register in2 = regs[2];
                    //VEX.NDS.128.0F.WIG 0C /r
                    int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_128, PP_NONE);
                    return vex_emit(vex, 0x0C, modRM(out, in2), imm);
                });
    }

    private static Long2 vblendps_naive(Long2 a, Long2 b, int imm) {
        float[] res = new float[4];
        for (int i = 0; i < 4; i++) {
            if (((1 << i) & imm) != 0) {
                res[i] = getFloat(b, i);
            } else {
                res[i] = getFloat(a, i);
            }
        }
        return Long2.make(pack(res[0], res[1]),
                pack(res[2], res[3]));
    }

    @Stable public static final MethodHandle[] MHm128_vblendps_handles = IntStream
            .range(0, 4)
            .mapToObj(PatchableVecUtils::MHm128_vblendps_gen)
            .toArray((int dontcare) -> new MethodHandle[4]);

    public static Long2 vblendps(Long2 a, Long2 b, int imm) {
        try {
            Long2 res = (Long2) MHm128_vblendps_handles[imm].invokeExact(a, b);
            assert assertEquals(res, vblendps_naive(a, b, imm));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VMULPS ymm1, ymm2, ymm3/m256
    public static final MethodHandle MHm256_vmulps = MachineCodeSnippet.make(
            "mm256_vmulps", MT_L4_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.256.0F.WIG 59 /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_256, PP_NONE);
                return vex_emit(vex, 0x59, modRM(out, in2));
            });

    private static Long4 vmulps_naive(Long4 a, Long4 b) {
        float[] res = new float[8];
        for (int i = 0; i < 8; i++) {
            res[i] = getFloat(a, i) * getFloat(b, i);
        }
        return Long4.make(pack(res[0], res[1]),
                pack(res[2], res[3]),
                pack(res[4], res[5]),
                pack(res[6], res[7]));
    }

    @ForceInline public static Long4 vmulps(Long4 a, Long4 b) {
        try {
            Long4 res = (Long4) MHm256_vmulps.invokeExact(a, b);
            assert assertEquals(res, vmulps_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    // VMULPS ymm1, ymm2, m256
    public static final MethodHandle MHm256_vmulps_sunk = MachineCodeSnippet.make(
            "mm256_vmulps_sunk", MT_L4_L4_FLOATARY_INT, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, cpuRegisters, cpuRegisters},
            (Register[] regs) -> {
                Register out = regs[0];
                Register vector2 = regs[1];
                Register base = regs[2];
                Register index = regs[3];
                //VEX.256.0F.WIG 59 /r
                int[] vex = vex_prefix(rBit(out), xBit(index), bBit(base), M_0F, W_LOW, vector2, L_256, PP_NONE);
                return vex_emit(vex, 0x59, modRM_SIB_DISP8(out), sibByte(index, base, sibScale(Unsafe.ARRAY_FLOAT_INDEX_SCALE)), Unsafe.ARRAY_FLOAT_BASE_OFFSET);
            });

    public static Long4 vmulps(Long4 a, float[] ary, int i) {
        try {
            Long4 res = (Long4) MHm256_vmulps_sunk.invokeExact(a, ary, i);
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VADDPS ymm1, ymm2, ymm3
    public static final MethodHandle MHm256_vaddps = MachineCodeSnippet.make(
            "mm256_vaddps", MT_L4_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.256.0F.WIG 58 /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_256, PP_NONE);
                return vex_emit(vex, 0x58, modRM(out, in2));
            });

    private static Long4 vaddps_naive(Long4 a, Long4 b) {
        float[] res = new float[8];
        for (int i = 0; i < 8; i++) {
            res[i] = getFloat(a, i) + getFloat(b, i);
        }
        return long4FromFloatArray(res, 0);
    }


    @ForceInline public static Long4 vaddps(Long4 a, Long4 b) {
        try {
            Long4 res = (Long4) MHm256_vaddps.invokeExact(a, b);
            //assert assertEquals(res, vaddps_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    // VADDPS ymm1, ymm2, m256
    public static final MethodHandle MHm256_vaddps_sunk = MachineCodeSnippet.make(
            "mm256_vaddps_sunk", MT_L4_L4_FLOATARY_INT, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, cpuRegisters, cpuRegisters},
            (Register[] regs) -> {
                Register out = regs[0];
                Register vector2 = regs[1];
                Register base = regs[2];
                Register index = regs[3];
                //VEX.256.0F.WIG 58 /r
                int[] vex = vex_prefix(rBit(out), xBit(index), bBit(base), M_0F, W_LOW, vector2, L_256, PP_NONE);
                return vex_emit(vex, 0x58, modRM_SIB_DISP8(out), sibByte(index, base, Unsafe.ARRAY_FLOAT_INDEX_SCALE), Unsafe.ARRAY_FLOAT_BASE_OFFSET);
            });

    public static Long4 vaddps(Long4 a, float[] ary, int i) {
        try {
            Long4 res = (Long4) MHm256_vaddps_sunk.invokeExact(a, ary, i);
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    //VADDPD xmm1, xmm2, xmm3/128
    public static final MethodHandle MHm128_vaddpd = MachineCodeSnippet.make(
            "mm128_vaddpd", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.128.66.0F.WIG 58 /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_128, PP_66);
                return vex_emit(vex, 0x58, modRM(out, in2));
            });

    private static Long2 vaddpd_naive(Long2 a, Long2 b) {
        //TODO: Write this test.
        return null;
    }

    public static Long2 vaddpd(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm128_vaddpd.invokeExact(a, b);
            //assert assertEquals(res, VADDPD_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    //VADDPD ymm1, ymm2, ymm3/256
    public static final MethodHandle MHm256_vaddpd = MachineCodeSnippet.make(
            "mm256_vaddpd", MT_L4_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.256.66.0F.WIG 58 /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_256, PP_66);
                return vex_emit(vex, 0x58, modRM(out, in2));
            });

    private static Long4 vaddpd_naive(Long4 a, Long4 b) {
        //TODO: Write this test.
        return null;
    }

    public static Long4 vaddpd(Long4 a, Long4 b) {
        try {
            Long4 res = (Long4) MHm256_vaddpd.invokeExact(a, b);
            //assert assertEquals(res, VADDPD_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    //VMULPD xmm1, xmm2, xmm3/m128
    public static final MethodHandle MHm128_vmulpd = MachineCodeSnippet.make(
            "mm128_vmulpd", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.128.66.0F.WIG 59 /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_128, PP_66);
                return vex_emit(vex, 0x59, modRM(out, in2));
            });

    private static Long2 vmulpd_naive(Long2 a, Long2 b) {
        //TODO: Write this test.
        return null;
    }

    public static Long2 vmulpd(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm128_vmulpd.invokeExact(a, b);
            //assert assertEquals(res, VMULPD_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    //VMULPD ymm1, ymm2, ymm3/m256
    public static final MethodHandle MHm256_vmulpd = MachineCodeSnippet.make(
            "mm256_vmulpd", MT_L4_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.256.66.0F.WIG 59 /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_256, PP_66);
                return vex_emit(vex, 0x59, modRM(out, in2));
            });

    private static Long4 vmulpd_naive(Long4 a, Long4 b) {
        //TODO: Write this test.
        return null;
    }

    public static Long4 vmulpd(Long4 a, Long4 b) {
        try {
            Long4 res = (Long4) MHm256_vmulpd.invokeExact(a, b);
            //assert assertEquals(res, VMULPD_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VPBROADCASTD ymm1, m64
    public static final MethodHandle MHm256_vpbroadcastd = MachineCodeSnippet.make(
            "mm256_vpbroadcastd", MT_L4_LONG, requires(AVX),
            new Register[][]{xmmRegistersSSE, cpuRegisters},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in = regs[1];
                //VEX.NDS.256.66.0F38.W0 58 /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in), M_0F38, W_LOW, in, L_256, PP_66);
                return vex_emit(vex, 0x58, modRM_regInd(out, in));
            });

//    private static Long4 vbroadcastd_naive(Long4 a) {
//        float[] res = new float[8];
//        for (int i = 0; i < 8; i++) {
//            res[i] = getFloat(a, 0);
//        }
//        return Long4.make(pack(res[0], res[1]),
//                pack(res[2], res[3]),
//                pack(res[4], res[5]),
//                pack(res[6], res[7]));
//    }

    public static Long4 vpbroadcastd(Long4 a) {
        try {
            Long4 res = (Long4) MHm256_vpbroadcastd.invokeExact(a);
            //assert assertEquals(res, vbroadcastd_naive(a));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VPSHUFD xmm1, xmm2/m128, imm8
    private static MethodHandle MHm128_vpshufd(Integer imm) {
        return MachineCodeSnippet.make(
                "mm128_vpshufd", MT_L2_UNARY, requires(AVX),
                new Register[][]{xmmRegistersSSE, xmmRegistersSSE},
                (Register[] regs) -> {
                    Register out = regs[0];
                    Register in1 = regs[1];
                    //VEX.128.66.0F.WIG 70 /r ib
                    int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in1), M_0F, W_LOW, in1, L_128, PP_66);
                    return vex_emit(vex, 0x70, modRM(out, in1), imm);
                });
    }

    static public Long2 vpshufd_naive(Long2 a, int imm) {
        int[] res = new int[4];
        for (int i = 0; i < 4; i++) {
            int ordoff = i * 2;
            int swap_index = (imm >> ordoff) & 0x3; //(imm & (3 << ordoff)) >> ordoff);
            res[i] = getInt(a, swap_index);
        }
        Long2 ress = Long2.make(pack(res[0], res[1]), pack(res[2], res[3]));
        return ress;
    }

    @Stable public static final MethodHandle[] MHm128_vpshufd_handles = IntStream
            .range(0, 4)
            .mapToObj(PatchableVecUtils::MHm128_vpshufd)
            .toArray((int dontcare) -> new MethodHandle[4]);

    public static Long2 vpshufd(Long2 a, int imm) {
        try {
            Long2 res = (Long2) MHm128_vpshufd_handles[imm].invokeExact(a);
            assert assertEquals(res, vpshufd_naive(a, imm));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VPBLENDD xmm1, xmm2, xmm3/m128, imm8
    private static MethodHandle MHm128_vpblendd(Integer imm) {
        return MachineCodeSnippet.make(
                "mm128_vpblendd", MT_L2_BINARY, requires(AVX2),
                new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
                (Register[] regs) -> {
                    Register out = regs[0];
                    Register in1 = regs[1];
                    Register in2 = regs[2];
                    //VEX.NDS.128.66.0F3A.W0 02 /r ib
                    int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F3A, W_LOW, in1, L_128, PP_66);
                    return vex_emit(vex, 0x02, modRM(out, in2), imm);
                });
    }

    private static Long2 vpblendd_naive(Long2 a, Long2 b, int imm) {
        int[] res = new int[4];
        for (int i = 0; i < res.length; i++) {
            if (((1 << i) & imm) != 0) {
                res[i] = getInt(b, i);
            } else {
                res[i] = getInt(a, i);
            }
        }
        return Long2.make(pack(res[0], res[1]), pack(res[2], res[3]));
    }

    @Stable public static final MethodHandle[] MHm128_vpblendd_handles = IntStream
            .range(0, 4)
            .mapToObj(PatchableVecUtils::MHm128_vpblendd)
            .toArray((int dontcare) -> new MethodHandle[4]);

    public static Long2 vpblendd(Long2 a, Long2 b, int imm) {
        try {
            Long2 res = (Long2) MHm128_vpblendd_handles[imm].invokeExact(a, b);
            assert assertEquals(res, vpblendd_naive(a, b, imm));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VINSERTPS xmm1, xmm2, xmm3/m32, imm8
    private static MethodHandle MHm128_vinsertps(Integer imm) {
        return MachineCodeSnippet.make(
                "mm128_vinsertps", MT_L2_BINARY, requires(AVX),
                new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
                (Register[] regs) -> {
                    Register out = regs[0];
                    Register in1 = regs[1];
                    Register in2 = regs[2];
                    //VEX.NDS.128.66.0F3A.WIG 21 /r ib
                    int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F3A, W_LOW, in1, L_128, PP_66);
                    return vex_emit(vex, 0x21, modRM(out, in2), imm);
                });
    }

    private static Long2 vinsertps_naive(Long2 a, Long2 b, int imm) {
        int src = (imm & 0b11000000) >>> 6,
                dst = (imm & 0b00110000) >>> 4;

        float[] src_a = new float[4],
                src_b = new float[4];
        for (int i = 0; i < 4; i++) {
            src_a[i] = getFloat(a, i);
            src_b[i] = getFloat(b, i);
        }

        src_a[dst] = src_b[src];

        for (int i = 0; i < 4; i++) {
            if (((1 << i) & imm) != 0) {
                src_a[i] = 0f;
            }
        }
        return Long2.make(pack(src_a[0], src_a[1]), pack(src_a[2], src_a[3]));
    }

    @Stable public static final MethodHandle[] MHm128_vinsertps_handles = IntStream
            .range(0, 4)
            .mapToObj(PatchableVecUtils::MHm128_vinsertps)
            .toArray((int dontcare) -> new MethodHandle[4]);

    public static Long2 vinsertps(Long2 a, Long2 b, int imm) {
        try {
            Long2 res = (Long2) MHm128_vinsertps_handles[imm].invokeExact(a, b);
            assert assertEquals(res, vinsertps_naive(a, b, imm));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VPINSRB xmm1, xmm2, r/m8, imm8
    private static MethodHandle MHm128_vpinsrb(Integer imm) {
        return MachineCodeSnippet.make(
                "mm128_vpinsrb", MT_L2_L2_BYTE, requires(AVX),
                new Register[][]{xmmRegistersSSE, xmmRegistersSSE, cpuRegisters}, //{rdi, rsi, rdx, rcx, r8, r9}},
                (Register[] regs) -> {
                    Register out = regs[0];
                    Register in1 = regs[1];
                    Register in2 = regs[2];
                    //VEX.NDS.128.66.0F3A.W0 20 /r ib
                    int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F3A, W_LOW, in1, L_128, PP_66);
                    return vex_emit(vex, 0x20, modRM(out, in2), imm);
                });
    }

    private static Long2 vpinsrb_naive(Long2 a, byte val, int imm) {
        int[] vals = new int[4];
        for (int i = 0; i < 4; i++) {
            vals[i] = getInt(a, i);
        }
        vals[imm & 0b11] = val;
        return Long2.make(pack(vals[0], vals[1]), pack(vals[2], vals[3]));
    }

    @Stable public static final MethodHandle[] MHm128_vpinsrb_handles = IntStream
            .range(0, 4)
            .mapToObj(PatchableVecUtils::MHm128_vpinsrb)
            .toArray((int dontcare) -> new MethodHandle[4]);

    public static Long2 vpinsrb(Long2 a, byte val, int imm) {
        try {
            Long2 res = (Long2) MHm128_vpinsrb_handles[imm & 0b1111].invokeExact(a, val);
            //assertEquals(res, vpinsrb_naive(a, val, imm));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VPINSRW xmm1, xmm2, r32/m16, imm8
    private static MethodHandle MHm128_vpinsrw(Integer imm) {
        return MachineCodeSnippet.make(
                "mm128_vpinsrw", MT_L2_L2_SHORT, requires(AVX),
                new Register[][]{xmmRegistersSSE, xmmRegistersSSE, cpuRegisters}, //{rdi, rsi, rdx, rcx, r8, r9}},
                (Register[] regs) -> {
                    Register out = regs[0];
                    Register in1 = regs[1];
                    Register in2 = regs[2];
                    //VEX.NDS.128.66.0F.W0 C4 /r ib
                    int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_128, PP_66);
                    return vex_emit(vex, 0xC4, modRM(out, in2), imm);
                });
    }

    private static Long2 vpinsrw_naive(Long2 a, short val, int imm) {
        /*int[] vals = new int[4];
        for (int i = 0; i < 4; i++) {
            vals[i] = getInt(a, i);
        }
        vals[imm & 0b11] = val;
        return Long2.make(pack(vals[0], vals[1]), pack(vals[2], vals[3]));*/
        return null;
    }

    @Stable public static final MethodHandle[] MHm128_vpinsrw_handles = IntStream
            .range(0, 8)
            .mapToObj(PatchableVecUtils::MHm128_vpinsrw)
            .toArray((int dontcare) -> new MethodHandle[8]);

    public static Long2 vpinsrw(Long2 a, short val, int imm) {
        try {
            Long2 res = (Long2) MHm128_vpinsrw_handles[imm & 0b111].invokeExact(a, val);
            //assertEquals(res, vpinsrb_naive(a, val, imm));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VPINSRD xmm1, xmm2, r/m32, imm8
    private static MethodHandle MHm128_vpinsrd(Integer imm) {
        return MachineCodeSnippet.make(
                "mm128_vpinsrd", MT_L2_L2_INT, requires(AVX),
                new Register[][]{xmmRegistersSSE, xmmRegistersSSE, cpuRegisters}, //{rdi, rsi, rdx, rcx, r8, r9}},
                (Register[] regs) -> {
                    Register out = regs[0];
                    Register in1 = regs[1];
                    Register in2 = regs[2];
                    //VEX.NDS.128.66.0F3A.W0 22 /r ib
                    int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F3A, W_LOW, in1, L_128, PP_66);
                    return vex_emit(vex, 0x22, modRM(out, in2), imm);
                });
    }

    private static Long2 vpinsrd_naive(Long2 a, int val, int imm) {
        int[] vals = new int[4];
        for (int i = 0; i < 4; i++) {
            vals[i] = getInt(a, i);
        }
        vals[imm & 0b1] = val;
        return Long2.make(pack(vals[0], vals[1]), pack(vals[2], vals[3]));
    }

    @Stable public static final MethodHandle[] MHm128_vpinsrd_handles = IntStream
            .range(0, 4)
            .mapToObj(PatchableVecUtils::MHm128_vpinsrd)
            .toArray((int dontcare) -> new MethodHandle[4]);

    public static Long2 vpinsrd(Long2 a, int val, int imm) {
        try {
            Long2 res = (Long2) MHm128_vpinsrd_handles[imm & 0b11].invokeExact(a, val);
            assertEquals(res, vpinsrd_naive(a, val, imm));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VPINSRQ xmm1, xmm2, r/m64, imm8
    private static MethodHandle MHm128_vpinsrq(Integer imm) {
        return MachineCodeSnippet.make(
                "mm128_vpinsrq", MT_L2_LONG_BINARY, requires(AVX),
                new Register[][]{xmmRegistersSSE, xmmRegistersSSE, cpuRegisters}, //{rdi, rsi, rdx, rcx, r8, r9}},
                (Register[] regs) -> {
                    Register out = regs[0];
                    Register in1 = regs[1];
                    Register in2 = regs[2];
                    //VEX.NDS.128.66.0F3A.W1 22 /r ib
                    int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F3A, W_HIGH, in1, L_128, PP_66);
                    return vex_emit(vex, 0x22, modRM(out, in2), imm);
                });
    }

    private static Long2 vpinsrq_naive(Long2 a, long val, int imm) {
        long[] vals = new long[2];
        for (int i = 0; i < 2; i++) {
            vals[i] = getLong(a, i);
        }
        vals[imm & 0b11] = val;
        return Long2.make(vals[0], vals[1]);
    }

    @Stable public static final MethodHandle[] MHm128_vpinsrq_handles = IntStream
            .range(0, 2)
            .mapToObj(PatchableVecUtils::MHm128_vpinsrq)
            .toArray((int dontcare) -> new MethodHandle[2]);

    public static Long2 vpinsrq(Long2 a, long val, int imm) {
        try {
            Long2 res = (Long2) MHm128_vpinsrq_handles[imm & 0b1].invokeExact(a, val);
            assertEquals(res, vpinsrq_naive(a, val, imm));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // MOV r32,r32
//    private static MethodHandle MHm32_mov = MachineCodeSnippet.make(
//            "mm32_mov", INT_BINARY, true,
//            new Register[][]{cpuRegisters,cpuRegisters},
//            (Register[] regs) -> {
//                Register out = regs[0];
//                Register in = regs[1];
//                return new int[] {
//                        0x8B, //mov out, in
//                        modRM(out,in)
//                };
//            });
//
//    private static int mov_naive(int a) {
//        return a;
//    }
//
//    public static int mov(int a){
//        try {
//            int res = (int) MHm32_mov.invokeExact(a);
//            assertEquals(res,mov_naive(a));
//            return res;
//        } catch (Throwable e){
//            throw new Error(e);
//        }
//    }

    /* ========================================================================================*/
    // VINSERTF128 ymm1, ymm2, xmm3/m128, imm8
    private static MethodHandle MHm256_vinsertf128(int imm) {
        return MachineCodeSnippet.make(
                "mm256_vinsertf128" + imm, MT_L4_L2, requires(AVX),
                new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
                (Register[] regs) -> {
                    Register out = regs[0],
                            in1 = regs[1],
                            in2 = regs[2];
                    //VEX.NDS.256.66.0F3A.W0 18 /r ib
                    int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F3A, W_LOW, in1, L_256, PP_66);
                    return vex_emit(vex, 0x18, modRM(out, in2), imm);
                });
    }

    @Stable public static final MethodHandle[] MHm256_vinsertf128_handles = IntStream
            .range(0, 2)
            .mapToObj(PatchableVecUtils::MHm256_vinsertf128)
            .toArray((int dontcare) -> new MethodHandle[2]);

    private static Long4 vinsertf128_naive(Long4 dst, Long2 src, int imm) {
        switch (imm & 0x1) {
            case 0:
                return Long4.make(src.extract(0), src.extract(1), dst.extract(2), dst.extract(3));
            case 1:
                return Long4.make(dst.extract(0), dst.extract(1), src.extract(0), src.extract(1));
            default:
                return dst;
        }
    }

    public static Long4 vinsertf128(Long4 dst, Long2 src, int imm) {
        try {
            Long4 res = (Long4) MHm256_vinsertf128_handles[imm].invokeExact(dst, src);
            assertEquals(res, vinsertf128_naive(dst, src, imm));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VPEXTRB r/m8, xmm2, imm8
    private static MethodHandle MHm128_vpextrb(int imm) {
        return MachineCodeSnippet.make(
                "mm128_vpextrb", MT_BYTE_L2, requires(AVX),
                new Register[][]{cpuRegisters, xmmRegistersSSE},
                //VEX.128.66.0F3A.W0 14 /r ib
                (Register[] regs) -> {
                    Register out = regs[0],
                            in = regs[1];
                    int[] vex = vex_prefix_nonds(rBit(in), X_LOW, bBit(out), M_0F3A, W_LOW, 0b1111, L_128, PP_66);
                    return vex_emit(vex, 0x14, modRM(in, out), imm);
                });
    }

   @Stable public static final MethodHandle[] MHm128_vpextrb_handles = IntStream
            .range(0, 16)
            .mapToObj(PatchableVecUtils::MHm128_vpextrb)
            .toArray((int dontcare) -> new MethodHandle[16]);

    /* public static int vextractps_naive(int i, Long2 val) {
        return getInt(val, i & 0b11);
    }*/

    public static byte vpextrb(int i, Long2 val) {
        try {
            byte res = (byte) MHm128_vpextrb_handles[i & 0b1111].invokeExact(val);
            //assertEquals(Integer.toHexString(Float.floatToIntBits(res)),Integer.toHexString(Float.floatToIntBits(vextractps_naive(i,val))));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VPEXTRW r/m16, xmm2, imm8
    private static MethodHandle MHm128_vpextrw(int imm) {
        return MachineCodeSnippet.make(
                "mm128_vpextrw", MT_SHORT_L2, requires(AVX),
                new Register[][]{cpuRegisters, xmmRegistersSSE},
                //VEX.128.66.0F.W0 C5 /r ib
                (Register[] regs) -> {
                    Register out = regs[0],
                            in = regs[1];
                    int[] vex = vex_prefix_nonds(rBit(in), X_LOW, bBit(out), M_0F3A, W_LOW, 0b1111, L_128, PP_66);
                    return vex_emit(vex, 0x15, modRM(in, out), imm);
                });
    }

    @Stable public static final MethodHandle[] MHm128_vpextrw_handles = IntStream
            .range(0, 8)
            .mapToObj(PatchableVecUtils::MHm128_vpextrw)
            .toArray((int dontcare) -> new MethodHandle[8]);

    /* public static int vextractps_naive(int i, Long2 val) {
        return getInt(val, i & 0b11);
    }*/

    public static short vpextrw(int i, Long2 val) {
        try {
            short res = (short) MHm128_vpextrw_handles[i & 0b111].invokeExact(val);
            //assertEquals(Integer.toHexString(Float.floatToIntBits(res)),Integer.toHexString(Float.floatToIntBits(vextractps_naive(i,val))));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VEXTRACTPS r/m32, xmm1, imm8
    private static MethodHandle MHm128_vextractps(int imm) {
        return MachineCodeSnippet.make(
                "mm128_vextractps", MT_INT_L2, requires(AVX),
                new Register[][]{cpuRegisters, xmmRegistersSSE},
                //VEX.128.66.0F3A.WIG 17 /r ib
                (Register[] regs) -> {
                    Register out = regs[0],
                            in = regs[1];
                    int[] vex = vex_prefix_nonds(rBit(in), X_LOW, bBit(out), M_0F3A, W_LOW, 0b1111, L_128, PP_66);
                    return vex_emit(vex, 0x17, modRM(in, out), imm);
                });
    }

    @Stable public static final MethodHandle[] MHm128_vextractps_handles = IntStream
            .range(0, 4)
            .mapToObj(PatchableVecUtils::MHm128_vextractps)
            .toArray((int dontcare) -> new MethodHandle[4]);

    public static int vextractps_naive(int i, Long2 val) {
        return getInt(val, i & 0b11);
    }

    public static float vextractps(int i, Long2 val) {
        try {
            int res = (int) MHm128_vextractps_handles[i & 0b11].invokeExact(val);
            //assertEquals(Integer.toHexString(Float.floatToIntBits(res)),Integer.toHexString(Float.floatToIntBits(vextractps_naive(i,val))));
            return Float.intBitsToFloat(res);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VPEXTRD r/m32, xmm1, imm8
    private static MethodHandle MHm128_vpextrd(int imm) {
        return MachineCodeSnippet.make(
                "mm128_vpextrd", MT_INT_L2, requires(AVX),
                new Register[][]{cpuRegisters, xmmRegistersSSE},
                //VEX.128.66.0F3A.W0 16 /r ib
                (Register[] regs) -> {
                    Register out = regs[0],
                            in = regs[1];
                    int[] vex = vex_prefix_nonds(R_LOW, X_LOW, B_LOW, M_0F3A, W_LOW, 0b1111, L_128, PP_66);
                    return vex_emit(vex, 0x16, modRM(in, out), imm);
                });
    }

    @Stable public static final MethodHandle[] MHm128_vpextrd_handles = IntStream
            .range(0, 4)
            .mapToObj(PatchableVecUtils::MHm128_vpextrd)
            .toArray((int dontcare) -> new MethodHandle[4]);

    public static int vpextrd_naive(int i, Long2 val) {
        return getInt(val, i & 0b11);
    }

    public static int vpextrd(int i, Long2 val) {
        try {
            int res = (int) MHm128_vpextrd_handles[i & 0b11].invokeExact(val);
            //assertEquals(Integer.toHexString(Float.floatToIntBits(res)),Integer.toHexString(Float.floatToIntBits(vextractps_naive(i,val))));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VPEXTRQ r/m64, xmm1, imm8
    private static MethodHandle MHm128_vpextrq(int imm) {
        return MachineCodeSnippet.make(
                "mm128_vpextrq", MT_LONG_L2, requires(AVX),
                new Register[][]{cpuRegisters, xmmRegistersSSE},
                //VEX.128.66.0F3A.W1 16 /r ib
                (Register[] regs) -> {
                    Register out = regs[0],
                            in = regs[1];
                    int[] vex = vex_prefix_nonds(rBit(in), X_LOW, bBit(out), M_0F3A, W_HIGH, 0b1111, L_128, PP_66);
                    return vex_emit(vex, 0x16, modRM(in, out), imm);
                });
    }

    @Stable public static final MethodHandle[] MHm128_vpextrq_handles = IntStream
            .range(0, 2)
            .mapToObj(PatchableVecUtils::MHm128_vpextrq)
            .toArray((int dontcare) -> new MethodHandle[2]);

    public static long vpextrq_naive(int i, Long2 val) {
        return getLong(val, i & 0b11);
    }

    public static long vpextrq(int i, Long2 val) {
        try {
            long res = (long) MHm128_vpextrq_handles[i & 0b11].invokeExact(val);
            //long res = vpextrq_naive(i, val);
            //assertEquals(Integer.toHexString(Float.floatToIntBits(res)),Integer.toHexString(Float.floatToIntBits(vextractps_naive(i,val))));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VEXTRACTF128 xmm/m128, ymm2, imm8
    private static MethodHandle MHm256_vextractf128(int imm) {
        return MachineCodeSnippet.make(
                "mm256_vextractf128" + imm, MT_L4_L2_, requires(AVX),
                new Register[][]{xmmRegistersSSE, xmmRegistersSSE},
                //VEX.256.66.0F3A.W0 19 /r ib
                (Register[] regs) -> {
                    Register out = regs[0],
                            in = regs[1];
                    int[] vex = vex_prefix_nonds(rBit(in), X_LOW, bBit(out), M_0F3A, W_LOW, 0b1111, L_256, PP_66);
                    return vex_emit(vex, 0x19, modRM(in, out), imm);
                });
    }

    @Stable public static final MethodHandle[] MHm256_vextractf128_handles = IntStream
            .range(0, 2)
            .mapToObj(PatchableVecUtils::MHm256_vextractf128)
            .toArray((int dontcare) -> new MethodHandle[2]);

    private static Long2 vextractf128_naive(int i, Long4 val) {
        switch (i & 0b1) {
            case 1:
                return Long2.make(val.extract(2), val.extract(3));
            default:
                return Long2.make(val.extract(0), val.extract(1));
        }
    }

    public static Long2 vextractf128(int i, Long4 val) {
        try {
            Long2 res = (Long2) MHm256_vextractf128_handles[i & 0b11].invokeExact(val);
            assertEquals(res, vextractf128_naive(i, val));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VMOVUPS ymm2/m256, ymm1
    public static final MethodHandle MHm256_vmovups_store = MachineCodeSnippet.make(
            "mm256_vmovups_store", MT_VOID_OBJ_LONG_L4, requires(AVX),
            new Register[][]{cpuRegisters, cpuRegisters, cpuRegisters, xmmRegistersSSE},
            (Register[] regs) -> {
                Register base = regs[1],
                        index = regs[2],
                        vector = regs[3];
                //VEX.256.0F.WIG 11 /r
                int[] vex = vex_prefix_nonds(rBit(vector), xBit(index), bBit(base), M_0F, W_LOW, 0b1111, L_256, PP_NONE);
                return vex_emit(vex, 0x11, modRM_SIB_NODISP(vector), sibByte(index, base, 0b00));
            });

    private static void vmovups_store_naive(float[] ary, int offset, Long4 vec) {
        for (int i = 0; i < 8; i++) {
            ary[i + offset] = getFloat(vec, i);
        }
    }

    public static void vmovups(Object base, long offset, Long4 vec) {
        try {
            MHm256_vmovups_store.invokeExact(base, offset, vec);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VMOVUPS ymm2/m256, ymm1
    public static final MethodHandle MHm256_vmovups_load = MachineCodeSnippet.make(
            "mm256_vmovups_load", MT_L4_OBJ_LONG, requires(AVX),
            new Register[][]{xmmRegistersSSE, cpuRegisters, cpuRegisters},
            (Register[] regs) -> {
                Register vector = regs[0],
                        base = regs[1],
                        index = regs[2];
                //VEX.256.0F.WIG 10 /r
                int[] vex = vex_prefix_nonds(rBit(vector), xBit(index), bBit(base), M_0F, W_LOW, 0b1111, L_256, PP_NONE);
                return vex_emit(vex, 0x10, modRM_SIB_NODISP(vector), sibByte(index, base, 0b00));
            });

    private static Long4 vmovups_load_naive(float[] ary, int offset) {
        float[] res = new float[8];
        System.arraycopy(ary, offset, res, 0, 8);
        return Long4.make(pack(res[0], res[1]), pack(res[2], res[3]), pack(res[4], res[5]), pack(res[6], res[7]));
    }

    public static Long4 vmovups(Object base, long index) {
        try {
            return (Long4) MHm256_vmovups_load.invokeExact(base, index);
            //assertEquals(res,vmovups_load_naive(addr,0));
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    //Constructing vmovdqu stores with or without a one-byte displacement
    private static MethodHandle vmovdqu_store_make(VEXLength len, MethodType type, int disp, int scale) {
        if (scale < 0 || scale > 3) throw new UnsupportedOperationException("SIB scale can only be 0-3 inclusive.");

        String pref = len == L_128 ? "mm128" : "mm256";
        String suff = "_scale_" + scale + "_disp_" + disp;
        String name = pref + "_vmovdqu_store" + suff;

        return MachineCodeSnippet.make(
                name, type, requires(AVX),
                new Register[][]{cpuRegisters, cpuRegisters, cpuRegisters, xmmRegistersSSE},
                (Register[] regs) -> {
                    Register base = regs[1],
                            index = regs[2],
                            vector = regs[3];
                    int modRM;
                    if (disp == 0) {
                        modRM = modRM_SIB_NODISP(vector);
                        int[] vex = vex_prefix_nonds(rBit(vector), xBit(index), bBit(base), M_0F, W_LOW, 0b1111, len, PP_F3);
                        return vex_emit(vex, 0x7F, modRM, sibByte(index, base, scale));
                    } else {
                        modRM = modRM_SIB_DISP8(vector);
                        int[] vex = vex_prefix_nonds(rBit(vector), xBit(index), bBit(base), M_0F, W_LOW, 0b1111, len, PP_F3);
                        return vex_emit(vex, 0x7F, modRM, sibByte(index, base, scale), disp); //1-byte displacement
                    }
                });
    }

    private static MethodHandle vmovdqu_store_make(VEXLength len, int disp, int scale) {
        MethodType type = len == L_128 ? MT_VOID_OBJ_LONG_L2 : MT_VOID_OBJ_LONG_L4;
        return vmovdqu_store_make(len, type, disp, scale);
    }

    private static final MethodType MT_VOID_INTARY_INT_L2 = MethodType.methodType(void.class, int[].class, int.class, Long2.class);
    private static final MethodType MT_VOID_INTARY_INT_L4 = MethodType.methodType(void.class, int[].class, int.class, Long4.class);

    private static final MethodType MT_VOID_FLOATARY_INT_L2 = MethodType.methodType(void.class, float[].class, int.class, Long2.class);
    private static final MethodType MT_VOID_FLOATARY_INT_L4 = MethodType.methodType(void.class, float[].class, int.class, Long4.class);

    private static final MethodType MT_VOID_DOUBLEARY_INT_L2 = MethodType.methodType(void.class, double[].class, int.class, Long2.class);
    private static final MethodType MT_VOID_DOUBLEARY_INT_L4 = MethodType.methodType(void.class, double[].class, int.class, Long4.class);

    private static final MethodType MT_VOID_LONGARY_INT_L2 = MethodType.methodType(void.class, long[].class, int.class, Long2.class);
    private static final MethodType MT_VOID_LONGARY_INT_L4 = MethodType.methodType(void.class, long[].class, int.class, Long4.class);

    private static final MethodType MT_VOID_BYTEARRAY_INT_L2 = MethodType.methodType(void.class, byte[].class, int.class, Long2.class);
    private static final MethodType MT_VOID_BYTEARRAY_INT_L4 = MethodType.methodType(void.class, byte[].class, int.class, Long4.class);

    private static final MethodType MT_VOID_SHORTARRAY_INT_L2 = MethodType.methodType(void.class, short[].class, int.class, Long2.class);
    private static final MethodType MT_VOID_SHORTARRAY_INT_L4 = MethodType.methodType(void.class, short[].class, int.class, Long4.class);

    //Constructing a vmovdqu load with or without a 1-byte displacement
    private static MethodHandle vmovdqu_load_make(VEXLength len, MethodType type, int disp, int scale) {
        if (scale < 0 || scale > 3) throw new UnsupportedOperationException("SIB scale can only be 0-3 inclusive.");

        String pref = len == L_128 ? "mm128" : "mm256";
        String suff = "_scale_" + scale + "_disp_" + disp;
        String name = pref + "_vmovdqu_load" + suff;

        return MachineCodeSnippet.make(
                name, type, requires(AVX),
                new Register[][]{xmmRegistersSSE, cpuRegisters, cpuRegisters},
                (Register[] regs) -> {
                    Register vector = regs[0],
                            base = regs[1],
                            index = regs[2];
                    int modRM;
                    if (disp == 0) {
                        modRM = modRM_SIB_NODISP(vector);
                        int[] vex = vex_prefix_nonds(rBit(vector), xBit(index), bBit(base), M_0F, W_LOW, 0b1111, len, PP_F3);
                        return vex_emit(vex, 0x6F, modRM, sibByte(index, base, scale));
                    } else {
                        modRM = modRM_SIB_DISP8(vector);
                        int[] vex = vex_prefix_nonds(rBit(vector), xBit(index), bBit(base), M_0F, W_LOW, 0b1111, len, PP_F3);
                        return vex_emit(vex, 0x6F, modRM, sibByte(index, base, scale), disp);
                    }
                });
    }

    private static MethodHandle vmovdqu_load_make(VEXLength len, int disp, int scale) {
        MethodType type = len == L_128 ? MT_L2_OBJ_LONG : MT_L4_OBJ_LONG;
        return vmovdqu_load_make(len, type, disp, scale);
    }

    private static final MethodType MT_L2_INTARY_INT = MethodType.methodType(Long2.class, int[].class, int.class);
    private static final MethodType MT_L4_INTARY_INT = MethodType.methodType(Long4.class, int[].class, int.class);

    private static final MethodType MT_L2_FLOATARY_INT = MethodType.methodType(Long2.class, float[].class, int.class);
    private static final MethodType MT_L4_FLOATARY_INT = MethodType.methodType(Long4.class, float[].class, int.class);

    private static final MethodType MT_L2_DOUBLEARY_INT = MethodType.methodType(Long2.class, double[].class, int.class);
    private static final MethodType MT_L4_DOUBLEARY_INT = MethodType.methodType(Long4.class, double[].class, int.class);

    private static final MethodType MT_L2_LONGARY_INT = MethodType.methodType(Long2.class, long[].class, int.class);
    private static final MethodType MT_L4_LONGARY_INT = MethodType.methodType(Long4.class, long[].class, int.class);

    private static final MethodType MT_L2_BYTEARY_INT = MethodType.methodType(Long2.class, byte[].class, int.class);
    private static final MethodType MT_L4_BYTEARY_INT = MethodType.methodType(Long4.class, byte[].class, int.class);

    private static final MethodType MT_L2_SHORTARY_INT = MethodType.methodType(Long2.class, short[].class, int.class);
    private static final MethodType MT_L4_SHORTARY_INT = MethodType.methodType(Long4.class, short[].class, int.class);
    /* ========================================================================================*/
    // VMOVDQU xmm2/m128, xmm1
    public static final MethodHandle MHm128_vmovdqu_store = vmovdqu_store_make(L_128, 0, 0b00);
    public static final MethodHandle MHm256_vmovdqu_store = vmovdqu_store_make(L_256, 0, 0b00);

    public static final MethodHandle MHm128_vmovdqu_store_intarray = vmovdqu_store_make(L_128, MT_VOID_INTARY_INT_L2, Unsafe.ARRAY_INT_BASE_OFFSET, sibScale(Unsafe.ARRAY_INT_INDEX_SCALE));
    public static final MethodHandle MHm256_vmovdqu_store_intarray = vmovdqu_store_make(L_256, MT_VOID_INTARY_INT_L4, Unsafe.ARRAY_INT_BASE_OFFSET, sibScale(Unsafe.ARRAY_INT_INDEX_SCALE));

    public static final MethodHandle MHm128_vmovdqu_store_floatarray = vmovdqu_store_make(L_128, MT_VOID_FLOATARY_INT_L2, Unsafe.ARRAY_FLOAT_BASE_OFFSET, sibScale(Unsafe.ARRAY_FLOAT_INDEX_SCALE));
    public static final MethodHandle MHm256_vmovdqu_store_floatarray = vmovdqu_store_make(L_256, MT_VOID_FLOATARY_INT_L4, Unsafe.ARRAY_FLOAT_BASE_OFFSET, sibScale(Unsafe.ARRAY_FLOAT_INDEX_SCALE));

    public static final MethodHandle MHm128_vmovdqu_store_doublearray = vmovdqu_store_make(L_128, MT_VOID_DOUBLEARY_INT_L2, Unsafe.ARRAY_DOUBLE_BASE_OFFSET, sibScale(Unsafe.ARRAY_DOUBLE_INDEX_SCALE));
    public static final MethodHandle MHm256_vmovdqu_store_doublearray = vmovdqu_store_make(L_256, MT_VOID_DOUBLEARY_INT_L4, Unsafe.ARRAY_DOUBLE_BASE_OFFSET, sibScale(Unsafe.ARRAY_DOUBLE_INDEX_SCALE));

    public static final MethodHandle MHm128_vmovdqu_store_longarray = vmovdqu_store_make(L_128, MT_VOID_LONGARY_INT_L2, Unsafe.ARRAY_LONG_BASE_OFFSET, sibScale(Unsafe.ARRAY_LONG_INDEX_SCALE));
    public static final MethodHandle MHm256_vmovdqu_store_longarray = vmovdqu_store_make(L_256, MT_VOID_LONGARY_INT_L4, Unsafe.ARRAY_LONG_BASE_OFFSET, sibScale(Unsafe.ARRAY_LONG_INDEX_SCALE));

    public static final MethodHandle MHm128_vmovdqu_store_bytearray = vmovdqu_store_make(L_128, MT_VOID_BYTEARRAY_INT_L2, Unsafe.ARRAY_BYTE_BASE_OFFSET, sibScale(Unsafe.ARRAY_BYTE_INDEX_SCALE));
    public static final MethodHandle MHm256_vmovdqu_store_bytearray = vmovdqu_store_make(L_256, MT_VOID_BYTEARRAY_INT_L4, Unsafe.ARRAY_BYTE_BASE_OFFSET, sibScale(Unsafe.ARRAY_BYTE_INDEX_SCALE));

    public static final MethodHandle MHm128_vmovdqu_store_shortarray = vmovdqu_store_make(L_128, MT_VOID_SHORTARRAY_INT_L2, Unsafe.ARRAY_SHORT_BASE_OFFSET, sibScale(Unsafe.ARRAY_SHORT_INDEX_SCALE));
    public static final MethodHandle MHm256_vmovdqu_store_shortarray = vmovdqu_store_make(L_256, MT_VOID_SHORTARRAY_INT_L4, Unsafe.ARRAY_SHORT_BASE_OFFSET, sibScale(Unsafe.ARRAY_SHORT_INDEX_SCALE));

    public static void vmovdqu(Object base, long offset, Long2 vec) {
        try {
            MHm128_vmovdqu_store.invokeExact(base, offset, vec);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static void vmovdqu(Object base, long offset, Long4 vec) {
        try {
            MHm256_vmovdqu_store.invokeExact(base, offset, vec);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static void long2ToIntArray(int[] base, int index, Long2 vec) {
        try {
            MHm128_vmovdqu_store_intarray.invokeExact(base, index, vec);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static void long4ToIntArray(int[] base, int index, Long4 vec) {
        try {
            MHm256_vmovdqu_store_intarray.invokeExact(base, index, vec);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static void long2ToFloatArray(float[] base, int index, Long2 vec) {
        try {
            MHm128_vmovdqu_store_floatarray.invokeExact(base, index, vec);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static void long4ToFloatArray(float[] base, int index, Long4 vec) {
        try {
            MHm256_vmovdqu_store_floatarray.invokeExact(base, index, vec);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static void long2ToDoubleArray(double[] base, int index, Long2 vec) {
        try {
            MHm128_vmovdqu_store_doublearray.invokeExact(base, index, vec);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static void long4ToDoubleArray(double[] base, int index, Long4 vec) {
        try {
            MHm256_vmovdqu_store_doublearray.invokeExact(base, index, vec);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static void long2ToLongArray(long[] base, int index, Long2 vec) {
        try {
            MHm128_vmovdqu_store_longarray.invokeExact(base, index, vec);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static void long4ToLongArray(long[] base, int index, Long4 vec) {
        try {
            MHm256_vmovdqu_store_longarray.invokeExact(base, index, vec);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static void long2ToByteArray(byte[] base, int index, Long2 vec) {
        try {
            MHm128_vmovdqu_store_bytearray.invokeExact(base, index, vec);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static void long4ToByteArray(byte[] base, int index, Long4 vec) {
        try {
            MHm256_vmovdqu_store_bytearray.invokeExact(base, index, vec);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static void long2ToShortArray(short[] base, int index, Long2 vec) {
        try {
            MHm128_vmovdqu_store_shortarray.invokeExact(base, index, vec);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static void long4ToShortArray(short[] base, int index, Long4 vec) {
        try {
            MHm256_vmovdqu_store_shortarray.invokeExact(base, index, vec);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VMOVDQU xmm2/m128, xmm1
    public static final MethodHandle MHm128_vmovdqu_load = vmovdqu_load_make(L_128, 0, 0b00);
    public static final MethodHandle MHm256_vmovdqu_load = vmovdqu_load_make(L_256, 0, 0b00);

    public static final MethodHandle MHm128_vmovdqu_load_intarray = vmovdqu_load_make(L_128, MT_L2_INTARY_INT, Unsafe.ARRAY_INT_BASE_OFFSET, sibScale(Unsafe.ARRAY_INT_INDEX_SCALE));
    public static final MethodHandle MHm256_vmovdqu_load_intarray = vmovdqu_load_make(L_256, MT_L4_INTARY_INT, Unsafe.ARRAY_INT_BASE_OFFSET, sibScale(Unsafe.ARRAY_INT_INDEX_SCALE));

    public static final MethodHandle MHm128_vmovdqu_load_floatarray = vmovdqu_load_make(L_128, MT_L2_FLOATARY_INT, Unsafe.ARRAY_FLOAT_BASE_OFFSET, sibScale(Unsafe.ARRAY_FLOAT_INDEX_SCALE));
    public static final MethodHandle MHm256_vmovdqu_load_floatarray = vmovdqu_load_make(L_256, MT_L4_FLOATARY_INT, Unsafe.ARRAY_FLOAT_BASE_OFFSET, sibScale(Unsafe.ARRAY_FLOAT_INDEX_SCALE));

    public static final MethodHandle MHm128_vmovdqu_load_doublearray = vmovdqu_load_make(L_128, MT_L2_DOUBLEARY_INT, Unsafe.ARRAY_DOUBLE_BASE_OFFSET, sibScale(Unsafe.ARRAY_DOUBLE_INDEX_SCALE));
    public static final MethodHandle MHm256_vmovdqu_load_doublearray = vmovdqu_load_make(L_256, MT_L4_DOUBLEARY_INT, Unsafe.ARRAY_DOUBLE_BASE_OFFSET, sibScale(Unsafe.ARRAY_DOUBLE_INDEX_SCALE));

    public static final MethodHandle MHm128_vmovdqu_load_longarray = vmovdqu_load_make(L_128, MT_L2_LONGARY_INT, Unsafe.ARRAY_LONG_BASE_OFFSET, sibScale(Unsafe.ARRAY_LONG_INDEX_SCALE));
    public static final MethodHandle MHm256_vmovdqu_load_longarray = vmovdqu_load_make(L_256, MT_L4_LONGARY_INT, Unsafe.ARRAY_LONG_BASE_OFFSET, sibScale(Unsafe.ARRAY_LONG_INDEX_SCALE));

    public static final MethodHandle MHm128_vmovdqu_load_bytearray = vmovdqu_load_make(L_128, MT_L2_BYTEARY_INT, Unsafe.ARRAY_BYTE_BASE_OFFSET, sibScale(Unsafe.ARRAY_BYTE_INDEX_SCALE));
    public static final MethodHandle MHm256_vmovdqu_load_bytearray = vmovdqu_load_make(L_256, MT_L4_BYTEARY_INT, Unsafe.ARRAY_BYTE_BASE_OFFSET, sibScale(Unsafe.ARRAY_BYTE_INDEX_SCALE));

    public static final MethodHandle MHm128_vmovdqu_load_shortarray = vmovdqu_load_make(L_128, MT_L2_SHORTARY_INT, Unsafe.ARRAY_SHORT_BASE_OFFSET, sibScale(Unsafe.ARRAY_SHORT_INDEX_SCALE));
    public static final MethodHandle MHm256_vmovdqu_load_shortarray = vmovdqu_load_make(L_256, MT_L4_SHORTARY_INT, Unsafe.ARRAY_SHORT_BASE_OFFSET, sibScale(Unsafe.ARRAY_SHORT_INDEX_SCALE));

    public static Long2 vmovdqu(Object base, long index) {
        try {
            return (Long2) MHm128_vmovdqu_load.invokeExact(base, index);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static Long4 vmovdqu_256(Object base, long index) {
        try {
            return (Long4) MHm256_vmovdqu_load.invokeExact(base, index);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static Long4 long4FromIntArray(int[] base, int index) {
        try {
            return (Long4) MHm256_vmovdqu_load_intarray.invokeExact(base, index);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static Long2 long2FromIntArray(int[] base, int index) {
        try {
            return (Long2) MHm128_vmovdqu_load_intarray.invokeExact(base, index);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static Long4 long4FromFloatArray(float[] base, int index) {
        try {
            return (Long4) MHm256_vmovdqu_load_floatarray.invokeExact(base, index);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static Long2 long2FromFloatArray(float[] base, int index) {
        try {
            return (Long2) MHm128_vmovdqu_load_floatarray.invokeExact(base, index);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static Long4 long4FromDoubleArray(double[] base, int index) {
        try {
            return (Long4) MHm256_vmovdqu_load_doublearray.invokeExact(base, index);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static Long2 long2FromDoubleArray(double[] base, int index) {
        try {
            return (Long2) MHm128_vmovdqu_load_doublearray.invokeExact(base, index);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static Long4 long4FromLongArray(long[] base, int index) {
        try {
            return (Long4) MHm256_vmovdqu_load_longarray.invokeExact(base, index);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static Long2 long2FromLongArray(long[] base, int index) {
        try {
            return (Long2) MHm128_vmovdqu_load_longarray.invokeExact(base, index);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static Long4 long4FromByteArray(byte[] base, int index) {
        try {
            return (Long4) MHm256_vmovdqu_load_bytearray.invokeExact(base, index);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static Long2 long2FromByteArray(byte[] base, int index) {
        try {
            return (Long2) MHm128_vmovdqu_load_bytearray.invokeExact(base, index);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static Long4 long4FromShortArray(short[] base, int index) {
        try {
            return (Long4) MHm256_vmovdqu_load_shortarray.invokeExact(base, index);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static Long2 long2FromShortArray(short[] base, int index) {
        try {
            return (Long2) MHm128_vmovdqu_load_shortarray.invokeExact(base, index);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VGATHERDPS ymm1, vm32y, ymm2
    private static final MethodType MT_L4_FLOATARY_L4_L4 = MethodType.methodType(Long4.class, float[].class, Long4.class, Long4.class);
    public static final MethodHandle MHm256_vgatherdps = MachineCodeSnippet.make(
            "mm256_vgatherdps", MT_L4_FLOATARY_L4_L4, requires(AVX2),
            new Register[][]{xmmRegistersSSE, cpuRegisters, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0],
                        base = regs[1],
                        indexes = regs[2],
                        masks = regs[3];
                //VEX.DDS.128.66.0F38.W0 92 /r
                int[] vex = vex_prefix(rBit(out), xBit(indexes), bBit(base), M_0F38, W_LOW, masks, L_256, PP_66);
                return vex_emit(vex, 0x92, modRM_SIB_DISP8(out), vsibByte(indexes, base, 0b10), Unsafe.ARRAY_FLOAT_BASE_OFFSET);
            });

    private static Long4 vgatherdps_naive(float[] base, Long4 indexes, Long4 masks) {
        //TODO: Write this test.
        return null;
    }

    public static Long4 vgatherdps(float[] base, Long4 indexes, Long4 masks) {
        try {
            Long4 res = (Long4) MHm256_vgatherdps.invokeExact(base, indexes, masks);
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }


    /* ========================================================================================*/
    //VADDPS xmm1, xmm2, xmm3/128
    public static final MethodHandle MHm128_vaddps = MachineCodeSnippet.make(
            "mm128_vaddps", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.128.0F.WIG 58 /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_128, PP_NONE);
                return vex_emit(vex, 0x58, modRM(out, in2));
            });

    private static Long2 vaddps_naive(Long2 a, Long2 b) {
        //TODO: Write this test.
        return null;
    }

    public static Long2 vaddps(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm128_vaddps.invokeExact(a, b);
            //assert assertEquals(res, VADDPS_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    //VSUBPS xmm1, xmm2, xmm3/m128
    public static final MethodHandle MHm128_vsubps = MachineCodeSnippet.make(
            "mm128_vsubps", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.128.0F.WIG 5C /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_128, PP_NONE);
                return vex_emit(vex, 0x5C, modRM(out, in2));
            });

    private static Long2 vsubps_naive(Long2 a, Long2 b) {
        //TODO: Write this test.
        return null;
    }

    public static Long2 vsubps(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm128_vsubps.invokeExact(a, b);
            //assert assertEquals(res, VSUBPS_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    //VSUBPS ymm1, ymm2, ymm3/m256
    public static final MethodHandle MHm256_vsubps = MachineCodeSnippet.make(
            "mm256_vsubps", MT_L4_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.256.0F.WIG 5C /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_256, PP_NONE);
                return vex_emit(vex, 0x5C, modRM(out, in2));
            });

    private static Long4 vsubps_naive(Long4 a, Long4 b) {
        //TODO: Write this test.
        return null;
    }

    public static Long4 vsubps(Long4 a, Long4 b) {
        try {
            Long4 res = (Long4) MHm256_vsubps.invokeExact(a, b);
            //assert assertEquals(res, VSUBPS_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    //VSUBPD xmm1, xmm2, xmm3/m128
    public static final MethodHandle MHm128_vsubpd = MachineCodeSnippet.make(
            "mm128_vsubpd", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.128.66.0F.WIG 5C /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_128, PP_66);
                return vex_emit(vex, 0x5C, modRM(out, in2));
            });

    private static Long2 vsubpd_naive(Long2 a, Long2 b) {
        //TODO: Write this test.
        return null;
    }

    public static Long2 vsubpd(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm128_vsubpd.invokeExact(a, b);
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }
    /* ========================================================================================*/
    //VSUBPD ymm1, ymm2, ymm3/m256
    public static final MethodHandle MHm256_vsubpd = MachineCodeSnippet.make(
            "mm256_vsubpd", MT_L4_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.256.66.0F.WIG 5C /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_256, PP_66);
                return vex_emit(vex, 0x5C, modRM(out, in2));
            });

    private static Long4 vsubpd_naive(Long4 a, Long4 b) {
        //TODO: Write this test.
        return null;
    }

    public static Long4 vsubpd(Long4 a, Long4 b) {
        try {
            Long4 res = (Long4) MHm256_vsubpd.invokeExact(a, b);
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    //VMULPS xmm1, xmm2, xmm3/m128
    public static final MethodHandle MHm128_vmulps = MachineCodeSnippet.make(
            "mm128_vmulps", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.128.0F.WIG 59 /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_128, PP_NONE);
                return vex_emit(vex, 0x59, modRM(out, in2));
            });

    private static Long2 vmulps_naive(Long2 a, Long2 b) {
        //TODO: Write this test.
        return null;
    }

    public static Long2 vmulps(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm128_vmulps.invokeExact(a, b);
            //assert assertEquals(res, VMULPS_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }


    /* ========================================================================================*/
    //VDIVPS xmm1, xmm2, xmm3/m128
    public static final MethodHandle MHm128_vdivps = MachineCodeSnippet.make(
            "mm128_vdivps", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.128.0F.WIG 5E /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_128, PP_NONE);
                return vex_emit(vex, 0x5E, modRM(out, in2));
            });

    private static Long2 vdivps_naive(Long2 a, Long2 b) {
        //TODO: Write this test.
        return null;
    }

    public static Long2 vdivps(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm128_vdivps.invokeExact(a, b);
            //assert assertEquals(res, VDIVPS_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    //VDIVPS xmm1, xmm2, xmm3/m128
    public static final MethodHandle MHm256_vdivps = MachineCodeSnippet.make(
            "mm256_vdivps", MT_L4_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.256.0F.WIG 5E /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_256, PP_NONE);
                return vex_emit(vex, 0x5E, modRM(out, in2));
            });

    private static Long4 vdivps_naive(Long4 a, Long4 b) {
        //TODO: Write this test.
        return null;
    }

    public static Long4 vdivps(Long4 a, Long4 b) {
        try {
            Long4 res = (Long4) MHm256_vdivps.invokeExact(a, b);
            //assert assertEquals(res, VDIVPS_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    //VANDPS xmm1, xmm2, xmm3/m128
    public static final MethodHandle MHm128_vandps = MachineCodeSnippet.make(
            "mm128_vandps", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.128.0F.WIG 54 /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_128, PP_NONE);
                return vex_emit(vex, 0x54, modRM(out, in2));
            });

    private static Long2 vandps_naive(Long2 a, Long2 b) {
        //TODO: Write this test.
        return null;
    }

    public static Long2 vandps(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm128_vandps.invokeExact(a, b);
            //assert assertEquals(res, VANDPS_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }
    /* ========================================================================================*/
    //VANDPS ymm1, ymm2, ymm3/m256
    public static final MethodHandle MHm256_vandps = MachineCodeSnippet.make(
            "mm256_vandps", MT_L4_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.256.0F.WIG 54 /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_256, PP_NONE);
                return vex_emit(vex, 0x54, modRM(out, in2));
            });

    private static Long4 vandps_naive(Long4 a, Long4 b) {
        //TODO: Write this test.
        return null;
    }

    public static Long4 vandps(Long4 a, Long4 b) {
        try {
            Long4 res = (Long4) MHm256_vandps.invokeExact(a, b);
            //assert assertEquals(res, VANDPS_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }
    /* ========================================================================================*/
    //VORPS xmm1, xmm2, xmm3/m128
    public static final MethodHandle MHm128_vorps = MachineCodeSnippet.make(
            "mm128_vorps", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.128.0F.WIG 56 /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_128, PP_NONE);
                return vex_emit(vex, 0x56, modRM(out, in2));
            });

    private static Long2 vorps_naive(Long2 a, Long2 b) {
        //TODO: Write this test.
        return null;
    }

    public static Long2 vorps(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm128_vorps.invokeExact(a, b);
            //assert assertEquals(res, VORPS_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    //VPADDB xmm1, xmm2, xmm3/128
    public static final MethodHandle MHm128_vpaddb = MachineCodeSnippet.make(
            "mm128_vpaddb", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.128.66.0F.WIG FC /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_128, PP_66);
                return vex_emit(vex, 0xFC, modRM(out, in2));
            });

    private static Long2 vpaddb_naive(Long2 a, Long2 b) {
        //TODO: Write this test.
        return null;
    }

    public static Long2 vpaddb(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm128_vpaddb.invokeExact(a, b);
            //assert assertEquals(res, VADDPS_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    //VPADDB ymm1, ymm2, ymm3/256
    public static final MethodHandle MHm256_vpaddb = MachineCodeSnippet.make(
            "mm256_vpaddb", MT_L4_BINARY, requires(AVX2),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.256.66.0F.WIG FC /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_256, PP_66);
                return vex_emit(vex, 0xFC, modRM(out, in2));
            });

    private static Long4 vpaddb_naive(Long4 a, Long4 b) {
        //TODO: Write this test.
        return null;
    }

    public static Long4 vpaddb(Long4 a, Long4 b) {
        try {
            Long4 res = (Long4) MHm256_vpaddb.invokeExact(a, b);
            //assert assertEquals(res, VADDPS_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    //VPSUBB xmm1, xmm2, xmm3/m128
    public static final MethodHandle MHm128_vpsubb = MachineCodeSnippet.make(
            "mm128_vpsubb", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.256.66.0F.WIG FC /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_128, PP_66);
                return vex_emit(vex, 0xF8, modRM(out, in2));
            });

    private static Long2 vpsubb_naive(Long2 a, Long2 b) {
        //TODO: Write this test.
        return null;
    }

    public static Long2 vpsubb(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm128_vpsubb.invokeExact(a, b);
            //assert assertEquals(res, VSUBPS_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    //VPSUBB ymm1, ymm2, ymm3/m256
    public static final MethodHandle MHm256_vpsubb = MachineCodeSnippet.make(
            "mm256_vpsubb", MT_L4_BINARY, requires(AVX2),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.256.66.0F.WIG F8 /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_256, PP_66);
                return vex_emit(vex, 0xF8, modRM(out, in2));
            });

    private static Long4 vpsubb_naive(Long4 a, Long4 b) {
        //TODO: Write this test.
        return null;
    }

    public static Long4 vpsubb(Long4 a, Long4 b) {
        try {
            Long4 res = (Long4) MHm256_vpsubb.invokeExact(a, b);
            //assert assertEquals(res, VSUBPS_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    //VPCMPEQB xmm1, xmm2, xmm3/m128
    public static final MethodHandle MHm128_vpcmpeqb = MachineCodeSnippet.make(
            "mm128_vpcmpeqb", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0],
                        in1 = regs[1],
                        in2 = regs[2];
                // VEX.NDS.128.66.0F.WIG 74 /r
                int[] vex = vex_prefix(rBit(out),X_LOW,bBit(in2),M_0F,W_LOW,in1,L_128,PP_66);
                return vex_emit(vex, 0x74, modRM(out,in2));
            });

    private static Long2 vpcmpeqb_naive(Long2 a, Long2 b) {
        //TODO: Write this test
        return null;
    }

    public static Long2 vpcmpeqb(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm128_vpcmpeqb.invokeExact(a, b);
            //assert assertEquals(res,vpcmpeqps_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    //VPCMPEQB ymm1, ymm2, ymm3/m256
    public static final MethodHandle MHm256_vpcmpeqb = MachineCodeSnippet.make(
            "mm256_vpcmpeqb", MT_L4_BINARY, requires(AVX2),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0],
                        in1 = regs[1],
                        in2 = regs[2];
                // VEX.NDS.256.66.0F.WIG 74 /r
                int[] vex = vex_prefix(rBit(out),X_LOW,bBit(in2),M_0F,W_LOW,in1,L_256,PP_66);
                return vex_emit(vex, 0x74, modRM(out,in2));
            });

    private static Long4 vpcmpeqb_naive(Long4 a, Long4 b) {
        //TODO: Write this test
        return null;
    }

    public static Long4 vpcmpeqb(Long4 a, Long4 b) {
        try {
            Long4 res = (Long4) MHm256_vpcmpeqb.invokeExact(a, b);
       System.out.println("Results is " + res);
            //assert assertEquals(res,vpcmpeqps_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    //VPCMPGTB xmm1, xmm2, xmm3/m128
    public static final MethodHandle MHm128_vpcmpgtb = MachineCodeSnippet.make(
            "mm128_vpcmpgtb",MT_L2_BINARY,requires(AVX),
            new Register[][]{xmmRegistersSSE,xmmRegistersSSE,xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0],
                        in1 = regs[1],
                        in2 = regs[2];
                //VEX.NDS.128.66.0F.WIG 64 /r
                int[] vex = vex_prefix(rBit(out),X_LOW,bBit(in2),M_0F,W_LOW,in1,L_128,PP_66);
                return vex_emit(vex, 0x64, modRM(out,in2));
            });

    private static Long2 vpcmpgtb_naive(Long2 a, Long2 b){
        //TODO: Write this test
        return null;
    }

    public static Long2 vpcmpgtb(Long2 a, Long2 b){
        try {
            Long2 res = (Long2) MHm128_vpcmpgtb.invokeExact(a,b);
            //assert assertEquals(res,vcmpgtps_naive(a, b));
            return res;
        } catch (Throwable e){
            throw new Error(e);
        }
    }

    //VPCMPGTB ymm1, ymm2, ymm3/m256
    public static final MethodHandle MHm256_vpcmpgtb = MachineCodeSnippet.make(
            "mm256_vpcmpgtb",MT_L4_BINARY,requires(AVX),
            new Register[][]{xmmRegistersSSE,xmmRegistersSSE,xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0],
                        in1 = regs[1],
                        in2 = regs[2];
                //VEX.NDS.256.66.0F.WIG 64 /r
                int[] vex = vex_prefix(rBit(out),X_LOW,bBit(in2),M_0F,W_LOW,in1,L_256,PP_66);
                return vex_emit(vex, 0x64, modRM(out,in2));
            });

    private static Long4 vpcmpgtb_naive(Long4 a, Long4 b){
        //TODO: Write this test
        return null;
    }

    public static Long4 vpcmpgtb(Long4 a, Long4 b){
        try {
            Long4 res = (Long4) MHm256_vpcmpgtb.invokeExact(a,b);
            //assert assertEquals(res,vcmpgtps_naive(a, b));
            return res;
        } catch (Throwable e){
            throw new Error(e);
        }
    }

    //VPADDW xmm1, xmm2, xmm3/128
    public static final MethodHandle MHm128_vpaddw = MachineCodeSnippet.make(
            "mm128_vpaddw", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.128.66.0F.WIG FD /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_128, PP_66);
                return vex_emit(vex, 0xFD, modRM(out, in2));
            });

    private static Long2 vpaddw_naive(Long2 a, Long2 b) {
        //TODO: Write this test.
        return null;
    }

    public static Long2 vpaddw(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm128_vpaddw.invokeExact(a, b);
            //assert assertEquals(res, VADDPS_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    //VPADDW ymm1, ymm2, ymm3/256
    public static final MethodHandle MHm256_vpaddw = MachineCodeSnippet.make(
            "mm256_vpaddw", MT_L4_BINARY, requires(AVX2),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.256.66.0F.WIG FD /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_256, PP_66);
                return vex_emit(vex, 0xFD, modRM(out, in2));
            });

    private static Long4 vpaddw_naive(Long4 a, Long4 b) {
        //TODO: Write this test.
        return null;
    }

    public static Long4 vpaddw(Long4 a, Long4 b) {
        try {
            Long4 res = (Long4) MHm256_vpaddw.invokeExact(a, b);
            //assert assertEquals(res, VADDPS_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    //VPSUBW xmm1, xmm2, xmm3/m128
    public static final MethodHandle MHm128_vpsubw = MachineCodeSnippet.make(
            "mm128_vpsubw", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.128.66.0F.WIG F9 /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_128, PP_66);
                return vex_emit(vex, 0xF9, modRM(out, in2));
            });

    private static Long2 vpsubw_naive(Long2 a, Long2 b) {
        //TODO: Write this test.
        return null;
    }

    public static Long2 vpsubw(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm128_vpsubw.invokeExact(a, b);
            //assert assertEquals(res, VSUBPS_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    //VPSUBW ymm1, ymm2, ymm3/m256
    public static final MethodHandle MHm256_vpsubw = MachineCodeSnippet.make(
            "mm256_vpsubw", MT_L4_BINARY, requires(AVX2),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.256.66.0F.WIG F9 /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_256, PP_66);
                return vex_emit(vex, 0xF9, modRM(out, in2));
            });

    private static Long4 vpsubw_naive(Long4 a, Long4 b) {
        //TODO: Write this test.
        return null;
    }

    public static Long4 vpsubw(Long4 a, Long4 b) {
        try {
            Long4 res = (Long4) MHm256_vpsubw.invokeExact(a, b);
            //assert assertEquals(res, VSUBPS_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    //VPCMPEQW xmm1, xmm2, xmm3/m128
    public static final MethodHandle MHm128_vpcmpeqw = MachineCodeSnippet.make(
            "mm128_vpcmpeqw", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0],
                        in1 = regs[1],
                        in2 = regs[2];
                // VEX.NDS.128.66.0F.WIG 75 /r
                int[] vex = vex_prefix(rBit(out),X_LOW,bBit(in2),M_0F,W_LOW,in1,L_128,PP_66);
                return vex_emit(vex, 0x75, modRM(out,in2));
            });

    private static Long2 vpcmpeqw_naive(Long2 a, Long2 b) {
        //TODO: Write this test
        return null;
    }

    public static Long2 vpcmpeqw(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm128_vpcmpeqw.invokeExact(a, b);
       System.out.println("Result is" + res);
            //assert assertEquals(res,vpcmpeqps_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    //VPCMPEQW ymm1, ymm2, ymm3/m256
    public static final MethodHandle MHm256_vpcmpeqw = MachineCodeSnippet.make(
            "mm256_vpcmpeqw", MT_L4_BINARY, requires(AVX2),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0],
                        in1 = regs[1],
                        in2 = regs[2];
                // VEX.NDS.256.66.0F.WIG 75 /r
                int[] vex = vex_prefix(rBit(out),X_LOW,bBit(in2),M_0F,W_LOW,in1,L_256,PP_66);
                return vex_emit(vex, 0x75, modRM(out,in2));
            });

    private static Long4 vpcmpeqw_naive(Long4 a, Long4 b) {
        //TODO: Write this test
        return null;
    }

    public static Long4 vpcmpeqw(Long4 a, Long4 b) {
        try {
            Long4 res = (Long4) MHm256_vpcmpeqw.invokeExact(a, b);
            System.out.println("Result is-------" + res);
            //assert assertEquals(res,vpcmpeqps_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    //VPCMPGTW xmm1, xmm2, xmm3/m128
    public static final MethodHandle MHm128_vpcmpgtw = MachineCodeSnippet.make(
            "mm128_vpcmpgtw",MT_L2_BINARY,requires(AVX),
            new Register[][]{xmmRegistersSSE,xmmRegistersSSE,xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0],
                        in1 = regs[1],
                        in2 = regs[2];
                //VEX.NDS.128.66.0F.WIG 65 /r
                int[] vex = vex_prefix(rBit(out),X_LOW,bBit(in2),M_0F,W_LOW,in1,L_128,PP_66);
                return vex_emit(vex, 0x65, modRM(out,in2));
            });

    private static Long2 vpcmpgtw_naive(Long2 a, Long2 b){
        //TODO: Write this test
        return null;
    }

    public static Long2 vpcmpgtw(Long2 a, Long2 b){
        try {
            Long2 res = (Long2) MHm128_vpcmpgtw.invokeExact(a,b);
            //assert assertEquals(res,vcmpgtps_naive(a, b));
            return res;
        } catch (Throwable e){
            throw new Error(e);
        }
    }

    //VPCMPGTW ymm1, ymm2, ymm3/m256
    public static final MethodHandle MHm256_vpcmpgtw = MachineCodeSnippet.make(
            "mm256_vpcmpgtw",MT_L4_BINARY,requires(AVX2),
            new Register[][]{xmmRegistersSSE,xmmRegistersSSE,xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0],
                        in1 = regs[1],
                        in2 = regs[2];
                //VEX.NDS.256.66.0F.WIG 65 /r
                int[] vex = vex_prefix(rBit(out),X_LOW,bBit(in2),M_0F,W_LOW,in1,L_256,PP_66);
                return vex_emit(vex, 0x65, modRM(out,in2));
            });

    private static Long4 vpcmpgtw_naive(Long4 a, Long4 b){
        //TODO: Write this test
        return null;
    }

    public static Long4 vpcmpgtw(Long4 a, Long4 b){
        try {
            Long4 res = (Long4) MHm256_vpcmpgtw.invokeExact(a,b);
            //assert assertEquals(res,vcmpgtps_naive(a, b));
            return res;
        } catch (Throwable e){
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    //VCMPPS xmm1, xmm2, xmm3/m128, 0x0  -- VCMPEQPS
    public static final MethodHandle MHm128_vcmpeqps = MachineCodeSnippet.make(
            "mm128_vcmpeqps", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0],
                        in1 = regs[1],
                        in2 = regs[2];
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_128, PP_NONE);
                return vex_emit(vex, 0xC2, modRM(out, in2), 0x0); //0x0 is the imm that encodes compare equal
            });

    private static Long2 vcmpeqps_naive(Long2 a, Long2 b) {
        //TODO: Write this test
        return null;
    }

    public static Long2 vcmpeqps(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm128_vcmpeqps.invokeExact(a, b);
            //assert assertEquals(res,vcmpeqps_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    //VCMPPS ymm1, ymm2, ymm3/m256, 0x0  -- VCMPEQPS
    public static final MethodHandle MHm256_vcmpeqps = MachineCodeSnippet.make(
            "mm256_vcmpeqps", MT_L4_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0],
                        in1 = regs[1],
                        in2 = regs[2];
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_256, PP_NONE);
                return vex_emit(vex, 0xC2, modRM(out, in2), 0x0); //0x0 is the imm that encodes compare equal
            });

    private static Long4 vcmpeqps_naive(Long4 a, Long4 b) {
        //TODO: Write this test
        return null;
    }

    public static Long4 vcmpeqps(Long4 a, Long4 b) {
        try {
            Long4 res = (Long4) MHm256_vcmpeqps.invokeExact(a, b);
            //assert assertEquals(res,vcmpeqps_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    //VCMPPS xmm1, xmm2, xmm3/m128, 0x1  -- VCMPLTPS
    public static final MethodHandle MHm128_vcmpltps = MachineCodeSnippet.make(
            "mm128_vcmpltps",MT_L2_BINARY,requires(AVX),
            new Register[][]{xmmRegistersSSE,xmmRegistersSSE,xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0],
                        in1 = regs[1],
                        in2 = regs[2];
                int[] vex = vex_prefix(rBit(out),X_LOW,bBit(in2),M_0F,W_LOW,in1,L_128,PP_NONE);
                return vex_emit(vex, 0xC2, modRM(out,in2), 0x1); //0x1 is the imm that encodes compare less than
            });

    private static Long2 vcmpltps_naive(Long2 a, Long2 b){
        //TODO: Write this test
        return null;
    }

    public static Long2 vcmpltps(Long2 a, Long2 b){
        try {
            Long2 res = (Long2) MHm128_vcmpltps.invokeExact(a,b);
            //assert assertEquals(res,vcmpltps_naive(a, b));
            return res;
        } catch (Throwable e){
            throw new Error(e);
        }
    }
    /* ========================================================================================*/
    //VCMPPS ymm1, ymm2, ymm3/m256, 0x1  -- VCMPLTPS
    public static final MethodHandle MHm256_vcmpltps = MachineCodeSnippet.make(
            "mm256_vcmpltps",MT_L4_BINARY,requires(AVX),
            new Register[][]{xmmRegistersSSE,xmmRegistersSSE,xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0],
                        in1 = regs[1],
                        in2 = regs[2];
                int[] vex = vex_prefix(rBit(out),X_LOW,bBit(in2),M_0F,W_LOW,in1,L_256,PP_NONE);
                return vex_emit(vex, 0xC2, modRM(out,in2), 0x1); //0x1 is the imm that encodes compare lessThan
            });

    private static Long4 vcmpltps_naive(Long4 a, Long4 b){
        //TODO: Write this test
        return null;
    }

    public static Long4 vcmpltps(Long4 a, Long4 b){
        try {
            Long4 res = (Long4) MHm256_vcmpltps.invokeExact(a,b);
            //assert assertEquals(res,vcmpeqps_naive(a, b));
            return res;
        } catch (Throwable e){
            throw new Error(e);
        }
    }
    /* ========================================================================================*/
    //VCMPPS xmm1, xmm2, xmm3/m128, 0xE  -- VCMPGTPS
    public static final MethodHandle MHm128_vcmpgtps = MachineCodeSnippet.make(
            "mm128_vcmpgtps",MT_L2_BINARY,requires(AVX),
            new Register[][]{xmmRegistersSSE,xmmRegistersSSE,xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0],
                        in1 = regs[1],
                        in2 = regs[2];
                int[] vex = vex_prefix(rBit(out),X_LOW,bBit(in2),M_0F,W_LOW,in1,L_128,PP_NONE);
                return vex_emit(vex, 0xC2, modRM(out,in2), 0xE); //0xE is the imm that encodes compare greater than
            });

    private static Long2 vcmpgtps_naive(Long2 a, Long2 b){
        //TODO: Write this test
        return null;
    }

    public static Long2 vcmpgtps(Long2 a, Long2 b){
        try {
            Long2 res = (Long2) MHm128_vcmpgtps.invokeExact(a,b);
            //assert assertEquals(res,vcmpgtps_naive(a, b));
            return res;
        } catch (Throwable e){
            throw new Error(e);
        }
    }
    /* ========================================================================================*/
    //VCMPPS ymm1, ymm2, ymm3/m256, 0xE  -- VCMPGTPS
    public static final MethodHandle MHm256_vcmpgtps = MachineCodeSnippet.make(
            "mm256_vcmpgtps",MT_L4_BINARY,requires(AVX),
            new Register[][]{xmmRegistersSSE,xmmRegistersSSE,xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0],
                        in1 = regs[1],
                        in2 = regs[2];
                int[] vex = vex_prefix(rBit(out),X_LOW,bBit(in2),M_0F,W_LOW,in1,L_256,PP_NONE);
                return vex_emit(vex, 0xC2, modRM(out,in2), 0xE); //0xE is the imm that encodes compare greaterThan
            });

    private static Long4 vcmpgtps_naive(Long4 a, Long4 b){
        //TODO: Write this test
        return null;
    }

    public static Long4 vcmpgtps(Long4 a, Long4 b){
        try {
            Long4 res = (Long4) MHm256_vcmpgtps.invokeExact(a,b);
            //assert assertEquals(res,vcmpgtps_naive(a, b));
            return res;
        } catch (Throwable e){
            throw new Error(e);
        }
    }
    /* ========================================================================================*/
    //VCMPPD xmm1, xmm2, xmm3/m128, 0x0  -- VCMPEQPD
    public static final MethodHandle MHm128_vcmpeqpd = MachineCodeSnippet.make(
            "mm128_vcmpeqpd", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0],
                         in1 = regs[1],
                         in2 = regs[2];
                //VEX.NDS.128.66.0F.WIG C2 /r ib
                int[] vex = vex_prefix(rBit(out),X_LOW,bBit(in2),M_0F,W_LOW,in1,L_128,PP_66);
                return vex_emit(vex, 0xC2, modRM(out,in2), 0x0); //0x0 is the imm that encodes compare equal
            });

    private static Long2 vcmpeqpd_naive(Long2 a, Long2 b) {
        //TODO: Write this test
        return null;
    }

    public static Long2 vcmpeqpd(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm128_vcmpeqpd.invokeExact(a, b);
            //assert assertEquals(res,vcmpeqps_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    //VCMPPD ymm1, ymm2, ymm3/m256, 0x0  -- VCMPEQPD
    public static final MethodHandle MHm256_vcmpeqpd = MachineCodeSnippet.make(
            "mm256_vcmpeqpd",MT_L4_BINARY,requires(AVX),
            new Register[][]{xmmRegistersSSE,xmmRegistersSSE,xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0],
                         in1 = regs[1],
                         in2 = regs[2];
       //VEX.NDS.256.66.0F.WIG C2 /r ib
                int[] vex = vex_prefix(rBit(out),X_LOW,bBit(in2),M_0F,W_LOW,in1,L_256,PP_66);
                return vex_emit(vex, 0xC2, modRM(out,in2), 0x0); //0x0 is the imm that encodes compare equal
            });

    private static Long4 vcmpeqpd_naive(Long4 a, Long4 b){
        //TODO: Write this test
        return null;
    }

    public static Long4 vcmpeqpd(Long4 a, Long4 b){
        try {
            Long4 res = (Long4) MHm256_vcmpeqpd.invokeExact(a,b);
            //assert assertEquals(res,vcmpeqps_naive(a, b));
            return res;
        } catch (Throwable e){
            throw new Error(e);
        }
    }
    /* ========================================================================================*/
    //VCMPPD xmm1, xmm2, xmm3/m128, 0x1  -- VCMPLTPD
    public static final MethodHandle MHm128_vcmpltpd = MachineCodeSnippet.make(
            "mm128_vcmpltpd",MT_L2_BINARY,requires(AVX),
            new Register[][]{xmmRegistersSSE,xmmRegistersSSE,xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0],
                        in1 = regs[1],
                        in2 = regs[2];
       //VEX.NDS.128.66.0F.WIG C2 /r ib
                int[] vex = vex_prefix(rBit(out),X_LOW,bBit(in2),M_0F,W_LOW,in1,L_128,PP_66);
                return vex_emit(vex, 0xC2, modRM(out,in2), 0x1); //0x1 is the imm that encodes compare lessThan
            });

    private static Long2 vcmpltpd_naive(Long2 a, Long2 b){
        //TODO: Write this test
        return null;
    }

    public static Long2 vcmpltpd(Long2 a, Long2 b){
        try {
            Long2 res = (Long2) MHm128_vcmpltpd.invokeExact(a,b);
            //assert assertEquals(res,vcmpltps_naive(a, b));
            return res;
        } catch (Throwable e){
            throw new Error(e);
        }
    }
    /* ========================================================================================*/
    //VCMPPD ymm1, ymm2, ymm3/m256, 0x1  -- VCMPLTPD
    public static final MethodHandle MHm256_vcmpltpd = MachineCodeSnippet.make(
            "mm256_vcmpltpd",MT_L4_BINARY,requires(AVX),
            new Register[][]{xmmRegistersSSE,xmmRegistersSSE,xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0],
                        in1 = regs[1],
                        in2 = regs[2];
       //VEX.NDS.256.66.0F.WIG C2 /r ib
                int[] vex = vex_prefix(rBit(out),X_LOW,bBit(in2),M_0F,W_LOW,in1,L_256,PP_66);
                return vex_emit(vex, 0xC2, modRM(out,in2), 0x1); //0x1 is the imm that encodes compare lessThan
            });

    private static Long4 vcmpltpd_naive(Long4 a, Long4 b){
        //TODO: Write this test
        return null;
    }

    public static Long4 vcmpltpd(Long4 a, Long4 b){
        try {
            Long4 res = (Long4) MHm256_vcmpltpd.invokeExact(a,b);
            //assert assertEquals(res,vcmpltps_naive(a, b));
            return res;
        } catch (Throwable e){
            throw new Error(e);
        }
    }
    /* ========================================================================================*/
    //VCMPPD xmm1, xmm2, xmm3/m128, 0xE  -- VCMPGTPD
    public static final MethodHandle MHm128_vcmpgtpd = MachineCodeSnippet.make(
            "mm128_vcmpgtpd",MT_L2_BINARY,requires(AVX),
            new Register[][]{xmmRegistersSSE,xmmRegistersSSE,xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0],
                        in1 = regs[1],
                        in2 = regs[2];
                int[] vex = vex_prefix(rBit(out),X_LOW,bBit(in2),M_0F,W_LOW,in1,L_128,PP_66);
                return vex_emit(vex, 0xC2, modRM(out,in2), 0xE); //0xE is the imm that encodes compare greaterThan
            });

    private static Long2 vcmpgtpd_naive(Long2 a, Long2 b){
        //TODO: Write this test
        return null;
    }

    public static Long2 vcmpgtpd(Long2 a, Long2 b){
        try {
            Long2 res = (Long2) MHm128_vcmpgtpd.invokeExact(a,b);
            //assert assertEquals(res,vcmpgtps_naive(a, b));
            return res;
        } catch (Throwable e){
            throw new Error(e);
        }
    }
    /* ========================================================================================*/
    //VCMPPD ymm1, ymm2, ymm3/m256, 0xE  -- VCMPGTPD
    public static final MethodHandle MHm256_vcmpgtpd = MachineCodeSnippet.make(
            "mm256_vcmpgtpd",MT_L4_BINARY,requires(AVX),
            new Register[][]{xmmRegistersSSE,xmmRegistersSSE,xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0],
                        in1 = regs[1],
                        in2 = regs[2];
                int[] vex = vex_prefix(rBit(out),X_LOW,bBit(in2),M_0F,W_LOW,in1,L_256,PP_66);
                return vex_emit(vex, 0xC2, modRM(out,in2), 0xE); //0xE is the imm that encodes compare greaterThan
            });

    private static Long4 vcmpgtpd_naive(Long4 a, Long4 b){
        //TODO: Write this test
        return null;
    }

    public static Long4 vcmpgtpd(Long4 a, Long4 b){
        try {
            Long4 res = (Long4) MHm256_vcmpgtpd.invokeExact(a,b);
            //assert assertEquals(res,vcmpgtps_naive(a, b));
            return res;
        } catch (Throwable e){
            throw new Error(e);
        }
    }
    /* ========================================================================================*/
    //VSQRTPS ymm1, ymm2
    public static final MethodHandle MHm256_vsqrtps = MachineCodeSnippet.make(
            "m256_vsqrtps", MT_L4_UNARY, requires(AVX),
            new Register[][]{xmmRegistersSSE,xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in  = regs[1];
       //VEX.256.0F.WIG 51 /r
                int[] vex = vex_prefix_nonds(rBit(out), X_LOW, bBit(in), M_0F, W_LOW, 0b1111, L_256, PP_NONE);
                return vex_emit(vex, 0x51, modRM(out,in));
            }
    );

    private static Long4 vsqrtps_naive(Long4 a){
        //TODO: Write this test
        return null;
    }

    public static Long4 vsqrtps(Long4 a){
        try {
            return (Long4) MHm256_vsqrtps.invokeExact(a);
        } catch (Throwable e){
            throw new Error(e);
        }
    }
    /* ========================================================================================*/
    //VSQRTPS xmm1, xmm2
    public static final MethodHandle MHm128_vsqrtps = MachineCodeSnippet.make(
            "m128_vsqrtps", MT_L2_UNARY, requires(AVX),
            new Register[][]{xmmRegistersSSE,xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in  = regs[1];
       //VEX.128.0F.WIG 51 /r
                int[] vex = vex_prefix_nonds(rBit(out), X_LOW, bBit(in), M_0F, W_LOW, 0b1111, L_128, PP_NONE);
                return vex_emit(vex, 0x51, modRM(out,in));
            }
    );

    private static Long2 vsqrtps_naive(Long2 a){
        //TODO: Write this test
        return null;
    }

    public static Long2 vsqrtps(Long2 a){
        try {
            return (Long2) MHm128_vsqrtps.invokeExact(a);
        } catch (Throwable e){
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    //VSQRTPD xmm1, xmm2
    public static final MethodHandle MHm128_vsqrtpd = MachineCodeSnippet.make(
            "m128_vsqrtpd", MT_L2_UNARY, requires(AVX),
            new Register[][]{xmmRegistersSSE,xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in  = regs[1];
                //VEX.128.66.0F.WIG 51 /r
                int[] vex = vex_prefix_nonds(rBit(out), X_LOW, bBit(in), M_0F, W_LOW, 0b1111, L_128, PP_66);
                return vex_emit(vex, 0x51, modRM(out,in));
            }
    );

    private static Long2 vsqrtpd_naive(Long2 a){
        //TODO: Write this test
        return null;
    }

    public static Long2 vsqrtpd(Long2 a){
        try {
            return (Long2) MHm128_vsqrtpd.invokeExact(a);
        } catch (Throwable e){
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    //VSQRTPD ymm1, ymm2
    public static final MethodHandle MHm256_vsqrtpd = MachineCodeSnippet.make(
            "m256_vsqrtpd", MT_L4_UNARY, requires(AVX),
            new Register[][]{xmmRegistersSSE,xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in  = regs[1];
                //VEX.256.66.0F.WIG 51 /r
                int[] vex = vex_prefix_nonds(rBit(out), X_LOW, bBit(in), M_0F, W_LOW, 0b1111, L_256, PP_66);
                return vex_emit(vex, 0x51, modRM(out,in));
            }
    );

    private static Long4 vsqrtpd_naive(Long4 a){
        //TODO: Write this test
        return null;
    }

    public static Long4 vsqrtpd(Long4 a){
        try {
            return (Long4) MHm256_vsqrtpd.invokeExact(a);
        } catch (Throwable e){
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    //VCVTPS2DQ xmm1, xmm2
    public static final MethodHandle MHm128_vcvtps2dq = MachineCodeSnippet.make(
            "m128_vcvtps2dq", MT_L2_UNARY, requires(AVX),
            new Register[][]{xmmRegistersSSE,xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in  = regs[1];
       //VEX.128.66.0f.WIG 5B /r
                int[] vex = vex_prefix_nonds(rBit(out), X_LOW, bBit(in), M_0F, W_LOW, 0b1111, L_128, PP_66);
                return vex_emit(vex, 0x5B, modRM(out,in));
            }
    );
    private static Long2 vcvtps2dq_naive(Long2 a){
        //TODO: Write this test
        return null;
    }

    public static Long2 vcvtps2dq(Long2 a){
        try {
            return (Long2) MHm128_vcvtps2dq.invokeExact(a);
        } catch (Throwable e){
            throw new Error(e);
        }
    }
    /* ========================================================================================*/
    //VCVTPS2DQ ymm1, ymm2
    public static final MethodHandle MHm256_vcvtps2dq = MachineCodeSnippet.make(
            "m256_vcvtps2dq", MT_L4_UNARY, requires(AVX),
            new Register[][]{xmmRegistersSSE,xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in  = regs[1];
       //VEX.256.66.0f.WIG 5B /r
                int[] vex = vex_prefix_nonds(rBit(out), X_LOW, bBit(in), M_0F, W_LOW, 0b1111, L_256, PP_66);
                return vex_emit(vex, 0x5B, modRM(out,in));
            }
    );
    private static Long4 vcvtps2dq_naive(Long4 a){
        //TODO: Write this test
        return null;
    }

    @ForceInline public static Long4 vcvtps2dq(Long4 a){
        try {
            return (Long4) MHm256_vcvtps2dq.invokeExact(a);
        } catch (Throwable e){
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    //VCVTDQ2PS xmm1, xmm2
    public static final MethodHandle MHm128_vcvtdq2ps = MachineCodeSnippet.make(
            "m128_vcvtdq2ps", MT_L2_UNARY, requires(AVX),
            new Register[][]{xmmRegistersSSE,xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in  = regs[1];
                //VEX.128.0F.WIG 5B /r
                int[] vex = vex_prefix_nonds(rBit(out), X_LOW, bBit(in), M_0F, W_LOW, 0b1111, L_128, PP_NONE);
                return vex_emit(vex, 0x5B, modRM(out,in));
            }
    );

    private static Long2 vcvtdq2ps_naive(Long2 a) {
        //TODO: Write this test
        return null;
    }

    @ForceInline public static Long2 vcvtdq2ps(Long2 a){
        try {
            return (Long2) MHm128_vcvtdq2ps.invokeExact(a);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    //VCVTDQ2PS ymm1, ymm2

    public static final MethodHandle MHm256_vcvtdq2ps = MachineCodeSnippet.make(
            "m256_vcvtdq2ps", MT_L4_UNARY, requires(AVX),
            new Register[][]{xmmRegistersSSE,xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in  = regs[1];
                //VEX.256.0F.WIG 5B /r
                int[] vex = vex_prefix_nonds(rBit(out), X_LOW, bBit(in), M_0F, W_LOW, 0b1111, L_256, PP_NONE);
                return vex_emit(vex, 0x5B, modRM(out,in));
            }
    );

    private static Long4 vcvtdq2ps_naive(Long4 a) {
        //TODO: Write this test
        return null;
    }

    @ForceInline public static Long4 vcvtdq2ps(Long4 a){
        try {
           return (Long4) MHm256_vcvtdq2ps.invokeExact(a);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VMOVD xmm1, r32/m32
    public static final MethodHandle MHm32_vmovd_gpr2xmm = MachineCodeSnippet.make(
            "mm32_vmovd", MT_INT_L2, requires(AVX),
            new Register[][]{xmmRegistersSSE, cpuRegisters},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in = regs[1];
       //VEX.128.66.0F.W0 6E /r
                int[] vex = vex_prefix_nonds(rBit(out), X_LOW, bBit(in), M_0F, W_LOW, 0b1111, L_128, PP_66);
                return vex_emit(vex, 0x6E, modRM_regInd(out, in));
            });

//    private static Long2 vmovd_naive_gpr2xmm(Long2 a, Long2 b){
//        float[] res = new float[4];
//        for (int i = 0; i < 4; i++) {
//            float a_i = getInt(a, i);
//            if (i == 0) {
//                float b_i = getInt(b, i);
//                res[i] = b_i;
//            } else {
//                res[i] = a_i;
//            }
//        }
//        return Long2.make(
//                pack(res[0], res[1]),
//                pack(res[2], res[3])
//        );
//    }

    public static Long2 vmovd_gpr2xmm(int i) {
        try {
            Long2 res = (Long2) MHm32_vmovd_gpr2xmm.invokeExact(i);
            //   assert assertEquals(res, vmovd_naive_gpr2xmm(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VMOVD r32/m32, xmm1
    public static final MethodHandle MHm32_vmovd_xmm2gpr = MachineCodeSnippet.make(
            "vmovups", MT_L2_INT, requires(AVX),
            new Register[][]{cpuRegisters, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in = regs[1];
       //VEX.128.66.0F.W0 7E /r
                int[] vex = vex_prefix_nonds(rBit(out), X_LOW, bBit(in), M_0F, W_LOW, 0b1111, L_128, PP_66);
                return vex_emit(vex, 0x7E, modRM_regInd(out, in));
            });

//    private static int vmovd_naive_xmm2gpr(Long2 b){
//        int[] res = new int[4];
//        for (int i = 0; i < 4; i++) {
//            res[i] = getInt(b, i);
//        }
//        return Long2.make(
//                pack(res[0], res[1]),
//                pack(res[2], res[3])
//        );
//    }

    public static int vmovd_xmm2gpr(Long2 a) {
        try {
            int res = (int) MHm32_vmovd_xmm2gpr.invokeExact(a);
            // assert assertEquals(res, vmovd_naive_xmm2gpr(b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    //VPADDQ xmm1, xmm2, xmm3/128
    public static final MethodHandle MHm128_vpaddq = MachineCodeSnippet.make(
            "mm128_vpaddq", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.128.66.0F.WIG D4 /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_128, PP_66);
                return vex_emit(vex, 0xD4, modRM(out, in2));
            });

    private static Long2 vpaddq_naive(Long2 a, Long2 b) {
        //TODO: Write this test.
        return null;
    }

    public static Long2 vpaddq(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm128_vpaddq.invokeExact(a, b);
            //assert assertEquals(res, VADDPS_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    //VPADDQ ymm1, ymm2, ymm3/256
    public static final MethodHandle MHm256_vpaddq = MachineCodeSnippet.make(
            "mm256_vpaddq", MT_L4_BINARY, requires(AVX2),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.256.66.0F .WIG D4 /r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_256, PP_66);
                return vex_emit(vex, 0xD4, modRM(out, in2));
            });

    private static Long4 vpaddq_naive(Long4 a, Long4 b) {
        //TODO: Write this test.
        return null;
    }

    public static Long4 vpaddq(Long4 a, Long4 b) {
        try {
            Long4 res = (Long4) MHm256_vpaddq.invokeExact(a, b);
            //assert assertEquals(res, VADDPS_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    //VPSUBQ xmm1, xmm2, xmm3/m128
    public static final MethodHandle MHm128_vpsubq = MachineCodeSnippet.make(
            "mm128_vpsubq", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.128.66.0F.WIG FB/r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_128, PP_66);
                return vex_emit(vex, 0xFB, modRM(out, in2));
            });

    private static Long2 vpsubq_naive(Long2 a, Long2 b) {
        //TODO: Write this test.
        return null;
    }

    public static Long2 vpsubq(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm128_vpsubq.invokeExact(a, b);
            //assert assertEquals(res, VSUBPS_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    //VPSUBQ ymm1, ymm2, ymm3/m256
    public static final MethodHandle MHm256_vpsubq = MachineCodeSnippet.make(
            "mm256_vpsubq", MT_L4_BINARY, requires(AVX2),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in1 = regs[1];
                Register in2 = regs[2];
                //VEX.NDS.128.66.0F.WIG FB/r
                int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_256, PP_66);
                return vex_emit(vex, 0xFB, modRM(out, in2));
            });

    private static Long4 vpsubq_naive(Long4 a, Long4 b) {
        //TODO: Write this test.
        return null;
    }

    public static Long4 vpsubq(Long4 a, Long4 b) {
        try {
            Long4 res = (Long4) MHm256_vpsubq.invokeExact(a, b);
            //assert assertEquals(res, VSUBPS_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    //VPCMPEQQ xmm1, xmm2, xmm3/m128
    public static final MethodHandle MHm128_vpcmpeqq = MachineCodeSnippet.make(
            "mm128_vpcmpeqq", MT_L2_BINARY, requires(AVX),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0],
                        in1 = regs[1],
                        in2 = regs[2];
                // VEX.NDS.128.66.0F38.WIG 29 /r
                int[] vex = vex_prefix(rBit(out),X_LOW,bBit(in2),M_0F38,W_LOW,in1,L_128,PP_66);
                return vex_emit(vex, 0x29, modRM(out,in2));
            });

    private static Long2 vpcmpeqq_naive(Long2 a, Long2 b) {
        //TODO: Write this test
        return null;
    }

    public static Long2 vpcmpeqq(Long2 a, Long2 b) {
        try {
            Long2 res = (Long2) MHm128_vpcmpeqq.invokeExact(a, b);
            //assert assertEquals(res,vpcmpeqps_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    //VPCMPEQQ ymm1, ymm2, ymm3/m256
    public static final MethodHandle MHm256_vpcmpeqq = MachineCodeSnippet.make(
            "mm256_vpcmpeqq", MT_L4_BINARY, requires(AVX2),
            new Register[][]{xmmRegistersSSE, xmmRegistersSSE, xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0],
                        in1 = regs[1],
                        in2 = regs[2];
                // VEX.NDS.256.66.0F38.WIG 29 /r
                int[] vex = vex_prefix(rBit(out),X_LOW,bBit(in2),M_0F38,W_LOW,in1,L_256,PP_66);
                return vex_emit(vex, 0x29, modRM(out,in2));
            });

    private static Long4 vpcmpeqq_naive(Long4 a, Long4 b) {
        //TODO: Write this test
        return null;
    }

    public static Long4 vpcmpeqq(Long4 a, Long4 b) {
        try {
            Long4 res = (Long4) MHm256_vpcmpeqq.invokeExact(a, b);
            //assert assertEquals(res,vpcmpeqps_naive(a, b));
            return res;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    //VPCMPGTQ xmm1, xmm2, xmm3/m128
    public static final MethodHandle MHm128_vpcmpgtq = MachineCodeSnippet.make(
            "mm128_vpcmpgtq",MT_L2_BINARY,requires(AVX),
            new Register[][]{xmmRegistersSSE,xmmRegistersSSE,xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0],
                        in1 = regs[1],
                        in2 = regs[2];
                //VEX.NDS.128.66.0F38.WIG 37 /r
                int[] vex = vex_prefix(rBit(out),X_LOW,bBit(in2),M_0F38,W_LOW,in1,L_128,PP_66);
                return vex_emit(vex, 0x37, modRM(out,in2));
            });

    private static Long2 vpcmpgtq_naive(Long2 a, Long2 b){
        //TODO: Write this test
        return null;
    }

    public static Long2 vpcmpgtq(Long2 a, Long2 b){
        try {
            Long2 res = (Long2) MHm128_vpcmpgtq.invokeExact(a,b);
            //assert assertEquals(res,vcmpgtps_naive(a, b));
            return res;
        } catch (Throwable e){
            throw new Error(e);
        }
    }

    //VPCMPGTQ ymm1, ymm2, ymm3/m256
    public static final MethodHandle MHm256_vpcmpgtq = MachineCodeSnippet.make(
            "mm256_vpcmpgtq",MT_L4_BINARY,requires(AVX),
            new Register[][]{xmmRegistersSSE,xmmRegistersSSE,xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0],
                        in1 = regs[1],
                        in2 = regs[2];
                //VEX.NDS.256.66.0F38.WIG 37 /r
                int[] vex = vex_prefix(rBit(out),X_LOW,bBit(in2),M_0F38,W_LOW,in1,L_256,PP_66);
                return vex_emit(vex, 0x37, modRM(out,in2));
            });

    private static Long4 vpcmpgtq_naive(Long4 a, Long4 b){
        //TODO: Write this test
        return null;
    }

    public static Long4 vpcmpgtq(Long4 a, Long4 b){
        try {
            Long4 res = (Long4) MHm256_vpcmpgtq.invokeExact(a,b);
            //assert assertEquals(res,vcmpgtps_naive(a, b));
            return res;
        } catch (Throwable e){
            throw new Error(e);
        }
    }

    /* ========================================================================================*/
    // VBROADCASTSS ymm1, ymm2
    public static final MethodHandle MHm256_vbroadcastss = MachineCodeSnippet.make(
            "vbroadcastss256", MT_L4_FLOAT, requires(AVX2),
            new Register[][]{xmmRegistersSSE,xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in  = regs[1];
       //VEX.256.66.0F38.W0 18 /r
                int[] vex = vex_prefix_nonds(rBit(out),X_LOW, bBit(in),M_0F38,W_LOW,0b1111,L_256,PP_66);
                return vex_emit(vex,0x18,modRM(out,in));
            });

    @ForceInline public static Long4 broadcastFloatL4(float f){
        try {
           return (Long4) MHm256_vbroadcastss.invokeExact(f);
        } catch (Throwable e){
            throw new Error(e);
        }
    }

    // VBROADCASTSS xmm1, xmm2
    public static final MethodHandle MHm128_vbroadcastss = MachineCodeSnippet.make(
            "vbroadcastss128", MT_L2_FLOAT, requires(AVX2),
            new Register[][]{xmmRegistersSSE,xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in  = regs[1];
       //VEX.128.66.0F38.W0 18 /r
                int[] vex = vex_prefix_nonds(rBit(out),X_LOW, bBit(in),M_0F38,W_LOW,0b1111,L_256,PP_66);
                return vex_emit(vex,0x18,modRM(out,in));
            });

    public static Long2 broadcastFloatL2(float f){
        try {
            return (Long2) MHm128_vbroadcastss.invokeExact(f);
        } catch (Throwable e){
            throw new Error(e);
        }
    }

    // VBROADCASTSD ymm1, xmm2
    public static final MethodHandle MHm256_vbroadcastsd = MachineCodeSnippet.make(
            "vbroadcastsd256", MT_L2_FLOAT, requires(AVX2),
            new Register[][]{xmmRegistersSSE,xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in  = regs[1];
       //VEX.256.66.0F38.W0 19 /r
                int[] vex = vex_prefix_nonds(rBit(out),X_LOW, bBit(in),M_0F38,W_LOW,0b1111,L_256,PP_66);
                return vex_emit(vex,0x19,modRM(out,in));
            });

    public static Long4 broadcastDoubleL4(double d){
        try {
            return (Long4) MHm256_vbroadcastsd.invokeExact(d);
        } catch (Throwable e){
            throw new Error(e);
        }
    }

    // VBROADCASTSD xmm1, xmm2
    public static final MethodHandle MHm128_vbroadcastsd = MachineCodeSnippet.make(
            "vbroadcastsd128", MT_L2_FLOAT, requires(AVX2),
            new Register[][]{xmmRegistersSSE,xmmRegistersSSE},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in  = regs[1];
       //VEX.128.66.0F38.W0 19 /r
                int[] vex = vex_prefix_nonds(rBit(out),X_LOW, bBit(in),M_0F38,W_LOW,0b1111,L_256,PP_66);
                return vex_emit(vex,0x18,modRM(out,in));
            });

    public static Long2 broadcastDoubleL2(double d){
        try {
            return (Long2) MHm128_vbroadcastsd.invokeExact(d);
        } catch (Throwable e){
            throw new Error(e);
        }
    }


    public static final MethodHandle MHm128_intBroadcast = MachineCodeSnippet.make(
            "intbroadcast128", MT_L2_INT, requires(AVX2),
            new Register[][]{xmmRegistersSSE,cpuRegisters},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in  = regs[1];

                return join(
                            vpinsrd(out,out,in,0x0)
                        ,   vpbroadcastD(out,out,L_128)
                );
            });

    public static Long2 broadcastIntL2(int i){
        try {
           return (Long2) MHm128_intBroadcast.invokeExact(i);
        } catch (Throwable e){
            throw new Error(e);
        }
    }

    public static final MethodHandle MHm256_intBroadcast = MachineCodeSnippet.make(
            "intbroadcast256", MT_L4_INT, requires(AVX2),
            new Register[][]{xmmRegistersSSE,cpuRegisters},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in  = regs[1];

                return join(
                            vpinsrd(out,out,in,0x0)
                        ,   vpbroadcastD(out,out,L_256)
                );
            });

    public static Long4 broadcastIntL4(int i){
        try {
            return (Long4) MHm256_intBroadcast.invokeExact(i);
        } catch (Throwable e){
            throw new Error(e);
        }
    }

    public static final MethodHandle MHm128_byteBroadcast = MachineCodeSnippet.make(
            "bytebroadcast128", MT_L2_BYTE, requires(AVX2),
            new Register[][]{xmmRegistersSSE,cpuRegisters},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in  = regs[1];

                return join(
                        vpinsrb(out,out,in,0x0)
                        ,   vpbroadcastB(out,out,L_128)
                );
            });

    public static Long2 broadcastByteL2(byte i){
        try {
            return (Long2) MHm128_byteBroadcast.invokeExact(i);
        } catch (Throwable e){
            throw new Error(e);
        }
    }

    public static final MethodHandle MHm256_byteBroadcast = MachineCodeSnippet.make(
            "bytebroadcast256", MT_L4_BYTE, requires(AVX2),
            new Register[][]{xmmRegistersSSE,cpuRegisters},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in  = regs[1];

                return join(
                        vpinsrb(out,out,in,0x0)
                        ,   vpbroadcastB(out,out,L_256)
                );
            });

    public static Long4 broadcastByteL4(byte i){
        try {
            return (Long4) MHm256_byteBroadcast.invokeExact(i);
        } catch (Throwable e){
            throw new Error(e);
        }
    }

    public static final MethodHandle MHm128_shortBroadcast = MachineCodeSnippet.make(
            "shortbroadcast128", MT_L2_SHORT, requires(AVX2),
            new Register[][]{xmmRegistersSSE,cpuRegisters},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in  = regs[1];

                return join(
                        vpinsrw(out,out,in,0x0)
                        ,   vpbroadcastW(out,out,L_128)
                );
            });

    public static Long2 broadcastShortL2(short i){
        try {
            return (Long2) MHm128_shortBroadcast.invokeExact(i);
        } catch (Throwable e){
            throw new Error(e);
        }
    }

    public static final MethodHandle MHm256_shortBroadcast = MachineCodeSnippet.make(
            "shortbroadcast256", MT_L4_SHORT, requires(AVX2),
            new Register[][]{xmmRegistersSSE,cpuRegisters},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in  = regs[1];

                return join(
                        vpinsrw(out,out,in,0x0)
                        ,   vpbroadcastW(out,out,L_256)
                );
            });

    public static Long4 broadcastShortL4(short i){
        try {
            return (Long4) MHm256_shortBroadcast.invokeExact(i);
        } catch (Throwable e){
            throw new Error(e);
        }
    }

    public static final MethodHandle MHm128_longBroadcast = MachineCodeSnippet.make(
            "longbroadcast128", MT_L2_LONG, requires(AVX2),
            new Register[][]{xmmRegistersSSE,cpuRegisters},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in  = regs[1];

                return join(
                        vpinsrq(out,out,in,0x0)
                        ,   vpbroadcastQ(out,out,L_128)
                );
            });

    public static Long2 broadcastLongL2(long i){
        try {
            return (Long2) MHm128_longBroadcast.invokeExact(i);
        } catch (Throwable e){
            throw new Error(e);
        }
    }

    public static final MethodHandle MHm256_longBroadcast = MachineCodeSnippet.make(
            "longbroadcast256", MT_L4_LONG, requires(AVX2),
            new Register[][]{xmmRegistersSSE,cpuRegisters},
            (Register[] regs) -> {
                Register out = regs[0];
                Register in  = regs[1];

                return join(
                        vpinsrq(out,out,in,0x0)
                        ,   vpbroadcastQ(out,out,L_256)
                );
            });

    public static Long4 broadcastLongL4(long i){
        try {
            return (Long4) MHm256_longBroadcast.invokeExact(i);
        } catch (Throwable e){
            throw new Error(e);
        }
    }

    private static int[] vpbroadcastD(Register out, Register in, VEXLength l) {
        int[] vex = vex_prefix_nonds(rBit(out), X_LOW, bBit(in), M_0F38, W_LOW, 0b1111, l,PP_66);
        return vex_emit(vex, 0x58, modRM(out,in));
    }

    private static int[] vpbroadcastB(Register out, Register in, VEXLength l) {
        int[] vex = vex_prefix_nonds(rBit(out), X_LOW, bBit(in), M_0F38, W_LOW, 0b1111, l,PP_66);
        return vex_emit(vex, 0x78, modRM(out,in));
    }

    private static int[] vpbroadcastW(Register out, Register in, VEXLength l) {
        int[] vex = vex_prefix_nonds(rBit(out), X_LOW, bBit(in), M_0F38, W_LOW, 0b1111, l,PP_66);
        return vex_emit(vex, 0x79, modRM(out,in));
    }

    private static int[] vpbroadcastQ(Register out, Register in, VEXLength l) {
        int[] vex = vex_prefix_nonds(rBit(out), X_LOW, bBit(in), M_0F38, W_LOW, 0b1111, l,PP_66);
        return vex_emit(vex, 0x59, modRM(out,in));
    }


    private static int[] vpinsrd(Register out, Register in1, Register in2, int imm){
        int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F3A, W_LOW, in1, L_128, PP_66);
        return vex_emit(vex, 0x22, modRM(out, in2), imm);
    }

    private static int[] vpinsrb(Register out, Register in1, Register in2, int imm){
        int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F3A, W_LOW, in1, L_128, PP_66);
        return vex_emit(vex, 0x20, modRM(out, in2), imm);
    }

    private static int[] vpinsrw(Register out, Register in1, Register in2, int imm){
        int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F, W_LOW, in1, L_128, PP_66);
        return vex_emit(vex, 0xC4, modRM(out, in2), imm);
    }

    private static int[] vpinsrq(Register out, Register in1, Register in2, int imm){
        int[] vex = vex_prefix(rBit(out), X_LOW, bBit(in2), M_0F3A, W_HIGH, in1, L_128, PP_66);
        return vex_emit(vex, 0x22, modRM(out, in2), imm);
    }
    /* ========================================================================================*/
    //PDEP r32a, r32b, r32(mask)
    public static final MethodHandle MHm32_pdep = MachineCodeSnippet.make(
        "pdep32", MT_INT_BINARY, requires(BMI2),
         new Register[][]{cpuRegisters,cpuRegisters,cpuRegisters},
         (Register[] regs) -> {
            Register out  = regs[0];
            Register in1  = regs[1];
            Register in2  = regs[2];
       //VEX.NDS.LZ.F2.0F38.W0 F5 /r
            int[] vex = vex_prefix(rBit(out),X_LOW,bBit(in2),M_0F38,W_LOW,in1,L_128 /* LZ */,PP_F2);
            return vex_emit(vex,0xF5,modRM(out,in2));
    });

    //PDEP r64a, r64b, r64(mask)
    public static final MethodHandle MHm64_pdep = MachineCodeSnippet.make(
            "pdep64", MT_LONG_BINARY, requires(BMI2),
            new Register[][]{cpuRegisters,cpuRegisters,cpuRegisters},
            (Register[] regs) -> {
                Register out  = regs[0];
                Register in1  = regs[1];
                Register in2  = regs[2];
       //VEX.NDS.LZ.F2.0F38.W1 F5 /r
                int[] vex = vex_prefix(rBit(out),X_LOW,bBit(in2),M_0F38,W_HIGH,in1,L_128 /* LZ */,PP_F2);
                return vex_emit(vex,0xF5,modRM(out,in2));
    });

    //PEXT r32a, r32b, r32(mask)
    public static final MethodHandle MHm32_pext = MachineCodeSnippet.make(
            "pext32", MT_INT_BINARY, requires(BMI2),
            new Register[][]{cpuRegisters,cpuRegisters,cpuRegisters},
            (Register[] regs) -> {
                Register out  = regs[0];
                Register in1  = regs[1];
                Register in2  = regs[2];
       //VEX.NDS.LZ.F3.0F38.W0 F5 /r
                int[] vex = vex_prefix(rBit(out),X_LOW,bBit(in2),M_0F38,W_LOW,in1,L_128 /* LZ */,PP_F3);
                return vex_emit(vex,0xF5,modRM(out,in2));
    });

    public static int pextInt(int val, int mask){
        try {
           return (int) MHm32_pext.invokeExact(val, mask);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    //PEXT r64a, r64b, r64(mask)
    public static final MethodHandle MHm64_pext = MachineCodeSnippet.make(
            "pext64", MT_LONG_BINARY, requires(BMI2),
            new Register[][]{cpuRegisters,cpuRegisters,cpuRegisters},
            (Register[] regs) -> {
                Register out  = regs[0];
                Register in1  = regs[1];
                Register in2  = regs[2];
       //VEX.NDS.LZ.F3.0F38.W1 F5 /r
                int[] vex = vex_prefix(rBit(out),X_LOW,bBit(in2),M_0F38,W_HIGH,in1,L_128 /* LZ */,PP_F3);
                return vex_emit(vex,0xF5,modRM(out,in2));
    });

    /* ========================================================================================*/
    /* Utility code for internals of assembler                                                   */
    /* ========================================================================================*/
    public static int getInt(Long2 l, int i) {
        if (i < 0 || i >= 4) throw new IllegalArgumentException("" + i);
        long r = l.extract(i / 2);
        int bits = 32 * (i % 2);
        int val = (int) (r >> bits);
        return val;
    }

    public static int getInt(Long4 l, int i) {
        if (i < 0 || i >= 8) throw new IllegalArgumentException("" + i);
        long r = l.extract(i / 2);
        int bits = 32 * (i % 2);
        int val = (int) (r >> bits);
        return val;
    }

    private static float getFloat(Long2 l, int i) {
        return Float.intBitsToFloat(getInt(l, i));
    }

    public static float getFloat(Long4 v, int i) {
        if (i > 7 || i < 0) throw new IllegalArgumentException("getFloat argument must be 0-7 inclusive");
        long r = v.extract(i / 2);
        int bits = 32 * (i % 2);
        return Float.intBitsToFloat((int) (r >> bits));
    }

    private static double getDouble(Long2 l, int i) {
        return Double.longBitsToDouble(getLong(l, i));
    }

    public static long getLong(Long2 l, int i) {
        if (i < 0 || i >= 2) throw new IllegalArgumentException("" + i);
        long r = l.extract(i / 2);
        //int bits = 32 * (i % 2);
        //int val = (int) (r >> bits);
        return r;
    }

    public static long pack(int lo, int hi) {
        long hiPacked = ((long) hi) << 32;
        long loPacked = lo & 0xFFFFFFFFL;
        return hiPacked | loPacked;
    }

    public static long pack(float lo, float hi) {
        return pack(Float.floatToIntBits(lo), Float.floatToIntBits(hi));
    }

    private static boolean assertEquals(Object o1, Object o2) {
        if (o1 == null && o2 == null) return true;
        if (o1 != null && o1.equals(o2)) return true;
        throw new AssertionError(o1 + " vs " + o2);
    }

    private static int sibScale(int i) {
        switch (i) {
            case 1:
                return 0;
            case 2:
                return 1;
            case 4:
                return 2;
            case 8:
                return 3;
            default:
                throw new UnsupportedOperationException("sibScale rescales 1,2,4, or 8");
        }
    }

    interface Bitty {
        boolean isHigh();
    }

    enum VEXRBit implements Bitty {
        R_LOW(0b0),
        R_HIGH(0b1);

        private final int encoding;

        VEXRBit(int enc) {
            encoding = enc;
        }

        int encoding() {
            return encoding;
        }

        public boolean isHigh() {
            return this == R_HIGH;
        }

        public boolean isLow() {
            return this == R_LOW;
        }
    }

    enum VEXXBit implements Bitty {
        X_LOW(0b0),
        X_HIGH(0b1);

        private final int encoding;

        VEXXBit(int enc) {
            encoding = enc;
        }

        int encoding() {
            return encoding;
        }

        public boolean isHigh() {
            return this == X_HIGH;
        }

        public boolean isLow() {
            return this == X_LOW;
        }
    }

    public enum VEXBBit implements Bitty {
        B_LOW(0b0),
        B_HIGH(0b1);

        private final int encoding;

        VEXBBit(int enc) {
            encoding = enc;
        }

        int encoding() {
            return encoding;
        }

        public boolean isHigh() {
            return this == B_HIGH;
        }

        public boolean isLow() {
            return this == B_LOW;
        }

    }

    public enum VEXWBit implements Bitty {
        W_LOW(0b0),
        W_HIGH(0b1);

        private final int encoding;

        VEXWBit(int enc) {
            encoding = enc;
        }

        int encoding() {
            return encoding;
        }

        public boolean isHigh() {
            return this == W_HIGH;
        }

        public boolean isLow() {
            return this == W_LOW;
        }
    }

    public enum VEXLength {
        L_128(0b0),
        L_256(0b1);

        private final int encoding;

        VEXLength(int enc) {
            encoding = enc;
        }

        int encoding() {
            return encoding;
        }
    }

    public enum VEXOpcode {
        M_Reserved(0b0),
        M_0F(0b00001),
        M_0F38(0b00010),
        M_0F3A(0b00011);
        private final int encoding;

        VEXOpcode(int enc) {
            encoding = enc;
        }

        int encoding() {
            return encoding;
        }
    }

    public enum SIMDPrefix {
        PP_NONE(0b00),
        PP_66(0b01),
        PP_F3(0b10),
        PP_F2(0b11);
        private final int encoding;

        SIMDPrefix(int enc) {
            encoding = enc;
        }

        int encoding() {
            return encoding;
        }
    }


}


