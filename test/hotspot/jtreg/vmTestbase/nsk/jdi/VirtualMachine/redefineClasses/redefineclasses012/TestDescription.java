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
 * @summary converted from VM Testbase nsk/jdi/VirtualMachine/redefineClasses/redefineclasses012.
 * VM Testbase keywords: [quick, jpda, jdi, redefine]
 * VM Testbase readme:
 * DESCRIPTION:
 *     The test against the method com.sun.jdi.VirtualMachine.redefineClasses()
 *     and checks up the following assertion:
 *        "If canUnrestrictedlyRedefineClasses() is false, changing the schema
 *        (the fields) will throw UnsupportedOperationException exception"
 *     This test looks like redefineclasses004 and checks the assertion
 *     against interfaces.
 *     The test consists of the following files:
 *         redefineclasses012.java             - debugger
 *         redefineclasses012a.java            - debuggee
 *         redefineclasses012b.java            - an initial redefined class
 *         newclassXX/redefineclasses012b.java - redefining debuggee's class
 *     This test performs the following cases:
 *       1. newclass01 - adding the fields
 *       2. newclass02 - deleting the fields
 *       3. newclass03 - adding <static> modifier
 *       4. newclass04 - adding <final> modifier
 *       5. newclass05 - changing <public> modifier to <private package>
 *       6. newclass06 - changing <private package> modifier to <public>
 *     The test checks two different cases for suspended debugee and not
 *     suspended one.
 * COMMENTS:
 *
 * @library /vmTestbase
 *          /test/lib
 * @build nsk.jdi.VirtualMachine.redefineClasses.redefineclasses012
 *        nsk.jdi.VirtualMachine.redefineClasses.redefineclasses012a
 *
 * @comment compile newclassXX to bin/newclassXX
 *          with full debug info
 * @run driver nsk.share.ExtraClassesBuilder
 *      -g:lines,source,vars
 *      newclass01 newclass02
 *
 * @run driver
 *      nsk.jdi.VirtualMachine.redefineClasses.redefineclasses012
 *      ./bin
 *      -verbose
 *      -arch=${os.family}-${os.simpleArch}
 *      -waittime=5
 *      -debugee.vmkind=java
 *      -transport.address=dynamic
 *      -debugee.vmkeys="${test.vm.opts} ${test.java.opts}"
 */

