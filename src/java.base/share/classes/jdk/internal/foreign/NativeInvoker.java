/*
 * Copyright (c) 2016, 2018, Oracle and/or its affiliates. All rights reserved.
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
import jdk.internal.foreign.abi.ShuffleRecipe;
import jdk.internal.foreign.abi.SystemABI;
import jdk.internal.foreign.memory.BoundedPointer;
import jdk.internal.foreign.memory.LayoutTypeImpl;

import java.lang.annotation.Retention;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.foreign.*;
import java.foreign.layout.Function;
import java.foreign.layout.Layout;
import java.foreign.memory.*;
import java.foreign.memory.Pointer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static sun.security.action.GetPropertyAction.privilegedGetProperty;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class NativeInvoker {
    private static final boolean DEBUG = Boolean.parseBoolean(
        privilegedGetProperty("jdk.internal.foreign.NativeInvoker.DEBUG"));

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
    private final Type genericReturnType;
    private final MethodHandle targetMethodHandle;
    private final Function function;
    private final CallingSequence callingSequence;
    private final ShuffleRecipe shuffleRecipe;
    private final LayoutType<?> returnLayoutType;
    private final LayoutType<?>[] argLayoutTypes;

    NativeInvoker(Function function, Method method) {
        this(function, Util.methodTypeFor(method), method.isVarArgs(), INVOKE_NATIVE_MH,
                method.toString(), method.getGenericParameterTypes(), method.getGenericReturnType());
    }

    private NativeInvoker(Function function, MethodType methodType, Boolean isVarArgs, MethodHandle targetMethodHandle,
                          String debugMethodString, Type[] genericParamTypes, Type genericReturnType) {
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
        for (int i = 0 ; i < args.size() ; i++) {
            Type carrier = genericParamTypes[i];
            if (!Util.isCStruct(Util.erasure(carrier))) {
                argLayoutTypes[i] = Util.makeType(carrier, args.get(i));
            }
        }
    }

    MethodHandle getBoundMethodHandle() {
        return BRIDGE_METHOD_HANDLE.bindTo(this).asCollector(Object[].class, methodType.parameterCount())
                .asType(methodType.insertParameterTypes(0, long.class));
    }

    MethodType type() {
        return methodType;
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

        boolean isVoid = methodType.returnType() == void.class;
        int nValues = shuffleRecipe.getNoofArgumentPulls();
        long[] values = new long[nValues];
        Pointer<Long> argsPtr = nValues > 0 ?
                BoundedPointer.fromLongArray(values) :
                Pointer.nullPointer();

        for (int i = 0 ; i < args.length ; i++) {
            Object arg = args[i];
            unboxValue(arg, argLayoutTypes[i], b -> argsPtr.offset(callingSequence.storageOffset(b)),
                    callingSequence.getArgumentBindings(i));
        }

        final Pointer<?> retPtr;
        long[] returnValues = new long[shuffleRecipe.getNoofReturnPulls()];
        if (callingSequence.returnsInMemory()) {
            // FIXME (STRUCT-LIFECYCLE):
            // Leak the allocated structs for now until the life cycle has been figured out
            Scope scope = Scope.newNativeScope();
            retPtr = ((ScopeImpl)scope).allocate(returnLayoutType, 8);
            unboxValue(retPtr, NativeTypes.UINT64.pointer(), b -> argsPtr.offset(callingSequence.storageOffset(b)),
                    callingSequence.getReturnBindings());
        } else if (!isVoid) {
            retPtr = BoundedPointer.fromLongArray(returnValues);
        } else {
            retPtr = Pointer.nullPointer();
        }

        if (DEBUG) {
            System.err.println("Invoking method " + debugMethodString + " with " + values.length + " argument values");
            for (int i = 0; i < values.length; i++) {
                System.err.println("value[" + i + "] = 0x" + Long.toHexString(values[i]));
            }
        }

        targetMethodHandle.invokeExact(values, returnValues, shuffleRecipe.getRecipe(), addr);

        if (DEBUG) {
            System.err.println("Returned from method " + debugMethodString + " with " + returnValues.length + " return values");
            System.err.println("structPtr = 0x" + Long.toHexString(retPtr.addr()));
            for (int i = 0; i < returnValues.length; i++) {
                System.err.println("returnValues[" + i + "] = 0x" + Long.toHexString(returnValues[i]));
            }
        }

        if (isVoid) {
            return null;
        } else if (!callingSequence.returnsInMemory()) {
            return boxValue(returnLayoutType, b -> retPtr.offset(b.getOffset() / 8), callingSequence.getReturnBindings());
        } else {
            return retPtr.get();
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

        NativeInvoker delegate = new NativeInvoker(varargFunc, dynamicMethodType, false, targetMethodHandle,
                debugMethodString, dynamicMethodType.parameterArray(), genericReturnType);
        return delegate.invoke(addr, allArgs);
    }

    // helper routines for marshalling/unmarshalling Java values to and from registers

    static void unboxValue(Object o, LayoutType<?> type, java.util.function.Function<ArgumentBinding, Pointer<?>> dstPtrFunc,
                           List<ArgumentBinding> bindings) throws Throwable {
        if (o instanceof Struct) {
            Struct<?> struct = (Struct<?>) o;
            Pointer<Long> src = Util.unsafeCast(struct.ptr(), NativeTypes.UINT64);
            for (ArgumentBinding binding : bindings) {
                Pointer<?> dst = dstPtrFunc.apply(binding);
                Pointer<Long> srcPtr = src.offset(binding.getOffset() / NativeTypes.UINT64.bytesSize());
                Util.copy(srcPtr, dst, binding.getStorage().getSize());
            }
        } else {
            assert bindings.size() == 1;
            Pointer<?> dst = dstPtrFunc.apply(bindings.get(0));
            Util.unsafeCast(dst, type).type().setter().invoke(dst, o);
        }
    }

    static Object boxValue(LayoutType<?> type, java.util.function.Function<ArgumentBinding, Pointer<?>> srcPtrFunc,
                           List<ArgumentBinding> bindings) throws IllegalAccessException {
        Class<?> carrier = ((LayoutTypeImpl<?>)type).carrier();
        if (Util.isCStruct(carrier)) {
            /*
             * Leak memory for now
             */
            Scope scope = Scope.newNativeScope();

            @SuppressWarnings({"rawtypes", "unchecked"})
            Pointer<?> rtmp = ((ScopeImpl)scope).allocate(type, 8);

            for (ArgumentBinding binding : bindings) {
                Pointer<Long> dst = Util.unsafeCast(rtmp, NativeTypes.UINT64).offset(binding.getOffset() / NativeTypes.UINT64.bytesSize());
                Util.copy(srcPtrFunc.apply(binding), dst, binding.getStorage().getSize());
            }

            return rtmp.get();
        } else {
            assert bindings.size() == 1;
            return Util.unsafeCast(srcPtrFunc.apply(bindings.get(0)), type).get();
        }
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
