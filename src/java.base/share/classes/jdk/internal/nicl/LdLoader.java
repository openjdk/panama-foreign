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

import java.io.File;
import java.lang.reflect.Field;
import java.nicl.Library;
import java.nicl.NativeScope;
import java.nicl.Scope;
import java.nicl.types.Pointer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import static sun.security.action.GetPropertyAction.privilegedGetProperty;
import static jdk.internal.nicl.UnixDynamicLibraries.*;

public class LdLoader extends LibraryLoader {
    private static final boolean DEBUG = Boolean.parseBoolean(
        privilegedGetProperty("jdk.internal.nicl.LdLoader.DEBUG"));

    private final String[] usr_paths;

    // preloaded/built-in library
    private final UnixLibrary defaultLibrary;

    private String[] getUserClassPath() {
        return AccessController.doPrivileged((PrivilegedAction<String[]>)() -> {
            try {
                Field f = ClassLoader.class.getDeclaredField("usr_paths");
                f.setAccessible(true);
                return (String[])f.get(null);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public LdLoader() {
        this.usr_paths = getUserClassPath();
        this.defaultLibrary = new UnixLibrary(null);
    }

    @Override
    public Library getDefaultLibrary() {
        return defaultLibrary;
    }

    private Library tryLoadLibrary(String libPath) {
        if (DEBUG) {
            System.err.println("Trying " + libPath);
        }

        try (Scope scope = new NativeScope()) {
            Pointer<Byte> cname = scope.toCString(libPath);

            try {
                Pointer<Void> handle = UnixDynamicLibraries.getInstance().dlopen(cname, RTLD_LAZY);
                if (handle != null) {
                    return new UnixLibrary(handle);
                }

                if (DEBUG) {
                    Pointer<Byte> err = UnixDynamicLibraries.getInstance().dlerror();
                    if (err != null) {
                        System.err.println(Pointer.toString(err));
                    }
                }

                throw new UnsatisfiedLinkError("Can't load library: " + libPath);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    @Override
    public Library load(String name, boolean isAbsolute) {
        if (DEBUG) {
            System.err.println("Library.load(U, " + name + ", " + isAbsolute + ")");
        }

        if (isAbsolute) {
            return tryLoadLibrary(name);
        }

        for (String usr_path : usr_paths) {
            String libPath = usr_path + File.separator + name;
            try {
                return tryLoadLibrary(libPath);
            } catch (UnsatisfiedLinkError e) {
                // ignore and try next path
            }
        }

        throw new UnsatisfiedLinkError(name);
    }
}
