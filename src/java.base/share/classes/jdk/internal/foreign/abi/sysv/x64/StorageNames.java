/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
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

import jdk.internal.foreign.abi.Storage;

public class StorageNames {
    private static final String[] INTEGER_ARGUMENT_REGISTER_NAMES = { "rdi", "rsi", "rdx", "rcx", "r8", "r9" };
    private static final String[] INTEGER_RETURN_REGISTERS_NAMES = { "rax", "rdx" };

    private static String getVectorRegisterName(long index, long size) {
        switch ((int)size) {
        case 8: return "xmm" + index + "_8";
        case 16: return "xmm" + index;
        case 32: return "ymm" + index;
        case 64: return "zmm" + index;
        default: throw new IllegalArgumentException("Illegal vector size: " + size);
        }
    }

    public static String getStorageName(Storage storage) {
        switch (storage.getStorageClass()) {
        case INTEGER_ARGUMENT_REGISTER:
            if (storage.getStorageIndex() > Constants.MAX_INTEGER_ARGUMENT_REGISTERS) {
                throw new IllegalArgumentException("Illegal storage: " + storage);
            }
            return INTEGER_ARGUMENT_REGISTER_NAMES[(int) storage.getStorageIndex()];

        case VECTOR_ARGUMENT_REGISTER:
            if (storage.getStorageIndex() > Constants.MAX_VECTOR_ARGUMENT_REGISTERS) {
                throw new IllegalArgumentException("Illegal storage: " + storage);
            }
            return getVectorRegisterName(storage.getStorageIndex(), storage.getSize());

        case INTEGER_RETURN_REGISTER:
            if (storage.getStorageIndex() > Constants.MAX_INTEGER_RETURN_REGISTERS) {
                throw new IllegalArgumentException("Illegal storage: " + storage);
            }

            return INTEGER_RETURN_REGISTERS_NAMES[(int) storage.getStorageIndex()];

        case VECTOR_RETURN_REGISTER:
            if (storage.getStorageIndex() > Constants.MAX_VECTOR_RETURN_REGISTERS) {
                throw new IllegalArgumentException("Illegal storage: " + storage);
            }
            return getVectorRegisterName(storage.getStorageIndex(), storage.getSize());

        case STACK_ARGUMENT_SLOT: return "[sp + " + Long.toHexString(8 * storage.getStorageIndex()) + "]";
        }

        throw new IllegalArgumentException("Unhandled storage type: " + storage.getStorageClass());
    }
}
