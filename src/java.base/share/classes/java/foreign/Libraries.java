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
package java.foreign;

import java.lang.invoke.MethodHandles.Lookup;
import java.io.File;
import java.util.Objects;
import jdk.internal.foreign.LibrariesHelper;

public final class Libraries {
    // don't create
    private Libraries() {}

    /**
     * Create a raw, uncivilized version of the interface
     *
     * @param <T> the interface type
     * @param c the interface class to bind
     * @param lib the library in which to look for native symbols
     * @return an object of class implementing the interface
     */
    public static <T> T bind(Class<T> c, Library lib) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(new RuntimePermission("java.foreign.bind"));
        }
        return LibrariesHelper.bind(Objects.requireNonNull(c), Objects.requireNonNull(lib));
    }

    /**
     * Create a raw, uncivilized version of the interface
     *
     * @param <T> the interface type
     * @param lookup the lookup object (used for implicit native library lookup)
     * @param c the class to bind
     * @return an object of class implementing the interface
     */
    public static <T> T bind(Lookup lookup, Class<T> c) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(new RuntimePermission("java.foreign.bind"));
        }
        return LibrariesHelper.bind(checkLookup(lookup), Objects.requireNonNull(c));
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
     * @param      lookup     the lookup object
     * @param      filename   the name of the library.
     * @return     the library object to be loaded.
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
    public static Library loadLibrary(Lookup lookup, String filename) {
        Objects.requireNonNull(filename);
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkLink(filename);
        }
        if (filename.indexOf(File.separatorChar) != -1) {
            throw new UnsatisfiedLinkError(
                "Directory separator should not appear in library name: " + filename);
        }
        return LibrariesHelper.loadLibrary(checkLookup(lookup), filename);
    }

    /**
     * Loads the native library specified by the filename argument.  The filename
     * argument must be an absolute path name.
     *
     * @param      lookup     the lookup object
     * @param      filename   the file to load.
     * @return     the library object to be loaded.
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
    public static Library load(Lookup lookup, String filename) {
        Objects.requireNonNull(filename);
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkLink(filename);
        }
        if (!(new File(filename).isAbsolute())) {
            throw new UnsatisfiedLinkError(
                "Expecting an absolute path of the library: " + filename);
        }
        return LibrariesHelper.load(checkLookup(lookup), filename);
    }

    public static Library getDefaultLibrary() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(new RuntimePermission("java.foreign.getDefaultLibrary"));
        }
        return LibrariesHelper.getDefaultLibrary();
    }

    private static Lookup checkLookup(Lookup lookup) {
        if (!Objects.requireNonNull(lookup).hasPrivateAccess()) {
            throw new IllegalArgumentException("Attempt to use non-private lookup object");
        }
        return lookup;
    }
}
