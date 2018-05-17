/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.ArrayList;

class BinderClassWriter extends ClassWriter {

    private final ArrayList<ConstantPoolPatch> cpPatches = new ArrayList<>();
    private int curUniquePatchIndex = 0;

    BinderClassWriter() {
        super(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    }

    public String makeConstantPoolPatch(Object o) {
        int myUniqueIndex = curUniquePatchIndex++;
        String cpPlaceholder = "CONSTANT_PLACEHOLDER_" + myUniqueIndex;
        int index = newConst(cpPlaceholder);
        cpPatches.add(new ConstantPoolPatch(index, cpPlaceholder, o));
        return cpPlaceholder;
    }

    public Object[] resolvePatches(byte[] classFile) {
        if (cpPatches.isEmpty()) {
            return null;
        }

        int size = ((classFile[8] & 0xFF) << 8) | (classFile[9] & 0xFF);

        Object[] patches = new Object[size];
        for (ConstantPoolPatch p : cpPatches) {
            if (p.index >= size) {
                throw new InternalError("Failed to resolve constant pool patch entries");
            }
            patches[p.index] = p.value;
        }

        return patches;
    }

    static class ConstantPoolPatch {
        final int index;
        final String placeholder;
        final Object value;

        ConstantPoolPatch(int index, String placeholder, Object value) {
            this.index = index;
            this.placeholder = placeholder;
            this.value = value;
        }

        @Override
        public String toString() {
            return "CpPatch/index="+index+",placeholder="+placeholder+",value="+value;
        }
    }
}
