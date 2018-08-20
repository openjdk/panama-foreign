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
	PUBLIC __svml_log1pf4_ha_e9

__svml_log1pf4_ha_e9	PROC

_B1_1::

        DB        243
        DB        15
        DB        30
        DB        250
L1::

        sub       rsp, 280
        lea       rax, QWORD PTR [__ImageBase]
        vmovups   XMMWORD PTR [192+rsp], xmm15
        vmovups   XMMWORD PTR [208+rsp], xmm14
        vmovups   XMMWORD PTR [240+rsp], xmm9
        vmovups   XMMWORD PTR [224+rsp], xmm8
        mov       QWORD PTR [256+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovups   xmm14, XMMWORD PTR [rcx]
        and       r13, -64
        vmovups   xmm0, XMMWORD PTR [__svml_slog1p_ha_data_internal+1984]
        vmaxps    xmm3, xmm0, xmm14
        vminps    xmm4, xmm0, xmm14
        vmovups   xmm2, XMMWORD PTR [__svml_slog1p_ha_data_internal+1088]
        vaddps    xmm5, xmm3, xmm4
        vandps    xmm15, xmm14, xmm2
        vandnps   xmm9, xmm2, xmm14
        vcmpltps  xmm8, xmm15, XMMWORD PTR [__svml_slog1p_ha_data_internal+1152]
        vorps     xmm1, xmm8, XMMWORD PTR [__svml_slog1p_ha_data_internal+1216]
        vandps    xmm2, xmm5, xmm1
        vmovups   xmm8, XMMWORD PTR [__svml_slog1p_ha_data_internal+1344]
        vsubps    xmm3, xmm3, xmm2
        mov       QWORD PTR [264+rsp], r13
        vaddps    xmm1, xmm4, xmm3
        vcmpltps  xmm3, xmm14, XMMWORD PTR [__svml_slog1p_ha_data_internal+1664]
        vandps    xmm4, xmm2, XMMWORD PTR [__svml_slog1p_ha_data_internal+1536]
        vorps     xmm15, xmm4, XMMWORD PTR [__svml_slog1p_ha_data_internal+1600]
        vrcpps    xmm5, xmm15
        vcmpnleps xmm4, xmm14, XMMWORD PTR [__svml_slog1p_ha_data_internal+1728]
        vandps    xmm15, xmm2, XMMWORD PTR [__svml_slog1p_ha_data_internal+1280]
        vorps     xmm3, xmm3, xmm4
        vroundps  xmm5, xmm5, 0
        vpsubd    xmm8, xmm8, xmm15
        vmulps    xmm15, xmm5, xmm8
        vpsrld    xmm8, xmm2, 23
        vcvtdq2ps xmm8, xmm8
        vmovmskps edx, xmm3
        vmulps    xmm2, xmm2, xmm15
        vmulps    xmm15, xmm1, xmm15
        vsubps    xmm2, xmm2, xmm0
        vpsrld    xmm5, xmm5, 13
        vaddps    xmm0, xmm2, xmm15
        vmovd     r8d, xmm5
        vsubps    xmm1, xmm0, xmm2
        vpextrd   r9d, xmm5, 1
        vsubps    xmm1, xmm15, xmm1
        vpextrd   r10d, xmm5, 2
        vpextrd   r11d, xmm5, 3
        movsxd    r8, r8d
        movsxd    r9, r9d
        movsxd    r10, r10d
        movsxd    r11, r11d
        vmovq     xmm3, QWORD PTR [imagerel(__svml_slog1p_ha_data_internal)-137216+rax+r8]
        vmovq     xmm4, QWORD PTR [imagerel(__svml_slog1p_ha_data_internal)-137216+rax+r9]
        vmovq     xmm15, QWORD PTR [imagerel(__svml_slog1p_ha_data_internal)-137216+rax+r10]
        vmovq     xmm5, QWORD PTR [imagerel(__svml_slog1p_ha_data_internal)-137216+rax+r11]
        vunpcklps xmm2, xmm3, xmm4
        vunpcklps xmm15, xmm15, xmm5
        vmovlhps  xmm3, xmm2, xmm15
        vshufps   xmm15, xmm2, xmm15, 238
        vmulps    xmm4, xmm8, XMMWORD PTR [__svml_slog1p_ha_data_internal+1856]
        vmulps    xmm2, xmm0, XMMWORD PTR [__svml_slog1p_ha_data_internal+1408]
        vmulps    xmm8, xmm8, XMMWORD PTR [__svml_slog1p_ha_data_internal+1920]
        vaddps    xmm5, xmm3, xmm4
        vmulps    xmm4, xmm0, xmm0
        vaddps    xmm3, xmm2, XMMWORD PTR [__svml_slog1p_ha_data_internal+1472]
        vaddps    xmm8, xmm15, xmm8
        vmulps    xmm15, xmm3, xmm4
        vaddps    xmm2, xmm1, xmm15
        vaddps    xmm15, xmm0, xmm5
        vsubps    xmm1, xmm15, xmm5
        vsubps    xmm0, xmm0, xmm1
        vaddps    xmm0, xmm8, xmm0
        vaddps    xmm1, xmm2, xmm0
        vaddps    xmm3, xmm15, xmm1
        vorps     xmm0, xmm3, xmm9
        test      edx, edx
        jne       _B1_3

_B1_2::

        vmovups   xmm8, XMMWORD PTR [224+rsp]
        vmovups   xmm9, XMMWORD PTR [240+rsp]
        vmovups   xmm14, XMMWORD PTR [208+rsp]
        vmovups   xmm15, XMMWORD PTR [192+rsp]
        mov       r13, QWORD PTR [256+rsp]
        add       rsp, 280
        ret

_B1_3::

        vmovups   XMMWORD PTR [r13], xmm14
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

        call      __svml_slog1p_ha_cout_rare_internal
        jmp       _B1_8
        ALIGN     16

_B1_11::

__svml_log1pf4_ha_e9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_log1pf4_ha_e9_B1_B3:
	DD	802305
	DD	2151486
	DD	952374
	DD	1021997
	DD	911396
	DD	849947
	DD	2294027

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B1_1
	DD	imagerel _B1_6
	DD	imagerel _unwind___svml_log1pf4_ha_e9_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_log1pf4_ha_e9_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B1_1
	DD	imagerel _B1_6
	DD	imagerel _unwind___svml_log1pf4_ha_e9_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B1_6
	DD	imagerel _B1_11
	DD	imagerel _unwind___svml_log1pf4_ha_e9_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST1:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_log1pf8_ha_l9

__svml_log1pf8_ha_l9	PROC

_B2_1::

        DB        243
        DB        15
        DB        30
        DB        250
L16::

        sub       rsp, 344
        lea       rax, QWORD PTR [__ImageBase]
        vmovups   XMMWORD PTR [208+rsp], xmm15
        vmovups   XMMWORD PTR [224+rsp], xmm14
        vmovups   XMMWORD PTR [240+rsp], xmm12
        vmovups   XMMWORD PTR [272+rsp], xmm11
        vmovups   XMMWORD PTR [288+rsp], xmm9
        vmovups   XMMWORD PTR [304+rsp], xmm8
        vmovups   XMMWORD PTR [256+rsp], xmm6
        mov       QWORD PTR [320+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovups   ymm6, YMMWORD PTR [rcx]
        and       r13, -64
        vmovups   ymm1, YMMWORD PTR [__svml_slog1p_ha_data_internal+1984]
        vmovups   ymm4, YMMWORD PTR [__svml_slog1p_ha_data_internal+1088]
        vmaxps    ymm15, ymm1, ymm6
        vminps    ymm9, ymm1, ymm6
        vandnps   ymm5, ymm4, ymm6
        vandps    ymm4, ymm6, ymm4
        vcmplt_oqps ymm3, ymm4, YMMWORD PTR [__svml_slog1p_ha_data_internal+1152]
        vaddps    ymm2, ymm15, ymm9
        vorps     ymm14, ymm3, YMMWORD PTR [__svml_slog1p_ha_data_internal+1216]
        vandps    ymm0, ymm2, ymm14
        vandps    ymm8, ymm0, YMMWORD PTR [__svml_slog1p_ha_data_internal+1536]
        vorps     ymm11, ymm8, YMMWORD PTR [__svml_slog1p_ha_data_internal+1600]
        vrcpps    ymm4, ymm11
        vmovups   ymm14, YMMWORD PTR [__svml_slog1p_ha_data_internal+1344]
        vpsrld    ymm11, ymm0, 23
        vsubps    ymm12, ymm15, ymm0
        vcmplt_oqps ymm3, ymm6, YMMWORD PTR [__svml_slog1p_ha_data_internal+1664]
        vcmpnle_uqps ymm2, ymm6, YMMWORD PTR [__svml_slog1p_ha_data_internal+1728]
        vaddps    ymm12, ymm9, ymm12
        vroundps  ymm9, ymm4, 0
        vcvtdq2ps ymm4, ymm11
        vandps    ymm15, ymm0, YMMWORD PTR [__svml_slog1p_ha_data_internal+1280]
        vpsubd    ymm8, ymm14, ymm15
        vmulps    ymm14, ymm9, ymm8
        vpsrld    ymm9, ymm9, 13
        vfmsub213ps ymm0, ymm14, ymm1
        vmulps    ymm1, ymm12, ymm14
        vorps     ymm3, ymm3, ymm2
        vmovmskps edx, ymm3
        vaddps    ymm3, ymm0, ymm1
        vsubps    ymm0, ymm3, ymm0
        vsubps    ymm2, ymm1, ymm0
        mov       QWORD PTR [328+rsp], r13
        vmovd     r8d, xmm9
        vextracti128 xmm12, ymm9, 1
        vpextrd   r9d, xmm9, 1
        vpextrd   r10d, xmm9, 2
        movsxd    r8, r8d
        movsxd    r9, r9d
        movsxd    r10, r10d
        vmovd     ecx, xmm12
        vmovq     xmm14, QWORD PTR [imagerel(__svml_slog1p_ha_data_internal)-137216+rax+r8]
        vmovq     xmm0, QWORD PTR [imagerel(__svml_slog1p_ha_data_internal)-137216+rax+r9]
        vpextrd   r11d, xmm9, 3
        vmovq     xmm15, QWORD PTR [imagerel(__svml_slog1p_ha_data_internal)-137216+rax+r10]
        vpextrd   r8d, xmm12, 1
        vpextrd   r9d, xmm12, 2
        vpextrd   r10d, xmm12, 3
        movsxd    r11, r11d
        movsxd    rcx, ecx
        movsxd    r8, r8d
        movsxd    r9, r9d
        movsxd    r10, r10d
        vmovq     xmm1, QWORD PTR [imagerel(__svml_slog1p_ha_data_internal)-137216+rax+r11]
        vmovq     xmm8, QWORD PTR [imagerel(__svml_slog1p_ha_data_internal)-137216+rax+rcx]
        vmovq     xmm9, QWORD PTR [imagerel(__svml_slog1p_ha_data_internal)-137216+rax+r8]
        vmovq     xmm11, QWORD PTR [imagerel(__svml_slog1p_ha_data_internal)-137216+rax+r9]
        vmovq     xmm12, QWORD PTR [imagerel(__svml_slog1p_ha_data_internal)-137216+rax+r10]
        vunpcklps xmm15, xmm14, xmm15
        vunpcklps xmm14, xmm0, xmm1
        vunpcklps xmm0, xmm8, xmm11
        vunpcklps xmm1, xmm9, xmm12
        vinsertf128 ymm8, ymm15, xmm0, 1
        vinsertf128 ymm11, ymm14, xmm1, 1
        vmovups   ymm1, YMMWORD PTR [__svml_slog1p_ha_data_internal+1408]
        vunpcklps ymm0, ymm8, ymm11
        vfmadd231ps ymm0, ymm4, YMMWORD PTR [__svml_slog1p_ha_data_internal+1856]
        vfmadd213ps ymm1, ymm3, YMMWORD PTR [__svml_slog1p_ha_data_internal+1472]
        vunpckhps ymm8, ymm8, ymm11
        vmulps    ymm11, ymm3, ymm3
        vfmadd132ps ymm4, ymm8, YMMWORD PTR [__svml_slog1p_ha_data_internal+1920]
        vaddps    ymm8, ymm3, ymm0
        vfmadd213ps ymm1, ymm11, ymm2
        vsubps    ymm2, ymm8, ymm0
        vsubps    ymm3, ymm3, ymm2
        vaddps    ymm4, ymm4, ymm3
        vaddps    ymm0, ymm1, ymm4
        vaddps    ymm1, ymm8, ymm0
        vorps     ymm0, ymm1, ymm5
        test      edx, edx
        jne       _B2_3

_B2_2::

        vmovups   xmm6, XMMWORD PTR [256+rsp]
        vmovups   xmm8, XMMWORD PTR [304+rsp]
        vmovups   xmm9, XMMWORD PTR [288+rsp]
        vmovups   xmm11, XMMWORD PTR [272+rsp]
        vmovups   xmm12, XMMWORD PTR [240+rsp]
        vmovups   xmm14, XMMWORD PTR [224+rsp]
        vmovups   xmm15, XMMWORD PTR [208+rsp]
        mov       r13, QWORD PTR [320+rsp]
        add       rsp, 344
        ret

_B2_3::

        vmovups   YMMWORD PTR [r13], ymm6
        vmovups   YMMWORD PTR [64+r13], ymm0

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

        call      __svml_slog1p_ha_cout_rare_internal
        jmp       _B2_8
        ALIGN     16

_B2_11::

__svml_log1pf8_ha_l9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_log1pf8_ha_l9_B1_B3:
	DD	1202433
	DD	2675801
	DD	1075281
	DD	1280072
	DD	1218623
	DD	1161270
	DD	1034285
	DD	976932
	DD	915483
	DD	2818315

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B2_1
	DD	imagerel _B2_6
	DD	imagerel _unwind___svml_log1pf8_ha_l9_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_log1pf8_ha_l9_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B2_1
	DD	imagerel _B2_6
	DD	imagerel _unwind___svml_log1pf8_ha_l9_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B2_6
	DD	imagerel _B2_11
	DD	imagerel _unwind___svml_log1pf8_ha_l9_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST2:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_log1pf4_ha_l9

__svml_log1pf4_ha_l9	PROC

_B3_1::

        DB        243
        DB        15
        DB        30
        DB        250
L37::

        sub       rsp, 280
        lea       rax, QWORD PTR [__ImageBase]
        vmovups   XMMWORD PTR [192+rsp], xmm15
        vmovups   XMMWORD PTR [208+rsp], xmm14
        vmovups   XMMWORD PTR [224+rsp], xmm10
        vmovups   XMMWORD PTR [240+rsp], xmm7
        mov       QWORD PTR [256+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovups   xmm15, XMMWORD PTR [rcx]
        and       r13, -64
        vmovups   xmm0, XMMWORD PTR [__svml_slog1p_ha_data_internal+1984]
        vmaxps    xmm2, xmm0, xmm15
        vminps    xmm3, xmm0, xmm15
        vmovups   xmm4, XMMWORD PTR [__svml_slog1p_ha_data_internal+1088]
        vaddps    xmm7, xmm2, xmm3
        vandps    xmm5, xmm15, xmm4
        vandnps   xmm14, xmm4, xmm15
        vcmpltps  xmm1, xmm5, XMMWORD PTR [__svml_slog1p_ha_data_internal+1152]
        vorps     xmm4, xmm1, XMMWORD PTR [__svml_slog1p_ha_data_internal+1216]
        vandps    xmm4, xmm7, xmm4
        vandps    xmm5, xmm4, XMMWORD PTR [__svml_slog1p_ha_data_internal+1280]
        vsubps    xmm2, xmm2, xmm4
        vmovups   xmm10, XMMWORD PTR [__svml_slog1p_ha_data_internal+1344]
        vaddps    xmm1, xmm3, xmm2
        vcmpltps  xmm2, xmm15, XMMWORD PTR [__svml_slog1p_ha_data_internal+1664]
        vandps    xmm3, xmm4, XMMWORD PTR [__svml_slog1p_ha_data_internal+1536]
        vpsubd    xmm10, xmm10, xmm5
        vorps     xmm7, xmm3, XMMWORD PTR [__svml_slog1p_ha_data_internal+1600]
        vrcpps    xmm7, xmm7
        vcmpnleps xmm3, xmm15, XMMWORD PTR [__svml_slog1p_ha_data_internal+1728]
        vroundps  xmm7, xmm7, 0
        vorps     xmm2, xmm2, xmm3
        vmulps    xmm5, xmm7, xmm10
        vpsrld    xmm10, xmm4, 23
        vcvtdq2ps xmm10, xmm10
        vmovmskps edx, xmm2
        vfmsub213ps xmm4, xmm5, xmm0
        vmulps    xmm0, xmm1, xmm5
        mov       QWORD PTR [264+rsp], r13
        vaddps    xmm5, xmm4, xmm0
        vsubps    xmm4, xmm5, xmm4
        vsubps    xmm1, xmm0, xmm4
        vpsrld    xmm0, xmm7, 13
        vmovd     r8d, xmm0
        vpextrd   r9d, xmm0, 1
        vpextrd   r10d, xmm0, 2
        vpextrd   r11d, xmm0, 3
        movsxd    r8, r8d
        movsxd    r9, r9d
        movsxd    r10, r10d
        movsxd    r11, r11d
        vmovq     xmm2, QWORD PTR [imagerel(__svml_slog1p_ha_data_internal)-137216+rax+r8]
        vmovq     xmm3, QWORD PTR [imagerel(__svml_slog1p_ha_data_internal)-137216+rax+r9]
        vmovq     xmm7, QWORD PTR [imagerel(__svml_slog1p_ha_data_internal)-137216+rax+r10]
        vmovq     xmm4, QWORD PTR [imagerel(__svml_slog1p_ha_data_internal)-137216+rax+r11]
        vunpcklps xmm2, xmm2, xmm3
        vunpcklps xmm7, xmm7, xmm4
        vmovlhps  xmm0, xmm2, xmm7
        vmovups   xmm4, XMMWORD PTR [__svml_slog1p_ha_data_internal+1408]
        vfmadd213ps xmm4, xmm5, XMMWORD PTR [__svml_slog1p_ha_data_internal+1472]
        vshufps   xmm3, xmm2, xmm7, 238
        vfmadd231ps xmm0, xmm10, XMMWORD PTR [__svml_slog1p_ha_data_internal+1856]
        vmulps    xmm7, xmm5, xmm5
        vfmadd132ps xmm10, xmm3, XMMWORD PTR [__svml_slog1p_ha_data_internal+1920]
        vfmadd213ps xmm4, xmm7, xmm1
        vaddps    xmm7, xmm5, xmm0
        vsubps    xmm1, xmm7, xmm0
        vsubps    xmm5, xmm5, xmm1
        vaddps    xmm10, xmm10, xmm5
        vaddps    xmm0, xmm4, xmm10
        vaddps    xmm1, xmm7, xmm0
        vorps     xmm0, xmm1, xmm14
        test      edx, edx
        jne       _B3_3

_B3_2::

        vmovups   xmm7, XMMWORD PTR [240+rsp]
        vmovups   xmm10, XMMWORD PTR [224+rsp]
        vmovups   xmm14, XMMWORD PTR [208+rsp]
        vmovups   xmm15, XMMWORD PTR [192+rsp]
        mov       r13, QWORD PTR [256+rsp]
        add       rsp, 280
        ret

_B3_3::

        vmovups   XMMWORD PTR [r13], xmm15
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

        call      __svml_slog1p_ha_cout_rare_internal
        jmp       _B3_8
        ALIGN     16

_B3_11::

__svml_log1pf4_ha_l9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_log1pf4_ha_l9_B1_B3:
	DD	802305
	DD	2151486
	DD	1013814
	DD	960557
	DD	911396
	DD	849947
	DD	2294027

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B3_1
	DD	imagerel _B3_6
	DD	imagerel _unwind___svml_log1pf4_ha_l9_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_log1pf4_ha_l9_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B3_1
	DD	imagerel _B3_6
	DD	imagerel _unwind___svml_log1pf4_ha_l9_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B3_6
	DD	imagerel _B3_11
	DD	imagerel _unwind___svml_log1pf4_ha_l9_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST3:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_log1pf4_ha_ex

__svml_log1pf4_ha_ex	PROC

_B4_1::

        DB        243
        DB        15
        DB        30
        DB        250
L52::

        sub       rsp, 280
        lea       rax, QWORD PTR [__ImageBase]
        movups    XMMWORD PTR [240+rsp], xmm15
        movups    XMMWORD PTR [208+rsp], xmm9
        movups    XMMWORD PTR [192+rsp], xmm8
        movups    XMMWORD PTR [224+rsp], xmm7
        mov       QWORD PTR [256+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        movups    xmm15, XMMWORD PTR [__svml_slog1p_ha_data_internal+1984]
        and       r13, -64
        movups    xmm7, XMMWORD PTR [rcx]
        movaps    xmm1, xmm15
        maxps     xmm1, xmm7
        movaps    xmm4, xmm15
        movaps    xmm0, xmm1
        minps     xmm4, xmm7
        movups    xmm2, XMMWORD PTR [__svml_slog1p_ha_data_internal+1088]
        addps     xmm0, xmm4
        movaps    xmm5, xmm2
        andps     xmm2, xmm7
        cmpltps   xmm2, XMMWORD PTR [__svml_slog1p_ha_data_internal+1152]
        orps      xmm2, XMMWORD PTR [__svml_slog1p_ha_data_internal+1216]
        andnps    xmm5, xmm7
        andps     xmm0, xmm2
        movups    xmm3, XMMWORD PTR [__svml_slog1p_ha_data_internal+1536]
        subps     xmm1, xmm0
        andps     xmm3, xmm0
        addps     xmm4, xmm1
        orps      xmm3, XMMWORD PTR [__svml_slog1p_ha_data_internal+1600]
        rcpps     xmm1, xmm3
        movups    xmm2, XMMWORD PTR [_2il0floatpacket_16]
        movaps    xmm3, xmm7
        movdqu    xmm8, XMMWORD PTR [__svml_slog1p_ha_data_internal+1280]
        addps     xmm1, xmm2
        cmpltps   xmm3, XMMWORD PTR [__svml_slog1p_ha_data_internal+1664]
        subps     xmm1, xmm2
        movdqu    xmm9, XMMWORD PTR [__svml_slog1p_ha_data_internal+1344]
        pand      xmm8, xmm0
        psubd     xmm9, xmm8
        movaps    xmm2, xmm0
        mulps     xmm9, xmm1
        movaps    xmm8, xmm7
        cmpnleps  xmm8, XMMWORD PTR [__svml_slog1p_ha_data_internal+1728]
        mulps     xmm0, xmm9
        mulps     xmm4, xmm9
        subps     xmm0, xmm15
        orps      xmm3, xmm8
        psrld     xmm1, 13
        movmskps  edx, xmm3
        movaps    xmm3, xmm0
        pshufd    xmm9, xmm1, 1
        psrld     xmm2, 23
        pshufd    xmm8, xmm1, 2
        addps     xmm3, xmm4
        cvtdq2ps  xmm2, xmm2
        movd      r8d, xmm1
        movd      r9d, xmm9
        movaps    xmm15, xmm3
        pshufd    xmm1, xmm1, 3
        subps     xmm15, xmm0
        movd      r10d, xmm8
        movd      r11d, xmm1
        movups    xmm1, XMMWORD PTR [__svml_slog1p_ha_data_internal+1408]
        subps     xmm4, xmm15
        mulps     xmm1, xmm3
        movsxd    r8, r8d
        addps     xmm1, XMMWORD PTR [__svml_slog1p_ha_data_internal+1472]
        movsxd    r9, r9d
        movsxd    r10, r10d
        movsxd    r11, r11d
        movq      xmm0, QWORD PTR [imagerel(__svml_slog1p_ha_data_internal)-137216+rax+r8]
        movq      xmm15, QWORD PTR [imagerel(__svml_slog1p_ha_data_internal)-137216+rax+r9]
        unpcklps  xmm0, xmm15
        movups    xmm15, XMMWORD PTR [__svml_slog1p_ha_data_internal+1856]
        mulps     xmm15, xmm2
        mulps     xmm2, XMMWORD PTR [__svml_slog1p_ha_data_internal+1920]
        movq      xmm9, QWORD PTR [imagerel(__svml_slog1p_ha_data_internal)-137216+rax+r10]
        movq      xmm8, QWORD PTR [imagerel(__svml_slog1p_ha_data_internal)-137216+rax+r11]
        unpcklps  xmm9, xmm8
        movaps    xmm8, xmm0
        shufps    xmm0, xmm9, 238
        movlhps   xmm8, xmm9
        addps     xmm0, xmm2
        addps     xmm8, xmm15
        movaps    xmm2, xmm3
        mulps     xmm2, xmm3
        mulps     xmm1, xmm2
        mov       QWORD PTR [264+rsp], r13
        addps     xmm4, xmm1
        movaps    xmm1, xmm3
        addps     xmm1, xmm8
        movaps    xmm9, xmm1
        subps     xmm9, xmm8
        subps     xmm3, xmm9
        addps     xmm0, xmm3
        addps     xmm4, xmm0
        addps     xmm1, xmm4
        orps      xmm1, xmm5
        test      edx, edx
        jne       _B4_3

_B4_2::

        movups    xmm7, XMMWORD PTR [224+rsp]
        movaps    xmm0, xmm1
        movups    xmm8, XMMWORD PTR [192+rsp]
        movups    xmm9, XMMWORD PTR [208+rsp]
        movups    xmm15, XMMWORD PTR [240+rsp]
        mov       r13, QWORD PTR [256+rsp]
        add       rsp, 280
        ret

_B4_3::

        movups    XMMWORD PTR [r13], xmm7
        movups    XMMWORD PTR [64+r13], xmm1

_B4_6::

        xor       ecx, ecx
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, ecx
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, edx

_B4_7::

        mov       ecx, ebx
        mov       eax, 1
        shl       eax, cl
        test      esi, eax
        jne       _B4_10

_B4_8::

        inc       ebx
        cmp       ebx, 4
        jl        _B4_7

_B4_9::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        movups    xmm1, XMMWORD PTR [64+r13]
        jmp       _B4_2

_B4_10::

        lea       rcx, QWORD PTR [r13+rbx*4]
        lea       rdx, QWORD PTR [64+r13+rbx*4]

        call      __svml_slog1p_ha_cout_rare_internal
        jmp       _B4_8
        ALIGN     16

_B4_11::

__svml_log1pf4_ha_ex ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_log1pf4_ha_ex_B1_B3:
	DD	802049
	DD	2151485
	DD	948277
	DD	821293
	DD	890916
	DD	1046555
	DD	2294027

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B4_1
	DD	imagerel _B4_6
	DD	imagerel _unwind___svml_log1pf4_ha_ex_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_log1pf4_ha_ex_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B4_1
	DD	imagerel _B4_6
	DD	imagerel _unwind___svml_log1pf4_ha_ex_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B4_6
	DD	imagerel _B4_11
	DD	imagerel _unwind___svml_log1pf4_ha_ex_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST4:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_log1pf8_ha_e9

__svml_log1pf8_ha_e9	PROC

_B5_1::

        DB        243
        DB        15
        DB        30
        DB        250
L67::

        sub       rsp, 344
        lea       rax, QWORD PTR [__ImageBase]
        vmovups   XMMWORD PTR [208+rsp], xmm14
        vmovups   XMMWORD PTR [224+rsp], xmm13
        vmovups   XMMWORD PTR [256+rsp], xmm12
        vmovups   XMMWORD PTR [304+rsp], xmm11
        vmovups   XMMWORD PTR [272+rsp], xmm10
        vmovups   XMMWORD PTR [288+rsp], xmm9
        vmovups   XMMWORD PTR [240+rsp], xmm6
        mov       QWORD PTR [320+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovups   ymm4, YMMWORD PTR [rcx]
        and       r13, -64
        vmovups   ymm10, YMMWORD PTR [__svml_slog1p_ha_data_internal+1984]
        vmovups   ymm0, YMMWORD PTR [__svml_slog1p_ha_data_internal+1088]
        vmaxps    ymm13, ymm10, ymm4
        vminps    ymm11, ymm10, ymm4
        vandps    ymm2, ymm4, ymm0
        vcmplt_oqps ymm1, ymm2, YMMWORD PTR [__svml_slog1p_ha_data_internal+1152]
        vaddps    ymm14, ymm13, ymm11
        vmovups   xmm2, XMMWORD PTR [__svml_slog1p_ha_data_internal+1280]
        vandnps   ymm3, ymm0, ymm4
        vorps     ymm0, ymm1, YMMWORD PTR [__svml_slog1p_ha_data_internal+1216]
        vandps    ymm5, ymm14, ymm0
        vandps    ymm6, ymm5, YMMWORD PTR [__svml_slog1p_ha_data_internal+1536]
        vorps     ymm14, ymm6, YMMWORD PTR [__svml_slog1p_ha_data_internal+1600]
        vrcpps    ymm0, ymm14
        vmovups   xmm1, XMMWORD PTR [__svml_slog1p_ha_data_internal+1344]
        vsubps    ymm12, ymm13, ymm5
        vroundps  ymm13, ymm0, 0
        vaddps    ymm9, ymm11, ymm12
        vcmplt_oqps ymm11, ymm4, YMMWORD PTR [__svml_slog1p_ha_data_internal+1664]
        vcmpnle_uqps ymm12, ymm4, YMMWORD PTR [__svml_slog1p_ha_data_internal+1728]
        vorps     ymm11, ymm11, ymm12
        mov       QWORD PTR [328+rsp], r13
        vpand     xmm14, xmm5, xmm2
        vextractf128 xmm6, ymm5, 1
        vpsubd    xmm0, xmm1, xmm14
        vpsrld    xmm14, xmm5, 23
        vandps    xmm2, xmm6, xmm2
        vpsrld    xmm6, xmm6, 23
        vpsubd    xmm1, xmm1, xmm2
        vinsertf128 ymm2, ymm0, xmm1, 1
        vmulps    ymm1, ymm13, ymm2
        vpxor     xmm0, xmm0, xmm0
        vmulps    ymm5, ymm5, ymm1
        vmulps    ymm9, ymm9, ymm1
        vsubps    ymm10, ymm5, ymm10
        vaddps    ymm1, ymm10, ymm9
        vextractf128 xmm12, ymm11, 1
        vinsertf128 ymm2, ymm14, xmm6, 1
        vpackssdw xmm14, xmm11, xmm12
        vpacksswb xmm11, xmm14, xmm0
        vpmovmskb edx, xmm11
        vsubps    ymm14, ymm1, ymm10
        vcvtdq2ps ymm2, ymm2
        vsubps    ymm0, ymm9, ymm14
        vpsrld    xmm12, xmm13, 13
        vmovd     r8d, xmm12
        vextractf128 xmm11, ymm13, 1
        vpextrd   r9d, xmm12, 1
        vpsrld    xmm11, xmm11, 13
        vpextrd   r10d, xmm12, 2
        movsxd    r8, r8d
        movsxd    r9, r9d
        movsxd    r10, r10d
        vmovd     ecx, xmm11
        vmovq     xmm14, QWORD PTR [imagerel(__svml_slog1p_ha_data_internal)-137216+rax+r8]
        vmovq     xmm10, QWORD PTR [imagerel(__svml_slog1p_ha_data_internal)-137216+rax+r9]
        vpextrd   r11d, xmm12, 3
        vmovq     xmm13, QWORD PTR [imagerel(__svml_slog1p_ha_data_internal)-137216+rax+r10]
        vpextrd   r8d, xmm11, 1
        vpextrd   r9d, xmm11, 2
        vpextrd   r10d, xmm11, 3
        movsxd    r11, r11d
        movsxd    rcx, ecx
        movsxd    r8, r8d
        movsxd    r9, r9d
        movsxd    r10, r10d
        vmovq     xmm9, QWORD PTR [imagerel(__svml_slog1p_ha_data_internal)-137216+rax+r11]
        vmovq     xmm6, QWORD PTR [imagerel(__svml_slog1p_ha_data_internal)-137216+rax+rcx]
        vmovq     xmm12, QWORD PTR [imagerel(__svml_slog1p_ha_data_internal)-137216+rax+r8]
        vmovq     xmm5, QWORD PTR [imagerel(__svml_slog1p_ha_data_internal)-137216+rax+r9]
        vmovq     xmm11, QWORD PTR [imagerel(__svml_slog1p_ha_data_internal)-137216+rax+r10]
        vunpcklps xmm13, xmm14, xmm13
        vunpcklps xmm14, xmm10, xmm9
        vunpcklps xmm5, xmm6, xmm5
        vunpcklps xmm6, xmm12, xmm11
        vmulps    ymm11, ymm1, ymm1
        vinsertf128 ymm9, ymm13, xmm5, 1
        vinsertf128 ymm10, ymm14, xmm6, 1
        vunpcklps ymm14, ymm9, ymm10
        vunpckhps ymm6, ymm9, ymm10
        vmulps    ymm9, ymm1, YMMWORD PTR [__svml_slog1p_ha_data_internal+1408]
        vmulps    ymm5, ymm2, YMMWORD PTR [__svml_slog1p_ha_data_internal+1856]
        vmulps    ymm2, ymm2, YMMWORD PTR [__svml_slog1p_ha_data_internal+1920]
        vaddps    ymm10, ymm9, YMMWORD PTR [__svml_slog1p_ha_data_internal+1472]
        vaddps    ymm14, ymm14, ymm5
        vaddps    ymm2, ymm6, ymm2
        vmulps    ymm12, ymm10, ymm11
        vaddps    ymm6, ymm1, ymm14
        vaddps    ymm5, ymm0, ymm12
        vsubps    ymm0, ymm6, ymm14
        vsubps    ymm1, ymm1, ymm0
        vaddps    ymm0, ymm2, ymm1
        vaddps    ymm1, ymm5, ymm0
        vaddps    ymm2, ymm6, ymm1
        vorps     ymm0, ymm2, ymm3
        test      dl, dl
        jne       _B5_3

_B5_2::

        vmovups   xmm6, XMMWORD PTR [240+rsp]
        vmovups   xmm9, XMMWORD PTR [288+rsp]
        vmovups   xmm10, XMMWORD PTR [272+rsp]
        vmovups   xmm11, XMMWORD PTR [304+rsp]
        vmovups   xmm12, XMMWORD PTR [256+rsp]
        vmovups   xmm13, XMMWORD PTR [224+rsp]
        vmovups   xmm14, XMMWORD PTR [208+rsp]
        mov       r13, QWORD PTR [320+rsp]
        add       rsp, 344
        ret

_B5_3::

        vmovups   YMMWORD PTR [r13], ymm4
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

        call      __svml_slog1p_ha_cout_rare_internal
        jmp       _B5_8
        ALIGN     16

_B5_11::

__svml_log1pf8_ha_e9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_log1pf8_ha_e9_B1_B3:
	DD	1202433
	DD	2675801
	DD	1009745
	DD	1218632
	DD	1157183
	DD	1292342
	DD	1099821
	DD	972836
	DD	911387
	DD	2818315

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B5_1
	DD	imagerel _B5_6
	DD	imagerel _unwind___svml_log1pf8_ha_e9_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_log1pf8_ha_e9_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B5_1
	DD	imagerel _B5_6
	DD	imagerel _unwind___svml_log1pf8_ha_e9_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B5_6
	DD	imagerel _B5_11
	DD	imagerel _unwind___svml_log1pf8_ha_e9_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST5:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_slog1p_ha_cout_rare_internal

__svml_slog1p_ha_cout_rare_internal	PROC

_B6_1::

        DB        243
        DB        15
        DB        30
        DB        250
L88::

        sub       rsp, 72
        mov       r8, rdx
        movss     xmm0, DWORD PTR [rcx]
        xor       eax, eax
        movups    XMMWORD PTR [48+rsp], xmm14
        xor       dl, dl
        addss     xmm0, DWORD PTR [_2il0floatpacket_100]
        movss     DWORD PTR [36+rsp], xmm0
        movzx     r9d, WORD PTR [38+rsp]
        and       r9d, 32640
        cmp       r9d, 32640
        je        _B6_15

_B6_2::

        movss     DWORD PTR [40+rsp], xmm0
        xor       r9d, r9d
        movzx     ecx, WORD PTR [42+rsp]
        test      ecx, 32640
        jne       _B6_4

_B6_3::

        mulss     xmm0, DWORD PTR [_2il0floatpacket_85]
        mov       dl, 1
        movss     DWORD PTR [40+rsp], xmm0
        mov       r9d, -40

_B6_4::

        pxor      xmm1, xmm1
        comiss    xmm0, xmm1
        jbe       _B6_10

_B6_5::

        movaps    xmm3, xmm0
        subss     xmm3, DWORD PTR [_2il0floatpacket_100]
        movss     DWORD PTR [36+rsp], xmm3
        and       BYTE PTR [39+rsp], 127
        movss     xmm2, DWORD PTR [36+rsp]
        comiss    xmm2, DWORD PTR [_2il0floatpacket_86]
        jbe       _B6_9

_B6_6::

        movzx     ecx, WORD PTR [42+rsp]
        pxor      xmm4, xmm4
        and       ecx, 32640
        shr       ecx, 7
        lea       r9d, DWORD PTR [-127+r9+rcx]
        cvtsi2ss  xmm4, r9d
        cmp       dl, 1
        je        _B6_13

_B6_7::

        movss     xmm3, DWORD PTR [_2il0floatpacket_96]
        movss     xmm1, DWORD PTR [_2il0floatpacket_97]
        mulss     xmm3, xmm4
        mulss     xmm4, xmm1
        movaps    xmm1, xmm4

_B6_8::

        movss     DWORD PTR [36+rsp], xmm0
        lea       r10, QWORD PTR [__ImageBase]
        movzx     edx, WORD PTR [38+rsp]
        movaps    xmm2, xmm3
        and       edx, -32641
        mov       r11, r10
        add       edx, 16256
        mov       WORD PTR [38+rsp], dx
        movss     xmm0, DWORD PTR [36+rsp]
        movaps    xmm4, xmm0
        addss     xmm4, DWORD PTR [_2il0floatpacket_98]
        movss     DWORD PTR [32+rsp], xmm4
        mov       ecx, DWORD PTR [32+rsp]
        and       ecx, 127
        lea       r9d, DWORD PTR [rcx+rcx*2]
        movss     xmm5, DWORD PTR [imagerel(_vmldLnHATab)+4+r11+r9*4]
        movss     xmm4, DWORD PTR [imagerel(_vmldLnHATab)+r10+r9*4]
        addss     xmm2, xmm5
        addss     xmm1, DWORD PTR [imagerel(_vmldLnHATab)+8+r10+r9*4]
        movaps    xmm14, xmm2
        subss     xmm14, xmm3
        movss     DWORD PTR [32+rsp], xmm14
        movss     xmm3, DWORD PTR [32+rsp]
        subss     xmm5, xmm3
        movss     DWORD PTR [32+rsp], xmm5
        movss     xmm3, DWORD PTR [32+rsp]
        movss     xmm5, DWORD PTR [_2il0floatpacket_99]
        addss     xmm3, xmm1
        movaps    xmm1, xmm5
        addss     xmm1, xmm0
        movss     DWORD PTR [32+rsp], xmm1
        movaps    xmm1, xmm3
        movss     xmm14, DWORD PTR [32+rsp]
        subss     xmm14, xmm5
        movss     xmm5, DWORD PTR [_2il0floatpacket_87]
        subss     xmm0, xmm14
        mulss     xmm14, xmm4
        subss     xmm14, DWORD PTR [_2il0floatpacket_100]
        mulss     xmm4, xmm0
        movaps    xmm0, xmm14
        addss     xmm1, xmm4
        addss     xmm0, xmm4
        addss     xmm14, xmm2
        mulss     xmm5, xmm0
        movaps    xmm2, xmm1
        addss     xmm5, DWORD PTR [_2il0floatpacket_88]
        subss     xmm2, xmm3
        mulss     xmm5, xmm0
        movss     DWORD PTR [32+rsp], xmm2
        addss     xmm5, DWORD PTR [_2il0floatpacket_89]
        mulss     xmm5, xmm0
        addss     xmm5, DWORD PTR [_2il0floatpacket_90]
        mulss     xmm5, xmm0
        addss     xmm5, DWORD PTR [_2il0floatpacket_91]
        mulss     xmm5, xmm0
        addss     xmm5, DWORD PTR [_2il0floatpacket_92]
        mulss     xmm5, xmm0
        addss     xmm5, DWORD PTR [_2il0floatpacket_93]
        mulss     xmm5, xmm0
        mulss     xmm5, xmm0
        movss     xmm0, DWORD PTR [32+rsp]
        subss     xmm4, xmm0
        movss     DWORD PTR [32+rsp], xmm4
        movss     xmm3, DWORD PTR [32+rsp]
        addss     xmm3, xmm5
        movaps    xmm5, xmm14
        addss     xmm5, xmm1
        movss     DWORD PTR [r8], xmm5
        subss     xmm5, xmm14
        movss     DWORD PTR [32+rsp], xmm5
        movss     xmm14, DWORD PTR [32+rsp]
        subss     xmm1, xmm14
        movss     DWORD PTR [32+rsp], xmm1
        movss     xmm1, DWORD PTR [32+rsp]
        addss     xmm3, xmm1
        movss     DWORD PTR [32+rsp], xmm3
        movss     xmm4, DWORD PTR [32+rsp]
        addss     xmm4, DWORD PTR [r8]
        movss     DWORD PTR [r8], xmm4
        jmp       _B6_12

_B6_9::

        movss     xmm0, DWORD PTR [_2il0floatpacket_87]
        mulss     xmm0, xmm3
        addss     xmm0, DWORD PTR [_2il0floatpacket_88]
        mulss     xmm0, xmm3
        addss     xmm0, DWORD PTR [_2il0floatpacket_89]
        mulss     xmm0, xmm3
        addss     xmm0, DWORD PTR [_2il0floatpacket_90]
        mulss     xmm0, xmm3
        addss     xmm0, DWORD PTR [_2il0floatpacket_91]
        mulss     xmm0, xmm3
        addss     xmm0, DWORD PTR [_2il0floatpacket_92]
        mulss     xmm0, xmm3
        addss     xmm0, DWORD PTR [_2il0floatpacket_93]
        mulss     xmm0, xmm3
        mulss     xmm0, xmm3
        addss     xmm3, xmm0
        movss     DWORD PTR [r8], xmm3
        jmp       _B6_12

_B6_10::

        ucomiss   xmm0, xmm1
        jp        _B6_11
        je        _B6_14

_B6_11::

        mov       eax, 1
        pxor      xmm1, xmm1
        pxor      xmm0, xmm0
        divss     xmm1, xmm0
        movss     DWORD PTR [r8], xmm1

_B6_12::

        movups    xmm14, XMMWORD PTR [48+rsp]
        add       rsp, 72
        ret

_B6_13::

        movss     xmm2, DWORD PTR [_2il0floatpacket_95]
        mulss     xmm4, xmm2
        movaps    xmm3, xmm4
        jmp       _B6_8

_B6_14::

        movss     xmm1, DWORD PTR [_2il0floatpacket_94]
        mov       eax, 2
        pxor      xmm0, xmm0
        divss     xmm1, xmm0
        movss     DWORD PTR [r8], xmm1
        jmp       _B6_12

_B6_15::

        mov       dl, BYTE PTR [39+rsp]
        and       dl, -128
        cmp       dl, -128
        je        _B6_17

_B6_16::

        mulss     xmm0, xmm0
        movss     DWORD PTR [r8], xmm0
        jmp       _B6_12

_B6_17::

        test      DWORD PTR [36+rsp], 8388607
        je        _B6_11
        jmp       _B6_16
        ALIGN     16

_B6_19::

__svml_slog1p_ha_cout_rare_internal ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_slog1p_ha_cout_rare_internal_B1_B17:
	DD	202497
	DD	256023
	DD	33288

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B6_1
	DD	imagerel _B6_19
	DD	imagerel _unwind___svml_slog1p_ha_cout_rare_internal_B1_B17

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_RDATA	SEGMENT     READ PAGE   'DATA'
	ALIGN  32
	PUBLIC __svml_slog1p_ha_data_internal_avx512
__svml_slog1p_ha_data_internal_avx512	DD	1060205056
	DD	1059688960
	DD	1059187712
	DD	1058701824
	DD	1058229248
	DD	1057769472
	DD	1057321984
	DD	1056807936
	DD	1055958016
	DD	1055129600
	DD	1054320640
	DD	1053531136
	DD	1052760064
	DD	1052006400
	DD	1051268096
	DD	1050547200
	DD	1049840640
	DD	1049148416
	DD	1048365056
	DD	1047035904
	DD	1045733376
	DD	1044455424
	DD	1043200000
	DD	1041969152
	DD	1040760832
	DD	1038958592
	DD	1036623872
	DD	1034330112
	DD	1032073216
	DD	1027907584
	DD	1023541248
	DD	1015087104
	DD	901758606
	DD	3071200204
	DD	931108809
	DD	3074069268
	DD	3077535321
	DD	3071146094
	DD	3063010043
	DD	3072147991
	DD	908173938
	DD	3049723733
	DD	925190435
	DD	923601997
	DD	3048768765
	DD	3076457870
	DD	926424291
	DD	3073778483
	DD	3069146713
	DD	912794238
	DD	912483742
	DD	920635797
	DD	3054902185
	DD	3069864633
	DD	922801832
	DD	3033791132
	DD	3076717488
	DD	3076037756
	DD	3072434855
	DD	3077481184
	DD	3066991812
	DD	917116064
	DD	925811956
	DD	900509991
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
	DD	3196061712
	DD	3196061712
	DD	3196061712
	DD	3196061712
	DD	3196061712
	DD	3196061712
	DD	3196061712
	DD	3196061712
	DD	3196061712
	DD	3196061712
	DD	3196061712
	DD	3196061712
	DD	3196061712
	DD	3196061712
	DD	3196061712
	DD	3196061712
	DD	1051373854
	DD	1051373854
	DD	1051373854
	DD	1051373854
	DD	1051373854
	DD	1051373854
	DD	1051373854
	DD	1051373854
	DD	1051373854
	DD	1051373854
	DD	1051373854
	DD	1051373854
	DD	1051373854
	DD	1051373854
	DD	1051373854
	DD	1051373854
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
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	901758606
	DD	901758606
	DD	901758606
	DD	901758606
	DD	901758606
	DD	901758606
	DD	901758606
	DD	901758606
	DD	901758606
	DD	901758606
	DD	901758606
	DD	901758606
	DD	901758606
	DD	901758606
	DD	901758606
	DD	901758606
	PUBLIC __svml_slog1p_ha_data_internal
__svml_slog1p_ha_data_internal	DD	3266227256
	DD	3107766024
	DD	3266228276
	DD	3107776882
	DD	3266229284
	DD	3109949545
	DD	3266230292
	DD	3108055846
	DD	3266231292
	DD	3106351937
	DD	3266232276
	DD	3109092567
	DD	3266233260
	DD	3107948216
	DD	3266234236
	DD	3107170960
	DD	3266235204
	DD	3106817287
	DD	3266236164
	DD	3106942449
	DD	3266237116
	DD	3107600489
	DD	3266238060
	DD	3108844279
	DD	3266239004
	DD	3106531253
	DD	3266239932
	DD	3109100650
	DD	3266240860
	DD	3108213420
	DD	3266241780
	DD	3108112381
	DD	3266242692
	DD	3108845034
	DD	3266243604
	DD	3106263589
	DD	3266244500
	DD	3108802209
	DD	3266245396
	DD	3108116909
	DD	3266246284
	DD	3108445707
	DD	3266247164
	DD	3109831435
	DD	3266248044
	DD	3108121760
	DD	3266248916
	DD	3107552123
	DD	3266249780
	DD	3108162844
	DD	3266250644
	DD	3105799146
	DD	3266251492
	DD	3108888393
	DD	3266252340
	DD	3109079979
	DD	3266253188
	DD	3106411173
	DD	3266254020
	DD	3109307139
	DD	3266254852
	DD	3109415127
	DD	3266255684
	DD	3106770317
	DD	3266256500
	DD	3109795834
	DD	3266257324
	DD	3105942641
	DD	3266258132
	DD	3107826892
	DD	3266258940
	DD	3107092610
	DD	3266259740
	DD	3107966131
	DD	3266260540
	DD	3106284596
	DD	3266261332
	DD	3106273188
	DD	3266262116
	DD	3107962226
	DD	3266262900
	DD	3107187186
	DD	3266263676
	DD	3108171617
	DD	3266264452
	DD	3106749947
	DD	3266265220
	DD	3107144703
	DD	3266265980
	DD	3109383615
	DD	3266266740
	DD	3109299629
	DD	3266267500
	DD	3106919521
	DD	3266268252
	DD	3106463913
	DD	3266268996
	DD	3107958670
	DD	3266269740
	DD	3107234917
	DD	3266270476
	DD	3108511954
	DD	3266271212
	DD	3107620056
	DD	3266271940
	DD	3108777693
	DD	3266272668
	DD	3107814325
	DD	3266273388
	DD	3108947630
	DD	3266274108
	DD	3108006290
	DD	3266274820
	DD	3109207222
	DD	3266275532
	DD	3108378366
	DD	3266276236
	DD	3109735912
	DD	3266276940
	DD	3109107087
	DD	3266277644
	DD	3106513079
	DD	3266278340
	DD	3106169044
	DD	3266279028
	DD	3108095503
	DD	3266279716
	DD	3108118349
	DD	3266280404
	DD	3106257463
	DD	3266281084
	DD	3106726720
	DD	3266281756
	DD	3109545389
	DD	3266282436
	DD	3106343833
	DD	3266283100
	DD	3109723642
	DD	3266283772
	DD	3107120300
	DD	3266284436
	DD	3106940529
	DD	3266285092
	DD	3109202170
	DD	3266285748
	DD	3109728494
	DD	3266286404
	DD	3108536808
	DD	3266287052
	DD	3109838471
	DD	3266287700
	DD	3109455977
	DD	3266288348
	DD	3107405879
	DD	3266288988
	DD	3107898790
	DD	3266289628
	DD	3106756477
	DD	3266290260
	DD	3108189081
	DD	3266290892
	DD	3108017907
	DD	3266291524
	DD	3106258339
	DD	3266292148
	DD	3107119845
	DD	3266292772
	DD	3106423069
	DD	3266293388
	DD	3108377050
	DD	3266294004
	DD	3108802011
	DD	3266294620
	DD	3107712277
	DD	3266295228
	DD	3109316274
	DD	3266295836
	DD	3109433625
	DD	3266296444
	DD	3108078064
	DD	3266297044
	DD	3109457438
	DD	3266297644
	DD	3109390801
	DD	3266298244
	DD	3107891329
	DD	3266298836
	DD	3109166323
	DD	3266299428
	DD	3109034299
	DD	3266300020
	DD	3107507904
	DD	3266300604
	DD	3108793919
	DD	3266301188
	DD	3108710352
	DD	3266301772
	DD	3107269350
	DD	3266302348
	DD	3108677203
	DD	3266302924
	DD	3108751436
	DD	3266303500
	DD	3107503720
	DD	3266304068
	DD	3109139881
	DD	3266304636
	DD	3109476985
	DD	3266305204
	DD	3108526254
	DD	3266305772
	DD	3106298768
	DD	3266306332
	DD	3106999765
	DD	3266306892
	DD	3106445739
	DD	3266307444
	DD	3108841650
	DD	3266308004
	DD	3105809415
	DD	3266308548
	DD	3109942336
	DD	3266309100
	DD	3108667760
	DD	3266309652
	DD	3106190122
	DD	3266310196
	DD	3106713732
	DD	3266310740
	DD	3106054165
	DD	3266311276
	DD	3108415484
	DD	3266311812
	DD	3109613023
	DD	3266312348
	DD	3109656301
	DD	3266312884
	DD	3108554723
	DD	3266313420
	DD	3106317576
	DD	3266313948
	DD	3107148341
	DD	3266314476
	DD	3106861780
	DD	3266314996
	DD	3109661153
	DD	3266315524
	DD	3107166702
	DD	3266316044
	DD	3107775778
	DD	3266316564
	DD	3107302717
	DD	3266317076
	DD	3109950361
	DD	3266317596
	DD	3107338539
	DD	3266318108
	DD	3107864196
	DB 0
	ORG $+54
	DB	0
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
	DD	964689920
	DD	964689920
	DD	964689920
	DD	964689920
	DD	964689920
	DD	964689920
	DD	964689920
	DD	964689920
	DD	964689920
	DD	964689920
	DD	964689920
	DD	964689920
	DD	964689920
	DD	964689920
	DD	964689920
	DD	964689920
	DD	4294967040
	DD	4294967040
	DD	4294967040
	DD	4294967040
	DD	4294967040
	DD	4294967040
	DD	4294967040
	DD	4294967040
	DD	4294967040
	DD	4294967040
	DD	4294967040
	DD	4294967040
	DD	4294967040
	DD	4294967040
	DD	4294967040
	DD	4294967040
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
	DD	2063597568
	DD	2063597568
	DD	2063597568
	DD	2063597568
	DD	2063597568
	DD	2063597568
	DD	2063597568
	DD	2063597568
	DD	2063597568
	DD	2063597568
	DD	2063597568
	DD	2063597568
	DD	2063597568
	DD	2063597568
	DD	2063597568
	DD	2063597568
	DD	1051372345
	DD	1051372345
	DD	1051372345
	DD	1051372345
	DD	1051372345
	DD	1051372345
	DD	1051372345
	DD	1051372345
	DD	1051372345
	DD	1051372345
	DD	1051372345
	DD	1051372345
	DD	1051372345
	DD	1051372345
	DD	1051372345
	DD	1051372345
	DD	3204448310
	DD	3204448310
	DD	3204448310
	DD	3204448310
	DD	3204448310
	DD	3204448310
	DD	3204448310
	DD	3204448310
	DD	3204448310
	DD	3204448310
	DD	3204448310
	DD	3204448310
	DD	3204448310
	DD	3204448310
	DD	3204448310
	DD	3204448310
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
	DD	998244352
	DD	998244352
	DD	998244352
	DD	998244352
	DD	998244352
	DD	998244352
	DD	998244352
	DD	998244352
	DD	998244352
	DD	998244352
	DD	998244352
	DD	998244352
	DD	998244352
	DD	998244352
	DD	998244352
	DD	998244352
	DD	3212836863
	DD	3212836863
	DD	3212836863
	DD	3212836863
	DD	3212836863
	DD	3212836863
	DD	3212836863
	DD	3212836863
	DD	3212836863
	DD	3212836863
	DD	3212836863
	DD	3212836863
	DD	3212836863
	DD	3212836863
	DD	3212836863
	DD	3212836863
	DD	2055208960
	DD	2055208960
	DD	2055208960
	DD	2055208960
	DD	2055208960
	DD	2055208960
	DD	2055208960
	DD	2055208960
	DD	2055208960
	DD	2055208960
	DD	2055208960
	DD	2055208960
	DD	2055208960
	DD	2055208960
	DD	2055208960
	DD	2055208960
	DD	4294967040
	DD	4294967040
	DD	4294967040
	DD	4294967040
	DD	4294967040
	DD	4294967040
	DD	4294967040
	DD	4294967040
	DD	4294967040
	DD	4294967040
	DD	4294967040
	DD	4294967040
	DD	4294967040
	DD	4294967040
	DD	4294967040
	DD	4294967040
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	901758464
	DD	901758464
	DD	901758464
	DD	901758464
	DD	901758464
	DD	901758464
	DD	901758464
	DD	901758464
	DD	901758464
	DD	901758464
	DD	901758464
	DD	901758464
	DD	901758464
	DD	901758464
	DD	901758464
	DD	901758464
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
	DD	3196059527
	DD	3196059527
	DD	3196059527
	DD	3196059527
	DD	3196059527
	DD	3196059527
	DD	3196059527
	DD	3196059527
	DD	3196059527
	DD	3196059527
	DD	3196059527
	DD	3196059527
	DD	3196059527
	DD	3196059527
	DD	3196059527
	DD	3196059527
	DD	1045220287
	DD	1045220287
	DD	1045220287
	DD	1045220287
	DD	1045220287
	DD	1045220287
	DD	1045220287
	DD	1045220287
	DD	1045220287
	DD	1045220287
	DD	1045220287
	DD	1045220287
	DD	1045220287
	DD	1045220287
	DD	1045220287
	DD	1045220287
	DD	3190475908
	DD	3190475908
	DD	3190475908
	DD	3190475908
	DD	3190475908
	DD	3190475908
	DD	3190475908
	DD	3190475908
	DD	3190475908
	DD	3190475908
	DD	3190475908
	DD	3190475908
	DD	3190475908
	DD	3190475908
	DD	3190475908
	DD	3190475908
	DD	1041398342
	DD	1041398342
	DD	1041398342
	DD	1041398342
	DD	1041398342
	DD	1041398342
	DD	1041398342
	DD	1041398342
	DD	1041398342
	DD	1041398342
	DD	1041398342
	DD	1041398342
	DD	1041398342
	DD	1041398342
	DD	1041398342
	DD	1041398342
	DD	3187262718
	DD	3187262718
	DD	3187262718
	DD	3187262718
	DD	3187262718
	DD	3187262718
	DD	3187262718
	DD	3187262718
	DD	3187262718
	DD	3187262718
	DD	3187262718
	DD	3187262718
	DD	3187262718
	DD	3187262718
	DD	3187262718
	DD	3187262718
	DD	1037844266
	DD	1037844266
	DD	1037844266
	DD	1037844266
	DD	1037844266
	DD	1037844266
	DD	1037844266
	DD	1037844266
	DD	1037844266
	DD	1037844266
	DD	1037844266
	DD	1037844266
	DD	1037844266
	DD	1037844266
	DD	1037844266
	DD	1037844266
	DD	3187902610
	DD	3187902610
	DD	3187902610
	DD	3187902610
	DD	3187902610
	DD	3187902610
	DD	3187902610
	DD	3187902610
	DD	3187902610
	DD	3187902610
	DD	3187902610
	DD	3187902610
	DD	3187902610
	DD	3187902610
	DD	3187902610
	DD	3187902610
	DD	1039525906
	DD	1039525906
	DD	1039525906
	DD	1039525906
	DD	1039525906
	DD	1039525906
	DD	1039525906
	DD	1039525906
	DD	1039525906
	DD	1039525906
	DD	1039525906
	DD	1039525906
	DD	1039525906
	DD	1039525906
	DD	1039525906
	DD	1039525906
	DD	16777216
	DD	16777216
	DD	16777216
	DD	16777216
	DD	16777216
	DD	16777216
	DD	16777216
	DD	16777216
	DD	16777216
	DD	16777216
	DD	16777216
	DD	16777216
	DD	16777216
	DD	16777216
	DD	16777216
	DD	16777216
	DD	25165824
	DD	25165824
	DD	25165824
	DD	25165824
	DD	25165824
	DD	25165824
	DD	25165824
	DD	25165824
	DD	25165824
	DD	25165824
	DD	25165824
	DD	25165824
	DD	25165824
	DD	25165824
	DD	25165824
	DD	25165824
	DD	1059760811
	DD	1059760811
	DD	1059760811
	DD	1059760811
	DD	1059760811
	DD	1059760811
	DD	1059760811
	DD	1059760811
	DD	1059760811
	DD	1059760811
	DD	1059760811
	DD	1059760811
	DD	1059760811
	DD	1059760811
	DD	1059760811
	DD	1059760811
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
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	1060205056
	DD	901758606
	DD	901758606
	DD	901758606
	DD	901758606
	DD	901758606
	DD	901758606
	DD	901758606
	DD	901758606
	DD	901758606
	DD	901758606
	DD	901758606
	DD	901758606
	DD	901758606
	DD	901758606
	DD	901758606
	DD	901758606
	DD	2139095040
	DD	4286578688
	DB 0
	ORG $+54
	DB	0
	DD	1065353216
	DD	3212836864
	DB 0
	ORG $+54
	DB	0
	DD	0
	DD	2147483648
	DB 0
	ORG $+54
	DB	0
_vmldLnHATab	DD	1065353216
	DD	0
	DD	0
	DD	1065091072
	DD	1015087104
	DD	900509991
	DD	1064828928
	DD	1023541248
	DD	925811956
	DD	1064566784
	DD	1027915776
	DD	3084221144
	DD	1064304640
	DD	1032073216
	DD	3066991812
	DD	1064173568
	DD	1033195520
	DD	882149603
	DD	1063911424
	DD	1035468800
	DD	928189163
	DD	1063649280
	DD	1037783040
	DD	927501741
	DD	1063518208
	DD	1038958592
	DD	3076037756
	DD	1063256064
	DD	1040759808
	DD	904405630
	DD	1063124992
	DD	1041361920
	DD	3052231524
	DD	1062862848
	DD	1042581504
	DD	922094799
	DD	1062731776
	DD	1043201024
	DD	3070120623
	DD	1062469632
	DD	1044455424
	DD	3069864633
	DD	1062338560
	DD	1045091328
	DD	3063188516
	DD	1062207488
	DD	1045733376
	DD	3054902185
	DD	1061945344
	DD	1047035904
	DD	920635797
	DD	1061814272
	DD	1047697408
	DD	904920689
	DD	1061683200
	DD	1048365056
	DD	912483742
	DD	1061552128
	DD	1048807936
	DD	3052664405
	DD	1061421056
	DD	1049148416
	DD	912794238
	DD	1061158912
	DD	1049840384
	DD	889474359
	DD	1061027840
	DD	1050191872
	DD	3059868362
	DD	1060896768
	DD	1050546944
	DD	3059256525
	DD	1060765696
	DD	1050905600
	DD	912008988
	DD	1060634624
	DD	1051268352
	DD	912290698
	DD	1060503552
	DD	1051635200
	DD	3037211048
	DD	1060372480
	DD	1052005888
	DD	906226119
	DD	1060241408
	DD	1052380928
	DD	3052480305
	DD	1060110336
	DD	1052760064
	DD	3048768765
	DD	1059979264
	DD	1053143552
	DD	3049975450
	DD	1059848192
	DD	1053531392
	DD	894485718
	DD	1059717120
	DD	1053923840
	DD	897598623
	DD	1059586048
	DD	1054320896
	DD	907355277
	DD	1059586048
	DD	1054320896
	DD	907355277
	DD	1059454976
	DD	1054722816
	DD	881705073
	DD	1059323904
	DD	1055129600
	DD	3049723733
	DD	1059192832
	DD	1055541248
	DD	890353599
	DD	1059061760
	DD	1055958016
	DD	908173938
	DD	1059061760
	DD	1055958016
	DD	908173938
	DD	1058930688
	DD	1056380160
	DD	883644938
	DD	1058799616
	DD	1056807680
	DD	3052015799
	DD	1058668544
	DD	1057102592
	DD	884897284
	DD	1058668544
	DD	1057102592
	DD	884897284
	DD	1058537472
	DD	1057321920
	DD	3037632470
	DD	1058406400
	DD	1057544128
	DD	865017195
	DD	1058275328
	DD	1057769344
	DD	3042936546
	DD	1058275328
	DD	1057769344
	DD	3042936546
	DD	1058144256
	DD	1057997568
	DD	903344518
	DD	1058013184
	DD	1058228992
	DD	897862967
	DD	1058013184
	DD	1058228992
	DD	897862967
	DD	1057882112
	DD	1058463680
	DD	3047822280
	DD	1057882112
	DD	1058463680
	DD	3047822280
	DD	1057751040
	DD	1058701632
	DD	883793293
	DD	1057619968
	DD	1058943040
	DD	851667963
	DD	1057619968
	DD	1058943040
	DD	851667963
	DD	1057488896
	DD	1059187968
	DD	3000004036
	DD	1057488896
	DD	1059187968
	DD	3000004036
	DD	1057357824
	DD	1059436544
	DD	3047430717
	DD	1057357824
	DD	1059436544
	DD	3047430717
	DD	1057226752
	DD	1059688832
	DD	3043802308
	DD	1057226752
	DD	1059688832
	DD	3043802308
	DD	1057095680
	DD	1059944960
	DD	876113044
	DD	1057095680
	DD	1059944960
	DD	876113044
	DD	1056964608
	DD	1060205056
	DD	901758606
	DD	1060205056
	DD	901758606
	DD	1207959616
	DD	1174405120
	DD	1008730112
	DD	1400897536
	DD	0
	DD	1065353216
	DD	3204448256
	DD	1051372203
	DD	3196059648
	DD	1045220557
	DD	3190467243
	DD	1041387009
	DD	3187672480
	DD 2 DUP (0H)	
_2il0floatpacket_16	DD	04b400000H,04b400000H,04b400000H,04b400000H
_2il0floatpacket_85	DD	053800000H
_2il0floatpacket_86	DD	03c200000H
_2il0floatpacket_87	DD	0be0005a0H
_2il0floatpacket_88	DD	03e124e01H
_2il0floatpacket_89	DD	0be2aaaabH
_2il0floatpacket_90	DD	03e4ccccdH
_2il0floatpacket_91	DD	0be800000H
_2il0floatpacket_92	DD	03eaaaaabH
_2il0floatpacket_93	DD	0bf000000H
_2il0floatpacket_94	DD	0bf800000H
_2il0floatpacket_95	DD	03f317218H
_2il0floatpacket_96	DD	03f317200H
_2il0floatpacket_97	DD	035bfbe8eH
_2il0floatpacket_98	DD	048000040H
_2il0floatpacket_99	DD	046000000H
_2il0floatpacket_100	DD	03f800000H
_RDATA	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS
EXTRN	__ImageBase:PROC
EXTRN	_fltused:BYTE
	ENDIF
	END
