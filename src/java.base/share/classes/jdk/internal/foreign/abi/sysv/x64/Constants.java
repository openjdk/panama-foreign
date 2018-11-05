/*
 * Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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
package jdk.internal.foreign.abi.sysv.x64;

import jdk.internal.foreign.abi.StorageClass;

public class Constants {
    public static final int INTEGER_REGISTER_SIZE = 8;
    public static final int VECTOR_REGISTER_SIZE = 64; // (maximum) vector size is 512 bits
    public static final int X87_REGISTER_SIZE = 16; // x87 register is 128 bits

    public static final int STACK_SLOT_SIZE = 8;

    public static final int MAX_INTEGER_ARGUMENT_REGISTERS = 6;
    public static final int MAX_INTEGER_RETURN_REGISTERS = 2;

    public static final int MAX_VECTOR_ARGUMENT_REGISTERS = 8;
    public static final int MAX_VECTOR_RETURN_REGISTERS = 2;
    public static final int MAX_X87_RETURN_REGISTERS = 2;

    public static final StorageClass[] ARGUMENT_STORAGE_CLASSES = {
        StorageClass.STACK_ARGUMENT_SLOT,
        StorageClass.VECTOR_ARGUMENT_REGISTER,
        StorageClass.INTEGER_ARGUMENT_REGISTER
    };

    public static final StorageClass[] RETURN_STORAGE_CLASSES = {
        StorageClass.VECTOR_RETURN_REGISTER,
        StorageClass.INTEGER_RETURN_REGISTER,
        StorageClass.X87_RETURN_REGISTER
    };
}
