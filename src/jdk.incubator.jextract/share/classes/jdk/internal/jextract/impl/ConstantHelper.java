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
import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.invoke.MethodType;
import java.util.List;

import static jdk.internal.jextract.impl.MultiFileConstantHelper.CONSTANTS_PER_CLASS_CLASSES;
import static jdk.internal.jextract.impl.MultiFileConstantHelper.CONSTANTS_PER_CLASS_SOURCES;

interface ConstantHelper {
    DirectMethodHandleDesc addLayout(String javaName, MemoryLayout layout);
    DirectMethodHandleDesc addFieldVarHandle(String javaName, String nativeName, MemoryLayout layout, Class<?> type, String parentJavaName, MemoryLayout parentLayout);
    DirectMethodHandleDesc addGlobalVarHandle(String javaName, String nativeName, MemoryLayout layout, Class<?> type);
    DirectMethodHandleDesc addMethodHandle(String javaName, String nativeName, MethodType mtype, FunctionDescriptor desc, boolean varargs);
    DirectMethodHandleDesc addSegment(String javaName, String nativeName, MemoryLayout layout);
    DirectMethodHandleDesc addFunctionDesc(String javaName, FunctionDescriptor fDesc);
    DirectMethodHandleDesc addConstant(String name, Class<?> type, Object value);
    List<JavaFileObject> build();

    static ConstantHelper make(boolean source, String packageName, String headerClassName, ClassDesc runtimeHelper,
                               ClassDesc cString, String[] libraryNames) {
        return new MultiFileConstantHelper(headerClassName,
            (simpleClassName, baseClassName, isFinal) -> source
                ? SourceConstantHelper.make(packageName, simpleClassName, libraryNames, baseClassName)
                : ClassConstantHelper.make(packageName, simpleClassName, runtimeHelper, cString, libraryNames, baseClassName),
            source ? CONSTANTS_PER_CLASS_SOURCES : CONSTANTS_PER_CLASS_CLASSES);
    }

    interface ConstantHelperFactory {
        ConstantHelper make(String headerClassName);
    }
}
