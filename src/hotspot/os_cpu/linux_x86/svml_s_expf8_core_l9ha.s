/*
 * Copyright (c) 2018, Intel Corporation.
 * Intel Short Vector Math Library (SVML) Source Code
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 *
 */

#include "utilities/globalDefinitions_vecApi.hpp"
#ifdef __VECTOR_API_MATH_INTRINSICS_LINUX

# -- Machine type EM64t
	.file "_svml_template.c"
	.text
..TXTST0:
.L_2__routine_start___svml_expf8_ha_l9_0:
# -- Begin  __svml_expf8_ha_l9
	.text
       .align    16,0x90
	.globl __svml_expf8_ha_l9
__svml_expf8_ha_l9:
# parameter 1: %ymm0
..B1.1:
        .byte     243
        .byte     15
        .byte     30
        .byte     250
	.cfi_startproc
..___tag_value___svml_expf8_ha_l9.1:
..L2:
        pushq     %rbp
	.cfi_def_cfa_offset 16
        movq      %rsp, %rbp
	.cfi_def_cfa 6, 16
	.cfi_offset 6, -16
        andq      $-64, %rsp
        subq      $448, %rsp
        vmovups   64+__svml_sexp_ha_data_internal(%rip), %ymm7
        vmovups   __svml_sexp_ha_data_internal(%rip), %ymm5
        vmovups   384+__svml_sexp_ha_data_internal(%rip), %ymm1
        vmovdqa   %ymm0, %ymm6
        vfmadd213ps %ymm7, %ymm6, %ymm5
        vmovups   256+__svml_sexp_ha_data_internal(%rip), %ymm0
        vsubps    %ymm7, %ymm5, %ymm4
        vmovups   448+__svml_sexp_ha_data_internal(%rip), %ymm7
        vfnmadd213ps %ymm6, %ymm4, %ymm0
        vandps    128+__svml_sexp_ha_data_internal(%rip), %ymm6, %ymm2
        vcmpnle_uqps 192+__svml_sexp_ha_data_internal(%rip), %ymm2, %ymm3
        vfnmadd132ps 320+__svml_sexp_ha_data_internal(%rip), %ymm0, %ymm4
        vmovups   512+__svml_sexp_ha_data_internal(%rip), %ymm2
        vmulps    %ymm4, %ymm4, %ymm0
        vfmadd213ps 576+__svml_sexp_ha_data_internal(%rip), %ymm4, %ymm2
        vfmadd213ps 640+__svml_sexp_ha_data_internal(%rip), %ymm4, %ymm2
        vmovmskps %ymm3, %edx
        vpermilps %ymm5, %ymm1, %ymm3
        testl     %edx, %edx
        vpermilps %ymm5, %ymm7, %ymm1
        vpslld    $21, %ymm5, %ymm5
        vfmadd213ps %ymm3, %ymm0, %ymm2
        vandps    704+__svml_sexp_ha_data_internal(%rip), %ymm5, %ymm5
        vmulps    %ymm5, %ymm1, %ymm1
        vaddps    %ymm2, %ymm4, %ymm0
        vfmadd213ps %ymm1, %ymm1, %ymm0
        jne       ..B1.3
..B1.2:
        movq      %rbp, %rsp
        popq      %rbp
	.cfi_def_cfa 7, 8
	.cfi_restore 6
        ret       
	.cfi_def_cfa 6, 16
	.cfi_offset 6, -16
..B1.3:
        vcmpgt_oqps 2560+__svml_sexp_ha_data_internal(%rip), %ymm6, %ymm1
        vcmplt_oqps 2624+__svml_sexp_ha_data_internal(%rip), %ymm6, %ymm2
        vblendvps %ymm1, .L_2il0floatpacket.14(%rip), %ymm0, %ymm0
        vorps     %ymm2, %ymm1, %ymm3
        vmovmskps %ymm3, %eax
        vandnps   %ymm0, %ymm2, %ymm0
        andn      %edx, %eax, %edx
        je        ..B1.2
..B1.4:
        vmovups   %ymm6, 320(%rsp)
        vmovups   %ymm0, 384(%rsp)
        je        ..B1.2
..B1.7:
        xorl      %eax, %eax
        vmovups   %ymm8, 224(%rsp)
        vmovups   %ymm9, 192(%rsp)
        vmovups   %ymm10, 160(%rsp)
        vmovups   %ymm11, 128(%rsp)
        vmovups   %ymm12, 96(%rsp)
        vmovups   %ymm13, 64(%rsp)
        vmovups   %ymm14, 32(%rsp)
        vmovups   %ymm15, (%rsp)
        movq      %rsi, 264(%rsp)
        movq      %rdi, 256(%rsp)
        movq      %r12, 280(%rsp)
	.cfi_escape 0x10, 0x04, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0x48, 0xff, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0x05, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0x40, 0xff, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0x0c, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0x58, 0xff, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0xdb, 0x00, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0x20, 0xff, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0xdc, 0x00, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0x00, 0xff, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0xdd, 0x00, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0xe0, 0xfe, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0xde, 0x00, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0xc0, 0xfe, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0xdf, 0x00, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0xa0, 0xfe, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0xe0, 0x00, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0x80, 0xfe, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0xe1, 0x00, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0x60, 0xfe, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0xe2, 0x00, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0x40, 0xfe, 0xff, 0xff, 0x22
        movl      %eax, %r12d
        movq      %r13, 272(%rsp)
	.cfi_escape 0x10, 0x0d, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0x50, 0xff, 0xff, 0xff, 0x22
        movl      %edx, %r13d
..B1.8:
        btl       %r12d, %r13d
        jc        ..B1.11
..B1.9:
        incl      %r12d
        cmpl      $8, %r12d
        jl        ..B1.8
..B1.10:
        vmovups   224(%rsp), %ymm8
	.cfi_restore 91
        vmovups   192(%rsp), %ymm9
	.cfi_restore 92
        vmovups   160(%rsp), %ymm10
	.cfi_restore 93
        vmovups   128(%rsp), %ymm11
	.cfi_restore 94
        vmovups   96(%rsp), %ymm12
	.cfi_restore 95
        vmovups   64(%rsp), %ymm13
	.cfi_restore 96
        vmovups   32(%rsp), %ymm14
	.cfi_restore 97
        vmovups   (%rsp), %ymm15
	.cfi_restore 98
        vmovups   384(%rsp), %ymm0
        movq      264(%rsp), %rsi
	.cfi_restore 4
        movq      256(%rsp), %rdi
	.cfi_restore 5
        movq      280(%rsp), %r12
	.cfi_restore 12
        movq      272(%rsp), %r13
	.cfi_restore 13
        jmp       ..B1.2
	.cfi_escape 0x10, 0x04, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0x48, 0xff, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0x05, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0x40, 0xff, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0x0c, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0x58, 0xff, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0x0d, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0x50, 0xff, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0xdb, 0x00, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0x20, 0xff, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0xdc, 0x00, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0x00, 0xff, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0xdd, 0x00, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0xe0, 0xfe, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0xde, 0x00, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0xc0, 0xfe, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0xdf, 0x00, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0xa0, 0xfe, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0xe0, 0x00, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0x80, 0xfe, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0xe1, 0x00, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0x60, 0xfe, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0xe2, 0x00, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0x40, 0xfe, 0xff, 0xff, 0x22
..B1.11:
        vzeroupper 
        lea       320(%rsp,%r12,4), %rdi
        lea       384(%rsp,%r12,4), %rsi
..___tag_value___svml_expf8_ha_l9.69:
        call      __svml_sexp_ha_cout_rare_internal
..___tag_value___svml_expf8_ha_l9.70:
        jmp       ..B1.9
        .align    16,0x90
	.cfi_endproc
	.type	__svml_expf8_ha_l9,@function
	.size	__svml_expf8_ha_l9,.-__svml_expf8_ha_l9
..LN__svml_expf8_ha_l9.0:
	.data
# -- End  __svml_expf8_ha_l9
	.section .rodata, "a"
	.align 32
	.align 32
.L_2il0floatpacket.14:
	.long	0x7f800000,0x7f800000,0x7f800000,0x7f800000,0x7f800000,0x7f800000,0x7f800000,0x7f800000
	.type	.L_2il0floatpacket.14,@object
	.size	.L_2il0floatpacket.14,32
	.data
	.hidden __svml_sexp_ha_data_internal
	.hidden __svml_sexp_ha_cout_rare_internal
	.section .note.GNU-stack, ""
	.section .note.gnu.property, "a"
	.align 8
	.long	4
	.long	16
	.long	5
	.long	5590599
	.long	3221225474
	.long	4
	.long	3
	.long	0
# End
#endif
