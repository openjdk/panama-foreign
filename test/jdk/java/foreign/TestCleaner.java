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
 * @modules jdk.incubator.foreign/jdk.internal.foreign
 */

import jdk.incubator.foreign.MemorySegment;
import jdk.internal.foreign.MemorySegmentImpl;
import jdk.internal.foreign.MemorySourceImpl;

public class TestCleaner {

    boolean isDone = false;

    public static void main(String[] args) {
        new TestCleaner().test();
    }

    void test() {
        MemorySegment segment = makeSegment();
        segment.source().registerCleaner();
        for (int i = 0 ; i < 1000 ; i++) {
            segment.acquire();
        }
        segment = null;
        while (!checkDone()) {
            byte[] b = new byte[100];
            System.gc();
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                throw new AssertionError(ex);
            }
        }
    }

    MemorySegment makeSegment() {
        MemorySourceImpl memorySource = new MemorySourceImpl.OfHeap(0, null, null, this::done);
        return new MemorySegmentImpl(0, 0, Thread.currentThread(), memorySource.acquire());
    }

    synchronized void done() {
        isDone = true;
    }

    synchronized boolean checkDone() {
        return isDone;
    }
}
