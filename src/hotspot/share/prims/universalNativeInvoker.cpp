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
#include "prims/universalNativeInvoker.hpp"
#include "runtime/interfaceSupport.inline.hpp"
#include "runtime/jniHandles.inline.hpp"

BufferBlob* UniversalNativeInvoker::_invoke_native_blob = NULL;

class UniversalInvokeNativeGenerator : public StubCodeGenerator {
public:
  UniversalInvokeNativeGenerator(CodeBuffer* code) : StubCodeGenerator(code, PrintMethodHandleStubs) {}

  void generate();
};

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

// These are the native methods on jdk.internal.foreign.invokers.UniversalNativeInvoker.
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
              "register jdk.internal.foreign.invokers.UniversalNativeInvoker natives");
  }
}
JVM_END

