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
	PUBLIC __svml_expf4_ha_l9

__svml_expf4_ha_l9	PROC

$B1$1::

        DB        243
        DB        15
        DB        30
        DB        250
L1::

        sub       rsp, 232
        vmovaps   xmm5, xmm0
        vmovups   XMMWORD PTR [192+rsp], xmm14
        mov       QWORD PTR [208+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovups   xmm14, XMMWORD PTR [__svml_sexp_ha_data_internal]
        and       r13, -64
        vmovups   xmm4, XMMWORD PTR [__svml_sexp_ha_data_internal+64]
        vfmadd213ps xmm14, xmm5, xmm4
        vmovups   xmm2, XMMWORD PTR [__svml_sexp_ha_data_internal+256]
        vandps    xmm3, xmm5, XMMWORD PTR [__svml_sexp_ha_data_internal+128]
        vmovups   xmm0, XMMWORD PTR [__svml_sexp_ha_data_internal+384]
        vcmpnleps xmm1, xmm3, XMMWORD PTR [__svml_sexp_ha_data_internal+192]
        vsubps    xmm4, xmm14, xmm4
        vmovmskps edx, xmm1
        vmovups   xmm1, XMMWORD PTR [__svml_sexp_ha_data_internal+448]
        vfnmadd213ps xmm2, xmm4, xmm5
        vfnmadd132ps xmm4, xmm2, XMMWORD PTR [__svml_sexp_ha_data_internal+320]
        vpermilps xmm3, xmm0, xmm14
        vmovups   xmm0, XMMWORD PTR [__svml_sexp_ha_data_internal+512]
        vfmadd213ps xmm0, xmm4, XMMWORD PTR [__svml_sexp_ha_data_internal+576]
        vmulps    xmm2, xmm4, xmm4
        vfmadd213ps xmm0, xmm4, XMMWORD PTR [__svml_sexp_ha_data_internal+640]
        vfmadd213ps xmm0, xmm2, xmm3
        vpermilps xmm1, xmm1, xmm14
        vpslld    xmm14, xmm14, 21
        vandps    xmm14, xmm14, XMMWORD PTR [__svml_sexp_ha_data_internal+704]
        vaddps    xmm0, xmm4, xmm0
        vmulps    xmm2, xmm1, xmm14
        mov       QWORD PTR [216+rsp], r13
        vfmadd213ps xmm0, xmm2, xmm2
        test      edx, edx
        jne       $B1$3

$B1$2::

        vmovups   xmm14, XMMWORD PTR [192+rsp]
        mov       r13, QWORD PTR [208+rsp]
        add       rsp, 232
        ret

$B1$3::

        vcmpgtps  xmm1, xmm5, XMMWORD PTR [__svml_sexp_ha_data_internal+2560]
        vcmpltps  xmm2, xmm5, XMMWORD PTR [__svml_sexp_ha_data_internal+2624]
        vblendvps xmm0, xmm0, XMMWORD PTR [_2il0floatpacket$14], xmm1
        vorps     xmm3, xmm1, xmm2
        vmovmskps eax, xmm3
        vandnps   xmm0, xmm2, xmm0
        andn      edx, eax, edx
        je        $B1$2

$B1$4::

        vmovups   XMMWORD PTR [r13], xmm5
        vmovups   XMMWORD PTR [64+r13], xmm0
        je        $B1$2

$B1$7::

        xor       eax, eax
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, eax
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, edx

$B1$8::

        bt        esi, ebx
        jc        $B1$11

$B1$9::

        inc       ebx
        cmp       ebx, 4
        jl        $B1$8

$B1$10::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        vmovups   xmm0, XMMWORD PTR [64+r13]
        jmp       $B1$2

$B1$11::

        lea       rcx, QWORD PTR [r13+rbx*4]
        lea       rdx, QWORD PTR [64+r13+rbx*4]

        call      __svml_sexp_ha_cout_rare_internal
        jmp       $B1$9
        ALIGN     16

$B1$12::

__svml_expf4_ha_l9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
$unwind$__svml_expf4_ha_l9$B1_B4	DD	401409
	DD	1758240
	DD	845848
	DD	1900811

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
$pdata$__svml_expf4_ha_l9$B1_B4	DD	imagerel $B1$1
	DD	imagerel $B1$7
	DD	imagerel $unwind$__svml_expf4_ha_l9$B1_B4

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
$unwind$__svml_expf4_ha_l9$B7_B11	DD	658945
	DD	287758
	DD	340999
	DD	845824
	DD	1758208
	DD	1900800

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
$pdata$__svml_expf4_ha_l9$B7_B11	DD	imagerel $B1$7
	DD	imagerel $B1$12
	DD	imagerel $unwind$__svml_expf4_ha_l9$B7_B11

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_RDATA	SEGMENT     READ PAGE   'DATA'
	ALIGN  32
_2il0floatpacket$14	DD	07f800000H,07f800000H,07f800000H,07f800000H
_RDATA	ENDS
_DATA	SEGMENT      'DATA'
EXTRN	__svml_sexp_ha_data_internal:BYTE
_DATA	ENDS
EXTRN	__svml_sexp_ha_cout_rare_internal:PROC
EXTRN	__ImageBase:PROC
EXTRN	_fltused:BYTE

ENDIF

	END