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
#include "ci/ciClassList.hpp"
#include "ci/ciMethodType.hpp"
#include "ci/ciNativeEntryPoint.hpp"
#include "ci/ciUtilities.hpp"
#include "classfile/javaClasses.hpp"
#include "oops/oop.inline.hpp"

// ------------------------------------------------------------------
// ciNativeEntryPoint::get_entry_point
//
// Return: NEP.addr
address ciNativeEntryPoint::get_entry_point() const {
  VM_ENTRY_MARK;
  return java_lang_invoke_NativeEntryPoint::addr(get_oop());
}

void ciNativeEntryPoint::init() {
  VM_ENTRY_MARK;
  // Copy name
  oop name_str = java_lang_invoke_NativeEntryPoint::name(get_oop());
  if (name_str != NULL) {
    char* temp_name = java_lang_String::as_quoted_ascii(name_str);
    size_t len = strlen(temp_name) + 1;
    char* name = (char*)CURRENT_ENV->arena()->Amalloc(len);
    strncpy(name, temp_name, len);
    _name = name;
  }
}

const char* ciNativeEntryPoint::name() {
  if (_name == NULL)  init();
  return _name;
}

ciMethodType* ciNativeEntryPoint::method_type() const {
  VM_ENTRY_MARK;
  return CURRENT_ENV->get_object(java_lang_invoke_NativeEntryPoint::type(get_oop()))->as_method_type();
}
