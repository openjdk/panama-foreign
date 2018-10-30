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

void DNI_invokeNative_V_V(JNIEnv *env, jobject _unused, jlong addr) {
     ((void (*)())addr)();
}
void DNI_invokeNative_V_D(JNIEnv *env, jobject _unused, jlong addr, jdouble arg0) {
     ((void (*)(jdouble))addr)(arg0);
}
void DNI_invokeNative_V_J(JNIEnv *env, jobject _unused, jlong addr, jlong arg0) {
     ((void (*)(jlong))addr)(arg0);
}
void DNI_invokeNative_V_DD(JNIEnv *env, jobject _unused, jlong addr, jdouble arg0, jdouble arg1) {
     ((void (*)(jdouble, jdouble))addr)(arg0, arg1);
}
void DNI_invokeNative_V_JD(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jdouble arg1) {
     ((void (*)(jlong, jdouble))addr)(arg0, arg1);
}
void DNI_invokeNative_V_JJ(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jlong arg1) {
     ((void (*)(jlong, jlong))addr)(arg0, arg1);
}
void DNI_invokeNative_V_DDD(JNIEnv *env, jobject _unused, jlong addr, jdouble arg0, jdouble arg1, jdouble arg2) {
     ((void (*)(jdouble, jdouble, jdouble))addr)(arg0, arg1, arg2);
}
void DNI_invokeNative_V_JDD(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jdouble arg1, jdouble arg2) {
     ((void (*)(jlong, jdouble, jdouble))addr)(arg0, arg1, arg2);
}
void DNI_invokeNative_V_JJD(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jlong arg1, jdouble arg2) {
     ((void (*)(jlong, jlong, jdouble))addr)(arg0, arg1, arg2);
}
void DNI_invokeNative_V_JJJ(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jlong arg1, jlong arg2) {
     ((void (*)(jlong, jlong, jlong))addr)(arg0, arg1, arg2);
}
void DNI_invokeNative_V_DDDD(JNIEnv *env, jobject _unused, jlong addr, jdouble arg0, jdouble arg1, jdouble arg2, jdouble arg3) {
     ((void (*)(jdouble, jdouble, jdouble, jdouble))addr)(arg0, arg1, arg2, arg3);
}
void DNI_invokeNative_V_JDDD(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jdouble arg1, jdouble arg2, jdouble arg3) {
     ((void (*)(jlong, jdouble, jdouble, jdouble))addr)(arg0, arg1, arg2, arg3);
}
void DNI_invokeNative_V_JJDD(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jlong arg1, jdouble arg2, jdouble arg3) {
     ((void (*)(jlong, jlong, jdouble, jdouble))addr)(arg0, arg1, arg2, arg3);
}
void DNI_invokeNative_V_JJJD(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jlong arg1, jlong arg2, jdouble arg3) {
     ((void (*)(jlong, jlong, jlong, jdouble))addr)(arg0, arg1, arg2, arg3);
}
void DNI_invokeNative_V_JJJJ(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jlong arg1, jlong arg2, jlong arg3) {
     ((void (*)(jlong, jlong, jlong, jlong))addr)(arg0, arg1, arg2, arg3);
}
void DNI_invokeNative_V_DDDDD(JNIEnv *env, jobject _unused, jlong addr, jdouble arg0, jdouble arg1, jdouble arg2, jdouble arg3, jdouble arg4) {
     ((void (*)(jdouble, jdouble, jdouble, jdouble, jdouble))addr)(arg0, arg1, arg2, arg3, arg4);
}
void DNI_invokeNative_V_JDDDD(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jdouble arg1, jdouble arg2, jdouble arg3, jdouble arg4) {
     ((void (*)(jlong, jdouble, jdouble, jdouble, jdouble))addr)(arg0, arg1, arg2, arg3, arg4);
}
void DNI_invokeNative_V_JJDDD(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jlong arg1, jdouble arg2, jdouble arg3, jdouble arg4) {
     ((void (*)(jlong, jlong, jdouble, jdouble, jdouble))addr)(arg0, arg1, arg2, arg3, arg4);
}
void DNI_invokeNative_V_JJJDD(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jlong arg1, jlong arg2, jdouble arg3, jdouble arg4) {
     ((void (*)(jlong, jlong, jlong, jdouble, jdouble))addr)(arg0, arg1, arg2, arg3, arg4);
}
void DNI_invokeNative_V_JJJJD(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jlong arg1, jlong arg2, jlong arg3, jdouble arg4) {
     ((void (*)(jlong, jlong, jlong, jlong, jdouble))addr)(arg0, arg1, arg2, arg3, arg4);
}
void DNI_invokeNative_V_JJJJJ(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jlong arg1, jlong arg2, jlong arg3, jlong arg4) {
     ((void (*)(jlong, jlong, jlong, jlong, jlong))addr)(arg0, arg1, arg2, arg3, arg4);
}
jlong DNI_invokeNative_J_V(JNIEnv *env, jobject _unused, jlong addr) {
    return ((jlong (*)())addr)();
}
jlong DNI_invokeNative_J_D(JNIEnv *env, jobject _unused, jlong addr, jdouble arg0) {
    return ((jlong (*)(jdouble))addr)(arg0);
}
jlong DNI_invokeNative_J_J(JNIEnv *env, jobject _unused, jlong addr, jlong arg0) {
    return ((jlong (*)(jlong))addr)(arg0);
}
jlong DNI_invokeNative_J_DD(JNIEnv *env, jobject _unused, jlong addr, jdouble arg0, jdouble arg1) {
    return ((jlong (*)(jdouble, jdouble))addr)(arg0, arg1);
}
jlong DNI_invokeNative_J_JD(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jdouble arg1) {
    return ((jlong (*)(jlong, jdouble))addr)(arg0, arg1);
}
jlong DNI_invokeNative_J_JJ(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jlong arg1) {
    return ((jlong (*)(jlong, jlong))addr)(arg0, arg1);
}
jlong DNI_invokeNative_J_DDD(JNIEnv *env, jobject _unused, jlong addr, jdouble arg0, jdouble arg1, jdouble arg2) {
    return ((jlong (*)(jdouble, jdouble, jdouble))addr)(arg0, arg1, arg2);
}
jlong DNI_invokeNative_J_JDD(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jdouble arg1, jdouble arg2) {
    return ((jlong (*)(jlong, jdouble, jdouble))addr)(arg0, arg1, arg2);
}
jlong DNI_invokeNative_J_JJD(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jlong arg1, jdouble arg2) {
    return ((jlong (*)(jlong, jlong, jdouble))addr)(arg0, arg1, arg2);
}
jlong DNI_invokeNative_J_JJJ(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jlong arg1, jlong arg2) {
    return ((jlong (*)(jlong, jlong, jlong))addr)(arg0, arg1, arg2);
}
jlong DNI_invokeNative_J_DDDD(JNIEnv *env, jobject _unused, jlong addr, jdouble arg0, jdouble arg1, jdouble arg2, jdouble arg3) {
    return ((jlong (*)(jdouble, jdouble, jdouble, jdouble))addr)(arg0, arg1, arg2, arg3);
}
jlong DNI_invokeNative_J_JDDD(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jdouble arg1, jdouble arg2, jdouble arg3) {
    return ((jlong (*)(jlong, jdouble, jdouble, jdouble))addr)(arg0, arg1, arg2, arg3);
}
jlong DNI_invokeNative_J_JJDD(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jlong arg1, jdouble arg2, jdouble arg3) {
    return ((jlong (*)(jlong, jlong, jdouble, jdouble))addr)(arg0, arg1, arg2, arg3);
}
jlong DNI_invokeNative_J_JJJD(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jlong arg1, jlong arg2, jdouble arg3) {
    return ((jlong (*)(jlong, jlong, jlong, jdouble))addr)(arg0, arg1, arg2, arg3);
}
jlong DNI_invokeNative_J_JJJJ(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jlong arg1, jlong arg2, jlong arg3) {
    return ((jlong (*)(jlong, jlong, jlong, jlong))addr)(arg0, arg1, arg2, arg3);
}
jlong DNI_invokeNative_J_DDDDD(JNIEnv *env, jobject _unused, jlong addr, jdouble arg0, jdouble arg1, jdouble arg2, jdouble arg3, jdouble arg4) {
    return ((jlong (*)(jdouble, jdouble, jdouble, jdouble, jdouble))addr)(arg0, arg1, arg2, arg3, arg4);
}
jlong DNI_invokeNative_J_JDDDD(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jdouble arg1, jdouble arg2, jdouble arg3, jdouble arg4) {
    return ((jlong (*)(jlong, jdouble, jdouble, jdouble, jdouble))addr)(arg0, arg1, arg2, arg3, arg4);
}
jlong DNI_invokeNative_J_JJDDD(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jlong arg1, jdouble arg2, jdouble arg3, jdouble arg4) {
    return ((jlong (*)(jlong, jlong, jdouble, jdouble, jdouble))addr)(arg0, arg1, arg2, arg3, arg4);
}
jlong DNI_invokeNative_J_JJJDD(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jlong arg1, jlong arg2, jdouble arg3, jdouble arg4) {
    return ((jlong (*)(jlong, jlong, jlong, jdouble, jdouble))addr)(arg0, arg1, arg2, arg3, arg4);
}
jlong DNI_invokeNative_J_JJJJD(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jlong arg1, jlong arg2, jlong arg3, jdouble arg4) {
    return ((jlong (*)(jlong, jlong, jlong, jlong, jdouble))addr)(arg0, arg1, arg2, arg3, arg4);
}
jlong DNI_invokeNative_J_JJJJJ(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jlong arg1, jlong arg2, jlong arg3, jlong arg4) {
    return ((jlong (*)(jlong, jlong, jlong, jlong, jlong))addr)(arg0, arg1, arg2, arg3, arg4);
}
jdouble DNI_invokeNative_D_V(JNIEnv *env, jobject _unused, jlong addr) {
    return ((jdouble (*)())addr)();
}
jdouble DNI_invokeNative_D_D(JNIEnv *env, jobject _unused, jlong addr, jdouble arg0) {
    return ((jdouble (*)(jdouble))addr)(arg0);
}
jdouble DNI_invokeNative_D_J(JNIEnv *env, jobject _unused, jlong addr, jlong arg0) {
    return ((jdouble (*)(jlong))addr)(arg0);
}
jdouble DNI_invokeNative_D_DD(JNIEnv *env, jobject _unused, jlong addr, jdouble arg0, jdouble arg1) {
    return ((jdouble (*)(jdouble, jdouble))addr)(arg0, arg1);
}
jdouble DNI_invokeNative_D_JD(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jdouble arg1) {
    return ((jdouble (*)(jlong, jdouble))addr)(arg0, arg1);
}
jdouble DNI_invokeNative_D_JJ(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jlong arg1) {
    return ((jdouble (*)(jlong, jlong))addr)(arg0, arg1);
}
jdouble DNI_invokeNative_D_DDD(JNIEnv *env, jobject _unused, jlong addr, jdouble arg0, jdouble arg1, jdouble arg2) {
    return ((jdouble (*)(jdouble, jdouble, jdouble))addr)(arg0, arg1, arg2);
}
jdouble DNI_invokeNative_D_JDD(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jdouble arg1, jdouble arg2) {
    return ((jdouble (*)(jlong, jdouble, jdouble))addr)(arg0, arg1, arg2);
}
jdouble DNI_invokeNative_D_JJD(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jlong arg1, jdouble arg2) {
    return ((jdouble (*)(jlong, jlong, jdouble))addr)(arg0, arg1, arg2);
}
jdouble DNI_invokeNative_D_JJJ(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jlong arg1, jlong arg2) {
    return ((jdouble (*)(jlong, jlong, jlong))addr)(arg0, arg1, arg2);
}
jdouble DNI_invokeNative_D_DDDD(JNIEnv *env, jobject _unused, jlong addr, jdouble arg0, jdouble arg1, jdouble arg2, jdouble arg3) {
    return ((jdouble (*)(jdouble, jdouble, jdouble, jdouble))addr)(arg0, arg1, arg2, arg3);
}
jdouble DNI_invokeNative_D_JDDD(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jdouble arg1, jdouble arg2, jdouble arg3) {
    return ((jdouble (*)(jlong, jdouble, jdouble, jdouble))addr)(arg0, arg1, arg2, arg3);
}
jdouble DNI_invokeNative_D_JJDD(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jlong arg1, jdouble arg2, jdouble arg3) {
    return ((jdouble (*)(jlong, jlong, jdouble, jdouble))addr)(arg0, arg1, arg2, arg3);
}
jdouble DNI_invokeNative_D_JJJD(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jlong arg1, jlong arg2, jdouble arg3) {
    return ((jdouble (*)(jlong, jlong, jlong, jdouble))addr)(arg0, arg1, arg2, arg3);
}
jdouble DNI_invokeNative_D_JJJJ(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jlong arg1, jlong arg2, jlong arg3) {
    return ((jdouble (*)(jlong, jlong, jlong, jlong))addr)(arg0, arg1, arg2, arg3);
}
jdouble DNI_invokeNative_D_DDDDD(JNIEnv *env, jobject _unused, jlong addr, jdouble arg0, jdouble arg1, jdouble arg2, jdouble arg3, jdouble arg4) {
    return ((jdouble (*)(jdouble, jdouble, jdouble, jdouble, jdouble))addr)(arg0, arg1, arg2, arg3, arg4);
}
jdouble DNI_invokeNative_D_JDDDD(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jdouble arg1, jdouble arg2, jdouble arg3, jdouble arg4) {
    return ((jdouble (*)(jlong, jdouble, jdouble, jdouble, jdouble))addr)(arg0, arg1, arg2, arg3, arg4);
}
jdouble DNI_invokeNative_D_JJDDD(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jlong arg1, jdouble arg2, jdouble arg3, jdouble arg4) {
    return ((jdouble (*)(jlong, jlong, jdouble, jdouble, jdouble))addr)(arg0, arg1, arg2, arg3, arg4);
}
jdouble DNI_invokeNative_D_JJJDD(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jlong arg1, jlong arg2, jdouble arg3, jdouble arg4) {
    return ((jdouble (*)(jlong, jlong, jlong, jdouble, jdouble))addr)(arg0, arg1, arg2, arg3, arg4);
}
jdouble DNI_invokeNative_D_JJJJD(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jlong arg1, jlong arg2, jlong arg3, jdouble arg4) {
    return ((jdouble (*)(jlong, jlong, jlong, jlong, jdouble))addr)(arg0, arg1, arg2, arg3, arg4);
}
jdouble DNI_invokeNative_D_JJJJJ(JNIEnv *env, jobject _unused, jlong addr, jlong arg0, jlong arg1, jlong arg2, jlong arg3, jlong arg4) {
    return ((jdouble (*)(jlong, jlong, jlong, jlong, jlong))addr)(arg0, arg1, arg2, arg3, arg4);
}

#define CC (char*)  /*cast a literal from (const char*)*/
#define FN_PTR(f) CAST_FROM_FN_PTR(void*, &f)
#define LANG "Ljava/lang/"

// These are the native methods on jdk.internal.foreign.invokers.DirectNativeInvoker.
static JNINativeMethod DNI_methods[] = {
  {CC "invokeNative_V_V", CC "(J)V", FN_PTR(DNI_invokeNative_V_V)},
  {CC "invokeNative_V_D", CC "(JD)V", FN_PTR(DNI_invokeNative_V_D)},
  {CC "invokeNative_V_J", CC "(JJ)V", FN_PTR(DNI_invokeNative_V_J)},
  {CC "invokeNative_V_DD", CC "(JDD)V", FN_PTR(DNI_invokeNative_V_DD)},
  {CC "invokeNative_V_JD", CC "(JJD)V", FN_PTR(DNI_invokeNative_V_JD)},
  {CC "invokeNative_V_JJ", CC "(JJJ)V", FN_PTR(DNI_invokeNative_V_JJ)},
  {CC "invokeNative_V_DDD", CC "(JDDD)V", FN_PTR(DNI_invokeNative_V_DDD)},
  {CC "invokeNative_V_JDD", CC "(JJDD)V", FN_PTR(DNI_invokeNative_V_JDD)},
  {CC "invokeNative_V_JJD", CC "(JJJD)V", FN_PTR(DNI_invokeNative_V_JJD)},
  {CC "invokeNative_V_JJJ", CC "(JJJJ)V", FN_PTR(DNI_invokeNative_V_JJJ)},
  {CC "invokeNative_V_DDDD", CC "(JDDDD)V", FN_PTR(DNI_invokeNative_V_DDDD)},
  {CC "invokeNative_V_JDDD", CC "(JJDDD)V", FN_PTR(DNI_invokeNative_V_JDDD)},
  {CC "invokeNative_V_JJDD", CC "(JJJDD)V", FN_PTR(DNI_invokeNative_V_JJDD)},
  {CC "invokeNative_V_JJJD", CC "(JJJJD)V", FN_PTR(DNI_invokeNative_V_JJJD)},
  {CC "invokeNative_V_JJJJ", CC "(JJJJJ)V", FN_PTR(DNI_invokeNative_V_JJJJ)},
  {CC "invokeNative_V_DDDDD", CC "(JDDDDD)V", FN_PTR(DNI_invokeNative_V_DDDDD)},
  {CC "invokeNative_V_JDDDD", CC "(JJDDDD)V", FN_PTR(DNI_invokeNative_V_JDDDD)},
  {CC "invokeNative_V_JJDDD", CC "(JJJDDD)V", FN_PTR(DNI_invokeNative_V_JJDDD)},
  {CC "invokeNative_V_JJJDD", CC "(JJJJDD)V", FN_PTR(DNI_invokeNative_V_JJJDD)},
  {CC "invokeNative_V_JJJJD", CC "(JJJJJD)V", FN_PTR(DNI_invokeNative_V_JJJJD)},
  {CC "invokeNative_V_JJJJJ", CC "(JJJJJJ)V", FN_PTR(DNI_invokeNative_V_JJJJJ)},
  {CC "invokeNative_J_V", CC "(J)J", FN_PTR(DNI_invokeNative_J_V)},
  {CC "invokeNative_J_D", CC "(JD)J", FN_PTR(DNI_invokeNative_J_D)},
  {CC "invokeNative_J_J", CC "(JJ)J", FN_PTR(DNI_invokeNative_J_J)},
  {CC "invokeNative_J_DD", CC "(JDD)J", FN_PTR(DNI_invokeNative_J_DD)},
  {CC "invokeNative_J_JD", CC "(JJD)J", FN_PTR(DNI_invokeNative_J_JD)},
  {CC "invokeNative_J_JJ", CC "(JJJ)J", FN_PTR(DNI_invokeNative_J_JJ)},
  {CC "invokeNative_J_DDD", CC "(JDDD)J", FN_PTR(DNI_invokeNative_J_DDD)},
  {CC "invokeNative_J_JDD", CC "(JJDD)J", FN_PTR(DNI_invokeNative_J_JDD)},
  {CC "invokeNative_J_JJD", CC "(JJJD)J", FN_PTR(DNI_invokeNative_J_JJD)},
  {CC "invokeNative_J_JJJ", CC "(JJJJ)J", FN_PTR(DNI_invokeNative_J_JJJ)},
  {CC "invokeNative_J_DDDD", CC "(JDDDD)J", FN_PTR(DNI_invokeNative_J_DDDD)},
  {CC "invokeNative_J_JDDD", CC "(JJDDD)J", FN_PTR(DNI_invokeNative_J_JDDD)},
  {CC "invokeNative_J_JJDD", CC "(JJJDD)J", FN_PTR(DNI_invokeNative_J_JJDD)},
  {CC "invokeNative_J_JJJD", CC "(JJJJD)J", FN_PTR(DNI_invokeNative_J_JJJD)},
  {CC "invokeNative_J_JJJJ", CC "(JJJJJ)J", FN_PTR(DNI_invokeNative_J_JJJJ)},
  {CC "invokeNative_J_DDDDD", CC "(JDDDDD)J", FN_PTR(DNI_invokeNative_J_DDDDD)},
  {CC "invokeNative_J_JDDDD", CC "(JJDDDD)J", FN_PTR(DNI_invokeNative_J_JDDDD)},
  {CC "invokeNative_J_JJDDD", CC "(JJJDDD)J", FN_PTR(DNI_invokeNative_J_JJDDD)},
  {CC "invokeNative_J_JJJDD", CC "(JJJJDD)J", FN_PTR(DNI_invokeNative_J_JJJDD)},
  {CC "invokeNative_J_JJJJD", CC "(JJJJJD)J", FN_PTR(DNI_invokeNative_J_JJJJD)},
  {CC "invokeNative_J_JJJJJ", CC "(JJJJJJ)J", FN_PTR(DNI_invokeNative_J_JJJJJ)},
  {CC "invokeNative_D_V", CC "(J)D", FN_PTR(DNI_invokeNative_D_V)},
  {CC "invokeNative_D_D", CC "(JD)D", FN_PTR(DNI_invokeNative_D_D)},
  {CC "invokeNative_D_J", CC "(JJ)D", FN_PTR(DNI_invokeNative_D_J)},
  {CC "invokeNative_D_DD", CC "(JDD)D", FN_PTR(DNI_invokeNative_D_DD)},
  {CC "invokeNative_D_JD", CC "(JJD)D", FN_PTR(DNI_invokeNative_D_JD)},
  {CC "invokeNative_D_JJ", CC "(JJJ)D", FN_PTR(DNI_invokeNative_D_JJ)},
  {CC "invokeNative_D_DDD", CC "(JDDD)D", FN_PTR(DNI_invokeNative_D_DDD)},
  {CC "invokeNative_D_JDD", CC "(JJDD)D", FN_PTR(DNI_invokeNative_D_JDD)},
  {CC "invokeNative_D_JJD", CC "(JJJD)D", FN_PTR(DNI_invokeNative_D_JJD)},
  {CC "invokeNative_D_JJJ", CC "(JJJJ)D", FN_PTR(DNI_invokeNative_D_JJJ)},
  {CC "invokeNative_D_DDDD", CC "(JDDDD)D", FN_PTR(DNI_invokeNative_D_DDDD)},
  {CC "invokeNative_D_JDDD", CC "(JJDDD)D", FN_PTR(DNI_invokeNative_D_JDDD)},
  {CC "invokeNative_D_JJDD", CC "(JJJDD)D", FN_PTR(DNI_invokeNative_D_JJDD)},
  {CC "invokeNative_D_JJJD", CC "(JJJJD)D", FN_PTR(DNI_invokeNative_D_JJJD)},
  {CC "invokeNative_D_JJJJ", CC "(JJJJJ)D", FN_PTR(DNI_invokeNative_D_JJJJ)},
  {CC "invokeNative_D_DDDDD", CC "(JDDDDD)D", FN_PTR(DNI_invokeNative_D_DDDDD)},
  {CC "invokeNative_D_JDDDD", CC "(JJDDDD)D", FN_PTR(DNI_invokeNative_D_JDDDD)},
  {CC "invokeNative_D_JJDDD", CC "(JJJDDD)D", FN_PTR(DNI_invokeNative_D_JJDDD)},
  {CC "invokeNative_D_JJJDD", CC "(JJJJDD)D", FN_PTR(DNI_invokeNative_D_JJJDD)},
  {CC "invokeNative_D_JJJJD", CC "(JJJJJD)D", FN_PTR(DNI_invokeNative_D_JJJJD)},
  {CC "invokeNative_D_JJJJJ", CC "(JJJJJJ)D", FN_PTR(DNI_invokeNative_D_JJJJJ)}
};

/**
 * This one function is exported, used by NativeLookup.
 */
JVM_ENTRY(void, JVM_RegisterDirectNativeInvokerMethods(JNIEnv *env, jclass DNI_class)) {
  {
    ThreadToNativeFromVM ttnfv(thread);

    int status = env->RegisterNatives(DNI_class, DNI_methods, sizeof(DNI_methods)/sizeof(JNINativeMethod));
    guarantee(status == JNI_OK && !env->ExceptionOccurred(),
              "register jdk.internal.foreign.invokers.DirectNativeInvoker natives");
  }
}
JVM_END
