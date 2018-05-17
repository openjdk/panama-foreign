/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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

package jdk.internal.nicl.types;

import java.lang.invoke.MethodHandle;

/**
 * A reference is a view over a memory layout associated with a {@link java.nicl.types.Pointer} instance. As such,
 * it provides capabilities for retrieving (resp. storing) values from (resp. to) memory.
 */
public interface Reference {


    /**
     * A {@link MethodHandle} which can be used to retrieve the contents of memory layout associated
     * with the pointer passed as argument.
     * <p>
     * A getter method handle is of the form:
     * {@code () -> T}
     * Where {@code T} is the Java type to which the layout will be converted.
     * </p>
     * @return a 'getter' method handle.
     */
    MethodHandle getter();

    /**
     * A {@link MethodHandle} which can be used to store a value into the memory layout associated with
     * <p>
     * A setter method handle is of the form:
     * {@code (T) -> V}
     * Where {@code T} is the Java type to which the layout will be converted.
     * </p>
     * the pointer passed as argument.
     * @return a 'getter' method handle.
     */
    MethodHandle setter();
}
