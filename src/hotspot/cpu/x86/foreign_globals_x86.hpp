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

#ifndef CPU_X86_VM_FOREIGN_GLOBALS_X86_HPP
#define CPU_X86_VM_FOREIGN_GLOBALS_X86_HPP

#include "asm/macroAssembler.hpp"
#include "utilities/growableArray.hpp"

class outputStream;

constexpr size_t xmm_reg_size = 16; // size of XMM reg

struct ABIDescriptor {
    GrowableArray<Register> _integer_argument_registers;
    GrowableArray<Register> _integer_return_registers;
    GrowableArray<XMMRegister> _vector_argument_registers;
    GrowableArray<XMMRegister> _vector_return_registers;
    size_t _X87_return_registers_noof;

    GrowableArray<Register> _integer_additional_volatile_registers;
    GrowableArray<XMMRegister> _vector_additional_volatile_registers;

    int32_t _stack_alignment_bytes;
    int32_t _shadow_space_bytes;

    bool is_volatile_reg(Register reg) const;
    bool is_volatile_reg(XMMRegister reg) const;
};

struct BufferLayout {
  size_t stack_args_bytes;
  size_t stack_args;
  size_t arguments_vector;
  size_t arguments_integer;
  size_t arguments_next_pc;
  size_t returns_vector;
  size_t returns_integer;
  size_t returns_x87;
  size_t buffer_size;
};

class RegSpillFill {
  const VMReg* _regs;
  int _num_regs;
  int _spill_size_bytes;
public:
  RegSpillFill(const VMReg* regs, int num_regs) : _regs(regs), _num_regs(num_regs) {
    _spill_size_bytes = compute_spill_area();
  }

  int spill_size_bytes() const { return _spill_size_bytes; }
  void gen_spill(MacroAssembler* masm, int rsp_offset) const { return gen(masm, rsp_offset, true); }
  void gen_fill(MacroAssembler* masm, int rsp_offset) const { return gen(masm, rsp_offset, false); }

private:
  int compute_spill_area();
  void gen(MacroAssembler* masm, int rsp_offset, bool is_spill) const;
};

class CallConvClosure {
public:
  virtual int calling_convention(BasicType* sig_bt, VMRegPair* regs, int num_args) = 0;
};

class JavaCallConv : public CallConvClosure {
public:
  int calling_convention(BasicType* sig_bt, VMRegPair* regs, int num_args) override;
};

class ArgumentShuffle {
public:
  struct Move {
    BasicType bt;
    VMRegPair from;
    VMRegPair to;

    bool is_identity() const {
        return (from.first() == to.first() && from.second() == to.second())
          && !from.first()->is_stack(); // stack regs are interpreted differently
    }
  };
private:
  GrowableArray<Move> _moves;
  int _out_arg_stack_slots;
public:
  ArgumentShuffle(
    BasicType* in_sig_bt, int num_in_args,
    BasicType* out_sig_bt, int num_out_args,
    CallConvClosure* input_conv, CallConvClosure* output_conv);

  int out_arg_stack_slots() const { return _out_arg_stack_slots; }
  void gen_shuffle(MacroAssembler* masm, int shuffle_space_rsp_offset = -1) const;

  void print_on(outputStream* os) const;
};

#endif // CPU_X86_VM_FOREIGN_GLOBALS_X86_HPP
