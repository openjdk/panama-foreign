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
 * @summary converted from VM Testbase nsk/jdi/ClassLoaderReference/definedClasses/definedclasses004.
 * VM Testbase keywords: [quick, jpda, jdi]
 * VM Testbase readme:
 * DESCRIPTION:
 *    The test checks up that an implementation of the
 *    com.sun.jdi.ClassLoaderReference.definedClasses method conforms
 *    with its spec.
 *    The test verifies an assertion:
 *       public List definedClasses()
 *       Returns a list of all loaded classes that were defined by this class
 *       loader. No ordering of this list guaranteed.
 *       The returned list will include reference types loaded at least to the
 *       point of preparation and types (like array) for which preparation is
 *       not defined.
 *    The test consists of:
 *      debugger application - definedclasses004,
 *      debuggee application - definedclasses004a,
 *      custom-loaded class in the debuggee - definedclasses004b.
 *    All classes belong to 'nsk.jdi.ClassLoaderReference.definedClasses' package.
 *    The debugger gets ClassLoaderReference of debuggee class loader.
 *    The test fails if isPrepared() method returns false for any
 *    ReferenceType item in the list returned by definedClasses() method.
 * COMMENTS:
 *
 * @library /vmTestbase
 *          /test/lib
 * @build nsk.jdi.ClassLoaderReference.definedClasses.definedclasses004
 *        nsk.jdi.ClassLoaderReference.definedClasses.definedclasses004a
 * @run driver
 *      nsk.jdi.ClassLoaderReference.definedClasses.definedclasses004
 *      -verbose
 *      -arch=${os.family}-${os.simpleArch}
 *      -waittime=5
 *      -debugee.vmkind=java
 *      -transport.address=dynamic
 *      -debugee.vmkeys="${test.vm.opts} ${test.java.opts}"
 */

