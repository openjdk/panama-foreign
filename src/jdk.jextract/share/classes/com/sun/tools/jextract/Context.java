/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
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

import jdk.internal.clang.*;
import jdk.internal.foreign.LibrariesHelper;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
import java.foreign.Library;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.jar.JarOutputStream;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * The setup for the tool execution
 */
public final class Context {
    // package name to TypeDictionary
    private final Map<String, TypeDictionary> tdMap;
    // The folder path mapping to package name
    private final Map<Path, String> pkgMap;
    // The header file parsed
    private final Map<Path, HeaderFile> headerMap;
    // The args for parsing C
    private final List<String> clangArgs;
    // The set of source header files
    private final Set<Path>  sources;
    // The list of library names
    private final List<String> libraryNames;
    // The list of library paths
    private final List<String> libraryPaths;
    // The list of library paths for link checks
    private final List<String> linkCheckPaths;
    // Symbol patterns to be excluded
    private final List<Pattern> excludeSymbols;

    final PrintWriter out;
    final PrintWriter err;

    private Predicate<String> symChecker;
    private Predicate<String> symFilter;

    private final MacroParser macroParser;

    private final static String defaultPkg = "jextract.dump";
    final Logger logger = Logger.getLogger(getClass().getPackage().getName());

    public Context(PrintWriter out, PrintWriter err) {
        this.tdMap = new HashMap<>();
        this.pkgMap = new HashMap<>();
        this.headerMap = new HashMap<>();
        this.clangArgs = new ArrayList<>();
        this.sources = new TreeSet<>();
        this.libraryNames = new ArrayList<>();
        this.libraryPaths = new ArrayList<>();
        this.linkCheckPaths = new ArrayList<>();
        this.excludeSymbols = new ArrayList<>();
        this.macroParser = new MacroParser();
        this.out = out;
        this.err = err;
    }

    public Context() {
        this(new PrintWriter(System.out, true), new PrintWriter(System.err, true));
    }

    TypeDictionary typeDictionaryFor(String pkg) {
        return tdMap.computeIfAbsent(pkg, p->new TypeDictionary(this, p));
    }

    void addClangArg(String arg) {
        clangArgs.add(arg);
    }

    public void addSource(Path path) {
        sources.add(path);
    }

    void addLibraryName(String name) {
        libraryNames.add(name);
    }

    void addLibraryPath(String path) {
        libraryPaths.add(path);
    }

    void addLinkCheckPath(String path) {
        linkCheckPaths.add(path);
    }

    void addExcludeSymbols(String pattern) {
        excludeSymbols.add(Pattern.compile(pattern));
    }

    boolean isSymbolFound(String name) {
        return symChecker == null? true : symChecker.test(name);
    }

    boolean isSymbolExcluded(String name) {
        return symFilter == null? false : symFilter.test(name);
    }

    /**
     * Setup a package name for a given folder.
     *
     * @param folder The path to the folder, use null to set catch-all.
     * @param pkg    The package name
     * @return True if the folder is setup successfully. False is a package
     * has been assigned for the folder.
     */
    public boolean usePackageForFolder(Path folder, String pkg) {
        if (folder != null) {
            folder = folder.toAbsolutePath();
            if (!Files.isDirectory(folder)) {
                folder = folder.getParent();
            }
        }
        String existing = pkgMap.putIfAbsent(folder, pkg);
        final String finalFolder = (null == folder) ? "all folders not configured" : folder.toString();
        if (null == existing) {
            logger.config(() -> "Package " + pkg + " is selected for " + finalFolder);
            return true;
        } else {
            logger.warning(() -> "Package " + existing + " had been selected for " + finalFolder + ", request to use " + pkg + " is ignored.");
            return false;
        }
    }

    static class Entity {
        final String pkg;
        final String entity;

        Entity(String pkg, String entity) {
            this.pkg = pkg;
            this.entity = entity;
        }
    }

    /**
     * Determine package and interface name given a path. If the path is
     * a folder, then only package name is determined. The package name is
     * determined with the longest path matching the setup. If the path is not
     * setup for any package, the default package name is returned.
     *
     * @param origin The source path
     * @return The Entity
     * @see Context::usePackageForFolder(Path, String)
     */
    Entity whatis(Path origin) {
        // normalize to absolute path
        origin = origin.toAbsolutePath();
        String filename = null;
        if (!Files.isDirectory(origin)) {
            // ensure it's a folder name
            filename = origin.getFileName().toString();
            origin = origin.getParent();
        }
        Path path = origin;

        // search the map for a hit with longest path
        while (path != null && !pkgMap.containsKey(path)) {
            path = path.getParent();
        }

        int start;
        String pkg;
        if (path != null) {
            start = path.getNameCount();
            pkg = pkgMap.get(path);
        } else {
            pkg = pkgMap.get(null);
            if (pkg == null) {
                start = 0;
                pkg = defaultPkg;
            } else {
                start = origin.getNameCount();
            }
        }

        if (filename == null) {
            // a folder, only pkg name matters
            return new Entity(pkg, null);
        }

        StringBuilder sb = new StringBuilder();
        while (start < origin.getNameCount()) {
            sb.append(Utils.toJavaIdentifier(origin.getName(start++).toString()));
            sb.append("_");
        }

        int ext = filename.lastIndexOf('.');
        if (ext != -1) {
            sb.append(filename.substring(0, ext));
        } else {
            sb.append(filename);
        }
        return new Entity(pkg, Utils.toClassName(sb.toString()));
    }

    HeaderFile getHeaderFile(Path header, HeaderFile main) {
        if (!Files.isRegularFile(header)) {
            logger.warning(() -> "Not a regular file: " + header.toString());
            throw new IllegalArgumentException(header.toString());
        }

        final Context.Entity e = whatis(header);
        return new HeaderFile(this, header, e.pkg, e.entity, main);
    }

    void defineMacro(Cursor cursor) {
        String macroName = cursor.spelling();
        if (cursor.isMacroFunctionLike()) {
            logger.fine(() -> "Skipping function-like macro " + macroName);
            return;
        }


        if (macroParser.isDefined(macroName)) {
            logger.fine(() -> "Macro " + macroName + " already handled");
            return;
        }

        logger.fine(() -> "Defining macro " + macroName);

        TranslationUnit tu = cursor.getTranslationUnit();
        SourceRange range = cursor.getExtent();
        String[] tokens = tu.tokens(range);

        macroParser.parse(cursor, tokens);
    }

    List<MacroParser.Macro> macros(HeaderFile header) {
        return macroParser.macros().stream()
                .filter(m -> m.getFileLocation().path().equals(header.path))
                .collect(Collectors.toList());
    }

    void processCursor(Cursor c, HeaderFile main, Function<HeaderFile, CodeFactory> fn) {
        SourceLocation loc = c.getSourceLocation();
        if (loc == null) {
            logger.info(() -> "Ignore Cursor " + c.spelling() + "@" + c.USR() + " has no SourceLocation");
            return;
        }
        logger.fine(() -> "Do cursor: " + c.spelling() + "@" + c.USR());

        HeaderFile header;
        boolean isBuiltIn = false;

        if (loc.isFromMainFile()) {
            header = main;
        } else {
            SourceLocation.Location src = loc.getFileLocation();
            if (src == null) {
                logger.info(() -> "Cursor " + c.spelling() + "@" + c.USR() + " has no FileLocation");
                return;
            }

            Path p = src.path();
            if (p == null) {
                logger.fine(() -> "Found built-in type: " + c.spelling());
                header = main;
                isBuiltIn = true;
            } else {
                p = p.normalize().toAbsolutePath();
                header = headerMap.get(p);
                if (header == null) {
                    final HeaderFile hf = header = getHeaderFile(p, main);
                    logger.config(() -> "First encounter of header file " + hf.path + ", assigned to package " + hf.pkgName);
                    // Only generate code for header files sepcified or in the same package
                    // System headers are excluded, they need to be explicitly specified in jextract cmdline
                    if (sources.contains(p) ||
                        (!loc.isInSystemHeader()) && (header.pkgName.equals(main.pkgName))) {
                        logger.config("Code gen for header " + p + " enabled in package " + header.pkgName);
                        header.useCodeFactory(fn.apply(header));
                    }
                    headerMap.put(p, header);
                }
            }
        }

        header.processCursor(c, main, isBuiltIn);
    }

    public void parse() {
        parse(header -> new AsmCodeFactory(this, header));
    }

    public void parse(Function<HeaderFile, CodeFactory> fn) {
        if (!libraryNames.isEmpty() && !linkCheckPaths.isEmpty()) {
            Library[] libs = LibrariesHelper.loadLibraries(MethodHandles.lookup(),
                linkCheckPaths.toArray(new String[0]),
                libraryNames.toArray(new String[0]));

            // check if the given symbol is found in any of the libraries or not.
            // If not found, warn the user for the missing symbol.
            symChecker = name -> {
                if (Main.DEBUG) {
                    err.println("Searching symbol: " + name);
                }
                return (Arrays.stream(libs).filter(lib -> {
                        try {
                            lib.lookup(name);
                            if (Main.DEBUG) {
                                err.println("Found symbol: " + name);
                            }
                            return true;
                        } catch (NoSuchMethodException nsme) {
                            return false;
                        }
                    }).findFirst().isPresent());
            };
        } else {
            symChecker = null;
        }

        if (!excludeSymbols.isEmpty()) {
            Pattern[] pats = excludeSymbols.toArray(new Pattern[0]);
            symFilter = name -> {
                return Arrays.stream(pats).filter(pat -> pat.matcher(name).matches()).
                    findFirst().isPresent();
            };
        } else {
            symFilter = null;
        }

        sources.forEach(path -> {
            if (headerMap.containsKey(path)) {
                logger.info(() -> path.toString() + " seen earlier via #include");
                return;
            }

            HeaderFile hf = headerMap.computeIfAbsent(path, p -> getHeaderFile(p, null));
            hf.useLibraries(libraryNames, libraryPaths);
            hf.useCodeFactory(fn.apply(hf));
            logger.info(() -> {
                StringBuilder sb = new StringBuilder(
                        "Parsing header file " + path + " with following args:\n");
                int i = 0;
                for (String arg : clangArgs) {
                    sb.append("arg[");
                    sb.append(i++);
                    sb.append("] = ");
                    sb.append(arg);
                    sb.append("\n");
                }
                return sb.toString();
            });

            Index index = LibClang.createIndex();
            Cursor tuCursor = index.parse(path.toString(),
                    d -> {
                        err.println(d);
                        if (d.severity() >  Diagnostic.CXDiagnostic_Warning) {
                            throw new RuntimeException(d.toString());
                        }
                    },
                    Main.INCLUDE_MACROS,
                    clangArgs.toArray(new String[0]));

            tuCursor.children()
                    .peek(c -> logger.finest(
                        () -> "Cursor: " + c.spelling() + "@" + c.USR() + "?" + c.isDeclaration()))
                    .filter(c -> c.isDeclaration() || c.isPreprocessing())
                    .forEach(c -> processCursor(c, hf, fn));
        });
    }

    private Map<String, List<CodeFactory>> getPkgCfMap() {
        final Map<String, List<CodeFactory>> mapPkgCf = new HashMap<>();
        // Build the pkg to CodeFactory map
        headerMap.values().forEach(header -> {
            CodeFactory cf = header.getCodeFactory();
            String pkg = header.pkgName;
            logger.config(() -> "File " + header + " is in package: " + pkg);
            if (cf == null) {
                logger.config(() -> "File " + header + " code generation is not activated!");
                return;
            }
            List<CodeFactory> l = mapPkgCf.computeIfAbsent(pkg, k -> new ArrayList<>());
            l.add(cf);
            logger.config(() -> "Add cf " + cf + " to pkg " + pkg + ", size is now " + l.size());
        });
        return Collections.unmodifiableMap(mapPkgCf);
    }

    public Map<String, byte[]> collectClasses(String... pkgs) {
        final Map<String, byte[]> rv = new HashMap<>();
        final Map<String, List<CodeFactory>> mapPkgCf = getPkgCfMap();
        for (String pkg_name : pkgs) {
            mapPkgCf.getOrDefault(pkg_name, Collections.emptyList())
                    .forEach(cf -> rv.putAll(cf.collect()));
        }
        return Collections.unmodifiableMap(rv);
    }

    void collectClassFiles(Path destDir, String... pkgs) throws IOException {
        try {
            collectClasses(pkgs).entrySet().stream().forEach(e -> {
                try {
                    String path = e.getKey().replace('.', File.separatorChar) + ".class";
                    logger.fine(() -> "Writing " + path);
                    Path fullPath = destDir.resolve(path).normalize();
                    Files.createDirectories(fullPath.getParent());
                    try (OutputStream fos = Files.newOutputStream(fullPath)) {
                        fos.write(e.getValue());
                        fos.flush();
                    }
                } catch (IOException ioe) {
                    throw new UncheckedIOException(ioe);
                }
            });
        } catch (UncheckedIOException uioe) {
            throw uioe.getCause();
        }
    }

    private void writeJar(CodeFactory cf, JarOutputStream jar) {
        cf.collect().entrySet().stream().forEach(e -> {
            try {
                String path = e.getKey().replace('.', File.separatorChar) + ".class";
                logger.fine(() -> "Add " + path);
                jar.putNextEntry(new ZipEntry(path));
                jar.write(e.getValue());
                jar.closeEntry();
            } catch (IOException ioe) {
                throw new UncheckedIOException(ioe);
            }
        });
    }

    public void collectJarFile(final JarOutputStream jos, String... pkgs) {
        final Map<String, List<CodeFactory>> mapPkgCf = getPkgCfMap();

        for (String pkg_name : pkgs) {
            // convert '.' to '/' to use as a path
            String entryName = Utils.toInternalName(pkg_name, "");
            // package folder
            if (!entryName.isEmpty()) {
                try {
                    jos.putNextEntry(new ZipEntry(entryName));
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
            logger.fine(() -> "Produce for package " + pkg_name);
            mapPkgCf.getOrDefault(pkg_name, Collections.emptyList())
                    .forEach(cf -> writeJar(cf, jos));
        }
    }

    void collectJarFile(final Path jar, String... pkgs) throws IOException {
        logger.info(() -> "Collecting jar file " + jar);
        try (OutputStream os = Files.newOutputStream(jar, CREATE, TRUNCATE_EXISTING, WRITE);
                JarOutputStream jo = new JarOutputStream(os)) {
            collectJarFile(jo, pkgs);
        } catch (UncheckedIOException uioe) {
            throw uioe.getCause();
        }
    }

    /**
     * Perform a local lookup, any undefined type will cause a JType
     * be defined within origin scope.
     *
     * @param type   The libclang type
     * @param origin The path of the file where type is encountered
     * @return The JType
     */
    JType getJType(final Type type, Path origin) {
        Path p = origin.normalize().toAbsolutePath();

        HeaderFile hf = headerMap.get(p);
        // We should not encounter a type if the header file reference to it is not yet processed
        assert(null != hf);
        if (hf == null) {
            throw new IllegalArgumentException("Failed to lookup header for " + p + " (origin: " + origin + ")");
        }

        return hf.localLookup(type);
    }

    /**
     * Perform a global lookup
     *
     * @param c The cursor define or declare the type.
     * @return
     */
    JType getJType(final Cursor c) {
        if (c.isInvalid()) {
            throw new IllegalArgumentException();
        }
        SourceLocation loc = c.getSourceLocation();
        if (null == loc) {
            return null;
        }
        Path p = loc.getFileLocation().path();
        if (null == p) {
            return null;
        }
        return getJType(c.type(), p);
    }
}
