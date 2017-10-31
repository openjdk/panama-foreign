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

bool ciType::is_float128vector() {
  return basic_type() == T_OBJECT && (as_klass()->name()->sid() == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Float128Vector) ||
  as_klass()->name()->sid() == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Float128Vector_Float128Species));
}

bool ciType::is_float256vector() {
  return basic_type() == T_OBJECT && (as_klass()->name()->sid() == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Float256Vector) ||
  as_klass()->name()->sid() == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Float256Vector_Float256Species));
}

bool ciType::is_float512vector() {
  return basic_type() == T_OBJECT && (as_klass()->name()->sid() == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Float512Vector) ||
  as_klass()->name()->sid() == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Float512Vector_Float512Species));
}

bool ciType::is_int128vector() {
  return basic_type() == T_OBJECT && (as_klass()->name()->sid() == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Int128Vector) ||
  as_klass()->name()->sid() == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Int128Vector_Int128Species));
}

bool ciType::is_int256vector() {
  return basic_type() == T_OBJECT && (as_klass()->name()->sid() == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Int256Vector) ||
  as_klass()->name()->sid() == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Int256Vector_Int256Species));
}

bool ciType::is_int512vector() {
  return basic_type() == T_OBJECT && (as_klass()->name()->sid() == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Int512Vector) ||
  as_klass()->name()->sid() == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Int512Vector_Int512Species));
}

bool ciType::is_double128vector() {
  return basic_type() == T_OBJECT && (as_klass()->name()->sid() == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Double128Vector) ||
  as_klass()->name()->sid() == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Double128Vector_Double128Species));
}

bool ciType::is_double256vector() {
  return basic_type() == T_OBJECT && (as_klass()->name()->sid() == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Double256Vector) ||
  as_klass()->name()->sid() == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Double256Vector_Double256Species));
}

bool ciType::is_double512vector() {
  return basic_type() == T_OBJECT && (as_klass()->name()->sid() == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Double512Vector) ||
  as_klass()->name()->sid() == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_Double512Vector_Double512Species));
}

bool ciType::is_vectormask() {
  return basic_type() == T_OBJECT && (as_klass()->name()->sid() == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_VectorMask) ||
  as_klass()->name()->sid() == vmSymbols::VM_SYMBOL_ENUM_NAME(jdk_incubator_vector_VectorGenericMask));
}

bool ciType::is_vectorapi_vector() {
  return is_float128vector() || is_float256vector() || is_float512vector() ||
      is_double128vector() || is_double256vector() || is_double512vector() ||
      is_int128vector() || is_int256vector() || is_int512vector() ||
      is_vectormask();
}

int ciType::vectorapi_vector_size() {
  if ( is_double128vector() ) return 2;
  if ( is_float128vector() || is_int128vector() || is_double256vector() ) return 4;
  if ( is_float256vector() || is_int256vector() || is_double512vector() ) return 8;
  if ( is_float512vector() || is_int512vector()) return 16;
  return -1;
}

BasicType ciType::vectorapi_vector_bt() {
  if ( is_vectormask() ) return T_BOOLEAN;
  if ( is_float128vector() || is_float256vector() || is_float512vector() ) return T_FLOAT;
  if ( is_double128vector() || is_double256vector() || is_double512vector() ) return T_DOUBLE;
  if ( is_int128vector() || is_int256vector() || is_int512vector() ) return T_INT;
  return T_VOID;
}

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
