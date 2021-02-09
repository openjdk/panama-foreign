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
        var layoutAccess = addLayout(layoutField(), ((Type.Declared)structType).tree().layout().get());
        emitGetter(MemoryLayout.class, "$LAYOUT", layoutAccess);
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

    @Override
    public void addVar(String javaName, String nativeName, MemoryLayout layout, Class<?> type) {
        if (type.equals(MemorySegment.class)) {
            emitSegmentGetter(javaName, nativeName, layout);
        } else {
            String vhAccess = addFieldVarHandle(getQualifiedName(javaName), nativeName, layout, type, layoutField(), structLayout, prefixNamesList());
            emitGetter(VarHandle.class, javaName + "$VH", vhAccess);
            emitFieldGetter(vhAccess, javaName, type);
            emitFieldSetter(vhAccess, javaName, type);
            emitIndexedFieldGetter(vhAccess, javaName, type);
            emitIndexedFieldSetter(vhAccess, javaName, type);
        }
    }

    // private generation

    private String getQualifiedName(String fieldName) {
        return qualifiedName(this) + "$" + fieldName;
    }

    private void emitFieldGetter(String vhStr, String javaName, Class<?> type) {
        incrAlign();
        indent();
        append(PUB_MODS + " " + type.getSimpleName() + " " + javaName + "$get(MemorySegment seg) {\n");
        incrAlign();
        indent();
        append("return (" + type.getName() + ")"
                + vhStr + ".get(seg);\n");
        decrAlign();
        indent();
        append("}\n");
        decrAlign();
    }

    private void emitFieldSetter(String vhStr, String javaName, Class<?> type) {
        incrAlign();
        indent();
        String param = MemorySegment.class.getSimpleName() + " seg";
        append(PUB_MODS + "void " + javaName + "$set( " + param + ", " + type.getSimpleName() + " x) {\n");
        incrAlign();
        indent();
        append(vhStr + ".set(seg, x);\n");
        decrAlign();
        indent();
        append("}\n");
        decrAlign();
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

    private void emitSegmentGetter(String javaName, String nativeName, MemoryLayout layout) {
        incrAlign();
        indent();
        append(PUB_MODS + "MemorySegment " + javaName + "$slice(MemorySegment seg) {\n");
        incrAlign();
        indent();
        append("return RuntimeHelper.nonCloseableNonTransferableSegment(seg.asSlice(");
        append(structLayout.byteOffset(elementPaths(nativeName)));
        append(", ");
        append(layout.byteSize());
        append("));\n");
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
        append(" MemorySegment allocate() { return MemorySegment.allocateNative($LAYOUT()); }\n");
        decrAlign();
    }

    private void emitScopeAllocate() {
        incrAlign();
        indent();
        append(PUB_MODS);
        append(" MemorySegment allocate(NativeScope scope) { return scope.allocate($LAYOUT()); }\n");
        decrAlign();
    }

    private void emitAllocateArray() {
        incrAlign();
        indent();
        append(PUB_MODS);
        append(" MemorySegment allocateArray(int len) {\n");
        incrAlign();
        indent();
        append("return MemorySegment.allocateNative(MemoryLayout.ofSequence(len, $LAYOUT()));\n");
        decrAlign();
        indent();
        append('}');
        decrAlign();
    }

    private void emitScopeAllocateArray() {
        incrAlign();
        indent();
        append(PUB_MODS);
        append(" MemorySegment allocateArray(int len, NativeScope scope) {\n");
        incrAlign();
        indent();
        append("return scope.allocate(MemoryLayout.ofSequence(len, $LAYOUT()));\n");
        decrAlign();
        indent();
        append("}\n");
        decrAlign();
    }

    private void emitAllocatePoiner() {
        incrAlign();
        indent();
        append(PUB_MODS);
        append(" MemorySegment allocatePointer() {\n");
        incrAlign();
        indent();
        append("return MemorySegment.allocateNative(C_POINTER);\n");
        decrAlign();
        indent();
        append("}\n");
        decrAlign();
    }

    private void emitScopeAllocatePointer() {
        incrAlign();
        indent();
        append(PUB_MODS);
        append(" MemorySegment allocatePointer(NativeScope scope) {\n");
        incrAlign();
        indent();
        append("return scope.allocate(C_POINTER);\n");
        decrAlign();
        indent();
        append("}\n");
        decrAlign();
    }

    private void emitAsRestricted() {
        incrAlign();
        indent();
        append(PUB_MODS);
        append(" MemorySegment ofAddressRestricted(MemoryAddress addr) { return RuntimeHelper.asArrayRestricted(addr, $LAYOUT(), 1); }\n");
        decrAlign();
    }

    private void emitIndexedFieldGetter(String vhStr, String javaName, Class<?> type) {
        incrAlign();
        indent();
        String params = MemorySegment.class.getSimpleName() + " seg, long index";
        append(PUB_MODS + " " + type.getSimpleName() + " " + javaName + "$get(" + params + ") {\n");
        incrAlign();
        indent();
        append("return (" + type.getName() + ")"
                + vhStr +
                ".get(seg.asSlice(index*sizeof()));\n");
        decrAlign();
        indent();
        append("}\n");
        decrAlign();
    }

    private void emitIndexedFieldSetter(String vhStr, String javaName, Class<?> type) {
        incrAlign();
        indent();
        String params = MemorySegment.class.getSimpleName() + " seg, long index, " + type.getSimpleName() + " x";
        append(PUB_MODS + "void " + javaName + "$set(" + params + ") {\n");
        incrAlign();
        indent();
        append(vhStr +
                ".set(seg.asSlice(index*sizeof()), x);\n");
        decrAlign();
        indent();
        append("}\n");
        decrAlign();
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

    private String layoutField() {
        String suffix = structLayout.isUnion() ? "union" : "struct";
        return qualifiedName(this) + "$" + suffix;
    }
}
