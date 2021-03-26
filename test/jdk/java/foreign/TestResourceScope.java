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

    final static int N_THREADS = 10000;

    @Test(dataProvider = "cleaners")
    public void testConfined(Supplier<Cleaner> cleanerSupplier) {
        AtomicInteger acc = new AtomicInteger();
        Cleaner cleaner = cleanerSupplier.get();
        ResourceScope scope = ResourceScope.ofConfined(null, cleaner);
        for (int i = 0 ; i < N_THREADS ; i++) {
            int delta = i;
            scope.addOnClose(() -> acc.addAndGet(delta));
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

    @Test(dataProvider = "cleaners")
    public void testSharedSingleThread(Supplier<Cleaner> cleanerSupplier) {
        AtomicInteger acc = new AtomicInteger();
        Cleaner cleaner = cleanerSupplier.get();
        ResourceScope scope = ResourceScope.ofShared(null, cleaner);
        for (int i = 0 ; i < N_THREADS ; i++) {
            int delta = i;
            scope.addOnClose(() -> acc.addAndGet(delta));
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

    @Test(dataProvider = "cleaners")
    public void testSharedMultiThread(Supplier<Cleaner> cleanerSupplier) {
        AtomicInteger acc = new AtomicInteger();
        Cleaner cleaner = cleanerSupplier.get();
        List<Thread> threads = new ArrayList<>();
        ResourceScope scope = ResourceScope.ofShared(null, cleaner);
        AtomicReference<ResourceScope> scopeRef = new AtomicReference<>(scope);
        for (int i = 0 ; i < N_THREADS ; i++) {
            int delta = i;
            Thread thread = new Thread(() -> {
                try {
                    scopeRef.get().addOnClose(() -> {
                        acc.addAndGet(delta);
                    });
                } catch (IllegalStateException ex) {
                    // already closed - we need to call cleanup manually
                    acc.addAndGet(delta);
                }
            });
            threads.add(thread);
        }
        assertEquals(acc.get(), 0);
        threads.forEach(Thread::start);

        // if no cleaner, close - not all segments might have been added to the scope!
        // if cleaner, don't unset the scope - after all, the scope is kept alive by threads
        if (cleaner == null) {
            scope.close();
        }

        threads.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException ex) {
                fail();
            }
        });

        if (cleaner == null) {
            assertEquals(acc.get(), IntStream.range(0, N_THREADS).sum());
        } else {
            scope = null;
            scopeRef.set(null);
            int expected = IntStream.range(0, N_THREADS).sum();
            while (acc.get() != expected) {
                kickGC();
            }
        }
    }

    @Test(dataProvider = "cleaners")
    public void testLockSingleThread(Supplier<Cleaner> cleanerSupplier) {
        Cleaner cleaner = cleanerSupplier.get();
        ResourceScope scope = ResourceScope.ofConfined(null, cleaner);
        List<ResourceScope.Handle> handles = new ArrayList<>();
        for (int i = 0 ; i < N_THREADS ; i++) {
            handles.add(scope.acquire());
        }

        while (true) {
            try {
                scope.close();
                assertEquals(handles.size(), 0);
                break;
            } catch (IllegalStateException ex) {
                assertTrue(handles.size() > 0);
                ResourceScope.Handle handle = handles.remove(0);
                handle.close();
                handle.close(); // make sure it's idempotent
                handle.close(); // make sure it's idempotent
            }
        }
    }

    @Test(dataProvider = "cleaners")
    public void testLockSharedMultiThread(Supplier<Cleaner> cleanerSupplier) {
        Cleaner cleaner = cleanerSupplier.get();
        ResourceScope scope = ResourceScope.ofShared(null, cleaner);
        for (int i = 0 ; i < N_THREADS ; i++) {
            new Thread(() -> {
                try {
                    ResourceScope.Handle handle = scope.acquire();
                    waitSomeTime();
                    handle.close();
                    handle.close(); // make sure it's idempotent
                    handle.close(); // make sure it's idempotent
                } catch (IllegalStateException ex) {
                    // might be already closed - do nothing
                }
            }).start();
        }

        while (true) {
            try {
                scope.close();
                break;
            } catch (IllegalStateException ex) {
                waitSomeTime();
            }
        }
    }

    @Test
    public void testCloseEmptyConfinedScope() {
        ResourceScope.ofConfined().close();
    }

    @Test
    public void testCloseEmptySharedScope() {
        ResourceScope.ofShared().close();
    }

    @Test
    public void testCloseConfinedLock() {
        ResourceScope.Handle handle = ResourceScope.ofConfined().acquire();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread t = new Thread(() -> {
            try {
                handle.close();
                handle.close(); // make sure it's idempotent
                handle.close(); // make sure it's idempotent
            } catch (Throwable ex) {
                failure.set(ex);
            }
        });
        t.start();
        try {
            t.join();
            assertNotNull(failure.get());
            assertEquals(failure.get().getClass(), IllegalStateException.class);
        } catch (Throwable ex) {
            throw new AssertionError(ex);
        }
    }

    private void waitSomeTime() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException ex) {
            // ignore
        }
    }

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
