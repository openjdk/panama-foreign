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
#include "classfile/javaClasses.inline.hpp"
#include "interpreter/interpreter.hpp"
#include "interpreter/interpreterRuntime.hpp"
#include "memory/allocation.inline.hpp"
#include "memory/resourceArea.hpp"
#include "include/jvm.h"
#include "prims/directUpcallHandler.hpp"
#include "runtime/javaCalls.hpp"
#include "runtime/interfaceSupport.inline.hpp"
#include "logging/log.hpp"
#include "logging/logStream.hpp"
#include "oops/arrayOop.inline.hpp"
#include "runtime/jniHandles.inline.hpp"

static struct SpecializedUpcallInfo {
  bool inited;
  Method* meth;
} specialized_upcall_info[5][5][3];

#define LONGS "JJJJJ"
#define DOUBLES "DDDDD"

static char decode_ret(int tag) {
   switch (tag) {
      case 0: return 'V';
      case 1: return 'J';
      case 2: return 'D';
   }
   return '\0';
}

#include "classfile/symbolTable.hpp"
// FIXME: This should be initialized explicitly instead of lazily/racily
static void specialized_upcall_init(int nlongs, int ndoubles, int rettag) {

  TRAPS = Thread::current();
  ResourceMark rm;
  stringStream desc, cname, mname;

  char ret = decode_ret(rettag);

  desc.print("(Ljdk/internal/foreign/abi/DirectUpcallHandler;%.*s%.*s)%c", nlongs, LONGS, ndoubles, DOUBLES, ret);
  if (nlongs + ndoubles == 0) {
    mname.print("invoke_%c_V", ret);
  } else {
    mname.print("invoke_%c_%.*s%.*s", ret, nlongs, LONGS, ndoubles, DOUBLES);
  }
  cname.print("jdk/internal/foreign/abi/DirectUpcallHandler");

  #if 0
    ::fprintf(stderr, "codes: %d, %d, %d\n", nlongs, ndoubles, rettag);
    ::fprintf(stderr, "mname: %s\n", mname.as_string());
    ::fprintf(stderr, "mdesc: %s\n", desc.as_string());
  #endif

  Symbol* cname_sym = SymbolTable::lookup(cname.as_string(), (int) cname.size(), THREAD);
  Symbol* mname_sym = SymbolTable::lookup(mname.as_string(), (int) mname.size(), THREAD);
  Symbol* mdesc_sym = SymbolTable::lookup(desc.as_string(), (int) desc.size(), THREAD);

  #if 0
    ::fprintf(stderr, "cname_sym: %p\n", cname_sym);
    ::fprintf(stderr, "mname_sym: %p\n", mname_sym);
    ::fprintf(stderr, "mdesc_sym: %p\n", mdesc_sym);
  #endif

  Klass* k = SystemDictionary::resolve_or_null(cname_sym, THREAD);
  #if 0
    ::fprintf(stderr, "Klass: %p\n", k);
  #endif

  Method* method = k->lookup_method(mname_sym, mdesc_sym);

  specialized_upcall_info[nlongs][ndoubles][rettag].meth = method;
  #if 0
    ::fprintf(stderr, "Method: %p\n", method);
  #endif
  specialized_upcall_info[nlongs][ndoubles][rettag].inited = true;
}

struct upcall_context {
  struct {
    uintptr_t rbx;
#ifdef _LP64
    uintptr_t r12;
    uintptr_t r13;
    uintptr_t r14;
    uintptr_t r15;
#endif
  } preserved;
};

#define ARG_MASK 0x000F
#define ARG_SHIFT 4

#define SPECIALIZED_HELPER_BODY(RES_T, RES_TAG) \
  int nlongs = (mask & ARG_MASK); \
  int ndoubles = ((mask >> ARG_SHIFT) & ARG_MASK); \
  int rettag = RES_TAG; \
\
  JavaCallArguments args((1 + nlongs + ndoubles) * 2); \
\
  args.push_jobject(rec); \
\
  long largs[] = { l0, l1, l2, l3 }; \
  double dargs[] = { d0, d1, d2, d3 }; \
\
  int i; \
  for (i = 0 ; i < nlongs ; i++) { \
    args.push_long(largs[i]); \
  } \
  for (int i = 0 ; i < ndoubles ; i++) { \
    args.push_double(dargs[i]); \
  } \
\
  Method* meth = specialized_upcall_info[nlongs][ndoubles][rettag].meth; \
\
  JavaThread* thread = JavaThread::current(); \
  ThreadInVMfromNative __tiv(thread); \
  JavaValue result(RES_T); \
  JavaCalls::call(&result, meth, &args, thread);


static long specialized_upcall_helper_J(long l0, long l1, long l2, long l3,
                                      double d0, double d1, double d2, double d3,
                                      unsigned int mask, jobject rec) {
  SPECIALIZED_HELPER_BODY(T_LONG, 1)
  return result.get_jlong();
}

static double specialized_upcall_helper_D(long l0, long l1, long l2, long l3,
                                      double d0, double d1, double d2, double d3,
                                      unsigned int mask, jobject rec) {
  SPECIALIZED_HELPER_BODY(T_DOUBLE, 2)
  return result.get_jdouble();
}

static void specialized_upcall_helper_V(long l0, long l1, long l2, long l3,
                                      double d0, double d1, double d2, double d3,
                                      unsigned int mask, jobject rec) {
  SPECIALIZED_HELPER_BODY(T_VOID, 0)
}

address DirectUpcallHandler::generate_specialized_upcall_stub(Handle& rec_handle, int nlongs, int ndoubles, int rettag) {
  ResourceMark rm;
  CodeBuffer buffer("upcall_stub", 1024, 1024);

  MacroAssembler* _masm = new MacroAssembler(&buffer);

  jobject rec = JNIHandles::make_weak_global(rec_handle);

#if 0
  fprintf(stderr, "generate_upcall_stub(%p)\n", rec);
#endif

  // stub code
  __ enter();

  // save pointer to JNI receiver handle into constant segment
  Address rec_adr = __ as_Address(InternalAddress(__ address_constant((address)rec)));

  //just save registers

  __ subptr(rsp, sizeof(struct upcall_context));
  __ andptr(rsp, -64);

  // Save preserved registers according to calling convention
  __ movptr(Address(rsp, offsetof(struct upcall_context, preserved.rbx)), rbx);
#ifdef _LP64
  __ movptr(Address(rsp, offsetof(struct upcall_context, preserved.r12)), r12);
  __ movptr(Address(rsp, offsetof(struct upcall_context, preserved.r13)), r13);
  __ movptr(Address(rsp, offsetof(struct upcall_context, preserved.r14)), r14);
  __ movptr(Address(rsp, offsetof(struct upcall_context, preserved.r15)), r15);
#endif

  if (!specialized_upcall_info[nlongs][ndoubles][rettag].inited) {
    specialized_upcall_init(nlongs, ndoubles, rettag);
  }

  int mask = (nlongs & ARG_MASK) |
              ((ndoubles & ARG_MASK) << ARG_SHIFT);

  // Call upcall helper
#ifdef _LP64 
#ifdef _WIN64
  // FIXME: Windows only allow 4 argument registers
#else
  __ movptr(c_rarg4, mask);
  __ movptr(c_rarg5, rec_adr);
#endif // _WIN64
#else
  //non LP64 cannot get to this stub
#endif

 address helper_addr = NULL;
 switch (rettag) {
    case 0: helper_addr = CAST_FROM_FN_PTR(address, specialized_upcall_helper_V); break;
    case 1: helper_addr = CAST_FROM_FN_PTR(address, specialized_upcall_helper_J); break;
    case 2: helper_addr = CAST_FROM_FN_PTR(address, specialized_upcall_helper_D); break;
 }
 __ call(RuntimeAddress(helper_addr));
#ifndef _LP64
  __ addptr(rsp, 8);
#endif

  // FIXME: More stuff stripped here

  // Restore preserved registers
#ifdef _LP64
  __ movptr(r12, Address(rsp, offsetof(struct upcall_context, preserved.r12)));
  __ movptr(r13, Address(rsp, offsetof(struct upcall_context, preserved.r13)));
  __ movptr(r14, Address(rsp, offsetof(struct upcall_context, preserved.r14)));
  __ movptr(r15, Address(rsp, offsetof(struct upcall_context, preserved.r15)));
#endif
  __ movptr(rbx, Address(rsp, offsetof(struct upcall_context, preserved.rbx)));

  // FIXME: More stuff stripped here

  __ leave();
  __ ret(0);

  _masm->flush();

  BufferBlob* blob = BufferBlob::create("upcall_stub", &buffer);

  return blob->code_begin();
}
