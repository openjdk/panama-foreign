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


import jdk.internal.ref.CleanerFactory;

import java.lang.ref.Cleaner;

public abstract class MemoryScope {

    //reference to keep hold onto
    final Object ref;

    public MemoryScope(Object ref) {
        this.ref = ref;
    }

    abstract void checkValidState();
    abstract void close();
    abstract boolean isShared();
    abstract boolean isAlive();

    static final class ConfinedScope extends MemoryScope {
        final Thread thread;
        final Runnable cleanupAction;
        boolean isAlive = true;

        ConfinedScope(Object ref, Thread thread, Runnable cleanupAction) {
            super(ref);
            this.thread = thread;
            this.cleanupAction = cleanupAction;
        }

        final void checkValidState() {
            if (Thread.currentThread() != thread) {
                throw new IllegalStateException("Attempt to access segment outside owning thread");
            } else if (!isAlive) {
                throw new IllegalStateException("Segment is not alive");
            }
        }

        @Override
        public boolean isAlive() {
            return isAlive;
        }

        @Override
        boolean isShared() {
            return false;
        }

        void close() {
            if (cleanupAction != null) {
                cleanupAction.run();
            }
            isAlive = false;
        }

        MemoryScope toShared(MemorySegmentImpl memorySegment) {
            isAlive = false;
            return new SharedScope(ref, cleanupAction);
        }

        MemoryScope transfer(Thread newOwner) {
            isAlive = false;
            return new ConfinedScope(ref, newOwner, cleanupAction);
        }
    }

    static final class SharedScope extends MemoryScope {

        Cleaner cleaner = CleanerFactory.cleaner();

        public SharedScope(Object ref, Runnable cleanup) {
            super(ref);
            if (cleanup != null) {
                cleaner.register(this, cleanup);
            }
        }

        void checkValidState() {
            //do nothing
        }

        @Override
        boolean isAlive() {
            return true;
        }

        @Override
        boolean isShared() {
            return true;
        }

        @Override
        void close() {
            throw new IllegalStateException("Cannot get here: shared scope should belong to pinned segment");
        }
    }
}
