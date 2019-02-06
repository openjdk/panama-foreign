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

/*
 * @test
 * @run testng PointerScopeTest
 */

import java.foreign.Libraries;
import java.foreign.Library;
import java.foreign.NativeTypes;
import java.foreign.Scope;
import java.foreign.annotations.NativeCallback;
import java.foreign.annotations.NativeFunction;
import java.foreign.annotations.NativeGetter;
import java.foreign.annotations.NativeHeader;
import java.foreign.annotations.NativeStruct;
import java.foreign.layout.Address;
import java.foreign.memory.Callback;
import java.foreign.memory.LayoutType;
import java.foreign.memory.Pointer;
import java.foreign.memory.Struct;
import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;
import java.util.function.Function;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class PointerScopeTest {

    @NativeHeader
    interface PointerScopeLib {
        @NativeFunction("(u64:u8)v")
        void ptr(Pointer<Byte> buf);
        @NativeFunction("(${pair})v")
        void pair(Pair p);
        @NativeFunction("(u64:()v)v")
        void cb(Callback<Cb> cb);

        @NativeStruct("[ i32(x) i32(y) ](pair)")
        interface Pair extends Struct<Pair> {
            @NativeGetter("x")
            int x();
            @NativeGetter("y")
            int y();
        }

        @NativeCallback("()v")
        interface Cb {
            void run();
        }
    }

    Library lib = Libraries.loadLibrary(MethodHandles.lookup(), "PointerScope");
    PointerScopeLib _ptrLib = Libraries.bind(PointerScopeLib.class, lib);
    
    //allocate pointer of given shape, and try to deref/get 

    @Test(expectedExceptions = IllegalStateException.class,
          expectedExceptionsMessageRegExp = ".*Scope is not alive")
    public void testIntDerefGet() {
        testAfterScope(s -> s.allocate(NativeTypes.INT32), Pointer::get);
    }

    @Test(expectedExceptions = IllegalStateException.class,
          expectedExceptionsMessageRegExp = ".*Scope is not alive")
    public void testPtrDerefGet() {
        testAfterScope(s -> s.allocate(NativeTypes.UINT8.pointer()), Pointer::get);
    }

    @Test(expectedExceptions = IllegalStateException.class,
          expectedExceptionsMessageRegExp = ".*Scope is not alive")
    public void testPairDerefGet() {
        testAfterScope(s -> s.allocate(LayoutType.ofStruct(PointerScopeLib.Pair.class)), Pointer::get);
    }

    @Test(expectedExceptions = IllegalStateException.class,
          expectedExceptionsMessageRegExp = ".*Scope is not alive")
    public void testArrayDerefGet() {
        testAfterScope(s -> s.allocate(NativeTypes.INT32.array(3)), Pointer::get);
    }

    @Test(expectedExceptions = IllegalStateException.class,
          expectedExceptionsMessageRegExp = ".*Scope is not alive")
    public void testCallbackDerefGet() {
        Address adrLayout = Address.ofFunction(64, java.foreign.layout.Function.ofVoid(false));
        testAfterScope(s -> s.allocate(LayoutType.ofFunction(adrLayout, PointerScopeLib.Cb.class)), Pointer::get);
    }

    //allocate pointer of given shape, and try to deref/set

    @Test(expectedExceptions = IllegalStateException.class,
          expectedExceptionsMessageRegExp = ".*Scope is not alive")
    public void testIntDerefSet() {
        testAfterScope(s -> s.allocate(NativeTypes.INT32), p -> p.set(42));
    }

    @Test(expectedExceptions = IllegalStateException.class,
          expectedExceptionsMessageRegExp = ".*Scope is not alive")
    public void testPtrDerefSet() {
        testAfterScope(s -> s.allocate(NativeTypes.UINT8.pointer()), p -> p.set(Pointer.nullPointer()));
    }

    @Test(expectedExceptions = IllegalStateException.class,
          expectedExceptionsMessageRegExp = ".*Scope is not alive")
    public void testPairDerefSet() {
        try (Scope sc = Scope.newNativeScope()) {
            testAfterScope(s -> s.allocate(LayoutType.ofStruct(PointerScopeLib.Pair.class)),
                    p -> p.set(sc.allocateStruct(PointerScopeLib.Pair.class)));
        }
    }

    @Test(expectedExceptions = IllegalStateException.class,
          expectedExceptionsMessageRegExp = ".*Scope is not alive")
    public void testArrayDerefSet() {
        try (Scope sc = Scope.newNativeScope()) {
            testAfterScope(s -> s.allocate(NativeTypes.INT32.array(3)),
                    p -> p.set(sc.allocateArray(NativeTypes.INT32, new int[] {1, 2, 3})));
        }
    }

    @Test(expectedExceptions = IllegalStateException.class,
          expectedExceptionsMessageRegExp = ".*Scope is not alive")
    public void testCallbackDerefSet() {
        try (Scope sc = Scope.newNativeScope()) {
            Address adrLayout = Address.ofFunction(64, java.foreign.layout.Function.ofVoid(false));
            testAfterScope(s -> s.allocate(LayoutType.ofFunction(adrLayout, PointerScopeLib.Cb.class)),
                    p -> p.set(sc.allocateCallback(PointerScopeLib.Cb.class, () -> {})));
        }
    }
    
    // allocate struct and try to access it
    
    @Test(expectedExceptions = IllegalStateException.class,
          expectedExceptionsMessageRegExp = ".*Scope is not alive")
    public void testPairAccess() {
        testAfterScope(s -> s.allocateStruct(PointerScopeLib.Pair.class), PointerScopeLib.Pair::x);
    }
    
    // allocate array and try to access it

    @Test(expectedExceptions = IllegalStateException.class,
          expectedExceptionsMessageRegExp = ".*Scope is not alive")
    public void testArrayAccess() {
        testAfterScope(s -> s.allocateArray(NativeTypes.INT32, new int[] { 1, 2, 3 }), a -> a.get(0));
    }
    
    // allocate callback and try to access it

    @Test(expectedExceptions = IllegalStateException.class,
          expectedExceptionsMessageRegExp = ".*Scope is not alive")
    public void testCallbackAccess() {
        testAfterScope(s -> s.allocateCallback(PointerScopeLib.Cb.class, ()->{}), cb -> getAddr(cb.entryPoint()));
    }

    //allocate pointer and pass it to function

    @Test(expectedExceptions = IllegalStateException.class,
          expectedExceptionsMessageRegExp = ".*Scope is not alive")
    public void testPtrCall() {
        testAfterScope(s -> s.allocate(NativeTypes.UINT8), _ptrLib::ptr);
    }
    
    //allocate struct and pass it to function

    @Test(expectedExceptions = IllegalStateException.class,
          expectedExceptionsMessageRegExp = ".*Scope is not alive")
    public void testPairCall() {
        testAfterScope(s -> s.allocateStruct(PointerScopeLib.Pair.class), _ptrLib::pair);
    }
    
    //allocate callback and pass it to function

    @Test(expectedExceptions = IllegalStateException.class,
          expectedExceptionsMessageRegExp = ".*Scope is not alive")
    public void testCallbackCall() {
        testAfterScope(s -> s.allocateCallback(PointerScopeLib.Cb.class, ()->{}), _ptrLib::cb);
    }

    private <Z> void testAfterScope(Function<Scope, Z> allocator, Consumer<Z> action) {
        Scope sc = Scope.newNativeScope();
        Z z = allocator.apply(sc);
        sc.close();
        action.accept(z);
    }

    private long getAddr(Pointer<?> ptr) {
        try {
            return ptr.addr();
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
