/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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
package java.foreign;

import java.util.Optional;
import java.util.OptionalLong;

/**
 * An unresolved layout acts as a placeholder for another layout. Unresolved layouts can be resolved, which yields
 * a new layout whose size is known. The resolution process is typically driven by the annotations attached to the
 * unresolved layout.
 */
public class Unresolved extends AbstractLayout<Unresolved> implements Layout {
    private final String layoutExpression;

    Unresolved(String layoutExpression, Optional<String> name) {
        super(OptionalLong.empty(), name);
        this.layoutExpression = layoutExpression;
    }

     /**
      * Create a new unresolved layout from given layout expression.
      * @param layoutExpression the layout expression.
      * @return the new unresolved layout.
      */
    public static Unresolved of(String layoutExpression) {
        return new Unresolved(layoutExpression, Optional.empty());
    }

    /**
     * The layout expression associated with this unresolved layout.
     * @return The layout expression associated with this unresolved layout.
     */
    public String layoutExpression() {
        return layoutExpression;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!super.equals(other)) {
            return false;
        }
        if (!(other instanceof Unresolved)) {
            return false;
        }
        return layoutExpression.equals(((Unresolved)other).layoutExpression);
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ layoutExpression.hashCode();
    }

    @Override
    public long bitsSize() {
        throw new UnsupportedOperationException("bitsSize on Unresolved");
    }

    @Override
    long naturalAlignmentBits() {
        throw new UnsupportedOperationException("alignmentBitCount on Unresolved");
    }

    @Override
    public boolean isPartial() {
        return true;
    }

    @Override
    public String toString() {
        return decorateLayoutString(String.format("${%s}", layoutExpression));
    }

    @Override
    Unresolved dup(OptionalLong alignment, Optional<String> name) {
        if (alignment.isPresent()) {
            throw new UnsupportedOperationException("alignTo on Unresolved");
        }
        return new Unresolved(layoutExpression, name);
    }
}
