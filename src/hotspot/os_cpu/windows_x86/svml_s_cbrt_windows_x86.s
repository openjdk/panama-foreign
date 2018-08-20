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
	PUBLIC __svml_cbrtf4_ha_l9

__svml_cbrtf4_ha_l9	PROC

_B1_1::

        DB        243
        DB        15
        DB        30
        DB        250
L1::

        sub       rsp, 248
        lea       rdx, QWORD PTR [__ImageBase]
        vmovups   XMMWORD PTR [208+rsp], xmm13
        vmovups   XMMWORD PTR [192+rsp], xmm7
        mov       QWORD PTR [224+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovups   xmm13, XMMWORD PTR [rcx]
        and       r13, -64
        vpsrld    xmm4, xmm13, 16
        vpand     xmm2, xmm4, XMMWORD PTR [__svml_scbrt_ha_data_internal+1408]
        vmovd     eax, xmm2
        vpextrd   r8d, xmm2, 1
        movsxd    rax, eax
        movsxd    r8, r8d
        vpextrd   r9d, xmm2, 2
        vpextrd   r10d, xmm2, 3
        movsxd    r9, r9d
        movsxd    r10, r10d
        vmovd     xmm1, DWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+rdx+rax]
        vmovd     xmm3, DWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+rdx+r8]
        vpunpcklqdq xmm0, xmm1, xmm3
        vmovd     xmm7, DWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+rdx+r9]
        vmovd     xmm5, DWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+rdx+r10]
        vandps    xmm3, xmm13, XMMWORD PTR [__svml_scbrt_ha_data_internal+1792]
        vpunpcklqdq xmm1, xmm7, xmm5
        vpsubd    xmm7, xmm3, XMMWORD PTR [__svml_scbrt_ha_data_internal+1856]
        vpcmpgtd  xmm3, xmm7, XMMWORD PTR [__svml_scbrt_ha_data_internal+1920]
        vandps    xmm7, xmm13, XMMWORD PTR [__svml_scbrt_ha_data_internal+1152]
        vandps    xmm5, xmm13, XMMWORD PTR [__svml_scbrt_ha_data_internal+1216]
        vorps     xmm7, xmm7, XMMWORD PTR [__svml_scbrt_ha_data_internal+1280]
        vorps     xmm5, xmm5, XMMWORD PTR [__svml_scbrt_ha_data_internal+1344]
        vshufps   xmm0, xmm0, xmm1, 136
        vpsrld    xmm1, xmm4, 7
        vpand     xmm4, xmm1, XMMWORD PTR [__svml_scbrt_ha_data_internal+1472]
        vsubps    xmm7, xmm7, xmm5
        vmovmskps eax, xmm3
        vpand     xmm5, xmm1, XMMWORD PTR [__svml_scbrt_ha_data_internal+1536]
        vmulps    xmm7, xmm0, xmm7
        vpmulld   xmm0, xmm4, XMMWORD PTR [__svml_scbrt_ha_data_internal+1728]
        vpsrld    xmm0, xmm0, 12
        vpaddd    xmm1, xmm0, XMMWORD PTR [__svml_scbrt_ha_data_internal+1600]
        vpsubd    xmm4, xmm4, XMMWORD PTR [__svml_scbrt_ha_data_internal+1664]
        vpor      xmm1, xmm1, xmm5
        vpslld    xmm5, xmm1, 23
        vpsubd    xmm1, xmm4, xmm0
        vpsubd    xmm4, xmm1, xmm0
        vpsubd    xmm0, xmm4, xmm0
        vpslld    xmm0, xmm0, 7
        vpaddd    xmm2, xmm2, xmm0
        vpandn    xmm3, xmm3, xmm2
        vpslld    xmm0, xmm3, 1
        vmovd     r11d, xmm0
        vpextrd   ecx, xmm0, 1
        vpextrd   r8d, xmm0, 2
        vpextrd   r9d, xmm0, 3
        movsxd    r11, r11d
        movsxd    rcx, ecx
        movsxd    r8, r8d
        movsxd    r9, r9d
        vmovq     xmm1, QWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+128+rdx+r11]
        vmovq     xmm2, QWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+128+rdx+rcx]
        vmovq     xmm3, QWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+128+rdx+r8]
        vmovq     xmm4, QWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+128+rdx+r9]
        vunpcklps xmm0, xmm1, xmm2
        vunpcklps xmm3, xmm3, xmm4
        vmovlhps  xmm1, xmm0, xmm3
        vshufps   xmm2, xmm0, xmm3, 238
        vmulps    xmm3, xmm5, xmm1
        vmulps    xmm0, xmm5, xmm2
        vmovups   xmm5, XMMWORD PTR [__svml_scbrt_ha_data_internal+896]
        vfmadd213ps xmm5, xmm7, XMMWORD PTR [__svml_scbrt_ha_data_internal+960]
        vfmadd213ps xmm5, xmm7, XMMWORD PTR [__svml_scbrt_ha_data_internal+1024]
        vfmadd213ps xmm5, xmm7, XMMWORD PTR [__svml_scbrt_ha_data_internal+1088]
        vmulps    xmm7, xmm7, xmm3
        vmulps    xmm1, xmm5, xmm7
        mov       QWORD PTR [232+rsp], r13
        vaddps    xmm2, xmm0, xmm1
        vaddps    xmm0, xmm3, xmm2
        test      eax, eax
        jne       _B1_3

_B1_2::

        vmovups   xmm7, XMMWORD PTR [192+rsp]
        vmovups   xmm13, XMMWORD PTR [208+rsp]
        mov       r13, QWORD PTR [224+rsp]
        add       rsp, 248
        ret

_B1_3::

        vmovups   XMMWORD PTR [r13], xmm13
        vmovups   XMMWORD PTR [64+r13], xmm0

_B1_6::

        xor       edx, edx
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, edx
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, eax

_B1_7::

        bt        esi, ebx
        jc        _B1_10

_B1_8::

        inc       ebx
        cmp       ebx, 4
        jl        _B1_7

_B1_9::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        vmovups   xmm0, XMMWORD PTR [64+r13]
        jmp       _B1_2

_B1_10::

        lea       rcx, QWORD PTR [r13+rbx*4]
        lea       rdx, QWORD PTR [64+r13+rbx*4]

        call      __svml_scbrt_ha_cout_rare_internal
        jmp       _B1_8
        ALIGN     16

_B1_11::

__svml_cbrtf4_ha_l9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_cbrtf4_ha_l9_B1_B3:
	DD	535553
	DD	1889324
	DD	817188
	DD	907291
	DD	2031883

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B1_1
	DD	imagerel _B1_6
	DD	imagerel _unwind___svml_cbrtf4_ha_l9_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_cbrtf4_ha_l9_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B1_1
	DD	imagerel _B1_6
	DD	imagerel _unwind___svml_cbrtf4_ha_l9_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B1_6
	DD	imagerel _B1_11
	DD	imagerel _unwind___svml_cbrtf4_ha_l9_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST1:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_cbrtf4_ha_ex

__svml_cbrtf4_ha_ex	PROC

_B2_1::

        DB        243
        DB        15
        DB        30
        DB        250
L12::

        sub       rsp, 264
        lea       rdx, QWORD PTR [__ImageBase]
        movups    XMMWORD PTR [192+rsp], xmm15
        movups    XMMWORD PTR [208+rsp], xmm10
        movups    XMMWORD PTR [224+rsp], xmm9
        mov       QWORD PTR [240+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        movups    xmm5, XMMWORD PTR [rcx]
        and       r13, -64
        movaps    xmm2, xmm5
        movdqu    xmm1, XMMWORD PTR [__svml_scbrt_ha_data_internal+1408]
        psrld     xmm2, 16
        pand      xmm1, xmm2
        psrld     xmm2, 7
        pshufd    xmm0, xmm1, 1
        movd      eax, xmm1
        pshufd    xmm9, xmm1, 2
        movups    xmm3, XMMWORD PTR [__svml_scbrt_ha_data_internal+1152]
        movd      r8d, xmm0
        andps     xmm3, xmm5
        pshufd    xmm0, xmm1, 3
        movups    xmm15, XMMWORD PTR [__svml_scbrt_ha_data_internal+1216]
        movd      r9d, xmm9
        andps     xmm15, xmm5
        movd      r10d, xmm0
        orps      xmm3, XMMWORD PTR [__svml_scbrt_ha_data_internal+1280]
        orps      xmm15, XMMWORD PTR [__svml_scbrt_ha_data_internal+1344]
        movsxd    rax, eax
        subps     xmm3, xmm15
        movsxd    r8, r8d
        movsxd    r9, r9d
        movsxd    r10, r10d
        movd      xmm4, DWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+rdx+rax]
        movd      xmm10, DWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+rdx+r8]
        punpcklqdq xmm4, xmm10
        movd      xmm9, DWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+rdx+r9]
        movd      xmm10, DWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+rdx+r10]
        punpcklqdq xmm9, xmm10
        shufps    xmm4, xmm9, 136
        movdqu    xmm0, XMMWORD PTR [__svml_scbrt_ha_data_internal+1472]
        mulps     xmm4, xmm3
        pand      xmm0, xmm2
        movdqu    xmm3, XMMWORD PTR [__svml_scbrt_ha_data_internal+1728]
        movdqa    xmm15, xmm0
        movdqa    xmm10, xmm3
        psrlq     xmm3, 32
        pmuludq   xmm10, xmm0
        psrlq     xmm0, 32
        pmuludq   xmm0, xmm3
        pand      xmm10, XMMWORD PTR [_2il0floatpacket_37]
        psllq     xmm0, 32
        por       xmm10, xmm0
        psubd     xmm15, XMMWORD PTR [__svml_scbrt_ha_data_internal+1664]
        psrld     xmm10, 12
        psubd     xmm15, xmm10
        movdqu    xmm9, XMMWORD PTR [__svml_scbrt_ha_data_internal+1792]
        psubd     xmm15, xmm10
        pand      xmm9, xmm5
        psubd     xmm15, xmm10
        psubd     xmm9, XMMWORD PTR [__svml_scbrt_ha_data_internal+1856]
        pslld     xmm15, 7
        pcmpgtd   xmm9, XMMWORD PTR [__svml_scbrt_ha_data_internal+1920]
        paddd     xmm1, xmm15
        movmskps  eax, xmm9
        pandn     xmm9, xmm1
        movdqu    xmm3, XMMWORD PTR [__svml_scbrt_ha_data_internal+1600]
        pslld     xmm9, 1
        pand      xmm2, XMMWORD PTR [__svml_scbrt_ha_data_internal+1536]
        paddd     xmm3, xmm10
        por       xmm3, xmm2
        pshufd    xmm2, xmm9, 1
        pslld     xmm3, 23
        movd      r11d, xmm9
        pshufd    xmm10, xmm9, 2
        pshufd    xmm0, xmm9, 3
        movd      ecx, xmm2
        movd      r8d, xmm10
        movd      r9d, xmm0
        movsxd    r11, r11d
        movsxd    rcx, ecx
        movsxd    r8, r8d
        movsxd    r9, r9d
        movq      xmm1, QWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+128+rdx+r11]
        movq      xmm15, QWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+128+rdx+rcx]
        movq      xmm10, QWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+128+rdx+r8]
        movq      xmm2, QWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+128+rdx+r9]
        unpcklps  xmm1, xmm15
        unpcklps  xmm10, xmm2
        movaps    xmm0, xmm1
        movlhps   xmm0, xmm10
        shufps    xmm1, xmm10, 238
        mulps     xmm0, xmm3
        mulps     xmm3, xmm1
        movups    xmm1, XMMWORD PTR [__svml_scbrt_ha_data_internal+896]
        mulps     xmm1, xmm4
        mov       QWORD PTR [248+rsp], r13
        addps     xmm1, XMMWORD PTR [__svml_scbrt_ha_data_internal+960]
        mulps     xmm1, xmm4
        addps     xmm1, XMMWORD PTR [__svml_scbrt_ha_data_internal+1024]
        mulps     xmm1, xmm4
        mulps     xmm4, xmm0
        addps     xmm1, XMMWORD PTR [__svml_scbrt_ha_data_internal+1088]
        mulps     xmm1, xmm4
        addps     xmm3, xmm1
        addps     xmm0, xmm3
        test      eax, eax
        jne       _B2_3

_B2_2::

        movups    xmm9, XMMWORD PTR [224+rsp]
        movups    xmm10, XMMWORD PTR [208+rsp]
        movups    xmm15, XMMWORD PTR [192+rsp]
        mov       r13, QWORD PTR [240+rsp]
        add       rsp, 264
        ret

_B2_3::

        movups    XMMWORD PTR [r13], xmm5
        movups    XMMWORD PTR [64+r13], xmm0

_B2_6::

        xor       ecx, ecx
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, ecx
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, eax

_B2_7::

        mov       ecx, ebx
        mov       edx, 1
        shl       edx, cl
        test      esi, edx
        jne       _B2_10

_B2_8::

        inc       ebx
        cmp       ebx, 4
        jl        _B2_7

_B2_9::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        movups    xmm0, XMMWORD PTR [64+r13]
        jmp       _B2_2

_B2_10::

        lea       rcx, QWORD PTR [r13+rbx*4]
        lea       rdx, QWORD PTR [64+r13+rbx*4]

        call      __svml_scbrt_ha_cout_rare_internal
        jmp       _B2_8
        ALIGN     16

_B2_11::

__svml_cbrtf4_ha_ex ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_cbrtf4_ha_ex_B1_B3:
	DD	668929
	DD	2020405
	DD	956461
	DD	895012
	DD	849947
	DD	2162955

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B2_1
	DD	imagerel _B2_6
	DD	imagerel _unwind___svml_cbrtf4_ha_ex_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_cbrtf4_ha_ex_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B2_1
	DD	imagerel _B2_6
	DD	imagerel _unwind___svml_cbrtf4_ha_ex_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B2_6
	DD	imagerel _B2_11
	DD	imagerel _unwind___svml_cbrtf4_ha_ex_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST2:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_cbrtf4_ha_e9

__svml_cbrtf4_ha_e9	PROC

_B3_1::

        DB        243
        DB        15
        DB        30
        DB        250
L25::

        sub       rsp, 248
        lea       rdx, QWORD PTR [__ImageBase]
        vmovups   XMMWORD PTR [192+rsp], xmm8
        vmovups   XMMWORD PTR [208+rsp], xmm7
        mov       QWORD PTR [224+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovups   xmm8, XMMWORD PTR [rcx]
        and       r13, -64
        vpsrld    xmm4, xmm8, 16
        vpand     xmm2, xmm4, XMMWORD PTR [__svml_scbrt_ha_data_internal+1408]
        vmovd     eax, xmm2
        vpextrd   r8d, xmm2, 1
        movsxd    rax, eax
        movsxd    r8, r8d
        vpextrd   r9d, xmm2, 2
        vpextrd   r10d, xmm2, 3
        movsxd    r9, r9d
        movsxd    r10, r10d
        vmovd     xmm1, DWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+rdx+rax]
        vmovd     xmm3, DWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+rdx+r8]
        vpunpcklqdq xmm0, xmm1, xmm3
        vmovd     xmm5, DWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+rdx+r9]
        vmovd     xmm7, DWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+rdx+r10]
        vandps    xmm3, xmm8, XMMWORD PTR [__svml_scbrt_ha_data_internal+1792]
        vpunpcklqdq xmm1, xmm5, xmm7
        vpsubd    xmm5, xmm3, XMMWORD PTR [__svml_scbrt_ha_data_internal+1856]
        vpcmpgtd  xmm3, xmm5, XMMWORD PTR [__svml_scbrt_ha_data_internal+1920]
        vandps    xmm5, xmm8, XMMWORD PTR [__svml_scbrt_ha_data_internal+1152]
        vandps    xmm7, xmm8, XMMWORD PTR [__svml_scbrt_ha_data_internal+1216]
        vorps     xmm5, xmm5, XMMWORD PTR [__svml_scbrt_ha_data_internal+1280]
        vorps     xmm7, xmm7, XMMWORD PTR [__svml_scbrt_ha_data_internal+1344]
        vshufps   xmm0, xmm0, xmm1, 136
        vpsrld    xmm1, xmm4, 7
        vpand     xmm4, xmm1, XMMWORD PTR [__svml_scbrt_ha_data_internal+1472]
        vsubps    xmm5, xmm5, xmm7
        vmovmskps eax, xmm3
        vmulps    xmm7, xmm0, xmm5
        vpmulld   xmm0, xmm4, XMMWORD PTR [__svml_scbrt_ha_data_internal+1728]
        vpsrld    xmm0, xmm0, 12
        vpand     xmm5, xmm1, XMMWORD PTR [__svml_scbrt_ha_data_internal+1536]
        vpaddd    xmm1, xmm0, XMMWORD PTR [__svml_scbrt_ha_data_internal+1600]
        vpsubd    xmm4, xmm4, XMMWORD PTR [__svml_scbrt_ha_data_internal+1664]
        vpor      xmm1, xmm1, xmm5
        vpslld    xmm5, xmm1, 23
        vpsubd    xmm1, xmm4, xmm0
        vpsubd    xmm4, xmm1, xmm0
        vpsubd    xmm0, xmm4, xmm0
        vpslld    xmm0, xmm0, 7
        vpaddd    xmm2, xmm2, xmm0
        vpandn    xmm3, xmm3, xmm2
        vpslld    xmm3, xmm3, 1
        vmovd     r11d, xmm3
        vpextrd   ecx, xmm3, 1
        vpextrd   r8d, xmm3, 2
        vpextrd   r9d, xmm3, 3
        movsxd    r11, r11d
        movsxd    rcx, ecx
        movsxd    r8, r8d
        movsxd    r9, r9d
        vmovq     xmm0, QWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+128+rdx+r11]
        vmovq     xmm1, QWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+128+rdx+rcx]
        vmovq     xmm2, QWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+128+rdx+r8]
        vmovq     xmm4, QWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+128+rdx+r9]
        vunpcklps xmm3, xmm0, xmm1
        vunpcklps xmm2, xmm2, xmm4
        vmovlhps  xmm0, xmm3, xmm2
        vshufps   xmm1, xmm3, xmm2, 238
        vmulps    xmm2, xmm7, XMMWORD PTR [__svml_scbrt_ha_data_internal+896]
        vmulps    xmm3, xmm5, xmm0
        vmulps    xmm5, xmm5, xmm1
        vaddps    xmm4, xmm2, XMMWORD PTR [__svml_scbrt_ha_data_internal+960]
        vmulps    xmm0, xmm7, xmm4
        mov       QWORD PTR [232+rsp], r13
        vaddps    xmm1, xmm0, XMMWORD PTR [__svml_scbrt_ha_data_internal+1024]
        vmulps    xmm2, xmm7, xmm1
        vmulps    xmm7, xmm7, xmm3
        vaddps    xmm4, xmm2, XMMWORD PTR [__svml_scbrt_ha_data_internal+1088]
        vmulps    xmm0, xmm4, xmm7
        vaddps    xmm5, xmm5, xmm0
        vaddps    xmm0, xmm3, xmm5
        test      eax, eax
        jne       _B3_3

_B3_2::

        vmovups   xmm7, XMMWORD PTR [208+rsp]
        vmovups   xmm8, XMMWORD PTR [192+rsp]
        mov       r13, QWORD PTR [224+rsp]
        add       rsp, 248
        ret

_B3_3::

        vmovups   XMMWORD PTR [r13], xmm8
        vmovups   XMMWORD PTR [64+r13], xmm0

_B3_6::

        xor       edx, edx
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, edx
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, eax

_B3_7::

        bt        esi, ebx
        jc        _B3_10

_B3_8::

        inc       ebx
        cmp       ebx, 4
        jl        _B3_7

_B3_9::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        vmovups   xmm0, XMMWORD PTR [64+r13]
        jmp       _B3_2

_B3_10::

        lea       rcx, QWORD PTR [r13+rbx*4]
        lea       rdx, QWORD PTR [64+r13+rbx*4]

        call      __svml_scbrt_ha_cout_rare_internal
        jmp       _B3_8
        ALIGN     16

_B3_11::

__svml_cbrtf4_ha_e9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_cbrtf4_ha_e9_B1_B3:
	DD	535553
	DD	1889324
	DD	882724
	DD	821275
	DD	2031883

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B3_1
	DD	imagerel _B3_6
	DD	imagerel _unwind___svml_cbrtf4_ha_e9_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_cbrtf4_ha_e9_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B3_1
	DD	imagerel _B3_6
	DD	imagerel _unwind___svml_cbrtf4_ha_e9_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B3_6
	DD	imagerel _B3_11
	DD	imagerel _unwind___svml_cbrtf4_ha_e9_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST3:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_cbrtf8_ha_l9

__svml_cbrtf8_ha_l9	PROC

_B4_1::

        DB        243
        DB        15
        DB        30
        DB        250
L36::

        sub       rsp, 312
        lea       rdx, QWORD PTR [__ImageBase]
        vmovups   XMMWORD PTR [208+rsp], xmm15
        vmovups   XMMWORD PTR [224+rsp], xmm14
        vmovups   XMMWORD PTR [240+rsp], xmm13
        vmovups   XMMWORD PTR [256+rsp], xmm12
        vmovups   XMMWORD PTR [272+rsp], xmm11
        mov       QWORD PTR [288+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovups   ymm4, YMMWORD PTR [rcx]
        and       r13, -64
        vpsrld    ymm5, ymm4, 16
        vpand     ymm12, ymm5, YMMWORD PTR [__svml_scbrt_ha_data_internal+1408]
        mov       QWORD PTR [296+rsp], r13
        vmovd     eax, xmm12
        vextracti128 xmm11, ymm12, 1
        vpextrd   r8d, xmm12, 1
        movsxd    rax, eax
        movsxd    r8, r8d
        vmovd     r11d, xmm11
        vmovd     xmm2, DWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+rdx+rax]
        vpextrd   r9d, xmm12, 2
        vpextrd   r10d, xmm12, 3
        vmovd     xmm3, DWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+rdx+r8]
        movsxd    r9, r9d
        movsxd    r10, r10d
        vpextrd   eax, xmm11, 1
        vpextrd   ecx, xmm11, 2
        vpextrd   r8d, xmm11, 3
        movsxd    r11, r11d
        movsxd    rax, eax
        movsxd    rcx, ecx
        movsxd    r8, r8d
        vmovd     xmm0, DWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+rdx+r9]
        vmovd     xmm1, DWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+rdx+r10]
        vpunpcklqdq xmm2, xmm2, xmm3
        vpunpcklqdq xmm3, xmm0, xmm1
        vmovd     xmm14, DWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+rdx+r11]
        vmovd     xmm15, DWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+rdx+rax]
        vmovd     xmm13, DWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+rdx+rcx]
        vmovd     xmm0, DWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+rdx+r8]
        vshufps   xmm1, xmm2, xmm3, 136
        vpunpcklqdq xmm2, xmm14, xmm15
        vpunpcklqdq xmm3, xmm13, xmm0
        vshufps   xmm14, xmm2, xmm3, 136
        vpsrld    ymm3, ymm5, 7
        vpand     ymm0, ymm3, YMMWORD PTR [__svml_scbrt_ha_data_internal+1472]
        vpand     ymm3, ymm3, YMMWORD PTR [__svml_scbrt_ha_data_internal+1536]
        vandps    ymm5, ymm4, YMMWORD PTR [__svml_scbrt_ha_data_internal+1792]
        vandps    ymm15, ymm4, YMMWORD PTR [__svml_scbrt_ha_data_internal+1152]
        vandps    ymm11, ymm4, YMMWORD PTR [__svml_scbrt_ha_data_internal+1216]
        vinsertf128 ymm2, ymm1, xmm14, 1
        vpsubd    ymm14, ymm5, YMMWORD PTR [__svml_scbrt_ha_data_internal+1856]
        vorps     ymm1, ymm15, YMMWORD PTR [__svml_scbrt_ha_data_internal+1280]
        vpmulld   ymm15, ymm0, YMMWORD PTR [__svml_scbrt_ha_data_internal+1728]
        vorps     ymm5, ymm11, YMMWORD PTR [__svml_scbrt_ha_data_internal+1344]
        vpsrld    ymm11, ymm15, 12
        vpsubd    ymm0, ymm0, YMMWORD PTR [__svml_scbrt_ha_data_internal+1664]
        vpcmpgtd  ymm13, ymm14, YMMWORD PTR [__svml_scbrt_ha_data_internal+1920]
        vsubps    ymm14, ymm1, ymm5
        vpaddd    ymm1, ymm11, YMMWORD PTR [__svml_scbrt_ha_data_internal+1600]
        vmulps    ymm2, ymm2, ymm14
        vpsubd    ymm14, ymm0, ymm11
        vpor      ymm3, ymm1, ymm3
        vpsubd    ymm15, ymm14, ymm11
        vpslld    ymm3, ymm3, 23
        vpsubd    ymm11, ymm15, ymm11
        vpslld    ymm0, ymm11, 7
        vpaddd    ymm12, ymm12, ymm0
        vmovmskps eax, ymm13
        vpandn    ymm13, ymm13, ymm12
        vpslld    ymm11, ymm13, 1
        vmovd     r9d, xmm11
        vextracti128 xmm13, ymm11, 1
        movsxd    r9, r9d
        vpextrd   ecx, xmm11, 2
        vpextrd   r8d, xmm11, 3
        movsxd    rcx, ecx
        movsxd    r8, r8d
        vmovq     xmm14, QWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+128+rdx+r9]
        vmovd     r9d, xmm13
        vmovq     xmm15, QWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+128+rdx+rcx]
        vmovq     xmm1, QWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+128+rdx+r8]
        vpextrd   r10d, xmm11, 1
        vpextrd   r11d, xmm13, 1
        vpextrd   ecx, xmm13, 2
        vpextrd   r8d, xmm13, 3
        movsxd    r10, r10d
        movsxd    r9, r9d
        movsxd    r11, r11d
        movsxd    rcx, ecx
        movsxd    r8, r8d
        vmovq     xmm0, QWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+128+rdx+r10]
        vmovq     xmm5, QWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+128+rdx+r9]
        vmovq     xmm11, QWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+128+rdx+r11]
        vmovq     xmm12, QWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+128+rdx+rcx]
        vmovq     xmm13, QWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+128+rdx+r8]
        vunpcklps xmm15, xmm14, xmm15
        vunpcklps xmm14, xmm0, xmm1
        vunpcklps xmm0, xmm5, xmm12
        vunpcklps xmm1, xmm11, xmm13
        vinsertf128 ymm5, ymm15, xmm0, 1
        vinsertf128 ymm12, ymm14, xmm1, 1
        vunpcklps ymm0, ymm5, ymm12
        vunpckhps ymm1, ymm5, ymm12
        vmulps    ymm5, ymm3, ymm0
        vmulps    ymm1, ymm3, ymm1
        vmovups   ymm3, YMMWORD PTR [__svml_scbrt_ha_data_internal+896]
        vfmadd213ps ymm3, ymm2, YMMWORD PTR [__svml_scbrt_ha_data_internal+960]
        vfmadd213ps ymm3, ymm2, YMMWORD PTR [__svml_scbrt_ha_data_internal+1024]
        vfmadd213ps ymm3, ymm2, YMMWORD PTR [__svml_scbrt_ha_data_internal+1088]
        vmulps    ymm2, ymm2, ymm5
        vmulps    ymm2, ymm3, ymm2
        vaddps    ymm0, ymm1, ymm2
        vaddps    ymm0, ymm5, ymm0
        test      eax, eax
        jne       _B4_3

_B4_2::

        vmovups   xmm11, XMMWORD PTR [272+rsp]
        vmovups   xmm12, XMMWORD PTR [256+rsp]
        vmovups   xmm13, XMMWORD PTR [240+rsp]
        vmovups   xmm14, XMMWORD PTR [224+rsp]
        vmovups   xmm15, XMMWORD PTR [208+rsp]
        mov       r13, QWORD PTR [288+rsp]
        add       rsp, 312
        ret

_B4_3::

        vmovups   YMMWORD PTR [r13], ymm4
        vmovups   YMMWORD PTR [64+r13], ymm0

_B4_6::

        xor       edx, edx
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, edx
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, eax

_B4_7::

        bt        esi, ebx
        jc        _B4_10

_B4_8::

        inc       ebx
        cmp       ebx, 8
        jl        _B4_7

_B4_9::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        vmovups   ymm0, YMMWORD PTR [64+r13]
        jmp       _B4_2

_B4_10::

        vzeroupper
        lea       rcx, QWORD PTR [r13+rbx*4]
        lea       rdx, QWORD PTR [64+r13+rbx*4]

        call      __svml_scbrt_ha_cout_rare_internal
        jmp       _B4_8
        ALIGN     16

_B4_11::

__svml_cbrtf8_ha_l9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_cbrtf8_ha_l9_B1_B3:
	DD	935681
	DD	2413639
	DD	1161279
	DD	1099830
	DD	1038381
	DD	976932
	DD	915483
	DD	2556171

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B4_1
	DD	imagerel _B4_6
	DD	imagerel _unwind___svml_cbrtf8_ha_l9_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_cbrtf8_ha_l9_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B4_1
	DD	imagerel _B4_6
	DD	imagerel _unwind___svml_cbrtf8_ha_l9_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B4_6
	DD	imagerel _B4_11
	DD	imagerel _unwind___svml_cbrtf8_ha_l9_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST4:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_cbrtf8_ha_e9

__svml_cbrtf8_ha_e9	PROC

_B5_1::

        DB        243
        DB        15
        DB        30
        DB        250
L53::

        sub       rsp, 392
        lea       rdx, QWORD PTR [__ImageBase]
        vmovups   XMMWORD PTR [224+rsp], xmm15
        vmovups   XMMWORD PTR [240+rsp], xmm14
        vmovups   XMMWORD PTR [256+rsp], xmm13
        vmovups   XMMWORD PTR [272+rsp], xmm12
        vmovups   XMMWORD PTR [208+rsp], xmm11
        vmovups   XMMWORD PTR [288+rsp], xmm10
        vmovups   XMMWORD PTR [304+rsp], xmm9
        vmovups   XMMWORD PTR [336+rsp], xmm8
        vmovups   XMMWORD PTR [352+rsp], xmm7
        vmovups   XMMWORD PTR [320+rsp], xmm6
        mov       QWORD PTR [368+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovups   ymm15, YMMWORD PTR [rcx]
        and       r13, -64
        vmovups   xmm10, XMMWORD PTR [__svml_scbrt_ha_data_internal+1408]
        vmovups   xmm0, XMMWORD PTR [__svml_scbrt_ha_data_internal+1472]
        vmovups   xmm3, XMMWORD PTR [__svml_scbrt_ha_data_internal+1792]
        vmovups   xmm4, XMMWORD PTR [__svml_scbrt_ha_data_internal+1856]
        vmovups   xmm5, XMMWORD PTR [__svml_scbrt_ha_data_internal+1920]
        vmovups   xmm1, XMMWORD PTR [__svml_scbrt_ha_data_internal+1536]
        vmovups   xmm2, XMMWORD PTR [__svml_scbrt_ha_data_internal+1600]
        mov       QWORD PTR [376+rsp], r13
        vpsrld    xmm7, xmm15, 16
        vpand     xmm9, xmm7, xmm10
        vextractf128 xmm6, ymm15, 1
        vmovd     eax, xmm9
        vpsrld    xmm8, xmm6, 16
        movsxd    rax, eax
        vpand     xmm10, xmm8, xmm10
        vpextrd   r8d, xmm9, 1
        vpsrld    xmm8, xmm8, 7
        movsxd    r8, r8d
        vpextrd   r9d, xmm9, 2
        vpextrd   r10d, xmm9, 3
        vmovd     r11d, xmm10
        vmovd     xmm13, DWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+rdx+rax]
        vmovd     xmm14, DWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+rdx+r8]
        movsxd    r9, r9d
        movsxd    r10, r10d
        vpextrd   eax, xmm10, 1
        movsxd    r11, r11d
        movsxd    rax, eax
        vpextrd   ecx, xmm10, 2
        vpextrd   r8d, xmm10, 3
        movsxd    rcx, ecx
        movsxd    r8, r8d
        vmovd     xmm12, DWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+rdx+r9]
        vmovd     xmm11, DWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+rdx+r10]
        vpunpcklqdq xmm13, xmm13, xmm14
        vpunpcklqdq xmm14, xmm12, xmm11
        vshufps   xmm11, xmm13, xmm14, 136
        vmovd     xmm13, DWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+rdx+r11]
        vmovd     xmm12, DWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+rdx+rax]
        vpunpcklqdq xmm12, xmm13, xmm12
        vmovd     xmm13, DWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+rdx+rcx]
        vmovd     xmm14, DWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+rdx+r8]
        vpunpcklqdq xmm13, xmm13, xmm14
        vshufps   xmm14, xmm12, xmm13, 136
        vpsrld    xmm12, xmm7, 7
        vpand     xmm7, xmm12, xmm0
        vpand     xmm13, xmm8, xmm0
        vpand     xmm0, xmm15, xmm3
        vandps    xmm3, xmm6, xmm3
        vpsubd    xmm0, xmm0, xmm4
        vpsubd    xmm4, xmm3, xmm4
        vpcmpgtd  xmm0, xmm0, xmm5
        vpcmpgtd  xmm6, xmm4, xmm5
        vpackssdw xmm5, xmm0, xmm6
        vpand     xmm8, xmm8, xmm1
        vpxor     xmm3, xmm3, xmm3
        vpacksswb xmm4, xmm5, xmm3
        vandps    ymm5, ymm15, YMMWORD PTR [__svml_scbrt_ha_data_internal+1152]
        vpmovmskb eax, xmm4
        vorps     ymm3, ymm5, YMMWORD PTR [__svml_scbrt_ha_data_internal+1280]
        vinsertf128 ymm11, ymm11, xmm14, 1
        vandps    ymm14, ymm15, YMMWORD PTR [__svml_scbrt_ha_data_internal+1216]
        vorps     ymm4, ymm14, YMMWORD PTR [__svml_scbrt_ha_data_internal+1344]
        vsubps    ymm5, ymm3, ymm4
        vmovups   xmm4, XMMWORD PTR [__svml_scbrt_ha_data_internal+1728]
        vpmulld   xmm14, xmm13, xmm4
        vmulps    ymm3, ymm11, ymm5
        vpmulld   xmm11, xmm7, xmm4
        vpsrld    xmm4, xmm14, 12
        vpsrld    xmm5, xmm11, 12
        vpand     xmm14, xmm12, xmm1
        vpaddd    xmm1, xmm2, xmm4
        vpaddd    xmm12, xmm5, xmm2
        vpor      xmm2, xmm1, xmm8
        vpor      xmm11, xmm12, xmm14
        vpslld    xmm1, xmm2, 23
        vpslld    xmm14, xmm11, 23
        vmovups   xmm2, XMMWORD PTR [__svml_scbrt_ha_data_internal+1664]
        vpsubd    xmm7, xmm7, xmm2
        vpsubd    xmm2, xmm13, xmm2
        vpsubd    xmm7, xmm7, xmm5
        vpsubd    xmm12, xmm2, xmm4
        vpsubd    xmm8, xmm7, xmm5
        vpsubd    xmm13, xmm12, xmm4
        vpsubd    xmm5, xmm8, xmm5
        vpsubd    xmm4, xmm13, xmm4
        vpslld    xmm11, xmm5, 7
        vpaddd    xmm9, xmm9, xmm11
        vpandn    xmm0, xmm0, xmm9
        vpslld    xmm9, xmm0, 1
        vmovd     r9d, xmm9
        movsxd    r9, r9d
        vpextrd   ecx, xmm9, 2
        vpextrd   r8d, xmm9, 3
        movsxd    rcx, ecx
        movsxd    r8, r8d
        vmovq     xmm2, QWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+128+rdx+r9]
        vpextrd   r10d, xmm9, 1
        movsxd    r10, r10d
        vinsertf128 ymm1, ymm14, xmm1, 1
        vpslld    xmm14, xmm4, 7
        vpaddd    xmm10, xmm10, xmm14
        vpandn    xmm6, xmm6, xmm10
        vpslld    xmm0, xmm6, 1
        vmovd     r9d, xmm0
        vmovq     xmm4, QWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+128+rdx+rcx]
        vmovq     xmm6, QWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+128+rdx+r8]
        vpextrd   r11d, xmm0, 1
        vpextrd   ecx, xmm0, 2
        vpextrd   r8d, xmm0, 3
        movsxd    r9, r9d
        movsxd    r11, r11d
        movsxd    rcx, ecx
        movsxd    r8, r8d
        vmovq     xmm5, QWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+128+rdx+r10]
        vmovq     xmm7, QWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+128+rdx+r9]
        vmovq     xmm9, QWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+128+rdx+r11]
        vmovq     xmm8, QWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+128+rdx+rcx]
        vmovq     xmm10, QWORD PTR [imagerel(__svml_scbrt_ha_data_internal)+128+rdx+r8]
        vunpcklps xmm11, xmm2, xmm4
        vunpcklps xmm13, xmm5, xmm6
        vunpcklps xmm12, xmm7, xmm8
        vunpcklps xmm14, xmm9, xmm10
        vinsertf128 ymm0, ymm11, xmm12, 1
        vinsertf128 ymm2, ymm13, xmm14, 1
        vunpcklps ymm4, ymm0, ymm2
        vunpckhps ymm5, ymm0, ymm2
        vmulps    ymm7, ymm1, ymm4
        vmulps    ymm6, ymm1, ymm5
        vmulps    ymm1, ymm3, YMMWORD PTR [__svml_scbrt_ha_data_internal+896]
        vaddps    ymm1, ymm1, YMMWORD PTR [__svml_scbrt_ha_data_internal+960]
        vmulps    ymm5, ymm3, ymm1
        vaddps    ymm0, ymm5, YMMWORD PTR [__svml_scbrt_ha_data_internal+1024]
        vmulps    ymm2, ymm3, ymm0
        vmulps    ymm3, ymm3, ymm7
        vaddps    ymm4, ymm2, YMMWORD PTR [__svml_scbrt_ha_data_internal+1088]
        vmulps    ymm0, ymm4, ymm3
        vaddps    ymm1, ymm6, ymm0
        vaddps    ymm0, ymm7, ymm1
        test      al, al
        jne       _B5_3

_B5_2::

        vmovups   xmm6, XMMWORD PTR [320+rsp]
        vmovups   xmm7, XMMWORD PTR [352+rsp]
        vmovups   xmm8, XMMWORD PTR [336+rsp]
        vmovups   xmm9, XMMWORD PTR [304+rsp]
        vmovups   xmm10, XMMWORD PTR [288+rsp]
        vmovups   xmm11, XMMWORD PTR [208+rsp]
        vmovups   xmm12, XMMWORD PTR [272+rsp]
        vmovups   xmm13, XMMWORD PTR [256+rsp]
        vmovups   xmm14, XMMWORD PTR [240+rsp]
        vmovups   xmm15, XMMWORD PTR [224+rsp]
        mov       r13, QWORD PTR [368+rsp]
        add       rsp, 392
        ret

_B5_3::

        vmovups   YMMWORD PTR [r13], ymm15
        vmovups   YMMWORD PTR [64+r13], ymm0
        test      eax, eax
        je        _B5_2

_B5_6::

        xor       edx, edx
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, edx
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, eax

_B5_7::

        bt        esi, ebx
        jc        _B5_10

_B5_8::

        inc       ebx
        cmp       ebx, 8
        jl        _B5_7

_B5_9::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        vmovups   ymm0, YMMWORD PTR [64+r13]
        jmp       _B5_2

_B5_10::

        vzeroupper
        lea       rcx, QWORD PTR [r13+rbx*4]
        lea       rdx, QWORD PTR [64+r13+rbx*4]

        call      __svml_scbrt_ha_cout_rare_internal
        jmp       _B5_8
        ALIGN     16

_B5_11::

__svml_cbrtf8_ha_e9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_cbrtf8_ha_e9_B1_B3:
	DD	1602561
	DD	3069044
	DD	1337452
	DD	1472611
	DD	1411162
	DD	1284177
	DD	1222728
	DD	899135
	DD	1165366
	DD	1103917
	DD	1042468
	DD	981019
	DD	3211531

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B5_1
	DD	imagerel _B5_6
	DD	imagerel _unwind___svml_cbrtf8_ha_e9_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_cbrtf8_ha_e9_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B5_1
	DD	imagerel _B5_6
	DD	imagerel _unwind___svml_cbrtf8_ha_e9_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B5_6
	DD	imagerel _B5_11
	DD	imagerel _unwind___svml_cbrtf8_ha_e9_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST5:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_scbrt_ha_cout_rare_internal

__svml_scbrt_ha_cout_rare_internal	PROC

_B6_1::

        DB        243
        DB        15
        DB        30
        DB        250
L80::

        push      rbx
        push      rbp
        sub       rsp, 56
        mov       r9, rdx
        movss     xmm0, DWORD PTR [rcx]
        lea       r8, QWORD PTR [__ImageBase]
        movss     xmm2, DWORD PTR [ione]
        mulss     xmm2, xmm0
        movss     DWORD PTR [52+rsp], xmm2
        movzx     eax, WORD PTR [54+rsp]
        and       eax, 32640
        shr       eax, 7
        cmp       eax, 255
        je        _B6_9

_B6_2::

        pxor      xmm0, xmm0
        cvtss2sd  xmm0, xmm2
        pxor      xmm1, xmm1
        ucomisd   xmm0, xmm1
        jp        _B6_3
        je        _B6_8

_B6_3::

        test      eax, eax
        jne       _B6_5

_B6_4::

        mov       DWORD PTR [32+rsp], 2122317824
        mov       DWORD PTR [36+rsp], 713031680
        jmp       _B6_6

_B6_5::

        mov       eax, 1065353216
        mov       DWORD PTR [32+rsp], eax
        mov       DWORD PTR [36+rsp], eax

_B6_6::

        movss     xmm0, DWORD PTR [32+rsp]
        mulss     xmm2, xmm0
        movd      ecx, xmm2
        movss     DWORD PTR [52+rsp], xmm2
        mov       r10d, ecx
        mov       ebx, ecx
        and       r10d, 8388607
        mov       r11d, ecx
        shr       ebx, 23
        and       r11d, 8257536
        or        r10d, -1082130432
        or        r11d, -1081999360
        mov       DWORD PTR [40+rsp], r10d
        mov       edx, ecx
        movzx     ebp, bl
        and       ecx, 2147483647
        mov       DWORD PTR [44+rsp], r11d
        and       ebx, -256
        movss     xmm2, DWORD PTR [40+rsp]
        add       ecx, 2139095040
        shr       edx, 16
        subss     xmm2, DWORD PTR [44+rsp]
        and       edx, 124
        lea       r10d, DWORD PTR [rbp+rbp*4]
        mulss     xmm2, DWORD PTR [imagerel(vscbrt_ha_cout_data)+r8+rdx]
        lea       r11d, DWORD PTR [r10+r10]
        movss     xmm5, DWORD PTR [_2il0floatpacket_42]
        lea       eax, DWORD PTR [r11+r11]
        add       eax, eax
        lea       r10d, DWORD PTR [r10+r11*8]
        add       eax, eax
        dec       ebp
        mulss     xmm5, xmm2
        shl       ebp, 7
        lea       r11d, DWORD PTR [r10+rax*8]
        lea       r10d, DWORD PTR [r11+rax*8]
        shr       r10d, 12
        addss     xmm5, DWORD PTR [_2il0floatpacket_43]
        mulss     xmm5, xmm2
        lea       eax, DWORD PTR [85+r10]
        or        eax, ebx
        addss     xmm5, DWORD PTR [_2il0floatpacket_44]
        xor       ebx, ebx
        cmp       ecx, -16777217
        setg      bl
        shl       r10d, 7
        neg       ebx
        sub       ebp, r10d
        add       r10d, r10d
        sub       ebp, r10d
        not       ebx
        add       edx, ebp
        and       ebx, edx
        shl       eax, 23
        add       ebx, ebx
        mov       DWORD PTR [48+rsp], eax
        movss     xmm4, DWORD PTR [imagerel(vscbrt_ha_cout_data)+128+r8+rbx]
        movss     xmm1, DWORD PTR [48+rsp]
        mulss     xmm5, xmm2
        mulss     xmm4, xmm1
        addss     xmm5, DWORD PTR [_2il0floatpacket_45]
        mulss     xmm2, xmm4
        movss     xmm3, DWORD PTR [imagerel(vscbrt_ha_cout_data)+132+r8+rbx]
        mulss     xmm3, xmm1
        mulss     xmm5, xmm2
        addss     xmm5, xmm3
        addss     xmm5, xmm4
        mulss     xmm5, DWORD PTR [36+rsp]
        movss     DWORD PTR [r9], xmm5

_B6_7::

        xor       eax, eax
        add       rsp, 56
        pop       rbp
        pop       rbx
        ret

_B6_8::

        movss     DWORD PTR [r9], xmm2
        jmp       _B6_7

_B6_9::

        addss     xmm0, xmm0
        movss     DWORD PTR [r9], xmm0
        jmp       _B6_7
        ALIGN     16

_B6_10::

__svml_scbrt_ha_cout_rare_internal ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_scbrt_ha_cout_rare_internal_B1_B9:
	DD	199169
	DD	1342595594
	DD	12293

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B6_1
	DD	imagerel _B6_10
	DD	imagerel _unwind___svml_scbrt_ha_cout_rare_internal_B1_B9

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_RDATA	SEGMENT     READ PAGE   'DATA'
	ALIGN  32
ione	DD	1065353216
	DD	-1082130432
	DD 14 DUP (0H)	
vscbrt_ha_cout_data	DD	3212578753
	DD	3212085645
	DD	3211621124
	DD	3211182772
	DD	3210768440
	DD	3210376206
	DD	3210004347
	DD	3209651317
	DD	3209315720
	DD	3208996296
	DD	3208691905
	DD	3208401508
	DD	3208124163
	DD	3207859009
	DD	3207605259
	DD	3207362194
	DD	3207129151
	DD	3206905525
	DD	3206690755
	DD	3206484326
	DD	3206285761
	DD	3206094618
	DD	3205910490
	DD	3205732998
	DD	3205561788
	DD	3205396533
	DD	3205236929
	DD	3205082689
	DD	3204933547
	DD	3204789256
	DD	3204649583
	DD	3204514308
	DD	1065396681
	DD	839340838
	DD	1065482291
	DD	867750258
	DD	1065566215
	DD	851786446
	DD	1065648532
	DD	853949398
	DD	1065729317
	DD	864938789
	DD	1065808640
	DD	864102364
	DD	1065886565
	DD	864209792
	DD	1065963152
	DD	865422805
	DD	1066038457
	DD	867593594
	DD	1066112533
	DD	854482593
	DD	1066185428
	DD	848298042
	DD	1066257188
	DD	860064854
	DD	1066327857
	DD	844792593
	DD	1066397474
	DD	870701309
	DD	1066466079
	DD	872023170
	DD	1066533708
	DD	860255342
	DD	1066600394
	DD	849966899
	DD	1066666169
	DD	863561479
	DD	1066731064
	DD	869115319
	DD	1066795108
	DD	871961375
	DD	1066858329
	DD	859537336
	DD	1066920751
	DD	871954398
	DD	1066982401
	DD	863817578
	DD	1067043301
	DD	861687921
	DD	1067103474
	DD	849594757
	DD	1067162941
	DD	816486846
	DD	1067221722
	DD	858183533
	DD	1067279837
	DD	864500406
	DD	1067337305
	DD	850523240
	DD	1067394143
	DD	808125243
	DD	1067450368
	DD	0
	DD	1067505996
	DD	861173761
	DD	1067588354
	DD	859000219
	DD	1067696217
	DD	823158129
	DD	1067801953
	DD	871826232
	DD	1067905666
	DD	871183196
	DD	1068007450
	DD	839030530
	DD	1068107390
	DD	867690638
	DD	1068205570
	DD	840440923
	DD	1068302063
	DD	868033274
	DD	1068396942
	DD	855856030
	DD	1068490271
	DD	865094453
	DD	1068582113
	DD	860418487
	DD	1068672525
	DD	866225006
	DD	1068761562
	DD	866458226
	DD	1068849275
	DD	865124659
	DD	1068935712
	DD	864837702
	DD	1069020919
	DD	811742505
	DD	1069104937
	DD	869432099
	DD	1069187809
	DD	864584201
	DD	1069269572
	DD	864183978
	DD	1069350263
	DD	844810573
	DD	1069429915
	DD	869245699
	DD	1069508563
	DD	859556409
	DD	1069586236
	DD	870675446
	DD	1069662966
	DD	814190139
	DD	1069738778
	DD	870686941
	DD	1069813702
	DD	861800510
	DD	1069887762
	DD	855649163
	DD	1069960982
	DD	869347119
	DD	1070033387
	DD	864252033
	DD	1070104998
	DD	867276215
	DD	1070175837
	DD	868189817
	DD	1070245925
	DD	849541095
	DD	1070349689
	DD	866633177
	DD	1070485588
	DD	843967686
	DD	1070618808
	DD	857522493
	DD	1070749478
	DD	862339487
	DD	1070877717
	DD	850054662
	DD	1071003634
	DD	864048556
	DD	1071127332
	DD	868027089
	DD	1071248907
	DD	848093931
	DD	1071368446
	DD	865355299
	DD	1071486034
	DD	848111485
	DD	1071601747
	DD	865557362
	DD	1071715659
	DD	870297525
	DD	1071827839
	DD	863416216
	DD	1071938350
	DD	869675693
	DD	1072047254
	DD	865888071
	DD	1072154608
	DD	825332584
	DD	1072260465
	DD	843309506
	DD	1072364876
	DD	870885636
	DD	1072467891
	DD	869119784
	DD	1072569555
	DD	865466648
	DD	1072669911
	DD	867459244
	DD	1072769001
	DD	861192764
	DD	1072866863
	DD	871247716
	DD	1072963536
	DD	864927982
	DD	1073059054
	DD	869195129
	DD	1073153452
	DD	864849564
	DD	1073246762
	DD	840005936
	DD	1073339014
	DD	852579258
	DD	1073430238
	DD	860852782
	DD	1073520462
	DD	869711141
	DD	1073609714
	DD	862506141
	DD	1073698019
	DD	837959274
	DD	3173551943
	DD	3173551943
	DD	3173551943
	DD	3173551943
	DD	3173551943
	DD	3173551943
	DD	3173551943
	DD	3173551943
	DD	3173551943
	DD	3173551943
	DD	3173551943
	DD	3173551943
	DD	3173551943
	DD	3173551943
	DD	3173551943
	DD	3173551943
	DD	1031591658
	DD	1031591658
	DD	1031591658
	DD	1031591658
	DD	1031591658
	DD	1031591658
	DD	1031591658
	DD	1031591658
	DD	1031591658
	DD	1031591658
	DD	1031591658
	DD	1031591658
	DD	1031591658
	DD	1031591658
	DD	1031591658
	DD	1031591658
	DD	3185806905
	DD	3185806905
	DD	3185806905
	DD	3185806905
	DD	3185806905
	DD	3185806905
	DD	3185806905
	DD	3185806905
	DD	3185806905
	DD	3185806905
	DD	3185806905
	DD	3185806905
	DD	3185806905
	DD	3185806905
	DD	3185806905
	DD	3185806905
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	8388607
	DD	8388607
	DD	8388607
	DD	8388607
	DD	8388607
	DD	8388607
	DD	8388607
	DD	8388607
	DD	8388607
	DD	8388607
	DD	8388607
	DD	8388607
	DD	8388607
	DD	8388607
	DD	8388607
	DD	8388607
	DD	8257536
	DD	8257536
	DD	8257536
	DD	8257536
	DD	8257536
	DD	8257536
	DD	8257536
	DD	8257536
	DD	8257536
	DD	8257536
	DD	8257536
	DD	8257536
	DD	8257536
	DD	8257536
	DD	8257536
	DD	8257536
	DD	3212836864
	DD	3212836864
	DD	3212836864
	DD	3212836864
	DD	3212836864
	DD	3212836864
	DD	3212836864
	DD	3212836864
	DD	3212836864
	DD	3212836864
	DD	3212836864
	DD	3212836864
	DD	3212836864
	DD	3212836864
	DD	3212836864
	DD	3212836864
	DD	3212967936
	DD	3212967936
	DD	3212967936
	DD	3212967936
	DD	3212967936
	DD	3212967936
	DD	3212967936
	DD	3212967936
	DD	3212967936
	DD	3212967936
	DD	3212967936
	DD	3212967936
	DD	3212967936
	DD	3212967936
	DD	3212967936
	DD	3212967936
	DD	124
	DD	124
	DD	124
	DD	124
	DD	124
	DD	124
	DD	124
	DD	124
	DD	124
	DD	124
	DD	124
	DD	124
	DD	124
	DD	124
	DD	124
	DD	124
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
	DD	256
	DD	256
	DD	256
	DD	256
	DD	256
	DD	256
	DD	256
	DD	256
	DD	256
	DD	256
	DD	256
	DD	256
	DD	256
	DD	256
	DD	256
	DD	256
	DD	85
	DD	85
	DD	85
	DD	85
	DD	85
	DD	85
	DD	85
	DD	85
	DD	85
	DD	85
	DD	85
	DD	85
	DD	85
	DD	85
	DD	85
	DD	85
	DD	1
	DD	1
	DD	1
	DD	1
	DD	1
	DD	1
	DD	1
	DD	1
	DD	1
	DD	1
	DD	1
	DD	1
	DD	1
	DD	1
	DD	1
	DD	1
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
	DD	2155872256
	DD	2155872256
	DD	2155872256
	DD	2155872256
	DD	2155872256
	DD	2155872256
	DD	2155872256
	DD	2155872256
	DD	2155872256
	DD	2155872256
	DD	2155872256
	DD	2155872256
	DD	2155872256
	DD	2155872256
	DD	2155872256
	DD	2155872256
	DD	4278190079
	DD	4278190079
	DD	4278190079
	DD	4278190079
	DD	4278190079
	DD	4278190079
	DD	4278190079
	DD	4278190079
	DD	4278190079
	DD	4278190079
	DD	4278190079
	DD	4278190079
	DD	4278190079
	DD	4278190079
	DD	4278190079
	DD	4278190079
	PUBLIC __svml_scbrt_ha_data_internal_avx512
__svml_scbrt_ha_data_internal_avx512	DD	1065353216
	DD	1067533592
	DD	1070280693
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
	DD	2999865775
	DD	849849800
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
	DD	1067533592
	DD	1067322155
	DD	1067126683
	DD	1066945178
	DD	1066775983
	DD	1066617708
	DD	1066469175
	DD	1066329382
	DD	1066197466
	DD	1066072682
	DD	1065954382
	DD	1065841998
	DD	1065735031
	DD	1065633040
	DD	1065535634
	DD	1065442463
	DD	1065353216
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
	DD	2999865775
	DD	849353281
	DD	2992093760
	DD	858369405
	DD	861891413
	DD	3001900484
	DD	2988845984
	DD	3009185201
	DD	3001209163
	DD	847824101
	DD	839380496
	DD	845124191
	DD	851391835
	DD	856440803
	DD	2989578734
	DD	852890174
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
	DD	2147483648
	DD	2147483648
	DD	2147483648
	DD	2147483648
	DD	2147483648
	DD	2147483648
	DD	2147483648
	DD	2147483648
	DD	2147483648
	DD	2147483648
	DD	2147483648
	DD	2147483648
	DD	2147483648
	DD	2147483648
	DD	2147483648
	DD	2147483648
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1249902592
	DD	1249902592
	DD	1249902592
	DD	1249902592
	DD	1249902592
	DD	1249902592
	DD	1249902592
	DD	1249902592
	DD	1249902592
	DD	1249902592
	DD	1249902592
	DD	1249902592
	DD	1249902592
	DD	1249902592
	DD	1249902592
	DD	1249902592
	DD	1077936128
	DD	1077936128
	DD	1077936128
	DD	1077936128
	DD	1077936128
	DD	1077936128
	DD	1077936128
	DD	1077936128
	DD	1077936128
	DD	1077936128
	DD	1077936128
	DD	1077936128
	DD	1077936128
	DD	1077936128
	DD	1077936128
	DD	1077936128
	DD	1065353216
	DD	1065353216
	DD	1065353216
	DD	1065353216
	DD	1065353216
	DD	1065353216
	DD	1065353216
	DD	1065353216
	DD	1065353216
	DD	1065353216
	DD	1065353216
	DD	1065353216
	DD	1065353216
	DD	1065353216
	DD	1065353216
	DD	1065353216
	DD	1031603580
	DD	1031603580
	DD	1031603580
	DD	1031603580
	DD	1031603580
	DD	1031603580
	DD	1031603580
	DD	1031603580
	DD	1031603580
	DD	1031603580
	DD	1031603580
	DD	1031603580
	DD	1031603580
	DD	1031603580
	DD	1031603580
	DD	1031603580
	DD	3185812323
	DD	3185812323
	DD	3185812323
	DD	3185812323
	DD	3185812323
	DD	3185812323
	DD	3185812323
	DD	3185812323
	DD	3185812323
	DD	3185812323
	DD	3185812323
	DD	3185812323
	DD	3185812323
	DD	3185812323
	DD	3185812323
	DD	3185812323
	DD	1051372202
	DD	1051372202
	DD	1051372202
	DD	1051372202
	DD	1051372202
	DD	1051372202
	DD	1051372202
	DD	1051372202
	DD	1051372202
	DD	1051372202
	DD	1051372202
	DD	1051372202
	DD	1051372202
	DD	1051372202
	DD	1051372202
	DD	1051372202
	PUBLIC __svml_scbrt_ha_data_internal
__svml_scbrt_ha_data_internal	DD	3212578753
	DD	3212085645
	DD	3211621124
	DD	3211182772
	DD	3210768440
	DD	3210376206
	DD	3210004347
	DD	3209651317
	DD	3209315720
	DD	3208996296
	DD	3208691905
	DD	3208401508
	DD	3208124163
	DD	3207859009
	DD	3207605259
	DD	3207362194
	DD	3207129151
	DD	3206905525
	DD	3206690755
	DD	3206484326
	DD	3206285761
	DD	3206094618
	DD	3205910490
	DD	3205732998
	DD	3205561788
	DD	3205396533
	DD	3205236929
	DD	3205082689
	DD	3204933547
	DD	3204789256
	DD	3204649583
	DD	3204514308
	DD	1065396681
	DD	839340838
	DD	1065482291
	DD	867750258
	DD	1065566215
	DD	851786446
	DD	1065648532
	DD	853949398
	DD	1065729317
	DD	864938789
	DD	1065808640
	DD	864102364
	DD	1065886565
	DD	864209792
	DD	1065963152
	DD	865422805
	DD	1066038457
	DD	867593594
	DD	1066112533
	DD	854482593
	DD	1066185428
	DD	848298042
	DD	1066257188
	DD	860064854
	DD	1066327857
	DD	844792593
	DD	1066397474
	DD	870701309
	DD	1066466079
	DD	872023170
	DD	1066533708
	DD	860255342
	DD	1066600394
	DD	849966899
	DD	1066666169
	DD	863561479
	DD	1066731064
	DD	869115319
	DD	1066795108
	DD	871961375
	DD	1066858329
	DD	859537336
	DD	1066920751
	DD	871954398
	DD	1066982401
	DD	863817578
	DD	1067043301
	DD	861687921
	DD	1067103474
	DD	849594757
	DD	1067162941
	DD	816486846
	DD	1067221722
	DD	858183533
	DD	1067279837
	DD	864500406
	DD	1067337305
	DD	850523240
	DD	1067394143
	DD	808125243
	DD	1067450368
	DD	0
	DD	1067505996
	DD	861173761
	DD	1067588354
	DD	859000219
	DD	1067696217
	DD	823158129
	DD	1067801953
	DD	871826232
	DD	1067905666
	DD	871183196
	DD	1068007450
	DD	839030530
	DD	1068107390
	DD	867690638
	DD	1068205570
	DD	840440923
	DD	1068302063
	DD	868033274
	DD	1068396942
	DD	855856030
	DD	1068490271
	DD	865094453
	DD	1068582113
	DD	860418487
	DD	1068672525
	DD	866225006
	DD	1068761562
	DD	866458226
	DD	1068849275
	DD	865124659
	DD	1068935712
	DD	864837702
	DD	1069020919
	DD	811742505
	DD	1069104937
	DD	869432099
	DD	1069187809
	DD	864584201
	DD	1069269572
	DD	864183978
	DD	1069350263
	DD	844810573
	DD	1069429915
	DD	869245699
	DD	1069508563
	DD	859556409
	DD	1069586236
	DD	870675446
	DD	1069662966
	DD	814190139
	DD	1069738778
	DD	870686941
	DD	1069813702
	DD	861800510
	DD	1069887762
	DD	855649163
	DD	1069960982
	DD	869347119
	DD	1070033387
	DD	864252033
	DD	1070104998
	DD	867276215
	DD	1070175837
	DD	868189817
	DD	1070245925
	DD	849541095
	DD	1070349689
	DD	866633177
	DD	1070485588
	DD	843967686
	DD	1070618808
	DD	857522493
	DD	1070749478
	DD	862339487
	DD	1070877717
	DD	850054662
	DD	1071003634
	DD	864048556
	DD	1071127332
	DD	868027089
	DD	1071248907
	DD	848093931
	DD	1071368446
	DD	865355299
	DD	1071486034
	DD	848111485
	DD	1071601747
	DD	865557362
	DD	1071715659
	DD	870297525
	DD	1071827839
	DD	863416216
	DD	1071938350
	DD	869675693
	DD	1072047254
	DD	865888071
	DD	1072154608
	DD	825332584
	DD	1072260465
	DD	843309506
	DD	1072364876
	DD	870885636
	DD	1072467891
	DD	869119784
	DD	1072569555
	DD	865466648
	DD	1072669911
	DD	867459244
	DD	1072769001
	DD	861192764
	DD	1072866863
	DD	871247716
	DD	1072963536
	DD	864927982
	DD	1073059054
	DD	869195129
	DD	1073153452
	DD	864849564
	DD	1073246762
	DD	840005936
	DD	1073339014
	DD	852579258
	DD	1073430238
	DD	860852782
	DD	1073520462
	DD	869711141
	DD	1073609714
	DD	862506141
	DD	1073698019
	DD	837959274
	DD	3173551943
	DD	3173551943
	DD	3173551943
	DD	3173551943
	DD	3173551943
	DD	3173551943
	DD	3173551943
	DD	3173551943
	DD	3173551943
	DD	3173551943
	DD	3173551943
	DD	3173551943
	DD	3173551943
	DD	3173551943
	DD	3173551943
	DD	3173551943
	DD	1031591658
	DD	1031591658
	DD	1031591658
	DD	1031591658
	DD	1031591658
	DD	1031591658
	DD	1031591658
	DD	1031591658
	DD	1031591658
	DD	1031591658
	DD	1031591658
	DD	1031591658
	DD	1031591658
	DD	1031591658
	DD	1031591658
	DD	1031591658
	DD	3185806905
	DD	3185806905
	DD	3185806905
	DD	3185806905
	DD	3185806905
	DD	3185806905
	DD	3185806905
	DD	3185806905
	DD	3185806905
	DD	3185806905
	DD	3185806905
	DD	3185806905
	DD	3185806905
	DD	3185806905
	DD	3185806905
	DD	3185806905
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	1051372203
	DD	8388607
	DD	8388607
	DD	8388607
	DD	8388607
	DD	8388607
	DD	8388607
	DD	8388607
	DD	8388607
	DD	8388607
	DD	8388607
	DD	8388607
	DD	8388607
	DD	8388607
	DD	8388607
	DD	8388607
	DD	8388607
	DD	8257536
	DD	8257536
	DD	8257536
	DD	8257536
	DD	8257536
	DD	8257536
	DD	8257536
	DD	8257536
	DD	8257536
	DD	8257536
	DD	8257536
	DD	8257536
	DD	8257536
	DD	8257536
	DD	8257536
	DD	8257536
	DD	3212836864
	DD	3212836864
	DD	3212836864
	DD	3212836864
	DD	3212836864
	DD	3212836864
	DD	3212836864
	DD	3212836864
	DD	3212836864
	DD	3212836864
	DD	3212836864
	DD	3212836864
	DD	3212836864
	DD	3212836864
	DD	3212836864
	DD	3212836864
	DD	3212967936
	DD	3212967936
	DD	3212967936
	DD	3212967936
	DD	3212967936
	DD	3212967936
	DD	3212967936
	DD	3212967936
	DD	3212967936
	DD	3212967936
	DD	3212967936
	DD	3212967936
	DD	3212967936
	DD	3212967936
	DD	3212967936
	DD	3212967936
	DD	124
	DD	124
	DD	124
	DD	124
	DD	124
	DD	124
	DD	124
	DD	124
	DD	124
	DD	124
	DD	124
	DD	124
	DD	124
	DD	124
	DD	124
	DD	124
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
	DD	256
	DD	256
	DD	256
	DD	256
	DD	256
	DD	256
	DD	256
	DD	256
	DD	256
	DD	256
	DD	256
	DD	256
	DD	256
	DD	256
	DD	256
	DD	256
	DD	85
	DD	85
	DD	85
	DD	85
	DD	85
	DD	85
	DD	85
	DD	85
	DD	85
	DD	85
	DD	85
	DD	85
	DD	85
	DD	85
	DD	85
	DD	85
	DD	1
	DD	1
	DD	1
	DD	1
	DD	1
	DD	1
	DD	1
	DD	1
	DD	1
	DD	1
	DD	1
	DD	1
	DD	1
	DD	1
	DD	1
	DD	1
	DD	1365
	DD	1365
	DD	1365
	DD	1365
	DD	1365
	DD	1365
	DD	1365
	DD	1365
	DD	1365
	DD	1365
	DD	1365
	DD	1365
	DD	1365
	DD	1365
	DD	1365
	DD	1365
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
	DD	2155872256
	DD	2155872256
	DD	2155872256
	DD	2155872256
	DD	2155872256
	DD	2155872256
	DD	2155872256
	DD	2155872256
	DD	2155872256
	DD	2155872256
	DD	2155872256
	DD	2155872256
	DD	2155872256
	DD	2155872256
	DD	2155872256
	DD	2155872256
	DD	4278190079
	DD	4278190079
	DD	4278190079
	DD	4278190079
	DD	4278190079
	DD	4278190079
	DD	4278190079
	DD	4278190079
	DD	4278190079
	DD	4278190079
	DD	4278190079
	DD	4278190079
	DD	4278190079
	DD	4278190079
	DD	4278190079
	DD	4278190079
_2il0floatpacket_37	DD	0ffffffffH,000000000H,0ffffffffH,000000000H
_2il0floatpacket_38	DD	0007fffffH
_2il0floatpacket_39	DD	0007e0000H
_2il0floatpacket_40	DD	0bf800000H
_2il0floatpacket_41	DD	0bf820000H
_2il0floatpacket_42	DD	0bd288f47H
_2il0floatpacket_43	DD	03d7cd6eaH
_2il0floatpacket_44	DD	0bde38e39H
_2il0floatpacket_45	DD	03eaaaaabH
_RDATA	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS
EXTRN	__ImageBase:PROC
EXTRN	_fltused:BYTE
	ENDIF
	END
