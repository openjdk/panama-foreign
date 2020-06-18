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
 * @run testng/othervm -Dforeign.restricted=permit TestNativeScope
 */

import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.NativeScope;
import jdk.incubator.foreign.MemoryHandles;
import jdk.incubator.foreign.MemoryLayouts;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemoryAddress;

import org.testng.annotations.*;

import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static jdk.incubator.foreign.MemorySegment.CLOSE;
import static jdk.incubator.foreign.MemorySegment.HANDOFF;
import static org.testng.Assert.*;

public class TestNativeScope {

    final static int ELEMS = 128;

    @Test(dataProvider = "nativeScopes")
    public <Z> void testAllocation(Z value, ScopeFactory scopeFactory, MemoryLayout layout, Class<?> carrier, AllocationFunction<Z> allocationFunction, Function<MemoryLayout, VarHandle> handleFactory) {
        MemoryLayout[] layouts = {
                layout,
                layout.withBitAlignment(layout.bitAlignment() * 2),
                layout.withBitAlignment(layout.bitAlignment() * 4),
                layout.withBitAlignment(layout.bitAlignment() * 8)
        };
        for (MemoryLayout alignedLayout : layouts) {
            List<MemoryAddress> addressList = new ArrayList<>();
            int elems = ELEMS / ((int)alignedLayout.byteAlignment() / (int)layout.byteAlignment());
            try (NativeScope scope = scopeFactory.make((int)alignedLayout.byteSize() * ELEMS)) {
                for (int i = 0 ; i < elems ; i++) {
                    MemoryAddress address = allocationFunction.allocate(scope, alignedLayout, value);
                    assertEquals(address.segment().byteSize(), alignedLayout.byteSize());
                    addressList.add(address);
                    VarHandle handle = handleFactory.apply(alignedLayout);
                    assertEquals(value, handle.get(address));
                    try {
                        address.segment().close();
                        fail();
                    } catch (UnsupportedOperationException uoe) {
                        //failure is expected
                        assertTrue(true);
                    }
                }
                boolean isBound = scope.byteSize().isPresent();
                try {
                    allocationFunction.allocate(scope, alignedLayout, value); //too much, should fail if bound
                    assertFalse(isBound);
                } catch (OutOfMemoryError ex) {
                    //failure is expected if bound
                    assertTrue(isBound);
                }
            }
            // addresses should be invalid now
            for (MemoryAddress address : addressList) {
                assertFalse(address.segment().isAlive());
            }
        }
    }

    static final int SIZE_256M = 1024 * 1024 * 256;

    @Test
    public void testBigAllocationInUnboundedScope() {
        try (NativeScope scope = NativeScope.unboundedScope()) {
            for (int i = 8 ; i < SIZE_256M ; i *= 8) {
                MemoryAddress address = scope.allocate(i);
                //check size
                assertEquals(address.segment().byteSize(), i);
                //check alignment
                assertTrue(address.segment().baseAddress().toRawLongValue() % i == 0);
            }
        }
    }

    @Test
    public void testAttachClose() {
        MemorySegment s1 = MemorySegment.ofArray(new byte[1]);
        MemorySegment s2 = MemorySegment.ofArray(new byte[1]);
        MemorySegment s3 = MemorySegment.ofArray(new byte[1]);
        assertTrue(s1.isAlive());
        assertTrue(s2.isAlive());
        assertTrue(s3.isAlive());
        try (NativeScope scope = NativeScope.boundedScope(10)) {
            MemorySegment ss1 = scope.register(s1);
            assertFalse(s1.isAlive());
            assertTrue(ss1.isAlive());
            s1 = ss1;
            MemorySegment ss2 = scope.register(s2);
            assertFalse(s2.isAlive());
            assertTrue(ss2.isAlive());
            s2 = ss2;
            MemorySegment ss3 = scope.register(s3);
            assertFalse(s3.isAlive());
            assertTrue(ss3.isAlive());
            s3 = ss3;
        }
        assertFalse(s1.isAlive());
        assertFalse(s2.isAlive());
        assertFalse(s3.isAlive());
    }

    @Test
    public void testNoTerminalOps() {
        try (NativeScope scope = NativeScope.boundedScope(10)) {
            MemorySegment s1 = MemorySegment.ofArray(new byte[1]);
            MemorySegment attached = scope.register(s1);
            int[] terminalOps = {CLOSE, HANDOFF};
            for (int mode : terminalOps) {
                if (attached.hasAccessModes(mode)) {
                    fail();
                }
            }
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNoReattach() {
        MemorySegment s1 = MemorySegment.ofArray(new byte[1]);
        NativeScope scope1 = NativeScope.boundedScope(10);
        NativeScope scope2 = NativeScope.boundedScope(10);
        scope2.register(scope1.register(s1));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testNullClaim() {
        NativeScope.boundedScope(10).register(null);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testNotAliveClaim() {
        MemorySegment segment = MemorySegment.ofArray(new byte[1]);
        segment.close();
        NativeScope.boundedScope(10).register(segment);
    }

    @Test
    public void testNoClaimFromWrongThread() throws InterruptedException {
        MemorySegment s = MemorySegment.ofArray(new byte[1]);
        AtomicBoolean failed = new AtomicBoolean(false);
        Thread t = new Thread(() -> {
            try {
                NativeScope.boundedScope(10).register(s);
            } catch (IllegalArgumentException ex) {
                failed.set(true);
            }
        });
        t.start();
        t.join();
        assertTrue(failed.get());
    }

    @Test
    public void testRegisterFromUnconfined() {
        MemorySegment unconfined = MemorySegment.ofNativeRestricted(MemoryAddress.ofLong(42), 10, null, null, null);
        NativeScope scope = NativeScope.boundedScope(10);
        MemorySegment registered = scope.register(unconfined);
        assertFalse(unconfined.isAlive());
        assertEquals(registered.ownerThread(), scope.ownerThread());
        scope.close();
        assertFalse(registered.isAlive());
    }

    @DataProvider(name = "nativeScopes")
    static Object[][] nativeScopes() {
        return new Object[][] {
                { (byte)42, (ScopeFactory) NativeScope::boundedScope, MemoryLayouts.BITS_8_BE, byte.class,
                        (AllocationFunction<Byte>) NativeScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(byte.class) },
                { (short)42, (ScopeFactory) NativeScope::boundedScope, MemoryLayouts.BITS_16_BE, short.class,
                        (AllocationFunction<Short>) NativeScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(short.class) },
                { 42, (ScopeFactory) NativeScope::boundedScope,
                        MemoryLayouts.BITS_32_BE, int.class,
                        (AllocationFunction<Integer>) NativeScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(int.class) },
                { 42f, (ScopeFactory) NativeScope::boundedScope, MemoryLayouts.BITS_32_BE, float.class,
                        (AllocationFunction<Float>) NativeScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(float.class) },
                { 42L, (ScopeFactory) NativeScope::boundedScope, MemoryLayouts.BITS_64_BE, long.class,
                        (AllocationFunction<Long>) NativeScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(long.class) },
                { 42d, (ScopeFactory) NativeScope::boundedScope, MemoryLayouts.BITS_64_BE, double.class,
                        (AllocationFunction<Double>) NativeScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(double.class) },
                { MemoryAddress.ofLong(42), (ScopeFactory) NativeScope::boundedScope, MemoryLayouts.BITS_64_BE, MemoryAddress.class,
                        (AllocationFunction<MemoryAddress>) NativeScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> MemoryHandles.asAddressVarHandle(l.varHandle(long.class)) },

                { (byte)42, (ScopeFactory) NativeScope::boundedScope, MemoryLayouts.BITS_8_LE, byte.class,
                        (AllocationFunction<Byte>) NativeScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(byte.class) },
                { (short)42, (ScopeFactory) NativeScope::boundedScope, MemoryLayouts.BITS_16_LE, short.class,
                        (AllocationFunction<Short>) NativeScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(short.class) },
                { 42, (ScopeFactory) NativeScope::boundedScope,
                        MemoryLayouts.BITS_32_LE, int.class,
                        (AllocationFunction<Integer>) NativeScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(int.class) },
                { 42f, (ScopeFactory) NativeScope::boundedScope, MemoryLayouts.BITS_32_LE, float.class,
                        (AllocationFunction<Float>) NativeScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(float.class) },
                { 42L, (ScopeFactory) NativeScope::boundedScope, MemoryLayouts.BITS_64_LE, long.class,
                        (AllocationFunction<Long>) NativeScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(long.class) },
                { 42d, (ScopeFactory) NativeScope::boundedScope, MemoryLayouts.BITS_64_LE, double.class,
                        (AllocationFunction<Double>) NativeScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(double.class) },
                { MemoryAddress.ofLong(42), (ScopeFactory) NativeScope::boundedScope, MemoryLayouts.BITS_64_LE, MemoryAddress.class,
                        (AllocationFunction<MemoryAddress>) NativeScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> MemoryHandles.asAddressVarHandle(l.varHandle(long.class)) },

                { (byte)42, (ScopeFactory)size -> NativeScope.unboundedScope(), MemoryLayouts.BITS_8_BE, byte.class,
                        (AllocationFunction<Byte>) NativeScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(byte.class) },
                { (short)42, (ScopeFactory)size -> NativeScope.unboundedScope(), MemoryLayouts.BITS_16_BE, short.class,
                        (AllocationFunction<Short>) NativeScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(short.class) },
                { 42, (ScopeFactory)size -> NativeScope.unboundedScope(),
                        MemoryLayouts.BITS_32_BE, int.class,
                        (AllocationFunction<Integer>) NativeScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(int.class) },
                { 42f, (ScopeFactory)size -> NativeScope.unboundedScope(), MemoryLayouts.BITS_32_BE, float.class,
                        (AllocationFunction<Float>) NativeScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(float.class) },
                { 42L, (ScopeFactory)size -> NativeScope.unboundedScope(), MemoryLayouts.BITS_64_BE, long.class,
                        (AllocationFunction<Long>) NativeScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(long.class) },
                { 42d, (ScopeFactory)size -> NativeScope.unboundedScope(), MemoryLayouts.BITS_64_BE, double.class,
                        (AllocationFunction<Double>) NativeScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(double.class) },
                { MemoryAddress.ofLong(42), (ScopeFactory)size -> NativeScope.unboundedScope(), MemoryLayouts.BITS_64_BE, MemoryAddress.class,
                        (AllocationFunction<MemoryAddress>) NativeScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> MemoryHandles.asAddressVarHandle(l.varHandle(long.class)) },

                { (byte)42, (ScopeFactory)size -> NativeScope.unboundedScope(), MemoryLayouts.BITS_8_LE, byte.class,
                        (AllocationFunction<Byte>) NativeScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(byte.class) },
                { (short)42, (ScopeFactory)size -> NativeScope.unboundedScope(), MemoryLayouts.BITS_16_LE, short.class,
                        (AllocationFunction<Short>) NativeScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(short.class) },
                { 42, (ScopeFactory)size -> NativeScope.unboundedScope(),
                        MemoryLayouts.BITS_32_LE, int.class,
                        (AllocationFunction<Integer>) NativeScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(int.class) },
                { 42f, (ScopeFactory)size -> NativeScope.unboundedScope(), MemoryLayouts.BITS_32_LE, float.class,
                        (AllocationFunction<Float>) NativeScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(float.class) },
                { 42L, (ScopeFactory)size -> NativeScope.unboundedScope(), MemoryLayouts.BITS_64_LE, long.class,
                        (AllocationFunction<Long>) NativeScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(long.class) },
                { 42d, (ScopeFactory)size -> NativeScope.unboundedScope(), MemoryLayouts.BITS_64_LE, double.class,
                        (AllocationFunction<Double>) NativeScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(double.class) },
                { MemoryAddress.ofLong(42), (ScopeFactory)size -> NativeScope.unboundedScope(), MemoryLayouts.BITS_64_LE, MemoryAddress.class,
                        (AllocationFunction<MemoryAddress>) NativeScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> MemoryHandles.asAddressVarHandle(l.varHandle(long.class)) },
        };
    }

    interface AllocationFunction<X> {
        MemoryAddress allocate(NativeScope scope, MemoryLayout layout, X value);
    }

    interface ScopeFactory {
        NativeScope make(int size);
    }
}
