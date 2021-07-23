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
#include "compiler/disassembler.hpp"
#include "logging/logStream.hpp"
#include "memory/resourceArea.hpp"
#include "prims/universalNativeInvoker.hpp"
#include "runtime/sharedRuntime.hpp"
#include "utilities/formatBuffer.hpp"

#define __ _masm->

void ProgrammableInvoker::Generator::generate() {
  __ enter();

  // Put the context pointer in ebx/rbx - it's going to be heavily used below both before and after the call
  Register ctxt_reg = rbx;
  Register used_regs[] = { ctxt_reg, rcx, rsi, rdi };
  GrowableArray<Register> preserved_regs;

  for (size_t i = 0; i < sizeof(used_regs)/sizeof(Register); i++) {
    Register used_reg = used_regs[i];
    if (!_abi->is_volatile_reg(used_reg)) {
      preserved_regs.push(used_reg);
    }
  }

  __ block_comment("init_and_alloc_stack");

  for (int i = 0; i < preserved_regs.length(); i++) {
    __ push(preserved_regs.at(i));
  }

  __ movptr(ctxt_reg, c_rarg0); // FIXME c args? or java?

  __ block_comment("allocate_stack");
  __ movptr(rcx, Address(ctxt_reg, (int) _layout->stack_args_bytes));
  __ subptr(rsp, rcx);
  __ andptr(rsp, -_abi->_stack_alignment_bytes);

  // Note: rcx is used below!


  __ block_comment("load_arguments");

  __ shrptr(rcx, LogBytesPerWord); // bytes -> words
  __ movptr(rsi, Address(ctxt_reg, (int) _layout->stack_args));
  __ movptr(rdi, rsp);
  __ rep_mov();


  for (int i = 0; i < _abi->_vector_argument_registers.length(); i++) {
    // [1] -> 64 bit -> xmm
    // [2] -> 128 bit -> xmm
    // [4] -> 256 bit -> ymm
    // [8] -> 512 bit -> zmm

    XMMRegister reg = _abi->_vector_argument_registers.at(i);
    size_t offs = _layout->arguments_vector + i * xmm_reg_size;
    __ movdqu(reg, Address(ctxt_reg, (int)offs));
  }

  for (int i = 0; i < _abi->_integer_argument_registers.length(); i++) {
    size_t offs = _layout->arguments_integer + i * sizeof(uintptr_t);
    __ movptr(_abi->_integer_argument_registers.at(i), Address(ctxt_reg, (int)offs));
  }

  if (_abi->_shadow_space_bytes != 0) {
    __ block_comment("allocate shadow space for argument register spill");
    __ subptr(rsp, _abi->_shadow_space_bytes);
  }

  // call target function
  __ block_comment("call target function");
  __ call(Address(ctxt_reg, (int) _layout->arguments_next_pc));

  if (_abi->_shadow_space_bytes != 0) {
    __ block_comment("pop shadow space");
    __ addptr(rsp, _abi->_shadow_space_bytes);
  }

  __ block_comment("store_registers");
  for (int i = 0; i < _abi->_integer_return_registers.length(); i++) {
    ssize_t offs = _layout->returns_integer + i * sizeof(uintptr_t);
    __ movptr(Address(ctxt_reg, offs), _abi->_integer_return_registers.at(i));
  }

  for (int i = 0; i < _abi->_vector_return_registers.length(); i++) {
    // [1] -> 64 bit -> xmm
    // [2] -> 128 bit -> xmm (SSE)
    // [4] -> 256 bit -> ymm (AVX)
    // [8] -> 512 bit -> zmm (AVX-512, aka AVX3)

    XMMRegister reg = _abi->_vector_return_registers.at(i);
    size_t offs = _layout->returns_vector + i * xmm_reg_size;
    __ movdqu(Address(ctxt_reg, (int)offs), reg);
  }

  for (size_t i = 0; i < _abi->_X87_return_registers_noof; i++) {
    size_t offs = _layout->returns_x87 + i * (sizeof(long double));
    __ fstp_x(Address(ctxt_reg, (int)offs)); //pop ST(0)
  }

  // Restore backed up preserved register
  for (int i = 0; i < preserved_regs.length(); i++) {
    __ movptr(preserved_regs.at(i), Address(rbp, -(int)(sizeof(uintptr_t) * (i + 1))));
  }

  __ leave();
  __ ret(0);

  __ flush();
}

address ProgrammableInvoker::generate_adapter(jobject jabi, jobject jlayout) {
  ResourceMark rm;
  const ABIDescriptor abi = ForeignGlobals::parse_abi_descriptor(jabi);
  const BufferLayout layout = ForeignGlobals::parse_buffer_layout(jlayout);

  BufferBlob* _invoke_native_blob = BufferBlob::create("invoke_native_blob", native_invoker_size);

  CodeBuffer code2(_invoke_native_blob);
  ProgrammableInvoker::Generator g2(&code2, &abi, &layout);
  g2.generate();
  code2.log_section_sizes("InvokeNativeBlob");

  return _invoke_native_blob->code_begin();
}

static const int native_invoker_code_size = 1024;

class NativeInvokerGenerator : public StubCodeGenerator {
  BasicType* _signature;
  int _num_args;
  BasicType _ret_bt;
  int _shadow_space_bytes;

  const GrowableArray<VMReg>& _input_registers;
  const GrowableArray<VMReg>& _output_registers;

  int _frame_complete;
  int _framesize;
  OopMapSet* _oop_maps;
public:
  NativeInvokerGenerator(CodeBuffer* buffer,
                         BasicType* signature,
                         int num_args,
                         BasicType ret_bt,
                         int shadow_space_bytes,
                         const GrowableArray<VMReg>& input_registers,
                         const GrowableArray<VMReg>& output_registers)
   : StubCodeGenerator(buffer, PrintMethodHandleStubs),
     _signature(signature),
     _num_args(num_args),
     _ret_bt(ret_bt),
     _shadow_space_bytes(shadow_space_bytes),
     _input_registers(input_registers),
     _output_registers(output_registers),
     _frame_complete(0),
     _framesize(0),
     _oop_maps(NULL) {
    assert(_output_registers.length() <= 1
           || (_output_registers.length() == 2 && !_output_registers.at(1)->is_valid()), "no multi-reg returns");

  }

  void generate();

  int spill_size_in_bytes() const {
    if (_output_registers.length() == 0) {
      return 0;
    }
    VMReg reg = _output_registers.at(0);
    assert(reg->is_reg(), "must be a register");
    if (reg->is_Register()) {
      return 8;
    } else if (reg->is_XMMRegister()) {
      // if (UseAVX >= 3) {
      //   return 64;
      // } else if (UseAVX >= 1) {
      //   return 32;
      // } else {
        return 16;
      // }
    } else {
      ShouldNotReachHere();
    }
    return 0;
  }

  void spill_out_registers() {
    if (_output_registers.length() == 0) {
      return;
    }
    VMReg reg = _output_registers.at(0);
    assert(reg->is_reg(), "must be a register");
    MacroAssembler* masm = _masm;
    if (reg->is_Register()) {
      __ movptr(Address(rsp, 0), reg->as_Register());
    } else if (reg->is_XMMRegister()) {
      // if (UseAVX >= 3) {
      //   __ evmovdqul(Address(rsp, 0), reg->as_XMMRegister(), Assembler::AVX_512bit);
      // } else if (UseAVX >= 1) {
      //   __ vmovdqu(Address(rsp, 0), reg->as_XMMRegister());
      // } else {
        __ movdqu(Address(rsp, 0), reg->as_XMMRegister());
      // }
    } else {
      ShouldNotReachHere();
    }
  }

  void fill_out_registers() {
    if (_output_registers.length() == 0) {
      return;
    }
    VMReg reg = _output_registers.at(0);
    assert(reg->is_reg(), "must be a register");
    MacroAssembler* masm = _masm;
    if (reg->is_Register()) {
      __ movptr(reg->as_Register(), Address(rsp, 0));
    } else if (reg->is_XMMRegister()) {
      // if (UseAVX >= 3) {
      //   __ evmovdqul(reg->as_XMMRegister(), Address(rsp, 0), Assembler::AVX_512bit);
      // } else if (UseAVX >= 1) {
      //   __ vmovdqu(reg->as_XMMRegister(), Address(rsp, 0));
      // } else {
        __ movdqu(reg->as_XMMRegister(), Address(rsp, 0));
      // }
    } else {
      ShouldNotReachHere();
    }
  }

  int frame_complete() const {
    return _frame_complete;
  }

  int framesize() const {
    return (_framesize >> (LogBytesPerWord - LogBytesPerInt));
  }

  OopMapSet* oop_maps() const {
    return _oop_maps;
  }

private:
#ifdef ASSERT
bool target_uses_register(VMReg reg) {
  return _input_registers.contains(reg) || _output_registers.contains(reg);
}
#endif
};

RuntimeStub* ProgrammableInvoker::make_native_invoker(BasicType* signature,
                                                      int num_args,
                                                      BasicType ret_bt,
                                                      int shadow_space_bytes,
                                                      const GrowableArray<VMReg>& input_registers,
                                                      const GrowableArray<VMReg>& output_registers) {
#ifdef ASSERT
  LogTarget(Trace, panama) lt;
  if (lt.is_enabled()) {
    ResourceMark rm;
    LogStream ls(lt);
    ls.print_cr("Generating native invoker for %s {", "native_invoker");
    ls.print("BasicType { ");
    for (int i = 0; i < num_args; i++) {
      ls.print("%s, ", type2name(signature[i]));
    }
    ls.print_cr("}");
    ls.print_cr("shadow_space_bytes = %d", shadow_space_bytes);
    ls.print("input_registers { ");
    for (int i = 0; i < input_registers.length(); i++) {
      VMReg reg = input_registers.at(i);
      ls.print("%s (" INTPTR_FORMAT "), ", reg->name(), reg->value());
    }
    ls.print_cr("}");
      ls.print("output_registers { ");
    for (int i = 0; i < output_registers.length(); i++) {
      VMReg reg = output_registers.at(i);
      ls.print("%s (" INTPTR_FORMAT "), ", reg->name(), reg->value());
    }
    ls.print_cr("}");
    ls.print_cr("}");
  }
#endif

  int locs_size  = 64;
  CodeBuffer code("nep_invoker_blob", native_invoker_code_size, locs_size);
  NativeInvokerGenerator g(&code, signature, num_args, ret_bt, shadow_space_bytes, input_registers, output_registers);
  g.generate();
  code.log_section_sizes("nep_invoker_blob");

  RuntimeStub* stub =
    RuntimeStub::new_runtime_stub("nep_invoker_blob",
                                  &code,
                                  g.frame_complete(),
                                  g.framesize(),
                                  g.oop_maps(), false);

  if (TraceNativeInvokers) {
    stub->print_on(tty);
    Disassembler::decode(stub, tty);
  }

  return stub;
}

struct ArgMove {
  BasicType bt;
  VMRegPair from;
  VMRegPair to;

  bool is_identity() const {
      return (from.first() == to.first() && from.second() == to.second())
        && !from.first()->is_stack(); // stack regs are interpreted differently
  }
};

static GrowableArray<ArgMove> compute_argument_shuffle(BasicType* sig_bt, int num_args, const GrowableArray<VMReg>& input_regs, int& out_arg_stk_slots, Register input_addr_reg) {

  VMRegPair* in_regs = NEW_RESOURCE_ARRAY(VMRegPair, num_args);

  SharedRuntime::java_calling_convention(sig_bt, in_regs, num_args);

  VMRegPair* out_regs = NEW_RESOURCE_ARRAY(VMRegPair, num_args);
  out_regs[0].set2(input_addr_reg->as_VMReg()); // address
  out_regs[1].set_bad(); // upper half

  int src_pos = 0;
  int stk_slots = 0;
  for (int i = 2; i < num_args; i++) { // skip address (2)
    switch (sig_bt[i]) {
      case T_BOOLEAN:
      case T_CHAR:
      case T_BYTE:
      case T_SHORT:
      case T_INT:
      case T_FLOAT: {
        VMReg reg = input_regs.at(src_pos++);
        out_regs[i].set1(reg);
        if (reg->is_stack())
          stk_slots += 2;
        break;
      }
      case T_LONG:
      case T_DOUBLE: {
        assert((i + 1) < num_args && sig_bt[i + 1] == T_VOID, "expecting half");
        VMReg reg = input_regs.at(src_pos);
        out_regs[i].set2(reg);
        src_pos += 2; // skip BAD as well
        if (reg->is_stack())
          stk_slots += 2;
        break;
      }
      case T_VOID: // Halves of longs and doubles
        assert(i != 0 && (sig_bt[i - 1] == T_LONG || sig_bt[i - 1] == T_DOUBLE), "expecting half");
        out_regs[i].set_bad();
        break;
      default:
        ShouldNotReachHere();
        break;
    }
  }
  out_arg_stk_slots = stk_slots;

  GrowableArray<int> arg_order(2 * num_args);

  VMRegPair tmp_vmreg;
  tmp_vmreg.set2(rbx->as_VMReg());

  // Compute a valid move order, using tmp_vmreg to break any cycles
  SharedRuntime::compute_move_order(sig_bt,
                                    num_args, in_regs,
                                    num_args, out_regs,
                                    arg_order,
                                    tmp_vmreg);

  GrowableArray<ArgMove> arg_order_vmreg(num_args); // conservative

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
    BasicType arg_bt = (in_arg != -1 ? sig_bt[in_arg] : sig_bt[out_arg]);
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
        assert(in_arg  == -1 || (in_arg  + 1 < num_args && sig_bt[in_arg  + 1] == T_VOID), "bad arg list: %d", in_arg);
        assert(out_arg == -1 || (out_arg + 1 < num_args && sig_bt[out_arg + 1] == T_VOID), "bad arg list: %d", out_arg);
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

    if(move.is_identity()) {
      continue; // useless move
    }

#ifdef ASSERT
    if (in_arg != -1) {
      if (in_regs[in_arg].first()->is_Register()) {
        assert(!reg_destroyed[in_regs[in_arg].first()->as_Register()->encoding()], "destroyed reg!");
      } else if (in_regs[in_arg].first()->is_XMMRegister()) {
        assert(!freg_destroyed[in_regs[in_arg].first()->as_XMMRegister()->encoding()], "destroyed reg!");
      }
    }
    if (out_arg != -1) {
      if (out_regs[out_arg].first()->is_Register()) {
        reg_destroyed[out_regs[out_arg].first()->as_Register()->encoding()] = true;
      } else if (out_regs[out_arg].first()->is_XMMRegister()) {
        freg_destroyed[out_regs[out_arg].first()->as_XMMRegister()->encoding()] = true;
      }
    }
#endif /* ASSERT */

    arg_order_vmreg.push(move);
  }

  return arg_order_vmreg;
}

static const char* null_safe_string(const char* str) {
  return str == nullptr ? "NULL" : str;
}

static bool is_fp_to_gp_move(VMRegPair from, VMRegPair to) {
  return from.first()->is_XMMRegister() && to.first()->is_Register();
}

static void shuffle_arguments(MacroAssembler* _masm, const GrowableArray<ArgMove>& arg_moves, int shuffle_spave_offset) {
  for (int i = 0; i < arg_moves.length(); i++) {
    ArgMove arg_mv = arg_moves.at(i);
    BasicType arg_bt     = arg_mv.bt;
    VMRegPair from_vmreg = arg_mv.from;
    VMRegPair to_vmreg   = arg_mv.to;

    // assert(
    //   !((from_vmreg.first()->is_Register() && to_vmreg.first()->is_XMMRegister())
    //   || (from_vmreg.first()->is_XMMRegister() && to_vmreg.first()->is_Register())),
    //    "move between gp and fp reg not supported");

    Address shuffle_space_addr(rsp, shuffle_spave_offset);

    __ block_comment(err_msg("bt=%s", null_safe_string(type2name(arg_bt))));
    switch (arg_bt) {
      case T_BOOLEAN:
      case T_BYTE:
      case T_SHORT:
      case T_CHAR:
      case T_INT:
       __ move32_64(from_vmreg, to_vmreg);
       break;

      case T_FLOAT:
        if (is_fp_to_gp_move(from_vmreg, to_vmreg)) { // Windows vararg call
          __ movsd(shuffle_space_addr, from_vmreg.first()->as_XMMRegister());
          __ movq(to_vmreg.first()->as_Register(), shuffle_space_addr);
        } else {
          __ float_move(from_vmreg, to_vmreg);
        }
        break;

      case T_DOUBLE:
        if (is_fp_to_gp_move(from_vmreg, to_vmreg)) { // Windows vararg call
          __ movsd(shuffle_space_addr, from_vmreg.first()->as_XMMRegister());
          __ movq(to_vmreg.first()->as_Register(), shuffle_space_addr);
        } else {
          __ double_move(from_vmreg, to_vmreg);
        }
        break;

      case T_LONG :
        __ long_move(from_vmreg, to_vmreg);
        break;

      default:
        fatal("found in upcall args: %s", type2name(arg_bt));
    }
  }
}

void NativeInvokerGenerator::generate() {
  assert(!(target_uses_register(r15_thread->as_VMReg()) || target_uses_register(rscratch1->as_VMReg())), "Register conflict");

  // TODO stack arg slots
  int out_arg_stk_slots = -1;
  Register input_addr_reg = rscratch1;
  GrowableArray<ArgMove> arg_moves = compute_argument_shuffle(_signature, _num_args, _input_registers, out_arg_stk_slots, rscratch1);
  assert(out_arg_stk_slots != -1, "out_arg_size_bytes was not set");

#ifdef ASSERT
  LogTarget(Trace, panama) lt;
  if (lt.is_enabled()) {
    ResourceMark rm;
    LogStream ls(lt);
    ls.print_cr("Argument shuffle for %s {", "native_invoker");
    for (int i = 0; i < arg_moves.length(); i++) {
      ArgMove arg_mv = arg_moves.at(i);
      BasicType arg_bt     = arg_mv.bt;
      VMRegPair from_vmreg = arg_mv.from;
      VMRegPair to_vmreg   = arg_mv.to;

      ls.print("Move a %s from (", null_safe_string(type2name(arg_bt)));
      from_vmreg.first()->print_on(&ls);
      ls.print(",");
      from_vmreg.second()->print_on(&ls);
      ls.print(") to (");
      to_vmreg.first()->print_on(&ls);
      ls.print(",");
      to_vmreg.second()->print_on(&ls);
      ls.print_cr(")");
    }
    ls.print_cr("Stack argument slots: %d", out_arg_stk_slots);
    ls.print_cr("}");
  }
#endif

  enum layout {
    rbp_off,
    rbp_off2,
    return_off,
    return_off2,
    framesize_base // inclusive of return address
    // The following are also computed dynamically:
    // shadow space
    // spill area
    // out arg area (e.g. for stack args)
  };

  // in bytes
  int allocated_frame_size = 0;
  allocated_frame_size += 16;
  allocated_frame_size += out_arg_stk_slots << LogBytesPerInt;
  allocated_frame_size += _shadow_space_bytes;

  // spill area can be shared with the above, so we take the max of the 2
  allocated_frame_size = spill_size_in_bytes() > allocated_frame_size ? spill_size_in_bytes() : allocated_frame_size;
  // in bytes
  const int shuffle_space_offset = _shadow_space_bytes + (out_arg_stk_slots << LogBytesPerInt);

  allocated_frame_size = align_up(allocated_frame_size, 16);
  // _framesize is in 32-bit stack slots:
  _framesize += framesize_base + (allocated_frame_size >> LogBytesPerInt);
  assert(is_even(_framesize/2), "sp not 16-byte aligned");

  _oop_maps  = new OopMapSet();
  MacroAssembler* masm = _masm;

  address start = __ pc();

  __ enter();

  // return address and rbp are already in place
  __ subptr(rsp, allocated_frame_size); // prolog

  _frame_complete = __ pc() - start;

  address the_pc = __ pc();

  __ block_comment("{ thread java2native");
  __ set_last_Java_frame(rsp, rbp, (address)the_pc);
  OopMap* map = new OopMap(_framesize, 0);
  _oop_maps->add_gc_map(the_pc - start, map);

  // State transition
  __ movl(Address(r15_thread, JavaThread::thread_state_offset()), _thread_in_native);
  __ block_comment("} thread java2native");

  __ block_comment("{ argument shuffle");
  shuffle_arguments(_masm, arg_moves, shuffle_space_offset);
  __ block_comment("} argument shuffle");

  __ call(input_addr_reg);

    // Unpack native results.
    switch (_ret_bt) {
      case T_BOOLEAN: __ c2bool(rax);            break;
      case T_CHAR   : __ movzwl(rax, rax);       break;
      case T_BYTE   : __ sign_extend_byte (rax); break;
      case T_SHORT  : __ sign_extend_short(rax); break;
      case T_INT    : /* nothing to do */        break;
      case T_DOUBLE :
      case T_FLOAT  :
        // Result is in xmm0 we'll save as needed
        break;
      case T_VOID: break;
      case T_LONG: break;
      default       : ShouldNotReachHere();
    }

  __ block_comment("{ thread native2java");
  __ restore_cpu_control_state_after_jni();

  __ movl(Address(r15_thread, JavaThread::thread_state_offset()), _thread_in_native_trans);

  // Force this write out before the read below
  __ membar(Assembler::Membar_mask_bits(
          Assembler::LoadLoad | Assembler::LoadStore |
          Assembler::StoreLoad | Assembler::StoreStore));

  Label L_after_safepoint_poll;
  Label L_safepoint_poll_slow_path;

  __ safepoint_poll(L_safepoint_poll_slow_path, r15_thread, true /* at_return */, false /* in_nmethod */);
  __ cmpl(Address(r15_thread, JavaThread::suspend_flags_offset()), 0);
  __ jcc(Assembler::notEqual, L_safepoint_poll_slow_path);

  __ bind(L_after_safepoint_poll);

  // change thread state
  __ movl(Address(r15_thread, JavaThread::thread_state_offset()), _thread_in_Java);

  __ block_comment("reguard stack check");
  Label L_reguard;
  Label L_after_reguard;
  __ cmpl(Address(r15_thread, JavaThread::stack_guard_state_offset()), StackOverflow::stack_guard_yellow_reserved_disabled);
  __ jcc(Assembler::equal, L_reguard);
  __ bind(L_after_reguard);

  __ reset_last_Java_frame(r15_thread, true);
  __ block_comment("} thread native2java");

  __ leave(); // required for proper stackwalking of RuntimeStub frame
  __ ret(0);

  //////////////////////////////////////////////////////////////////////////////

  __ block_comment("{ L_safepoint_poll_slow_path");
  __ bind(L_safepoint_poll_slow_path);
  __ vzeroupper();

  spill_out_registers();

  __ mov(c_rarg0, r15_thread);
  __ mov(r12, rsp); // remember sp
  __ subptr(rsp, frame::arg_reg_save_area_bytes); // windows
  __ andptr(rsp, -16); // align stack as required by ABI
  __ call(RuntimeAddress(CAST_FROM_FN_PTR(address, JavaThread::check_special_condition_for_native_trans)));
  __ mov(rsp, r12); // restore sp
  __ reinit_heapbase();

  fill_out_registers();

  __ jmp(L_after_safepoint_poll);
  __ block_comment("} L_safepoint_poll_slow_path");

  //////////////////////////////////////////////////////////////////////////////

  __ block_comment("{ L_reguard");
  __ bind(L_reguard);
  __ vzeroupper();

  spill_out_registers();

  __ mov(r12, rsp); // remember sp
  __ subptr(rsp, frame::arg_reg_save_area_bytes); // windows
  __ andptr(rsp, -16); // align stack as required by ABI
  __ call(RuntimeAddress(CAST_FROM_FN_PTR(address, SharedRuntime::reguard_yellow_pages)));
  __ mov(rsp, r12); // restore sp
  __ reinit_heapbase();

  fill_out_registers();

  __ jmp(L_after_reguard);

  __ block_comment("} L_reguard");

  //////////////////////////////////////////////////////////////////////////////

  __ flush();
}
