/*
 *  Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.  Oracle designates this
 *  particular file as subject to the "Classpath" exception as provided
 *  by Oracle in the LICENSE file that accompanied this code.
 *
 *  This code is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  version 2 for more details (a copy is included in the LICENSE file that
 *  accompanied this code).
 *
 *  You should have received a copy of the GNU General Public License version
 *  2 along with this work; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *   Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 *
 */
package jdk.internal.foreign;

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.internal.access.JavaLangAccess;
import jdk.internal.access.SharedSecrets;

import java.lang.invoke.MethodHandles.Lookup;
import jdk.incubator.foreign.LibraryLookup;
import jdk.internal.access.foreign.NativeLibraryProxy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

public final class LibrariesHelper {
    private LibrariesHelper() {}

    private static final JavaLangAccess jlAccess = SharedSecrets.getJavaLangAccess();

    /**
     * Load the specified shared library.
     *
     * @param lookup Lookup object of the caller.
     * @param name Name of the shared library to load.
     */
    public static LibraryLookup loadLibrary(Lookup lookup, String name) {
        return new LibraryLookupImpl(jlAccess.loadLibrary(lookup, name));
    }

    /**
     * Load the specified shared library.
     *
     * @param lookup Lookup object of the caller.
     * @param path Path of the shared library to load.
     */
    public static LibraryLookup load(Lookup lookup, String path) {
        return new LibraryLookupImpl(jlAccess.load(lookup, path));
    }

    // return the absolute path of the library of given name by searching
    // in the given array of paths.
    private static Optional<Path> findLibraryPath(Path[] paths, String libName) {
         return Arrays.stream(paths).
              map(p -> p.resolve(System.mapLibraryName(libName))).
              filter(Files::isRegularFile).map(Path::toAbsolutePath).findFirst();
    }

    public static LibraryLookup getDefaultLibrary() {
        return new LibraryLookupImpl(jlAccess.defaultLibrary());
    }

    static class LibraryLookupImpl implements LibraryLookup {
        NativeLibraryProxy proxy;

        LibraryLookupImpl(NativeLibraryProxy proxy) {
            this.proxy = proxy;
        }

        @Override
        public MemoryAddress lookup(String name) throws NoSuchMethodException {
            long addr = proxy.lookup(name);
            return MemoryAddress.ofLong(addr);
        }
    }
}
