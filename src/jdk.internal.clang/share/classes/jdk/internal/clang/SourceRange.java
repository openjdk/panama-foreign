/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
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

import java.nio.ByteBuffer;
import java.util.Objects;

public class SourceRange extends StructType {

    SourceRange(ByteBuffer buf) {
        super(buf);
    }

    protected SourceRange(ByteBuffer buf, boolean copy) {
        super(buf, copy);
    }

    public native SourceLocation getBegin();
    public native SourceLocation getEnd();

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(this instanceof SourceRange)) {
            return false;
        }
        SourceRange sr = (SourceRange)other;
        return Objects.equals(getBegin(), sr.getBegin()) &&
            Objects.equals(getEnd(), sr.getEnd());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getBegin()) ^ Objects.hashCode(getEnd());
    }
}
