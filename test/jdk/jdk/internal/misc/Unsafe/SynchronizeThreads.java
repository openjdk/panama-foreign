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
 * @modules java.base/jdk.internal.misc
 * @run main SynchronizeThreads
 * @summary Ensure that jdk.internal.misc.Unsafe::synchronizeThreads works.
 */

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import jdk.internal.misc.Unsafe;

public class SynchronizeThreads {
    static int state = 0;
    static int counter = 0;

    static final Unsafe UNSAFE = Unsafe.getUnsafe();
    static final long stateOffset;
    static final long counterOffset;

    static {
        try {
            stateOffset = UNSAFE.staticFieldOffset(SynchronizeThreads.class.getDeclaredField("state"));
            counterOffset = UNSAFE.staticFieldOffset(SynchronizeThreads.class.getDeclaredField("counter"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static int getState() {
        return UNSAFE.getIntOpaque(SynchronizeThreads.class, stateOffset);
    }

    static void setState(int state) {
        UNSAFE.putIntOpaque(SynchronizeThreads.class, stateOffset, state);
    }

    static int getCounter() {
        return UNSAFE.getIntOpaque(SynchronizeThreads.class, counterOffset);
    }

    static void setCounter(int state) {
        UNSAFE.putIntOpaque(SynchronizeThreads.class, counterOffset, state);
    }

    static class Worker extends Thread {
        @Override
        public void run() {
            setState(1);
            while (SynchronizeThreads.getState() != 2) {
                setCounter(getCounter() + 1);
            }
        }
    }

    // This tests that a thread that once a thread has been notified with an opaque store
    // to stop incrementing a counter, that after a thread-local handshake with all threads,
    // it really has stopped incrementing that counter, despite this being lock-free code.
    static void testAsymmetricDekkerSynchronization() {
        setState(0);
        Thread thread = new Worker();
        thread.start();
        while (getState() == 0);           // Notify Worker started
        setState(2);                       // Tell Worker to stop incrementing the counter
        UNSAFE.synchronizeThreads(null);   // Make sure Worker has seen the signal
        int counter = getCounter();
        LockSupport.parkNanos(1000);       // Allow some time for the other thread to race
        if (getCounter() != counter) {     // Check that no counter incremented racingly
            throw new RuntimeException("Asymmetric Dekker Synchronization failed");
        }
        try {
            thread.join();
        } catch (Exception e) {}
    }

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 10_000; ++i) {
            testAsymmetricDekkerSynchronization();
        }
    }
}
