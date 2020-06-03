/*
 *  Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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
 * @run testng/othervm -Dforeign.restricted=permit VaListTest
 */

import jdk.incubator.foreign.CSupport;
import jdk.incubator.foreign.CSupport.VaList;
import jdk.incubator.foreign.ForeignLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.LibraryLookup;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;

import static jdk.incubator.foreign.CSupport.C_DOUBLE;
import static jdk.incubator.foreign.CSupport.C_INT;
import static jdk.incubator.foreign.CSupport.C_LONGLONG;
import static jdk.incubator.foreign.CSupport.C_POINTER;
import static jdk.incubator.foreign.CSupport.C_VA_LIST;
import static jdk.incubator.foreign.CSupport.Win64.asVarArg;
import static jdk.incubator.foreign.MemoryLayout.PathElement.groupElement;
import static org.testng.Assert.assertEquals;

public class VaListTest {

    private static final ForeignLinker abi = CSupport.getSystemLinker();
    private static final LibraryLookup lookup = LibraryLookup.ofLibrary("VaList");

    private static final VarHandle VH_int = C_INT.varHandle(int.class);

    private static final MethodHandle MH_sumInts = link("sumInts",
            MethodType.methodType(int.class, int.class, VaList.class),
            FunctionDescriptor.of(C_INT, C_INT, CSupport.C_VA_LIST));
    private static final MethodHandle MH_sumDoubles = link("sumDoubles",
            MethodType.methodType(double.class, int.class, VaList.class),
            FunctionDescriptor.of(C_DOUBLE, C_INT, CSupport.C_VA_LIST));
    private static final MethodHandle MH_getInt = link("getInt",
            MethodType.methodType(int.class, VaList.class),
            FunctionDescriptor.of(C_INT, C_VA_LIST));
    private static final MethodHandle MH_sumStruct = link("sumStruct",
            MethodType.methodType(int.class, VaList.class),
            FunctionDescriptor.of(C_INT, C_VA_LIST));
    private static final MethodHandle MH_sumBigStruct = link("sumBigStruct",
            MethodType.methodType(long.class, VaList.class),
            FunctionDescriptor.of(C_LONGLONG, C_VA_LIST));
    private static final MethodHandle MH_sumStack = link("sumStack",
            MethodType.methodType(void.class, MemoryAddress.class, MemoryAddress.class, int.class,
                long.class, long.class, long.class, long.class,
                long.class, long.class, long.class, long.class,
                long.class, long.class, long.class, long.class,
                long.class, long.class, long.class, long.class,
                double.class, double.class, double.class, double.class,
                double.class, double.class, double.class, double.class,
                double.class, double.class, double.class, double.class,
                double.class, double.class, double.class, double.class
            ),
            FunctionDescriptor.ofVoid(C_POINTER, C_POINTER, C_INT,
                asVarArg(C_LONGLONG), asVarArg(C_LONGLONG), asVarArg(C_LONGLONG), asVarArg(C_LONGLONG),
                asVarArg(C_LONGLONG), asVarArg(C_LONGLONG), asVarArg(C_LONGLONG), asVarArg(C_LONGLONG),
                asVarArg(C_LONGLONG), asVarArg(C_LONGLONG), asVarArg(C_LONGLONG), asVarArg(C_LONGLONG),
                asVarArg(C_LONGLONG), asVarArg(C_LONGLONG), asVarArg(C_LONGLONG), asVarArg(C_LONGLONG),
                asVarArg(C_DOUBLE), asVarArg(C_DOUBLE), asVarArg(C_DOUBLE), asVarArg(C_DOUBLE),
                asVarArg(C_DOUBLE), asVarArg(C_DOUBLE), asVarArg(C_DOUBLE), asVarArg(C_DOUBLE),
                asVarArg(C_DOUBLE), asVarArg(C_DOUBLE), asVarArg(C_DOUBLE), asVarArg(C_DOUBLE),
                asVarArg(C_DOUBLE), asVarArg(C_DOUBLE), asVarArg(C_DOUBLE), asVarArg(C_DOUBLE)
            ));

    private static final VarHandle VH_long = C_LONGLONG.varHandle(long.class);
    private static final VarHandle VH_double = C_DOUBLE.varHandle(double.class);

    private static MethodHandle link(String symbol, MethodType mt, FunctionDescriptor fd) {
        try {
            return abi.downcallHandle(lookup.lookup(symbol), mt, fd);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }

    private static MethodHandle linkVaListCB(String symbol) {
        return link(symbol,
            MethodType.methodType(void.class, MemoryAddress.class),
            FunctionDescriptor.ofVoid(C_POINTER));

    }

    private static final GroupLayout BigPoint_LAYOUT = MemoryLayout.ofStruct(
        C_LONGLONG.withName("x"),
        C_LONGLONG.withName("y")
    );
    private static final VarHandle VH_BigPoint_x = BigPoint_LAYOUT.varHandle(long.class, groupElement("x"));
    private static final VarHandle VH_BigPoint_y = BigPoint_LAYOUT.varHandle(long.class, groupElement("y"));
    private static final GroupLayout Point_LAYOUT = MemoryLayout.ofStruct(
        C_INT.withName("x"),
        C_INT.withName("y")
    );
    private static final VarHandle VH_Point_x = Point_LAYOUT.varHandle(int.class, groupElement("x"));
    private static final VarHandle VH_Point_y = Point_LAYOUT.varHandle(int.class, groupElement("y"));

    @Test
    public void testIntSum() throws Throwable {
        try (VaList vaList = CSupport.newVaList(b ->
                b.intArg(C_INT, 10)
                 .intArg(C_INT, 15)
                 .intArg(C_INT, 20))) {
            int x = (int) MH_sumInts.invokeExact(3, vaList);
            assertEquals(x, 45);
        }
    }

    @Test
    public void testDoubleSum() throws Throwable {
        try (VaList vaList = CSupport.newVaList(b ->
                b.doubleArg(C_DOUBLE, 3.0D)
                 .doubleArg(C_DOUBLE, 4.0D)
                 .doubleArg(C_DOUBLE, 5.0D))) {
            double x = (double) MH_sumDoubles.invokeExact(3, vaList);
            assertEquals(x, 12.0D);
        }
    }

    @Test
    public void testVaListMemoryAddress() throws Throwable {
        try (MemorySegment msInt = MemorySegment.allocateNative(C_INT)) {
            VH_int.set(msInt.baseAddress(), 10);
            try (VaList vaList = CSupport.newVaList(b -> b.memoryAddressArg(C_POINTER, msInt.baseAddress()))) {
                int x = (int) MH_getInt.invokeExact(vaList);
                assertEquals(x, 10);
            }
        }
    }

    @Test
    public void testWinStructByValue() throws Throwable {
        try (MemorySegment struct = MemorySegment.allocateNative(Point_LAYOUT)) {
            VH_Point_x.set(struct.baseAddress(), 5);
            VH_Point_y.set(struct.baseAddress(), 10);

            try (VaList vaList = CSupport.newVaList(b -> b.memorySegmentArg(Point_LAYOUT, struct))) {
                int sum = (int) MH_sumStruct.invokeExact(vaList);
                assertEquals(sum, 15);
            }
        }
    }

    @Test
    public void testWinStructByReference() throws Throwable {
        try (MemorySegment struct = MemorySegment.allocateNative(BigPoint_LAYOUT)) {
            VH_BigPoint_x.set(struct.baseAddress(), 5);
            VH_BigPoint_y.set(struct.baseAddress(), 10);

            try (VaList vaList = CSupport.newVaList(b -> b.memorySegmentArg(BigPoint_LAYOUT, struct))) {
                long sum = (long) MH_sumBigStruct.invokeExact(vaList);
                assertEquals(sum, 15);
            }
        }
    }

    @Test
    public void testStack() throws Throwable {
       try (MemorySegment longSum = MemorySegment.allocateNative(C_LONGLONG);
            MemorySegment doubleSum = MemorySegment.allocateNative(C_DOUBLE)) {
            VH_long.set(longSum.baseAddress(), 0L);
            VH_double.set(doubleSum.baseAddress(), 0D);

            MH_sumStack.invokeExact(longSum.baseAddress(), doubleSum.baseAddress(), 32,
                1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L,
                1D, 2D, 3D, 4D, 5D, 6D, 7D, 8D, 9D, 10D, 11D, 12D, 13D, 14D, 15D, 16D);

            long lSum = (long) VH_long.get(longSum.baseAddress());
            double dSum = (double) VH_double.get(doubleSum.baseAddress());

            assertEquals(lSum, 136L);
            assertEquals(dSum, 136D);
        }
    }

    @Test(dataProvider = "upcalls")
    public void testUpcall(MethodHandle target, MethodHandle callback) throws Throwable {
        FunctionDescriptor desc = FunctionDescriptor.ofVoid(C_VA_LIST);
        try (MemorySegment stub = abi.upcallStub(callback, desc)) {
            target.invokeExact(stub.baseAddress());
        }
    }

    @DataProvider
    public static Object[][] upcalls() {
        return new Object[][]{
            { linkVaListCB("upcallBigStruct"), VaListConsumer.mh(vaList -> {
                try (MemorySegment struct = vaList.readStructOrUnion(BigPoint_LAYOUT)) {
                    assertEquals((long) VH_BigPoint_x.get(struct.baseAddress()), 8);
                    assertEquals((long) VH_BigPoint_y.get(struct.baseAddress()), 16);
                }
            })},
            { linkVaListCB("upcallBigStruct"), VaListConsumer.mh(vaList -> {
                VaList copy = vaList.copy();
                try (MemorySegment struct = vaList.readStructOrUnion(BigPoint_LAYOUT)) {
                    assertEquals((long) VH_BigPoint_x.get(struct.baseAddress()), 8);
                    assertEquals((long) VH_BigPoint_y.get(struct.baseAddress()), 16);

                    VH_BigPoint_x.set(struct.baseAddress(), 0);
                    VH_BigPoint_y.set(struct.baseAddress(), 0);
                }

                // should be independent
                try (MemorySegment struct = copy.readStructOrUnion(BigPoint_LAYOUT)) {
                    assertEquals((long) VH_BigPoint_x.get(struct.baseAddress()), 8);
                    assertEquals((long) VH_BigPoint_y.get(struct.baseAddress()), 16);
                }
            })},
            { linkVaListCB("upcallStruct"), VaListConsumer.mh(vaList -> {
                try (MemorySegment struct = vaList.readStructOrUnion(Point_LAYOUT)) {
                    assertEquals((int) VH_Point_x.get(struct.baseAddress()), 5);
                    assertEquals((int) VH_Point_y.get(struct.baseAddress()), 10);
                }
            })},
            { linkVaListCB("upcallMemoryAddress"), VaListConsumer.mh(vaList -> {
                MemoryAddress intPtr = vaList.readPointer(C_POINTER);
                MemorySegment ms = MemorySegment.ofNativeRestricted(intPtr, C_INT.byteSize(),
                                                                    Thread.currentThread(), null, null);
                int x = (int) VH_int.get(ms.baseAddress());
                assertEquals(x, 10);
            })},
            { linkVaListCB("upcallDoubles"), VaListConsumer.mh(vaList -> {
                assertEquals(vaList.readDouble(C_DOUBLE), 3.0);
                assertEquals(vaList.readDouble(C_DOUBLE), 4.0);
                assertEquals(vaList.readDouble(C_DOUBLE), 5.0);
            })},
            { linkVaListCB("upcallInts"), VaListConsumer.mh(vaList -> {
                assertEquals(vaList.readInt(C_INT), 10);
                assertEquals(vaList.readInt(C_INT), 15);
                assertEquals(vaList.readInt(C_INT), 20);
            })},
            { linkVaListCB("upcallStack"), VaListConsumer.mh(vaList -> {
                // skip all registers
                assertEquals(vaList.readLong(C_LONGLONG), 1L); // 1st windows arg read from shadow space
                assertEquals(vaList.readLong(C_LONGLONG), 2L); // 2nd windows arg read from shadow space
                assertEquals(vaList.readLong(C_LONGLONG), 3L); // windows 1st stack arg (int/float)
                assertEquals(vaList.readLong(C_LONGLONG), 4L);
                assertEquals(vaList.readLong(C_LONGLONG), 5L);
                assertEquals(vaList.readLong(C_LONGLONG), 6L);
                assertEquals(vaList.readLong(C_LONGLONG), 7L); // sysv 1st int stack arg
                assertEquals(vaList.readLong(C_LONGLONG), 8L);
                assertEquals(vaList.readLong(C_LONGLONG), 9L);
                assertEquals(vaList.readLong(C_LONGLONG), 10L);
                assertEquals(vaList.readLong(C_LONGLONG), 11L);
                assertEquals(vaList.readLong(C_LONGLONG), 12L);
                assertEquals(vaList.readLong(C_LONGLONG), 13L);
                assertEquals(vaList.readLong(C_LONGLONG), 14L);
                assertEquals(vaList.readLong(C_LONGLONG), 15L);
                assertEquals(vaList.readLong(C_LONGLONG), 16L);
                assertEquals(vaList.readDouble(C_DOUBLE), 1.0D);
                assertEquals(vaList.readDouble(C_DOUBLE), 2.0D);
                assertEquals(vaList.readDouble(C_DOUBLE), 3.0D);
                assertEquals(vaList.readDouble(C_DOUBLE), 4.0D);
                assertEquals(vaList.readDouble(C_DOUBLE), 5.0D);
                assertEquals(vaList.readDouble(C_DOUBLE), 6.0D);
                assertEquals(vaList.readDouble(C_DOUBLE), 7.0D);
                assertEquals(vaList.readDouble(C_DOUBLE), 8.0D);
                assertEquals(vaList.readDouble(C_DOUBLE), 9.0D); // sysv 1st float stack arg
                assertEquals(vaList.readDouble(C_DOUBLE), 10.0D);
                assertEquals(vaList.readDouble(C_DOUBLE), 11.0D);
                assertEquals(vaList.readDouble(C_DOUBLE), 12.0D);
                assertEquals(vaList.readDouble(C_DOUBLE), 13.0D);
                assertEquals(vaList.readDouble(C_DOUBLE), 14.0D);
                assertEquals(vaList.readDouble(C_DOUBLE), 15.0D);
                assertEquals(vaList.readDouble(C_DOUBLE), 16.0D);

                // test some arbitrary values on the stack
                assertEquals((byte) vaList.readInt(C_INT), (byte) 1);
                assertEquals((char) vaList.readInt(C_INT), 'a');
                assertEquals((short) vaList.readInt(C_INT), (short) 3);
                assertEquals(vaList.readInt(C_INT), 4);
                assertEquals(vaList.readLong(C_LONGLONG), 5L);
                assertEquals((float) vaList.readDouble(C_DOUBLE), 6.0F);
                assertEquals(vaList.readDouble(C_DOUBLE), 7.0D);
                assertEquals((byte) vaList.readInt(C_INT), (byte) 8);
                assertEquals((char) vaList.readInt(C_INT), 'b');
                assertEquals((short) vaList.readInt(C_INT), (short) 10);
                assertEquals(vaList.readInt(C_INT), 11);
                assertEquals(vaList.readLong(C_LONGLONG), 12L);
                assertEquals((float) vaList.readDouble(C_DOUBLE), 13.0F);
                assertEquals(vaList.readDouble(C_DOUBLE), 14.0D);

                try (MemorySegment point = vaList.readStructOrUnion(Point_LAYOUT)) {
                    assertEquals((int) VH_Point_x.get(point.baseAddress()), 5);
                    assertEquals((int) VH_Point_y.get(point.baseAddress()), 10);
                }

                VaList copy = vaList.copy();
                try (MemorySegment bigPoint = vaList.readStructOrUnion(BigPoint_LAYOUT)) {
                    assertEquals((long) VH_BigPoint_x.get(bigPoint.baseAddress()), 15);
                    assertEquals((long) VH_BigPoint_y.get(bigPoint.baseAddress()), 20);

                    VH_BigPoint_x.set(bigPoint.baseAddress(), 0);
                    VH_BigPoint_y.set(bigPoint.baseAddress(), 0);
                }

                // should be independent
                try (MemorySegment struct = copy.readStructOrUnion(BigPoint_LAYOUT)) {
                    assertEquals((long) VH_BigPoint_x.get(struct.baseAddress()), 15);
                    assertEquals((long) VH_BigPoint_y.get(struct.baseAddress()), 20);
                }
            })},
            // test skip
            { linkVaListCB("upcallStack"), VaListConsumer.mh(vaList -> {
                vaList.skip(C_LONGLONG, C_LONGLONG, C_LONGLONG, C_LONGLONG);
                assertEquals(vaList.readLong(C_LONGLONG), 5L);
                vaList.skip(C_LONGLONG, C_LONGLONG, C_LONGLONG, C_LONGLONG);
                assertEquals(vaList.readLong(C_LONGLONG), 10L);
                vaList.skip(C_LONGLONG, C_LONGLONG, C_LONGLONG, C_LONGLONG, C_LONGLONG, C_LONGLONG);
                assertEquals(vaList.readDouble(C_DOUBLE), 1.0D);
                vaList.skip(C_DOUBLE, C_DOUBLE, C_DOUBLE, C_DOUBLE);
                assertEquals(vaList.readDouble(C_DOUBLE), 6.0D);
            })},
        };
    }

    interface VaListConsumer {
        void accept(CSupport.VaList list);

        static MethodHandle mh(VaListConsumer instance) {
            try {
                return MethodHandles.lookup().findVirtual(VaListConsumer.class, "accept",
                    MethodType.methodType(void.class, VaList.class)).bindTo(instance);
            } catch (ReflectiveOperationException e) {
                throw new InternalError(e);
            }
        }
    }

}
