/*
 * Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
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

package jdk.internal.foreign;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public abstract class ResourceList {
    ResourceCleanup fst;

    abstract void add(ResourceCleanup cleanup);

    abstract void cleanup();

    final void cleanup(ResourceCleanup first) {
        ResourceCleanup current = first;
        while (current != null) {
            current.cleanup();
            current = current.next;
        }
    }

    public static abstract class ResourceCleanup {
        ResourceCleanup next;

        public abstract void cleanup();

        static ResourceCleanup DUMMY_CLEANUP = new ResourceCleanup() {
            @Override
            public void cleanup() {
                // do nothing
            }
        };

        static ResourceCleanup ofRunnable(Runnable cleanupAction) {
            return new ResourceCleanup() {
                @Override
                public void cleanup() {
                    cleanupAction.run();
                }
            };
        }
    }

    static class ConfinedResourceList extends ResourceList {
        @Override
        void add(ResourceCleanup cleanup) {
            cleanup.next = fst;
            fst = cleanup;
        }

        @Override
        void cleanup() {
            cleanup(fst);
        }
    }

    static class SharedResourceList extends ResourceList {

        static final VarHandle FST;

        static {
            try {
                FST = MethodHandles.lookup().findVarHandle(ResourceList.class, "fst", ResourceCleanup.class);
            } catch (Throwable ex) {
                throw new ExceptionInInitializerError();
            }
        }

        @Override
        void add(ResourceCleanup cleanup) {
            while (true) {
                ResourceCleanup prev = (ResourceCleanup) FST.getAcquire(this);
                cleanup.next = prev;
                ResourceCleanup newSegment = (ResourceCleanup) FST.compareAndExchangeRelease(this, prev, cleanup);
                if (newSegment == ResourceCleanup.DUMMY_CLEANUP) {
                    // too late
                    cleanup.cleanup();
                    throw new IllegalStateException("Already closed");
                } else if (newSegment == prev) {
                    return; //victory
                }
                // keep trying
            }
        }

        void cleanup() {
            //ok now we're really closing down
            ResourceCleanup prev = null;
            while (true) {
                prev = (ResourceCleanup) FST.getAcquire(this);
                // no need to check for DUMMY, since only one thread can get here!
                if (FST.weakCompareAndSetRelease(this, prev, ResourceCleanup.DUMMY_CLEANUP)) {
                    break;
                }
            }
            cleanup(prev);
        }
    }
}
