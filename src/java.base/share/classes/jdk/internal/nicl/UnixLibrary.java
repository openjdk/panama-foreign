/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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
package jdk.internal.nicl;

import java.nicl.Library;
import java.nicl.LibrarySymbol;
import java.nicl.NativeScope;
import java.nicl.Scope;
import java.nicl.types.Pointer;
import java.nicl.types.Transformer;

import static jdk.internal.nicl.UnixDynamicLibraries.RTLD_DEFAULT_PTR;

class UnixLibrary implements Library {
    private static final boolean DEBUG = Boolean.getBoolean("jdk.internal.nicl.UnixLibrary.DEBUG");

    private final Pointer<Void> handle;

    UnixLibrary(Pointer<Void> handle) {
        this.handle = handle;
    }

    @Override
    public LibrarySymbol lookup(String name) throws NoSuchMethodException {
        if (DEBUG) {
            System.err.println("Library.lookup(" + name + ")");
        }

        try (Scope scope = new NativeScope()) {
            Pointer<Byte> cname = Transformer.toCString(name, scope);

            Pointer<Void> h;
            if (handle == null) {
                h = RTLD_DEFAULT_PTR;
            } else {
                h = handle;
            }

            // Clear any previous error message (ignore return value)
            UnixDynamicLibraries.getInstance().dlerror();

            // Lookup symbol, NULL is not necessarily an error
            Pointer<Void> addr = UnixDynamicLibraries.getInstance().dlsym(h, cname);

            if (DEBUG) {
                System.err.println("dlsym(" + name + ") -> " + addr);
            }

            // Check for errors using dlerror()
            Pointer<Byte> errMsg = UnixDynamicLibraries.getInstance().dlerror();
            if (errMsg != null) {
                throw new NoSuchMethodException("Failed to look up " + name);
            }

            return new LibrarySymbol(name, addr);
        } catch (Throwable t) {
            throw new NoSuchMethodException("Failed to look up " + name);
        }
    }
}
