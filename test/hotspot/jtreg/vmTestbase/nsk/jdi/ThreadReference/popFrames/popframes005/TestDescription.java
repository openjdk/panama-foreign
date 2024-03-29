/*
 * Copyright (c) 2018, 2024, Oracle and/or its affiliates. All rights reserved.
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


/*
 * @test
 *
 * @summary converted from VM Testbase nsk/jdi/ThreadReference/popFrames/popframes005.
 * VM Testbase keywords: [quick, jpda, jdi]
 * VM Testbase readme:
 * DESCRIPTION:
 *     The test for the implementation of an object of the type
 *     ThreadReference.
 *     The test checks up that a result of the method
 *     com.sun.jdi.ThreadReference.popFrames()
 *     complies with its spec:
 *     public void popFrames(StackFrame frame)
 *                throws IncompatibleThreadStateException
 *      Pop stack frames.
 *       All frames up to and including the frame are popped off the stack.
 *      The frame previous to the
 *      parameter frame will become the current frame.
 *       After this operation, this thread will be suspended at the invoke
 *      instruction of the target method that created frame.
 *      The frame's method can be reentered with a step into the instruction.
 *       The operand stack is restored, however, any changes to the arguments that
 *      occurred in the called method, remain. For example, if the method foo:
 *          void foo(int x) {
 *              System.out.println("Foo: " + x);
 *              x = 4;
 *              System.out.println("pop here");
 *          }
 *      was called with foo(7) and foo is popped at the second println and resumed,
 *      it  will print: Foo: 4.
 *       Locks acquired by a popped frame are released when it is popped.
 *      This applies to synchronized methods that are popped, and
 *      to any synchronized blocks within them.
 *       Finally blocks are not executed.
 *       No aspect of state, other than this thread's execution point and locks,
 *      is affected by this call. Specifically, the values of fields are unchanged,
 *      as are external resources such as I/O streams. Additionally,
 *      the target program might be placed in a state that is impossible with
 *      normal program flow; for example, order of lock acquisition might be
 *      perturbed. Thus the target program may proceed differently than the user would expect.
 *       The specified thread must be suspended.
 *       All StackFrame objects for this thread are invalidated.
 *       No events are generated by this method.
 *       None of the frames through and including frame may be native.
 *       Not all target virtual machines support this operation. Use
 *      VirtualMachine.canPopFrames() to determine if the operation is supported.
 *      Parameters: frame - Stack frame to pop.
 *                          frame is on this thread's call stack.
 *      Throws: UnsupportedOperationException -
 *              if the target virtual machine does not support this operation - see
 *              VirtualMachine.canPopFrames().
 *              IncompatibleThreadStateException -
 *              if this thread is not suspended.
 *              IllegalArgumentException -
 *              if frame is not on this thread's call stack.
 *              NativeMethodException -
 *              if one of the frames that would be popped is that of
 *              a native method or if frame is native.
 *              InvalidStackFrameException -
 *              if frame has become invalid. Once this thread is resumed,
 *              the stack frame is no longer valid.
 *     The test checks up on the assertions:
 *      (1) All StackFrame objects for this thread are invalidated.
 *      (2) The frame previous to the parameter frame will become the current frame.
 *     The test works as follows:
 *     The debugger program - nsk.jdi.ThreadReference.popFrames.popframes005;
 *     the debuggee program - nsk.jdi.ThreadReference.popFrames.popframes005a.
 *     Using nsk.jdi.share classes,
 *     the debugger gets the debuggee running on another JavaVM,
 *     creates the object debuggee.VM, and waits for VMStartEvent.
 *     Upon getting the debuggee VM started,
 *     the debugger calls corresponding debuggee.VM methods to get
 *     needed data and to perform checks.
 *     In case of error the test produces the return value 97 and
 *     a corresponding error message(s).
 *     Otherwise, the test is passed and produces
 *     the return value 95 and no message.
 * COMMENTS:
 *
 * @library /vmTestbase
 *          /test/lib
 * @build nsk.jdi.ThreadReference.popFrames.popframes005
 *        nsk.jdi.ThreadReference.popFrames.popframes005a
 * @run driver
 *      nsk.jdi.ThreadReference.popFrames.popframes005
 *      -verbose
 *      -arch=${os.family}-${os.simpleArch}
 *      -waittime=5
 *      -debugee.vmkind=java
 *      -transport.address=dynamic
 *      -debugee.vmkeys="${test.vm.opts} ${test.java.opts}"
 */

