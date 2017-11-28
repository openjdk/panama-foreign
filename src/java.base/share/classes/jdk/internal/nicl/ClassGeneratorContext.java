/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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

import jdk.internal.org.objectweb.asm.ClassWriter;

import java.util.HashMap;

class ClassGeneratorContext {
    private final ClassWriter cw;
    private final String implClassName;

    private final FieldsBuilder fields = new FieldsBuilder();

    private final HashMap<Object, ConstantPoolPatch> cpPatches = new HashMap<>();
    private int curUniquePatchIndex = 0;

    ClassGeneratorContext(ClassWriter cw, String implClassName) {
        this.cw = cw;
        this.implClassName = implClassName;
    }

    public ClassWriter getClassWriter() {
        return cw;
    }

    public String getClassName() {
        return implClassName;
    }

    public FieldsBuilder getFieldsBuilder() {
        return fields;
    }

    public String makeConstantPoolPatch(Object o) {
        int myUniqueIndex = curUniquePatchIndex++;

        String cpPlaceholder = "CONSTANT_PLACEHOLDER_" + myUniqueIndex;

        int index = cw.newConst(cpPlaceholder);

        cpPatches.put(cpPlaceholder, new ConstantPoolPatch(index, cpPlaceholder, o));

        return cpPlaceholder;
    }

    public Object[] resolvePatches(byte[] classFile) {
        if (cpPatches.isEmpty()) {
            return null;
        }

        int size = ((classFile[8] & 0xFF) << 8) | (classFile[9] & 0xFF);

        Object[] patches = new Object[size];
        for (ConstantPoolPatch p : cpPatches.values()) {
            if (p.getIndex() >= size) {
                throw new InternalError("Failed to resolve constant pool patch entries");
            }

            patches[p.getIndex()] = p.getValue();
        }

        return patches;
    }
}
