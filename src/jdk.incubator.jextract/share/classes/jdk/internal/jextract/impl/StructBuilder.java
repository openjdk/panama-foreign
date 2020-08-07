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
package jdk.internal.jextract.impl;

import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;

/**
 * This class generates static utilities class for C structs, unions.
 */
class StructBuilder extends JavaSourceBuilder {

    private final JavaSourceBuilder prev;

    StructBuilder(JavaSourceBuilder prev, String className, String pkgName, ConstantHelper constantHelper) {
        super(prev.uniqueNestedClassName(className), pkgName, constantHelper);
        this.prev = prev;
    }

    JavaSourceBuilder prev() {
        return prev;
    }

    @Override
    void append(String s) {
        prev.append(s);
    }

    @Override
    void append(char c) {
        prev.append(c);
    }

    @Override
    void append(long l) {
        prev.append(l);
    }

    @Override
    void indent() {
        prev.indent();
    }

    @Override
    void incrAlign() {
        prev.incrAlign();
    }

    @Override
    void decrAlign() {
        prev.decrAlign();
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
    JavaSourceBuilder classEnd() {
        emitSizeof();
        emitAllocate();
        emitScopeAllocate();
        emitAllocateArray();
        emitScopeAllocateArray();
        return super.classEnd();
    }

    @Override
    void addLayoutGetter(String javaName, MemoryLayout layout) {
        var desc = constantHelper.addLayout(javaName + "$struct", layout);
        incrAlign();
        indent();
        append(PUB_MODS + displayName(desc.invocationType().returnType()) + " $LAYOUT() {\n");
        incrAlign();
        indent();
        append("return " + getCallString(desc) + ";\n");
        decrAlign();
        indent();
        append("}\n");
        decrAlign();
    }

    @Override
    void addGetter(String javaName, String nativeName, MemoryLayout layout, Class<?> type, MemoryLayout parentLayout) {
        incrAlign();
        indent();
        append(PUB_MODS + type.getName() + " " + javaName + "$get(MemorySegment addr) {\n");
        incrAlign();
        indent();
        append("return (" + type.getName() + ")"
                + varHandleGetCallString(javaName, nativeName, layout, type, parentLayout) + ".get(addr);\n");
        decrAlign();
        indent();
        append("}\n");
        decrAlign();

        addIndexGetter(javaName, nativeName, layout, type, parentLayout);
    }

    @Override
    void addSetter(String javaName, String nativeName, MemoryLayout layout, Class<?> type, MemoryLayout parentLayout) {
        incrAlign();
        indent();
        String param = MemorySegment.class.getName() + " addr";
        append(PUB_MODS + "void " + javaName + "$set(" + param + ", " + type.getName() + " x) {\n");
        incrAlign();
        indent();
        append(varHandleGetCallString(javaName, nativeName, layout, type, null) + ".set(addr, x);\n");
        decrAlign();
        indent();
        append("}\n");
        decrAlign();

        addIndexSetter(javaName, nativeName, layout, type, parentLayout);
    }

    @Override
    void addAddressGetter(String javaName, String nativeName, MemoryLayout layout, MemoryLayout parentLayout) {
        incrAlign();
        indent();
        append(PUB_MODS + "MemorySegment " + javaName + "$addr(MemorySegment addr) {\n");
        incrAlign();
        indent();
        append("return addr.asSlice(");
        append(parentLayout.byteOffset(MemoryLayout.PathElement.groupElement(nativeName)));
        append(", ");
        append(layout.byteSize());
        append(");\n");
        decrAlign();
        indent();
        append("}\n");
        decrAlign();
    }

    private void emitSizeof() {
        incrAlign();
        indent();
        append(PUB_MODS);
        append("long sizeof() { return $LAYOUT().byteSize(); }\n");
        decrAlign();
    }

    private void emitAllocate() {
        incrAlign();
        indent();
        append(PUB_MODS);
        append("MemorySegment allocate() { return MemorySegment.allocateNative($LAYOUT()); }\n");
        decrAlign();
    }

    private void emitScopeAllocate() {
        incrAlign();
        indent();
        append(PUB_MODS);
        append("MemorySegment allocate(NativeScope scope) { return scope.allocate($LAYOUT()); }\n");
        decrAlign();
    }

    private void emitAllocateArray() {
        incrAlign();
        indent();
        append(PUB_MODS);
        append("MemorySegment allocateArray(int len) {\n");
        incrAlign();
        indent();
        append("return MemorySegment.allocateNative(MemoryLayout.ofSequence(len, $LAYOUT()));");
        decrAlign();
        append("}\n");
        decrAlign();
    }

    private void emitScopeAllocateArray() {
        incrAlign();
        indent();
        append(PUB_MODS);
        append("MemorySegment allocateArray(int len, NativeScope scope) {\n");
        incrAlign();
        indent();
        append("return scope.allocate(MemoryLayout.ofSequence(len, $LAYOUT()));");
        decrAlign();
        append("}\n");
        decrAlign();
    }

    private void addIndexGetter(String javaName, String nativeName, MemoryLayout layout, Class<?> type, MemoryLayout parentLayout) {
        incrAlign();
        indent();
        String params = MemorySegment.class.getName() + " addr, long index";
        append(PUB_MODS + type.getName() + " " + javaName + "$get(" + params + ") {\n");
        incrAlign();
        indent();
        append("return (" + type.getName() + ")"
                + varHandleGetCallString(javaName, nativeName, layout, type, parentLayout) + ".get(addr.asSlice(index*sizeof()));\n");
        decrAlign();
        indent();
        append("}\n");
        decrAlign();
    }

    private void addIndexSetter(String javaName, String nativeName, MemoryLayout layout, Class<?> type, MemoryLayout parentLayout) {
        incrAlign();
        indent();
        String params = MemorySegment.class.getName() + " addr, long index, " + type.getName() + " x";
        append(PUB_MODS + "void " + javaName + "$set(" + params + ") {\n");
        incrAlign();
        indent();
        append(varHandleGetCallString(javaName, nativeName, layout, type, parentLayout) + ".set(addr.asSlice(index*sizeof()), x);\n");
        decrAlign();
        indent();
        append("}\n");
        decrAlign();
    }
}
