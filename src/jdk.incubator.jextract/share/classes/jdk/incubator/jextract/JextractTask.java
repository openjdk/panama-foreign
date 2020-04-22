/*
 *  Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.  Oracle designates this
 *  particular file as subject to the "Classpath" exception as provided
 *  by Oracle in the LICENSE file that accompanied this code.
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
 *   Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 *
 */

package jdk.incubator.jextract;

import jdk.internal.jextract.impl.JextractTaskImpl;

import javax.tools.JavaFileObject;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * This interface models a so called <em>extraction</em> task which aims at generating a suitable Java API from a foreign
 * library. A new extraction task is obtained using the {@link #newTask(boolean, Path...)} factory. Once an extraction
 * task has been obtained, the source files of the foreign library can be parsed into a toplevel foreign
 * declaration (see {@link Declaration}). After parsing, a client can define one or more processing steps
 * (typically by using type and declaration visitors, see {@link Type.Visitor} and {@link Declaration.Visitor}.
 * Once processing is complete, the client should generate a list of {@link JavaFileObject} which embody the result
 * of the API extraction process; such files can be written onto a given destination path using the extraction
 * task (see {@link #write(Path, JavaFileObject...)}).
 */
public interface JextractTask {

    /**
     * Creates a new extraction task with given input files.
     * @param compileSources true, if the task should result in the compilation of the generated {@link JavaFileObject}
     *                       whose kind is set to {@link JavaFileObject.Kind#SOURCE}.
     * @param inputFiles the input files of the extraction task.
     * @return a new extraction task with given input files.
     */
    static JextractTask newTask(boolean compileSources, Path... inputFiles) {
        return new JextractTaskImpl(compileSources, inputFiles);
    }

    /**
     * A constant parser is an helper object that is used to parse constant values in a foreign language,
     * and create a corresponding declaration constant, if possible.
     */
    interface ConstantParser {
        /**
         * Parses a constant at given position, with given name and list of tokens.
         * @param pos the constant position.
         * @param name the constant name.
         * @param tokens the constant tokens.
         * @return a constant declaration which embeds the parsed constant value, if possible.
         */
        Optional<Declaration.Constant> parseConstant(Position pos, String name, String[] tokens);
    }

    /**
     * Parse input files into a toplevel declaration with given options.
     * @param parserOptions options to be passed to the parser.
     * @return a toplevel declaration.
     */
    Declaration.Scoped parse(String... parserOptions);

    /**
     * Parse input files into a toplevel declaration with given constant parser and options.
     * @param constantParser the constant parser to evaluate constants.
     * @param parserOptions options to be passed to the parser.
     * @return a toplevel declaration.
     */
    Declaration.Scoped parse(ConstantParser constantParser, String... parserOptions);

    /**
     * Write resulting {@link JavaFileObject} instances into specified destination path.
     * @param dest the destination path.
     * @param files the {@link JavaFileObject} instances to be written.
     */
    void write(Path dest, JavaFileObject... files) throws UncheckedIOException;
}
