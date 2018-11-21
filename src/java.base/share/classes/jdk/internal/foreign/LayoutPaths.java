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

package jdk.internal.foreign;

import java.foreign.layout.Group;
import java.foreign.layout.Layout;
import java.foreign.layout.Value;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class LayoutPaths {

    public static class LayoutPath {

        final Layout layout;
        final LayoutPath enclosing;
        final LongSupplier offsetFunc;

        LayoutPath(Layout layout, LayoutPath enclosing, LongSupplier offsetFunc) {
            this.layout = layout;
            this.enclosing = enclosing;
            this.offsetFunc = offsetFunc;
        }

        public Layout layout() {
            return layout;
        }

        public LayoutPath enclosing() {
            return enclosing;
        }

        public long offset() {
            return offsetFunc.getAsLong();
        }

        public LayoutPath dup(LayoutPath prev) {
            return of(layout(), prev, () -> prev.offset() + offset());
        }
    }

    public static LayoutPath of(Layout layout, LayoutPath prev, LongSupplier offsetFunc) {
        return new LayoutPath(layout, prev, offsetFunc);
    }

    public static LayoutPath of(Layout layout) {
        return of(layout, ROOT, () -> 0L);
    }

    public static Stream<LayoutPath> lookup(Layout layout, Predicate<? super Layout> condition) {
        if (layout instanceof Group) {
            return lookupGroup((Group)layout, condition);
        } else if (layout instanceof Value) {
            return lookupContents((Value) layout, condition);
        } else {
            return Stream.empty();
        }
    }

    private static Stream<LayoutPath> lookupGroup(Group group, Predicate<? super Layout> condition) {
        LayoutPath prev = of(group);
        LongSupplier offset = prev::offset;
        Stream<LayoutPath> result = Stream.empty();
        for (Layout l : group.elements()) {
            LayoutPath path = of(l, prev, offset);
            if (condition.test(l)) {
                result = Stream.concat(result, Stream.of(path));
            }
            result = Stream.concat(result,
                    lookup(l, condition).map(p -> p.dup(path)));
            LongSupplier offsetPrev = offset;
            offset = () -> offsetPrev.getAsLong() + l.bitsSize();
        }
        return result;
    }

    private static Stream<LayoutPath> lookupContents(Value value, Predicate<? super Layout> condition) {
        LayoutPath thisPath = of(value);
        return value.contents().isPresent() ?
                lookup(value.contents().get(), condition).map(p -> p.dup(thisPath)) :
                Stream.empty();
    }

    public static LayoutPath ROOT = of(null, null, () -> 0L);
}
