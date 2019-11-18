/*
 *  Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.  Oracle designates this
 *  particular file as subject to the "Classpath" exception as provided
 *  by Oracle in the LICENSE file that accompanied this code.
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

package jdk.internal.foreign;

import java.util.concurrent.atomic.AtomicInteger;

public class MemoryScope {

    //reference to keep hold onto
    final Object ref;
    private boolean isAlive = true;

    final AtomicInteger activeCount = new AtomicInteger();

    final static int UNACQUIRED = 0;
    final static int CLOSED = -1;

    final Runnable cleanupAction;

    public MemoryScope(Object ref, Runnable cleanupAction) {
        this.ref = ref;
        this.cleanupAction = cleanupAction;
    }

    final boolean isAlive() {
        return isAlive;
    }

    final void checkAlive() {
        if (!isAlive) {
            throw new IllegalStateException("Segment is not alive");
        }
    }

    MemoryScope acquire() {
        if (activeCount.updateAndGet(i -> i == CLOSED ? CLOSED : ++i) == CLOSED) {
            //cannot get here - segment is not alive!
            throw new IllegalStateException();
        }
        return new MemoryScope(ref, this::release);
    }

    void release() {
        if (activeCount.getAndUpdate(i -> i <= UNACQUIRED ? i : --i) <= UNACQUIRED) {
            //cannot get here - we can't close segment twice
            throw new IllegalStateException();
        }
    }

    void close() {
        if (!activeCount.compareAndSet(UNACQUIRED, CLOSED)) {
            throw new UnsupportedOperationException("Cannot close a segment that has active acquired views");
        }
        isAlive = false;
        if (cleanupAction != null) {
            cleanupAction.run();
        }
    }
}
