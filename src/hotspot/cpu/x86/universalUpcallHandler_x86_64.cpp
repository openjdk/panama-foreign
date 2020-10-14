/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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
#include "code/codeBlob.hpp"
#include "code/vmreg.inline.hpp"
#include "memory/resourceArea.hpp"
#include "prims/universalUpcallHandler.hpp"
#include "runtime/sharedRuntime.hpp"
#include "code/codeBlob.hpp"
#include "compiler/disassembler.hpp"
#include "utilities/globalDefinitions.hpp"

#define __ _masm->

// 1. Create buffer according to layout
// 2. Load registers & stack args into buffer
// 3. Call upcall helper with upcall handler instance & buffer pointer (C++ ABI)
// 4. Load return value from buffer into foreign ABI registers
// 5. Return
address ProgrammableUpcallHandler::generate_upcall_stub(jobject rec, jobject jabi, jobject jlayout) {
  ResourceMark rm;
  const ABIDescriptor abi = ForeignGlobals::parse_abi_descriptor(jabi);
  const BufferLayout layout = ForeignGlobals::parse_buffer_layout(jlayout);

  CodeBuffer buffer("upcall_stub", 1024, upcall_stub_size);

  MacroAssembler* _masm = new MacroAssembler(&buffer);
  int stack_alignment_C = 16; // bytes
  int register_size = sizeof(uintptr_t);
  int buffer_alignment = xmm_reg_size;

  // stub code
  __ enter();

  // save pointer to JNI receiver handle into constant segment
  Address rec_adr = __ as_Address(InternalAddress(__ address_constant((address)rec)));

  __ subptr(rsp, (int) align_up(layout.buffer_size, buffer_alignment));

  Register used[] = { c_rarg0, c_rarg1, rax, rbx, rdi, rsi, r12, r13, r14, r15 };
  GrowableArray<Register> preserved;
  // TODO need to preserve anything killed by the upcall that is non-volatile, needs XMM regs as well, probably
  for (size_t i = 0; i < sizeof(used)/sizeof(Register); i++) {
    Register reg = used[i];
    if (!abi.is_volatile_reg(reg)) {
      preserved.push(reg);
    }
  }

  int preserved_size = align_up(preserved.length() * register_size, stack_alignment_C); // includes register alignment
  int buffer_offset = preserved_size; // offset from rsp

  __ subptr(rsp, preserved_size);
  for (int i = 0; i < preserved.length(); i++) {
    __ movptr(Address(rsp, i * register_size), preserved.at(i));
  }

  for (int i = 0; i < abi._integer_argument_registers.length(); i++) {
    size_t offs = buffer_offset + layout.arguments_integer + i * sizeof(uintptr_t);
    __ movptr(Address(rsp, (int)offs), abi._integer_argument_registers.at(i));
  }

  for (int i = 0; i < abi._vector_argument_registers.length(); i++) {
    XMMRegister reg = abi._vector_argument_registers.at(i);
    size_t offs = buffer_offset + layout.arguments_vector + i * xmm_reg_size;
    __ movdqu(Address(rsp, (int)offs), reg);
  }

  // Capture prev stack pointer (stack arguments base)
#ifndef _WIN64
  __ lea(rax, Address(rbp, 16)); // skip frame+return address
#else
  __ lea(rax, Address(rbp, 16 + 32)); // also skip shadow space
#endif
  __ movptr(Address(rsp, buffer_offset + (int) layout.stack_args), rax);
#ifndef PRODUCT
  __ movptr(Address(rsp, buffer_offset + (int) layout.stack_args_bytes), -1); // unknown
#endif

  // Call upcall helper

  __ movptr(c_rarg0, rec_adr);
  __ lea(c_rarg1, Address(rsp, buffer_offset));

#ifdef _WIN64
  __ block_comment("allocate shadow space for argument register spill");
  __ subptr(rsp, 32);
#endif

  __ call(RuntimeAddress(CAST_FROM_FN_PTR(address, ProgrammableUpcallHandler::attach_thread_and_do_upcall)));

#ifdef _WIN64
  __ block_comment("pop shadow space");
  __ addptr(rsp, 32);
#endif

  for (int i = 0; i < abi._integer_return_registers.length(); i++) {
    size_t offs = buffer_offset + layout.returns_integer + i * sizeof(uintptr_t);
    __ movptr(abi._integer_return_registers.at(i), Address(rsp, (int)offs));
  }

  for (int i = 0; i < abi._vector_return_registers.length(); i++) {
    XMMRegister reg = abi._vector_return_registers.at(i);
    size_t offs = buffer_offset + layout.returns_vector + i * xmm_reg_size;
    __ movdqu(reg, Address(rsp, (int)offs));
  }

  for (size_t i = abi._X87_return_registers_noof; i > 0 ; i--) {
      ssize_t offs = buffer_offset + layout.returns_x87 + (i - 1) * (sizeof(long double));
      __ fld_x (Address(rsp, (int)offs));
  }

  // Restore preserved registers
  for (int i = 0; i < preserved.length(); i++) {
    __ movptr(preserved.at(i), Address(rsp, i * register_size));
  }

  __ leave();
  __ ret(0);

  _masm->flush();

  BufferBlob* blob = BufferBlob::create("upcall_stub", &buffer);

  return blob->code_begin();
}

struct ArgMove {
  BasicType bt;
  VMRegPair from;
  VMRegPair to;
};

static GrowableArray<ArgMove>* compute_argument_shuffle(Method* entry, int& frame_size, const CallRegs& conv) {
  assert(entry->is_static(), "");

  // Fill in the signature array, for the calling-convention call.
  const int total_out_args = entry->size_of_parameters();
  assert(total_out_args > 0, "receiver arg ");

  BasicType* out_sig_bt = NEW_RESOURCE_ARRAY(BasicType, total_out_args);
  VMRegPair* out_regs = NEW_RESOURCE_ARRAY(VMRegPair, total_out_args);

  {
    int i = 0; // skip receiver
    SignatureStream ss(entry->signature());
    for (; !ss.at_return_type(); ss.next()) {
      out_sig_bt[i++] = ss.type();  // Collect remaining bits of signature
      if (ss.type() == T_LONG || ss.type() == T_DOUBLE)
        out_sig_bt[i++] = T_VOID;   // Longs & doubles take 2 Java slots
    }
    assert(i == total_out_args, "");
    BasicType ret_type = ss.type();
  }

  int out_arg_slots = SharedRuntime::java_calling_convention(out_sig_bt, out_regs, total_out_args);

  const int total_in_args = total_out_args - 1; // skip receiver
  BasicType* in_sig_bt  = NEW_RESOURCE_ARRAY(BasicType, total_in_args);
  VMRegPair* in_regs    = NEW_RESOURCE_ARRAY(VMRegPair, total_in_args);

  for (int i = 0; i < total_in_args ; i++ ) {
    in_sig_bt[i] = out_sig_bt[i+1];
  }

  // Now figure out where the args must be stored and how much stack space they require.
  conv.calling_convention(in_sig_bt, in_regs, total_in_args);

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

void save_java_frame_anchor(MacroAssembler* _masm, ByteSize store_offset, Register thread) {
  __ block_comment("{ save_java_frame_anchor ");
  // upcall->jfa._last_Java_fp = _thread->_anchor._last_Java_fp;
  __ movptr(rscratch1, Address(thread, JavaThread::last_Java_fp_offset()));
  __ movptr(Address(rsp, store_offset + JavaFrameAnchor::last_Java_fp_offset()), rscratch1);

  // upcall->jfa._last_Java_pc = _thread->_anchor._last_Java_pc;
  __ movptr(rscratch1, Address(thread, JavaThread::last_Java_pc_offset()));
  __ movptr(Address(rsp, store_offset + JavaFrameAnchor::last_Java_pc_offset()), rscratch1);

  __ movptr(rscratch1, Address(thread, JavaThread::saved_rbp_address_offset()));
  __ movptr(Address(rsp, store_offset + JavaFrameAnchor::saved_rbp_address_offset()), rscratch1);

  // upcall->jfa._last_Java_sp = _thread->_anchor._last_Java_sp;
  __ movptr(rscratch1, Address(thread, JavaThread::last_Java_sp_offset()));
  __ movptr(Address(rsp, store_offset + JavaFrameAnchor::last_Java_sp_offset()), rscratch1);
  __ block_comment("} save_java_frame_anchor ");
}

void restore_java_frame_anchor(MacroAssembler* _masm, ByteSize load_offset, Register thread) {
  __ block_comment("{ restore_java_frame_anchor ");
  // thread->_last_Java_sp = NULL
  __ movptr(Address(thread, JavaThread::last_Java_sp_offset()), NULL_WORD);

  // ThreadStateTransition::transition_from_java(_thread, _thread_in_vm);
  // __ movl(Address(r15_thread, JavaThread::thread_state_offset()), _thread_in_native_trans);
  __ movl(Address(r15_thread, JavaThread::thread_state_offset()), _thread_in_native);

  //_thread->frame_anchor()->copy(&_anchor);
//  _thread->_last_Java_fp = upcall->_last_Java_fp;
//  _thread->_last_Java_pc = upcall->_last_Java_pc;
//  _thread->_last_Java_sp = upcall->_last_Java_sp;

  __ movptr(rscratch1, Address(rsp, load_offset + JavaFrameAnchor::last_Java_fp_offset()));
  __ movptr(Address(thread, JavaThread::last_Java_fp_offset()), rscratch1);

  __ movptr(rscratch1, Address(rsp, load_offset + JavaFrameAnchor::last_Java_pc_offset()));
  __ movptr(Address(thread, JavaThread::last_Java_pc_offset()), rscratch1);

  __ movptr(rscratch1, Address(rsp, load_offset + JavaFrameAnchor::saved_rbp_address_offset()));
  __ movptr(Address(thread, JavaThread::saved_rbp_address_offset()), rscratch1);

  __ movptr(rscratch1, Address(rsp, load_offset + JavaFrameAnchor::last_Java_sp_offset()));
  __ movptr(Address(thread, JavaThread::last_Java_sp_offset()), rscratch1);
  __ block_comment("} restore_java_frame_anchor ");
}

static void save_native_arguments(MacroAssembler* _masm, const ABIDescriptor& abi) {
  __ block_comment("{ save_native_args ");
  for (int i = 0; i < abi._integer_argument_registers.length(); i++) {
    Register reg = abi._integer_argument_registers.at(i);
    __ push(reg);
  }
  for (int i = 0; i < abi._vector_argument_registers.length(); i++) {
    XMMRegister reg = abi._vector_argument_registers.at(i);
    if (UseAVX >= 3) {
      __ subptr(rsp, 64); // bytes
      __ evmovdqul(Address(rsp, 0), reg, Assembler::AVX_512bit);
    } else if (UseAVX >= 1) {
      __ subptr(rsp, 32);
      __ vmovdqu(Address(rsp, 0), reg);
    } else {
      __ subptr(rsp, 16);
      __ movdqu(Address(rsp, 0), reg);
    }
  }
  __ block_comment("} save_native_args ");
}

static void restore_native_arguments(MacroAssembler* _masm, const ABIDescriptor& abi) {
  __ block_comment("{ restore_native_args ");
  for (int i = abi._vector_argument_registers.length() - 1; i >= 0; i--) {
    XMMRegister reg = abi._vector_argument_registers.at(i);
    if (UseAVX >= 3) {
      __ evmovdqul(reg, Address(rsp, 0), Assembler::AVX_512bit);
      __ addptr(rsp, 64); // bytes
    } else if (UseAVX >= 1) {
      __ vmovdqu(reg, Address(rsp, 0));
      __ addptr(rsp, 32);
    } else {
      __ movdqu(reg, Address(rsp, 0));
      __ addptr(rsp, 16);
    }
  }
  for (int i = abi._integer_argument_registers.length() - 1; i >= 0; i--) {
    Register reg = abi._integer_argument_registers.at(i);
    __ pop(reg);
  }
  __ block_comment("} restore_native_args ");
}

static bool is_valid_XMM(XMMRegister reg) {
  return reg->is_valid() && (UseAVX >= 3 || (reg->encoding() < 16)); // why is this not covered by is_valid()?
}

static int compute_reg_save_area_size(const ABIDescriptor& abi) {
  int size = 0;
  for (Register reg = as_Register(0); reg->is_valid(); reg = reg->successor()) {
    if (reg == rbp || reg == rsp) continue; // saved/restored by prologue/epilogue
    if (!abi.is_volatile_reg(reg)) {
      size += 8; // bytes
    }
  }

  for (XMMRegister reg = as_XMMRegister(0); is_valid_XMM(reg); reg = reg->successor()) {
    if (!abi.is_volatile_reg(reg)) {
      if (UseAVX >= 3) {
        size += 64; // bytes
      } else if (UseAVX >= 1) {
        size += 32;
      } else {
        size += 16;
      }
    }
  }

  return size;
}

static void preserve_callee_saved_registers(MacroAssembler* _masm, const ABIDescriptor& abi, int reg_save_area_offset) {
  // 1. iterate all registers in the architecture
  //     - check if they are volatile or not for the given abi
  //     - if NOT, we need to save it here
  // 2. save mxcsr (?)

  int offset = reg_save_area_offset;

  __ block_comment("{ preserve_callee_saved_regs ");
  for (Register reg = as_Register(0); reg->is_valid(); reg = reg->successor()) {
    if (reg == rbp || reg == rsp) continue; // saved/restored by prologue/epilogue
    if (!abi.is_volatile_reg(reg)) {
      __ movptr(Address(rsp, offset), reg);
      offset += 8;
    }
  }

  for (XMMRegister reg = as_XMMRegister(0); is_valid_XMM(reg); reg = reg->successor()) {
    if (!abi.is_volatile_reg(reg)) {
      if (UseAVX >= 3) {
        __ evmovdqul(Address(rsp, offset), reg, Assembler::AVX_512bit);
        offset += 64;
      } else if (UseAVX >= 1) {
        __ vmovdqu(Address(rsp, offset), reg);
        offset += 32;
      } else {
        __ movdqu(Address(rsp, offset), reg);
        offset += 16;
      }
    }
  }
  __ block_comment("} preserve_callee_saved_regs ");

  // TODO mxcsr
}

static void restore_callee_saved_registers(MacroAssembler* _masm, const ABIDescriptor& abi, int reg_save_area_offset) {
  // 1. iterate all registers in the architecture
  //     - check if they are volatile or not for the given abi
  //     - if NOT, we need to restore it here
  // 2. restore mxcsr (?)

  int offset = reg_save_area_offset;

  __ block_comment("{ restore_callee_saved_regs ");
  for (Register reg = as_Register(0); reg->is_valid(); reg = reg->successor()) {
    if (reg == rbp || reg == rsp) continue; // saved/restored by prologue/epilogue
    if (!abi.is_volatile_reg(reg)) {
      __ movptr(reg, Address(rsp, offset));
      offset += 8;
    }
  }

  for (XMMRegister reg = as_XMMRegister(0); is_valid_XMM(reg); reg = reg->successor()) {
    if (!abi.is_volatile_reg(reg)) {
      if (UseAVX >= 3) {
        __ evmovdqul(reg, Address(rsp, offset), Assembler::AVX_512bit);
        offset += 64;
      } else if (UseAVX >= 1) {
        __ vmovdqu(reg, Address(rsp, offset));
        offset += 32;
      } else {
        __ movdqu(reg, Address(rsp, offset));
        offset += 16;
      }
    }
  }

  __ block_comment("} restore_callee_saved_regs ");

  // TODO mxcsr
}

static const char* null_safe_string(const char* str) {
  return str == nullptr ? "NULL" : str;
}

static void shuffle_arguments(MacroAssembler* _masm, GrowableArray<ArgMove>* arg_moves) {
  for (int i = 0; i < arg_moves->length(); i++) {
    ArgMove arg_mv = arg_moves->at(i);
    BasicType arg_bt     = arg_mv.bt;
    VMRegPair from_vmreg = arg_mv.from;
    VMRegPair   to_vmreg = arg_mv.to;

    __ block_comment(err_msg("bt=%s", null_safe_string(type2name(arg_bt))));
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

struct AuxiliarySaves {
  JavaFrameAnchor jfa;
  uintptr_t thread;
};

address ProgrammableUpcallHandler::generate_optimized_upcall_stub(jobject receiver, Method* entry, jobject jabi, jobject jconv) {
  ResourceMark rm;
  const ABIDescriptor abi = ForeignGlobals::parse_abi_descriptor(jabi);
  const CallRegs conv = ForeignGlobals::parse_call_regs(jconv);
  CodeBuffer buffer("upcall_stub_linkToNative", 1024, 1024);

  int stack_alignment_C = 16; // bytes. FIXME assuming C
  int register_size = sizeof(uintptr_t);
  int buffer_alignment = xmm_reg_size;

  int shuffle_area_size = -1;
  GrowableArray<ArgMove>* arg_moves = compute_argument_shuffle(entry, shuffle_area_size, conv);
  assert(shuffle_area_size != -1, "Should have been set");

  // FIXME:
  // For some reason, if we don't do this the callee's locals
  // will overlap with our register save area during a deopt
  // and the first saved registers ends up being clobbered (rbx on SysV)
  // I'm guessing this space corresponds to the receiver, and compute_argument_shuffle
  // does not account for it
  shuffle_area_size += BytesPerWord;

  int reg_save_area_size = compute_reg_save_area_size(abi);
  int frame_size = shuffle_area_size + reg_save_area_size + sizeof(AuxiliarySaves);

  int auxiliary_saves_offset = shuffle_area_size + reg_save_area_size;
  int reg_save_are_offset = shuffle_area_size;
  int shuffle_area_offset = 0;
  ByteSize jfa_offset = in_ByteSize(auxiliary_saves_offset) + byte_offset_of(AuxiliarySaves, jfa);
  ByteSize thread_offset = in_ByteSize(auxiliary_saves_offset) + byte_offset_of(AuxiliarySaves, thread);

  frame_size = align_up(frame_size, stack_alignment_C);

  // Ok The space we have allocated will look like:
  //
  //
  // FP-> |                     |
  //      |---------------------|
  //      |                     |
  //      | AuxiliarySaves      |
  //      |---------------------| = auxiliary_saves_offset = shuffle_area_size + reg_save_area_size
  //      |                     |
  //      | reg_save_area       |
  //      |---------------------| = reg_save_are_offset = shuffle_area_size
  //      |                     |
  // SP-> | shuffle_area        |   needs to be at end for shadow space
  //
  //

  //////////////////////////////////////////////////////////////////////////////

  MacroAssembler* _masm = new MacroAssembler(&buffer);
  Label call_return;
  address start = __ pc();
  __ enter(); // set up frame
  __ subptr(rsp, frame_size);

  preserve_callee_saved_registers(_masm, abi, reg_save_are_offset);

  // FIXME: mxcsr (see stubGenerator_x86_64.cpp 'generate_call_stub')

  __ block_comment("{ get_thread");
  __ get_thread(r15_thread);
  __ movptr(Address(rsp, thread_offset), r15_thread);
  __ block_comment("} get_thread");

  // FIXME: is it needed?
//  JNIHandleBlock* new_handles = JNIHandleBlock::allocate_block(thread);
//  _handles      = _thread->active_handles();    // save previous handle block & Java frame linkage
//  _thread->set_active_handles(new_handles);     // install new handle block and reset Java frame linkage

  // FIXME: pending exceptions?
  __ block_comment("{ safepoint poll");
  __ movl(Address(r15_thread, JavaThread::thread_state_offset()), _thread_in_native_trans);

  if (os::is_MP()) {
    __ membar(Assembler::Membar_mask_bits(
                Assembler::LoadLoad  | Assembler::StoreLoad |
                Assembler::LoadStore | Assembler::StoreStore));
   }

  // check for safepoint operation in progress and/or pending suspend requests
  Label L_after_safepoint_poll;
  Label L_safepoint_poll_slow_path;

  __ safepoint_poll(L_safepoint_poll_slow_path, r15_thread, false /* at_return */, false /* in_nmethod */);

  __ cmpl(Address(r15_thread, JavaThread::suspend_flags_offset()), 0);
  __ jcc(Assembler::notEqual, L_safepoint_poll_slow_path);

  __ bind(L_after_safepoint_poll);
  __ block_comment("} safepoint poll");
  // change thread state
  __ movl(Address(r15_thread, JavaThread::thread_state_offset()), _thread_in_Java);

  __ block_comment("{ reguard stack check");
  Label L_reguard;
  Label L_after_reguard;
  __ cmpl(Address(r15_thread, JavaThread::stack_guard_state_offset()), StackOverflow::stack_guard_yellow_reserved_disabled);
  __ jcc(Assembler::equal, L_reguard);
  __ bind(L_after_reguard);
  __ block_comment("} reguard stack check");

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

  save_java_frame_anchor(_masm, jfa_offset, r15_thread);
  __ reset_last_Java_frame(r15_thread, true);

  __ call(Address(rbx, Method::from_compiled_offset()));

  // FIXME: return value post-processing? -> Yes, native result regs might not be the same as Java's

  __ bind(call_return);

  // also sets last Java frame
  __ movptr(r15_thread, Address(rsp, thread_offset));
  restore_java_frame_anchor(_masm, jfa_offset, r15_thread);
  restore_callee_saved_registers(_masm, abi, reg_save_are_offset);

//  __ vzeroupper()
   __ leave();
   __ ret(0);

  //////////////////////////////////////////////////////////////////////////////

  __ block_comment("{ L_safepoint_poll_slow_path");
  __ bind(L_safepoint_poll_slow_path);
  __ vzeroupper();
  save_native_arguments(_masm, abi);
  __ mov(c_rarg0, r15_thread);
  __ mov(r12, rsp); // remember sp
  __ subptr(rsp, frame::arg_reg_save_area_bytes); // windows
  __ andptr(rsp, -16); // align stack as required by ABI
  __ call(RuntimeAddress(CAST_FROM_FN_PTR(address, JavaThread::check_special_condition_for_native_trans)));
  __ mov(rsp, r12); // restore sp
  __ reinit_heapbase();
  restore_native_arguments(_masm, abi);
  __ jmp(L_after_safepoint_poll);

  __ block_comment("} L_safepoint_poll_slow_path");

  //////////////////////////////////////////////////////////////////////////////

  __ block_comment("{ L_reguard");
  __ bind(L_reguard);
  __ vzeroupper();
  save_native_arguments(_masm, abi);
  __ mov(r12, rsp); // remember sp
  __ subptr(rsp, frame::arg_reg_save_area_bytes); // windows
  __ andptr(rsp, -16); // align stack as required by ABI
  __ call(RuntimeAddress(CAST_FROM_FN_PTR(address, SharedRuntime::reguard_yellow_pages)));
  __ mov(rsp, r12); // restore sp
  __ reinit_heapbase();
  restore_native_arguments(_masm, abi);
  __ jmp(L_after_reguard);

  __ block_comment("} L_reguard");

  //////////////////////////////////////////////////////////////////////////////

  __ block_comment("{ exception handler");

  intptr_t exception_handler_offset = __ pc() - start;

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


#ifndef PRODUCT
  stringStream ss;
  ss.print("upcall_stub_linkToNative_J%s", entry->signature()->as_C_string());
  const char* name = _masm->code_string(ss.as_string());
#else // PRODUCT
  const char* name = "upcall_stub_linkToNative";
#endif // PRODUCT

  EntryBlob* blob = EntryBlob::create(name, &buffer, exception_handler_offset, receiver, jfa_offset);

  if (UseNewCode) {
    blob->print_on(tty);
    Disassembler::decode(blob, tty);
  }

   return blob->code_begin();
}
