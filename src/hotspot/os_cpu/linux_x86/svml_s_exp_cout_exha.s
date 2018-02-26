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
.L_2__routine_start___svml_sexp_ha_cout_rare_internal_0:
# -- Begin  __svml_sexp_ha_cout_rare_internal
	.text
       .align    16,0x90
	.hidden __svml_sexp_ha_cout_rare_internal
	.globl __svml_sexp_ha_cout_rare_internal
__svml_sexp_ha_cout_rare_internal:
# parameter 1: %rdi
# parameter 2: %rsi
..B1.1:
        .byte     243
        .byte     15
        .byte     30
        .byte     250
	.cfi_startproc
..___tag_value___svml_sexp_ha_cout_rare_internal.1:
..L2:
        xorl      %eax, %eax
        movzwl    2(%rdi), %edx
        andl      $32640, %edx
        shrl      $7, %edx
        cmpl      $255, %edx
        je        ..B1.17
..B1.2:
        pxor      %xmm7, %xmm7
        cvtss2sd  (%rdi), %xmm7
        cmpl      $74, %edx
        jle       ..B1.15
..B1.3:
        movsd     1080+_vmldExpHATab(%rip), %xmm0
        comisd    %xmm7, %xmm0
        jb        ..B1.14
..B1.4:
        comisd    1096+_vmldExpHATab(%rip), %xmm7
        jb        ..B1.13
..B1.5:
        movsd     1024+_vmldExpHATab(%rip), %xmm1
        movaps    %xmm7, %xmm6
        mulsd     %xmm7, %xmm1
        lea       _vmldExpHATab(%rip), %r10
        movsd     %xmm1, -16(%rsp)
        movsd     -16(%rsp), %xmm2
        movsd     1072+_vmldExpHATab(%rip), %xmm1
        movsd     1136+_vmldExpHATab(%rip), %xmm0
        movsd     %xmm0, -24(%rsp)
        addsd     1032+_vmldExpHATab(%rip), %xmm2
        movsd     %xmm2, -8(%rsp)
        movsd     -8(%rsp), %xmm3
        movl      -8(%rsp), %edi
        movl      %edi, %ecx
        andl      $63, %edi
        subsd     1032+_vmldExpHATab(%rip), %xmm3
        movsd     %xmm3, -16(%rsp)
        lea       1(%rdi,%rdi), %r8d
        movsd     -16(%rsp), %xmm4
        lea       (%rdi,%rdi), %r9d
        mulsd     1104+_vmldExpHATab(%rip), %xmm4
        movsd     -16(%rsp), %xmm5
        subsd     %xmm4, %xmm6
        mulsd     1112+_vmldExpHATab(%rip), %xmm5
        shrl      $6, %ecx
        subsd     %xmm5, %xmm6
        comisd    1088+_vmldExpHATab(%rip), %xmm7
        mulsd     %xmm6, %xmm1
        movsd     (%r10,%r9,8), %xmm0
        lea       1023(%rcx), %edx
        addsd     1064+_vmldExpHATab(%rip), %xmm1
        mulsd     %xmm6, %xmm1
        addsd     1056+_vmldExpHATab(%rip), %xmm1
        mulsd     %xmm6, %xmm1
        addsd     1048+_vmldExpHATab(%rip), %xmm1
        mulsd     %xmm6, %xmm1
        addsd     1040+_vmldExpHATab(%rip), %xmm1
        mulsd     %xmm6, %xmm1
        mulsd     %xmm6, %xmm1
        addsd     %xmm6, %xmm1
        addsd     (%r10,%r8,8), %xmm1
        mulsd     %xmm0, %xmm1
        jb        ..B1.9
..B1.6:
        andl      $2047, %edx
        addsd     %xmm0, %xmm1
        cmpl      $2046, %edx
        ja        ..B1.8
..B1.7:
        movzwl    1142+_vmldExpHATab(%rip), %ecx
        shll      $4, %edx
        andl      $-32753, %ecx
        orl       %edx, %ecx
        movw      %cx, -18(%rsp)
        movsd     -24(%rsp), %xmm0
        mulsd     %xmm0, %xmm1
        cvtsd2ss  %xmm1, %xmm1
        movss     %xmm1, (%rsi)
        ret       
..B1.8:
        decl      %edx
        andl      $2047, %edx
        movzwl    -18(%rsp), %ecx
        shll      $4, %edx
        andl      $-32753, %ecx
        orl       %edx, %ecx
        movw      %cx, -18(%rsp)
        movsd     -24(%rsp), %xmm0
        mulsd     %xmm0, %xmm1
        mulsd     1152+_vmldExpHATab(%rip), %xmm1
        cvtsd2ss  %xmm1, %xmm1
        movss     %xmm1, (%rsi)
        ret       
..B1.9:
        addl      $1083, %ecx
        andl      $2047, %ecx
        movl      %ecx, %eax
        movzwl    -18(%rsp), %edx
        shll      $4, %eax
        andl      $-32753, %edx
        orl       %eax, %edx
        movw      %dx, -18(%rsp)
        movsd     -24(%rsp), %xmm3
        mulsd     %xmm3, %xmm1
        mulsd     %xmm0, %xmm3
        movaps    %xmm1, %xmm2
        addsd     %xmm3, %xmm2
        cmpl      $50, %ecx
        ja        ..B1.11
..B1.10:
        movsd     1160+_vmldExpHATab(%rip), %xmm0
        mulsd     %xmm0, %xmm2
        cvtsd2ss  %xmm2, %xmm2
        jmp       ..B1.12
..B1.11:
        movsd     %xmm2, -72(%rsp)
        movsd     -72(%rsp), %xmm0
        subsd     %xmm0, %xmm3
        movsd     %xmm3, -64(%rsp)
        movsd     -64(%rsp), %xmm2
        addsd     %xmm1, %xmm2
        movsd     %xmm2, -64(%rsp)
        movsd     -72(%rsp), %xmm1
        mulsd     1168+_vmldExpHATab(%rip), %xmm1
        movsd     %xmm1, -56(%rsp)
        movsd     -72(%rsp), %xmm4
        movsd     -56(%rsp), %xmm3
        addsd     %xmm3, %xmm4
        movsd     %xmm4, -48(%rsp)
        movsd     -48(%rsp), %xmm6
        movsd     -56(%rsp), %xmm5
        subsd     %xmm5, %xmm6
        movsd     %xmm6, -40(%rsp)
        movsd     -72(%rsp), %xmm8
        movsd     -40(%rsp), %xmm7
        subsd     %xmm7, %xmm8
        movsd     %xmm8, -32(%rsp)
        movsd     -64(%rsp), %xmm10
        movsd     -32(%rsp), %xmm9
        addsd     %xmm9, %xmm10
        movsd     %xmm10, -32(%rsp)
        movsd     -40(%rsp), %xmm11
        mulsd     1160+_vmldExpHATab(%rip), %xmm11
        movsd     %xmm11, -40(%rsp)
        movsd     -32(%rsp), %xmm12
        mulsd     1160+_vmldExpHATab(%rip), %xmm12
        movsd     %xmm12, -32(%rsp)
        movsd     -40(%rsp), %xmm2
        movsd     -32(%rsp), %xmm13
        addsd     %xmm13, %xmm2
        cvtsd2ss  %xmm2, %xmm2
..B1.12:
        movss     %xmm2, (%rsi)
        movl      $4, %eax
        ret       
..B1.13:
        movsd     1120+_vmldExpHATab(%rip), %xmm0
        movl      $4, %eax
        mulsd     %xmm0, %xmm0
        cvtsd2ss  %xmm0, %xmm0
        movss     %xmm0, (%rsi)
        ret       
..B1.14:
        movsd     1128+_vmldExpHATab(%rip), %xmm0
        movl      $3, %eax
        mulsd     %xmm0, %xmm0
        cvtsd2ss  %xmm0, %xmm0
        movss     %xmm0, (%rsi)
        ret       
..B1.15:
        movsd     1144+_vmldExpHATab(%rip), %xmm0
        addsd     %xmm7, %xmm0
        cvtsd2ss  %xmm0, %xmm0
        movss     %xmm0, (%rsi)
..B1.16:
        ret       
..B1.17:
        movb      3(%rdi), %dl
        andb      $-128, %dl
        cmpb      $-128, %dl
        je        ..B1.19
..B1.18:
        movss     (%rdi), %xmm0
        mulss     %xmm0, %xmm0
        movss     %xmm0, (%rsi)
        ret       
..B1.19:
        testl     $8388607, (%rdi)
        jne       ..B1.18
..B1.20:
        movsd     1136+_vmldExpHATab(%rip), %xmm0
        cvtsd2ss  %xmm0, %xmm0
        movss     %xmm0, (%rsi)
        ret       
        .align    16,0x90
	.cfi_endproc
	.type	__svml_sexp_ha_cout_rare_internal,@function
	.size	__svml_sexp_ha_cout_rare_internal,.-__svml_sexp_ha_cout_rare_internal
..LN__svml_sexp_ha_cout_rare_internal.0:
	.data
# -- End  __svml_sexp_ha_cout_rare_internal
	.section .rodata, "a"
	.align 32
	.align 32
_vmldExpHATab:
	.long	0
	.long	1072693248
	.long	0
	.long	0
	.long	1048019041
	.long	1072704666
	.long	2631457885
	.long	3161546771
	.long	3541402996
	.long	1072716208
	.long	896005651
	.long	1015861842
	.long	410360776
	.long	1072727877
	.long	1642514529
	.long	1012987726
	.long	1828292879
	.long	1072739672
	.long	1568897901
	.long	1016568486
	.long	852742562
	.long	1072751596
	.long	1882168529
	.long	1010744893
	.long	3490863953
	.long	1072763649
	.long	707771662
	.long	3163903570
	.long	2930322912
	.long	1072775834
	.long	3117806614
	.long	3163670819
	.long	1014845819
	.long	1072788152
	.long	3936719688
	.long	3162512149
	.long	3949972341
	.long	1072800603
	.long	1058231231
	.long	1015777676
	.long	828946858
	.long	1072813191
	.long	1044000608
	.long	1016786167
	.long	2288159958
	.long	1072825915
	.long	1151779725
	.long	1015705409
	.long	1853186616
	.long	1072838778
	.long	3819481236
	.long	1016499965
	.long	1709341917
	.long	1072851781
	.long	2552227826
	.long	1015039787
	.long	4112506593
	.long	1072864925
	.long	1829350193
	.long	1015216097
	.long	2799960843
	.long	1072878213
	.long	1913391796
	.long	1015756674
	.long	171030293
	.long	1072891646
	.long	1303423926
	.long	1015238005
	.long	2992903935
	.long	1072905224
	.long	1574172746
	.long	1016061241
	.long	926591435
	.long	1072918951
	.long	3427487848
	.long	3163704045
	.long	887463927
	.long	1072932827
	.long	1049900754
	.long	3161575912
	.long	1276261410
	.long	1072946854
	.long	2804567149
	.long	1015390024
	.long	569847338
	.long	1072961034
	.long	1209502043
	.long	3159926671
	.long	1617004845
	.long	1072975368
	.long	1623370769
	.long	1011049453
	.long	3049340112
	.long	1072989858
	.long	3667985273
	.long	1013894369
	.long	3577096743
	.long	1073004506
	.long	3145379760
	.long	1014403278
	.long	1990012071
	.long	1073019314
	.long	7447438
	.long	3163526196
	.long	1453150082
	.long	1073034283
	.long	3171891295
	.long	3162037958
	.long	917841882
	.long	1073049415
	.long	419288974
	.long	1016280325
	.long	3712504873
	.long	1073064711
	.long	3793507337
	.long	1016095713
	.long	363667784
	.long	1073080175
	.long	728023093
	.long	1016345318
	.long	2956612997
	.long	1073095806
	.long	1005538728
	.long	3163304901
	.long	2186617381
	.long	1073111608
	.long	2018924632
	.long	3163803357
	.long	1719614413
	.long	1073127582
	.long	3210617384
	.long	3163796463
	.long	1013258799
	.long	1073143730
	.long	3094194670
	.long	3160631279
	.long	3907805044
	.long	1073160053
	.long	2119843535
	.long	3161988964
	.long	1447192521
	.long	1073176555
	.long	508946058
	.long	3162904882
	.long	1944781191
	.long	1073193236
	.long	3108873501
	.long	3162190556
	.long	919555682
	.long	1073210099
	.long	2882956373
	.long	1013312481
	.long	2571947539
	.long	1073227145
	.long	4047189812
	.long	3163777462
	.long	2604962541
	.long	1073244377
	.long	3631372142
	.long	3163870288
	.long	1110089947
	.long	1073261797
	.long	3253791412
	.long	1015920431
	.long	2568320822
	.long	1073279406
	.long	1509121860
	.long	1014756995
	.long	2966275557
	.long	1073297207
	.long	2339118633
	.long	3160254904
	.long	2682146384
	.long	1073315202
	.long	586480042
	.long	3163702083
	.long	2191782032
	.long	1073333393
	.long	730975783
	.long	1014083580
	.long	2069751141
	.long	1073351782
	.long	576856675
	.long	3163014404
	.long	2990417245
	.long	1073370371
	.long	3552361237
	.long	3163667409
	.long	1434058175
	.long	1073389163
	.long	1853053619
	.long	1015310724
	.long	2572866477
	.long	1073408159
	.long	2462790535
	.long	1015814775
	.long	3092190715
	.long	1073427362
	.long	1457303226
	.long	3159737305
	.long	4076559943
	.long	1073446774
	.long	950899508
	.long	3160987380
	.long	2420883922
	.long	1073466398
	.long	174054861
	.long	1014300631
	.long	3716502172
	.long	1073486235
	.long	816778419
	.long	1014197934
	.long	777507147
	.long	1073506289
	.long	3507050924
	.long	1015341199
	.long	3706687593
	.long	1073526560
	.long	1821514088
	.long	1013410604
	.long	1242007932
	.long	1073547053
	.long	1073740399
	.long	3163532637
	.long	3707479175
	.long	1073567768
	.long	2789017511
	.long	1014276997
	.long	64696965
	.long	1073588710
	.long	3586233004
	.long	1015962192
	.long	863738719
	.long	1073609879
	.long	129252895
	.long	3162690849
	.long	3884662774
	.long	1073631278
	.long	1614448851
	.long	1014281732
	.long	2728693978
	.long	1073652911
	.long	2413007344
	.long	3163551506
	.long	3999357479
	.long	1073674779
	.long	1101668360
	.long	1015989180
	.long	1533953344
	.long	1073696886
	.long	835814894
	.long	1015702697
	.long	2174652632
	.long	1073719233
	.long	1301400989
	.long	1014466875
	.long	1697350398
	.long	1079448903
	.long	0
	.long	1127743488
	.long	0
	.long	1071644672
	.long	1431652600
	.long	1069897045
	.long	1431670732
	.long	1067799893
	.long	984555731
	.long	1065423122
	.long	472530941
	.long	1062650218
	.long	3758096384
	.long	1079389762
	.long	3758096384
	.long	3226850697
	.long	2147483648
	.long	3227123254
	.long	4277796864
	.long	1065758274
	.long	3164486458
	.long	1025308570
	.long	1
	.long	1048576
	.long	4294967295
	.long	2146435071
	.long	0
	.long	0
	.long	0
	.long	1072693248
	.long	0
	.long	1073741824
	.long	0
	.long	1009778688
	.long	0
	.long	1106771968
	.type	_vmldExpHATab,@object
	.size	_vmldExpHATab,1176
	.data
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
