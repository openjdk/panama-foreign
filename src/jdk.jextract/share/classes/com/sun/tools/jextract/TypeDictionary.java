/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.jextract;

import jdk.internal.clang.Cursor;
import jdk.internal.clang.Type;
import jdk.internal.clang.TypeKind;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nicl.metadata.NativeType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.READ;

/**
 * A dictionary that find Java type for a given native type.
 * Each instance of TypeDictionary presents types for a given java package.
 */
final class TypeDictionary {
    private final Logger logger = Logger.getLogger(getClass().getPackage().getName());
    private Context ctx;
    private final String pkgName;
    // clang.Type.spelling() to java type
    private final Map<String, JType> typeMap;
    private final AtomicInteger serialNo;

    private int serialNo() {
        return serialNo.incrementAndGet();
    }

    TypeDictionary(Context ctx, String pkg) {
        this.ctx = ctx;
        this.pkgName = pkg;
        this.typeMap = new HashMap<>();
        this.serialNo = new AtomicInteger();
    }

/*
    public static boolean addType(Context ctx, Type t, Class<?> cls) {
        if (cls.isAnnotation() || cls.isArray()) {
            throw new IllegalArgumentException();
        }

        String pkg = cls.getPackage().getName();
        TypeDictionary dict = ctx.typeDictionaryFor(pkg);

        String type = t.spelling();
        return dict.addWithClass(type, cls);
    }

    boolean addType(String type, Class<?> cls) {
        String pkg = cls.getPackage().getName();
        // Class must be in this or sub packages
        if (! pkg.startsWith(pkgName)) {
            return false;
        }
        return addWithClass(type, cls);
    }

    private boolean addWithClass(String type, Class<?> cls) {
        assert(! cls.isArray());
        assert(! cls.isAnnotation());

        return (null == typeMap.putIfAbsent(type, JType.of(cls)));
    }
*/

    static class JarClassStreamer extends ClassLoader {
        private final Context ctx;
        private final HashMap<String, byte[]> bytecodes = new HashMap<>();

        JarClassStreamer(Context ctx, Path jar) {
            this.ctx = ctx;
            try (JarInputStream jis = new JarInputStream(Files.newInputStream(jar, READ))) {
                // List all classes in the jar
                for (JarEntry e = jis.getNextJarEntry(); e != null; e = jis.getNextJarEntry()) {
                    if (e.isDirectory()) {
                        jis.closeEntry();
                        continue;
                    }
                    String name = e.getName();
                    if (! name.endsWith(".class")) {
                        // Should not have file not class files
                        ctx.err.println("Warning: unexpected file " + name);
                    }
                    name = name.substring(0, name.length() - 6);
                    byte[] buf = new byte[4096];
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    int n;
                    while ((n = jis.read(buf)) > -1) {
                        bos.write(buf, 0, n);
                    }
                    bytecodes.put(name.replace(File.separatorChar, '.'), bos.toByteArray());
                }
            } catch (IOException ioe) {
                ctx.err.println("Failed to load types from jar file: " + jar.toString());
            }
        }

        public Stream<Class<?>> stream() {
            return bytecodes.entrySet().stream()
                    .map(e -> {
                        byte[] bytecode = e.getValue();
                        return defineClass(e.getKey(), bytecode, 0, bytecode.length);
                    });
        }
    }

    void loadJar(Path jar) {
        JarClassStreamer cl = new JarClassStreamer(ctx, jar);
        cl.stream().forEach(cls -> {
            Package pkg = cls.getPackage();
            TypeDictionary dict = ctx.typeDictionaryFor(pkg.getName());
            NativeType nt = cls.getAnnotation(NativeType.class);
            dict.typeMap.putIfAbsent(nt.ctype(), JType.of(cls));
        });
    }

    private static JType checkPrimitive(Type t) {
        switch (t.kind()) {
            case Void:
                return JType.Void;
            // signed integer
            case Char_S:
            case SChar:
            case Short:
            case Int:
            case Long:
            case LongLong:
            case Int128:
            // unsigned integer, size-compatible at groveller level
            // accomodate value-range needed at civilizer level
            case UShort:
            case UInt:
            case UInt128:
            case ULong:
            case ULongLong:
            case Char_U:
            case UChar:
                long size = t.size();
                if (size <= 1) {
                    return JType.Byte;
                } else if (size <= 2) {
                    return JType.Short;
                } else if (size <= 4) {
                    return JType.Int;
                } else if (size <= 8) {
                    return JType.Long;
                } else {
                    return JType.of(BigInteger.class);
                }
            case WChar:
            case Char16:
                return JType.Char;
            case Bool:
                return JType.Bool;
            case Double:
                return JType.Double;
            case Float:
                return JType.Float;
            case LongDouble:
                return JType.of(BigDecimal.class);
        }
        return null;
    }

    private JType exist(Type t) {
        JType jt = checkPrimitive(t);
        if (jt == null) {
            jt = typeMap.get(t.spelling());
        }
        return jt;
    }

    /**
     * @param t
     * @return
     */
    final JType get(Type t) {
        JType jt;

        switch(t.kind()) {
            case Unexposed:
                // Always look at canonical type
                jt = get(t.canonicalType());
                break;
            case ConstantArray:
                // Array of element type
                jt = get(t.getElementType());
                if (jt != null) {
                    jt = new JType.Array(jt);
                }
                break;
            case IncompleteArray:
                jt = get(t.getElementType());
                if (jt != null) {
                    jt = new PointerType(jt);
                }
                break;
            case Pointer:
                // Pointer type to non-primitive type need to be added to pointee dictionary explicitly
                // Pointee type can be from other dictionary
                jt = exist(t);
                if (jt == null) {
                    Type pointee = t.getPointeeType();
                    jt = checkPrimitive(pointee);
                    if (jt != null) {
                        jt = new PointerType(jt);
                    }
                    // leave out non-primitive type for caller to define
                }
                break;
            case Vector:
                switch ((int) t.size()) {
                    case 8:
                        jt = JType.Long;
                        break;
                    case 16:
                    case 32:
                    case 64:
                    default:
                        throw new UnsupportedOperationException("Support for vector size: " + t.size());
                }
                break;
            default:
                jt = exist(t);
        }

        if (null == jt) {
            logger.fine(() -> "Cannot find type for " + t.spelling());
        } else {
            final JType finalJt = jt;
            logger.fine(() -> "Found type " + finalJt.getDescriptor() + " for " + t.spelling());
            jt = (jt instanceof JType2) ? jt : JType2.bind(jt, t, t.getDeclarationCursor());
        }
        return jt;
    }

    final JType computeIfAbsent(Type t, Function<Type, JType> fn) {
        JType jt = get(t);
        if (jt != null) {
            return jt;
        }

        // avoid nested call of computeAbsent as fn is likely to define a type
        jt = fn.apply(t);
        JType rv = typeMap.putIfAbsent(t.spelling(), jt);
        // should we be alert in this situation?
        return (rv == null) ? jt : rv;
    }

    /**
     * Look up a type in this instance first, if cannot find it, try to
     * look into the origin(declaring) TypeDictionary.
     * @param t
     * @return
     * @throws com.sun.tools.jextract.TypeDictionary.NotDeclaredException
     */
    final JType lookup(Type t) throws NotDeclaredException {
        JType jt = get(t);
        if (jt == null && t.kind() != TypeKind.Pointer) {
            // Pointer type need to check with pointee type, as the declaration
            // might still be in same TypeDictionary
            Cursor c = t.getDeclarationCursor();
            if (c.isInvalid()) {
                logger.info(() -> "Type " + t.spelling() + " has invalid declaration cursor.");
                logger.fine(() -> Printer.Stringifier(p -> p.dumpType(t)));
                throw new NotDeclaredException(t);
            }
            jt = ctx.getJType(t.getDeclarationCursor());
        }
        return jt;
    }

    static class NotDeclaredException extends RuntimeException {
        private static final long serialVersionUID = -1L;

        private final Type type;

        public NotDeclaredException(Type t) {
            type = t;
        }

        public final Type getType() { return type; }
    }
}
