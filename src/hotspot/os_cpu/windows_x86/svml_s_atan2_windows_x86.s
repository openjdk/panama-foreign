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
	PUBLIC __svml_atan2f4_ha_l9

__svml_atan2f4_ha_l9	PROC

_B1_1::

        DB        243
        DB        15
        DB        30
        DB        250
L1::

        sub       rsp, 488
        vmovups   XMMWORD PTR [368+rsp], xmm15
        vmovups   XMMWORD PTR [400+rsp], xmm14
        vmovups   XMMWORD PTR [352+rsp], xmm13
        vmovups   XMMWORD PTR [256+rsp], xmm12
        vmovups   XMMWORD PTR [272+rsp], xmm11
        vmovups   XMMWORD PTR [288+rsp], xmm10
        vmovups   XMMWORD PTR [304+rsp], xmm9
        vmovups   XMMWORD PTR [320+rsp], xmm8
        vmovups   XMMWORD PTR [336+rsp], xmm7
        vmovups   XMMWORD PTR [384+rsp], xmm6
        mov       QWORD PTR [464+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovups   xmm10, XMMWORD PTR [rdx]
        and       r13, -64
        vmovups   xmm8, XMMWORD PTR [__svml_satan2_ha_data_internal+192]
        xor       edx, edx
        vmovups   xmm5, XMMWORD PTR [rcx]
        vandps    xmm9, xmm10, xmm8
        vmovups   xmm0, XMMWORD PTR [__svml_satan2_ha_data_internal+1728]
        vandps    xmm8, xmm5, xmm8
        vmovups   xmm15, XMMWORD PTR [__svml_satan2_ha_data_internal+1792]
        vpsubd    xmm1, xmm9, xmm0
        vxorps    xmm4, xmm5, xmm8
        vxorps    xmm2, xmm10, xmm9
        vmovups   XMMWORD PTR [416+rsp], xmm4
        vpsubd    xmm12, xmm8, xmm0
        vmovups   XMMWORD PTR [432+rsp], xmm2
        vpcmpgtd  xmm3, xmm1, xmm15
        vmulps    xmm4, xmm9, XMMWORD PTR [__svml_satan2_ha_data_internal+384]
        vpcmpeqd  xmm2, xmm1, xmm15
        vmulps    xmm6, xmm9, XMMWORD PTR [__svml_satan2_ha_data_internal+448]
        vmulps    xmm0, xmm9, XMMWORD PTR [__svml_satan2_ha_data_internal+512]
        vpor      xmm13, xmm3, xmm2
        vpcmpgtd  xmm14, xmm12, xmm15
        vmulps    xmm3, xmm9, XMMWORD PTR [__svml_satan2_ha_data_internal+576]
        vpcmpeqd  xmm11, xmm12, xmm15
        vmovups   XMMWORD PTR [32+rsp], xmm5
        vpor      xmm5, xmm14, xmm11
        vpcmpgtd  xmm1, xmm4, xmm8
        vpcmpgtd  xmm2, xmm6, xmm8
        vpcmpgtd  xmm12, xmm0, xmm8
        vpor      xmm7, xmm13, xmm5
        vpcmpgtd  xmm5, xmm8, xmm3
        vpandn    xmm6, xmm1, xmm2
        vpandn    xmm4, xmm2, xmm12
        vmovdqa   xmm3, xmm9
        vmovups   xmm2, XMMWORD PTR [__svml_satan2_ha_data_internal+704]
        vmovdqa   xmm14, xmm9
        vfnmadd213ps xmm3, xmm2, xmm8
        vpxor     xmm15, xmm5, XMMWORD PTR [__svml_satan2_ha_data_internal+1600]
        vmovdqu   XMMWORD PTR [448+rsp], xmm7
        vpandn    xmm7, xmm12, xmm15
        vmovups   xmm0, XMMWORD PTR [__svml_satan2_ha_data_internal+640]
        vmovdqa   xmm15, xmm9
        vandps    xmm12, xmm8, xmm1
        vandps    xmm1, xmm9, xmm1
        vandps    xmm11, xmm3, xmm4
        vmovups   xmm3, XMMWORD PTR [__svml_satan2_ha_data_internal+768]
        vfnmadd213ps xmm14, xmm0, xmm8
        vfnmadd213ps xmm15, xmm3, xmm8
        vfmadd213ps xmm0, xmm8, xmm9
        vfmadd213ps xmm2, xmm8, xmm9
        vfmadd213ps xmm3, xmm8, xmm9
        vandps    xmm13, xmm14, xmm6
        vandps    xmm14, xmm15, xmm7
        vorps     xmm13, xmm12, xmm13
        vxorps    xmm15, xmm9, XMMWORD PTR [__svml_satan2_ha_data_internal+128]
        vorps     xmm11, xmm11, xmm14
        vandps    xmm15, xmm15, xmm5
        vorps     xmm12, xmm13, xmm11
        vorps     xmm12, xmm12, xmm15
        vandps    xmm15, xmm0, xmm6
        vandps    xmm2, xmm2, xmm4
        vandps    xmm3, xmm3, xmm7
        vorps     xmm14, xmm1, xmm15
        vorps     xmm11, xmm2, xmm3
        vandps    xmm1, xmm6, XMMWORD PTR [__svml_satan2_ha_data_internal+832]
        vandps    xmm0, xmm8, xmm5
        vandps    xmm2, xmm4, XMMWORD PTR [__svml_satan2_ha_data_internal+960]
        vorps     xmm13, xmm14, xmm11
        vandps    xmm15, xmm7, XMMWORD PTR [__svml_satan2_ha_data_internal+1088]
        vorps     xmm3, xmm13, xmm0
        vandps    xmm14, xmm5, XMMWORD PTR [__svml_satan2_ha_data_internal+1216]
        vorps     xmm11, xmm1, xmm2
        vorps     xmm13, xmm15, xmm14
        vmovdqa   xmm14, xmm3
        vorps     xmm15, xmm11, xmm13
        vrcpps    xmm11, xmm3
        vmovups   xmm2, XMMWORD PTR [__svml_satan2_ha_data_internal+64]
        vfnmadd231ps xmm2, xmm3, xmm11
        vandps    xmm5, xmm5, XMMWORD PTR [__svml_satan2_ha_data_internal+1280]
        vandps    xmm4, xmm4, XMMWORD PTR [__svml_satan2_ha_data_internal+1024]
        vmovups   xmm1, XMMWORD PTR [__svml_satan2_ha_data_internal+1408]
        vfmadd213ps xmm2, xmm2, xmm2
        vfmadd213ps xmm11, xmm2, xmm11
        vmulps    xmm0, xmm12, xmm11
        vfnmadd213ps xmm14, xmm0, xmm12
        vfmadd213ps xmm14, xmm11, xmm0
        vfnmadd213ps xmm3, xmm14, xmm12
        vandps    xmm12, xmm6, XMMWORD PTR [__svml_satan2_ha_data_internal+896]
        vandps    xmm6, xmm7, XMMWORD PTR [__svml_satan2_ha_data_internal+1152]
        vorps     xmm7, xmm12, xmm4
        vmulps    xmm13, xmm11, xmm3
        vorps     xmm3, xmm6, xmm5
        vmulps    xmm5, xmm14, xmm14
        vmulps    xmm4, xmm5, xmm5
        vorps     xmm2, xmm7, xmm3
        vaddps    xmm11, xmm2, xmm13
        vmovups   xmm6, XMMWORD PTR [__svml_satan2_ha_data_internal+1344]
        vfmadd213ps xmm6, xmm4, XMMWORD PTR [__svml_satan2_ha_data_internal+1472]
        vfmadd213ps xmm4, xmm1, XMMWORD PTR [__svml_satan2_ha_data_internal+1536]
        vmovups   xmm3, XMMWORD PTR [__svml_satan2_ha_data_internal]
        vmovups   xmm2, XMMWORD PTR [432+rsp]
        vcmpltps  xmm0, xmm10, xmm3
        vfmadd213ps xmm6, xmm5, xmm4
        vandps    xmm7, xmm0, XMMWORD PTR [__svml_satan2_ha_data_internal+320]
        vxorps    xmm13, xmm7, xmm2
        vandps    xmm12, xmm0, XMMWORD PTR [__svml_satan2_ha_data_internal+256]
        vaddps    xmm11, xmm13, xmm11
        vmulps    xmm0, xmm5, xmm6
        vfmadd213ps xmm0, xmm14, xmm11
        mov       QWORD PTR [472+rsp], r13
        vaddps    xmm14, xmm14, xmm0
        vaddps    xmm15, xmm15, xmm14
        vmovdqu   xmm14, XMMWORD PTR [448+rsp]
        vmovmskps eax, xmm14
        vorps     xmm0, xmm15, xmm2
        test      eax, eax
        vaddps    xmm12, xmm12, xmm0
        vorps     xmm0, xmm12, XMMWORD PTR [416+rsp]
        jne       _B1_12

_B1_2::

        test      edx, edx
        jne       _B1_4

_B1_3::

        vmovups   xmm6, XMMWORD PTR [384+rsp]
        vmovups   xmm7, XMMWORD PTR [336+rsp]
        vmovups   xmm8, XMMWORD PTR [320+rsp]
        vmovups   xmm9, XMMWORD PTR [304+rsp]
        vmovups   xmm10, XMMWORD PTR [288+rsp]
        vmovups   xmm11, XMMWORD PTR [272+rsp]
        vmovups   xmm12, XMMWORD PTR [256+rsp]
        vmovups   xmm13, XMMWORD PTR [352+rsp]
        vmovups   xmm14, XMMWORD PTR [400+rsp]
        vmovups   xmm15, XMMWORD PTR [368+rsp]
        mov       r13, QWORD PTR [464+rsp]
        add       rsp, 488
        ret

_B1_4::

        vmovups   xmm1, XMMWORD PTR [32+rsp]
        vmovups   XMMWORD PTR [r13], xmm1
        vmovups   XMMWORD PTR [64+r13], xmm10
        vmovups   XMMWORD PTR [128+r13], xmm0
        je        _B1_3

_B1_7::

        xor       eax, eax
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, eax
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, edx

_B1_8::

        bt        esi, ebx
        jc        _B1_11

_B1_9::

        inc       ebx
        cmp       ebx, 4
        jl        _B1_8

_B1_10::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        vmovups   xmm0, XMMWORD PTR [128+r13]
        jmp       _B1_3

_B1_11::

        lea       rcx, QWORD PTR [r13+rbx*4]
        lea       rdx, QWORD PTR [64+r13+rbx*4]
        lea       r8, QWORD PTR [128+r13+rbx*4]

        call      __svml_satan2_ha_cout_rare_internal
        jmp       _B1_9

_B1_12::

        vcmpltps  xmm15, xmm8, xmm9
        vcmpordps xmm5, xmm10, xmm10
        vpcmpeqd  xmm11, xmm8, xmm3
        vpcmpeqd  xmm7, xmm9, xmm3
        vblendvps xmm8, xmm8, xmm9, xmm15
        vpor      xmm12, xmm7, xmm11
        vmovups   xmm4, XMMWORD PTR [32+rsp]
        vcmpordps xmm6, xmm4, xmm4
        vcmpeqps  xmm4, xmm8, xmm3
        vandnps   xmm9, xmm15, XMMWORD PTR [__svml_satan2_ha_data_internal+1856]
        vandps    xmm13, xmm5, xmm6
        vblendvps xmm5, xmm9, xmm3, xmm4
        vpcmpgtd  xmm3, xmm3, xmm10
        vandps    xmm3, xmm3, XMMWORD PTR [__svml_satan2_ha_data_internal+1920]
        vpand     xmm1, xmm12, xmm13
        vorps     xmm2, xmm5, xmm2
        vpandn    xmm14, xmm1, xmm14
        vmovmskps edx, xmm14
        vaddps    xmm2, xmm2, xmm3
        vorps     xmm4, xmm2, XMMWORD PTR [416+rsp]
        vblendvps xmm0, xmm0, xmm4, xmm1
        jmp       _B1_2
        ALIGN     16

_B1_13::

__svml_atan2f4_ha_l9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_atan2f4_ha_l9_B1_B4:
	DD	1600769
	DD	3855469
	DD	1599589
	DD	1407068
	DD	1345619
	DD	1284170
	DD	1222721
	DD	1161272
	DD	1099823
	DD	1497126
	DD	1697821
	DD	1570836
	DD	3997963

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B1_1
	DD	imagerel _B1_7
	DD	imagerel _unwind___svml_atan2f4_ha_l9_B1_B4

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_atan2f4_ha_l9_B7_B11:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B1_1
	DD	imagerel _B1_7
	DD	imagerel _unwind___svml_atan2f4_ha_l9_B1_B4

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B1_7
	DD	imagerel _B1_12
	DD	imagerel _unwind___svml_atan2f4_ha_l9_B7_B11

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_atan2f4_ha_l9_B12_B12:
	DD	33
	DD	imagerel _B1_1
	DD	imagerel _B1_7
	DD	imagerel _unwind___svml_atan2f4_ha_l9_B1_B4

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B1_12
	DD	imagerel _B1_13
	DD	imagerel _unwind___svml_atan2f4_ha_l9_B12_B12

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST1:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_atan2f4_ha_e9

__svml_atan2f4_ha_e9	PROC

_B2_1::

        DB        243
        DB        15
        DB        30
        DB        250
L38::

        sub       rsp, 488
        vmovups   XMMWORD PTR [368+rsp], xmm15
        vmovups   XMMWORD PTR [400+rsp], xmm14
        vmovups   XMMWORD PTR [352+rsp], xmm13
        vmovups   XMMWORD PTR [304+rsp], xmm12
        vmovups   XMMWORD PTR [320+rsp], xmm11
        vmovups   XMMWORD PTR [256+rsp], xmm10
        vmovups   XMMWORD PTR [272+rsp], xmm9
        vmovups   XMMWORD PTR [288+rsp], xmm8
        vmovups   XMMWORD PTR [336+rsp], xmm7
        vmovups   XMMWORD PTR [384+rsp], xmm6
        mov       QWORD PTR [464+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovups   xmm1, XMMWORD PTR [rcx]
        and       r13, -64
        vmovups   xmm12, XMMWORD PTR [rdx]
        xor       edx, edx
        vmovups   xmm10, XMMWORD PTR [__svml_satan2_ha_data_internal+192]
        vandps    xmm11, xmm12, xmm10
        vandps    xmm10, xmm1, xmm10
        vmovups   XMMWORD PTR [32+rsp], xmm1
        vxorps    xmm3, xmm1, xmm10
        vmovups   xmm2, XMMWORD PTR [__svml_satan2_ha_data_internal+1728]
        vxorps    xmm5, xmm12, xmm11
        vmulps    xmm1, xmm11, XMMWORD PTR [__svml_satan2_ha_data_internal+384]
        vpsubd    xmm6, xmm11, xmm2
        vmovups   XMMWORD PTR [416+rsp], xmm3
        vpsubd    xmm15, xmm10, xmm2
        vmovups   xmm14, XMMWORD PTR [__svml_satan2_ha_data_internal+1792]
        vmulps    xmm3, xmm11, XMMWORD PTR [__svml_satan2_ha_data_internal+448]
        vpcmpgtd  xmm9, xmm6, xmm14
        vmulps    xmm2, xmm11, XMMWORD PTR [__svml_satan2_ha_data_internal+512]
        vpcmpeqd  xmm4, xmm6, xmm14
        vpcmpgtd  xmm13, xmm15, xmm14
        vpcmpeqd  xmm8, xmm15, xmm14
        vmovups   XMMWORD PTR [432+rsp], xmm5
        vpor      xmm5, xmm9, xmm4
        vpor      xmm7, xmm13, xmm8
        vpcmpgtd  xmm9, xmm3, xmm10
        vpor      xmm0, xmm5, xmm7
        vpcmpgtd  xmm4, xmm2, xmm10
        vmovdqu   XMMWORD PTR [448+rsp], xmm0
        vpcmpgtd  xmm0, xmm1, xmm10
        vmovups   xmm1, XMMWORD PTR [__svml_satan2_ha_data_internal+640]
        vpandn    xmm7, xmm0, xmm9
        vmulps    xmm3, xmm11, xmm1
        vpandn    xmm5, xmm9, xmm4
        vmulps    xmm6, xmm11, XMMWORD PTR [__svml_satan2_ha_data_internal+576]
        vmulps    xmm1, xmm10, xmm1
        vsubps    xmm14, xmm10, xmm3
        vmovups   xmm2, XMMWORD PTR [__svml_satan2_ha_data_internal+704]
        vpcmpgtd  xmm6, xmm10, xmm6
        vmulps    xmm3, xmm11, xmm2
        vandps    xmm13, xmm10, xmm0
        vmulps    xmm2, xmm10, xmm2
        vsubps    xmm9, xmm10, xmm3
        vmovups   xmm3, XMMWORD PTR [__svml_satan2_ha_data_internal+768]
        vandps    xmm14, xmm14, xmm7
        vpxor     xmm15, xmm6, XMMWORD PTR [__svml_satan2_ha_data_internal+1600]
        vorps     xmm13, xmm13, xmm14
        vpandn    xmm8, xmm4, xmm15
        vandps    xmm15, xmm9, xmm5
        vmulps    xmm4, xmm11, xmm3
        vandps    xmm0, xmm11, xmm0
        vmulps    xmm3, xmm10, xmm3
        vsubps    xmm9, xmm10, xmm4
        vaddps    xmm14, xmm11, xmm3
        vandps    xmm4, xmm9, xmm8
        vxorps    xmm9, xmm11, XMMWORD PTR [__svml_satan2_ha_data_internal+128]
        vorps     xmm4, xmm15, xmm4
        vandps    xmm9, xmm9, xmm6
        vorps     xmm15, xmm13, xmm4
        vorps     xmm4, xmm15, xmm9
        vaddps    xmm9, xmm11, xmm1
        vaddps    xmm15, xmm11, xmm2
        vandps    xmm13, xmm9, xmm7
        vandps    xmm9, xmm15, xmm5
        vandps    xmm1, xmm14, xmm8
        vorps     xmm0, xmm0, xmm13
        vorps     xmm3, xmm9, xmm1
        vandps    xmm15, xmm10, xmm6
        vorps     xmm2, xmm0, xmm3
        vorps     xmm14, xmm2, xmm15
        vcvtps2pd ymm15, xmm4
        vandps    xmm9, xmm7, XMMWORD PTR [__svml_satan2_ha_data_internal+832]
        vandps    xmm13, xmm5, XMMWORD PTR [__svml_satan2_ha_data_internal+960]
        vandps    xmm0, xmm8, XMMWORD PTR [__svml_satan2_ha_data_internal+1088]
        vorps     xmm3, xmm9, xmm13
        vrcpps    xmm4, xmm14
        vandps    xmm1, xmm6, XMMWORD PTR [__svml_satan2_ha_data_internal+1216]
        vorps     xmm2, xmm0, xmm1
        vcvtps2pd ymm4, xmm4
        vorps     xmm9, xmm3, xmm2
        vcvtps2pd ymm3, xmm14
        vandps    xmm8, xmm8, XMMWORD PTR [__svml_satan2_ha_data_internal+1152]
        vandps    xmm7, xmm7, XMMWORD PTR [__svml_satan2_ha_data_internal+896]
        vandps    xmm5, xmm5, XMMWORD PTR [__svml_satan2_ha_data_internal+1024]
        vmovupd   ymm2, YMMWORD PTR [__svml_satan2_ha_data_internal+1664]
        vmulpd    ymm14, ymm3, ymm4
        vsubpd    ymm14, ymm2, ymm14
        vmulpd    ymm3, ymm4, ymm14
        vmulpd    ymm13, ymm14, ymm14
        vaddpd    ymm0, ymm4, ymm3
        vaddpd    ymm2, ymm2, ymm13
        vmulpd    ymm1, ymm0, ymm2
        vmulpd    ymm15, ymm15, ymm1
        vcvtpd2ps xmm4, ymm15
        vcvtps2pd ymm3, xmm4
        vsubpd    ymm2, ymm15, ymm3
        vcvtpd2ps xmm3, ymm2
        vandps    xmm2, xmm6, XMMWORD PTR [__svml_satan2_ha_data_internal+1280]
        vorps     xmm6, xmm7, xmm5
        vorps     xmm15, xmm8, xmm2
        vorps     xmm14, xmm6, xmm15
        mov       QWORD PTR [472+rsp], r13
        vaddps    xmm15, xmm3, xmm14
        vmovups   xmm3, XMMWORD PTR [__svml_satan2_ha_data_internal]
        vcmpltps  xmm13, xmm12, xmm3
        vandps    xmm6, xmm13, XMMWORD PTR [__svml_satan2_ha_data_internal+256]
        vandps    xmm14, xmm13, XMMWORD PTR [__svml_satan2_ha_data_internal+320]
        vmulps    xmm13, xmm4, xmm4
        vmulps    xmm1, xmm13, xmm13
        vmulps    xmm0, xmm1, XMMWORD PTR [__svml_satan2_ha_data_internal+1344]
        vmulps    xmm5, xmm1, XMMWORD PTR [__svml_satan2_ha_data_internal+1408]
        vaddps    xmm7, xmm0, XMMWORD PTR [__svml_satan2_ha_data_internal+1472]
        vaddps    xmm8, xmm5, XMMWORD PTR [__svml_satan2_ha_data_internal+1536]
        vmulps    xmm2, xmm13, xmm7
        vaddps    xmm0, xmm8, xmm2
        vmovups   xmm2, XMMWORD PTR [432+rsp]
        vmulps    xmm1, xmm13, xmm0
        vxorps    xmm14, xmm14, xmm2
        vaddps    xmm5, xmm14, xmm15
        vmulps    xmm7, xmm4, xmm1
        vmovdqu   xmm14, XMMWORD PTR [448+rsp]
        vaddps    xmm8, xmm5, xmm7
        vmovmskps eax, xmm14
        vaddps    xmm4, xmm4, xmm8
        vaddps    xmm9, xmm9, xmm4
        vorps     xmm0, xmm9, xmm2
        vaddps    xmm1, xmm6, xmm0
        vorps     xmm0, xmm1, XMMWORD PTR [416+rsp]
        test      eax, eax
        jne       _B2_12

_B2_2::

        test      edx, edx
        jne       _B2_4

_B2_3::

        vzeroupper
        vmovups   xmm6, XMMWORD PTR [384+rsp]
        vmovups   xmm7, XMMWORD PTR [336+rsp]
        vmovups   xmm8, XMMWORD PTR [288+rsp]
        vmovups   xmm9, XMMWORD PTR [272+rsp]
        vmovups   xmm10, XMMWORD PTR [256+rsp]
        vmovups   xmm11, XMMWORD PTR [320+rsp]
        vmovups   xmm12, XMMWORD PTR [304+rsp]
        vmovups   xmm13, XMMWORD PTR [352+rsp]
        vmovups   xmm14, XMMWORD PTR [400+rsp]
        vmovups   xmm15, XMMWORD PTR [368+rsp]
        mov       r13, QWORD PTR [464+rsp]
        add       rsp, 488
        ret

_B2_4::

        vmovups   xmm1, XMMWORD PTR [32+rsp]
        vmovups   XMMWORD PTR [r13], xmm1
        vmovups   XMMWORD PTR [64+r13], xmm12
        vmovups   XMMWORD PTR [128+r13], xmm0
        je        _B2_3

_B2_7::

        xor       eax, eax
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, eax
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, edx

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
        vmovups   xmm0, XMMWORD PTR [128+r13]
        jmp       _B2_3

_B2_11::

        vzeroupper
        lea       rcx, QWORD PTR [r13+rbx*4]
        lea       rdx, QWORD PTR [64+r13+rbx*4]
        lea       r8, QWORD PTR [128+r13+rbx*4]

        call      __svml_satan2_ha_cout_rare_internal
        jmp       _B2_9

_B2_12::

        vcmpltps  xmm15, xmm10, xmm11
        vcmpordps xmm5, xmm12, xmm12
        vpcmpeqd  xmm8, xmm10, xmm3
        vpcmpeqd  xmm7, xmm11, xmm3
        vblendvps xmm10, xmm10, xmm11, xmm15
        vpor      xmm9, xmm7, xmm8
        vmovups   xmm4, XMMWORD PTR [32+rsp]
        vcmpordps xmm6, xmm4, xmm4
        vcmpeqps  xmm4, xmm10, xmm3
        vandnps   xmm11, xmm15, XMMWORD PTR [__svml_satan2_ha_data_internal+1856]
        vandps    xmm13, xmm5, xmm6
        vblendvps xmm5, xmm11, xmm3, xmm4
        vpcmpgtd  xmm3, xmm3, xmm12
        vandps    xmm3, xmm3, XMMWORD PTR [__svml_satan2_ha_data_internal+1920]
        vpand     xmm1, xmm9, xmm13
        vorps     xmm2, xmm5, xmm2
        vpandn    xmm14, xmm1, xmm14
        vmovmskps edx, xmm14
        vaddps    xmm2, xmm2, xmm3
        vorps     xmm4, xmm2, XMMWORD PTR [416+rsp]
        vblendvps xmm0, xmm0, xmm4, xmm1
        jmp       _B2_2
        ALIGN     16

_B2_13::

__svml_atan2f4_ha_e9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_atan2f4_ha_e9_B1_B4:
	DD	1600769
	DD	3855469
	DD	1599589
	DD	1407068
	DD	1214547
	DD	1153098
	DD	1091649
	DD	1357880
	DD	1296431
	DD	1497126
	DD	1697821
	DD	1570836
	DD	3997963

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B2_1
	DD	imagerel _B2_7
	DD	imagerel _unwind___svml_atan2f4_ha_e9_B1_B4

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_atan2f4_ha_e9_B7_B11:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B2_1
	DD	imagerel _B2_7
	DD	imagerel _unwind___svml_atan2f4_ha_e9_B1_B4

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B2_7
	DD	imagerel _B2_12
	DD	imagerel _unwind___svml_atan2f4_ha_e9_B7_B11

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_atan2f4_ha_e9_B12_B12:
	DD	33
	DD	imagerel _B2_1
	DD	imagerel _B2_7
	DD	imagerel _unwind___svml_atan2f4_ha_e9_B1_B4

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B2_12
	DD	imagerel _B2_13
	DD	imagerel _unwind___svml_atan2f4_ha_e9_B12_B12

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST2:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_atan2f8_ha_e9

__svml_atan2f8_ha_e9	PROC

_B3_1::

        DB        243
        DB        15
        DB        30
        DB        250
L75::

        sub       rsp, 520
        vmovups   XMMWORD PTR [368+rsp], xmm15
        vmovups   XMMWORD PTR [384+rsp], xmm14
        vmovups   XMMWORD PTR [400+rsp], xmm13
        vmovups   XMMWORD PTR [416+rsp], xmm12
        vmovups   XMMWORD PTR [320+rsp], xmm11
        vmovups   XMMWORD PTR [352+rsp], xmm10
        vmovups   XMMWORD PTR [336+rsp], xmm9
        vmovups   XMMWORD PTR [272+rsp], xmm8
        vmovups   XMMWORD PTR [288+rsp], xmm7
        vmovups   XMMWORD PTR [304+rsp], xmm6
        mov       QWORD PTR [496+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovups   ymm1, YMMWORD PTR [rdx]
        and       r13, -64
        vmovups   ymm2, YMMWORD PTR [__svml_satan2_ha_data_internal+192]
        vmovups   ymm4, YMMWORD PTR [rcx]
        vmovups   xmm9, XMMWORD PTR [__svml_satan2_ha_data_internal+1792]
        vmovups   YMMWORD PTR [96+r13], ymm1
        vmovups   YMMWORD PTR [32+r13], ymm4
        vandps    ymm3, ymm1, ymm2
        xor       edx, edx
        vandps    ymm2, ymm4, ymm2
        vxorps    ymm0, ymm1, ymm3
        vxorps    ymm5, ymm4, ymm2
        vmovups   YMMWORD PTR [64+r13], ymm0
        vmovups   xmm4, XMMWORD PTR [__svml_satan2_ha_data_internal+1728]
        vmovups   YMMWORD PTR [r13], ymm5
        mov       QWORD PTR [504+rsp], r13
        vpsubd    xmm10, xmm3, xmm4
        vextractf128 xmm6, ymm3, 1
        vpcmpgtd  xmm13, xmm10, xmm9
        vpcmpeqd  xmm7, xmm10, xmm9
        vmovups   XMMWORD PTR [32+rsp], xmm6
        vpor      xmm5, xmm13, xmm7
        vpsubd    xmm6, xmm6, xmm4
        vpcmpgtd  xmm10, xmm6, xmm9
        vpcmpeqd  xmm6, xmm6, xmm9
        vpor      xmm7, xmm10, xmm6
        vmovups   XMMWORD PTR [432+rsp], xmm3
        vpsubd    xmm8, xmm2, xmm4
        vextractf128 xmm0, ymm2, 1
        vpcmpgtd  xmm15, xmm8, xmm9
        vpcmpeqd  xmm11, xmm8, xmm9
        vpor      xmm14, xmm15, xmm11
        vpsubd    xmm12, xmm0, xmm4
        vpor      xmm4, xmm5, xmm14
        vpcmpgtd  xmm13, xmm12, xmm9
        vpcmpeqd  xmm9, xmm12, xmm9
        vpor      xmm12, xmm13, xmm9
        vpor      xmm6, xmm7, xmm12
        vmovdqu   XMMWORD PTR [464+rsp], xmm4
        vmovdqu   XMMWORD PTR [448+rsp], xmm6
        vmulps    ymm4, ymm3, YMMWORD PTR [__svml_satan2_ha_data_internal+384]
        vmulps    ymm6, ymm3, YMMWORD PTR [__svml_satan2_ha_data_internal+448]
        vmulps    ymm11, ymm3, YMMWORD PTR [__svml_satan2_ha_data_internal+512]
        vmulps    ymm12, ymm3, YMMWORD PTR [__svml_satan2_ha_data_internal+576]
        vmovups   XMMWORD PTR [480+rsp], xmm0
        vmovups   xmm5, XMMWORD PTR [__svml_satan2_ha_data_internal+1600]
        vpcmpgtd  xmm8, xmm4, xmm2
        vextractf128 xmm13, ymm4, 1
        vpcmpgtd  xmm4, xmm13, xmm0
        vpcmpgtd  xmm15, xmm6, xmm2
        vextractf128 xmm9, ymm6, 1
        vpcmpgtd  xmm13, xmm9, xmm0
        vpcmpgtd  xmm9, xmm11, xmm2
        vextractf128 xmm10, ymm11, 1
        vpcmpgtd  xmm14, xmm10, xmm0
        vpcmpgtd  xmm6, xmm2, xmm12
        vextractf128 xmm7, ymm12, 1
        vpcmpgtd  xmm10, xmm0, xmm7
        vpandn    xmm7, xmm15, xmm9
        vpandn    xmm0, xmm8, xmm15
        vinsertf128 ymm11, ymm8, xmm4, 1
        vpandn    xmm4, xmm4, xmm13
        vpxor     xmm15, xmm6, xmm5
        vpandn    xmm13, xmm13, xmm14
        vpandn    xmm9, xmm9, xmm15
        vinsertf128 ymm0, ymm0, xmm4, 1
        vpxor     xmm4, xmm10, xmm5
        vpandn    xmm14, xmm14, xmm4
        vmovups   ymm5, YMMWORD PTR [__svml_satan2_ha_data_internal+640]
        vandps    ymm4, ymm2, ymm11
        vinsertf128 ymm12, ymm9, xmm14, 1
        vmovups   ymm14, YMMWORD PTR [__svml_satan2_ha_data_internal+704]
        vmulps    ymm15, ymm3, ymm14
        vmulps    ymm14, ymm2, ymm14
        vsubps    ymm9, ymm2, ymm15
        vmovups   ymm15, YMMWORD PTR [__svml_satan2_ha_data_internal+768]
        vinsertf128 ymm13, ymm7, xmm13, 1
        vmulps    ymm7, ymm3, ymm5
        vsubps    ymm8, ymm2, ymm7
        vmulps    ymm7, ymm3, ymm15
        vinsertf128 ymm10, ymm6, xmm10, 1
        vandps    ymm6, ymm8, ymm0
        vandps    ymm8, ymm9, ymm13
        vsubps    ymm9, ymm2, ymm7
        vandps    ymm7, ymm9, ymm12
        vxorps    ymm9, ymm3, YMMWORD PTR [__svml_satan2_ha_data_internal+128]
        vorps     ymm6, ymm4, ymm6
        vorps     ymm4, ymm8, ymm7
        vandps    ymm9, ymm9, ymm10
        vorps     ymm8, ymm6, ymm4
        vorps     ymm7, ymm8, ymm9
        vandps    ymm4, ymm3, ymm11
        vmulps    ymm11, ymm2, ymm5
        vmulps    ymm8, ymm2, ymm15
        vaddps    ymm9, ymm3, ymm14
        vaddps    ymm5, ymm3, ymm11
        vaddps    ymm15, ymm3, ymm8
        vandps    ymm6, ymm5, ymm0
        vandps    ymm9, ymm9, ymm13
        vandps    ymm11, ymm15, ymm12
        vorps     ymm6, ymm4, ymm6
        vorps     ymm4, ymm9, ymm11
        vorps     ymm5, ymm6, ymm4
        vandps    ymm6, ymm0, YMMWORD PTR [__svml_satan2_ha_data_internal+832]
        vandps    ymm9, ymm13, YMMWORD PTR [__svml_satan2_ha_data_internal+960]
        vorps     ymm11, ymm6, ymm9
        vandps    ymm14, ymm2, ymm10
        vorps     ymm4, ymm5, ymm14
        vandps    ymm8, ymm12, YMMWORD PTR [__svml_satan2_ha_data_internal+1088]
        vandps    ymm15, ymm10, YMMWORD PTR [__svml_satan2_ha_data_internal+1216]
        vorps     ymm5, ymm8, ymm15
        vorps     ymm9, ymm11, ymm5
        vandps    ymm12, ymm12, YMMWORD PTR [__svml_satan2_ha_data_internal+1152]
        vandps    ymm10, ymm10, YMMWORD PTR [__svml_satan2_ha_data_internal+1280]
        vorps     ymm10, ymm12, ymm10
        vandps    ymm0, ymm0, YMMWORD PTR [__svml_satan2_ha_data_internal+896]
        vandps    ymm13, ymm13, YMMWORD PTR [__svml_satan2_ha_data_internal+1024]
        vorps     ymm0, ymm0, ymm13
        vextractf128 xmm6, ymm7, 1
        vcvtps2pd ymm8, xmm7
        vcvtps2pd ymm7, xmm6
        vcvtps2pd ymm6, xmm4
        vextractf128 xmm14, ymm4, 1
        vrcpps    ymm4, ymm4
        vcvtps2pd ymm11, xmm14
        vcvtps2pd ymm15, xmm4
        vextractf128 xmm5, ymm4, 1
        vmovupd   ymm4, YMMWORD PTR [__svml_satan2_ha_data_internal+1664]
        vmulpd    ymm6, ymm6, ymm15
        vcvtps2pd ymm5, xmm5
        vsubpd    ymm6, ymm4, ymm6
        vmulpd    ymm11, ymm11, ymm5
        vsubpd    ymm14, ymm4, ymm11
        vmulpd    ymm11, ymm15, ymm6
        vaddpd    ymm11, ymm15, ymm11
        vmulpd    ymm15, ymm5, ymm14
        vmulpd    ymm14, ymm14, ymm14
        vaddpd    ymm15, ymm5, ymm15
        vmulpd    ymm5, ymm6, ymm6
        vaddpd    ymm6, ymm4, ymm14
        vaddpd    ymm5, ymm4, ymm5
        vmulpd    ymm4, ymm11, ymm5
        vmulpd    ymm5, ymm15, ymm6
        vmulpd    ymm8, ymm8, ymm4
        vmulpd    ymm7, ymm7, ymm5
        vcvtpd2ps xmm11, ymm8
        vcvtpd2ps xmm14, ymm7
        vcvtps2pd ymm6, xmm14
        vsubpd    ymm6, ymm7, ymm6
        vcvtpd2ps xmm5, ymm6
        vcvtps2pd ymm15, xmm11
        vinsertf128 ymm4, ymm11, xmm14, 1
        vsubpd    ymm8, ymm8, ymm15
        vmulps    ymm15, ymm4, ymm4
        vcvtpd2ps xmm7, ymm8
        vmulps    ymm8, ymm15, ymm15
        vmulps    ymm11, ymm8, YMMWORD PTR [__svml_satan2_ha_data_internal+1408]
        vaddps    ymm13, ymm11, YMMWORD PTR [__svml_satan2_ha_data_internal+1536]
        vinsertf128 ymm6, ymm7, xmm5, 1
        vmulps    ymm7, ymm8, YMMWORD PTR [__svml_satan2_ha_data_internal+1344]
        vmovups   ymm8, YMMWORD PTR [64+r13]
        vaddps    ymm12, ymm7, YMMWORD PTR [__svml_satan2_ha_data_internal+1472]
        vmulps    ymm14, ymm15, ymm12
        vorps     ymm5, ymm0, ymm10
        vaddps    ymm10, ymm6, ymm5
        vaddps    ymm5, ymm13, ymm14
        vmovups   ymm6, YMMWORD PTR [__svml_satan2_ha_data_internal]
        vmulps    ymm7, ymm15, ymm5
        vcmplt_oqps ymm1, ymm1, ymm6
        vandps    ymm0, ymm1, YMMWORD PTR [__svml_satan2_ha_data_internal+256]
        vandps    ymm1, ymm1, YMMWORD PTR [__svml_satan2_ha_data_internal+320]
        vxorps    ymm1, ymm1, ymm8
        vaddps    ymm10, ymm1, ymm10
        vmulps    ymm1, ymm4, ymm7
        vaddps    ymm7, ymm10, ymm1
        vaddps    ymm4, ymm4, ymm7
        vpxor     xmm7, xmm7, xmm7
        vaddps    ymm9, ymm9, ymm4
        vmovdqu   xmm4, XMMWORD PTR [464+rsp]
        vpackssdw xmm5, xmm4, XMMWORD PTR [448+rsp]
        vorps     ymm1, ymm9, ymm8
        vpacksswb xmm9, xmm5, xmm7
        vpmovmskb eax, xmm9
        vaddps    ymm0, ymm0, ymm1
        vmovups   xmm1, XMMWORD PTR [480+rsp]
        vmovaps   xmm4, xmm2
        vorps     ymm0, ymm0, YMMWORD PTR [r13]
        test      al, al
        jne       _B3_12

_B3_2::

        test      dl, dl
        jne       _B3_4

_B3_3::

        vmovups   xmm6, XMMWORD PTR [304+rsp]
        vmovups   xmm7, XMMWORD PTR [288+rsp]
        vmovups   xmm8, XMMWORD PTR [272+rsp]
        vmovups   xmm9, XMMWORD PTR [336+rsp]
        vmovups   xmm10, XMMWORD PTR [352+rsp]
        vmovups   xmm11, XMMWORD PTR [320+rsp]
        vmovups   xmm12, XMMWORD PTR [416+rsp]
        vmovups   xmm13, XMMWORD PTR [400+rsp]
        vmovups   xmm14, XMMWORD PTR [384+rsp]
        vmovups   xmm15, XMMWORD PTR [368+rsp]
        mov       r13, QWORD PTR [496+rsp]
        add       rsp, 520
        ret

_B3_4::

        vmovups   ymm1, YMMWORD PTR [32+r13]
        vmovups   ymm2, YMMWORD PTR [96+r13]
        vmovups   YMMWORD PTR [128+r13], ymm0
        vmovups   YMMWORD PTR [r13], ymm1
        vmovups   YMMWORD PTR [64+r13], ymm2
        test      edx, edx
        je        _B3_3

_B3_7::

        xor       eax, eax
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, eax
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, edx

_B3_8::

        bt        esi, ebx
        jc        _B3_11

_B3_9::

        inc       ebx
        cmp       ebx, 8
        jl        _B3_8

_B3_10::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        vmovups   ymm0, YMMWORD PTR [128+r13]
        jmp       _B3_3

_B3_11::

        vzeroupper
        lea       rcx, QWORD PTR [r13+rbx*4]
        lea       rdx, QWORD PTR [64+r13+rbx*4]
        lea       r8, QWORD PTR [128+r13+rbx*4]

        call      __svml_satan2_ha_cout_rare_internal
        jmp       _B3_9

_B3_12::

        vmovups   ymm8, YMMWORD PTR [96+r13]
        vmovups   ymm9, YMMWORD PTR [32+r13]
        vcmpordps ymm10, ymm8, ymm8
        vcmpordps ymm11, ymm9, ymm9
        vandps    ymm12, ymm10, ymm11
        vpcmpeqd  xmm14, xmm4, xmm6
        vextractf128 xmm7, ymm6, 1
        vpcmpeqd  xmm13, xmm6, XMMWORD PTR [432+rsp]
        vpor      xmm15, xmm13, xmm14
        vpcmpeqd  xmm9, xmm7, XMMWORD PTR [32+rsp]
        vpcmpeqd  xmm1, xmm1, xmm7
        vpor      xmm10, xmm9, xmm1
        vpxor     xmm14, xmm14, xmm14
        vpand     xmm4, xmm15, xmm12
        vextractf128 xmm5, ymm12, 1
        vpand     xmm1, xmm10, xmm5
        vpandn    xmm5, xmm4, XMMWORD PTR [464+rsp]
        vpandn    xmm11, xmm1, XMMWORD PTR [448+rsp]
        vpackssdw xmm13, xmm5, xmm11
        vcmplt_oqps ymm5, ymm2, ymm3
        vpacksswb xmm15, xmm13, xmm14
        vpmovmskb edx, xmm15
        vblendvps ymm2, ymm2, ymm3, ymm5
        vcmpeqps  ymm2, ymm2, ymm6
        vandnps   ymm3, ymm5, YMMWORD PTR [__svml_satan2_ha_data_internal+1856]
        vblendvps ymm3, ymm3, ymm6, ymm2
        vorps     ymm10, ymm3, YMMWORD PTR [64+r13]
        vpcmpgtd  xmm6, xmm6, xmm8
        vextractf128 xmm5, ymm8, 1
        vpcmpgtd  xmm7, xmm7, xmm5
        vinsertf128 ymm9, ymm6, xmm7, 1
        vandps    ymm11, ymm9, YMMWORD PTR [__svml_satan2_ha_data_internal+1920]
        vaddps    ymm12, ymm10, ymm11
        vorps     ymm13, ymm12, YMMWORD PTR [r13]
        vinsertf128 ymm1, ymm4, xmm1, 1
        vblendvps ymm0, ymm0, ymm13, ymm1
        jmp       _B3_2
        ALIGN     16

_B3_13::

__svml_atan2f8_ha_e9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_atan2f8_ha_e9_B1_B4:
	DD	1600769
	DD	4117613
	DD	1271909
	DD	1210460
	DD	1149011
	DD	1415242
	DD	1484865
	DD	1357880
	DD	1755183
	DD	1693734
	DD	1632285
	DD	1570836
	DD	4260107

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B3_1
	DD	imagerel _B3_7
	DD	imagerel _unwind___svml_atan2f8_ha_e9_B1_B4

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_atan2f8_ha_e9_B7_B11:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B3_1
	DD	imagerel _B3_7
	DD	imagerel _unwind___svml_atan2f8_ha_e9_B1_B4

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B3_7
	DD	imagerel _B3_12
	DD	imagerel _unwind___svml_atan2f8_ha_e9_B7_B11

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_atan2f8_ha_e9_B12_B12:
	DD	33
	DD	imagerel _B3_1
	DD	imagerel _B3_7
	DD	imagerel _unwind___svml_atan2f8_ha_e9_B1_B4

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B3_12
	DD	imagerel _B3_13
	DD	imagerel _unwind___svml_atan2f8_ha_e9_B12_B12

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST3:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_atan2f4_ha_ex

__svml_atan2f4_ha_ex	PROC

_B4_1::

        DB        243
        DB        15
        DB        30
        DB        250
L126::

        sub       rsp, 504
        xor       eax, eax
        movups    XMMWORD PTR [400+rsp], xmm15
        movups    XMMWORD PTR [272+rsp], xmm14
        movups    XMMWORD PTR [288+rsp], xmm13
        movups    XMMWORD PTR [304+rsp], xmm12
        movups    XMMWORD PTR [320+rsp], xmm11
        movups    XMMWORD PTR [352+rsp], xmm10
        movups    XMMWORD PTR [368+rsp], xmm9
        movups    XMMWORD PTR [384+rsp], xmm8
        movups    XMMWORD PTR [336+rsp], xmm7
        movups    XMMWORD PTR [256+rsp], xmm6
        mov       QWORD PTR [480+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        movups    xmm8, XMMWORD PTR [rdx]
        and       r13, -64
        movups    xmm4, XMMWORD PTR [rcx]
        movaps    xmm11, xmm8
        movups    xmm10, XMMWORD PTR [__svml_satan2_ha_data_internal+192]
        andps     xmm11, xmm10
        andps     xmm10, xmm4
        movdqu    xmm3, XMMWORD PTR [__svml_satan2_ha_data_internal+1728]
        movaps    xmm14, xmm11
        movaps    xmm1, xmm10
        movaps    xmm5, xmm11
        psubd     xmm14, xmm3
        psubd     xmm1, xmm3
        pxor      xmm5, xmm8
        movdqa    xmm6, xmm14
        movdqu    xmm0, XMMWORD PTR [__svml_satan2_ha_data_internal+1792]
        movaps    xmm15, xmm11
        movups    XMMWORD PTR [448+rsp], xmm5
        movdqa    xmm5, xmm1
        pcmpgtd   xmm6, xmm0
        pcmpeqd   xmm14, xmm0
        pcmpgtd   xmm5, xmm0
        pcmpeqd   xmm1, xmm0
        por       xmm6, xmm14
        por       xmm5, xmm1
        movups    xmm13, XMMWORD PTR [__svml_satan2_ha_data_internal+384]
        por       xmm6, xmm5
        movups    xmm2, XMMWORD PTR [__svml_satan2_ha_data_internal+576]
        movaps    xmm9, xmm10
        movdqu    XMMWORD PTR [464+rsp], xmm6
        movaps    xmm7, xmm10
        movups    xmm5, XMMWORD PTR [__svml_satan2_ha_data_internal+448]
        pxor      xmm9, xmm4
        movups    xmm6, XMMWORD PTR [__svml_satan2_ha_data_internal+512]
        movaps    xmm0, xmm10
        mulps     xmm13, xmm11
        movaps    xmm12, xmm10
        mulps     xmm2, xmm11
        mulps     xmm5, xmm11
        mulps     xmm6, xmm11
        movups    xmm3, XMMWORD PTR [__svml_satan2_ha_data_internal+640]
        pcmpgtd   xmm13, xmm10
        mulps     xmm15, xmm3
        pcmpgtd   xmm7, xmm2
        mulps     xmm3, xmm10
        subps     xmm0, xmm15
        addps     xmm3, xmm11
        movups    XMMWORD PTR [432+rsp], xmm9
        pcmpgtd   xmm5, xmm10
        movdqu    xmm9, XMMWORD PTR [__svml_satan2_ha_data_internal+1600]
        pcmpgtd   xmm6, xmm10
        movups    XMMWORD PTR [32+rsp], xmm4
        pxor      xmm9, xmm7
        movaps    xmm4, xmm13
        movaps    xmm15, xmm11
        movups    xmm14, XMMWORD PTR [__svml_satan2_ha_data_internal+704]
        pandn     xmm4, xmm5
        pandn     xmm5, xmm6
        pandn     xmm6, xmm9
        movaps    xmm9, xmm11
        movaps    xmm2, xmm10
        movups    xmm1, XMMWORD PTR [__svml_satan2_ha_data_internal+768]
        andps     xmm12, xmm13
        mulps     xmm9, xmm14
        andps     xmm0, xmm4
        mulps     xmm15, xmm1
        mulps     xmm14, xmm10
        subps     xmm2, xmm9
        mulps     xmm1, xmm10
        addps     xmm14, xmm11
        addps     xmm1, xmm11
        movaps    xmm9, xmm10
        andps     xmm2, xmm5
        orps      xmm12, xmm0
        andps     xmm13, xmm11
        andps     xmm3, xmm4
        andps     xmm14, xmm5
        andps     xmm1, xmm6
        movaps    xmm0, xmm10
        orps      xmm13, xmm3
        orps      xmm14, xmm1
        movups    xmm3, XMMWORD PTR [__svml_satan2_ha_data_internal+960]
        andps     xmm0, xmm7
        orps      xmm13, xmm14
        andps     xmm3, xmm5
        orps      xmm13, xmm0
        subps     xmm9, xmm15
        movups    xmm15, XMMWORD PTR [__svml_satan2_ha_data_internal+128]
        andps     xmm9, xmm6
        pxor      xmm15, xmm11
        orps      xmm2, xmm9
        movups    xmm9, XMMWORD PTR [__svml_satan2_ha_data_internal+832]
        andps     xmm15, xmm7
        orps      xmm12, xmm2
        andps     xmm9, xmm4
        orps      xmm12, xmm15
        orps      xmm9, xmm3
        cvtps2pd  xmm3, xmm12
        movhlps   xmm12, xmm12
        movaps    xmm1, xmm13
        cvtps2pd  xmm2, xmm12
        rcpps     xmm12, xmm13
        movups    xmm0, XMMWORD PTR [__svml_satan2_ha_data_internal+1088]
        movups    xmm14, XMMWORD PTR [__svml_satan2_ha_data_internal+1216]
        andps     xmm0, xmm6
        andps     xmm14, xmm7
        orps      xmm0, xmm14
        movhlps   xmm1, xmm13
        orps      xmm9, xmm0
        cvtps2pd  xmm0, xmm13
        cvtps2pd  xmm13, xmm12
        cvtps2pd  xmm1, xmm1
        mulpd     xmm0, xmm13
        movhlps   xmm12, xmm12
        cvtps2pd  xmm12, xmm12
        mulpd     xmm1, xmm12
        movups    xmm14, XMMWORD PTR [__svml_satan2_ha_data_internal+1664]
        movaps    xmm15, xmm14
        subpd     xmm15, xmm0
        movaps    xmm0, xmm14
        subpd     xmm0, xmm1
        movaps    xmm1, xmm15
        mulpd     xmm1, xmm13
        mulpd     xmm15, xmm15
        addpd     xmm13, xmm1
        addpd     xmm15, xmm14
        movaps    xmm1, xmm0
        mulpd     xmm1, xmm12
        mulpd     xmm0, xmm0
        addpd     xmm12, xmm1
        addpd     xmm14, xmm0
        mulpd     xmm13, xmm15
        mulpd     xmm12, xmm14
        mulpd     xmm3, xmm13
        mulpd     xmm2, xmm12
        cvtpd2ps  xmm14, xmm3
        cvtpd2ps  xmm13, xmm2
        movlhps   xmm14, xmm13
        movaps    xmm0, xmm14
        movhlps   xmm0, xmm14
        cvtps2pd  xmm1, xmm14
        cvtps2pd  xmm12, xmm0
        subpd     xmm3, xmm1
        subpd     xmm2, xmm12
        cvtpd2ps  xmm3, xmm3
        cvtpd2ps  xmm2, xmm2
        movlhps   xmm3, xmm2
        movups    xmm2, XMMWORD PTR [__svml_satan2_ha_data_internal+896]
        andps     xmm2, xmm4
        movaps    xmm4, xmm14
        movups    xmm15, XMMWORD PTR [__svml_satan2_ha_data_internal+1024]
        movups    xmm1, XMMWORD PTR [__svml_satan2_ha_data_internal+1152]
        andps     xmm15, xmm5
        movups    xmm0, XMMWORD PTR [__svml_satan2_ha_data_internal+1280]
        andps     xmm1, xmm6
        mulps     xmm4, xmm14
        andps     xmm0, xmm7
        orps      xmm2, xmm15
        orps      xmm1, xmm0
        orps      xmm2, xmm1
        movaps    xmm7, xmm4
        movups    xmm1, XMMWORD PTR [__svml_satan2_ha_data_internal]
        addps     xmm3, xmm2
        mulps     xmm7, xmm4
        movups    XMMWORD PTR [416+rsp], xmm8
        cmpltps   xmm8, xmm1
        movups    xmm0, XMMWORD PTR [__svml_satan2_ha_data_internal+256]
        movups    xmm5, XMMWORD PTR [__svml_satan2_ha_data_internal+320]
        andps     xmm0, xmm8
        andps     xmm5, xmm8
        movups    xmm8, XMMWORD PTR [__svml_satan2_ha_data_internal+1344]
        mulps     xmm8, xmm7
        movups    xmm2, XMMWORD PTR [__svml_satan2_ha_data_internal+1408]
        mulps     xmm2, xmm7
        addps     xmm8, XMMWORD PTR [__svml_satan2_ha_data_internal+1472]
        mulps     xmm8, xmm4
        addps     xmm2, XMMWORD PTR [__svml_satan2_ha_data_internal+1536]
        mov       QWORD PTR [488+rsp], r13
        addps     xmm2, xmm8
        mulps     xmm4, xmm2
        movups    xmm2, XMMWORD PTR [448+rsp]
        pxor      xmm5, xmm2
        mulps     xmm4, xmm14
        addps     xmm5, xmm3
        addps     xmm5, xmm4
        addps     xmm14, xmm5
        movdqu    xmm5, XMMWORD PTR [464+rsp]
        movmskps  r8d, xmm5
        addps     xmm9, xmm14
        orps      xmm9, xmm2
        addps     xmm0, xmm9
        orps      xmm0, XMMWORD PTR [432+rsp]
        test      r8d, r8d
        jne       _B4_12

_B4_2::

        test      eax, eax
        jne       _B4_4

_B4_3::

        movups    xmm6, XMMWORD PTR [256+rsp]
        movups    xmm7, XMMWORD PTR [336+rsp]
        movups    xmm8, XMMWORD PTR [384+rsp]
        movups    xmm9, XMMWORD PTR [368+rsp]
        movups    xmm10, XMMWORD PTR [352+rsp]
        movups    xmm11, XMMWORD PTR [320+rsp]
        movups    xmm12, XMMWORD PTR [304+rsp]
        movups    xmm13, XMMWORD PTR [288+rsp]
        movups    xmm14, XMMWORD PTR [272+rsp]
        movups    xmm15, XMMWORD PTR [400+rsp]
        mov       r13, QWORD PTR [480+rsp]
        add       rsp, 504
        ret

_B4_4::

        movups    xmm1, XMMWORD PTR [32+rsp]
        movups    xmm2, XMMWORD PTR [416+rsp]
        movups    XMMWORD PTR [r13], xmm1
        movups    XMMWORD PTR [64+r13], xmm2
        movups    XMMWORD PTR [128+r13], xmm0
        je        _B4_3

_B4_7::

        xor       ecx, ecx
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, ecx
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, eax

_B4_8::

        mov       ecx, ebx
        mov       edx, 1
        shl       edx, cl
        test      esi, edx
        jne       _B4_11

_B4_9::

        inc       ebx
        cmp       ebx, 4
        jl        _B4_8

_B4_10::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        movups    xmm0, XMMWORD PTR [128+r13]
        jmp       _B4_3

_B4_11::

        lea       rcx, QWORD PTR [r13+rbx*4]
        lea       rdx, QWORD PTR [64+r13+rbx*4]
        lea       r8, QWORD PTR [128+r13+rbx*4]

        call      __svml_satan2_ha_cout_rare_internal
        jmp       _B4_9

_B4_12::

        movups    xmm8, XMMWORD PTR [416+rsp]
        movaps    xmm9, xmm11
        movups    xmm2, XMMWORD PTR [32+rsp]
        movaps    xmm4, xmm8
        cmpordps  xmm4, xmm8
        cmpordps  xmm2, xmm2
        movaps    xmm3, xmm10
        pcmpeqd   xmm9, xmm1
        pcmpeqd   xmm3, xmm1
        andps     xmm4, xmm2
        por       xmm9, xmm3
        andps     xmm9, xmm4
        movaps    xmm6, xmm9
        pandn     xmm6, xmm5
        movaps    xmm5, xmm10
        cmpltps   xmm5, xmm11
        movmskps  eax, xmm6
        movaps    xmm7, xmm5
        andps     xmm11, xmm5
        andnps    xmm7, xmm10
        orps      xmm7, xmm11
        cmpeqps   xmm7, xmm1
        andnps    xmm5, XMMWORD PTR [__svml_satan2_ha_data_internal+1856]
        movaps    xmm10, xmm7
        andnps    xmm10, xmm5
        andps     xmm7, xmm1
        orps      xmm10, xmm7
        pcmpgtd   xmm1, xmm8
        andps     xmm1, XMMWORD PTR [__svml_satan2_ha_data_internal+1920]
        orps      xmm10, XMMWORD PTR [448+rsp]
        addps     xmm10, xmm1
        movaps    xmm1, xmm9
        orps      xmm10, XMMWORD PTR [432+rsp]
        andnps    xmm1, xmm0
        andps     xmm10, xmm9
        movaps    xmm0, xmm1
        orps      xmm0, xmm10
        jmp       _B4_2
        ALIGN     16

_B4_13::

__svml_atan2f4_ha_ex ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_atan2f4_ha_ex_B1_B4:
	DD	1600769
	DD	3986541
	DD	1075301
	DD	1407069
	DD	1607765
	DD	1546316
	DD	1484867
	DD	1357882
	DD	1296433
	DD	1234984
	DD	1173535
	DD	1701910
	DD	4129035

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B4_1
	DD	imagerel _B4_7
	DD	imagerel _unwind___svml_atan2f4_ha_ex_B1_B4

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_atan2f4_ha_ex_B7_B11:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B4_1
	DD	imagerel _B4_7
	DD	imagerel _unwind___svml_atan2f4_ha_ex_B1_B4

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B4_7
	DD	imagerel _B4_12
	DD	imagerel _unwind___svml_atan2f4_ha_ex_B7_B11

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_atan2f4_ha_ex_B12_B12:
	DD	33
	DD	imagerel _B4_1
	DD	imagerel _B4_7
	DD	imagerel _unwind___svml_atan2f4_ha_ex_B1_B4

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B4_12
	DD	imagerel _B4_13
	DD	imagerel _unwind___svml_atan2f4_ha_ex_B12_B12

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST4:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_atan2f8_ha_l9

__svml_atan2f8_ha_l9	PROC

_B5_1::

        DB        243
        DB        15
        DB        30
        DB        250
L167::

        sub       rsp, 456
        vmovups   XMMWORD PTR [384+rsp], xmm15
        vmovups   XMMWORD PTR [416+rsp], xmm14
        vmovups   XMMWORD PTR [368+rsp], xmm13
        vmovups   XMMWORD PTR [272+rsp], xmm12
        vmovups   XMMWORD PTR [288+rsp], xmm11
        vmovups   XMMWORD PTR [304+rsp], xmm10
        vmovups   XMMWORD PTR [320+rsp], xmm9
        vmovups   XMMWORD PTR [336+rsp], xmm8
        vmovups   XMMWORD PTR [352+rsp], xmm7
        vmovups   XMMWORD PTR [400+rsp], xmm6
        mov       QWORD PTR [432+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovups   ymm5, YMMWORD PTR [rcx]
        and       r13, -64
        vmovups   ymm10, YMMWORD PTR [rdx]
        vmovups   ymm8, YMMWORD PTR [__svml_satan2_ha_data_internal+192]
        vmovups   ymm0, YMMWORD PTR [__svml_satan2_ha_data_internal+1728]
        vmovups   ymm15, YMMWORD PTR [__svml_satan2_ha_data_internal+1792]
        vmovups   YMMWORD PTR [32+r13], ymm5
        vandps    ymm9, ymm10, ymm8
        xor       edx, edx
        vandps    ymm8, ymm5, ymm8
        vxorps    ymm4, ymm5, ymm8
        vmovups   YMMWORD PTR [r13], ymm4
        vmulps    ymm4, ymm9, YMMWORD PTR [__svml_satan2_ha_data_internal+384]
        vmulps    ymm6, ymm9, YMMWORD PTR [__svml_satan2_ha_data_internal+448]
        vpsubd    ymm1, ymm9, ymm0
        vpsubd    ymm12, ymm8, ymm0
        vmulps    ymm0, ymm9, YMMWORD PTR [__svml_satan2_ha_data_internal+512]
        vxorps    ymm2, ymm10, ymm9
        vmovups   YMMWORD PTR [64+r13], ymm2
        vpcmpgtd  ymm3, ymm1, ymm15
        vpcmpeqd  ymm2, ymm1, ymm15
        vpcmpgtd  ymm14, ymm12, ymm15
        vpcmpeqd  ymm11, ymm12, ymm15
        vpor      ymm13, ymm3, ymm2
        vpor      ymm5, ymm14, ymm11
        vmulps    ymm3, ymm9, YMMWORD PTR [__svml_satan2_ha_data_internal+576]
        vpor      ymm7, ymm13, ymm5
        vpcmpgtd  ymm1, ymm4, ymm8
        vpcmpgtd  ymm2, ymm6, ymm8
        vpcmpgtd  ymm12, ymm0, ymm8
        vpandn    ymm6, ymm1, ymm2
        vpandn    ymm4, ymm2, ymm12
        vmovups   ymm2, YMMWORD PTR [__svml_satan2_ha_data_internal+704]
        vmovups   ymm0, YMMWORD PTR [__svml_satan2_ha_data_internal+640]
        vmovdqu   YMMWORD PTR [96+r13], ymm7
        vpcmpgtd  ymm5, ymm8, ymm3
        vmovdqa   ymm3, ymm9
        vfnmadd213ps ymm3, ymm2, ymm8
        vfmadd213ps ymm2, ymm8, ymm9
        vpxor     ymm15, ymm5, YMMWORD PTR [__svml_satan2_ha_data_internal+1600]
        vandps    ymm11, ymm3, ymm4
        vmovups   ymm3, YMMWORD PTR [__svml_satan2_ha_data_internal+768]
        vpandn    ymm7, ymm12, ymm15
        vmovdqa   ymm14, ymm9
        vmovdqa   ymm15, ymm9
        vfnmadd213ps ymm14, ymm0, ymm8
        vfnmadd213ps ymm15, ymm3, ymm8
        vfmadd213ps ymm0, ymm8, ymm9
        vfmadd213ps ymm3, ymm8, ymm9
        vandps    ymm12, ymm8, ymm1
        vandps    ymm13, ymm14, ymm6
        vandps    ymm14, ymm15, ymm7
        vxorps    ymm15, ymm9, YMMWORD PTR [__svml_satan2_ha_data_internal+128]
        vorps     ymm13, ymm12, ymm13
        vorps     ymm11, ymm11, ymm14
        vandps    ymm15, ymm15, ymm5
        vorps     ymm12, ymm13, ymm11
        vorps     ymm12, ymm12, ymm15
        vandps    ymm1, ymm9, ymm1
        vandps    ymm15, ymm0, ymm6
        vandps    ymm2, ymm2, ymm4
        vandps    ymm3, ymm3, ymm7
        vorps     ymm14, ymm1, ymm15
        vorps     ymm11, ymm2, ymm3
        vandps    ymm0, ymm8, ymm5
        vorps     ymm13, ymm14, ymm11
        vandps    ymm1, ymm6, YMMWORD PTR [__svml_satan2_ha_data_internal+832]
        vandps    ymm2, ymm4, YMMWORD PTR [__svml_satan2_ha_data_internal+960]
        vandps    ymm15, ymm7, YMMWORD PTR [__svml_satan2_ha_data_internal+1088]
        vandps    ymm14, ymm5, YMMWORD PTR [__svml_satan2_ha_data_internal+1216]
        vorps     ymm3, ymm13, ymm0
        vorps     ymm11, ymm1, ymm2
        vorps     ymm13, ymm15, ymm14
        vorps     ymm15, ymm11, ymm13
        vrcpps    ymm11, ymm3
        vmovups   ymm2, YMMWORD PTR [__svml_satan2_ha_data_internal+64]
        vmovups   ymm1, YMMWORD PTR [__svml_satan2_ha_data_internal+1408]
        vfnmadd231ps ymm2, ymm3, ymm11
        vfmadd213ps ymm2, ymm2, ymm2
        vfmadd213ps ymm11, ymm2, ymm11
        vmulps    ymm0, ymm12, ymm11
        vmovdqa   ymm14, ymm3
        vfnmadd213ps ymm14, ymm0, ymm12
        vfmadd213ps ymm14, ymm11, ymm0
        vfnmadd213ps ymm3, ymm14, ymm12
        vandps    ymm12, ymm6, YMMWORD PTR [__svml_satan2_ha_data_internal+896]
        vandps    ymm6, ymm7, YMMWORD PTR [__svml_satan2_ha_data_internal+1152]
        vandps    ymm5, ymm5, YMMWORD PTR [__svml_satan2_ha_data_internal+1280]
        vmulps    ymm13, ymm11, ymm3
        vorps     ymm3, ymm6, ymm5
        vmulps    ymm5, ymm14, ymm14
        vmovups   ymm6, YMMWORD PTR [__svml_satan2_ha_data_internal+1344]
        vandps    ymm4, ymm4, YMMWORD PTR [__svml_satan2_ha_data_internal+1024]
        vorps     ymm7, ymm12, ymm4
        vmulps    ymm4, ymm5, ymm5
        vfmadd213ps ymm6, ymm4, YMMWORD PTR [__svml_satan2_ha_data_internal+1472]
        vfmadd213ps ymm4, ymm1, YMMWORD PTR [__svml_satan2_ha_data_internal+1536]
        vorps     ymm2, ymm7, ymm3
        vmovups   ymm3, YMMWORD PTR [__svml_satan2_ha_data_internal]
        vfmadd213ps ymm6, ymm5, ymm4
        vaddps    ymm11, ymm2, ymm13
        vcmplt_oqps ymm0, ymm10, ymm3
        vmovups   ymm2, YMMWORD PTR [64+r13]
        vandps    ymm7, ymm0, YMMWORD PTR [__svml_satan2_ha_data_internal+320]
        vandps    ymm12, ymm0, YMMWORD PTR [__svml_satan2_ha_data_internal+256]
        vmulps    ymm0, ymm5, ymm6
        vxorps    ymm13, ymm7, ymm2
        vaddps    ymm11, ymm13, ymm11
        vfmadd213ps ymm0, ymm14, ymm11
        vaddps    ymm14, ymm14, ymm0
        vaddps    ymm15, ymm15, ymm14
        vmovdqu   ymm14, YMMWORD PTR [96+r13]
        vorps     ymm0, ymm15, ymm2
        vaddps    ymm12, ymm12, ymm0
        vmovmskps eax, ymm14
        vorps     ymm0, ymm12, YMMWORD PTR [r13]
        test      eax, eax
        mov       QWORD PTR [440+rsp], r13
        jne       _B5_12

_B5_2::

        test      edx, edx
        jne       _B5_4

_B5_3::

        vmovups   xmm6, XMMWORD PTR [400+rsp]
        vmovups   xmm7, XMMWORD PTR [352+rsp]
        vmovups   xmm8, XMMWORD PTR [336+rsp]
        vmovups   xmm9, XMMWORD PTR [320+rsp]
        vmovups   xmm10, XMMWORD PTR [304+rsp]
        vmovups   xmm11, XMMWORD PTR [288+rsp]
        vmovups   xmm12, XMMWORD PTR [272+rsp]
        vmovups   xmm13, XMMWORD PTR [368+rsp]
        vmovups   xmm14, XMMWORD PTR [416+rsp]
        vmovups   xmm15, XMMWORD PTR [384+rsp]
        mov       r13, QWORD PTR [432+rsp]
        add       rsp, 456
        ret

_B5_4::

        vmovups   ymm1, YMMWORD PTR [32+r13]
        vmovups   YMMWORD PTR [64+r13], ymm10
        vmovups   YMMWORD PTR [128+r13], ymm0
        vmovups   YMMWORD PTR [r13], ymm1
        je        _B5_3

_B5_7::

        xor       eax, eax
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, eax
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, edx

_B5_8::

        bt        esi, ebx
        jc        _B5_11

_B5_9::

        inc       ebx
        cmp       ebx, 8
        jl        _B5_8

_B5_10::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        vmovups   ymm0, YMMWORD PTR [128+r13]
        jmp       _B5_3

_B5_11::

        vzeroupper
        lea       rcx, QWORD PTR [r13+rbx*4]
        lea       rdx, QWORD PTR [64+r13+rbx*4]
        lea       r8, QWORD PTR [128+r13+rbx*4]

        call      __svml_satan2_ha_cout_rare_internal
        jmp       _B5_9

_B5_12::

        vmovups   ymm4, YMMWORD PTR [32+r13]
        vcmplt_oqps ymm15, ymm8, ymm9
        vcmpordps ymm5, ymm10, ymm10
        vcmpordps ymm6, ymm4, ymm4
        vpcmpeqd  ymm7, ymm9, ymm3
        vpcmpeqd  ymm11, ymm8, ymm3
        vblendvps ymm8, ymm8, ymm9, ymm15
        vpor      ymm12, ymm7, ymm11
        vcmpeqps  ymm4, ymm8, ymm3
        vandps    ymm13, ymm5, ymm6
        vandnps   ymm9, ymm15, YMMWORD PTR [__svml_satan2_ha_data_internal+1856]
        vpand     ymm1, ymm12, ymm13
        vblendvps ymm5, ymm9, ymm3, ymm4
        vpandn    ymm14, ymm1, ymm14
        vpcmpgtd  ymm3, ymm3, ymm10
        vandps    ymm3, ymm3, YMMWORD PTR [__svml_satan2_ha_data_internal+1920]
        vorps     ymm2, ymm5, ymm2
        vaddps    ymm2, ymm2, ymm3
        vorps     ymm4, ymm2, YMMWORD PTR [r13]
        vmovmskps edx, ymm14
        vblendvps ymm0, ymm0, ymm4, ymm1
        jmp       _B5_2
        ALIGN     16

_B5_13::

__svml_atan2f8_ha_l9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_atan2f8_ha_l9_B1_B4:
	DD	1600769
	DD	3593325
	DD	1665125
	DD	1472604
	DD	1411155
	DD	1349706
	DD	1288257
	DD	1226808
	DD	1165359
	DD	1562662
	DD	1763357
	DD	1636372
	DD	3735819

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B5_1
	DD	imagerel _B5_7
	DD	imagerel _unwind___svml_atan2f8_ha_l9_B1_B4

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_atan2f8_ha_l9_B7_B11:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B5_1
	DD	imagerel _B5_7
	DD	imagerel _unwind___svml_atan2f8_ha_l9_B1_B4

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B5_7
	DD	imagerel _B5_12
	DD	imagerel _unwind___svml_atan2f8_ha_l9_B7_B11

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_atan2f8_ha_l9_B12_B12:
	DD	33
	DD	imagerel _B5_1
	DD	imagerel _B5_7
	DD	imagerel _unwind___svml_atan2f8_ha_l9_B1_B4

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B5_12
	DD	imagerel _B5_13
	DD	imagerel _unwind___svml_atan2f8_ha_l9_B12_B12

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST5:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_satan2_ha_cout_rare_internal

__svml_satan2_ha_cout_rare_internal	PROC

_B6_1::

        DB        243
        DB        15
        DB        30
        DB        250
L204::

        sub       rsp, 168
        pxor      xmm1, xmm1
        movss     xmm0, DWORD PTR [ione]
        movss     xmm3, DWORD PTR [rcx]
        movss     xmm2, DWORD PTR [rdx]
        mulss     xmm3, xmm0
        mulss     xmm2, xmm0
        pxor      xmm0, xmm0
        cvtss2sd  xmm0, xmm3
        cvtss2sd  xmm1, xmm2
        movss     DWORD PTR [72+rsp], xmm3
        movss     DWORD PTR [76+rsp], xmm2
        movsd     QWORD PTR [56+rsp], xmm0
        movsd     QWORD PTR [64+rsp], xmm1
        movzx     ecx, WORD PTR [74+rsp]
        and       ecx, 32640
        mov       al, BYTE PTR [79+rsp]
        movzx     r10d, WORD PTR [62+rsp]
        and       al, -128
        movzx     r9d, WORD PTR [70+rsp]
        and       r10d, 32752
        and       r9d, 32752
        shr       ecx, 7
        mov       dl, BYTE PTR [75+rsp]
        shr       dl, 7
        shr       al, 7
        movups    XMMWORD PTR [128+rsp], xmm15
        movups    XMMWORD PTR [144+rsp], xmm14
        movups    XMMWORD PTR [112+rsp], xmm6
        shr       r10d, 4
        shr       r9d, 4
        cmp       ecx, 255
        je        _B6_35

_B6_2::

        movzx     r11d, WORD PTR [78+rsp]
        and       r11d, 32640
        cmp       r11d, 32640
        je        _B6_35

_B6_3::

        test      r10d, r10d
        jne       _B6_5

_B6_4::

        test      DWORD PTR [72+rsp], 8388607
        je        _B6_30

_B6_5::

        test      r9d, r9d
        jne       _B6_7

_B6_6::

        test      DWORD PTR [76+rsp], 8388607
        je        _B6_27

_B6_7::

        neg       r9d
        movsd     QWORD PTR [56+rsp], xmm0
        add       r9d, r10d
        movsd     QWORD PTR [64+rsp], xmm1
        mov       r11b, BYTE PTR [63+rsp]
        mov       cl, BYTE PTR [71+rsp]
        and       r11b, 127
        and       cl, 127
        cmp       r9d, -54
        jle       _B6_22

_B6_8::

        cmp       r9d, 54
        jge       _B6_19

_B6_9::

        mov       BYTE PTR [71+rsp], cl
        mov       BYTE PTR [63+rsp], r11b
        test      al, al
        jne       _B6_11

_B6_10::

        movsd     xmm14, QWORD PTR [_vmldAtanHATab+1976]
        movaps    xmm15, xmm14
        jmp       _B6_12

_B6_11::

        movsd     xmm14, QWORD PTR [_vmldAtanHATab+1936]
        movsd     xmm15, QWORD PTR [_vmldAtanHATab+1944]

_B6_12::

        movsd     xmm1, QWORD PTR [56+rsp]
        movsd     xmm6, QWORD PTR [64+rsp]
        movaps    xmm2, xmm1
        divsd     xmm2, xmm6
        movzx     ecx, WORD PTR [62+rsp]
        movsd     QWORD PTR [88+rsp], xmm2
        test      r10d, r10d
        jle       _B6_34

_B6_13::

        cmp       r10d, 2046
        jge       _B6_15

_B6_14::

        and       ecx, -32753
        add       r10d, -1023
        movsd     QWORD PTR [56+rsp], xmm1
        add       ecx, 16368
        mov       WORD PTR [62+rsp], cx
        jmp       _B6_16

_B6_15::

        movsd     xmm0, QWORD PTR [_vmldAtanHATab+1992]
        mov       r10d, 1022
        mulsd     xmm1, xmm0
        movsd     QWORD PTR [56+rsp], xmm1

_B6_16::

        neg       r10d
        mov       rcx, QWORD PTR [_vmldAtanHATab+1888]
        add       r10d, 1023
        mov       QWORD PTR [64+rsp], rcx
        and       r10d, 2047
        shr       rcx, 48
        shl       r10d, 4
        and       ecx, -32753
        or        ecx, r10d
        mov       WORD PTR [70+rsp], cx
        movsd     xmm0, QWORD PTR [64+rsp]
        mulsd     xmm6, xmm0
        comisd    xmm2, QWORD PTR [_vmldAtanHATab+1880]
        jb        _B6_18

_B6_17::

        movsd     xmm1, QWORD PTR [_vmldAtanHATab+2000]
        mov       ecx, 113
        mulsd     xmm1, xmm6
        movsd     QWORD PTR [32+rsp], xmm1
        movsd     xmm4, QWORD PTR [32+rsp]
        movzx     r10d, WORD PTR [94+rsp]
        subsd     xmm4, xmm6
        movsd     QWORD PTR [40+rsp], xmm4
        and       r10d, 32752
        movsd     xmm3, QWORD PTR [32+rsp]
        movsd     xmm5, QWORD PTR [40+rsp]
        shr       r10d, 4
        subsd     xmm3, xmm5
        movsd     QWORD PTR [32+rsp], xmm3
        mov       r9d, DWORD PTR [92+rsp]
        movsd     xmm0, QWORD PTR [32+rsp]
        and       r9d, 1048575
        movsd     QWORD PTR [80+rsp], xmm2
        movaps    xmm2, xmm6
        shl       r10d, 20
        subsd     xmm2, xmm0
        mov       r11d, DWORD PTR [84+rsp]
        or        r10d, r9d
        mov       r9d, r11d
        and       r11d, -524288
        add       r11d, 262144
        and       r9d, -1048576
        and       r11d, 1048575
        add       r10d, -1069547520
        or        r9d, r11d
        mov       DWORD PTR [80+rsp], 0
        mov       DWORD PTR [84+rsp], r9d
        lea       r9, QWORD PTR [__ImageBase]
        movsd     QWORD PTR [40+rsp], xmm2
        movsd     xmm0, QWORD PTR [32+rsp]
        movsd     xmm2, QWORD PTR [80+rsp]
        movsd     xmm3, QWORD PTR [40+rsp]
        mulsd     xmm0, xmm2
        mulsd     xmm3, xmm2
        movsd     xmm1, QWORD PTR [_vmldAtanHATab+1968]
        mulsd     xmm0, xmm1
        mulsd     xmm3, xmm1
        movaps    xmm4, xmm0
        sar       r10d, 19
        addsd     xmm4, xmm3
        movsd     QWORD PTR [32+rsp], xmm4
        cmp       r10d, 113
        movsd     xmm5, QWORD PTR [32+rsp]
        cmovl     ecx, r10d
        subsd     xmm0, xmm5
        add       ecx, ecx
        addsd     xmm0, xmm3
        movsd     QWORD PTR [40+rsp], xmm0
        movsd     xmm0, QWORD PTR [56+rsp]
        movsd     xmm5, QWORD PTR [32+rsp]
        movaps    xmm4, xmm0
        movsd     xmm1, QWORD PTR [40+rsp]
        addsd     xmm4, xmm5
        movsd     QWORD PTR [32+rsp], xmm4
        movaps    xmm4, xmm0
        movsd     xmm3, QWORD PTR [32+rsp]
        movsxd    rcx, ecx
        subsd     xmm4, xmm3
        movsd     QWORD PTR [40+rsp], xmm4
        movsd     xmm4, QWORD PTR [32+rsp]
        movsd     xmm3, QWORD PTR [40+rsp]
        addsd     xmm4, xmm3
        movsd     QWORD PTR [48+rsp], xmm4
        movaps    xmm4, xmm0
        movsd     xmm3, QWORD PTR [40+rsp]
        addsd     xmm5, xmm3
        movsd     QWORD PTR [40+rsp], xmm5
        movsd     xmm5, QWORD PTR [48+rsp]
        subsd     xmm4, xmm5
        movsd     QWORD PTR [48+rsp], xmm4
        movsd     xmm3, QWORD PTR [40+rsp]
        movsd     xmm5, QWORD PTR [48+rsp]
        addsd     xmm3, xmm5
        movsd     QWORD PTR [48+rsp], xmm3
        movsd     xmm4, QWORD PTR [32+rsp]
        movsd     xmm3, QWORD PTR [_vmldAtanHATab+2000]
        mulsd     xmm3, xmm4
        movsd     xmm5, QWORD PTR [48+rsp]
        movsd     QWORD PTR [32+rsp], xmm3
        addsd     xmm1, xmm5
        movsd     xmm5, QWORD PTR [32+rsp]
        subsd     xmm5, xmm4
        movsd     QWORD PTR [40+rsp], xmm5
        movsd     xmm5, QWORD PTR [32+rsp]
        movsd     xmm3, QWORD PTR [40+rsp]
        subsd     xmm5, xmm3
        movsd     QWORD PTR [32+rsp], xmm5
        movsd     xmm3, QWORD PTR [32+rsp]
        subsd     xmm4, xmm3
        movsd     QWORD PTR [40+rsp], xmm4
        movsd     xmm4, QWORD PTR [32+rsp]
        movsd     xmm5, QWORD PTR [40+rsp]
        addsd     xmm5, xmm1
        movaps    xmm1, xmm0
        mulsd     xmm1, QWORD PTR [_vmldAtanHATab+2000]
        movsd     QWORD PTR [32+rsp], xmm1
        movsd     xmm3, QWORD PTR [32+rsp]
        subsd     xmm3, QWORD PTR [56+rsp]
        movsd     QWORD PTR [40+rsp], xmm3
        movsd     xmm3, QWORD PTR [32+rsp]
        movsd     xmm1, QWORD PTR [40+rsp]
        subsd     xmm3, xmm1
        movsd     QWORD PTR [32+rsp], xmm3
        movsd     xmm1, QWORD PTR [32+rsp]
        subsd     xmm0, xmm1
        movsd     QWORD PTR [40+rsp], xmm0
        movsd     xmm3, QWORD PTR [32+rsp]
        movsd     xmm1, QWORD PTR [40+rsp]
        mulsd     xmm3, xmm2
        mulsd     xmm1, xmm2
        movaps    xmm2, xmm3
        addsd     xmm2, xmm1
        movsd     QWORD PTR [32+rsp], xmm2
        movaps    xmm2, xmm6
        movsd     xmm0, QWORD PTR [32+rsp]
        subsd     xmm3, xmm0
        addsd     xmm3, xmm1
        movsd     QWORD PTR [40+rsp], xmm3
        movsd     xmm1, QWORD PTR [32+rsp]
        movsd     xmm3, QWORD PTR [40+rsp]
        addsd     xmm2, xmm1
        movsd     QWORD PTR [32+rsp], xmm2
        movaps    xmm2, xmm6
        movsd     xmm0, QWORD PTR [32+rsp]
        subsd     xmm2, xmm0
        movsd     QWORD PTR [40+rsp], xmm2
        movsd     xmm2, QWORD PTR [32+rsp]
        movsd     xmm0, QWORD PTR [40+rsp]
        addsd     xmm2, xmm0
        movsd     QWORD PTR [48+rsp], xmm2
        movsd     xmm0, QWORD PTR [40+rsp]
        movsd     xmm2, QWORD PTR [_vmldAtanHATab+2000]
        addsd     xmm1, xmm0
        movsd     QWORD PTR [40+rsp], xmm1
        movsd     xmm1, QWORD PTR [48+rsp]
        subsd     xmm6, xmm1
        movsd     QWORD PTR [48+rsp], xmm6
        movsd     xmm1, QWORD PTR [40+rsp]
        movsd     xmm6, QWORD PTR [48+rsp]
        addsd     xmm1, xmm6
        movsd     QWORD PTR [48+rsp], xmm1
        movsd     xmm6, QWORD PTR [32+rsp]
        mulsd     xmm2, xmm6
        movsd     xmm0, QWORD PTR [48+rsp]
        movsd     QWORD PTR [32+rsp], xmm2
        addsd     xmm3, xmm0
        movsd     xmm1, QWORD PTR [32+rsp]
        subsd     xmm1, xmm6
        movsd     QWORD PTR [40+rsp], xmm1
        movsd     xmm1, QWORD PTR [32+rsp]
        movsd     xmm0, QWORD PTR [40+rsp]
        subsd     xmm1, xmm0
        movsd     QWORD PTR [32+rsp], xmm1
        movsd     xmm2, QWORD PTR [32+rsp]
        subsd     xmm6, xmm2
        movsd     QWORD PTR [40+rsp], xmm6
        movsd     xmm0, QWORD PTR [32+rsp]
        movsd     xmm6, QWORD PTR [_vmldAtanHATab+1888]
        divsd     xmm6, xmm0
        movsd     xmm2, QWORD PTR [40+rsp]
        addsd     xmm2, xmm3
        movsd     xmm3, QWORD PTR [_vmldAtanHATab+2000]
        mulsd     xmm3, xmm6
        movsd     QWORD PTR [40+rsp], xmm3
        movsd     xmm1, QWORD PTR [40+rsp]
        subsd     xmm1, xmm6
        movsd     QWORD PTR [48+rsp], xmm1
        movsd     xmm3, QWORD PTR [40+rsp]
        movsd     xmm6, QWORD PTR [48+rsp]
        movsd     xmm1, QWORD PTR [_vmldAtanHATab+1888]
        subsd     xmm3, xmm6
        movsd     QWORD PTR [48+rsp], xmm3
        movsd     xmm6, QWORD PTR [48+rsp]
        mulsd     xmm0, xmm6
        subsd     xmm1, xmm0
        movsd     xmm0, QWORD PTR [48+rsp]
        mulsd     xmm2, xmm0
        movsd     QWORD PTR [40+rsp], xmm2
        movsd     xmm2, QWORD PTR [40+rsp]
        subsd     xmm1, xmm2
        movsd     QWORD PTR [40+rsp], xmm1
        movsd     xmm3, QWORD PTR [40+rsp]
        mov       r10, QWORD PTR [48+rsp]
        movsd     xmm6, QWORD PTR [40+rsp]
        movsd     xmm0, QWORD PTR [48+rsp]
        mov       QWORD PTR [64+rsp], r10
        mov       r10, r9
        movsd     xmm2, QWORD PTR [64+rsp]
        addsd     xmm3, QWORD PTR [_vmldAtanHATab+1888]
        mulsd     xmm3, xmm6
        movaps    xmm6, xmm2
        mulsd     xmm6, xmm4
        mulsd     xmm3, xmm0
        movaps    xmm1, xmm3
        mulsd     xmm1, xmm5
        mulsd     xmm4, xmm3
        mulsd     xmm5, xmm2
        addsd     xmm1, xmm4
        movaps    xmm4, xmm6
        addsd     xmm1, xmm5
        movsd     QWORD PTR [32+rsp], xmm1
        movsd     xmm1, QWORD PTR [32+rsp]
        movsd     xmm0, QWORD PTR [_vmldAtanHATab+1872]
        addsd     xmm4, xmm1
        movsd     QWORD PTR [32+rsp], xmm4
        movsd     xmm5, QWORD PTR [32+rsp]
        movsd     xmm4, QWORD PTR [_vmldAtanHATab+2000]
        subsd     xmm6, xmm5
        addsd     xmm6, xmm1
        movsd     QWORD PTR [40+rsp], xmm6
        movsd     xmm2, QWORD PTR [32+rsp]
        movaps    xmm6, xmm2
        mulsd     xmm6, xmm2
        mulsd     xmm4, xmm2
        mulsd     xmm0, xmm6
        movsd     xmm1, QWORD PTR [40+rsp]
        movsd     QWORD PTR [32+rsp], xmm4
        movsd     xmm3, QWORD PTR [32+rsp]
        addsd     xmm0, QWORD PTR [_vmldAtanHATab+1864]
        subsd     xmm3, xmm2
        mulsd     xmm0, xmm6
        movsd     QWORD PTR [40+rsp], xmm3
        addsd     xmm0, QWORD PTR [_vmldAtanHATab+1856]
        mulsd     xmm0, xmm6
        addsd     xmm0, QWORD PTR [_vmldAtanHATab+1848]
        mulsd     xmm0, xmm6
        addsd     xmm0, QWORD PTR [_vmldAtanHATab+1840]
        mulsd     xmm0, xmm6
        addsd     xmm0, QWORD PTR [_vmldAtanHATab+1832]
        mulsd     xmm0, xmm6
        addsd     xmm0, QWORD PTR [_vmldAtanHATab+1824]
        mulsd     xmm0, xmm6
        movsd     xmm6, QWORD PTR [32+rsp]
        movsd     xmm5, QWORD PTR [40+rsp]
        subsd     xmm6, xmm5
        movsd     QWORD PTR [32+rsp], xmm6
        movsd     xmm4, QWORD PTR [32+rsp]
        movsd     xmm6, QWORD PTR [_vmldAtanHATab+2000]
        subsd     xmm2, xmm4
        mulsd     xmm6, xmm0
        movsd     QWORD PTR [40+rsp], xmm2
        movsd     xmm3, QWORD PTR [32+rsp]
        movsd     xmm2, QWORD PTR [40+rsp]
        movsd     QWORD PTR [32+rsp], xmm6
        addsd     xmm1, xmm2
        movsd     xmm4, QWORD PTR [32+rsp]
        subsd     xmm4, xmm0
        movsd     QWORD PTR [40+rsp], xmm4
        movsd     xmm6, QWORD PTR [32+rsp]
        movsd     xmm5, QWORD PTR [40+rsp]
        subsd     xmm6, xmm5
        movsd     QWORD PTR [32+rsp], xmm6
        movsd     xmm2, QWORD PTR [32+rsp]
        subsd     xmm0, xmm2
        movsd     QWORD PTR [40+rsp], xmm0
        movsd     xmm0, QWORD PTR [32+rsp]
        movsd     xmm6, QWORD PTR [40+rsp]
        movaps    xmm5, xmm0
        movaps    xmm4, xmm6
        mulsd     xmm4, xmm1
        mulsd     xmm0, xmm1
        mulsd     xmm6, xmm3
        mulsd     xmm5, xmm3
        addsd     xmm4, xmm0
        movaps    xmm2, xmm5
        addsd     xmm4, xmm6
        addsd     xmm2, xmm3
        movsd     QWORD PTR [32+rsp], xmm4
        movaps    xmm6, xmm5
        movsd     xmm4, QWORD PTR [32+rsp]
        movsd     QWORD PTR [32+rsp], xmm2
        movsd     xmm0, QWORD PTR [32+rsp]
        subsd     xmm6, xmm0
        movsd     QWORD PTR [40+rsp], xmm6
        movsd     xmm2, QWORD PTR [32+rsp]
        movsd     xmm0, QWORD PTR [40+rsp]
        addsd     xmm2, xmm0
        movsd     QWORD PTR [48+rsp], xmm2
        movsd     xmm6, QWORD PTR [40+rsp]
        addsd     xmm3, xmm6
        movsd     QWORD PTR [40+rsp], xmm3
        movsd     xmm3, QWORD PTR [48+rsp]
        subsd     xmm5, xmm3
        movsd     QWORD PTR [48+rsp], xmm5
        movsd     xmm6, QWORD PTR [40+rsp]
        movsd     xmm5, QWORD PTR [48+rsp]
        addsd     xmm6, xmm5
        movsd     QWORD PTR [48+rsp], xmm6
        mov       r11, QWORD PTR [32+rsp]
        movsd     xmm0, QWORD PTR [48+rsp]
        mov       QWORD PTR [64+rsp], r11
        addsd     xmm1, xmm0
        movsd     xmm6, QWORD PTR [64+rsp]
        addsd     xmm4, xmm1
        movaps    xmm1, xmm6
        movaps    xmm2, xmm6
        movsd     QWORD PTR [72+rsp], xmm4
        addsd     xmm1, QWORD PTR [imagerel(_vmldAtanHATab)+r9+rcx*8]
        movsd     QWORD PTR [32+rsp], xmm1
        movsd     xmm0, QWORD PTR [32+rsp]
        subsd     xmm2, xmm0
        movsd     QWORD PTR [40+rsp], xmm2
        movsd     xmm3, QWORD PTR [32+rsp]
        movsd     xmm1, QWORD PTR [40+rsp]
        addsd     xmm3, xmm1
        movsd     QWORD PTR [48+rsp], xmm3
        movsd     xmm4, QWORD PTR [40+rsp]
        addsd     xmm4, QWORD PTR [imagerel(_vmldAtanHATab)+r10+rcx*8]
        movsd     QWORD PTR [40+rsp], xmm4
        movsd     xmm5, QWORD PTR [48+rsp]
        subsd     xmm6, xmm5
        movsd     QWORD PTR [48+rsp], xmm6
        movsd     xmm0, QWORD PTR [40+rsp]
        movsd     xmm6, QWORD PTR [48+rsp]
        addsd     xmm0, xmm6
        movsd     QWORD PTR [48+rsp], xmm0
        mov       r10, QWORD PTR [32+rsp]
        movsd     xmm0, QWORD PTR [48+rsp]
        mov       QWORD PTR [64+rsp], r10

_B6_51::

        movsd     xmm1, QWORD PTR [72+rsp]
        movaps    xmm2, xmm14
        lea       r9, QWORD PTR [__ImageBase]
        addsd     xmm1, xmm0
        shl       dl, 7
        addsd     xmm1, QWORD PTR [imagerel(_vmldAtanHATab)+8+r9+rcx*8]
        mov       cl, al
        shl       cl, 7
        movsd     QWORD PTR [72+rsp], xmm1
        shr       r10, 56
        mov       r11b, BYTE PTR [79+rsp]
        and       r10b, 127
        or        r10b, cl
        mov       cl, r11b
        mov       BYTE PTR [71+rsp], r10b
        and       cl, 127
        movsd     xmm0, QWORD PTR [64+rsp]
        movaps    xmm4, xmm0
        addsd     xmm2, xmm0
        shr       r11b, 7
        movsd     QWORD PTR [32+rsp], xmm2
        movsd     xmm3, QWORD PTR [32+rsp]
        subsd     xmm4, xmm3
        movsd     QWORD PTR [40+rsp], xmm4
        xor       al, r11b
        movsd     xmm6, QWORD PTR [32+rsp]
        movsd     xmm5, QWORD PTR [40+rsp]
        shl       al, 7
        addsd     xmm6, xmm5
        movsd     QWORD PTR [48+rsp], xmm6
        movsd     xmm1, QWORD PTR [40+rsp]
        addsd     xmm14, xmm1
        movsd     QWORD PTR [40+rsp], xmm14
        or        cl, al
        movsd     xmm14, QWORD PTR [48+rsp]
        mov       BYTE PTR [79+rsp], cl
        subsd     xmm0, xmm14
        movsd     QWORD PTR [48+rsp], xmm0
        movsd     xmm1, QWORD PTR [40+rsp]
        movsd     xmm0, QWORD PTR [48+rsp]
        addsd     xmm1, xmm0
        movsd     QWORD PTR [48+rsp], xmm1
        mov       rax, QWORD PTR [32+rsp]
        movsd     xmm2, QWORD PTR [48+rsp]
        mov       QWORD PTR [64+rsp], rax
        movsd     xmm3, QWORD PTR [64+rsp]
        addsd     xmm2, QWORD PTR [72+rsp]
        addsd     xmm15, xmm2
        movsd     QWORD PTR [72+rsp], xmm15
        addsd     xmm3, xmm15
        movsd     QWORD PTR [80+rsp], xmm3
        mov       cl, BYTE PTR [87+rsp]
        and       cl, 127
        or        cl, dl
        mov       BYTE PTR [87+rsp], cl
        movsd     xmm15, QWORD PTR [80+rsp]
        cvtsd2ss  xmm15, xmm15
        movss     DWORD PTR [r8], xmm15
        jmp       _B6_33

_B6_18::

        movsd     xmm4, QWORD PTR [56+rsp]
        mov       r10b, al
        movaps    xmm3, xmm4
        mulsd     xmm3, QWORD PTR [_vmldAtanHATab+2000]
        shl       r10b, 7
        shl       dl, 7
        movsd     QWORD PTR [32+rsp], xmm3
        movsd     xmm5, QWORD PTR [32+rsp]
        subsd     xmm5, QWORD PTR [56+rsp]
        movsd     QWORD PTR [40+rsp], xmm5
        movsd     xmm1, QWORD PTR [32+rsp]
        movsd     xmm0, QWORD PTR [40+rsp]
        subsd     xmm1, xmm0
        movsd     xmm0, QWORD PTR [_vmldAtanHATab+2000]
        mulsd     xmm0, xmm6
        movsd     QWORD PTR [32+rsp], xmm1
        movsd     xmm2, QWORD PTR [32+rsp]
        subsd     xmm4, xmm2
        movsd     QWORD PTR [40+rsp], xmm4
        movsd     xmm4, QWORD PTR [32+rsp]
        movsd     xmm5, QWORD PTR [40+rsp]
        movsd     QWORD PTR [32+rsp], xmm0
        movsd     xmm1, QWORD PTR [32+rsp]
        subsd     xmm1, xmm6
        movsd     QWORD PTR [40+rsp], xmm1
        movsd     xmm0, QWORD PTR [32+rsp]
        movsd     xmm3, QWORD PTR [40+rsp]
        subsd     xmm0, xmm3
        movsd     QWORD PTR [32+rsp], xmm0
        movsd     xmm1, QWORD PTR [32+rsp]
        movsd     xmm0, QWORD PTR [_vmldAtanHATab+1888]
        subsd     xmm6, xmm1
        movsd     QWORD PTR [40+rsp], xmm6
        movsd     xmm1, QWORD PTR [32+rsp]
        divsd     xmm0, xmm1
        movsd     xmm6, QWORD PTR [_vmldAtanHATab+2000]
        mulsd     xmm6, xmm0
        movsd     xmm2, QWORD PTR [40+rsp]
        movsd     QWORD PTR [40+rsp], xmm6
        movsd     xmm3, QWORD PTR [40+rsp]
        subsd     xmm3, xmm0
        movsd     QWORD PTR [48+rsp], xmm3
        movsd     xmm0, QWORD PTR [40+rsp]
        movsd     xmm6, QWORD PTR [48+rsp]
        movsd     xmm3, QWORD PTR [_vmldAtanHATab+1888]
        subsd     xmm0, xmm6
        movsd     QWORD PTR [48+rsp], xmm0
        movsd     xmm6, QWORD PTR [48+rsp]
        mulsd     xmm1, xmm6
        subsd     xmm3, xmm1
        movsd     xmm1, QWORD PTR [48+rsp]
        mulsd     xmm2, xmm1
        movsd     QWORD PTR [40+rsp], xmm2
        movsd     xmm2, QWORD PTR [40+rsp]
        subsd     xmm3, xmm2
        movsd     QWORD PTR [40+rsp], xmm3
        movsd     xmm0, QWORD PTR [40+rsp]
        mov       rcx, QWORD PTR [48+rsp]
        movsd     xmm6, QWORD PTR [40+rsp]
        movsd     xmm1, QWORD PTR [48+rsp]
        mov       QWORD PTR [64+rsp], rcx
        movsd     xmm2, QWORD PTR [64+rsp]
        addsd     xmm0, QWORD PTR [_vmldAtanHATab+1888]
        mulsd     xmm0, xmm6
        movaps    xmm6, xmm2
        mulsd     xmm6, xmm4
        mulsd     xmm2, xmm5
        mulsd     xmm0, xmm1
        movaps    xmm3, xmm0
        mulsd     xmm3, xmm5
        movaps    xmm5, xmm6
        mulsd     xmm4, xmm0
        movsd     xmm1, QWORD PTR [_vmldAtanHATab+2000]
        addsd     xmm3, xmm4
        addsd     xmm3, xmm2
        movsd     QWORD PTR [32+rsp], xmm3
        movsd     xmm0, QWORD PTR [32+rsp]
        addsd     xmm5, xmm0
        movsd     QWORD PTR [32+rsp], xmm5
        movsd     xmm4, QWORD PTR [32+rsp]
        subsd     xmm6, xmm4
        movsd     xmm4, QWORD PTR [_vmldAtanHATab+1872]
        addsd     xmm6, xmm0
        movsd     QWORD PTR [40+rsp], xmm6
        movsd     xmm6, QWORD PTR [88+rsp]
        mulsd     xmm6, xmm6
        mulsd     xmm4, xmm6
        movsd     xmm3, QWORD PTR [32+rsp]
        mulsd     xmm1, xmm3
        addsd     xmm4, QWORD PTR [_vmldAtanHATab+1864]
        mulsd     xmm4, xmm6
        movsd     xmm5, QWORD PTR [40+rsp]
        movsd     QWORD PTR [32+rsp], xmm1
        movsd     xmm2, QWORD PTR [32+rsp]
        addsd     xmm4, QWORD PTR [_vmldAtanHATab+1856]
        subsd     xmm2, xmm3
        mulsd     xmm4, xmm6
        movsd     QWORD PTR [40+rsp], xmm2
        addsd     xmm4, QWORD PTR [_vmldAtanHATab+1848]
        mulsd     xmm4, xmm6
        addsd     xmm4, QWORD PTR [_vmldAtanHATab+1840]
        mulsd     xmm4, xmm6
        addsd     xmm4, QWORD PTR [_vmldAtanHATab+1832]
        mulsd     xmm4, xmm6
        addsd     xmm4, QWORD PTR [_vmldAtanHATab+1824]
        mulsd     xmm4, xmm6
        movsd     xmm6, QWORD PTR [32+rsp]
        movsd     xmm0, QWORD PTR [40+rsp]
        subsd     xmm6, xmm0
        movsd     QWORD PTR [32+rsp], xmm6
        movsd     xmm0, QWORD PTR [32+rsp]
        movsd     xmm6, QWORD PTR [_vmldAtanHATab+2000]
        subsd     xmm3, xmm0
        mulsd     xmm6, xmm4
        movsd     QWORD PTR [40+rsp], xmm3
        movsd     xmm2, QWORD PTR [32+rsp]
        movsd     xmm3, QWORD PTR [40+rsp]
        movsd     QWORD PTR [32+rsp], xmm6
        addsd     xmm5, xmm3
        movsd     xmm0, QWORD PTR [32+rsp]
        subsd     xmm0, xmm4
        movsd     QWORD PTR [40+rsp], xmm0
        movsd     xmm6, QWORD PTR [32+rsp]
        movsd     xmm1, QWORD PTR [40+rsp]
        subsd     xmm6, xmm1
        movsd     QWORD PTR [32+rsp], xmm6
        movsd     xmm3, QWORD PTR [32+rsp]
        subsd     xmm4, xmm3
        movsd     QWORD PTR [40+rsp], xmm4
        movsd     xmm4, QWORD PTR [32+rsp]
        movsd     xmm6, QWORD PTR [40+rsp]
        movaps    xmm1, xmm4
        movaps    xmm0, xmm6
        mulsd     xmm0, xmm5
        mulsd     xmm4, xmm5
        mulsd     xmm6, xmm2
        mulsd     xmm1, xmm2
        addsd     xmm0, xmm4
        movaps    xmm3, xmm1
        addsd     xmm0, xmm6
        addsd     xmm3, xmm2
        movsd     QWORD PTR [32+rsp], xmm0
        movaps    xmm6, xmm1
        movsd     xmm0, QWORD PTR [32+rsp]
        movsd     QWORD PTR [32+rsp], xmm3
        movsd     xmm4, QWORD PTR [32+rsp]
        subsd     xmm6, xmm4
        movsd     QWORD PTR [40+rsp], xmm6
        movsd     xmm4, QWORD PTR [32+rsp]
        movsd     xmm3, QWORD PTR [40+rsp]
        addsd     xmm4, xmm3
        movsd     QWORD PTR [48+rsp], xmm4
        movsd     xmm6, QWORD PTR [40+rsp]
        addsd     xmm2, xmm6
        movsd     QWORD PTR [40+rsp], xmm2
        movsd     xmm2, QWORD PTR [48+rsp]
        subsd     xmm1, xmm2
        movsd     QWORD PTR [48+rsp], xmm1
        movsd     xmm6, QWORD PTR [40+rsp]
        movsd     xmm1, QWORD PTR [48+rsp]
        addsd     xmm6, xmm1
        movsd     QWORD PTR [48+rsp], xmm6
        mov       r9, QWORD PTR [32+rsp]
        movsd     xmm1, QWORD PTR [48+rsp]
        mov       QWORD PTR [64+rsp], r9
        addsd     xmm5, xmm1
        shr       r9, 56
        addsd     xmm0, xmm5
        and       r9b, 127
        movaps    xmm5, xmm14
        or        r9b, r10b
        mov       BYTE PTR [71+rsp], r9b
        movsd     xmm6, QWORD PTR [64+rsp]
        movsd     QWORD PTR [72+rsp], xmm0
        movaps    xmm1, xmm6
        mov       r11b, BYTE PTR [79+rsp]
        mov       cl, r11b
        shr       r11b, 7
        addsd     xmm5, xmm6
        movsd     QWORD PTR [32+rsp], xmm5
        movsd     xmm0, QWORD PTR [32+rsp]
        subsd     xmm1, xmm0
        movsd     QWORD PTR [40+rsp], xmm1
        xor       al, r11b
        movsd     xmm3, QWORD PTR [32+rsp]
        and       cl, 127
        movsd     xmm2, QWORD PTR [40+rsp]
        shl       al, 7
        addsd     xmm3, xmm2
        movsd     QWORD PTR [48+rsp], xmm3
        movsd     xmm4, QWORD PTR [40+rsp]
        addsd     xmm14, xmm4
        movsd     QWORD PTR [40+rsp], xmm14
        or        cl, al
        movsd     xmm14, QWORD PTR [48+rsp]
        mov       BYTE PTR [79+rsp], cl
        subsd     xmm6, xmm14
        movsd     QWORD PTR [48+rsp], xmm6
        movsd     xmm1, QWORD PTR [40+rsp]
        movsd     xmm0, QWORD PTR [48+rsp]
        addsd     xmm1, xmm0
        movsd     QWORD PTR [48+rsp], xmm1
        mov       rax, QWORD PTR [32+rsp]
        movsd     xmm2, QWORD PTR [48+rsp]
        mov       QWORD PTR [64+rsp], rax
        movsd     xmm3, QWORD PTR [64+rsp]
        addsd     xmm2, QWORD PTR [72+rsp]
        addsd     xmm15, xmm2
        movsd     QWORD PTR [72+rsp], xmm15
        addsd     xmm3, xmm15
        movsd     QWORD PTR [80+rsp], xmm3
        mov       cl, BYTE PTR [87+rsp]
        and       cl, 127
        or        cl, dl
        mov       BYTE PTR [87+rsp], cl
        movsd     xmm15, QWORD PTR [80+rsp]
        cvtsd2ss  xmm15, xmm15
        movss     DWORD PTR [r8], xmm15
        jmp       _B6_33

_B6_19::

        cmp       r9d, 74
        jge       _B6_21

_B6_20::

        mov       BYTE PTR [63+rsp], r11b
        divsd     xmm1, QWORD PTR [56+rsp]
        movsd     xmm0, QWORD PTR [_vmldAtanHATab+1928]
        shl       dl, 7
        subsd     xmm0, xmm1
        addsd     xmm0, QWORD PTR [_vmldAtanHATab+1920]
        movsd     QWORD PTR [80+rsp], xmm0
        mov       al, BYTE PTR [87+rsp]
        and       al, 127
        or        al, dl
        mov       BYTE PTR [87+rsp], al
        movsd     xmm1, QWORD PTR [80+rsp]
        cvtsd2ss  xmm1, xmm1
        movss     DWORD PTR [r8], xmm1
        jmp       _B6_33

_B6_21::

        movsd     xmm0, QWORD PTR [_vmldAtanHATab+1920]
        shl       dl, 7
        addsd     xmm0, QWORD PTR [_vmldAtanHATab+1928]
        movsd     QWORD PTR [80+rsp], xmm0
        mov       al, BYTE PTR [87+rsp]
        and       al, 127
        or        al, dl
        mov       BYTE PTR [87+rsp], al
        movsd     xmm1, QWORD PTR [80+rsp]
        cvtsd2ss  xmm1, xmm1
        movss     DWORD PTR [r8], xmm1
        jmp       _B6_33

_B6_22::

        test      al, al
        jne       _B6_32

_B6_23::

        mov       BYTE PTR [63+rsp], r11b
        pxor      xmm0, xmm0
        mov       BYTE PTR [71+rsp], cl
        movsd     xmm2, QWORD PTR [56+rsp]
        divsd     xmm2, QWORD PTR [64+rsp]
        cvtsd2ss  xmm0, xmm2
        movss     DWORD PTR [96+rsp], xmm0
        movzx     eax, WORD PTR [98+rsp]
        movsd     QWORD PTR [80+rsp], xmm2
        test      eax, 32640
        je        _B6_25

_B6_24::

        movsd     xmm0, QWORD PTR [_vmldAtanHATab+1888]
        shl       dl, 7
        addsd     xmm0, xmm2
        movsd     QWORD PTR [32+rsp], xmm0
        movsd     xmm1, QWORD PTR [32+rsp]
        mulsd     xmm2, xmm1
        movsd     QWORD PTR [80+rsp], xmm2
        mov       al, BYTE PTR [87+rsp]
        and       al, 127
        or        al, dl
        mov       BYTE PTR [87+rsp], al
        movsd     xmm2, QWORD PTR [80+rsp]
        cvtsd2ss  xmm2, xmm2
        movss     DWORD PTR [r8], xmm2
        jmp       _B6_33

_B6_25::

        movsd     xmm0, QWORD PTR [80+rsp]
        cvtsd2ss  xmm0, xmm0
        shl       dl, 7
        movss     DWORD PTR [96+rsp], xmm0
        movss     xmm2, DWORD PTR [96+rsp]
        movss     xmm1, DWORD PTR [96+rsp]
        mulss     xmm2, xmm1
        movss     DWORD PTR [96+rsp], xmm2
        movss     xmm3, DWORD PTR [96+rsp]
        cvtss2sd  xmm3, xmm3
        addsd     xmm3, QWORD PTR [80+rsp]
        movsd     QWORD PTR [80+rsp], xmm3
        mov       al, BYTE PTR [87+rsp]
        and       al, 127
        or        al, dl
        mov       BYTE PTR [87+rsp], al
        movsd     xmm4, QWORD PTR [80+rsp]
        cvtsd2ss  xmm4, xmm4
        movss     DWORD PTR [r8], xmm4
        jmp       _B6_33

_B6_27::

        test      r10d, r10d
        jne       _B6_21

_B6_28::

        test      DWORD PTR [72+rsp], 8388607
        jne       _B6_21

_B6_30::

        test      al, al
        jne       _B6_32

_B6_31::

        shl       dl, 7
        mov       rax, QWORD PTR [_vmldAtanHATab+1976]
        mov       QWORD PTR [80+rsp], rax
        shr       rax, 56
        and       al, 127
        or        al, dl
        mov       BYTE PTR [87+rsp], al
        movsd     xmm0, QWORD PTR [80+rsp]
        cvtsd2ss  xmm0, xmm0
        movss     DWORD PTR [r8], xmm0
        jmp       _B6_33

_B6_32::

        movsd     xmm0, QWORD PTR [_vmldAtanHATab+1936]
        shl       dl, 7
        addsd     xmm0, QWORD PTR [_vmldAtanHATab+1944]
        movsd     QWORD PTR [80+rsp], xmm0
        mov       al, BYTE PTR [87+rsp]
        and       al, 127
        or        al, dl
        mov       BYTE PTR [87+rsp], al
        movsd     xmm1, QWORD PTR [80+rsp]
        cvtsd2ss  xmm1, xmm1
        movss     DWORD PTR [r8], xmm1

_B6_33::

        movups    xmm6, XMMWORD PTR [112+rsp]
        xor       eax, eax
        movups    xmm14, XMMWORD PTR [144+rsp]
        movups    xmm15, XMMWORD PTR [128+rsp]
        add       rsp, 168
        ret

_B6_34::

        movsd     xmm0, QWORD PTR [_vmldAtanHATab+1984]
        mov       r10d, -1022
        mulsd     xmm1, xmm0
        movsd     QWORD PTR [56+rsp], xmm1
        jmp       _B6_16

_B6_35::

        cmp       r10d, 2047
        je        _B6_48

_B6_36::

        cmp       r9d, 2047
        je        _B6_46

_B6_37::

        movzx     r9d, WORD PTR [78+rsp]
        and       r9d, 32640
        cmp       r9d, 32640
        jne       _B6_21

_B6_39::

        cmp       ecx, 255
        je        _B6_43

_B6_40::

        test      al, al
        je        _B6_31
        jmp       _B6_32

_B6_43::

        test      al, al
        jne       _B6_45

_B6_44::

        movsd     xmm0, QWORD PTR [_vmldAtanHATab+1904]
        shl       dl, 7
        addsd     xmm0, QWORD PTR [_vmldAtanHATab+1912]
        movsd     QWORD PTR [80+rsp], xmm0
        mov       al, BYTE PTR [87+rsp]
        and       al, 127
        or        al, dl
        mov       BYTE PTR [87+rsp], al
        movsd     xmm1, QWORD PTR [80+rsp]
        cvtsd2ss  xmm1, xmm1
        movss     DWORD PTR [r8], xmm1
        jmp       _B6_33

_B6_45::

        movsd     xmm0, QWORD PTR [_vmldAtanHATab+1952]
        shl       dl, 7
        addsd     xmm0, QWORD PTR [_vmldAtanHATab+1960]
        movsd     QWORD PTR [80+rsp], xmm0
        mov       al, BYTE PTR [87+rsp]
        and       al, 127
        or        al, dl
        mov       BYTE PTR [87+rsp], al
        movsd     xmm1, QWORD PTR [80+rsp]
        cvtsd2ss  xmm1, xmm1
        movss     DWORD PTR [r8], xmm1
        jmp       _B6_33

_B6_46::

        test      DWORD PTR [76+rsp], 8388607
        je        _B6_37

_B6_47::

        addss     xmm3, xmm2
        movss     DWORD PTR [r8], xmm3
        jmp       _B6_33

_B6_48::

        test      DWORD PTR [72+rsp], 8388607
        jne       _B6_47
        jmp       _B6_36
        ALIGN     16

_B6_49::

__svml_satan2_ha_cout_rare_internal ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_satan2_ha_cout_rare_internal_B1_B48:
	DD	563713
	DD	485530
	DD	649365
	DD	587916
	DD	1376523

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B6_1
	DD	imagerel _B6_49
	DD	imagerel _unwind___svml_satan2_ha_cout_rare_internal_B1_B48

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_RDATA	SEGMENT     READ PAGE   'DATA'
	ALIGN  32
ione	DD	1065353216
	DD	-1082130432
	DD 14 DUP (0H)	
	PUBLIC __svml_satan2_ha_data_internal
__svml_satan2_ha_data_internal	DD	0
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
	DD	1078530016
	DD	1078530016
	DD	1078530016
	DD	1078530016
	DD	1078530016
	DD	1078530016
	DD	1078530016
	DD	1078530016
	DD	1078530016
	DD	1078530016
	DD	1078530016
	DD	1078530016
	DD	1078530016
	DD	1078530016
	DD	1078530016
	DD	1078530016
	DD	3047930835
	DD	3047930835
	DD	3047930835
	DD	3047930835
	DD	3047930835
	DD	3047930835
	DD	3047930835
	DD	3047930835
	DD	3047930835
	DD	3047930835
	DD	3047930835
	DD	3047930835
	DD	3047930835
	DD	3047930835
	DD	3047930835
	DD	3047930835
	DD	1052770304
	DD	1052770304
	DD	1052770304
	DD	1052770304
	DD	1052770304
	DD	1052770304
	DD	1052770304
	DD	1052770304
	DD	1052770304
	DD	1052770304
	DD	1052770304
	DD	1052770304
	DD	1052770304
	DD	1052770304
	DD	1052770304
	DD	1052770304
	DD	1060655596
	DD	1060655596
	DD	1060655596
	DD	1060655596
	DD	1060655596
	DD	1060655596
	DD	1060655596
	DD	1060655596
	DD	1060655596
	DD	1060655596
	DD	1060655596
	DD	1060655596
	DD	1060655596
	DD	1060655596
	DD	1060655596
	DD	1060655596
	DD	1071644672
	DD	1071644672
	DD	1071644672
	DD	1071644672
	DD	1071644672
	DD	1071644672
	DD	1071644672
	DD	1071644672
	DD	1071644672
	DD	1071644672
	DD	1071644672
	DD	1071644672
	DD	1071644672
	DD	1071644672
	DD	1071644672
	DD	1071644672
	DD	1076625408
	DD	1076625408
	DD	1076625408
	DD	1076625408
	DD	1076625408
	DD	1076625408
	DD	1076625408
	DD	1076625408
	DD	1076625408
	DD	1076625408
	DD	1076625408
	DD	1076625408
	DD	1076625408
	DD	1076625408
	DD	1076625408
	DD	1076625408
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
	DD	1055744824
	DD	1055744824
	DD	1055744824
	DD	1055744824
	DD	1055744824
	DD	1055744824
	DD	1055744824
	DD	1055744824
	DD	1055744824
	DD	1055744824
	DD	1055744824
	DD	1055744824
	DD	1055744824
	DD	1055744824
	DD	1055744824
	DD	1055744824
	DD	833369960
	DD	833369960
	DD	833369960
	DD	833369960
	DD	833369960
	DD	833369960
	DD	833369960
	DD	833369960
	DD	833369960
	DD	833369960
	DD	833369960
	DD	833369960
	DD	833369960
	DD	833369960
	DD	833369960
	DD	833369960
	DD	1061752792
	DD	1061752792
	DD	1061752792
	DD	1061752792
	DD	1061752792
	DD	1061752792
	DD	1061752792
	DD	1061752792
	DD	1061752792
	DD	1061752792
	DD	1061752792
	DD	1061752792
	DD	1061752792
	DD	1061752792
	DD	1061752792
	DD	1061752792
	DD	875071576
	DD	875071576
	DD	875071576
	DD	875071576
	DD	875071576
	DD	875071576
	DD	875071576
	DD	875071576
	DD	875071576
	DD	875071576
	DD	875071576
	DD	875071576
	DD	875071576
	DD	875071576
	DD	875071576
	DD	875071576
	DD	1066252048
	DD	1066252048
	DD	1066252048
	DD	1066252048
	DD	1066252048
	DD	1066252048
	DD	1066252048
	DD	1066252048
	DD	1066252048
	DD	1066252048
	DD	1066252048
	DD	1066252048
	DD	1066252048
	DD	1066252048
	DD	1066252048
	DD	1066252048
	DD	3034196096
	DD	3034196096
	DD	3034196096
	DD	3034196096
	DD	3034196096
	DD	3034196096
	DD	3034196096
	DD	3034196096
	DD	3034196096
	DD	3034196096
	DD	3034196096
	DD	3034196096
	DD	3034196096
	DD	3034196096
	DD	3034196096
	DD	3034196096
	DD	1070141400
	DD	1070141400
	DD	1070141400
	DD	1070141400
	DD	1070141400
	DD	1070141400
	DD	1070141400
	DD	1070141400
	DD	1070141400
	DD	1070141400
	DD	1070141400
	DD	1070141400
	DD	1070141400
	DD	1070141400
	DD	1070141400
	DD	1070141400
	DD	883460184
	DD	883460184
	DD	883460184
	DD	883460184
	DD	883460184
	DD	883460184
	DD	883460184
	DD	883460184
	DD	883460184
	DD	883460184
	DD	883460184
	DD	883460184
	DD	883460184
	DD	883460184
	DD	883460184
	DD	883460184
	DD	1034818345
	DD	1034818345
	DD	1034818345
	DD	1034818345
	DD	1034818345
	DD	1034818345
	DD	1034818345
	DD	1034818345
	DD	1034818345
	DD	1034818345
	DD	1034818345
	DD	1034818345
	DD	1034818345
	DD	1034818345
	DD	1034818345
	DD	1034818345
	DD	3188674126
	DD	3188674126
	DD	3188674126
	DD	3188674126
	DD	3188674126
	DD	3188674126
	DD	3188674126
	DD	3188674126
	DD	3188674126
	DD	3188674126
	DD	3188674126
	DD	3188674126
	DD	3188674126
	DD	3188674126
	DD	3188674126
	DD	3188674126
	DD	1045211707
	DD	1045211707
	DD	1045211707
	DD	1045211707
	DD	1045211707
	DD	1045211707
	DD	1045211707
	DD	1045211707
	DD	1045211707
	DD	1045211707
	DD	1045211707
	DD	1045211707
	DD	1045211707
	DD	1045211707
	DD	1045211707
	DD	1045211707
	DD	3198855788
	DD	3198855788
	DD	3198855788
	DD	3198855788
	DD	3198855788
	DD	3198855788
	DD	3198855788
	DD	3198855788
	DD	3198855788
	DD	3198855788
	DD	3198855788
	DD	3198855788
	DD	3198855788
	DD	3198855788
	DD	3198855788
	DD	3198855788
	DD	4294967295
	DD	4294967295
	DD	4294967295
	DD	4294967295
	DD	4294967295
	DD	4294967295
	DD	4294967295
	DD	4294967295
	DD	4294967295
	DD	4294967295
	DD	4294967295
	DD	4294967295
	DD	4294967295
	DD	4294967295
	DD	4294967295
	DD	4294967295
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
	DD	2172649472
	DD	2172649472
	DD	2172649472
	DD	2172649472
	DD	2172649472
	DD	2172649472
	DD	2172649472
	DD	2172649472
	DD	2172649472
	DD	2172649472
	DD	2172649472
	DD	2172649472
	DD	2172649472
	DD	2172649472
	DD	2172649472
	DD	2172649472
	DD	4219469824
	DD	4219469824
	DD	4219469824
	DD	4219469824
	DD	4219469824
	DD	4219469824
	DD	4219469824
	DD	4219469824
	DD	4219469824
	DD	4219469824
	DD	4219469824
	DD	4219469824
	DD	4219469824
	DD	4219469824
	DD	4219469824
	DD	4219469824
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
	DD	1078530011
	DD	1078530011
	DD	1078530011
	DD	1078530011
	DD	1078530011
	DD	1078530011
	DD	1078530011
	DD	1078530011
	DD	1078530011
	DD	1078530011
	DD	1078530011
	DD	1078530011
	DD	1078530011
	DD	1078530011
	DD	1078530011
	DD	1078530011
_vmldAtanHATab	DD	3892314112
	DD	1069799150
	DD	2332892550
	DD	1039715405
	DD	1342177280
	DD	1070305495
	DD	270726690
	DD	1041535749
	DD	939524096
	DD	1070817911
	DD	2253973841
	DD	3188654726
	DD	3221225472
	DD	1071277294
	DD	3853927037
	DD	1043226911
	DD	2818572288
	DD	1071767563
	DD	2677759107
	DD	1044314101
	DD	3355443200
	DD	1072103591
	DD	1636578514
	DD	3191094734
	DD	1476395008
	DD	1072475260
	DD	1864703685
	DD	3188646936
	DD	805306368
	DD	1072747407
	DD	192551812
	DD	3192726267
	DD	2013265920
	DD	1072892781
	DD	2240369452
	DD	1043768538
	DD	0
	DD	1072999953
	DD	3665168337
	DD	3192705970
	DD	402653184
	DD	1073084787
	DD	1227953434
	DD	3192313277
	DD	2013265920
	DD	1073142981
	DD	3853283127
	DD	1045277487
	DD	805306368
	DD	1073187261
	DD	1676192264
	DD	3192868861
	DD	134217728
	DD	1073217000
	DD	4290763938
	DD	1042034855
	DD	671088640
	DD	1073239386
	DD	994303084
	DD	3189643768
	DD	402653184
	DD	1073254338
	DD	1878067156
	DD	1042652475
	DD	1610612736
	DD	1073265562
	DD	670314820
	DD	1045138554
	DD	3221225472
	DD	1073273048
	DD	691126919
	DD	3189987794
	DD	3489660928
	DD	1073278664
	DD	1618990832
	DD	3188194509
	DD	1207959552
	DD	1073282409
	DD	2198872939
	DD	1044806069
	DD	3489660928
	DD	1073285217
	DD	2633982383
	DD	1042307894
	DD	939524096
	DD	1073287090
	DD	1059367786
	DD	3189114230
	DD	2281701376
	DD	1073288494
	DD	3158525533
	DD	1044484961
	DD	3221225472
	DD	1073289430
	DD	286581777
	DD	1044893263
	DD	4026531840
	DD	1073290132
	DD	2000245215
	DD	3191647611
	DD	134217728
	DD	1073290601
	DD	4205071590
	DD	1045035927
	DD	536870912
	DD	1073290952
	DD	2334392229
	DD	1043447393
	DD	805306368
	DD	1073291186
	DD	2281458177
	DD	3188885569
	DD	3087007744
	DD	1073291361
	DD	691611507
	DD	1044733832
	DD	3221225472
	DD	1073291478
	DD	1816229550
	DD	1044363390
	DD	2281701376
	DD	1073291566
	DD	1993843750
	DD	3189837440
	DD	134217728
	DD	1073291625
	DD	3654754496
	DD	1044970837
	DD	4026531840
	DD	1073291668
	DD	3224300229
	DD	3191935390
	DD	805306368
	DD	1073291698
	DD	2988777976
	DD	3188950659
	DD	536870912
	DD	1073291720
	DD	1030371341
	DD	1043402665
	DD	3221225472
	DD	1073291734
	DD	1524463765
	DD	1044361356
	DD	3087007744
	DD	1073291745
	DD	2754295320
	DD	1044731036
	DD	134217728
	DD	1073291753
	DD	3099629057
	DD	1044970710
	DD	2281701376
	DD	1073291758
	DD	962914160
	DD	3189838838
	DD	805306368
	DD	1073291762
	DD	3543908206
	DD	3188950786
	DD	4026531840
	DD	1073291764
	DD	1849909620
	DD	3191935434
	DD	3221225472
	DD	1073291766
	DD	1641333636
	DD	1044361352
	DD	536870912
	DD	1073291768
	DD	1373968792
	DD	1043402654
	DD	134217728
	DD	1073291769
	DD	2033191599
	DD	1044970710
	DD	3087007744
	DD	1073291769
	DD	4117947437
	DD	1044731035
	DD	805306368
	DD	1073291770
	DD	315378368
	DD	3188950787
	DD	2281701376
	DD	1073291770
	DD	2428571750
	DD	3189838838
	DD	3221225472
	DD	1073291770
	DD	1608007466
	DD	1044361352
	DD	4026531840
	DD	1073291770
	DD	1895711420
	DD	3191935434
	DD	134217728
	DD	1073291771
	DD	2031108713
	DD	1044970710
	DD	536870912
	DD	1073291771
	DD	1362518342
	DD	1043402654
	DD	805306368
	DD	1073291771
	DD	317461253
	DD	3188950787
	DD	939524096
	DD	1073291771
	DD	4117231784
	DD	1044731035
	DD	1073741824
	DD	1073291771
	DD	1607942376
	DD	1044361352
	DD	1207959552
	DD	1073291771
	DD	2428929577
	DD	3189838838
	DD	1207959552
	DD	1073291771
	DD	2031104645
	DD	1044970710
	DD	1342177280
	DD	1073291771
	DD	1895722602
	DD	3191935434
	DD	1342177280
	DD	1073291771
	DD	317465322
	DD	3188950787
	DD	1342177280
	DD	1073291771
	DD	1362515546
	DD	1043402654
	DD	1342177280
	DD	1073291771
	DD	1607942248
	DD	1044361352
	DD	1342177280
	DD	1073291771
	DD	4117231610
	DD	1044731035
	DD	1342177280
	DD	1073291771
	DD	2031104637
	DD	1044970710
	DD	1342177280
	DD	1073291771
	DD	1540251232
	DD	1045150466
	DD	1342177280
	DD	1073291771
	DD	2644671394
	DD	1045270303
	DD	1342177280
	DD	1073291771
	DD	2399244691
	DD	1045360181
	DD	1342177280
	DD	1073291771
	DD	803971124
	DD	1045420100
	DD	1476395008
	DD	1073291771
	DD	3613709523
	DD	3192879152
	DD	1476395008
	DD	1073291771
	DD	2263862659
	DD	3192849193
	DD	1476395008
	DD	1073291771
	DD	177735686
	DD	3192826724
	DD	1476395008
	DD	1073291771
	DD	1650295902
	DD	3192811744
	DD	1476395008
	DD	1073291771
	DD	2754716064
	DD	3192800509
	DD	1476395008
	DD	1073291771
	DD	3490996172
	DD	3192793019
	DD	1476395008
	DD	1073291771
	DD	1895722605
	DD	3192787402
	DD	1476395008
	DD	1073291771
	DD	2263862659
	DD	3192783657
	DD	1476395008
	DD	1073291771
	DD	3613709523
	DD	3192780848
	DD	1476395008
	DD	1073291771
	DD	1650295902
	DD	3192778976
	DD	1476395008
	DD	1073291771
	DD	177735686
	DD	3192777572
	DD	1476395008
	DD	1073291771
	DD	3490996172
	DD	3192776635
	DD	1476395008
	DD	1073291771
	DD	2754716064
	DD	3192775933
	DD	1476395008
	DD	1073291771
	DD	2263862659
	DD	3192775465
	DD	1476395008
	DD	1073291771
	DD	1895722605
	DD	3192775114
	DD	1476395008
	DD	1073291771
	DD	1650295902
	DD	3192774880
	DD	1476395008
	DD	1073291771
	DD	3613709523
	DD	3192774704
	DD	1476395008
	DD	1073291771
	DD	3490996172
	DD	3192774587
	DD	1476395008
	DD	1073291771
	DD	177735686
	DD	3192774500
	DD	1476395008
	DD	1073291771
	DD	2263862659
	DD	3192774441
	DD	1476395008
	DD	1073291771
	DD	2754716064
	DD	3192774397
	DD	1476395008
	DD	1073291771
	DD	1650295902
	DD	3192774368
	DD	1476395008
	DD	1073291771
	DD	1895722605
	DD	3192774346
	DD	1476395008
	DD	1073291771
	DD	3490996172
	DD	3192774331
	DD	1476395008
	DD	1073291771
	DD	3613709523
	DD	3192774320
	DD	1476395008
	DD	1073291771
	DD	2263862659
	DD	3192774313
	DD	1476395008
	DD	1073291771
	DD	177735686
	DD	3192774308
	DD	1476395008
	DD	1073291771
	DD	1650295902
	DD	3192774304
	DD	1476395008
	DD	1073291771
	DD	2754716064
	DD	3192774301
	DD	1476395008
	DD	1073291771
	DD	3490996172
	DD	3192774299
	DD	1476395008
	DD	1073291771
	DD	1895722605
	DD	3192774298
	DD	1476395008
	DD	1073291771
	DD	2263862659
	DD	3192774297
	DD	1476395008
	DD	1073291771
	DD	3613709523
	DD	3192774296
	DD	1476395008
	DD	1073291771
	DD	1650295902
	DD	3192774296
	DD	1476395008
	DD	1073291771
	DD	177735686
	DD	3192774296
	DD	1476395008
	DD	1073291771
	DD	3490996172
	DD	3192774295
	DD	1476395008
	DD	1073291771
	DD	2754716064
	DD	3192774295
	DD	1476395008
	DD	1073291771
	DD	2263862659
	DD	3192774295
	DD	1476395008
	DD	1073291771
	DD	1895722605
	DD	3192774295
	DD	1476395008
	DD	1073291771
	DD	1650295902
	DD	3192774295
	DD	1476395008
	DD	1073291771
	DD	1466225875
	DD	3192774295
	DD	1476395008
	DD	1073291771
	DD	1343512524
	DD	3192774295
	DD	1476395008
	DD	1073291771
	DD	1251477510
	DD	3192774295
	DD	1476395008
	DD	1073291771
	DD	1190120835
	DD	3192774295
	DD	1476395008
	DD	1073291771
	DD	1144103328
	DD	3192774295
	DD	1476395008
	DD	1073291771
	DD	1113424990
	DD	3192774295
	DD	1476395008
	DD	1073291771
	DD	1090416237
	DD	3192774295
	DD	1476395008
	DD	1073291771
	DD	1075077068
	DD	3192774295
	DD	1431655765
	DD	3218429269
	DD	2576978363
	DD	1070176665
	DD	2453154343
	DD	3217180964
	DD	4189149139
	DD	1069314502
	DD	1775019125
	DD	3216459198
	DD	273199057
	DD	1068739452
	DD	874748308
	DD	3215993277
	DD	0
	DD	1069547520
	DD	0
	DD	1072693248
	DD	0
	DD	1073741824
	DD	1413754136
	DD	1072243195
	DD	856972295
	DD	1015129638
	DD	1413754136
	DD	1073291771
	DD	856972295
	DD	1016178214
	DD	1413754136
	DD	1074340347
	DD	856972295
	DD	1017226790
	DD	2134057426
	DD	1073928572
	DD	1285458442
	DD	1016756537
	DD	0
	DD	3220176896
	DD	0
	DD	0
	DD	0
	DD	2144337920
	DD	0
	DD	1048576
	DD	33554432
	DD	1101004800
_RDATA	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS
EXTRN	__ImageBase:PROC
EXTRN	_fltused:BYTE
	ENDIF
	END
