/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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
package jdk.internal.nicl.abi;

import jdk.internal.nicl.types.Container;
import jdk.internal.nicl.types.ContainerSizeInfo;
import jdk.internal.nicl.types.Type;
import jdk.internal.nicl.types.Function;

public interface SystemABI {
    /**
     * The size in bytes for a type as defined by the ABI
     */
    long definedSize(char typeCode);

    /**
     * The alignment requirement for a type as defined by the ABI
     */
    long definedAlignment(char typeCode);

    /**
     * The alignment requirement for a given type
     * @param isVar indicate if the type is a standalone variable. This change how
     * array is aligned. for example.
     */
    long alignment(Type t, boolean isVar);

    /**
     * The size of a given type considering alignment requirement
     */
    long sizeof(Type t);

    /**
     * Align the specified type from a given address
     * @return The address the data should be at based on alignment requirement
     */
    long align(Type t, boolean isVar, long addr);

    ContainerSizeInfo layout(Container c, long pack);

    CallingSequence arrangeCall(Function f);
}
