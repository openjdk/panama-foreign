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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/*
 * @test
 * @modules jdk.jextract
 * @run main TestJextractFFI
 */
public class TestJextractFFI {
    final String jextract_cmd;
    final String javac_cmd;
    final Path jclang_src_path;
    final Path clang_header_path;
    final Path clang_lib_path;

    final static String CLANG_JAR = "clang.jar";
    final static String CLANG_JNI_PATH = "clang_jni";
    final static String CLANG_FFI_PATH = "clang_ffi";
    final static String JCLANG_PATH = "jclang";

    TestJextractFFI(Path clang_header_path, Path clang_lib_path, Path jclang_src) {
        this.clang_header_path = clang_header_path;
        this.clang_lib_path = clang_lib_path;
        this.jclang_src_path = jclang_src;

        final Path jdk_path = Paths.get(System.getProperty("test.jdk"))
                .resolve("bin").toAbsolutePath();
        jextract_cmd = jdk_path.resolve("jextract").toString();
        javac_cmd = jdk_path.resolve("javac").toString();
    }

    private Path getClassPath(String component) {
        return Paths.get(System.getProperty("test.classes")).resolve(component);
    }

    File launch(List<String> command) throws IOException, InterruptedException {
        File err = File.createTempFile(
                Paths.get(command.get(0)).getFileName().toString(), null);
        err.deleteOnExit();
        Process proc = new ProcessBuilder()
                .directory(new File(System.getProperty("test.classes")))
                .command(command)
                .redirectError(err)
                .start();
        String cmdline = proc.info().commandLine().orElse(String.join(" ", command));
        proc.waitFor(3, TimeUnit.MINUTES);
        Files.lines(err.toPath()).forEachOrdered(System.err::println);
        if (proc.exitValue() != 0) {
            throw new Error("Command '" + cmdline +
                "' exit with non-zero value: " + proc.exitValue());
        }
        return err;
    }

    public void jextractJNI() throws IOException, InterruptedException {
        List<String> command = List.of(jextract_cmd,
                "-I", clang_header_path.toString(),
                "-t", "clang",
                "-o", CLANG_JAR,
                "-d", CLANG_JNI_PATH,
                clang_header_path
                        .resolve("clang-c").resolve("Index.h").toString());
        launch(command);
    }

    public void buildJclang() throws IOException, InterruptedException {
        List<String> command = new ArrayList<>(List.of(
                javac_cmd,
                "--module-path", CLANG_JAR,
                "-d", JCLANG_PATH));
        Files.walk(jclang_src_path)
                .map(path -> path.toString())
                .filter(filename -> filename.endsWith(".java"))
                .forEach(command::add);
        launch(command);
    }

    public void jextractFFI() throws IOException, InterruptedException {
        List<String> command = List.of(jextract_cmd,
                "-I", clang_header_path.toString(),
                "-t", "clang",
                "-d", CLANG_FFI_PATH,
                "-J-Djextract.debug=true",
                "-J-Dlibclang.debug=true",
                "-J-Djava.library.path=" + clang_lib_path.toString(),
                "-J--module-path", "-J" + CLANG_JAR,
                "-J--upgrade-module-path", "-J" + JCLANG_PATH,
                clang_header_path
                        .resolve("clang-c").resolve("Index.h").toString());
        File err = launch(command);
        if (! Files.lines(err.toPath())
                .anyMatch(s -> s.contains("FFI"))) {
            throw new Error("Not running jextract on FFI stack");
        }
    }

    public void compareResult() throws IOException {
        // We cannot do jar file byte-to-byte comparison because
        // jar file format include timestamp regarding modification,
        // thus such comparison will fail.
        Path[] jni = Files.walk(getClassPath(CLANG_JNI_PATH))
                .sorted().toArray(Path[]::new);
        Path[] ffi = Files.walk(getClassPath(CLANG_FFI_PATH))
                .sorted().toArray(Path[]::new);
        if (jni.length != ffi.length) {
            throw new Error("Number of classes mismatch.");
        }

        // Skip the first element which is top folder
        for (int i = 1; i < jni.length; i++) {
            final Path pathJNI = jni[i];
            final Path pathFFI = ffi[i];
            // Basename should be the same
            if (! pathJNI.getFileName().equals(pathFFI.getFileName())) {
                throw new Error("Different filename:" + pathJNI.getFileName()
                        + " vs " + pathFFI.getFileName());
            }
            // Direcoty for package
            if (Files.isDirectory(pathJNI)) {
                if (Files.isDirectory(pathFFI)) {
                    continue;
                }
                throw new Error("Mismatch file type: " + pathJNI.getFileName()
                        + " vs " + pathFFI.getFileName());
            }
            // ignore non-class files
            if (!pathJNI.getFileName().endsWith("class")) {
                continue;
            }
            // Classfile
            if (! Arrays.equals(
                    Files.readAllBytes(pathJNI),
                    Files.readAllBytes(pathFFI))) {
                throw new Error("Mismatch class files: " + pathJNI.toString()
                        + " vs " + pathFFI.toString());
            }
        }
    }

    public void run() throws IOException, InterruptedException {
        jextractJNI();
        buildJclang();
        jextractFFI();
        compareResult();
    }

    public static int main(String... args) throws IOException, InterruptedException {
        final Path srcPath = Paths.get(System.getProperty("test.src"));
        final String clangInclude = System.getProperty("clang.include.path");
        final String clangLib = System.getProperty("clang.lib.path");

        if (clangInclude != null && clangLib != null) {
            TestJextractFFI test = new TestJextractFFI(
                    Paths.get(clangInclude).toAbsolutePath(),
                    Paths.get(clangLib).toAbsolutePath(),
                    srcPath.resolve("src").toAbsolutePath()
            );

            test.run();
        } else {
            // FIXME: we should try to figure out clang paths automatically.
            System.err.println("WARNING: clang paths not found, vacuously passing");
        }
        return 0;
    }
}
