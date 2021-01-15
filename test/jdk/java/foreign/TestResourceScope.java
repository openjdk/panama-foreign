/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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

/*
 * @test
 * @modules java.base/jdk.internal.ref
 *          jdk.incubator.foreign/jdk.incubator.foreign
 * @run testng/othervm -Dforeign.restricted=permit TestResourceScope
 */

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import java.lang.ref.Cleaner;

import jdk.incubator.foreign.ResourceScope;
import jdk.internal.ref.CleanerFactory;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class TestResourceScope {

    final static int N_THREADS = 1000;

    static MemorySegment makeSegment(ResourceScope scope, Runnable cleanupAction) {
        return MemoryAddress.NULL.asSegmentRestricted(1, cleanupAction, scope);
    }

    @Test(dataProvider = "cleaners")
    public void testConfined(Supplier<Cleaner> cleanerSupplier) {
        AtomicInteger acc = new AtomicInteger();
        Cleaner cleaner = cleanerSupplier.get();
        ResourceScope scope = ResourceScope.ofConfined(cleaner);
        for (int i = 0 ; i < N_THREADS ; i++) {
            int delta = i;
            makeSegment(scope, () -> acc.addAndGet(delta));
        }
        assertEquals(acc.get(), 0);

        if (cleaner == null) {
            scope.close();
            assertEquals(acc.get(), IntStream.range(0, N_THREADS).sum());
        } else {
            scope = null;
            int expected = IntStream.range(0, N_THREADS).sum();
            while (acc.get() != expected) {
                kickGC();
            }
        }
    }

//    @Test(dataProvider = "cleaners")
//    public void testSharedSingleThread(Supplier<Cleaner> cleanerSupplier) {
//        AtomicInteger acc = new AtomicInteger();
//        Cleaner cleaner = cleanerSupplier.get();
//        ResourceScope scope = ResourceScope.ofShared(cleaner);
//        for (int i = 0 ; i < N_THREADS ; i++) {
//            int delta = i;
//            makeSegment(scope, () -> acc.addAndGet(delta));
//        }
//        assertEquals(acc.get(), 0);
//
//        if (cleaner == null) {
//            scope.close();
//            assertEquals(acc.get(), IntStream.range(0, N_THREADS).sum());
//        } else {
//            scope = null;
//            int expected = IntStream.range(0, N_THREADS).sum();
//            while (acc.get() != expected) {
//                kickGC();
//            }
//        }
//    }
//
//    @Test(dataProvider = "cleaners")
//    public void testSharedMultiThread(Supplier<Cleaner> cleanerSupplier) {
//        AtomicInteger acc = new AtomicInteger();
//        Cleaner cleaner = cleanerSupplier.get();
//        List<Thread> threads = new ArrayList<>();
//        ResourceScope scope = ResourceScope.ofShared(cleaner);
//        AtomicReference<ResourceScope> scopeRef = new AtomicReference<>(scope);
//        for (int i = 0 ; i < N_THREADS ; i++) {
//            int delta = i;
//            Thread thread = new Thread(() -> {
//                try {
//                    makeSegment(scopeRef.get(), () -> {
//                        acc.addAndGet(delta);
//                    });
//                } catch (IllegalStateException ex) {
//                    // already closed - ignore
//                }
//            });
//            threads.add(thread);
//        }
//        assertEquals(acc.get(), 0);
//        threads.forEach(Thread::start);
//
//        // if no cleaner, close - not all segments might have been added to the scope!
//        // if cleaner, don't unset the scope - after all, the scope is kept alive by threads
//        if (cleaner == null) {
//            scope.close();
//        }
//
//        threads.forEach(t -> {
//            try {
//                t.join();
//            } catch (InterruptedException ex) {
//                fail();
//            }
//        });
//
//        if (cleaner == null) {
//            assertEquals(acc.get(), IntStream.range(0, N_THREADS).sum());
//        } else {
//            scope = null;
//            scopeRef.set(null);
//            int expected = IntStream.range(0, N_THREADS).sum();
//            while (acc.get() != expected) {
//                kickGC();
//            }
//        }
//    }

    private void kickGC() {
        for (int i = 0 ; i < 100 ; i++) {
            byte[] b = new byte[100];
            System.gc();
            Thread.onSpinWait();
        }
    }

    @DataProvider
    static Object[][] cleaners() {
        return new Object[][] {
                { (Supplier<Cleaner>)() -> null },
                { (Supplier<Cleaner>)Cleaner::create },
                { (Supplier<Cleaner>)CleanerFactory::cleaner }
        };
    }
}
