/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.
 *
 *  This code is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  version 2 for more details (a copy is included in the LICENSE file that
 *  accompanied this code).
 *
 *  You should have received a copy of the GNU General Public License version
 *  2 along with this work; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *   Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 *
 */

/*
 * @test
 * @run testng TestUpcallStubs
 */

import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.SystemABI;
import org.testng.annotations.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;

import static jdk.incubator.foreign.MemoryLayouts.JAVA_INT;

public class TestUpcallStubs {

    static final SystemABI abi = SystemABI.getInstance();
    static final MethodHandle MH_dummy;

    static {
        try {
            MH_dummy = MethodHandles.lookup()
                .findStatic(TestUpcallStubs.class, "dummy", MethodType.methodType(void.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new BootstrapMethodError(e);
        }
    }

    private static MemoryAddress getStub() {
        return abi.upcallStub(MH_dummy, FunctionDescriptor.ofVoid());
    }

    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void testNoAccess() {
        MemoryAddress stub = getStub();
        try {
            VarHandle vh = JAVA_INT.varHandle(int.class);
            vh.set(stub, 10);
        } finally {
            abi.freeUpcallStub(stub);
        }
    }

    @Test
    public void testFree() {
        MemoryAddress stub = getStub();
        abi.freeUpcallStub(stub);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = ".*Not a stub address: .*")
    public void testAlreadyFreed() {
        MemoryAddress stub = getStub();
        abi.freeUpcallStub(stub);
        abi.freeUpcallStub(stub); // should fail
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = ".*Not a stub address: .*",
          dataProvider = "badAddresses")
    public void testCanNotFree(MemoryAddress ma) {
        abi.freeUpcallStub(ma);
    }

    @DataProvider
    public static Object[][] badAddresses() {
        return new Object[][]{
            { MemoryAddress.ofLong(42) /* random address */ },
            { MemorySegment.ofArray(new int []{ 1, 2, 3 }).baseAddress() /* heap address */ }
        };
    }

    // where
    public static void dummy() {}

}
