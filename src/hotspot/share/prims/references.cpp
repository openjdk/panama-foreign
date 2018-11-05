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
#include "runtime/interfaceSupport.inline.hpp"
#include "runtime/jniHandles.inline.hpp"

double RF_longDoubleToDouble(JNIEnv *env, jobject _unused, jlong addr) {
     return (double)*((long double*)addr);
}
void RF_doubleToLongDouble(JNIEnv *env, jobject _unused, jlong addr, jdouble val) {
     *((long double*)addr) = (long double)val;
}

#define CC (char*)  /*cast a literal from (const char*)*/
#define FN_PTR(f) CAST_FROM_FN_PTR(void*, &f)

// These are the native methods on jdk.internal.foreign.invokers.DirectNativeInvoker.
static JNINativeMethod RF_methods[] = {
  {CC "longDoubleToDouble", CC "(J)D", FN_PTR(RF_longDoubleToDouble)},
  {CC "doubleToLongDouble", CC "(JD)V", FN_PTR(RF_doubleToLongDouble)}
};

/**
 * This one function is exported, used by NativeLookup.
 */
JVM_ENTRY(void, JVM_RegisterReferencesMethods(JNIEnv *env, jclass RF_class)) {
  {
    ThreadToNativeFromVM ttnfv(thread);

    int status = env->RegisterNatives(RF_class, RF_methods, sizeof(RF_methods)/sizeof(JNINativeMethod));
    guarantee(status == JNI_OK && !env->ExceptionOccurred(),
              "register jdk.internal.foreign.memory.References natives");
  }
}
JVM_END
