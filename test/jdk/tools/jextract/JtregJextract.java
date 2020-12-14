/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.io.File;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JtregJextract {
    private final Path inputDir;
    private final Path outputDir;

    JtregJextract() {
        this(null, null);
    }

    JtregJextract(Path input, Path output) {
        inputDir = (input != null) ? input :
                Paths.get(System.getProperty("test.src", "."));
        outputDir = (output != null) ? output :
                Paths.get(System.getProperty("test.classes", "."));

    }

    protected String[] processArgs(String... args) {
        Pattern sysPropPattern = Pattern.compile("'?\\$\\((.*)\\)'?");
        ArrayList<String> jextrOpts = new ArrayList<>();

        jextrOpts.clear();
        jextrOpts.add("-C-nostdinc");
        jextrOpts.add("-I");
        jextrOpts.add(inputDir.toAbsolutePath().toString());
        jextrOpts.add("-d");
        jextrOpts.add(outputDir.toAbsolutePath().toString());

        int i = 0;
        while (i < args.length) {
            String opt = args[i++];
            if ("--".equals(opt)) {
                break;
            }

            if ("-libpath".equals(opt)) {
                String lib = args[i];
                jextrOpts.add("-l");
                String libpath = System.getProperty("test.nativepath") + File.separator + System.mapLibraryName(lib);
                System.err.println("jextract driver libpath passed: " + libpath);
                jextrOpts.add(libpath);
                i++;
                continue;
            }

            if ("-d".equals(opt)) {
                i++;
                continue;
            }
            // Pattern $(system.property.name) is replaced with the
            // value of the System property of that name.
            Matcher m = sysPropPattern.matcher(opt);
            if (m.matches()) {
                jextrOpts.add(System.getProperty(m.group(1)));
            } else {
                jextrOpts.add(opt);
            }
        }

        while (i < args.length) {
            jextrOpts.add(getInputFilePath(args[i++]).toString());
        }

        return jextrOpts.toArray(String[]::new);
    }

    protected int jextract(String... options) {
        String[] args = processArgs(options);
        String[] commands = new String[args.length + 1];
        commands[0] = Paths.get(System.getProperty("test.jdk"), "bin", "jextract").toString();
        System.arraycopy(args, 0, commands, 1, args.length);
        try {
            Process proc = new ProcessBuilder(commands).inheritIO().start();
            int result = proc.waitFor();
            if (result != 0) {
                throw new RuntimeException("jextract returns non-zero value");
            }
            return result;
        } catch (IOException ioExp) {
            throw new UncheckedIOException(ioExp);
        } catch (InterruptedException intExp) {
            throw new RuntimeException(intExp);
        }
    }

    private Path getInputFilePath(String filename) {
        return inputDir.resolve(filename).toAbsolutePath();
    }

    public static int main(String[] args) {
        JtregJextract jj =  new JtregJextract();
        return jj.jextract(args);
    }
}
