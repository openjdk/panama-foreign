/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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

#include "precompiled.hpp"
#include "asm/macroAssembler.hpp"
#include "classfile/javaClasses.inline.hpp"
#include "interpreter/interpreter.hpp"
#include "interpreter/interpreterRuntime.hpp"
#include "memory/allocation.inline.hpp"
#include "memory/resourceArea.hpp"
#include "include/jvm.h"
#include "prims/universalUpcallHandler.hpp"
#include "runtime/javaCalls.hpp"
#include "runtime/interfaceSupport.inline.hpp"
#include "logging/log.hpp"
#include "logging/logStream.hpp"
#include "oops/arrayOop.inline.hpp"
#include "runtime/jniHandles.inline.hpp"

static struct {
  bool inited;
  struct {
    Klass* klass;
    Symbol* name;
    Symbol* sig;
  } upcall_method;  // jdk.internal.foreign.abi.UniversalUpcallHandler::invoke
} upcall_info;

#include "classfile/symbolTable.hpp"
// FIXME: This should be initialized explicitly instead of lazily/racily
static void upcall_init(void) {
#if 0
  fprintf(stderr, "upcall_init()\n");
#endif

  TRAPS = Thread::current();
  ResourceMark rm;

  const char* cname = "jdk/internal/foreign/abi/UniversalUpcallHandler";
  const char* mname = "invoke";
  const char* mdesc = "(Ljdk/internal/foreign/abi/UniversalUpcallHandler;JJJJJJ)V";
  Symbol* cname_sym = SymbolTable::lookup(cname, (int)strlen(cname), THREAD);
  Symbol* mname_sym = SymbolTable::lookup(mname, (int)strlen(mname), THREAD);
  Symbol* mdesc_sym = SymbolTable::lookup(mdesc, (int)strlen(mdesc), THREAD);

#if 0
  ::fprintf(stderr, "cname_sym: %p\n", cname_sym);
  ::fprintf(stderr, "mname_sym: %p\n", mname_sym);
  ::fprintf(stderr, "mdesc_sym: %p\n", mdesc_sym);
#endif

  Klass* k = SystemDictionary::resolve_or_null(cname_sym, THREAD);
#if 0
  ::fprintf(stderr, "Klass: %p\n", k);
#endif

  Method* method = k->lookup_method(mname_sym, mdesc_sym);
#if 0
  ::fprintf(stderr, "Method: %p\n", method);
#endif

  upcall_info.upcall_method.klass = k;
  upcall_info.upcall_method.name = mname_sym;
  upcall_info.upcall_method.sig = mdesc_sym;

  upcall_info.inited = true;
}

struct upcall_context {
  struct {
    uintptr_t rbx;
#ifdef _WIN64
    uintptr_t rdi;
    uintptr_t rsi;
#endif
#ifdef _LP64
    uintptr_t r12;
    uintptr_t r13;
    uintptr_t r14;
    uintptr_t r15;
#endif
  } preserved;

  struct {
#ifdef _LP64
    union {
      struct {
#ifndef _WIN64
        uintptr_t rdi;
        uintptr_t rsi;
#endif
#ifdef _WIN64 // rdx and rcx are reversed on windows
        uintptr_t rcx;
        uintptr_t rdx;
#else
        uintptr_t rdx;
        uintptr_t rcx;
#endif
        uintptr_t r8;
        uintptr_t r9;
      } reg;
      uintptr_t regs[INTEGER_ARGUMENT_REGISTERS_NOOF];
    } integer;

    union {
      struct {
        VectorRegister xmm0;
        VectorRegister xmm1;
        VectorRegister xmm2;
        VectorRegister xmm3;
        VectorRegister xmm4;
        VectorRegister xmm5;
        VectorRegister xmm6;
        VectorRegister xmm7;
      } reg;
      VectorRegister regs[VECTOR_ARGUMENT_REGISTERS_NOOF];
    } vector;

    uintptr_t rax;
#endif

    uintptr_t rsp;
  } args;

  struct {
    union {
      struct {
        uintptr_t rax;
#ifdef _LP64
#ifndef _WIN64
        uintptr_t rdx;
#endif
#endif
      } reg;
      uintptr_t regs[INTEGER_RETURN_REGISTERS_NOOF];
    } integer;

    union {
      struct {
        VectorRegister xmm0;
#ifdef _LP64
#ifndef _WIN64
        VectorRegister xmm1;
#endif  
#endif
      } reg;
      VectorRegister regs[VECTOR_RETURN_REGISTERS_NOOF];
    } vector;

    union {
      struct {
        long double st0;
        long double st1;
      } reg;
      long double regs[X87_RETURN_REGISTERS_NOOF];
    } x87;
  } returns;
};

static void upcall_helper(jobject rec, struct upcall_context* context) {
#if 0
  ::fprintf(stderr, "upcall_helper(%p, %p)\n", rec, context);
#endif

  JavaThread* thread = JavaThread::current();

  assert(thread->is_Java_thread(), "really?");

#if 0
  fprintf(stderr, "args.integer.regs: %p\n", context->args.integer.regs);
  for (size_t i = 0; i < INTEGER_ARGUMENT_REGISTERS_NOOF; i++) {
    fprintf(stderr, "args.integer.regs[%zd]: 0x%lx\n", i, context->args.integer.regs[i]);
  }

  fprintf(stderr, "args.vector.regs: %p\n", context->args.vector.regs);
  for (size_t i = 0; i < VECTOR_ARGUMENT_REGISTERS_NOOF; i++) {
    fprintf(stderr, "args.vector.regs[%zd]:\n", i);

    fprintf(stderr, "\traw: | ");
    for (size_t j = 0; j < VectorRegister::VECTOR_MAX_WIDTH_U64S; j++) {
      fprintf(stderr, "\t0x%016lx |", context->args.vector.regs[i].u64[j]);
    }
    fprintf(stderr, "\n");

    fprintf(stderr, "\tfloat: |");
    for (size_t j = 0; j < VectorRegister::VECTOR_MAX_WIDTH_FLOATS; j++) {
      fprintf(stderr, "%f |", context->args.vector.regs[i].f[j]);
    }
    fprintf(stderr, "\n");

    fprintf(stderr, "\tdouble: |");
    for (size_t j = 0; j < VectorRegister::VECTOR_MAX_WIDTH_DOUBLES; j++) {
      fprintf(stderr, "%f |", context->args.vector.regs[i].d[j]);
    }
    fprintf(stderr, "\n");
  }

  fprintf(stderr, "args.rsp: 0x%lx\n", context->args.rsp);
  for (int i = 0; i < 64; i += 8) {
    fprintf(stderr, "args.stack+%d: 0x%lx\n", i, *(uintptr_t*)(context->args.rsp+i));
  }
#endif

  if (!upcall_info.inited) {
    upcall_init();
  }

  ThreadInVMfromNative __tiv(thread);

  JavaValue result(T_VOID);
  JavaCallArguments args(6 * 2);

  args.push_jobject(rec);
#ifdef _LP64
  args.push_long((jlong)&context->args.integer.regs);
  args.push_long((jlong)&context->args.vector.regs);
#else
  args.push_long((jlong)0);
  args.push_long((jlong)0);
#endif
  args.push_long((jlong)context->args.rsp);
  args.push_long((jlong)&context->returns.integer.regs);
  args.push_long((jlong)&context->returns.vector.regs);
  args.push_long((jlong)&context->returns.x87.regs);

  JavaCalls::call_static(&result, upcall_info.upcall_method.klass, upcall_info.upcall_method.name, upcall_info.upcall_method.sig, &args, thread);

#if 0
  fprintf(stderr, "returns.integer.regs: %p\n", context->returns.integer.regs);
  fprintf(stderr, "returns.integer.reg.rax: 0x%lx\n", context->returns.integer.reg.rax);
  fprintf(stderr, "returns.integer.reg.rdx: 0x%lx\n", context->returns.integer.reg.rdx);
  fprintf(stderr, "returns.x87.st0: %Lf\n", context->returns.x87.reg.st0);
  fprintf(stderr, "returns.x87.st1: %Lf\n", context->returns.x87.reg.st1);
#endif
}

address UniversalUpcallHandler::generate_upcall_stub(Handle& rec_handle) {
  ResourceMark rm;
  CodeBuffer buffer("upcall_stub", 1024, 1024);

  MacroAssembler* _masm = new MacroAssembler(&buffer);

  jobject rec = JNIHandles::make_weak_global(rec_handle);

#if 0
  fprintf(stderr, "generate_upcall_stub(%p)\n", rec);
#endif


  // stub code
  __ enter();

  // save pointer to JNI receiver handle into constant segment
  Address rec_adr = __ as_Address(InternalAddress(__ address_constant((address)rec)));

  __ subptr(rsp, sizeof(struct upcall_context));
  __ andptr(rsp, -64);

  // Save preserved registers according to calling convention
  __ movptr(Address(rsp, offsetof(struct upcall_context, preserved.rbx)), rbx);
#ifdef _WIN64
  __ movptr(Address(rsp, offsetof(struct upcall_context, preserved.rdi)), rdi);
  __ movptr(Address(rsp, offsetof(struct upcall_context, preserved.rsi)), rsi);
#endif
#ifdef _LP64
  __ movptr(Address(rsp, offsetof(struct upcall_context, preserved.r12)), r12);
  __ movptr(Address(rsp, offsetof(struct upcall_context, preserved.r13)), r13);
  __ movptr(Address(rsp, offsetof(struct upcall_context, preserved.r14)), r14);
  __ movptr(Address(rsp, offsetof(struct upcall_context, preserved.r15)), r15);
#endif

  // FIXME: Tons of stuff stripped here...


#ifdef _LP64
  // Capture argument registers
#ifndef _WIN64
   __ movptr(Address(rsp, offsetof(struct upcall_context, args.integer.reg.rdi)), rdi);
   __ movptr(Address(rsp, offsetof(struct upcall_context, args.integer.reg.rsi)), rsi);
#endif
  __ movptr(Address(rsp, offsetof(struct upcall_context, args.integer.reg.rdx)), rdx);
  __ movptr(Address(rsp, offsetof(struct upcall_context, args.integer.reg.rcx)), rcx);
  __ movptr(Address(rsp, offsetof(struct upcall_context, args.integer.reg.r8)), r8);
  __ movptr(Address(rsp, offsetof(struct upcall_context, args.integer.reg.r9)), r9);

  for (size_t i = 0; i < VECTOR_ARGUMENT_REGISTERS_NOOF; i++) {
    XMMRegister reg = vector_argument_registers[i];

    size_t offset = offsetof(struct upcall_context, args.vector.regs) + i * sizeof(VectorRegister);

    if (UseAVX >= 3) {
      __ evmovdqul(Address(rsp, (int)offset), reg, Assembler::AVX_512bit);
    } else if (UseAVX >= 1) {
      __ vmovdqu(Address(rsp, (int)offset), reg);
    } else {
      __ movdqu(Address(rsp, (int)offset), reg);
    }
  }

  __ movptr(Address(rsp, offsetof(struct upcall_context, args.rax)), rax);
#endif

  // Capture prev stack pointer (stack arguments base)
#ifndef _WIN64
   __ lea(rax, Address(rbp, 16)); // skip frame+return address
#else
  __ lea(rax, Address(rbp, 16 + 32)); // also skip shadow space
#endif
  __ movptr(Address(rsp, offsetof(struct upcall_context, args.rsp)), rax);


  // Call upcall helper
#ifdef _LP64
  __ movptr(c_rarg0, rec_adr);
  __ movptr(c_rarg1, rsp);
#ifdef _WIN64
  __ block_comment("allocate shadow space for argument register spill");
  __ subptr(rsp, 32);
#endif
#else
  __ movptr(rax, rsp);
  __ subptr(rsp, 8);
  __ movptr(Address(rsp, 4), rax);
  __ movptr(rax, rec_adr);
  __ movptr(Address(rsp, 0), rax);
#endif
  __ call(RuntimeAddress(CAST_FROM_FN_PTR(address, upcall_helper)));
#ifdef _LP64
#ifdef _WIN64
  __ block_comment("pop shadow space");
  __ addptr(rsp, 32);
#endif
# else
   __ addptr(rsp, 8);
#endif


  // Handle return values
  for (size_t i = 0; i < INTEGER_RETURN_REGISTERS_NOOF; i++) {
    Register reg = integer_return_registers[i];
    ssize_t offs = offsetof(struct upcall_context, returns.integer.regs) + i * sizeof(uintptr_t);

    __ movptr(reg, Address(rsp, offs));
  }

  for (size_t i = 0; i < VECTOR_RETURN_REGISTERS_NOOF; i++) {
    XMMRegister reg = vector_return_registers[i];
    ssize_t offs = offsetof(struct upcall_context, returns.vector.regs) + i * sizeof(VectorRegister);
    if (UseAVX >= 3) {
      __ evmovdqul(reg, Address(rsp, offs), Assembler::AVX_512bit);
    } else if (UseAVX >= 1) {
      __ vmovdqu(reg, Address(rsp, offs));
    } else {
      __ movdqu(reg, Address(rsp, offs));
    }
  }

  for (size_t i = X87_RETURN_REGISTERS_NOOF; i > 0 ; i--) {
      ssize_t offs = offsetof(struct upcall_context, returns.x87.regs) + (i - 1) * (sizeof(long double));
      __ fld_x (Address(rsp, (int)offs));
  }


  // FIXME: More stuff stripped here

  // Restore preserved registers
#ifdef _LP64
  __ movptr(r12, Address(rsp, offsetof(struct upcall_context, preserved.r12)));
  __ movptr(r13, Address(rsp, offsetof(struct upcall_context, preserved.r13)));
  __ movptr(r14, Address(rsp, offsetof(struct upcall_context, preserved.r14)));
  __ movptr(r15, Address(rsp, offsetof(struct upcall_context, preserved.r15)));
#endif
  __ movptr(rbx, Address(rsp, offsetof(struct upcall_context, preserved.rbx)));
#ifdef _WIN64
  __ movptr(rdi, Address(rsp, offsetof(struct upcall_context, preserved.rdi)));
  __ movptr(rsi, Address(rsp, offsetof(struct upcall_context, preserved.rsi)));
#endif

  // FIXME: More stuff stripped here

  __ leave();
  __ ret(0);

  _masm->flush();

  BufferBlob* blob = BufferBlob::create("upcall_stub", &buffer);

  return blob->code_begin();
}
