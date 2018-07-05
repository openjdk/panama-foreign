/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
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
package jdk.internal.foreign;

import jdk.internal.foreign.memory.BoundedMemoryRegion;
import jdk.internal.foreign.memory.BoundedPointer;

import java.lang.ref.Cleaner;
import java.foreign.NativeTypes;
import java.foreign.memory.Pointer;

class UpcallStub {
    private static final Cleaner cleaner = Cleaner.create();

    static class Stub implements Runnable {
        private final Pointer<?> entryPoint;

        Stub(UpcallHandler handler) throws Throwable {
            this.entryPoint = new BoundedPointer<>(NativeTypes.VOID,
                    new BoundedMemoryRegion(NativeInvoker.allocateUpcallStub(handler), 0), 0, 0);
        }

        @Override
        public void run() {
            try {
                NativeInvoker.freeUpcallStub(entryPoint.addr());
            } catch (Throwable t) {
                // all exception in cleanser atr ignored anyway
            }
        }
    }

    private Stub stub;

    UpcallStub(UpcallHandler id) throws Throwable {
        stub = new Stub(id);
        cleaner.register(this, stub);
    }

    public Pointer<?> getEntryPoint() {
        return stub.entryPoint;
    }
}
