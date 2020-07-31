/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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

package jdk.incubator.jbind.core;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import jdk.incubator.foreign.CSupport;
import jdk.incubator.foreign.ForeignLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.LibraryLookup;
import jdk.incubator.foreign.LibraryLookup.Symbol;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryHandles;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.SequenceLayout;

public class RuntimeHelper {
    private final static ForeignLinker ABI = CSupport.getSystemLinker();
    private final static ClassLoader LOADER = RuntimeHelper.class.getClassLoader();

    private final static MethodHandles.Lookup MH_LOOKUP = MethodHandles.lookup();

    public static final LibraryLookup[] libraries(String... libNames) {
        if (libNames.length == 0) {
            return new LibraryLookup[] { LibraryLookup.ofDefault() };
        } else {
            return Arrays.stream(libNames)
                .map(libName -> LibraryLookup.ofLibrary(libName))
                .toArray(LibraryLookup[]::new);
        }
    }

    public static final LibraryLookup[] libraries(String[] libNames, String[] libPaths) {
        if (libNames.length == 0) {
            return new LibraryLookup[]{LibraryLookup.ofDefault()};
        } else {
            Path[] paths = Arrays.stream(libPaths).map(Paths::get).toArray(Path[]::new);
            return Arrays.stream(libNames).map(libName -> {
                Optional<Path> absPath = findLibraryPath(paths, libName);
                return absPath.isPresent() ?
                        LibraryLookup.ofPath(absPath.get().toString()) :
                        LibraryLookup.ofLibrary(libName);
            }).toArray(LibraryLookup[]::new);
        }
    }

    private static final Optional<Path> findLibraryPath(Path[] paths, String libName) {
        return Arrays.stream(paths).
                map(p -> p.resolve(System.mapLibraryName(libName))).
                filter(Files::isRegularFile).map(Path::toAbsolutePath).findFirst();
    }

    private static final Optional<Symbol> lookup(LibraryLookup[] LIBRARIES, String sym) {
        for (LibraryLookup l : LIBRARIES) {
            try {
                return Optional.of(l.lookup(sym));
            } catch (Throwable t) {
            }
        }
        try {
            return Optional.of(LibraryLookup.ofDefault().lookup(sym));
        } catch (Throwable t) {
            return Optional.empty();
        }
    }

    public static final MemorySegment lookupGlobalVariable(LibraryLookup[] LIBRARIES, String name, MemoryLayout layout) {
        return lookup(LIBRARIES, name).map(s ->
            MemorySegment.ofNativeRestricted(
                 s.address(), layout.byteSize(), null, null, s
            ).withAccessModes(MemorySegment.READ | MemorySegment.WRITE)).orElse(null);
    }

    public static final MethodHandle downcallHandle(LibraryLookup[] LIBRARIES, String name, String desc, FunctionDescriptor fdesc, boolean isVariadic) {
        Symbol symbol = lookup(LIBRARIES, name).orElse(null);
        if (symbol == null) return null;

        MethodType mt = MethodType.fromMethodDescriptorString(desc, LOADER);
        if (isVariadic) {
            return VarargsInvoker.make(symbol, mt, fdesc);
        } else {
            return ABI.downcallHandle(symbol, mt, fdesc);
        }
    }

    public static final MemorySegment upcallStub(MethodHandle handle, FunctionDescriptor fdesc) {
        return ABI.upcallStub(handle, fdesc);
    }

    public static final <Z> MemorySegment upcallStub(Class<Z> fi, Z z, FunctionDescriptor fdesc, String mtypeDesc) {
        try {
            MethodHandle handle = MH_LOOKUP.findVirtual(fi, "apply",
                    MethodType.fromMethodDescriptorString(mtypeDesc, LOADER));
            handle = handle.bindTo(z);
            return upcallStub(handle, fdesc);
        } catch (Throwable ex) {
            throw new AssertionError(ex);
        }
    }

    public static VarHandle varHandle(Class<?> carrier, MemoryLayout layout) {
        boolean isAddr = carrier == MemoryAddress.class;
        int dims = 0;
        MemoryLayout tmp = layout;
        while (tmp instanceof SequenceLayout) {
            dims++;
            tmp = ((SequenceLayout) tmp).elementLayout();
        }
        MemoryLayout.PathElement[] args = new MemoryLayout.PathElement[dims];
        Arrays.fill(args, MemoryLayout.PathElement.sequenceElement());
        VarHandle vh = layout.varHandle(isAddr ? long.class : carrier, args);
        if (isAddr) {
            vh = MemoryHandles.asAddressVarHandle(vh);
        }
        return vh;
    }
}
