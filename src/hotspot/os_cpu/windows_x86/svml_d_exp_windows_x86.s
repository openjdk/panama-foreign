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
	PUBLIC __svml_exp1_ha_e9

__svml_exp1_ha_e9	PROC

_B1_1::

        DB        243
        DB        15
        DB        30
        DB        250
L1::

        sub       rsp, 184
        mov       eax, 2147483647
        mov       QWORD PTR [168+rsp], r13
        mov       edx, 1082532650
        vmovupd   xmm3, XMMWORD PTR [rcx]
        lea       r9, QWORD PTR [__ImageBase]
        vmulsd    xmm1, xmm3, QWORD PTR [__svml_dexp_ha_data_internal+1024]
        lea       r13, QWORD PTR [95+rsp]
        vmovsd    xmm2, QWORD PTR [__svml_dexp_ha_data_internal+1088]
        vmovd     xmm5, eax
        vmovsd    xmm0, QWORD PTR [__svml_dexp_ha_data_internal+1536]
        and       r13, -64
        mov       QWORD PTR [176+rsp], r13
        vaddsd    xmm1, xmm1, xmm2
        vsubsd    xmm4, xmm1, xmm2
        vpshufd   xmm2, xmm3, 85
        vpand     xmm2, xmm2, xmm5
        vmovd     xmm5, edx
        vpcmpgtd  xmm2, xmm2, xmm5
        vmovmskps eax, xmm2
        vmovq     xmm2, QWORD PTR [__svml_dexp_ha_data_internal+1600]
        vpand     xmm5, xmm1, xmm2
        vpandn    xmm1, xmm2, xmm1
        vpshufd   xmm5, xmm5, 0
        vpsllq    xmm2, xmm1, 46
        vpslld    xmm5, xmm5, 4
        vmovd     r8d, xmm5
        vmulsd    xmm5, xmm4, QWORD PTR [__svml_dexp_ha_data_internal+1216]
        vmulsd    xmm4, xmm4, QWORD PTR [__svml_dexp_ha_data_internal+1280]
        vsubsd    xmm5, xmm3, xmm5
        movsxd    r8, r8d
        vsubsd    xmm5, xmm5, xmm4
        vmulsd    xmm0, xmm0, xmm5
        vaddsd    xmm4, xmm0, QWORD PTR [__svml_dexp_ha_data_internal+1472]
        vmulsd    xmm0, xmm4, xmm5
        vmulsd    xmm4, xmm5, xmm5
        vaddsd    xmm0, xmm0, QWORD PTR [__svml_dexp_ha_data_internal+1408]
        vmulsd    xmm0, xmm0, xmm5
        vaddsd    xmm0, xmm0, QWORD PTR [__svml_dexp_ha_data_internal+1344]
        vmulsd    xmm4, xmm0, xmm4
        vaddsd    xmm5, xmm4, xmm5
        vmulsd    xmm0, xmm5, QWORD PTR [imagerel(__svml_dexp_ha_data_internal)+r9+r8]
        vaddsd    xmm4, xmm0, QWORD PTR [imagerel(__svml_dexp_ha_data_internal)+8+r9+r8]
        vaddsd    xmm5, xmm4, QWORD PTR [imagerel(__svml_dexp_ha_data_internal)+r9+r8]
        vpaddq    xmm0, xmm5, xmm2
        test      al, 1
        jne       _B1_3

_B1_2::

        mov       r13, QWORD PTR [168+rsp]
        add       rsp, 184
        ret

_B1_3::

        vcmpgtsd  xmm1, xmm3, QWORD PTR [__svml_dexp_ha_data_internal+1984]
        vcmpltsd  xmm2, xmm3, QWORD PTR [__svml_dexp_ha_data_internal+2048]
        vblendvpd xmm0, xmm0, XMMWORD PTR [_2il0floatpacket_22], xmm1
        vorpd     xmm4, xmm1, xmm2
        vmovmskpd edx, xmm4
        vandnpd   xmm0, xmm2, xmm0
        not       edx
        and       edx, eax
        and       edx, 1
        je        _B1_2

_B1_4::

        vmovsd    QWORD PTR [r13], xmm3
        vmovsd    QWORD PTR [64+r13], xmm0
        jne       _B1_7

_B1_5::

        vmovsd    xmm0, QWORD PTR [64+r13]
        jmp       _B1_2

_B1_7::

        lea       rcx, QWORD PTR [r13]
        lea       rdx, QWORD PTR [64+r13]

        call      __svml_dexp_ha_cout_rare_internal
        jmp       _B1_5
        ALIGN     16

_B1_8::

__svml_exp1_ha_e9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_exp1_ha_e9_B1_B7:
	DD	268289
	DD	1430552
	DD	1507595

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B1_1
	DD	imagerel _B1_8
	DD	imagerel _unwind___svml_exp1_ha_e9_B1_B7

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST1:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_exp1_ha_l9

__svml_exp1_ha_l9	PROC

_B2_1::

        DB        243
        DB        15
        DB        30
        DB        250
L4::

        sub       rsp, 200
        lea       rdx, QWORD PTR [__ImageBase]
        vmovups   XMMWORD PTR [176+rsp], xmm8
        mov       QWORD PTR [168+rsp], r13
        lea       r13, QWORD PTR [95+rsp]
        vmovupd   xmm4, XMMWORD PTR [rcx]
        and       r13, -64
        vmovsd    xmm2, QWORD PTR [__svml_dexp_ha_data_internal+1088]
        vmovapd   xmm1, xmm4
        vfmadd132sd xmm1, xmm2, QWORD PTR [__svml_dexp_ha_data_internal+1024]
        vmovsd    xmm0, QWORD PTR [__svml_dexp_ha_data_internal+1728]
        vandpd    xmm8, xmm4, xmm0
        vmovsd    xmm5, QWORD PTR [__svml_dexp_ha_data_internal+1536]
        mov       QWORD PTR [192+rsp], r13
        vsubsd    xmm3, xmm1, xmm2
        vcmpnlesd xmm1, xmm8, QWORD PTR [__svml_dexp_ha_data_internal+1792]
        vmovapd   xmm8, xmm3
        vfnmadd132sd xmm8, xmm4, QWORD PTR [__svml_dexp_ha_data_internal+1216]
        vmovq     xmm2, QWORD PTR [__svml_dexp_ha_data_internal+1664]
        vmovmskpd ecx, xmm1
        vaddsd    xmm1, xmm3, QWORD PTR [__svml_dexp_ha_data_internal+1152]
        vfnmadd132sd xmm3, xmm8, QWORD PTR [__svml_dexp_ha_data_internal+1280]
        vpand     xmm0, xmm1, xmm2
        vmovd     eax, xmm0
        vfmadd213sd xmm5, xmm3, QWORD PTR [__svml_dexp_ha_data_internal+1472]
        vmulsd    xmm8, xmm3, xmm3
        vfmadd213sd xmm5, xmm3, QWORD PTR [__svml_dexp_ha_data_internal+1408]
        vfmadd213sd xmm5, xmm3, QWORD PTR [__svml_dexp_ha_data_internal+1344]
        movsxd    rax, eax
        vfmadd213sd xmm5, xmm8, xmm3
        vpandn    xmm3, xmm2, xmm1
        vmovddup  xmm0, QWORD PTR [imagerel(__svml_dexp_ha_data_internal)+8+rdx+rax]
        vfmadd132sd xmm5, xmm0, QWORD PTR [imagerel(__svml_dexp_ha_data_internal)+rdx+rax]
        vpsllq    xmm0, xmm3, 42
        vaddsd    xmm5, xmm5, QWORD PTR [imagerel(__svml_dexp_ha_data_internal)+rdx+rax]
        vpaddq    xmm0, xmm5, xmm0
        test      cl, 1
        jne       _B2_3

_B2_2::

        vmovups   xmm8, XMMWORD PTR [176+rsp]
        mov       r13, QWORD PTR [168+rsp]
        add       rsp, 200
        ret

_B2_3::

        vcmpgtsd  xmm1, xmm4, QWORD PTR [__svml_dexp_ha_data_internal+1984]
        vcmpltsd  xmm2, xmm4, QWORD PTR [__svml_dexp_ha_data_internal+2048]
        vblendvpd xmm0, xmm0, XMMWORD PTR [_2il0floatpacket_22], xmm1
        vorpd     xmm3, xmm1, xmm2
        vmovmskpd eax, xmm3
        vandnpd   xmm0, xmm2, xmm0
        andn      eax, eax, ecx
        and       eax, 1
        je        _B2_2

_B2_4::

        vmovsd    QWORD PTR [r13], xmm4
        vmovsd    QWORD PTR [64+r13], xmm0
        jne       _B2_7

_B2_5::

        vmovsd    xmm0, QWORD PTR [64+r13]
        jmp       _B2_2

_B2_7::

        lea       rcx, QWORD PTR [r13]
        lea       rdx, QWORD PTR [64+r13]

        call      __svml_dexp_ha_cout_rare_internal
        jmp       _B2_5
        ALIGN     16

_B2_8::

__svml_exp1_ha_l9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_exp1_ha_l9_B1_B7:
	DD	402177
	DD	1430563
	DD	755739
	DD	1638667

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B2_1
	DD	imagerel _B2_8
	DD	imagerel _unwind___svml_exp1_ha_l9_B1_B7

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST2:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_exp2_ha_ex

__svml_exp2_ha_ex	PROC

_B3_1::

        DB        243
        DB        15
        DB        30
        DB        250
L9::

        sub       rsp, 280
        lea       r8, QWORD PTR [__ImageBase]
        movups    XMMWORD PTR [208+rsp], xmm15
        movups    XMMWORD PTR [224+rsp], xmm14
        movups    XMMWORD PTR [240+rsp], xmm13
        movups    XMMWORD PTR [192+rsp], xmm6
        mov       QWORD PTR [256+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        movups    xmm4, XMMWORD PTR [rcx]
        and       r13, -64
        movups    xmm14, XMMWORD PTR [__svml_dexp_ha_data_internal+1024]
        mulpd     xmm14, xmm4
        movups    xmm0, XMMWORD PTR [__svml_dexp_ha_data_internal+1088]
        addpd     xmm14, xmm0
        movq      xmm3, QWORD PTR [__svml_dexp_ha_data_internal+1856]
        movaps    xmm15, xmm14
        pshufd    xmm1, xmm4, 221
        movq      xmm2, QWORD PTR [__svml_dexp_ha_data_internal+1920]
        pand      xmm1, xmm3
        movdqu    xmm3, XMMWORD PTR [__svml_dexp_ha_data_internal+1600]
        pcmpgtd   xmm1, xmm2
        movdqa    xmm2, xmm3
        pandn     xmm3, xmm14
        subpd     xmm15, xmm0
        movmskps  ecx, xmm1
        pand      xmm2, xmm14
        psllq     xmm3, 46
        pshufd    xmm0, xmm2, 136
        pslld     xmm0, 4
        movd      eax, xmm0
        pshufd    xmm0, xmm0, 1
        movups    xmm5, XMMWORD PTR [__svml_dexp_ha_data_internal+1216]
        movd      edx, xmm0
        movups    xmm6, XMMWORD PTR [__svml_dexp_ha_data_internal+1280]
        movups    xmm13, XMMWORD PTR [__svml_dexp_ha_data_internal+1536]
        mulpd     xmm5, xmm15
        mulpd     xmm6, xmm15
        movsxd    rax, eax
        movsxd    rdx, edx
        mov       QWORD PTR [264+rsp], r13
        movups    xmm2, XMMWORD PTR [imagerel(__svml_dexp_ha_data_internal)+r8+rax]
        movups    xmm1, XMMWORD PTR [imagerel(__svml_dexp_ha_data_internal)+r8+rdx]
        movaps    xmm0, xmm2
        unpcklpd  xmm0, xmm1
        unpckhpd  xmm2, xmm1
        movaps    xmm1, xmm4
        subpd     xmm1, xmm5
        subpd     xmm1, xmm6
        mulpd     xmm13, xmm1
        addpd     xmm13, XMMWORD PTR [__svml_dexp_ha_data_internal+1472]
        mulpd     xmm13, xmm1
        addpd     xmm13, XMMWORD PTR [__svml_dexp_ha_data_internal+1408]
        mulpd     xmm13, xmm1
        movaps    xmm5, xmm1
        mulpd     xmm5, xmm1
        addpd     xmm13, XMMWORD PTR [__svml_dexp_ha_data_internal+1344]
        mulpd     xmm5, xmm13
        addpd     xmm1, xmm5
        mulpd     xmm1, xmm0
        addpd     xmm2, xmm1
        addpd     xmm0, xmm2
        paddq     xmm0, xmm3
        test      cl, 3
        jne       _B3_3

_B3_2::

        movups    xmm6, XMMWORD PTR [192+rsp]
        movups    xmm13, XMMWORD PTR [240+rsp]
        movups    xmm14, XMMWORD PTR [224+rsp]
        movups    xmm15, XMMWORD PTR [208+rsp]
        mov       r13, QWORD PTR [256+rsp]
        add       rsp, 280
        ret

_B3_3::

        movups    xmm3, XMMWORD PTR [__svml_dexp_ha_data_internal+1984]
        movaps    xmm2, xmm4
        cmpltpd   xmm3, xmm4
        cmpltpd   xmm2, XMMWORD PTR [__svml_dexp_ha_data_internal+2048]
        movaps    xmm1, xmm3
        andnps    xmm1, xmm0
        movups    xmm0, XMMWORD PTR [_2il0floatpacket_22]
        andps     xmm0, xmm3
        orps      xmm3, xmm2
        movmskpd  eax, xmm3
        orps      xmm1, xmm0
        movaps    xmm0, xmm2
        andnps    xmm0, xmm1
        not       eax
        and       eax, ecx
        and       eax, 3
        je        _B3_2

_B3_4::

        movups    XMMWORD PTR [r13], xmm4
        movups    XMMWORD PTR [64+r13], xmm0
        je        _B3_2

_B3_7::

        xor       ecx, ecx
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, ecx
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, eax

_B3_8::

        mov       ecx, ebx
        mov       edx, 1
        shl       edx, cl
        test      esi, edx
        jne       _B3_11

_B3_9::

        inc       ebx
        cmp       ebx, 2
        jl        _B3_8

_B3_10::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        movups    xmm0, XMMWORD PTR [64+r13]
        jmp       _B3_2

_B3_11::

        lea       rcx, QWORD PTR [r13+rbx*8]
        lea       rdx, QWORD PTR [64+r13+rbx*8]

        call      __svml_dexp_ha_cout_rare_internal
        jmp       _B3_9
        ALIGN     16

_B3_12::

__svml_exp2_ha_ex ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_exp2_ha_ex_B1_B4:
	DD	802049
	DD	2151485
	DD	813109
	DD	1038381
	DD	976932
	DD	915483
	DD	2294027

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B3_1
	DD	imagerel _B3_7
	DD	imagerel _unwind___svml_exp2_ha_ex_B1_B4

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_exp2_ha_ex_B7_B11:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B3_1
	DD	imagerel _B3_7
	DD	imagerel _unwind___svml_exp2_ha_ex_B1_B4

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B3_7
	DD	imagerel _B3_12
	DD	imagerel _unwind___svml_exp2_ha_ex_B7_B11

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST3:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_exp4_ha_e9

__svml_exp4_ha_e9	PROC

_B4_1::

        DB        243
        DB        15
        DB        30
        DB        250
L24::

        sub       rsp, 280
        lea       rax, QWORD PTR [__ImageBase]
        vmovups   XMMWORD PTR [240+rsp], xmm15
        vmovups   XMMWORD PTR [224+rsp], xmm9
        vmovups   XMMWORD PTR [208+rsp], xmm8
        mov       QWORD PTR [256+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovupd   ymm5, YMMWORD PTR [rcx]
        and       r13, -64
        vmovupd   ymm4, YMMWORD PTR [__svml_dexp_ha_data_internal+1088]
        vmulpd    ymm2, ymm5, YMMWORD PTR [__svml_dexp_ha_data_internal+1024]
        vaddpd    ymm3, ymm4, ymm2
        vsubpd    ymm0, ymm3, ymm4
        mov       QWORD PTR [264+rsp], r13
        vextractf128 xmm8, ymm5, 1
        vshufps   xmm2, xmm5, xmm8, 221
        vandps    xmm4, xmm2, XMMWORD PTR [__svml_dexp_ha_data_internal+1856]
        vpcmpgtd  xmm1, xmm4, XMMWORD PTR [__svml_dexp_ha_data_internal+1920]
        vmovupd   xmm4, XMMWORD PTR [__svml_dexp_ha_data_internal+1600]
        vmovmskps edx, xmm1
        vandps    xmm15, xmm3, xmm4
        vextractf128 xmm2, ymm3, 1
        vpandn    xmm3, xmm4, xmm3
        vandps    xmm9, xmm2, xmm4
        vpandn    xmm2, xmm4, xmm2
        vshufps   xmm8, xmm15, xmm9, 136
        vpsllq    xmm4, xmm2, 46
        vpslld    xmm15, xmm8, 4
        vmovd     r8d, xmm15
        vpextrd   r9d, xmm15, 1
        movsxd    r8, r8d
        movsxd    r9, r9d
        vpextrd   r10d, xmm15, 2
        vpextrd   r11d, xmm15, 3
        movsxd    r10, r10d
        movsxd    r11, r11d
        vmovupd   xmm9, XMMWORD PTR [imagerel(__svml_dexp_ha_data_internal)+rax+r8]
        vmovupd   xmm1, XMMWORD PTR [imagerel(__svml_dexp_ha_data_internal)+rax+r9]
        vinsertf128 ymm15, ymm9, XMMWORD PTR [imagerel(__svml_dexp_ha_data_internal)+rax+r10], 1
        vinsertf128 ymm8, ymm1, XMMWORD PTR [imagerel(__svml_dexp_ha_data_internal)+rax+r11], 1
        vunpcklpd ymm1, ymm15, ymm8
        vunpckhpd ymm8, ymm15, ymm8
        vmulpd    ymm15, ymm0, YMMWORD PTR [__svml_dexp_ha_data_internal+1216]
        vmulpd    ymm0, ymm0, YMMWORD PTR [__svml_dexp_ha_data_internal+1280]
        vsubpd    ymm15, ymm5, ymm15
        vsubpd    ymm15, ymm15, ymm0
        vmulpd    ymm9, ymm15, YMMWORD PTR [__svml_dexp_ha_data_internal+1536]
        vaddpd    ymm0, ymm9, YMMWORD PTR [__svml_dexp_ha_data_internal+1472]
        vmulpd    ymm9, ymm15, ymm0
        vaddpd    ymm0, ymm9, YMMWORD PTR [__svml_dexp_ha_data_internal+1408]
        vmulpd    ymm9, ymm15, ymm15
        vmulpd    ymm0, ymm15, ymm0
        vaddpd    ymm0, ymm0, YMMWORD PTR [__svml_dexp_ha_data_internal+1344]
        vmulpd    ymm9, ymm9, ymm0
        vaddpd    ymm15, ymm15, ymm9
        vmulpd    ymm0, ymm1, ymm15
        vaddpd    ymm8, ymm8, ymm0
        vpsllq    xmm0, xmm3, 46
        vaddpd    ymm1, ymm1, ymm8
        vextractf128 xmm8, ymm1, 1
        vpaddq    xmm3, xmm1, xmm0
        vpaddq    xmm0, xmm8, xmm4
        vinsertf128 ymm0, ymm3, xmm0, 1
        test      edx, edx
        jne       _B4_3

_B4_2::

        vmovups   xmm8, XMMWORD PTR [208+rsp]
        vmovups   xmm9, XMMWORD PTR [224+rsp]
        vmovups   xmm15, XMMWORD PTR [240+rsp]
        mov       r13, QWORD PTR [256+rsp]
        add       rsp, 280
        ret

_B4_3::

        vcmpgt_oqpd ymm1, ymm5, YMMWORD PTR [__svml_dexp_ha_data_internal+1984]
        vcmplt_oqpd ymm2, ymm5, YMMWORD PTR [__svml_dexp_ha_data_internal+2048]
        vblendvpd ymm0, ymm0, YMMWORD PTR [_2il0floatpacket_23], ymm1
        vorpd     ymm3, ymm1, ymm2
        vandnpd   ymm0, ymm2, ymm0
        vextractf128 xmm4, ymm3, 1
        vshufps   xmm8, xmm3, xmm4, 221
        vmovmskps ecx, xmm8
        not       ecx
        and       ecx, edx
        je        _B4_2

_B4_4::

        vmovupd   YMMWORD PTR [r13], ymm5
        vmovupd   YMMWORD PTR [64+r13], ymm0
        je        _B4_2

_B4_7::

        xor       eax, eax
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, eax
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, ecx

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
        vmovupd   ymm0, YMMWORD PTR [64+r13]
        jmp       _B4_2

_B4_11::

        vzeroupper
        lea       rcx, QWORD PTR [r13+rbx*8]
        lea       rdx, QWORD PTR [64+r13+rbx*8]

        call      __svml_dexp_ha_cout_rare_internal
        jmp       _B4_9
        ALIGN     16

_B4_12::

__svml_exp4_ha_e9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_exp4_ha_e9_B1_B4:
	DD	668929
	DD	2151477
	DD	886829
	DD	956452
	DD	1046555
	DD	2294027

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B4_1
	DD	imagerel _B4_7
	DD	imagerel _unwind___svml_exp4_ha_e9_B1_B4

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_exp4_ha_e9_B7_B11:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B4_1
	DD	imagerel _B4_7
	DD	imagerel _unwind___svml_exp4_ha_e9_B1_B4

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B4_7
	DD	imagerel _B4_12
	DD	imagerel _unwind___svml_exp4_ha_e9_B7_B11

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST4:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_exp2_ha_l9

__svml_exp2_ha_l9	PROC

_B5_1::

        DB        243
        DB        15
        DB        30
        DB        250
L37::

        sub       rsp, 264
        lea       r8, QWORD PTR [__ImageBase]
        vmovups   XMMWORD PTR [192+rsp], xmm9
        vmovups   XMMWORD PTR [208+rsp], xmm8
        vmovups   XMMWORD PTR [224+rsp], xmm7
        mov       QWORD PTR [240+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovupd   xmm5, XMMWORD PTR [rcx]
        and       r13, -64
        vmovupd   xmm2, XMMWORD PTR [__svml_dexp_ha_data_internal+1024]
        vmovupd   xmm3, XMMWORD PTR [__svml_dexp_ha_data_internal+1088]
        vfmadd213pd xmm2, xmm5, xmm3
        vmovupd   xmm7, XMMWORD PTR [__svml_dexp_ha_data_internal+1216]
        vmovupd   xmm8, XMMWORD PTR [__svml_dexp_ha_data_internal+1536]
        vandpd    xmm0, xmm5, XMMWORD PTR [__svml_dexp_ha_data_internal+1728]
        vcmpnlepd xmm1, xmm0, XMMWORD PTR [__svml_dexp_ha_data_internal+1792]
        vsubpd    xmm4, xmm2, xmm3
        vmovmskpd ecx, xmm1
        vmovdqu   xmm3, XMMWORD PTR [__svml_dexp_ha_data_internal+1664]
        vfnmadd213pd xmm7, xmm4, xmm5
        vaddpd    xmm2, xmm4, XMMWORD PTR [__svml_dexp_ha_data_internal+1152]
        vfnmadd132pd xmm4, xmm7, XMMWORD PTR [__svml_dexp_ha_data_internal+1280]
        vpand     xmm9, xmm2, xmm3
        vmovd     eax, xmm9
        vfmadd213pd xmm8, xmm4, XMMWORD PTR [__svml_dexp_ha_data_internal+1472]
        vmulpd    xmm7, xmm4, xmm4
        vfmadd213pd xmm8, xmm4, XMMWORD PTR [__svml_dexp_ha_data_internal+1408]
        vfmadd213pd xmm8, xmm4, XMMWORD PTR [__svml_dexp_ha_data_internal+1344]
        vpextrd   edx, xmm9, 2
        movsxd    rax, eax
        movsxd    rdx, edx
        vfmadd213pd xmm8, xmm7, xmm4
        vpandn    xmm4, xmm3, xmm2
        vmovupd   xmm1, XMMWORD PTR [imagerel(__svml_dexp_ha_data_internal)+r8+rax]
        vmovupd   xmm9, XMMWORD PTR [imagerel(__svml_dexp_ha_data_internal)+r8+rdx]
        vunpcklpd xmm0, xmm1, xmm9
        vunpckhpd xmm1, xmm1, xmm9
        vfmadd213pd xmm8, xmm0, xmm1
        vpsllq    xmm1, xmm4, 42
        vaddpd    xmm0, xmm0, xmm8
        mov       QWORD PTR [248+rsp], r13
        vpaddq    xmm0, xmm0, xmm1
        test      ecx, ecx
        jne       _B5_3

_B5_2::

        vmovups   xmm7, XMMWORD PTR [224+rsp]
        vmovups   xmm8, XMMWORD PTR [208+rsp]
        vmovups   xmm9, XMMWORD PTR [192+rsp]
        mov       r13, QWORD PTR [240+rsp]
        add       rsp, 264
        ret

_B5_3::

        vcmpgtpd  xmm1, xmm5, XMMWORD PTR [__svml_dexp_ha_data_internal+1984]
        vcmpltpd  xmm2, xmm5, XMMWORD PTR [__svml_dexp_ha_data_internal+2048]
        vblendvpd xmm0, xmm0, XMMWORD PTR [_2il0floatpacket_22], xmm1
        vorpd     xmm3, xmm1, xmm2
        vmovmskpd eax, xmm3
        vandnpd   xmm0, xmm2, xmm0
        andn      edx, eax, ecx
        je        _B5_2

_B5_4::

        vmovupd   XMMWORD PTR [r13], xmm5
        vmovupd   XMMWORD PTR [64+r13], xmm0
        je        _B5_2

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
        cmp       ebx, 2
        jl        _B5_8

_B5_10::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        vmovupd   xmm0, XMMWORD PTR [64+r13]
        jmp       _B5_2

_B5_11::

        lea       rcx, QWORD PTR [r13+rbx*8]
        lea       rdx, QWORD PTR [64+r13+rbx*8]

        call      __svml_dexp_ha_cout_rare_internal
        jmp       _B5_9
        ALIGN     16

_B5_12::

__svml_exp2_ha_l9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_exp2_ha_l9_B1_B4:
	DD	668929
	DD	2020405
	DD	948269
	DD	886820
	DD	825371
	DD	2162955

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B5_1
	DD	imagerel _B5_7
	DD	imagerel _unwind___svml_exp2_ha_l9_B1_B4

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_exp2_ha_l9_B7_B11:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B5_1
	DD	imagerel _B5_7
	DD	imagerel _unwind___svml_exp2_ha_l9_B1_B4

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B5_7
	DD	imagerel _B5_12
	DD	imagerel _unwind___svml_exp2_ha_l9_B7_B11

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST5:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_exp1_ha_ex

__svml_exp1_ha_ex	PROC

_B6_1::

        DB        243
        DB        15
        DB        30
        DB        250
L50::

        sub       rsp, 200
        mov       eax, 2147483647
        movups    XMMWORD PTR [176+rsp], xmm13
        mov       edx, 1082532650
        mov       QWORD PTR [168+rsp], r13
        lea       r9, QWORD PTR [__ImageBase]
        movups    xmm5, XMMWORD PTR [rcx]
        lea       r13, QWORD PTR [95+rsp]
        movaps    xmm3, xmm5
        movd      xmm1, eax
        mulsd     xmm3, QWORD PTR [__svml_dexp_ha_data_internal+1024]
        movd      xmm13, edx
        movsd     xmm2, QWORD PTR [__svml_dexp_ha_data_internal+1088]
        and       r13, -64
        movsd     xmm0, QWORD PTR [__svml_dexp_ha_data_internal+1536]
        addsd     xmm3, xmm2
        movaps    xmm4, xmm3
        mov       QWORD PTR [192+rsp], r13
        subsd     xmm4, xmm2
        pshufd    xmm2, xmm5, 85
        pand      xmm2, xmm1
        pcmpgtd   xmm2, xmm13
        movmskps  eax, xmm2
        movq      xmm2, QWORD PTR [__svml_dexp_ha_data_internal+1600]
        movdqa    xmm1, xmm2
        pandn     xmm2, xmm3
        pand      xmm1, xmm3
        psllq     xmm2, 46
        pshufd    xmm13, xmm1, 0
        movaps    xmm1, xmm5
        pslld     xmm13, 4
        movd      r8d, xmm13
        movaps    xmm13, xmm4
        mulsd     xmm13, QWORD PTR [__svml_dexp_ha_data_internal+1216]
        mulsd     xmm4, QWORD PTR [__svml_dexp_ha_data_internal+1280]
        subsd     xmm1, xmm13
        movsxd    r8, r8d
        subsd     xmm1, xmm4
        mulsd     xmm0, xmm1
        movaps    xmm4, xmm1
        mulsd     xmm4, xmm1
        addsd     xmm0, QWORD PTR [__svml_dexp_ha_data_internal+1472]
        mulsd     xmm0, xmm1
        addsd     xmm0, QWORD PTR [__svml_dexp_ha_data_internal+1408]
        mulsd     xmm0, xmm1
        addsd     xmm0, QWORD PTR [__svml_dexp_ha_data_internal+1344]
        mulsd     xmm0, xmm4
        addsd     xmm0, xmm1
        mulsd     xmm0, QWORD PTR [imagerel(__svml_dexp_ha_data_internal)+r9+r8]
        addsd     xmm0, QWORD PTR [imagerel(__svml_dexp_ha_data_internal)+8+r9+r8]
        addsd     xmm0, QWORD PTR [imagerel(__svml_dexp_ha_data_internal)+r9+r8]
        paddq     xmm0, xmm2
        test      al, 1
        jne       _B6_3

_B6_2::

        movups    xmm13, XMMWORD PTR [176+rsp]
        mov       r13, QWORD PTR [168+rsp]
        add       rsp, 200
        ret

_B6_3::

        movsd     xmm1, QWORD PTR [__svml_dexp_ha_data_internal+1984]
        movaps    xmm3, xmm5
        movaps    xmm4, xmm5
        cmpltsd   xmm1, xmm5
        cmpltsd   xmm3, QWORD PTR [__svml_dexp_ha_data_internal+2048]
        movsd     xmm4, xmm1
        movaps    xmm2, xmm4
        andnps    xmm2, xmm0
        movups    xmm0, XMMWORD PTR [_2il0floatpacket_22]
        andps     xmm0, xmm4
        orps      xmm4, xmm3
        movmskpd  edx, xmm4
        orps      xmm2, xmm0
        movaps    xmm0, xmm3
        andnps    xmm0, xmm2
        not       edx
        and       edx, eax
        and       edx, 1
        je        _B6_2

_B6_4::

        movsd     QWORD PTR [r13], xmm5
        movsd     QWORD PTR [64+r13], xmm0
        jne       _B6_7

_B6_5::

        movsd     xmm0, QWORD PTR [64+r13]
        jmp       _B6_2

_B6_7::

        lea       rcx, QWORD PTR [r13]
        lea       rdx, QWORD PTR [64+r13]

        call      __svml_dexp_ha_cout_rare_internal
        jmp       _B6_5
        ALIGN     16

_B6_8::

__svml_exp1_ha_ex ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_exp1_ha_ex_B1_B7:
	DD	402945
	DD	1430566
	DD	776217
	DD	1638667

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B6_1
	DD	imagerel _B6_8
	DD	imagerel _unwind___svml_exp1_ha_ex_B1_B7

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST6:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_exp4_ha_l9

__svml_exp4_ha_l9	PROC

_B7_1::

        DB        243
        DB        15
        DB        30
        DB        250
L55::

        sub       rsp, 328
        lea       rax, QWORD PTR [__ImageBase]
        vmovups   XMMWORD PTR [224+rsp], xmm14
        vmovups   XMMWORD PTR [208+rsp], xmm10
        vmovups   XMMWORD PTR [240+rsp], xmm9
        vmovups   XMMWORD PTR [256+rsp], xmm8
        vmovups   XMMWORD PTR [272+rsp], xmm7
        vmovups   XMMWORD PTR [288+rsp], xmm6
        mov       QWORD PTR [304+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovupd   ymm8, YMMWORD PTR [rcx]
        and       r13, -64
        vmovupd   ymm4, YMMWORD PTR [__svml_dexp_ha_data_internal+1024]
        vmovupd   ymm5, YMMWORD PTR [__svml_dexp_ha_data_internal+1088]
        vmovupd   ymm9, YMMWORD PTR [__svml_dexp_ha_data_internal+1216]
        vmovupd   ymm10, YMMWORD PTR [__svml_dexp_ha_data_internal+1536]
        vfmadd213pd ymm4, ymm8, ymm5
        vsubpd    ymm7, ymm4, ymm5
        vmovupd   ymm5, YMMWORD PTR [__svml_dexp_ha_data_internal+1664]
        vfnmadd213pd ymm9, ymm7, ymm8
        vaddpd    ymm4, ymm7, YMMWORD PTR [__svml_dexp_ha_data_internal+1152]
        vfnmadd132pd ymm7, ymm9, YMMWORD PTR [__svml_dexp_ha_data_internal+1280]
        vfmadd213pd ymm10, ymm7, YMMWORD PTR [__svml_dexp_ha_data_internal+1472]
        vmulpd    ymm9, ymm7, ymm7
        vfmadd213pd ymm10, ymm7, YMMWORD PTR [__svml_dexp_ha_data_internal+1408]
        vandps    ymm1, ymm4, ymm5
        vfmadd213pd ymm10, ymm7, YMMWORD PTR [__svml_dexp_ha_data_internal+1344]
        vpandn    ymm4, ymm5, ymm4
        vfmadd213pd ymm10, ymm9, ymm7
        vpsllq    ymm5, ymm4, 42
        vandpd    ymm0, ymm8, YMMWORD PTR [__svml_dexp_ha_data_internal+1728]
        vcmpnle_uqpd ymm2, ymm0, YMMWORD PTR [__svml_dexp_ha_data_internal+1792]
        vmovmskpd edx, ymm2
        mov       QWORD PTR [312+rsp], r13
        vextracti128 xmm14, ymm1, 1
        vmovd     r8d, xmm1
        vmovd     r10d, xmm14
        vpextrd   r9d, xmm1, 2
        vpextrd   r11d, xmm14, 2
        movsxd    r8, r8d
        movsxd    r9, r9d
        movsxd    r10, r10d
        movsxd    r11, r11d
        vmovupd   xmm6, XMMWORD PTR [imagerel(__svml_dexp_ha_data_internal)+rax+r8]
        vmovupd   xmm3, XMMWORD PTR [imagerel(__svml_dexp_ha_data_internal)+rax+r9]
        vmovupd   xmm2, XMMWORD PTR [imagerel(__svml_dexp_ha_data_internal)+rax+r10]
        vmovupd   xmm1, XMMWORD PTR [imagerel(__svml_dexp_ha_data_internal)+rax+r11]
        vunpcklpd xmm0, xmm6, xmm3
        vunpckhpd xmm3, xmm6, xmm3
        vunpcklpd xmm14, xmm2, xmm1
        vunpckhpd xmm1, xmm2, xmm1
        vinsertf128 ymm0, ymm0, xmm14, 1
        vinsertf128 ymm2, ymm3, xmm1, 1
        vfmadd213pd ymm10, ymm0, ymm2
        vaddpd    ymm0, ymm0, ymm10
        vpaddq    ymm0, ymm0, ymm5
        test      edx, edx
        jne       _B7_3

_B7_2::

        vmovups   xmm6, XMMWORD PTR [288+rsp]
        vmovups   xmm7, XMMWORD PTR [272+rsp]
        vmovups   xmm8, XMMWORD PTR [256+rsp]
        vmovups   xmm9, XMMWORD PTR [240+rsp]
        vmovups   xmm10, XMMWORD PTR [208+rsp]
        vmovups   xmm14, XMMWORD PTR [224+rsp]
        mov       r13, QWORD PTR [304+rsp]
        add       rsp, 328
        ret

_B7_3::

        vcmpgt_oqpd ymm1, ymm8, YMMWORD PTR [__svml_dexp_ha_data_internal+1984]
        vcmplt_oqpd ymm2, ymm8, YMMWORD PTR [__svml_dexp_ha_data_internal+2048]
        vblendvpd ymm0, ymm0, YMMWORD PTR [_2il0floatpacket_23], ymm1
        vorpd     ymm3, ymm1, ymm2
        vmovmskpd eax, ymm3
        vandnpd   ymm0, ymm2, ymm0
        andn      edx, eax, edx
        je        _B7_2

_B7_4::

        vmovupd   YMMWORD PTR [r13], ymm8
        vmovupd   YMMWORD PTR [64+r13], ymm0
        je        _B7_2

_B7_7::

        xor       eax, eax
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, eax
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, edx

_B7_8::

        bt        esi, ebx
        jc        _B7_11

_B7_9::

        inc       ebx
        cmp       ebx, 4
        jl        _B7_8

_B7_10::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        vmovupd   ymm0, YMMWORD PTR [64+r13]
        jmp       _B7_2

_B7_11::

        vzeroupper
        lea       rcx, QWORD PTR [r13+rbx*8]
        lea       rdx, QWORD PTR [64+r13+rbx*8]

        call      __svml_dexp_ha_cout_rare_internal
        jmp       _B7_9
        ALIGN     16

_B7_12::

__svml_exp4_ha_l9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_exp4_ha_l9_B1_B4:
	DD	1069057
	DD	2544720
	DD	1206344
	DD	1144895
	DD	1083446
	DD	1021997
	DD	895012
	DD	976923
	DD	2687243

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B7_1
	DD	imagerel _B7_7
	DD	imagerel _unwind___svml_exp4_ha_l9_B1_B4

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_exp4_ha_l9_B7_B11:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B7_1
	DD	imagerel _B7_7
	DD	imagerel _unwind___svml_exp4_ha_l9_B1_B4

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B7_7
	DD	imagerel _B7_12
	DD	imagerel _unwind___svml_exp4_ha_l9_B7_B11

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST7:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_exp2_ha_e9

__svml_exp2_ha_e9	PROC

_B8_1::

        DB        243
        DB        15
        DB        30
        DB        250
L74::

        sub       rsp, 248
        lea       r8, QWORD PTR [__ImageBase]
        vmovups   XMMWORD PTR [192+rsp], xmm7
        vmovups   XMMWORD PTR [208+rsp], xmm6
        mov       QWORD PTR [224+rsp], r13
        lea       r13, QWORD PTR [111+rsp]
        vmovupd   xmm7, XMMWORD PTR [rcx]
        and       r13, -64
        vmulpd    xmm4, xmm7, XMMWORD PTR [__svml_dexp_ha_data_internal+1024]
        vmovupd   xmm3, XMMWORD PTR [__svml_dexp_ha_data_internal+1088]
        vaddpd    xmm6, xmm3, xmm4
        vmovq     xmm1, QWORD PTR [__svml_dexp_ha_data_internal+1856]
        vpshufd   xmm2, xmm7, 221
        vmovq     xmm4, QWORD PTR [__svml_dexp_ha_data_internal+1920]
        vpand     xmm0, xmm2, xmm1
        vsubpd    xmm5, xmm6, xmm3
        vpcmpgtd  xmm3, xmm0, xmm4
        vmovdqu   xmm4, XMMWORD PTR [__svml_dexp_ha_data_internal+1600]
        vpand     xmm2, xmm6, xmm4
        vpandn    xmm4, xmm4, xmm6
        vpshufd   xmm1, xmm2, 136
        vpsllq    xmm6, xmm4, 46
        vpslld    xmm0, xmm1, 4
        vmovd     eax, xmm0
        vmovmskps ecx, xmm3
        vpextrd   edx, xmm0, 1
        vmulpd    xmm0, xmm5, XMMWORD PTR [__svml_dexp_ha_data_internal+1216]
        vmulpd    xmm5, xmm5, XMMWORD PTR [__svml_dexp_ha_data_internal+1280]
        movsxd    rax, eax
        movsxd    rdx, edx
        mov       QWORD PTR [232+rsp], r13
        vmovupd   xmm2, XMMWORD PTR [imagerel(__svml_dexp_ha_data_internal)+r8+rax]
        vmovupd   xmm1, XMMWORD PTR [imagerel(__svml_dexp_ha_data_internal)+r8+rdx]
        vunpcklpd xmm3, xmm2, xmm1
        vunpckhpd xmm2, xmm2, xmm1
        vsubpd    xmm1, xmm7, xmm0
        vsubpd    xmm1, xmm1, xmm5
        vmulpd    xmm0, xmm1, XMMWORD PTR [__svml_dexp_ha_data_internal+1536]
        vaddpd    xmm5, xmm0, XMMWORD PTR [__svml_dexp_ha_data_internal+1472]
        vmulpd    xmm0, xmm1, xmm5
        vaddpd    xmm5, xmm0, XMMWORD PTR [__svml_dexp_ha_data_internal+1408]
        vmulpd    xmm0, xmm1, xmm1
        vmulpd    xmm5, xmm1, xmm5
        vaddpd    xmm5, xmm5, XMMWORD PTR [__svml_dexp_ha_data_internal+1344]
        vmulpd    xmm0, xmm0, xmm5
        vaddpd    xmm1, xmm1, xmm0
        vmulpd    xmm0, xmm3, xmm1
        vaddpd    xmm2, xmm2, xmm0
        vaddpd    xmm3, xmm3, xmm2
        vpaddq    xmm0, xmm3, xmm6
        test      cl, 3
        jne       _B8_3

_B8_2::

        vmovups   xmm6, XMMWORD PTR [208+rsp]
        vmovups   xmm7, XMMWORD PTR [192+rsp]
        mov       r13, QWORD PTR [224+rsp]
        add       rsp, 248
        ret

_B8_3::

        vcmpgtpd  xmm1, xmm7, XMMWORD PTR [__svml_dexp_ha_data_internal+1984]
        vcmpltpd  xmm2, xmm7, XMMWORD PTR [__svml_dexp_ha_data_internal+2048]
        vblendvpd xmm0, xmm0, XMMWORD PTR [_2il0floatpacket_22], xmm1
        vorpd     xmm3, xmm1, xmm2
        vmovmskpd edx, xmm3
        vandnpd   xmm0, xmm2, xmm0
        not       edx
        and       edx, ecx
        and       edx, 3
        je        _B8_2

_B8_4::

        vmovupd   XMMWORD PTR [r13], xmm7
        vmovupd   XMMWORD PTR [64+r13], xmm0
        je        _B8_2

_B8_7::

        xor       eax, eax
        mov       QWORD PTR [40+rsp], rbx
        mov       ebx, eax
        mov       QWORD PTR [32+rsp], rsi
        mov       esi, edx

_B8_8::

        bt        esi, ebx
        jc        _B8_11

_B8_9::

        inc       ebx
        cmp       ebx, 2
        jl        _B8_8

_B8_10::

        mov       rbx, QWORD PTR [40+rsp]
        mov       rsi, QWORD PTR [32+rsp]
        vmovupd   xmm0, XMMWORD PTR [64+r13]
        jmp       _B8_2

_B8_11::

        lea       rcx, QWORD PTR [r13+rbx*8]
        lea       rdx, QWORD PTR [64+r13+rbx*8]

        call      __svml_dexp_ha_cout_rare_internal
        jmp       _B8_9
        ALIGN     16

_B8_12::

__svml_exp2_ha_e9 ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_exp2_ha_e9_B1_B4:
	DD	535553
	DD	1889324
	DD	878628
	DD	817179
	DD	2031883

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B8_1
	DD	imagerel _B8_7
	DD	imagerel _unwind___svml_exp2_ha_e9_B1_B4

.pdata	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_exp2_ha_e9_B7_B11:
	DD	265761
	DD	287758
	DD	340999
	DD	imagerel _B8_1
	DD	imagerel _B8_7
	DD	imagerel _unwind___svml_exp2_ha_e9_B1_B4

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B8_7
	DD	imagerel _B8_12
	DD	imagerel _unwind___svml_exp2_ha_e9_B7_B11

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_TEXT	SEGMENT      'CODE'

TXTST8:

_TEXT	ENDS
_TEXT	SEGMENT      'CODE'

       ALIGN     16
	PUBLIC __svml_dexp_ha_cout_rare_internal

__svml_dexp_ha_cout_rare_internal	PROC

_B9_1::

        DB        243
        DB        15
        DB        30
        DB        250
L85::

        sub       rsp, 104
        mov       r9, rdx
        movzx     edx, WORD PTR [6+rcx]
        xor       eax, eax
        and       edx, 32752
        shr       edx, 4
        movsd     xmm1, QWORD PTR [rcx]
        movsd     QWORD PTR [96+rsp], xmm1
        cmp       edx, 2047
        je        _B9_17

_B9_2::

        cmp       edx, 970
        jle       _B9_15

_B9_3::

        movsd     xmm0, QWORD PTR [_imldExpHATab+1080]
        comisd    xmm0, xmm1
        jb        _B9_14

_B9_4::

        comisd    xmm1, QWORD PTR [_imldExpHATab+1096]
        jb        _B9_13

_B9_5::

        movsd     xmm0, QWORD PTR [_imldExpHATab+1024]
        lea       r11, QWORD PTR [__ImageBase]
        mulsd     xmm0, xmm1
        movsd     QWORD PTR [80+rsp], xmm0
        movsd     xmm2, QWORD PTR [80+rsp]
        movsd     xmm0, QWORD PTR [_imldExpHATab+1072]
        mov       rcx, QWORD PTR [_imldExpHATab+1136]
        mov       QWORD PTR [96+rsp], rcx
        addsd     xmm2, QWORD PTR [_imldExpHATab+1032]
        movsd     QWORD PTR [88+rsp], xmm2
        movaps    xmm2, xmm1
        movsd     xmm3, QWORD PTR [88+rsp]
        mov       edx, DWORD PTR [88+rsp]
        mov       r8d, edx
        and       edx, 63
        subsd     xmm3, QWORD PTR [_imldExpHATab+1032]
        movsd     QWORD PTR [80+rsp], xmm3
        lea       r10d, DWORD PTR [rdx+rdx]
        movsd     xmm4, QWORD PTR [80+rsp]
        lea       edx, DWORD PTR [1+rdx+rdx]
        mulsd     xmm4, QWORD PTR [_imldExpHATab+1104]
        movsd     xmm5, QWORD PTR [80+rsp]
        subsd     xmm2, xmm4
        mulsd     xmm5, QWORD PTR [_imldExpHATab+1112]
        shr       r8d, 6
        subsd     xmm2, xmm5
        comisd    xmm1, QWORD PTR [_imldExpHATab+1088]
        mulsd     xmm0, xmm2
        addsd     xmm0, QWORD PTR [_imldExpHATab+1064]
        mulsd     xmm0, xmm2
        lea       ecx, DWORD PTR [1023+r8]
        addsd     xmm0, QWORD PTR [_imldExpHATab+1056]
        mulsd     xmm0, xmm2
        addsd     xmm0, QWORD PTR [_imldExpHATab+1048]
        mulsd     xmm0, xmm2
        addsd     xmm0, QWORD PTR [_imldExpHATab+1040]
        mulsd     xmm0, xmm2
        mulsd     xmm0, xmm2
        addsd     xmm0, xmm2
        movsd     xmm2, QWORD PTR [imagerel(_imldExpHATab)+r11+r10*8]
        addsd     xmm0, QWORD PTR [imagerel(_imldExpHATab)+r11+rdx*8]
        mulsd     xmm0, xmm2
        jb        _B9_9

_B9_6::

        and       ecx, 2047
        addsd     xmm0, xmm2
        cmp       ecx, 2046
        ja        _B9_8

_B9_7::

        mov       rdx, QWORD PTR [_imldExpHATab+1136]
        shr       rdx, 48
        shl       ecx, 4
        and       edx, -32753
        or        edx, ecx
        mov       WORD PTR [102+rsp], dx
        movsd     xmm1, QWORD PTR [96+rsp]
        mulsd     xmm0, xmm1
        movsd     QWORD PTR [r9], xmm0
        add       rsp, 104
        ret

_B9_8::

        dec       ecx
        and       ecx, 2047
        movzx     edx, WORD PTR [102+rsp]
        shl       ecx, 4
        and       edx, -32753
        or        edx, ecx
        mov       WORD PTR [102+rsp], dx
        movsd     xmm1, QWORD PTR [96+rsp]
        mulsd     xmm0, xmm1
        mulsd     xmm0, QWORD PTR [_imldExpHATab+1152]
        movsd     QWORD PTR [r9], xmm0
        add       rsp, 104
        ret

_B9_9::

        add       r8d, 1083
        and       r8d, 2047
        mov       eax, r8d
        movzx     edx, WORD PTR [102+rsp]
        shl       eax, 4
        and       edx, -32753
        or        edx, eax
        mov       WORD PTR [102+rsp], dx
        movsd     xmm3, QWORD PTR [96+rsp]
        mulsd     xmm0, xmm3
        mulsd     xmm3, xmm2
        movaps    xmm1, xmm3
        addsd     xmm1, xmm0
        cmp       r8d, 50
        ja        _B9_11

_B9_10::

        mulsd     xmm1, QWORD PTR [_imldExpHATab+1160]
        movsd     QWORD PTR [r9], xmm1
        jmp       _B9_12

_B9_11::

        movsd     QWORD PTR [32+rsp], xmm1
        movsd     xmm1, QWORD PTR [32+rsp]
        subsd     xmm3, xmm1
        movsd     QWORD PTR [40+rsp], xmm3
        movsd     xmm2, QWORD PTR [40+rsp]
        addsd     xmm2, xmm0
        movsd     QWORD PTR [40+rsp], xmm2
        movsd     xmm0, QWORD PTR [32+rsp]
        mulsd     xmm0, QWORD PTR [_imldExpHATab+1168]
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
        mulsd     xmm0, QWORD PTR [_imldExpHATab+1160]
        movsd     QWORD PTR [64+rsp], xmm0
        movsd     xmm1, QWORD PTR [72+rsp]
        mulsd     xmm1, QWORD PTR [_imldExpHATab+1160]
        movsd     QWORD PTR [72+rsp], xmm1
        movsd     xmm2, QWORD PTR [64+rsp]
        movsd     xmm5, QWORD PTR [72+rsp]
        addsd     xmm2, xmm5
        movsd     QWORD PTR [r9], xmm2

_B9_12::

        mov       eax, 4
        add       rsp, 104
        ret

_B9_13::

        movsd     xmm0, QWORD PTR [_imldExpHATab+1120]
        mov       eax, 4
        mulsd     xmm0, xmm0
        movsd     QWORD PTR [r9], xmm0
        add       rsp, 104
        ret

_B9_14::

        movsd     xmm0, QWORD PTR [_imldExpHATab+1128]
        mov       eax, 3
        mulsd     xmm0, xmm0
        movsd     QWORD PTR [r9], xmm0
        add       rsp, 104
        ret

_B9_15::

        movsd     xmm0, QWORD PTR [_imldExpHATab+1144]
        addsd     xmm0, xmm1
        movsd     QWORD PTR [r9], xmm0

_B9_16::

        add       rsp, 104
        ret

_B9_17::

        mov       dl, BYTE PTR [103+rsp]
        and       dl, -128
        cmp       dl, -128
        je        _B9_19

_B9_18::

        mulsd     xmm1, xmm1
        movsd     QWORD PTR [r9], xmm1
        add       rsp, 104
        ret

_B9_19::

        test      DWORD PTR [100+rsp], 1048575
        jne       _B9_18

_B9_20::

        cmp       DWORD PTR [96+rsp], 0
        jne       _B9_18

_B9_21::

        mov       rdx, QWORD PTR [_imldExpHATab+1136]
        mov       QWORD PTR [r9], rdx
        add       rsp, 104
        ret
        ALIGN     16

_B9_22::

__svml_dexp_ha_cout_rare_internal ENDP

_TEXT	ENDS
.xdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H
_unwind___svml_dexp_ha_cout_rare_internal_B1_B21:
	DD	67585
	DD	49672

.xdata	ENDS
.pdata	SEGMENT  DWORD   READ  ''

	ALIGN 004H

	DD	imagerel _B9_1
	DD	imagerel _B9_22
	DD	imagerel _unwind___svml_dexp_ha_cout_rare_internal_B1_B21

.pdata	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS

_RDATA	SEGMENT     READ PAGE   'DATA'
	ALIGN  32
	PUBLIC __svml_dexp_ha_data_internal_avx512
__svml_dexp_ha_data_internal_avx512	DD	0
	DD	1072693248
	DD	1828292879
	DD	1072739672
	DD	1014845819
	DD	1072788152
	DD	1853186616
	DD	1072838778
	DD	171030293
	DD	1072891646
	DD	1276261410
	DD	1072946854
	DD	3577096743
	DD	1073004506
	DD	3712504873
	DD	1073064711
	DD	1719614413
	DD	1073127582
	DD	1944781191
	DD	1073193236
	DD	1110089947
	DD	1073261797
	DD	2191782032
	DD	1073333393
	DD	2572866477
	DD	1073408159
	DD	3716502172
	DD	1073486235
	DD	3707479175
	DD	1073567768
	DD	2728693978
	DD	1073652911
	DD	0
	DD	0
	DD	1568897901
	DD	1016568486
	DD	3936719688
	DD	3162512149
	DD	3819481236
	DD	1016499965
	DD	1303423926
	DD	1015238005
	DD	2804567149
	DD	1015390024
	DD	3145379760
	DD	1014403278
	DD	3793507337
	DD	1016095713
	DD	3210617384
	DD	3163796463
	DD	3108873501
	DD	3162190556
	DD	3253791412
	DD	1015920431
	DD	730975783
	DD	1014083580
	DD	2462790535
	DD	1015814775
	DD	816778419
	DD	1014197934
	DD	2789017511
	DD	1014276997
	DD	2413007344
	DD	3163551506
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
	DD	16368
	DD	1123549184
	DD	16368
	DD	1123549184
	DD	16368
	DD	1123549184
	DD	16368
	DD	1123549184
	DD	16368
	DD	1123549184
	DD	16368
	DD	1123549184
	DD	16368
	DD	1123549184
	DD	16368
	DD	1123549184
	DD	4277811695
	DD	1072049730
	DD	4277811695
	DD	1072049730
	DD	4277811695
	DD	1072049730
	DD	4277811695
	DD	1072049730
	DD	4277811695
	DD	1072049730
	DD	4277811695
	DD	1072049730
	DD	4277811695
	DD	1072049730
	DD	4277811695
	DD	1072049730
	DD	993624127
	DD	1014676638
	DD	993624127
	DD	1014676638
	DD	993624127
	DD	1014676638
	DD	993624127
	DD	1014676638
	DD	993624127
	DD	1014676638
	DD	993624127
	DD	1014676638
	DD	993624127
	DD	1014676638
	DD	993624127
	DD	1014676638
	DD	4294967295
	DD	3221225471
	DD	4294967295
	DD	3221225471
	DD	4294967295
	DD	3221225471
	DD	4294967295
	DD	3221225471
	DD	4294967295
	DD	3221225471
	DD	4294967295
	DD	3221225471
	DD	4294967295
	DD	3221225471
	DD	4294967295
	DD	3221225471
	DD	915672068
	DD	1062682904
	DD	915672068
	DD	1062682904
	DD	915672068
	DD	1062682904
	DD	915672068
	DD	1062682904
	DD	915672068
	DD	1062682904
	DD	915672068
	DD	1062682904
	DD	915672068
	DD	1062682904
	DD	915672068
	DD	1062682904
	DD	3150079424
	DD	1065422876
	DD	3150079424
	DD	1065422876
	DD	3150079424
	DD	1065422876
	DD	3150079424
	DD	1065422876
	DD	3150079424
	DD	1065422876
	DD	3150079424
	DD	1065422876
	DD	3150079424
	DD	1065422876
	DD	3150079424
	DD	1065422876
	DD	606955774
	DD	1067799895
	DD	606955774
	DD	1067799895
	DD	606955774
	DD	1067799895
	DD	606955774
	DD	1067799895
	DD	606955774
	DD	1067799895
	DD	606955774
	DD	1067799895
	DD	606955774
	DD	1067799895
	DD	606955774
	DD	1067799895
	DD	1402181426
	DD	1069897045
	DD	1402181426
	DD	1069897045
	DD	1402181426
	DD	1069897045
	DD	1402181426
	DD	1069897045
	DD	1402181426
	DD	1069897045
	DD	1402181426
	DD	1069897045
	DD	1402181426
	DD	1069897045
	DD	1402181426
	DD	1069897045
	DD	53256
	DD	1071644672
	DD	53256
	DD	1071644672
	DD	53256
	DD	1071644672
	DD	53256
	DD	1071644672
	DD	53256
	DD	1071644672
	DD	53256
	DD	1071644672
	DD	53256
	DD	1071644672
	DD	53256
	DD	1071644672
	DD	4294967152
	DD	1072693247
	DD	4294967152
	DD	1072693247
	DD	4294967152
	DD	1072693247
	DD	4294967152
	DD	1072693247
	DD	4294967152
	DD	1072693247
	DD	4294967152
	DD	1072693247
	DD	4294967152
	DD	1072693247
	DD	4294967152
	DD	1072693247
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
	DD	1287323204
	DD	1082531232
	DD	1287323204
	DD	1082531232
	DD	1287323204
	DD	1082531232
	DD	1287323204
	DD	1082531232
	DD	1287323204
	DD	1082531232
	DD	1287323204
	DD	1082531232
	DD	1287323204
	DD	1082531232
	DD	1287323204
	DD	1082531232
	DD	0
	DD	1005584384
	DD	0
	DD	1005584384
	DD	0
	DD	1005584384
	DD	0
	DD	1005584384
	DD	0
	DD	1005584384
	DD	0
	DD	1005584384
	DD	0
	DD	1005584384
	DD	0
	DD	1005584384
	DD	120
	DD	0
	DD	120
	DD	0
	DD	120
	DD	0
	DD	120
	DD	0
	DD	120
	DD	0
	DD	120
	DD	0
	DD	120
	DD	0
	DD	120
	DD	0
	DD	32752
	DD	0
	DD	32752
	DD	0
	DD	32752
	DD	0
	DD	32752
	DD	0
	DD	32752
	DD	0
	DD	32752
	DD	0
	DD	32752
	DD	0
	DD	32752
	DD	0
	PUBLIC __svml_dexp_ha_data_internal
__svml_dexp_ha_data_internal	DD	0
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
	DD	1697350398
	DD	1079448903
	DD	1697350398
	DD	1079448903
	DD	1697350398
	DD	1079448903
	DD	1697350398
	DD	1079448903
	DD	1697350398
	DD	1079448903
	DD	1697350398
	DD	1079448903
	DD	1697350398
	DD	1079448903
	DD	1697350398
	DD	1079448903
	DD	0
	DD	1127743488
	DD	0
	DD	1127743488
	DD	0
	DD	1127743488
	DD	0
	DD	1127743488
	DD	0
	DD	1127743488
	DD	0
	DD	1127743488
	DD	0
	DD	1127743488
	DD	0
	DD	1127743488
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
	DD	4277796864
	DD	1065758274
	DD	4277796864
	DD	1065758274
	DD	4277796864
	DD	1065758274
	DD	4277796864
	DD	1065758274
	DD	4277796864
	DD	1065758274
	DD	4277796864
	DD	1065758274
	DD	4277796864
	DD	1065758274
	DD	4277796864
	DD	1065758274
	DD	3164486458
	DD	1025308570
	DD	3164486458
	DD	1025308570
	DD	3164486458
	DD	1025308570
	DD	3164486458
	DD	1025308570
	DD	3164486458
	DD	1025308570
	DD	3164486458
	DD	1025308570
	DD	3164486458
	DD	1025308570
	DD	3164486458
	DD	1025308570
	DD	4294957883
	DD	1071644671
	DD	4294957883
	DD	1071644671
	DD	4294957883
	DD	1071644671
	DD	4294957883
	DD	1071644671
	DD	4294957883
	DD	1071644671
	DD	4294957883
	DD	1071644671
	DD	4294957883
	DD	1071644671
	DD	4294957883
	DD	1071644671
	DD	1431659838
	DD	1069897045
	DD	1431659838
	DD	1069897045
	DD	1431659838
	DD	1069897045
	DD	1431659838
	DD	1069897045
	DD	1431659838
	DD	1069897045
	DD	1431659838
	DD	1069897045
	DD	1431659838
	DD	1069897045
	DD	1431659838
	DD	1069897045
	DD	1059163027
	DD	1067799895
	DD	1059163027
	DD	1067799895
	DD	1059163027
	DD	1067799895
	DD	1059163027
	DD	1067799895
	DD	1059163027
	DD	1067799895
	DD	1059163027
	DD	1067799895
	DD	1059163027
	DD	1067799895
	DD	1059163027
	DD	1067799895
	DD	765416603
	DD	1065423121
	DD	765416603
	DD	1065423121
	DD	765416603
	DD	1065423121
	DD	765416603
	DD	1065423121
	DD	765416603
	DD	1065423121
	DD	765416603
	DD	1065423121
	DD	765416603
	DD	1065423121
	DD	765416603
	DD	1065423121
	DD	63
	DD	0
	DD	63
	DD	0
	DD	63
	DD	0
	DD	63
	DD	0
	DD	63
	DD	0
	DD	63
	DD	0
	DD	63
	DD	0
	DD	63
	DD	0
	DD	1023
	DD	0
	DD	1023
	DD	0
	DD	1023
	DD	0
	DD	1023
	DD	0
	DD	1023
	DD	0
	DD	1023
	DD	0
	DD	1023
	DD	0
	DD	1023
	DD	0
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
	DD	4294967295
	DD	1082532650
	DD	4294967295
	DD	1082532650
	DD	4294967295
	DD	1082532650
	DD	4294967295
	DD	1082532650
	DD	4294967295
	DD	1082532650
	DD	4294967295
	DD	1082532650
	DD	4294967295
	DD	1082532650
	DD	4294967295
	DD	1082532650
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
	DD	1082532650
	DD	1082532650
	DD	1082532650
	DD	1082532650
	DD	1082532650
	DD	1082532650
	DD	1082532650
	DD	1082532650
	DD	1082532650
	DD	1082532650
	DD	1082532650
	DD	1082532650
	DD	1082532650
	DD	1082532650
	DD	1082532650
	DD	1082532650
	DD	4277811695
	DD	1082535490
	DD	4277811695
	DD	1082535490
	DD	4277811695
	DD	1082535490
	DD	4277811695
	DD	1082535490
	DD	4277811695
	DD	1082535490
	DD	4277811695
	DD	1082535490
	DD	4277811695
	DD	1082535490
	DD	4277811695
	DD	1082535490
	DD	3576508497
	DD	3230091536
	DD	3576508497
	DD	3230091536
	DD	3576508497
	DD	3230091536
	DD	3576508497
	DD	3230091536
	DD	3576508497
	DD	3230091536
	DD	3576508497
	DD	3230091536
	DD	3576508497
	DD	3230091536
	DD	3576508497
	DD	3230091536
_2il0floatpacket_23	DD	000000000H,07ff00000H,000000000H,07ff00000H,000000000H,07ff00000H,000000000H,07ff00000H
_imldExpHATab	DD	0
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
	DD	4277811695
	DD	1082535490
	DD	3715808466
	DD	3230016299
	DD	3576508497
	DD	3230091536
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
_2il0floatpacket_22	DD	000000000H,07ff00000H,000000000H,07ff00000H
_RDATA	ENDS
_DATA	SEGMENT      'DATA'
_DATA	ENDS
EXTRN	__ImageBase:PROC
EXTRN	_fltused:BYTE
	ENDIF
	END
