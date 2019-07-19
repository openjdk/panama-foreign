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
#include "include/jvm.h"
#include "logging/log.hpp"
#include "logging/logStream.hpp"
#include "memory/resourceArea.hpp"
#include "oops/arrayOop.inline.hpp"
#include "prims/universalNativeInvoker.hpp"
#include "runtime/interfaceSupport.inline.hpp"
#include "runtime/javaCalls.hpp"

//// shuffle recipe based (mikael style) stub ////

//#define DEBUG_OPS
//#define DEBUG_CALLS

typedef void (*InvokeNativeStub)(struct ShuffleDowncallContext* ctxt);

size_t ShuffleRecipe::storage_class_max_width(ShuffleRecipeStorageClass c) {
  switch (c) {
  case CLASS_BUF:
  case CLASS_STACK:
  case CLASS_INTEGER:
  case CLASS_INDIRECT:
    return 1;

  case CLASS_VECTOR:
    return 2;

  default:
    assert(false, "Unexpected class");
    return 1;
  }
}

struct ShuffleDowncallContext {
  struct {
    uint64_t integer[INTEGER_ARGUMENT_REGISTERS_NOOF];
    VectorRegister vector[VECTOR_ARGUMENT_REGISTERS_NOOF];
    uintptr_t indirect;
    uint64_t* stack_args;
    size_t stack_args_bytes;
    address next_pc;
  } arguments;

  struct {
    uint64_t integer[INTEGER_RETURN_REGISTERS_NOOF];
    VectorRegister vector[VECTOR_RETURN_REGISTERS_NOOF];
  } returns;
};

static void dump_vector_register(outputStream* out, FloatRegister reg, VectorRegister value) {
  out->print("%s = {", reg->name());
  for (size_t i = 0; i < VectorRegister::VECTOR_MAX_WIDTH_U64S; i++) {
    if (i != 0) {
      out->print(",");
    }
    out->print(" 0x%016" PRIx64, value.u64[i]);
  }
  out->print_cr(" }");
}

static void dump_integer_register(outputStream* out, Register reg, uint64_t value) {
  out->print_cr("%s = 0x%016" PRIx64, reg->name(), value);
}

#ifdef _LP64
static void dump_stack_slot(outputStream* out, size_t offset, uint64_t value) {
  out->print_cr("[sp+0x%zx] = 0x%016" PRIx64, offset, value);
}
#endif

static void dump_argument_registers(struct ShuffleDowncallContext* ctxt) {
#ifdef _LP64
  LogTarget(Info, panama) lt;
  if (lt.is_enabled()) {
    LogStream ls(lt);
    ls.print("Argument registers:\n");
    for (size_t i = 0; i < INTEGER_ARGUMENT_REGISTERS_NOOF; i++) {
      dump_integer_register(&ls, integer_argument_registers[i], ctxt->arguments.integer[i]);
    }
    for (size_t i = 0; i < VECTOR_ARGUMENT_REGISTERS_NOOF; i++) {
      dump_vector_register(&ls, vector_argument_registers[i], ctxt->arguments.vector[i]);
    }
#ifdef X86
    dump_integer_register(&ls, rax, ctxt->arguments.rax);
#endif

    for (size_t i = 0; i < ctxt->arguments.stack_args_bytes; i += sizeof(uint64_t)) {
      size_t slot = i / sizeof(uint64_t);
      dump_stack_slot(&ls, i, ctxt->arguments.stack_args[slot]);
    }
  }
#endif
}

static void dump_return_registers(struct ShuffleDowncallContext* ctxt) {
  LogTarget(Info, panama) lt;
  if (lt.is_enabled()) {
    LogStream ls(lt);
    ls.print("Return registers:\n");
    for (size_t i = 0; i < INTEGER_RETURN_REGISTERS_NOOF; i++) {
      dump_integer_register(&ls, integer_return_registers[i], ctxt->returns.integer[i]);
    }
    for (size_t i = 0; i < VECTOR_RETURN_REGISTERS_NOOF; i++) {
      dump_vector_register(&ls, vector_return_registers[i], ctxt->returns.vector[i]);
    }
  }
}


class ShuffleDowncall : public StackObj {
public:
  ShuffleDowncall(ShuffleRecipe& recipe, arrayHandle args, arrayHandle rets, address code, address stub)
    : _recipe(recipe), _stream(recipe), _args(args), _rets(rets), _code(code), _stub((InvokeNativeStub)stub) {
    memset(&_context.arguments, 0, sizeof(_context.arguments));
    memset(&_context.returns, 0, sizeof(_context.returns));

    _context.arguments.stack_args_bytes = _recipe.stack_args_slots() * sizeof(uint64_t);

    if (_context.arguments.stack_args_bytes == 0) {
      _context.arguments.stack_args = NULL;
    } else {
      _context.arguments.stack_args = NEW_RESOURCE_ARRAY(uint64_t, _context.arguments.stack_args_bytes / sizeof(uint64_t));
      memset(_context.arguments.stack_args, 0, _context.arguments.stack_args_bytes);
    }

    _context.arguments.next_pc = code;

    _buffers = NEW_RESOURCE_ARRAY(uint64_t, _recipe.buffer_slots());
  }

  void invoke(JavaThread* thread) {
#ifdef DEBUG_CALLS
    ::fprintf(stderr, "calling target function\n");
#endif

    prepare_call();

    dump_argument_registers(&_context);

    {
      assert(thread->thread_state() == _thread_in_vm, "thread state is: %d", thread->thread_state());
      ThreadToNativeFromVM ttnfvm(thread);
      assert(thread->thread_state() == _thread_in_native, "thread state is: %d", thread->thread_state());
      _stub(context());
      assert(thread->thread_state() == _thread_in_native, "thread state is: %d", thread->thread_state());
    }
    assert(thread->thread_state() == _thread_in_vm, "thread state is: %d", thread->thread_state());

    dump_return_registers(&_context);

    process_returns();
  }

private:
  void copy_argument_value(void** src_addrp) {
    LogTarget(Debug, panama) lt;
    if (lt.is_enabled()) {
      LogStream ls(lt);
      uint64_t* values = (uint64_t*)*src_addrp;
      for (size_t i = 0; i < _npulls; i++) {
        ls.print_cr("Pulling %3zd times to %20s[%3zd]: 0x%" PRIx64 "\n",
          _npulls, ShuffleRecipe::storage_class_name(_cur_class), _index_in_class, values[i]);
      }
    }

    switch (_cur_class) {
    case CLASS_BUF:
      assert(_index_in_class < _recipe.buffer_slots(), "out of bounds");
      memcpy((char*)_buffers + _index_in_class * sizeof(uint64_t), *src_addrp, sizeof(void*));
      break;

    case CLASS_STACK:
      assert(_index_in_class < _recipe.stack_args_slots(), "out of bounds");
      memcpy(&_context.arguments.stack_args[_index_in_class], *src_addrp, sizeof(uint64_t));
      if (lt.is_enabled()) {
        LogStream ls(lt);
        ls.print_cr("Pulling stack value: 0x%" PRIx64 "\n", *(uint64_t*)*src_addrp);
      }
      break;

#ifdef _LP64
    case CLASS_VECTOR:
      assert(_index_in_class < VECTOR_ARGUMENT_REGISTERS_NOOF, "out of bounds");
      memcpy(&_context.arguments.vector[_index_in_class], *src_addrp, _npulls * sizeof(uint64_t));
      break;

    case CLASS_INTEGER:
      assert(_index_in_class < INTEGER_ARGUMENT_REGISTERS_NOOF, "out of bounds");
      memcpy(&_context.arguments.integer[_index_in_class], *src_addrp, sizeof(uint64_t));
      break;
#endif

    case CLASS_INDIRECT:
      assert(_index_in_class == 0, "index must be zero");
      _context.arguments.indirect = *(uint64_t *)*src_addrp;
      break;

    default:
      assert(false, "Invalid class");
      break;
    }

    *src_addrp = (char*)*src_addrp + _npulls * sizeof(uint64_t);
    _npulls = 0;
    _index_in_class++;
  }

  void copy_return_value(void** dst_addrp) {
    switch (_cur_class) {
    case CLASS_BUF:
      assert(_index_in_class < _recipe.buffer_slots(), "out of bounds");
      memcpy(*dst_addrp, (char*)_buffers + _index_in_class * sizeof(uint64_t), sizeof(void*));
      break;

    case CLASS_STACK:
      assert(false, "Invalid class");
      break;

    case CLASS_VECTOR:
      assert(_index_in_class < VECTOR_RETURN_REGISTERS_NOOF, "out of bounds");
      memcpy(*dst_addrp, &_context.returns.vector[_index_in_class], _npulls * sizeof(uint64_t));
      break;

    case CLASS_INTEGER:
      assert(_index_in_class < INTEGER_RETURN_REGISTERS_NOOF, "out of bounds");
      memcpy(*dst_addrp, &_context.returns.integer[_index_in_class], sizeof(uint64_t));
      break;

#ifdef X86
    case CLASS_X87: {
      assert(_index_in_class < X87_RETURN_REGISTERS_NOOF, "out of bounds");
      memcpy(*dst_addrp, &_context.returns.x87[_index_in_class], _npulls * sizeof(uint64_t));
      break;
    }
#endif

    default:
      assert(false, "Invalid class");
      break;
    }

    *dst_addrp = (char*)*dst_addrp + _npulls * sizeof(uint64_t);
    _npulls = 0;
    _index_in_class++;
  }

  void prepare_call() {
#ifdef DEBUG_CALLS
    ::fprintf(stderr, "preparing_call()\n");
#endif

    _cur_class = CLASS_FIRST;
    _index_in_class = 0;
    _npulls = 0;

    void* cur_value_data = _args()->base(T_LONG);
    struct {
      void** labels;
      size_t produce;
      size_t consume;
    } labels;

    labels.labels = NEW_RESOURCE_ARRAY(void*, _recipe.nlabels());
    labels.produce = labels.consume = 0;

    while(_stream.has_more()) {
      ShuffleRecipeOp op = _stream.next();

#ifdef DEBUG_OPS
      ::fprintf(stderr, "OP: %s\n", op2name(op));
#endif

      switch (op) {
      case OP_NOP:
        break;

      case OP_STOP:
        if (_npulls > 0) {
          copy_argument_value(&cur_value_data);
        }

#if defined _LP64 && defined X86
        if (_cur_class == CLASS_VECTOR) {
          _context.arguments.rax = _index_in_class;
        }
#endif

        _index_in_class = 0;
        _cur_class = ShuffleRecipe::next_storage_class(_cur_class);
        break;

      case OP_CREATE_BUFFER:
        assert(labels.produce < _recipe.nlabels(), "out of bounds");
        _labels[labels.produce] = (void*)((char*)_buffers + _index_in_class * sizeof(uint64_t));
        labels.produce++;
        break;

      case OP_PULL_LABEL:
        assert(labels.consume < labels.produce, "out of bounds");
        copy_argument_value(&_labels[labels.consume]);
        break;

      case OP_SKIP:
        if (_npulls > 0) {
          copy_argument_value(&cur_value_data);
        } else {
          _index_in_class++;
        }
        break;

      case OP_PULL:
        _npulls++;
        if (_npulls == ShuffleRecipe::storage_class_max_width(_cur_class)) {
          copy_argument_value(&cur_value_data);
        }
        break;

      default:
        assert(false, "Unexpected op");
        break;
      }
    }
  }

  void process_returns() {
#ifdef DEBUG_CALLS
    ::fprintf(stderr, "processing returns\n");
#endif

    _stream.init_for_returns();

    void* cur_value_data = _rets()->base(T_LONG);
    struct {
      size_t consume;
    } labels = { 0 };

    _cur_class = CLASS_FIRST;
    // FIXME: What about buffers?

    while(_stream.has_more()) {
      ShuffleRecipeOp op = _stream.next();

      switch (op) {
      case OP_NOP:
        break;

      case OP_STOP:
        if (_npulls > 0) {
          copy_return_value(&cur_value_data);
        }

        _index_in_class = 0;
        _cur_class = ShuffleRecipe::next_storage_class(_cur_class);
        break;

      case OP_CREATE_BUFFER:
        assert(false, "Creating buffer now allowed in this phase");
        break;

      case OP_PULL_LABEL:
        assert(labels.consume < _recipe.nlabels(), "out of bounds");
        copy_return_value((void**)&_labels[labels.consume]);
        break;

      case OP_SKIP:
        if (_npulls > 0) {
          copy_return_value(&cur_value_data);
        } else {
          _index_in_class++;
        }
        break;

      case OP_PULL:
        _npulls++;
        if (_npulls == ShuffleRecipe::storage_class_max_width(_cur_class)) {
          copy_return_value(&cur_value_data);
        }
        break;

      default:
        assert(false, "Unexpected op");
        break;
      }
    }
  }

  struct ShuffleDowncallContext* context() { return &_context; }

  ShuffleRecipe& _recipe;
  ShuffleRecipeStream _stream;
  arrayHandle _args;
  arrayHandle _rets;
  address _code;
  InvokeNativeStub _stub;

  struct ShuffleDowncallContext _context;

  ShuffleRecipeStorageClass _cur_class;
  size_t _index_in_class;
  size_t _npulls;

  void* _src_addr;

  void* _cur_value_data;

  void* _buffers;
  void** _labels;
};

void UniversalNativeInvoker::generate_invoke_native(MacroAssembler* _masm) {

  /**
   * invoke_native_stub(struct ShuffleDowncallContext* ctxt) {
   *   rbx = ctxt;
   *
   *   stack = alloca(ctxt->arguments.stack_args_bytes);
   *
   *   load_all_registers();
   *   memcpy(stack, ctxt->arguments.stack_args, arguments.stack_args_bytes);
   *
   *   (*ctxt->arguments.next_pc)();
   *
   *   store_all_registers();
   * }
   */

  __ enter();

  // Name registers used in the stub code. These are all caller-save so
  // may be clobbered by the call to the native function. Avoid using
  // rscratch1 here as it's r8 which is the indirect result register in
  // the standard ABI.
  Register Rctx = r10, Rstack_size = r11;
  Register Rwords = r12, Rtmp = r13;
  Register Rsrc_ptr = r14, Rdst_ptr = r15;

  assert_different_registers(Rctx, Rstack_size, rscratch1, rscratch2);

  __ block_comment("init_and_alloc_stack");

  __ mov(Rctx, c_rarg0);
  __ str(Rctx, Address(__ pre(sp, -2 * wordSize)));

  __ block_comment("allocate_stack");
  __ ldr(Rstack_size, Address(Rctx, offsetof(struct ShuffleDowncallContext,
                                             arguments.stack_args_bytes)));
  __ add(rscratch2, Rstack_size, 15);
  __ andr(rscratch2, rscratch2, -16);    // SP must always be 16 byte aligned
  __ sub(sp, sp, rscratch2);

  __ block_comment("load_arguments");

  __ ldr(Rsrc_ptr, Address(Rctx, offsetof(struct ShuffleDowncallContext,
                                          arguments.stack_args)));
  __ lsr(Rwords, Rstack_size, LogBytesPerWord);
  __ mov(Rdst_ptr, sp);

  Label Ldone, Lnext;
  __ bind(Lnext);
  __ cbz(Rwords, Ldone);
  __ ldr(Rtmp, __ post(Rsrc_ptr, wordSize));
  __ str(Rtmp, __ post(Rdst_ptr, wordSize));
  __ sub(Rwords, Rwords, 1);
  __ b(Lnext);
  __ bind(Ldone);

  const size_t vector_arg_base = offsetof(struct ShuffleDowncallContext, arguments.vector);
  for (size_t i = 0; i < VECTOR_ARGUMENT_REGISTERS_NOOF; i++) {
    size_t offs = vector_arg_base + i * sizeof(VectorRegister);
    __ ldrq(vector_argument_registers[i], Address(Rctx, (int)offs));
  }

  const size_t integer_arg_base = offsetof(struct ShuffleDowncallContext, arguments.integer);
  for (size_t i = 0; i < INTEGER_ARGUMENT_REGISTERS_NOOF; i++) {
    size_t offs = integer_arg_base + i * sizeof(uintptr_t);
    __ ldr(integer_argument_registers[i], Address(Rctx, (int)offs));
  }

  // Temporary storage for struct returned by value
  __ ldr(r8, Address(Rctx, offsetof(struct ShuffleDowncallContext, arguments.indirect)));

  // call target function
  __ block_comment("call target function");
  __ ldr(rscratch2, Address(Rctx, offsetof(struct ShuffleDowncallContext,
                                           arguments.next_pc)));
  __ blr(rscratch2);

  __ ldr(Rctx, Address(rfp, -2 * wordSize));   // Might have clobbered Rctx

  __ block_comment("store_registers");

  const size_t integer_return_base = offsetof(struct ShuffleDowncallContext, returns.integer);
  for (size_t i = 0; i < INTEGER_RETURN_REGISTERS_NOOF; i++) {
    ssize_t offs = integer_return_base + i * sizeof(uintptr_t);
    __ str(integer_return_registers[i], Address(Rctx, offs));
  }

  const size_t vector_return_base = offsetof(struct ShuffleDowncallContext, returns.vector);
  for (size_t i = 0; i < VECTOR_RETURN_REGISTERS_NOOF; i++) {
    ssize_t offs = vector_return_base + i * sizeof(VectorRegister);
    __ lea(rscratch2, Address(Rctx, offs));
    __ strq(vector_return_registers[i], rscratch2);
  }

  __ leave();
  __ ret(lr);

  __ flush();
}

void UniversalNativeInvoker::invoke_recipe(ShuffleRecipe& recipe, arrayHandle args_arr,
                                           arrayHandle rets_arr, address code,
                                           JavaThread* thread) {

  ShuffleDowncall call(recipe, args_arr, rets_arr, code, invoke_native_address());
  call.invoke(thread);
}
