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
package jdk.internal.foreign.invokers;

import jdk.internal.foreign.LayoutResolver;
import jdk.internal.foreign.Util;
import jdk.internal.foreign.abi.CallingSequence;
import jdk.internal.foreign.abi.SystemABI;
import jdk.internal.foreign.memory.*;

import java.lang.reflect.Method;
import java.foreign.NativeTypes;
import java.foreign.layout.Function;
import java.foreign.memory.Pointer;
import java.util.function.ToLongFunction;

import static sun.security.action.GetPropertyAction.privilegedGetProperty;

/**
 * This class encapsulates a given callback strategy.
 */
public abstract class UpcallHandler {

    private static final String fastPath = privilegedGetProperty("jdk.internal.foreign.UpcallHandler.FASTPATH");

    protected final Object receiver;
    protected long entryPoint = -1;
    protected final CallingSequence callingSequence;
    protected final Method fiMethod;
    protected final Function function;
    private final ToLongFunction<UpcallHandler> entryPointSupplier;


    protected UpcallHandler(CallingSequence callingSequence, Method fiMethod, Function function, Object receiver, ToLongFunction<UpcallHandler> entryPointSupplier) {
        this.fiMethod = fiMethod;
        this.function = function;
        this.callingSequence = callingSequence;
        this.receiver = receiver;
        this.entryPointSupplier = entryPointSupplier;
    }

    public static UpcallHandler of(Class<?> c, Object receiver) {
        if (!Util.isCallback(c)) {
            throw new IllegalArgumentException("Class is not a @FunctionalInterface: " + c.getName());
        }

        Method fiMethod = Util.findFunctionalInterfaceMethod(c);
        LayoutResolver resolver = LayoutResolver.get(c);
        resolver.scanMethod(fiMethod);
        Function function = resolver.resolve(Util.functionof(c));

        Util.checkCompatible(fiMethod, function);

        CallingSequence callingSequence = SystemABI.getInstance().arrangeCall(function);

        if (fastPath == null || !fastPath.equals("none")) {
            if (DirectSignatureShuffler.acceptUpcall(function, callingSequence)) {
                return new DirectUpcallHandler(callingSequence, fiMethod, function, receiver);
            } else if (fastPath != null && fastPath.equals("direct")) {
                throw new IllegalStateException(
                        String.format("No fast path for: %s = %s",
                                Util.findFunctionalInterfaceMethod(c).getName(),
                                Util.functionof(c)));
            }
        }
        return new UniversalUpcallHandler(callingSequence, fiMethod, function, receiver);
    }

    public long getNativeEntryPoint() {
        if (entryPoint == -1) {
            this.entryPoint = entryPointSupplier.applyAsLong(this);
        }
        return entryPoint;
    }

    public Object getCallbackObject() {
        return receiver;
    }

    public void free() {
        try {
            freeUpcallStub(entryPoint);
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
    }

    // natives

    static native long allocateUpcallStub(UpcallHandler handler);
    static native void freeUpcallStub(long addr);
    public static native UpcallHandler getUpcallHandler(long addr);

    private static native void registerNatives();
    static {
        registerNatives();
    }
}
