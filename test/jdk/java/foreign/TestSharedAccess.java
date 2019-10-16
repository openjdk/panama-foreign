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
 * @run testng TestSharedAccess
 */

import jdk.incubator.foreign.MemoryLayouts;
import jdk.incubator.foreign.MemorySegment;
import org.testng.annotations.*;

import java.lang.invoke.VarHandle;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.testng.Assert.*;

public class TestSharedAccess {

    static final VarHandle intHandle = MemoryLayouts.JAVA_INT.varHandle(int.class);

    @Test
    public void testConfined() throws Throwable {
        Thread owner = Thread.currentThread();
        MemorySegment s = MemorySegment.ofNative(4);
        AtomicReference<MemorySegment> confined = new AtomicReference<>(s);
        setInt(s, 42);
        assertEquals(getInt(s), 42);
        List<Thread> threads = new ArrayList<>();
        for (int i = 0 ; i < 100000 ; i++) {
            threads.add(new Thread(() -> {
                assertEquals(getInt(confined.get()), 42);
                confined.set(confined.get().asConfined(owner));
            }));
        }
        threads.forEach(t -> {
            confined.set(confined.get().asConfined(t));
            t.start();
            try {
                t.join();
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        });
        confined.get().close();
    }

    @Test
    public void testShared() throws Throwable {
        MemorySegment seg = MemorySegment.ofNative(4);
        WeakReference<MemorySegment> wr = null;
        {
            MemorySegment s = seg.asShared();
            wr = new WeakReference<>(s);
            setInt(s, 42);
            assertEquals(getInt(s), 42);
            List<Thread> threads = new ArrayList<>();
                for (int i = 0 ; i < 100000 ; i++) {
                    threads.add(new Thread(() -> {
                        assertEquals(getInt(s), 42);
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
        seg = null;
        while (wr.get() != null) {
            System.gc();
            Thread.sleep(100);
        }
        // make sure we terminate :-)
    }

    @Test
    public void testBadSharedToConfined() throws Throwable {
        MemorySegment segment = MemorySegment.ofNative(4);
        assertTrue(segment.isAccessible());
        assertTrue(segment.isConfined(Thread.currentThread()));
        assertFalse(segment.isShared());
        MemorySegment shared = segment.asShared();
        assertTrue(shared.isAccessible());
        assertTrue(shared.isShared());
        try {
            shared.asConfined(Thread.currentThread());
            assertTrue(false); //should not get here
        } catch (IllegalStateException ise) {
            assertTrue(true);
        } catch (Throwable t) {
            assertTrue(false); //should not get here
        }
    }

    @Test
    public void testBadSharedToShared() throws Throwable {
        MemorySegment segment = MemorySegment.ofNative(4);
        assertTrue(segment.isAccessible());
        assertTrue(segment.isConfined(Thread.currentThread()));
        assertFalse(segment.isShared());
        MemorySegment shared = segment.asShared();
        assertTrue(shared.isAccessible());
        assertTrue(shared.isShared());
        try {
            shared.asShared();
            assertTrue(false); //should not get here
        } catch (IllegalStateException ise) {
            assertTrue(true);
        } catch (Throwable t) {
            assertTrue(false); //should not get here
        }
    }

    @Test
    public void testBadConfinedToConfined() throws Throwable {
        try (MemorySegment segment = MemorySegment.ofNative(4)) {
            assertTrue(segment.isAccessible());
            assertTrue(segment.isConfined(Thread.currentThread()));
            assertFalse(segment.isShared());
            try {
                segment.asConfined(Thread.currentThread());
                assertTrue(false); //should not get here
            } catch (IllegalArgumentException ise) {
                assertTrue(true);
            } catch (Throwable t) {
                assertTrue(false); //should not get here
            }
        }
    }

    static int getInt(MemorySegment handle) {
        return (int)intHandle.getVolatile(handle.baseAddress());
    }

    static void setInt(MemorySegment handle, int value) {
        intHandle.setVolatile(handle.baseAddress(), value);
    }
}
