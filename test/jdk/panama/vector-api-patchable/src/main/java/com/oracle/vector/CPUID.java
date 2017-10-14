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
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.vector;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

public class CPUID {
    static boolean isX64() {
        String arch = System.getProperties().getProperty("os.arch");
        return "x86_64".equals(arch) || "amd64".equals(arch);
    }

    private static final MethodHandle MHcpuid = jdk.vm.ci.panama.MachineCodeSnippet.make(
            "cpuid", MethodType.methodType(Long2.class, int.class /*esi*/, int.class /*edx*/),
            /*isSupported=*/isX64(),
            0x53,                                // push rbx          ;; callee-saved reg
            0x8B, 0xC6,                          // mov eax,esi       ;; put cpuid arguments (eax, ecx)
            0x8B, 0xCA,                          // mov ecx,edx
            0x0F, 0xA2,                          // cpuid
            0x66, 0x0F, 0x3A, 0x22, 0xC0, 0x00,  // pinsrd xmm0,eax,0 ;; pack result
            0x66, 0x0F, 0x3A, 0x22, 0xC3, 0x01,  // pinsrd xmm0,ebx,1
            0x66, 0x0F, 0x3A, 0x22, 0xC1, 0x02,  // pinsrd xmm0,ecx,2
            0x66, 0x0F, 0x3A, 0x22, 0xC2, 0x03,  // pinsrd xmm0,edx,3
            0x5B);                               // pop rbx           ;; restore


    private final int eax;
    private final int ebx;
    private final int ecx;
    private final int edx;

    private CPUID(int eax, int ebx, int ecx, int edx) {
        this.eax = eax;
        this.ebx = ebx;
        this.ecx = ecx;
        this.edx = edx;
    }

    private static CPUID cpuid(int eax, int ecx) {
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

    enum Capabilities {
        SSE, SSE2, SSE3, SSSE3, SSE41, SSE42,
        AVX, AVX2, AVX512, BMI2
    }

    static boolean has(Capabilities cap) {
        switch(cap) {
            case SSE:    return ((cpuid(0x01, 0).edx >>> 25) & 1) != 0; // CPUID.EAX=01H:EDX.SSE  [bit 25] = 1
            case SSE2:   return ((cpuid(0x01, 0).edx >>> 26) & 1) != 0; // CPUID.EAX=01H:EDX.SSE2 [bit 26] = 1
            case SSE3:   return ((cpuid(0x01, 0).ecx >>>  0) & 1) != 0; // CPUID.EAX=01H:ECX.SSE3 [bit  0] = 1
            case SSSE3:  return ((cpuid(0x01, 0).ecx >>>  9) & 1) != 0; // CPUID.EAX=01H:ECX.SSSE3[bit  9] = 1
            case SSE41:  return ((cpuid(0x01, 0).ecx >>> 19) & 1) != 0; // CPUID.EAX=01H:ECX.SSE41[bit 19] = 1
            case SSE42:  return ((cpuid(0x01, 0).ecx >>> 20) & 1) != 0; // CPUID.EAX=01H:ECX.SSE42[bit 20] = 1
            case AVX:    return ((cpuid(0x01, 0).ecx >>> 28) & 1) != 0; // CPUID.EAX=01H:ECX.AVX  [bit 28] = 1
            case AVX2:   return ((cpuid(0x07, 0).ebx >>>  5) & 1) != 0; // CPUID.EAX=07H.EBX.AVX2 [bit  5] = 1
            case BMI2:   return ((cpuid(0x07, 0).ebx >>>  8) & 1) != 0; // CPUID.EAX=07H.EBX.BMI  [bit  8] = 1
            case AVX512: return false; // TODO
            default:
                throw new Error("Unknown capability: "+cap.toString());
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

    public static void main(String[] args) {
        if (!isX64())  return; // Not supported

        int max = cpuid(0, 0).eax;
        for (int i = 0; i < max; i++) {
            System.out.printf("0x%02xH: %s\n", i, cpuid(i, 0));
        }
    }
}
