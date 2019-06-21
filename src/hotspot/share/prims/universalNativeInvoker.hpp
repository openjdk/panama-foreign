/*
 * Copyright (c) 2018, 2019, Oracle and/or its affiliates. All rights reserved.
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

#ifndef SHARE_VM_PRIMS_UNIVERSALNATIVEINVOKER_HPP
#define SHARE_VM_PRIMS_UNIVERSALNATIVEINVOKER_HPP

#include "classfile/javaClasses.hpp"
#include "classfile/vmSymbols.hpp"
#include "include/jvm.h"
#include "runtime/frame.inline.hpp"
#include "runtime/globals.hpp"
#include "utilities/macros.hpp"
#include CPU_HEADER(foreign_globals)

#ifdef ZERO
# include "entry_zero.hpp"
#endif


class MacroAssembler;
class Label;
class ShuffleRecipe;

class UniversalNativeInvoker: AllStatic {

private:
   // Native invoker code
  static BufferBlob* _invoke_native_blob;

  static void invoke_recipe(ShuffleRecipe& recipe, arrayHandle args_arr,
                            arrayHandle rets_arr, address code, JavaThread* thread);

public:
  static void invoke_native(arrayHandle recipe, arrayHandle args, arrayHandle rets, address code, JavaThread* thread);
  // Generate invoke native stub
  static void generate_invoke_native(MacroAssembler* masm);
  static address invoke_native_address() {
      return _invoke_native_blob->code_begin();
  }

  static void generate_adapter();
};

enum ShuffleRecipeOp {
  OP_STOP,
  OP_SKIP,
  OP_PULL,
  OP_PULL_LABEL,
  OP_CREATE_BUFFER,
  OP_NOP,
  OP_NOOF
};

enum ShuffleRecipeStorageClass {
  CLASS_BUF,
  CLASS_FIRST = CLASS_BUF,
  CLASS_STACK,
  CLASS_VECTOR,
  CLASS_INTEGER,
  CLASS_X87,
  CLASS_INDIRECT,
  CLASS_LAST = CLASS_INDIRECT,
  CLASS_NOOF
};

class ShuffleRecipe : public StackObj {
public:
  ShuffleRecipe(arrayHandle recipe)
    : _recipe(recipe) {
    assert(_recipe()->length() > 0, "Empty recipe not allowed");

    init_sizes();
  }

  arrayHandle recipe() { return _recipe; }
  uint64_t word(size_t index);

  size_t length() { return _length; }

  size_t stack_args_slots() { return _stack_args_slots; }
  size_t buffer_slots() { return _buffer_slots; }
  size_t nlabels() { return _nlabels; }

  static ShuffleRecipeStorageClass next_storage_class(ShuffleRecipeStorageClass c);
  static const char *storage_class_name(ShuffleRecipeStorageClass c);
  static size_t storage_class_max_width(ShuffleRecipeStorageClass c);

  NOT_PRODUCT(void print(outputStream* s);)

private:
  void init_sizes();

  NOT_PRODUCT(static const char* op2name(ShuffleRecipeOp op);)

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

  ShuffleRecipeOp next();

private:
  enum Direction { ARGUMENTS, RETURNS };

  Direction _direction;
  ShuffleRecipe& _recipe;

  size_t _next_word_index;
  uint64_t _cur_bits;

  ShuffleRecipeStorageClass _cur_class;
};

#endif // SHARE_VM_PRIMS_UNIVERSALNATIVEINVOKER_HPP
