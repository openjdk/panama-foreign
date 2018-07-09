/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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
package java.foreign;

import java.foreign.memory.Pointer;

/**
 * A native library. Native libraries expose lookup operation for searching symbols, see {@link Library#lookup(String)}.
 */
public interface Library {

    /**
     * A native library symbol.
     */
    interface Symbol {
        /**
         * The library symbol's name.
         * @return the library symbol's name.
         */
        String getName();

        /**
         * The library symbol's address.
         * @return the library symbol's address.
         */
        Pointer<?> getAddress();
    }

    /**
     * Lookups a symbol with given name in this library.
     * @param name the symbol name.
     * @return the library symbol (if any)
     * @throws NoSuchMethodException if no symbol with given name could be found.
     */
    Symbol lookup(String name) throws NoSuchMethodException;
}
