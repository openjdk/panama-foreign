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

import java.foreign.Library;
import java.foreign.memory.LayoutType;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public class LinkToNativeInvoker {
    public static MethodHandle make(Library.Symbol symbol, CallingSequence callingSequence, LayoutType<?> ret, LayoutType<?>... args) {
        LinkToNativeSignatureShuffler shuffler =
                LinkToNativeSignatureShuffler.javaToNativeShuffler(callingSequence, Util.methodType(ret, args),
                        pos -> (pos == -1L) ? ret : args[pos]);
        MethodHandle mh;
        try {
            mh = MethodHandles.lookup().findNative(symbol, shuffler.nativeMethodType());
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
        return shuffler.adapt(mh);
    }
}
