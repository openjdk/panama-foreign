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
	PUBLIC __svml_cosh1_ha_e9

__svml_cosh1_ha_e9	PROC

_B1_1::

        DB        243
        DB        15
        DB        30
        DB        250
L1::

        sub       rsp, 264
        mov       eax, 1082531225
        vmovups   XMMWORD PTR [192+rsp], xmm14
        mov       edx, 127
        vmovups   XMMWORD PTR [208+rsp], xmm13
        lea       r9, QWORD PTR [__ImageBase]
        vmovups   XMMWORD PTR [224+rsp], xmm12
        vmovups   XMMWORD PTR [240+rsp], xmm11
        vmovups   XMMWORD PTR [176+rsp], xmm6
        mov       QWORD PTR [168+rsp], r13
        lea       r13, QWORD PTR [95+rsp]
        vmovupd   xmm4, XMMWORD PTR [rcx]
        and       r13, -64
        vmovsd    xmm0, QWORD PTR [__svml_dcosh_ha_data_internal+4672]
        vandnpd   xmm13, xmm0, xmm4
        vmulsd    xmm5, xmm13, QWORD PTR [__svml_dcosh_ha_data_internal+3968]
        vmovsd    xmm1, QWORD PTR [__svml_dcosh_ha_data_internal+4160]
        vmovsd    xmm11, QWORD PTR [__svml_dcosh_ha_data_internal+4032]
        vaddsd    xmm0, xmm5, xmm1
        vpshufd   xmm14, xmm13, 85
        vmovd     xmm5, eax
        vmovsd    xmm12, QWORD PTR [__svml_dcosh_ha_data_internal+4096]
        vpcmpgtd  xmm6, xmm14, xmm5
        vpshufd   xmm14, xmm0, 0
        vmovd     xmm5, edx
        vpand     xmm14, xmm14, xmm5
        vsubsd    xmm1, xmm0, xmm1
        vmovmskps eax, xmm6
        vpslld    xmm6, xmm14, 1
        vpsllq    xmm0, xmm0, 45
        vmulsd    xmm11, xmm11, xmm1
        vpaddd    xmm5, xmm6, xmm14
        vmulsd    xmm12, xmm12, xmm1
        vsubsd    xmm13, xmm13, xmm11
        vpslld    xmm6, xmm5, 3
        vsubsd    xmm1, xmm13, xmm12
        vmovd     r8d, xmm6
        vmovq     xmm12, QWORD PTR [__svml_dcosh_ha_data_internal+4608]
        vmovq     xmm6, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+912+r9+r8]
        vpand     xmm11, xmm0, xmm12
        vpsubq    xmm12, xmm6, xmm11
        vmulsd    xmm6, xmm1, xmm1
        vmovsd    xmm2, QWORD PTR [__svml_dcosh_ha_data_internal+4544]
        vmulsd    xmm2, xmm2, xmm6
        vmovsd    xmm3, QWORD PTR [__svml_dcosh_ha_data_internal+4480]
        vmulsd    xmm3, xmm3, xmm6
        vaddsd    xmm2, xmm2, QWORD PTR [__svml_dcosh_ha_data_internal+4416]
        vmovq     xmm14, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+896+r9+r8]
        vmovq     xmm5, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+904+r9+r8]
        vpaddq    xmm0, xmm14, xmm11
        vpaddq    xmm13, xmm5, xmm11
        vaddsd    xmm11, xmm3, QWORD PTR [__svml_dcosh_ha_data_internal+4352]
        vmulsd    xmm3, xmm2, xmm6
        vaddsd    xmm5, xmm0, xmm12
        vsubsd    xmm14, xmm0, xmm12
        vmulsd    xmm2, xmm11, xmm6
        vmulsd    xmm3, xmm3, xmm1
        vmulsd    xmm5, xmm2, xmm5
        vaddsd    xmm1, xmm3, xmm1
        vaddsd    xmm6, xmm5, xmm13
        vmulsd    xmm1, xmm1, xmm14
        mov       QWORD PTR [256+rsp], r13
        vaddsd    xmm2, xmm1, xmm6
        vaddsd    xmm3, xmm2, xmm12
        vaddsd    xmm0, xmm3, xmm0
        and       eax, 1
        jne       _B1_3

_B1_2::

        vmovups   xmm6, XMMWORD PTR [176+rsp]
        vmovups   xmm11, XMMWORD PTR [240+rsp]
        vmovups   xmm12, XMMWORD PTR [224+rsp]
        vmovups   xmm13, XMMWORD PTR [208+rsp]
        vmovups   xmm14, XMMWORD PTR [192+rsp]
        mov       r13, QWORD PTR [168+rsp]
        add       rsp, 264
        ret

_B1_3::

        vmovsd    QWORD PTR [r13], xmm4
        vmovsd    QWORD PTR [64+r13], xmm0
        jne       _B1_6

_B1_4::

        vmovsd    xmm0, QWORD PTR [64+r13]
        jmp       _B1_2

_B1_6::

        lea       rcx, QWORD PTR [r13]
        lea       rdx, QWORD PTR [64+r13]

        call      __svml_dcosh_ha_cout_rare_internal
        jmp       _B1_4
        ALIGN     16

_B1_7::

__svml_cosh1_ha_e9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_cosh1_ha_e9_B1_B6:
	DD	938241
	DD	1430609
	DD	747593
	DD	1030208
	DD	968759
	DD	907303
	DD	845849
	DD	2162955

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B1_1
	DD	imagerel _B1_7
	DD	imagerel _unwind___svml_cosh1_ha_e9_B1_B6

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST1:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_cosh4_ha_l9

__svml_cosh4_ha_l9	PROC

_B2_1::

        DB        243
        DB        15
        DB        30
        DB        250
L14::

        sub       rsp, 312
        lea       rdx, QWORD PTR [__ImageBase]
        vmovups   XMMWORD PTR [272+rsp], xmm15
        vmovups   XMMWORD PTR [224+rsp], xmm10
        vmovups   XMMWORD PTR [256+rsp], xmm9
        vmovups   XMMWORD PTR [208+rsp], xmm8
        vmovups   XMMWORD PTR [240+rsp], xmm7
        mov       QWORD PTR [288+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovupd   ymm2, YMMWORD PTR [rcx]
        and       r13, -64
        vmovupd   ymm3, YMMWORD PTR [__svml_dcosh_ha_data_internal+4672]
        vmovupd   ymm8, YMMWORD PTR [__svml_dcosh_ha_data_internal+3968]
        vmovupd   ymm4, YMMWORD PTR [__svml_dcosh_ha_data_internal+4160]
        vmovupd   ymm0, YMMWORD PTR [__svml_dcosh_ha_data_internal+4480]
        vmovupd   ymm1, YMMWORD PTR [__svml_dcosh_ha_data_internal+4544]
        vandnpd   ymm3, ymm3, ymm2
        vfmadd213pd ymm8, ymm3, ymm4
        mov       QWORD PTR [296+rsp], r13
        vextracti128 xmm5, ymm3, 1
        vshufps   xmm9, xmm3, xmm5, 221
        vpcmpgtd  xmm5, xmm9, XMMWORD PTR [__svml_dcosh_ha_data_internal+4736]
        vmovmskps r8d, xmm5
        vextracti128 xmm7, ymm8, 1
        vsubpd    ymm4, ymm8, ymm4
        vshufps   xmm15, xmm8, xmm7, 136
        vpand     xmm9, xmm15, XMMWORD PTR [__svml_dcosh_ha_data_internal+4224]
        vpslld    xmm10, xmm9, 1
        vpaddd    xmm5, xmm10, xmm9
        vpslld    xmm10, xmm5, 3
        vfnmadd231pd ymm3, ymm4, YMMWORD PTR [__svml_dcosh_ha_data_internal+4032]
        vpsllq    ymm8, ymm8, 45
        vmovd     ecx, xmm10
        vfnmadd231pd ymm3, ymm4, YMMWORD PTR [__svml_dcosh_ha_data_internal+4096]
        vmovq     xmm7, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+896+rdx+rcx]
        vpand     ymm4, ymm8, YMMWORD PTR [__svml_dcosh_ha_data_internal+4608]
        vpextrd   r9d, xmm10, 2
        vpextrd   eax, xmm10, 1
        vpextrd   r10d, xmm10, 3
        vmovq     xmm9, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+896+rdx+r9]
        vmovhpd   xmm15, xmm7, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+896+rdx+rax]
        vmovhpd   xmm5, xmm9, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+896+rdx+r10]
        vmovq     xmm7, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+904+rdx+rcx]
        vmovhpd   xmm10, xmm7, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+904+rdx+rax]
        vinsertf128 ymm9, ymm15, xmm5, 1
        vmovq     xmm5, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+904+rdx+r9]
        vmovhpd   xmm7, xmm5, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+904+rdx+r10]
        vmovq     xmm5, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+912+rdx+rcx]
        vpaddq    ymm9, ymm9, ymm4
        vinsertf128 ymm15, ymm10, xmm7, 1
        vpaddq    ymm8, ymm15, ymm4
        vmovq     xmm10, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+912+rdx+r9]
        vmovhpd   xmm7, xmm5, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+912+rdx+rax]
        vmovhpd   xmm5, xmm10, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+912+rdx+r10]
        vmulpd    ymm15, ymm3, ymm3
        vfmadd213pd ymm1, ymm15, YMMWORD PTR [__svml_dcosh_ha_data_internal+4416]
        vfmadd213pd ymm0, ymm15, YMMWORD PTR [__svml_dcosh_ha_data_internal+4352]
        vmulpd    ymm1, ymm15, ymm1
        vmulpd    ymm0, ymm15, ymm0
        vfmadd213pd ymm1, ymm3, ymm3
        vinsertf128 ymm10, ymm7, xmm5, 1
        vpsubq    ymm7, ymm10, ymm4
        vaddpd    ymm4, ymm9, ymm7
        vsubpd    ymm5, ymm9, ymm7
        vfmadd213pd ymm0, ymm4, ymm8
        vfmadd213pd ymm1, ymm5, ymm0
        vaddpd    ymm0, ymm7, ymm1
        vaddpd    ymm0, ymm9, ymm0
        test      r8d, r8d
        jne       _B2_3

_B2_2::

        vmovups   xmm7, XMMWORD PTR [240+rsp]
        vmovups   xmm8, XMMWORD PTR [208+rsp]
        vmovups   xmm9, XMMWORD PTR [256+rsp]
        vmovups   xmm10, XMMWORD PTR [224+rsp]
        vmovups   xmm15, XMMWORD PTR [272+rsp]
        mov       r13, QWORD PTR [288+rsp]
        add       rsp, 312
        ret

_B2_3::

        vmovupd   YMMWORD PTR [r13], ymm2
        vmovupd   YMMWORD PTR [64+r13], ymm0

_B2_6::

        xor       eax, eax
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, eax
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, r8d

_B2_7::

        bt        esi, ebx
        jc        _B2_10

_B2_8::

        inc       ebx
        cmp       ebx, 4
        jl        _B2_7

_B2_9::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        vmovupd   ymm0, YMMWORD PTR [64+r13]
        jmp       _B2_2

_B2_10::

        vzeroupper
        lea       rcx, QWORD PTR [r13+rbx*8]
        lea       rdx, QWORD PTR [64+r13+rbx*8]

        call      __svml_dcosh_ha_cout_rare_internal
        jmp       _B2_8
        ALIGN     16

_B2_11::

__svml_cosh4_ha_l9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_cosh4_ha_l9_B1_B3:
	DD	935681
	DD	2413639
	DD	1013823
	DD	886838
	DD	1087533
	DD	960548
	DD	1177627
	DD	2556171

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B2_1
	DD	imagerel _B2_6
	DD	imagerel _unwind___svml_cosh4_ha_l9_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_cosh4_ha_l9_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B2_1
	DD	imagerel _B2_6
	DD	imagerel _unwind___svml_cosh4_ha_l9_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B2_6
	DD	imagerel _B2_11
	DD	imagerel _unwind___svml_cosh4_ha_l9_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST2:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_cosh2_ha_l9

__svml_cosh2_ha_l9	PROC

_B3_1::

        DB        243
        DB        15
        DB        30
        DB        250
L31::

        sub       rsp, 280
        lea       r9, QWORD PTR [__ImageBase]
        vmovups   XMMWORD PTR [224+rsp], xmm10
        vmovups   XMMWORD PTR [240+rsp], xmm9
        vmovups   XMMWORD PTR [192+rsp], xmm7
        vmovups   XMMWORD PTR [208+rsp], xmm6
        mov       QWORD PTR [256+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovupd   xmm3, XMMWORD PTR [rcx]
        and       r13, -64
        vmovupd   xmm1, XMMWORD PTR [__svml_dcosh_ha_data_internal+4672]
        vmovupd   xmm10, XMMWORD PTR [__svml_dcosh_ha_data_internal+3968]
        vandnpd   xmm1, xmm1, xmm3
        vmovupd   xmm9, XMMWORD PTR [__svml_dcosh_ha_data_internal+4160]
        vfmadd213pd xmm10, xmm1, xmm9
        vmovq     xmm0, QWORD PTR [__svml_dcosh_ha_data_internal+4736]
        vpshufd   xmm2, xmm1, 221
        vmovq     xmm7, QWORD PTR [__svml_dcosh_ha_data_internal+4224]
        vpcmpgtd  xmm2, xmm2, xmm0
        vmovupd   xmm5, XMMWORD PTR [__svml_dcosh_ha_data_internal+4480]
        vsubpd    xmm9, xmm10, xmm9
        vmovmskps edx, xmm2
        vpshufd   xmm0, xmm10, 136
        vpsllq    xmm10, xmm10, 45
        vpand     xmm6, xmm0, xmm7
        vpslld    xmm7, xmm6, 1
        vfnmadd231pd xmm1, xmm9, XMMWORD PTR [__svml_dcosh_ha_data_internal+4032]
        vpaddd    xmm2, xmm7, xmm6
        vpslld    xmm0, xmm2, 3
        vmovupd   xmm4, XMMWORD PTR [__svml_dcosh_ha_data_internal+4544]
        vmovd     eax, xmm0
        vfnmadd231pd xmm1, xmm9, XMMWORD PTR [__svml_dcosh_ha_data_internal+4096]
        vmovq     xmm7, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+896+r9+rax]
        vmovq     xmm6, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+904+r9+rax]
        vpextrd   r8d, xmm0, 1
        vmovhpd   xmm2, xmm7, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+896+r9+r8]
        vmovq     xmm7, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+912+r9+rax]
        vmovhpd   xmm0, xmm6, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+904+r9+r8]
        vmovhpd   xmm6, xmm7, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+912+r9+r8]
        vpand     xmm7, xmm10, XMMWORD PTR [__svml_dcosh_ha_data_internal+4608]
        vpsubq    xmm10, xmm6, xmm7
        vpaddq    xmm2, xmm2, xmm7
        vpaddq    xmm0, xmm0, xmm7
        vmulpd    xmm6, xmm1, xmm1
        vaddpd    xmm7, xmm2, xmm10
        vsubpd    xmm9, xmm2, xmm10
        vfmadd213pd xmm4, xmm6, XMMWORD PTR [__svml_dcosh_ha_data_internal+4416]
        vfmadd213pd xmm5, xmm6, XMMWORD PTR [__svml_dcosh_ha_data_internal+4352]
        vmulpd    xmm4, xmm6, xmm4
        vmulpd    xmm5, xmm6, xmm5
        vfmadd213pd xmm4, xmm1, xmm1
        vfmadd213pd xmm5, xmm7, xmm0
        vfmadd213pd xmm4, xmm9, xmm5
        vaddpd    xmm0, xmm10, xmm4
        mov       QWORD PTR [264+rsp], r13
        vaddpd    xmm0, xmm2, xmm0
        and       edx, 3
        jne       _B3_3

_B3_2::

        vmovups   xmm6, XMMWORD PTR [208+rsp]
        vmovups   xmm7, XMMWORD PTR [192+rsp]
        vmovups   xmm9, XMMWORD PTR [240+rsp]
        vmovups   xmm10, XMMWORD PTR [224+rsp]
        mov       r13, QWORD PTR [256+rsp]
        add       rsp, 280
        ret

_B3_3::

        vmovupd   XMMWORD PTR [r13], xmm3
        vmovupd   XMMWORD PTR [64+r13], xmm0
        je        _B3_2

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
        cmp       ebx, 2
        jl        _B3_7

_B3_9::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        vmovupd   xmm0, XMMWORD PTR [64+r13]
        jmp       _B3_2

_B3_10::

        lea       rcx, QWORD PTR [r13+rbx*8]
        lea       rdx, QWORD PTR [64+r13+rbx*8]

        call      __svml_dcosh_ha_cout_rare_internal
        jmp       _B3_8
        ALIGN     16

_B3_11::

__svml_cosh2_ha_l9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_cosh2_ha_l9_B1_B3:
	DD	802305
	DD	2151486
	DD	878646
	DD	817197
	DD	1021988
	DD	960539
	DD	2294027

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B3_1
	DD	imagerel _B3_6
	DD	imagerel _unwind___svml_cosh2_ha_l9_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_cosh2_ha_l9_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B3_1
	DD	imagerel _B3_6
	DD	imagerel _unwind___svml_cosh2_ha_l9_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B3_6
	DD	imagerel _B3_11
	DD	imagerel _unwind___svml_cosh2_ha_l9_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST3:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_cosh1_ha_l9

__svml_cosh1_ha_l9	PROC

_B4_1::

        DB        243
        DB        15
        DB        30
        DB        250
L46::

        sub       rsp, 264
        mov       eax, 1082531225
        vmovups   XMMWORD PTR [224+rsp], xmm15
        mov       edx, 127
        vmovups   XMMWORD PTR [240+rsp], xmm14
        lea       r9, QWORD PTR [__ImageBase]
        vmovups   XMMWORD PTR [176+rsp], xmm9
        vmovups   XMMWORD PTR [192+rsp], xmm8
        vmovd     xmm15, eax
        vmovups   XMMWORD PTR [208+rsp], xmm7
        mov       QWORD PTR [168+rsp], r13
        lea       r13, QWORD PTR [95+rsp]
        vmovupd   xmm4, XMMWORD PTR [rcx]
        and       r13, -64
        vmovsd    xmm0, QWORD PTR [__svml_dcosh_ha_data_internal+4672]
        vandnpd   xmm7, xmm0, xmm4
        vmovsd    xmm8, QWORD PTR [__svml_dcosh_ha_data_internal+4160]
        vmovapd   xmm14, xmm7
        vfmadd132sd xmm14, xmm8, QWORD PTR [__svml_dcosh_ha_data_internal+3968]
        vpshufd   xmm1, xmm7, 85
        vmovsd    xmm9, QWORD PTR [__svml_dcosh_ha_data_internal+4032]
        vpcmpgtd  xmm0, xmm1, xmm15
        vmovd     xmm15, edx
        vmovsd    xmm5, QWORD PTR [__svml_dcosh_ha_data_internal+4096]
        vpshufd   xmm1, xmm14, 0
        vsubsd    xmm8, xmm14, xmm8
        vmovmskps eax, xmm0
        vpand     xmm1, xmm1, xmm15
        vpsllq    xmm14, xmm14, 45
        vfnmadd213sd xmm9, xmm8, xmm7
        vpslld    xmm0, xmm1, 1
        vpaddd    xmm15, xmm0, xmm1
        vpslld    xmm0, xmm15, 3
        vmovd     r8d, xmm0
        vmovq     xmm7, QWORD PTR [__svml_dcosh_ha_data_internal+4608]
        vmovsd    xmm2, QWORD PTR [__svml_dcosh_ha_data_internal+4544]
        vfnmadd213sd xmm5, xmm8, xmm9
        vpand     xmm9, xmm14, xmm7
        vmovq     xmm15, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+896+r9+r8]
        vmovq     xmm0, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+904+r9+r8]
        vmovq     xmm1, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+912+r9+r8]
        vpaddq    xmm15, xmm15, xmm9
        vpaddq    xmm14, xmm0, xmm9
        vpsubq    xmm9, xmm1, xmm9
        vmulsd    xmm1, xmm5, xmm5
        vaddsd    xmm7, xmm15, xmm9
        vsubsd    xmm8, xmm15, xmm9
        vfmadd213sd xmm2, xmm1, QWORD PTR [__svml_dcosh_ha_data_internal+4416]
        vmovsd    xmm3, QWORD PTR [__svml_dcosh_ha_data_internal+4480]
        vfmadd213sd xmm3, xmm1, QWORD PTR [__svml_dcosh_ha_data_internal+4352]
        vmulsd    xmm2, xmm2, xmm1
        vmulsd    xmm3, xmm3, xmm1
        vfmadd213sd xmm2, xmm5, xmm5
        vfmadd213sd xmm3, xmm7, xmm14
        vfmadd213sd xmm2, xmm8, xmm3
        mov       QWORD PTR [256+rsp], r13
        vaddsd    xmm5, xmm2, xmm9
        vaddsd    xmm0, xmm5, xmm15
        and       eax, 1
        jne       _B4_3

_B4_2::

        vmovups   xmm7, XMMWORD PTR [208+rsp]
        vmovups   xmm8, XMMWORD PTR [192+rsp]
        vmovups   xmm9, XMMWORD PTR [176+rsp]
        vmovups   xmm14, XMMWORD PTR [240+rsp]
        vmovups   xmm15, XMMWORD PTR [224+rsp]
        mov       r13, QWORD PTR [168+rsp]
        add       rsp, 264
        ret

_B4_3::

        vmovsd    QWORD PTR [r13], xmm4
        vmovsd    QWORD PTR [64+r13], xmm0
        jne       _B4_6

_B4_4::

        vmovsd    xmm0, QWORD PTR [64+r13]
        jmp       _B4_2

_B4_6::

        lea       rcx, QWORD PTR [r13]
        lea       rdx, QWORD PTR [64+r13]

        call      __svml_dcosh_ha_cout_rare_internal
        jmp       _B4_4
        ALIGN     16

_B4_7::

__svml_cosh1_ha_l9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_cosh1_ha_l9_B1_B6:
	DD	939265
	DD	1430613
	DD	882765
	DD	821312
	DD	759863
	DD	1042471
	DD	981017
	DD	2162955

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B4_1
	DD	imagerel _B4_7
	DD	imagerel _unwind___svml_cosh1_ha_l9_B1_B6

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST4:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_cosh4_ha_e9

__svml_cosh4_ha_e9	PROC

_B5_1::

        DB        243
        DB        15
        DB        30
        DB        250
L59::

        sub       rsp, 328
        lea       rdx, QWORD PTR [__ImageBase]
        vmovups   XMMWORD PTR [208+rsp], xmm15
        vmovups   XMMWORD PTR [224+rsp], xmm14
        vmovups   XMMWORD PTR [240+rsp], xmm13
        vmovups   XMMWORD PTR [256+rsp], xmm12
        vmovups   XMMWORD PTR [272+rsp], xmm11
        vmovups   XMMWORD PTR [288+rsp], xmm10
        mov       QWORD PTR [304+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovupd   ymm4, YMMWORD PTR [rcx]
        and       r13, -64
        vmovupd   ymm1, YMMWORD PTR [__svml_dcosh_ha_data_internal+4672]
        vmovupd   ymm14, YMMWORD PTR [__svml_dcosh_ha_data_internal+4160]
        vandnpd   ymm10, ymm1, ymm4
        vmulpd    ymm3, ymm10, YMMWORD PTR [__svml_dcosh_ha_data_internal+3968]
        vaddpd    ymm11, ymm14, ymm3
        mov       QWORD PTR [312+rsp], r13
        vextractf128 xmm12, ymm11, 1
        vshufps   xmm13, xmm11, xmm12, 136
        vpsllq    xmm12, xmm12, 45
        vpand     xmm1, xmm13, XMMWORD PTR [__svml_dcosh_ha_data_internal+4224]
        vpslld    xmm15, xmm1, 1
        vpaddd    xmm3, xmm15, xmm1
        vextractf128 xmm0, ymm10, 1
        vshufps   xmm5, xmm10, xmm0, 221
        vpcmpgtd  xmm2, xmm5, XMMWORD PTR [__svml_dcosh_ha_data_internal+4736]
        vpslld    xmm5, xmm3, 3
        vmovd     ecx, xmm5
        vmovmskps r8d, xmm2
        vmovq     xmm1, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+904+rdx+rcx]
        vpextrd   r9d, xmm5, 2
        vpextrd   r10d, xmm5, 3
        vpextrd   eax, xmm5, 1
        vmovq     xmm2, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+896+rdx+r9]
        vmovq     xmm0, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+896+rdx+rcx]
        vmovhpd   xmm13, xmm2, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+896+rdx+r10]
        vmovhpd   xmm2, xmm1, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+904+rdx+rax]
        vmovq     xmm3, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+904+rdx+r9]
        vmovq     xmm1, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+912+rdx+r9]
        vmovhpd   xmm15, xmm0, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+896+rdx+rax]
        vmovhpd   xmm0, xmm3, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+904+rdx+r10]
        vmovhpd   xmm3, xmm1, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+912+rdx+r10]
        vsubpd    ymm1, ymm11, ymm14
        vpsllq    xmm11, xmm11, 45
        vmovq     xmm5, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+912+rdx+rcx]
        vmovhpd   xmm5, xmm5, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+912+rdx+rax]
        vmulpd    ymm14, ymm1, YMMWORD PTR [__svml_dcosh_ha_data_internal+4032]
        vmulpd    ymm1, ymm1, YMMWORD PTR [__svml_dcosh_ha_data_internal+4096]
        vsubpd    ymm14, ymm10, ymm14
        vmovupd   xmm10, XMMWORD PTR [__svml_dcosh_ha_data_internal+4608]
        vpand     xmm12, xmm12, xmm10
        vsubpd    ymm1, ymm14, ymm1
        vpand     xmm14, xmm11, xmm10
        vpaddq    xmm10, xmm2, xmm14
        vpaddq    xmm15, xmm15, xmm14
        vpaddq    xmm13, xmm13, xmm12
        vpsubq    xmm3, xmm3, xmm12
        vpsubq    xmm14, xmm5, xmm14
        vmulpd    ymm2, ymm1, ymm1
        vmulpd    ymm11, ymm2, YMMWORD PTR [__svml_dcosh_ha_data_internal+4544]
        vinsertf128 ymm13, ymm15, xmm13, 1
        vpaddq    xmm15, xmm0, xmm12
        vmulpd    ymm12, ymm2, YMMWORD PTR [__svml_dcosh_ha_data_internal+4480]
        vinsertf128 ymm0, ymm10, xmm15, 1
        vinsertf128 ymm15, ymm14, xmm3, 1
        vaddpd    ymm3, ymm11, YMMWORD PTR [__svml_dcosh_ha_data_internal+4416]
        vaddpd    ymm10, ymm12, YMMWORD PTR [__svml_dcosh_ha_data_internal+4352]
        vaddpd    ymm5, ymm13, ymm15
        vsubpd    ymm14, ymm13, ymm15
        vmulpd    ymm3, ymm2, ymm3
        vmulpd    ymm2, ymm2, ymm10
        vmulpd    ymm5, ymm5, ymm2
        vaddpd    ymm2, ymm0, ymm5
        vmulpd    ymm0, ymm1, ymm3
        vaddpd    ymm1, ymm1, ymm0
        vmulpd    ymm0, ymm14, ymm1
        vaddpd    ymm1, ymm2, ymm0
        vaddpd    ymm3, ymm15, ymm1
        vaddpd    ymm0, ymm13, ymm3
        test      r8d, r8d
        jne       _B5_3

_B5_2::

        vmovups   xmm10, XMMWORD PTR [288+rsp]
        vmovups   xmm11, XMMWORD PTR [272+rsp]
        vmovups   xmm12, XMMWORD PTR [256+rsp]
        vmovups   xmm13, XMMWORD PTR [240+rsp]
        vmovups   xmm14, XMMWORD PTR [224+rsp]
        vmovups   xmm15, XMMWORD PTR [208+rsp]
        mov       r13, QWORD PTR [304+rsp]
        add       rsp, 328
        ret

_B5_3::

        vmovupd   YMMWORD PTR [r13], ymm4
        vmovupd   YMMWORD PTR [64+r13], ymm0

_B5_6::

        xor       eax, eax
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, eax
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, r8d

_B5_7::

        bt        esi, ebx
        jc        _B5_10

_B5_8::

        inc       ebx
        cmp       ebx, 4
        jl        _B5_7

_B5_9::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        vmovupd   ymm0, YMMWORD PTR [64+r13]
        jmp       _B5_2

_B5_10::

        vzeroupper
        lea       rcx, QWORD PTR [r13+rbx*8]
        lea       rdx, QWORD PTR [64+r13+rbx*8]

        call      __svml_dcosh_ha_cout_rare_internal
        jmp       _B5_8
        ALIGN     16

_B5_11::

__svml_cosh4_ha_e9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_cosh4_ha_e9_B1_B3:
	DD	1069057
	DD	2544720
	DD	1222728
	DD	1161279
	DD	1099830
	DD	1038381
	DD	976932
	DD	915483
	DD	2687243

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B5_1
	DD	imagerel _B5_6
	DD	imagerel _unwind___svml_cosh4_ha_e9_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_cosh4_ha_e9_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B5_1
	DD	imagerel _B5_6
	DD	imagerel _unwind___svml_cosh4_ha_e9_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B5_6
	DD	imagerel _B5_11
	DD	imagerel _unwind___svml_cosh4_ha_e9_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST5:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_cosh1_ha_ex

__svml_cosh1_ha_ex	PROC

_B6_1::

        DB        243
        DB        15
        DB        30
        DB        250
L78::

        sub       rsp, 280
        mov       eax, 1082531225
        movups    XMMWORD PTR [240+rsp], xmm11
        mov       edx, 127
        movups    XMMWORD PTR [256+rsp], xmm10
        lea       r9, QWORD PTR [__ImageBase]
        movups    XMMWORD PTR [176+rsp], xmm9
        movups    XMMWORD PTR [192+rsp], xmm8
        movd      xmm1, eax
        movups    XMMWORD PTR [208+rsp], xmm7
        movd      xmm3, edx
        movups    XMMWORD PTR [224+rsp], xmm6
        mov       QWORD PTR [168+rsp], r13
        lea       r13, QWORD PTR [95+rsp]
        movups    xmm10, XMMWORD PTR [rcx]
        and       r13, -64
        movsd     xmm8, QWORD PTR [__svml_dcosh_ha_data_internal+4672]
        andnps    xmm8, xmm10
        movaps    xmm6, xmm8
        mulsd     xmm6, QWORD PTR [__svml_dcosh_ha_data_internal+3968]
        movsd     xmm9, QWORD PTR [__svml_dcosh_ha_data_internal+4160]
        movsd     xmm5, QWORD PTR [__svml_dcosh_ha_data_internal+4032]
        addsd     xmm6, xmm9
        movaps    xmm11, xmm6
        movsd     xmm4, QWORD PTR [__svml_dcosh_ha_data_internal+4096]
        subsd     xmm11, xmm9
        mulsd     xmm5, xmm11
        mulsd     xmm4, xmm11
        pshufd    xmm2, xmm8, 85
        subsd     xmm8, xmm5
        movsd     xmm0, QWORD PTR [__svml_dcosh_ha_data_internal+4544]
        pcmpgtd   xmm2, xmm1
        pshufd    xmm1, xmm6, 0
        psllq     xmm6, 45
        pand      xmm1, xmm3
        subsd     xmm8, xmm4
        movmskps  eax, xmm2
        movaps    xmm5, xmm8
        movdqa    xmm2, xmm1
        mulsd     xmm5, xmm8
        pslld     xmm2, 1
        mulsd     xmm0, xmm5
        paddd     xmm2, xmm1
        movsd     xmm7, QWORD PTR [__svml_dcosh_ha_data_internal+4480]
        pslld     xmm2, 3
        mulsd     xmm7, xmm5
        addsd     xmm0, QWORD PTR [__svml_dcosh_ha_data_internal+4416]
        movd      r8d, xmm2
        movq      xmm4, QWORD PTR [__svml_dcosh_ha_data_internal+4608]
        mulsd     xmm0, xmm5
        addsd     xmm7, QWORD PTR [__svml_dcosh_ha_data_internal+4352]
        movq      xmm1, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+896+r9+r8]
        pand      xmm6, xmm4
        movq      xmm2, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+912+r9+r8]
        and       eax, 1
        paddq     xmm1, xmm6
        psubq     xmm2, xmm6
        mulsd     xmm7, xmm5
        mulsd     xmm0, xmm8
        movdqa    xmm4, xmm1
        addsd     xmm0, xmm8
        addsd     xmm4, xmm2
        movq      xmm3, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+904+r9+r8]
        paddq     xmm3, xmm6
        mulsd     xmm7, xmm4
        movdqa    xmm6, xmm1
        addsd     xmm7, xmm3
        subsd     xmm6, xmm2
        mulsd     xmm0, xmm6
        mov       QWORD PTR [272+rsp], r13
        addsd     xmm0, xmm7
        addsd     xmm0, xmm2
        addsd     xmm0, xmm1
        jne       _B6_3

_B6_2::

        movups    xmm6, XMMWORD PTR [224+rsp]
        movups    xmm7, XMMWORD PTR [208+rsp]
        movups    xmm8, XMMWORD PTR [192+rsp]
        movups    xmm9, XMMWORD PTR [176+rsp]
        movups    xmm10, XMMWORD PTR [256+rsp]
        movups    xmm11, XMMWORD PTR [240+rsp]
        mov       r13, QWORD PTR [168+rsp]
        add       rsp, 280
        ret

_B6_3::

        movsd     QWORD PTR [r13], xmm10
        movsd     QWORD PTR [64+r13], xmm0
        jne       _B6_6

_B6_4::

        movsd     xmm0, QWORD PTR [64+r13]
        jmp       _B6_2

_B6_6::

        lea       rcx, QWORD PTR [r13]
        lea       rdx, QWORD PTR [64+r13]

        call      __svml_dcosh_ha_cout_rare_internal
        jmp       _B6_4
        ALIGN     16

_B6_7::

__svml_cosh1_ha_ex ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_cosh1_ha_ex_B1_B6:
	DD	1073153
	DD	1430624
	DD	944216
	DD	882764
	DD	821312
	DD	759863
	DD	1091623
	DD	1030169
	DD	2294027

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B6_1
	DD	imagerel _B6_7
	DD	imagerel _unwind___svml_cosh1_ha_ex_B1_B6

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST6:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_cosh2_ha_ex

__svml_cosh2_ha_ex	PROC

_B7_1::

        DB        243
        DB        15
        DB        30
        DB        250
L93::

        sub       rsp, 312
        lea       r9, QWORD PTR [__ImageBase]
        movups    XMMWORD PTR [192+rsp], xmm12
        movups    XMMWORD PTR [208+rsp], xmm11
        movups    XMMWORD PTR [224+rsp], xmm10
        movups    XMMWORD PTR [240+rsp], xmm9
        movups    XMMWORD PTR [256+rsp], xmm8
        movups    XMMWORD PTR [272+rsp], xmm7
        mov       QWORD PTR [288+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        movups    xmm11, XMMWORD PTR [rcx]
        and       r13, -64
        movups    xmm9, XMMWORD PTR [__svml_dcosh_ha_data_internal+4672]
        movups    xmm7, XMMWORD PTR [__svml_dcosh_ha_data_internal+3968]
        andnps    xmm9, xmm11
        mulpd     xmm7, xmm9
        movups    xmm10, XMMWORD PTR [__svml_dcosh_ha_data_internal+4160]
        addpd     xmm7, xmm10
        movq      xmm2, QWORD PTR [__svml_dcosh_ha_data_internal+4224]
        movq      xmm0, QWORD PTR [__svml_dcosh_ha_data_internal+4736]
        pshufd    xmm1, xmm9, 221
        pshufd    xmm12, xmm7, 136
        pcmpgtd   xmm1, xmm0
        pand      xmm12, xmm2
        movmskps  eax, xmm1
        movdqa    xmm1, xmm12
        pslld     xmm1, 1
        paddd     xmm1, xmm12
        movaps    xmm12, xmm7
        subpd     xmm12, xmm10
        movups    xmm4, XMMWORD PTR [__svml_dcosh_ha_data_internal+4032]
        pslld     xmm1, 3
        mulpd     xmm4, xmm12
        movd      edx, xmm1
        movups    xmm3, XMMWORD PTR [__svml_dcosh_ha_data_internal+4096]
        psllq     xmm7, 45
        movups    xmm8, XMMWORD PTR [__svml_dcosh_ha_data_internal+4544]
        mulpd     xmm3, xmm12
        subpd     xmm9, xmm4
        subpd     xmm9, xmm3
        movaps    xmm3, xmm9
        and       eax, 3
        mulpd     xmm3, xmm9
        mulpd     xmm8, xmm3
        movups    xmm5, XMMWORD PTR [__svml_dcosh_ha_data_internal+4480]
        addpd     xmm8, XMMWORD PTR [__svml_dcosh_ha_data_internal+4416]
        mulpd     xmm5, xmm3
        mulpd     xmm8, xmm3
        addpd     xmm5, XMMWORD PTR [__svml_dcosh_ha_data_internal+4352]
        mulpd     xmm8, xmm9
        mulpd     xmm3, xmm5
        addpd     xmm9, xmm8
        pshufd    xmm2, xmm1, 1
        movd      r8d, xmm2
        movq      xmm0, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+896+r9+rdx]
        movq      xmm2, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+912+r9+rdx]
        movhpd    xmm0, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+896+r9+r8]
        pand      xmm7, XMMWORD PTR [__svml_dcosh_ha_data_internal+4608]
        movhpd    xmm2, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+912+r9+r8]
        paddq     xmm0, xmm7
        psubq     xmm2, xmm7
        movaps    xmm4, xmm0
        addpd     xmm4, xmm2
        movq      xmm1, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+904+r9+rdx]
        movhpd    xmm1, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+904+r9+r8]
        paddq     xmm1, xmm7
        mulpd     xmm4, xmm3
        movaps    xmm7, xmm0
        subpd     xmm7, xmm2
        addpd     xmm1, xmm4
        mulpd     xmm7, xmm9
        addpd     xmm1, xmm7
        addpd     xmm2, xmm1
        mov       QWORD PTR [296+rsp], r13
        addpd     xmm0, xmm2
        jne       _B7_3

_B7_2::

        movups    xmm7, XMMWORD PTR [272+rsp]
        movups    xmm8, XMMWORD PTR [256+rsp]
        movups    xmm9, XMMWORD PTR [240+rsp]
        movups    xmm10, XMMWORD PTR [224+rsp]
        movups    xmm11, XMMWORD PTR [208+rsp]
        movups    xmm12, XMMWORD PTR [192+rsp]
        mov       r13, QWORD PTR [288+rsp]
        add       rsp, 312
        ret

_B7_3::

        movups    XMMWORD PTR [r13], xmm11
        movups    XMMWORD PTR [64+r13], xmm0
        je        _B7_2

_B7_6::

        xor       ecx, ecx
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, ecx
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, eax

_B7_7::

        mov       ecx, ebx
        mov       edx, 1
        shl       edx, cl
        test      esi, edx
        jne       _B7_10

_B7_8::

        inc       ebx
        cmp       ebx, 2
        jl        _B7_7

_B7_9::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        movups    xmm0, XMMWORD PTR [64+r13]
        jmp       _B7_2

_B7_10::

        lea       rcx, QWORD PTR [r13+rbx*8]
        lea       rdx, QWORD PTR [64+r13+rbx*8]

        call      __svml_dcosh_ha_cout_rare_internal
        jmp       _B7_8
        ALIGN     16

_B7_11::

__svml_cosh2_ha_ex ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_cosh2_ha_ex_B1_B3:
	DD	1068801
	DD	2413647
	DD	1144903
	DD	1083455
	DD	1022006
	DD	960557
	DD	899108
	DD	837659
	DD	2556171

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B7_1
	DD	imagerel _B7_6
	DD	imagerel _unwind___svml_cosh2_ha_ex_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_cosh2_ha_ex_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B7_1
	DD	imagerel _B7_6
	DD	imagerel _unwind___svml_cosh2_ha_ex_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B7_6
	DD	imagerel _B7_11
	DD	imagerel _unwind___svml_cosh2_ha_ex_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST7:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_cosh2_ha_e9

__svml_cosh2_ha_e9	PROC

_B8_1::

        DB        243
        DB        15
        DB        30
        DB        250
L112::

        sub       rsp, 280
        lea       r9, QWORD PTR [__ImageBase]
        vmovups   XMMWORD PTR [192+rsp], xmm14
        vmovups   XMMWORD PTR [208+rsp], xmm13
        vmovups   XMMWORD PTR [224+rsp], xmm12
        vmovups   XMMWORD PTR [240+rsp], xmm11
        mov       QWORD PTR [256+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovupd   xmm11, XMMWORD PTR [rcx]
        and       r13, -64
        vmovupd   xmm5, XMMWORD PTR [__svml_dcosh_ha_data_internal+4672]
        vandnpd   xmm0, xmm5, xmm11
        vmulpd    xmm4, xmm0, XMMWORD PTR [__svml_dcosh_ha_data_internal+3968]
        vmovupd   xmm3, XMMWORD PTR [__svml_dcosh_ha_data_internal+4160]
        vaddpd    xmm12, xmm3, xmm4
        vmovq     xmm1, QWORD PTR [__svml_dcosh_ha_data_internal+4224]
        vpshufd   xmm5, xmm12, 136
        vpand     xmm5, xmm5, xmm1
        vpslld    xmm1, xmm5, 1
        vmovq     xmm14, QWORD PTR [__svml_dcosh_ha_data_internal+4736]
        vpaddd    xmm4, xmm1, xmm5
        vpshufd   xmm2, xmm0, 221
        vpcmpgtd  xmm13, xmm2, xmm14
        vpslld    xmm2, xmm4, 3
        vmovd     eax, xmm2
        vmovmskps edx, xmm13
        vmovq     xmm1, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+896+r9+rax]
        vpextrd   r8d, xmm2, 1
        vmovhpd   xmm5, xmm1, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+896+r9+r8]
        vsubpd    xmm1, xmm12, xmm3
        vmulpd    xmm3, xmm1, XMMWORD PTR [__svml_dcosh_ha_data_internal+4032]
        vmulpd    xmm1, xmm1, XMMWORD PTR [__svml_dcosh_ha_data_internal+4096]
        vsubpd    xmm0, xmm0, xmm3
        vsubpd    xmm1, xmm0, xmm1
        vmulpd    xmm3, xmm1, xmm1
        vmovq     xmm13, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+912+r9+rax]
        vmovhpd   xmm4, xmm13, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+912+r9+r8]
        vmulpd    xmm13, xmm3, XMMWORD PTR [__svml_dcosh_ha_data_internal+4544]
        vmovq     xmm14, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+904+r9+rax]
        vmovhpd   xmm2, xmm14, QWORD PTR [imagerel(__svml_dcosh_ha_data_internal)+904+r9+r8]
        vpsllq    xmm14, xmm12, 45
        vpand     xmm12, xmm14, XMMWORD PTR [__svml_dcosh_ha_data_internal+4608]
        vaddpd    xmm14, xmm13, XMMWORD PTR [__svml_dcosh_ha_data_internal+4416]
        vmulpd    xmm13, xmm3, XMMWORD PTR [__svml_dcosh_ha_data_internal+4480]
        vpaddq    xmm5, xmm5, xmm12
        vpsubq    xmm4, xmm4, xmm12
        vpaddq    xmm0, xmm2, xmm12
        vaddpd    xmm13, xmm13, XMMWORD PTR [__svml_dcosh_ha_data_internal+4352]
        vaddpd    xmm12, xmm5, xmm4
        vsubpd    xmm2, xmm5, xmm4
        vmulpd    xmm14, xmm3, xmm14
        vmulpd    xmm3, xmm3, xmm13
        vmulpd    xmm3, xmm12, xmm3
        vaddpd    xmm3, xmm0, xmm3
        vmulpd    xmm0, xmm1, xmm14
        vaddpd    xmm0, xmm1, xmm0
        vmulpd    xmm2, xmm2, xmm0
        vaddpd    xmm0, xmm3, xmm2
        vaddpd    xmm4, xmm4, xmm0
        mov       QWORD PTR [264+rsp], r13
        vaddpd    xmm0, xmm5, xmm4
        and       edx, 3
        jne       _B8_3

_B8_2::

        vmovups   xmm11, XMMWORD PTR [240+rsp]
        vmovups   xmm12, XMMWORD PTR [224+rsp]
        vmovups   xmm13, XMMWORD PTR [208+rsp]
        vmovups   xmm14, XMMWORD PTR [192+rsp]
        mov       r13, QWORD PTR [256+rsp]
        add       rsp, 280
        ret

_B8_3::

        vmovupd   XMMWORD PTR [r13], xmm11
        vmovupd   XMMWORD PTR [64+r13], xmm0
        je        _B8_2

_B8_6::

        xor       eax, eax
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, eax
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, edx

_B8_7::

        bt        esi, ebx
        jc        _B8_10

_B8_8::

        inc       ebx
        cmp       ebx, 2
        jl        _B8_7

_B8_9::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        vmovupd   xmm0, XMMWORD PTR [64+r13]
        jmp       _B8_2

_B8_10::

        lea       rcx, QWORD PTR [r13+rbx*8]
        lea       rdx, QWORD PTR [64+r13+rbx*8]

        call      __svml_dcosh_ha_cout_rare_internal
        jmp       _B8_8
        ALIGN     16

_B8_11::

__svml_cosh2_ha_e9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_cosh2_ha_e9_B1_B3:
	DD	802305
	DD	2151486
	DD	1030198
	DD	968749
	DD	907300
	DD	845851
	DD	2294027

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B8_1
	DD	imagerel _B8_6
	DD	imagerel _unwind___svml_cosh2_ha_e9_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_cosh2_ha_e9_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B8_1
	DD	imagerel _B8_6
	DD	imagerel _unwind___svml_cosh2_ha_e9_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B8_6
	DD	imagerel _B8_11
	DD	imagerel _unwind___svml_cosh2_ha_e9_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST8:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_dcosh_ha_cout_rare_internal

__svml_dcosh_ha_cout_rare_internal	PROC

_B9_1::

        DB        243
        DB        15
        DB        30
        DB        250
L127::

        sub       rsp, 136
        xor       eax, eax
        movzx     r8d, WORD PTR [6+rcx]
        and       r8d, 32752
        movups    XMMWORD PTR [96+rsp], xmm12
        movups    XMMWORD PTR [112+rsp], xmm11
        movups    XMMWORD PTR [80+rsp], xmm6
        cmp       r8d, 32752
        je        _B9_12

_B9_2::

        mov       rcx, QWORD PTR [rcx]
        mov       QWORD PTR [64+rsp], rcx
        shr       rcx, 56
        and       ecx, 127
        mov       BYTE PTR [71+rsp], cl
        movzx     r8d, WORD PTR [70+rsp]
        and       r8d, 32752
        cmp       r8d, 15504
        jle       _B9_10

_B9_3::

        movsd     xmm12, QWORD PTR [64+rsp]
        movsd     xmm0, QWORD PTR [_vmldCoshHATab+1096]
        comisd    xmm0, xmm12
        jbe       _B9_9

_B9_4::

        mov       rcx, QWORD PTR [_vmldCoshHATab+1128]
        movsd     xmm2, QWORD PTR [_vmldCoshHATab+1040]
        mov       QWORD PTR [64+rsp], rcx
        mulsd     xmm2, xmm12
        comisd    xmm12, QWORD PTR [_vmldCoshHATab+1144]
        jb        _B9_8

_B9_5::

        movsd     xmm0, QWORD PTR [_vmldCoshHATab+1048]
        lea       r9, QWORD PTR [__ImageBase]
        addsd     xmm2, xmm0
        movsd     QWORD PTR [32+rsp], xmm2
        movsd     xmm1, QWORD PTR [32+rsp]
        mov       ecx, DWORD PTR [32+rsp]
        mov       r10d, ecx
        and       r10d, 63
        subsd     xmm1, QWORD PTR [_vmldCoshHATab+1048]
        movsd     QWORD PTR [40+rsp], xmm1
        lea       r8d, DWORD PTR [r10+r10]
        movsd     xmm2, QWORD PTR [40+rsp]
        lea       r11d, DWORD PTR [1+r10+r10]
        mulsd     xmm2, QWORD PTR [_vmldCoshHATab+1104]
        movsd     xmm3, QWORD PTR [40+rsp]
        subsd     xmm12, xmm2
        mulsd     xmm3, QWORD PTR [_vmldCoshHATab+1112]
        movsd     xmm1, QWORD PTR [_vmldCoshHATab+1088]
        subsd     xmm12, xmm3
        mulsd     xmm1, xmm12
        movsd     xmm4, QWORD PTR [imagerel(_vmldCoshHATab)+r9+r8*8]
        mov       r8, r9
        shr       ecx, 6
        addsd     xmm1, QWORD PTR [_vmldCoshHATab+1080]
        mulsd     xmm1, xmm12
        add       ecx, 1022
        and       ecx, 2047
        addsd     xmm1, QWORD PTR [_vmldCoshHATab+1072]
        mulsd     xmm1, xmm12
        addsd     xmm1, QWORD PTR [_vmldCoshHATab+1064]
        mulsd     xmm1, xmm12
        addsd     xmm1, QWORD PTR [_vmldCoshHATab+1056]
        mulsd     xmm1, xmm12
        mulsd     xmm1, xmm12
        addsd     xmm1, xmm12
        mulsd     xmm1, xmm4
        addsd     xmm1, QWORD PTR [imagerel(_vmldCoshHATab)+r8+r11*8]
        addsd     xmm1, xmm4
        cmp       ecx, 2046
        ja        _B9_7

_B9_6::

        mov       r8, QWORD PTR [_vmldCoshHATab+1128]
        shr       r8, 48
        shl       ecx, 4
        and       r8d, -32753
        or        r8d, ecx
        mov       WORD PTR [70+rsp], r8w
        movsd     xmm0, QWORD PTR [64+rsp]
        mulsd     xmm0, xmm1
        movsd     QWORD PTR [rdx], xmm0
        jmp       _B9_11

_B9_7::

        dec       ecx
        and       ecx, 2047
        movzx     r8d, WORD PTR [70+rsp]
        shl       ecx, 4
        and       r8d, -32753
        or        r8d, ecx
        mov       WORD PTR [70+rsp], r8w
        movsd     xmm0, QWORD PTR [64+rsp]
        mulsd     xmm1, xmm0
        mulsd     xmm1, QWORD PTR [_vmldCoshHATab+1024]
        movsd     QWORD PTR [rdx], xmm1
        jmp       _B9_11

_B9_8::

        movsd     xmm1, QWORD PTR [_vmldCoshHATab+1048]
        lea       r8, QWORD PTR [__ImageBase]
        movzx     r9d, WORD PTR [70+rsp]
        addsd     xmm2, xmm1
        movsd     QWORD PTR [32+rsp], xmm2
        and       r9d, -32753
        movsd     xmm6, QWORD PTR [32+rsp]
        mov       r11d, DWORD PTR [32+rsp]
        mov       ecx, r11d
        shr       ecx, 6
        and       r11d, 63
        subsd     xmm6, QWORD PTR [_vmldCoshHATab+1048]
        movsd     QWORD PTR [40+rsp], xmm6
        lea       r10d, DWORD PTR [1023+rcx]
        movsd     xmm11, QWORD PTR [40+rsp]
        add       ecx, 1022
        mulsd     xmm11, QWORD PTR [_vmldCoshHATab+1104]
        and       ecx, 2047
        movsd     xmm4, QWORD PTR [40+rsp]
        neg       r10d
        mulsd     xmm4, QWORD PTR [_vmldCoshHATab+1112]
        subsd     xmm12, xmm11
        movsd     xmm11, QWORD PTR [_vmldCoshHATab+1088]
        add       r10d, -4
        shl       ecx, 4
        and       r10d, 2047
        movsd     xmm6, QWORD PTR [_vmldCoshHATab+1080]
        or        r9d, ecx
        mov       WORD PTR [70+rsp], r9w
        lea       ecx, DWORD PTR [r11+r11]
        shl       r10d, 4
        and       r9d, -32753
        or        r9d, r10d
        subsd     xmm12, xmm4
        movaps    xmm5, xmm12
        mulsd     xmm5, xmm12
        mulsd     xmm11, xmm5
        mulsd     xmm6, xmm5
        addsd     xmm11, QWORD PTR [_vmldCoshHATab+1072]
        addsd     xmm6, QWORD PTR [_vmldCoshHATab+1064]
        mulsd     xmm11, xmm5
        mulsd     xmm6, xmm5
        addsd     xmm11, QWORD PTR [_vmldCoshHATab+1056]
        mulsd     xmm6, xmm12
        mulsd     xmm11, xmm5
        movsd     xmm4, QWORD PTR [imagerel(_vmldCoshHATab)+r8+rcx*8]
        neg       ecx
        movsd     xmm1, QWORD PTR [64+rsp]
        lea       r8d, DWORD PTR [1+r11+r11]
        lea       r11, QWORD PTR [__ImageBase]
        mov       r10, r11
        mov       WORD PTR [70+rsp], r9w
        lea       r9d, DWORD PTR [128+rcx]
        movsd     xmm0, QWORD PTR [64+rsp]
        add       ecx, 129
        movsd     xmm5, QWORD PTR [imagerel(_vmldCoshHATab)+r11+r8*8]
        mov       r8, r11
        movsd     xmm2, QWORD PTR [imagerel(_vmldCoshHATab)+r10+r9*8]
        mulsd     xmm4, xmm1
        mulsd     xmm2, xmm0
        mulsd     xmm5, xmm1
        movaps    xmm1, xmm4
        movsd     xmm3, QWORD PTR [imagerel(_vmldCoshHATab)+r8+rcx*8]
        addsd     xmm1, xmm2
        mulsd     xmm3, xmm0
        movsd     QWORD PTR [48+rsp], xmm1
        movaps    xmm1, xmm4
        movsd     xmm0, QWORD PTR [48+rsp]
        subsd     xmm4, xmm2
        subsd     xmm1, xmm0
        mulsd     xmm6, xmm4
        addsd     xmm1, xmm2
        movaps    xmm2, xmm5
        movsd     QWORD PTR [56+rsp], xmm1
        subsd     xmm2, xmm3
        mulsd     xmm2, xmm12
        mulsd     xmm12, xmm4
        addsd     xmm2, xmm3
        movsd     xmm1, QWORD PTR [48+rsp]
        addsd     xmm2, xmm5
        mulsd     xmm11, xmm1
        movsd     xmm0, QWORD PTR [56+rsp]
        addsd     xmm2, xmm0
        addsd     xmm2, xmm6
        addsd     xmm2, xmm11
        addsd     xmm2, xmm12
        movsd     QWORD PTR [48+rsp], xmm2
        movsd     xmm12, QWORD PTR [48+rsp]
        addsd     xmm1, xmm12
        movsd     QWORD PTR [rdx], xmm1
        jmp       _B9_11

_B9_9::

        movsd     xmm0, QWORD PTR [_vmldCoshHATab+1120]
        mov       eax, 3
        mulsd     xmm0, xmm0
        movsd     QWORD PTR [rdx], xmm0
        jmp       _B9_11

_B9_10::

        movsd     xmm0, QWORD PTR [_vmldCoshHATab+1136]
        addsd     xmm0, QWORD PTR [64+rsp]
        movsd     QWORD PTR [rdx], xmm0

_B9_11::

        movups    xmm6, XMMWORD PTR [80+rsp]
        movups    xmm11, XMMWORD PTR [112+rsp]
        movups    xmm12, XMMWORD PTR [96+rsp]
        add       rsp, 136
        ret

_B9_12::

        movsd     xmm0, QWORD PTR [rcx]
        mulsd     xmm0, xmm0
        movsd     QWORD PTR [rdx], xmm0
        jmp       _B9_11
        ALIGN     16

_B9_13::

__svml_dcosh_ha_cout_rare_internal ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_dcosh_ha_cout_rare_internal_B1_B12:
	DD	535041
	DD	354346
	DD	505893
	DD	444447
	DD	1114379

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B9_1
	DD	imagerel _B9_13
	DD	imagerel _unwind___svml_dcosh_ha_cout_rare_internal_B1_B12

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_RDATA	SEGMENT     READ PAGE   'DATA'
	ALIGN  32
	PUBLIC __svml_dcosh_ha_data_internal
__svml_dcosh_ha_data_internal	DD	0
	DD	1071644672
	DD	1828292879
	DD	1071691096
	DD	1014845819
	DD	1071739576
	DD	1853186616
	DD	1071790202
	DD	171030293
	DD	1071843070
	DD	1276261410
	DD	1071898278
	DD	3577096743
	DD	1071955930
	DD	3712504873
	DD	1072016135
	DD	1719614413
	DD	1072079006
	DD	1944781191
	DD	1072144660
	DD	1110089947
	DD	1072213221
	DD	2191782032
	DD	1072284817
	DD	2572866477
	DD	1072359583
	DD	3716502172
	DD	1072437659
	DD	3707479175
	DD	1072519192
	DD	2728693978
	DD	1072604335
	DD	0
	DD	0
	DD	1255956747
	DD	1015588398
	DD	3117910646
	DD	3161559105
	DD	3066496371
	DD	1015656574
	DD	3526460132
	DD	1014428778
	DD	300981948
	DD	1014684169
	DD	2951496418
	DD	1013793687
	DD	88491949
	DD	1015427660
	DD	330458198
	DD	3163282740
	DD	3993278767
	DD	3161724279
	DD	1451641639
	DD	1015474673
	DD	2960257726
	DD	1013742662
	DD	878562433
	DD	1015521741
	DD	2303740125
	DD	1014042725
	DD	3613079303
	DD	1014164738
	DD	396109971
	DD	3163462691
	DD	0
	DD	1071644672
	DD	2728693978
	DD	1071555759
	DD	3707479175
	DD	1071470616
	DD	3716502172
	DD	1071389083
	DD	2572866477
	DD	1071311007
	DD	2191782032
	DD	1071236241
	DD	1110089947
	DD	1071164645
	DD	1944781191
	DD	1071096084
	DD	1719614413
	DD	1071030430
	DD	3712504873
	DD	1070967559
	DD	3577096743
	DD	1070907354
	DD	1276261410
	DD	1070849702
	DD	171030293
	DD	1070794494
	DD	1853186616
	DD	1070741626
	DD	1014845819
	DD	1070691000
	DD	1828292879
	DD	1070642520
	DD	0
	DD	1123549184
	DD	0
	DD	1123549184
	DD	0
	DD	1123549184
	DD	0
	DD	1123549184
	DD	0
	DD	1123549184
	DD	0
	DD	1123549184
	DD	0
	DD	1123549184
	DD	0
	DD	1123549184
	DD	15
	DD	0
	DD	15
	DD	0
	DD	15
	DD	0
	DD	15
	DD	0
	DD	15
	DD	0
	DD	15
	DD	0
	DD	15
	DD	0
	DD	15
	DD	0
	DD	4
	DD	1071644672
	DD	4
	DD	1071644672
	DD	4
	DD	1071644672
	DD	4
	DD	1071644672
	DD	4
	DD	1071644672
	DD	4
	DD	1071644672
	DD	4
	DD	1071644672
	DD	4
	DD	1071644672
	DD	1431655747
	DD	1069897045
	DD	1431655747
	DD	1069897045
	DD	1431655747
	DD	1069897045
	DD	1431655747
	DD	1069897045
	DD	1431655747
	DD	1069897045
	DD	1431655747
	DD	1069897045
	DD	1431655747
	DD	1069897045
	DD	1431655747
	DD	1069897045
	DD	1430802231
	DD	1067799893
	DD	1430802231
	DD	1067799893
	DD	1430802231
	DD	1067799893
	DD	1430802231
	DD	1067799893
	DD	1430802231
	DD	1067799893
	DD	1430802231
	DD	1067799893
	DD	1430802231
	DD	1067799893
	DD	1430802231
	DD	1067799893
	DD	287861260
	DD	1065423121
	DD	287861260
	DD	1065423121
	DD	287861260
	DD	1065423121
	DD	287861260
	DD	1065423121
	DD	287861260
	DD	1065423121
	DD	287861260
	DD	1065423121
	DD	287861260
	DD	1065423121
	DD	287861260
	DD	1065423121
	DD	3658019094
	DD	1062650243
	DD	3658019094
	DD	1062650243
	DD	3658019094
	DD	1062650243
	DD	3658019094
	DD	1062650243
	DD	3658019094
	DD	1062650243
	DD	3658019094
	DD	1062650243
	DD	3658019094
	DD	1062650243
	DD	3658019094
	DD	1062650243
	DD	1993999322
	DD	1059717517
	DD	1993999322
	DD	1059717517
	DD	1993999322
	DD	1059717517
	DD	1993999322
	DD	1059717517
	DD	1993999322
	DD	1059717517
	DD	1993999322
	DD	1059717517
	DD	1993999322
	DD	1059717517
	DD	1993999322
	DD	1059717517
	DD	0
	DD	1071644672
	DD	0
	DD	0
	DD	0
	DD	1071644672
	DD	2851812149
	DD	1071650365
	DD	2595802551
	DD	1015767337
	DD	730821105
	DD	1071633346
	DD	1048019041
	DD	1071656090
	DD	1398474845
	DD	3160510595
	DD	2174652632
	DD	1071622081
	DD	3899555717
	DD	1071661845
	DD	427280750
	DD	3162546972
	DD	2912730644
	DD	1071610877
	DD	3541402996
	DD	1071667632
	DD	2759177317
	DD	1014854626
	DD	1533953344
	DD	1071599734
	DD	702412510
	DD	1071673451
	DD	3803266087
	DD	3162280415
	DD	929806999
	DD	1071588651
	DD	410360776
	DD	1071679301
	DD	1269990655
	DD	1011975870
	DD	3999357479
	DD	1071577627
	DD	3402036099
	DD	1071685182
	DD	405889334
	DD	1015105656
	DD	764307441
	DD	1071566664
	DD	1828292879
	DD	1071691096
	DD	1255956747
	DD	1015588398
	DD	2728693978
	DD	1071555759
	DD	728909815
	DD	1071697042
	DD	383930225
	DD	1015029468
	DD	4224142467
	DD	1071544913
	DD	852742562
	DD	1071703020
	DD	667253587
	DD	1009793559
	DD	3884662774
	DD	1071534126
	DD	2952712987
	DD	1071709030
	DD	3293494651
	DD	3160120301
	DD	351641897
	DD	1071523398
	DD	3490863953
	DD	1071715073
	DD	960797498
	DD	3162948880
	DD	863738719
	DD	1071512727
	DD	3228316108
	DD	1071721149
	DD	3010241991
	DD	3158422804
	DD	4076975200
	DD	1071502113
	DD	2930322912
	DD	1071727258
	DD	2599499422
	DD	3162714047
	DD	64696965
	DD	1071491558
	DD	3366293073
	DD	1071733400
	DD	3119426314
	DD	1014120554
	DD	382305176
	DD	1071481059
	DD	1014845819
	DD	1071739576
	DD	3117910646
	DD	3161559105
	DD	3707479175
	DD	1071470616
	DD	948735466
	DD	1071745785
	DD	3516338028
	DD	3162574883
	DD	135105010
	DD	1071460231
	DD	3949972341
	DD	1071752027
	DD	2068408548
	DD	1014913868
	DD	1242007932
	DD	1071449901
	DD	2214878420
	DD	1071758304
	DD	892270087
	DD	3163116422
	DD	1432208378
	DD	1071439627
	DD	828946858
	DD	1071764615
	DD	10642492
	DD	1015939438
	DD	3706687593
	DD	1071429408
	DD	586995997
	DD	1071770960
	DD	41662348
	DD	3162627992
	DD	2483480501
	DD	1071419245
	DD	2288159958
	DD	1071777339
	DD	2169144469
	DD	1014876021
	DD	777507147
	DD	1071409137
	DD	2440944790
	DD	1071783753
	DD	2492769774
	DD	1014147454
	DD	1610600570
	DD	1071399083
	DD	1853186616
	DD	1071790202
	DD	3066496371
	DD	1015656574
	DD	3716502172
	DD	1071389083
	DD	1337108031
	DD	1071796686
	DD	3203724452
	DD	1014677845
	DD	1540824585
	DD	1071379138
	DD	1709341917
	DD	1071803205
	DD	2571168217
	DD	1014152499
	DD	2420883922
	DD	1071369246
	DD	3790955393
	DD	1071809759
	DD	2352942462
	DD	3163180090
	DD	815859274
	DD	1071359408
	DD	4112506593
	DD	1071816349
	DD	2947355221
	DD	1014371048
	DD	4076559943
	DD	1071349622
	DD	3504003472
	DD	1071822975
	DD	3594001060
	DD	3157330652
	DD	2380618042
	DD	1071339890
	DD	2799960843
	DD	1071829637
	DD	1423655381
	DD	1015022151
	DD	3092190715
	DD	1071330210
	DD	2839424854
	DD	1071836335
	DD	1171596163
	DD	1013041679
	DD	697153126
	DD	1071320583
	DD	171030293
	DD	1071843070
	DD	3526460132
	DD	1014428778
	DD	2572866477
	DD	1071311007
	DD	4232894513
	DD	1071849840
	DD	2383938684
	DD	1014668519
	DD	3218338682
	DD	1071301483
	DD	2992903935
	DD	1071856648
	DD	2218154406
	DD	1015228193
	DD	1434058175
	DD	1071292011
	DD	1603444721
	DD	1071863493
	DD	1548633640
	DD	3162201326
	DD	321958744
	DD	1071282590
	DD	926591435
	DD	1071870375
	DD	3208833762
	DD	3162913514
	DD	2990417245
	DD	1071273219
	DD	1829099622
	DD	1071877294
	DD	1016661181
	DD	3163461005
	DD	3964284211
	DD	1071263899
	DD	887463927
	DD	1071884251
	DD	3596744163
	DD	3160794166
	DD	2069751141
	DD	1071254630
	DD	3272845541
	DD	1071891245
	DD	928852419
	DD	3163488248
	DD	434316067
	DD	1071245411
	DD	1276261410
	DD	1071898278
	DD	300981948
	DD	1014684169
	DD	2191782032
	DD	1071236241
	DD	78413852
	DD	1071905349
	DD	4183226867
	DD	3163017251
	DD	1892288442
	DD	1071227121
	DD	569847338
	DD	1071912458
	DD	472945272
	DD	3159290729
	DD	2682146384
	DD	1071218050
	DD	3645941911
	DD	1071919605
	DD	3814685081
	DD	3161573341
	DD	3418903055
	DD	1071209028
	DD	1617004845
	DD	1071926792
	DD	82804944
	DD	1010342778
	DD	2966275557
	DD	1071200055
	DD	3978100823
	DD	1071934017
	DD	3513027190
	DD	1015845963
	DD	194117574
	DD	1071191131
	DD	3049340112
	DD	1071941282
	DD	3062915824
	DD	1013170595
	DD	2568320822
	DD	1071182254
	DD	4040676318
	DD	1071948586
	DD	4090609238
	DD	1015663458
	DD	380978316
	DD	1071173426
	DD	3577096743
	DD	1071955930
	DD	2951496418
	DD	1013793687
	DD	1110089947
	DD	1071164645
	DD	2583551245
	DD	1071963314
	DD	3161094195
	DD	1015606491
	DD	3649726105
	DD	1071155911
	DD	1990012071
	DD	1071970738
	DD	3529070563
	DD	3162813193
	DD	2604962541
	DD	1071147225
	DD	2731501122
	DD	1071978202
	DD	1774031855
	DD	3162470021
	DD	1176749997
	DD	1071138586
	DD	1453150082
	DD	1071985707
	DD	498154669
	DD	3161488062
	DD	2571947539
	DD	1071129993
	DD	3395129871
	DD	1071993252
	DD	4025345435
	DD	3162335388
	DD	1413356050
	DD	1071121447
	DD	917841882
	DD	1072000839
	DD	18715565
	DD	1015659308
	DD	919555682
	DD	1071112947
	DD	3566716925
	DD	1072008466
	DD	1536826856
	DD	1014142433
	DD	19972402
	DD	1071104493
	DD	3712504873
	DD	1072016135
	DD	88491949
	DD	1015427660
	DD	1944781191
	DD	1071096084
	DD	2321106615
	DD	1072023846
	DD	2171176610
	DD	1009535771
	DD	1339972927
	DD	1071087721
	DD	363667784
	DD	1072031599
	DD	813753950
	DD	1015785209
	DD	1447192521
	DD	1071079403
	DD	3111574537
	DD	1072039393
	DD	2606161479
	DD	3162759746
	DD	1218806132
	DD	1071071130
	DD	2956612997
	DD	1072047230
	DD	2118169751
	DD	3162735553
	DD	3907805044
	DD	1071062901
	DD	885834528
	DD	1072055110
	DD	1973258547
	DD	3162261564
	DD	4182873220
	DD	1071054717
	DD	2186617381
	DD	1072063032
	DD	2270764084
	DD	3163272713
	DD	1013258799
	DD	1071046578
	DD	3561793907
	DD	1072070997
	DD	1157054053
	DD	1011890350
	DD	1963711167
	DD	1071038482
	DD	1719614413
	DD	1072079006
	DD	330458198
	DD	3163282740
	DD	1719614413
	DD	1071030430
	DD	1963711167
	DD	1072087058
	DD	1744767757
	DD	3160574294
	DD	3561793907
	DD	1071022421
	DD	1013258799
	DD	1072095154
	DD	1748797611
	DD	3160129082
	DD	2186617381
	DD	1071014456
	DD	4182873220
	DD	1072103293
	DD	629542646
	DD	3161996303
	DD	885834528
	DD	1071006534
	DD	3907805044
	DD	1072111477
	DD	2257091225
	DD	3161550407
	DD	2956612997
	DD	1070998654
	DD	1218806132
	DD	1072119706
	DD	1818613052
	DD	3162548441
	DD	3111574537
	DD	1070990817
	DD	1447192521
	DD	1072127979
	DD	1462857171
	DD	3162514521
	DD	363667784
	DD	1070983023
	DD	1339972927
	DD	1072136297
	DD	167908909
	DD	1015572152
	DD	2321106615
	DD	1070975270
	DD	1944781191
	DD	1072144660
	DD	3993278767
	DD	3161724279
	DD	3712504873
	DD	1070967559
	DD	19972402
	DD	1072153069
	DD	3507899862
	DD	1016009292
	DD	3566716925
	DD	1070959890
	DD	919555682
	DD	1072161523
	DD	3121969534
	DD	1012948226
	DD	917841882
	DD	1070952263
	DD	1413356050
	DD	1072170023
	DD	1651349291
	DD	3162668166
	DD	3395129871
	DD	1070944676
	DD	2571947539
	DD	1072178569
	DD	3558159064
	DD	3163376669
	DD	1453150082
	DD	1070937131
	DD	1176749997
	DD	1072187162
	DD	2738998779
	DD	3162035844
	DD	2731501122
	DD	1070929626
	DD	2604962541
	DD	1072195801
	DD	2614425274
	DD	3163539192
	DD	1990012071
	DD	1070922162
	DD	3649726105
	DD	1072204487
	DD	4085036346
	DD	1015649474
	DD	2583551245
	DD	1070914738
	DD	1110089947
	DD	1072213221
	DD	1451641639
	DD	1015474673
	DD	3577096743
	DD	1070907354
	DD	380978316
	DD	1072222002
	DD	854188970
	DD	3160462686
	DD	4040676318
	DD	1070900010
	DD	2568320822
	DD	1072230830
	DD	2732824428
	DD	1014352915
	DD	3049340112
	DD	1070892706
	DD	194117574
	DD	1072239707
	DD	777528612
	DD	3163412089
	DD	3978100823
	DD	1070885441
	DD	2966275557
	DD	1072248631
	DD	2176155324
	DD	3159842759
	DD	1617004845
	DD	1070878216
	DD	3418903055
	DD	1072257604
	DD	2527457337
	DD	3160820604
	DD	3645941911
	DD	1070871029
	DD	2682146384
	DD	1072266626
	DD	2082178513
	DD	3163363419
	DD	569847338
	DD	1070863882
	DD	1892288442
	DD	1072275697
	DD	2446255666
	DD	3162600381
	DD	78413852
	DD	1070856773
	DD	2191782032
	DD	1072284817
	DD	2960257726
	DD	1013742662
	DD	1276261410
	DD	1070849702
	DD	434316067
	DD	1072293987
	DD	2028358766
	DD	1013458122
	DD	3272845541
	DD	1070842669
	DD	2069751141
	DD	1072303206
	DD	1562170675
	DD	3162724681
	DD	887463927
	DD	1070835675
	DD	3964284211
	DD	1072312475
	DD	2111583915
	DD	1015427164
	DD	1829099622
	DD	1070828718
	DD	2990417245
	DD	1072321795
	DD	3683467745
	DD	3163369326
	DD	926591435
	DD	1070821799
	DD	321958744
	DD	1072331166
	DD	3401933767
	DD	1015794558
	DD	1603444721
	DD	1070814917
	DD	1434058175
	DD	1072340587
	DD	251133233
	DD	1015085769
	DD	2992903935
	DD	1070808072
	DD	3218338682
	DD	1072350059
	DD	3404164304
	DD	3162477108
	DD	4232894513
	DD	1070801264
	DD	2572866477
	DD	1072359583
	DD	878562433
	DD	1015521741
	DD	171030293
	DD	1070794494
	DD	697153126
	DD	1072369159
	DD	1283515429
	DD	3163283189
	DD	2839424854
	DD	1070787759
	DD	3092190715
	DD	1072378786
	DD	814012168
	DD	3159523422
	DD	2799960843
	DD	1070781061
	DD	2380618042
	DD	1072388466
	DD	3149557219
	DD	3163320799
	DD	3504003472
	DD	1070774399
	DD	4076559943
	DD	1072398198
	DD	2119478331
	DD	3160758351
	DD	4112506593
	DD	1070767773
	DD	815859274
	DD	1072407984
	DD	240396590
	DD	3163487443
	DD	3790955393
	DD	1070761183
	DD	2420883922
	DD	1072417822
	DD	2049810052
	DD	1014119888
	DD	1709341917
	DD	1070754629
	DD	1540824585
	DD	1072427714
	DD	1064017011
	DD	3163487690
	DD	1337108031
	DD	1070748110
	DD	3716502172
	DD	1072437659
	DD	2303740125
	DD	1014042725
	DD	1853186616
	DD	1070741626
	DD	1610600570
	DD	1072447659
	DD	3766732298
	DD	1015760183
	DD	2440944790
	DD	1070735177
	DD	777507147
	DD	1072457713
	DD	4282924205
	DD	1015187533
	DD	2288159958
	DD	1070728763
	DD	2483480501
	DD	1072467821
	DD	1216371780
	DD	1013034172
	DD	586995997
	DD	1070722384
	DD	3706687593
	DD	1072477984
	DD	3521726939
	DD	1013253067
	DD	828946858
	DD	1070716039
	DD	1432208378
	DD	1072488203
	DD	1401068914
	DD	3162363963
	DD	2214878420
	DD	1070709728
	DD	1242007932
	DD	1072498477
	DD	1132034716
	DD	3163339831
	DD	3949972341
	DD	1070703451
	DD	135105010
	DD	1072508807
	DD	1906148728
	DD	3163375739
	DD	948735466
	DD	1070697209
	DD	3707479175
	DD	1072519192
	DD	3613079303
	DD	1014164738
	DD	1014845819
	DD	1070691000
	DD	382305176
	DD	1072529635
	DD	2347622376
	DD	3162578625
	DD	3366293073
	DD	1070684824
	DD	64696965
	DD	1072540134
	DD	1768797490
	DD	1015816960
	DD	2930322912
	DD	1070678682
	DD	4076975200
	DD	1072550689
	DD	2029000899
	DD	1015208535
	DD	3228316108
	DD	1070672573
	DD	863738719
	DD	1072561303
	DD	1326992220
	DD	3162613197
	DD	3490863953
	DD	1070666497
	DD	351641897
	DD	1072571974
	DD	2172261526
	DD	3163010599
	DD	2952712987
	DD	1070660454
	DD	3884662774
	DD	1072582702
	DD	2158611599
	DD	1014210185
	DD	852742562
	DD	1070654444
	DD	4224142467
	DD	1072593489
	DD	3389820386
	DD	1015207202
	DD	728909815
	DD	1070648466
	DD	2728693978
	DD	1072604335
	DD	396109971
	DD	3163462691
	DD	1828292879
	DD	1070642520
	DD	764307441
	DD	1072615240
	DD	3021057420
	DD	3163329523
	DD	3402036099
	DD	1070636606
	DD	3999357479
	DD	1072626203
	DD	2258941616
	DD	1015924724
	DD	410360776
	DD	1070630725
	DD	929806999
	DD	1072637227
	DD	3205336643
	DD	1015259557
	DD	702412510
	DD	1070624875
	DD	1533953344
	DD	1072648310
	DD	769171851
	DD	1015665633
	DD	3541402996
	DD	1070619056
	DD	2912730644
	DD	1072659453
	DD	3490067722
	DD	3163405074
	DD	3899555717
	DD	1070613269
	DD	2174652632
	DD	1072670657
	DD	4087714590
	DD	1014450259
	DD	1048019041
	DD	1070607514
	DD	730821105
	DD	1072681922
	DD	2523232743
	DD	1012067188
	DD	2851812149
	DD	1070601789
	DD	1697350398
	DD	1073157447
	DD	1697350398
	DD	1073157447
	DD	1697350398
	DD	1073157447
	DD	1697350398
	DD	1073157447
	DD	1697350398
	DD	1073157447
	DD	1697350398
	DD	1073157447
	DD	1697350398
	DD	1073157447
	DD	1697350398
	DD	1073157447
	DD	4277796864
	DD	1072049730
	DD	4277796864
	DD	1072049730
	DD	4277796864
	DD	1072049730
	DD	4277796864
	DD	1072049730
	DD	4277796864
	DD	1072049730
	DD	4277796864
	DD	1072049730
	DD	4277796864
	DD	1072049730
	DD	4277796864
	DD	1072049730
	DD	3164486458
	DD	1031600026
	DD	3164486458
	DD	1031600026
	DD	3164486458
	DD	1031600026
	DD	3164486458
	DD	1031600026
	DD	3164486458
	DD	1031600026
	DD	3164486458
	DD	1031600026
	DD	3164486458
	DD	1031600026
	DD	3164486458
	DD	1031600026
	DD	0
	DD	1120403456
	DD	0
	DD	1120403456
	DD	0
	DD	1120403456
	DD	0
	DD	1120403456
	DD	0
	DD	1120403456
	DD	0
	DD	1120403456
	DD	0
	DD	1120403456
	DD	0
	DD	1120403456
	DD	127
	DD	127
	DD	127
	DD	127
	DD	127
	DD	127
	DD	127
	DD	127
	DD	127
	DD	127
	DD	127
	DD	127
	DD	127
	DD	127
	DD	127
	DD	127
	DD	0
	DD	1072693248
	DD	0
	DD	1072693248
	DD	0
	DD	1072693248
	DD	0
	DD	1072693248
	DD	0
	DD	1072693248
	DD	0
	DD	1072693248
	DD	0
	DD	1072693248
	DD	0
	DD	1072693248
	DD	4294967128
	DD	1071644671
	DD	4294967128
	DD	1071644671
	DD	4294967128
	DD	1071644671
	DD	4294967128
	DD	1071644671
	DD	4294967128
	DD	1071644671
	DD	4294967128
	DD	1071644671
	DD	4294967128
	DD	1071644671
	DD	4294967128
	DD	1071644671
	DD	1431655910
	DD	1069897045
	DD	1431655910
	DD	1069897045
	DD	1431655910
	DD	1069897045
	DD	1431655910
	DD	1069897045
	DD	1431655910
	DD	1069897045
	DD	1431655910
	DD	1069897045
	DD	1431655910
	DD	1069897045
	DD	1431655910
	DD	1069897045
	DD	2898925341
	DD	1067799893
	DD	2898925341
	DD	1067799893
	DD	2898925341
	DD	1067799893
	DD	2898925341
	DD	1067799893
	DD	2898925341
	DD	1067799893
	DD	2898925341
	DD	1067799893
	DD	2898925341
	DD	1067799893
	DD	2898925341
	DD	1067799893
	DD	564252519
	DD	1065423121
	DD	564252519
	DD	1065423121
	DD	564252519
	DD	1065423121
	DD	564252519
	DD	1065423121
	DD	564252519
	DD	1065423121
	DD	564252519
	DD	1065423121
	DD	564252519
	DD	1065423121
	DD	564252519
	DD	1065423121
	DD	0
	DD	2146435072
	DD	0
	DD	2146435072
	DD	0
	DD	2146435072
	DD	0
	DD	2146435072
	DD	0
	DD	2146435072
	DD	0
	DD	2146435072
	DD	0
	DD	2146435072
	DD	0
	DD	2146435072
	DD	0
	DD	2147483648
	DD	0
	DD	2147483648
	DD	0
	DD	2147483648
	DD	0
	DD	2147483648
	DD	0
	DD	2147483648
	DD	0
	DD	2147483648
	DD	0
	DD	2147483648
	DD	0
	DD	2147483648
	DD	1082531225
	DD	1082531225
	DD	1082531225
	DD	1082531225
	DD	1082531225
	DD	1082531225
	DD	1082531225
	DD	1082531225
	DD	1082531225
	DD	1082531225
	DD	1082531225
	DD	1082531225
	DD	1082531225
	DD	1082531225
	DD	1082531225
	DD	1082531225
_vmldCoshHATab	DD	0
	DD	1072693248
	DD	0
	DD	0
	DD	1048019041
	DD	1072704666
	DD	1398474845
	DD	3161559171
	DD	3541402996
	DD	1072716208
	DD	2759177317
	DD	1015903202
	DD	410360776
	DD	1072727877
	DD	1269990655
	DD	1013024446
	DD	1828292879
	DD	1072739672
	DD	1255956747
	DD	1016636974
	DD	852742562
	DD	1072751596
	DD	667253587
	DD	1010842135
	DD	3490863953
	DD	1072763649
	DD	960797498
	DD	3163997456
	DD	2930322912
	DD	1072775834
	DD	2599499422
	DD	3163762623
	DD	1014845819
	DD	1072788152
	DD	3117910646
	DD	3162607681
	DD	3949972341
	DD	1072800603
	DD	2068408548
	DD	1015962444
	DD	828946858
	DD	1072813191
	DD	10642492
	DD	1016988014
	DD	2288159958
	DD	1072825915
	DD	2169144469
	DD	1015924597
	DD	1853186616
	DD	1072838778
	DD	3066496371
	DD	1016705150
	DD	1709341917
	DD	1072851781
	DD	2571168217
	DD	1015201075
	DD	4112506593
	DD	1072864925
	DD	2947355221
	DD	1015419624
	DD	2799960843
	DD	1072878213
	DD	1423655381
	DD	1016070727
	DD	171030293
	DD	1072891646
	DD	3526460132
	DD	1015477354
	DD	2992903935
	DD	1072905224
	DD	2218154406
	DD	1016276769
	DD	926591435
	DD	1072918951
	DD	3208833762
	DD	3163962090
	DD	887463927
	DD	1072932827
	DD	3596744163
	DD	3161842742
	DD	1276261410
	DD	1072946854
	DD	300981948
	DD	1015732745
	DD	569847338
	DD	1072961034
	DD	472945272
	DD	3160339305
	DD	1617004845
	DD	1072975368
	DD	82804944
	DD	1011391354
	DD	3049340112
	DD	1072989858
	DD	3062915824
	DD	1014219171
	DD	3577096743
	DD	1073004506
	DD	2951496418
	DD	1014842263
	DD	1990012071
	DD	1073019314
	DD	3529070563
	DD	3163861769
	DD	1453150082
	DD	1073034283
	DD	498154669
	DD	3162536638
	DD	917841882
	DD	1073049415
	DD	18715565
	DD	1016707884
	DD	3712504873
	DD	1073064711
	DD	88491949
	DD	1016476236
	DD	363667784
	DD	1073080175
	DD	813753950
	DD	1016833785
	DD	2956612997
	DD	1073095806
	DD	2118169751
	DD	3163784129
	DD	2186617381
	DD	1073111608
	DD	2270764084
	DD	3164321289
	DD	1719614413
	DD	1073127582
	DD	330458198
	DD	3164331316
	DD	1013258799
	DD	1073143730
	DD	1748797611
	DD	3161177658
	DD	3907805044
	DD	1073160053
	DD	2257091225
	DD	3162598983
	DD	1447192521
	DD	1073176555
	DD	1462857171
	DD	3163563097
	DD	1944781191
	DD	1073193236
	DD	3993278767
	DD	3162772855
	DD	919555682
	DD	1073210099
	DD	3121969534
	DD	1013996802
	DD	2571947539
	DD	1073227145
	DD	3558159064
	DD	3164425245
	DD	2604962541
	DD	1073244377
	DD	2614425274
	DD	3164587768
	DD	1110089947
	DD	1073261797
	DD	1451641639
	DD	1016523249
	DD	2568320822
	DD	1073279406
	DD	2732824428
	DD	1015401491
	DD	2966275557
	DD	1073297207
	DD	2176155324
	DD	3160891335
	DD	2682146384
	DD	1073315202
	DD	2082178513
	DD	3164411995
	DD	2191782032
	DD	1073333393
	DD	2960257726
	DD	1014791238
	DD	2069751141
	DD	1073351782
	DD	1562170675
	DD	3163773257
	DD	2990417245
	DD	1073370371
	DD	3683467745
	DD	3164417902
	DD	1434058175
	DD	1073389163
	DD	251133233
	DD	1016134345
	DD	2572866477
	DD	1073408159
	DD	878562433
	DD	1016570317
	DD	3092190715
	DD	1073427362
	DD	814012168
	DD	3160571998
	DD	4076559943
	DD	1073446774
	DD	2119478331
	DD	3161806927
	DD	2420883922
	DD	1073466398
	DD	2049810052
	DD	1015168464
	DD	3716502172
	DD	1073486235
	DD	2303740125
	DD	1015091301
	DD	777507147
	DD	1073506289
	DD	4282924205
	DD	1016236109
	DD	3706687593
	DD	1073526560
	DD	3521726939
	DD	1014301643
	DD	1242007932
	DD	1073547053
	DD	1132034716
	DD	3164388407
	DD	3707479175
	DD	1073567768
	DD	3613079303
	DD	1015213314
	DD	64696965
	DD	1073588710
	DD	1768797490
	DD	1016865536
	DD	863738719
	DD	1073609879
	DD	1326992220
	DD	3163661773
	DD	3884662774
	DD	1073631278
	DD	2158611599
	DD	1015258761
	DD	2728693978
	DD	1073652911
	DD	396109971
	DD	3164511267
	DD	3999357479
	DD	1073674779
	DD	2258941616
	DD	1016973300
	DD	1533953344
	DD	1073696886
	DD	769171851
	DD	1016714209
	DD	2174652632
	DD	1073719233
	DD	4087714590
	DD	1015498835
	DD	0
	DD	1073741824
	DD	0
	DD	0
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
	DD	2411329662
	DD	1082536910
	DD	4277796864
	DD	1065758274
	DD	3164486458
	DD	1025308570
	DD	4294967295
	DD	2146435071
	DD	0
	DD	0
	DD	0
	DD	1072693248
	DD	3875694624
	DD	1077247184
_RDATA	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS
EXTRN	__ImageBase:PROC
EXTRN	_fltused:BYTE
	ENDIF
	END
