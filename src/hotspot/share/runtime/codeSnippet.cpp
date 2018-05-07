/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
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
 *
 */

#include "precompiled.hpp"
#include "asm/macroAssembler.hpp"
#include "classfile/javaClasses.hpp"
#include "code/codeCache.hpp"
#include "compiler/disassembler.hpp"
#include "memory/resourceArea.hpp"
#include "jni.h"
#include "runtime/interfaceSupport.inline.hpp"
#include "runtime/jniHandles.inline.hpp"
#include "runtime/sharedRuntime.hpp"
#include "utilities/decoder.hpp"

#ifdef COMPILER2
#include "opto/machnode.hpp"
#include "opto/regalloc.hpp"
#include "runtime/codeSnippet.hpp"
#include "oops/objArrayOop.inline.hpp"
#include "oops/typeArrayOop.inline.hpp"
#endif // COMPILER2

// TODO: class CodeSnippetBlob: public BufferBlob { ... };

JVM_ENTRY(jlong, MCS_install(JNIEnv* env, jobject igcls, jstring name, jobject code_jh, jobject method_type_jh)) {
  //ThreadToNativeFromVM ttnfv(thread);
  ResourceMark rm;

  typeArrayHandle code(THREAD, (typeArrayOop)JNIHandles::resolve_non_null(code_jh));
  Handle mt(THREAD, JNIHandles::resolve_non_null(method_type_jh));

  char* utfName = NULL;
  if (name != NULL) {
    ThreadToNativeFromVM ttnfv(thread);
    uint len = env->GetStringUTFLength(name);
    int unicode_len = env->GetStringLength(name);
    utfName = NEW_C_HEAP_ARRAY(char, len + 1, mtInternal);
    guarantee(utfName != NULL, "");
    env->GetStringUTFRegion(name, 0, unicode_len, utfName);
  } else {
    utfName = (char*)"UNKNOWN";
  }
  int code_size = code->length();

  stringStream ss;
  ss.print("code_snippet: %s", utfName);

  CodeBuffer buffer(ss.as_string(), code_size + 1000, 100); // FIXME: adaptive sizing?
  MacroAssembler _masm(&buffer);

  SharedRuntime::generate_snippet(&_masm, mt(), code(), utfName);

  const char* snippet_name = _masm.code_string(ss.as_string());
  BufferBlob* buf = BufferBlob::create(snippet_name, &buffer);
  jlong address = -1;
  if (buf != NULL) {
    if (PrintCodeSnippets) {
      ttyLocker ttyl;
      tty->print_cr("Decoding code snippet \"%s\" @ " INTPTR_FORMAT, utfName, p2i(buf->code_begin()));
      Disassembler::decode(buf->code_begin(), buf->code_end());
      tty->cr();
    }
    address = (jlong)buf->code_begin();
  }
  if (name != NULL && utfName != NULL) {
    FREE_C_HEAP_ARRAY(char, utfName);
  }
  return address;
}
JVM_END

// Remove unused snippet stub.
JVM_ENTRY(void, MCS_freeStub(JNIEnv* env, jobject igcls, jlong p)) {
  address addr = (address)p;
  CodeBlob* cb = CodeCache::find_blob(addr);
  assert(cb != NULL, "");
  assert(cb->code_begin() == addr, "should match");
  cb->flush();
  {
    MutexLockerEx mu(CodeCache_lock, Mutex::_no_safepoint_check_flag);
    CodeCache::free((CodeBlob*)cb);
  }
  return;
}
JVM_END

#ifdef COMPILER2
CodeSnippetRequest* CodeSnippetSlot = NULL;

static Node* proj_out(Node* n, uint which_proj) {
  for( DUIterator_Fast imax, i = n->fast_outs(imax); i < imax; i++ ) {
    Node *p = n->fast_out(i);
    if (p->is_Proj()) {
      ProjNode *proj = p->as_Proj();
      if (proj->_con == which_proj) {
        return proj;
      }
    }
  }
  return NULL;
}

void CodeSnippetRequest::extract_regs(PhaseRegAlloc* ra) {
  const TypeTuple* dom = _n->tf()->domain();
  uint arg_max = dom->cnt();
  uint halves = 0;
  for (uint i = TypeFunc::Parms; i < arg_max; i++) {
    if (dom->field_at(i)->base() == Type::Half) {
      halves++;
    }
  }
  uint arg_num = arg_max - TypeFunc::Parms - halves;

  _size = arg_num + 1/*ret value*/;

  // Freed in ~CodeSnippetRequest()
  _regs = NEW_C_HEAP_ARRAY(OptoReg::Name, _size, mtCompiler); // FIXME

  uint arg_idx = 1; // return value @ [0]
  for (uint i = TypeFunc::Parms; i < arg_max; i++) {
    if (dom->field_at(i)->base() == Type::Half)  continue; // skip halves
    OptoReg::Name first_reg = ra->get_reg_first(_n->in(i));
    _regs[arg_idx] = first_reg;
    arg_idx++;
  }
  // return value
  Node* proj_res = proj_out(_n, TypeFunc::Parms);
  if (proj_res != NULL) {
    assert(_n->tf()->return_type() != T_VOID, "sanity");
    OptoReg::Name first_reg = ra->get_reg_first(proj_res);
    _regs[0] = first_reg;
  } else {
    assert(_n->tf()->return_type() == T_VOID, "sanity");
    _regs[0] = OptoReg::Bad;
  }
}
#endif // COMPILER2

JVM_ENTRY(jobject, MCS_processTask(JNIEnv* env, jobject igcls, jobject arr, jobject arr1)) {
#ifdef COMPILER2
  //ThreadToNativeFromVM ttnfv(thread);

  MonitorLockerEx mu(CodeSnippet_lock);
  if (CodeSnippetSlot == NULL)  return NULL;
  MachCallSnippetNode* node = (MachCallSnippetNode*)CodeSnippetSlot->_n;
//  tty->print_cr("MCS_processTask: " INTPTR_FORMAT, p2i(node));
  jobject generator = JNIHandles::make_local(env, node->_generator->get_oop());

  // Copy info
  if (arr1 != NULL) {
    oop mt_arr = JNIHandles::resolve(arr1);
    if (mt_arr->is_objArray()) {
      ((objArrayOop)mt_arr)->obj_at_put(0, NULL); // FIXME
      ((objArrayOop)mt_arr)->obj_at_put(1, node->_mt->get_oop());
    }
  }

  // Copy register masks
  if (arr != NULL) {
    typeArrayOop arr_oop = (typeArrayOop)JNIHandles::resolve(arr);
    assert(arr_oop->is_typeArray(), "");
    assert(CodeSnippetSlot->_size <= (uint)arr_oop->length(),
           "%d %d", CodeSnippetSlot->_size, arr_oop->length());
    for (uint i = 0; i < CodeSnippetSlot->_size; i++) {
      OptoReg::Name reg = CodeSnippetSlot->_regs[i];
      arr_oop->int_at_put(i, reg);
    }
  }
  return generator;
#else
  return NULL; // Not supported
#endif // COMPILER2
}
JVM_END

JVM_ENTRY(jboolean, MCS_finishTask(JNIEnv* env, jobject igcls, jobject arr)) {
#ifdef COMPILER2
//  ThreadToNativeFromVM ttnfv(thread);

  MonitorLockerEx mu(CodeSnippet_lock);
  guarantee(CodeSnippetSlot != NULL, "");
  MachCallSnippetNode* node = (MachCallSnippetNode*)CodeSnippetSlot->_n;
  if (arr != NULL) {
    typeArrayOop x = (typeArrayOop)JNIHandles::resolve(arr);
    assert(x->is_typeArray(), "");
    int len = x->length();
    node->_size = len;
    if (len > 0) {
      // Freed in MachCallSnippetNode::specialize(PhaseRegAlloc*)
      uint8_t* code = NEW_C_HEAP_ARRAY(uint8_t, len, mtCompiler); // FIXME
      for (int i = 0; i < len; i++) {
        code[i] = x->byte_at(i);
      }
      node->_code = code;
    }
  } else {
//  tty->print_cr("MCS_finishTask: failed task" INTPTR_FORMAT, p2i(node));
  }

//  tty->print_cr("MCS_finishTask: " INTPTR_FORMAT, p2i(node));
  CodeSnippetSlot = NULL;
  mu.notify_all();
  return JNI_TRUE;
#else
  return JNI_FALSE; // Not supported
#endif // COMPILER2
}
JVM_END

JVM_ENTRY(jint, MCS_reg2opto(JNIEnv* env, jobject igcls, jobject str)) {
#ifdef COMPILER2
//    ThreadToNativeFromVM ttnfv(thread);
  if (str != NULL) {
    ResourceMark rm;
    oop str_oop = (typeArrayOop)JNIHandles::resolve(str);
    char* name = java_lang_String::as_utf8_string(str_oop);
    for (OptoReg::Name i = OptoReg::Name(0); i < OptoReg::Name(REG_COUNT); i = OptoReg::add(i,1) ) {
      const char* regname = Matcher::regName[i];
      if (!strcmp(name, regname)) {
//        tty->print_cr("reg2opto: found: %s == %s (%d)", name, regname, i);
        return i;
      }
    }
  }
  return -1; // Not found.
#else
  return -1; // Not supported.
#endif // COMPILER2
}
JVM_END

JVM_ENTRY(void, MCS_printRegisters(JNIEnv* env, jobject igcls)) {
  ttyLocker ttyl;
  tty->print("Registers: [");
#ifdef COMPILER2
  for (OptoReg::Name i = OptoReg::Name(0); i < OptoReg::Name(REG_COUNT); i = OptoReg::add(i,1) ) {
    const char* regname = OptoReg::regname(i);
    tty->print(" %d:%s:%s", i, Matcher::regName[i], regname);
  }
#endif // COMPILER2
  tty->print_cr(" ]");
}
JVM_END

#define OBJ  "Ljava/lang/Object;"
#define OBJ_ARR "["OBJ
#define STRG "Ljava/lang/String;"
#define MT   "Ljava/lang/invoke/MethodType;"

#define CC (char*)  /*cast a literal from (const char*)*/
#define FN_PTR(f) CAST_FROM_FN_PTR(void*, &f)

// These are the native methods on jdk.vm.ci.panama.MachineCodeSnippet.
static JNINativeMethod MCS_methods[] = {
  {CC "install",  CC "(" STRG "[B" MT ")J",        FN_PTR(MCS_install)},
  {CC "freeStub", CC "(J)V",                       FN_PTR(MCS_freeStub)},
  {CC "processTask",  CC "([I" OBJ_ARR ")" OBJ,    FN_PTR(MCS_processTask)},
  {CC "finishTask",   CC "([B)Z",                  FN_PTR(MCS_finishTask)},
  {CC "reg2opto",     CC "(" STRG ")I",            FN_PTR(MCS_reg2opto)},
  {CC "printRegisters", CC "()V",                  FN_PTR(MCS_printRegisters)},
};

/**
 * This one function is exported, used by NativeLookup.
 */
JVM_ENTRY(void, JVM_RegisterMachineCodeSnippetMethods(JNIEnv *env, jclass MCS_class)) {
  ThreadToNativeFromVM ttnfv(thread);
  env->RegisterNatives(MCS_class, MCS_methods, sizeof(MCS_methods)/sizeof(JNINativeMethod));
}
JVM_END
