package com.oracle.vector;/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
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
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

import jdk.vm.ci.panama.MachineCodeSnippet;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import static com.oracle.vector.CPUID.Capabilities.SSE2;
import static com.oracle.vector.CPUID.Capabilities.SSE41;
import static com.oracle.vector.CPUID.Capabilities.SSSE3;

final class LowLevelVectorOps {

    static boolean isX64() {
        String arch = System.getProperties().getProperty("os.arch");
        return "x86_64".equals(arch);
    }

    static boolean requires(CPUID.Capabilities cap) {
        return isX64() && CPUID.has(cap);
    }

    static final MethodType MT_L2_BINARY = MethodType.methodType(Long2.class, Long2.class, Long2.class);


    // paddb xmm0, xmm1
    public static Long2 paddb(Long2 v1, Long2 v2) {
        try {
            return (Long2) MH_m128_paddb.invokeExact(v1, v2);
        }
        catch (Throwable e) {
            throw new Error(e);
        }
    }
    static final MethodHandle MH_m128_paddb = make_m128_paddb();
    static MethodHandle make_m128_paddb() {
        return MachineCodeSnippet.make("mm128_paddb",
                                MT_L2_BINARY,
                                requires(SSE2),
                                0x66, 0x0F, 0xFC, 0xC1);
    }


    // pxor xmm0,xmm1
    public static Long2 xor(Long2 v1, Long2 v2) {
        try {
            return (Long2) MH_m128_pxor.invokeExact(v1, v2);
        }
        catch (Throwable e) {
            throw new Error(e);
        }
    }
    private static final MethodHandle MH_m128_pxor = make_m128_pxor();
    private static MethodHandle make_m128_pxor() {
        return MachineCodeSnippet.make("mm128_pxor",
                                MT_L2_BINARY,
                                requires(SSE2),
                                0x66, 0x0F, 0xEF, 0xC1);
    }


    // pshufb xmm0,xmm1
    public static Long2 pshufb(Long2 v1, Long2 v2) {
        try {
            return (Long2) MH_m128_pshufb.invokeExact(v1, v2);
        }
        catch (Throwable e) {
            throw new Error(e);
        }
    }
    private static final MethodHandle MH_m128_pshufb = make_m128_pshufb();
    private static MethodHandle make_m128_pshufb() {
        return MachineCodeSnippet.make("m128_pshufb",
                                MT_L2_BINARY,
                                requires(SSSE3),
                                0x66, 0x0F, 0x38, 0x00, 0xC1);
    }


    // pblendvb xmm1,xmm2 # mask is implicit in xmm0
    // movdqu xmm1, xmm0 # move to return correct result
    public static Long2 pblendvb(Long2 v1, Long2 v2, Long2 v3) {
        try {
            return (Long2) MH_m128_pblendvb.invokeExact(v1, v2, v3);
        }
        catch (Throwable e) {
            throw new Error(e);
        }
    }
    private static final MethodHandle MH_m128_pblendvb = make_m128_pblendvb();
    private static MethodHandle make_m128_pblendvb() {
        return MachineCodeSnippet.make("m128_pblendvb",
                                MethodType.methodType(Long2.class, Long2.class, Long2.class, Long2.class),
                                requires(SSE41),
                                0x66, 0x0F, 0x38, 0x10, 0xCA,
                                0XF3, 0x0F, 0x6F, 0xC1);
    }


    // pcmpeqb xmm0,xmm1
    public static Long2 cmpeqb(Long2 v1, Long2 v2) {
        try {
            return (Long2) MH_m128_pcmpeqb.invokeExact(v1, v2);
        }
        catch (Throwable e) {
            throw new Error(e);
        }
    }
    private static final MethodHandle MH_m128_pcmpeqb = make_m128_pcmpeqb();
    private static MethodHandle make_m128_pcmpeqb() {
        return MachineCodeSnippet.make("m128_pcmpeqb",
                                MT_L2_BINARY,
                                requires(SSE2),
                                0x66, 0x0F, 0x74, 0xC1);
    }


    // pmovmskb xmm0, rax
    public static int pmovmskb(Long2 v1) {
        try {
            return (int) MH_m128_pmovmskb.invokeExact(v1);
        }
        catch (Throwable e) {
            throw new Error(e);
        }
    }
    static final MethodHandle MH_m128_pmovmskb = make_m128_pmovmskb();
    static MethodHandle make_m128_pmovmskb() {
        return MachineCodeSnippet.make("mm128_pmovmskb",
                                MethodType.methodType(int.class, Long2.class),
                                requires(SSE2),
                                0x66, 0x48, 0x0F, 0xD7, 0xC0);
    }



    // pextrb $0, %xmm0, %eax
    public static int pextrb(Long2 v, int index) {
        try {
            return (int) MH_m128_pextrb[index].invokeExact(v);
        }
        catch (Throwable e) {
            throw new Error(e);
        }
    }
    // @Stable
    static final MethodHandle[] MH_m128_pextrb = make_m128_pextrb();
    static MethodHandle[] make_m128_pextrb() {
        MethodHandle[] hs = new MethodHandle[16];
        for (int i = 0; i < 16; i++) {
            hs[i] = MachineCodeSnippet.make("mm128_pextrb_" + i,
                                    MethodType.methodType(int.class, Long2.class),
                                     requires(SSE41),
                                     0x66, 0x0F, 0X3A, 0x14, 0xC0, i);
        }
        return hs;
    }

    // pinsrb $0, %esi, %xmm0
    public static Long2 pinsrb(Long2 v, int index, int value) {
        try {
            return (Long2) MH_m128_pinsrb[index].invokeExact(value, v);
        }
        catch (Throwable e) {
            throw new Error(e);
        }
    }
    // @Stable
    static final MethodHandle[] MH_m128_pinsrb = make_m128_pinsrb();
    static MethodHandle[] make_m128_pinsrb() {
        MethodHandle[] hs = new MethodHandle[16];
        for (int i = 0; i < 16; i++) {
            hs[i] = MachineCodeSnippet.make("mm128_pinsrb_" + i,
                                     MethodType.methodType(Long2.class, int.class, Long2.class),
                                     requires(SSE41),
                                     0x66, 0x0F, 0X3A, 0x20, 0xC6, i);
        }
        return hs;
    }
}
