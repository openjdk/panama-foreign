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
 * @modules jdk.incubator.foreign java.base/jdk.internal.vm.annotation java.base/jdk.internal.misc
 * @run testng/othervm TestHandshake
 * @run testng/othervm -Xint TestHandshake
 * @run testng/othervm -XX:TieredStopAtLevel=1 TestHandshake
 */

import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemorySegment;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class TestHandshake {

    static final int ITERATIONS = 5;

    @Test(dataProvider = "accessors")
    public void testHandshake(Function<MemorySegment, Runnable> accessorFactory) throws InterruptedException {
        for (int it = 0 ; it < ITERATIONS ; it++) {
            MemorySegment segment = MemorySegment.allocateNative(1_000_000).share();
            System.err.println("ITERATION " + it);
            List<Thread> accessors = new ArrayList<>();
            for (int i = 0; i < ThreadLocalRandom.current().nextInt(Runtime.getRuntime().availableProcessors()); i++) {
                Thread access = new Thread(accessorFactory.apply(segment));
                access.start();
                accessors.add(access);
            }
            Thread t2 = new Thread(new Handshaker(segment));
            t2.start();
            t2.join();
            accessors.forEach(t -> {
                try {
                    t.join();
                } catch (InterruptedException ex) {
                    // do nothing
                }
            });
            assertTrue(!segment.isAlive());
        }
    }

    static class SegmentAccessor implements Runnable {

        final MemorySegment segment;

        SegmentAccessor(MemorySegment segment) {
            this.segment = segment;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    int sum = 0;
                    for (int i = 0; i < segment.byteSize(); i++) {
                        sum += MemoryAccess.getByteAtIndex(segment, i);
                    }
                }
            } catch (IllegalStateException ex) {
                // do nothing
            }
        }
    }

    static class SegmentCopyAccessor implements Runnable {

        final MemorySegment segment;

        SegmentCopyAccessor(MemorySegment segment) {
            this.segment = segment;
        }

        @Override
        public void run() {
            try {
                long split = segment.byteSize() / 2;
                MemorySegment first = segment.asSlice(0, split);
                MemorySegment second = segment.asSlice(split);
                while (true) {
                    for (int i = 0; i < segment.byteSize(); i++) {
                        first.copyFrom(second);
                    }
                }
            } catch (IllegalStateException ex) {
                // do nothing
            }
        }
    }

    static class BufferAccessor implements Runnable {

        final ByteBuffer bb;

        BufferAccessor(MemorySegment segment) {
            this.bb = segment.asByteBuffer();
        }

        @Override
        public void run() {
            try {
                while (true) {
                    int sum = 0;
                    for (int i = 0; i < bb.capacity(); i++) {
                        sum += bb.get(i);
                    }
                }
            } catch (IllegalStateException ex) {
                // do nothing
            }
        }
    }

    static class BufferHandleAccessor implements Runnable {

        static VarHandle handle = MethodHandles.byteBufferViewVarHandle(short[].class, ByteOrder.nativeOrder());

        final ByteBuffer bb;

        public BufferHandleAccessor(MemorySegment segment) {
            this.bb = segment.asByteBuffer();
        }

        @Override
        public void run() {
            try {
                while (true) {
                    int sum = 0;
                    for (int i = 0; i < bb.capacity() / 2; i++) {
                        sum += (short)handle.get(bb, i);
                    }
                }
            } catch (IllegalStateException ex) {
                // do nothing
            }
        }
    };

    static class Handshaker implements Runnable {

        final MemorySegment segment;

        Handshaker(MemorySegment segment) {
            this.segment = segment;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(2000));
                long prev = System.currentTimeMillis();
                segment.close();
                long delay = System.currentTimeMillis() - prev;
                System.out.println("Segment closed - delay (ms): " + delay);
            } catch (InterruptedException ex) {
                // do nothing
            }
        }
    }

    @DataProvider
    static Object[][] accessors() {
        return new Object[][] {
                { (Function<MemorySegment, Runnable>)SegmentAccessor::new },
                { (Function<MemorySegment, Runnable>)SegmentCopyAccessor::new },
                { (Function<MemorySegment, Runnable>)BufferAccessor::new },
                { (Function<MemorySegment, Runnable>)BufferHandleAccessor::new }
        };
    }
}
