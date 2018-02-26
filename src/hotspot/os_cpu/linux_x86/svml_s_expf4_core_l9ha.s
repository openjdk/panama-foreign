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
.L_2__routine_start___svml_expf4_ha_l9_0:
# -- Begin  __svml_expf4_ha_l9
	.text
       .align    16,0x90
	.globl __svml_expf4_ha_l9
__svml_expf4_ha_l9:
# parameter 1: %xmm0
..B1.1:
        .byte     243
        .byte     15
        .byte     30
        .byte     250
	.cfi_startproc
..___tag_value___svml_expf4_ha_l9.1:
..L2:
        pushq     %rbp
	.cfi_def_cfa_offset 16
        movq      %rsp, %rbp
	.cfi_def_cfa 6, 16
	.cfi_offset 6, -16
        andq      $-64, %rsp
        subq      $320, %rsp
        vmovaps   %xmm0, %xmm6
        vmovups   64+__svml_sexp_ha_data_internal(%rip), %xmm7
        vmovups   __svml_sexp_ha_data_internal(%rip), %xmm5
        vfmadd213ps %xmm7, %xmm6, %xmm5
        vmovups   256+__svml_sexp_ha_data_internal(%rip), %xmm0
        vandps    128+__svml_sexp_ha_data_internal(%rip), %xmm6, %xmm2
        vmovups   384+__svml_sexp_ha_data_internal(%rip), %xmm1
        vcmpnleps 192+__svml_sexp_ha_data_internal(%rip), %xmm2, %xmm3
        vsubps    %xmm7, %xmm5, %xmm4
        vmovmskps %xmm3, %edx
        vmovups   512+__svml_sexp_ha_data_internal(%rip), %xmm2
        vmovups   448+__svml_sexp_ha_data_internal(%rip), %xmm7
        vfnmadd213ps %xmm6, %xmm4, %xmm0
        vfnmadd132ps 320+__svml_sexp_ha_data_internal(%rip), %xmm0, %xmm4
        vfmadd213ps 576+__svml_sexp_ha_data_internal(%rip), %xmm4, %xmm2
        vmulps    %xmm4, %xmm4, %xmm0
        vfmadd213ps 640+__svml_sexp_ha_data_internal(%rip), %xmm4, %xmm2
        vpermilps %xmm5, %xmm1, %xmm3
        vfmadd213ps %xmm3, %xmm0, %xmm2
        vpermilps %xmm5, %xmm7, %xmm1
        vpslld    $21, %xmm5, %xmm5
        vandps    704+__svml_sexp_ha_data_internal(%rip), %xmm5, %xmm5
        vaddps    %xmm2, %xmm4, %xmm0
        vmulps    %xmm5, %xmm1, %xmm1
        vfmadd213ps %xmm1, %xmm1, %xmm0
        testl     %edx, %edx
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
        vcmpgtps  2560+__svml_sexp_ha_data_internal(%rip), %xmm6, %xmm1
        vcmpltps  2624+__svml_sexp_ha_data_internal(%rip), %xmm6, %xmm2
        vblendvps %xmm1, .L_2il0floatpacket.14(%rip), %xmm0, %xmm0
        vorps     %xmm2, %xmm1, %xmm3
        vmovmskps %xmm3, %eax
        vandnps   %xmm0, %xmm2, %xmm0
        andn      %edx, %eax, %edx
        je        ..B1.2
..B1.4:
        vmovups   %xmm6, 192(%rsp)
        vmovups   %xmm0, 256(%rsp)
        je        ..B1.2
..B1.7:
        xorl      %eax, %eax
        vmovups   %xmm8, 112(%rsp)
        vmovups   %xmm9, 96(%rsp)
        vmovups   %xmm10, 80(%rsp)
        vmovups   %xmm11, 64(%rsp)
        vmovups   %xmm12, 48(%rsp)
        vmovups   %xmm13, 32(%rsp)
        vmovups   %xmm14, 16(%rsp)
        vmovups   %xmm15, (%rsp)
        movq      %rsi, 136(%rsp)
        movq      %rdi, 128(%rsp)
        movq      %r12, 152(%rsp)
	.cfi_escape 0x10, 0x04, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0x48, 0xff, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0x05, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0x40, 0xff, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0x0c, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0x58, 0xff, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0x19, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0x30, 0xff, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0x1a, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0x20, 0xff, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0x1b, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0x10, 0xff, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0x1c, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0x00, 0xff, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0x1d, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0xf0, 0xfe, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0x1e, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0xe0, 0xfe, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0x1f, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0xd0, 0xfe, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0x20, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0xc0, 0xfe, 0xff, 0xff, 0x22
        movl      %eax, %r12d
        movq      %r13, 144(%rsp)
	.cfi_escape 0x10, 0x0d, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0x50, 0xff, 0xff, 0xff, 0x22
        movl      %edx, %r13d
..B1.8:
        btl       %r12d, %r13d
        jc        ..B1.11
..B1.9:
        incl      %r12d
        cmpl      $4, %r12d
        jl        ..B1.8
..B1.10:
        vmovups   112(%rsp), %xmm8
	.cfi_restore 25
        vmovups   96(%rsp), %xmm9
	.cfi_restore 26
        vmovups   80(%rsp), %xmm10
	.cfi_restore 27
        vmovups   64(%rsp), %xmm11
	.cfi_restore 28
        vmovups   48(%rsp), %xmm12
	.cfi_restore 29
        vmovups   32(%rsp), %xmm13
	.cfi_restore 30
        vmovups   16(%rsp), %xmm14
	.cfi_restore 31
        vmovups   (%rsp), %xmm15
	.cfi_restore 32
        movq      136(%rsp), %rsi
	.cfi_restore 4
        movq      128(%rsp), %rdi
	.cfi_restore 5
        movq      152(%rsp), %r12
	.cfi_restore 12
        movq      144(%rsp), %r13
	.cfi_restore 13
        vmovups   256(%rsp), %xmm0
        jmp       ..B1.2
	.cfi_escape 0x10, 0x04, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0x48, 0xff, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0x05, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0x40, 0xff, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0x0c, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0x58, 0xff, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0x0d, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0x50, 0xff, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0x19, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0x30, 0xff, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0x1a, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0x20, 0xff, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0x1b, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0x10, 0xff, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0x1c, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0x00, 0xff, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0x1d, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0xf0, 0xfe, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0x1e, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0xe0, 0xfe, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0x1f, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0xd0, 0xfe, 0xff, 0xff, 0x22
	.cfi_escape 0x10, 0x20, 0x0e, 0x38, 0x1c, 0x0d, 0xc0, 0xff, 0xff, 0xff, 0x1a, 0x0d, 0xc0, 0xfe, 0xff, 0xff, 0x22
..B1.11:
        lea       192(%rsp,%r12,4), %rdi
        lea       256(%rsp,%r12,4), %rsi
..___tag_value___svml_expf4_ha_l9.69:
        call      __svml_sexp_ha_cout_rare_internal
..___tag_value___svml_expf4_ha_l9.70:
        jmp       ..B1.9
        .align    16,0x90
	.cfi_endproc
	.type	__svml_expf4_ha_l9,@function
	.size	__svml_expf4_ha_l9,.-__svml_expf4_ha_l9
..LN__svml_expf4_ha_l9.0:
	.data
# -- End  __svml_expf4_ha_l9
	.section .rodata, "a"
	.align 16
	.align 16
.L_2il0floatpacket.14:
	.long	0x7f800000,0x7f800000,0x7f800000,0x7f800000
	.type	.L_2il0floatpacket.14,@object
	.size	.L_2il0floatpacket.14,16
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
