/*
 * Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved.
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
 */

/*
 * @test
 * @compile VectorUtils.java VectorizedHashCode.java MachineCodeSnippetSamples.java
 * @modules java.base/jdk.internal.misc
 * @run main/othervm panama.snippets.MachineCodeSnippetSamples
 */

package panama.snippets;

import jdk.vm.ci.panama.MachineCodeSnippet;

import jdk.vm.ci.panama.amd64.CPUID;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.Objects;

import static jdk.vm.ci.panama.MachineCodeSnippet.requires;
import static jdk.vm.ci.panama.MachineCodeSnippet.Effect.*;
import static jdk.vm.ci.amd64.AMD64.CPUFeature.*;

import static panama.snippets.VectorUtils.*;

public class MachineCodeSnippetSamples {

    static final MethodHandle mov256MH = MachineCodeSnippet.make("move256",
            MethodType.methodType(void.class,                  // return type
                    Object.class /*rdi*/, long.class /*rsi*/,  // src
                    Object.class /*rdx*/, long.class /*rcx*/), // dst
            effects(READ_MEMORY, WRITE_MEMORY), // RW
            requires(AVX),
            0xC4, 0xE1, 0x7E, 0x6F, 0x04, 0x37,  // vmovdqu ymm0,[rsi+rdi]
            0xC4, 0xE1, 0x7E, 0x7F, 0x04, 0x0A); // vmovdqu [rdx+rcx],ymm0

    /*
       # {method} {0x115c2f880} 'move256' '(Ljava/lang/Object;JLjava/lang/Object;J)V'
       # parm0:    rsi:rsi   = 'java/lang/Object'
       # parm1:    rdx:rdx   = long
       # parm2:    rcx:rcx   = 'java/lang/Object'
       # parm3:    r8:r8     = long
       #           [sp+0x20]  (sp of caller)

       0x1051bd560: mov    %eax,-0x16000(%rsp)
       0x1051bd567: push   %rbp
       0x1051bd568: sub    $0x10,%rsp

       0x1051bd56c: mov    %rsi,%rdi
       0x1051bd56f: mov    %rdx,%rsi
       0x1051bd572: mov    %rcx,%rdx
       0x1051bd575: mov    %r8,%rcx

       0x1051bd578: vmovdqu (%rdi,%rsi,1),%ymm0
       0x1051bd57e: vmovdqu %ymm0,(%rdx,%rcx,1)

       0x1051bd584: add    $0x10,%rsp
       0x1051bd588: pop    %rbp
       0x1051bd589: test   %eax,-0x4d3d58f(%rip)

       0x1051bd58f: retq
     */
    private static void move256(Object src, long off1, Object dst, long off2) {
        try {
            mov256MH.invokeExact(src, off1, dst, off2);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    static void copy256(byte[] src, int idx1, byte[] dst, int idx2) {
        // Array bounds checks
        if (idx1 + 32 > src.length || idx2 + 32 > dst.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        // Offset computations
        long off1 = Unsafe.ARRAY_BYTE_BASE_OFFSET + idx1;
        long off2 = Unsafe.ARRAY_BYTE_BASE_OFFSET + idx2;

        move256(src, off1, dst, off2);
    }

    public static void testCopy256() {
        byte[] src = new byte[32];
        for (int i = 0; i < src.length; i++) {
            src[i] = (byte)i;
        }
        byte[] dst = new byte[32];

        copy256(src, 0, dst, 0);

        for (int i = 0; i < dst.length; i++) {
            if (src[i] != dst[i]) {
                throw new AssertionError(src[i] + " != " + dst[i]);
            }
        }
    }

    /* $ java -XX:-UseTLAB -XX:CompileCommand=dontinline,*Main::test* -XX:CompileCommand=inline,*VecUtils::* ... Main

      # {method} {0x11532c7e0} 'testVAdd' '(Ljava/lang/Long2;Ljava/lang/Long2;)Ljava/lang/Long2;' in 'Main'
      # parm0:    rsi:rsi   = 'java/lang/Long2'
      # parm1:    rdx:rdx   = 'java/lang/Long2'
      #           [sp+0x40]  (sp of caller)

      0x1049c8b60: mov    %eax,-0x16000(%rsp)
      0x1049c8b67: push   %rbp
      0x1049c8b68: sub    $0x30,%rsp

      0x1049c8b6c: mov    %rdx,(%rsp)
      0x1049c8b70: mov    %rsi,%rbp
      0x1049c8b73: movabs $0x7c0011bc8,%rsi  ;   {metadata('java/lang/Long2')}
      0x1049c8b7d: nop
      0x1049c8b7e: nop
      0x1049c8b7f: callq  0x10496fae0        ;   {runtime_call _new_instance_Java}

      0x1049c8b84: mov    %rax,%rbx
      0x1049c8b87: vmovdqu 0x10(%rbp),%xmm0
      0x1049c8b8c: mov    (%rsp),%r10
      0x1049c8b90: vmovdqu 0x10(%r10),%xmm1

      0x1049c8b96: paddd  %xmm1,%xmm0        ; - VecUtils::add_epi32@5 (line 88)

      0x1049c8b9a: vmovdqu %xmm0,0x10(%rbx)
      0x1049c8b9f: mov    %rbx,%rax
      0x1049c8ba2: add    $0x30,%rsp
      0x1049c8ba6: pop    %rbp
      0x1049c8ba7: test   %eax,-0x4649bad(%rip)

      0x1049c8bad: retq

    @ 2   VecUtils::add_epi32 (47 bytes)   force inline by CompilerOracle
      @ 5   java.lang.invoke.LambdaForm$MH/1612799726::invokeExact_MT (21 bytes)   force inline by annotation
        @ 2   java.lang.invoke.Invokers::checkExactType (30 bytes)   force inline by annotation
          @ 11   java.lang.invoke.MethodHandle::type (5 bytes)   accessor
        @ 6   java.lang.invoke.Invokers::checkCustomized (28 bytes)   force inline by annotation
          @ 6   java.lang.invoke.MethodHandleImpl::isCompileConstant (2 bytes)   (intrinsic)
        @ 17   java.lang.invoke.LambdaForm$BMH/1286084959::reinvoke (44 bytes)   force inline by annotation
          @ 20   java.lang.invoke.LambdaForm$BMH/292938459::reinvoke (16 bytes)   force inline by annotation
            @ 12   java.lang.invoke.LambdaForm$DMH/932583850::invokeStatic__L (13 bytes)   force inline by annotation
              @ 1   java.lang.invoke.DirectMethodHandle::internalMemberName (8 bytes)   force inline by annotation
              @ 9   java.lang.Long2::make (6 bytes)   (intrinsic)
          @ 40   java.lang.invoke.LambdaForm$NMH/99747242::invokeNative_L3_L (15 bytes)   force inline by annotation
            @ 1   java.lang.invoke.NativeMethodHandle::internalNativeEntryPoint (8 bytes)   force inline by annotation
            @ 11   java.lang.invoke.MethodHandle::linkToNative(LLLL)L (0 bytes)   direct native call
     */
    public static Long2 testVAdd(Long2 v1, Long2 v2) {
        return add_epi32(v1, v2);
    }

    /* $ java -XX:-UseTLAB -XX:CompileCommand=dontinline,*Main::test* -XX:CompileCommand=inline,*VecUtils::* ... Main

      # {method} {0x11532c960} 'testVAdd' '(Ljava/lang/Long2;Ljava/lang/Long2;Ljava/lang/Long2;)Ljava/lang/Long2;' in 'test/Main'
      # parm0:    rsi:rsi   = 'java/lang/Long2'
      # parm1:    rdx:rdx   = 'java/lang/Long2'
      # parm2:    rcx:rcx   = 'java/lang/Long2'
      #           [sp+0x40]  (sp of caller)

      0x1049d07e0: mov    %eax,-0x16000(%rsp)
      0x1049d07e7: push   %rbp
      0x1049d07e8: sub    $0x30,%rsp

      0x1049d07ec: mov    %rcx,%rbp
      0x1049d07ef: vmovdqu 0x10(%rdx),%xmm1
      0x1049d07f4: vmovdqu 0x10(%rsi),%xmm0

      0x1049d07f9: paddd  %xmm1,%xmm0        ; - VecUtils::add_epi32@5 (line 88)

      0x1049d07fd: vmovdqu %xmm0,(%rsp)

      0x1049d0802: movabs $0x7c0011bc8,%rsi  ;   {metadata('java/lang/Long2')}
      0x1049d080c: nop
      0x1049d080d: nop
      0x1049d080e: nop
      0x1049d080f: callq  0x10496fae0        ;   {runtime_call _new_instance_Java}

      0x1049d0814: mov    %rax,%rbx
      0x1049d0817: vmovdqu 0x10(%rbp),%xmm1
      0x1049d081c: vmovdqu (%rsp),%xmm0
      0x1049d0821: paddd  %xmm1,%xmm0        ; - VecUtils::add_epi32@5 (line 88)
      0x1049d0825: vmovdqu %xmm0,0x10(%rbx)

      0x1049d082a: mov    %rbx,%rax
      0x1049d082d: add    $0x30,%rsp
      0x1049d0831: pop    %rbp
      0x1049d0832: test   %eax,-0x4651838(%rip)

      0x1049d0838: retq

    @ 2   VecUtils::add_epi32 (47 bytes)   force inline by CompilerOracle
      @ 5   java.lang.invoke.LambdaForm$MH/1612799726::invokeExact_MT (21 bytes)   force inline by annotation
        @ 2   java.lang.invoke.Invokers::checkExactType (30 bytes)   force inline by annotation
          @ 11   java.lang.invoke.MethodHandle::type (5 bytes)   accessor
        @ 6   java.lang.invoke.Invokers::checkCustomized (28 bytes)   force inline by annotation
          @ 6   java.lang.invoke.MethodHandleImpl::isCompileConstant (2 bytes)   (intrinsic)
        @ 17   java.lang.invoke.LambdaForm$BMH/1286084959::reinvoke (44 bytes)   force inline by annotation
          @ 20   java.lang.invoke.LambdaForm$BMH/292938459::reinvoke (16 bytes)   force inline by annotation
            @ 12   java.lang.invoke.LambdaForm$DMH/932583850::invokeStatic__L (13 bytes)   force inline by annotation
              @ 1   java.lang.invoke.DirectMethodHandle::internalMemberName (8 bytes)   force inline by annotation
              @ 9   java.lang.Long2::make (6 bytes)   (intrinsic)
          @ 40   java.lang.invoke.LambdaForm$NMH/99747242::invokeNative_L3_L (15 bytes)   force inline by annotation
            @ 1   java.lang.invoke.NativeMethodHandle::internalNativeEntryPoint (8 bytes)   force inline by annotation
            @ 11   java.lang.invoke.MethodHandle::linkToNative(LLLL)L (0 bytes)   direct native call
    @ 6   VecUtils::add_epi32 (47 bytes)   force inline by CompilerOracle
      @ 5   java.lang.invoke.LambdaForm$MH/1612799726::invokeExact_MT (21 bytes)   force inline by annotation
        @ 2   java.lang.invoke.Invokers::checkExactType (30 bytes)   force inline by annotation
          @ 11   java.lang.invoke.MethodHandle::type (5 bytes)   accessor
        @ 6   java.lang.invoke.Invokers::checkCustomized (28 bytes)   force inline by annotation
          @ 6   java.lang.invoke.MethodHandleImpl::isCompileConstant (2 bytes)   (intrinsic)
        @ 17   java.lang.invoke.LambdaForm$BMH/1286084959::reinvoke (44 bytes)   force inline by annotation
          @ 20   java.lang.invoke.LambdaForm$BMH/292938459::reinvoke (16 bytes)   force inline by annotation
            @ 12   java.lang.invoke.LambdaForm$DMH/932583850::invokeStatic__L (13 bytes)   force inline by annotation
              @ 1   java.lang.invoke.DirectMethodHandle::internalMemberName (8 bytes)   force inline by annotation
              @ 9   java.lang.Long2::make (6 bytes)   (intrinsic)
          @ 40   java.lang.invoke.LambdaForm$NMH/99747242::invokeNative_L3_L (15 bytes)   force inline by annotation
            @ 1   java.lang.invoke.NativeMethodHandle::internalNativeEntryPoint (8 bytes)   force inline by annotation
            @ 11   java.lang.invoke.MethodHandle::linkToNative(LLLL)L (0 bytes)   direct native call
    */
    public static Long2 testVAdd(Long2 v1, Long2 v2, Long2 v3) {
        return add_epi32(add_epi32(v1, v2), v3);
    }

    /* $ java -XX:-UseTLAB -XX:CompileCommand=dontinline,*Main::test* -XX:CompileCommand=inline,*VecUtils::* ... Main

      # {method} {0x115b2c0e8} 'testVAdd' '(Ljava/lang/Long4;Ljava/lang/Long4;)Ljava/lang/Long4;' in 'Main'
      # parm0:    rsi:rsi   = 'java/lang/Long4'
      # parm1:    rdx:rdx   = 'java/lang/Long4'
      #           [sp+0x30]  (sp of caller)

      0x1051c9a60: mov    %eax,-0x16000(%rsp)
      0x1051c9a67: push   %rbp
      0x1051c9a68: sub    $0x20,%rsp

      0x1051c9a6c: mov    %rdx,(%rsp)
      0x1051c9a70: mov    %rsi,%rbp

      0x1051c9a73: movabs $0x7c0011dc8,%rsi  ;   {metadata('java/lang/Long4')}
      0x1051c9a7d: nop
      0x1051c9a7e: nop
      0x1051c9a7f: nop
      0x1051c9a80: vzeroupper
      0x1051c9a83: callq  0x10516f260        ;   {runtime_call _new_instance_Java}

      0x1051c9a88: mov    %rax,%rbx
      0x1051c9a8b: vmovdqu 0x10(%rbp),%ymm0
      0x1051c9a90: mov    (%rsp),%r10
      0x1051c9a94: vmovdqu 0x10(%r10),%ymm1

      0x1051c9a9a: vpaddd %ymm0,%ymm1,%ymm0  ; - VecUtils::vadd@5 (line 447)

      0x1051c9a9e: vmovdqu %ymm0,0x10(%rbx)
      0x1051c9aa3: mov    %rbx,%rax

      0x1051c9aa6: vzeroupper
      0x1051c9aa9: add    $0x20,%rsp
      0x1051c9aad: pop    %rbp
      0x1051c9aae: test   %eax,-0x4d47ab4(%rip)

      0x1051c9ab4: retq

    @ 2   VecUtils::vadd (19 bytes)   force inline by CompilerOracle
      @ 5   java.lang.invoke.LambdaForm$MH/977993101::invokeExact_MT (21 bytes)   force inline by annotation
        @ 2   java.lang.invoke.Invokers::checkExactType (30 bytes)   force inline by annotation
          @ 11   java.lang.invoke.MethodHandle::type (5 bytes)   accessor
        @ 6   java.lang.invoke.Invokers::checkCustomized (28 bytes)   force inline by annotation
          @ 6   java.lang.invoke.MethodHandleImpl::isCompileConstant (2 bytes)   (intrinsic)
        @ 17   java.lang.invoke.LambdaForm$BMH/5592464::reinvoke (44 bytes)   force inline by annotation
          @ 20   java.lang.invoke.LambdaForm$BMH/1608446010::reinvoke (16 bytes)   force inline by annotation
            @ 12   java.lang.invoke.LambdaForm$DMH/1865127310::invokeStatic__L (13 bytes)   force inline by annotation
              @ 1   java.lang.invoke.DirectMethodHandle::internalMemberName (8 bytes)   force inline by annotation
              @ 9   java.lang.Long4::make (8 bytes)   (intrinsic)
          @ 40   java.lang.invoke.LambdaForm$NMH/804611486::invokeNative_L3_L (15 bytes)   force inline by annotation
            @ 1   java.lang.invoke.NativeMethodHandle::internalNativeEntryPoint (8 bytes)   force inline by annotation
            @ 11   java.lang.invoke.MethodHandle::linkToNative(LLLL)L (0 bytes)   direct native call
    */
    public static Long4 testVAdd(Long4 v1, Long4 v2) {
        return vadd(v1, v2);
    }


    /* $ java -XX:-UseTLAB -XX:CompileCommand=dontinline,*Main::test* -XX:CompileCommand=inline,*VecUtils::* ... Main

      # {method} {0x11532ca28} 'testVAdd' '(Ljava/lang/Long4;Ljava/lang/Long4;Ljava/lang/Long4;)Ljava/lang/Long4;' in 'Main'
      # parm0:    rsi:rsi   = 'java/lang/Long4'
      # parm1:    rdx:rdx   = 'java/lang/Long4'
      # parm2:    rcx:rcx   = 'java/lang/Long4'
      #           [sp+0x40]  (sp of caller)

      0x1049d28e0: mov    %eax,-0x16000(%rsp)
      0x1049d28e7: push   %rbp
      0x1049d28e8: sub    $0x30,%rsp

      0x1049d28ec: mov    %rcx,%rbp
      0x1049d28ef: vmovdqu 0x10(%rdx),%ymm1
      0x1049d28f4: vmovdqu 0x10(%rsi),%ymm0
      0x1049d28f9: vpaddd %ymm0,%ymm1,%ymm0  ; - VecUtils::vadd@5 (line 444)
      0x1049d28fd: vmovdqu %ymm0,(%rsp)

      0x1049d2902: movabs $0x7c0011dc8,%rsi  ;   {metadata('java/lang/Long4')}
      0x1049d290c: vzeroupper
      0x1049d290f: callq  0x10496fae0        ;   {runtime_call _new_instance_Java}

      0x1049d2914: mov    %rax,%rbx
      0x1049d2917: vmovdqu 0x10(%rbp),%ymm1
      0x1049d291c: vmovdqu (%rsp),%ymm0

      0x1049d2921: vpaddd %ymm0,%ymm1,%ymm0  ; - VecUtils::vadd@5 (line 444)

      0x1049d2925: vmovdqu %ymm0,0x10(%rbx)

      0x1049d292a: mov    %rbx,%rax

      0x1049d292d: vzeroupper
      0x1049d2930: add    $0x30,%rsp
      0x1049d2934: pop    %rbp
      0x1049d2935: test   %eax,-0x465393b(%rip)

      0x1049d293b: retq

    @ 2   VecUtils::vadd (19 bytes)   force inline by CompilerOracle
      @ 5   java.lang.invoke.LambdaForm$MH/1612799726::invokeExact_MT (21 bytes)   force inline by annotation
        @ 2   java.lang.invoke.Invokers::checkExactType (30 bytes)   force inline by annotation
          @ 11   java.lang.invoke.MethodHandle::type (5 bytes)   accessor
        @ 6   java.lang.invoke.Invokers::checkCustomized (28 bytes)   force inline by annotation
          @ 6   java.lang.invoke.MethodHandleImpl::isCompileConstant (2 bytes)   (intrinsic)
        @ 17   java.lang.invoke.LambdaForm$BMH/1639622804::reinvoke (44 bytes)   force inline by annotation
          @ 20   java.lang.invoke.LambdaForm$BMH/292938459::reinvoke (16 bytes)   force inline by annotation
            @ 12   java.lang.invoke.LambdaForm$DMH/932583850::invokeStatic__L (13 bytes)   force inline by annotation
              @ 1   java.lang.invoke.DirectMethodHandle::internalMemberName (8 bytes)   force inline by annotation
              @ 9   java.lang.Long4::make (8 bytes)   (intrinsic)
          @ 40   java.lang.invoke.LambdaForm$NMH/99747242::invokeNative_L3_L (15 bytes)   force inline by annotation
            @ 1   java.lang.invoke.NativeMethodHandle::internalNativeEntryPoint (8 bytes)   force inline by annotation
            @ 11   java.lang.invoke.MethodHandle::linkToNative(LLLL)L (0 bytes)   direct native call
    @ 6   VecUtils::vadd (19 bytes)   force inline by CompilerOracle
      @ 5   java.lang.invoke.LambdaForm$MH/1612799726::invokeExact_MT (21 bytes)   force inline by annotation
        @ 2   java.lang.invoke.Invokers::checkExactType (30 bytes)   force inline by annotation
          @ 11   java.lang.invoke.MethodHandle::type (5 bytes)   accessor
        @ 6   java.lang.invoke.Invokers::checkCustomized (28 bytes)   force inline by annotation
          @ 6   java.lang.invoke.MethodHandleImpl::isCompileConstant (2 bytes)   (intrinsic)
        @ 17   java.lang.invoke.LambdaForm$BMH/1639622804::reinvoke (44 bytes)   force inline by annotation
          @ 20   java.lang.invoke.LambdaForm$BMH/292938459::reinvoke (16 bytes)   force inline by annotation
            @ 12   java.lang.invoke.LambdaForm$DMH/932583850::invokeStatic__L (13 bytes)   force inline by annotation
              @ 1   java.lang.invoke.DirectMethodHandle::internalMemberName (8 bytes)   force inline by annotation
              @ 9   java.lang.Long4::make (8 bytes)   (intrinsic)
          @ 40   java.lang.invoke.LambdaForm$NMH/99747242::invokeNative_L3_L (15 bytes)   force inline by annotation
            @ 1   java.lang.invoke.NativeMethodHandle::internalNativeEntryPoint (8 bytes)   force inline by annotation
            @ 11   java.lang.invoke.MethodHandle::linkToNative(LLLL)L (0 bytes)   direct native call
    */
    public static Long4 testVAdd3(Long4 v1, Long4 v2, Long4 v3) {
        return vadd(vadd(v1, v2), v3);
    }

    public static Long4 testVAdd4(Long4 v1, Long4 v2, Long4 v3, Long4 v4) {
        Long4 t1 = vadd(v1, v2);
        Long4 t2 = vadd(v3, v4);
        Long4 t3 = vadd(t1, t2);
        return vadd(t3, v1);
    }

    /* $ java -XX:-UseTLAB -XX:CompileCommand=dontinline,*Main::test* -XX:CompileCommand=inline,*VecUtils::* ... Main

      # {method} {0x115b2c268} 'testShuffle' '(Ljava/lang/Long2;Ljava/lang/Long2;)Ljava/lang/Long2;' in 'Main'
      # parm0:    rsi:rsi   = 'java/lang/Long2'
      # parm1:    rdx:rdx   = 'java/lang/Long2'
      #           [sp+0x30]  (sp of caller)

      0x1051d25e0: mov    %eax,-0x16000(%rsp)
      0x1051d25e7: push   %rbp
      0x1051d25e8: sub    $0x20,%rsp

      0x1051d25ec: mov    %rdx,(%rsp)
      0x1051d25f0: mov    %rsi,%rbp

      0x1051d25f3: movabs $0x7c0011bc8,%rsi  ;   {metadata('java/lang/Long2')}
      0x1051d25fd: nop
      0x1051d25fe: nop
      0x1051d25ff: callq  0x10516f260        ;   {runtime_call _new_instance_Java}

      0x1051d2604: mov    %rax,%rbx
      0x1051d2607: vmovdqu 0x10(%rbp),%xmm0
      0x1051d260c: mov    (%rsp),%r10
      0x1051d2610: vmovdqu 0x10(%r10),%xmm1

      0x1051d2616: pshufb %xmm1,%xmm0        ; - VecUtils::shuffle_epi8@5 (line 31)

      0x1051d261b: vmovdqu %xmm0,0x10(%rbx)

      0x1051d2620: mov    %rbx,%rax
      0x1051d2623: add    $0x20,%rsp
      0x1051d2627: pop    %rbp
      0x1051d2628: test   %eax,-0x4d5062e(%rip)

      0x1051d262e: retq

    @ 2   VecUtils::shuffle_epi8 (21 bytes)   force inline by CompilerOracle
      @ 5   java.lang.invoke.LambdaForm$MH/977993101::invokeExact_MT (21 bytes)   force inline by annotation
        @ 2   java.lang.invoke.Invokers::checkExactType (30 bytes)   force inline by annotation
          @ 11   java.lang.invoke.MethodHandle::type (5 bytes)   accessor
        @ 6   java.lang.invoke.Invokers::checkCustomized (28 bytes)   force inline by annotation
          @ 6   java.lang.invoke.MethodHandleImpl::isCompileConstant (2 bytes)   (intrinsic)
        @ 17   java.lang.invoke.LambdaForm$BMH/380936215::reinvoke (44 bytes)   force inline by annotation
          @ 20   java.lang.invoke.LambdaForm$BMH/1608446010::reinvoke (16 bytes)   force inline by annotation
            @ 12   java.lang.invoke.LambdaForm$DMH/1865127310::invokeStatic__L (13 bytes)   force inline by annotation
              @ 1   java.lang.invoke.DirectMethodHandle::internalMemberName (8 bytes)   force inline by annotation
              @ 9   java.lang.Long2::make (6 bytes)   (intrinsic)
          @ 40   java.lang.invoke.LambdaForm$NMH/804611486::invokeNative_L3_L (15 bytes)   force inline by annotation
            @ 1   java.lang.invoke.NativeMethodHandle::internalNativeEntryPoint (8 bytes)   force inline by annotation
            @ 11   java.lang.invoke.MethodHandle::linkToNative(LLLL)L (0 bytes)   direct native call
    */
    static Long2 testShuffle(Long2 v1, Long2 v2) {
        return shuffle_epi8(v1, v2);
    }

    /* $ java -XX:-UseTLAB -XX:CompileCommand=dontinline,*Main::test* -XX:CompileCommand=inline,*VecUtils::* ... Main
      # {method} {0x115b2c330} 'testCompare128' '(Ljava/lang/Long2;Ljava/lang/Long2;)Z' in 'Main'
      # parm0:    rsi:rsi   = 'java/lang/Long2'
      # parm1:    rdx:rdx   = 'java/lang/Long2'
      #           [sp+0x20]  (sp of caller)

      0x1051d3c60: mov    %eax,-0x16000(%rsp)
      0x1051d3c67: push   %rbp
      0x1051d3c68: sub    $0x10,%rsp

      0x1051d3c6c: vmovdqu 0x10(%rdx),%xmm1
      0x1051d3c71: vmovdqu 0x10(%rsi),%xmm0

      0x1051d3c76: pxor   %xmm1,%xmm0        ; - VecUtils::xor@5 (line 359)

      0x1051d3c7a: xor    %rax,%rax
      0x1051d3c7d: xor    %rcx,%rcx
      0x1051d3c80: mov    $0x1,%cl

      0x1051d3c82: ptest  %xmm0,%xmm0        ; - VecUtils::ptest@4 (line 404)
      0x1051d3c87: cmovne %rcx,%rax          ;

      0x1051d3c8b: add    $0x10,%rsp
      0x1051d3c8f: pop    %rbp
      0x1051d3c90: test   %eax,-0x4d51c96(%rip)

      0x1051d3c96: retq

    @ 2   VecUtils::xor (19 bytes)   force inline by CompilerOracle
      @ 5   java.lang.invoke.LambdaForm$MH/977993101::invokeExact_MT (21 bytes)   force inline by annotation
        @ 2   java.lang.invoke.Invokers::checkExactType (30 bytes)   force inline by annotation
          @ 11   java.lang.invoke.MethodHandle::type (5 bytes)   accessor
        @ 6   java.lang.invoke.Invokers::checkCustomized (28 bytes)   force inline by annotation
          @ 6   java.lang.invoke.MethodHandleImpl::isCompileConstant (2 bytes)   (intrinsic)
        @ 17   java.lang.invoke.LambdaForm$BMH/428746855::reinvoke (44 bytes)   force inline by annotation
          @ 20   java.lang.invoke.LambdaForm$BMH/1608446010::reinvoke (16 bytes)   force inline by annotation
            @ 12   java.lang.invoke.LambdaForm$DMH/1865127310::invokeStatic__L (13 bytes)   force inline by annotation
              @ 1   java.lang.invoke.DirectMethodHandle::internalMemberName (8 bytes)   force inline by annotation
              @ 9   java.lang.Long2::make (6 bytes)   (intrinsic)
          @ 40   java.lang.invoke.LambdaForm$NMH/804611486::invokeNative_L3_L (15 bytes)   force inline by annotation
            @ 1   java.lang.invoke.NativeMethodHandle::internalNativeEntryPoint (8 bytes)   force inline by annotation
            @ 11   java.lang.invoke.MethodHandle::linkToNative(LLLL)L (0 bytes)   direct native call
    @ 7   VecUtils::ptest (18 bytes)   force inline by CompilerOracle
      @ 4   java.lang.invoke.LambdaForm$MH/705265961::invokeExact_MT (20 bytes)   force inline by annotation
        @ 2   java.lang.invoke.Invokers::checkExactType (30 bytes)   force inline by annotation
          @ 11   java.lang.invoke.MethodHandle::type (5 bytes)   accessor
        @ 6   java.lang.invoke.Invokers::checkCustomized (28 bytes)   force inline by annotation
          @ 6   java.lang.invoke.MethodHandleImpl::isCompileConstant (2 bytes)   (intrinsic)
        @ 16   java.lang.invoke.LambdaForm$NMH/317983781::invokeNative_L_I (17 bytes)   force inline by annotation
          @ 7   java.lang.invoke.NativeMethodHandle::internalNativeEntryPoint (8 bytes)   force inline by annotation
          @ 13   java.lang.invoke.MethodHandle::linkToNative(LL)I (0 bytes)   direct native call
    */
    static boolean testCompare128(Long2 v1, Long2 v2) {
        return ptest(xor(v1, v2));
    }

    /* $ java -XX:-UseTLAB -XX:CompileCommand=dontinline,*Main::test* -XX:CompileCommand=inline,*VecUtils::* ... Main

      # {method} {0x115b2c3f8} 'testCompare256' '(Ljava/lang/Long4;Ljava/lang/Long4;)Z' in 'Main'
      # parm0:    rsi:rsi   = 'java/lang/Long4'
      # parm1:    rdx:rdx   = 'java/lang/Long4'
      #           [sp+0x20]  (sp of caller)

      0x1051d7ce0: mov    %eax,-0x16000(%rsp)
      0x1051d7ce7: push   %rbp
      0x1051d7ce8: sub    $0x10,%rsp

      0x1051d7cec: vmovdqu 0x10(%rdx),%ymm1
      0x1051d7cf1: vmovdqu 0x10(%rsi),%ymm0

      0x1051d7cf6: vpxor  %ymm1,%ymm0,%ymm0  ; - VecUtils::xor@5 (line 380)

      0x1051d7cfa: xor    %rax,%rax
      0x1051d7cfd: xor    %rcx,%rcx
      0x1051d7d00: mov    $0x1,%cl

      0x1051d7d02: vptest %ymm0,%ymm0        ; - VecUtils::ptest@4 (line 429)
      0x1051d7d07: cmovne %rcx,%rax          ;

      0x1051d7d0b: vzeroupper
      0x1051d7d0e: add    $0x10,%rsp
      0x1051d7d12: pop    %rbp
      0x1051d7d13: test   %eax,-0x4d55d19(%rip)

      0x1051d7d19: retq

    @ 2   VecUtils::xor (19 bytes)   force inline by CompilerOracle
      @ 5   java.lang.invoke.LambdaForm$MH/977993101::invokeExact_MT (21 bytes)   force inline by annotation
        @ 2   java.lang.invoke.Invokers::checkExactType (30 bytes)   force inline by annotation
          @ 11   java.lang.invoke.MethodHandle::type (5 bytes)   accessor
        @ 6   java.lang.invoke.Invokers::checkCustomized (28 bytes)   force inline by annotation
          @ 6   java.lang.invoke.MethodHandleImpl::isCompileConstant (2 bytes)   (intrinsic)
        @ 17   java.lang.invoke.LambdaForm$BMH/874088044::reinvoke (44 bytes)   force inline by annotation
          @ 20   java.lang.invoke.LambdaForm$BMH/1608446010::reinvoke (16 bytes)   force inline by annotation
            @ 12   java.lang.invoke.LambdaForm$DMH/1865127310::invokeStatic__L (13 bytes)   force inline by annotation
              @ 1   java.lang.invoke.DirectMethodHandle::internalMemberName (8 bytes)   force inline by annotation
              @ 9   java.lang.Long4::make (8 bytes)   (intrinsic)
          @ 40   java.lang.invoke.LambdaForm$NMH/804611486::invokeNative_L3_L (15 bytes)   force inline by annotation
            @ 1   java.lang.invoke.NativeMethodHandle::internalNativeEntryPoint (8 bytes)   force inline by annotation
            @ 11   java.lang.invoke.MethodHandle::linkToNative(LLLL)L (0 bytes)   direct native call
    @ 7   VecUtils::ptest (18 bytes)   force inline by CompilerOracle
      @ 4   java.lang.invoke.LambdaForm$MH/705265961::invokeExact_MT (20 bytes)   force inline by annotation
        @ 2   java.lang.invoke.Invokers::checkExactType (30 bytes)   force inline by annotation
          @ 11   java.lang.invoke.MethodHandle::type (5 bytes)   accessor
        @ 6   java.lang.invoke.Invokers::checkCustomized (28 bytes)   force inline by annotation
          @ 6   java.lang.invoke.MethodHandleImpl::isCompileConstant (2 bytes)   (intrinsic)
        @ 16   java.lang.invoke.LambdaForm$NMH/104739310::invokeNative_L_I (17 bytes)   force inline by annotation
          @ 7   java.lang.invoke.NativeMethodHandle::internalNativeEntryPoint (8 bytes)   force inline by annotation
          @ 13   java.lang.invoke.MethodHandle::linkToNative(LL)I (0 bytes)   direct native call
    */
    static boolean testCompare256(Long4 v1, Long4 v2) {
        return ptest(xor(v1, v2));
    }

    /* $ java -XX:-UseTLAB -XX:CompileCommand=dontinline,*Main::test* -XX:CompileCommand=inline,*VecUtils::* ... Main

      # {method} {0x115b2c4d8} 'testVHashIter' '(Ljava/lang/Long4;J)Ljava/lang/Long4;' in 'Main'
      # parm0:    rsi:rsi   = 'java/lang/Long4'
      # parm1:    rdx:rdx   = long
      #           [sp+0x60]  (sp of caller)

      0x1051df260: mov    %eax,-0x16000(%rsp)
      0x1051df267: push   %rbp
      0x1051df268: sub    $0x50,%rsp

      0x1051df26c: mov    %rdx,(%rsp)
      0x1051df270: vmovdqu 0x10(%rsi),%ymm0
      0x1051df275: movabs $0x6cf350f20,%rbx  ;   {oop(a 'java/lang/Long2')}
      0x1051df27f: movabs $0x6cfd057d8,%rbp  ;   {oop(a 'java/lang/Long2')}
      0x1051df289: movabs $0x6cfd057f8,%r13  ;   {oop(a 'java/lang/Long2')}
      0x1051df293: movabs $0x6cf359618,%r14  ;   {oop(a 'java/lang/Long4')}
      0x1051df29d: movabs $0x6cfd057a8,%r10  ;   {oop(a 'java/lang/Long4')}
      0x1051df2a7: vmovdqu 0x10(%r10),%ymm1

      0x1051df2ad: vpmulld %ymm1,%ymm0,%ymm0 ; - VecUtils::mullo_epi32@5 (line 218)

      0x1051df2b2: vmovdqu %ymm0,0x20(%rsp)
      0x1051df2b8: vmovdqu 0x10(%rbx),%xmm0
      0x1051df2bd: mov    (%rsp),%rdx

      0x1051df2c1: pinsrq $0x0,%rdx,%xmm0    ; - VecUtils::insert_epi64@33 (line 304)

      0x1051df2c8: vmovdqu %xmm0,0x10(%rsp)
      0x1051df2ce: vmovdqu 0x10(%rbp),%xmm1
      0x1051df2d3: vmovdqu 0x10(%rsp),%xmm0

      0x1051df2d9: pshufb %xmm1,%xmm0        ; - VecUtils::shuffle_epi8@5 (line 31)

      0x1051df2de: vmovdqu %xmm0,(%rsp)
      0x1051df2e3: vmovdqu 0x10(%r13),%xmm1
      0x1051df2e9: vmovdqu 0x10(%rsp),%xmm0

      0x1051df2ef: pshufb %xmm1,%xmm0        ; - VecUtils::shuffle_epi8@5 (line 31)

      0x1051df2f4: vmovdqu %xmm0,0x10(%rsp)
      0x1051df2fa: vmovdqu 0x10(%r14),%ymm0
      0x1051df300: vmovdqu (%rsp),%xmm1

      0x1051df305: vinserti128 $0x0,%xmm1,%ymm0,%ymm0 ; - VecUtils::insert_m128@42 (line 337)

      0x1051df30b: vmovdqu 0x10(%rsp),%xmm1

      0x1051df311: vinserti128 $0x1,%xmm1,%ymm0,%ymm0 ; - VecUtils::insert_m128@42 (line 337)

      0x1051df317: movabs $0x6cfd05778,%r10  ;   {oop(a 'java/lang/Long4')}
      0x1051df321: vmovdqu 0x10(%r10),%ymm1

      0x1051df327: vpmulld %ymm1,%ymm0,%ymm0 ; - VecUtils::mullo_epi32@5 (line 218)

      0x1051df32c: vmovdqu %ymm0,(%rsp)
      0x1051df331: movabs $0x7c0011dc8,%rsi  ;   {metadata('java/lang/Long4')}
      0x1051df33b: nop
      0x1051df33c: vzeroupper
      0x1051df33f: callq  0x10516fae0        ;   {runtime_call _new_instance_Java}

      0x1051df344: mov    %rax,%rbx
      0x1051df347: vmovdqu 0x20(%rsp),%ymm0
      0x1051df34d: vmovdqu (%rsp),%ymm1

      0x1051df352: vpaddd %ymm1,%ymm0,%ymm0 ; - VecUtils::add_epi32@5 (line 121)

      0x1051df356: vmovdqu %ymm0,0x10(%rbx)
      0x1051df35b: mov    %rbx,%rax

      0x1051df35e: vzeroupper
      0x1051df361: add    $0x50,%rsp
      0x1051df365: pop    %rbp
      0x1051df366: test   %eax,-0x4e6036c(%rip)

      0x1051df36c: retq

    @ 4   VecUtils::mullo_epi32 (47 bytes)   force inline by CompilerOracle
      @ 5   java.lang.invoke.LambdaForm$MH/977993101::invokeExact_MT (21 bytes)   force inline by annotation
        @ 2   java.lang.invoke.Invokers::checkExactType (30 bytes)   force inline by annotation
          @ 11   java.lang.invoke.MethodHandle::type (5 bytes)   accessor
        @ 6   java.lang.invoke.Invokers::checkCustomized (28 bytes)   force inline by annotation
          @ 6   java.lang.invoke.MethodHandleImpl::isCompileConstant (2 bytes)   (intrinsic)
        @ 17   java.lang.invoke.LambdaForm$BMH/105704967::reinvoke (44 bytes)   force inline by annotation
          @ 20   java.lang.invoke.LambdaForm$BMH/1608446010::reinvoke (16 bytes)   force inline by annotation
            @ 12   java.lang.invoke.LambdaForm$DMH/1865127310::invokeStatic__L (13 bytes)   force inline by annotation
              @ 1   java.lang.invoke.DirectMethodHandle::internalMemberName (8 bytes)   force inline by annotation
              @ 9   java.lang.Long4::make (8 bytes)   (intrinsic)
          @ 40   java.lang.invoke.LambdaForm$NMH/804611486::invokeNative_L3_L (15 bytes)   force inline by annotation
            @ 1   java.lang.invoke.NativeMethodHandle::internalNativeEntryPoint (8 bytes)   force inline by annotation
            @ 11   java.lang.invoke.MethodHandle::linkToNative(LLLL)L (0 bytes)   direct native call
    @ 9   VHash::load_v8qi_to_v8si (37 bytes)   inline (hot)
      @ 5   VecUtils::insert_epi64 (85 bytes)   force inline by CompilerOracle
        @ 33   java.lang.invoke.LambdaForm$MH/1464642111::invokeExact_MT (22 bytes)   force inline by annotation
          @ 3   java.lang.invoke.Invokers::checkExactType (30 bytes)   force inline by annotation
            @ 11   java.lang.invoke.MethodHandle::type (5 bytes)   accessor
          @ 7   java.lang.invoke.Invokers::checkCustomized (28 bytes)   force inline by annotation
            @ 6   java.lang.invoke.MethodHandleImpl::isCompileConstant (2 bytes)   (intrinsic)
          @ 18   java.lang.invoke.LambdaForm$BMH/392292416::reinvoke (46 bytes)   force inline by annotation
            @ 22   java.lang.invoke.LambdaForm$BMH/1608446010::reinvoke (16 bytes)   force inline by annotation
              @ 12   java.lang.invoke.LambdaForm$DMH/1865127310::invokeStatic__L (13 bytes)   force inline by annotation
                @ 1   java.lang.invoke.DirectMethodHandle::internalMemberName (8 bytes)   force inline by annotation
                @ 9   java.lang.Long2::make (6 bytes)   (intrinsic)
            @ 42   java.lang.invoke.LambdaForm$NMH/1395089624::invokeNative_LLJ_L (15 bytes)   force inline by annotation
              @ 1   java.lang.invoke.NativeMethodHandle::internalNativeEntryPoint (8 bytes)   force inline by annotation
              @ 11   java.lang.invoke.MethodHandle::linkToNative(LLJL)L (0 bytes)   direct native call
      @ 15   VecUtils::shuffle_epi8 (21 bytes)   force inline by CompilerOracle
        @ 5   java.lang.invoke.LambdaForm$MH/977993101::invokeExact_MT (21 bytes)   force inline by annotation
          @ 2   java.lang.invoke.Invokers::checkExactType (30 bytes)   force inline by annotation
            @ 11   java.lang.invoke.MethodHandle::type (5 bytes)   accessor
          @ 6   java.lang.invoke.Invokers::checkCustomized (28 bytes)   force inline by annotation
            @ 6   java.lang.invoke.MethodHandleImpl::isCompileConstant (2 bytes)   (intrinsic)
          @ 17   java.lang.invoke.LambdaForm$BMH/380936215::reinvoke (44 bytes)   force inline by annotation
            @ 20   java.lang.invoke.LambdaForm$BMH/1608446010::reinvoke (16 bytes)   force inline by annotation
              @ 12   java.lang.invoke.LambdaForm$DMH/1865127310::invokeStatic__L (13 bytes)   force inline by annotation
                @ 1   java.lang.invoke.DirectMethodHandle::internalMemberName (8 bytes)   force inline by annotation
                @ 9   java.lang.Long2::make (6 bytes)   (intrinsic)
            @ 40   java.lang.invoke.LambdaForm$NMH/804611486::invokeNative_L3_L (15 bytes)   force inline by annotation
              @ 1   java.lang.invoke.NativeMethodHandle::internalNativeEntryPoint (8 bytes)   force inline by annotation
              @ 11   java.lang.invoke.MethodHandle::linkToNative(LLLL)L (0 bytes)   direct native call
      @ 23   VecUtils::shuffle_epi8 (21 bytes)   force inline by CompilerOracle
        @ 5   java.lang.invoke.LambdaForm$MH/977993101::invokeExact_MT (21 bytes)   force inline by annotation
          @ 2   java.lang.invoke.Invokers::checkExactType (30 bytes)   force inline by annotation
            @ 11   java.lang.invoke.MethodHandle::type (5 bytes)   accessor
          @ 6   java.lang.invoke.Invokers::checkCustomized (28 bytes)   force inline by annotation
            @ 6   java.lang.invoke.MethodHandleImpl::isCompileConstant (2 bytes)   (intrinsic)
          @ 17   java.lang.invoke.LambdaForm$BMH/380936215::reinvoke (44 bytes)   force inline by annotation
            @ 20   java.lang.invoke.LambdaForm$BMH/1608446010::reinvoke (16 bytes)   force inline by annotation
              @ 12   java.lang.invoke.LambdaForm$DMH/1865127310::invokeStatic__L (13 bytes)   force inline by annotation
                @ 1   java.lang.invoke.DirectMethodHandle::internalMemberName (8 bytes)   force inline by annotation
                @ 9   java.lang.Long2::make (6 bytes)   (intrinsic)
            @ 40   java.lang.invoke.LambdaForm$NMH/804611486::invokeNative_L3_L (15 bytes)   force inline by annotation
              @ 1   java.lang.invoke.NativeMethodHandle::internalNativeEntryPoint (8 bytes)   force inline by annotation
              @ 11   java.lang.invoke.MethodHandle::linkToNative(LLLL)L (0 bytes)   direct native call
      @ 29   VecUtils::pack (117 bytes)   force inline by CompilerOracle
        @ 7   VecUtils::insert_m128 (83 bytes)   force inline by CompilerOracle
          @ 33   java.lang.invoke.LambdaForm$MH/977993101::invokeExact_MT (21 bytes)   force inline by annotation
            @ 2   java.lang.invoke.Invokers::checkExactType (30 bytes)   force inline by annotation
              @ 11   java.lang.invoke.MethodHandle::type (5 bytes)   accessor
            @ 6   java.lang.invoke.Invokers::checkCustomized (28 bytes)   force inline by annotation
              @ 6   java.lang.invoke.MethodHandleImpl::isCompileConstant (2 bytes)   (intrinsic)
            @ 17   java.lang.invoke.LambdaForm$BMH/1818402158::reinvoke (44 bytes)   force inline by annotation
              @ 20   java.lang.invoke.LambdaForm$BMH/1608446010::reinvoke (16 bytes)   force inline by annotation
                @ 12   java.lang.invoke.LambdaForm$DMH/1865127310::invokeStatic__L (13 bytes)   force inline by annotation
                  @ 1   java.lang.invoke.DirectMethodHandle::internalMemberName (8 bytes)   force inline by annotation
                  @ 9   java.lang.Long4::make (8 bytes)   (intrinsic)
              @ 40   java.lang.invoke.LambdaForm$NMH/804611486::invokeNative_L3_L (15 bytes)   force inline by annotation
                @ 1   java.lang.invoke.NativeMethodHandle::internalNativeEntryPoint (8 bytes)   force inline by annotation
                @ 11   java.lang.invoke.MethodHandle::linkToNative(LLLL)L (0 bytes)   direct native call
        @ 14   VecUtils::insert_m128 (83 bytes)   force inline by CompilerOracle
          @ 42   java.lang.invoke.LambdaForm$MH/977993101::invokeExact_MT (21 bytes)   force inline by annotation
            @ 2   java.lang.invoke.Invokers::checkExactType (30 bytes)   force inline by annotation
              @ 11   java.lang.invoke.MethodHandle::type (5 bytes)   accessor
            @ 6   java.lang.invoke.Invokers::checkCustomized (28 bytes)   force inline by annotation
              @ 6   java.lang.invoke.MethodHandleImpl::isCompileConstant (2 bytes)   (intrinsic)
            @ 17   java.lang.invoke.LambdaForm$BMH/1590550415::reinvoke (44 bytes)   force inline by annotation
              @ 20   java.lang.invoke.LambdaForm$BMH/1608446010::reinvoke (16 bytes)   force inline by annotation
                @ 12   java.lang.invoke.LambdaForm$DMH/1865127310::invokeStatic__L (13 bytes)   force inline by annotation
                  @ 1   java.lang.invoke.DirectMethodHandle::internalMemberName (8 bytes)   force inline by annotation
                  @ 9   java.lang.Long4::make (8 bytes)   (intrinsic)
              @ 40   java.lang.invoke.LambdaForm$NMH/804611486::invokeNative_L3_L (15 bytes)   force inline by annotation
                @ 1   java.lang.invoke.NativeMethodHandle::internalNativeEntryPoint (8 bytes)   force inline by annotation
                @ 11   java.lang.invoke.MethodHandle::linkToNative(LLLL)L (0 bytes)   direct native call
    @ 17   VecUtils::mullo_epi32 (47 bytes)   force inline by CompilerOracle
      @ 5   java.lang.invoke.LambdaForm$MH/977993101::invokeExact_MT (21 bytes)   force inline by annotation
        @ 2   java.lang.invoke.Invokers::checkExactType (30 bytes)   force inline by annotation
          @ 11   java.lang.invoke.MethodHandle::type (5 bytes)   accessor
        @ 6   java.lang.invoke.Invokers::checkCustomized (28 bytes)   force inline by annotation
          @ 6   java.lang.invoke.MethodHandleImpl::isCompileConstant (2 bytes)   (intrinsic)
        @ 17   java.lang.invoke.LambdaForm$BMH/105704967::reinvoke (44 bytes)   force inline by annotation
          @ 20   java.lang.invoke.LambdaForm$BMH/1608446010::reinvoke (16 bytes)   force inline by annotation
            @ 12   java.lang.invoke.LambdaForm$DMH/1865127310::invokeStatic__L (13 bytes)   force inline by annotation
              @ 1   java.lang.invoke.DirectMethodHandle::internalMemberName (8 bytes)   force inline by annotation
              @ 9   java.lang.Long4::make (8 bytes)   (intrinsic)
          @ 40   java.lang.invoke.LambdaForm$NMH/804611486::invokeNative_L3_L (15 bytes)   force inline by annotation
            @ 1   java.lang.invoke.NativeMethodHandle::internalNativeEntryPoint (8 bytes)   force inline by annotation
            @ 11   java.lang.invoke.MethodHandle::linkToNative(LLLL)L (0 bytes)   direct native call
    @ 23   VecUtils::add_epi32 (47 bytes)   force inline by CompilerOracle
      @ 5   java.lang.invoke.LambdaForm$MH/977993101::invokeExact_MT (21 bytes)   force inline by annotation
        @ 2   java.lang.invoke.Invokers::checkExactType (30 bytes)   force inline by annotation
          @ 11   java.lang.invoke.MethodHandle::type (5 bytes)   accessor
        @ 6   java.lang.invoke.Invokers::checkCustomized (28 bytes)   force inline by annotation
          @ 6   java.lang.invoke.MethodHandleImpl::isCompileConstant (2 bytes)   (intrinsic)
        @ 17   java.lang.invoke.LambdaForm$BMH/1058025095::reinvoke (44 bytes)   force inline by annotation
          @ 20   java.lang.invoke.LambdaForm$BMH/1608446010::reinvoke (16 bytes)   force inline by annotation
            @ 12   java.lang.invoke.LambdaForm$DMH/1865127310::invokeStatic__L (13 bytes)   force inline by annotation
              @ 1   java.lang.invoke.DirectMethodHandle::internalMemberName (8 bytes)   force inline by annotation
              @ 9   java.lang.Long4::make (8 bytes)   (intrinsic)
          @ 40   java.lang.invoke.LambdaForm$NMH/804611486::invokeNative_L3_L (15 bytes)   force inline by annotation
            @ 1   java.lang.invoke.NativeMethodHandle::internalNativeEntryPoint (8 bytes)   force inline by annotation
            @ 11   java.lang.invoke.MethodHandle::linkToNative(LLLL)L (0 bytes)   direct native call
    */
    static Long4 testVHashIter(Long4 acc, long ch8) {
        return VectorizedHashCode.vhash8(acc, ch8);
    }

    public static Long2 testVAddRA(Long2 v1, Long2 v2) {
        return VectorUtils.add_epi32(v1, v2);
    }

    public static Long2 testNestedXorRA(Long2 v1, Long2 v2) {
        Long2 v3 = VectorUtils.xor(v1, v2);
        Long2 v4 = VectorUtils.xor(v2, v3);
        Long2 v5 = VectorUtils.xor(v3, v4);
        Long2 v6 = VectorUtils.xor(v4, v5);
        Long2 v7 = VectorUtils.xor(v5, v6);
        return v7;
    }


    static final MethodHandle UNSUPPORTED_MH = MachineCodeSnippet.make("unsupported", MethodType.methodType(void.class), false);
    public static void testUnsupported() {
        try {
            UNSUPPORTED_MH.invokeExact();
            throw new AssertionError("No exception thrown");
        } catch (UnsupportedOperationException e) {
            /* expected*/
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    static final MethodHandle EMPTY_MH = MachineCodeSnippet.make("empty", MethodType.methodType(void.class), true);
    public static void testEmpty() {
        try {
            EMPTY_MH.invokeExact();
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    static final int[] arr1 = new int[] { 1,  2,  3,  4,  5,  6,  7,  8};
    static final int[] arr2 = new int[] { 9, 10, 11, 12, 13, 14, 15, 16};

    static final Long2 V128 = loadL2(arr1, Unsafe.ARRAY_INT_BASE_OFFSET);
    static final Long4 V256 = loadL4(arr2, Unsafe.ARRAY_INT_BASE_OFFSET);

    static final Long2 contents = loadL2(new byte[]{ 1, 2, 3, 4, 5, 6, 7, 8, 0, 0, 0, 0, 0, 0, 0, 0},
                                         Unsafe.ARRAY_BYTE_BASE_OFFSET);
    static final Long2 shuffle  = loadL2(new byte[]{-1, -1, -1, 3, -1, -1, -1, 2, -1, -1, -1, 1, -1, -1, -1, 0},
                                         Unsafe.ARRAY_BYTE_BASE_OFFSET);

    interface CallThrowable <T> {
        T call() throws Throwable;
    }

    static <T> void runUntilCompiled(Runnable r) {
        try {
            for (int i = 0; i < 20_000; i++) {
                r.run();
            }
        } catch (AssertionError e) {
            throw e;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    static <T> void runUntilCompiled(CallThrowable<T> r) {
        try {
            T v = r.call();
            for (int i = 0; i < 20_000; i++) {
                T v1 = r.call();
                if (!Objects.equals(v, v1)) {
                    throw new AssertionError("v=" + v + "; v1=" + v1);
                }
            }
        } catch (AssertionError e) {
            throw e;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static void main(String[] args) throws Throwable {
        if (!CPUID.has(SSE))  return; // Not supported.

        runUntilCompiled(() -> testUnsupported());
        runUntilCompiled(() -> testEmpty());

        if (CPUID.has(SSE2)) {
            runUntilCompiled(() -> testVAdd(V128, V128));
            runUntilCompiled(() -> testVAdd(V128, V128, V128));
        }
        if (CPUID.has(SSE3)) {
            runUntilCompiled(() -> testShuffle(contents, shuffle));
        }
        if (CPUID.has(SSE4_1)) {
            runUntilCompiled(() -> testCompare128(loadL2(arr1, Unsafe.ARRAY_INT_BASE_OFFSET),
                                                  loadL2(arr1, Unsafe.ARRAY_INT_BASE_OFFSET)));

            runUntilCompiled(() -> testCompare128(loadL2(arr1, Unsafe.ARRAY_INT_BASE_OFFSET),
                                                  loadL2(arr2, Unsafe.ARRAY_INT_BASE_OFFSET)));
        }
        if (CPUID.has(AVX)) {
            runUntilCompiled(() -> testCopy256());
            runUntilCompiled(() -> testVAdd(V256, V256));
            runUntilCompiled(() -> testVAdd3(V256, V256, V256));
            runUntilCompiled(() -> testVAdd4(V256, V256, V256, V256));

            runUntilCompiled(() -> testVAddRA(V128, V128));
            runUntilCompiled(() -> testNestedXorRA(V128, V128));
        }
        if (CPUID.has(AVX2)) {
            runUntilCompiled(() -> testCompare256(loadL4(arr2, Unsafe.ARRAY_INT_BASE_OFFSET),
                                                  loadL4(arr2, Unsafe.ARRAY_INT_BASE_OFFSET)));

            runUntilCompiled(() -> testCompare256(loadL4(arr1, Unsafe.ARRAY_INT_BASE_OFFSET),
                                                  loadL4(arr2, Unsafe.ARRAY_INT_BASE_OFFSET)));

            runUntilCompiled(() -> testVHashIter(Long4.ZERO, 0x0102030405060708L));
        }
    }
}


