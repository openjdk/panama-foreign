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

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.sun.tools.jextract.tree.StructTree;
import com.sun.tools.jextract.tree.Tree;

/**
 * This class represent a native code header file
 */
public final class HeaderFile {
    private final Context ctx;
    final Path path;
    final String pkgName;
    final String clsName;
    private final TypeDictionary dict;
    // The top header file cause this file to be parsed
    private HeaderFile main;
    private AsmCodeFactory cf;
    List<String> libraries; // immutable
    List<String> libraryPaths; // immutable

    private final Logger logger = Logger.getLogger(getClass().getPackage().getName());

    HeaderFile(Context ctx, Path path, String pkgName, String clsName, HeaderFile main) {
        this.ctx = ctx;
        this.path = path;
        this.pkgName = pkgName;
        this.clsName = clsName;
        this.main = main == null ? this : main;
        this.dict = new TypeDictionary(ctx, this);
    }

    void useLibraries(List<String> libraries, List<String> libraryPaths) {
        this.libraries = Collections.unmodifiableList(libraries);
        this.libraryPaths = Collections.unmodifiableList(libraryPaths);
    }

    AsmCodeFactory getCodeFactory() {
        return cf;
    }

    TypeDictionary dictionary() {
        return dict;
    }

    /**
     * Call this function to enable code generation for this HeaderFile.
     * This function should only be called once to turn on code generation and before process any cursor.
     * @param cf The CodeFactory used to generate code
     */
    void useCodeFactory(AsmCodeFactory cf) {
        if (null != this.cf) {
            logger.config(() -> "CodeFactory had been initialized for " + path);
            // Diagnosis code
            if (Main.DEBUG) {
                new Throwable().printStackTrace(ctx.err);
            }
        } else {
            this.cf = cf;
        }
    }

    @Override
    public String toString() {
        return "HeaderFile(path=" + path + ")";
    }

    void processTree(Tree tree, HeaderFile main, boolean isBuiltIn) {
        if (tree.isDeclaration()) {
            tree.accept(new TypeEnter(dictionary()), null);
            JType jt = dictionary().lookup(tree.type());

            if (tree instanceof StructTree) {
                ((StructTree)tree).nestedTypes().forEach(nt -> processTree(nt, main, isBuiltIn));
            }

            // Only main file can define interface
            if (cf != null && this.main == main) {
                cf.addType(jt, tree);
            }
        } else if (tree.isPreprocessing() && !isBuiltIn) {
            if (cf != null) {
                tree.accept(cf, null);
            }
        }
    }
}
