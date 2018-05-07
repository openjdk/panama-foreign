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
#include "ci/ciMachineCodeSnippet.hpp"
#include "ci/ciUtilities.inline.hpp"
#include "classfile/javaClasses.hpp"
#include "oops/oop.inline.hpp"

ciObject* ciMachineCodeSnippet::reg_masks() const {
  VM_ENTRY_MARK;
  return CURRENT_ENV->get_object(java_lang_invoke_MachineCodeSnippet::reg_masks(get_oop()));
}

ciObject* ciMachineCodeSnippet::killed_reg_mask() const {
  VM_ENTRY_MARK;
  return CURRENT_ENV->get_object(java_lang_invoke_MachineCodeSnippet::killed_reg_mask(get_oop()));
}

ciObject* ciMachineCodeSnippet::generator() const {
  VM_ENTRY_MARK;
  return CURRENT_ENV->get_object(java_lang_invoke_MachineCodeSnippet::generator(get_oop()));
}

jint ciMachineCodeSnippet::flags() const {
  VM_ENTRY_MARK;
  return java_lang_invoke_MachineCodeSnippet::flags(get_oop());
}
