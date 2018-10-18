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

import java.foreign.Libraries;
import java.foreign.Library;
import java.foreign.NativeTypes;
import java.foreign.Scope;
import java.foreign.memory.Array;
import java.foreign.memory.Pointer;
import java.foreign.memory.Struct;
import java.lang.invoke.MethodHandles;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import static test.jextract.lp.libproc_h.*;
import static test.jextract.lp.proc_info_h.*;
import test.jextract.lp.proc_info;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/*
 * @test
 * @library ..
 * @requires (os.family == "mac")
 * @run driver JtregJextract -t test.jextract.lp -lproc -rpath /usr/lib -- /usr/include/libproc.h /usr/include/sys/proc_info.h
 * @run testng LibprocTest
 */
public class LibprocTest {
    private static final int NAME_BUF_MAX = 256;
    @Test
    public void processListTest() {
        long curProcPid = ProcessHandle.current().pid();
        boolean foundCurProc = false;

        // Scope for native allocations
        try (Scope s = Scope.newNativeScope()) {
            // get the number of processes
            int numPids = proc_listallpids(Pointer.nullPointer(), 0);
            // allocate an array
            Array<Integer> pids = s.allocateArray(NativeTypes.INT32, numPids);
            // list all the pids into the native array
            proc_listallpids(pids.elementPointer(), numPids);
            // convert native array to java array
            int[] jpids = pids.toArray(num -> new int[num]);
            // buffer for process name
            Pointer<Byte> nameBuf = s.allocate(NativeTypes.INT8, NAME_BUF_MAX);
            for (int i = 0; i < jpids.length; i++) {
                int pid = jpids[i];
                // get the process name
                proc_name(pid, nameBuf, NAME_BUF_MAX);
                String procName = Pointer.toString(nameBuf);
                // print pid and process name
                System.out.printf("%d %s\n", pid, procName);
                if (curProcPid == pid) {
                    foundCurProc = true;
                    System.out.println("Found the current process!");
                }
            }
        }
        assertTrue(foundCurProc);
    }

    @Test
    public void processInfoTest() {
        long curProcPid = ProcessHandle.current().pid();
        try (Scope s = Scope.newNativeScope()) {
            proc_info.proc_taskinfo ti = s.allocateStruct(proc_info.proc_taskinfo.class);
            int taskInfoSize = (int)Struct.sizeof(proc_info.proc_taskinfo.class);
            int resultSize = proc_pidinfo((int)curProcPid, PROC_PIDTASKINFO, 0, ti.ptr(), taskInfoSize);
            assertEquals(resultSize, taskInfoSize);
            System.out.println("total virtual memory size = " + ti.pti_virtual_size$get());
            System.out.println("resident memory size = " + ti.pti_resident_size$get());
            System.out.println("total time = " + ti.pti_total_user$get());
        }
    }
}
