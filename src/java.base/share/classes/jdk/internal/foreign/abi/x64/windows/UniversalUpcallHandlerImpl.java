package jdk.internal.foreign.abi.x64.windows;

import jdk.internal.foreign.abi.ArgumentBinding;
import jdk.internal.foreign.abi.CallingSequence;
import jdk.internal.foreign.abi.UniversalUpcallHandler;

import java.foreign.NativeMethodType;
import java.foreign.memory.LayoutType;
import java.foreign.memory.Pointer;
import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.function.Function;

class UniversalUpcallHandlerImpl extends UniversalUpcallHandler {

    public UniversalUpcallHandlerImpl(MethodHandle target, CallingSequence callingSequence, NativeMethodType nmt) {
        super(target, callingSequence, nmt);
    }

    @Override
    public void unboxValue(Object o, LayoutType<?> type, Function<ArgumentBinding, Pointer<?>> dstPtrFunc,
                           List<ArgumentBinding> bindings) throws Throwable {
        Windowsx64ABI.unboxValue(o, type, dstPtrFunc, bindings);
    }

    @Override
    public Object boxValue(LayoutType<?> type, Function<ArgumentBinding, Pointer<?>> srcPtrFunc,
                           List<ArgumentBinding> bindings) throws IllegalAccessException {
        return Windowsx64ABI.boxValue(type, srcPtrFunc, bindings);
    }

}
