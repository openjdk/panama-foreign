/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
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

#ifndef SHARE_PRIMS_VMSTORAGE_X86
#define SHARE_PRIMS_VMSTORAGE_X86

#include <cstdint>

#include "asm/register.hpp"
#include "prims/vmstorageBase.inline.hpp"

// keep in sync with jdk/internal/foreign/abi/x64/X86_64Architecture
enum class RegType : int8_t {
  STACK = 0,
  INTEGER = 1,
  VECTOR = 2,
  X87 = 3,
  INVALID = -1
};

constexpr uint16_t REG64_MASK = 0b0000000000001111;
constexpr uint16_t XMM_MASK   = 0b0000000000000001;

constexpr VMStorage VMS_RAX = VMStorage::reg_storage(RegType::INTEGER, REG64_MASK, 0);
constexpr VMStorage VMS_RBX = VMStorage::reg_storage(RegType::INTEGER, REG64_MASK, 3);
constexpr VMStorage VMS_XMM0 = VMStorage::reg_storage(RegType::VECTOR, XMM_MASK, 0);

inline Register as_Register(VMStorage vms) {
  assert(vms.type() == RegType::INTEGER, "not the right type");
  return ::as_Register(vms.index());
}

inline XMMRegister as_XMMRegister(VMStorage vms) {
  assert(vms.type() == RegType::VECTOR, "not the right type");
  return ::as_XMMRegister(vms.index());
}

inline VMStorage as_VMStorage(Register reg) {
  return VMStorage::reg_storage(RegType::INTEGER, REG64_MASK, reg->encoding());
}

inline VMStorage as_VMStorage(XMMRegister reg) {
  return VMStorage::reg_storage(RegType::VECTOR, XMM_MASK, reg->encoding());
}

inline VMStorage as_VMStorage(VMReg reg) {
  if (reg->is_Register()) {
    return as_VMStorage(reg->as_Register());
  } else if (reg->is_XMMRegister()) {
    return as_VMStorage(reg->as_XMMRegister());
  } else if (reg->is_stack()) {
    return VMStorage::stack_storage(reg);
  } else if (!reg->is_valid()) {
    return VMStorage::invalid();
  }

  ShouldNotReachHere();
  return VMStorage::invalid();
}

#endif // SHARE_PRIMS_VMSTORAGE_X86