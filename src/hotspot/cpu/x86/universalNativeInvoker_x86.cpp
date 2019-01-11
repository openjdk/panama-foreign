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
#include "interpreter/interpreter.hpp"
#include "interpreter/interpreterRuntime.hpp"
#include "memory/allocation.inline.hpp"
#include "memory/resourceArea.hpp"
#include "include/jvm.h"
#include "prims/universalNativeInvoker.hpp"
#include "runtime/javaCalls.hpp"
#include "runtime/interfaceSupport.inline.hpp"
#include "logging/log.hpp"
#include "logging/logStream.hpp"
#include "oops/arrayOop.inline.hpp"
#include "runtime/jniHandles.inline.hpp"

//// shuffle recipe based (mikael style) stub ////

//#define DEBUG_OPS
//#define DEBUG_CALLS

typedef void (*InvokeNativeStub)(struct ShuffleDowncallContext* ctxt);

enum ShuffleRecipeStorageClass {
  CLASS_BUF,
  CLASS_FIRST = CLASS_BUF,
  CLASS_STACK,
  CLASS_VECTOR,
  CLASS_INTEGER,
  CLASS_X87,
  CLASS_LAST = CLASS_X87,
  CLASS_NOOF
};

static ShuffleRecipeStorageClass index2storage_class[CLASS_NOOF + 1] = {
  CLASS_BUF, CLASS_STACK, CLASS_VECTOR, CLASS_INTEGER, CLASS_X87, CLASS_NOOF
};

static const char* index2storage_class_name[CLASS_NOOF] = {
  "CLASS_BUF", "CLASS_STACK", "CLASS_VECTOR", "CLASS_INTEGER", "CLASS_X87"
};

static ShuffleRecipeStorageClass next_storage_class(ShuffleRecipeStorageClass c) {
  int idx = (int)c + 1;
  assert(idx < CLASS_NOOF + 1, "Out of bounds");
  return index2storage_class[idx];
}


static size_t class2maxwidth(ShuffleRecipeStorageClass c) {
  switch (c) {
  case CLASS_BUF:
  case CLASS_STACK:
  case CLASS_INTEGER:
    return 1;

  case CLASS_X87:
    return 2;

  case CLASS_VECTOR:
    if (UseAVX >= 3) {
      return 8;
    } else if (UseAVX >= 1) {
      return 4;
    } else {
      return 2;
    }

  default:
    assert(false, "Unexpected class");
    return 1;
  }
}

enum ShuffleRecipeOp {
  OP_STOP,
  OP_SKIP,
  OP_PULL,
  OP_PULL_LABEL,
  OP_CREATE_BUFFER,
  OP_NOP,
  OP_NOOF
};

static const char* op_name[OP_NOOF] = {
  "OP_STOP", "OP_SKIP", "OP_PULL", "OP_PULL_LABEL", "OP_CREATE_BUFFER", "OP_NOP"
};

#ifndef PRODUCT
static const char* op2name(ShuffleRecipeOp op) {
  assert(op <= OP_NOOF, "invalid op");
  return op_name[op];
}
#endif

class ShuffleRecipe : public StackObj {
public:
  ShuffleRecipe(arrayHandle recipe)
    : _recipe(recipe) {
    assert(_recipe()->length() > 0, "Empty recipe not allowed");

    init_sizes();
  }

  arrayHandle recipe() { return _recipe; }
  uint64_t word(size_t index) {
    uint64_t* bits = (uint64_t*)_recipe()->base(T_LONG);
    return bits[index];
  }

  size_t length() { return _length; }

  size_t stack_args_slots() { return _stack_args_slots; }
  size_t buffer_slots() { return _buffer_slots; }
  size_t nlabels() { return _nlabels; }

#ifndef PRODUCT
  void print(outputStream* s);
#endif

private:
  void init_sizes();

  arrayHandle _recipe; // the long[] recipe array
  size_t _length;

  size_t _buffer_slots;
  size_t _stack_args_slots;
  size_t _nlabels;
};

class ShuffleRecipeStream {
public:
  ShuffleRecipeStream(ShuffleRecipe& recipe)
    : _recipe(recipe) {
    _next_word_index = 0;
    _cur_class = CLASS_FIRST;
    _direction = ARGUMENTS;

    read_recipe_word();
  }

  void read_recipe_word() {
    _cur_bits = _recipe.word(_next_word_index);
    _next_word_index++;
  }

  bool has_more() {
    return _cur_class < CLASS_NOOF;
  }

  void init_for_returns() {
    assert(_direction == ARGUMENTS, "stream already advanced");
    _cur_class = CLASS_FIRST;
    _direction = RETURNS;
  }

  ShuffleRecipeOp next() {
    assert(has_more(), "stream empty");

    if (_cur_bits == 1) {
      read_recipe_word();
    }

    ShuffleRecipeOp op = (ShuffleRecipeOp)(_cur_bits & 7);
    _cur_bits >>= 3;

    if (op == OP_STOP) {
      _cur_class = next_storage_class(_cur_class);
    }

    return op;
  }

private:
  enum Direction { ARGUMENTS, RETURNS };

  Direction _direction;
  ShuffleRecipe& _recipe;

  size_t _next_word_index;
  uint64_t _cur_bits;

  ShuffleRecipeStorageClass _cur_class;
};

void ShuffleRecipe::init_sizes() {
  _length = _recipe()->length();

  size_t slots_for_class[CLASS_NOOF] = { 0 };
  size_t nbuffers = 0;

  int cur_class = CLASS_FIRST;

  ShuffleRecipeStream stream(*this);

  while(stream.has_more() && cur_class <= CLASS_STACK) {
    switch (stream.next()) {
    case OP_NOP:
      break;

    case OP_STOP:
      cur_class++;
      break;

    case OP_CREATE_BUFFER:
      nbuffers++;
      break;

    case OP_SKIP:
    case OP_PULL:
    case OP_PULL_LABEL:
      slots_for_class[cur_class]++;
      break;

    default:
      assert(false, "Unexpected op");
      break;
    }
  }

  _stack_args_slots = slots_for_class[CLASS_STACK];
  _buffer_slots = slots_for_class[CLASS_BUF];
  _nlabels = nbuffers;
}

#ifndef PRODUCT
void ShuffleRecipe::print(outputStream* s) {
  ShuffleRecipeStream stream(*this);

  s->print_cr("Arguments:");
  while (stream.has_more()) {
    ShuffleRecipeOp op = stream.next();

    s->print_cr("OP: %s", op2name(op));
  }

  s->print_cr("Returns:");
  while (stream.has_more()) {
    ShuffleRecipeOp op = stream.next();

    s->print_cr("OP: %s", op2name(op));
  }
}
#endif


struct ShuffleDowncallContext {
  struct {
#ifdef _LP64
    uint64_t integer[INTEGER_ARGUMENT_REGISTERS_NOOF];
    VectorRegister vector[VECTOR_ARGUMENT_REGISTERS_NOOF];
    uintptr_t rax;
#endif
    uint64_t* stack_args;
    size_t stack_args_bytes;
    address next_pc;
  } arguments;

  struct {
    uint64_t integer[INTEGER_RETURN_REGISTERS_NOOF];
    VectorRegister vector[VECTOR_RETURN_REGISTERS_NOOF];
    long double x87[X87_RETURN_REGISTERS_NOOF];
  } returns;
};

static void dump_vector_register(outputStream* out, XMMRegister reg, VectorRegister value) {
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
    dump_integer_register(&ls, rax, ctxt->arguments.rax);

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
          _npulls, index2storage_class_name[_cur_class], _index_in_class, values[i]);
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

    case CLASS_X87: {
      assert(_index_in_class < X87_RETURN_REGISTERS_NOOF, "out of bounds");
      memcpy(*dst_addrp, &_context.returns.x87[_index_in_class], _npulls * sizeof(uint64_t));
      break;
    }

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

#ifdef _LP64
        if (_cur_class == CLASS_VECTOR) {
          _context.arguments.rax = _index_in_class;
        }
#endif

        _index_in_class = 0;
        _cur_class = next_storage_class(_cur_class);
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
        if (_npulls == class2maxwidth(_cur_class)) {
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
        _cur_class = next_storage_class(_cur_class);
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
        if (_npulls == class2maxwidth(_cur_class)) {
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

#ifndef PRODUCT
class ShuffleRecipeVerifier : public StackObj {
public:
  ShuffleRecipeVerifier(ShuffleRecipe& recipe, size_t args_length, size_t rets_length)
    : _recipe(recipe), _args_length(args_length), _rets_length(rets_length), _stream(_recipe) {
  }

  void verify() {
    for (size_t i = 0; i < _recipe.length(); i++) {
      assert((_recipe.word(i) >> 63) == 1, "MSB in recipe word must be set");
    }

    do_verify(ARGUMENTS);

    _stream.init_for_returns();
    do_verify(RETURNS);
  }

private:
  enum Direction {
    ARGUMENTS,
    RETURNS
  };

  void do_verify(Direction direction) {
    struct op_stats {
      size_t op_count_per_class[CLASS_NOOF][OP_NOOF];
      size_t op_count[OP_NOOF];
    };

    struct op_stats stats;

    memset(&stats, 0, sizeof(stats));

    int cur_class = CLASS_FIRST;
    bool done = false;

    while (_stream.has_more()) {
      assert(!done, "Stream unexpectedly returned additional tokens");

      ShuffleRecipeOp op = _stream.next();

      stats.op_count_per_class[cur_class][op]++;
      stats.op_count[op]++;

      switch (op) {
      case OP_NOP:
      case OP_SKIP:
      case OP_PULL:
        break;

      case OP_STOP:
        cur_class++;
        if (cur_class == CLASS_NOOF) {
          done = true;
        }
        break;

      case OP_CREATE_BUFFER:
        assert(cur_class == CLASS_BUF, "Buffers may only be created in buffer class");
        assert(direction == ARGUMENTS, "Buffers can only be created when processing arguments");
        break;

      case OP_PULL_LABEL:
        assert(cur_class != CLASS_BUF, "Must not pull pull labels in buffer class");
        assert(direction == ARGUMENTS, "Buffer labels can only be pulled when processing arguments");
        break;

      default:
        assert(false, "Unexpected op");
        break;
      }
    }

    assert(done, "Not enough STOP operations");

    assert(stats.op_count[OP_CREATE_BUFFER] == stats.op_count[OP_PULL_LABEL], "All labels must be pulled");
    assert(direction == RETURNS || stats.op_count[OP_PULL] == _args_length, "All argument values must be pulled");
    assert(direction == ARGUMENTS || stats.op_count[OP_PULL] == _rets_length, "All return values must be pulled");
  }

private:
  ShuffleRecipe& _recipe;
  size_t _args_length;
  size_t _rets_length;
  ShuffleRecipeStream _stream;
};
#endif

void UniversalNativeInvoker::generate_invoke_native(MacroAssembler* _masm) {


#if 0
  fprintf(stderr, "generate_invoke_native()\n");
#endif

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

  // Put the context pointer in ebx/rbx - it's going to be heavily used below both before and after the call
  Register ctxt_reg = rbx;

#ifdef _LP64
  __ block_comment("init_and_alloc_stack");

  __ push(ctxt_reg); // need to preserve register
#ifdef _WIN64 // preserve extra registers on MSx64
  __ push(rsi);
  __ push(rdi);
#endif

  __ movptr(ctxt_reg, c_rarg0);

  __ block_comment("allocate_stack");
  __ movptr(rcx, Address(ctxt_reg, offsetof(struct ShuffleDowncallContext, arguments.stack_args_bytes)));
  __ subptr(rsp, rcx);
  __ andptr(rsp, -64);

  // Note: rcx is used below!


  __ block_comment("load_arguments");

  __ shrptr(rcx, LogBytesPerWord); // bytes -> words
  __ movptr(rsi, Address(ctxt_reg, offsetof(struct ShuffleDowncallContext, arguments.stack_args)));
  __ movptr(rdi, rsp);
  __ rep_mov();


  for (size_t i = 0; i < VECTOR_ARGUMENT_REGISTERS_NOOF; i++) {
    // [1] -> 64 bit -> xmm
    // [2] -> 128 bit -> xmm
    // [4] -> 256 bit -> ymm
    // [8] -> 512 bit -> zmm

    XMMRegister reg = vector_argument_registers[i];
    size_t offs = offsetof(struct ShuffleDowncallContext, arguments.vector) + i * sizeof(VectorRegister);
    if (UseAVX >= 3) {
      __ evmovdqul(reg, Address(ctxt_reg, (int)offs), Assembler::AVX_512bit);
    } else if (UseAVX >= 1) {
      __ vmovdqu(reg, Address(ctxt_reg, (int)offs));
    } else {
      __ movdqu(reg, Address(ctxt_reg, (int)offs));
    }
  }

  for (size_t i = 0; i < INTEGER_ARGUMENT_REGISTERS_NOOF; i++) {
    size_t offs = offsetof(struct ShuffleDowncallContext, arguments.integer) + i * sizeof(uintptr_t);
    __ movptr(integer_argument_registers[i], Address(ctxt_reg, (int)offs));
  }

  __ movptr(rax, Address(ctxt_reg, offsetof(struct ShuffleDowncallContext, arguments.rax)));

#ifdef _WIN64
  __ block_comment("allocate shadow space for argument register spill");
  __ subptr(rsp, 32);
#endif

  // call target function
  __ block_comment("call target function");
  __ call(Address(ctxt_reg, offsetof(struct ShuffleDowncallContext, arguments.next_pc)));

#ifdef _WIN64
  __ block_comment("pop shadow space");
  __ addptr(rsp, 32);
#endif

  __ block_comment("store_registers");
  for (size_t i = 0; i < INTEGER_RETURN_REGISTERS_NOOF; i++) {
    ssize_t offs = offsetof(struct ShuffleDowncallContext, returns.integer) + i * sizeof(uintptr_t);
    __ movptr(Address(ctxt_reg, offs), integer_return_registers[i]);
  }

  for (size_t i = 0; i < VECTOR_RETURN_REGISTERS_NOOF; i++) {
    // [1] -> 64 bit -> xmm
    // [2] -> 128 bit -> xmm (SSE)
    // [4] -> 256 bit -> ymm (AVX)
    // [8] -> 512 bit -> zmm (AVX-512, aka AVX3)

    XMMRegister reg = vector_return_registers[i];
    size_t offs = offsetof(struct ShuffleDowncallContext, returns.vector) + i * sizeof(VectorRegister);
    if (UseAVX >= 3) {
      __ evmovdqul(Address(ctxt_reg, (int)offs), reg, Assembler::AVX_512bit);
    } else if (UseAVX >= 1) {
      __ vmovdqu(Address(ctxt_reg, (int)offs), reg);
    } else {
      __ movdqu(Address(ctxt_reg, (int)offs), reg);
    }
  }

  for (size_t i = 0; i < X87_RETURN_REGISTERS_NOOF; i++) {
    size_t offs = offsetof(struct ShuffleDowncallContext, returns.x87) + i * (sizeof(long double));
    __ fstp_x(Address(ctxt_reg, (int)offs)); //pop ST(0)
  }
#else
  __ hlt();
#endif

  // Restore backed up preserved register
  __ movptr(ctxt_reg, Address(rbp, -(int)sizeof(uintptr_t)));
#ifdef _WIN64
  __ movptr(rsi, Address(rbp, -(int)(sizeof(uintptr_t) * 2)));
  __ movptr(rdi, Address(rbp, -(int)(sizeof(uintptr_t) * 3)));
#endif

  __ leave();
  __ ret(0);

  __ flush();
}

void UniversalNativeInvoker::invoke_native(arrayHandle recipe_arr, arrayHandle args_arr, arrayHandle rets_arr, address code, JavaThread* thread) {
  ShuffleRecipe recipe(recipe_arr);

#ifndef PRODUCT
  ShuffleRecipeVerifier verifier(recipe, args_arr()->length(), rets_arr()->length());
  verifier.verify();
#endif

  ShuffleDowncall call(recipe, args_arr, rets_arr, code, invoke_native_address());
  call.invoke(thread);
}
