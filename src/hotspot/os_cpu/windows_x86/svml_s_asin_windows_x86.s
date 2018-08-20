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
	PUBLIC __svml_asinf8_ha_l9

__svml_asinf8_ha_l9	PROC

_B1_1::

        DB        243
        DB        15
        DB        30
        DB        250
L1::

        sub       rsp, 296
        vmovups   XMMWORD PTR [256+rsp], xmm13
        vmovups   XMMWORD PTR [240+rsp], xmm10
        vmovups   XMMWORD PTR [224+rsp], xmm9
        vmovups   XMMWORD PTR [208+rsp], xmm6
        mov       QWORD PTR [272+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovups   ymm4, YMMWORD PTR [rcx]
        and       r13, -64
        vmovups   ymm3, YMMWORD PTR [__svml_sasin_ha_data_internal]
        vmovups   ymm6, YMMWORD PTR [__svml_sasin_ha_data_internal+64]
        vandps    ymm5, ymm3, ymm4
        vmovdqa   ymm0, ymm5
        vfnmadd213ps ymm0, ymm6, ymm6
        vmulps    ymm1, ymm5, ymm5
        vcmpgt_oqps ymm13, ymm5, YMMWORD PTR [__svml_sasin_ha_data_internal+256]
        vcmplt_oqps ymm2, ymm0, YMMWORD PTR [__svml_sasin_ha_data_internal+192]
        vminps    ymm9, ymm1, ymm0
        vcmpnlt_uqps ymm1, ymm5, ymm6
        vrsqrtps  ymm10, ymm0
        vaddps    ymm0, ymm0, ymm0
        vandps    ymm6, ymm0, YMMWORD PTR [__svml_sasin_ha_data_internal+320]
        vmovmskps edx, ymm13
        vandnps   ymm13, ymm2, ymm10
        test      edx, edx
        vsubps    ymm10, ymm0, ymm6
        vmovups   ymm2, YMMWORD PTR [__svml_sasin_ha_data_internal+448]
        vmulps    ymm0, ymm13, ymm6
        vmulps    ymm6, ymm13, ymm10
        vfmsub231ps ymm2, ymm13, ymm0
        vmulps    ymm13, ymm13, ymm6
        vaddps    ymm10, ymm0, ymm6
        vaddps    ymm13, ymm2, ymm13
        vmovups   ymm2, YMMWORD PTR [__svml_sasin_ha_data_internal+512]
        vmulps    ymm10, ymm10, ymm13
        vfmadd213ps ymm2, ymm13, YMMWORD PTR [__svml_sasin_ha_data_internal+576]
        vmovups   ymm13, YMMWORD PTR [__svml_sasin_ha_data_internal+768]
        vfmsub213ps ymm2, ymm10, ymm6
        vmovups   ymm10, YMMWORD PTR [__svml_sasin_ha_data_internal+640]
        vfmadd213ps ymm13, ymm9, YMMWORD PTR [__svml_sasin_ha_data_internal+832]
        vmulps    ymm6, ymm9, ymm9
        vfmadd213ps ymm10, ymm9, YMMWORD PTR [__svml_sasin_ha_data_internal+704]
        vfmadd213ps ymm10, ymm6, ymm13
        vsubps    ymm13, ymm2, ymm0
        vaddps    ymm2, ymm2, YMMWORD PTR [__svml_sasin_ha_data_internal+1024]
        vfmadd213ps ymm10, ymm9, YMMWORD PTR [__svml_sasin_ha_data_internal+896]
        vmulps    ymm9, ymm9, ymm10
        vmovups   ymm10, YMMWORD PTR [__svml_sasin_ha_data_internal+960]
        vsubps    ymm6, ymm10, ymm0
        vsubps    ymm10, ymm10, ymm6
        vsubps    ymm0, ymm0, ymm10
        vsubps    ymm2, ymm2, ymm0
        vandps    ymm0, ymm13, ymm1
        vandnps   ymm5, ymm1, ymm5
        vandps    ymm13, ymm2, ymm1
        vorps     ymm0, ymm0, ymm5
        vfmadd213ps ymm9, ymm0, ymm13
        vandps    ymm2, ymm6, ymm1
        vorps     ymm1, ymm2, ymm5
        vaddps    ymm2, ymm1, ymm9
        vandnps   ymm3, ymm3, ymm4
        mov       QWORD PTR [280+rsp], r13
        vxorps    ymm0, ymm2, ymm3
        jne       _B1_3

_B1_2::

        vmovups   xmm6, XMMWORD PTR [208+rsp]
        vmovups   xmm9, XMMWORD PTR [224+rsp]
        vmovups   xmm10, XMMWORD PTR [240+rsp]
        vmovups   xmm13, XMMWORD PTR [256+rsp]
        mov       r13, QWORD PTR [272+rsp]
        add       rsp, 296
        ret

_B1_3::

        vmovups   YMMWORD PTR [r13], ymm4
        vmovups   YMMWORD PTR [64+r13], ymm0

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
        cmp       ebx, 8
        jl        _B1_7

_B1_9::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        vmovups   ymm0, YMMWORD PTR [64+r13]
        jmp       _B1_2

_B1_10::

        vzeroupper
        lea       rcx, QWORD PTR [r13+rbx*4]
        lea       rdx, QWORD PTR [64+r13+rbx*4]

        call      __svml_sasin_ha_cout_rare_internal
        jmp       _B1_8
        ALIGN     16

_B1_11::

__svml_asinf8_ha_l9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_asinf8_ha_l9_B1_B3:
	DD	800513
	DD	2282551
	DD	878639
	DD	956454
	DD	1026077
	DD	1103892
	DD	2425099

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B1_1
	DD	imagerel _B1_6
	DD	imagerel _unwind___svml_asinf8_ha_l9_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_asinf8_ha_l9_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B1_1
	DD	imagerel _B1_6
	DD	imagerel _unwind___svml_asinf8_ha_l9_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B1_6
	DD	imagerel _B1_11
	DD	imagerel _unwind___svml_asinf8_ha_l9_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST1:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_asinf8_ha_e9

__svml_asinf8_ha_e9	PROC

_B2_1::

        DB        243
        DB        15
        DB        30
        DB        250
L16::

        sub       rsp, 312
        vmovups   XMMWORD PTR [272+rsp], xmm15
        vmovups   XMMWORD PTR [208+rsp], xmm10
        vpxor     xmm15, xmm15, xmm15
        vmovups   XMMWORD PTR [256+rsp], xmm9
        vmovups   XMMWORD PTR [224+rsp], xmm7
        vmovups   XMMWORD PTR [240+rsp], xmm6
        mov       QWORD PTR [288+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovups   ymm10, YMMWORD PTR [rcx]
        and       r13, -64
        vmovups   ymm9, YMMWORD PTR [__svml_sasin_ha_data_internal]
        vmovups   ymm7, YMMWORD PTR [__svml_sasin_ha_data_internal+64]
        vandps    ymm6, ymm9, ymm10
        vmulps    ymm4, ymm6, ymm7
        vmulps    ymm1, ymm6, ymm6
        vcmpgt_oqps ymm3, ymm6, YMMWORD PTR [__svml_sasin_ha_data_internal+256]
        vsubps    ymm5, ymm7, ymm4
        vcmpnlt_uqps ymm7, ymm6, ymm7
        vminps    ymm4, ymm1, ymm5
        vandnps   ymm6, ymm7, ymm6
        vandnps   ymm9, ymm9, ymm10
        mov       QWORD PTR [296+rsp], r13
        vextractf128 xmm0, ymm3, 1
        vpackssdw xmm2, xmm3, xmm0
        vrsqrtps  ymm0, ymm5
        vpacksswb xmm1, xmm2, xmm15
        vmovups   ymm2, YMMWORD PTR [__svml_sasin_ha_data_internal+320]
        vcmplt_oqps ymm3, ymm5, YMMWORD PTR [__svml_sasin_ha_data_internal+192]
        vaddps    ymm5, ymm5, ymm5
        vpmovmskb edx, xmm1
        vandnps   ymm0, ymm3, ymm0
        vandps    ymm15, ymm5, ymm2
        vsubps    ymm1, ymm5, ymm15
        vmulps    ymm5, ymm0, ymm15
        vmulps    ymm1, ymm0, ymm1
        vandps    ymm2, ymm5, ymm2
        vsubps    ymm15, ymm5, ymm2
        vmulps    ymm2, ymm0, ymm2
        vmulps    ymm3, ymm0, ymm1
        vmulps    ymm0, ymm0, ymm15
        vsubps    ymm15, ymm2, YMMWORD PTR [__svml_sasin_ha_data_internal+448]
        vaddps    ymm3, ymm3, ymm15
        vaddps    ymm15, ymm5, ymm1
        vaddps    ymm2, ymm0, ymm3
        vmulps    ymm0, ymm2, YMMWORD PTR [__svml_sasin_ha_data_internal+512]
        vaddps    ymm3, ymm0, YMMWORD PTR [__svml_sasin_ha_data_internal+576]
        vmulps    ymm0, ymm2, ymm15
        vmulps    ymm15, ymm4, YMMWORD PTR [__svml_sasin_ha_data_internal+768]
        vmulps    ymm2, ymm3, ymm0
        vmulps    ymm0, ymm4, ymm4
        vsubps    ymm3, ymm2, ymm1
        vaddps    ymm2, ymm15, YMMWORD PTR [__svml_sasin_ha_data_internal+832]
        vmulps    ymm1, ymm4, YMMWORD PTR [__svml_sasin_ha_data_internal+640]
        vaddps    ymm1, ymm1, YMMWORD PTR [__svml_sasin_ha_data_internal+704]
        vmulps    ymm15, ymm1, ymm0
        vaddps    ymm1, ymm2, ymm15
        vsubps    ymm15, ymm3, ymm5
        vaddps    ymm3, ymm3, YMMWORD PTR [__svml_sasin_ha_data_internal+1024]
        vmulps    ymm0, ymm4, ymm1
        vaddps    ymm2, ymm0, YMMWORD PTR [__svml_sasin_ha_data_internal+896]
        vmovups   ymm0, YMMWORD PTR [__svml_sasin_ha_data_internal+960]
        vmulps    ymm4, ymm4, ymm2
        vsubps    ymm1, ymm0, ymm5
        vsubps    ymm2, ymm0, ymm1
        vsubps    ymm5, ymm5, ymm2
        vsubps    ymm0, ymm3, ymm5
        vandps    ymm2, ymm15, ymm7
        vandps    ymm15, ymm0, ymm7
        vandps    ymm1, ymm1, ymm7
        vorps     ymm7, ymm2, ymm6
        vmulps    ymm4, ymm7, ymm4
        vorps     ymm0, ymm1, ymm6
        vaddps    ymm1, ymm15, ymm4
        vaddps    ymm2, ymm0, ymm1
        vxorps    ymm0, ymm2, ymm9
        test      dl, dl
        jne       _B2_3

_B2_2::

        vmovups   xmm6, XMMWORD PTR [240+rsp]
        vmovups   xmm7, XMMWORD PTR [224+rsp]
        vmovups   xmm9, XMMWORD PTR [256+rsp]
        vmovups   xmm10, XMMWORD PTR [208+rsp]
        vmovups   xmm15, XMMWORD PTR [272+rsp]
        mov       r13, QWORD PTR [288+rsp]
        add       rsp, 312
        ret

_B2_3::

        vmovups   YMMWORD PTR [r13], ymm10
        vmovups   YMMWORD PTR [64+r13], ymm0
        test      edx, edx
        je        _B2_2

_B2_6::

        xor       eax, eax
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, eax
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, edx

_B2_7::

        bt        esi, ebx
        jc        _B2_10

_B2_8::

        inc       ebx
        cmp       ebx, 8
        jl        _B2_7

_B2_9::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        vmovups   ymm0, YMMWORD PTR [64+r13]
        jmp       _B2_2

_B2_10::

        vzeroupper
        lea       rcx, QWORD PTR [r13+rbx*4]
        lea       rdx, QWORD PTR [64+r13+rbx*4]

        call      __svml_sasin_ha_cout_rare_internal
        jmp       _B2_8
        ALIGN     16

_B2_11::

__svml_asinf8_ha_e9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_asinf8_ha_e9_B1_B3:
	DD	935169
	DD	2413637
	DD	1009725
	DD	948276
	DD	1087531
	DD	895005
	DD	1177620
	DD	2556171

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B2_1
	DD	imagerel _B2_6
	DD	imagerel _unwind___svml_asinf8_ha_e9_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_asinf8_ha_e9_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B2_1
	DD	imagerel _B2_6
	DD	imagerel _unwind___svml_asinf8_ha_e9_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B2_6
	DD	imagerel _B2_11
	DD	imagerel _unwind___svml_asinf8_ha_e9_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST2:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_asinf4_ha_l9

__svml_asinf4_ha_l9	PROC

_B3_1::

        DB        243
        DB        15
        DB        30
        DB        250
L33::

        sub       rsp, 280
        vmovups   XMMWORD PTR [240+rsp], xmm13
        vmovups   XMMWORD PTR [224+rsp], xmm10
        vmovups   XMMWORD PTR [208+rsp], xmm9
        vmovups   XMMWORD PTR [192+rsp], xmm6
        mov       QWORD PTR [256+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovups   xmm4, XMMWORD PTR [rcx]
        and       r13, -64
        vmovups   xmm3, XMMWORD PTR [__svml_sasin_ha_data_internal]
        vandps    xmm5, xmm3, xmm4
        vandnps   xmm3, xmm3, xmm4
        vmovups   xmm6, XMMWORD PTR [__svml_sasin_ha_data_internal+64]
        vmovaps   xmm0, xmm5
        vfnmadd213ps xmm0, xmm6, xmm6
        vmulps    xmm1, xmm5, xmm5
        vcmpgtps  xmm13, xmm5, XMMWORD PTR [__svml_sasin_ha_data_internal+256]
        vrsqrtps  xmm10, xmm0
        vcmpltps  xmm2, xmm0, XMMWORD PTR [__svml_sasin_ha_data_internal+192]
        vminps    xmm9, xmm1, xmm0
        vcmpnltps xmm1, xmm5, xmm6
        vaddps    xmm0, xmm0, xmm0
        vmovmskps edx, xmm13
        vandps    xmm6, xmm0, XMMWORD PTR [__svml_sasin_ha_data_internal+320]
        vandnps   xmm13, xmm2, xmm10
        vmovups   xmm2, XMMWORD PTR [__svml_sasin_ha_data_internal+448]
        vandnps   xmm5, xmm1, xmm5
        mov       QWORD PTR [264+rsp], r13
        vsubps    xmm10, xmm0, xmm6
        vmulps    xmm0, xmm13, xmm6
        vmulps    xmm6, xmm13, xmm10
        vfmsub231ps xmm2, xmm13, xmm0
        vmulps    xmm13, xmm13, xmm6
        vaddps    xmm10, xmm0, xmm6
        vaddps    xmm13, xmm2, xmm13
        vmovups   xmm2, XMMWORD PTR [__svml_sasin_ha_data_internal+512]
        vfmadd213ps xmm2, xmm13, XMMWORD PTR [__svml_sasin_ha_data_internal+576]
        vmulps    xmm10, xmm10, xmm13
        vfmsub213ps xmm2, xmm10, xmm6
        vmovups   xmm10, XMMWORD PTR [__svml_sasin_ha_data_internal+640]
        vmovups   xmm13, XMMWORD PTR [__svml_sasin_ha_data_internal+768]
        vfmadd213ps xmm10, xmm9, XMMWORD PTR [__svml_sasin_ha_data_internal+704]
        vfmadd213ps xmm13, xmm9, XMMWORD PTR [__svml_sasin_ha_data_internal+832]
        vmulps    xmm6, xmm9, xmm9
        vfmadd213ps xmm10, xmm6, xmm13
        vsubps    xmm13, xmm2, xmm0
        vaddps    xmm2, xmm2, XMMWORD PTR [__svml_sasin_ha_data_internal+1024]
        vfmadd213ps xmm10, xmm9, XMMWORD PTR [__svml_sasin_ha_data_internal+896]
        vmulps    xmm9, xmm9, xmm10
        vmovups   xmm10, XMMWORD PTR [__svml_sasin_ha_data_internal+960]
        vsubps    xmm6, xmm10, xmm0
        vsubps    xmm10, xmm10, xmm6
        vsubps    xmm0, xmm0, xmm10
        vsubps    xmm2, xmm2, xmm0
        vandps    xmm0, xmm13, xmm1
        vandps    xmm13, xmm2, xmm1
        vorps     xmm0, xmm0, xmm5
        vfmadd213ps xmm9, xmm0, xmm13
        vandps    xmm2, xmm6, xmm1
        vorps     xmm1, xmm2, xmm5
        vaddps    xmm2, xmm1, xmm9
        vxorps    xmm0, xmm2, xmm3
        test      edx, edx
        jne       _B3_3

_B3_2::

        vmovups   xmm6, XMMWORD PTR [192+rsp]
        vmovups   xmm9, XMMWORD PTR [208+rsp]
        vmovups   xmm10, XMMWORD PTR [224+rsp]
        vmovups   xmm13, XMMWORD PTR [240+rsp]
        mov       r13, QWORD PTR [256+rsp]
        add       rsp, 280
        ret

_B3_3::

        vmovups   XMMWORD PTR [r13], xmm4
        vmovups   XMMWORD PTR [64+r13], xmm0

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

        call      __svml_sasin_ha_cout_rare_internal
        jmp       _B3_8
        ALIGN     16

_B3_11::

__svml_asinf4_ha_l9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_asinf4_ha_l9_B1_B3:
	DD	800513
	DD	2151479
	DD	813103
	DD	890918
	DD	960541
	DD	1038356
	DD	2294027

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B3_1
	DD	imagerel _B3_6
	DD	imagerel _unwind___svml_asinf4_ha_l9_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_asinf4_ha_l9_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B3_1
	DD	imagerel _B3_6
	DD	imagerel _unwind___svml_asinf4_ha_l9_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B3_6
	DD	imagerel _B3_11
	DD	imagerel _unwind___svml_asinf4_ha_l9_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST3:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_asinf4_ha_e9

__svml_asinf4_ha_e9	PROC

_B4_1::

        DB        243
        DB        15
        DB        30
        DB        250
L48::

        sub       rsp, 296
        vmovups   XMMWORD PTR [240+rsp], xmm11
        vmovups   XMMWORD PTR [256+rsp], xmm10
        vmovups   XMMWORD PTR [208+rsp], xmm9
        vmovups   XMMWORD PTR [224+rsp], xmm8
        vmovups   XMMWORD PTR [192+rsp], xmm7
        mov       QWORD PTR [272+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovups   xmm10, XMMWORD PTR [rcx]
        and       r13, -64
        vmovups   xmm9, XMMWORD PTR [__svml_sasin_ha_data_internal]
        vmovups   xmm1, XMMWORD PTR [__svml_sasin_ha_data_internal+64]
        vandps    xmm7, xmm9, xmm10
        vmulps    xmm11, xmm7, xmm1
        vandnps   xmm9, xmm9, xmm10
        vcmpgtps  xmm8, xmm7, XMMWORD PTR [__svml_sasin_ha_data_internal+256]
        vmulps    xmm4, xmm7, xmm7
        vsubps    xmm5, xmm1, xmm11
        vmovmskps edx, xmm8
        vcmpnltps xmm8, xmm7, xmm1
        vcmpltps  xmm3, xmm5, XMMWORD PTR [__svml_sasin_ha_data_internal+192]
        vminps    xmm4, xmm4, xmm5
        vrsqrtps  xmm0, xmm5
        vaddps    xmm5, xmm5, xmm5
        vmovups   xmm2, XMMWORD PTR [__svml_sasin_ha_data_internal+320]
        vandnps   xmm0, xmm3, xmm0
        vandps    xmm11, xmm5, xmm2
        vandnps   xmm7, xmm8, xmm7
        mov       QWORD PTR [280+rsp], r13
        vsubps    xmm1, xmm5, xmm11
        vmulps    xmm5, xmm0, xmm11
        vmulps    xmm1, xmm0, xmm1
        vandps    xmm2, xmm5, xmm2
        vmulps    xmm3, xmm0, xmm1
        vsubps    xmm11, xmm5, xmm2
        vmulps    xmm2, xmm0, xmm2
        vmulps    xmm0, xmm0, xmm11
        vsubps    xmm11, xmm2, XMMWORD PTR [__svml_sasin_ha_data_internal+448]
        vaddps    xmm3, xmm3, xmm11
        vaddps    xmm11, xmm5, xmm1
        vaddps    xmm2, xmm0, xmm3
        vmulps    xmm0, xmm2, XMMWORD PTR [__svml_sasin_ha_data_internal+512]
        vaddps    xmm3, xmm0, XMMWORD PTR [__svml_sasin_ha_data_internal+576]
        vmulps    xmm0, xmm2, xmm11
        vmulps    xmm11, xmm4, XMMWORD PTR [__svml_sasin_ha_data_internal+768]
        vmulps    xmm2, xmm3, xmm0
        vmulps    xmm0, xmm4, xmm4
        vsubps    xmm3, xmm2, xmm1
        vmulps    xmm1, xmm4, XMMWORD PTR [__svml_sasin_ha_data_internal+640]
        vaddps    xmm2, xmm11, XMMWORD PTR [__svml_sasin_ha_data_internal+832]
        vaddps    xmm1, xmm1, XMMWORD PTR [__svml_sasin_ha_data_internal+704]
        vmulps    xmm11, xmm1, xmm0
        vaddps    xmm1, xmm2, xmm11
        vsubps    xmm11, xmm3, xmm5
        vaddps    xmm3, xmm3, XMMWORD PTR [__svml_sasin_ha_data_internal+1024]
        vmulps    xmm0, xmm4, xmm1
        vaddps    xmm2, xmm0, XMMWORD PTR [__svml_sasin_ha_data_internal+896]
        vmovups   xmm0, XMMWORD PTR [__svml_sasin_ha_data_internal+960]
        vmulps    xmm4, xmm4, xmm2
        vsubps    xmm1, xmm0, xmm5
        vsubps    xmm2, xmm0, xmm1
        vandps    xmm1, xmm1, xmm8
        vorps     xmm1, xmm1, xmm7
        vsubps    xmm5, xmm5, xmm2
        vandps    xmm2, xmm11, xmm8
        vsubps    xmm0, xmm3, xmm5
        vandps    xmm11, xmm0, xmm8
        vorps     xmm0, xmm2, xmm7
        vmulps    xmm4, xmm0, xmm4
        vaddps    xmm2, xmm11, xmm4
        vaddps    xmm3, xmm1, xmm2
        vxorps    xmm0, xmm3, xmm9
        test      edx, edx
        jne       _B4_3

_B4_2::

        vmovups   xmm7, XMMWORD PTR [192+rsp]
        vmovups   xmm8, XMMWORD PTR [224+rsp]
        vmovups   xmm9, XMMWORD PTR [208+rsp]
        vmovups   xmm10, XMMWORD PTR [256+rsp]
        vmovups   xmm11, XMMWORD PTR [240+rsp]
        mov       r13, QWORD PTR [272+rsp]
        add       rsp, 296
        ret

_B4_3::

        vmovups   XMMWORD PTR [r13], xmm10
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

        call      __svml_sasin_ha_cout_rare_internal
        jmp       _B4_8
        ALIGN     16

_B4_11::

__svml_asinf4_ha_e9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_asinf4_ha_e9_B1_B3:
	DD	933889
	DD	2282560
	DD	817208
	DD	952367
	DD	890918
	DD	1091613
	DD	1030164
	DD	2425099

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B4_1
	DD	imagerel _B4_6
	DD	imagerel _unwind___svml_asinf4_ha_e9_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_asinf4_ha_e9_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B4_1
	DD	imagerel _B4_6
	DD	imagerel _unwind___svml_asinf4_ha_e9_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B4_6
	DD	imagerel _B4_11
	DD	imagerel _unwind___svml_asinf4_ha_e9_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST4:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_asinf4_ha_ex

__svml_asinf4_ha_ex	PROC

_B5_1::

        DB        243
        DB        15
        DB        30
        DB        250
L65::

        sub       rsp, 296
        movups    XMMWORD PTR [256+rsp], xmm15
        movups    XMMWORD PTR [224+rsp], xmm12
        movups    XMMWORD PTR [240+rsp], xmm11
        movups    XMMWORD PTR [192+rsp], xmm10
        movups    XMMWORD PTR [208+rsp], xmm6
        mov       QWORD PTR [272+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        movups    xmm10, XMMWORD PTR [__svml_sasin_ha_data_internal]
        and       r13, -64
        movups    xmm5, XMMWORD PTR [rcx]
        movaps    xmm6, xmm10
        andps     xmm6, xmm5
        andnps    xmm10, xmm5
        movups    xmm2, XMMWORD PTR [__svml_sasin_ha_data_internal+64]
        movaps    xmm4, xmm6
        mulps     xmm4, xmm2
        movaps    xmm11, xmm2
        movups    xmm3, XMMWORD PTR [__svml_sasin_ha_data_internal+256]
        subps     xmm11, xmm4
        cmpltps   xmm3, xmm6
        rsqrtps   xmm1, xmm11
        movmskps  eax, xmm3
        movaps    xmm4, xmm6
        movaps    xmm0, xmm11
        movaps    xmm3, xmm6
        mulps     xmm4, xmm6
        cmpltps   xmm0, XMMWORD PTR [__svml_sasin_ha_data_internal+192]
        cmpnltps  xmm3, xmm2
        minps     xmm4, xmm11
        addps     xmm11, xmm11
        andnps    xmm0, xmm1
        movaps    xmm2, xmm11
        movups    xmm1, XMMWORD PTR [__svml_sasin_ha_data_internal+320]
        movaps    xmm12, xmm0
        andps     xmm2, xmm1
        mov       QWORD PTR [280+rsp], r13
        subps     xmm11, xmm2
        mulps     xmm2, xmm0
        mulps     xmm11, xmm0
        andps     xmm1, xmm2
        movaps    xmm15, xmm2
        mulps     xmm12, xmm11
        subps     xmm15, xmm1
        mulps     xmm1, xmm0
        mulps     xmm0, xmm15
        subps     xmm1, XMMWORD PTR [__svml_sasin_ha_data_internal+448]
        movaps    xmm15, xmm2
        addps     xmm12, xmm1
        addps     xmm15, xmm11
        addps     xmm0, xmm12
        movups    xmm1, XMMWORD PTR [__svml_sasin_ha_data_internal+512]
        mulps     xmm1, xmm0
        mulps     xmm0, xmm15
        addps     xmm1, XMMWORD PTR [__svml_sasin_ha_data_internal+576]
        mulps     xmm1, xmm0
        movaps    xmm0, xmm4
        mulps     xmm0, xmm4
        subps     xmm1, xmm11
        movups    xmm11, XMMWORD PTR [__svml_sasin_ha_data_internal+640]
        mulps     xmm11, xmm4
        movups    xmm12, XMMWORD PTR [__svml_sasin_ha_data_internal+768]
        mulps     xmm12, xmm4
        addps     xmm11, XMMWORD PTR [__svml_sasin_ha_data_internal+704]
        mulps     xmm11, xmm0
        addps     xmm12, XMMWORD PTR [__svml_sasin_ha_data_internal+832]
        movups    xmm15, XMMWORD PTR [__svml_sasin_ha_data_internal+960]
        addps     xmm12, xmm11
        mulps     xmm12, xmm4
        movaps    xmm0, xmm15
        subps     xmm0, xmm2
        addps     xmm12, XMMWORD PTR [__svml_sasin_ha_data_internal+896]
        subps     xmm15, xmm0
        mulps     xmm4, xmm12
        movups    xmm12, XMMWORD PTR [__svml_sasin_ha_data_internal+1024]
        movaps    xmm11, xmm1
        andps     xmm0, xmm3
        subps     xmm11, xmm2
        addps     xmm12, xmm1
        subps     xmm2, xmm15
        andps     xmm11, xmm3
        subps     xmm12, xmm2
        andps     xmm12, xmm3
        andnps    xmm3, xmm6
        orps      xmm11, xmm3
        orps      xmm0, xmm3
        mulps     xmm11, xmm4
        addps     xmm12, xmm11
        addps     xmm0, xmm12
        pxor      xmm0, xmm10
        test      eax, eax
        jne       _B5_3

_B5_2::

        movups    xmm6, XMMWORD PTR [208+rsp]
        movups    xmm10, XMMWORD PTR [192+rsp]
        movups    xmm11, XMMWORD PTR [240+rsp]
        movups    xmm12, XMMWORD PTR [224+rsp]
        movups    xmm15, XMMWORD PTR [256+rsp]
        mov       r13, QWORD PTR [272+rsp]
        add       rsp, 296
        ret

_B5_3::

        movups    XMMWORD PTR [r13], xmm5
        movups    XMMWORD PTR [64+r13], xmm0

_B5_6::

        xor       ecx, ecx
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, ecx
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, eax

_B5_7::

        mov       ecx, ebx
        mov       edx, 1
        shl       edx, cl
        test      esi, edx
        jne       _B5_10

_B5_8::

        inc       ebx
        cmp       ebx, 4
        jl        _B5_7

_B5_9::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        movups    xmm0, XMMWORD PTR [64+r13]
        jmp       _B5_2

_B5_10::

        lea       rcx, QWORD PTR [r13+rbx*4]
        lea       rdx, QWORD PTR [64+r13+rbx*4]

        call      __svml_sasin_ha_cout_rare_internal
        jmp       _B5_8
        ALIGN     16

_B5_11::

__svml_asinf4_ha_ex ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_asinf4_ha_ex_B1_B3:
	DD	933633
	DD	2282559
	DD	878647
	DD	829487
	DD	1030182
	DD	968733
	DD	1112084
	DD	2425099

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B5_1
	DD	imagerel _B5_6
	DD	imagerel _unwind___svml_asinf4_ha_ex_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_asinf4_ha_ex_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B5_1
	DD	imagerel _B5_6
	DD	imagerel _unwind___svml_asinf4_ha_ex_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B5_6
	DD	imagerel _B5_11
	DD	imagerel _unwind___svml_asinf4_ha_ex_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST5:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_sasin_ha_cout_rare_internal

__svml_sasin_ha_cout_rare_internal	PROC

_B6_1::

        DB        243
        DB        15
        DB        30
        DB        250
L82::

        sub       rsp, 216
        mov       r8, rcx
        movups    XMMWORD PTR [128+rsp], xmm15
        mov       r9, rdx
        movups    XMMWORD PTR [176+rsp], xmm14
        xor       eax, eax
        movups    XMMWORD PTR [192+rsp], xmm12
        movzx     r10d, WORD PTR [2+r8]
        and       r10d, 32640
        movss     xmm1, DWORD PTR [r8]
        movups    XMMWORD PTR [96+rsp], xmm10
        movups    XMMWORD PTR [112+rsp], xmm9
        movups    XMMWORD PTR [144+rsp], xmm8
        movups    XMMWORD PTR [160+rsp], xmm7
        cmp       r10d, 32640
        je        _B6_12

_B6_2::

        pxor      xmm0, xmm0
        cvtss2sd  xmm0, xmm1
        movsd     QWORD PTR [56+rsp], xmm0
        and       BYTE PTR [63+rsp], 127
        movsd     xmm4, QWORD PTR [56+rsp]
        movsd     xmm0, QWORD PTR [_vmldASinHATab+4160]
        comisd    xmm0, xmm4
        jb        _B6_10

_B6_3::

        movsd     xmm2, QWORD PTR [_vmldASinHATab+4168]
        comisd    xmm2, xmm4
        jbe       _B6_9

_B6_4::

        comisd    xmm4, QWORD PTR [_vmldASinHATab+4136]
        jbe       _B6_6

_B6_5::

        movsd     xmm8, QWORD PTR [_vmldASinHATab+4104]
        movaps    xmm10, xmm4
        mulsd     xmm8, xmm4
        movaps    xmm15, xmm4
        movsd     QWORD PTR [40+rsp], xmm8
        movsd     xmm7, QWORD PTR [40+rsp]
        movsd     xmm0, QWORD PTR [_vmldASinHATab+4296]
        movsd     xmm12, QWORD PTR [_vmldASinHATab+4104]
        subsd     xmm7, QWORD PTR [56+rsp]
        movsd     QWORD PTR [32+rsp], xmm7
        movsd     xmm9, QWORD PTR [40+rsp]
        movsd     xmm3, QWORD PTR [32+rsp]
        subsd     xmm9, xmm3
        movsd     QWORD PTR [40+rsp], xmm9
        movsd     xmm2, QWORD PTR [40+rsp]
        subsd     xmm10, xmm2
        movsd     QWORD PTR [32+rsp], xmm10
        movsd     xmm1, QWORD PTR [40+rsp]
        movaps    xmm2, xmm1
        addsd     xmm15, xmm1
        mulsd     xmm2, xmm1
        movsd     xmm14, QWORD PTR [32+rsp]
        movaps    xmm5, xmm2
        mulsd     xmm15, xmm14
        mulsd     xmm12, xmm2
        addsd     xmm5, xmm15
        mulsd     xmm0, xmm5
        movsd     QWORD PTR [40+rsp], xmm12
        movsd     xmm8, QWORD PTR [40+rsp]
        addsd     xmm0, QWORD PTR [_vmldASinHATab+4288]
        subsd     xmm8, xmm2
        mulsd     xmm0, xmm5
        movsd     QWORD PTR [32+rsp], xmm8
        movsd     xmm3, QWORD PTR [40+rsp]
        movsd     xmm7, QWORD PTR [32+rsp]
        addsd     xmm0, QWORD PTR [_vmldASinHATab+4280]
        subsd     xmm3, xmm7
        mulsd     xmm0, xmm5
        movsd     QWORD PTR [40+rsp], xmm3
        movsd     xmm9, QWORD PTR [40+rsp]
        addsd     xmm0, QWORD PTR [_vmldASinHATab+4272]
        subsd     xmm2, xmm9
        mulsd     xmm0, xmm5
        movsd     QWORD PTR [32+rsp], xmm2
        movaps    xmm2, xmm4
        movsd     xmm3, QWORD PTR [40+rsp]
        movsd     xmm10, QWORD PTR [32+rsp]
        mulsd     xmm14, xmm3
        addsd     xmm0, QWORD PTR [_vmldASinHATab+4264]
        addsd     xmm15, xmm10
        mulsd     xmm1, xmm3
        mulsd     xmm0, xmm5
        mulsd     xmm2, xmm15
        addsd     xmm0, QWORD PTR [_vmldASinHATab+4256]
        addsd     xmm2, xmm14
        mulsd     xmm0, xmm5
        movsd     xmm14, QWORD PTR [_vmldASinHATab+4104]
        movaps    xmm12, xmm1
        mulsd     xmm14, xmm1
        addsd     xmm0, QWORD PTR [_vmldASinHATab+4248]
        movsd     QWORD PTR [40+rsp], xmm14
        movsd     xmm8, QWORD PTR [40+rsp]
        mulsd     xmm0, xmm5
        subsd     xmm8, xmm1
        addsd     xmm1, xmm2
        addsd     xmm0, QWORD PTR [_vmldASinHATab+4240]
        mulsd     xmm15, xmm1
        mulsd     xmm0, xmm5
        movsd     QWORD PTR [32+rsp], xmm8
        movsd     xmm9, QWORD PTR [40+rsp]
        movsd     xmm7, QWORD PTR [32+rsp]
        subsd     xmm9, xmm7
        addsd     xmm0, QWORD PTR [_vmldASinHATab+4232]
        movsd     QWORD PTR [40+rsp], xmm9
        movsd     xmm10, QWORD PTR [40+rsp]
        mulsd     xmm0, xmm5
        subsd     xmm12, xmm10
        movsd     QWORD PTR [32+rsp], xmm12
        movaps    xmm12, xmm3
        movsd     xmm9, QWORD PTR [40+rsp]
        movsd     xmm10, QWORD PTR [32+rsp]
        movaps    xmm8, xmm10
        addsd     xmm0, QWORD PTR [_vmldASinHATab+4224]
        mulsd     xmm12, xmm9
        addsd     xmm8, xmm2
        mulsd     xmm0, xmm5
        mulsd     xmm3, xmm8
        addsd     xmm0, QWORD PTR [_vmldASinHATab+4216]
        addsd     xmm3, xmm15
        mulsd     xmm0, xmm5
        movsd     xmm15, QWORD PTR [_vmldASinHATab+4104]
        mulsd     xmm15, xmm12
        addsd     xmm0, QWORD PTR [_vmldASinHATab+4208]
        movsd     QWORD PTR [40+rsp], xmm15
        movsd     xmm8, QWORD PTR [40+rsp]
        subsd     xmm8, xmm12
        movsd     QWORD PTR [32+rsp], xmm8
        movaps    xmm8, xmm12
        movsd     xmm14, QWORD PTR [40+rsp]
        addsd     xmm12, xmm3
        mulsd     xmm5, xmm12
        mulsd     xmm0, xmm5
        movsd     xmm5, QWORD PTR [_vmldASinHATab+4184]
        mulsd     xmm5, xmm1
        movsd     xmm1, QWORD PTR [_vmldASinHATab+4200]
        mulsd     xmm1, xmm12
        movsd     xmm7, QWORD PTR [32+rsp]
        addsd     xmm5, xmm1
        subsd     xmm14, xmm7
        addsd     xmm5, xmm0
        movsd     xmm0, QWORD PTR [_vmldASinHATab+4176]
        movaps    xmm1, xmm4
        mulsd     xmm9, xmm0
        mulsd     xmm2, xmm0
        mulsd     xmm10, xmm0
        addsd     xmm1, xmm9
        addsd     xmm5, xmm2
        movsd     QWORD PTR [40+rsp], xmm14
        addsd     xmm5, xmm10
        movsd     xmm15, QWORD PTR [40+rsp]
        subsd     xmm8, xmm15
        movsd     QWORD PTR [32+rsp], xmm8
        movsd     xmm8, QWORD PTR [40+rsp]
        movsd     xmm7, QWORD PTR [32+rsp]
        movsd     QWORD PTR [40+rsp], xmm1
        movsd     xmm0, QWORD PTR [40+rsp]
        subsd     xmm4, xmm0
        addsd     xmm4, xmm9
        movsd     QWORD PTR [32+rsp], xmm4
        movsd     xmm0, QWORD PTR [40+rsp]
        movsd     xmm1, QWORD PTR [32+rsp]
        movsd     xmm4, QWORD PTR [_vmldASinHATab+4192]
        addsd     xmm1, xmm5
        mulsd     xmm3, xmm4
        mulsd     xmm8, xmm4
        mulsd     xmm4, xmm7
        addsd     xmm1, xmm3
        movaps    xmm3, xmm0
        addsd     xmm1, xmm4
        addsd     xmm3, xmm8
        movsd     QWORD PTR [40+rsp], xmm3
        movsd     xmm5, QWORD PTR [40+rsp]
        subsd     xmm0, xmm5
        addsd     xmm0, xmm8
        movsd     QWORD PTR [32+rsp], xmm0
        movsd     xmm7, QWORD PTR [40+rsp]
        movsd     xmm2, QWORD PTR [32+rsp]
        mov       r8b, BYTE PTR [3+r8]
        addsd     xmm2, xmm1
        and       r8b, -128
        addsd     xmm7, xmm2
        movsd     QWORD PTR [48+rsp], xmm7
        mov       dl, BYTE PTR [55+rsp]
        and       dl, 127
        or        dl, r8b
        mov       BYTE PTR [55+rsp], dl
        movsd     xmm8, QWORD PTR [48+rsp]
        cvtsd2ss  xmm8, xmm8
        movss     DWORD PTR [r9], xmm8
        jmp       _B6_11

_B6_6::

        comisd    xmm4, QWORD PTR [_vmldASinHATab+4128]
        jb        _B6_8

_B6_7::

        addsd     xmm4, xmm0
        movsd     QWORD PTR [40+rsp], xmm4
        movsd     xmm0, QWORD PTR [40+rsp]
        mulsd     xmm0, QWORD PTR [56+rsp]
        movsd     QWORD PTR [48+rsp], xmm0
        mov       dl, BYTE PTR [55+rsp]
        mov       r8b, BYTE PTR [3+r8]
        and       dl, 127
        and       r8b, -128
        or        dl, r8b
        mov       BYTE PTR [55+rsp], dl
        movsd     xmm1, QWORD PTR [48+rsp]
        cvtsd2ss  xmm1, xmm1
        movss     DWORD PTR [r9], xmm1
        jmp       _B6_11

_B6_8::

        mulsd     xmm4, xmm4
        movsd     QWORD PTR [40+rsp], xmm4
        movsd     xmm0, QWORD PTR [40+rsp]
        mov       r8b, BYTE PTR [3+r8]
        and       r8b, -128
        addsd     xmm0, QWORD PTR [56+rsp]
        movsd     QWORD PTR [48+rsp], xmm0
        mov       dl, BYTE PTR [55+rsp]
        and       dl, 127
        or        dl, r8b
        mov       BYTE PTR [55+rsp], dl
        movsd     xmm1, QWORD PTR [48+rsp]
        cvtsd2ss  xmm1, xmm1
        movss     DWORD PTR [r9], xmm1
        jmp       _B6_11

_B6_9::

        movaps    xmm7, xmm0
        movzx     r11d, WORD PTR [_vmldASinHATab+4166]
        subsd     xmm7, xmm4
        mulsd     xmm2, xmm7
        and       r11d, -32753
        movsd     QWORD PTR [56+rsp], xmm2
        movzx     ecx, WORD PTR [62+rsp]
        and       ecx, 32752
        shr       ecx, 4
        add       ecx, -1023
        mov       r10d, ecx
        mov       edx, ecx
        neg       r10d
        add       r10d, 1023
        and       r10d, 2047
        shl       r10d, 4
        movsd     QWORD PTR [64+rsp], xmm0
        or        r11d, r10d
        mov       WORD PTR [70+rsp], r11w
        and       edx, 1
        movsd     xmm3, QWORD PTR [64+rsp]
        mov       r10d, edx
        mulsd     xmm2, xmm3
        movaps    xmm5, xmm2
        movsd     xmm1, QWORD PTR [_vmldASinHATab+4112]
        addsd     xmm5, xmm2
        jne       L90
        movaps    xmm5, xmm2
L90:
        movsd     xmm12, QWORD PTR [_vmldASinHATab+4120]
        movaps    xmm3, xmm5
        mulsd     xmm1, xmm5
        addsd     xmm2, xmm12
        movsd     QWORD PTR [72+rsp], xmm2
        movaps    xmm14, xmm1
        mov       r11d, DWORD PTR [72+rsp]
        sub       ecx, edx
        shl       r10d, 8
        and       r11d, 511
        add       r11d, r10d
        lea       r10, QWORD PTR [__ImageBase]
        movsd     xmm9, QWORD PTR [_vmldASinHATab+4496]
        subsd     xmm14, xmm5
        movsd     QWORD PTR [32+rsp], xmm14
        movsd     xmm4, QWORD PTR [32+rsp]
        movsd     xmm2, QWORD PTR [imagerel(_vmldASinHATab)+r10+r11*8]
        subsd     xmm1, xmm4
        movaps    xmm8, xmm2
        movaps    xmm10, xmm1
        mulsd     xmm8, xmm2
        subsd     xmm3, xmm1
        mulsd     xmm1, xmm2
        mulsd     xmm10, xmm8
        mulsd     xmm8, xmm3
        subsd     xmm10, xmm0
        shr       ecx, 1
        addsd     xmm8, xmm10
        mulsd     xmm9, xmm8
        add       ecx, 1023
        and       ecx, 2047
        addsd     xmm9, QWORD PTR [_vmldASinHATab+4488]
        mulsd     xmm9, xmm8
        movzx     r10d, WORD PTR [_vmldASinHATab+4166]
        shl       ecx, 4
        and       r10d, -32753
        movsd     QWORD PTR [80+rsp], xmm0
        or        r10d, ecx
        mov       WORD PTR [86+rsp], r10w
        movsd     xmm0, QWORD PTR [80+rsp]
        mulsd     xmm1, xmm0
        addsd     xmm9, QWORD PTR [_vmldASinHATab+4480]
        mulsd     xmm9, xmm8
        movsd     xmm14, QWORD PTR [_vmldASinHATab+4336]
        movsd     xmm4, QWORD PTR [_vmldASinHATab+4320]
        movaps    xmm12, xmm4
        addsd     xmm9, QWORD PTR [_vmldASinHATab+4472]
        addsd     xmm14, QWORD PTR [_vmldASinHATab+4344]
        addsd     xmm12, QWORD PTR [_vmldASinHATab+4328]
        mulsd     xmm9, xmm8
        mulsd     xmm14, xmm7
        addsd     xmm9, QWORD PTR [_vmldASinHATab+4464]
        addsd     xmm12, xmm14
        mulsd     xmm9, xmm8
        mulsd     xmm12, xmm7
        addsd     xmm9, QWORD PTR [_vmldASinHATab+4456]
        mulsd     xmm9, xmm8
        movaps    xmm14, xmm7
        movsd     xmm10, QWORD PTR [_vmldASinHATab+4304]
        addsd     xmm9, QWORD PTR [_vmldASinHATab+4448]
        addsd     xmm10, QWORD PTR [_vmldASinHATab+4312]
        mulsd     xmm8, xmm9
        addsd     xmm10, xmm12
        mulsd     xmm8, xmm2
        mulsd     xmm2, xmm3
        mulsd     xmm5, xmm8
        movsd     xmm9, QWORD PTR [_vmldASinHATab+4104]
        addsd     xmm2, xmm5
        mulsd     xmm9, xmm7
        mulsd     xmm2, xmm0
        movsd     xmm5, QWORD PTR [_vmldASinHATab+4104]
        mulsd     xmm5, xmm1
        movsd     QWORD PTR [40+rsp], xmm5
        movsd     xmm8, QWORD PTR [40+rsp]
        movsd     xmm12, QWORD PTR [_vmldASinHATab+4104]
        subsd     xmm8, xmm1
        movsd     QWORD PTR [32+rsp], xmm8
        movsd     xmm3, QWORD PTR [40+rsp]
        movsd     xmm15, QWORD PTR [32+rsp]
        subsd     xmm3, xmm15
        movsd     QWORD PTR [40+rsp], xmm3
        movsd     xmm5, QWORD PTR [40+rsp]
        subsd     xmm1, xmm5
        movsd     QWORD PTR [32+rsp], xmm1
        movsd     xmm5, QWORD PTR [40+rsp]
        movsd     xmm1, QWORD PTR [32+rsp]
        movsd     QWORD PTR [40+rsp], xmm9
        addsd     xmm1, xmm2
        movsd     xmm2, QWORD PTR [_vmldASinHATab+4440]
        mulsd     xmm2, xmm7
        movsd     xmm8, QWORD PTR [40+rsp]
        addsd     xmm2, QWORD PTR [_vmldASinHATab+4432]
        subsd     xmm8, xmm7
        mulsd     xmm2, xmm7
        movsd     QWORD PTR [32+rsp], xmm8
        movsd     xmm3, QWORD PTR [40+rsp]
        movsd     xmm0, QWORD PTR [32+rsp]
        addsd     xmm2, QWORD PTR [_vmldASinHATab+4424]
        subsd     xmm3, xmm0
        mulsd     xmm2, xmm7
        movsd     QWORD PTR [40+rsp], xmm3
        movsd     xmm15, QWORD PTR [40+rsp]
        addsd     xmm2, QWORD PTR [_vmldASinHATab+4416]
        subsd     xmm14, xmm15
        mulsd     xmm2, xmm7
        movsd     QWORD PTR [32+rsp], xmm14
        movsd     xmm8, QWORD PTR [40+rsp]
        movaps    xmm15, xmm8
        movaps    xmm0, xmm8
        mulsd     xmm15, xmm8
        addsd     xmm2, QWORD PTR [_vmldASinHATab+4408]
        addsd     xmm0, xmm7
        mulsd     xmm8, xmm4
        mulsd     xmm2, xmm7
        mulsd     xmm12, xmm15
        addsd     xmm2, QWORD PTR [_vmldASinHATab+4400]
        mulsd     xmm2, xmm7
        movsd     xmm3, QWORD PTR [32+rsp]
        movsd     QWORD PTR [40+rsp], xmm12
        movsd     xmm14, QWORD PTR [40+rsp]
        mulsd     xmm0, xmm3
        addsd     xmm2, QWORD PTR [_vmldASinHATab+4392]
        subsd     xmm14, xmm15
        mulsd     xmm4, xmm3
        mulsd     xmm2, xmm7
        movsd     QWORD PTR [32+rsp], xmm14
        movsd     xmm9, QWORD PTR [40+rsp]
        addsd     xmm2, QWORD PTR [_vmldASinHATab+4384]
        mulsd     xmm2, xmm7
        addsd     xmm2, QWORD PTR [_vmldASinHATab+4376]
        mulsd     xmm2, xmm7
        addsd     xmm2, QWORD PTR [_vmldASinHATab+4368]
        mulsd     xmm2, xmm7
        addsd     xmm2, QWORD PTR [_vmldASinHATab+4360]
        mulsd     xmm2, xmm7
        addsd     xmm2, QWORD PTR [_vmldASinHATab+4352]
        mulsd     xmm2, xmm7
        mulsd     xmm2, xmm7
        mulsd     xmm2, xmm7
        addsd     xmm10, xmm2
        mulsd     xmm1, xmm10
        movsd     xmm10, QWORD PTR [32+rsp]
        subsd     xmm9, xmm10
        movsd     QWORD PTR [40+rsp], xmm9
        movsd     xmm14, QWORD PTR [40+rsp]
        movsd     xmm9, QWORD PTR [_vmldASinHATab+4304]
        subsd     xmm15, xmm14
        movsd     QWORD PTR [32+rsp], xmm15
        movaps    xmm15, xmm8
        movsd     xmm10, QWORD PTR [40+rsp]
        mulsd     xmm10, QWORD PTR [_vmldASinHATab+4336]
        movsd     xmm14, QWORD PTR [32+rsp]
        addsd     xmm15, xmm10
        addsd     xmm14, xmm0
        movsd     QWORD PTR [40+rsp], xmm15
        movsd     xmm12, QWORD PTR [40+rsp]
        movsd     QWORD PTR [72+rsp], xmm10
        subsd     xmm8, xmm12
        mulsd     xmm14, QWORD PTR [_vmldASinHATab+4336]
        addsd     xmm8, xmm10
        movsd     QWORD PTR [32+rsp], xmm8
        movsd     xmm10, QWORD PTR [40+rsp]
        movsd     xmm8, QWORD PTR [_vmldASinHATab+4304]
        movsd     xmm12, QWORD PTR [32+rsp]
        addsd     xmm8, xmm10
        movsd     QWORD PTR [40+rsp], xmm8
        movsd     xmm8, QWORD PTR [40+rsp]
        movsd     xmm15, QWORD PTR [_vmldASinHATab+4104]
        subsd     xmm9, xmm8
        movsd     xmm0, QWORD PTR [_vmldASinHATab+4344]
        addsd     xmm9, xmm10
        mulsd     xmm0, xmm7
        movsd     QWORD PTR [32+rsp], xmm9
        movsd     xmm9, QWORD PTR [40+rsp]
        mulsd     xmm15, xmm9
        addsd     xmm0, QWORD PTR [_vmldASinHATab+4328]
        movsd     xmm10, QWORD PTR [32+rsp]
        movsd     QWORD PTR [40+rsp], xmm15
        movsd     xmm8, QWORD PTR [40+rsp]
        mulsd     xmm0, xmm7
        subsd     xmm8, xmm9
        movsd     QWORD PTR [32+rsp], xmm8
        movsd     xmm8, QWORD PTR [40+rsp]
        movsd     xmm15, QWORD PTR [32+rsp]
        subsd     xmm8, xmm15
        addsd     xmm0, QWORD PTR [_vmldASinHATab+4312]
        movsd     QWORD PTR [40+rsp], xmm8
        addsd     xmm0, xmm2
        movsd     xmm15, QWORD PTR [40+rsp]
        mulsd     xmm0, xmm5
        subsd     xmm9, xmm15
        movsd     QWORD PTR [32+rsp], xmm9
        movsd     xmm8, QWORD PTR [40+rsp]
        movsd     xmm9, QWORD PTR [32+rsp]
        movsd     xmm2, QWORD PTR [_vmldASinHATab+4152]
        addsd     xmm9, xmm12
        addsd     xmm9, xmm10
        addsd     xmm9, xmm4
        addsd     xmm9, xmm14
        mulsd     xmm9, xmm5
        mulsd     xmm5, xmm8
        addsd     xmm1, xmm9
        addsd     xmm1, xmm0
        movsd     xmm0, QWORD PTR [_vmldASinHATab+4144]
        movaps    xmm7, xmm0
        subsd     xmm2, xmm1
        subsd     xmm7, xmm5
        movsd     QWORD PTR [40+rsp], xmm7
        movsd     xmm7, QWORD PTR [40+rsp]
        subsd     xmm0, xmm7
        subsd     xmm0, xmm5
        movsd     QWORD PTR [32+rsp], xmm0
        movsd     xmm4, QWORD PTR [40+rsp]
        movsd     xmm3, QWORD PTR [32+rsp]
        mov       r8b, BYTE PTR [3+r8]
        addsd     xmm3, xmm2
        and       r8b, -128
        addsd     xmm4, xmm3
        movsd     QWORD PTR [48+rsp], xmm4
        mov       dl, BYTE PTR [55+rsp]
        and       dl, 127
        or        dl, r8b
        mov       BYTE PTR [55+rsp], dl
        movsd     xmm1, QWORD PTR [48+rsp]
        cvtsd2ss  xmm1, xmm1
        movss     DWORD PTR [r9], xmm1
        jmp       _B6_11

_B6_10::

        mov       eax, 1
        subss     xmm1, xmm1
        divss     xmm1, xmm1
        movss     DWORD PTR [r9], xmm1

_B6_11::

        movups    xmm7, XMMWORD PTR [160+rsp]
        movups    xmm8, XMMWORD PTR [144+rsp]
        movups    xmm9, XMMWORD PTR [112+rsp]
        movups    xmm10, XMMWORD PTR [96+rsp]
        movups    xmm12, XMMWORD PTR [192+rsp]
        movups    xmm14, XMMWORD PTR [176+rsp]
        movups    xmm15, XMMWORD PTR [128+rsp]
        add       rsp, 216
        ret

_B6_12::

        divss     xmm1, xmm1
        movss     DWORD PTR [r9], xmm1
        xor       eax, eax
        test      DWORD PTR [r8], 8388607
        sete      al
        jmp       _B6_11
        ALIGN     16

_B6_13::

__svml_sasin_ha_cout_rare_internal ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_sasin_ha_cout_rare_internal_B1_B12:
	DD	1072129
	DD	686172
	DD	624724
	DD	497739
	DD	436293
	DD	837678
	DD	780323
	DD	587799
	DD	1769739

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B6_1
	DD	imagerel _B6_13
	DD	imagerel _unwind___svml_sasin_ha_cout_rare_internal_B1_B12

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_RDATA	SEGMENT     READ PAGE   'DATA'
	ALIGN  32
	PUBLIC __svml_sasin_ha_data_internal
__svml_sasin_ha_data_internal	DD	2147483647
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
	DD	796917760
	DD	796917760
	DD	796917760
	DD	796917760
	DD	796917760
	DD	796917760
	DD	796917760
	DD	796917760
	DD	796917760
	DD	796917760
	DD	796917760
	DD	796917760
	DD	796917760
	DD	796917760
	DD	796917760
	DD	796917760
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
	DD	4294959104
	DD	4294959104
	DD	4294959104
	DD	4294959104
	DD	4294959104
	DD	4294959104
	DD	4294959104
	DD	4294959104
	DD	4294959104
	DD	4294959104
	DD	4294959104
	DD	4294959104
	DD	4294959104
	DD	4294959104
	DD	4294959104
	DD	4294959104
	DD	4294965248
	DD	4294965248
	DD	4294965248
	DD	4294965248
	DD	4294965248
	DD	4294965248
	DD	4294965248
	DD	4294965248
	DD	4294965248
	DD	4294965248
	DD	4294965248
	DD	4294965248
	DD	4294965248
	DD	4294965248
	DD	4294965248
	DD	4294965248
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
	DD	3183476740
	DD	3183476740
	DD	3183476740
	DD	3183476740
	DD	3183476740
	DD	3183476740
	DD	3183476740
	DD	3183476740
	DD	3183476740
	DD	3183476740
	DD	3183476740
	DD	3183476740
	DD	3183476740
	DD	3183476740
	DD	3183476740
	DD	3183476740
	DD	1048576001
	DD	1048576001
	DD	1048576001
	DD	1048576001
	DD	1048576001
	DD	1048576001
	DD	1048576001
	DD	1048576001
	DD	1048576001
	DD	1048576001
	DD	1048576001
	DD	1048576001
	DD	1048576001
	DD	1048576001
	DD	1048576001
	DD	1048576001
	DD	1026481159
	DD	1026481159
	DD	1026481159
	DD	1026481159
	DD	1026481159
	DD	1026481159
	DD	1026481159
	DD	1026481159
	DD	1026481159
	DD	1026481159
	DD	1026481159
	DD	1026481159
	DD	1026481159
	DD	1026481159
	DD	1026481159
	DD	1026481159
	DD	1019423339
	DD	1019423339
	DD	1019423339
	DD	1019423339
	DD	1019423339
	DD	1019423339
	DD	1019423339
	DD	1019423339
	DD	1019423339
	DD	1019423339
	DD	1019423339
	DD	1019423339
	DD	1019423339
	DD	1019423339
	DD	1019423339
	DD	1019423339
	DD	1027250868
	DD	1027250868
	DD	1027250868
	DD	1027250868
	DD	1027250868
	DD	1027250868
	DD	1027250868
	DD	1027250868
	DD	1027250868
	DD	1027250868
	DD	1027250868
	DD	1027250868
	DD	1027250868
	DD	1027250868
	DD	1027250868
	DD	1027250868
	DD	1033468946
	DD	1033468946
	DD	1033468946
	DD	1033468946
	DD	1033468946
	DD	1033468946
	DD	1033468946
	DD	1033468946
	DD	1033468946
	DD	1033468946
	DD	1033468946
	DD	1033468946
	DD	1033468946
	DD	1033468946
	DD	1033468946
	DD	1033468946
	DD	1042983679
	DD	1042983679
	DD	1042983679
	DD	1042983679
	DD	1042983679
	DD	1042983679
	DD	1042983679
	DD	1042983679
	DD	1042983679
	DD	1042983679
	DD	1042983679
	DD	1042983679
	DD	1042983679
	DD	1042983679
	DD	1042983679
	DD	1042983679
	DD	1070141403
	DD	1070141403
	DD	1070141403
	DD	1070141403
	DD	1070141403
	DD	1070141403
	DD	1070141403
	DD	1070141403
	DD	1070141403
	DD	1070141403
	DD	1070141403
	DD	1070141403
	DD	1070141403
	DD	1070141403
	DD	1070141403
	DD	1070141403
	DD	3007036718
	DD	3007036718
	DD	3007036718
	DD	3007036718
	DD	3007036718
	DD	3007036718
	DD	3007036718
	DD	3007036718
	DD	3007036718
	DD	3007036718
	DD	3007036718
	DD	3007036718
	DD	3007036718
	DD	3007036718
	DD	3007036718
	DD	3007036718
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
	DD	4290772992
	DD	4290772992
	DD	4290772992
	DD	4290772992
	DD	4290772992
	DD	4290772992
	DD	4290772992
	DD	4290772992
	DD	4290772992
	DD	4290772992
	DD	4290772992
	DD	4290772992
	DD	4290772992
	DD	4290772992
	DD	4290772992
	DD	4290772992
	DD	1036306094
	DD	1036306094
	DD	1036306094
	DD	1036306094
	DD	1036306094
	DD	1036306094
	DD	1036306094
	DD	1036306094
	DD	1036306094
	DD	1036306094
	DD	1036306094
	DD	1036306094
	DD	1036306094
	DD	1036306094
	DD	1036306094
	DD	1036306094
	DD	1042839218
	DD	1042839218
	DD	1042839218
	DD	1042839218
	DD	1042839218
	DD	1042839218
	DD	1042839218
	DD	1042839218
	DD	1042839218
	DD	1042839218
	DD	1042839218
	DD	1042839218
	DD	1042839218
	DD	1042839218
	DD	1042839218
	DD	1042839218
	DD	939876771
	DD	939876771
	DD	939876771
	DD	939876771
	DD	939876771
	DD	939876771
	DD	939876771
	DD	939876771
	DD	939876771
	DD	939876771
	DD	939876771
	DD	939876771
	DD	939876771
	DD	939876771
	DD	939876771
	DD	939876771
_vmldASinHATab	DD	0
	DD	1072693248
	DD	0
	DD	1072689152
	DD	0
	DD	1072685056
	DD	0
	DD	1072680960
	DD	0
	DD	1072676864
	DD	0
	DD	1072672768
	DD	0
	DD	1072668672
	DD	0
	DD	1072665600
	DD	0
	DD	1072661504
	DD	0
	DD	1072657408
	DD	0
	DD	1072653312
	DD	0
	DD	1072649216
	DD	0
	DD	1072646144
	DD	0
	DD	1072642048
	DD	0
	DD	1072637952
	DD	0
	DD	1072634880
	DD	0
	DD	1072630784
	DD	0
	DD	1072626688
	DD	0
	DD	1072623616
	DD	0
	DD	1072619520
	DD	0
	DD	1072615424
	DD	0
	DD	1072612352
	DD	0
	DD	1072608256
	DD	0
	DD	1072605184
	DD	0
	DD	1072601088
	DD	0
	DD	1072598016
	DD	0
	DD	1072593920
	DD	0
	DD	1072590848
	DD	0
	DD	1072586752
	DD	0
	DD	1072583680
	DD	0
	DD	1072580608
	DD	0
	DD	1072576512
	DD	0
	DD	1072573440
	DD	0
	DD	1072570368
	DD	0
	DD	1072566272
	DD	0
	DD	1072563200
	DD	0
	DD	1072560128
	DD	0
	DD	1072556032
	DD	0
	DD	1072552960
	DD	0
	DD	1072549888
	DD	0
	DD	1072546816
	DD	0
	DD	1072542720
	DD	0
	DD	1072539648
	DD	0
	DD	1072536576
	DD	0
	DD	1072533504
	DD	0
	DD	1072530432
	DD	0
	DD	1072527360
	DD	0
	DD	1072523264
	DD	0
	DD	1072520192
	DD	0
	DD	1072517120
	DD	0
	DD	1072514048
	DD	0
	DD	1072510976
	DD	0
	DD	1072507904
	DD	0
	DD	1072504832
	DD	0
	DD	1072501760
	DD	0
	DD	1072498688
	DD	0
	DD	1072495616
	DD	0
	DD	1072492544
	DD	0
	DD	1072489472
	DD	0
	DD	1072486400
	DD	0
	DD	1072483328
	DD	0
	DD	1072480256
	DD	0
	DD	1072478208
	DD	0
	DD	1072475136
	DD	0
	DD	1072472064
	DD	0
	DD	1072468992
	DD	0
	DD	1072465920
	DD	0
	DD	1072462848
	DD	0
	DD	1072459776
	DD	0
	DD	1072457728
	DD	0
	DD	1072454656
	DD	0
	DD	1072451584
	DD	0
	DD	1072448512
	DD	0
	DD	1072446464
	DD	0
	DD	1072443392
	DD	0
	DD	1072440320
	DD	0
	DD	1072437248
	DD	0
	DD	1072435200
	DD	0
	DD	1072432128
	DD	0
	DD	1072429056
	DD	0
	DD	1072427008
	DD	0
	DD	1072423936
	DD	0
	DD	1072420864
	DD	0
	DD	1072418816
	DD	0
	DD	1072415744
	DD	0
	DD	1072412672
	DD	0
	DD	1072410624
	DD	0
	DD	1072407552
	DD	0
	DD	1072405504
	DD	0
	DD	1072402432
	DD	0
	DD	1072400384
	DD	0
	DD	1072397312
	DD	0
	DD	1072395264
	DD	0
	DD	1072392192
	DD	0
	DD	1072390144
	DD	0
	DD	1072387072
	DD	0
	DD	1072385024
	DD	0
	DD	1072381952
	DD	0
	DD	1072379904
	DD	0
	DD	1072376832
	DD	0
	DD	1072374784
	DD	0
	DD	1072371712
	DD	0
	DD	1072369664
	DD	0
	DD	1072366592
	DD	0
	DD	1072364544
	DD	0
	DD	1072362496
	DD	0
	DD	1072359424
	DD	0
	DD	1072357376
	DD	0
	DD	1072355328
	DD	0
	DD	1072352256
	DD	0
	DD	1072350208
	DD	0
	DD	1072347136
	DD	0
	DD	1072345088
	DD	0
	DD	1072343040
	DD	0
	DD	1072340992
	DD	0
	DD	1072337920
	DD	0
	DD	1072335872
	DD	0
	DD	1072333824
	DD	0
	DD	1072330752
	DD	0
	DD	1072328704
	DD	0
	DD	1072326656
	DD	0
	DD	1072324608
	DD	0
	DD	1072321536
	DD	0
	DD	1072319488
	DD	0
	DD	1072317440
	DD	0
	DD	1072315392
	DD	0
	DD	1072313344
	DD	0
	DD	1072310272
	DD	0
	DD	1072308224
	DD	0
	DD	1072306176
	DD	0
	DD	1072304128
	DD	0
	DD	1072302080
	DD	0
	DD	1072300032
	DD	0
	DD	1072296960
	DD	0
	DD	1072294912
	DD	0
	DD	1072292864
	DD	0
	DD	1072290816
	DD	0
	DD	1072288768
	DD	0
	DD	1072286720
	DD	0
	DD	1072284672
	DD	0
	DD	1072282624
	DD	0
	DD	1072280576
	DD	0
	DD	1072278528
	DD	0
	DD	1072275456
	DD	0
	DD	1072273408
	DD	0
	DD	1072271360
	DD	0
	DD	1072269312
	DD	0
	DD	1072267264
	DD	0
	DD	1072265216
	DD	0
	DD	1072263168
	DD	0
	DD	1072261120
	DD	0
	DD	1072259072
	DD	0
	DD	1072257024
	DD	0
	DD	1072254976
	DD	0
	DD	1072252928
	DD	0
	DD	1072250880
	DD	0
	DD	1072248832
	DD	0
	DD	1072246784
	DD	0
	DD	1072244736
	DD	0
	DD	1072243712
	DD	0
	DD	1072241664
	DD	0
	DD	1072239616
	DD	0
	DD	1072237568
	DD	0
	DD	1072235520
	DD	0
	DD	1072233472
	DD	0
	DD	1072231424
	DD	0
	DD	1072229376
	DD	0
	DD	1072227328
	DD	0
	DD	1072225280
	DD	0
	DD	1072223232
	DD	0
	DD	1072222208
	DD	0
	DD	1072220160
	DD	0
	DD	1072218112
	DD	0
	DD	1072216064
	DD	0
	DD	1072214016
	DD	0
	DD	1072211968
	DD	0
	DD	1072210944
	DD	0
	DD	1072208896
	DD	0
	DD	1072206848
	DD	0
	DD	1072204800
	DD	0
	DD	1072202752
	DD	0
	DD	1072201728
	DD	0
	DD	1072199680
	DD	0
	DD	1072197632
	DD	0
	DD	1072195584
	DD	0
	DD	1072193536
	DD	0
	DD	1072192512
	DD	0
	DD	1072190464
	DD	0
	DD	1072188416
	DD	0
	DD	1072186368
	DD	0
	DD	1072185344
	DD	0
	DD	1072183296
	DD	0
	DD	1072181248
	DD	0
	DD	1072179200
	DD	0
	DD	1072178176
	DD	0
	DD	1072176128
	DD	0
	DD	1072174080
	DD	0
	DD	1072173056
	DD	0
	DD	1072171008
	DD	0
	DD	1072168960
	DD	0
	DD	1072167936
	DD	0
	DD	1072165888
	DD	0
	DD	1072163840
	DD	0
	DD	1072161792
	DD	0
	DD	1072160768
	DD	0
	DD	1072158720
	DD	0
	DD	1072157696
	DD	0
	DD	1072155648
	DD	0
	DD	1072153600
	DD	0
	DD	1072152576
	DD	0
	DD	1072150528
	DD	0
	DD	1072148480
	DD	0
	DD	1072147456
	DD	0
	DD	1072145408
	DD	0
	DD	1072143360
	DD	0
	DD	1072142336
	DD	0
	DD	1072140288
	DD	0
	DD	1072139264
	DD	0
	DD	1072137216
	DD	0
	DD	1072135168
	DD	0
	DD	1072134144
	DD	0
	DD	1072132096
	DD	0
	DD	1072131072
	DD	0
	DD	1072129024
	DD	0
	DD	1072128000
	DD	0
	DD	1072125952
	DD	0
	DD	1072124928
	DD	0
	DD	1072122880
	DD	0
	DD	1072120832
	DD	0
	DD	1072119808
	DD	0
	DD	1072117760
	DD	0
	DD	1072116736
	DD	0
	DD	1072114688
	DD	0
	DD	1072113664
	DD	0
	DD	1072111616
	DD	0
	DD	1072110592
	DD	0
	DD	1072108544
	DD	0
	DD	1072107520
	DD	0
	DD	1072105472
	DD	0
	DD	1072104448
	DD	0
	DD	1072102400
	DD	0
	DD	1072101376
	DD	0
	DD	1072099328
	DD	0
	DD	1072098304
	DD	0
	DD	1072096256
	DD	0
	DD	1072095232
	DD	0
	DD	1072094208
	DD	0
	DD	1072092160
	DD	0
	DD	1072091136
	DD	0
	DD	1072089088
	DD	0
	DD	1072088064
	DD	0
	DD	1072086016
	DD	0
	DD	1072084992
	DD	0
	DD	1072082944
	DD	0
	DD	1072081920
	DD	0
	DD	1072080896
	DD	0
	DD	1072078848
	DD	0
	DD	1072075776
	DD	0
	DD	1072073728
	DD	0
	DD	1072070656
	DD	0
	DD	1072067584
	DD	0
	DD	1072064512
	DD	0
	DD	1072061440
	DD	0
	DD	1072059392
	DD	0
	DD	1072056320
	DD	0
	DD	1072053248
	DD	0
	DD	1072051200
	DD	0
	DD	1072048128
	DD	0
	DD	1072045056
	DD	0
	DD	1072043008
	DD	0
	DD	1072039936
	DD	0
	DD	1072037888
	DD	0
	DD	1072034816
	DD	0
	DD	1072031744
	DD	0
	DD	1072029696
	DD	0
	DD	1072026624
	DD	0
	DD	1072024576
	DD	0
	DD	1072021504
	DD	0
	DD	1072019456
	DD	0
	DD	1072016384
	DD	0
	DD	1072014336
	DD	0
	DD	1072011264
	DD	0
	DD	1072009216
	DD	0
	DD	1072006144
	DD	0
	DD	1072004096
	DD	0
	DD	1072002048
	DD	0
	DD	1071998976
	DD	0
	DD	1071996928
	DD	0
	DD	1071993856
	DD	0
	DD	1071991808
	DD	0
	DD	1071989760
	DD	0
	DD	1071986688
	DD	0
	DD	1071984640
	DD	0
	DD	1071982592
	DD	0
	DD	1071979520
	DD	0
	DD	1071977472
	DD	0
	DD	1071975424
	DD	0
	DD	1071972352
	DD	0
	DD	1071970304
	DD	0
	DD	1071968256
	DD	0
	DD	1071966208
	DD	0
	DD	1071964160
	DD	0
	DD	1071961088
	DD	0
	DD	1071959040
	DD	0
	DD	1071956992
	DD	0
	DD	1071954944
	DD	0
	DD	1071952896
	DD	0
	DD	1071949824
	DD	0
	DD	1071947776
	DD	0
	DD	1071945728
	DD	0
	DD	1071943680
	DD	0
	DD	1071941632
	DD	0
	DD	1071939584
	DD	0
	DD	1071937536
	DD	0
	DD	1071935488
	DD	0
	DD	1071933440
	DD	0
	DD	1071930368
	DD	0
	DD	1071928320
	DD	0
	DD	1071926272
	DD	0
	DD	1071924224
	DD	0
	DD	1071922176
	DD	0
	DD	1071920128
	DD	0
	DD	1071918080
	DD	0
	DD	1071916032
	DD	0
	DD	1071913984
	DD	0
	DD	1071911936
	DD	0
	DD	1071909888
	DD	0
	DD	1071907840
	DD	0
	DD	1071905792
	DD	0
	DD	1071903744
	DD	0
	DD	1071901696
	DD	0
	DD	1071900672
	DD	0
	DD	1071898624
	DD	0
	DD	1071896576
	DD	0
	DD	1071894528
	DD	0
	DD	1071892480
	DD	0
	DD	1071890432
	DD	0
	DD	1071888384
	DD	0
	DD	1071886336
	DD	0
	DD	1071884288
	DD	0
	DD	1071883264
	DD	0
	DD	1071881216
	DD	0
	DD	1071879168
	DD	0
	DD	1071877120
	DD	0
	DD	1071875072
	DD	0
	DD	1071873024
	DD	0
	DD	1071872000
	DD	0
	DD	1071869952
	DD	0
	DD	1071867904
	DD	0
	DD	1071865856
	DD	0
	DD	1071864832
	DD	0
	DD	1071862784
	DD	0
	DD	1071860736
	DD	0
	DD	1071858688
	DD	0
	DD	1071856640
	DD	0
	DD	1071855616
	DD	0
	DD	1071853568
	DD	0
	DD	1071851520
	DD	0
	DD	1071850496
	DD	0
	DD	1071848448
	DD	0
	DD	1071846400
	DD	0
	DD	1071844352
	DD	0
	DD	1071843328
	DD	0
	DD	1071841280
	DD	0
	DD	1071839232
	DD	0
	DD	1071838208
	DD	0
	DD	1071836160
	DD	0
	DD	1071834112
	DD	0
	DD	1071833088
	DD	0
	DD	1071831040
	DD	0
	DD	1071830016
	DD	0
	DD	1071827968
	DD	0
	DD	1071825920
	DD	0
	DD	1071824896
	DD	0
	DD	1071822848
	DD	0
	DD	1071821824
	DD	0
	DD	1071819776
	DD	0
	DD	1071817728
	DD	0
	DD	1071816704
	DD	0
	DD	1071814656
	DD	0
	DD	1071813632
	DD	0
	DD	1071811584
	DD	0
	DD	1071810560
	DD	0
	DD	1071808512
	DD	0
	DD	1071806464
	DD	0
	DD	1071805440
	DD	0
	DD	1071803392
	DD	0
	DD	1071802368
	DD	0
	DD	1071800320
	DD	0
	DD	1071799296
	DD	0
	DD	1071797248
	DD	0
	DD	1071796224
	DD	0
	DD	1071794176
	DD	0
	DD	1071793152
	DD	0
	DD	1071791104
	DD	0
	DD	1071790080
	DD	0
	DD	1071788032
	DD	0
	DD	1071787008
	DD	0
	DD	1071784960
	DD	0
	DD	1071783936
	DD	0
	DD	1071782912
	DD	0
	DD	1071780864
	DD	0
	DD	1071779840
	DD	0
	DD	1071777792
	DD	0
	DD	1071776768
	DD	0
	DD	1071774720
	DD	0
	DD	1071773696
	DD	0
	DD	1071772672
	DD	0
	DD	1071770624
	DD	0
	DD	1071769600
	DD	0
	DD	1071767552
	DD	0
	DD	1071766528
	DD	0
	DD	1071765504
	DD	0
	DD	1071763456
	DD	0
	DD	1071762432
	DD	0
	DD	1071760384
	DD	0
	DD	1071759360
	DD	0
	DD	1071758336
	DD	0
	DD	1071756288
	DD	0
	DD	1071755264
	DD	0
	DD	1071754240
	DD	0
	DD	1071752192
	DD	0
	DD	1071751168
	DD	0
	DD	1071750144
	DD	0
	DD	1071748096
	DD	0
	DD	1071747072
	DD	0
	DD	1071746048
	DD	0
	DD	1071744000
	DD	0
	DD	1071742976
	DD	0
	DD	1071741952
	DD	0
	DD	1071739904
	DD	0
	DD	1071738880
	DD	0
	DD	1071737856
	DD	0
	DD	1071736832
	DD	0
	DD	1071734784
	DD	0
	DD	1071733760
	DD	0
	DD	1071732736
	DD	0
	DD	1071730688
	DD	0
	DD	1071729664
	DD	0
	DD	1071728640
	DD	0
	DD	1071727616
	DD	0
	DD	1071725568
	DD	0
	DD	1071724544
	DD	0
	DD	1071723520
	DD	0
	DD	1071722496
	DD	0
	DD	1071720448
	DD	0
	DD	1071719424
	DD	0
	DD	1071718400
	DD	0
	DD	1071717376
	DD	0
	DD	1071715328
	DD	0
	DD	1071714304
	DD	0
	DD	1071713280
	DD	0
	DD	1071712256
	DD	0
	DD	1071711232
	DD	0
	DD	1071709184
	DD	0
	DD	1071708160
	DD	0
	DD	1071707136
	DD	0
	DD	1071706112
	DD	0
	DD	1071705088
	DD	0
	DD	1071704064
	DD	0
	DD	1071702016
	DD	0
	DD	1071700992
	DD	0
	DD	1071699968
	DD	0
	DD	1071698944
	DD	0
	DD	1071697920
	DD	0
	DD	1071696896
	DD	0
	DD	1071694848
	DD	0
	DD	1071693824
	DD	0
	DD	1071692800
	DD	0
	DD	1071691776
	DD	0
	DD	1071690752
	DD	0
	DD	1071689728
	DD	0
	DD	1071688704
	DD	0
	DD	1071686656
	DD	0
	DD	1071685632
	DD	0
	DD	1071684608
	DD	0
	DD	1071683584
	DD	0
	DD	1071682560
	DD	0
	DD	1071681536
	DD	0
	DD	1071680512
	DD	0
	DD	1071679488
	DD	0
	DD	1071677440
	DD	0
	DD	1071676416
	DD	0
	DD	1071675392
	DD	0
	DD	1071674368
	DD	0
	DD	1071673344
	DD	0
	DD	1071672320
	DD	0
	DD	1071671296
	DD	0
	DD	1071670272
	DD	0
	DD	1071669248
	DD	0
	DD	1071668224
	DD	0
	DD	1071667200
	DD	0
	DD	1071666176
	DD	0
	DD	1071665152
	DD	0
	DD	1071663104
	DD	0
	DD	1071662080
	DD	0
	DD	1071661056
	DD	0
	DD	1071660032
	DD	0
	DD	1071659008
	DD	0
	DD	1071657984
	DD	0
	DD	1071656960
	DD	0
	DD	1071655936
	DD	0
	DD	1071654912
	DD	0
	DD	1071653888
	DD	0
	DD	1071652864
	DD	0
	DD	1071651840
	DD	0
	DD	1071650816
	DD	0
	DD	1071649792
	DD	0
	DD	1071648768
	DD	0
	DD	1071647744
	DD	0
	DD	1071646720
	DD	0
	DD	1071645696
	DD	0
	DD	1071644672
	DD	0
	DD	1101004800
	DD	1073741824
	DD	1095761920
	DD	256
	DD	1118830592
	DD	0
	DD	1048576
	DD	0
	DD	1017118720
	DD	1413754136
	DD	1073291771
	DD	856972295
	DD	1016178214
	DD	0
	DD	1072693248
	DD	0
	DD	1071644672
	DD	1476395008
	DD	1069897045
	DD	1768958041
	DD	3189069141
	DD	805306368
	DD	1068708659
	DD	3580333578
	DD	1040816593
	DD	3067382784
	DD	1067899757
	DD	3397590151
	DD	1067392113
	DD	2939529726
	DD	1066854585
	DD	1423429166
	DD	1066517752
	DD	1775218934
	DD	1066178574
	DD	1185392460
	DD	1065859647
	DD	289998670
	DD	1065577550
	DD	3179807072
	DD	1065648121
	DD	3781007284
	DD	1061576176
	DD	2482106687
	DD	1067019199
	DD	763519713
	DD	3214591591
	DD	3695107454
	DD	1067530646
	DD	0
	DD	1073741824
	DD	1124791109
	DD	1006764147
	DD	1476395008
	DD	1069897045
	DD	1953913876
	DD	3189069141
	DD	805306368
	DD	1067660083
	DD	165110192
	DD	1039768033
	DD	3067304082
	DD	1065802605
	DD	3404727379
	DD	1064246385
	DD	2737480376
	DD	1062660281
	DD	933797922
	DD	1061274873
	DD	1475716730
	DD	1059887095
	DD	1511619763
	DD	1058519827
	DD	556024211
	DD	1057187555
	DD	3482101045
	DD	1056217350
	DD	1174622859
	DD	1050762633
	DD	899668651
	DD	1055506366
	DD	1081094694
	DD	3202035365
	DD	2559814773
	DD	1053906576
	DD	0
	DD	3219128320
	DD	0
	DD	1071120384
	DD	0
	DD	3218341888
	DD	0
	DD	1070694400
	DD	0
	DD	3218046976
	DD	0
	DD	1070391296
	DD	0
	DD	3217739776
_RDATA	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS
EXTRN	__ImageBase:PROC
EXTRN	_fltused:BYTE
	ENDIF
	END
