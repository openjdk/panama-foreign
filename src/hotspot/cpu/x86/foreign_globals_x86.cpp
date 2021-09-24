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
#include "runtime/jniHandles.hpp"
#include "runtime/jniHandles.inline.hpp"
#include "oops/typeArrayOop.inline.hpp"
#include "oops/oopCast.inline.hpp"
#include "prims/foreign_globals.hpp"
#include "prims/foreign_globals.inline.hpp"
#include "runtime/sharedRuntime.hpp"

bool ABIDescriptor::is_volatile_reg(Register reg) const {
    return _integer_argument_registers.contains(reg)
        || _integer_additional_volatile_registers.contains(reg);
}

bool ABIDescriptor::is_volatile_reg(XMMRegister reg) const {
    return _vector_argument_registers.contains(reg)
        || _vector_additional_volatile_registers.contains(reg);
}

#define INTEGER_TYPE 0
#define VECTOR_TYPE 1
#define X87_TYPE 2

const ABIDescriptor ForeignGlobals::parse_abi_descriptor_impl(jobject jabi) const {
  oop abi_oop = JNIHandles::resolve_non_null(jabi);
  ABIDescriptor abi;

  objArrayOop inputStorage = oop_cast<objArrayOop>(abi_oop->obj_field(ABI.inputStorage_offset));
  loadArray(inputStorage, INTEGER_TYPE, abi._integer_argument_registers, as_Register);
  loadArray(inputStorage, VECTOR_TYPE, abi._vector_argument_registers, as_XMMRegister);

  objArrayOop outputStorage = oop_cast<objArrayOop>(abi_oop->obj_field(ABI.outputStorage_offset));
  loadArray(outputStorage, INTEGER_TYPE, abi._integer_return_registers, as_Register);
  loadArray(outputStorage, VECTOR_TYPE, abi._vector_return_registers, as_XMMRegister);
  objArrayOop subarray = oop_cast<objArrayOop>(outputStorage->obj_at(X87_TYPE));
  abi._X87_return_registers_noof = subarray->length();

  objArrayOop volatileStorage = oop_cast<objArrayOop>(abi_oop->obj_field(ABI.volatileStorage_offset));
  loadArray(volatileStorage, INTEGER_TYPE, abi._integer_additional_volatile_registers, as_Register);
  loadArray(volatileStorage, VECTOR_TYPE, abi._vector_additional_volatile_registers, as_XMMRegister);

  abi._stack_alignment_bytes = abi_oop->int_field(ABI.stackAlignment_offset);
  abi._shadow_space_bytes = abi_oop->int_field(ABI.shadowSpace_offset);

  return abi;
}

const BufferLayout ForeignGlobals::parse_buffer_layout_impl(jobject jlayout) const {
  oop layout_oop = JNIHandles::resolve_non_null(jlayout);
  BufferLayout layout;

  layout.stack_args_bytes = layout_oop->long_field(BL.stack_args_bytes_offset);
  layout.stack_args = layout_oop->long_field(BL.stack_args_offset);
  layout.arguments_next_pc = layout_oop->long_field(BL.arguments_next_pc_offset);

  typeArrayOop input_offsets = oop_cast<typeArrayOop>(layout_oop->obj_field(BL.input_type_offsets_offset));
  layout.arguments_integer = (size_t) input_offsets->long_at(INTEGER_TYPE);
  layout.arguments_vector = (size_t) input_offsets->long_at(VECTOR_TYPE);

  typeArrayOop output_offsets = oop_cast<typeArrayOop>(layout_oop->obj_field(BL.output_type_offsets_offset));
  layout.returns_integer = (size_t) output_offsets->long_at(INTEGER_TYPE);
  layout.returns_vector = (size_t) output_offsets->long_at(VECTOR_TYPE);
  layout.returns_x87 = (size_t) output_offsets->long_at(X87_TYPE);

  layout.buffer_size = layout_oop->long_field(BL.size_offset);

  return layout;
}

const CallRegs ForeignGlobals::parse_call_regs_impl(jobject jconv) const {
  oop conv_oop = JNIHandles::resolve_non_null(jconv);
  objArrayOop arg_regs_oop = oop_cast<objArrayOop>(conv_oop->obj_field(CallConvOffsets.arg_regs_offset));
  objArrayOop ret_regs_oop = oop_cast<objArrayOop>(conv_oop->obj_field(CallConvOffsets.ret_regs_offset));

  CallRegs result;
  result._args_length = arg_regs_oop->length();
  result._arg_regs = NEW_RESOURCE_ARRAY(VMReg, result._args_length);

  result._rets_length = ret_regs_oop->length();
  result._ret_regs = NEW_RESOURCE_ARRAY(VMReg, result._rets_length);

  for (int i = 0; i < result._args_length; i++) {
    oop storage = arg_regs_oop->obj_at(i);
    jint index = storage->int_field(VMS.index_offset);
    jint type = storage->int_field(VMS.type_offset);
    result._arg_regs[i] = vmstorage_to_vmreg(type, index);
  }

  for (int i = 0; i < result._rets_length; i++) {
    oop storage = ret_regs_oop->obj_at(i);
    jint index = storage->int_field(VMS.index_offset);
    jint type = storage->int_field(VMS.type_offset);
    result._ret_regs[i] = vmstorage_to_vmreg(type, index);
  }

  return result;
}

enum class RegType {
  INTEGER = 0,
  VECTOR = 1,
  X87 = 2,
  STACK = 3
};

VMReg vmstorage_to_vmreg(int type, int index, int stk_slot_offset) {
  switch(static_cast<RegType>(type)) {
    case RegType::INTEGER: return ::as_Register(index)->as_VMReg();
    case RegType::VECTOR: return ::as_XMMRegister(index)->as_VMReg();
    case RegType::STACK: return VMRegImpl::stack2reg(stk_slot_offset + (index LP64_ONLY(* 2))); // numbering on x64 goes per 64-bits
  }
  return VMRegImpl::Bad();
}

int RegSpillFill::compute_spill_area() {
  int result_size = 0;
  for (int i = 0; i < _num_regs; i++) {
    VMReg reg = _regs[i];
    if (reg->is_Register()) {
      result_size += 8;
    } else if (reg->is_XMMRegister()) {
      result_size += 16;
    } else {
      // stack and BAD regs
    }
  }
  return result_size;
}

void RegSpillFill::gen(MacroAssembler* masm, int rsp_offset, bool spill) const {
  int offset = rsp_offset;
  for (int i = 0; i < _num_regs; i++) {
    VMReg reg = _regs[i];
    if (reg->is_Register()) {
      if (spill) {
        masm->movptr(Address(rsp, offset), reg->as_Register());
      } else {
        masm->movptr(reg->as_Register(), Address(rsp, offset));
      }
      offset += 8;
    } else if (reg->is_XMMRegister()) {
      if (spill) {
        masm->movdqu(Address(rsp, offset), reg->as_XMMRegister());
      } else {
        masm->movdqu(reg->as_XMMRegister(), Address(rsp, offset));
      }
      offset += 16;
    } else {
      // stack and BAD regs
    }
  }
}

ArgumentShuffle::ArgumentShuffle(
    BasicType* in_sig_bt,
    int num_in_args,
    BasicType* out_sig_bt,
    int num_out_args,
    CallConvClosure* input_conv,
    CallConvClosure* output_conv) : _moves(num_in_args), _out_arg_stack_slots(0) {

  VMRegPair* in_regs = NEW_RESOURCE_ARRAY(VMRegPair, num_in_args);
  input_conv->calling_convention(in_sig_bt, in_regs, num_in_args);

  VMRegPair* out_regs = NEW_RESOURCE_ARRAY(VMRegPair, num_out_args);
  _out_arg_stack_slots = output_conv->calling_convention(out_sig_bt, out_regs, num_out_args);

  GrowableArray<int> arg_order(2 * num_in_args);

  VMRegPair tmp_vmreg;
  tmp_vmreg.set2(rbx->as_VMReg());

  // Compute a valid move order, using tmp_vmreg to break any cycles
  SharedRuntime::compute_move_order(in_sig_bt,
                                    num_in_args, in_regs,
                                    num_out_args, out_regs,
                                    arg_order,
                                    tmp_vmreg);

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
        assert(in_arg  == -1 || (in_arg  + 1 < num_in_args && in_sig_bt[in_arg  + 1] == T_VOID), "bad arg list: %d", in_arg);
        assert(out_arg == -1 || (out_arg + 1 < num_out_args && out_sig_bt[out_arg + 1] == T_VOID), "bad arg list: %d", out_arg);
        break; // process

      case T_VOID:
        continue; // skip

      default:
        fatal("found in upcall args: %s", type2name(arg_bt));
    }

    Move move;
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

    _moves.push(move);
  }
}

static bool is_fp_to_gp_move(VMRegPair from, VMRegPair to) {
  return from.first()->is_XMMRegister() && to.first()->is_Register();
}

void ArgumentShuffle::gen_shuffle(MacroAssembler* masm, int shuffle_space_rsp_offset) const {
  for (int i = 0; i < _moves.length(); i++) {
    Move move = _moves.at(i);
    BasicType arg_bt     = move.bt;
    VMRegPair from_vmreg = move.from;
    VMRegPair to_vmreg   = move.to;

    masm->block_comment(err_msg("bt=%s", null_safe_string(type2name(arg_bt))));
    switch (arg_bt) {
      case T_BOOLEAN:
      case T_BYTE:
      case T_SHORT:
      case T_CHAR:
      case T_INT:
       masm->move32_64(from_vmreg, to_vmreg);
       break;

      case T_FLOAT:
        if (is_fp_to_gp_move(from_vmreg, to_vmreg)) { // Windows vararg call
          assert(shuffle_space_rsp_offset != -1, "shuffle area not available");
          Address shuffle_space_addr(rsp, shuffle_space_rsp_offset);
          masm->movsd(shuffle_space_addr, from_vmreg.first()->as_XMMRegister());
          masm->movq(to_vmreg.first()->as_Register(), shuffle_space_addr);
        } else {
          masm->float_move(from_vmreg, to_vmreg);
        }
        break;

      case T_DOUBLE:
        if (is_fp_to_gp_move(from_vmreg, to_vmreg)) { // Windows vararg call
          assert(shuffle_space_rsp_offset != -1, "shuffle area not available");
          Address shuffle_space_addr(rsp, shuffle_space_rsp_offset);
          masm->movsd(shuffle_space_addr, from_vmreg.first()->as_XMMRegister());
          masm->movq(to_vmreg.first()->as_Register(), shuffle_space_addr);
        } else {
          masm->double_move(from_vmreg, to_vmreg);
        }
        break;

      case T_LONG :
        masm->long_move(from_vmreg, to_vmreg);
        break;

      default:
        fatal("found in upcall args: %s", type2name(arg_bt));
    }
  }
}

void ArgumentShuffle::print_on(outputStream* os) const {
  os->print_cr("Argument shuffle {");
  for (int i = 0; i < _moves.length(); i++) {
    Move move = _moves.at(i);
    BasicType arg_bt     = move.bt;
    VMRegPair from_vmreg = move.from;
    VMRegPair to_vmreg   = move.to;

    os->print("Move a %s from (", null_safe_string(type2name(arg_bt)));
    from_vmreg.first()->print_on(os);
    os->print(",");
    from_vmreg.second()->print_on(os);
    os->print(") to (");
    to_vmreg.first()->print_on(os);
    os->print(",");
    to_vmreg.second()->print_on(os);
    os->print_cr(")");
  }
  os->print_cr("Stack argument slots: %d", _out_arg_stack_slots);
  os->print_cr("}");
}

int JavaCallConv::calling_convention(BasicType* sig_bt, VMRegPair* regs, int num_args) {
  return SharedRuntime::java_calling_convention(sig_bt, regs, num_args);
}
