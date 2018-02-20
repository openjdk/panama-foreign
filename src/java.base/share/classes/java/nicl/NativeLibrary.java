/*
 * Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved.
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
package java.nicl;

import java.io.File;
import jdk.internal.nicl.Errno;
import jdk.internal.nicl.NativeLibraryImpl;
import jdk.internal.nicl.types.BindingRegistry;
import jdk.internal.nicl.types.Function;
import jdk.internal.nicl.types.Type;
import jdk.internal.nicl.types.LayoutTypeImpl;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.nicl.types.LayoutType;
import java.util.Objects;

public final class NativeLibrary {
    private static final Errno ERRNO = Errno.platformHasErrno() ? new Errno() : null;

    // don't create
    private NativeLibrary() {}

    /**
     * Create a raw, uncivilized version of the interface
     *
     * @param c the class to bind
     * @param lib the library in which to look for native symbols
     * @return
     */
    @Deprecated
    public static <T> T bindRaw(Class<T> c, Library lib) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(new RuntimePermission("java.nicl.bindRaw"));
        }
        return NativeLibraryImpl.bindRaw(c, lib);
    }

    /**
     * Create a raw, uncivilized version of the interface
     *
     * @param c the class to bind
     * @return
     */
    public static <T> T bindRaw(Class<T> c) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(new RuntimePermission("java.nicl.bindRaw"));
        }
        return NativeLibraryImpl.bindRaw(c);
    }

    /**
     * Create a civilized version of the interface
     *
     * @param c the raw, uncivilized version of the interface
     * @return
     */
    @Deprecated
    public static Object bind(Class<?> c) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(new RuntimePermission("java.nicl.bind"));
        }
        return NativeLibraryImpl.bind(c);
    }

    /**
     * Create a civilized version of the interface
     *
     * @param c the raw, uncivilized version of the interface
     * @param lib the library in which to look for native symbols
     * @return
     */
    @Deprecated
    public static Object bind(Class<?> c, Library lib) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(new RuntimePermission("java.nicl.bind"));
        }
        return NativeLibraryImpl.bind(c, lib);
    }

    /**
     * Lookup a method handle for a native function with the specified arguments
     *
     * @param libs the libraries in which to look for native symbols
     * @param symbolName the name of the symbol to look up
     * @param methodType the type of the returned method handle
     * @param isVarArgs true if the function is a varargs method
     * @return a method handle which, when invoked, will call the target native function
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     */
    public static MethodHandle lookupNativeMethod(Library[] libs, String symbolName, MethodType methodType, boolean isVarArgs) throws NoSuchMethodException, IllegalAccessException {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(new RuntimePermission("java.nicl.lookupNative", symbolName));
        }
        return NativeLibraryImpl.lookupNativeMethod(libs, symbolName, methodType, isVarArgs);
    }

    public static MethodHandle lookupNativeMethod(Library lib, String symbolName, MethodType methodType, boolean isVarArgs) throws NoSuchMethodException, IllegalAccessException {
        return lookupNativeMethod(new Library[] { lib }, symbolName, methodType, isVarArgs);
    }

    /**
     * Loads the native library specified by the <code>libname</code>
     * argument.  The <code>libname</code> argument must not contain any platform
     * specific prefix, file extension or path.
     *
     * Otherwise, the libname argument is loaded from a system library
     * location and mapped to a native library image in an implementation-
     * dependent manner.
     * <p>
     *
     * @param      libname   the name of the library.
     * @exception  SecurityException  if a security manager exists and its
     *             <code>checkLink</code> method doesn't allow
     *             loading of the specified dynamic library
     * @exception  UnsatisfiedLinkError if either the libname argument
     *             contains a file path, the native library is not statically
     *             linked with the VM,  or the library cannot be mapped to a
     *             native library image by the host system.
     * @exception  NullPointerException if <code>libname</code> is
     *             <code>null</code>
     * @see        java.lang.SecurityManager#checkLink(java.lang.String)
     */
    public static Library loadLibrary(String filename) {
        Objects.requireNonNull(filename);
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkLink(filename);
        }
        if (filename.indexOf(File.separatorChar) != -1) {
            throw new UnsatisfiedLinkError(
                "Directory separator should not appear in library name: " + filename);
        }
        return NativeLibraryImpl.loadLibrary(filename);
    }

    /**
     * Loads the native library specified by the filename argument.  The filename
     * argument must be an absolute path name.
     *
     * @param      filename   the file to load.
     * @exception  SecurityException  if a security manager exists and its
     *             <code>checkLink</code> method doesn't allow
     *             loading of the specified dynamic library
     * @exception  UnsatisfiedLinkError  if either the filename is not an
     *             absolute path name, the native library is not statically
     *             linked with the VM, or the library cannot be mapped to
     *             a native library image by the host system.
     * @exception  NullPointerException if <code>filename</code> is
     *             <code>null</code>
     * @see        java.lang.SecurityManager#checkLink(java.lang.String)
     */
    public static Library load(String filename) {
        Objects.requireNonNull(filename);
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkLink(filename);
        }
        if (!(new File(filename).isAbsolute())) {
            throw new UnsatisfiedLinkError(
                "Expecting an absolute path of the library: " + filename);
        }
        return NativeLibraryImpl.load(filename);
    }

    public static Library getDefaultLibrary() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(new RuntimePermission("java.nicl.getDefaultLibrary"));
        }
        return NativeLibraryImpl.getDefaultLibrary();
    }

    public static <T> LayoutType<T> createLayout(Class<T> c) {
        return LayoutTypeImpl.create(c);
    }

    public static int errno() {
        if (ERRNO == null) {
            throw new UnsupportedOperationException();
        }

        return ERRNO.errno();
    }

    public static void throwErrnoException(String msg) throws Exception {
        throw new Exception(msg + ": " + ERRNO.strerror(errno()));
    }

    public static void throwErrnoException() throws Exception {
        throw new Exception(ERRNO.strerror(errno()));
    }

    /**
     * Lookup a method handle for a native function with the specified arguments
     *
     * @param lib the library in which to look for the function
     * @param symbol the name of the symbol to look up
     * @param type the type descriptor of the native function
     */
    public static MethodHandle lookupNativeMethod(Library lib, String symbol, String type)
        throws NoSuchMethodException, IllegalAccessException {
        Function fn = (Function) Type.of(type);
        MethodType mt = BindingRegistry.getInstance().defaultMethodType(fn);
        return lookupNativeMethod(lib, symbol, mt, fn.isVarArg());
    }
}
