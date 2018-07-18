/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.spi.ToolProvider;

public class JtregJextract {
    private static final ToolProvider JEXTRACT = ToolProvider.findFirst("jextract")
            .orElseThrow(() ->
                    new RuntimeException("jextract tool not found")
            );

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
        ArrayList<String> jextrOpts = new ArrayList<>();

        jextrOpts.clear();
        jextrOpts.add("-I");
        jextrOpts.add(inputDir.toAbsolutePath().toString());
        jextrOpts.add("-d");
        jextrOpts.add( outputDir.toAbsolutePath().toString());

        int i = 0;
        while (i < args.length) {
            String opt = args[i++];

            if (opt.equals("--")) {
                break;
            }

            if (opt.equals("-d")) {
                i++;
                continue;
            }

            jextrOpts.add(opt);
        }

        while (i < args.length) {
            jextrOpts.add(getInputFilePath(args[i++]).toString());
        }

        return jextrOpts.toArray(String[]::new);
    }

    protected int jextract(String... options) {
        int result = JEXTRACT.run(System.out, System.err, processArgs(options));
        if (result != 0) {
            throw new RuntimeException(JEXTRACT.name() + " returns non-zero value");
        }
        return result;
    }

    private Path getInputFilePath(String filename) {
        return inputDir.resolve(filename).toAbsolutePath();
    }

    public static int main(String[] args) {
        JtregJextract jj =  new JtregJextract();
        return jj.jextract(args);
    }
}
