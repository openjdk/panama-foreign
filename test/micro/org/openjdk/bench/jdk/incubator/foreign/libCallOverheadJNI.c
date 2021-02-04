/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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
#include <jni.h>
#include <stdlib.h>
#include "libCallOverhead.c"

JNIEXPORT void JNICALL Java_org_openjdk_bench_jdk_incubator_foreign_CallOverhead_blank
  (JNIEnv *env, jclass cls) {
    func();
}

JNIEXPORT jint JNICALL Java_org_openjdk_bench_jdk_incubator_foreign_CallOverhead_identity
  (JNIEnv *env, jclass cls, jint x) {
    return identity(x);
}

JNIEXPORT jobject JNICALL Java_org_openjdk_bench_jdk_incubator_foreign_CallOverhead_identityStruct
  (JNIEnv *env, jclass cls, jobject pointBuf) {
    Point *pInput = (Point*)(*env)->GetDirectBufferAddress(env, pointBuf);
    Point p = identity_struct(*pInput);
    Point *ret = (Point*)malloc(sizeof(Point));
    ret->x = p.x;
    ret->y = p.y;
    return (jobject)(*env)->NewDirectByteBuffer(env, ret, sizeof(Point));
}

JNIEXPORT jobject JNICALL Java_org_openjdk_bench_jdk_incubator_foreign_CallOverhead_identityStructAlloc
  (JNIEnv *env, jclass cls, jobject pointBuf, jobject resBuf) {
    Point *pInput = (Point*)(*env)->GetDirectBufferAddress(env, pointBuf);
    Point p = identity_struct(*pInput);
    Point *pOutput = (Point*)(*env)->GetDirectBufferAddress(env, resBuf);
    pOutput->x = p.x;
    pOutput->y = p.y;
    return resBuf;
}

JNIEXPORT void JNICALL Java_org_openjdk_bench_jdk_incubator_foreign_CallOverhead_freeStruct
 (JNIEnv *env, jclass cls, jobject pointBuf) {
    void *buf = (void*)(*env)->GetDirectBufferAddress(env, pointBuf);
    free(buf);
}
