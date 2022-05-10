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

import jdk.internal.foreign.Scoped;
import jdk.internal.foreign.abi.AbstractLinker;
import jdk.internal.foreign.abi.SharedUtils;
import jdk.internal.foreign.abi.aarch64.TypeClass;

import java.lang.foreign.Addressable;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryAddress;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.MemorySession;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SequenceLayout;
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
                    MethodType.methodType(Object.class, SegmentAllocator.class, Object[].class, InvocationData.class));
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

    private static final Map<Class<?>, MemoryAddress> CARRIER_TO_TYPE = Map.of(
        boolean.class, MemoryAddress.ofLong(ffi_type_uint8()),
        byte.class, MemoryAddress.ofLong(ffi_type_sint8()),
        short.class, MemoryAddress.ofLong(ffi_type_sint16()),
        char.class, MemoryAddress.ofLong(ffi_type_uint16()),
        int.class, MemoryAddress.ofLong(ffi_type_sint32()),
        long.class, MemoryAddress.ofLong(ffi_type_sint64()),
        float.class, MemoryAddress.ofLong(ffi_type_float()),
        double.class, MemoryAddress.ofLong(ffi_type_double()),
        MemoryAddress.class, MemoryAddress.ofLong(ffi_type_pointer()),
        Addressable.class, MemoryAddress.ofLong(ffi_type_pointer())
    );

    private static final MemoryAddress VOID_TYPE = MemoryAddress.ofLong(ffi_type_void());
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
    protected MethodHandle arrangeDowncall(MethodType inferredMethodType, FunctionDescriptor function) {
        MemorySession cifSession = MemorySession.openImplicit();
        MemorySegment cif = MemorySegment.allocateNative(sizeofCif(), cifSession);
        long allocatorSize = function.argumentLayouts().size() * ADDRESS.byteSize();
        List<MemoryLayout> argLayouts = function.argumentLayouts();
        MemorySegment argPtrs = cifSession.allocate(function.argumentLayouts().size() * ADDRESS.byteSize());
        List<TypeClass> aTypes = new ArrayList<>();
        int numScopedArgs = 0;
        for (int i = 0; i < argLayouts.size(); i++) {
            Class<?> pType = inferredMethodType.parameterType(i);
            MemoryLayout layout = argLayouts.get(i);
            allocatorSize = SharedUtils.alignUp(allocatorSize, layout.byteAlignment());
            allocatorSize += layout.byteSize();
            setAddress(argPtrs, i, toFFIType(cifSession, pType, layout));
            TypeClass aType = TypeClass.forCarrier(pType);
            if (aType == TypeClass.ADDRESS) {
                numScopedArgs++;
            }
            aTypes.add(aType);
        }

        Addressable ffiRType = inferredMethodType.returnType() != void.class
                ? toFFIType(cifSession, inferredMethodType.returnType(), function.returnLayout().orElseThrow())
                : VOID_TYPE;
        TypeClass rType = TypeClass.forCarrier(inferredMethodType.returnType());
        if (function.returnLayout().isPresent() && rType != TypeClass.SEGMENT) {
            MemoryLayout retLayout = function.returnLayout().get();
            allocatorSize = SharedUtils.alignUp(allocatorSize, retLayout.byteAlignment());
            allocatorSize += retLayout.byteSize();
        }

        checkStatus(ffi_prep_cif(cif.address().toRawLongValue(), DEFAULT_ABI, argLayouts.size(),
                ffiRType.address().toRawLongValue(), argPtrs.address().toRawLongValue()));

        InvocationData invData = new InvocationData(cif, function.returnLayout().orElse(null),
                rType, function.argumentLayouts(), List.copyOf(aTypes), allocatorSize, numScopedArgs);

        MethodHandle target = MethodHandles.insertArguments(MH_DO_DOWNCALL, 2, invData);
        target = target.asCollector(1, Object[].class, inferredMethodType.parameterCount() + 1); // +1 for address
        target = target.asType(inferredMethodType.insertParameterTypes(0, SegmentAllocator.class, Addressable.class));
        target = foldArguments(target, 1, SharedUtils.MH_CHECK_SYMBOL);
        target = SharedUtils.swapArguments(target, 0, 1); // normalize parameter order

        return target;
    }

    @Override
    protected MemorySegment arrangeUpcall(MethodHandle target, MethodType targetType, FunctionDescriptor function, MemorySession session) {
        // FIXME cheating
        return jdk.internal.foreign.abi.x64.sysv.CallArranger.arrangeUpcall(target, targetType, function, session);
    }

    private static final class ffi_type {
        private static final long SIZE_BYTES = sizeof();
        private static native long sizeof();
        private static native void setType(long ptr, short type);
        private static native void setElements(long ptr, long elements);

        static MemorySegment make(List<MemoryLayout> elements, MemorySession session) {
            MemorySegment elementsSeg = session.allocate((elements.size() + 1) * ADDRESS.byteSize());
            int i = 0;
            for (; i < elements.size(); i++) {
                MemoryLayout elementLayout = elements.get(i);
                Addressable elementType = toFFIType(session, inferCarrier(elementLayout), elementLayout);
                setAddress(elementsSeg, i, elementType);
            }
            // elements array is null-terminated
            setAddress(elementsSeg, i, MemoryAddress.NULL);

            MemorySegment ffiType = session.allocate(SIZE_BYTES);
            setType(ffiType.address().toRawLongValue(), STRUCT_TYPE);
            setElements(ffiType.address().toRawLongValue(), elementsSeg.address().toRawLongValue());

            return ffiType;
        }
    }

    private static Addressable toFFIType(MemorySession cifSession, Class<?> type, MemoryLayout layout) {
        if (layout instanceof GroupLayout grpl) {
            assert type == MemorySegment.class;
            if (grpl.isStruct()) {
                // libffi doesn't want our padding
                List<MemoryLayout> filteredLayouts = grpl.memberLayouts().stream()
                        .filter(Predicate.not(MemoryLayout::isPadding))
                        .toList();
                Addressable structType = ffi_type.make(filteredLayouts, cifSession);
                verifyStructType(grpl, filteredLayouts, structType);
                return structType;
            }
            assert grpl.isUnion();
            throw new UnsupportedOperationException("No unions (TODO)");
        } else if (layout instanceof SequenceLayout sl) {
            List<MemoryLayout> elements = Collections.nCopies(Math.toIntExact(sl.elementCount()), sl.elementLayout());
            return ffi_type.make(elements, cifSession);
        }
        return Objects.requireNonNull(CARRIER_TO_TYPE.get(type));
    }

    // verify layout against what libffi set
    private static void verifyStructType(GroupLayout grpl, List<MemoryLayout> filteredLayouts, Addressable structType) {
        try (MemorySession verifySession = MemorySession.openConfined()) {
            MemorySegment offsetsOut = MemorySegment.allocateNative(ADDRESS.byteSize() * filteredLayouts.size(), verifySession);
            checkStatus(ffi_get_struct_offsets(DEFAULT_ABI,
                    structType.address().toRawLongValue(), offsetsOut.address().toRawLongValue()));
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

        public static TypeClass forCarrier(Class<?> carrier) {
            if (carrier == boolean.class) {
                return BOOLEAN;
            } else if (carrier == byte.class) {
                return BYTE;
            } else if (carrier == short.class) {
                return SHORT;
            } else if (carrier == char.class) {
                return CHAR;
            } else if (carrier == int.class) {
                return INT;
            } else if (carrier == long.class) {
                return LONG;
            } else if (carrier == float.class) {
                return FLOAT;
            } else if (carrier == double.class) {
                return DOUBLE;
            } else if (carrier == MemoryAddress.class || carrier == Addressable.class) {
                return ADDRESS;
            } else if (carrier == MemorySegment.class) {
                return SEGMENT;
            } else if (carrier == void.class) {
                return VOID;
            }
            throw new IllegalArgumentException("Unsupported carrier: " + carrier);
        }
    }

    private record InvocationData(MemorySegment cif, MemoryLayout returnLayout, TypeClass rType,
                                  List<MemoryLayout> argLayouts, List<TypeClass> aTypes,
                                  long allocatorSize, int numScopedArgs) {}

    private static Object doDowncall(SegmentAllocator returnAllocator, Object[] args, InvocationData invData) {
        try (MemorySession session = MemorySession.openConfined()) {
            SegmentAllocator allocator = invData.allocatorSize() != 0
                ? SegmentAllocator.newNativeArena(invData.allocatorSize(), session)
                : SharedUtils.THROWING_ALLOCATOR;

            Addressable target = (Addressable) args[0];
            List<MemoryLayout> argLayouts = invData.argLayouts();
            MemorySegment argPtrs = allocator.allocate(argLayouts.size() * ADDRESS.byteSize());
            ArrayList<Scoped> scoped = new ArrayList<>(invData.numScopedArgs());
            for (int i = 0; i < argLayouts.size(); i++) {
                Object arg = args[i + 1]; // skip address
                MemoryLayout layout = argLayouts.get(i);
                MemorySegment argSeg = allocator.allocate(layout);
                switch (invData.aTypes().get(i)) {
                    case BOOLEAN -> argSeg.set((ValueLayout.OfBoolean) layout, 0, (Boolean) arg);
                    case BYTE -> argSeg.set((ValueLayout.OfByte) layout, 0, (Byte) arg);
                    case CHAR -> argSeg.set((ValueLayout.OfChar) layout, 0, (Character) arg);
                    case SHORT -> argSeg.set((ValueLayout.OfShort) layout, 0, (Short) arg);
                    case INT -> argSeg.set((ValueLayout.OfInt) layout, 0, (Integer) arg);
                    case LONG -> argSeg.set((ValueLayout.OfLong) layout, 0, (Long) arg);
                    case FLOAT -> argSeg.set((ValueLayout.OfFloat) layout, 0, (Float) arg);
                    case DOUBLE -> argSeg.set((ValueLayout.OfDouble) layout, 0, (Double) arg);
                    case ADDRESS -> {
                        Addressable addrArg = (Addressable) arg;
                        Scoped scopedArg = (Scoped) addrArg;
                        scoped.add(scopedArg);
                        scopedArg.sessionImpl().acquire0();
                        argSeg.set((ValueLayout.OfAddress) layout, 0, addrArg);
                    }
                    case SEGMENT -> argSeg.copyFrom((MemorySegment) arg); // by-value struct
                }
                setAddress(argPtrs, i, argSeg);
            }

            MemorySegment retSeg = null;
            MemoryAddress retPtr = MemoryAddress.NULL;
            if (invData.rType != TypeClass.VOID) {
                retSeg = (invData.rType() == TypeClass.SEGMENT ? returnAllocator : allocator).allocate(invData.returnLayout);
                retPtr = retSeg.address();
            }

            ffi_call(invData.cif().address().toRawLongValue(), target.address().toRawLongValue(),
                    retPtr.toRawLongValue(), argPtrs.address().toRawLongValue());

            Reference.reachabilityFence(invData.cif());

            for (Scoped scopedArg : scoped) {
                scopedArg.sessionImpl().release0();
            }

            return switch (invData.rType()) {
                case BOOLEAN -> retSeg.get((ValueLayout.OfBoolean) invData.returnLayout(), 0);
                case BYTE -> retSeg.get((ValueLayout.OfByte) invData.returnLayout(), 0);
                case CHAR -> retSeg.get((ValueLayout.OfChar) invData.returnLayout(), 0);
                case SHORT -> retSeg.get((ValueLayout.OfShort) invData.returnLayout(), 0);
                case INT -> retSeg.get((ValueLayout.OfInt) invData.returnLayout(), 0);
                case LONG -> retSeg.get((ValueLayout.OfLong) invData.returnLayout(), 0);
                case FLOAT -> retSeg.get((ValueLayout.OfFloat) invData.returnLayout(), 0);
                case DOUBLE -> retSeg.get((ValueLayout.OfDouble) invData.returnLayout(), 0);
                case ADDRESS -> retSeg.get((ValueLayout.OfAddress) invData.returnLayout(), 0);
                case SEGMENT -> retSeg;
                case VOID -> null;
            };
        }
    }

    private static void setAddress(MemorySegment segment, long index, Addressable addr) {
        PTR_ARRAY.set(segment, index, addr.address());
    }

    private static long getLong(MemorySegment segment, long index) {
        return (long) LONG_ARRAY.get(segment, index);
    }

    private static int getInt(MemorySegment segment, long index) {
        return (int) INT_ARRAY.get(segment, index);
    }

    private static native long sizeofCif();

    private static native int ffi_prep_cif(long cif, int abi, int nargs, long rtype, long atypes);
    private static native void ffi_call(long cif, long fn, long rvalue, long avalues);
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
