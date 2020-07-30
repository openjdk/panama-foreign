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
 * @modules jdk.incubator.foreign java.base/jdk.internal.misc
 * @run main TestHandshake
 */

import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemorySegment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TestHandshake {

    static MemorySegment segment = MemorySegment.allocateNative(1_000).share();

    public static void main(String[] args) throws Exception {
        List<Thread> accessors = new ArrayList<>();
        for (int i = 0 ; i < 100 ; i++) {
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

    static Runnable memoryAccess = () -> {
        try {
            while (segment.isAlive()) {
                int sum = 0;
                for (int i = 0; i < segment.byteSize(); i++) {
                    sum += MemoryAccess.getByteAtIndex(segment, i);
                }
            }
        } catch (IllegalStateException ex) {
            // do nothing
        }
    };

    static Runnable handshake = () -> {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(10000));
            segment.close();
        } catch (InterruptedException ex) {
            // do nothing
        }
    };
}
