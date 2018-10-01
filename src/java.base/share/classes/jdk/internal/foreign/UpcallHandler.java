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

    private final Object receiver;
    private final long entryPoint;
    private final MethodHandle mh;
    private final CallingSequence callingSequence;
    private final LayoutType<?> returnLayout;
    private final LayoutType<?>[] argLayouts;

    public UpcallHandler(Class<?> c, Object receiver) {
        if (!Util.isCallback(c)) {
            throw new IllegalArgumentException("Class is not a @FunctionalInterface: " + c.getName());
        }

        Method ficMethod = Util.findFunctionalInterfaceMethod(c);
        LayoutResolver resolver = LayoutResolver.get(c);
        resolver.scanMethod(ficMethod);
        Function ftype = resolver.resolve(Util.functionof(c));

        Util.checkCompatible(ficMethod, ftype);

        try {
            MethodHandle mh = MethodHandles.publicLookup().unreflect(ficMethod);
            Util.checkNoArrays(mh.type());
            this.mh = mh.asSpreader(Object[].class, ftype.argumentLayouts().size());
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }

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
        this.receiver = receiver;
        this.entryPoint = NativeInvoker.allocateUpcallStub(this);
    }

    public static void invoke(UpcallHandler handler, long integers, long vectors, long stack, long integerReturn, long vectorReturn) {
        if (DEBUG) {
            System.err.println("UpcallHandler.invoke(" + handler + ", ...)");
        }

        UpcallContext context = handler.new UpcallContext(integers, vectors, stack, integerReturn, vectorReturn);
        handler.invoke(context);
    }

    public long getNativeEntryPoint() {
        return entryPoint;
    }

    public Object getCallbackObject() {
        return receiver;
    }

    public void free() {
        try {
            NativeInvoker.freeUpcallStub(entryPoint);
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
    }

    class UpcallContext {

        private final Pointer<Long> integers;
        private final Pointer<Long> vectors;
        private final Pointer<Long> stack;
        private final Pointer<Long> integerReturns;
        private final Pointer<Long> vectorReturns;

        UpcallContext(long integers, long vectors, long stack, long integerReturn, long vectorReturn) {
            this.integers = BoundedPointer.createNativePointer(NativeTypes.UINT64, integers);
            this.vectors = BoundedPointer.createNativePointer(NativeTypes.UINT64, vectors);
            this.stack = BoundedPointer.createNativePointer(NativeTypes.UINT64, stack);
            this.integerReturns = BoundedPointer.createNativePointer(NativeTypes.UINT64, integerReturn);
            this.vectorReturns = BoundedPointer.createNativePointer(NativeTypes.UINT64, vectorReturn);
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
            assert callingSequence.returnsInMemory();
            Pointer<Long> res = getPtr(callingSequence.getReturnBindings().get(0));
            long structAddr = res.get();
            long size = Util.alignUp(returnLayout.bytesSize(), 8);
            return new BoundedPointer<Object>((LayoutType)returnLayout, new BoundedMemoryRegion(structAddr, size));
        }
    }

    private void invoke(UpcallContext context) {
        try {
            // FIXME: Handle varargs upcalls here
            if (DEBUG) {
                System.err.println("=== UpcallHandler.invoke ===");
                System.err.println(callingSequence.asString());
            }

            Object[] args = new Object[argLayouts.length];
            for (int i = 0 ; i < argLayouts.length ; i++) {
                args[i] = NativeInvoker.boxValue(argLayouts[i], context::getPtr, callingSequence.getArgumentBindings(i));
            }

            Object o = mh.invoke(receiver, args);

            if (mh.type().returnType() != void.class) {
                if (!callingSequence.returnsInMemory()) {
                    NativeInvoker.unboxValue(o, returnLayout, context::getPtr,
                            callingSequence.getReturnBindings());
                } else {
                    context.inMemoryPtr().set(o);
                }
            }
        } catch (Throwable t) {
            throw new IllegalStateException(t);
        }
    }
}
