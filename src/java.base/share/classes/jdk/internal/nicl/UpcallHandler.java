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
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.nicl.NativeTypes;
import java.nicl.Scope;
import java.nicl.layout.Function;
import java.nicl.layout.Layout;
import java.nicl.types.*;
import java.nicl.types.Pointer;
import java.util.ArrayList;
import static sun.security.action.GetPropertyAction.privilegedGetProperty;

public class UpcallHandler {

    private static final boolean DEBUG = Boolean.parseBoolean(
        privilegedGetProperty("jdk.internal.nicl.UpcallHandler.DEBUG"));
    private static final LayoutType<Long> LONG_LAYOUT_TYPE = NativeTypes.UINT64;

    private static final long MAX_STACK_ARG_BYTES = 64 * 1024; // FIXME: Arbitrary limitation for now...

    private static final Object HANDLERS_LOCK = new Object();
    private static final ArrayList<UpcallHandler> ID2HANDLER = new ArrayList<>();

    private final MethodHandle mh;
    private final Function ftype;

    private final UpcallStub stub;

    static {
        long size = Util.sizeof(long.class);
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

    public static UpcallHandler make(Class<?> c, Object o) throws Throwable {
        if (!Util.isFunctionalInterface(c)) {
            throw new IllegalArgumentException("Class is not a @FunctionalInterface: " + c.getName());
        }
        if (o == null) {
            throw new NullPointerException();
        }

        if (!c.isInstance(o)) {
            throw new IllegalArgumentException("Object must implement FunctionalInterface class: " + c.getName());
        }

        Method ficMethod = Util.findFunctionalInterfaceMethod(c);
        Function ftype = Util.functionof(Util.methodTypeFor(ficMethod));
//        if (ftype instanceof jdk.internal.nicl.types.Pointer) { //???
//            ftype = ((jdk.internal.nicl.types.Pointer) ftype).getPointeeType();
//        }

        MethodType mt = MethodHandles.publicLookup().unreflect(ficMethod).type().dropParameterTypes(0, 1);

        MethodHandle mh = MethodHandles.publicLookup().findVirtual(c, "fn", mt);

        return UpcallHandler.make(mh.bindTo(o), ftype);
    }

    private static UpcallHandler make(MethodHandle mh, Function ftype) throws Throwable {
        synchronized (HANDLERS_LOCK) {
            int id = ID2HANDLER.size();
            UpcallHandler handler = new UpcallHandler(mh, ftype, id);
            ID2HANDLER.add(handler);

            if (DEBUG) {
                System.err.println("Allocated upcall handler with id " + id);
            }

            return handler;
        }
    }

    public static void invoke(int id, long integers, long vectors, long stack, long integerReturn, long vectorReturn) {
        UpcallHandler handler;

        if (DEBUG) {
            System.err.println("UpcallHandler.invoke(" + id + ", ...) with " + ID2HANDLER.size() + " stubs allocated");
        }

        synchronized (HANDLERS_LOCK) {
            handler = ID2HANDLER.get(id);
        }

        try (Scope scope = Scope.newNativeScope()) {
            UpcallContext context = new UpcallContext(scope, integers, vectors, stack, integerReturn, vectorReturn);
            handler.invoke(context);
        }
    }

    private UpcallHandler(MethodHandle mh, Function ftype, int id) throws Throwable {
        this.mh = mh;
        this.ftype = ftype;
        this.stub = new UpcallStub(id);
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
                return vectors.offset(storage.getStorageIndex() * Constants.VECTOR_REGISTER_SIZE / Util.sizeof(long.class));
            case STACK_ARGUMENT_SLOT:
                return stack.offset(storage.getStorageIndex());

            case INTEGER_RETURN_REGISTER:
                return integerReturns.offset(storage.getStorageIndex());
            case VECTOR_RETURN_REGISTER:
                return vectorReturns.offset(storage.getStorageIndex() * Constants.VECTOR_REGISTER_SIZE / Util.sizeof(long.class));
            default:
                throw new Error("Unhandled storage: " + storage);
            }
        }
    }

    private Object boxArgument(Scope scope, UpcallContext context, Struct<?>[] structs, ArgumentBinding binding) throws IllegalAccessException {
        Class<?> carrierType = binding.getMember().getCarrierType(mh.type());

        Pointer<Long> src = context.getPtr(binding.getStorage());

        if (DEBUG) {
            System.err.println("boxArgument carrier type: " + carrierType);
        }


        if (Util.isCStruct(carrierType)) {
            int index = binding.getMember().getArgumentIndex();
            Struct<?> r = structs[index];
            if (r == null) {
                /*
                 * FIXME (STRUCT-LIFECYCLE):
                 *
                 * Leak memory for now
                 */
                scope = Scope.newNativeScope();

                @SuppressWarnings({"rawtypes", "unchecked"})
                Struct<?> rtmp = scope.allocateStruct((Class)carrierType);

                structs[index] = r = rtmp;
            }

            if (DEBUG) {
                System.out.println("Populating struct at arg index " + index + " at offset 0x" + Long.toHexString(binding.getOffset()));
            }

            if ((binding.getOffset() % LONG_LAYOUT_TYPE.bytesSize()) != 0) {
                throw new Error("Invalid offset: " + binding.getOffset());
            }
            Pointer<Long> dst = r.ptr().cast(LONG_LAYOUT_TYPE).offset(binding.getOffset() / LONG_LAYOUT_TYPE.bytesSize());

            if (DEBUG) {
                System.err.println("Copying struct data, value: 0x" + Long.toHexString(src.get()));
            }

            Util.copy(src, dst, binding.getStorage().getSize());

            return r;
        } else {
            return src.cast(Util.makeType(carrierType, src.type().layout())).get();
        }
    }

    private Object[] boxArguments(Scope scope, UpcallContext context, CallingSequence callingSequence) {
        Object[] args = new Object[mh.type().parameterCount()];

        Struct<?>[] structs = new Struct<?>[mh.type().parameterCount()];

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
                        args[binding.getMember().getArgumentIndex()] = boxArgument(scope, context, structs, binding);
                    } catch (IllegalAccessException e) {
                        throw new IllegalArgumentException("Failed to box argument", e);
                    }
                });
        }

        return args;
    }

    private void copy(long bits, Pointer<Long> dst) throws IllegalAccessException {
        try (Scope scope = Scope.newNativeScope()) {
            Pointer<Long> src = Util.createArrayElementsPointer(new long[] { bits }, scope);
            Util.copy(src, dst, 8);
        }
    }

    private void unboxReturn(Class<?> c, UpcallContext context, ArgumentBinding binding, Object o) throws IllegalAccessException {
        if (DEBUG) {
            System.out.println("unboxReturn " + c.getName());
            System.out.println(binding.toString());
        }

        Pointer<Long> dst = context.getPtr(binding.getStorage());

        if (c.isPrimitive()) {
            switch (PrimitiveClassType.typeof(c)) {
                case BOOLEAN:
                    copy(((boolean) o) ? 1 : 0, dst);
                    break;

                case BYTE:
                    copy((Byte) o, dst);
                    break;

                case SHORT:
                    copy((Short) o, dst);
                    break;

                case CHAR:
                    copy((Character) o, dst);
                    break;

                case INT:
                    copy((Integer) o, dst);
                    break;

                case LONG:
                    copy((Long) o, dst);
                    break;

                case FLOAT:
                    copy(Float.floatToRawIntBits((Float) o), dst);
                    break;

                case DOUBLE:
                    copy(Double.doubleToRawLongBits((Double) o), dst);
                    break;

                case VOID:
                    throw new UnsupportedOperationException("Unhandled type: " + c.getName());
            }
        } else if (Pointer.class.isAssignableFrom(c)) {
            long addr = Util.unpack((Pointer<?>) o);
            dst.set(addr);
        } else if (Util.isCStruct(c)) {
            Function ft = Function.of(Util.typeof(c), false, new Layout[0]);
            boolean returnsInMemory = SystemABI.getInstance().arrangeCall(ft).returnsInMemory();

            Struct<?> struct = (Struct<?>) o;

            Pointer<Long> src = struct.ptr().cast(LONG_LAYOUT_TYPE);

            if (returnsInMemory) {
                // the first integer argument register contains a pointer to caller allocated struct
                long structAddr = context.getPtr(new Storage(StorageClass.INTEGER_ARGUMENT_REGISTER, 0, Constants.INTEGER_REGISTER_SIZE)).get();

                // FIXME: 32-bit support goes here
                long size = Util.alignUp(Util.sizeof(c), 8);
                Pointer<Long> dstStructPtr = new BoundedPointer<>(LONG_LAYOUT_TYPE, new BoundedMemoryRegion(structAddr, size));

                Util.copy(src, dstStructPtr, size);

                // the first integer return register needs to be populated with the (caller supplied) struct addr
                Pointer<Long> retRegPtr = context.getPtr(new Storage(StorageClass.INTEGER_RETURN_REGISTER, 0, Constants.INTEGER_REGISTER_SIZE));
                retRegPtr.set(structAddr);
            } else {
                if ((binding.getOffset() % LONG_LAYOUT_TYPE.bytesSize()) != 0) {
                    throw new Error("Invalid offset: " + binding.getOffset());
                }
                Pointer<Long> srcPtr = src.offset(binding.getOffset() / LONG_LAYOUT_TYPE.bytesSize());

                Util.copy(srcPtr, dst, binding.getStorage().getSize());
            }
        } else {
            throw new UnsupportedOperationException("Unhandled type: " + c.getName());
        }
    }

    private void invoke(UpcallContext context) {
        try (Scope scope = Scope.newNativeScope()) {
            // FIXME: Handle varargs upcalls here
            CallingSequence callingSequence = SystemABI.getInstance().arrangeCall(ftype);

            if (DEBUG) {
                System.err.println("=== UpcallHandler.invoke ===");
                System.err.println(callingSequence.asString());
            }

            Object[] args = boxArguments(scope, context, callingSequence);

            Object o = mh.asSpreader(Object[].class, args.length).invoke(args);

            if (mh.type().returnType() != void.class) {
                for (StorageClass c : Constants.RETURN_STORAGE_CLASSES) {
                    for (ArgumentBinding binding : callingSequence.getBindings(c)) {
                        unboxReturn(mh.type().returnType(), context, binding, o);
                    }
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
