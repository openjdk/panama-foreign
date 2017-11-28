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

import jdk.internal.misc.Unsafe;
import jdk.internal.nicl.types.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nicl.Library;
import java.nicl.LibrarySymbol;
import java.nicl.NativeLibrary;
import java.nicl.RuntimeSupport;
import java.nicl.types.Pointer;
import java.nicl.types.PointerToken;

// FIXME: Grovel this?
class UnixDynamicLibraries {
    static final int RTLD_LAZY = 0x00001;
    static final int RTLD_NOW  = 0x00002;

    private static final long RTLD_DEFAULT = getConstants().getRtldDefault();
    private static final long RTLD_NEXT = getConstants().getRtldNext();

    static class Constants {
        private final int rtldDefault, rtldNext;

        Constants(int rtldDefault, int rtldNext) {
            this.rtldDefault = rtldDefault;
            this.rtldNext = rtldNext;
        }

        public int getRtldDefault() {
            return rtldDefault;
        }

        public int getRtldNext() {
            return rtldNext;
        }
    }

    private static Constants getConstants() {
        String os = System.getProperty("os.name");

        if ("Linux".equals(os)) {
            return new Constants(0, -1);
        } else if (os.contains("OS X") || "SunOS".equals(os)) {
            return new Constants(-2, -1);
        } else {
            // FIXME: Add additional OSes here
            throw new UnsupportedOperationException(os + " not supported (yet)");
        }
    }

    static final Pointer<Void> RTLD_DEFAULT_PTR = BoundedPointer.createNativeVoidPointer(RTLD_DEFAULT);

    private static final UnixDynamicLibraries INSTANCE = new UnixDynamicLibraries();

    static UnixDynamicLibraries getInstance() {
        return INSTANCE;
    }

    private final MethodHandle DLOPEN;
    private final MethodHandle DLCLOSE;
    private final MethodHandle DLERROR;
    private final MethodHandle DLSYM;
    private final PointerToken TOKEN = new PointerTokenImpl();

    private UnixDynamicLibraries() {
        try {
            // FIXME: Use pointers here
            // void* dlopen(const char* filename, int flag);
            DLOPEN = Util.findNative("dlopen", MethodType.methodType(long.class, long.class, int.class));

            // int dlclose(void* handle);
            DLCLOSE = Util.findNative("dlclose", MethodType.methodType(int.class, long.class));

            // char* dlerror(void);
            DLERROR = Util.findNative("dlerror", MethodType.methodType(long.class));

            // void* dlsym(void* handle, const char* symbol);
            DLSYM = Util.findNative("dlsym", MethodType.methodType(long.class, long.class, long.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    public Pointer<Void> dlopen(Pointer<Byte> filename, int flag) {
        try {
            long handle = (long)DLOPEN.invokeExact(filename.addr(TOKEN), flag);
            if (handle == 0) {
                return null;
            }

            return BoundedPointer.createNativeVoidPointer(handle);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException("Failed to invoke dlopen");
        }
    }

    public Pointer<Void> dlsym(Pointer<Void> handle, Pointer<Byte> symbol) {
        try {
            long addr = (long)DLSYM.invokeExact(handle.addr(TOKEN), symbol.addr(TOKEN));
            if (addr == 0) {
                return null;
            }

            return BoundedPointer.createNativeVoidPointer(addr);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException("Failed to invoke dlopen");
        }
    }

    public int dlclose(Pointer<Byte> handle) {
        try {
            return (int)DLCLOSE.invokeExact(handle.addr(TOKEN));
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException("Failed to invoke dlopen");
        }
    }

    public Pointer<Byte> dlerror() {
        try {
            long addr = (long)DLERROR.invokeExact();
            if (addr == 0) {
                return null;
            }

            return new BoundedPointer<>(NativeLibrary.createLayout(byte.class), new BoundedMemoryRegion(addr, RuntimeSupport.strlen(addr) + 1));
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException("Failed to invoke dlopen");
        }
    }
}
