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

import java.foreign.Group;
import java.foreign.Layout;
import java.foreign.LayoutPath;
import java.foreign.Sequence;
import java.foreign.Value;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.LongSupplier;

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
        public LayoutPath groupElement(String name) throws IllegalArgumentException {
            return LayoutPathsImpl.lookup(this, new ByName(name));
        }

        @Override
        public LayoutPath groupElement(long index) throws IllegalArgumentException {
            return LayoutPathsImpl.lookup(this, new ByIndex(index));
        }

        @Override
        public LayoutPath sequenceElement() throws UnsupportedOperationException {
            if (layout() instanceof Sequence) {
                return new LayoutPathImpl(((Sequence)layout()).elementLayout(), this, offsetFunc);
            } else {
                throw unsupported(layout());
            }
        }
    }

    static LayoutPath of(Layout layout, LayoutPath prev, LongSupplier offsetFunc) {
        return new LayoutPathImpl(layout, (LayoutPathImpl)prev, offsetFunc);
    }

    public static LayoutPath of(Layout layout) {
        return of(layout, ROOT, () -> 0L);
    }

    private static LayoutPath lookup(LayoutPath path, LayoutSelector selector) {
        if (path.layout() instanceof Group) {
            return lookupGroup(path, (Group)path.layout(), selector);
        } else if (path.layout() instanceof Value) {
            return lookupContents(path, selector);
        } else {
            throw unsupported(path.layout());
        }
    }

    private static LayoutPath lookupGroup(LayoutPath encl, Group group, LayoutSelector selector) {
        LongSupplier offset = encl::offset;
        long index = 0;
        for (Layout l : group) {
            if (selector.test(l, index)) {
                return of(l, encl, offset);
            }
            if (group.kind() != Group.Kind.UNION) {
                LongSupplier offsetPrev = offset;
                offset = () -> offsetPrev.getAsLong() + l.bitsSize();
            }
            index++;
        }
        throw selector.lookupError(group);
    }

    private static LayoutPath lookupContents(LayoutPath thisPath, LayoutSelector selector) {
        Value value = (Value)thisPath.layout();
        if (value.contents().isPresent() && (value.contents().get() instanceof Group)) {
            return lookupGroup(thisPath, (Group)value.contents().get(), selector);
        } else {
            throw unsupported(thisPath.layout());
        }
    }

    interface LayoutSelector {
        boolean test(Layout l, long index);
        IllegalArgumentException lookupError(Group l);
    }

    static class ByName implements LayoutSelector {

        private final String name;

        ByName(String name) {
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Empty field name");
            }
            this.name = name;
        }

        @Override
        public boolean test(Layout l, long index) {
            return l.name().isPresent() &&
                    name.equals(l.name().get());
        }

        @Override
        public IllegalArgumentException lookupError(Group l) {
            return new IllegalArgumentException(String.format("Cannot find field '%s' in layout %s", name, l));
        }
    }

    static class ByIndex implements LayoutSelector {

        private final long index;

        ByIndex(long index) {
            if (index < 0) {
                throw new IllegalArgumentException("Negative field index");
            }
            this.index = index;
        }

        @Override
        public boolean test(Layout l, long index) {
            return index == this.index;
        }

        @Override
        public IllegalArgumentException lookupError(Group l) {
            return new IllegalArgumentException(String.format("Cannot find field with index '%d' in layout %s", index, l));
        }
    }

    private static UnsupportedOperationException unsupported(Layout l) {
        return new UnsupportedOperationException("Unsupported path operation on layout " + l);
    }

    public static LayoutPath ROOT = of(null, null, () -> 0L);
}
