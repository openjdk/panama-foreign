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
#include "classfile/javaClasses.hpp"
#include "oops/oop.inline.hpp"
#include "jni.h"
#include "runtime/jniHandles.inline.hpp"
#include "utilities/exceptions.hpp"

/*
 * Implementation of class java.lang.Long2, java.lang.Long4, and java.lang.Long8.
 */

static instanceOop alloc_object(jclass clazz, TRAPS) {
  Klass* k = java_lang_Class::as_Klass(JNIHandles::resolve_non_null(clazz));
  if (k == NULL) {
    ResourceMark rm(THREAD);
    THROW_(vmSymbols::java_lang_InstantiationException(), NULL);
  }
  k->check_valid_for_instantiation(false, CHECK_NULL);
  InstanceKlass::cast(k)->initialize(CHECK_NULL);
  instanceOop ih = InstanceKlass::cast(k)->allocate_instance(THREAD);
  return ih;
}

JVM_ENTRY(jobject, Vector_Long2_make(JNIEnv *env, jclass cls,
                                     jlong lo, jlong hi))
{
  instanceOop x = alloc_object(cls, CHECK_NULL);

  static jlong base = java_lang_Long2::base_offset_in_bytes();
  // FIXME: assumes little endian layout
  x->long_field_put(base + 0, lo);
  x->long_field_put(base + 8, hi);

  return JNIHandles::make_local(env, x);
}
JVM_END

JVM_ENTRY(jobject, Vector_Long4_make(JNIEnv *env, jclass cls,
                                     jlong lo, jlong l2, jlong l3, jlong hi))
{
  instanceOop x = alloc_object(cls, CHECK_NULL);

  static jlong base = java_lang_Long4::base_offset_in_bytes();
  // FIXME: assumes little endian layout
  x->long_field_put(base +  0, lo);
  x->long_field_put(base +  8, l2);
  x->long_field_put(base + 16, l3);
  x->long_field_put(base + 24, hi);

  return JNIHandles::make_local(env, x);
}
JVM_END

JVM_ENTRY(jobject, Vector_Long8_make(JNIEnv *env, jclass cls,
                                     jlong lo, jlong l2, jlong l3, jlong l4,
                                     jlong l5, jlong l6, jlong l7, jlong hi))
{
  instanceOop x = alloc_object(cls, CHECK_NULL);

  static jlong base = java_lang_Long8::base_offset_in_bytes();
  // FIXME: assumes little endian layout
  x->long_field_put(base +  0, lo);
  x->long_field_put(base +  8, l2);
  x->long_field_put(base + 16, l3);
  x->long_field_put(base + 24, l4);
  x->long_field_put(base + 32, l5);
  x->long_field_put(base + 40, l6);
  x->long_field_put(base + 48, l7);
  x->long_field_put(base + 56, hi);

  return JNIHandles::make_local(env, x);
;
}
JVM_END

JVM_ENTRY(jlong, Vector_extract(JNIEnv *env, jobject v, jint idx))
{
  oop x = JNIHandles::resolve_non_null(v);

  int base = 0, max_idx = 0;
  Klass* k = x->klass();
  if (k == SystemDictionary::Long2_klass()) {
    base = java_lang_Long2::base_offset_in_bytes();
    max_idx = 2;
  } else if (k == SystemDictionary::Long4_klass()) {
    base = java_lang_Long4::base_offset_in_bytes();
    max_idx = 4;
  }  else if (k == SystemDictionary::Long8_klass()) {
    base = java_lang_Long8::base_offset_in_bytes();
    max_idx = 8;
  } else {
    THROW_MSG_(vmSymbols::java_lang_IllegalArgumentException(), "Unknown class", 0);
  }
  if (idx < 0 || idx >= max_idx) {
    THROW_MSG_(vmSymbols::java_lang_IllegalArgumentException(), "Invalid index", 0);
  }
  return x->long_field(base + 8 * idx);
}
JVM_END

/// JVM_RegisterLong2Methods

#define LANG "Ljava/lang/"

#define LNG2 LANG "Long2;"
#define LNG4 LANG "Long4;"
#define LNG8 LANG "Long8;"

#define CC (char*)  /*cast a literal from (const char*)*/
#define FN_PTR(f) CAST_FROM_FN_PTR(void*, &f)

static JNINativeMethod methodsL2[] = {
  {CC "make",    CC "(JJ)" LNG2,  FN_PTR(Vector_Long2_make)},
  {CC "extract", CC "(I)J",     FN_PTR(Vector_extract)},
};

static JNINativeMethod methodsL4[] = {
  {CC "make",    CC "(JJJJ)" LNG4, FN_PTR(Vector_Long4_make)},
  {CC "extract", CC "(I)J",      FN_PTR(Vector_extract)},
};

static JNINativeMethod methodsL8[] = {
  {CC "make",    CC "(JJJJJJJJ)" LNG8, FN_PTR(Vector_Long8_make)},
  {CC "extract", CC "(I)J",          FN_PTR(Vector_extract)},
};

#undef CC
#undef FN_PTR

#undef LANG
#undef LNG2
#undef LNG4
#undef LNG8

// This one function is exported, used by NativeLookup.

JVM_ENTRY(void, JVM_RegisterVectorMethods(JNIEnv *env, jclass cls)) {
  ThreadToNativeFromVM ttnfv(thread);
  int res = 0;

  jclass clsL2 = (jclass) JNIHandles::make_local(env, SystemDictionary::Long2_klass()->java_mirror());
  res = env->RegisterNatives(clsL2, methodsL2, sizeof(methodsL2)/sizeof(JNINativeMethod));
  guarantee(res == 0, "register Long2 natives");

  jclass clsL4 = (jclass) JNIHandles::make_local(SystemDictionary::Long4_klass()->java_mirror());
  res = env->RegisterNatives(clsL4, methodsL4, sizeof(methodsL4)/sizeof(JNINativeMethod));
  guarantee(res == 0, "register Long4 natives");

  jclass clsL8 = (jclass) JNIHandles::make_local(env, SystemDictionary::Long8_klass()->java_mirror());
  res = env->RegisterNatives(clsL8, methodsL8, sizeof(methodsL8)/sizeof(JNINativeMethod));
  guarantee(res == 0, "register Long8 natives");
}
JVM_END
