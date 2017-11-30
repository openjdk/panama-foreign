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

import jdk.internal.joptsimple.OptionParser;
import jdk.internal.joptsimple.OptionSet;
import jdk.internal.joptsimple.util.KeyValuePair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class Main {
    // FIXME: Remove this if/when the macros support is deemed stable
    public static boolean INCLUDE_MACROS = Boolean.parseBoolean(System.getProperty("jextract.INCLUDE_MACROS", "true"));

    final Context ctx;
    String targetPackage;

    public Main(Context ctx) {
        this.ctx = ctx;
    }

    private void processPackageMapping(Object arg) {
        String str = (String) arg;
        Path p = null;
        String pkgName;
        if (str.indexOf('=') == -1) {
            pkgName = str;
        } else {
            KeyValuePair kv = KeyValuePair.valueOf(str);
            p = Paths.get(kv.key);
            pkgName = kv.value;

            if (!Files.isDirectory(p)) {
                throw new IllegalArgumentException("Not a directory: " + kv.key);
            }
        }

        Validators.validPackageName(pkgName);
        ctx.usePackageForFolder(p, pkgName);
    }

    private void processHeader(Object header) {
        Path p = Paths.get((String) header);
        if (!Files.isReadable(p)) {
            throw new IllegalArgumentException("Cannot read the file: " + header);
        }
        p = p.toAbsolutePath();
        ctx.usePackageForFolder(p.getParent(), targetPackage);
        ctx.sources.add(p);
    }

    private void setupLogging(Level level) {
        Logger logger = ctx.logger;
        logger.setUseParentHandlers(false);
        ConsoleHandler log = new ConsoleHandler();
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s%n");
        log.setFormatter(new SimpleFormatter());
        logger.setLevel(level);
        log.setLevel(level);
        logger.addHandler(log);
    }

    public void run(String[] args) {
        OptionParser parser = new OptionParser();
        parser.accepts("I", "specify include files path").withRequiredArg();
        parser.accepts("L", "specify library path").withRequiredArg();
        parser.accepts("l", "specify a library").withRequiredArg();
        parser.accepts("o", "specify output jar file").withRequiredArg();
        parser.accepts("t", "target package for specified header files").withRequiredArg();
        parser.accepts("m", "specify package mapping as dir=pkg").withRequiredArg();
        parser.accepts("h", "print help").forHelp();
        parser.accepts("C", "pass through argument for clang").withRequiredArg();
        parser.accepts("log", "specify log level in j.u.l.Level name").withRequiredArg();
        parser.accepts("?", "print help").forHelp();
        parser.nonOptions("header files");

        OptionSet options = parser.parse(args);

        if (args.length == 0 || options.has("h") || options.has("?")) {
            try {
                parser.printHelpOn(System.out);
            } catch (IOException ex) {
            }
            System.exit(1);
        }

        if (options.has("log")) {
            setupLogging(Level.parse((String) options.valueOf("log")));
        }

        if (options.has("I")) {
            options.valuesOf("I").forEach(p -> ctx.clangArgs.add("-I" + p));
        }

        if (options.has("C")) {
            options.valuesOf("C").forEach(p -> ctx.clangArgs.add((String) p));
        }

        targetPackage = options.has("t") ? (String) options.valueOf("t") : "";
        if (!targetPackage.isEmpty()) {
            Validators.validPackageName(targetPackage);
        }

        if (options.has("m")) {
            options.valuesOf("m").forEach(this::processPackageMapping);
        }

        options.nonOptionArguments().stream().forEach(this::processHeader);
        try {
            ctx.parse(AsmCodeFactory::new);
        } catch (RuntimeException re) {
            re.printStackTrace(System.err);
            System.exit(2);
        }

        if (options.has("o")) {
            Path jar = Paths.get((String) options.valueOf("o"));
            try {
                ctx.collectJarFile(jar, targetPackage);
            } catch (IOException ex) {
                System.out.println("Error occurred producing jar file.");
                ex.printStackTrace(System.err);
                System.exit(3);
            }
        }
    }

    public static void main(String... args) {
        Main instance = new Main(Context.getInstance());

        instance.run(args);
    }

}
