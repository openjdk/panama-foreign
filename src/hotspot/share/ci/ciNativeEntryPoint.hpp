/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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

#ifndef SHARE_VM_CI_CINATIVEENTRYPOINT_HPP
#define SHARE_VM_CI_CINATIVEENTRYPOINT_HPP

#include "ci/ciInstance.hpp"
#include "ci/ciObjArray.hpp"
#include "ci/ciABIDescriptor.hpp"
#include "ci/ciMethodType.hpp"

// ciNativeEntryPoint
//
// The class represents a java.lang.invoke.NativeEntryPoint object.
class ciNativeEntryPoint : public ciInstance {
private:
  const char* _name;

  void init();
public:
  ciNativeEntryPoint(instanceHandle h_i) : ciInstance(h_i), _name(NULL) {}

  // What kind of ciObject is this?
  bool is_native_entry_point() const { return true; }

  address          entry_point() const;
  ciABIDescriptor* abi_descriptor() const;
  ciObjArray*      argMoves() const;
  ciObjArray*      returnMoves() const;
  jboolean         need_transition() const;
  ciMethodType*    method_type() const;
  const char*      name();
};

#endif // SHARE_VM_CI_CINATIVEENTRYPOINT_HPP
