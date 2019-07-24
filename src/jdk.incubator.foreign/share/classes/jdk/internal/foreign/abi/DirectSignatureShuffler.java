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

package jdk.internal.foreign.abi;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemoryLayouts;
import jdk.incubator.foreign.MemorySegment;
import jdk.internal.foreign.MemoryAddressImpl;

/**
 * This class implements the shuffling logic that is required to adapt a method handle modelling a Java method into the
 * corresponding 'direct' native adapter (and viceversa, for upcalls). The shuffling is generally composed by two steps:
 * first we have to adapt incoming Java arguments into native values (or viceversa, in case of upcalls). Once that's done
 * a final permutation step is needed in order to push all the long arguments in front.
 */
public class DirectSignatureShuffler {

    private static final MethodHandle LONG_TO_BOOLEAN;
    private static final MethodHandle BOOLEAN_TO_LONG;
    private static final MethodHandle LONG_TO_POINTER;
    private static final MethodHandle POINTER_TO_LONG;
    private static final MethodHandle STRUCT_TO_LONG;
    private static final MethodHandle STRUCT_TO_DOUBLE;
    private static final MethodHandle LONG_TO_STRUCT;
    private static final MethodHandle DOUBLE_TO_STRUCT;

    private static final VarHandle BYTE_ADDR_VH;
    private static final VarHandle SHORT_ADDR_VH;
    private static final VarHandle INT_ADDR_VH;
    private static final VarHandle LONG_ADDR_VH;

    private static final VarHandle FLOAT_ADDR_VH;
    private static final VarHandle DOUBLE_ADDR_VH;

    private static final int RET_POS = -1;

    static {
        try {
            LONG_TO_BOOLEAN = MethodHandles.lookup().findStatic(DirectSignatureShuffler.class, "longToBoolean", MethodType.methodType(boolean.class, long.class));
            BOOLEAN_TO_LONG = MethodHandles.lookup().findStatic(DirectSignatureShuffler.class, "booleanToLong", MethodType.methodType(long.class, boolean.class));
            LONG_TO_POINTER = MethodHandles.lookup().findStatic(DirectSignatureShuffler.class, "longToPointer", MethodType.methodType(MemoryAddress.class, long.class));
            POINTER_TO_LONG = MethodHandles.lookup().findStatic(DirectSignatureShuffler.class, "pointerToLong", MethodType.methodType(long.class, MemoryAddress.class));
            STRUCT_TO_LONG = MethodHandles.lookup().findStatic(DirectSignatureShuffler.class, "structToLong", MethodType.methodType(long.class, VarHandle.class, MemorySegment.class));
            STRUCT_TO_DOUBLE = MethodHandles.lookup().findStatic(DirectSignatureShuffler.class, "structToDouble", MethodType.methodType(double.class, VarHandle.class, MemorySegment.class));
            LONG_TO_STRUCT = MethodHandles.lookup().findStatic(DirectSignatureShuffler.class, "longToStruct", MethodType.methodType(MemorySegment.class, long.class));
            DOUBLE_TO_STRUCT = MethodHandles.lookup().findStatic(DirectSignatureShuffler.class, "doubleToStruct", MethodType.methodType(MemorySegment.class, double.class));

            BYTE_ADDR_VH = MemoryLayouts.JAVA_BYTE.withOrder(ByteOrder.nativeOrder()).varHandle(byte.class);
            SHORT_ADDR_VH = MemoryLayouts.JAVA_SHORT.withOrder(ByteOrder.nativeOrder()).varHandle(short.class);
            INT_ADDR_VH = MemoryLayouts.JAVA_INT.withOrder(ByteOrder.nativeOrder()).varHandle(int.class);
            LONG_ADDR_VH = MemoryLayouts.JAVA_LONG.withOrder(ByteOrder.nativeOrder()).varHandle(long.class);


            FLOAT_ADDR_VH = MemoryLayouts.JAVA_FLOAT.withOrder(ByteOrder.nativeOrder()).varHandle(float.class);
            DOUBLE_ADDR_VH = MemoryLayouts.JAVA_DOUBLE.withOrder(ByteOrder.nativeOrder()).varHandle(double.class);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private final ShuffleDirection direction;
    private final MethodType javaMethodType;
    private MethodType erasedMethodType = MethodType.methodType(void.class);
    private List<UnaryOperator<MethodHandle>> adapters = new ArrayList<>();
    private List<Integer> longPerms = new ArrayList<>();
    private List<Integer> doublePerms = new ArrayList<>();

    private DirectSignatureShuffler(CallingSequence callingSequence, MethodType type, ShuffleDirection direction) {
        checkCallingSequence(callingSequence);
        this.direction = direction;
        this.javaMethodType = type;
        processType(RET_POS, type.returnType(), callingSequence.returnBindings(), direction.flip());
        for (int i = 0 ; i < javaMethodType.parameterCount() ; i++) {
            processType(i, type.parameterType(i), callingSequence.argumentBindings(i), direction);
        }
    }

    static DirectSignatureShuffler javaToNativeShuffler(CallingSequence callingSequence, MethodType type) {
        return new DirectSignatureShuffler(callingSequence, type, ShuffleDirection.JAVA_TO_NATIVE);
    }

    static DirectSignatureShuffler nativeToJavaShuffler(CallingSequence callingSequence, MethodType type) {
        return new DirectSignatureShuffler(callingSequence, type, ShuffleDirection.NATIVE_TO_JAVA);
    }

    MethodHandle adapt(MethodHandle mh) {
        if (direction == ShuffleDirection.JAVA_TO_NATIVE) {
            mh = MethodHandles.permuteArguments(mh, erasedMethodType, forwardPermutations());
        }

        for (UnaryOperator<MethodHandle> adapter : adapters) {
            mh = adapter.apply(mh);
        }

        if (direction == ShuffleDirection.NATIVE_TO_JAVA) {
            mh = MethodHandles.permuteArguments(mh, nativeMethodType(), reversePermutations());
        }
        return mh;
    }

    MethodType nativeMethodType() {
        MethodType mt = MethodType.methodType(erasedMethodType.returnType());
        mt = mt.appendParameterTypes(Collections.nCopies(longPerms.size(), long.class));
        return mt.appendParameterTypes(Collections.nCopies(doublePerms.size(), double.class));
    }

    MethodType javaMethodType() {
        return javaMethodType;
    }

    String nativeSigSuffix() {
        MethodType mt = nativeMethodType();
        return String.format("%s_%s",
                desc(mt.returnType()),
                mt.parameterCount() > 0 ?
                        mt.parameterList().stream().map(this::desc).collect(Collectors.joining()) :
                        "V");

    }

    private static void checkCallingSequence(CallingSequence callingSequence) {
        if (callingSequence.returnsInMemory() ||
                !callingSequence.bindings(StorageClass.STACK_ARGUMENT_SLOT).isEmpty()) {
            throw new IllegalArgumentException("Unsupported non-scalarized calling sequence!");
        }
    }

    private void processType(int sigPos, Class<?> carrier, List<ArgumentBinding> bindings, ShuffleDirection direction) {
        if (carrier.isPrimitive()) {
            if (carrier == long.class) {
                updateNativeMethodType(sigPos, long.class);
            } else if (carrier == double.class) {
                updateNativeMethodType(sigPos, double.class);
            } else if (carrier == float.class) {
                updateNativeMethodType(sigPos, double.class);
                adapters.add(direction.doubleAdapter(sigPos, carrier));
            } else if (carrier == boolean.class) {
                updateNativeMethodType(sigPos, long.class);
                adapters.add(direction.booleanAdapter(sigPos));
            } else if (carrier == void.class) {
                assert sigPos == -1;
            } else {
                updateNativeMethodType(sigPos, long.class);
                adapters.add(direction.longAdapter(sigPos, carrier));
            }
        } else if (carrier == MemoryAddress.class) {
            updateNativeMethodType(sigPos, long.class);
            adapters.add(direction.pointerAdapter(sigPos));
        } else if (carrier == MemorySegment.class) {
            if (bindings.size() == 1) {
                ArgumentBinding binding = bindings.get(0);
                switch (binding.storage().getStorageClass()) {
                    case INTEGER_ARGUMENT_REGISTER:
                    case INTEGER_RETURN_REGISTER:
                        updateNativeMethodType(sigPos, long.class);
                        adapters.add(direction.longStructAdapter(binding.argument().layout(), sigPos));
                        break;
                    case VECTOR_ARGUMENT_REGISTER:
                    case VECTOR_RETURN_REGISTER:
                        updateNativeMethodType(sigPos, double.class);
                        adapters.add(direction.doubleStructAdapter(binding.argument().layout(), sigPos));
                        break;
                    default:
                        //non-register bindings should have already been discarded by now
                        throw new IllegalStateException("Cannot get here!");
                }
            } else {
                throw new IllegalArgumentException("Multi-value struct!");
            }
        } else {
            throw new IllegalArgumentException("Unsupported carrier: " + carrier);
        }
    }

    private void updateNativeMethodType(int sigPos, Class<?> carrier) {
        if (sigPos == -1) {
            erasedMethodType = erasedMethodType.changeReturnType(carrier);
        } else {
            erasedMethodType = erasedMethodType.appendParameterTypes(carrier);
            if (carrier == long.class) {
                longPerms.add(sigPos);
            } else {
                doublePerms.add(sigPos);
            }
        }
    }

    private int[] forwardPermutations() {
        return Stream.concat(longPerms.stream(), doublePerms.stream())
                .mapToInt(x -> x)
                .toArray();
    }

    private int[] reversePermutations() {
        int[] forward = forwardPermutations();
        int[] reverse = new int[forward.length];
        for (int i = 0 ; i < forward.length ; i++) {
            reverse[i] = lookup(forward, i);
        }
        return reverse;
    }

    private int lookup(int[] arr, int v) {
        for (int i = 0 ; i < arr.length ; i++) {
            if (arr[i] == v) return i;
        }
        throw new IllegalStateException();
    }

    private String desc(Class<?> clazz) {
        if (clazz == long.class) {
            return "J";
        } else if (clazz == double.class) {
            return "D";
        } else if (clazz == void.class) {
            return "V";
        } else {
            throw new IllegalStateException("Unexpected class: " + clazz);
        }
    }

    // adapter helpers

    enum ShuffleDirection {
        JAVA_TO_NATIVE(5),
        NATIVE_TO_JAVA(4);

        ShuffleDirection(int maxArity) {
            this.maxArity = maxArity;
        }

        int maxArity;

        ShuffleDirection flip() {
            return this == JAVA_TO_NATIVE ? NATIVE_TO_JAVA : JAVA_TO_NATIVE;
        }

        MethodHandle filterHandle(MethodHandle mh, int pos, MethodHandle filter) {
            return pos == RET_POS ?
                    MethodHandles.filterReturnValue(mh, filter) :
                    MethodHandles.filterArguments(mh, pos, filter);
        }

        UnaryOperator<MethodHandle> longAdapter(int pos, Class<?> carrier) {
            return mh -> filterHandle(mh, pos,
                    (this == JAVA_TO_NATIVE) ? primitiveToLong(carrier) : longToPrimitive(carrier));
        }

        UnaryOperator<MethodHandle> doubleAdapter(int pos, Class<?> carrier) {
            return mh -> filterHandle(mh, pos,
                    (this == JAVA_TO_NATIVE) ? primitiveToDouble(carrier) : doubleToPrimitive(carrier));
        }

        UnaryOperator<MethodHandle> booleanAdapter(int pos) {
            return mh -> filterHandle(mh, pos,
                    (this == JAVA_TO_NATIVE) ? BOOLEAN_TO_LONG : LONG_TO_BOOLEAN);
        }

        UnaryOperator<MethodHandle> pointerAdapter(int pos) {
            return mh -> filterHandle(mh, pos,
                    (this == JAVA_TO_NATIVE) ? POINTER_TO_LONG : LONG_TO_POINTER);
        }

        UnaryOperator<MethodHandle> longStructAdapter(MemoryLayout l, int pos) {
            return mh -> filterHandle(mh, pos,
                    (this == JAVA_TO_NATIVE) ? STRUCT_TO_LONG.bindTo(varHandleForLong(l)) : LONG_TO_STRUCT);
        }

        UnaryOperator<MethodHandle> doubleStructAdapter(MemoryLayout l, int pos) {
            return mh -> filterHandle(mh, pos,
                    (this == JAVA_TO_NATIVE) ? STRUCT_TO_DOUBLE.bindTo(varHandleForDouble(l)) : DOUBLE_TO_STRUCT);
        }
    }

    static VarHandle varHandleForLong(MemoryLayout layout) {
        switch ((int) layout.bitSize()) {
            case 8: return BYTE_ADDR_VH;
            case 16: return SHORT_ADDR_VH;
            case 32: return INT_ADDR_VH;
            case 64: return LONG_ADDR_VH;
            default:
                throw new IllegalStateException("No handle for layout: " + layout);
        }
    }

    static VarHandle varHandleForDouble(MemoryLayout layout) {
        switch ((int) layout.bitSize()) {
            case 32: return FLOAT_ADDR_VH;
            case 64: return DOUBLE_ADDR_VH;
            default:
                throw new IllegalStateException("No handle for layout: " + layout);
        }
    }

    private static boolean longToBoolean(long value) {
        return value != 0;
    }

    private static long booleanToLong(boolean value) {
        return value ? 1 : 0;
    }

    private static long pointerToLong(MemoryAddress value) {
        return MemoryAddressImpl.addressof(value);
    }

    private static MemoryAddress longToPointer(long addr) {
        return addr == 0L ?
                MemoryAddressImpl.ofNull() :
                MemoryAddressImpl.ofNative(addr);
    }

    private static long structToLong(VarHandle vh, MemorySegment str) {
        return (long)vh.get(str.baseAddress());
    }

    private static double structToDouble(VarHandle vh, MemorySegment str) {
        return (double)vh.get(str.baseAddress());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static MemorySegment longToStruct(long value) {
        return MemorySegment.ofArray(new long[] { value });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static MemorySegment doubleToStruct(double value) {
        return longToStruct(Double.doubleToLongBits(value));
    }

    private static MethodHandle primitiveToLong(Class<?> carrier) {
        MethodHandle mh = MethodHandles.identity(long.class);
        return MethodHandles.explicitCastArguments(mh, MethodType.methodType(long.class, carrier));
    }

    private static MethodHandle longToPrimitive(Class<?> carrier) {
        MethodHandle mh = MethodHandles.identity(long.class);
        return MethodHandles.explicitCastArguments(mh, MethodType.methodType(carrier, long.class));
    }

    private static MethodHandle primitiveToDouble(Class<?> carrier) {
        MethodHandle mh = MethodHandles.identity(double.class);
        return MethodHandles.explicitCastArguments(mh, MethodType.methodType(double.class, carrier));
    }

    private static MethodHandle doubleToPrimitive(Class<?> carrier) {
        MethodHandle mh = MethodHandles.identity(double.class);
        return MethodHandles.explicitCastArguments(mh, MethodType.methodType(carrier, double.class));
    }

    // predicate: is fast path applicable?

    public static boolean acceptDowncall(MethodType type, CallingSequence callingSequence) {
        return accept(type, callingSequence, ShuffleDirection.JAVA_TO_NATIVE);
    }

    public static boolean acceptUpcall(MethodType type, CallingSequence callingSequence) {
        return accept(type, callingSequence, ShuffleDirection.NATIVE_TO_JAVA);
    }

    private static boolean accept(MethodType type, CallingSequence callingSequence, ShuffleDirection direction) {
        if (type.parameterCount() > direction.maxArity) return false;
        for (int i = 0 ; i < type.parameterCount(); i++) {
            List<ArgumentBinding> argumentBindings = callingSequence.argumentBindings(i);
            if (argumentBindings.size() != 1 ||
                    !isDirectBinding(argumentBindings.get(0))) {
                return false;
            }
        }

        List<ArgumentBinding> returnBindings = callingSequence.returnBindings();
        if (returnBindings.isEmpty()) {
            return true;
        } else {
            return !callingSequence.returnsInMemory() &&
                    returnBindings.size() == 1 && isDirectBinding(returnBindings.get(0));
        }
    }

    private static boolean isDirectBinding(ArgumentBinding binding) {
        switch (binding.storage().getStorageClass()) {
            case X87_RETURN_REGISTER:
            case STACK_ARGUMENT_SLOT:
            case INDIRECT_RESULT_REGISTER:
                //arguments passed in memory not supported
                return false;
            case VECTOR_ARGUMENT_REGISTER:
            case VECTOR_RETURN_REGISTER:
                //avoid passing around floats as doubles as that leads to trouble
                return binding.argument().layout().bitSize() == 64;
            case INTEGER_ARGUMENT_REGISTER:
                // On some platforms large by-ValueLayout structures are passed by
                // pointer in integer argument registers
                return (binding.argument().layout().byteSize()) <= binding.storage().getSize();
            default:
                return true;
        }
    }
}
