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
#include "compiler/disassembler.hpp"
#include "interpreter/interpreter.hpp"
#include "interpreter/interpreterRuntime.hpp"
#include "memory/allocation.inline.hpp"
#include "memory/resourceArea.hpp"
#include "include/jvm.h"
#include "prims/directUpcallHandler.hpp"
#include "runtime/interfaceSupport.inline.hpp"
#include "runtime/javaCalls.hpp"
#include "runtime/jniHandles.inline.hpp"
#include "runtime/sharedRuntime.hpp"
#include "logging/log.hpp"
#include "logging/logStream.hpp"
#include "oops/arrayOop.inline.hpp"

static struct SpecializedUpcallInfo {
  bool inited;
  Method* meth;
} specialized_upcall_info[5][5][3];

#define LONGS "JJJJJ"
#define DOUBLES "DDDDD"

static char decode_ret(int tag) {
   switch (tag) {
      case 0: return 'V';
      case 1: return 'J';
      case 2: return 'D';
   }
   return '\0';
}

#include "classfile/symbolTable.hpp"
// FIXME: This should be initialized explicitly instead of lazily/racily
static Method* specialized_upcall_init(const char* prefix, int nlongs, int ndoubles, int rettag) {
  SpecializedUpcallInfo* info = &specialized_upcall_info[nlongs][ndoubles][rettag];
  if (info->inited) {
    return info->meth;
  }
  TRAPS = Thread::current();
  ResourceMark rm;
  stringStream desc, cname, mname;

  char ret = decode_ret(rettag);

  desc.print("(Ljdk/internal/foreign/abi/%sUpcallHandler;%.*s%.*s)%c", prefix, nlongs, LONGS, ndoubles, DOUBLES, ret);
  if (nlongs + ndoubles == 0) {
    mname.print("invoke_%c_V", ret);
  } else {
    mname.print("invoke_%c_%.*s%.*s", ret, nlongs, LONGS, ndoubles, DOUBLES);
  }
  cname.print("jdk/internal/foreign/abi/%sUpcallHandler", prefix);

  Symbol* cname_sym = SymbolTable::lookup(cname.as_string(), (int) cname.size(), THREAD);
  Symbol* mname_sym = SymbolTable::lookup(mname.as_string(), (int) mname.size(), THREAD);
  Symbol* mdesc_sym = SymbolTable::lookup(desc.as_string(), (int) desc.size(), THREAD);

  Klass* k = SystemDictionary::resolve_or_null(cname_sym, THREAD);
  Method* method = k->lookup_method(mname_sym, mdesc_sym);
  method->link_method(method, THREAD);

  info->meth = method;
  info->inited = true;

  return method;
}

struct upcall_context {
  struct {
    uintptr_t rbx;
#ifdef _LP64
    uintptr_t r12;
    uintptr_t r13;
    uintptr_t r14;
    uintptr_t r15;
#endif
    JavaFrameAnchor jfa;
    uintptr_t thread;
  } preserved;
};

void save_upcall_context(MacroAssembler* _masm) {
  __ movptr(Address(rsp, offsetof(struct upcall_context, preserved.rbx)), rbx);
#ifdef _LP64
  __ movptr(Address(rsp, offsetof(struct upcall_context, preserved.r12)), r12);
  __ movptr(Address(rsp, offsetof(struct upcall_context, preserved.r13)), r13);
  __ movptr(Address(rsp, offsetof(struct upcall_context, preserved.r14)), r14);
  __ movptr(Address(rsp, offsetof(struct upcall_context, preserved.r15)), r15);
#endif
}

static void restore_upcall_context(MacroAssembler* _masm) {
  __ movptr(rbx, Address(rsp, offsetof(struct upcall_context, preserved.rbx)));
#ifdef _LP64
  __ movptr(r12, Address(rsp, offsetof(struct upcall_context, preserved.r12)));
  __ movptr(r13, Address(rsp, offsetof(struct upcall_context, preserved.r13)));
  __ movptr(r14, Address(rsp, offsetof(struct upcall_context, preserved.r14)));
  __ movptr(r15, Address(rsp, offsetof(struct upcall_context, preserved.r15)));
#endif
}

void DirectUpcallHandler::save_java_frame_anchor(MacroAssembler* _masm, Register thread) {
  int thread_base_offset = offsetof(JavaThread, _anchor);
  int upcall_base_offset = offsetof(struct upcall_context, preserved.jfa);

  int last_Java_fp_offset = offsetof(JavaFrameAnchor, _last_Java_fp);
  int last_Java_pc_offset = offsetof(JavaFrameAnchor, _last_Java_pc);
  int last_Java_sp_offset = offsetof(JavaFrameAnchor, _last_Java_sp);

  // upcall->jfa._last_Java_fp = _thread->_anchor._last_Java_fp;
  __ movptr(rscratch1, Address(thread, thread_base_offset + last_Java_fp_offset));
  __ movptr(Address(rsp, upcall_base_offset + last_Java_fp_offset), rscratch1);

  // upcall->jfa._last_Java_pc = _thread->_anchor._last_Java_pc;
  __ movptr(rscratch1, Address(thread, thread_base_offset + last_Java_pc_offset));
  __ movptr(Address(rsp, upcall_base_offset + last_Java_pc_offset), rscratch1);

  // upcall->jfa._last_Java_sp = _thread->_anchor._last_Java_sp;
  __ movptr(rscratch1, Address(thread, thread_base_offset + last_Java_sp_offset));
  __ movptr(Address(rsp, upcall_base_offset + last_Java_sp_offset), rscratch1);
}

void DirectUpcallHandler::restore_java_frame_anchor(MacroAssembler* _masm, Register thread) {
  int thread_base_offset = offsetof(JavaThread, _anchor);
  int upcall_base_offset = offsetof(struct upcall_context, preserved.jfa);

  int last_Java_fp_offset = offsetof(JavaFrameAnchor, _last_Java_fp);
  int last_Java_pc_offset = offsetof(JavaFrameAnchor, _last_Java_pc);
  int last_Java_sp_offset = offsetof(JavaFrameAnchor, _last_Java_sp);

  // thread->_last_Java_sp = NULL
  __ movptr(Address(thread, thread_base_offset + last_Java_sp_offset), NULL);

  // ThreadStateTransition::transition_from_java(_thread, _thread_in_vm);
  // __ movl(Address(r15_thread, JavaThread::thread_state_offset()), _thread_in_native_trans);
  __ movl(Address(r15_thread, JavaThread::thread_state_offset()), _thread_in_native);

  //_thread->frame_anchor()->copy(&_anchor);
//  _thread->_last_Java_fp = upcall->_last_Java_fp;
//  _thread->_last_Java_pc = upcall->_last_Java_pc;
//  _thread->_last_Java_sp = upcall->_last_Java_sp;

  __ movptr(rscratch1, Address(rsp, upcall_base_offset + last_Java_fp_offset));
  __ movptr(Address(thread, thread_base_offset + last_Java_fp_offset), rscratch1);

  __ movptr(rscratch1, Address(rsp, upcall_base_offset + last_Java_pc_offset));
  __ movptr(Address(thread, thread_base_offset + last_Java_pc_offset), rscratch1);

  __ movptr(rscratch1, Address(rsp, upcall_base_offset + last_Java_sp_offset));
  __ movptr(Address(thread, thread_base_offset + last_Java_sp_offset), rscratch1);
}

#define ARG_MASK 0x000F
#define ARG_SHIFT 4

#define SPECIALIZED_HELPER_BODY(RES_T, RES_TAG) \
  int nlongs = (mask & ARG_MASK); \
  int ndoubles = ((mask >> ARG_SHIFT) & ARG_MASK); \
  int rettag = RES_TAG; \
\
  JavaCallArguments args((1 + nlongs + ndoubles) * 2); \
\
  args.push_jobject(rec); \
\
  long largs[] = { l0, l1, l2, l3 }; \
  double dargs[] = { d0, d1, d2, d3 }; \
\
  int i; \
  for (i = 0 ; i < nlongs ; i++) { \
    args.push_long(largs[i]); \
  } \
  for (int i = 0 ; i < ndoubles ; i++) { \
    args.push_double(dargs[i]); \
  } \
\
  Method* meth = specialized_upcall_info[nlongs][ndoubles][rettag].meth; \
\
  JavaThread* thread = JavaThread::current(); \
  ThreadInVMfromNative __tiv(thread); \
  JavaValue result(RES_T); \
  JavaCalls::call(&result, meth, &args, thread);


static long specialized_upcall_helper_J(long l0, long l1, long l2, long l3,
                                      double d0, double d1, double d2, double d3,
                                      unsigned int mask, jobject rec) {
  SPECIALIZED_HELPER_BODY(T_LONG, 1)
  return result.get_jlong();
}

static double specialized_upcall_helper_D(long l0, long l1, long l2, long l3,
                                      double d0, double d1, double d2, double d3,
                                      unsigned int mask, jobject rec) {
  SPECIALIZED_HELPER_BODY(T_DOUBLE, 2)
  return result.get_jdouble();
}

static void specialized_upcall_helper_V(long l0, long l1, long l2, long l3,
                                      double d0, double d1, double d2, double d3,
                                      unsigned int mask, jobject rec) {
  SPECIALIZED_HELPER_BODY(T_VOID, 0)
}

address DirectUpcallHandler::generate_specialized_upcall_stub(jobject rec_handle, int nlongs, int ndoubles, int rettag) {
  ResourceMark rm;
  CodeBuffer buffer("upcall_stub_specialized", 1024, 1024);

  MacroAssembler* _masm = new MacroAssembler(&buffer);

  // stub code
  __ enter();

  // save pointer to JNI receiver handle into constant segment
  Address rec_adr = __ as_Address(InternalAddress(__ address_constant((address)rec_handle)));

  //just save registers

  __ subptr(rsp, sizeof(struct upcall_context));
  __ andptr(rsp, -64);

  save_upcall_context(_masm);

  specialized_upcall_init("Direct", nlongs, ndoubles, rettag);

  int mask = (nlongs & ARG_MASK) |
              ((ndoubles & ARG_MASK) << ARG_SHIFT);

  // Call upcall helper
#ifdef _LP64
#ifdef _WIN64
  // FIXME: Windows only allow 4 argument registers
#else
  __ movptr(c_rarg4, mask);
  __ movptr(c_rarg5, rec_adr);
#endif // _WIN64
#else
  //non LP64 cannot get to this stub
#endif

 address helper_addr = NULL;
 switch (rettag) {
    case 0: helper_addr = CAST_FROM_FN_PTR(address, specialized_upcall_helper_V); break;
    case 1: helper_addr = CAST_FROM_FN_PTR(address, specialized_upcall_helper_J); break;
    case 2: helper_addr = CAST_FROM_FN_PTR(address, specialized_upcall_helper_D); break;
 }
 __ call(RuntimeAddress(helper_addr));
#ifndef _LP64
  __ addptr(rsp, 8);
#endif

  // FIXME: More stuff stripped here

  restore_upcall_context(_masm);

  // FIXME: More stuff stripped here

  __ leave();
  __ ret(0);

  _masm->flush();

  stringStream ss;
  ss.print("upcall_stub_specialized_J%d_D%d_R%d", nlongs, ndoubles, rettag);
  const char* name = _masm->code_string(ss.as_string());
  BufferBlob* blob = BufferBlob::create(name, &buffer);

  if (UseNewCode) {
    blob->print_on(tty);
    Disassembler::decode(blob, tty);
  }

  return blob->code_begin();
}

static void save_native_arguments(MacroAssembler* _masm) {
  __ push(c_rarg0);
  __ push(c_rarg1);
  __ push(c_rarg2);
  __ push(c_rarg3);
#ifndef _WIN64
  __ push(c_rarg4);
  __ push(c_rarg5);
#endif // _WIN64
}

static void restore_native_arguments(MacroAssembler* _masm) {
#ifndef _WIN64
  __ pop(c_rarg5);
  __ pop(c_rarg4);
#endif // _WIN64
  __ pop(c_rarg3);
  __ pop(c_rarg2);
  __ pop(c_rarg1);
  __ pop(c_rarg0);
}

address DirectUpcallHandler::generate_linkToNative_upcall_stub(jobject receiver, int nlongs, int ndoubles, int rettag) {
  CodeBuffer buffer("upcall_stub_linkToNative", 1024, 1024);
  MacroAssembler* _masm = new MacroAssembler(&buffer);

  Label call_return;
  address start = __ pc();

  __ enter(); // set up frame

  __ subptr(rsp, align_up(sizeof(struct upcall_context), 16));

  save_upcall_context(_masm);

  __ get_thread(r15_thread);

  __ movptr(Address(rsp, offsetof(struct upcall_context, preserved.thread)), r15_thread);

  //////////////////////////////////////////////////////////////////////////////

  __ block_comment("safepoint poll");
  __ movl(Address(r15_thread, JavaThread::thread_state_offset()), _thread_in_native_trans);

  __ membar(Assembler::Membar_mask_bits(
              Assembler::LoadLoad  | Assembler::StoreLoad |
              Assembler::LoadStore | Assembler::StoreStore));

  // check for safepoint operation in progress and/or pending suspend requests
  Label L_after_safepoint_poll;
  Label L_safepoint_poll_slow_path;

  __ safepoint_poll(L_safepoint_poll_slow_path, r15_thread, rscratch1);

  __ cmpl(Address(r15_thread, JavaThread::suspend_flags_offset()), 0);
  __ jcc(Assembler::notEqual, L_safepoint_poll_slow_path);

  __ bind(L_after_safepoint_poll);

  // change thread state
  __ movl(Address(r15_thread, JavaThread::thread_state_offset()), _thread_in_Java);

  __ block_comment("reguard stack check");
  Label L_reguard;
  Label L_after_reguard;
  __ cmpl(Address(r15_thread, JavaThread::stack_guard_state_offset()), JavaThread::stack_guard_yellow_reserved_disabled);
  __ jcc(Assembler::equal, L_reguard);
  __ bind(L_after_reguard);

  //////////////////////////////////////////////////////////////////////////////

  // shuffle (need to add receiver in front)
  //  j_rarg0 = c_rarg1 = rsi
  //  j_rarg1 = c_rarg2 = rdx
  //  j_rarg2 = c_rarg3 = rcx
  //  j_rarg3 = c_rarg4 = r8
  //  j_rarg4 = c_rarg5 = r9
  //  j_rarg5 = c_rarg0 = rdi

  // (c_rarg0, c_rarg1, c_rarg2, c_rarg3, c_rarg4, c_rarg5)
  // (j_rarg5, j_rarg0, j_rarg1, j_rarg2, j_rarg3, j_rarg4)
  //   ==>
  // (j_rarg0, j_rarg1, j_rarg2, j_rarg3, j_rarg4, j_rarg5)
  // (      _, c_rarg2, c_rarg3, c_rarg4, c_rarg5, c_rarg0) c_rarg1
  //
  //   0    1    2    3    4   5            0    1    2   3   4    5
  // (rdi, rsi, rdx, rcx, r8, r9) => (rsi, rdx, rcx, r8, r9, rdi)  _

  // r9  => stack

  // rcx => r9
  // rsi => rcx

  // rdi => rdx
  // r8  => rdi
  // rdx => r8

#ifndef _WIN64
  __ block_comment("argument shuffle");

  // FIXME: remove redundant shuffles
  // on stack: r9 => stack

  // rcx => r9, rsi => rcx
  __ movptr(j_rarg4, c_rarg3); // r9 <= rcx;
  __ movptr(j_rarg2, c_rarg1); // rcx <= rsi

  __ movptr(rscratch1, c_rarg2); // temp: r10 <= rdx

  // rdi => rdx, r8 => rdi, rdx/r10 => r8
  __ movptr(j_rarg1, c_rarg0); // rdx <= rdi
  __ movptr(j_rarg5, c_rarg4); // rdi <= r8
  __ movptr(j_rarg3, rscratch1); // r8 <= r10 (contains c_rarg2)
#else
#error "Not supported"
#endif // _WIN64

  __ block_comment("{ receiver ");
  __ movptr(rscratch1, (intptr_t)receiver);
  //__ movptr(rscratch1, ExternalAddress((address)receiver));
  __ resolve_jobject(rscratch1, r15_thread, rscratch2);
  __ movptr(j_rarg0, rscratch1);
  __ block_comment("} receiver ");

  // FIXME: should go through an invoker?
  Method* meth = NULL;
  {
    oop recv = JNIHandles::resolve(receiver);
    oop lform = java_lang_invoke_MethodHandle::form(recv);
    oop vmentry = java_lang_invoke_LambdaForm::vmentry(lform);
    meth = java_lang_invoke_MemberName::vmtarget(vmentry);
  }

  __ mov_metadata(rbx, meth);

  __ movptr(Address(r15_thread, JavaThread::callee_target_offset()), rbx); // just in case callee is deoptimized

  __ reinit_heapbase();

  //__ movl(Address(r15_thread, JavaThread::thread_state_offset()), _thread_in_Java);

  save_java_frame_anchor(_masm, r15_thread);

  __ reset_last_Java_frame(r15_thread, true);

  __ call(Address(rbx, Method::from_compiled_offset()));

  __ bind(call_return);

  restore_java_frame_anchor(_masm, r15_thread);

  //__ set_last_Java_frame(rsp, noreg, (address)the_pc);

  // FIXME: More stuff stripped here

   restore_upcall_context(_masm);

  // FIXME: More stuff stripped here

  __ leave();
  __ ret(0);

  //////////////////////////////////////////////////////////////////////////////

  __ block_comment("{ L_safepoint_poll_slow_path");
  __ bind(L_safepoint_poll_slow_path);
  __ vzeroupper();
  save_native_arguments(_masm);
  __ mov(c_rarg0, r15_thread);
  __ mov(r12, rsp); // remember sp
  __ subptr(rsp, frame::arg_reg_save_area_bytes); // windows
  __ andptr(rsp, -16); // align stack as required by ABI
  __ call(RuntimeAddress(CAST_FROM_FN_PTR(address, JavaThread::check_special_condition_for_native_trans)));
  __ mov(rsp, r12); // restore sp
  __ reinit_heapbase();
  restore_native_arguments(_masm);
  __ jmp(L_after_safepoint_poll);

  //////////////////////////////////////////////////////////////////////////////

  __ block_comment("{ L_reguard");
  __ bind(L_reguard);
  __ vzeroupper();
  save_native_arguments(_masm);
  __ mov(r12, rsp); // remember sp
  __ subptr(rsp, frame::arg_reg_save_area_bytes); // windows
  __ andptr(rsp, -16); // align stack as required by ABI
  __ call(RuntimeAddress(CAST_FROM_FN_PTR(address, SharedRuntime::reguard_yellow_pages)));
  __ mov(rsp, r12); // restore sp
  __ reinit_heapbase();
  restore_native_arguments(_masm);
  __ jmp(L_after_reguard);

  //////////////////////////////////////////////////////////////////////////////

  __ block_comment("{ exception handler");

  intptr_t exception_handler_offset = __ pc() - start;

  int thread_off = -2;
  const Address thread(rbp, thread_off * wordSize);

#ifdef ASSERT
  // verify that threads correspond
  __ block_comment("{ verification");
  /*if (Verify?)*/ {
    Label L1, L2, L3;
    __ cmpptr(r15_thread, thread);
    __ jcc(Assembler::equal, L1);
    __ stop("StubRoutines::catch_exception: r15_thread is corrupted");
    __ bind(L1);
    __ get_thread(rbx);
    __ cmpptr(r15_thread, thread);
    __ jcc(Assembler::equal, L2);
    __ stop("StubRoutines::catch_exception: r15_thread is modified by call");
    __ bind(L2);
    __ cmpptr(r15_thread, rbx);
    __ jcc(Assembler::equal, L3);
    __ stop("StubRoutines::catch_exception: threads must correspond");
    __ bind(L3);
  }
  __ block_comment("} verification");
#endif

  // set pending exception
  __ verify_oop(rax);

  __ movptr(Address(r15_thread, Thread::pending_exception_offset()), rax);
  __ lea(rscratch1, ExternalAddress((address)__FILE__));
  __ movptr(Address(r15_thread, Thread::exception_file_offset()), rscratch1);
  __ movl(Address(r15_thread, Thread::exception_line_offset()), (int)  __LINE__);

  __ jmp(call_return);
  __ block_comment("} exception handler");

  _masm->flush();

  stringStream ss;
  ss.print("upcall_stub_specialized_J%d_D%d_R%d", nlongs, ndoubles, rettag);
  const char* name = _masm->code_string(ss.as_string());

  EntryBlob* blob = EntryBlob::create("upcall_stub_linkToNative", &buffer, exception_handler_offset, receiver);

  if (UseNewCode) {
    blob->print_on(tty);
    Disassembler::decode(blob, tty);
  }

  return blob->code_begin();
}
