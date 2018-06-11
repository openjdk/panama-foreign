/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
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

import jdk.internal.clang.Cursor;

import java.util.Map;

/**
 * Interface of factory that takes libclang cursors and generate java codes
 */
public abstract class CodeFactory {
    /**
     * Generate code for the declaring cursor
     * @param type
     * @param cursor
     * @return The JType for the generated entity
     */
    protected abstract CodeFactory addType(JType type, Cursor cursor);

    protected abstract void produce();

    /**
     * Collect the classes generated from this CodeFactory. The Map key is the
     * full qualified class name, and the value is bytecode for the class.
     * @return Map contains class name and bytecode.
     */
    protected abstract Map<String, byte[]> collect();
}
