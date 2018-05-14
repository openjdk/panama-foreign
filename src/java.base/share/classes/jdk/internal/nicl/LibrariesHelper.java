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

import jdk.internal.misc.JavaLangAccess;
import jdk.internal.misc.SharedSecrets;
import jdk.internal.misc.Unsafe;
import jdk.internal.org.objectweb.asm.Type;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.io.File;
import java.nicl.Library;
import java.nicl.Libraries;
import java.nicl.metadata.LibraryDependencies;
import java.nicl.metadata.NativeType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Optional;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class LibrariesHelper {

    static JavaLangAccess jlAccess = SharedSecrets.getJavaLangAccess();

    enum ImplType {
        HEADER, CIVILIZED;

        public String getImplName() {
            return name().charAt(0) + name().substring(1).toLowerCase();
        }
    }

    // Map of interface -> impl class (per ImplType)
    @SuppressWarnings("unchecked")
    private static final Map<Class<?>, Class<?>>[] IMPLEMENTATIONS = (Map<Class<?>, Class<?>>[]) new Map<?, ?>[ImplType.values().length];

    static {
        for (int i = 0; i < IMPLEMENTATIONS.length; i++) {
            IMPLEMENTATIONS[i] = new WeakHashMap<>();
        }
    }

    private static final Unsafe U = Unsafe.getUnsafe();

    private static String generateImplName(Class<?> c, ImplType type) {
        return Type.getInternalName(c) + "$" + type.getImplName() + "Impl";
    }

    public static <T> Class<? extends T> getImplClass(Class<T> c) {
        return getOrCreateImpl(c, getSymbolLookupForClass(c));
    }

    /**
     * Look up the implementation for an interface, or generate it if needed
     *
     * @param c the interface for which to return an implementation class
     * @param generator a generator capable of generating an implementation, if needed
     * @return a class implementing the interface
     */
    @SuppressWarnings("unchecked")
    private static <T> Class<? extends T> getOrCreateImpl(ImplType type, Class<T> c, ImplGenerator generator) {
        Class<? extends T> implCls;

        Map<Class<?>, Class<?>> map = IMPLEMENTATIONS[type.ordinal()];

        synchronized (map) {
            implCls = (Class<? extends T>) map.get(c);
        }

        if (implCls == null) {
            Class<? extends T> newCls;
            try {
                newCls = (Class<? extends T>)AccessController.doPrivileged((PrivilegedAction<Class<?>>) () -> {
                        return generator.generate();
                    });
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate " + type + " for class " + c, e);
            }

            synchronized (map) {
                implCls = (Class<? extends T>) map.get(c);
                if (implCls == null) {
                    map.put(c, newCls);
                    implCls = newCls;
                }
            }
        }

        return implCls;
    }

    /**
     * Generate an implementation class for a header type
     *
     * @param c an interface representing a header file - must have an @Header annotation
     * @param lookup the symbol lookup to use to look up native symbols
     * @return a class implementing the header
     */
    private static <T> Class<? extends T> getOrCreateImpl(Class<T> c, SymbolLookup lookup)
            throws SecurityException, InternalError {
        /*
        if (!c.isAnnotationPresent(Header.class)) {
            throw new IllegalArgumentException("No @Header annotation on class " + c);
        }
        */

        String implClassName = generateImplName(c, ImplType.HEADER);

        boolean isRecordType = c.isAnnotationPresent(NativeType.class) && c.getAnnotation(NativeType.class).isRecordType();

        return getOrCreateImpl(ImplType.HEADER, c, new HeaderImplGenerator(c, implClassName, c, lookup, isRecordType));
    }

    private static <T> Class<? extends T> getOrCreateCivilizedImpl(Class<T> c, T rawInstance)
            throws SecurityException, InternalError {
        /*
        if (!c.isAnnotationPresent(Header.class)) {
            throw new IllegalArgumentException("No @Header annotation on class " + c);
        }
        */

        String implClassName = generateImplName(c, ImplType.CIVILIZED);

        return getOrCreateImpl(ImplType.CIVILIZED, c, new CivilizedHeaderImplGenerator<>(c, implClassName, c, rawInstance));
    }

    public static Library loadLibrary(Lookup lookup, String name) {
        return jlAccess.findLibrary(lookup, name);
    }

    // return the absolute path of the library of given name by searching
    // in the given array of paths.
    private static Optional<Path> findLibraryPath(Path[] paths, String libName) {
         return Arrays.stream(paths).
              map(p -> p.resolve(System.mapLibraryName(libName))).
              filter(Files::isRegularFile).map(Path::toAbsolutePath).findFirst();
    }

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

    private static Library[] loadLibraries(Lookup lookup, LibraryDependencies deps) {
        return loadLibraries(lookup, deps.paths(), deps.names());
    }

    private static LibraryDependencies getLibraryDependenciesForClass(Class<?> c) {
        if (c.isAnnotationPresent(LibraryDependencies.class)) {
            return c.getAnnotation(LibraryDependencies.class);
        } else {
            return null;
        }
    }

    private static SymbolLookup getSymbolLookupForClass(Class<?> c) {
        LibraryDependencies deps = getLibraryDependenciesForClass(c);

        Library[] libs;

        if (deps == null) {
            // FIXME: Require @LibraryDependencies on all relevant classes
            // System.err.println("WARNING: No @LibraryDependencies annotation on class " + c.getName());
            // throw new IllegalArgumentException("No @LibraryDependencies annotation on class " + c.getName());
            libs = new Library[] { getDefaultLibrary() };
        } else {
            libs = loadLibraries(MethodHandles.publicLookup().in(c), deps);
        }

        return new SymbolLookup(libs);
    }

    public static Library getDefaultLibrary() {
        return jlAccess.defaultLibrary();
    }

    public static <T> T bindRaw(Class<T> c, Library lib) {
        return bindRaw(c, new SymbolLookup(new Library[] { lib }));
    }

    private static <T> T bindRaw(Class<T> c, SymbolLookup lookup) {
        Class<? extends T> cls = getOrCreateImpl(c, lookup);

        try {
            //FIXME: Run some constructor here...?
            @SuppressWarnings("unchecked")
            T instance = (T) U.allocateInstance(cls);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    public static <T> T bindRaw(Class<T> c) {
        return bindRaw(c, getSymbolLookupForClass(c));
    }

    private static <T> Object bind(Class<T> c, SymbolLookup lookup) {
        try {
            T rawInstance = bindRaw(c);

            Class<?> civilizedCls = LibrariesHelper.getOrCreateCivilizedImpl(c, rawInstance);

            return U.allocateInstance(civilizedCls);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static <T> Object bind(Class<T> c, Library lib) {
        return bind(c, new SymbolLookup(lib));
    }

    public static <T> Object bind(Class<T> c) {
        return bind(c, getSymbolLookupForClass(c));
    }

    public static MethodHandle lookupNativeMethod(Library[] libs, String symbolName, MethodType methodType, boolean isVarArgs) throws NoSuchMethodException, IllegalAccessException {
        NativeInvoker invoker = new NativeInvoker(methodType, isVarArgs, new SymbolLookup(libs), symbolName);
        return invoker.getBoundMethodHandle().asCollector(Object[].class, methodType.parameterCount());
    }
}
