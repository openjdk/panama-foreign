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

import jdk.internal.nicl.abi.SystemABI;
import jdk.internal.nicl.abi.sysv.x64.SysVx64ABI;

public abstract class Platform {
    private final Value.Endianness endianness;
    private final LibraryLoader loader;
    private final SystemABI abi;

    protected Platform(Value.Endianness endianness, LibraryLoader loader) {
        this(endianness, loader, new SysVx64ABI());
    }

    protected Platform(Value.Endianness endianness, LibraryLoader loader, SystemABI abi) {
        this.endianness = endianness;
        this.loader = loader;
        this.abi = abi;
    }

    public Value.Endianness defaultEndianness() { return endianness; }
    public SystemABI getABI() { return abi; }

    abstract String mapLibraryName(String name);

    public Library defaultLibrary() {
        return loader.getDefaultLibrary();
    }

    public Library loadLibrary(String name, boolean isAbsolute) {
        try {
            return loader.load(isAbsolute ? name : mapLibraryName(name), isAbsolute);
        } catch (UnsatisfiedLinkError ule) {
            throw new UnsatisfiedLinkError(name);
        }
    }

    public static Platform getInstance() {
        return Host.getInstance();
    }
}
