/*
 * Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.foreign;

import jdk.internal.access.JavaLangAccess;
import jdk.internal.access.SharedSecrets;
import jdk.internal.foreign.MemorySessionImpl;
import jdk.internal.foreign.SystemLookup;
import jdk.internal.loader.NativeLibrary;
import jdk.internal.loader.RawNativeLibraries;
import jdk.internal.reflect.CallerSensitive;
import jdk.internal.reflect.Reflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * A symbol lookup. Exposes a lookup operation for searching symbol addresses by name, see {@link jdk.incubator.foreign.SymbolLookup#lookup(String)}.
 * A symbol lookup can be used to look up a symbol in a loaded library. Clients can obtain a {@linkplain #loaderLookup() loader lookup},
 * which can be used to search symbols in libraries loaded by the current classloader (e.g. using {@link System#load(String)},
 * or {@link System#loadLibrary(String)}).
 * Alternatively, clients can search symbols in the standard C library using a {@link CLinker}, which conveniently
 * implements this interface.
 * <p> Unless otherwise specified, passing a {@code null} argument, or an array argument containing one or more {@code null}
 * elements to a method in this class causes a {@link NullPointerException NullPointerException} to be thrown. </p>
 */
@FunctionalInterface
public interface SymbolLookup {

    /**
     * Looks up a symbol with given name in this lookup.
     *
     * @param name the symbol name.
     * @return the lookup symbol (if any).
     */
    Optional<MemorySegment> lookup(String name);

    /**
     * Obtains a symbol lookup suitable to find symbols in native libraries associated with the caller's classloader
     * (that is, libraries loaded using {@link System#loadLibrary} or {@link System#load}). The returned lookup
     * returns native symbols backed by a non-closeable, shared scope which keeps the caller's classloader
     * <a href="../../../java/lang/ref/package.html#reachability">reachable</a>.
     *
     * @return a symbol lookup suitable to find symbols in libraries loaded by the caller's classloader.
     * @throws IllegalCallerException if access to this method occurs from a module {@code M} and the command line option
     * {@code --enable-native-access} is either absent, or does not mention the module name {@code M}, or
     * {@code ALL-UNNAMED} in case {@code M} is an unnamed module.
     */
    @CallerSensitive
    static SymbolLookup loaderLookup() {
        Class<?> caller = Reflection.getCallerClass();
        ClassLoader loader = Objects.requireNonNull(caller.getClassLoader());
        MemorySessionImpl loaderSession = MemorySessionImpl.heapSession(loader);
        return name -> {
            Objects.requireNonNull(name);
            JavaLangAccess javaLangAccess = SharedSecrets.getJavaLangAccess();
            MemoryAddress addr = MemoryAddress.ofLong(javaLangAccess.findNative(loader, name));
            return addr == MemoryAddress.NULL
                    ? Optional.empty() :
                    Optional.of(MemorySegment.ofAddress(addr, 0L, loaderSession));
        };
    }

    /**
     * Look up a symbol in the standard libraries associated with this linker.
     * The set of symbols available for lookup is unspecified, as it depends on the platform and on the operating system.
     * @param name the symbol name
     * @return a zero-length segment, whose base address is the symbol address (if any).
     */
    static SymbolLookup systemLookup() {
        return SystemLookup.getInstance();
    }

    static SymbolLookup libraryLookup(String name, MemorySession session) {
        Objects.requireNonNull(name);
        RawNativeLibraries nativeLibraries = RawNativeLibraries.newInstance(MethodHandles.lookup());
        NativeLibrary library = nativeLibraries.load(name);
        return libraryLookup(library, session);
    }

    static SymbolLookup libraryLookup(Path path, MemorySession session) {
        Objects.requireNonNull(path);
        RawNativeLibraries nativeLibraries = RawNativeLibraries.newInstance(MethodHandles.lookup());
        NativeLibrary library = nativeLibraries.load(path);
        return libraryLookup(library, session);
    }

    private static SymbolLookup libraryLookup(NativeLibrary library, MemorySession session) {
        return name -> {
            Objects.requireNonNull(name);
            MemoryAddress addr = MemoryAddress.ofLong(library.find(name));
            return addr == MemoryAddress.NULL
                    ? Optional.empty() :
                    Optional.of(MemorySegment.ofAddress(addr, 0L, session));
        };
    }
}
