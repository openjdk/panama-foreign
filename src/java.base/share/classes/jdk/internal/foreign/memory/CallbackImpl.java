package jdk.internal.foreign.memory;

import jdk.internal.foreign.LibrariesHelper;
import jdk.internal.foreign.invokers.NativeInvoker;
import jdk.internal.foreign.invokers.UpcallHandler;

import java.foreign.memory.Callback;
import java.foreign.memory.Pointer;

public class CallbackImpl<X> implements Callback<X> {

    private final Pointer<?> addr;
    private final Class<?> funcIntfClass;

    public CallbackImpl(Pointer<?> addr, Class<?> funcIntfClass) {
        this.addr = addr;
        this.funcIntfClass = funcIntfClass;
    }

    @Override
    public Pointer<?> entryPoint() {
        return addr;
    }

    @Override
    @SuppressWarnings("unchecked")
    public X asFunction() {
        try {
            entryPoint().scope().checkAlive();
            long addr = entryPoint().addr();
            UpcallHandler handler = UpcallHandler.getUpcallHandler(addr);
            if (handler != null) {
                //shortcut - this comes from Java code!
                return (X)handler.getCallbackObject();
            } else {
                //create a wrapper around a true native function
                Class<?> callbackClass = LibrariesHelper.getCallbackImplClass(funcIntfClass);
                Pointer<?> resource = BoundedPointer.createNativeVoidPointer(entryPoint().scope(), addr);
                return (X)callbackClass.getConstructor(Pointer.class).newInstance(resource);
            }
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
    }
}
