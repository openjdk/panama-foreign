/*
 * Copyright (c) 2001, 2024, Oracle and/or its affiliates. All rights reserved.
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

package nsk.jdi.Method.returnType;

import nsk.share.*;
import nsk.share.jpda.*;
import nsk.share.jdi.*;

import com.sun.jdi.*;
import java.util.*;
import java.io.*;

/**
 * The test for the implementation of an object of the type     <BR>
 * Method.                                                      <BR>
 *                                                              <BR>
 * The test checks up that results of the method                <BR>
 * <code>com.sun.jdi.Method.returnType()</code>                 <BR>
 * complies with its spec when a type is one of ReferenceTypes. <BR>
 * <BR>
 * The cases for testing are as follows.                <BR>
 *                                                      <BR>
 * When a gebuggee creates an object of the following   <BR>
 * class type with methods of ReferenceType return values:<BR>
 *                                                      <BR>
 *    class returntype003aTestClass {                   <BR>
 *              .                                       <BR>
 *              .                                       <BR>
 *        public ClassForCheck[] arraymethod () {       <BR>
 *            return cfc;                               <BR>
 *        }                                             <BR>
 *        public ClassForCheck classmethod () {         <BR>
 *            return classFC;                           <BR>
 *        }                                             <BR>
 *        public InterfaceForCheck ifacemethod () {     <BR>
 *            return iface;                             <BR>
 *        }                                             <BR>
 *   }                                                  <BR>
 *                                                      <BR>
 * a debugger forms their corresponding Type objects.   <BR>
 * <BR>
 */

public class returntype003 {

    //----------------------------------------------------- templete section
    static final int PASSED = 0;
    static final int FAILED = 2;
    static final int PASS_BASE = 95;

    //----------------------------------------------------- templete parameters
    static final String
    sHeader1 = "\n==> nsk/jdi/Method/returnType/returntype003",
    sHeader2 = "--> returntype003: ",
    sHeader3 = "##> returntype003: ";

    //----------------------------------------------------- main method

    public static void main (String argv[]) {
        int result = run(argv, System.out);
        if (result != 0) {
            throw new RuntimeException("TEST FAILED with result " + result);
        }
    }

    public static int run (String argv[], PrintStream out) {
        return new returntype003().runThis(argv, out);
    }

     //--------------------------------------------------   log procedures

    //private static boolean verbMode = false;

    private static Log  logHandler;

    private static void log1(String message) {
        logHandler.display(sHeader1 + message);
    }
    private static void log2(String message) {
        logHandler.display(sHeader2 + message);
    }
    private static void log3(String message) {
        logHandler.complain(sHeader3 + message);
    }

    //  ************************************************    test parameters

    private String debuggeeName =
        "nsk.jdi.Method.returnType.returntype003a";

    String mName = "nsk.jdi.Method.returnType";

    //====================================================== test program

    static ArgumentHandler      argsHandler;
    static int                  testExitCode = PASSED;

    //------------------------------------------------------ common section

    private int runThis (String argv[], PrintStream out) {

        Debugee debuggee;

        argsHandler     = new ArgumentHandler(argv);
        logHandler      = new Log(out, argsHandler);
        Binder binder   = new Binder(argsHandler, logHandler);

        if (argsHandler.verbose()) {
            debuggee = binder.bindToDebugee(debuggeeName + " -vbs");  // *** tp
        } else {
            debuggee = binder.bindToDebugee(debuggeeName);            // *** tp
        }

        IOPipe pipe     = new IOPipe(debuggee);

        debuggee.redirectStderr(out);
        log2("returntype003a debuggee launched");
        debuggee.resume();

        String line = pipe.readln();
        if ((line == null) || !line.equals("ready")) {
            log3("signal received is not 'ready' but: " + line);
            return FAILED;
        } else {
            log2("'ready' recieved");
        }

        VirtualMachine vm = debuggee.VM();

    //------------------------------------------------------  testing section
        log1("      TESTING BEGINS");

        for (int i = 0; ; i++) {
        pipe.println("newcheck");
            line = pipe.readln();

            if (line.equals("checkend")) {
                log2("     : returned string is 'checkend'");
                break ;
            } else if (!line.equals("checkready")) {
                log3("ERROR: returned string is not 'checkready'");
                testExitCode = FAILED;
                break ;
            }

            log1("new check: #" + i);

            //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ variable part

           List listOfDebuggeeClasses = vm.classesByName(mName + ".returntype003aTestClass");
                if (listOfDebuggeeClasses.size() != 1) {
                    testExitCode = FAILED;
                    log3("ERROR: listOfDebuggeeClasses.size() != 1");
                    break ;
                }

            List   methods = null;
            Method m       = null;

            int i2;

            for (i2 = 0; ; i2++) {

                int expresult = 0;

                log2("new check: #" + i2);

                switch (i2) {

                case 0:                 // ArrayType

                       methods = ((ReferenceType) listOfDebuggeeClasses.get(0)).
                           methodsByName("arraymethod");
                        m = (Method) methods.get(0);
                        try {
                            ArrayType aType = (ArrayType) m.returnType();
                        } catch ( ClassCastException e1 ) {
                            log3("ERROR: CCE: (ArrayType) m.returnType();");
                            expresult = 1;
                            break;
                        } catch ( ClassNotLoadedException e2 ) {
                            log3("ERROR: CNLE: (ArrayType) m.returnType();");
                            expresult = 1;
                            break;
                        }
                        break;

                case 1:                 // ClassType

                       methods = ((ReferenceType) listOfDebuggeeClasses.get(0)).
                           methodsByName("classmethod");
                        m = (Method) methods.get(0);
                        try {
                            ClassType cType = (ClassType) m.returnType();
                        } catch ( ClassCastException e1 ) {
                            log3("ERROR: CCE: (ClassType) m.returnType();");
                            expresult = 1;
                            break;
                        } catch ( ClassNotLoadedException e2 ) {
                            log3("ERROR: CNLE: (ClassType) m.returnType();");
                            expresult = 1;
                            break;
                        }
                        break;

                case 2:                 // InterfaceType

                       methods = ((ReferenceType) listOfDebuggeeClasses.get(0)).
                           methodsByName("ifacemethod");
                        m = (Method) methods.get(0);
                        try {
                            InterfaceType iType = (InterfaceType) m.returnType();
                        } catch ( ClassCastException e1 ) {
                            log3("ERROR: CCE: (InterfaceType) m.returnType();");
                            expresult = 1;
                            break;
                        } catch ( ClassNotLoadedException e2 ) {
                            log3("ERROR: CNLE: (InterfaceType) m.returnType();");
                            expresult = 1;
                            break;
                        }
                        break;


                default: expresult = 2;
                         break ;
                }

                if (expresult == 2) {
                    log2("      test cases finished");
                    break ;
                } else if (expresult == 1) {
                    log3("ERROR: expresult != true;  check # = " + i);
                    testExitCode = FAILED;
                }
            }
            //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        }
        log1("      TESTING ENDS");

    //--------------------------------------------------   test summary section
    //-------------------------------------------------    standard end section

        pipe.println("quit");
        log2("waiting for the debuggee to finish ...");
        debuggee.waitFor();

        int status = debuggee.getStatus();
        if (status != PASSED + PASS_BASE) {
            log3("debuggee returned UNEXPECTED exit status: " +
                    status + " != PASS_BASE");
            testExitCode = FAILED;
        } else {
            log2("debuggee returned expected exit status: " +
                    status + " == PASS_BASE");
        }

        if (testExitCode != PASSED) {
            logHandler.complain("TEST FAILED");
        }
        return testExitCode;
    }
}
