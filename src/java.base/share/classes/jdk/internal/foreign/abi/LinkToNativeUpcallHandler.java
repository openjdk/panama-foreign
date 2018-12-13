/*
 *  Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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
 *  Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 */

package jdk.internal.foreign.abi;

import jdk.internal.foreign.Util;
import jdk.internal.foreign.memory.BoundedPointer;
import jdk.internal.vm.annotation.Stable;

import java.foreign.NativeMethodType;
import java.foreign.memory.LayoutType;
import java.foreign.memory.Pointer;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

/**
 * This class implements upcall invocation from native code through a set of specialized entry points. A specialized
 * entry point is a Java method which takes a number N of long arguments followed by a number M of double arguments;
 * possible return types for the entry point are either long, double or void.
 */
public class LinkToNativeUpcallHandler implements UpcallStub {
    @Stable
    private final MethodHandle mh;
    private final Pointer<?> entryPoint;

    public LinkToNativeUpcallHandler(MethodHandle target, CallingSequence callingSequence, NativeMethodType nmt) {
        try {
            Util.checkNoArrays(target.type());
            LinkToNativeSignatureShuffler shuffler =
                    LinkToNativeSignatureShuffler.nativeToJavaShuffler(callingSequence, nmt);
            this.mh = shuffler.adapt(target);
            this.entryPoint = BoundedPointer.createNativeVoidPointer(allocateLinkToNativeUpcallStub());
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public MethodHandle methodHandle() {
        return mh;
    }

    @Override
    public String getName() {
        return toString();
    }

    @Override
    public Pointer<?> getAddress() {
        return entryPoint;
    }

    /*** direct entry points ***/

    public static long invoke_J_JJ(LinkToNativeUpcallHandler handler, long arg0, long arg1) throws Throwable {
        return (long)handler.mh.invokeExact(arg0, arg1);
    }

    // natives

    native long allocateLinkToNativeUpcallStub();

    private static native void registerNatives();
    static {
        LinkToNativeUpcallHandler.registerNatives();
    }
}
