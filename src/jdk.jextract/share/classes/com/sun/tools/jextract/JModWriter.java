/*
 *  Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.
 *
 *  This code is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  version 2 for more details (a copy is included in the LICENSE file that
 *  accompanied this code).
 *
 *  You should have received a copy of the GNU General Public License version
 *  2 along with this work; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *  Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 */

package com.sun.tools.jextract;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.spi.ToolProvider;
import java.util.logging.Logger;

// Utility class to generate a .jmod file
public final class JModWriter {
    private final Context ctx;
    private final Writer writer;

    private static ToolProvider findTool(String name) {
        Optional<ToolProvider> tp = ToolProvider.findFirst(name);
        if (!tp.isPresent()) {
            throw new RuntimeException("Cannot find " + name);
        }

        return tp.get();
    }

    private static final ToolProvider JAVAC = findTool("javac");
    private static final ToolProvider JMOD = findTool("jmod");

    public JModWriter(Context ctx, Writer writer) {
        this.ctx = ctx;
        this.writer = writer;
    }

    public void writeJModFile(Path jmodFile, String[] args) throws IOException {
        if (ctx.options.targetPackage == null || ctx.options.targetPackage.isEmpty()) {
            throw new IllegalArgumentException("no --target-package specified");
        }

        ctx.log.print(Level.INFO, () -> "Collecting jmod file " + jmodFile);

        String modName = jmodFile.getFileName().toString();
        modName = modName.substring(0, modName.length() - 5 /* ".jmod".length() */);
        // FIXME: validate modName

        Path jmodRootDir = Files.createTempDirectory("jextract.jmod");
        jmodRootDir.toFile().deleteOnExit();

        ctx.log.print(Level.INFO, () -> "Writing .class files");
        // write .class files
        Path modClassesDir = jmodRootDir.resolve(modName);
        writer.writeClassFiles(modClassesDir, args);

        ctx.log.print(Level.INFO, () -> "Generating module-info.class");
        // generate module-info.class
        generateModuleInfoClass(jmodRootDir, modClassesDir, modName);

        // copy libraries
        Path libsDir = jmodRootDir.resolve("libs");
        copyNativeLibraries(libsDir);

        // generate .jmod file
        generateJMod(modClassesDir, libsDir, jmodFile);
    }

    private void generateModuleInfoClass(Path jmodRootDir, Path modClassesDir, String modName) throws IOException {
        // collect package names
        final Set<String> packages = new HashSet<>();
        for (String cls : writer.results().keySet()) {
            int idx = cls.lastIndexOf(".");
            packages.add(cls.substring(0, idx));
        }

        // module-info.java source code string
        StringBuilder modInfoCode = new StringBuilder();
        modInfoCode.append("module ");
        modInfoCode.append(modName);
        modInfoCode.append(" {\n");
        for (String pkg : packages) {
            modInfoCode.append("    exports ");
            modInfoCode.append(pkg);
            modInfoCode.append(";\n");
        }
        modInfoCode.append("}");

        // write module-info.java source in module directory
        Files.write(modClassesDir.resolve("module-info.java"), List.of(modInfoCode.toString()));

        // compile module-info.java
        int exitCode = JAVAC.run(ctx.log.getOut(), ctx.log.getErr(),
            "--module-source-path", jmodRootDir.toString(),
            "-d", jmodRootDir.toString(),
            modClassesDir.resolve("module-info.java").toString());

        if (exitCode != 0) {
            throw new RuntimeException("module-info.class generation failed: " + exitCode);
        }
    }

    private void copyNativeLibraries(Path libsDir) throws IOException {
        Files.createDirectory(libsDir);
        if (!ctx.options.libraryNames.isEmpty()) {
            if (ctx.options.libraryPaths.isEmpty()) {
                ctx.log.printWarning("warn.no.library.paths.specified");
                return;
            }
            ctx.log.print(Level.INFO, () -> "Copying native libraries");
            Path[] paths = ctx.options.libraryPaths.stream().map(Paths::get).toArray(Path[]::new);
            ctx.options.libraryNames.forEach(libName -> {
                Optional<Path> absPath = Utils.findLibraryPath(paths, libName);
                if (absPath.isPresent()) {
                    Path libPath = absPath.get();
                    try {
                        Files.copy(absPath.get(), libsDir.resolve(libPath.getFileName()));
                    } catch (IOException ioExp) {
                        throw new UncheckedIOException(ioExp);
                    }
                } else {
                    ctx.log.printWarning("warn.library.not.copied", libName);
                }
            });
        }
    }

    private void generateJMod(Path classesDir, Path libsDir, Path jmodFile)
            throws IOException {
        ctx.log.print(Level.INFO, () -> "Generating jmod file: " + jmodFile);
        int exitCode = JMOD.run(ctx.log.getOut(), ctx.log.getErr(), "create",
            "--class-path", classesDir.toString(),
            "--libs", libsDir.toString(),
            jmodFile.toString());

        if (exitCode != 0) {
            throw new RuntimeException("jmod generation failed: " + exitCode);
        }
    }
}
