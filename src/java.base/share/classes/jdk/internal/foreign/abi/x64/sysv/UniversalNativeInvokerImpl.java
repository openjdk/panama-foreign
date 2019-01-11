package jdk.internal.foreign.abi.x64.sysv;

import jdk.internal.foreign.abi.ArgumentBinding;
import jdk.internal.foreign.abi.CallingSequence;
import jdk.internal.foreign.abi.UniversalNativeInvoker;

import java.foreign.Library;
import java.foreign.NativeMethodType;
import java.foreign.memory.LayoutType;
import java.foreign.memory.Pointer;
import java.util.List;
import java.util.function.Function;

class UniversalNativeInvokerImpl extends UniversalNativeInvoker {

    public UniversalNativeInvokerImpl(long addr, String methodName, CallingSequence callingSequence, NativeMethodType nmt) {
        super(addr, methodName, callingSequence, nmt);
    }

    public static UniversalNativeInvoker make(Library.Symbol symbol, CallingSequence callingSequence, NativeMethodType nmt) {
        try {
            return new UniversalNativeInvokerImpl(symbol.getAddress().addr(), symbol.getName(), callingSequence, nmt);
        } catch (IllegalAccessException iae) {
            throw new IllegalStateException(iae);
        }
    }

    @Override
    public void unboxValue(Object o, LayoutType<?> type, Function<ArgumentBinding, Pointer<?>> dstPtrFunc,
                           List<ArgumentBinding> bindings) throws Throwable {
        SysVx64ABI.unboxValue(o, type, dstPtrFunc, bindings);
    }

    @Override
    public Object boxValue(LayoutType<?> type, Function<ArgumentBinding, Pointer<?>> srcPtrFunc,
                           List<ArgumentBinding> bindings) throws IllegalAccessException {
        return SysVx64ABI.boxValue(type, srcPtrFunc, bindings);
    }
}
