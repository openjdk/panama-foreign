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
#include "code/codeBlob.hpp"
#include "prims/universalNativeInvoker.hpp"
#include "runtime/interfaceSupport.inline.hpp"
#include "runtime/jniHandles.inline.hpp"
#include "runtime/stubCodeGenerator.hpp"
#include "prims/methodHandles.hpp"

BufferBlob* UniversalNativeInvoker::_invoke_native_blob = NULL;

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

class UniversalInvokeNativeGenerator : public StubCodeGenerator {
public:
  UniversalInvokeNativeGenerator(CodeBuffer* code) : StubCodeGenerator(code, PrintMethodHandleStubs) {}

  void generate();
};

static ShuffleRecipeStorageClass index2storage_class[CLASS_NOOF + 1] = {
  CLASS_BUF, CLASS_STACK, CLASS_VECTOR, CLASS_INTEGER, CLASS_X87, CLASS_INDIRECT, CLASS_NOOF
};

static const char* index2storage_class_name[CLASS_NOOF] = {
  "CLASS_BUF", "CLASS_STACK", "CLASS_VECTOR", "CLASS_INTEGER", "CLASS_X87", "CLASS_INDIRECT"
};

ShuffleRecipeStorageClass ShuffleRecipe::next_storage_class(ShuffleRecipeStorageClass c) {
  int idx = (int)c + 1;
  assert(idx < CLASS_NOOF + 1, "Out of bounds");
  return index2storage_class[idx];
}

const char *ShuffleRecipe::storage_class_name(ShuffleRecipeStorageClass c) {
  int idx = (int)c;
  assert(idx < CLASS_NOOF, "Out of bounds");
  return index2storage_class_name[idx];
}

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

uint64_t ShuffleRecipe::word(size_t index) {
  uint64_t* bits = (uint64_t*)_recipe()->base(T_LONG);
  return bits[index];
}

#ifndef PRODUCT
const char* ShuffleRecipe::op2name(ShuffleRecipeOp op) {
  static const char* op_name[OP_NOOF] = {
    "OP_STOP", "OP_SKIP", "OP_PULL", "OP_PULL_LABEL", "OP_CREATE_BUFFER", "OP_NOP"
  };

  assert(op <= OP_NOOF, "invalid op");
  return op_name[op];
}
#endif

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

ShuffleRecipeOp ShuffleRecipeStream::next() {
  assert(has_more(), "stream empty");

  if (_cur_bits == 1) {
    read_recipe_word();
  }

  ShuffleRecipeOp op = (ShuffleRecipeOp)(_cur_bits & 7);
  _cur_bits >>= 3;

  if (op == OP_STOP) {
    _cur_class = ShuffleRecipe::next_storage_class(_cur_class);
  }

  return op;
}

void UniversalInvokeNativeGenerator::generate() {
  UniversalNativeInvoker::generate_invoke_native(_masm);
}

void UniversalNativeInvoker::generate_adapter() {
    ResourceMark rm;
    _invoke_native_blob = BufferBlob::create("invoke_native_blob", MethodHandles::adapter_code_size);

    CodeBuffer code2(_invoke_native_blob);
    UniversalInvokeNativeGenerator g2(&code2);
    g2.generate();
    code2.log_section_sizes("InvokeNativeBlob");
}

void UniversalNativeInvoker::invoke_native(arrayHandle recipe_arr, arrayHandle args_arr,
                                           arrayHandle rets_arr, address code,
                                           JavaThread* thread) {
  ShuffleRecipe recipe(recipe_arr);

#ifndef PRODUCT
  ShuffleRecipeVerifier verifier(recipe, args_arr()->length(), rets_arr()->length());
  verifier.verify();
#endif

  invoke_recipe(recipe, args_arr, rets_arr, code, thread);
}

JVM_ENTRY(void, UNI_invokeNative(JNIEnv* env, jobject _unused, jlongArray args_jh, jlongArray rets_jh, jlongArray recipe_jh, jlong nep_jh)) {
  arrayHandle recipe(THREAD, (arrayOop)JNIHandles::resolve(recipe_jh));
  arrayHandle args(THREAD, (arrayOop)JNIHandles::resolve(args_jh));
  arrayHandle rets(THREAD, (arrayOop)JNIHandles::resolve(rets_jh));

  assert(thread->thread_state() == _thread_in_vm, "thread state is: %d", thread->thread_state());
  address c = (address)nep_jh;

  UniversalNativeInvoker::invoke_native(recipe, args, rets, c, thread);
}
JVM_END

#define CC (char*)  /*cast a literal from (const char*)*/
#define FN_PTR(f) CAST_FROM_FN_PTR(void*, &f)
#define LANG "Ljava/lang/"

// These are the native methods on jdk.internal.foreign.abi.UniversalNativeInvoker.
static JNINativeMethod UNI_methods[] = {
  {CC "invokeNative",       CC "([J[J[JJ)V",           FN_PTR(UNI_invokeNative)}
};

/**
 * This one function is exported, used by NativeLookup.
 */
JVM_ENTRY(void, JVM_RegisterUniversalNativeInvokerMethods(JNIEnv *env, jclass UNI_class)) {
  {
    ThreadToNativeFromVM ttnfv(thread);

    int status = env->RegisterNatives(UNI_class, UNI_methods, sizeof(UNI_methods)/sizeof(JNINativeMethod));
    guarantee(status == JNI_OK && !env->ExceptionOccurred(),
              "register jdk.internal.foreign.invoke.abi.UniversalNativeInvoker natives");
  }
}
JVM_END
