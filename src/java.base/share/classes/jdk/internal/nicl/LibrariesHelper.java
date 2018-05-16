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

import java.lang.invoke.MethodHandles.Lookup;
import java.nicl.Library;
import java.nicl.Libraries;
import java.nicl.metadata.NativeHeader;
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

    // Map of interface -> impl class
    private static final Map<Class<?>, Class<?>> IMPLEMENTATIONS = new WeakHashMap<>();

    private static final Unsafe U = Unsafe.getUnsafe();

    private static String generateImplName(Class<?> c) {
        return Type.getInternalName(c) + "$" + "Impl";
    }

    public static <T> Class<? extends T> getStructImplClass(Class<T> c) {
        return getOrCreateImpl(c, SymbolLookup.NO_LOOKUP);
    }

    /**
     * Look up the implementation for an interface, or generate it if needed
     *
     * @param c the interface for which to return an implementation class
     * @param generator a generator capable of generating an implementation, if needed
     * @return a class implementing the interface
     */
    @SuppressWarnings("unchecked")
    private static <T> Class<? extends T> getOrCreateImpl(Class<T> c, BinderClassGenerator generator) {
        Class<? extends T> implCls;

        synchronized (IMPLEMENTATIONS) {
            implCls = (Class<? extends T>) IMPLEMENTATIONS.get(c);
        }

        if (implCls == null) {
            Class<? extends T> newCls;
            try {
                newCls = (Class<? extends T>)AccessController.doPrivileged((PrivilegedAction<Class<?>>) () -> {
                        return generator.generate();
                    });
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate implementation for class " + c, e);
            }

            synchronized (IMPLEMENTATIONS) {
                implCls = (Class<? extends T>) IMPLEMENTATIONS.get(c);
                if (implCls == null) {
                    IMPLEMENTATIONS.put(c, newCls);
                    implCls = newCls;
                }
            }
        }

        return implCls;
    }

    /**
     * Generate an implementation class for a header type
     *
     * @param c an interface representing a header file - must have an @NativeHeader annotation
     * @param lookup the symbol lookup to use to look up native symbols
     * @return a class implementing the header
     */
    private static <T> Class<? extends T> getOrCreateImpl(Class<T> c, SymbolLookup lookup)
            throws SecurityException, InternalError {
        /*
        if (!c.isAnnotationPresent(NativeHeader.class)) {
            throw new IllegalArgumentException("No @NativeHeader annotation on class " + c);
        }
        */

        String implClassName = generateImplName(c);

        boolean isRecordType = c.isAnnotationPresent(NativeType.class) && c.getAnnotation(NativeType.class).isRecordType();
        BinderClassGenerator generator = isRecordType ?
                new StructImplGenerator(c, implClassName, c) :
                new HeaderImplGenerator(c, implClassName, c, lookup);

        return getOrCreateImpl(c, generator);
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

    public static <T> T bind(Class<T> c, Library lib) {
        return bind(c, new SymbolLookup(lib));
    }

    private static <T> T bind(Class<T> c, SymbolLookup lookup) {
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

    public static <T> T bind(Lookup lookup, Class<T> c) {
        return bind(c, getSymbolLookupForClass(lookup, c));
    }
}
