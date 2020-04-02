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
 * @run testng TestAllocationScope
 */

import jdk.incubator.foreign.NativeAllocationScope;
import jdk.incubator.foreign.MemoryHandles;
import jdk.incubator.foreign.MemoryLayouts;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemoryAddress;

import org.testng.annotations.*;

import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.testng.Assert.*;

public class TestAllocationScope {

    final static int ELEMS = 128;

    @Test(dataProvider = "allocationScopes")
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
            try (NativeAllocationScope scope = scopeFactory.make((int)alignedLayout.byteSize() * ELEMS)) {
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
        try (NativeAllocationScope scope = NativeAllocationScope.unboundedScope()) {
            for (int i = 8 ; i < SIZE_256M ; i *= 8) {
                MemoryAddress address = scope.allocate(i);
                //check size
                assertEquals(address.segment().byteSize(), i);
                //check alignment
                assertTrue(address.segment().baseAddress().toRawLongValue() % i == 0);
            }
        }
    }

    @DataProvider(name = "allocationScopes")
    static Object[][] allocationScopes() {
        return new Object[][] {
                { (byte)42, (ScopeFactory) NativeAllocationScope::boundedScope, MemoryLayouts.BITS_8_BE, byte.class,
                        (AllocationFunction<Byte>) NativeAllocationScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(byte.class) },
                { (short)42, (ScopeFactory) NativeAllocationScope::boundedScope, MemoryLayouts.BITS_16_BE, short.class,
                        (AllocationFunction<Short>) NativeAllocationScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(short.class) },
                { 42, (ScopeFactory) NativeAllocationScope::boundedScope,
                        MemoryLayouts.BITS_32_BE, int.class,
                        (AllocationFunction<Integer>) NativeAllocationScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(int.class) },
                { 42f, (ScopeFactory) NativeAllocationScope::boundedScope, MemoryLayouts.BITS_32_BE, float.class,
                        (AllocationFunction<Float>) NativeAllocationScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(float.class) },
                { 42L, (ScopeFactory) NativeAllocationScope::boundedScope, MemoryLayouts.BITS_64_BE, long.class,
                        (AllocationFunction<Long>) NativeAllocationScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(long.class) },
                { 42d, (ScopeFactory) NativeAllocationScope::boundedScope, MemoryLayouts.BITS_64_BE, double.class,
                        (AllocationFunction<Double>) NativeAllocationScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(double.class) },
                { MemoryAddress.ofLong(42), (ScopeFactory) NativeAllocationScope::boundedScope, MemoryLayouts.BITS_64_BE, MemoryAddress.class,
                        (AllocationFunction<MemoryAddress>) NativeAllocationScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> MemoryHandles.asAddressVarHandle(l.varHandle(long.class)) },

                { (byte)42, (ScopeFactory) NativeAllocationScope::boundedScope, MemoryLayouts.BITS_8_LE, byte.class,
                        (AllocationFunction<Byte>) NativeAllocationScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(byte.class) },
                { (short)42, (ScopeFactory) NativeAllocationScope::boundedScope, MemoryLayouts.BITS_16_LE, short.class,
                        (AllocationFunction<Short>) NativeAllocationScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(short.class) },
                { 42, (ScopeFactory) NativeAllocationScope::boundedScope,
                        MemoryLayouts.BITS_32_LE, int.class,
                        (AllocationFunction<Integer>) NativeAllocationScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(int.class) },
                { 42f, (ScopeFactory) NativeAllocationScope::boundedScope, MemoryLayouts.BITS_32_LE, float.class,
                        (AllocationFunction<Float>) NativeAllocationScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(float.class) },
                { 42L, (ScopeFactory) NativeAllocationScope::boundedScope, MemoryLayouts.BITS_64_LE, long.class,
                        (AllocationFunction<Long>) NativeAllocationScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(long.class) },
                { 42d, (ScopeFactory) NativeAllocationScope::boundedScope, MemoryLayouts.BITS_64_LE, double.class,
                        (AllocationFunction<Double>) NativeAllocationScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(double.class) },
                { MemoryAddress.ofLong(42), (ScopeFactory) NativeAllocationScope::boundedScope, MemoryLayouts.BITS_64_LE, MemoryAddress.class,
                        (AllocationFunction<MemoryAddress>) NativeAllocationScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> MemoryHandles.asAddressVarHandle(l.varHandle(long.class)) },

                { (byte)42, (ScopeFactory)size -> NativeAllocationScope.unboundedScope(), MemoryLayouts.BITS_8_BE, byte.class,
                        (AllocationFunction<Byte>) NativeAllocationScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(byte.class) },
                { (short)42, (ScopeFactory)size -> NativeAllocationScope.unboundedScope(), MemoryLayouts.BITS_16_BE, short.class,
                        (AllocationFunction<Short>) NativeAllocationScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(short.class) },
                { 42, (ScopeFactory)size -> NativeAllocationScope.unboundedScope(),
                        MemoryLayouts.BITS_32_BE, int.class,
                        (AllocationFunction<Integer>) NativeAllocationScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(int.class) },
                { 42f, (ScopeFactory)size -> NativeAllocationScope.unboundedScope(), MemoryLayouts.BITS_32_BE, float.class,
                        (AllocationFunction<Float>) NativeAllocationScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(float.class) },
                { 42L, (ScopeFactory)size -> NativeAllocationScope.unboundedScope(), MemoryLayouts.BITS_64_BE, long.class,
                        (AllocationFunction<Long>) NativeAllocationScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(long.class) },
                { 42d, (ScopeFactory)size -> NativeAllocationScope.unboundedScope(), MemoryLayouts.BITS_64_BE, double.class,
                        (AllocationFunction<Double>) NativeAllocationScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(double.class) },
                { MemoryAddress.ofLong(42), (ScopeFactory)size -> NativeAllocationScope.unboundedScope(), MemoryLayouts.BITS_64_BE, MemoryAddress.class,
                        (AllocationFunction<MemoryAddress>) NativeAllocationScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> MemoryHandles.asAddressVarHandle(l.varHandle(long.class)) },

                { (byte)42, (ScopeFactory)size -> NativeAllocationScope.unboundedScope(), MemoryLayouts.BITS_8_LE, byte.class,
                        (AllocationFunction<Byte>) NativeAllocationScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(byte.class) },
                { (short)42, (ScopeFactory)size -> NativeAllocationScope.unboundedScope(), MemoryLayouts.BITS_16_LE, short.class,
                        (AllocationFunction<Short>) NativeAllocationScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(short.class) },
                { 42, (ScopeFactory)size -> NativeAllocationScope.unboundedScope(),
                        MemoryLayouts.BITS_32_LE, int.class,
                        (AllocationFunction<Integer>) NativeAllocationScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(int.class) },
                { 42f, (ScopeFactory)size -> NativeAllocationScope.unboundedScope(), MemoryLayouts.BITS_32_LE, float.class,
                        (AllocationFunction<Float>) NativeAllocationScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(float.class) },
                { 42L, (ScopeFactory)size -> NativeAllocationScope.unboundedScope(), MemoryLayouts.BITS_64_LE, long.class,
                        (AllocationFunction<Long>) NativeAllocationScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(long.class) },
                { 42d, (ScopeFactory)size -> NativeAllocationScope.unboundedScope(), MemoryLayouts.BITS_64_LE, double.class,
                        (AllocationFunction<Double>) NativeAllocationScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> l.varHandle(double.class) },
                { MemoryAddress.ofLong(42), (ScopeFactory)size -> NativeAllocationScope.unboundedScope(), MemoryLayouts.BITS_64_LE, MemoryAddress.class,
                        (AllocationFunction<MemoryAddress>) NativeAllocationScope::allocate,
                        (Function<MemoryLayout, VarHandle>)l -> MemoryHandles.asAddressVarHandle(l.varHandle(long.class)) },
        };
    }

    interface AllocationFunction<X> {
        MemoryAddress allocate(NativeAllocationScope scope, MemoryLayout layout, X value);
    }

    interface ScopeFactory {
        NativeAllocationScope make(int size);
    }
}
