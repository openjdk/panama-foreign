/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2019, Arm Limited. All rights reserved.
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
#include "classfile/symbolTable.hpp"
#include "include/jvm.h"
#include "jni.h"
#include "logging/log.hpp"
#include "logging/logStream.hpp"
#include "memory/allocation.inline.hpp"
#include "memory/resourceArea.hpp"
#include "oops/arrayOop.inline.hpp"
#include "prims/universalUpcallHandler.hpp"
#include "runtime/interfaceSupport.inline.hpp"
#include "runtime/javaCalls.hpp"
#include "runtime/jniHandles.inline.hpp"

extern struct JavaVM_ main_vm;

static struct {
  bool inited;
  struct {
    Klass* klass;
    Symbol* name;
    Symbol* sig;
  } upcall_method;  // jdk.internal.foreign.abi.UniversalUpcallHandler::invoke
} upcall_info;

// FIXME: This should be initialized explicitly instead of lazily/racily
static void upcall_init(void) {
#if 0
  fprintf(stderr, "upcall_init()\n");
#endif

  TRAPS = Thread::current();
  ResourceMark rm;

  const char* cname = "jdk/internal/foreign/abi/UniversalUpcallHandler";
  const char* mname = "invoke";
  const char* mdesc = "(Ljdk/internal/foreign/abi/UniversalUpcallHandler;JJJJJJJ)V";
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
    uintptr_t integer[10];  // r19-r28
    double    vector[8];    // v8-v15 (lower 64 bits)
  } preserved;

  struct {
    uintptr_t integer[INTEGER_ARGUMENT_REGISTERS_NOOF];
    VectorRegister vector[VECTOR_ARGUMENT_REGISTERS_NOOF];
    uintptr_t sp;
    uintptr_t indirect;
  } args;

  struct {
    uintptr_t integer[INTEGER_RETURN_REGISTERS_NOOF];
    VectorRegister vector[VECTOR_RETURN_REGISTERS_NOOF];
  } returns;
};

static void upcall_helper(jobject rec, struct upcall_context* context) {
#if 0
  ::fprintf(stderr, "upcall_helper(%p, %p)\n", rec, context);
#endif

   void *p_env = NULL;

  JavaThread* thread = JavaThread::current();
  if (thread == NULL) {
    JavaVM_ *vm = (JavaVM *)(&main_vm);
    vm -> functions -> AttachCurrentThreadAsDaemon(vm, &p_env, NULL);
    thread = JavaThread::current();
  }

  assert(thread->is_Java_thread(), "really?");

#if 0
  fprintf(stderr, "args.integer: %p\n", context->args.integer);
  for (size_t i = 0; i < INTEGER_ARGUMENT_REGISTERS_NOOF; i++) {
    fprintf(stderr, "args.integer[%zd]: 0x%lx\n", i, context->args.integer[i]);
  }

  fprintf(stderr, "args.vector: %p\n", context->args.vector);
  for (size_t i = 0; i < VECTOR_ARGUMENT_REGISTERS_NOOF; i++) {
    fprintf(stderr, "args.vector[%zd]:\n", i);

    fprintf(stderr, "\traw: | ");
    for (size_t j = 0; j < VectorRegister::VECTOR_MAX_WIDTH_U64S; j++) {
      fprintf(stderr, "\t0x%016lx |", context->args.vector[i].u64[j]);
    }
    fprintf(stderr, "\n");

    fprintf(stderr, "\tfloat: |");
    for (size_t j = 0; j < VectorRegister::VECTOR_MAX_WIDTH_FLOATS; j++) {
      fprintf(stderr, "%f |", context->args.vector[i].f[j]);
    }
    fprintf(stderr, "\n");

    fprintf(stderr, "\tdouble: |");
    for (size_t j = 0; j < VectorRegister::VECTOR_MAX_WIDTH_DOUBLES; j++) {
      fprintf(stderr, "%f |", context->args.vector[i].d[j]);
    }
    fprintf(stderr, "\n");
  }

  fprintf(stderr, "args.sp: 0x%lx\n", context->args.sp);
  for (int i = 0; i < 64; i += 8) {
    fprintf(stderr, "args.stack+%d: 0x%lx\n", i, *(uintptr_t*)(context->args.sp+i));
  }
#endif

  if (!upcall_info.inited) {
    upcall_init();
  }

  ThreadInVMfromNative __tiv(thread);

  JavaValue result(T_VOID);
  JavaCallArguments args(7 * 2);

  args.push_jobject(rec);
  args.push_long((jlong)&context->args.integer);
  args.push_long((jlong)&context->args.vector);
  args.push_long((jlong)context->args.sp);
  args.push_long((jlong)&context->returns.integer);
  args.push_long((jlong)&context->returns.vector);
  args.push_long(0L /* X87 regs on x86 */);
  args.push_long((jlong)&context->args.indirect);

  JavaCalls::call_static(&result, upcall_info.upcall_method.klass,
                         upcall_info.upcall_method.name, upcall_info.upcall_method.sig,
                         &args, thread);
}

address UniversalUpcallHandler::generate_upcall_stub(Handle& rec_handle) {
  ResourceMark rm;
  CodeBuffer buffer("upcall_stub", 1024, 1024);

  MacroAssembler* _masm = new MacroAssembler(&buffer);

  jobject rec = JNIHandles::make_weak_global(rec_handle);

  // stub code
  __ enter();

  // save pointer to JNI receiver handle into constant segment
  Address rec_adr = InternalAddress(__ address_constant((address)rec));

  __ sub(sp, sp, align_up(sizeof(struct upcall_context), 16));

  // Save preserved registers according to calling convention
  __ stp(r19, r20, Address(sp, offsetof(struct upcall_context, preserved.integer[0])));
  __ stp(r21, r22, Address(sp, offsetof(struct upcall_context, preserved.integer[2])));
  __ stp(r23, r24, Address(sp, offsetof(struct upcall_context, preserved.integer[4])));
  __ stp(r25, r26, Address(sp, offsetof(struct upcall_context, preserved.integer[6])));
  __ stp(r27, r28, Address(sp, offsetof(struct upcall_context, preserved.integer[8])));

  __ stpd(v8, v9, Address(sp, offsetof(struct upcall_context, preserved.vector[0])));
  __ stpd(v10, v11, Address(sp, offsetof(struct upcall_context, preserved.vector[2])));
  __ stpd(v12, v13, Address(sp, offsetof(struct upcall_context, preserved.vector[4])));
  __ stpd(v14, v15, Address(sp, offsetof(struct upcall_context, preserved.vector[6])));

  // Capture argument registers

  const size_t integer_arg_base = offsetof(struct upcall_context, args.integer);
  for (size_t i = 0; i < INTEGER_ARGUMENT_REGISTERS_NOOF; i++) {
    Register reg = integer_argument_registers[i];
    size_t offset = integer_arg_base + i * sizeof(uintptr_t);
    __ str(reg, Address(sp, (int)offset));
  }

  const size_t vector_arg_base = offsetof(struct upcall_context, args.vector);
  for (size_t i = 0; i < VECTOR_ARGUMENT_REGISTERS_NOOF; i++) {
    FloatRegister reg = vector_argument_registers[i];
    size_t offset = vector_arg_base + i * sizeof(VectorRegister);
    __ strq(reg, Address(sp, (int)offset));
  }

  // Final integer argument is indirect result register
  __ str(r8, Address(sp, offsetof(struct upcall_context, args.indirect)));

  // Capture prev stack pointer (stack arguments base)
  __ mov(rscratch1, sp);
  __ str(rscratch1, Address(sp, offsetof(struct upcall_context, args.sp)));

  // Call upcall helper
  __ ldr(c_rarg0, rec_adr);
  __ mov(c_rarg1, sp);
  __ movptr(rscratch1, CAST_FROM_FN_PTR(uint64_t, upcall_helper));
  __ blr(rscratch1);

  // Handle return values
  const size_t integer_return_base = offsetof(struct upcall_context, returns.integer);
  for (size_t i = 0; i < INTEGER_RETURN_REGISTERS_NOOF; i++) {
    Register reg = integer_return_registers[i];
    ssize_t offs = integer_return_base + i * sizeof(uintptr_t);
    __ lea(rscratch1, Address(sp, offs));
    __ ldr(reg, rscratch1);
  }

  const size_t vector_return_base = offsetof(struct upcall_context, returns.vector);
  for (size_t i = 0; i < VECTOR_ARGUMENT_REGISTERS_NOOF; i++) {
    FloatRegister reg = vector_return_registers[i];
    ssize_t offs = vector_return_base + i * sizeof(VectorRegister);
    __ lea(rscratch1, Address(sp, offs));
    __ ldrq(reg, rscratch1);
  }

  // Restore preserved registers
  __ ldp(r19, r20, Address(sp, offsetof(struct upcall_context, preserved.integer[0])));
  __ ldp(r21, r22, Address(sp, offsetof(struct upcall_context, preserved.integer[2])));
  __ ldp(r23, r24, Address(sp, offsetof(struct upcall_context, preserved.integer[4])));
  __ ldp(r25, r26, Address(sp, offsetof(struct upcall_context, preserved.integer[6])));
  __ ldp(r27, r28, Address(sp, offsetof(struct upcall_context, preserved.integer[8])));

  __ ldpd(v8, v9, Address(sp, offsetof(struct upcall_context, preserved.vector[0])));
  __ ldpd(v10, v11, Address(sp, offsetof(struct upcall_context, preserved.vector[2])));
  __ ldpd(v12, v13, Address(sp, offsetof(struct upcall_context, preserved.vector[4])));
  __ ldpd(v14, v15, Address(sp, offsetof(struct upcall_context, preserved.vector[6])));

  __ leave();
  __ ret(lr);

  __ flush();

  BufferBlob* blob = BufferBlob::create("upcall_stub", &buffer);

  return blob->code_begin();
}
