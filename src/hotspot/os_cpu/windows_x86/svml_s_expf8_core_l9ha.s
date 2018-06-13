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

INCLUDE globals_vectorApiSupport_windows.hpp
IFNB __VECTOR_API_MATH_INTRINSICS_WINDOWS

	OPTION DOTNAME

_TEXT	SEGMENT      'CODE'

TXTST0:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_expf8_ha_l9

__svml_expf8_ha_l9	PROC

$B1$1::

        DB        243
        DB        15
        DB        30
        DB        250
L1::

        sub       rsp, 552
        vmovups   YMMWORD PTR [496+rsp], ymm14
        mov       QWORD PTR [528+rsp], r13
        lea       r13, QWORD PTR [399+rsp]
        vmovups   ymm14, YMMWORD PTR [__svml_sexp_ha_data_internal]
        and       r13, -64
        vmovups   ymm4, YMMWORD PTR [__svml_sexp_ha_data_internal+64]
        vmovups   ymm2, YMMWORD PTR [__svml_sexp_ha_data_internal+256]
        vmovdqa   ymm5, ymm0
        vfmadd213ps ymm14, ymm5, ymm4
        vmovups   ymm0, YMMWORD PTR [__svml_sexp_ha_data_internal+448]
        vsubps    ymm4, ymm14, ymm4
        vandps    ymm1, ymm5, YMMWORD PTR [__svml_sexp_ha_data_internal+128]
        vcmpnle_uqps ymm3, ymm1, YMMWORD PTR [__svml_sexp_ha_data_internal+192]
        vfnmadd213ps ymm2, ymm4, ymm5
        vmovups   ymm1, YMMWORD PTR [__svml_sexp_ha_data_internal+384]
        vfnmadd132ps ymm4, ymm2, YMMWORD PTR [__svml_sexp_ha_data_internal+320]
        vmovmskps edx, ymm3
        vpermilps ymm3, ymm1, ymm14
        test      edx, edx
        vmovups   ymm1, YMMWORD PTR [__svml_sexp_ha_data_internal+512]
        vfmadd213ps ymm1, ymm4, YMMWORD PTR [__svml_sexp_ha_data_internal+576]
        vfmadd213ps ymm1, ymm4, YMMWORD PTR [__svml_sexp_ha_data_internal+640]
        vpermilps ymm2, ymm0, ymm14
        vmulps    ymm0, ymm4, ymm4
        vpslld    ymm14, ymm14, 21
        vfmadd213ps ymm1, ymm0, ymm3
        vandps    ymm14, ymm14, YMMWORD PTR [__svml_sexp_ha_data_internal+704]
        vmulps    ymm2, ymm2, ymm14
        vaddps    ymm0, ymm4, ymm1
        mov       QWORD PTR [536+rsp], r13
        vfmadd213ps ymm0, ymm2, ymm2
        jne       $B1$3

$B1$2::

        vmovups   ymm14, YMMWORD PTR [496+rsp]
        mov       r13, QWORD PTR [528+rsp]
        add       rsp, 552
        ret

$B1$3::

        vcmpgt_oqps ymm1, ymm5, YMMWORD PTR [__svml_sexp_ha_data_internal+2560]
        vcmplt_oqps ymm2, ymm5, YMMWORD PTR [__svml_sexp_ha_data_internal+2624]
        vblendvps ymm0, ymm0, YMMWORD PTR [_2il0floatpacket$14], ymm1
        vorps     ymm3, ymm1, ymm2
        vmovmskps eax, ymm3
        vandnps   ymm0, ymm2, ymm0
        andn      edx, eax, edx
        je        $B1$2

$B1$4::

        vmovups   YMMWORD PTR [r13], ymm5
        vmovups   YMMWORD PTR [64+r13], ymm0
        je        $B1$2

$B1$7::

        xor       eax, eax
        vmovups   YMMWORD PTR [288+rsp], ymm6
        vmovups   YMMWORD PTR [256+rsp], ymm7
        vmovups   YMMWORD PTR [224+rsp], ymm8
        vmovups   YMMWORD PTR [192+rsp], ymm9
        vmovups   YMMWORD PTR [160+rsp], ymm10
        vmovups   YMMWORD PTR [128+rsp], ymm11
        vmovups   YMMWORD PTR [96+rsp], ymm12
        vmovups   YMMWORD PTR [64+rsp], ymm13
        vmovups   YMMWORD PTR [32+rsp], ymm15
        mov       QWORD PTR [328+rsp], rbx
        mov       ebx, eax
        mov       QWORD PTR [320+rsp], rsi
        mov       esi, edx

$B1$8::

        bt        esi, ebx
        jc        $B1$11

$B1$9::

        inc       ebx
        cmp       ebx, 8
        jl        $B1$8

$B1$10::

        vmovups   ymm6, YMMWORD PTR [288+rsp]
        vmovups   ymm7, YMMWORD PTR [256+rsp]
        vmovups   ymm8, YMMWORD PTR [224+rsp]
        vmovups   ymm9, YMMWORD PTR [192+rsp]
        vmovups   ymm10, YMMWORD PTR [160+rsp]
        vmovups   ymm11, YMMWORD PTR [128+rsp]
        vmovups   ymm12, YMMWORD PTR [96+rsp]
        vmovups   ymm13, YMMWORD PTR [64+rsp]
        vmovups   ymm15, YMMWORD PTR [32+rsp]
        vmovups   ymm0, YMMWORD PTR [64+r13]
        mov       rbx, QWORD PTR [328+rsp]
        mov       rsi, QWORD PTR [320+rsp]
        jmp       $B1$2

$B1$11::

        vzeroupper
        lea       rcx, QWORD PTR [r13+rbx*4]
        lea       rdx, QWORD PTR [64+r13+rbx*4]

        call      __svml_sexp_ha_cout_rare_internal
        jmp       $B1$9
        ALIGN     16

$B1$12::

__svml_expf8_ha_l9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
$unwind$__svml_expf8_ha_l9$B1_B4	DD	400385
	DD	4379676
	DD	2091028
	DD	4522251

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
$pdata$__svml_expf8_ha_l9$B1_B4	DD	imagerel $B1$1
	DD	imagerel $B1$7
	DD	imagerel $unwind$__svml_expf8_ha_l9$B1_B4

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
$unwind$__svml_expf8_ha_l9$B7_B11	DD	1858561
	DD	2647132
	DD	2700370
	DD	194634
	DD	317508
	DD	444478
	DD	571448
	DD	698415
	DD	825382
	DD	952349
	DD	1079316
	DD	1206283
	DD	2091008
	DD	4379648
	DD	4522240

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
$pdata$__svml_expf8_ha_l9$B7_B11	DD	imagerel $B1$7
	DD	imagerel $B1$12
	DD	imagerel $unwind$__svml_expf8_ha_l9$B7_B11

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_RDATA	SEGMENT     READ PAGE   'DATA'
	ALIGN  32
_2il0floatpacket$14	DD	07f800000H,07f800000H,07f800000H,07f800000H,07f800000H,07f800000H,07f800000H,07f800000H
_RDATA	ENDS
_DATA	SEGMENT      'DATA'
EXTRN	__svml_sexp_ha_data_internal:BYTE
_DATA	ENDS
EXTRN	__svml_sexp_ha_cout_rare_internal:PROC
EXTRN	__ImageBase:PROC
EXTRN	_fltused:BYTE

ENDIF

	END