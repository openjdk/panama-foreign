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

#ifndef SHARE_VM_CI_CIABIDESCRIPTOR_HPP
#define SHARE_VM_CI_CIABIDESCRIPTOR_HPP

#include "ci/ciInstance.hpp"
#include "ciObjArray.hpp"

// ciABIDescriptor
//
// The class represents a jdk.internal.foreign.abi.ABIDescriptor object.
class ciABIDescriptor : public ciInstance {
public:
  ciABIDescriptor(instanceHandle h_i) : ciInstance(h_i) {}

  // What kind of ciObject is this?
  bool is_abi_descriptor() const { return true; }

  ciObjArray* input_storage() const;
  ciObjArray* output_storage() const;
  ciObjArray* volatile_storage() const;
  jint stack_alignment() const;
  jint shadow_space() const;
};

#endif // SHARE_VM_CI_CIABIDESCRIPTOR_HPP
