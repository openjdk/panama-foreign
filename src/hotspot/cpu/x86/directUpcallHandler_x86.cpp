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
#include "vmreg_x86.inline.hpp"

struct upcall_context {
  struct {
    uintptr_t rbx;
#ifdef _LP64
    uintptr_t r12;
    uintptr_t r13;
    uintptr_t r14;
    uintptr_t r15;
    uintptr_t mxcsr;
#ifdef _WIN64
    uintptr_t rsi;
    uintptr_t rdi;
 #endif
#endif
    JavaFrameAnchor jfa;
    uintptr_t thread;
  } preserved;
};

void save_upcall_context(MacroAssembler* _masm, int upcall_context_offset) {
  __ movptr(Address(rsp, upcall_context_offset + offsetof(struct upcall_context, preserved.rbx)), rbx);
#ifdef _LP64
  __ movptr(Address(rsp, upcall_context_offset + offsetof(struct upcall_context, preserved.r12)), r12);
  __ movptr(Address(rsp, upcall_context_offset + offsetof(struct upcall_context, preserved.r13)), r13);
  __ movptr(Address(rsp, upcall_context_offset + offsetof(struct upcall_context, preserved.r14)), r14);
  __ movptr(Address(rsp, upcall_context_offset + offsetof(struct upcall_context, preserved.r15)), r15);
#ifndef _WIN64
  const Address mxcsr_save(rsp, upcall_context_offset + offsetof(struct upcall_context, preserved.mxcsr));
  const int MXCSR_MASK = 0xFFC0;  // Mask out any pending exceptions

  Label skip_ldmx;
  __ stmxcsr(mxcsr_save);
  __ movl(rax, mxcsr_save);
  __ andl(rax, MXCSR_MASK);    // Only check control and mask bits
  ExternalAddress mxcsr_std(StubRoutines::addr_mxcsr_std());
  __ cmp32(rax, mxcsr_std);
  __ jcc(Assembler::equal, skip_ldmx);
  __ ldmxcsr(mxcsr_std);
  __ bind(skip_ldmx);
#else // _WIN64
#error "NYI WIN64"
  __ movptr(Address(rsp, upcall_context_offset + offsetof(struct upcall_context, preserved.rsi)), rsi);
  __ movptr(Address(rsp, upcall_context_offset + offsetof(struct upcall_context, preserved.rdi)), rdi);
//    int last_reg = (UseAVX > 2 ? 31 : 15);
//
//    if (VM_Version::supports_evex()) {
//      for (int i = xmm_save_first; i <= last_reg; i++) {
//        __ vextractf32x4(xmm_save(i), as_XMMRegister(i), 0);
//      }
//    } else {
//      for (int i = xmm_save_first; i <= last_reg; i++) {
//        __ movdqu(xmm_save(i), as_XMMRegister(i));
//      }
//    }
//
//    const Address rdi_save(rbp, rdi_off * wordSize);
//    const Address rsi_save(rbp, rsi_off * wordSize);
//
#endif // _WIN64
#else // _LP64
#error "NYI 32-bit"
#endif // _LP64

#ifdef _WIN64
#else
#endif // _WIN64
}

static void restore_upcall_context(MacroAssembler* _masm, int upcall_context_offset) {
  __ movptr(rbx, Address(rsp, upcall_context_offset + offsetof(struct upcall_context, preserved.rbx)));
#ifdef _LP64
  __ movptr(r12, Address(rsp, upcall_context_offset + offsetof(struct upcall_context, preserved.r12)));
  __ movptr(r13, Address(rsp, upcall_context_offset + offsetof(struct upcall_context, preserved.r13)));
  __ movptr(r14, Address(rsp, upcall_context_offset + offsetof(struct upcall_context, preserved.r14)));
  __ movptr(r15, Address(rsp, upcall_context_offset + offsetof(struct upcall_context, preserved.r15)));
#ifndef _WIN64
  const Address mxcsr_save(rsp, upcall_context_offset + offsetof(struct upcall_context, preserved.mxcsr));
  __ ldmxcsr(mxcsr_save);
#else // _WIN64
#error "NYI WIN64"
  __ movptr(rsi, Address(rsp, upcall_context_offset + offsetof(struct upcall_context, preserved.rsi)));
  __ movptr(rdi, Address(rsp, upcall_context_offset + offsetof(struct upcall_context, preserved.rdi)));
    // emit the restores for xmm regs
//    if (VM_Version::supports_evex()) {
//      for (int i = xmm_save_first; i <= last_reg; i++) {
//        __ vinsertf32x4(as_XMMRegister(i), as_XMMRegister(i), xmm_save(i), 0);
//      }
//    } else {
//      for (int i = xmm_save_first; i <= last_reg; i++) {
//        __ movdqu(as_XMMRegister(i), xmm_save(i));
//      }
//    }
//    __ movptr(rdi, rdi_save);
//    __ movptr(rsi, rsi_save);
#endif // _WIN64
#endif // _LP64
}

void DirectUpcallHandler::save_java_frame_anchor(MacroAssembler* _masm, int upcall_context_offset, Register thread) {
  int thread_base_offset = offsetof(JavaThread, _anchor);
  int upcall_base_offset = upcall_context_offset + offsetof(struct upcall_context, preserved.jfa);

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

void DirectUpcallHandler::restore_java_frame_anchor(MacroAssembler* _masm, int upcall_context_offset, Register thread) {
  int thread_base_offset = offsetof(JavaThread, _anchor);
  int upcall_base_offset = upcall_context_offset + offsetof(struct upcall_context, preserved.jfa);

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

struct ArgMove {
  BasicType bt;
  VMRegPair from;
  VMRegPair to;
};

static GrowableArray<ArgMove>* compute_argument_shuffle(Method* entry, int& frame_size) {
  assert(entry->is_static(), "");

  // Fill in the signature array, for the calling-convention call.
  const int total_out_args = entry->size_of_parameters();
  assert(total_out_args > 0, "receiver arg ");

  BasicType* out_sig_bt = NEW_RESOURCE_ARRAY(BasicType, total_out_args);
  VMRegPair* out_regs = NEW_RESOURCE_ARRAY(VMRegPair, total_out_args);

  {
    int i = 0;
    SignatureStream ss(entry->signature());
    for (; !ss.at_return_type(); ss.next()) {
      out_sig_bt[i++] = ss.type();  // Collect remaining bits of signature
      if (ss.type() == T_LONG || ss.type() == T_DOUBLE)
        out_sig_bt[i++] = T_VOID;   // Longs & doubles take 2 Java slots
    }
    assert(i == total_out_args, "");
    BasicType ret_type = ss.type();
  }

  const bool is_outgoing = false; // method->is_method_handle_intrinsic();
  int out_arg_slots = SharedRuntime::java_calling_convention(out_sig_bt, out_regs, total_out_args, is_outgoing);

  const int total_in_args = total_out_args - 1;
  BasicType* in_sig_bt  = NEW_RESOURCE_ARRAY(BasicType, total_in_args);
  VMRegPair* in_regs    = NEW_RESOURCE_ARRAY(VMRegPair, total_in_args);

  for (int i = 0; i < total_in_args ; i++ ) {
    in_sig_bt[i] = out_sig_bt[i+1];
  }

  // Now figure out where the args must be stored and how much stack space they require.
  int in_arg_slots = SharedRuntime::c_calling_convention(in_sig_bt, in_regs, NULL, total_in_args);

  GrowableArray<int> arg_order(2 * total_in_args);

  VMRegPair tmp_vmreg;
  tmp_vmreg.set2(rbx->as_VMReg());

  // Compute a valid move order, using tmp_vmreg to break any cycles
  SharedRuntime::compute_move_order(in_sig_bt,
                                    total_in_args, in_regs,
                                    total_out_args, out_regs,
                                    arg_order,
                                    tmp_vmreg);

  GrowableArray<ArgMove>* arg_order_vmreg = new GrowableArray<ArgMove>(total_in_args); // conservative

#ifdef ASSERT
  bool reg_destroyed[RegisterImpl::number_of_registers];
  bool freg_destroyed[XMMRegisterImpl::number_of_registers];
  for ( int r = 0 ; r < RegisterImpl::number_of_registers ; r++ ) {
    reg_destroyed[r] = false;
  }
  for ( int f = 0 ; f < XMMRegisterImpl::number_of_registers ; f++ ) {
    freg_destroyed[f] = false;
  }
#endif // ASSERT

  for (int i = 0; i < arg_order.length(); i += 2) {
    int in_arg  = arg_order.at(i);
    int out_arg = arg_order.at(i + 1);

    assert(in_arg != -1 || out_arg != -1, "");
    BasicType arg_bt = (in_arg != -1 ? in_sig_bt[in_arg] : out_sig_bt[out_arg]);
    switch (arg_bt) {
      case T_BOOLEAN:
      case T_BYTE:
      case T_SHORT:
      case T_CHAR:
      case T_INT:
      case T_FLOAT:
        break; // process

      case T_LONG:
      case T_DOUBLE:
        assert(in_arg  == -1 || (in_arg  + 1 < total_in_args  &&  in_sig_bt[in_arg  + 1] == T_VOID), "bad arg list: %d", in_arg);
        assert(out_arg == -1 || (out_arg + 1 < total_out_args && out_sig_bt[out_arg + 1] == T_VOID), "bad arg list: %d", out_arg);
        break; // process

      case T_VOID:
        continue; // skip

      default:
        fatal("found in upcall args: %s", type2name(arg_bt));
    }

    ArgMove move;
    move.bt   = arg_bt;
    move.from = (in_arg != -1 ? in_regs[in_arg] : tmp_vmreg);
    move.to   = (out_arg != -1 ? out_regs[out_arg] : tmp_vmreg);

#ifdef ASSERT
    if (in_regs[in_arg].first()->is_Register()) {
      assert(!reg_destroyed[in_regs[in_arg].first()->as_Register()->encoding()], "destroyed reg!");
    } else if (in_regs[in_arg].first()->is_XMMRegister()) {
      assert(!freg_destroyed[in_regs[in_arg].first()->as_XMMRegister()->encoding()], "destroyed reg!");
    }
    if (out_arg != -1) {
      if (out_regs[out_arg].first()->is_Register()) {
        reg_destroyed[out_regs[out_arg].first()->as_Register()->encoding()] = true;
      } else if (out_regs[out_arg].first()->is_XMMRegister()) {
        freg_destroyed[out_regs[out_arg].first()->as_XMMRegister()->encoding()] = true;
      }
    }
#endif /* ASSERT */

    arg_order_vmreg->push(move);
  }

  // Calculate the total number of stack slots we will need.

  // First count the abi requirement plus all of the outgoing args
  int stack_slots = SharedRuntime::out_preserve_stack_slots() + out_arg_slots;

  // Now a place (+2) to save return values or temp during shuffling
  // + 4 for return address (which we own) and saved rbp
//  stack_slots += 6;

  // Ok The space we have allocated will look like:
  //
  //
  // FP-> |                     |
  //      |---------------------|
  //      | 2 slots for moves   |
  //      |---------------------|
  //      | outbound memory     |
  //      | based arguments     |
  //      |                     |
  //      |---------------------|
  //      |                     |
  // SP-> | out_preserved_slots |
  //
  //

  // Now compute actual number of stack words we need rounding to make
  // stack properly aligned.

  frame_size = align_up(stack_slots * VMRegImpl::stack_slot_size, StackAlignmentInBytes);

  return arg_order_vmreg;
}

static void shuffle_arguments(MacroAssembler* _masm, GrowableArray<ArgMove>* arg_moves) {
  for (int i = 0; i < arg_moves->length(); i++) {
    ArgMove arg_mv = arg_moves->at(i);
    BasicType arg_bt     = arg_mv.bt;
    VMRegPair from_vmreg = arg_mv.from;
    VMRegPair   to_vmreg = arg_mv.to;

    __ block_comment(err_msg("bt=%s", type2name(arg_bt)));
    switch (arg_bt) {
      case T_BOOLEAN:
      case T_BYTE:
      case T_SHORT:
      case T_CHAR:
      case T_INT:
       SharedRuntime::move32_64(_masm, from_vmreg, to_vmreg);
       break;

      case T_FLOAT:
        SharedRuntime::float_move(_masm, from_vmreg, to_vmreg);
        break;

      case T_DOUBLE:
        SharedRuntime::double_move(_masm, from_vmreg, to_vmreg);
        break;

      case T_LONG :
        SharedRuntime::long_move(_masm, from_vmreg, to_vmreg);
        break;

      default:
        fatal("found in upcall args: %s", type2name(arg_bt));
    }
  }
}

address DirectUpcallHandler::generate_linkToNative_upcall_stub(jobject receiver, Method* entry, TRAPS) {
  CodeBuffer buffer("upcall_stub_linkToNative", 1024, 1024);

  int frame_size = -1;
  GrowableArray<ArgMove>* arg_moves = compute_argument_shuffle(entry, frame_size);

  int upcall_context_offset = frame_size;

  frame_size = align_up(frame_size + sizeof(struct upcall_context), StackAlignmentInBytes);

  //////////////////////////////////////////////////////////////////////////////

  MacroAssembler* _masm = new MacroAssembler(&buffer);

  Label call_return;
  address start = __ pc();

  __ enter(); // set up frame

  __ subptr(rsp, frame_size);

  save_upcall_context(_masm, upcall_context_offset);

  __ get_thread(r15_thread);

  __ movptr(Address(rsp, upcall_context_offset + offsetof(struct upcall_context, preserved.thread)), r15_thread);

  // FIXME: is it needed?
//  JNIHandleBlock* new_handles = JNIHandleBlock::allocate_block(thread);
//  _handles      = _thread->active_handles();    // save previous handle block & Java frame linkage
//  _thread->set_active_handles(new_handles);     // install new handle block and reset Java frame linkage

  // FIXME: pending exceptions?

  __ block_comment("safepoint poll");
  __ movl(Address(r15_thread, JavaThread::thread_state_offset()), _thread_in_native_trans);

  if (os::is_MP()) {
    __ membar(Assembler::Membar_mask_bits(
                Assembler::LoadLoad  | Assembler::StoreLoad |
                Assembler::LoadStore | Assembler::StoreStore));
  }

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

  __ block_comment("{ argument shuffle");
  shuffle_arguments(_masm, arg_moves);
  __ block_comment("} argument shuffle");

  __ block_comment("{ receiver ");
  __ movptr(rscratch1, (intptr_t)receiver);
  //__ movptr(rscratch1, ExternalAddress((address)receiver));
  __ resolve_jobject(rscratch1, r15_thread, rscratch2);
  __ movptr(j_rarg0, rscratch1);
  __ block_comment("} receiver ");

  __ mov_metadata(rbx, entry);

  __ movptr(Address(r15_thread, JavaThread::callee_target_offset()), rbx); // just in case callee is deoptimized

  __ reinit_heapbase();

  save_java_frame_anchor(_masm, upcall_context_offset, r15_thread);

  __ reset_last_Java_frame(r15_thread, true);

  __ call(Address(rbx, Method::from_compiled_offset()));

  // FIXME: return value post-processing?

  __ bind(call_return);

  restore_java_frame_anchor(_masm, upcall_context_offset, r15_thread);

  restore_upcall_context(_masm, upcall_context_offset);

//  __ vzeroupper()
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

  __ block_comment("} L_safepoint_poll_slow_path");

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

  __ block_comment("} L_reguard");

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

  // TODO: return value?

  __ jmp(call_return);
  __ block_comment("} exception handler");

  _masm->flush();

  stringStream ss;
  ss.print("upcall_stub_linkToNative_J%s", entry->signature()->as_C_string());
  const char* name = _masm->code_string(ss.as_string());

  EntryBlob* blob = EntryBlob::create(name, &buffer, exception_handler_offset, receiver);

  if (UseNewCode) {
    blob->print_on(tty);
    Disassembler::decode(blob, tty);
  }

  return blob->code_begin();
}
