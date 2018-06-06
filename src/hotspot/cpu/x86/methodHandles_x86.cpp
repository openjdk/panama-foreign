/*
 * Copyright (c) 1997, 2018, Oracle and/or its affiliates. All rights reserved.
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
#include "jvm.h"
#include "asm/macroAssembler.hpp"
#include "classfile/javaClasses.inline.hpp"
#include "interpreter/interpreter.hpp"
#include "interpreter/interpreterRuntime.hpp"
#include "memory/allocation.inline.hpp"
#include "oops/arrayOop.inline.hpp"
#include "memory/resourceArea.hpp"
#include "prims/methodHandles.hpp"
#include "runtime/interfaceSupport.inline.hpp"
#include "runtime/javaCalls.hpp"
#include "logging/log.hpp"
#include "logging/logStream.hpp"
#include "runtime/flags/flagSetting.hpp"
#include "runtime/frame.inline.hpp"
#include "utilities/preserveException.hpp"

#define __ _masm->

#ifdef PRODUCT
#define BLOCK_COMMENT(str) /* nothing */
#define STOP(error) stop(error)
#else
#define BLOCK_COMMENT(str) __ block_comment(str)
#define STOP(error) block_comment(error); __ stop(error)
#endif

#define BIND(label) bind(label); BLOCK_COMMENT(#label ":")

void MethodHandles::load_klass_from_Class(MacroAssembler* _masm, Register klass_reg) {
  if (VerifyMethodHandles)
    verify_klass(_masm, klass_reg, SystemDictionary::WK_KLASS_ENUM_NAME(java_lang_Class),
                 "MH argument is a Class");
  __ movptr(klass_reg, Address(klass_reg, java_lang_Class::klass_offset_in_bytes()));
}

#ifdef ASSERT
static int check_nonzero(const char* xname, int x) {
  assert(x != 0, "%s should be nonzero", xname);
  return x;
}
#define NONZERO(x) check_nonzero(#x, x)
#else //ASSERT
#define NONZERO(x) (x)
#endif //ASSERT

#ifdef ASSERT
void MethodHandles::verify_klass(MacroAssembler* _masm,
                                 Register obj, SystemDictionary::WKID klass_id,
                                 const char* error_message) {
  InstanceKlass** klass_addr = SystemDictionary::well_known_klass_addr(klass_id);
  Klass* klass = SystemDictionary::well_known_klass(klass_id);
  Register temp = rdi;
  Register temp2 = noreg;
  LP64_ONLY(temp2 = rscratch1);  // used by MacroAssembler::cmpptr
  Label L_ok, L_bad;
  BLOCK_COMMENT("verify_klass {");
  __ verify_oop(obj);
  __ testptr(obj, obj);
  __ jcc(Assembler::zero, L_bad);
  __ push(temp); if (temp2 != noreg)  __ push(temp2);
#define UNPUSH { if (temp2 != noreg)  __ pop(temp2);  __ pop(temp); }
  __ load_klass(temp, obj);
  __ cmpptr(temp, ExternalAddress((address) klass_addr));
  __ jcc(Assembler::equal, L_ok);
  intptr_t super_check_offset = klass->super_check_offset();
  __ movptr(temp, Address(temp, super_check_offset));
  __ cmpptr(temp, ExternalAddress((address) klass_addr));
  __ jcc(Assembler::equal, L_ok);
  UNPUSH;
  __ bind(L_bad);
  __ STOP(error_message);
  __ BIND(L_ok);
  UNPUSH;
  BLOCK_COMMENT("} verify_klass");
}

void MethodHandles::verify_ref_kind(MacroAssembler* _masm, int ref_kind, Register member_reg, Register temp) {
  Label L;
  BLOCK_COMMENT("verify_ref_kind {");
  __ movl(temp, Address(member_reg, NONZERO(java_lang_invoke_MemberName::flags_offset_in_bytes())));
  __ shrl(temp, java_lang_invoke_MemberName::MN_REFERENCE_KIND_SHIFT);
  __ andl(temp, java_lang_invoke_MemberName::MN_REFERENCE_KIND_MASK);
  __ cmpl(temp, ref_kind);
  __ jcc(Assembler::equal, L);
  { char* buf = NEW_C_HEAP_ARRAY(char, 100, mtInternal);
    jio_snprintf(buf, 100, "verify_ref_kind expected %x", ref_kind);
    if (ref_kind == JVM_REF_invokeVirtual ||
        ref_kind == JVM_REF_invokeSpecial)
      // could do this for all ref_kinds, but would explode assembly code size
      trace_method_handle(_masm, buf);
    __ STOP(buf);
  }
  BLOCK_COMMENT("} verify_ref_kind");
  __ bind(L);
}

#endif //ASSERT

void MethodHandles::jump_from_method_handle(MacroAssembler* _masm, Register method, Register temp,
                                            bool for_compiler_entry) {
  assert(method == rbx, "interpreter calling convention");

   Label L_no_such_method;
   __ testptr(rbx, rbx);
   __ jcc(Assembler::zero, L_no_such_method);

  __ verify_method_ptr(method);

  if (!for_compiler_entry && JvmtiExport::can_post_interpreter_events()) {
    Label run_compiled_code;
    // JVMTI events, such as single-stepping, are implemented partly by avoiding running
    // compiled code in threads for which the event is enabled.  Check here for
    // interp_only_mode if these events CAN be enabled.
#ifdef _LP64
    Register rthread = r15_thread;
#else
    Register rthread = temp;
    __ get_thread(rthread);
#endif
    // interp_only is an int, on little endian it is sufficient to test the byte only
    // Is a cmpl faster?
    __ cmpb(Address(rthread, JavaThread::interp_only_mode_offset()), 0);
    __ jccb(Assembler::zero, run_compiled_code);
    __ jmp(Address(method, Method::interpreter_entry_offset()));
    __ BIND(run_compiled_code);
  }

  const ByteSize entry_offset = for_compiler_entry ? Method::from_compiled_offset() :
                                                     Method::from_interpreted_offset();
  __ jmp(Address(method, entry_offset));

  __ bind(L_no_such_method);
  __ jump(RuntimeAddress(StubRoutines::throw_AbstractMethodError_entry()));
}

void MethodHandles::jump_to_lambda_form(MacroAssembler* _masm,
                                        Register recv, Register method_temp,
                                        Register temp2,
                                        bool for_compiler_entry) {
  BLOCK_COMMENT("jump_to_lambda_form {");
  // This is the initial entry point of a lazy method handle.
  // After type checking, it picks up the invoker from the LambdaForm.
  assert_different_registers(recv, method_temp, temp2);
  assert(recv != noreg, "required register");
  assert(method_temp == rbx, "required register for loading method");

  //NOT_PRODUCT({ FlagSetting fs(TraceMethodHandles, true); trace_method_handle(_masm, "LZMH"); });

  // Load the invoker, as MH -> MH.form -> LF.vmentry
  __ verify_oop(recv);
  __ load_heap_oop(method_temp, Address(recv, NONZERO(java_lang_invoke_MethodHandle::form_offset_in_bytes())), temp2);
  __ verify_oop(method_temp);
  __ load_heap_oop(method_temp, Address(method_temp, NONZERO(java_lang_invoke_LambdaForm::vmentry_offset_in_bytes())), temp2);
  __ verify_oop(method_temp);
  __ load_heap_oop(method_temp, Address(method_temp, NONZERO(java_lang_invoke_MemberName::method_offset_in_bytes())), temp2);
  __ verify_oop(method_temp);
  __ access_load_at(T_ADDRESS, IN_HEAP, method_temp,
                    Address(method_temp, NONZERO(java_lang_invoke_ResolvedMethodName::vmtarget_offset_in_bytes())),
                    noreg, noreg);

  if (VerifyMethodHandles && !for_compiler_entry) {
    // make sure recv is already on stack
    __ movptr(temp2, Address(method_temp, Method::const_offset()));
    __ load_sized_value(temp2,
                        Address(temp2, ConstMethod::size_of_parameters_offset()),
                        sizeof(u2), /*is_signed*/ false);
    // assert(sizeof(u2) == sizeof(Method::_size_of_parameters), "");
    Label L;
    __ cmpoop(recv, __ argument_address(temp2, -1));
    __ jcc(Assembler::equal, L);
    __ movptr(rax, __ argument_address(temp2, -1));
    __ STOP("receiver not on stack");
    __ BIND(L);
  }

  jump_from_method_handle(_masm, method_temp, temp2, for_compiler_entry);
  BLOCK_COMMENT("} jump_to_lambda_form");
}


// Code generation
address MethodHandles::generate_method_handle_interpreter_entry(MacroAssembler* _masm,
                                                                vmIntrinsics::ID iid) {
  const bool not_for_compiler_entry = false;  // this is the interpreter entry
  assert(is_signature_polymorphic(iid), "expected invoke iid");
  if (iid == vmIntrinsics::_invokeGeneric ||
      iid == vmIntrinsics::_compiledLambdaForm) {
    // Perhaps surprisingly, the symbolic references visible to Java are not directly used.
    // They are linked to Java-generated adapters via MethodHandleNatives.linkMethod.
    // They all allow an appendix argument.
    __ hlt();           // empty stubs make SG sick
    return NULL;
  }
  // No need in interpreter entry for linkToNative for now.
  // Interpreter calls compiled entry through i2c.
  if (iid == vmIntrinsics::_linkToNative) {
    __ hlt();
    return NULL;
  }

  // rsi/r13: sender SP (must preserve; see prepare_to_jump_from_interpreted)
  // rbx: Method*
  // rdx: argument locator (parameter slot count, added to rsp)
  // rcx: used as temp to hold mh or receiver
  // rax, rdi: garbage temps, blown away
  Register rdx_argp   = rdx;   // argument list ptr, live on error paths
  Register rax_temp   = rax;
  Register rcx_mh     = rcx;   // MH receiver; dies quickly and is recycled
  Register rbx_method = rbx;   // eventual target of this invocation

  // here's where control starts out:
  __ align(CodeEntryAlignment);
  address entry_point = __ pc();

  if (VerifyMethodHandles) {
    assert(Method::intrinsic_id_size_in_bytes() == 2, "assuming Method::_intrinsic_id is u2");

    Label L;
    BLOCK_COMMENT("verify_intrinsic_id {");
    __ cmpw(Address(rbx_method, Method::intrinsic_id_offset_in_bytes()), (int) iid);
    __ jcc(Assembler::equal, L);
    if (iid == vmIntrinsics::_linkToVirtual ||
        iid == vmIntrinsics::_linkToSpecial) {
      // could do this for all kinds, but would explode assembly code size
      trace_method_handle(_masm, "bad Method*::intrinsic_id");
    }
    __ STOP("bad Method*::intrinsic_id");
    __ bind(L);
    BLOCK_COMMENT("} verify_intrinsic_id");
  }

  // First task:  Find out how big the argument list is.
  Address rdx_first_arg_addr;
  int ref_kind = signature_polymorphic_intrinsic_ref_kind(iid);
  assert(ref_kind != 0 || iid == vmIntrinsics::_invokeBasic, "must be _invokeBasic or a linkTo intrinsic");
  if (ref_kind == 0 || MethodHandles::ref_kind_has_receiver(ref_kind)) {
    __ movptr(rdx_argp, Address(rbx_method, Method::const_offset()));
    __ load_sized_value(rdx_argp,
                        Address(rdx_argp, ConstMethod::size_of_parameters_offset()),
                        sizeof(u2), /*is_signed*/ false);
    // assert(sizeof(u2) == sizeof(Method::_size_of_parameters), "");
    rdx_first_arg_addr = __ argument_address(rdx_argp, -1);
  } else {
    DEBUG_ONLY(rdx_argp = noreg);
  }

  if (!is_signature_polymorphic_static(iid)) {
    __ movptr(rcx_mh, rdx_first_arg_addr);
    DEBUG_ONLY(rdx_argp = noreg);
  }

  // rdx_first_arg_addr is live!

  trace_method_handle_interpreter_entry(_masm, iid);

  if (iid == vmIntrinsics::_invokeBasic) {
    generate_method_handle_dispatch(_masm, iid, rcx_mh, noreg, not_for_compiler_entry);

  } else {
    // Adjust argument list by popping the trailing MemberName argument.
    Register rcx_recv = noreg;
    if (MethodHandles::ref_kind_has_receiver(ref_kind)) {
      // Load the receiver (not the MH; the actual MemberName's receiver) up from the interpreter stack.
      __ movptr(rcx_recv = rcx, rdx_first_arg_addr);
    }
    DEBUG_ONLY(rdx_argp = noreg);
    Register rbx_member = rbx_method;  // MemberName ptr; incoming method ptr is dead now
    __ pop(rax_temp);           // return address
    __ pop(rbx_member);         // extract last argument
    __ push(rax_temp);          // re-push return address
    generate_method_handle_dispatch(_masm, iid, rcx_recv, rbx_member, not_for_compiler_entry);
  }

  return entry_point;
}

void MethodHandles::generate_method_handle_dispatch(MacroAssembler* _masm,
                                                    vmIntrinsics::ID iid,
                                                    Register receiver_reg,
                                                    Register member_reg,
                                                    bool for_compiler_entry) {
  assert(is_signature_polymorphic(iid), "expected invoke iid");
  Register rbx_method = rbx;   // eventual target of this invocation
  // temps used in this code are not used in *either* compiled or interpreted calling sequences
#ifdef _LP64
  Register temp1 = rscratch1;
  Register temp2 = rscratch2;
  Register temp3 = rax;
  if (for_compiler_entry) {
    assert(receiver_reg == (iid == vmIntrinsics::_linkToStatic ? noreg : j_rarg0), "only valid assignment");
    assert_different_registers(temp1,        j_rarg0, j_rarg1, j_rarg2, j_rarg3, j_rarg4, j_rarg5);
    assert_different_registers(temp2,        j_rarg0, j_rarg1, j_rarg2, j_rarg3, j_rarg4, j_rarg5);
    assert_different_registers(temp3,        j_rarg0, j_rarg1, j_rarg2, j_rarg3, j_rarg4, j_rarg5);
  }
#else
  Register temp1 = (for_compiler_entry ? rsi : rdx);
  Register temp2 = rdi;
  Register temp3 = rax;
  if (for_compiler_entry) {
    assert(receiver_reg == (iid == vmIntrinsics::_linkToStatic ? noreg : rcx), "only valid assignment");
    assert_different_registers(temp1,        rcx, rdx);
    assert_different_registers(temp2,        rcx, rdx);
    assert_different_registers(temp3,        rcx, rdx);
  }
#endif
  else {
    assert_different_registers(temp1, temp2, temp3, saved_last_sp_register());  // don't trash lastSP
  }
  assert_different_registers(temp1, temp2, temp3, receiver_reg);
  assert_different_registers(temp1, temp2, temp3, member_reg);

  if (iid == vmIntrinsics::_invokeBasic) {
    // indirect through MH.form.vmentry.vmtarget
    jump_to_lambda_form(_masm, receiver_reg, rbx_method, temp1, for_compiler_entry);

  } else {
    // The method is a member invoker used by direct method handles.
    if (VerifyMethodHandles) {
      // make sure the trailing argument really is a MemberName (caller responsibility)
      verify_klass(_masm, member_reg, SystemDictionary::WK_KLASS_ENUM_NAME(java_lang_invoke_MemberName),
                   "MemberName required for invokeVirtual etc.");
    }

    Address member_clazz(    member_reg, NONZERO(java_lang_invoke_MemberName::clazz_offset_in_bytes()));
    Address member_vmindex(  member_reg, NONZERO(java_lang_invoke_MemberName::vmindex_offset_in_bytes()));
    Address member_vmtarget( member_reg, NONZERO(java_lang_invoke_MemberName::method_offset_in_bytes()));
    Address vmtarget_method( rbx_method, NONZERO(java_lang_invoke_ResolvedMethodName::vmtarget_offset_in_bytes()));

    Register temp1_recv_klass = temp1;
    if (iid != vmIntrinsics::_linkToStatic) {
      __ verify_oop(receiver_reg);
      if (iid == vmIntrinsics::_linkToSpecial) {
        // Don't actually load the klass; just null-check the receiver.
        __ null_check(receiver_reg);
      } else {
        // load receiver klass itself
        __ null_check(receiver_reg, oopDesc::klass_offset_in_bytes());
        __ load_klass(temp1_recv_klass, receiver_reg);
        __ verify_klass_ptr(temp1_recv_klass);
      }
      BLOCK_COMMENT("check_receiver {");
      // The receiver for the MemberName must be in receiver_reg.
      // Check the receiver against the MemberName.clazz
      if (VerifyMethodHandles && iid == vmIntrinsics::_linkToSpecial) {
        // Did not load it above...
        __ load_klass(temp1_recv_klass, receiver_reg);
        __ verify_klass_ptr(temp1_recv_klass);
      }
      if (VerifyMethodHandles && iid != vmIntrinsics::_linkToInterface) {
        Label L_ok;
        Register temp2_defc = temp2;
        __ load_heap_oop(temp2_defc, member_clazz, temp3);
        load_klass_from_Class(_masm, temp2_defc);
        __ verify_klass_ptr(temp2_defc);
        __ check_klass_subtype(temp1_recv_klass, temp2_defc, temp3, L_ok);
        // If we get here, the type check failed!
        __ STOP("receiver class disagrees with MemberName.clazz");
        __ bind(L_ok);
      }
      BLOCK_COMMENT("} check_receiver");
    }
    if (iid == vmIntrinsics::_linkToSpecial ||
        iid == vmIntrinsics::_linkToStatic) {
      DEBUG_ONLY(temp1_recv_klass = noreg);  // these guys didn't load the recv_klass
    }

    // Live registers at this point:
    //  member_reg - MemberName that was the trailing argument
    //  temp1_recv_klass - klass of stacked receiver, if needed
    //  rsi/r13 - interpreter linkage (if interpreted)
    //  rcx, rdx, rsi, rdi, r8 - compiler arguments (if compiled)

    Label L_incompatible_class_change_error;
    switch (iid) {
    case vmIntrinsics::_linkToSpecial:
      if (VerifyMethodHandles) {
        verify_ref_kind(_masm, JVM_REF_invokeSpecial, member_reg, temp3);
      }
      __ load_heap_oop(rbx_method, member_vmtarget);
      __ access_load_at(T_ADDRESS, IN_HEAP, rbx_method, vmtarget_method, noreg, noreg);
      break;

    case vmIntrinsics::_linkToStatic:
      if (VerifyMethodHandles) {
        verify_ref_kind(_masm, JVM_REF_invokeStatic, member_reg, temp3);
      }
      __ load_heap_oop(rbx_method, member_vmtarget);
      __ access_load_at(T_ADDRESS, IN_HEAP, rbx_method, vmtarget_method, noreg, noreg);
      break;

    case vmIntrinsics::_linkToVirtual:
    {
      // same as TemplateTable::invokevirtual,
      // minus the CP setup and profiling:

      if (VerifyMethodHandles) {
        verify_ref_kind(_masm, JVM_REF_invokeVirtual, member_reg, temp3);
      }

      // pick out the vtable index from the MemberName, and then we can discard it:
      Register temp2_index = temp2;
      __ access_load_at(T_ADDRESS, IN_HEAP, temp2_index, member_vmindex, noreg, noreg);

      if (VerifyMethodHandles) {
        Label L_index_ok;
        __ cmpl(temp2_index, 0);
        __ jcc(Assembler::greaterEqual, L_index_ok);
        __ STOP("no virtual index");
        __ BIND(L_index_ok);
      }

      // Note:  The verifier invariants allow us to ignore MemberName.clazz and vmtarget
      // at this point.  And VerifyMethodHandles has already checked clazz, if needed.

      // get target Method* & entry point
      __ lookup_virtual_method(temp1_recv_klass, temp2_index, rbx_method);
      break;
    }

    case vmIntrinsics::_linkToInterface:
    {
      // same as TemplateTable::invokeinterface
      // (minus the CP setup and profiling, with different argument motion)
      if (VerifyMethodHandles) {
        verify_ref_kind(_masm, JVM_REF_invokeInterface, member_reg, temp3);
      }

      BarrierSetAssembler* bs = BarrierSet::barrier_set()->barrier_set_assembler();

      Register temp3_intf = temp3;
      __ load_heap_oop(temp3_intf, member_clazz);
      load_klass_from_Class(_masm, temp3_intf);
      __ verify_klass_ptr(temp3_intf);

      Register rbx_index = rbx_method;
      __ access_load_at(T_ADDRESS, IN_HEAP, rbx_index, member_vmindex, noreg, noreg);
      if (VerifyMethodHandles) {
        Label L;
        __ cmpl(rbx_index, 0);
        __ jcc(Assembler::greaterEqual, L);
        __ STOP("invalid vtable index for MH.invokeInterface");
        __ bind(L);
      }

      // given intf, index, and recv klass, dispatch to the implementation method
      __ lookup_interface_method(temp1_recv_klass, temp3_intf,
                                 // note: next two args must be the same:
                                 rbx_index, rbx_method,
                                 temp2,
                                 L_incompatible_class_change_error);
      break;
    }

    default:
      fatal("unexpected intrinsic %d: %s", iid, vmIntrinsics::name_at(iid));
      break;
    }

    // Live at this point:
    //   rbx_method
    //   rsi/r13 (if interpreted)

    // After figuring out which concrete method to call, jump into it.
    // Note that this works in the interpreter with no data motion.
    // But the compiled version will require that rcx_recv be shifted out.
    __ verify_method_ptr(rbx_method);
    jump_from_method_handle(_masm, rbx_method, temp1, for_compiler_entry);

    if (iid == vmIntrinsics::_linkToInterface) {
      __ bind(L_incompatible_class_change_error);
      __ jump(RuntimeAddress(StubRoutines::throw_IncompatibleClassChangeError_entry()));
    }
  }
}

#ifndef PRODUCT
void trace_method_handle_stub(const char* adaptername,
                              oop mh,
                              intptr_t* saved_regs,
                              intptr_t* entry_sp) {
  // called as a leaf from native code: do not block the JVM!
  bool has_mh = (strstr(adaptername, "/static") == NULL &&
                 strstr(adaptername, "linkTo") == NULL);    // static linkers don't have MH
  const char* mh_reg_name = has_mh ? "rcx_mh" : "rcx";
  tty->print_cr("MH %s %s=" PTR_FORMAT " sp=" PTR_FORMAT,
                adaptername, mh_reg_name,
                p2i(mh), p2i(entry_sp));

  if (Verbose) {
    tty->print_cr("Registers:");
    const int saved_regs_count = RegisterImpl::number_of_registers;
    for (int i = 0; i < saved_regs_count; i++) {
      Register r = as_Register(i);
      // The registers are stored in reverse order on the stack (by pusha).
      tty->print("%3s=" PTR_FORMAT, r->name(), saved_regs[((saved_regs_count - 1) - i)]);
      if ((i + 1) % 4 == 0) {
        tty->cr();
      } else {
        tty->print(", ");
      }
    }
    tty->cr();

    {
     // dumping last frame with frame::describe

      JavaThread* p = JavaThread::active();

      ResourceMark rm;
      PRESERVE_EXCEPTION_MARK; // may not be needed by safer and unexpensive here
      FrameValues values;

      // Note: We want to allow trace_method_handle from any call site.
      // While trace_method_handle creates a frame, it may be entered
      // without a PC on the stack top (e.g. not just after a call).
      // Walking that frame could lead to failures due to that invalid PC.
      // => carefully detect that frame when doing the stack walking

      // Current C frame
      frame cur_frame = os::current_frame();

      // Robust search of trace_calling_frame (independant of inlining).
      // Assumes saved_regs comes from a pusha in the trace_calling_frame.
      assert(cur_frame.sp() < saved_regs, "registers not saved on stack ?");
      frame trace_calling_frame = os::get_sender_for_C_frame(&cur_frame);
      while (trace_calling_frame.fp() < saved_regs) {
        trace_calling_frame = os::get_sender_for_C_frame(&trace_calling_frame);
      }

      // safely create a frame and call frame::describe
      intptr_t *dump_sp = trace_calling_frame.sender_sp();
      intptr_t *dump_fp = trace_calling_frame.link();

      bool walkable = has_mh; // whether the traced frame shoud be walkable

      if (walkable) {
        // The previous definition of walkable may have to be refined
        // if new call sites cause the next frame constructor to start
        // failing. Alternatively, frame constructors could be
        // modified to support the current or future non walkable
        // frames (but this is more intrusive and is not considered as
        // part of this RFE, which will instead use a simpler output).
        frame dump_frame = frame(dump_sp, dump_fp);
        dump_frame.describe(values, 1);
      } else {
        // Stack may not be walkable (invalid PC above FP):
        // Add descriptions without building a Java frame to avoid issues
        values.describe(-1, dump_fp, "fp for #1 <not parsed, cannot trust pc>");
        values.describe(-1, dump_sp, "sp for #1");
      }
      values.describe(-1, entry_sp, "raw top of stack");

      tty->print_cr("Stack layout:");
      values.print(p);
    }
    if (has_mh && oopDesc::is_oop(mh)) {
      mh->print();
      if (java_lang_invoke_MethodHandle::is_instance(mh)) {
        if (java_lang_invoke_MethodHandle::form_offset_in_bytes() != 0)
          java_lang_invoke_MethodHandle::form(mh)->print();
      }
    }
  }
}

// The stub wraps the arguments in a struct on the stack to avoid
// dealing with the different calling conventions for passing 6
// arguments.
struct MethodHandleStubArguments {
  const char* adaptername;
  oopDesc* mh;
  intptr_t* saved_regs;
  intptr_t* entry_sp;
};
void trace_method_handle_stub_wrapper(MethodHandleStubArguments* args) {
  trace_method_handle_stub(args->adaptername,
                           args->mh,
                           args->saved_regs,
                           args->entry_sp);
}

void MethodHandles::trace_method_handle(MacroAssembler* _masm, const char* adaptername) {
  if (!TraceMethodHandles)  return;
  BLOCK_COMMENT(err_msg("trace_method_handle %s {", adaptername));
  __ enter();
  __ andptr(rsp, -16); // align stack if needed for FPU state
  __ pusha();
  __ mov(rbx, rsp); // for retreiving saved_regs
  // Note: saved_regs must be in the entered frame for the
  // robust stack walking implemented in trace_method_handle_stub.

  // save FP result, valid at some call sites (adapter_opt_return_float, ...)
  __ increment(rsp, -2 * wordSize);
  if  (UseSSE >= 2) {
    __ movdbl(Address(rsp, 0), xmm0);
  } else if (UseSSE == 1) {
    __ movflt(Address(rsp, 0), xmm0);
  } else {
    __ fst_d(Address(rsp, 0));
  }

  // Incoming state:
  // rcx: method handle
  //
  // To avoid calling convention issues, build a record on the stack
  // and pass the pointer to that instead.
  __ push(rbp);               // entry_sp (with extra align space)
  __ push(rbx);               // pusha saved_regs
  __ push(rcx);               // mh
  __ push(rcx);               // slot for adaptername
  __ movptr(Address(rsp, 0), (intptr_t) adaptername);
  __ super_call_VM_leaf(CAST_FROM_FN_PTR(address, trace_method_handle_stub_wrapper), rsp);
  __ increment(rsp, sizeof(MethodHandleStubArguments));

  if  (UseSSE >= 2) {
    __ movdbl(xmm0, Address(rsp, 0));
  } else if (UseSSE == 1) {
    __ movflt(xmm0, Address(rsp, 0));
  } else {
    __ fld_d(Address(rsp, 0));
  }
  __ increment(rsp, 2 * wordSize);

  __ popa();
  __ leave();
  BLOCK_COMMENT("} trace_method_handle");
}
#endif //PRODUCT




//// shuffle recipe based (mikael style) stub ////

//#define DEBUG_OPS
//#define DEBUG_CALLS

typedef void (*InvokeNativeStub)(struct ShuffleDowncallContext* ctxt);

#ifdef _LP64

static const size_t INTEGER_ARGUMENT_REGISTERS_NOOF = Argument::n_int_register_parameters_c;
static const size_t INTEGER_RETURN_REGISTERS_NOOF = 2;
static const size_t VECTOR_ARGUMENT_REGISTERS_NOOF = Argument::n_float_register_parameters_c;
static const size_t VECTOR_RETURN_REGISTERS_NOOF = 2;

static Register integer_argument_registers[INTEGER_ARGUMENT_REGISTERS_NOOF] = {
  c_rarg0, c_rarg1, c_rarg2, c_rarg3,
#ifndef _WIN64
  c_rarg4, c_rarg5
#endif
};

static Register integer_return_registers[INTEGER_RETURN_REGISTERS_NOOF] = {
  rax,
#ifndef _WIN64
  rdx
#endif
};

static XMMRegister vector_argument_registers[VECTOR_ARGUMENT_REGISTERS_NOOF] = {
  c_farg0, c_farg1, c_farg2, c_farg3,
#ifndef _WIN64
  c_farg4, c_farg5, c_farg6, c_farg7
#endif
};

static XMMRegister vector_return_registers[VECTOR_RETURN_REGISTERS_NOOF] = {
  xmm0,
#ifndef _WIN64
  xmm1
#endif
};

#else // _LP64

static const size_t INTEGER_ARGUMENT_REGISTERS_NOOF = 0;
static const size_t INTEGER_RETURN_REGISTERS_NOOF = 1;
static const size_t VECTOR_ARGUMENT_REGISTERS_NOOF = 0;
static const size_t VECTOR_RETURN_REGISTERS_NOOF = 1;

static Register integer_return_registers[INTEGER_RETURN_REGISTERS_NOOF] = {
  rax
};

static XMMRegister vector_return_registers[VECTOR_RETURN_REGISTERS_NOOF] = {
  xmm0
};

#endif // _LP64

struct VectorRegister {
  static const size_t VECTOR_MAX_WIDTH_BITS = 512; // AVX-512 (64-byte) vector types
  static const size_t VECTOR_MAX_WIDTH_BYTES = VECTOR_MAX_WIDTH_BITS / 8;
  static const size_t VECTOR_MAX_WIDTH_U64S = VECTOR_MAX_WIDTH_BITS / 64;
  static const size_t VECTOR_MAX_WIDTH_FLOATS = VECTOR_MAX_WIDTH_BITS / 32;
  static const size_t VECTOR_MAX_WIDTH_DOUBLES = VECTOR_MAX_WIDTH_BITS / 64;

  union {
    uint8_t bits[VECTOR_MAX_WIDTH_BYTES];
    uint64_t u64[VECTOR_MAX_WIDTH_U64S];
    float f[VECTOR_MAX_WIDTH_FLOATS];
    double d[VECTOR_MAX_WIDTH_DOUBLES];
  };
};

enum ShuffleRecipeStorageClass {
  CLASS_BUF,
  CLASS_FIRST = CLASS_BUF,
  CLASS_STACK,
  CLASS_VECTOR,
  CLASS_INTEGER,
  CLASS_LAST = CLASS_INTEGER,
  CLASS_NOOF
};

static ShuffleRecipeStorageClass index2storage_class[CLASS_NOOF + 1] = {
  CLASS_BUF, CLASS_STACK, CLASS_VECTOR, CLASS_INTEGER, CLASS_NOOF
};

static const char* index2storage_class_name[CLASS_NOOF] = {
  "CLASS_BUF", "CLASS_STACK", "CLASS_VECTOR", "CLASS_INTEGER"
};

static ShuffleRecipeStorageClass next_storage_class(ShuffleRecipeStorageClass c) {
  int idx = (int)c + 1;
  assert(idx < CLASS_NOOF + 1, "Out of bounds");
  return index2storage_class[idx];
}


static size_t class2maxwidth(ShuffleRecipeStorageClass c) {
  switch (c) {
  case CLASS_BUF:
  case CLASS_STACK:
  case CLASS_INTEGER:
    return 1;

  case CLASS_VECTOR:
    if (UseAVX >= 3) {
      return 8;
    } else if (UseAVX >= 1) {
      return 4;
    } else {
      return 2;
    }

  default:
    assert(false, "Unexpected class");
    return 1;
  }
}

enum ShuffleRecipeOp {
  OP_STOP,
  OP_SKIP,
  OP_PULL,
  OP_PULL_LABEL,
  OP_CREATE_BUFFER,
  OP_NOP,
  OP_NOOF
};

static const char* op_name[OP_NOOF] = {
  "OP_STOP", "OP_SKIP", "OP_PULL", "OP_PULL_LABEL", "OP_CREATE_BUFFER", "OP_NOP"
};

#ifndef PRODUCT
static const char* op2name(ShuffleRecipeOp op) {
  assert(op <= OP_NOOF, "invalid op");
  return op_name[op];
}
#endif

class ShuffleRecipe : public StackObj {
public:
  ShuffleRecipe(arrayHandle recipe)
    : _recipe(recipe) {
    assert(_recipe()->length() > 0, "Empty recipe not allowed");

    init_sizes();
  }

  arrayHandle recipe() { return _recipe; }
  uint64_t word(size_t index) {
    uint64_t* bits = (uint64_t*)_recipe()->base(T_LONG);
    return bits[index];
  }

  size_t length() { return _length; }

  size_t stack_args_slots() { return _stack_args_slots; }
  size_t buffer_slots() { return _buffer_slots; }
  size_t nlabels() { return _nlabels; }

#ifndef PRODUCT
  void print(outputStream* s);
#endif

private:
  void init_sizes();

  arrayHandle _recipe; // the long[] recipe array
  size_t _length;

  size_t _buffer_slots;
  size_t _stack_args_slots;
  size_t _nlabels;
};

class ShuffleRecipeStream {
public:
  ShuffleRecipeStream(ShuffleRecipe& recipe)
    : _recipe(recipe) {
    _next_word_index = 0;
    _cur_class = CLASS_FIRST;
    _direction = ARGUMENTS;

    read_recipe_word();
  }

  void read_recipe_word() {
    _cur_bits = _recipe.word(_next_word_index);
    _next_word_index++;
  }

  bool has_more() {
    return _cur_class < CLASS_NOOF;
  }

  void init_for_returns() {
    assert(_direction == ARGUMENTS, "stream already advanced");
    _cur_class = CLASS_FIRST;
    _direction = RETURNS;
  }

  ShuffleRecipeOp next() {
    assert(has_more(), "stream empty");

    if (_cur_bits == 1) {
      read_recipe_word();
    }

    ShuffleRecipeOp op = (ShuffleRecipeOp)(_cur_bits & 7);
    _cur_bits >>= 3;

    if (op == OP_STOP) {
      _cur_class = next_storage_class(_cur_class);
    }

    return op;
  }

private:
  enum Direction { ARGUMENTS, RETURNS };

  Direction _direction;
  ShuffleRecipe& _recipe;

  size_t _next_word_index;
  uint64_t _cur_bits;

  ShuffleRecipeStorageClass _cur_class;
};

void ShuffleRecipe::init_sizes() {
  _length = _recipe()->length();

  size_t slots_for_class[CLASS_NOOF] = { 0 };
  size_t nbuffers = 0;

  int cur_class = CLASS_FIRST;

  ShuffleRecipeStream stream(*this);

  while(stream.has_more() && cur_class <= CLASS_STACK) {
    switch (stream.next()) {
    case OP_NOP:
      break;

    case OP_STOP:
      cur_class++;
      break;

    case OP_CREATE_BUFFER:
      nbuffers++;
      break;

    case OP_SKIP:
    case OP_PULL:
    case OP_PULL_LABEL:
      slots_for_class[cur_class]++;
      break;

    default:
      assert(false, "Unexpected op");
      break;
    }
  }

  _stack_args_slots = slots_for_class[CLASS_STACK];
  _buffer_slots = slots_for_class[CLASS_BUF];
  _nlabels = nbuffers;
}

#ifndef PRODUCT
void ShuffleRecipe::print(outputStream* s) {
  ShuffleRecipeStream stream(*this);

  s->print_cr("Arguments:");
  while (stream.has_more()) {
    ShuffleRecipeOp op = stream.next();

    s->print_cr("OP: %s", op2name(op));
  }

  s->print_cr("Returns:");
  while (stream.has_more()) {
    ShuffleRecipeOp op = stream.next();

    s->print_cr("OP: %s", op2name(op));
  }
}
#endif


struct ShuffleDowncallContext {
  struct {
#ifdef _LP64
    uint64_t integer[INTEGER_ARGUMENT_REGISTERS_NOOF];
    VectorRegister vector[VECTOR_ARGUMENT_REGISTERS_NOOF];
    uintptr_t rax;
#endif
    uint64_t* stack_args;
    size_t stack_args_bytes;
    address next_pc;
  } arguments;

  struct {
    uint64_t integer[INTEGER_RETURN_REGISTERS_NOOF];
    VectorRegister vector[VECTOR_RETURN_REGISTERS_NOOF];
  } returns;
};

static void dump_vector_register(outputStream* out, XMMRegister reg, VectorRegister value) {
  out->print("%s = {", reg->name());
  for (size_t i = 0; i < VectorRegister::VECTOR_MAX_WIDTH_U64S; i++) {
    if (i != 0) {
      out->print(",");
    }
    out->print(" 0x%016" PRIx64, value.u64[i]);
  }
  out->print_cr(" }");
}

static void dump_integer_register(outputStream* out, Register reg, uint64_t value) {
  out->print_cr("%s = 0x%016" PRIx64, reg->name(), value);
}

#ifdef _LP64
static void dump_stack_slot(outputStream* out, size_t offset, uint64_t value) {
  out->print_cr("[sp+0x%zx] = 0x%016" PRIx64, offset, value);
}
#endif

static void dump_argument_registers(struct ShuffleDowncallContext* ctxt) {
#ifdef _LP64
  LogTarget(Info, panama) lt;
  if (lt.is_enabled()) {
    LogStream ls(lt);
    ls.print("Argument registers:\n");
    for (size_t i = 0; i < INTEGER_ARGUMENT_REGISTERS_NOOF; i++) {
      dump_integer_register(&ls, integer_argument_registers[i], ctxt->arguments.integer[i]);
    }
    for (size_t i = 0; i < VECTOR_ARGUMENT_REGISTERS_NOOF; i++) {
      dump_vector_register(&ls, vector_argument_registers[i], ctxt->arguments.vector[i]);
    }
    dump_integer_register(&ls, rax, ctxt->arguments.rax);

    for (size_t i = 0; i < ctxt->arguments.stack_args_bytes; i += sizeof(uint64_t)) {
      size_t slot = i / sizeof(uint64_t);
      dump_stack_slot(&ls, i, ctxt->arguments.stack_args[slot]);
    }
  }
#endif
}

static void dump_return_registers(struct ShuffleDowncallContext* ctxt) {
  LogTarget(Info, panama) lt;
  if (lt.is_enabled()) {
    LogStream ls(lt);
    ls.print("Return registers:\n");
    for (size_t i = 0; i < INTEGER_RETURN_REGISTERS_NOOF; i++) {
      dump_integer_register(&ls, integer_return_registers[i], ctxt->returns.integer[i]);
    }
    for (size_t i = 0; i < VECTOR_RETURN_REGISTERS_NOOF; i++) {
      dump_vector_register(&ls, vector_return_registers[i], ctxt->returns.vector[i]);
    }
  }
}


class ShuffleDowncall : public StackObj {
public:
  ShuffleDowncall(ShuffleRecipe& recipe, arrayHandle args, arrayHandle rets, address code, address stub)
    : _recipe(recipe), _args(args), _rets(rets), _code(code), _stub((InvokeNativeStub)stub), _stream(recipe) {
    memset(&_context.arguments, 0, sizeof(_context.arguments));
    memset(&_context.returns, 0, sizeof(_context.returns));

    _context.arguments.stack_args_bytes = _recipe.stack_args_slots() * sizeof(uint64_t);

    if (_context.arguments.stack_args_bytes == 0) {
      _context.arguments.stack_args = NULL;
    } else {
      _context.arguments.stack_args = NEW_RESOURCE_ARRAY(uint64_t, _context.arguments.stack_args_bytes / sizeof(uint64_t));
      memset(_context.arguments.stack_args, 0, _context.arguments.stack_args_bytes);
    }

    _context.arguments.next_pc = code;

    _buffers = NEW_RESOURCE_ARRAY(uint64_t, _recipe.buffer_slots());
  }

  void invoke(JavaThread* thread) {
#ifdef DEBUG_CALLS
    ::fprintf(stderr, "calling target function\n");
#endif

    prepare_call();

    dump_argument_registers(&_context);

    {
      assert(thread->thread_state() == _thread_in_vm, "thread state is: %d", thread->thread_state());
      ThreadToNativeFromVM ttnfvm(thread);
      assert(thread->thread_state() == _thread_in_native, "thread state is: %d", thread->thread_state());
      _stub(context());
      assert(thread->thread_state() == _thread_in_native, "thread state is: %d", thread->thread_state());
    }
    assert(thread->thread_state() == _thread_in_vm, "thread state is: %d", thread->thread_state());

    dump_return_registers(&_context);

    process_returns();
  }

private:
  void copy_argument_value(void** src_addrp) {
    LogTarget(Debug, panama) lt;
    if (lt.is_enabled()) {
      LogStream ls(lt);
      uint64_t* values = (uint64_t*)*src_addrp;
      for (size_t i = 0; i < _npulls; i++) {
        ls.print_cr("Pulling %3zd times to %20s[%3zd]: 0x%" PRIx64 "\n",
          _npulls, index2storage_class_name[_cur_class], _index_in_class, values[i]);
      }
    }

    switch (_cur_class) {
    case CLASS_BUF:
      assert(_index_in_class < _recipe.buffer_slots(), "out of bounds");
      memcpy((char*)_buffers + _index_in_class * sizeof(uint64_t), *src_addrp, sizeof(void*));
      break;

    case CLASS_STACK:
      assert(_index_in_class < _recipe.stack_args_slots(), "out of bounds");
      memcpy(&_context.arguments.stack_args[_index_in_class], *src_addrp, sizeof(uint64_t));
      if (lt.is_enabled()) {
        LogStream ls(lt);
        ls.print_cr("Pulling stack value: 0x%" PRIx64 "\n", *(uint64_t*)*src_addrp);
      }
      break;

#ifdef _LP64
    case CLASS_VECTOR:
      assert(_index_in_class < VECTOR_ARGUMENT_REGISTERS_NOOF, "out of bounds");
      memcpy(&_context.arguments.vector[_index_in_class], *src_addrp, _npulls * sizeof(uint64_t));
      break;

    case CLASS_INTEGER:
      assert(_index_in_class < INTEGER_ARGUMENT_REGISTERS_NOOF, "out of bounds");
      memcpy(&_context.arguments.integer[_index_in_class], *src_addrp, sizeof(uint64_t));
      break;
#endif

    default:
      assert(false, "Invalid class");
      break;
    }

    *src_addrp = (char*)*src_addrp + _npulls * sizeof(uint64_t);
    _npulls = 0;
    _index_in_class++;
  }

  void copy_return_value(void** dst_addrp) {
    switch (_cur_class) {
    case CLASS_BUF:
      assert(_index_in_class < _recipe.buffer_slots(), "out of bounds");
      memcpy(*dst_addrp, (char*)_buffers + _index_in_class * sizeof(uint64_t), sizeof(void*));
      break;

    case CLASS_STACK:
      assert(false, "Invalid class");
      break;

    case CLASS_VECTOR:
      assert(_index_in_class < VECTOR_RETURN_REGISTERS_NOOF, "out of bounds");
      memcpy(*dst_addrp, &_context.returns.vector[_index_in_class], _npulls * sizeof(uint64_t));
      break;

    case CLASS_INTEGER:
      assert(_index_in_class < INTEGER_RETURN_REGISTERS_NOOF, "out of bounds");
      memcpy(*dst_addrp, &_context.returns.integer[_index_in_class], sizeof(uint64_t));
      break;

    default:
      assert(false, "Invalid class");
      break;
    }

    *dst_addrp = (char*)*dst_addrp + _npulls * sizeof(uint64_t);
    _npulls = 0;
    _index_in_class++;
  }

  void prepare_call() {
#ifdef DEBUG_CALLS
    ::fprintf(stderr, "preparing_call()\n");
#endif

    _cur_class = CLASS_FIRST;
    _index_in_class = 0;
    _npulls = 0;

    void* cur_value_data = _args()->base(T_LONG);
    struct {
      void** labels;
      size_t produce;
      size_t consume;
    } labels;

    labels.labels = NEW_RESOURCE_ARRAY(void*, _recipe.nlabels());
    labels.produce = labels.consume = 0;

    while(_stream.has_more()) {
      ShuffleRecipeOp op = _stream.next();

#ifdef DEBUG_OPS
      ::fprintf(stderr, "OP: %s\n", op2name(op));
#endif

      switch (op) {
      case OP_NOP:
        break;

      case OP_STOP:
        if (_npulls > 0) {
          copy_argument_value(&cur_value_data);
        }

#ifdef _LP64
        if (_cur_class == CLASS_VECTOR) {
          _context.arguments.rax = _index_in_class;
        }
#endif

        _index_in_class = 0;
        _cur_class = next_storage_class(_cur_class);
        break;

      case OP_CREATE_BUFFER:
        assert(labels.produce < _recipe.nlabels(), "out of bounds");
        _labels[labels.produce] = (void*)((char*)_buffers + _index_in_class * sizeof(uint64_t));
        labels.produce++;
        break;

      case OP_PULL_LABEL:
        assert(labels.consume < labels.produce, "out of bounds");
        copy_argument_value(&_labels[labels.consume]);
        break;

      case OP_SKIP:
        if (_npulls > 0) {
          copy_argument_value(&cur_value_data);
        } else {
          _index_in_class++;
        }
        break;

      case OP_PULL:
        _npulls++;
        if (_npulls == class2maxwidth(_cur_class)) {
          copy_argument_value(&cur_value_data);
        }
        break;

      default:
        assert(false, "Unexpected op");
        break;
      }
    }
  }

  void process_returns() {
#ifdef DEBUG_CALLS
    ::fprintf(stderr, "processing returns\n");
#endif

    _stream.init_for_returns();

    void* cur_value_data = _rets()->base(T_LONG);
    struct {
      size_t consume;
    } labels = { 0 };

    _cur_class = CLASS_FIRST;
    // FIXME: What about buffers?

    while(_stream.has_more()) {
      ShuffleRecipeOp op = _stream.next();

      switch (op) {
      case OP_NOP:
        break;

      case OP_STOP:
        if (_npulls > 0) {
          copy_return_value(&cur_value_data);
        }

        _index_in_class = 0;
        _cur_class = next_storage_class(_cur_class);
        break;

      case OP_CREATE_BUFFER:
        assert(false, "Creating buffer now allowed in this phase");
        break;

      case OP_PULL_LABEL:
        assert(labels.consume < _recipe.nlabels(), "out of bounds");
        copy_return_value((void**)&_labels[labels.consume]);
        break;

      case OP_SKIP:
        if (_npulls > 0) {
          copy_return_value(&cur_value_data);
        } else {
          _index_in_class++;
        }
        break;

      case OP_PULL:
        _npulls++;
        if (_npulls == class2maxwidth(_cur_class)) {
          copy_return_value(&cur_value_data);
        }
        break;

      default:
        assert(false, "Unexpected op");
        break;
      }
    }
  }

  struct ShuffleDowncallContext* context() { return &_context; }

  ShuffleRecipe& _recipe;
  ShuffleRecipeStream _stream;
  arrayHandle _args;
  arrayHandle _rets;
  address _code;
  InvokeNativeStub _stub;

  struct ShuffleDowncallContext _context;

  ShuffleRecipeStorageClass _cur_class;
  size_t _index_in_class;
  size_t _npulls;

  void* _src_addr;

  void* _cur_value_data;

  void* _buffers;
  void** _labels;
};

#ifndef PRODUCT
class ShuffleRecipeVerifier : public StackObj {
public:
  ShuffleRecipeVerifier(ShuffleRecipe& recipe, size_t args_length, size_t rets_length)
    : _recipe(recipe), _args_length(args_length), _rets_length(rets_length), _stream(_recipe) {
  }

  void verify() {
    for (size_t i = 0; i < _recipe.length(); i++) {
      assert((_recipe.word(i) >> 63) == 1, "MSB in recipe word must be set");
    }

    do_verify(ARGUMENTS);

    _stream.init_for_returns();
    do_verify(RETURNS);
  }

private:
  enum Direction {
    ARGUMENTS,
    RETURNS
  };

  void do_verify(Direction direction) {
    struct op_stats {
      size_t op_count_per_class[CLASS_NOOF][OP_NOOF];
      size_t op_count[OP_NOOF];
    };

    struct op_stats stats;

    memset(&stats, 0, sizeof(stats));

    int cur_class = CLASS_FIRST;
    bool done = false;

    while (_stream.has_more()) {
      assert(!done, "Stream unexpectedly returned additional tokens");

      ShuffleRecipeOp op = _stream.next();

      stats.op_count_per_class[cur_class][op]++;
      stats.op_count[op]++;

      switch (op) {
      case OP_NOP:
      case OP_SKIP:
      case OP_PULL:
        break;

      case OP_STOP:
        cur_class++;
        if (cur_class == CLASS_NOOF) {
          done = true;
        }
        break;

      case OP_CREATE_BUFFER:
        assert(cur_class == CLASS_BUF, "Buffers may only be created in buffer class");
        assert(direction == ARGUMENTS, "Buffers can only be created when processing arguments");
        break;

      case OP_PULL_LABEL:
        assert(cur_class != CLASS_BUF, "Must not pull pull labels in buffer class");
        assert(direction == ARGUMENTS, "Buffer labels can only be pulled when processing arguments");
        break;

      default:
        assert(false, "Unexpected op");
        break;
      }
    }

    assert(done, "Not enough STOP operations");

    assert(stats.op_count[OP_CREATE_BUFFER] == stats.op_count[OP_PULL_LABEL], "All labels must be pulled");
    assert(direction == RETURNS || stats.op_count[OP_PULL] == _args_length, "All argument values must be pulled");
    assert(direction == ARGUMENTS || stats.op_count[OP_PULL] == _rets_length, "All return values must be pulled");
  }

private:
  ShuffleRecipe& _recipe;
  ShuffleRecipeStream _stream;
  size_t _args_length;
  size_t _rets_length;
};
#endif

void MethodHandles::generate_invoke_native(MacroAssembler* _masm) {


#if 0
  fprintf(stderr, "generate_invoke_native()\n");
#endif

  /**
   * invoke_native_stub(struct ShuffleDowncallContext* ctxt) {
   *   rbx = ctxt;
   *
   *   stack = alloca(ctxt->arguments.stack_args_bytes);
   *
   *   load_all_registers();
   *   memcpy(stack, ctxt->arguments.stack_args, arguments.stack_args_bytes);
   *
   *   (*ctxt->arguments.next_pc)();
   *
   *   store_all_registers();
   * }
   */

  __ enter();

  // Put the context pointer in ebx/rbx - it's going to be heavily used below both before and after the call
  Register ctxt_reg = rbx;

#ifdef _LP64
  __ block_comment("init_and_alloc_stack");

  __ push(ctxt_reg); // need to preserve register

#ifdef _LP64
  __ movptr(ctxt_reg, c_rarg0);
#else
  __ movptr(ctxt_reg, Address(rbp, 8));
#endif

  __ block_comment("allocate_stack");
  __ movptr(rcx, Address(ctxt_reg, offsetof(struct ShuffleDowncallContext, arguments.stack_args_bytes)));
  __ subptr(rsp, rcx);
  __ andptr(rsp, -64);

  // Note: rcx is used below!


  __ block_comment("load_arguments");

  __ shrptr(rcx, LogBytesPerWord); // bytes -> words
  __ movptr(rsi, Address(ctxt_reg, offsetof(struct ShuffleDowncallContext, arguments.stack_args)));
  __ movptr(rdi, rsp);
  __ rep_mov();

#ifdef _LP64
  for (size_t i = 0; i < VECTOR_ARGUMENT_REGISTERS_NOOF; i++) {
    // [1] -> 64 bit -> xmm
    // [2] -> 128 bit -> xmm
    // [4] -> 256 bit -> ymm
    // [8] -> 512 bit -> zmm

    XMMRegister reg = vector_argument_registers[i];
    size_t offs = offsetof(struct ShuffleDowncallContext, arguments.vector) + i * sizeof(VectorRegister);
    if (UseAVX >= 3) {
      __ evmovdqul(reg, Address(ctxt_reg, (int)offs), Assembler::AVX_512bit);
    } else if (UseAVX >= 1) {
      __ vmovdqu(reg, Address(ctxt_reg, (int)offs));
    } else {
      __ movdqu(reg, Address(ctxt_reg, (int)offs));
    }
  }

  for (size_t i = 0; i < INTEGER_ARGUMENT_REGISTERS_NOOF; i++) {
    size_t offs = offsetof(struct ShuffleDowncallContext, arguments.integer) + i * sizeof(uintptr_t);
    __ movptr(integer_argument_registers[i], Address(ctxt_reg, (int)offs));
  }

  __ movptr(rax, Address(ctxt_reg, offsetof(struct ShuffleDowncallContext, arguments.rax)));
#endif


  // call target function
  __ block_comment("call target function");
  __ call(Address(ctxt_reg, offsetof(struct ShuffleDowncallContext, arguments.next_pc)));


  __ block_comment("store_registers");
  for (size_t i = 0; i < INTEGER_RETURN_REGISTERS_NOOF; i++) {
    ssize_t offs = offsetof(struct ShuffleDowncallContext, returns.integer) + i * sizeof(uintptr_t);
    __ movptr(Address(ctxt_reg, offs), integer_return_registers[i]);
  }

  for (size_t i = 0; i < VECTOR_RETURN_REGISTERS_NOOF; i++) {
    // [1] -> 64 bit -> xmm
    // [2] -> 128 bit -> xmm (SSE)
    // [4] -> 256 bit -> ymm (AVX)
    // [8] -> 512 bit -> zmm (AVX-512, aka AVX3)

    XMMRegister reg = vector_return_registers[i];
    size_t offs = offsetof(struct ShuffleDowncallContext, returns.vector) + i * sizeof(VectorRegister);
    if (UseAVX >= 3) {
      __ evmovdqul(Address(ctxt_reg, (int)offs), reg, Assembler::AVX_512bit);
    } else if (UseAVX >= 1) {
      __ vmovdqu(Address(ctxt_reg, (int)offs), reg);
    } else {
      __ movdqu(Address(ctxt_reg, (int)offs), reg);
    }
  }
#else
  __ hlt();
#endif

  // Restore backed up preserved register
  __ movptr(ctxt_reg, Address(rbp, -(int)sizeof(uintptr_t)));

  __ leave();
  __ ret(0);

  __ flush();
}

void MethodHandles::invoke_native(arrayHandle recipe_arr, arrayHandle args_arr, arrayHandle rets_arr, address code, JavaThread* thread) {
  ShuffleRecipe recipe(recipe_arr);

#ifndef PRODUCT
  ShuffleRecipeVerifier verifier(recipe, args_arr()->length(), rets_arr()->length());
  verifier.verify();
#endif

  ShuffleDowncall call(recipe, args_arr, rets_arr, code, invoke_native_address());
  call.invoke(thread);
}
