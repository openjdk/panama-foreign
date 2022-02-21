/*
 * Copyright (c) 2021, 2022, Oracle and/or its affiliates. All rights reserved.
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

package jdk.internal.foreign;

import java.lang.foreign.MemoryAddress;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.NativeSymbol;
import java.lang.foreign.ResourceScope;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import jdk.internal.loader.NativeLibraries;
import jdk.internal.loader.NativeLibrary;
import jdk.internal.loader.RawNativeLibraries;
import sun.security.action.GetPropertyAction;

import static java.lang.foreign.ValueLayout.ADDRESS;

public class SystemLookup {

    private SystemLookup() { }

    static final SystemLookup INSTANCE = new SystemLookup();

    /*
     * On POSIX systems, dlsym will allow us to lookup symbol in library dependencies; the same trick doesn't work
     * on Windows. For this reason, on Windows we do not generate any side-library, and load msvcrt.dll directly instead.
     */
    private static final Function<String, Optional<NativeSymbol>> syslookup = switch (CABI.current()) {
        case SysV, LinuxAArch64, MacOsAArch64 -> libLookup(libs -> libs.load(jdkLibraryPath("syslookup")));
        case Win64 -> makeWindowsLookup(); // out of line to workaround javac crash
    };

    private static Function<String, Optional<NativeSymbol>> makeWindowsLookup() {
        Path system32 = Path.of(System.getenv("SystemRoot"), "System32");
        Path ucrtbase = system32.resolve("ucrtbase.dll");
        Path msvcrt = system32.resolve("msvcrt.dll");

        boolean useUCRT = Files.exists(ucrtbase);
        Path stdLib = useUCRT ? ucrtbase : msvcrt;
        Function<String, Optional<NativeSymbol>> lookup = libLookup(libs -> libs.load(stdLib));

        if (useUCRT) {
            // use a fallback lookup to look up inline functions from fallback lib

            Function<String, Optional<NativeSymbol>> fallbackLibLookup =
                    libLookup(libs -> libs.load(jdkLibraryPath("WinFallbackLookup")));

            int numSymbols = WindowsFallbackSymbols.values().length;
            MemorySegment funcs = MemorySegment.ofAddress(fallbackLibLookup.apply("funcs").orElseThrow().address(),
                ADDRESS.byteSize() * numSymbols, ResourceScope.globalScope());

            Function<String, Optional<NativeSymbol>> fallbackLookup = name -> Optional.ofNullable(WindowsFallbackSymbols.valueOfOrNull(name))
                .map(symbol -> NativeSymbol.ofAddress(symbol.name(), funcs.getAtIndex(ADDRESS, symbol.ordinal()), ResourceScope.globalScope()));

            final Function<String, Optional<NativeSymbol>> finalLookup = lookup;
            lookup = name -> finalLookup.apply(name).or(() -> fallbackLookup.apply(name));
        }

        return lookup;
    }

    private static Function<String, Optional<NativeSymbol>> libLookup(Function<RawNativeLibraries, NativeLibrary> loader) {
        NativeLibrary lib = loader.apply(RawNativeLibraries.newInstance(MethodHandles.lookup()));
        return name -> {
            Objects.requireNonNull(name);
            try {
                long addr = lib.lookup(name);
                return addr == 0 ?
                        Optional.empty() :
                        Optional.of(NativeSymbol.ofAddress(name, MemoryAddress.ofLong(addr), ResourceScope.globalScope()));
            } catch (NoSuchMethodException e) {
                return Optional.empty();
            }
        };
    }

    /*
     * Returns the path of the given library name from JDK
     */
    private static Path jdkLibraryPath(String name) {
        Path javahome = Path.of(GetPropertyAction.privilegedGetProperty("java.home"));
        String lib = switch (CABI.current()) {
            case SysV, LinuxAArch64, MacOsAArch64 -> "lib";
            case Win64 -> "bin";
        };
        String libname = System.mapLibraryName(name);
        return javahome.resolve(lib).resolve(libname);
    }


    public static SystemLookup getInstance() {
        return INSTANCE;
    }

    public Optional<NativeSymbol> lookup(String name) {
        return syslookup.apply(name);
    }

    // fallback symbols missing from ucrtbase.dll
    // this list has to be kept in sync with the table in the companion native library
    private enum WindowsFallbackSymbols {
        // stdio
        fprintf,
        fprintf_s,
        fscanf,
        fscanf_s,
        fwprintf,
        fwprintf_s,
        fwscanf,
        fwscanf_s,
        printf,
        printf_s,
        scanf,
        scanf_s,
        snprintf,
        sprintf,
        sprintf_s,
        sscanf,
        sscanf_s,
        swprintf,
        swprintf_s,
        swscanf,
        swscanf_s,
        vfprintf,
        vfprintf_s,
        vfscanf,
        vfscanf_s,
        vfwprintf,
        vfwprintf_s,
        vfwscanf,
        vfwscanf_s,
        vprintf,
        vprintf_s,
        vscanf,
        vscanf_s,
        vsnprintf,
        vsnprintf_s,
        vsprintf,
        vsprintf_s,
        vsscanf,
        vsscanf_s,
        vswprintf,
        vswprintf_s,
        vswscanf,
        vswscanf_s,
        vwprintf,
        vwprintf_s,
        vwscanf,
        vwscanf_s,
        wprintf,
        wprintf_s,
        wscanf,
        wscanf_s,

        // time
        gmtime
        ;

        static WindowsFallbackSymbols valueOfOrNull(String name) {
            try {
                return valueOf(name);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
}
