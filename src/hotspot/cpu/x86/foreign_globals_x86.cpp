/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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

#include "precompiled.hpp"
#include "asm/macroAssembler.hpp"
#include CPU_HEADER(foreign_globals)

#ifdef _LP64

Register integer_argument_registers[INTEGER_ARGUMENT_REGISTERS_NOOF] = {
  c_rarg0, c_rarg1, c_rarg2, c_rarg3,
#ifndef _WIN64
  c_rarg4, c_rarg5
#endif
};

Register integer_return_registers[INTEGER_RETURN_REGISTERS_NOOF] = {
  rax,
#ifndef _WIN64
  rdx
#endif
};

XMMRegister vector_argument_registers[VECTOR_ARGUMENT_REGISTERS_NOOF] = {
  c_farg0, c_farg1, c_farg2, c_farg3,
#ifndef _WIN64
  c_farg4, c_farg5, c_farg6, c_farg7
#endif
};

XMMRegister vector_return_registers[VECTOR_RETURN_REGISTERS_NOOF] = {
  xmm0,
#ifndef _WIN64
  xmm1
#endif
};

#else

static Register integer_return_registers[INTEGER_RETURN_REGISTERS_NOOF] = {
  rax
};

static XMMRegister vector_return_registers[VECTOR_RETURN_REGISTERS_NOOF] = {
  xmm0
};

#endif
