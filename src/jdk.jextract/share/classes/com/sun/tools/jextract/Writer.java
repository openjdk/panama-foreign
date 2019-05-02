/*
 *  Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

public final class Writer {

    private final Context ctx;
    private final Map<String, ? extends CharSequence> sources;
    private Map<String, byte[]> results;

    Writer(Context ctx, Map<String, ? extends CharSequence> sources) {
        this.ctx = ctx;
        this.sources = sources;
    }

    private void ensureSourcesCompiled() {
        if (results == null) {
            results = !sources.isEmpty() ? InMemoryJavaCompiler.compile(sources) : Map.of();
        }
    }

    static final String JEXTRACT_MANIFEST = "META-INF/jextract.properties";

    public boolean isEmpty() {
        return sources.isEmpty();
    }

    @SuppressWarnings("deprecation")
    byte[] getJextractProperties(String[] args) {
        Properties props = new Properties();
        props.setProperty("os.name", System.getProperty("os.name"));
        props.setProperty("os.version", System.getProperty("os.version"));
        props.setProperty("os.arch", System.getProperty("os.arch"));
        props.setProperty("jextract.args", Arrays.toString(args));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        props.save(baos, "jextract meta data");
        return baos.toByteArray();
    }

    void writeClassFiles(Path destDir, String[] args) throws IOException {
        ensureSourcesCompiled();
        destDir = createOutputDir(destDir);
        for (var entry : results().entrySet()) {
            String cls = entry.getKey();
            byte[] bytes = entry.getValue();

            String path = cls.replace('.', File.separatorChar) + ".class";
            ctx.log.print(Level.FINE, () -> "Writing " + path);
            Path fullPath = destDir.resolve(path).normalize();
            Files.createDirectories(fullPath.getParent());
            Files.write(fullPath, bytes);
        }

        Path propsPath = destDir.resolve(JEXTRACT_MANIFEST).normalize();
        Files.createDirectories(propsPath.getParent());
        Files.write(propsPath, getJextractProperties(args));
    }

    void writeSourceFiles(Path destDir, String[] args) throws IOException {
        destDir = createOutputDir(destDir);
        for (var entry : sources.entrySet()) {
            String srcPath = entry.getKey().replace('.', File.separatorChar) + ".java";
            Path fullPath = destDir.resolve(srcPath).normalize();
            Files.createDirectories(fullPath.getParent());
            Files.write(fullPath, List.of(entry.getValue()));
        }
    }

    void writeJMod(Path destDir, String[] args) throws IOException {
        ensureSourcesCompiled();
        new JModWriter(ctx, this).writeJModFile(destDir, args);
    }

    void writeJar(Path destDir, String[] args) throws IOException {
        ensureSourcesCompiled();
        new JarWriter(ctx, this).writeJarFile(destDir, args);
    }

    private Path createOutputDir(Path dest) throws IOException {
        dest = dest.toAbsolutePath();
        if (!Files.exists(dest)) {
            Files.createDirectories(dest);
        }
        if (!Files.isDirectory(dest)) {
            throw new IOException(Log.format("not.a.directory", dest));
        }
        return dest;
    }

    //These methods are used for testing (see Runner.java)

    public Map<String, byte[]> results() {
        ensureSourcesCompiled();
        return results;
    }
}
