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

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentScope;

class LibFallback {
    static final boolean SUPPORTED;

    static {
        SUPPORTED = tryLoadLibrary();
    }

    private static boolean tryLoadLibrary() {
        try {
            System.loadLibrary("fallbackLinker");
        } catch (UnsatisfiedLinkError ule) {
            return false;
        }
        init();
        return true;
    }

    static final int DEFAULT_ABI = ffi_default_abi();

    static final MemorySegment UINT8_TYPE = MemorySegment.ofAddress(ffi_type_uint8());
    static final MemorySegment SINT8_TYPE = MemorySegment.ofAddress(ffi_type_sint8());
    static final MemorySegment UINT16_TYPE = MemorySegment.ofAddress(ffi_type_uint16());
    static final MemorySegment SINT16_TYPE = MemorySegment.ofAddress(ffi_type_sint16());
    static final MemorySegment SINT32_TYPE = MemorySegment.ofAddress(ffi_type_sint32());
    static final MemorySegment SINT64_TYPE = MemorySegment.ofAddress(ffi_type_sint64());
    static final MemorySegment FLOAT_TYPE = MemorySegment.ofAddress(ffi_type_float());
    static final MemorySegment DOUBLE_TYPE = MemorySegment.ofAddress(ffi_type_double());
    static final MemorySegment POINTER_TYPE = MemorySegment.ofAddress(ffi_type_pointer());

    static final MemorySegment VOID_TYPE = MemorySegment.ofAddress(ffi_type_void());
    static final short STRUCT_TAG = ffi_type_struct();

    static void doDowncall(MemorySegment cif, MemorySegment target, long retPtr, MemorySegment argPtrs,
                                  long capturedStateAddr, int capturedStateMask) {
            doDowncall(cif.address(), target.address(),
                    retPtr, argPtrs.address(), capturedStateAddr, capturedStateMask);
    }

    static MemorySegment prepCif(MemorySegment returnType, int numArgs, MemorySegment argTypes, FFIABI abi,
                                        SegmentScope scope) {
        MemorySegment cif = MemorySegment.allocateNative(sizeofCif(), scope);
        checkStatus(ffi_prep_cif(cif.address(), abi.value(), numArgs,
                returnType.address(), argTypes.address()));
        return cif;
    }

    static MemorySegment createClosure(MemorySegment cif, Object userData, SegmentScope scope) {
        long[] ptrs = new long[3];
        checkStatus(createClosure(cif.address(), userData, ptrs));
        long closurePtr = ptrs[0];
        long execPtr = ptrs[1];
        long globalUserData = ptrs[2];

        return MemorySegment.ofAddress(execPtr, 0, scope, () -> freeClosure(closurePtr, globalUserData));
    }

    static void getStructOffsets(MemorySegment structType, MemorySegment offsetsOut, FFIABI abi) {
        checkStatus(ffi_get_struct_offsets(abi.value(),
                    structType.address(), offsetsOut.address()));
    }

    private static void checkStatus(int code) {
        FFIStatus status = FFIStatus.of(code);
        if (FFIStatus.of(code) != FFIStatus.FFI_OK) {
            throw new IllegalStateException("libffi call failed with code: " + status);
        }
    }

    private static native void init();

    private static native long sizeofCif();

    private static native int createClosure(long cif, Object invData, long[] ptrs);
    private static native void freeClosure(long closureAddress, long globalRef);
    private static native void doDowncall(long cif, long fn, long rvalue, long avalues, long capturedState, int capturedStateMask);

    private static native int ffi_prep_cif(long cif, int abi, int nargs, long rtype, long atypes);
    private static native int ffi_get_struct_offsets(int abi, long type, long offsets);

    private static native int ffi_default_abi();
    private static native short ffi_type_struct();

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

    static native long FFITypeSizeof();
    static native void FFITypeSetType(long ptr, short type);
    static native void FFITypeSetElements(long ptr, long elements);
}
