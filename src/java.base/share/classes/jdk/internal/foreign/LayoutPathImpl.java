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

import java.foreign.Compound;
import java.foreign.Group;
import java.foreign.Layout;
import java.foreign.LayoutPath;
import java.foreign.Sequence;
import java.foreign.Value;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;

public class LayoutPathImpl implements LayoutPath {

    private static JavaLangInvokeAccess JLI = SharedSecrets.getJavaLangInvokeAccess();

    final Layout layout;
    final LayoutPathImpl enclosing;
    final long offset;
    final List<Sequence> enclSequences;

    LayoutPathImpl(Layout layout, LayoutPathImpl enclosing, long offset, List<Sequence> enclSequences) {
        this.layout = layout;
        this.enclosing = enclosing;
        this.offset = offset;
        this.enclSequences = enclSequences;
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
        return offset;
    }

    @Override
    public VarHandle dereferenceHandle(Class<?> carrier) {
        return JLI.memoryAddressViewVarHandle(carrier, this);
    }

    public final List<Sequence> enclosingSequences() {
        return enclSequences;
    }

    @Override
    public int dimensions() {
        return enclSequences.size();
    }

    @Override
    public LayoutPath elementPath(String name) throws IllegalArgumentException, UnsupportedOperationException {
        if (layout() instanceof Group) {
            return LayoutPathImpl.lookup(this, new ByName(name));
        } else {
            throw unsupported(layout());
        }
    }

    @Override
    public LayoutPath elementPath(long index) throws IllegalArgumentException, UnsupportedOperationException {
        if (layout() instanceof Compound) {
            return LayoutPathImpl.lookup(this, new ByIndex(index));
        } else {
            throw unsupported(layout());
        }
    }

    @Override
    public LayoutPath elementPath() throws UnsupportedOperationException {
        if (layout() instanceof Sequence) {
            List<Sequence> newEnclSequences = new ArrayList<>(enclSequences);
            newEnclSequences.add((Sequence)layout());
            return new LayoutPathImpl(((Sequence)layout()).elementLayout(), this, offset, newEnclSequences);
        } else {
            throw unsupported(layout());
        }
    }

    static LayoutPath of(Layout layout, LayoutPath prev, long offset, List<Sequence> enclSequences) {
        checkAlignmentConstraints(prev.layout(), layout, offset);
        return new LayoutPathImpl(layout, (LayoutPathImpl)prev, offset, enclSequences);
    }

    static void checkAlignmentConstraints(Layout encl, Layout current, long offset) {
        long alignment = current.alignmentBits();
        if (offset % alignment != 0) {
            throw new UnsupportedOperationException("Invalid alignment requirements for layout " + current);
        }
        if (encl != null && encl.alignmentBits() < alignment) {
            throw new UnsupportedOperationException("Alignment requirements for layout " + current + " do not match those for enclosing layout " + encl);
        }
    }

    public static LayoutPath of(Layout layout) {
        return of(layout, ROOT, 0L, List.of());
    }

    private static LayoutPath lookup(LayoutPath path, LayoutSelector selector) {
        if (path.layout() instanceof Compound) {
            return lookupCompound(path, (Compound) path.layout(), selector);
        } else {
            throw unsupported(path.layout());
        }
    }

    private static LayoutPath lookupCompound(LayoutPath encl, Compound compound, LayoutSelector selector) {
        long offset = encl.offset();
        if (compound instanceof Group) {
            Group group = (Group)compound;
            long index = 0;
            for (Layout l : group) {
                if (selector.test(l, index)) {
                    return of(l, encl, offset, ((LayoutPathImpl)encl).enclSequences);
                }
                if (group.kind() != Group.Kind.UNION) {
                    offset += l.bitsSize();
                }
                index++;
            }
        } else {
            Sequence seq = (Sequence)compound;
            long index = ((ByIndex)selector).index;
            if (index < 0 || (seq.elementsSize().isPresent() && index >= seq.elementsSize().getAsLong())) {
                throw new IllegalArgumentException("Invalid index for sequence layout: " + seq);
            } else {
                long elemOffset = seq.elementLayout().bitsSize() * index;
                return of(seq.elementLayout(), encl, offset + elemOffset, ((LayoutPathImpl) encl).enclSequences);
            }
        }
        throw selector.lookupError(compound);
    }

    interface LayoutSelector {
        boolean test(Layout l, long index);
        IllegalArgumentException lookupError(Compound l);
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
        public IllegalArgumentException lookupError(Compound l) {
            return new IllegalArgumentException(String.format("Cannot find field '%s' in layout %s", name, l));
        }
    }

    static class ByIndex implements LayoutSelector {

        private final long index;

        ByIndex(long index) {
            if (index < 0) {
                throw new IllegalArgumentException("Negative element index");
            }
            this.index = index;
        }

        @Override
        public boolean test(Layout l, long index) {
            return index == this.index;
        }

        @Override
        public IllegalArgumentException lookupError(Compound l) {
            return new IllegalArgumentException(String.format("Cannot find field with index '%d' in layout %s", index, l));
        }
    }

    private static UnsupportedOperationException unsupported(Layout l) {
        return new UnsupportedOperationException("Unsupported path operation on layout " + l);
    }

    public static LayoutPath ROOT = new LayoutPathImpl(null, null, 0L, List.of());
}
