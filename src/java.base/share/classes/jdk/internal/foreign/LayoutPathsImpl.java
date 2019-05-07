/*
 *  Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.
 *
 *  This code is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  version 2 for more details (a copy is included in the LICENSE file that
 *  accompanied this code).
 *
 *  You should have received a copy of the GNU General Public License version
 *  2 along with this work; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *  Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 */

package jdk.internal.foreign;

import jdk.internal.access.JavaLangInvokeAccess;
import jdk.internal.access.SharedSecrets;

import java.foreign.layout.Group;
import java.foreign.layout.Layout;
import java.foreign.layout.LayoutPath;
import java.foreign.layout.Sequence;
import java.foreign.layout.Value;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class LayoutPathsImpl {

    private static JavaLangInvokeAccess JLI = SharedSecrets.getJavaLangInvokeAccess();

    public final static class LayoutPathImpl implements LayoutPath {

        final Layout layout;
        final LayoutPathImpl enclosing;
        final LongSupplier offsetFunc;

        LayoutPathImpl(Layout layout, LayoutPathImpl enclosing, LongSupplier offsetFunc) {
            this.layout = layout;
            this.enclosing = enclosing;
            this.offsetFunc = offsetFunc;
        }

        @Override
        public Layout layout() {
            return layout;
        }

        @Override
        public LayoutPathImpl enclosing() {
            return enclosing;
        }

        @Override
        public long offset() {
            return offsetFunc.getAsLong();
        }

        LayoutPath dup(LayoutPath prev) {
            return LayoutPathsImpl.of(layout(), prev, () -> prev.offset() + offset());
        }

        @Override
        public VarHandle dereferenceHandle(Class<?> carrier) {
            return JLI.memoryAddressViewVarHandle(carrier, this);
        }

        public final List<Sequence> enclosingSequences() {
            List<Sequence> enclSeqs = new ArrayList<>();
            enclosingSequences(enclSeqs);
            Collections.reverse(enclSeqs);
            return enclSeqs;
        }

        void enclosingSequences(List<Sequence> enclSeqs) {
            if (layout instanceof Sequence) {
                enclSeqs.add((Sequence)layout);
            }
            if (enclosing != ROOT) {
                enclosing.enclosingSequences(enclSeqs);
            }
        }

        @Override
        public Stream<LayoutPath> lookup(Predicate<? super Layout> condition) {
            return LayoutPathsImpl.lookup(layout(), condition);
        }
    }

    static LayoutPath of(Layout layout, LayoutPath prev, LongSupplier offsetFunc) {
        return new LayoutPathImpl(layout, (LayoutPathImpl)prev, offsetFunc);
    }

    public static LayoutPath of(Layout layout) {
        return of(layout, ROOT, () -> 0L);
    }

    /**
     * Lookup a layout path with given matching predicate.
     * @param layout the starting layout.
     * @param condition the predicate describing matching layout subelements.
     * @return a stream of matching layout subelements.
     */
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
        LayoutPath thisPath = of(group);
        Stream<LayoutPath> result = condition.test(group) ?
            Stream.of(thisPath) : Stream.empty();
        for (Layout l : group.elements()) {
            LayoutPath path = of(l, prev, offset);
            if (condition.test(l)) {
                result = Stream.concat(result, Stream.of(path));
            }
            result = Stream.concat(result,
                    lookup(l, condition).map(p -> ((LayoutPathImpl)p).dup(path)));
            if (group.kind() != Group.Kind.UNION) {
                LongSupplier offsetPrev = offset;
                offset = () -> offsetPrev.getAsLong() + l.bitsSize();
            }
        }
        return result;
    }

    private static Stream<LayoutPath> lookupContents(Value value, Predicate<? super Layout> condition) {
        LayoutPath thisPath = of(value);
        Stream<LayoutPath> paths = condition.test(value) ?
            Stream.of(thisPath) : Stream.empty();
        return Stream.concat(paths, value.contents().isPresent() ?
                lookup(value.contents().get(), condition).map(p -> ((LayoutPathImpl)p).dup(thisPath)) :
                Stream.empty());
    }

    public static LayoutPath ROOT = of(null, null, () -> 0L);
}
