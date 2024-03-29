/*
 * Copyright (c) 2002, 2024, Oracle and/or its affiliates. All rights reserved.
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

package nsk.jdi.BScenarios.hotswap;

import nsk.share.*;
import nsk.share.jpda.*;
import nsk.share.jdi.*;

import com.sun.jdi.*;
import com.sun.jdi.request.*;
import com.sun.jdi.event.*;

import java.util.*;
import java.io.*;

/**
 * This test is from the group of so-called Borland's scenarios and
 * implements the following test case:                                   <br>
 *     Suite 3 - Hot Swap                                                <br>
 *     Test case:      TC5                                               <br>
 *     Description:    After point of execution, same method - stepping  <br>
 *     Steps:          1.Set breakpoint at line 24 (call to b()          <br>
 *                        from a())                                      <br>
 *                     2.Debug Main                                      <br>
 *                     3.Insert as next line after point of              <br>
 *                        execution: System.err.println("foo");          <br>
 *                     4.Smart Swap                                      <br>
 *                     5.F7 to step into                                 <br>
 *                        X. Steps into method b()                       <br>
 *                                                                       <br>
 * The description was drown up according to steps under JBuilder.
 *
 * Of course, the test has own line numbers and method/class names and
 * works as follow:
 * When the test is starting debugee, debugger sets breakpoint at
 * the 38th line (method <code>method_A</code>).
 * After the breakpoint is reached, debugger redefines debugee adding
 * a new line into <code>method_A</code>, creates <code>StepRequest</code> and
 * resumes debugee. When the location of the current <code>StepEvent</code> is
 * in <code>method_B</code>, created <code>StepRequest</code> is disabled.
 */

public class tc05x002 {

    public final static String UNEXPECTED_STRING = "***Unexpected exception ";

    private final static String prefix = "nsk.jdi.BScenarios.hotswap.";
    private final static String debuggerName = prefix + "tc05x002";
    private final static String debugeeName = debuggerName + "a";

    private final static String newClassFile = "newclass" + File.separator
                    + debugeeName.replace('.',File.separatorChar)
                    + ".class";

    private static int exitStatus;
    private static Log log;
    private static Debugee debugee;
    private static long waitTime;
    private static String classDir;

    private static final String firstMethodName = "method_B";

    private int eventCount;
    private int expectedEventCount = 2;
    private ReferenceType debugeeClass;

    private static void display(String msg) {
        log.display(msg);
    }

    private static void complain(String msg) {
        log.complain("debugger FAILURE> " + msg + "\n");
    }

    public static void main(String argv[]) {
        int result = run(argv,System.out);
        if (result != 0) {
            throw new RuntimeException("TEST FAILED with result " + result);
        }
    }

    public static int run(String argv[], PrintStream out) {

        exitStatus = Consts.TEST_PASSED;

        tc05x002 thisTest = new tc05x002();

        ArgumentHandler argHandler = new ArgumentHandler(argv);
        log = new Log(out, argHandler);

        classDir = argv[0];
        waitTime = argHandler.getWaitTime() * 60000;

        Binder binder = new Binder(argHandler, log);
        debugee = binder.bindToDebugee(debugeeName);

        try {
            thisTest.execTest();
        } catch (Throwable e) {
            exitStatus = Consts.TEST_FAILED;
            e.printStackTrace();
        } finally {
            debugee.endDebugee();
        }
        display("Test finished. exitStatus = " + exitStatus);

        return exitStatus;
    }

    private void execTest() throws Failure {

        if (!debugee.VM().canRedefineClasses()) {
            display("\n>>>canRedefineClasses() is false<<< test canceled.\n");
            return;
        }

        display("\nTEST BEGINS");
        display("===========");

        EventSet eventSet = null;
        EventIterator eventIterator = null;
        Event event;
        long totalTime = waitTime;
        long tmp, begin = System.currentTimeMillis(),
             delta = 0;
        boolean exit = false;

        eventCount = 0;
        EventRequestManager evm = debugee.getEventRequestManager();
        ClassPrepareRequest req = evm.createClassPrepareRequest();
        req.addClassFilter(debugeeName);
        req.enable();
        debugee.resume();

        while (totalTime > 0 && !exit) {
            if (eventIterator == null || !eventIterator.hasNext()) {
                try {
                    eventSet = debugee.VM().eventQueue().remove(totalTime);
                } catch (InterruptedException e) {
                    new Failure(e);
                }
                if (eventSet != null) {
                    eventIterator = eventSet.eventIterator();
                } else {
                    eventIterator = null;
                }
            }
            if (eventIterator != null) {
                while (eventIterator.hasNext()) {
                    event = eventIterator.nextEvent();
//                    display("\n event ===>>> " + event);

                    if (event instanceof ClassPrepareEvent) {
                        display("\n event ===>>> " + event);
                        debugeeClass = ((ClassPrepareEvent )event).referenceType();
                        display("Tested class\t:" + debugeeClass.name());
                        debugee.setBreakpoint(debugeeClass,
                                                    tc05x002a.brkpMethodName,
                                                    tc05x002a.brkpLineNumber);

                        debugee.resume();

                    } else if (event instanceof BreakpointEvent) {
                        display("\n event ===>>> " + event);
                        hitBreakpoint((BreakpointEvent )event);
                        display("redefining...");
                        redefineDebugee();
                        createStepRequest(((LocatableEvent )event).thread());
                        debugee.resume();

                    } else if (event instanceof StepEvent) {
                        display("\n event ===>>> " + event);
                        hitStep((StepEvent )event);
                        debugee.resume();

                    } else if (event instanceof VMDeathEvent) {
                        exit = true;
                        break;
                    } else if (event instanceof VMDisconnectEvent) {
                        exit = true;
                        break;
                    } // if
                } // while
            } // if
            tmp = System.currentTimeMillis();
            delta = tmp - begin;
            totalTime -= delta;
                begin = tmp;
        }

        if (eventCount != expectedEventCount) {
            if (totalTime <= 0) {
                complain("out of wait time...");
            }
            complain("expecting " + expectedEventCount
                        + " events, but "
                        + eventCount + " events arrived.");
            exitStatus = Consts.TEST_FAILED;
        }

        display("=============");
        display("TEST FINISHES\n");
    }

    private void redefineDebugee() {
        Map<com.sun.jdi.ReferenceType,byte[]> mapBytes;
        boolean alreadyComplained = false;
        mapBytes = mapClassToBytes(newClassFile);
        try {
            debugee.VM().redefineClasses(mapBytes);
        } catch (Exception e) {
            throw new Failure(UNEXPECTED_STRING + e);
        }
    }

    private Map<com.sun.jdi.ReferenceType,byte[]> mapClassToBytes(String fileName) {
        display("class-file\t:" + fileName);
        File fileToBeRedefined = new File(classDir + File.separator + fileName);
        int fileToRedefineLength = (int )fileToBeRedefined.length();
        byte[] arrayToRedefine = new byte[fileToRedefineLength];

        FileInputStream inputFile;
        try {
            inputFile = new FileInputStream(fileToBeRedefined);
        } catch (FileNotFoundException e) {
            throw new Failure(UNEXPECTED_STRING + e);
        }

        try {
            inputFile.read(arrayToRedefine);
            inputFile.close();
        } catch (IOException e) {
            throw new Failure(UNEXPECTED_STRING + e);
        }
        HashMap<com.sun.jdi.ReferenceType,byte[]> mapForClass = new HashMap<com.sun.jdi.ReferenceType,byte[]>();
        mapForClass.put(debugeeClass, arrayToRedefine);
        return mapForClass;
    }

    private StepRequest createStepRequest(ThreadReference thread) {
        EventRequestManager evm = debugee.getEventRequestManager();
        StepRequest request = evm.createStepRequest(thread,
                                                        StepRequest.STEP_LINE,
                                                        StepRequest.STEP_INTO);
        request.enable();
        return request;
    }

    private void hitBreakpoint(BreakpointEvent event) {
        locationInfo(event);
        if (event.location().lineNumber() != tc05x002a.checkLastLine) {
            complain("BreakpointEvent steps to line " + event.location().lineNumber()
                        + ", expected line number is "
                        + tc05x002a.checkLastLine);
            exitStatus = Consts.TEST_FAILED;
        } else {
            display("!!!BreakpointEvent steps to the expected line "
                        + event.location().lineNumber() + "!!!");
        }
        display("");
    }

    private void hitStep(StepEvent event) {
        locationInfo(event);
        String methodName = event.location().method().name();
        StepRequest request = (StepRequest )event.request();
        if (methodName.compareTo(firstMethodName) != 0) {
            if (!event.location().method().isObsolete()) {
                complain("Unexpected event" + event);
                exitStatus = Consts.TEST_FAILED;
            } else {
                display("!!!Expected step event!!!");
            }
        } else {
            display("!!!Expected step event!!!");
            request.disable();
        }
        display("");
    }

    private void locationInfo(LocatableEvent event) {
        if (!event.location().method().isObsolete()) {
            eventCount++;
        }
        String methodName = "<>";
        display("event info: #" + eventCount);
        display("\tthread\t- " + event.thread().name());
        try {
            methodName = event.location().method().name();
            display("\tsource\t- " + event.location().sourceName());
            display("\tmethod\t- " + methodName);
            display("\tline\t- " + event.location().lineNumber());
        } catch (AbsentInformationException e) {
        }
        if (event.location().method().isObsolete()) {
            display(methodName + " method is skipped");
        }
    }
}
