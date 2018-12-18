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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class HeaderResolver {

    // The folder path mapping to package name
    private final Map<Path, String> pkgMap = new LinkedHashMap<>();
    // The header file parsed
    private final Map<Path, HeaderFile> headerMap = new LinkedHashMap<>();


    private final Logger logger = Logger.getLogger(getClass().getPackage().getName());

    public HeaderResolver(Context ctx) {
        usePackageForFolder(Main.getBuiltinHeadersDir(), "clang_support");
        ctx.sources.stream()
                .map(Path::getParent)
                .forEach(p -> usePackageForFolder(p, ctx.targetPackage));
        ctx.pkgMappings.forEach(this::usePackageForFolder);
    }

    private void usePackageForFolder(Path folder, String pkg) {
        folder = folder.normalize().toAbsolutePath();
        String existing = pkgMap.putIfAbsent(folder, pkg);
        final String finalFolder = (null == folder) ? "all folders not configured" : folder.toString();
        if (existing == null) {
            logger.config(() -> "Package " + pkg + " is selected for " + finalFolder);
        } else {
            String pkgName = pkg.isEmpty() ? "<default-package>" : pkg;
            logger.warning(() -> "Package " + existing + " had been selected for " + finalFolder + ", request to use " + pkgName + " is ignored.");
        }
    }

    // start of header file resolution logic

    static class HeaderPath {
        final String pkg;
        final String cls;

        HeaderPath(String pkg, String cls) {
            this.pkg = pkg;
            this.cls = cls;
        }
    }

    /**
     * Determine package and interface name given a path. If the path is
     * a folder, then only package name is determined. The package name is
     * determined with the longest path matching the setup. If the path is not
     * setup for any package, the default package name is returned.
     *
     * @param origin The source path
     * @return The HeaderPath
     * @see Context::usePackageForFolder(Path, String)
     */
    private HeaderPath resolveHeaderPath(Path origin) {
        // normalize to absolute path
        origin = origin.normalize().toAbsolutePath();
        if (Files.isDirectory(origin)) {
            throw new IllegalStateException("Not an header file: " + origin);
        }
        String filename = origin.getFileName().toString();
        origin = origin.getParent();
        Path path = origin;

        // search the map for a hit with longest path
        while (path != null && !pkgMap.containsKey(path)) {
            path = path.getParent();
        }

        String pkg;
        if (path != null) {
            pkg = pkgMap.get(path);
            if (path.getNameCount() != origin.getNameCount()) {
                String sep = pkg.isEmpty() ? "" : "/";
                for (int i = path.getNameCount() ; i < origin.getNameCount() ; i++) {
                    pkg += sep + Utils.toJavaIdentifier(origin.getName(i).toString());
                    sep = "/";
                }
                usePackageForFolder(origin, pkg);
            }
        } else {
            //infer a package name from path
            List<String> parts = new ArrayList<>();
            for (Path p : origin) {
                parts.add(Utils.toJavaIdentifier(p.toString()));
            }
            pkg = String.join("/", parts);
            usePackageForFolder(origin, pkg);
        }

        int ext = filename.lastIndexOf('.');
        String cls = (ext != -1) ?
                filename.substring(0, ext) :
                filename;
        return new HeaderPath(pkg, Utils.toClassName(cls));
    }

    private HeaderFile getHeaderFile(Path header) {
        if (!Files.isRegularFile(header)) {
            logger.warning(() -> "Not a regular file: " + header.toString());
            throw new IllegalArgumentException(header.toString());
        }

        final HeaderPath hp = resolveHeaderPath(header);
        return new HeaderFile(this, header, hp.pkg, hp.cls);
    }

    public HeaderFile headerFor(Path path) {
        return headerMap.computeIfAbsent(path.normalize().toAbsolutePath(), this::getHeaderFile);
    }
}
