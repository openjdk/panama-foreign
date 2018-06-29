/*
 * Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved.
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
package jdk.internal.nicl;

import jdk.internal.nicl.abi.ArgumentBinding;
import jdk.internal.nicl.abi.CallingSequence;
import jdk.internal.nicl.abi.Storage;
import jdk.internal.nicl.abi.StorageClass;
import jdk.internal.nicl.abi.SystemABI;
import jdk.internal.nicl.abi.sysv.x64.Constants;
import jdk.internal.nicl.types.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.nicl.NativeTypes;
import java.nicl.Scope;
import java.nicl.layout.Function;
import java.nicl.layout.Layout;
import java.nicl.types.*;
import java.nicl.types.Pointer;
import java.util.List;

import static sun.security.action.GetPropertyAction.privilegedGetProperty;

public class UpcallHandler {

    private static final boolean DEBUG = Boolean.parseBoolean(
        privilegedGetProperty("jdk.internal.nicl.UpcallHandler.DEBUG"));
    private static final LayoutType<Long> LONG_LAYOUT_TYPE = NativeTypes.UINT64;

    private static final long MAX_STACK_ARG_BYTES = 64 * 1024; // FIXME: Arbitrary limitation for now...

    private Object receiver;
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
            return new UpcallHandlerFactory(mh.asSpreader(Object[].class, ftype.argumentLayouts().size()), ftype, ficMethod);
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
    }

    static class UpcallHandlerFactory {

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

        UpcallHandler buildHandler(Object arg) throws Throwable {
            return new UpcallHandler(arg, this);
        }
    }

    public static void invoke(UpcallHandler handler, long integers, long vectors, long stack, long integerReturn, long vectorReturn) {
        if (DEBUG) {
            System.err.println("UpcallHandler.invoke(" + handler + ", ...)");
        }

        try (Scope scope = Scope.newNativeScope()) {
            UpcallContext context = new UpcallContext(scope, integers, vectors, stack, integerReturn, vectorReturn);
            handler.invoke(context);
        }
    }

    private UpcallHandler(Object receiver, UpcallHandlerFactory factory) throws Throwable {
        this.receiver = receiver;
        this.factory = factory;
        this.stub = new UpcallStub(this);
    }

    public Pointer<?> getNativeEntryPoint() {
        return stub.getEntryPoint();
    }

    static class UpcallContext {

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

        Pointer<Long> getPtr(Storage storage) {
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
    }

    private Object boxArgument(Scope scope, UpcallContext context, Pointer<?>[] structPtrs, ArgumentBinding binding) throws IllegalAccessException {
        LayoutType<?> layoutType = factory.argLayouts[binding.getMember().getArgumentIndex()];
        Class<?> carrierClass = ((LayoutTypeImpl<?>)layoutType).carrier();

        Pointer<Long> src = context.getPtr(binding.getStorage());

        if (DEBUG) {
            System.err.println("boxArgument carrier type: " + carrierClass);
        }


        if (Util.isCStruct(carrierClass)) {
            int index = binding.getMember().getArgumentIndex();
            Pointer<?> r = structPtrs[index];
            if (r == null) {
                /*
                 * FIXME (STRUCT-LIFECYCLE):
                 *
                 * Leak memory for now
                 */
                scope = Scope.newNativeScope();

                @SuppressWarnings({"rawtypes", "unchecked"})
                Pointer<?> rtmp = ((ScopeImpl)scope).allocate(layoutType, 8);

                structPtrs[index] = r = rtmp;
            }

            if (DEBUG) {
                System.out.println("Populating struct at arg index " + index + " at offset 0x" + Long.toHexString(binding.getOffset()));
            }

            if ((binding.getOffset() % LONG_LAYOUT_TYPE.bytesSize()) != 0) {
                throw new Error("Invalid offset: " + binding.getOffset());
            }
            Pointer<Long> dst = Util.unsafeCast(r, LONG_LAYOUT_TYPE).offset(binding.getOffset() / LONG_LAYOUT_TYPE.bytesSize());

            if (DEBUG) {
                System.err.println("Copying struct data, value: 0x" + Long.toHexString(src.get()));
            }

            Util.copy(src, dst, binding.getStorage().getSize());

            return r.get();
        } else {
            return Util.unsafeCast(src, layoutType).get();
        }
    }

    private Object[] boxArguments(Scope scope, UpcallContext context, CallingSequence callingSequence) {
        Object[] args = new Object[factory.argLayouts.length];
        Pointer<?>[] structPtrs = new Pointer<?>[factory.argLayouts.length];

        if (DEBUG) {
            System.out.println("boxArguments " + callingSequence.asString());
        }

        for (StorageClass c : Constants.ARGUMENT_STORAGE_CLASSES) {
            int skip = (c == StorageClass.INTEGER_ARGUMENT_REGISTER && callingSequence.returnsInMemory()) ? 1 : 0;
            callingSequence
                .getBindings(c)
                .stream()
                .skip(skip)
                .filter(binding -> binding != null)
                .forEach(binding -> {
                    try {
                        args[binding.getMember().getArgumentIndex()] = boxArgument(scope, context, structPtrs, binding);
                    } catch (IllegalAccessException e) {
                        throw new IllegalArgumentException("Failed to box argument", e);
                    }
                });
        }

        return args;
    }

    private void unboxReturn(Class<?> c, UpcallContext context, ArgumentBinding binding, Object o) throws IllegalAccessException {
        if (DEBUG) {
            System.out.println("unboxReturn " + c.getName());
            System.out.println(binding.toString());
        }

        Pointer<Long> dst = context.getPtr(binding.getStorage());

        if (Util.isCStruct(c)) {
            Function ft = Function.of(Util.layoutof(c), false, new Layout[0]);
            boolean returnsInMemory = SystemABI.getInstance().arrangeCall(ft).returnsInMemory();

            Struct<?> struct = (Struct<?>) o;

            Pointer<Long> src = Util.unsafeCast(struct.ptr(), LONG_LAYOUT_TYPE);

            if (returnsInMemory) {
                // the first integer argument register contains a pointer to caller allocated struct
                long structAddr = context.getPtr(new Storage(StorageClass.INTEGER_ARGUMENT_REGISTER, 0, Constants.INTEGER_REGISTER_SIZE)).get();
                long size = Util.alignUp(factory.returnLayout.bytesSize(), 8);
                Pointer<?> dstStructPtr = new BoundedPointer<>(factory.returnLayout, new BoundedMemoryRegion(structAddr, size));
                try {
                    ((BoundedPointer<?>) dstStructPtr).type.setter().invoke(dstStructPtr, o);
                } catch (Throwable ex) {
                    throw new IllegalStateException(ex);
                }
            } else {
                if ((binding.getOffset() % LONG_LAYOUT_TYPE.bytesSize()) != 0) {
                    throw new Error("Invalid offset: " + binding.getOffset());
                }
                Pointer<Long> srcPtr = src.offset(binding.getOffset() / LONG_LAYOUT_TYPE.bytesSize());
                Util.copy(srcPtr, dst, binding.getStorage().getSize());
            }
        } else {
            try {
                Util.unsafeCast(dst, factory.returnLayout).type().setter().invoke(dst, o);
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    private void invoke(UpcallContext context) {
        try (Scope scope = Scope.newNativeScope()) {
            // FIXME: Handle varargs upcalls here
            CallingSequence callingSequence = factory.callingSequence;
            if (DEBUG) {
                System.err.println("=== UpcallHandler.invoke ===");
                System.err.println(callingSequence.asString());
            }

            Object[] args = boxArguments(scope, context, callingSequence);

            Object o = factory.mh.invoke(receiver, args);

            if (factory.mh.type().returnType() != void.class) {
                for (StorageClass c : Constants.RETURN_STORAGE_CLASSES) {
                    for (ArgumentBinding binding : callingSequence.getBindings(c)) {
                        unboxReturn(factory.mh.type().returnType(), context, binding, o);
                    }
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
