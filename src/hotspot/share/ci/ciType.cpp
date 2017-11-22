/*
 * Copyright (c) 2000, 2017, Oracle and/or its affiliates. All rights reserved.
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
#include "ci/ciEnv.hpp"
#include "ci/ciType.hpp"
#include "ci/ciUtilities.hpp"
#include "classfile/systemDictionary.hpp"
#include "memory/resourceArea.hpp"
#include "oops/oop.inline.hpp"

ciType* ciType::_basic_types[T_CONFLICT+1];

// ciType
//
// This class represents either a class (T_OBJECT), array (T_ARRAY),
// or one of the primitive types such as T_INT.

// ------------------------------------------------------------------
// ciType::ciType
//
ciType::ciType(BasicType basic_type) : ciMetadata() {
  assert(basic_type >= T_BOOLEAN && basic_type <= T_CONFLICT, "range check");
  _basic_type = basic_type;
}

ciType::ciType(Klass* k) : ciMetadata(k) {
  _basic_type = k->is_array_klass() ? T_ARRAY : T_OBJECT;
}


// ------------------------------------------------------------------
// ciType::is_subtype_of
//
bool ciType::is_subtype_of(ciType* type) {
  if (this == type)  return true;
  if (is_klass() && type->is_klass())
    return this->as_klass()->is_subtype_of(type->as_klass());
  return false;
}

// ------------------------------------------------------------------
// ciType::name
//
// Return the name of this type
const char* ciType::name() {
  if (is_primitive_type()) {
    return type2name(basic_type());
  } else {
    assert(is_klass(), "must be");
    return as_klass()->name()->as_utf8();
  }
}

// ------------------------------------------------------------------
// ciType::print_impl
//
// Implementation of the print method.
void ciType::print_impl(outputStream* st) {
  st->print(" type=");
  print_name_on(st);
}

// ------------------------------------------------------------------
// ciType::print_name
//
// Print the name of this type
void ciType::print_name_on(outputStream* st) {
  ResourceMark rm;
  st->print("%s", name());
}



// ------------------------------------------------------------------
// ciType::java_mirror
//
ciInstance* ciType::java_mirror() {
  VM_ENTRY_MARK;
  return CURRENT_THREAD_ENV->get_instance(Universe::java_mirror(basic_type()));
}

// ------------------------------------------------------------------
// ciType::box_klass
//
ciKlass* ciType::box_klass() {
  if (!is_primitive_type())  return this->as_klass();  // reference types are "self boxing"

  // Void is "boxed" with a null.
  if (basic_type() == T_VOID)  return NULL;

  VM_ENTRY_MARK;
  return CURRENT_THREAD_ENV->get_instance_klass(SystemDictionary::box_klass(basic_type()));
}


// ------------------------------------------------------------------
// ciType::make
//
// Produce the ciType for a given primitive BasicType.
// As a bonus, produce the right reference type for T_OBJECT.
// Does not work on T_ARRAY.
ciType* ciType::make(BasicType t) {
  // short, etc.
  // Note: Bare T_ADDRESS means a raw pointer type, not a return_address.
  assert((uint)t < T_CONFLICT+1, "range check");
  if (t == T_OBJECT)  return ciEnv::_Object_klass;  // java/lang/Object
  assert(_basic_types[t] != NULL, "domain check");
  return _basic_types[t];
}

static bool is_float128vector(BasicType bt, vmSymbols::SID sid) {
  return bt == T_OBJECT && sid == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Float128Vector);
}
static bool is_float128species(BasicType bt, vmSymbols::SID sid) {
  return bt == T_OBJECT && sid == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Float128Vector_Float128Species);
}
static bool is_float128mask(BasicType bt, vmSymbols::SID sid) {
  return bt == T_OBJECT && sid == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Float128Vector_Float128Mask);
}
static bool is_float128(BasicType bt, vmSymbols::SID sid) {
  return is_float128vector(bt, sid) || is_float128species(bt, sid) || is_float128mask(bt, sid);
}
static bool is_float256vector(BasicType bt, vmSymbols::SID sid) {
  return bt == T_OBJECT && sid == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Float256Vector);
}
static bool is_float256species(BasicType bt, vmSymbols::SID sid) {
  return bt == T_OBJECT && sid == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Float256Vector_Float256Species);
}
static bool is_float256mask(BasicType bt, vmSymbols::SID sid) {
  return bt == T_OBJECT && sid == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Float256Vector_Float256Mask);
}
static bool is_float256(BasicType bt, vmSymbols::SID sid) {
  return is_float256vector(bt, sid) || is_float256species(bt, sid) || is_float256mask(bt, sid);
}
static bool is_float512vector(BasicType bt, vmSymbols::SID sid) {
  return bt == T_OBJECT && sid == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Float512Vector);
}
static bool is_float512species(BasicType bt, vmSymbols::SID sid) {
  return bt == T_OBJECT && sid == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Float512Vector_Float512Species);
}
static bool is_float512mask(BasicType bt, vmSymbols::SID sid) {
  return bt == T_OBJECT && sid == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Float512Vector_Float512Mask);
}
static bool is_float512(BasicType bt, vmSymbols::SID sid) {
  return is_float512vector(bt, sid) || is_float512species(bt, sid) || is_float512mask(bt, sid);
}
static bool is_float_vec_or_mask(BasicType bt, vmSymbols::SID sid) {
  return is_float128(bt, sid) || is_float256(bt, sid) || is_float512(bt, sid);
}
static bool is_double128vector(BasicType bt, vmSymbols::SID sid) {
  return bt == T_OBJECT && sid == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Double128Vector);
}
static bool is_double128species(BasicType bt, vmSymbols::SID sid) {
  return bt == T_OBJECT && sid == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Double128Vector_Double128Species);
}
static bool is_double128mask(BasicType bt, vmSymbols::SID sid) {
  return bt == T_OBJECT && sid == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Double128Vector_Double128Mask);
}
static bool is_double128(BasicType bt, vmSymbols::SID sid) {
  return is_double128vector(bt, sid) || is_double128species(bt, sid) || is_double128mask(bt, sid);
}
static bool is_double256vector(BasicType bt, vmSymbols::SID sid) {
  return bt == T_OBJECT && sid == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Double256Vector);
}
static bool is_double256species(BasicType bt, vmSymbols::SID sid) {
  return bt == T_OBJECT && sid == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Double256Vector_Double256Species);
}
static bool is_double256mask(BasicType bt, vmSymbols::SID sid) {
  return bt == T_OBJECT && sid == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Double256Vector_Double256Mask);
}
static bool is_double256(BasicType bt, vmSymbols::SID sid) {
  return is_double256vector(bt, sid) || is_double256species(bt, sid) || is_double256mask(bt, sid);
}
static bool is_double512vector(BasicType bt, vmSymbols::SID sid) {
  return bt == T_OBJECT && sid == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Double512Vector);
}
static bool is_double512species(BasicType bt, vmSymbols::SID sid) {
  return bt == T_OBJECT && sid == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Double512Vector_Double512Species);
}
static bool is_double512mask(BasicType bt, vmSymbols::SID sid) {
  return bt == T_OBJECT && sid == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Double512Vector_Double512Mask);
}
static bool is_double512(BasicType bt, vmSymbols::SID sid) {
  return is_double512vector(bt, sid) || is_double512species(bt, sid) || is_double512mask(bt, sid);
}
static bool is_double_vec_or_mask(BasicType bt, vmSymbols::SID sid) {
  return is_double128(bt, sid) || is_double256(bt, sid) || is_double512(bt, sid);
}
static bool is_int128vector(BasicType bt, vmSymbols::SID sid) {
  return bt == T_OBJECT && sid == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Int128Vector);
}
static bool is_int128species(BasicType bt, vmSymbols::SID sid) {
  return bt == T_OBJECT && sid == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Int128Vector_Int128Species);
}
static bool is_int128mask(BasicType bt, vmSymbols::SID sid) {
  return bt == T_OBJECT && sid == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Int128Vector_Int128Mask);
}
static bool is_int128(BasicType bt, vmSymbols::SID sid) {
  return is_int128vector(bt, sid) || is_int128species(bt, sid) || is_int128mask(bt, sid);
}
static bool is_int256vector(BasicType bt, vmSymbols::SID sid) {
  return bt == T_OBJECT && sid == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Int256Vector);
}
static bool is_int256species(BasicType bt, vmSymbols::SID sid) {
  return bt == T_OBJECT && sid == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Int256Vector_Int256Species);
}
static bool is_int256mask(BasicType bt, vmSymbols::SID sid) {
  return bt == T_OBJECT && sid == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Int256Vector_Int256Mask);
}
static bool is_int256(BasicType bt, vmSymbols::SID sid) {
  return is_int256vector(bt, sid) || is_int256species(bt, sid) || is_int256mask(bt, sid);
}
static bool is_int512vector(BasicType bt, vmSymbols::SID sid) {
  return bt == T_OBJECT && sid == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Int512Vector);
}
static bool is_int512species(BasicType bt, vmSymbols::SID sid) {
  return bt == T_OBJECT && sid == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Int512Vector_Int512Species);
}
static bool is_int512mask(BasicType bt, vmSymbols::SID sid) {
  return bt == T_OBJECT && sid == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Int512Vector_Int512Mask);
}
static bool is_int512(BasicType bt, vmSymbols::SID sid) {
  return is_int512vector(bt, sid) || is_int512species(bt, sid) || is_int512mask(bt, sid);
}
static bool is_int_vec_or_mask(BasicType bt, vmSymbols::SID sid) {
  return is_int128(bt, sid) || is_int256(bt, sid) || is_int512(bt, sid);
}

#define __ basic_type(), as_klass()->name()->sid()

bool ciType::is_vectormask() {
  return basic_type() == T_OBJECT &&
      (as_klass()->name()->sid() == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_VectorMask) ||
      (as_klass()->name()->sid() == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_VectorAbstractMask)) ||
      is_float128mask(__) || is_float256mask(__) || is_float512mask(__) ||
      is_double128mask(__) || is_double256mask(__) || is_double512mask(__) ||
      is_int128mask(__) || is_int256mask(__) || is_int512mask(__));
}

bool ciType::is_vectorapi_vector() {
  return is_float_vec_or_mask(__) ||
      is_double_vec_or_mask(__) ||
      is_int_vec_or_mask(__);
}

int ciType::vectorapi_vector_size() {
  if ( is_double128(__) ) return 2;
  if ( is_float128(__) || is_int128(__) || is_double256(__) ) return 4;
  if ( is_float256(__) || is_int256(__) || is_double512(__) ) return 8;
  if ( is_float512(__) || is_int512(__)) return 16;
  return -1;
}

BasicType ciType::vectorapi_vector_bt() {
  if ( is_float_vec_or_mask(__) ) return T_FLOAT;
  if ( is_double_vec_or_mask(__) ) return T_DOUBLE;
  if ( is_int_vec_or_mask(__) ) return T_INT;
  return T_VOID;
}

#undef __

// ciReturnAddress
//
// This class represents the type of a specific return address in the
// bytecodes.

// ------------------------------------------------------------------
// ciReturnAddress::ciReturnAddress
//
ciReturnAddress::ciReturnAddress(int bci) : ciType(T_ADDRESS) {
  assert(0 <= bci, "bci cannot be negative");
  _bci = bci;
}

// ------------------------------------------------------------------
// ciReturnAddress::print_impl
//
// Implementation of the print method.
void ciReturnAddress::print_impl(outputStream* st) {
  st->print(" bci=%d", _bci);
}

// ------------------------------------------------------------------
// ciReturnAddress::make
ciReturnAddress* ciReturnAddress::make(int bci) {
  GUARDED_VM_ENTRY(return CURRENT_ENV->get_return_address(bci);)
}
