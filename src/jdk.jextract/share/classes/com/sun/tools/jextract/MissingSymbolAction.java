/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.jextract;

import java.io.PrintWriter;
import java.util.function.BiPredicate;

// What to do on missing native library symbol?
public enum MissingSymbolAction {
    // throw error on missing symbol
    ERROR((err, name) -> { throw new RuntimeException(Main.format("err.symbol.not.found", name)); }),

    // issue warning and exclude the symbol for the output
    EXCLUDE((err, name) -> { err.println(Main.format("warn.symbol.excluded", name)); return true; }),

    // ignore and generate the symbol ("I know what I am doing")
    IGNORE((err, name) -> false);

    private final BiPredicate<PrintWriter, String> action;

    private MissingSymbolAction(BiPredicate<PrintWriter, String> action) {
        this.action = action;
    }

    // return if the symbol has to be excluded for the output
    boolean handle(PrintWriter err, String name) {
        return action.test(err, name);
    }
}
