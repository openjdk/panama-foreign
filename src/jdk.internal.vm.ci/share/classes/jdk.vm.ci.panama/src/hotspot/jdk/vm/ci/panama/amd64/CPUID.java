/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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

package jdk.vm.ci.panama.amd64;

import jdk.vm.ci.amd64.AMD64;
import jdk.vm.ci.panama.MachineCodeSnippet;

import java.lang.invoke.MethodHandle;

import static jdk.vm.ci.amd64.AMD64.*;

public final class CPUID {
    private static final boolean IS_X64;
    static {
        String arch = System.getProperties().getProperty("os.arch");
        IS_X64 = "x86_64".equals(arch) || "amd64".equals(arch);
    }

    public static boolean isSupported() {
        return IS_X64;
    }

    static final MethodHandle MHcpuid = MachineCodeSnippet.builder("cpuid")
            .effects(/*no effects*/)
            .argument(int.class, rsi)
            .argument(int.class, rdx)
            .returns(Long2.class, xmm0)
            .kills(rax, rbx, rcx, rdx)
            .code(0x8B, 0xC6,                         // mov eax,esi       ;; put cpuid arguments (eax, ecx)
                  0x8B, 0xCA,                         // mov ecx,edx
                  0x0F, 0xA2,                         // cpuid
                  0x66, 0x0F, 0x3A, 0x22, 0xC0, 0x00, // pinsrd xmm0,eax,0 ;; pack result
                  0x66, 0x0F, 0x3A, 0x22, 0xC3, 0x01, // pinsrd xmm0,ebx,1 ;;
                  0x66, 0x0F, 0x3A, 0x22, 0xC1, 0x02, // pinsrd xmm0,ecx,2 ;;
                  0x66, 0x0F, 0x3A, 0x22, 0xC2, 0x03) // pinsrd xmm0,edx,3 ;;
            .make();

    public final int eax;
    public final int ebx;
    public final int ecx;
    public final int edx;

    private CPUID(int eax, int ebx, int ecx, int edx) {
        this.eax = eax;
        this.ebx = ebx;
        this.ecx = ecx;
        this.edx = edx;
    }

    public static CPUID cpuid(int eax, int ecx) {
        try {
            Long2 res = (Long2)MHcpuid.invokeExact(eax, ecx);
            return new CPUID(
                    (int)(res.extract(0) & 0xFFFFFFFF), // eax
                    (int)(res.extract(0) >>> 32),       // ebx
                    (int)(res.extract(1) & 0xFFFFFFFF), // ecx
                    (int)(res.extract(1) >>> 32));      // edx
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static boolean has(AMD64.CPUFeature feature) {
        if (!isSupported()) {
            return false; // Not supported
        }
        switch(feature) {
            case SSE:     return ((cpuid(0x01, 0).edx >>> 25) & 1) != 0; // CPUID.EAX=01H:EDX.SSE  [bit 25] = 1
            case SSE2:    return ((cpuid(0x01, 0).edx >>> 26) & 1) != 0; // CPUID.EAX=01H:EDX.SSE2 [bit 26] = 1
            case SSE3:    return ((cpuid(0x01, 0).ecx >>>  0) & 1) != 0; // CPUID.EAX=01H:ECX.SSE3 [bit  0] = 1
            case SSSE3:   return ((cpuid(0x01, 0).ecx >>>  9) & 1) != 0; // CPUID.EAX=01H:ECX.SSSE3[bit  9] = 1
            case SSE4_1:  return ((cpuid(0x01, 0).ecx >>> 19) & 1) != 0; // CPUID.EAX=01H:ECX.SSE41[bit 19] = 1
            case SSE4_2:  return ((cpuid(0x01, 0).ecx >>> 20) & 1) != 0; // CPUID.EAX=01H:ECX.SSE42[bit 20] = 1
            case AVX:     return ((cpuid(0x01, 0).ecx >>> 28) & 1) != 0; // CPUID.EAX=01H:ECX.AVX  [bit 28] = 1
            case AVX2:    return ((cpuid(0x07, 0).ebx >>>  5) & 1) != 0; // CPUID.EAX=07H.EBX.AVX2 [bit  5] = 1
            //case AVX512*: return false; // TODO
            default:
                throw new Error("Unknown capability: "+feature.toString());
        }
    }
    @Override
    public String toString() {
        return String.format("CPUID{ eax=%08x; ebx=%08x; ecx=%08x; edx=%08x }",
                eax, ebx, ecx, edx);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CPUID cpuid = (CPUID) o;

        if (eax != cpuid.eax) return false;
        if (ebx != cpuid.ebx) return false;
        if (ecx != cpuid.ecx) return false;
        return edx == cpuid.edx;

    }

    @Override
    public int hashCode() {
        int result = eax;
        result = 31 * result + ebx;
        result = 31 * result + ecx;
        result = 31 * result + edx;
        return result;
    }
}
