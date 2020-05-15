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

import jdk.incubator.foreign.MemoryAddress;
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
    protected String getClassModifiers() {
        return PUB_MODS;
    }

    @Override
    protected void addPackagePrefix() {
        // nested class. containing class has necessary package declaration
    }

    @Override
    protected void addImportSection() {
        // nested class. containing class has necessary imports
    }

    @Override
    public void classEnd() {
        emitSizeof();
        emitAllocate();
        emitScopeAllocate();
        emitAllocateArray();
        emitScopeAllocateArray();
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

    @Override
    public void addGetter(String javaName, String nativeName, MemoryLayout layout, Class<?> type, MemoryLayout parentLayout) {
        incrAlign();
        indent();
        sb.append(PUB_MODS + type.getName() + " " + javaName + "$get(MemoryAddress addr) {\n");
        incrAlign();
        indent();
        sb.append("return (" + type.getName() + ")"
                + varHandleGetCallString(javaName, nativeName, layout, type, parentLayout) + ".get(addr);\n");
        decrAlign();
        indent();
        sb.append("}\n");
        decrAlign();

        addIndexGetter(javaName, nativeName, layout, type, parentLayout);
    }

    @Override
    public void addSetter(String javaName, String nativeName, MemoryLayout layout, Class<?> type, MemoryLayout parentLayout) {
        incrAlign();
        indent();
        String param = MemoryAddress.class.getName() + " addr";
        sb.append(PUB_MODS + "void " + javaName + "$set(" + param + ", " + type.getName() + " x) {\n");
        incrAlign();
        indent();
        sb.append(varHandleGetCallString(javaName, nativeName, layout, type, null) + ".set(addr, x);\n");
        decrAlign();
        indent();
        sb.append("}\n");
        decrAlign();

        addIndexSetter(javaName, nativeName, layout, type, parentLayout);
    }

    @Override
    public void addAddressGetter(String javaName, String nativeName, MemoryLayout layout, MemoryLayout parentLayout) {
        incrAlign();
        indent();
        sb.append(PUB_MODS + "MemoryAddress " + javaName + "$addr(MemoryAddress addr) {\n");
        incrAlign();
        indent();
        sb.append("return addr.segment().asSlice(");
        sb.append(parentLayout.byteOffset(MemoryLayout.PathElement.groupElement(nativeName)));
        sb.append(", ");
        sb.append(layout.byteSize());
        sb.append(").baseAddress();\n");
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

    private void emitAllocateArray() {
        incrAlign();
        indent();
        sb.append(PUB_MODS);
        sb.append("MemorySegment allocateArray(int len) {\n");
        incrAlign();
        indent();
        sb.append("return MemorySegment.allocateNative(MemoryLayout.ofSequence(len, $LAYOUT()));");
        decrAlign();
        sb.append("}\n");
        decrAlign();
    }

    private void emitScopeAllocateArray() {
        incrAlign();
        indent();
        sb.append(PUB_MODS);
        sb.append("MemoryAddress allocateArray(int len, NativeAllocationScope scope) {\n");
        incrAlign();
        indent();
        sb.append("return scope.allocate(MemoryLayout.ofSequence(len, $LAYOUT()));");
        decrAlign();
        sb.append("}\n");
        decrAlign();
    }

    private void addIndexGetter(String javaName, String nativeName, MemoryLayout layout, Class<?> type, MemoryLayout parentLayout) {
        incrAlign();
        indent();
        String params = MemoryAddress.class.getName() + " addr, long index";
        sb.append(PUB_MODS + type.getName() + " " + javaName + "$get(" + params + ") {\n");
        incrAlign();
        indent();
        sb.append("return (" + type.getName() + ")"
                + varHandleGetCallString(javaName, nativeName, layout, type, parentLayout) + ".get(addr.addOffset(index*sizeof()));\n");
        decrAlign();
        indent();
        sb.append("}\n");
        decrAlign();
    }

    private void addIndexSetter(String javaName, String nativeName, MemoryLayout layout, Class<?> type, MemoryLayout parentLayout) {
        incrAlign();
        indent();
        String params = MemoryAddress.class.getName() + " addr, long index, " + type.getName() + " x";
        sb.append(PUB_MODS + "void " + javaName + "$set(" + params + ") {\n");
        incrAlign();
        indent();
        sb.append(varHandleGetCallString(javaName, nativeName, layout, type, parentLayout) + ".set(addr.addOffset(index*sizeof()), x);\n");
        decrAlign();
        indent();
        sb.append("}\n");
        decrAlign();
    }
}
