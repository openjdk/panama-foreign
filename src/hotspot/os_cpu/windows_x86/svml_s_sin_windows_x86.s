;
; Copyright (c) 2018, Intel Corporation.
; Intel Short Vector Math Library (SVML) Source Code
;
; DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
;
; This code is free software; you can redistribute it and/or modify it
; under the terms of the GNU General Public License version 2 only, as
; published by the Free Software Foundation.
;
; This code is distributed in the hope that it will be useful, but WITHOUT
; ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
; FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
; version 2 for more details (a copy is included in the LICENSE file that
; accompanied this code).
;
; You should have received a copy of the GNU General Public License version
; 2 along with this work; if not, write to the Free Software Foundation,
; Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
;
; Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
; or visit www.oracle.com if you need additional information or have any
; questions.
;

INCLUDE globals_vectorApiSupport_windows.hpp
IFNB __VECTOR_API_MATH_INTRINSICS_WINDOWS

	OPTION DOTNAME

_TEXT	SEGMENT      'CODE'

TXTST0:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_sinf8_ha_l9

__svml_sinf8_ha_l9	PROC

_B1_1::

        DB        243
        DB        15
        DB        30
        DB        250
L1::

        push      r14
        push      r15
        sub       rsp, 376
        xor       r15d, r15d
        vmovups   XMMWORD PTR [208+rsp], xmm15
        vmovups   XMMWORD PTR [224+rsp], xmm14
        vmovups   XMMWORD PTR [240+rsp], xmm13
        vmovups   XMMWORD PTR [256+rsp], xmm12
        vmovups   XMMWORD PTR [272+rsp], xmm11
        vmovups   XMMWORD PTR [288+rsp], xmm10
        vmovups   XMMWORD PTR [304+rsp], xmm9
        vmovups   XMMWORD PTR [320+rsp], xmm8
        vmovups   XMMWORD PTR [336+rsp], xmm7
        vmovups   XMMWORD PTR [352+rsp], xmm6
        mov       QWORD PTR [40+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovups   ymm14, YMMWORD PTR [rcx]
        and       r13, -64
        vmovups   ymm3, YMMWORD PTR [__svml_ssin_ha_data_internal+4096]
        vmovups   ymm1, YMMWORD PTR [__svml_ssin_ha_data_internal+5056]
        vmovups   ymm0, YMMWORD PTR [__svml_ssin_ha_data_internal+4544]
        vmovupd   ymm7, YMMWORD PTR [__svml_ssin_ha_data_internal+5120]
        vmovupd   ymm9, YMMWORD PTR [__svml_ssin_ha_data_internal+5184]
        vmovupd   ymm11, YMMWORD PTR [__svml_ssin_ha_data_internal+5312]
        vmovupd   ymm12, YMMWORD PTR [__svml_ssin_ha_data_internal+5248]
        vandps    ymm4, ymm14, ymm3
        mov       QWORD PTR [368+rsp], r13
        vfmadd213ps ymm1, ymm4, ymm0
        vcvtps2pd ymm8, xmm4
        vextractf128 xmm5, ymm4, 1
        vpslld    ymm2, ymm1, 31
        vcvtps2pd ymm10, xmm5
        vsubps    ymm5, ymm1, ymm0
        vextractf128 xmm6, ymm5, 1
        vcvtps2pd ymm1, xmm5
        vcvtps2pd ymm0, xmm6
        vmovupd   ymm6, YMMWORD PTR [__svml_ssin_ha_data_internal+5376]
        vfnmadd231pd ymm8, ymm7, ymm1
        vfnmadd231pd ymm10, ymm7, ymm0
        vfnmadd213pd ymm1, ymm9, ymm8
        vfnmadd213pd ymm0, ymm9, ymm10
        vmulpd    ymm13, ymm1, ymm1
        vmulpd    ymm5, ymm0, ymm0
        vandnps   ymm7, ymm3, ymm14
        vmovupd   ymm3, YMMWORD PTR [__svml_ssin_ha_data_internal+5440]
        vmovdqa   ymm15, ymm6
        vfmadd231pd ymm15, ymm3, ymm13
        vfmadd231pd ymm6, ymm3, ymm5
        vfmadd213pd ymm15, ymm13, ymm11
        vfmadd213pd ymm6, ymm5, ymm11
        vfmadd213pd ymm15, ymm13, ymm12
        vfmadd213pd ymm6, ymm5, ymm12
        vmulpd    ymm3, ymm13, ymm15
        vmulpd    ymm8, ymm5, ymm6
        vcmpnle_uqps ymm13, ymm4, YMMWORD PTR [__svml_ssin_ha_data_internal+4160]
        vfmadd213pd ymm3, ymm1, ymm1
        vfmadd213pd ymm8, ymm0, ymm0
        vxorps    ymm1, ymm2, ymm7
        vcvtpd2ps xmm2, ymm3
        vcvtpd2ps xmm3, ymm8
        vmovmskps eax, ymm13
        test      eax, eax
        vinsertf128 ymm0, ymm2, xmm3, 1
        vxorps    ymm0, ymm0, ymm1
        jne       _B1_12

_B1_2::

        test      r15d, r15d
        jne       _B1_4

_B1_3::

        vmovups   xmm6, XMMWORD PTR [352+rsp]
        vmovups   xmm7, XMMWORD PTR [336+rsp]
        vmovups   xmm8, XMMWORD PTR [320+rsp]
        vmovups   xmm9, XMMWORD PTR [304+rsp]
        vmovups   xmm10, XMMWORD PTR [288+rsp]
        vmovups   xmm11, XMMWORD PTR [272+rsp]
        vmovups   xmm12, XMMWORD PTR [256+rsp]
        vmovups   xmm13, XMMWORD PTR [240+rsp]
        vmovups   xmm14, XMMWORD PTR [224+rsp]
        vmovups   xmm15, XMMWORD PTR [208+rsp]
        mov       r13, QWORD PTR [40+rsp]
        add       rsp, 376
        pop       r15
        pop       r14
        ret

_B1_4::

        vmovups   YMMWORD PTR [r13], ymm14
        vmovups   YMMWORD PTR [64+r13], ymm0
        je        _B1_3

_B1_7::

        xor       r14d, r14d

_B1_8::

        bt        r15d, r14d
        jc        _B1_11

_B1_9::

        inc       r14d
        cmp       r14d, 8
        jl        _B1_8

_B1_10::

        vmovups   ymm0, YMMWORD PTR [64+r13]
        jmp       _B1_3

_B1_11::

        vzeroupper
        lea       rcx, QWORD PTR [r13+r14*4]
        lea       rdx, QWORD PTR [64+r13+r14*4]

        call      __svml_ssin_ha_cout_rare_internal
        jmp       _B1_9

_B1_12::

        vpand     ymm6, ymm14, YMMWORD PTR [_2il0floatpacket_20]
        vmovups   ymm8, YMMWORD PTR [__svml_ssin_ha_data_internal+4224]
        vmovups   YMMWORD PTR [r13], ymm0
        vpsrld    ymm3, ymm6, 23
        vpslld    ymm7, ymm3, 1
        vandps    ymm10, ymm8, ymm4
        vcmpeqps  ymm12, ymm10, ymm8
        vpaddd    ymm10, ymm7, ymm3
        vpslld    ymm9, ymm10, 2
        vmovmskps eax, ymm12
        mov       DWORD PTR [32+rsp], eax
        lea       rax, QWORD PTR [__ImageBase]
        vpextrd   r8d, xmm9, 1
        vextracti128 xmm1, ymm9, 1
        vmovd     ecx, xmm9
        vmovd     xmm5, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+rax+r8]
        vmovd     r11d, xmm1
        vmovd     xmm8, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+rax+rcx]
        vpunpcklqdq xmm15, xmm8, xmm5
        vpextrd   r9d, xmm9, 2
        vpextrd   r10d, xmm9, 3
        vpextrd   r14d, xmm1, 1
        vpextrd   r15d, xmm1, 2
        vpextrd   edx, xmm1, 3
        vmovd     xmm11, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+rax+r9]
        vmovd     xmm12, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+rax+r10]
        vmovd     xmm2, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+rax+r11]
        vmovd     xmm0, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+rax+r14]
        vmovd     xmm7, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+rax+r15]
        vmovd     xmm3, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+rax+rdx]
        vpunpcklqdq xmm6, xmm11, xmm12
        vpunpcklqdq xmm10, xmm2, xmm0
        vpunpcklqdq xmm9, xmm7, xmm3
        vshufps   xmm4, xmm15, xmm6, 136
        vmovd     xmm5, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+4+rax+rcx]
        vmovd     xmm11, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+4+rax+r8]
        vmovd     xmm12, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+4+rax+r9]
        vmovd     xmm15, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+4+rax+r10]
        vshufps   xmm8, xmm10, xmm9, 136
        vmovd     xmm0, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+4+rax+r11]
        vmovd     xmm7, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+4+rax+r14]
        vmovd     xmm3, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+4+rax+r15]
        vmovd     xmm9, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+4+rax+rdx]
        vpunpcklqdq xmm6, xmm5, xmm11
        vpunpcklqdq xmm1, xmm12, xmm15
        vpunpcklqdq xmm5, xmm3, xmm9
        vshufps   xmm2, xmm6, xmm1, 136
        vmovd     xmm6, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+8+rax+r9]
        vmovd     xmm12, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+8+rax+rcx]
        vmovd     xmm15, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+8+rax+r8]
        vmovd     xmm3, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+8+rax+r11]
        vpunpcklqdq xmm1, xmm12, xmm15
        vinsertf128 ymm10, ymm4, xmm8, 1
        vpunpcklqdq xmm8, xmm0, xmm7
        vmovd     xmm4, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+8+rax+r10]
        vshufps   xmm11, xmm8, xmm5, 136
        vpunpcklqdq xmm0, xmm6, xmm4
        vpand     ymm4, ymm14, YMMWORD PTR [_2il0floatpacket_21]
        vmovd     xmm8, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+8+rax+r14]
        vmovd     xmm5, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+8+rax+r15]
        vpunpcklqdq xmm12, xmm3, xmm8
        vshufps   xmm7, xmm1, xmm0, 136
        vpaddd    ymm1, ymm4, YMMWORD PTR [_2il0floatpacket_22]
        vinsertf128 ymm9, ymm2, xmm11, 1
        vmovd     xmm11, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+8+rax+rdx]
        vpunpcklqdq xmm15, xmm5, xmm11
        vshufps   xmm6, xmm12, xmm15, 136
        vmovdqu   ymm12, YMMWORD PTR [_2il0floatpacket_23]
        vpsrld    ymm4, ymm9, 16
        vpand     ymm2, ymm9, ymm12
        vpsrld    ymm9, ymm1, 16
        vinsertf128 ymm11, ymm7, xmm6, 1
        vpand     ymm5, ymm11, ymm12
        vpsrld    ymm0, ymm11, 16
        vpand     ymm6, ymm1, ymm12
        vpand     ymm7, ymm10, ymm12
        vpsrld    ymm10, ymm10, 16
        vpmulld   ymm11, ymm9, ymm5
        vpsrld    ymm15, ymm11, 16
        vpmulld   ymm11, ymm9, ymm0
        vpmulld   ymm0, ymm6, ymm0
        vpsrld    ymm0, ymm0, 16
        vpmulld   ymm1, ymm6, ymm2
        vpand     ymm5, ymm1, ymm12
        vpaddd    ymm11, ymm11, ymm0
        vpaddd    ymm5, ymm5, ymm11
        vpaddd    ymm11, ymm15, ymm5
        vpsrld    ymm15, ymm1, 16
        vpsrld    ymm0, ymm11, 16
        vpand     ymm11, ymm11, ymm12
        vpmulld   ymm3, ymm6, ymm4
        vpmulld   ymm2, ymm9, ymm2
        vpand     ymm5, ymm3, ymm12
        vpaddd    ymm1, ymm2, ymm15
        vpsrld    ymm3, ymm3, 16
        vpaddd    ymm5, ymm5, ymm1
        vpmulld   ymm8, ymm6, ymm7
        vpmulld   ymm4, ymm9, ymm4
        vpaddd    ymm5, ymm0, ymm5
        vpand     ymm1, ymm8, ymm12
        vpaddd    ymm3, ymm4, ymm3
        vpand     ymm0, ymm14, YMMWORD PTR [_2il0floatpacket_24]
        vpsrld    ymm2, ymm5, 16
        vpslld    ymm5, ymm5, 16
        vpaddd    ymm15, ymm1, ymm3
        vpmulld   ymm3, ymm6, ymm10
        vpsrld    ymm10, ymm8, 16
        vpaddd    ymm4, ymm2, ymm15
        vpand     ymm6, ymm3, ymm12
        vpaddd    ymm2, ymm5, ymm11
        vpsrld    ymm15, ymm4, 16
        vpand     ymm4, ymm4, ymm12
        vpand     ymm5, ymm2, YMMWORD PTR [_2il0floatpacket_28]
        vpsrld    ymm2, ymm2, 18
        vpslld    ymm11, ymm5, 5
        vpmulld   ymm7, ymm9, ymm7
        vpaddd    ymm9, ymm7, ymm10
        vpxor     ymm7, ymm0, YMMWORD PTR [_2il0floatpacket_25]
        vmovups   ymm10, YMMWORD PTR [_2il0floatpacket_26]
        vpaddd    ymm8, ymm6, ymm9
        vpaddd    ymm15, ymm15, ymm8
        vpslld    ymm1, ymm15, 16
        vpxor     ymm15, ymm0, YMMWORD PTR [_2il0floatpacket_27]
        vpaddd    ymm3, ymm1, ymm4
        vpsrld    ymm12, ymm3, 9
        vpor      ymm9, ymm12, ymm7
        vpor      ymm12, ymm11, ymm15
        vaddps    ymm7, ymm9, ymm10
        vsubps    ymm4, ymm12, ymm15
        vmovups   ymm15, YMMWORD PTR [_2il0floatpacket_31]
        vsubps    ymm8, ymm7, ymm10
        vpand     ymm7, ymm7, YMMWORD PTR [_2il0floatpacket_35]
        vsubps    ymm1, ymm9, ymm8
        vpxor     ymm9, ymm0, YMMWORD PTR [_2il0floatpacket_29]
        vpand     ymm0, ymm3, YMMWORD PTR [_2il0floatpacket_30]
        vpslld    ymm7, ymm7, 4
        vpslld    ymm3, ymm0, 14
        vpor      ymm3, ymm3, ymm2
        vpor      ymm10, ymm3, ymm9
        vsubps    ymm8, ymm10, ymm9
        vaddps    ymm6, ymm1, ymm8
        vsubps    ymm5, ymm1, ymm6
        vmovups   ymm1, YMMWORD PTR [_2il0floatpacket_34]
        vaddps    ymm11, ymm8, ymm5
        vaddps    ymm8, ymm4, ymm11
        vmulps    ymm4, ymm15, ymm6
        vmovaps   ymm12, ymm15
        vfmsub213ps ymm12, ymm6, ymm4
        vfmadd132ps ymm6, ymm12, YMMWORD PTR [_2il0floatpacket_32]
        vandps    ymm0, ymm14, YMMWORD PTR [_2il0floatpacket_33]
        vcmpgt_oqps ymm9, ymm0, ymm1
        vcmple_oqps ymm2, ymm0, ymm1
        vfmadd213ps ymm8, ymm15, ymm6
        vandps    ymm3, ymm2, ymm14
        vandps    ymm10, ymm9, ymm4
        vorps     ymm3, ymm3, ymm10
        vandps    ymm10, ymm9, ymm8
        vmulps    ymm6, ymm3, ymm3
        vextracti128 xmm1, ymm7, 1
        vmovd     r14d, xmm7
        vmovd     r8d, xmm1
        vmovd     xmm9, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+rax+r14]
        vpextrd   r11d, xmm7, 1
        vpextrd   r10d, xmm7, 2
        vpextrd   r9d, xmm7, 3
        vpextrd   ecx, xmm1, 1
        vpextrd   edx, xmm1, 2
        vpextrd   r15d, xmm1, 3
        vmovd     xmm8, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+rax+r11]
        vmovd     xmm5, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+rax+r10]
        vmovd     xmm11, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+rax+r9]
        vpunpcklqdq xmm12, xmm9, xmm8
        vmovd     xmm2, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+rax+r8]
        vmovd     xmm4, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+rax+rcx]
        vmovd     xmm7, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+rax+rdx]
        vmovd     xmm9, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+rax+r15]
        vpunpcklqdq xmm15, xmm5, xmm11
        vpunpcklqdq xmm8, xmm2, xmm4
        vpunpcklqdq xmm5, xmm7, xmm9
        vshufps   xmm0, xmm12, xmm15, 136
        vshufps   xmm11, xmm8, xmm5, 136
        vmovd     xmm12, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+4+rax+r14]
        vmovd     xmm1, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+4+rax+r11]
        vmovd     xmm2, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+4+rax+r10]
        vmovd     xmm4, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+4+rax+r9]
        vpunpcklqdq xmm7, xmm12, xmm1
        vmovd     xmm5, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+4+rax+r8]
        vmovd     xmm12, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+4+rax+rdx]
        vpunpcklqdq xmm9, xmm2, xmm4
        vshufps   xmm8, xmm7, xmm9, 136
        vmovd     xmm7, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+12+rax+r14]
        vinsertf128 ymm15, ymm0, xmm11, 1
        vmovd     xmm11, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+4+rax+rcx]
        vmovd     xmm0, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+4+rax+r15]
        vpunpcklqdq xmm1, xmm5, xmm11
        vpunpcklqdq xmm2, xmm12, xmm0
        vshufps   xmm4, xmm1, xmm2, 136
        vmovd     xmm5, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+12+rax+r10]
        vmovd     xmm11, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+12+rax+r9]
        vmovd     xmm2, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+12+rax+r8]
        vpunpcklqdq xmm0, xmm5, xmm11
        vinsertf128 ymm9, ymm8, xmm4, 1
        vmovd     xmm8, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+12+rax+r11]
        vpunpcklqdq xmm12, xmm7, xmm8
        vmovd     xmm4, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+12+rax+rcx]
        vmovd     xmm7, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+12+rax+rdx]
        vmovd     xmm8, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+12+rax+r15]
        vpunpcklqdq xmm5, xmm2, xmm4
        vpunpcklqdq xmm11, xmm7, xmm8
        vshufps   xmm1, xmm12, xmm0, 136
        vshufps   xmm12, xmm5, xmm11, 136
        vmovaps   ymm8, ymm3
        vmovaps   ymm7, ymm15
        vmovups   ymm2, YMMWORD PTR [__svml_ssin_ha_data_internal+4352]
        vfmadd213ps ymm2, ymm6, YMMWORD PTR [__svml_ssin_ha_data_internal+4288]
        vmulps    ymm4, ymm6, ymm2
        vinsertf128 ymm11, ymm1, xmm12, 1
        vfmadd213ps ymm8, ymm11, ymm9
        vmovd     xmm1, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+8+rax+r9]
        vfmadd213ps ymm7, ymm3, ymm8
        vsubps    ymm5, ymm9, ymm8
        vsubps    ymm0, ymm8, ymm7
        vfmadd231ps ymm5, ymm11, ymm3
        vfmadd231ps ymm0, ymm3, ymm15
        vaddps    ymm11, ymm15, ymm11
        vmovups   ymm15, YMMWORD PTR [__svml_ssin_ha_data_internal+4480]
        vaddps    ymm8, ymm5, ymm0
        vmovd     xmm0, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+8+rax+r10]
        vfmadd213ps ymm15, ymm6, YMMWORD PTR [__svml_ssin_ha_data_internal+4416]
        vmulps    ymm5, ymm3, ymm4
        vpunpcklqdq xmm4, xmm0, xmm1
        vmovd     xmm0, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+8+rax+rcx]
        vmovd     xmm1, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+8+rax+rdx]
        vmulps    ymm12, ymm6, ymm15
        vmovd     xmm6, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+8+rax+r14]
        vmovd     xmm15, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+8+rax+r11]
        vpunpcklqdq xmm2, xmm6, xmm15
        vshufps   xmm15, xmm2, xmm4, 136
        vfnmadd213ps ymm3, ymm9, ymm11
        vmovd     xmm6, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+8+rax+r8]
        vmovd     xmm2, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+8+rax+r15]
        vpunpcklqdq xmm4, xmm6, xmm0
        vpunpcklqdq xmm6, xmm1, xmm2
        vshufps   xmm0, xmm4, xmm6, 136
        vfmadd213ps ymm5, ymm3, ymm8
        mov       r15d, DWORD PTR [32+rsp]
        vinsertf128 ymm15, ymm15, xmm0, 1
        vfmadd213ps ymm10, ymm3, ymm15
        vmovups   ymm0, YMMWORD PTR [r13]
        vfmadd213ps ymm12, ymm9, ymm10
        vaddps    ymm3, ymm12, ymm5
        vaddps    ymm10, ymm7, ymm3
        vblendvps ymm0, ymm0, ymm10, ymm13
        jmp       _B1_2
        ALIGN     16

_B1_13::

__svml_sinf8_ha_l9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_sinf8_ha_l9_B1_B12:
	DD	1732865
	DD	382065
	DD	1468524
	DD	1407075
	DD	1345626
	DD	1284177
	DD	1222728
	DD	1161279
	DD	1099830
	DD	1038381
	DD	976932
	DD	915483
	DD	3080463
	DD	3758551048

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B1_1
	DD	imagerel _B1_13
	DD	imagerel _unwind___svml_sinf8_ha_l9_B1_B12

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST1:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_sinf4_ha_e9

__svml_sinf4_ha_e9	PROC

_B2_1::

        DB        243
        DB        15
        DB        30
        DB        250
L28::

        sub       rsp, 376
        lea       r9, QWORD PTR [__ImageBase]
        vmovups   XMMWORD PTR [272+rsp], xmm15
        xor       r10d, r10d
        vmovups   XMMWORD PTR [288+rsp], xmm14
        vmovups   XMMWORD PTR [192+rsp], xmm13
        vmovups   XMMWORD PTR [208+rsp], xmm12
        vmovups   XMMWORD PTR [240+rsp], xmm11
        vmovups   XMMWORD PTR [256+rsp], xmm10
        vmovups   XMMWORD PTR [224+rsp], xmm9
        vmovups   XMMWORD PTR [320+rsp], xmm8
        vmovups   XMMWORD PTR [336+rsp], xmm7
        vmovups   XMMWORD PTR [304+rsp], xmm6
        mov       QWORD PTR [352+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovups   xmm14, XMMWORD PTR [rcx]
        and       r13, -64
        vmovups   xmm7, XMMWORD PTR [__svml_ssin_ha_data_internal+4096]
        vandps    xmm1, xmm14, xmm7
        vmulps    xmm2, xmm1, XMMWORD PTR [__svml_ssin_ha_data_internal+5056]
        vcvtps2pd ymm6, xmm1
        vmovups   xmm4, XMMWORD PTR [__svml_ssin_ha_data_internal+4544]
        mov       QWORD PTR [360+rsp], r13
        vaddps    xmm3, xmm4, xmm2
        vpslld    xmm0, xmm3, 31
        vsubps    xmm5, xmm3, xmm4
        vcvtps2pd ymm3, xmm5
        vmulpd    ymm2, ymm3, YMMWORD PTR [__svml_ssin_ha_data_internal+5120]
        vmulpd    ymm5, ymm3, YMMWORD PTR [__svml_ssin_ha_data_internal+5184]
        vandnps   xmm3, xmm7, xmm14
        vsubpd    ymm4, ymm6, ymm2
        vsubpd    ymm2, ymm4, ymm5
        vmulpd    ymm13, ymm2, ymm2
        vmulpd    ymm8, ymm13, YMMWORD PTR [__svml_ssin_ha_data_internal+5440]
        vaddpd    ymm9, ymm8, YMMWORD PTR [__svml_ssin_ha_data_internal+5376]
        vmulpd    ymm10, ymm13, ymm9
        vaddpd    ymm11, ymm10, YMMWORD PTR [__svml_ssin_ha_data_internal+5312]
        vmulpd    ymm12, ymm13, ymm11
        vaddpd    ymm15, ymm12, YMMWORD PTR [__svml_ssin_ha_data_internal+5248]
        vmulpd    ymm8, ymm13, ymm15
        vcmpnleps xmm13, xmm1, XMMWORD PTR [__svml_ssin_ha_data_internal+4160]
        vmulpd    ymm9, ymm2, ymm8
        vmovmskps eax, xmm13
        vaddpd    ymm4, ymm2, ymm9
        vxorps    xmm2, xmm0, xmm3
        vcvtpd2ps xmm0, ymm4
        vxorps    xmm0, xmm0, xmm2
        test      eax, eax
        jne       _B2_12

_B2_2::

        test      r10d, r10d
        jne       _B2_4

_B2_3::

        vzeroupper
        vmovups   xmm6, XMMWORD PTR [304+rsp]
        vmovups   xmm7, XMMWORD PTR [336+rsp]
        vmovups   xmm8, XMMWORD PTR [320+rsp]
        vmovups   xmm9, XMMWORD PTR [224+rsp]
        vmovups   xmm10, XMMWORD PTR [256+rsp]
        vmovups   xmm11, XMMWORD PTR [240+rsp]
        vmovups   xmm12, XMMWORD PTR [208+rsp]
        vmovups   xmm13, XMMWORD PTR [192+rsp]
        vmovups   xmm14, XMMWORD PTR [288+rsp]
        vmovups   xmm15, XMMWORD PTR [272+rsp]
        mov       r13, QWORD PTR [352+rsp]
        add       rsp, 376
        ret

_B2_4::

        vmovups   XMMWORD PTR [r13], xmm14
        vmovups   XMMWORD PTR [64+r13], xmm0
        je        _B2_3

_B2_7::

        xor       eax, eax
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, eax
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, r10d

_B2_8::

        bt        esi, ebx
        jc        _B2_11

_B2_9::

        inc       ebx
        cmp       ebx, 4
        jl        _B2_8

_B2_10::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        vmovups   xmm0, XMMWORD PTR [64+r13]
        jmp       _B2_3

_B2_11::

        vzeroupper
        lea       rcx, QWORD PTR [r13+rbx*4]
        lea       rdx, QWORD PTR [64+r13+rbx*4]

        call      __svml_ssin_ha_cout_rare_internal
        jmp       _B2_9

_B2_12::

        mov       r11d, 2139095040
        vmovups   xmm12, XMMWORD PTR [__svml_ssin_ha_data_internal+4224]
        vandps    xmm10, xmm12, xmm1
        vcmpeqps  xmm9, xmm10, xmm12
        vmovd     xmm4, r11d
        mov       r11d, 8388607
        vpshufd   xmm8, xmm4, 0
        vpand     xmm5, xmm8, xmm14
        vpsrld    xmm7, xmm5, 23
        vpslld    xmm11, xmm7, 1
        vpaddd    xmm15, xmm11, xmm7
        vpslld    xmm10, xmm15, 2
        vmovd     r8d, xmm10
        vmovmskps r10d, xmm9
        vmovups   XMMWORD PTR [32+rsp], xmm0
        vmovd     xmm12, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+r9+r8]
        vpextrd   ecx, xmm10, 1
        vpextrd   edx, xmm10, 2
        vpextrd   eax, xmm10, 3
        vmovd     xmm3, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+r9+rcx]
        vpunpcklqdq xmm4, xmm12, xmm3
        vmovd     xmm6, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+r9+rdx]
        vmovd     xmm8, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+4+r9+rdx]
        vmovd     xmm3, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+8+r9+rdx]
        mov       edx, 8388608
        vmovd     xmm9, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+r9+rax]
        vmovd     xmm5, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+4+r9+rax]
        vpunpcklqdq xmm1, xmm6, xmm9
        vmovd     xmm9, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+8+r9+rax]
        mov       eax, 65535
        vmovd     xmm2, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+4+r9+r8]
        vmovd     xmm0, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+4+r9+rcx]
        vpunpcklqdq xmm11, xmm2, xmm0
        vmovd     xmm2, r11d
        vmovd     xmm12, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+8+r9+rcx]
        mov       ecx, -2147483648
        vpunpcklqdq xmm7, xmm8, xmm5
        vmovd     xmm8, edx
        vmovd     xmm15, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+8+r9+r8]
        mov       r8d, 1065353216
        vpshufd   xmm0, xmm2, 0
        mov       r11d, 679477248
        vshufps   xmm10, xmm4, xmm1, 136
        vpand     xmm5, xmm0, xmm14
        vshufps   xmm6, xmm11, xmm7, 136
        mov       edx, 262143
        vpunpcklqdq xmm4, xmm15, xmm12
        vmovd     xmm15, eax
        vpunpcklqdq xmm1, xmm3, xmm9
        mov       eax, 872415232
        vpshufd   xmm7, xmm8, 0
        vshufps   xmm11, xmm4, xmm1, 136
        vpaddd    xmm3, xmm5, xmm7
        vpshufd   xmm12, xmm15, 0
        vpsrld    xmm4, xmm6, 16
        vpand     xmm2, xmm6, xmm12
        vpsrld    xmm0, xmm11, 16
        vpsrld    xmm9, xmm3, 16
        vpand     xmm6, xmm3, xmm12
        vpand     xmm11, xmm11, xmm12
        vpand     xmm7, xmm10, xmm12
        vpmulld   xmm5, xmm9, xmm11
        vpsrld    xmm10, xmm10, 16
        vpmulld   xmm11, xmm9, xmm0
        vpsrld    xmm15, xmm5, 16
        vpmulld   xmm0, xmm6, xmm0
        vpmulld   xmm1, xmm6, xmm2
        vpsrld    xmm0, xmm0, 16
        vpand     xmm5, xmm1, xmm12
        vpaddd    xmm11, xmm11, xmm0
        vpmulld   xmm3, xmm6, xmm4
        vpaddd    xmm5, xmm5, xmm11
        vpmulld   xmm2, xmm9, xmm2
        vpsrld    xmm1, xmm1, 16
        vpaddd    xmm11, xmm15, xmm5
        vpand     xmm0, xmm3, xmm12
        vpaddd    xmm15, xmm2, xmm1
        vpsrld    xmm5, xmm11, 16
        vpmulld   xmm8, xmm6, xmm7
        vpaddd    xmm1, xmm0, xmm15
        vpmulld   xmm4, xmm9, xmm4
        vpsrld    xmm15, xmm3, 16
        vpaddd    xmm5, xmm5, xmm1
        vpand     xmm1, xmm8, xmm12
        vpaddd    xmm3, xmm4, xmm15
        vpsrld    xmm2, xmm5, 16
        vpmulld   xmm6, xmm6, xmm10
        vpaddd    xmm4, xmm1, xmm3
        vpmulld   xmm7, xmm9, xmm7
        vpsrld    xmm8, xmm8, 16
        vpaddd    xmm3, xmm2, xmm4
        vpand     xmm10, xmm6, xmm12
        vpaddd    xmm6, xmm7, xmm8
        vpsrld    xmm15, xmm3, 16
        vpaddd    xmm9, xmm10, xmm6
        vpand     xmm3, xmm3, xmm12
        vpaddd    xmm15, xmm15, xmm9
        vpand     xmm11, xmm11, xmm12
        vmovd     xmm12, ecx
        vpslld    xmm4, xmm15, 16
        vpshufd   xmm1, xmm12, 0
        vmovd     xmm2, r8d
        vpshufd   xmm8, xmm2, 0
        vpaddd    xmm7, xmm4, xmm3
        vpslld    xmm5, xmm5, 16
        vpand     xmm0, xmm1, xmm14
        vpaddd    xmm5, xmm5, xmm11
        vpsrld    xmm11, xmm7, 9
        vpxor     xmm15, xmm0, xmm8
        mov       ecx, 511
        vmovups   xmm10, XMMWORD PTR [_2il0floatpacket_36]
        vpor      xmm12, xmm11, xmm15
        vmovd     xmm6, r11d
        vmovd     xmm4, edx
        vpshufd   xmm9, xmm6, 0
        vaddps    xmm8, xmm12, xmm10
        vpshufd   xmm1, xmm4, 0
        vsubps    xmm3, xmm8, xmm10
        vpand     xmm11, xmm1, xmm5
        vpsrld    xmm5, xmm5, 18
        vpslld    xmm15, xmm11, 5
        vsubps    xmm2, xmm12, xmm3
        vmovd     xmm3, eax
        vpxor     xmm12, xmm0, xmm9
        vpshufd   xmm6, xmm3, 0
        vpor      xmm10, xmm15, xmm12
        vpxor     xmm3, xmm0, xmm6
        vmovd     xmm0, ecx
        vpshufd   xmm11, xmm0, 0
        vsubps    xmm1, xmm10, xmm12
        vpand     xmm7, xmm11, xmm7
        mov       r8d, 255
        vpslld    xmm15, xmm7, 14
        vpor      xmm10, xmm15, xmm5
        vpor      xmm12, xmm10, xmm3
        vmovups   xmm11, XMMWORD PTR [_2il0floatpacket_39]
        vsubps    xmm6, xmm12, xmm3
        vaddps    xmm0, xmm2, xmm6
        vsubps    xmm9, xmm2, xmm0
        vandps    xmm2, xmm0, XMMWORD PTR [_2il0floatpacket_40]
        vmulps    xmm10, xmm11, xmm2
        vsubps    xmm7, xmm0, xmm2
        vaddps    xmm4, xmm6, xmm9
        vmulps    xmm3, xmm11, xmm7
        vaddps    xmm5, xmm1, xmm4
        vmovups   xmm1, XMMWORD PTR [_2il0floatpacket_38]
        vmulps    xmm15, xmm1, xmm7
        vmulps    xmm12, xmm5, XMMWORD PTR [_2il0floatpacket_37]
        vmulps    xmm4, xmm1, xmm2
        vaddps    xmm6, xmm15, xmm10
        vaddps    xmm9, xmm12, xmm3
        vmovups   xmm2, XMMWORD PTR [_2il0floatpacket_42]
        vaddps    xmm0, xmm6, xmm9
        vandps    xmm1, xmm14, XMMWORD PTR [_2il0floatpacket_41]
        vaddps    xmm11, xmm4, xmm0
        vcmpleps  xmm5, xmm1, xmm2
        vcmpgtps  xmm10, xmm1, xmm2
        vsubps    xmm4, xmm4, xmm11
        vandps    xmm7, xmm5, xmm14
        vmovd     xmm5, r8d
        vandps    xmm15, xmm10, xmm11
        vaddps    xmm12, xmm0, xmm4
        vpshufd   xmm11, xmm5, 0
        vorps     xmm3, xmm7, xmm15
        vpand     xmm8, xmm8, xmm11
        vandps    xmm10, xmm10, xmm12
        vpslld    xmm8, xmm8, 4
        vmovd     edx, xmm8
        vmulps    xmm6, xmm3, xmm3
        vpextrd   eax, xmm8, 1
        vpextrd   r11d, xmm8, 2
        vpextrd   ecx, xmm8, 3
        vmovd     xmm5, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+r9+rdx]
        vmovd     xmm11, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+r9+rax]
        vmovd     xmm7, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+r9+r11]
        vmovd     xmm15, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+r9+rcx]
        vpunpcklqdq xmm12, xmm5, xmm11
        vpunpcklqdq xmm9, xmm7, xmm15
        vmovd     xmm0, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+4+r9+rdx]
        vmovd     xmm1, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+4+r9+rax]
        vmovd     xmm15, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+12+r9+rax]
        vshufps   xmm9, xmm12, xmm9, 136
        vpunpcklqdq xmm8, xmm0, xmm1
        vmovd     xmm2, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+4+r9+r11]
        vmovd     xmm12, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+12+r9+r11]
        vmovd     xmm4, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+4+r9+rcx]
        vmovd     xmm0, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+12+r9+rcx]
        vmovd     xmm11, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+12+r9+rdx]
        vpunpcklqdq xmm5, xmm2, xmm4
        vpunpcklqdq xmm1, xmm11, xmm15
        vpunpcklqdq xmm2, xmm12, xmm0
        vshufps   xmm15, xmm1, xmm2, 136
        vmulps    xmm4, xmm3, xmm15
        vmulps    xmm12, xmm3, xmm9
        vshufps   xmm7, xmm8, xmm5, 136
        vaddps    xmm11, xmm7, xmm4
        vsubps    xmm5, xmm7, xmm11
        vaddps    xmm8, xmm12, xmm11
        vaddps    xmm1, xmm4, xmm5
        vmulps    xmm4, xmm6, XMMWORD PTR [__svml_ssin_ha_data_internal+4352]
        vsubps    xmm0, xmm11, xmm8
        vaddps    xmm11, xmm4, XMMWORD PTR [__svml_ssin_ha_data_internal+4288]
        vaddps    xmm2, xmm12, xmm0
        vmulps    xmm12, xmm6, xmm11
        vaddps    xmm5, xmm1, xmm2
        vmulps    xmm11, xmm3, xmm12
        vaddps    xmm12, xmm9, xmm15
        vmulps    xmm9, xmm6, XMMWORD PTR [__svml_ssin_ha_data_internal+4480]
        vmulps    xmm3, xmm3, xmm7
        vaddps    xmm15, xmm9, XMMWORD PTR [__svml_ssin_ha_data_internal+4416]
        vmulps    xmm15, xmm6, xmm15
        vmovd     xmm6, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+8+r9+rdx]
        vmovd     xmm0, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+8+r9+rax]
        vmovd     xmm1, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+8+r9+r11]
        vmovd     xmm2, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+8+r9+rcx]
        vpunpcklqdq xmm4, xmm6, xmm0
        vpunpcklqdq xmm6, xmm1, xmm2
        vsubps    xmm1, xmm12, xmm3
        vmulps    xmm7, xmm7, xmm15
        vmulps    xmm10, xmm10, xmm1
        vmulps    xmm11, xmm11, xmm1
        vshufps   xmm9, xmm4, xmm6, 136
        vaddps    xmm3, xmm5, xmm11
        vaddps    xmm0, xmm9, xmm10
        vaddps    xmm2, xmm0, xmm7
        vmovups   xmm0, XMMWORD PTR [32+rsp]
        vaddps    xmm4, xmm2, xmm3
        vaddps    xmm5, xmm8, xmm4
        vblendvps xmm0, xmm0, xmm5, xmm13
        jmp       _B2_2
        ALIGN     16

_B2_13::

__svml_sinf4_ha_e9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_sinf4_ha_e9_B1_B4:
	DD	1603329
	DD	2937975
	DD	1271919
	DD	1407078
	DD	1345629
	DD	956500
	DD	1091659
	DD	1030210
	DD	903225
	DD	841776
	DD	1239079
	DD	1177627
	DD	3080459

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B2_1
	DD	imagerel _B2_7
	DD	imagerel _unwind___svml_sinf4_ha_e9_B1_B4

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_sinf4_ha_e9_B7_B11:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B2_1
	DD	imagerel _B2_7
	DD	imagerel _unwind___svml_sinf4_ha_e9_B1_B4

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B2_7
	DD	imagerel _B2_12
	DD	imagerel _unwind___svml_sinf4_ha_e9_B7_B11

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_sinf4_ha_e9_B12_B12:
	DD	33
	DD	imagerel _B2_1
	DD	imagerel _B2_7
	DD	imagerel _unwind___svml_sinf4_ha_e9_B1_B4

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B2_12
	DD	imagerel _B2_13
	DD	imagerel _unwind___svml_sinf4_ha_e9_B12_B12

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST2:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_sinf8_ha_e9

__svml_sinf8_ha_e9	PROC

_B3_1::

        DB        243
        DB        15
        DB        30
        DB        250
L57::

        push      rdi
        push      r15
        push      rbp
        sub       rsp, 544
        xor       ebp, ebp
        vmovups   XMMWORD PTR [368+rsp], xmm15
        vmovups   XMMWORD PTR [384+rsp], xmm14
        vmovups   XMMWORD PTR [400+rsp], xmm13
        vmovups   XMMWORD PTR [416+rsp], xmm12
        vmovups   XMMWORD PTR [432+rsp], xmm11
        vmovups   XMMWORD PTR [512+rsp], xmm10
        vmovups   XMMWORD PTR [496+rsp], xmm9
        vmovups   XMMWORD PTR [448+rsp], xmm8
        vmovups   XMMWORD PTR [464+rsp], xmm7
        vmovups   XMMWORD PTR [480+rsp], xmm6
        mov       QWORD PTR [528+rsp], r13
        lea       r13, QWORD PTR [271+rsp]
        vmovups   ymm4, YMMWORD PTR [rcx]
        and       r13, -64
        vmovups   ymm8, YMMWORD PTR [__svml_ssin_ha_data_internal+4096]
        vmovups   ymm10, YMMWORD PTR [__svml_ssin_ha_data_internal+4544]
        vmovupd   ymm15, YMMWORD PTR [__svml_ssin_ha_data_internal+5120]
        vandps    ymm9, ymm4, ymm8
        mov       QWORD PTR [536+rsp], r13
        vmulps    ymm12, ymm9, YMMWORD PTR [__svml_ssin_ha_data_internal+5056]
        vcvtps2pd ymm7, xmm9
        vextractf128 xmm0, ymm9, 1
        vaddps    ymm1, ymm10, ymm12
        vcvtps2pd ymm6, xmm0
        vpslld    xmm5, xmm1, 31
        vsubps    ymm11, ymm1, ymm10
        vextractf128 xmm3, ymm1, 1
        vpslld    xmm2, xmm3, 31
        vextractf128 xmm13, ymm11, 1
        vcvtps2pd ymm12, xmm11
        vmovupd   ymm11, YMMWORD PTR [__svml_ssin_ha_data_internal+5440]
        vmulpd    ymm14, ymm12, ymm15
        vsubpd    ymm1, ymm7, ymm14
        vinsertf128 ymm5, ymm5, xmm2, 1
        vcvtps2pd ymm2, xmm13
        vmovupd   ymm13, YMMWORD PTR [__svml_ssin_ha_data_internal+5376]
        vmulpd    ymm0, ymm2, ymm15
        vsubpd    ymm7, ymm6, ymm0
        vmovupd   ymm6, YMMWORD PTR [__svml_ssin_ha_data_internal+5184]
        vmulpd    ymm3, ymm12, ymm6
        vmulpd    ymm10, ymm2, ymm6
        vsubpd    ymm3, ymm1, ymm3
        vsubpd    ymm1, ymm7, ymm10
        vmovupd   ymm7, YMMWORD PTR [__svml_ssin_ha_data_internal+5312]
        vmulpd    ymm12, ymm3, ymm3
        vmulpd    ymm2, ymm1, ymm1
        vandnps   ymm0, ymm8, ymm4
        vmulpd    ymm8, ymm11, ymm12
        vmulpd    ymm14, ymm11, ymm2
        vaddpd    ymm15, ymm13, ymm8
        vaddpd    ymm6, ymm13, ymm14
        vmovupd   ymm14, YMMWORD PTR [__svml_ssin_ha_data_internal+5248]
        vmulpd    ymm15, ymm12, ymm15
        vmulpd    ymm8, ymm2, ymm6
        vaddpd    ymm10, ymm7, ymm15
        vaddpd    ymm13, ymm7, ymm8
        vpxor     xmm8, xmm8, xmm8
        vmulpd    ymm11, ymm12, ymm10
        vmulpd    ymm13, ymm2, ymm13
        vaddpd    ymm6, ymm14, ymm11
        vaddpd    ymm7, ymm14, ymm13
        vmulpd    ymm12, ymm12, ymm6
        vmulpd    ymm6, ymm2, ymm7
        vmulpd    ymm2, ymm3, ymm12
        vaddpd    ymm12, ymm3, ymm2
        vmulpd    ymm3, ymm1, ymm6
        vaddpd    ymm1, ymm1, ymm3
        vxorps    ymm2, ymm5, ymm0
        vcvtpd2ps xmm5, ymm12
        vcvtpd2ps xmm0, ymm1
        vinsertf128 ymm12, ymm5, xmm0, 1
        vxorps    ymm0, ymm12, ymm2
        vcmpnle_uqps ymm12, ymm9, YMMWORD PTR [__svml_ssin_ha_data_internal+4160]
        vextractf128 xmm3, ymm12, 1
        vpackssdw xmm6, xmm12, xmm3
        vpacksswb xmm7, xmm6, xmm8
        vpmovmskb eax, xmm7
        test      al, al
        jne       _B3_12

_B3_2::

        test      bpl, bpl
        jne       _B3_4

_B3_3::

        vmovups   xmm6, XMMWORD PTR [480+rsp]
        vmovups   xmm7, XMMWORD PTR [464+rsp]
        vmovups   xmm8, XMMWORD PTR [448+rsp]
        vmovups   xmm9, XMMWORD PTR [496+rsp]
        vmovups   xmm10, XMMWORD PTR [512+rsp]
        vmovups   xmm11, XMMWORD PTR [432+rsp]
        vmovups   xmm12, XMMWORD PTR [416+rsp]
        vmovups   xmm13, XMMWORD PTR [400+rsp]
        vmovups   xmm14, XMMWORD PTR [384+rsp]
        vmovups   xmm15, XMMWORD PTR [368+rsp]
        mov       r13, QWORD PTR [528+rsp]
        add       rsp, 544
        pop       rbp
        pop       r15
        pop       rdi
        ret

_B3_4::

        vmovups   YMMWORD PTR [r13], ymm4
        vmovups   YMMWORD PTR [64+r13], ymm0
        test      ebp, ebp
        je        _B3_3

_B3_7::

        xor       edi, edi

_B3_8::

        bt        ebp, edi
        jc        _B3_11

_B3_9::

        inc       edi
        cmp       edi, 8
        jl        _B3_8

_B3_10::

        vmovups   ymm0, YMMWORD PTR [64+r13]
        jmp       _B3_3

_B3_11::

        vzeroupper
        lea       rcx, QWORD PTR [r13+rdi*4]
        lea       rdx, QWORD PTR [64+r13+rdi*4]

        call      __svml_ssin_ha_cout_rare_internal
        jmp       _B3_9

_B3_12::

        vmovups   ymm5, YMMWORD PTR [__svml_ssin_ha_data_internal+4224]
        mov       edx, 2139095040
        vmovups   YMMWORD PTR [r13], ymm4
        vmovups   YMMWORD PTR [64+r13], ymm0
        vmovups   YMMWORD PTR [32+r13], ymm12
        vmovd     xmm7, edx
        vpshufd   xmm13, xmm7, 0
        vandps    ymm2, ymm5, ymm9
        lea       rax, QWORD PTR [__ImageBase]
        vcmpeqps  ymm1, ymm2, ymm5
        vpand     xmm6, xmm13, xmm4
        vextractf128 xmm15, ymm4, 1
        vpsrld    xmm10, xmm6, 23
        vpslld    xmm9, xmm10, 1
        vpaddd    xmm12, xmm9, xmm10
        vpand     xmm0, xmm13, xmm15
        vpsrld    xmm2, xmm0, 23
        vpslld    xmm14, xmm2, 1
        vpaddd    xmm5, xmm14, xmm2
        vpslld    xmm0, xmm5, 2
        vmovd     r10d, xmm0
        vmovups   XMMWORD PTR [32+rsp], xmm4
        vmovups   XMMWORD PTR [48+rsp], xmm15
        vpextrd   r11d, xmm0, 1
        vpextrd   r15d, xmm0, 2
        vpextrd   edx, xmm0, 3
        vmovd     xmm9, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+rax+r10]
        vmovd     xmm10, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+rax+r11]
        vmovd     xmm14, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+rax+rdx]
        vpunpcklqdq xmm2, xmm9, xmm10
        vmovd     xmm0, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+4+rax+r10]
        vmovd     xmm9, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+4+rax+r11]
        vextractf128 xmm3, ymm1, 1
        vpackssdw xmm11, xmm1, xmm3
        vpslld    xmm1, xmm12, 2
        vmovd     ecx, xmm1
        vpacksswb xmm8, xmm11, xmm8
        vmovd     xmm12, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+rax+r15]
        vpextrd   edi, xmm1, 1
        vpextrd   r8d, xmm1, 2
        vpextrd   r9d, xmm1, 3
        vpmovmskb ebp, xmm8
        vmovd     xmm3, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+rax+rcx]
        vmovd     xmm11, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+rax+rdi]
        vmovd     xmm8, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+rax+r8]
        vmovd     xmm7, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+rax+r9]
        vpunpcklqdq xmm6, xmm3, xmm11
        vpunpcklqdq xmm13, xmm8, xmm7
        vmovd     xmm7, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+4+rax+r9]
        vpunpcklqdq xmm1, xmm12, xmm14
        vmovd     xmm3, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+4+rax+rcx]
        vmovd     xmm11, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+4+rax+rdi]
        vmovd     xmm8, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+4+rax+r8]
        vmovd     xmm12, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+4+rax+r15]
        vmovd     xmm14, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+4+rax+rdx]
        vshufps   xmm5, xmm6, xmm13, 136
        vshufps   xmm2, xmm2, xmm1, 136
        vpunpcklqdq xmm6, xmm3, xmm11
        vpunpcklqdq xmm13, xmm8, xmm7
        vpunpcklqdq xmm1, xmm0, xmm9
        vpunpcklqdq xmm3, xmm12, xmm14
        vshufps   xmm10, xmm6, xmm13, 136
        vshufps   xmm13, xmm1, xmm3, 136
        vmovd     xmm3, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+8+rax+r15]
        mov       r15d, 8388607
        vmovd     xmm14, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+8+rax+r11]
        mov       r11d, 8388608
        vmovd     xmm11, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+8+rax+rcx]
        mov       ecx, 679477248
        vmovd     xmm8, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+8+rax+rdi]
        mov       edi, 255
        vmovd     xmm7, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+8+rax+r8]
        mov       r8d, 1065353216
        vmovd     xmm6, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+8+rax+r9]
        mov       r9d, -2147483648
        vmovd     xmm12, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+8+rax+r10]
        mov       r10d, 65535
        vpunpcklqdq xmm0, xmm11, xmm8
        vpunpcklqdq xmm9, xmm7, xmm6
        vmovd     xmm6, r15d
        vshufps   xmm1, xmm0, xmm9, 136
        vmovd     xmm0, r11d
        vpshufd   xmm9, xmm6, 0
        mov       r15d, 262143
        vpunpcklqdq xmm8, xmm12, xmm14
        vpand     xmm4, xmm9, xmm4
        vpshufd   xmm12, xmm0, 0
        vpand     xmm15, xmm9, xmm15
        vmovd     xmm11, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+8+rax+rdx]
        vpaddd    xmm14, xmm4, xmm12
        vmovd     xmm4, r10d
        vpaddd    xmm6, xmm15, xmm12
        vpunpcklqdq xmm7, xmm3, xmm11
        vpsrld    xmm11, xmm10, 16
        vpshufd   xmm15, xmm4, 0
        mov       r11d, 872415232
        vshufps   xmm3, xmm8, xmm7, 136
        vpand     xmm7, xmm5, xmm15
        vpand     xmm9, xmm14, xmm15
        vpand     xmm12, xmm2, xmm15
        vmovups   XMMWORD PTR [64+rsp], xmm5
        vpand     xmm5, xmm10, xmm15
        vmovups   XMMWORD PTR [80+rsp], xmm2
        vpsrld    xmm2, xmm1, 16
        vpsrld    xmm10, xmm14, 16
        vpand     xmm14, xmm6, xmm15
        vmovdqu   XMMWORD PTR [96+rsp], xmm7
        vpand     xmm1, xmm1, xmm15
        vpmulld   xmm7, xmm9, xmm7
        vpsrld    xmm8, xmm13, 16
        vmovdqu   XMMWORD PTR [176+rsp], xmm7
        vpand     xmm0, xmm13, xmm15
        vpmulld   xmm7, xmm10, xmm2
        vpsrld    xmm13, xmm6, 16
        vpmulld   xmm2, xmm9, xmm2
        vpsrld    xmm4, xmm3, 16
        vpmulld   xmm6, xmm14, xmm12
        vpsrld    xmm2, xmm2, 16
        vmovdqu   XMMWORD PTR [192+rsp], xmm6
        vpaddd    xmm7, xmm7, xmm2
        vpmulld   xmm6, xmm9, xmm5
        vpand     xmm3, xmm3, xmm15
        vpmulld   xmm1, xmm10, xmm1
        mov       r10d, 511
        vmovdqu   XMMWORD PTR [112+rsp], xmm12
        vmovdqu   XMMWORD PTR [144+rsp], xmm8
        vmovdqu   XMMWORD PTR [160+rsp], xmm0
        vpmulld   xmm12, xmm14, xmm8
        vpmulld   xmm8, xmm14, xmm0
        vpsrld    xmm0, xmm1, 16
        vpand     xmm1, xmm6, xmm15
        vpaddd    xmm1, xmm1, xmm7
        vpaddd    xmm7, xmm0, xmm1
        vpmulld   xmm0, xmm13, xmm3
        vpmulld   xmm3, xmm13, xmm4
        vpsrld    xmm2, xmm0, 16
        vpmulld   xmm4, xmm14, xmm4
        vpand     xmm0, xmm8, xmm15
        vpsrld    xmm4, xmm4, 16
        vpsrld    xmm8, xmm8, 16
        vpaddd    xmm1, xmm3, xmm4
        vpaddd    xmm3, xmm0, xmm1
        vpsrld    xmm1, xmm7, 16
        vmovdqu   XMMWORD PTR [128+rsp], xmm11
        vpaddd    xmm4, xmm2, xmm3
        vpmulld   xmm11, xmm9, xmm11
        vpsrld    xmm3, xmm6, 16
        vpmulld   xmm5, xmm10, xmm5
        vpand     xmm2, xmm11, xmm15
        vpaddd    xmm5, xmm5, xmm3
        vpsrld    xmm11, xmm11, 16
        vpmulld   xmm0, xmm13, XMMWORD PTR [160+rsp]
        vpaddd    xmm2, xmm2, xmm5
        vpaddd    xmm6, xmm1, xmm2
        vpand     xmm2, xmm12, xmm15
        vpaddd    xmm5, xmm0, xmm8
        vpsrld    xmm1, xmm4, 16
        vpaddd    xmm3, xmm2, xmm5
        vpsrld    xmm12, xmm12, 16
        vpmulld   xmm0, xmm10, XMMWORD PTR [128+rsp]
        vpaddd    xmm8, xmm1, xmm3
        vmovdqu   xmm1, XMMWORD PTR [176+rsp]
        vpaddd    xmm5, xmm0, xmm11
        vpand     xmm2, xmm1, xmm15
        vpsrld    xmm3, xmm6, 16
        vpaddd    xmm2, xmm2, xmm5
        vpsrld    xmm11, xmm8, 16
        vpmulld   xmm5, xmm13, XMMWORD PTR [144+rsp]
        vpaddd    xmm2, xmm3, xmm2
        vmovdqu   xmm3, XMMWORD PTR [192+rsp]
        vpaddd    xmm5, xmm5, xmm12
        vpand     xmm0, xmm3, xmm15
        vpsrld    xmm1, xmm1, 16
        vmovups   xmm12, XMMWORD PTR [64+rsp]
        vpaddd    xmm0, xmm0, xmm5
        vpaddd    xmm5, xmm11, xmm0
        vpsrld    xmm0, xmm12, 16
        vpmulld   xmm10, xmm10, XMMWORD PTR [96+rsp]
        vpsrld    xmm11, xmm2, 16
        vpmulld   xmm9, xmm9, xmm0
        vpaddd    xmm0, xmm10, xmm1
        vpand     xmm9, xmm9, xmm15
        vpand     xmm2, xmm2, xmm15
        vpaddd    xmm9, xmm9, xmm0
        vpsrld    xmm0, xmm5, 16
        vpaddd    xmm11, xmm11, xmm9
        vpand     xmm5, xmm5, xmm15
        vmovups   xmm12, XMMWORD PTR [80+rsp]
        vpslld    xmm10, xmm11, 16
        vpaddd    xmm10, xmm10, xmm2
        vpsrld    xmm2, xmm12, 16
        vpmulld   xmm13, xmm13, XMMWORD PTR [112+rsp]
        vpand     xmm7, xmm7, xmm15
        vpmulld   xmm14, xmm14, xmm2
        vpsrld    xmm2, xmm3, 16
        vpand     xmm1, xmm14, xmm15
        vpaddd    xmm3, xmm13, xmm2
        vpaddd    xmm11, xmm1, xmm3
        vpand     xmm15, xmm4, xmm15
        vpaddd    xmm13, xmm0, xmm11
        vmovd     xmm4, r9d
        vpslld    xmm0, xmm13, 16
        vpslld    xmm2, xmm8, 16
        vpshufd   xmm8, xmm4, 0
        vmovd     xmm12, r8d
        vpshufd   xmm3, xmm12, 0
        vpaddd    xmm0, xmm0, xmm5
        vpand     xmm5, xmm8, XMMWORD PTR [32+rsp]
        vpaddd    xmm15, xmm2, xmm15
        vmovups   ymm13, YMMWORD PTR [_2il0floatpacket_26]
        vpsrld    xmm14, xmm10, 9
        vpand     xmm2, xmm8, XMMWORD PTR [48+rsp]
        vpxor     xmm4, xmm5, xmm3
        vpslld    xmm6, xmm6, 16
        vpsrld    xmm11, xmm0, 9
        vpxor     xmm8, xmm2, xmm3
        vpor      xmm1, xmm14, xmm4
        vpaddd    xmm9, xmm6, xmm7
        vpor      xmm7, xmm11, xmm8
        vmovd     xmm11, r15d
        vmovd     xmm4, edi
        vinsertf128 ymm12, ymm1, xmm7, 1
        vmovd     xmm1, ecx
        vaddps    ymm6, ymm12, ymm13
        vpshufd   xmm3, xmm1, 0
        vpshufd   xmm7, xmm4, 0
        vpxor     xmm4, xmm5, xmm3
        vsubps    ymm14, ymm6, ymm13
        vsubps    ymm13, ymm12, ymm14
        vpxor     xmm12, xmm2, xmm3
        vpshufd   xmm14, xmm11, 0
        vpand     xmm1, xmm14, xmm9
        vpand     xmm14, xmm14, xmm15
        vpslld    xmm3, xmm1, 5
        vpslld    xmm1, xmm14, 5
        vpor      xmm11, xmm3, xmm4
        vpor      xmm3, xmm1, xmm12
        vpsrld    xmm9, xmm9, 18
        vpsrld    xmm15, xmm15, 18
        vinsertf128 ymm11, ymm11, xmm3, 1
        vinsertf128 ymm12, ymm4, xmm12, 1
        vmovd     xmm4, r11d
        vsubps    ymm3, ymm11, ymm12
        vpshufd   xmm11, xmm4, 0
        vpxor     xmm1, xmm5, xmm11
        vpxor     xmm5, xmm2, xmm11
        vmovd     xmm2, r10d
        vpshufd   xmm2, xmm2, 0
        vpand     xmm10, xmm2, xmm10
        vpand     xmm2, xmm2, xmm0
        vpslld    xmm4, xmm10, 14
        vpslld    xmm0, xmm2, 14
        vpor      xmm4, xmm4, xmm9
        vpor      xmm11, xmm4, xmm1
        vpor      xmm4, xmm0, xmm15
        vpor      xmm2, xmm4, xmm5
        vmovups   ymm9, YMMWORD PTR [_2il0floatpacket_44]
        vinsertf128 ymm0, ymm11, xmm2, 1
        vinsertf128 ymm5, ymm1, xmm5, 1
        vsubps    ymm4, ymm0, ymm5
        vaddps    ymm5, ymm13, ymm4
        vsubps    ymm2, ymm13, ymm5
        vaddps    ymm1, ymm4, ymm2
        vaddps    ymm0, ymm3, ymm1
        vmovups   ymm3, YMMWORD PTR [_2il0floatpacket_43]
        vmulps    ymm14, ymm0, YMMWORD PTR [_2il0floatpacket_31]
        vandps    ymm11, ymm5, YMMWORD PTR [_2il0floatpacket_45]
        vsubps    ymm10, ymm5, ymm11
        vmulps    ymm13, ymm9, ymm11
        vmulps    ymm4, ymm3, ymm11
        vmulps    ymm12, ymm3, ymm10
        vmulps    ymm15, ymm9, ymm10
        vmovups   ymm11, YMMWORD PTR [_2il0floatpacket_34]
        vaddps    ymm2, ymm12, ymm13
        vaddps    ymm1, ymm14, ymm15
        vaddps    ymm5, ymm2, ymm1
        vaddps    ymm9, ymm4, ymm5
        vsubps    ymm4, ymm4, ymm9
        vaddps    ymm14, ymm5, ymm4
        vmovups   ymm4, YMMWORD PTR [r13]
        vandps    ymm3, ymm4, YMMWORD PTR [_2il0floatpacket_33]
        vcmpgt_oqps ymm13, ymm3, ymm11
        vcmple_oqps ymm0, ymm3, ymm11
        vandps    ymm12, ymm13, ymm9
        vandps    ymm10, ymm0, ymm4
        vorps     ymm2, ymm10, ymm12
        vandps    ymm1, ymm13, ymm14
        vmulps    ymm5, ymm2, ymm2
        vextractf128 xmm8, ymm6, 1
        vpand     xmm6, xmm6, xmm7
        vpslld    xmm3, xmm6, 4
        vmovd     r15d, xmm3
        vpand     xmm7, xmm8, xmm7
        vpslld    xmm9, xmm7, 4
        vmovd     r8d, xmm9
        vmovd     xmm8, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+rax+r15]
        vpextrd   r11d, xmm3, 1
        vpextrd   r10d, xmm3, 2
        vpextrd   r9d, xmm3, 3
        vmovd     xmm3, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+rax+r11]
        vpextrd   edi, xmm9, 1
        vpextrd   ecx, xmm9, 2
        vpextrd   edx, xmm9, 3
        vpunpcklqdq xmm0, xmm8, xmm3
        vmovd     xmm11, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+rax+r10]
        vmovd     xmm8, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+rax+r9]
        vmovd     xmm10, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+rax+r8]
        vmovd     xmm12, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+rax+rdi]
        vmovd     xmm13, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+rax+rcx]
        vmovd     xmm14, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+rax+rdx]
        vpunpcklqdq xmm6, xmm11, xmm8
        vpunpcklqdq xmm15, xmm10, xmm12
        vpunpcklqdq xmm3, xmm13, xmm14
        vshufps   xmm7, xmm0, xmm6, 136
        vshufps   xmm11, xmm15, xmm3, 136
        vmovd     xmm0, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+4+rax+r15]
        vmovd     xmm6, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+4+rax+r11]
        vmovd     xmm8, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+4+rax+r10]
        vmovd     xmm9, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+4+rax+r9]
        vmovd     xmm14, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+4+rax+r8]
        vmovd     xmm15, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+4+rax+rdi]
        vpunpcklqdq xmm10, xmm0, xmm6
        vpunpcklqdq xmm12, xmm8, xmm9
        vpunpcklqdq xmm15, xmm14, xmm15
        vmovd     xmm14, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+4+rax+rdx]
        vshufps   xmm13, xmm10, xmm12, 136
        vmovd     xmm8, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+12+rax+r11]
        vmovd     xmm9, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+12+rax+r10]
        vmovd     xmm10, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+12+rax+r9]
        vinsertf128 ymm3, ymm7, xmm11, 1
        vmovd     xmm11, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+4+rax+rcx]
        vpunpcklqdq xmm0, xmm11, xmm14
        vmovd     xmm7, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+12+rax+r15]
        vshufps   xmm6, xmm15, xmm0, 136
        vpunpcklqdq xmm12, xmm7, xmm8
        vpunpcklqdq xmm0, xmm9, xmm10
        vmovd     xmm7, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+12+rax+r8]
        vmovd     xmm8, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+12+rax+rdi]
        vpunpcklqdq xmm7, xmm7, xmm8
        mov       eax, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+12+rax+rcx]
        vinsertf128 ymm11, ymm13, xmm6, 1
        vshufps   xmm6, xmm12, xmm0, 136
        vmovups   ymm0, YMMWORD PTR [64+r13]
        vmovups   ymm12, YMMWORD PTR [32+r13]

_B3_15::

        vmovd     xmm13, eax
        lea       rax, QWORD PTR [__ImageBase]
        vmovd     xmm14, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+12+rax+rdx]
        vpunpcklqdq xmm15, xmm13, xmm14
        vshufps   xmm10, xmm7, xmm15, 136
        vinsertf128 ymm9, ymm6, xmm10, 1
        vmulps    ymm8, ymm2, ymm9
        vmulps    ymm13, ymm2, ymm3
        vaddps    ymm6, ymm11, ymm8
        vsubps    ymm7, ymm11, ymm6
        vaddps    ymm10, ymm13, ymm6
        vaddps    ymm15, ymm8, ymm7
        vsubps    ymm14, ymm6, ymm10
        vmulps    ymm7, ymm5, YMMWORD PTR [__svml_ssin_ha_data_internal+4352]
        vaddps    ymm8, ymm13, ymm14
        vmovd     xmm14, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+8+rax+r9]
        vaddps    ymm6, ymm7, YMMWORD PTR [__svml_ssin_ha_data_internal+4288]
        vaddps    ymm8, ymm15, ymm8
        vmulps    ymm13, ymm5, ymm6
        vaddps    ymm6, ymm3, ymm9
        vmulps    ymm3, ymm5, YMMWORD PTR [__svml_ssin_ha_data_internal+4480]
        vmulps    ymm7, ymm2, ymm13
        vmulps    ymm2, ymm2, ymm11
        vmovd     xmm13, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+8+rax+r10]
        vaddps    ymm9, ymm3, YMMWORD PTR [__svml_ssin_ha_data_internal+4416]
        vmovd     xmm3, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+8+rax+r15]
        vsubps    ymm2, ymm6, ymm2
        vmulps    ymm5, ymm5, ymm9
        vmovd     xmm9, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+8+rax+r11]
        vpunpcklqdq xmm15, xmm3, xmm9
        vpunpcklqdq xmm3, xmm13, xmm14
        vshufps   xmm3, xmm15, xmm3, 136
        vmulps    ymm1, ymm1, ymm2
        vmovd     xmm9, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+8+rax+r8]
        vmovd     xmm13, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+8+rax+rdi]
        vmovd     xmm14, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+8+rax+rcx]
        vmovd     xmm15, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+8+rax+rdx]
        vpunpcklqdq xmm9, xmm9, xmm13
        vpunpcklqdq xmm13, xmm14, xmm15
        vshufps   xmm14, xmm9, xmm13, 136
        vmulps    ymm11, ymm11, ymm5
        vmulps    ymm7, ymm7, ymm2
        vinsertf128 ymm3, ymm3, xmm14, 1
        vaddps    ymm1, ymm3, ymm1
        vaddps    ymm3, ymm8, ymm7
        vaddps    ymm1, ymm1, ymm11
        vaddps    ymm5, ymm1, ymm3
        vaddps    ymm6, ymm10, ymm5
        vblendvps ymm0, ymm0, ymm6, ymm12
        jmp       _B3_2
        ALIGN     16

_B3_13::

__svml_sinf8_ha_e9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_sinf8_ha_e9_B1_B15:
	DD	1798913
	DD	4379763
	DD	1992811
	DD	1931362
	DD	1869913
	DD	2070608
	DD	2140231
	DD	1816638
	DD	1755189
	DD	1693740
	DD	1632291
	DD	1570842
	DD	4456719
	DD	4027011080
	DD	28677

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B3_1
	DD	imagerel _B3_13
	DD	imagerel _unwind___svml_sinf8_ha_e9_B1_B15

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST3:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_sinf4_ha_l9

__svml_sinf4_ha_l9	PROC

_B4_1::

        DB        243
        DB        15
        DB        30
        DB        250
L108::

        sub       rsp, 376
        lea       r9, QWORD PTR [__ImageBase]
        vmovups   XMMWORD PTR [192+rsp], xmm15
        xor       r10d, r10d
        vmovups   XMMWORD PTR [208+rsp], xmm14
        vmovups   XMMWORD PTR [224+rsp], xmm13
        vmovups   XMMWORD PTR [240+rsp], xmm12
        vmovups   XMMWORD PTR [256+rsp], xmm11
        vmovups   XMMWORD PTR [272+rsp], xmm10
        vmovups   XMMWORD PTR [288+rsp], xmm9
        vmovups   XMMWORD PTR [304+rsp], xmm8
        vmovups   XMMWORD PTR [320+rsp], xmm7
        vmovups   XMMWORD PTR [336+rsp], xmm6
        mov       QWORD PTR [352+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovups   xmm14, XMMWORD PTR [rcx]
        and       r13, -64
        vmovups   xmm5, XMMWORD PTR [__svml_ssin_ha_data_internal+4096]
        vmovups   xmm0, XMMWORD PTR [__svml_ssin_ha_data_internal+5056]
        vandps    xmm1, xmm14, xmm5
        vmovups   xmm2, XMMWORD PTR [__svml_ssin_ha_data_internal+4544]
        vfmadd213ps xmm0, xmm1, xmm2
        vcmpnleps xmm13, xmm1, XMMWORD PTR [__svml_ssin_ha_data_internal+4160]
        vcvtps2pd ymm4, xmm1
        vsubps    xmm3, xmm0, xmm2
        vmovmskps eax, xmm13
        vpslld    xmm6, xmm0, 31
        vcvtps2pd ymm2, xmm3
        vandnps   xmm3, xmm5, xmm14
        vxorps    xmm8, xmm6, xmm3
        vmovupd   ymm0, YMMWORD PTR [__svml_ssin_ha_data_internal+5440]
        vfnmadd231pd ymm4, ymm2, YMMWORD PTR [__svml_ssin_ha_data_internal+5120]
        vfnmadd132pd ymm2, ymm4, YMMWORD PTR [__svml_ssin_ha_data_internal+5184]
        vmulpd    ymm4, ymm2, ymm2
        vfmadd213pd ymm0, ymm4, YMMWORD PTR [__svml_ssin_ha_data_internal+5376]
        vfmadd213pd ymm0, ymm4, YMMWORD PTR [__svml_ssin_ha_data_internal+5312]
        vfmadd213pd ymm0, ymm4, YMMWORD PTR [__svml_ssin_ha_data_internal+5248]
        vmulpd    ymm7, ymm4, ymm0
        vfmadd213pd ymm7, ymm2, ymm2
        vcvtpd2ps xmm5, ymm7
        mov       QWORD PTR [360+rsp], r13
        vxorps    xmm0, xmm5, xmm8
        test      eax, eax
        jne       _B4_12

_B4_2::

        test      r10d, r10d
        jne       _B4_4

_B4_3::

        vzeroupper
        vmovups   xmm6, XMMWORD PTR [336+rsp]
        vmovups   xmm7, XMMWORD PTR [320+rsp]
        vmovups   xmm8, XMMWORD PTR [304+rsp]
        vmovups   xmm9, XMMWORD PTR [288+rsp]
        vmovups   xmm10, XMMWORD PTR [272+rsp]
        vmovups   xmm11, XMMWORD PTR [256+rsp]
        vmovups   xmm12, XMMWORD PTR [240+rsp]
        vmovups   xmm13, XMMWORD PTR [224+rsp]
        vmovups   xmm14, XMMWORD PTR [208+rsp]
        vmovups   xmm15, XMMWORD PTR [192+rsp]
        mov       r13, QWORD PTR [352+rsp]
        add       rsp, 376
        ret

_B4_4::

        vmovups   XMMWORD PTR [r13], xmm14
        vmovups   XMMWORD PTR [64+r13], xmm0
        je        _B4_3

_B4_7::

        xor       eax, eax
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, eax
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, r10d

_B4_8::

        bt        esi, ebx
        jc        _B4_11

_B4_9::

        inc       ebx
        cmp       ebx, 4
        jl        _B4_8

_B4_10::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        vmovups   xmm0, XMMWORD PTR [64+r13]
        jmp       _B4_3

_B4_11::

        vzeroupper
        lea       rcx, QWORD PTR [r13+rbx*4]
        lea       rdx, QWORD PTR [64+r13+rbx*4]

        call      __svml_ssin_ha_cout_rare_internal
        jmp       _B4_9

_B4_12::

        vpand     xmm12, xmm14, XMMWORD PTR [_2il0floatpacket_46]
        vmovups   xmm6, XMMWORD PTR [__svml_ssin_ha_data_internal+4224]
        vpsrld    xmm3, xmm12, 23
        vandps    xmm10, xmm6, xmm1
        vpslld    xmm9, xmm3, 1
        vcmpeqps  xmm7, xmm10, xmm6
        vpaddd    xmm4, xmm9, xmm3
        vpslld    xmm8, xmm4, 2
        vmovd     r8d, xmm8
        vmovmskps r10d, xmm7
        vmovups   XMMWORD PTR [32+rsp], xmm0
        vmovd     xmm10, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+r9+r8]
        vpextrd   ecx, xmm8, 1
        vpextrd   r11d, xmm8, 2
        vpextrd   edx, xmm8, 3
        vmovd     xmm6, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+r9+rcx]
        vmovd     xmm7, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+r9+r11]
        vmovd     xmm5, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+r9+rdx]
        vpunpcklqdq xmm11, xmm10, xmm6
        vpunpcklqdq xmm12, xmm7, xmm5
        vmovd     xmm7, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+8+r9+rdx]
        vmovd     xmm2, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+8+r9+r8]
        vmovd     xmm8, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+8+r9+rcx]
        vmovd     xmm6, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+8+r9+r11]
        vmovd     xmm0, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+4+r9+r11]
        vshufps   xmm10, xmm11, xmm12, 136
        vmovd     xmm3, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+4+r9+rdx]
        vpand     xmm12, xmm14, XMMWORD PTR [_2il0floatpacket_47]
        vpunpcklqdq xmm5, xmm2, xmm8
        vpunpcklqdq xmm11, xmm6, xmm7
        vpunpcklqdq xmm1, xmm0, xmm3
        vpaddd    xmm3, xmm12, XMMWORD PTR [_2il0floatpacket_48]
        vmovd     xmm15, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+4+r9+r8]
        vmovd     xmm9, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+4+r9+rcx]
        vshufps   xmm11, xmm5, xmm11, 136
        vmovdqu   xmm12, XMMWORD PTR [_2il0floatpacket_49]
        vpsrld    xmm0, xmm11, 16
        vpunpcklqdq xmm4, xmm15, xmm9
        vpsrld    xmm9, xmm3, 16
        vpand     xmm5, xmm11, xmm12
        vpand     xmm6, xmm3, xmm12
        vshufps   xmm15, xmm4, xmm1, 136
        vpand     xmm7, xmm10, xmm12
        vpmulld   xmm11, xmm9, xmm5
        vpsrld    xmm4, xmm15, 16
        vpand     xmm2, xmm15, xmm12
        vpsrld    xmm15, xmm11, 16
        vpmulld   xmm11, xmm9, xmm0
        vpsrld    xmm10, xmm10, 16
        vpmulld   xmm0, xmm6, xmm0
        vpmulld   xmm1, xmm6, xmm2
        vpsrld    xmm0, xmm0, 16
        vpand     xmm5, xmm1, xmm12
        vpaddd    xmm11, xmm11, xmm0
        vpmulld   xmm3, xmm6, xmm4
        vpaddd    xmm5, xmm5, xmm11
        vpmulld   xmm2, xmm9, xmm2
        vpsrld    xmm1, xmm1, 16
        vpaddd    xmm11, xmm15, xmm5
        vpand     xmm5, xmm3, xmm12
        vpaddd    xmm0, xmm2, xmm1
        vpsrld    xmm15, xmm11, 16
        vpaddd    xmm5, xmm5, xmm0
        vpand     xmm11, xmm11, xmm12
        vpmulld   xmm8, xmm6, xmm7
        vpaddd    xmm5, xmm15, xmm5
        vpmulld   xmm4, xmm9, xmm4
        vpsrld    xmm15, xmm3, 16
        vpand     xmm1, xmm8, xmm12
        vpaddd    xmm0, xmm4, xmm15
        vpmulld   xmm10, xmm6, xmm10
        vpsrld    xmm2, xmm5, 16
        vpmulld   xmm9, xmm9, xmm7
        vpaddd    xmm3, xmm1, xmm0
        vpsrld    xmm8, xmm8, 16
        vpaddd    xmm0, xmm2, xmm3
        vpand     xmm6, xmm10, xmm12
        vpaddd    xmm10, xmm9, xmm8
        vpsrld    xmm15, xmm0, 16
        vpaddd    xmm7, xmm6, xmm10
        vpaddd    xmm15, xmm15, xmm7
        vpand     xmm0, xmm0, xmm12
        vpslld    xmm9, xmm15, 16
        vpslld    xmm5, xmm5, 16
        vpand     xmm2, xmm14, XMMWORD PTR [_2il0floatpacket_50]
        vpaddd    xmm8, xmm9, xmm0
        vpxor     xmm3, xmm2, XMMWORD PTR [_2il0floatpacket_51]
        vpsrld    xmm12, xmm8, 9
        vmovups   xmm4, XMMWORD PTR [_2il0floatpacket_36]
        vpor      xmm10, xmm12, xmm3
        vpxor     xmm12, xmm2, XMMWORD PTR [_2il0floatpacket_52]
        vpaddd    xmm1, xmm5, xmm11
        vpand     xmm7, xmm1, XMMWORD PTR [_2il0floatpacket_53]
        vpsrld    xmm1, xmm1, 18
        vpslld    xmm5, xmm7, 5
        vaddps    xmm0, xmm10, xmm4
        vpor      xmm11, xmm5, xmm12
        vsubps    xmm6, xmm0, xmm4
        vsubps    xmm3, xmm11, xmm12
        vsubps    xmm4, xmm10, xmm6
        vpxor     xmm6, xmm2, XMMWORD PTR [_2il0floatpacket_54]
        vpand     xmm2, xmm8, XMMWORD PTR [_2il0floatpacket_55]
        vpslld    xmm8, xmm2, 14
        vpor      xmm8, xmm8, xmm1
        vpor      xmm10, xmm8, xmm6
        vmovups   xmm15, XMMWORD PTR [_2il0floatpacket_37]
        vsubps    xmm7, xmm10, xmm6
        vmovaps   xmm12, xmm15
        vaddps    xmm9, xmm4, xmm7
        vmovups   xmm2, XMMWORD PTR [_2il0floatpacket_42]
        vsubps    xmm5, xmm4, xmm9
        vmulps    xmm4, xmm15, xmm9
        vaddps    xmm11, xmm7, xmm5
        vfmsub213ps xmm12, xmm9, xmm4
        vandps    xmm1, xmm14, XMMWORD PTR [_2il0floatpacket_41]
        vaddps    xmm7, xmm3, xmm11
        vfmadd132ps xmm9, xmm12, XMMWORD PTR [_2il0floatpacket_56]
        vcmpgtps  xmm6, xmm1, xmm2
        vcmpleps  xmm3, xmm1, xmm2
        vfmadd213ps xmm7, xmm15, xmm9
        vpand     xmm0, xmm0, XMMWORD PTR [_2il0floatpacket_57]
        vandps    xmm8, xmm3, xmm14
        vandps    xmm10, xmm6, xmm4
        vorps     xmm10, xmm8, xmm10
        vpslld    xmm8, xmm0, 4
        vmovd     edx, xmm8
        vandps    xmm6, xmm6, xmm7
        vmulps    xmm9, xmm10, xmm10
        vpextrd   eax, xmm8, 1
        vpextrd   ecx, xmm8, 2
        vpextrd   r8d, xmm8, 3
        vmovd     xmm7, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+r9+rdx]
        vmovd     xmm5, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+r9+rax]
        vmovd     xmm11, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+r9+rcx]
        vmovd     xmm12, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+r9+r8]
        vpunpcklqdq xmm15, xmm7, xmm5
        vpunpcklqdq xmm0, xmm11, xmm12
        vmovd     xmm4, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+4+r9+r8]
        vshufps   xmm15, xmm15, xmm0, 136
        vmovd     xmm0, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+12+r9+r8]
        vmovd     xmm1, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+4+r9+rdx]
        vmovd     xmm5, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+12+r9+rdx]
        vmovd     xmm2, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+4+r9+rax]
        vmovd     xmm11, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+12+r9+rax]
        vmovd     xmm3, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+4+r9+rcx]
        vmovd     xmm12, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+12+r9+rcx]
        vpunpcklqdq xmm8, xmm1, xmm2
        vpunpcklqdq xmm7, xmm3, xmm4
        vmovaps   xmm3, xmm10
        vpunpcklqdq xmm1, xmm5, xmm11
        vpunpcklqdq xmm2, xmm12, xmm0
        vshufps   xmm7, xmm8, xmm7, 136
        vmovaps   xmm8, xmm15
        vshufps   xmm0, xmm1, xmm2, 136
        vfmadd213ps xmm3, xmm0, xmm7
        vmovups   xmm12, XMMWORD PTR [__svml_ssin_ha_data_internal+4352]
        vmovd     xmm1, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+8+r9+rcx]
        vmovd     xmm2, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+8+r9+r8]
        vfmadd213ps xmm12, xmm9, XMMWORD PTR [__svml_ssin_ha_data_internal+4288]
        vfmadd213ps xmm8, xmm10, xmm3
        vsubps    xmm5, xmm7, xmm3
        vmulps    xmm4, xmm9, xmm12
        vaddps    xmm12, xmm15, xmm0
        vfmadd231ps xmm5, xmm0, xmm10
        vsubps    xmm11, xmm3, xmm8
        vfmadd231ps xmm11, xmm10, xmm15
        vmovups   xmm15, XMMWORD PTR [__svml_ssin_ha_data_internal+4480]
        vmovd     xmm0, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+8+r9+rax]
        vfmadd213ps xmm15, xmm9, XMMWORD PTR [__svml_ssin_ha_data_internal+4416]
        vaddps    xmm5, xmm5, xmm11
        vmulps    xmm11, xmm10, xmm4
        vfnmadd213ps xmm10, xmm7, xmm12
        vmulps    xmm15, xmm9, xmm15
        vfmadd213ps xmm11, xmm10, xmm5
        vmovd     xmm9, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+8+r9+rdx]
        vpunpcklqdq xmm3, xmm9, xmm0
        vpunpcklqdq xmm4, xmm1, xmm2
        vshufps   xmm9, xmm3, xmm4, 136
        vfmadd213ps xmm6, xmm10, xmm9
        vmovups   xmm0, XMMWORD PTR [32+rsp]
        vfmadd213ps xmm15, xmm7, xmm6
        vaddps    xmm6, xmm15, xmm11
        vaddps    xmm10, xmm8, xmm6
        vblendvps xmm0, xmm0, xmm10, xmm13
        jmp       _B4_2
        ALIGN     16

_B4_13::

__svml_sinf4_ha_l9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_sinf4_ha_l9_B1_B4:
	DD	1603329
	DD	2937975
	DD	1402991
	DD	1341542
	DD	1280093
	DD	1218644
	DD	1157195
	DD	1095746
	DD	1034297
	DD	972848
	DD	911399
	DD	849947
	DD	3080459

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B4_1
	DD	imagerel _B4_7
	DD	imagerel _unwind___svml_sinf4_ha_l9_B1_B4

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_sinf4_ha_l9_B7_B11:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B4_1
	DD	imagerel _B4_7
	DD	imagerel _unwind___svml_sinf4_ha_l9_B1_B4

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B4_7
	DD	imagerel _B4_12
	DD	imagerel _unwind___svml_sinf4_ha_l9_B7_B11

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_sinf4_ha_l9_B12_B12:
	DD	33
	DD	imagerel _B4_1
	DD	imagerel _B4_7
	DD	imagerel _unwind___svml_sinf4_ha_l9_B1_B4

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B4_12
	DD	imagerel _B4_13
	DD	imagerel _unwind___svml_sinf4_ha_l9_B12_B12

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST4:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_sinf4_ha_ex

__svml_sinf4_ha_ex	PROC

_B5_1::

        DB        243
        DB        15
        DB        30
        DB        250
L137::

        sub       rsp, 504
        lea       r9, QWORD PTR [__ImageBase]
        movups    XMMWORD PTR [368+rsp], xmm15
        xor       r10d, r10d
        movups    XMMWORD PTR [384+rsp], xmm14
        movups    XMMWORD PTR [320+rsp], xmm13
        movups    XMMWORD PTR [336+rsp], xmm12
        movups    XMMWORD PTR [448+rsp], xmm11
        movups    XMMWORD PTR [416+rsp], xmm10
        movups    XMMWORD PTR [352+rsp], xmm9
        movups    XMMWORD PTR [432+rsp], xmm8
        movups    XMMWORD PTR [464+rsp], xmm7
        movups    XMMWORD PTR [400+rsp], xmm6
        mov       QWORD PTR [480+rsp], r13
        lea       r13, QWORD PTR [239+rsp]
        movups    xmm1, XMMWORD PTR [rcx]
        and       r13, -64
        movups    xmm4, XMMWORD PTR [__svml_ssin_ha_data_internal+4096]
        movaps    xmm5, xmm1
        movups    xmm7, XMMWORD PTR [__svml_ssin_ha_data_internal+5056]
        andps     xmm5, xmm4
        mulps     xmm7, xmm5
        movaps    xmm3, xmm5
        cvtps2pd  xmm0, xmm5
        movups    xmm6, XMMWORD PTR [__svml_ssin_ha_data_internal+4544]
        andnps    xmm4, xmm1
        movhlps   xmm3, xmm5
        addps     xmm7, xmm6
        cvtps2pd  xmm2, xmm3
        movaps    xmm3, xmm7
        subps     xmm7, xmm6
        cvtps2pd  xmm10, xmm7
        movhlps   xmm7, xmm7
        pslld     xmm3, 31
        cvtps2pd  xmm12, xmm7
        movups    xmm9, XMMWORD PTR [__svml_ssin_ha_data_internal+5120]
        pxor      xmm3, xmm4
        movaps    xmm8, xmm9
        mulpd     xmm8, xmm10
        mulpd     xmm9, xmm12
        subpd     xmm0, xmm8
        subpd     xmm2, xmm9
        movups    xmm11, XMMWORD PTR [__svml_ssin_ha_data_internal+5184]
        mulpd     xmm10, xmm11
        mulpd     xmm12, xmm11
        subpd     xmm0, xmm10
        subpd     xmm2, xmm12
        movaps    xmm7, xmm0
        movaps    xmm8, xmm2
        mulpd     xmm7, xmm0
        mulpd     xmm8, xmm2
        movups    xmm13, XMMWORD PTR [__svml_ssin_ha_data_internal+5440]
        movaps    xmm6, xmm13
        mulpd     xmm6, xmm7
        mulpd     xmm13, xmm8
        movups    xmm14, XMMWORD PTR [__svml_ssin_ha_data_internal+5376]
        addpd     xmm6, xmm14
        addpd     xmm14, xmm13
        mulpd     xmm6, xmm7
        mulpd     xmm14, xmm8
        movups    xmm15, XMMWORD PTR [__svml_ssin_ha_data_internal+5312]
        addpd     xmm6, xmm15
        addpd     xmm15, xmm14
        mulpd     xmm6, xmm7
        mulpd     xmm15, xmm8
        movups    xmm13, XMMWORD PTR [__svml_ssin_ha_data_internal+5248]
        addpd     xmm6, xmm13
        addpd     xmm13, xmm15
        mulpd     xmm7, xmm6
        mulpd     xmm8, xmm13
        mulpd     xmm7, xmm0
        mulpd     xmm8, xmm2
        addpd     xmm0, xmm7
        addpd     xmm2, xmm8
        cvtpd2ps  xmm0, xmm0
        cvtpd2ps  xmm2, xmm2
        movlhps   xmm0, xmm2
        movaps    xmm2, xmm5
        cmpnleps  xmm2, XMMWORD PTR [__svml_ssin_ha_data_internal+4160]
        movmskps  eax, xmm2
        pxor      xmm0, xmm3
        mov       QWORD PTR [488+rsp], r13
        test      eax, eax
        jne       _B5_12

_B5_2::

        test      r10d, r10d
        jne       _B5_4

_B5_3::

        movups    xmm6, XMMWORD PTR [400+rsp]
        movups    xmm7, XMMWORD PTR [464+rsp]
        movups    xmm8, XMMWORD PTR [432+rsp]
        movups    xmm9, XMMWORD PTR [352+rsp]
        movups    xmm10, XMMWORD PTR [416+rsp]
        movups    xmm11, XMMWORD PTR [448+rsp]
        movups    xmm12, XMMWORD PTR [336+rsp]
        movups    xmm13, XMMWORD PTR [320+rsp]
        movups    xmm14, XMMWORD PTR [384+rsp]
        movups    xmm15, XMMWORD PTR [368+rsp]
        mov       r13, QWORD PTR [480+rsp]
        add       rsp, 504
        ret

_B5_4::

        movups    XMMWORD PTR [r13], xmm1
        movups    XMMWORD PTR [64+r13], xmm0
        je        _B5_3

_B5_7::

        xor       ecx, ecx
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, ecx
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, r10d

_B5_8::

        mov       ecx, ebx
        mov       eax, 1
        shl       eax, cl
        test      esi, eax
        jne       _B5_11

_B5_9::

        inc       ebx
        cmp       ebx, 4
        jl        _B5_8

_B5_10::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        movups    xmm0, XMMWORD PTR [64+r13]
        jmp       _B5_3

_B5_11::

        lea       rcx, QWORD PTR [r13+rbx*4]
        lea       rdx, QWORD PTR [64+r13+rbx*4]

        call      __svml_ssin_ha_cout_rare_internal
        jmp       _B5_9

_B5_12::

        mov       r11d, 2139095040
        movups    XMMWORD PTR [64+rsp], xmm0
        movups    XMMWORD PTR [48+rsp], xmm2
        movups    xmm4, XMMWORD PTR [__svml_ssin_ha_data_internal+4224]
        movd      xmm13, r11d
        mov       r11d, 8388607
        pshufd    xmm12, xmm13, 0
        andps     xmm5, xmm4
        pand      xmm12, xmm1
        psrld     xmm12, 23
        movdqa    xmm7, xmm12
        pslld     xmm7, 1
        paddd     xmm7, xmm12
        pslld     xmm7, 2
        pshufd    xmm10, xmm7, 2
        movd      edx, xmm10
        movd      xmm10, r11d
        pshufd    xmm14, xmm7, 3
        pshufd    xmm11, xmm7, 1
        movd      eax, xmm14
        mov       r11d, 1195376640
        movd      r8d, xmm7
        movd      ecx, xmm11
        movd      xmm6, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+r9+rdx]
        movd      xmm0, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+4+r9+rdx]
        movd      xmm11, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+8+r9+rdx]
        mov       edx, 8388608
        movd      xmm2, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+r9+rax]
        movd      xmm3, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+4+r9+rax]
        movd      xmm12, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+8+r9+rax]
        mov       eax, 65535
        movd      xmm8, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+r9+r8]
        movd      xmm15, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+r9+rcx]
        pshufd    xmm14, xmm10, 0
        punpcklqdq xmm8, xmm15
        pand      xmm14, xmm1
        punpcklqdq xmm6, xmm2
        movd      xmm13, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+8+r9+rcx]
        movups    XMMWORD PTR [32+rsp], xmm1
        movd      xmm1, edx
        cmpeqps   xmm5, xmm4
        movd      xmm4, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+8+r9+r8]
        mov       edx, 679477248
        shufps    xmm8, xmm6, 136
        punpcklqdq xmm4, xmm13
        movd      xmm13, eax
        pshufd    xmm1, xmm1, 0
        mov       eax, 262143
        punpcklqdq xmm11, xmm12
        movaps    xmm12, xmm8
        pshufd    xmm7, xmm13, 0
        paddd     xmm14, xmm1
        psrld     xmm12, 16
        pand      xmm8, xmm7
        movdqu    XMMWORD PTR [80+rsp], xmm12
        movdqa    xmm12, xmm14
        pand      xmm14, xmm7
        movaps    xmm13, xmm8
        movdqa    xmm10, xmm14
        psrlq     xmm13, 32
        psrlq     xmm10, 32
        movdqa    xmm15, xmm14
        movdqa    xmm6, xmm10
        psrld     xmm12, 16
        movmskps  r10d, xmm5
        movd      xmm9, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+4+r9+r8]
        movd      xmm5, DWORD PTR [imagerel(__svml_ssin_ha_reduction_data_internal)+4+r9+rcx]
        pmuludq   xmm15, xmm8
        mov       ecx, -2147483648
        pmuludq   xmm6, xmm13
        punpcklqdq xmm9, xmm5
        psllq     xmm6, 32
        punpcklqdq xmm0, xmm3
        mov       r8d, 1065353216
        shufps    xmm9, xmm0, 136
        movaps    xmm0, xmm9
        pand      xmm9, xmm7
        movups    XMMWORD PTR [96+rsp], xmm8
        psrld     xmm0, 16
        movdqu    xmm8, XMMWORD PTR [_2il0floatpacket_58]
        movdqa    xmm1, xmm0
        pand      xmm15, xmm8
        psrlq     xmm1, 32
        por       xmm15, xmm6
        movaps    xmm5, xmm9
        movdqu    XMMWORD PTR [160+rsp], xmm15
        movdqa    xmm6, xmm14
        movdqa    xmm15, xmm10
        psrlq     xmm5, 32
        movdqu    XMMWORD PTR [112+rsp], xmm0
        movdqu    XMMWORD PTR [144+rsp], xmm1
        pmuludq   xmm6, xmm0
        movdqa    xmm0, xmm14
        pmuludq   xmm15, xmm1
        pmuludq   xmm0, xmm9
        pmuludq   xmm9, xmm12
        movdqa    xmm1, xmm10
        pand      xmm0, xmm8
        pmuludq   xmm1, xmm5
        pand      xmm6, xmm8
        shufps    xmm4, xmm11, 136
        psllq     xmm1, 32
        movaps    xmm2, xmm4
        pand      xmm4, xmm7
        movdqa    xmm11, xmm12
        por       xmm0, xmm1
        movdqa    xmm1, xmm12
        psrlq     xmm11, 32
        pmuludq   xmm1, xmm4
        psrlq     xmm4, 32
        pmuludq   xmm4, xmm11
        pmuludq   xmm5, xmm11
        psrld     xmm2, 16
        pand      xmm1, xmm8
        movdqa    xmm3, xmm2
        psllq     xmm4, 32
        movdqu    XMMWORD PTR [128+rsp], xmm13
        psrlq     xmm3, 32
        por       xmm1, xmm4
        movdqa    xmm13, xmm12
        movdqa    xmm4, xmm11
        psllq     xmm15, 32
        pmuludq   xmm13, xmm2
        por       xmm6, xmm15
        pmuludq   xmm4, xmm3
        pmuludq   xmm2, xmm14
        pmuludq   xmm3, xmm10
        pand      xmm2, xmm8
        psllq     xmm3, 32
        pand      xmm13, xmm8
        psllq     xmm4, 32
        por       xmm2, xmm3
        movdqa    xmm15, xmm0
        por       xmm13, xmm4
        psrld     xmm2, 16
        pand      xmm9, xmm8
        psllq     xmm5, 32
        pand      xmm15, xmm7
        paddd     xmm13, xmm2
        movdqa    xmm2, xmm6
        por       xmm9, xmm5
        psrld     xmm0, 16
        psrld     xmm1, 16
        paddd     xmm15, xmm13
        pand      xmm2, xmm7
        paddd     xmm9, xmm0
        paddd     xmm1, xmm15
        movdqu    xmm15, XMMWORD PTR [112+rsp]
        paddd     xmm2, xmm9
        movdqu    xmm9, XMMWORD PTR [144+rsp]
        psrld     xmm6, 16
        pmuludq   xmm15, xmm12
        movdqa    xmm13, xmm1
        pmuludq   xmm9, xmm11
        pmuludq   xmm12, XMMWORD PTR [96+rsp]
        pmuludq   xmm11, XMMWORD PTR [128+rsp]
        pand      xmm15, xmm8
        psllq     xmm9, 32
        por       xmm15, xmm9
        psrld     xmm13, 16
        paddd     xmm15, xmm6
        paddd     xmm13, xmm2
        movdqu    xmm6, XMMWORD PTR [80+rsp]
        movdqa    xmm0, xmm13
        pmuludq   xmm14, xmm6
        psrlq     xmm6, 32
        pmuludq   xmm10, xmm6
        movdqu    xmm5, XMMWORD PTR [160+rsp]
        psrld     xmm0, 16
        movdqa    xmm2, xmm5
        pand      xmm14, xmm8
        pand      xmm2, xmm7
        psllq     xmm10, 32
        paddd     xmm2, xmm15
        pand      xmm12, xmm8
        psllq     xmm11, 32
        paddd     xmm0, xmm2
        por       xmm14, xmm10
        por       xmm12, xmm11
        psrld     xmm5, 16
        movdqa    xmm3, xmm0
        pand      xmm14, xmm7
        paddd     xmm12, xmm5
        psrld     xmm3, 16
        paddd     xmm14, xmm12
        paddd     xmm3, xmm14
        pand      xmm0, xmm7
        pslld     xmm3, 16
        pslld     xmm13, 16
        pand      xmm1, xmm7
        movd      xmm8, ecx
        pshufd    xmm9, xmm8, 0
        paddd     xmm3, xmm0
        paddd     xmm13, xmm1
        movd      xmm12, r8d
        movups    xmm1, XMMWORD PTR [32+rsp]
        movdqa    xmm5, xmm3
        pshufd    xmm11, xmm12, 0
        pand      xmm9, xmm1
        psrld     xmm5, 9
        pxor      xmm11, xmm9
        por       xmm5, xmm11
        movd      xmm10, r11d
        pshufd    xmm7, xmm10, 0
        movdqa    xmm2, xmm5
        mov       r8d, 511
        mov       ecx, 872415232
        movd      xmm8, eax
        movd      xmm15, edx
        pshufd    xmm6, xmm8, 0
        mov       r11d, 1086918619
        pshufd    xmm0, xmm15, 0
        pand      xmm6, xmm13
        movd      xmm11, r8d
        movd      xmm4, ecx
        pshufd    xmm10, xmm11, 0
        psrld     xmm13, 18
        pand      xmm10, xmm3
        pxor      xmm0, xmm9
        pshufd    xmm12, xmm4, 0
        pslld     xmm10, 14
        pxor      xmm9, xmm12
        por       xmm10, xmm13
        por       xmm10, xmm9
        pslld     xmm6, 5
        por       xmm6, xmm0
        mov       ecx, -4096
        movd      xmm13, r11d
        mov       edx, 1086918656
        mov       eax, -1214941318
        mov       r8d, 2147483647
        mov       r11d, 897581056
        addps     xmm2, xmm7
        subps     xmm10, xmm9
        subps     xmm6, xmm0
        movaps    xmm14, xmm2
        movd      xmm9, ecx
        movd      xmm3, eax
        movd      xmm15, r11d
        pshufd    xmm11, xmm3, 0
        subps     xmm14, xmm7
        pshufd    xmm0, xmm15, 0
        subps     xmm5, xmm14
        movaps    xmm12, xmm5
        movd      xmm14, r8d
        addps     xmm12, xmm10
        subps     xmm5, xmm12
        addps     xmm10, xmm5
        movd      xmm5, edx
        pshufd    xmm8, xmm5, 0
        mov       edx, 255
        movdqa    xmm7, xmm8
        addps     xmm6, xmm10
        pshufd    xmm10, xmm13, 0
        pshufd    xmm13, xmm9, 0
        andps     xmm13, xmm12
        mulps     xmm7, xmm13
        subps     xmm12, xmm13
        mulps     xmm13, xmm11
        mulps     xmm10, xmm6
        mulps     xmm8, xmm12
        mulps     xmm11, xmm12
        addps     xmm8, xmm13
        addps     xmm10, xmm11
        movd      xmm6, edx
        movdqa    xmm11, xmm0
        pshufd    xmm13, xmm6, 0
        addps     xmm8, xmm10
        movaps    xmm4, xmm8
        pand      xmm2, xmm13
        pshufd    xmm12, xmm14, 0
        pslld     xmm2, 4
        andps     xmm12, xmm1
        addps     xmm4, xmm7
        cmpltps   xmm11, xmm12
        cmpleps   xmm12, xmm0
        subps     xmm7, xmm4
        movd      edx, xmm2
        pshufd    xmm14, xmm2, 2
        andps     xmm12, xmm1
        movd      ecx, xmm14
        andps     xmm4, xmm11
        orps      xmm12, xmm4
        addps     xmm8, xmm7
        pshufd    xmm7, xmm2, 1
        andps     xmm11, xmm8
        pshufd    xmm2, xmm2, 3
        movaps    xmm10, xmm12
        movd      r8d, xmm2
        movd      eax, xmm7
        movd      xmm15, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+r9+rcx]
        movd      xmm14, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+r9+r8]
        punpcklqdq xmm15, xmm14
        movd      xmm14, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+4+r9+rdx]
        movd      xmm0, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+4+r9+rax]
        movd      xmm13, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+r9+rax]
        movd      xmm4, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+12+r9+rax]
        movd      xmm7, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+r9+rdx]
        punpcklqdq xmm14, xmm0
        movd      xmm0, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+12+r9+rdx]
        movd      xmm6, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+12+r9+rcx]
        movd      xmm5, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+12+r9+r8]
        punpcklqdq xmm7, xmm13
        punpcklqdq xmm0, xmm4
        movaps    xmm4, xmm12
        punpcklqdq xmm6, xmm5
        shufps    xmm7, xmm15, 136
        movaps    xmm15, xmm12
        shufps    xmm0, xmm6, 136
        mulps     xmm15, xmm0
        mulps     xmm10, xmm12
        mulps     xmm4, xmm7
        addps     xmm7, xmm0
        movd      xmm2, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+4+r9+r8]
        movd      xmm3, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+4+r9+rcx]
        punpcklqdq xmm3, xmm2
        shufps    xmm14, xmm3, 136
        movaps    xmm9, xmm14
        movaps    xmm8, xmm14
        movups    xmm2, XMMWORD PTR [__svml_ssin_ha_data_internal+4480]
        addps     xmm9, xmm15
        mulps     xmm2, xmm10
        subps     xmm8, xmm9
        addps     xmm2, XMMWORD PTR [__svml_ssin_ha_data_internal+4416]
        addps     xmm15, xmm8
        movups    xmm8, XMMWORD PTR [__svml_ssin_ha_data_internal+4352]
        movaps    xmm13, xmm9
        mulps     xmm8, xmm10
        addps     xmm13, xmm4
        movd      xmm0, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+8+r9+rdx]
        subps     xmm9, xmm13
        addps     xmm8, XMMWORD PTR [__svml_ssin_ha_data_internal+4288]
        addps     xmm4, xmm9
        mulps     xmm8, xmm10
        mulps     xmm10, xmm2
        addps     xmm15, xmm4
        mulps     xmm8, xmm12
        mulps     xmm12, xmm14
        mulps     xmm14, xmm10
        subps     xmm7, xmm12
        mulps     xmm11, xmm7
        mulps     xmm8, xmm7
        movd      xmm3, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+8+r9+rax]
        addps     xmm15, xmm8
        movd      xmm6, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+8+r9+rcx]
        movd      xmm5, DWORD PTR [imagerel(__svml_ssin_ha_data_internal)+8+r9+r8]
        punpcklqdq xmm0, xmm3
        punpcklqdq xmm6, xmm5
        shufps    xmm0, xmm6, 136
        movups    xmm7, XMMWORD PTR [48+rsp]
        addps     xmm0, xmm11
        addps     xmm0, xmm14
        addps     xmm0, xmm15
        addps     xmm13, xmm0
        movaps    xmm0, xmm7
        andnps    xmm0, XMMWORD PTR [64+rsp]
        andps     xmm13, xmm7
        orps      xmm0, xmm13
        jmp       _B5_2
        ALIGN     16

_B5_13::

__svml_sinf4_ha_ex ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_sinf4_ha_ex_B1_B4:
	DD	1602817
	DD	3986549
	DD	1665133
	DD	1931365
	DD	1804381
	DD	1480788
	DD	1747019
	DD	1882178
	DD	1427513
	DD	1366064
	DD	1632295
	DD	1570843
	DD	4129035

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B5_1
	DD	imagerel _B5_7
	DD	imagerel _unwind___svml_sinf4_ha_ex_B1_B4

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_sinf4_ha_ex_B7_B11:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B5_1
	DD	imagerel _B5_7
	DD	imagerel _unwind___svml_sinf4_ha_ex_B1_B4

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B5_7
	DD	imagerel _B5_12
	DD	imagerel _unwind___svml_sinf4_ha_ex_B7_B11

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_sinf4_ha_ex_B12_B12:
	DD	33
	DD	imagerel _B5_1
	DD	imagerel _B5_7
	DD	imagerel _unwind___svml_sinf4_ha_ex_B1_B4

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B5_12
	DD	imagerel _B5_13
	DD	imagerel _unwind___svml_sinf4_ha_ex_B12_B12

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST5:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_ssin_ha_cout_rare_internal

__svml_ssin_ha_cout_rare_internal	PROC

_B6_1::

        DB        243
        DB        15
        DB        30
        DB        250
L182::

        sub       rsp, 40
        mov       r8, rdx
        mov       edx, DWORD PTR [rcx]
        movzx     eax, WORD PTR [2+rcx]
        mov       DWORD PTR [32+rsp], edx
        and       eax, 32640
        shr       edx, 24
        and       edx, 127
        movss     xmm1, DWORD PTR [rcx]
        cmp       eax, 32640
        jne       _B6_6

_B6_2::

        mov       BYTE PTR [35+rsp], dl
        cmp       DWORD PTR [32+rsp], 2139095040
        jne       _B6_4

_B6_3::

        movss     xmm0, DWORD PTR [_vmlsSinHATab]
        mov       eax, 1
        mulss     xmm1, xmm0
        movss     DWORD PTR [r8], xmm1
        add       rsp, 40
        ret

_B6_4::

        mulss     xmm1, DWORD PTR [rcx]
        xor       eax, eax
        movss     DWORD PTR [r8], xmm1

_B6_5::

        add       rsp, 40
        ret

_B6_6::

        xor       eax, eax
        add       rsp, 40
        ret
        ALIGN     16

_B6_7::

__svml_ssin_ha_cout_rare_internal ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_ssin_ha_cout_rare_internal_B1_B6:
	DD	67585
	DD	16904

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B6_1
	DD	imagerel _B6_7
	DD	imagerel _unwind___svml_ssin_ha_cout_rare_internal_B1_B6

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_RDATA	SEGMENT     READ PAGE   'DATA'
	ALIGN  32
	PUBLIC __svml_ssin_ha_reduction_data_internal
__svml_ssin_ha_reduction_data_internal	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	1
	DD	0
	DD	0
	DD	2
	DD	0
	DD	0
	DD	5
	DD	0
	DD	0
	DD	10
	DD	0
	DD	0
	DD	20
	DD	0
	DD	0
	DD	40
	DD	0
	DD	0
	DD	81
	DD	0
	DD	0
	DD	162
	DD	0
	DD	0
	DD	325
	DD	0
	DD	0
	DD	651
	DD	0
	DD	0
	DD	1303
	DD	0
	DD	0
	DD	2607
	DD	0
	DD	0
	DD	5215
	DD	0
	DD	0
	DD	10430
	DD	0
	DD	0
	DD	20860
	DD	0
	DD	0
	DD	41721
	DD	0
	DD	0
	DD	83443
	DD	0
	DD	0
	DD	166886
	DD	0
	DD	0
	DD	333772
	DD	0
	DD	0
	DD	667544
	DD	0
	DD	0
	DD	1335088
	DD	0
	DD	0
	DD	2670176
	DD	0
	DD	0
	DD	5340353
	DD	0
	DD	0
	DD	10680707
	DD	0
	DD	0
	DD	21361414
	DD	0
	DD	0
	DD	42722829
	DD	0
	DD	0
	DD	85445659
	DD	0
	DD	0
	DD	170891318
	DD	0
	DD	0
	DD	341782637
	DD	0
	DD	0
	DD	683565275
	DD	0
	DD	0
	DD	1367130551
	DD	0
	DD	0
	DD	2734261102
	DD	0
	DD	1
	DD	1173554908
	DD	0
	DD	2
	DD	2347109817
	DD	0
	DD	5
	DD	399252338
	DD	0
	DD	10
	DD	798504676
	DD	0
	DD	20
	DD	1597009353
	DD	0
	DD	40
	DD	3194018707
	DD	0
	DD	81
	DD	2093070119
	DD	0
	DD	162
	DD	4186140238
	DD	0
	DD	325
	DD	4077313180
	DD	0
	DD	651
	DD	3859659065
	DD	0
	DD	1303
	DD	3424350834
	DD	0
	DD	2607
	DD	2553734372
	DD	0
	DD	5215
	DD	812501448
	DD	0
	DD	10430
	DD	1625002897
	DD	0
	DD	20860
	DD	3250005794
	DD	0
	DD	41721
	DD	2205044292
	DD	0
	DD	83443
	DD	115121288
	DD	0
	DD	166886
	DD	230242576
	DD	0
	DD	333772
	DD	460485152
	DD	0
	DD	667544
	DD	920970305
	DD	0
	DD	1335088
	DD	1841940610
	DD	0
	DD	2670176
	DD	3683881221
	DD	0
	DD	5340353
	DD	3072795146
	DD	0
	DD	10680707
	DD	1850622997
	DD	0
	DD	21361414
	DD	3701245994
	DD	0
	DD	42722829
	DD	3107524692
	DD	0
	DD	85445659
	DD	1920082089
	DD	0
	DD	170891318
	DD	3840164178
	DD	0
	DD	341782637
	DD	3385361061
	DD	0
	DD	683565275
	DD	2475754826
	DD	0
	DD	1367130551
	DD	656542356
	DD	0
	DD	2734261102
	DD	1313084713
	DD	1
	DD	1173554908
	DD	2626169427
	DD	2
	DD	2347109817
	DD	957371559
	DD	5
	DD	399252338
	DD	1914743119
	DD	10
	DD	798504676
	DD	3829486239
	DD	20
	DD	1597009353
	DD	3364005183
	DD	40
	DD	3194018707
	DD	2433043071
	DD	81
	DD	2093070119
	DD	571118846
	DD	162
	DD	4186140238
	DD	1142237692
	DD	325
	DD	4077313180
	DD	2284475384
	DD	651
	DD	3859659065
	DD	273983472
	DD	1303
	DD	3424350834
	DD	547966945
	DD	2607
	DD	2553734372
	DD	1095933890
	DD	5215
	DD	812501448
	DD	2191867780
	DD	10430
	DD	1625002897
	DD	88768265
	DD	20860
	DD	3250005794
	DD	177536531
	DD	41721
	DD	2205044292
	DD	355073063
	DD	83443
	DD	115121288
	DD	710146126
	DD	166886
	DD	230242576
	DD	1420292253
	DD	333772
	DD	460485152
	DD	2840584506
	DD	667544
	DD	920970305
	DD	1386201717
	DD	1335088
	DD	1841940610
	DD	2772403434
	DD	2670176
	DD	3683881221
	DD	1249839573
	DD	5340353
	DD	3072795146
	DD	2499679147
	DD	10680707
	DD	1850622997
	DD	704390999
	DD	21361414
	DD	3701245994
	DD	1408781999
	DD	42722829
	DD	3107524692
	DD	2817563999
	DD	85445659
	DD	1920082089
	DD	1340160702
	DD	170891318
	DD	3840164178
	DD	2680321405
	DD	341782637
	DD	3385361061
	DD	1065675514
	DD	683565275
	DD	2475754826
	DD	2131351028
	DD	1367130551
	DD	656542356
	DD	4262702056
	DD	2734261102
	DD	1313084713
	DD	4230436817
	DD	1173554908
	DD	2626169427
	DD	4165906339
	DD	2347109817
	DD	957371559
	DD	4036845383
	DD	399252338
	DD	1914743119
	DD	3778723471
	DD	798504676
	DD	3829486239
	DD	3262479647
	DD	1597009353
	DD	3364005183
	DD	2229991998
	DD	3194018707
	DD	2433043071
	DD	165016701
	DD	2093070119
	DD	571118846
	DD	330033402
	DD	4186140238
	DD	1142237692
	DD	660066805
	DD	4077313180
	DD	2284475384
	DD	1320133610
	DD	3859659065
	DD	273983472
	DD	2640267220
	DD	3424350834
	DD	547966945
	DD	985567145
	DD	2553734372
	DD	1095933890
	DD	1971134291
	DD	812501448
	DD	2191867780
	DD	3942268582
	DD	1625002897
	DD	88768265
	DD	3589569869
	DD	3250005794
	DD	177536531
	DD	2884172442
	DD	2205044292
	DD	355073063
	DD	1473377588
	DD	115121288
	DD	710146126
	DD	2946755177
	DD	230242576
	DD	1420292253
	DD	1598543059
	DD	460485152
	DD	2840584506
	DD	3197086118
	DD	920970305
	DD	1386201717
	DD	2099204941
	DD	1841940610
	DD	2772403434
	DD	4198409883
	DD	3683881221
	DD	1249839573
	DD	4101852471
	DD	3072795146
	DD	2499679147
	DD	3908737646
	DD	1850622997
	DD	704390999
	DD	3522507997
	DD	3701245994
	DD	1408781999
	DD	2750048699
	DD	3107524692
	DD	2817563999
	DD	1205130103
	DD	1920082089
	DD	1340160702
	DD	2410260206
	DD	3840164178
	DD	2680321405
	DD	525553116
	DD	3385361061
	DD	1065675514
	DD	1051106232
	DD	2475754826
	DD	2131351028
	DD	2102212464
	DD	656542356
	DD	4262702056
	DD	4204424928
	DD	1313084713
	DD	4230436817
	DD	4113882560
	DD	2626169427
	DD	4165906339
	DD	3932797825
	DD	957371559
	DD	4036845383
	DD	3570628355
	DD	1914743119
	DD	3778723471
	DD	2846289414
	DD	3829486239
	DD	3262479647
	DD	1397611533
	DD	3364005183
	DD	2229991998
	DD	2795223067
	DD	2433043071
	DD	165016701
	DD	1295478838
	DD	571118846
	DD	330033402
	DD	2590957677
	DD	1142237692
	DD	660066805
	DD	886948059
	DD	2284475384
	DD	1320133610
	DD	1773896118
	DD	273983472
	DD	2640267220
	DD	3547792237
	DD	547966945
	DD	985567145
	DD	2800617179
	DD	1095933890
	DD	1971134291
	DD	1306267062
	DD	2191867780
	DD	3942268582
	DD	2612534124
	DD	88768265
	DD	3589569869
	DD	930100952
	DD	177536531
	DD	2884172442
	DD	1860201905
	DD	355073063
	DD	1473377588
	DD	3720403810
	DD	710146126
	DD	2946755177
	DD	3145840325
	DD	1420292253
	DD	1598543059
	DD	1996713354
	DD	2840584506
	DD	3197086118
	DD	3993426708
	DD	1386201717
	DD	2099204941
	DD	3691886121
	DD	2772403434
	DD	4198409883
	DD	3088804946
	DD	1249839573
	DD	4101852471
	DD	1882642597
	DD	2499679147
	DD	3908737646
	DD	3765285194
	DD	704390999
	DD	3522507997
	DD	3235603093
	DD	1408781999
	DD	2750048699
	DD	2176238891
	DD	2817563999
	DD	1205130103
	DD	57510486
	DD	1340160702
	DD	2410260206
	DD	115020972
	DD	2680321405
	DD	525553116
	DD	230041945
	DD	1065675514
	DD	1051106232
	DD	460083891
	DD	2131351028
	DD	2102212464
	DD	920167782
	DD	4262702056
	DD	4204424928
	DD	1840335564
	DD	4230436817
	DD	4113882560
	DD	3680671129
	DD	4165906339
	DD	3932797825
	DD	3066374962
	DD	4036845383
	DD	3570628355
	DD	1837782628
	DD	3778723471
	DD	2846289414
	DD	3675565257
	DD	3262479647
	DD	1397611533
	DD	3056163219
	DD	2229991998
	DD	2795223067
	DD	1817359143
	DD	165016701
	DD	1295478838
	DD	3634718287
	DD	330033402
	DD	2590957677
	DD	2974469278
	DD	660066805
	DD	886948059
	DD	1653971260
	DD	1320133610
	DD	1773896118
	DD	3307942520
	DD	2640267220
	DD	3547792237
	DD	2320917745
	DD	985567145
	DD	2800617179
	DD	346868194
	DD	1971134291
	DD	1306267062
	DD	693736388
	DD	3942268582
	DD	2612534124
	DD	1387472776
	DD	3589569869
	DD	930100952
	DD	2774945552
	DD	2884172442
	DD	1860201905
	DD	1254923809
	DD	1473377588
	DD	3720403810
	DD	2509847619
	DD	2946755177
	DD	3145840325
	DD	724727943
	DD	1598543059
	DD	1996713354
	DD	1449455886
	DD	3197086118
	DD	3993426708
	DD	2898911772
	DD	2099204941
	DD	3691886121
	DD	1502856249
	DD	4198409883
	DD	3088804946
	DD	3005712498
	DD	4101852471
	DD	1882642597
	DD	1716457700
	DD	3908737646
	DD	3765285194
	DD	3432915400
	DD	3522507997
	DD	3235603093
	DD	2570863504
	DD	2750048699
	DD	2176238891
	DD	846759712
	DD	1205130103
	DD	57510486
	DD	1693519425
	DD	2410260206
	DD	115020972
	DD	3387038850
	DD	525553116
	DD	230041945
	DD	2479110404
	DD	1051106232
	DD	460083891
	DD	663253512
	DD	2102212464
	DD	920167782
	DD	1326507024
	DD	4204424928
	DD	1840335564
	DD	2653014048
	DD	4113882560
	DD	3680671129
	DD	1011060801
	DD	3932797825
	DD	3066374962
	DD	2022121603
	DD	3570628355
	DD	1837782628
	DD	4044243207
	DD	2846289414
	DD	3675565257
	DD	3793519119
	DD	1397611533
	DD	3056163219
	DD	3292070943
	DD	2795223067
	DD	1817359143
	DD	2289174591
	DD	1295478838
	DD	3634718287
	DD	283381887
	DD	2590957677
	DD	2974469278
	DD	566763775
	PUBLIC __svml_ssin_ha_data_internal
__svml_ssin_ha_data_internal	DD	0
	DD	0
	DD	0
	DD	1065353216
	DD	3114133471
	DD	1019808432
	DD	2953169304
	DD	1065353216
	DD	3130909128
	DD	1028193072
	DD	2968461951
	DD	1065353216
	DD	3140588184
	DD	1033283845
	DD	2975014497
	DD	1065353216
	DD	3147680113
	DD	1036565814
	DD	2960495349
	DD	1065353216
	DD	3153489468
	DD	1039839859
	DD	2970970319
	DD	1065353216
	DD	3157349634
	DD	1041645699
	DD	837346836
	DD	1065353216
	DD	3161536011
	DD	1043271842
	DD	823224313
	DD	1065353216
	DD	3164432432
	DD	1044891074
	DD	2967836285
	DD	1065353216
	DD	3167161428
	DD	1046502419
	DD	833086710
	DD	1065353216
	DD	3170205956
	DD	1048104908
	DD	2971391005
	DD	1065353216
	DD	3172229004
	DD	1049136787
	DD	824999326
	DD	1065353216
	DD	3174063957
	DD	1049927729
	DD	846027248
	DD	1065353216
	DD	3176053642
	DD	1050712805
	DD	2990442912
	DD	1065353216
	DD	3178196862
	DD	1051491540
	DD	2988789250
	DD	1065353216
	DD	3179887378
	DD	1052263466
	DD	2993707942
	DD	1065353216
	DD	3181110540
	DD	1053028117
	DD	836097324
	DD	1065353216
	DD	3182408396
	DD	1053785034
	DD	829045603
	DD	1065353216
	DD	3183780163
	DD	1054533760
	DD	840832460
	DD	1065353216
	DD	3185225016
	DD	1055273845
	DD	2983839604
	DD	1065353216
	DD	3186742084
	DD	1056004842
	DD	2986287417
	DD	1065353216
	DD	3188000746
	DD	1056726311
	DD	2978016425
	DD	1065353216
	DD	3188830103
	DD	1057201213
	DD	2992349186
	DD	1065353216
	DD	3189694133
	DD	1057551771
	DD	2998815566
	DD	1065353216
	DD	3190592315
	DD	1057896922
	DD	2991207143
	DD	1065353216
	DD	3191524108
	DD	1058236458
	DD	852349230
	DD	1065353216
	DD	3192488951
	DD	1058570176
	DD	2982650867
	DD	1065353216
	DD	3193486263
	DD	1058897873
	DD	848430348
	DD	1065353216
	DD	3194515443
	DD	1059219353
	DD	841032635
	DD	1065353216
	DD	3195575871
	DD	1059534422
	DD	2986574659
	DD	1065353216
	DD	3196363278
	DD	1059842890
	DD	2998350134
	DD	1065353216
	DD	3196923773
	DD	1060144571
	DD	2997759282
	DD	1065353216
	DD	3197498906
	DD	1060439283
	DD	844097402
	DD	1065353216
	DD	1044518635
	DD	1060726850
	DD	2994798599
	DD	1056964608
	DD	1043311911
	DD	1061007097
	DD	832220140
	DD	1056964608
	DD	1042078039
	DD	1061279856
	DD	851442039
	DD	1056964608
	DD	1040817765
	DD	1061544963
	DD	850481524
	DD	1056964608
	DD	1038876298
	DD	1061802258
	DD	848897600
	DD	1056964608
	DD	1036254719
	DD	1062051586
	DD	847147240
	DD	1056964608
	DD	1033584979
	DD	1062292797
	DD	806113028
	DD	1056964608
	DD	1029938589
	DD	1062525745
	DD	848357914
	DD	1056964608
	DD	1024416170
	DD	1062750291
	DD	2994560960
	DD	1056964608
	DD	1013387058
	DD	1062966298
	DD	841166280
	DD	1056964608
	DD	3152590408
	DD	1063173637
	DD	851900755
	DD	1056964608
	DD	3169472868
	DD	1063372184
	DD	3001545765
	DD	1056964608
	DD	3176031322
	DD	1063561817
	DD	823789818
	DD	1056964608
	DD	3180617215
	DD	1063742424
	DD	2998678409
	DD	1056964608
	DD	3183612120
	DD	1063913895
	DD	3001754476
	DD	1056964608
	DD	3186639787
	DD	1064076126
	DD	854796500
	DD	1056964608
	DD	3188684717
	DD	1064229022
	DD	2995991516
	DD	1056964608
	DD	1035072335
	DD	1064372488
	DD	840880349
	DD	1048576000
	DD	1031957395
	DD	1064506439
	DD	851742225
	DD	1048576000
	DD	1025835404
	DD	1064630795
	DD	2996018466
	DD	1048576000
	DD	1015605553
	DD	1064745479
	DD	846006572
	DD	1048576000
	DD	3152414341
	DD	1064850424
	DD	2987244005
	DD	1048576000
	DD	3170705253
	DD	1064945565
	DD	851856985
	DD	1048576000
	DD	3177244920
	DD	1065030846
	DD	855602635
	DD	1048576000
	DD	1027359369
	DD	1065106216
	DD	2989610635
	DD	1040187392
	DD	1018299420
	DD	1065171628
	DD	2969000681
	DD	1040187392
	DD	3140071849
	DD	1065227044
	DD	3002197507
	DD	1040187392
	DD	3168602920
	DD	1065272429
	DD	838093129
	DD	1040187392
	DD	1010124837
	DD	1065307757
	DD	852498564
	DD	1031798784
	DD	3160150850
	DD	1065333007
	DD	836655967
	DD	1031798784
	DD	3151746369
	DD	1065348163
	DD	814009613
	DD	1023410176
	DD	0
	DD	1065353216
	DD	0
	DD	0
	DD	1004262721
	DD	1065348163
	DD	814009613
	DD	3170893824
	DD	1012667202
	DD	1065333007
	DD	836655967
	DD	3179282432
	DD	3157608485
	DD	1065307757
	DD	852498564
	DD	3179282432
	DD	1021119272
	DD	1065272429
	DD	838093129
	DD	3187671040
	DD	992588201
	DD	1065227044
	DD	3002197507
	DD	3187671040
	DD	3165783068
	DD	1065171628
	DD	2969000681
	DD	3187671040
	DD	3174843017
	DD	1065106216
	DD	2989610635
	DD	3187671040
	DD	1029761272
	DD	1065030846
	DD	855602635
	DD	3196059648
	DD	1023221605
	DD	1064945565
	DD	851856985
	DD	3196059648
	DD	1004930693
	DD	1064850424
	DD	2987244005
	DD	3196059648
	DD	3163089201
	DD	1064745479
	DD	846006572
	DD	3196059648
	DD	3173319052
	DD	1064630795
	DD	2996018466
	DD	3196059648
	DD	3179441043
	DD	1064506439
	DD	851742225
	DD	3196059648
	DD	3182555983
	DD	1064372488
	DD	840880349
	DD	3196059648
	DD	1041201069
	DD	1064229022
	DD	2995991516
	DD	3204448256
	DD	1039156139
	DD	1064076126
	DD	854796500
	DD	3204448256
	DD	1036128472
	DD	1063913895
	DD	3001754476
	DD	3204448256
	DD	1033133567
	DD	1063742424
	DD	2998678409
	DD	3204448256
	DD	1028547674
	DD	1063561817
	DD	823789818
	DD	3204448256
	DD	1021989220
	DD	1063372184
	DD	3001545765
	DD	3204448256
	DD	1005106760
	DD	1063173637
	DD	851900755
	DD	3204448256
	DD	3160870706
	DD	1062966298
	DD	841166280
	DD	3204448256
	DD	3171899818
	DD	1062750291
	DD	2994560960
	DD	3204448256
	DD	3177422237
	DD	1062525745
	DD	848357914
	DD	3204448256
	DD	3181068627
	DD	1062292797
	DD	806113028
	DD	3204448256
	DD	3183738367
	DD	1062051586
	DD	847147240
	DD	3204448256
	DD	3186359946
	DD	1061802258
	DD	848897600
	DD	3204448256
	DD	3188301413
	DD	1061544963
	DD	850481524
	DD	3204448256
	DD	3189561687
	DD	1061279856
	DD	851442039
	DD	3204448256
	DD	3190795559
	DD	1061007097
	DD	832220140
	DD	3204448256
	DD	3192002283
	DD	1060726850
	DD	2994798599
	DD	3204448256
	DD	1050015258
	DD	1060439283
	DD	844097402
	DD	3212836864
	DD	1049440125
	DD	1060144571
	DD	2997759282
	DD	3212836864
	DD	1048879630
	DD	1059842890
	DD	2998350134
	DD	3212836864
	DD	1048092223
	DD	1059534422
	DD	2986574659
	DD	3212836864
	DD	1047031795
	DD	1059219353
	DD	841032635
	DD	3212836864
	DD	1046002615
	DD	1058897873
	DD	848430348
	DD	3212836864
	DD	1045005303
	DD	1058570176
	DD	2982650867
	DD	3212836864
	DD	1044040460
	DD	1058236458
	DD	852349230
	DD	3212836864
	DD	1043108667
	DD	1057896922
	DD	2991207143
	DD	3212836864
	DD	1042210485
	DD	1057551771
	DD	2998815566
	DD	3212836864
	DD	1041346455
	DD	1057201213
	DD	2992349186
	DD	3212836864
	DD	1040517098
	DD	1056726311
	DD	2978016425
	DD	3212836864
	DD	1039258436
	DD	1056004842
	DD	2986287417
	DD	3212836864
	DD	1037741368
	DD	1055273845
	DD	2983839604
	DD	3212836864
	DD	1036296515
	DD	1054533760
	DD	840832460
	DD	3212836864
	DD	1034924748
	DD	1053785034
	DD	829045603
	DD	3212836864
	DD	1033626892
	DD	1053028117
	DD	836097324
	DD	3212836864
	DD	1032403730
	DD	1052263466
	DD	2993707942
	DD	3212836864
	DD	1030713214
	DD	1051491540
	DD	2988789250
	DD	3212836864
	DD	1028569994
	DD	1050712805
	DD	2990442912
	DD	3212836864
	DD	1026580309
	DD	1049927729
	DD	846027248
	DD	3212836864
	DD	1024745356
	DD	1049136787
	DD	824999326
	DD	3212836864
	DD	1022722308
	DD	1048104908
	DD	2971391005
	DD	3212836864
	DD	1019677780
	DD	1046502419
	DD	833086710
	DD	3212836864
	DD	1016948784
	DD	1044891074
	DD	2967836285
	DD	3212836864
	DD	1014052363
	DD	1043271842
	DD	823224313
	DD	3212836864
	DD	1009865986
	DD	1041645699
	DD	837346836
	DD	3212836864
	DD	1006005820
	DD	1039839859
	DD	2970970319
	DD	3212836864
	DD	1000196465
	DD	1036565814
	DD	2960495349
	DD	3212836864
	DD	993104536
	DD	1033283845
	DD	2975014497
	DD	3212836864
	DD	983425480
	DD	1028193072
	DD	2968461951
	DD	3212836864
	DD	966649823
	DD	1019808432
	DD	2953169304
	DD	3212836864
	DD	0
	DD	0
	DD	0
	DD	3212836864
	DD	966649823
	DD	3167292080
	DD	805685656
	DD	3212836864
	DD	983425480
	DD	3175676720
	DD	820978303
	DD	3212836864
	DD	993104536
	DD	3180767493
	DD	827530849
	DD	3212836864
	DD	1000196465
	DD	3184049462
	DD	813011701
	DD	3212836864
	DD	1006005820
	DD	3187323507
	DD	823486671
	DD	3212836864
	DD	1009865986
	DD	3189129347
	DD	2984830484
	DD	3212836864
	DD	1014052363
	DD	3190755490
	DD	2970707961
	DD	3212836864
	DD	1016948784
	DD	3192374722
	DD	820352637
	DD	3212836864
	DD	1019677780
	DD	3193986067
	DD	2980570358
	DD	3212836864
	DD	1022722308
	DD	3195588556
	DD	823907357
	DD	3212836864
	DD	1024745356
	DD	3196620435
	DD	2972482974
	DD	3212836864
	DD	1026580309
	DD	3197411377
	DD	2993510896
	DD	3212836864
	DD	1028569994
	DD	3198196453
	DD	842959264
	DD	3212836864
	DD	1030713214
	DD	3198975188
	DD	841305602
	DD	3212836864
	DD	1032403730
	DD	3199747114
	DD	846224294
	DD	3212836864
	DD	1033626892
	DD	3200511765
	DD	2983580972
	DD	3212836864
	DD	1034924748
	DD	3201268682
	DD	2976529251
	DD	3212836864
	DD	1036296515
	DD	3202017408
	DD	2988316108
	DD	3212836864
	DD	1037741368
	DD	3202757493
	DD	836355956
	DD	3212836864
	DD	1039258436
	DD	3203488490
	DD	838803769
	DD	3212836864
	DD	1040517098
	DD	3204209959
	DD	830532777
	DD	3212836864
	DD	1041346455
	DD	3204684861
	DD	844865538
	DD	3212836864
	DD	1042210485
	DD	3205035419
	DD	851331918
	DD	3212836864
	DD	1043108667
	DD	3205380570
	DD	843723495
	DD	3212836864
	DD	1044040460
	DD	3205720106
	DD	2999832878
	DD	3212836864
	DD	1045005303
	DD	3206053824
	DD	835167219
	DD	3212836864
	DD	1046002615
	DD	3206381521
	DD	2995913996
	DD	3212836864
	DD	1047031795
	DD	3206703001
	DD	2988516283
	DD	3212836864
	DD	1048092223
	DD	3207018070
	DD	839091011
	DD	3212836864
	DD	1048879630
	DD	3207326538
	DD	850866486
	DD	3212836864
	DD	1049440125
	DD	3207628219
	DD	850275634
	DD	3212836864
	DD	1050015258
	DD	3207922931
	DD	2991581050
	DD	3212836864
	DD	3192002283
	DD	3208210498
	DD	847314951
	DD	3204448256
	DD	3190795559
	DD	3208490745
	DD	2979703788
	DD	3204448256
	DD	3189561687
	DD	3208763504
	DD	2998925687
	DD	3204448256
	DD	3188301413
	DD	3209028611
	DD	2997965172
	DD	3204448256
	DD	3186359946
	DD	3209285906
	DD	2996381248
	DD	3204448256
	DD	3183738367
	DD	3209535234
	DD	2994630888
	DD	3204448256
	DD	3181068627
	DD	3209776445
	DD	2953596676
	DD	3204448256
	DD	3177422237
	DD	3210009393
	DD	2995841562
	DD	3204448256
	DD	3171899818
	DD	3210233939
	DD	847077312
	DD	3204448256
	DD	3160870706
	DD	3210449946
	DD	2988649928
	DD	3204448256
	DD	1005106760
	DD	3210657285
	DD	2999384403
	DD	3204448256
	DD	1021989220
	DD	3210855832
	DD	854062117
	DD	3204448256
	DD	1028547674
	DD	3211045465
	DD	2971273466
	DD	3204448256
	DD	1033133567
	DD	3211226072
	DD	851194761
	DD	3204448256
	DD	1036128472
	DD	3211397543
	DD	854270828
	DD	3204448256
	DD	1039156139
	DD	3211559774
	DD	3002280148
	DD	3204448256
	DD	1041201069
	DD	3211712670
	DD	848507868
	DD	3204448256
	DD	3182555983
	DD	3211856136
	DD	2988363997
	DD	3196059648
	DD	3179441043
	DD	3211990087
	DD	2999225873
	DD	3196059648
	DD	3173319052
	DD	3212114443
	DD	848534818
	DD	3196059648
	DD	3163089201
	DD	3212229127
	DD	2993490220
	DD	3196059648
	DD	1004930693
	DD	3212334072
	DD	839760357
	DD	3196059648
	DD	1023221605
	DD	3212429213
	DD	2999340633
	DD	3196059648
	DD	1029761272
	DD	3212514494
	DD	3003086283
	DD	3196059648
	DD	3174843017
	DD	3212589864
	DD	842126987
	DD	3187671040
	DD	3165783068
	DD	3212655276
	DD	821517033
	DD	3187671040
	DD	992588201
	DD	3212710692
	DD	854713859
	DD	3187671040
	DD	1021119272
	DD	3212756077
	DD	2985576777
	DD	3187671040
	DD	3157608485
	DD	3212791405
	DD	2999982212
	DD	3179282432
	DD	1012667202
	DD	3212816655
	DD	2984139615
	DD	3179282432
	DD	1004262721
	DD	3212831811
	DD	2961493261
	DD	3170893824
	DD	0
	DD	3212836864
	DD	0
	DD	0
	DD	3151746369
	DD	3212831811
	DD	2961493261
	DD	1023410176
	DD	3160150850
	DD	3212816655
	DD	2984139615
	DD	1031798784
	DD	1010124837
	DD	3212791405
	DD	2999982212
	DD	1031798784
	DD	3168602920
	DD	3212756077
	DD	2985576777
	DD	1040187392
	DD	3140071849
	DD	3212710692
	DD	854713859
	DD	1040187392
	DD	1018299420
	DD	3212655276
	DD	821517033
	DD	1040187392
	DD	1027359369
	DD	3212589864
	DD	842126987
	DD	1040187392
	DD	3177244920
	DD	3212514494
	DD	3003086283
	DD	1048576000
	DD	3170705253
	DD	3212429213
	DD	2999340633
	DD	1048576000
	DD	3152414341
	DD	3212334072
	DD	839760357
	DD	1048576000
	DD	1015605553
	DD	3212229127
	DD	2993490220
	DD	1048576000
	DD	1025835404
	DD	3212114443
	DD	848534818
	DD	1048576000
	DD	1031957395
	DD	3211990087
	DD	2999225873
	DD	1048576000
	DD	1035072335
	DD	3211856136
	DD	2988363997
	DD	1048576000
	DD	3188684717
	DD	3211712670
	DD	848507868
	DD	1056964608
	DD	3186639787
	DD	3211559774
	DD	3002280148
	DD	1056964608
	DD	3183612120
	DD	3211397543
	DD	854270828
	DD	1056964608
	DD	3180617215
	DD	3211226072
	DD	851194761
	DD	1056964608
	DD	3176031322
	DD	3211045465
	DD	2971273466
	DD	1056964608
	DD	3169472868
	DD	3210855832
	DD	854062117
	DD	1056964608
	DD	3152590408
	DD	3210657285
	DD	2999384403
	DD	1056964608
	DD	1013387058
	DD	3210449946
	DD	2988649928
	DD	1056964608
	DD	1024416170
	DD	3210233939
	DD	847077312
	DD	1056964608
	DD	1029938589
	DD	3210009393
	DD	2995841562
	DD	1056964608
	DD	1033584979
	DD	3209776445
	DD	2953596676
	DD	1056964608
	DD	1036254719
	DD	3209535234
	DD	2994630888
	DD	1056964608
	DD	1038876298
	DD	3209285906
	DD	2996381248
	DD	1056964608
	DD	1040817765
	DD	3209028611
	DD	2997965172
	DD	1056964608
	DD	1042078039
	DD	3208763504
	DD	2998925687
	DD	1056964608
	DD	1043311911
	DD	3208490745
	DD	2979703788
	DD	1056964608
	DD	1044518635
	DD	3208210498
	DD	847314951
	DD	1056964608
	DD	3197498906
	DD	3207922931
	DD	2991581050
	DD	1065353216
	DD	3196923773
	DD	3207628219
	DD	850275634
	DD	1065353216
	DD	3196363278
	DD	3207326538
	DD	850866486
	DD	1065353216
	DD	3195575871
	DD	3207018070
	DD	839091011
	DD	1065353216
	DD	3194515443
	DD	3206703001
	DD	2988516283
	DD	1065353216
	DD	3193486263
	DD	3206381521
	DD	2995913996
	DD	1065353216
	DD	3192488951
	DD	3206053824
	DD	835167219
	DD	1065353216
	DD	3191524108
	DD	3205720106
	DD	2999832878
	DD	1065353216
	DD	3190592315
	DD	3205380570
	DD	843723495
	DD	1065353216
	DD	3189694133
	DD	3205035419
	DD	851331918
	DD	1065353216
	DD	3188830103
	DD	3204684861
	DD	844865538
	DD	1065353216
	DD	3188000746
	DD	3204209959
	DD	830532777
	DD	1065353216
	DD	3186742084
	DD	3203488490
	DD	838803769
	DD	1065353216
	DD	3185225016
	DD	3202757493
	DD	836355956
	DD	1065353216
	DD	3183780163
	DD	3202017408
	DD	2988316108
	DD	1065353216
	DD	3182408396
	DD	3201268682
	DD	2976529251
	DD	1065353216
	DD	3181110540
	DD	3200511765
	DD	2983580972
	DD	1065353216
	DD	3179887378
	DD	3199747114
	DD	846224294
	DD	1065353216
	DD	3178196862
	DD	3198975188
	DD	841305602
	DD	1065353216
	DD	3176053642
	DD	3198196453
	DD	842959264
	DD	1065353216
	DD	3174063957
	DD	3197411377
	DD	2993510896
	DD	1065353216
	DD	3172229004
	DD	3196620435
	DD	2972482974
	DD	1065353216
	DD	3170205956
	DD	3195588556
	DD	823907357
	DD	1065353216
	DD	3167161428
	DD	3193986067
	DD	2980570358
	DD	1065353216
	DD	3164432432
	DD	3192374722
	DD	820352637
	DD	1065353216
	DD	3161536011
	DD	3190755490
	DD	2970707961
	DD	1065353216
	DD	3157349634
	DD	3189129347
	DD	2984830484
	DD	1065353216
	DD	3153489468
	DD	3187323507
	DD	823486671
	DD	1065353216
	DD	3147680113
	DD	3184049462
	DD	813011701
	DD	1065353216
	DD	3140588184
	DD	3180767493
	DD	827530849
	DD	1065353216
	DD	3130909128
	DD	3175676720
	DD	820978303
	DD	1065353216
	DD	3114133471
	DD	3167292080
	DD	805685656
	DD	1065353216
	DD	2147483647
	DD	2147483647
	DD	2147483647
	DD	2147483647
	DD	2147483647
	DD	2147483647
	DD	2147483647
	DD	2147483647
	DD	2147483647
	DD	2147483647
	DD	2147483647
	DD	2147483647
	DD	2147483647
	DD	2147483647
	DD	2147483647
	DD	2147483647
	DD	1176256512
	DD	1176256512
	DD	1176256512
	DD	1176256512
	DD	1176256512
	DD	1176256512
	DD	1176256512
	DD	1176256512
	DD	1176256512
	DD	1176256512
	DD	1176256512
	DD	1176256512
	DD	1176256512
	DD	1176256512
	DD	1176256512
	DD	1176256512
	DD	2139095040
	DD	2139095040
	DD	2139095040
	DD	2139095040
	DD	2139095040
	DD	2139095040
	DD	2139095040
	DD	2139095040
	DD	2139095040
	DD	2139095040
	DD	2139095040
	DD	2139095040
	DD	2139095040
	DD	2139095040
	DD	2139095040
	DD	2139095040
	DD	3190467243
	DD	3190467243
	DD	3190467243
	DD	3190467243
	DD	3190467243
	DD	3190467243
	DD	3190467243
	DD	3190467243
	DD	3190467243
	DD	3190467243
	DD	3190467243
	DD	3190467243
	DD	3190467243
	DD	3190467243
	DD	3190467243
	DD	3190467243
	DD	1007192156
	DD	1007192156
	DD	1007192156
	DD	1007192156
	DD	1007192156
	DD	1007192156
	DD	1007192156
	DD	1007192156
	DD	1007192156
	DD	1007192156
	DD	1007192156
	DD	1007192156
	DD	1007192156
	DD	1007192156
	DD	1007192156
	DD	1007192156
	DD	3204448256
	DD	3204448256
	DD	3204448256
	DD	3204448256
	DD	3204448256
	DD	3204448256
	DD	3204448256
	DD	3204448256
	DD	3204448256
	DD	3204448256
	DD	3204448256
	DD	3204448256
	DD	3204448256
	DD	3204448256
	DD	3204448256
	DD	3204448256
	DD	1026206332
	DD	1026206332
	DD	1026206332
	DD	1026206332
	DD	1026206332
	DD	1026206332
	DD	1026206332
	DD	1026206332
	DD	1026206332
	DD	1026206332
	DD	1026206332
	DD	1026206332
	DD	1026206332
	DD	1026206332
	DD	1026206332
	DD	1026206332
	DD	1262485504
	DD	1262485504
	DD	1262485504
	DD	1262485504
	DD	1262485504
	DD	1262485504
	DD	1262485504
	DD	1262485504
	DD	1262485504
	DD	1262485504
	DD	1262485504
	DD	1262485504
	DD	1262485504
	DD	1262485504
	DD	1262485504
	DD	1262485504
	DD	255
	DD	255
	DD	255
	DD	255
	DD	255
	DD	255
	DD	255
	DD	255
	DD	255
	DD	255
	DD	255
	DD	255
	DD	255
	DD	255
	DD	255
	DD	255
	DD	1109588355
	DD	1109588355
	DD	1109588355
	DD	1109588355
	DD	1109588355
	DD	1109588355
	DD	1109588355
	DD	1109588355
	DD	1109588355
	DD	1109588355
	DD	1109588355
	DD	1109588355
	DD	1109588355
	DD	1109588355
	DD	1109588355
	DD	1109588355
	DD	3167293403
	DD	3167293403
	DD	3167293403
	DD	3167293403
	DD	3167293403
	DD	3167293403
	DD	3167293403
	DD	3167293403
	DD	3167293403
	DD	3167293403
	DD	3167293403
	DD	3167293403
	DD	3167293403
	DD	3167293403
	DD	3167293403
	DD	3167293403
	DD	809222144
	DD	809222144
	DD	809222144
	DD	809222144
	DD	809222144
	DD	809222144
	DD	809222144
	DD	809222144
	DD	809222144
	DD	809222144
	DD	809222144
	DD	809222144
	DD	809222144
	DD	809222144
	DD	809222144
	DD	809222144
	DD	2838781952
	DD	2838781952
	DD	2838781952
	DD	2838781952
	DD	2838781952
	DD	2838781952
	DD	2838781952
	DD	2838781952
	DD	2838781952
	DD	2838781952
	DD	2838781952
	DD	2838781952
	DD	2838781952
	DD	2838781952
	DD	2838781952
	DD	2838781952
	DD	2710384946
	DD	2710384946
	DD	2710384946
	DD	2710384946
	DD	2710384946
	DD	2710384946
	DD	2710384946
	DD	2710384946
	DD	2710384946
	DD	2710384946
	DD	2710384946
	DD	2710384946
	DD	2710384946
	DD	2710384946
	DD	2710384946
	DD	2710384946
	DD	1073741824
	DD	1073741824
	DD	1073741824
	DD	1073741824
	DD	1073741824
	DD	1073741824
	DD	1073741824
	DD	1073741824
	DD	1073741824
	DD	1073741824
	DD	1073741824
	DD	1073741824
	DD	1073741824
	DD	1073741824
	DD	1073741824
	DD	1073741824
	DD	1050868099
	DD	1050868099
	DD	1050868099
	DD	1050868099
	DD	1050868099
	DD	1050868099
	DD	1050868099
	DD	1050868099
	DD	1050868099
	DD	1050868099
	DD	1050868099
	DD	1050868099
	DD	1050868099
	DD	1050868099
	DD	1050868099
	DD	1050868099
	DD	1413742592
	DD	1074340347
	DD	1413742592
	DD	1074340347
	DD	1413742592
	DD	1074340347
	DD	1413742592
	DD	1074340347
	DD	1413742592
	DD	1074340347
	DD	1413742592
	DD	1074340347
	DD	1413742592
	DD	1074340347
	DD	1413742592
	DD	1074340347
	DD	1280075305
	DD	1033276451
	DD	1280075305
	DD	1033276451
	DD	1280075305
	DD	1033276451
	DD	1280075305
	DD	1033276451
	DD	1280075305
	DD	1033276451
	DD	1280075305
	DD	1033276451
	DD	1280075305
	DD	1033276451
	DD	1280075305
	DD	1033276451
	DD	3162727815
	DD	3217380692
	DD	3162727815
	DD	3217380692
	DD	3162727815
	DD	3217380692
	DD	3162727815
	DD	3217380692
	DD	3162727815
	DD	3217380692
	DD	3162727815
	DD	3217380692
	DD	3162727815
	DD	3217380692
	DD	3162727815
	DD	3217380692
	DD	939838102
	DD	1065423085
	DD	939838102
	DD	1065423085
	DD	939838102
	DD	1065423085
	DD	939838102
	DD	1065423085
	DD	939838102
	DD	1065423085
	DD	939838102
	DD	1065423085
	DD	939838102
	DD	1065423085
	DD	939838102
	DD	1065423085
	DD	4003935331
	DD	3207198463
	DD	4003935331
	DD	3207198463
	DD	4003935331
	DD	3207198463
	DD	4003935331
	DD	3207198463
	DD	4003935331
	DD	3207198463
	DD	4003935331
	DD	3207198463
	DD	4003935331
	DD	3207198463
	DD	4003935331
	DD	3207198463
	DD	239893998
	DD	1053154271
	DD	239893998
	DD	1053154271
	DD	239893998
	DD	1053154271
	DD	239893998
	DD	1053154271
	DD	239893998
	DD	1053154271
	DD	239893998
	DD	1053154271
	DD	239893998
	DD	1053154271
	DD	239893998
	DD	1053154271
_2il0floatpacket_20	DD	07f800000H,07f800000H,07f800000H,07f800000H,07f800000H,07f800000H,07f800000H,07f800000H
_2il0floatpacket_21	DD	0007fffffH,0007fffffH,0007fffffH,0007fffffH,0007fffffH,0007fffffH,0007fffffH,0007fffffH
_2il0floatpacket_22	DD	000800000H,000800000H,000800000H,000800000H,000800000H,000800000H,000800000H,000800000H
_2il0floatpacket_23	DD	00000ffffH,00000ffffH,00000ffffH,00000ffffH,00000ffffH,00000ffffH,00000ffffH,00000ffffH
_2il0floatpacket_24	DD	080000000H,080000000H,080000000H,080000000H,080000000H,080000000H,080000000H,080000000H
_2il0floatpacket_25	DD	03f800000H,03f800000H,03f800000H,03f800000H,03f800000H,03f800000H,03f800000H,03f800000H
_2il0floatpacket_26	DD	047400000H,047400000H,047400000H,047400000H,047400000H,047400000H,047400000H,047400000H
_2il0floatpacket_27	DD	028800000H,028800000H,028800000H,028800000H,028800000H,028800000H,028800000H,028800000H
_2il0floatpacket_28	DD	00003ffffH,00003ffffH,00003ffffH,00003ffffH,00003ffffH,00003ffffH,00003ffffH,00003ffffH
_2il0floatpacket_29	DD	034000000H,034000000H,034000000H,034000000H,034000000H,034000000H,034000000H,034000000H
_2il0floatpacket_30	DD	0000001ffH,0000001ffH,0000001ffH,0000001ffH,0000001ffH,0000001ffH,0000001ffH,0000001ffH
_2il0floatpacket_31	DD	040c90fdbH,040c90fdbH,040c90fdbH,040c90fdbH,040c90fdbH,040c90fdbH,040c90fdbH,040c90fdbH
_2il0floatpacket_32	DD	0b43bbd2eH,0b43bbd2eH,0b43bbd2eH,0b43bbd2eH,0b43bbd2eH,0b43bbd2eH,0b43bbd2eH,0b43bbd2eH
_2il0floatpacket_33	DD	07fffffffH,07fffffffH,07fffffffH,07fffffffH,07fffffffH,07fffffffH,07fffffffH,07fffffffH
_2il0floatpacket_34	DD	035800000H,035800000H,035800000H,035800000H,035800000H,035800000H,035800000H,035800000H
_2il0floatpacket_35	DD	0000000ffH,0000000ffH,0000000ffH,0000000ffH,0000000ffH,0000000ffH,0000000ffH,0000000ffH
_2il0floatpacket_43	DD	040c91000H,040c91000H,040c91000H,040c91000H,040c91000H,040c91000H,040c91000H,040c91000H
_2il0floatpacket_44	DD	0b795777aH,0b795777aH,0b795777aH,0b795777aH,0b795777aH,0b795777aH,0b795777aH,0b795777aH
_2il0floatpacket_45	DD	0fffff000H,0fffff000H,0fffff000H,0fffff000H,0fffff000H,0fffff000H,0fffff000H,0fffff000H
_2il0floatpacket_36	DD	047400000H,047400000H,047400000H,047400000H
_2il0floatpacket_37	DD	040c90fdbH,040c90fdbH,040c90fdbH,040c90fdbH
_2il0floatpacket_38	DD	040c91000H,040c91000H,040c91000H,040c91000H
_2il0floatpacket_39	DD	0b795777aH,0b795777aH,0b795777aH,0b795777aH
_2il0floatpacket_40	DD	0fffff000H,0fffff000H,0fffff000H,0fffff000H
_2il0floatpacket_41	DD	07fffffffH,07fffffffH,07fffffffH,07fffffffH
_2il0floatpacket_42	DD	035800000H,035800000H,035800000H,035800000H
_2il0floatpacket_46	DD	07f800000H,07f800000H,07f800000H,07f800000H
_2il0floatpacket_47	DD	0007fffffH,0007fffffH,0007fffffH,0007fffffH
_2il0floatpacket_48	DD	000800000H,000800000H,000800000H,000800000H
_2il0floatpacket_49	DD	00000ffffH,00000ffffH,00000ffffH,00000ffffH
_2il0floatpacket_50	DD	080000000H,080000000H,080000000H,080000000H
_2il0floatpacket_51	DD	03f800000H,03f800000H,03f800000H,03f800000H
_2il0floatpacket_52	DD	028800000H,028800000H,028800000H,028800000H
_2il0floatpacket_53	DD	00003ffffH,00003ffffH,00003ffffH,00003ffffH
_2il0floatpacket_54	DD	034000000H,034000000H,034000000H,034000000H
_2il0floatpacket_55	DD	0000001ffH,0000001ffH,0000001ffH,0000001ffH
_2il0floatpacket_56	DD	0b43bbd2eH,0b43bbd2eH,0b43bbd2eH,0b43bbd2eH
_2il0floatpacket_57	DD	0000000ffH,0000000ffH,0000000ffH,0000000ffH
_2il0floatpacket_58	DD	0ffffffffH,000000000H,0ffffffffH,000000000H
_vmlsSinHATab	DD	0
	DD	2139095040
_RDATA	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS
EXTRN	__ImageBase:PROC
EXTRN	_fltused:BYTE
	ENDIF
	END
