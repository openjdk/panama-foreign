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
	PUBLIC __svml_expm1f4_ha_e9

__svml_expm1f4_ha_e9	PROC

_B1_1::

        DB        243
        DB        15
        DB        30
        DB        250
L1::

        sub       rsp, 312
        lea       rax, QWORD PTR [__ImageBase]
        vmovups   XMMWORD PTR [192+rsp], xmm15
        vmovups   XMMWORD PTR [208+rsp], xmm14
        vmovups   XMMWORD PTR [224+rsp], xmm13
        vmovups   XMMWORD PTR [240+rsp], xmm12
        vmovups   XMMWORD PTR [256+rsp], xmm11
        vmovups   XMMWORD PTR [272+rsp], xmm10
        mov       QWORD PTR [288+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovups   xmm10, XMMWORD PTR [rcx]
        and       r13, -64
        vmovups   xmm4, XMMWORD PTR [__svml_sexpm1_ha_data_internal+1280]
        vandps    xmm11, xmm10, xmm4
        vmulps    xmm14, xmm10, XMMWORD PTR [__svml_sexpm1_ha_data_internal+768]
        vcmpnleps xmm13, xmm11, XMMWORD PTR [__svml_sexpm1_ha_data_internal+1344]
        vroundps  xmm14, xmm14, 0
        vmovmskps edx, xmm13
        vaddps    xmm13, xmm14, XMMWORD PTR [__svml_sexpm1_ha_data_internal+960]
        vmulps    xmm5, xmm14, XMMWORD PTR [__svml_sexpm1_ha_data_internal+832]
        vmulps    xmm2, xmm14, XMMWORD PTR [__svml_sexpm1_ha_data_internal+896]
        vsubps    xmm3, xmm10, xmm5
        vandps    xmm12, xmm13, XMMWORD PTR [__svml_sexpm1_ha_data_internal+1024]
        vandnps   xmm5, xmm4, xmm10
        vmovd     r8d, xmm12
        vsubps    xmm1, xmm3, xmm2
        vcmpeqps  xmm0, xmm3, xmm10
        vpextrd   r9d, xmm12, 1
        vpextrd   r10d, xmm12, 2
        vpextrd   r11d, xmm12, 3
        movsxd    r8, r8d
        movsxd    r9, r9d
        movsxd    r10, r10d
        movsxd    r11, r11d
        vmovq     xmm14, QWORD PTR [imagerel(__svml_sexpm1_ha_data_internal)+rax+r8]
        vmovq     xmm15, QWORD PTR [imagerel(__svml_sexpm1_ha_data_internal)+rax+r9]
        vmovq     xmm11, QWORD PTR [imagerel(__svml_sexpm1_ha_data_internal)+rax+r10]
        vmovq     xmm12, QWORD PTR [imagerel(__svml_sexpm1_ha_data_internal)+rax+r11]
        vunpcklps xmm15, xmm14, xmm15
        vunpcklps xmm11, xmm11, xmm12
        vandps    xmm13, xmm13, XMMWORD PTR [__svml_sexpm1_ha_data_internal+1088]
        vmovlhps  xmm14, xmm15, xmm11
        vpslld    xmm12, xmm13, 14
        vshufps   xmm15, xmm15, xmm11, 238
        vorps     xmm13, xmm14, xmm12
        vmulps    xmm11, xmm15, xmm12
        vmulps    xmm15, xmm1, XMMWORD PTR [__svml_sexpm1_ha_data_internal+512]
        vmulps    xmm12, xmm1, xmm1
        vaddps    xmm14, xmm15, XMMWORD PTR [__svml_sexpm1_ha_data_internal+576]
        vorps     xmm0, xmm0, XMMWORD PTR [__svml_sexpm1_ha_data_internal+1152]
        vandps    xmm0, xmm3, xmm0
        vmulps    xmm15, xmm13, xmm0
        vsubps    xmm1, xmm3, xmm0
        vmulps    xmm3, xmm14, xmm12
        vsubps    xmm2, xmm1, xmm2
        vandps    xmm14, xmm13, xmm4
        vaddps    xmm1, xmm2, xmm3
        vmovups   xmm3, XMMWORD PTR [__svml_sexpm1_ha_data_internal+1216]
        vaddps    xmm0, xmm0, xmm1
        vaddps    xmm2, xmm13, xmm3
        vandps    xmm4, xmm3, xmm4
        vcmpleps  xmm14, xmm14, xmm4
        vblendvps xmm4, xmm13, xmm3, xmm14
        vblendvps xmm12, xmm3, xmm13, xmm14
        vmulps    xmm13, xmm13, xmm1
        vsubps    xmm3, xmm4, xmm2
        vmulps    xmm1, xmm11, xmm0
        vaddps    xmm14, xmm12, xmm3
        mov       QWORD PTR [296+rsp], r13
        vaddps    xmm4, xmm11, xmm14
        vaddps    xmm14, xmm15, xmm2
        vaddps    xmm11, xmm4, xmm1
        vsubps    xmm2, xmm2, xmm14
        vaddps    xmm15, xmm15, xmm2
        vaddps    xmm2, xmm15, xmm13
        vaddps    xmm0, xmm2, xmm11
        vaddps    xmm1, xmm14, xmm0
        vorps     xmm0, xmm1, xmm5
        test      edx, edx
        jne       _B1_3

_B1_2::

        vmovups   xmm10, XMMWORD PTR [272+rsp]
        vmovups   xmm11, XMMWORD PTR [256+rsp]
        vmovups   xmm12, XMMWORD PTR [240+rsp]
        vmovups   xmm13, XMMWORD PTR [224+rsp]
        vmovups   xmm14, XMMWORD PTR [208+rsp]
        vmovups   xmm15, XMMWORD PTR [192+rsp]
        mov       r13, QWORD PTR [288+rsp]
        add       rsp, 312
        ret

_B1_3::

        vmovups   XMMWORD PTR [r13], xmm10
        vmovups   XMMWORD PTR [64+r13], xmm0

_B1_6::

        xor       eax, eax
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, eax
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, edx

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

        call      __svml_sexpm1_ha_cout_rare_internal
        jmp       _B1_8
        ALIGN     16

_B1_11::

__svml_expm1f4_ha_e9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_expm1f4_ha_e9_B1_B3:
	DD	1069057
	DD	2413648
	DD	1157192
	DD	1095743
	DD	1034294
	DD	972845
	DD	911396
	DD	849947
	DD	2556171

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B1_1
	DD	imagerel _B1_6
	DD	imagerel _unwind___svml_expm1f4_ha_e9_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_expm1f4_ha_e9_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B1_1
	DD	imagerel _B1_6
	DD	imagerel _unwind___svml_expm1f4_ha_e9_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B1_6
	DD	imagerel _B1_11
	DD	imagerel _unwind___svml_expm1f4_ha_e9_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST1:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_expm1f4_ha_ex

__svml_expm1f4_ha_ex	PROC

_B2_1::

        DB        243
        DB        15
        DB        30
        DB        250
L20::

        sub       rsp, 312
        lea       rax, QWORD PTR [__ImageBase]
        movups    XMMWORD PTR [240+rsp], xmm15
        movups    XMMWORD PTR [256+rsp], xmm14
        movups    XMMWORD PTR [272+rsp], xmm13
        movups    XMMWORD PTR [192+rsp], xmm8
        movups    XMMWORD PTR [208+rsp], xmm7
        movups    XMMWORD PTR [224+rsp], xmm6
        mov       QWORD PTR [288+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        movups    xmm13, XMMWORD PTR [rcx]
        and       r13, -64
        movups    xmm6, XMMWORD PTR [__svml_sexpm1_ha_data_internal+768]
        movaps    xmm1, xmm13
        mulps     xmm6, xmm13
        movups    xmm5, XMMWORD PTR [_2il0floatpacket_16]
        movups    xmm14, XMMWORD PTR [__svml_sexpm1_ha_data_internal+1280]
        addps     xmm6, xmm5
        andps     xmm1, xmm14
        movaps    xmm8, xmm14
        cmpnleps  xmm1, XMMWORD PTR [__svml_sexpm1_ha_data_internal+1344]
        subps     xmm6, xmm5
        movmskps  edx, xmm1
        movups    xmm1, XMMWORD PTR [__svml_sexpm1_ha_data_internal+960]
        movaps    xmm5, xmm13
        movups    xmm2, XMMWORD PTR [__svml_sexpm1_ha_data_internal+832]
        andnps    xmm8, xmm13
        mulps     xmm2, xmm6
        addps     xmm1, xmm6
        movdqu    xmm15, XMMWORD PTR [__svml_sexpm1_ha_data_internal+1024]
        subps     xmm5, xmm2
        pand      xmm15, xmm1
        movaps    xmm3, xmm5
        pshufd    xmm4, xmm15, 1
        movaps    xmm7, xmm5
        movd      r8d, xmm15
        pshufd    xmm2, xmm15, 2
        pshufd    xmm15, xmm15, 3
        movd      r9d, xmm4
        movups    xmm0, XMMWORD PTR [__svml_sexpm1_ha_data_internal+896]
        pand      xmm1, XMMWORD PTR [__svml_sexpm1_ha_data_internal+1088]
        movd      r10d, xmm2
        pslld     xmm1, 14
        movd      r11d, xmm15
        mulps     xmm0, xmm6
        cmpeqps   xmm7, xmm13
        subps     xmm3, xmm0
        movsxd    r8, r8d
        movsxd    r9, r9d
        movsxd    r10, r10d
        movsxd    r11, r11d
        movq      xmm6, QWORD PTR [imagerel(__svml_sexpm1_ha_data_internal)+rax+r8]
        movq      xmm4, QWORD PTR [imagerel(__svml_sexpm1_ha_data_internal)+rax+r9]
        movq      xmm2, QWORD PTR [imagerel(__svml_sexpm1_ha_data_internal)+rax+r10]
        movq      xmm15, QWORD PTR [imagerel(__svml_sexpm1_ha_data_internal)+rax+r11]
        unpcklps  xmm6, xmm4
        unpcklps  xmm2, xmm15
        movaps    xmm4, xmm6
        movlhps   xmm4, xmm2
        shufps    xmm6, xmm2, 238
        orps      xmm4, xmm1
        movups    xmm2, XMMWORD PTR [__svml_sexpm1_ha_data_internal+512]
        mulps     xmm2, xmm3
        mulps     xmm3, xmm3
        mulps     xmm6, xmm1
        addps     xmm2, XMMWORD PTR [__svml_sexpm1_ha_data_internal+576]
        orps      xmm7, XMMWORD PTR [__svml_sexpm1_ha_data_internal+1152]
        movaps    xmm1, xmm4
        movups    xmm15, XMMWORD PTR [__svml_sexpm1_ha_data_internal+1216]
        andps     xmm7, xmm5
        mulps     xmm2, xmm3
        movaps    xmm3, xmm4
        subps     xmm5, xmm7
        mulps     xmm1, xmm7
        subps     xmm5, xmm0
        andps     xmm3, xmm14
        andps     xmm14, xmm15
        cmpleps   xmm3, xmm14
        addps     xmm5, xmm2
        movaps    xmm0, xmm3
        movaps    xmm14, xmm3
        andps     xmm0, xmm15
        andnps    xmm14, xmm15
        movaps    xmm2, xmm3
        andps     xmm3, xmm4
        andnps    xmm2, xmm4
        orps      xmm14, xmm3
        orps      xmm2, xmm0
        addps     xmm15, xmm4
        mulps     xmm4, xmm5
        addps     xmm7, xmm5
        subps     xmm2, xmm15
        movaps    xmm0, xmm15
        addps     xmm14, xmm2
        addps     xmm0, xmm1
        addps     xmm14, xmm6
        mulps     xmm6, xmm7
        subps     xmm15, xmm0
        addps     xmm14, xmm6
        addps     xmm1, xmm15
        mov       QWORD PTR [296+rsp], r13
        addps     xmm1, xmm4
        addps     xmm1, xmm14
        addps     xmm0, xmm1
        orps      xmm0, xmm8
        test      edx, edx
        jne       _B2_3

_B2_2::

        movups    xmm6, XMMWORD PTR [224+rsp]
        movups    xmm7, XMMWORD PTR [208+rsp]
        movups    xmm8, XMMWORD PTR [192+rsp]
        movups    xmm13, XMMWORD PTR [272+rsp]
        movups    xmm14, XMMWORD PTR [256+rsp]
        movups    xmm15, XMMWORD PTR [240+rsp]
        mov       r13, QWORD PTR [288+rsp]
        add       rsp, 312
        ret

_B2_3::

        movups    XMMWORD PTR [r13], xmm13
        movups    XMMWORD PTR [64+r13], xmm0

_B2_6::

        xor       ecx, ecx
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, ecx
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, edx

_B2_7::

        mov       ecx, ebx
        mov       eax, 1
        shl       eax, cl
        test      esi, eax
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

        call      __svml_sexpm1_ha_cout_rare_internal
        jmp       _B2_8
        ALIGN     16

_B2_11::

__svml_expm1f4_ha_ex ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_expm1f4_ha_ex_B1_B3:
	DD	1068545
	DD	2413646
	DD	944198
	DD	882750
	DD	821302
	DD	1169453
	DD	1108004
	DD	1046555
	DD	2556171

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B2_1
	DD	imagerel _B2_6
	DD	imagerel _unwind___svml_expm1f4_ha_ex_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_expm1f4_ha_ex_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B2_1
	DD	imagerel _B2_6
	DD	imagerel _unwind___svml_expm1f4_ha_ex_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B2_6
	DD	imagerel _B2_11
	DD	imagerel _unwind___svml_expm1f4_ha_ex_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST2:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_expm1f8_ha_l9

__svml_expm1f8_ha_l9	PROC

_B3_1::

        DB        243
        DB        15
        DB        30
        DB        250
L39::

        sub       rsp, 392
        lea       rax, QWORD PTR [__ImageBase]
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
        mov       QWORD PTR [368+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovups   ymm13, YMMWORD PTR [rcx]
        and       r13, -64
        vmovups   ymm11, YMMWORD PTR [__svml_sexpm1_ha_data_internal+1280]
        vmovups   ymm10, YMMWORD PTR [__svml_sexpm1_ha_data_internal+832]
        vmulps    ymm0, ymm13, YMMWORD PTR [__svml_sexpm1_ha_data_internal+768]
        vroundps  ymm5, ymm0, 0
        vaddps    ymm2, ymm5, YMMWORD PTR [__svml_sexpm1_ha_data_internal+960]
        vfnmadd213ps ymm10, ymm5, ymm13
        vmulps    ymm9, ymm5, YMMWORD PTR [__svml_sexpm1_ha_data_internal+896]
        vcmpeqps  ymm7, ymm10, ymm13
        vsubps    ymm8, ymm10, ymm9
        vandps    ymm6, ymm2, YMMWORD PTR [__svml_sexpm1_ha_data_internal+1024]
        vandps    ymm1, ymm13, ymm11
        vcmpnle_uqps ymm3, ymm1, YMMWORD PTR [__svml_sexpm1_ha_data_internal+1344]
        vandps    ymm2, ymm2, YMMWORD PTR [__svml_sexpm1_ha_data_internal+1088]
        vmovmskps edx, ymm3
        vpslld    ymm2, ymm2, 14
        vorps     ymm7, ymm7, YMMWORD PTR [__svml_sexpm1_ha_data_internal+1152]
        vandnps   ymm12, ymm11, ymm13
        mov       QWORD PTR [376+rsp], r13
        vmovd     r8d, xmm6
        vpextrd   r9d, xmm6, 1
        vpextrd   r10d, xmm6, 2
        vpextrd   r11d, xmm6, 3
        vextracti128 xmm6, ymm6, 1
        movsxd    r8, r8d
        movsxd    r9, r9d
        movsxd    r10, r10d
        vmovd     ecx, xmm6
        vmovq     xmm15, QWORD PTR [imagerel(__svml_sexpm1_ha_data_internal)+rax+r8]
        vmovq     xmm0, QWORD PTR [imagerel(__svml_sexpm1_ha_data_internal)+rax+r9]
        vmovq     xmm14, QWORD PTR [imagerel(__svml_sexpm1_ha_data_internal)+rax+r10]
        vpextrd   r8d, xmm6, 1
        vpextrd   r9d, xmm6, 2
        vpextrd   r10d, xmm6, 3
        movsxd    r11, r11d
        movsxd    rcx, ecx
        movsxd    r8, r8d
        movsxd    r9, r9d
        movsxd    r10, r10d
        vmovq     xmm1, QWORD PTR [imagerel(__svml_sexpm1_ha_data_internal)+rax+r11]
        vmovq     xmm3, QWORD PTR [imagerel(__svml_sexpm1_ha_data_internal)+rax+rcx]
        vmovq     xmm4, QWORD PTR [imagerel(__svml_sexpm1_ha_data_internal)+rax+r8]
        vmovq     xmm5, QWORD PTR [imagerel(__svml_sexpm1_ha_data_internal)+rax+r9]
        vmovq     xmm6, QWORD PTR [imagerel(__svml_sexpm1_ha_data_internal)+rax+r10]
        vunpcklps xmm14, xmm15, xmm14
        vunpcklps xmm15, xmm0, xmm1
        vunpcklps xmm0, xmm3, xmm5
        vunpcklps xmm4, xmm4, xmm6
        vinsertf128 ymm1, ymm14, xmm0, 1
        vinsertf128 ymm3, ymm15, xmm4, 1
        vunpcklps ymm5, ymm1, ymm3
        vunpckhps ymm4, ymm1, ymm3
        vorps     ymm6, ymm5, ymm2
        vmulps    ymm4, ymm4, ymm2
        vmovups   ymm2, YMMWORD PTR [__svml_sexpm1_ha_data_internal+512]
        vmovups   ymm1, YMMWORD PTR [__svml_sexpm1_ha_data_internal+1216]
        vfmadd213ps ymm2, ymm8, YMMWORD PTR [__svml_sexpm1_ha_data_internal+576]
        vmulps    ymm8, ymm8, ymm8
        vandps    ymm15, ymm10, ymm7
        vsubps    ymm10, ymm10, ymm15
        vmulps    ymm14, ymm6, ymm15
        vsubps    ymm9, ymm10, ymm9
        vandps    ymm0, ymm6, ymm11
        vandps    ymm11, ymm1, ymm11
        vfmadd213ps ymm2, ymm8, ymm9
        vcmple_oqps ymm0, ymm0, ymm11
        vaddps    ymm9, ymm6, ymm1
        vblendvps ymm3, ymm6, ymm1, ymm0
        vblendvps ymm5, ymm1, ymm6, ymm0
        vaddps    ymm0, ymm14, ymm9
        vsubps    ymm7, ymm3, ymm9
        vsubps    ymm10, ymm9, ymm0
        vaddps    ymm8, ymm5, ymm7
        vaddps    ymm14, ymm14, ymm10
        vaddps    ymm11, ymm4, ymm8
        vfmadd213ps ymm6, ymm2, ymm14
        vaddps    ymm2, ymm15, ymm2
        vfmadd213ps ymm4, ymm2, ymm11
        vaddps    ymm4, ymm6, ymm4
        vaddps    ymm6, ymm0, ymm4
        vorps     ymm0, ymm6, ymm12
        test      edx, edx
        jne       _B3_3

_B3_2::

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
        mov       r13, QWORD PTR [368+rsp]
        add       rsp, 392
        ret

_B3_3::

        vmovups   YMMWORD PTR [r13], ymm13
        vmovups   YMMWORD PTR [64+r13], ymm0

_B3_6::

        xor       eax, eax
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, eax
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, edx

_B3_7::

        bt        esi, ebx
        jc        _B3_10

_B3_8::

        inc       ebx
        cmp       ebx, 8
        jl        _B3_7

_B3_9::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        vmovups   ymm0, YMMWORD PTR [64+r13]
        jmp       _B3_2

_B3_10::

        vzeroupper
        lea       rcx, QWORD PTR [r13+rbx*4]
        lea       rdx, QWORD PTR [64+r13+rbx*4]

        call      __svml_sexpm1_ha_cout_rare_internal
        jmp       _B3_8
        ALIGN     16

_B3_11::

__svml_expm1f8_ha_l9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_expm1f8_ha_l9_B1_B3:
	DD	1602561
	DD	3069044
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
	DD	3211531

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B3_1
	DD	imagerel _B3_6
	DD	imagerel _unwind___svml_expm1f8_ha_l9_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_expm1f8_ha_l9_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B3_1
	DD	imagerel _B3_6
	DD	imagerel _unwind___svml_expm1f8_ha_l9_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B3_6
	DD	imagerel _B3_11
	DD	imagerel _unwind___svml_expm1f8_ha_l9_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST3:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_expm1f4_ha_l9

__svml_expm1f4_ha_l9	PROC

_B4_1::

        DB        243
        DB        15
        DB        30
        DB        250
L66::

        sub       rsp, 312
        lea       rax, QWORD PTR [__ImageBase]
        vmovups   XMMWORD PTR [208+rsp], xmm15
        vmovups   XMMWORD PTR [224+rsp], xmm14
        vmovups   XMMWORD PTR [240+rsp], xmm13
        vmovups   XMMWORD PTR [256+rsp], xmm12
        vmovups   XMMWORD PTR [272+rsp], xmm11
        vmovups   XMMWORD PTR [192+rsp], xmm6
        mov       QWORD PTR [288+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovups   xmm6, XMMWORD PTR [rcx]
        and       r13, -64
        vmovups   xmm4, XMMWORD PTR [__svml_sexpm1_ha_data_internal+1280]
        vandps    xmm11, xmm6, xmm4
        vandnps   xmm5, xmm4, xmm6
        vmulps    xmm12, xmm6, XMMWORD PTR [__svml_sexpm1_ha_data_internal+768]
        vcmpnleps xmm13, xmm11, XMMWORD PTR [__svml_sexpm1_ha_data_internal+1344]
        vroundps  xmm12, xmm12, 0
        vmovmskps edx, xmm13
        vaddps    xmm13, xmm12, XMMWORD PTR [__svml_sexpm1_ha_data_internal+960]
        vmulps    xmm3, xmm12, XMMWORD PTR [__svml_sexpm1_ha_data_internal+896]
        vmovups   xmm2, XMMWORD PTR [__svml_sexpm1_ha_data_internal+832]
        vfnmadd213ps xmm2, xmm12, xmm6
        vandps    xmm12, xmm13, XMMWORD PTR [__svml_sexpm1_ha_data_internal+1024]
        vandps    xmm13, xmm13, XMMWORD PTR [__svml_sexpm1_ha_data_internal+1088]
        vmovd     r8d, xmm12
        vcmpeqps  xmm0, xmm2, xmm6
        vsubps    xmm1, xmm2, xmm3
        vpextrd   r9d, xmm12, 1
        vpextrd   r10d, xmm12, 2
        vpextrd   r11d, xmm12, 3
        movsxd    r8, r8d
        movsxd    r9, r9d
        movsxd    r10, r10d
        movsxd    r11, r11d
        vmovq     xmm14, QWORD PTR [imagerel(__svml_sexpm1_ha_data_internal)+rax+r8]
        vmovq     xmm15, QWORD PTR [imagerel(__svml_sexpm1_ha_data_internal)+rax+r9]
        vmovq     xmm11, QWORD PTR [imagerel(__svml_sexpm1_ha_data_internal)+rax+r10]
        vmovq     xmm12, QWORD PTR [imagerel(__svml_sexpm1_ha_data_internal)+rax+r11]
        vunpcklps xmm15, xmm14, xmm15
        vunpcklps xmm11, xmm11, xmm12
        vmovlhps  xmm14, xmm15, xmm11
        vshufps   xmm15, xmm15, xmm11, 238
        vpslld    xmm11, xmm13, 14
        vorps     xmm0, xmm0, XMMWORD PTR [__svml_sexpm1_ha_data_internal+1152]
        vorps     xmm12, xmm14, xmm11
        vmulps    xmm11, xmm15, xmm11
        vandps    xmm15, xmm2, xmm0
        vsubps    xmm2, xmm2, xmm15
        vmulps    xmm14, xmm12, xmm15
        vsubps    xmm3, xmm2, xmm3
        vmovups   xmm13, XMMWORD PTR [__svml_sexpm1_ha_data_internal+512]
        vfmadd213ps xmm13, xmm1, XMMWORD PTR [__svml_sexpm1_ha_data_internal+576]
        vmulps    xmm1, xmm1, xmm1
        vmovups   xmm0, XMMWORD PTR [__svml_sexpm1_ha_data_internal+1216]
        vfmadd213ps xmm13, xmm1, xmm3
        vandps    xmm1, xmm12, xmm4
        vandps    xmm4, xmm0, xmm4
        vaddps    xmm2, xmm12, xmm0
        vcmpleps  xmm3, xmm1, xmm4
        vblendvps xmm4, xmm12, xmm0, xmm3
        vblendvps xmm1, xmm0, xmm12, xmm3
        vsubps    xmm0, xmm4, xmm2
        mov       QWORD PTR [296+rsp], r13
        vaddps    xmm3, xmm1, xmm0
        vaddps    xmm4, xmm11, xmm3
        vaddps    xmm3, xmm14, xmm2
        vsubps    xmm2, xmm2, xmm3
        vaddps    xmm14, xmm14, xmm2
        vfmadd213ps xmm12, xmm13, xmm14
        vaddps    xmm13, xmm15, xmm13
        vfmadd213ps xmm11, xmm13, xmm4
        vaddps    xmm11, xmm12, xmm11
        vaddps    xmm12, xmm3, xmm11
        vorps     xmm0, xmm12, xmm5
        test      edx, edx
        jne       _B4_3

_B4_2::

        vmovups   xmm6, XMMWORD PTR [192+rsp]
        vmovups   xmm11, XMMWORD PTR [272+rsp]
        vmovups   xmm12, XMMWORD PTR [256+rsp]
        vmovups   xmm13, XMMWORD PTR [240+rsp]
        vmovups   xmm14, XMMWORD PTR [224+rsp]
        vmovups   xmm15, XMMWORD PTR [208+rsp]
        mov       r13, QWORD PTR [288+rsp]
        add       rsp, 312
        ret

_B4_3::

        vmovups   XMMWORD PTR [r13], xmm6
        vmovups   XMMWORD PTR [64+r13], xmm0

_B4_6::

        xor       eax, eax
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, eax
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, edx

_B4_7::

        bt        esi, ebx
        jc        _B4_10

_B4_8::

        inc       ebx
        cmp       ebx, 4
        jl        _B4_7

_B4_9::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        vmovups   xmm0, XMMWORD PTR [64+r13]
        jmp       _B4_2

_B4_10::

        lea       rcx, QWORD PTR [r13+rbx*4]
        lea       rdx, QWORD PTR [64+r13+rbx*4]

        call      __svml_sexpm1_ha_cout_rare_internal
        jmp       _B4_8
        ALIGN     16

_B4_11::

__svml_expm1f4_ha_l9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_expm1f4_ha_l9_B1_B3:
	DD	1069057
	DD	2413648
	DD	813128
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
	DD	imagerel _unwind___svml_expm1f4_ha_l9_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_expm1f4_ha_l9_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B4_1
	DD	imagerel _B4_6
	DD	imagerel _unwind___svml_expm1f4_ha_l9_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B4_6
	DD	imagerel _B4_11
	DD	imagerel _unwind___svml_expm1f4_ha_l9_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST4:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_expm1f8_ha_e9

__svml_expm1f8_ha_e9	PROC

_B5_1::

        DB        243
        DB        15
        DB        30
        DB        250
L85::

        sub       rsp, 392
        lea       rax, QWORD PTR [__ImageBase]
        vmovups   XMMWORD PTR [224+rsp], xmm15
        vmovups   XMMWORD PTR [208+rsp], xmm14
        vmovups   XMMWORD PTR [240+rsp], xmm13
        vmovups   XMMWORD PTR [256+rsp], xmm12
        vmovups   XMMWORD PTR [272+rsp], xmm11
        vmovups   XMMWORD PTR [288+rsp], xmm10
        vmovups   XMMWORD PTR [304+rsp], xmm9
        vmovups   XMMWORD PTR [320+rsp], xmm8
        vmovups   XMMWORD PTR [352+rsp], xmm7
        vmovups   XMMWORD PTR [336+rsp], xmm6
        vpxor     xmm7, xmm7, xmm7
        mov       QWORD PTR [368+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovups   ymm4, YMMWORD PTR [rcx]
        and       r13, -64
        vmovups   ymm14, YMMWORD PTR [__svml_sexpm1_ha_data_internal+1280]
        vmulps    ymm1, ymm4, YMMWORD PTR [__svml_sexpm1_ha_data_internal+768]
        vmovups   YMMWORD PTR [r13], ymm4
        vroundps  ymm0, ymm1, 0
        vmovups   xmm1, XMMWORD PTR [__svml_sexpm1_ha_data_internal+1024]
        vandps    ymm5, ymm4, ymm14
        vcmpnle_uqps ymm9, ymm5, YMMWORD PTR [__svml_sexpm1_ha_data_internal+1344]
        vmulps    ymm3, ymm0, YMMWORD PTR [__svml_sexpm1_ha_data_internal+832]
        vmulps    ymm13, ymm0, YMMWORD PTR [__svml_sexpm1_ha_data_internal+896]
        vsubps    ymm12, ymm4, ymm3
        vsubps    ymm11, ymm12, ymm13
        vcmpeqps  ymm10, ymm12, ymm4
        vandnps   ymm15, ymm14, ymm4
        vorps     ymm10, ymm10, YMMWORD PTR [__svml_sexpm1_ha_data_internal+1152]
        vandps    ymm10, ymm12, ymm10
        vsubps    ymm12, ymm12, ymm10
        vsubps    ymm13, ymm12, ymm13
        mov       QWORD PTR [376+rsp], r13
        vextractf128 xmm8, ymm9, 1
        vpackssdw xmm6, xmm9, xmm8
        vaddps    ymm8, ymm0, YMMWORD PTR [__svml_sexpm1_ha_data_internal+960]
        vpacksswb xmm2, xmm6, xmm7
        vpmovmskb edx, xmm2
        vandps    xmm3, xmm8, xmm1
        vmovd     r8d, xmm3
        vextractf128 xmm9, ymm8, 1
        vpextrd   r9d, xmm3, 1
        vandps    xmm6, xmm9, xmm1
        vpextrd   r10d, xmm3, 2
        movsxd    r8, r8d
        movsxd    r9, r9d
        movsxd    r10, r10d
        vmovd     ecx, xmm6
        vmovq     xmm0, QWORD PTR [imagerel(__svml_sexpm1_ha_data_internal)+rax+r8]
        vmovq     xmm2, QWORD PTR [imagerel(__svml_sexpm1_ha_data_internal)+rax+r9]
        vpextrd   r11d, xmm3, 3
        vmovq     xmm1, QWORD PTR [imagerel(__svml_sexpm1_ha_data_internal)+rax+r10]
        vpextrd   r8d, xmm6, 1
        vpextrd   r9d, xmm6, 2
        vpextrd   r10d, xmm6, 3
        movsxd    r11, r11d
        movsxd    rcx, ecx
        movsxd    r8, r8d
        movsxd    r9, r9d
        movsxd    r10, r10d
        vmovq     xmm3, QWORD PTR [imagerel(__svml_sexpm1_ha_data_internal)+rax+r11]
        vmovq     xmm4, QWORD PTR [imagerel(__svml_sexpm1_ha_data_internal)+rax+rcx]
        vmovq     xmm7, QWORD PTR [imagerel(__svml_sexpm1_ha_data_internal)+rax+r8]
        vmovq     xmm5, QWORD PTR [imagerel(__svml_sexpm1_ha_data_internal)+rax+r9]
        vmovq     xmm6, QWORD PTR [imagerel(__svml_sexpm1_ha_data_internal)+rax+r10]
        vunpcklps xmm0, xmm0, xmm1
        vunpcklps xmm2, xmm2, xmm3
        vunpcklps xmm4, xmm4, xmm5
        vunpcklps xmm5, xmm7, xmm6
        vinsertf128 ymm7, ymm0, xmm4, 1
        vinsertf128 ymm0, ymm2, xmm5, 1
        vmovups   xmm2, XMMWORD PTR [__svml_sexpm1_ha_data_internal+1088]
        vmulps    ymm5, ymm11, YMMWORD PTR [__svml_sexpm1_ha_data_internal+512]
        vandps    xmm8, xmm8, xmm2
        vandps    xmm9, xmm9, xmm2
        vpslld    xmm1, xmm8, 14
        vpslld    xmm3, xmm9, 14
        vmulps    ymm11, ymm11, ymm11
        vmovups   ymm2, YMMWORD PTR [__svml_sexpm1_ha_data_internal+1216]
        vunpcklps ymm6, ymm7, ymm0
        vunpckhps ymm7, ymm7, ymm0
        vinsertf128 ymm4, ymm1, xmm3, 1
        vorps     ymm8, ymm6, ymm4
        vaddps    ymm6, ymm5, YMMWORD PTR [__svml_sexpm1_ha_data_internal+576]
        vaddps    ymm5, ymm8, ymm2
        vmulps    ymm12, ymm8, ymm10
        vmulps    ymm0, ymm6, ymm11
        vmulps    ymm9, ymm7, ymm4
        vaddps    ymm13, ymm13, ymm0
        vandps    ymm1, ymm8, ymm14
        vandps    ymm14, ymm2, ymm14
        vcmple_oqps ymm14, ymm1, ymm14
        vaddps    ymm10, ymm10, ymm13
        vblendvps ymm0, ymm8, ymm2, ymm14
        vblendvps ymm1, ymm2, ymm8, ymm14
        vsubps    ymm3, ymm0, ymm5
        vaddps    ymm14, ymm12, ymm5
        vmulps    ymm8, ymm8, ymm13
        vaddps    ymm4, ymm1, ymm3
        vsubps    ymm6, ymm5, ymm14
        vaddps    ymm7, ymm9, ymm4
        vaddps    ymm12, ymm12, ymm6
        vmulps    ymm9, ymm9, ymm10
        vaddps    ymm11, ymm12, ymm8
        vaddps    ymm13, ymm7, ymm9
        vaddps    ymm0, ymm11, ymm13
        vaddps    ymm1, ymm14, ymm0
        vorps     ymm0, ymm1, ymm15
        vmovups   ymm1, YMMWORD PTR [r13]
        test      dl, dl
        jne       _B5_3

_B5_2::

        vmovups   xmm6, XMMWORD PTR [336+rsp]
        vmovups   xmm7, XMMWORD PTR [352+rsp]
        vmovups   xmm8, XMMWORD PTR [320+rsp]
        vmovups   xmm9, XMMWORD PTR [304+rsp]
        vmovups   xmm10, XMMWORD PTR [288+rsp]
        vmovups   xmm11, XMMWORD PTR [272+rsp]
        vmovups   xmm12, XMMWORD PTR [256+rsp]
        vmovups   xmm13, XMMWORD PTR [240+rsp]
        vmovups   xmm14, XMMWORD PTR [208+rsp]
        vmovups   xmm15, XMMWORD PTR [224+rsp]
        mov       r13, QWORD PTR [368+rsp]
        add       rsp, 392
        ret

_B5_3::

        vmovups   YMMWORD PTR [r13], ymm1
        vmovups   YMMWORD PTR [64+r13], ymm0
        test      edx, edx
        je        _B5_2

_B5_6::

        xor       eax, eax
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, eax
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, edx

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

        call      __svml_sexpm1_ha_cout_rare_internal
        jmp       _B5_8
        ALIGN     16

_B5_11::

__svml_expm1f8_ha_e9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_expm1f8_ha_e9_B1_B3:
	DD	1603585
	DD	3069048
	DD	1402988
	DD	1472611
	DD	1345626
	DD	1284177
	DD	1222728
	DD	1161279
	DD	1099830
	DD	1038381
	DD	911396
	DD	981019
	DD	3211531

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B5_1
	DD	imagerel _B5_6
	DD	imagerel _unwind___svml_expm1f8_ha_e9_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_expm1f8_ha_e9_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B5_1
	DD	imagerel _B5_6
	DD	imagerel _unwind___svml_expm1f8_ha_e9_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B5_6
	DD	imagerel _B5_11
	DD	imagerel _unwind___svml_expm1f8_ha_e9_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST5:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_sexpm1_ha_cout_rare_internal

__svml_sexpm1_ha_cout_rare_internal	PROC

_B6_1::

        DB        243
        DB        15
        DB        30
        DB        250
L114::

        sub       rsp, 104
        mov       r9, rdx
        movss     xmm1, DWORD PTR [rcx]
        pxor      xmm0, xmm0
        xor       eax, eax
        comiss    xmm0, xmm1
        ja        _B6_18

_B6_2::

        movzx     edx, WORD PTR [2+rcx]
        and       edx, 32640
        shr       edx, 7
        cmp       edx, 255
        je        _B6_19

_B6_3::

        cvtss2sd  xmm1, xmm1
        cmp       edx, 74
        jle       _B6_16

_B6_4::

        movsd     xmm0, QWORD PTR [_vmldExpHATab+1080]
        comisd    xmm0, xmm1
        jb        _B6_15

_B6_5::

        comisd    xmm1, QWORD PTR [_vmldExpHATab+1096]
        jb        _B6_14

_B6_6::

        movsd     xmm2, QWORD PTR [_vmldExpHATab+1024]
        mulsd     xmm2, xmm1
        movsd     QWORD PTR [88+rsp], xmm2
        movaps    xmm2, xmm1
        movsd     xmm3, QWORD PTR [88+rsp]
        movsd     xmm0, QWORD PTR [_vmldExpHATab+1136]
        movsd     QWORD PTR [80+rsp], xmm0
        addsd     xmm3, QWORD PTR [_vmldExpHATab+1032]
        movsd     QWORD PTR [96+rsp], xmm3
        movsd     xmm4, QWORD PTR [96+rsp]
        mov       r10d, DWORD PTR [96+rsp]
        mov       r8d, r10d
        and       r10d, 63
        subsd     xmm4, QWORD PTR [_vmldExpHATab+1032]
        movsd     QWORD PTR [88+rsp], xmm4
        lea       edx, DWORD PTR [r10+r10]
        movsd     xmm5, QWORD PTR [88+rsp]
        lea       r11d, DWORD PTR [1+r10+r10]
        mulsd     xmm5, QWORD PTR [_vmldExpHATab+1104]
        lea       r10, QWORD PTR [__ImageBase]
        movsd     xmm0, QWORD PTR [88+rsp]
        subsd     xmm2, xmm5
        mulsd     xmm0, QWORD PTR [_vmldExpHATab+1112]
        shr       r8d, 6
        subsd     xmm2, xmm0
        comisd    xmm1, QWORD PTR [_vmldExpHATab+1088]
        movsd     xmm0, QWORD PTR [_vmldExpHATab+1072]
        lea       ecx, DWORD PTR [1023+r8]
        mulsd     xmm0, xmm2
        addsd     xmm0, QWORD PTR [_vmldExpHATab+1064]
        mulsd     xmm0, xmm2
        addsd     xmm0, QWORD PTR [_vmldExpHATab+1056]
        mulsd     xmm0, xmm2
        addsd     xmm0, QWORD PTR [_vmldExpHATab+1048]
        mulsd     xmm0, xmm2
        addsd     xmm0, QWORD PTR [_vmldExpHATab+1040]
        mulsd     xmm0, xmm2
        mulsd     xmm0, xmm2
        addsd     xmm0, xmm2
        movsd     xmm2, QWORD PTR [imagerel(_vmldExpHATab)+r10+rdx*8]
        addsd     xmm0, QWORD PTR [imagerel(_vmldExpHATab)+r10+r11*8]
        mulsd     xmm0, xmm2
        jb        _B6_10

_B6_7::

        and       ecx, 2047
        addsd     xmm0, xmm2
        cmp       ecx, 2046
        ja        _B6_9

_B6_8::

        movzx     edx, WORD PTR [_vmldExpHATab+1142]
        shl       ecx, 4
        and       edx, -32753
        or        edx, ecx
        mov       WORD PTR [86+rsp], dx
        movsd     xmm1, QWORD PTR [80+rsp]
        mulsd     xmm0, xmm1
        cvtsd2ss  xmm0, xmm0
        movss     DWORD PTR [r9], xmm0
        add       rsp, 104
        ret

_B6_9::

        dec       ecx
        and       ecx, 2047
        movzx     edx, WORD PTR [86+rsp]
        shl       ecx, 4
        and       edx, -32753
        or        edx, ecx
        mov       WORD PTR [86+rsp], dx
        movsd     xmm1, QWORD PTR [80+rsp]
        mulsd     xmm0, xmm1
        mulsd     xmm0, QWORD PTR [_vmldExpHATab+1152]
        cvtsd2ss  xmm0, xmm0
        movss     DWORD PTR [r9], xmm0
        add       rsp, 104
        ret

_B6_10::

        add       r8d, 1083
        and       r8d, 2047
        mov       eax, r8d
        movzx     edx, WORD PTR [86+rsp]
        shl       eax, 4
        and       edx, -32753
        or        edx, eax
        mov       WORD PTR [86+rsp], dx
        movsd     xmm3, QWORD PTR [80+rsp]
        mulsd     xmm0, xmm3
        mulsd     xmm3, xmm2
        movaps    xmm1, xmm0
        addsd     xmm1, xmm3
        cmp       r8d, 50
        ja        _B6_12

_B6_11::

        movsd     xmm0, QWORD PTR [_vmldExpHATab+1160]
        mulsd     xmm1, xmm0
        cvtsd2ss  xmm1, xmm1
        jmp       _B6_13

_B6_12::

        movsd     QWORD PTR [32+rsp], xmm1
        movsd     xmm1, QWORD PTR [32+rsp]
        subsd     xmm3, xmm1
        movsd     QWORD PTR [40+rsp], xmm3
        movsd     xmm2, QWORD PTR [40+rsp]
        addsd     xmm2, xmm0
        movsd     QWORD PTR [40+rsp], xmm2
        movsd     xmm0, QWORD PTR [32+rsp]
        mulsd     xmm0, QWORD PTR [_vmldExpHATab+1168]
        movsd     QWORD PTR [48+rsp], xmm0
        movsd     xmm4, QWORD PTR [32+rsp]
        movsd     xmm3, QWORD PTR [48+rsp]
        addsd     xmm4, xmm3
        movsd     QWORD PTR [56+rsp], xmm4
        movsd     xmm0, QWORD PTR [56+rsp]
        movsd     xmm5, QWORD PTR [48+rsp]
        subsd     xmm0, xmm5
        movsd     QWORD PTR [64+rsp], xmm0
        movsd     xmm2, QWORD PTR [32+rsp]
        movsd     xmm1, QWORD PTR [64+rsp]
        subsd     xmm2, xmm1
        movsd     QWORD PTR [72+rsp], xmm2
        movsd     xmm4, QWORD PTR [40+rsp]
        movsd     xmm3, QWORD PTR [72+rsp]
        addsd     xmm4, xmm3
        movsd     QWORD PTR [72+rsp], xmm4
        movsd     xmm0, QWORD PTR [64+rsp]
        mulsd     xmm0, QWORD PTR [_vmldExpHATab+1160]
        movsd     QWORD PTR [64+rsp], xmm0
        movsd     xmm1, QWORD PTR [72+rsp]
        mulsd     xmm1, QWORD PTR [_vmldExpHATab+1160]
        movsd     QWORD PTR [72+rsp], xmm1
        movsd     xmm1, QWORD PTR [64+rsp]
        movsd     xmm5, QWORD PTR [72+rsp]
        addsd     xmm1, xmm5
        cvtsd2ss  xmm1, xmm1

_B6_13::

        movss     DWORD PTR [r9], xmm1
        mov       eax, 4
        add       rsp, 104
        ret

_B6_14::

        movsd     xmm0, QWORD PTR [_vmldExpHATab+1120]
        mov       eax, 4
        mulsd     xmm0, xmm0
        cvtsd2ss  xmm0, xmm0
        movss     DWORD PTR [r9], xmm0
        add       rsp, 104
        ret

_B6_15::

        movsd     xmm0, QWORD PTR [_vmldExpHATab+1128]
        mov       eax, 3
        mulsd     xmm0, xmm0
        cvtsd2ss  xmm0, xmm0
        movss     DWORD PTR [r9], xmm0
        add       rsp, 104
        ret

_B6_16::

        movsd     xmm0, QWORD PTR [_vmldExpHATab+1144]
        addsd     xmm0, xmm1
        cvtsd2ss  xmm0, xmm0
        movss     DWORD PTR [r9], xmm0

_B6_17::

        add       rsp, 104
        ret

_B6_18::

        mov       DWORD PTR [r9], -1082130432
        add       rsp, 104
        ret

_B6_19::

        mov       dl, BYTE PTR [3+rcx]
        and       dl, -128
        cmp       dl, -128
        je        _B6_21

_B6_20::

        mulss     xmm1, xmm1
        movss     DWORD PTR [r9], xmm1
        add       rsp, 104
        ret

_B6_21::

        test      DWORD PTR [rcx], 8388607
        jne       _B6_20

_B6_22::

        movsd     xmm0, QWORD PTR [_vmldExpHATab+1136]
        cvtsd2ss  xmm0, xmm0
        movss     DWORD PTR [r9], xmm0
        add       rsp, 104
        ret
        ALIGN     16

_B6_23::

__svml_sexpm1_ha_cout_rare_internal ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_sexpm1_ha_cout_rare_internal_B1_B22:
	DD	67585
	DD	49672

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B6_1
	DD	imagerel _B6_23
	DD	imagerel _unwind___svml_sexpm1_ha_cout_rare_internal_B1_B22

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_RDATA	SEGMENT     READ PAGE   'DATA'
	ALIGN  32
	PUBLIC __svml_sexpm1_ha_data_internal_avx512
__svml_sexpm1_ha_data_internal_avx512	DD	1065353216
	DD	1065724611
	DD	1066112450
	DD	1066517459
	DD	1066940400
	DD	1067382066
	DD	1067843287
	DD	1068324927
	DD	1068827891
	DD	1069353124
	DD	1069901610
	DD	1070474380
	DD	1071072509
	DD	1071697119
	DD	1072349383
	DD	1073030525
	DD	0
	DD	860277610
	DD	2991457809
	DD	860562562
	DD	856238081
	DD	857441778
	DD	3003456168
	DD	2986372182
	DD	848495278
	DD	3003311522
	DD	833868005
	DD	835836658
	DD	2994528642
	DD	2981408986
	DD	2983366846
	DD	833659207
	DD	1069066811
	DD	1069066811
	DD	1069066811
	DD	1069066811
	DD	1069066811
	DD	1069066811
	DD	1069066811
	DD	1069066811
	DD	1069066811
	DD	1069066811
	DD	1069066811
	DD	1069066811
	DD	1069066811
	DD	1069066811
	DD	1069066811
	DD	1069066811
	DD	1228931072
	DD	1228931072
	DD	1228931072
	DD	1228931072
	DD	1228931072
	DD	1228931072
	DD	1228931072
	DD	1228931072
	DD	1228931072
	DD	1228931072
	DD	1228931072
	DD	1228931072
	DD	1228931072
	DD	1228931072
	DD	1228931072
	DD	1228931072
	DD	1118652779
	DD	1118652779
	DD	1118652779
	DD	1118652779
	DD	1118652779
	DD	1118652779
	DD	1118652779
	DD	1118652779
	DD	1118652779
	DD	1118652779
	DD	1118652779
	DD	1118652779
	DD	1118652779
	DD	1118652779
	DD	1118652779
	DD	1118652779
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
	DD	1060205080
	DD	1060205080
	DD	1060205080
	DD	1060205080
	DD	1060205080
	DD	1060205080
	DD	1060205080
	DD	1060205080
	DD	1060205080
	DD	1060205080
	DD	1060205080
	DD	1060205080
	DD	1060205080
	DD	1060205080
	DD	1060205080
	DD	1060205080
	DD	2969756424
	DD	2969756424
	DD	2969756424
	DD	2969756424
	DD	2969756424
	DD	2969756424
	DD	2969756424
	DD	2969756424
	DD	2969756424
	DD	2969756424
	DD	2969756424
	DD	2969756424
	DD	2969756424
	DD	2969756424
	DD	2969756424
	DD	2969756424
	DD	3263168512
	DD	3263168512
	DD	3263168512
	DD	3263168512
	DD	3263168512
	DD	3263168512
	DD	3263168512
	DD	3263168512
	DD	3263168512
	DD	3263168512
	DD	3263168512
	DD	3263168512
	DD	3263168512
	DD	3263168512
	DD	3263168512
	DD	3263168512
	DD	3221225471
	DD	3221225471
	DD	3221225471
	DD	3221225471
	DD	3221225471
	DD	3221225471
	DD	3221225471
	DD	3221225471
	DD	3221225471
	DD	3221225471
	DD	3221225471
	DD	3221225471
	DD	3221225471
	DD	3221225471
	DD	3221225471
	DD	3221225471
	DD	1026207428
	DD	1026207428
	DD	1026207428
	DD	1026207428
	DD	1026207428
	DD	1026207428
	DD	1026207428
	DD	1026207428
	DD	1026207428
	DD	1026207428
	DD	1026207428
	DD	1026207428
	DD	1026207428
	DD	1026207428
	DD	1026207428
	DD	1026207428
	DD	1042984644
	DD	1042984644
	DD	1042984644
	DD	1042984644
	DD	1042984644
	DD	1042984644
	DD	1042984644
	DD	1042984644
	DD	1042984644
	DD	1042984644
	DD	1042984644
	DD	1042984644
	DD	1042984644
	DD	1042984644
	DD	1042984644
	DD	1042984644
	DD	1056964608
	DD	1056964608
	DD	1056964608
	DD	1056964608
	DD	1056964608
	DD	1056964608
	DD	1056964608
	DD	1056964608
	DD	1056964608
	DD	1056964608
	DD	1056964608
	DD	1056964608
	DD	1056964608
	DD	1056964608
	DD	1056964608
	DD	1056964608
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
	PUBLIC __svml_sexpm1_ha_data_internal
__svml_sexpm1_ha_data_internal	DD	0
	DD	0
	DD	90112
	DD	958021240
	DD	184320
	DD	3097385429
	DD	278528
	DD	3107645322
	DD	372736
	DD	3106378502
	DD	466944
	DD	3080612193
	DD	565248
	DD	3112054766
	DD	659456
	DD	958049961
	DD	757760
	DD	959986813
	DD	860160
	DD	3106149520
	DD	958464
	DD	956772713
	DD	1060864
	DD	946741642
	DD	1163264
	DD	955571435
	DD	1269760
	DD	3107625502
	DD	1372160
	DD	958264608
	DD	1478656
	DD	956671716
	DD	1585152
	DD	964561458
	DD	1695744
	DD	923477569
	DD	1806336
	DD	3098644875
	DD	1916928
	DD	3088264298
	DD	2027520
	DD	958811154
	DD	2142208
	DD	924983155
	DD	2256896
	DD	923141657
	DD	2371584
	DD	958576065
	DD	2490368
	DD	3088362279
	DD	2609152
	DD	3097170630
	DD	2727936
	DD	942496373
	DD	2850816
	DD	3107506507
	DD	2973696
	DD	3111658168
	DD	3096576
	DD	3104893523
	DD	3219456
	DD	956129397
	DD	3346432
	DD	945949013
	DD	3473408
	DD	958293631
	DD	3604480
	DD	3097200438
	DD	3735552
	DD	3104446228
	DD	3866624
	DD	3081063917
	DD	4001792
	DD	3110833173
	DD	4132864
	DD	963851983
	DD	4272128
	DD	3102526822
	DD	4407296
	DD	962173765
	DD	4546560
	DD	962937387
	DD	4689920
	DD	3097677269
	DD	4833280
	DD	3108523826
	DD	4976640
	DD	3103440963
	DD	5120000
	DD	957448868
	DD	5267456
	DD	952956604
	DD	5419008
	DD	3111931330
	DD	5566464
	DD	953592564
	DD	5718016
	DD	958372187
	DD	5873664
	DD	3099234146
	DD	6029312
	DD	3104378117
	DD	6184960
	DD	938771038
	DD	6344704
	DD	3100132597
	DD	6504448
	DD	3077380739
	DD	6668288
	DD	3110019856
	DD	6832128
	DD	3109205496
	DD	6995968
	DD	935782365
	DD	7163904
	DD	3083854118
	DD	7331840
	DD	957821820
	DD	7503872
	DD	943431213
	DD	7675904
	DD	959423141
	DD	7852032
	DD	937390859
	DD	8028160
	DD	955037405
	DD	8208384
	DD	3095001478
	DD	1042983615
	DD	1042983615
	DD	1042983615
	DD	1042983615
	DD	1042983615
	DD	1042983615
	DD	1042983615
	DD	1042983615
	DD	1042983615
	DD	1042983615
	DD	1042983615
	DD	1042983615
	DD	1042983615
	DD	1042983615
	DD	1042983615
	DD	1042983615
	DD	1056964623
	DD	1056964623
	DD	1056964623
	DD	1056964623
	DD	1056964623
	DD	1056964623
	DD	1056964623
	DD	1056964623
	DD	1056964623
	DD	1056964623
	DD	1056964623
	DD	1056964623
	DD	1056964623
	DD	1056964623
	DD	1056964623
	DD	1056964623
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
	DD	1119398459
	DD	1119398459
	DD	1119398459
	DD	1119398459
	DD	1119398459
	DD	1119398459
	DD	1119398459
	DD	1119398459
	DD	1119398459
	DD	1119398459
	DD	1119398459
	DD	1119398459
	DD	1119398459
	DD	1119398459
	DD	1119398459
	DD	1119398459
	DD	1009876992
	DD	1009876992
	DD	1009876992
	DD	1009876992
	DD	1009876992
	DD	1009876992
	DD	1009876992
	DD	1009876992
	DD	1009876992
	DD	1009876992
	DD	1009876992
	DD	1009876992
	DD	1009876992
	DD	1009876992
	DD	1009876992
	DD	1009876992
	DD	3059646595
	DD	3059646595
	DD	3059646595
	DD	3059646595
	DD	3059646595
	DD	3059646595
	DD	3059646595
	DD	3059646595
	DD	3059646595
	DD	3059646595
	DD	3059646595
	DD	3059646595
	DD	3059646595
	DD	3059646595
	DD	3059646595
	DD	3059646595
	DD	1240530432
	DD	1240530432
	DD	1240530432
	DD	1240530432
	DD	1240530432
	DD	1240530432
	DD	1240530432
	DD	1240530432
	DD	1240530432
	DD	1240530432
	DD	1240530432
	DD	1240530432
	DD	1240530432
	DD	1240530432
	DD	1240530432
	DD	1240530432
	DD	504
	DD	504
	DD	504
	DD	504
	DD	504
	DD	504
	DD	504
	DD	504
	DD	504
	DD	504
	DD	504
	DD	504
	DD	504
	DD	504
	DD	504
	DD	504
	DD	130560
	DD	130560
	DD	130560
	DD	130560
	DD	130560
	DD	130560
	DD	130560
	DD	130560
	DD	130560
	DD	130560
	DD	130560
	DD	130560
	DD	130560
	DD	130560
	DD	130560
	DD	130560
	DD	4294963200
	DD	4294963200
	DD	4294963200
	DD	4294963200
	DD	4294963200
	DD	4294963200
	DD	4294963200
	DD	4294963200
	DD	4294963200
	DD	4294963200
	DD	4294963200
	DD	4294963200
	DD	4294963200
	DD	4294963200
	DD	4294963200
	DD	4294963200
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
	DD	1118652779
	DD	1118652779
	DD	1118652779
	DD	1118652779
	DD	1118652779
	DD	1118652779
	DD	1118652779
	DD	1118652779
	DD	1118652779
	DD	1118652779
	DD	1118652779
	DD	1118652779
	DD	1118652779
	DD	1118652779
	DD	1118652779
	DD	1118652779
	DD	1111009851
	DD	1111009851
	DD	1111009851
	DD	1111009851
	DD	1111009851
	DD	1111009851
	DD	1111009851
	DD	1111009851
	DD	1111009851
	DD	1111009851
	DD	1111009851
	DD	1111009851
	DD	1111009851
	DD	1111009851
	DD	1111009851
	DD	1111009851
	DD	1018262040
	DD	1018262040
	DD	1018262040
	DD	1018262040
	DD	1018262040
	DD	1018262040
	DD	1018262040
	DD	1018262040
	DD	1018262040
	DD	1018262040
	DD	1018262040
	DD	1018262040
	DD	1018262040
	DD	1018262040
	DD	1018262040
	DD	1018262040
	DD	1245724544
	DD	1245724544
	DD	1245724544
	DD	1245724544
	DD	1245724544
	DD	1245724544
	DD	1245724544
	DD	1245724544
	DD	1245724544
	DD	1245724544
	DD	1245724544
	DD	1245724544
	DD	1245724544
	DD	1245724544
	DD	1245724544
	DD	1245724544
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
	DD	32640
	DD	32640
	DD	32640
	DD	32640
	DD	32640
	DD	32640
	DD	32640
	DD	32640
	DD	32640
	DD	32640
	DD	32640
	DD	32640
	DD	32640
	DD	32640
	DD	32640
	DD	32640
_vmldExpHATab	DD	0
	DD	1072693248
	DD	0
	DD	0
	DD	1048019041
	DD	1072704666
	DD	2631457885
	DD	3161546771
	DD	3541402996
	DD	1072716208
	DD	896005651
	DD	1015861842
	DD	410360776
	DD	1072727877
	DD	1642514529
	DD	1012987726
	DD	1828292879
	DD	1072739672
	DD	1568897901
	DD	1016568486
	DD	852742562
	DD	1072751596
	DD	1882168529
	DD	1010744893
	DD	3490863953
	DD	1072763649
	DD	707771662
	DD	3163903570
	DD	2930322912
	DD	1072775834
	DD	3117806614
	DD	3163670819
	DD	1014845819
	DD	1072788152
	DD	3936719688
	DD	3162512149
	DD	3949972341
	DD	1072800603
	DD	1058231231
	DD	1015777676
	DD	828946858
	DD	1072813191
	DD	1044000608
	DD	1016786167
	DD	2288159958
	DD	1072825915
	DD	1151779725
	DD	1015705409
	DD	1853186616
	DD	1072838778
	DD	3819481236
	DD	1016499965
	DD	1709341917
	DD	1072851781
	DD	2552227826
	DD	1015039787
	DD	4112506593
	DD	1072864925
	DD	1829350193
	DD	1015216097
	DD	2799960843
	DD	1072878213
	DD	1913391796
	DD	1015756674
	DD	171030293
	DD	1072891646
	DD	1303423926
	DD	1015238005
	DD	2992903935
	DD	1072905224
	DD	1574172746
	DD	1016061241
	DD	926591435
	DD	1072918951
	DD	3427487848
	DD	3163704045
	DD	887463927
	DD	1072932827
	DD	1049900754
	DD	3161575912
	DD	1276261410
	DD	1072946854
	DD	2804567149
	DD	1015390024
	DD	569847338
	DD	1072961034
	DD	1209502043
	DD	3159926671
	DD	1617004845
	DD	1072975368
	DD	1623370769
	DD	1011049453
	DD	3049340112
	DD	1072989858
	DD	3667985273
	DD	1013894369
	DD	3577096743
	DD	1073004506
	DD	3145379760
	DD	1014403278
	DD	1990012071
	DD	1073019314
	DD	7447438
	DD	3163526196
	DD	1453150082
	DD	1073034283
	DD	3171891295
	DD	3162037958
	DD	917841882
	DD	1073049415
	DD	419288974
	DD	1016280325
	DD	3712504873
	DD	1073064711
	DD	3793507337
	DD	1016095713
	DD	363667784
	DD	1073080175
	DD	728023093
	DD	1016345318
	DD	2956612997
	DD	1073095806
	DD	1005538728
	DD	3163304901
	DD	2186617381
	DD	1073111608
	DD	2018924632
	DD	3163803357
	DD	1719614413
	DD	1073127582
	DD	3210617384
	DD	3163796463
	DD	1013258799
	DD	1073143730
	DD	3094194670
	DD	3160631279
	DD	3907805044
	DD	1073160053
	DD	2119843535
	DD	3161988964
	DD	1447192521
	DD	1073176555
	DD	508946058
	DD	3162904882
	DD	1944781191
	DD	1073193236
	DD	3108873501
	DD	3162190556
	DD	919555682
	DD	1073210099
	DD	2882956373
	DD	1013312481
	DD	2571947539
	DD	1073227145
	DD	4047189812
	DD	3163777462
	DD	2604962541
	DD	1073244377
	DD	3631372142
	DD	3163870288
	DD	1110089947
	DD	1073261797
	DD	3253791412
	DD	1015920431
	DD	2568320822
	DD	1073279406
	DD	1509121860
	DD	1014756995
	DD	2966275557
	DD	1073297207
	DD	2339118633
	DD	3160254904
	DD	2682146384
	DD	1073315202
	DD	586480042
	DD	3163702083
	DD	2191782032
	DD	1073333393
	DD	730975783
	DD	1014083580
	DD	2069751141
	DD	1073351782
	DD	576856675
	DD	3163014404
	DD	2990417245
	DD	1073370371
	DD	3552361237
	DD	3163667409
	DD	1434058175
	DD	1073389163
	DD	1853053619
	DD	1015310724
	DD	2572866477
	DD	1073408159
	DD	2462790535
	DD	1015814775
	DD	3092190715
	DD	1073427362
	DD	1457303226
	DD	3159737305
	DD	4076559943
	DD	1073446774
	DD	950899508
	DD	3160987380
	DD	2420883922
	DD	1073466398
	DD	174054861
	DD	1014300631
	DD	3716502172
	DD	1073486235
	DD	816778419
	DD	1014197934
	DD	777507147
	DD	1073506289
	DD	3507050924
	DD	1015341199
	DD	3706687593
	DD	1073526560
	DD	1821514088
	DD	1013410604
	DD	1242007932
	DD	1073547053
	DD	1073740399
	DD	3163532637
	DD	3707479175
	DD	1073567768
	DD	2789017511
	DD	1014276997
	DD	64696965
	DD	1073588710
	DD	3586233004
	DD	1015962192
	DD	863738719
	DD	1073609879
	DD	129252895
	DD	3162690849
	DD	3884662774
	DD	1073631278
	DD	1614448851
	DD	1014281732
	DD	2728693978
	DD	1073652911
	DD	2413007344
	DD	3163551506
	DD	3999357479
	DD	1073674779
	DD	1101668360
	DD	1015989180
	DD	1533953344
	DD	1073696886
	DD	835814894
	DD	1015702697
	DD	2174652632
	DD	1073719233
	DD	1301400989
	DD	1014466875
	DD	1697350398
	DD	1079448903
	DD	0
	DD	1127743488
	DD	0
	DD	1071644672
	DD	1431652600
	DD	1069897045
	DD	1431670732
	DD	1067799893
	DD	984555731
	DD	1065423122
	DD	472530941
	DD	1062650218
	DD	3758096384
	DD	1079389762
	DD	3758096384
	DD	3226850697
	DD	2147483648
	DD	3227123254
	DD	4277796864
	DD	1065758274
	DD	3164486458
	DD	1025308570
	DD	1
	DD	1048576
	DD	4294967295
	DD	2146435071
	DD	0
	DD	0
	DD	0
	DD	1072693248
	DD	0
	DD	1073741824
	DD	0
	DD	1009778688
	DD	0
	DD	1106771968
	DD 2 DUP (0H)	
_2il0floatpacket_16	DD	04b400000H,04b400000H,04b400000H,04b400000H
_2il0floatpacket_75	DD	0bf800000H
_RDATA	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS
EXTRN	__ImageBase:PROC
EXTRN	_fltused:BYTE
	ENDIF
	END
