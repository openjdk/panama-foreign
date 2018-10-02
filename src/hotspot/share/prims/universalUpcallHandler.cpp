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

#include "prims/universalUpcallHandler.hpp"
#include "runtime/jniHandles.inline.hpp"
#include "runtime/interfaceSupport.inline.hpp"

JVM_ENTRY(static jlong, UUH_AllocateUpcallStub(JNIEnv *env, jobject _unused, jobject rec))
  Handle receiver(THREAD, JNIHandles::resolve(rec));
  return (jlong)UniversalUpcallHandler::generate_upcall_stub(receiver);
JVM_END

#define CC (char*)  /*cast a literal from (const char*)*/
#define FN_PTR(f) CAST_FROM_FN_PTR(void*, &f)
#define LANG "Ljava/lang/"
#define UPCALL "Ljdk/internal/foreign/invokers/UpcallHandler;"

// These are the native methods on jdk.internal.foreign.invokers.UniversalUpcallHandler.
static JNINativeMethod UUH_methods[] = {
  {CC "allocateUpcallStub", CC "(" UPCALL ")J",                 FN_PTR(UUH_AllocateUpcallStub)},
};

/**
 * This one function is exported, used by NativeLookup.
 */
JVM_ENTRY(void, JVM_RegisterUniversalUpcallHandlerMethods(JNIEnv *env, jclass UUH_class)) {
  {
    ThreadToNativeFromVM ttnfv(thread);

    int status = env->RegisterNatives(UUH_class, UUH_methods, sizeof(UUH_methods)/sizeof(JNINativeMethod));
    guarantee(status == JNI_OK && !env->ExceptionOccurred(),
              "register jdk.internal.foreign.invoker.UniversalUpcallHandler natives");
  }
}
JVM_END
