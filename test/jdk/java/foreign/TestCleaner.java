/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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

/*
 * @test
 * @modules java.base/jdk.internal.ref
 *          jdk.incubator.foreign/jdk.incubator.foreign
 * @run testng TestCleaner
 */

import jdk.incubator.foreign.MemorySegment;
import java.lang.ref.Cleaner;
import jdk.internal.ref.CleanerFactory;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

public class TestCleaner {

    static class SegmentState {
        private AtomicInteger cleanupCalls = new AtomicInteger(0);

        void cleanup() {
            cleanupCalls.incrementAndGet();
        }

        int cleanupCalls() {
            return cleanupCalls.get();
        }
    }

    @Test(dataProvider = "cleaners")
    public void test(Supplier<Cleaner> cleanerFactory, SegmentFunction segmentFunction) {
        SegmentState segmentState = new SegmentState();
        MemorySegment segment = MemorySegment.allocateNative(10)
                .handoff(r -> r.addCleanupAction(segmentState::cleanup));
        // register cleaners before
        segment = segment.handoff(r -> r.registerCleaner(cleanerFactory.get()));
        segment = segmentFunction.apply(segment);
        if (segment.isAlive()) {
            // also register cleaners after
            segment = segment.handoff(r -> r.registerCleaner(cleanerFactory.get()));
        }
        //check that cleanup has not been called by any cleaner yet!
        assertEquals(segmentState.cleanupCalls(), segment.isAlive() ? 0 : 1);
        segment = null;
        while (segmentState.cleanupCalls() == 0) {
            byte[] b = new byte[100];
            System.gc();
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                throw new AssertionError(ex);
            }
        }
        assertEquals(segmentState.cleanupCalls(), 1);
    }

    enum SegmentFunction implements Function<MemorySegment, MemorySegment> {
        IDENTITY(Function.identity()),
        CLOSE(s -> { s.close(); return s; }),
        SHARE(s -> { return s.handoff(MemorySegment.HandoffTransform::removeOwnerThread); });

        private final Function<MemorySegment, MemorySegment> segmentFunction;

        SegmentFunction(Function<MemorySegment, MemorySegment> segmentFunction) {
            this.segmentFunction = segmentFunction;
        }

        @Override
        public MemorySegment apply(MemorySegment segment) {
            return segmentFunction.apply(segment);
        }
    }

    @DataProvider
    static Object[][] cleaners() {
        Supplier<?>[] cleaners = {
                (Supplier<Cleaner>)Cleaner::create,
                (Supplier<Cleaner>)CleanerFactory::cleaner
        };

        SegmentFunction[] segmentFunctions = SegmentFunction.values();
        Object[][] data = new Object[cleaners.length * segmentFunctions.length][3];

        for (int cleaner = 0 ; cleaner < cleaners.length ; cleaner++) {
            for (int segmentFunction = 0 ; segmentFunction < segmentFunctions.length ; segmentFunction++) {
                data[cleaner + (cleaners.length * segmentFunction)] =
                        new Object[] { cleaners[cleaner], segmentFunctions[segmentFunction] };
            }
        }

        return data;
    }
}
