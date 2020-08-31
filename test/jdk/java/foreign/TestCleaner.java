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
 * @run testng/othervm -Dforeign.restricted=permit TestCleaner
 */

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import java.lang.ref.Cleaner;
import jdk.internal.ref.CleanerFactory;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;
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
    public void test(int n, Supplier<Cleaner> cleanerFactory) {
        SegmentState segmentState = new SegmentState();
        MemorySegment segment = makeSegment(segmentState);
        for (int i = 0 ; i < n ; i++) {
            segment.registerCleaner(cleanerFactory.get());
        }
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

    MemorySegment makeSegment(SegmentState segmentState) {
        return MemorySegment.ofNativeRestricted(MemoryAddress.NULL, 10, null, segmentState::cleanup, null);
    }

    @DataProvider
    static Object[][] cleaners() {
        Supplier<Cleaner> CLEANER = Cleaner::create;
        Supplier<Cleaner> CLEANER_FACTORY = CleanerFactory::cleaner;

        return new Object[][]{
                { 1, CLEANER },
                { 2, CLEANER },
                { 4, CLEANER },
                { 8, CLEANER },
                { 16, CLEANER },
                { 1, CLEANER_FACTORY },
                { 2, CLEANER_FACTORY },
                { 4, CLEANER_FACTORY },
                { 8, CLEANER_FACTORY },
                { 16, CLEANER_FACTORY },
        };
    }
}
