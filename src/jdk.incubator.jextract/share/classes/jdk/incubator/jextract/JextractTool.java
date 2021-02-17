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

package jdk.incubator.jextract;

import jdk.internal.jextract.impl.ClangException;
import jdk.internal.jextract.impl.CompilationFailedException;
import jdk.internal.jextract.impl.Filter;
import jdk.internal.jextract.impl.OutputFactory;
import jdk.internal.jextract.impl.Parser;
import jdk.internal.jextract.impl.Options;
import jdk.internal.jextract.impl.Writer;
import jdk.internal.joptsimple.OptionException;
import jdk.internal.joptsimple.OptionParser;
import jdk.internal.joptsimple.OptionSet;

import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.spi.ToolProvider;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Simple extraction tool which generates a minimal Java API. Such an API consists mainly of static methods,
 * where for each native function a static method is added which calls the underlying native method handles.
 * Similarly, for struct fields and global variables, static accessors (getter and setter) are generated
 * on top of the underlying memory access var handles. For each struct, a static layout field is generated.
 */
public final class JextractTool {
    private static final String MESSAGES_RESOURCE = "jdk.internal.jextract.impl.resources.Messages";

    private static final ResourceBundle MESSAGES_BUNDLE;
    static {
        MESSAGES_BUNDLE = ResourceBundle.getBundle(MESSAGES_RESOURCE, Locale.getDefault());
    }

    public static final boolean DEBUG = Boolean.getBoolean("jextract.debug");

    // error codes
    private static final int SUCCESS       = 0;
    private static final int OPTION_ERROR  = 1;
    private static final int INPUT_ERROR   = 2;
    private static final int CLANG_ERROR   = 3;
    private static final int RUNTIME_ERROR = 4;
    private static final int OUTPUT_ERROR  = 5;

    private final PrintWriter out;
    private final PrintWriter err;

    private static String format(String msgId, Object... args) {
        return new MessageFormat(MESSAGES_BUNDLE.getString(msgId)).format(args);
    }

    private JextractTool(PrintWriter out, PrintWriter err) {
        this.out = out;
        this.err = err;
    }

    private static Path generateTmpSource(List<Path> headers) {
        assert headers.size() > 1;
        try {
            Path tmpFile = Files.createTempFile("jextract", ".h");
            tmpFile.toFile().deleteOnExit();
            Files.write(tmpFile, headers.stream().
                    map(src -> "#include \"" + src + "\"").
                    collect(Collectors.toList()));
            return tmpFile;
        } catch (IOException ioExp) {
            throw new UncheckedIOException(ioExp);
        }
    }

    /**
     * Parse input files into a toplevel declaration with given options.
     * @param parserOptions options to be passed to the parser.
     * @return a toplevel declaration.
     */
    public static Declaration.Scoped parse(List<Path> headers, String... parserOptions) {
        Path source = headers.size() > 1? generateTmpSource(headers) : headers.iterator().next();
        return new Parser().parse(source, Stream.of(parserOptions).collect(Collectors.toList()));
    }

    public static Declaration.Scoped filter(Declaration.Scoped decl, String... includedNames) {
        return Filter.filter(decl, includedNames);
    }

    public static List<JavaFileObject> generate(Declaration.Scoped decl, String headerName,
                                                String targetPkg, List<String> libNames) {
        return List.of(OutputFactory.generateWrapped(decl, headerName, targetPkg, libNames));
    }

    /**
     * Write resulting {@link JavaFileObject} instances into specified destination path.
     * @param dest the destination path.
     * @param compileSources whether to compile .java sources or not
     * @param files the {@link JavaFileObject} instances to be written.
     */
    public static void write(Path dest, boolean compileSources, List<JavaFileObject> files) throws UncheckedIOException {
        try {
            new Writer(dest, files).writeAll(compileSources);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private int printHelp(OptionParser parser, int exitCode) {
        try {
            parser.printHelpOn(err);
        } catch (IOException ignored) {}
        return exitCode;
    }

    /**
     * Main entry point to run the JextractTool
     *
     * @param args command line options passed
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Expected a header file");
            return;
        }

        JextractTool m = new JextractTool(new PrintWriter(System.out, true), new PrintWriter(System.err, true));
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
                if (lib.indexOf(File.separatorChar) == -1) {
                    builder.addLibraryName(lib);
                } else {
                    Path libPath = Paths.get(lib);
                    if (libPath.isAbsolute() && Files.isRegularFile(libPath)) {
                        builder.addLibraryName(lib);
                    } else {
                        err.println(format("l.option.value.invalid", lib));
                        return OPTION_ERROR;
                    }
                }
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
        if (!(Files.isRegularFile(header))) {
            err.println(format("not.a.file", header));
            return INPUT_ERROR;
        }

        List<JavaFileObject> files = null;
        try {
            Declaration.Scoped toplevel = parse(List.of(header), options.clangArgs.toArray(new String[0]));

            //filter
            if (!options.filters.isEmpty()) {
                toplevel = filter(toplevel, options.filters.toArray(new String[0]));
            }

            if (JextractTool.DEBUG) {
                System.out.println(toplevel);
            }

            files = generate(
                toplevel, header.getFileName().toString(),
                options.targetPackage, options.libraryNames);
        } catch (ClangException ce) {
            err.println(ce.getMessage());
            if (JextractTool.DEBUG) {
                ce.printStackTrace(err);
            }
            return CLANG_ERROR;
        } catch (RuntimeException re) {
            err.println(re.getMessage());
            if (JextractTool.DEBUG) {
                re.printStackTrace(err);
            }
            return RUNTIME_ERROR;
        }

        try {
            Path output = Path.of(options.outputDir);
            write(output, !options.source, files);
        } catch (CompilationFailedException cfe) {
            err.println(cfe.getMessage());
            if (JextractTool.DEBUG) {
                cfe.printStackTrace(err);
            }
            return RUNTIME_ERROR;
        } catch (UncheckedIOException uioe) {
            err.println(uioe.getMessage());
            if (JextractTool.DEBUG) {
                uioe.printStackTrace(err);
            }
            return OUTPUT_ERROR;
        }

        return SUCCESS;
    }

    /**
     * ToolProvider implementation for jextract tool.
     */
    public static class JextractToolProvider implements ToolProvider {
        public JextractToolProvider() {}

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

            JextractTool instance = new JextractTool(out, err);
            return instance.run(args);
        }
    }
}
