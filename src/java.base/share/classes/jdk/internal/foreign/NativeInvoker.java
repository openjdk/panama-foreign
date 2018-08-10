/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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

import jdk.internal.foreign.UpcallHandler.UpcallHandlerFactory;
import jdk.internal.foreign.abi.ArgumentBinding;
import jdk.internal.foreign.abi.CallingSequence;
import jdk.internal.foreign.abi.ShuffleRecipe;
import jdk.internal.foreign.abi.StorageClass;
import jdk.internal.foreign.abi.SystemABI;
import jdk.internal.foreign.abi.sysv.x64.Constants;

import java.lang.annotation.Retention;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.ref.Reference;
import java.foreign.*;
import java.foreign.layout.Function;
import java.foreign.layout.Layout;
import java.foreign.memory.*;
import java.foreign.memory.Pointer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static sun.security.action.GetPropertyAction.privilegedGetProperty;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class NativeInvoker {
    private static final boolean DEBUG = Boolean.parseBoolean(
        privilegedGetProperty("jdk.internal.foreign.NativeInvoker.DEBUG"));

    private static final LayoutType<Long> LAYOUT_TYPE_LONG = NativeTypes.INT64;

    // Unbound MH for the invoke() method
    private static final MethodHandle BRIDGE_METHOD_HANDLE;
    private static final MethodHandle INVOKE_NATIVE_MH;

    @Retention(RUNTIME)
    private @interface InvokerMethod {
    }

    private static Method findInvokerMethod() {
        for (Method m : NativeInvoker.class.getDeclaredMethods()) {
            if (m.isAnnotationPresent(InvokerMethod.class)) {
                return m;
            }
        }

        throw new RuntimeException("Failed to locate invoke method");
    }

    static {
        try {
            MethodType INVOKE_NATIVE_MT = MethodType.methodType(void.class, long[].class, long[].class, long[].class, long.class);
            INVOKE_NATIVE_MH = MethodHandles.lookup().findStatic(NativeInvoker.class, "invokeNative", INVOKE_NATIVE_MT);
            BRIDGE_METHOD_HANDLE = MethodHandles.lookup().unreflect(findInvokerMethod());
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private final MethodType methodType;
    private final boolean isVarArgs;
    private final String debugMethodString;
    private final java.lang.reflect.Type genericReturnType;
    private final MethodHandle targetMethodHandle;
    private final Function function;
    private final CallingSequence callingSequence;
    private final ShuffleRecipe shuffleRecipe;
    private final LayoutType<?> returnLayoutType;
    private final LayoutType<?>[] argLayoutTypes;
    private final UpcallHandlerFactory[] upcallHandlers;

    NativeInvoker(Function function, MethodType methodType, Boolean isVarArgs, String debugMethodString, java.lang.reflect.Type genericReturnType) throws NoSuchMethodException, IllegalAccessException {
        this(function, methodType, isVarArgs, INVOKE_NATIVE_MH, debugMethodString, genericReturnType);
    }

    private NativeInvoker(Function function, MethodType methodType, Boolean isVarArgs, MethodHandle targetMethodHandle, String debugMethodString, java.lang.reflect.Type genericReturnType) {
        this.function = function;
        this.methodType = Util.checkNoArrays(methodType);
        this.isVarArgs = isVarArgs;
        this.targetMethodHandle = targetMethodHandle;
        this.debugMethodString = debugMethodString;
        this.genericReturnType = genericReturnType;
        this.callingSequence = SystemABI.getInstance().arrangeCall(function);
        this.shuffleRecipe = ShuffleRecipe.make(callingSequence);
        if (function.returnLayout().isPresent()) {
            returnLayoutType = Util.makeType(genericReturnType, function.returnLayout().get());
        } else {
            returnLayoutType = null;
        }
        List<Layout> args = function.argumentLayouts();
        argLayoutTypes = new LayoutType<?>[args.size()];
        upcallHandlers = new UpcallHandlerFactory[args.size()];
        for (int i = 0 ; i < args.size() ; i++) {
            Class<?> carrier = methodType.parameterType(i);
            if (Util.isCallback(carrier)) {
                upcallHandlers[i] = UpcallHandler.makeFactory(carrier);
            } else if (!Util.isCStruct(carrier)) {
                argLayoutTypes[i] = Util.makeType(carrier, args.get(i));
            }
        }
    }

    MethodHandle getBoundMethodHandle() {
        return BRIDGE_METHOD_HANDLE.bindTo(this).asCollector(Object[].class, methodType.parameterCount())
                .asType(methodType.insertParameterTypes(0, long.class));
    }

    private Class<?> computeClass(Class<?> c) {
        if (c.isPrimitive()) {
            throw new IllegalArgumentException("Not expecting primitive type " + c.getName());
        }

        if (c == Byte.class || c == Short.class || c == Character.class || c == Integer.class || c == Long.class) {
            return long.class;
        } else if (c == Float.class || c == Double.class) {
            return double.class;
        } else if (Pointer.class.isAssignableFrom(c)) {
            return Pointer.class;
        } else {
            throw new UnsupportedOperationException("Type unhandled: " + c.getName());
        }
    }

    private MethodType getDynamicMethodType(MethodType baseMethodType, Object[] unnamedArgs) {
        ArrayList<Class<?>> types = new ArrayList<>();

        Class<?>[] staticArgTypes = baseMethodType.parameterArray();

        // skip trailing Object[]
        for (int i = 0; i < staticArgTypes.length - 1; i++) {
            types.add(staticArgTypes[i]);
        }

        for (Object o : unnamedArgs) {
            types.add(computeClass(o.getClass()));
        }

        return MethodType.methodType(baseMethodType.returnType(), types.toArray(new Class<?>[0]));
    }

    private void dumpArgs(String debugMethodString, boolean isVarArgs, Object[] args) {
        System.err.println("invoking method " + debugMethodString);

        int nNamedArgs = args.length - (isVarArgs ? 1 : 0);
        for (int i = 0; i < nNamedArgs; i++) {
            System.err.println("    named arg: " + args[i]);
        }

        if (isVarArgs) {
            for (Object o : (Object[]) args[args.length - 1]) {
                System.err.println("  unnamed args: " + o);
            }
        }
    }


    @InvokerMethod
    private Object invoke(long addr, Object[] args) throws Throwable {
        if (DEBUG) {
            dumpArgs(debugMethodString, isVarArgs, args);
        }

        if (isVarArgs) {
            return invokeVarargs(addr, args);
        } else {
            return invokeNormal(addr, args);
        }
    }

    private Object invokeNormal(long addr, Object[] args) throws Throwable {
        //Fixme: this should use the function field, not creating the layout via reflection!!!


        Pointer<?> returnStructPtr = null;
        if (Util.isCStruct(methodType.returnType())) {
            // FIXME (STRUCT-LIFECYCLE):
            // Leak the allocated structs for now until the life cycle has been figured out
            Scope scope = Scope.newNativeScope();

            Class<?> c = methodType.returnType();
            @SuppressWarnings("unchecked")
            Pointer<?> r = ((ScopeImpl)scope).allocate(returnLayoutType, 8);

            returnStructPtr = r;
        }

        // we need to keep upcall handlers alive until native invocation completes, otherwise the native stub
        // is going to attempt an upcall using an already GC'ed handler instance. We do this by saving all handlers
        // into this local list.
        List<UpcallHandler> upcallHandlers = new ArrayList<>();

        int nValues = shuffleRecipe.getNoofArgumentPulls();
        long[] values = new long[nValues];
        if (nValues != 0) {
            int curValueArrayIndex = 0;
            for (StorageClass c : Constants.ARGUMENT_STORAGE_CLASSES) {
                for (ArgumentBinding binding : callingSequence.getBindings(c)) {
                    if (binding == null) {
                        // stack slot padding/alignment
                        continue;
                    }

                    if (c == StorageClass.INTEGER_ARGUMENT_REGISTER &&
                        callingSequence.returnsInMemory() &&
                        binding.getStorage().getStorageIndex() == 0) {
                        values[curValueArrayIndex] = returnStructPtr.addr();
                        curValueArrayIndex++;
                    } else {
                        long n = binding.getStorage().getSize() / 8;

                        int argIndex = binding.getMember().getArgumentIndex();
                        Class<?> argCarrier = methodType.parameterType(argIndex);
                        long[] argValues = getArgumentValues(argCarrier, binding, n, args[argIndex], upcallHandlers);
                        if (n != argValues.length) {
                            throw new InternalError("carrierType: " + argCarrier + " binding: " + binding + " n: " + n + " argValues.length: " + argValues.length);
                        }
                        System.arraycopy(argValues, 0, values, curValueArrayIndex, (int)n);
                        curValueArrayIndex += n;
                    }
                }
            }
        }

        if (DEBUG) {
            System.err.println("Invoking method " + debugMethodString + " with " + values.length + " argument values");
            for (int i = 0; i < values.length; i++) {
                System.err.println("value[" + i + "] = 0x" + Long.toHexString(values[i]));
            }
        }

        long[] returnValues = new long[shuffleRecipe.getNoofReturnPulls()];

        targetMethodHandle.invokeExact(values, returnValues, shuffleRecipe.getRecipe(), addr);

        //defeat JIT dead code analysis
        Reference.reachabilityFence(upcallHandlers);

        if (DEBUG) {
            System.err.println("Returned from method " + debugMethodString + " with " + returnValues.length + " return values");
            for (int i = 0; i < returnValues.length; i++) {
                System.err.println("returnValues[" + i + "] = 0x" + Long.toHexString(returnValues[i]));
            }
        }

        return processReturnValue(callingSequence, returnValues, returnStructPtr);
    }

    private Object processReturnValue(CallingSequence callingSequence, long[] returnValues, Pointer<?> returnStructPtr) {
        if (methodType.returnType() == void.class) {
            return null;
        } else if (Util.isCStruct(methodType.returnType())) {
            if (!callingSequence.returnsInMemory()) {
                int curValueArrayIndex = 0;

                for (StorageClass c : Constants.RETURN_STORAGE_CLASSES) {
                    for (ArgumentBinding binding : callingSequence.getBindings(c)) {
                        assert binding != null;
                        assert returnStructPtr != null;

                        int n = (int)(binding.getStorage().getSize() / 8);
                        copyStructReturnValue(returnStructPtr, binding.getOffset(), returnValues, curValueArrayIndex, n);
                        curValueArrayIndex += n;
                    }
                }
            }

            return returnStructPtr.get();
        } else {
            Pointer<Long> ptr = Scope.newHeapScope().allocate(NativeTypes.INT64);
            ptr.set(returnValues[0]);
            try {
                return returnLayoutType.getter().invoke(Util.unsafeCast(ptr, returnLayoutType));
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    private Pointer<Long> rawStructPointer(Pointer<?> structPtr, long offset) {
        if ((offset % LAYOUT_TYPE_LONG.bytesSize()) != 0) {
            throw new IllegalArgumentException("Invalid offset: " + offset);
        }
        return Util.unsafeCast(structPtr, LAYOUT_TYPE_LONG).offset(offset / LAYOUT_TYPE_LONG.bytesSize());
    }

    public void rawStructWrite(Pointer<?> structPtr, long offset, long value) {
        rawStructPointer(structPtr, offset).set(value);
    }

    private void copyStructReturnValue(Pointer<?> structPtr, long structOffset, long[] returnValues, int curReturnValuesIndex, int n) {
        for (int i = 0; i < n; i++) {
            long offs = structOffset + i * 8;
            rawStructWrite(structPtr, offs, returnValues[curReturnValuesIndex]);
            curReturnValuesIndex++;
        }
    }

    private long[] getArgumentValues(Class<?> carrierType, ArgumentBinding binding, long n, Object arg, List<UpcallHandler> handlers) throws Throwable {
        if (Util.isCStruct(carrierType)) {
            long[] values = new long[(int)n];
            Struct<?> r = (Struct<?>)arg;

            Pointer<Long> src = Util.unsafeCast(Util.unsafeCast(r.ptr(), NativeTypes.UINT8).offset(binding.getOffset()), NativeTypes.UINT64);
            for (int i = 0; i < n; i++) {
                values[i] = src.offset(i).get();
            }
            return values;
        } else if (Util.isCallback(carrierType)) {
            UpcallHandler handler = upcallHandlers[binding.getMember().getArgumentIndex()].buildHandler((Callback<?>)arg);
            handlers.add(handler);
            return new long[] { handler.getNativeEntryPoint().addr() };
        } else {
            Pointer<Long> ptr = Scope.newHeapScope().allocate(NativeTypes.INT64);
            Util.unsafeCast(ptr, argLayoutTypes[binding.getMember().getArgumentIndex()])
                    .type().setter().invoke(ptr, arg);
            return new long[] { ptr.get() };
        }
    }

    private Object invokeVarargs(long addr, Object[] args) throws Throwable {
        // one trailing Object[]
        int nNamedArgs = methodType.parameterCount() - 1;
        Object[] unnamedArgs = (Object[]) args[args.length - 1];

        // flatten argument list so that it can be passed to an asSpreader MH
        Object[] allArgs = new Object[nNamedArgs + unnamedArgs.length];
        System.arraycopy(args, 0, allArgs, 0, nNamedArgs);
        System.arraycopy(unnamedArgs, 0, allArgs, nNamedArgs, unnamedArgs.length);

        //we need to infer layouts for all unnamed arguments
        Layout[] argLayouts = Stream.concat(function.argumentLayouts().stream(),
                Stream.of(unnamedArgs).map(Object::getClass).map(Util::variadicLayout))
                .toArray(Layout[]::new);

        Function varargFunc = function.returnLayout().isPresent() ?
                Function.of(function.returnLayout().get(), false, argLayouts) :
                Function.ofVoid(false, argLayouts);

        MethodType dynamicMethodType = getDynamicMethodType(methodType, unnamedArgs);

        NativeInvoker delegate = new NativeInvoker(varargFunc, dynamicMethodType, false, targetMethodHandle, debugMethodString, genericReturnType);
        return delegate.invoke(addr, allArgs);
    }

    //natives

    static native void invokeNative(long[] args, long[] rets, long[] recipe, long addr);
    static native long allocateUpcallStub(UpcallHandler handler);
    static native void freeUpcallStub(long addr);
    public static native UpcallHandler getUpcallHandler(long addr);

    private static native void registerNatives();
    static {
        registerNatives();
    }
}
