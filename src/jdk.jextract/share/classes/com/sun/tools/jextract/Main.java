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

import jdk.internal.joptsimple.OptionException;
import jdk.internal.joptsimple.OptionParser;
import jdk.internal.joptsimple.OptionSet;
import jdk.internal.joptsimple.util.KeyValuePair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

public final class Main {
    public static final boolean DEBUG = Boolean.getBoolean("jextract.debug");

    // FIXME: Remove this if/when the macros support is deemed stable
    public static boolean INCLUDE_MACROS = Boolean.parseBoolean(System.getProperty("jextract.INCLUDE_MACROS", "true"));

    private static final String MESSAGES_RESOURCE = "com.sun.tools.jextract.resources.Messages";

    private static final ResourceBundle MESSAGES_BUNDLE;
    static {
        MESSAGES_BUNDLE = ResourceBundle.getBundle(MESSAGES_RESOURCE, Locale.getDefault());
    }

    public static String format(String msgId, Object... args) {
        return new MessageFormat(MESSAGES_BUNDLE.getString(msgId)).format(args);
    }

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
                throw new IllegalArgumentException(format("not.a.directory", kv.key));
            }
        }

        Validators.validPackageName(pkgName);
        ctx.usePackageForFolder(p, pkgName);
    }

    private void processHeader(Object header) {
        Path p = Paths.get((String) header);
        if (!Files.isReadable(p)) {
            throw new IllegalArgumentException(format("cannot.read.header.file", header));
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

    private void printHelpAndExit(OptionParser parser) {
        try {
            parser.printHelpOn(System.err);
        } catch (IOException ex) {
            if (Main.DEBUG) {
                ex.printStackTrace(System.err);
            }
        }
        System.exit(1);
    }

    public void run(String[] args) {
        OptionParser parser = new OptionParser();
        parser.accepts("dry-run", format("help.dry_run"));
        parser.accepts("I", format("help.I")).withRequiredArg();
        parser.accepts("L", format("help.L")).withRequiredArg();
        parser.accepts("l", format("help.l")).withRequiredArg();
        parser.accepts("o", format("help.o")).withRequiredArg();
        parser.accepts("t", format("help.t")).withRequiredArg();
        parser.accepts("m", format("help.m")).withRequiredArg();
        parser.accepts("h", format("help.h")).forHelp();
        parser.accepts("help", format("help.h")).forHelp();
        parser.accepts("C", format("help.C")).withRequiredArg();
        parser.accepts("log", format("help.log")).withRequiredArg();
        parser.accepts("?", format("help.h")).forHelp();
        parser.nonOptions(format("help.non.option"));

        OptionSet options = null;
        try {
             options = parser.parse(args);
        } catch (OptionException oe) {
             System.err.println(oe.getMessage());
             if (Main.DEBUG) {
                 oe.printStackTrace(System.err);
             }
             printHelpAndExit(parser);
        }

        if (args.length == 0 || options.has("h") || options.has("?") || options.has("help")) {
             printHelpAndExit(parser);
        }

        if (options.has("log")) {
            setupLogging(Level.parse((String) options.valueOf("log")));
        } else {
            setupLogging(Level.WARNING);
        }

        if (options.has("I")) {
            options.valuesOf("I").forEach(p -> ctx.clangArgs.add("-I" + p));
        }

        if (options.has("C")) {
            options.valuesOf("C").forEach(p -> ctx.clangArgs.add((String) p));
        }

        if (options.has("l")) {
            options.valuesOf("l").forEach(p -> ctx.libraries.add((String) p));
        }

        targetPackage = options.has("t") ? (String) options.valueOf("t") : "";
        if (!targetPackage.isEmpty()) {
            Validators.validPackageName(targetPackage);
        }

        if (options.has("m")) {
            options.valuesOf("m").forEach(this::processPackageMapping);
        }

        try {
            options.nonOptionArguments().stream().forEach(this::processHeader);
            ctx.parse(AsmCodeFactory::new);
        } catch (RuntimeException re) {
            System.err.println(re.getMessage());
            if (Main.DEBUG) {
                re.printStackTrace(System.err);
            }
            System.exit(2);
        }

        if (options.has("dry-run")) {
            System.exit(0);
        }

        String outputName = options.has("o")? (String)options.valueOf("o") :
            options.nonOptionArguments().get(0) + ".jar";
        Path jar = Paths.get(outputName);
        try {
            ctx.collectJarFile(jar, targetPackage);
        } catch (IOException ex) {
            System.err.println(format("cannot.write.jar.file", jar, ex));
            if (Main.DEBUG) {
                ex.printStackTrace(System.err);
            }
            System.exit(3);
        }
    }

    public static void main(String... args) {
        Main instance = new Main(Context.getInstance());

        instance.run(args);
    }

}
