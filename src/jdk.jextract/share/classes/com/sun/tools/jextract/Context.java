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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.foreign.Library;
import java.foreign.Libraries;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.jar.JarOutputStream;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import com.sun.tools.jextract.parser.Parser;
import com.sun.tools.jextract.tree.FunctionTree;
import com.sun.tools.jextract.tree.HeaderTree;
import com.sun.tools.jextract.tree.Tree;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * The setup for the tool execution
 */
public final class Context {
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
    // Symbol patterns to be included
    private final List<Pattern> includeSymbols;
    // Symbol patterns to be excluded
    private final List<Pattern> excludeSymbols;
    // no NativeLocation info
    private boolean noNativeLocations;
    // generate static forwarder class or not?
    private boolean genStaticForwarder;

    final PrintWriter out;
    final PrintWriter err;

    private Predicate<String> symChecker;
    private Predicate<String> includeSymFilter;
    private Predicate<String> excludeSymFilter;

    private final Parser parser;

    final static String defaultPkg = "jextract.dump";
    final Logger logger = Logger.getLogger(getClass().getPackage().getName());

    public Context(PrintWriter out, PrintWriter err) {
        this.pkgMap = new HashMap<>();
        this.headerMap = new HashMap<>();
        this.clangArgs = new ArrayList<>();
        this.sources = new TreeSet<>();
        this.libraryNames = new ArrayList<>();
        this.libraryPaths = new ArrayList<>();
        this.linkCheckPaths = new ArrayList<>();
        this.includeSymbols = new ArrayList<>();
        this.excludeSymbols = new ArrayList<>();
        this.parser = new Parser(out, err, Main.INCLUDE_MACROS);
        this.out = out;
        this.err = err;
    }

    public Context() {
        this(new PrintWriter(System.out, true), new PrintWriter(System.err, true));
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

    boolean getNoNativeLocations() {
        return noNativeLocations;
    }

    void setNoNativeLocations() {
        noNativeLocations = true;
    }

    void addIncludeSymbols(String pattern) {
        includeSymbols.add(Pattern.compile(pattern));
    }

    void addExcludeSymbols(String pattern) {
        excludeSymbols.add(Pattern.compile(pattern));
    }

    void setGenStaticForwarder(boolean flag) {
        this.genStaticForwarder = flag;
    }

    // return the absolute path of the library of given name by searching
    // in the given array of paths.
    private static Optional<Path> findLibraryPath(Path[] paths, String libName) {
         return Arrays.stream(paths).
              map(p -> p.resolve(System.mapLibraryName(libName))).
              filter(Files::isRegularFile).map(Path::toAbsolutePath).findFirst();
    }

    /*
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

    private void initSymChecker() {
        if (!libraryNames.isEmpty() && !linkCheckPaths.isEmpty()) {
            try {
                Library[] libs = loadLibraries(MethodHandles.lookup(),
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
            } catch (UnsatisfiedLinkError ex) {
                err.println(Main.format("warn.lib.not.found"));
                symChecker = null;
            }
        } else {
            symChecker = null;
        }
    }

    private boolean isSymbolFound(String name) {
        return symChecker == null? true : symChecker.test(name);
    }

    private void initSymFilters() {
        if (!includeSymbols.isEmpty()) {
            Pattern[] pats = includeSymbols.toArray(new Pattern[0]);
            includeSymFilter = name -> {
                return Arrays.stream(pats).filter(pat -> pat.matcher(name).matches()).
                    findFirst().isPresent();
            };
        } else {
            includeSymFilter = null;
        }

        if (!excludeSymbols.isEmpty()) {
            Pattern[] pats = excludeSymbols.toArray(new Pattern[0]);
            excludeSymFilter = name -> {
                return Arrays.stream(pats).filter(pat -> pat.matcher(name).matches()).
                    findFirst().isPresent();
            };
        } else {
            excludeSymFilter = null;
        }
    }

    private boolean isSymbolIncluded(String name) {
        return includeSymFilter == null? true : includeSymFilter.test(name);
    }

    private boolean isSymbolExcluded(String name) {
        return excludeSymFilter == null? false : excludeSymFilter.test(name);
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
        HeaderFile headerFile = new HeaderFile(this, header, e.pkg, e.entity, main);
        headerFile.useLibraries(libraryNames, libraryPaths);
        return headerFile;
    }

    void processTree(Tree tree, HeaderFile main, Function<HeaderFile, AsmCodeFactory> fn) {
        SourceLocation loc = tree.location();

        HeaderFile header;
        boolean isBuiltIn = false;

        if (tree.isFromMain()) {
            header = main;
        } else {
            SourceLocation.Location src = loc.getFileLocation();
            if (src == null) {
                logger.info(() -> "Tree " + tree.name() + "@" + tree.USR() + " has no FileLocation");
                return;
            }

            Path p = src.path();
            if (p == null) {
                logger.fine(() -> "Found built-in type: " + tree.name());
                header = main;
                isBuiltIn = true;
            } else {
                p = p.normalize().toAbsolutePath();
                header = headerMap.get(p);
                if (header == null) {
                    final HeaderFile hf = header = getHeaderFile(p, main);
                    logger.config(() -> "First encounter of header file " + hf.path + ", assigned to package " + hf.pkgName);
                    // Only generate code for header files specified or in the same package
                    if (sources.contains(p) ||
                        (header.pkgName.equals(main.pkgName))) {
                        logger.config("Code gen for header " + p + " enabled in package " + header.pkgName);
                        header.useCodeFactory(fn.apply(header));
                    }
                    headerMap.put(p, header);
                }
            }
        }

        header.processTree(tree, main, isBuiltIn);
    }

    public void parse() {
        parse(header -> genStaticForwarder?
            new AsmCodeFactoryExt(this, header) : new AsmCodeFactory(this, header));
    }

    private boolean symbolFilter(Tree tree) {
         String name = tree.name();
         if (!isSymbolIncluded(name) || isSymbolExcluded(name)) {
             return false;
         }

         // check for function symbols in libraries & warn missing symbols
         if (tree instanceof FunctionTree && !isSymbolFound(name)) {
             err.println(Main.format("warn.symbol.not.found", name));
             //auto-exclude symbols not found
             return false;
         }

         return true;
    }

    public HeaderFile headerFor(Path p) {
        return headerMap.get(p);
    }

    public void parse(Function<HeaderFile, AsmCodeFactory> fn) {
        initSymChecker();
        initSymFilters();

        List<HeaderTree> headers = parser.parse(sources, clangArgs);
        processHeaders(headers, fn);
    }

    private void processHeaders(List<HeaderTree> headers, Function<HeaderFile, AsmCodeFactory> fn) {
        headers.stream().
                map(new TreeFilter(this::symbolFilter)).
                map(new TypedefHandler()).
                map(new EmptyNameHandler()).
                forEach(header -> {
            HeaderFile hf = headerMap.computeIfAbsent(header.path(), p -> getHeaderFile(p, null));
            hf.useCodeFactory(fn.apply(hf));
            logger.info(() -> "Processing header file " + header.path());
            header.declarations().stream()
                    .peek(decl -> logger.finest(
                        () -> "Cursor: " + decl.name() + "@" + decl.USR() + "?" + decl.isDeclaration()))
                    .forEach(decl -> processTree(decl, hf, fn));
        });
    }

    private Map<String, List<AsmCodeFactory>> getPkgCfMap() {
        final Map<String, List<AsmCodeFactory>> mapPkgCf = new HashMap<>();
        // Build the pkg to CodeFactory map
        headerMap.values().forEach(header -> {
            AsmCodeFactory cf = header.getCodeFactory();
            String pkg = header.pkgName;
            logger.config(() -> "File " + header + " is in package: " + pkg);
            if (cf == null) {
                logger.config(() -> "File " + header + " code generation is not activated!");
                return;
            }
            List<AsmCodeFactory> l = mapPkgCf.computeIfAbsent(pkg, k -> new ArrayList<>());
            l.add(cf);
            logger.config(() -> "Add cf " + cf + " to pkg " + pkg + ", size is now " + l.size());
        });
        return Collections.unmodifiableMap(mapPkgCf);
    }

    public Map<String, byte[]> collectClasses(String... pkgs) {
        final Map<String, byte[]> rv = new HashMap<>();
        final Map<String, List<AsmCodeFactory>> mapPkgCf = getPkgCfMap();
        for (String pkg_name : pkgs) {
            mapPkgCf.getOrDefault(pkg_name, Collections.emptyList())
                    .forEach(cf -> rv.putAll(cf.collect()));
        }
        return Collections.unmodifiableMap(rv);
    }

    private static final String JEXTRACT_MANIFEST = "META-INFO" + File.separatorChar + "jextract.properties";

    @SuppressWarnings("deprecation")
    private byte[] getJextractProperties(String[] args) {
        Properties props = new Properties();
        props.setProperty("os.name", System.getProperty("os.name"));
        props.setProperty("os.version", System.getProperty("os.version"));
        props.setProperty("os.arch", System.getProperty("os.arch"));
        props.setProperty("jextract.args", Arrays.toString(args));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        props.save(baos, "jextract meta data");
        return baos.toByteArray();
    }

    void collectClassFiles(Path destDir, String[] args, String... pkgs) throws IOException {
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

            Path propsPath = destDir.resolve(JEXTRACT_MANIFEST).normalize();
            Files.createDirectories(propsPath.getParent());
            try (OutputStream fos = Files.newOutputStream(propsPath)) {
                fos.write(getJextractProperties(args));
                fos.flush();
            }
        } catch (UncheckedIOException uioe) {
            throw uioe.getCause();
        }
    }

    private void writeJar(AsmCodeFactory cf, JarOutputStream jar) {
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

    public void collectJarFile(final JarOutputStream jos, String[] args, String... pkgs) {
        final Map<String, List<AsmCodeFactory>> mapPkgCf = getPkgCfMap();

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

        try {
            jos.putNextEntry(new ZipEntry(JEXTRACT_MANIFEST));
            jos.write(getJextractProperties(args));
            jos.closeEntry();
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    void collectJarFile(final Path jar, String[] args, String... pkgs) throws IOException {
        logger.info(() -> "Collecting jar file " + jar);
        try (OutputStream os = Files.newOutputStream(jar, CREATE, TRUNCATE_EXISTING, WRITE);
                JarOutputStream jo = new JarOutputStream(os)) {
            collectJarFile(jo, args, pkgs);
        } catch (UncheckedIOException uioe) {
            throw uioe.getCause();
        }
    }
}
