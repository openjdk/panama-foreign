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
 *
 */

#ifndef SHARE_UTILITIES_REGIONPTR_HPP
#define SHARE_UTILITIES_REGIONPTR_HPP

#include "utilities/debug.hpp"

#include <algorithm>

// A thin wrapper around a pointer + an element count
// Loosely modelled after std::span
template<class T>
class RegionPtr : StackObj {
  template<class X> friend bool operator==(const RegionPtr<X>& r1, const RegionPtr<X>& r2);
private:
  T* _ptr;
  size_t _element_count;
public:
  RegionPtr() : _ptr(NULL), _element_count(0) {}
  RegionPtr(T* ptr, size_t element_count) : _ptr(ptr), _element_count(element_count) {}

  T& front() { return (*this)[0]; }
  T& back() { return (*this)[_element_count - 1]; }
  T& operator[](size_t idx) {
    assert(idx < _element_count, "OOB access!");
    return _ptr[idx];
  }

  // returns a literal pointer to the data
  T* data() { return _ptr; }

  // returns an iterator pointing at the first element
  T* begin() { return _ptr; }
  T* end() { return _ptr + _element_count; }

  bool deep_equals(const RegionPtr& other) const {
    if (_element_count != other._element_count) {
      return false;
    }
    return std::equal(begin(), end(), other.begin());
  }
  bool contains(const RegionPtr<T>& rp2) const {
    return begin() <= rp2.begin() && end() >= rp2.end();
  }
  bool contains(const void* addr) const {
    return addr >= (void*)begin() && addr < (void*)end();
  }

  size_t byte_size() const { return _element_count * sizeof(T); }
  size_t element_count() const { return _element_count; }
  bool is_empty() const { return _element_count == 0; }
};

template<class T>
inline bool operator==(const RegionPtr<T>& r1, const RegionPtr<T>& r2) {
  return r1._ptr == r2._ptr && r1._element_count == r2._element_count;
}

template<class T>
inline bool operator!=(const RegionPtr<T>& r1, const RegionPtr<T>& r2) {
  return !(r1 == r2);
}

#endif // SHARE_UTILITIES_REGIONPTR_HPP
