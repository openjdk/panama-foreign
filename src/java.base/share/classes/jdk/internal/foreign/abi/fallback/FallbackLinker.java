/*
 * Copyright (c) 2023, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
package jdk.internal.foreign.abi.fallback;

import jdk.internal.foreign.AbstractMemorySegmentImpl;
import jdk.internal.foreign.MemorySessionImpl;
import jdk.internal.foreign.abi.AbstractLinker;
import jdk.internal.foreign.abi.CapturableState;
import jdk.internal.foreign.abi.LinkerOptions;
import jdk.internal.foreign.abi.SharedUtils;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SegmentScope;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_LONG;
import static java.lang.invoke.MethodHandles.foldArguments;

public final class FallbackLinker extends AbstractLinker {

    private static final MethodHandle MH_DO_DOWNCALL;
    private static final MethodHandle MH_DO_UPCALL;

    static {
        try {
            MH_DO_DOWNCALL = MethodHandles.lookup().findStatic(FallbackLinker.class, "doDowncall",
                    MethodType.methodType(Object.class, SegmentAllocator.class, Object[].class, FallbackLinker.DowncallData.class));
            MH_DO_UPCALL = MethodHandles.lookup().findStatic(FallbackLinker.class, "doUpcall",
                    MethodType.methodType(void.class, MemorySegment.class, MemorySegment.class, UpcallData.class));
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static FallbackLinker getInstance() {
        class Holder {
            static final FallbackLinker INSTANCE = new FallbackLinker();
        }
        return Holder.INSTANCE;
    }

    public static boolean isSupported() {
        return LibFallback.SUPPORTED;
    }

    @Override
    protected MethodHandle arrangeDowncall(MethodType inferredMethodType, FunctionDescriptor function, LinkerOptions options) {
        List<TypeClass> aTypes = function.argumentLayouts().stream().map(TypeClass::classify).toList();
        TypeClass rType = function.returnLayout().map(TypeClass::classify).orElse(TypeClass.VOID);
        MemorySegment cif = makeCif(inferredMethodType, function, FFIABI.DEFAULT, SegmentScope.auto());

        int capturedStateMask = options.capturedCallState()
                .mapToInt(CapturableState::mask)
                .reduce(0, (a, b) -> a | b);
        DowncallData invData = new DowncallData(cif, function.returnLayout().orElse(null),
                rType, function.argumentLayouts(), aTypes, capturedStateMask);

        MethodHandle target = MethodHandles.insertArguments(MH_DO_DOWNCALL, 2, invData);

        int leadingArguments = 1; // address
        MethodType type = inferredMethodType.insertParameterTypes(0, SegmentAllocator.class, MemorySegment.class);
        if (capturedStateMask != 0) {
            leadingArguments++;
            type = type.insertParameterTypes(2, MemorySegment.class);
        }
        target = target.asCollector(1, Object[].class, inferredMethodType.parameterCount() + leadingArguments);
        target = target.asType(type);
        target = foldArguments(target, 1, SharedUtils.MH_CHECK_SYMBOL);
        target = SharedUtils.swapArguments(target, 0, 1); // normalize parameter order

        return target;
    }

    @Override
    protected MemorySegment arrangeUpcall(MethodHandle target, MethodType targetType, FunctionDescriptor function,
                                          SegmentScope scope) {
        List<TypeClass> aTypes = function.argumentLayouts().stream().map(TypeClass::classify).toList();
        TypeClass rType = function.returnLayout().map(TypeClass::classify).orElse(TypeClass.VOID);
        MemorySegment cif = makeCif(targetType, function, FFIABI.DEFAULT, scope);

        UpcallData invData = new UpcallData(target, function.returnLayout().orElse(null), rType,
                function.argumentLayouts(), aTypes);

        MethodHandle doUpcallMH = MethodHandles.insertArguments(MH_DO_UPCALL, 2, invData);
        return LibFallback.createClosure(cif, doUpcallMH, scope);
    }

    private static MemorySegment makeCif(MethodType methodType, FunctionDescriptor function, FFIABI abi, SegmentScope scope) {
        MemorySegment argTypes = MemorySegment.allocateNative(function.argumentLayouts().size() * ADDRESS.byteSize(), scope);
        List<MemoryLayout> argLayouts = function.argumentLayouts();
        for (int i = 0; i < argLayouts.size(); i++) {
            MemoryLayout layout = argLayouts.get(i);
            argTypes.setAtIndex(ADDRESS, i, FFIType.toFFIType(layout, abi, scope));
        }

        MemorySegment returnType = methodType.returnType() != void.class
                ? FFIType.toFFIType(function.returnLayout().orElseThrow(), abi, scope)
                : LibFallback.VOID_TYPE;
        return LibFallback.prepCif(returnType, argLayouts.size(), argTypes, abi, scope);
    }

    private record DowncallData(MemorySegment cif, MemoryLayout returnLayout, TypeClass rType,
                                List<MemoryLayout> argLayouts, List<TypeClass> aTypes,
                                int capturedStateMask) {}

    private static Object doDowncall(SegmentAllocator returnAllocator, Object[] args, DowncallData invData) {
        List<MemorySessionImpl> acquiredSessions = new ArrayList<>();
        try (Arena arena = Arena.openConfined()) {
            int argStart = 0;

            MemorySegment target = (MemorySegment) args[argStart++];
            MemorySessionImpl targetImpl = ((AbstractMemorySegmentImpl) target).sessionImpl();
            targetImpl.acquire0();
            acquiredSessions.add(targetImpl);

            MemorySegment capturedState = null;
            if (invData.capturedStateMask() != 0) {
                capturedState = (MemorySegment) args[argStart++];
                MemorySessionImpl capturedStateImpl = ((AbstractMemorySegmentImpl) capturedState).sessionImpl();
                capturedStateImpl.acquire0();
                acquiredSessions.add(capturedStateImpl);
            }

            List<MemoryLayout> argLayouts = invData.argLayouts();
            MemorySegment argPtrs = arena.allocate(argLayouts.size() * ADDRESS.byteSize());
            for (int i = 0; i < argLayouts.size(); i++) {
                Object arg = args[argStart + i];
                MemoryLayout layout = argLayouts.get(i);
                MemorySegment argSeg = arena.allocate(layout);
                writeValue(invData.aTypes().get(i), arg, layout, argSeg, addr -> {
                    MemorySessionImpl sessionImpl = ((AbstractMemorySegmentImpl) addr).sessionImpl();
                    sessionImpl.acquire0();
                    acquiredSessions.add(sessionImpl);
                });
                argPtrs.setAtIndex(ADDRESS, i, argSeg);
            }

            MemorySegment retSeg = null;
            if (invData.rType != TypeClass.VOID) {
                retSeg = (invData.rType() == TypeClass.SEGMENT ? returnAllocator : arena).allocate(invData.returnLayout);
            }

            LibFallback.doDowncall(invData.cif, target, retSeg, argPtrs, capturedState, invData.capturedStateMask());

            Reference.reachabilityFence(invData.cif());

            return readValue(retSeg, invData.rType(), invData.returnLayout());
        } finally {
            for (MemorySessionImpl session : acquiredSessions) {
                session.release0();
            }
        }
    }

    private record UpcallData(MethodHandle target, MemoryLayout returnLayout, TypeClass rType,
                              List<MemoryLayout> argLayouts, List<TypeClass> aTypes) {}

    private static void doUpcall(MemorySegment retPtr, MemorySegment argPtrs, UpcallData data) throws Throwable {
        List<MemoryLayout> argLayouts = data.argLayouts();
        int numArgs = argLayouts.size();
        MemoryLayout retLayout = data.returnLayout();
        try (Arena upcallArena = Arena.openConfined()) {
            MemorySegment argsSeg = MemorySegment.ofAddress(argPtrs.address(), numArgs * ADDRESS.byteSize(), upcallArena.scope());
            MemorySegment retSeg = data.rType() != TypeClass.VOID
                ? MemorySegment.ofAddress(retPtr.address(), retLayout.byteSize(), upcallArena.scope())
                : null;

            Object[] args = new Object[numArgs];
            for (int i = 0; i < numArgs; i++) {
                MemoryLayout argLayout = argLayouts.get(i);
                TypeClass argType = data.aTypes().get(i);
                MemorySegment argPtr = MemorySegment.ofAddress(argsSeg.getAtIndex(JAVA_LONG, i), argLayout.byteSize(), upcallArena.scope());

                args[i] = readValue(argPtr, argType, argLayout);
            }

            Object result = data.target().invokeWithArguments(args);

            writeValue(data.rType(), result, data.returnLayout(), retSeg);
        }
    }

    // where
    private static void writeValue(TypeClass type, Object arg, MemoryLayout layout, MemorySegment argSeg) {
        writeValue(type, arg, layout, argSeg, addr -> {});
    }

    private static void writeValue(TypeClass type, Object arg, MemoryLayout layout, MemorySegment argSeg,
                                   Consumer<MemorySegment> acquireCallback) {
        switch (type) {
            case BOOLEAN -> argSeg.set((ValueLayout.OfBoolean) layout, 0, (Boolean) arg);
            case BYTE -> argSeg.set((ValueLayout.OfByte) layout, 0, (Byte) arg);
            case CHAR -> argSeg.set((ValueLayout.OfChar) layout, 0, (Character) arg);
            case SHORT -> argSeg.set((ValueLayout.OfShort) layout, 0, (Short) arg);
            case INT -> argSeg.set((ValueLayout.OfInt) layout, 0, (Integer) arg);
            case LONG -> argSeg.set((ValueLayout.OfLong) layout, 0, (Long) arg);
            case FLOAT -> argSeg.set((ValueLayout.OfFloat) layout, 0, (Float) arg);
            case DOUBLE -> argSeg.set((ValueLayout.OfDouble) layout, 0, (Double) arg);
            case ADDRESS -> {
                MemorySegment addrArg = (MemorySegment) arg;
                acquireCallback.accept(addrArg);
                argSeg.set((ValueLayout.OfAddress) layout, 0, addrArg);
            }
            case SEGMENT -> argSeg.copyFrom((MemorySegment) arg); // by-value struct
            case VOID -> {}
        }
    }

    private static Object readValue(MemorySegment seg, TypeClass type, MemoryLayout layout) {
        return switch (type) {
            case BOOLEAN -> seg.get((ValueLayout.OfBoolean) layout, 0);
            case BYTE -> seg.get((ValueLayout.OfByte) layout, 0);
            case CHAR -> seg.get((ValueLayout.OfChar) layout, 0);
            case SHORT -> seg.get((ValueLayout.OfShort) layout, 0);
            case INT -> seg.get((ValueLayout.OfInt) layout, 0);
            case LONG -> seg.get((ValueLayout.OfLong) layout, 0);
            case FLOAT -> seg.get((ValueLayout.OfFloat) layout, 0);
            case DOUBLE -> seg.get((ValueLayout.OfDouble) layout, 0);
            case ADDRESS -> seg.get((ValueLayout.OfAddress) layout, 0);
            case SEGMENT -> seg;
            case VOID -> null;
        };
    }
}
