/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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

package jdk.incubator.jextract.tool;

import jdk.incubator.jextract.Declaration;
import jdk.incubator.jextract.JextractTask;
import jdk.internal.joptsimple.OptionException;
import jdk.internal.joptsimple.OptionParser;
import jdk.internal.joptsimple.OptionSet;
import jdk.internal.joptsimple.util.KeyValuePair;

import javax.tools.JavaFileObject;
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
import java.util.spi.ToolProvider;

/**
 * Simple extraction tool which generates a minimal Java API. Such an API consists mainly of static methods,
 * where for each native function a static method is added which calls the underlying native method handles.
 * Similarly, for struct fields and global variables, static accessors (getter and setter) are generated
 * on top of the underlying memory access var handles. For each struct, a static layout field is generated.
 */
public class Main {
    private static final String MESSAGES_RESOURCE = "jdk.incubator.jextract.tool.resources.Messages";

    private static final ResourceBundle MESSAGES_BUNDLE;
    static {
        MESSAGES_BUNDLE = ResourceBundle.getBundle(MESSAGES_RESOURCE, Locale.getDefault());
    }

    public static final boolean DEBUG = Boolean.getBoolean("jextract.debug");

    // error codes
    private static final int SUCCESS       = 0;
    private static final int OPTION_ERROR  = 1;
    private static final int INPUT_ERROR   = 2;
    private static final int OUTPUT_ERROR  = 3;
    private static final int RUNTIME_ERROR = 4;

    private final PrintWriter out;
    private final PrintWriter err;

    private static String format(String msgId, Object... args) {
        return new MessageFormat(MESSAGES_BUNDLE.getString(msgId)).format(args);
    }

    private Main(PrintWriter out, PrintWriter err) {
        this.out = out;
        this.err = err;
    }

    private int printHelp(OptionParser parser, int exitCode) {
        try {
            parser.printHelpOn(err);
        } catch (IOException ignored) {}
        return exitCode;
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Expected a header file");
            return;
        }

        Main m = new Main(new PrintWriter(System.out, true), new PrintWriter(System.err, true));
        System.exit(m.run(args));
    }

    private int run(String[] args) {
        OptionParser parser = new OptionParser(false);
        parser.accepts("C", format("help.C")).withRequiredArg();
        parser.accepts("I", format("help.I")).withRequiredArg();
        parser.accepts("d", format("help.d")).withRequiredArg();
        parser.accepts("filter", format("help.filter")).withRequiredArg();
        parser.accepts("l", format("help.l")).withRequiredArg();
        parser.accepts("source", format("help.source"));
        parser.acceptsAll(List.of("t", "target-package"), format("help.t")).withRequiredArg();
        parser.acceptsAll(List.of("?", "h", "help"), format("help.h")).forHelp();
        parser.nonOptions(format("help.non.option"));

        OptionSet optionSet;
        try {
            optionSet = parser.parse(args);
        } catch (OptionException oe) {
            return printHelp(parser, OPTION_ERROR);
        }

        if (optionSet.has("h")) {
            return printHelp(parser, SUCCESS);
        }

        if (optionSet.nonOptionArguments().size() != 1) {
            return printHelp(parser, OPTION_ERROR);
        }

        Options.Builder builder = Options.builder();
        if (optionSet.has("I")) {
            optionSet.valuesOf("I").forEach(p -> builder.addClangArg("-I" + p));
        }

        Path builtinInc = Paths.get(System.getProperty("java.home"), "conf", "jextract");
        builder.addClangArg("-I" + builtinInc);

        if (optionSet.has("C")) {
            optionSet.valuesOf("C").forEach(p -> builder.addClangArg((String) p));
        }

        if (optionSet.has("filter")) {
            optionSet.valuesOf("filter").forEach(p -> builder.addFilter((String) p));
        }

        if (optionSet.has("d")) {
            builder.setOutputDir(optionSet.valueOf("d").toString());
        }

        if (optionSet.has("source")) {
            builder.setGenerateSource();
        }

        boolean librariesSpecified = optionSet.has("l");
        if (librariesSpecified) {
            for (Object arg : optionSet.valuesOf("l")) {
                String lib = (String)arg;
                if (lib.indexOf(File.separatorChar) != -1) {
                    err.println(format("l.name.should.not.be.path", lib));
                    return OPTION_ERROR;
                }
                builder.addLibraryName(lib);
            }
        }

        String targetPackage = optionSet.has("t") ? (String) optionSet.valueOf("t") : "";
        builder.setTargetPackage(targetPackage);

        Options options = builder.build();

        Path header = Paths.get(optionSet.nonOptionArguments().get(0).toString());
        if (!Files.isReadable(header)) {
            err.println(format("cannot.read.header.file", header));
            return INPUT_ERROR;
        }

        //parse
        JextractTask jextractTask = JextractTask.newTask(!options.source, header);
        Declaration.Scoped toplevel = jextractTask.parse(options.clangArgs.toArray(new String[0]));

        //filter
        if (!options.filters.isEmpty()) {
            toplevel = Filter.filter(toplevel, options.filters.toArray(new String[0]));
        }

        if (Main.DEBUG) {
            System.out.println(toplevel);
        }

        Path output = Path.of(options.outputDir);
        //generate
        try {
            JavaFileObject[] files = HandleSourceFactory.generateWrapped(
                toplevel,
                header.getFileName().toString().replace(".h", "_h"),
                options.targetPackage,
                options.libraryNames);
            jextractTask.write(output, files);
        } catch (RuntimeException re) {
            err.println(re);
            if (Main.DEBUG) {
                re.printStackTrace(err);
            }
            return RUNTIME_ERROR;
        }
        return SUCCESS;
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
}
