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

import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.jextract.Type;

import java.lang.invoke.VarHandle;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * This class generates static utilities class for C structs, unions.
 */
class StructBuilder extends ConstantBuilder {

    private final GroupLayout structLayout;
    private final Type structType;
    private final Deque<String> prefixElementNames;

    StructBuilder(JavaSourceBuilder enclosing, String className, GroupLayout structLayout, Type structType) {
        super(enclosing, Kind.CLASS, className);
        this.structLayout = structLayout;
        this.structType = structType;
        prefixElementNames = new ArrayDeque<>();
        classBegin();
    }

    public void pushPrefixElement(String prefixElementName) {
        this.prefixElementNames.push(prefixElementName);
    }

    public void popPrefixElement() {
        this.prefixElementNames.pop();
    }

    private List<String> prefixNamesList() {
        return Collections.unmodifiableList(new ArrayList<>(prefixElementNames));
    }

    @Override
    void classBegin() {
        super.classBegin();
        addLayoutGetter(layoutField(), ((Type.Declared)structType).tree().layout().get());
    }

    @Override
    JavaSourceBuilder classEnd() {
        emitSizeof();
        emitAllocate();
        emitScopeAllocate();
        emitAllocateArray();
        emitScopeAllocateArray();
        emitAllocatePoiner();
        emitScopeAllocatePointer();
        emitAsRestricted();
        return super.classEnd();
    }

    private String getQualifiedName(String fieldName) {
        return qualifiedName(this) + "$" + fieldName;
    }

    private void addVarHandleGetter(String javaName, String nativeName, MemoryLayout layout, Class<?> type) {
        var desc = addFieldVarHandle(getQualifiedName(javaName), nativeName, layout, type, layoutField(), structLayout, prefixNamesList());
        builder.incrAlign();
        builder.indent();
        builder.append(PUB_MODS + VarHandle.class.getSimpleName() + " " + javaName + "$VH() {\n");
        builder.incrAlign();
        builder.indent();
        builder.append("return " + desc + ";\n");
        builder.decrAlign();
        builder.indent();
        builder.append("}\n");
        builder.decrAlign();
    }

    private void addLayoutGetter(String javaName, MemoryLayout layout) {
        var desc = addLayout(javaName, layout);
        builder.incrAlign();
        builder.indent();
        builder.append(PUB_MODS + MemoryLayout.class.getSimpleName() + " $LAYOUT() {\n");
        builder.incrAlign();
        builder.indent();
        builder.append("return " + desc + ";\n");
        builder.decrAlign();
        builder.indent();
        builder.append("}\n");
        builder.decrAlign();
    }

    private void addGetter(String javaName, String nativeName, MemoryLayout layout, Class<?> type) {
        String vhStr = addFieldVarHandle(getQualifiedName(javaName), nativeName, layout, type, layoutField(), structLayout, prefixNamesList());
        builder.incrAlign();
        builder.indent();
        builder.append(PUB_MODS + " " + type.getSimpleName() + " " + javaName + "$get(MemorySegment seg) {\n");
        builder.incrAlign();
        builder.indent();
        builder.append("return (" + type.getName() + ")"
                + vhStr + ".get(seg);\n");
        builder.decrAlign();
        builder.indent();
        builder.append("}\n");
        builder.decrAlign();

        addIndexGetter(javaName, nativeName, layout, type);
    }

    private void addSetter(String javaName, String nativeName, MemoryLayout layout, Class<?> type) {
        String vhStr = addFieldVarHandle(getQualifiedName(javaName), nativeName, layout, type, layoutField(), structLayout, prefixNamesList());
        builder.incrAlign();
        builder.indent();
        String param = MemorySegment.class.getSimpleName() + " seg";
        builder.append(PUB_MODS + "void " + javaName + "$set( " + param + ", " + type.getSimpleName() + " x) {\n");
        builder.incrAlign();
        builder.indent();
        builder.append(vhStr + ".set(seg, x);\n");
        builder.decrAlign();
        builder.indent();
        builder.append("}\n");
        builder.decrAlign();

        addIndexSetter(javaName, nativeName, layout, type);
    }

    private MemoryLayout.PathElement[] elementPaths(String nativeFieldName) {
        List<String> prefixElements = prefixNamesList();
        MemoryLayout.PathElement[] elems = new MemoryLayout.PathElement[prefixElements.size() + 1];
        int i = 0;
        for (; i < prefixElements.size(); i++) {
            elems[i] = MemoryLayout.PathElement.groupElement(prefixElements.get(i));
        }
        elems[i] = MemoryLayout.PathElement.groupElement(nativeFieldName);
        return elems;
    }

    private void addSegmentGetter(String javaName, String nativeName, MemoryLayout layout) {
        builder.incrAlign();
        builder.indent();
        builder.append(PUB_MODS + "MemorySegment " + javaName + "$slice(MemorySegment seg) {\n");
        builder.incrAlign();
        builder.indent();
        builder.append("return RuntimeHelper.nonCloseableNonTransferableSegment(seg.asSlice(");
        builder.append(structLayout.byteOffset(elementPaths(nativeName)));
        builder.append(", ");
        builder.append(layout.byteSize());
        builder.append("));\n");
        builder.decrAlign();
        builder.indent();
        builder.append("}\n");
        builder.decrAlign();

    }

    private void emitSizeof() {
        builder.incrAlign();
        builder.indent();
        builder.append(PUB_MODS);
        builder.append("long sizeof() { return $LAYOUT().byteSize(); }\n");
        builder.decrAlign();
    }

    private void emitAllocate() {
        builder.incrAlign();
        builder.indent();
        builder.append(PUB_MODS);
        builder.append(" MemorySegment allocate() { return MemorySegment.allocateNative($LAYOUT()); }\n");
        builder.decrAlign();
    }

    private void emitScopeAllocate() {
        builder.incrAlign();
        builder.indent();
        builder.append(PUB_MODS);
        builder.append(" MemorySegment allocate(NativeScope scope) { return scope.allocate($LAYOUT()); }\n");
        builder.decrAlign();
    }

    private void emitAllocateArray() {
        builder.incrAlign();
        builder.indent();
        builder.append(PUB_MODS);
        builder.append(" MemorySegment allocateArray(int len) {\n");
        builder.incrAlign();
        builder.indent();
        builder.append("return MemorySegment.allocateNative(MemoryLayout.ofSequence(len, $LAYOUT()));\n");
        builder.decrAlign();
        builder.indent();
        builder.append('}');
        builder.decrAlign();
    }

    private void emitScopeAllocateArray() {
        builder.incrAlign();
        builder.indent();
        builder.append(PUB_MODS);
        builder.append(" MemorySegment allocateArray(int len, NativeScope scope) {\n");
        builder.incrAlign();
        builder.indent();
        builder.append("return scope.allocate(MemoryLayout.ofSequence(len, $LAYOUT()));\n");
        builder.decrAlign();
        builder.indent();
        builder.append("}\n");
        builder.decrAlign();
    }

    private void emitAllocatePoiner() {
        builder.incrAlign();
        builder.indent();
        builder.append(PUB_MODS);
        builder.append(" MemorySegment allocatePointer() {\n");
        builder.incrAlign();
        builder.indent();
        builder.append("return MemorySegment.allocateNative(C_POINTER);\n");
        builder.decrAlign();
        builder.indent();
        builder.append("}\n");
        builder.decrAlign();
    }

    private void emitScopeAllocatePointer() {
        builder.incrAlign();
        builder.indent();
        builder.append(PUB_MODS);
        builder.append(" MemorySegment allocatePointer(NativeScope scope) {\n");
        builder.incrAlign();
        builder.indent();
        builder.append("return scope.allocate(C_POINTER);\n");
        builder.decrAlign();
        builder.indent();
        builder.append("}\n");
        builder.decrAlign();
    }

    private void emitAsRestricted() {
        builder.incrAlign();
        builder.indent();
        builder.append(PUB_MODS);
        builder.append(" MemorySegment ofAddressRestricted(MemoryAddress addr) { return RuntimeHelper.asArrayRestricted(addr, $LAYOUT(), 1); }\n");
        builder.decrAlign();
    }

    private void addIndexGetter(String javaName, String nativeName, MemoryLayout layout, Class<?> type) {
        String vhStr = addFieldVarHandle(getQualifiedName(javaName), nativeName, layout, type, layoutField(), structLayout, prefixNamesList());
        builder.incrAlign();
        builder.indent();
        String params = MemorySegment.class.getSimpleName() + " seg, long index";
        builder.append(PUB_MODS + " " + type.getSimpleName() + " " + javaName + "$get(" + params + ") {\n");
        builder.incrAlign();
        builder.indent();
        builder.append("return (" + type.getName() + ")"
                + vhStr +
                ".get(seg.asSlice(index*sizeof()));\n");
        builder.decrAlign();
        builder.indent();
        builder.append("}\n");
        builder.decrAlign();
    }

    private void addIndexSetter(String javaName, String nativeName, MemoryLayout layout, Class<?> type) {
        String vhStr = addFieldVarHandle(getQualifiedName(javaName), nativeName, layout, type, layoutField(), structLayout, prefixNamesList());
        builder.incrAlign();
        builder.indent();
        String params = MemorySegment.class.getSimpleName() + " seg, long index, " + type.getSimpleName() + " x";
        builder.append(PUB_MODS + "void " + javaName + "$set(" + params + ") {\n");
        builder.incrAlign();
        builder.indent();
        builder.append(vhStr +
                ".set(seg.asSlice(index*sizeof()), x);\n");
        builder.decrAlign();
        builder.indent();
        builder.append("}\n");
        builder.decrAlign();
    }

    private String qualifiedName(JavaSourceBuilder builder) {
        if (builder instanceof NestedClassBuilder) {
            NestedClassBuilder nestedClassBuilder = (NestedClassBuilder)builder;
            String prefix = qualifiedName(nestedClassBuilder.enclosing);
            return prefix.isEmpty() ?
                    nestedClassBuilder.className :
                    prefix + "$" + nestedClassBuilder.className;
        } else {
            return "";
        }
    }

    String layoutField() {
        String suffix = structLayout.isUnion() ? "union" : "struct";
        return qualifiedName(this) + "$" + suffix;
    }

    @Override
    public void addVar(String javaName, String nativeName, MemoryLayout layout, Class<?> type) {
        if (type.equals(MemorySegment.class)) {
            addSegmentGetter(javaName, nativeName, layout);
        } else {
            addVarHandleGetter(javaName, nativeName, layout, type);
            addGetter(javaName, nativeName, layout, type);
            addSetter(javaName, nativeName, layout, type);
        }
    }

    @Override
    public StructBuilder addStruct(String name, GroupLayout parentLayout, Type type) {
        return new StructBuilder(this, name, structLayout, type);
    }
}
