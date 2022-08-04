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

#ifndef SHARE_PRIMS_VMSTORAGEBASE
#define SHARE_PRIMS_VMSTORAGEBASE

// DO NOT INCLUDE THIS FILE. INCLUDE vmstorage.inline.hpp INSTEAD

#include <cstdint>

#include "code/vmreg.hpp"
#include "utilities/debug.hpp"
#include "utilities/ostream.hpp"

enum class RegType : int8_t; // defined in arch specific headers

class VMStorage {
public:
  constexpr static RegType INVALID_TYPE = static_cast<RegType>(-1);
private:
  RegType _type;
  uint8_t _reserved;
  union {
    uint16_t _segment_mask;
    uint16_t _size;
  };
  uint32_t _index; // stack offset in bytes for stack storage

  friend bool operator==(const VMStorage& a, const VMStorage& b);
public:
  constexpr VMStorage() : _type(INVALID_TYPE), _reserved(0), _segment_mask(0), _index(0) {};

  constexpr static VMStorage reg_storage(RegType type, uint16_t segment_mask, uint32_t index) {
    assert(type != stack_type(), "can not be stack type");
    assert(type != INVALID_TYPE, "can not be invalid type");
    VMStorage result;
    result._type = type;
    result._segment_mask = segment_mask;
    result._index = index;
    return result;
  }

  constexpr static VMStorage stack_storage(uint16_t size, uint32_t index) {
    VMStorage result;
    result._type = stack_type();
    result._size = size;
    result._index = index;
    return result;
  }

  static VMStorage stack_storage(VMReg reg) {
    return stack_storage(BytesPerWord, checked_cast<uint16_t>(reg->reg2stack() * VMRegImpl::stack_slot_size));
  }

  constexpr static VMStorage invalid() {
    VMStorage result;
    result._type = INVALID_TYPE;
    return result;
  }

  constexpr inline static RegType stack_type();

  RegType type() const { return _type; }
  uint16_t segment_mask() const { assert(is_reg(), "must be reg"); return _segment_mask; }
  uint16_t stack_size() const { assert(is_stack(), "must be stack"); return _size; }
  uint32_t index() const { assert(is_valid(), "no index"); return _index; }

  bool is_valid() const { return _type != INVALID_TYPE; }
  bool is_reg() const { return is_valid() && !is_stack(); }
  bool is_stack() const { return _type == stack_type(); }

  void print_on(outputStream* os) const;
};

inline bool operator==(const VMStorage& a, const VMStorage& b) {
  return a._type == b._type
    && a._index == b._index
    && (a.is_stack()
      ? a._size == b._size
      : a._segment_mask == b._segment_mask);
}

#endif // SHARE_PRIMS_VMSTORAGEBASE
