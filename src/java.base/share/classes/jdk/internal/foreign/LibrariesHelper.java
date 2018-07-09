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
package jdk.internal.foreign;

import jdk.internal.misc.JavaLangAccess;
import jdk.internal.misc.SharedSecrets;
import jdk.internal.org.objectweb.asm.Type;

import java.foreign.Scope;
import java.lang.invoke.MethodHandles.Lookup;
import java.foreign.Library;
import java.foreign.Libraries;
import java.foreign.annotations.NativeHeader;
import java.lang.ref.Cleaner;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.security.AccessController;
import java.security.PrivilegedAction;

public final class LibrariesHelper {
    private LibrariesHelper() {}

    private static final JavaLangAccess jlAccess = SharedSecrets.getJavaLangAccess();

    private static String generateImplName(Class<?> c) {
        return Type.getInternalName(c) + "$" + "Impl";
    }

    /**
     * Generate the implementation for an interface.
     *
     * @param c the interface for which to return an implementation class
     * @param generator a generator capable of generating an implementation, if needed
     * @return a class implementing the interface
     */
    private static Class<?> generateImpl(Class<?> c, BinderClassGenerator generator) {
        try {
            return AccessController.doPrivileged((PrivilegedAction<Class<?>>) () -> {
                    return generator.generate();
                });
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate implementation for class " + c, e);
        }
    }

    // Cache: Struct interface Class -> Impl Class.
    private static final ClassValue<Class<?>> STRUCT_IMPLEMENTATIONS = new ClassValue<>() {
        @Override
        protected Class<?> computeValue(Class<?> c) {
            assert Util.isCStruct(c);
            return generateImpl(c, new StructImplGenerator(c, generateImplName(c), c));
        }
    };

    /**
     * Get the implementation for a Struct interface.
     *
     * @param c the Struct interface for which to return an implementation class
     * @return a class implementing the interface
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<? extends T> getStructImplClass(Class<T> c) {
        if (!Util.isCStruct(c)) {
            throw new IllegalArgumentException("Not a Struct interface: " + c);
        }

        return (Class<? extends T>)STRUCT_IMPLEMENTATIONS.get(c);
    }

    // Cache: Callback interface Class -> Impl Class.
    private static final ClassValue<Class<?>> CALLBACK_IMPLEMENTATIONS = new ClassValue<>() {
        @Override
        protected Class<?> computeValue(Class<?> c) {
            assert Util.isCallback(c);
            return generateImpl(c, new CallbackImplGenerator(c, generateImplName(c), c));
        }
    };

    /**
     * Get the implementation for a Callback interface.
     *
     * @param c the Callback interface for which to return an implementation class
     * @return a class implementing the interface
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<? extends T> getCallbackImplClass(Class<T> c) {
        if (!Util.isCallback(c)) {
            throw new IllegalArgumentException("Not a Callback interface: " + c);
        }

        return (Class<? extends T>)CALLBACK_IMPLEMENTATIONS.get(c);
    }

    // This is used to pass the current SymbolLookup object to the header computeValue method below.
    private static final ThreadLocal<SymbolLookup> curSymLookup = new ThreadLocal<>();

    // This is used to clear global scopes when libraries are unloaded
    private static final Cleaner cleaner = Cleaner.create();

    // Cache: Header interface Class -> Impl Class.
    private static final ClassValue<Class<?>> HEADER_IMPLEMENTATIONS = new ClassValue<>() {
        @Override
        protected Class<?> computeValue(Class<?> c) {
            assert c.isAnnotationPresent(NativeHeader.class);
            assert curSymLookup.get() != null;
            String implName = generateImplName(c);
            Scope libScope = Scope.newNativeScope();
            BinderClassGenerator generator = new HeaderImplGenerator(c, implName, c, curSymLookup.get(), libScope);
            Class<?> lib = generateImpl(c, generator);
            cleaner.register(lib, libScope::close);
            return lib;
        }
    };

    /**
     * Get an implementation class for a header type
     *
     * @param c an interface representing a header file - must have an @NativeHeader annotation
     * @param lookup the symbol lookup to use to look up native symbols
     * @return a class implementing the header
     */
    @SuppressWarnings("unchecked")
    private static <T> Class<? extends T> getHeaderImplClass(Class<T> c, SymbolLookup lookup) {
        if (!c.isAnnotationPresent(NativeHeader.class)) {
            throw new IllegalArgumentException("No @NativeHeader annotation on class " + c);
        }

        // Thread local is used to pass additional argument to the header
        // implementation generator's computeValue method.
        try {
            curSymLookup.set(lookup);
            return (Class<? extends T>)HEADER_IMPLEMENTATIONS.get(c);
        } finally {
            curSymLookup.remove();
        }
    }

    /**
     * Load the specified shared library.
     *
     * @param lookup Lookup object of the caller.
     * @param name Name of the shared library to load.
     */
    public static Library loadLibrary(Lookup lookup, String name) {
        return jlAccess.loadLibrary(lookup, name);
    }

    /**
     * Load the specified shared library.
     *
     * @param lookup Lookup object of the caller.
     * @param path Path of the shared library to load.
     */
    public static Library load(Lookup lookup, String path) {
        return jlAccess.load(lookup, path);
    }

    // return the absolute path of the library of given name by searching
    // in the given array of paths.
    private static Optional<Path> findLibraryPath(Path[] paths, String libName) {
         return Arrays.stream(paths).
              map(p -> p.resolve(System.mapLibraryName(libName))).
              filter(Files::isRegularFile).map(Path::toAbsolutePath).findFirst();
    }

    /**
     * Load the specified shared libraries from the specified paths.
     *
     * @param lookup Lookup object of the caller.
     * @param pathStrs array of paths to load the shared libraries from.
     * @param names array of shared library names.
     */
    // used by jextract tool to load libraries for symbol checks.
    public static Library[] loadLibraries(Lookup lookup, String[] pathStrs, String[] names) {
        if (pathStrs == null || pathStrs.length == 0) {
            return Arrays.stream(names).map(
                name -> Libraries.loadLibrary(lookup, name)).toArray(Library[]::new);
        } else {
            Path[] paths = Arrays.stream(pathStrs).map(Paths::get).toArray(Path[]::new);
            return Arrays.stream(names).map(libName -> {
                Optional<Path> absPath = findLibraryPath(paths, libName);
                return absPath.isPresent() ?
                    Libraries.load(lookup, absPath.get().toString()) :
                    Libraries.loadLibrary(lookup, libName);
            }).toArray(Library[]::new);
        }
    }

    private static Library[] loadLibraries(Lookup lookup, NativeHeader nativeHeader) {
        return loadLibraries(lookup, nativeHeader.libraryPaths(), nativeHeader.libraries());
    }

    private static SymbolLookup getSymbolLookupForClass(Lookup lookup, Class<?> c) {
        NativeHeader nativeHeader = c.getAnnotation(NativeHeader.class);
        Library[] libs = nativeHeader == null || nativeHeader.libraries().length == 0 ?
            new Library[] { getDefaultLibrary() } :
            loadLibraries(lookup, nativeHeader);

        return new SymbolLookup(libs);
    }

    public static Library getDefaultLibrary() {
        return jlAccess.defaultLibrary();
    }

    /**
     * Create a raw, uncivilized version of the interface
     *
     * @param c the interface class to bind
     * @param lib the library in which to look for native symbols
     * @return an object of class implementing the interfacce
     */
    public static <T> T bind(Class<T> c, Library lib) {
        return bind(c, new SymbolLookup(lib));
    }

    private static <T> T bind(Class<T> c, SymbolLookup lookup) {
        Class<? extends T> cls = getHeaderImplClass(c, lookup);

        try {
            @SuppressWarnings("unchecked")
            T instance = (T) cls.getDeclaredConstructor().newInstance();
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    /**
     * Create a raw, uncivilized version of the interface
     *
     * @param lookup the lookup object (used for implicit native library lookup)
     * @param c the class to bind
     * @return an object of class implementing the interfacce
     */
    public static <T> T bind(Lookup lookup, Class<T> c) {
        return bind(c, getSymbolLookupForClass(lookup, c));
    }
}
