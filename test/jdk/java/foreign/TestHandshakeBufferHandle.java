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
 * @run main/othervm TestHandshakeBufferHandle
 * @run main/othervm -Xint TestHandshakeBufferHandle
 * @run main/othervm -XX:TieredStopAtLevel=1 TestHandshakeBufferHandle
 */

import jdk.incubator.foreign.MemorySegment;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TestHandshakeBufferHandle {

    static final int ITERATIONS = 10;

    static MemorySegment segment;
    static ByteBuffer buffer;

    public static void main(String[] args) throws Exception {
        for (int it = 0 ; it < ITERATIONS ; it++) {
            segment = MemorySegment.allocateNative(2_000_000).share();
            buffer = segment.asByteBuffer();
            System.err.println("ITERATION " + it);
            List<Thread> accessors = new ArrayList<>();
            for (int i = 0; i < ThreadLocalRandom.current().nextInt(Runtime.getRuntime().availableProcessors()); i++) {
                Thread access = new Thread(memoryAccess);
                access.start();
                accessors.add(access);
            }
            Thread t2 = new Thread(handshake);
            t2.start();
            t2.join();
            accessors.forEach(t -> {
                try {
                    t.join();
                } catch (InterruptedException ex) {
                    // do nothing
                }
            });
        }
    }

    static class Accessor implements Runnable {

        static VarHandle handle = MethodHandles.byteBufferViewVarHandle(short[].class, ByteOrder.nativeOrder());

        @Override
        public void run() {
            try {
                while (true) {
                    int sum = 0;
                    for (int i = 0; i < segment.byteSize() / 2; i++) {
                        sum += (short)handle.get(buffer, i);
                    }
                }
            } catch (IllegalStateException ex) {
                // do nothing
            }
        }
    };

    static Accessor memoryAccess = new Accessor();

    static class Handshaker implements Runnable {
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

    static Handshaker handshake = new Handshaker();
}
