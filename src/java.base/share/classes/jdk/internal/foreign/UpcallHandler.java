/*
 * Copyright (c) 2015, 2018, Oracle and/or its affiliates. All rights reserved.
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
package jdk.internal.foreign;

import jdk.internal.foreign.abi.ArgumentBinding;
import jdk.internal.foreign.abi.CallingSequence;
import jdk.internal.foreign.abi.Storage;
import jdk.internal.foreign.abi.SystemABI;
import jdk.internal.foreign.abi.sysv.x64.Constants;
import jdk.internal.foreign.memory.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.foreign.NativeTypes;
import java.foreign.Scope;
import java.foreign.layout.Function;
import java.foreign.layout.Layout;
import java.foreign.memory.*;
import java.foreign.memory.Pointer;
import java.util.List;

import static sun.security.action.GetPropertyAction.privilegedGetProperty;

public class UpcallHandler {

    private static final boolean DEBUG = Boolean.parseBoolean(
        privilegedGetProperty("jdk.internal.foreign.UpcallHandler.DEBUG"));
    private static final LayoutType<Long> LONG_LAYOUT_TYPE = NativeTypes.UINT64;

    private static final long MAX_STACK_ARG_BYTES = 64 * 1024; // FIXME: Arbitrary limitation for now...

    private Callback<?> receiver;
    private final UpcallHandlerFactory factory;

    private final UpcallStub stub;

    static {
        long size = 8;
        if (Constants.STACK_SLOT_SIZE != size) {
            throw new Error("Invalid size: " + Constants.STACK_SLOT_SIZE);
        }
        if (Constants.INTEGER_REGISTER_SIZE != size) {
            throw new Error("Invalid size: " + Constants.INTEGER_REGISTER_SIZE);
        }
        if ((Constants.VECTOR_REGISTER_SIZE % size) != 0) {
            throw new Error("Invalid size: " + Constants.VECTOR_REGISTER_SIZE);
        }
    }

    public static UpcallHandlerFactory makeFactory(Class<?> c) {
        if (!Util.isCallback(c)) {
            throw new IllegalArgumentException("Class is not a @FunctionalInterface: " + c.getName());
        }

        Method ficMethod = Util.findFunctionalInterfaceMethod(c);
        LayoutResolver resolver = LayoutResolver.get(c);
        resolver.scanMethod(ficMethod);
        Function ftype = resolver.resolve(Util.functionof(c));

        try {
            MethodHandle mh = MethodHandles.publicLookup().unreflect(ficMethod);
            Util.checkNoArrays(mh.type());
            return new UpcallHandlerFactory(mh.asSpreader(Object[].class, ftype.argumentLayouts().size()), ftype, ficMethod);
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static class UpcallHandlerFactory {

        final MethodHandle mh;
        final CallingSequence callingSequence;
        final LayoutType<?> returnLayout;
        final LayoutType<?>[] argLayouts;

        UpcallHandlerFactory(MethodHandle mh, Function ftype, Method ficMethod) throws Throwable {
            this.mh = mh;
            this.callingSequence = SystemABI.getInstance().arrangeCall(ftype);
            if (ftype.returnLayout().isPresent()) {
                returnLayout = Util.makeType(ficMethod.getGenericReturnType(), ftype.returnLayout().get());
            } else {
                returnLayout = null;
            }
            List<Layout> args = ftype.argumentLayouts();
            argLayouts = new LayoutType<?>[args.size()];
            for (int i = 0 ; i < args.size() ; i++) {
                argLayouts[i] = Util.makeType(ficMethod.getGenericParameterTypes()[i], args.get(i));
            }
        }

        public UpcallHandler buildHandler(Callback<?> arg) throws Throwable {
            return new UpcallHandler(arg, this);
        }
    }

    public static void invoke(UpcallHandler handler, long integers, long vectors, long stack, long integerReturn, long vectorReturn) {
        if (DEBUG) {
            System.err.println("UpcallHandler.invoke(" + handler + ", ...)");
        }

        try (Scope scope = Scope.newNativeScope()) {
            UpcallContext context = handler.new UpcallContext(scope, integers, vectors, stack, integerReturn, vectorReturn);
            handler.invoke(context);
        }
    }

    private UpcallHandler(Callback<?> receiver, UpcallHandlerFactory factory) throws Throwable {
        this.receiver = receiver;
        this.factory = factory;
        this.stub = new UpcallStub(this);
    }

    public Pointer<?> getNativeEntryPoint() {
        return stub.getEntryPoint();
    }

    public Callback<?> getCallbackObject() {
        return receiver;
    }

    class UpcallContext {

        private final Pointer<Long> integers;
        private final Pointer<Long> vectors;
        private final Pointer<Long> stack;
        private final Pointer<Long> integerReturns;
        private final Pointer<Long> vectorReturns;

        UpcallContext(Scope scope, long integers, long vectors, long stack, long integerReturn, long vectorReturn) {
            this.integers = new BoundedPointer<>(LONG_LAYOUT_TYPE, new BoundedMemoryRegion(integers, Constants.MAX_INTEGER_ARGUMENT_REGISTERS * Constants.INTEGER_REGISTER_SIZE, scope), 0, BoundedMemoryRegion.MODE_R);
            this.vectors = new BoundedPointer<>(LONG_LAYOUT_TYPE, new BoundedMemoryRegion(vectors, Constants.MAX_VECTOR_ARGUMENT_REGISTERS * Constants.VECTOR_REGISTER_SIZE, scope), 0, BoundedMemoryRegion.MODE_R);
            this.stack = new BoundedPointer<>(LONG_LAYOUT_TYPE, new BoundedMemoryRegion(stack, MAX_STACK_ARG_BYTES, scope), 0, BoundedMemoryRegion.MODE_R);
            this.integerReturns = new BoundedPointer<>(LONG_LAYOUT_TYPE, new BoundedMemoryRegion(integerReturn, Constants.MAX_INTEGER_RETURN_REGISTERS * Constants.INTEGER_REGISTER_SIZE, scope), 0, BoundedMemoryRegion.MODE_W);
            this.vectorReturns = new BoundedPointer<>(LONG_LAYOUT_TYPE, new BoundedMemoryRegion(vectorReturn, Constants.MAX_VECTOR_RETURN_REGISTERS * Constants.VECTOR_REGISTER_SIZE, scope), 0, BoundedMemoryRegion.MODE_W);
        }

        Pointer<Long> getPtr(ArgumentBinding binding) {
            Storage storage = binding.getStorage();
            switch (storage.getStorageClass()) {
            case INTEGER_ARGUMENT_REGISTER:
                return integers.offset(storage.getStorageIndex());
            case VECTOR_ARGUMENT_REGISTER:
                return vectors.offset(storage.getStorageIndex() * Constants.VECTOR_REGISTER_SIZE / 8);
            case STACK_ARGUMENT_SLOT:
                return stack.offset(storage.getStorageIndex());

            case INTEGER_RETURN_REGISTER:
                return integerReturns.offset(storage.getStorageIndex());
            case VECTOR_RETURN_REGISTER:
                return vectorReturns.offset(storage.getStorageIndex() * Constants.VECTOR_REGISTER_SIZE / 8);
            default:
                throw new Error("Unhandled storage: " + storage);
            }
        }

        @SuppressWarnings("unchecked")
        Pointer<Object> inMemoryPtr() {
            assert factory.callingSequence.returnsInMemory();
            Pointer<Long> res = getPtr(factory.callingSequence.getReturnBindings().get(0));
            long structAddr = res.get();
            long size = Util.alignUp(factory.returnLayout.bytesSize(), 8);
            return new BoundedPointer<Object>((LayoutType)factory.returnLayout, new BoundedMemoryRegion(structAddr, size));
        }
    }

    private void invoke(UpcallContext context) {
        try {
            // FIXME: Handle varargs upcalls here
            if (DEBUG) {
                System.err.println("=== UpcallHandler.invoke ===");
                System.err.println(factory.callingSequence.asString());
            }

            Object[] args = new Object[factory.argLayouts.length];
            for (int i = 0 ; i < factory.argLayouts.length ; i++) {
                args[i] = NativeInvoker.boxValue(factory.argLayouts[i], context::getPtr, factory.callingSequence.getArgumentBindings(i));
            }

            Object o = factory.mh.invoke(receiver, args);

            if (factory.mh.type().returnType() != void.class) {
                if (!factory.callingSequence.returnsInMemory()) {
                    NativeInvoker.unboxValue(o, factory.returnLayout, context::getPtr,
                            b -> { throw new UnsupportedOperationException("Callbacks return not supported here!"); },
                            factory.callingSequence.getReturnBindings());
                } else {
                    context.inMemoryPtr().set(o);
                }
            }
        } catch (Throwable t) {
            throw new IllegalStateException(t);
        }
    }
}
