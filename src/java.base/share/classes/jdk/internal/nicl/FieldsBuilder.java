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
package jdk.internal.nicl;

import jdk.internal.misc.Unsafe;
import jdk.internal.org.objectweb.asm.ClassWriter;

import java.lang.reflect.Field;
import java.util.ArrayList;

class FieldsBuilder {
    private static final Unsafe U = Unsafe.getUnsafe();

    private int id;
    private final ArrayList<FieldGenerator> generators = new ArrayList<>();

    FieldsBuilder() {
    }

    public void add(FieldGenerator generator) {
        generators.add(generator);
    }

    public int generateUniqueId() {
        return id++;
    }

    /**
     * Generate fields
     */
    public void generateFields(ClassWriter cw) {
        generators
                .stream()
                .forEach(g -> g.generate(cw));
    }

    private void initStaticField(Class<?> c, FieldGenerator fg) throws NoSuchFieldException {
        Field f = c.getDeclaredField(fg.getName());
        long offset = U.staticFieldOffset(f);
        U.putObject(c, offset, fg.getInitValue());
    }

    public void initStaticFields(Class<?> c) {
        generators
                .stream()
                .filter(FieldGenerator::isStatic)
                .forEach(fg -> {
                    try {
                        initStaticField(c, fg);
                    } catch (NoSuchFieldException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
