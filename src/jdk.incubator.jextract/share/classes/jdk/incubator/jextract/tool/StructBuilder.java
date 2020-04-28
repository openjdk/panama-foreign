/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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
package jdk.incubator.jextract.tool;

import jdk.incubator.foreign.MemoryLayout;

import java.lang.constant.DirectMethodHandleDesc;

/**
 * This class generates static utilities class for C structs, unions.
 */
class StructBuilder extends JavaSourceBuilder {
    StructBuilder(String className, String pkgName, ConstantHelper constantHelper) {
        super(className, pkgName, constantHelper);
    }

    @Override
    public void classEnd() {
        emitSizeof();
        emitAllocate();
        emitScopeAllocate();
        super.classEnd();
    }

    @Override
    public void addLayoutGetter(String javaName, MemoryLayout layout) {
        var desc = constantHelper.addLayout(javaName, layout);
        incrAlign();
        indent();
        sb.append(PUB_MODS + displayName(desc.invocationType().returnType()) + " $LAYOUT() {\n");
        incrAlign();
        indent();
        sb.append("return " + getCallString(desc) + ";\n");
        decrAlign();
        indent();
        sb.append("}\n");
        decrAlign();
    }

    private void emitSizeof() {
        incrAlign();
        indent();
        sb.append(PUB_MODS);
        sb.append("long sizeof() { return $LAYOUT().byteSize(); }\n");
        decrAlign();
    }

    private void emitAllocate() {
        incrAlign();
        indent();
        sb.append(PUB_MODS);
        sb.append("MemorySegment allocate() { return MemorySegment.allocateNative($LAYOUT()); }\n");
        decrAlign();
    }

    private void emitScopeAllocate() {
        incrAlign();
        indent();
        sb.append(PUB_MODS);
        sb.append("MemoryAddress allocate(NativeAllocationScope scope) { return scope.allocate($LAYOUT()); }\n");
        decrAlign();
    }
}
