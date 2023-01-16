/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
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
import jdk.internal.foreign.abi.Binding;
import jdk.internal.foreign.abi.CapturableState;
import jdk.internal.foreign.abi.LinkerOptions;
import jdk.internal.foreign.abi.SharedUtils;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.PaddingLayout;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SegmentScope;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.StructLayout;
import java.lang.foreign.UnionLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;
import static java.lang.invoke.MethodHandles.foldArguments;

public final class FallbackLinker extends AbstractLinker {

    private static final MethodHandle MH_DO_DOWNCALL;
    private static final VarHandle PTR_ARRAY = ADDRESS.arrayElementVarHandle().withInvokeExactBehavior();
    private static final VarHandle LONG_ARRAY = JAVA_LONG.arrayElementVarHandle().withInvokeExactBehavior();
    private static final VarHandle INT_ARRAY = JAVA_INT.arrayElementVarHandle().withInvokeExactBehavior();

    static {
        System.loadLibrary("fallbackLinker");

        try {
            MH_DO_DOWNCALL = MethodHandles.lookup().findStatic(FallbackLinker.class, "doDowncall",
                    MethodType.methodType(Object.class, SegmentAllocator.class, Object[].class, DowncallData.class));
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    private static FallbackLinker INSTANCE;


    public static FallbackLinker getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FallbackLinker();
        }
        return INSTANCE;
    }

    private static final Map<Class<?>, MemorySegment> CARRIER_TO_TYPE = Map.of(
        boolean.class, MemorySegment.ofAddress(ffi_type_uint8()),
        byte.class, MemorySegment.ofAddress(ffi_type_sint8()),
        short.class, MemorySegment.ofAddress(ffi_type_sint16()),
        char.class, MemorySegment.ofAddress(ffi_type_uint16()),
        int.class, MemorySegment.ofAddress(ffi_type_sint32()),
        long.class, MemorySegment.ofAddress(ffi_type_sint64()),
        float.class, MemorySegment.ofAddress(ffi_type_float()),
        double.class, MemorySegment.ofAddress(ffi_type_double()),
        MemorySegment.class, MemorySegment.ofAddress(ffi_type_pointer())
    );

    private static final MemorySegment VOID_TYPE = MemorySegment.ofAddress(ffi_type_void());
    private static final short STRUCT_TYPE = ffi_type_struct();

    private static final int DEFAULT_ABI = ffi_default_abi();

    /*
    typedef enum {
      FFI_OK = 0,
      FFI_BAD_TYPEDEF,
      FFI_BAD_ABI,
      FFI_BAD_ARGTYPE
    } ffi_status;
     */
    private enum ffi_status {
        FFI_OK,
        FFI_BAD_TYPEDEF,
        FFI_BAD_ABI,
        FFI_BAD_ARGTYPE;

        public static ffi_status of(int code) {
            return switch (code) {
                case 0 -> FFI_OK;
                case 1 -> FFI_BAD_TYPEDEF;
                case 2 -> FFI_BAD_ABI;
                case 3 -> FFI_BAD_ARGTYPE;
                default -> throw new IllegalArgumentException("Unknown status code: " + code);
            };
        }
    }

    @Override
    protected MethodHandle arrangeDowncall(MethodType inferredMethodType, FunctionDescriptor function, LinkerOptions options) {
        SegmentScope cifScope = SegmentScope.auto();
        List<FallbackLinker.TypeClass> aTypes = function.argumentLayouts().stream().map(TypeClass::classify).toList();
        int numScopedArgs = (int) aTypes.stream().filter(tp -> tp == TypeClass.ADDRESS).count();
        FallbackLinker.TypeClass rType = function.returnLayout().map(TypeClass::classify).orElse(TypeClass.VOID);
        long allocatorSize = computeAllocatorSize(function);
        MemorySegment cif = prepCif(inferredMethodType, function, cifScope);

        int capturedStateMask = options.capturedCallState()
                .mapToInt(CapturableState::mask)
                .reduce(0, (a, b) -> a | b);
        DowncallData invData = new DowncallData(cif, function.returnLayout().orElse(null),
                rType, function.argumentLayouts(), List.copyOf(aTypes), allocatorSize, numScopedArgs, capturedStateMask);

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

    private static long computeAllocatorSize(FunctionDescriptor function) {
        long allocatorSize = function.argumentLayouts().size() * ADDRESS.byteSize();

        for (int i = 0; i < function.argumentLayouts().size(); i++) {
            MemoryLayout layout = function.argumentLayouts().get(i);
            allocatorSize = SharedUtils.alignUp(allocatorSize, layout.byteAlignment());
            allocatorSize += layout.byteSize();
        }

        if (function.returnLayout().isPresent()
                && !(function.returnLayout().get() instanceof GroupLayout)) { // returned struct is allocated by caller
            MemoryLayout retLayout = function.returnLayout().get();
            allocatorSize = SharedUtils.alignUp(allocatorSize, retLayout.byteAlignment());
            allocatorSize += retLayout.byteSize();
        }

        return allocatorSize;
    }

    @Override
    protected MemorySegment arrangeUpcall(MethodHandle target, MethodType targetType, FunctionDescriptor function,
                                          SegmentScope scope) {
        List<FallbackLinker.TypeClass> aTypes = function.argumentLayouts().stream().map(TypeClass::classify).toList();
        FallbackLinker.TypeClass rType = function.returnLayout().map(TypeClass::classify).orElse(TypeClass.VOID);
        MemorySegment cif = prepCif(targetType, function, scope);

        UpcallData invData = new UpcallData(target, function.returnLayout().orElse(null), rType,
                function.argumentLayouts(), aTypes);

        long[] ptrs = new long[3];
        checkStatus(createClosure(cif.address(), invData, ptrs));
        long closurePtr = ptrs[0];
        long execPtr = ptrs[1];
        long globalUpdallData = ptrs[2];

        return MemorySegment.ofAddress(execPtr, 0, scope, () -> freeClosure(closurePtr, globalUpdallData));
    }

    private static MemorySegment prepCif(MethodType inferredMethodType, FunctionDescriptor function, SegmentScope cifScope) {
        MemorySegment cif = MemorySegment.allocateNative(sizeofCif(), cifScope);

        MemorySegment argPtrs = MemorySegment.allocateNative(function.argumentLayouts().size() * ADDRESS.byteSize(), cifScope);
        List<MemoryLayout> argLayouts = function.argumentLayouts();
        for (int i = 0; i < argLayouts.size(); i++) {
            Class<?> pType = inferredMethodType.parameterType(i);
            MemoryLayout layout = argLayouts.get(i);
            setAddress(argPtrs, i, toFFIType(cifScope, pType, layout));
        }

        MemorySegment ffiRType = inferredMethodType.returnType() != void.class
                ? toFFIType(cifScope, inferredMethodType.returnType(), function.returnLayout().orElseThrow())
                : VOID_TYPE;

        checkStatus(ffi_prep_cif(cif.address(), DEFAULT_ABI, argLayouts.size(),
                ffiRType.address(), argPtrs.address()));

        return cif;
    }

    private static final class ffi_type {
        private static final long SIZE_BYTES = sizeof();
        private static native long sizeof();
        private static native void setType(long ptr, short type);
        private static native void setElements(long ptr, long elements);

        static MemorySegment make(List<MemoryLayout> elements, SegmentScope scope) {
            MemorySegment elementsSeg = MemorySegment.allocateNative((elements.size() + 1) * ADDRESS.byteSize(), scope);
            int i = 0;
            for (; i < elements.size(); i++) {
                MemoryLayout elementLayout = elements.get(i);
                MemorySegment elementType = toFFIType(scope, inferCarrier(elementLayout), elementLayout);
                setAddress(elementsSeg, i, elementType);
            }
            // elements array is null-terminated
            setAddress(elementsSeg, i, MemorySegment.NULL);

            MemorySegment ffiType = MemorySegment.allocateNative(SIZE_BYTES, scope);
            setType(ffiType.address(), STRUCT_TYPE);
            setElements(ffiType.address(), elementsSeg.address());

            return ffiType;
        }
    }

    private static MemorySegment toFFIType(SegmentScope cifScope, Class<?> type, MemoryLayout layout) {
        if (layout instanceof GroupLayout grpl) {
            assert type == MemorySegment.class;
            if (grpl instanceof StructLayout) {
                // libffi doesn't want our padding
                List<MemoryLayout> filteredLayouts = grpl.memberLayouts().stream()
                        .filter(Predicate.not(PaddingLayout.class::isInstance))
                        .toList();
                MemorySegment structType = ffi_type.make(filteredLayouts, cifScope);
                verifyStructType(grpl, filteredLayouts, structType);
                return structType;
            }
            assert grpl instanceof UnionLayout;
            throw new UnsupportedOperationException("No unions (TODO)");
        } else if (layout instanceof SequenceLayout sl) {
            List<MemoryLayout> elements = Collections.nCopies(Math.toIntExact(sl.elementCount()), sl.elementLayout());
            return ffi_type.make(elements, cifScope);
        }
        return Objects.requireNonNull(CARRIER_TO_TYPE.get(type));
    }

    // verify layout against what libffi set
    private static void verifyStructType(GroupLayout grpl, List<MemoryLayout> filteredLayouts, MemorySegment structType) {
        try (Arena verifyArena = Arena.openConfined()) {
            MemorySegment offsetsOut = verifyArena.allocate(ADDRESS.byteSize() * filteredLayouts.size());
            checkStatus(ffi_get_struct_offsets(DEFAULT_ABI,
                    structType.address(), offsetsOut.address()));
            for (int i = 0; i < filteredLayouts.size(); i++) {
                MemoryLayout element = filteredLayouts.get(i);
                final int finalI = i;
                element.name().ifPresent(name -> {
                    long layoutOffset = grpl.byteOffset(MemoryLayout.PathElement.groupElement(name));
                    long ffiOffset = switch ((int) ADDRESS.bitSize()) {
                        case 64 -> getLong(offsetsOut, finalI);
                        case 32 -> getInt(offsetsOut, finalI);
                        default -> throw new IllegalStateException("Address size not supported: " + ADDRESS.byteSize());
                    };
                    if (ffiOffset != layoutOffset) {
                        throw new IllegalArgumentException("Invalid group layout." +
                                " Offset of '" + name + "': " + layoutOffset + " != " + ffiOffset);
                    }
                });
            }
        }
    }

    private static void checkStatus(int code) {
        ffi_status status = ffi_status.of(code);
        if (ffi_status.of(code) != ffi_status.FFI_OK) {
            throw new IllegalStateException("ffi_prep_cif failed with code: " + status);
        }
    }

    private static Class<?> inferCarrier(MemoryLayout element) {
        if (element instanceof ValueLayout vl) {
            return vl.carrier();
        } else if (element instanceof GroupLayout) {
            return MemorySegment.class;
        } else if (element instanceof SequenceLayout) {
            return null; // field of struct
        }
        throw new IllegalArgumentException("Can not infer carrier for: " + element);
    }

    private enum TypeClass {
        BOOLEAN,
        BYTE,
        CHAR,
        SHORT,
        INT,
        LONG,
        FLOAT,
        DOUBLE,
        ADDRESS,
        SEGMENT,
        VOID;

        public static TypeClass classify(MemoryLayout layout) {
            if (layout instanceof ValueLayout.OfBoolean) {
                return BOOLEAN;
            } else if (layout instanceof ValueLayout.OfByte) {
                return BYTE;
            } else if (layout instanceof ValueLayout.OfShort) {
                return SHORT;
            } else if (layout instanceof ValueLayout.OfChar) {
                return CHAR;
            } else if (layout instanceof ValueLayout.OfInt) {
                return INT;
            } else if (layout instanceof ValueLayout.OfLong) {
                return LONG;
            } else if (layout instanceof ValueLayout.OfFloat) {
                return FLOAT;
            } else if (layout instanceof ValueLayout.OfDouble) {
                return DOUBLE;
            } else if (layout instanceof ValueLayout.OfAddress) {
                return ADDRESS;
            } else if (layout instanceof GroupLayout) {
                return SEGMENT;
            }
            throw new IllegalArgumentException("Can not classify layout: " + layout);
        }
    }

    private record DowncallData(MemorySegment cif, MemoryLayout returnLayout, FallbackLinker.TypeClass rType,
                                List<MemoryLayout> argLayouts, List<FallbackLinker.TypeClass> aTypes,
                                long allocatorSize, int numScopedArgs, int capturedStateMask) {}

    private static Object doDowncall(SegmentAllocator returnAllocator, Object[] args, DowncallData invData) {
        List<MemorySessionImpl> acquiredSessions = new ArrayList<>(invData.numScopedArgs());
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
                setAddress(argPtrs, i, argSeg);
            }

            MemorySegment retSeg = null;
            long retPtr = 0;
            if (invData.rType != TypeClass.VOID) {
                retSeg = (invData.rType() == TypeClass.SEGMENT ? returnAllocator : arena).allocate(invData.returnLayout);
                retPtr = retSeg.address();
            }

            long capturedStateAddr = capturedState == null ? 0 : capturedState.address();
            ffi_call(invData.cif().address(), target.address(),
                    retPtr, argPtrs.address(), capturedStateAddr, invData.capturedStateMask());

            Reference.reachabilityFence(invData.cif());

            return readValue(retSeg, invData.rType(), invData.returnLayout());
        } finally {
            for (MemorySessionImpl session : acquiredSessions) {
                session.release0();
            }
        }
    }

    private static void writeValue(FallbackLinker.TypeClass type, Object arg, MemoryLayout layout, MemorySegment argSeg) {
        writeValue(type, arg, layout, argSeg, addr -> {});
    }

    private static void writeValue(FallbackLinker.TypeClass type, Object arg, MemoryLayout layout, MemorySegment argSeg,
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

    private static Object readValue(MemorySegment seg, FallbackLinker.TypeClass type, MemoryLayout layout) {
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

    private record UpcallData(MethodHandle target, MemoryLayout returnLayout, FallbackLinker.TypeClass rType,
                              List<MemoryLayout> argLayouts, List<FallbackLinker.TypeClass> aTypes) {}

    private static void doUpcall(long retPtr, long argPtrs, UpcallData data) {
        List<MemoryLayout> argLayouts = data.argLayouts();
        int numArgs = argLayouts.size();
        MemoryLayout retLayout = data.returnLayout();
        try (Arena upcallArena = Arena.openConfined()) {
            MemorySegment argsSeg = MemorySegment.ofAddress(argPtrs, numArgs * ADDRESS.byteSize(), upcallArena.scope());
            MemorySegment retSeg = data.rType() != TypeClass.VOID
                ? MemorySegment.ofAddress(retPtr, retLayout.byteSize(), upcallArena.scope())
                : null;

            Object[] args = new Object[numArgs];
            for (int i = 0; i < numArgs; i++) {
                MemoryLayout argLayout = argLayouts.get(i);
                FallbackLinker.TypeClass argType = data.aTypes().get(i);
                MemorySegment argPtr = MemorySegment.ofAddress(getLong(argsSeg, i), argLayout.byteSize(), upcallArena.scope());

                args[i] = readValue(argPtr, argType, argLayout);
            }

            Object result = data.target().invokeWithArguments(args);

            writeValue(data.rType(), result, data.returnLayout(), retSeg);
        } catch (Throwable t) {
            SharedUtils.handleUncaughtException(t);
        }
    }

    private static void setAddress(MemorySegment segment, long index, MemorySegment addr) {
        PTR_ARRAY.set(segment, index, addr);
    }

    private static long getLong(MemorySegment segment, long index) {
        return (long) LONG_ARRAY.get(segment, index);
    }

    private static int getInt(MemorySegment segment, long index) {
        return (int) INT_ARRAY.get(segment, index);
    }

    private static native long sizeofCif();

    private static native int createClosure(long cif, UpcallData invData, long[] ptrs);
    private static native void freeClosure(long closureAddress, long globalRef);

    private static native int ffi_prep_cif(long cif, int abi, int nargs, long rtype, long atypes);
    private static native void ffi_call(long cif, long fn, long rvalue, long avalues, long capturedState, int capturedStateMask);
    private static native short ffi_type_struct();
    private static native int ffi_get_struct_offsets(int abi, long type, long offsets);

    private static native int ffi_default_abi();
    private static native long ffi_type_void();
    private static native long ffi_type_uint8();
    private static native long ffi_type_sint8();
    private static native long ffi_type_uint16();
    private static native long ffi_type_sint16();
    private static native long ffi_type_uint32();
    private static native long ffi_type_sint32();
    private static native long ffi_type_uint64();
    private static native long ffi_type_sint64();
    private static native long ffi_type_float();
    private static native long ffi_type_double();
    private static native long ffi_type_pointer();
}
