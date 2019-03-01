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
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.spi.ToolProvider;
import java.util.logging.Logger;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.ModuleVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;

// Utility class to generate a .jmod file
public final class JModWriter {
    private final Options options;
    private final Log log;
    private final Writer writer;

    private static ToolProvider findTool(String name) {
        Optional<ToolProvider> tp = ToolProvider.findFirst(name);
        if (!tp.isPresent()) {
            throw new RuntimeException("Cannot find " + name);
        }

        return tp.get();
    }

    private static final ToolProvider JMOD = findTool("jmod");

    public JModWriter(Context ctx, Writer writer) {
        this.options = ctx.options;
        this.log = ctx.log;
        this.writer = writer;
    }

    public void writeJModFile(Path jmodFile, String[] args) throws IOException {
        if (options.targetPackage == null || options.targetPackage.isEmpty()) {
            throw new IllegalArgumentException("no --target-package specified");
        }

        log.print(Level.INFO, () -> "Collecting jmod file " + jmodFile);

        String modName = jmodFile.getFileName().toString();
        modName = modName.substring(0, modName.length() - 5 /* ".jmod".length() */);
        // FIXME: validate modName

        Path jmodRootDir = Files.createTempDirectory("jextract.jmod");
        jmodRootDir.toFile().deleteOnExit();

        log.print(Level.INFO, () -> "Writing .class files");
        // write .class files
        Path modClassesDir = jmodRootDir.resolve(modName);
        writer.writeClassFiles(modClassesDir, args);

        log.print(Level.INFO, () -> "Generating module-info.class");
        // generate module-info.class
        generateModuleInfoClass(modClassesDir, modName);

        // copy libraries
        Path libsDir = jmodRootDir.resolve("libs");
        copyNativeLibraries(libsDir);

        // generate .jmod file
        generateJMod(modClassesDir, libsDir, jmodFile);
    }

    private void generateModuleInfoClass(Path modClassesDir, String modName) throws IOException {
        // collect package names
        final Set<String> packages = new HashSet<>();
        for (String cls : writer.results().keySet()) {
            int idx = cls.lastIndexOf("/");
            packages.add(cls.substring(0, idx));
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(Opcodes.V9, Opcodes.ACC_MODULE, "module-info", null, null, null);

        ModuleVisitor mv = cw.visitModule(modName, Opcodes.ACC_MANDATED, null);
        mv.visitRequire("java.base", Opcodes.ACC_MANDATED, null);
        for (String pkg : packages) {
            mv.visitExport(pkg, Opcodes.ACC_MANDATED);
        }
        mv.visitEnd();

        cw.visitEnd();

        // write module-info.class source in module directory
        Files.write(modClassesDir.resolve("module-info.class"), cw.toByteArray());
    }

    private void copyNativeLibraries(Path libsDir) throws IOException {
        Files.createDirectory(libsDir);
        if (!options.libraryNames.isEmpty()) {
            if (options.libraryPaths.isEmpty()) {
                log.printWarning("warn.no.library.paths.specified");
                return;
            }
            log.print(Level.INFO, () -> "Copying native libraries");
            Path[] paths = options.libraryPaths.stream().map(Paths::get).toArray(Path[]::new);
            options.libraryNames.forEach(libName -> {
                Optional<Path> absPath = Utils.findLibraryPath(paths, libName);
                if (absPath.isPresent()) {
                    Path libPath = absPath.get();
                    try {
                        Files.copy(absPath.get(), libsDir.resolve(libPath.getFileName()));
                    } catch (IOException ioExp) {
                        throw new UncheckedIOException(ioExp);
                    }
                } else {
                    log.printWarning("warn.library.not.copied", libName);
                }
            });
        }
    }

    private void generateJMod(Path classesDir, Path libsDir, Path jmodFile)
            throws IOException {
        log.print(Level.INFO, () -> "Generating jmod file: " + jmodFile);
        int exitCode = JMOD.run(log.getOut(), log.getErr(), "create",
            "--class-path", classesDir.toString(),
            "--libs", libsDir.toString(),
            jmodFile.toString());

        if (exitCode != 0) {
            throw new RuntimeException("jmod generation failed: " + exitCode);
        }
    }
}
