/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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

#include "jdk_internal_foreign_abi_fallback_FallbackLinker.h"
#include "jdk_internal_foreign_abi_fallback_FallbackLinker_ffi_type.h"

#include <ffi.h>

#include <errno.h>
#ifdef _WIN64
#include <Windows.h>
#include <Winsock2.h>
#endif

#include "jlong.h"

JNIEXPORT jlong JNICALL
Java_jdk_internal_foreign_abi_fallback_FallbackLinker_sizeofCif(JNIEnv* env, jclass cls) {
  return sizeof(ffi_cif);
}

JNIEXPORT jint JNICALL
Java_jdk_internal_foreign_abi_fallback_FallbackLinker_ffi_1prep_1cif(JNIEnv* env, jclass cls, jlong cif, jint abi, jint nargs, jlong rtype, jlong atypes) {
  return ffi_prep_cif(jlong_to_ptr(cif), (ffi_abi) abi, (unsigned int) nargs, jlong_to_ptr(rtype), jlong_to_ptr(atypes));
}
JNIEXPORT jint JNICALL
Java_jdk_internal_foreign_abi_fallback_FallbackLinker_ffi_1get_1struct_1offsets(JNIEnv* env, jclass cls, jint abi, jlong type, jlong offsets) {
  return ffi_get_struct_offsets((ffi_abi) abi, jlong_to_ptr(type), jlong_to_ptr(offsets));
}

static void do_capture_state(int32_t* value_ptr, int captured_state_mask) {
    // keep in synch with jdk.internal.foreign.abi.CapturableState
  enum PreservableValues {
    NONE = 0,
    GET_LAST_ERROR = 1,
    WSA_GET_LAST_ERROR = 1 << 1,
    ERRNO = 1 << 2
  };
#ifdef _WIN64
  if (captured_state_mask & GET_LAST_ERROR) {
    *value_ptr = GetLastError();
    value_ptr++;
  }
  if (captured_state_mask & WSA_GET_LAST_ERROR) {
    *value_ptr = WSAGetLastError();
    value_ptr++;
  }
#endif
  if (captured_state_mask & ERRNO) {
    *value_ptr = errno;
  }
}

JNIEXPORT void JNICALL
Java_jdk_internal_foreign_abi_fallback_FallbackLinker_ffi_1call(JNIEnv* env, jclass cls, jlong cif, jlong fn, jlong rvalue, jlong avalues, jlong jcaptured_state, jint captured_state_mask) {
  ffi_call(jlong_to_ptr(cif), jlong_to_ptr(fn), jlong_to_ptr(rvalue), jlong_to_ptr(avalues));

  if (captured_state_mask != 0) {
    int32_t* captured_state = jlong_to_ptr(jcaptured_state);
    do_capture_state(captured_state, captured_state_mask);
  }
}

JNIEXPORT jint JNICALL
Java_jdk_internal_foreign_abi_fallback_FallbackLinker_ffi_1default_1abi(JNIEnv* env, jclass cls) {
  return (jint) FFI_DEFAULT_ABI;
}

JNIEXPORT jshort JNICALL
Java_jdk_internal_foreign_abi_fallback_FallbackLinker_ffi_1type_1struct(JNIEnv* env, jclass cls) {
  return (jshort) FFI_TYPE_STRUCT;
}

JNIEXPORT jlong JNICALL
Java_jdk_internal_foreign_abi_fallback_FallbackLinker_ffi_1type_1void(JNIEnv* env, jclass cls) {
  return ptr_to_jlong(&ffi_type_void);
}

JNIEXPORT jlong JNICALL
Java_jdk_internal_foreign_abi_fallback_FallbackLinker_ffi_1type_1uint8(JNIEnv* env, jclass cls) {
  return ptr_to_jlong(&ffi_type_uint8);
}
JNIEXPORT jlong JNICALL
Java_jdk_internal_foreign_abi_fallback_FallbackLinker_ffi_1type_1sint8(JNIEnv* env, jclass cls) {
  return ptr_to_jlong(&ffi_type_sint8);
}
JNIEXPORT jlong JNICALL
Java_jdk_internal_foreign_abi_fallback_FallbackLinker_ffi_1type_1uint16(JNIEnv* env, jclass cls) {
  return ptr_to_jlong(&ffi_type_uint16);
}
JNIEXPORT jlong JNICALL
Java_jdk_internal_foreign_abi_fallback_FallbackLinker_ffi_1type_1sint16(JNIEnv* env, jclass cls) {
  return ptr_to_jlong(&ffi_type_sint16);
}
JNIEXPORT jlong JNICALL
Java_jdk_internal_foreign_abi_fallback_FallbackLinker_ffi_1type_1uint32(JNIEnv* env, jclass cls) {
  return ptr_to_jlong(&ffi_type_uint32);
}
JNIEXPORT jlong JNICALL
Java_jdk_internal_foreign_abi_fallback_FallbackLinker_ffi_1type_1sint32(JNIEnv* env, jclass cls) {
  return ptr_to_jlong(&ffi_type_sint32);
}
JNIEXPORT jlong JNICALL
Java_jdk_internal_foreign_abi_fallback_FallbackLinker_ffi_1type_1uint64(JNIEnv* env, jclass cls) {
  return ptr_to_jlong(&ffi_type_uint64);
}
JNIEXPORT jlong JNICALL
Java_jdk_internal_foreign_abi_fallback_FallbackLinker_ffi_1type_1sint64(JNIEnv* env, jclass cls) {
  return ptr_to_jlong(&ffi_type_sint64);
}
JNIEXPORT jlong JNICALL
Java_jdk_internal_foreign_abi_fallback_FallbackLinker_ffi_1type_1float(JNIEnv* env, jclass cls) {
  return ptr_to_jlong(&ffi_type_float);
}
JNIEXPORT jlong JNICALL
Java_jdk_internal_foreign_abi_fallback_FallbackLinker_ffi_1type_1double(JNIEnv* env, jclass cls) {
  return ptr_to_jlong(&ffi_type_double);
}
JNIEXPORT jlong JNICALL
Java_jdk_internal_foreign_abi_fallback_FallbackLinker_ffi_1type_1pointer(JNIEnv* env, jclass cls) {
  return ptr_to_jlong(&ffi_type_pointer);
}

// ffi_type impl

/*
 * Class:     jdk_internal_foreign_abi_fallback_FallbackLinker_ffi_type
 * Method:    sizeof
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_jdk_internal_foreign_abi_fallback_FallbackLinker_00024ffi_1type_sizeof
  (JNIEnv *env, jclass cls) {
  return sizeof(ffi_type);
}

/*
 * Class:     jdk_internal_foreign_abi_fallback_FallbackLinker_ffi_type
 * Method:    setType
 * Signature: (JS)V
 */
JNIEXPORT void JNICALL Java_jdk_internal_foreign_abi_fallback_FallbackLinker_00024ffi_1type_setType
  (JNIEnv *env, jclass cls, jlong lptr, jshort type) {
  ffi_type* ptr = jlong_to_ptr(lptr);
  ptr->type = type;
}

/*
 * Class:     jdk_internal_foreign_abi_fallback_FallbackLinker_ffi_type
 * Method:    setElements
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_jdk_internal_foreign_abi_fallback_FallbackLinker_00024ffi_1type_setElements
  (JNIEnv *env, jclass cls, jlong lptr, jlong lelements) {
  ffi_type* ptr = jlong_to_ptr(lptr);
  ptr->elements = jlong_to_ptr(lelements);
}
