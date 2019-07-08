/*
 *  Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.  Oracle designates this
 *  particular file as subject to the "Classpath" exception as provided
 *  by Oracle in the LICENSE file that accompanied this code.
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
 *   Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 *
 */
package jdk.internal.foreign;

import jdk.internal.access.JavaLangInvokeAccess;
import jdk.internal.access.SharedSecrets;
import sun.invoke.util.Wrapper;

import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.Layout;
import jdk.incubator.foreign.SequenceLayout;
import jdk.incubator.foreign.ValueLayout;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class LayoutPathImpl {

    private static JavaLangInvokeAccess JLI = SharedSecrets.getJavaLangInvokeAccess();

    private final Layout layout;
    private final long offset;
    private final LayoutPathImpl enclosing;
    private final List<Long> scales;

    private LayoutPathImpl(Layout layout, long offset, List<Long> scales, LayoutPathImpl enclosing) {
        this.layout = layout;
        this.offset = offset;
        this.scales = scales;
        this.enclosing = enclosing;
    }

    public long offset() {
        return offset;
    }

    public LayoutPathImpl sequenceElement() throws UnsupportedOperationException {
        check(SequenceLayout.class);
        SequenceLayout seq = (SequenceLayout)layout;
        Layout elem = seq.elementLayout();
        List<Long> newScales = new ArrayList<>(scales);
        newScales.add(elem.bitsSize());
        return LayoutPathImpl.nestedPath(elem, offset, newScales, this);
    }

    public LayoutPathImpl sequenceElement(long index) throws IllegalArgumentException, UnsupportedOperationException {
        check(SequenceLayout.class);
        SequenceLayout seq = (SequenceLayout)layout;
        if (seq.elementsCount().isPresent() && index >= seq.elementsCount().getAsLong()) {
            throw new IllegalArgumentException("Sequence index out of bound; found: %d, size: %d");
        }
        return LayoutPathImpl.nestedPath(seq.elementLayout(), offset, scales, this);
    }

    public LayoutPathImpl groupElement(String name) throws IllegalArgumentException, UnsupportedOperationException {
        check(GroupLayout.class);
        GroupLayout g = (GroupLayout)layout;
        long offset = 0;
        Layout elem = null;
        for (int i = 0; i < g.memberLayouts().size(); i++) {
            Layout l = g.memberLayouts().get(i);
            if (l.name().isPresent() &&
                l.name().get().equals(name)) {
                elem = l;
                break;
            } else {
                offset += l.bitsSize();
            }
        }
        if (elem == null) {
            throw new IllegalArgumentException("Cannot resolve '" + name + "' in layout " + layout);
        }
        return LayoutPathImpl.nestedPath(elem, this.offset + offset, scales, this);
    }

    void check(Class<?> layoutClass) {
        if (!layoutClass.isAssignableFrom(layout.getClass())) {
            throw new IllegalStateException("Expected layout of type: " + layoutClass.getName());
        }
    }

    public VarHandle dereferenceHandle(Class<?> carrier) {
        if (!(layout instanceof ValueLayout)) {
            throw new IllegalArgumentException("Not a value layout: " + layout);
        }

        if (!carrier.isPrimitive() || carrier == void.class || carrier == boolean.class // illegal carrier?
                || Wrapper.forPrimitiveType(carrier).bitWidth() != layout.bitsSize()) { // carrier has the right size?
            throw new IllegalArgumentException("Invalid carrier: " + carrier + ", for layout " + layout);
        }

        checkAlignment(this);

        return JLI.memoryAddressViewVarHandle(
                carrier,
                layout.bytesAlignment(),
                ((ValueLayout) layout).order(),
                Utils.bitsToBytesOrThrow(offset, IllegalStateException::new),
                scales.stream().mapToLong(s -> Utils.bitsToBytesOrThrow(s, IllegalStateException::new)).toArray());
    }

    public static LayoutPathImpl rootPath(Layout layout) {
        return new LayoutPathImpl(layout, 0L, List.of(), null);
    }

    public static LayoutPathImpl nestedPath(Layout layout, long offset, List<Long> scales, LayoutPathImpl encl) {
        return new LayoutPathImpl(layout, offset, scales, encl);
    }

    static void checkAlignment(LayoutPathImpl path) {
        Layout layout = path.layout;
        long alignment = layout.bitsAlignment();
        if (path.offset % alignment != 0) {
            throw new UnsupportedOperationException("Invalid alignment requirements for layout " + layout);
        }
        LayoutPathImpl encl = path.enclosing;
        if (encl != null) {
            if (encl.layout.bitsAlignment() < alignment) {
                throw new UnsupportedOperationException("Alignment requirements for layout " + layout + " do not match those for enclosing layout " + encl.layout);
            }
            checkAlignment(encl);
        }
    }

    public static class PathElementImpl implements Layout.PathElement, UnaryOperator<LayoutPathImpl> {

        final UnaryOperator<LayoutPathImpl> pathOp;

        public PathElementImpl(UnaryOperator<LayoutPathImpl> pathOp) {
            this.pathOp = pathOp;
        }

        @Override
        public LayoutPathImpl apply(LayoutPathImpl layoutPath) {
            return pathOp.apply(layoutPath);
        }
    }
}
