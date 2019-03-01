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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.PatternSyntaxException;
import java.util.spi.ToolProvider;

public final class Main {
    public static final boolean DEBUG = Boolean.getBoolean("jextract.debug");

    // error codes
    private static final int OPTION_ERROR  = 1;
    private static final int INPUT_ERROR   = 2;
    private static final int OUTPUT_ERROR  = 3;
    private static final int RUNTIME_ERROR = 4;

    private final PrintWriter out;
    private final PrintWriter err;

    private Main(PrintWriter out, PrintWriter err) {
        this.out = out;
        this.err = err;
    }

    private void printHelp(OptionParser parser) {
        try {
            parser.printHelpOn(err);
        } catch (IOException ex) {
            throw new FatalError(RUNTIME_ERROR, ex);
        }
    }

    public static void processPackageMapping(Object arg, Options.Builder builder) {
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
                throw new IllegalArgumentException(Log.format("not.a.directory", kv.key));
            }
        }

        Utils.validPackageName(pkgName);
        builder.addPackageMapping(p, pkgName);
    }

    private Options createOptions(OptionSet options) {
        Options.Builder builder = Options.builder();

        if (options.has("I")) {
            options.valuesOf("I").forEach(p -> builder.addClangArg("-I" + p));
        }

        // append the built-in headers directory
        builder.addClangArg("-I" + Context.getBuiltinHeadersDir());

        if (options.has("C")) {
            options.valuesOf("C").forEach(p -> builder.addClangArg((String) p));
        }

        boolean librariesSpecified = options.has("l");
        if (librariesSpecified) {
            for (Object arg : options.valuesOf("l")) {
                String lib = (String)arg;
                if (lib.indexOf(File.separatorChar) != -1) {
                    throw new FatalError(OPTION_ERROR, Log.format("l.name.should.not.be.path", lib));
                }
                builder.addLibraryName(lib);
            }
        }

        if (options.has("no-locations")) {
            builder.setNoNativeLocations();
        }

        // generate static forwarder class if user specified -l option
        boolean staticForwarder = true;
        if (options.has("static-forwarder")) {
            staticForwarder = (boolean)options.valueOf("static-forwarder");
        }
        builder.setGenStaticForwarder(staticForwarder && options.has("l"));

        if (options.has("include-symbols")) {
            try {
                options.valuesOf("include-symbols").forEach(sym -> builder.addIncludeSymbols((String) sym));
            } catch (PatternSyntaxException pse) {
                throw new FatalError(OPTION_ERROR, Log.format("include.symbols.pattern.error", pse.getMessage()));
            }
        }

        if (options.has("exclude-symbols")) {
            try {
                options.valuesOf("exclude-symbols").forEach(sym -> builder.addExcludeSymbols((String) sym));
            } catch (PatternSyntaxException pse) {
                throw new FatalError(OPTION_ERROR, Log.format("exclude.symbols.pattern.error", pse.getMessage()));
            }
        }

        boolean recordLibraryPath = options.has("record-library-path");
        if (recordLibraryPath) {
            // "record-library-path" with no "l"
            if (!librariesSpecified) {
                err.println(Log.format("warn.record_library_path.without.l"));
            }
            builder.setRecordLibraryPath();
        }

        if (options.has("L")) {
            List<?> libpaths = options.valuesOf("L");
            if (librariesSpecified) {
                libpaths.forEach(p -> builder.addLibraryPath((String) p));
            } else {
                // "L" with no "l" option!
                err.println(Log.format("warn.L.without.l"));
            }
        } else {
            // "record-library-path" with no "L"
            if (recordLibraryPath) {
                err.println(Log.format("warn.record_library_path.without.L"));
            }

            // "l" with no "L"
            if (librariesSpecified) {
                err.println(Log.format("warn.l.without.L"));
                // assume java.library.path
                err.println(Log.format("warn.using.java.library.path"));
                String[] libPaths = System.getProperty("java.library.path").split(File.pathSeparator);
                for (String lp : libPaths) {
                    builder.addLibraryPath(lp);
                }
            }
        }

        if (options.has("package-map")) {
            options.valuesOf("package-map").forEach(p -> processPackageMapping(p, builder));
        }

        if (options.has("missing-symbols")) {
            String ms = options.valueOf("missing-symbols").toString();
            final MissingSymbolAction msa;
            try {
                msa = Enum.valueOf(MissingSymbolAction.class, ms.toUpperCase());
            } catch (IllegalArgumentException iae) {
                throw new FatalError(OPTION_ERROR, Log.format("invalid.missing_symbols.option.value", ms));
            }
            builder.setMissingSymbolAction(msa);
            if (!librariesSpecified) {
                err.println(Log.format("warn.missing_symbols.without.l"));
            }
        } else {
            // default is to exclude missing symbols
            builder.setMissingSymbolAction(MissingSymbolAction.EXCLUDE);
        }

        String targetPackage = options.has("t") ? (String) options.valueOf("t") : "";
        if (!targetPackage.isEmpty()) {
            Utils.validPackageName(targetPackage);
        }
        builder.setTargetPackage(targetPackage);

        return builder.build();
    }

    private int run(String[] args) {
        try {
            runInternal(args);
        } catch(FatalError e) {
            err.println(e.getMessage());
            if(Main.DEBUG) {
                e.printStackTrace(err);
            }
            return e.errorCode;
        }

        return 0;
    }

    private void runInternal(String[] args) {
        OptionParser parser = new OptionParser();
        parser.accepts("C", Log.format("help.C")).withRequiredArg();
        parser.accepts("I", Log.format("help.I")).withRequiredArg();
        parser.acceptsAll(List.of("L", "library-path"), Log.format("help.L")).withRequiredArg();
        parser.accepts("d", Log.format("help.d")).withRequiredArg();
        parser.accepts("dry-run", Log.format("help.dry_run"));
        parser.accepts("exclude-symbols", Log.format("help.exclude_symbols")).withRequiredArg();
        parser.accepts("include-symbols", Log.format("help.include_symbols")).withRequiredArg();
        // option is expected to specify paths to load shared libraries
        parser.accepts("l", Log.format("help.l")).withRequiredArg();
        parser.accepts("log", Log.format("help.log")).withRequiredArg();
        parser.accepts("package-map", Log.format("help.package_map")).withRequiredArg();
        parser.accepts("missing-symbols", Log.format("help.missing_symbols")).withRequiredArg();
        parser.accepts("no-locations", Log.format("help.no.locations"));
        parser.accepts("o", Log.format("help.o")).withRequiredArg();
        parser.accepts("record-library-path", Log.format("help.record_library_path"));
        parser.accepts("static-forwarder", Log.format("help.static_forwarder")).
                withRequiredArg().ofType(boolean.class);
        parser.acceptsAll(List.of("t", "target-package"), Log.format("help.t")).withRequiredArg();
        parser.acceptsAll(List.of("?", "h", "help"), Log.format("help.h")).forHelp();
        parser.nonOptions(Log.format("help.non.option"));

        OptionSet optionSet;
        try {
            optionSet = parser.parse(args);
        } catch (OptionException oe) {
            printHelp(parser);
            throw new FatalError(OPTION_ERROR, oe);
        }

        if (args.length == 0 || optionSet.has("h")) {
            printHelp(parser);
            if(args.length == 0)
                throw new FatalError(OPTION_ERROR);
            return;
        }

        if (optionSet.nonOptionArguments().isEmpty()) {
            throw new FatalError(OPTION_ERROR, Log.format("err.no.input.files"));
        }

        Log log;
        if (optionSet.has("log")) {
            log = Log.of(out, err, Level.parse((String) optionSet.valueOf("log")));
        } else {
            log = Log.of(out, err, Level.WARNING);
        }

        Options options = createOptions(optionSet);

        List<Path> sources = new ArrayList<>();
        for (Object header : optionSet.nonOptionArguments()) {
            Path p = Paths.get((String)header);
            if (!Files.isReadable(p)) {
                throw new FatalError(INPUT_ERROR, Log.format("cannot.read.header.file", header));
            }
            sources.add(p.normalize().toAbsolutePath());
        }
        sources = Collections.unmodifiableList(sources);

        Context ctx = new Context(sources, options, log);

        Writer writer;
        try {
            writer = new JextractTool(ctx).processHeaders();
        } catch (RuntimeException re) {
            throw new FatalError(RUNTIME_ERROR, re);
        }

        if (optionSet.has("dry-run")) {
            return;
        }

        if (writer.isEmpty()) {
            err.println(Log.format("warn.no.output"));
            return;
        }

        boolean hasOutput = false;
        if (optionSet.has("d")) {
            hasOutput = true;
            Path dest = Paths.get((String) optionSet.valueOf("d"));
            dest = dest.toAbsolutePath();
            try {
                if (!Files.exists(dest)) {
                    Files.createDirectories(dest);
                }
                if (!Files.isDirectory(dest)) {
                    throw new FatalError(OUTPUT_ERROR, Log.format("not.a.directory", dest));
                }
                writer.writeClassFiles(dest, args);
            } catch (IOException ex) {
                throw new FatalError(OPTION_ERROR, Log.format("cannot.write.class.file", dest, ex), ex);
            }
        }

        String outputName;
        if (optionSet.has("o")) {
            outputName = (String) optionSet.valueOf("o");
        } else if (hasOutput) {
            return;
        } else {
            outputName = Paths.get((String) optionSet.nonOptionArguments().get(0)).getFileName() + ".jar";
        }

        boolean isJMod = outputName.endsWith("jmod");
        try {
            if (isJMod) {
                new JModWriter(ctx, writer).writeJModFile(Paths.get(outputName), args);
            } else {
                new JarWriter(ctx, writer).writeJarFile(Paths.get(outputName), args);
            }
        } catch (IOException ex) {
            throw new FatalError(OUTPUT_ERROR,
                    Log.format(isJMod ? "cannot.write.jmod.file" : "cannot.write.jar.file", outputName, ex), ex);
        }

        return;
    }

    public static void main(String... args) {
        Main instance = new Main(Log.defaultOut(), Log.defaultErr());
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

            Main instance = new Main(out, err);
            return instance.run(args);
        }
    }

    private static class FatalError extends Error {
        private static final long serialVersionUID = 0L;

        public final int errorCode;

        public FatalError(int errorCode) {
            this.errorCode = errorCode;
        }

        public FatalError(int errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }

        public FatalError(int errorCode, Throwable cause) {
            super(cause);
            this.errorCode = errorCode;
        }

        public FatalError(int errorCode, String message, Throwable cause) {
            super(message, cause);
            this.errorCode = errorCode;
        }

    }

}
