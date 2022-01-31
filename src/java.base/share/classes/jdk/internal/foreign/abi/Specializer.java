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
package jdk.internal.foreign.abi;

import jdk.internal.foreign.MemoryAddressImpl;
import jdk.internal.foreign.ResourceScopeImpl;
import jdk.internal.misc.VM;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.ConstantDynamic;
import jdk.internal.org.objectweb.asm.Handle;
import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.util.CheckClassAdapter;
import sun.security.action.GetBooleanAction;
import sun.security.action.GetPropertyAction;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.constant.ConstantDescs;
import java.lang.foreign.Addressable;
import java.lang.foreign.MemoryAddress;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ResourceScope;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static java.lang.invoke.MethodType.methodType;
import static jdk.internal.org.objectweb.asm.Opcodes.*;

public class Specializer {
    private static final String DUMP_CLASSES_DIR
        = GetPropertyAction.privilegedGetProperty("jdk.internal.foreign.abi.Specializer.DUMP_CLASSES_DIR");
    private static final boolean PERFORM_VERIFICATION
        = GetBooleanAction.privilegedGetProperty("jdk.internal.foreign.abi.Specializer.PERFORM_VERIFICATION");

    private static final int CLASSFILE_VERSION = VM.classFileVersion();

    private static final String OBJECT_DESC = Object.class.descriptorString();
    private static final String OBJECT_INTRN = Type.getInternalName(Object.class);

    private static final String BINDING_CONTEXT_DESC = Binding.Context.class.descriptorString();
    private static final String BINDING_CONTEXT_INTRN = Type.getInternalName(Binding.Context.class);
    private static final String OF_BOUNDED_ALLOCATOR_NAME = "ofBoundedAllocator";
    private static final String OF_BOUNDED_ALLOCATOR_DESC = methodType(Binding.Context.class, long.class).descriptorString();
    private static final String OF_SCOPE_NAME = "ofScope";
    private static final String OF_SCOPE_DESC = methodType(Binding.Context.class).descriptorString();
    private static final String DUMMY_CONTEXT_NAME = "DUMMY";
    private static final String ALLOCATOR_NAME = "allocator";
    private static final String ALLOCATOR_DESC = methodType(SegmentAllocator.class).descriptorString();
    private static final String SCOPE_NAME = "scope";
    private static final String SCOPE_DESC = methodType(ResourceScope.class).descriptorString();
    private static final String CLOSE_NAME = "close";
    private static final String CLOSE_DESC = methodType(void.class).descriptorString();

    private static final String ADDRESSABLE_INTRN = Type.getInternalName(Addressable.class);
    private static final String ADDRESS_NAME = "address";
    private static final String ADDRESS_DESC = methodType(MemoryAddress.class).descriptorString();

    private static final String MEMORY_SEGMENT_INTRN = Type.getInternalName(MemorySegment.class);
    private static final String GET_NAME = "get";
    private static final String SET_NAME = "set";
    private static final String COPY_NAME = "copy";
    private static final String COPY_DESC = methodType(void.class, MemorySegment.class, long.class, MemorySegment.class, long.class, long.class).descriptorString();

    private static final String MEMORY_ADDRESS_INTRN = Type.getInternalName(MemoryAddress.class);
    private static final String TO_RAW_LONG_VALUE_NAME = "toRawLongValue";
    private static final String TO_RAW_LONG_VALUE_DESC = methodType(long.class).descriptorString();
    private static final String OF_LONG_NAME = "ofLong";
    private static final String OF_LONG_DESC = methodType(MemoryAddress.class, long.class).descriptorString();

    private static final String MEMORY_ADDRESS_IMPL_INTRN = Type.getInternalName(MemoryAddressImpl.class);
    private static final String OF_LONG_UNCHECKED_NAME = "ofLongUnchecked";
    private static final String OF_LONG_UNCHECKED_DESC = methodType(MemorySegment.class, long.class, long.class, ResourceScopeImpl.class).descriptorString();

    private static final String VALUE_LAYOUT_INTRN = Type.getInternalName(ValueLayout.class);

    private static final String SEGMENT_ALLOCATOR_INTRN = Type.getInternalName(SegmentAllocator.class);
    private static final String ALLOCATE_NAME = "allocate";
    private static final String ALLOCATE_DESC = methodType(MemorySegment.class, long.class, long.class).descriptorString();

    private static final String RESOURCE_SCOPE_IMPL_INTRN = Type.getInternalName(ResourceScopeImpl.class);

    private static final String SHARED_UTILS_INTRN = Type.getInternalName(SharedUtils.class);
    private static final String HANDLE_UNCAUGHT_EXCEPTION_NAME = "handleUncaughtException";
    private static final String HANDLE_UNCAUGHT_EXCEPTION_DESC = methodType(void.class, Throwable.class).descriptorString();

    private static final String METHOD_HANDLES_INTRN = Type.getInternalName(MethodHandles.class);
    private static final String CLASS_DATA_NAME = "classData";
    private static final String CLASS_DATA_DESC = methodType(Object.class, MethodHandles.Lookup.class, String.class, Class.class).descriptorString();

    private static final String METHOD_HANDLE_INTRN = Type.getInternalName(MethodHandle.class);
    private static final String INVOKE_EXACT_NAME = "invokeExact";

    private static final Handle BSM_CLASS_DATA = new Handle(
            H_INVOKESTATIC,
            METHOD_HANDLES_INTRN,
            CLASS_DATA_NAME,
            CLASS_DATA_DESC,
            false);
    private static final ConstantDynamic CLASS_DATA_CONDY = new ConstantDynamic(
            ConstantDescs.DEFAULT_NAME,
            OBJECT_DESC,
            BSM_CLASS_DATA);

    private static final String CLASS_NAME_DOWNCALL = "jdk/internal/foreign/abi/DowncallStub";
    private static final String CLASS_NAME_UPCALL = "jdk/internal/foreign/abi/UpcallStub";
    private static final String METHOD_NAME = "invoke";

    /** Name of its super class*/
    private static final String SUPER_NAME = OBJECT_INTRN;

    private enum BasicType {
        Z, B, S, C, I, J, F, D, L;

        static BasicType of(Class<?> cls) {
            if (cls == boolean.class) {
                return Z;
            } else if (cls == byte.class) {
                return B;
            } else if (cls == short.class) {
                return S;
            } else if (cls == char.class) {
                return C;
            } else if (cls == int.class) {
                return I;
            } else if (cls == long.class) {
                return J;
            } else if (cls == float.class) {
                return F;
            } else if (cls == double.class) {
                return D;
            } else {
                return L;
            }
        }
    }

    private final MethodVisitor mv;
    private final MethodType callerMethodType;
    private final CallingSequence callingSequence;
    private final ABIDescriptor abi;
    private final MethodType leafType;

    private int localIdx = 0;
    private int[] paramIndex2ParamSlot;
    private int[] leafArgSlots;
    private int RETURN_ALLOCATOR_IDX = -1;
    private int CONTEXT_IDX = -1;
    private int RETURN_BUFFER_IDX = -1;
    private int RET_VAL_IDX = -1;
    private Deque<Class<?>> typeStack;
    private List<Class<?>> leafArgTypes;
    private int paramIndex;
    private long retBufOffset; // for needsReturnBuffer

    private Specializer(MethodVisitor mv, MethodType callerMethodType, CallingSequence callingSequence, ABIDescriptor abi, MethodType leafType) {
        this.mv = mv;
        this.callerMethodType = callerMethodType;
        this.callingSequence = callingSequence;
        this.abi = abi;
        this.leafType = leafType;
    }

    static MethodHandle specialize(MethodHandle leafHandle, CallingSequence callingSequence, ABIDescriptor abi) {
        String className = callingSequence.forDowncall() ? CLASS_NAME_DOWNCALL : CLASS_NAME_UPCALL;
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES);
        cw.visit(CLASSFILE_VERSION, ACC_PUBLIC + ACC_FINAL + ACC_SUPER, className, null, SUPER_NAME, null);

        MethodType callerMethodType = callingSequence.callerMethodType();
        if (callingSequence.forDowncall()) {
            if (callingSequence.needsReturnBuffer()) {
                callerMethodType = callerMethodType.dropParameterTypes(0, 1); // Return buffer does not appear in the parameter list
            }
            callerMethodType = callerMethodType.insertParameterTypes(0, SegmentAllocator.class);
        }
        String descriptor = callerMethodType.descriptorString();
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, METHOD_NAME, descriptor, null, null);

        new Specializer(mv, callerMethodType, callingSequence, abi, leafHandle.type()).specialize();

        mv.visitMaxs(0, 0);
        mv.visitEnd();

        cw.visitEnd();

        byte[] bytes = cw.toByteArray();
        if (DUMP_CLASSES_DIR != null) {
            String fileName = className + escapeForFileName(callingSequence.functionDesc().toString()) + ".class";
            Path dumpPath = Path.of(DUMP_CLASSES_DIR).resolve(fileName);
            try {
                Files.createDirectories(dumpPath.getParent());
                Files.write(dumpPath, bytes);
            } catch (IOException e) {
                throw new InternalError(e);
            }
        }

        if (PERFORM_VERIFICATION) {
            boolean printResults = false; // only print in case of exception
            CheckClassAdapter.verify(new ClassReader(bytes), null, printResults, new PrintWriter(System.err));
        }

        try {
            // We must initialize the class since the upcall stubs don't have a clinit barrier, and the slow
            // path in the c2i adapter we end up calling can not handle the particular code shape where the
            // caller is an optimized upcall stub.
            boolean initialize = callingSequence.forUpcall();
            MethodHandles.Lookup lookup = MethodHandles.lookup().defineHiddenClassWithClassData(bytes, leafHandle, initialize);
            return lookup.findStatic(lookup.lookupClass(), METHOD_NAME, callerMethodType);
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new InternalError("Should not happen", e);
        }
    }

    private static String escapeForFileName(String str) {
        StringBuilder sb = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            sb.append(switch (c) {
                case ' ' -> '_';
                case '[', '<' -> '{';
                case ']', '>' -> '}';
                case '/', '\\', ':', '*', '?', '"', '|' -> '!'; // illegal in Windows file names.
                default -> c;
            });
        }
        return sb.toString();
    }

    private void specialize() {
        // map of parameter indexes to local var table slots
        paramIndex2ParamSlot = new int[callerMethodType.parameterCount()];
        for (int i = 0; i < callerMethodType.parameterCount(); i++) {
            paramIndex2ParamSlot[i] = newLocal(callerMethodType.parameterType(i));
        }

        // slots that store the output arguments (passed to the leaf handle)
        leafArgSlots = new int[leafType.parameterCount()];
        for (int i = 0; i < leafType.parameterCount(); i++) {
            leafArgSlots[i] = newLocal(leafType.parameterType(i));
        }

        // allocator passed to us for allocating the return MS (downcalls only)
        if (callingSequence.forDowncall()) {
            RETURN_ALLOCATOR_IDX = 0; // first param
        }

        if (callingSequence.allocationSize() != 0) {
            emitConst(callingSequence.allocationSize());
            mv.visitMethodInsn(INVOKESTATIC, BINDING_CONTEXT_INTRN, OF_BOUNDED_ALLOCATOR_NAME, OF_BOUNDED_ALLOCATOR_DESC, false);
        } else if (callingSequence.forDowncall()) {
            mv.visitFieldInsn(GETSTATIC, BINDING_CONTEXT_INTRN, DUMMY_CONTEXT_NAME, BINDING_CONTEXT_DESC);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, BINDING_CONTEXT_INTRN, OF_SCOPE_NAME, OF_SCOPE_DESC, false);
        }
        CONTEXT_IDX = newLocal(BasicType.L);
        emitStore(BasicType.L, CONTEXT_IDX);

        if (callingSequence.needsReturnBuffer() && callingSequence.forDowncall()) {
            emitLoadInteralAllocator();
            emitAllocateCall(callingSequence.returnBufferSize(), 1);
            RETURN_BUFFER_IDX = newLocal(BasicType.L);
            emitStore(BasicType.L, RETURN_BUFFER_IDX);
            // for upcalls the wrapper stub allocates the buffer
        }

        Label tryStart = new Label();
        Label tryEnd = new Label();
        Label catchStart = new Label();

        mv.visitLabel(tryStart);

        typeStack = new ArrayDeque<>();
        leafArgTypes = new ArrayList<>();
        // Return buffer does not appear in the parameter list
        paramIndex = callingSequence.forDowncall() ? 1 : 0; // +1 to skip SegmentAllocator
        for (int i = 0; i < callingSequence.argumentBindingsCount(); i++) {
            if (callingSequence.forDowncall()) {
                // for downcalls we need to pre-load args
                if (callingSequence.needsReturnBuffer() && i == 0) {
                    assert RETURN_BUFFER_IDX != -1;
                    emitLoad(BasicType.L, RETURN_BUFFER_IDX);
                    typeStack.push(MemorySegment.class);
                } else {
                    emitGetInput();
                }
            }

            doBindings(callingSequence.argumentBindings(i));

            if (callingSequence.forUpcall()) {
                // for upcalls we need to post-store args
                if (callingSequence.needsReturnBuffer() && i == 0) {
                    // return buffer is wrapped above, but not passed to the leaf handle
                    assert typeStack.pop() == MemorySegment.class;
                    RETURN_BUFFER_IDX = newLocal(BasicType.L);
                    emitStore(BasicType.L, RETURN_BUFFER_IDX);
                } else {
                    // for upcalls the result is an argument to the leaf handle
                    emitSetOutput(typeStack.pop());
                }
            }
            assert typeStack.isEmpty();
        }

        assert leafArgTypes.equals(leafType.parameterList());

        mv.visitLdcInsn(CLASS_DATA_CONDY);
        mv.visitTypeInsn(CHECKCAST, METHOD_HANDLE_INTRN);

        // now re-load all the low-level args
        for (int i = 0; i < leafArgSlots.length; i++) {
            emitLoad(leafArgTypes.get(i), leafArgSlots[i]);
        }

        // call leaf MH
        mv.visitMethodInsn(INVOKEVIRTUAL, METHOD_HANDLE_INTRN, INVOKE_EXACT_NAME, leafType.descriptorString(), false);
        if (callingSequence.forDowncall() && leafType.returnType() != void.class) {
            // for upcalls this happens lazily through a VM_STORE
            emitSaveReturnValue(leafType.returnType());
        }
        // in the case of upcalls we leave the return value on the stack to pick up below

        // return value processing
        if (callingSequence.hasReturnBindings()) {
            if (callingSequence.forUpcall()) {
                typeStack.push(leafType.returnType());
            }

            retBufOffset = 0;
            doBindings(callingSequence.returnBindings());

            if (callingSequence.forUpcall() && !callingSequence.needsReturnBuffer()) {
                // was VM_STOREd somewhere in the bindings
                emitRestoreReturnValue(callerMethodType.returnType());
            }
            mv.visitLabel(tryEnd);
            // finally
            emitCloseContext();

            if (callerMethodType.returnType() == void.class) {
                // The case for upcalls that return by return buffer
                assert typeStack.isEmpty();
                mv.visitInsn(RETURN);
            } else {
                assert typeStack.pop() == callerMethodType.returnType();
                assert typeStack.isEmpty();
                emitReturn(callerMethodType.returnType());
            }
        } else {
            assert callerMethodType.returnType() == void.class;
            assert typeStack.isEmpty();
            mv.visitLabel(tryEnd);
            // finally
            emitCloseContext();
            mv.visitInsn(RETURN);
        }

        mv.visitLabel(catchStart);
        // finally
        emitCloseContext();
        if (callingSequence.forDowncall()) {
            mv.visitInsn(ATHROW);
        } else {
           mv.visitMethodInsn(INVOKESTATIC, SHARED_UTILS_INTRN, HANDLE_UNCAUGHT_EXCEPTION_NAME, HANDLE_UNCAUGHT_EXCEPTION_DESC, false);
           if (callerMethodType.returnType() != void.class) {
               emitConstZero(callerMethodType.returnType());
               emitReturn(callerMethodType.returnType());
           } else {
               mv.visitInsn(RETURN);
           }
        }

        mv.visitTryCatchBlock(tryStart, tryEnd, catchStart, null);
    }

    private void doBindings(List<Binding> bindings) {
        for (Binding binding : bindings) {
            switch (binding.tag()) {
                case VM_STORE -> emitVMStore((Binding.VMStore) binding);
                case VM_LOAD -> emitVMLoad((Binding.VMLoad) binding);
                case BUFFER_STORE -> emitBufferStore((Binding.BufferStore) binding);
                case BUFFER_LOAD -> emitBufferLoad((Binding.BufferLoad) binding);
                case COPY_BUFFER -> emitCopyBuffer((Binding.Copy) binding);
                case ALLOC_BUFFER -> emitAllocBuffer((Binding.Allocate) binding);
                case BOX_ADDRESS -> emitBoxAddress();
                case UNBOX_ADDRESS -> emitUnboxAddress();
                case TO_SEGMENT -> emitToSegment((Binding.ToSegment) binding);
                case DUP -> emitDupBinding();
            }
        }
    }

    private void emitSetOutput(Class<?> storeType) {
        emitStore(BasicType.of(storeType), leafArgSlots[leafArgTypes.size()]);
        leafArgTypes.add(storeType);
    }

    private void emitGetInput() {
        Class<?> highLevelType = callerMethodType.parameterType(paramIndex);
        emitLoad(BasicType.of(highLevelType), paramIndex2ParamSlot[paramIndex]);
        typeStack.push(highLevelType);
        paramIndex++;
    }

    private void emitSaveReturnValue(Class<?> storeType) {
        RET_VAL_IDX = newLocal(BasicType.of(storeType));
        emitStore(BasicType.of(storeType), RET_VAL_IDX);
    }

    private void emitRestoreReturnValue(Class<?> loadType) {
        assert RET_VAL_IDX != -1;
        emitLoad(BasicType.of(loadType), RET_VAL_IDX);
        typeStack.push(loadType);
    }

    private int newLocal(Class<?> type) {
        return newLocal(BasicType.of(type));
    }

    private int newLocal(BasicType type) {
        int idx = localIdx;
        localIdx += (type == BasicType.D || type == BasicType.J) ? 2 : 1;
        return idx;
    }

    private void emitLoadInternalScope() {
        assert CONTEXT_IDX != -1;
        emitLoad(BasicType.L, CONTEXT_IDX);
        mv.visitMethodInsn(INVOKEVIRTUAL, BINDING_CONTEXT_INTRN, SCOPE_NAME, SCOPE_DESC, false);
    }

    private void emitLoadInteralAllocator() {
        assert CONTEXT_IDX != -1;
        emitLoad(BasicType.L, CONTEXT_IDX);
        mv.visitMethodInsn(INVOKEVIRTUAL, BINDING_CONTEXT_INTRN, ALLOCATOR_NAME, ALLOCATOR_DESC, false);
    }

    private void emitCloseContext() {
        assert CONTEXT_IDX != -1;
        emitLoad(BasicType.L, CONTEXT_IDX);
        mv.visitMethodInsn(INVOKEVIRTUAL, BINDING_CONTEXT_INTRN, CLOSE_NAME, CLOSE_DESC, false);
    }

    private void emitToSegment(Binding.ToSegment binding) {
        long size = binding.size();
        assert typeStack.pop() == MemoryAddress.class;

        emitToRawLongValue();
        emitConst(size);
        emitLoadInternalScope();
        mv.visitTypeInsn(CHECKCAST, RESOURCE_SCOPE_IMPL_INTRN);
        mv.visitMethodInsn(INVOKESTATIC, MEMORY_ADDRESS_IMPL_INTRN, OF_LONG_UNCHECKED_NAME, OF_LONG_UNCHECKED_DESC, false);

        typeStack.push(MemorySegment.class);
    }

    private void emitToRawLongValue() {
        mv.visitMethodInsn(INVOKEINTERFACE, MEMORY_ADDRESS_INTRN, TO_RAW_LONG_VALUE_NAME, TO_RAW_LONG_VALUE_DESC, true);
    }

    private void emitBoxAddress() {
        assert typeStack.pop() == long.class;
        mv.visitMethodInsn(INVOKESTATIC, MEMORY_ADDRESS_INTRN, OF_LONG_NAME, OF_LONG_DESC, true);
        typeStack.push(MemoryAddress.class);
    }

    private void emitAllocBuffer(Binding.Allocate binding) {
        if (callingSequence.forDowncall()) {
            assert RETURN_ALLOCATOR_IDX != -1;
            emitLoad(BasicType.L, RETURN_ALLOCATOR_IDX);
        } else {
            emitLoadInteralAllocator();
        }
        emitAllocateCall(binding.size(), binding.alignment());
        typeStack.push(MemorySegment.class);
    }

    private void emitBufferStore(Binding.BufferStore bufferStore) {
        Class<?> storeType = bufferStore.type();
        long offset = bufferStore.offset();

        assert typeStack.pop() == storeType;
        assert typeStack.pop() == MemorySegment.class;
        BasicType basicStoreType = BasicType.of(storeType);
        int valueIdx = newLocal(basicStoreType);
        emitStore(basicStoreType, valueIdx);

        Class<?> valueLayoutType = emitLoadLayoutConstant(storeType);
        emitConst(offset);
        emitLoad(basicStoreType, valueIdx);
        String descriptor = methodType(void.class, valueLayoutType, long.class, storeType).descriptorString();
        mv.visitMethodInsn(INVOKEINTERFACE, MEMORY_SEGMENT_INTRN, SET_NAME, descriptor, true);
    }

    // VM_STORE and VM_LOAD are emulated, which is different for down/upcalls

    private void emitVMStore(Binding.VMStore vmStore) {
        Class<?> storeType = vmStore.type();
        assert typeStack.pop() == storeType;

        if (callingSequence.forDowncall()) {
            // processing arg
            emitSetOutput(storeType);
        } else {
            // processing return
            if (!callingSequence.needsReturnBuffer()) {
                emitSaveReturnValue(storeType);
            } else {
                BasicType basicStoreType = BasicType.of(storeType);
                int valueIdx = newLocal(basicStoreType);
                emitStore(basicStoreType, valueIdx); // store away the stored value, need it later

                assert RETURN_BUFFER_IDX != -1;
                emitLoad(BasicType.L, RETURN_BUFFER_IDX);
                Class<?> valueLayoutType = emitLoadLayoutConstant(storeType);
                emitConst(retBufOffset);
                emitLoad(basicStoreType, valueIdx);
                String descriptor = methodType(void.class, valueLayoutType, long.class, storeType).descriptorString();
                mv.visitMethodInsn(INVOKEINTERFACE, MEMORY_SEGMENT_INTRN, SET_NAME, descriptor, true);
                retBufOffset += abi.arch.typeSize(vmStore.storage().type());
            }
        }
    }
    private void emitVMLoad(Binding.VMLoad vmLoad) {
        Class<?> loadType = vmLoad.type();

        if (callingSequence.forDowncall()) {
            // processing return
            if (!callingSequence.needsReturnBuffer()) {
                emitRestoreReturnValue(loadType);
            } else {
                assert RETURN_BUFFER_IDX != -1;
                emitLoad(BasicType.L, RETURN_BUFFER_IDX);
                Class<?> valueLayoutType = emitLoadLayoutConstant(loadType);
                emitConst(retBufOffset);
                String descriptor = methodType(loadType, valueLayoutType, long.class).descriptorString();
                mv.visitMethodInsn(INVOKEINTERFACE, MEMORY_SEGMENT_INTRN, GET_NAME, descriptor, true);
                retBufOffset += abi.arch.typeSize(vmLoad.storage().type());
                typeStack.push(loadType);
            }
        } else {
            // processing arg
            emitGetInput();
        }
    }

    private void emitDupBinding() {
        Class<?> dupType = typeStack.peek();
        emitDup(BasicType.of(dupType));
        typeStack.push(dupType);
    }

    private void emitUnboxAddress() {
        assert Addressable.class.isAssignableFrom(typeStack.pop());
        mv.visitMethodInsn(INVOKEINTERFACE, ADDRESSABLE_INTRN, ADDRESS_NAME, ADDRESS_DESC, true);
        emitToRawLongValue();
        typeStack.push(long.class);
    }

    private void emitBufferLoad(Binding.BufferLoad bufferLoad) {
        Class<?> loadType = bufferLoad.type();
        long offset = bufferLoad.offset();

        assert typeStack.pop() == MemorySegment.class;

        Class<?> valueLayoutType = emitLoadLayoutConstant(loadType);
        emitConst(offset);
        String descriptor = methodType(loadType, valueLayoutType, long.class).descriptorString();
        mv.visitMethodInsn(INVOKEINTERFACE, MEMORY_SEGMENT_INTRN, GET_NAME, descriptor, true);
        typeStack.push(loadType);
    }

    private void emitCopyBuffer(Binding.Copy copy) {
        long size = copy.size();
        long alignment = copy.alignment();

        assert typeStack.pop() == MemorySegment.class;

        // operand/srcSegment is on the stack
        // copy(MemorySegment srcSegment, long srcOffset, MemorySegment dstSegment, long dstOffset, long bytes)
        emitConst(0L);

        // create the dstSegment by allocating it
        // context.allocator().allocate(size, alignment)
        emitLoadInteralAllocator();
        emitAllocateCall(size, alignment);
        emitDup(BasicType.L);
        int storeIdx = newLocal(BasicType.L);
        emitStore(BasicType.L, storeIdx);

        emitConst(0L);
        emitConst(size);
        mv.visitMethodInsn(INVOKESTATIC, MEMORY_SEGMENT_INTRN, COPY_NAME, COPY_DESC, true);

        emitLoad(BasicType.L, storeIdx);
        typeStack.push(MemorySegment.class);
    }

    private void emitAllocateCall(long size, long alignment) {
        emitConst(size);
        emitConst(alignment);
        mv.visitMethodInsn(INVOKEINTERFACE, SEGMENT_ALLOCATOR_INTRN, ALLOCATE_NAME, ALLOCATE_DESC, true);
    }

    private Class<?> emitLoadLayoutConstant(Class<?> type) {
        Class<?> valueLayoutType = valueLayoutTypeFor(type);
        String valueLayoutConstantName = valueLayoutConstantFor(type);
        mv.visitFieldInsn(GETSTATIC, VALUE_LAYOUT_INTRN, valueLayoutConstantName, valueLayoutType.descriptorString());
        return valueLayoutType;
    }

    private static String valueLayoutConstantFor(Class<?> type) {
        if (type == boolean.class) {
            return "JAVA_BOOLEAN";
        } else if (type == byte.class) {
            return "JAVA_BYTE";
        } else if (type == short.class) {
            return "JAVA_SHORT";
        } else if (type == char.class) {
            return "JAVA_CHAR";
        } else if (type == int.class) {
            return "JAVA_INT";
        } else if (type == long.class) {
            return "JAVA_LONG";
        } else if (type == float.class) {
            return "JAVA_FLOAT";
        } else if (type == double.class) {
            return "JAVA_DOUBLE";
        } else if (type == MemoryAddress.class) {
            return "ADDRESS";
        } else {
            throw new IllegalStateException("Unknown type: " + type);
        }
    }

    private static Class<?> valueLayoutTypeFor(Class<?> type) {
        if (type == boolean.class) {
            return ValueLayout.OfBoolean.class;
        } else if (type == byte.class) {
            return ValueLayout.OfByte.class;
        } else if (type == short.class) {
            return ValueLayout.OfShort.class;
        } else if (type == char.class) {
            return ValueLayout.OfChar.class;
        } else if (type == int.class) {
            return ValueLayout.OfInt.class;
        } else if (type == long.class) {
            return ValueLayout.OfLong.class;
        } else if (type == float.class) {
            return ValueLayout.OfFloat.class;
        } else if (type == double.class) {
            return ValueLayout.OfDouble.class;
        } else if (type == MemoryAddress.class) {
            return ValueLayout.OfAddress.class;
        } else {
            throw new IllegalStateException("Unknown type: " + type);
        }
    }

    private void emitDup(BasicType type) {
        if (type == BasicType.D || type == BasicType.J) {
            mv.visitInsn(DUP2);
        } else {
            mv.visitInsn(Opcodes.DUP);
        }
    }

    /*
     * Low-level emit helpers.
     */

    private void emitConstZero(Class<?> type) {
        emitConst(switch (BasicType.of(type)) {
            case Z, B, S, C, I -> 0;
            case J -> 0L;
            case F -> 0F;
            case D -> 0D;
            case L -> null;
        });
    }

    private void emitConst(Object con) {
        if (con == null) {
            mv.visitInsn(Opcodes.ACONST_NULL);
            return;
        }
        if (con instanceof Integer) {
            emitIconstInsn((int) con);
            return;
        }
        if (con instanceof Byte) {
            emitIconstInsn((byte)con);
            return;
        }
        if (con instanceof Short) {
            emitIconstInsn((short)con);
            return;
        }
        if (con instanceof Character) {
            emitIconstInsn((char)con);
            return;
        }
        if (con instanceof Long) {
            long x = (long) con;
            short sx = (short)x;
            if (x == sx) {
                if (sx >= 0 && sx <= 1) {
                    mv.visitInsn(Opcodes.LCONST_0 + (int) sx);
                } else {
                    emitIconstInsn((int) x);
                    mv.visitInsn(Opcodes.I2L);
                }
                return;
            }
        }
        if (con instanceof Float) {
            float x = (float) con;
            short sx = (short)x;
            if (x == sx) {
                if (sx >= 0 && sx <= 2) {
                    mv.visitInsn(Opcodes.FCONST_0 + (int) sx);
                } else {
                    emitIconstInsn((int) x);
                    mv.visitInsn(Opcodes.I2F);
                }
                return;
            }
        }
        if (con instanceof Double) {
            double x = (double) con;
            short sx = (short)x;
            if (x == sx) {
                if (sx >= 0 && sx <= 1) {
                    mv.visitInsn(Opcodes.DCONST_0 + (int) sx);
                } else {
                    emitIconstInsn((int) x);
                    mv.visitInsn(Opcodes.I2D);
                }
                return;
            }
        }
        if (con instanceof Boolean) {
            emitIconstInsn((boolean) con ? 1 : 0);
            return;
        }
        // fall through:
        mv.visitLdcInsn(con);
    }

    private void emitIconstInsn(int cst) {
        if (cst >= -1 && cst <= 5) {
            mv.visitInsn(Opcodes.ICONST_0 + cst);
        } else if (cst >= Byte.MIN_VALUE && cst <= Byte.MAX_VALUE) {
            mv.visitIntInsn(Opcodes.BIPUSH, cst);
        } else if (cst >= Short.MIN_VALUE && cst <= Short.MAX_VALUE) {
            mv.visitIntInsn(Opcodes.SIPUSH, cst);
        } else {
            mv.visitLdcInsn(cst);
        }
    }

    private void emitLoad(Class<?> type, int index) {
        emitLoad(BasicType.of(type), index);
    }

    private void emitLoad(BasicType type, int index) {
        int opcode = loadOpcode(type);
        mv.visitVarInsn(opcode, index);
    }

    private static int loadOpcode(BasicType type) throws InternalError {
        return switch (type) {
            case Z, B, S, C, I -> ILOAD;
            case J -> LLOAD;
            case F -> FLOAD;
            case D -> DLOAD;
            case L -> ALOAD;
        };
    }

    private void emitStore(BasicType type, int index) {
        int opcode = storeOpcode(type);
        mv.visitVarInsn(opcode, index);
    }

    private static int storeOpcode(BasicType type) throws InternalError {
        return switch (type) {
            case Z, B, S, C, I -> ISTORE;
            case J -> LSTORE;
            case F -> FSTORE;
            case D -> DSTORE;
            case L -> ASTORE;
        };
    }

    private void emitReturn(Class<?> type) {
        int opcode = returnOpcode(BasicType.of(type));
        mv.visitInsn(opcode);
    }

    private static int returnOpcode(BasicType type) {
       return switch (type) {
            case Z, B, S, C, I -> IRETURN;
            case J -> LRETURN;
            case F -> FRETURN;
            case D -> DRETURN;
            case L -> ARETURN;
        };
    }
}
