/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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

package jdk.internal.nicl;

import java.nicl.Library;
import java.nicl.layout.Value;

public final class Host extends Platform {
    private final static String LIB_PREFIX = "lib";
    private final static String LIB_SUFFIX = ".so";
    private final static LibraryLoader loader = new LdLoader();
    private final static Host instance = new Host();

    private Host() {
        // FIXME: Need to get Endianness from CPU arch
        super(Value.Endianness.LITTLE_ENDIAN, loader);
    }

    @Override
    public String mapLibraryName(String name) {
        return LIB_PREFIX + name + LIB_SUFFIX;
    }

    public static Host getInstance() {
        return instance;
    }
}
