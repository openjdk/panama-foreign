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

import javax.tools.JavaFileObject;
import java.lang.constant.ClassDesc;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

import static jdk.internal.jextract.impl.LayoutUtils.JEXTRACT_ANONYMOUS;

/**
 * This class generates static utilities class for C structs, unions.
 */
class StructBuilder extends ConstantBuilder {

    private static final String MEMBER_MODS = "public static";

    private final GroupLayout structLayout;
    private final Type structType;
    private final Deque<String> prefixElementNames;

    StructBuilder(ConstantHelper constantHelper, ClassDesc desc, GroupLayout structLayout, Type structType) {
        super(constantHelper, Kind.CLASS, desc);
        this.structLayout = structLayout;
        this.structType = structType;
        prefixElementNames = new ArrayDeque<>();
    }

    StructBuilder(JavaSourceBuilder enclosing, String className, GroupLayout structLayout, Type structType) {
        super(enclosing, Kind.CLASS, className);
        this.structLayout = structLayout;
        this.structType = structType;
        prefixElementNames = new ArrayDeque<>();
    }

    private String safeParameterName(String paramName) {
        return isEnclosedBySameName(paramName)? paramName + "$" : paramName;
    }

    void pushPrefixElement(String prefixElementName) {
        prefixElementNames.push(prefixElementName);
    }

    void popPrefixElement() {
        prefixElementNames.pop();
    }

    private List<String> prefixNamesList() {
        List<String> prefixes = new ArrayList<>(prefixElementNames);
        Collections.reverse(prefixes);
        return Collections.unmodifiableList(prefixes);
    }

    @Override
    void classBegin() {
        if (!inAnonymousNested()) {
            super.classBegin();
            addLayout(layoutField(), ((Type.Declared) structType).tree().layout().get())
                    .emitGetter(this, MEMBER_MODS, Constant.SUFFIX_ONLY);
        }
    }

    @Override
    JavaSourceBuilder classEnd() {
        if (!inAnonymousNested()) {
            emitSizeof();
            emitAllocate();
            emitScopeAllocate();
            emitAllocateArray();
            emitScopeAllocateArray();
            emitAllocatePoiner();
            emitScopeAllocatePointer();
            emitAsRestricted();
            return super.classEnd();
        } else {
            // we're in an anonymous struct which got merged into this one, return this very builder and keep it open
            popPrefixElement();
            return this;
        }
    }

    boolean inAnonymousNested() {
        return !prefixElementNames.isEmpty();
    }

    @Override
    public StructBuilder addStruct(String name, Declaration parent, GroupLayout layout, Type type) {
        if (name.isEmpty() && (parent instanceof Declaration.Scoped)) {
            //nested anon struct - merge into this builder!
            GroupLayout parentLayout = (GroupLayout)((Declaration.Scoped)parent).layout().get();
            String anonName = findAnonymousStructName(parentLayout, layout);
            pushPrefixElement(anonName);
            return this;
        } else {
            return new StructBuilder(this, name.isEmpty() ? parent.name() : name, layout, type);
        }
    }

    @Override
    public String addFunctionalInterface(String name, FunctionInfo functionInfo) {
        FunctionalInterfaceBuilder builder = new FunctionalInterfaceBuilder(this, name, functionInfo.methodType(), functionInfo.descriptor());
        builder.classBegin();
        builder.classEnd();
        return builder.className();
    }

    private String findAnonymousStructName(GroupLayout parentLayout, GroupLayout layout) {
        // nested anonymous struct or union
        for (MemoryLayout ml : parentLayout.memberLayouts()) {
            // look for anonymous structs
            if (ml.attribute(JEXTRACT_ANONYMOUS).isPresent()) {
                // it's enough to just compare the member layouts, since the member names
                // have to be unique within the parent layout (in C)
                if (((GroupLayout) ml).memberLayouts().equals(layout.memberLayouts())) {
                    return ml.name().orElseThrow();
                }
            }
        }
        throw new IllegalStateException("Could not find layout in parent");
    }

    @Override
    public void addVar(String javaName, String nativeName, VarInfo varInfo) {
        try {
            structLayout.byteOffset(elementPaths(nativeName));
        } catch (UnsupportedOperationException uoe) {
            // bad layout - do nothing
            OutputFactory.warn("skipping '" + className() + "." + nativeName + "' : " + uoe.toString());
            return;
        }
        if (varInfo.carrier().equals(MemorySegment.class)) {
            emitSegmentGetter(javaName, nativeName, varInfo.layout());
        } else {
            Constant vhConstant = addFieldVarHandle(javaName, nativeName, varInfo, layoutField(), prefixNamesList())
                    .emitGetter(this, MEMBER_MODS, Constant.QUALIFIED_NAME);
            emitFieldGetter(vhConstant, javaName, varInfo.carrier());
            emitFieldSetter(vhConstant, javaName, varInfo.carrier());
            emitIndexedFieldGetter(vhConstant, javaName, varInfo.carrier());
            emitIndexedFieldSetter(vhConstant, javaName, varInfo.carrier());
            if (varInfo.fiName().isPresent()) {
                emitFunctionalInterfaceGetter(varInfo.fiName().get(), javaName);
            }
        }
    }

    private void emitFunctionalInterfaceGetter(String fiName, String javaName) {
        incrAlign();
        indent();
        append(MEMBER_MODS + " ");
        append(fiName + " " + javaName + " (MemorySegment segment) {\n");
        incrAlign();
        indent();
        append("return " + fiName + ".ofAddressRestricted(" + javaName + "$get(segment));\n");
        decrAlign();
        indent();
        append("}\n");
        decrAlign();
    }

    private void emitFieldGetter(Constant vhConstant, String javaName, Class<?> type) {
        incrAlign();
        indent();
        String seg = safeParameterName("seg");
        append(MEMBER_MODS + " " + type.getSimpleName() + " " + javaName + "$get(MemorySegment " + seg + ") {\n");
        incrAlign();
        indent();
        append("return (" + type.getName() + ")"
                + vhConstant.accessExpression() + ".get(" + seg + ");\n");
        decrAlign();
        indent();
        append("}\n");
        decrAlign();
    }

    private void emitFieldSetter(Constant vhConstant, String javaName, Class<?> type) {
        incrAlign();
        indent();
        String seg = safeParameterName("seg");
        String x = safeParameterName("x");
        String param = MemorySegment.class.getSimpleName() + " " + seg;
        append(MEMBER_MODS + " void " + javaName + "$set( " + param + ", " + type.getSimpleName() + " " + x + ") {\n");
        incrAlign();
        indent();
        append(vhConstant.accessExpression() + ".set(" + seg + ", " + x + ");\n");
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
        String seg = safeParameterName("seg");
        append(MEMBER_MODS + " MemorySegment " + javaName + "$slice(MemorySegment " + seg + ") {\n");
        incrAlign();
        indent();
        append("return RuntimeHelper.nonCloseableNonTransferableSegment(");
        append(seg);
        append(".asSlice(");
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
        append(MEMBER_MODS);
        append(" long sizeof() { return $LAYOUT().byteSize(); }\n");
        decrAlign();
    }

    private void emitAllocate() {
        incrAlign();
        indent();
        append(MEMBER_MODS);
        append(" MemorySegment allocate() { return MemorySegment.allocateNative($LAYOUT()); }\n");
        decrAlign();
    }

    private void emitScopeAllocate() {
        incrAlign();
        indent();
        append(MEMBER_MODS);
        append(" MemorySegment allocate(NativeScope scope) { return scope.allocate($LAYOUT()); }\n");
        decrAlign();
    }

    private void emitAllocateArray() {
        incrAlign();
        indent();
        append(MEMBER_MODS);
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
        append(MEMBER_MODS);
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
        append(MEMBER_MODS);
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
        append(MEMBER_MODS);
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
        append(MEMBER_MODS);
        append(" MemorySegment ofAddressRestricted(MemoryAddress addr) { return RuntimeHelper.asArrayRestricted(addr, $LAYOUT(), 1); }\n");
        decrAlign();
    }

    private void emitIndexedFieldGetter(Constant vhConstant, String javaName, Class<?> type) {
        incrAlign();
        indent();
        String index = safeParameterName("index");
        String seg = safeParameterName("seg");
        String params = MemorySegment.class.getSimpleName() + " " + seg + ", long " + index;
        append(MEMBER_MODS + " " + type.getSimpleName() + " " + javaName + "$get(" + params + ") {\n");
        incrAlign();
        indent();
        append("return (" + type.getName() + ")");
        append(vhConstant.accessExpression());
        append(".get(");
        append(seg);
        append(".asSlice(");
        append(index);
        append("*sizeof()));\n");
        decrAlign();
        indent();
        append("}\n");
        decrAlign();
    }

    private void emitIndexedFieldSetter(Constant vhConstant, String javaName, Class<?> type) {
        incrAlign();
        indent();
        String index = safeParameterName("index");
        String seg = safeParameterName("seg");
        String x = safeParameterName("x");
        String params = MemorySegment.class.getSimpleName() + " " + seg +
            ", long " + index + ", " + type.getSimpleName() + " " + x;
        append(MEMBER_MODS + " void " + javaName + "$set(" + params + ") {\n");
        incrAlign();
        indent();
        append(vhConstant.accessExpression());
        append(".set(");
        append(seg);
        append(".asSlice(");
        append(index);
        append("*sizeof()), ");
        append(x);
        append(");\n");
        decrAlign();
        indent();
        append("}\n");
        decrAlign();
    }

    private String qualifiedName(JavaSourceBuilder builder) {
        if (builder.enclosing != null) {
            String prefix = qualifiedName(builder.enclosing);
            return prefix.isEmpty() ?
                    builder.className() :
                    prefix + "$" + builder.className();
        } else {
            return "";
        }
    }

    private String layoutField() {
        String suffix = structLayout.isUnion() ? "union" : "struct";
        return qualifiedName(this) + "$" + suffix;
    }

    @Override
    public ConstantHelper constantHelper() {
        return new ConstantHelper() {
            @Override
            public String librariesClass() {
                return constantHelper.librariesClass();
            }

            @Override
            public void emitWithConstantClass(Consumer<ConstantBuilder> constantConsumer) {
                constantConsumer.accept(StructBuilder.this);
            }
        };
    }
}
