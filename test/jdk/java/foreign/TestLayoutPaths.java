/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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
 *   Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 *
 */

/*
 * @test
 * @run testng TestLayoutPaths
 */

import jdk.incubator.foreign.CompoundLayout;
import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.PaddingLayout;
import jdk.incubator.foreign.SequenceLayout;
import jdk.incubator.foreign.ValueLayout;

import org.testng.annotations.*;
import static org.testng.Assert.*;

public class TestLayoutPaths {

    @Test(expectedExceptions = IllegalStateException.class)
    public void testBadSelectFromSeq() {
        SequenceLayout seq = SequenceLayout.of(ValueLayout.ofSignedInt(32));
        seq.offset(path -> path.groupElement("foo"));
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testBadSelectFromStruct() {
        GroupLayout g = GroupLayout.ofStruct(ValueLayout.ofSignedInt(32));
        g.offset(path -> path.sequenceElement());
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testBadSelectFromValue() {
        SequenceLayout seq = SequenceLayout.of(ValueLayout.ofSignedInt(32));
        seq.offset(path -> path.sequenceElement().sequenceElement());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testUnknownStructField() {
        GroupLayout g = GroupLayout.ofStruct(ValueLayout.ofSignedInt(32));
        g.offset(path -> path.groupElement("foo"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testOutOfBoundsSeqIndex() {
        SequenceLayout seq = SequenceLayout.of(5, ValueLayout.ofSignedInt(32));
        seq.offset(path -> path.sequenceElement(6));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNegativeSeqIndex() {
        SequenceLayout seq = SequenceLayout.of(5, ValueLayout.ofSignedInt(32));
        seq.offset(path -> path.sequenceElement(-2));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testIncompleteAccess() {
        SequenceLayout seq = SequenceLayout.of(5, GroupLayout.ofStruct(ValueLayout.ofSignedInt(32)));
        seq.dereferenceHandle(int.class, CompoundLayout.Path::sequenceElement);
    }

    @Test
    public void testBadContainerAlign() {
        GroupLayout g = GroupLayout.ofStruct(ValueLayout.ofSignedInt(32).alignTo(16).withName("foo")).alignTo(8);
        try {
            g.offset(path -> path.groupElement("foo"));
        } catch (Throwable ex) {
            throw new AssertionError(ex); // should be ok!
        }
        try {
            g.dereferenceHandle(int.class, path -> path.groupElement("foo")); //ok
            assertTrue(false); //should fail!
        } catch (UnsupportedOperationException ex) {
            //ok
        } catch (Throwable ex) {
            throw new AssertionError(ex); //should fail!
        }
    }

    @Test
    public void testBadAlignOffset() {
        GroupLayout g = GroupLayout.ofStruct(PaddingLayout.of(8), ValueLayout.ofSignedInt(32).alignTo(16).withName("foo"));
        try {
            g.offset(path -> path.groupElement("foo"));
        } catch (Throwable ex) {
            throw new AssertionError(ex); // should be ok!
        }
        try {
            g.dereferenceHandle(int.class, path -> path.groupElement("foo")); //ok
            assertTrue(false); //should fail!
        } catch (UnsupportedOperationException ex) {
            //ok
        } catch (Throwable ex) {
            throw new AssertionError(ex); //should fail!
        }
    }
}

