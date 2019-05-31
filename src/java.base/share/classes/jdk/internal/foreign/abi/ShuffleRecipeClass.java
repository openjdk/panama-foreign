/*
 * Copyright (c) 2015, 2019, Oracle and/or its affiliates. All rights reserved.
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
package jdk.internal.foreign.abi;

enum ShuffleRecipeClass {
    BUFFER(null, null),
    STACK(StorageClass.STACK_ARGUMENT_SLOT, null),
    VECTOR(StorageClass.VECTOR_ARGUMENT_REGISTER, StorageClass.VECTOR_RETURN_REGISTER),
    INTEGER(StorageClass.INTEGER_ARGUMENT_REGISTER, StorageClass.INTEGER_RETURN_REGISTER),
    X87(null, StorageClass.X87_RETURN_REGISTER),
    INDIRECT(StorageClass.INDIRECT_RESULT_REGISTER, null);

    private final StorageClass argumentStorageClass;
    private final StorageClass returnStorageClass;

    ShuffleRecipeClass(StorageClass argumentStorageClass, StorageClass returnStorageClass) {
        this.argumentStorageClass = argumentStorageClass;
        this.returnStorageClass = returnStorageClass;
    }

    public StorageClass storageClass(boolean args) {
        return args ? argumentStorageClass : returnStorageClass;
    }
}
