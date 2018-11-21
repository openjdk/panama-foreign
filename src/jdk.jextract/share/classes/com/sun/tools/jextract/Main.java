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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.PatternSyntaxException;
import java.util.spi.ToolProvider;

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

    private final Context ctx;
    private String targetPackage;

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

        Utils.validPackageName(pkgName);
        ctx.usePackageForFolder(p, pkgName);
    }

    private void processHeader(Object header) {
        Path p = Paths.get((String) header);
        if (!Files.isReadable(p)) {
            throw new IllegalArgumentException(format("cannot.read.header.file", header));
        }
        p = p.toAbsolutePath();
        ctx.usePackageForFolder(p.getParent(), targetPackage);
        ctx.addSource(p);
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

    private void printHelp(OptionParser parser) {
        try {
            parser.printHelpOn(ctx.err);
        } catch (IOException ex) {
            if (Main.DEBUG) {
                ex.printStackTrace(ctx.err);
            }
        }
    }

    public int run(String[] args) {
        OptionParser parser = new OptionParser();
        parser.accepts("dry-run", format("help.dry_run"));
        parser.accepts("I", format("help.I")).withRequiredArg();
        // option is expected to specify paths to load shared libraries
        // to check & warn missing symbols during jextract session.
        parser.accepts("L", format("help.L")).withRequiredArg();
        parser.accepts("l", format("help.l")).withRequiredArg();
        parser.accepts("d", format("help.d")).withRequiredArg();
        parser.acceptsAll(List.of("o", "jar"), format("help.o")).withRequiredArg();
        parser.acceptsAll(List.of("t", "target-package"), format("help.t")).withRequiredArg();
        parser.acceptsAll(List.of("m", "package-map"), format("help.m")).withRequiredArg();
        parser.acceptsAll(List.of("?", "h", "help"), format("help.h")).forHelp();
        parser.accepts("C", format("help.C")).withRequiredArg();
        parser.accepts("include-symbols", format("help.include_symbols")).withRequiredArg();
        parser.accepts("log", format("help.log")).withRequiredArg();
        parser.accepts("no-locations", format("help.no.locations"));
        parser.accepts("exclude-symbols", format("help.exclude_symbols")).withRequiredArg();
        parser.accepts("rpath", format("help.rpath")).withRequiredArg();
        parser.accepts("infer-rpath", format("help.infer.rpath"));
        parser.accepts("static-forwarder", format("help.static.forwarder")).
            withRequiredArg().ofType(boolean.class);
        parser.nonOptions(format("help.non.option"));

        OptionSet options = null;
        try {
             options = parser.parse(args);
        } catch (OptionException oe) {
             ctx.err.println(oe.getMessage());
             if (Main.DEBUG) {
                 oe.printStackTrace(ctx.err);
             }
             printHelp(parser);
             return 1;
        }

        if (args.length == 0 || options.has("h")) {
             printHelp(parser);
             return args.length == 0? 1 : 0;
        }

        if (options.nonOptionArguments().isEmpty()) {
            ctx.err.println(format("err.no.input.files"));
            return 2;
        }

        if (options.has("log")) {
            setupLogging(Level.parse((String) options.valueOf("log")));
        } else {
            setupLogging(Level.WARNING);
        }

        if (options.has("I")) {
            options.valuesOf("I").forEach(p -> ctx.addClangArg("-I" + p));
        }

        // append the built-in headers directory
        ctx.addClangArg("-I" + getBuiltinHeadersDir());

        if (options.has("C")) {
            options.valuesOf("C").forEach(p -> ctx.addClangArg((String) p));
        }

        if (options.has("l")) {
            for (Object arg : options.valuesOf("l")) {
                String lib = (String)arg;
                if (lib.indexOf(File.separatorChar) != -1) {
                    ctx.err.println(format("l.name.should.not.be.path", lib));
                    return 1;
                }
                ctx.addLibraryName(lib);
            }
        }

        if (options.has("no-locations")) {
            ctx.setNoNativeLocations();
        }

        boolean infer_rpath = options.has("infer-rpath");
        if (options.has("rpath")) {
            if (infer_rpath) {
                //conflicting rpaths options
                ctx.err.println(format("warn.rpath.auto.conflict"));
                infer_rpath = false;
            }

            // "rpath" with no "l" option!
            if (options.has("l")) {
                options.valuesOf("rpath").forEach(p -> ctx.addLibraryPath((String) p));
            } else {
                ctx.err.println(format("warn.rpath.without.l"));
            }
        }

        // generate static forwarder class if user specified -l option
        boolean staticForwarder = true;
        if (options.has("static-forwarder")) {
            staticForwarder = (boolean)options.valueOf("static-forwarder");
        }
        ctx.setGenStaticForwarder(staticForwarder && options.has("l"));

        if (options.has("include-symbols")) {
            try {
                options.valuesOf("include-symbols").forEach(sym -> ctx.addIncludeSymbols((String) sym));
            } catch (PatternSyntaxException pse) {
                ctx.err.println(format("include.symbols.pattern.error", pse.getMessage()));
            }
        }

        if (options.has("exclude-symbols")) {
            try {
                options.valuesOf("exclude-symbols").forEach(sym -> ctx.addExcludeSymbols((String) sym));
            } catch (PatternSyntaxException pse) {
                ctx.err.println(format("exclude.symbols.pattern.error", pse.getMessage()));
            }
        }

        if (options.has("L")) {
            List<?> libpaths = options.valuesOf("L");
            // "L" with no "l" option!
            if (options.has("l")) {
                libpaths.forEach(p -> ctx.addLinkCheckPath((String) p));
                if (infer_rpath) {
                    libpaths.forEach(p -> ctx.addLibraryPath((String) p));
                }
            } else {
                ctx.err.println(format("warn.L.without.l"));
            }
        } else if (infer_rpath) {
            ctx.err.println(format("warn.rpath.auto.without.L"));
        }

        targetPackage = options.has("t") ? (String) options.valueOf("t") : "";
        if (!targetPackage.isEmpty()) {
            Utils.validPackageName(targetPackage);
        }

        if (options.has("m")) {
            options.valuesOf("m").forEach(this::processPackageMapping);
        }

        try {
            options.nonOptionArguments().stream().forEach(this::processHeader);
            ctx.parse();
        } catch (RuntimeException re) {
            ctx.err.println(re.getMessage());
            if (Main.DEBUG) {
                re.printStackTrace(ctx.err);
            }
            return 2;
        }

        if (options.has("dry-run")) {
            return 0;
        }

        boolean hasOutput = false;

        if (options.has("d")) {
            hasOutput = true;
            Path dest = Paths.get((String) options.valueOf("d"));
            dest = dest.toAbsolutePath();
            try {
                if (!Files.exists(dest)) {
                    Files.createDirectories(dest);
                } else if (!Files.isDirectory(dest)) {
                    ctx.err.println(format("not.a.directory", dest));
                    return 4;
                }
                ctx.collectClassFiles(dest, args, targetPackage);
            } catch (IOException ex) {
                ctx.err.println(format("cannot.write.class.file", dest, ex));
                if (Main.DEBUG) {
                    ex.printStackTrace(ctx.err);
                }
                return 5;
            }
        }

        String outputName;
        if (options.has("o")) {
            outputName = (String) options.valueOf("o");
        } else if (hasOutput) {
            return 0;
        } else {
            outputName =  Paths.get((String)options.nonOptionArguments().get(0)).getFileName() + ".jar";
        }

        try {
            ctx.collectJarFile(Paths.get(outputName), args, targetPackage);
        } catch (IOException ex) {
            ctx.err.println(format("cannot.write.jar.file", outputName, ex));
            if (Main.DEBUG) {
                ex.printStackTrace(ctx.err);
            }
            return 3;
        }

        return 0;
    }

    private static Path getBuiltinHeadersDir() {
        return Paths.get(System.getProperty("java.home"), "conf", "jextract");
    }

    public static void main(String... args) {
        Main instance = new Main(new Context());

        System.exit(instance.run(args));
    }

    public static class JextractToolProvider implements ToolProvider {
        @Override
        public String name() {
            return "jextract";
        }

        @Override
        public int run(PrintWriter out, PrintWriter err, String... args) {
            // defensive check to throw security exception early.
            // Note that the successful run of jextract under security
            // manager would require far more permissions like loading
            // library (clang), file system access etc.
            if (System.getSecurityManager() != null) {
                System.getSecurityManager().
                    checkPermission(new RuntimePermission("jextract"));
            }

            Main instance = new Main(new Context(out, err));
            return instance.run(args);
        }
    }
}
