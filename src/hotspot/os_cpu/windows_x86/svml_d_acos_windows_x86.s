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
	PUBLIC __svml_acos2_ha_l9

__svml_acos2_ha_l9	PROC

_B1_1::

        DB        243
        DB        15
        DB        30
        DB        250
L1::

        sub       rsp, 312
        vmovups   XMMWORD PTR [272+rsp], xmm12
        vmovups   XMMWORD PTR [240+rsp], xmm11
        vmovups   XMMWORD PTR [256+rsp], xmm10
        vmovups   XMMWORD PTR [192+rsp], xmm9
        vmovups   XMMWORD PTR [224+rsp], xmm8
        vmovups   XMMWORD PTR [208+rsp], xmm7
        mov       QWORD PTR [288+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovupd   xmm7, XMMWORD PTR [rcx]
        and       r13, -64
        vmovupd   xmm5, XMMWORD PTR [__svml_dacos_ha_data_internal]
        vmovupd   xmm4, XMMWORD PTR [__svml_dacos_ha_data_internal+64]
        vorpd     xmm1, xmm5, xmm7
        vfmadd231pd xmm4, xmm4, xmm1
        vandpd    xmm5, xmm5, xmm7
        vmulpd    xmm3, xmm1, xmm1
        vcvtpd2ps xmm8, xmm4
        vminpd    xmm10, xmm3, xmm4
        vcmpltpd  xmm9, xmm4, XMMWORD PTR [__svml_dacos_ha_data_internal+128]
        vcmpngepd xmm2, xmm1, XMMWORD PTR [__svml_dacos_ha_data_internal+256]
        vmovlhps  xmm0, xmm8, xmm8
        vrsqrtps  xmm3, xmm0
        vmovmskpd edx, xmm2
        vcvtps2pd xmm11, xmm3
        vcmpnltpd xmm2, xmm10, xmm4
        vaddpd    xmm4, xmm4, xmm4
        vandnpd   xmm12, xmm9, xmm11
        vandpd    xmm9, xmm4, xmm2
        vmulpd    xmm8, xmm12, xmm12
        vmulpd    xmm3, xmm12, xmm9
        vfmsub213pd xmm8, xmm9, XMMWORD PTR [__svml_dacos_ha_data_internal+384]
        vmovupd   xmm4, XMMWORD PTR [__svml_dacos_ha_data_internal+448]
        vmovupd   xmm11, XMMWORD PTR [__svml_dacos_ha_data_internal+832]
        vfmsub213pd xmm9, xmm12, xmm3
        vfmadd213pd xmm11, xmm10, XMMWORD PTR [__svml_dacos_ha_data_internal+896]
        vfmadd213pd xmm4, xmm8, XMMWORD PTR [__svml_dacos_ha_data_internal+512]
        vmovupd   xmm12, XMMWORD PTR [__svml_dacos_ha_data_internal+1088]
        vmulpd    xmm0, xmm8, xmm3
        vfmadd213pd xmm4, xmm8, XMMWORD PTR [__svml_dacos_ha_data_internal+576]
        vfmadd213pd xmm12, xmm10, XMMWORD PTR [__svml_dacos_ha_data_internal+1152]
        vfmadd213pd xmm4, xmm8, XMMWORD PTR [__svml_dacos_ha_data_internal+640]
        vmulpd    xmm8, xmm10, xmm10
        vfmsub213pd xmm4, xmm0, xmm9
        vmovupd   xmm0, XMMWORD PTR [__svml_dacos_ha_data_internal+704]
        vmovupd   xmm9, XMMWORD PTR [__svml_dacos_ha_data_internal+960]
        vfmadd213pd xmm0, xmm10, XMMWORD PTR [__svml_dacos_ha_data_internal+768]
        vfmadd213pd xmm9, xmm10, XMMWORD PTR [__svml_dacos_ha_data_internal+1024]
        vfmadd213pd xmm0, xmm8, xmm11
        vfmadd213pd xmm9, xmm8, xmm12
        vmovupd   xmm12, XMMWORD PTR [__svml_dacos_ha_data_internal+1216]
        vmulpd    xmm11, xmm8, xmm8
        vfmadd213pd xmm12, xmm10, XMMWORD PTR [__svml_dacos_ha_data_internal+1280]
        vfmadd213pd xmm0, xmm11, xmm9
        vandnpd   xmm9, xmm2, xmm1
        vorpd     xmm3, xmm3, xmm9
        vandnpd   xmm11, xmm2, XMMWORD PTR [__svml_dacos_ha_data_internal+1664]
        vfmadd213pd xmm0, xmm8, xmm12
        vfmadd213pd xmm0, xmm10, XMMWORD PTR [__svml_dacos_ha_data_internal+1344]
        vfmadd213pd xmm0, xmm10, XMMWORD PTR [__svml_dacos_ha_data_internal+1408]
        vmulpd    xmm0, xmm10, xmm0
        vcmpnltpd xmm10, xmm10, xmm7
        vandpd    xmm8, xmm10, xmm2
        vandpd    xmm12, xmm8, XMMWORD PTR [__svml_dacos_ha_data_internal+1472]
        vandpd    xmm10, xmm8, XMMWORD PTR [__svml_dacos_ha_data_internal+1536]
        vandnpd   xmm8, xmm2, XMMWORD PTR [__svml_dacos_ha_data_internal+1600]
        vorpd     xmm1, xmm8, xmm12
        vorpd     xmm8, xmm11, xmm10
        vxorpd    xmm1, xmm1, xmm5
        vsubpd    xmm2, xmm3, xmm4
        vsubpd    xmm4, xmm1, xmm4
        vfmadd213pd xmm0, xmm2, xmm4
        vaddpd    xmm0, xmm3, xmm0
        vxorpd    xmm5, xmm0, xmm5
        mov       QWORD PTR [296+rsp], r13
        vaddpd    xmm0, xmm8, xmm5
        test      edx, edx
        jne       _B1_3

_B1_2::

        vmovups   xmm7, XMMWORD PTR [208+rsp]
        vmovups   xmm8, XMMWORD PTR [224+rsp]
        vmovups   xmm9, XMMWORD PTR [192+rsp]
        vmovups   xmm10, XMMWORD PTR [256+rsp]
        vmovups   xmm11, XMMWORD PTR [240+rsp]
        vmovups   xmm12, XMMWORD PTR [272+rsp]
        mov       r13, QWORD PTR [288+rsp]
        add       rsp, 312
        ret

_B1_3::

        vmovupd   XMMWORD PTR [r13], xmm7
        vmovupd   XMMWORD PTR [64+r13], xmm0

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
        vmovupd   xmm0, XMMWORD PTR [64+r13]
        jmp       _B1_2

_B1_10::

        lea       rcx, QWORD PTR [r13+rbx*8]
        lea       rdx, QWORD PTR [64+r13+rbx*8]

        call      __svml_dacos_ha_cout_rare_internal
        jmp       _B1_8
        ALIGN     16

_B1_11::

__svml_acos2_ha_l9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_acos2_ha_l9_B1_B3:
	DD	1067265
	DD	2413641
	DD	882753
	DD	952376
	DD	825391
	DD	1091622
	DD	1030173
	DD	1165332
	DD	2556171

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B1_1
	DD	imagerel _B1_6
	DD	imagerel _unwind___svml_acos2_ha_l9_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_acos2_ha_l9_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B1_1
	DD	imagerel _B1_6
	DD	imagerel _unwind___svml_acos2_ha_l9_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B1_6
	DD	imagerel _B1_11
	DD	imagerel _unwind___svml_acos2_ha_l9_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST1:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_acos1_ha_ex

__svml_acos1_ha_ex	PROC

_B2_1::

        DB        243
        DB        15
        DB        30
        DB        250
L20::

        sub       rsp, 280
        movups    XMMWORD PTR [224+rsp], xmm15
        movups    XMMWORD PTR [256+rsp], xmm14
        movups    XMMWORD PTR [192+rsp], xmm13
        movups    XMMWORD PTR [208+rsp], xmm12
        movups    XMMWORD PTR [240+rsp], xmm7
        movups    XMMWORD PTR [176+rsp], xmm6
        mov       QWORD PTR [168+rsp], r13
        lea       r13, QWORD PTR [95+rsp]
        movups    xmm13, XMMWORD PTR [rcx]
        and       r13, -64
        movsd     xmm12, QWORD PTR [__svml_dacos_ha_data_internal]
        movaps    xmm2, xmm13
        orps      xmm2, xmm12
        andps     xmm12, xmm13
        movsd     xmm5, QWORD PTR [__svml_dacos_ha_data_internal+64]
        movaps    xmm0, xmm2
        mulsd     xmm0, xmm5
        movaps    xmm4, xmm2
        movsd     xmm1, QWORD PTR [__svml_dacos_ha_data_internal+256]
        addsd     xmm0, xmm5
        cmpnlesd  xmm1, xmm2
        cvtpd2ps  xmm7, xmm0
        movsd     xmm4, xmm1
        movaps    xmm1, xmm2
        mulsd     xmm1, xmm2
        movaps    xmm3, xmm0
        movmskpd  eax, xmm4
        shufps    xmm7, xmm7, 0
        rsqrtps   xmm6, xmm7
        unpcklpd  xmm1, xmm1
        and       eax, 1
        unpcklpd  xmm3, xmm0
        minpd     xmm1, xmm3
        cvtps2pd  xmm14, xmm6
        movaps    xmm5, xmm1
        movaps    xmm3, xmm0
        movsd     xmm7, QWORD PTR [__svml_dacos_ha_data_internal+320]
        movaps    xmm4, xmm1
        movsd     xmm6, QWORD PTR [__svml_dacos_ha_data_internal+448]
        cmpnltsd  xmm5, xmm0
        mulsd     xmm4, QWORD PTR [__svml_dacos_ha_data_internal+832]
        addsd     xmm0, xmm0
        cmpltsd   xmm3, QWORD PTR [__svml_dacos_ha_data_internal+128]
        addsd     xmm4, QWORD PTR [__svml_dacos_ha_data_internal+896]
        andps     xmm0, xmm5
        andnps    xmm3, xmm14
        andps     xmm7, xmm0
        mov       QWORD PTR [272+rsp], r13
        subsd     xmm0, xmm7
        mulsd     xmm7, xmm3
        mulsd     xmm0, xmm3
        movaps    xmm14, xmm7
        movaps    xmm15, xmm0
        mulsd     xmm14, xmm3
        mulsd     xmm15, xmm3
        subsd     xmm14, QWORD PTR [__svml_dacos_ha_data_internal+384]
        movaps    xmm3, xmm7
        addsd     xmm14, xmm15
        addsd     xmm3, xmm0
        mulsd     xmm6, xmm14
        mulsd     xmm3, xmm14
        addsd     xmm6, QWORD PTR [__svml_dacos_ha_data_internal+512]
        mulsd     xmm6, xmm14
        movaps    xmm15, xmm1
        mulsd     xmm15, QWORD PTR [__svml_dacos_ha_data_internal+1088]
        addsd     xmm6, QWORD PTR [__svml_dacos_ha_data_internal+576]
        addsd     xmm15, QWORD PTR [__svml_dacos_ha_data_internal+1152]
        mulsd     xmm6, xmm14
        movaps    xmm14, xmm1
        addsd     xmm6, QWORD PTR [__svml_dacos_ha_data_internal+640]
        mulsd     xmm14, QWORD PTR [__svml_dacos_ha_data_internal+960]
        mulsd     xmm6, xmm3
        addsd     xmm14, QWORD PTR [__svml_dacos_ha_data_internal+1024]
        subsd     xmm6, xmm0
        movaps    xmm0, xmm1
        movaps    xmm3, xmm1
        mulsd     xmm0, QWORD PTR [__svml_dacos_ha_data_internal+704]
        mulsd     xmm3, xmm1
        addsd     xmm0, QWORD PTR [__svml_dacos_ha_data_internal+768]
        mulsd     xmm14, xmm3
        mulsd     xmm0, xmm3
        addsd     xmm14, xmm15
        addsd     xmm0, xmm4
        movaps    xmm4, xmm3
        movaps    xmm15, xmm1
        mulsd     xmm4, xmm3
        mulsd     xmm15, QWORD PTR [__svml_dacos_ha_data_internal+1216]
        mulsd     xmm0, xmm4
        addsd     xmm15, QWORD PTR [__svml_dacos_ha_data_internal+1280]
        addsd     xmm0, xmm14
        mulsd     xmm0, xmm3
        movsd     xmm3, QWORD PTR [__svml_dacos_ha_data_internal+1472]
        addsd     xmm0, xmm15
        mulsd     xmm0, xmm1
        movsd     xmm4, QWORD PTR [__svml_dacos_ha_data_internal+1536]
        movsd     xmm14, QWORD PTR [__svml_dacos_ha_data_internal+1600]
        movsd     xmm15, QWORD PTR [__svml_dacos_ha_data_internal+1664]
        addsd     xmm0, QWORD PTR [__svml_dacos_ha_data_internal+1344]
        mulsd     xmm0, xmm1
        addsd     xmm0, QWORD PTR [__svml_dacos_ha_data_internal+1408]
        mulsd     xmm0, xmm1
        cmpnltsd  xmm1, xmm13
        andps     xmm1, xmm5
        andps     xmm3, xmm1
        andps     xmm4, xmm1
        movaps    xmm1, xmm5
        andnps    xmm1, xmm14
        movaps    xmm14, xmm5
        andnps    xmm5, xmm2
        orps      xmm1, xmm3
        orps      xmm7, xmm5
        pxor      xmm1, xmm12
        movaps    xmm2, xmm7
        andnps    xmm14, xmm15
        orps      xmm14, xmm4
        subsd     xmm2, xmm6
        subsd     xmm1, xmm6
        mulsd     xmm0, xmm2
        addsd     xmm0, xmm1
        addsd     xmm0, xmm7
        pxor      xmm0, xmm12
        addsd     xmm0, xmm14
        jne       _B2_3

_B2_2::

        movups    xmm6, XMMWORD PTR [176+rsp]
        movups    xmm7, XMMWORD PTR [240+rsp]
        movups    xmm12, XMMWORD PTR [208+rsp]
        movups    xmm13, XMMWORD PTR [192+rsp]
        movups    xmm14, XMMWORD PTR [256+rsp]
        movups    xmm15, XMMWORD PTR [224+rsp]
        mov       r13, QWORD PTR [168+rsp]
        add       rsp, 280
        ret

_B2_3::

        movsd     QWORD PTR [r13], xmm13
        movsd     QWORD PTR [64+r13], xmm0
        jne       _B2_6

_B2_4::

        movsd     xmm0, QWORD PTR [64+r13]
        jmp       _B2_2

_B2_6::

        lea       rcx, QWORD PTR [r13]
        lea       rdx, QWORD PTR [64+r13]

        call      __svml_dacos_ha_cout_rare_internal
        jmp       _B2_4
        ALIGN     16

_B2_7::

__svml_acos1_ha_ex ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_acos1_ha_ex_B1_B6:
	DD	1066753
	DD	1430599
	DD	747583
	DD	1013815
	DD	903215
	DD	841766
	DD	1107997
	DD	981012
	DD	2294027

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B2_1
	DD	imagerel _B2_7
	DD	imagerel _unwind___svml_acos1_ha_ex_B1_B6

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST2:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_acos1_ha_e9

__svml_acos1_ha_e9	PROC

_B3_1::

        DB        243
        DB        15
        DB        30
        DB        250
L35::

        sub       rsp, 280
        vmovups   XMMWORD PTR [208+rsp], xmm15
        vmovups   XMMWORD PTR [224+rsp], xmm14
        vmovups   XMMWORD PTR [256+rsp], xmm13
        vmovups   XMMWORD PTR [192+rsp], xmm8
        vmovups   XMMWORD PTR [240+rsp], xmm7
        vmovups   XMMWORD PTR [176+rsp], xmm6
        mov       QWORD PTR [168+rsp], r13
        lea       r13, QWORD PTR [95+rsp]
        vmovupd   xmm15, XMMWORD PTR [rcx]
        and       r13, -64
        vmovsd    xmm14, QWORD PTR [__svml_dacos_ha_data_internal]
        vmovsd    xmm3, QWORD PTR [__svml_dacos_ha_data_internal+64]
        vorpd     xmm7, xmm14, xmm15
        vmulsd    xmm5, xmm7, xmm3
        vcmpngesd xmm0, xmm7, QWORD PTR [__svml_dacos_ha_data_internal+256]
        vmulsd    xmm4, xmm7, xmm7
        vmovmskpd eax, xmm0
        vaddsd    xmm8, xmm5, xmm3
        vcvtpd2ps xmm5, xmm8
        vshufps   xmm13, xmm5, xmm5, 0
        vandpd    xmm14, xmm14, xmm15
        vrsqrtps  xmm3, xmm13
        vmovddup  xmm1, xmm4
        vmovddup  xmm2, xmm8
        vminpd    xmm4, xmm1, xmm2
        vcvtps2pd xmm2, xmm3
        vcmpltsd  xmm1, xmm8, QWORD PTR [__svml_dacos_ha_data_internal+128]
        vcmpnltsd xmm6, xmm4, xmm8
        vaddsd    xmm8, xmm8, xmm8
        vmovsd    xmm0, QWORD PTR [__svml_dacos_ha_data_internal+320]
        vandpd    xmm5, xmm8, xmm6
        vandnpd   xmm13, xmm1, xmm2
        vandpd    xmm8, xmm5, xmm0
        vmovsd    xmm0, QWORD PTR [__svml_dacos_ha_data_internal+448]
        vsubsd    xmm3, xmm5, xmm8
        vmulsd    xmm8, xmm8, xmm13
        vmulsd    xmm3, xmm3, xmm13
        vmulsd    xmm1, xmm8, xmm13
        vmulsd    xmm2, xmm3, xmm13
        vsubsd    xmm13, xmm1, QWORD PTR [__svml_dacos_ha_data_internal+384]
        vaddsd    xmm1, xmm8, xmm3
        vaddsd    xmm5, xmm13, xmm2
        vmulsd    xmm13, xmm0, xmm5
        mov       QWORD PTR [272+rsp], r13
        vaddsd    xmm2, xmm13, QWORD PTR [__svml_dacos_ha_data_internal+512]
        vmulsd    xmm13, xmm1, xmm5
        vmulsd    xmm0, xmm2, xmm5
        vmulsd    xmm2, xmm4, xmm4
        vaddsd    xmm1, xmm0, QWORD PTR [__svml_dacos_ha_data_internal+576]
        vmulsd    xmm0, xmm4, QWORD PTR [__svml_dacos_ha_data_internal+960]
        vmulsd    xmm5, xmm1, xmm5
        vaddsd    xmm0, xmm0, QWORD PTR [__svml_dacos_ha_data_internal+1024]
        vmulsd    xmm1, xmm4, QWORD PTR [__svml_dacos_ha_data_internal+832]
        vaddsd    xmm5, xmm5, QWORD PTR [__svml_dacos_ha_data_internal+640]
        vmulsd    xmm13, xmm5, xmm13
        vaddsd    xmm5, xmm1, QWORD PTR [__svml_dacos_ha_data_internal+896]
        vmulsd    xmm1, xmm4, QWORD PTR [__svml_dacos_ha_data_internal+1088]
        vsubsd    xmm13, xmm13, xmm3
        vmulsd    xmm3, xmm4, QWORD PTR [__svml_dacos_ha_data_internal+704]
        vaddsd    xmm1, xmm1, QWORD PTR [__svml_dacos_ha_data_internal+1152]
        vaddsd    xmm3, xmm3, QWORD PTR [__svml_dacos_ha_data_internal+768]
        vmulsd    xmm3, xmm3, xmm2
        vaddsd    xmm3, xmm3, xmm5
        vmulsd    xmm5, xmm0, xmm2
        vmulsd    xmm0, xmm2, xmm2
        vaddsd    xmm1, xmm5, xmm1
        vmulsd    xmm5, xmm4, QWORD PTR [__svml_dacos_ha_data_internal+1216]
        vmulsd    xmm3, xmm3, xmm0
        vaddsd    xmm5, xmm5, QWORD PTR [__svml_dacos_ha_data_internal+1280]
        vaddsd    xmm1, xmm3, xmm1
        vmulsd    xmm2, xmm1, xmm2
        vaddsd    xmm5, xmm2, xmm5
        vmulsd    xmm3, xmm5, xmm4
        vaddsd    xmm1, xmm3, QWORD PTR [__svml_dacos_ha_data_internal+1344]
        vmulsd    xmm2, xmm1, xmm4
        vmovsd    xmm3, QWORD PTR [__svml_dacos_ha_data_internal+1472]
        vaddsd    xmm0, xmm2, QWORD PTR [__svml_dacos_ha_data_internal+1408]
        vmulsd    xmm5, xmm0, xmm4
        vcmpnltsd xmm4, xmm4, xmm15
        vmovsd    xmm2, QWORD PTR [__svml_dacos_ha_data_internal+1536]
        vandpd    xmm4, xmm4, xmm6
        vmovsd    xmm0, QWORD PTR [__svml_dacos_ha_data_internal+1600]
        vandpd    xmm1, xmm3, xmm4
        vandpd    xmm3, xmm2, xmm4
        vandnpd   xmm2, xmm6, xmm0
        vmovsd    xmm4, QWORD PTR [__svml_dacos_ha_data_internal+1664]
        vorpd     xmm1, xmm2, xmm1
        vandnpd   xmm4, xmm6, xmm4
        vandnpd   xmm6, xmm6, xmm7
        vxorpd    xmm0, xmm1, xmm14
        vorpd     xmm1, xmm8, xmm6
        vorpd     xmm7, xmm4, xmm3
        vsubsd    xmm6, xmm1, xmm13
        vsubsd    xmm0, xmm0, xmm13
        vmulsd    xmm5, xmm5, xmm6
        vaddsd    xmm6, xmm5, xmm0
        vaddsd    xmm2, xmm6, xmm1
        vxorpd    xmm14, xmm2, xmm14
        vaddsd    xmm0, xmm14, xmm7
        and       eax, 1
        jne       _B3_3

_B3_2::

        vmovups   xmm6, XMMWORD PTR [176+rsp]
        vmovups   xmm7, XMMWORD PTR [240+rsp]
        vmovups   xmm8, XMMWORD PTR [192+rsp]
        vmovups   xmm13, XMMWORD PTR [256+rsp]
        vmovups   xmm14, XMMWORD PTR [224+rsp]
        vmovups   xmm15, XMMWORD PTR [208+rsp]
        mov       r13, QWORD PTR [168+rsp]
        add       rsp, 280
        ret

_B3_3::

        vmovsd    QWORD PTR [r13], xmm15
        vmovsd    QWORD PTR [64+r13], xmm0
        jne       _B3_6

_B3_4::

        vmovsd    xmm0, QWORD PTR [64+r13]
        jmp       _B3_2

_B3_6::

        lea       rcx, QWORD PTR [r13]
        lea       rdx, QWORD PTR [64+r13]

        call      __svml_dacos_ha_cout_rare_internal
        jmp       _B3_4
        ALIGN     16

_B3_7::

__svml_acos1_ha_e9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_acos1_ha_e9_B1_B6:
	DD	1067265
	DD	1430601
	DD	747585
	DD	1013816
	DD	821295
	DD	1103910
	DD	976925
	DD	915476
	DD	2294027

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B3_1
	DD	imagerel _B3_7
	DD	imagerel _unwind___svml_acos1_ha_e9_B1_B6

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST3:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_acos4_ha_l9

__svml_acos4_ha_l9	PROC

_B4_1::

        DB        243
        DB        15
        DB        30
        DB        250
L50::

        sub       rsp, 328
        vmovups   XMMWORD PTR [288+rsp], xmm12
        vmovups   XMMWORD PTR [272+rsp], xmm11
        vmovups   XMMWORD PTR [208+rsp], xmm10
        vmovups   XMMWORD PTR [240+rsp], xmm9
        vmovups   XMMWORD PTR [256+rsp], xmm8
        vmovups   XMMWORD PTR [224+rsp], xmm7
        mov       QWORD PTR [304+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovupd   ymm7, YMMWORD PTR [rcx]
        and       r13, -64
        vmovupd   ymm5, YMMWORD PTR [__svml_dacos_ha_data_internal]
        vmovupd   ymm4, YMMWORD PTR [__svml_dacos_ha_data_internal+64]
        vorpd     ymm1, ymm5, ymm7
        vfmadd231pd ymm4, ymm4, ymm1
        vmulpd    ymm3, ymm1, ymm1
        vcmpnge_uqpd ymm2, ymm1, YMMWORD PTR [__svml_dacos_ha_data_internal+256]
        vcvtpd2ps xmm8, ymm4
        vminpd    ymm10, ymm3, ymm4
        vcmplt_oqpd ymm3, ymm4, YMMWORD PTR [__svml_dacos_ha_data_internal+128]
        vrsqrtps  xmm0, xmm8
        vcvtps2pd ymm9, xmm0
        vandnpd   ymm11, ymm3, ymm9
        vmulpd    ymm12, ymm11, ymm11
        vmovupd   ymm9, YMMWORD PTR [__svml_dacos_ha_data_internal+960]
        vmovmskpd edx, ymm2
        vcmpnlt_uqpd ymm2, ymm10, ymm4
        test      edx, edx
        vaddpd    ymm4, ymm4, ymm4
        vfmadd213pd ymm9, ymm10, YMMWORD PTR [__svml_dacos_ha_data_internal+1024]
        vandpd    ymm0, ymm4, ymm2
        vfmsub213pd ymm12, ymm0, YMMWORD PTR [__svml_dacos_ha_data_internal+384]
        vmovupd   ymm4, YMMWORD PTR [__svml_dacos_ha_data_internal+448]
        vmulpd    ymm3, ymm11, ymm0
        vfmadd213pd ymm4, ymm12, YMMWORD PTR [__svml_dacos_ha_data_internal+512]
        vfmsub213pd ymm0, ymm11, ymm3
        vmulpd    ymm8, ymm12, ymm3
        vmovupd   ymm11, YMMWORD PTR [__svml_dacos_ha_data_internal+832]
        vfmadd213pd ymm4, ymm12, YMMWORD PTR [__svml_dacos_ha_data_internal+576]
        vfmadd213pd ymm11, ymm10, YMMWORD PTR [__svml_dacos_ha_data_internal+896]
        vfmadd213pd ymm4, ymm12, YMMWORD PTR [__svml_dacos_ha_data_internal+640]
        vmovupd   ymm12, YMMWORD PTR [__svml_dacos_ha_data_internal+1088]
        vfmsub213pd ymm4, ymm8, ymm0
        vmovupd   ymm0, YMMWORD PTR [__svml_dacos_ha_data_internal+704]
        vmulpd    ymm8, ymm10, ymm10
        vfmadd213pd ymm12, ymm10, YMMWORD PTR [__svml_dacos_ha_data_internal+1152]
        vfmadd213pd ymm0, ymm10, YMMWORD PTR [__svml_dacos_ha_data_internal+768]
        vfmadd213pd ymm9, ymm8, ymm12
        vmovupd   ymm12, YMMWORD PTR [__svml_dacos_ha_data_internal+1216]
        vfmadd213pd ymm0, ymm8, ymm11
        vmulpd    ymm11, ymm8, ymm8
        vfmadd213pd ymm12, ymm10, YMMWORD PTR [__svml_dacos_ha_data_internal+1280]
        vfmadd213pd ymm0, ymm11, ymm9
        vcmpnlt_uqpd ymm9, ymm10, ymm7
        vfmadd213pd ymm0, ymm8, ymm12
        vfmadd213pd ymm0, ymm10, YMMWORD PTR [__svml_dacos_ha_data_internal+1344]
        vfmadd213pd ymm0, ymm10, YMMWORD PTR [__svml_dacos_ha_data_internal+1408]
        vmulpd    ymm0, ymm10, ymm0
        vandpd    ymm10, ymm9, ymm2
        vandpd    ymm12, ymm10, YMMWORD PTR [__svml_dacos_ha_data_internal+1472]
        vandnpd   ymm8, ymm2, YMMWORD PTR [__svml_dacos_ha_data_internal+1600]
        vandpd    ymm5, ymm5, ymm7
        vandnpd   ymm9, ymm2, ymm1
        vorpd     ymm1, ymm8, ymm12
        vxorpd    ymm1, ymm1, ymm5
        vorpd     ymm3, ymm3, ymm9
        vandnpd   ymm11, ymm2, YMMWORD PTR [__svml_dacos_ha_data_internal+1664]
        vsubpd    ymm2, ymm3, ymm4
        vsubpd    ymm4, ymm1, ymm4
        vfmadd213pd ymm0, ymm2, ymm4
        vaddpd    ymm0, ymm3, ymm0
        vandpd    ymm10, ymm10, YMMWORD PTR [__svml_dacos_ha_data_internal+1536]
        vorpd     ymm8, ymm11, ymm10
        vxorpd    ymm5, ymm0, ymm5
        mov       QWORD PTR [312+rsp], r13
        vaddpd    ymm0, ymm8, ymm5
        jne       _B4_3

_B4_2::

        vmovups   xmm7, XMMWORD PTR [224+rsp]
        vmovups   xmm8, XMMWORD PTR [256+rsp]
        vmovups   xmm9, XMMWORD PTR [240+rsp]
        vmovups   xmm10, XMMWORD PTR [208+rsp]
        vmovups   xmm11, XMMWORD PTR [272+rsp]
        vmovups   xmm12, XMMWORD PTR [288+rsp]
        mov       r13, QWORD PTR [304+rsp]
        add       rsp, 328
        ret

_B4_3::

        vmovupd   YMMWORD PTR [r13], ymm7
        vmovupd   YMMWORD PTR [64+r13], ymm0

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
        vmovupd   ymm0, YMMWORD PTR [64+r13]
        jmp       _B4_2

_B4_10::

        vzeroupper
        lea       rcx, QWORD PTR [r13+rbx*8]
        lea       rdx, QWORD PTR [64+r13+rbx*8]

        call      __svml_dacos_ha_cout_rare_internal
        jmp       _B4_8
        ALIGN     16

_B4_11::

__svml_acos4_ha_l9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_acos4_ha_l9_B1_B3:
	DD	1067265
	DD	2544713
	DD	948289
	DD	1083448
	DD	1021999
	DD	895014
	DD	1161245
	DD	1230868
	DD	2687243

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B4_1
	DD	imagerel _B4_6
	DD	imagerel _unwind___svml_acos4_ha_l9_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_acos4_ha_l9_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B4_1
	DD	imagerel _B4_6
	DD	imagerel _unwind___svml_acos4_ha_l9_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B4_6
	DD	imagerel _B4_11
	DD	imagerel _unwind___svml_acos4_ha_l9_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST4:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_acos2_ha_e9

__svml_acos2_ha_e9	PROC

_B5_1::

        DB        243
        DB        15
        DB        30
        DB        250
L69::

        sub       rsp, 312
        vmovups   XMMWORD PTR [224+rsp], xmm15
        vmovups   XMMWORD PTR [272+rsp], xmm14
        vmovups   XMMWORD PTR [240+rsp], xmm9
        vmovups   XMMWORD PTR [192+rsp], xmm8
        vmovups   XMMWORD PTR [208+rsp], xmm7
        vmovups   XMMWORD PTR [256+rsp], xmm6
        mov       QWORD PTR [288+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovupd   xmm15, XMMWORD PTR [rcx]
        and       r13, -64
        vmovupd   xmm14, XMMWORD PTR [__svml_dacos_ha_data_internal]
        vmovupd   xmm5, XMMWORD PTR [__svml_dacos_ha_data_internal+64]
        vorpd     xmm6, xmm14, xmm15
        vmulpd    xmm4, xmm6, xmm5
        vmulpd    xmm3, xmm6, xmm6
        vaddpd    xmm8, xmm5, xmm4
        vcmpngepd xmm2, xmm6, XMMWORD PTR [__svml_dacos_ha_data_internal+256]
        vcvtpd2ps xmm1, xmm8
        vminpd    xmm4, xmm3, xmm8
        vcmpltpd  xmm9, xmm8, XMMWORD PTR [__svml_dacos_ha_data_internal+128]
        vmovmskpd edx, xmm2
        vcmpnltpd xmm7, xmm4, xmm8
        vaddpd    xmm8, xmm8, xmm8
        vmovlhps  xmm0, xmm1, xmm1
        vandpd    xmm2, xmm8, xmm7
        vrsqrtps  xmm5, xmm0
        vcvtps2pd xmm3, xmm5
        vandpd    xmm1, xmm2, XMMWORD PTR [__svml_dacos_ha_data_internal+320]
        vandnpd   xmm9, xmm9, xmm3
        vsubpd    xmm0, xmm2, xmm1
        vmulpd    xmm8, xmm9, xmm1
        vmulpd    xmm3, xmm9, xmm0
        vmulpd    xmm5, xmm9, xmm8
        vmulpd    xmm9, xmm9, xmm3
        vsubpd    xmm2, xmm5, XMMWORD PTR [__svml_dacos_ha_data_internal+384]
        vaddpd    xmm0, xmm8, xmm3
        vaddpd    xmm5, xmm9, xmm2
        vmulpd    xmm1, xmm5, XMMWORD PTR [__svml_dacos_ha_data_internal+448]
        vmulpd    xmm9, xmm5, xmm0
        vaddpd    xmm2, xmm1, XMMWORD PTR [__svml_dacos_ha_data_internal+512]
        vmulpd    xmm1, xmm5, xmm2
        vaddpd    xmm0, xmm1, XMMWORD PTR [__svml_dacos_ha_data_internal+576]
        vmulpd    xmm1, xmm4, XMMWORD PTR [__svml_dacos_ha_data_internal+960]
        vmulpd    xmm5, xmm5, xmm0
        vaddpd    xmm0, xmm1, XMMWORD PTR [__svml_dacos_ha_data_internal+1024]
        vaddpd    xmm5, xmm5, XMMWORD PTR [__svml_dacos_ha_data_internal+640]
        vmulpd    xmm1, xmm4, XMMWORD PTR [__svml_dacos_ha_data_internal+1088]
        vmulpd    xmm9, xmm9, xmm5
        vmulpd    xmm5, xmm4, XMMWORD PTR [__svml_dacos_ha_data_internal+832]
        vsubpd    xmm9, xmm9, xmm3
        vaddpd    xmm1, xmm1, XMMWORD PTR [__svml_dacos_ha_data_internal+1152]
        vaddpd    xmm5, xmm5, XMMWORD PTR [__svml_dacos_ha_data_internal+896]
        vmulpd    xmm3, xmm4, XMMWORD PTR [__svml_dacos_ha_data_internal+704]
        vaddpd    xmm2, xmm3, XMMWORD PTR [__svml_dacos_ha_data_internal+768]
        vmulpd    xmm3, xmm4, xmm4
        vmulpd    xmm0, xmm3, xmm0
        vmulpd    xmm2, xmm2, xmm3
        vaddpd    xmm1, xmm1, xmm0
        vaddpd    xmm2, xmm5, xmm2
        vmulpd    xmm0, xmm3, xmm3
        vmulpd    xmm5, xmm4, XMMWORD PTR [__svml_dacos_ha_data_internal+1216]
        vmulpd    xmm2, xmm2, xmm0
        vaddpd    xmm5, xmm5, XMMWORD PTR [__svml_dacos_ha_data_internal+1280]
        vaddpd    xmm0, xmm1, xmm2
        vmulpd    xmm3, xmm3, xmm0
        vaddpd    xmm5, xmm5, xmm3
        vmulpd    xmm3, xmm4, xmm5
        vaddpd    xmm2, xmm3, XMMWORD PTR [__svml_dacos_ha_data_internal+1344]
        vmulpd    xmm0, xmm4, xmm2
        vaddpd    xmm1, xmm0, XMMWORD PTR [__svml_dacos_ha_data_internal+1408]
        vmulpd    xmm5, xmm4, xmm1
        vcmpnltpd xmm4, xmm4, xmm15
        vandpd    xmm3, xmm4, xmm7
        vandpd    xmm14, xmm14, xmm15
        vandpd    xmm2, xmm3, XMMWORD PTR [__svml_dacos_ha_data_internal+1472]
        vandpd    xmm4, xmm3, XMMWORD PTR [__svml_dacos_ha_data_internal+1536]
        vandnpd   xmm0, xmm7, XMMWORD PTR [__svml_dacos_ha_data_internal+1600]
        vandnpd   xmm3, xmm7, XMMWORD PTR [__svml_dacos_ha_data_internal+1664]
        vandnpd   xmm7, xmm7, xmm6
        vorpd     xmm1, xmm0, xmm2
        vorpd     xmm0, xmm8, xmm7
        vsubpd    xmm7, xmm0, xmm9
        vmulpd    xmm5, xmm5, xmm7
        vxorpd    xmm2, xmm1, xmm14
        vorpd     xmm6, xmm3, xmm4
        vsubpd    xmm2, xmm2, xmm9
        vaddpd    xmm1, xmm2, xmm5
        vaddpd    xmm3, xmm0, xmm1
        vxorpd    xmm14, xmm3, xmm14
        mov       QWORD PTR [296+rsp], r13
        vaddpd    xmm0, xmm6, xmm14
        test      edx, edx
        jne       _B5_3

_B5_2::

        vmovups   xmm6, XMMWORD PTR [256+rsp]
        vmovups   xmm7, XMMWORD PTR [208+rsp]
        vmovups   xmm8, XMMWORD PTR [192+rsp]
        vmovups   xmm9, XMMWORD PTR [240+rsp]
        vmovups   xmm14, XMMWORD PTR [272+rsp]
        vmovups   xmm15, XMMWORD PTR [224+rsp]
        mov       r13, QWORD PTR [288+rsp]
        add       rsp, 312
        ret

_B5_3::

        vmovupd   XMMWORD PTR [r13], xmm15
        vmovupd   XMMWORD PTR [64+r13], xmm0

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
        cmp       ebx, 2
        jl        _B5_7

_B5_9::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        vmovupd   xmm0, XMMWORD PTR [64+r13]
        jmp       _B5_2

_B5_10::

        lea       rcx, QWORD PTR [r13+rbx*8]
        lea       rdx, QWORD PTR [64+r13+rbx*8]

        call      __svml_dacos_ha_cout_rare_internal
        jmp       _B5_8
        ALIGN     16

_B5_11::

__svml_acos2_ha_e9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_acos2_ha_e9_B1_B3:
	DD	1067265
	DD	2413641
	DD	1075265
	DD	882744
	DD	821295
	DD	1021990
	DD	1173533
	DD	981012
	DD	2556171

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B5_1
	DD	imagerel _B5_6
	DD	imagerel _unwind___svml_acos2_ha_e9_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_acos2_ha_e9_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B5_1
	DD	imagerel _B5_6
	DD	imagerel _unwind___svml_acos2_ha_e9_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B5_6
	DD	imagerel _B5_11
	DD	imagerel _unwind___svml_acos2_ha_e9_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST5:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_acos4_ha_e9

__svml_acos4_ha_e9	PROC

_B6_1::

        DB        243
        DB        15
        DB        30
        DB        250
L88::

        sub       rsp, 328
        vmovups   XMMWORD PTR [224+rsp], xmm15
        vmovups   XMMWORD PTR [240+rsp], xmm14
        vmovups   XMMWORD PTR [272+rsp], xmm13
        vmovups   XMMWORD PTR [288+rsp], xmm12
        vmovups   XMMWORD PTR [256+rsp], xmm7
        vmovups   XMMWORD PTR [208+rsp], xmm6
        mov       QWORD PTR [304+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovupd   ymm15, YMMWORD PTR [rcx]
        and       r13, -64
        vmovupd   ymm14, YMMWORD PTR [__svml_dacos_ha_data_internal]
        vmovupd   ymm5, YMMWORD PTR [__svml_dacos_ha_data_internal+64]
        vorpd     ymm6, ymm14, ymm15
        vmulpd    ymm4, ymm6, ymm5
        vmulpd    ymm3, ymm6, ymm6
        vcmpnge_uqpd ymm2, ymm6, YMMWORD PTR [__svml_dacos_ha_data_internal+256]
        vaddpd    ymm12, ymm5, ymm4
        vcvtpd2ps xmm5, ymm12
        vminpd    ymm4, ymm3, ymm12
        vcmplt_oqpd ymm3, ymm12, YMMWORD PTR [__svml_dacos_ha_data_internal+128]
        vrsqrtps  xmm13, xmm5
        vcmpnlt_uqpd ymm7, ymm4, ymm12
        vaddpd    ymm12, ymm12, ymm12
        vandpd    ymm14, ymm14, ymm15
        mov       QWORD PTR [312+rsp], r13
        vextractf128 xmm1, ymm2, 1
        vshufps   xmm0, xmm2, xmm1, 221
        vcvtps2pd ymm2, xmm13
        vmovmskps edx, xmm0
        vandpd    ymm1, ymm12, ymm7
        vandnpd   ymm13, ymm3, ymm2
        vandpd    ymm0, ymm1, YMMWORD PTR [__svml_dacos_ha_data_internal+320]
        vsubpd    ymm5, ymm1, ymm0
        vmulpd    ymm12, ymm13, ymm0
        vmulpd    ymm3, ymm13, ymm5
        vmulpd    ymm2, ymm13, ymm12
        vmulpd    ymm13, ymm13, ymm3
        vsubpd    ymm1, ymm2, YMMWORD PTR [__svml_dacos_ha_data_internal+384]
        vaddpd    ymm5, ymm13, ymm1
        vaddpd    ymm13, ymm12, ymm3
        vmulpd    ymm0, ymm5, YMMWORD PTR [__svml_dacos_ha_data_internal+448]
        vmulpd    ymm13, ymm5, ymm13
        vaddpd    ymm2, ymm0, YMMWORD PTR [__svml_dacos_ha_data_internal+512]
        vmulpd    ymm1, ymm5, ymm2
        vaddpd    ymm0, ymm1, YMMWORD PTR [__svml_dacos_ha_data_internal+576]
        vmulpd    ymm1, ymm4, YMMWORD PTR [__svml_dacos_ha_data_internal+960]
        vmulpd    ymm5, ymm5, ymm0
        vaddpd    ymm0, ymm1, YMMWORD PTR [__svml_dacos_ha_data_internal+1024]
        vmulpd    ymm1, ymm4, YMMWORD PTR [__svml_dacos_ha_data_internal+1088]
        vaddpd    ymm5, ymm5, YMMWORD PTR [__svml_dacos_ha_data_internal+640]
        vaddpd    ymm1, ymm1, YMMWORD PTR [__svml_dacos_ha_data_internal+1152]
        vmulpd    ymm13, ymm13, ymm5
        vmulpd    ymm5, ymm4, YMMWORD PTR [__svml_dacos_ha_data_internal+832]
        vsubpd    ymm13, ymm13, ymm3
        vaddpd    ymm5, ymm5, YMMWORD PTR [__svml_dacos_ha_data_internal+896]
        vmulpd    ymm3, ymm4, YMMWORD PTR [__svml_dacos_ha_data_internal+704]
        vaddpd    ymm2, ymm3, YMMWORD PTR [__svml_dacos_ha_data_internal+768]
        vmulpd    ymm3, ymm4, ymm4
        vmulpd    ymm0, ymm3, ymm0
        vmulpd    ymm2, ymm2, ymm3
        vaddpd    ymm1, ymm1, ymm0
        vaddpd    ymm2, ymm5, ymm2
        vmulpd    ymm0, ymm3, ymm3
        vmulpd    ymm5, ymm4, YMMWORD PTR [__svml_dacos_ha_data_internal+1216]
        vmulpd    ymm2, ymm2, ymm0
        vaddpd    ymm5, ymm5, YMMWORD PTR [__svml_dacos_ha_data_internal+1280]
        vaddpd    ymm0, ymm1, ymm2
        vmulpd    ymm3, ymm3, ymm0
        vaddpd    ymm5, ymm5, ymm3
        vmulpd    ymm3, ymm4, ymm5
        vaddpd    ymm2, ymm3, YMMWORD PTR [__svml_dacos_ha_data_internal+1344]
        vmulpd    ymm0, ymm4, ymm2
        vaddpd    ymm1, ymm0, YMMWORD PTR [__svml_dacos_ha_data_internal+1408]
        vmulpd    ymm5, ymm4, ymm1
        vcmpnlt_uqpd ymm4, ymm4, ymm15
        vandpd    ymm3, ymm4, ymm7
        vandpd    ymm2, ymm3, YMMWORD PTR [__svml_dacos_ha_data_internal+1472]
        vandpd    ymm4, ymm3, YMMWORD PTR [__svml_dacos_ha_data_internal+1536]
        vandnpd   ymm0, ymm7, YMMWORD PTR [__svml_dacos_ha_data_internal+1600]
        vandnpd   ymm3, ymm7, YMMWORD PTR [__svml_dacos_ha_data_internal+1664]
        vandnpd   ymm7, ymm7, ymm6
        vorpd     ymm1, ymm0, ymm2
        vorpd     ymm0, ymm12, ymm7
        vsubpd    ymm7, ymm0, ymm13
        vmulpd    ymm5, ymm5, ymm7
        vxorpd    ymm2, ymm1, ymm14
        vsubpd    ymm2, ymm2, ymm13
        vaddpd    ymm1, ymm2, ymm5
        vorpd     ymm6, ymm3, ymm4
        vaddpd    ymm3, ymm0, ymm1
        vxorpd    ymm14, ymm3, ymm14
        vaddpd    ymm0, ymm6, ymm14
        test      edx, edx
        jne       _B6_3

_B6_2::

        vmovups   xmm6, XMMWORD PTR [208+rsp]
        vmovups   xmm7, XMMWORD PTR [256+rsp]
        vmovups   xmm12, XMMWORD PTR [288+rsp]
        vmovups   xmm13, XMMWORD PTR [272+rsp]
        vmovups   xmm14, XMMWORD PTR [240+rsp]
        vmovups   xmm15, XMMWORD PTR [224+rsp]
        mov       r13, QWORD PTR [304+rsp]
        add       rsp, 328
        ret

_B6_3::

        vmovupd   YMMWORD PTR [r13], ymm15
        vmovupd   YMMWORD PTR [64+r13], ymm0

_B6_6::

        xor       eax, eax
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, eax
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, edx

_B6_7::

        bt        esi, ebx
        jc        _B6_10

_B6_8::

        inc       ebx
        cmp       ebx, 4
        jl        _B6_7

_B6_9::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        vmovupd   ymm0, YMMWORD PTR [64+r13]
        jmp       _B6_2

_B6_10::

        vzeroupper
        lea       rcx, QWORD PTR [r13+rbx*8]
        lea       rdx, QWORD PTR [64+r13+rbx*8]

        call      __svml_dacos_ha_cout_rare_internal
        jmp       _B6_8
        ALIGN     16

_B6_11::

__svml_acos4_ha_e9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_acos4_ha_e9_B1_B3:
	DD	1067265
	DD	2544713
	DD	878657
	DD	1079352
	DD	1230895
	DD	1169446
	DD	1042461
	DD	981012
	DD	2687243

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B6_1
	DD	imagerel _B6_6
	DD	imagerel _unwind___svml_acos4_ha_e9_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_acos4_ha_e9_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B6_1
	DD	imagerel _B6_6
	DD	imagerel _unwind___svml_acos4_ha_e9_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B6_6
	DD	imagerel _B6_11
	DD	imagerel _unwind___svml_acos4_ha_e9_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST6:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_acos2_ha_ex

__svml_acos2_ha_ex	PROC

_B7_1::

        DB        243
        DB        15
        DB        30
        DB        250
L107::

        sub       rsp, 312
        movups    XMMWORD PTR [240+rsp], xmm15
        movups    XMMWORD PTR [224+rsp], xmm14
        movups    XMMWORD PTR [208+rsp], xmm10
        movups    XMMWORD PTR [272+rsp], xmm8
        movups    XMMWORD PTR [192+rsp], xmm7
        movups    XMMWORD PTR [256+rsp], xmm6
        mov       QWORD PTR [288+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        movups    xmm14, XMMWORD PTR [__svml_dacos_ha_data_internal]
        and       r13, -64
        movups    xmm6, XMMWORD PTR [__svml_dacos_ha_data_internal+64]
        movaps    xmm10, xmm14
        movups    xmm15, XMMWORD PTR [rcx]
        movaps    xmm5, xmm6
        orps      xmm10, xmm15
        andps     xmm14, xmm15
        mulpd     xmm5, xmm10
        addpd     xmm6, xmm5
        movaps    xmm8, xmm10
        cvtpd2ps  xmm1, xmm6
        mulpd     xmm8, xmm10
        movlhps   xmm1, xmm1
        rsqrtps   xmm0, xmm1
        minpd     xmm8, xmm6
        cvtps2pd  xmm2, xmm0
        movaps    xmm7, xmm8
        movaps    xmm0, xmm6
        cmpnltpd  xmm7, xmm6
        addpd     xmm6, xmm6
        cmpltpd   xmm0, XMMWORD PTR [__svml_dacos_ha_data_internal+128]
        movups    xmm5, XMMWORD PTR [__svml_dacos_ha_data_internal+320]
        andps     xmm6, xmm7
        andnps    xmm0, xmm2
        andps     xmm5, xmm6
        subpd     xmm6, xmm5
        mulpd     xmm5, xmm0
        mulpd     xmm6, xmm0
        movaps    xmm3, xmm0
        movaps    xmm2, xmm8
        mulpd     xmm3, xmm5
        mulpd     xmm0, xmm6
        subpd     xmm3, XMMWORD PTR [__svml_dacos_ha_data_internal+384]
        mulpd     xmm2, xmm8
        addpd     xmm0, xmm3
        movups    xmm1, XMMWORD PTR [__svml_dacos_ha_data_internal+448]
        mulpd     xmm1, xmm0
        addpd     xmm1, XMMWORD PTR [__svml_dacos_ha_data_internal+512]
        mulpd     xmm1, xmm0
        movups    xmm4, XMMWORD PTR [__svml_dacos_ha_data_internal+256]
        cmpnlepd  xmm4, xmm10
        addpd     xmm1, XMMWORD PTR [__svml_dacos_ha_data_internal+576]
        movmskpd  eax, xmm4
        movaps    xmm4, xmm5
        movups    xmm3, XMMWORD PTR [__svml_dacos_ha_data_internal+832]
        addpd     xmm4, xmm6
        mulpd     xmm3, xmm8
        mulpd     xmm4, xmm0
        mulpd     xmm0, xmm1
        addpd     xmm3, XMMWORD PTR [__svml_dacos_ha_data_internal+896]
        addpd     xmm0, XMMWORD PTR [__svml_dacos_ha_data_internal+640]
        mulpd     xmm4, xmm0
        subpd     xmm4, xmm6
        movups    xmm6, XMMWORD PTR [__svml_dacos_ha_data_internal+704]
        mulpd     xmm6, xmm8
        addpd     xmm6, XMMWORD PTR [__svml_dacos_ha_data_internal+768]
        movups    xmm1, XMMWORD PTR [__svml_dacos_ha_data_internal+960]
        mulpd     xmm1, xmm8
        mulpd     xmm6, xmm2
        addpd     xmm1, XMMWORD PTR [__svml_dacos_ha_data_internal+1024]
        addpd     xmm3, xmm6
        mulpd     xmm1, xmm2
        movups    xmm0, XMMWORD PTR [__svml_dacos_ha_data_internal+1088]
        movaps    xmm6, xmm2
        mulpd     xmm0, xmm8
        mulpd     xmm6, xmm2
        addpd     xmm0, XMMWORD PTR [__svml_dacos_ha_data_internal+1152]
        mulpd     xmm3, xmm6
        addpd     xmm0, xmm1
        movups    xmm1, XMMWORD PTR [__svml_dacos_ha_data_internal+1216]
        mulpd     xmm1, xmm8
        addpd     xmm0, xmm3
        addpd     xmm1, XMMWORD PTR [__svml_dacos_ha_data_internal+1280]
        mulpd     xmm2, xmm0
        addpd     xmm1, xmm2
        mulpd     xmm1, xmm8
        addpd     xmm1, XMMWORD PTR [__svml_dacos_ha_data_internal+1344]
        mulpd     xmm1, xmm8
        addpd     xmm1, XMMWORD PTR [__svml_dacos_ha_data_internal+1408]
        mulpd     xmm1, xmm8
        cmpnltpd  xmm8, xmm15
        movups    xmm2, XMMWORD PTR [__svml_dacos_ha_data_internal+1472]
        andps     xmm8, xmm7
        movups    xmm3, XMMWORD PTR [__svml_dacos_ha_data_internal+1536]
        andps     xmm2, xmm8
        andps     xmm3, xmm8
        movaps    xmm8, xmm7
        movaps    xmm0, xmm7
        andnps    xmm7, xmm10
        orps      xmm5, xmm7
        movaps    xmm7, xmm5
        subpd     xmm7, xmm4
        andnps    xmm8, XMMWORD PTR [__svml_dacos_ha_data_internal+1600]
        mulpd     xmm1, xmm7
        orps      xmm8, xmm2
        pxor      xmm8, xmm14
        subpd     xmm8, xmm4
        addpd     xmm8, xmm1
        addpd     xmm5, xmm8
        andnps    xmm0, XMMWORD PTR [__svml_dacos_ha_data_internal+1664]
        pxor      xmm5, xmm14
        orps      xmm0, xmm3
        mov       QWORD PTR [296+rsp], r13
        addpd     xmm0, xmm5
        test      eax, eax
        jne       _B7_3

_B7_2::

        movups    xmm6, XMMWORD PTR [256+rsp]
        movups    xmm7, XMMWORD PTR [192+rsp]
        movups    xmm8, XMMWORD PTR [272+rsp]
        movups    xmm10, XMMWORD PTR [208+rsp]
        movups    xmm14, XMMWORD PTR [224+rsp]
        movups    xmm15, XMMWORD PTR [240+rsp]
        mov       r13, QWORD PTR [288+rsp]
        add       rsp, 312
        ret

_B7_3::

        movups    XMMWORD PTR [r13], xmm15
        movups    XMMWORD PTR [64+r13], xmm0

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

        call      __svml_dacos_ha_cout_rare_internal
        jmp       _B7_8
        ALIGN     16

_B7_11::

__svml_acos2_ha_ex ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_acos2_ha_ex_B1_B3:
	DD	1066753
	DD	2413639
	DD	1075263
	DD	817207
	DD	1148975
	DD	895014
	DD	976925
	DD	1046548
	DD	2556171

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B7_1
	DD	imagerel _B7_6
	DD	imagerel _unwind___svml_acos2_ha_ex_B1_B3

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_acos2_ha_ex_B6_B10:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B7_1
	DD	imagerel _B7_6
	DD	imagerel _unwind___svml_acos2_ha_ex_B1_B3

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B7_6
	DD	imagerel _B7_11
	DD	imagerel _unwind___svml_acos2_ha_ex_B6_B10

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST7:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_acos1_ha_l9

__svml_acos1_ha_l9	PROC

_B8_1::

        DB        243
        DB        15
        DB        30
        DB        250
L126::

        sub       rsp, 296
        vmovups   XMMWORD PTR [256+rsp], xmm15
        vmovups   XMMWORD PTR [240+rsp], xmm11
        vmovups   XMMWORD PTR [272+rsp], xmm10
        vmovups   XMMWORD PTR [208+rsp], xmm9
        vmovups   XMMWORD PTR [224+rsp], xmm8
        vmovups   XMMWORD PTR [176+rsp], xmm7
        vmovups   XMMWORD PTR [192+rsp], xmm6
        mov       QWORD PTR [168+rsp], r13
        lea       r13, QWORD PTR [95+rsp]
        vmovupd   xmm5, XMMWORD PTR [rcx]
        and       r13, -64
        vmovsd    xmm4, QWORD PTR [__svml_dacos_ha_data_internal]
        vorpd     xmm0, xmm4, xmm5
        vandpd    xmm4, xmm4, xmm5
        vmovsd    xmm3, QWORD PTR [__svml_dacos_ha_data_internal+64]
        vmovdqa   xmm9, xmm0
        vfmadd213sd xmm9, xmm3, xmm3
        vmulsd    xmm2, xmm0, xmm0
        vcmpngesd xmm8, xmm0, QWORD PTR [__svml_dacos_ha_data_internal+256]
        vcvtpd2ps xmm3, xmm9
        vmovmskpd eax, xmm8
        vcmpltsd  xmm11, xmm9, QWORD PTR [__svml_dacos_ha_data_internal+128]
        vmovddup  xmm1, xmm2
        and       eax, 1
        vbroadcastss xmm2, xmm3
        vmovddup  xmm6, xmm9
        vminpd    xmm7, xmm1, xmm6
        vrsqrtps  xmm10, xmm2
        vcmpnltsd xmm1, xmm7, xmm9
        vcvtps2pd xmm15, xmm10
        vaddsd    xmm9, xmm9, xmm9
        vandnpd   xmm3, xmm11, xmm15
        vandpd    xmm10, xmm9, xmm1
        vmulsd    xmm6, xmm3, xmm3
        vmovdqa   xmm8, xmm10
        vmulsd    xmm2, xmm10, xmm3
        vfmsub213sd xmm8, xmm6, QWORD PTR [__svml_dacos_ha_data_internal+384]
        vmovsd    xmm11, QWORD PTR [__svml_dacos_ha_data_internal+704]
        vmovdqa   xmm6, xmm7
        vmovapd   xmm15, xmm7
        vfmsub213sd xmm10, xmm3, xmm2
        vmovsd    xmm3, QWORD PTR [__svml_dacos_ha_data_internal+448]
        vfmadd213sd xmm3, xmm8, QWORD PTR [__svml_dacos_ha_data_internal+512]
        vmulsd    xmm9, xmm2, xmm8
        vfmadd213sd xmm6, xmm11, QWORD PTR [__svml_dacos_ha_data_internal+768]
        vfmadd213sd xmm3, xmm8, QWORD PTR [__svml_dacos_ha_data_internal+576]
        vmovapd   xmm11, xmm7
        vfmadd213sd xmm3, xmm8, QWORD PTR [__svml_dacos_ha_data_internal+640]
        vmovsd    xmm8, QWORD PTR [__svml_dacos_ha_data_internal+960]
        vfmadd213sd xmm11, xmm8, QWORD PTR [__svml_dacos_ha_data_internal+1024]
        vmovapd   xmm8, xmm7
        vfmsub213sd xmm3, xmm9, xmm10
        vmovsd    xmm9, QWORD PTR [__svml_dacos_ha_data_internal+832]
        vfmadd213sd xmm15, xmm9, QWORD PTR [__svml_dacos_ha_data_internal+896]
        vmovsd    xmm9, QWORD PTR [__svml_dacos_ha_data_internal+1088]
        vfmadd213sd xmm8, xmm9, QWORD PTR [__svml_dacos_ha_data_internal+1152]
        vmulsd    xmm10, xmm7, xmm7
        vfmadd213sd xmm6, xmm10, xmm15
        vfmadd213sd xmm11, xmm10, xmm8
        vmovapd   xmm8, xmm7
        vmulsd    xmm9, xmm10, xmm10
        vmovsd    xmm15, QWORD PTR [__svml_dacos_ha_data_internal+1216]
        vfmadd213sd xmm8, xmm15, QWORD PTR [__svml_dacos_ha_data_internal+1280]
        vfmadd213sd xmm6, xmm9, xmm11
        vmovsd    xmm11, QWORD PTR [__svml_dacos_ha_data_internal+1600]
        vmovsd    xmm15, QWORD PTR [__svml_dacos_ha_data_internal+1664]
        vfmadd213sd xmm6, xmm10, xmm8
        vmovsd    xmm10, QWORD PTR [__svml_dacos_ha_data_internal+1536]
        vfmadd213sd xmm6, xmm7, QWORD PTR [__svml_dacos_ha_data_internal+1344]
        vfmadd213sd xmm6, xmm7, QWORD PTR [__svml_dacos_ha_data_internal+1408]
        vmulsd    xmm9, xmm6, xmm7
        vcmpnltsd xmm7, xmm7, xmm5
        vmovsd    xmm6, QWORD PTR [__svml_dacos_ha_data_internal+1472]
        vandpd    xmm7, xmm7, xmm1
        vandpd    xmm8, xmm6, xmm7
        vandnpd   xmm6, xmm1, xmm11
        vandpd    xmm7, xmm10, xmm7
        vandnpd   xmm10, xmm1, xmm15
        vandnpd   xmm1, xmm1, xmm0
        vorpd     xmm0, xmm6, xmm8
        vorpd     xmm6, xmm10, xmm7
        vxorpd    xmm0, xmm0, xmm4
        vorpd     xmm7, xmm2, xmm1
        mov       QWORD PTR [288+rsp], r13
        vsubsd    xmm2, xmm7, xmm3
        vsubsd    xmm3, xmm0, xmm3
        vfmadd213sd xmm9, xmm2, xmm3
        vaddsd    xmm8, xmm9, xmm7
        vxorpd    xmm4, xmm8, xmm4
        vaddsd    xmm0, xmm4, xmm6
        jne       _B8_3

_B8_2::

        vmovups   xmm6, XMMWORD PTR [192+rsp]
        vmovups   xmm7, XMMWORD PTR [176+rsp]
        vmovups   xmm8, XMMWORD PTR [224+rsp]
        vmovups   xmm9, XMMWORD PTR [208+rsp]
        vmovups   xmm10, XMMWORD PTR [272+rsp]
        vmovups   xmm11, XMMWORD PTR [240+rsp]
        vmovups   xmm15, XMMWORD PTR [256+rsp]
        mov       r13, QWORD PTR [168+rsp]
        add       rsp, 296
        ret

_B8_3::

        vmovsd    QWORD PTR [r13], xmm5
        vmovsd    QWORD PTR [64+r13], xmm0
        jne       _B8_6

_B8_4::

        vmovsd    xmm0, QWORD PTR [64+r13]
        jmp       _B8_2

_B8_6::

        lea       rcx, QWORD PTR [r13]
        lea       rdx, QWORD PTR [64+r13]

        call      __svml_dacos_ha_cout_rare_internal
        jmp       _B8_4
        ALIGN     16

_B8_7::

__svml_acos1_ha_l9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_acos1_ha_l9_B1_B6:
	DD	1200641
	DD	1430610
	DD	813130
	DD	751681
	DD	952376
	DD	890927
	DD	1157158
	DD	1030173
	DD	1112084
	DD	2425099

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B8_1
	DD	imagerel _B8_7
	DD	imagerel _unwind___svml_acos1_ha_l9_B1_B6

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST8:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_dacos_ha_cout_rare_internal

__svml_dacos_ha_cout_rare_internal	PROC

_B9_1::

        DB        243
        DB        15
        DB        30
        DB        250
L143::

        push      rbp
        sub       rsp, 192
        lea       rbp, QWORD PTR [32+rsp]
        mov       r8, rcx
        movups    XMMWORD PTR [112+rbp], xmm15
        mov       r9, rdx
        movups    XMMWORD PTR [144+rbp], xmm13
        xor       eax, eax
        movups    XMMWORD PTR [80+rbp], xmm12
        movzx     r10d, WORD PTR [6+r8]
        and       r10d, 32752
        mov       dl, BYTE PTR [7+r8]
        movsd     xmm1, QWORD PTR [r8]
        movups    XMMWORD PTR [64+rbp], xmm11
        movups    XMMWORD PTR [128+rbp], xmm10
        movups    XMMWORD PTR [96+rbp], xmm7
        movups    XMMWORD PTR [48+rbp], xmm6
        cmp       r10d, 32752
        je        _B9_20

_B9_2::

        movsd     QWORD PTR [24+rbp], xmm1
        and       dl, 127
        mov       BYTE PTR [31+rbp], dl
        movsd     xmm4, QWORD PTR [24+rbp]
        movsd     xmm0, QWORD PTR [_vmldACosHATab+4168]
        comisd    xmm0, xmm4
        jbe       _B9_14

_B9_3::

        movsd     xmm2, QWORD PTR [_vmldACosHATab+4176]
        comisd    xmm2, xmm4
        jbe       _B9_10

_B9_4::

        comisd    xmm4, QWORD PTR [_vmldACosHATab+4128]
        jbe       _B9_9

_B9_5::

        movsd     xmm0, QWORD PTR [_vmldACosHATab+4104]
        movaps    xmm2, xmm4
        mulsd     xmm0, xmm4
        movaps    xmm15, xmm4
        movsd     QWORD PTR [16+rbp], xmm0
        movsd     xmm7, QWORD PTR [16+rbp]
        movsd     xmm0, QWORD PTR [_vmldACosHATab+4312]
        movsd     xmm11, QWORD PTR [_vmldACosHATab+4104]
        subsd     xmm7, QWORD PTR [24+rbp]
        movsd     QWORD PTR [8+rbp], xmm7
        movsd     xmm3, QWORD PTR [16+rbp]
        movsd     xmm6, QWORD PTR [8+rbp]
        subsd     xmm3, xmm6
        movsd     QWORD PTR [16+rbp], xmm3
        movsd     xmm10, QWORD PTR [16+rbp]
        subsd     xmm2, xmm10
        movsd     QWORD PTR [8+rbp], xmm2
        movsd     xmm1, QWORD PTR [16+rbp]
        movaps    xmm10, xmm1
        addsd     xmm15, xmm1
        mulsd     xmm10, xmm1
        movsd     xmm13, QWORD PTR [8+rbp]
        movaps    xmm5, xmm10
        mulsd     xmm15, xmm13
        mulsd     xmm11, xmm10
        addsd     xmm5, xmm15
        mulsd     xmm0, xmm5
        movsd     QWORD PTR [16+rbp], xmm11
        movsd     xmm12, QWORD PTR [16+rbp]
        addsd     xmm0, QWORD PTR [_vmldACosHATab+4304]
        subsd     xmm12, xmm10
        mulsd     xmm0, xmm5
        movsd     QWORD PTR [8+rbp], xmm12
        movsd     xmm6, QWORD PTR [16+rbp]
        movsd     xmm7, QWORD PTR [8+rbp]
        addsd     xmm0, QWORD PTR [_vmldACosHATab+4296]
        subsd     xmm6, xmm7
        mulsd     xmm0, xmm5
        movsd     QWORD PTR [16+rbp], xmm6
        movsd     xmm3, QWORD PTR [16+rbp]
        addsd     xmm0, QWORD PTR [_vmldACosHATab+4288]
        subsd     xmm10, xmm3
        mulsd     xmm0, xmm5
        movsd     QWORD PTR [8+rbp], xmm10
        movsd     xmm3, QWORD PTR [16+rbp]
        movsd     xmm2, QWORD PTR [8+rbp]
        mulsd     xmm13, xmm3
        addsd     xmm0, QWORD PTR [_vmldACosHATab+4280]
        addsd     xmm15, xmm2
        mulsd     xmm1, xmm3
        mulsd     xmm0, xmm5
        movaps    xmm2, xmm4
        movaps    xmm12, xmm1
        mulsd     xmm2, xmm15
        addsd     xmm0, QWORD PTR [_vmldACosHATab+4272]
        mulsd     xmm0, xmm5
        addsd     xmm2, xmm13
        movsd     xmm13, QWORD PTR [_vmldACosHATab+4104]
        mulsd     xmm13, xmm1
        addsd     xmm0, QWORD PTR [_vmldACosHATab+4264]
        movsd     QWORD PTR [16+rbp], xmm13
        movsd     xmm7, QWORD PTR [16+rbp]
        mulsd     xmm0, xmm5
        subsd     xmm7, xmm1
        addsd     xmm1, xmm2
        addsd     xmm0, QWORD PTR [_vmldACosHATab+4256]
        mulsd     xmm15, xmm1
        mulsd     xmm0, xmm5
        movsd     QWORD PTR [8+rbp], xmm7
        movsd     xmm10, QWORD PTR [16+rbp]
        movsd     xmm6, QWORD PTR [8+rbp]
        subsd     xmm10, xmm6
        addsd     xmm0, QWORD PTR [_vmldACosHATab+4248]
        movsd     QWORD PTR [16+rbp], xmm10
        movsd     xmm11, QWORD PTR [16+rbp]
        mulsd     xmm0, xmm5
        subsd     xmm12, xmm11
        movsd     QWORD PTR [8+rbp], xmm12
        movaps    xmm12, xmm3
        movsd     xmm10, QWORD PTR [16+rbp]
        movsd     xmm11, QWORD PTR [8+rbp]
        movaps    xmm7, xmm11
        addsd     xmm0, QWORD PTR [_vmldACosHATab+4240]
        mulsd     xmm12, xmm10
        addsd     xmm7, xmm2
        mulsd     xmm0, xmm5
        mulsd     xmm3, xmm7
        addsd     xmm0, QWORD PTR [_vmldACosHATab+4232]
        addsd     xmm3, xmm15
        mulsd     xmm0, xmm5
        movsd     xmm15, QWORD PTR [_vmldACosHATab+4104]
        mulsd     xmm15, xmm12
        addsd     xmm0, QWORD PTR [_vmldACosHATab+4224]
        movsd     QWORD PTR [16+rbp], xmm15
        movsd     xmm7, QWORD PTR [16+rbp]
        subsd     xmm7, xmm12
        movsd     QWORD PTR [8+rbp], xmm7
        movaps    xmm7, xmm12
        movsd     xmm13, QWORD PTR [16+rbp]
        addsd     xmm12, xmm3
        mulsd     xmm5, xmm12
        mulsd     xmm0, xmm5
        movsd     xmm5, QWORD PTR [_vmldACosHATab+4200]
        mulsd     xmm5, xmm1
        movsd     xmm1, QWORD PTR [_vmldACosHATab+4216]
        mulsd     xmm1, xmm12
        movsd     xmm6, QWORD PTR [8+rbp]
        addsd     xmm5, xmm1
        subsd     xmm13, xmm6
        addsd     xmm5, xmm0
        movsd     xmm0, QWORD PTR [_vmldACosHATab+4192]
        mulsd     xmm2, xmm0
        mulsd     xmm10, xmm0
        mulsd     xmm11, xmm0
        addsd     xmm5, xmm2
        movsd     QWORD PTR [16+rbp], xmm13
        movaps    xmm2, xmm4
        movsd     xmm15, QWORD PTR [16+rbp]
        addsd     xmm2, xmm10
        subsd     xmm7, xmm15
        addsd     xmm5, xmm11
        movsd     QWORD PTR [8+rbp], xmm7
        movsd     xmm7, QWORD PTR [16+rbp]
        movsd     xmm6, QWORD PTR [8+rbp]
        movsd     QWORD PTR [16+rbp], xmm2
        movsd     xmm1, QWORD PTR [16+rbp]
        movsd     QWORD PTR [rbp], xmm10
        subsd     xmm4, xmm1
        addsd     xmm4, xmm10
        movsd     QWORD PTR [8+rbp], xmm4
        movsd     xmm4, QWORD PTR [_vmldACosHATab+4208]
        mulsd     xmm7, xmm4
        mulsd     xmm3, xmm4
        mulsd     xmm4, xmm6
        movsd     xmm1, QWORD PTR [16+rbp]
        movaps    xmm0, xmm1
        movsd     xmm2, QWORD PTR [8+rbp]
        addsd     xmm0, xmm7
        addsd     xmm2, xmm5
        movsd     QWORD PTR [16+rbp], xmm0
        addsd     xmm2, xmm3
        movsd     xmm3, QWORD PTR [16+rbp]
        addsd     xmm2, xmm4
        subsd     xmm1, xmm3
        addsd     xmm1, xmm7
        movsd     QWORD PTR [8+rbp], xmm1
        movsd     xmm4, QWORD PTR [16+rbp]
        movsd     xmm5, QWORD PTR [r8]
        movsd     xmm0, QWORD PTR [8+rbp]
        comisd    xmm5, QWORD PTR [_vmldACosHATab+4184]
        addsd     xmm0, xmm2
        jbe       _B9_7

_B9_6::

        movsd     xmm3, QWORD PTR [_vmldACosHATab+4136]
        movaps    xmm1, xmm3
        subsd     xmm1, xmm4
        movsd     QWORD PTR [16+rbp], xmm1
        movsd     xmm2, QWORD PTR [16+rbp]
        subsd     xmm3, xmm2
        subsd     xmm3, xmm4
        movsd     xmm4, QWORD PTR [_vmldACosHATab+4144]
        subsd     xmm4, xmm0
        movsd     QWORD PTR [8+rbp], xmm3
        movsd     xmm2, QWORD PTR [16+rbp]
        movsd     xmm1, QWORD PTR [8+rbp]
        addsd     xmm1, xmm4
        jmp       _B9_8

_B9_7::

        movsd     xmm3, QWORD PTR [_vmldACosHATab+4136]
        movaps    xmm1, xmm4
        addsd     xmm1, xmm3
        movsd     QWORD PTR [16+rbp], xmm1
        movsd     xmm2, QWORD PTR [16+rbp]
        subsd     xmm3, xmm2
        addsd     xmm3, xmm4
        movsd     xmm4, QWORD PTR [_vmldACosHATab+4144]
        addsd     xmm0, xmm4
        movsd     QWORD PTR [8+rbp], xmm3
        movsd     xmm2, QWORD PTR [16+rbp]
        movsd     xmm1, QWORD PTR [8+rbp]
        addsd     xmm1, xmm0

_B9_8::

        addsd     xmm2, xmm1
        movsd     QWORD PTR [r9], xmm2
        jmp       _B9_17

_B9_9::

        movsd     xmm1, QWORD PTR [_vmldACosHATab+4144]
        addsd     xmm4, xmm0
        movsd     QWORD PTR [16+rbp], xmm4
        movsd     xmm0, QWORD PTR [16+rbp]
        mulsd     xmm0, QWORD PTR [24+rbp]
        movsd     QWORD PTR [rbp], xmm0
        mov       dl, BYTE PTR [7+rbp]
        mov       r8b, BYTE PTR [7+r8]
        and       dl, 127
        and       r8b, -128
        or        dl, r8b
        mov       BYTE PTR [7+rbp], dl
        subsd     xmm1, QWORD PTR [rbp]
        addsd     xmm1, QWORD PTR [_vmldACosHATab+4136]
        movsd     QWORD PTR [r9], xmm1
        jmp       _B9_17

_B9_10::

        movaps    xmm6, xmm0
        movzx     r11d, WORD PTR [_vmldACosHATab+4174]
        subsd     xmm6, xmm4
        mulsd     xmm2, xmm6
        and       r11d, -32753
        movsd     QWORD PTR [rbp], xmm2
        movzx     ecx, WORD PTR [6+rbp]
        and       ecx, 32752
        shr       ecx, 4
        add       ecx, -1023
        mov       r10d, ecx
        mov       edx, ecx
        neg       r10d
        add       r10d, 1023
        and       r10d, 2047
        shl       r10d, 4
        movsd     QWORD PTR [24+rbp], xmm0
        or        r11d, r10d
        mov       WORD PTR [30+rbp], r11w
        and       edx, 1
        movsd     xmm1, QWORD PTR [24+rbp]
        mov       r10d, edx
        mulsd     xmm2, xmm1
        movaps    xmm5, xmm2
        movsd     xmm1, QWORD PTR [_vmldACosHATab+4112]
        addsd     xmm5, xmm2
        jne       L151
        movaps    xmm5, xmm2
L151:
        movsd     xmm13, QWORD PTR [_vmldACosHATab+4120]
        movaps    xmm10, xmm5
        mulsd     xmm1, xmm5
        addsd     xmm2, xmm13
        movsd     QWORD PTR [32+rbp], xmm2
        movaps    xmm7, xmm1
        mov       r11d, DWORD PTR [32+rbp]
        sub       ecx, edx
        shl       r10d, 8
        and       r11d, 511
        add       r11d, r10d
        lea       r10, QWORD PTR [__ImageBase]
        movsd     xmm12, QWORD PTR [_vmldACosHATab+4512]
        subsd     xmm7, xmm5
        movsd     QWORD PTR [8+rbp], xmm7
        movsd     xmm3, QWORD PTR [8+rbp]
        movsd     xmm2, QWORD PTR [imagerel(_vmldACosHATab)+r10+r11*8]
        subsd     xmm1, xmm3
        movaps    xmm11, xmm2
        movaps    xmm4, xmm1
        mulsd     xmm11, xmm2
        subsd     xmm10, xmm1
        mulsd     xmm1, xmm2
        mulsd     xmm4, xmm11
        mulsd     xmm11, xmm10
        subsd     xmm4, xmm0
        shr       ecx, 1
        addsd     xmm11, xmm4
        mulsd     xmm12, xmm11
        add       ecx, 1023
        and       ecx, 2047
        addsd     xmm12, QWORD PTR [_vmldACosHATab+4504]
        mulsd     xmm12, xmm11
        movzx     r10d, WORD PTR [_vmldACosHATab+4174]
        shl       ecx, 4
        and       r10d, -32753
        movsd     QWORD PTR [40+rbp], xmm0
        or        r10d, ecx
        mov       WORD PTR [46+rbp], r10w
        movsd     xmm0, QWORD PTR [40+rbp]
        mulsd     xmm1, xmm0
        addsd     xmm12, QWORD PTR [_vmldACosHATab+4496]
        mulsd     xmm12, xmm11
        movsd     xmm15, QWORD PTR [_vmldACosHATab+4104]
        mulsd     xmm15, xmm1
        addsd     xmm12, QWORD PTR [_vmldACosHATab+4488]
        mulsd     xmm12, xmm11
        movsd     QWORD PTR [16+rbp], xmm15
        movsd     xmm3, QWORD PTR [_vmldACosHATab+4352]
        movsd     xmm4, QWORD PTR [_vmldACosHATab+4336]
        movaps    xmm13, xmm4
        addsd     xmm12, QWORD PTR [_vmldACosHATab+4480]
        addsd     xmm3, QWORD PTR [_vmldACosHATab+4360]
        addsd     xmm13, QWORD PTR [_vmldACosHATab+4344]
        mulsd     xmm12, xmm11
        mulsd     xmm3, xmm6
        addsd     xmm12, QWORD PTR [_vmldACosHATab+4472]
        addsd     xmm13, xmm3
        mulsd     xmm12, xmm11
        mulsd     xmm13, xmm6
        addsd     xmm12, QWORD PTR [_vmldACosHATab+4464]
        mulsd     xmm11, xmm12
        movaps    xmm3, xmm6
        mulsd     xmm11, xmm2
        mulsd     xmm2, xmm10
        mulsd     xmm5, xmm11
        movsd     xmm11, QWORD PTR [_vmldACosHATab+4104]
        addsd     xmm2, xmm5
        mulsd     xmm11, xmm6
        mulsd     xmm2, xmm0
        movsd     xmm5, QWORD PTR [16+rbp]
        movsd     xmm12, QWORD PTR [_vmldACosHATab+4320]
        subsd     xmm5, xmm1
        addsd     xmm12, QWORD PTR [_vmldACosHATab+4328]
        movsd     QWORD PTR [8+rbp], xmm5
        addsd     xmm12, xmm13
        movsd     xmm7, QWORD PTR [16+rbp]
        movsd     xmm5, QWORD PTR [8+rbp]
        movsd     xmm13, QWORD PTR [_vmldACosHATab+4104]
        subsd     xmm7, xmm5
        movsd     QWORD PTR [16+rbp], xmm7
        movsd     xmm0, QWORD PTR [16+rbp]
        subsd     xmm1, xmm0
        movsd     QWORD PTR [8+rbp], xmm1
        movsd     xmm5, QWORD PTR [16+rbp]
        movsd     xmm1, QWORD PTR [8+rbp]
        movsd     QWORD PTR [16+rbp], xmm11
        addsd     xmm1, xmm2
        movsd     xmm2, QWORD PTR [_vmldACosHATab+4456]
        mulsd     xmm2, xmm6
        movsd     xmm10, QWORD PTR [16+rbp]
        addsd     xmm2, QWORD PTR [_vmldACosHATab+4448]
        subsd     xmm10, xmm6
        mulsd     xmm2, xmm6
        movsd     QWORD PTR [8+rbp], xmm10
        movsd     xmm7, QWORD PTR [16+rbp]
        movsd     xmm15, QWORD PTR [8+rbp]
        addsd     xmm2, QWORD PTR [_vmldACosHATab+4440]
        subsd     xmm7, xmm15
        mulsd     xmm2, xmm6
        movsd     QWORD PTR [16+rbp], xmm7
        movsd     xmm0, QWORD PTR [16+rbp]
        addsd     xmm2, QWORD PTR [_vmldACosHATab+4432]
        subsd     xmm3, xmm0
        mulsd     xmm2, xmm6
        movsd     QWORD PTR [8+rbp], xmm3
        movsd     xmm7, QWORD PTR [16+rbp]
        movaps    xmm15, xmm7
        movaps    xmm0, xmm7
        mulsd     xmm15, xmm7
        addsd     xmm2, QWORD PTR [_vmldACosHATab+4424]
        addsd     xmm0, xmm6
        mulsd     xmm7, xmm4
        mulsd     xmm2, xmm6
        mulsd     xmm13, xmm15
        addsd     xmm2, QWORD PTR [_vmldACosHATab+4416]
        mulsd     xmm2, xmm6
        movsd     xmm3, QWORD PTR [8+rbp]
        movsd     QWORD PTR [16+rbp], xmm13
        mulsd     xmm0, xmm3
        addsd     xmm2, QWORD PTR [_vmldACosHATab+4408]
        mulsd     xmm4, xmm3
        mulsd     xmm2, xmm6
        addsd     xmm2, QWORD PTR [_vmldACosHATab+4400]
        mulsd     xmm2, xmm6
        addsd     xmm2, QWORD PTR [_vmldACosHATab+4392]
        mulsd     xmm2, xmm6
        addsd     xmm2, QWORD PTR [_vmldACosHATab+4384]
        mulsd     xmm2, xmm6
        addsd     xmm2, QWORD PTR [_vmldACosHATab+4376]
        mulsd     xmm2, xmm6
        addsd     xmm2, QWORD PTR [_vmldACosHATab+4368]
        mulsd     xmm2, xmm6
        mulsd     xmm2, xmm6
        mulsd     xmm2, xmm6
        addsd     xmm12, xmm2
        mulsd     xmm1, xmm12
        movsd     xmm12, QWORD PTR [16+rbp]
        subsd     xmm12, xmm15
        movsd     QWORD PTR [8+rbp], xmm12
        movsd     xmm13, QWORD PTR [16+rbp]
        movsd     xmm11, QWORD PTR [8+rbp]
        subsd     xmm13, xmm11
        movsd     QWORD PTR [16+rbp], xmm13
        movsd     xmm10, QWORD PTR [16+rbp]
        subsd     xmm15, xmm10
        movsd     QWORD PTR [8+rbp], xmm15
        movaps    xmm15, xmm7
        movsd     xmm11, QWORD PTR [16+rbp]
        mulsd     xmm11, QWORD PTR [_vmldACosHATab+4352]
        movsd     xmm13, QWORD PTR [8+rbp]
        addsd     xmm15, xmm11
        addsd     xmm13, xmm0
        movsd     QWORD PTR [16+rbp], xmm15
        movsd     xmm12, QWORD PTR [16+rbp]
        movsd     QWORD PTR [32+rbp], xmm11
        subsd     xmm7, xmm12
        mulsd     xmm13, QWORD PTR [_vmldACosHATab+4352]
        addsd     xmm7, xmm11
        movsd     QWORD PTR [8+rbp], xmm7
        movsd     xmm15, QWORD PTR [16+rbp]
        movsd     xmm7, QWORD PTR [_vmldACosHATab+4320]
        movsd     xmm12, QWORD PTR [8+rbp]
        addsd     xmm7, xmm15
        movsd     QWORD PTR [16+rbp], xmm7
        movsd     xmm10, QWORD PTR [16+rbp]
        movsd     xmm7, QWORD PTR [_vmldACosHATab+4320]
        movsd     xmm0, QWORD PTR [_vmldACosHATab+4360]
        subsd     xmm7, xmm10
        mulsd     xmm0, xmm6
        addsd     xmm7, xmm15
        addsd     xmm0, QWORD PTR [_vmldACosHATab+4344]
        movsd     QWORD PTR [8+rbp], xmm7
        movsd     xmm10, QWORD PTR [16+rbp]
        movsd     xmm15, QWORD PTR [_vmldACosHATab+4104]
        mulsd     xmm15, xmm10
        mulsd     xmm0, xmm6
        movsd     xmm11, QWORD PTR [8+rbp]
        movsd     QWORD PTR [16+rbp], xmm15
        movsd     xmm7, QWORD PTR [16+rbp]
        subsd     xmm7, xmm10
        addsd     xmm0, QWORD PTR [_vmldACosHATab+4328]
        movsd     QWORD PTR [8+rbp], xmm7
        addsd     xmm0, xmm2
        movsd     xmm7, QWORD PTR [16+rbp]
        movsd     xmm15, QWORD PTR [8+rbp]
        mulsd     xmm0, xmm5
        subsd     xmm7, xmm15
        movsd     QWORD PTR [16+rbp], xmm7
        movsd     xmm15, QWORD PTR [16+rbp]
        subsd     xmm10, xmm15
        movsd     QWORD PTR [8+rbp], xmm10
        movsd     xmm7, QWORD PTR [16+rbp]
        movsd     xmm10, QWORD PTR [8+rbp]
        movsd     xmm6, QWORD PTR [r8]
        addsd     xmm10, xmm12
        comisd    xmm6, QWORD PTR [_vmldACosHATab+4184]
        addsd     xmm10, xmm11
        addsd     xmm10, xmm4
        addsd     xmm10, xmm13
        mulsd     xmm10, xmm5
        mulsd     xmm5, xmm7
        addsd     xmm1, xmm10
        addsd     xmm1, xmm0
        ja        _B9_13

_B9_12::

        movsd     xmm3, QWORD PTR [_vmldACosHATab+4152]
        movaps    xmm0, xmm3
        movsd     xmm6, QWORD PTR [_vmldACosHATab+4160]
        subsd     xmm0, xmm5
        subsd     xmm6, xmm1
        movsd     QWORD PTR [16+rbp], xmm0
        movsd     xmm2, QWORD PTR [16+rbp]
        movsd     QWORD PTR [rbp], xmm5
        subsd     xmm3, xmm2
        subsd     xmm3, xmm5
        movsd     QWORD PTR [8+rbp], xmm3
        movsd     xmm5, QWORD PTR [16+rbp]
        movsd     xmm4, QWORD PTR [8+rbp]
        movaps    xmm1, xmm4
        addsd     xmm1, xmm6

_B9_13::

        addsd     xmm5, xmm1
        movsd     QWORD PTR [r9], xmm5
        jmp       _B9_17

_B9_14::

        ucomisd   xmm1, xmm0
        jp        _B9_15
        je        _B9_19

_B9_15::

        xorps     xmm0, XMMWORD PTR [_2il0floatpacket_200]
        ucomisd   xmm1, xmm0
        jp        _B9_16
        je        _B9_18

_B9_16::

        mov       eax, 1
        subsd     xmm1, xmm1
        divsd     xmm1, xmm1
        movsd     QWORD PTR [r9], xmm1

_B9_17::

        movups    xmm6, XMMWORD PTR [48+rbp]
        movups    xmm7, XMMWORD PTR [96+rbp]
        movups    xmm10, XMMWORD PTR [128+rbp]
        movups    xmm11, XMMWORD PTR [64+rbp]
        movups    xmm12, XMMWORD PTR [80+rbp]
        movups    xmm13, XMMWORD PTR [144+rbp]
        movups    xmm15, XMMWORD PTR [112+rbp]
        lea       rsp, QWORD PTR [160+rbp]
        pop       rbp
        ret

_B9_18::

        movsd     xmm0, QWORD PTR [_vmldACosHATab+4152]
        addsd     xmm0, QWORD PTR [_vmldACosHATab+4160]
        movsd     QWORD PTR [r9], xmm0
        jmp       _B9_17

_B9_19::

        mov       rdx, QWORD PTR [_vmldACosHATab+4184]
        mov       QWORD PTR [r9], rdx
        jmp       _B9_17

_B9_20::

        divsd     xmm1, xmm1
        movsd     QWORD PTR [r9], xmm1
        test      DWORD PTR [4+r8], 1048575
        jne       _B9_17

_B9_21::

        cmp       DWORD PTR [r8], 1
        setb      al
        jmp       _B9_17
        ALIGN     16

_B9_22::

__svml_dacos_ha_cout_rare_internal ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_dacos_ha_cout_rare_internal_B1_B21:
	DD	621958401
	DD	354389
	DD	555089
	DD	698445
	DD	440389
	DD	509995
	DD	776228
	DD	653337
	DD	17564433
	DD	1342504984

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B9_1
	DD	imagerel _B9_22
	DD	imagerel _unwind___svml_dacos_ha_cout_rare_internal_B1_B21

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_RDATA	SEGMENT     READ PAGE   'DATA'
	ALIGN  32
	PUBLIC __svml_dacos_ha_data_internal
__svml_dacos_ha_data_internal	DD	0
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
	DD	0
	DD	805306368
	DD	0
	DD	805306368
	DD	0
	DD	805306368
	DD	0
	DD	805306368
	DD	0
	DD	805306368
	DD	0
	DD	805306368
	DD	0
	DD	805306368
	DD	0
	DD	805306368
	DD	0
	DD	4294967040
	DD	0
	DD	4294967040
	DD	0
	DD	4294967040
	DD	0
	DD	4294967040
	DD	0
	DD	4294967040
	DD	0
	DD	4294967040
	DD	0
	DD	4294967040
	DD	0
	DD	4294967040
	DD	0
	DD	3220176896
	DD	0
	DD	3220176896
	DD	0
	DD	3220176896
	DD	0
	DD	3220176896
	DD	0
	DD	3220176896
	DD	0
	DD	3220176896
	DD	0
	DD	3220176896
	DD	0
	DD	3220176896
	DD	4227858432
	DD	4294967295
	DD	4227858432
	DD	4294967295
	DD	4227858432
	DD	4294967295
	DD	4227858432
	DD	4294967295
	DD	4227858432
	DD	4294967295
	DD	4227858432
	DD	4294967295
	DD	4227858432
	DD	4294967295
	DD	4227858432
	DD	4294967295
	DD	0
	DD	1073741824
	DD	0
	DD	1073741824
	DD	0
	DD	1073741824
	DD	0
	DD	1073741824
	DD	0
	DD	1073741824
	DD	0
	DD	1073741824
	DD	0
	DD	1073741824
	DD	0
	DD	1073741824
	DD	2570790083
	DD	3213983744
	DD	2570790083
	DD	3213983744
	DD	2570790083
	DD	3213983744
	DD	2570790083
	DD	3213983744
	DD	2570790083
	DD	3213983744
	DD	2570790083
	DD	3213983744
	DD	2570790083
	DD	3213983744
	DD	2570790083
	DD	3213983744
	DD	1869665325
	DD	1067712512
	DD	1869665325
	DD	1067712512
	DD	1869665325
	DD	1067712512
	DD	1869665325
	DD	1067712512
	DD	1869665325
	DD	1067712512
	DD	1869665325
	DD	1067712512
	DD	1869665325
	DD	1067712512
	DD	1869665325
	DD	1067712512
	DD	4294966935
	DD	3216506879
	DD	4294966935
	DD	3216506879
	DD	4294966935
	DD	3216506879
	DD	4294966935
	DD	3216506879
	DD	4294966935
	DD	3216506879
	DD	4294966935
	DD	3216506879
	DD	4294966935
	DD	3216506879
	DD	4294966935
	DD	3216506879
	DD	4294967197
	DD	1070596095
	DD	4294967197
	DD	1070596095
	DD	4294967197
	DD	1070596095
	DD	4294967197
	DD	1070596095
	DD	4294967197
	DD	1070596095
	DD	4294967197
	DD	1070596095
	DD	4294967197
	DD	1070596095
	DD	4294967197
	DD	1070596095
	DD	3339630857
	DD	1067480352
	DD	3339630857
	DD	1067480352
	DD	3339630857
	DD	1067480352
	DD	3339630857
	DD	1067480352
	DD	3339630857
	DD	1067480352
	DD	3339630857
	DD	1067480352
	DD	3339630857
	DD	1067480352
	DD	3339630857
	DD	1067480352
	DD	4158370029
	DD	3213949719
	DD	4158370029
	DD	3213949719
	DD	4158370029
	DD	3213949719
	DD	4158370029
	DD	3213949719
	DD	4158370029
	DD	3213949719
	DD	4158370029
	DD	3213949719
	DD	4158370029
	DD	3213949719
	DD	4158370029
	DD	3213949719
	DD	3216784302
	DD	1066680132
	DD	3216784302
	DD	1066680132
	DD	3216784302
	DD	1066680132
	DD	3216784302
	DD	1066680132
	DD	3216784302
	DD	1066680132
	DD	3216784302
	DD	1066680132
	DD	3216784302
	DD	1066680132
	DD	3216784302
	DD	1066680132
	DD	2513723093
	DD	1064982579
	DD	2513723093
	DD	1064982579
	DD	2513723093
	DD	1064982579
	DD	2513723093
	DD	1064982579
	DD	2513723093
	DD	1064982579
	DD	2513723093
	DD	1064982579
	DD	2513723093
	DD	1064982579
	DD	2513723093
	DD	1064982579
	DD	721210070
	DD	1065941212
	DD	721210070
	DD	1065941212
	DD	721210070
	DD	1065941212
	DD	721210070
	DD	1065941212
	DD	721210070
	DD	1065941212
	DD	721210070
	DD	1065941212
	DD	721210070
	DD	1065941212
	DD	721210070
	DD	1065941212
	DD	3414736215
	DD	1066167739
	DD	3414736215
	DD	1066167739
	DD	3414736215
	DD	1066167739
	DD	3414736215
	DD	1066167739
	DD	3414736215
	DD	1066167739
	DD	3414736215
	DD	1066167739
	DD	3414736215
	DD	1066167739
	DD	3414736215
	DD	1066167739
	DD	4114132270
	DD	1066518236
	DD	4114132270
	DD	1066518236
	DD	4114132270
	DD	1066518236
	DD	4114132270
	DD	1066518236
	DD	4114132270
	DD	1066518236
	DD	4114132270
	DD	1066518236
	DD	4114132270
	DD	1066518236
	DD	4114132270
	DD	1066518236
	DD	3957258973
	DD	1066854556
	DD	3957258973
	DD	1066854556
	DD	3957258973
	DD	1066854556
	DD	3957258973
	DD	1066854556
	DD	3957258973
	DD	1066854556
	DD	3957258973
	DD	1066854556
	DD	3957258973
	DD	1066854556
	DD	3957258973
	DD	1066854556
	DD	3778730174
	DD	1067392114
	DD	3778730174
	DD	1067392114
	DD	3778730174
	DD	1067392114
	DD	3778730174
	DD	1067392114
	DD	3778730174
	DD	1067392114
	DD	3778730174
	DD	1067392114
	DD	3778730174
	DD	1067392114
	DD	3778730174
	DD	1067392114
	DD	3014936056
	DD	1067899757
	DD	3014936056
	DD	1067899757
	DD	3014936056
	DD	1067899757
	DD	3014936056
	DD	1067899757
	DD	3014936056
	DD	1067899757
	DD	3014936056
	DD	1067899757
	DD	3014936056
	DD	1067899757
	DD	3014936056
	DD	1067899757
	DD	859300062
	DD	1068708659
	DD	859300062
	DD	1068708659
	DD	859300062
	DD	1068708659
	DD	859300062
	DD	1068708659
	DD	859300062
	DD	1068708659
	DD	859300062
	DD	1068708659
	DD	859300062
	DD	1068708659
	DD	859300062
	DD	1068708659
	DD	1431655068
	DD	1069897045
	DD	1431655068
	DD	1069897045
	DD	1431655068
	DD	1069897045
	DD	1431655068
	DD	1069897045
	DD	1431655068
	DD	1069897045
	DD	1431655068
	DD	1069897045
	DD	1431655068
	DD	1069897045
	DD	1431655068
	DD	1069897045
	DD	856972295
	DD	1017226790
	DD	856972295
	DD	1017226790
	DD	856972295
	DD	1017226790
	DD	856972295
	DD	1017226790
	DD	856972295
	DD	1017226790
	DD	856972295
	DD	1017226790
	DD	856972295
	DD	1017226790
	DD	856972295
	DD	1017226790
	DD	1413754136
	DD	1074340347
	DD	1413754136
	DD	1074340347
	DD	1413754136
	DD	1074340347
	DD	1413754136
	DD	1074340347
	DD	1413754136
	DD	1074340347
	DD	1413754136
	DD	1074340347
	DD	1413754136
	DD	1074340347
	DD	1413754136
	DD	1074340347
	DD	856972295
	DD	1016178214
	DD	856972295
	DD	1016178214
	DD	856972295
	DD	1016178214
	DD	856972295
	DD	1016178214
	DD	856972295
	DD	1016178214
	DD	856972295
	DD	1016178214
	DD	856972295
	DD	1016178214
	DD	856972295
	DD	1016178214
	DD	1413754136
	DD	1073291771
	DD	1413754136
	DD	1073291771
	DD	1413754136
	DD	1073291771
	DD	1413754136
	DD	1073291771
	DD	1413754136
	DD	1073291771
	DD	1413754136
	DD	1073291771
	DD	1413754136
	DD	1073291771
	DD	1413754136
	DD	1073291771
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
	DD	0
	DD	4294705152
	DD	0
	DD	4294705152
	DD	0
	DD	4294705152
	DD	0
	DD	4294705152
	DD	0
	DD	4294705152
	DD	0
	DD	4294705152
	DD	0
	DD	4294705152
	DD	0
	DD	4294705152
	DD	4131758366
	DD	1067674714
	DD	4131758366
	DD	1067674714
	DD	4131758366
	DD	1067674714
	DD	4131758366
	DD	1067674714
	DD	4131758366
	DD	1067674714
	DD	4131758366
	DD	1067674714
	DD	4131758366
	DD	1067674714
	DD	4131758366
	DD	1067674714
	DD	3936260738
	DD	1066197319
	DD	3936260738
	DD	1066197319
	DD	3936260738
	DD	1066197319
	DD	3936260738
	DD	1066197319
	DD	3936260738
	DD	1066197319
	DD	3936260738
	DD	1066197319
	DD	3936260738
	DD	1066197319
	DD	3936260738
	DD	1066197319
	DD	354394453
	DD	1067472564
	DD	354394453
	DD	1067472564
	DD	354394453
	DD	1067472564
	DD	354394453
	DD	1067472564
	DD	354394453
	DD	1067472564
	DD	354394453
	DD	1067472564
	DD	354394453
	DD	1067472564
	DD	354394453
	DD	1067472564
	DD	709676628
	DD	1067895021
	DD	709676628
	DD	1067895021
	DD	709676628
	DD	1067895021
	DD	709676628
	DD	1067895021
	DD	709676628
	DD	1067895021
	DD	709676628
	DD	1067895021
	DD	709676628
	DD	1067895021
	DD	709676628
	DD	1067895021
	DD	3958922090
	DD	1068708761
	DD	3958922090
	DD	1068708761
	DD	3958922090
	DD	1068708761
	DD	3958922090
	DD	1068708761
	DD	3958922090
	DD	1068708761
	DD	3958922090
	DD	1068708761
	DD	3958922090
	DD	1068708761
	DD	3958922090
	DD	1068708761
	DD	2160605765
	DD	1069897044
	DD	2160605765
	DD	1069897044
	DD	2160605765
	DD	1069897044
	DD	2160605765
	DD	1069897044
	DD	2160605765
	DD	1069897044
	DD	2160605765
	DD	1069897044
	DD	2160605765
	DD	1069897044
	DD	2160605765
	DD	1069897044
	DD	0
	DD	2146435072
	DD	0
	DD	4293918720
	DB 0
	ORG $+46
	DB	0
	DD	0
	DD	1072693248
	DD	0
	DD	3220176896
	DB 0
	ORG $+46
	DB	0
	DD	0
	DD	0
	DD	0
	DD	2147483648
	DB 0
	ORG $+46
	DB	0
_vmldACosHATab	DD	0
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
	DD	1017118720
	DD	1413754136
	DD	1073291771
	DD	856972295
	DD	1016178214
	DD	1413754136
	DD	1074340347
	DD	856972295
	DD	1017226790
	DD	0
	DD	1072693248
	DD	0
	DD	1071644672
	DD	0
	DD	0
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
	DD 22 DUP (0H)	
_2il0floatpacket_200	DD	000000000H,080000000H,000000000H,000000000H
_RDATA	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS
EXTRN	__ImageBase:PROC
EXTRN	_fltused:BYTE
	ENDIF
	END
