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
	PUBLIC __svml_atan22_ha_ex

__svml_atan22_ha_ex	PROC

_B1_1::

        DB        243
        DB        15
        DB        30
        DB        250
L1::

        sub       rsp, 536
        lea       r10, QWORD PTR [__ImageBase]
        movups    XMMWORD PTR [320+rsp], xmm15
        xor       eax, eax
        movups    XMMWORD PTR [336+rsp], xmm14
        movups    XMMWORD PTR [352+rsp], xmm13
        movups    XMMWORD PTR [368+rsp], xmm12
        movups    XMMWORD PTR [400+rsp], xmm11
        movups    XMMWORD PTR [384+rsp], xmm10
        movups    XMMWORD PTR [304+rsp], xmm9
        movups    XMMWORD PTR [256+rsp], xmm8
        movups    XMMWORD PTR [288+rsp], xmm7
        movups    XMMWORD PTR [272+rsp], xmm6
        mov       QWORD PTR [512+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        movups    xmm13, XMMWORD PTR [rdx]
        and       r13, -64
        movups    xmm0, XMMWORD PTR [rcx]
        movaps    xmm1, xmm13
        movups    xmm4, XMMWORD PTR [__svml_datan2_ha_data_internal+896]
        movaps    xmm14, xmm13
        movups    xmm2, XMMWORD PTR [__svml_datan2_ha_data_internal+2880]
        andps     xmm1, xmm4
        andps     xmm4, xmm0
        pxor      xmm14, xmm1
        movups    XMMWORD PTR [416+rsp], xmm13
        movaps    xmm9, xmm4
        movups    XMMWORD PTR [432+rsp], xmm2
        movaps    xmm8, xmm1
        cmpltpd   xmm13, xmm2
        pshufd    xmm15, xmm1, 221
        pshufd    xmm2, xmm4, 221
        movdqa    xmm7, xmm15
        movq      xmm5, QWORD PTR [__svml_datan2_ha_data_internal+2688]
        movdqa    xmm6, xmm2
        movups    xmm12, XMMWORD PTR [__svml_datan2_ha_data_internal+1088]
        psubd     xmm7, xmm5
        psubd     xmm6, xmm5
        movdqa    xmm5, xmm2
        movq      xmm3, QWORD PTR [__svml_datan2_ha_data_internal+3200]
        movaps    xmm10, xmm12
        psubd     xmm5, xmm15
        andps     xmm10, xmm4
        movups    XMMWORD PTR [448+rsp], xmm4
        paddd     xmm5, xmm3
        movups    XMMWORD PTR [32+rsp], xmm0
        pxor      xmm0, xmm4
        movq      xmm4, QWORD PTR [__svml_datan2_ha_data_internal+2752]
        movdqa    xmm3, xmm7
        pcmpgtd   xmm3, xmm4
        pcmpeqd   xmm7, xmm4
        por       xmm3, xmm7
        movdqa    xmm7, xmm6
        pcmpgtd   xmm7, xmm4
        pcmpeqd   xmm6, xmm4
        por       xmm7, xmm6
        movaps    xmm11, xmm12
        movq      xmm6, QWORD PTR [__svml_datan2_ha_data_internal+3264]
        por       xmm3, xmm7
        movdqa    xmm15, xmm6
        pcmpeqd   xmm6, xmm5
        pcmpgtd   xmm15, xmm5
        andps     xmm11, xmm1
        por       xmm15, xmm6
        por       xmm3, xmm15
        movdqu    XMMWORD PTR [496+rsp], xmm3
        movups    xmm6, XMMWORD PTR [__svml_datan2_ha_data_internal+1152]
        movups    xmm7, XMMWORD PTR [__svml_datan2_ha_data_internal+1216]
        movups    xmm4, XMMWORD PTR [__svml_datan2_ha_data_internal+1280]
        movups    xmm3, XMMWORD PTR [__svml_datan2_ha_data_internal+1344]
        mulpd     xmm6, xmm1
        mulpd     xmm3, xmm1
        mulpd     xmm7, xmm1
        mulpd     xmm4, xmm1
        subpd     xmm8, xmm11
        subpd     xmm9, xmm10
        movups    XMMWORD PTR [464+rsp], xmm1
        pshufd    xmm6, xmm6, 221
        pshufd    xmm1, xmm3, 221
        psubd     xmm6, xmm2
        pshufd    xmm15, xmm7, 221
        psubd     xmm1, xmm2
        pshufd    xmm7, xmm4, 221
        psubd     xmm15, xmm2
        psubd     xmm7, xmm2
        psrad     xmm6, 31
        psrad     xmm1, 31
        psrad     xmm15, 31
        psrad     xmm7, 31
        paddd     xmm6, xmm1
        paddd     xmm15, xmm7
        movaps    xmm1, xmm11
        movq      xmm5, QWORD PTR [__svml_datan2_ha_data_internal+1408]
        paddd     xmm6, xmm15
        paddd     xmm6, xmm5
        movaps    xmm15, xmm8
        pslld     xmm6, 5
        movaps    xmm7, xmm9
        movd      r8d, xmm6
        pshufd    xmm2, xmm6, 1
        movups    XMMWORD PTR [480+rsp], xmm0
        movd      r9d, xmm2
        movaps    xmm2, xmm10
        movq      xmm3, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+r10+r8]
        movq      xmm4, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+24+r10+r8]
        movhpd    xmm3, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+r10+r9]
        mulpd     xmm1, xmm3
        mulpd     xmm15, xmm3
        mulpd     xmm10, xmm3
        mulpd     xmm3, xmm9
        movhpd    xmm4, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+24+r10+r9]
        andps     xmm2, xmm4
        andps     xmm7, xmm4
        subpd     xmm2, xmm1
        subpd     xmm7, xmm15
        andps     xmm11, xmm4
        andps     xmm4, xmm8
        addpd     xmm11, xmm10
        addpd     xmm4, xmm3
        movaps    xmm1, xmm2
        movaps    xmm9, xmm11
        subpd     xmm1, xmm7
        addpd     xmm9, xmm4
        movaps    xmm15, xmm1
        subpd     xmm15, xmm2
        subpd     xmm11, xmm9
        subpd     xmm7, xmm15
        addpd     xmm4, xmm11
        movaps    xmm2, xmm12
        andps     xmm12, xmm9
        subpd     xmm9, xmm12
        pshufd    xmm15, xmm12, 221
        andps     xmm2, xmm1
        movdqa    xmm11, xmm15
        movq      xmm8, QWORD PTR [__svml_datan2_ha_data_internal+1600]
        pslld     xmm11, 3
        movq      xmm10, QWORD PTR [__svml_datan2_ha_data_internal+1664]
        pand      xmm11, xmm8
        por       xmm11, xmm10
        addpd     xmm4, xmm9
        subpd     xmm1, xmm2
        rcpps     xmm9, xmm11
        addpd     xmm7, xmm1
        movq      xmm3, QWORD PTR [__svml_datan2_ha_data_internal+1472]
        psrld     xmm9, 3
        movq      xmm8, QWORD PTR [__svml_datan2_ha_data_internal+1536]
        pand      xmm15, xmm3
        movq      xmm1, QWORD PTR [__svml_datan2_ha_data_internal+1728]
        psubd     xmm8, xmm15
        psubd     xmm9, xmm1
        movaps    xmm1, xmm2
        paddd     xmm9, xmm8
        pshufd    xmm10, xmm9, 80
        andps     xmm10, XMMWORD PTR [__svml_datan2_ha_data_internal+3008]
        mulpd     xmm12, xmm10
        mulpd     xmm4, xmm10
        subpd     xmm12, XMMWORD PTR [__svml_datan2_ha_data_internal+1792]
        mulpd     xmm2, xmm10
        addpd     xmm12, xmm4
        movaps    xmm4, xmm12
        mulpd     xmm4, xmm12
        subpd     xmm4, xmm12
        mulpd     xmm4, xmm12
        addpd     xmm4, xmm12
        mulpd     xmm4, xmm12
        subpd     xmm4, xmm12
        mulpd     xmm4, xmm12
        addpd     xmm4, xmm12
        mulpd     xmm4, xmm12
        subpd     xmm4, xmm12
        mulpd     xmm4, xmm10
        mulpd     xmm1, xmm4
        mulpd     xmm4, xmm7
        mulpd     xmm7, xmm10
        addpd     xmm1, xmm4
        addpd     xmm1, xmm7
        movaps    xmm8, xmm1
        addpd     xmm8, xmm2
        movaps    xmm3, xmm8
        mulpd     xmm3, xmm8
        movaps    xmm7, xmm3
        mulpd     xmm7, xmm3
        movups    xmm4, XMMWORD PTR [__svml_datan2_ha_data_internal+1856]
        mulpd     xmm4, xmm7
        movups    xmm12, XMMWORD PTR [__svml_datan2_ha_data_internal+1920]
        addpd     xmm4, XMMWORD PTR [__svml_datan2_ha_data_internal+1984]
        mulpd     xmm12, xmm7
        mulpd     xmm4, xmm7
        addpd     xmm12, XMMWORD PTR [__svml_datan2_ha_data_internal+2048]
        addpd     xmm4, XMMWORD PTR [__svml_datan2_ha_data_internal+2112]
        mulpd     xmm12, xmm7
        mulpd     xmm4, xmm7
        addpd     xmm12, XMMWORD PTR [__svml_datan2_ha_data_internal+2176]
        addpd     xmm4, XMMWORD PTR [__svml_datan2_ha_data_internal+2240]
        mulpd     xmm12, xmm7
        mulpd     xmm4, xmm7
        addpd     xmm12, XMMWORD PTR [__svml_datan2_ha_data_internal+2304]
        addpd     xmm4, XMMWORD PTR [__svml_datan2_ha_data_internal+2368]
        mulpd     xmm12, xmm7
        mulpd     xmm4, xmm7
        addpd     xmm12, XMMWORD PTR [__svml_datan2_ha_data_internal+2432]
        addpd     xmm4, XMMWORD PTR [__svml_datan2_ha_data_internal+2496]
        mulpd     xmm7, xmm12
        mulpd     xmm4, xmm3
        addpd     xmm7, XMMWORD PTR [__svml_datan2_ha_data_internal+2560]
        addpd     xmm7, xmm4
        mulpd     xmm3, xmm7
        movq      xmm5, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+16+r10+r8]
        movhpd    xmm5, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+16+r10+r9]
        addpd     xmm5, xmm1
        mulpd     xmm8, xmm3
        movups    xmm0, XMMWORD PTR [__svml_datan2_ha_data_internal+960]
        andps     xmm0, xmm13
        andps     xmm13, XMMWORD PTR [__svml_datan2_ha_data_internal+1024]
        pxor      xmm13, xmm14
        addpd     xmm13, xmm5
        addpd     xmm13, xmm8
        addpd     xmm2, xmm13
        movq      xmm6, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+8+r10+r8]
        movhpd    xmm6, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+8+r10+r9]
        addpd     xmm6, xmm2
        pxor      xmm6, xmm14
        addpd     xmm0, xmm6
        movdqu    xmm6, XMMWORD PTR [496+rsp]
        movmskps  r11d, xmm6
        orps      xmm0, XMMWORD PTR [480+rsp]
        movups    xmm10, XMMWORD PTR [464+rsp]
        mov       QWORD PTR [520+rsp], r13
        test      r11d, 3
        jne       _B1_12

_B1_2::

        test      eax, eax
        jne       _B1_4

_B1_3::

        movups    xmm6, XMMWORD PTR [272+rsp]
        movups    xmm7, XMMWORD PTR [288+rsp]
        movups    xmm8, XMMWORD PTR [256+rsp]
        movups    xmm9, XMMWORD PTR [304+rsp]
        movups    xmm10, XMMWORD PTR [384+rsp]
        movups    xmm11, XMMWORD PTR [400+rsp]
        movups    xmm12, XMMWORD PTR [368+rsp]
        movups    xmm13, XMMWORD PTR [352+rsp]
        movups    xmm14, XMMWORD PTR [336+rsp]
        movups    xmm15, XMMWORD PTR [320+rsp]
        mov       r13, QWORD PTR [512+rsp]
        add       rsp, 536
        ret

_B1_4::

        movups    xmm1, XMMWORD PTR [32+rsp]
        movups    xmm2, XMMWORD PTR [416+rsp]
        movups    XMMWORD PTR [r13], xmm1
        movups    XMMWORD PTR [64+r13], xmm2
        movups    XMMWORD PTR [128+r13], xmm0
        je        _B1_3

_B1_7::

        xor       ecx, ecx
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, ecx
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, eax

_B1_8::

        mov       ecx, ebx
        mov       edx, 1
        shl       edx, cl
        test      esi, edx
        jne       _B1_11

_B1_9::

        inc       ebx
        cmp       ebx, 2
        jl        _B1_8

_B1_10::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        movups    xmm0, XMMWORD PTR [128+r13]
        jmp       _B1_3

_B1_11::

        lea       rcx, QWORD PTR [r13+rbx*8]
        lea       rdx, QWORD PTR [64+r13+rbx*8]
        lea       r8, QWORD PTR [128+r13+rbx*8]

        call      __svml_datan2_ha_cout_rare_internal
        jmp       _B1_9

_B1_12::

        movups    xmm9, XMMWORD PTR [448+rsp]
        movaps    xmm7, xmm9
        cmpnltpd  xmm7, xmm10
        movups    xmm5, XMMWORD PTR [416+rsp]
        movaps    xmm2, xmm7
        movups    xmm8, XMMWORD PTR [32+rsp]
        movaps    xmm11, xmm5
        movups    xmm13, XMMWORD PTR [432+rsp]
        andnps    xmm2, xmm10
        movups    xmm3, XMMWORD PTR [__svml_datan2_ha_data_internal+832]
        movups    xmm1, XMMWORD PTR [__svml_datan2_ha_data_internal+3136]
        movaps    xmm4, xmm3
        cmpordpd  xmm11, xmm5
        cmpeqpd   xmm10, xmm13
        andps     xmm3, xmm8
        andps     xmm1, xmm7
        cmpordpd  xmm8, xmm8
        andps     xmm7, xmm9
        andps     xmm11, xmm8
        cmpeqpd   xmm9, xmm13
        orps      xmm2, xmm7
        orps      xmm10, xmm9
        cmpeqpd   xmm2, xmm13
        pshufd    xmm7, xmm10, 221
        movaps    xmm8, xmm2
        pshufd    xmm10, xmm11, 221
        andnps    xmm8, xmm1
        pand      xmm7, xmm10
        andps     xmm2, xmm13
        movdqa    xmm12, xmm7
        andps     xmm4, xmm5
        pshufd    xmm14, xmm13, 221
        pandn     xmm12, xmm6
        pshufd    xmm6, xmm5, 221
        orps      xmm8, xmm2
        pcmpgtd   xmm14, xmm6
        orps      xmm8, xmm4
        pshufd    xmm15, xmm14, 80
        andps     xmm15, XMMWORD PTR [__svml_datan2_ha_data_internal+3072]
        addpd     xmm8, xmm15
        movmskps  eax, xmm12
        pshufd    xmm1, xmm7, 80
        orps      xmm8, xmm3
        movdqa    xmm2, xmm1
        andps     xmm8, xmm1
        andnps    xmm2, xmm0
        and       eax, 3
        movaps    xmm0, xmm2
        orps      xmm0, xmm8
        jmp       _B1_2
        ALIGN     16

_B1_13::

__svml_atan22_ha_ex ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_atan22_ha_ex_B1_B4:
	DD	1602561
	DD	4248692
	DD	1140844
	DD	1210468
	DD	1083484
	DD	1284179
	DD	1615946
	DD	1685569
	DD	1558584
	DD	1497135
	DD	1435686
	DD	1374235
	DD	4391179

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B1_1
	DD	imagerel _B1_7
	DD	imagerel _unwind___svml_atan22_ha_ex_B1_B4

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_atan22_ha_ex_B7_B11:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B1_1
	DD	imagerel _B1_7
	DD	imagerel _unwind___svml_atan22_ha_ex_B1_B4

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B1_7
	DD	imagerel _B1_12
	DD	imagerel _unwind___svml_atan22_ha_ex_B7_B11

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_atan22_ha_ex_B12_B12:
	DD	33
	DD	imagerel _B1_1
	DD	imagerel _B1_7
	DD	imagerel _unwind___svml_atan22_ha_ex_B1_B4

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B1_12
	DD	imagerel _B1_13
	DD	imagerel _unwind___svml_atan22_ha_ex_B12_B12

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST1:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_atan21_ha_ex

__svml_atan21_ha_ex	PROC

_B2_1::

        DB        243
        DB        15
        DB        30
        DB        250
L44::

        sub       rsp, 488
        mov       r8d, -2144337920
        movups    XMMWORD PTR [240+rsp], xmm15
        mov       r10d, 1005584384
        movups    XMMWORD PTR [256+rsp], xmm14
        mov       r9d, -36700160
        movups    XMMWORD PTR [272+rsp], xmm13
        mov       r11d, 4
        movups    XMMWORD PTR [288+rsp], xmm12
        movd      xmm14, r8d
        movups    XMMWORD PTR [304+rsp], xmm11
        lea       rax, QWORD PTR [__ImageBase]
        movups    XMMWORD PTR [320+rsp], xmm10
        mov       r8d, 8388607
        movups    XMMWORD PTR [336+rsp], xmm9
        movups    XMMWORD PTR [352+rsp], xmm8
        movups    XMMWORD PTR [368+rsp], xmm7
        movups    XMMWORD PTR [384+rsp], xmm6
        mov       QWORD PTR [232+rsp], r13
        lea       r13, QWORD PTR [95+rsp]
        movups    xmm9, XMMWORD PTR [rdx]
        and       r13, -64
        movsd     xmm13, QWORD PTR [__svml_datan2_ha_data_internal+2880]
        movaps    xmm12, xmm9
        movups    XMMWORD PTR [416+rsp], xmm9
        movaps    xmm10, xmm9
        movups    xmm0, XMMWORD PTR [rcx]
        xor       ecx, ecx
        movsd     xmm5, QWORD PTR [__svml_datan2_ha_data_internal+896]
        cmpltsd   xmm9, xmm13
        andps     xmm12, xmm5
        andps     xmm5, xmm0
        movups    XMMWORD PTR [400+rsp], xmm0
        pxor      xmm0, xmm5
        movups    XMMWORD PTR [464+rsp], xmm0
        movaps    xmm2, xmm5
        movups    XMMWORD PTR [432+rsp], xmm13
        movaps    xmm1, xmm12
        movsd     xmm11, QWORD PTR [__svml_datan2_ha_data_internal+960]
        pxor      xmm10, xmm12
        movsd     xmm7, QWORD PTR [__svml_datan2_ha_data_internal+1024]
        andps     xmm11, xmm9
        pshufd    xmm13, xmm12, 85
        andps     xmm9, xmm7
        pshufd    xmm0, xmm5, 85
        movdqa    xmm15, xmm13
        movsd     xmm8, QWORD PTR [__svml_datan2_ha_data_internal+1088]
        movdqa    xmm7, xmm0
        movaps    xmm3, xmm8
        psubd     xmm15, xmm14
        psubd     xmm7, xmm13
        movd      xmm13, r10d
        movups    XMMWORD PTR [448+rsp], xmm5
        andps     xmm3, xmm5
        movd      xmm5, r9d
        movdqa    xmm6, xmm0
        paddd     xmm7, xmm13
        movdqa    xmm13, xmm15
        psubd     xmm6, xmm14
        pcmpgtd   xmm13, xmm5
        pcmpeqd   xmm15, xmm5
        movaps    xmm14, xmm12
        por       xmm13, xmm15
        movdqa    xmm15, xmm6
        pcmpgtd   xmm15, xmm5
        pcmpeqd   xmm6, xmm5
        por       xmm15, xmm6
        movaps    xmm4, xmm8
        pxor      xmm5, xmm5
        pxor      xmm6, xmm6
        pcmpgtd   xmm5, xmm7
        pcmpeqd   xmm6, xmm7
        por       xmm13, xmm15
        por       xmm5, xmm6
        movaps    xmm7, xmm12
        movaps    xmm15, xmm12
        movaps    xmm6, xmm12
        por       xmm13, xmm5
        mulsd     xmm7, QWORD PTR [__svml_datan2_ha_data_internal+1152]
        andps     xmm4, xmm12
        subsd     xmm1, xmm4
        mulsd     xmm6, QWORD PTR [__svml_datan2_ha_data_internal+1216]
        subsd     xmm2, xmm3
        mulsd     xmm14, QWORD PTR [__svml_datan2_ha_data_internal+1280]
        mulsd     xmm15, QWORD PTR [__svml_datan2_ha_data_internal+1344]
        pshufd    xmm5, xmm7, 85
        mov       r9d, 1065353216
        pshufd    xmm7, xmm15, 85
        psubd     xmm5, xmm0
        pshufd    xmm6, xmm6, 85
        psubd     xmm7, xmm0
        pshufd    xmm14, xmm14, 85
        psubd     xmm6, xmm0
        psubd     xmm14, xmm0
        psrad     xmm5, 31
        psrad     xmm7, 31
        psrad     xmm6, 31
        psrad     xmm14, 31
        paddd     xmm5, xmm7
        paddd     xmm6, xmm14
        movd      xmm0, r11d
        paddd     xmm5, xmm6
        mov       r11d, -1048576
        paddd     xmm5, xmm0
        mov       r10d, 133169152
        pslld     xmm5, 5
        pxor      xmm9, xmm10
        movd      edx, xmm5
        movq      xmm6, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+rax+rdx]
        movdqa    xmm5, xmm6
        movdqa    xmm7, xmm6
        mulsd     xmm5, xmm4
        mulsd     xmm7, xmm1
        movq      xmm14, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+24+rax+rdx]
        movdqa    xmm0, xmm14
        movdqa    xmm15, xmm14
        andps     xmm0, xmm3
        andps     xmm15, xmm2
        andps     xmm4, xmm14
        andps     xmm14, xmm1
        movdqa    xmm1, xmm6
        subsd     xmm0, xmm5
        mulsd     xmm1, xmm3
        subsd     xmm15, xmm7
        mulsd     xmm6, xmm2
        addsd     xmm1, xmm4
        addsd     xmm6, xmm14
        movaps    xmm7, xmm0
        movaps    xmm2, xmm1
        movd      xmm3, r8d
        movd      xmm4, r9d
        mov       r8d, 2145386496
        subsd     xmm7, xmm15
        addsd     xmm2, xmm6
        movaps    xmm5, xmm7
        subsd     xmm1, xmm2
        subsd     xmm5, xmm0
        addsd     xmm6, xmm1
        subsd     xmm15, xmm5
        movaps    xmm5, xmm8
        andps     xmm8, xmm2
        pshufd    xmm14, xmm8, 85
        andps     xmm5, xmm7
        movdqa    xmm0, xmm14
        movd      xmm1, r10d
        pslld     xmm0, 3
        subsd     xmm2, xmm8
        subsd     xmm7, xmm5
        addsd     xmm6, xmm2
        addsd     xmm7, xmm15
        pand      xmm0, xmm3
        movd      xmm2, r11d
        por       xmm0, xmm4
        pand      xmm14, xmm2
        rcpps     xmm0, xmm0
        psrld     xmm0, 3
        movd      xmm3, r8d
        psubd     xmm0, xmm1
        psubd     xmm3, xmm14
        paddd     xmm0, xmm3
        movsd     xmm15, QWORD PTR [__svml_datan2_ha_data_internal+3008]
        pshufd    xmm0, xmm0, 0
        andps     xmm0, xmm15
        mulsd     xmm8, xmm0
        movaps    xmm1, xmm0
        mulsd     xmm6, xmm0
        mulsd     xmm1, xmm7
        subsd     xmm8, QWORD PTR [__svml_datan2_ha_data_internal+1792]
        movsd     xmm2, QWORD PTR [__svml_datan2_ha_data_internal+1856]
        addsd     xmm6, xmm8
        movaps    xmm8, xmm6
        mulsd     xmm8, xmm6
        mov       QWORD PTR [480+rsp], r13
        subsd     xmm8, xmm6
        mulsd     xmm8, xmm6
        addsd     xmm8, xmm6
        mulsd     xmm8, xmm6
        subsd     xmm8, xmm6
        mulsd     xmm8, xmm6
        addsd     xmm8, xmm6
        mulsd     xmm8, xmm6
        subsd     xmm8, xmm6
        mulsd     xmm8, xmm0
        mulsd     xmm0, xmm5
        movaps    xmm6, xmm8
        movaps    xmm15, xmm0
        mulsd     xmm6, xmm5
        mulsd     xmm8, xmm7
        movsd     xmm7, QWORD PTR [__svml_datan2_ha_data_internal+1920]
        addsd     xmm8, xmm6
        addsd     xmm1, xmm8
        addsd     xmm15, xmm1
        addsd     xmm1, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+16+rax+rdx]
        movaps    xmm14, xmm15
        addsd     xmm1, xmm9
        mulsd     xmm14, xmm15
        movaps    xmm5, xmm14
        mulsd     xmm5, xmm14
        mulsd     xmm2, xmm5
        mulsd     xmm7, xmm5
        addsd     xmm2, QWORD PTR [__svml_datan2_ha_data_internal+1984]
        addsd     xmm7, QWORD PTR [__svml_datan2_ha_data_internal+2048]
        mulsd     xmm2, xmm5
        mulsd     xmm7, xmm5
        addsd     xmm2, QWORD PTR [__svml_datan2_ha_data_internal+2112]
        addsd     xmm7, QWORD PTR [__svml_datan2_ha_data_internal+2176]
        mulsd     xmm2, xmm5
        mulsd     xmm7, xmm5
        addsd     xmm2, QWORD PTR [__svml_datan2_ha_data_internal+2240]
        addsd     xmm7, QWORD PTR [__svml_datan2_ha_data_internal+2304]
        mulsd     xmm2, xmm5
        mulsd     xmm7, xmm5
        addsd     xmm2, QWORD PTR [__svml_datan2_ha_data_internal+2368]
        addsd     xmm7, QWORD PTR [__svml_datan2_ha_data_internal+2432]
        mulsd     xmm2, xmm5
        mulsd     xmm7, xmm5
        addsd     xmm2, QWORD PTR [__svml_datan2_ha_data_internal+2496]
        addsd     xmm7, QWORD PTR [__svml_datan2_ha_data_internal+2560]
        mulsd     xmm2, xmm14
        addsd     xmm2, xmm7
        mulsd     xmm2, xmm14
        mulsd     xmm2, xmm15
        addsd     xmm2, xmm1
        addsd     xmm0, xmm2
        addsd     xmm0, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+8+rax+rdx]
        movmskps  eax, xmm13
        pxor      xmm0, xmm10
        movups    xmm10, XMMWORD PTR [448+rsp]
        addsd     xmm0, xmm11
        orps      xmm0, XMMWORD PTR [464+rsp]
        test      al, 1
        jne       _B2_8

_B2_2::

        test      ecx, ecx
        jne       _B2_4

_B2_3::

        movups    xmm6, XMMWORD PTR [384+rsp]
        movups    xmm7, XMMWORD PTR [368+rsp]
        movups    xmm8, XMMWORD PTR [352+rsp]
        movups    xmm9, XMMWORD PTR [336+rsp]
        movups    xmm10, XMMWORD PTR [320+rsp]
        movups    xmm11, XMMWORD PTR [304+rsp]
        movups    xmm12, XMMWORD PTR [288+rsp]
        movups    xmm13, XMMWORD PTR [272+rsp]
        movups    xmm14, XMMWORD PTR [256+rsp]
        movups    xmm15, XMMWORD PTR [240+rsp]
        mov       r13, QWORD PTR [232+rsp]
        add       rsp, 488
        ret

_B2_4::

        movups    xmm1, XMMWORD PTR [400+rsp]
        movups    xmm2, XMMWORD PTR [416+rsp]
        movsd     QWORD PTR [r13], xmm1
        movsd     QWORD PTR [64+r13], xmm2
        movsd     QWORD PTR [128+r13], xmm0
        jne       _B2_6

_B2_5::

        movsd     xmm0, QWORD PTR [128+r13]
        jmp       _B2_3

_B2_6::

        je        _B2_5

_B2_7::

        lea       rcx, QWORD PTR [r13]
        lea       rdx, QWORD PTR [64+r13]
        lea       r8, QWORD PTR [128+r13]

        call      __svml_datan2_ha_cout_rare_internal
        jmp       _B2_5

_B2_8::

        movaps    xmm8, xmm10
        movups    xmm7, XMMWORD PTR [416+rsp]
        cmpnltsd  xmm8, xmm12
        movups    xmm9, XMMWORD PTR [400+rsp]
        movaps    xmm2, xmm8
        movups    xmm1, XMMWORD PTR [432+rsp]
        movaps    xmm11, xmm7
        movsd     xmm4, QWORD PTR [__svml_datan2_ha_data_internal+832]
        andnps    xmm2, xmm12
        movsd     xmm3, QWORD PTR [__svml_datan2_ha_data_internal+3136]
        movaps    xmm5, xmm4
        andps     xmm4, xmm9
        andps     xmm3, xmm8
        andps     xmm8, xmm10
        andps     xmm5, xmm7
        orps      xmm2, xmm8
        cmpordsd  xmm11, xmm7
        cmpordsd  xmm9, xmm9
        cmpeqsd   xmm12, xmm1
        cmpeqsd   xmm10, xmm1
        cmpeqsd   xmm2, xmm1
        andps     xmm11, xmm9
        orps      xmm12, xmm10
        pshufd    xmm12, xmm12, 85
        movaps    xmm8, xmm2
        pshufd    xmm10, xmm11, 85
        andnps    xmm8, xmm3
        pand      xmm12, xmm10
        andps     xmm2, xmm1
        movdqa    xmm14, xmm12
        orps      xmm8, xmm2
        pshufd    xmm15, xmm1, 85
        pandn     xmm14, xmm13
        pshufd    xmm13, xmm7, 85
        orps      xmm8, xmm5
        pcmpgtd   xmm15, xmm13
        movsd     xmm6, QWORD PTR [__svml_datan2_ha_data_internal+3072]
        pshufd    xmm1, xmm15, 0
        andps     xmm1, xmm6
        pshufd    xmm2, xmm12, 0
        addsd     xmm8, xmm1
        movmskps  ecx, xmm14
        movdqa    xmm3, xmm2
        orps      xmm8, xmm4
        andnps    xmm3, xmm0
        andps     xmm8, xmm2
        movaps    xmm0, xmm3
        and       ecx, 1
        orps      xmm0, xmm8
        jmp       _B2_2
        ALIGN     16

_B2_9::

__svml_atan21_ha_ex ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_atan21_ha_ex_B1_B8:
	DD	1611009
	DD	1954965
	DD	1599629
	DD	1538181
	DD	1476733
	DD	1415284
	DD	1353829
	DD	1292373
	DD	1230919
	DD	1169464
	DD	1108009
	DD	1046554
	DD	3997963

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B2_1
	DD	imagerel _B2_9
	DD	imagerel _unwind___svml_atan21_ha_ex_B1_B8

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST2:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_atan24_ha_l9

__svml_atan24_ha_l9	PROC

_B3_1::

        DB        243
        DB        15
        DB        30
        DB        250
L79::

        sub       rsp, 456
        xor       r8d, r8d
        vmovups   XMMWORD PTR [272+rsp], xmm15
        vmovups   XMMWORD PTR [288+rsp], xmm14
        vmovups   XMMWORD PTR [304+rsp], xmm13
        vmovups   XMMWORD PTR [320+rsp], xmm12
        vmovups   XMMWORD PTR [336+rsp], xmm11
        vmovups   XMMWORD PTR [352+rsp], xmm10
        vmovups   XMMWORD PTR [368+rsp], xmm9
        vmovups   XMMWORD PTR [384+rsp], xmm8
        vmovups   XMMWORD PTR [416+rsp], xmm7
        vmovups   XMMWORD PTR [400+rsp], xmm6
        mov       QWORD PTR [432+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovupd   ymm3, YMMWORD PTR [rcx]
        and       r13, -64
        vmovupd   ymm7, YMMWORD PTR [rdx]
        vmovupd   ymm5, YMMWORD PTR [__svml_datan2_ha_data_internal+896]
        vmovupd   ymm4, YMMWORD PTR [__svml_datan2_ha_data_internal+2880]
        vmovups   xmm15, XMMWORD PTR [__svml_datan2_ha_data_internal+2688]
        vmovupd   YMMWORD PTR [32+r13], ymm3
        vcmplt_oqpd ymm0, ymm7, ymm4
        vmovupd   YMMWORD PTR [r13], ymm4
        vandpd    ymm6, ymm7, ymm5
        lea       rdx, QWORD PTR [__ImageBase]
        vandpd    ymm5, ymm3, ymm5
        vxorpd    ymm1, ymm7, ymm6
        vxorpd    ymm3, ymm3, ymm5
        vandpd    ymm2, ymm0, YMMWORD PTR [__svml_datan2_ha_data_internal+960]
        vandpd    ymm0, ymm0, YMMWORD PTR [__svml_datan2_ha_data_internal+1024]
        vxorpd    ymm0, ymm0, ymm1
        mov       QWORD PTR [440+rsp], r13
        vextracti128 xmm4, ymm6, 1
        vextracti128 xmm13, ymm5, 1
        vshufps   xmm8, xmm6, xmm4, 221
        vshufps   xmm12, xmm5, xmm13, 221
        vpsubd    xmm14, xmm8, xmm15
        vmovups   xmm4, XMMWORD PTR [__svml_datan2_ha_data_internal+2752]
        vpsubd    xmm10, xmm12, xmm15
        vpcmpgtd  xmm11, xmm14, xmm4
        vpcmpeqd  xmm9, xmm14, xmm4
        vpcmpgtd  xmm13, xmm10, xmm4
        vpcmpeqd  xmm4, xmm10, xmm4
        vpor      xmm15, xmm13, xmm4
        vpor      xmm8, xmm11, xmm9
        vmulpd    ymm13, ymm6, YMMWORD PTR [__svml_datan2_ha_data_internal+1344]
        vpor      xmm4, xmm8, xmm15
        vmulpd    ymm11, ymm6, YMMWORD PTR [__svml_datan2_ha_data_internal+1152]
        vmulpd    ymm14, ymm6, YMMWORD PTR [__svml_datan2_ha_data_internal+1216]
        vmulpd    ymm15, ymm6, YMMWORD PTR [__svml_datan2_ha_data_internal+1280]
        vmovupd   ymm8, YMMWORD PTR [__svml_datan2_ha_data_internal+1792]
        vmovmskps r11d, xmm4
        vextracti128 xmm10, ymm13, 1
        vshufps   xmm10, xmm13, xmm10, 221
        vpsubd    xmm10, xmm10, xmm12
        vextracti128 xmm9, ymm11, 1
        vextracti128 xmm13, ymm14, 1
        vshufps   xmm9, xmm11, xmm9, 221
        vshufps   xmm11, xmm14, xmm13, 221
        vpsubd    xmm9, xmm9, xmm12
        vpsubd    xmm11, xmm11, xmm12
        vpsrad    xmm9, xmm9, 31
        vextracti128 xmm14, ymm15, 1
        vshufps   xmm13, xmm15, xmm14, 221
        vpsrad    xmm15, xmm10, 31
        vpsubd    xmm12, xmm13, xmm12
        vpsrad    xmm14, xmm11, 31
        vpsrad    xmm13, xmm12, 31
        vpaddd    xmm12, xmm9, xmm15
        vpaddd    xmm15, xmm14, xmm13
        vpaddd    xmm14, xmm12, xmm15
        vpaddd    xmm11, xmm14, XMMWORD PTR [__svml_datan2_ha_data_internal+1408]
        vpslld    xmm13, xmm11, 5
        vmovd     ecx, xmm13
        vpextrd   r9d, xmm13, 2
        vpextrd   eax, xmm13, 1
        vmovq     xmm9, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+rdx+rcx]
        vpextrd   r10d, xmm13, 3
        vmovq     xmm12, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+rdx+r9]
        vmovhpd   xmm10, xmm9, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+rdx+rax]
        vmovhpd   xmm15, xmm12, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+rdx+r10]
        vmovq     xmm11, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+8+rdx+rcx]
        vmovq     xmm13, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+8+rdx+r9]
        vmovhpd   xmm9, xmm11, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+8+rdx+rax]
        vmovhpd   xmm12, xmm13, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+8+rdx+r10]
        vinsertf128 ymm14, ymm10, xmm15, 1
        vmovq     xmm15, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+16+rdx+rcx]
        vmovq     xmm10, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+16+rdx+r9]
        vmovhpd   xmm11, xmm15, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+16+rdx+rax]
        vmovq     xmm15, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+24+rdx+rcx]
        vinsertf128 ymm13, ymm9, xmm12, 1
        vmovhpd   xmm12, xmm10, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+16+rdx+r10]
        vmovq     xmm10, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+24+rdx+r9]
        vmovhpd   xmm9, xmm15, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+24+rdx+rax]
        vmovhpd   xmm15, xmm10, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+24+rdx+r10]
        vinsertf128 ymm12, ymm11, xmm12, 1
        vinsertf128 ymm11, ymm9, xmm15, 1
        vandpd    ymm15, ymm11, ymm5
        vandpd    ymm10, ymm11, ymm6
        vfnmadd231pd ymm15, ymm6, ymm14
        vfmadd213pd ymm14, ymm5, ymm10
        vmovups   xmm9, XMMWORD PTR [__svml_datan2_ha_data_internal+1536]
        vextracti128 xmm11, ymm14, 1
        vshufps   xmm10, xmm14, xmm11, 221
        vpslld    xmm11, xmm10, 3
        vandps    xmm11, xmm11, XMMWORD PTR [__svml_datan2_ha_data_internal+1600]
        vorps     xmm11, xmm11, XMMWORD PTR [__svml_datan2_ha_data_internal+1664]
        vrcpps    xmm11, xmm11
        vandps    xmm10, xmm10, XMMWORD PTR [__svml_datan2_ha_data_internal+1472]
        vpsrld    xmm11, xmm11, 3
        vpsubd    xmm11, xmm11, XMMWORD PTR [__svml_datan2_ha_data_internal+1728]
        vpsubd    xmm9, xmm9, xmm10
        vpaddd    xmm10, xmm11, xmm9
        vpshufd   xmm9, xmm10, 80
        vpshufd   xmm11, xmm10, 250
        vmovapd   ymm10, ymm8
        vinserti128 ymm9, ymm9, xmm11, 1
        vandpd    ymm9, ymm9, YMMWORD PTR [__svml_datan2_ha_data_internal+3008]
        vfnmadd231pd ymm10, ymm9, ymm14
        vmovupd   ymm11, YMMWORD PTR [__svml_datan2_ha_data_internal+1856]
        vfmadd213pd ymm10, ymm10, ymm10
        vfmadd213pd ymm9, ymm10, ymm9
        vfnmadd231pd ymm8, ymm9, ymm14
        vfmadd213pd ymm9, ymm8, ymm9
        vmulpd    ymm8, ymm15, ymm9
        vfnmadd213pd ymm14, ymm8, ymm15
        vmulpd    ymm10, ymm8, ymm8
        vmulpd    ymm15, ymm9, ymm14
        vmovupd   ymm9, YMMWORD PTR [__svml_datan2_ha_data_internal+1920]
        vmulpd    ymm14, ymm10, ymm10
        vaddpd    ymm12, ymm12, ymm15
        vfmadd213pd ymm11, ymm14, YMMWORD PTR [__svml_datan2_ha_data_internal+1984]
        vfmadd213pd ymm9, ymm14, YMMWORD PTR [__svml_datan2_ha_data_internal+2048]
        vaddpd    ymm0, ymm0, ymm12
        vfmadd213pd ymm11, ymm14, YMMWORD PTR [__svml_datan2_ha_data_internal+2112]
        vfmadd213pd ymm9, ymm14, YMMWORD PTR [__svml_datan2_ha_data_internal+2176]
        vfmadd213pd ymm11, ymm14, YMMWORD PTR [__svml_datan2_ha_data_internal+2240]
        vfmadd213pd ymm9, ymm14, YMMWORD PTR [__svml_datan2_ha_data_internal+2304]
        vfmadd213pd ymm11, ymm14, YMMWORD PTR [__svml_datan2_ha_data_internal+2368]
        vfmadd213pd ymm9, ymm14, YMMWORD PTR [__svml_datan2_ha_data_internal+2432]
        vfmadd213pd ymm11, ymm14, YMMWORD PTR [__svml_datan2_ha_data_internal+2496]
        vfmadd213pd ymm9, ymm14, YMMWORD PTR [__svml_datan2_ha_data_internal+2560]
        vfmadd213pd ymm11, ymm10, ymm9
        vmulpd    ymm9, ymm10, ymm11
        vfmadd213pd ymm9, ymm8, ymm0
        vaddpd    ymm8, ymm8, ymm9
        vaddpd    ymm10, ymm13, ymm8
        vxorpd    ymm1, ymm10, ymm1
        vaddpd    ymm2, ymm2, ymm1
        vorpd     ymm0, ymm2, ymm3
        test      r11d, r11d
        jne       _B3_12

_B3_2::

        test      r8d, r8d
        jne       _B3_4

_B3_3::

        vmovups   xmm6, XMMWORD PTR [400+rsp]
        vmovups   xmm7, XMMWORD PTR [416+rsp]
        vmovups   xmm8, XMMWORD PTR [384+rsp]
        vmovups   xmm9, XMMWORD PTR [368+rsp]
        vmovups   xmm10, XMMWORD PTR [352+rsp]
        vmovups   xmm11, XMMWORD PTR [336+rsp]
        vmovups   xmm12, XMMWORD PTR [320+rsp]
        vmovups   xmm13, XMMWORD PTR [304+rsp]
        vmovups   xmm14, XMMWORD PTR [288+rsp]
        vmovups   xmm15, XMMWORD PTR [272+rsp]
        mov       r13, QWORD PTR [432+rsp]
        add       rsp, 456
        ret

_B3_4::

        vmovupd   ymm1, YMMWORD PTR [32+r13]
        vmovupd   YMMWORD PTR [64+r13], ymm7
        vmovupd   YMMWORD PTR [128+r13], ymm0
        vmovupd   YMMWORD PTR [r13], ymm1
        je        _B3_3

_B3_7::

        xor       eax, eax
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, eax
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, r8d

_B3_8::

        bt        esi, ebx
        jc        _B3_11

_B3_9::

        inc       ebx
        cmp       ebx, 4
        jl        _B3_8

_B3_10::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        vmovupd   ymm0, YMMWORD PTR [128+r13]
        jmp       _B3_3

_B3_11::

        vzeroupper
        lea       rcx, QWORD PTR [r13+rbx*8]
        lea       rdx, QWORD PTR [64+r13+rbx*8]
        lea       r8, QWORD PTR [128+r13+rbx*8]

        call      __svml_datan2_ha_cout_rare_internal
        jmp       _B3_9

_B3_12::

        vmovupd   ymm13, YMMWORD PTR [32+r13]
        vmovupd   ymm11, YMMWORD PTR [__svml_datan2_ha_data_internal+832]
        vcmpordpd ymm14, ymm7, ymm7
        vcmpordpd ymm15, ymm13, ymm13
        vcmpnlt_uqpd ymm12, ymm5, ymm6
        vandpd    ymm2, ymm14, ymm15
        vblendvpd ymm3, ymm6, ymm5, ymm12
        vandpd    ymm9, ymm7, ymm11
        vandpd    ymm10, ymm13, ymm11
        vandpd    ymm8, ymm12, YMMWORD PTR [__svml_datan2_ha_data_internal+3136]
        vextracti128 xmm1, ymm2, 1
        vshufps   xmm1, xmm2, xmm1, 221
        vmovupd   ymm2, YMMWORD PTR [r13]
        vcmpeqpd  ymm6, ymm6, ymm2
        vcmpeqpd  ymm5, ymm5, ymm2
        vorpd     ymm6, ymm6, ymm5
        vextracti128 xmm11, ymm6, 1
        vshufps   xmm12, xmm6, xmm11, 221
        vpand     xmm5, xmm12, xmm1
        vcmpeqpd  ymm1, ymm3, ymm2
        vpandn    xmm4, xmm5, xmm4
        vmovmskps r8d, xmm4
        vblendvpd ymm3, ymm8, ymm2, ymm1
        vorpd     ymm4, ymm3, ymm9
        vextracti128 xmm8, ymm2, 1
        vextracti128 xmm9, ymm7, 1
        vshufps   xmm13, xmm2, xmm8, 221
        vshufps   xmm14, xmm7, xmm9, 221
        vpcmpgtd  xmm1, xmm13, xmm14
        vpshufd   xmm15, xmm1, 80
        vpshufd   xmm2, xmm1, 250
        vinserti128 ymm3, ymm15, xmm2, 1
        vandpd    ymm6, ymm3, YMMWORD PTR [__svml_datan2_ha_data_internal+3072]
        vaddpd    ymm4, ymm4, ymm6
        vorpd     ymm1, ymm4, ymm10
        vpshufd   xmm10, xmm5, 80
        vpshufd   xmm5, xmm5, 250
        vinserti128 ymm2, ymm10, xmm5, 1
        vblendvpd ymm0, ymm0, ymm1, ymm2
        jmp       _B3_2
        ALIGN     16

_B3_13::

__svml_atan24_ha_l9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_atan24_ha_l9_B1_B4:
	DD	1601537
	DD	3593328
	DD	1665128
	DD	1734751
	DD	1607766
	DD	1546317
	DD	1484868
	DD	1423419
	DD	1361970
	DD	1300521
	DD	1239072
	DD	1177623
	DD	3735819

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B3_1
	DD	imagerel _B3_7
	DD	imagerel _unwind___svml_atan24_ha_l9_B1_B4

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_atan24_ha_l9_B7_B11:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B3_1
	DD	imagerel _B3_7
	DD	imagerel _unwind___svml_atan24_ha_l9_B1_B4

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B3_7
	DD	imagerel _B3_12
	DD	imagerel _unwind___svml_atan24_ha_l9_B7_B11

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_atan24_ha_l9_B12_B12:
	DD	33
	DD	imagerel _B3_1
	DD	imagerel _B3_7
	DD	imagerel _unwind___svml_atan24_ha_l9_B1_B4

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B3_12
	DD	imagerel _B3_13
	DD	imagerel _unwind___svml_atan24_ha_l9_B12_B12

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST3:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_atan24_ha_e9

__svml_atan24_ha_e9	PROC

_B4_1::

        DB        243
        DB        15
        DB        30
        DB        250
L111::

        sub       rsp, 520
        xor       r8d, r8d
        vmovups   XMMWORD PTR [336+rsp], xmm15
        vmovups   XMMWORD PTR [352+rsp], xmm14
        vmovups   XMMWORD PTR [368+rsp], xmm13
        vmovups   XMMWORD PTR [384+rsp], xmm12
        vmovups   XMMWORD PTR [400+rsp], xmm11
        vmovups   XMMWORD PTR [416+rsp], xmm10
        vmovups   XMMWORD PTR [432+rsp], xmm9
        vmovups   XMMWORD PTR [448+rsp], xmm8
        vmovups   XMMWORD PTR [480+rsp], xmm7
        vmovups   XMMWORD PTR [464+rsp], xmm6
        mov       QWORD PTR [496+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovupd   ymm15, YMMWORD PTR [rcx]
        and       r13, -64
        vmovupd   ymm9, YMMWORD PTR [rdx]
        vmovupd   ymm13, YMMWORD PTR [__svml_datan2_ha_data_internal+896]
        vmovupd   ymm8, YMMWORD PTR [__svml_datan2_ha_data_internal+1088]
        vmovupd   ymm4, YMMWORD PTR [__svml_datan2_ha_data_internal+2880]
        vmovupd   YMMWORD PTR [32+r13], ymm15
        vmovupd   YMMWORD PTR [64+r13], ymm9
        vcmplt_oqpd ymm1, ymm9, ymm4
        vmovupd   YMMWORD PTR [r13], ymm4
        vmovups   xmm4, XMMWORD PTR [__svml_datan2_ha_data_internal+2688]
        vandpd    ymm6, ymm9, ymm13
        lea       rdx, QWORD PTR [__ImageBase]
        vandpd    ymm5, ymm15, ymm13
        vxorpd    ymm10, ymm15, ymm5
        vandpd    ymm12, ymm5, ymm8
        vandpd    ymm7, ymm6, ymm8
        vmovupd   YMMWORD PTR [96+r13], ymm6
        vmovupd   YMMWORD PTR [128+r13], ymm5
        vmovupd   YMMWORD PTR [160+r13], ymm10
        vsubpd    ymm11, ymm5, ymm12
        vsubpd    ymm10, ymm6, ymm7
        vxorpd    ymm13, ymm9, ymm6
        vandpd    ymm2, ymm1, YMMWORD PTR [__svml_datan2_ha_data_internal+960]
        vmovupd   YMMWORD PTR [192+r13], ymm2
        vandpd    ymm9, ymm1, YMMWORD PTR [__svml_datan2_ha_data_internal+1024]
        vxorpd    ymm9, ymm9, ymm13
        mov       QWORD PTR [504+rsp], r13
        vextractf128 xmm3, ymm6, 1
        vextractf128 xmm0, ymm5, 1
        vshufps   xmm2, xmm6, xmm3, 221
        vshufps   xmm5, xmm5, xmm0, 221
        vpsubd    xmm15, xmm2, xmm4
        vpsubd    xmm14, xmm5, xmm4
        vpsubd    xmm1, xmm5, xmm2
        vmovups   xmm4, XMMWORD PTR [__svml_datan2_ha_data_internal+2752]
        vpcmpgtd  xmm3, xmm15, xmm4
        vpcmpeqd  xmm15, xmm15, xmm4
        vpaddd    xmm0, xmm1, XMMWORD PTR [__svml_datan2_ha_data_internal+3200]
        vpor      xmm2, xmm3, xmm15
        vpcmpgtd  xmm15, xmm14, xmm4
        vpcmpeqd  xmm14, xmm14, xmm4
        vmovups   xmm4, XMMWORD PTR [__svml_datan2_ha_data_internal+3264]
        vpor      xmm15, xmm15, xmm14
        vpcmpgtd  xmm1, xmm4, xmm0
        vpcmpeqd  xmm0, xmm4, xmm0
        vmulpd    ymm14, ymm6, YMMWORD PTR [__svml_datan2_ha_data_internal+1152]
        vpor      xmm3, xmm2, xmm15
        vmulpd    ymm2, ymm6, YMMWORD PTR [__svml_datan2_ha_data_internal+1216]
        vpor      xmm15, xmm1, xmm0
        vmulpd    ymm0, ymm6, YMMWORD PTR [__svml_datan2_ha_data_internal+1344]
        vpor      xmm4, xmm3, xmm15
        vmulpd    ymm6, ymm6, YMMWORD PTR [__svml_datan2_ha_data_internal+1280]
        vmovdqu   XMMWORD PTR [32+rsp], xmm4
        vextractf128 xmm15, ymm14, 1
        vextractf128 xmm1, ymm0, 1
        vextractf128 xmm3, ymm2, 1
        vextractf128 xmm4, ymm6, 1
        vshufps   xmm14, xmm14, xmm15, 221
        vshufps   xmm0, xmm0, xmm1, 221
        vpsubd    xmm15, xmm14, xmm5
        vshufps   xmm2, xmm2, xmm3, 221
        vpsubd    xmm1, xmm0, xmm5
        vshufps   xmm6, xmm6, xmm4, 221
        vpsubd    xmm2, xmm2, xmm5
        vpsubd    xmm6, xmm6, xmm5
        vpsrad    xmm15, xmm15, 31
        vpsrad    xmm4, xmm1, 31
        vpsrad    xmm2, xmm2, 31
        vpsrad    xmm5, xmm6, 31
        vpaddd    xmm15, xmm15, xmm4
        vpaddd    xmm6, xmm2, xmm5
        vpaddd    xmm4, xmm15, xmm6
        vpaddd    xmm2, xmm4, XMMWORD PTR [__svml_datan2_ha_data_internal+1408]
        vpslld    xmm3, xmm2, 5
        vmovd     ecx, xmm3
        vpextrd   r9d, xmm3, 2
        vpextrd   eax, xmm3, 1
        vmovq     xmm5, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+rdx+rcx]
        vpextrd   r10d, xmm3, 3
        vmovq     xmm0, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+rdx+r9]
        vmovq     xmm4, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+8+rdx+r9]
        vmovhpd   xmm1, xmm5, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+rdx+rax]
        vmovq     xmm15, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+8+rdx+rcx]
        vmovq     xmm5, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+16+rdx+rcx]
        vmovhpd   xmm14, xmm0, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+rdx+r10]
        vmovq     xmm0, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+16+rdx+r9]
        vmovhpd   xmm6, xmm15, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+8+rdx+rax]
        vmovhpd   xmm2, xmm4, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+8+rdx+r10]
        vmovq     xmm4, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+24+rdx+rcx]
        vinsertf128 ymm3, ymm1, xmm14, 1
        vmovhpd   xmm1, xmm5, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+16+rdx+rax]
        vmovhpd   xmm14, xmm0, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+16+rdx+r10]
        vmovq     xmm5, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+24+rdx+r9]
        vinsertf128 ymm15, ymm6, xmm2, 1
        vmovhpd   xmm2, xmm4, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+24+rdx+rax]
        vinsertf128 ymm6, ymm1, xmm14, 1
        vmovhpd   xmm1, xmm5, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+24+rdx+r10]
        vmulpd    ymm14, ymm7, ymm3
        vinsertf128 ymm1, ymm2, xmm1, 1
        vmulpd    ymm2, ymm3, ymm10
        vandpd    ymm0, ymm1, ymm12
        vandpd    ymm4, ymm1, ymm11
        vmulpd    ymm12, ymm12, ymm3
        vmulpd    ymm11, ymm3, ymm11
        vmovups   xmm3, XMMWORD PTR [__svml_datan2_ha_data_internal+1536]
        vsubpd    ymm5, ymm0, ymm14
        vsubpd    ymm0, ymm4, ymm2
        vandpd    ymm7, ymm1, ymm7
        vandpd    ymm10, ymm1, ymm10
        vsubpd    ymm2, ymm5, ymm0
        vaddpd    ymm12, ymm7, ymm12
        vaddpd    ymm10, ymm10, ymm11
        vsubpd    ymm14, ymm2, ymm5
        vaddpd    ymm11, ymm12, ymm10
        vsubpd    ymm5, ymm0, ymm14
        vsubpd    ymm7, ymm12, ymm11
        vandpd    ymm4, ymm2, ymm8
        vsubpd    ymm2, ymm2, ymm4
        vandpd    ymm12, ymm11, ymm8
        vsubpd    ymm8, ymm11, ymm12
        vaddpd    ymm5, ymm5, ymm2
        vaddpd    ymm2, ymm10, ymm7
        vaddpd    ymm10, ymm2, ymm8
        vextractf128 xmm7, ymm12, 1
        vshufps   xmm14, xmm12, xmm7, 221
        vpslld    xmm11, xmm14, 3
        vandps    xmm8, xmm11, XMMWORD PTR [__svml_datan2_ha_data_internal+1600]
        vorps     xmm2, xmm8, XMMWORD PTR [__svml_datan2_ha_data_internal+1664]
        vrcpps    xmm1, xmm2
        vandps    xmm7, xmm14, XMMWORD PTR [__svml_datan2_ha_data_internal+1472]
        vpsrld    xmm0, xmm1, 3
        vpsubd    xmm11, xmm0, XMMWORD PTR [__svml_datan2_ha_data_internal+1728]
        vpsubd    xmm3, xmm3, xmm7
        vpaddd    xmm11, xmm11, xmm3
        vpshufd   xmm7, xmm11, 80
        vpshufd   xmm8, xmm11, 250
        vinsertf128 ymm2, ymm7, xmm8, 1
        vandpd    ymm2, ymm2, YMMWORD PTR [__svml_datan2_ha_data_internal+3008]
        vmulpd    ymm12, ymm12, ymm2
        vmulpd    ymm10, ymm2, ymm10
        vsubpd    ymm1, ymm12, YMMWORD PTR [__svml_datan2_ha_data_internal+1792]
        vaddpd    ymm1, ymm1, ymm10
        vmulpd    ymm10, ymm1, ymm1
        vsubpd    ymm12, ymm10, ymm1
        vmulpd    ymm7, ymm1, ymm12
        vaddpd    ymm11, ymm1, ymm7
        vmulpd    ymm8, ymm1, ymm11
        vsubpd    ymm0, ymm8, ymm1
        vmulpd    ymm3, ymm1, ymm0
        vaddpd    ymm14, ymm1, ymm3
        vmulpd    ymm10, ymm1, ymm14
        vsubpd    ymm1, ymm10, ymm1
        vmulpd    ymm10, ymm2, ymm1
        vmulpd    ymm12, ymm4, ymm10
        vmulpd    ymm7, ymm5, ymm10
        vmulpd    ymm5, ymm2, ymm5
        vmulpd    ymm10, ymm4, ymm2
        vaddpd    ymm11, ymm12, ymm7
        vaddpd    ymm7, ymm11, ymm5
        vaddpd    ymm12, ymm7, ymm10
        vaddpd    ymm6, ymm6, ymm7
        vmulpd    ymm11, ymm12, ymm12
        vmulpd    ymm8, ymm11, ymm11
        vmulpd    ymm4, ymm8, YMMWORD PTR [__svml_datan2_ha_data_internal+1856]
        vmulpd    ymm0, ymm8, YMMWORD PTR [__svml_datan2_ha_data_internal+1920]
        vaddpd    ymm1, ymm4, YMMWORD PTR [__svml_datan2_ha_data_internal+1984]
        vaddpd    ymm3, ymm0, YMMWORD PTR [__svml_datan2_ha_data_internal+2048]
        vmulpd    ymm2, ymm8, ymm1
        vmulpd    ymm4, ymm8, ymm3
        vaddpd    ymm5, ymm2, YMMWORD PTR [__svml_datan2_ha_data_internal+2112]
        vaddpd    ymm0, ymm4, YMMWORD PTR [__svml_datan2_ha_data_internal+2176]
        vmulpd    ymm14, ymm8, ymm5
        vmulpd    ymm1, ymm8, ymm0
        vaddpd    ymm2, ymm14, YMMWORD PTR [__svml_datan2_ha_data_internal+2240]
        vaddpd    ymm4, ymm1, YMMWORD PTR [__svml_datan2_ha_data_internal+2304]
        vmulpd    ymm3, ymm8, ymm2
        vmulpd    ymm5, ymm8, ymm4
        vaddpd    ymm14, ymm3, YMMWORD PTR [__svml_datan2_ha_data_internal+2368]
        vaddpd    ymm0, ymm5, YMMWORD PTR [__svml_datan2_ha_data_internal+2432]
        vmulpd    ymm14, ymm8, ymm14
        vmulpd    ymm8, ymm8, ymm0
        vaddpd    ymm1, ymm14, YMMWORD PTR [__svml_datan2_ha_data_internal+2496]
        vaddpd    ymm0, ymm8, YMMWORD PTR [__svml_datan2_ha_data_internal+2560]
        vmulpd    ymm2, ymm11, ymm1
        vaddpd    ymm3, ymm0, ymm2
        vaddpd    ymm0, ymm9, ymm6
        vmovdqu   xmm9, XMMWORD PTR [32+rsp]
        vmulpd    ymm11, ymm11, ymm3
        vmovmskps r11d, xmm9
        vmulpd    ymm1, ymm12, ymm11
        vaddpd    ymm2, ymm0, ymm1
        vmovupd   ymm1, YMMWORD PTR [128+r13]
        vaddpd    ymm3, ymm10, ymm2
        vmovupd   ymm10, YMMWORD PTR [64+r13]
        vmovupd   ymm2, YMMWORD PTR [96+r13]
        vaddpd    ymm15, ymm15, ymm3
        vxorpd    ymm13, ymm15, ymm13
        vaddpd    ymm0, ymm13, YMMWORD PTR [192+r13]
        vorpd     ymm0, ymm0, YMMWORD PTR [160+r13]
        test      r11d, r11d
        jne       _B4_12

_B4_2::

        test      r8d, r8d
        jne       _B4_4

_B4_3::

        vmovups   xmm6, XMMWORD PTR [464+rsp]
        vmovups   xmm7, XMMWORD PTR [480+rsp]
        vmovups   xmm8, XMMWORD PTR [448+rsp]
        vmovups   xmm9, XMMWORD PTR [432+rsp]
        vmovups   xmm10, XMMWORD PTR [416+rsp]
        vmovups   xmm11, XMMWORD PTR [400+rsp]
        vmovups   xmm12, XMMWORD PTR [384+rsp]
        vmovups   xmm13, XMMWORD PTR [368+rsp]
        vmovups   xmm14, XMMWORD PTR [352+rsp]
        vmovups   xmm15, XMMWORD PTR [336+rsp]
        mov       r13, QWORD PTR [496+rsp]
        add       rsp, 520
        ret

_B4_4::

        vmovupd   ymm1, YMMWORD PTR [32+r13]
        vmovupd   YMMWORD PTR [64+r13], ymm10
        vmovupd   YMMWORD PTR [128+r13], ymm0
        vmovupd   YMMWORD PTR [r13], ymm1
        je        _B4_3

_B4_7::

        xor       eax, eax
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, eax
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, r8d

_B4_8::

        bt        esi, ebx
        jc        _B4_11

_B4_9::

        inc       ebx
        cmp       ebx, 4
        jl        _B4_8

_B4_10::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        vmovupd   ymm0, YMMWORD PTR [128+r13]
        jmp       _B4_3

_B4_11::

        vzeroupper
        lea       rcx, QWORD PTR [r13+rbx*8]
        lea       rdx, QWORD PTR [64+r13+rbx*8]
        lea       r8, QWORD PTR [128+r13+rbx*8]

        call      __svml_datan2_ha_cout_rare_internal
        jmp       _B4_9

_B4_12::

        vmovupd   ymm13, YMMWORD PTR [32+r13]
        vmovupd   ymm11, YMMWORD PTR [__svml_datan2_ha_data_internal+832]
        vcmpordpd ymm14, ymm10, ymm10
        vcmpordpd ymm15, ymm13, ymm13
        vcmpnlt_uqpd ymm12, ymm1, ymm2
        vandpd    ymm4, ymm14, ymm15
        vblendvpd ymm5, ymm2, ymm1, ymm12
        vandpd    ymm7, ymm10, ymm11
        vandpd    ymm8, ymm13, ymm11
        vandpd    ymm6, ymm12, YMMWORD PTR [__svml_datan2_ha_data_internal+3136]
        vextractf128 xmm3, ymm4, 1
        vshufps   xmm3, xmm4, xmm3, 221
        vmovupd   ymm4, YMMWORD PTR [r13]
        vcmpeqpd  ymm2, ymm2, ymm4
        vcmpeqpd  ymm1, ymm1, ymm4
        vorpd     ymm11, ymm2, ymm1
        vcmpeqpd  ymm1, ymm5, ymm4
        vextractf128 xmm12, ymm11, 1
        vshufps   xmm13, xmm11, xmm12, 221
        vpand     xmm2, xmm13, xmm3
        vblendvpd ymm3, ymm6, ymm4, ymm1
        vpandn    xmm9, xmm2, xmm9
        vmovmskps r8d, xmm9
        vorpd     ymm1, ymm3, ymm7
        vextractf128 xmm5, ymm4, 1
        vextractf128 xmm6, ymm10, 1
        vshufps   xmm7, xmm4, xmm5, 221
        vshufps   xmm9, xmm10, xmm6, 221
        vpcmpgtd  xmm12, xmm7, xmm9
        vpshufd   xmm11, xmm12, 80
        vpshufd   xmm13, xmm12, 250
        vinsertf128 ymm14, ymm11, xmm13, 1
        vandpd    ymm15, ymm14, YMMWORD PTR [__svml_datan2_ha_data_internal+3072]
        vaddpd    ymm1, ymm1, ymm15
        vorpd     ymm1, ymm1, ymm8
        vpshufd   xmm8, xmm2, 80
        vpshufd   xmm2, xmm2, 250
        vinsertf128 ymm2, ymm8, xmm2, 1
        vblendvpd ymm0, ymm0, ymm1, ymm2
        jmp       _B4_2
        ALIGN     16

_B4_13::

__svml_atan24_ha_e9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_atan24_ha_e9_B1_B4:
	DD	1601537
	DD	4117616
	DD	1927272
	DD	1996895
	DD	1869910
	DD	1808461
	DD	1747012
	DD	1685563
	DD	1624114
	DD	1562665
	DD	1501216
	DD	1439767
	DD	4260107

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B4_1
	DD	imagerel _B4_7
	DD	imagerel _unwind___svml_atan24_ha_e9_B1_B4

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_atan24_ha_e9_B7_B11:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B4_1
	DD	imagerel _B4_7
	DD	imagerel _unwind___svml_atan24_ha_e9_B1_B4

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B4_7
	DD	imagerel _B4_12
	DD	imagerel _unwind___svml_atan24_ha_e9_B7_B11

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_atan24_ha_e9_B12_B12:
	DD	33
	DD	imagerel _B4_1
	DD	imagerel _B4_7
	DD	imagerel _unwind___svml_atan24_ha_e9_B1_B4

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B4_12
	DD	imagerel _B4_13
	DD	imagerel _unwind___svml_atan24_ha_e9_B12_B12

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST4:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_atan22_ha_l9

__svml_atan22_ha_l9	PROC

_B5_1::

        DB        243
        DB        15
        DB        30
        DB        250
L155::

        sub       rsp, 456
        lea       r10, QWORD PTR [__ImageBase]
        vmovups   XMMWORD PTR [288+rsp], xmm15
        xor       eax, eax
        vmovups   XMMWORD PTR [304+rsp], xmm14
        vmovups   XMMWORD PTR [320+rsp], xmm13
        vmovups   XMMWORD PTR [336+rsp], xmm12
        vmovups   XMMWORD PTR [352+rsp], xmm11
        vmovups   XMMWORD PTR [368+rsp], xmm10
        vmovups   XMMWORD PTR [400+rsp], xmm9
        vmovups   XMMWORD PTR [384+rsp], xmm8
        vmovups   XMMWORD PTR [272+rsp], xmm7
        vmovups   XMMWORD PTR [256+rsp], xmm6
        mov       QWORD PTR [432+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovupd   xmm4, XMMWORD PTR [rcx]
        and       r13, -64
        vmovupd   xmm12, XMMWORD PTR [rdx]
        vmovupd   xmm9, XMMWORD PTR [__svml_datan2_ha_data_internal+896]
        vandpd    xmm10, xmm12, xmm9
        vandpd    xmm9, xmm4, xmm9
        vpshufd   xmm0, xmm9, 221
        vxorpd    xmm8, xmm4, xmm9
        vmovq     xmm2, QWORD PTR [__svml_datan2_ha_data_internal+2688]
        vxorpd    xmm6, xmm12, xmm10
        vpshufd   xmm1, xmm10, 221
        vpsubd    xmm13, xmm0, xmm2
        vmovq     xmm11, QWORD PTR [__svml_datan2_ha_data_internal+2752]
        vmovupd   XMMWORD PTR [32+rsp], xmm4
        vpsubd    xmm4, xmm1, xmm2
        vmovupd   xmm3, XMMWORD PTR [__svml_datan2_ha_data_internal+2880]
        vpcmpgtd  xmm14, xmm4, xmm11
        vmovupd   XMMWORD PTR [416+rsp], xmm3
        vpcmpgtd  xmm1, xmm13, xmm11
        vcmpltpd  xmm5, xmm12, xmm3
        vpcmpeqd  xmm3, xmm4, xmm11
        vpcmpeqd  xmm11, xmm13, xmm11
        vpor      xmm2, xmm14, xmm3
        vpor      xmm15, xmm1, xmm11
        vmulpd    xmm3, xmm10, XMMWORD PTR [__svml_datan2_ha_data_internal+1152]
        vmulpd    xmm4, xmm10, XMMWORD PTR [__svml_datan2_ha_data_internal+1216]
        vmulpd    xmm14, xmm10, XMMWORD PTR [__svml_datan2_ha_data_internal+1280]
        vpor      xmm11, xmm2, xmm15
        vmulpd    xmm2, xmm10, XMMWORD PTR [__svml_datan2_ha_data_internal+1344]
        vmovmskps r11d, xmm11
        vpshufd   xmm1, xmm3, 221
        vpshufd   xmm2, xmm2, 221
        vpsubd    xmm15, xmm1, xmm0
        vpshufd   xmm4, xmm4, 221
        vpsubd    xmm3, xmm2, xmm0
        vpshufd   xmm14, xmm14, 221
        vpsubd    xmm4, xmm4, xmm0
        vpsubd    xmm0, xmm14, xmm0
        vpsrad    xmm1, xmm15, 31
        vpsrad    xmm3, xmm3, 31
        vpsrad    xmm4, xmm4, 31
        vpsrad    xmm14, xmm0, 31
        vpaddd    xmm3, xmm1, xmm3
        vpaddd    xmm4, xmm4, xmm14
        vmovq     xmm13, QWORD PTR [__svml_datan2_ha_data_internal+1408]
        vpaddd    xmm1, xmm3, xmm4
        vpaddd    xmm13, xmm1, xmm13
        vpslld    xmm2, xmm13, 5
        vmovd     r8d, xmm2
        vandpd    xmm7, xmm5, XMMWORD PTR [__svml_datan2_ha_data_internal+960]
        vandpd    xmm5, xmm5, XMMWORD PTR [__svml_datan2_ha_data_internal+1024]
        vpextrd   r9d, xmm2, 1
        vxorpd    xmm5, xmm5, xmm6
        vmovq     xmm13, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+24+r10+r8]
        vmovq     xmm15, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+r10+r8]
        vmovhpd   xmm2, xmm13, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+24+r10+r9]
        vmovhpd   xmm15, xmm15, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+r10+r9]
        vandpd    xmm1, xmm2, xmm9
        vandpd    xmm2, xmm2, xmm10
        vfmadd231pd xmm2, xmm9, xmm15
        vmovq     xmm0, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+8+r10+r8]
        vmovq     xmm14, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+16+r10+r8]
        vmovhpd   xmm4, xmm0, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+8+r10+r9]
        vmovq     xmm0, QWORD PTR [__svml_datan2_ha_data_internal+1600]
        vpshufd   xmm13, xmm2, 221
        vfnmadd231pd xmm1, xmm10, xmm15
        vpslld    xmm15, xmm13, 3
        vmovhpd   xmm3, xmm14, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+16+r10+r9]
        vpand     xmm15, xmm15, xmm0
        vmovq     xmm14, QWORD PTR [__svml_datan2_ha_data_internal+1664]
        vpor      xmm14, xmm15, xmm14
        vmovq     xmm15, QWORD PTR [__svml_datan2_ha_data_internal+1728]
        vrcpps    xmm0, xmm14
        vpsrld    xmm14, xmm0, 3
        vmovq     xmm0, QWORD PTR [__svml_datan2_ha_data_internal+1472]
        vpsubd    xmm15, xmm14, xmm15
        vpand     xmm14, xmm13, xmm0
        vmovq     xmm13, QWORD PTR [__svml_datan2_ha_data_internal+1536]
        vpsubd    xmm13, xmm13, xmm14
        vpaddd    xmm0, xmm15, xmm13
        vpshufd   xmm15, xmm0, 80
        vandpd    xmm0, xmm15, XMMWORD PTR [__svml_datan2_ha_data_internal+3008]
        vmovupd   xmm13, XMMWORD PTR [__svml_datan2_ha_data_internal+1792]
        vmovapd   xmm14, xmm0
        vfnmadd213pd xmm14, xmm2, xmm13
        vmovupd   xmm15, XMMWORD PTR [__svml_datan2_ha_data_internal+1856]
        vfmadd213pd xmm14, xmm14, xmm14
        vfmadd213pd xmm0, xmm14, xmm0
        vfnmadd231pd xmm13, xmm0, xmm2
        vfmadd213pd xmm0, xmm13, xmm0
        vmulpd    xmm14, xmm1, xmm0
        vfnmadd213pd xmm2, xmm14, xmm1
        vmulpd    xmm13, xmm0, xmm2
        vmulpd    xmm2, xmm14, xmm14
        vaddpd    xmm3, xmm3, xmm13
        vmulpd    xmm1, xmm2, xmm2
        vaddpd    xmm5, xmm5, xmm3
        vfmadd213pd xmm15, xmm1, XMMWORD PTR [__svml_datan2_ha_data_internal+1984]
        vmovupd   xmm0, XMMWORD PTR [__svml_datan2_ha_data_internal+1920]
        vfmadd213pd xmm0, xmm1, XMMWORD PTR [__svml_datan2_ha_data_internal+2048]
        vfmadd213pd xmm15, xmm1, XMMWORD PTR [__svml_datan2_ha_data_internal+2112]
        vfmadd213pd xmm0, xmm1, XMMWORD PTR [__svml_datan2_ha_data_internal+2176]
        vfmadd213pd xmm15, xmm1, XMMWORD PTR [__svml_datan2_ha_data_internal+2240]
        vfmadd213pd xmm0, xmm1, XMMWORD PTR [__svml_datan2_ha_data_internal+2304]
        vfmadd213pd xmm15, xmm1, XMMWORD PTR [__svml_datan2_ha_data_internal+2368]
        vfmadd213pd xmm0, xmm1, XMMWORD PTR [__svml_datan2_ha_data_internal+2432]
        vfmadd213pd xmm15, xmm1, XMMWORD PTR [__svml_datan2_ha_data_internal+2496]
        vfmadd213pd xmm0, xmm1, XMMWORD PTR [__svml_datan2_ha_data_internal+2560]
        vfmadd213pd xmm15, xmm2, xmm0
        vmulpd    xmm0, xmm2, xmm15
        vfmadd213pd xmm0, xmm14, xmm5
        vaddpd    xmm1, xmm14, xmm0
        vaddpd    xmm2, xmm4, xmm1
        vxorpd    xmm6, xmm2, xmm6
        vaddpd    xmm7, xmm7, xmm6
        mov       QWORD PTR [440+rsp], r13
        vorpd     xmm0, xmm7, xmm8
        test      r11d, 3
        jne       _B5_12

_B5_2::

        test      eax, eax
        jne       _B5_4

_B5_3::

        vmovups   xmm6, XMMWORD PTR [256+rsp]
        vmovups   xmm7, XMMWORD PTR [272+rsp]
        vmovups   xmm8, XMMWORD PTR [384+rsp]
        vmovups   xmm9, XMMWORD PTR [400+rsp]
        vmovups   xmm10, XMMWORD PTR [368+rsp]
        vmovups   xmm11, XMMWORD PTR [352+rsp]
        vmovups   xmm12, XMMWORD PTR [336+rsp]
        vmovups   xmm13, XMMWORD PTR [320+rsp]
        vmovups   xmm14, XMMWORD PTR [304+rsp]
        vmovups   xmm15, XMMWORD PTR [288+rsp]
        mov       r13, QWORD PTR [432+rsp]
        add       rsp, 456
        ret

_B5_4::

        vmovupd   xmm1, XMMWORD PTR [32+rsp]
        vmovupd   XMMWORD PTR [r13], xmm1
        vmovupd   XMMWORD PTR [64+r13], xmm12
        vmovupd   XMMWORD PTR [128+r13], xmm0
        je        _B5_3

_B5_7::

        xor       edx, edx
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, edx
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, eax

_B5_8::

        bt        esi, ebx
        jc        _B5_11

_B5_9::

        inc       ebx
        cmp       ebx, 2
        jl        _B5_8

_B5_10::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        vmovupd   xmm0, XMMWORD PTR [128+r13]
        jmp       _B5_3

_B5_11::

        lea       rcx, QWORD PTR [r13+rbx*8]
        lea       rdx, QWORD PTR [64+r13+rbx*8]
        lea       r8, QWORD PTR [128+r13+rbx*8]

        call      __svml_datan2_ha_cout_rare_internal
        jmp       _B5_9

_B5_12::

        vcmpnltpd xmm7, xmm9, xmm10
        vcmpordpd xmm13, xmm12, xmm12
        vblendvpd xmm2, xmm10, xmm9, xmm7
        vmovupd   xmm8, XMMWORD PTR [32+rsp]
        vmovupd   xmm1, XMMWORD PTR [416+rsp]
        vcmpordpd xmm14, xmm8, xmm8
        vcmpeqpd  xmm10, xmm10, xmm1
        vcmpeqpd  xmm15, xmm9, xmm1
        vcmpeqpd  xmm2, xmm2, xmm1
        vmovupd   xmm6, XMMWORD PTR [__svml_datan2_ha_data_internal+832]
        vorpd     xmm9, xmm10, xmm15
        vandpd    xmm3, xmm7, XMMWORD PTR [__svml_datan2_ha_data_internal+3136]
        vandpd    xmm5, xmm12, xmm6
        vandpd    xmm4, xmm8, xmm6
        vandpd    xmm6, xmm13, xmm14
        vblendvpd xmm3, xmm3, xmm1, xmm2
        vpshufd   xmm7, xmm9, 221
        vpshufd   xmm8, xmm6, 221
        vpshufd   xmm1, xmm1, 221
        vpand     xmm9, xmm7, xmm8
        vorpd     xmm8, xmm3, xmm5
        vpandn    xmm11, xmm9, xmm11
        vpshufd   xmm5, xmm12, 221
        vpcmpgtd  xmm6, xmm1, xmm5
        vpshufd   xmm7, xmm6, 80
        vandpd    xmm10, xmm7, XMMWORD PTR [__svml_datan2_ha_data_internal+3072]
        vmovmskps eax, xmm11
        vpshufd   xmm9, xmm9, 80
        vaddpd    xmm11, xmm8, xmm10
        vorpd     xmm4, xmm11, xmm4
        and       eax, 3
        vblendvpd xmm0, xmm0, xmm4, xmm9
        jmp       _B5_2
        ALIGN     16

_B5_13::

__svml_atan22_ha_l9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_atan22_ha_l9_B1_B4:
	DD	1603073
	DD	3593334
	DD	1075310
	DD	1144933
	DD	1607772
	DD	1677395
	DD	1550410
	DD	1488961
	DD	1427512
	DD	1366063
	DD	1304614
	DD	1243163
	DD	3735819

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B5_1
	DD	imagerel _B5_7
	DD	imagerel _unwind___svml_atan22_ha_l9_B1_B4

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_atan22_ha_l9_B7_B11:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B5_1
	DD	imagerel _B5_7
	DD	imagerel _unwind___svml_atan22_ha_l9_B1_B4

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B5_7
	DD	imagerel _B5_12
	DD	imagerel _unwind___svml_atan22_ha_l9_B7_B11

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_atan22_ha_l9_B12_B12:
	DD	33
	DD	imagerel _B5_1
	DD	imagerel _B5_7
	DD	imagerel _unwind___svml_atan22_ha_l9_B1_B4

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B5_12
	DD	imagerel _B5_13
	DD	imagerel _unwind___svml_atan22_ha_l9_B12_B12

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST5:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_atan22_ha_e9

__svml_atan22_ha_e9	PROC

_B6_1::

        DB        243
        DB        15
        DB        30
        DB        250
L187::

        sub       rsp, 520
        lea       r10, QWORD PTR [__ImageBase]
        vmovups   XMMWORD PTR [288+rsp], xmm15
        xor       eax, eax
        vmovups   XMMWORD PTR [304+rsp], xmm14
        vmovups   XMMWORD PTR [320+rsp], xmm13
        vmovups   XMMWORD PTR [336+rsp], xmm12
        vmovups   XMMWORD PTR [352+rsp], xmm11
        vmovups   XMMWORD PTR [368+rsp], xmm10
        vmovups   XMMWORD PTR [400+rsp], xmm9
        vmovups   XMMWORD PTR [384+rsp], xmm8
        vmovups   XMMWORD PTR [272+rsp], xmm7
        vmovups   XMMWORD PTR [256+rsp], xmm6
        mov       QWORD PTR [496+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovupd   xmm3, XMMWORD PTR [rdx]
        and       r13, -64
        vmovupd   xmm7, XMMWORD PTR [__svml_datan2_ha_data_internal+2880]
        vcmpltpd  xmm15, xmm3, xmm7
        vmovupd   xmm12, XMMWORD PTR [rcx]
        vmovupd   xmm13, XMMWORD PTR [__svml_datan2_ha_data_internal+896]
        vmovupd   xmm6, XMMWORD PTR [__svml_datan2_ha_data_internal+1088]
        vandpd    xmm4, xmm12, xmm13
        vandpd    xmm2, xmm3, xmm13
        vandpd    xmm10, xmm4, xmm6
        vmovupd   XMMWORD PTR [464+rsp], xmm4
        vxorpd    xmm13, xmm12, xmm4
        vsubpd    xmm9, xmm4, xmm10
        vpshufd   xmm1, xmm2, 221
        vxorpd    xmm11, xmm3, xmm2
        vpshufd   xmm4, xmm4, 221
        vandpd    xmm5, xmm2, xmm6
        vmovupd   XMMWORD PTR [32+rsp], xmm12
        vmovupd   XMMWORD PTR [416+rsp], xmm7
        vandpd    xmm12, xmm15, XMMWORD PTR [__svml_datan2_ha_data_internal+960]
        vandpd    xmm7, xmm15, XMMWORD PTR [__svml_datan2_ha_data_internal+1024]
        vmovupd   XMMWORD PTR [432+rsp], xmm3
        vpsubd    xmm3, xmm4, xmm1
        vmovq     xmm14, QWORD PTR [__svml_datan2_ha_data_internal+2688]
        vxorpd    xmm7, xmm7, xmm11
        vmovq     xmm15, QWORD PTR [__svml_datan2_ha_data_internal+3200]
        vpsubd    xmm0, xmm1, xmm14
        vpaddd    xmm3, xmm3, xmm15
        vpsubd    xmm14, xmm4, xmm14
        vmovq     xmm15, QWORD PTR [__svml_datan2_ha_data_internal+2752]
        vpcmpgtd  xmm1, xmm0, xmm15
        vpcmpeqd  xmm0, xmm0, xmm15
        vpor      xmm0, xmm1, xmm0
        vpcmpgtd  xmm1, xmm14, xmm15
        vpcmpeqd  xmm14, xmm14, xmm15
        vmovq     xmm15, QWORD PTR [__svml_datan2_ha_data_internal+3264]
        vpor      xmm1, xmm1, xmm14
        vpcmpgtd  xmm14, xmm15, xmm3
        vpcmpeqd  xmm3, xmm15, xmm3
        vpor      xmm0, xmm0, xmm1
        vpor      xmm1, xmm14, xmm3
        vmulpd    xmm15, xmm2, XMMWORD PTR [__svml_datan2_ha_data_internal+1152]
        vmulpd    xmm3, xmm2, XMMWORD PTR [__svml_datan2_ha_data_internal+1344]
        vsubpd    xmm8, xmm2, xmm5
        vpor      xmm0, xmm0, xmm1
        vmovdqu   XMMWORD PTR [480+rsp], xmm0
        vmulpd    xmm1, xmm2, XMMWORD PTR [__svml_datan2_ha_data_internal+1216]
        vmulpd    xmm0, xmm2, XMMWORD PTR [__svml_datan2_ha_data_internal+1280]
        vmovupd   XMMWORD PTR [448+rsp], xmm2
        vpshufd   xmm2, xmm15, 221
        vpshufd   xmm3, xmm3, 221
        vpsubd    xmm2, xmm2, xmm4
        vpshufd   xmm1, xmm1, 221
        vpsubd    xmm3, xmm3, xmm4
        vpshufd   xmm0, xmm0, 221
        vpsrad    xmm15, xmm2, 31
        vpsrad    xmm2, xmm3, 31
        vpsubd    xmm3, xmm1, xmm4
        vpsubd    xmm4, xmm0, xmm4
        vpsrad    xmm3, xmm3, 31
        vpsrad    xmm4, xmm4, 31
        vpaddd    xmm15, xmm15, xmm2
        vpaddd    xmm3, xmm3, xmm4
        vmovq     xmm14, QWORD PTR [__svml_datan2_ha_data_internal+1408]
        vpaddd    xmm2, xmm15, xmm3
        vpaddd    xmm14, xmm2, xmm14
        vpslld    xmm3, xmm14, 5
        vmovd     r8d, xmm3
        vpextrd   r9d, xmm3, 1
        vmovq     xmm2, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+r10+r8]
        vmovq     xmm4, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+16+r10+r8]
        vmovhpd   xmm1, xmm2, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+r10+r9]
        vmovhpd   xmm2, xmm4, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+16+r10+r9]
        vmulpd    xmm4, xmm5, xmm1
        vmovq     xmm0, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+24+r10+r8]
        vmovhpd   xmm0, xmm0, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+24+r10+r9]
        vandpd    xmm14, xmm0, xmm10
        vandpd    xmm5, xmm0, xmm5
        vmulpd    xmm10, xmm10, xmm1
        vsubpd    xmm4, xmm14, xmm4
        vaddpd    xmm10, xmm5, xmm10
        vmulpd    xmm14, xmm1, xmm8
        vmulpd    xmm5, xmm1, xmm9
        vmovq     xmm15, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+8+r10+r8]
        vandpd    xmm8, xmm0, xmm8
        vmovhpd   xmm3, xmm15, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+8+r10+r9]
        vandpd    xmm15, xmm0, xmm9
        vsubpd    xmm15, xmm15, xmm14
        vaddpd    xmm9, xmm8, xmm5
        vsubpd    xmm14, xmm4, xmm15
        vaddpd    xmm5, xmm10, xmm9
        vsubpd    xmm4, xmm14, xmm4
        vsubpd    xmm8, xmm10, xmm5
        vsubpd    xmm4, xmm15, xmm4
        vaddpd    xmm10, xmm9, xmm8
        vandpd    xmm15, xmm14, xmm6
        vsubpd    xmm14, xmm14, xmm15
        vaddpd    xmm4, xmm4, xmm14
        vandpd    xmm14, xmm5, xmm6
        vsubpd    xmm6, xmm5, xmm14
        vpshufd   xmm1, xmm14, 221
        vaddpd    xmm0, xmm10, xmm6
        vmovq     xmm10, QWORD PTR [__svml_datan2_ha_data_internal+1600]
        vpslld    xmm8, xmm1, 3
        vmovq     xmm5, QWORD PTR [__svml_datan2_ha_data_internal+1664]
        vpand     xmm8, xmm8, xmm10
        vpor      xmm5, xmm8, xmm5
        vrcpps    xmm8, xmm5
        vmovq     xmm9, QWORD PTR [__svml_datan2_ha_data_internal+1728]
        vpsrld    xmm10, xmm8, 3
        vmovq     xmm6, QWORD PTR [__svml_datan2_ha_data_internal+1472]
        vpsubd    xmm8, xmm10, xmm9
        vmovq     xmm9, QWORD PTR [__svml_datan2_ha_data_internal+1536]
        vpand     xmm6, xmm1, xmm6
        vpsubd    xmm1, xmm9, xmm6
        vpaddd    xmm8, xmm8, xmm1
        vpshufd   xmm10, xmm8, 80
        vandpd    xmm10, xmm10, XMMWORD PTR [__svml_datan2_ha_data_internal+3008]
        vmulpd    xmm14, xmm14, xmm10
        vmulpd    xmm0, xmm10, xmm0
        vsubpd    xmm8, xmm14, XMMWORD PTR [__svml_datan2_ha_data_internal+1792]
        vaddpd    xmm8, xmm8, xmm0
        vmulpd    xmm5, xmm8, xmm8
        vsubpd    xmm9, xmm5, xmm8
        vmulpd    xmm6, xmm8, xmm9
        vaddpd    xmm0, xmm8, xmm6
        vmulpd    xmm1, xmm8, xmm0
        vsubpd    xmm14, xmm1, xmm8
        vmulpd    xmm5, xmm8, xmm14
        vaddpd    xmm9, xmm8, xmm5
        vmulpd    xmm6, xmm8, xmm9
        vsubpd    xmm8, xmm6, xmm8
        vmulpd    xmm8, xmm10, xmm8
        vmulpd    xmm5, xmm15, xmm8
        vmulpd    xmm9, xmm4, xmm8
        vmulpd    xmm4, xmm10, xmm4
        vaddpd    xmm6, xmm5, xmm9
        vmulpd    xmm8, xmm15, xmm10
        vaddpd    xmm5, xmm6, xmm4
        vaddpd    xmm10, xmm5, xmm8
        vaddpd    xmm2, xmm2, xmm5
        vmulpd    xmm9, xmm10, xmm10
        vmulpd    xmm6, xmm9, xmm9
        vmulpd    xmm15, xmm6, XMMWORD PTR [__svml_datan2_ha_data_internal+1856]
        vmulpd    xmm0, xmm6, XMMWORD PTR [__svml_datan2_ha_data_internal+1920]
        vaddpd    xmm1, xmm15, XMMWORD PTR [__svml_datan2_ha_data_internal+1984]
        vaddpd    xmm14, xmm0, XMMWORD PTR [__svml_datan2_ha_data_internal+2048]
        vmulpd    xmm4, xmm6, xmm1
        vmulpd    xmm15, xmm6, xmm14
        vaddpd    xmm0, xmm4, XMMWORD PTR [__svml_datan2_ha_data_internal+2112]
        vaddpd    xmm4, xmm15, XMMWORD PTR [__svml_datan2_ha_data_internal+2176]
        vmulpd    xmm1, xmm6, xmm0
        vmulpd    xmm14, xmm6, xmm4
        vaddpd    xmm15, xmm1, XMMWORD PTR [__svml_datan2_ha_data_internal+2240]
        vaddpd    xmm0, xmm14, XMMWORD PTR [__svml_datan2_ha_data_internal+2304]
        vmulpd    xmm15, xmm6, xmm15
        vmulpd    xmm1, xmm6, xmm0
        vaddpd    xmm4, xmm15, XMMWORD PTR [__svml_datan2_ha_data_internal+2368]
        vaddpd    xmm0, xmm1, XMMWORD PTR [__svml_datan2_ha_data_internal+2432]
        vmulpd    xmm14, xmm6, xmm4
        vmulpd    xmm6, xmm6, xmm0
        vaddpd    xmm1, xmm14, XMMWORD PTR [__svml_datan2_ha_data_internal+2496]
        vaddpd    xmm0, xmm6, XMMWORD PTR [__svml_datan2_ha_data_internal+2560]
        vmulpd    xmm4, xmm9, xmm1
        vaddpd    xmm6, xmm0, xmm4
        vaddpd    xmm0, xmm7, xmm2
        vmulpd    xmm9, xmm9, xmm6
        vmulpd    xmm1, xmm10, xmm9
        vaddpd    xmm2, xmm0, xmm1
        vaddpd    xmm4, xmm8, xmm2
        vaddpd    xmm3, xmm3, xmm4
        vxorpd    xmm11, xmm3, xmm11
        vaddpd    xmm0, xmm12, xmm11
        vmovdqu   xmm7, XMMWORD PTR [480+rsp]
        vorpd     xmm0, xmm0, xmm13
        vmovmskps r11d, xmm7
        vmovupd   xmm1, XMMWORD PTR [464+rsp]
        vmovupd   xmm8, XMMWORD PTR [432+rsp]
        vmovupd   xmm14, XMMWORD PTR [448+rsp]
        mov       QWORD PTR [504+rsp], r13
        test      r11d, 3
        jne       _B6_12

_B6_2::

        test      eax, eax
        jne       _B6_4

_B6_3::

        vmovups   xmm6, XMMWORD PTR [256+rsp]
        vmovups   xmm7, XMMWORD PTR [272+rsp]
        vmovups   xmm8, XMMWORD PTR [384+rsp]
        vmovups   xmm9, XMMWORD PTR [400+rsp]
        vmovups   xmm10, XMMWORD PTR [368+rsp]
        vmovups   xmm11, XMMWORD PTR [352+rsp]
        vmovups   xmm12, XMMWORD PTR [336+rsp]
        vmovups   xmm13, XMMWORD PTR [320+rsp]
        vmovups   xmm14, XMMWORD PTR [304+rsp]
        vmovups   xmm15, XMMWORD PTR [288+rsp]
        mov       r13, QWORD PTR [496+rsp]
        add       rsp, 520
        ret

_B6_4::

        vmovupd   xmm1, XMMWORD PTR [32+rsp]
        vmovupd   XMMWORD PTR [r13], xmm1
        vmovupd   XMMWORD PTR [64+r13], xmm8
        vmovupd   XMMWORD PTR [128+r13], xmm0
        je        _B6_3

_B6_7::

        xor       edx, edx
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, edx
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, eax

_B6_8::

        bt        esi, ebx
        jc        _B6_11

_B6_9::

        inc       ebx
        cmp       ebx, 2
        jl        _B6_8

_B6_10::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        vmovupd   xmm0, XMMWORD PTR [128+r13]
        jmp       _B6_3

_B6_11::

        lea       rcx, QWORD PTR [r13+rbx*8]
        lea       rdx, QWORD PTR [64+r13+rbx*8]
        lea       r8, QWORD PTR [128+r13+rbx*8]

        call      __svml_datan2_ha_cout_rare_internal
        jmp       _B6_9

_B6_12::

        vcmpnltpd xmm10, xmm1, xmm14
        vcmpordpd xmm12, xmm8, xmm8
        vblendvpd xmm3, xmm14, xmm1, xmm10
        vmovupd   xmm11, XMMWORD PTR [32+rsp]
        vmovupd   xmm2, XMMWORD PTR [416+rsp]
        vcmpordpd xmm13, xmm11, xmm11
        vcmpeqpd  xmm14, xmm14, xmm2
        vcmpeqpd  xmm15, xmm1, xmm2
        vcmpeqpd  xmm3, xmm3, xmm2
        vmovupd   xmm9, XMMWORD PTR [__svml_datan2_ha_data_internal+832]
        vorpd     xmm1, xmm14, xmm15
        vandpd    xmm6, xmm8, xmm9
        vandpd    xmm5, xmm11, xmm9
        vandpd    xmm9, xmm12, xmm13
        vandpd    xmm4, xmm10, XMMWORD PTR [__svml_datan2_ha_data_internal+3136]
        vpshufd   xmm10, xmm1, 221
        vpshufd   xmm11, xmm9, 221
        vblendvpd xmm4, xmm4, xmm2, xmm3
        vpand     xmm1, xmm10, xmm11
        vpshufd   xmm2, xmm2, 221
        vpandn    xmm7, xmm1, xmm7
        vorpd     xmm10, xmm4, xmm6
        vpshufd   xmm6, xmm8, 221
        vmovmskps eax, xmm7
        vpcmpgtd  xmm7, xmm2, xmm6
        vpshufd   xmm9, xmm7, 80
        vandpd    xmm11, xmm9, XMMWORD PTR [__svml_datan2_ha_data_internal+3072]
        vaddpd    xmm12, xmm10, xmm11
        vpshufd   xmm1, xmm1, 80
        vorpd     xmm5, xmm12, xmm5
        vblendvpd xmm0, xmm0, xmm5, xmm1
        and       eax, 3
        jmp       _B6_2
        ALIGN     16

_B6_13::

__svml_atan22_ha_e9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_atan22_ha_e9_B1_B4:
	DD	1603073
	DD	4117622
	DD	1075310
	DD	1144933
	DD	1607772
	DD	1677395
	DD	1550410
	DD	1488961
	DD	1427512
	DD	1366063
	DD	1304614
	DD	1243163
	DD	4260107

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B6_1
	DD	imagerel _B6_7
	DD	imagerel _unwind___svml_atan22_ha_e9_B1_B4

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_atan22_ha_e9_B7_B11:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B6_1
	DD	imagerel _B6_7
	DD	imagerel _unwind___svml_atan22_ha_e9_B1_B4

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B6_7
	DD	imagerel _B6_12
	DD	imagerel _unwind___svml_atan22_ha_e9_B7_B11

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_atan22_ha_e9_B12_B12:
	DD	33
	DD	imagerel _B6_1
	DD	imagerel _B6_7
	DD	imagerel _unwind___svml_atan22_ha_e9_B1_B4

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B6_12
	DD	imagerel _B6_13
	DD	imagerel _unwind___svml_atan22_ha_e9_B12_B12

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST6:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_atan21_ha_e9

__svml_atan21_ha_e9	PROC

_B7_1::

        DB        243
        DB        15
        DB        30
        DB        250
L227::

        sub       rsp, 472
        mov       r8d, -2144337920
        vmovups   XMMWORD PTR [240+rsp], xmm15
        mov       r9d, -36700160
        vmovups   XMMWORD PTR [256+rsp], xmm14
        mov       r10d, 1005584384
        vmovups   XMMWORD PTR [272+rsp], xmm13
        mov       r11d, 4
        vmovups   XMMWORD PTR [288+rsp], xmm12
        lea       rax, QWORD PTR [__ImageBase]
        vmovups   XMMWORD PTR [304+rsp], xmm11
        vmovups   XMMWORD PTR [320+rsp], xmm10
        vmovups   XMMWORD PTR [336+rsp], xmm9
        vmovups   XMMWORD PTR [352+rsp], xmm8
        vmovups   XMMWORD PTR [368+rsp], xmm7
        vmovups   XMMWORD PTR [384+rsp], xmm6
        mov       QWORD PTR [232+rsp], r13
        lea       r13, QWORD PTR [95+rsp]
        vmovupd   xmm11, XMMWORD PTR [rdx]
        and       r13, -64
        vmovsd    xmm8, QWORD PTR [__svml_datan2_ha_data_internal+2880]
        vmovupd   xmm2, XMMWORD PTR [rcx]
        xor       ecx, ecx
        vmovsd    xmm12, QWORD PTR [__svml_datan2_ha_data_internal+896]
        vcmpltsd  xmm1, xmm11, xmm8
        vmovsd    xmm3, QWORD PTR [__svml_datan2_ha_data_internal+960]
        vandpd    xmm13, xmm11, xmm12
        vmovsd    xmm0, QWORD PTR [__svml_datan2_ha_data_internal+1024]
        vandpd    xmm14, xmm2, xmm12
        vmovsd    xmm5, QWORD PTR [__svml_datan2_ha_data_internal+1088]
        vxorpd    xmm10, xmm11, xmm13
        vmovupd   XMMWORD PTR [432+rsp], xmm11
        vandpd    xmm4, xmm14, xmm5
        vmovupd   XMMWORD PTR [416+rsp], xmm8
        vandpd    xmm11, xmm1, xmm3
        vpshufd   xmm3, xmm14, 85
        vandpd    xmm8, xmm1, xmm0
        vpshufd   xmm1, xmm13, 85
        vmovd     xmm0, r8d
        vmovupd   XMMWORD PTR [400+rsp], xmm2
        vxorpd    xmm12, xmm2, xmm14
        vmovupd   XMMWORD PTR [448+rsp], xmm14
        vpsubd    xmm2, xmm1, xmm0
        vpsubd    xmm15, xmm3, xmm0
        vpsubd    xmm1, xmm3, xmm1
        vmovd     xmm0, r10d
        vandpd    xmm6, xmm13, xmm5
        vpaddd    xmm1, xmm1, xmm0
        mov       r8d, 8388607
        mov       r10d, 133169152
        vsubsd    xmm9, xmm14, xmm4
        vsubsd    xmm7, xmm13, xmm6
        vmovd     xmm14, r9d
        mov       r9d, 1065353216
        vpcmpgtd  xmm0, xmm2, xmm14
        vpcmpeqd  xmm2, xmm2, xmm14
        vpor      xmm0, xmm0, xmm2
        vpcmpgtd  xmm2, xmm15, xmm14
        vpcmpeqd  xmm15, xmm15, xmm14
        vpor      xmm2, xmm2, xmm15
        vpxor     xmm14, xmm14, xmm14
        vpor      xmm2, xmm0, xmm2
        vpcmpgtd  xmm0, xmm14, xmm1
        vpcmpeqd  xmm1, xmm14, xmm1
        vpor      xmm15, xmm0, xmm1
        vmulsd    xmm1, xmm13, QWORD PTR [__svml_datan2_ha_data_internal+1152]
        vpor      xmm2, xmm2, xmm15
        vmulsd    xmm0, xmm13, QWORD PTR [__svml_datan2_ha_data_internal+1216]
        vmulsd    xmm14, xmm13, QWORD PTR [__svml_datan2_ha_data_internal+1280]
        vmulsd    xmm15, xmm13, QWORD PTR [__svml_datan2_ha_data_internal+1344]
        vpshufd   xmm1, xmm1, 85
        vpshufd   xmm15, xmm15, 85
        vpsubd    xmm1, xmm1, xmm3
        vpshufd   xmm0, xmm0, 85
        vpsubd    xmm15, xmm15, xmm3
        vpshufd   xmm14, xmm14, 85
        vpsubd    xmm0, xmm0, xmm3
        vpsubd    xmm3, xmm14, xmm3
        vpsrad    xmm1, xmm1, 31
        vpsrad    xmm15, xmm15, 31
        vpsrad    xmm0, xmm0, 31
        vpsrad    xmm14, xmm3, 31
        vpaddd    xmm3, xmm1, xmm15
        vpaddd    xmm1, xmm0, xmm14
        vmovd     xmm15, r11d
        vpaddd    xmm0, xmm3, xmm1
        mov       r11d, -1048576
        vpaddd    xmm14, xmm0, xmm15
        vpslld    xmm3, xmm14, 5
        vmovd     edx, xmm3
        vmovq     xmm1, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+rax+rdx]
        vmulsd    xmm3, xmm1, xmm6
        vmovq     xmm0, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+24+rax+rdx]
        vandpd    xmm15, xmm0, xmm4
        vandpd    xmm14, xmm0, xmm9
        vmulsd    xmm4, xmm1, xmm4
        vsubsd    xmm3, xmm15, xmm3
        vmulsd    xmm15, xmm1, xmm7
        vandpd    xmm6, xmm0, xmm6
        vsubsd    xmm14, xmm14, xmm15
        vaddsd    xmm4, xmm4, xmm6
        vsubsd    xmm15, xmm3, xmm14
        mov       QWORD PTR [464+rsp], r13
        vsubsd    xmm3, xmm15, xmm3
        vsubsd    xmm3, xmm14, xmm3
        vandpd    xmm14, xmm15, xmm5
        vsubsd    xmm15, xmm15, xmm14
        vaddsd    xmm3, xmm15, xmm3
        vandpd    xmm15, xmm0, xmm7
        vmulsd    xmm7, xmm1, xmm9
        vaddsd    xmm6, xmm7, xmm15
        vaddsd    xmm1, xmm4, xmm6
        vandpd    xmm0, xmm1, xmm5
        vsubsd    xmm9, xmm4, xmm1
        vsubsd    xmm5, xmm1, xmm0
        vaddsd    xmm15, xmm6, xmm9
        vpshufd   xmm9, xmm0, 85
        vmovd     xmm4, r8d
        vpslld    xmm7, xmm9, 3
        mov       r8d, 2145386496
        vpand     xmm6, xmm7, xmm4
        vaddsd    xmm1, xmm15, xmm5
        vmovd     xmm15, r9d
        vpor      xmm7, xmm6, xmm15
        vmovd     xmm15, r10d
        vrcpps    xmm4, xmm7
        vmovd     xmm7, r11d
        vpsrld    xmm6, xmm4, 3
        vpand     xmm7, xmm9, xmm7
        vmovd     xmm9, r8d
        vpsubd    xmm6, xmm6, xmm15
        vpsubd    xmm4, xmm9, xmm7
        vpaddd    xmm6, xmm6, xmm4
        vmovsd    xmm5, QWORD PTR [__svml_datan2_ha_data_internal+3008]
        vpshufd   xmm9, xmm6, 0
        vandpd    xmm4, xmm9, xmm5
        vmulsd    xmm5, xmm0, xmm4
        vmulsd    xmm1, xmm1, xmm4
        vsubsd    xmm0, xmm5, QWORD PTR [__svml_datan2_ha_data_internal+1792]
        vaddsd    xmm7, xmm1, xmm0
        vmulsd    xmm6, xmm7, xmm7
        vsubsd    xmm9, xmm6, xmm7
        vmulsd    xmm5, xmm9, xmm7
        vaddsd    xmm0, xmm5, xmm7
        vmulsd    xmm1, xmm0, xmm7
        vmovsd    xmm0, QWORD PTR [__svml_datan2_ha_data_internal+1920]
        vsubsd    xmm15, xmm1, xmm7
        vmulsd    xmm6, xmm15, xmm7
        vaddsd    xmm9, xmm6, xmm7
        vmulsd    xmm5, xmm9, xmm7
        vsubsd    xmm7, xmm5, xmm7
        vmulsd    xmm7, xmm7, xmm4
        vmulsd    xmm9, xmm7, xmm14
        vmulsd    xmm6, xmm7, xmm3
        vmulsd    xmm3, xmm4, xmm3
        vmulsd    xmm7, xmm4, xmm14
        vaddsd    xmm5, xmm6, xmm9
        vmovsd    xmm14, QWORD PTR [__svml_datan2_ha_data_internal+1856]
        vaddsd    xmm6, xmm3, xmm5
        vaddsd    xmm4, xmm7, xmm6
        vmulsd    xmm9, xmm4, xmm4
        vmulsd    xmm5, xmm9, xmm9
        vmulsd    xmm14, xmm14, xmm5
        vmulsd    xmm1, xmm0, xmm5
        vaddsd    xmm3, xmm14, QWORD PTR [__svml_datan2_ha_data_internal+1984]
        vaddsd    xmm0, xmm1, QWORD PTR [__svml_datan2_ha_data_internal+2048]
        vmulsd    xmm15, xmm3, xmm5
        vmulsd    xmm1, xmm0, xmm5
        vaddsd    xmm3, xmm15, QWORD PTR [__svml_datan2_ha_data_internal+2112]
        vaddsd    xmm15, xmm1, QWORD PTR [__svml_datan2_ha_data_internal+2176]
        vmulsd    xmm14, xmm3, xmm5
        vmulsd    xmm15, xmm15, xmm5
        vaddsd    xmm0, xmm14, QWORD PTR [__svml_datan2_ha_data_internal+2240]
        vaddsd    xmm3, xmm15, QWORD PTR [__svml_datan2_ha_data_internal+2304]
        vmulsd    xmm1, xmm0, xmm5
        vmulsd    xmm14, xmm3, xmm5
        vaddsd    xmm0, xmm1, QWORD PTR [__svml_datan2_ha_data_internal+2368]
        vaddsd    xmm3, xmm14, QWORD PTR [__svml_datan2_ha_data_internal+2432]
        vmulsd    xmm1, xmm0, xmm5
        vmulsd    xmm5, xmm3, xmm5
        vaddsd    xmm14, xmm1, QWORD PTR [__svml_datan2_ha_data_internal+2496]
        vaddsd    xmm1, xmm5, QWORD PTR [__svml_datan2_ha_data_internal+2560]
        vmulsd    xmm0, xmm14, xmm9
        vaddsd    xmm3, xmm0, xmm1
        vxorpd    xmm0, xmm8, xmm10
        vaddsd    xmm8, xmm6, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+16+rax+rdx]
        vmulsd    xmm9, xmm3, xmm9
        vaddsd    xmm1, xmm8, xmm0
        vmulsd    xmm4, xmm9, xmm4
        vmovupd   xmm9, XMMWORD PTR [432+rsp]
        vaddsd    xmm3, xmm4, xmm1
        vaddsd    xmm5, xmm7, xmm3
        vaddsd    xmm6, xmm5, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+8+rax+rdx]
        vmovmskps eax, xmm2
        vxorpd    xmm10, xmm6, xmm10
        vmovupd   xmm5, XMMWORD PTR [448+rsp]
        vaddsd    xmm0, xmm10, xmm11
        vorpd     xmm0, xmm0, xmm12
        test      al, 1
        jne       _B7_8

_B7_2::

        test      ecx, ecx
        jne       _B7_4

_B7_3::

        vmovups   xmm6, XMMWORD PTR [384+rsp]
        vmovups   xmm7, XMMWORD PTR [368+rsp]
        vmovups   xmm8, XMMWORD PTR [352+rsp]
        vmovups   xmm9, XMMWORD PTR [336+rsp]
        vmovups   xmm10, XMMWORD PTR [320+rsp]
        vmovups   xmm11, XMMWORD PTR [304+rsp]
        vmovups   xmm12, XMMWORD PTR [288+rsp]
        vmovups   xmm13, XMMWORD PTR [272+rsp]
        vmovups   xmm14, XMMWORD PTR [256+rsp]
        vmovups   xmm15, XMMWORD PTR [240+rsp]
        mov       r13, QWORD PTR [232+rsp]
        add       rsp, 472
        ret

_B7_4::

        vmovupd   xmm1, XMMWORD PTR [400+rsp]
        vmovsd    QWORD PTR [r13], xmm1
        vmovsd    QWORD PTR [64+r13], xmm9
        vmovsd    QWORD PTR [128+r13], xmm0
        jne       _B7_6

_B7_5::

        vmovsd    xmm0, QWORD PTR [128+r13]
        jmp       _B7_3

_B7_6::

        je        _B7_5

_B7_7::

        lea       rcx, QWORD PTR [r13]
        lea       rdx, QWORD PTR [64+r13]
        lea       r8, QWORD PTR [128+r13]

        call      __svml_datan2_ha_cout_rare_internal
        jmp       _B7_5

_B7_8::

        vmovupd   xmm14, XMMWORD PTR [400+rsp]
        vcmpordsd xmm15, xmm9, xmm9
        vcmpordsd xmm1, xmm14, xmm14
        vcmpnltsd xmm12, xmm5, xmm13
        vmovsd    xmm10, QWORD PTR [__svml_datan2_ha_data_internal+832]
        vandpd    xmm7, xmm9, xmm10
        vandpd    xmm6, xmm14, xmm10
        vandpd    xmm10, xmm15, xmm1
        vmovupd   xmm1, XMMWORD PTR [416+rsp]
        vblendvpd xmm3, xmm13, xmm5, xmm12
        vcmpeqsd  xmm13, xmm13, xmm1
        vcmpeqsd  xmm5, xmm5, xmm1
        vmovsd    xmm11, QWORD PTR [__svml_datan2_ha_data_internal+3136]
        vorpd     xmm13, xmm13, xmm5
        vpshufd   xmm5, xmm13, 85
        vandpd    xmm4, xmm12, xmm11
        vpshufd   xmm11, xmm10, 85
        vpand     xmm13, xmm5, xmm11
        vpandn    xmm2, xmm13, xmm2
        vmovmskps ecx, xmm2
        vcmpeqsd  xmm2, xmm3, xmm1
        vblendvpd xmm3, xmm4, xmm1, xmm2
        and       ecx, 1
        vpshufd   xmm4, xmm9, 85
        vpshufd   xmm1, xmm1, 85
        vorpd     xmm10, xmm3, xmm7
        vpcmpgtd  xmm5, xmm1, xmm4
        vmovsd    xmm8, QWORD PTR [__svml_datan2_ha_data_internal+3072]
        vpshufd   xmm7, xmm5, 0
        vandpd    xmm8, xmm7, xmm8
        vpshufd   xmm12, xmm13, 0
        vaddsd    xmm11, xmm10, xmm8
        vorpd     xmm6, xmm11, xmm6
        vblendvpd xmm0, xmm0, xmm6, xmm12
        jmp       _B7_2
        ALIGN     16

_B7_9::

__svml_atan21_ha_e9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_atan21_ha_e9_B1_B8:
	DD	1608705
	DD	1954956
	DD	1599620
	DD	1538171
	DD	1476722
	DD	1415273
	DD	1353824
	DD	1292375
	DD	1230919
	DD	1169464
	DD	1108009
	DD	1046554
	DD	3866891

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B7_1
	DD	imagerel _B7_9
	DD	imagerel _unwind___svml_atan21_ha_e9_B1_B8

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST7:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_atan21_ha_l9

__svml_atan21_ha_l9	PROC

_B8_1::

        DB        243
        DB        15
        DB        30
        DB        250
L259::

        sub       rsp, 424
        mov       r8d, -2144337920
        vmovups   XMMWORD PTR [240+rsp], xmm15
        mov       r9d, -36700160
        vmovups   XMMWORD PTR [256+rsp], xmm14
        mov       r10d, 4
        vmovups   XMMWORD PTR [272+rsp], xmm13
        lea       rax, QWORD PTR [__ImageBase]
        vmovups   XMMWORD PTR [288+rsp], xmm12
        vmovd     xmm14, r8d
        vmovups   XMMWORD PTR [304+rsp], xmm11
        mov       r11d, 8388607
        vmovups   XMMWORD PTR [320+rsp], xmm10
        vmovd     xmm10, r9d
        vmovups   XMMWORD PTR [336+rsp], xmm9
        mov       r8d, 1065353216
        vmovups   XMMWORD PTR [352+rsp], xmm8
        mov       r9d, 133169152
        vmovups   XMMWORD PTR [368+rsp], xmm7
        vmovups   XMMWORD PTR [384+rsp], xmm6
        mov       QWORD PTR [232+rsp], r13
        lea       r13, QWORD PTR [95+rsp]
        vmovupd   xmm11, XMMWORD PTR [rdx]
        and       r13, -64
        vmovsd    xmm4, QWORD PTR [__svml_datan2_ha_data_internal+2880]
        vmovupd   xmm12, XMMWORD PTR [rcx]
        xor       ecx, ecx
        vmovsd    xmm8, QWORD PTR [__svml_datan2_ha_data_internal+896]
        vcmpltsd  xmm2, xmm11, xmm4
        vmovsd    xmm0, QWORD PTR [__svml_datan2_ha_data_internal+1024]
        vandpd    xmm9, xmm11, xmm8
        vandpd    xmm8, xmm12, xmm8
        vxorpd    xmm5, xmm11, xmm9
        vmovupd   XMMWORD PTR [400+rsp], xmm4
        vandpd    xmm4, xmm2, xmm0
        vpshufd   xmm0, xmm8, 85
        vxorpd    xmm7, xmm12, xmm8
        vpshufd   xmm1, xmm9, 85
        vpsubd    xmm15, xmm0, xmm14
        vmovsd    xmm3, QWORD PTR [__svml_datan2_ha_data_internal+960]
        vpsubd    xmm13, xmm1, xmm14
        vandpd    xmm6, xmm2, xmm3
        vpcmpgtd  xmm3, xmm13, xmm10
        vpcmpeqd  xmm2, xmm13, xmm10
        vpcmpgtd  xmm1, xmm15, xmm10
        vpcmpeqd  xmm10, xmm15, xmm10
        vpor      xmm14, xmm3, xmm2
        vmulsd    xmm2, xmm9, QWORD PTR [__svml_datan2_ha_data_internal+1152]
        vpor      xmm10, xmm1, xmm10
        vmulsd    xmm15, xmm9, QWORD PTR [__svml_datan2_ha_data_internal+1216]
        vmulsd    xmm1, xmm9, QWORD PTR [__svml_datan2_ha_data_internal+1280]
        vmulsd    xmm13, xmm9, QWORD PTR [__svml_datan2_ha_data_internal+1344]
        vpshufd   xmm13, xmm13, 85
        vpor      xmm10, xmm14, xmm10
        vpshufd   xmm14, xmm2, 85
        vpshufd   xmm15, xmm15, 85
        vpsubd    xmm2, xmm14, xmm0
        vpshufd   xmm1, xmm1, 85
        vpsubd    xmm14, xmm13, xmm0
        vpsubd    xmm13, xmm15, xmm0
        vpsubd    xmm0, xmm1, xmm0
        vpsrad    xmm2, xmm2, 31
        vpsrad    xmm14, xmm14, 31
        vpsrad    xmm15, xmm13, 31
        vpsrad    xmm13, xmm0, 31
        vpaddd    xmm14, xmm2, xmm14
        vpaddd    xmm0, xmm15, xmm13
        vpaddd    xmm2, xmm14, xmm0
        vmovd     xmm15, r10d
        vpaddd    xmm1, xmm2, xmm15
        mov       r10d, -1048576
        vpslld    xmm14, xmm1, 5
        vmovd     edx, xmm14
        vmovsd    xmm3, QWORD PTR [__svml_datan2_ha_data_internal+1792]
        vmovq     xmm15, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+24+rax+rdx]
        vmovq     xmm2, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+rax+rdx]
        vandpd    xmm1, xmm15, xmm9
        vmovdqa   xmm0, xmm2
        vandpd    xmm13, xmm15, xmm8
        vfmadd213sd xmm2, xmm8, xmm1
        vmovd     xmm1, r11d
        vfnmadd213sd xmm0, xmm9, xmm13
        vmovsd    xmm13, QWORD PTR [__svml_datan2_ha_data_internal+3008]
        vpshufd   xmm14, xmm2, 85
        vpslld    xmm15, xmm14, 3
        mov       r11d, 2145386496
        vpand     xmm15, xmm15, xmm1
        vmovd     xmm1, r8d
        vpor      xmm15, xmm15, xmm1
        vrcpps    xmm1, xmm15
        vpsrld    xmm15, xmm1, 3
        vmovd     xmm1, r9d
        vpsubd    xmm15, xmm15, xmm1
        vmovd     xmm1, r10d
        vpand     xmm1, xmm14, xmm1
        vmovd     xmm14, r11d
        vpsubd    xmm14, xmm14, xmm1
        vpaddd    xmm15, xmm15, xmm14
        vpshufd   xmm14, xmm15, 0
        vandpd    xmm1, xmm14, xmm13
        vmovaps   xmm13, xmm2
        vfnmadd213sd xmm13, xmm1, xmm3
        vmovaps   xmm14, xmm2
        vfmadd213sd xmm13, xmm13, xmm13
        vfmadd213sd xmm1, xmm13, xmm1
        vfnmadd213sd xmm14, xmm1, xmm3
        vfmadd213sd xmm1, xmm14, xmm1
        vmulsd    xmm14, xmm1, xmm0
        vfnmadd213sd xmm2, xmm14, xmm0
        vmovsd    xmm0, QWORD PTR [__svml_datan2_ha_data_internal+1920]
        vmulsd    xmm13, xmm2, xmm1
        vmulsd    xmm2, xmm14, xmm14
        vmulsd    xmm3, xmm2, xmm2
        vmovsd    xmm1, QWORD PTR [__svml_datan2_ha_data_internal+1856]
        vfmadd213sd xmm1, xmm3, QWORD PTR [__svml_datan2_ha_data_internal+1984]
        vfmadd213sd xmm0, xmm3, QWORD PTR [__svml_datan2_ha_data_internal+2048]
        vfmadd213sd xmm1, xmm3, QWORD PTR [__svml_datan2_ha_data_internal+2112]
        vfmadd213sd xmm0, xmm3, QWORD PTR [__svml_datan2_ha_data_internal+2176]
        vfmadd213sd xmm1, xmm3, QWORD PTR [__svml_datan2_ha_data_internal+2240]
        vfmadd213sd xmm0, xmm3, QWORD PTR [__svml_datan2_ha_data_internal+2304]
        vfmadd213sd xmm1, xmm3, QWORD PTR [__svml_datan2_ha_data_internal+2368]
        vfmadd213sd xmm0, xmm3, QWORD PTR [__svml_datan2_ha_data_internal+2432]
        vfmadd213sd xmm1, xmm3, QWORD PTR [__svml_datan2_ha_data_internal+2496]
        vfmadd213sd xmm0, xmm3, QWORD PTR [__svml_datan2_ha_data_internal+2560]
        vfmadd213sd xmm1, xmm2, xmm0
        vxorpd    xmm0, xmm4, xmm5
        vaddsd    xmm4, xmm13, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+16+rax+rdx]
        vmulsd    xmm15, xmm1, xmm2
        vaddsd    xmm4, xmm4, xmm0
        vfmadd213sd xmm15, xmm14, xmm4
        mov       QWORD PTR [416+rsp], r13
        vaddsd    xmm1, xmm14, xmm15
        vaddsd    xmm2, xmm1, QWORD PTR [imagerel(__svml_datan2_ha_data_internal)+8+rax+rdx]
        vmovmskps eax, xmm10
        vxorpd    xmm5, xmm2, xmm5
        vaddsd    xmm0, xmm5, xmm6
        vorpd     xmm0, xmm0, xmm7
        test      al, 1
        jne       _B8_8

_B8_2::

        test      ecx, ecx
        jne       _B8_4

_B8_3::

        vmovups   xmm6, XMMWORD PTR [384+rsp]
        vmovups   xmm7, XMMWORD PTR [368+rsp]
        vmovups   xmm8, XMMWORD PTR [352+rsp]
        vmovups   xmm9, XMMWORD PTR [336+rsp]
        vmovups   xmm10, XMMWORD PTR [320+rsp]
        vmovups   xmm11, XMMWORD PTR [304+rsp]
        vmovups   xmm12, XMMWORD PTR [288+rsp]
        vmovups   xmm13, XMMWORD PTR [272+rsp]
        vmovups   xmm14, XMMWORD PTR [256+rsp]
        vmovups   xmm15, XMMWORD PTR [240+rsp]
        mov       r13, QWORD PTR [232+rsp]
        add       rsp, 424
        ret

_B8_4::

        vmovsd    QWORD PTR [r13], xmm12
        vmovsd    QWORD PTR [64+r13], xmm11
        vmovsd    QWORD PTR [128+r13], xmm0
        jne       _B8_6

_B8_5::

        vmovsd    xmm0, QWORD PTR [128+r13]
        jmp       _B8_3

_B8_6::

        je        _B8_5

_B8_7::

        lea       rcx, QWORD PTR [r13]
        lea       rdx, QWORD PTR [64+r13]
        lea       r8, QWORD PTR [128+r13]

        call      __svml_datan2_ha_cout_rare_internal
        jmp       _B8_5

_B8_8::

        vmovsd    xmm13, QWORD PTR [__svml_datan2_ha_data_internal+832]
        vcmpordsd xmm2, xmm11, xmm11
        vcmpordsd xmm1, xmm12, xmm12
        vcmpnltsd xmm15, xmm8, xmm9
        vandpd    xmm1, xmm2, xmm1
        vandpd    xmm6, xmm11, xmm13
        vmovupd   xmm2, XMMWORD PTR [400+rsp]
        vandpd    xmm5, xmm12, xmm13
        vblendvpd xmm3, xmm9, xmm8, xmm15
        vcmpeqsd  xmm9, xmm9, xmm2
        vcmpeqsd  xmm8, xmm8, xmm2
        vorpd     xmm9, xmm9, xmm8
        vpshufd   xmm13, xmm9, 85
        vpshufd   xmm1, xmm1, 85
        vpand     xmm8, xmm13, xmm1
        vcmpeqsd  xmm1, xmm3, xmm2
        vmovsd    xmm14, QWORD PTR [__svml_datan2_ha_data_internal+3136]
        vpandn    xmm10, xmm8, xmm10
        vandpd    xmm4, xmm15, xmm14
        vblendvpd xmm3, xmm4, xmm2, xmm1
        vpshufd   xmm4, xmm11, 85
        vpshufd   xmm2, xmm2, 85
        vmovmskps ecx, xmm10
        vorpd     xmm10, xmm3, xmm6
        vpcmpgtd  xmm6, xmm2, xmm4
        vmovsd    xmm7, QWORD PTR [__svml_datan2_ha_data_internal+3072]
        vpshufd   xmm9, xmm6, 0
        and       ecx, 1
        vandpd    xmm7, xmm9, xmm7
        vpshufd   xmm8, xmm8, 0
        vaddsd    xmm13, xmm10, xmm7
        vorpd     xmm5, xmm13, xmm5
        vblendvpd xmm0, xmm0, xmm5, xmm8
        jmp       _B8_2
        ALIGN     16

_B8_9::

__svml_atan21_ha_l9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_atan21_ha_l9_B1_B8:
	DD	1614337
	DD	1954978
	DD	1599642
	DD	1538193
	DD	1476738
	DD	1415283
	DD	1353829
	DD	1292374
	DD	1230920
	DD	1169464
	DD	1108009
	DD	1046554
	DD	3473675

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B8_1
	DD	imagerel _B8_9
	DD	imagerel _unwind___svml_atan21_ha_l9_B1_B8

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST8:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_datan2_ha_cout_rare_internal

__svml_datan2_ha_cout_rare_internal	PROC

_B9_1::

        DB        243
        DB        15
        DB        30
        DB        250
L284::

        sub       rsp, 152
        movsd     xmm1, QWORD PTR [_vmldAtanHATab+1888]
        movsd     xmm0, QWORD PTR [rcx]
        mulsd     xmm0, xmm1
        mulsd     xmm1, QWORD PTR [rdx]
        movsd     QWORD PTR [56+rsp], xmm0
        movsd     QWORD PTR [64+rsp], xmm1
        movzx     eax, WORD PTR [62+rsp]
        and       eax, 32752
        mov       dl, BYTE PTR [71+rsp]
        movzx     r11d, WORD PTR [70+rsp]
        and       dl, -128
        and       r11d, 32752
        shr       eax, 4
        mov       cl, BYTE PTR [63+rsp]
        shr       cl, 7
        shr       dl, 7
        movups    XMMWORD PTR [112+rsp], xmm15
        movups    XMMWORD PTR [128+rsp], xmm13
        movups    XMMWORD PTR [96+rsp], xmm6
        shr       r11d, 4
        cmp       eax, 2047
        je        _B9_49

_B9_2::

        cmp       r11d, 2047
        je        _B9_38

_B9_3::

        test      eax, eax
        jne       _B9_6

_B9_4::

        test      DWORD PTR [60+rsp], 1048575
        jne       _B9_6

_B9_5::

        cmp       DWORD PTR [56+rsp], 0
        je        _B9_31

_B9_6::

        test      r11d, r11d
        jne       _B9_9

_B9_7::

        test      DWORD PTR [68+rsp], 1048575
        jne       _B9_9

_B9_8::

        cmp       DWORD PTR [64+rsp], 0
        je        _B9_29

_B9_9::

        neg       r11d
        movsd     QWORD PTR [56+rsp], xmm0
        add       r11d, eax
        movsd     QWORD PTR [64+rsp], xmm1
        mov       r10b, BYTE PTR [63+rsp]
        mov       r9b, BYTE PTR [71+rsp]
        and       r10b, 127
        and       r9b, 127
        cmp       r11d, -54
        jle       _B9_24

_B9_10::

        cmp       r11d, 54
        jge       _B9_21

_B9_11::

        mov       BYTE PTR [71+rsp], r9b
        mov       BYTE PTR [63+rsp], r10b
        test      dl, dl
        jne       _B9_13

_B9_12::

        movsd     xmm13, QWORD PTR [_vmldAtanHATab+1976]
        movaps    xmm15, xmm13
        jmp       _B9_14

_B9_13::

        movsd     xmm13, QWORD PTR [_vmldAtanHATab+1936]
        movsd     xmm15, QWORD PTR [_vmldAtanHATab+1944]

_B9_14::

        movsd     xmm1, QWORD PTR [56+rsp]
        movsd     xmm6, QWORD PTR [64+rsp]
        movaps    xmm2, xmm1
        divsd     xmm2, xmm6
        movzx     r9d, WORD PTR [62+rsp]
        movsd     QWORD PTR [88+rsp], xmm2
        test      eax, eax
        jle       _B9_37

_B9_15::

        cmp       eax, 2046
        jge       _B9_17

_B9_16::

        and       r9d, -32753
        add       eax, -1023
        movsd     QWORD PTR [56+rsp], xmm1
        add       r9d, 16368
        mov       WORD PTR [62+rsp], r9w
        jmp       _B9_18

_B9_17::

        movsd     xmm0, QWORD PTR [_vmldAtanHATab+1992]
        mov       eax, 1022
        mulsd     xmm1, xmm0
        movsd     QWORD PTR [56+rsp], xmm1

_B9_18::

        neg       eax
        add       eax, 1023
        and       eax, 2047
        movzx     r9d, WORD PTR [_vmldAtanHATab+1894]
        movsd     xmm0, QWORD PTR [_vmldAtanHATab+1888]
        and       r9d, -32753
        shl       eax, 4
        movsd     QWORD PTR [64+rsp], xmm0
        or        r9d, eax
        mov       WORD PTR [70+rsp], r9w
        movsd     xmm1, QWORD PTR [64+rsp]
        mulsd     xmm6, xmm1
        comisd    xmm2, QWORD PTR [_vmldAtanHATab+1880]
        jb        _B9_20

_B9_19::

        movsd     xmm1, QWORD PTR [_vmldAtanHATab+2000]
        mov       eax, 113
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
        cmovl     eax, r10d
        subsd     xmm0, xmm5
        add       eax, eax
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
        movsxd    rax, eax
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
        addsd     xmm1, QWORD PTR [imagerel(_vmldAtanHATab)+r9+rax*8]
        movsd     QWORD PTR [32+rsp], xmm1
        movsd     xmm0, QWORD PTR [32+rsp]
        subsd     xmm2, xmm0
        movsd     QWORD PTR [40+rsp], xmm2
        movsd     xmm3, QWORD PTR [32+rsp]
        movsd     xmm1, QWORD PTR [40+rsp]
        addsd     xmm3, xmm1
        movsd     QWORD PTR [48+rsp], xmm3
        movsd     xmm4, QWORD PTR [40+rsp]
        addsd     xmm4, QWORD PTR [imagerel(_vmldAtanHATab)+r10+rax*8]
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

_B9_56::

        movsd     xmm1, QWORD PTR [72+rsp]
        movaps    xmm2, xmm13
        lea       r9, QWORD PTR [__ImageBase]
        addsd     xmm1, xmm0
        shl       cl, 7
        addsd     xmm1, QWORD PTR [imagerel(_vmldAtanHATab)+8+r9+rax*8]
        mov       al, dl
        shl       al, 7
        movsd     QWORD PTR [72+rsp], xmm1
        shr       r10, 56
        mov       r11b, BYTE PTR [79+rsp]
        and       r10b, 127
        or        r10b, al
        mov       al, r11b
        mov       BYTE PTR [71+rsp], r10b
        and       al, 127
        movsd     xmm0, QWORD PTR [64+rsp]
        movaps    xmm4, xmm0
        addsd     xmm2, xmm0
        shr       r11b, 7
        movsd     QWORD PTR [32+rsp], xmm2
        movsd     xmm3, QWORD PTR [32+rsp]
        subsd     xmm4, xmm3
        movsd     QWORD PTR [40+rsp], xmm4
        xor       dl, r11b
        movsd     xmm6, QWORD PTR [32+rsp]
        movsd     xmm5, QWORD PTR [40+rsp]
        shl       dl, 7
        addsd     xmm6, xmm5
        movsd     QWORD PTR [48+rsp], xmm6
        movsd     xmm1, QWORD PTR [40+rsp]
        addsd     xmm13, xmm1
        movsd     QWORD PTR [40+rsp], xmm13
        or        al, dl
        movsd     xmm13, QWORD PTR [48+rsp]
        mov       BYTE PTR [79+rsp], al
        subsd     xmm0, xmm13
        movsd     QWORD PTR [48+rsp], xmm0
        movsd     xmm1, QWORD PTR [40+rsp]
        movsd     xmm0, QWORD PTR [48+rsp]
        addsd     xmm1, xmm0
        movsd     QWORD PTR [48+rsp], xmm1
        mov       rdx, QWORD PTR [32+rsp]
        movsd     xmm2, QWORD PTR [48+rsp]
        mov       QWORD PTR [64+rsp], rdx
        movsd     xmm3, QWORD PTR [64+rsp]
        addsd     xmm2, QWORD PTR [72+rsp]
        addsd     xmm15, xmm2
        movsd     QWORD PTR [72+rsp], xmm15
        addsd     xmm3, xmm15
        movsd     QWORD PTR [80+rsp], xmm3
        mov       al, BYTE PTR [87+rsp]
        and       al, 127
        or        al, cl
        mov       BYTE PTR [87+rsp], al
        mov       rcx, QWORD PTR [80+rsp]
        mov       QWORD PTR [r8], rcx
        jmp       _B9_36

_B9_20::

        movsd     xmm4, QWORD PTR [56+rsp]
        mov       r10b, dl
        movaps    xmm3, xmm4
        mulsd     xmm3, QWORD PTR [_vmldAtanHATab+2000]
        shl       r10b, 7
        shl       cl, 7
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
        mov       rax, QWORD PTR [48+rsp]
        movsd     xmm6, QWORD PTR [40+rsp]
        movsd     xmm1, QWORD PTR [48+rsp]
        mov       QWORD PTR [64+rsp], rax
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
        movaps    xmm5, xmm13
        or        r9b, r10b
        mov       BYTE PTR [71+rsp], r9b
        movsd     xmm6, QWORD PTR [64+rsp]
        movsd     QWORD PTR [72+rsp], xmm0
        movaps    xmm1, xmm6
        mov       r11b, BYTE PTR [79+rsp]
        mov       al, r11b
        shr       r11b, 7
        addsd     xmm5, xmm6
        movsd     QWORD PTR [32+rsp], xmm5
        movsd     xmm0, QWORD PTR [32+rsp]
        subsd     xmm1, xmm0
        movsd     QWORD PTR [40+rsp], xmm1
        xor       dl, r11b
        movsd     xmm3, QWORD PTR [32+rsp]
        and       al, 127
        movsd     xmm2, QWORD PTR [40+rsp]
        shl       dl, 7
        addsd     xmm3, xmm2
        movsd     QWORD PTR [48+rsp], xmm3
        movsd     xmm4, QWORD PTR [40+rsp]
        addsd     xmm13, xmm4
        movsd     QWORD PTR [40+rsp], xmm13
        or        al, dl
        movsd     xmm13, QWORD PTR [48+rsp]
        mov       BYTE PTR [79+rsp], al
        subsd     xmm6, xmm13
        movsd     QWORD PTR [48+rsp], xmm6
        movsd     xmm1, QWORD PTR [40+rsp]
        movsd     xmm0, QWORD PTR [48+rsp]
        addsd     xmm1, xmm0
        movsd     QWORD PTR [48+rsp], xmm1
        mov       rdx, QWORD PTR [32+rsp]
        movsd     xmm2, QWORD PTR [48+rsp]
        mov       QWORD PTR [64+rsp], rdx
        movsd     xmm3, QWORD PTR [64+rsp]
        addsd     xmm2, QWORD PTR [72+rsp]
        addsd     xmm15, xmm2
        movsd     QWORD PTR [72+rsp], xmm15
        addsd     xmm3, xmm15
        movsd     QWORD PTR [80+rsp], xmm3
        mov       al, BYTE PTR [87+rsp]
        and       al, 127
        or        al, cl
        mov       BYTE PTR [87+rsp], al
        mov       rcx, QWORD PTR [80+rsp]
        mov       QWORD PTR [r8], rcx
        jmp       _B9_36

_B9_21::

        cmp       r11d, 74
        jge       _B9_53

_B9_22::

        mov       BYTE PTR [63+rsp], r10b
        divsd     xmm1, QWORD PTR [56+rsp]
        movsd     xmm0, QWORD PTR [_vmldAtanHATab+1928]
        shl       cl, 7
        subsd     xmm0, xmm1
        addsd     xmm0, QWORD PTR [_vmldAtanHATab+1920]
        movsd     QWORD PTR [80+rsp], xmm0
        mov       al, BYTE PTR [87+rsp]
        and       al, 127
        or        al, cl
        mov       BYTE PTR [87+rsp], al
        mov       rdx, QWORD PTR [80+rsp]
        mov       QWORD PTR [r8], rdx
        jmp       _B9_36

_B9_24::

        test      dl, dl
        jne       _B9_35

_B9_25::

        mov       BYTE PTR [63+rsp], r10b
        mov       BYTE PTR [71+rsp], r9b
        movsd     xmm2, QWORD PTR [56+rsp]
        divsd     xmm2, QWORD PTR [64+rsp]
        movsd     QWORD PTR [80+rsp], xmm2
        movzx     eax, WORD PTR [86+rsp]
        test      eax, 32752
        je        _B9_27

_B9_26::

        movsd     xmm0, QWORD PTR [_vmldAtanHATab+1888]
        shl       cl, 7
        addsd     xmm0, xmm2
        movsd     QWORD PTR [32+rsp], xmm0
        movsd     xmm1, QWORD PTR [32+rsp]
        mulsd     xmm2, xmm1
        movsd     QWORD PTR [80+rsp], xmm2
        mov       al, BYTE PTR [87+rsp]
        and       al, 127
        or        al, cl
        mov       BYTE PTR [87+rsp], al
        mov       rdx, QWORD PTR [80+rsp]
        mov       QWORD PTR [r8], rdx
        jmp       _B9_36

_B9_27::

        mulsd     xmm2, xmm2
        shl       cl, 7
        movsd     QWORD PTR [32+rsp], xmm2
        movsd     xmm0, QWORD PTR [32+rsp]
        addsd     xmm0, QWORD PTR [80+rsp]
        movsd     QWORD PTR [80+rsp], xmm0
        mov       al, BYTE PTR [87+rsp]
        and       al, 127
        or        al, cl
        mov       BYTE PTR [87+rsp], al
        mov       rdx, QWORD PTR [80+rsp]
        mov       QWORD PTR [r8], rdx
        jmp       _B9_36

_B9_29::

        test      eax, eax
        jne       _B9_53

_B9_30::

        test      DWORD PTR [60+rsp], 1048575
        jne       _B9_53
        jmp       _B9_57

_B9_31::

        jne       _B9_53

_B9_33::

        test      dl, dl
        jne       _B9_35

_B9_34::

        shl       cl, 7
        mov       rax, QWORD PTR [_vmldAtanHATab+1976]
        mov       QWORD PTR [80+rsp], rax
        shr       rax, 56
        and       al, 127
        or        al, cl
        mov       BYTE PTR [87+rsp], al
        mov       rdx, QWORD PTR [80+rsp]
        mov       QWORD PTR [r8], rdx
        jmp       _B9_36

_B9_35::

        movsd     xmm0, QWORD PTR [_vmldAtanHATab+1936]
        shl       cl, 7
        addsd     xmm0, QWORD PTR [_vmldAtanHATab+1944]
        movsd     QWORD PTR [80+rsp], xmm0
        mov       al, BYTE PTR [87+rsp]
        and       al, 127
        or        al, cl
        mov       BYTE PTR [87+rsp], al
        mov       rdx, QWORD PTR [80+rsp]
        mov       QWORD PTR [r8], rdx

_B9_36::

        movups    xmm6, XMMWORD PTR [96+rsp]
        xor       eax, eax
        movups    xmm13, XMMWORD PTR [128+rsp]
        movups    xmm15, XMMWORD PTR [112+rsp]
        add       rsp, 152
        ret

_B9_37::

        movsd     xmm0, QWORD PTR [_vmldAtanHATab+1984]
        mov       eax, -1022
        mulsd     xmm1, xmm0
        movsd     QWORD PTR [56+rsp], xmm1
        jmp       _B9_18

_B9_38::

        cmp       eax, 2047
        je        _B9_49

_B9_39::

        test      DWORD PTR [68+rsp], 1048575
        jne       _B9_41

_B9_40::

        cmp       DWORD PTR [64+rsp], 0
        je        _B9_42

_B9_41::

        addsd     xmm0, xmm1
        movsd     QWORD PTR [r8], xmm0
        jmp       _B9_36

_B9_42::

        cmp       eax, 2047
        je        _B9_46

_B9_43::

        test      dl, dl
        je        _B9_34
        jmp       _B9_35

_B9_46::

        test      dl, dl
        jne       _B9_48

_B9_47::

        movsd     xmm0, QWORD PTR [_vmldAtanHATab+1904]
        shl       cl, 7
        addsd     xmm0, QWORD PTR [_vmldAtanHATab+1912]
        movsd     QWORD PTR [80+rsp], xmm0
        mov       al, BYTE PTR [87+rsp]
        and       al, 127
        or        al, cl
        mov       BYTE PTR [87+rsp], al
        mov       rdx, QWORD PTR [80+rsp]
        mov       QWORD PTR [r8], rdx
        jmp       _B9_36

_B9_48::

        movsd     xmm0, QWORD PTR [_vmldAtanHATab+1952]
        shl       cl, 7
        addsd     xmm0, QWORD PTR [_vmldAtanHATab+1960]
        movsd     QWORD PTR [80+rsp], xmm0
        mov       al, BYTE PTR [87+rsp]
        and       al, 127
        or        al, cl
        mov       BYTE PTR [87+rsp], al
        mov       rdx, QWORD PTR [80+rsp]
        mov       QWORD PTR [r8], rdx
        jmp       _B9_36

_B9_49::

        test      DWORD PTR [60+rsp], 1048575
        jne       _B9_41

_B9_50::

        cmp       DWORD PTR [56+rsp], 0
        jne       _B9_41

_B9_51::

        cmp       r11d, 2047
        je        _B9_39

_B9_53::

        movsd     xmm0, QWORD PTR [_vmldAtanHATab+1920]
        shl       cl, 7
        addsd     xmm0, QWORD PTR [_vmldAtanHATab+1928]
        movsd     QWORD PTR [80+rsp], xmm0
        mov       al, BYTE PTR [87+rsp]
        and       al, 127
        or        al, cl
        mov       BYTE PTR [87+rsp], al
        mov       rdx, QWORD PTR [80+rsp]
        mov       QWORD PTR [r8], rdx
        jmp       _B9_36

_B9_57::

        cmp       DWORD PTR [56+rsp], 0
        jne       _B9_53
        jmp       _B9_33
        ALIGN     16

_B9_54::

__svml_datan2_ha_cout_rare_internal ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_datan2_ha_cout_rare_internal_B1_B57:
	DD	551425
	DD	419946
	DD	579685
	DD	522332
	DD	1245451

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B9_1
	DD	imagerel _B9_54
	DD	imagerel _unwind___svml_datan2_ha_cout_rare_internal_B1_B57

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_RDATA	SEGMENT     READ PAGE   'DATA'
	ALIGN  32
	PUBLIC __svml_datan2_ha_data_internal
__svml_datan2_ha_data_internal	DD	0
	DD	1072693248
	DD	1413754136
	DD	1073291771
	DD	856972295
	DD	1016178214
	DD	0
	DD	0
	DD	0
	DD	1073217536
	DD	3531732635
	DD	1072657163
	DD	2062601149
	DD	1013974920
	DD	4294967295
	DD	4294967295
	DD	0
	DD	1072693248
	DD	1413754136
	DD	1072243195
	DD	856972295
	DD	1015129638
	DD	4294967295
	DD	4294967295
	DD	0
	DD	1071644672
	DD	90291023
	DD	1071492199
	DD	573531618
	DD	1014639487
	DD	4294967295
	DD	4294967295
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	4294967295
	DD	4294967295
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	0
	DD	90291023
	DD	1071492199
	DD	90291023
	DD	1071492199
	DD	90291023
	DD	1071492199
	DD	90291023
	DD	1071492199
	DD	90291023
	DD	1071492199
	DD	90291023
	DD	1071492199
	DD	90291023
	DD	1071492199
	DD	90291023
	DD	1071492199
	DD	573531618
	DD	1014639487
	DD	573531618
	DD	1014639487
	DD	573531618
	DD	1014639487
	DD	573531618
	DD	1014639487
	DD	573531618
	DD	1014639487
	DD	573531618
	DD	1014639487
	DD	573531618
	DD	1014639487
	DD	573531618
	DD	1014639487
	DD	1413754136
	DD	1072243195
	DD	1413754136
	DD	1072243195
	DD	1413754136
	DD	1072243195
	DD	1413754136
	DD	1072243195
	DD	1413754136
	DD	1072243195
	DD	1413754136
	DD	1072243195
	DD	1413754136
	DD	1072243195
	DD	1413754136
	DD	1072243195
	DD	856972295
	DD	1015129638
	DD	856972295
	DD	1015129638
	DD	856972295
	DD	1015129638
	DD	856972295
	DD	1015129638
	DD	856972295
	DD	1015129638
	DD	856972295
	DD	1015129638
	DD	856972295
	DD	1015129638
	DD	856972295
	DD	1015129638
	DD	3531732635
	DD	1072657163
	DD	3531732635
	DD	1072657163
	DD	3531732635
	DD	1072657163
	DD	3531732635
	DD	1072657163
	DD	3531732635
	DD	1072657163
	DD	3531732635
	DD	1072657163
	DD	3531732635
	DD	1072657163
	DD	3531732635
	DD	1072657163
	DD	2062601149
	DD	1013974920
	DD	2062601149
	DD	1013974920
	DD	2062601149
	DD	1013974920
	DD	2062601149
	DD	1013974920
	DD	2062601149
	DD	1013974920
	DD	2062601149
	DD	1013974920
	DD	2062601149
	DD	1013974920
	DD	2062601149
	DD	1013974920
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
	DD	1073217536
	DD	0
	DD	1073217536
	DD	0
	DD	1073217536
	DD	0
	DD	1073217536
	DD	0
	DD	1073217536
	DD	0
	DD	1073217536
	DD	0
	DD	1073217536
	DD	0
	DD	1073217536
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
	DD	0
	DD	1017226816
	DD	0
	DD	1017226816
	DD	0
	DD	1017226816
	DD	0
	DD	1017226816
	DD	0
	DD	1017226816
	DD	0
	DD	1017226816
	DD	0
	DD	1017226816
	DD	0
	DD	1017226816
	DD	4160749568
	DD	4294967295
	DD	4160749568
	DD	4294967295
	DD	4160749568
	DD	4294967295
	DD	4160749568
	DD	4294967295
	DD	4160749568
	DD	4294967295
	DD	4160749568
	DD	4294967295
	DD	4160749568
	DD	4294967295
	DD	4160749568
	DD	4294967295
	DD	0
	DD	1071382528
	DD	0
	DD	1071382528
	DD	0
	DD	1071382528
	DD	0
	DD	1071382528
	DD	0
	DD	1071382528
	DD	0
	DD	1071382528
	DD	0
	DD	1071382528
	DD	0
	DD	1071382528
	DD	0
	DD	1072889856
	DD	0
	DD	1072889856
	DD	0
	DD	1072889856
	DD	0
	DD	1072889856
	DD	0
	DD	1072889856
	DD	0
	DD	1072889856
	DD	0
	DD	1072889856
	DD	0
	DD	1072889856
	DD	0
	DD	1073971200
	DD	0
	DD	1073971200
	DD	0
	DD	1073971200
	DD	0
	DD	1073971200
	DD	0
	DD	1073971200
	DD	0
	DD	1073971200
	DD	0
	DD	1073971200
	DD	0
	DD	1073971200
	DD	0
	DD	1072037888
	DD	0
	DD	1072037888
	DD	0
	DD	1072037888
	DD	0
	DD	1072037888
	DD	0
	DD	1072037888
	DD	0
	DD	1072037888
	DD	0
	DD	1072037888
	DD	0
	DD	1072037888
	DD	4
	DD	4
	DD	4
	DD	4
	DD	4
	DD	4
	DD	4
	DD	4
	DD	4
	DD	4
	DD	4
	DD	4
	DD	4
	DD	4
	DD	4
	DD	4
	DD	4293918720
	DD	4293918720
	DD	4293918720
	DD	4293918720
	DD	4293918720
	DD	4293918720
	DD	4293918720
	DD	4293918720
	DD	4293918720
	DD	4293918720
	DD	4293918720
	DD	4293918720
	DD	4293918720
	DD	4293918720
	DD	4293918720
	DD	4293918720
	DD	2145386496
	DD	2145386496
	DD	2145386496
	DD	2145386496
	DD	2145386496
	DD	2145386496
	DD	2145386496
	DD	2145386496
	DD	2145386496
	DD	2145386496
	DD	2145386496
	DD	2145386496
	DD	2145386496
	DD	2145386496
	DD	2145386496
	DD	2145386496
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
	DD	133169152
	DD	133169152
	DD	133169152
	DD	133169152
	DD	133169152
	DD	133169152
	DD	133169152
	DD	133169152
	DD	133169152
	DD	133169152
	DD	133169152
	DD	133169152
	DD	133169152
	DD	133169152
	DD	133169152
	DD	133169152
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
	DD	3866310424
	DD	1066132731
	DD	3866310424
	DD	1066132731
	DD	3866310424
	DD	1066132731
	DD	3866310424
	DD	1066132731
	DD	3866310424
	DD	1066132731
	DD	3866310424
	DD	1066132731
	DD	3866310424
	DD	1066132731
	DD	3866310424
	DD	1066132731
	DD	529668190
	DD	3214953687
	DD	529668190
	DD	3214953687
	DD	529668190
	DD	3214953687
	DD	529668190
	DD	3214953687
	DD	529668190
	DD	3214953687
	DD	529668190
	DD	3214953687
	DD	529668190
	DD	3214953687
	DD	529668190
	DD	3214953687
	DD	1493047753
	DD	1067887957
	DD	1493047753
	DD	1067887957
	DD	1493047753
	DD	1067887957
	DD	1493047753
	DD	1067887957
	DD	1493047753
	DD	1067887957
	DD	1493047753
	DD	1067887957
	DD	1493047753
	DD	1067887957
	DD	1493047753
	DD	1067887957
	DD	1554070819
	DD	3215629941
	DD	1554070819
	DD	3215629941
	DD	1554070819
	DD	3215629941
	DD	1554070819
	DD	3215629941
	DD	1554070819
	DD	3215629941
	DD	1554070819
	DD	3215629941
	DD	1554070819
	DD	3215629941
	DD	1554070819
	DD	3215629941
	DD	3992437651
	DD	1068372721
	DD	3992437651
	DD	1068372721
	DD	3992437651
	DD	1068372721
	DD	3992437651
	DD	1068372721
	DD	3992437651
	DD	1068372721
	DD	3992437651
	DD	1068372721
	DD	3992437651
	DD	1068372721
	DD	3992437651
	DD	1068372721
	DD	845965549
	DD	3216052365
	DD	845965549
	DD	3216052365
	DD	845965549
	DD	3216052365
	DD	845965549
	DD	3216052365
	DD	845965549
	DD	3216052365
	DD	845965549
	DD	3216052365
	DD	845965549
	DD	3216052365
	DD	845965549
	DD	3216052365
	DD	3073500986
	DD	1068740914
	DD	3073500986
	DD	1068740914
	DD	3073500986
	DD	1068740914
	DD	3073500986
	DD	1068740914
	DD	3073500986
	DD	1068740914
	DD	3073500986
	DD	1068740914
	DD	3073500986
	DD	1068740914
	DD	3073500986
	DD	1068740914
	DD	426211919
	DD	3216459217
	DD	426211919
	DD	3216459217
	DD	426211919
	DD	3216459217
	DD	426211919
	DD	3216459217
	DD	426211919
	DD	3216459217
	DD	426211919
	DD	3216459217
	DD	426211919
	DD	3216459217
	DD	426211919
	DD	3216459217
	DD	435789718
	DD	1069314503
	DD	435789718
	DD	1069314503
	DD	435789718
	DD	1069314503
	DD	435789718
	DD	1069314503
	DD	435789718
	DD	1069314503
	DD	435789718
	DD	1069314503
	DD	435789718
	DD	1069314503
	DD	435789718
	DD	1069314503
	DD	2453936673
	DD	3217180964
	DD	2453936673
	DD	3217180964
	DD	2453936673
	DD	3217180964
	DD	2453936673
	DD	3217180964
	DD	2453936673
	DD	3217180964
	DD	2453936673
	DD	3217180964
	DD	2453936673
	DD	3217180964
	DD	2453936673
	DD	3217180964
	DD	2576977731
	DD	1070176665
	DD	2576977731
	DD	1070176665
	DD	2576977731
	DD	1070176665
	DD	2576977731
	DD	1070176665
	DD	2576977731
	DD	1070176665
	DD	2576977731
	DD	1070176665
	DD	2576977731
	DD	1070176665
	DD	2576977731
	DD	1070176665
	DD	1431655762
	DD	3218429269
	DD	1431655762
	DD	3218429269
	DD	1431655762
	DD	3218429269
	DD	1431655762
	DD	3218429269
	DD	1431655762
	DD	3218429269
	DD	1431655762
	DD	3218429269
	DD	1431655762
	DD	3218429269
	DD	1431655762
	DD	3218429269
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
	DD	2150629376
	DD	2150629376
	DD	2150629376
	DD	2150629376
	DD	2150629376
	DD	2150629376
	DD	2150629376
	DD	2150629376
	DD	2150629376
	DD	2150629376
	DD	2150629376
	DD	2150629376
	DD	2150629376
	DD	2150629376
	DD	2150629376
	DD	2150629376
	DD	4258267136
	DD	4258267136
	DD	4258267136
	DD	4258267136
	DD	4258267136
	DD	4258267136
	DD	4258267136
	DD	4258267136
	DD	4258267136
	DD	4258267136
	DD	4258267136
	DD	4258267136
	DD	4258267136
	DD	4258267136
	DD	4258267136
	DD	4258267136
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
	DD	0
	DD	4294967295
	DD	0
	DD	4294967295
	DD	0
	DD	4294967295
	DD	0
	DD	4294967295
	DD	0
	DD	4294967295
	DD	0
	DD	4294967295
	DD	0
	DD	4294967295
	DD	0
	DD	4294967295
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
	DD	1005584384
	DD	1005584384
	DD	1005584384
	DD	1005584384
	DD	1005584384
	DD	1005584384
	DD	1005584384
	DD	1005584384
	DD	1005584384
	DD	1005584384
	DD	1005584384
	DD	1005584384
	DD	1005584384
	DD	1005584384
	DD	1005584384
	DD	1005584384
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
