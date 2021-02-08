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

import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryLayout;

import javax.tools.JavaFileObject;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

public class MultiFileConstantHelper implements ConstantHelper {
    static final int CONSTANTS_PER_CLASS_SOURCES = Integer.getInteger("jextract.constants.per.class.source", 1000);
    static final int CONSTANTS_PER_CLASS_CLASSES = Integer.getInteger("jextract.constants.per.class.binary", 10000);

    @FunctionalInterface
    interface ConstantHelperFunc {
        ConstantHelper make(String simpleClassName, String baseClassName, boolean isFinal);
    }

    private final ConstantHelperFunc delegateFactory;
    private final String headerClassName;
    private final int constantsPerClass;

    private int constantCount;
    private int constantClassCount;
    private ConstantHelper delegate;

    private final List<JavaFileObject> finishedClasses = new ArrayList<>();

    public MultiFileConstantHelper(String headerClassName, ConstantHelperFunc func, int constantsPerClass) {
        this.headerClassName = headerClassName;
        this.delegateFactory = func;
        this.constantsPerClass = constantsPerClass;
        this.delegate = delegateFactory.make(getConstantClassName(), null, false);
    }

    private String getConstantClassName() {
        return headerClassName + "_constants_" + constantClassCount;
    }

    private void checkNewConstantsClass() {
        if (constantCount > constantsPerClass) {
            newConstantsClass(false);
        }
        constantCount++;
    }

    private void newConstantsClass(boolean isFinal) {
        finishedClasses.addAll(delegate.build());
        String currentClassName = getConstantClassName();
        constantClassCount++;
        String newClassName = getConstantClassName();
        delegate = delegateFactory.make(newClassName, currentClassName, isFinal);
        this.constantCount = 0;
    }

    @Override
    public DirectMethodHandleDesc addLayout(String javaName, MemoryLayout layout) {
        checkNewConstantsClass();
        return delegate.addLayout(javaName, layout);
    }

    @Override
    public DirectMethodHandleDesc addFieldVarHandle(String javaName, String nativeName, MemoryLayout layout,
                                                    Class<?> type, String rootJavaName, MemoryLayout rootLayout,
                                                    List<String> elementNames) {
        checkNewConstantsClass();
        return delegate.addFieldVarHandle(javaName, nativeName, layout, type, rootJavaName, rootLayout, elementNames);
    }

    @Override
    public DirectMethodHandleDesc addGlobalVarHandle(String javaName, String nativeName, MemoryLayout layout, Class<?> type) {
        checkNewConstantsClass();
        return delegate.addGlobalVarHandle(javaName, nativeName, layout, type);
    }

    @Override
    public DirectMethodHandleDesc addMethodHandle(String javaName, String nativeName, MethodType mtype, FunctionDescriptor desc, boolean varargs) {
        checkNewConstantsClass();
        return delegate.addMethodHandle(javaName, nativeName, mtype, desc, varargs);
    }

    @Override
    public DirectMethodHandleDesc addSegment(String javaName, String nativeName, MemoryLayout layout) {
        checkNewConstantsClass();
        return delegate.addSegment(javaName, nativeName, layout);
    }

    @Override
    public DirectMethodHandleDesc addFunctionDesc(String javaName, FunctionDescriptor fDesc) {
        checkNewConstantsClass();
        return delegate.addFunctionDesc(javaName, fDesc);
    }

    @Override
    public DirectMethodHandleDesc addConstantDesc(String name, Class<?> type, Object value) {
        checkNewConstantsClass();
        return delegate.addConstantDesc(name, type, value);
    }

    @Override
    public List<JavaFileObject> build() {
        newConstantsClass(true);
        return new ArrayList<>(finishedClasses);
    }
}
