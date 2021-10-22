/*
 * Copyright (c) 2020, 2022, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2019, 2022, Arm Limited. All rights reserved.
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
#include "code/vmreg.inline.hpp"
#include "runtime/jniHandles.hpp"
#include "runtime/jniHandles.inline.hpp"
#include "oops/typeArrayOop.inline.hpp"
#include "oops/oopCast.inline.hpp"
#include "prims/foreignGlobals.hpp"
#include "prims/foreignGlobals.inline.hpp"
#include "prims/vmstorage.inline.hpp"
#include "utilities/formatBuffer.hpp"

bool ABIDescriptor::is_volatile_reg(Register reg) const {
  return _integer_argument_registers.contains(reg)
    || _integer_additional_volatile_registers.contains(reg);
}

bool ABIDescriptor::is_volatile_reg(FloatRegister reg) const {
    return _vector_argument_registers.contains(reg)
        || _vector_additional_volatile_registers.contains(reg);
}

const ABIDescriptor ForeignGlobals::parse_abi_descriptor(jobject jabi) {
  oop abi_oop = JNIHandles::resolve_non_null(jabi);
  ABIDescriptor abi;

  objArrayOop inputStorage = jdk_internal_foreign_abi_ABIDescriptor::inputStorage(abi_oop);
  parse_register_array(inputStorage, (int) RegType::INTEGER, abi._integer_argument_registers, as_Register);
  parse_register_array(inputStorage, (int) RegType::VECTOR, abi._vector_argument_registers, as_FloatRegister);

  objArrayOop outputStorage = jdk_internal_foreign_abi_ABIDescriptor::outputStorage(abi_oop);
  parse_register_array(outputStorage, (int) RegType::INTEGER, abi._integer_return_registers, as_Register);
  parse_register_array(outputStorage, (int) RegType::VECTOR, abi._vector_return_registers, as_FloatRegister);

  objArrayOop volatileStorage = jdk_internal_foreign_abi_ABIDescriptor::volatileStorage(abi_oop);
  parse_register_array(volatileStorage, (int) RegType::INTEGER, abi._integer_additional_volatile_registers, as_Register);
  parse_register_array(volatileStorage, (int) RegType::VECTOR, abi._vector_additional_volatile_registers, as_FloatRegister);

  abi._stack_alignment_bytes = jdk_internal_foreign_abi_ABIDescriptor::stackAlignment(abi_oop);
  abi._shadow_space_bytes = jdk_internal_foreign_abi_ABIDescriptor::shadowSpace(abi_oop);

  abi._target_addr_reg = as_Register(parse_vmstorage(jdk_internal_foreign_abi_ABIDescriptor::targetAddrStorage(abi_oop)));
  abi._ret_buf_addr_reg = as_Register(parse_vmstorage(jdk_internal_foreign_abi_ABIDescriptor::retBufAddrStorage(abi_oop)));

  return abi;
}

int RegSpiller::pd_reg_size(VMStorage reg) {
  if (reg.type() == RegType::INTEGER) {
    return 8;
  } else if (reg.type() == RegType::VECTOR) {
    return 16;   // Always spill/unspill Q registers
  }
  return 0; // stack and BAD
}

void RegSpiller::pd_store_reg(MacroAssembler* masm, int offset, VMStorage reg) {
  if (reg.type() == RegType::INTEGER) {
    masm->spill(as_Register(reg), true, offset);
  } else if (reg.type() == RegType::VECTOR) {
    masm->spill(as_FloatRegister(reg), masm->Q, offset);
  } else {
    // stack and BAD
  }
}

void RegSpiller::pd_load_reg(MacroAssembler* masm, int offset, VMStorage reg) {
  if (reg.type() == RegType::INTEGER) {
    masm->unspill(as_Register(reg), true, offset);
  } else if (reg.type() == RegType::VECTOR) {
    masm->unspill(as_FloatRegister(reg), masm->Q, offset);
  } else {
    // stack and BAD
  }
}

constexpr int RFP_BIAS = 16; // skip old rfp and lr

static void move_reg64(MacroAssembler* masm, int out_stk_bias,
                       Register from_reg, VMStorage to_reg) {
  switch (to_reg.type()) {
    case RegType::INTEGER:
      assert(to_reg.segment_mask() == REG64_MASK, "only moves to 64-bit registers supported");
      masm->mov(as_Register(to_reg), from_reg);
      break;
    case RegType::STACK:
      switch (to_reg.stack_size()) {
        // FIXME use correctly sized stores
        case 8: case 4: case 2: case 1:
          masm->str(from_reg, Address(sp, to_reg.index() + out_stk_bias));
        break;
        default: ShouldNotReachHere();
      }
      break;
    default: ShouldNotReachHere();
  }
}

static void move_stack(MacroAssembler* masm, Register tmp_reg, int in_stk_bias, int out_stk_bias,
                       VMStorage from_reg, VMStorage to_reg) {
  switch (to_reg.type()) {
    case RegType::INTEGER:
      assert(to_reg.segment_mask() == REG64_MASK, "only moves to 64-bit registers supported");
      switch (from_reg.stack_size()) {
        // FIXME use correctly sized loads
        case 8: case 4: case 2: case 1:
          masm->ldr(as_Register(to_reg), Address(rfp, RFP_BIAS + from_reg.index() + in_stk_bias));
        break;
        default: ShouldNotReachHere();
      }
      break;
    case RegType::VECTOR:
      assert(to_reg.segment_mask() == V128_MASK, "only moves to v128 registers supported");
      switch (from_reg.stack_size()) {
        case 8:
          masm->ldrd(as_FloatRegister(to_reg), Address(rfp, RFP_BIAS + from_reg.index() + in_stk_bias));
        break;
        case 4:
          masm->ldrs(as_FloatRegister(to_reg), Address(rfp, RFP_BIAS + from_reg.index() + in_stk_bias));
        break;
        default: ShouldNotReachHere();
      }
      break;
    case RegType::STACK:
      assert(from_reg.stack_size() == to_reg.stack_size(), "must be same");
      switch (from_reg.stack_size()) {
        // FIXME use correctly sized loads & stores
        case 8: case 4: case 2: case 1:
          masm->ldr(tmp_reg, Address(rfp, RFP_BIAS + from_reg.index() + in_stk_bias));
          masm->str(tmp_reg, Address(sp, to_reg.index() + out_stk_bias));
        break;
        default: ShouldNotReachHere();
      }
      break;
    default: ShouldNotReachHere();
  }
}

static void move_v128(MacroAssembler* masm, int out_stk_bias,
                      FloatRegister from_reg, VMStorage to_reg) {
  switch (to_reg.type()) {
    case RegType::VECTOR:
      assert(to_reg.segment_mask() == V128_MASK, "only moves to v128 registers supported");
      masm->fmovd(as_FloatRegister(to_reg), from_reg);
      break;
    case RegType::STACK:
      switch(to_reg.stack_size()) {
        case 8:
          masm->strd(from_reg, Address(sp, to_reg.index() + out_stk_bias));
        break;
        case 4:
          masm->strs(from_reg, Address(sp, to_reg.index() + out_stk_bias));
        break;
        default: ShouldNotReachHere();
      }
      break;
    default: ShouldNotReachHere();
  }
}

void ArgumentShuffle::pd_generate(MacroAssembler* masm, VMStorage tmp, int in_stk_bias, int out_stk_bias) const {
  Register tmp_reg = as_Register(tmp);
  for (int i = 0; i < _moves.length(); i++) {
    Move move = _moves.at(i);
    VMStorage from_reg = move.from;
    VMStorage to_reg   = move.to;

    switch (from_reg.type()) {
      case RegType::INTEGER:
        assert(from_reg.segment_mask() == REG64_MASK, "only 64-bit register supported");
        move_reg64(masm, out_stk_bias, as_Register(from_reg), to_reg);
        break;
      case RegType::VECTOR:
        assert(from_reg.segment_mask() == V128_MASK, "only v128 register supported");
        move_v128(masm, out_stk_bias, as_FloatRegister(from_reg), to_reg);
        break;
      case RegType::STACK:
        move_stack(masm, tmp_reg, in_stk_bias, out_stk_bias, from_reg, to_reg);
        break;
      default: ShouldNotReachHere();
    }
  }
}

int JavaCallingConvention::calling_convention(const BasicType* sig_bt, VMStorage* regs, int num_args) const {

  // Create the mapping between argument positions and
  // registers.
  static const Register INT_ArgReg[Argument::n_int_register_parameters_j] = {
    j_rarg0, j_rarg1, j_rarg2, j_rarg3, j_rarg4, j_rarg5, j_rarg6, j_rarg7
  };
  static const FloatRegister FP_ArgReg[Argument::n_float_register_parameters_j] = {
    j_farg0, j_farg1, j_farg2, j_farg3,
    j_farg4, j_farg5, j_farg6, j_farg7
  };

  uint int_args = 0;
  uint fp_args = 0;
  uint stk_offset = 0;
  constexpr uint SLOT_SIZE = 8;

  for (int i = 0; i < num_args; i++) {
    switch (sig_bt[i]) {
    case T_BOOLEAN:
    case T_CHAR:
    case T_BYTE:
    case T_SHORT:
    case T_INT:
      if (int_args < Argument::n_int_register_parameters_j) {
        regs[i] = as_VMStorage(INT_ArgReg[int_args++]);
      } else {
        regs[i] = VMStorage::stack_storage(SLOT_SIZE, stk_offset);
        stk_offset += SLOT_SIZE;
      }
      break;
    case T_VOID:
      // halves of T_LONG or T_DOUBLE
      assert(i != 0 && (sig_bt[i - 1] == T_LONG || sig_bt[i - 1] == T_DOUBLE), "expecting half");
      regs[i] = VMStorage::invalid();
      break;
    case T_LONG:
      assert((i + 1) < num_args && sig_bt[i + 1] == T_VOID, "expecting half");
      // fall through
    case T_OBJECT:
    case T_ARRAY:
    case T_ADDRESS:
      if (int_args < Argument::n_int_register_parameters_j) {
        regs[i] = as_VMStorage(INT_ArgReg[int_args++]);
      } else {
        regs[i] = VMStorage::stack_storage(SLOT_SIZE, stk_offset);
        stk_offset += SLOT_SIZE;
      }
      break;
    case T_FLOAT:
      if (fp_args < Argument::n_float_register_parameters_j) {
        regs[i] = as_VMStorage(FP_ArgReg[fp_args++]);
      } else {
        regs[i] = VMStorage::stack_storage(SLOT_SIZE, stk_offset);
        stk_offset += SLOT_SIZE;
      }
      break;
    case T_DOUBLE:
      assert((i + 1) < num_args && sig_bt[i + 1] == T_VOID, "expecting half");
      if (fp_args < Argument::n_float_register_parameters_j) {
        regs[i] = as_VMStorage(FP_ArgReg[fp_args++]);
      } else {
        regs[i] = VMStorage::stack_storage(SLOT_SIZE, stk_offset);
        stk_args += SLOT_SIZE;
      }
      break;
    default:
      ShouldNotReachHere();
      break;
    }
  }

  return align_up(stk_offset, 16);
}
