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
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarOutputStream;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

import static java.nio.file.StandardOpenOption.*;

public class Writer {

    private final Logger logger = Logger.getLogger(getClass().getPackage().getName());
    private final Map<String, byte[]> results;

    Writer(Map<String, byte[]> results) {
        this.results = results;
    }

    private static final String JEXTRACT_MANIFEST = "META-INF" + File.separatorChar + "jextract.properties";

    public boolean isEmpty() {
        return results.isEmpty();
    }

    @SuppressWarnings("deprecation")
    private byte[] getJextractProperties(String[] args) {
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
        try {
            results.forEach((cls, bytes) -> {
                try {
                    String path = cls.replace('.', File.separatorChar) + ".class";
                    logger.fine(() -> "Writing " + path);
                    Path fullPath = destDir.resolve(path).normalize();
                    Files.createDirectories(fullPath.getParent());
                    try (OutputStream fos = Files.newOutputStream(fullPath)) {
                        fos.write(bytes);
                        fos.flush();
                    }
                } catch (IOException ioe) {
                    throw new UncheckedIOException(ioe);
                }
            });

            Path propsPath = destDir.resolve(JEXTRACT_MANIFEST).normalize();
            Files.createDirectories(propsPath.getParent());
            try (OutputStream fos = Files.newOutputStream(propsPath)) {
                fos.write(getJextractProperties(args));
                fos.flush();
            }
        } catch (UncheckedIOException uioe) {
            throw uioe.getCause();
        }
    }

    public void writeJarFile(Path jarpath, String[] args) throws IOException {
        logger.info(() -> "Collecting jar file " + jarpath);
        try (OutputStream os = Files.newOutputStream(jarpath, CREATE, TRUNCATE_EXISTING, WRITE);
                JarOutputStream jar = new JarOutputStream(os)) {
            writeJarFile(jar, args);
        }
    }

    //These methods are used for testing (see Runner.java)

    public Map<String, byte[]> results() {
        return results;
    }

    public void writeJarFile(JarOutputStream jar, String[] args) {
        results.forEach((cls, bytes) -> {
                try {
                    String path = cls.replace('.', File.separatorChar) + ".class";
                    logger.fine(() -> "Add " + path);
                    jar.putNextEntry(new ZipEntry(path));
                    jar.write(bytes);
                    jar.closeEntry();
                } catch (IOException ioe) {
                    throw new UncheckedIOException(ioe);
                }
            });

            try {
                jar.putNextEntry(new ZipEntry(JEXTRACT_MANIFEST));
                jar.write(getJextractProperties(args));
                jar.closeEntry();
            } catch (IOException ioe) {
                throw new UncheckedIOException(ioe);
            }
    }
}
