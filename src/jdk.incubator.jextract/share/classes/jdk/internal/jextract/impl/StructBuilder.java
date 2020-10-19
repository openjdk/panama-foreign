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
import jdk.incubator.jextract.Declaration;
import jdk.incubator.jextract.Type;

/**
 * This class generates static utilities class for C structs, unions.
 */
class StructBuilder extends NestedClassBuilder {

    private final GroupLayout parentLayout;
    private final String structAnno;
    private final String structArrayAnno;
    private final String structPtrAnno;
    private final Type structType;

    StructBuilder(JavaSourceBuilder enclosing, String className, GroupLayout parentLayout, Type structType) {
        super(enclosing, Kind.CLASS, className);
        this.parentLayout = parentLayout;
        this.structAnno = annotationWriter.getCAnnotation(structType);
        this.structArrayAnno = annotationWriter.getCAnnotation(Type.array(structType));
        this.structPtrAnno = annotationWriter.getCAnnotation(Type.pointer(structType));
        this.structType = structType;
    }

    @Override
    Type type() {
        return structType;
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

    @Override
    void addVarHandleGetter(String javaName, String nativeName, MemoryLayout layout, Class<?> type) {
        var desc = constantHelper.addFieldVarHandle(getQualifiedName(javaName), nativeName, layout, type, layoutField(), parentLayout);
        builder.incrAlign();
        builder.indent();
        builder.append(PUB_MODS + displayName(desc.invocationType().returnType()) + " " + javaName + "$VH() {\n");
        builder.incrAlign();
        builder.indent();
        builder.append("return " + getCallString(desc) + ";\n");
        builder.decrAlign();
        builder.indent();
        builder.append("}\n");
        builder.decrAlign();
    }

    @Override
    void addLayoutGetter(String javaName, MemoryLayout layout) {
        var desc = constantHelper.addLayout(javaName, layout);
        builder.incrAlign();
        builder.indent();
        builder.append(PUB_MODS + displayName(desc.invocationType().returnType()) + " $LAYOUT() {\n");
        builder.incrAlign();
        builder.indent();
        builder.append("return " + getCallString(desc) + ";\n");
        builder.decrAlign();
        builder.indent();
        builder.append("}\n");
        builder.decrAlign();
    }

    @Override
    void addGetter(String javaName, String nativeName, MemoryLayout layout, Class<?> type, String anno) {
        builder.incrAlign();
        builder.indent();
        builder.append(PUB_MODS + " " + anno + " " + type.getSimpleName() + " " + javaName + "$get(" + this.structAnno + " MemorySegment seg) {\n");
        builder.incrAlign();
        builder.indent();
        builder.append("return (" + type.getName() + ")"
                + fieldVarHandleGetCallString(getQualifiedName(javaName), nativeName, layout, type) + ".get(seg);\n");
        builder.decrAlign();
        builder.indent();
        builder.append("}\n");
        builder.decrAlign();

        addIndexGetter(javaName, nativeName, layout, type, anno);
    }

    @Override
    void addSetter(String javaName, String nativeName, MemoryLayout layout, Class<?> type, String anno) {
        builder.incrAlign();
        builder.indent();
        String param = MemorySegment.class.getSimpleName() + " seg";
        builder.append(PUB_MODS + "void " + javaName + "$set(" + this.structAnno + " " + param + ", " + anno + " " + type.getSimpleName() + " x) {\n");
        builder.incrAlign();
        builder.indent();
        builder.append(fieldVarHandleGetCallString(getQualifiedName(javaName), nativeName, layout, type) + ".set(seg, x);\n");
        builder.decrAlign();
        builder.indent();
        builder.append("}\n");
        builder.decrAlign();

        addIndexSetter(javaName, nativeName, layout, type, anno);
    }

    @Override
    void addSegmentGetter(String javaName, String nativeName, MemoryLayout layout) {
        builder.incrAlign();
        builder.indent();
        builder.append(PUB_MODS + "MemorySegment " + javaName + "$slice(MemorySegment seg) {\n");
        builder.incrAlign();
        builder.indent();
        builder.append("return RuntimeHelper.nonCloseableNonTransferableSegment(seg.asSlice(");
        builder.append(parentLayout.byteOffset(MemoryLayout.PathElement.groupElement(nativeName)));
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
        builder.append(structAnno + " MemorySegment allocate() { return MemorySegment.allocateNative($LAYOUT()); }\n");
        builder.decrAlign();
    }

    private void emitScopeAllocate() {
        builder.incrAlign();
        builder.indent();
        builder.append(PUB_MODS);
        builder.append(structAnno + " MemorySegment allocate(NativeScope scope) { return scope.allocate($LAYOUT()); }\n");
        builder.decrAlign();
    }

    private void emitAllocateArray() {
        builder.incrAlign();
        builder.indent();
        builder.append(PUB_MODS);
        builder.append(structArrayAnno + " MemorySegment allocateArray(int len) {\n");
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
        builder.append(structArrayAnno + " MemorySegment allocateArray(int len, NativeScope scope) {\n");
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
        builder.append(structPtrAnno + " MemorySegment allocatePointer() {\n");
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
        builder.append(structPtrAnno + " MemorySegment allocatePointer(NativeScope scope) {\n");
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
        builder.append(structAnno + " MemorySegment ofAddressRestricted(MemoryAddress addr) { return RuntimeHelper.asArrayRestricted(addr, $LAYOUT(), 1); }\n");
        builder.decrAlign();
    }

    private void addIndexGetter(String javaName, String nativeName, MemoryLayout layout, Class<?> type, String anno) {
        builder.incrAlign();
        builder.indent();
        String params = this.structAnno + " " + MemorySegment.class.getSimpleName() + " seg, long index";
        builder.append(PUB_MODS + " " + anno + " " + type.getSimpleName() + " " + javaName + "$get(" + params + ") {\n");
        builder.incrAlign();
        builder.indent();
        builder.append("return (" + type.getName() + ")"
                + fieldVarHandleGetCallString(getQualifiedName(javaName), nativeName, layout, type) +
                ".get(seg.asSlice(index*sizeof()));\n");
        builder.decrAlign();
        builder.indent();
        builder.append("}\n");
        builder.decrAlign();
    }

    private void addIndexSetter(String javaName, String nativeName, MemoryLayout layout, Class<?> type, String anno) {
        builder.incrAlign();
        builder.indent();
        String params = this.structAnno + " " + MemorySegment.class.getSimpleName() + " seg, long index, " + anno + " " + type.getSimpleName() + " x";
        builder.append(PUB_MODS + "void " + javaName + "$set(" + params + ") {\n");
        builder.incrAlign();
        builder.indent();
        builder.append(fieldVarHandleGetCallString(getQualifiedName(javaName), nativeName, layout, type) +
                ".set(seg.asSlice(index*sizeof()), x);\n");
        builder.decrAlign();
        builder.indent();
        builder.append("}\n");
        builder.decrAlign();
    }

    private String fieldVarHandleGetCallString(String javaName, String nativeName, MemoryLayout layout, Class<?> type) {
        return getCallString(constantHelper.addFieldVarHandle(javaName, nativeName, layout, type, layoutField(), parentLayout));
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
        GroupLayout groupLayout = parentLayout;
        String suffix = groupLayout.isUnion() ? "union" : "struct";
        return qualifiedName(this) + "$" + suffix;
    }
}
