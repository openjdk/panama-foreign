/*
 * Copyright (c) 2020, 2021, Oracle and/or its affiliates. All rights reserved.
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
#include "foreign_globals.hpp"
#include "classfile/symbolTable.hpp"
#include "classfile/systemDictionary.hpp"
#include "classfile/vmSymbols.hpp"
#include "memory/resourceArea.hpp"
#include "prims/foreign_globals.inline.hpp"
#include "runtime/fieldDescriptor.hpp"
#include "runtime/fieldDescriptor.inline.hpp"

#define FOREIGN_ABI "jdk/internal/foreign/abi/"

static int field_offset(InstanceKlass* cls, const char* fieldname, Symbol* sigsym) {
  TempNewSymbol fieldnamesym = SymbolTable::new_symbol(fieldname, (int)strlen(fieldname));
  fieldDescriptor fd;
  bool success = cls->find_field(fieldnamesym, sigsym, false, &fd);
  assert(success, "Field not found");
  return fd.offset();
}

static InstanceKlass* find_InstanceKlass(const char* name, TRAPS) {
  TempNewSymbol sym = SymbolTable::new_symbol(name, (int)strlen(name));
  Klass* k = SystemDictionary::resolve_or_null(sym, Handle(), Handle(), THREAD);
  assert(k != nullptr, "Can not find class: %s", name);
  return InstanceKlass::cast(k);
}

const ForeignGlobals& ForeignGlobals::instance() {
  static ForeignGlobals globals; // thread-safe lazy init-once (since C++11)
  return globals;
}

const ABIDescriptor ForeignGlobals::parse_abi_descriptor(jobject jabi) {
  return instance().parse_abi_descriptor_impl(jabi);
}
const BufferLayout ForeignGlobals::parse_buffer_layout(jobject jlayout) {
  return instance().parse_buffer_layout_impl(jlayout);
}

const CallRegs ForeignGlobals::parse_call_regs(jobject jconv) {
  return instance().parse_call_regs_impl(jconv);
}

ForeignGlobals::ForeignGlobals() {
  JavaThread* current_thread = JavaThread::current();
  ResourceMark rm(current_thread);

  // ABIDescriptor
  InstanceKlass* k_ABI = find_InstanceKlass(FOREIGN_ABI "ABIDescriptor", current_thread);
  const char* strVMSArrayArray = "[[L" FOREIGN_ABI "VMStorage;";
  Symbol* symVMSArrayArray = SymbolTable::new_symbol(strVMSArrayArray);
  ABI.inputStorage_offset = field_offset(k_ABI, "inputStorage", symVMSArrayArray);
  ABI.outputStorage_offset = field_offset(k_ABI, "outputStorage", symVMSArrayArray);
  ABI.volatileStorage_offset = field_offset(k_ABI, "volatileStorage", symVMSArrayArray);
  ABI.stackAlignment_offset = field_offset(k_ABI, "stackAlignment", vmSymbols::int_signature());
  ABI.shadowSpace_offset = field_offset(k_ABI, "shadowSpace", vmSymbols::int_signature());

  // VMStorage
  InstanceKlass* k_VMS = find_InstanceKlass(FOREIGN_ABI "VMStorage", current_thread);
  VMS.index_offset = field_offset(k_VMS, "index", vmSymbols::int_signature());
  VMS.type_offset = field_offset(k_VMS, "type", vmSymbols::int_signature());

  // BufferLayout
  InstanceKlass* k_BL = find_InstanceKlass(FOREIGN_ABI "BufferLayout", current_thread);
  BL.size_offset = field_offset(k_BL, "size", vmSymbols::long_signature());
  BL.arguments_next_pc_offset = field_offset(k_BL, "arguments_next_pc", vmSymbols::long_signature());
  BL.stack_args_bytes_offset = field_offset(k_BL, "stack_args_bytes", vmSymbols::long_signature());
  BL.stack_args_offset = field_offset(k_BL, "stack_args", vmSymbols::long_signature());
  BL.input_type_offsets_offset = field_offset(k_BL, "input_type_offsets", vmSymbols::long_array_signature());
  BL.output_type_offsets_offset = field_offset(k_BL, "output_type_offsets", vmSymbols::long_array_signature());

  // CallRegs
  const char* strVMSArray = "[L" FOREIGN_ABI "VMStorage;";
  Symbol* symVMSArray = SymbolTable::new_symbol(strVMSArray);
  InstanceKlass* k_CC = find_InstanceKlass(FOREIGN_ABI "ProgrammableUpcallHandler$CallRegs", current_thread);
  CallConvOffsets.arg_regs_offset = field_offset(k_CC, "argRegs", symVMSArray);
  CallConvOffsets.ret_regs_offset = field_offset(k_CC, "retRegs", symVMSArray);
}

int CallRegs::calling_convention(BasicType* sig_bt, VMRegPair *regs, int num_args) const {
  int src_pos = 0;
  for (int i = 0; i < num_args; i++) {
    switch (sig_bt[i]) {
      case T_BOOLEAN:
      case T_CHAR:
      case T_BYTE:
      case T_SHORT:
      case T_INT:
      case T_FLOAT:
        assert(src_pos < _args_length, "oob");
        regs[i].set1(_arg_regs[src_pos++]);
        break;
      case T_LONG:
      case T_DOUBLE:
        assert((i + 1) < num_args && sig_bt[i + 1] == T_VOID, "expecting half");
        assert(src_pos < _args_length, "oob");
        regs[i].set2(_arg_regs[src_pos++]);
        break;
      case T_VOID: // Halves of longs and doubles
        assert(i != 0 && (sig_bt[i - 1] == T_LONG || sig_bt[i - 1] == T_DOUBLE), "expecting half");
        regs[i].set_bad();
        break;
      default:
        ShouldNotReachHere();
        break;
    }
  }
  return 0; // assumed unused
}

int RegSpillFill::compute_spill_area() {
  int result_size = 0;
  for (int i = 0; i < _num_regs; i++) {
    result_size += pd_reg_size(_regs[i]);
  }
  return result_size;
}

void RegSpillFill::gen(MacroAssembler* masm, int rsp_offset, bool spill) const {
  int offset = rsp_offset;
  for (int i = 0; i < _num_regs; i++) {
    VMReg reg = _regs[i];
    if (spill) {
      pd_store_reg(masm, offset, reg);
    } else {
      pd_load_reg(masm, offset, reg);
    }
    offset += pd_reg_size(reg);
  }
}

ArgumentShuffle::ArgumentShuffle(
    BasicType* in_sig_bt,
    int num_in_args,
    BasicType* out_sig_bt,
    int num_out_args,
    const CallConvClosure* input_conv,
    const CallConvClosure* output_conv,
    VMReg shuffle_temp) : _moves(num_in_args), _out_arg_stack_slots(0) {

  VMRegPair* in_regs = NEW_RESOURCE_ARRAY(VMRegPair, num_in_args);
  input_conv->calling_convention(in_sig_bt, in_regs, num_in_args);

  VMRegPair* out_regs = NEW_RESOURCE_ARRAY(VMRegPair, num_out_args);
  _out_arg_stack_slots = output_conv->calling_convention(out_sig_bt, out_regs, num_out_args);

  GrowableArray<int> arg_order(2 * num_in_args);

  VMRegPair tmp_vmreg;
  tmp_vmreg.set2(shuffle_temp);

  // Compute a valid move order, using tmp_vmreg to break any cycles
  SharedRuntime::compute_move_order(in_sig_bt,
                                    num_in_args, in_regs,
                                    num_out_args, out_regs,
                                    arg_order,
                                    tmp_vmreg);

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

    _moves.push(move);
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

int DowncallNativeCallConv::calling_convention(BasicType* sig_bt, VMRegPair* out_regs, int num_args) const {
  out_regs[0].set2(_input_addr_reg); // address
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
        VMReg reg = _input_regs.at(src_pos++);
        out_regs[i].set1(reg);
        if (reg->is_stack())
          stk_slots += 2;
        break;
      }
      case T_LONG:
      case T_DOUBLE: {
        assert((i + 1) < num_args && sig_bt[i + 1] == T_VOID, "expecting half");
        VMReg reg = _input_regs.at(src_pos);
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
  return stk_slots;
}
