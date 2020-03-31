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
 * @run testng/othervm -Djdk.incubator.foreign.Foreign=permit TestSharedAccess
 */

import jdk.incubator.foreign.Foreign;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.MemoryLayouts;
import jdk.incubator.foreign.SequenceLayout;
import org.testng.annotations.*;

import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.*;

public class TestSharedAccess {

    static final VarHandle intHandle = MemoryLayouts.JAVA_INT.varHandle(int.class);

    @Test
    public void testShared() throws Throwable {
        SequenceLayout layout = MemoryLayout.ofSequence(1024, MemoryLayouts.JAVA_INT);
        try (MemorySegment s = MemorySegment.allocateNative(layout)) {
            for (int i = 0 ; i < layout.elementCount().getAsLong() ; i++) {
                setInt(s.baseAddress().addOffset(i * 4), 42);
            }
            List<Thread> threads = new ArrayList<>();
            List<Spliterator<MemorySegment>> spliterators = new ArrayList<>();
            spliterators.add(s.spliterator(layout));
            while (true) {
                boolean progress = false;
                List<Spliterator<MemorySegment>> newSpliterators = new ArrayList<>();
                for (Spliterator<MemorySegment> spliterator : spliterators) {
                    Spliterator<MemorySegment> sub = spliterator.trySplit();
                    if (sub != null) {
                        progress = true;
                        newSpliterators.add(sub);
                    }
                }
                spliterators.addAll(newSpliterators);
                if (!progress) break;
            }

            AtomicInteger accessCount = new AtomicInteger();
            for (Spliterator<MemorySegment> spliterator : spliterators) {
                threads.add(new Thread(() -> {
                    spliterator.tryAdvance(local -> {
                        assertEquals(getInt(local.baseAddress()), 42);
                        accessCount.incrementAndGet();
                    });
                }));
            }
            threads.forEach(Thread::start);
            threads.forEach(t -> {
                try {
                    t.join();
                } catch (Throwable e) {
                    throw new IllegalStateException(e);
                }
            });
            assertEquals(accessCount.get(), 1024);
        }
    }

    @Test
    public void testSharedUnsafe() throws Throwable {
        try (MemorySegment s = MemorySegment.allocateNative(4)) {
            setInt(s.baseAddress(), 42);
            assertEquals(getInt(s.baseAddress()), 42);
            List<Thread> threads = new ArrayList<>();
            MemorySegment sharedSegment = Foreign.getInstance().asUnconfined(s);
            for (int i = 0 ; i < 1000 ; i++) {
                threads.add(new Thread(() -> {
                    assertEquals(getInt(sharedSegment.baseAddress()), 42);
                }));
            }
            threads.forEach(Thread::start);
            threads.forEach(t -> {
                try {
                    t.join();
                } catch (Throwable e) {
                    throw new IllegalStateException(e);
                }
            });
        }
    }


    @Test(expectedExceptions=IllegalStateException.class)
    public void testBadCloseWithPendingAcquire() throws InterruptedException {
        try (MemorySegment segment = MemorySegment.allocateNative(16)) {
            Spliterator<MemorySegment> spliterator = segment.spliterator(MemoryLayout.ofSequence(16, MemoryLayouts.JAVA_BYTE));
            Runnable r = () -> spliterator.forEachRemaining(s -> {
                try {
                    Thread.sleep(5000 * 100);
                } catch (InterruptedException ex) {
                    throw new AssertionError(ex);
                }
            });
            new Thread(r).start();
            Thread.sleep(5000);
        } //should fail here!
    }

    static int getInt(MemoryAddress address) {
        return (int)intHandle.getVolatile(address);
    }

    static void setInt(MemoryAddress address, int value) {
        intHandle.setVolatile(address, value);
    }
}
