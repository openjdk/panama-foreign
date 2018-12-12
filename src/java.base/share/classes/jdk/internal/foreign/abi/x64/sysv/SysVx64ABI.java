/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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
package jdk.internal.foreign.abi.x64.sysv;

import java.foreign.Library;
import java.foreign.NativeMethodType;
import java.foreign.layout.Address;
import java.foreign.layout.Group;
import java.foreign.layout.Layout;
import java.foreign.layout.Padding;
import java.foreign.layout.Sequence;
import java.foreign.layout.Value;
import java.foreign.memory.LayoutType;
import java.foreign.memory.Pointer;
import java.lang.invoke.MethodHandle;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jdk.internal.foreign.abi.*;

import static sun.security.action.GetPropertyAction.privilegedGetProperty;

/**
 * ABI implementation based on System V ABI AMD64 supplement v.0.99.6
 */
public class SysVx64ABI implements SystemABI {
    private static final String fastPath = privilegedGetProperty("jdk.internal.foreign.NativeInvoker.FASTPATH");
    private static SysVx64ABI instance;

    public static SysVx64ABI getInstance() {
        if (instance == null) {
            instance = new SysVx64ABI();
        }
        return instance;
    }

    @Override
    public Layout layoutOf(CType type) {
        switch (type) {
            case Char:
            case SignedChar:
                return Value.ofSignedInt(8);
            case Bool:
            case UnsignedChar:
                return Value.ofUnsignedInt(8);
            case Short:
                return Value.ofSignedInt(16);
            case UnsignedShort:
                return Value.ofUnsignedInt(16);
            case Int:
                return Value.ofSignedInt(32);
            case UnsignedInt:
                return Value.ofUnsignedInt(32);
            case Long:
            case LongLong:
                return Value.ofSignedInt(64);
            case UnsignedLong:
            case UnsignedLongLong:
                return Value.ofUnsignedInt(64);
            case Float:
                return Value.ofFloatingPoint(32);
            case Double:
                return Value.ofFloatingPoint(64);
            case LongDouble:
                return Value.ofFloatingPoint(128);
            case Pointer:
                return Value.ofUnsignedInt(64);
            default:
                throw new IllegalArgumentException("Unknown layout " + type);

        }
    }

    @Override
    public MethodHandle downcallHandle(CallingConvention cc, Library.Symbol symbol, NativeMethodType nmt) {
        if (symbol instanceof UpcallStub) {
            return ((UpcallStub) symbol).methodHandle();
        }

        try {
            UpcallStub stub = getUpcallStub(symbol.getAddress().addr());
            if (null != stub) {
                return stub.methodHandle();
            }
        } catch (IllegalAccessException iae) {
            throw new IllegalStateException(iae);
        }

        if (nmt.isVarArgs()) {
            return VarargsInvoker.make(symbol, nmt);
        }

        StandardCall sc = new StandardCall();
        LayoutType<?> ret = nmt.getReturnType();
        LayoutType<?>[] args = nmt.getArgsType();
        CallingSequence callingSequence = sc.arrangeCall(ret, args);

        if (fastPath == null || !fastPath.equals("none")) {
            if (LinkToNativeSignatureShuffler.acceptDowncall(args.length, callingSequence)) {
                return LinkToNativeInvoker.make(symbol, callingSequence, ret, args);
            } else if (fastPath != null && fastPath.equals("direct")) {
                throw new IllegalStateException(
                        String.format("No fast path for: %s", symbol.getName()));
            }
        }
        return UniversalNativeInvoker.make(symbol, callingSequence, ret, args);
    }

    @Override
    public Library.Symbol upcallStub(CallingConvention cc, MethodHandle target, NativeMethodType nmt) {
        StandardCall sc = new StandardCall();
        LayoutType<?> ret = nmt.getReturnType();
        LayoutType<?>[] args = nmt.getArgsType();
        CallingSequence callingSequence = sc.arrangeCall(ret, args);
        Pointer<?> ptr;
        if (fastPath == null || !fastPath.equals("none")) {
            if (LinkToNativeSignatureShuffler.acceptUpcall(args.length, callingSequence)) {
                return new LinkToNativeUpcallHandler(target, callingSequence, ret, args);
            } else if (fastPath != null && fastPath.equals("direct")) {
                throw new IllegalStateException(
                        String.format("No fast path for function type %s", nmt.function()));
            }
        }
        return new UniversalUpcallHandler(target, callingSequence, ret, args);
    }

    @Override
    public void freeUpcallStub(Library.Symbol stub) {
        try {
            freeUpcallStub(stub.getAddress().addr());
        } catch (IllegalAccessException iae) {
            throw new IllegalStateException(iae);
        }
    }

    private static final Map<String, CallingConvention> SysVCallingConventions = new HashMap<>();
    private static final CallingConvention CV_C = () -> "C";

    static {
        SysVCallingConventions.put("C", CV_C);
    }

    @Override
    public CallingConvention defaultCallingConvention() {
        return CV_C;
    }

    @Override
    public CallingConvention namedCallingConvention(String name) throws IllegalArgumentException {
        CallingConvention cv = SysVCallingConventions.get(name);
        if (null == cv) {
            throw new IllegalArgumentException("Unknown calling convention " + name);
        }
        return cv;
    }

    @Override
    public Collection<CallingConvention> callingConventions() {
        return Collections.unmodifiableCollection(SysVCallingConventions.values());
    }

    private long alignUp(long addr, long alignment) {
        return ((addr - 1) | (alignment - 1)) + 1;
    }

    private long alignDown(long addr, long alignment) {
        return addr & ~(alignment - 1);
    }

    private long alignmentOfScalar(Value st) {
        return st.bitsSize() / 8;
    }

    private long alignmentOfArray(Sequence ar, boolean isVar) {
        if (ar.elementsSize() == 0) {
            // VLA or incomplete
            return 16;
        } else if ((ar.bitsSize() / 8) >= 16 && isVar) {
            return 16;
        } else {
            // align as element type
            Layout elementType = ar.element();
            return alignment(elementType, false);
        }
    }

    private long alignmentOfContainer(Group ct) {
        // Most strict member
        return ct.elements().stream().mapToLong(t -> alignment(t, false)).max().orElse(1);
    }

    /**
     * The alignment requirement for a given type
     * @param isVar indicate if the type is a standalone variable. This change how
     * array is aligned. for example.
     */
    long alignment(Layout t, boolean isVar) {
        if (t instanceof Value) {
            return alignmentOfScalar((Value) t);
        } else if (t instanceof Sequence) {
            // when array is used alone
            return alignmentOfArray((Sequence) t, isVar);
        } else if (t instanceof Group) {
            return alignmentOfContainer((Group) t);
        } else if (t instanceof Address) {
            return 8;
        } else if (t instanceof Padding) {
            return 1;
        } else {
            throw new IllegalArgumentException("Invalid type: " + t);
        }
    }

    private long sizeOfArray(Sequence ar) {
        long occurrence = ar.elementsSize();
        if (occurrence == 0) {
            // VLA or incomplete, unknown size with negative value
            return occurrence;
        } else {
            Layout elementType = ar.element();
            return occurrence * alignment(elementType, false);
        }
    }

    private class ContainerSizeInfo {
        private final boolean isUnion;
        private final long pack;
        private long[] offsets;
        private long size = 0;
        private long alignment_of_container = 0;

        ContainerSizeInfo(Group ct, long pack) {
            this.isUnion = ct.kind() == Group.Kind.UNION;
            this.pack = pack;
            offsets = new long[ct.elements().size()];
            for (int i = 0; i < offsets.length; i++) {
                offsets[i] = calculate(ct.elements().get(i));
            }
        }

        private long calculate(Layout t) {
            long offset = 0;
            long alignment = (pack <= 0) ? SysVx64ABI.this.alignment(t, false) : pack;
            if (isUnion) {
                if ((t.bitsSize() / 8) > size) {
                    size = t.bitsSize() / 8;
                }
            } else {
                offset = alignUp(size, alignment);
                size = offset + (t.bitsSize() / 8);
            }

            if (alignment > alignment_of_container) {
                alignment_of_container = alignment;
            }

            return offset;
        }

        public long alignment() {
            return alignment_of_container;
        }

        public long size() {
            // need to be multiple of alignment requirement
            return alignUp(size, alignment_of_container);
        }

        public long[] offsets() {
            return offsets;
        }

        public long offset(int index) {
            return offsets[index];
        }
    }

    /**
     * The size of a given type considering alignment requirement
     */
    private long sizeof(Layout t) {
        if (t instanceof Value) {
            return t.bitsSize() / 8;
        } else if (t instanceof Sequence) {
            return sizeOfArray((Sequence) t);
        } else if (t instanceof Group) {
            return new ContainerSizeInfo((Group) t, -1).size();
        } else if (t instanceof Address) {
            return t.bitsSize() / 8;
        } else {
            throw new IllegalArgumentException("Invalid type: " + t);
        }
    }

    /**
     * Align the specified type from a given address
     * @return The address the data should be at based on alignment requirement
     */
    long align(Layout t, boolean isVar, long addr) {
        return alignUp(addr, alignment(t, isVar));
    }

    // natives
    private static native UpcallStub getUpcallStub(long addr);
    private static native void freeUpcallStub(long addr);

    private static native void registerNatives();
    static {
        registerNatives();
    }
}

