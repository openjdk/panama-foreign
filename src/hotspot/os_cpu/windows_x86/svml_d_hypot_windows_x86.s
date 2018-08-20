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
	PUBLIC __svml_hypot2_ha_e9

__svml_hypot2_ha_e9	PROC

_B1_1::

        DB        243
        DB        15
        DB        30
        DB        250
L1::

        sub       rsp, 344
        vmovups   XMMWORD PTR [256+rsp], xmm15
        vmovups   XMMWORD PTR [272+rsp], xmm14
        vmovups   XMMWORD PTR [288+rsp], xmm12
        vmovups   XMMWORD PTR [304+rsp], xmm11
        mov       QWORD PTR [320+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovupd   xmm11, XMMWORD PTR [rcx]
        and       r13, -64
        vmovupd   xmm5, XMMWORD PTR [rdx]
        vmovupd   xmm3, XMMWORD PTR [__svml_dhypot_ha_data_internal+64]
        vandpd    xmm4, xmm11, xmm3
        vandpd    xmm2, xmm5, xmm3
        vmaxpd    xmm0, xmm4, xmm2
        vminpd    xmm1, xmm2, xmm4
        vpshufd   xmm14, xmm0, 221
        vpshufd   xmm15, xmm1, 221
        vmovq     xmm4, QWORD PTR [__svml_dhypot_ha_data_internal+512]
        vpsubd    xmm14, xmm14, xmm15
        vpcmpgtd  xmm4, xmm14, xmm4
        vmovupd   xmm12, XMMWORD PTR [__svml_dhypot_ha_data_internal]
        vpshufd   xmm4, xmm4, 80
        vandpd    xmm2, xmm0, xmm12
        vandnpd   xmm12, xmm4, xmm12
        vandpd    xmm15, xmm1, xmm12
        vmulpd    xmm4, xmm2, xmm2
        vmulpd    xmm12, xmm15, xmm15
        vsubpd    xmm3, xmm0, xmm2
        vsubpd    xmm14, xmm1, xmm15
        vaddpd    xmm0, xmm0, xmm2
        vaddpd    xmm4, xmm4, xmm12
        vmulpd    xmm1, xmm1, xmm14
        vmulpd    xmm12, xmm3, xmm0
        vmulpd    xmm14, xmm15, xmm14
        vaddpd    xmm0, xmm12, xmm1
        vaddpd    xmm15, xmm0, xmm14
        vaddpd    xmm1, xmm4, xmm15
        vmovq     xmm14, QWORD PTR [__svml_dhypot_ha_data_internal+640]
        vpshufd   xmm3, xmm1, 221
        vpcmpgtd  xmm0, xmm3, xmm14
        vcvtpd2ps xmm14, xmm1
        vmovq     xmm2, QWORD PTR [__svml_dhypot_ha_data_internal+576]
        vpcmpgtd  xmm12, xmm2, xmm3
        vpor      xmm2, xmm12, xmm0
        vmovlhps  xmm12, xmm14, xmm14
        vrsqrtps  xmm0, xmm12
        vcvtps2pd xmm14, xmm0
        vpshufd   xmm3, xmm2, 80
        vmovmskpd edx, xmm3
        vmulpd    xmm3, xmm14, xmm14
        vmulpd    xmm2, xmm4, xmm3
        vmulpd    xmm0, xmm15, xmm3
        vsubpd    xmm12, xmm2, XMMWORD PTR [__svml_dhypot_ha_data_internal+128]
        vmulpd    xmm15, xmm14, xmm15
        vaddpd    xmm12, xmm12, xmm0
        vmulpd    xmm4, xmm14, xmm4
        vmulpd    xmm2, xmm12, XMMWORD PTR [__svml_dhypot_ha_data_internal+192]
        vaddpd    xmm3, xmm2, XMMWORD PTR [__svml_dhypot_ha_data_internal+256]
        vmulpd    xmm0, xmm12, xmm3
        vaddpd    xmm2, xmm0, XMMWORD PTR [__svml_dhypot_ha_data_internal+320]
        vmulpd    xmm3, xmm12, xmm2
        vaddpd    xmm0, xmm3, XMMWORD PTR [__svml_dhypot_ha_data_internal+384]
        vmulpd    xmm2, xmm12, xmm0
        vaddpd    xmm3, xmm2, XMMWORD PTR [__svml_dhypot_ha_data_internal+448]
        vmulpd    xmm12, xmm12, xmm3
        vmulpd    xmm0, xmm14, xmm12
        vmulpd    xmm1, xmm1, xmm0
        vaddpd    xmm0, xmm1, xmm15
        mov       QWORD PTR [328+rsp], r13
        vaddpd    xmm0, xmm0, xmm4
        test      edx, edx
        jne       _B1_3

_B1_2::

        vmovups   xmm11, XMMWORD PTR [304+rsp]
        vmovups   xmm12, XMMWORD PTR [288+rsp]
        vmovups   xmm14, XMMWORD PTR [272+rsp]
        vmovups   xmm15, XMMWORD PTR [256+rsp]
        mov       r13, QWORD PTR [320+rsp]
        add       rsp, 344
        ret

_B1_3::

        vmovupd   XMMWORD PTR [r13], xmm11
        vmovupd   XMMWORD PTR [64+r13], xmm5
        vmovupd   XMMWORD PTR [128+r13], xmm0

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
        cmp       ebx, 2
        jl        _B1_7

_B1_9::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        vmovupd   xmm0, XMMWORD PTR [128+r13]
        jmp       _B1_2

_B1_10::

        lea       rcx, QWORD PTR [r13+rbx*8]
        lea       rdx, QWORD PTR [64+r13+rbx*8]
        lea       r8, QWORD PTR [128+r13+rbx*8]

        call      __svml_dhypot_ha_cout_rare_internal
        jmp       _B1_8
        ALIGN     16

_B1_11::

__svml_hypot2_ha_e9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_hypot2_ha_e9_B1_B3:
	DD	800513
	DD	2675767
	DD	1292335
	DD	1230886
	DD	1173533
	DD	1112084
	DD	2818315

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B1_1
	DD	imagerel _B1_6
	DD	imagerel _unwind___svml_hypot2_ha_e9_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_hypot2_ha_e9_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B1_1
	DD	imagerel _B1_6
	DD	imagerel _unwind___svml_hypot2_ha_e9_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B1_6
	DD	imagerel _B1_11
	DD	imagerel _unwind___svml_hypot2_ha_e9_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST1:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_hypot4_ha_e9

__svml_hypot4_ha_e9	PROC

_B2_1::

        DB        243
        DB        15
        DB        30
        DB        250
L16::

        sub       rsp, 360
        vmovups   XMMWORD PTR [288+rsp], xmm15
        vmovups   XMMWORD PTR [320+rsp], xmm13
        vmovups   XMMWORD PTR [304+rsp], xmm12
        vmovups   XMMWORD PTR [272+rsp], xmm6
        mov       QWORD PTR [336+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovupd   ymm13, YMMWORD PTR [rcx]
        and       r13, -64
        vmovupd   ymm12, YMMWORD PTR [rdx]
        vmovupd   ymm4, YMMWORD PTR [__svml_dhypot_ha_data_internal+64]
        vmovupd   ymm0, YMMWORD PTR [__svml_dhypot_ha_data_internal]
        vandpd    ymm6, ymm13, ymm4
        vandpd    ymm1, ymm12, ymm4
        vmaxpd    ymm4, ymm6, ymm1
        vminpd    ymm2, ymm1, ymm6
        vandpd    ymm3, ymm4, ymm0
        vsubpd    ymm5, ymm4, ymm3
        mov       QWORD PTR [344+rsp], r13
        vextractf128 xmm6, ymm4, 1
        vextractf128 xmm1, ymm2, 1
        vshufps   xmm6, xmm4, xmm6, 221
        vshufps   xmm15, xmm2, xmm1, 221
        vpsubd    xmm1, xmm6, xmm15
        vpcmpgtd  xmm15, xmm1, XMMWORD PTR [__svml_dhypot_ha_data_internal+512]
        vpshufd   xmm6, xmm15, 80
        vpshufd   xmm1, xmm15, 250
        vinsertf128 ymm6, ymm6, xmm1, 1
        vandnpd   ymm0, ymm6, ymm0
        vandpd    ymm0, ymm2, ymm0
        vsubpd    ymm15, ymm2, ymm0
        vmulpd    ymm1, ymm3, ymm3
        vmulpd    ymm6, ymm0, ymm0
        vmulpd    ymm2, ymm2, ymm15
        vaddpd    ymm3, ymm4, ymm3
        vaddpd    ymm6, ymm1, ymm6
        vmulpd    ymm5, ymm5, ymm3
        vmulpd    ymm3, ymm0, ymm15
        vmovups   xmm0, XMMWORD PTR [__svml_dhypot_ha_data_internal+576]
        vaddpd    ymm2, ymm5, ymm2
        vaddpd    ymm3, ymm2, ymm3
        vaddpd    ymm5, ymm6, ymm3
        vextractf128 xmm15, ymm5, 1
        vshufps   xmm1, xmm5, xmm15, 221
        vpcmpgtd  xmm2, xmm1, XMMWORD PTR [__svml_dhypot_ha_data_internal+640]
        vpcmpgtd  xmm4, xmm0, xmm1
        vpor      xmm15, xmm4, xmm2
        vcvtpd2ps xmm2, ymm5
        vpshufd   xmm0, xmm15, 80
        vpshufd   xmm1, xmm15, 250
        vshufps   xmm4, xmm0, xmm1, 221
        vrsqrtps  xmm15, xmm2
        vmovmskps edx, xmm4
        vcvtps2pd ymm2, xmm15
        vmulpd    ymm1, ymm2, ymm2
        vmulpd    ymm0, ymm6, ymm1
        vmulpd    ymm15, ymm3, ymm1
        vmulpd    ymm3, ymm2, ymm3
        vmulpd    ymm6, ymm2, ymm6
        vsubpd    ymm4, ymm0, YMMWORD PTR [__svml_dhypot_ha_data_internal+128]
        vaddpd    ymm15, ymm4, ymm15
        vmulpd    ymm0, ymm15, YMMWORD PTR [__svml_dhypot_ha_data_internal+192]
        vaddpd    ymm1, ymm0, YMMWORD PTR [__svml_dhypot_ha_data_internal+256]
        vmulpd    ymm4, ymm15, ymm1
        vaddpd    ymm0, ymm4, YMMWORD PTR [__svml_dhypot_ha_data_internal+320]
        vmulpd    ymm1, ymm15, ymm0
        vaddpd    ymm4, ymm1, YMMWORD PTR [__svml_dhypot_ha_data_internal+384]
        vmulpd    ymm0, ymm15, ymm4
        vaddpd    ymm1, ymm0, YMMWORD PTR [__svml_dhypot_ha_data_internal+448]
        vmulpd    ymm15, ymm15, ymm1
        vmulpd    ymm0, ymm2, ymm15
        vmulpd    ymm5, ymm5, ymm0
        vaddpd    ymm0, ymm5, ymm3
        vaddpd    ymm0, ymm0, ymm6
        test      edx, edx
        jne       _B2_3

_B2_2::

        vmovups   xmm6, XMMWORD PTR [272+rsp]
        vmovups   xmm12, XMMWORD PTR [304+rsp]
        vmovups   xmm13, XMMWORD PTR [320+rsp]
        vmovups   xmm15, XMMWORD PTR [288+rsp]
        mov       r13, QWORD PTR [336+rsp]
        add       rsp, 360
        ret

_B2_3::

        vmovupd   YMMWORD PTR [r13], ymm13
        vmovupd   YMMWORD PTR [64+r13], ymm12
        vmovupd   YMMWORD PTR [128+r13], ymm0

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
        cmp       ebx, 4
        jl        _B2_7

_B2_9::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        vmovupd   ymm0, YMMWORD PTR [128+r13]
        jmp       _B2_2

_B2_10::

        vzeroupper
        lea       rcx, QWORD PTR [r13+rbx*8]
        lea       rdx, QWORD PTR [64+r13+rbx*8]
        lea       r8, QWORD PTR [128+r13+rbx*8]

        call      __svml_dhypot_ha_cout_rare_internal
        jmp       _B2_8
        ALIGN     16

_B2_11::

__svml_hypot4_ha_e9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_hypot4_ha_e9_B1_B3:
	DD	800513
	DD	2806839
	DD	1140783
	DD	1296422
	DD	1366045
	DD	1243156
	DD	2949387

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B2_1
	DD	imagerel _B2_6
	DD	imagerel _unwind___svml_hypot4_ha_e9_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_hypot4_ha_e9_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B2_1
	DD	imagerel _B2_6
	DD	imagerel _unwind___svml_hypot4_ha_e9_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B2_6
	DD	imagerel _B2_11
	DD	imagerel _unwind___svml_hypot4_ha_e9_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST2:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_hypot1_ha_l9

__svml_hypot1_ha_l9	PROC

_B3_1::

        DB        243
        DB        15
        DB        30
        DB        250
L31::

        sub       rsp, 328
        mov       eax, 6291456
        vmovups   XMMWORD PTR [288+rsp], xmm15
        mov       r9d, 1141899264
        vmovups   XMMWORD PTR [304+rsp], xmm14
        mov       r8d, 1002438656
        vmovups   XMMWORD PTR [256+rsp], xmm9
        vmovups   XMMWORD PTR [240+rsp], xmm8
        vmovups   XMMWORD PTR [272+rsp], xmm7
        mov       QWORD PTR [232+rsp], r13
        lea       r13, QWORD PTR [95+rsp]
        vmovupd   xmm3, XMMWORD PTR [rcx]
        and       r13, -64
        vmovupd   xmm2, XMMWORD PTR [rdx]
        vmovsd    xmm1, QWORD PTR [__svml_dhypot_ha_data_internal+64]
        vandpd    xmm5, xmm3, xmm1
        vandpd    xmm1, xmm2, xmm1
        vmovddup  xmm15, xmm5
        vmovddup  xmm7, xmm1
        vmaxpd    xmm5, xmm15, xmm7
        vminpd    xmm15, xmm7, xmm15
        vpshufd   xmm8, xmm5, 85
        vpshufd   xmm1, xmm15, 85
        vpsubd    xmm7, xmm8, xmm1
        vmovd     xmm8, eax
        vmovsd    xmm0, QWORD PTR [__svml_dhypot_ha_data_internal]
        vpcmpgtd  xmm1, xmm7, xmm8
        vpshufd   xmm7, xmm1, 0
        vandpd    xmm9, xmm5, xmm0
        vandnpd   xmm0, xmm7, xmm0
        vsubsd    xmm14, xmm5, xmm9
        vmulsd    xmm7, xmm9, xmm9
        vaddsd    xmm9, xmm5, xmm9
        vandpd    xmm8, xmm15, xmm0
        vmulsd    xmm14, xmm9, xmm14
        vsubsd    xmm1, xmm15, xmm8
        vmovapd   xmm5, xmm1
        vmovapd   xmm0, xmm8
        vfmadd213sd xmm5, xmm15, xmm14
        vmovd     xmm14, r9d
        vfmadd213sd xmm0, xmm8, xmm7
        vmovsd    xmm4, QWORD PTR [__svml_dhypot_ha_data_internal+192]
        vfmadd213sd xmm1, xmm8, xmm5
        vmovd     xmm8, r8d
        mov       QWORD PTR [320+rsp], r13
        vaddsd    xmm7, xmm0, xmm1
        vpshufd   xmm9, xmm7, 85
        vpcmpgtd  xmm5, xmm9, xmm14
        vpcmpgtd  xmm15, xmm8, xmm9
        vcvtpd2ps xmm14, xmm7
        vpor      xmm8, xmm15, xmm5
        vbroadcastss xmm15, xmm14
        vmovapd   xmm14, xmm1
        vpshufd   xmm9, xmm8, 0
        vmovmskpd eax, xmm9
        vmovapd   xmm9, xmm0
        vrsqrtps  xmm5, xmm15
        vcvtps2pd xmm5, xmm5
        vmulsd    xmm8, xmm5, xmm5
        vfmsub213sd xmm9, xmm8, QWORD PTR [__svml_dhypot_ha_data_internal+128]
        vfmadd213sd xmm14, xmm8, xmm9
        vfmadd213sd xmm4, xmm14, QWORD PTR [__svml_dhypot_ha_data_internal+256]
        vfmadd213sd xmm4, xmm14, QWORD PTR [__svml_dhypot_ha_data_internal+320]
        vfmadd213sd xmm4, xmm14, QWORD PTR [__svml_dhypot_ha_data_internal+384]
        vfmadd213sd xmm4, xmm14, QWORD PTR [__svml_dhypot_ha_data_internal+448]
        vmulsd    xmm4, xmm4, xmm14
        vmulsd    xmm4, xmm4, xmm5
        vmulsd    xmm7, xmm4, xmm7
        vfmadd213sd xmm1, xmm5, xmm7
        vfmadd213sd xmm0, xmm5, xmm1
        and       eax, 1
        jne       _B3_3

_B3_2::

        vmovups   xmm7, XMMWORD PTR [272+rsp]
        vmovups   xmm8, XMMWORD PTR [240+rsp]
        vmovups   xmm9, XMMWORD PTR [256+rsp]
        vmovups   xmm14, XMMWORD PTR [304+rsp]
        vmovups   xmm15, XMMWORD PTR [288+rsp]
        mov       r13, QWORD PTR [232+rsp]
        add       rsp, 328
        ret

_B3_3::

        vmovsd    QWORD PTR [r13], xmm3
        vmovsd    QWORD PTR [64+r13], xmm2
        vmovsd    QWORD PTR [128+r13], xmm0
        jne       _B3_6

_B3_4::

        vmovsd    xmm0, QWORD PTR [128+r13]
        jmp       _B3_2

_B3_6::

        lea       rcx, QWORD PTR [r13]
        lea       rdx, QWORD PTR [64+r13]
        lea       r8, QWORD PTR [128+r13]

        call      __svml_dhypot_ha_cout_rare_internal
        jmp       _B3_4
        ALIGN     16

_B3_7::

__svml_hypot1_ha_l9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_hypot1_ha_l9_B1_B6:
	DD	938241
	DD	1954897
	DD	1144905
	DD	1017920
	DD	1087543
	DD	1304616
	DD	1243161
	DD	2687243

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B3_1
	DD	imagerel _B3_7
	DD	imagerel _unwind___svml_hypot1_ha_l9_B1_B6

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST3:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_hypot2_ha_l9

__svml_hypot2_ha_l9	PROC

_B4_1::

        DB        243
        DB        15
        DB        30
        DB        250
L44::

        sub       rsp, 376
        vmovups   XMMWORD PTR [320+rsp], xmm15
        vmovups   XMMWORD PTR [256+rsp], xmm14
        vmovups   XMMWORD PTR [272+rsp], xmm13
        vmovups   XMMWORD PTR [304+rsp], xmm12
        vmovups   XMMWORD PTR [336+rsp], xmm7
        vmovups   XMMWORD PTR [288+rsp], xmm6
        mov       QWORD PTR [352+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovupd   xmm12, XMMWORD PTR [rcx]
        and       r13, -64
        vmovupd   xmm7, XMMWORD PTR [rdx]
        vmovupd   xmm0, XMMWORD PTR [__svml_dhypot_ha_data_internal+64]
        vandpd    xmm4, xmm12, xmm0
        vandpd    xmm2, xmm7, xmm0
        vmaxpd    xmm1, xmm4, xmm2
        vminpd    xmm2, xmm2, xmm4
        vmovupd   xmm15, XMMWORD PTR [__svml_dhypot_ha_data_internal]
        vandpd    xmm3, xmm1, xmm15
        vsubpd    xmm4, xmm1, xmm3
        vpshufd   xmm13, xmm1, 221
        vaddpd    xmm1, xmm1, xmm3
        vpshufd   xmm14, xmm2, 221
        vmovq     xmm0, QWORD PTR [__svml_dhypot_ha_data_internal+512]
        vpsubd    xmm13, xmm13, xmm14
        vpcmpgtd  xmm0, xmm13, xmm0
        vpshufd   xmm0, xmm0, 80
        vandnpd   xmm15, xmm0, xmm15
        vmulpd    xmm0, xmm3, xmm3
        vmulpd    xmm3, xmm4, xmm1
        vandpd    xmm14, xmm2, xmm15
        vsubpd    xmm15, xmm2, xmm14
        vfmadd231pd xmm0, xmm14, xmm14
        vmovq     xmm1, QWORD PTR [__svml_dhypot_ha_data_internal+576]
        vfmadd231pd xmm3, xmm2, xmm15
        vmovupd   xmm6, XMMWORD PTR [__svml_dhypot_ha_data_internal+128]
        vmovupd   xmm5, XMMWORD PTR [__svml_dhypot_ha_data_internal+192]
        vfmadd213pd xmm15, xmm14, xmm3
        vmovq     xmm3, QWORD PTR [__svml_dhypot_ha_data_internal+640]
        vaddpd    xmm13, xmm0, xmm15
        vpshufd   xmm2, xmm13, 221
        vpcmpgtd  xmm14, xmm2, xmm3
        vpcmpgtd  xmm4, xmm1, xmm2
        vcvtpd2ps xmm3, xmm13
        vpor      xmm1, xmm4, xmm14
        vmovlhps  xmm4, xmm3, xmm3
        vrsqrtps  xmm14, xmm4
        vpshufd   xmm2, xmm1, 80
        vmovmskpd edx, xmm2
        vcvtps2pd xmm2, xmm14
        vmulpd    xmm1, xmm2, xmm2
        vfmsub231pd xmm6, xmm1, xmm0
        vfmadd231pd xmm6, xmm1, xmm15
        vfmadd213pd xmm5, xmm6, XMMWORD PTR [__svml_dhypot_ha_data_internal+256]
        vfmadd213pd xmm5, xmm6, XMMWORD PTR [__svml_dhypot_ha_data_internal+320]
        vfmadd213pd xmm5, xmm6, XMMWORD PTR [__svml_dhypot_ha_data_internal+384]
        vfmadd213pd xmm5, xmm6, XMMWORD PTR [__svml_dhypot_ha_data_internal+448]
        vmulpd    xmm5, xmm6, xmm5
        vmulpd    xmm1, xmm2, xmm5
        vmulpd    xmm5, xmm13, xmm1
        vfmadd213pd xmm15, xmm2, xmm5
        mov       QWORD PTR [360+rsp], r13
        vfmadd213pd xmm0, xmm2, xmm15
        test      edx, edx
        jne       _B4_3

_B4_2::

        vmovups   xmm6, XMMWORD PTR [288+rsp]
        vmovups   xmm7, XMMWORD PTR [336+rsp]
        vmovups   xmm12, XMMWORD PTR [304+rsp]
        vmovups   xmm13, XMMWORD PTR [272+rsp]
        vmovups   xmm14, XMMWORD PTR [256+rsp]
        vmovups   xmm15, XMMWORD PTR [320+rsp]
        mov       r13, QWORD PTR [352+rsp]
        add       rsp, 376
        ret

_B4_3::

        vmovupd   XMMWORD PTR [r13], xmm12
        vmovupd   XMMWORD PTR [64+r13], xmm7
        vmovupd   XMMWORD PTR [128+r13], xmm0

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
        cmp       ebx, 2
        jl        _B4_7

_B4_9::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        vmovupd   xmm0, XMMWORD PTR [128+r13]
        jmp       _B4_2

_B4_10::

        lea       rcx, QWORD PTR [r13+rbx*8]
        lea       rdx, QWORD PTR [64+r13+rbx*8]
        lea       r8, QWORD PTR [128+r13+rbx*8]

        call      __svml_dhypot_ha_cout_rare_internal
        jmp       _B4_8
        ALIGN     16

_B4_11::

__svml_hypot2_ha_l9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_hypot2_ha_l9_B1_B3:
	DD	1067265
	DD	2937929
	DD	1206337
	DD	1407032
	DD	1296431
	DD	1169446
	DD	1107997
	DD	1374228
	DD	3080459

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B4_1
	DD	imagerel _B4_6
	DD	imagerel _unwind___svml_hypot2_ha_l9_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_hypot2_ha_l9_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B4_1
	DD	imagerel _B4_6
	DD	imagerel _unwind___svml_hypot2_ha_l9_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B4_6
	DD	imagerel _B4_11
	DD	imagerel _unwind___svml_hypot2_ha_l9_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST4:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_hypot4_ha_l9

__svml_hypot4_ha_l9	PROC

_B5_1::

        DB        243
        DB        15
        DB        30
        DB        250
L63::

        sub       rsp, 376
        vmovups   XMMWORD PTR [272+rsp], xmm15
        vmovups   XMMWORD PTR [288+rsp], xmm13
        vmovups   XMMWORD PTR [304+rsp], xmm12
        vmovups   XMMWORD PTR [320+rsp], xmm7
        vmovups   XMMWORD PTR [336+rsp], xmm6
        mov       QWORD PTR [352+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovupd   ymm7, YMMWORD PTR [rcx]
        and       r13, -64
        vmovupd   ymm6, YMMWORD PTR [rdx]
        vmovupd   ymm4, YMMWORD PTR [__svml_dhypot_ha_data_internal+64]
        vmovupd   ymm0, YMMWORD PTR [__svml_dhypot_ha_data_internal]
        vmovupd   ymm12, YMMWORD PTR [__svml_dhypot_ha_data_internal+128]
        vmovupd   ymm13, YMMWORD PTR [__svml_dhypot_ha_data_internal+192]
        vandpd    ymm5, ymm7, ymm4
        vandpd    ymm2, ymm6, ymm4
        vmaxpd    ymm3, ymm5, ymm2
        vminpd    ymm1, ymm2, ymm5
        vandpd    ymm2, ymm3, ymm0
        vsubpd    ymm4, ymm3, ymm2
        mov       QWORD PTR [360+rsp], r13
        vextracti128 xmm5, ymm3, 1
        vextracti128 xmm15, ymm1, 1
        vshufps   xmm5, xmm3, xmm5, 221
        vshufps   xmm15, xmm1, xmm15, 221
        vpsubd    xmm5, xmm5, xmm15
        vpcmpgtd  xmm15, xmm5, XMMWORD PTR [__svml_dhypot_ha_data_internal+512]
        vpshufd   xmm5, xmm15, 80
        vpshufd   xmm15, xmm15, 250
        vinserti128 ymm5, ymm5, xmm15, 1
        vandnpd   ymm0, ymm5, ymm0
        vandpd    ymm15, ymm1, ymm0
        vmulpd    ymm0, ymm2, ymm2
        vaddpd    ymm2, ymm3, ymm2
        vsubpd    ymm5, ymm1, ymm15
        vfmadd231pd ymm0, ymm15, ymm15
        vmulpd    ymm4, ymm4, ymm2
        vfmadd231pd ymm4, ymm1, ymm5
        vfmadd213pd ymm5, ymm15, ymm4
        vmovups   xmm15, XMMWORD PTR [__svml_dhypot_ha_data_internal+576]
        vaddpd    ymm1, ymm0, ymm5
        vextracti128 xmm3, ymm1, 1
        vshufps   xmm2, xmm1, xmm3, 221
        vpcmpgtd  xmm4, xmm2, XMMWORD PTR [__svml_dhypot_ha_data_internal+640]
        vpcmpgtd  xmm3, xmm15, xmm2
        vpor      xmm2, xmm3, xmm4
        vpshufd   xmm15, xmm2, 80
        vpshufd   xmm3, xmm2, 250
        vinserti128 ymm4, ymm15, xmm3, 1
        vcvtpd2ps xmm15, ymm1
        vrsqrtps  xmm2, xmm15
        vcvtps2pd ymm15, xmm2
        vmulpd    ymm3, ymm15, ymm15
        vfmsub231pd ymm12, ymm3, ymm0
        vfmadd231pd ymm12, ymm3, ymm5
        vfmadd213pd ymm13, ymm12, YMMWORD PTR [__svml_dhypot_ha_data_internal+256]
        vfmadd213pd ymm13, ymm12, YMMWORD PTR [__svml_dhypot_ha_data_internal+320]
        vfmadd213pd ymm13, ymm12, YMMWORD PTR [__svml_dhypot_ha_data_internal+384]
        vfmadd213pd ymm13, ymm12, YMMWORD PTR [__svml_dhypot_ha_data_internal+448]
        vmulpd    ymm12, ymm12, ymm13
        vmulpd    ymm13, ymm15, ymm12
        vmulpd    ymm1, ymm1, ymm13
        vfmadd213pd ymm5, ymm15, ymm1
        vmovmskpd edx, ymm4
        vfmadd213pd ymm0, ymm15, ymm5
        test      edx, edx
        jne       _B5_3

_B5_2::

        vmovups   xmm6, XMMWORD PTR [336+rsp]
        vmovups   xmm7, XMMWORD PTR [320+rsp]
        vmovups   xmm12, XMMWORD PTR [304+rsp]
        vmovups   xmm13, XMMWORD PTR [288+rsp]
        vmovups   xmm15, XMMWORD PTR [272+rsp]
        mov       r13, QWORD PTR [352+rsp]
        add       rsp, 376
        ret

_B5_3::

        vmovupd   YMMWORD PTR [r13], ymm7
        vmovupd   YMMWORD PTR [64+r13], ymm6
        vmovupd   YMMWORD PTR [128+r13], ymm0

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
        cmp       ebx, 4
        jl        _B5_7

_B5_9::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        vmovupd   ymm0, YMMWORD PTR [128+r13]
        jmp       _B5_2

_B5_10::

        vzeroupper
        lea       rcx, QWORD PTR [r13+rbx*8]
        lea       rdx, QWORD PTR [64+r13+rbx*8]
        lea       r8, QWORD PTR [128+r13+rbx*8]

        call      __svml_dhypot_ha_cout_rare_internal
        jmp       _B5_8
        ALIGN     16

_B5_11::

__svml_hypot4_ha_l9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_hypot4_ha_l9_B1_B3:
	DD	933889
	DD	2937920
	DD	1402936
	DD	1341487
	DD	1296422
	DD	1234973
	DD	1177620
	DD	3080459

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B5_1
	DD	imagerel _B5_6
	DD	imagerel _unwind___svml_hypot4_ha_l9_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_hypot4_ha_l9_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B5_1
	DD	imagerel _B5_6
	DD	imagerel _unwind___svml_hypot4_ha_l9_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B5_6
	DD	imagerel _B5_11
	DD	imagerel _unwind___svml_hypot4_ha_l9_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST5:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_hypot2_ha_ex

__svml_hypot2_ha_ex	PROC

_B6_1::

        DB        243
        DB        15
        DB        30
        DB        250
L80::

        sub       rsp, 360
        movups    XMMWORD PTR [272+rsp], xmm15
        movups    XMMWORD PTR [288+rsp], xmm14
        movups    XMMWORD PTR [320+rsp], xmm8
        movups    XMMWORD PTR [256+rsp], xmm7
        movups    XMMWORD PTR [304+rsp], xmm6
        mov       QWORD PTR [336+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        movups    xmm14, XMMWORD PTR [rcx]
        and       r13, -64
        movups    xmm5, XMMWORD PTR [__svml_dhypot_ha_data_internal+64]
        movaps    xmm4, xmm14
        movups    xmm8, XMMWORD PTR [rdx]
        andps     xmm4, xmm5
        andps     xmm5, xmm8
        movaps    xmm6, xmm4
        maxpd     xmm6, xmm5
        minpd     xmm5, xmm4
        pshufd    xmm15, xmm6, 221
        movaps    xmm4, xmm6
        pshufd    xmm0, xmm5, 221
        movq      xmm1, QWORD PTR [__svml_dhypot_ha_data_internal+512]
        psubd     xmm15, xmm0
        pcmpgtd   xmm15, xmm1
        movups    xmm3, XMMWORD PTR [__svml_dhypot_ha_data_internal]
        pshufd    xmm0, xmm15, 80
        movaps    xmm2, xmm3
        andnps    xmm0, xmm3
        andps     xmm2, xmm6
        andps     xmm0, xmm5
        movaps    xmm15, xmm5
        subpd     xmm4, xmm2
        subpd     xmm15, xmm0
        addpd     xmm6, xmm2
        mulpd     xmm5, xmm15
        mulpd     xmm4, xmm6
        movaps    xmm1, xmm2
        movaps    xmm3, xmm0
        mulpd     xmm1, xmm2
        mulpd     xmm3, xmm0
        mulpd     xmm0, xmm15
        addpd     xmm4, xmm5
        addpd     xmm1, xmm3
        addpd     xmm4, xmm0
        movaps    xmm0, xmm1
        addpd     xmm0, xmm4
        cvtpd2ps  xmm6, xmm0
        movq      xmm3, QWORD PTR [__svml_dhypot_ha_data_internal+576]
        movq      xmm2, QWORD PTR [__svml_dhypot_ha_data_internal+640]
        pshufd    xmm5, xmm0, 221
        movlhps   xmm6, xmm6
        pcmpgtd   xmm3, xmm5
        pcmpgtd   xmm5, xmm2
        por       xmm3, xmm5
        rsqrtps   xmm5, xmm6
        pshufd    xmm2, xmm3, 80
        cvtps2pd  xmm3, xmm5
        movmskpd  eax, xmm2
        movaps    xmm15, xmm3
        movaps    xmm2, xmm1
        mulpd     xmm15, xmm3
        mulpd     xmm2, xmm15
        mulpd     xmm15, xmm4
        subpd     xmm2, XMMWORD PTR [__svml_dhypot_ha_data_internal+128]
        mulpd     xmm4, xmm3
        addpd     xmm2, xmm15
        movups    xmm7, XMMWORD PTR [__svml_dhypot_ha_data_internal+192]
        mulpd     xmm7, xmm2
        addpd     xmm7, XMMWORD PTR [__svml_dhypot_ha_data_internal+256]
        mulpd     xmm7, xmm2
        addpd     xmm7, XMMWORD PTR [__svml_dhypot_ha_data_internal+320]
        mulpd     xmm7, xmm2
        addpd     xmm7, XMMWORD PTR [__svml_dhypot_ha_data_internal+384]
        mulpd     xmm7, xmm2
        addpd     xmm7, XMMWORD PTR [__svml_dhypot_ha_data_internal+448]
        mulpd     xmm2, xmm7
        mulpd     xmm2, xmm3
        mulpd     xmm3, xmm1
        mulpd     xmm0, xmm2
        addpd     xmm0, xmm4
        mov       QWORD PTR [344+rsp], r13
        addpd     xmm0, xmm3
        test      eax, eax
        jne       _B6_3

_B6_2::

        movups    xmm6, XMMWORD PTR [304+rsp]
        movups    xmm7, XMMWORD PTR [256+rsp]
        movups    xmm8, XMMWORD PTR [320+rsp]
        movups    xmm14, XMMWORD PTR [288+rsp]
        movups    xmm15, XMMWORD PTR [272+rsp]
        mov       r13, QWORD PTR [336+rsp]
        add       rsp, 360
        ret

_B6_3::

        movups    XMMWORD PTR [r13], xmm14
        movups    XMMWORD PTR [64+r13], xmm8
        movups    XMMWORD PTR [128+r13], xmm0

_B6_6::

        xor       ecx, ecx
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, ecx
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, eax

_B6_7::

        mov       ecx, ebx
        mov       edx, 1
        shl       edx, cl
        test      esi, edx
        jne       _B6_10

_B6_8::

        inc       ebx
        cmp       ebx, 2
        jl        _B6_7

_B6_9::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        movups    xmm0, XMMWORD PTR [128+r13]
        jmp       _B6_2

_B6_10::

        lea       rcx, QWORD PTR [r13+rbx*8]
        lea       rdx, QWORD PTR [64+r13+rbx*8]
        lea       r8, QWORD PTR [128+r13+rbx*8]

        call      __svml_dhypot_ha_cout_rare_internal
        jmp       _B6_8
        ALIGN     16

_B6_11::

__svml_hypot2_ha_ex ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_hypot2_ha_ex_B1_B3:
	DD	933377
	DD	2806846
	DD	1271862
	DD	1079342
	DD	1345574
	DD	1239069
	DD	1177620
	DD	2949387

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B6_1
	DD	imagerel _B6_6
	DD	imagerel _unwind___svml_hypot2_ha_ex_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_hypot2_ha_ex_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B6_1
	DD	imagerel _B6_6
	DD	imagerel _unwind___svml_hypot2_ha_ex_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B6_6
	DD	imagerel _B6_11
	DD	imagerel _unwind___svml_hypot2_ha_ex_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST6:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_hypot1_ha_ex

__svml_hypot1_ha_ex	PROC

_B7_1::

        DB        243
        DB        15
        DB        30
        DB        250
L97::

        sub       rsp, 328
        mov       eax, 6291456
        movups    XMMWORD PTR [288+rsp], xmm15
        mov       r8d, 1002438656
        movups    XMMWORD PTR [272+rsp], xmm11
        mov       r9d, 1141899264
        movups    XMMWORD PTR [304+rsp], xmm9
        movups    XMMWORD PTR [240+rsp], xmm8
        movups    XMMWORD PTR [256+rsp], xmm6
        movd      xmm6, eax
        mov       QWORD PTR [232+rsp], r13
        lea       r13, QWORD PTR [95+rsp]
        movups    xmm3, XMMWORD PTR [rcx]
        and       r13, -64
        movsd     xmm11, QWORD PTR [__svml_dhypot_ha_data_internal+64]
        movaps    xmm4, xmm3
        movups    xmm2, XMMWORD PTR [rdx]
        andps     xmm4, xmm11
        unpcklpd  xmm4, xmm4
        andps     xmm11, xmm2
        unpcklpd  xmm11, xmm11
        movaps    xmm15, xmm4
        maxpd     xmm15, xmm11
        minpd     xmm11, xmm4
        pshufd    xmm4, xmm15, 85
        movaps    xmm9, xmm15
        pshufd    xmm5, xmm11, 85
        psubd     xmm4, xmm5
        pcmpgtd   xmm4, xmm6
        movaps    xmm6, xmm11
        movsd     xmm0, QWORD PTR [__svml_dhypot_ha_data_internal]
        pshufd    xmm5, xmm4, 0
        movaps    xmm8, xmm0
        andnps    xmm5, xmm0
        andps     xmm8, xmm15
        andps     xmm5, xmm11
        movaps    xmm4, xmm8
        mulsd     xmm4, xmm8
        subsd     xmm6, xmm5
        subsd     xmm9, xmm8
        addsd     xmm15, xmm8
        movaps    xmm8, xmm6
        movaps    xmm0, xmm5
        mulsd     xmm15, xmm9
        mulsd     xmm8, xmm11
        mulsd     xmm0, xmm5
        mulsd     xmm6, xmm5
        addsd     xmm8, xmm15
        addsd     xmm0, xmm4
        addsd     xmm6, xmm8
        movaps    xmm4, xmm0
        movd      xmm11, r8d
        movd      xmm5, r9d
        addsd     xmm4, xmm6
        pshufd    xmm9, xmm4, 85
        pcmpgtd   xmm11, xmm9
        pcmpgtd   xmm9, xmm5
        cvtpd2ps  xmm5, xmm4
        shufps    xmm5, xmm5, 0
        por       xmm11, xmm9
        rsqrtps   xmm8, xmm5
        pshufd    xmm15, xmm11, 0
        movaps    xmm11, xmm0
        movmskpd  eax, xmm15
        movaps    xmm5, xmm6
        movsd     xmm1, QWORD PTR [__svml_dhypot_ha_data_internal+192]
        cvtps2pd  xmm15, xmm8
        movaps    xmm9, xmm15
        and       eax, 1
        mulsd     xmm9, xmm15
        mulsd     xmm6, xmm15
        mulsd     xmm0, xmm15
        mulsd     xmm11, xmm9
        mulsd     xmm5, xmm9
        subsd     xmm11, QWORD PTR [__svml_dhypot_ha_data_internal+128]
        mov       QWORD PTR [320+rsp], r13
        addsd     xmm5, xmm11
        mulsd     xmm1, xmm5
        addsd     xmm1, QWORD PTR [__svml_dhypot_ha_data_internal+256]
        mulsd     xmm1, xmm5
        addsd     xmm1, QWORD PTR [__svml_dhypot_ha_data_internal+320]
        mulsd     xmm1, xmm5
        addsd     xmm1, QWORD PTR [__svml_dhypot_ha_data_internal+384]
        mulsd     xmm1, xmm5
        addsd     xmm1, QWORD PTR [__svml_dhypot_ha_data_internal+448]
        mulsd     xmm1, xmm5
        mulsd     xmm1, xmm15
        mulsd     xmm1, xmm4
        addsd     xmm6, xmm1
        addsd     xmm0, xmm6
        jne       _B7_3

_B7_2::

        movups    xmm6, XMMWORD PTR [256+rsp]
        movups    xmm8, XMMWORD PTR [240+rsp]
        movups    xmm9, XMMWORD PTR [304+rsp]
        movups    xmm11, XMMWORD PTR [272+rsp]
        movups    xmm15, XMMWORD PTR [288+rsp]
        mov       r13, QWORD PTR [232+rsp]
        add       rsp, 328
        ret

_B7_3::

        movsd     QWORD PTR [r13], xmm3
        movsd     QWORD PTR [64+r13], xmm2
        movsd     QWORD PTR [128+r13], xmm0
        jne       _B7_6

_B7_4::

        movsd     xmm0, QWORD PTR [128+r13]
        jmp       _B7_2

_B7_6::

        lea       rcx, QWORD PTR [r13]
        lea       rdx, QWORD PTR [64+r13]
        lea       r8, QWORD PTR [128+r13]

        call      __svml_dhypot_ha_cout_rare_internal
        jmp       _B7_4
        ALIGN     16

_B7_7::

__svml_hypot1_ha_ex ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_hypot1_ha_ex_B1_B6:
	DD	939009
	DD	1954900
	DD	1075272
	DD	1017920
	DD	1284151
	DD	1161256
	DD	1243161
	DD	2687243

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B7_1
	DD	imagerel _B7_7
	DD	imagerel _unwind___svml_hypot1_ha_ex_B1_B6

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST7:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_hypot1_ha_e9

__svml_hypot1_ha_e9	PROC

_B8_1::

        DB        243
        DB        15
        DB        30
        DB        250
L110::

        sub       rsp, 328
        mov       eax, 6291456
        vmovups   XMMWORD PTR [240+rsp], xmm15
        mov       r9d, 1141899264
        vmovups   XMMWORD PTR [256+rsp], xmm14
        mov       r8d, 1002438656
        vmovups   XMMWORD PTR [272+rsp], xmm13
        vmovups   XMMWORD PTR [288+rsp], xmm11
        vmovups   XMMWORD PTR [304+rsp], xmm10
        mov       QWORD PTR [232+rsp], r13
        lea       r13, QWORD PTR [95+rsp]
        vmovupd   xmm2, XMMWORD PTR [rcx]
        and       r13, -64
        vmovupd   xmm1, XMMWORD PTR [rdx]
        vmovsd    xmm4, QWORD PTR [__svml_dhypot_ha_data_internal+64]
        vandpd    xmm5, xmm2, xmm4
        vandpd    xmm11, xmm1, xmm4
        vmovddup  xmm13, xmm5
        vmovddup  xmm4, xmm11
        vmaxpd    xmm10, xmm13, xmm4
        vminpd    xmm15, xmm4, xmm13
        vpshufd   xmm14, xmm10, 85
        vpshufd   xmm4, xmm15, 85
        vpsubd    xmm13, xmm14, xmm4
        vmovd     xmm14, eax
        vpcmpgtd  xmm4, xmm13, xmm14
        vmovsd    xmm3, QWORD PTR [__svml_dhypot_ha_data_internal]
        vpshufd   xmm13, xmm4, 0
        vandpd    xmm11, xmm10, xmm3
        vandnpd   xmm3, xmm13, xmm3
        vsubsd    xmm5, xmm10, xmm11
        vmulsd    xmm4, xmm11, xmm11
        vaddsd    xmm10, xmm10, xmm11
        vandpd    xmm13, xmm15, xmm3
        vmulsd    xmm5, xmm10, xmm5
        vsubsd    xmm14, xmm15, xmm13
        vmulsd    xmm3, xmm13, xmm13
        vmulsd    xmm15, xmm14, xmm15
        vmulsd    xmm11, xmm14, xmm13
        vaddsd    xmm3, xmm3, xmm4
        vaddsd    xmm10, xmm15, xmm5
        vmovd     xmm14, r9d
        vmovd     xmm4, r8d
        vmovsd    xmm0, QWORD PTR [__svml_dhypot_ha_data_internal+192]
        vaddsd    xmm11, xmm11, xmm10
        mov       QWORD PTR [320+rsp], r13
        vaddsd    xmm10, xmm3, xmm11
        vpshufd   xmm13, xmm10, 85
        vpcmpgtd  xmm5, xmm13, xmm14
        vpcmpgtd  xmm15, xmm4, xmm13
        vcvtpd2ps xmm14, xmm10
        vpor      xmm4, xmm15, xmm5
        vshufps   xmm15, xmm14, xmm14, 0
        vrsqrtps  xmm5, xmm15
        vcvtps2pd xmm5, xmm5
        vpshufd   xmm13, xmm4, 0
        vmovmskpd eax, xmm13
        vmulsd    xmm13, xmm5, xmm5
        vmulsd    xmm4, xmm3, xmm13
        vmulsd    xmm14, xmm11, xmm13
        vmulsd    xmm11, xmm11, xmm5
        vsubsd    xmm15, xmm4, QWORD PTR [__svml_dhypot_ha_data_internal+128]
        vmulsd    xmm3, xmm3, xmm5
        vaddsd    xmm4, xmm14, xmm15
        vmulsd    xmm0, xmm0, xmm4
        vaddsd    xmm0, xmm0, QWORD PTR [__svml_dhypot_ha_data_internal+256]
        vmulsd    xmm13, xmm0, xmm4
        vaddsd    xmm14, xmm13, QWORD PTR [__svml_dhypot_ha_data_internal+320]
        vmulsd    xmm15, xmm14, xmm4
        vaddsd    xmm0, xmm15, QWORD PTR [__svml_dhypot_ha_data_internal+384]
        vmulsd    xmm13, xmm0, xmm4
        vaddsd    xmm14, xmm13, QWORD PTR [__svml_dhypot_ha_data_internal+448]
        vmulsd    xmm4, xmm14, xmm4
        vmulsd    xmm0, xmm4, xmm5
        vmulsd    xmm10, xmm0, xmm10
        vaddsd    xmm0, xmm11, xmm10
        vaddsd    xmm0, xmm3, xmm0
        and       eax, 1
        jne       _B8_3

_B8_2::

        vmovups   xmm10, XMMWORD PTR [304+rsp]
        vmovups   xmm11, XMMWORD PTR [288+rsp]
        vmovups   xmm13, XMMWORD PTR [272+rsp]
        vmovups   xmm14, XMMWORD PTR [256+rsp]
        vmovups   xmm15, XMMWORD PTR [240+rsp]
        mov       r13, QWORD PTR [232+rsp]
        add       rsp, 328
        ret

_B8_3::

        vmovsd    QWORD PTR [r13], xmm2
        vmovsd    QWORD PTR [64+r13], xmm1
        vmovsd    QWORD PTR [128+r13], xmm0
        jne       _B8_6

_B8_4::

        vmovsd    xmm0, QWORD PTR [128+r13]
        jmp       _B8_2

_B8_6::

        lea       rcx, QWORD PTR [r13]
        lea       rdx, QWORD PTR [64+r13]
        lea       r8, QWORD PTR [128+r13]

        call      __svml_dhypot_ha_cout_rare_internal
        jmp       _B8_4
        ALIGN     16

_B8_7::

__svml_hypot1_ha_e9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_hypot1_ha_e9_B1_B6:
	DD	938241
	DD	1954897
	DD	1288265
	DD	1226816
	DD	1169463
	DD	1108008
	DD	1046553
	DD	2687243

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B8_1
	DD	imagerel _B8_7
	DD	imagerel _unwind___svml_hypot1_ha_e9_B1_B6

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST8:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_dhypot_ha_cout_rare_internal

__svml_dhypot_ha_cout_rare_internal	PROC

_B9_1::

        DB        243
        DB        15
        DB        30
        DB        250
L123::

        push      r12
        sub       rsp, 128
        lea       r10, QWORD PTR [__ImageBase]
        movzx     eax, WORD PTR [6+rcx]
        and       eax, 32752
        movups    XMMWORD PTR [32+rsp], xmm15
        movups    XMMWORD PTR [48+rsp], xmm14
        cmp       eax, 32752
        je        _B9_14

_B9_2::

        movzx     eax, WORD PTR [6+rdx]
        and       eax, 32752
        cmp       eax, 32752
        je        _B9_13

_B9_3::

        movsd     xmm1, QWORD PTR [rcx]
        movsd     xmm4, QWORD PTR [_vmldHypotHATab+4096]
        mov       r9b, BYTE PTR [7+rcx]
        mov       al, BYTE PTR [7+rdx]
        movsd     xmm0, QWORD PTR [rdx]
        ucomisd   xmm1, xmm4
        jp        _B9_4
        je        _B9_11

_B9_4::

        movsd     QWORD PTR [120+rsp], xmm1
        and       r9b, 127
        movsd     QWORD PTR [88+rsp], xmm0
        and       al, 127
        mov       BYTE PTR [127+rsp], r9b
        mov       BYTE PTR [95+rsp], al
        movsd     xmm0, QWORD PTR [120+rsp]
        movsd     xmm3, QWORD PTR [88+rsp]
        comisd    xmm3, xmm0
        jbe       _B9_6

_B9_5::

        movaps    xmm1, xmm0
        movaps    xmm0, xmm3
        movsd     QWORD PTR [120+rsp], xmm3
        movaps    xmm3, xmm1

_B9_6::

        movzx     edx, WORD PTR [126+rsp]
        and       edx, 32752
        shr       edx, 4
        neg       edx
        movzx     r11d, WORD PTR [_vmldHypotHATab+4102]
        and       r11d, -32753
        movsd     QWORD PTR [80+rsp], xmm4
        movsd     xmm2, QWORD PTR [_vmldHypotHATab+4128]
        lea       r9d, DWORD PTR [1025+rdx]
        neg       r9d
        add       r9d, 1000
        shr       r9d, 31
        imul      eax, r9d, -23
        lea       r9d, DWORD PTR [1025+rax+rdx]
        lea       ecx, DWORD PTR [1023+r9]
        and       ecx, 2047
        shl       ecx, 4
        or        r11d, ecx
        mov       WORD PTR [86+rsp], r11w
        movsd     xmm1, QWORD PTR [80+rsp]
        mulsd     xmm0, xmm1
        mulsd     xmm3, xmm1
        mulsd     xmm2, xmm0
        movsd     QWORD PTR [64+rsp], xmm2
        movsd     xmm5, QWORD PTR [64+rsp]
        movsd     QWORD PTR [120+rsp], xmm0
        subsd     xmm5, xmm0
        movsd     QWORD PTR [72+rsp], xmm5
        movsd     xmm15, QWORD PTR [64+rsp]
        movsd     xmm14, QWORD PTR [72+rsp]
        movsd     QWORD PTR [88+rsp], xmm3
        subsd     xmm15, xmm14
        movsd     QWORD PTR [64+rsp], xmm15
        movsd     xmm1, QWORD PTR [64+rsp]
        movzx     eax, WORD PTR [126+rsp]
        subsd     xmm0, xmm1
        movzx     r12d, WORD PTR [94+rsp]
        and       eax, 32752
        and       r12d, 32752
        shr       eax, 4
        shr       r12d, 4
        movsd     QWORD PTR [72+rsp], xmm0
        sub       eax, r12d
        movsd     xmm2, QWORD PTR [64+rsp]
        movsd     xmm0, QWORD PTR [72+rsp]
        cmp       eax, 6
        jle       _B9_8

_B9_7::

        movaps    xmm14, xmm3
        jmp       _B9_9

_B9_8::

        movsd     xmm3, QWORD PTR [88+rsp]
        movsd     xmm1, QWORD PTR [_vmldHypotHATab+4128]
        mulsd     xmm1, xmm3
        movsd     QWORD PTR [64+rsp], xmm1
        movaps    xmm1, xmm3
        movsd     xmm4, QWORD PTR [64+rsp]
        subsd     xmm4, QWORD PTR [88+rsp]
        movsd     QWORD PTR [72+rsp], xmm4
        movsd     xmm14, QWORD PTR [64+rsp]
        movsd     xmm5, QWORD PTR [72+rsp]
        subsd     xmm14, xmm5
        movsd     QWORD PTR [64+rsp], xmm14
        movsd     xmm15, QWORD PTR [64+rsp]
        subsd     xmm1, xmm15
        movsd     QWORD PTR [72+rsp], xmm1
        movsd     xmm4, QWORD PTR [64+rsp]
        movsd     xmm14, QWORD PTR [72+rsp]

_B9_9::

        movsd     xmm5, QWORD PTR [120+rsp]
        movaps    xmm1, xmm2
        mulsd     xmm3, xmm14
        addsd     xmm5, xmm2
        mulsd     xmm1, xmm2
        mulsd     xmm0, xmm5
        movaps    xmm15, xmm4
        neg       r9d
        mulsd     xmm15, xmm4
        addsd     xmm0, xmm3
        mulsd     xmm4, xmm14
        addsd     xmm1, xmm15
        addsd     xmm0, xmm4
        movaps    xmm14, xmm1
        add       r9d, 1023
        mov       rax, QWORD PTR [_vmldHypotHATab+4112]
        mov       r12, rax
        shr       r12, 48
        and       r9d, 2047
        shl       r9d, 4
        addsd     xmm14, xmm0
        movsd     QWORD PTR [80+rsp], xmm14
        and       r12d, -32753
        movzx     ecx, WORD PTR [86+rsp]
        and       ecx, 32752
        shr       ecx, 4
        add       ecx, -1023
        mov       edx, ecx
        and       edx, 1
        sub       ecx, edx
        shr       ecx, 1
        movsd     QWORD PTR [88+rsp], xmm14
        movzx     r11d, WORD PTR [94+rsp]
        and       r11d, -32753
        add       r11d, 16368
        mov       WORD PTR [94+rsp], r11w
        lea       r11d, DWORD PTR [1023+rcx]
        add       ecx, ecx
        and       r11d, 2047
        neg       ecx
        add       ecx, 1023
        and       ecx, 2047
        shl       r11d, 4
        shl       ecx, 4
        or        r11d, r12d
        movsd     xmm5, QWORD PTR [88+rsp]
        or        ecx, r12d
        or        r12d, r9d
        mov       r9d, DWORD PTR [92+rsp]
        mulsd     xmm5, QWORD PTR [imagerel(_vmldHypotHATab)+4112+r10+rdx*8]
        and       r9d, 1048575
        shr       r9d, 12
        shl       edx, 8
        add       r9d, edx
        movsd     xmm2, QWORD PTR [imagerel(_vmldHypotHATab)+r10+r9*8]
        movsd     xmm14, QWORD PTR [_vmldHypotHATab+4104]
        mulsd     xmm5, xmm2
        mulsd     xmm14, xmm2
        movaps    xmm3, xmm5
        mulsd     xmm3, xmm14
        movsd     xmm2, QWORD PTR [_vmldHypotHATab+4104]
        mov       QWORD PTR [104+rsp], rax
        subsd     xmm2, xmm3
        movaps    xmm4, xmm2
        mulsd     xmm2, xmm5
        mulsd     xmm4, xmm14
        addsd     xmm5, xmm2
        addsd     xmm14, xmm4
        movaps    xmm3, xmm5
        movaps    xmm15, xmm14
        mulsd     xmm3, xmm14
        movsd     xmm2, QWORD PTR [_vmldHypotHATab+4104]
        mov       WORD PTR [110+rsp], cx
        subsd     xmm2, xmm3
        mulsd     xmm15, xmm2
        mulsd     xmm2, xmm5
        addsd     xmm14, xmm15
        addsd     xmm5, xmm2
        movaps    xmm3, xmm5
        movaps    xmm4, xmm14
        mulsd     xmm3, xmm14
        movsd     xmm2, QWORD PTR [_vmldHypotHATab+4104]
        mov       QWORD PTR [96+rsp], rax
        subsd     xmm2, xmm3
        mulsd     xmm4, xmm2
        mulsd     xmm2, xmm5
        addsd     xmm14, xmm4
        addsd     xmm5, xmm2
        movsd     xmm3, QWORD PTR [_vmldHypotHATab+4128]
        mulsd     xmm3, xmm5
        movsd     QWORD PTR [64+rsp], xmm3
        movsd     xmm15, QWORD PTR [64+rsp]
        mov       WORD PTR [102+rsp], r11w
        subsd     xmm15, xmm5
        movsd     QWORD PTR [72+rsp], xmm15
        movsd     xmm3, QWORD PTR [64+rsp]
        movsd     xmm2, QWORD PTR [72+rsp]
        mov       QWORD PTR [112+rsp], rax
        subsd     xmm3, xmm2
        movsd     QWORD PTR [64+rsp], xmm3
        movaps    xmm2, xmm5
        movsd     xmm4, QWORD PTR [64+rsp]
        mov       WORD PTR [118+rsp], r12w
        subsd     xmm2, xmm4
        movsd     QWORD PTR [72+rsp], xmm2
        movsd     xmm2, QWORD PTR [64+rsp]
        movsd     xmm4, QWORD PTR [104+rsp]
        movaps    xmm15, xmm2
        mulsd     xmm1, xmm4
        mulsd     xmm15, xmm2
        mulsd     xmm0, xmm4
        subsd     xmm1, xmm15
        movsd     xmm15, QWORD PTR [_vmldHypotHATab+4120]
        mulsd     xmm15, xmm2
        movsd     xmm3, QWORD PTR [72+rsp]
        mulsd     xmm15, xmm3
        mulsd     xmm3, xmm3
        subsd     xmm1, xmm15
        subsd     xmm1, xmm3
        addsd     xmm1, xmm0
        mulsd     xmm14, xmm1
        addsd     xmm5, xmm14
        mulsd     xmm5, QWORD PTR [96+rsp]
        mulsd     xmm5, QWORD PTR [112+rsp]
        movsd     QWORD PTR [r8], xmm5

_B9_10::

        movups    xmm14, XMMWORD PTR [48+rsp]
        xor       eax, eax
        movups    xmm15, XMMWORD PTR [32+rsp]
        add       rsp, 128
        pop       r12
        ret

_B9_11::

        ucomisd   xmm0, xmm4
        jne       _B9_4
        jp        _B9_4

_B9_12::

        movsd     QWORD PTR [r8], xmm4
        jmp       _B9_10

_B9_13::

        movsd     xmm0, QWORD PTR [rdx]
        mulsd     xmm0, xmm0
        movsd     QWORD PTR [r8], xmm0
        jmp       _B9_10

_B9_14::

        movzx     eax, WORD PTR [6+rdx]
        and       eax, 32752
        cmp       eax, 32752
        je        _B9_16

_B9_15::

        movsd     xmm0, QWORD PTR [rcx]
        mulsd     xmm0, xmm0
        movsd     QWORD PTR [r8], xmm0
        jmp       _B9_10

_B9_16::

        mov       eax, DWORD PTR [4+rcx]
        and       eax, 1048575
        jne       _B9_18

_B9_17::

        cmp       DWORD PTR [rcx], 0
        je        _B9_22

_B9_18::

        test      DWORD PTR [4+rdx], 1048575
        jne       _B9_20

_B9_19::

        cmp       DWORD PTR [rdx], 0
        je        _B9_21

_B9_20::

        movsd     xmm0, QWORD PTR [rcx]
        mulsd     xmm0, QWORD PTR [rdx]
        movsd     QWORD PTR [r8], xmm0
        jmp       _B9_10

_B9_21::

        test      eax, eax
        jne       _B9_13

_B9_27::

        cmp       DWORD PTR [rcx], 0

_B9_22::

        je        _B9_15
        jmp       _B9_13
        ALIGN     16

_B9_25::

__svml_dhypot_ha_cout_rare_internal ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_dhypot_ha_cout_rare_internal_B1_B22:
	DD	403713
	DD	256041
	DD	194595
	DD	3221680653

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B9_1
	DD	imagerel _B9_25
	DD	imagerel _unwind___svml_dhypot_ha_cout_rare_internal_B1_B22

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_RDATA	SEGMENT     READ PAGE   'DATA'
	ALIGN  32
	PUBLIC __svml_dhypot_ha_data_internal
__svml_dhypot_ha_data_internal	DD	0
	DD	4294950912
	DD	0
	DD	4294950912
	DD	0
	DD	4294950912
	DD	0
	DD	4294950912
	DD	0
	DD	4294950912
	DD	0
	DD	4294950912
	DD	0
	DD	4294950912
	DD	0
	DD	4294950912
	DD	4294967295
	DD	2147483647
	DD	4294967295
	DD	2147483647
	DD	4294967295
	DD	2147483647
	DD	4294967295
	DD	2147483647
	DD	4294967295
	DD	2147483647
	DD	4294967295
	DD	2147483647
	DD	4294967295
	DD	2147483647
	DD	4294967295
	DD	2147483647
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
	DD	0
	DD	3218046976
	DD	0
	DD	3218046976
	DD	0
	DD	3218046976
	DD	0
	DD	3218046976
	DD	0
	DD	3218046976
	DD	0
	DD	3218046976
	DD	0
	DD	3218046976
	DD	0
	DD	3218046976
	DD	0
	DD	1070694400
	DD	0
	DD	1070694400
	DD	0
	DD	1070694400
	DD	0
	DD	1070694400
	DD	0
	DD	1070694400
	DD	0
	DD	1070694400
	DD	0
	DD	1070694400
	DD	0
	DD	1070694400
	DD	0
	DD	3218341888
	DD	0
	DD	3218341888
	DD	0
	DD	3218341888
	DD	0
	DD	3218341888
	DD	0
	DD	3218341888
	DD	0
	DD	3218341888
	DD	0
	DD	3218341888
	DD	0
	DD	3218341888
	DD	0
	DD	1071120384
	DD	0
	DD	1071120384
	DD	0
	DD	1071120384
	DD	0
	DD	1071120384
	DD	0
	DD	1071120384
	DD	0
	DD	1071120384
	DD	0
	DD	1071120384
	DD	0
	DD	1071120384
	DD	0
	DD	3219128320
	DD	0
	DD	3219128320
	DD	0
	DD	3219128320
	DD	0
	DD	3219128320
	DD	0
	DD	3219128320
	DD	0
	DD	3219128320
	DD	0
	DD	3219128320
	DD	0
	DD	3219128320
	DD	6291456
	DD	6291456
	DD	6291456
	DD	6291456
	DD	6291456
	DD	6291456
	DD	6291456
	DD	6291456
	DD	6291456
	DD	6291456
	DD	6291456
	DD	6291456
	DD	6291456
	DD	6291456
	DD	6291456
	DD	6291456
	DD	1002438656
	DD	1002438656
	DD	1002438656
	DD	1002438656
	DD	1002438656
	DD	1002438656
	DD	1002438656
	DD	1002438656
	DD	1002438656
	DD	1002438656
	DD	1002438656
	DD	1002438656
	DD	1002438656
	DD	1002438656
	DD	1002438656
	DD	1002438656
	DD	1141899264
	DD	1141899264
	DD	1141899264
	DD	1141899264
	DD	1141899264
	DD	1141899264
	DD	1141899264
	DD	1141899264
	DD	1141899264
	DD	1141899264
	DD	1141899264
	DD	1141899264
	DD	1141899264
	DD	1141899264
	DD	1141899264
	DD	1141899264
	DD	0
	DD	1082126336
	DD	0
	DD	1082126336
	DD	0
	DD	1082126336
	DD	0
	DD	1082126336
	DD	0
	DD	1082126336
	DD	0
	DD	1082126336
	DD	0
	DD	1082126336
	DD	0
	DD	1082126336
	DD	0
	DD	1078951936
	DD	0
	DD	1078951936
	DD	0
	DD	1078951936
	DD	0
	DD	1078951936
	DD	0
	DD	1078951936
	DD	0
	DD	1078951936
	DD	0
	DD	1078951936
	DD	0
	DD	1078951936
	DD	0
	DD	1071644672
	DD	0
	DD	1071644672
	DD	0
	DD	1071644672
	DD	0
	DD	1071644672
	DD	0
	DD	1071644672
	DD	0
	DD	1071644672
	DD	0
	DD	1071644672
	DD	0
	DD	1071644672
_vmldHypotHATab	DD	0
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
	DD	0
	DD	0
	DD	1071644672
	DD	0
	DD	1072693248
	DD	0
	DD	1073741824
	DD	33554432
	DD	1101004800
_RDATA	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS
EXTRN	__ImageBase:PROC
EXTRN	_fltused:BYTE
	ENDIF
	END
