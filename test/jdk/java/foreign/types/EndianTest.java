/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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
 * @modules java.base/jdk.internal.foreign.abi
 * @run testng EndianTest
 */

import java.foreign.Libraries;
import java.foreign.Library;
import java.foreign.NativeMethodType;
import java.foreign.NativeTypes;
import java.foreign.Scope;
import java.foreign.annotations.NativeAddressof;
import java.foreign.annotations.NativeGetter;
import java.foreign.annotations.NativeSetter;
import java.foreign.annotations.NativeStruct;
import java.foreign.layout.Group;
import java.foreign.layout.Value;
import java.foreign.memory.Array;
import java.foreign.memory.LayoutType;
import java.foreign.memory.Pointer;
import java.foreign.memory.Struct;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.nio.ByteOrder;
import jdk.internal.foreign.abi.SystemABI;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

@Test
public class EndianTest {
    private static final boolean isHostLE = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;
    private static byte[] DATA = { 0x12, 0x34, 0x56, 0x78, (byte) 0x9A, (byte) 0xBC, (byte) 0xDE, (byte) 0xF0 };
    private static final long BEL = 0x123456789ABCDEF0L;
    private static final int[] BEI = { 0x12345678, 0x9ABCDEF0 };
    private static final short[] BES = { 0x1234, 0x5678, (short) 0x9ABC, (short) 0xDEF0 };

    private static final long LEL = 0xF0DEBC9A78563412L;
    private static final int[] LEI = { 0x78563412, 0xF0DEBC9A };
    private static final short[] LES =  { 0x3412, 0x7856, (short) 0xBC9A, (short) 0xF0DE };

    private static final long HL = isHostLE ? LEL : BEL;
    private static final int[] HI = isHostLE ? LEI : BEI;
    private static final short[] HS = isHostLE ? LES : BES;

    @NativeStruct(">[[u16(hs)|u32(hl)|u64(hll)]u16(ns)x16u32(nl)u64(nll)]")
    interface HostNetworkValuesBE extends Struct<EndianTest.HostNetworkValuesBE> {
        @NativeGetter("hs")
        short hs$get();
        @NativeSetter("hs")
        void hs$set(short s);
        @NativeAddressof("hs")
        Pointer<Short> hs$ptr();

        @NativeGetter("hl")
        int hl$get();
        @NativeSetter("hl")
        void hl$set(int i);
        @NativeAddressof("hl")
        Pointer<Integer> hl$ptr();

        @NativeGetter("hll")
        long hll$get();
        @NativeSetter("hll")
        void hll$set(long l);
        @NativeAddressof("hll")
        Pointer<Long> hll$ptr();

        @NativeGetter("ns")
        short ns$get();
        @NativeSetter("nl")
        void ns$set(short s);
        @NativeAddressof("ns")
        Pointer<Short> ns$ptr();

        @NativeGetter("nl")
        int nl$get();
        @NativeSetter("nl")
        void nl$set(int i);
        @NativeAddressof("nl")
        Pointer<Short> nl$ptr();

        @NativeGetter("nll")
        long nll$get();
        @NativeSetter("nll")
        void nll$set(long l);
        @NativeAddressof("nll")
        Pointer<Short> nll$ptr();
    }

    @NativeStruct("[<[u16(hs)|u32(hl)|u64(hll)]>u16(ns)x16>u32(nl)>u64(nll)]")
    interface HostNetworkValuesLE extends Struct<EndianTest.HostNetworkValuesLE> {
        @NativeGetter("hs")
        short hs$get();
        @NativeSetter("hs")
        void hs$set(short s);
        @NativeAddressof("hs")
        Pointer<Short> hs$ptr();

        @NativeGetter("hl")
        int hl$get();
        @NativeSetter("hl")
        void hl$set(int i);
        @NativeAddressof("hl")
        Pointer<Integer> hl$ptr();

        @NativeGetter("hll")
        long hll$get();
        @NativeSetter("hll")
        void hll$set(long l);
        @NativeAddressof("hll")
        Pointer<Long> hll$ptr();

        @NativeGetter("ns")
        short ns$get();
        @NativeSetter("ns")
        void ns$set(short s);
        @NativeAddressof("ns")
        Pointer<Short> ns$ptr();

        @NativeGetter("nl")
        int nl$get();
        @NativeSetter("nl")
        void nl$set(int i);
        @NativeAddressof("nl")
        Pointer<Short> nl$ptr();

        @NativeGetter("nll")
        long nll$get();
        @NativeSetter("nll")
        void nll$set(long l);
        @NativeAddressof("nll")
        Pointer<Short> nll$ptr();
    }

    @NativeStruct("[[u16(hs)|u32(hl)|u64(hll)]>u16(ns)x16>u32(nl)>u64(nll)]")
    interface HostNetworkValues extends Struct<EndianTest.HostNetworkValues> {
        @NativeGetter("hs")
        short hs$get();
        @NativeSetter("hs")
        void hs$set(short s);
        @NativeAddressof("hs")
        Pointer<Short> hs$ptr();

        @NativeGetter("hl")
        int hl$get();
        @NativeSetter("hl")
        void hl$set(int i);
        @NativeAddressof("hl")
        Pointer<Integer> hl$ptr();

        @NativeGetter("hll")
        long hll$get();
        @NativeSetter("hll")
        void hll$set(long l);
        @NativeAddressof("hll")
        Pointer<Long> hll$ptr();

        @NativeGetter("ns")
        short ns$get();
        @NativeSetter("nl")
        void ns$set(short s);
        @NativeAddressof("ns")
        Pointer<Short> ns$ptr();

        @NativeGetter("nl")
        int nl$get();
        @NativeSetter("nl")
        void nl$set(int i);
        @NativeAddressof("nl")
        Pointer<Short> nl$ptr();

        @NativeGetter("nll")
        long nll$get();
        @NativeSetter("nll")
        void nll$set(long l);
        @NativeAddressof("nll")
        Pointer<Short> nll$ptr();
    }

    private static final Library lib;

    static {
        lib = Libraries.loadLibrary(MethodHandles.lookup(), "EndianTest");
    }

    @Test
    public void JavaRead() {
        try (Scope s = Scope.globalScope().fork()) {
            Array<Byte> ar = s.allocateArray(NativeTypes.UINT8, DATA.length);
            for (int i = 0; i < DATA.length; i++) {
                ar.set(i, DATA[i]);
            }
            Pointer<Void> p = ar.elementPointer().cast(NativeTypes.VOID);
            
            // Read without knowing endianness
            for (int i = 0; i < HS.length; i++) {
                assertEquals((short) p.cast(NativeTypes.INT16).offset(i).get(), HS[i]);
                assertEquals((short) p.cast(NativeTypes.UINT16).offset(i).get(), HS[i]);
            }
            for (int i = 0; i < HI.length; i++) {
                assertEquals((int) p.cast(NativeTypes.INT32).offset(i).get(), HI[i]);
                assertEquals((int) p.cast(NativeTypes.UINT32).offset(i).get(), HI[i]);
            }
            assertEquals((long) p.cast(NativeTypes.INT64).get(), HL);
            assertEquals((long) p.cast(NativeTypes.UINT64).get(), HL);

            // Read as little endian
            for (int i = 0; i < LES.length; i++) {
                assertEquals((short) p.cast(NativeTypes.LE_INT16).offset(i).get(), LES[i]);
                assertEquals((short) p.cast(NativeTypes.LE_UINT16).offset(i).get(), LES[i]);
            }
            for (int i = 0; i < LEI.length; i++) {
                assertEquals((int) p.cast(NativeTypes.LE_INT32).offset(i).get(), LEI[i]);
                assertEquals((int) p.cast(NativeTypes.LE_UINT32).offset(i).get(), LEI[i]);
            }
            assertEquals((long) p.cast(NativeTypes.LE_INT64).get(), LEL);
            assertEquals((long) p.cast(NativeTypes.LE_UINT64).get(), LEL);

            // Read as big endian
            for (int i = 0; i < BES.length; i++) {
                assertEquals((short) p.cast(NativeTypes.BE_INT16).offset(i).get(), BES[i]);
                assertEquals((short) p.cast(NativeTypes.BE_UINT16).offset(i).get(), BES[i]);
            }
            for (int i = 0; i < BEI.length; i++) {
                assertEquals((int) p.cast(NativeTypes.BE_INT32).offset(i).get(), BEI[i]);
                assertEquals((int) p.cast(NativeTypes.BE_UINT32).offset(i).get(), BEI[i]);
            }
            assertEquals((long) p.cast(NativeTypes.BE_INT64).get(), BEL);
            assertEquals((long) p.cast(NativeTypes.BE_UINT64).get(), BEL);
        }
    }

    @Test
    public void JavaWrite() {
        try (Scope s = Scope.globalScope().fork()) {
            Pointer<Void> p = s.allocate(NativeTypes.INT64).cast(NativeTypes.VOID);
            Pointer<Byte> pb = p.cast(NativeTypes.UINT8);

            p.cast(NativeTypes.BE_INT64).set(BEL);
            for (int i = 0; i < DATA.length; i++) {
                assertEquals((byte) pb.offset(i).get(), DATA[i]);
            }
            assertEquals((int) p.cast(NativeTypes.LE_UINT32).offset(1).get(), LEI[1]);
            assertEquals((short) p.cast(NativeTypes.BE_UINT16).offset(1).get(), BES[1]);

            p.cast(NativeTypes.LE_INT64).set(LEL);
            assertEquals((long) p.cast(NativeTypes.BE_INT64).get(), BEL);
            assertEquals((long) p.cast(NativeTypes.INT64).get(), HL);

            p.cast(NativeTypes.INT64).set(HL);
            assertEquals((long) p.cast(NativeTypes.UINT64).get(), HL);
            assertEquals((long) p.cast(NativeTypes.BE_INT64).get(), BEL);
            assertEquals((int) p.cast(NativeTypes.INT32).get(), HI[0]);

            p.cast(NativeTypes.BE_INT16).set((short) 0xDEAD);
            p.cast(NativeTypes.INT16).offset(1).set(isHostLE ? (short) 0xEFBE : (short) 0xBEEF);
            p.cast(NativeTypes.BE_INT32).offset(1).set(0xCAFEBABE);
            assertEquals((long) p.cast(NativeTypes.BE_UINT64).get(), 0xDEADBEEFCAFEBABEL);
            p.cast(NativeTypes.INT32).set(isHostLE ? 0x12345678 : 0x78563412);
            p.cast(NativeTypes.BE_UINT16).offset(2).set((short) 0xBABE);
            p.cast(NativeTypes.LE_UINT16).offset(3).set((short) 0xDEC0);
            assertEquals((long) p.cast(NativeTypes.BE_INT64).get(), 0x78563412BABEC0DEL);
        }
    }

    private MethodHandle ofFunction(String fn, boolean seedArgBE) throws NoSuchMethodException {
        Library.Symbol initFn = lib.lookup(fn);
        LayoutType<?> arg1 = isHostLE ?
                LayoutType.ofStruct(HostNetworkValuesLE.class).pointer() :
                LayoutType.ofStruct(HostNetworkValuesBE.class).pointer();
        LayoutType<Long> arg2 = seedArgBE ? NativeTypes.BE_INT64 : NativeTypes.INT64;

        NativeMethodType nmt = NativeMethodType.of(NativeTypes.INT64, arg1, arg2);
        return SystemABI.getInstance().downcallHandle(initFn, nmt);
    }

    @Test
    public void MatchRead() throws Throwable {
        try (Scope s = Scope.globalScope().fork()) {
            HostNetworkValuesLE le = s.allocateStruct(HostNetworkValuesLE.class);
            Pointer<Void> p = le.ptr().cast(NativeTypes.VOID);
            HostNetworkValuesBE be = p.cast(LayoutType.ofStruct(HostNetworkValuesBE.class)).get();
            MethodHandle mh = ofFunction("initHost", false);
            long hll = (long) mh.invokeExact(p, HL);
            for (int i = 0; i < DATA.length; i++) {
                assertEquals((byte) p.cast(NativeTypes.UINT8).offset(i).get(), DATA[i]);
            }
            assertEquals(hll, HL);
            // read host value as LittleEndian
            assertEquals(le.hs$get(), LES[0]);
            assertEquals(le.hl$get(), LEI[0]);
            assertEquals(le.hll$get(), LEL);
            // read host value as BigEndian
            assertEquals(be.hs$get(), BES[0]);
            assertEquals(be.hl$get(), BEI[0]);
            assertEquals(be.hll$get(), BEL);
            // Struct with BigEndian descriptor
            // so read a network order stored value should end up as host value
            assertEquals(le.ns$get(), HS[0]);
            assertEquals(le.nl$get(), HI[0]);
            assertEquals(le.nll$get(), HL);
        }
    }

    @Test
    public void MatchWrite() throws Throwable {
        try (Scope s = Scope.globalScope().fork()) {
            HostNetworkValuesLE le = s.allocateStruct(HostNetworkValuesLE.class);
            Pointer<Void> p = le.ptr().cast(NativeTypes.VOID);
            HostNetworkValuesBE be = p.cast(LayoutType.ofStruct(HostNetworkValuesBE.class)).get();

            if (isHostLE) {
                le.hll$set(HL);
            } else {
                be.hll$set(HL);
            }
            assertEquals(le.hll$get(), LEL);
            assertEquals(be.hll$get(), BEL);
            // Descriptors are Big Endian, so we set with Host Endian value will end up as Big Endian
            le.ns$set(HS[0]);
            le.nl$set(HI[0]);
            le.nll$set(HL);
            MethodHandle mh = ofFunction("isSameValue", false);
            assertEquals((long) mh.invokeExact(p, HL), 0);
        }
    }

    @Test
    public void BigEndianArgNotAllowed() throws Throwable {
        try (Scope s = Scope.globalScope().fork()) {
            Pointer<Void> p = s.allocateStruct(HostNetworkValuesLE.class).ptr().cast(NativeTypes.VOID);
            MethodHandle mh = ofFunction("initBE", false);
            assertEquals((long) mh.invokeExact(p, BEL), HL);
            for (int i = 0; i < DATA.length; i++) {
                assertEquals((byte) p.cast(NativeTypes.UINT8).offset(i).get(), DATA[i]);
            }

            try {
                mh = ofFunction("isSameValue", true);
                fail("Expecting IllegalArgumentException with endianness argument");
            } catch (IllegalArgumentException iae) {
                // Ignore expected exception
            }
        }
    }

    @Test
    public void verifyLayout() {
        final String layoutLE = "[[<u16(hs)|<u32(hl)|<u64(hll)]>u16(ns)x16>u32(nl)>u64(nll)]";
        final String layoutBE = "[[>u16(hs)|>u32(hl)|>u64(hll)]>u16(ns)x16>u32(nl)>u64(nll)]";
        final String layoutNE = "[[u16(hs)|u32(hl)|u64(hll)]>u16(ns)x16>u32(nl)>u64(nll)]";

        assertEquals(LayoutType.ofStruct(HostNetworkValuesBE.class).layout().toString(), layoutBE);
        assertEquals(LayoutType.ofStruct(HostNetworkValuesLE.class).layout().toString(), layoutLE);

        Group NE = (Group) LayoutType.ofStruct(HostNetworkValues.class).layout();
        Class hnv = isHostLE ? HostNetworkValuesLE.class : HostNetworkValuesBE.class;
        assertEquals(NE.toString(), layoutNE);
        assertEquals(NE.withEndianness(Value.Endianness.hostEndian()).toString(),
                LayoutType.ofStruct(hnv).layout().toString());
        assertEquals(NE.withEndianness(Value.Endianness.BIG_ENDIAN).toString(), layoutBE);
        assertEquals(NE.withEndianness(Value.Endianness.LITTLE_ENDIAN).toString(), layoutLE);
    }
}
