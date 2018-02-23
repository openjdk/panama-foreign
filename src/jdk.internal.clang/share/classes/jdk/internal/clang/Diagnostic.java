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
package jdk.internal.clang;

public class Diagnostic {
    final long ptr;

    // Various Diagnostic severity levels - from Clang enum CXDiagnosticSeverity

    /**
     * A diagnostic that has been suppressed, e.g., by a command-line
     * option.
     */
    public static final int CXDiagnostic_Ignored = 0;

    /**
     * This diagnostic is a note that should be attached to the
     * previous (non-note) diagnostic.
     */
    public static final int CXDiagnostic_Note    = 1;

    /**
     * This diagnostic indicates suspicious code that may not be
     * wrong.
     */
    public static final int CXDiagnostic_Warning = 2;

    /**
     * This diagnostic indicates that the code is ill-formed.
     */
    public static final int CXDiagnostic_Error   = 3;

    /**
     * This diagnostic indicates that the code is ill-formed such
     * that future parser recovery is unlikely to produce useful
     * results.
     */
    public static final int CXDiagnostic_Fatal   = 4;

    Diagnostic(long ptr) {
        this.ptr = ptr;
    }

    native int severity(long addr);
    public int severity() { return severity(ptr); }

    native SourceLocation location(long addr);
    public SourceLocation location() { return location(ptr); }

    native String spelling(long addr);
    public String spelling() { return spelling(ptr); }

    native String format(long addr);

    native void dispose(long addr);
    public void dispose() { dispose(ptr); }

    @Override
    public String toString() {
        return format(ptr);
    }
}
